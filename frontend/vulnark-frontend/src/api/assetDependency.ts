import api from './index'

// 资产依赖关系类型定义
export interface AssetDependency {
  id: number
  sourceAssetId: number
  sourceAssetName?: string
  targetAssetId: number
  targetAssetName?: string
  dependencyType: DependencyType
  dependencyTypeDescription?: string
  dependencyStrength: DependencyStrength
  dependencyStrengthDescription?: string
  description?: string
  port?: number
  protocol?: string
  serviceName?: string
  isCritical: boolean
  status: DependencyStatus
  statusDescription?: string
  createdTime: string
  updatedTime: string
  createdBy?: number
  createdByName?: string
}

export type DependencyType = 
  | 'NETWORK'
  | 'DATABASE' 
  | 'SERVICE'
  | 'APPLICATION'
  | 'INFRASTRUCTURE'
  | 'DATA_FLOW'
  | 'AUTHENTICATION'
  | 'STORAGE'
  | 'MONITORING'
  | 'BACKUP'
  | 'OTHER'

export type DependencyStrength = 
  | 'WEAK'
  | 'MEDIUM'
  | 'STRONG'
  | 'CRITICAL'

export type DependencyStatus = 
  | 'ACTIVE'
  | 'INACTIVE'
  | 'BROKEN'
  | 'DEPRECATED'

export interface AssetDependencyRequest {
  sourceAssetId: number
  targetAssetId: number
  dependencyType: DependencyType
  dependencyStrength: DependencyStrength
  description?: string
  port?: number
  protocol?: string
  serviceName?: string
  isCritical?: boolean
  status?: DependencyStatus
}

