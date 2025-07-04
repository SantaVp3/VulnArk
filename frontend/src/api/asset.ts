import request from './request'

// 资产类型定义
export interface Asset {
  id?: number
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

  ownerId?: number
  location?: string
  vendor?: string
  tags?: string
  notes?: string
  vulnerabilityCount?: number
  createdTime?: string
  updatedTime?: string
}

export interface AssetQueryParams {
  name?: string
  type?: string
  status?: string
  importance?: string

  ownerId?: number
  ipAddress?: string
  domain?: string
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface AssetStats {
  totalAssets: number
  assetsByType: Record<string, number>
  assetsByStatus: Record<string, number>
  assetsByImportance: Record<string, number>
}

// 资产API
export const assetApi = {
  // 获取资产列表
  getAssets: (params: AssetQueryParams) => {
    return request.get<PageResult<Asset>>('/assets', { params })
  },

  // 获取资产详情
  getAsset: (id: number) => {
    return request.get<Asset>(`/assets/${id}`)
  },

  // 创建资产
  createAsset: (asset: Omit<Asset, 'id'>) => {
    return request.post<Asset>('/assets', asset)
  },

  // 更新资产
  updateAsset: (id: number, asset: Partial<Asset>) => {
    return request.put<Asset>(`/assets/${id}`, asset)
  },

  // 删除资产
  deleteAsset: (id: number) => {
    return request.delete(`/assets/${id}`)
  },

  // 批量删除资产
  batchDeleteAssets: (ids: number[]) => {
    return request.post('/assets/batch-delete', { ids })
  },

  // 更新资产状态
  updateAssetStatus: (id: number, status: string) => {
    return request.put<Asset>(`/assets/${id}/status`, { status })
  },

  // 导入资产
  importAssets: (data: any) => {
    return request.post<Asset[]>('/assets/import', data)
  },

  // 导出资产
  exportAssets: (ids?: number[]) => {
    const params = ids ? { ids } : undefined
    return request.get<Asset[]>('/assets/export', { params })
  },

  // 获取所有资产
  getAllAssets: () => {
    return request.get<Asset[]>('/assets/all')
  },

  // 获取负责人资产
  getOwnerAssets: (ownerId: number) => {
    return request.get<Asset[]>(`/assets/owner/${ownerId}`)
  },

  // 获取最近资产
  getRecentAssets: (limit: number = 10) => {
    return request.get<Asset[]>('/assets/recent', { params: { limit } })
  },

  // 获取资产统计
  getAssetStats: () => {
    return request.get<AssetStats>('/assets/stats')
  },

  // 搜索资产
  searchAssets: (keyword: string) => {
    return request.get<Asset[]>('/assets/search', { params: { keyword } })
  },


} 