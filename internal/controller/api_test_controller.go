package controller

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/pkg/utils"
)

// APITestController API测试控制器
type APITestController struct{}

// NewAPITestController 创建API测试控制器
func NewAPITestController() *APITestController {
	return &APITestController{}
}

// validateURL 验证URL是否安全，防止SSRF攻击
func validateURL(rawURL string) error {
	parsedURL, err := url.Parse(rawURL)
	if err != nil {
		return fmt.Errorf("无效的URL格式")
	}

	// 只允许HTTP和HTTPS协议
	if parsedURL.Scheme != "http" && parsedURL.Scheme != "https" {
		return fmt.Errorf("不支持的协议，只允许HTTP和HTTPS")
	}

	// 获取主机名
	hostname := parsedURL.Hostname()
	if hostname == "" {
		return fmt.Errorf("无效的主机名")
	}

	// 白名单域名（可配置）
	allowedDomains := []string{
		"api.vulnark.com",
		"test.vulnark.com",
		"localhost",
		"127.0.0.1",
		"httpbin.org", // 用于测试的公共API
		"jsonplaceholder.typicode.com", // 用于测试的公共API
	}

	// 检查域名白名单
	domainAllowed := false
	for _, domain := range allowedDomains {
		if hostname == domain {
			domainAllowed = true
			break
		}
	}

	if !domainAllowed {
		return fmt.Errorf("不允许访问的域名: %s", hostname)
	}

	// 检查是否为IP地址
	if ip := net.ParseIP(hostname); ip != nil {
		// 禁止访问内网地址
		if ip.IsLoopback() && hostname != "127.0.0.1" && hostname != "localhost" {
			return fmt.Errorf("不允许访问回环地址")
		}

		if ip.IsPrivate() && hostname != "127.0.0.1" {
			return fmt.Errorf("不允许访问私有网络地址")
		}

		// 禁止访问特殊地址
		if ip.IsMulticast() || ip.IsUnspecified() {
			return fmt.Errorf("不允许访问特殊网络地址")
		}
	}

	// 检查端口范围
	port := parsedURL.Port()
	if port != "" {
		// 只允许常见的HTTP端口
		allowedPorts := []string{"80", "443", "8080", "8443", "3000", "8000"}
		portAllowed := false
		for _, allowedPort := range allowedPorts {
			if port == allowedPort {
				portAllowed = true
				break
			}
		}
		if !portAllowed {
			return fmt.Errorf("不允许的端口: %s", port)
		}
	}

	return nil
}

// APITestRequest API测试请求
type APITestRequest struct {
	Method  string                 `json:"method" binding:"required,oneof=GET POST PUT DELETE PATCH"`
	URL     string                 `json:"url" binding:"required"`
	Headers map[string]string      `json:"headers"`
	Body    map[string]interface{} `json:"body"`
	Timeout int                    `json:"timeout"` // 超时时间（秒）
}

// APITestResponse API测试响应
type APITestResponse struct {
	StatusCode   int                    `json:"status_code"`
	Headers      map[string][]string    `json:"headers"`
	Body         interface{}            `json:"body"`
	ResponseTime int64                  `json:"response_time"` // 响应时间（毫秒）
	Error        string                 `json:"error,omitempty"`
}

// APIEndpoint API端点信息
type APIEndpoint struct {
	Method      string            `json:"method"`
	Path        string            `json:"path"`
	Description string            `json:"description"`
	Tags        []string          `json:"tags"`
	Parameters  []APIParameter    `json:"parameters"`
	Responses   map[string]string `json:"responses"`
}

// APIParameter API参数信息
type APIParameter struct {
	Name        string `json:"name"`
	In          string `json:"in"` // query, path, header, body
	Type        string `json:"type"`
	Required    bool   `json:"required"`
	Description string `json:"description"`
	Example     string `json:"example"`
}

