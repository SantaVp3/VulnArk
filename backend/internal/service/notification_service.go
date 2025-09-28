package service

import (
	"errors"
	"fmt"
	"strings"

	"vulnark/internal/config"
	"vulnark/internal/model"
	"vulnark/internal/repository"
	"vulnark/pkg/email"
)

// NotificationService 通知服务接口
type NotificationService interface {
	// 通知管理
	CreateNotification(req *model.NotificationCreateRequest, senderID *uint) (*model.Notification, error)
	GetNotificationByID(id uint) (*model.Notification, error)
	GetNotificationList(req *model.NotificationSearchRequest) (*model.PaginationResponse, error)
	GetUserNotifications(userID uint, limit int) ([]*model.Notification, error)
	MarkAsRead(id uint, userID uint) error
	MarkAllAsRead(userID uint) error
	DeleteNotification(id uint, userID uint) error
	GetNotificationStats(userID uint) (*model.NotificationStats, error)
	
	// 邮件模板管理
	CreateEmailTemplate(req *model.EmailTemplateCreateRequest, creatorID uint) (*model.EmailTemplate, error)
	GetEmailTemplateByID(id uint) (*model.EmailTemplate, error)
	GetEmailTemplateByName(name string) (*model.EmailTemplate, error)
	UpdateEmailTemplate(id uint, req *model.EmailTemplateUpdateRequest) error
	DeleteEmailTemplate(id uint) error
	GetEmailTemplateList() ([]*model.EmailTemplate, error)
	
	// 邮件发送
	SendEmail(req *model.EmailSendRequest, senderID *uint) error
	SendTemplateEmail(templateName string, to []string, variables map[string]interface{}, senderID *uint) error
	
	// 通知设置
	GetNotificationSetting(userID uint) (*model.NotificationSetting, error)
	UpdateNotificationSetting(userID uint, req *model.NotificationSettingUpdateRequest) error
	
	// 系统通知
	NotifyVulnerabilityAssigned(vulnID uint, assigneeID uint, assignerID uint) error
	NotifyReportSubmitted(reportID uint, reviewerIDs []uint) error
	NotifyReportReviewed(reportID uint, submitterID uint, reviewerID uint) error
	NotifySystemMaintenance(userIDs []uint, maintenanceInfo map[string]interface{}) error
	
	// 清理任务
	CleanupExpiredNotifications() error
}

// notificationService 通知服务实现
type notificationService struct {
	notificationRepo repository.NotificationRepository
	templateRepo     repository.EmailTemplateRepository
	logRepo          repository.EmailLogRepository
	settingRepo      repository.NotificationSettingRepository
	userRepo         repository.UserRepository
	vulnRepo         repository.VulnerabilityRepository
	reportRepo       repository.ReportRepository
	emailService     email.EmailService
}

// NewNotificationService 创建通知服务
func NewNotificationService(notificationRepo repository.NotificationRepository, templateRepo repository.EmailTemplateRepository, logRepo repository.EmailLogRepository, settingRepo repository.NotificationSettingRepository, userRepo repository.UserRepository, vulnRepo repository.VulnerabilityRepository, reportRepo repository.ReportRepository) NotificationService {
	return &notificationService{
		notificationRepo: notificationRepo,
		templateRepo:     templateRepo,
		logRepo:          logRepo,
		settingRepo:      settingRepo,
		userRepo:         userRepo,
		vulnRepo:         vulnRepo,
		reportRepo:       reportRepo,
		emailService:     email.NewEmailService(),
	}
}

// CreateNotification 创建通知
func (s *notificationService) CreateNotification(req *model.NotificationCreateRequest, senderID *uint) (*model.Notification, error) {
	// 验证接收人是否存在
	if _, err := s.userRepo.GetByID(req.RecipientID); err != nil {
		return nil, errors.New("接收人不存在")
	}

	// 验证发送人是否存在（如果指定）
	if senderID != nil {
		if _, err := s.userRepo.GetByID(*senderID); err != nil {
			return nil, errors.New("发送人不存在")
		}
	}

	notification := &model.Notification{
		Title:       req.Title,
		Content:     req.Content,
		Type:        req.Type,
		Level:       req.Level,
		RecipientID: req.RecipientID,
		SenderID:    senderID,
		Data:        req.Data,
		EntityType:  req.EntityType,
		EntityID:    req.EntityID,
		ExpiresAt:   req.ExpiresAt,
	}

	if err := s.notificationRepo.Create(notification); err != nil {
		return nil, errors.New("创建通知失败")
	}

	return notification, nil
}

