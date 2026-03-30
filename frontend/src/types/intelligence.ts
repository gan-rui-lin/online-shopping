import type { PageResult } from './common'

export interface CopywritingResultVO {
  content: string
  variants: string[]
}

export interface ReviewSummaryVO {
  spuId: number
  totalReviews: number
  averageScore: number
  pros: string[]
  cons: string[]
  summary: string
}

export interface ProductEvaluationVO {
  spuId: number
  overallLevel: string
  qualityScore: number
  valueScore: number
  scenarioFit: string
  potentialRisks: string[]
  summary: string
}

export interface AgentRecommendationVO {
  spuId: number
  skuId: number
  title: string
  mainImage: string
  price: number
  reason: string
}

export interface AgentTaskVO {
  taskId: string
  taskType: string
  taskStatus: number
  userPrompt: string
  createTime: string
  recommendations: AgentRecommendationVO[]
}

export interface AgentTaskCreateDTO {
  taskType: 'NECESSITY' | 'INTENTION'
  requiredCategoryId?: number
  requiredCategoryName?: string
  frequency?: string
  bindSpuId?: number
  quantity?: number
  intentRequirement?: string
  preference?: string
  budgetLimit?: number
}

export interface RagAnswerVO {
  question: string
  answer: string
  sessionId: number
  referenceDocTitles: string[]
}

export interface ChatMessageVO {
  role: string
  content: string
  createTime: string
}

export interface FavoriteVO {
  spuId: number
  title: string
  mainImage: string
  minPrice: number
  shopName: string
  createTime: string
}

export interface BrowseHistoryVO {
  spuId: number
  title: string
  mainImage: string
  minPrice: number
  browseTime: string
}

export interface ShoppingPlanItemDTO {
  keyword?: string
  categoryId?: number
  expectedPriceMin?: number
  expectedPriceMax?: number
  quantity?: number
}

export interface ShoppingPlanCreateDTO {
  planName: string
  triggerTime?: string
  budgetAmount?: number
  remark?: string
  items: ShoppingPlanItemDTO[]
}

export interface ShoppingPlanItemVO {
  id: number
  keyword: string
  categoryId: number
  expectedPriceMin?: number
  expectedPriceMax?: number
  quantity: number
  matchedSpuId?: number
  matchedProductTitle?: string
}

export interface ShoppingPlanVO {
  planId: number
  planName: string
  triggerTime?: string
  planStatus: number
  budgetAmount?: number
  remark?: string
  createTime: string
  items: ShoppingPlanItemVO[]
}

export type FavoritePage = PageResult<FavoriteVO>
export type BrowseHistoryPage = PageResult<BrowseHistoryVO>
