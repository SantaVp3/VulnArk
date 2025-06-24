import api from './index'

// 资产相关类型定义
export interface Asset {
  id: number
  name: string
  description?: string
  type: 'SERVER' | 'WORKSTATION' | 'NETWORK_DEVICE' | 'DATABASE' | 'WEB_APPLICATION' | 'MOBILE_APPLICATION' | 'IOT_DEVICE' | 'CLOUD_SERVICE' | 'OTHER'
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'DECOMMISSIONED'
  ipAddress?: string
  domain?: string
  port?: number
  protocol?: string
  service?: string
  version?: string
  operatingSystem?: string
  importance: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  projectId: number
  ownerId?: number
  location?: string
  vendor?: string
  tags?: string
  lastScanTime?: string
  vulnerabilityCount: number
  riskScore: number
  notes?: string
  createdTime: string
  updatedTime: string
  deleted: boolean
}

export interface AssetRequest {
  name: string
  description?: string
  type: 'SERVER' | 'WORKSTATION' | 'NETWORK_DEVICE' | 'DATABASE' | 'WEB_APPLICATION' | 'MOBILE_APPLICATION' | 'IOT_DEVICE' | 'CLOUD_SERVICE' | 'OTHER'
  status?: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'DECOMMISSIONED'
  ipAddress?: string
  domain?: string
  port?: number
  protocol?: string
  service?: string
  version?: string
  operatingSystem?: string
  importance?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  projectId: number
  ownerId?: number
  location?: string
  vendor?: string
  tags?: string
  lastScanTime?: string
  riskScore?: number
  notes?: string
}

export interface AssetQueryParams {
  name?: string
  type?: string
  status?: string
  importance?: string
  projectId?: number
  ownerId?: number
  ipAddress?: string
  domain?: string
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export interface AssetStats {
  total: number
  active: number
  inactive: number
  maintenance: number
  high: number
  critical: number
}

// 获取资产列表
export const getAssets = (params: AssetQueryParams) => {
  return api.get<any, { content: Asset[], totalElements: number, totalPages: number }>('/assets', { params })
}

// 获取所有资产
export const getAllAssets = () => {
  return api.get<any, Asset[]>('/assets/all')
}

// 根据ID获取资产详情
export const getAssetById = (id: number) => {
  return api.get<any, Asset>(`/assets/${id}`)
}

// 创建资产
export const createAsset = (data: AssetRequest) => {
  return api.post<any, Asset>('/assets', data)
}

// 更新资产
export const updateAsset = (id: number, data: AssetRequest) => {
  return api.put<any, Asset>(`/assets/${id}`, data)
}

// 删除资产
export const deleteAsset = (id: number) => {
  return api.delete<any, string>(`/assets/${id}`)
}

// 根据项目ID获取资产
export const getAssetsByProjectId = (projectId: number) => {
  return api.get<any, Asset[]>(`/assets/project/${projectId}`)
}

// 根据负责人ID获取资产
export const getAssetsByOwnerId = (ownerId: number) => {
  return api.get<any, Asset[]>(`/assets/owner/${ownerId}`)
}

// 获取最近的资产
export const getRecentAssets = (limit: number = 10) => {
  return api.get<any, Asset[]>('/assets/recent', { params: { limit } })
}

// 获取高风险资产
export const getHighRiskAssets = (minRiskScore?: number) => {
  return api.get<any, Asset[]>('/assets/high-risk', { params: { minRiskScore } })
}

// 获取需要扫描的资产
export const getAssetsNeedingScan = (daysBefore: number = 30) => {
  return api.get<any, Asset[]>('/assets/need-scan', { params: { daysBefore } })
}

// 获取活跃资产
export const getActiveAssets = () => {
  return api.get<any, Asset[]>('/assets/active')
}

// 更新资产状态
export const updateAssetStatus = (id: number, status: string) => {
  return api.put<any, Asset>(`/assets/${id}/status`, { status })
}

// 更新资产风险评分
export const updateAssetRiskScore = (id: number, riskScore: number) => {
  return api.put<any, Asset>(`/assets/${id}/risk-score`, { riskScore })
}

// 更新资产扫描时间
export const updateAssetScanTime = (id: number) => {
  return api.put<any, Asset>(`/assets/${id}/scan-time`)
}

// 更新资产统计信息
export const updateAssetStatistics = (id: number) => {
  return api.put<any, Asset>(`/assets/${id}/statistics`)
}

// 获取资产统计信息
export const getAssetStats = () => {
  return api.get<any, AssetStats>('/assets/stats')
}

// 批量导入资产
export const importAssets = (data: AssetRequest[]) => {
  return api.post<any, Asset[]>('/assets/import', data)
}

// 批量导出资产
export const exportAssets = (assetIds?: number[]) => {
  return api.post<any, Asset[]>('/assets/export', assetIds || [])
}
