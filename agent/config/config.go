package config

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"

	"gopkg.in/yaml.v3"
)

// Config Agent配置结构
type Config struct {
	Server   ServerConfig   `yaml:"server"`
	Agent    AgentConfig    `yaml:"agent"`
	Security SecurityConfig `yaml:"security"`
	Logging  LoggingConfig  `yaml:"logging"`
}

// ServerConfig 服务器配置
type ServerConfig struct {
	URL             string `yaml:"url"`
	Timeout         int    `yaml:"timeout"`
	HeartbeatInterval int  `yaml:"heartbeat_interval"`
	TaskPollInterval  int  `yaml:"task_poll_interval"`
	RetryCount       int    `yaml:"retry_count"`
	RetryDelay       int    `yaml:"retry_delay"`
}

// AgentConfig Agent配置
type AgentConfig struct {
	Name        string `yaml:"name"`
	Description string `yaml:"description"`
	WorkDir     string `yaml:"work_dir"`
	LogDir      string `yaml:"log_dir"`
	TempDir     string `yaml:"temp_dir"`
	MaxTasks    int    `yaml:"max_tasks"`
}

// SecurityConfig 安全配置
type SecurityConfig struct {
	AgentID       string `yaml:"agent_id"`
	Token         string `yaml:"token"`
	TLSVerify     bool   `yaml:"tls_verify"`
	CertFile      string `yaml:"cert_file"`
	KeyFile       string `yaml:"key_file"`
	CAFile        string `yaml:"ca_file"`
}

// LoggingConfig 日志配置
type LoggingConfig struct {
	Level      string `yaml:"level"`
	File       string `yaml:"file"`
	MaxSize    int    `yaml:"max_size"`
	MaxBackups int    `yaml:"max_backups"`
	MaxAge     int    `yaml:"max_age"`
	Compress   bool   `yaml:"compress"`
}

// DefaultConfig 返回默认配置
func DefaultConfig() *Config {
	hostname, _ := os.Hostname()
	
	return &Config{
		Server: ServerConfig{
			URL:               "http://localhost:8080",
			Timeout:           30,
			HeartbeatInterval: 30,
			TaskPollInterval:  60,
			RetryCount:        3,
			RetryDelay:        5,
		},
		Agent: AgentConfig{
			Name:        fmt.Sprintf("vulnark-agent-%s", hostname),
			Description: "VulnArk Baseline Scanner Agent",
			WorkDir:     getDefaultWorkDir(),
			LogDir:      getDefaultLogDir(),
			TempDir:     getDefaultTempDir(),
			MaxTasks:    5,
		},
		Security: SecurityConfig{
			TLSVerify: true,
		},
		Logging: LoggingConfig{
			Level:      "info",
			File:       "",
			MaxSize:    100,
			MaxBackups: 3,
			MaxAge:     28,
			Compress:   true,
		},
	}
}

// LoadConfig 从文件加载配置
func LoadConfig(configFile string) (*Config, error) {
	config := DefaultConfig()
	
	if configFile == "" {
		configFile = getDefaultConfigFile()
	}
	
	// 如果配置文件不存在，创建默认配置文件
	if _, err := os.Stat(configFile); os.IsNotExist(err) {
		if err := SaveConfig(config, configFile); err != nil {
			return nil, fmt.Errorf("failed to create default config: %v", err)
		}
		return config, nil
	}
	
	data, err := os.ReadFile(configFile)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %v", err)
	}
	
	if err := yaml.Unmarshal(data, config); err != nil {
		return nil, fmt.Errorf("failed to parse config file: %v", err)
	}
	
	// 验证配置
	if err := config.Validate(); err != nil {
		return nil, fmt.Errorf("invalid config: %v", err)
	}
	
	return config, nil
}

// SaveConfig 保存配置到文件
func SaveConfig(config *Config, configFile string) error {
	// 确保目录存在
	dir := filepath.Dir(configFile)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create config directory: %v", err)
	}
	
	data, err := yaml.Marshal(config)
	if err != nil {
		return fmt.Errorf("failed to marshal config: %v", err)
	}
	
	if err := os.WriteFile(configFile, data, 0600); err != nil {
		return fmt.Errorf("failed to write config file: %v", err)
	}
	
	return nil
}

// Validate 验证配置
func (c *Config) Validate() error {
	if c.Server.URL == "" {
		return fmt.Errorf("server URL is required")
	}
	
	if c.Agent.Name == "" {
		return fmt.Errorf("agent name is required")
	}
	
	if c.Server.HeartbeatInterval <= 0 {
		return fmt.Errorf("heartbeat interval must be positive")
	}
	
	if c.Server.TaskPollInterval <= 0 {
		return fmt.Errorf("task poll interval must be positive")
	}
	
	return nil
}

// getDefaultConfigFile 获取默认配置文件路径
func getDefaultConfigFile() string {
	if runtime.GOOS == "windows" {
		return filepath.Join(os.Getenv("PROGRAMDATA"), "VulnArk", "agent", "config.yaml")
	}
	return "/etc/vulnark/agent/config.yaml"
}

// getDefaultWorkDir 获取默认工作目录
func getDefaultWorkDir() string {
	if runtime.GOOS == "windows" {
		return filepath.Join(os.Getenv("PROGRAMDATA"), "VulnArk", "agent")
	}
	return "/var/lib/vulnark/agent"
}

// getDefaultLogDir 获取默认日志目录
func getDefaultLogDir() string {
	if runtime.GOOS == "windows" {
		return filepath.Join(os.Getenv("PROGRAMDATA"), "VulnArk", "agent", "logs")
	}
	return "/var/log/vulnark/agent"
}

// getDefaultTempDir 获取默认临时目录
func getDefaultTempDir() string {
	if runtime.GOOS == "windows" {
		return filepath.Join(os.Getenv("TEMP"), "vulnark-agent")
	}
	return "/tmp/vulnark-agent"
}

// EnsureDirectories 确保必要的目录存在
func (c *Config) EnsureDirectories() error {
	dirs := []string{
		c.Agent.WorkDir,
		c.Agent.LogDir,
		c.Agent.TempDir,
	}
	
	for _, dir := range dirs {
		if err := os.MkdirAll(dir, 0755); err != nil {
			return fmt.Errorf("failed to create directory %s: %v", dir, err)
		}
	}
	
	return nil
}
