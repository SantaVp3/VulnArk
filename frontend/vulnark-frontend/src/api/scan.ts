import api from './index'

export interface ScanTask {
  id: number
  name: string
  description?: string
  scanType: ScanType
  scanTypeDescription?: string
  scanEngine: ScanEngine
  scanEngineDescription?: string
  scanTemplate?: ScanTemplate
  scanTemplateDescription?: string
  status: TaskStatus
  statusDescription?: string
  progress: number
  projectId?: number
  projectName?: string
  targetAssets?: string
  scanParameters?: string
  scanOptions?: string
  totalVulnerabilityCount?: number
  highRiskCount?: number
  mediumRiskCount?: number
  lowRiskCount?: number
  infoRiskCount?: number
  scheduledStartTime?: string
  actualStartTime?: string
  completedTime?: string
  estimatedDuration?: number
  actualDuration?: number
  externalTaskId?: string
  errorMessage?: string
  createdById?: number
  createdByName?: string
  createdTime: string
  updatedTime: string
}

// 扫描任务请求类型
export interface ScanTaskRequest {
  name: string
  description?: string
  scanType: ScanType
  scanEngine: ScanEngine
  scanTemplate?: ScanTemplate
  projectId?: number
  targetAssets?: string
  scanParameters?: string
  scanOptions?: string
  scheduledStartTime?: string
  estimatedDuration?: number
  scanConfigId?: number
}

// 扫描结果类型
export interface ScanResult {
  id: number
  scanTaskId: number
  targetHost: string
  targetPort?: number
  vulnerabilityName: string
  vulnerabilityDescription?: string
  severity: Severity
  vulnerabilityType?: string
  cveId?: string
  cvssScore?: number
  cvssVector?: string
  status: VulnerabilityStatus
  solution?: string
  references?: string
  proof?: string
  request?: string
  response?: string
  pluginId?: string
  pluginName?: string
  pluginFamily?: string
  externalResultId?: string
  rawData?: string
  riskLevel?: RiskLevel
  falsePositive: boolean
  confirmationStatus: ConfirmationStatus
  assetId?: number
  vulnerabilityId?: number
  discoveredTime: string
  createdTime: string
  updatedTime: string
}

// 枚举类型
export type ScanType = 'PORT_SCAN' | 'WEB_SCAN' | 'SYSTEM_SCAN' | 'COMPREHENSIVE_SCAN' | 'CUSTOM_SCAN'
export type ScanEngine = 'NESSUS' | 'OPENVAS' | 'AWVS' | 'NUCLEI' | 'NMAP' | 'INTERNAL'
export type ScanTemplate = 'QUICK_SCAN' | 'FULL_SCAN' | 'WEB_APP_SCAN' | 'NETWORK_SCAN' | 'COMPLIANCE_SCAN' | 'CUSTOM'
export type TaskStatus = 'CREATED' | 'QUEUED' | 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'TIMEOUT'
export type Severity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
export type VulnerabilityStatus = 'OPEN' | 'FIXED' | 'ACCEPTED' | 'IGNORED' | 'RETEST'
export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'NONE'
export type ConfirmationStatus = 'UNCONFIRMED' | 'CONFIRMED' | 'FALSE_POSITIVE' | 'DUPLICATE'

// 分页响应类型
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// API 方法

/**
 * 获取扫描任务列表
 */
export const getScanTasks = (page: number = 0, size: number = 10, sortBy: string = 'createdTime', sortDir: string = 'desc') => {
  return api.get<any, PageResponse<ScanTask>>('/scan/tasks', {
    params: { page, size, sortBy, sortDir }
  })
}

/**
 * 搜索扫描任务
 */
export const searchScanTasks = (params: {
  name?: string
  status?: TaskStatus
  scanType?: ScanType
  scanEngine?: ScanEngine
  projectId?: number
  createdById?: number
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}) => {
  return api.get<any, PageResponse<ScanTask>>('/scan/tasks/search', { params })
}

/**
 * 获取扫描任务详情
 */
export const getScanTask = (id: number) => {
  return api.get<any, ScanTask>(`/scan/tasks/${id}`)
}

/**
 * 创建扫描任务
 */
export const createScanTask = (data: ScanTaskRequest) => {
  return api.post<any, ScanTask>('/scan/tasks', data)
}

/**
 * 更新扫描任务
 */
export const updateScanTask = (id: number, data: ScanTaskRequest) => {
  return api.put<any, ScanTask>(`/scan/tasks/${id}`, data)
}

/**
 * 删除扫描任务
 */
export const deleteScanTask = (id: number) => {
  return api.delete<any, string>(`/scan/tasks/${id}`)
}

/**
 * 启动扫描任务
 */
export const startScanTask = (id: number) => {
  return api.post<any, string>(`/scan/tasks/${id}/start`)
}

/**
 * 停止扫描任务
 */
export const stopScanTask = (id: number) => {
  return api.post<any, string>(`/scan/tasks/${id}/stop`)
}

/**
 * 获取扫描结果
 */
