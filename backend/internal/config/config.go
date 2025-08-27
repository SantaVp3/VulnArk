package config

import (
	"errors"
	"log"
	"os"
	"strings"

	"github.com/spf13/viper"
)

// Config 应用配置结构
type Config struct {
	Server   ServerConfig   `mapstructure:"server"`
	Database DatabaseConfig `mapstructure:"database"`
	JWT      JWTConfig      `mapstructure:"jwt"`
	OSS      OSSConfig      `mapstructure:"oss"`
	Storage  StorageConfig  `mapstructure:"storage"`
	Email    EmailConfig    `mapstructure:"email"`
	Log      LogConfig      `mapstructure:"log"`
	CORS     CORSConfig     `mapstructure:"cors"`
}

// ServerConfig 服务器配置
type ServerConfig struct {
	Host         string `mapstructure:"host"`
	Port         string `mapstructure:"port"`
	Mode         string `mapstructure:"mode"`
	ReadTimeout  int    `mapstructure:"read_timeout"`
	WriteTimeout int    `mapstructure:"write_timeout"`
}

// DatabaseConfig 数据库配置
type DatabaseConfig struct {
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	Username string `mapstructure:"username"`
	Password string `mapstructure:"password"`
	Database string `mapstructure:"database"`
	Charset  string `mapstructure:"charset"`
}

// JWTConfig JWT配置
type JWTConfig struct {
	Secret     string `mapstructure:"secret"`
	ExpireTime int    `mapstructure:"expire_time"`
}

// OSSConfig 对象存储配置
type OSSConfig struct {
	Provider        string `mapstructure:"provider"`
	AccessKeyID     string `mapstructure:"access_key_id"`
	AccessKeySecret string `mapstructure:"access_key_secret"`
	Bucket          string `mapstructure:"bucket"`
	Endpoint        string `mapstructure:"endpoint"`
	Region          string `mapstructure:"region"`
}

// StorageConfig 存储配置
type StorageConfig struct {
	Type      string `mapstructure:"type"`       // local, oss, s3
	LocalPath string `mapstructure:"local_path"` // 本地存储路径
	BaseURL   string `mapstructure:"base_url"`   // 文件访问基础URL
}

// EmailConfig 邮件配置
type EmailConfig struct {
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	Username string `mapstructure:"username"`
	Password string `mapstructure:"password"`
	From     string `mapstructure:"from"`
}

// LogConfig 日志配置
type LogConfig struct {
	Level  string `mapstructure:"level"`
	Format string `mapstructure:"format"`
	Output string `mapstructure:"output"`
}

// AI助手配置已移至前端用户界面
// 用户可以通过设置页面进行个性化配置

var AppConfig *Config

// LoadConfig 加载配置文件
func LoadConfig() {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath("./configs")        // 从项目根目录运行时
	viper.AddConfigPath("../configs")       // 从cmd目录运行时
	viper.AddConfigPath(".")

	// 读取配置文件
	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("配置文件读取失败: %v", err)
	}

	// 可选：支持环境变量覆盖（如果需要）
	viper.AutomaticEnv()
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	AppConfig = &Config{}
	if err := viper.Unmarshal(AppConfig); err != nil {
		log.Fatalf("配置文件解析失败: %v", err)
	}

	// 验证关键配置
	if err := validateConfig(); err != nil {
		log.Fatalf("配置验证失败: %v", err)
	}

	log.Println("配置加载成功")
}

// 注意：移除了setDefaults函数，所有配置都从config.yaml文件中读取
// 这样确保用户必须在配置文件中明确设置所有必要的参数

// validateConfig 验证关键配置
func validateConfig() error {
	// 验证JWT密钥
	if AppConfig.JWT.Secret == "" ||
	   AppConfig.JWT.Secret == "your-jwt-secret-key-at-least-32-characters-long-for-security" {
		return errors.New("JWT密钥未设置或使用默认值，请在configs/config.yaml中修改jwt.secret")
	}

	if len(AppConfig.JWT.Secret) < 32 {
		return errors.New("JWT密钥长度不足32位，请在configs/config.yaml中设置更长的密钥")
	}

	// 验证数据库密码
	if AppConfig.Database.Password == "" ||
	   AppConfig.Database.Password == "your-secure-database-password-here" {
		return errors.New("数据库密码未设置或使用默认值，请在configs/config.yaml中修改database.password")
	}

	if len(AppConfig.Database.Password) < 6 {
		return errors.New("数据库密码长度不足6位，请设置更长的密码")
	}

	// 验证服务器模式
	if AppConfig.Server.Mode == "debug" {
		log.Println("⚠️  警告: 当前使用debug模式，生产环境请在config.yaml中将server.mode设置为release")
	}

	// 验证邮件配置（如果启用邮件功能）
	if AppConfig.Email.Username != "" && AppConfig.Email.Password == "" {
		return errors.New("邮件用户名已设置但密码为空，请在configs/config.yaml中设置email.password")
	}

	// 验证OSS配置（如果使用云存储）
	if AppConfig.OSS.Provider != "local" {
		if AppConfig.OSS.AccessKeyID == "" || AppConfig.OSS.AccessKeySecret == "" {
			return errors.New("云存储配置不完整，请在configs/config.yaml中设置oss.access_key_id和oss.access_key_secret")
		}
	}

	return nil
}

// CORSConfig CORS跨域配置
type CORSConfig struct {
	Enabled          bool     `mapstructure:"enabled"`
	AllowedOrigins   []string `mapstructure:"allowed_origins"`
	AllowedMethods   []string `mapstructure:"allowed_methods"`
	AllowedHeaders   []string `mapstructure:"allowed_headers"`
	ExposedHeaders   []string `mapstructure:"exposed_headers"`
	AllowCredentials bool     `mapstructure:"allow_credentials"`
	MaxAge           int      `mapstructure:"max_age"`
}

// GetEnv 获取环境变量，如果不存在则返回默认值
func GetEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
