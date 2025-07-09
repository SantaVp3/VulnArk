import request from './request'

export interface BaselineScan {
  id: number
  scanName: string
  description?: string
  assetId: number
  assetName?: string
  assetIpAddress?: string
  scanType: string
  status: string
  startTime?: string
  endTime?: string
  totalChecks: number
  passedChecks: number
  failedChecks: number
  warningChecks: number
  complianceScore: number
  errorMessage?: string
  createdTime: string
  updatedTime: string
}

export interface BaselineScanRequest {
  scanName: string
  description?: string
  assetId: number
  scanType: string
  executeImmediately?: boolean
  timeoutMinutes?: number
}

export interface BaselineScanResult {
  id: number
  checkId: string
  checkName: string
  checkDescription?: string
  category: string
  severity: string
  status: string
  expectedValue?: string
  actualValue?: string
  checkCommand?: string
  remediation?: string
  reference?: string
  executionTime?: number
  errorMessage?: string
}

export interface ScanStatistics {
  totalScans: number
  completedScans: number
  runningScans: number
  failedScans: number
  averageComplianceScore: number
  statusDistribution: Record<string, number>
  typeDistribution: Record<string, number>
  recentScans: BaselineScan[]
}

// 基线扫描API
export const baselineScanApi = {
  // 获取扫描列表
  getScans: (params: {
    scanName?: string
    status?: string
    scanType?: string
    page?: number
    size?: number
  }) => {
    return request.get('/baseline-scans', { params })
  },

  // 获取扫描详情
  getScanById: (scanId: number) => {
    return request.get(`/baseline-scans/${scanId}`)
  },

  // 创建扫描任务
  createScan: (data: BaselineScanRequest) => {
    return request.post('/baseline-scans', data)
  },

  // 执行扫描
  executeScan: (scanId: number) => {
    return request.post(`/baseline-scans/${scanId}/execute`)
  },

  // 取消扫描
  cancelScan: (scanId: number) => {
    return request.post(`/baseline-scans/${scanId}/cancel`)
  },

  // 重新执行扫描
  rerunScan: (scanId: number) => {
    return request.post(`/baseline-scans/${scanId}/rerun`)
  },

  // 删除扫描
  deleteScan: (scanId: number) => {
    return request.delete(`/baseline-scans/${scanId}`)
  },

  // 获取扫描结果
  getScanResults: (scanId: number) => {
    return request.get(`/baseline-scans/${scanId}/results`)
  },

  // 获取失败的检查项
  getFailedChecks: (scanId: number) => {
    return request.get(`/baseline-scans/${scanId}/failed-checks`)
  },

  // 获取高危失败的检查项
  getHighRiskFailedChecks: (scanId: number) => {
    return request.get(`/baseline-scans/${scanId}/high-risk-failed-checks`)
  },

  // 获取统计信息
  getStatistics: () => {
    return request.get('/baseline-scans/statistics')
  },

  // 获取资产扫描历史
  getAssetScanHistory: (assetId: number) => {
    return request.get(`/baseline-scans/asset/${assetId}/history`)
  }
}

export default baselineScanApi
