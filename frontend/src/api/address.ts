import request from '@/utils/request'
import type { AddressVO, AddressCreateDTO, AddressUpdateDTO } from '@/types/address'

export function getAddressList(): Promise<AddressVO[]> {
  return request.get<AddressVO[]>('/address/list')
}

export function createAddress(data: AddressCreateDTO): Promise<void> {
  return request.post<void>('/address', data)
}

export function updateAddress(data: AddressUpdateDTO): Promise<void> {
  return request.put<void>('/address', data)
}

export function deleteAddress(id: number): Promise<void> {
  return request.delete<void>(`/address/${id}`)
}
