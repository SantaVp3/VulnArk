import api from './index'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName?: string
  phone?: string
}

export interface User {
  id: number
  username: string
  email: string
  fullName?: string
  phone?: string
  avatarUrl?: string
  role: 'ADMIN' | 'MANAGER' | 'ANALYST' | 'VIEWER'
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  lastLoginTime?: string
  createdTime: string
  updatedTime: string
}

export interface LoginResponse {
  token: string
  user: User
}

// 用户登录
export const login = (data: LoginRequest) => {
  return api.post<any, LoginResponse>('/auth/login', data)
}

// 用户注册
export const register = (data: RegisterRequest) => {
  return api.post<any, User>('/auth/register', data)
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return api.get<any, User>('/auth/me')
}

// 用户登出
export const logout = () => {
  return api.post('/auth/logout')
}
