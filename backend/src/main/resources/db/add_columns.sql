-- 添加projects表缺失的字段
ALTER TABLE projects ADD COLUMN type VARCHAR(50) COMMENT '项目类型';
ALTER TABLE projects ADD COLUMN budget DECIMAL(15,2) COMMENT '预算';
ALTER TABLE projects ADD COLUMN tags VARCHAR(500) COMMENT '项目标签';
ALTER TABLE projects ADD COLUMN member_count INT DEFAULT 0 COMMENT '项目成员数量';
ALTER TABLE projects ADD COLUMN vulnerability_count INT DEFAULT 0 COMMENT '漏洞数量';
ALTER TABLE projects ADD COLUMN asset_count INT DEFAULT 0 COMMENT '资产数量';
ALTER TABLE projects ADD COLUMN progress INT DEFAULT 0 COMMENT '项目进度百分比';
