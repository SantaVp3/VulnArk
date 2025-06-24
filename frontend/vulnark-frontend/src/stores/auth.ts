import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/api/auth'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 从localStorage恢复用户信息
  const getStoredUser = (): User | null => {
    try {
      const storedUser = localStorage.getItem('user')
      return storedUser ? JSON.parse(storedUser) : null
    } catch (error) {
      console.error('Failed to parse stored user:', error)
      localStorage.removeItem('user')
      return null
    }
  }

  const user = ref<User | null>(getStoredUser())
  const token = ref<string | null>(localStorage.getItem('token'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isManager = computed(() => user.value?.role === 'MANAGER' || isAdmin.value)

  // 登录
  const login = async (username: string, password: string) => {
    try {
      const response = await authApi.login({ username, password })

      // API返回的是 { code, message, data, timestamp } 格式
      // data 包含 { token, user }
      const loginData = response.data
      token.value = loginData.token
      user.value = loginData.user

      // 持久化token和用户信息
      localStorage.setItem('token', loginData.token)
      localStorage.setItem('user', JSON.stringify(loginData.user))

      return loginData
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  // 注册
  const register = async (data: authApi.RegisterRequest) => {
    try {
      const response = await authApi.register(data)
      return response
    } catch (error) {
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      // 即使API调用失败，也要清除本地状态
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    try {
      if (token.value) {
        const userData = await authApi.getCurrentUser()
        user.value = userData
        // 更新localStorage中的用户信息
        localStorage.setItem('user', JSON.stringify(userData))
      }
    } catch (error) {
      // 如果获取用户信息失败，清除token
      logout()
      throw error
    }
  }

  // 初始化认证状态
  const initializeAuth = async () => {
    // 如果有token但没有用户信息，尝试获取用户信息
    if (token.value && !user.value) {
      try {
        await fetchCurrentUser()
      } catch (error) {
        console.error('Failed to initialize auth:', error)
        // 如果初始化失败，清除所有认证信息
        logout()
      }
    }
  }

  // 验证token是否有效
  const validateToken = async () => {
    if (token.value) {
      try {
        await authApi.getCurrentUser()
        return true
      } catch (error) {
        // token无效，清除认证信息
        logout()
        return false
      }
    }
    return false
  }

  // 初始化时如果有token，验证并获取用户信息
  if (token.value) {
    initializeAuth()
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    isManager,
    login,
    register,
    logout,
    fetchCurrentUser,
    initializeAuth,
    validateToken
  }
})
