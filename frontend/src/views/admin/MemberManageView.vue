<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsers, updateAdminUserStatus } from '@/api/admin'
import type { AdminUserVO, AdminUserQueryDTO } from '@/types/admin'
import { UserType } from '@/constants/enums'
import { formatDate } from '@/utils/format'

const { t } = useI18n()
const list = ref<AdminUserVO[]>([])
const total = ref(0)
const loading = ref(false)

const query = reactive<AdminUserQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: undefined,
  userType: undefined,
})

const userTypeOptions = [
  { label: 'Buyer', value: UserType.BUYER },
  { label: 'Merchant', value: UserType.MERCHANT },
  { label: 'Admin', value: UserType.ADMIN },
]

const statusOptions = [
  { label: 'Active', value: 1 },
  { label: 'Disabled', value: 0 },
]

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getAdminUsers({ ...query, keyword: query.keyword?.trim() || undefined })
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
  fetchUsers()
}

function handleReset() {
  query.keyword = ''
  query.status = undefined
  query.userType = undefined
  query.pageNum = 1
  fetchUsers()
}

function handlePageChange(page: number) {
  query.pageNum = page
  fetchUsers()
}

async function handleStatusSwitch(row: AdminUserVO, value: number | string | boolean) {
  const status = Number(value)
  const oldStatus = status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      status === 1 ? t('admin.confirmEnableUser') : t('admin.confirmDisableUser'),
      t('buyer.confirm'),
    )
    await updateAdminUserStatus(row.id, status)
    ElMessage.success(t('admin.userStatusUpdated'))
  } catch {
    row.status = oldStatus
  }
}

function getUserTypeLabel(userType: number) {
  const target = userTypeOptions.find(item => item.value === userType)
  return target?.label || '-'
}

onMounted(fetchUsers)
</script>

<template>
  <div class="member-manage-page">
    <h2 class="page-title mb-24">{{ t('admin.memberManagement') }}</h2>

    <div class="card-box mb-16">
      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-input
            v-model="query.keyword"
            :placeholder="t('admin.memberKeywordPlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.userType" :placeholder="t('admin.memberType')" clearable style="width: 140px">
            <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.status" :placeholder="t('merchant.status')" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column prop="username" :label="t('admin.user')" width="140" />
        <el-table-column prop="nickname" :label="t('buyer.nickname')" width="140" />
        <el-table-column prop="phone" :label="t('buyer.phone')" width="140" />
        <el-table-column prop="email" label="Email" min-width="180" />
        <el-table-column :label="t('admin.memberType')" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getUserTypeLabel(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.roleCodes')" min-width="180">
          <template #default="{ row }">
            <span>{{ row.roles?.join(', ') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('merchant.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? 'Active' : 'Disabled' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.lastLogin')" width="180">
          <template #default="{ row }">{{ row.lastLoginTime ? formatDate(row.lastLoginTime) : '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('buyer.created')" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('buyer.action')" width="120" fixed="right">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="ON"
              inactive-text="OFF"
              @change="(v) => handleStatusSwitch(row, v)"
            />
          </template>
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
