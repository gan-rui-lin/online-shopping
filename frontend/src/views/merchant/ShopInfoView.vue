<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCurrentShop, updateShop } from '@/api/merchant'
import type { MerchantShopVO, ShopUpdateDTO } from '@/types/merchant'

const shop = ref<MerchantShopVO | null>(null)
const formRef = ref<FormInstance>()
const loading = ref(true)
const saving = ref(false)
const { t } = useI18n()

const form = reactive<ShopUpdateDTO>({
  shopName: '',
  shopLogo: '',
  shopDesc: '',
})

const rules: FormRules = {
  shopName: [{ required: true, message: t('buyer.required'), trigger: 'blur' }],
}

async function fetchShop() {
  loading.value = true
  try {
    shop.value = await getCurrentShop()
    form.shopName = shop.value.shopName || ''
    form.shopLogo = shop.value.shopLogo || ''
    form.shopDesc = shop.value.shopDesc || ''
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateShop(form)
    ElMessage.success(t('merchant.shopUpdated'))
    fetchShop()
  } catch { /* handled */ } finally {
    saving.value = false
  }
}

onMounted(fetchShop)
</script>

<template>
  <div v-loading="loading" class="shop-info-page">
    <h2 class="page-title mb-24">{{ t('merchant.shopInformation') }}</h2>

    <div class="card-box" style="max-width: 600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('merchant.shopId')">
          <el-input :model-value="String(shop?.shopId || '')" disabled />
        </el-form-item>
        <el-form-item :label="t('merchant.shopStatus')">
          <el-tag :type="shop?.shopStatus === 1 ? 'success' : 'warning'">
            {{ shop?.shopStatus === 1 ? t('merchant.active') : t('merchant.inactive') }}
          </el-tag>
        </el-form-item>
        <el-form-item :label="t('merchantLayout.shopInfo')" prop="shopName">
          <el-input v-model="form.shopName" />
        </el-form-item>
        <el-form-item :label="t('merchant.shopLogoUrl')">
          <el-input v-model="form.shopLogo" :placeholder="t('merchant.shopLogoPlaceholder')" />
          <el-image v-if="form.shopLogo" :src="form.shopLogo" fit="contain" class="logo-preview" />
        </el-form-item>
        <el-form-item :label="t('merchant.description')">
          <el-input v-model="form.shopDesc" type="textarea" :rows="4" :placeholder="t('merchant.descriptionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('merchant.rating')">
          <el-rate :model-value="shop?.score || 0" disabled show-score />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">{{ t('buyer.saveChanges') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}

.logo-preview {
  width: 100px;
  height: 100px;
  margin-top: 8px;
  border-radius: 8px;
}
</style>
