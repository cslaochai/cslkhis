import { getPatientId } from '../../utils/auth'
import { appointApi } from '../../utils/api'

Page({
  data: {
    list: [],
    loading: false
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await appointApi.listPage({ patientId, pageNum: 1, pageSize: 50 })
      if (res.code === 200 && res.data) {
        const list = (res.data.records || []).map(a => ({
          id: a.id,
          registNo: a.registNo,
          deptName: a.deptName,
          doctorName: a.doctorName,
          visitDate: a.visitDate,
          slotStart: a.slotStart ? a.slotStart.slice(11, 16) : '',
          slotEnd: a.slotEnd ? a.slotEnd.slice(11, 16) : '',
          registStatus: a.registStatus,
          statusText: this.statusText(a.registStatus),
          statusClass: this.statusClass(a.registStatus),
          canCancel: a.registStatus === 1 || a.registStatus === 2
        }))
        this.setData({ list })
      }
    } catch (e) {
      console.error('加载预约列表失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  statusText(status) {
    const map = { 1: '已挂号', 2: '已签到', 3: '就诊中', 4: '已就诊', 5: '已退号', 6: '已过号' }
    return map[status] || '未知'
  },

  statusClass(status) {
    if (status === 5 || status === 6) return 'cancelled'
    if (status === 3 || status === 4) return 'done'
    return 'active'
  },

  async onCancel(e) {
    const { id } = e.currentTarget.dataset
    const res = await wx.showModal({ title: '确认退号', content: '退号后需重新挂号，确定吗？' })
    if (!res.confirm) return
    try {
      const r = await appointApi.cancel({ registId: id, reason: '患者主动退号' })
      if (r.code === 200) {
        wx.showToast({ title: '已退号', icon: 'success' })
        this.loadList()
      } else {
        wx.showToast({ title: r.message || '退号失败', icon: 'none' })
      }
    } catch (err) {
      wx.showToast({ title: err.message || '退号失败', icon: 'none' })
    }
  },

  goAppointment() {
    wx.switchTab({ url: '/pages/appointment/appointment' })
  }
})
