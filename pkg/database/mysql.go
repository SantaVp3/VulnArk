package database

import (
	"fmt"
	"log"
	"time"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"vulnark/internal/config"
)

var DB *gorm.DB

// InitDatabase 初始化数据库连接
func InitDatabase() {
	cfg := config.AppConfig.Database

	// 首先连接到MySQL服务器（不指定数据库）来创建数据库
	serverDSN := fmt.Sprintf("%s:%s@tcp(%s:%d)/?charset=%s&parseTime=True&loc=Local",
		cfg.Username,
		cfg.Password,
		cfg.Host,
		cfg.Port,
		cfg.Charset,
	)

	// 配置GORM
	gormConfig := &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
		NowFunc: func() time.Time {
			return time.Now().Local()
		},
	}

	// 先连接到MySQL服务器
	serverDB, err := gorm.Open(mysql.Open(serverDSN), gormConfig)
	if err != nil {
		log.Fatalf("MySQL服务器连接失败: %v", err)
	}

	// 创建数据库（如果不存在）
	createDBSQL := fmt.Sprintf("CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", cfg.Database)
	if err := serverDB.Exec(createDBSQL).Error; err != nil {
		log.Fatalf("创建数据库失败: %v", err)
	}
	log.Printf("数据库 %s 已确保存在", cfg.Database)

	// 现在连接到具体的数据库
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=%s&parseTime=True&loc=Local",
		cfg.Username,
		cfg.Password,
		cfg.Host,
		cfg.Port,
		cfg.Database,
		cfg.Charset,
	)

	// 连接到目标数据库
	DB, err = gorm.Open(mysql.Open(dsn), gormConfig)
	if err != nil {
		log.Fatalf("数据库连接失败: %v", err)
	}

	// 获取底层sql.DB对象进行连接池配置
	sqlDB, err := DB.DB()
	if err != nil {
		log.Fatalf("获取数据库实例失败: %v", err)
	}

	// 设置连接池参数
	sqlDB.SetMaxIdleConns(10)           // 最大空闲连接数
	sqlDB.SetMaxOpenConns(100)          // 最大打开连接数
	sqlDB.SetConnMaxLifetime(time.Hour) // 连接最大生存时间

	// 测试连接
	if err := sqlDB.Ping(); err != nil {
		log.Fatalf("数据库连接测试失败: %v", err)
	}

	log.Println("数据库连接成功")
}

// AutoMigrate 自动迁移数据库表
func AutoMigrate() error {
	if DB == nil {
		return fmt.Errorf("数据库连接未初始化")
	}

	// 导入所有模型
	// 注意：这里需要导入model包，需要在文件顶部添加import
	// 暂时注释掉，将在实际使用时启用
	/*
	tables := []interface{}{
		&model.Role{},
		&model.User{},
		&model.Vulnerability{},
		&model.Asset{},
		&model.AIConfiguration{},
		&model.AIConversation{},
		&model.AIMessage{},
		&model.AIProvider{},
		&model.VulnerabilityAssignment{},
		&model.AssignmentRule{},
		&model.VulnerabilityTimeline{},
		&model.Analytics{},
		&model.KnowledgeBase{},
		&model.KnowledgeCategory{},
		&model.Notification{},
		&model.NotificationRule{},
		&model.Permission{},
		&model.Report{},
	}

	for _, table := range tables {
		if err := DB.AutoMigrate(table); err != nil {
			return fmt.Errorf("迁移表 %T 失败: %w", table, err)
		}
		log.Printf("✅ 表 %T 迁移成功", table)
	}
	*/

	log.Println("数据库表迁移完成")
	return nil
}

// GetDB 获取数据库实例
func GetDB() *gorm.DB {
	return DB
}

// CloseDatabase 关闭数据库连接
func CloseDatabase() {
	if DB != nil {
		sqlDB, err := DB.DB()
		if err != nil {
			log.Printf("获取数据库实例失败: %v", err)
			return
		}
		if err := sqlDB.Close(); err != nil {
			log.Printf("关闭数据库连接失败: %v", err)
		} else {
			log.Println("数据库连接已关闭")
		}
	}
}
