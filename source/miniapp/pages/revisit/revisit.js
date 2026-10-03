import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { revisitApi, scheduleApi, payApi } from '../../utils/api'
import { askSubscribe } from '../../utils/wechat'

/** 可线上预约：开了预约池且池内有余号（后端 registSource=4 只从预约池扣） */
function apptRemain(s) {
  return (s.appointmentSource || 0) - (s.usedAppointmentSource || 0)
}

function isBookable(s) {
  return s.isAppointment === 1 && apptRemain(s) > 0
}

/** startTime 在不同排班来源下可能是 HH:mm 或 HH:mm:ss，统一截到分 */
function hhmm(t) {
  return t ? String(t).slice(0, 5) : ''
}

function money(v) {
  return Number(v || 0).toFixed(2)
}

/** 距今天数：患者认「3 天前看过」比认日期快 */
function daysAgo(dateText) {
  if (!dateText) return ''
  const then = new Date(String(dateText).replace(/-/g, '/'))
  if (isNaN(then.getTime())) return ''
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const diff = Math.round((now.getTime() - then.getTime()) / 86400000)
  return diff <= 0 ? '今天' : `${diff} 天前`
}

function normalizeSchedule(s) {
  return {
    ...s,
    timeText: `${hhmm(s.startTime)}-${hhmm(s.endTime)}`,
    remain: apptRemain(s),
    feeText: money(Number(s.registFee || 0) + Number(s.diagnosisFee || 0)
      + (s.isExpert === 1 ? Number(s.expertFee || 0) : 0))
  }
}

const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

/** 号源筛选档：同医生 > 同科室 > 全部，命中不到就逐级放宽 */
const SCOPE_DOCTOR = 'doctor'
const SCOPE_DEPT = 'dept'
const SCOPE_ALL = 'all'

