package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// KnowledgeController 知识库控制器
type KnowledgeController struct {
	knowledgeService service.KnowledgeService
	tagService       service.TagService
	templateService  service.TemplateService
}

// NewKnowledgeController 创建知识库控制器
func NewKnowledgeController(knowledgeService service.KnowledgeService, tagService service.TagService, templateService service.TemplateService) *KnowledgeController {
	return &KnowledgeController{
		knowledgeService: knowledgeService,
		tagService:       tagService,
		templateService:  templateService,
	}
}

// CreateKnowledge 创建知识库
// @Summary 创建知识库
// @Description 创建新的知识库条目
// @Tags 知识库管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.KnowledgeCreateRequest true "创建知识库请求"
// @Success 200 {object} model.Response{data=model.KnowledgeBase}
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge [post]
func (c *KnowledgeController) CreateKnowledge(ctx *gin.Context) {
	var req model.KnowledgeCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	authorID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	knowledge, err := c.knowledgeService.CreateKnowledge(&req, authorID)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, knowledge)
}

// GetKnowledgeList 获取知识库列表
// @Summary 获取知识库列表
// @Description 获取知识库列表（分页）
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param category query string false "分类"
// @Param type query string false "类型"
// @Param severity query string false "严重程度"
// @Param status query string false "状态"
// @Param author_id query string false "作者ID"
// @Param tags query string false "标签"
// @Param sort_by query string false "排序字段"
// @Param sort_desc query bool false "是否降序"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge [get]
func (c *KnowledgeController) GetKnowledgeList(ctx *gin.Context) {
	var req model.KnowledgeSearchRequest
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

	resp, err := c.knowledgeService.GetKnowledgeList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetKnowledge 获取知识库详情
// @Summary 获取知识库详情
// @Description 根据ID获取知识库详情
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "知识库ID"
// @Success 200 {object} model.Response{data=model.KnowledgeBase}
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id} [get]
func (c *KnowledgeController) GetKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	knowledge, err := c.knowledgeService.GetKnowledgeByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	// 增加浏览次数
	c.knowledgeService.ViewKnowledge(uint(id))

	utils.SuccessResponse(ctx, knowledge)
}

// UpdateKnowledge 更新知识库
// @Summary 更新知识库
// @Description 更新知识库信息
// @Tags 知识库管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "知识库ID"
// @Param request body model.KnowledgeUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id} [put]
func (c *KnowledgeController) UpdateKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	var req model.KnowledgeUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.knowledgeService.UpdateKnowledge(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteKnowledge 删除知识库
// @Summary 删除知识库
// @Description 删除知识库
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "知识库ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id} [delete]
func (c *KnowledgeController) DeleteKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	if err := c.knowledgeService.DeleteKnowledge(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetKnowledgeStats 获取知识库统计
// @Summary 获取知识库统计
// @Description 获取知识库统计信息
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.KnowledgeStats}
// @Failure 500 {object} model.Response
// @Router /api/v1/knowledge/stats [get]
func (c *KnowledgeController) GetKnowledgeStats(ctx *gin.Context) {
	stats, err := c.knowledgeService.GetKnowledgeStats()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// LikeKnowledge 点赞知识库
// @Summary 点赞知识库
// @Description 为知识库点赞
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "知识库ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id}/like [post]
func (c *KnowledgeController) LikeKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	if err := c.knowledgeService.LikeKnowledge(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// PublishKnowledge 发布知识库
// @Summary 发布知识库
// @Description 发布知识库
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "知识库ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id}/publish [post]
func (c *KnowledgeController) PublishKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	reviewerID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	if err := c.knowledgeService.PublishKnowledge(uint(id), reviewerID); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ArchiveKnowledge 归档知识库
// @Summary 归档知识库
// @Description 归档知识库
// @Tags 知识库管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "知识库ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id}/archive [post]
func (c *KnowledgeController) ArchiveKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	if err := c.knowledgeService.ArchiveKnowledge(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// SearchKnowledge 搜索知识库
// @Summary 搜索知识库
// @Description 搜索知识库内容
// @Tags 知识库管理
// @Produce json
// @Param q query string true "搜索关键词"
// @Param limit query int false "限制数量" default(10)
// @Success 200 {object} model.Response{data=[]model.KnowledgeBase}
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/search [get]
func (c *KnowledgeController) SearchKnowledge(ctx *gin.Context) {
	keyword := ctx.Query("q")
	if keyword == "" {
		utils.BadRequestResponse(ctx, "搜索关键词不能为空")
		return
	}

	limitStr := ctx.DefaultQuery("limit", "10")
	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 10
	}

	knowledges, err := c.knowledgeService.SearchKnowledge(keyword, limit)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, knowledges)
}

// GetPopularKnowledge 获取热门知识库
// @Summary 获取热门知识库
// @Description 获取热门知识库列表
// @Tags 知识库管理
// @Produce json
// @Param limit query int false "限制数量" default(10)
// @Success 200 {object} model.Response{data=[]model.KnowledgeBase}
// @Failure 500 {object} model.Response
// @Router /api/v1/knowledge/popular [get]
func (c *KnowledgeController) GetPopularKnowledge(ctx *gin.Context) {
	limitStr := ctx.DefaultQuery("limit", "10")
	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 10
	}

	knowledges, err := c.knowledgeService.GetPopularKnowledge(limit)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, knowledges)
}

