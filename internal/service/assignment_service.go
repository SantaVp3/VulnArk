package service

import (
	"errors"
	"fmt"

	"vulnark/internal/model"
	"vulnark/internal/repository"
)

// AssignmentService 分配服务接口
type AssignmentService interface {
	// 分配规则管理
	CreateAssignmentRule(req *model.AssignmentRuleCreateRequest) (*model.AssignmentRule, error)
	GetAssignmentRuleByID(id uint) (*model.AssignmentRule, error)
	UpdateAssignmentRule(id uint, req *model.AssignmentRuleUpdateRequest) error
	DeleteAssignmentRule(id uint) error
	GetAssignmentRuleList(req *model.AssignmentRuleSearchRequest) (*model.PaginationResponse, error)
	
	// 漏洞分配
	AssignVulnerability(vulnID uint, req *model.VulnerabilityAssignRequest, assignerID uint) error
	BatchAssignVulnerabilities(req *model.BatchAssignRequest, assignerID uint) ([]string, error)
	AutoAssignVulnerabilities(req *model.AutoAssignRequest) ([]string, error)
	ReassignVulnerability(vulnID uint, newAssigneeID uint, assignerID uint, reason string) error
	
	// 统计和历史
	GetAssignmentStats() (*model.AssignmentStats, error)
	GetVulnerabilityTimeline(vulnID uint) ([]*model.TimelineEntry, error)
	
	// 自动分配引擎
	FindBestAssignee(vulnerability *model.Vulnerability) (uint, string, error)
	ValidateAssignmentRule(req *model.AssignmentRuleCreateRequest) error
}

// assignmentService 分配服务实现
type assignmentService struct {
	assignmentRepo repository.AssignmentRepository
	ruleRepo       repository.AssignmentRuleRepository
	timelineRepo   repository.TimelineRepository
	vulnRepo       repository.VulnerabilityRepository
	userRepo       repository.UserRepository
	assetRepo      repository.AssetRepository
}

// NewAssignmentService 创建分配服务
func NewAssignmentService(assignmentRepo repository.AssignmentRepository, ruleRepo repository.AssignmentRuleRepository, timelineRepo repository.TimelineRepository, vulnRepo repository.VulnerabilityRepository, userRepo repository.UserRepository, assetRepo repository.AssetRepository) AssignmentService {
	return &assignmentService{
		assignmentRepo: assignmentRepo,
		ruleRepo:       ruleRepo,
		timelineRepo:   timelineRepo,
		vulnRepo:       vulnRepo,
		userRepo:       userRepo,
		assetRepo:      assetRepo,
	}
}

// CreateAssignmentRule 创建分配规则
func (s *assignmentService) CreateAssignmentRule(req *model.AssignmentRuleCreateRequest) (*model.AssignmentRule, error) {
	// 验证规则数据
	if err := s.ValidateAssignmentRule(req); err != nil {
		return nil, err
	}

	// 验证分配人是否存在
	if _, err := s.userRepo.GetByID(req.AssigneeID); err != nil {
		return nil, errors.New("指定的分配人不存在")
	}

	rule := &model.AssignmentRule{
		Name:       req.Name,
		RuleType:   req.RuleType,
		Conditions: req.Conditions,
		AssigneeID: req.AssigneeID,
		Priority:   req.Priority,
		IsActive:   true,
	}

	if err := s.ruleRepo.Create(rule); err != nil {
		return nil, errors.New("创建分配规则失败")
	}

	return rule, nil
}

// GetAssignmentRuleByID 根据ID获取分配规则
func (s *assignmentService) GetAssignmentRuleByID(id uint) (*model.AssignmentRule, error) {
	return s.ruleRepo.GetByID(id)
}

// UpdateAssignmentRule 更新分配规则
func (s *assignmentService) UpdateAssignmentRule(id uint, req *model.AssignmentRuleUpdateRequest) error {
	rule, err := s.ruleRepo.GetByID(id)
	if err != nil {
		return err
	}

	// 更新字段
	if req.Name != "" {
		rule.Name = req.Name
	}

	if req.RuleType != "" {
		rule.RuleType = req.RuleType
	}

	if req.Conditions != nil {
		rule.Conditions = req.Conditions
	}

	if req.AssigneeID != nil {
		// 验证分配人是否存在
		if _, err := s.userRepo.GetByID(*req.AssigneeID); err != nil {
			return errors.New("指定的分配人不存在")
		}
		rule.AssigneeID = *req.AssigneeID
	}

	if req.Priority != nil {
		rule.Priority = *req.Priority
	}

	if req.IsActive != nil {
		rule.IsActive = *req.IsActive
	}

	return s.ruleRepo.Update(rule)
}

// DeleteAssignmentRule 删除分配规则
func (s *assignmentService) DeleteAssignmentRule(id uint) error {
	// 检查规则是否存在
	if _, err := s.ruleRepo.GetByID(id); err != nil {
		return err
	}

	return s.ruleRepo.Delete(id)
}

