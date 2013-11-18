package main
import (
	"crypto/tls"
	"encoding/json"
	"net"
	"os"
	"log"
	"sync"
)

//Global SSL/TLS config
var sslCertificates tls.Certificate

//Global Node config
var nodeConfig NodeConfig

type NodeConfig struct {
	ApiKey string
}

var sslConfigMutex sync.Mutex
func getSslConfig() *tls.Config {
	sslConfigMutex.Lock()
	defer sslConfigMutex.Unlock()
	sslConfig := new(tls.Config)
	sslConfig.Certificates = []tls.Certificate{sslCertificates}
	return sslConfig
}

func main() {
	fileReader, err := os.Open("config.json")
	if err != nil {
		log.Panicf("Load Config: open err: %v", err)
	}	
	jsonReader := json.NewDecoder(fileReader)
	err = jsonReader.Decode(&nodeConfig)
	fileReader.Close()
	if err != nil {
		log.Panicf("Load Config: json err: %v", err)
	} else {
		log.Println("Load Config: OK")
	}

	initializeLibvirt()
	
	sslCertificates, err = tls.LoadX509KeyPair("node.crt", "node.key")
	if err != nil {
		log.Panicf("Load SSL: error %v", err)
	} else {
		log.Println("Load SSL: OK")
	}

	//sslConfig := new(tls.Config)
	//sslConfig.Certificates = []tls.Certificate{sslCertificates}
	
	nodeListener, err := net.Listen("tcp4", "0.0.0.0:1532")
	if err != nil {
		log.Panicf("Node API: cannot listen: %v", err)
	}
	nodeListener = tls.NewListener(nodeListener, getSslConfig())
	log.Println("Node API: ready for commands")
	
	for {
		nodeConn, err := nodeListener.Accept()
		if nodeConn == nil {
			log.Printf("accept error: %v\n", err)
		} else {
			go handleNodeConn(nodeConn)
		}
	}
}
