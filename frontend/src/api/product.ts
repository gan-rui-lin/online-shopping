import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  ProductSimpleVO,
  ProductDetailVO,
  ProductSearchDTO,
  ProductSpuCreateDTO,
  ProductSpuUpdateDTO,
} from '@/types/product'

export function searchProducts(params: ProductSearchDTO): Promise<PageResult<ProductSimpleVO>> {
  return request.get<PageResult<ProductSimpleVO>>('/products', params)
}

export function getProductDetail(spuId: number): Promise<ProductDetailVO> {
  return request.get<ProductDetailVO>(`/products/${spuId}`)
}

export function createProduct(data: ProductSpuCreateDTO): Promise<void> {
  return request.post<void>('/products', data)
}

export function updateProduct(spuId: number, data: ProductSpuUpdateDTO): Promise<void> {
  return request.put<void>(`/products/${spuId}`, data)
}

export function onShelfProduct(spuId: number): Promise<void> {
  return request.put<void>(`/products/${spuId}/on-shelf`)
}

export function offShelfProduct(spuId: number): Promise<void> {
  return request.put<void>(`/products/${spuId}/off-shelf`)
}

export function deleteProduct(spuId: number): Promise<void> {
  return request.delete<void>(`/products/${spuId}`)
}

export function getMyProducts(pageNum = 1, pageSize = 10): Promise<PageResult<ProductSimpleVO>> {
  return request.get<PageResult<ProductSimpleVO>>('/products/my', { pageNum, pageSize })
}
