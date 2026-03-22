import request from '@/utils/request'
import type { BrowseHistoryPage } from '@/types/intelligence'

export function getBrowseHistory(pageNum = 1, pageSize = 10): Promise<BrowseHistoryPage> {
  return request.get<BrowseHistoryPage>('/behavior/history', { pageNum, pageSize })
}

export function clearBrowseHistory(): Promise<void> {
  return request.delete<void>('/behavior/history')
}
