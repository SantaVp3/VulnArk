package repository

import (
	"errors"
	"fmt"
	"time"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// AnalyticsRepository 统计分析仓储接口
type AnalyticsRepository interface {
	// 仪表盘统计
	GetDashboardStats() (*model.DashboardStats, error)
	
	// 漏洞分析
	GetVulnerabilityAnalytics(req *model.AnalyticsRequest) (*model.VulnerabilityAnalytics, error)
	GetVulnerabilityTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error)
	GetSeverityTrend(startDate, endDate time.Time) ([]model.SeverityTrendPoint, error)
	
	// 资产分析
	GetAssetAnalytics(req *model.AnalyticsRequest) (*model.AssetAnalytics, error)
	GetAssetGrowthTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error)
	GetAssetRiskAssessment() ([]model.AssetRiskCount, error)
	
	// 报告分析
	GetReportAnalytics(req *model.AnalyticsRequest) (*model.ReportAnalytics, error)
	GetReportSubmissionTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error)
	
	// 用户分析
	GetUserAnalytics(req *model.AnalyticsRequest) (*model.UserAnalytics, error)
	GetUserLoginTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error)
	
	// 通用统计
	GetTimeRangeStats(tableName string, dateColumn string) (*model.TimeRangeStats, error)
	GetMonthlyVulnerabilityTrends() ([]model.MonthlyTrendPoint, error)
}

// analyticsRepository 统计分析仓储实现
type analyticsRepository struct {
	db *gorm.DB
}

// NewAnalyticsRepository 创建分析仓储
func NewAnalyticsRepository(db *gorm.DB) AnalyticsRepository {
	return &analyticsRepository{
		db: db,
	}
}

// validateGranularity 验证时间粒度参数，防止SQL注入
func validateGranularity(granularity string) (string, error) {
	validFormats := map[string]string{
		"day":   "%Y-%m-%d",
		"week":  "%Y-%u",
		"month": "%Y-%m",
	}

	if format, exists := validFormats[granularity]; exists {
		return format, nil
	}
	return "", errors.New("无效的时间粒度参数")
}

