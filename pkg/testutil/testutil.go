package testutil

import (
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"vulnark/internal/model"
)

// NewTestDB 创建用于测试的SQLite内存数据库
func NewTestDB() *gorm.DB {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		panic("failed to connect database: " + err.Error())
	}

	// 自动迁移测试数据库
	err = db.AutoMigrate(
		&model.Role{},
		&model.User{},
		&model.Asset{},
		&model.Vulnerability{},
		&model.OWASPCategory{},
		&model.VulnerabilityAttachment{},
	)
	if err != nil {
		panic("failed to migrate database: " + err.Error())
	}

	return db
}

// SeedTestData 为测试数据库填充测试数据
func SeedTestData(db *gorm.DB) {
	// 创建测试角色
	adminRole := &model.Role{
		ID:          1,
		Name:        "测试管理员",
		Description: "测试管理员角色",
		Permissions: []string{"*"},
	}
	db.Create(adminRole)

	// 创建测试用户
	testUser := &model.User{
		ID:           1,
		Username:     "testuser",
		Email:        "test@example.com",
		PasswordHash: "$2a$10$example_hash",
		RealName:     "测试用户",
		RoleID:       1,
		Status:       1,
	}
	db.Create(testUser)
}

// CleanupTestData 清理测试数据
func CleanupTestData(db *gorm.DB) {
	db.Exec("DELETE FROM users")
	db.Exec("DELETE FROM roles")
	db.Exec("DELETE FROM assets")
	db.Exec("DELETE FROM vulnerabilities")
}