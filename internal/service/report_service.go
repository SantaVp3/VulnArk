package service

import (
	"errors"
	"mime/multipart"
	"os"
	"path/filepath"
	"strings"
	"time"

	"vulnark/internal/model"
	"vulnark/internal/repository"
	"vulnark/pkg/storage"
)

// ReportService 报告服务接口
type ReportService interface {
	// 报告管理
	UploadReport(req *model.ReportUploadRequest, file *multipart.FileHeader, uploaderID uint) (*model.Report, error)
	GetReportByID(id uint) (*model.Report, error)
	GetReportContent(id uint) (content string, contentType string, err error)
	UpdateReport(id uint, req *model.ReportUpdateRequest) error
	DeleteReport(id uint) error
	GetReportList(req *model.ReportSearchRequest) (*model.PaginationResponse, error)
	GetReportStats() (*model.ReportStats, error)
	
	// 报告状态管理
	SubmitReport(id uint, userID uint) error
	ReviewReport(id uint, req *model.ReportReviewRequest, reviewerID uint) error
	ArchiveReport(id uint) error
	
	// 报告评论
	AddComment(reportID uint, req *model.ReportCommentRequest, userID uint) (*model.ReportComment, error)
	GetComments(reportID uint) ([]*model.ReportComment, error)
	DeleteComment(id uint, userID uint) error
	
	// 文件下载
	GetDownloadURL(id uint) (string, error)
}

// reportService 报告服务实现
type reportService struct {
	reportRepo        repository.ReportRepository
	commentRepo       repository.ReportCommentRepository
	attachmentRepo    repository.FileAttachmentRepository
	userRepo          repository.UserRepository
	assetRepo         repository.AssetRepository
	storageService    storage.StorageService
}

// NewReportService 创建报告服务
func NewReportService(reportRepo repository.ReportRepository, commentRepo repository.ReportCommentRepository, attachmentRepo repository.FileAttachmentRepository, userRepo repository.UserRepository, assetRepo repository.AssetRepository) ReportService {
	return &reportService{
		reportRepo:     reportRepo,
		commentRepo:    commentRepo,
		attachmentRepo: attachmentRepo,
		userRepo:       userRepo,
		assetRepo:      assetRepo,
		storageService: storage.NewStorageService(),
	}
}

// UploadReport 上传报告
func (s *reportService) UploadReport(req *model.ReportUploadRequest, file *multipart.FileHeader, uploaderID uint) (*model.Report, error) {
	// 验证文件类型
	if !storage.ValidateFileType(file.Filename, storage.GetAllowedReportTypes()) {
		return nil, errors.New("不支持的文件类型")
	}

	// 验证文件大小
	if !storage.ValidateFileSize(file.Size, storage.GetMaxReportSize()) {
		return nil, errors.New("文件大小超过限制")
	}

	// 验证上传者是否存在
	if _, err := s.userRepo.GetByID(uploaderID); err != nil {
		return nil, errors.New("上传者不存在")
	}

	// 验证关联的资产是否存在
	for _, assetID := range req.AssetIDs {
		if _, err := s.assetRepo.GetByID(assetID); err != nil {
			return nil, errors.New("关联的资产不存在")
		}
	}

	// 上传文件
	fileInfo, err := s.storageService.UploadFile(file, "reports")
	if err != nil {
		return nil, errors.New("文件上传失败: " + err.Error())
	}

	// 创建报告记录
	report := &model.Report{
		Title:       req.Title,
		Description: req.Description,
		Type:        req.Type,
		Severity:    req.Severity,
		Status:      model.ReportStatusDraft,
		AssetIDs:    req.AssetIDs,
		Tags:        req.Tags,
		FilePath:    fileInfo.FilePath,
		FileName:    fileInfo.OriginalName,
		FileSize:    fileInfo.FileSize,
		FileType:    fileInfo.FileType,
		DownloadURL: fileInfo.DownloadURL,
		UploaderID:  uploaderID,
		TestDate:    req.TestDate,
	}

	if err := s.reportRepo.Create(report); err != nil {
		// 如果数据库操作失败，删除已上传的文件
		s.storageService.DeleteFile(fileInfo.FilePath)
		return nil, errors.New("创建报告记录失败")
	}

	return report, nil
}

// GetReportByID 根据ID获取报告
func (s *reportService) GetReportByID(id uint) (*model.Report, error) {
	return s.reportRepo.GetByID(id)
}

// GetReportContent 获取报告内容
func (s *reportService) GetReportContent(id uint) (content string, contentType string, err error) {
	// 首先获取报告信息
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return "", "", err
	}

	// 检查文件是否存在
	if report.FilePath == "" {
		return "", "", errors.New("报告文件路径为空")
	}

	// 读取文件内容
	fileContent, err := os.ReadFile(report.FilePath)
	if err != nil {
		return "", "", errors.New("无法读取报告文件")
	}

	// 根据文件扩展名确定内容类型
	ext := strings.ToLower(filepath.Ext(report.FilePath))
	switch ext {
	case ".html", ".htm":
		contentType = "text/html; charset=utf-8"
	case ".txt":
		contentType = "text/plain; charset=utf-8"
	case ".pdf":
		contentType = "application/pdf"
	default:
		contentType = "application/octet-stream"
	}

	return string(fileContent), contentType, nil
}

