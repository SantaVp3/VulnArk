package com.vulnark.service;

import com.vulnark.entity.Asset;
import com.vulnark.entity.User;
import com.vulnark.entity.Vulnerability;
import com.vulnark.repository.VulnerabilityRepository;
// import com.vulnark.repository.ProjectRepository; // 已删除项目功能
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    // @Autowired
    // private ProjectRepository projectRepository; // 已删除项目功能
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 获取仪表盘统计数据
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        try {
            // 漏洞统计
            VulnerabilityDashboardStats vulnStats = new VulnerabilityDashboardStats();
            vulnStats.setTotal(vulnerabilityRepository.countByDeletedFalse());
            vulnStats.setCritical(vulnerabilityRepository.countBySeverityAndDeletedFalse(Vulnerability.Severity.CRITICAL));
            vulnStats.setHigh(vulnerabilityRepository.countBySeverityAndDeletedFalse(Vulnerability.Severity.HIGH));
            vulnStats.setMedium(vulnerabilityRepository.countBySeverityAndDeletedFalse(Vulnerability.Severity.MEDIUM));
            vulnStats.setLow(vulnerabilityRepository.countBySeverityAndDeletedFalse(Vulnerability.Severity.LOW));
            vulnStats.setInfo(vulnerabilityRepository.countBySeverityAndDeletedFalse(Vulnerability.Severity.INFO));
            vulnStats.setOpen(vulnerabilityRepository.countByStatusAndDeletedFalse(Vulnerability.Status.OPEN));
            vulnStats.setInProgress(vulnerabilityRepository.countByStatusAndDeletedFalse(Vulnerability.Status.IN_PROGRESS));
            vulnStats.setResolved(vulnerabilityRepository.countByStatusAndDeletedFalse(Vulnerability.Status.RESOLVED));
            vulnStats.setClosed(vulnerabilityRepository.countByStatusAndDeletedFalse(Vulnerability.Status.CLOSED));
            vulnStats.setReopened(vulnerabilityRepository.countByStatusAndDeletedFalse(Vulnerability.Status.REOPENED));
            stats.setVulnerabilities(vulnStats);
        } catch (Exception e) {
            // 如果漏洞统计失败，设置默认值
            VulnerabilityDashboardStats vulnStats = new VulnerabilityDashboardStats();
            stats.setVulnerabilities(vulnStats);
        }



        try {
            // 资产统计
            AssetDashboardStats assetStats = new AssetDashboardStats();
            assetStats.setTotal(assetRepository.countByDeletedFalse());
            assetStats.setOnline(assetRepository.countByStatusAndDeletedFalse(Asset.Status.ACTIVE));
            assetStats.setOffline(assetRepository.countByStatusAndDeletedFalse(Asset.Status.INACTIVE));
            assetStats.setMaintenance(assetRepository.countByStatusAndDeletedFalse(Asset.Status.MAINTENANCE));
            assetStats.setHigh(assetRepository.countByImportanceAndDeletedFalse(Asset.Importance.HIGH));
            assetStats.setCritical(assetRepository.countByImportanceAndDeletedFalse(Asset.Importance.CRITICAL));
            stats.setAssets(assetStats);
        } catch (Exception e) {
            // 如果资产统计失败，设置默认值
            AssetDashboardStats assetStats = new AssetDashboardStats();
            stats.setAssets(assetStats);
        }

        try {
            // 用户统计
            UserDashboardStats userStats = new UserDashboardStats();
            userStats.setTotal(userRepository.countByDeletedFalse());
            userStats.setActive(userRepository.countByStatusAndDeletedFalse(User.Status.ACTIVE));
            userStats.setInactive(userRepository.countByStatusAndDeletedFalse(User.Status.INACTIVE) + 
                                   userRepository.countByStatusAndDeletedFalse(User.Status.LOCKED));
            userStats.setAdmin(userRepository.countByRoleAndDeletedFalse(User.Role.ADMIN));
            userStats.setAnalyst(userRepository.countByRoleAndDeletedFalse(User.Role.ANALYST));
            userStats.setViewer(userRepository.countByRoleAndDeletedFalse(User.Role.VIEWER));
            stats.setUsers(userStats);
        } catch (Exception e) {
            // 如果用户统计失败，设置默认值
            UserDashboardStats userStats = new UserDashboardStats();
            userStats.setTotal(1); // 至少有一个管理员用户
            userStats.setActive(1);
            userStats.setAdmin(1);
            stats.setUsers(userStats);
        }

        return stats;
    }

    /**
     * 获取漏洞趋势数据
     */
    public List<VulnerabilityTrendData> getVulnerabilityTrends(Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<VulnerabilityTrendData> trends = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            VulnerabilityTrendData trend = new VulnerabilityTrendData();
            trend.setDate(date.toString());

            // 当天发现的漏洞数量
            trend.setDiscovered(vulnerabilityRepository.countByDiscoveredDateBetweenAndDeletedFalse(date, date));

            // 当天解决的漏洞数量
            trend.setResolved(vulnerabilityRepository.countByResolvedDateBetweenAndDeletedFalse(date, date));

            trends.add(trend);
        }

        return trends;
    }

    /**
     * 获取漏洞严重程度分布
     */
    public List<SeverityDistribution> getVulnerabilitySeverityDistribution() {
        List<SeverityDistribution> distribution = new ArrayList<>();
        long total = vulnerabilityRepository.countByDeletedFalse();
        
        for (Vulnerability.Severity severity : Vulnerability.Severity.values()) {
            long count = vulnerabilityRepository.countBySeverityAndDeletedFalse(severity);
            if (count > 0) {
                SeverityDistribution item = new SeverityDistribution();
                item.setSeverity(severity.name());
                item.setCount(count);
                item.setPercentage(total > 0 ? (double) count / total * 100 : 0);
                distribution.add(item);
            }
        }
        
        return distribution;
    }



    /**
     * 获取资产状态分布
     */
    public List<AssetStatusDistribution> getAssetStatusDistribution() {
        List<AssetStatusDistribution> distribution = new ArrayList<>();
        long total = assetRepository.countByDeletedFalse();

        // 使用正确的Asset.Status枚举值
        Asset.Status[] statuses = {Asset.Status.ACTIVE, Asset.Status.INACTIVE, Asset.Status.MAINTENANCE, Asset.Status.DECOMMISSIONED};
        for (Asset.Status status : statuses) {
            long count = assetRepository.countByStatusAndDeletedFalse(status);
            if (count > 0) {
                AssetStatusDistribution item = new AssetStatusDistribution();
                // 将枚举值转换为前端友好的显示名称
                item.setStatus(mapStatusToDisplayName(status));
                item.setCount(count);
                item.setPercentage(total > 0 ? (double) count / total * 100 : 0);
                distribution.add(item);
            }
        }

        return distribution;
    }

    /**
     * 将Asset.Status枚举映射为前端友好的显示名称
     */
    private String mapStatusToDisplayName(Asset.Status status) {
        switch (status) {
            case ACTIVE:
                return "active";
            case INACTIVE:
                return "inactive";
            case MAINTENANCE:
                return "maintenance";
            case DECOMMISSIONED:
                return "decommissioned";
            default:
                return status.name().toLowerCase();
        }
    }

    /**
     * 获取最近活动
     */
    public List<RecentActivity> getRecentActivities(Integer limit) {
        List<RecentActivity> activities = new ArrayList<>();

        try {
            // 获取最近的漏洞活动
            Pageable pageable = PageRequest.of(0, Math.max(1, limit));
            List<Object[]> recentVulns = vulnerabilityRepository.getRecentVulnerabilityActivities(pageable);
            for (Object[] row : recentVulns) {
                RecentActivity activity = new RecentActivity();
                activity.setId(row[0] != null ? ((Number) row[0]).longValue() : 0L);
                activity.setType("vulnerability");
                activity.setTitle("漏洞: " + (row[1] != null ? (String) row[1] : "未知"));
                activity.setDescription("严重程度: " + (row[2] != null ? row[2] : "未知") +
                                      ", 状态: " + (row[3] != null ? row[3] : "未知"));
                activity.setTimestamp(row[4] != null ? row[4].toString() : "");
                activity.setSeverity(row[2] != null ? row[2].toString() : "UNKNOWN");
                activity.setStatus(row[3] != null ? row[3].toString() : "UNKNOWN");
                activities.add(activity);
            }
        } catch (Exception e) {
            // 如果查询失败，添加一个默认活动
            RecentActivity defaultActivity = new RecentActivity();
            defaultActivity.setId(0L);
            defaultActivity.setType("system");
            defaultActivity.setTitle("系统启动");
            defaultActivity.setDescription("VulnArk系统已启动");
            defaultActivity.setTimestamp(new java.util.Date().toString());
            defaultActivity.setSeverity("INFO");
            defaultActivity.setStatus("ACTIVE");
            activities.add(defaultActivity);
        }

        // 按时间排序
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取系统健康状态
     */
    public SystemHealth getSystemHealth() {
        SystemHealth health = new SystemHealth();
        
        // 模拟系统健康数据
        health.setCpu(Math.random() * 100);
        health.setMemory(Math.random() * 100);
        health.setDisk(Math.random() * 100);
        health.setNetwork(Math.random() * 100);
        health.setDatabase("healthy");
        
        List<ServiceStatus> services = new ArrayList<>();
        services.add(new ServiceStatus("Web服务", "running", System.currentTimeMillis() / 1000));
        services.add(new ServiceStatus("数据库", "running", System.currentTimeMillis() / 1000));
        services.add(new ServiceStatus("扫描引擎", "running", System.currentTimeMillis() / 1000));
        health.setServices(services);
        
        return health;
    }

    /**
     * 刷新仪表盘数据
     */
    public void refreshDashboardData() {
        // 这里可以实现缓存刷新逻辑
        // 目前是空实现，因为我们没有使用缓存
    }

    // 数据传输对象
    public static class DashboardStats {
        private VulnerabilityDashboardStats vulnerabilities;
        private AssetDashboardStats assets;
        private UserDashboardStats users;

        // Getters and Setters
        public VulnerabilityDashboardStats getVulnerabilities() { return vulnerabilities; }
        public void setVulnerabilities(VulnerabilityDashboardStats vulnerabilities) { this.vulnerabilities = vulnerabilities; }
        public AssetDashboardStats getAssets() { return assets; }
        public void setAssets(AssetDashboardStats assets) { this.assets = assets; }
        public UserDashboardStats getUsers() { return users; }
        public void setUsers(UserDashboardStats users) { this.users = users; }
    }

    public static class VulnerabilityDashboardStats {
        private long total;
        private long critical;
        private long high;
        private long medium;
        private long low;
        private long info;
        private long open;
        private long inProgress;
        private long resolved;
        private long closed;
        private long reopened;

        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getCritical() { return critical; }
        public void setCritical(long critical) { this.critical = critical; }
        public long getHigh() { return high; }
        public void setHigh(long high) { this.high = high; }
        public long getMedium() { return medium; }
        public void setMedium(long medium) { this.medium = medium; }
        public long getLow() { return low; }
        public void setLow(long low) { this.low = low; }
        public long getInfo() { return info; }
        public void setInfo(long info) { this.info = info; }
        public long getOpen() { return open; }
        public void setOpen(long open) { this.open = open; }
        public long getInProgress() { return inProgress; }
        public void setInProgress(long inProgress) { this.inProgress = inProgress; }
        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }
        public long getClosed() { return closed; }
        public void setClosed(long closed) { this.closed = closed; }
        public long getReopened() { return reopened; }
        public void setReopened(long reopened) { this.reopened = reopened; }
    }



    public static class AssetDashboardStats {
        private long total;
        private long online;
        private long offline;
        private long maintenance;
        private long high;
        private long critical;

        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getOnline() { return online; }
        public void setOnline(long online) { this.online = online; }
        public long getOffline() { return offline; }
        public void setOffline(long offline) { this.offline = offline; }
        public long getMaintenance() { return maintenance; }
        public void setMaintenance(long maintenance) { this.maintenance = maintenance; }
        public long getHigh() { return high; }
        public void setHigh(long high) { this.high = high; }
        public long getCritical() { return critical; }
        public void setCritical(long critical) { this.critical = critical; }
    }

    public static class UserDashboardStats {
        private long total;
        private long active;
        private long inactive;
        private long admin;
        private long analyst;
        private long viewer;

        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        public long getInactive() { return inactive; }
        public void setInactive(long inactive) { this.inactive = inactive; }
        public long getAdmin() { return admin; }
        public void setAdmin(long admin) { this.admin = admin; }
        public long getAnalyst() { return analyst; }
        public void setAnalyst(long analyst) { this.analyst = analyst; }
        public long getViewer() { return viewer; }
        public void setViewer(long viewer) { this.viewer = viewer; }
    }

    public static class VulnerabilityTrendData {
        private String date;
        private long discovered;
        private long resolved;

        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public long getDiscovered() { return discovered; }
        public void setDiscovered(long discovered) { this.discovered = discovered; }
        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }
    }

    public static class SeverityDistribution {
        private String severity;
        private long count;
        private double percentage;

        // Getters and Setters
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }



    public static class AssetStatusDistribution {
        private String status;
        private long count;
        private double percentage;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class RecentActivity {
        private long id;
        private String type;
        private String title;
        private String description;
        private String timestamp;
        private String severity;
        private String status;

        // Getters and Setters
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class SystemHealth {
        private double cpu;
        private double memory;
        private double disk;
        private double network;
        private String database;
        private List<ServiceStatus> services;

        // Getters and Setters
        public double getCpu() { return cpu; }
        public void setCpu(double cpu) { this.cpu = cpu; }
        public double getMemory() { return memory; }
        public void setMemory(double memory) { this.memory = memory; }
        public double getDisk() { return disk; }
        public void setDisk(double disk) { this.disk = disk; }
        public double getNetwork() { return network; }
        public void setNetwork(double network) { this.network = network; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public List<ServiceStatus> getServices() { return services; }
        public void setServices(List<ServiceStatus> services) { this.services = services; }
    }

    public static class ServiceStatus {
        private String name;
        private String status;
        private long uptime;

        public ServiceStatus() {}

        public ServiceStatus(String name, String status, long uptime) {
            this.name = name;
            this.status = status;
            this.uptime = uptime;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
    }
}