// GetNotificationByID 根据ID获取通知
func (s *notificationService) GetNotificationByID(id uint) (*model.Notification, error) {
	return s.notificationRepo.GetByID(id)
}

// GetNotificationList 获取通知列表
func (s *notificationService) GetNotificationList(req *model.NotificationSearchRequest) (*model.PaginationResponse, error) {
	notifications, total, err := s.notificationRepo.List(req)
	if err != nil {
		return nil, err
	}

	return model.NewPaginationResponse(total, req.Page, req.PageSize, notifications), nil
}

// GetUserNotifications 获取用户通知列表
func (s *notificationService) GetUserNotifications(userID uint, limit int) ([]*model.Notification, error) {
	return s.notificationRepo.GetByRecipientID(userID, limit)
}

// MarkAsRead 标记为已读
func (s *notificationService) MarkAsRead(id uint, userID uint) error {
	// 验证通知是否属于当前用户
	notification, err := s.notificationRepo.GetByID(id)
	if err != nil {
		return err
	}

	if notification.RecipientID != userID {
		return errors.New("无权限操作此通知")
	}

	return s.notificationRepo.MarkAsRead(id)
}

// MarkAllAsRead 标记所有通知为已读
func (s *notificationService) MarkAllAsRead(userID uint) error {
	return s.notificationRepo.MarkAllAsRead(userID)
}

// DeleteNotification 删除通知
func (s *notificationService) DeleteNotification(id uint, userID uint) error {
	// 验证通知是否属于当前用户
	notification, err := s.notificationRepo.GetByID(id)
	if err != nil {
		return err
	}

	if notification.RecipientID != userID {
		return errors.New("无权限删除此通知")
	}

	return s.notificationRepo.Delete(id)
}

// GetNotificationStats 获取通知统计
func (s *notificationService) GetNotificationStats(userID uint) (*model.NotificationStats, error) {
	return s.notificationRepo.GetStats(userID)
}

// CreateEmailTemplate 创建邮件模板
func (s *notificationService) CreateEmailTemplate(req *model.EmailTemplateCreateRequest, creatorID uint) (*model.EmailTemplate, error) {
	// 验证创建者是否存在
	if _, err := s.userRepo.GetByID(creatorID); err != nil {
		return nil, errors.New("创建者不存在")
	}

	template := &model.EmailTemplate{
		Name:      req.Name,
		Subject:   req.Subject,
		Content:   req.Content,
		Type:      req.Type,
		Variables: req.Variables,
		CreatorID: creatorID,
	}

	if err := s.templateRepo.Create(template); err != nil {
		if strings.Contains(err.Error(), "Duplicate entry") {
			return nil, errors.New("模板名称已存在")
		}
		return nil, errors.New("创建邮件模板失败")
	}

	return template, nil
}

// GetEmailTemplateByID 根据ID获取邮件模板
func (s *notificationService) GetEmailTemplateByID(id uint) (*model.EmailTemplate, error) {
	return s.templateRepo.GetByID(id)
}

// GetEmailTemplateByName 根据名称获取邮件模板
func (s *notificationService) GetEmailTemplateByName(name string) (*model.EmailTemplate, error) {
	return s.templateRepo.GetByName(name)
}

// UpdateEmailTemplate 更新邮件模板
func (s *notificationService) UpdateEmailTemplate(id uint, req *model.EmailTemplateUpdateRequest) error {
	template, err := s.templateRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 更新字段
	if req.Name != "" {
		template.Name = req.Name
	}
	if req.Subject != "" {
		template.Subject = req.Subject
	}
	if req.Content != "" {
		template.Content = req.Content
	}
	if req.Type != "" {
		template.Type = req.Type
	}
	if req.Variables != "" {
		template.Variables = req.Variables
	}
	if req.IsActive != nil {
		template.IsActive = *req.IsActive
	}

	return s.templateRepo.Update(template)
}

