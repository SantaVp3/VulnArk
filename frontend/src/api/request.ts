import axios from 'axios'
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios'
import { Message } from '@arco-design/web-vue'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加缓存控制头，防止浏览器缓存
    if (config.method === 'get') {
      config.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
      config.headers['Pragma'] = 'no-cache'
      config.headers['Expires'] = '0'
      
      // 添加时间戳参数到URL，确保GET请求不会被缓存
      if (!config.params) {
        config.params = {}
      }
      config.params._t = new Date().getTime()
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 检查响应数据结构
    if (data && typeof data === 'object' && 'code' in data) {
      // 后端返回格式: {code: 200, message: "success", data: [...]}
      if (data.code === 200) {
        return data.data !== undefined ? data.data : data
      } else {
        Message.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    } else if (response.status === 200) {
      // 直接返回数据
      return data
    } else {
      Message.error('请求失败')
      return Promise.reject(new Error('请求失败'))
    }
  },
  (error: AxiosError) => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          Message.error('认证失败，请重新登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          Message.error('权限不足')
          break
        case 404:
          Message.error('请求的资源不存在')
          break
        case 500:
          Message.error('服务器内部错误')
          break
        default:
          Message.error((data as any)?.message || '网络错误')
      }
    } else {
      Message.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request 