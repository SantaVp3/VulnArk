package auth

import (
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"vulnark/internal/config"
)

// Claims JWT声明
type Claims struct {
	UserID   uint   `json:"user_id"`
	Username string `json:"username"`
	RoleID   uint   `json:"role_id"`
	jwt.RegisteredClaims
}

// JWTManager JWT管理器
type JWTManager struct {
	secretKey   string
	expireTime  time.Duration
}

// NewJWTManager 创建JWT管理器
func NewJWTManager() *JWTManager {
	cfg := config.AppConfig.JWT
	return &JWTManager{
		secretKey:  cfg.Secret,
		expireTime: time.Duration(cfg.ExpireTime) * time.Second,
	}
}

// GenerateToken 生成JWT令牌
func (j *JWTManager) GenerateToken(userID uint, username string, roleID uint) (string, time.Time, error) {
	expiresAt := time.Now().Add(j.expireTime)
	
	claims := &Claims{
		UserID:   userID,
		Username: username,
		RoleID:   roleID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expiresAt),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			NotBefore: jwt.NewNumericDate(time.Now()),
			Issuer:    "vulnark",
			Subject:   username,
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString([]byte(j.secretKey))
	if err != nil {
		return "", time.Time{}, err
	}

	return tokenString, expiresAt, nil
}

// ParseToken 解析JWT令牌
func (j *JWTManager) ParseToken(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("无效的签名方法")
		}
		return []byte(j.secretKey), nil
	})

	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims, nil
	}

	return nil, errors.New("无效的令牌")
}

// RefreshToken 刷新令牌
func (j *JWTManager) RefreshToken(tokenString string) (string, time.Time, error) {
	claims, err := j.ParseToken(tokenString)
	if err != nil {
		return "", time.Time{}, err
	}

	// 检查令牌是否即将过期（剩余时间少于1小时）
	if time.Until(claims.ExpiresAt.Time) > time.Hour {
		return "", time.Time{}, errors.New("令牌尚未到刷新时间")
	}

	return j.GenerateToken(claims.UserID, claims.Username, claims.RoleID)
}

// ValidateToken 验证令牌有效性
func (j *JWTManager) ValidateToken(tokenString string) bool {
	_, err := j.ParseToken(tokenString)
	return err == nil
}
