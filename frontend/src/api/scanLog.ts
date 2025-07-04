import request from './request'

// 扫描日志类型定义
export interface ScanLogMessage {
  id?: number
  taskId: number
  timestamp: string
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG'
  message: string
  scanEngine?: string
  currentTarget?: string
  progress?: number
  stackTrace?: string
}

export interface ScanLogQueryParams {
  level?: string
  scanEngine?: string
  startTime?: string
  endTime?: string
  keyword?: string
  page?: number
  size?: number
}

// 扫描日志API
export const scanLogApi = {
  // 获取扫描日志
  getScanLogs: (taskId: number, params: ScanLogQueryParams = {}) => {
    return request.get<{ success: boolean, data: ScanLogMessage[] }>(`/scan-logs/${taskId}`, { params })
  },

  // 清空扫描日志
  clearScanLogs: (taskId: number) => {
    return request.delete(`/scan-logs/${taskId}/clear`)
  },

  // 导出扫描日志
  exportScanLogs: (taskId: number, params: ScanLogQueryParams = {}) => {
    return request.get(`/scan-logs/${taskId}/export`, { params, responseType: 'blob' })
  }
} 