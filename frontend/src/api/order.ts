import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  OrderListVO,
  OrderDetailVO,
  OrderSubmitDTO,
  OrderSubmitVO,
  OrderQueryDTO,
  RefundRequestDTO,
  PaymentVO,
  DeliveryDetailVO,
} from '@/types/order'

export function submitOrder(data: OrderSubmitDTO): Promise<OrderSubmitVO> {
  return request.post<OrderSubmitVO>('/order/submit', data)
}

export function getOrderDetail(orderNo: string): Promise<OrderDetailVO> {
  return request.get<OrderDetailVO>(`/order/${orderNo}`)
}

export function getOrderList(params: OrderQueryDTO): Promise<PageResult<OrderListVO>> {
  return request.get<PageResult<OrderListVO>>('/order/list', params)
}

export function cancelOrder(orderNo: string, reason?: string): Promise<void> {
  return request.post<void>(`/order/${orderNo}/cancel`, null, { params: { reason } })
}

export function payOrder(orderNo: string): Promise<PaymentVO> {
  return request.post<PaymentVO>(`/order/${orderNo}/pay`)
}

export function confirmReceive(orderNo: string): Promise<void> {
  return request.post<void>(`/order/${orderNo}/confirm-receive`)
}

export function deliverOrder(orderNo: string): Promise<void> {
  return request.post<void>(`/order/${orderNo}/deliver`)
}

export function getMerchantOrders(params: OrderQueryDTO): Promise<PageResult<OrderListVO>> {
  return request.get<PageResult<OrderListVO>>('/order/merchant/list', params)
}

export function applyRefund(orderNo: string, data: RefundRequestDTO): Promise<void> {
  return request.post<void>(`/order/${orderNo}/refund/apply`, data)
}

export function approveRefund(orderNo: string): Promise<void> {
  return request.post<void>(`/order/${orderNo}/refund/approve`)
}

export function rejectRefund(orderNo: string, reason: string): Promise<void> {
  return request.post<void>(`/order/${orderNo}/refund/reject`, null, { params: { reason } })
}

export function getDeliveryDetail(orderNo: string): Promise<DeliveryDetailVO> {
  return request.get<DeliveryDetailVO>(`/order/${orderNo}/delivery`)
}
