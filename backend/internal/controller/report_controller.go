package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// ReportController 报告控制器
type ReportController struct {
	reportService service.ReportService
}

// NewReportController 创建报告控制器
func NewReportController(reportService service.ReportService) *ReportController {
	return &ReportController{
		reportService: reportService,
	}
}

// UploadReport 上传报告
// @Summary 上传渗透测试报告
// @Description 上传渗透测试报告文件（支持PDF、DOC、DOCX格式）
// @Tags 报告管理
// @Accept multipart/form-data
// @Produce json
// @Security ApiKeyAuth
// @Param file formData file true "报告文件（PDF、DOC、DOCX格式）"
// @Param title formData string true "报告标题"
// @Param description formData string false "报告描述"
// @Param type formData string true "报告类型" Enums(penetration_test,vulnerability_assessment,security_audit,compliance_check,other)
// @Param severity formData string true "严重程度" Enums(critical,high,medium,low,info)
// @Param asset_ids formData string false "关联资产ID列表，逗号分隔"
// @Param tags formData string false "标签列表，逗号分隔"
// @Param test_date formData string false "测试日期，格式：2006-01-02"
// @Success 200 {object} model.Response{data=model.Report}
// @Failure 400 {object} model.Response
// @Router /api/v1/reports [post]
func (c *ReportController) UploadReport(ctx *gin.Context) {
	// 获取上传的文件
	file, err := ctx.FormFile("file")
	if err != nil {
		utils.BadRequestResponse(ctx, "请选择要上传的文件")
		return
	}

	// 解析表单数据
	req := &model.ReportUploadRequest{
		Title:       ctx.PostForm("title"),
		Description: ctx.PostForm("description"),
		Type:        ctx.PostForm("type"),
		Severity:    ctx.PostForm("severity"),
	}

	// 验证必填字段
	if req.Title == "" || req.Type == "" || req.Severity == "" {
		utils.BadRequestResponse(ctx, "标题、类型和严重程度为必填项")
		return
	}

	// 解析资产ID列表
	if assetIDsStr := ctx.PostForm("asset_ids"); assetIDsStr != "" {
		assetIDs, err := utils.ParseUintSlice(assetIDsStr, ",")
		if err != nil {
			utils.BadRequestResponse(ctx, "资产ID格式错误")
			return
		}
		req.AssetIDs = assetIDs
	}

	// 解析标签列表
	if tagsStr := ctx.PostForm("tags"); tagsStr != "" {
		req.Tags = utils.ParseStringSlice(tagsStr, ",")
	}

	// 解析测试日期
	if testDateStr := ctx.PostForm("test_date"); testDateStr != "" {
		testDate, err := utils.ParseDate(testDateStr)
		if err != nil {
			utils.BadRequestResponse(ctx, "测试日期格式错误")
			return
		}
		req.TestDate = &testDate
	}

	uploaderID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	report, err := c.reportService.UploadReport(req, file, uploaderID)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, report)
}

// GetReportList 获取报告列表
// @Summary 获取报告列表
// @Description 获取报告列表（分页）
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param type query string false "报告类型"
// @Param severity query string false "严重程度"
// @Param status query string false "状态"
// @Param uploader_id query string false "上传者ID"
// @Param asset_id query string false "资产ID"
// @Param tag query string false "标签"
// @Param start_date query string false "开始日期"
// @Param end_date query string false "结束日期"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/reports [get]
func (c *ReportController) GetReportList(ctx *gin.Context) {
	var req model.ReportSearchRequest
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

	resp, err := c.reportService.GetReportList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetReport 获取报告详情
// @Summary 获取报告详情
// @Description 根据ID获取报告详情
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Success 200 {object} model.Response{data=model.Report}
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id} [get]
func (c *ReportController) GetReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	report, err := c.reportService.GetReportByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, report)
}

// GetReportContent 获取报告内容
// @Summary 获取报告内容
// @Description 根据ID获取报告的具体内容（用于预览）
// @Tags 报告管理
// @Produce text/html,text/plain
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Success 200 {string} string "报告内容"
// @Failure 400 {object} model.Response
// @Failure 404 {object} model.Response
// @Router /api/v1/reports/{id}/content [get]
func (c *ReportController) GetReportContent(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	content, contentType, err := c.reportService.GetReportContent(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	// Set appropriate content type
	ctx.Header("Content-Type", contentType)
	ctx.String(200, content)
}

// UpdateReport 更新报告
// @Summary 更新报告
// @Description 更新报告信息
// @Tags 报告管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Param request body model.ReportUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id} [put]
func (c *ReportController) UpdateReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	var req model.ReportUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.reportService.UpdateReport(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteReport 删除报告
// @Summary 删除报告
// @Description 删除报告
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id} [delete]
func (c *ReportController) DeleteReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	if err := c.reportService.DeleteReport(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetReportStats 获取报告统计
// @Summary 获取报告统计
// @Description 获取报告统计信息
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.ReportStats}
// @Failure 500 {object} model.Response
// @Router /api/v1/reports/stats [get]
func (c *ReportController) GetReportStats(ctx *gin.Context) {
	stats, err := c.reportService.GetReportStats()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// SubmitReport 提交报告
// @Summary 提交报告
// @Description 提交报告进行审核
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id}/submit [post]
func (c *ReportController) SubmitReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.reportService.SubmitReport(uint(id), userID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ReviewReport 审核报告
// @Summary 审核报告
// @Description 审核报告（通过或拒绝）
// @Tags 报告管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Param request body model.ReportReviewRequest true "审核请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id}/review [post]
func (c *ReportController) ReviewReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	var req model.ReportReviewRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	reviewerID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.reportService.ReviewReport(uint(id), &req, reviewerID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ArchiveReport 归档报告
// @Summary 归档报告
// @Description 归档报告
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id}/archive [post]
func (c *ReportController) ArchiveReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	if err := c.reportService.ArchiveReport(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DownloadReport 下载报告
// @Summary 下载报告
// @Description 获取报告下载链接
// @Tags 报告管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "报告ID"
// @Success 200 {object} model.Response{data=map[string]string}
// @Failure 400 {object} model.Response
// @Router /api/v1/reports/{id}/download [get]
func (c *ReportController) DownloadReport(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的报告ID")
		return
	}

	downloadURL, err := c.reportService.GetDownloadURL(uint(id))
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, map[string]string{
		"download_url": downloadURL,
	})
}
