import request from './request'

// 用户类型定义
export interface User {
  id: number
  username: string
  email: string
  fullName?: string
  role: 'ADMIN' | 'MANAGER' | 'ANALYST' | 'VIEWER' | 'USER'
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  department?: string
  position?: string
  phone?: string
  avatarUrl?: string
  notes?: string
  lastLoginTime?: string
  createdTime?: string
  updatedTime?: string
  deleted?: boolean
}

export interface UserRequest {
  username: string
  email: string
  fullName?: string
  role: 'ADMIN' | 'MANAGER' | 'ANALYST' | 'VIEWER' | 'USER'
  status?: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  department?: string
  position?: string
  phone?: string
  password?: string
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

// 用户API
export const userApi = {
  // 获取所有用户
  getAllUsers: async (): Promise<User[]> => {
    return request.get('/users/all') as Promise<User[]>;
  },

  // 分页查询用户
  getUsers: async (params: UserQueryRequest) => {
    return request.get('/users', { params }) as Promise<{content: User[], totalElements: number}>;
  },

  // 获取用户详情
  getUser: async (id: number): Promise<User> => {
    return request.get(`/users/${id}`) as Promise<User>;
  },

  // 创建用户
  createUser: async (data: UserRequest): Promise<User> => {
    return request.post('/users', data) as Promise<User>;
  },

  // 更新用户
  updateUser: async (id: number, data: UserRequest): Promise<User> => {
    return request.put(`/users/${id}`, data) as Promise<User>;
  },

  // 删除用户
  deleteUser: async (id: number): Promise<string> => {
    return request.delete(`/users/${id}`) as Promise<string>;
  },

  // 更新用户状态
  updateUserStatus: async (id: number, status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'): Promise<User> => {
    return request.put(`/users/${id}/status`, { status }) as Promise<User>;
  },

  // 重置用户密码
  resetUserPassword: async (id: number, password: string): Promise<User> => {
    return request.put(`/users/${id}/password`, { password }) as Promise<User>;
  },

  // 根据角色获取用户
  getUsersByRole: async (role: string): Promise<User[]> => {
    return request.get(`/users/role/${role}`) as Promise<User[]>;
  },

  // 获取用户统计信息
  getUserStats: async () => {
    return request.get('/users/stats');
  }
} 