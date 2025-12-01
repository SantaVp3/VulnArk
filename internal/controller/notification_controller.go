package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// NotificationController 通知控制器
type NotificationController struct {
	notificationService service.NotificationService
}

// NewNotificationController 创建通知控制器
func NewNotificationController(notificationService service.NotificationService) *NotificationController {
	return &NotificationController{
		notificationService: notificationService,
	}
}

// CreateNotification 创建通知
// @Summary 创建通知
// @Description 创建新的系统通知
// @Tags 通知管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.NotificationCreateRequest true "创建通知请求"
// @Success 200 {object} model.Response{data=model.Notification}
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications [post]
func (c *NotificationController) CreateNotification(ctx *gin.Context) {
	var req model.NotificationCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	senderID, exists := middleware.GetCurrentUserID(ctx)
	var senderPtr *uint
	if exists {
		senderPtr = &senderID
	}

	notification, err := c.notificationService.CreateNotification(&req, senderPtr)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, notification)
}

// GetNotificationList 获取通知列表
// @Summary 获取通知列表
// @Description 获取通知列表（分页）
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param type query string false "通知类型"
// @Param level query string false "通知级别"
// @Param is_read query string false "是否已读"
// @Param recipient_id query string false "接收人ID"
// @Param start_date query string false "开始日期"
// @Param end_date query string false "结束日期"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications [get]
func (c *NotificationController) GetNotificationList(ctx *gin.Context) {
	var req model.NotificationSearchRequest
	if err := utils.BindQueryAndValidate(ctx, &req); err != nil {
		return
	}

	// 设置默认值
	if req.Page <= 0 {
		req.Page = 1
	}
	if req.PageSize <= 0 {
		req.PageSize = 10
	}

	resp, err := c.notificationService.GetNotificationList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetNotification 获取通知详情
// @Summary 获取通知详情
// @Description 根据ID获取通知详情
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "通知ID"
// @Success 200 {object} model.Response{data=model.Notification}
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/{id} [get]
func (c *NotificationController) GetNotification(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的通知ID")
		return
	}

	notification, err := c.notificationService.GetNotificationByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, notification)
}

// GetMyNotifications 获取我的通知
// @Summary 获取我的通知
// @Description 获取当前用户的通知列表
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Param limit query int false "限制数量" default(20)
// @Success 200 {object} model.Response{data=[]model.Notification}
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/my [get]
func (c *NotificationController) GetMyNotifications(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	limitStr := ctx.DefaultQuery("limit", "20")
	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 20
	}

	notifications, err := c.notificationService.GetUserNotifications(userID, limit)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, notifications)
}

// MarkAsRead 标记为已读
// @Summary 标记通知为已读
// @Description 标记指定通知为已读
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "通知ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/{id}/read [post]
func (c *NotificationController) MarkAsRead(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的通知ID")
		return
	}

	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.notificationService.MarkAsRead(uint(id), userID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// MarkAllAsRead 标记所有通知为已读
// @Summary 标记所有通知为已读
// @Description 标记当前用户的所有通知为已读
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/read-all [post]
func (c *NotificationController) MarkAllAsRead(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.notificationService.MarkAllAsRead(userID); err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteNotification 删除通知
// @Summary 删除通知
// @Description 删除指定通知
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "通知ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/{id} [delete]
func (c *NotificationController) DeleteNotification(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的通知ID")
		return
	}

	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.notificationService.DeleteNotification(uint(id), userID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetNotificationStats 获取通知统计
// @Summary 获取通知统计
// @Description 获取当前用户的通知统计信息
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.NotificationStats}
// @Failure 500 {object} model.Response
// @Router /api/v1/notifications/stats [get]
func (c *NotificationController) GetNotificationStats(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	stats, err := c.notificationService.GetNotificationStats(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// SendEmail 发送邮件
// @Summary 发送邮件
// @Description 发送邮件通知
// @Tags 通知管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.EmailSendRequest true "发送邮件请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/email [post]
func (c *NotificationController) SendEmail(ctx *gin.Context) {
	var req model.EmailSendRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	senderID, exists := middleware.GetCurrentUserID(ctx)
	var senderPtr *uint
	if exists {
		senderPtr = &senderID
	}

	if err := c.notificationService.SendEmail(&req, senderPtr); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetNotificationSetting 获取通知设置
// @Summary 获取通知设置
// @Description 获取当前用户的通知设置
// @Tags 通知管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.NotificationSetting}
// @Failure 500 {object} model.Response
// @Router /api/v1/notifications/settings [get]
func (c *NotificationController) GetNotificationSetting(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	setting, err := c.notificationService.GetNotificationSetting(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, setting)
}

// UpdateNotificationSetting 更新通知设置
// @Summary 更新通知设置
// @Description 更新当前用户的通知设置
// @Tags 通知管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.NotificationSettingUpdateRequest true "更新通知设置请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/notifications/settings [put]
func (c *NotificationController) UpdateNotificationSetting(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	var req model.NotificationSettingUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.notificationService.UpdateNotificationSetting(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// CreateEmailTemplate 创建邮件模板
// @Summary 创建邮件模板
// @Description 创建新的邮件模板
// @Tags 邮件模板
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.EmailTemplateCreateRequest true "创建邮件模板请求"
// @Success 200 {object} model.Response{data=model.EmailTemplate}
// @Failure 400 {object} model.Response
// @Router /api/v1/email-templates [post]
func (c *NotificationController) CreateEmailTemplate(ctx *gin.Context) {
	var req model.EmailTemplateCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	creatorID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	template, err := c.notificationService.CreateEmailTemplate(&req, creatorID)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, template)
}

// GetEmailTemplateList 获取邮件模板列表
// @Summary 获取邮件模板列表
// @Description 获取所有邮件模板
// @Tags 邮件模板
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]model.EmailTemplate}
// @Failure 500 {object} model.Response
// @Router /api/v1/email-templates [get]
func (c *NotificationController) GetEmailTemplateList(ctx *gin.Context) {
	templates, err := c.notificationService.GetEmailTemplateList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, templates)
}

// GetEmailTemplate 获取邮件模板详情
// @Summary 获取邮件模板详情
// @Description 根据ID获取邮件模板详情
// @Tags 邮件模板
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "模板ID"
// @Success 200 {object} model.Response{data=model.EmailTemplate}
// @Failure 400 {object} model.Response
// @Router /api/v1/email-templates/{id} [get]
func (c *NotificationController) GetEmailTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	template, err := c.notificationService.GetEmailTemplateByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, template)
}

// UpdateEmailTemplate 更新邮件模板
// @Summary 更新邮件模板
// @Description 更新邮件模板信息
// @Tags 邮件模板
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "模板ID"
// @Param request body model.EmailTemplateUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/email-templates/{id} [put]
func (c *NotificationController) UpdateEmailTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	var req model.EmailTemplateUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.notificationService.UpdateEmailTemplate(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteEmailTemplate 删除邮件模板
// @Summary 删除邮件模板
// @Description 删除邮件模板
// @Tags 邮件模板
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "模板ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/email-templates/{id} [delete]
func (c *NotificationController) DeleteEmailTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	if err := c.notificationService.DeleteEmailTemplate(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}
