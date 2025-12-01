package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// RoleController 角色控制器
type RoleController struct {
	roleService service.RoleService
}

// NewRoleController 创建角色控制器
func NewRoleController(roleService service.RoleService) *RoleController {
	return &RoleController{
		roleService: roleService,
	}
}

// CreateRole 创建角色
// @Summary 创建角色
// @Description 管理员创建新角色
// @Tags 角色管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.RoleCreateRequest true "创建角色请求"
// @Success 200 {object} model.Response{data=model.Role}
// @Failure 400 {object} model.Response
// @Router /api/v1/roles [post]
func (c *RoleController) CreateRole(ctx *gin.Context) {
	var req model.RoleCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	role, err := c.roleService.CreateRole(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, role)
}

// GetRoleList 获取角色列表
// @Summary 获取角色列表
// @Description 获取所有角色列表
// @Tags 角色管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]model.Role}
// @Failure 500 {object} model.Response
// @Router /api/v1/roles [get]
func (c *RoleController) GetRoleList(ctx *gin.Context) {
	roles, err := c.roleService.GetRoleList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, roles)
}

// GetRole 获取角色详情
// @Summary 获取角色详情
// @Description 根据ID获取角色详情
// @Tags 角色管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "角色ID"
// @Success 200 {object} model.Response{data=model.Role}
// @Failure 400 {object} model.Response
// @Router /api/v1/roles/{id} [get]
func (c *RoleController) GetRole(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的角色ID")
		return
	}

	role, err := c.roleService.GetRoleByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, role)
}

// UpdateRole 更新角色
// @Summary 更新角色
// @Description 管理员更新角色信息
// @Tags 角色管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "角色ID"
// @Param request body model.RoleUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/roles/{id} [put]
func (c *RoleController) UpdateRole(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的角色ID")
		return
	}

	var req model.RoleUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.roleService.UpdateRole(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteRole 删除角色
// @Summary 删除角色
// @Description 管理员删除角色
// @Tags 角色管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "角色ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/roles/{id} [delete]
func (c *RoleController) DeleteRole(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的角色ID")
		return
	}

	if err := c.roleService.DeleteRole(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetRoleOptions 获取角色选项（用于用户创建）
// @Summary 获取角色选项
// @Description 获取可用于用户创建的角色选项列表
// @Tags 角色管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]model.RoleOption}
// @Failure 500 {object} model.Response
// @Router /api/v1/roles/options [get]
func (c *RoleController) GetRoleOptions(ctx *gin.Context) {
	roles, err := c.roleService.GetRoleList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	// 转换为选项格式
	options := make([]map[string]interface{}, 0, len(roles))
	for _, role := range roles {
		options = append(options, map[string]interface{}{
			"value": role.ID,
			"label": role.Name,
			"description": role.Description,
		})
	}

	utils.SuccessResponse(ctx, options)
}
