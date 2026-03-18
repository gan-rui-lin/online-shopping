import type { PageQuery } from './common'

export interface ReviewVO {
  reviewId: number
  userId: number
  nickname: string
  avatarUrl: string
  anonymousFlag: number
  score: number
  content: string
  imageUrls: string[]
  skuName: string
  replyContent: string
  replyTime: string
  createTime: string
}

export interface ReviewStatisticVO {
  spuId: number
  totalCount: number
  goodCount: number
  mediumCount: number
  badCount: number
  goodRate: number
}

export interface ReviewCreateDTO {
  orderItemId: number
  score: number
  content?: string
  imageUrls?: string[]
  anonymousFlag?: number
}

export interface ReviewReplyDTO {
  replyContent: string
}

export interface ReviewQueryDTO extends PageQuery {
  spuId?: number
  score?: number
}
