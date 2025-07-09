package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"os/signal"
	"path/filepath"
	"strings"
	"syscall"

	"vulnark-agent/agent"
	"vulnark-agent/config"
	"vulnark-agent/system"
)

const (
	Version = "1.0.0"
	AppName = "VulnArk Agent"
)

func main() {
	// 命令行参数
	var (
		configFile = flag.String("config", "", "配置文件路径")
		showInfo   = flag.Bool("info", false, "显示系统信息")
		showStatus = flag.Bool("status", false, "显示Agent状态")
		version    = flag.Bool("version", false, "显示版本信息")
		daemon     = flag.Bool("daemon", false, "以守护进程模式运行")
	)
	flag.Parse()

	// 显示版本信息
	if *version {
		fmt.Printf("%s v%s\n", AppName, Version)
		fmt.Printf("Platform: %s\n", system.GetPlatformType())
		return
	}

	// 显示系统信息
	if *showInfo {
		showSystemInfo()
		return
	}

	// 加载配置
	cfg, err := config.LoadConfig(*configFile)
	if err != nil {
		log.Fatalf("加载配置失败: %v", err)
	}

	// 设置日志
	setupLogging(cfg)

	// 创建Agent实例
	agentInstance := agent.NewAgent(cfg)

	// 显示状态
	if *showStatus {
		showAgentStatus(agentInstance)
		return
	}

	// 启动Agent
	if err := agentInstance.Start(); err != nil {
		log.Fatalf("启动Agent失败: %v", err)
	}

	// 如果是守护进程模式，在后台运行
	if *daemon {
		log.Println("以守护进程模式运行...")
	}

	// 等待中断信号
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)

	log.Printf("%s v%s 已启动", AppName, Version)
	log.Println("按 Ctrl+C 停止Agent")

	// 等待信号
	<-sigChan
	log.Println("收到停止信号，正在关闭Agent...")

	// 停止Agent
	agentInstance.Stop()
	log.Println("Agent已安全关闭")
}

// showSystemInfo 显示系统信息
func showSystemInfo() {
	fmt.Printf("%s v%s - 系统信息\n", AppName, Version)
	fmt.Println(strings.Repeat("=", 50))

	sysInfo, err := system.GetSystemInfo()
	if err != nil {
		log.Fatalf("获取系统信息失败: %v", err)
	}

	fmt.Printf("主机名:     %s\n", sysInfo.Hostname)
	fmt.Printf("平台:       %s\n", sysInfo.Platform)
	fmt.Printf("操作系统:   %s\n", sysInfo.OSVersion)
	fmt.Printf("架构:       %s\n", sysInfo.Architecture)
	fmt.Printf("IP地址:     %s\n", sysInfo.IPAddress)
	fmt.Printf("CPU核心数:  %d\n", sysInfo.CPUCount)
	if sysInfo.Memory > 0 {
		fmt.Printf("内存:       %.2f GB\n", float64(sysInfo.Memory)/(1024*1024*1024))
	}
}

// showAgentStatus 显示Agent状态
func showAgentStatus(agentInstance *agent.Agent) {
	fmt.Printf("%s v%s - Agent状态\n", AppName, Version)
	fmt.Println(strings.Repeat("=", 50))

	status := agentInstance.GetStatus()
	
	fmt.Printf("运行状态:   %v\n", status["running"])
	if agentID, ok := status["agent_id"].(string); ok && agentID != "" {
		fmt.Printf("Agent ID:   %s\n", agentID)
	} else {
		fmt.Printf("Agent ID:   未注册\n")
	}
	fmt.Printf("Agent名称:  %v\n", status["name"])
	fmt.Printf("平台:       %v\n", status["platform"])
	if hostname, ok := status["hostname"].(string); ok {
		fmt.Printf("主机名:     %s\n", hostname)
	}
	if ipAddress, ok := status["ip_address"].(string); ok {
		fmt.Printf("IP地址:     %s\n", ipAddress)
	}
	if osVersion, ok := status["os_version"].(string); ok {
		fmt.Printf("操作系统:   %s\n", osVersion)
	}
}

// setupLogging 设置日志
func setupLogging(cfg *config.Config) {
	// 设置日志格式
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	// 如果配置了日志文件，写入文件
	if cfg.Logging.File != "" {
		// 确保日志目录存在
		logDir := filepath.Dir(cfg.Logging.File)
		if err := os.MkdirAll(logDir, 0755); err != nil {
			log.Printf("警告: 无法创建日志目录 %s: %v", logDir, err)
			return
		}

		// 打开日志文件
		logFile, err := os.OpenFile(cfg.Logging.File, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
		if err != nil {
			log.Printf("警告: 无法打开日志文件 %s: %v", cfg.Logging.File, err)
			return
		}

		log.SetOutput(logFile)
		log.Printf("日志输出到文件: %s", cfg.Logging.File)
	}
}


