export interface DashboardVO {
  userCount: number
  merchantCount: number
  productCount: number
  orderCount: number
  gmv: number
  todayOrderCount: number
  orderTrend: DashboardTrendVO[]
  gmvTrend: DashboardTrendVO[]
  orderStatusStats: OrderStatusStatVO[]
}

export interface DashboardTrendVO {
  date: string
  value: number
}

export interface OrderStatusStatVO {
  status: number
  count: number
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

export interface AdminActionLogVO {
  id: number
  operatorId: number
  operatorName: string
  module: string
  action: string
  targetType: string
  targetId: string
  detail: string
  success: number
  createTime: string
}

export interface AdminActionLogQueryDTO {
  pageNum: number
  pageSize: number
  operatorId?: number
  module?: string
  success?: number
}

export interface SecurityOverviewVO {
  maxFailures: number
  lockMinutes: number
  lockedAccountCount: number
  todayFailedLoginCount: number
  lockedAccounts: string[]
}
