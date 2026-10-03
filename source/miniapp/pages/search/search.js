import { deptApi, doctorApi } from '../../utils/api'

const APPOINTMENT_TAB = '/pages/appointment/appointment'
const LIMIT = 20

Page({
  data: {
    keyword: '',
    depts: [],
    doctors: [],
    searched: false,
    loading: false
  },

  onLoad() {
    this.seq = 0
  },

  onUnload() {
    clearTimeout(this.timer)
  },

  onKeywordInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword })
    clearTimeout(this.timer)
    const trimmed = keyword.trim()
    if (!trimmed) {
      this.setData({ depts: [], doctors: [], searched: false })
      return
    }
    this.timer = setTimeout(() => this.search(trimmed), 300)
  },

  clearKeyword() {
    clearTimeout(this.timer)
    this.setData({ keyword: '', depts: [], doctors: [], searched: false })
  },

  async search(keyword) {
    const seq = ++this.seq
    this.setData({ loading: true })
    try {
      const [deptRes, doctorRes] = await Promise.all([
        deptApi.selectList({ deptName: keyword }),
        doctorApi.selectList({ empName: keyword, empType: 1 })
      ])
      if (seq !== this.seq) return
      const depts = (deptRes.code === 200 ? deptRes.data || [] : []).slice(0, LIMIT)
      const doctors = (doctorRes.code === 200 ? doctorRes.data || [] : []).slice(0, LIMIT)
      this.setData({ depts, doctors, searched: true })
    } catch (err) {
      if (seq !== this.seq) return
      wx.showToast({ title: err.message || '搜索失败', icon: 'none' })
    } finally {
      if (seq === this.seq) this.setData({ loading: false })
    }
  },

  onDeptTap(e) {
    const dept = e.currentTarget.dataset.dept
    this.gotoAppointment({ deptId: dept.id, deptName: dept.deptName })
  },

  onDoctorTap(e) {
    const doctor = e.currentTarget.dataset.doctor
    if (!doctor.deptId) {
      wx.showToast({ title: '该医生未绑定出诊科室', icon: 'none' })
      return
    }
    this.gotoAppointment({
      deptId: doctor.deptId,
      deptName: doctor.deptName,
      doctorId: doctor.id,
      doctorName: doctor.empName
    })
  },

  // appointment 是 tabBar 页，switchTab 带不了参数，只能落一次性的交接数据
  gotoAppointment(target) {
    wx.setStorageSync('appoint_target', target)
    wx.switchTab({ url: APPOINTMENT_TAB })
  },

  goChat() {
    wx.navigateTo({ url: '/pages/chat/chat' })
  },

  onCancel() {
    wx.navigateBack()
  }
})
