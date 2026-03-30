import type { PageQuery } from './common'

export interface OrderItemVO {
  spuId: number
  skuId: number
  productTitle: string
  skuName: string
  skuSpecJson: string
  productImage: string
  salePrice: number
  totalAmount: number
  quantity: number
  reviewStatus: number
}

export interface OrderListVO {
  orderNo: string
  shopId: number
  shopName: string
  orderStatus: number
  payAmount: number
  createTime: string
  itemList: OrderItemVO[]
}

export interface OrderDetailVO {
  orderNo: string
  orderStatus: number
  payStatus: number
  totalAmount: number
  discountAmount: number
  freightAmount: number
  payAmount: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  createTime: string
  payTime: string
  deliveryTime: string
  finishTime: string
  cancelTime: string
  cancelReason: string
  itemList: OrderItemVO[]
}

export interface OrderSubmitDTO {
  addressId: number
  remark?: string
  cartSkuIds: number[]
}

export interface OrderSubmitVO {
  orderNo: string
  totalAmount: number
  discountAmount: number
  freightAmount: number
  payAmount: number
}

export interface OrderQueryDTO extends PageQuery {
  orderStatus?: number
}

export interface RefundRequestDTO {
  reason: string
}

export interface PaymentVO {
  orderNo: string
  payNo: string
  payStatus: number
  amount: number
  payTime: string
}

export interface DeliveryDetailVO {
  orderNo: string
  trackingNo: string
  carrier: string
  status: number
  deliveryTime: string
  estimatedTime: string
  currentLocation: string
}
