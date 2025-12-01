package service

import (
	"errors"
	"strings"

	"vulnark/internal/model"
	"vulnark/internal/repository"
)

// PermissionService 权限服务接口
type PermissionService interface {
	// 权限管理
	CreatePermission(req *model.PermissionCreateRequest) (*model.Permission, error)
	GetPermissionByID(id uint) (*model.Permission, error)
	UpdatePermission(id uint, req *model.PermissionUpdateRequest) error
	DeletePermission(id uint) error
	GetPermissionList() ([]*model.Permission, error)
	GetPermissionsByModule(module string) ([]*model.Permission, error)
	
	// 角色权限管理
	AssignRolePermissions(roleID uint, req *model.RolePermissionRequest) error
	GetRolePermissions(roleID uint) ([]*model.Permission, error)
	
	// 用户权限检查
	GetUserPermissions(userID uint) ([]*model.Permission, error)
	CheckUserPermission(userID uint, permissionName string) (bool, error)
	
	// 初始化默认权限
	InitializeDefaultPermissions() error
}

// permissionService 权限服务实现
type permissionService struct {
	permissionRepo     repository.PermissionRepository
	rolePermissionRepo repository.RolePermissionRepository
	roleRepo           repository.RoleRepository
	userRepo           repository.UserRepository
}

// NewPermissionService 创建权限服务
func NewPermissionService(permissionRepo repository.PermissionRepository, rolePermissionRepo repository.RolePermissionRepository, roleRepo repository.RoleRepository, userRepo repository.UserRepository) PermissionService {
	return &permissionService{
		permissionRepo:     permissionRepo,
		rolePermissionRepo: rolePermissionRepo,
		roleRepo:           roleRepo,
		userRepo:           userRepo,
	}
}

// CreatePermission 创建权限
func (s *permissionService) CreatePermission(req *model.PermissionCreateRequest) (*model.Permission, error) {
	// 检查权限名称是否已存在
	if _, err := s.permissionRepo.GetByName(req.Name); err == nil {
		return nil, errors.New("权限名称已存在")
	}

	permission := &model.Permission{
		Name:        req.Name,
		DisplayName: req.DisplayName,
		Description: req.Description,
		Module:      req.Module,
		Action:      req.Action,
		Resource:    req.Resource,
		IsActive:    true,
	}

	if err := s.permissionRepo.Create(permission); err != nil {
		return nil, errors.New("创建权限失败")
	}

	return permission, nil
}

// GetPermissionByID 根据ID获取权限
func (s *permissionService) GetPermissionByID(id uint) (*model.Permission, error) {
	return s.permissionRepo.GetByID(id)
}

// UpdatePermission 更新权限
func (s *permissionService) UpdatePermission(id uint, req *model.PermissionUpdateRequest) error {
	permission, err := s.permissionRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 更新字段
	if req.DisplayName != "" {
		permission.DisplayName = req.DisplayName
	}
	if req.Description != "" {
		permission.Description = req.Description
	}
	if req.IsActive != nil {
		permission.IsActive = *req.IsActive
	}

	return s.permissionRepo.Update(permission)
}

// DeletePermission 删除权限
func (s *permissionService) DeletePermission(id uint) error {
	// 检查权限是否存在
	if _, err := s.permissionRepo.GetByID(id); err != nil {
		return err
	}

	return s.permissionRepo.Delete(id)
}

// GetPermissionList 获取权限列表
func (s *permissionService) GetPermissionList() ([]*model.Permission, error) {
	return s.permissionRepo.List()
}

// GetPermissionsByModule 根据模块获取权限列表
func (s *permissionService) GetPermissionsByModule(module string) ([]*model.Permission, error) {
	return s.permissionRepo.GetByModule(module)
}

// AssignRolePermissions 分配角色权限
func (s *permissionService) AssignRolePermissions(roleID uint, req *model.RolePermissionRequest) error {
	// 检查角色是否存在
	if _, err := s.roleRepo.GetByID(roleID); err != nil {
		return errors.New("角色不存在")
	}

	// 检查权限是否都存在
	for _, permissionID := range req.PermissionIDs {
		if _, err := s.permissionRepo.GetByID(permissionID); err != nil {
			return errors.New("权限不存在")
		}
	}

	return s.rolePermissionRepo.AssignPermissions(roleID, req.PermissionIDs)
}

