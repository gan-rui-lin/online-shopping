import request from '@/utils/request'
import type { CartVO, CartAddDTO, CartUpdateDTO } from '@/types/cart'

export function getCartList(): Promise<CartVO> {
  return request.get<CartVO>('/cart/list')
}

export function addToCart(data: CartAddDTO): Promise<void> {
  return request.post<void>('/cart/add', data)
}

export function updateCartItem(data: CartUpdateDTO): Promise<void> {
  return request.put<void>('/cart/update', data)
}

export function removeCartItem(skuId: number): Promise<void> {
  return request.delete<void>(`/cart/item/${skuId}`)
}
