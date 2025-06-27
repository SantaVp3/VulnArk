package com.vulnark.dto;

import com.vulnark.entity.AssetDependency;
import java.time.LocalDateTime;

/**
 * 资产依赖关系响应DTO
 */
public class AssetDependencyResponse {
    
    private Long id;
    private Long sourceAssetId;
    private String sourceAssetName;
    private Long targetAssetId;
    private String targetAssetName;
    private AssetDependency.DependencyType dependencyType;
    private String dependencyTypeDescription;
    private AssetDependency.DependencyStrength dependencyStrength;
    private String dependencyStrengthDescription;
    private String description;
    private Integer port;
    private String protocol;
    private String serviceName;
    private Boolean isCritical;
    private AssetDependency.DependencyStatus status;
    private String statusDescription;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Long createdBy;
    private String createdByName;
    
    /**
     * 从实体转换为响应DTO
     */
    public static AssetDependencyResponse fromEntity(AssetDependency dependency) {
        AssetDependencyResponse response = new AssetDependencyResponse();
        response.setId(dependency.getId());
        response.setSourceAssetId(dependency.getSourceAssetId());
        response.setTargetAssetId(dependency.getTargetAssetId());
        response.setDependencyType(dependency.getDependencyType());
        response.setDependencyTypeDescription(dependency.getDependencyType().getDescription());
        response.setDependencyStrength(dependency.getDependencyStrength());
        response.setDependencyStrengthDescription(dependency.getDependencyStrength().getDescription());
        response.setDescription(dependency.getDescription());
        response.setPort(dependency.getPort());
        response.setProtocol(dependency.getProtocol());
        response.setServiceName(dependency.getServiceName());
        response.setIsCritical(dependency.getIsCritical());
        response.setStatus(dependency.getStatus());
        response.setStatusDescription(dependency.getStatus().getDescription());
        response.setCreatedTime(dependency.getCreatedTime());
        response.setUpdatedTime(dependency.getUpdatedTime());
        response.setCreatedBy(dependency.getCreatedBy());
        
        // 设置资产名称（如果关联对象存在）
        if (dependency.getSourceAsset() != null) {
            response.setSourceAssetName(dependency.getSourceAsset().getName());
        }
        if (dependency.getTargetAsset() != null) {
            response.setTargetAssetName(dependency.getTargetAsset().getName());
        }
        
        return response;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceAssetId() {
        return sourceAssetId;
    }

    public void setSourceAssetId(Long sourceAssetId) {
        this.sourceAssetId = sourceAssetId;
    }

    public String getSourceAssetName() {
        return sourceAssetName;
    }

    public void setSourceAssetName(String sourceAssetName) {
        this.sourceAssetName = sourceAssetName;
    }

    public Long getTargetAssetId() {
        return targetAssetId;
    }

    public void setTargetAssetId(Long targetAssetId) {
        this.targetAssetId = targetAssetId;
    }

    public String getTargetAssetName() {
        return targetAssetName;
    }

    public void setTargetAssetName(String targetAssetName) {
        this.targetAssetName = targetAssetName;
    }

    public AssetDependency.DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(AssetDependency.DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public String getDependencyTypeDescription() {
        return dependencyTypeDescription;
    }

    public void setDependencyTypeDescription(String dependencyTypeDescription) {
        this.dependencyTypeDescription = dependencyTypeDescription;
    }

    public AssetDependency.DependencyStrength getDependencyStrength() {
        return dependencyStrength;
    }

    public void setDependencyStrength(AssetDependency.DependencyStrength dependencyStrength) {
        this.dependencyStrength = dependencyStrength;
    }

    public String getDependencyStrengthDescription() {
        return dependencyStrengthDescription;
    }

    public void setDependencyStrengthDescription(String dependencyStrengthDescription) {
        this.dependencyStrengthDescription = dependencyStrengthDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getIsCritical() {
        return isCritical;
    }

    public void setIsCritical(Boolean isCritical) {
        this.isCritical = isCritical;
    }

    public AssetDependency.DependencyStatus getStatus() {
        return status;
    }

    public void setStatus(AssetDependency.DependencyStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}

/**
 * 资产依赖关系查询请求DTO
 */
class AssetDependencyQueryRequest {
    
    /**
     * 资产ID（查找与该资产相关的所有依赖关系）
     */
    private Long assetId;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 依赖类型
     */
    private AssetDependency.DependencyType dependencyType;
    
    /**
     * 依赖强度
     */
    private AssetDependency.DependencyStrength dependencyStrength;
    
    /**
     * 依赖状态
     */
    private AssetDependency.DependencyStatus status;
    
    /**
     * 是否为关键依赖
     */
    private Boolean isCritical;
    
    /**
     * 端口
     */
    private Integer port;
    
    /**
     * 协议
     */
    private String protocol;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer page = 0;
    
    /**
     * 页大小
     */
    private Integer size = 20;
    
    /**
     * 排序字段
     */
    private String sortBy = "createdTime";
    
    /**
     * 排序方向
     */
    private String sortDir = "desc";

    // Getter and Setter methods
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public AssetDependency.DependencyType getDependencyType() { return dependencyType; }
    public void setDependencyType(AssetDependency.DependencyType dependencyType) { this.dependencyType = dependencyType; }
    public AssetDependency.DependencyStrength getDependencyStrength() { return dependencyStrength; }
    public void setDependencyStrength(AssetDependency.DependencyStrength dependencyStrength) { this.dependencyStrength = dependencyStrength; }
    public AssetDependency.DependencyStatus getStatus() { return status; }
    public void setStatus(AssetDependency.DependencyStatus status) { this.status = status; }
    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
}

/**
 * 资产依赖拓扑图节点DTO
 */
class AssetDependencyNode {
    
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
    
    public static AssetDependencyNode fromAsset(com.vulnark.entity.Asset asset) {
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

/**
 * 资产依赖拓扑图边DTO
 */
class AssetDependencyEdge {
    
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

/**
 * 资产依赖拓扑图DTO
 */
class AssetDependencyTopology {

    private java.util.List<AssetDependencyNode> nodes;
    private java.util.List<AssetDependencyEdge> edges;
    private Long projectId;
    private String projectName;
    private Integer totalNodes;
    private Integer totalEdges;

    // Getter and Setter methods
    public java.util.List<AssetDependencyNode> getNodes() { return nodes; }
    public void setNodes(java.util.List<AssetDependencyNode> nodes) { this.nodes = nodes; }
    public java.util.List<AssetDependencyEdge> getEdges() { return edges; }
    public void setEdges(java.util.List<AssetDependencyEdge> edges) { this.edges = edges; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Integer getTotalNodes() { return totalNodes; }
    public void setTotalNodes(Integer totalNodes) { this.totalNodes = totalNodes; }
    public Integer getTotalEdges() { return totalEdges; }
    public void setTotalEdges(Integer totalEdges) { this.totalEdges = totalEdges; }
}
