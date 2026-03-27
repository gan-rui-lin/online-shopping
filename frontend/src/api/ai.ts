import request from '@/utils/request'
import type { CopywritingResultVO, ProductEvaluationVO, ReviewSummaryVO } from '@/types/intelligence'

export function generateTitle(spuId: number): Promise<CopywritingResultVO> {
  return request.post<CopywritingResultVO>(`/ai/copywriting/title/${spuId}`)
}

export function generateDescription(data: {
  keywords?: string
  targetAudience?: string
  style?: string
}): Promise<CopywritingResultVO> {
  return request.post<CopywritingResultVO>('/ai/copywriting/description', data)
}

export function generateSellingPoints(spuId: number): Promise<CopywritingResultVO> {
  return request.post<CopywritingResultVO>(`/ai/copywriting/selling-points/${spuId}`)
}

export function getReviewSummary(spuId: number): Promise<ReviewSummaryVO> {
  return request.get<ReviewSummaryVO>(`/ai/review-summary/${spuId}`)
}

export function getProductEvaluation(spuId: number): Promise<ProductEvaluationVO> {
  return request.get<ProductEvaluationVO>(`/ai/copywriting/evaluate/${spuId}`)
}