// GetAssignmentRuleList 获取分配规则列表
func (s *assignmentService) GetAssignmentRuleList(req *model.AssignmentRuleSearchRequest) (*model.PaginationResponse, error) {
	rules, total, err := s.ruleRepo.List(req)
	if err != nil {
		return nil, err
	}

	return model.NewPaginationResponse(total, req.Page, req.PageSize, rules), nil
}

// AssignVulnerability 分配漏洞
func (s *assignmentService) AssignVulnerability(vulnID uint, req *model.VulnerabilityAssignRequest, assignerID uint) error {
	// 获取漏洞信息
	vulnerability, err := s.vulnRepo.GetByID(vulnID)
	if err != nil {
		return err
	}

	// 验证分配人是否存在
	if _, err := s.userRepo.GetByID(req.AssigneeID); err != nil {
		return errors.New("指定的分配人不存在")
	}

	// 记录旧的分配人
	oldAssigneeID := vulnerability.AssigneeID
	var oldAssigneeName string
	if oldAssigneeID != nil {
		if oldAssignee, err := s.userRepo.GetByID(*oldAssigneeID); err == nil {
			oldAssigneeName = oldAssignee.RealName
		}
	}

	// 更新漏洞分配
	if err := s.vulnRepo.AssignToUser(vulnID, req.AssigneeID, assignerID); err != nil {
		return err
	}

	// 创建分配记录
	assignment := &model.VulnerabilityAssignment{
		VulnerabilityID:  vulnID,
		AssigneeID:       req.AssigneeID,
		AssignerID:       assignerID,
		AssignmentType:   model.AssignmentTypeManual,
		AssignmentReason: req.AssignmentReason,
	}
	if err := s.assignmentRepo.CreateAssignment(assignment); err != nil {
		return err
	}

	// 获取新分配人信息
	newAssignee, _ := s.userRepo.GetByID(req.AssigneeID)
	
	// 创建时间线记录
	description := fmt.Sprintf("漏洞被分配给 %s", newAssignee.RealName)
	if req.AssignmentReason != "" {
		description += fmt.Sprintf("，原因：%s", req.AssignmentReason)
	}
	
	return s.timelineRepo.CreateTimelineEntry(
		vulnID,
		model.TimelineActionAssigned,
		description,
		oldAssigneeName,
		newAssignee.RealName,
		assignerID,
	)
}

// BatchAssignVulnerabilities 批量分配漏洞
func (s *assignmentService) BatchAssignVulnerabilities(req *model.BatchAssignRequest, assignerID uint) ([]string, error) {
	var errorList []string

	// 验证分配人是否存在
	_, err := s.userRepo.GetByID(req.AssigneeID)
	if err != nil {
		return nil, errors.New("指定的分配人不存在")
	}

	for _, vulnID := range req.VulnerabilityIDs {
		assignReq := &model.VulnerabilityAssignRequest{
			AssigneeID:       req.AssigneeID,
			AssignmentReason: req.AssignmentReason,
		}
		
		if err := s.AssignVulnerability(vulnID, assignReq, assignerID); err != nil {
			errorList = append(errorList, fmt.Sprintf("漏洞ID %d: %s", vulnID, err.Error()))
		}
	}

	return errorList, nil
}

// AutoAssignVulnerabilities 自动分配漏洞
func (s *assignmentService) AutoAssignVulnerabilities(req *model.AutoAssignRequest) ([]string, error) {
	var errorList []string
	var vulnIDs []uint

	// 如果没有指定漏洞ID，则获取所有未分配的漏洞
	if len(req.VulnerabilityIDs) == 0 {
		// TODO: 实现获取未分配漏洞的逻辑
		return []string{"未指定要分配的漏洞"}, nil
	} else {
		vulnIDs = req.VulnerabilityIDs
	}

	for _, vulnID := range vulnIDs {
		vulnerability, err := s.vulnRepo.GetByID(vulnID)
		if err != nil {
			errorList = append(errorList, fmt.Sprintf("漏洞ID %d: %s", vulnID, err.Error()))
			continue
		}

		// 如果已分配且不强制重新分配，则跳过
		if vulnerability.AssigneeID != nil && !req.ForceReassign {
			continue
		}

		// 查找最佳分配人
		assigneeID, reason, err := s.FindBestAssignee(vulnerability)
		if err != nil {
			errorList = append(errorList, fmt.Sprintf("漏洞ID %d: %s", vulnID, err.Error()))
			continue
		}

		if assigneeID == 0 {
			errorList = append(errorList, fmt.Sprintf("漏洞ID %d: 未找到合适的分配人", vulnID))
			continue
		}

		// 执行自动分配
		if err := s.vulnRepo.AssignToUser(vulnID, assigneeID, 1); err != nil { // 使用系统用户ID 1
			errorList = append(errorList, fmt.Sprintf("漏洞ID %d: %s", vulnID, err.Error()))
			continue
		}

		// 创建分配记录
		assignment := &model.VulnerabilityAssignment{
			VulnerabilityID:  vulnID,
			AssigneeID:       assigneeID,
			AssignerID:       1, // 系统自动分配
			AssignmentType:   model.AssignmentTypeAuto,
			AssignmentReason: reason,
		}
		s.assignmentRepo.CreateAssignment(assignment)

		// 创建时间线记录
		assignee, _ := s.userRepo.GetByID(assigneeID)
		description := fmt.Sprintf("系统自动分配给 %s，匹配规则：%s", assignee.RealName, reason)
		s.timelineRepo.CreateTimelineEntry(
			vulnID,
			model.TimelineActionAssigned,
			description,
			"",
			assignee.RealName,
			1,
		)
	}

	return errorList, nil
}

