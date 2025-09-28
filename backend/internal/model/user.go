package model

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"
)

// Role 角色模型
type Role struct {
	ID          uint        `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time   `json:"created_at"`
	UpdatedAt   time.Time   `json:"updated_at"`
	Name        string      `json:"name" gorm:"size:50;not null" binding:"required"`
	Description string      `json:"description" gorm:"size:255"`
	Permissions Permissions `json:"permissions" gorm:"type:json"`
	Users       []User      `json:"-" gorm:"foreignKey:RoleID"`
}

// Permissions 权限列表类型
type Permissions []string

// Value 实现 driver.Valuer 接口
func (p Permissions) Value() (driver.Value, error) {
	if len(p) == 0 {
		return "[]", nil
	}
	return json.Marshal(p)
}

// Scan 实现 sql.Scanner 接口
func (p *Permissions) Scan(value interface{}) error {
	if value == nil {
		*p = Permissions{}
		return nil
	}
	
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}
	
	return json.Unmarshal(bytes, p)
}

// User 用户模型
type User struct {
	ID          uint       `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time  `json:"created_at"`
	UpdatedAt   time.Time  `json:"updated_at"`
	Username    string     `json:"username" gorm:"size:50;not null" binding:"required"`
	Email       string     `json:"email" gorm:"size:100;not null" binding:"required,email"`
	PasswordHash string    `json:"-" gorm:"size:255;not null"`
	RealName    string     `json:"real_name" gorm:"size:50;not null" binding:"required"`
	Phone       string     `json:"phone" gorm:"size:20"`
	Avatar      string     `json:"avatar" gorm:"size:255"`
	RoleID      uint       `json:"role_id" gorm:"not null" binding:"required"`
	Department        string     `json:"department" gorm:"size:100"`
	Status            int        `json:"status" gorm:"default:1"` // 1-正常，0-禁用
	TwoFactorEnabled  bool       `json:"two_factor_enabled" gorm:"default:false"`
	TwoFactorSecret   string     `json:"-" gorm:"size:255"`
	LastLoginAt       *time.Time `json:"last_login_at"`

	// 关联
	Role Role `json:"role" gorm:"foreignKey:RoleID"`
}

// TableName 指定表名
func (User) TableName() string {
	return "users"
}

// TableName 指定表名
func (Role) TableName() string {
	return "roles"
}

// UserCreateRequest 用户创建请求
type UserCreateRequest struct {
	Username   string `json:"username" binding:"required,min=3,max=50"`
	Email      string `json:"email" binding:"required,email"`
	Password   string `json:"password" binding:"required,min=6"`
	RealName   string `json:"real_name" binding:"required"`
	Phone      string `json:"phone"`
	RoleID     uint   `json:"role_id" binding:"required"`
	Department string `json:"department"`
}

// UserUpdateRequest 用户更新请求
type UserUpdateRequest struct {
	Email      string `json:"email" binding:"omitempty,email"`
	RealName   string `json:"real_name"`
	Phone      string `json:"phone"`
	RoleID     uint   `json:"role_id"`
	Department string `json:"department"`
	Status     *int   `json:"status"`
}

// UserLoginRequest 用户登录请求
type UserLoginRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

// UserLoginResponse 用户登录响应
type UserLoginResponse struct {
	Token     string    `json:"token"`
	ExpiresAt time.Time `json:"expires_at"`
	User      UserInfo  `json:"user"`
}

// UserInfo 用户信息
type UserInfo struct {
	ID         uint        `json:"id"`
	Username   string      `json:"username"`
	Email      string      `json:"email"`
	RealName   string      `json:"real_name"`
	Phone      string      `json:"phone"`
	Avatar     string      `json:"avatar"`
	Department string      `json:"department"`
	Status     int         `json:"status"`
	Role       RoleInfo    `json:"role"`
	LastLoginAt *time.Time `json:"last_login_at"`
}

// RoleInfo 角色信息
type RoleInfo struct {
	ID          uint        `json:"id"`
	Name        string      `json:"name"`
	Description string      `json:"description"`
	Permissions Permissions `json:"permissions"`
}

// ChangePasswordRequest 修改密码请求
type ChangePasswordRequest struct {
	CurrentPassword string `json:"current_password" binding:"required"`
	NewPassword     string `json:"new_password" binding:"required,min=6"`
	ConfirmPassword string `json:"confirm_password" binding:"required"`
}

// RoleCreateRequest 角色创建请求
type RoleCreateRequest struct {
	Name        string      `json:"name" binding:"required,min=2,max=50"`
	Description string      `json:"description"`
	Permissions Permissions `json:"permissions"`
}

// RoleUpdateRequest 角色更新请求
type RoleUpdateRequest struct {
	Name        string      `json:"name" binding:"omitempty,min=2,max=50"`
	Description string      `json:"description"`
	Permissions Permissions `json:"permissions"`
}

// HasPermission 检查用户是否有指定权限
func (u *User) HasPermission(permission string) bool {
	for _, p := range u.Role.Permissions {
		if p == permission {
			return true
		}
	}
	return false
}

// IsActive 检查用户是否激活
func (u *User) IsActive() bool {
	return u.Status == 1
}

// ToUserInfo 转换为用户信息
func (u *User) ToUserInfo() UserInfo {
	return UserInfo{
		ID:         u.ID,
		Username:   u.Username,
		Email:      u.Email,
		RealName:   u.RealName,
		Phone:      u.Phone,
		Avatar:     u.Avatar,
		Department: u.Department,
		Status:     u.Status,
		Role: RoleInfo{
			ID:          u.Role.ID,
			Name:        u.Role.Name,
			Description: u.Role.Description,
			Permissions: u.Role.Permissions,
		},
		LastLoginAt: u.LastLoginAt,
	}
}