// GetRecentKnowledge 获取最新知识库
// @Summary 获取最新知识库
// @Description 获取最新知识库列表
// @Tags 知识库管理
// @Produce json
// @Param limit query int false "限制数量" default(10)
// @Success 200 {object} model.Response{data=[]model.KnowledgeBase}
// @Failure 500 {object} model.Response
// @Router /api/v1/knowledge/recent [get]
func (c *KnowledgeController) GetRecentKnowledge(ctx *gin.Context) {
	limitStr := ctx.DefaultQuery("limit", "10")
	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 10
	}

	knowledges, err := c.knowledgeService.GetRecentKnowledge(limit)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, knowledges)
}

// GetRelatedKnowledge 获取相关知识库
// @Summary 获取相关知识库
// @Description 获取与指定知识库相关的其他知识库
// @Tags 知识库管理
// @Produce json
// @Param id path int true "知识库ID"
// @Param limit query int false "限制数量" default(5)
// @Success 200 {object} model.Response{data=[]model.KnowledgeBase}
// @Failure 400 {object} model.Response
// @Router /api/v1/knowledge/{id}/related [get]
func (c *KnowledgeController) GetRelatedKnowledge(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的知识库ID")
		return
	}

	limitStr := ctx.DefaultQuery("limit", "5")
	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 5
	}

	knowledges, err := c.knowledgeService.GetRelatedKnowledge(uint(id), limit)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, knowledges)
}

// CreateTag 创建标签
// @Summary 创建标签
// @Description 创建新的标签
// @Tags 标签管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.TagCreateRequest true "创建标签请求"
// @Success 200 {object} model.Response{data=model.Tag}
// @Failure 400 {object} model.Response
// @Router /api/v1/tags [post]
func (c *KnowledgeController) CreateTag(ctx *gin.Context) {
	var req model.TagCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	creatorID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	tag, err := c.tagService.CreateTag(&req, creatorID)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, tag)
}

// GetTagList 获取标签列表
// @Summary 获取标签列表
// @Description 获取所有标签列表
// @Tags 标签管理
// @Produce json
// @Success 200 {object} model.Response{data=[]model.Tag}
// @Failure 500 {object} model.Response
// @Router /api/v1/tags [get]
func (c *KnowledgeController) GetTagList(ctx *gin.Context) {
	tags, err := c.tagService.GetTagList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, tags)
}

