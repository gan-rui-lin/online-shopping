<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword } from '@/api/user'
import type { UpdateProfileDTO, ChangePasswordDTO } from '@/types/user'

const userStore = useUserStore()
const profileRef = ref<FormInstance>()
const passwordRef = ref<FormInstance>()
const profileLoading = ref(false)
const passwordLoading = ref(false)
const activeTab = ref('profile')
const { t } = useI18n()

const profileForm = reactive<UpdateProfileDTO>({
  nickname: '',
  email: '',
  phone: '',
  avatarUrl: '',
})

const passwordForm = reactive<ChangePasswordDTO & { confirmPassword: string }>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules: FormRules = {
  nickname: [{ max: 64, message: t('buyer.max64'), trigger: 'blur' }],
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: t('buyer.required'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('buyer.required'), trigger: 'blur' },
    { min: 6, max: 64, message: t('buyer.len6to64'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t('buyer.required'), trigger: 'blur' },
    {
      validator: (_r: any, v: string, cb: any) => {
        v !== passwordForm.newPassword ? cb(new Error(t('buyer.passwordMismatch'))) : cb()
      },
      trigger: 'blur',
    },
  ],
}

async function handleUpdateProfile() {
  const valid = await profileRef.value?.validate().catch(() => false)
  if (!valid) return
  profileLoading.value = true
  try {
    await updateProfile(profileForm)
    await userStore.fetchUserInfo()
    ElMessage.success(t('buyer.profileUpdated'))
  } catch { /* handled */ } finally {
    profileLoading.value = false
  }
}

async function handleChangePassword() {
  const valid = await passwordRef.value?.validate().catch(() => false)
  if (!valid) return
  passwordLoading.value = true
  try {
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success(t('buyer.passwordChanged'))
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch { /* handled */ } finally {
    passwordLoading.value = false
  }
}

onMounted(async () => {
  try {
    await userStore.fetchUserInfo()
  } catch { /* handled */ }
  const info = userStore.userInfo
  if (info) {
    profileForm.nickname = info.nickname || ''
    profileForm.email = info.email || ''
    profileForm.phone = info.phone || ''
    profileForm.avatarUrl = info.avatarUrl || ''
  }
})
</script>

<template>
  <div class="profile-page">
    <h2 class="page-title mb-24">{{ t('buyer.myProfile') }}</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane :label="t('buyer.profileInfo')" name="profile">
        <div class="card-box">
          <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-width="120px" style="max-width: 500px">
            <el-form-item :label="t('buyer.username')">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item :label="t('buyer.nickname')" prop="nickname">
              <el-input v-model="profileForm.nickname" :placeholder="t('buyer.nickname')" />
            </el-form-item>
            <el-form-item :label="t('buyer.email')" prop="email">
              <el-input v-model="profileForm.email" :placeholder="t('buyer.email')" />
            </el-form-item>
            <el-form-item :label="t('buyer.phone')" prop="phone">
              <el-input v-model="profileForm.phone" :placeholder="t('buyer.phone')" />
            </el-form-item>
            <el-form-item :label="t('buyer.avatarUrl')" prop="avatarUrl">
              <el-input v-model="profileForm.avatarUrl" :placeholder="t('buyer.avatarUrl')" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">{{ t('buyer.saveChanges') }}</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('buyer.changePassword')" name="password">
        <div class="card-box">
          <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="160px" style="max-width: 500px">
            <el-form-item :label="t('buyer.currentPassword')" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('buyer.newPassword')" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('buyer.confirmPassword')" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">{{ t('buyer.changePasswordBtn') }}</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}
</style>
