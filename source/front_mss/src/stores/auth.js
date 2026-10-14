// 登录态 store：token + 当前用户 + 可见菜单树 + 权限码集合
import { defineStore } from 'pinia'
import { loginApi, userInfoApi, menuTreeApi, logoutApi } from '@/api/auth'
import { setPermList } from '@/lib/perm'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: (() => {
      try {
        return JSON.parse(localStorage.getItem('userInfo') || 'null')
      } catch {
        return null
      }
    })(),
    menus: [],
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    realName: (s) => s.userInfo?.realName || s.userInfo?.username || '',
    roleName: (s) => s.userInfo?.roleName || '',
    deptName: (s) => s.userInfo?.deptName || '',
  },
  actions: {
    async login(username, password) {
      const res = await loginApi({ username, password })
      this.token = res.data.token
      localStorage.setItem('token', this.token)
      await this.fetchInfoAndMenus()
      return res
    },
    async fetchInfoAndMenus() {
      const [infoRes, menuRes] = await Promise.all([userInfoApi(), menuTreeApi()])
      this.userInfo = infoRes.data
      this.menus = menuRes.data || []
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      // 权限码集合喂给 v-perm（异步到达，指令订阅集合变化自动收敛）
      setPermList(this.userInfo?.permissions || [])
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 登出接口失败不阻塞本地清理
      }
      this.reset()
    },
    reset() {
      this.token = ''
      this.userInfo = null
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