// GetDashboardStats 获取仪表盘统计数据
func (r *analyticsRepository) GetDashboardStats() (*model.DashboardStats, error) {
	stats := &model.DashboardStats{}

	// Ensure we get fresh data by using a new transaction
	tx := r.db.Begin()
	defer func() {
		if r := recover(); r != nil {
			tx.Rollback()
		}
	}()

	if tx.Error != nil {
		return nil, tx.Error
	}
	
	// 总体统计 - 使用事务确保数据一致性
	tx.Model(&model.Vulnerability{}).Count(&stats.TotalVulnerabilities)
	tx.Model(&model.Asset{}).Count(&stats.TotalAssets)
	tx.Model(&model.User{}).Count(&stats.TotalUsers)
	tx.Model(&model.Report{}).Count(&stats.TotalReports)
	
	// 漏洞状态统计
	var statusStats []struct {
		Status string
		Count  int64
	}
	tx.Model(&model.Vulnerability{}).
		Select("status, COUNT(*) as count").
		Group("status").
		Scan(&statusStats)
	
	for _, stat := range statusStats {
		switch stat.Status {
		case "new":
			stats.VulnerabilityStats.Open = stat.Count
		case "processing":
			stats.VulnerabilityStats.InProgress = stat.Count
		case "fixed":
			stats.VulnerabilityStats.Resolved = stat.Count
		case "verified":
			stats.VulnerabilityStats.Resolved += stat.Count // Add verified to resolved
		case "closed":
			stats.VulnerabilityStats.Closed = stat.Count
		}
	}
	
	// 严重程度统计
	var severityStats []struct {
		SeverityLevel int
		Count         int64
	}
	tx.Model(&model.Vulnerability{}).
		Select("severity_level, COUNT(*) as count").
		Group("severity_level").
		Scan(&severityStats)

	for _, stat := range severityStats {
		var severityStr string
		switch stat.SeverityLevel {
		case 1:
			severityStr = "critical"
		case 2:
			severityStr = "high"
		case 3:
			severityStr = "medium"
		case 4:
			severityStr = "low"
		default:
			severityStr = "unknown"
		}

		stats.SeverityStats = append(stats.SeverityStats, model.SeverityCount{
			Severity: severityStr,
			Count:    stat.Count,
			Color:    model.GetSeverityColor(severityStr),
		})
	}
	
	// 资产类型统计
	var assetTypeStats []struct {
		Type  string
		Count int64
	}
	tx.Model(&model.Asset{}).
		Select("type, COUNT(*) as count").
		Group("type").
		Scan(&assetTypeStats)
	
	for _, stat := range assetTypeStats {
		stats.AssetTypeStats = append(stats.AssetTypeStats, model.AssetTypeCount{
			Type:  stat.Type,
			Count: stat.Count,
		})
	}
	
	// 热门漏洞类型
	var vulnTypeStats []struct {
		OwaspCategory string
		Count         int64
	}
	tx.Model(&model.Vulnerability{}).
		Select("owasp_category, COUNT(*) as count").
		Group("owasp_category").
		Order("count DESC").
		Limit(10).
		Scan(&vulnTypeStats)
	
	for _, stat := range vulnTypeStats {
		stats.TopVulnerabilityTypes = append(stats.TopVulnerabilityTypes, model.VulnerabilityTypeCount{
			Type:  stat.OwaspCategory,
			Count: stat.Count,
		})
	}
	
	// 活跃用户（最近30天发现漏洞最多的用户）
	var activeUsers []struct {
		UserID   uint
		Username string
		RealName string
		Count    int64
	}
	tx.Table("vulnerabilities v").
		Select("u.id as user_id, u.username, u.real_name, COUNT(*) as count").
		Joins("JOIN users u ON v.discoverer_id = u.id").
		Where("v.created_at >= ?", time.Now().AddDate(0, 0, -30)).
		Group("u.id, u.username, u.real_name").
		Order("count DESC").
		Limit(10).
		Scan(&activeUsers)
	
	for _, user := range activeUsers {
		stats.ActiveUsers = append(stats.ActiveUsers, model.UserActivity{
			UserID:        user.UserID,
			Username:      user.Username,
			RealName:      user.RealName,
			ActivityCount: user.Count,
		})
	}
	
	// 时间范围统计 - 用于计算百分比变化
	vulnTimeStats, _ := r.GetTimeRangeStats("vulnerabilities", "created_at")
	if vulnTimeStats != nil {
		stats.VulnerabilityTimeStats = *vulnTimeStats
	}

	assetTimeStats, _ := r.GetTimeRangeStats("assets", "created_at")
	if assetTimeStats != nil {
		stats.AssetTimeStats = *assetTimeStats
	}

	userTimeStats, _ := r.GetTimeRangeStats("users", "created_at")
	if userTimeStats != nil {
		stats.UserTimeStats = *userTimeStats
	}

	reportTimeStats, _ := r.GetTimeRangeStats("reports", "created_at")
	if reportTimeStats != nil {
		stats.ReportTimeStats = *reportTimeStats
	}

	// 最近趋势（最近7天）
	endDate := time.Now()
	startDate := endDate.AddDate(0, 0, -7)

	vulnTrend, _ := r.GetVulnerabilityTrend(startDate, endDate, "day")
	assetTrend, _ := r.GetAssetGrowthTrend(startDate, endDate, "day")
	reportTrend, _ := r.GetReportSubmissionTrend(startDate, endDate, "day")

	stats.RecentTrends = model.TrendStats{
		VulnerabilityTrend: vulnTrend,
		AssetTrend:         assetTrend,
		ReportTrend:        reportTrend,
	}

	// 提交事务
	if err := tx.Commit().Error; err != nil {
		return nil, err
	}

	return stats, nil
}