// TestAPI 测试API接口
// @Summary 测试API接口
// @Description 发送HTTP请求测试指定的API接口
// @Tags API测试
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body APITestRequest true "API测试请求"
// @Success 200 {object} model.Response{data=APITestResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/api-test/test [post]
func (c *APITestController) TestAPI(ctx *gin.Context) {
	var req APITestRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	// 验证URL安全性，防止SSRF攻击
	if err := validateURL(req.URL); err != nil {
		utils.BadRequestResponse(ctx, "URL验证失败: "+err.Error())
		return
	}

	// 设置默认超时时间
	if req.Timeout <= 0 {
		req.Timeout = 30
	}

	startTime := time.Now()

	// 准备请求体
	var bodyReader io.Reader
	if req.Body != nil && (req.Method == "POST" || req.Method == "PUT" || req.Method == "PATCH") {
		bodyBytes, err := json.Marshal(req.Body)
		if err != nil {
			utils.BadRequestResponse(ctx, "请求体JSON格式错误: "+err.Error())
			return
		}
		bodyReader = bytes.NewReader(bodyBytes)
	}

	// 创建HTTP请求
	httpReq, err := http.NewRequest(req.Method, req.URL, bodyReader)
	if err != nil {
		utils.BadRequestResponse(ctx, "创建HTTP请求失败: "+err.Error())
		return
	}

	// 设置请求头
	if req.Headers != nil {
		for key, value := range req.Headers {
			httpReq.Header.Set(key, value)
		}
	}

	// 如果没有设置Content-Type且有请求体，设置为application/json
	if bodyReader != nil && httpReq.Header.Get("Content-Type") == "" {
		httpReq.Header.Set("Content-Type", "application/json")
	}

	// 创建HTTP客户端
	client := &http.Client{
		Timeout: time.Duration(req.Timeout) * time.Second,
	}

	// 发送请求
	resp, err := client.Do(httpReq)
	if err != nil {
		responseTime := time.Since(startTime).Milliseconds()
		response := APITestResponse{
			ResponseTime: responseTime,
			Error:        "请求失败: " + err.Error(),
		}
		utils.SuccessResponse(ctx, response)
		return
	}
	defer resp.Body.Close()

	// 计算响应时间
	responseTime := time.Since(startTime).Milliseconds()

	// 读取响应体
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		response := APITestResponse{
			StatusCode:   resp.StatusCode,
			Headers:      resp.Header,
			ResponseTime: responseTime,
			Error:        "读取响应体失败: " + err.Error(),
		}
		utils.SuccessResponse(ctx, response)
		return
	}

	// 尝试解析JSON响应
	var bodyData interface{}
	if len(respBody) > 0 {
		if err := json.Unmarshal(respBody, &bodyData); err != nil {
			// 如果不是JSON，返回原始字符串
			bodyData = string(respBody)
		}
	}

	response := APITestResponse{
		StatusCode:   resp.StatusCode,
		Headers:      resp.Header,
		Body:         bodyData,
		ResponseTime: responseTime,
	}

	utils.SuccessResponse(ctx, response)
}

