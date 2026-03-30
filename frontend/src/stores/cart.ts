import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addToCart as addApi, updateCartItem, removeCartItem } from '@/api/cart'
import type { CartVO, CartAddDTO, CartUpdateDTO } from '@/types/cart'

export const useCartStore = defineStore('cart', () => {
  const cart = ref<CartVO | null>(null)
  const loading = ref(false)

  const totalCount = computed(() => cart.value?.totalCount || 0)
  const checkedCount = computed(() => cart.value?.checkedCount || 0)
  const checkedAmount = computed(() => cart.value?.checkedAmount || 0)
  const items = computed(() => cart.value?.items || [])

  async function fetchCart() {
    loading.value = true
    try {
      cart.value = await getCartList()
    } catch {
      cart.value = null
    } finally {
      loading.value = false
    }
  }

  async function addItem(data: CartAddDTO) {
    await addApi(data)
    await fetchCart()
  }

  async function updateItem(data: CartUpdateDTO) {
    await updateCartItem(data)
    await fetchCart()
  }

  async function removeItem(skuId: number) {
    await removeCartItem(skuId)
    await fetchCart()
  }

  return { cart, loading, totalCount, checkedCount, checkedAmount, items, fetchCart, addItem, updateItem, removeItem }
})
