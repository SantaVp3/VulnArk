package client

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"vulnark-agent/config"
)

// Client API客户端
type Client struct {
	config     *config.Config
	httpClient *http.Client
	baseURL    string
	agentID    string
	token      string
}

// NewClient 创建新的API客户端
func NewClient(cfg *config.Config) *Client {
	// 配置HTTP客户端
	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: !cfg.Security.TLSVerify,
		},
	}
	
	httpClient := &http.Client{
		Transport: transport,
		Timeout:   time.Duration(cfg.Server.Timeout) * time.Second,
	}
	
	return &Client{
		config:     cfg,
		httpClient: httpClient,
		baseURL:    cfg.Server.URL,
		agentID:    cfg.Security.AgentID,
		token:      cfg.Security.Token,
	}
}

// ApiResponse API响应结构
type ApiResponse struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
}

// RegistrationRequest 注册请求
type RegistrationRequest struct {
	Name         string `json:"name"`
	Hostname     string `json:"hostname"`
	IPAddress    string `json:"ipAddress"`
	Platform     string `json:"platform"`
	OSVersion    string `json:"osVersion"`
	AgentVersion string `json:"agentVersion"`
	Description  string `json:"description"`
}

// RegistrationResponse 注册响应
type RegistrationResponse struct {
	AgentID            string `json:"agentId"`
	Token              string `json:"token"`
	ServerTime         int64  `json:"serverTime"`
	HeartbeatInterval  int    `json:"heartbeatInterval"`
	TaskPollInterval   int    `json:"taskPollInterval"`
}

// BaselineTask 基线检查任务
type BaselineTask struct {
	ID            int64  `json:"id"`
	TaskID        string `json:"taskId"`
	Name          string `json:"name"`
	TaskType      string `json:"taskType"`
	Status        string `json:"status"`
	Configuration string `json:"configuration"`
	CheckRules    string `json:"checkRules"`
	ScheduledTime string `json:"scheduledTime"`
}

// BaselineResult 基线检查结果
type BaselineResult struct {
	CheckID        string  `json:"checkId"`
	CheckName      string  `json:"checkName"`
	Category       string  `json:"category"`
	Severity       string  `json:"severity"`
	Status         string  `json:"status"`
	Description    string  `json:"description"`
	ExpectedValue  string  `json:"expectedValue"`
	ActualValue    string  `json:"actualValue"`
	Evidence       string  `json:"evidence"`
	Recommendation string  `json:"recommendation"`
	Reference      string  `json:"reference"`
	Score          float64 `json:"score"`
}

// Register 注册Agent
func (c *Client) Register(req *RegistrationRequest) (*RegistrationResponse, error) {
	url := fmt.Sprintf("%s/api/agents/register", c.baseURL)
	
	var response ApiResponse
	if err := c.doRequest("POST", url, req, &response, false); err != nil {
		return nil, err
	}
	
	if response.Code != 200 {
		return nil, fmt.Errorf("registration failed: %s", response.Message)
	}
	
	// 解析响应数据
	dataBytes, err := json.Marshal(response.Data)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal response data: %v", err)
	}
	
	var regResp RegistrationResponse
	if err := json.Unmarshal(dataBytes, &regResp); err != nil {
		return nil, fmt.Errorf("failed to unmarshal registration response: %v", err)
	}
	
	// 更新客户端的认证信息
	c.agentID = regResp.AgentID
	c.token = regResp.Token
	
	return &regResp, nil
}

// Heartbeat 发送心跳
func (c *Client) Heartbeat() error {
	url := fmt.Sprintf("%s/api/agents/heartbeat", c.baseURL)
	
	var response ApiResponse
	if err := c.doRequest("POST", url, nil, &response, true); err != nil {
		return err
	}
	
	if response.Code != 200 {
		return fmt.Errorf("heartbeat failed: %s", response.Message)
	}
	
	return nil
}

// GetTasks 获取待执行任务
func (c *Client) GetTasks() ([]*BaselineTask, error) {
	url := fmt.Sprintf("%s/api/agents/tasks", c.baseURL)
	
	var response ApiResponse
	if err := c.doRequest("GET", url, nil, &response, true); err != nil {
		return nil, err
	}
	
	if response.Code != 200 {
		return nil, fmt.Errorf("get tasks failed: %s", response.Message)
	}
	
	// 解析任务列表
	dataBytes, err := json.Marshal(response.Data)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal tasks data: %v", err)
	}
	
	var tasks []*BaselineTask
	if err := json.Unmarshal(dataBytes, &tasks); err != nil {
		return nil, fmt.Errorf("failed to unmarshal tasks: %v", err)
	}
	
	return tasks, nil
}

// SubmitResults 提交任务结果
func (c *Client) SubmitResults(taskID string, results []*BaselineResult) error {
	url := fmt.Sprintf("%s/api/agents/results", c.baseURL)
	
	payload := map[string]interface{}{
		"taskId":  taskID,
		"results": results,
	}
	
	var response ApiResponse
	if err := c.doRequest("POST", url, payload, &response, true); err != nil {
		return err
	}
	
	if response.Code != 200 {
		return fmt.Errorf("submit results failed: %s", response.Message)
	}
	
	return nil
}

// doRequest 执行HTTP请求
func (c *Client) doRequest(method, url string, body interface{}, response interface{}, needAuth bool) error {
	var reqBody io.Reader
	
	if body != nil {
		jsonData, err := json.Marshal(body)
		if err != nil {
			return fmt.Errorf("failed to marshal request body: %v", err)
		}
		reqBody = bytes.NewBuffer(jsonData)
	}
	
	req, err := http.NewRequest(method, url, reqBody)
	if err != nil {
		return fmt.Errorf("failed to create request: %v", err)
	}
	
	// 设置请求头
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("User-Agent", "VulnArk-Agent/1.0")
	
	// 如果需要认证，添加认证头
	if needAuth {
		if c.agentID == "" || c.token == "" {
			return fmt.Errorf("agent not registered")
		}
		req.Header.Set("X-Agent-ID", c.agentID)
		req.Header.Set("Authorization", "Bearer "+c.token)
	}
	
	// 执行请求
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("request failed: %v", err)
	}
	defer resp.Body.Close()
	
	// 读取响应
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %v", err)
	}
	
	// 解析响应
	if err := json.Unmarshal(respBody, response); err != nil {
		return fmt.Errorf("failed to unmarshal response: %v", err)
	}
	
	return nil
}

// SetCredentials 设置认证信息
func (c *Client) SetCredentials(agentID, token string) {
	c.agentID = agentID
	c.token = token
}

// GetAgentID 获取Agent ID
func (c *Client) GetAgentID() string {
	return c.agentID
}

// GetToken 获取Token
func (c *Client) GetToken() string {
	return c.token
}
