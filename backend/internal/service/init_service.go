package service

import (
	"fmt"
	"log"

	"vulnark/internal/model"
	"vulnark/internal/repository"
	"vulnark/pkg/utils"
)

// InitService 初始化服务接口
type InitService interface {
	InitializeSystem() error
	CreateDefaultAdminUser() (string, error)
}

// initService 初始化服务实现
type initService struct {
	userRepo        repository.UserRepository
	roleRepo        repository.RoleRepository
	migrationService MigrationService
}

// NewInitService 创建初始化服务
func NewInitService() InitService {
	return &initService{
		userRepo:         repository.NewUserRepository(),
		roleRepo:         repository.NewRoleRepository(),
		migrationService: NewMigrationService(),
	}
}

// InitializeSystem 初始化系统
func (s *initService) InitializeSystem() error {
	log.Println("开始初始化系统...")

	// 1. 执行数据库迁移
	if err := s.migrationService.RunMigrations(); err != nil {
		log.Printf("⚠️  数据库迁移失败: %v", err)
		// 不中断初始化过程，继续执行
	}

	// 2. 执行种子数据
	if err := s.migrationService.RunSeeds(); err != nil {
		log.Printf("⚠️  种子数据执行失败: %v", err)
		// 不中断初始化过程，继续执行
	}

	// 3. 检查是否已有管理员用户
	adminUser, err := s.userRepo.GetByUsername("admin")
	if err == nil && adminUser != nil {
		log.Println("========================================")
		log.Println("🎉 VulnArk 系统已就绪！")
		log.Println("========================================")
		log.Println("📋 管理员账号信息：")
		log.Printf("   用户名: admin")
		log.Printf("   邮箱: %s", adminUser.Email)
		log.Printf("   真实姓名: %s", adminUser.RealName)
		log.Println("   密码: [已设置，请使用现有密码登录]")
		log.Println("========================================")
		log.Println("🌐 访问地址: http://localhost:8080")
		log.Println("========================================")
		return nil
	}

	// 创建默认管理员用户
	password, err := s.CreateDefaultAdminUser()
	if err != nil {
		return fmt.Errorf("创建默认管理员用户失败: %v", err)
	}

	// 输出管理员账号信息
	log.Println("========================================")
	log.Println("🎉 VulnArk 系统初始化完成！")
	log.Println("========================================")
	log.Println("📋 默认管理员账号信息：")
	log.Printf("   用户名: admin")
	log.Printf("   密码: %s", password)
	log.Printf("   邮箱: admin@vulnark.com")
	log.Println("========================================")
	log.Println("⚠️  请妥善保存管理员密码，首次登录后建议修改！")
	log.Println("🌐 访问地址: http://localhost:8080")
	log.Println("========================================")

	return nil
}

// CreateDefaultAdminUser 创建默认管理员用户
func (s *initService) CreateDefaultAdminUser() (string, error) {
	// 生成随机密码
	password, err := utils.GenerateSecurePassword(16)
	if err != nil {
		return "", fmt.Errorf("生成随机密码失败: %v", err)
	}

	// 加密密码
	hashedPassword, err := utils.HashPassword(password)
	if err != nil {
		return "", fmt.Errorf("密码加密失败: %v", err)
	}

	// 确保管理员角色存在
	adminRole, err := s.roleRepo.GetByID(1)
	if err != nil {
		// 如果角色不存在，创建默认管理员角色
		adminRole = &model.Role{
			ID:          1,
			Name:        "管理员",
			Description: "系统管理员，拥有所有权限",
			Permissions: []string{"*"}, // 所有权限
		}
		if err := s.roleRepo.Create(adminRole); err != nil {
			return "", fmt.Errorf("创建管理员角色失败: %v", err)
		}
	}

	// 创建管理员用户
	adminUser := &model.User{
		Username:     "admin",
		Email:        "admin@vulnark.com",
		PasswordHash: hashedPassword,
		RealName:     "系统管理员",
		Phone:        "",
		RoleID:       1,
		Department:   "IT部门",
		Status:       1, // 启用状态
	}

	if err := s.userRepo.Create(adminUser); err != nil {
		return "", fmt.Errorf("创建管理员用户失败: %v", err)
	}

	log.Printf("✅ 默认管理员用户创建成功 (ID: %d)", adminUser.ID)
	return password, nil
}
