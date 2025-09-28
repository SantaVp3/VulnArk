package controller

import (
	"net/http"
	"os"
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// SystemController 系统控制器
type SystemController struct {
	systemConfigService service.SystemConfigService
	auditLogService     service.AuditLogService
}

// NewSystemController 创建系统控制器
func NewSystemController(systemConfigService service.SystemConfigService, auditLogService service.AuditLogService) *SystemController {
	return &SystemController{
		systemConfigService: systemConfigService,
		auditLogService:     auditLogService,
	}
}

// GetPublicConfigs 获取公开配置
// @Summary 获取公开系统配置
// @Description 获取可以公开访问的系统配置信息
// @Tags 系统管理
// @Produce json
// @Success 200 {object} model.Response{data=[]model.SystemConfig}
// @Router /api/v1/public/configs [get]
func (c *SystemController) GetPublicConfigs(ctx *gin.Context) {
	configs, err := c.systemConfigService.GetPublicConfigs()
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取公开配置失败")
		return
	}

	utils.SuccessResponse(ctx, configs)
}

// GetSystemInfo 获取系统信息
// @Summary 获取系统基本信息
// @Description 获取系统版本、状态等基本信息
// @Tags 系统管理
// @Produce json
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Router /api/v1/public/system/info [get]
func (c *SystemController) GetSystemInfo(ctx *gin.Context) {
	// 检查是否为首次部署
	isFirstDeploy := c.isFirstTimeDeployment()
	
	systemInfo := map[string]interface{}{
		"name":           "VulnArk漏洞管理系统",
		"version":        "1.0.0",
		"status":         "running",
		"first_deploy":   isFirstDeploy,
		"has_admin_file": c.hasAdminCredentialsFile(),
	}

	utils.SuccessResponse(ctx, systemInfo)
}

// GetClientConfig 获取客户端配置
// @Summary 获取客户端配置信息
// @Description 获取前端需要的配置信息
// @Tags 系统管理
// @Produce json
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Router /api/v1/public/client-config [get]
func (c *SystemController) GetClientConfig(ctx *gin.Context) {
	config := map[string]interface{}{
		"system_name":    "VulnArk漏洞管理系统",
		"version":        "1.0.0",
		"first_deploy":   c.isFirstTimeDeployment(),
		"has_admin_file": c.hasAdminCredentialsFile(),
		"login_tips": map[string]interface{}{
			"show_first_deploy_tip": c.isFirstTimeDeployment(),
			"admin_username":        "admin",
			"credential_file":       "admin_credentials.txt",
		},
	}

	utils.SuccessResponse(ctx, config)
}

// GetSystemConfigs 获取系统配置列表
// @Summary 获取系统配置列表
// @Description 获取所有系统配置
// @Tags 系统管理
// @Security ApiKeyAuth
// @Produce json
// @Success 200 {object} model.Response{data=[]model.SystemConfig}
// @Failure 401 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/configs [get]
func (c *SystemController) GetSystemConfigs(ctx *gin.Context) {
	configs, err := c.systemConfigService.GetConfigList()
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取系统配置失败")
		return
	}

	utils.SuccessResponse(ctx, configs)
}

// GetConfigsByCategory 根据分类获取配置
// @Summary 根据分类获取系统配置
// @Description 根据配置分类获取相关配置项
// @Tags 系统管理
// @Security ApiKeyAuth
// @Param category path string true "配置分类"
// @Produce json
// @Success 200 {object} model.Response{data=[]model.SystemConfig}
// @Failure 401 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/configs/category/{category} [get]
func (c *SystemController) GetConfigsByCategory(ctx *gin.Context) {
	category := ctx.Param("category")
	if category == "" {
		utils.BadRequestResponse(ctx, "配置分类不能为空")
		return
	}

	configs, err := c.systemConfigService.GetConfigsByCategory(category)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取配置失败")
		return
	}

	utils.SuccessResponse(ctx, configs)
}

// GetSystemConfig 获取单个系统配置
// @Summary 获取单个系统配置
// @Description 根据配置键获取配置值
// @Tags 系统管理
// @Security ApiKeyAuth
// @Param key path string true "配置键"
// @Produce json
// @Success 200 {object} model.Response{data=model.SystemConfig}
// @Failure 401 {object} model.Response
// @Failure 404 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/configs/{key} [get]
func (c *SystemController) GetSystemConfig(ctx *gin.Context) {
	key := ctx.Param("key")
	if key == "" {
		utils.BadRequestResponse(ctx, "配置键不能为空")
		return
	}

	config, err := c.systemConfigService.GetConfigByKey(key)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusNotFound, "配置不存在")
		return
	}

	utils.SuccessResponse(ctx, config)
}

