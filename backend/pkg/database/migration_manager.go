package database

import (
	"fmt"
	"log"
	"os"
	"reflect"
	"strings"
	"time"
	"unicode"
	"vulnark/internal/model"
	"vulnark/pkg/utils"
)

// MigrationManager 数据库迁移管理器
type MigrationManager struct{}

// NewMigrationManager 创建迁移管理器
func NewMigrationManager() *MigrationManager {
	return &MigrationManager{}
}

// RunMigrations 执行所有数据库迁移
func (m *MigrationManager) RunMigrations() error {
	log.Println("🔄 开始执行数据库迁移...")

	// 定义迁移步骤
	migrations := []MigrationStep{
		{Name: "预处理数据库约束", Function: m.preProcessConstraints},
		{Name: "基础表迁移", Function: m.migrateBasicTables},
		{Name: "业务表迁移", Function: m.migrateBusinessTables},
		{Name: "系统表迁移", Function: m.migrateSystemTables},
		{Name: "创建唯一约束", Function: m.createUniqueConstraints},
		{Name: "初始数据插入", Function: m.seedInitialData},
	}

	// 执行迁移
	for _, migration := range migrations {
		log.Printf("📋 执行: %s", migration.Name)
		if err := migration.Function(); err != nil {
			log.Printf("❌ %s 失败: %v", migration.Name, err)
			return fmt.Errorf("迁移失败: %s - %v", migration.Name, err)
		}
		log.Printf("✅ %s 完成", migration.Name)
	}

	log.Println("🎉 数据库迁移完成!")
	return nil
}

// MigrationStep 迁移步骤
type MigrationStep struct {
	Name     string
	Function func() error
}

// preProcessConstraints 预处理数据库约束问题
func (m *MigrationManager) preProcessConstraints() error {
	// 需要处理约束问题的表列表
	tables := []string{"roles", "users"}

	for _, tableName := range tables {
		// 检查表是否存在
		var tableExists int64
		DB.Raw("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?", tableName).Scan(&tableExists)

		if tableExists > 0 {
			log.Printf("  🔧 清理 %s 表的可能冲突约束...", tableName)

			// 查询所有与表相关的索引
			var indexes []struct {
				KeyName   string `gorm:"column:Key_name"`
				IndexType string `gorm:"column:Index_type"`
			}
			DB.Raw("SHOW INDEX FROM " + tableName).Scan(&indexes)

			log.Printf("  📋 %s表发现 %d 个索引:", tableName, len(indexes))
			for _, index := range indexes {
				log.Printf("    - %s (类型: %s)", index.KeyName, index.IndexType)
			}

			// 删除可能冲突的旧约束
			constraintsToRemove := []string{
				"uni_roles_name",
				"uni_users_username",
				"uni_users_email",
			}

			for _, constraintName := range constraintsToRemove {
				// 检查约束是否存在并删除
				for _, index := range indexes {
					if index.KeyName == constraintName {
						log.Printf("  🗑️  删除%s表的索引: %s", tableName, constraintName)
						if err := DB.Exec("ALTER TABLE `" + tableName + "` DROP INDEX `" + constraintName + "`").Error; err != nil {
							log.Printf("  ⚠️  删除索引失败: %v", err)
						} else {
							log.Printf("  ✓ 成功删除索引: %s", constraintName)
						}
						break
					}
				}
			}

			// 查询所有外键约束
			var foreignKeys []struct {
				ConstraintName string `json:"constraint_name"`
			}
			DB.Raw("SELECT constraint_name FROM information_schema.referential_constraints WHERE constraint_schema = DATABASE() AND table_name = ?", tableName).Scan(&foreignKeys)

			log.Printf("  📋 %s表发现 %d 个外键约束:", tableName, len(foreignKeys))
			for _, fk := range foreignKeys {
				log.Printf("    - %s", fk.ConstraintName)
			}
		}
	}

	return nil
}

// migrateBasicTables 迁移基础表
func (m *MigrationManager) migrateBasicTables() error {
	tables := []interface{}{
		&model.Role{},
		&model.User{},
		&model.SystemSettings{},
		&model.NotificationSettings{},
	}

	return m.migrateTables(tables)
}

