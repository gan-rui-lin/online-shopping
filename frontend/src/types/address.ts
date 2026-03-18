export interface AddressVO {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  fullAddress: string
  isDefault: number
  tagName: string
}

export interface AddressCreateDTO {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  postalCode?: string
  isDefault?: number
  tagName?: string
}

export interface AddressUpdateDTO {
  id: number
  receiverName?: string
  receiverPhone?: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  postalCode?: string
  isDefault?: number
  tagName?: string
}
