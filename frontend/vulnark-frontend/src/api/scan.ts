import { request } from './index'

export interface ScanTask {
  id: number
  name: string
  description?: string
  type: 'PORT_SCAN' | 'VULNERABILITY_SCAN' | 'SERVICE_SCAN' | 'WEB_SCAN' | 'NETWORK_SCAN'
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
  targetAssetId?: number
  targetIp?: string
  targetDomain?: string
  targetPorts?: string
  scanConfig?: string
  scanEngine?: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  createdBy: number
  projectId?: number
  scheduledTime?: string
  startTime?: string
  endTime?: string
  scanResult?: string
  vulnerabilityCount?: number
  portCount?: number
  serviceCount?: number
  errorMessage?: string
  progress: number
  createdTime: string
  updatedTime: string
}

export interface CreateScanTaskRequest {
  name: string
  type: string
  targetIp: string
  targetPorts?: string
  projectId?: number
}

export interface ScanStats {
  total: number
  pending: number
  running: number
  completed: number
  failed: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 扫描管理API
export const scanApi = {
  // 获取扫描任务列表
  getTasks: (params?: any) => {
    return request.get('/scan/tasks', { params })
  },

  // 获取扫描任务详情
  getTaskById: (id: number) => {
    return request.get(`/scan/tasks/${id}`)
  },

  // 创建扫描任务
  createTask: (data: CreateScanTaskRequest) => {
    return request.post('/scan/tasks', data)
  },

  // 为资产创建扫描任务
  createAssetTask: (assetId: number, data: { type: string }) => {
    return request.post(`/scan/tasks/asset/${assetId}`, data)
  },

  // 执行扫描任务
  executeTask: (id: number) => {
    return request.post(`/scan/tasks/${id}/execute`)
  },

  // 取消扫描任务
  cancelTask: (id: number) => {
    return request.post(`/scan/tasks/${id}/cancel`)
  },

  // 删除扫描任务
  deleteTask: (id: number) => {
    return request.delete(`/scan/tasks/${id}`)
  },

  // 获取待执行的任务
  getPendingTasks: () => {
    return request.get('/scan/tasks/pending')
  },

  // 获取正在运行的任务
  getRunningTasks: () => {
    return request.get('/scan/tasks/running')
  },

  // 获取扫描统计
  getStats: () => {
    return request.get('/scan/stats')
  }
}

export default scanApi
