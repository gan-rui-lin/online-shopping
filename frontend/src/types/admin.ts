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

export interface AdminOrderVO {
  orderNo: string
  userId: number
  username: string
  shopId: number
  shopName: string
  orderStatus: number
  payStatus: number
  payAmount: number
  cancelReason: string
  createTime: string
  payTime: string
}

export interface AdminOrderQueryDTO {
  pageNum: number
  pageSize: number
  orderNo?: string
  userId?: number
  shopId?: number
  orderStatus?: number
}
