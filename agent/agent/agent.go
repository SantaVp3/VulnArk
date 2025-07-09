package agent

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"vulnark-agent/client"
	"vulnark-agent/config"
	"vulnark-agent/scanner"
	"vulnark-agent/system"
)

// Agent 主要的Agent结构
type Agent struct {
	config    *config.Config
	client    *client.Client
	scanner   *scanner.Engine
	ctx       context.Context
	cancel    context.CancelFunc
	wg        sync.WaitGroup
	isRunning bool
	mu        sync.RWMutex
}

// NewAgent 创建新的Agent实例
func NewAgent(cfg *config.Config) *Agent {
	ctx, cancel := context.WithCancel(context.Background())
	
	return &Agent{
		config:  cfg,
		client:  client.NewClient(cfg),
		scanner: scanner.NewEngine(),
		ctx:     ctx,
		cancel:  cancel,
	}
}

// Start 启动Agent
func (a *Agent) Start() error {
	a.mu.Lock()
	defer a.mu.Unlock()
	
	if a.isRunning {
		return fmt.Errorf("agent is already running")
	}
	
	log.Println("启动VulnArk Agent...")
	
	// 确保必要的目录存在
	if err := a.config.EnsureDirectories(); err != nil {
		return fmt.Errorf("failed to create directories: %v", err)
	}
	
	// 如果没有注册信息，先进行注册
	if a.config.Security.AgentID == "" || a.config.Security.Token == "" {
		if err := a.register(); err != nil {
			return fmt.Errorf("failed to register agent: %v", err)
		}
	} else {
		// 设置已有的认证信息
		a.client.SetCredentials(a.config.Security.AgentID, a.config.Security.Token)
	}
	
	a.isRunning = true
	
	// 启动心跳协程
	a.wg.Add(1)
	go a.heartbeatLoop()
	
	// 启动任务轮询协程
	a.wg.Add(1)
	go a.taskPollLoop()
	
	log.Printf("Agent启动成功 (ID: %s)", a.config.Security.AgentID)
	return nil
}

// Stop 停止Agent
func (a *Agent) Stop() {
	a.mu.Lock()
	defer a.mu.Unlock()
	
	if !a.isRunning {
		return
	}
	
	log.Println("正在停止Agent...")
	
	// 取消所有协程
	a.cancel()
	
	// 等待所有协程结束
	a.wg.Wait()
	
	a.isRunning = false
	log.Println("Agent已停止")
}

// IsRunning 检查Agent是否正在运行
func (a *Agent) IsRunning() bool {
	a.mu.RLock()
	defer a.mu.RUnlock()
	return a.isRunning
}

// register 注册Agent到服务器
func (a *Agent) register() error {
	log.Println("正在注册Agent到服务器...")
	
	// 获取系统信息
	sysInfo, err := system.GetSystemInfo()
	if err != nil {
		return fmt.Errorf("failed to get system info: %v", err)
	}
	
	// 构建注册请求
	req := &client.RegistrationRequest{
		Name:         a.config.Agent.Name,
		Hostname:     sysInfo.Hostname,
		IPAddress:    sysInfo.IPAddress,
		Platform:     sysInfo.Platform,
		OSVersion:    sysInfo.OSVersion,
		AgentVersion: "1.0.0",
		Description:  a.config.Agent.Description,
	}
	
	// 发送注册请求
	resp, err := a.client.Register(req)
	if err != nil {
		return fmt.Errorf("registration failed: %v", err)
	}
	
	// 保存注册信息
	a.config.Security.AgentID = resp.AgentID
	a.config.Security.Token = resp.Token
	
	// 更新配置中的间隔时间
	if resp.HeartbeatInterval > 0 {
		a.config.Server.HeartbeatInterval = resp.HeartbeatInterval
	}
	if resp.TaskPollInterval > 0 {
		a.config.Server.TaskPollInterval = resp.TaskPollInterval
	}
	
	// 保存配置到文件
	if err := config.SaveConfig(a.config, ""); err != nil {
		log.Printf("警告: 无法保存配置文件: %v", err)
	}
	
	log.Printf("Agent注册成功 (ID: %s)", resp.AgentID)
	return nil
}

// heartbeatLoop 心跳循环
func (a *Agent) heartbeatLoop() {
	defer a.wg.Done()
	
	ticker := time.NewTicker(time.Duration(a.config.Server.HeartbeatInterval) * time.Second)
	defer ticker.Stop()
	
	// 立即发送一次心跳
	if err := a.sendHeartbeat(); err != nil {
		log.Printf("心跳发送失败: %v", err)
	}
	
	for {
		select {
		case <-a.ctx.Done():
			return
		case <-ticker.C:
			if err := a.sendHeartbeat(); err != nil {
				log.Printf("心跳发送失败: %v", err)
			}
		}
	}
}

// sendHeartbeat 发送心跳
func (a *Agent) sendHeartbeat() error {
	if err := a.client.Heartbeat(); err != nil {
		return err
	}
	
	log.Printf("心跳发送成功")
	return nil
}

// taskPollLoop 任务轮询循环
func (a *Agent) taskPollLoop() {
	defer a.wg.Done()
	
	ticker := time.NewTicker(time.Duration(a.config.Server.TaskPollInterval) * time.Second)
	defer ticker.Stop()
	
	for {
		select {
		case <-a.ctx.Done():
			return
		case <-ticker.C:
			if err := a.pollAndExecuteTasks(); err != nil {
				log.Printf("任务轮询失败: %v", err)
			}
		}
	}
}

// pollAndExecuteTasks 轮询并执行任务
func (a *Agent) pollAndExecuteTasks() error {
	// 获取待执行任务
	tasks, err := a.client.GetTasks()
	if err != nil {
		return fmt.Errorf("failed to get tasks: %v", err)
	}
	
	if len(tasks) == 0 {
		log.Printf("没有待执行的任务")
		return nil
	}
	
	log.Printf("获取到 %d 个待执行任务", len(tasks))
	
	// 执行每个任务
	for _, task := range tasks {
		if err := a.executeTask(task); err != nil {
			log.Printf("任务执行失败 %s: %v", task.TaskID, err)
		}
	}
	
	return nil
}

// executeTask 执行单个任务
func (a *Agent) executeTask(task *client.BaselineTask) error {
	log.Printf("开始执行任务: %s (%s)", task.Name, task.TaskID)
	
	// 执行基线检查
	results, err := a.scanner.ExecuteTask(task)
	if err != nil {
		return fmt.Errorf("task execution failed: %v", err)
	}
	
	// 提交结果
	if err := a.client.SubmitResults(task.TaskID, results); err != nil {
		return fmt.Errorf("failed to submit results: %v", err)
	}
	
	log.Printf("任务执行完成: %s, 检查项: %d", task.TaskID, len(results))
	return nil
}

// GetStatus 获取Agent状态
func (a *Agent) GetStatus() map[string]interface{} {
	a.mu.RLock()
	defer a.mu.RUnlock()
	
	status := map[string]interface{}{
		"running":   a.isRunning,
		"agent_id":  a.config.Security.AgentID,
		"name":      a.config.Agent.Name,
		"platform":  system.GetPlatformType(),
	}
	
	if sysInfo, err := system.GetSystemInfo(); err == nil {
		status["hostname"] = sysInfo.Hostname
		status["ip_address"] = sysInfo.IPAddress
		status["os_version"] = sysInfo.OSVersion
	}
	
	return status
}