// GetVulnerabilityAnalytics 获取漏洞分析数据
func (r *analyticsRepository) GetVulnerabilityAnalytics(req *model.AnalyticsRequest) (*model.VulnerabilityAnalytics, error) {
	analytics := &model.VulnerabilityAnalytics{}
	
	// 构建查询条件
	query := r.db.Model(&model.Vulnerability{})
	if req.StartDate != "" && req.EndDate != "" {
		query = query.Where("created_at BETWEEN ? AND ?", req.StartDate, req.EndDate)
	}
	if req.AssetID != "" {
		query = query.Where("asset_id = ?", req.AssetID)
	}
	if req.Severity != "" {
		query = query.Where("severity = ?", req.Severity)
	}
	if req.Status != "" {
		query = query.Where("status = ?", req.Status)
	}
	
	// 时间范围统计
	timeStats, _ := r.GetTimeRangeStats("vulnerabilities", "created_at")
	analytics.TimeRangeStats = *timeStats
	
	// 状态分布
	var statusStats []struct {
		Status string
		Count  int64
	}
	query.Select("status, COUNT(*) as count").Group("status").Scan(&statusStats)
	
	for _, stat := range statusStats {
		analytics.StatusDistribution = append(analytics.StatusDistribution, model.StatusCount{
			Status: stat.Status,
			Count:  stat.Count,
			Color:  model.GetStatusColor(stat.Status),
		})
	}
	
	// OWASP分类统计
	var owaspStats []struct {
		Category string
		Count    int64
	}
	query.Select("owasp_category as category, COUNT(*) as count").
		Group("owasp_category").
		Scan(&owaspStats)
	
	for _, stat := range owaspStats {
		analytics.OWASPStats = append(analytics.OWASPStats, model.OWASPCount{
			Category: stat.Category,
			Count:    stat.Count,
		})
	}
	
	// 资产漏洞分布
	var assetVulnStats []struct {
		AssetID   uint
		AssetName string
		AssetType string
		Count     int64
	}
	r.db.Table("vulnerabilities v").
		Select("a.id as asset_id, a.name as asset_name, a.type as asset_type, COUNT(*) as count").
		Joins("JOIN assets a ON v.asset_id = a.id").
		Group("a.id, a.name, a.type").
		Order("count DESC").
		Limit(20).
		Scan(&assetVulnStats)
	
	for _, stat := range assetVulnStats {
		analytics.AssetVulnerabilityDistribution = append(analytics.AssetVulnerabilityDistribution, model.AssetVulnerabilityCount{
			AssetID:   stat.AssetID,
			AssetName: stat.AssetName,
			AssetType: stat.AssetType,
			Count:     stat.Count,
		})
	}
	
	// 修复时间统计
	var fixTimes []float64
	r.db.Model(&model.Vulnerability{}).
		Select("DATEDIFF(resolved_at, created_at) as fix_time").
		Where("status = ? AND resolved_at IS NOT NULL", "resolved").
		Pluck("fix_time", &fixTimes)
	
	if len(fixTimes) > 0 {
		// 计算平均值和中位数
		var sum float64
		for _, time := range fixTimes {
			sum += time
		}
		analytics.FixTimeStats.AverageFixTime = sum / float64(len(fixTimes))
		
		// 简单的中位数计算
		if len(fixTimes)%2 == 0 {
			analytics.FixTimeStats.MedianFixTime = (fixTimes[len(fixTimes)/2-1] + fixTimes[len(fixTimes)/2]) / 2
		} else {
			analytics.FixTimeStats.MedianFixTime = fixTimes[len(fixTimes)/2]
		}
		
		// 修复时间范围统计
		var fixTimeRanges []struct {
			Range string
			Count int64
		}
		r.db.Raw(`
			SELECT 
				CASE 
					WHEN DATEDIFF(resolved_at, created_at) <= 1 THEN '1天内'
					WHEN DATEDIFF(resolved_at, created_at) <= 7 THEN '1-7天'
					WHEN DATEDIFF(resolved_at, created_at) <= 30 THEN '1-4周'
					WHEN DATEDIFF(resolved_at, created_at) <= 90 THEN '1-3个月'
					ELSE '3个月以上'
				END as range,
				COUNT(*) as count
			FROM vulnerabilities 
			WHERE status = 'resolved' AND resolved_at IS NOT NULL
			GROUP BY range
		`).Scan(&fixTimeRanges)
		
		for _, stat := range fixTimeRanges {
			analytics.FixTimeStats.FixTimeRanges = append(analytics.FixTimeStats.FixTimeRanges, model.FixTimeRangeCount{
				Range: stat.Range,
				Count: stat.Count,
			})
		}
	}
	
	// 发现者统计
	var discovererStats []struct {
		DiscovererID uint
		Username     string
		RealName     string
		Count        int64
	}
	query.Table("vulnerabilities v").
		Select("u.id as discoverer_id, u.username, u.real_name, COUNT(*) as count").
		Joins("JOIN users u ON v.discoverer_id = u.id").
		Group("u.id, u.username, u.real_name").
		Order("count DESC").
		Limit(10).
		Scan(&discovererStats)
	
	for _, stat := range discovererStats {
		analytics.DiscovererStats = append(analytics.DiscovererStats, model.DiscovererCount{
			DiscovererID: stat.DiscovererID,
			Username:     stat.Username,
			RealName:     stat.RealName,
			Count:        stat.Count,
		})
	}
	
	return analytics, nil
}

