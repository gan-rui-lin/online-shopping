export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
}

export interface LoginVO {
  token: string
  tokenHead: string
  userId: number
  username: string
  nickname: string
  userType: number
  roles: string[]
}