// GetAPIEndpoints 获取API端点列表
// @Summary 获取API端点列表
// @Description 获取系统所有可用的API端点信息
// @Tags API测试
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]APIEndpoint}
// @Failure 500 {object} model.Response
// @Router /api/v1/api-test/endpoints [get]
func (c *APITestController) GetAPIEndpoints(ctx *gin.Context) {
	endpoints := []APIEndpoint{
		// 用户管理
		{
			Method:      "POST",
			Path:        "/api/v1/login",
			Description: "用户登录",
			Tags:        []string{"用户管理"},
			Parameters: []APIParameter{
				{Name: "username", In: "body", Type: "string", Required: true, Description: "用户名", Example: "admin"},
				{Name: "password", In: "body", Type: "string", Required: true, Description: "密码", Example: "admin123"},
			},
			Responses: map[string]string{
				"200": "登录成功",
				"400": "参数错误",
				"401": "认证失败",
			},
		},
		{
			Method:      "GET",
			Path:        "/api/v1/users",
			Description: "获取用户列表",
			Tags:        []string{"用户管理"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
				{Name: "page", In: "query", Type: "integer", Required: false, Description: "页码", Example: "1"},
				{Name: "page_size", In: "query", Type: "integer", Required: false, Description: "每页数量", Example: "10"},
			},
			Responses: map[string]string{
				"200": "获取成功",
				"401": "未授权",
			},
		},
		// 资产管理
		{
			Method:      "GET",
			Path:        "/api/v1/assets",
			Description: "获取资产列表",
			Tags:        []string{"资产管理"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
				{Name: "page", In: "query", Type: "integer", Required: false, Description: "页码", Example: "1"},
				{Name: "page_size", In: "query", Type: "integer", Required: false, Description: "每页数量", Example: "10"},
			},
			Responses: map[string]string{
				"200": "获取成功",
				"401": "未授权",
			},
		},
		{
			Method:      "POST",
			Path:        "/api/v1/assets",
			Description: "创建资产",
			Tags:        []string{"资产管理"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
				{Name: "name", In: "body", Type: "string", Required: true, Description: "资产名称", Example: "Web服务器"},
				{Name: "type", In: "body", Type: "string", Required: true, Description: "资产类型", Example: "server"},
				{Name: "ip_address", In: "body", Type: "string", Required: false, Description: "IP地址", Example: "192.168.1.100"},
			},
			Responses: map[string]string{
				"200": "创建成功",
				"400": "参数错误",
				"401": "未授权",
			},
		},
		// 漏洞管理
		{
			Method:      "GET",
			Path:        "/api/v1/vulnerabilities",
			Description: "获取漏洞列表",
			Tags:        []string{"漏洞管理"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
				{Name: "page", In: "query", Type: "integer", Required: false, Description: "页码", Example: "1"},
				{Name: "page_size", In: "query", Type: "integer", Required: false, Description: "每页数量", Example: "10"},
			},
			Responses: map[string]string{
				"200": "获取成功",
				"401": "未授权",
			},
		},
		{
			Method:      "POST",
			Path:        "/api/v1/vulnerabilities",
			Description: "创建漏洞",
			Tags:        []string{"漏洞管理"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
				{Name: "title", In: "body", Type: "string", Required: true, Description: "漏洞标题", Example: "SQL注入漏洞"},
				{Name: "description", In: "body", Type: "string", Required: true, Description: "漏洞描述", Example: "发现SQL注入漏洞"},
				{Name: "asset_id", In: "body", Type: "integer", Required: true, Description: "资产ID", Example: "1"},
			},
			Responses: map[string]string{
				"200": "创建成功",
				"400": "参数错误",
				"401": "未授权",
			},
		},
		// 统计分析
		{
			Method:      "GET",
			Path:        "/api/v1/analytics/dashboard",
			Description: "获取仪表盘统计",
			Tags:        []string{"统计分析"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
			},
			Responses: map[string]string{
				"200": "获取成功",
				"401": "未授权",
			},
		},
		// 知识库
		{
			Method:      "GET",
			Path:        "/api/v1/knowledge",
			Description: "获取知识库列表",
			Tags:        []string{"知识库"},
			Parameters: []APIParameter{
				{Name: "Authorization", In: "header", Type: "string", Required: true, Description: "Bearer token", Example: "Bearer your-token"},
				{Name: "page", In: "query", Type: "integer", Required: false, Description: "页码", Example: "1"},
				{Name: "page_size", In: "query", Type: "integer", Required: false, Description: "每页数量", Example: "10"},
			},
			Responses: map[string]string{
				"200": "获取成功",
				"401": "未授权",
			},
		},
	}

	utils.SuccessResponse(ctx, endpoints)
}

// GenerateAPIToken 生成API测试令牌
// @Summary 生成API测试令牌
// @Description 为当前用户生成API测试专用令牌
// @Tags API测试
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=map[string]string}
// @Failure 401 {object} model.Response
// @Router /api/v1/api-test/token [post]
func (c *APITestController) GenerateAPIToken(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	// 生成测试令牌（这里简化处理，实际应该生成专用的测试令牌）
	token := fmt.Sprintf("test_token_%d_%d", userID, time.Now().Unix())

	response := map[string]string{
		"token":      token,
		"expires_in": "3600", // 1小时
		"type":       "Bearer",
		"usage":      "API测试专用令牌",
	}

	utils.SuccessResponse(ctx, response)
}

