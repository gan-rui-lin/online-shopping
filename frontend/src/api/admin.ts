import request from '@/utils/request'
import type { DashboardVO } from '@/types/admin'
import type { PageResult } from '@/types/common'
import type { MerchantApplyVO } from '@/types/merchant'

export function getDashboard(): Promise<DashboardVO> {
  return request.get<DashboardVO>('/admin/dashboard')
}

export function approveProduct(spuId: number): Promise<void> {
  return request.post<void>(`/admin/product/${spuId}/approve`)
}

export function rejectProduct(spuId: number): Promise<void> {
  return request.post<void>(`/admin/product/${spuId}/reject`)
}
