import request from '@/utils/request'
import type { CategoryVO, CategoryCreateDTO } from '@/types/product'

export function getCategoryTree(): Promise<CategoryVO[]> {
  return request.get<CategoryVO[]>('/categories')
}

export function createCategory(data: CategoryCreateDTO): Promise<void> {
  return request.post<void>('/categories', data)
}