// GetVulnerabilityTrend 获取漏洞趋势数据
func (r *analyticsRepository) GetVulnerabilityTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error) {
	var trends []model.TrendPoint

	// 验证时间粒度参数，防止SQL注入
	dateFormat, err := validateGranularity(granularity)
	if err != nil {
		return nil, err
	}

	var results []struct {
		Date  string
		Count int64
	}

	r.db.Model(&model.Vulnerability{}).
		Select(fmt.Sprintf("DATE_FORMAT(created_at, '%s') as date, COUNT(*) as count", dateFormat)).
		Where("created_at BETWEEN ? AND ?", startDate, endDate).
		Group("date").
		Order("date").
		Scan(&results)
	
	for _, result := range results {
		trends = append(trends, model.TrendPoint{
			Date:  result.Date,
			Count: result.Count,
		})
	}
	
	return trends, nil
}

// GetSeverityTrend 获取严重程度趋势数据
func (r *analyticsRepository) GetSeverityTrend(startDate, endDate time.Time) ([]model.SeverityTrendPoint, error) {
	var trends []model.SeverityTrendPoint
	
	var results []struct {
		Date     string
		Severity string
		Count    int64
	}
	
	r.db.Model(&model.Vulnerability{}).
		Select("DATE_FORMAT(created_at, '%Y-%m-%d') as date, severity, COUNT(*) as count").
		Where("created_at BETWEEN ? AND ?", startDate, endDate).
		Group("date, severity").
		Order("date").
		Scan(&results)
	
	// 按日期分组
	dateMap := make(map[string]*model.SeverityTrendPoint)
	for _, result := range results {
		if _, exists := dateMap[result.Date]; !exists {
			dateMap[result.Date] = &model.SeverityTrendPoint{Date: result.Date}
		}
		
		switch result.Severity {
		case "critical":
			dateMap[result.Date].Critical = result.Count
		case "high":
			dateMap[result.Date].High = result.Count
		case "medium":
			dateMap[result.Date].Medium = result.Count
		case "low":
			dateMap[result.Date].Low = result.Count
		}
	}
	
	for _, trend := range dateMap {
		trends = append(trends, *trend)
	}
	
	return trends, nil
}

// validateTableAndColumn 验证表名和列名，防止SQL注入
func validateTableAndColumn(tableName, dateColumn string) error {
	validTables := map[string][]string{
		"vulnerabilities": {"created_at", "updated_at", "resolved_at"},
		"assets":         {"created_at", "updated_at"},
		"reports":        {"created_at", "reviewed_at"},
		"users":          {"created_at", "last_login_at"},
	}

	if columns, exists := validTables[tableName]; exists {
		for _, col := range columns {
			if col == dateColumn {
				return nil
			}
		}
	}
	return errors.New("无效的表名或列名")
}