// GetRolePermissions 获取角色权限列表
func (s *permissionService) GetRolePermissions(roleID uint) ([]*model.Permission, error) {
	// 检查角色是否存在
	if _, err := s.roleRepo.GetByID(roleID); err != nil {
		return nil, errors.New("角色不存在")
	}

	return s.rolePermissionRepo.GetRolePermissions(roleID)
}

// GetUserPermissions 获取用户权限列表
func (s *permissionService) GetUserPermissions(userID uint) ([]*model.Permission, error) {
	return s.permissionRepo.GetUserPermissions(userID)
}

// CheckUserPermission 检查用户权限（支持权限代码和权限名称）
func (s *permissionService) CheckUserPermission(userID uint, permissionName string) (bool, error) {
	// 获取用户信息和角色
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return false, err
	}

	// 获取用户角色
	role, err := s.roleRepo.GetByID(user.RoleID)
	if err != nil {
		return false, err
	}

	// 检查角色权限
	for _, perm := range role.Permissions {
		// 检查通配符权限
		if perm == "*" {
			return true, nil
		}

		// 检查精确匹配
		if perm == permissionName {
			return true, nil
		}

		// 检查模块级通配符 (如 "user:*" 匹配 "user:create")
		if strings.HasSuffix(perm, ":*") {
			module := strings.TrimSuffix(perm, ":*")
			if strings.HasPrefix(permissionName, module+":") {
				return true, nil
			}
		}
	}

	// 获取用户权限（从权限表）
	permissions, err := s.GetUserPermissions(userID)
	if err != nil {
		return false, err
	}

	// 检查是否包含指定权限（支持权限名称）
	for _, permission := range permissions {
		if permission.Name == permissionName {
			return true, nil
		}
	}

	return false, nil
}

// InitializeDefaultPermissions 初始化默认权限
func (s *permissionService) InitializeDefaultPermissions() error {
	defaultPermissions := []model.Permission{
		// 用户管理权限
		{Name: "user:create", DisplayName: "创建用户", Module: "user", Action: "create", Resource: "user"},
		{Name: "user:read", DisplayName: "查看用户", Module: "user", Action: "read", Resource: "user"},
		{Name: "user:update", DisplayName: "更新用户", Module: "user", Action: "update", Resource: "user"},
		{Name: "user:delete", DisplayName: "删除用户", Module: "user", Action: "delete", Resource: "user"},
		{Name: "user:manage", DisplayName: "管理用户", Module: "user", Action: "manage", Resource: "user"},

		// 角色管理权限
		{Name: "role:create", DisplayName: "创建角色", Module: "role", Action: "create", Resource: "role"},
		{Name: "role:read", DisplayName: "查看角色", Module: "role", Action: "read", Resource: "role"},
		{Name: "role:update", DisplayName: "更新角色", Module: "role", Action: "update", Resource: "role"},
		{Name: "role:delete", DisplayName: "删除角色", Module: "role", Action: "delete", Resource: "role"},
		{Name: "role:assign", DisplayName: "分配角色", Module: "role", Action: "assign", Resource: "role"},

		// 资产管理权限
		{Name: "asset:create", DisplayName: "创建资产", Module: "asset", Action: "create", Resource: "asset"},
		{Name: "asset:read", DisplayName: "查看资产", Module: "asset", Action: "read", Resource: "asset"},
		{Name: "asset:update", DisplayName: "更新资产", Module: "asset", Action: "update", Resource: "asset"},
		{Name: "asset:delete", DisplayName: "删除资产", Module: "asset", Action: "delete", Resource: "asset"},
		{Name: "asset:import", DisplayName: "导入资产", Module: "asset", Action: "import", Resource: "asset"},
		{Name: "asset:export", DisplayName: "导出资产", Module: "asset", Action: "export", Resource: "asset"},

		// 漏洞管理权限
		{Name: "vulnerability:create", DisplayName: "创建漏洞", Module: "vulnerability", Action: "create", Resource: "vulnerability"},
		{Name: "vulnerability:read", DisplayName: "查看漏洞", Module: "vulnerability", Action: "read", Resource: "vulnerability"},
		{Name: "vulnerability:update", DisplayName: "更新漏洞", Module: "vulnerability", Action: "update", Resource: "vulnerability"},
		{Name: "vulnerability:delete", DisplayName: "删除漏洞", Module: "vulnerability", Action: "delete", Resource: "vulnerability"},
		{Name: "vulnerability:assign", DisplayName: "分配漏洞", Module: "vulnerability", Action: "assign", Resource: "vulnerability"},

		// 报告管理权限
		{Name: "report:create", DisplayName: "创建报告", Module: "report", Action: "create", Resource: "report"},
		{Name: "report:read", DisplayName: "查看报告", Module: "report", Action: "read", Resource: "report"},
		{Name: "report:update", DisplayName: "更新报告", Module: "report", Action: "update", Resource: "report"},
		{Name: "report:delete", DisplayName: "删除报告", Module: "report", Action: "delete", Resource: "report"},
		{Name: "report:review", DisplayName: "审核报告", Module: "report", Action: "review", Resource: "report"},

		// 通知管理权限
		{Name: "notification:create", DisplayName: "创建通知", Module: "notification", Action: "create", Resource: "notification"},
		{Name: "notification:read", DisplayName: "查看通知", Module: "notification", Action: "read", Resource: "notification"},
		{Name: "notification:manage", DisplayName: "管理通知", Module: "notification", Action: "manage", Resource: "notification"},

		// 系统管理权限
		{Name: "system:config", DisplayName: "系统配置", Module: "system", Action: "manage", Resource: "config"},
		{Name: "system:audit", DisplayName: "审计日志", Module: "system", Action: "read", Resource: "audit"},
		{Name: "system:permission", DisplayName: "权限管理", Module: "system", Action: "manage", Resource: "permission"},
	}

	for _, permission := range defaultPermissions {
		// 检查权限是否已存在
		if _, err := s.permissionRepo.GetByName(permission.Name); err != nil {
			// 权限不存在，创建它
			permission.IsActive = true
			if err := s.permissionRepo.Create(&permission); err != nil {
				return err
			}
		}
	}

	return nil
}

