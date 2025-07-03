import request from './request'
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '@/types/auth'

// 登录
export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  return request.post('/auth/login', data)
}

// 注册
export const register = async (data: RegisterRequest): Promise<void> => {
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export const getCurrentUser = async (): Promise<User> => {
  return request.get('/auth/me')
}

// 登出
export const logout = async (): Promise<void> => {
  return request.post('/auth/logout')
} 