// DeleteEmailTemplate 删除邮件模板
func (s *notificationService) DeleteEmailTemplate(id uint) error {
	// 检查模板是否存在
	if _, err := s.templateRepo.GetByID(id); err != nil {
		return err
	}

	return s.templateRepo.Delete(id)
}

// GetEmailTemplateList 获取邮件模板列表
func (s *notificationService) GetEmailTemplateList() ([]*model.EmailTemplate, error) {
	return s.templateRepo.List()
}

// SendEmail 发送邮件
func (s *notificationService) SendEmail(req *model.EmailSendRequest, senderID *uint) error {
	// 验证邮箱地址
	for _, email := range req.To {
		if !s.emailService.ValidateEmailAddress(email) {
			return fmt.Errorf("无效的邮箱地址: %s", email)
		}
	}

	// 创建邮件日志
	emailLog := &model.EmailLog{
		To:         strings.Join(req.To, ","),
		Cc:         strings.Join(req.Cc, ","),
		Bcc:        strings.Join(req.Bcc, ","),
		Subject:    req.Subject,
		Content:    req.Content,
		TemplateID: req.TemplateID,
		Status:     model.EmailStatusPending,
		SenderID:   senderID,
	}

	if err := s.logRepo.Create(emailLog); err != nil {
		return errors.New("创建邮件日志失败")
	}

	// 发送邮件
	var err error
	if req.TemplateID != nil && len(req.Variables) > 0 {
		// 使用模板发送
		template, templateErr := s.templateRepo.GetByID(*req.TemplateID)
		if templateErr != nil {
			s.logRepo.UpdateStatus(emailLog.ID, model.EmailStatusFailed, "模板不存在")
			return templateErr
		}
		err = s.emailService.SendTemplateEmail(req.To, req.Cc, req.Bcc, template.Subject, template.Content, req.Variables)
	} else {
		// 直接发送
		err = s.emailService.SendEmail(req.To, req.Cc, req.Bcc, req.Subject, req.Content)
	}

	// 更新邮件状态
	if err != nil {
		s.logRepo.UpdateStatus(emailLog.ID, model.EmailStatusFailed, err.Error())
		return fmt.Errorf("邮件发送失败: %v", err)
	} else {
		s.logRepo.UpdateStatus(emailLog.ID, model.EmailStatusSent, "")
	}

	return nil
}

// SendTemplateEmail 发送模板邮件
func (s *notificationService) SendTemplateEmail(templateName string, to []string, variables map[string]interface{}, senderID *uint) error {
	// 获取模板
	template, err := s.templateRepo.GetByName(templateName)
	if err != nil {
		return err
	}

	// 构建发送请求
	req := &model.EmailSendRequest{
		To:         to,
		Subject:    template.Subject,
		Content:    template.Content,
		TemplateID: &template.ID,
		Variables:  variables,
	}

	return s.SendEmail(req, senderID)
}

// GetNotificationSetting 获取通知设置
func (s *notificationService) GetNotificationSetting(userID uint) (*model.NotificationSetting, error) {
	return s.settingRepo.GetByUserID(userID)
}

// UpdateNotificationSetting 更新通知设置
func (s *notificationService) UpdateNotificationSetting(userID uint, req *model.NotificationSettingUpdateRequest) error {
	setting, err := s.settingRepo.GetByUserID(userID)
	if err != nil {
		return err
	}

	// 更新字段
	if req.EmailEnabled != nil {
		setting.EmailEnabled = *req.EmailEnabled
	}
	if req.SystemEnabled != nil {
		setting.SystemEnabled = *req.SystemEnabled
	}
	if req.VulnerabilityAssigned != nil {
		setting.VulnerabilityAssigned = *req.VulnerabilityAssigned
	}
	if req.VulnerabilityUpdated != nil {
		setting.VulnerabilityUpdated = *req.VulnerabilityUpdated
	}
	if req.ReportSubmitted != nil {
		setting.ReportSubmitted = *req.ReportSubmitted
	}
	if req.ReportReviewed != nil {
		setting.ReportReviewed = *req.ReportReviewed
	}
	if req.SystemMaintenance != nil {
		setting.SystemMaintenance = *req.SystemMaintenance
	}
	if req.SecurityAlert != nil {
		setting.SecurityAlert = *req.SecurityAlert
	}

	return s.settingRepo.CreateOrUpdate(setting)
}

