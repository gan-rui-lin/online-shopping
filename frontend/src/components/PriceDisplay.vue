<script setup lang="ts">
import { computed } from 'vue'
import { formatPrice } from '@/utils/format'

const props = withDefaults(defineProps<{
  price: number | undefined | null
  originPrice?: number | undefined | null
  size?: 'small' | 'default' | 'large'
}>(), {
  size: 'default',
})

const priceText = computed(() => formatPrice(props.price))
const originText = computed(() => props.originPrice ? formatPrice(props.originPrice) : '')
const hasDiscount = computed(() => props.originPrice && props.originPrice > (props.price || 0))
</script>

<template>
  <span class="price-display" :class="[`price-${size}`]">
    <span class="current-price">{{ priceText }}</span>
    <span v-if="hasDiscount" class="origin-price">{{ originText }}</span>
  </span>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.price-display {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.current-price {
  color: $danger-color;
  font-weight: 600;
}

.origin-price {
  color: $text-placeholder;
  text-decoration: line-through;
  font-size: 0.85em;
}

.price-small .current-price { font-size: $font-size-sm; }
.price-default .current-price { font-size: $font-size-lg; }
.price-large .current-price { font-size: 24px; }
</style>
