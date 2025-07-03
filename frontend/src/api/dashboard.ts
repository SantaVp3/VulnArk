import request from './request'

export interface DashboardStats {
  vulnerabilities: {
    total: number
    critical: number
    high: number
    medium: number
    low: number
    info: number
    open: number
    inProgress: number
    resolved: number
    closed: number
    reopened: number
  }
  assets: {
    total: number
    online: number
    offline: number
    maintenance: number
    high: number
    critical: number
  }
  users: {
    total: number
    active: number
    inactive: number
    admin: number
    analyst: number
    viewer: number
  }
  projects: {
    total: number
    active: number
    completed: number
    archived: number
    overdue: number
  }
}

export interface VulnerabilityTrendData {
  date: string
  discovered: number
  resolved: number
}

export interface SeverityDistribution {
  severity: string
  count: number
  percentage: number
}

export interface AssetStatusDistribution {
  status: string
  count: number
  percentage: number
}

// 获取仪表板统计数据
export const getDashboardStats = async (): Promise<DashboardStats> => {
  return request.get('/dashboard/stats') as Promise<DashboardStats>
}

// 获取漏洞趋势数据
export const getVulnerabilityTrend = async (days: number = 30): Promise<VulnerabilityTrendData[]> => {
  return request.get(`/dashboard/vulnerability-trends?days=${days}`) as Promise<VulnerabilityTrendData[]>
}

// 获取严重程度分布
export const getSeverityDistribution = async (): Promise<SeverityDistribution[]> => {
  return request.get('/dashboard/vulnerability-severity-distribution') as Promise<SeverityDistribution[]>
}

// 获取资产状态分布
export const getAssetStatusDistribution = async (): Promise<AssetStatusDistribution[]> => {
  return request.get('/dashboard/asset-status-distribution') as Promise<AssetStatusDistribution[]>
}

// 获取最近活动
export const getRecentActivities = async (limit: number = 10) => {
  return request.get(`/dashboard/recent-activities?limit=${limit}`)
}

// 获取资产状态分布
export const getAssetDistribution = async () => {
  return request.get('/dashboard/asset-distribution')
} 