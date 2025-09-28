package repository

import (
	"errors"
	"strconv"
	"time"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// ReportRepository 报告仓储接口
type ReportRepository interface {
	Create(report *model.Report) error
	GetByID(id uint) (*model.Report, error)
	Update(report *model.Report) error
	Delete(id uint) error
	List(req *model.ReportSearchRequest) ([]*model.Report, int64, error)
	GetStats() (*model.ReportStats, error)
	UpdateStatus(id uint, status string) error
	GetByUploaderID(uploaderID uint) ([]*model.Report, error)
}

// reportRepository 报告仓储实现
type reportRepository struct {
	db *gorm.DB
}

// NewReportRepository 创建报告仓储
func NewReportRepository(db *gorm.DB) ReportRepository {
	return &reportRepository{
		db: db,
	}
}

// Create 创建报告
func (r *reportRepository) Create(report *model.Report) error {
	return r.db.Create(report).Error
}

// GetByID 根据ID获取报告
func (r *reportRepository) GetByID(id uint) (*model.Report, error) {
	var report model.Report
	err := r.db.Preload("Uploader").Preload("Reviewer").First(&report, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("报告不存在")
		}
		return nil, err
	}
	return &report, nil
}

// Update 更新报告
func (r *reportRepository) Update(report *model.Report) error {
	return r.db.Save(report).Error
}

// Delete 删除报告
func (r *reportRepository) Delete(id uint) error {
	return r.db.Delete(&model.Report{}, id).Error
}

// List 获取报告列表
func (r *reportRepository) List(req *model.ReportSearchRequest) ([]*model.Report, int64, error) {
	var reports []*model.Report
	var total int64

	query := r.db.Model(&model.Report{}).Preload("Uploader").Preload("Reviewer")

	// 搜索条件
	if req.Keyword != "" {
		query = query.Where("title LIKE ? OR description LIKE ?", "%"+req.Keyword+"%", "%"+req.Keyword+"%")
	}

	if req.Type != "" {
		query = query.Where("type = ?", req.Type)
	}

	if req.Severity != "" {
		query = query.Where("severity = ?", req.Severity)
	}

	if req.Status != "" {
		query = query.Where("status = ?", req.Status)
	}

	if req.UploaderID != "" {
		if uploaderID, err := strconv.ParseUint(req.UploaderID, 10, 32); err == nil {
			query = query.Where("uploader_id = ?", uint(uploaderID))
		}
	}

	if req.AssetID != "" {
		if assetID, err := strconv.ParseUint(req.AssetID, 10, 32); err == nil {
			query = query.Where("JSON_CONTAINS(asset_ids, ?)", assetID)
		}
	}

	if req.Tag != "" {
		query = query.Where("JSON_CONTAINS(tags, ?)", `"`+req.Tag+`"`)
	}

	// 日期范围
	if req.StartDate != "" {
		if startDate, err := time.Parse("2006-01-02", req.StartDate); err == nil {
			query = query.Where("created_at >= ?", startDate)
		}
	}

	if req.EndDate != "" {
		if endDate, err := time.Parse("2006-01-02", req.EndDate); err == nil {
			query = query.Where("created_at <= ?", endDate.Add(24*time.Hour))
		}
	}

	// 获取总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 分页
	offset := (req.Page - 1) * req.PageSize
	if err := query.Offset(offset).Limit(req.PageSize).Order("created_at DESC").Find(&reports).Error; err != nil {
		return nil, 0, err
	}

	return reports, total, nil
}