// GetTimeRangeStats 获取时间范围统计
func (r *analyticsRepository) GetTimeRangeStats(tableName string, dateColumn string) (*model.TimeRangeStats, error) {
	// 验证表名和列名，防止SQL注入
	if err := validateTableAndColumn(tableName, dateColumn); err != nil {
		return nil, err
	}

	stats := &model.TimeRangeStats{}

	now := time.Now()
	today := now.Format("2006-01-02")
	yesterday := now.AddDate(0, 0, -1).Format("2006-01-02")

	// 今天
	r.db.Table(tableName).Where(fmt.Sprintf("DATE(%s) = ?", dateColumn), today).Count(&stats.Today)

	// 昨天
	r.db.Table(tableName).Where(fmt.Sprintf("DATE(%s) = ?", dateColumn), yesterday).Count(&stats.Yesterday)

	// 本周
	r.db.Table(tableName).Where(fmt.Sprintf("YEARWEEK(%s) = YEARWEEK(NOW())", dateColumn)).Count(&stats.ThisWeek)

	// 上周
	r.db.Table(tableName).Where(fmt.Sprintf("YEARWEEK(%s) = YEARWEEK(NOW()) - 1", dateColumn)).Count(&stats.LastWeek)

	// 本月
	r.db.Table(tableName).Where(fmt.Sprintf("YEAR(%s) = YEAR(NOW()) AND MONTH(%s) = MONTH(NOW())", dateColumn, dateColumn)).Count(&stats.ThisMonth)

	// 上月
	r.db.Table(tableName).Where(fmt.Sprintf("YEAR(%s) = YEAR(NOW() - INTERVAL 1 MONTH) AND MONTH(%s) = MONTH(NOW() - INTERVAL 1 MONTH)", dateColumn, dateColumn)).Count(&stats.LastMonth)

	return stats, nil
}

// GetAssetAnalytics 获取资产分析数据
func (r *analyticsRepository) GetAssetAnalytics(req *model.AnalyticsRequest) (*model.AssetAnalytics, error) {
	analytics := &model.AssetAnalytics{}

	// 构建查询条件
	query := r.db.Model(&model.Asset{})
	if req.StartDate != "" && req.EndDate != "" {
		query = query.Where("created_at BETWEEN ? AND ?", req.StartDate, req.EndDate)
	}

	// 资产类型分布
	var typeStats []struct {
		Type  string
		Count int64
	}
	query.Select("type, COUNT(*) as count").Group("type").Scan(&typeStats)

	for _, stat := range typeStats {
		analytics.TypeDistribution = append(analytics.TypeDistribution, model.AssetTypeCount{
			Type:  stat.Type,
			Count: stat.Count,
		})
	}

	// 重要性级别分布
	var importanceStats []struct {
		Importance string
		Count      int64
	}
	query.Select("importance_level as importance, COUNT(*) as count").
		Group("importance_level").
		Scan(&importanceStats)

	for _, stat := range importanceStats {
		analytics.ImportanceDistribution = append(analytics.ImportanceDistribution, model.ImportanceCount{
			Importance: stat.Importance,
			Count:      stat.Count,
			Color:      model.GetImportanceColor(stat.Importance),
		})
	}

	// 资产状态分布
	var statusStats []struct {
		Status string
		Count  int64
	}
	query.Select("status, COUNT(*) as count").Group("status").Scan(&statusStats)

	for _, stat := range statusStats {
		analytics.StatusDistribution = append(analytics.StatusDistribution, model.AssetStatusCount{
			Status: stat.Status,
			Count:  stat.Count,
		})
	}

	// 资产风险评估
	riskAssessment, _ := r.GetAssetRiskAssessment()
	analytics.RiskAssessment = riskAssessment

	// 资产增长趋势
	if req.StartDate != "" && req.EndDate != "" {
		startDate, _ := time.Parse("2006-01-02", req.StartDate)
		endDate, _ := time.Parse("2006-01-02", req.EndDate)
		granularity := req.Granularity
		if granularity == "" {
			granularity = "day"
		}
		growthTrend, _ := r.GetAssetGrowthTrend(startDate, endDate, granularity)
		analytics.GrowthTrend = growthTrend
	}

	return analytics, nil
}

// GetAssetGrowthTrend 获取资产增长趋势
func (r *analyticsRepository) GetAssetGrowthTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error) {
	var trends []model.TrendPoint

	// 验证时间粒度参数，防止SQL注入
	dateFormat, err := validateGranularity(granularity)
	if err != nil {
		return nil, err
	}

	var results []struct {
		Date  string
		Count int64
	}

	r.db.Model(&model.Asset{}).
		Select(fmt.Sprintf("DATE_FORMAT(created_at, '%s') as date, COUNT(*) as count", dateFormat)).
		Where("created_at BETWEEN ? AND ?", startDate, endDate).
		Group("date").
		Order("date").
		Scan(&results)

	for _, result := range results {
		trends = append(trends, model.TrendPoint{
			Date:  result.Date,
			Count: result.Count,
		})
	}

	return trends, nil
}

