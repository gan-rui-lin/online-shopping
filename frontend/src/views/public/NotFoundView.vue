<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">{{ t('notFound.message') }}</p>
      <el-button type="primary" size="large" @click="router.push('/')">{{ t('notFound.backHome') }}</el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.not-found-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - $header-height - 80px);
  padding: 40px 20px;
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 96px;
  font-weight: 700;
  color: $primary-color;
  line-height: 1;
  margin-bottom: 16px;
}

.error-message {
  font-size: $font-size-lg;
  color: $text-secondary;
  margin-bottom: 32px;
}
</style>
