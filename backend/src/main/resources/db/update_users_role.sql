-- 修改users表的role字段，添加USER枚举值
ALTER TABLE users MODIFY COLUMN role ENUM('ADMIN', 'MANAGER', 'ANALYST', 'VIEWER', 'USER') NOT NULL DEFAULT 'VIEWER' COMMENT '角色'; 