import type { PageQuery } from './common'

export interface ProductSimpleVO {
  spuId: number
  title: string
  subTitle: string
  mainImage: string
  minPrice: number
  maxPrice: number
  salesCount: number
  status: number
  auditStatus: number
  shopName: string
}

export interface ProductDetailVO {
  spuId: number
  title: string
  subTitle: string
  brandName: string
  mainImage: string
  detailText: string
  status: number
  shopId: number
  shopName: string
  minPrice: number
  maxPrice: number
  salesCount: number
  favoriteCount: number
  imageList: string[]
  skuList: ProductSkuVO[]
}

export interface ProductSkuVO {
  skuId: number
  skuCode: string
  skuName: string
  specJson: string
  salePrice: number
  originPrice: number
  stock: number
  imageUrl: string
}

export interface CategoryVO {
  id: number
  parentId: number
  categoryName: string
  level: number
  children: CategoryVO[]
}

export interface ProductSearchDTO extends PageQuery {
  keyword?: string
  categoryId?: number
  brandName?: string
  minPrice?: number
  maxPrice?: number
  sortField?: string
  sortOrder?: string
}

export interface ProductSkuDTO {
  skuId?: number
  skuCode?: string
  skuName?: string
  specJson?: string
  price: number
  originPrice?: number
  stock: number
  imageUrl?: string
}

export interface ProductSpuCreateDTO {
  categoryId: number
  brandName?: string
  title: string
  subTitle?: string
  mainImage?: string
  detailText?: string
  skuList?: ProductSkuDTO[]
  imageList?: string[]
}

export interface ProductSpuUpdateDTO {
  categoryId?: number
  brandName?: string
  title: string
  subTitle?: string
  mainImage?: string
  detailText?: string
  imageList?: string[]
  skuList?: ProductSkuDTO[]
}

export interface CategoryCreateDTO {
  parentId?: number
  categoryName: string
  sortOrder?: number
}
