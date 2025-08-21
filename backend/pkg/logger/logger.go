package logger

import (
	"os"
	"path/filepath"
	"time"

	"github.com/sirupsen/logrus"
	"vulnark/internal/config"
)

var Logger *logrus.Logger

// InitLogger 初始化日志
func InitLogger() {
	Logger = logrus.New()
	
	cfg := config.AppConfig.Log

	// 设置日志级别
	level, err := logrus.ParseLevel(cfg.Level)
	if err != nil {
		level = logrus.InfoLevel
	}
	Logger.SetLevel(level)

	// 设置日志格式
	if cfg.Format == "json" {
		Logger.SetFormatter(&logrus.JSONFormatter{
			TimestampFormat: time.RFC3339,
		})
	} else {
		Logger.SetFormatter(&logrus.TextFormatter{
			FullTimestamp:   true,
			TimestampFormat: "2006-01-02 15:04:05",
		})
	}

	// 设置输出
	if cfg.Output == "file" {
		// 创建日志目录
		logDir := "logs"
		if err := os.MkdirAll(logDir, 0755); err != nil {
			Logger.Fatalf("创建日志目录失败: %v", err)
		}

		// 创建日志文件
		logFile := filepath.Join(logDir, "vulnark.log")
		file, err := os.OpenFile(logFile, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
		if err != nil {
			Logger.Fatalf("打开日志文件失败: %v", err)
		}
		Logger.SetOutput(file)
	} else {
		Logger.SetOutput(os.Stdout)
	}

	Logger.Info("日志系统初始化完成")
}

// GetLogger 获取日志实例
func GetLogger() *logrus.Logger {
	return Logger
}

// Info 记录信息日志
func Info(args ...interface{}) {
	Logger.Info(args...)
}

// Infof 记录格式化信息日志
func Infof(format string, args ...interface{}) {
	Logger.Infof(format, args...)
}

// Warn 记录警告日志
func Warn(args ...interface{}) {
	Logger.Warn(args...)
}

// Warnf 记录格式化警告日志
func Warnf(format string, args ...interface{}) {
	Logger.Warnf(format, args...)
}

// Error 记录错误日志
func Error(args ...interface{}) {
	Logger.Error(args...)
}

// Errorf 记录格式化错误日志
func Errorf(format string, args ...interface{}) {
	Logger.Errorf(format, args...)
}

// Debug 记录调试日志
func Debug(args ...interface{}) {
	Logger.Debug(args...)
}

// Debugf 记录格式化调试日志
func Debugf(format string, args ...interface{}) {
	Logger.Debugf(format, args...)
}

// Fatal 记录致命错误日志并退出
func Fatal(args ...interface{}) {
	Logger.Fatal(args...)
}

// Fatalf 记录格式化致命错误日志并退出
func Fatalf(format string, args ...interface{}) {
	Logger.Fatalf(format, args...)
}

// WithFields 创建带字段的日志条目
func WithFields(fields logrus.Fields) *logrus.Entry {
	return Logger.WithFields(fields)
}

// WithField 创建带单个字段的日志条目
func WithField(key string, value interface{}) *logrus.Entry {
	return Logger.WithField(key, value)
}
