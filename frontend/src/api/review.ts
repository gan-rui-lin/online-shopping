import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { ReviewVO, ReviewStatisticVO, ReviewCreateDTO, ReviewReplyDTO, ReviewQueryDTO } from '@/types/review'

export function getProductReviews(spuId: number, params?: ReviewQueryDTO): Promise<PageResult<ReviewVO>> {
  return request.get<PageResult<ReviewVO>>(`/review/product/${spuId}`, { ...params, spuId: undefined })
}

export function getReviewStatistics(spuId: number): Promise<ReviewStatisticVO> {
  return request.get<ReviewStatisticVO>(`/review/product/${spuId}/statistics`)
}

export function createReview(data: ReviewCreateDTO): Promise<void> {
  return request.post<void>('/review', data)
}

export function replyReview(reviewId: number, data: ReviewReplyDTO): Promise<void> {
  return request.post<void>(`/review/${reviewId}/reply`, data)
}