// ReassignVulnerability 重新分配漏洞
func (s *assignmentService) ReassignVulnerability(vulnID uint, newAssigneeID uint, assignerID uint, reason string) error {
	req := &model.VulnerabilityAssignRequest{
		AssigneeID:       newAssigneeID,
		AssignmentReason: reason,
	}
	return s.AssignVulnerability(vulnID, req, assignerID)
}

// GetAssignmentStats 获取分配统计
func (s *assignmentService) GetAssignmentStats() (*model.AssignmentStats, error) {
	return s.assignmentRepo.GetAssignmentStats()
}

// GetVulnerabilityTimeline 获取漏洞时间线
func (s *assignmentService) GetVulnerabilityTimeline(vulnID uint) ([]*model.TimelineEntry, error) {
	timeline, err := s.timelineRepo.GetByVulnerabilityID(vulnID)
	if err != nil {
		return nil, err
	}

	var entries []*model.TimelineEntry
	for _, item := range timeline {
		entry := &model.TimelineEntry{
			ID:          item.ID,
			CreatedAt:   item.CreatedAt,
			Action:      item.Action,
			Description: item.Description,
			OldValue:    item.OldValue,
			NewValue:    item.NewValue,
			User:        item.User.ToUserInfo(),
		}
		entries = append(entries, entry)
	}

	return entries, nil
}

// FindBestAssignee 查找最佳分配人
func (s *assignmentService) FindBestAssignee(vulnerability *model.Vulnerability) (uint, string, error) {
	// 获取激活的分配规则，按优先级排序
	rules, err := s.ruleRepo.GetActiveRules()
	if err != nil {
		return 0, "", err
	}

	// 获取资产信息
	asset, err := s.assetRepo.GetByID(vulnerability.AssetID)
	if err != nil {
		return 0, "", err
	}

	// 遍历规则，找到第一个匹配的
	for _, rule := range rules {
		if s.matchRule(rule, vulnerability, asset) {
			return rule.AssigneeID, rule.Name, nil
		}
	}

	return 0, "", errors.New("未找到匹配的分配规则")
}

// matchRule 检查规则是否匹配
func (s *assignmentService) matchRule(rule *model.AssignmentRule, vulnerability *model.Vulnerability, asset *model.Asset) bool {
	switch rule.RuleType {
	case model.RuleTypeAssetType:
		if assetType, ok := rule.Conditions["asset_type"].(string); ok {
			return asset.Type == assetType
		}
	case model.RuleTypeSeverity:
		if severity, ok := rule.Conditions["severity_level"].(float64); ok {
			return vulnerability.SeverityLevel == int(severity)
		}
	case model.RuleTypeDepartment:
		if department, ok := rule.Conditions["department"].(string); ok {
			return asset.Department == department
		}
	case model.RuleTypeOWASPCategory:
		if owaspID, ok := rule.Conditions["owasp_category_id"].(float64); ok {
			return vulnerability.OWASPCategoryID != nil && *vulnerability.OWASPCategoryID == uint(owaspID)
		}
	}
	return false
}

// ValidateAssignmentRule 验证分配规则
func (s *assignmentService) ValidateAssignmentRule(req *model.AssignmentRuleCreateRequest) error {
	if req.Name == "" {
		return errors.New("规则名称不能为空")
	}

	if req.AssigneeID == 0 {
		return errors.New("必须指定分配人")
	}

	// 验证规则类型
	validTypes := []string{model.RuleTypeAssetType, model.RuleTypeSeverity, model.RuleTypeDepartment, model.RuleTypeOWASPCategory}
	isValidType := false
	for _, validType := range validTypes {
		if req.RuleType == validType {
			isValidType = true
			break
		}
	}
	if !isValidType {
		return errors.New("无效的规则类型")
	}

	// 验证条件
	if len(req.Conditions) == 0 {
		return errors.New("规则条件不能为空")
	}

	return nil
}
