package controller

import (
	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// SettingsController 设置控制器
type SettingsController struct {
	settingsService service.SettingsService
}

// NewSettingsController 创建设置控制器
func NewSettingsController(settingsService service.SettingsService) *SettingsController {
	return &SettingsController{
		settingsService: settingsService,
	}
}

// UpdateProfile 更新用户资料
// @Summary 更新用户资料
// @Description 更新当前用户的个人资料信息
// @Tags 设置管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.UserProfileUpdateRequest true "用户资料更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/profile [put]
func (c *SettingsController) UpdateProfile(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}
	
	var req model.UserProfileUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.settingsService.UpdateProfile(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ChangePassword 修改密码
// @Summary 修改密码
// @Description 修改当前用户的登录密码
// @Tags 设置管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.ChangePasswordRequest true "修改密码请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/password [put]
func (c *SettingsController) ChangePassword(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}
	
	var req model.ChangePasswordRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.settingsService.ChangePassword(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// Setup2FA 设置双因素认证
// @Summary 设置双因素认证
// @Description 为当前用户设置双因素认证
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.TwoFactorSetupResponse}
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/2fa/setup [post]
func (c *SettingsController) Setup2FA(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	setupData, err := c.settingsService.Setup2FA(userID)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, setupData)
}

// Verify2FA 验证双因素认证
// @Summary 验证双因素认证
// @Description 验证双因素认证码并启用2FA
// @Tags 设置管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.TwoFactorVerifyRequest true "2FA验证请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/2fa/verify [post]
func (c *SettingsController) Verify2FA(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	var req model.TwoFactorVerifyRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.settingsService.Verify2FA(userID, req.Code); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// Disable2FA 禁用双因素认证
// @Summary 禁用双因素认证
// @Description 禁用当前用户的双因素认证
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/2fa [delete]
func (c *SettingsController) Disable2FA(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	if err := c.settingsService.Disable2FA(userID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// Get2FAStatus 获取双因素认证状态
// @Summary 获取双因素认证状态
// @Description 获取当前用户的双因素认证状态
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.TwoFactorStatusResponse}
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/2fa/status [get]
func (c *SettingsController) Get2FAStatus(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	status, err := c.settingsService.Get2FAStatus(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, status)
}

// GetSystemSettings 获取系统设置
// @Summary 获取系统设置
// @Description 获取当前用户的系统设置
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.SystemSettings}
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/system [get]
func (c *SettingsController) GetSystemSettings(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	settings, err := c.settingsService.GetSystemSettings(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, settings)
}

// UpdateSystemSettings 更新系统设置
// @Summary 更新系统设置
// @Description 更新当前用户的系统设置
// @Tags 设置管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.SystemSettingsRequest true "系统设置更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/system [put]
func (c *SettingsController) UpdateSystemSettings(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}
	
	var req model.SystemSettingsRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.settingsService.UpdateSystemSettings(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetNotificationSettings 获取通知设置
// @Summary 获取通知设置
// @Description 获取当前用户的通知设置
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.NotificationSettings}
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/notifications [get]
func (c *SettingsController) GetNotificationSettings(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	settings, err := c.settingsService.GetNotificationSettings(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, settings)
}

// UpdateNotificationSettings 更新通知设置
// @Summary 更新通知设置
// @Description 更新当前用户的通知设置
// @Tags 设置管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.NotificationSettingsRequest true "通知设置更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/notifications [put]
func (c *SettingsController) UpdateNotificationSettings(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}
	
	var req model.NotificationSettingsRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.settingsService.UpdateNotificationSettings(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ExportData 导出数据
// @Summary 导出数据
// @Description 导出用户数据
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Param type query string true "数据类型" Enums(profile,settings,all)
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/export [get]
func (c *SettingsController) ExportData(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}
	dataType := ctx.Query("type")
	
	if dataType == "" {
		utils.BadRequestResponse(ctx, "请指定数据类型")
		return
	}

	data, err := c.settingsService.ExportUserData(userID, dataType)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	// 设置下载头
	ctx.Header("Content-Type", "application/json")
	ctx.Header("Content-Disposition", "attachment; filename=export_"+dataType+".json")
	ctx.Data(200, "application/json", data)
}

// ImportData 导入数据
// @Summary 导入数据
// @Description 导入用户数据
// @Tags 设置管理
// @Accept multipart/form-data
// @Produce json
// @Security ApiKeyAuth
// @Param file formData file true "数据文件"
// @Param type formData string true "数据类型"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/import [post]
func (c *SettingsController) ImportData(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}
	dataType := ctx.PostForm("type")
	
	if dataType == "" {
		utils.BadRequestResponse(ctx, "请指定数据类型")
		return
	}

	file, err := ctx.FormFile("file")
	if err != nil {
		utils.BadRequestResponse(ctx, "请选择文件")
		return
	}

	// 读取文件内容
	src, err := file.Open()
	if err != nil {
		utils.BadRequestResponse(ctx, "文件读取失败")
		return
	}
	defer src.Close()

	data := make([]byte, file.Size)
	if _, err := src.Read(data); err != nil {
		utils.BadRequestResponse(ctx, "文件读取失败")
		return
	}

	if err := c.settingsService.ImportUserData(userID, data, dataType); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ClearCache 清理缓存
// @Summary 清理缓存
// @Description 清理用户相关缓存
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/cache/clear [post]
func (c *SettingsController) ClearCache(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	if err := c.settingsService.ClearCache(userID); err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// OptimizeDatabase 优化数据库
// @Summary 优化数据库
// @Description 执行数据库优化操作
// @Tags 设置管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response
// @Failure 401 {object} model.Response
// @Router /api/v1/settings/database/optimize [post]
func (c *SettingsController) OptimizeDatabase(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "用户未登录")
		return
	}

	if err := c.settingsService.OptimizeDatabase(userID); err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}
