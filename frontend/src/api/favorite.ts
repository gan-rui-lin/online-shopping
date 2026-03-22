import request from '@/utils/request'
import type { FavoritePage } from '@/types/intelligence'

export function toggleFavorite(spuId: number): Promise<boolean> {
  return request.post<boolean>('/favorite/toggle', { spuId })
}

export function getFavoriteList(pageNum = 1, pageSize = 10): Promise<FavoritePage> {
  return request.get<FavoritePage>('/favorite/list', { pageNum, pageSize })
}

export function checkFavorite(spuId: number): Promise<boolean> {
  return request.get<boolean>(`/favorite/check/${spuId}`)
}
