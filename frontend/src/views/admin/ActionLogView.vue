<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAdminActionLogs } from '@/api/admin'
import type { AdminActionLogVO, AdminActionLogQueryDTO } from '@/types/admin'
import { formatDate } from '@/utils/format'

const { t } = useI18n()
const loading = ref(false)
const list = ref<AdminActionLogVO[]>([])
const total = ref(0)

const query = reactive<AdminActionLogQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  operatorId: undefined,
  module: undefined,
  success: undefined,
})

const moduleOptions = [
  { value: 'MEMBER', label: 'MEMBER' },
  { value: 'ORDER_INTERVENTION', label: 'ORDER_INTERVENTION' },
  { value: 'PRODUCT_AUDIT', label: 'PRODUCT_AUDIT' },
]

async function fetchLogs() {
  loading.value = true
  try {
    const res = await getAdminActionLogs(query)
    list.value = res.list
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchLogs()
}

function handleReset() {
  query.operatorId = undefined
  query.module = undefined
  query.success = undefined
  query.pageNum = 1
  fetchLogs()
}

function handlePageChange(page: number) {
  query.pageNum = page
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<template>
  <div class="action-log-page">
    <h2 class="page-title mb-24">{{ t('admin.actionLogs') }}</h2>

    <div class="card-box mb-16">
      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-input-number v-model="query.operatorId" :min="1" :placeholder="t('admin.operatorId')" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.module" :placeholder="t('admin.logModule')" clearable style="width: 200px">
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.success" :placeholder="t('admin.logResult')" clearable style="width: 120px">
            <el-option :label="t('admin.success')" :value="1" />
            <el-option :label="t('admin.failed')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-box">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column :label="t('admin.operator')" width="160">
          <template #default="{ row }">{{ row.operatorName }} (#{{ row.operatorId }})</template>
        </el-table-column>
        <el-table-column prop="module" :label="t('admin.logModule')" width="180" />
        <el-table-column prop="action" :label="t('admin.logAction')" width="160" />
        <el-table-column :label="t('admin.logTarget')" width="170">
          <template #default="{ row }">{{ row.targetType }} {{ row.targetId }}</template>
        </el-table-column>
        <el-table-column prop="detail" :label="t('admin.logDetail')" min-width="240" show-overflow-tooltip />
        <el-table-column :label="t('admin.logResult')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
              {{ row.success === 1 ? t('admin.success') : t('admin.failed') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.logTime')" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.pageNum"
        :total="total"
        :page-size="query.pageSize"
        layout="total, prev, pager, next"
        class="mt-16"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