// GetAPIDocumentation 获取API文档信息
// @Summary 获取API文档信息
// @Description 获取系统API文档的基本信息和访问地址
// @Tags API测试
// @Produce json
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Router /api/v1/api-test/docs [get]
func (c *APITestController) GetAPIDocumentation(ctx *gin.Context) {
	host := ctx.Request.Host
	scheme := "http"
	if ctx.Request.TLS != nil {
		scheme = "https"
	}

	docs := map[string]interface{}{
		"title":       "VulnArk API Documentation",
		"version":     "1.0.0",
		"description": "VulnArk漏洞管理系统API文档",
		"swagger_url": fmt.Sprintf("%s://%s/swagger/index.html", scheme, host),
		"openapi_spec": map[string]string{
			"json": fmt.Sprintf("%s://%s/swagger/doc.json", scheme, host),
			"yaml": fmt.Sprintf("%s://%s/swagger/doc.yaml", scheme, host),
		},
		"endpoints_count": 50, // 大概的端点数量
		"last_updated":    time.Now().Format("2006-01-02 15:04:05"),
		"features": []string{
			"用户认证与授权",
			"资产管理",
			"漏洞管理",
			"报告管理",
			"知识库管理",
			"统计分析",
			"通知系统",
			"系统配置",
		},
	}

	utils.SuccessResponse(ctx, docs)
}

// ValidateAPIRequest 验证API请求格式
// @Summary 验证API请求格式
// @Description 验证API请求的格式是否正确，不实际发送请求
// @Tags API测试
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body APITestRequest true "API测试请求"
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Failure 400 {object} model.Response
// @Router /api/v1/api-test/validate [post]
func (c *APITestController) ValidateAPIRequest(ctx *gin.Context) {
	var req APITestRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	validation := map[string]interface{}{
		"valid":  true,
		"method": req.Method,
		"url":    req.URL,
		"checks": []map[string]interface{}{
			{
				"name":   "HTTP方法",
				"status": "通过",
				"detail": fmt.Sprintf("方法 %s 是有效的HTTP方法", req.Method),
			},
			{
				"name":   "URL格式",
				"status": "通过",
				"detail": "URL格式正确",
			},
		},
	}

	// 验证URL格式
	if !strings.HasPrefix(req.URL, "http://") && !strings.HasPrefix(req.URL, "https://") {
		validation["valid"] = false
		validation["checks"] = append(validation["checks"].([]map[string]interface{}), map[string]interface{}{
			"name":   "URL协议",
			"status": "失败",
			"detail": "URL必须以http://或https://开头",
		})
	}

	// 验证请求头
	if req.Headers != nil {
		headerCheck := map[string]interface{}{
			"name":   "请求头",
			"status": "通过",
			"detail": fmt.Sprintf("包含 %d 个请求头", len(req.Headers)),
		}
		validation["checks"] = append(validation["checks"].([]map[string]interface{}), headerCheck)
	}

	// 验证请求体
	if req.Body != nil {
		if req.Method == "GET" || req.Method == "DELETE" {
			validation["valid"] = false
			validation["checks"] = append(validation["checks"].([]map[string]interface{}), map[string]interface{}{
				"name":   "请求体",
				"status": "警告",
				"detail": fmt.Sprintf("%s 方法通常不应包含请求体", req.Method),
			})
		} else {
			validation["checks"] = append(validation["checks"].([]map[string]interface{}), map[string]interface{}{
				"name":   "请求体",
				"status": "通过",
				"detail": "请求体格式正确",
			})
		}
	}

	// 验证超时设置
	if req.Timeout > 0 && req.Timeout <= 300 {
		validation["checks"] = append(validation["checks"].([]map[string]interface{}), map[string]interface{}{
			"name":   "超时设置",
			"status": "通过",
			"detail": fmt.Sprintf("超时时间设置为 %d 秒", req.Timeout),
		})
	} else if req.Timeout > 300 {
		validation["valid"] = false
		validation["checks"] = append(validation["checks"].([]map[string]interface{}), map[string]interface{}{
			"name":   "超时设置",
			"status": "失败",
			"detail": "超时时间不能超过300秒",
		})
	}

	utils.SuccessResponse(ctx, validation)
}
