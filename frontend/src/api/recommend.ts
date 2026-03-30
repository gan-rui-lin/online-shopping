import request from '@/utils/request'

export interface RecommendProductVO {
  spuId: number
  title: string
  mainImage: string
  minPrice: number
  salesCount: number
  score: number
  reason: string
}

export function getHotProducts(limit = 10): Promise<RecommendProductVO[]> {
  return request.get<RecommendProductVO[]>('/recommend/hot', { limit })
}

export function getSimilarProducts(spuId: number, limit = 6): Promise<RecommendProductVO[]> {
  return request.get<RecommendProductVO[]>(`/recommend/similar/${spuId}`, { limit })
}

export function getPersonalProducts(limit = 10): Promise<RecommendProductVO[]> {
  return request.get<RecommendProductVO[]>('/recommend/personal', { limit })
}
