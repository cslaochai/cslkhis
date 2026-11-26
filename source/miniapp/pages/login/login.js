import { authApi } from '../../utils/api'
import { saveLogin } from '../../utils/auth'
import { encryptPassword } from '../../utils/password'

/**
 * 测试环境账号预填：页面打开即把账号口令填好；多账号时点标签可切换。
 * 小程序只服务患者端，这里只放患者账号（sys_user.user_type=3，带 patient_id），
 * 院内员工账号登进来是空壳，不放进来。
 * ⚠ 正式发布前把数组清空（TEST_ACCOUNTS = []），页面即回到空白手输状态。
 */
const TEST_ACCOUNTS = [
  { label: '陈周敏', username: '13899000001', password: '123456' }
]

Page({
  data: {
    username: '',
    password: '',
    showPassword: false,
    loading: false,
    errorMsg: '',
    testAccounts: TEST_ACCOUNTS
  },

  onLoad(options) {
    // 注册成功跳回来时带 username（手机号），以它为优先，不覆盖用户刚注册的账号
    if (options && options.username) {
      this.setData({ username: options.username })
      return
    }
    if (TEST_ACCOUNTS.length) {
      this.fillAccount(0)
    }
  },

  /** 点测试账号标签：换账号并同步口令 */
  fillAccount(indexOrEvent) {
    const index = typeof indexOrEvent === 'number' ? indexOrEvent : Number(indexOrEvent.currentTarget.dataset.index)
    const account = TEST_ACCOUNTS[index]
    if (!account) return
    this.setData({ username: account.username, password: account.password, errorMsg: '' })
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
