import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { examApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'

Page({
  data: {
    tab: 'pending',
    patientName: '',
    pending: [],
    mine: [],
    loading: false,
    // 选时段浮层：apply 为当前申请单，slots 为可选时段
    apply: null,
    slots: [],
    slotsLoading: false,
    booking: false
  },

  onShow() {
    this.setData({ patientName: getCurrentPatient() ? getCurrentPatient().patientName : '' })
    this.load()
  },

  async load() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    await restoreCurrentPatient()
    this.setData({ loading: true })
    try {
      const [pendingRes, mineRes] = await Promise.all([
        examApi.pending(getPatientId() || patientId),
        examApi.myAppointments(getPatientId() || patientId)
      ])
      if (pendingRes.code !== 200) {
        wx.showToast({ title: pendingRes.message || '加载失败', icon: 'none' })
        return
      }
      this.setData({
        pending: (pendingRes.data && pendingRes.data.records) || [],
        mine: (mineRes.code === 200 && mineRes.data && mineRes.data.records) || []
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    this.setData({ tab: e.currentTarget.dataset.tab })
  },

  async onChoose(e) {
    const apply = e.currentTarget.dataset.apply
    if (Number(apply.deviceCount) === 0) {
      wx.showToast({ title: '该项目暂无可预约设备，请联系医技科室', icon: 'none' })
      return
    }
    this.setData({ apply, slots: [], slotsLoading: true })
    try {
      const res = await examApi.slots({ patientId: getPatientId(), applyId: apply.applyId })
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '时段加载失败', icon: 'none' })
        return
      }
      this.setData({ slots: res.data || [] })
      if (!res.data || res.data.length === 0) {
        wx.showToast({ title: '暂无可约时段，请稍后再试', icon: 'none' })
      }
    } finally {
      this.setData({ slotsLoading: false })
    }
  },

  closeSheet() {
    this.setData({ apply: null, slots: [] })
  },

  async onBook(e) {
    const slot = e.currentTarget.dataset.slot
    const { apply } = this.data
    if (!apply || this.data.booking) return
    const res = await new Promise((resolve) => {
      wx.showModal({
        title: '确认预约',
        content: `${apply.itemName || '检查'}\n${slot.examDate} ${slot.startTime}-${slot.endTime}\n${slot.deviceName || ''}`,
        success: resolve
      })
    })
    if (!res.confirm) return
    this.setData({ booking: true })
    try {
      const result = await examApi.book({
        patientId: getPatientId(),
        applyId: apply.applyId,
        deviceId: slot.deviceId,
        examDate: slot.examDate,
        startTime: slot.startTime
      })
      if (result.code !== 200) {
        wx.showToast({ title: result.message || '预约失败', icon: 'none' })
        return
      }
      wx.showToast({ title: '预约成功', icon: 'success' })
      this.closeSheet()
      await this.load()
      this.setData({ tab: 'mine' })
    } catch (err) {
      wx.showToast({ title: err.message || '预约失败', icon: 'none' })
    } finally {
      this.setData({ booking: false })
    }
  },

  async onCancel(e) {
    const appt = e.currentTarget.dataset.appt
    const res = await new Promise((resolve) => {
      wx.showModal({ title: '取消预约', content: `确认取消 ${appt.itemName || ''} 的检查预约？`, success: resolve })
    })
    if (!res.confirm) return
    const result = await examApi.cancel({
      patientId: getPatientId(),
      apptId: appt.apptId,
      reason: '患者自助取消'
    })
    if (result.code !== 200) {
      wx.showToast({ title: result.message || '取消失败', icon: 'none' })
      return
    }
    wx.showToast({ title: '已取消', icon: 'success' })
    await this.load()
  }
})