// NotifyVulnerabilityAssigned 通知漏洞分配
func (s *notificationService) NotifyVulnerabilityAssigned(vulnID uint, assigneeID uint, assignerID uint) error {
	// 获取漏洞信息
	vulnerability, err := s.vulnRepo.GetByID(vulnID)
	if err != nil {
		return err
	}

	// 获取分配人信息
	assignee, err := s.userRepo.GetByID(assigneeID)
	if err != nil {
		return err
	}

	// 获取分配者信息
	assigner, err := s.userRepo.GetByID(assignerID)
	if err != nil {
		return err
	}

	// 检查通知设置
	setting, err := s.settingRepo.GetByUserID(assigneeID)
	if err != nil || !setting.VulnerabilityAssigned {
		return nil // 用户关闭了此类通知
	}

	// 创建系统通知
	if setting.SystemEnabled {
		notificationReq := &model.NotificationCreateRequest{
			Title:       fmt.Sprintf("漏洞分配通知 - %s", vulnerability.Title),
			Content:     fmt.Sprintf("您有一个新的漏洞需要处理：%s（严重程度：%d级）", vulnerability.Title, vulnerability.SeverityLevel),
			Type:        model.NotificationTypeVulnerability,
			Level:       model.NotificationLevelInfo,
			RecipientID: assigneeID,
			EntityType:  "vulnerability",
			EntityID:    &vulnID,
		}
		s.CreateNotification(notificationReq, &assignerID)
	}

	// 发送邮件通知
	if setting.EmailEnabled && assignee.Email != "" {
		variables := map[string]interface{}{
			"AssigneeName":        assignee.RealName,
			"VulnerabilityTitle":  vulnerability.Title,
			"Severity":            fmt.Sprintf("%d级", vulnerability.SeverityLevel),
			"DiscoveredAt":        vulnerability.CreatedAt.Format("2006-01-02 15:04:05"),
			"AssignerName":        assigner.RealName,
			"SystemURL":           fmt.Sprintf("http://%s:%s", config.AppConfig.Server.Host, config.AppConfig.Server.Port),
		}
		s.SendTemplateEmail("vulnerability_assigned", []string{assignee.Email}, variables, &assignerID)
	}

	return nil
}

// NotifyReportSubmitted 通知报告提交
func (s *notificationService) NotifyReportSubmitted(reportID uint, reviewerIDs []uint) error {
	// 获取报告信息
	report, err := s.reportRepo.GetByID(reportID)
	if err != nil {
		return err
	}

	// 获取提交人信息
	submitter, err := s.userRepo.GetByID(report.UploaderID)
	if err != nil {
		return err
	}

	// 通知所有审核人
	for _, reviewerID := range reviewerIDs {
		reviewer, err := s.userRepo.GetByID(reviewerID)
		if err != nil {
			continue
		}

		// 检查通知设置
		setting, err := s.settingRepo.GetByUserID(reviewerID)
		if err != nil || !setting.ReportSubmitted {
			continue
		}

		// 创建系统通知
		if setting.SystemEnabled {
			notificationReq := &model.NotificationCreateRequest{
				Title:       fmt.Sprintf("报告提交通知 - %s", report.Title),
				Content:     fmt.Sprintf("有一个新的报告等待您的审核：%s（类型：%s）", report.Title, report.GetTypeText()),
				Type:        model.NotificationTypeReport,
				Level:       model.NotificationLevelInfo,
				RecipientID: reviewerID,
				EntityType:  "report",
				EntityID:    &reportID,
			}
			s.CreateNotification(notificationReq, &report.UploaderID)
		}

		// 发送邮件通知
		if setting.EmailEnabled && reviewer.Email != "" {
			variables := map[string]interface{}{
				"ReportTitle":     report.Title,
				"ReportType":      report.GetTypeText(),
				"Severity":        report.GetSeverityText(),
				"SubmitterName":   submitter.RealName,
				"SubmittedAt":     report.SubmittedAt.Format("2006-01-02 15:04:05"),
				"SystemURL":       "http://localhost:8080",
			}
			s.SendTemplateEmail("report_submitted", []string{reviewer.Email}, variables, &report.UploaderID)
		}
	}

	return nil
}

