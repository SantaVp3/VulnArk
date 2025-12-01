package router

import (
	"vulnark/internal/container"
	"vulnark/internal/middleware"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

// SetupRoutes 设置路由
func SetupRoutes(r *gin.Engine, c *container.Container) {
	// 使用依赖注入容器中的控制器实例
	userController := c.UserController
	assetController := c.AssetController
	vulnController := c.VulnController
	roleController := c.RoleController
	assignmentController := c.AssignmentController
	reportController := c.ReportController
	notificationController := c.NotificationController
	permissionController := c.PermissionController
	systemController := c.SystemController
	knowledgeController := c.KnowledgeController
	analyticsController := c.AnalyticsController
	apiTestController := c.APITestController
	apiVersionController := c.APIVersionController
	settingsController := c.SettingsController
	aiController := c.AIController

	// 辅助函数简化权限中间件调用
	requirePermission := func(permission string) gin.HandlerFunc {
		return middleware.RequirePermission(permission, c.PermissionService)
	}

	// 全局中间件
	r.Use(middleware.GlobalRateLimit()) // 全局速率限制

	// Swagger API文档
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	// API版本管理（公开接口）
	r.GET("/api/versions", apiVersionController.GetAPIVersions)
	r.GET("/api/versions/:version", apiVersionController.GetVersionDetails)
	r.GET("/api/versions/compatibility", apiVersionController.GetCompatibility)
	r.GET("/api/health", apiVersionController.GetAPIHealth)
	r.GET("/api/status", apiVersionController.GetAPIStatus)
	r.GET("/api/changelog", apiVersionController.GetAPIChangelog)
	r.GET("/api/metrics", apiVersionController.GetAPIMetrics)

	// API路由组
	api := r.Group("/api/v1")
	{
		// 公开接口（不需要认证）
		public := api.Group("/public")
		{
			public.GET("/ping", func(c *gin.Context) {
				c.JSON(200, gin.H{"message": "pong"})
			})
			public.GET("/configs", systemController.GetPublicConfigs)
			public.GET("/system/info", systemController.GetSystemInfo)
			public.GET("/client-config", systemController.GetClientConfig)
		}

		// 认证接口（添加登录速率限制）
		api.POST("/login", middleware.LoginRateLimit(), userController.Login)

		// 需要认证的接口
		auth := api.Group("/")
		auth.Use(middleware.AuthMiddleware())
		{
			// 用户个人资料
			auth.GET("/profile", userController.GetProfile)
			auth.PUT("/profile", userController.UpdateProfile)
			auth.POST("/change-password", userController.ChangePassword)

			// 用户管理（需要管理员权限）
			users := auth.Group("/users")
			users.Use(requirePermission("user:manage"))
			{
				users.GET("/departments", userController.GetDepartmentOptions) // 部门选项
				users.POST("", userController.CreateUser)
				users.GET("", userController.GetUserList)
				users.GET("/:id", userController.GetUser)
				users.PUT("/:id", userController.UpdateUser)
				users.DELETE("/:id", userController.DeleteUser)
			}

			// 角色选项（用于用户创建，需要用户管理权限）
			auth.GET("/roles/options", requirePermission("user:manage"), roleController.GetRoleOptions)

			// 角色管理（需要管理员权限）
			roles := auth.Group("/roles")
			roles.Use(requirePermission("role:manage"))
			{
				roles.POST("", roleController.CreateRole)
				roles.GET("", roleController.GetRoleList)
				roles.GET("/:id", roleController.GetRole)
				roles.PUT("/:id", roleController.UpdateRole)
				roles.DELETE("/:id", roleController.DeleteRole)
			}

			// 资产管理
			assets := auth.Group("/assets")
			assets.Use(requirePermission("asset:read")) // 基础权限检查
			{
				assets.POST("", assetController.CreateAsset)
				assets.GET("", assetController.GetAssetList)
				assets.GET("/stats", assetController.GetAssetStats)
				assets.GET("/types", assetController.GetAssetTypes)
				assets.GET("/importance-levels", assetController.GetImportanceLevels)
				assets.POST("/import", assetController.ImportAssets)
				assets.GET("/:id", assetController.GetAsset)
				assets.PUT("/:id", assetController.UpdateAsset)
				assets.DELETE("/:id", assetController.DeleteAsset)
			}

			// 漏洞管理
			vulnerabilities := auth.Group("/vulnerabilities")
			vulnerabilities.Use(requirePermission("vuln:read")) // 基础权限检查
			{
				vulnerabilities.POST("", vulnController.CreateVulnerability)
				vulnerabilities.GET("", vulnController.GetVulnerabilityList)
				vulnerabilities.GET("/stats", vulnController.GetVulnerabilityStats)
				vulnerabilities.GET("/owasp-categories", vulnController.GetOWASPCategories)
				vulnerabilities.GET("/severity-levels", vulnController.GetSeverityLevels)
				vulnerabilities.GET("/status-list", vulnController.GetStatusList)
				vulnerabilities.POST("/batch-assign", assignmentController.BatchAssignVulnerabilities)
				vulnerabilities.POST("/auto-assign", assignmentController.AutoAssignVulnerabilities)
				vulnerabilities.GET("/:id", vulnController.GetVulnerability)
				vulnerabilities.PUT("/:id", vulnController.UpdateVulnerability)
				vulnerabilities.DELETE("/:id", vulnController.DeleteVulnerability)
				vulnerabilities.PUT("/:id/status", vulnController.UpdateVulnerabilityStatus)
				vulnerabilities.POST("/:id/assign", assignmentController.AssignVulnerability)
				vulnerabilities.GET("/:id/timeline", assignmentController.GetVulnerabilityTimeline)
			}

			// 分配规则管理
			assignmentRules := auth.Group("/assignment-rules")
			assignmentRules.Use(requirePermission("vuln:manage")) // 分配权限检查
			{
				assignmentRules.POST("", assignmentController.CreateAssignmentRule)
				assignmentRules.GET("", assignmentController.GetAssignmentRuleList)
				assignmentRules.GET("/types", assignmentController.GetRuleTypes)
				assignmentRules.GET("/:id", assignmentController.GetAssignmentRule)
				assignmentRules.PUT("/:id", assignmentController.UpdateAssignmentRule)
				assignmentRules.DELETE("/:id", assignmentController.DeleteAssignmentRule)
			}

			// 分配统计
			assignments := auth.Group("/assignments")
			{
				assignments.GET("/stats", assignmentController.GetAssignmentStats)
			}

			// 报告管理
			reports := auth.Group("/reports")
			// reports.Use(middleware.RequirePermission("report:manage")) // TODO: 实现权限中间件
			{
				reports.POST("", reportController.UploadReport)
				reports.GET("", reportController.GetReportList)
				reports.GET("/stats", reportController.GetReportStats)
				reports.GET("/:id", reportController.GetReport)
				reports.GET("/:id/content", reportController.GetReportContent)
				reports.PUT("/:id", reportController.UpdateReport)
				reports.DELETE("/:id", reportController.DeleteReport)
				reports.POST("/:id/submit", reportController.SubmitReport)
				reports.POST("/:id/review", reportController.ReviewReport)
				reports.POST("/:id/archive", reportController.ArchiveReport)
				reports.GET("/:id/download", reportController.DownloadReport)
			}

			// 通知管理
			notifications := auth.Group("/notifications")
			// notifications.Use(middleware.RequirePermission("notification:manage")) // TODO: 实现权限中间件
			{
				notifications.POST("", notificationController.CreateNotification)
				notifications.GET("", notificationController.GetNotificationList)
				notifications.GET("/my", notificationController.GetMyNotifications)
				notifications.GET("/stats", notificationController.GetNotificationStats)
				notifications.GET("/settings", notificationController.GetNotificationSetting)
				notifications.PUT("/settings", notificationController.UpdateNotificationSetting)
				notifications.POST("/read-all", notificationController.MarkAllAsRead)
				notifications.POST("/email", notificationController.SendEmail)
				notifications.GET("/:id", notificationController.GetNotification)
				notifications.POST("/:id/read", notificationController.MarkAsRead)
				notifications.DELETE("/:id", notificationController.DeleteNotification)
			}

			// 邮件模板管理
			emailTemplates := auth.Group("/email-templates")
			// emailTemplates.Use(middleware.RequirePermission("email:manage")) // TODO: 实现权限中间件
			{
				emailTemplates.POST("", notificationController.CreateEmailTemplate)
				emailTemplates.GET("", notificationController.GetEmailTemplateList)
				emailTemplates.GET("/:id", notificationController.GetEmailTemplate)
				emailTemplates.PUT("/:id", notificationController.UpdateEmailTemplate)
				emailTemplates.DELETE("/:id", notificationController.DeleteEmailTemplate)
			}

			// 权限管理
			permissions := auth.Group("/permissions")
			// permissions.Use(middleware.RequirePermission("system:permission")) // TODO: 实现权限中间件
			{
				permissions.POST("", permissionController.CreatePermission)
				permissions.GET("", permissionController.GetPermissionList)
				permissions.GET("/my", permissionController.GetMyPermissions)
				permissions.GET("/check", permissionController.CheckPermission)
				permissions.GET("/module/:module", permissionController.GetPermissionsByModule)
				permissions.GET("/:id", permissionController.GetPermission)
				permissions.PUT("/:id", permissionController.UpdatePermission)
				permissions.DELETE("/:id", permissionController.DeletePermission)
			}

			// 角色权限管理
			rolePermissions := auth.Group("/role-permissions")
			{
				rolePermissions.POST("/:role_id", permissionController.AssignRolePermissions)
				rolePermissions.GET("/:role_id", permissionController.GetRolePermissions)
			}

			// 系统管理
			system := auth.Group("/system")
			// system.Use(middleware.RequirePermission("system:config")) // TODO: 实现权限中间件
			{
				system.GET("/configs", systemController.GetSystemConfigs)
				system.GET("/configs/category/:category", systemController.GetConfigsByCategory)
				system.GET("/configs/:key", systemController.GetSystemConfig)
				system.PUT("/configs/:key", systemController.UpdateSystemConfig)

				system.GET("/audit-logs", systemController.GetAuditLogs)
				system.GET("/audit-logs/stats", systemController.GetAuditLogStats)
				system.GET("/audit-logs/:id", systemController.GetAuditLog)
				system.POST("/audit-logs/cleanup", systemController.CleanupAuditLogs)
			}

			// 知识库管理
			knowledge := auth.Group("/knowledge")
			// knowledge.Use(middleware.RequirePermission("knowledge:read")) // TODO: 实现权限中间件
			{
				knowledge.POST("", knowledgeController.CreateKnowledge)
				knowledge.GET("", knowledgeController.GetKnowledgeList)
				knowledge.GET("/stats", knowledgeController.GetKnowledgeStats)
				knowledge.GET("/search", knowledgeController.SearchKnowledge)
				knowledge.GET("/popular", knowledgeController.GetPopularKnowledge)
				knowledge.GET("/recent", knowledgeController.GetRecentKnowledge)
				knowledge.GET("/:id", knowledgeController.GetKnowledge)
				knowledge.PUT("/:id", knowledgeController.UpdateKnowledge)
				knowledge.DELETE("/:id", knowledgeController.DeleteKnowledge)
				knowledge.POST("/:id/like", knowledgeController.LikeKnowledge)
				knowledge.POST("/:id/publish", knowledgeController.PublishKnowledge)
				knowledge.POST("/:id/archive", knowledgeController.ArchiveKnowledge)
				knowledge.GET("/:id/related", knowledgeController.GetRelatedKnowledge)
			}

			// 标签管理
			tags := auth.Group("/tags")
			// tags.Use(middleware.RequirePermission("tag:manage")) // TODO: 实现权限中间件
			{
				tags.POST("", knowledgeController.CreateTag)
				tags.GET("", knowledgeController.GetTagList)
				tags.GET("/popular", knowledgeController.GetPopularTags)
				tags.GET("/:id", knowledgeController.GetTag)
				tags.PUT("/:id", knowledgeController.UpdateTag)
				tags.DELETE("/:id", knowledgeController.DeleteTag)
			}

			// 模板管理
			templates := auth.Group("/templates")
			// templates.Use(middleware.RequirePermission("template:manage")) // TODO: 实现权限中间件
			{
				templates.POST("", knowledgeController.CreateTemplate)
				templates.GET("", knowledgeController.GetTemplateList)
				templates.GET("/:id", knowledgeController.GetTemplate)
				templates.PUT("/:id", knowledgeController.UpdateTemplate)
				templates.DELETE("/:id", knowledgeController.DeleteTemplate)
				templates.POST("/:id/use", knowledgeController.UseTemplate)
			}

			// 统计分析
			analytics := auth.Group("/analytics")
			// analytics.Use(middleware.RequirePermission("analytics:read")) // TODO: 实现权限中间件
			{
				analytics.GET("/dashboard", analyticsController.GetDashboardStats)
				analytics.GET("/overview", analyticsController.GetSystemOverview)
				analytics.GET("/vulnerability", analyticsController.GetVulnerabilityAnalytics)
				analytics.GET("/vulnerability/trend", analyticsController.GetVulnerabilityTrend)
				analytics.GET("/vulnerability/severity-trend", analyticsController.GetSeverityTrend)
				analytics.GET("/vulnerability/monthly-trends", analyticsController.GetMonthlyVulnerabilityTrends)
				analytics.GET("/asset", analyticsController.GetAssetAnalytics)
				analytics.GET("/asset/risk", analyticsController.GetAssetRiskAssessment)
				analytics.GET("/report", analyticsController.GetReportAnalytics)
				analytics.GET("/user", analyticsController.GetUserAnalytics)
				analytics.POST("/export", analyticsController.ExportData)
			}

			// API测试工具
			apiTest := auth.Group("/api-test")
			apiTest.Use(requirePermission("api:test"))
			apiTest.Use(middleware.APIRateLimit()) // API测试速率限制
			{
				apiTest.POST("/test", apiTestController.TestAPI)
				apiTest.GET("/endpoints", apiTestController.GetAPIEndpoints)
				apiTest.POST("/token", apiTestController.GenerateAPIToken)
				apiTest.GET("/docs", apiTestController.GetAPIDocumentation)
				apiTest.POST("/validate", apiTestController.ValidateAPIRequest)
			}

			// 设置管理
			settings := auth.Group("/settings")
			{
				// 用户资料设置
				settings.PUT("/profile", settingsController.UpdateProfile)
				settings.PUT("/password", settingsController.ChangePassword)

				// 双因素认证
				settings.POST("/2fa/setup", settingsController.Setup2FA)
				settings.POST("/2fa/verify", settingsController.Verify2FA)
				settings.DELETE("/2fa", settingsController.Disable2FA)
				settings.GET("/2fa/status", settingsController.Get2FAStatus)

				// 系统设置
				settings.GET("/system", settingsController.GetSystemSettings)
				settings.PUT("/system", settingsController.UpdateSystemSettings)

				// 通知设置
				settings.GET("/notifications", settingsController.GetNotificationSettings)
				settings.PUT("/notifications", settingsController.UpdateNotificationSettings)

				// 数据管理
				settings.GET("/export", settingsController.ExportData)
				settings.POST("/import", settingsController.ImportData)
				settings.POST("/cache/clear", settingsController.ClearCache)
				settings.POST("/database/optimize", settingsController.OptimizeDatabase)
			}

			// AI助手管理
			ai := auth.Group("/ai")
			{
				// AI配置管理
				ai.GET("/config", aiController.GetAIConfiguration)
				ai.POST("/config", aiController.SaveAIConfiguration)
				ai.POST("/test", aiController.TestAIConnection)
				ai.GET("/providers", aiController.GetSupportedProviders)

				// AI对话管理
				ai.POST("/conversation", aiController.StartConversation)
				ai.POST("/chat", aiController.SendMessage)
				ai.GET("/conversations", aiController.GetConversations)
				ai.DELETE("/conversation/:session_id", aiController.DeleteConversation)

				// AI使用统计
				ai.GET("/stats", aiController.GetUsageStats)
			}
		}
	}



}
