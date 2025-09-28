package service

import (
	"fmt"
	"log"
	"os"
	"strings"

	"vulnark/internal/repository"
	"vulnark/pkg/database"
)

	CreateDefaultAdminUser() (string, error)
	ResetAdminPassword() (string, error)
>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
}
=======
// InitService 初始化服务接口
type InitService interface {
	InitializeSystem() error
	CreateDefaultAdminUser() (string, error)
	ResetAdminPassword() (string, error)
}
=======
	CreateDefaultAdminUser() (string, error)
	ResetAdminPassword() (string, error)
>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
}

// initService 初始化服务实现
type initService struct {
	userRepo repository.UserRepository
}

// NewInitService 创建初始化服务
func NewInitService(userRepo repository.UserRepository) InitService {
	return &initService{
		userRepo: userRepo,
	}
}

// InitializeSystem 初始化系统
func (s *initService) InitializeSystem() error {
	log.Println("🚀 开始初始化VulnArk系统...")

	// 检查是否为首次部署
	isFirstDeploy := s.isFirstTimeDeployment()

	// 2. 执行种子数据
	if err := s.migrationService.RunSeeds(); err != nil {
		log.Printf("⚠️  种子数据执行失败: %v", err)
		// 不中断初始化过程，继续执行
	}

	// 3. 检查是否需要重置管理员密码或创建管理员用户
>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
	adminUser, err := s.userRepo.GetByUsername("admin")
=======
	// 使用新的迁移管理器执行数据库迁移和初始化
	migrationManager := database.NewMigrationManager()
	if err := migrationManager.RunMigrations(); err != nil {
		return fmt.Errorf("系统初始化失败: %v", err)
	}

	// 检查管理员用户并显示登录信息
	adminUser, err := s.userRepo.GetByUsername("admin")
=======
	// 2. 执行种子数据
	if err := s.migrationService.RunSeeds(); err != nil {
		log.Printf("⚠️  种子数据执行失败: %v", err)
		// 不中断初始化过程，继续执行
	}

	// 3. 检查是否需要重置管理员密码或创建管理员用户
>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
	adminUser, err := s.userRepo.GetByUsername("admin")
	var password string

	if err == nil && adminUser != nil {
		// 管理员用户已存在，重置为随机密码
		password, err = s.ResetAdminPassword()
		if err != nil {
			return fmt.Errorf("重置管理员密码失败: %v", err)
		}

		log.Println("📋 管理员账号信息（密码已重置）：")
		log.Printf("   用户名: admin")
		log.Printf("   密码: %s", password)
		log.Printf("   邮箱: %s", adminUser.Email)
		log.Printf("   真实姓名: %s", adminUser.RealName)
		log.Println("========================================")
		log.Println("⚠️  请妥善保存管理员密码，首次登录后建议修改！")
		log.Println("🌐 访问地址: http://localhost:8080")
		log.Println("========================================")
	} else {
		// 创建默认管理员用户
		password, err = s.CreateDefaultAdminUser()
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
	}

>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
=======
		log.Println("========================================")
		log.Println("🎉 VulnArk 系统已就绪！")
		log.Println("========================================")
		log.Println("📋 管理员账号信息：")
		log.Printf("   👤 用户名: admin")
		log.Printf("   📧 邮  箱: %s", adminUser.Email)
		log.Printf("   👨‍💼 真实姓名: %s", adminUser.RealName)
		
		// 如果是首次部署或存在凭证文件，显示密码信息
		if isFirstDeploy {
			password := s.getAdminPasswordFromFile()
			if password != "" {
				log.Printf("   🔑 密  码: %s", password)
				log.Println("========================================")
				log.Println("⚠️  重要提醒:")
				log.Println("   • 这是首次部署，请立即登录并修改默认密码")
				log.Println("   • 请妥善保管管理员账号信息")
				log.Println("   • 建议启用双因素认证增强安全性")
				log.Println("   • 登录后请删除 admin_credentials.txt 文件")
			} else {
				log.Println("   🔑 密  码: [请查看 admin_credentials.txt 文件]")
			}
		} else {
			log.Println("   🔑 密  码: [请使用您设置的密码登录]")
		}
		
		log.Println("========================================")
		log.Println("🌐 访问地址:")
		log.Println("   • http://localhost:8080")
		log.Println("   • http://127.0.0.1:8080")
		log.Println("========================================")
	} else {
		// 创建默认管理员用户
		password, err = s.CreateDefaultAdminUser()
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
	}

