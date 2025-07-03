// 用户角色枚举
export enum UserRole {
  ADMIN = 'ADMIN',
  ANALYST = 'ANALYST', 
  VIEWER = 'VIEWER'
}

// 用户状态枚举
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED'
}

export interface User {
  id: number
  username: string
  fullName: string
  email: string
  phone?: string
  role: UserRole
  status: UserStatus
  department?: string
  position?: string
  createdTime: string
  updatedTime: string
  lastLoginTime?: string
}

export interface LoginRequest {
  username: string
  password: string
}

// 登录响应中的用户信息
export interface UserInfo {
  id: number
  username: string
  email: string
  fullName?: string
  phone?: string
  role: UserRole
  status: UserStatus
  department?: string
  position?: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface RegisterRequest {
  username: string
  fullName: string
  email: string
  phone?: string
  password: string
  confirmPassword: string
} 