// migrateBusinessTables 迁移业务表
func (m *MigrationManager) migrateBusinessTables() error {
	tables := []interface{}{
		// 资产管理
		&model.Asset{},

		// 漏洞管理 - 按依赖顺序排列
		&model.OWASPCategory{},
		&model.Vulnerability{},
		&model.VulnerabilityAttachment{},
		&model.VulnerabilityAssignment{},
		&model.VulnerabilityTimeline{},
		&model.AssignmentRule{},

		// 知识库
		&model.KnowledgeBase{},
		&model.Tag{},
		&model.Template{},

		// 报告管理
		&model.Report{},
		&model.ReportComment{},
		&model.FileAttachment{},

		// 通知系统
		&model.Notification{},
		&model.NotificationSetting{},
		&model.EmailTemplate{},
		&model.EmailLog{},
	}

	return m.migrateTables(tables)
}

// migrateSystemTables 迁移系统表
func (m *MigrationManager) migrateSystemTables() error {
	tables := []interface{}{
		// 权限系统
		&model.Permission{},
		&model.RolePermission{},

		// 系统配置
		&model.SystemConfig{},
		&model.AuditLog{},

		// AI模块
		&model.AIConfiguration{},
		&model.AIProvider{},
		&model.AIConversation{},
		&model.AIMessage{},
	}

	return m.migrateTables(tables)
}

// migrateTables 批量迁移表
func (m *MigrationManager) migrateTables(tables []interface{}) error {
	for _, table := range tables {
		tableName := getModelTableName(table)
		log.Printf("  📊 迁移表: %s", tableName)

		// 检查表是否已存在
		var tableExists int64
		DB.Raw("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?", tableName).Scan(&tableExists)
		
		if tableExists > 0 {
			log.Printf("  ✓ 表 %s 已存在，跳过创建", tableName)
		} else {
			// 对于 vulnerabilities 表，使用手动创建以确保正确性
			if tableName == "vulnerabilities" {
				if err := m.createVulnerabilitiesTable(); err != nil {
					log.Printf("  ❌ 手动创建 vulnerabilities 表失败: %v", err)
					return fmt.Errorf("创建 vulnerabilities 表失败: %w", err)
				}
				log.Printf("  ✅ 表 %s 手动创建完成", tableName)
				continue
			}
			
			// 对于 vulnerability_attachments 表，使用手动创建
			if tableName == "vulnerability_attachments" {
				if err := m.createVulnerabilityAttachmentsTable(); err != nil {
					log.Printf("  ❌ 手动创建 vulnerability_attachments 表失败: %v", err)
					return fmt.Errorf("创建 vulnerability_attachments 表失败: %w", err)
				}
				log.Printf("  ✅ 表 %s 手动创建完成", tableName)
				continue
			}
		}

		// 使用 GORM AutoMigrate 处理其他表
		if err := DB.AutoMigrate(table); err != nil {
			// 检查是否是预期的约束删除错误
			if strings.Contains(err.Error(), "Can't DROP") &&
			   strings.Contains(err.Error(), "check that column/key exists") {
				log.Printf("  ⚠️  忽略预期的约束错误: %v", err)
				log.Printf("  ✅ 表 %s 迁移完成（忽略约束错误）", tableName)
				continue
			}
			log.Printf("  ❌ 表 %s 迁移失败: %v", tableName, err)
			return fmt.Errorf("迁移表 %s 失败: %w", tableName, err)
		}
		
		if tableExists == 0 {
			log.Printf("  ✅ 表 %s 迁移完成", tableName)
		}
	}
	return nil
}

// createVulnerabilitiesTable 手动创建 vulnerabilities 表
func (m *MigrationManager) createVulnerabilitiesTable() error {
	sql := `CREATE TABLE vulnerabilities (
		id bigint unsigned NOT NULL AUTO_INCREMENT,
		created_at datetime(3) DEFAULT NULL,
		updated_at datetime(3) DEFAULT NULL,
		title varchar(255) NOT NULL,
		description text,
		cve_id varchar(50) DEFAULT NULL,
		cnvd_id varchar(50) DEFAULT NULL,
		owasp_category_id bigint unsigned DEFAULT NULL,
		severity_level int NOT NULL,
		cvss_score decimal(3,1) DEFAULT NULL,
		asset_id bigint unsigned NOT NULL,
		discoverer_id bigint unsigned DEFAULT NULL,
		assignee_id bigint unsigned DEFAULT NULL,
		status varchar(20) DEFAULT 'new',
		impact_scope text,
		reproduction_steps text,
		fix_suggestion text,
		discovered_at datetime DEFAULT CURRENT_TIMESTAMP,
		assigned_at datetime(3) DEFAULT NULL,
		fixed_at datetime(3) DEFAULT NULL,
		verified_at datetime(3) DEFAULT NULL,
		closed_at datetime(3) DEFAULT NULL,
		PRIMARY KEY (id),
		KEY idx_vulnerabilities_asset_id (asset_id),
		KEY idx_vulnerabilities_assignee_id (assignee_id),
		KEY idx_vulnerabilities_discoverer_id (discoverer_id),
		KEY idx_vulnerabilities_owasp_category_id (owasp_category_id),
		KEY idx_vulnerabilities_status (status),
		KEY idx_vulnerabilities_severity_level (severity_level)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci`
	
	return DB.Exec(sql).Error
}

