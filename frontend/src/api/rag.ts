import request from '@/utils/request'
import type { ChatMessageVO, RagAnswerVO } from '@/types/intelligence'

export function askRag(data: { spuIds?: number[]; spuId?: number; question: string; sessionId?: number; locale?: string }): Promise<RagAnswerVO> {
  return request.post<RagAnswerVO>('/rag/ask', data)
}

export function getRagHistory(sessionId: number): Promise<ChatMessageVO[]> {
  return request.get<ChatMessageVO[]>(`/rag/session/${sessionId}/history`)
}

export function importRagKnowledge(spuId: number): Promise<void> {
  return request.post<void>(`/rag/knowledge/import/${spuId}`)
}
