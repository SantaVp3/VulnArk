package service

import (
	"errors"
	"fmt"
	"net/url"
	"reflect"

	"vulnark/internal/model"
	"vulnark/internal/repository"
	"vulnark/pkg/utils"
)

// SettingsService 设置服务接口
type SettingsService interface {
	// 用户资料
	UpdateProfile(userID uint, req *model.UserProfileUpdateRequest) error
	ChangePassword(userID uint, req *model.ChangePasswordRequest) error

	// 双因素认证
	Setup2FA(userID uint) (*model.TwoFactorSetupResponse, error)
	Verify2FA(userID uint, code string) error
	Disable2FA(userID uint) error
	Get2FAStatus(userID uint) (*model.TwoFactorStatusResponse, error)

	// 系统设置
	GetSystemSettings(userID uint) (*model.SystemSettings, error)
	UpdateSystemSettings(userID uint, req *model.SystemSettingsRequest) error

	// 通知设置
	GetNotificationSettings(userID uint) (*model.NotificationSettings, error)
	UpdateNotificationSettings(userID uint, req *model.NotificationSettingsRequest) error

	// 数据管理
	ExportUserData(userID uint, dataType string) ([]byte, error)
	ImportUserData(userID uint, data []byte, dataType string) error
	ClearCache(userID uint) error
	OptimizeDatabase(userID uint) error
}

// settingsService 设置服务实现
type settingsService struct {
	userRepo     repository.UserRepository
	settingsRepo repository.SettingsRepository
}

// NewSettingsService 创建设置服务
func NewSettingsService(userRepo repository.UserRepository, settingsRepo repository.SettingsRepository) SettingsService {
	return &settingsService{
		userRepo:     userRepo,
		settingsRepo: settingsRepo,
	}
}

// UpdateProfile 更新用户资料
func (s *settingsService) UpdateProfile(userID uint, req *model.UserProfileUpdateRequest) error {
	// 获取用户
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return errors.New("用户不存在")
	}

	// 检查用户名是否已存在（如果要更新用户名）
	if req.Username != "" && req.Username != user.Username {
		existingUser, _ := s.userRepo.GetByUsername(req.Username)
		if existingUser != nil && existingUser.ID != userID {
			return errors.New("用户名已存在")
		}
	}

	// 检查邮箱是否已存在（如果要更新邮箱）
	if req.Email != "" && req.Email != user.Email {
		existingUser, _ := s.userRepo.GetByEmail(req.Email)
		if existingUser != nil && existingUser.ID != userID {
			return errors.New("邮箱已存在")
		}
	}

	// 更新用户信息
	updateData := &model.UserUpdateRequest{
		Email:      req.Email,
		RealName:   req.RealName,
		Phone:      req.Phone,
		Department: req.Department,
	}

	return s.userRepo.Update(userID, updateData)
}

// ChangePassword 修改密码
func (s *settingsService) ChangePassword(userID uint, req *model.ChangePasswordRequest) error {
	// 验证确认密码
	if req.NewPassword != req.ConfirmPassword {
		return errors.New("确认密码不匹配")
	}

	// 获取用户
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return errors.New("用户不存在")
	}

	// 验证当前密码
	if !utils.CheckPasswordHash(req.CurrentPassword, user.PasswordHash) {
		return errors.New("当前密码错误")
	}

	// 生成新密码哈希
	newPasswordHash, err := utils.HashPassword(req.NewPassword)
	if err != nil {
		return errors.New("密码加密失败")
	}

	// 更新密码
	return s.userRepo.UpdatePassword(userID, newPasswordHash)
}

// GetSystemSettings 获取系统设置
func (s *settingsService) GetSystemSettings(userID uint) (*model.SystemSettings, error) {
	return s.settingsRepo.GetSystemSettings(userID)
}

// UpdateSystemSettings 更新系统设置
func (s *settingsService) UpdateSystemSettings(userID uint, req *model.SystemSettingsRequest) error {
	// 获取当前设置
	currentSettings, err := s.settingsRepo.GetSystemSettings(userID)
	if err != nil {
		return err
	}

	// 更新非空字段
	if req.Theme != "" {
		currentSettings.Theme = req.Theme
	}
	if req.Language != "" {
		currentSettings.Language = req.Language
	}
	if req.Timezone != "" {
		currentSettings.Timezone = req.Timezone
	}
	if req.DateFormat != "" {
		currentSettings.DateFormat = req.DateFormat
	}
	if req.TimeFormat != "" {
		currentSettings.TimeFormat = req.TimeFormat
	}

	return s.settingsRepo.UpdateSystemSettings(userID, currentSettings)
}

// GetNotificationSettings 获取通知设置
func (s *settingsService) GetNotificationSettings(userID uint) (*model.NotificationSettings, error) {
	return s.settingsRepo.GetNotificationSettings(userID)
}

