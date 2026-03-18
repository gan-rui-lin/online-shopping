export const OrderStatus = {
  UNPAID: 0,
  TO_SHIP: 1,
  TO_RECEIVE: 2,
  COMPLETED: 3,
  CANCELLED: 4,
  REFUNDING: 5,
  REFUNDED: 6,
} as const

export const OrderStatusMap: Record<number, string> = {
  [OrderStatus.UNPAID]: 'Unpaid',
  [OrderStatus.TO_SHIP]: 'To Ship',
  [OrderStatus.TO_RECEIVE]: 'To Receive',
  [OrderStatus.COMPLETED]: 'Completed',
  [OrderStatus.CANCELLED]: 'Cancelled',
  [OrderStatus.REFUNDING]: 'Refunding',
  [OrderStatus.REFUNDED]: 'Refunded',
}

export const PayStatus = {
  UNPAID: 0,
  PAID: 1,
  REFUNDED: 2,
} as const

export const PayStatusMap: Record<number, string> = {
  [PayStatus.UNPAID]: 'Unpaid',
  [PayStatus.PAID]: 'Paid',
  [PayStatus.REFUNDED]: 'Refunded',
}

export const ProductStatus = {
  DRAFT: 0,
  ON_SHELF: 1,
  OFF_SHELF: 2,
} as const

export const ProductStatusMap: Record<number, string> = {
  [ProductStatus.DRAFT]: 'Draft',
  [ProductStatus.ON_SHELF]: 'On Shelf',
  [ProductStatus.OFF_SHELF]: 'Off Shelf',
}

export const UserType = {
  BUYER: 1,
  MERCHANT: 2,
  ADMIN: 3,
} as const

export const UserTypeMap: Record<number, string> = {
  [UserType.BUYER]: 'Buyer',
  [UserType.MERCHANT]: 'Merchant',
  [UserType.ADMIN]: 'Admin',
}

export const MerchantApplyStatus = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
} as const

export const MerchantApplyStatusMap: Record<number, string> = {
  [MerchantApplyStatus.PENDING]: 'Pending',
  [MerchantApplyStatus.APPROVED]: 'Approved',
  [MerchantApplyStatus.REJECTED]: 'Rejected',
}