// GetStats 获取报告统计
func (r *reportRepository) GetStats() (*model.ReportStats, error) {
	stats := &model.ReportStats{
		ByType:     make(map[string]int64),
		BySeverity: make(map[string]int64),
		ByStatus:   make(map[string]int64),
	}

	// 总数
	if err := r.db.Model(&model.Report{}).Count(&stats.Total).Error; err != nil {
		return nil, err
	}

	// 按类型统计
	var typeStats []struct {
		Type  string
		Count int64
	}
	if err := r.db.Model(&model.Report{}).
		Select("type, COUNT(*) as count").
		Group("type").
		Scan(&typeStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range typeStats {
		stats.ByType[stat.Type] = stat.Count
	}

	// 按严重程度统计
	var severityStats []struct {
		Severity string
		Count    int64
	}
	if err := r.db.Model(&model.Report{}).
		Select("severity, COUNT(*) as count").
		Group("severity").
		Scan(&severityStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range severityStats {
		stats.BySeverity[stat.Severity] = stat.Count
	}

	// 按状态统计
	var statusStats []struct {
		Status string
		Count  int64
	}
	if err := r.db.Model(&model.Report{}).
		Select("status, COUNT(*) as count").
		Group("status").
		Scan(&statusStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range statusStats {
		stats.ByStatus[stat.Status] = stat.Count
	}

	// 总文件大小
	var totalSize struct {
		TotalSize int64
	}
	if err := r.db.Model(&model.Report{}).
		Select("SUM(file_size) as total_size").
		Scan(&totalSize).Error; err != nil {
		return nil, err
	}
	stats.TotalSize = totalSize.TotalSize

	// 最近报告
	var recentReports []model.ReportSummary
	if err := r.db.Table("reports r").
		Select("r.id, r.title, r.type, r.severity, r.status, r.file_size, r.created_at, u.real_name as uploader_name").
		Joins("LEFT JOIN users u ON r.uploader_id = u.id").
		Order("r.created_at DESC").
		Limit(10).
		Scan(&recentReports).Error; err != nil {
		return nil, err
	}
	stats.RecentReports = recentReports

	return stats, nil
}

// UpdateStatus 更新报告状态
func (r *reportRepository) UpdateStatus(id uint, status string) error {
	updates := map[string]interface{}{
		"status":     status,
		"updated_at": time.Now(),
	}

	// 根据状态设置相应的时间戳
	switch status {
	case model.ReportStatusSubmitted:
		updates["submitted_at"] = time.Now()
	case model.ReportStatusReviewing:
		// 不需要额外时间戳
	case model.ReportStatusApproved, model.ReportStatusRejected:
		updates["reviewed_at"] = time.Now()
	}

	return r.db.Model(&model.Report{}).Where("id = ?", id).Updates(updates).Error
}

// GetByUploaderID 根据上传者ID获取报告列表
func (r *reportRepository) GetByUploaderID(uploaderID uint) ([]*model.Report, error) {
	var reports []*model.Report
	err := r.db.Preload("Uploader").Preload("Reviewer").
		Where("uploader_id = ?", uploaderID).
		Order("created_at DESC").
		Find(&reports).Error
	return reports, err
}

// ReportCommentRepository 报告评论仓储接口
type ReportCommentRepository interface {
	Create(comment *model.ReportComment) error
	GetByReportID(reportID uint) ([]*model.ReportComment, error)
	Delete(id uint) error
}

// reportCommentRepository 报告评论仓储实现
type reportCommentRepository struct {
	db *gorm.DB
}

// NewReportCommentRepository 创建报告评论仓储
func NewReportCommentRepository(db *gorm.DB) ReportCommentRepository {
	return &reportCommentRepository{
		db: db,
	}
}

// Create 创建评论
func (r *reportCommentRepository) Create(comment *model.ReportComment) error {
	return r.db.Create(comment).Error
}

// GetByReportID 根据报告ID获取评论列表
func (r *reportCommentRepository) GetByReportID(reportID uint) ([]*model.ReportComment, error) {
	var comments []*model.ReportComment
	err := r.db.Preload("User").
		Where("report_id = ?", reportID).
		Order("created_at ASC").
		Find(&comments).Error
	return comments, err
}

// Delete 删除评论
func (r *reportCommentRepository) Delete(id uint) error {
	return r.db.Delete(&model.ReportComment{}, id).Error
}

// FileAttachmentRepository 文件附件仓储接口
type FileAttachmentRepository interface {
	Create(attachment *model.FileAttachment) error
	GetByID(id uint) (*model.FileAttachment, error)
	GetByEntityID(entityType string, entityID uint) ([]*model.FileAttachment, error)
	Delete(id uint) error
	DeleteByFilePath(filePath string) error
}

// fileAttachmentRepository 文件附件仓储实现
type fileAttachmentRepository struct {
	db *gorm.DB
}

// NewFileAttachmentRepository 创建文件附件仓储
func NewFileAttachmentRepository(db *gorm.DB) FileAttachmentRepository {
	return &fileAttachmentRepository{
		db: db,
	}
}

// Create 创建文件附件
func (r *fileAttachmentRepository) Create(attachment *model.FileAttachment) error {
	return r.db.Create(attachment).Error
}

// GetByID 根据ID获取文件附件
func (r *fileAttachmentRepository) GetByID(id uint) (*model.FileAttachment, error) {
	var attachment model.FileAttachment
	err := r.db.Preload("Uploader").First(&attachment, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("文件不存在")
		}
		return nil, err
	}
	return &attachment, nil
}

// GetByEntityID 根据实体ID获取文件附件列表
func (r *fileAttachmentRepository) GetByEntityID(entityType string, entityID uint) ([]*model.FileAttachment, error) {
	var attachments []*model.FileAttachment
	err := r.db.Preload("Uploader").
		Where("entity_type = ? AND entity_id = ?", entityType, entityID).
		Order("created_at DESC").
		Find(&attachments).Error
	return attachments, err
}

// Delete 删除文件附件
func (r *fileAttachmentRepository) Delete(id uint) error {
	return r.db.Delete(&model.FileAttachment{}, id).Error
}

// DeleteByFilePath 根据文件路径删除文件附件
func (r *fileAttachmentRepository) DeleteByFilePath(filePath string) error {
	return r.db.Where("file_path = ?", filePath).Delete(&model.FileAttachment{}).Error
}
