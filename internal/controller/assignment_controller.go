package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// AssignmentController 分配控制器
type AssignmentController struct {
	assignmentService service.AssignmentService
}

// NewAssignmentController 创建分配控制器
func NewAssignmentController(assignmentService service.AssignmentService) *AssignmentController {
	return &AssignmentController{
		assignmentService: assignmentService,
	}
}

// CreateAssignmentRule 创建分配规则
// @Summary 创建分配规则
// @Description 创建新的漏洞分配规则
// @Tags 分配管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.AssignmentRuleCreateRequest true "创建分配规则请求"
// @Success 200 {object} model.Response{data=model.AssignmentRule}
// @Failure 400 {object} model.Response
// @Router /api/v1/assignment-rules [post]
func (c *AssignmentController) CreateAssignmentRule(ctx *gin.Context) {
	var req model.AssignmentRuleCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	rule, err := c.assignmentService.CreateAssignmentRule(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, rule)
}

// GetAssignmentRuleList 获取分配规则列表
// @Summary 获取分配规则列表
// @Description 获取分配规则列表（分页）
// @Tags 分配管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param rule_type query string false "规则类型"
// @Param assignee_id query string false "分配人ID"
// @Param is_active query string false "是否激活"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/assignment-rules [get]
func (c *AssignmentController) GetAssignmentRuleList(ctx *gin.Context) {
	var req model.AssignmentRuleSearchRequest
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

	resp, err := c.assignmentService.GetAssignmentRuleList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetAssignmentRule 获取分配规则详情
// @Summary 获取分配规则详情
// @Description 根据ID获取分配规则详情
// @Tags 分配管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "规则ID"
// @Success 200 {object} model.Response{data=model.AssignmentRule}
// @Failure 400 {object} model.Response
// @Router /api/v1/assignment-rules/{id} [get]
func (c *AssignmentController) GetAssignmentRule(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的规则ID")
		return
	}

	rule, err := c.assignmentService.GetAssignmentRuleByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, rule)
}

// UpdateAssignmentRule 更新分配规则
// @Summary 更新分配规则
// @Description 更新分配规则信息
// @Tags 分配管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "规则ID"
// @Param request body model.AssignmentRuleUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/assignment-rules/{id} [put]
func (c *AssignmentController) UpdateAssignmentRule(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的规则ID")
		return
	}

	var req model.AssignmentRuleUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.assignmentService.UpdateAssignmentRule(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteAssignmentRule 删除分配规则
// @Summary 删除分配规则
// @Description 删除分配规则
// @Tags 分配管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "规则ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/assignment-rules/{id} [delete]
func (c *AssignmentController) DeleteAssignmentRule(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的规则ID")
		return
	}

	if err := c.assignmentService.DeleteAssignmentRule(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// AssignVulnerability 分配漏洞
// @Summary 分配漏洞
// @Description 手动分配漏洞给指定用户
// @Tags 分配管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "漏洞ID"
// @Param request body model.VulnerabilityAssignRequest true "分配请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/vulnerabilities/{id}/assign [post]
func (c *AssignmentController) AssignVulnerability(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的漏洞ID")
		return
	}

	var req model.VulnerabilityAssignRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	assignerID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.assignmentService.AssignVulnerability(uint(id), &req, assignerID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// BatchAssignVulnerabilities 批量分配漏洞
// @Summary 批量分配漏洞
// @Description 批量分配多个漏洞给指定用户
// @Tags 分配管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.BatchAssignRequest true "批量分配请求"
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Failure 400 {object} model.Response
// @Router /api/v1/vulnerabilities/batch-assign [post]
func (c *AssignmentController) BatchAssignVulnerabilities(ctx *gin.Context) {
	var req model.BatchAssignRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	assignerID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	errors, err := c.assignmentService.BatchAssignVulnerabilities(&req, assignerID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	result := map[string]interface{}{
		"total_count":    len(req.VulnerabilityIDs),
		"success_count":  len(req.VulnerabilityIDs) - len(errors),
		"error_count":    len(errors),
		"errors":         errors,
	}

	if len(errors) > 0 {
		result["message"] = "部分漏洞分配失败"
	} else {
		result["message"] = "所有漏洞分配成功"
	}

	utils.SuccessResponse(ctx, result)
}

// AutoAssignVulnerabilities 自动分配漏洞
// @Summary 自动分配漏洞
// @Description 根据分配规则自动分配漏洞
// @Tags 分配管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.AutoAssignRequest true "自动分配请求"
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Failure 400 {object} model.Response
// @Router /api/v1/vulnerabilities/auto-assign [post]
func (c *AssignmentController) AutoAssignVulnerabilities(ctx *gin.Context) {
	var req model.AutoAssignRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	errors, err := c.assignmentService.AutoAssignVulnerabilities(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	totalCount := len(req.VulnerabilityIDs)
	if totalCount == 0 {
		totalCount = 1 // 如果没有指定漏洞，假设处理了1个
	}

	result := map[string]interface{}{
		"total_count":   totalCount,
		"success_count": totalCount - len(errors),
		"error_count":   len(errors),
		"errors":        errors,
	}

	if len(errors) > 0 {
		result["message"] = "部分漏洞自动分配失败"
	} else {
		result["message"] = "所有漏洞自动分配成功"
	}

	utils.SuccessResponse(ctx, result)
}

// GetAssignmentStats 获取分配统计
// @Summary 获取分配统计
// @Description 获取漏洞分配统计信息
// @Tags 分配管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.AssignmentStats}
// @Failure 500 {object} model.Response
// @Router /api/v1/assignments/stats [get]
func (c *AssignmentController) GetAssignmentStats(ctx *gin.Context) {
	stats, err := c.assignmentService.GetAssignmentStats()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// GetVulnerabilityTimeline 获取漏洞时间线
// @Summary 获取漏洞时间线
// @Description 获取指定漏洞的完整时间线
// @Tags 分配管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "漏洞ID"
// @Success 200 {object} model.Response{data=[]model.TimelineEntry}
// @Failure 400 {object} model.Response
// @Router /api/v1/vulnerabilities/{id}/timeline [get]
func (c *AssignmentController) GetVulnerabilityTimeline(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的漏洞ID")
		return
	}

	timeline, err := c.assignmentService.GetVulnerabilityTimeline(uint(id))
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, timeline)
}

// GetRuleTypes 获取规则类型列表
// @Summary 获取规则类型列表
// @Description 获取所有可用的分配规则类型
// @Tags 分配管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]map[string]string}
// @Router /api/v1/assignment-rules/types [get]
func (c *AssignmentController) GetRuleTypes(ctx *gin.Context) {
	types := []map[string]string{
		{"value": model.RuleTypeAssetType, "label": "资产类型"},
		{"value": model.RuleTypeSeverity, "label": "严重程度"},
		{"value": model.RuleTypeDepartment, "label": "所属部门"},
		{"value": model.RuleTypeOWASPCategory, "label": "OWASP分类"},
	}

	utils.SuccessResponse(ctx, types)
}
