package repository

import (
	"errors"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// UserRepository 用户仓储接口
type UserRepository interface {
	Create(user *model.User) error
	GetByID(id uint) (*model.User, error)
	GetByUsername(username string) (*model.User, error)
	GetByEmail(email string) (*model.User, error)
	Update(id uint, req *model.UserUpdateRequest) error
	UpdatePassword(id uint, passwordHash string) error
	Update2FA(id uint, enabled bool, secret string) error
	Delete(id uint) error
	List(req *model.SearchRequest) ([]*model.User, int64, error)
	UpdateLastLogin(id uint) error
}

// userRepository 用户仓储实现
type userRepository struct {
	db *gorm.DB
}

// NewUserRepository 创建用户仓储
func NewUserRepository(db *gorm.DB) UserRepository {
	return &userRepository{
		db: db,
	}
}

// Create 创建用户
func (r *userRepository) Create(user *model.User) error {
	return r.db.Create(user).Error
}

// GetByID 根据ID获取用户
func (r *userRepository) GetByID(id uint) (*model.User, error) {
	var user model.User
	err := r.db.Preload("Role").First(&user, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("用户不存在")
		}
		return nil, err
	}
	return &user, nil
}

// GetByUsername 根据用户名获取用户
func (r *userRepository) GetByUsername(username string) (*model.User, error) {
	var user model.User
	err := r.db.Preload("Role").Where("username = ?", username).First(&user).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("用户不存在")
		}
		return nil, err
	}
	return &user, nil
}

// GetByEmail 根据邮箱获取用户
func (r *userRepository) GetByEmail(email string) (*model.User, error) {
	var user model.User
	err := r.db.Preload("Role").Where("email = ?", email).First(&user).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("用户不存在")
		}
		return nil, err
	}
	return &user, nil
}

// Update 更新用户
func (r *userRepository) Update(id uint, req *model.UserUpdateRequest) error {
	return r.db.Model(&model.User{}).Where("id = ?", id).Updates(req).Error
}

// UpdatePassword 更新用户密码
func (r *userRepository) UpdatePassword(id uint, passwordHash string) error {
	return r.db.Model(&model.User{}).Where("id = ?", id).Update("password_hash", passwordHash).Error
}

// Update2FA 更新用户2FA设置
func (r *userRepository) Update2FA(id uint, enabled bool, secret string) error {
	updates := map[string]interface{}{
		"two_factor_enabled": enabled,
	}
	if secret != "" {
		updates["two_factor_secret"] = secret
	}
	return r.db.Model(&model.User{}).Where("id = ?", id).Updates(updates).Error
}

// Delete 删除用户
func (r *userRepository) Delete(id uint) error {
	return r.db.Delete(&model.User{}, id).Error
}

// List 获取用户列表
func (r *userRepository) List(req *model.SearchRequest) ([]*model.User, int64, error) {
	var users []*model.User
	var total int64

	query := r.db.Model(&model.User{}).Preload("Role")

	// 搜索条件
	if req.Keyword != "" {
		query = query.Where("username LIKE ? OR real_name LIKE ? OR email LIKE ?",
			"%"+req.Keyword+"%", "%"+req.Keyword+"%", "%"+req.Keyword+"%")
	}

	if req.Status != "" {
		query = query.Where("status = ?", req.Status)
	}

	// 获取总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 分页
	offset := (req.Page - 1) * req.PageSize
	if err := query.Offset(offset).Limit(req.PageSize).Find(&users).Error; err != nil {
		return nil, 0, err
	}

	return users, total, nil
}

// UpdateLastLogin 更新最后登录时间
func (r *userRepository) UpdateLastLogin(id uint) error {
	return r.db.Model(&model.User{}).Where("id = ?", id).Update("last_login_at", gorm.Expr("NOW()")).Error
}

// RoleRepository 角色仓储接口
type RoleRepository interface {
	Create(role *model.Role) error
	GetByID(id uint) (*model.Role, error)
	GetByName(name string) (*model.Role, error)
	Update(role *model.Role) error
	Delete(id uint) error
	List() ([]*model.Role, error)
}

// roleRepository 角色仓储实现
type roleRepository struct {
	db *gorm.DB
}

// NewRoleRepository 创建角色仓储
func NewRoleRepository(db *gorm.DB) RoleRepository {
	return &roleRepository{
		db: db,
	}
}

// Create 创建角色
func (r *roleRepository) Create(role *model.Role) error {
	return r.db.Create(role).Error
}

// GetByID 根据ID获取角色
func (r *roleRepository) GetByID(id uint) (*model.Role, error) {
	var role model.Role
	err := r.db.First(&role, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("角色不存在")
		}
		return nil, err
	}
	return &role, nil
}

// GetByName 根据名称获取角色
func (r *roleRepository) GetByName(name string) (*model.Role, error) {
	var role model.Role
	err := r.db.Where("name = ?", name).First(&role).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("角色不存在")
		}
		return nil, err
	}
	return &role, nil
}

// Update 更新角色
func (r *roleRepository) Update(role *model.Role) error {
	return r.db.Save(role).Error
}

// Delete 删除角色
func (r *roleRepository) Delete(id uint) error {
	// 检查是否有用户使用该角色
	var count int64
	if err := r.db.Model(&model.User{}).Where("role_id = ?", id).Count(&count).Error; err != nil {
		return err
	}
	if count > 0 {
		return errors.New("该角色正在被使用，无法删除")
	}
	return r.db.Delete(&model.Role{}, id).Error
}

// List 获取角色列表
func (r *roleRepository) List() ([]*model.Role, error) {
	var roles []*model.Role
	err := r.db.Find(&roles).Error
	return roles, err
}
