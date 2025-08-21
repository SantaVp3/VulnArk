package model

import (
	"time"
)

// DashboardStats 仪表盘统计数据
type DashboardStats struct {
	// 总体统计
	TotalVulnerabilities int64 `json:"total_vulnerabilities"`
	TotalAssets          int64 `json:"total_assets"`
	TotalUsers           int64 `json:"total_users"`
	TotalReports         int64 `json:"total_reports"`

	// 时间范围统计 - 用于计算百分比变化
	VulnerabilityTimeStats TimeRangeStats `json:"vulnerability_time_stats"`
	AssetTimeStats         TimeRangeStats `json:"asset_time_stats"`
	UserTimeStats          TimeRangeStats `json:"user_time_stats"`
	ReportTimeStats        TimeRangeStats `json:"report_time_stats"`

	// 漏洞状态统计
	VulnerabilityStats VulnerabilityStatusStats `json:"vulnerability_stats"`

	// 严重程度统计
	SeverityStats []SeverityCount `json:"severity_stats"`

	// 资产类型统计
	AssetTypeStats []AssetTypeCount `json:"asset_type_stats"`

	// 最近趋势
	RecentTrends TrendStats `json:"recent_trends"`

	// 热门漏洞类型
	TopVulnerabilityTypes []VulnerabilityTypeCount `json:"top_vulnerability_types"`

	// 活跃用户
	ActiveUsers []UserActivity `json:"active_users"`
}

// VulnerabilityStatusStats 漏洞状态统计
type VulnerabilityStatusStats struct {
	Open       int64 `json:"open"`
	InProgress int64 `json:"in_progress"`
	Resolved   int64 `json:"resolved"`
	Closed     int64 `json:"closed"`
	Reopened   int64 `json:"reopened"`
}

// SeverityCount 严重程度统计
type SeverityCount struct {
	Severity string `json:"severity"`
	Count    int64  `json:"count"`
	Color    string `json:"color"`
}

// AssetTypeCount 资产类型统计
type AssetTypeCount struct {
	Type  string `json:"type"`
	Count int64  `json:"count"`
}

// VulnerabilityTypeCount 漏洞类型统计
type VulnerabilityTypeCount struct {
	Type  string `json:"type"`
	Count int64  `json:"count"`
}

// UserActivity 用户活动统计
type UserActivity struct {
	UserID       uint   `json:"user_id"`
	Username     string `json:"username"`
	RealName     string `json:"real_name"`
	ActivityCount int64  `json:"activity_count"`
}

// TrendStats 趋势统计
type TrendStats struct {
	VulnerabilityTrend []TrendPoint `json:"vulnerability_trend"`
	AssetTrend         []TrendPoint `json:"asset_trend"`
	ReportTrend        []TrendPoint `json:"report_trend"`
}

// TrendPoint 趋势数据点
type TrendPoint struct {
	Date  string `json:"date"`
	Count int64  `json:"count"`
}

// VulnerabilityAnalytics 漏洞分析数据
type VulnerabilityAnalytics struct {
	// 时间范围统计
	TimeRangeStats TimeRangeStats `json:"time_range_stats"`
	
	// 严重程度趋势
	SeverityTrend []SeverityTrendPoint `json:"severity_trend"`
	
	// 状态分布
	StatusDistribution []StatusCount `json:"status_distribution"`
	
	// OWASP分类统计
	OWASPStats []OWASPCount `json:"owasp_stats"`
	
	// 资产漏洞分布
	AssetVulnerabilityDistribution []AssetVulnerabilityCount `json:"asset_vulnerability_distribution"`
	
	// 修复时间统计
	FixTimeStats FixTimeStats `json:"fix_time_stats"`
	
	// 发现者统计
	DiscovererStats []DiscovererCount `json:"discoverer_stats"`
}

// TimeRangeStats 时间范围统计
type TimeRangeStats struct {
	Today     int64 `json:"today"`
	Yesterday int64 `json:"yesterday"`
	ThisWeek  int64 `json:"this_week"`
	LastWeek  int64 `json:"last_week"`
	ThisMonth int64 `json:"this_month"`
	LastMonth int64 `json:"last_month"`
}

// SeverityTrendPoint 严重程度趋势点
type SeverityTrendPoint struct {
	Date     string `json:"date"`
	Critical int64  `json:"critical"`
	High     int64  `json:"high"`
	Medium   int64  `json:"medium"`
	Low      int64  `json:"low"`
}

