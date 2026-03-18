<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getAddressList, createAddress, updateAddress, deleteAddress } from '@/api/address'
import type { AddressVO, AddressCreateDTO, AddressUpdateDTO } from '@/types/address'

const addresses = ref<AddressVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('Add Address')
const formRef = ref<FormInstance>()
const saving = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: 0,
  tagName: '',
})

const rules: FormRules = {
  receiverName: [{ required: true, message: 'Required', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: 'Required', trigger: 'blur' }],
  province: [{ required: true, message: 'Required', trigger: 'blur' }],
  city: [{ required: true, message: 'Required', trigger: 'blur' }],
  district: [{ required: true, message: 'Required', trigger: 'blur' }],
  detailAddress: [{ required: true, message: 'Required', trigger: 'blur' }],
}

async function fetchAddresses() {
  loading.value = true
  try {
    addresses.value = await getAddressList()
  } catch {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  dialogTitle.value = 'Add Address'
  Object.assign(form, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', postalCode: '', isDefault: 0, tagName: '' })
  dialogVisible.value = true
}

function openEdit(addr: AddressVO) {
  editingId.value = addr.id
  dialogTitle.value = 'Edit Address'
  Object.assign(form, {
    receiverName: addr.receiverName,
    receiverPhone: addr.receiverPhone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detailAddress: addr.detailAddress,
    postalCode: '',
    isDefault: addr.isDefault,
    tagName: addr.tagName || '',
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateAddress({ id: editingId.value, ...form } as AddressUpdateDTO)
      ElMessage.success('Address updated')
    } else {
      await createAddress(form as AddressCreateDTO)
      ElMessage.success('Address created')
    }
    dialogVisible.value = false
    fetchAddresses()
  } catch { /* handled */ } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('Delete this address?', 'Confirm')
  try {
    await deleteAddress(id)
    ElMessage.success('Address deleted')
    fetchAddresses()
  } catch { /* handled */ }
}

async function handleSetDefault(addr: AddressVO) {
  try {
    await updateAddress({ id: addr.id, isDefault: 1 })
    ElMessage.success('Default address set')
    fetchAddresses()
  } catch { /* handled */ }
}

onMounted(fetchAddresses)
</script>

<template>
  <div class="address-page">
    <div class="page-header mb-16">
      <h2 class="page-title">My Addresses</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> Add Address
      </el-button>
    </div>

    <div v-loading="loading">
      <div v-for="addr in addresses" :key="addr.id" class="address-card card-box mb-16">
        <div class="address-info">
          <div class="address-top">
            <span class="receiver-name">{{ addr.receiverName }}</span>
            <span class="receiver-phone">{{ addr.receiverPhone }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="primary">Default</el-tag>
            <el-tag v-if="addr.tagName" size="small" type="info">{{ addr.tagName }}</el-tag>
          </div>
          <p class="full-address">{{ addr.fullAddress || `${addr.province} ${addr.city} ${addr.district} ${addr.detailAddress}` }}</p>
        </div>
        <div class="address-actions">
          <el-button text type="primary" @click="openEdit(addr)">Edit</el-button>
          <el-button v-if="addr.isDefault !== 1" text type="primary" @click="handleSetDefault(addr)">Set Default</el-button>
          <el-button text type="danger" @click="handleDelete(addr.id)">Delete</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && !addresses.length" description="No addresses yet" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Name" prop="receiverName">
          <el-input v-model="form.receiverName" />
        </el-form-item>
        <el-form-item label="Phone" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" />
        </el-form-item>
        <el-form-item label="Province" prop="province">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="City" prop="city">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="District" prop="district">
          <el-input v-model="form.district" />
        </el-form-item>
        <el-form-item label="Detail Address" prop="detailAddress">
          <el-input v-model="form.detailAddress" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="Tag">
          <el-input v-model="form.tagName" placeholder="e.g. Home, Office" />
        </el-form-item>
        <el-form-item label="Default">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.receiver-name {
  font-weight: 500;
}

.receiver-phone {
  color: $text-secondary;
}

.full-address {
  color: $text-regular;
  font-size: $font-size-sm;
}

.address-actions {
  flex-shrink: 0;
}
</style>
