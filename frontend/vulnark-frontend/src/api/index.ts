import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // 后端返回的是ApiResponse格式: { code, message, data, timestamp }
    const result = response.data
    if (result.code === 200) {
      return result // 返回完整的响应，让调用方处理
    } else {
      console.error('API Error:', result.message)
      return Promise.reject(new Error(result.message || '请求失败'))
    }
  },
  (error) => {
    console.error('Response Error:', error.response?.status, error.response?.data, error.message)
    if (error.response?.status === 401) {
      // 清除token并跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }

    // 返回更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(errorMessage))
  }
)

export default api
export { api as request }
