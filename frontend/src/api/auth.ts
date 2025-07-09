import request from './request'
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '@/types/auth'

// 登录
export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await request.post('/auth/login', data)
  return response.data.data
}

// 注册
export const register = async (data: RegisterRequest): Promise<void> => {
  const response = await request.post('/auth/register', data)
  return response.data.data
}

// 获取当前用户信息
export const getCurrentUser = async (): Promise<User> => {
  const response = await request.get('/auth/me')
  return response.data.data
}

// 登出
export const logout = async (): Promise<void> => {
  const response = await request.post('/auth/logout')
  return response.data.data
}