=======
		log.Println("📋 管理员账号信息（密码已重置）：")
		log.Printf("   用户名: admin")
		log.Printf("   密码: %s", password)
		log.Printf("   邮箱: %s", adminUser.Email)
		log.Printf("   真实姓名: %s", adminUser.RealName)
		log.Println("========================================")
		log.Println("⚠️  请妥善保存管理员密码，首次登录后建议修改！")
		log.Println("🌐 访问地址: http://localhost:8080")
		log.Println("========================================")
	} else {
		// 创建默认管理员用户
		password, err = s.CreateDefaultAdminUser()
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
	}

>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
	return nil
}

// isFirstTimeDeployment 检查是否为首次部署
func (s *initService) isFirstTimeDeployment() bool {
	// 检查是否存在管理员凭证文件
	if _, err := os.Stat("admin_credentials.txt"); err == nil {
		return true
	}
	
	// 检查数据库中是否只有一个管理员用户（刚创建的）
	var userCount int64
	if db := database.GetDB(); db != nil {
		db.Table("users").Count(&userCount)
		return userCount <= 1
	}
<<<<<<< HEAD
	
	return false
}

// getAdminPasswordFromFile 从凭证文件中读取管理员密码
func (s *initService) getAdminPasswordFromFile() string {
	credentialsFile := "admin_credentials.txt"
	
	// 检查文件是否存在
	if _, err := os.Stat(credentialsFile); os.IsNotExist(err) {
		return ""
	}
	
	// 读取文件内容
	content, err := os.ReadFile(credentialsFile)
	if err != nil {
		log.Printf("⚠️  读取凭证文件失败: %v", err)
		return ""
	}
	
	// 解析密码
	lines := strings.Split(string(content), "\n")
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if strings.HasPrefix(line, "密码:") || strings.HasPrefix(line, "密码：") {
			parts := strings.SplitN(line, ":", 2)
			if len(parts) == 2 {
				return strings.TrimSpace(parts[1])
			}
		}
	}
	
	return ""
}

// CleanupCredentialsFile 清理凭证文件（可选的辅助方法）
func (s *initService) CleanupCredentialsFile() error {
	credentialsFile := "admin_credentials.txt"
	if _, err := os.Stat(credentialsFile); err == nil {
		if err := os.Remove(credentialsFile); err != nil {
			return fmt.Errorf("删除凭证文件失败: %v", err)
		}
		log.Println("✅ 已清理管理员凭证文件")
	}
	return nil
=======

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

	// 创建欢迎通知
	s.createWelcomeNotification(adminUser.ID)

	log.Printf("✅ 默认管理员用户创建成功 (ID: %d)", adminUser.ID)
	return password, nil
>>>>>>> 9fc845faa32a3eb384f5dbb9db2d2f34a8f6a3ac
}

// ResetAdminPassword 重置管理员密码
func (s *initService) ResetAdminPassword() (string, error) {
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

	// 获取管理员用户
	adminUser, err := s.userRepo.GetByUsername("admin")
	if err != nil {
		return "", fmt.Errorf("获取管理员用户失败: %v", err)
	}

	// 更新密码
	if err := s.userRepo.UpdatePassword(adminUser.ID, hashedPassword); err != nil {
		return "", fmt.Errorf("更新管理员密码失败: %v", err)
	}

	log.Printf("✅ 管理员密码已重置")
	return password, nil
}

// createWelcomeNotification 创建欢迎通知
func (s *initService) createWelcomeNotification(userID uint) {
	// 这里可以添加通知创建逻辑，暂时跳过以避免循环依赖
	// 可以在后续版本中通过数据库直接插入或使用通知服务
	log.Printf("✅ 欢迎通知已准备就绪")
}
