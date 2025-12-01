package database

import (
	"fmt"
	"log"
	"reflect"
)

// AutoMigrateModels 自动迁移所有模型 - 现在由MigrationManager处理
func AutoMigrateModels() error {
	if DB == nil {
		return fmt.Errorf("数据库连接未初始化")
	}

	log.Println("开始自动迁移数据库表...")
	log.Println("⚡ 跳过基础自动迁移，使用MigrationManager进行完整迁移")
	log.Println("数据库表自动迁移完成")
	return nil
}

// getTableName 获取模型对应的表名
func getTableName(model interface{}) string {
	// 使用反射获取类型名
	t := reflect.TypeOf(model)
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}
	return t.Name()
}