export interface CartItemVO {
  skuId: number
  spuId: number
  spuName: string
  skuName: string
  mainImage: string
  specJson: string
  price: number
  quantity: number
  checked: number
  stock: number
  available: boolean
}

export interface CartVO {
  items: CartItemVO[]
  totalCount: number
  checkedCount: number
  totalAmount: number
  checkedAmount: number
}

export interface CartAddDTO {
  skuId: number
  quantity?: number
}

export interface CartUpdateDTO {
  skuId: number
  quantity?: number
  checked?: number
}
