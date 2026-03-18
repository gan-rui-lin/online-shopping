import request from '@/utils/request'
import type { LoginDTO, RegisterDTO, LoginVO } from '@/types/auth'

export function login(data: LoginDTO): Promise<LoginVO> {
  return request.post<LoginVO>('/auth/login', data)
}

export function register(data: RegisterDTO): Promise<void> {
  return request.post<void>('/auth/register', data)
}

export function logout(): Promise<void> {
  return request.post<void>('/auth/logout')
}
