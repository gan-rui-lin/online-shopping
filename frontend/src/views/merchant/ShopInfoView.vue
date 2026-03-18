<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCurrentShop, updateShop } from '@/api/merchant'
import type { MerchantShopVO, ShopUpdateDTO } from '@/types/merchant'

const shop = ref<MerchantShopVO | null>(null)
const formRef = ref<FormInstance>()
const loading = ref(true)
const saving = ref(false)

const form = reactive<ShopUpdateDTO>({
  shopName: '',
  shopLogo: '',
  shopDesc: '',
})

const rules: FormRules = {
  shopName: [{ required: true, message: 'Shop name is required', trigger: 'blur' }],
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
    ElMessage.success('Shop info updated')
    fetchShop()
  } catch { /* handled */ } finally {
    saving.value = false
  }
}

onMounted(fetchShop)
</script>

<template>
  <div v-loading="loading" class="shop-info-page">
    <h2 class="page-title mb-24">Shop Information</h2>

    <div class="card-box" style="max-width: 600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Shop ID">
          <el-input :model-value="String(shop?.shopId || '')" disabled />
        </el-form-item>
        <el-form-item label="Shop Status">
          <el-tag :type="shop?.shopStatus === 1 ? 'success' : 'warning'">
            {{ shop?.shopStatus === 1 ? 'Active' : 'Inactive' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="Shop Name" prop="shopName">
          <el-input v-model="form.shopName" />
        </el-form-item>
        <el-form-item label="Shop Logo URL">
          <el-input v-model="form.shopLogo" placeholder="Enter logo URL" />
          <el-image v-if="form.shopLogo" :src="form.shopLogo" fit="contain" class="logo-preview" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="form.shopDesc" type="textarea" :rows="4" placeholder="Describe your shop" />
        </el-form-item>
        <el-form-item label="Rating">
          <el-rate :model-value="shop?.score || 0" disabled show-score />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">Save Changes</el-button>
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
