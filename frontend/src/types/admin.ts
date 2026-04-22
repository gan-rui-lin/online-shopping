export interface DashboardVO {
  userCount: number
  merchantCount: number
  productCount: number
  orderCount: number
  gmv: number
  todayOrderCount: number
}

export interface AdminUserVO {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  status: number
  userType: number
  roles: string[]
  lastLoginTime: string
  createTime: string
}

export interface AdminUserQueryDTO {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: number
  userType?: number
}
