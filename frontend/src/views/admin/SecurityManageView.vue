<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getSecurityOverview } from '@/api/admin'
import type { SecurityOverviewVO } from '@/types/admin'

const { t } = useI18n()
const loading = ref(false)
const data = ref<SecurityOverviewVO | null>(null)

async function fetchOverview() {
  loading.value = true
  try {
    data.value = await getSecurityOverview()
  } finally {
    loading.value = false
  }
}

onMounted(fetchOverview)
</script>

<template>
  <div v-loading="loading" class="security-page">
    <h2 class="page-title mb-24">{{ t('admin.securityManagement') }}</h2>

    <template v-if="data">
      <div class="security-cards mb-24">
        <div class="security-card">
          <div class="label">{{ t('admin.loginMaxFailures') }}</div>
          <div class="value">{{ data.maxFailures }}</div>
        </div>
        <div class="security-card">
          <div class="label">{{ t('admin.loginLockMinutes') }}</div>
          <div class="value">{{ data.lockMinutes }}</div>
        </div>
        <div class="security-card">
          <div class="label">{{ t('admin.lockedAccounts') }}</div>
          <div class="value">{{ data.lockedAccountCount }}</div>
        </div>
        <div class="security-card">
          <div class="label">{{ t('admin.todayFailedLogins') }}</div>
          <div class="value">{{ data.todayFailedLoginCount }}</div>
        </div>
      </div>

      <div class="card-box">
        <h3 class="section-title mb-16">{{ t('admin.currentLockedUsers') }}</h3>
        <el-empty v-if="!data.lockedAccounts?.length" :description="t('admin.noLockedUsers')" />
        <el-tag
          v-for="username in data.lockedAccounts"
          v-else
          :key="username"
          type="danger"
          class="mr-8 mb-8"
        >
          {{ username }}
        </el-tag>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}

.security-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.security-card {
  background: linear-gradient(135deg, #fff4ec 0%, #ffe8d4 100%);
  border: 1px solid #ffd8b0;
  border-radius: 14px;
  padding: 16px;

  .label {
    font-size: 12px;
    color: #8a5a2f;
  }

  .value {
    margin-top: 8px;
    font-size: 28px;
    font-weight: 700;
    color: #bf5f00;
  }
}

.section-title {
  font-size: 16px;
}

.mr-8 {
  margin-right: 8px;
}

.mb-8 {
  margin-bottom: 8px;
}

@media (max-width: 1280px) {
  .security-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
