package utils

import (
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"fmt"

	"golang.org/x/crypto/bcrypt"
)

// HashPassword 使用bcrypt加密密码
func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(bytes), err
}

// CheckPassword 验证密码
func CheckPassword(password, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}

// CheckPasswordHash 验证密码哈希（别名函数，保持兼容性）
func CheckPasswordHash(password, hash string) bool {
	return CheckPassword(password, hash)
}

// GenerateRandomString 生成随机字符串
func GenerateRandomString(length int) (string, error) {
	bytes := make([]byte, length)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	return hex.EncodeToString(bytes)[:length], nil
}

// SHA256Hash 计算SHA256哈希
func SHA256Hash(data string) string {
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}

// GenerateFileHash 生成文件哈希（用于文件去重）
func GenerateFileHash(filename string, size int64) string {
	data := fmt.Sprintf("%s_%d", filename, size)
	return SHA256Hash(data)
}

// GenerateSecurePassword 生成安全的随机密码
func GenerateSecurePassword(length int) (string, error) {
	if length < 8 {
		length = 12 // 默认最小长度
	}

	// 定义字符集
	const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*"

	// 生成随机字节
	bytes := make([]byte, length)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}

	// 将字节转换为字符
	password := make([]byte, length)
	for i, b := range bytes {
		password[i] = charset[int(b)%len(charset)]
	}

	return string(password), nil
}