// UpdateReport 更新报告
func (s *reportService) UpdateReport(id uint, req *model.ReportUpdateRequest) error {
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 只有草稿状态的报告才能修改
	if report.Status != model.ReportStatusDraft {
		return errors.New("只有草稿状态的报告才能修改")
	}

	// 验证关联的资产是否存在
	for _, assetID := range req.AssetIDs {
		if _, err := s.assetRepo.GetByID(assetID); err != nil {
			return errors.New("关联的资产不存在")
		}
	}

	// 更新字段
	if req.Title != "" {
		report.Title = req.Title
	}
	report.Description = req.Description
	if req.Type != "" {
		report.Type = req.Type
	}
	if req.Severity != "" {
		report.Severity = req.Severity
	}
	report.AssetIDs = req.AssetIDs
	report.Tags = req.Tags
	report.TestDate = req.TestDate

	return s.reportRepo.Update(report)
}

// DeleteReport 删除报告
func (s *reportService) DeleteReport(id uint) error {
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 删除文件
	if report.FilePath != "" {
		s.storageService.DeleteFile(report.FilePath)
	}

	// 删除数据库记录
	return s.reportRepo.Delete(id)
}

// GetReportList 获取报告列表
func (s *reportService) GetReportList(req *model.ReportSearchRequest) (*model.PaginationResponse, error) {
	reports, total, err := s.reportRepo.List(req)
	if err != nil {
		return nil, err
	}

	return model.NewPaginationResponse(total, req.Page, req.PageSize, reports), nil
}

// GetReportStats 获取报告统计
func (s *reportService) GetReportStats() (*model.ReportStats, error) {
	return s.reportRepo.GetStats()
}

// SubmitReport 提交报告
func (s *reportService) SubmitReport(id uint, userID uint) error {
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 只有上传者才能提交报告
	if report.UploaderID != userID {
		return errors.New("只有上传者才能提交报告")
	}

	// 只有草稿状态的报告才能提交
	if report.Status != model.ReportStatusDraft {
		return errors.New("只有草稿状态的报告才能提交")
	}

	return s.reportRepo.UpdateStatus(id, model.ReportStatusSubmitted)
}

// ReviewReport 审核报告
func (s *reportService) ReviewReport(id uint, req *model.ReportReviewRequest, reviewerID uint) error {
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 只有已提交的报告才能审核
	if report.Status != model.ReportStatusSubmitted {
		return errors.New("只有已提交的报告才能审核")
	}

	// 验证审核者是否存在
	if _, err := s.userRepo.GetByID(reviewerID); err != nil {
		return errors.New("审核者不存在")
	}

	// 更新报告信息
	report.ReviewerID = &reviewerID
	report.ReviewNotes = req.ReviewNotes
	report.Status = req.Status

	return s.reportRepo.Update(report)
}

// ArchiveReport 归档报告
func (s *reportService) ArchiveReport(id uint) error {
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 只有已通过的报告才能归档
	if report.Status != model.ReportStatusApproved {
		return errors.New("只有已通过的报告才能归档")
	}

	return s.reportRepo.UpdateStatus(id, model.ReportStatusArchived)
}

// AddComment 添加评论
func (s *reportService) AddComment(reportID uint, req *model.ReportCommentRequest, userID uint) (*model.ReportComment, error) {
	// 验证报告是否存在
	if _, err := s.reportRepo.GetByID(reportID); err != nil {
		return nil, err
	}

	// 验证用户是否存在
	if _, err := s.userRepo.GetByID(userID); err != nil {
		return nil, errors.New("用户不存在")
	}

	comment := &model.ReportComment{
		ReportID: reportID,
		Content:  req.Content,
		UserID:   userID,
	}

	if err := s.commentRepo.Create(comment); err != nil {
		return nil, errors.New("创建评论失败")
	}

	// 重新获取评论（包含关联数据）
	return s.getCommentByID(comment.ID)
}

// GetComments 获取评论列表
func (s *reportService) GetComments(reportID uint) ([]*model.ReportComment, error) {
	// 验证报告是否存在
	if _, err := s.reportRepo.GetByID(reportID); err != nil {
		return nil, err
	}

	return s.commentRepo.GetByReportID(reportID)
}

// DeleteComment 删除评论
func (s *reportService) DeleteComment(id uint, userID uint) error {
	// TODO: 实现获取评论详情的方法来验证权限
	// 目前简化处理，直接删除
	return s.commentRepo.Delete(id)
}

// GetDownloadURL 获取下载URL
func (s *reportService) GetDownloadURL(id uint) (string, error) {
	report, err := s.reportRepo.GetByID(id)
	if err != nil {
		return "", err
	}

	if report.FilePath == "" {
		return "", errors.New("文件不存在")
	}

	// 生成预签名URL（有效期1小时）
	return s.storageService.GeneratePresignedURL(report.FilePath, time.Hour)
}

// getCommentByID 根据ID获取评论（内部方法）
func (s *reportService) getCommentByID(id uint) (*model.ReportComment, error) {
	// 这里需要实现一个根据ID获取评论的方法
	// 为了简化，暂时返回nil
	return nil, nil
}
