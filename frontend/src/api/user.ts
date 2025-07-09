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
  phone?: string
  password?: string
  notes?: string
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
  getAllUsers: async () => {
    const response = await request.get('/users/all');
    return response.data.data || [];
  },

  // 分页查询用户
  getUsers: async (params: UserQueryRequest) => {
    const response = await request.get('/users', { params });
    return response.data.data || { content: [], totalElements: 0 };
  },

  // 获取用户详情
  getUser: async (id: number) => {
    const response = await request.get(`/users/${id}`);
    return response.data.data;
  },

  // 创建用户
  createUser: async (data: UserRequest) => {
    console.log('创建用户请求数据:', data);
    try {
      const response = await request.post('/users', data);
      console.log('创建用户响应:', response);
      return response.data.data;
    } catch (error) {
      console.error('创建用户错误:', error);
      throw error;
    }
  },

  // 更新用户
  updateUser: async (id: number, data: UserRequest) => {
    const response = await request.put(`/users/${id}`, data);
    return response.data.data;
  },

  // 删除用户
  deleteUser: async (id: number) => {
    const response = await request.delete(`/users/${id}`);
    return response.data.data;
  },

  // 更新用户状态
  updateUserStatus: async (id: number, status: 'ACTIVE' | 'INACTIVE' | 'LOCKED') => {
    const response = await request.put(`/users/${id}/status`, { status });
    return response.data.data;
  },

  // 重置用户密码
  resetUserPassword: async (id: number, password: string) => {
    const response = await request.put(`/users/${id}/password`, { password });
    return response.data.data;
  },

  // 根据角色获取用户
  getUsersByRole: async (role: string) => {
    const response = await request.get(`/users/role/${role}`);
    return response.data.data || [];
  },

  // 获取用户统计信息
  getUserStats: async () => {
    const response = await request.get('/users/stats');
    return response.data.data;
  }
}