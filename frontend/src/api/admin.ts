import request from '@/utils/request'
import type { DashboardVO } from '@/types/admin'
import type { PageResult } from '@/types/common'
import type { ProductSimpleVO } from '@/types/product'
import type { MerchantApplyVO } from '@/types/merchant'

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
