import request from '@/utils/request'
import type {
  MerchantShopVO,
  MerchantApplyVO,
  MerchantApplyDTO,
  MerchantAuditDTO,
  ShopUpdateDTO,
  ShopStatisticVO,
} from '@/types/merchant'

export function applyMerchant(data: MerchantApplyDTO): Promise<void> {
  return request.post<void>('/merchant/apply', data)
}

export function getCurrentShop(): Promise<MerchantShopVO> {
  return request.get<MerchantShopVO>('/merchant/shop/current')
}

export function getApplyList(): Promise<MerchantApplyVO[]> {
  return request.get<MerchantApplyVO[]>('/merchant/apply/list')
}

export function auditMerchantApply(id: number, data: MerchantAuditDTO): Promise<void> {
  return request.post<void>(`/merchant/apply/${id}/audit`, data)
}

export function updateShop(data: ShopUpdateDTO): Promise<void> {
  return request.put<void>('/merchant/shop', data)
}

export function getShopStatistics(): Promise<ShopStatisticVO> {
  return request.get<ShopStatisticVO>('/merchant/shop/statistics')
}
