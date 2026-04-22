import request from '@/utils/request'
import type {
  DashboardVO,
  AdminUserVO,
  AdminUserQueryDTO,
  AdminOrderVO,
  AdminOrderQueryDTO,
} from '@/types/admin'
import type { PageResult } from '@/types/common'
import type { ProductSimpleVO } from '@/types/product'

export function getDashboard(): Promise<DashboardVO> {
  return request.get<DashboardVO>('/admin/dashboard')
}

export function getPendingProducts(pageNum = 1, pageSize = 10): Promise<PageResult<ProductSimpleVO>> {
  return request.get<PageResult<ProductSimpleVO>>('/admin/products/pending', { pageNum, pageSize })
}

export function approveProduct(spuId: number): Promise<void> {
  return request.post<void>(`/admin/product/${spuId}/approve`)
}

export function rejectProduct(spuId: number): Promise<void> {
  return request.post<void>(`/admin/product/${spuId}/reject`)
}

export function getAdminUsers(params: AdminUserQueryDTO): Promise<PageResult<AdminUserVO>> {
  return request.get<PageResult<AdminUserVO>>('/admin/users', params)
}

export function updateAdminUserStatus(userId: number, status: number): Promise<void> {
  return request.put<void>(`/admin/users/${userId}/status`, null, { params: { status } })
}

export function getAdminOrders(params: AdminOrderQueryDTO): Promise<PageResult<AdminOrderVO>> {
  return request.get<PageResult<AdminOrderVO>>('/admin/orders', params)
}

export function cancelAdminOrder(orderNo: string, reason?: string): Promise<void> {
  return request.post<void>(`/admin/orders/${orderNo}/cancel`, null, { params: { reason } })
}

export function approveAdminRefund(orderNo: string): Promise<void> {
  return request.post<void>(`/admin/orders/${orderNo}/refund/approve`)
}

export function rejectAdminRefund(orderNo: string, reason: string): Promise<void> {
  return request.post<void>(`/admin/orders/${orderNo}/refund/reject`, null, { params: { reason } })
}
