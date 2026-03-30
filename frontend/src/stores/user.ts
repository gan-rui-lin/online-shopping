import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { getUserInfo as getUserInfoApi } from '@/api/user'
import { getToken, setToken, setTokenHead, removeToken, setItem, getItem, removeItem } from '@/utils/storage'
import type { LoginDTO, LoginVO } from '@/types/auth'
import type { UserInfoVO } from '@/types/user'

const USER_INFO_KEY = 'user_info'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref<UserInfoVO | null>(getItem<UserInfoVO>(USER_INFO_KEY))

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value?.roles || [])
  const userType = computed(() => userInfo.value?.userType)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  const hasRole = (role: string) => roles.value.includes(role)
  const isMerchant = computed(() => hasRole('ROLE_MERCHANT'))
  const isAdmin = computed(() => hasRole('ROLE_ADMIN'))

  async function login(loginData: LoginDTO): Promise<LoginVO> {
    const data = await loginApi(loginData)
    token.value = data.token
    setToken(data.token)
    setTokenHead(data.tokenHead)
    const info: UserInfoVO = {
      id: data.userId,
      username: data.username,
      nickname: data.nickname,
      phone: '',
      email: '',
      avatarUrl: '',
      userType: data.userType,
      roles: data.roles,
    }
    userInfo.value = info
    setItem(USER_INFO_KEY, info)
    return data
  }

  async function fetchUserInfo(): Promise<UserInfoVO> {
    const data = await getUserInfoApi()
    userInfo.value = data
    setItem(USER_INFO_KEY, data)
    return data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // ignore logout API errors
    } finally {
      resetState()
    }
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeItem(USER_INFO_KEY)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roles,
    userType,
    nickname,
    isMerchant,
    isAdmin,
    hasRole,
    login,
    fetchUserInfo,
    logout,
    resetState,
  }
})