// createVulnerabilityAttachmentsTable 手动创建 vulnerability_attachments 表
func (m *MigrationManager) createVulnerabilityAttachmentsTable() error {
	sql := `CREATE TABLE vulnerability_attachments (
		id bigint unsigned NOT NULL AUTO_INCREMENT,
		created_at datetime(3) DEFAULT NULL,
		vulnerability_id bigint unsigned NOT NULL,
		filename varchar(255) NOT NULL,
		original_name varchar(255) NOT NULL,
		file_path varchar(500) NOT NULL,
		file_size bigint NOT NULL,
		file_type varchar(100) DEFAULT NULL,
		uploader_id bigint unsigned NOT NULL,
		PRIMARY KEY (id),
		KEY idx_vulnerability_attachments_vulnerability_id (vulnerability_id),
		KEY idx_vulnerability_attachments_uploader_id (uploader_id)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci`
	
	return DB.Exec(sql).Error
}

// getModelTableName 获取模型表名
func getModelTableName(model interface{}) string {
	// 尝试调用 TableName 方法
	if tabler, ok := model.(interface{ TableName() string }); ok {
		return tabler.TableName()
	}
	
	// 如果没有 TableName 方法，使用反射获取类型名并转换为蛇形命名
	t := reflect.TypeOf(model)
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}
	
	// 将驼峰命名转换为蛇形命名
	name := t.Name()
	var result []rune
	for i, r := range name {
		if i > 0 && unicode.IsUpper(r) {
			result = append(result, '_')
		}
		result = append(result, unicode.ToLower(r))
	}
	return string(result)
}

// createUniqueConstraints 创建唯一约束
func (m *MigrationManager) createUniqueConstraints() error {
	log.Println("  🔧 创建必要的唯一约束...")

	constraints := []struct {
		Table      string
		Column     string
		IndexName  string
		Constraint string
	}{
		{"roles", "name", "idx_role_name", "UNIQUE INDEX `idx_role_name` (`name`)"},
		{"users", "username", "idx_user_username", "UNIQUE INDEX `idx_user_username` (`username`)"},
		{"users", "email", "idx_user_email", "UNIQUE INDEX `idx_user_email` (`email`)"},
	}

	for _, constraint := range constraints {
		// 检查约束是否已存在
		var indexExists int64
		DB.Raw("SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?", constraint.Table, constraint.IndexName).Scan(&indexExists)

		if indexExists == 0 {
			log.Printf("  📝 创建 %s.%s 的唯一约束", constraint.Table, constraint.Column)
			if err := DB.Exec("ALTER TABLE `" + constraint.Table + "` ADD " + constraint.Constraint).Error; err != nil {
				log.Printf("  ⚠️  创建约束失败: %v", err)
				return fmt.Errorf("创建%s表%s列的唯一约束失败: %w", constraint.Table, constraint.Column, err)
			}
			log.Printf("  ✅ 成功创建 %s", constraint.IndexName)
		} else {
			log.Printf("  ✓ %s.%s 唯一约束已存在", constraint.Table, constraint.Column)
		}
	}

	return nil
}

// seedInitialData 插入初始数据
func (m *MigrationManager) seedInitialData() error {
	// 初始化默认权限
	if err := m.initializePermissions(); err != nil {
		return err
	}

	// 检查是否已有角色数据
	var roleCount int64
	DB.Model(&model.Role{}).Count(&roleCount)

	if roleCount == 0 {
		log.Println("  🌱 创建默认角色...")
		if err := m.createDefaultRoles(); err != nil {
			return err
		}
	}

	// 检查是否已有管理员用户
	var adminCount int64
	DB.Model(&model.User{}).Where("username = ?", "admin").Count(&adminCount)

	if adminCount == 0 {
		log.Println("  👤 创建默认管理员...")
		if err := m.createDefaultAdmin(); err != nil {
			return err
		}
	}

	// 初始化系统配置
	if err := m.initializeSystemConfigs(); err != nil {
		return err
	}

	return nil
}

