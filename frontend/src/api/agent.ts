import request from '@/utils/request'
import type { AgentTaskVO } from '@/types/intelligence'

export function createAgentTask(userPrompt: string): Promise<AgentTaskVO> {
  return request.post<AgentTaskVO>('/agent/task', { userPrompt })
}

export function getAgentTask(taskId: number): Promise<AgentTaskVO> {
  return request.get<AgentTaskVO>(`/agent/task/${taskId}`)
}

export function addAgentResultToCart(taskId: number, skuIds: number[]): Promise<void> {
  return request.post<void>(`/agent/task/${taskId}/add-to-cart`, { skuIds })
}