// MonthlyTrendPoint 月度趋势点
type MonthlyTrendPoint struct {
	Month           string `json:"month"`
	Vulnerabilities int64  `json:"vulnerabilities"`
	Resolved        int64  `json:"resolved"`
}

// StatusCount 状态统计
type StatusCount struct {
	Status string `json:"status"`
	Count  int64  `json:"count"`
	Color  string `json:"color"`
}

// OWASPCount OWASP分类统计
type OWASPCount struct {
	Category string `json:"category"`
	Count    int64  `json:"count"`
}

// AssetVulnerabilityCount 资产漏洞统计
type AssetVulnerabilityCount struct {
	AssetID   uint   `json:"asset_id"`
	AssetName string `json:"asset_name"`
	AssetType string `json:"asset_type"`
	Count     int64  `json:"count"`
}

// FixTimeStats 修复时间统计
type FixTimeStats struct {
	AverageFixTime float64            `json:"average_fix_time"` // 平均修复时间（天）
	MedianFixTime  float64            `json:"median_fix_time"`  // 中位数修复时间（天）
	FixTimeRanges  []FixTimeRangeCount `json:"fix_time_ranges"`
}

// FixTimeRangeCount 修复时间范围统计
type FixTimeRangeCount struct {
	Range string `json:"range"`
	Count int64  `json:"count"`
}

// DiscovererCount 发现者统计
type DiscovererCount struct {
	DiscovererID uint   `json:"discoverer_id"`
	Username     string `json:"username"`
	RealName     string `json:"real_name"`
	Count        int64  `json:"count"`
}

// AssetAnalytics 资产分析数据
type AssetAnalytics struct {
	// 资产类型分布
	TypeDistribution []AssetTypeCount `json:"type_distribution"`
	
	// 重要性级别分布
	ImportanceDistribution []ImportanceCount `json:"importance_distribution"`
	
	// 资产状态分布
	StatusDistribution []AssetStatusCount `json:"status_distribution"`
	
	// 资产漏洞风险评估
	RiskAssessment []AssetRiskCount `json:"risk_assessment"`
	
	// 资产增长趋势
	GrowthTrend []TrendPoint `json:"growth_trend"`
}

// ImportanceCount 重要性统计
type ImportanceCount struct {
	Importance string `json:"importance"`
	Count      int64  `json:"count"`
	Color      string `json:"color"`
}

// AssetStatusCount 资产状态统计
type AssetStatusCount struct {
	Status string `json:"status"`
	Count  int64  `json:"count"`
}

// AssetRiskCount 资产风险统计
type AssetRiskCount struct {
	AssetID        uint   `json:"asset_id"`
	AssetName      string `json:"asset_name"`
	AssetType      string `json:"asset_type"`
	RiskScore      int    `json:"risk_score"`
	VulnerabilityCount int64  `json:"vulnerability_count"`
	CriticalCount  int64  `json:"critical_count"`
	HighCount      int64  `json:"high_count"`
}

// ReportAnalytics 报告分析数据
type ReportAnalytics struct {
	// 报告状态分布
	StatusDistribution []ReportStatusCount `json:"status_distribution"`
	
	// 报告类型分布
	TypeDistribution []ReportTypeCount `json:"type_distribution"`
	
	// 提交趋势
	SubmissionTrend []TrendPoint `json:"submission_trend"`
	
	// 审核时间统计
	ReviewTimeStats ReviewTimeStats `json:"review_time_stats"`
	
	// 提交者统计
	SubmitterStats []SubmitterCount `json:"submitter_stats"`
}

// ReportStatusCount 报告状态统计
type ReportStatusCount struct {
	Status string `json:"status"`
	Count  int64  `json:"count"`
}

// ReportTypeCount 报告类型统计
type ReportTypeCount struct {
	Type  string `json:"type"`
	Count int64  `json:"count"`
}

// ReviewTimeStats 审核时间统计
type ReviewTimeStats struct {
	AverageReviewTime float64 `json:"average_review_time"` // 平均审核时间（天）
	MedianReviewTime  float64 `json:"median_review_time"`  // 中位数审核时间（天）
}

// SubmitterCount 提交者统计
type SubmitterCount struct {
	SubmitterID uint   `json:"submitter_id"`
	Username    string `json:"username"`
	RealName    string `json:"real_name"`
	Count       int64  `json:"count"`
}

