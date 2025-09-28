package main

import (
	"context"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"vulnark/internal/config"
	"vulnark/internal/container"
	"vulnark/internal/middleware"
	"vulnark/internal/router"
	"vulnark/internal/service"
	"vulnark/pkg/database"
	"vulnark/pkg/logger"
	_ "vulnark/docs"
)

// @title VulnArk API
// @version 1.0.0
// @description VulnArk漏洞管理系统API文档
// @termsOfService https://vulnark.com/terms

// @contact.name VulnArk Team
// @contact.email support@vulnark.com
// @contact.url https://vulnark.com

// @license.name MIT
// @license.url https://opensource.org/licenses/MIT

// @host 0.0.0.0:8080
// @BasePath /

// @securityDefinitions.apikey ApiKeyAuth
// @in header
// @name Authorization
// @description Bearer token authentication

func main() {
	// 加载配置
	config.LoadConfig()

	// 初始化日志
	logger.InitLogger()

	// 初始化数据库
	database.InitDatabase()
	defer database.CloseDatabase()

	// 执行数据库表迁移
	if err := database.AutoMigrateModels(); err != nil {
		logger.Errorf("数据库表迁移失败: %v", err)
		// 不中断程序，继续执行，让SQL迁移作为备选方案
	}

	// 初始化依赖注入容器
	appContainer := container.NewContainer()

	// 初始化系统（创建默认管理员账号等）
	initService := service.NewInitService(appContainer.UserRepo)
	if err := initService.InitializeSystem(); err != nil {
		logger.Fatalf("系统初始化失败: %v", err)
	}

	// 设置Gin模式
	gin.SetMode(config.AppConfig.Server.Mode)

	// 创建Gin引擎
	r := gin.New()

	// 注册中间件
	r.Use(middleware.RecoveryMiddleware())
	r.Use(middleware.RequestLoggerMiddleware())
	r.Use(middleware.CORSMiddleware())
	r.Use(middleware.FirstDeployMiddleware())

	// 健康检查接口
	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status":    "ok",
			"timestamp": time.Now().Unix(),
			"version":   "1.0.0",
		})
	})

	// 设置API路由（使用依赖注入容器）
	router.SetupRoutes(r, appContainer)

	// 服务前端静态文件
	r.Static("/assets", "./web/assets")
	r.StaticFile("/vite.svg", "./web/vite.svg")

	// 配置JavaScript文件的正确MIME类型
	r.GET("/config.js", func(c *gin.Context) {
		c.Header("Content-Type", "application/javascript; charset=utf-8")
		c.File("./web/config.js")
	})

	// 处理前端路由 - 所有非API请求都返回index.html
	r.NoRoute(func(c *gin.Context) {
		path := c.Request.URL.Path
		// 如果是API请求，返回404
		if strings.HasPrefix(path, "/api") {
			c.JSON(http.StatusNotFound, gin.H{"error": "Route not found"})
			return
		}
		// 否则返回前端index.html (支持前端路由)
		c.File("./web/index.html")
	})

	// 启动服务器
	host := config.AppConfig.Server.Host
	if host == "" {
		host = "0.0.0.0" // 确保监听所有IPv4接口
	}
	addr := host + ":" + config.AppConfig.Server.Port
	srv := &http.Server{
		Addr:         addr,
		Handler:      r,
		ReadTimeout:  time.Duration(config.AppConfig.Server.ReadTimeout) * time.Second,
		WriteTimeout: time.Duration(config.AppConfig.Server.WriteTimeout) * time.Second,
	}

	// 在goroutine中启动服务器
	go func() {
		logger.Infof("服务器启动在 %s", addr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Fatalf("服务器启动失败: %v", err)
		}
	}()

	// 等待中断信号以优雅地关闭服务器
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	logger.Info("正在关闭服务器...")

	// 给服务器5秒钟来完成现有请求
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		logger.Fatalf("服务器强制关闭: %v", err)
	}

	logger.Info("服务器已关闭")
}

// setupRoutes 设置路由（后续会扩展）
func setupRoutes(r *gin.Engine) {
	// TODO: 在这里注册各个模块的路由
	// 例如：
	// userController := controller.NewUserController()
	// r.POST("/api/v1/login", userController.Login)
	// r.POST("/api/v1/register", userController.Register)
}
