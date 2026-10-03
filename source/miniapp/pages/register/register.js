import { patientApi, smsApi } from '../../utils/api'

const RESEND_SECONDS = 60

Page({
  data: {
    patientName: '',
    idCard: '',
    phone: '',
    gender: 1, // 1-男 2-女
    smsCode: '',
    password: '',
    confirmPassword: '',
    showPassword: false,
    loading: false,
    sendingCode: false,
    countdown: 0,
    errorMsg: ''
  },

  onUnload() {
    this.clearTimer()
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value, errorMsg: '' })
  },

  selectGender(e) {
    this.setData({ gender: Number(e.currentTarget.dataset.gender) })
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  // ===== 短信验证码 =====

  async sendCode() {
    if (this.data.countdown > 0 || this.data.sendingCode) return
    const phone = this.data.phone.trim()
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      this.setData({ errorMsg: '请先填写正确的手机号' })
      return
    }
    this.setData({ sendingCode: true, errorMsg: '' })
    try {
      const res = await smsApi.sendCode({ phone })
      if (res.code !== 200) {
        this.setData({ errorMsg: res.message || '验证码发送失败' })
        return
      }
      // 后端 mock 模式回显验证码，直接填上，省得去翻服务端日志
      const mockCode = res.data && res.data.mockCode
      if (mockCode) this.setData({ smsCode: mockCode })
      wx.showToast({ title: mockCode ? '验证码已自动填入' : '验证码已发送', icon: 'none' })
      this.startCountdown()
    } catch (e) {
      this.setData({ errorMsg: e.message || '验证码发送失败' })
    } finally {
      this.setData({ sendingCode: false })
    }
  },

  startCountdown() {
    this.clearTimer()
    this.setData({ countdown: RESEND_SECONDS })
    this.timer = setInterval(() => {
      const next = this.data.countdown - 1
      this.setData({ countdown: next })
      if (next <= 0) this.clearTimer()
    }, 1000)
  },

  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },

  // ===== 提交 =====

  validate() {
    const { patientName, idCard, phone, password, confirmPassword, smsCode } = this.data
    if (!patientName.trim()) return '请输入姓名'
    if (!/^\d{17}[\dXx]$/.test(idCard.trim())) return '请输入正确的身份证号'
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) return '请输入正确的手机号'
    if (!/^\d{4,8}$/.test(smsCode.trim())) return '请输入短信验证码'
    if (password.length < 6) return '密码至少 6 位'
    if (password !== confirmPassword) return '两次输入的密码不一致'
    return ''
  },

  async handleRegister() {
    const err = this.validate()
    if (err) {
      this.setData({ errorMsg: err })
      return
    }
    this.setData({ loading: true, errorMsg: '' })
    try {
      const res = await patientApi.register({
        patientName: this.data.patientName.trim(),
        idCard: this.data.idCard.trim().toUpperCase(),
        phone: this.data.phone.trim(),
        gender: this.data.gender,
        password: this.data.password,
        smsCode: this.data.smsCode.trim()
      })
      if (res.code === 200) {
        this.clearTimer()
        this.setData({ countdown: 0 })
        wx.showToast({ title: '注册成功', icon: 'success' })
        const phone = this.data.phone.trim()
        setTimeout(() => {
          wx.redirectTo({ url: `/pages/login/login?username=${phone}` })
        }, 1200)
      } else {
        this.setData({ errorMsg: res.message || '注册失败' })
      }
    } catch (e) {
      this.setData({ errorMsg: e.message || '网络错误，请重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goLogin() {
    wx.navigateBack()
  }
})
