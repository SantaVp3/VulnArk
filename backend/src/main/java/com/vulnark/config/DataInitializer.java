package com.vulnark.config;

import com.vulnark.entity.User;
import com.vulnark.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;

/**
 * 数据初始化器
 * 确保系统启动时有默认的管理员用户
 */
@Component  // 启用数据初始化器，确保创建默认用户
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void initializeData() {
        logger.info("开始初始化系统数据...");
        
        // 初始化默认用户
        initializeDefaultUsers();
        
        logger.info("系统数据初始化完成");
    }

    @Override
    public void run(String... args) throws Exception {
        // 更新现有agents记录，为agent_id字段设置值
        try {
            jdbcTemplate.execute("UPDATE agents SET agent_id = CONCAT('agent-', id) WHERE agent_id IS NULL");
            logger.info("已更新agents表中的agent_id字段");
        } catch (Exception e) {
            logger.warn("更新agents表时出错: {}", e.getMessage());
        }
    }
    
    /**
     * 初始化默认用户
     */
    private void initializeDefaultUsers() {
        // 检查是否已存在admin用户
        if (userRepository.findByUsername("admin").isPresent()) {
            logger.info("默认admin用户已存在，跳过创建");
            return;
        }
        
        logger.info("创建默认admin用户...");
        
        // 创建默认admin用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@vulnark.com");
        adminUser.setPassword(passwordEncoder.encode("password123")); // 使用您提到的密码
        adminUser.setFullName("系统管理员");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.Status.ACTIVE);
        adminUser.setDeleted(false);
        
        try {
            userRepository.save(adminUser);
            logger.info("默认admin用户创建成功");
            logger.info("登录信息：用户名=admin, 密码=password123");
        } catch (Exception e) {
            logger.error("创建默认admin用户失败", e);
        }
        
        // 创建其他默认用户
        createDefaultUser("manager", "manager@vulnark.com", "项目经理", User.Role.MANAGER);
        createDefaultUser("analyst", "analyst@vulnark.com", "安全分析师", User.Role.ANALYST);
        createDefaultUser("viewer", "viewer@vulnark.com", "查看者", User.Role.VIEWER);
    }
    
    /**
     * 创建默认用户
     */
    private void createDefaultUser(String username, String email, String fullName, User.Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.info("用户 {} 已存在，跳过创建", username);
            return;
        }
        
        logger.info("创建默认用户: {}", username);
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123")); // 统一使用相同密码
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(User.Status.ACTIVE);
        user.setDeleted(false);
        
        try {
            userRepository.save(user);
            logger.info("用户 {} 创建成功", username);
        } catch (Exception e) {
            logger.error("创建用户 {} 失败", username, e);
        }
    }
}
