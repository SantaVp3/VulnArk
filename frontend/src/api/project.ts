import request from './request'

// 项目类型定义
export interface Project {
  id: number
  name: string
  description?: string
  status: 'ACTIVE' | 'INACTIVE' | 'COMPLETED'
  createdTime?: string
  updatedTime?: string
}

// 项目API
export const projectApi = {
  // 获取所有项目
  getAllProjects: () => {
    return request.get<Project[]>('/projects/all')
  },

  // 获取项目详情
  getProject: (id: number) => {
    return request.get<Project>(`/projects/${id}`)
  }
} 