// createDefaultRoles 创建默认角色
func (m *MigrationManager) createDefaultRoles() error {
	roles := []*model.Role{
		{
			Name:        "管理员",
			Description: "系统管理员，拥有所有权限",
			Permissions: []string{"*"}, // 所有权限
		},
		{
			Name:        "开发工程师",
			Description: "开发工程师，可以管理漏洞和资产",
			Permissions: []string{"vuln:*", "asset:*", "report:read", "knowledge:*"},
		},
		{
			Name:        "普通用户",
			Description: "普通用户，只能查看和提交漏洞",
			Permissions: []string{"vuln:read", "vuln:create", "asset:read", "report:read", "knowledge:read"},
		},
	}

	for _, role := range roles {
		if err := DB.Create(role).Error; err != nil {
			return fmt.Errorf("创建角色 %s 失败: %w", role.Name, err)
		}
		log.Printf("    ✓ 角色: %s", role.Name)
	}

	return nil
}

// createDefaultAdmin 创建默认管理员
func (m *MigrationManager) createDefaultAdmin() error {
	// 获取管理员角色
	var adminRole model.Role
	if err := DB.Where("name = ?", "管理员").First(&adminRole).Error; err != nil {
		return fmt.Errorf("获取管理员角色失败: %w", err)
	}

	// 生成随机密码
	password, err := utils.GenerateSecurePassword(16)
	if err != nil {
		// 如果生成失败，使用备用密码
		randomStr, _ := utils.GenerateRandomString(8)
		password = "VulnArk" + randomStr
	}

	hashedPassword, err := utils.HashPassword(password)
	if err != nil {
		return fmt.Errorf("密码加密失败: %w", err)
	}

	admin := &model.User{
		Username:     "admin",
		Email:        "admin@vulnark.local",
		PasswordHash: hashedPassword,
		RealName:     "系统管理员",
		RoleID:       adminRole.ID,
		Department:   "系统管理",
		Status:       1,
	}

	if err := DB.Create(admin).Error; err != nil {
		return fmt.Errorf("创建管理员失败: %w", err)
	}

	log.Println("========================================")
	log.Println("🎉 VulnArk 系统已就绪！")
	log.Println("========================================")
	log.Printf("📋 管理员账号信息：")
	log.Printf("   👤 用户名: admin")
	log.Printf("   📧 邮  箱: admin@vulnark.local")
	log.Printf("   👨‍💼 真实姓名: 系统管理员")
	log.Printf("   🔑 密  码: %s", password)
	log.Println("========================================")
	log.Println("⚠️  重要提醒:")
	log.Println("   • 这是首次部署，请立即登录并修改默认密码")
	log.Println("   • 请妥善保管管理员账号信息")
	log.Println("   • 建议启用双因素认证增强安全性")
	log.Println("   • 登录后请删除 admin_credentials.txt 文件")
	log.Println("========================================")
	log.Println("🌐 访问地址:")
	log.Println("   • http://localhost:8080")
	log.Println("   • http://127.0.0.1:8080")
	log.Println("========================================")

	// 同时保存密码信息到临时文件，方便用户查看
	if err := m.saveAdminCredentials("admin", password); err != nil {
		log.Printf("⚠️  保存管理员凭证到文件失败: %v", err)
	}

	return nil
}

// saveAdminCredentials 保存管理员凭证到文件
func (m *MigrationManager) saveAdminCredentials(username, password string) error {
	// 创建凭证文件内容
	content := fmt.Sprintf(`VulnArk 系统管理员凭证
===============================
生成时间: %s
用户名: %s
密码: %s
邮箱: admin@vulnark.local
===============================
安全提醒:
1. 请立即登录系统并修改默认密码
2. 删除此凭证文件以确保安全
3. 建议启用双因素认证
===============================
`,
		time.Now().Format("2006-01-02 15:04:05"),
		username,
		password,
	)

	// 创建凭证文件（在项目根目录）
	filename := "admin_credentials.txt"
	if err := os.WriteFile(filename, []byte(content), 0600); err != nil {
		return err
	}

	log.Printf("📄 管理员凭证已保存到: %s", filename)
	log.Printf("🔒 文件权限已设置为仅当前用户可读")

	return nil
}

