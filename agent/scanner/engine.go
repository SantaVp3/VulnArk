package scanner

import (
	"encoding/json"
	"fmt"
	"log"
	"os/exec"
	"runtime"
	"strings"
	"time"

	"vulnark-agent/client"
)

// Engine 基线检查引擎
type Engine struct {
	platform string
}

// NewEngine 创建新的检查引擎
func NewEngine() *Engine {
	return &Engine{
		platform: runtime.GOOS,
	}
}

// CheckRule 检查规则结构
type CheckRule struct {
	RuleID         string  `json:"ruleId"`
	Name           string  `json:"name"`
	Category       string  `json:"category"`
	Description    string  `json:"description"`
	Severity       string  `json:"severity"`
	Platform       string  `json:"platform"`
	CheckScript    string  `json:"checkScript"`
	ExpectedValue  string  `json:"expectedValue"`
	Recommendation string  `json:"recommendation"`
	Reference      string  `json:"reference"`
	Score          float64 `json:"score"`
}

// TaskConfiguration 任务配置
type TaskConfiguration struct {
	Rules []CheckRule `json:"rules"`
}

// ExecuteTask 执行基线检查任务
func (e *Engine) ExecuteTask(task *client.BaselineTask) ([]*client.BaselineResult, error) {
	log.Printf("开始执行基线检查任务: %s", task.Name)
	
	// 解析任务配置
	var config TaskConfiguration
	if err := json.Unmarshal([]byte(task.CheckRules), &config); err != nil {
		return nil, fmt.Errorf("failed to parse task configuration: %v", err)
	}
	
	var results []*client.BaselineResult
	
	// 执行每个检查规则
	for _, rule := range config.Rules {
		// 检查平台兼容性
		if !e.isPlatformCompatible(rule.Platform) {
			log.Printf("跳过不兼容的规则: %s (平台: %s)", rule.Name, rule.Platform)
			continue
		}
		
		result := e.executeRule(&rule)
		results = append(results, result)
		
		log.Printf("完成检查规则: %s, 结果: %s", rule.Name, result.Status)
	}
	
	log.Printf("基线检查任务完成: %s, 共检查 %d 项", task.Name, len(results))
	return results, nil
}

// executeRule 执行单个检查规则
func (e *Engine) executeRule(rule *CheckRule) *client.BaselineResult {
	result := &client.BaselineResult{
		CheckID:        rule.RuleID,
		CheckName:      rule.Name,
		Category:       rule.Category,
		Severity:       rule.Severity,
		Description:    rule.Description,
		ExpectedValue:  rule.ExpectedValue,
		Recommendation: rule.Recommendation,
		Reference:      rule.Reference,
		Score:          rule.Score,
	}
	
	// 执行检查脚本
	actualValue, err := e.executeScript(rule.CheckScript)
	if err != nil {
		result.Status = "ERROR"
		result.ActualValue = ""
		result.Evidence = fmt.Sprintf("执行检查脚本失败: %v", err)
		return result
	}
	
	result.ActualValue = actualValue
	result.Evidence = fmt.Sprintf("检查脚本输出: %s", actualValue)
	
	// 比较实际值与期望值
	if e.compareValues(actualValue, rule.ExpectedValue) {
		result.Status = "PASS"
	} else {
		result.Status = "FAIL"
	}
	
	return result
}

// executeScript 执行检查脚本
func (e *Engine) executeScript(script string) (string, error) {
	if script == "" {
		return "", fmt.Errorf("empty script")
	}
	
	var cmd *exec.Cmd
	
	// 根据平台选择执行方式
	if runtime.GOOS == "windows" {
		// Windows使用PowerShell或cmd
		if strings.Contains(script, "Get-") || strings.Contains(script, "powershell") {
			cmd = exec.Command("powershell", "-Command", script)
		} else {
			cmd = exec.Command("cmd", "/c", script)
		}
	} else {
		// Linux/Unix使用bash
		cmd = exec.Command("bash", "-c", script)
	}
	
	// 设置超时
	timeout := 30 * time.Second
	
	// 执行命令
	output, err := e.runCommandWithTimeout(cmd, timeout)
	if err != nil {
		return "", fmt.Errorf("script execution failed: %v", err)
	}
	
	return strings.TrimSpace(string(output)), nil
}

// runCommandWithTimeout 带超时的命令执行
func (e *Engine) runCommandWithTimeout(cmd *exec.Cmd, timeout time.Duration) ([]byte, error) {
	// 创建一个通道来接收命令执行结果
	done := make(chan error, 1)
	var output []byte
	var err error
	
	go func() {
		output, err = cmd.Output()
		done <- err
	}()
	
	// 等待命令完成或超时
	select {
	case err := <-done:
		return output, err
	case <-time.After(timeout):
		// 超时，杀死进程
		if cmd.Process != nil {
			cmd.Process.Kill()
		}
		return nil, fmt.Errorf("command timeout after %v", timeout)
	}
}

// compareValues 比较实际值与期望值
func (e *Engine) compareValues(actual, expected string) bool {
	if expected == "" {
		return true // 如果没有期望值，认为检查通过
	}
	
	actual = strings.TrimSpace(actual)
	expected = strings.TrimSpace(expected)
	
	// 简单的字符串比较
	// 可以根据需要扩展为更复杂的比较逻辑（正则表达式、数值比较等）
	return strings.EqualFold(actual, expected)
}

// isPlatformCompatible 检查平台兼容性
func (e *Engine) isPlatformCompatible(rulePlatform string) bool {
	if rulePlatform == "BOTH" {
		return true
	}
	
	currentPlatform := strings.ToUpper(e.platform)
	if currentPlatform == "WINDOWS" && rulePlatform == "WINDOWS" {
		return true
	}
	if currentPlatform == "LINUX" && rulePlatform == "LINUX" {
		return true
	}
	
	return false
}

// GetSupportedPlatforms 获取支持的平台列表
func (e *Engine) GetSupportedPlatforms() []string {
	return []string{"WINDOWS", "LINUX"}
}

// ValidateScript 验证脚本语法（基础验证）
func (e *Engine) ValidateScript(script string) error {
	if script == "" {
		return fmt.Errorf("script cannot be empty")
	}
	
	// 检查危险命令
	dangerousCommands := []string{
		"rm -rf", "del /f", "format", "fdisk",
		"shutdown", "reboot", "halt", "poweroff",
		"dd if=", "mkfs", "parted",
	}
	
	scriptLower := strings.ToLower(script)
	for _, dangerous := range dangerousCommands {
		if strings.Contains(scriptLower, dangerous) {
			return fmt.Errorf("script contains dangerous command: %s", dangerous)
		}
	}
	
	return nil
}
