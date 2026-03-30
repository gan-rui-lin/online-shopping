import { ref, reactive, watch } from 'vue'
import type { PageResult } from '@/types/common'

interface UsePaginationOptions<T> {
  fetchFn: (params: Record<string, any>) => Promise<PageResult<T>>
  defaultPageSize?: number
  immediate?: boolean
}

export function usePagination<T>(options: UsePaginationOptions<T>) {
  const { fetchFn, defaultPageSize = 10, immediate = true } = options

  const list = ref<T[]>([]) as { value: T[] }
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(defaultPageSize)
  const loading = ref(false)
  const extraParams = reactive<Record<string, any>>({})

  async function fetch() {
    loading.value = true
    try {
      const res = await fetchFn({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        ...extraParams,
      })
      list.value = res.list
      total.value = res.total
    } catch (error) {
      console.error(error)
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  function handlePageChange(page: number) {
    pageNum.value = page
    fetch()
  }

  function handleSizeChange(size: number) {
    pageSize.value = size
    pageNum.value = 1
    fetch()
  }

  function reset() {
    pageNum.value = 1
    Object.keys(extraParams).forEach((key) => delete extraParams[key])
    fetch()
  }

  function search(params: Record<string, any>) {
    pageNum.value = 1
    Object.assign(extraParams, params)
    fetch()
  }

  if (immediate) {
    fetch()
  }

  return {
    list,
    total,
    pageNum,
    pageSize,
    loading,
    extraParams,
    fetch,
    handlePageChange,
    handleSizeChange,
    reset,
    search,
  }
}