export interface AssetDependencyQueryParams {
  assetId?: number
  projectId?: number
  dependencyType?: DependencyType
  dependencyStrength?: DependencyStrength
  status?: DependencyStatus
  isCritical?: boolean
  port?: number
  protocol?: string
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

// 拓扑图相关类型
export interface AssetDependencyNode {
  id: number
  name: string
  type: string
  status: string
  importance: string
  x?: number
  y?: number
  color?: string
  shape?: string
  size?: number
}

export interface AssetDependencyEdge {
  id: number
  source: number
  target: number
  type: string
  strength: string
  label?: string
  color?: string
  width?: number
  style?: string
}

export interface AssetDependencyTopology {
  nodes: AssetDependencyNode[]
  edges: AssetDependencyEdge[]
  projectId: number
  projectName?: string
  totalNodes: number
  totalEdges: number
}

// 依赖路径分析
export interface DependencyPathAnalysis {
  sourceAssetId: number
  targetAssetId: number
  paths: number[][]
  hasPath: boolean
  shortestPathLength?: number
  totalPaths?: number
}

// 依赖统计
export interface DependencyStatistics {
  totalDependencies: number
  criticalDependencies: number
  brokenDependencies: number
  dependencyTypeStats: Record<DependencyType, number>
  dependencyStrengthStats: Record<DependencyStrength, number>
}

// API 方法

// 创建资产依赖关系
export const createAssetDependency = (data: AssetDependencyRequest) => {
  return api.post<any, AssetDependency>('/asset-dependencies', data)
}

// 更新资产依赖关系
export const updateAssetDependency = (id: number, data: AssetDependencyRequest) => {
  return api.put<any, AssetDependency>(`/asset-dependencies/${id}`, data)
}

// 删除资产依赖关系
export const deleteAssetDependency = (id: number) => {
  return api.delete<any, string>(`/asset-dependencies/${id}`)
}

// 批量删除资产依赖关系
export const batchDeleteAssetDependencies = (ids: number[]) => {
  return api.delete<any, string>('/asset-dependencies/batch', { data: ids })
}

// 获取资产依赖关系详情
export const getAssetDependencyById = (id: number) => {
  return api.get<any, AssetDependency>(`/asset-dependencies/${id}`)
}

// 获取资产的所有依赖关系
export const getAssetDependencies = (assetId: number) => {
  return api.get<any, AssetDependency[]>(`/asset-dependencies/asset/${assetId}`)
}

// 获取资产的直接依赖
export const getDirectDependencies = (assetId: number) => {
  return api.get<any, AssetDependency[]>(`/asset-dependencies/asset/${assetId}/direct`)
}

// 获取资产的反向依赖
export const getReverseDependencies = (assetId: number) => {
  return api.get<any, AssetDependency[]>(`/asset-dependencies/asset/${assetId}/reverse`)
}

// 获取项目依赖拓扑图
export const getProjectDependencyTopology = (projectId: number) => {
  return api.get<any, AssetDependencyTopology>(`/asset-dependencies/topology/project/${projectId}`)
}

// 分析依赖路径
export const analyzeDependencyPath = (sourceAssetId: number, targetAssetId: number) => {
  return api.get<any, DependencyPathAnalysis>('/asset-dependencies/path/analyze', {
    params: { sourceAssetId, targetAssetId }
  })
}

// 检测循环依赖
export const detectCircularDependencies = () => {
  return api.get<any, AssetDependency[]>('/asset-dependencies/circular')
}

// 获取依赖统计信息
export const getDependencyStatistics = (projectId?: number) => {
  return api.get<any, DependencyStatistics>('/asset-dependencies/statistics', {
    params: projectId ? { projectId } : {}
  })
}

// 依赖类型选项
export const dependencyTypeOptions = [
  { value: 'NETWORK', label: '网络依赖' },
  { value: 'DATABASE', label: '数据库依赖' },
  { value: 'SERVICE', label: '服务依赖' },
  { value: 'APPLICATION', label: '应用依赖' },
  { value: 'INFRASTRUCTURE', label: '基础设施依赖' },
  { value: 'DATA_FLOW', label: '数据流依赖' },
  { value: 'AUTHENTICATION', label: '认证依赖' },
  { value: 'STORAGE', label: '存储依赖' },
  { value: 'MONITORING', label: '监控依赖' },
  { value: 'BACKUP', label: '备份依赖' },
  { value: 'OTHER', label: '其他依赖' }
]

// 依赖强度选项
export const dependencyStrengthOptions = [
  { value: 'WEAK', label: '弱依赖' },
  { value: 'MEDIUM', label: '中等依赖' },
  { value: 'STRONG', label: '强依赖' },
  { value: 'CRITICAL', label: '关键依赖' }
]

// 依赖状态选项
export const dependencyStatusOptions = [
  { value: 'ACTIVE', label: '活跃' },
  { value: 'INACTIVE', label: '非活跃' },
  { value: 'BROKEN', label: '已断开' },
  { value: 'DEPRECATED', label: '已废弃' }
]

// 工具函数

// 获取依赖类型标签颜色
export const getDependencyTypeColor = (type: DependencyType): string => {
  const colorMap: Record<DependencyType, string> = {
    NETWORK: 'blue',
    DATABASE: 'green',
    SERVICE: 'orange',
    APPLICATION: 'purple',
    INFRASTRUCTURE: 'red',
    DATA_FLOW: 'cyan',
    AUTHENTICATION: 'gold',
    STORAGE: 'lime',
    MONITORING: 'magenta',
    BACKUP: 'volcano',
    OTHER: 'gray'
  }
  return colorMap[type] || 'gray'
}

// 获取依赖强度标签颜色
export const getDependencyStrengthColor = (strength: DependencyStrength): string => {
  const colorMap: Record<DependencyStrength, string> = {
    WEAK: 'gray',
    MEDIUM: 'blue',
    STRONG: 'orange',
    CRITICAL: 'red'
  }
  return colorMap[strength] || 'gray'
}

// 获取依赖状态标签颜色
export const getDependencyStatusColor = (status: DependencyStatus): string => {
  const colorMap: Record<DependencyStatus, string> = {
    ACTIVE: 'green',
    INACTIVE: 'gray',
    BROKEN: 'red',
    DEPRECATED: 'orange'
  }
  return colorMap[status] || 'gray'
}
