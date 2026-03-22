<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { submitOrder } from '@/api/order'
import { getAddressList } from '@/api/address'
import type { AddressVO } from '@/types/address'
import PriceDisplay from '@/components/PriceDisplay.vue'
import { formatSpec } from '@/utils/format'

const router = useRouter()
const cartStore = useCartStore()
const addresses = ref<AddressVO[]>([])
const selectedAddressId = ref<number | undefined>()
const submitting = ref(false)
const showCheckout = ref(false)
const orderRemark = ref('')
const { t } = useI18n()

const checkedItems = computed(() => cartStore.items.filter((i) => i.checked === 1))

async function handleCheck(skuId: number, checked: number) {
  try {
    await cartStore.updateItem({ skuId, checked })
  } catch { /* handled */ }
}

async function handleQuantityChange(skuId: number, quantity: number) {
  try {
    await cartStore.updateItem({ skuId, quantity })
  } catch { /* handled */ }
}

async function handleRemove(skuId: number) {
  await ElMessageBox.confirm(t('buyer.removeItemConfirm'), t('buyer.confirm'))
  try {
    await cartStore.removeItem(skuId)
    ElMessage.success(t('buyer.removed'))
  } catch { /* handled */ }
}

async function openCheckout() {
  if (!checkedItems.value.length) {
    ElMessage.warning(t('buyer.selectItemsWarning'))
    return
  }
  try {
    addresses.value = await getAddressList()
    const defaultAddr = addresses.value.find((a) => a.isDefault === 1)
    selectedAddressId.value = defaultAddr?.id || addresses.value[0]?.id
  } catch {
    addresses.value = []
  }
  showCheckout.value = true
}

