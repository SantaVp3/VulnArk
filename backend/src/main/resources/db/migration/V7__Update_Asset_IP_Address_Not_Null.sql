-- 更新资产表的IP地址字段为非空
ALTER TABLE assets MODIFY ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址';

-- 更新现有的空IP地址记录为默认值
UPDATE assets SET ip_address = '0.0.0.0' WHERE ip_address IS NULL OR ip_address = ''; 