// UpdateNotificationSettings 更新通知设置
func (s *settingsService) UpdateNotificationSettings(userID uint, req *model.NotificationSettingsRequest) error {
	// 获取当前设置
	currentSettings, err := s.settingsRepo.GetNotificationSettings(userID)
	if err != nil {
		return err
	}

	// 使用反射更新非nil字段
	reqValue := reflect.ValueOf(req).Elem()
	settingsValue := reflect.ValueOf(currentSettings).Elem()
	reqType := reflect.TypeOf(req).Elem()

	for i := 0; i < reqValue.NumField(); i++ {
		field := reqValue.Field(i)
		fieldType := reqType.Field(i)
		
		// 跳过nil指针
		if field.IsNil() {
			continue
		}
		
		// 获取对应的设置字段
		settingsField := settingsValue.FieldByName(fieldType.Name)
		if settingsField.IsValid() && settingsField.CanSet() {
			settingsField.SetBool(field.Elem().Bool())
		}
	}

	return s.settingsRepo.UpdateNotificationSettings(userID, currentSettings)
}

// ExportUserData 导出用户数据
func (s *settingsService) ExportUserData(userID uint, dataType string) ([]byte, error) {
	// TODO: 实现数据导出逻辑
	// 这里可以根据dataType导出不同类型的数据
	// 例如：assets, vulnerabilities, reports, all
	
	switch dataType {
	case "profile":
		// 导出用户资料
		user, err := s.userRepo.GetByID(userID)
		if err != nil {
			return nil, err
		}
		return utils.ToJSON(user)
	case "settings":
		// 导出设置数据
		systemSettings, _ := s.settingsRepo.GetSystemSettings(userID)
		notificationSettings, _ := s.settingsRepo.GetNotificationSettings(userID)
		
		data := map[string]interface{}{
			"system_settings":       systemSettings,
			"notification_settings": notificationSettings,
		}
		return utils.ToJSON(data)
	default:
		return nil, errors.New("不支持的数据类型")
	}
}

// ImportUserData 导入用户数据
func (s *settingsService) ImportUserData(userID uint, data []byte, dataType string) error {
	// TODO: 实现数据导入逻辑
	// 这里需要解析数据并更新相应的设置
	return errors.New("功能开发中")
}

// ClearCache 清理缓存
func (s *settingsService) ClearCache(userID uint) error {
	// TODO: 实现缓存清理逻辑
	// 这里可以清理用户相关的缓存数据
	return nil
}

// OptimizeDatabase 优化数据库
func (s *settingsService) OptimizeDatabase(userID uint) error {
	// TODO: 实现数据库优化逻辑
	// 这里可以执行一些数据库优化操作
	// 注意：这个操作通常需要管理员权限
	return nil
}

// Setup2FA 设置双因素认证
func (s *settingsService) Setup2FA(userID uint) (*model.TwoFactorSetupResponse, error) {
	// 获取用户信息
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return nil, errors.New("用户不存在")
	}

	// 生成TOTP密钥
	secret, err := utils.GenerateRandomString(32)
	if err != nil {
		return nil, err
	}

	// 构建TOTP URL
	totpURL := fmt.Sprintf("otpauth://totp/VulnArk:%s?secret=%s&issuer=VulnArk",
		user.Email, secret)

	// 生成QR码URL (使用URL编码)
	qrCodeURL := "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" +
		url.QueryEscape(totpURL)

	// 生成备用恢复码
	backupCodes := make([]string, 8)
	for i := range backupCodes {
		code, err := utils.GenerateRandomString(8)
		if err != nil {
			return nil, err
		}
		backupCodes[i] = code
	}

	return &model.TwoFactorSetupResponse{
		QRCodeURL:   qrCodeURL,
		Secret:      secret,
		BackupCodes: backupCodes,
	}, nil
}

// Verify2FA 验证双因素认证
func (s *settingsService) Verify2FA(userID uint, code string) error {
	// TODO: 实现2FA验证逻辑
	// 1. 从临时存储中获取用户的TOTP密钥
	// 2. 验证提供的代码
	// 3. 如果验证成功，将2FA信息保存到用户表
	// 4. 清理临时存储

	// 模拟验证成功
	if len(code) == 6 {
		// 更新用户的2FA状态
		return s.userRepo.Update2FA(userID, true, "")
	}

	return errors.New("验证码无效")
}

// Disable2FA 禁用双因素认证
func (s *settingsService) Disable2FA(userID uint) error {
	// 更新用户的2FA状态
	return s.userRepo.Update2FA(userID, false, "")
}

// Get2FAStatus 获取双因素认证状态
func (s *settingsService) Get2FAStatus(userID uint) (*model.TwoFactorStatusResponse, error) {
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return nil, err
	}

	return &model.TwoFactorStatusResponse{
		Enabled: user.TwoFactorEnabled,
	}, nil
}
