import request from '@/utils/request'
import type { ShoppingPlanCreateDTO, ShoppingPlanVO } from '@/types/intelligence'

export function createShoppingPlan(data: ShoppingPlanCreateDTO): Promise<void> {
  return request.post<void>('/plan', data)
}

export function getShoppingPlanList(): Promise<ShoppingPlanVO[]> {
  return request.get<ShoppingPlanVO[]>('/plan/list')
}

export function getShoppingPlanDetail(planId: number): Promise<ShoppingPlanVO> {
  return request.get<ShoppingPlanVO>(`/plan/${planId}`)
}

export function cancelShoppingPlan(planId: number): Promise<void> {
  return request.post<void>(`/plan/${planId}/cancel`)
}

export function executeShoppingPlan(planId: number): Promise<void> {
  return request.post<void>(`/plan/${planId}/execute`)
}
