export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatarUrl: string
  userType: number
  roles: string[]
}

export interface UpdateProfileDTO {
  nickname?: string
  email?: string
  avatarUrl?: string
  phone?: string
}

export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
}