// GetTag 获取标签详情
// @Summary 获取标签详情
// @Description 根据ID获取标签详情
// @Tags 标签管理
// @Produce json
// @Param id path int true "标签ID"
// @Success 200 {object} model.Response{data=model.Tag}
// @Failure 400 {object} model.Response
// @Router /api/v1/tags/{id} [get]
func (c *KnowledgeController) GetTag(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的标签ID")
		return
	}

	tag, err := c.tagService.GetTagByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, tag)
}

// UpdateTag 更新标签
// @Summary 更新标签
// @Description 更新标签信息
// @Tags 标签管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "标签ID"
// @Param request body model.TagUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/tags/{id} [put]
func (c *KnowledgeController) UpdateTag(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的标签ID")
		return
	}

	var req model.TagUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.tagService.UpdateTag(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteTag 删除标签
// @Summary 删除标签
// @Description 删除标签
// @Tags 标签管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "标签ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/tags/{id} [delete]
func (c *KnowledgeController) DeleteTag(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的标签ID")
		return
	}

	if err := c.tagService.DeleteTag(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetPopularTags 获取热门标签
// @Summary 获取热门标签
// @Description 获取热门标签列表
// @Tags 标签管理
// @Produce json
// @Param limit query int false "限制数量" default(20)
// @Success 200 {object} model.Response{data=[]model.Tag}
// @Failure 500 {object} model.Response
// @Router /api/v1/tags/popular [get]
func (c *KnowledgeController) GetPopularTags(ctx *gin.Context) {
	limitStr := ctx.DefaultQuery("limit", "20")
	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 20
	}

	tags, err := c.tagService.GetPopularTags(limit)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, tags)
}

// CreateTemplate 创建模板
// @Summary 创建模板
// @Description 创建新的模板
// @Tags 模板管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.TemplateCreateRequest true "创建模板请求"
// @Success 200 {object} model.Response{data=model.Template}
// @Failure 400 {object} model.Response
// @Router /api/v1/templates [post]
func (c *KnowledgeController) CreateTemplate(ctx *gin.Context) {
	var req model.TemplateCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	creatorID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	template, err := c.templateService.CreateTemplate(&req, creatorID)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, template)
}

// GetTemplateList 获取模板列表
// @Summary 获取模板列表
// @Description 获取所有模板列表
// @Tags 模板管理
// @Produce json
// @Success 200 {object} model.Response{data=[]model.Template}
// @Failure 500 {object} model.Response
// @Router /api/v1/templates [get]
func (c *KnowledgeController) GetTemplateList(ctx *gin.Context) {
	templates, err := c.templateService.GetTemplateList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, templates)
}

// GetTemplate 获取模板详情
// @Summary 获取模板详情
// @Description 根据ID获取模板详情
// @Tags 模板管理
// @Produce json
// @Param id path int true "模板ID"
// @Success 200 {object} model.Response{data=model.Template}
// @Failure 400 {object} model.Response
// @Router /api/v1/templates/{id} [get]
func (c *KnowledgeController) GetTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	template, err := c.templateService.GetTemplateByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, template)
}

// UpdateTemplate 更新模板
// @Summary 更新模板
// @Description 更新模板信息
// @Tags 模板管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "模板ID"
// @Param request body model.TemplateUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/templates/{id} [put]
func (c *KnowledgeController) UpdateTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	var req model.TemplateUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.templateService.UpdateTemplate(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteTemplate 删除模板
// @Summary 删除模板
// @Description 删除模板
// @Tags 模板管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "模板ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/templates/{id} [delete]
func (c *KnowledgeController) DeleteTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	if err := c.templateService.DeleteTemplate(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// UseTemplate 使用模板
// @Summary 使用模板
// @Description 使用模板（增加使用次数）
// @Tags 模板管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "模板ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/templates/{id}/use [post]
func (c *KnowledgeController) UseTemplate(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的模板ID")
		return
	}

	if err := c.templateService.UseTemplate(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}
