import request from './request'

export interface DashboardStats {
  vulnerabilities: {
    total: number
    open: number
    closed: number
    change: number
  }
  assets: {
    total: number
    online: number
    offline: number
    change: number
  }
  scans: {
    total: number
    completed: number
    pending: number
    change: number
  }
  users: {
    total: number
    active: number
    inactive: number
    change: number
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
}

export interface AssetStatusDistribution {
  status: string
  count: number
}

export interface RecentActivity {
  id: string
  type: string
  title: string
  description: string
  createdTime: string
  icon?: any
}

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取漏洞趋势数据
 */
export function getVulnerabilityTrend(days: number) {
  return request({
    url: '/dashboard/vulnerability-trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取漏洞严重程度分布
 */
export function getSeverityDistribution() {
  return request({
    url: '/dashboard/severity-distribution',
    method: 'get'
  })
}

/**
 * 获取资产状态分布
 */
export function getAssetStatusDistribution() {
  return request({
    url: '/dashboard/asset-status-distribution',
    method: 'get'
  })
}

/**
 * 获取最近活动
 */
export function getRecentActivities() {
  return request({
    url: '/dashboard/recent-activities',
    method: 'get'
  })
}