import { ref } from 'vue'

export function useLoading(initialState = false) {
  const loading = ref(initialState)

  async function run<T>(fn: () => Promise<T>): Promise<T | undefined> {
    loading.value = true
    try {
      return await fn()
    } catch (error) {
      console.error(error)
      return undefined
    } finally {
      loading.value = false
    }
  }

  return { loading, run }
}
