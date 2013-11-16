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
func getListenPort() int64 {
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

	listenPort := getListenPort()

	go listenVNC(listenPort, int64(jsonData.Dport), jsonData.Password, jsonData.Tls)

	io.WriteString(jsonConn, strconv.FormatInt(listenPort, 10))
	io.WriteString(jsonConn, "\r\n")
	jsonConn.Close()
}

//Listen for exactly one VNC connection on specified target port (ready to proxy to local VNC)
func listenVNC(listenPort int64, vncPort int64, password string, useSSL bool) {
	listenAddr := "0.0.0.0:" + strconv.FormatInt(listenPort, 10)

	//We use all the ListenTCP here to be able to use TCPListener.SetDeadline
	listenTcpAddr, err := net.ResolveTCPAddr("tcp4", listenAddr)
	if listenTcpAddr == nil {
		logout("cannot resolve: %v %s", err, listenTcpAddr)
		return
	}

	tcpListener, err := net.ListenTCP("tcp4", listenTcpAddr)
	if tcpListener == nil {
		logout("cannot listen: %v %s", err, listenAddr)
		return
	}
	tcpListener.SetDeadline(time.Now().Add(time.Duration(60) * time.Second))

	//Wrap SSL/TLS if requested
	listener := net.Listener(tcpListener)
	if useSSL {
		listener = tls.NewListener(listener, sslConfig)
	}

	//Accept one connection, then close listener
	clientConn, err := listener.Accept()
	listener.Close()
	if clientConn == nil {
		logout("accept failed: %v $s", err, listenAddr)
		return
	}

	go handleAuth(clientConn, vncPort, password)
}

//Handle VNC authentication and handshaking from the remote
func handleAuth(clientConn net.Conn, vncPort int64, password string) {
	passwordBytes := make([]byte, 8)
	copy(passwordBytes, []byte(password))

	io.WriteString(clientConn, "RFB 003.008\n")
	buf := make([]byte, 12)
	io.ReadFull(clientConn, buf)

	//1 auth method present, type 2 (VNC authentication)
	clientConn.Write([]byte{1, 2})
	//Read auth method response, only accept type 2
	buf = make([]byte, 1)
	io.ReadFull(clientConn, buf)
	if buf[0] != 2 {
		clientConn.Close()
		logout("wrong auth type: %v", buf)
		return
	}

	//Make challenge for VNC authentication
	challenge := make([]byte, 16)
	rand.Read(challenge)
	clientConn.Write(challenge)
	
	//Read response
	response := make([]byte, 16)
	io.ReadFull(clientConn, response)

	//VNC mirrors bits in the password used for DES (http://www.vidarholen.net/contents/junk/vnc.html)
	mirrorBits(passwordBytes)
	//Create cipher from password (VNC auth = DES encrypt challenge with password)
	responseCipher, err := des.NewCipher(passwordBytes)
	if responseCipher == nil {
		clientConn.Close()	
		logout("cipher failed: %v", err)
		return
	}

	//Decrypt the two blocks of the challenge
	challengeDecrypted := make([]byte, 16)
	responseCipher.Decrypt(challengeDecrypted[:8], response[:8])
	responseCipher.Decrypt(challengeDecrypted[8:], response[8:])

	//Compare challenge with decrypted challenge
	if bytes.Equal(challengeDecrypted, challenge) {
		go forward(clientConn, vncPort)
	} else {
		//U32 for "failed" (0 is okay), then U32 for length of reason
		clientConn.Write([]byte{0,0,0,1, 0,0,0,14})
		io.WriteString(clientConn, "Wrong password") //len = 5 + 1 + 8 = 14
		clientConn.Close()
		logout("auth failed %v %v %v\n", challenge, challengeDecrypted, passwordBytes)
		return
	}
}

//Establish connection to target VNC server and do basic handshaking
func forward(clientConn net.Conn, vncPort int64) {
	vncAddr := "127.0.0.1:" + strconv.FormatInt(vncPort, 10)
	vncConn, err := net.Dial("tcp", vncAddr)
	if vncConn == nil {
		clientConn.Close()
		logout("remote dial failed: %v", err)
		return
	}

	//Read version string from VNC server
	buf := make([]byte, 12)
	io.ReadFull(vncConn, buf)
	//Reply with v3.8 (the version we use)
	io.WriteString(vncConn, "RFB 003.008\n")

	//Read 1 byte (count of auth methods)
	buf = make([]byte, 1)
	io.ReadFull(vncConn, buf)
	//Read auth methods
	buf = make([]byte, buf[0])
	io.ReadFull(vncConn, buf)
	
	//use auth method 1 (no authentication)
	vncConn.Write([]byte{1})

	//Real proxying here
	go io.Copy(clientConn, vncConn)
	go io.Copy(vncConn, clientConn)
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
