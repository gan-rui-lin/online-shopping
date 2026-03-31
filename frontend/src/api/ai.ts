import request from '@/utils/request'
import type { CopywritingResultVO, ProductEvaluationVO, ReviewSummaryVO } from '@/types/intelligence'

export function generateTitle(spuId: number, locale?: string): Promise<CopywritingResultVO> {
  return request.post<CopywritingResultVO>(`/ai/copywriting/title/${spuId}`, undefined, { params: { locale } })
}

export function generateDescription(data: {
  keywords?: string
  targetAudience?: string
  style?: string
  locale?: string
}): Promise<CopywritingResultVO> {
  return request.post<CopywritingResultVO>('/ai/copywriting/description', data)
}

export function generateSellingPoints(spuId: number, locale?: string): Promise<CopywritingResultVO> {
  return request.post<CopywritingResultVO>(
    `/ai/copywriting/selling-points/${spuId}`,
    undefined,
    { params: { locale } },
  )
}

export function getReviewSummary(spuId: number, locale?: string): Promise<ReviewSummaryVO> {
  return request.get<ReviewSummaryVO>(`/ai/review-summary/${spuId}`, { locale })
}

export function getProductEvaluation(spuId: number, locale?: string): Promise<ProductEvaluationVO> {
  return request.get<ProductEvaluationVO>(`/ai/copywriting/evaluate/${spuId}`, { locale })
}
