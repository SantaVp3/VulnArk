-- 更新现有项目的新字段
UPDATE projects SET 
    type = CASE 
        WHEN name = 'Web应用安全测试' THEN 'Security Testing'
        WHEN name = '移动应用安全评估' THEN 'Mobile Security'
        WHEN name = '网络基础设施安全' THEN 'Infrastructure'
        ELSE 'General'
    END,
    budget = CASE 
        WHEN name = 'Web应用安全测试' THEN 500000.00
        WHEN name = '移动应用安全评估' THEN 300000.00
        WHEN name = '网络基础设施安全' THEN 800000.00
        ELSE 100000.00
    END,
    tags = CASE 
        WHEN name = 'Web应用安全测试' THEN 'web,security,testing'
        WHEN name = '移动应用安全评估' THEN 'mobile,security,audit'
        WHEN name = '网络基础设施安全' THEN 'network,infrastructure,hardening'
        ELSE 'general'
    END,
    member_count = 3,
    vulnerability_count = CASE 
        WHEN id = 1 THEN 3
        WHEN id = 2 THEN 1
        WHEN id = 3 THEN 1
        ELSE 0
    END,
    asset_count = CASE 
        WHEN id = 1 THEN 3
        WHEN id = 2 THEN 2
        WHEN id = 3 THEN 3
        ELSE 0
    END,
    progress = CASE 
        WHEN name = 'Web应用安全测试' THEN 75
        WHEN name = '移动应用安全评估' THEN 45
        WHEN name = '网络基础设施安全' THEN 60
        ELSE 0
    END
WHERE type IS NULL OR budget IS NULL;
