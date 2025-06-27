import api from './index'

// 简化的类型定义
export interface AssetDiscoveryTask {
  id: number
  name: string
  description?: string
  targetType: string
  targets: string
  scanType: string
  status: string
  progress: number
  createdTime: string
  updatedTime: string
}

export interface AssetDiscoveryTaskRequest {
  name: string
  description?: string
  targetType: string
  targets: string
  scanType: string
  scanPorts?: string
  scheduleType: string
}

export interface SystemStatistics {
  pendingTasks: number
  runningTasks: number
  completedTasks: number
  failedTasks: number
  cancelledTasks: number
  recentTasks: number
  newDiscoveredAssets: number
}

export interface EnumOption {
  value: string
  label: string
}

// 简化的API方法
export const getSystemStatistics = () => {
  return api.get<any, SystemStatistics>('/asset-discovery/statistics')
}

export const getDiscoveryTasks = (page: number = 0, size: number = 10) => {
  return api.get<any, { content: AssetDiscoveryTask[], totalElements: number, totalPages: number }>('/asset-discovery/tasks', {
    params: { page, size }
  })
}

export const createDiscoveryTask = (data: AssetDiscoveryTaskRequest) => {
  return api.post<any, AssetDiscoveryTask>('/asset-discovery/tasks', data)
}

// 简化的工具函数
export const commonPortConfigs = [
  { label: '常用端口', value: '21,22,23,25,53,80,110,143,443' },
  { label: 'Web端口', value: '80,443,8080,8443' },
  { label: '数据库端口', value: '1433,3306,5432' }
]
