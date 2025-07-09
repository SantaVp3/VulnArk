import request from './request'

// Agent状态枚举
export enum AgentStatus {
  ONLINE = 'ONLINE',
  OFFLINE = 'OFFLINE',
  ERROR = 'ERROR',
  MAINTENANCE = 'MAINTENANCE'
}

// Agent平台枚举
export enum AgentPlatform {
  WINDOWS = 'WINDOWS',
  LINUX = 'LINUX'
}

// Agent接口
export interface Agent {
  id: number
  agentId: string
  name: string
  hostname: string
  ipAddress: string
  platform: AgentPlatform
  osVersion: string
  agentVersion: string
  status: AgentStatus
  description?: string
  lastHeartbeat: string
  registrationTime: string
  updateTime: string
}

// Agent统计信息
export interface AgentStats {
  totalAgents: number
  onlineAgents: number
  offlineAgents: number
  errorAgents: number
  windowsAgents: number
  linuxAgents: number
}

// 基线任务状态枚举
export enum BaselineTaskStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

// 基线任务类型枚举
export enum BaselineTaskType {
  CIS_BENCHMARK = 'CIS_BENCHMARK',
  CUSTOM_BASELINE = 'CUSTOM_BASELINE',
  SECURITY_POLICY = 'SECURITY_POLICY',
  SYSTEM_CONFIG = 'SYSTEM_CONFIG'
}

// 基线任务接口
export interface BaselineTask {
  id: number
  taskId: string
  agentId: string
  agentName?: string
  name: string
  taskType: BaselineTaskType
  status: BaselineTaskStatus
  configuration: string
  checkRules: string
  scheduledTime: string
  startTime?: string
  endTime?: string
  errorMessage?: string
  createTime: string
  updateTime: string
}

// 基线检查结果
export interface BaselineResult {
  id: number
  taskId: string
  agentId: string
  checkId: string
  checkName: string
  category: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
  status: 'PASS' | 'FAIL' | 'ERROR' | 'SKIP'
  description: string
  expectedValue: string
  actualValue: string
  evidence: string
  recommendation: string
  reference: string
  score: number
  createTime: string
}

// 基线规则接口
export interface BaselineRule {
  id: number
  ruleId: string
  name: string
  category: string
  description: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
  platform: 'WINDOWS' | 'LINUX' | 'BOTH'
  standard: string
  version: string
  checkScript: string
  expectedValue: string
  recommendation: string
  reference: string
  enabled: boolean
  score: number
  tags?: string
  createTime: string
  updateTime: string
}

// Agent API
export const agentApi = {
  // 获取Agent列表
  getAgents: (params?: {
    page?: number
    size?: number
    keyword?: string
    status?: AgentStatus
    platform?: AgentPlatform
  }) => {
    return request.get('/admin/agents', { params })
  },

  // 获取Agent详情
  getAgent: (agentId: string) => {
    return request.get(`/admin/agents/${agentId}`)
  },

  // 更新Agent状态
  updateAgentStatus: (agentId: string, status: AgentStatus) => {
    return request.put(`/admin/agents/${agentId}/status`, { status })
  },

  // 删除Agent
  deleteAgent: (agentId: string) => {
    return request.delete(`/admin/agents/${agentId}`)
  },

  // 获取Agent统计信息
  getAgentStats: () => {
    return request.get('/admin/agents/stats')
  },

  // 强制Agent离线
  forceOffline: (agentId: string) => {
    return request.post(`/admin/agents/${agentId}/offline`)
  },

  // 下载Agent客户端
  downloadAgent: (platform: string, arch: string) => {
    return request.get(`/admin/agents/download/${platform}/${arch}`, {
      responseType: 'blob'
    })
  },

  // 获取下载链接
  getDownloadUrl: (platform: string, arch: string) => {
    return `/api/admin/agents/download/${platform}/${arch}`
  }
}

// 基线任务API
export const baselineTaskApi = {
  // 获取任务列表
  getTasks: (params?: {
    page?: number
    size?: number
    agentId?: string
    status?: BaselineTaskStatus
    taskType?: BaselineTaskType
  }) => {
    return request.get('/admin/baseline/tasks', { params })
  },

  // 创建任务
  createTask: (data: {
    agentIds: string[]
    name: string
    taskType: BaselineTaskType
    ruleIds: string[]
    scheduledTime?: string
  }) => {
    return request.post('/admin/baseline/tasks', data)
  },

  // 获取任务详情
  getTask: (taskId: string) => {
    return request.get(`/admin/baseline/tasks/${taskId}`)
  },

  // 取消任务
  cancelTask: (taskId: string) => {
    return request.post(`/admin/baseline/tasks/${taskId}/cancel`)
  },

  // 重新执行任务
  retryTask: (taskId: string) => {
    return request.post(`/admin/baseline/tasks/${taskId}/retry`)
  },

  // 获取任务结果
  getTaskResults: (taskId: string) => {
    return request.get(`/admin/baseline/tasks/${taskId}/results`)
  }
}

// 基线规则API
export const baselineRuleApi = {
  // 获取规则列表
  getRules: (params?: {
    page?: number
    size?: number
    keyword?: string
    category?: string
    severity?: string
    platform?: string
    standard?: string
    enabled?: boolean
  }) => {
    return request.get('/admin/baseline/rules', { params })
  },

  // 创建规则
  createRule: (data: Omit<BaselineRule, 'id' | 'createTime' | 'updateTime'>) => {
    return request.post('/admin/baseline/rules', data)
  },

  // 更新规则
  updateRule: (ruleId: string, data: Partial<BaselineRule>) => {
    return request.put(`/admin/baseline/rules/${ruleId}`, data)
  },

  // 删除规则
  deleteRule: (ruleId: string) => {
    return request.delete(`/admin/baseline/rules/${ruleId}`)
  },

  // 启用/禁用规则
  toggleRule: (ruleId: string, enabled: boolean) => {
    return request.put(`/admin/baseline/rules/${ruleId}/toggle`, { enabled })
  },

  // 获取规则分类
  getCategories: () => {
    return request.get('/admin/baseline/rules/categories')
  },

  // 获取规则标准
  getStandards: () => {
    return request.get('/admin/baseline/rules/standards')
  },

  // 批量导入规则
  importRules: (rules: BaselineRule[]) => {
    return request.post('/admin/baseline/rules/import', { rules })
  }
}
