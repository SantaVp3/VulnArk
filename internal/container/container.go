package container

import (
	"gorm.io/gorm"
	"vulnark/internal/controller"
	"vulnark/internal/repository"
	"vulnark/internal/service"
	"vulnark/pkg/database"
	"vulnark/pkg/storage"
)

// Container 依赖注入容器
type Container struct {
	DB *gorm.DB
	
	// Storage
	StorageService storage.StorageService
	
	// Repositories
	UserRepo           repository.UserRepository
	RoleRepo           repository.RoleRepository
	AssetRepo          repository.AssetRepository
	VulnRepo           repository.VulnerabilityRepository
	OWASPRepo          repository.OWASPCategoryRepository
	AnalyticsRepo      repository.AnalyticsRepository
	AssignmentRepo     repository.AssignmentRepository
	KnowledgeRepo      repository.KnowledgeRepository
	ReportRepo         repository.ReportRepository
	NotificationRepo   repository.NotificationRepository
	PermissionRepo     repository.PermissionRepository
	SettingsRepo       repository.SettingsRepository
	AIRepo             repository.AIRepository
	TagRepo            repository.TagRepository
	TemplateRepo       repository.TemplateRepository
	AssignmentRuleRepo repository.AssignmentRuleRepository
	TimelineRepo       repository.TimelineRepository
	RolePermissionRepo repository.RolePermissionRepository
	SystemConfigRepo   repository.SystemConfigRepository
	AuditLogRepo       repository.AuditLogRepository
	EmailTemplateRepo  repository.EmailTemplateRepository
	EmailLogRepo       repository.EmailLogRepository
	NotificationSettingRepo repository.NotificationSettingRepository
	ReportCommentRepo  repository.ReportCommentRepository
	FileAttachmentRepo repository.FileAttachmentRepository
	
	// Services
	UserService        service.UserService
	RoleService        service.RoleService
	AssetService       service.AssetService
	VulnService        service.VulnerabilityService
	AnalyticsService   service.AnalyticsService
	AssignmentService  service.AssignmentService
	KnowledgeService   service.KnowledgeService
	ReportService      service.ReportService
	NotificationService service.NotificationService
	PermissionService  service.PermissionService
	SettingsService    service.SettingsService
	AIService          service.AIService
	AuditLogService    service.AuditLogService
	SystemConfigService service.SystemConfigService
	TagService         service.TagService
	TemplateService    service.TemplateService
	
	// Controllers
	UserController        *controller.UserController
	AssetController       *controller.AssetController
	VulnController        *controller.VulnerabilityController
	RoleController        *controller.RoleController
	AnalyticsController   *controller.AnalyticsController
	AssignmentController  *controller.AssignmentController
	KnowledgeController   *controller.KnowledgeController
	ReportController      *controller.ReportController
	NotificationController *controller.NotificationController
	PermissionController  *controller.PermissionController
	SettingsController    *controller.SettingsController
	SystemController      *controller.SystemController
	AIController          *controller.AIController
	APITestController     *controller.APITestController
	APIVersionController  *controller.APIVersionController
}

// NewContainer 创建新的依赖注入容器
func NewContainer() *Container {
	container := &Container{}
	
	// 初始化数据库连接
	container.DB = database.GetDB()
	
	// 初始化存储服务
	container.StorageService = storage.NewLocalStorageService()
	
	// 初始化 Repositories
	container.initRepositories()
	
	// 初始化 Services
	container.initServices()
	
	// 初始化 Controllers
	container.initControllers()
	
	return container
}