// UserAnalytics 用户分析数据
type UserAnalytics struct {
	// 用户角色分布
	RoleDistribution []UserRoleCount `json:"role_distribution"`
	
	// 用户状态分布
	StatusDistribution []UserStatusCount `json:"status_distribution"`
	
	// 用户活动统计
	ActivityStats []UserActivityStats `json:"activity_stats"`
	
	// 登录趋势
	LoginTrend []TrendPoint `json:"login_trend"`
	
	// 部门分布
	DepartmentDistribution []DepartmentCount `json:"department_distribution"`
}

// UserRoleCount 用户角色统计
type UserRoleCount struct {
	RoleID   uint   `json:"role_id"`
	RoleName string `json:"role_name"`
	Count    int64  `json:"count"`
}

// UserStatusCount 用户状态统计
type UserStatusCount struct {
	Status string `json:"status"`
	Count  int64  `json:"count"`
}

// UserActivityStats 用户活动统计
type UserActivityStats struct {
	UserID            uint   `json:"user_id"`
	Username          string `json:"username"`
	RealName          string `json:"real_name"`
	VulnerabilitiesFound int64  `json:"vulnerabilities_found"`
	ReportsSubmitted  int64  `json:"reports_submitted"`
	LastLoginAt       *time.Time `json:"last_login_at"`
}

// DepartmentCount 部门统计
type DepartmentCount struct {
	Department string `json:"department"`
	Count      int64  `json:"count"`
}

// AnalyticsRequest 分析请求参数
type AnalyticsRequest struct {
	StartDate string `json:"start_date" form:"start_date"`
	EndDate   string `json:"end_date" form:"end_date"`
	Granularity string `json:"granularity" form:"granularity"` // day, week, month
	AssetID   string `json:"asset_id" form:"asset_id"`
	UserID    string `json:"user_id" form:"user_id"`
	Severity  string `json:"severity" form:"severity"`
	Status    string `json:"status" form:"status"`
	Category  string `json:"category" form:"category"`
}

// ExportRequest 导出请求参数
type ExportRequest struct {
	Type      string `json:"type" binding:"required,oneof=dashboard vulnerability asset report user"`
	Format    string `json:"format" binding:"required,oneof=excel pdf csv"`
	StartDate string `json:"start_date"`
	EndDate   string `json:"end_date"`
	Filters   map[string]interface{} `json:"filters"`
}

// ExportResponse 导出响应
type ExportResponse struct {
	FileName string `json:"file_name"`
	FileURL  string `json:"file_url"`
	FileSize int64  `json:"file_size"`
}

// 时间粒度常量
const (
	GranularityDay   = "day"
	GranularityWeek  = "week"
	GranularityMonth = "month"
)

// 导出类型常量
const (
	ExportTypeDashboard     = "dashboard"
	ExportTypeVulnerability = "vulnerability"
	ExportTypeAsset         = "asset"
	ExportTypeReport        = "report"
	ExportTypeUser          = "user"
)

// 导出格式常量
const (
	ExportFormatExcel = "excel"
	ExportFormatPDF   = "pdf"
	ExportFormatCSV   = "csv"
)

// GetSeverityColor 获取严重程度颜色
func GetSeverityColor(severity string) string {
	switch severity {
	case "critical":
		return "#dc3545" // 红色
	case "high":
		return "#fd7e14" // 橙色
	case "medium":
		return "#ffc107" // 黄色
	case "low":
		return "#28a745" // 绿色
	default:
		return "#6c757d" // 灰色
	}
}

// GetStatusColor 获取状态颜色
func GetStatusColor(status string) string {
	switch status {
	case "open":
		return "#dc3545" // 红色
	case "in_progress":
		return "#ffc107" // 黄色
	case "resolved":
		return "#28a745" // 绿色
	case "closed":
		return "#6c757d" // 灰色
	case "reopened":
		return "#fd7e14" // 橙色
	default:
		return "#6c757d" // 灰色
	}
}

// GetImportanceColor 获取重要性颜色
func GetImportanceColor(importance string) string {
	switch importance {
	case "critical":
		return "#dc3545" // 红色
	case "high":
		return "#fd7e14" // 橙色
	case "medium":
		return "#ffc107" // 黄色
	case "low":
		return "#28a745" // 绿色
	default:
		return "#6c757d" // 灰色
	}
}
