import request from '@/utils/request'
import type { UserInfoVO, UpdateProfileDTO, ChangePasswordDTO } from '@/types/user'

export function getUserInfo(): Promise<UserInfoVO> {
  return request.get<UserInfoVO>('/user/me')
}

export function updateProfile(data: UpdateProfileDTO): Promise<void> {
  return request.put<void>('/user/profile', data)
}

export function changePassword(data: ChangePasswordDTO): Promise<void> {
  return request.put<void>('/user/password', data)
}
