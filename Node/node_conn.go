package main
import (
	"net"
	"io"
	"log"
	"bytes"
	"encoding/binary"
	"encoding/json"
)

type APIRequest struct {
	Key string
	Target string
	Action string
	
	Vm string
	
	Ssl bool
}

type APICommandResult struct {
	Result string
}

type APIStatusResult struct {
	Result VMStatus
}

type APIListResult struct {
	Result []VMStatus
}

type APIVNCResult struct {
	Password string
	Port int64
}

type VMStatus struct {
	Name string
	IsPoweredOn bool
	CpuUsage float64
	RamUsage float64
	Vcpus int64
}

func handleNodeConn(nodeConn net.Conn) {
	defer nodeConn.Close()

	buf := make([]byte, 4)
	io.ReadFull(nodeConn, buf)
	bufLenByteBuf := bytes.NewBuffer(buf)
	var bufLen int32
	binary.Read(bufLenByteBuf, binary.BigEndian, &bufLen)
	
	buf = make([]byte, bufLen)
	io.ReadFull(nodeConn, buf)

	var apiRequest APIRequest
	err := json.Unmarshal(buf, &apiRequest)
	if err != nil {
		log.Printf("API request: json err: %v", err)
		return
	}
	
	if apiRequest.Key != nodeConfig.ApiKey {
		log.Printf("Wrong API key: %v", apiRequest.Key)
		return
	}
	
	//log.Printf("API request: %v", apiRequest)
	
	res := processAPIRequest(apiRequest)
	if res == nil {
		return
	}
	
	resBytes, err := json.Marshal(res)
	if err != nil {
		log.Printf("API reply: json err: %v", err)
		return
	}
	
	bufLenByteBuf = new(bytes.Buffer)
	err = binary.Write(bufLenByteBuf, binary.BigEndian, int32(len(resBytes)))
	if err != nil {
		log.Printf("API Buf error: %v", err)
	}
	buf = bufLenByteBuf.Bytes()
	nodeConn.Write(buf)
	nodeConn.Write(resBytes)
	
	//log.Printf("API reply: %v %v %v", len(resBytes), buf, string(resBytes))
}

func processAPIRequest(apiRequest APIRequest) interface{} {
	if apiRequest.Target == "vm" {
		switch apiRequest.Action {
			case "list":
				var result APIListResult
				result.Result = vmList()
				return result
			case "vnc":
				vncPort := vmGetVNCPort(apiRequest.Vm)
				if vncPort < 1 {
					break
				}
				var result APIVNCResult
				result.Password = randString(16)
				result.Port = proxyVNC(vncPort, result.Password, apiRequest.Ssl)
				return result
			case "status":
				var result APIStatusResult
				result.Result = vmGetStatus(apiRequest.Vm)
				return result
			default:
				var result APICommandResult
				result.Result = "OK"
				vmProcessCommand(apiRequest.Vm, apiRequest.Action)
				return result
		}
	}
	
	var res APICommandResult
	res.Result = "ERROR"
	return res
}
