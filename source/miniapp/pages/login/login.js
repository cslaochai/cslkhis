import { authApi } from '../../utils/api'
import { saveLogin } from '../../utils/auth'
import { encryptPassword } from '../../utils/password'

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
      const plain = password.trim()
      // 口令不出手机：提交前先用后端公钥做 SM2 加密。
      // 后端若换过密钥会报「密文解析失败」，这时丢掉缓存公钥重拉一次再试。
      let res = await authApi.login({ username: username.trim(), password: await encryptPassword(plain) })
      if (res && res.code !== 200 && String(res.message || '').indexOf('密文') >= 0) {
        res = await authApi.login({ username: username.trim(), password: await encryptPassword(plain, true) })
      }
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
