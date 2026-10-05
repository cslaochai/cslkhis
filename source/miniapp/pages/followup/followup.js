import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { followupApi } from '../../utils/api'

const TYPE_TEXT = { 1: '复诊提醒', 2: '慢病随访', 3: '用药指导', 4: '术后随访' }
const STATUS_TEXT = { 1: '待随访', 2: '随访中', 3: '已完成', 4: '已取消' }

Page({
  data: {
    patientName: '',
    list: [],
    loading: false,
    replying: false,
    activeTaskId: '',
    replyText: ''
  },

  onShow() {
    const current = getCurrentPatient()
    this.setData({ patientName: current ? current.patientName : '' })
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
      const res = await followupApi.myList(patientId)
      if (res.code === 200) {
        const list = (res.data || []).map(t => ({
          ...t,
          typeText: TYPE_TEXT[t.followupType] || '随访',
          statusText: STATUS_TEXT[t.followupStatus] || '未知',
          statusClass: t.followupStatus === 3 ? 'done' : (t.followupStatus === 4 ? 'cancel' : ''),
          timeText: t.followupTime ? String(t.followupTime).replace('T', ' ') : '',
          replyTimeText: t.patientReplyTime ? String(t.patientReplyTime).replace('T', ' ') : '',
          canReply: t.followupStatus !== 4
        }))
        this.setData({ list })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载随访任务失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  toggleReply(e) {
    const taskId = e.currentTarget.dataset.taskId
    if (this.data.activeTaskId === taskId) {
      this.setData({ activeTaskId: '', replyText: '' })
    } else {
      this.setData({ activeTaskId: taskId, replyText: '' })
    }
  },

  onReplyInput(e) {
    this.setData({ replyText: e.detail.value })
  },

  async submitReply(e) {
    const taskId = e.currentTarget.dataset.taskId
    const replyText = String(this.data.replyText || '').trim()
    if (!replyText) {
      wx.showToast({ title: '请填写反馈内容', icon: 'none' })
      return
    }
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ replying: true })
    try {
      const res = await followupApi.reply({ taskId, patientId, replyText })
      if (res.code === 200) {
        wx.showToast({ title: '反馈已提交', icon: 'success' })
        this.setData({ activeTaskId: '', replyText: '' })
        this.loadList()
      } else {
        wx.showToast({ title: res.message || '提交失败', icon: 'none' })
      }
    } catch (err) {
      console.error('提交随访反馈失败', err)
      wx.showToast({ title: '提交失败', icon: 'none' })
    } finally {
      this.setData({ replying: false })
    }
  }
})
