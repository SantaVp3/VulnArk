package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/middleware"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// UserController 用户控制器
type UserController struct {
	userService service.UserService
	roleService service.RoleService
}

// NewUserController 创建用户控制器
func NewUserController(userService service.UserService, roleService service.RoleService) *UserController {
	return &UserController{
		userService: userService,
		roleService: roleService,
	}
}

// Login 用户登录
// @Summary 用户登录
// @Description 用户登录接口
// @Tags 用户管理
// @Accept json
// @Produce json
// @Param request body model.UserLoginRequest true "登录请求"
// @Success 200 {object} model.Response{data=model.UserLoginResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/login [post]
func (c *UserController) Login(ctx *gin.Context) {
	var req model.UserLoginRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	resp, err := c.userService.Login(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetProfile 获取用户资料
// @Summary 获取用户资料
// @Description 获取当前登录用户的资料
// @Tags 用户管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=model.UserInfo}
// @Failure 401 {object} model.Response
// @Router /api/v1/profile [get]
func (c *UserController) GetProfile(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	profile, err := c.userService.GetProfile(userID)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, profile)
}

// UpdateProfile 更新用户资料
// @Summary 更新用户资料
// @Description 更新当前登录用户的资料
// @Tags 用户管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.UserUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/profile [put]
func (c *UserController) UpdateProfile(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	var req model.UserUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.userService.UpdateProfile(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// ChangePassword 修改密码
// @Summary 修改密码
// @Description 修改当前登录用户的密码
// @Tags 用户管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.ChangePasswordRequest true "修改密码请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/change-password [post]
func (c *UserController) ChangePassword(ctx *gin.Context) {
	userID, exists := middleware.GetCurrentUserID(ctx)
	if !exists {
		utils.UnauthorizedResponse(ctx, "")
		return
	}

	var req model.ChangePasswordRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.userService.ChangePassword(userID, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// CreateUser 创建用户
// @Summary 创建用户
// @Description 管理员创建新用户
// @Tags 用户管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body model.UserCreateRequest true "创建用户请求"
// @Success 200 {object} model.Response{data=model.User}
// @Failure 400 {object} model.Response
// @Router /api/v1/users [post]
func (c *UserController) CreateUser(ctx *gin.Context) {
	var req model.UserCreateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	user, err := c.userService.CreateUser(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, user)
}

// GetUserList 获取用户列表
// @Summary 获取用户列表
// @Description 获取用户列表（分页）
// @Tags 用户管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param status query string false "用户状态"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/users [get]
func (c *UserController) GetUserList(ctx *gin.Context) {
	var req model.SearchRequest
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

	resp, err := c.userService.GetUserList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetUser 获取用户详情
// @Summary 获取用户详情
// @Description 根据ID获取用户详情
// @Tags 用户管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "用户ID"
// @Success 200 {object} model.Response{data=model.UserInfo}
// @Failure 400 {object} model.Response
// @Router /api/v1/users/{id} [get]
func (c *UserController) GetUser(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的用户ID")
		return
	}

	user, err := c.userService.GetUserByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, user)
}

// UpdateUser 更新用户
// @Summary 更新用户
// @Description 管理员更新用户信息
// @Tags 用户管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "用户ID"
// @Param request body model.UserUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/users/{id} [put]
func (c *UserController) UpdateUser(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的用户ID")
		return
	}

	var req model.UserUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.userService.UpdateUser(uint(id), &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// DeleteUser 删除用户
// @Summary 删除用户
// @Description 管理员删除用户
// @Tags 用户管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "用户ID"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/users/{id} [delete]
func (c *UserController) DeleteUser(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的用户ID")
		return
	}

	if err := c.userService.DeleteUser(uint(id)); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetDepartmentOptions 获取部门选项
// @Summary 获取部门选项
// @Description 获取可用的部门选项列表
// @Tags 用户管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]map[string]string}
// @Router /api/v1/users/departments [get]
func (c *UserController) GetDepartmentOptions(ctx *gin.Context) {
	departments := []map[string]string{
		{"value": "IT部门", "label": "IT部门"},
		{"value": "安全部门", "label": "安全部门"},
		{"value": "开发部门", "label": "开发部门"},
		{"value": "测试部门", "label": "测试部门"},
		{"value": "运维部门", "label": "运维部门"},
		{"value": "产品部门", "label": "产品部门"},
		{"value": "设计部门", "label": "设计部门"},
		{"value": "市场部门", "label": "市场部门"},
		{"value": "销售部门", "label": "销售部门"},
		{"value": "人事部门", "label": "人事部门"},
		{"value": "财务部门", "label": "财务部门"},
		{"value": "法务部门", "label": "法务部门"},
	}

	utils.SuccessResponse(ctx, departments)
}