// initializePermissions 初始化默认权限
func (m *MigrationManager) initializePermissions() error {
	var permCount int64
	DB.Model(&model.Permission{}).Count(&permCount)

	if permCount > 0 {
		return nil // 权限已存在，跳过
	}

	log.Println("  🔐 初始化系统权限...")

	permissions := []*model.Permission{
		// 用户管理权限
		{Name: "user:create", DisplayName: "创建用户", Module: "user", Action: "create", Resource: "user", IsActive: true},
		{Name: "user:read", DisplayName: "查看用户", Module: "user", Action: "read", Resource: "user", IsActive: true},
		{Name: "user:update", DisplayName: "更新用户", Module: "user", Action: "update", Resource: "user", IsActive: true},
		{Name: "user:delete", DisplayName: "删除用户", Module: "user", Action: "delete", Resource: "user", IsActive: true},
		{Name: "user:manage", DisplayName: "管理用户", Module: "user", Action: "manage", Resource: "user", IsActive: true},

		// 角色管理权限
		{Name: "role:create", DisplayName: "创建角色", Module: "role", Action: "create", Resource: "role", IsActive: true},
		{Name: "role:read", DisplayName: "查看角色", Module: "role", Action: "read", Resource: "role", IsActive: true},
		{Name: "role:update", DisplayName: "更新角色", Module: "role", Action: "update", Resource: "role", IsActive: true},
		{Name: "role:delete", DisplayName: "删除角色", Module: "role", Action: "delete", Resource: "role", IsActive: true},
		{Name: "role:assign", DisplayName: "分配角色", Module: "role", Action: "assign", Resource: "role", IsActive: true},

		// 资产管理权限
		{Name: "asset:create", DisplayName: "创建资产", Module: "asset", Action: "create", Resource: "asset", IsActive: true},
		{Name: "asset:read", DisplayName: "查看资产", Module: "asset", Action: "read", Resource: "asset", IsActive: true},
		{Name: "asset:update", DisplayName: "更新资产", Module: "asset", Action: "update", Resource: "asset", IsActive: true},
		{Name: "asset:delete", DisplayName: "删除资产", Module: "asset", Action: "delete", Resource: "asset", IsActive: true},

		// 漏洞管理权限
		{Name: "vuln:create", DisplayName: "创建漏洞", Module: "vuln", Action: "create", Resource: "vulnerability", IsActive: true},
		{Name: "vuln:read", DisplayName: "查看漏洞", Module: "vuln", Action: "read", Resource: "vulnerability", IsActive: true},
		{Name: "vuln:update", DisplayName: "更新漏洞", Module: "vuln", Action: "update", Resource: "vulnerability", IsActive: true},
		{Name: "vuln:delete", DisplayName: "删除漏洞", Module: "vuln", Action: "delete", Resource: "vulnerability", IsActive: true},
		{Name: "vuln:manage", DisplayName: "管理漏洞", Module: "vuln", Action: "manage", Resource: "vulnerability", IsActive: true},

		// 系统管理权限
		{Name: "system:config", DisplayName: "系统配置", Module: "system", Action: "manage", Resource: "config", IsActive: true},
		{Name: "system:audit", DisplayName: "审计日志", Module: "system", Action: "read", Resource: "audit", IsActive: true},
		{Name: "system:permission", DisplayName: "权限管理", Module: "system", Action: "manage", Resource: "permission", IsActive: true},
	}

	for _, perm := range permissions {
		if err := DB.Create(perm).Error; err != nil {
			return fmt.Errorf("创建权限 %s 失败: %w", perm.Name, err)
		}
	}

	log.Printf("    ✓ 已创建 %d 个系统权限", len(permissions))
	return nil
}

// initializeSystemConfigs 初始化系统配置
func (m *MigrationManager) initializeSystemConfigs() error {
	var configCount int64
	DB.Model(&model.SystemConfig{}).Count(&configCount)

	if configCount > 0 {
		return nil // 配置已存在，跳过
	}

	log.Println("  ⚙️  初始化系统配置...")

	configs := []*model.SystemConfig{
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
	}

	for _, config := range configs {
		if err := DB.Create(config).Error; err != nil {
			return fmt.Errorf("创建系统配置 %s 失败: %w", config.Key, err)
		}
	}

	log.Printf("    ✓ 已创建 %d 个系统配置", len(configs))
	return nil
}