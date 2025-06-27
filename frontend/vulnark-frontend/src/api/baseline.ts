import api from './index'
import type { Asset } from './asset'
import type { User } from './user'

// 基线检查相关接口

/**
 * 基线检查类型
 */
export interface BaselineCheck {
  id?: number
  name: string
  description?: string
  checkType: CheckType
  asset?: Asset
  status: CheckStatus
  result?: CheckResult
  progress: number
  totalItems: number
  passedItems: number
  failedItems: number
  warningItems: number
  skippedItems: number
  complianceScore: number
  checkConfig?: string
  reportPath?: string
  errorMessage?: string
  createdBy?: User
  startTime?: string
  endTime?: string
  createdTime?: string
  updatedTime?: string
  deleted?: boolean
}

/**
 * 基线检查项类型
 */
export interface BaselineCheckItem {
  id?: number
  baselineCheck?: BaselineCheck
  itemCode: string
  itemName: string
  description?: string
  category?: string
  severity: SeverityLevel
  status: ItemStatus
  result?: ItemResult
  expectedValue?: string
  actualValue?: string
  checkCommand?: string
  checkDetails?: string
  remediation?: string
  reference?: string
  errorMessage?: string
  checkTime?: string
  createdTime?: string
  updatedTime?: string
}

/**
 * 检查类型枚举
 */
export enum CheckType {
  SYSTEM_SECURITY = 'SYSTEM_SECURITY',
  NETWORK_SECURITY = 'NETWORK_SECURITY',
  DATABASE_SECURITY = 'DATABASE_SECURITY',
  WEB_SECURITY = 'WEB_SECURITY',
  MIDDLEWARE_SECURITY = 'MIDDLEWARE_SECURITY',
  CLOUD_SECURITY = 'CLOUD_SECURITY',
  CUSTOM = 'CUSTOM'
}

/**
 * 检查状态枚举
 */
export enum CheckStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

/**
 * 检查结果枚举
 */
export enum CheckResult {
  PASS = 'PASS',
  FAIL = 'FAIL',
  WARNING = 'WARNING',
  PARTIAL = 'PARTIAL'
}

/**
 * 严重级别枚举
 */
export enum SeverityLevel {
  CRITICAL = 'CRITICAL',
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW',
  INFO = 'INFO'
}

/**
 * 检查项状态枚举
 */
export enum ItemStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  SKIPPED = 'SKIPPED',
  ERROR = 'ERROR'
}

/**
 * 检查项结果枚举
 */
export enum ItemResult {
  PASS = 'PASS',
  FAIL = 'FAIL',
  WARNING = 'WARNING',
  NOT_APPLICABLE = 'NOT_APPLICABLE'
}

/**
 * 基线检查统计信息
 */
export interface BaselineCheckStatistics {
  statusCounts: Record<CheckStatus, number>
  typeCounts: Record<CheckType, number>
  resultCounts: Record<CheckResult, number>
  averageComplianceScore: number
}

/**
 * 资产检查统计信息
 */
export interface AssetCheckStatistics {
  assetId: number
  assetName: string
  totalChecks: number
  passedChecks: number
  failedChecks: number
  averageScore: number
}

/**
 * 分页查询参数
 */