// UserProfileUpdateRequest 用户资料更新请求
type UserProfileUpdateRequest struct {
	Username   string `json:"username" binding:"omitempty,min=3,max=50"`
	RealName   string `json:"real_name" binding:"omitempty"`
	Email      string `json:"email" binding:"omitempty,email"`
	Phone      string `json:"phone"`
	Department string `json:"department"`
}

// SystemSettings 系统设置模型
type SystemSettings struct {
	ID        uint      `json:"id" gorm:"primarykey"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	UserID    uint      `json:"user_id" gorm:"not null;uniqueIndex"`
	Theme     string    `json:"theme" gorm:"size:20;default:'system'"` // light, dark, system
	Language  string    `json:"language" gorm:"size:10;default:'zh-CN'"`
	Timezone  string    `json:"timezone" gorm:"size:50;default:'Asia/Shanghai'"`
	DateFormat string   `json:"date_format" gorm:"size:20;default:'YYYY-MM-DD'"`
	TimeFormat string   `json:"time_format" gorm:"size:10;default:'24h'"`

	// 关联
	User User `json:"-" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (SystemSettings) TableName() string {
	return "system_settings"
}

// SystemSettingsRequest 系统设置请求
type SystemSettingsRequest struct {
	Theme      string `json:"theme" binding:"omitempty,oneof=light dark system"`
	Language   string `json:"language" binding:"omitempty,oneof=zh-CN en-US"`
	Timezone   string `json:"timezone"`
	DateFormat string `json:"date_format"`
	TimeFormat string `json:"time_format" binding:"omitempty,oneof=12h 24h"`
}

// NotificationSettings 通知设置模型
type NotificationSettings struct {
	ID        uint      `json:"id" gorm:"primarykey"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	UserID    uint      `json:"user_id" gorm:"not null;uniqueIndex"`

	// 漏洞相关通知
	VulnNewEmail       bool `json:"vuln_new_email" gorm:"default:true"`
	VulnNewPush        bool `json:"vuln_new_push" gorm:"default:true"`
	VulnNewSMS         bool `json:"vuln_new_sms" gorm:"default:false"`
	VulnAssignedEmail  bool `json:"vuln_assigned_email" gorm:"default:true"`
	VulnAssignedPush   bool `json:"vuln_assigned_push" gorm:"default:true"`
	VulnAssignedSMS    bool `json:"vuln_assigned_sms" gorm:"default:true"`
	VulnFixedEmail     bool `json:"vuln_fixed_email" gorm:"default:true"`
	VulnFixedPush      bool `json:"vuln_fixed_push" gorm:"default:false"`
	VulnFixedSMS       bool `json:"vuln_fixed_sms" gorm:"default:false"`

	// 报告相关通知
	ReportEmail bool `json:"report_email" gorm:"default:true"`
	ReportPush  bool `json:"report_push" gorm:"default:false"`
	ReportSMS   bool `json:"report_sms" gorm:"default:false"`

	// 系统相关通知
	SystemEmail bool `json:"system_email" gorm:"default:true"`
	SystemPush  bool `json:"system_push" gorm:"default:true"`
	SystemSMS   bool `json:"system_sms" gorm:"default:false"`

	// 社交相关通知
	SocialEmail bool `json:"social_email" gorm:"default:false"`
	SocialPush  bool `json:"social_push" gorm:"default:true"`
	SocialSMS   bool `json:"social_sms" gorm:"default:false"`

	// 关联
	User User `json:"-" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (NotificationSettings) TableName() string {
	return "notification_settings"
}

// NotificationSettingsRequest 通知设置请求
type NotificationSettingsRequest struct {
	VulnNewEmail       *bool `json:"vuln_new_email"`
	VulnNewPush        *bool `json:"vuln_new_push"`
	VulnNewSMS         *bool `json:"vuln_new_sms"`
	VulnAssignedEmail  *bool `json:"vuln_assigned_email"`
	VulnAssignedPush   *bool `json:"vuln_assigned_push"`
	VulnAssignedSMS    *bool `json:"vuln_assigned_sms"`
	VulnFixedEmail     *bool `json:"vuln_fixed_email"`
	VulnFixedPush      *bool `json:"vuln_fixed_push"`
	VulnFixedSMS       *bool `json:"vuln_fixed_sms"`
	ReportEmail        *bool `json:"report_email"`
	ReportPush         *bool `json:"report_push"`
	ReportSMS          *bool `json:"report_sms"`
	SystemEmail        *bool `json:"system_email"`
	SystemPush         *bool `json:"system_push"`
	SystemSMS          *bool `json:"system_sms"`
	SocialEmail        *bool `json:"social_email"`
	SocialPush         *bool `json:"social_push"`
	SocialSMS          *bool `json:"social_sms"`
}

// TwoFactorSetupResponse 双因素认证设置响应
type TwoFactorSetupResponse struct {
	QRCodeURL   string   `json:"qr_code_url"`
	Secret      string   `json:"secret"`
	BackupCodes []string `json:"backup_codes"`
}

// TwoFactorVerifyRequest 双因素认证验证请求
type TwoFactorVerifyRequest struct {
	Code string `json:"code" binding:"required,len=6"`
}

// TwoFactorStatusResponse 双因素认证状态响应
type TwoFactorStatusResponse struct {
	Enabled bool `json:"enabled"`
}
