import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { queueApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'
import { askSubscribe } from '../../utils/wechat'

Page({
  data: {
    today: '',
    patientName: '',
    queues: [],
    loading: false,
    checking: false
  },

  onShow() {
    const t = new Date()
    const today = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`
    const current = getCurrentPatient()
    this.setData({ today, patientName: current ? current.patientName : '' })
    this.loadQueue()
  },

  async loadQueue() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await queueApi.myQueue(patientId)
      if (res.code === 200) {
        const queues = (res.data || []).map(q => ({
          ...q,
          key: q.queueId || q.registId,
          step: this.toStep(q),
          slotText: q.slotStart ? `${q.slotStart}${q.slotEnd ? ' - ' + q.slotEnd : ''}` : '',
          aheadText: this.aheadText(q),
          // 后端只放行「就诊日=今天」的签到，跨日给按钮只会换来一次报错
          canCheckIn: !q.checkedIn && q.visitDate === this.data.today
        }))
        this.setData({ queues })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载排队信息失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 进度步骤以队列状态为准（后端 QueueStatusEnum）：
   * 无队列行=待签到(1)；2候诊中(2)；3就诊中(3)；4已就诊(5完成)；6已过号停在(2)；5/7 不再展示进度
   */
  toStep(q) {
    if (!q.checkedIn) return 1
    const map = { 2: 2, 3: 3, 4: 4, 6: 2 }
    return map[q.queueStatus] || 1
  },

  aheadText(q) {
    if (!q.checkedIn) {
      return q.visitDate === this.data.today
        ? '尚未到院签到，签到后才进入候诊队列'
        : `该号就诊日为 ${q.visitDate}，未到就诊日不能签到`
    }
    if (q.queueStatus === 2) {
      const called = q.currentCalledNo != null ? `，当前叫到 ${q.currentCalledNo} 号` : ''
      return `您排在第 ${q.sequenceNo != null ? q.sequenceNo : '—'} 号，前方还有 ${q.aheadCount} 人候诊${called}`
    }
    if (q.queueStatus === 3) return '轮到您了，请前往诊室就诊'
    return ''
  },

  async onCheckIn(e) {
    const registId = e.currentTarget.dataset.registId
    if (!registId || this.data.checking) return
    const conf = await wx.showModal({ title: '到院签到', content: '确认您已到医院，现在加入候诊队列？' })
    if (!conf.confirm) return
    this.setData({ checking: true })
    try {
      const res = await queueApi.myCheckIn(registId)
      if (res.code === 200) {
        wx.showToast({ title: '签到成功', icon: 'success' })
        askSubscribe('queue_called')
        this.loadQueue()
      } else {
        // 到院补缴口径：未缴费的号被签到校验挡下时，把院内文案翻译成患者可执行的指引
        const msg = res.message || ''
        const unpaid = /缴费|支付|charge/i.test(msg)
        wx.showToast({ title: unpaid ? '挂号费尚未缴纳，请到院收费窗口补缴后再签到' : (msg || '签到失败'), icon: 'none' })
      }
    } catch (err) {
      console.error('签到失败', err)
      wx.showToast({ title: '签到失败', icon: 'none' })
    } finally {
      this.setData({ checking: false })
    }
  },

  goPrevisit(e) {
    const registId = e.currentTarget.dataset.registId
    if (!registId) return
    wx.navigateTo({ url: `/pages/previsit/previsit?registId=${registId}` })
  },

  goAppointment() {
    wx.switchTab({ url: '/pages/appointment/appointment' })
  }
})
