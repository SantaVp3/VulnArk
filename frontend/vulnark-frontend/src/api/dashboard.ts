import api from './index'

// 仪表盘统计数据接口
export interface DashboardStats {
  vulnerabilities: VulnerabilityDashboardStats
  projects: ProjectDashboardStats
  assets: AssetDashboardStats
  users: UserDashboardStats
}

export interface VulnerabilityDashboardStats {
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

export interface ProjectDashboardStats {
  total: number
  active: number
  completed: number
  archived: number
  overdue: number
}

export interface AssetDashboardStats {
  total: number
  online: number
  offline: number
  maintenance: number
  high: number
  critical: number
}

export interface UserDashboardStats {
  total: number
  active: number
  inactive: number
  admin: number
  analyst: number
  viewer: number
}

export interface VulnerabilityTrendData {
  date: string
  discovered: number
  resolved: number
}

export interface ProjectVulnerabilityRankData {
  projectId: number
  projectName: string
  vulnerabilityCount: number
  criticalCount: number
  highCount: number
}

export interface AssetStatusDistribution {
  status: string
  count: number
  percentage: number
}

export interface SeverityDistribution {
  severity: string
  count: number
  percentage: number
}

// 获取仪表盘统计数据
export const getDashboardStats = () => {
  return api.get<any, DashboardStats>('/dashboard/stats')
}

// 获取漏洞趋势数据（最近30天）
export const getVulnerabilityTrends = (days: number = 30) => {
  return api.get<any, VulnerabilityTrendData[]>('/dashboard/vulnerability-trends', { 
    params: { days } 
  })
}

// 获取漏洞严重程度分布
export const getVulnerabilitySeverityDistribution = () => {
  return api.get<any, SeverityDistribution[]>('/dashboard/vulnerability-severity-distribution')
}

// 获取项目漏洞排行榜
export const getProjectVulnerabilityRanks = (limit: number = 10) => {
  return api.get<any, ProjectVulnerabilityRankData[]>('/dashboard/project-vulnerability-ranks', {
    params: { limit }
  })
}

// 获取资产状态分布
export const getAssetStatusDistribution = () => {
  return api.get<any, AssetStatusDistribution[]>('/dashboard/asset-status-distribution')
}

// 获取最近活动
export interface RecentActivity {
  id: number
  type: 'vulnerability' | 'project' | 'asset' | 'scan'
  title: string
  description: string
  timestamp: string
  severity?: string
  status?: string
}

export const getRecentActivities = (limit: number = 10) => {
  return api.get<any, RecentActivity[]>('/dashboard/recent-activities', {
    params: { limit }
  })
}

// 获取系统健康状态
export interface SystemHealth {
  cpu: number
  memory: number
  disk: number
  network: number
  database: 'healthy' | 'warning' | 'error'
  services: ServiceStatus[]
}

export interface ServiceStatus {
  name: string
  status: 'running' | 'stopped' | 'error'
  uptime: number
}

export const getSystemHealth = () => {
  return api.get<any, SystemHealth>('/dashboard/system-health')
}

// 刷新仪表盘数据
export const refreshDashboardData = () => {
  return api.post('/dashboard/refresh')
}