export interface BaselineCheckSearchParams {
  name?: string
  checkType?: CheckType
  status?: CheckStatus
  assetId?: number
  createdById?: number
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

/**
 * 检查项查询参数
 */
export interface CheckItemSearchParams {
  itemName?: string
  category?: string
  severity?: SeverityLevel
  status?: ItemStatus
  result?: ItemResult
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

// API 接口

/**
 * 创建基线检查
 */
export const createBaselineCheck = (data: BaselineCheck) => {
  return api.post('/baseline/checks', data)
}

/**
 * 获取基线检查详情
 */
export const getBaselineCheck = (id: number) => {
  return api.get(`/baseline/checks/${id}`)
}

/**
 * 搜索基线检查
 */
export const searchBaselineChecks = (params: BaselineCheckSearchParams) => {
  return api.get('/baseline/checks/search', { params })
}

/**
 * 启动基线检查
 */
export const startBaselineCheck = (id: number) => {
  return api.post(`/baseline/checks/${id}/start`)
}

/**
 * 停止基线检查
 */
export const stopBaselineCheck = (id: number) => {
  return api.post(`/baseline/checks/${id}/stop`)
}

/**
 * 删除基线检查
 */
export const deleteBaselineCheck = (id: number) => {
  return api.delete(`/baseline/checks/${id}`)
}

/**
 * 获取检查项列表
 */
export const getCheckItems = (checkId: number, params?: CheckItemSearchParams) => {
  return api.get(`/baseline/checks/${checkId}/items`, { params })
}

/**
 * 搜索检查项
 */
export const searchCheckItems = (checkId: number, params: CheckItemSearchParams) => {
  return api.get(`/baseline/checks/${checkId}/items/search`, { params })
}

/**
 * 获取失败的检查项
 */
export const getFailedItems = (checkId: number) => {
  return api.get(`/baseline/checks/${checkId}/items/failed`)
}

/**
 * 获取高风险失败项
 */
export const getHighRiskFailedItems = (checkId: number) => {
  return api.get(`/baseline/checks/${checkId}/items/high-risk`)
}

/**
 * 获取检查统计信息
 */
export const getCheckStatistics = () => {
  return api.get('/baseline/statistics')
}

/**
 * 获取资产检查统计
 */
export const getAssetCheckStatistics = () => {
  return api.get('/baseline/statistics/assets')
}

/**
 * 获取检查类型枚举
 */
export const getCheckTypes = () => {
  return api.get('/baseline/check-types')
}

/**
 * 获取严重级别枚举
 */
export const getSeverityLevels = () => {
  return api.get('/baseline/severity-levels')
}

// 工具函数

/**
 * 获取检查类型描述
 */
export const getCheckTypeDescription = (type: CheckType): string => {
  const descriptions = {
    [CheckType.SYSTEM_SECURITY]: '系统安全基线',
    [CheckType.NETWORK_SECURITY]: '网络安全基线',
    [CheckType.DATABASE_SECURITY]: '数据库安全基线',
    [CheckType.WEB_SECURITY]: 'Web应用安全基线',
    [CheckType.MIDDLEWARE_SECURITY]: '中间件安全基线',
    [CheckType.CLOUD_SECURITY]: '云安全基线',
    [CheckType.CUSTOM]: '自定义基线'
  }
  return descriptions[type] || type
}

/**
 * 获取检查状态描述
 */
export const getCheckStatusDescription = (status: CheckStatus): string => {
  const descriptions = {
    [CheckStatus.PENDING]: '待检查',
    [CheckStatus.RUNNING]: '检查中',
    [CheckStatus.COMPLETED]: '已完成',
    [CheckStatus.FAILED]: '检查失败',
    [CheckStatus.CANCELLED]: '已取消'
  }
  return descriptions[status] || status
}

/**
 * 获取检查结果描述
 */
export const getCheckResultDescription = (result: CheckResult): string => {
  const descriptions = {
    [CheckResult.PASS]: '通过',
    [CheckResult.FAIL]: '失败',
    [CheckResult.WARNING]: '警告',
    [CheckResult.PARTIAL]: '部分通过'
  }
  return descriptions[result] || result
}

/**
 * 获取严重级别描述
 */
export const getSeverityLevelDescription = (severity: SeverityLevel): string => {
  const descriptions = {
    [SeverityLevel.CRITICAL]: '严重',
    [SeverityLevel.HIGH]: '高',
    [SeverityLevel.MEDIUM]: '中',
    [SeverityLevel.LOW]: '低',
    [SeverityLevel.INFO]: '信息'
  }
  return descriptions[severity] || severity
}

/**
 * 获取检查项状态描述
 */
export const getItemStatusDescription = (status: ItemStatus): string => {
  const descriptions = {
    [ItemStatus.PENDING]: '待检查',
    [ItemStatus.RUNNING]: '检查中',
    [ItemStatus.COMPLETED]: '已完成',
    [ItemStatus.SKIPPED]: '已跳过',
    [ItemStatus.ERROR]: '检查错误'
  }
  return descriptions[status] || status
}

/**
 * 获取检查项结果描述
 */
export const getItemResultDescription = (result: ItemResult): string => {
  const descriptions = {
    [ItemResult.PASS]: '通过',
    [ItemResult.FAIL]: '失败',
    [ItemResult.WARNING]: '警告',
    [ItemResult.NOT_APPLICABLE]: '不适用'
  }
  return descriptions[result] || result
}
