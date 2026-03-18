<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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
  nickname: [{ max: 64, message: 'Max 64 characters', trigger: 'blur' }],
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: 'Required', trigger: 'blur' }],
  newPassword: [
    { required: true, message: 'Required', trigger: 'blur' },
    { min: 6, max: 64, message: '6-64 characters', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: 'Required', trigger: 'blur' },
    {
      validator: (_r: any, v: string, cb: any) => {
        v !== passwordForm.newPassword ? cb(new Error('Passwords do not match')) : cb()
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
    ElMessage.success('Profile updated')
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
    ElMessage.success('Password changed successfully')
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
    <h2 class="page-title mb-24">My Profile</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="Profile Info" name="profile">
        <div class="card-box">
          <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-width="120px" style="max-width: 500px">
            <el-form-item label="Username">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="Nickname" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="Enter nickname" />
            </el-form-item>
            <el-form-item label="Email" prop="email">
              <el-input v-model="profileForm.email" placeholder="Enter email" />
            </el-form-item>
            <el-form-item label="Phone" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="Enter phone" />
            </el-form-item>
            <el-form-item label="Avatar URL" prop="avatarUrl">
              <el-input v-model="profileForm.avatarUrl" placeholder="Enter avatar URL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">Save Changes</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Change Password" name="password">
        <div class="card-box">
          <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="160px" style="max-width: 500px">
            <el-form-item label="Current Password" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="New Password" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="Confirm Password" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">Change Password</el-button>
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
