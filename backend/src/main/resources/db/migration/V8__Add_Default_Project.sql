-- 添加默认项目（如果不存在）
INSERT IGNORE INTO projects (id, name, description, owner_id, status, type, priority, start_date, end_date, budget, tags, member_count, vulnerability_count, asset_count, progress, created_time, updated_time, deleted)
SELECT 1, '默认项目', '系统默认项目，用于存放未分类资产', 1, 'ACTIVE', 'General', 'MEDIUM', CURRENT_DATE(), DATE_ADD(CURRENT_DATE(), INTERVAL 1 YEAR), 0.00, 'default', 1, 0, 0, 0, NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE id = 1); 