// initRepositories 初始化所有Repository
func (c *Container) initRepositories() {
	c.UserRepo = repository.NewUserRepository(c.DB)
	c.RoleRepo = repository.NewRoleRepository(c.DB)
	c.AssetRepo = repository.NewAssetRepository(c.DB)
	c.VulnRepo = repository.NewVulnerabilityRepository(c.DB)
	c.OWASPRepo = repository.NewOWASPCategoryRepository(c.DB)
	c.AnalyticsRepo = repository.NewAnalyticsRepository(c.DB)
	c.AssignmentRepo = repository.NewAssignmentRepository(c.DB)
	c.KnowledgeRepo = repository.NewKnowledgeRepository(c.DB)
	c.ReportRepo = repository.NewReportRepository(c.DB)
	c.NotificationRepo = repository.NewNotificationRepository(c.DB)
	c.PermissionRepo = repository.NewPermissionRepository(c.DB)
	c.SettingsRepo = repository.NewSettingsRepository(c.DB)
	c.AIRepo = repository.NewAIRepository(c.DB)
	c.TagRepo = repository.NewTagRepository(c.DB)
	c.TemplateRepo = repository.NewTemplateRepository(c.DB)
	c.AssignmentRuleRepo = repository.NewAssignmentRuleRepository(c.DB)
	c.TimelineRepo = repository.NewTimelineRepository(c.DB)
	c.RolePermissionRepo = repository.NewRolePermissionRepository(c.DB)
	c.SystemConfigRepo = repository.NewSystemConfigRepository(c.DB)
	c.AuditLogRepo = repository.NewAuditLogRepository(c.DB)
	c.EmailTemplateRepo = repository.NewEmailTemplateRepository(c.DB)
	c.EmailLogRepo = repository.NewEmailLogRepository(c.DB)
	c.NotificationSettingRepo = repository.NewNotificationSettingRepository(c.DB)
	c.ReportCommentRepo = repository.NewReportCommentRepository(c.DB)
	c.FileAttachmentRepo = repository.NewFileAttachmentRepository(c.DB)
}

// initServices 初始化所有Service
func (c *Container) initServices() {
	c.UserService = service.NewUserService(c.UserRepo, c.RoleRepo)
	c.RoleService = service.NewRoleService(c.RoleRepo)
	c.AssetService = service.NewAssetService(c.AssetRepo, c.UserRepo)
	c.VulnService = service.NewVulnerabilityService(c.VulnRepo, c.AssetRepo, c.UserRepo, c.OWASPRepo)
	c.AnalyticsService = service.NewAnalyticsService(c.AnalyticsRepo)
	c.AssignmentService = service.NewAssignmentService(c.AssignmentRepo, c.AssignmentRuleRepo, c.TimelineRepo, c.VulnRepo, c.UserRepo, c.AssetRepo)
	c.KnowledgeService = service.NewKnowledgeService(c.KnowledgeRepo, c.TagRepo, c.UserRepo)
	c.ReportService = service.NewReportService(c.ReportRepo, c.ReportCommentRepo, c.FileAttachmentRepo, c.UserRepo, c.AssetRepo)
	c.NotificationService = service.NewNotificationService(c.NotificationRepo, c.EmailTemplateRepo, c.EmailLogRepo, c.NotificationSettingRepo, c.UserRepo, c.VulnRepo, c.ReportRepo)
	c.PermissionService = service.NewPermissionService(c.PermissionRepo, c.RolePermissionRepo, c.RoleRepo, c.UserRepo)
	c.SettingsService = service.NewSettingsService(c.UserRepo, c.SettingsRepo)
	c.AIService = service.NewAIService(c.AIRepo)
	c.AuditLogService = service.NewAuditLogService(c.AuditLogRepo)
	c.SystemConfigService = service.NewSystemConfigService(c.SystemConfigRepo)
	c.TagService = service.NewTagService(c.TagRepo)
	c.TemplateService = service.NewTemplateService(c.TemplateRepo)
}

// initControllers 初始化所有Controller
func (c *Container) initControllers() {
	c.UserController = controller.NewUserController(c.UserService, c.RoleService)
	c.AssetController = controller.NewAssetController(c.AssetService)
	c.VulnController = controller.NewVulnerabilityController(c.VulnService, c.StorageService)
	c.RoleController = controller.NewRoleController(c.RoleService)
	c.AnalyticsController = controller.NewAnalyticsController(c.AnalyticsService)
	c.AssignmentController = controller.NewAssignmentController(c.AssignmentService)
	c.KnowledgeController = controller.NewKnowledgeController(c.KnowledgeService, c.TagService, c.TemplateService)
	c.ReportController = controller.NewReportController(c.ReportService)
	c.NotificationController = controller.NewNotificationController(c.NotificationService)
	c.PermissionController = controller.NewPermissionController(c.PermissionService)
	c.SettingsController = controller.NewSettingsController(c.SettingsService)
	c.SystemController = controller.NewSystemController(c.SystemConfigService, c.AuditLogService)
	c.AIController = controller.NewAIController(c.AIService)
	c.APITestController = controller.NewAPITestController()
	c.APIVersionController = controller.NewAPIVersionController()
}