package service

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"vulnark/pkg/database"
)

// MigrationService 数据库迁移服务接口
type MigrationService interface {
	RunMigrations() error
	RunSeeds() error
	ExecuteSQLFile(filePath string) error
}

// migrationService 数据库迁移服务实现
type migrationService struct{}

// NewMigrationService 创建数据库迁移服务
func NewMigrationService() MigrationService {
	return &migrationService{}
}

// RunMigrations 执行数据库迁移
func (s *migrationService) RunMigrations() error {
	log.Println("开始执行数据库迁移...")

	// 获取迁移文件目录
	migrationsDir := "../../database/migrations"
	if _, err := os.Stat(migrationsDir); os.IsNotExist(err) {
		// 尝试其他可能的路径
		migrationsDir = "../database/migrations"
		if _, err := os.Stat(migrationsDir); os.IsNotExist(err) {
			migrationsDir = "database/migrations"
			if _, err := os.Stat(migrationsDir); os.IsNotExist(err) {
				return fmt.Errorf("找不到迁移文件目录: %v", err)
			}
		}
	}

	// 读取迁移文件
	files, err := ioutil.ReadDir(migrationsDir)
	if err != nil {
		return fmt.Errorf("读取迁移目录失败: %v", err)
	}

	// 过滤并排序SQL文件
	var sqlFiles []string
	for _, file := range files {
		if !file.IsDir() && strings.HasSuffix(file.Name(), ".sql") {
			sqlFiles = append(sqlFiles, file.Name())
		}
	}
	sort.Strings(sqlFiles)

	// 确保init.sql首先执行
	var orderedFiles []string
	for _, file := range sqlFiles {
		if file == "init.sql" {
			orderedFiles = append([]string{file}, orderedFiles...)
		} else {
			orderedFiles = append(orderedFiles, file)
		}
	}

	// 执行迁移文件
	for _, file := range orderedFiles {
		filePath := filepath.Join(migrationsDir, file)
		log.Printf("执行迁移文件: %s", file)
		
		if err := s.ExecuteSQLFile(filePath); err != nil {
			log.Printf("⚠️  迁移文件 %s 执行失败: %v", file, err)
			// 继续执行其他文件，不中断整个过程
			continue
		}
		log.Printf("✅ 迁移文件 %s 执行成功", file)
	}

	log.Println("数据库迁移完成")
	return nil
}

// RunSeeds 执行数据库种子数据
func (s *migrationService) RunSeeds() error {
	log.Println("开始执行数据库种子数据...")

	// 获取种子文件目录
	seedsDir := "../../database/seeds"
	if _, err := os.Stat(seedsDir); os.IsNotExist(err) {
		// 尝试其他可能的路径
		seedsDir = "../database/seeds"
		if _, err := os.Stat(seedsDir); os.IsNotExist(err) {
			seedsDir = "database/seeds"
			if _, err := os.Stat(seedsDir); os.IsNotExist(err) {
				return fmt.Errorf("找不到种子文件目录: %v", err)
			}
		}
	}

	// 读取种子文件
	files, err := ioutil.ReadDir(seedsDir)
	if err != nil {
		return fmt.Errorf("读取种子目录失败: %v", err)
	}

	// 过滤并排序SQL文件
	var sqlFiles []string
	for _, file := range files {
		if !file.IsDir() && strings.HasSuffix(file.Name(), ".sql") {
			sqlFiles = append(sqlFiles, file.Name())
		}
	}
	sort.Strings(sqlFiles)

	// 执行种子文件
	for _, file := range sqlFiles {
		filePath := filepath.Join(seedsDir, file)
		log.Printf("执行种子文件: %s", file)
		
		if err := s.ExecuteSQLFile(filePath); err != nil {
			log.Printf("⚠️  种子文件 %s 执行失败: %v", file, err)
			// 继续执行其他文件，不中断整个过程
			continue
		}
		log.Printf("✅ 种子文件 %s 执行成功", file)
	}

	log.Println("数据库种子数据执行完成")
	return nil
}

// ExecuteSQLFile 执行SQL文件
func (s *migrationService) ExecuteSQLFile(filePath string) error {
	// 读取SQL文件内容
	content, err := ioutil.ReadFile(filePath)
	if err != nil {
		return fmt.Errorf("读取SQL文件失败: %v", err)
	}

	// 分割SQL语句（按分号分割）
	sqlContent := string(content)
	statements := s.splitSQLStatements(sqlContent)

	// 获取数据库连接
	db := database.GetDB()
	if db == nil {
		return fmt.Errorf("数据库连接未初始化")
	}

	// 执行每个SQL语句
	for i, statement := range statements {
		statement = strings.TrimSpace(statement)
		if statement == "" || strings.HasPrefix(statement, "--") {
			continue // 跳过空语句和注释
		}

		if err := db.Exec(statement).Error; err != nil {
			// 某些错误可以忽略（如表已存在等）
			if s.isIgnorableError(err) {
				log.Printf("⚠️  忽略错误 (语句 %d): %v", i+1, err)
				continue
			}
			return fmt.Errorf("执行SQL语句失败 (语句 %d): %v\nSQL: %s", i+1, err, statement)
		}
	}

	return nil
}

// splitSQLStatements 分割SQL语句
func (s *migrationService) splitSQLStatements(content string) []string {
	var result []string
	var currentStatement strings.Builder
	var inMultiLineComment bool

	lines := strings.Split(content, "\n")

	for _, line := range lines {
		line = strings.TrimSpace(line)

		// 跳过空行
		if line == "" {
			continue
		}

		// 跳过单行注释
		if strings.HasPrefix(line, "--") {
			continue
		}

		// 处理多行注释
		if strings.Contains(line, "/*") && strings.Contains(line, "*/") {
			// 单行内的多行注释，跳过
			continue
		}
		if strings.Contains(line, "/*") {
			inMultiLineComment = true
			continue
		}
		if strings.Contains(line, "*/") {
			inMultiLineComment = false
			continue
		}
		if inMultiLineComment {
			continue
		}

		// 添加到当前语句
		if currentStatement.Len() > 0 {
			currentStatement.WriteString(" ")
		}
		currentStatement.WriteString(line)

		// 检查是否语句结束（以分号结尾且不在字符串中）
		if strings.HasSuffix(line, ";") {
			stmt := strings.TrimSpace(currentStatement.String())
			if stmt != "" && !strings.HasPrefix(stmt, "--") {
				// 移除末尾的分号
				stmt = strings.TrimSuffix(stmt, ";")
				result = append(result, stmt)
			}
			currentStatement.Reset()
		}
	}

	// 处理最后一个语句（如果没有分号结尾）
	if currentStatement.Len() > 0 {
		stmt := strings.TrimSpace(currentStatement.String())
		if stmt != "" && !strings.HasPrefix(stmt, "--") {
			result = append(result, stmt)
		}
	}

	return result
}

// isIgnorableError 判断是否为可忽略的错误
func (s *migrationService) isIgnorableError(err error) bool {
	errStr := strings.ToLower(err.Error())
	
	// 可忽略的错误类型
	ignorableErrors := []string{
		"table already exists",
		"duplicate column name",
		"duplicate key name",
		"duplicate entry",
		"column already exists",
		"index already exists",
	}
	
	for _, ignorable := range ignorableErrors {
		if strings.Contains(errStr, ignorable) {
			return true
		}
	}
	
	return false
}
