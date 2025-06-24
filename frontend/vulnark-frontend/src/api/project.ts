import api from './index'

// 项目相关类型定义
export interface Project {
  id: number
  name: string
  description?: string
  ownerId: number
  status: 'ACTIVE' | 'INACTIVE' | 'COMPLETED' | 'ARCHIVED' | 'SUSPENDED'
  type?: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  startDate?: string
  endDate?: string
  budget?: number
  tags?: string
  memberCount: number
  vulnerabilityCount: number
  assetCount: number
  progress: number
  createdTime: string
  updatedTime: string
  deleted: boolean
}

export interface ProjectRequest {
  name: string
  description?: string
  ownerId: number
  status?: 'ACTIVE' | 'INACTIVE' | 'COMPLETED' | 'ARCHIVED' | 'SUSPENDED'
  type?: string
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  startDate?: string
  endDate?: string
  budget?: number
  tags?: string
  memberCount?: number
  progress?: number
}

export interface ProjectQueryParams {
  name?: string
  status?: string
  priority?: string
  type?: string
  ownerId?: number
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export interface ProjectStats {
  total: number
  active: number
  completed: number
  archived: number
  high: number
  critical: number
}

// 获取项目列表
export const getProjects = (params: ProjectQueryParams) => {
  return api.get<any, { content: Project[], totalElements: number, totalPages: number }>('/projects', { params })
}

// 获取所有项目
export const getAllProjects = () => {
  return api.get<any, Project[]>('/projects/all')
}

// 根据ID获取项目详情
export const getProjectById = (id: number) => {
  return api.get<any, Project>(`/projects/${id}`)
}

// 创建项目
export const createProject = (data: ProjectRequest) => {
  return api.post<any, Project>('/projects', data)
}

// 更新项目
export const updateProject = (id: number, data: ProjectRequest) => {
  return api.put<any, Project>(`/projects/${id}`, data)
}

// 删除项目
export const deleteProject = (id: number) => {
  return api.delete<any, string>(`/projects/${id}`)
}

// 根据负责人ID获取项目
export const getProjectsByOwnerId = (ownerId: number) => {
  return api.get<any, Project[]>(`/projects/owner/${ownerId}`)
}

// 获取最近的项目
export const getRecentProjects = (limit: number = 10) => {
  return api.get<any, Project[]>('/projects/recent', { params: { limit } })
}

// 获取即将到期的项目
export const getOverdueProjects = () => {
  return api.get<any, Project[]>('/projects/overdue')
}

// 获取活跃项目
export const getActiveProjects = () => {
  return api.get<any, Project[]>('/projects/active')
}

// 更新项目状态
export const updateProjectStatus = (id: number, status: string) => {
  return api.put<any, Project>(`/projects/${id}/status`, { status })
}

// 更新项目进度
export const updateProjectProgress = (id: number, progress: number) => {
  return api.put<any, Project>(`/projects/${id}/progress`, { progress })
}

// 更新项目统计信息
export const updateProjectStatistics = (id: number) => {
  return api.put<any, Project>(`/projects/${id}/statistics`)
}

// 获取项目统计信息
export const getProjectStats = () => {
  return api.get<any, ProjectStats>('/projects/stats')
}

// 批量导入项目
export const importProjects = (data: ProjectRequest[]) => {
  return api.post<any, Project[]>('/projects/import', data)
}

// 批量导出项目
export const exportProjects = (projectIds?: number[]) => {
  return api.post<any, Project[]>('/projects/export', projectIds || [])
}