Page({
  data: {
    step: 1,
    patientId: null,
    patientName: '',

    records: [],
    recordsLoading: false,
    selectedRecord: null,

    dates: [],
    selectedDate: '',
    loading: false,
    loadError: '',

    allSchedules: [],
    schedules: [],
    scope: SCOPE_ALL,
    scopeCounts: { doctor: 0, dept: 0, all: 0 },
    selectedSchedule: null,

    settlementType: 1,
    preview: null,
    previewLoading: false,

    submitting: false,
    result: null
  },

  onLoad() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先选择就诊人', icon: 'none' })
      setTimeout(() => wx.navigateTo({ url: '/pages/patients/patients' }), 800)
      return
    }
    this.setData(this.buildDates())
    this.bindPatient()
    this.loadRecords(patientId)
    this.loadDay(this.data.selectedDate)
  },

  onShow() {
    this.bindPatient()
    const target = wx.getStorageSync('revisit_target')
    if (!target) return
    wx.removeStorageSync('revisit_target')
    // 病历页「预约复诊」带过来的记录ID；首次进入时列表往往还没加载完，
    // 存成 pending 由 loadRecords 再兑一次，否则会静默停在第一步
    this._pendingTarget = target
    this.applyTarget(target)
  },

  bindPatient() {
    const current = getCurrentPatient()
    this.setData({ patientId: getPatientId(), patientName: current ? current.patientName : '' })
  },

  buildDates() {
    const base = new Date()
    const dates = []
    for (let i = 0; i < 7; i++) {
      const d = new Date(base)
      d.setDate(base.getDate() + i)
      const value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      dates.push({
        value,
        day: String(d.getDate()).padStart(2, '0'),
        label: i === 0 ? '今天' : i === 1 ? '明天' : WEEK[d.getDay()]
      })
    }
    return { dates, selectedDate: dates[0].value }
  },

  // ===== 原病历 =====

  async loadRecords(patientId) {
    this.setData({ recordsLoading: true })
    try {
      const res = await revisitApi.recordSelectList(patientId)
      if (res.code !== 200) throw new Error(res.message || '就诊记录加载失败')
      const records = (res.data || []).map(r => ({ ...r, daysAgo: daysAgo(r.visitDate) }))
      this.setData({ records, recordsLoading: false })
      if (this._pendingTarget && this.applyTarget(this._pendingTarget)) {
        this._pendingTarget = null
      }
    } catch (err) {
      this.setData({ recordsLoading: false, loadError: err.message || '就诊记录加载失败' })
    }
  },

  applyTarget(target) {
    const hit = this.data.records.find(r => String(r.id) === String(target.recordId))
    if (!hit) {
      if (this.data.records.length) {
        wx.showToast({ title: '该次就诊已不可用于复诊', icon: 'none' })
      }
      return false
    }
    this.selectRecord(hit)
    return true
  },

  onRecordTap(e) {
    this.selectRecord(e.currentTarget.dataset.record)
  },

  selectRecord(record) {
    this.setData({ step: 2, selectedRecord: record, selectedSchedule: null, preview: null })
    this.applyScope(this.pickScope(record))
  },

  // ===== 号源 =====

  async loadDay(date) {
    this.setData({ loading: true, loadError: '' })
    try {
      const res = await scheduleApi.availableList({ visitDate: date })
      if (res.code !== 200) throw new Error(res.message || '号源加载失败')
      const all = (res.data || []).filter(isBookable).map(normalizeSchedule)
      this.setData({ allSchedules: all, loading: false })
      this.applyScope(this.data.scope)
    } catch (err) {
      this.setData({ loading: false, loadError: err.message || '号源加载失败', allSchedules: [], schedules: [] })
    }
  },

  /** 默认落在最能命中减免策略的那一档（同医生），患者通常就是回来找原来那位医生复查 */
  pickScope(record) {
    if (!record) return SCOPE_ALL
    if (this.data.allSchedules.some(s => String(s.doctorId) === String(record.doctorId))) return SCOPE_DOCTOR
    if (this.data.allSchedules.some(s => String(s.deptId) === String(record.deptId))) return SCOPE_DEPT
    return SCOPE_ALL
  },

  matchOne(scope, record, s) {
    if (scope === SCOPE_DOCTOR) return String(s.doctorId) === String(record.doctorId)
    if (scope === SCOPE_DEPT) return String(s.deptId) === String(record.deptId)
    return true
  },

  applyScope(scope) {
    const record = this.data.selectedRecord
    const all = this.data.allSchedules
    const counts = {
      doctor: record ? all.filter(s => this.matchOne(SCOPE_DOCTOR, record, s)).length : 0,
      dept: record ? all.filter(s => this.matchOne(SCOPE_DEPT, record, s)).length : 0,
      all: all.length
    }
    const effective = record && counts[scope] > 0 ? scope : (record && counts.dept > 0 ? SCOPE_DEPT : SCOPE_ALL)
    const list = record ? all.filter(s => this.matchOne(effective, record, s)) : all
    this.setData({ scope: effective, scopeCounts: counts, schedules: list })
  },

  onScopeTap(e) {
    const scope = e.currentTarget.dataset.scope
    if (scope === this.data.scope) return
    this.setData({ selectedSchedule: null, preview: null })
    this.applyScope(scope)
  },

  onDateTap(e) {
    const date = e.currentTarget.dataset.date
    if (date === this.data.selectedDate) return
    this.setData({ selectedDate: date, selectedSchedule: null, preview: null })
    this.loadDay(date)
  },

  onRetry() {
    if (this.data.selectedRecord) {
      this.loadRecords(this.data.patientId)
    }
    this.loadDay(this.data.selectedDate)
  },

  onScheduleTap(e) {
    this.setData({ step: 3, selectedSchedule: e.currentTarget.dataset.schedule })
    this.runPreview()
  },

  goStep(e) {
    const step = Number(e.currentTarget.dataset.step)
    if (step >= this.data.step) return
    this.setData({ step, preview: null })
  },

  onSettlementTap(e) {
    this.setData({ settlementType: Number(e.currentTarget.dataset.type) })
  },

  goPatients() {
    wx.navigateTo({ url: '/pages/patients/patients' })
  },

  goMyAppointments() {
    wx.navigateTo({ url: '/pages/appointmentList/appointmentList' })
  },

  goAppointment() {
    wx.switchTab({ url: '/pages/appointment/appointment' }).catch(() => {
      wx.navigateTo({ url: '/pages/appointment/appointment' })
    })
  },

  // ===== 费用预估 =====

  async runPreview() {
    const { patientId, selectedRecord, selectedSchedule } = this.data
    if (!patientId || !selectedRecord || !selectedSchedule) return
    const token = (this._previewToken || 0) + 1
    this._previewToken = token
    this.setData({ previewLoading: true })
    try {
      const res = await revisitApi.feePreview({
        patientId,
        revisitRecordId: selectedRecord.id,
        // 来源不传：后端固定按「3-患者自助」判定，预览与实收同一口径
        scheduleId: selectedSchedule.id
      })
      if (res.code !== 200) throw new Error(res.message || '费用预估失败')
      const p = res.data || {}
      if (this._previewToken !== token) return
      this.setData({
        previewLoading: false,
        preview: {
          ...p,
          registFeeText: money(p.registFee),
          diagnosisFeeText: money(p.diagnosisFee),
          totalFeeText: money(p.totalFee),
          waived: !!p.waived
        }
      })
    } catch (err) {
      if (this._previewToken !== token) return
      this.setData({ previewLoading: false, preview: null })
      wx.showToast({ title: err.message || '费用预估失败', icon: 'none' })
    }
  },

  // ===== 提交 =====

  async onConfirm() {
    const { patientId, selectedRecord, selectedSchedule, settlementType, preview, submitting } = this.data
    if (submitting) return
    if (!patientId) {
      wx.showToast({ title: '请先选择就诊人', icon: 'none' })
      setTimeout(() => wx.navigateTo({ url: '/pages/patients/patients' }), 800)
      return
    }
    if (!selectedRecord || !selectedSchedule) {
      wx.showToast({ title: '请选择原病历与号源', icon: 'none' })
      return
    }
    // 金额没算出来就不提交：预览 0 元、实收 12 元是最伤信任的错法
    if (!preview) {
      wx.showToast({ title: '正在计算费用，请稍候', icon: 'none' })
      this.runPreview()
      return
    }
    this.setData({ submitting: true })
    try {
      const res = await revisitApi.upsert({
        patientId,
        revisitRecordId: selectedRecord.id,
        scheduleId: selectedSchedule.id,
        settlementType
      })
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '预约失败', icon: 'none' })
        this.setData({ submitting: false })
        return
      }
      this.setData({
        submitting: false,
        step: 4,
        // 成功后号源列表会刷新，选中态随之失效，结果页只认提交时的快照
        result: {
          ...(res.data || {}),
          timeText: selectedSchedule.timeText,
          feeText: preview.totalFeeText,
          waived: preview.waived,
          policyName: preview.policyName || '',
          reason: preview.reason || ''
        }
      })
      this.loadDay(this.data.selectedDate)
    } catch (err) {
      this.setData({ submitting: false })
      wx.showToast({ title: err.message || '预约失败', icon: 'none' })
    }
  },

  restart() {
    this.setData({
      step: 1,
      result: null,
      selectedRecord: null,
      selectedSchedule: null,
      preview: null
    })
    this.applyScope(SCOPE_ALL)
  },

  // 复诊挂号费支付（免收的号没有待缴金额，不进这里）
  async payRegFee() {
    const { result } = this.data
    if (!result || !result.id) {
      wx.showToast({ title: '挂号单缺失', icon: 'none' })
      return
    }
    this.setData({ paying: true })
    try {
      const order = await payApi.createOrder({
        bizType: 2,
        bizId: result.id,
        amount: Number(result.feeText || 0)
      })
      if (order.code !== 200 || !order.data) {
        wx.showToast({ title: order.message || '下单失败', icon: 'none' })
        return
      }
      const { payParams, payStatus } = order.data
      if (payParams) {
        await new Promise((resolve, reject) => {
          wx.requestPayment({ ...payParams, success: resolve, fail: (err) => reject(new Error(err.errMsg || '支付取消')) })
        })
      }
      if (payParams || payStatus === 1) {
        askSubscribe('pay_receipt')
        this.setData({ result: { ...result, payStatus: 1 } })
        wx.showToast({ title: '挂号费已缴', icon: 'success' })
      }
    } catch (err) {
      wx.showToast({ title: err.message || '支付失败', icon: 'none' })
    } finally {
      this.setData({ paying: false })
    }
  }
})
