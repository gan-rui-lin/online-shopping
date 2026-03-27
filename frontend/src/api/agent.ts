import request from '@/utils/request'
import type { AgentTaskCreateDTO, AgentTaskVO } from '@/types/intelligence'

export function createAgentTask(data: AgentTaskCreateDTO): Promise<AgentTaskVO> {
  return request.post<AgentTaskVO>('/agent/task', data)
}

export function getAgentTask(taskId: string | number): Promise<AgentTaskVO> {
  return request.get<AgentTaskVO>(`/agent/task/${taskId}`)
}

export function addAgentResultToCart(taskId: string | number, skuIds: number[]): Promise<void> {
  return request.post<void>(`/agent/task/${taskId}/add-to-cart`, { skuIds })
}