async function handleSubmitOrder() {
  if (!selectedAddressId.value) {
    ElMessage.warning(t('buyer.selectAddressWarning'))
    return
  }
  submitting.value = true
  try {
    const result = await submitOrder({
      addressId: selectedAddressId.value,
      remark: orderRemark.value || undefined,
      cartSkuIds: checkedItems.value.map((i) => i.skuId),
    })
    ElMessage.success(t('buyer.orderSubmitted'))
    showCheckout.value = false
    await cartStore.fetchCart()
    router.push(`/buyer/orders/${result.orderNo}`)
  } catch { /* handled */ } finally {
    submitting.value = false
  }
}

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<template>
  <div class="cart-page">
    <h2 class="page-title mb-24">{{ t('buyer.shoppingCart') }}</h2>

    <div v-loading="cartStore.loading">
      <div v-if="cartStore.items.length" class="cart-list">
        <div class="cart-header">
          <span class="col-check">{{ t('buyer.select') }}</span>
          <span class="col-product">{{ t('intelligence.product') }}</span>
          <span class="col-price">{{ t('intelligence.price') }}</span>
          <span class="col-quantity">{{ t('buyer.quantity') }}</span>
          <span class="col-total">{{ t('buyer.total') }}</span>
          <span class="col-action">{{ t('buyer.action') }}</span>
        </div>

        <div v-for="item in cartStore.items" :key="item.skuId" class="cart-item card-box mb-16">
          <div class="col-check">
            <el-checkbox
              :model-value="item.checked === 1"
              :disabled="!item.available"
              @change="(val: any) => handleCheck(item.skuId, val ? 1 : 0)"
            />
          </div>
          <div class="col-product">
            <el-image :src="item.mainImage" fit="cover" class="item-image">
              <template #error>
                <div class="img-fallback"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="item-info">
              <router-link :to="`/products/${item.spuId}`" class="item-name">{{ item.spuName }}</router-link>
              <span class="item-spec">{{ item.skuName || formatSpec(item.specJson) }}</span>
              <el-tag v-if="!item.available" type="danger" size="small">{{ t('buyer.unavailable') }}</el-tag>
            </div>
          </div>
          <div class="col-price">
            <PriceDisplay :price="item.price" size="small" />
          </div>
          <div class="col-quantity">
            <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" @change="(val: any) => handleQuantityChange(item.skuId, val)" />
          </div>
          <div class="col-total">
            <PriceDisplay :price="item.price * item.quantity" />
          </div>
          <div class="col-action">
            <el-button text type="danger" @click="handleRemove(item.skuId)">{{ t('merchant.delete') }}</el-button>
          </div>
        </div>
      </div>

      <el-empty v-else :description="t('buyer.cartEmpty')">
        <router-link to="/products">
          <el-button type="primary">{{ t('buyer.goShopping') }}</el-button>
        </router-link>
      </el-empty>
    </div>

    <div v-if="cartStore.items.length" class="cart-footer card-box mt-24">
      <div class="footer-left">
        <span>{{ t('buyer.selectedItems', { count: cartStore.checkedCount }) }}</span>
      </div>
      <div class="footer-right">
        <span class="total-label">{{ t('buyer.total') }}:</span>
        <PriceDisplay :price="cartStore.checkedAmount" size="large" />
        <el-button type="primary" size="large" :disabled="!checkedItems.length" @click="openCheckout">
          {{ t('buyer.checkout') }}
        </el-button>
      </div>
    </div>

    <el-dialog v-model="showCheckout" :title="t('buyer.confirmOrder')" width="600px">
      <div class="checkout-section">
        <h4 class="mb-16">{{ t('buyer.shippingAddress') }}</h4>
        <el-radio-group v-model="selectedAddressId" class="address-radio-group">
          <el-radio v-for="addr in addresses" :key="addr.id" :value="addr.id" class="address-radio">
            <span class="addr-name">{{ addr.receiverName }} {{ addr.receiverPhone }}</span>
            <span class="addr-detail">{{ addr.fullAddress || `${addr.province} ${addr.city} ${addr.district} ${addr.detailAddress}` }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small">{{ t('buyer.setDefault') }}</el-tag>
          </el-radio>
        </el-radio-group>
        <el-empty v-if="!addresses.length" :description="t('buyer.noAddressFirst')">
          <el-button type="primary" @click="router.push('/buyer/addresses')">{{ t('buyer.addAddressBtn') }}</el-button>
        </el-empty>

        <h4 class="mt-24 mb-16">{{ t('buyer.orderItems') }}</h4>
        <div v-for="item in checkedItems" :key="item.skuId" class="checkout-item">
          <span>{{ item.spuName }} - {{ item.skuName || formatSpec(item.specJson) }}</span>
          <span>x{{ item.quantity }}</span>
          <PriceDisplay :price="item.price * item.quantity" size="small" />
        </div>

        <el-form-item :label="t('buyer.remark')" class="mt-16">
          <el-input v-model="orderRemark" :placeholder="t('buyer.optionalRemark')" />
        </el-form-item>
      </div>
      <template #footer>
        <div class="checkout-footer">
          <span>{{ t('buyer.total') }}: </span>
          <PriceDisplay :price="cartStore.checkedAmount" size="large" />
          <el-button @click="showCheckout = false">{{ t('buyer.cancel') }}</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmitOrder">{{ t('buyer.submitOrder') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: $bg-color;
  border-radius: $border-radius;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 16px;
}

.col-check { width: 50px; flex-shrink: 0; }
.col-product { flex: 1; display: flex; gap: 12px; align-items: center; min-width: 0; }
.col-price { width: 100px; text-align: center; }
.col-quantity { width: 140px; text-align: center; }
.col-total { width: 100px; text-align: center; }
.col-action { width: 80px; text-align: center; }

.item-image {
  width: 80px;
  height: 80px;
  border-radius: $border-radius;
  flex-shrink: 0;
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-color;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.item-name {
  font-weight: 500;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  &:hover { color: $primary-color; text-decoration: none; }
}

.item-spec {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  position: sticky;
  bottom: 0;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.total-label {
  font-size: $font-size-base;
  color: $text-regular;
}

.address-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-radio {
  display: flex;
  align-items: flex-start;
  gap: 8px;

  .addr-name { font-weight: 500; }
  .addr-detail { color: $text-secondary; font-size: $font-size-sm; }
}

.checkout-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid $border-lighter;
  font-size: $font-size-sm;
}

.checkout-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
</style>
