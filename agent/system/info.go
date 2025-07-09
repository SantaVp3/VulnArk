package system

import (
	"fmt"
	"net"
	"os"
	"runtime"
	"strings"

	"github.com/shirou/gopsutil/v3/host"
)

// SystemInfo 系统信息结构
type SystemInfo struct {
	Hostname    string `json:"hostname"`
	Platform    string `json:"platform"`
	OSVersion   string `json:"os_version"`
	IPAddress   string `json:"ip_address"`
	Architecture string `json:"architecture"`
	CPUCount    int    `json:"cpu_count"`
	Memory      uint64 `json:"memory"`
}

// GetSystemInfo 获取系统信息
func GetSystemInfo() (*SystemInfo, error) {
	info := &SystemInfo{}
	
	// 获取主机名
	hostname, err := os.Hostname()
	if err != nil {
		return nil, fmt.Errorf("failed to get hostname: %v", err)
	}
	info.Hostname = hostname
	
	// 获取平台信息
	info.Platform = strings.ToUpper(runtime.GOOS)
	info.Architecture = runtime.GOARCH
	info.CPUCount = runtime.NumCPU()
	
	// 获取详细的操作系统信息
	hostInfo, err := host.Info()
	if err == nil {
		info.OSVersion = fmt.Sprintf("%s %s", hostInfo.Platform, hostInfo.PlatformVersion)
	} else {
		info.OSVersion = runtime.GOOS
	}
	
	// 获取IP地址
	ipAddress, err := getLocalIP()
	if err != nil {
		return nil, fmt.Errorf("failed to get IP address: %v", err)
	}
	info.IPAddress = ipAddress
	
	// 获取内存信息
	if hostInfo != nil {
		info.Memory = hostInfo.TotalMemory
	}
	
	return info, nil
}

// getLocalIP 获取本地IP地址
func getLocalIP() (string, error) {
	// 尝试连接到外部地址来获取本地IP
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		// 如果无法连接外部，则获取第一个非回环接口的IP
		return getFirstNonLoopbackIP()
	}
	defer conn.Close()
	
	localAddr := conn.LocalAddr().(*net.UDPAddr)
	return localAddr.IP.String(), nil
}

// getFirstNonLoopbackIP 获取第一个非回环接口的IP地址
func getFirstNonLoopbackIP() (string, error) {
	interfaces, err := net.Interfaces()
	if err != nil {
		return "", err
	}
	
	for _, iface := range interfaces {
		// 跳过回环接口和未启用的接口
		if iface.Flags&net.FlagLoopback != 0 || iface.Flags&net.FlagUp == 0 {
			continue
		}
		
		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}
		
		for _, addr := range addrs {
			var ip net.IP
			switch v := addr.(type) {
			case *net.IPNet:
				ip = v.IP
			case *net.IPAddr:
				ip = v.IP
			}
			
			// 只返回IPv4地址
			if ip != nil && ip.To4() != nil && !ip.IsLoopback() {
				return ip.String(), nil
			}
		}
	}
	
	return "127.0.0.1", nil
}

// IsWindows 检查是否为Windows系统
func IsWindows() bool {
	return runtime.GOOS == "windows"
}

// IsLinux 检查是否为Linux系统
func IsLinux() bool {
	return runtime.GOOS == "linux"
}

// GetPlatformType 获取平台类型（用于后端API）
func GetPlatformType() string {
	switch runtime.GOOS {
	case "windows":
		return "WINDOWS"
	case "linux":
		return "LINUX"
	default:
		return "LINUX" // 默认为Linux
	}
}
