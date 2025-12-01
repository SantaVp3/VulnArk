package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// PermissionController 权限控制器
type PermissionController struct {
	permissionService service.PermissionService
}

// NewPermissionController 创建权限控制器
func NewPermissionController(permissionService service.PermissionService) *PermissionController {
	return &PermissionController{
		permissionService: permissionService,
	}
}

// CreatePermission 创建权限
// @Summary 创建权限
// @Description 创建新的系统权限
// @Tags 权限管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.PermissionCreateRequest true "创建权限请求"
// @Success 200 {object} model.Response{data=model.Permission}
// @Failure 400 {object} model.Response
// @Router /api/v1/permissions [post]
func (c *PermissionController) CreatePermission(ctx *gin.Context) {
	var req model.PermissionCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	permission, err := c.permissionService.CreatePermission(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, permission)
}

// GetPermissionList 获取权限列表
// @Summary 获取权限列表
// @Description 获取所有权限列表
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]model.Permission}
// @Failure 500 {object} model.Response
// @Router /api/v1/permissions [get]
func (c *PermissionController) GetPermissionList(ctx *gin.Context) {
	permissions, err := c.permissionService.GetPermissionList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, permissions)
}

// GetPermission 获取权限详情
// @Summary 获取权限详情
// @Description 根据ID获取权限详情
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "权限ID"
// @Success 200 {object} model.Response{data=model.Permission}
// @Failure 400 {object} model.Response
// @Router /api/v1/permissions/{id} [get]
func (c *PermissionController) GetPermission(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的权限ID")
		return
	}

	permission, err := c.permissionService.GetPermissionByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, permission)
}

// UpdatePermission 更新权限
// @Summary 更新权限
// @Description 更新权限信息
// @Tags 权限管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "权限ID"
// @Param request body model.PermissionUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/permissions/{id} [put]
func (c *PermissionController) UpdatePermission(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的权限ID")
		return
	}

	var req model.PermissionUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.permissionService.UpdatePermission(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeletePermission 删除权限
// @Summary 删除权限
// @Description 删除权限
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "权限ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/permissions/{id} [delete]
func (c *PermissionController) DeletePermission(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的权限ID")
		return
	}

	if err := c.permissionService.DeletePermission(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetPermissionsByModule 根据模块获取权限列表
// @Summary 根据模块获取权限列表
// @Description 根据模块获取权限列表
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Param module path string true "模块名称"
// @Success 200 {object} model.Response{data=[]model.Permission}
// @Failure 500 {object} model.Response
// @Router /api/v1/permissions/module/{module} [get]
func (c *PermissionController) GetPermissionsByModule(ctx *gin.Context) {
	module := ctx.Param("module")
	if module == "" {
		utils.BadRequestResponse(ctx, "模块名称不能为空")
		return
	}

	permissions, err := c.permissionService.GetPermissionsByModule(module)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, permissions)
}

// AssignRolePermissions 分配角色权限
// @Summary 分配角色权限
// @Description 为角色分配权限
// @Tags 权限管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param role_id path int true "角色ID"
// @Param request body model.RolePermissionRequest true "权限分配请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/roles/{role_id}/permissions [post]
func (c *PermissionController) AssignRolePermissions(ctx *gin.Context) {
	roleIDStr := ctx.Param("role_id")
	roleID, err := strconv.ParseUint(roleIDStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的角色ID")
		return
	}

	var req model.RolePermissionRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.permissionService.AssignRolePermissions(uint(roleID), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetRolePermissions 获取角色权限列表
// @Summary 获取角色权限列表
// @Description 获取指定角色的权限列表
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Param role_id path int true "角色ID"
// @Success 200 {object} model.Response{data=[]model.Permission}
// @Failure 400 {object} model.Response
// @Router /api/v1/roles/{role_id}/permissions [get]
func (c *PermissionController) GetRolePermissions(ctx *gin.Context) {
	roleIDStr := ctx.Param("role_id")
	roleID, err := strconv.ParseUint(roleIDStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的角色ID")
		return
	}

	permissions, err := c.permissionService.GetRolePermissions(uint(roleID))
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, permissions)
}

// GetMyPermissions 获取我的权限列表
// @Summary 获取我的权限列表
// @Description 获取当前用户的权限列表
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]model.Permission}
// @Failure 401 {object} model.Response
// @Router /api/v1/permissions/my [get]
func (c *PermissionController) GetMyPermissions(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	permissions, err := c.permissionService.GetUserPermissions(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, permissions)
}

// CheckPermission 检查权限
// @Summary 检查权限
// @Description 检查当前用户是否有指定权限
// @Tags 权限管理
// @Produce json
// @Security ApiKeyAuth
// @Param permission query string true "权限名称"
// @Success 200 {object} model.Response{data=bool}
// @Failure 401 {object} model.Response
// @Router /api/v1/permissions/check [get]
func (c *PermissionController) CheckPermission(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	permission := ctx.Query("permission")
	if permission == "" {
		utils.BadRequestResponse(ctx, "权限名称不能为空")
		return
	}

	hasPermission, err := c.permissionService.CheckUserPermission(userID, permission)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, hasPermission)
}