// SystemConfigService 系统配置服务接口
type SystemConfigService interface {
	GetConfigByKey(key string) (*model.SystemConfig, error)
	UpdateConfig(key string, req *model.SystemConfigUpdateRequest) error
	GetConfigList() ([]*model.SystemConfig, error)
	GetConfigsByCategory(category string) ([]*model.SystemConfig, error)
	GetPublicConfigs() ([]*model.SystemConfig, error)
	InitializeDefaultConfigs() error
}

// systemConfigService 系统配置服务实现
type systemConfigService struct {
	configRepo repository.SystemConfigRepository
}

// NewSystemConfigService 创建系统配置服务
func NewSystemConfigService(configRepo repository.SystemConfigRepository) SystemConfigService {
	return &systemConfigService{
		configRepo: configRepo,
	}
}

// GetConfigByKey 根据键获取配置
func (s *systemConfigService) GetConfigByKey(key string) (*model.SystemConfig, error) {
	return s.configRepo.GetByKey(key)
}

// UpdateConfig 更新配置
func (s *systemConfigService) UpdateConfig(key string, req *model.SystemConfigUpdateRequest) error {
	config, err := s.configRepo.GetByKey(key)
	if err != nil {
		return err
	}

	if !config.IsEditable {
		return errors.New("该配置不允许编辑")
	}

	config.Value = req.Value
	return s.configRepo.Update(config)
}

// GetConfigList 获取配置列表
func (s *systemConfigService) GetConfigList() ([]*model.SystemConfig, error) {
	return s.configRepo.List()
}

// GetConfigsByCategory 根据分类获取配置列表
func (s *systemConfigService) GetConfigsByCategory(category string) ([]*model.SystemConfig, error) {
	return s.configRepo.GetByCategory(category)
}

// GetPublicConfigs 获取公开配置列表
func (s *systemConfigService) GetPublicConfigs() ([]*model.SystemConfig, error) {
	return s.configRepo.GetPublicConfigs()
}

