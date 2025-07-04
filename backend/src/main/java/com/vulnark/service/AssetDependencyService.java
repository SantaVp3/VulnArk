package com.vulnark.service;

import com.vulnark.dto.AssetDependencyRequest;
import com.vulnark.dto.AssetDependencyResponse;
import com.vulnark.entity.Asset;
import com.vulnark.entity.AssetDependency;
import com.vulnark.repository.AssetDependencyRepository;
import com.vulnark.repository.AssetRepository;
// import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产依赖关系服务
 */
@Service
@Transactional
public class AssetDependencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssetDependencyService.class);
    
    @Autowired
    private AssetDependencyRepository dependencyRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    /**
     * 创建资产依赖关系
     */
    public AssetDependency createDependency(AssetDependencyRequest request, Long createdBy) {
        // 验证资产是否存在
        validateAssetsExist(request.getSourceAssetId(), request.getTargetAssetId());
        
        // 检查是否为循环依赖
        if (request.getSourceAssetId().equals(request.getTargetAssetId())) {
            throw new IllegalArgumentException("不能创建循环依赖关系");
        }
        
        // 检查依赖关系是否已存在
        if (dependencyRepository.existsDependency(request.getSourceAssetId(), request.getTargetAssetId())) {
            throw new IllegalArgumentException("依赖关系已存在");
        }
        
        AssetDependency dependency = new AssetDependency();
        BeanUtils.copyProperties(request, dependency);
        dependency.setCreatedBy(createdBy);
        
        AssetDependency saved = dependencyRepository.save(dependency);
        logger.info("创建资产依赖关系: {} -> {}", request.getSourceAssetId(), request.getTargetAssetId());
        
        return saved;
    }
    
    /**
     * 更新资产依赖关系
     */
    public AssetDependency updateDependency(Long id, AssetDependencyRequest request) {
        AssetDependency dependency = dependencyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("依赖关系不存在: " + id));
        
        // 如果修改了资产ID，需要重新验证
        if (!dependency.getSourceAssetId().equals(request.getSourceAssetId()) ||
            !dependency.getTargetAssetId().equals(request.getTargetAssetId())) {
            validateAssetsExist(request.getSourceAssetId(), request.getTargetAssetId());
            
            // 检查是否为循环依赖
            if (request.getSourceAssetId().equals(request.getTargetAssetId())) {
                throw new IllegalArgumentException("不能创建循环依赖关系");
            }
        }
        
        BeanUtils.copyProperties(request, dependency, "id", "createdTime", "createdBy");
        
        AssetDependency updated = dependencyRepository.save(dependency);
        logger.info("更新资产依赖关系: {}", id);
        
        return updated;
    }
    
    /**
     * 删除资产依赖关系
     */
    public void deleteDependency(Long id) {
        AssetDependency dependency = dependencyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("依赖关系不存在: " + id));
        
        dependency.setDeleted(true);
        dependencyRepository.save(dependency);
        
        logger.info("删除资产依赖关系: {}", id);
    }
    
    /**
     * 批量删除资产依赖关系
     */
    public void batchDeleteDependencies(List<Long> ids) {
        List<AssetDependency> dependencies = dependencyRepository.findAllById(ids);
        dependencies.forEach(dep -> dep.setDeleted(true));
        dependencyRepository.saveAll(dependencies);
        
        logger.info("批量删除资产依赖关系: {}", ids);
    }
    
    /**
     * 根据ID获取资产依赖关系
     */
    public AssetDependencyResponse getDependencyById(Long id) {
        AssetDependency dependency = dependencyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("依赖关系不存在: " + id));
        
        return AssetDependencyResponse.fromEntity(dependency);
    }
    
    /**
     * 获取资产的所有依赖关系
     */
    public List<AssetDependencyResponse> getAssetDependencies(Long assetId) {
        List<AssetDependency> dependencies = dependencyRepository.findByAssetId(assetId);
        return dependencies.stream()
                .map(AssetDependencyResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取资产的直接依赖
     */
    public List<AssetDependencyResponse> getDirectDependencies(Long assetId) {
        List<AssetDependency> dependencies = dependencyRepository.findDirectDependencies(assetId);
        return dependencies.stream()
                .map(AssetDependencyResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取资产的反向依赖
     */
    public List<AssetDependencyResponse> getReverseDependencies(Long assetId) {
        List<AssetDependency> dependencies = dependencyRepository.findReverseDependencies(assetId);
        return dependencies.stream()
                .map(AssetDependencyResponse::fromEntity)
                .collect(Collectors.toList());
    }
    

    
    /**
     * 分析资产依赖路径
     */
    public DependencyPathAnalysis analyzeDependencyPath(Long sourceAssetId, Long targetAssetId) {
        // 使用广度优先搜索找到最短路径
        List<List<Long>> paths = findDependencyPaths(sourceAssetId, targetAssetId, 5); // 最大深度5
        
        DependencyPathAnalysis analysis = new DependencyPathAnalysis();
        analysis.setSourceAssetId(sourceAssetId);
        analysis.setTargetAssetId(targetAssetId);
        analysis.setPaths(paths);
        analysis.setHasPath(!paths.isEmpty());
        
        if (!paths.isEmpty()) {
            analysis.setShortestPathLength(paths.get(0).size() - 1);
            analysis.setTotalPaths(paths.size());
        }
        
        return analysis;
    }
    
    /**
     * 检测循环依赖
     */
    public List<AssetDependencyResponse> detectCircularDependencies() {
        List<AssetDependency> circularDeps = dependencyRepository.findCircularDependencies();
        return circularDeps.stream()
                .map(AssetDependencyResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取依赖统计信息
     */
    public DependencyStatistics getDependencyStatistics(Long projectId) {
        // 项目功能已删除，直接获取所有依赖关系
        List<AssetDependency> dependencies = dependencyRepository.findByDeletedFalse();
        
        DependencyStatistics stats = new DependencyStatistics();
        stats.setTotalDependencies(dependencies.size());
        
        // 按类型统计
        Map<AssetDependency.DependencyType, Long> typeStats = dependencies.stream()
                .collect(Collectors.groupingBy(AssetDependency::getDependencyType, Collectors.counting()));
        stats.setDependencyTypeStats(typeStats);
        
        // 按强度统计
        Map<AssetDependency.DependencyStrength, Long> strengthStats = dependencies.stream()
                .collect(Collectors.groupingBy(AssetDependency::getDependencyStrength, Collectors.counting()));
        stats.setDependencyStrengthStats(strengthStats);
        
        // 关键依赖数量
        long criticalCount = dependencies.stream()
                .mapToLong(dep -> dep.getIsCritical() ? 1 : 0)
                .sum();
        stats.setCriticalDependencies(criticalCount);
        
        // 断开的依赖数量
        long brokenCount = dependencies.stream()
                .mapToLong(dep -> dep.getStatus() == AssetDependency.DependencyStatus.BROKEN ? 1 : 0)
                .sum();
        stats.setBrokenDependencies(brokenCount);
        
        return stats;
    }
    
    // 私有辅助方法
    
    private void validateAssetsExist(Long sourceAssetId, Long targetAssetId) {
        if (!assetRepository.existsByIdAndDeletedFalse(sourceAssetId)) {
            throw new IllegalArgumentException("源资产不存在: " + sourceAssetId);
        }
        if (!assetRepository.existsByIdAndDeletedFalse(targetAssetId)) {
            throw new IllegalArgumentException("目标资产不存在: " + targetAssetId);
        }
    }
    
    private void calculateNodePositions(List<AssetDependencyNode> nodes) {
        int centerX = 400;
        int centerY = 300;
        int radius = 200;
        
        for (int i = 0; i < nodes.size(); i++) {
            double angle = 2 * Math.PI * i / nodes.size();
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            
            nodes.get(i).setX(x);
            nodes.get(i).setY(y);
        }
    }
    
    private List<List<Long>> findDependencyPaths(Long sourceId, Long targetId, int maxDepth) {
        List<List<Long>> paths = new ArrayList<>();
        List<Long> currentPath = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        
        currentPath.add(sourceId);
        findPathsRecursive(sourceId, targetId, currentPath, visited, paths, maxDepth);
        
        return paths;
    }
    
    private void findPathsRecursive(Long currentId, Long targetId, List<Long> currentPath, 
                                   Set<Long> visited, List<List<Long>> paths, int maxDepth) {
        if (currentPath.size() > maxDepth) {
            return;
        }
        
        if (currentId.equals(targetId)) {
            paths.add(new ArrayList<>(currentPath));
            return;
        }
        
        visited.add(currentId);
        
        List<AssetDependency> nextDeps = dependencyRepository.findBySourceAssetId(currentId);
        for (AssetDependency dep : nextDeps) {
            Long nextId = dep.getTargetAssetId();
            if (!visited.contains(nextId)) {
                currentPath.add(nextId);
                findPathsRecursive(nextId, targetId, currentPath, visited, paths, maxDepth);
                currentPath.remove(currentPath.size() - 1);
            }
        }
        
        visited.remove(currentId);
    }
    
    // 内部类定义
    
    public static class DependencyPathAnalysis {
        private Long sourceAssetId;
        private Long targetAssetId;
        private List<List<Long>> paths;
        private boolean hasPath;
        private int shortestPathLength;
        private int totalPaths;

        // Getter and Setter methods
        public Long getSourceAssetId() { return sourceAssetId; }
        public void setSourceAssetId(Long sourceAssetId) { this.sourceAssetId = sourceAssetId; }
        public Long getTargetAssetId() { return targetAssetId; }
        public void setTargetAssetId(Long targetAssetId) { this.targetAssetId = targetAssetId; }
        public List<List<Long>> getPaths() { return paths; }
        public void setPaths(List<List<Long>> paths) { this.paths = paths; }
        public boolean getHasPath() { return hasPath; }
        public void setHasPath(boolean hasPath) { this.hasPath = hasPath; }
        public int getShortestPathLength() { return shortestPathLength; }
        public void setShortestPathLength(int shortestPathLength) { this.shortestPathLength = shortestPathLength; }
        public int getTotalPaths() { return totalPaths; }
        public void setTotalPaths(int totalPaths) { this.totalPaths = totalPaths; }
    }
    
    public static class DependencyStatistics {
        private int totalDependencies;
        private long criticalDependencies;
        private long brokenDependencies;
        private Map<AssetDependency.DependencyType, Long> dependencyTypeStats;
        private Map<AssetDependency.DependencyStrength, Long> dependencyStrengthStats;

        // Getter and Setter methods
        public int getTotalDependencies() { return totalDependencies; }
        public void setTotalDependencies(int totalDependencies) { this.totalDependencies = totalDependencies; }
        public long getCriticalDependencies() { return criticalDependencies; }
        public void setCriticalDependencies(long criticalDependencies) { this.criticalDependencies = criticalDependencies; }
        public long getBrokenDependencies() { return brokenDependencies; }
        public void setBrokenDependencies(long brokenDependencies) { this.brokenDependencies = brokenDependencies; }
        public Map<AssetDependency.DependencyType, Long> getDependencyTypeStats() { return dependencyTypeStats; }
        public void setDependencyTypeStats(Map<AssetDependency.DependencyType, Long> dependencyTypeStats) { this.dependencyTypeStats = dependencyTypeStats; }
        public Map<AssetDependency.DependencyStrength, Long> getDependencyStrengthStats() { return dependencyStrengthStats; }
        public void setDependencyStrengthStats(Map<AssetDependency.DependencyStrength, Long> dependencyStrengthStats) { this.dependencyStrengthStats = dependencyStrengthStats; }
    }

    public static class AssetDependencyNode {
        private Long id;
        private String name;
        private String type;
        private String status;
        private String importance;
        private Integer x;
        private Integer y;
        private String color;
        private String shape;
        private Integer size;

        public static AssetDependencyNode fromAsset(Asset asset) {
            AssetDependencyNode node = new AssetDependencyNode();
            node.setId(asset.getId());
            node.setName(asset.getName());
            node.setType(asset.getType().name());
            node.setStatus(asset.getStatus().name());
            node.setImportance(asset.getImportance().name());

            // 根据资产类型设置节点样式
            switch (asset.getType()) {
                case SERVER:
                    node.setShape("rect");
                    node.setColor("#1890ff");
                    break;
                case DATABASE:
                    node.setShape("cylinder");
                    node.setColor("#52c41a");
                    break;
                case WEB_APPLICATION:
                    node.setShape("ellipse");
                    node.setColor("#fa8c16");
                    break;
                case NETWORK_DEVICE:
                    node.setShape("diamond");
                    node.setColor("#722ed1");
                    break;
                default:
                    node.setShape("circle");
                    node.setColor("#8c8c8c");
            }

            // 根据重要性设置节点大小
            switch (asset.getImportance()) {
                case CRITICAL:
                    node.setSize(60);
                    break;
                case HIGH:
                    node.setSize(50);
                    break;
                case MEDIUM:
                    node.setSize(40);
                    break;
                case LOW:
                    node.setSize(30);
                    break;
            }

            return node;
        }

        // Getter and Setter methods
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getImportance() { return importance; }
        public void setImportance(String importance) { this.importance = importance; }
        public Integer getX() { return x; }
        public void setX(Integer x) { this.x = x; }
        public Integer getY() { return y; }
        public void setY(Integer y) { this.y = y; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getShape() { return shape; }
        public void setShape(String shape) { this.shape = shape; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }

    public static class AssetDependencyEdge {
        private Long id;
        private Long source;
        private Long target;
        private String type;
        private String strength;
        private String label;
        private String color;
        private Integer width;
        private String style;

        public static AssetDependencyEdge fromDependency(AssetDependency dependency) {
            AssetDependencyEdge edge = new AssetDependencyEdge();
            edge.setId(dependency.getId());
            edge.setSource(dependency.getSourceAssetId());
            edge.setTarget(dependency.getTargetAssetId());
            edge.setType(dependency.getDependencyType().name());
            edge.setStrength(dependency.getDependencyStrength().name());
            edge.setLabel(dependency.getDependencyType().getDescription());

            // 根据依赖强度设置边的样式
            switch (dependency.getDependencyStrength()) {
                case CRITICAL:
                    edge.setColor("#ff4d4f");
                    edge.setWidth(4);
                    edge.setStyle("solid");
                    break;
                case STRONG:
                    edge.setColor("#fa8c16");
                    edge.setWidth(3);
                    edge.setStyle("solid");
                    break;
                case MEDIUM:
                    edge.setColor("#1890ff");
                    edge.setWidth(2);
                    edge.setStyle("solid");
                    break;
                case WEAK:
                    edge.setColor("#8c8c8c");
                    edge.setWidth(1);
                    edge.setStyle("dashed");
                    break;
            }

            return edge;
        }

        // Getter and Setter methods
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getSource() { return source; }
        public void setSource(Long source) { this.source = source; }
        public Long getTarget() { return target; }
        public void setTarget(Long target) { this.target = target; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStrength() { return strength; }
        public void setStrength(String strength) { this.strength = strength; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
    }

    public static class AssetDependencyTopology {
        private List<AssetDependencyNode> nodes;
        private List<AssetDependencyEdge> edges;
        private Long projectId;
        private String projectName;
        private Integer totalNodes;
        private Integer totalEdges;

        // Getter and Setter methods
        public List<AssetDependencyNode> getNodes() { return nodes; }
        public void setNodes(List<AssetDependencyNode> nodes) { this.nodes = nodes; }
        public List<AssetDependencyEdge> getEdges() { return edges; }
        public void setEdges(List<AssetDependencyEdge> edges) { this.edges = edges; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public Integer getTotalNodes() { return totalNodes; }
        public void setTotalNodes(Integer totalNodes) { this.totalNodes = totalNodes; }
        public Integer getTotalEdges() { return totalEdges; }
        public void setTotalEdges(Integer totalEdges) { this.totalEdges = totalEdges; }
    }
}
