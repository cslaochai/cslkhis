import { authApi } from '../../utils/api'
import { saveLogin } from '../../utils/auth'

Page({
  data: {
    username: '',
    password: '',
    showPassword: false,
    loading: false,
    errorMsg: ''
  },

  onLoad(options) {
    if (options && options.username) {
      this.setData({ username: options.username })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value, errorMsg: '' })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value, errorMsg: '' })
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  async handleLogin() {
    const { username, password } = this.data
    if (!username.trim()) {
      this.setData({ errorMsg: '请输入用户名' })
      return
    }
    if (!password.trim()) {
      this.setData({ errorMsg: '请输入密码' })
      return
    }

    this.setData({ loading: true, errorMsg: '' })
    try {
      const res = await authApi.login({
        username: username.trim(),
        password: password.trim()
      })
      if (res.code === 200 && res.data) {
        saveLogin(res.data)
        wx.reLaunch({ url: '/pages/home/home' })
      } else {
        this.setData({ errorMsg: res.message || '登录失败' })
      }
    } catch (err) {
      this.setData({ errorMsg: err.message || '网络错误，请重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
