import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserInfo, LoginRequest, LoginResponse } from '@/types/auth'
import { login as apiLogin, getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  
  const isAuthenticated = computed(() => !!token.value)
  
  const login = async (credentials: LoginRequest) => {
    try {
      const response = await apiLogin(credentials)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      return response
    } catch (error) {
      throw error
    }
  }
  
  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }
  
  const checkAuth = async () => {
    if (token.value) {
      try {
        const userData = await getCurrentUser()
        // 转换User类型到UserInfo类型
        user.value = {
          id: userData.id,
          username: userData.username,
          email: userData.email,
          fullName: userData.fullName,
          phone: userData.phone,
          role: userData.role,
          status: userData.status,
          department: userData.department,
          position: userData.position
        }
      } catch (error) {
        logout()
      }
    }
  }
  
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUser = (newUser: UserInfo) => {
    user.value = newUser
  }
  
  return {
    user,
    token,
    isAuthenticated,
    login,
    logout,
    checkAuth,
    setToken,
    setUser
  }
}) 