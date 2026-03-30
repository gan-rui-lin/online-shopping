<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { ProductSimpleVO } from '@/types/product'
import PriceDisplay from './PriceDisplay.vue'

const props = defineProps<{
  product: ProductSimpleVO
}>()

const router = useRouter()

const displayPrice = computed(() => {
  if (props.product.minPrice === props.product.maxPrice) {
    return props.product.minPrice
  }
  return props.product.minPrice
})

function goDetail() {
  router.push(`/products/${props.product.spuId}`)
}
</script>

<template>
  <div class="product-card" @click="goDetail">
    <div class="product-image">
      <el-image :src="product.mainImage" fit="cover" lazy>
        <template #error>
          <div class="image-placeholder">
            <el-icon :size="32"><Picture /></el-icon>
          </div>
        </template>
      </el-image>
    </div>
    <div class="product-info">
      <h3 class="product-title text-ellipsis">{{ product.title }}</h3>
      <p v-if="product.subTitle" class="product-subtitle text-ellipsis">{{ product.subTitle }}</p>
      <div class="product-meta">
        <PriceDisplay :price="displayPrice" />
        <span class="sales-count">{{ product.salesCount }} sold</span>
      </div>
      <div v-if="product.shopName" class="shop-name text-ellipsis">
        <el-icon :size="12"><Shop /></el-icon>
        {{ product.shopName }}
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.product-card {
  background: $bg-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  box-shadow: $shadow-sm;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
}

.product-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;

  .el-image {
    width: 100%;
    height: 100%;
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $bg-color;
    color: $text-placeholder;
  }
}

.product-info {
  padding: 12px;
}

.product-title {
  font-size: $font-size-base;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 4px;
}

.product-subtitle {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 4px;
}

.sales-count {
  font-size: $font-size-sm;
  color: $text-placeholder;
}

.shop-name {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