// GetAssetRiskAssessment 获取资产风险评估
func (r *analyticsRepository) GetAssetRiskAssessment() ([]model.AssetRiskCount, error) {
	var risks []model.AssetRiskCount

	var results []struct {
		AssetID           uint
		AssetName         string
		AssetType         string
		VulnerabilityCount int64
		CriticalCount     int64
		HighCount         int64
	}

	r.db.Table("assets a").
		Select(`
			a.id as asset_id,
			a.name as asset_name,
			a.type as asset_type,
			COUNT(v.id) as vulnerability_count,
			SUM(CASE WHEN v.severity = 'critical' THEN 1 ELSE 0 END) as critical_count,
			SUM(CASE WHEN v.severity = 'high' THEN 1 ELSE 0 END) as high_count
		`).
		Joins("LEFT JOIN vulnerabilities v ON a.id = v.asset_id").
		Group("a.id, a.name, a.type").
		Having("vulnerability_count > 0").
		Order("critical_count DESC, high_count DESC, vulnerability_count DESC").
		Limit(20).
		Scan(&results)

	for _, result := range results {
		// 计算风险评分：严重漏洞*10 + 高危漏洞*5 + 总漏洞数
		riskScore := int(result.CriticalCount*10 + result.HighCount*5 + result.VulnerabilityCount)

		risks = append(risks, model.AssetRiskCount{
			AssetID:            result.AssetID,
			AssetName:          result.AssetName,
			AssetType:          result.AssetType,
			RiskScore:          riskScore,
			VulnerabilityCount: result.VulnerabilityCount,
			CriticalCount:      result.CriticalCount,
			HighCount:          result.HighCount,
		})
	}

	return risks, nil
}

// GetReportAnalytics 获取报告分析数据
func (r *analyticsRepository) GetReportAnalytics(req *model.AnalyticsRequest) (*model.ReportAnalytics, error) {
	analytics := &model.ReportAnalytics{}

	// 构建查询条件
	query := r.db.Model(&model.Report{})
	if req.StartDate != "" && req.EndDate != "" {
		query = query.Where("created_at BETWEEN ? AND ?", req.StartDate, req.EndDate)
	}

	// 报告状态分布
	var statusStats []struct {
		Status string
		Count  int64
	}
	query.Select("status, COUNT(*) as count").Group("status").Scan(&statusStats)

	for _, stat := range statusStats {
		analytics.StatusDistribution = append(analytics.StatusDistribution, model.ReportStatusCount{
			Status: stat.Status,
			Count:  stat.Count,
		})
	}

	// 报告类型分布
	var typeStats []struct {
		Type  string
		Count int64
	}
	query.Select("type, COUNT(*) as count").Group("type").Scan(&typeStats)

	for _, stat := range typeStats {
		analytics.TypeDistribution = append(analytics.TypeDistribution, model.ReportTypeCount{
			Type:  stat.Type,
			Count: stat.Count,
		})
	}

	// 提交趋势
	if req.StartDate != "" && req.EndDate != "" {
		startDate, _ := time.Parse("2006-01-02", req.StartDate)
		endDate, _ := time.Parse("2006-01-02", req.EndDate)
		granularity := req.Granularity
		if granularity == "" {
			granularity = "day"
		}
		submissionTrend, _ := r.GetReportSubmissionTrend(startDate, endDate, granularity)
		analytics.SubmissionTrend = submissionTrend
	}

	// 审核时间统计
	var reviewTimes []float64
	r.db.Model(&model.Report{}).
		Select("DATEDIFF(reviewed_at, created_at) as review_time").
		Where("status IN ? AND reviewed_at IS NOT NULL", []string{"approved", "rejected"}).
		Pluck("review_time", &reviewTimes)

	if len(reviewTimes) > 0 {
		var sum float64
		for _, time := range reviewTimes {
			sum += time
		}
		analytics.ReviewTimeStats.AverageReviewTime = sum / float64(len(reviewTimes))

		// 简单的中位数计算
		if len(reviewTimes)%2 == 0 {
			analytics.ReviewTimeStats.MedianReviewTime = (reviewTimes[len(reviewTimes)/2-1] + reviewTimes[len(reviewTimes)/2]) / 2
		} else {
			analytics.ReviewTimeStats.MedianReviewTime = reviewTimes[len(reviewTimes)/2]
		}
	}

	// 提交者统计
	var submitterStats []struct {
		SubmitterID uint
		Username    string
		RealName    string
		Count       int64
	}
	query.Table("reports r").
		Select("u.id as submitter_id, u.username, u.real_name, COUNT(*) as count").
		Joins("JOIN users u ON r.submitter_id = u.id").
		Group("u.id, u.username, u.real_name").
		Order("count DESC").
		Limit(10).
		Scan(&submitterStats)

	for _, stat := range submitterStats {
		analytics.SubmitterStats = append(analytics.SubmitterStats, model.SubmitterCount{
			SubmitterID: stat.SubmitterID,
			Username:    stat.Username,
			RealName:    stat.RealName,
			Count:       stat.Count,
		})
	}

	return analytics, nil
}

