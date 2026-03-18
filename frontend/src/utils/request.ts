import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, getTokenHead, removeToken } from './storage'
import router from '@/router'
import type { ApiResult } from '@/types/common'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      const head = getTokenHead()
      config.headers.Authorization = `${head}${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    if (res.code === 200) {
      return res.data as any
    }
    ElMessage.error(res.message || 'Request failed')
    return Promise.reject(new Error(res.message || 'Request failed'))
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
      ElMessage.error('Session expired, please login again')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || 'Network error')
    }
    return Promise.reject(error)
  },
)

function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

request.get = <T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ ...config, method: 'GET', url, params })
}

request.post = <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ ...config, method: 'POST', url, data })
}

request.put = <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ ...config, method: 'PUT', url, data })
}

request.delete = <T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ ...config, method: 'DELETE', url, params })
}

export default request