export const getScanResults = (taskId: number, page: number = 0, size: number = 10, sortBy: string = 'discoveredTime', sortDir: string = 'desc') => {
  return api.get<any, PageResponse<ScanResult>>(`/scan/tasks/${taskId}/results`, {
    params: { page, size, sortBy, sortDir }
  })
}

/**
 * 获取扫描引擎列表
 */
export const getScanEngines = () => {
  return api.get<any, ScanEngine[]>('/scan/engines')
}

/**
 * 获取扫描类型列表
 */
export const getScanTypes = () => {
  return api.get<any, ScanType[]>('/scan/types')
}

/**
 * 获取扫描模板列表
 */
export const getScanTemplates = () => {
  return api.get<any, ScanTemplate[]>('/scan/templates')
}

/**
 * 获取任务状态列表
 */
export const getTaskStatuses = () => {
  return api.get<any, TaskStatus[]>('/scan/statuses')
}

// 工具函数

/**
 * 获取任务状态颜色
 */
export const getTaskStatusColor = (status: TaskStatus): string => {
  const colorMap: Record<TaskStatus, string> = {
    CREATED: 'gray',
    QUEUED: 'purple',
    RUNNING: 'blue',
    PAUSED: 'orange',
    COMPLETED: 'green',
    FAILED: 'red',
    CANCELLED: 'orange',
    TIMEOUT: 'red'
  }
  return colorMap[status] || 'gray'
}

/**
 * 获取扫描类型颜色
 */
export const getScanTypeColor = (type: ScanType): string => {
  const colorMap: Record<ScanType, string> = {
    PORT_SCAN: 'blue',
    WEB_SCAN: 'purple',
    SYSTEM_SCAN: 'orange',
    COMPREHENSIVE_SCAN: 'red',
    CUSTOM_SCAN: 'gray'
  }
  return colorMap[type] || 'gray'
}

/**
 * 获取严重程度颜色
 */
export const getSeverityColor = (severity: Severity): string => {
  const colorMap: Record<Severity, string> = {
    CRITICAL: 'red',
    HIGH: 'orange',
    MEDIUM: 'gold',
    LOW: 'blue',
    INFO: 'gray'
  }
  return colorMap[severity] || 'gray'
}

/**
 * 获取扫描引擎描述
 */
export const getScanEngineDescription = (engine: ScanEngine): string => {
  const descMap: Record<ScanEngine, string> = {
    NESSUS: 'Nessus',
    OPENVAS: 'OpenVAS',
    AWVS: 'AWVS',
    NUCLEI: 'Nuclei',
    NMAP: 'Nmap',
    INTERNAL: '内置引擎'
  }
  return descMap[engine] || engine
}

/**
 * 获取扫描类型描述
 */
export const getScanTypeDescription = (type: ScanType): string => {
  const descMap: Record<ScanType, string> = {
    PORT_SCAN: '端口扫描',
    WEB_SCAN: 'Web应用扫描',
    SYSTEM_SCAN: '系统漏洞扫描',
    COMPREHENSIVE_SCAN: '综合扫描',
    CUSTOM_SCAN: '自定义扫描'
  }
  return descMap[type] || type
}

/**
 * 获取任务状态描述
 */
export const getTaskStatusDescription = (status: TaskStatus): string => {
  const descMap: Record<TaskStatus, string> = {
    CREATED: '已创建',
    QUEUED: '排队中',
    RUNNING: '扫描中',
    PAUSED: '已暂停',
    COMPLETED: '已完成',
    FAILED: '扫描失败',
    CANCELLED: '已取消',
    TIMEOUT: '扫描超时'
  }
  return descMap[status] || status
}

/**
 * 格式化进度百分比
 */
export const formatProgress = (progress: number): string => {
  return `${progress}%`
}

/**
 * 格式化执行时间
 */
export const formatDuration = (minutes?: number): string => {
  if (!minutes) return '-'

  if (minutes < 60) {
    return `${minutes}分钟`
  } else {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  }
}

/**
 * 检查任务是否可以启动
 */
export const canStartTask = (status: TaskStatus): boolean => {
  return status === 'CREATED' || status === 'QUEUED'
}

/**
 * 检查任务是否可以停止
 */
export const canStopTask = (status: TaskStatus): boolean => {
  return status === 'RUNNING' || status === 'QUEUED'
}

/**
 * 检查任务是否正在运行
 */
export const isTaskRunning = (status: TaskStatus): boolean => {
  return status === 'RUNNING'
}

/**
 * 检查任务是否已完成
 */
export const isTaskCompleted = (status: TaskStatus): boolean => {
  return status === 'COMPLETED'
}

/**
 * 检查任务是否失败
 */
export const isTaskFailed = (status: TaskStatus): boolean => {
  return status === 'FAILED' || status === 'TIMEOUT'
}

// 扫描管理API (保持向后兼容)
export const scanApi = {
  getTasks: getScanTasks,
  getTaskById: getScanTask,
  createTask: createScanTask,
  executeTask: startScanTask,
  cancelTask: stopScanTask,
  deleteTask: deleteScanTask
}

export default scanApi