// NotifyReportReviewed 通知报告审核结果
func (s *notificationService) NotifyReportReviewed(reportID uint, submitterID uint, reviewerID uint) error {
	// 获取报告信息
	report, err := s.reportRepo.GetByID(reportID)
	if err != nil {
		return err
	}

	// 获取提交人信息
	submitter, err := s.userRepo.GetByID(submitterID)
	if err != nil {
		return err
	}

	// 获取审核人信息
	reviewer, err := s.userRepo.GetByID(reviewerID)
	if err != nil {
		return err
	}

	// 检查通知设置
	setting, err := s.settingRepo.GetByUserID(submitterID)
	if err != nil || !setting.ReportReviewed {
		return nil
	}

	// 创建系统通知
	if setting.SystemEnabled {
		notificationReq := &model.NotificationCreateRequest{
			Title:       fmt.Sprintf("报告审核结果通知 - %s", report.Title),
			Content:     fmt.Sprintf("您的报告已完成审核：%s（结果：%s）", report.Title, report.GetStatusText()),
			Type:        model.NotificationTypeReport,
			Level:       model.NotificationLevelInfo,
			RecipientID: submitterID,
			EntityType:  "report",
			EntityID:    &reportID,
		}
		s.CreateNotification(notificationReq, &reviewerID)
	}

	// 发送邮件通知
	if setting.EmailEnabled && submitter.Email != "" {
		variables := map[string]interface{}{
			"SubmitterName": submitter.RealName,
			"ReportTitle":   report.Title,
			"ReviewResult":  report.GetStatusText(),
			"ReviewerName":  reviewer.RealName,
			"ReviewedAt":    report.ReviewedAt.Format("2006-01-02 15:04:05"),
			"ReviewNotes":   report.ReviewNotes,
			"SystemURL":     "http://localhost:8080",
		}
		s.SendTemplateEmail("report_reviewed", []string{submitter.Email}, variables, &reviewerID)
	}

	return nil
}

// NotifySystemMaintenance 通知系统维护
func (s *notificationService) NotifySystemMaintenance(userIDs []uint, maintenanceInfo map[string]interface{}) error {
	for _, userID := range userIDs {
		user, err := s.userRepo.GetByID(userID)
		if err != nil {
			continue
		}

		// 检查通知设置
		setting, err := s.settingRepo.GetByUserID(userID)
		if err != nil || !setting.SystemMaintenance {
			continue
		}

		// 创建系统通知
		if setting.SystemEnabled {
			notificationReq := &model.NotificationCreateRequest{
				Title:       "系统维护通知",
				Content:     fmt.Sprintf("系统将于 %v 进行维护，预计时长 %v", maintenanceInfo["MaintenanceTime"], maintenanceInfo["Duration"]),
				Type:        model.NotificationTypeMaintenance,
				Level:       model.NotificationLevelWarning,
				RecipientID: userID,
			}
			s.CreateNotification(notificationReq, nil)
		}

		// 发送邮件通知
		if setting.EmailEnabled && user.Email != "" {
			s.SendTemplateEmail("system_maintenance", []string{user.Email}, maintenanceInfo, nil)
		}
	}

	return nil
}

// CleanupExpiredNotifications 清理过期通知
func (s *notificationService) CleanupExpiredNotifications() error {
	return s.notificationRepo.DeleteExpired()
}