// GetReportSubmissionTrend 获取报告提交趋势
func (r *analyticsRepository) GetReportSubmissionTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error) {
	var trends []model.TrendPoint

	// 验证时间粒度参数，防止SQL注入
	dateFormat, err := validateGranularity(granularity)
	if err != nil {
		return nil, err
	}

	var results []struct {
		Date  string
		Count int64
	}

	r.db.Model(&model.Report{}).
		Select(fmt.Sprintf("DATE_FORMAT(created_at, '%s') as date, COUNT(*) as count", dateFormat)).
		Where("created_at BETWEEN ? AND ?", startDate, endDate).
		Group("date").
		Order("date").
		Scan(&results)

	for _, result := range results {
		trends = append(trends, model.TrendPoint{
			Date:  result.Date,
			Count: result.Count,
		})
	}

	return trends, nil
}

// GetUserAnalytics 获取用户分析数据
func (r *analyticsRepository) GetUserAnalytics(req *model.AnalyticsRequest) (*model.UserAnalytics, error) {
	analytics := &model.UserAnalytics{}

	// 构建查询条件
	query := r.db.Model(&model.User{})
	if req.StartDate != "" && req.EndDate != "" {
		query = query.Where("created_at BETWEEN ? AND ?", req.StartDate, req.EndDate)
	}

	// 用户角色分布
	var roleStats []struct {
		RoleID   uint
		RoleName string
		Count    int64
	}
	query.Table("users u").
		Select("r.id as role_id, r.name as role_name, COUNT(*) as count").
		Joins("JOIN roles r ON u.role_id = r.id").
		Group("r.id, r.name").
		Scan(&roleStats)

	for _, stat := range roleStats {
		analytics.RoleDistribution = append(analytics.RoleDistribution, model.UserRoleCount{
			RoleID:   stat.RoleID,
			RoleName: stat.RoleName,
			Count:    stat.Count,
		})
	}

	// 用户状态分布
	var statusStats []struct {
		Status string
		Count  int64
	}
	query.Select("CASE WHEN status = 1 THEN 'active' ELSE 'inactive' END as status, COUNT(*) as count").
		Group("status").
		Scan(&statusStats)

	for _, stat := range statusStats {
		analytics.StatusDistribution = append(analytics.StatusDistribution, model.UserStatusCount{
			Status: stat.Status,
			Count:  stat.Count,
		})
	}

	// 用户活动统计
	var activityStats []struct {
		UserID               uint
		Username             string
		RealName             string
		VulnerabilitiesFound int64
		ReportsSubmitted     int64
		LastLoginAt          *time.Time
	}
	r.db.Table("users u").
		Select(`
			u.id as user_id,
			u.username,
			u.real_name,
			COUNT(DISTINCT v.id) as vulnerabilities_found,
			COUNT(DISTINCT r.id) as reports_submitted,
			u.last_login_at
		`).
		Joins("LEFT JOIN vulnerabilities v ON u.id = v.discoverer_id").
		Joins("LEFT JOIN reports r ON u.id = r.submitter_id").
		Group("u.id, u.username, u.real_name, u.last_login_at").
		Having("vulnerabilities_found > 0 OR reports_submitted > 0").
		Order("vulnerabilities_found DESC, reports_submitted DESC").
		Limit(20).
		Scan(&activityStats)

	for _, stat := range activityStats {
		analytics.ActivityStats = append(analytics.ActivityStats, model.UserActivityStats{
			UserID:               stat.UserID,
			Username:             stat.Username,
			RealName:             stat.RealName,
			VulnerabilitiesFound: stat.VulnerabilitiesFound,
			ReportsSubmitted:     stat.ReportsSubmitted,
			LastLoginAt:          stat.LastLoginAt,
		})
	}

	// 登录趋势
	if req.StartDate != "" && req.EndDate != "" {
		startDate, _ := time.Parse("2006-01-02", req.StartDate)
		endDate, _ := time.Parse("2006-01-02", req.EndDate)
		granularity := req.Granularity
		if granularity == "" {
			granularity = "day"
		}
		loginTrend, _ := r.GetUserLoginTrend(startDate, endDate, granularity)
		analytics.LoginTrend = loginTrend
	}

	// 部门分布
	var deptStats []struct {
		Department string
		Count      int64
	}
	query.Select("department, COUNT(*) as count").
		Where("department != ''").
		Group("department").
		Scan(&deptStats)

	for _, stat := range deptStats {
		analytics.DepartmentDistribution = append(analytics.DepartmentDistribution, model.DepartmentCount{
			Department: stat.Department,
			Count:      stat.Count,
		})
	}

	return analytics, nil
}

