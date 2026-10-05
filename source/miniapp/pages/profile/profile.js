import { getUser, getPatientId, logout } from '../../utils/auth'
import { patientApi, authApi } from '../../utils/api'
import { bindOpenidSilently } from '../../utils/wechat'
import { genderText } from '../../utils/gender'

Page({
  data: {
    user: null,
    patient: null,
    // 只保留首页/底部 tab 没有入口的功能；其余（预约/报告/缴费/叫号/处方/消息/就诊人）都从首页进
    menus: [
      { id: 'emr', name: '门诊病历', icon: '/images/icons/clipboard.svg', page: '/pages/emr/emr' },
      { id: 'chronic', name: '慢病档案', icon: '/images/icons/shield.svg', page: '/pages/chronic/chronic' }
    ]
  },

  onShow() {
    const user = getUser()
    this.setData({ user })
    this.loadPatient()
    // 订阅消息口子：登录后静默绑 openid（已绑会被唯一键拒绝，静默忽略）
    bindOpenidSilently()
  },

  async loadPatient() {
    const patientId = getPatientId()
    if (!patientId) return
    try {
      const res = await patientApi.getById(patientId)
      if (res.code === 200 && res.data) {
        const p = res.data
        this.setData({
          patient: {
            patientName: p.patientName,
            patientNo: p.patientNo,
            genderText: genderText(p.gender),
            phone: p.phone,
            idCardMasked: p.idCard ? p.idCard.replace(/^(.{6}).*(.{4})$/, '$1********$2') : '',
            balance: p.balance != null ? p.balance.toFixed(2) : '0.00',
            visitCount: p.visitCount || 0
          }
        })
      }
    } catch (e) {
      console.error('加载患者档案失败', e)
    }
  },

  onMenuTap(e) {
    const menu = e.currentTarget.dataset.menu
    wx.navigateTo({ url: menu.page })
  },

  async onLogout() {
    const res = await wx.showModal({ title: '退出登录', content: '确定要退出当前账号吗？' })
    if (!res.confirm) return
    try {
      await authApi.logout()
    } catch (e) { /* 忽略 */ }
    logout()
    wx.reLaunch({ url: '/pages/login/login' })
  }
})
