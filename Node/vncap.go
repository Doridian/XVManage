package main
import (
	"bytes"
	"net"
	"fmt"
	"io"
	"os"
	"time"
	"bufio"
	"strconv"
	"encoding/json"
	"crypto/rand"
	"crypto/des"
	"crypto/tls"
	"sync/atomic"
)

//Gets a free port (might need more smartness soon or something)
var port_counter int64
func getLocalPort() int64 {
	return (atomic.AddInt64(&port_counter, 1) % 1000) + 19000
}

//Global SSL/TLS config
var sslConfig *tls.Config

func main() {
	sslCertificates, error := tls.LoadX509KeyPair("vncap.crt", "vncap.key")
	if error != nil {
		fatal("Load SSL ERROR: %v", error)
	} else {
		logout("Load SSL: OK")
	}

	sslConfig := new(tls.Config)
	sslConfig.Certificates = []tls.Certificate{sslCertificates}

	jsonListener, err := net.Listen("tcp4", "127.0.0.1:8888")
	if jsonListener == nil {
		fatal("cannot listen: %v %s", err, "l:8888")
	}
	
	for {
		jsonConn, err := jsonListener.Accept()
		if jsonConn == nil {
			logout("accept error: %v", err)
		} else {
			go handleJSONRPC(jsonConn)
		}
	}
}

//JSON request structure
type VNCJSONReq struct {
	Daddr string //Ignored
	Dport float64
	Password string
	Ws bool //Possibly implement
	Tls bool
}

//Handle one JSON request
func handleJSONRPC(jsonConn net.Conn) {
	bufferReader := bufio.NewReader(jsonConn)
	dataStr, err := bufferReader.ReadBytes('\n')
	if dataStr == nil {
		logout("byte err: %v", err)
		jsonConn.Close()
		return
	}
	var jsonData VNCJSONReq
	err = json.Unmarshal(dataStr, &jsonData)
	if err != nil {
		logout("json err: %v", err)
		jsonConn.Close()
		return
	}

	localPort := getLocalPort()

	go listenVNC(localPort, int64(jsonData.Dport), jsonData.Password, jsonData.Tls)

	io.WriteString(jsonConn, strconv.FormatInt(localPort, 10))
	io.WriteString(jsonConn, "\r\n")
	jsonConn.Close()
}

//Listen for exactly one VNC connection on specified target port (ready to proxy to local VNC)
func listenVNC(localAddrP int64, remoteAddrP int64, password string, useSSL bool) {
	localAddr := "0.0.0.0:" + strconv.FormatInt(localAddrP, 10)

	//We use all the ListenTCP here to be able to use TCPListener.SetDeadline
	localTcpAddr, err := net.ResolveTCPAddr("tcp4", localAddr)
	if localTcpAddr == nil {
		logout("cannot resolve: %v %s", err, localTcpAddr)
		return
	}

	local, err := net.ListenTCP("tcp4", localTcpAddr)
	if local == nil {
		logout("cannot listen: %v %s", err, localAddr)
		return
	}
	local.SetDeadline(time.Now().Add(time.Duration(60) * time.Second))

	//Wrap SSL/TLS if requested
	listener := net.Listener(local)
	if useSSL {
		listener = tls.NewListener(listener, sslConfig)
	}

	//Accept one connection, then close listener
	conn, err := listener.Accept()
	listener.Close()
	if conn == nil {
		logout("accept failed: %v $s", err, localAddr)
		return
	}

	go handleAuth(conn, remoteAddrP, password)
}

//Handle VNC authentication and handshaking from the remote
func handleAuth(local net.Conn, remoteAddrP int64, password string) {
	passwordBytes := make([]byte, 8)
	copy(passwordBytes, []byte(password))

	io.WriteString(local, "RFB 003.008\n")
	buf := make([]byte, 12)
	io.ReadFull(local, buf)

	//1 auth method present, type 2 (VNC authentication)
	local.Write([]byte{1, 2})
	//Read auth method response, only accept type 2
	buf = make([]byte, 1)
	io.ReadFull(local, buf)
	if buf[0] != 2 {
		local.Close()
		logout("wrong auth type: %v", buf)
		return
	}

	//Make challenge for VNC authentication
	challenge := make([]byte, 16)
	rand.Read(challenge)
	local.Write(challenge)
	
	//Read response
	response := make([]byte, 16)
	io.ReadFull(local, response)

	//VNC mirrors bits in the password used for DES (http://www.vidarholen.net/contents/junk/vnc.html)
	mirrorBits(passwordBytes)
	//Create cipher from password (VNC auth = DES encrypt challenge with password)
	responseCipher, err := des.NewCipher(passwordBytes)
	if responseCipher == nil {
		local.Close()	
		logout("cipher failed: %v", err)
		return
	}

	//Decrypt the two blocks of the challenge
	challengeDecrypted := make([]byte, 16)
	responseCipher.Decrypt(challengeDecrypted[:8], response[:8])
	responseCipher.Decrypt(challengeDecrypted[8:], response[8:])

	//Compare challenge with decrypted challenge
	if bytes.Equal(challengeDecrypted, challenge) {
		go forward(local, remoteAddrP)
	} else {
		//U32 for "failed" (0 is okay), then U32 for length of reason
		local.Write([]byte{0,0,0,1, 0,0,0,14})
		io.WriteString(local, "Wrong password") //len = 5 + 1 + 8 = 14
		local.Close()
		logout("auth failed %v %v %v\n", challenge, challengeDecrypted, passwordBytes)
		return
	}
}

//Establish connection to target VNC server and do basic handshaking
func forward(local net.Conn, remoteAddrP int64) {
	remoteAddr := "127.0.0.1:" + strconv.FormatInt(remoteAddrP, 10)
	remote, err := net.Dial("tcp", remoteAddr)
	if remote == nil {
		local.Close()
		logout("remote dial failed: %v", err)
		return
	}

	//Read version string from VNC server
	buf := make([]byte, 12)
	io.ReadFull(remote, buf)
	//Reply with v3.8 (the version we use)
	io.WriteString(local, "RFB 003.008\n")

	//Read 1 byte (count of auth methods)
	buf = make([]byte, 1)
	io.ReadFull(remote, buf)
	//Read auth methods
	buf = make([]byte, buf[0])
	io.ReadFull(remote, buf)
	
	//use auth method 1 (no authentication)
	remote.Write([]byte{1})

	//Real proxying here
	go io.Copy(local, remote)
	go io.Copy(remote, local)
}

func fatal(s string, a ... interface{}) {
	logout(s, a)
	os.Exit(2)
}

func logout(s string, a ... interface{}) {
	fmt.Fprintf(os.Stderr, s, a)
}

//Bit mirroring function for DES encryption that VNC uses
func mirrorBits(k []byte) {
	s := byte(0)
	kSize := len(k)
	for i := 0; i < kSize; i++ {
		s = k[i]
		s = (((s >> 1) & 0x55) | ((s << 1) & 0xaa))
		s = (((s >> 2) & 0x33) | ((s << 2) & 0xcc))
		s = (((s >> 4) & 0x0f) | ((s << 4) & 0xf0))
		k[i] = s
	}
}
