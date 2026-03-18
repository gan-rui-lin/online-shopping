export interface MerchantShopVO {
  shopId: number
  userId: number
  shopName: string
  shopLogo: string
  shopDesc: string
  shopStatus: number
  score: number
}

export interface MerchantApplyVO {
  id: number
  userId: number
  username: string
  shopName: string
  businessLicenseNo: string
  contactName: string
  contactPhone: string
  applyStatus: number
  remark: string
  createTime: string
}

export interface MerchantApplyDTO {
  shopName: string
  businessLicenseNo?: string
  contactName?: string
  contactPhone?: string
}

export interface MerchantAuditDTO {
  auditStatus: number
  remark?: string
}

export interface ShopUpdateDTO {
  shopName: string
  shopLogo?: string
  shopDesc?: string
}

export interface ShopStatisticVO {
  shopId: number
  shopName: string
  totalProducts: number
  onShelfProducts: number
  totalOrders: number
  pendingOrders: number
  totalRevenue: number
  score: number
}