// GetUserLoginTrend 获取用户登录趋势
func (r *analyticsRepository) GetUserLoginTrend(startDate, endDate time.Time, granularity string) ([]model.TrendPoint, error) {
	var trends []model.TrendPoint

	// 验证时间粒度参数，防止SQL注入
	dateFormat, err := validateGranularity(granularity)
	if err != nil {
		return nil, err
	}

	var results []struct {
		Date  string
		Count int64
	}

	r.db.Model(&model.User{}).
		Select(fmt.Sprintf("DATE_FORMAT(last_login_at, '%s') as date, COUNT(DISTINCT id) as count", dateFormat)).
		Where("last_login_at BETWEEN ? AND ?", startDate, endDate).
		Group("date").
		Order("date").
		Scan(&results)

	for _, result := range results {
		trends = append(trends, model.TrendPoint{
			Date:  result.Date,
			Count: result.Count,
		})
	}

	return trends, nil
}

// GetMonthlyVulnerabilityTrends 获取月度漏洞趋势
func (r *analyticsRepository) GetMonthlyVulnerabilityTrends() ([]model.MonthlyTrendPoint, error) {
	var trends []model.MonthlyTrendPoint

	// 获取最近6个月的数据
	now := time.Now()

	for i := 5; i >= 0; i-- {
		monthStart := time.Date(now.Year(), now.Month()-time.Month(i), 1, 0, 0, 0, 0, now.Location())
		monthEnd := monthStart.AddDate(0, 1, 0).Add(-time.Second)

		// 统计该月发现的漏洞数量
		var vulnerabilityCount int64
		err := r.db.Model(&model.Vulnerability{}).
			Where("created_at BETWEEN ? AND ?", monthStart, monthEnd).
			Count(&vulnerabilityCount).Error
		if err != nil {
			return nil, err
		}

		// 统计该月解决的漏洞数量
		var resolvedCount int64
		err = r.db.Model(&model.Vulnerability{}).
			Where("status IN ? AND updated_at BETWEEN ? AND ?", []string{"resolved", "fixed", "verified"}, monthStart, monthEnd).
			Count(&resolvedCount).Error
		if err != nil {
			return nil, err
		}

		trends = append(trends, model.MonthlyTrendPoint{
			Month:           monthStart.Format("1月"),
			Vulnerabilities: vulnerabilityCount,
			Resolved:        resolvedCount,
		})
	}

	return trends, nil
}