// UpdateSystemConfig 更新系统配置
// @Summary 更新系统配置
// @Description 更新指定的系统配置
// @Tags 系统管理
// @Security ApiKeyAuth
// @Param key path string true "配置键"
// @Param request body model.SystemConfigUpdateRequest true "更新请求"
// @Produce json
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Failure 404 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/configs/{key} [put]
func (c *SystemController) UpdateSystemConfig(ctx *gin.Context) {
	key := ctx.Param("key")
	if key == "" {
		utils.BadRequestResponse(ctx, "配置键不能为空")
		return
	}

	var req model.SystemConfigUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.systemConfigService.UpdateConfig(key, &req); err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "更新配置失败")
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetAuditLogs 获取审计日志列表
// @Summary 获取审计日志列表
// @Description 获取系统审计日志
// @Tags 系统管理
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(20)
// @Param action query string false "操作类型"
// @Param user_id query int false "用户ID"
// @Param start_time query string false "开始时间"
// @Param end_time query string false "结束时间"
// @Produce json
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 401 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/audit-logs [get]
func (c *SystemController) GetAuditLogs(ctx *gin.Context) {
	var req model.AuditLogSearchRequest
	if err := ctx.ShouldBindQuery(&req); err != nil {
		utils.BadRequestResponse(ctx, "参数错误: "+err.Error())
		return
	}

	// 设置默认分页参数
	if req.Page <= 0 {
		req.Page = 1
	}
	if req.PageSize <= 0 {
		req.PageSize = 20
	}

	result, err := c.auditLogService.GetAuditLogList(&req)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取审计日志失败")
		return
	}

	utils.SuccessResponse(ctx, result)
}

// GetAuditLogStats 获取审计日志统计
// @Summary 获取审计日志统计信息
// @Description 获取审计日志的统计数据
// @Tags 系统管理
// @Security ApiKeyAuth
// @Produce json
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Failure 401 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/audit-logs/stats [get]
func (c *SystemController) GetAuditLogStats(ctx *gin.Context) {
	stats, err := c.auditLogService.GetAuditLogStats()
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取统计信息失败")
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// GetAuditLog 获取单个审计日志
// @Summary 获取单个审计日志详情
// @Description 根据ID获取审计日志详情
// @Tags 系统管理
// @Security ApiKeyAuth
// @Param id path int true "日志ID"
// @Produce json
// @Success 200 {object} model.Response{data=model.AuditLog}
// @Failure 401 {object} model.Response
// @Failure 404 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/audit-logs/{id} [get]
func (c *SystemController) GetAuditLog(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的日志ID")
		return
	}

	log, err := c.auditLogService.GetAuditLogByID(uint(id))
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusNotFound, "日志不存在")
		return
	}

	utils.SuccessResponse(ctx, log)
}

// CleanupAuditLogs 清理审计日志
// @Summary 清理过期的审计日志
// @Description 清理指定天数之前的审计日志
// @Tags 系统管理
// @Security ApiKeyAuth
// @Param request body map[string]int true "清理请求" example({"days": 90})
// @Produce json
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Failure 500 {object} model.Response
// @Router /api/v1/system/audit-logs/cleanup [post]
func (c *SystemController) CleanupAuditLogs(ctx *gin.Context) {
	var req struct {
		Days int `json:"days" binding:"required,min=1,max=3650"`
	}

	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.auditLogService.CleanupOldLogs(req.Days); err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "清理日志失败")
		return
	}

	utils.SuccessResponse(ctx, map[string]interface{}{
		"message": "审计日志清理完成",
		"days":    req.Days,
	})
}

// isFirstTimeDeployment 检查是否为首次部署
func (c *SystemController) isFirstTimeDeployment() bool {
	// 检查是否存在管理员凭证文件
	if _, err := os.Stat("admin_credentials.txt"); err == nil {
		return true
	}
	return false
}

// hasAdminCredentialsFile 检查是否存在管理员凭证文件
func (c *SystemController) hasAdminCredentialsFile() bool {
	if _, err := os.Stat("admin_credentials.txt"); err == nil {
		return true
	}
	return false
}