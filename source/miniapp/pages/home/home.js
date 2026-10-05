import { getUser, getPatientId, getCurrentPatient } from '../../utils/auth'
import { appointApi, hospitalApi } from '../../utils/api'
import { ensurePatient } from '../../utils/patientSync'

/** 不在 tabBar 里的页面：只能 navigateTo 进入 */
const NON_TAB_PAGES = ['/pages/chat/chat', '/pages/triage/triage', '/pages/followup/followup']

// slot_start/slot_end 是 char(5) 的 HH:mm 快照，不是 datetime，不能按日期串 slice
function slotText(start, end) {
  if (!start) return ''
  return end ? `${start} - ${end}` : start
}

Page({
  data: {
    user: null,
    currentPatient: null,
    greeting: '',
    quickEntries: [
      { id: 1, name: '预约挂号', icon: '/images/icons/appointment.svg', bg: '#e8f4fd', page: '/pages/appointment/appointment' },
      { id: 5, name: '复诊预约', icon: '/images/icons/clipboard.svg', bg: '#e0f2f1', page: '/pages/revisit/revisit' },
      { id: 2, name: '门诊缴费', icon: '/images/icons/payment.svg', bg: '#e8f5e9', page: '/pages/payment/payment' },
      { id: 3, name: '报告查询', icon: '/images/icons/report.svg', bg: '#fef3e2', page: '/pages/report/report' },
      { id: 4, name: '住院服务', icon: '/images/icons/bed.svg', bg: '#fce4ec', page: '/pages/deposit/deposit' }
    ],
    services: [
      { id: 1, name: '智能导诊', icon: '/images/icons/robot.svg', bg: '#e8f4fd', page: '/pages/triage/triage' },
      { id: 2, name: '排队叫号', icon: '/images/icons/clock.svg', bg: '#fef3e2', page: '/pages/queue/queue' },
      { id: 3, name: '检查报告', icon: '/images/icons/filetext.svg', bg: '#e8f5e9', page: '/pages/report/report' },
      { id: 4, name: '处方查询', icon: '/images/icons/pill.svg', bg: '#fce4ec', page: '/pages/prescription/prescription' },
      { id: 5, name: '在线客服', icon: '/images/icons/sms.svg', bg: '#f3e5f5', page: '/pages/chat/chat' },
      { id: 6, name: '费用明细', icon: '/images/icons/money.svg', bg: '#e0f2f1', page: '/pages/payment/payment' },
      { id: 7, name: '我的随访', icon: '/images/icons/clipboard.svg', bg: '#e8f5e9', page: '/pages/followup/followup' }
    ],
    nextAppointment: null,
    hospitalInfo: wx.getStorageSync('hospitalInfo') || {},
    bannerTop: 48,
    loading: false
  },

  onLoad() {
    const user = getUser()
    this.setData({ user, greeting: this.getGreeting(), bannerTop: this.bannerInsetTop() })
    this.loadHospitalInfo()
  },

  // 灵动岛机型 env(safe-area-inset-top) 在 WXSS 的 padding 简写里不解析，顶部只剩兜底值，
  // 医院名会被岛压住一截；改为按系统信息换算成 rpx 内联下发。48 是原设计留白。
  bannerInsetTop() {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const top = Math.max(info.statusBarHeight || 0, info.safeArea ? info.safeArea.top : 0)
    return Math.round(top * 750 / (info.windowWidth || 375)) + 48
  },

  async onShow() {
    // 登录会清本地指针防串号，这里按后端 isDefault 回填，否则重登后当前就诊人就空了
    await ensurePatient()
    this.setData({ currentPatient: this.formatPatient(getCurrentPatient()) })
    this.loadNextAppointment()
  },

  // WXML 里不能调方法，展示字段一次算好；VO 的 idCard/phone 服务端已打码，这里只做拼接
  formatPatient(p) {
    if (!p) return null
    const genderText = p.gender === 1 ? '男' : p.gender === 2 ? '女' : ''
    const descText = [
      genderText,
      p.age != null ? p.age + '岁' : '',
      p.idCard ? '证件尾号 ' + String(p.idCard).slice(-4) : ''
    ].filter(Boolean).join(' · ')
    return { ...p, initial: (p.patientName || '').charAt(0), descText }
  },

  goPatients() {
    wx.navigateTo({ url: '/pages/patients/patients' })
  },

  async loadHospitalInfo() {
    try {
      const res = await hospitalApi.info()
      if (res.code === 200 && res.data) {
        this.setData({ hospitalInfo: res.data })
        wx.setStorageSync('hospitalInfo', res.data)
      }
    } catch (e) {
      console.error('加载医院信息失败', e)
    }
  },

  onCallHospital() {
    const phone = (this.data.hospitalInfo.hospitalPhone || '').replace(/[^0-9]/g, '')
    if (!phone) {
      wx.showToast({ title: '暂无联系电话', icon: 'none' })
      return
    }
    wx.makePhoneCall({ phoneNumber: phone })
  },

  onCopyAddress() {
    const address = this.data.hospitalInfo.hospitalAddress
    if (!address) return
    wx.setClipboardData({ data: address })
  },

  getGreeting() {
    const h = new Date().getHours()
    if (h < 6) return '夜深了'
    if (h < 12) return '早上好'
    if (h < 14) return '中午好'
    if (h < 18) return '下午好'
    return '晚上好'
  },

  onNotify() {
    wx.showToast({ title: '暂无新通知', icon: 'none' })
  },

  onEntryTap(e) {
    const entry = e.currentTarget.dataset.entry
    if (entry.page) {
      wx.switchTab({ url: entry.page }).catch(() => wx.navigateTo({ url: entry.page }))
    } else {
      wx.showToast({ title: `${entry.name}开发中`, icon: 'none' })
    }
  },

  onServiceTap(e) {
    const service = e.currentTarget.dataset.service
    if (service.page) {
      // 非 tabBar 页只能用 navigateTo，先试 switchTab 会白失败一次（被 catch 兜住但没必要）
      if (NON_TAB_PAGES.indexOf(service.page) >= 0) {
        wx.navigateTo({ url: service.page })
      } else {
        wx.switchTab({ url: service.page }).catch(() => wx.navigateTo({ url: service.page }))
      }
    } else {
      wx.showToast({ title: `${service.name}开发中`, icon: 'none' })
    }
  },

  async loadNextAppointment() {
    const patientId = getPatientId()
    if (!patientId) return
    this.setData({ loading: true })
    try {
      const res = await appointApi.listPage({ patientId, pageNum: 1, pageSize: 5 })
      if (res.code === 200 && res.data && res.data.records) {
        const list = res.data.records
        // 取最近一条未退号的挂号
        const upcoming = list.find(a => a.registStatus !== 5) || list[0]
        if (upcoming) {
          this.setData({
            nextAppointment: {
              id: upcoming.id,
              dept: upcoming.deptName,
              doctor: upcoming.doctorName,
              date: upcoming.visitDate,
              time: slotText(upcoming.slotStart, upcoming.slotEnd),
              status: this.statusText(upcoming.registStatus),
              statusClass: this.statusClass(upcoming.registStatus)
            }
          })
        } else {
          this.setData({ nextAppointment: null })
        }
      }
    } catch (e) {
      console.error('加载预约失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },

  statusText(status) {
    const map = { 1: '已挂号', 2: '已签到', 3: '就诊中', 4: '已就诊', 5: '已退号', 6: '已过号' }
    return map[status] || '未知'
  },

  statusClass(status) {
    return status === 5 || status === 6 ? 'cancelled' : 'confirmed'
  },

  goAppointmentList() {
    wx.navigateTo({ url: '/pages/appointmentList/appointmentList' })
  }
})
