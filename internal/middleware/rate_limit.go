package middleware

import (
	"fmt"
	"strconv"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"vulnark/pkg/utils"
)

// 全局速率限制器实例
var globalLimiter *TokenBucketLimiter

// 初始化全局限制器
func init() {
	// 创建全局限制器：每秒100个请求，容量200
	globalLimiter = NewTokenBucketLimiter(100, 200)
}

// RateLimiter 速率限制器接口
type RateLimiter interface {
	Allow(key string) bool
	Reset(key string)
}

// TokenBucketLimiter 令牌桶限制器
type TokenBucketLimiter struct {
	rate     int           // 每秒生成的令牌数
	capacity int           // 桶容量
	buckets  map[string]*TokenBucket
	mutex    sync.RWMutex
}

// TokenBucket 令牌桶
type TokenBucket struct {
	tokens   int       // 当前令牌数
	capacity int       // 桶容量
	rate     int       // 每秒生成的令牌数
	lastTime time.Time // 上次更新时间
	mutex    sync.Mutex
}

// NewTokenBucketLimiter 创建令牌桶限制器
func NewTokenBucketLimiter(rate, capacity int) *TokenBucketLimiter {
	return &TokenBucketLimiter{
		rate:     rate,
		capacity: capacity,
		buckets:  make(map[string]*TokenBucket),
		mutex:    sync.RWMutex{},
	}
}

// Allow 检查是否允许请求
func (tbl *TokenBucketLimiter) Allow(key string) bool {
	tbl.mutex.Lock()
	bucket, exists := tbl.buckets[key]
	if !exists {
		bucket = &TokenBucket{
			tokens:   tbl.capacity,
			capacity: tbl.capacity,
			rate:     tbl.rate,
			lastTime: time.Now(),
		}
		tbl.buckets[key] = bucket
	}
	tbl.mutex.Unlock()

	return bucket.takeToken()
}

// Reset 重置指定key的限制
func (tbl *TokenBucketLimiter) Reset(key string) {
	tbl.mutex.Lock()
	delete(tbl.buckets, key)
	tbl.mutex.Unlock()
}

// takeToken 获取令牌
func (tb *TokenBucket) takeToken() bool {
	tb.mutex.Lock()
	defer tb.mutex.Unlock()

	now := time.Now()
	elapsed := now.Sub(tb.lastTime).Seconds()
	
	// 添加新令牌
	tokensToAdd := int(elapsed * float64(tb.rate))
	tb.tokens += tokensToAdd
	if tb.tokens > tb.capacity {
		tb.tokens = tb.capacity
	}
	
	tb.lastTime = now

	// 检查是否有可用令牌
	if tb.tokens > 0 {
		tb.tokens--
		return true
	}

	return false
}

// 全局限制器实例
var (
	apiLimiter       *TokenBucketLimiter
	loginLimiter     *TokenBucketLimiter
	uploadLimiter    *TokenBucketLimiter
	limiterOnce      sync.Once
)

// initLimiters 初始化限制器
func initLimiters() {
	limiterOnce.Do(func() {
		// 全局限制：每秒100个请求，桶容量200
		globalLimiter = NewTokenBucketLimiter(100, 200)
		
		// API限制：每秒50个请求，桶容量100
		apiLimiter = NewTokenBucketLimiter(50, 100)
		
		// 登录限制：每分钟5次，桶容量10
		loginLimiter = NewTokenBucketLimiter(5, 10)
		
		// 上传限制：每分钟10次，桶容量20
		uploadLimiter = NewTokenBucketLimiter(10, 20)
	})
}

// RateLimitMiddleware 通用速率限制中间件
func RateLimitMiddleware(limiter *TokenBucketLimiter, keyFunc func(*gin.Context) string) gin.HandlerFunc {
	initLimiters()
	
	return func(c *gin.Context) {
		key := keyFunc(c)
		
		if !limiter.Allow(key) {
			c.Header("X-RateLimit-Limit", strconv.Itoa(limiter.capacity))
			c.Header("X-RateLimit-Remaining", "0")
			c.Header("Retry-After", "60")
			
			utils.TooManyRequestsResponse(c, "请求过于频繁，请稍后再试")
			c.Abort()
			return
		}
		
		c.Next()
	}
}

// GlobalRateLimit 全局速率限制
func GlobalRateLimit() gin.HandlerFunc {
	return RateLimitMiddleware(globalLimiter, func(c *gin.Context) string {
		return c.ClientIP()
	})
}

// APIRateLimit API速率限制
func APIRateLimit() gin.HandlerFunc {
	return RateLimitMiddleware(apiLimiter, func(c *gin.Context) string {
		return c.ClientIP() + ":" + c.Request.URL.Path
	})
}

// LoginRateLimit 登录速率限制
func LoginRateLimit() gin.HandlerFunc {
	return RateLimitMiddleware(loginLimiter, func(c *gin.Context) string {
		return "login:" + c.ClientIP()
	})
}

// UploadRateLimit 上传速率限制
func UploadRateLimit() gin.HandlerFunc {
	return RateLimitMiddleware(uploadLimiter, func(c *gin.Context) string {
		// 如果用户已登录，使用用户ID，否则使用IP
		if userID, exists := GetCurrentUserID(c); exists {
			return fmt.Sprintf("upload:user:%d", userID)
		}
		return "upload:ip:" + c.ClientIP()
	})
}

// UserBasedRateLimit 基于用户的速率限制
func UserBasedRateLimit(rate, capacity int) gin.HandlerFunc {
	limiter := NewTokenBucketLimiter(rate, capacity)
	
	return RateLimitMiddleware(limiter, func(c *gin.Context) string {
		if userID, exists := GetCurrentUserID(c); exists {
			return fmt.Sprintf("user:%d", userID)
		}
		return "ip:" + c.ClientIP()
	})
}

// IPBasedRateLimit 基于IP的速率限制
func IPBasedRateLimit(rate, capacity int) gin.HandlerFunc {
	limiter := NewTokenBucketLimiter(rate, capacity)
	
	return RateLimitMiddleware(limiter, func(c *gin.Context) string {
		return c.ClientIP()
	})
}

// EndpointRateLimit 基于端点的速率限制
func EndpointRateLimit(rate, capacity int) gin.HandlerFunc {
	limiter := NewTokenBucketLimiter(rate, capacity)
	
	return RateLimitMiddleware(limiter, func(c *gin.Context) string {
		return c.Request.Method + ":" + c.Request.URL.Path
	})
}

// CleanupExpiredBuckets 清理过期的令牌桶（定期调用）
func CleanupExpiredBuckets() {
	initLimiters()
	
	limiters := []*TokenBucketLimiter{
		globalLimiter, apiLimiter, loginLimiter, uploadLimiter,
	}
	
	for _, limiter := range limiters {
		limiter.mutex.Lock()
		now := time.Now()
		for key, bucket := range limiter.buckets {
			bucket.mutex.Lock()
			// 如果桶超过10分钟没有使用，删除它
			if now.Sub(bucket.lastTime) > 10*time.Minute {
				delete(limiter.buckets, key)
			}
			bucket.mutex.Unlock()
		}
		limiter.mutex.Unlock()
	}
}

// StartCleanupRoutine 启动清理协程
func StartCleanupRoutine() {
	go func() {
		ticker := time.NewTicker(5 * time.Minute)
		defer ticker.Stop()
		
		for range ticker.C {
			CleanupExpiredBuckets()
		}
	}()
}
