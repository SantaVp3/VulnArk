package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// AssetController 资产控制器
type AssetController struct {
	assetService service.AssetService
}

// NewAssetController 创建资产控制器
func NewAssetController(assetService service.AssetService) *AssetController {
	return &AssetController{
		assetService: assetService,
	}
}

// CreateAsset 创建资产
// @Summary 创建资产
// @Description 创建新的资产
// @Tags 资产管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.AssetCreateRequest true "创建资产请求"
// @Success 200 {object} model.Response{data=model.Asset}
// @Failure 400 {object} model.Response
// @Router /api/v1/assets [post]
func (c *AssetController) CreateAsset(ctx *gin.Context) {
	var req model.AssetCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	asset, err := c.assetService.CreateAsset(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, asset)
}

// GetAssetList 获取资产列表
// @Summary 获取资产列表
// @Description 获取资产列表（分页）
// @Tags 资产管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param type query string false "资产类型"
// @Param category query string false "资产分类"
// @Param department query string false "所属部门"
// @Param importance_level query string false "重要性等级"
// @Param status query string false "资产状态"
// @Param owner_id query string false "负责人ID"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/assets [get]
func (c *AssetController) GetAssetList(ctx *gin.Context) {
	var req model.AssetSearchRequest
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

	resp, err := c.assetService.GetAssetList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetAsset 获取资产详情
// @Summary 获取资产详情
// @Description 根据ID获取资产详情
// @Tags 资产管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "资产ID"
// @Success 200 {object} model.Response{data=model.AssetInfo}
// @Failure 400 {object} model.Response
// @Router /api/v1/assets/{id} [get]
func (c *AssetController) GetAsset(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的资产ID")
		return
	}

	asset, err := c.assetService.GetAssetByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, asset)
}

// UpdateAsset 更新资产
// @Summary 更新资产
// @Description 更新资产信息
// @Tags 资产管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "资产ID"
// @Param request body model.AssetUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/assets/{id} [put]
func (c *AssetController) UpdateAsset(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的资产ID")
		return
	}

	var req model.AssetUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.assetService.UpdateAsset(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteAsset 删除资产
// @Summary 删除资产
// @Description 删除资产
// @Tags 资产管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "资产ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/assets/{id} [delete]
func (c *AssetController) DeleteAsset(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的资产ID")
		return
	}

	if err := c.assetService.DeleteAsset(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetAssetStats 获取资产统计
// @Summary 获取资产统计
// @Description 获取资产统计信息
// @Tags 资产管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.AssetStats}
// @Failure 500 {object} model.Response
// @Router /api/v1/assets/stats [get]
func (c *AssetController) GetAssetStats(ctx *gin.Context) {
	stats, err := c.assetService.GetAssetStats()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// ImportAssets 批量导入资产
// @Summary 批量导入资产
// @Description 批量导入资产数据
// @Tags 资产管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.AssetImportRequest true "导入请求"
// @Success 200 {object} model.Response{data=[]string}
// @Failure 400 {object} model.Response
// @Router /api/v1/assets/import [post]
func (c *AssetController) ImportAssets(ctx *gin.Context) {
	var req model.AssetImportRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	errors, err := c.assetService.ImportAssets(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	if len(errors) > 0 {
		utils.BadRequestResponse(ctx, "导入过程中发现错误")
		return
	}

	utils.SuccessResponse(ctx, map[string]interface{}{
		"message": "导入成功",
		"errors":  errors,
	})
}

// GetAssetTypes 获取资产类型列表
// @Summary 获取资产类型列表
// @Description 获取所有可用的资产类型
// @Tags 资产管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]string}
// @Router /api/v1/assets/types [get]
func (c *AssetController) GetAssetTypes(ctx *gin.Context) {
	types := []map[string]string{
		{"value": model.AssetTypeServer, "label": "服务器"},
		{"value": model.AssetTypeDatabase, "label": "数据库"},
		{"value": model.AssetTypeApplication, "label": "应用系统"},
		{"value": model.AssetTypeNetwork, "label": "网络设备"},
	}

	utils.SuccessResponse(ctx, types)
}

// GetImportanceLevels 获取重要性等级列表
// @Summary 获取重要性等级列表
// @Description 获取所有可用的重要性等级
// @Tags 资产管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]string}
// @Router /api/v1/assets/importance-levels [get]
func (c *AssetController) GetImportanceLevels(ctx *gin.Context) {
	levels := []map[string]interface{}{
		{"value": model.ImportanceLevelHigh, "label": "高"},
		{"value": model.ImportanceLevelMedium, "label": "中"},
		{"value": model.ImportanceLevelLow, "label": "低"},
	}

	utils.SuccessResponse(ctx, levels)
}