// InitializeDefaultConfigs 初始化默认配置
func (s *systemConfigService) InitializeDefaultConfigs() error {
	defaultConfigs := []model.SystemConfig{
		{
			Key:          "system.name",
			Value:        model.ConfigValue{"value": "VulnArk漏洞管理系统"},
			Type:         model.ConfigTypeString,
			Category:     model.ConfigCategorySystem,
			DisplayName:  "系统名称",
			Description:  "系统显示名称",
			IsPublic:     true,
			IsEditable:   true,
			DefaultValue: model.ConfigValue{"value": "VulnArk漏洞管理系统"},
		},
		{
			Key:          "system.version",
			Value:        model.ConfigValue{"value": "1.0.0"},
			Type:         model.ConfigTypeString,
			Category:     model.ConfigCategorySystem,
			DisplayName:  "系统版本",
			Description:  "当前系统版本号",
			IsPublic:     true,
			IsEditable:   false,
			DefaultValue: model.ConfigValue{"value": "1.0.0"},
		},
		{
			Key:          "security.password_min_length",
			Value:        model.ConfigValue{"value": 8},
			Type:         model.ConfigTypeInt,
			Category:     model.ConfigCategorySecurity,
			DisplayName:  "密码最小长度",
			Description:  "用户密码最小长度要求",
			IsPublic:     false,
			IsEditable:   true,
			DefaultValue: model.ConfigValue{"value": 8},
		},
		{
			Key:          "security.session_timeout",
			Value:        model.ConfigValue{"value": 3600},
			Type:         model.ConfigTypeInt,
			Category:     model.ConfigCategorySecurity,
			DisplayName:  "会话超时时间",
			Description:  "用户会话超时时间（秒）",
			IsPublic:     false,
			IsEditable:   true,
			DefaultValue: model.ConfigValue{"value": 3600},
		},
		{
			Key:          "notification.email_enabled",
			Value:        model.ConfigValue{"value": true},
			Type:         model.ConfigTypeBool,
			Category:     model.ConfigCategoryNotification,
			DisplayName:  "邮件通知开关",
			Description:  "是否启用邮件通知功能",
			IsPublic:     false,
			IsEditable:   true,
			DefaultValue: model.ConfigValue{"value": true},
		},
		{
			Key:          "audit.log_retention_days",
			Value:        model.ConfigValue{"value": 90},
			Type:         model.ConfigTypeInt,
			Category:     model.ConfigCategoryAudit,
			DisplayName:  "审计日志保留天数",
			Description:  "审计日志保留天数，超过将自动删除",
			IsPublic:     false,
			IsEditable:   true,
			DefaultValue: model.ConfigValue{"value": 90},
		},
	}

	for _, config := range defaultConfigs {
		// 检查配置是否已存在
		if _, err := s.configRepo.GetByKey(config.Key); err != nil {
			// 配置不存在，创建它
			if err := s.configRepo.Create(&config); err != nil {
				return err
			}
		}
	}

	return nil
}

// AuditLogService 审计日志服务接口
type AuditLogService interface {
	CreateAuditLog(log *model.AuditLog) error
	GetAuditLogByID(id uint) (*model.AuditLog, error)
	GetAuditLogList(req *model.AuditLogSearchRequest) (*model.PaginationResponse, error)
	GetAuditLogStats() (map[string]interface{}, error)
	CleanupOldLogs(days int) error
}

// auditLogService 审计日志服务实现
type auditLogService struct {
	auditRepo repository.AuditLogRepository
}

// NewAuditLogService 创建审计日志服务
func NewAuditLogService(auditRepo repository.AuditLogRepository) AuditLogService {
	return &auditLogService{
		auditRepo: auditRepo,
	}
}

// CreateAuditLog 创建审计日志
func (s *auditLogService) CreateAuditLog(log *model.AuditLog) error {
	return s.auditRepo.Create(log)
}

// GetAuditLogByID 根据ID获取审计日志
func (s *auditLogService) GetAuditLogByID(id uint) (*model.AuditLog, error) {
	return s.auditRepo.GetByID(id)
}

// GetAuditLogList 获取审计日志列表
func (s *auditLogService) GetAuditLogList(req *model.AuditLogSearchRequest) (*model.PaginationResponse, error) {
	logs, total, err := s.auditRepo.List(req)
	if err != nil {
		return nil, err
	}

	return model.NewPaginationResponse(total, req.Page, req.PageSize, logs), nil
}

// GetAuditLogStats 获取审计日志统计
func (s *auditLogService) GetAuditLogStats() (map[string]interface{}, error) {
	return s.auditRepo.GetStats()
}

// CleanupOldLogs 清理旧日志
func (s *auditLogService) CleanupOldLogs(days int) error {
	return s.auditRepo.DeleteOldLogs(days)
}
