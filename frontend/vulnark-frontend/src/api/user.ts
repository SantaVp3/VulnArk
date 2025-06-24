import { request } from './index'

export interface User {
  id: number
  username: string
  email: string
  fullName: string
  phone?: string
  avatarUrl?: string
  role: 'ADMIN' | 'MANAGER' | 'ANALYST' | 'VIEWER' | 'USER'
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  department?: string
  position?: string
  notes?: string
  lastLoginTime?: string
  createdTime: string
  updatedTime: string
}

export interface CreateUserRequest {
  username: string
  password: string
  confirmPassword: string
  email: string
  fullName: string
  phone?: string
  avatarUrl?: string
  role: string
  status?: string
  department?: string
  position?: string
  notes?: string
}

export interface UpdateUserRequest {
  email: string
  fullName: string
  phone?: string
  role: string
  status: string
  department?: string
  position?: string
  notes?: string
}

export interface ResetPasswordRequest {
  newPassword: string
  confirmPassword: string
}

export interface UserQueryRequest {
  username?: string
  email?: string
  fullName?: string
  role?: string
  status?: string
  department?: string
  position?: string
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export interface UserStats {
  total: number
  active: number
  inactive: number
  admin: number
  manager: number
  user: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 用户管理API
export const userApi = {
  // 获取用户列表
  getList: (params?: UserQueryRequest): Promise<PageResponse<User>> => {
    return request.get('/users', { params })
  },
  
  // 获取用户详情
  getById: (id: number): Promise<User> => {
    return request.get(`/users/${id}`)
  },
  
  // 创建用户
  create: (data: CreateUserRequest): Promise<User> => {
    return request.post('/users', data)
  },
  
  // 更新用户
  update: (id: number, data: Partial<CreateUserRequest>): Promise<User> => {
    return request.put(`/users/${id}`, data)
  },
  
  // 删除用户
  delete: (id: number): Promise<string> => {
    return request.delete(`/users/${id}`)
  },
  
  // 获取所有用户
  getAll: (): Promise<User[]> => {
    return request.get('/users/all')
  },
  
  // 根据角色获取用户
  getByRole: (role: string): Promise<User[]> => {
    return request.get(`/users/role/${role}`)
  },
  
  // 根据状态获取用户
  getByStatus: (status: string): Promise<User[]> => {
    return request.get(`/users/status/${status}`)
  },
  
  // 根据部门获取用户
  getByDepartment: (department: string): Promise<User[]> => {
    return request.get(`/users/department/${department}`)
  },
  
  // 更新用户状态
  updateStatus: (id: number, status: string): Promise<User> => {
    return request.put(`/users/${id}/status`, { status })
  },
  
  // 重置用户密码
  resetPassword: (id: number, data: ResetPasswordRequest): Promise<User> => {
    return request.put(`/users/${id}/password`, { password: data.newPassword })
  },
  
  // 获取用户统计
  getStats: (): Promise<UserStats> => {
    return request.get('/users/stats')
  },
  
  // 批量导入用户
  import: (data: CreateUserRequest[]): Promise<User[]> => {
    return request.post('/users/import', data)
  },
  
  // 批量导出用户
  export: (params?: UserQueryRequest): Promise<Blob> => {
    return request.get('/users/export', {
      params,
      responseType: 'blob'
    })
  },

  // 批量删除用户
  batchDelete: (ids: number[]): Promise<string> => {
    return request.delete('/users/batch', { data: { ids } })
  },

  // 导入用户文件
  importFile: (file: File): Promise<{ success: number; failed: number; errors: string[] }> => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/users/import-file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 下载导入模板
  downloadTemplate: (): Promise<Blob> => {
    return request.get('/users/template', {
      responseType: 'blob'
    })
  },

  // 检查用户名是否可用
  checkUsername: (username: string, excludeId?: number): Promise<{ available: boolean }> => {
    return request.get('/users/check-username', {
      params: { username, excludeId }
    })
  },

  // 检查邮箱是否可用
  checkEmail: (email: string, excludeId?: number): Promise<{ available: boolean }> => {
    return request.get('/users/check-email', {
      params: { email, excludeId }
    })
  }
}

export default userApi
