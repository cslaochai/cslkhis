import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { doctorApi, scheduleApi, appointApi, payApi } from '../../utils/api'
import { askSubscribe } from '../../utils/wechat'

/** 可线上预约：开了预约池且池内有余号（后端 registSource=4 只从预约池扣） */
function apptRemain(s) {
  return (s.appointmentSource || 0) - (s.usedAppointmentSource || 0)
}

function isBookable(s) {
  return s.isAppointment === 1 && apptRemain(s) > 0
}

/** startTime 在不同排班来源下可能是 HH:mm 或 HH:mm:ss，统一截到分 */
// 诊查费职级加收档位，与后端 TitleTierMapping 一致（1-普通 2-副主任 3-主任 4-知名专家，湘医保发〔2025〕58号附件1）
const TIER_TEXT = { 1: '', 2: '副主任医师', 3: '主任医师', 4: '知名专家' }

function hhmm(t) {
  return t ? String(t).slice(0, 5) : ''
}

function fee(s) {
  // 🔑 只读诊查费：湘医保发〔2025〕58号（长沙市医保局同文，2026-01-01 执行）已废止挂号费，
  //   合并为门诊诊查费；知名专家档的加收价已经并进号源 diagnosis_fee（9 + 61 = 70），
  //   expert_fee 是 272 之前遗留的独立加收列，再加一次等于向患者多显示 2 元。
  //   口径必须与后端 AppointServiceImpl.scheduleFeeTotal 一致，否则小程序显示价比窗口实收高。
  const diagnosis = Number(s.diagnosisFee || 0)
  // wxml 里不能调 toFixed，金额一律在 js 侧格式化成文本
  return {
    diagnosis: diagnosis.toFixed(2),
    total: diagnosis.toFixed(2),
    // 档位标签：让患者看到 ¥31.00 时知道这是主任医师档，不是普通号被加价。
    // 只给数字不说明来源，是门诊价格投诉最常见的起点。
    tierText: TIER_TEXT[Number(s.diagnosisTier || 1)] || ''
  }
}

function normalize(s) {
  const f = fee(s)
  return {
    ...s,
    timeText: `${hhmm(s.startTime)}-${hhmm(s.endTime)}`,
    remain: apptRemain(s),
    fee: f,
    feeText: f.total
  }
}

function groupByDept(list) {
  const map = new Map()
  list.forEach(s => {
    if (!map.has(s.deptId)) {
      map.set(s.deptId, { deptId: s.deptId, deptName: s.deptName, doctorIds: new Set(), remain: 0 })
    }
    const g = map.get(s.deptId)
    g.doctorIds.add(s.doctorId)
    g.remain += s.remain
  })
  return [...map.values()]
    .map(g => ({ deptId: g.deptId, deptName: g.deptName, doctorCount: g.doctorIds.size, remain: g.remain }))
    .sort((a, b) => b.remain - a.remain)
}

function groupByDoctor(list) {
  const map = new Map()
  list.forEach(s => {
    if (!map.has(s.doctorId)) {
      map.set(s.doctorId, { doctorId: s.doctorId, doctorName: s.doctorName, schedules: [], remain: 0 })
    }
    const g = map.get(s.doctorId)
    g.schedules.push(s)
    g.remain += s.remain
  })
  return [...map.values()].map(g => {
    const first = g.schedules[0]
    return {
      ...g,
      deptId: first.deptId,
      deptName: first.deptName,
      isExpert: first.isExpert,
      timeText: g.schedules.map(s => s.timeText).join(' / ')
    }
  })
}

const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

Page({
  data: {
    step: 1,
    dates: [],
    selectedDate: '',
    loading: false,
    loadError: '',

    depts: [],
    selectedDept: null,
    doctors: [],
    selectedDoctor: null,

    patientName: '',
    patientId: null,
    // 线上只能抢普通号源，一律按初诊提交；复诊不占号源、要引用原病历，只有院内窗口/医生站能开
    visitType: 1,
    settlementType: 1,
    selectedSchedule: null,

    submitting: false,
    result: null
  },

  onLoad() {
    this.setData(this.buildDates())
    this.bindPatient()
    this.loadDay(this.data.selectedDate)
  },

  onShow() {
    this.bindPatient()
    const target = wx.getStorageSync('appoint_target')
    if (!target) return
    wx.removeStorageSync('appoint_target')
    this.applyTarget(target)
  },

  bindPatient() {
    const current = getCurrentPatient()
    this.setData({
      patientId: getPatientId(),
      patientName: current ? current.patientName : ''
    })
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

  // ===== 号源加载 =====

  async loadDay(date) {
    this.setData({ loading: true, loadError: '' })
    try {
      const res = await scheduleApi.availableList({ visitDate: date })
      if (res.code !== 200) throw new Error(res.message || '号源加载失败')
      const list = (res.data || []).filter(isBookable).map(normalize)
      const depts = groupByDept(list)
      const patch = { depts, loading: false }

      // 换日期后原来选的科室/医生可能已经没号，逐级回退而不是停在空列表
      const dept = this.data.selectedDept
      const keepDept = dept && depts.find(d => String(d.deptId) === String(dept.deptId))
      if (!keepDept) {
        patch.step = 1
        patch.selectedDept = null
        patch.selectedDoctor = null
        patch.doctors = []
        patch.selectedSchedule = null
      } else {
        patch.selectedDept = keepDept
        patch.doctors = groupByDoctor(list.filter(s => String(s.deptId) === String(keepDept.deptId)))
      }
      this.setData(patch)
    } catch (err) {
      this.setData({ loading: false, loadError: err.message || '号源加载失败', depts: [], doctors: [] })
    }
  },

  onDateTap(e) {
    const date = e.currentTarget.dataset.date
    if (date === this.data.selectedDate) return
    this.setData({ selectedDate: date })
    this.loadDay(date)
  },

  onRetry() {
    this.loadDay(this.data.selectedDate)
  },

  // 搜索页选中科室/医生后 switchTab 过来，只能靠一次性交接数据带进来
  async applyTarget(target) {
    this.setData({ loading: true })
    try {
      const res = await scheduleApi.availableList({
        deptId: target.deptId || undefined,
        doctorId: target.doctorId || undefined
      })
      const list = ((res && res.data) || []).filter(isBookable).map(normalize)
      const limit = this.data.dates[this.data.dates.length - 1].value
      const future = list.filter(s => s.scheduleDate > this.data.selectedDate && s.scheduleDate <= limit)
      const hit = list.filter(s => s.scheduleDate === this.data.selectedDate)[0] || future[0]
      if (!hit) {
        this.setData({ loading: false })
        wx.showToast({ title: '近 7 天暂无可约号源', icon: 'none' })
        this.loadDay(this.data.selectedDate)
        return
      }
      this.setData({ selectedDate: hit.scheduleDate })
      await this.loadDay(hit.scheduleDate)
      const dept = this.data.depts.find(d => String(d.deptId) === String(hit.deptId))
      if (!dept) {
        this.setData({ loading: false })
        return
      }
      this.selectDept(dept)
      if (target.doctorId) {
        const doctor = this.data.doctors.find(d => String(d.doctorId) === String(target.doctorId))
        if (doctor) this.selectDoctor(doctor)
      }
    } catch (err) {
      this.setData({ loading: false, loadError: err.message || '号源加载失败' })
    }
  },

  // ===== 逐级选择 =====

  onDeptTap(e) {
    this.selectDept(e.currentTarget.dataset.dept)
  },

  async selectDept(dept) {
    this.setData({ step: 2, selectedDept: dept, selectedDoctor: null, selectedSchedule: null })
    // 排班里只有医生姓名，职称/擅长要再查一次员工档案；失败不阻断，退化成只显示姓名
    try {
      const res = await doctorApi.selectList({ deptId: dept.deptId })
      const profile = {}
      if (res.code === 200) {
        (res.data || []).forEach(emp => { profile[String(emp.id)] = emp })
      }
      this.setData({
        doctors: this.data.doctors.map(d => ({
          ...d,
          title: (profile[String(d.doctorId)] || {}).title || '',
          specialty: (profile[String(d.doctorId)] || {}).specialty || ''
        }))
      })
    } catch (err) {
      console.error('医生档案加载失败', err)
    }
  },

  onDoctorTap(e) {
    this.selectDoctor(e.currentTarget.dataset.doctor)
  },

  selectDoctor(doctor) {
    this.setData({ step: 3, selectedDoctor: doctor, selectedSchedule: doctor.schedules[0] || null })
  },

  onScheduleTap(e) {
    this.setData({ selectedSchedule: e.currentTarget.dataset.schedule })
  },

  onSettlementTap(e) {
    this.setData({ settlementType: Number(e.currentTarget.dataset.type) })
  },

  goStep(e) {
    const step = Number(e.currentTarget.dataset.step)
    if (step >= this.data.step) return
    this.setData({ step })
  },

  goPatients() {
    wx.navigateTo({ url: '/pages/patients/patients' })
  },

  goMyAppointments() {
    wx.navigateTo({ url: '/pages/appointmentList/appointmentList' })
  },

  // ===== 提交 =====

  async onConfirm() {
    const { selectedSchedule, patientId, visitType, settlementType } = this.data
    if (!patientId) {
      wx.showToast({ title: '请先选择就诊人', icon: 'none' })
      setTimeout(() => wx.navigateTo({ url: '/pages/patients/patients' }), 800)
      return
    }
    if (!selectedSchedule) {
      wx.showToast({ title: '请选择就诊时段', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const res = await appointApi.upsert({
        patientId,
        scheduleId: selectedSchedule.id,
        visitType,
        settlementType,
        // 4-预约挂号：后端只从排班的预约池扣号，传别的会被当成现场渠道抢号
        registSource: 4
      })
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '挂号失败', icon: 'none' })
        this.setData({ submitting: false })
        return
      }
      this.setData({
        submitting: false,
        step: 4,
        // 成功后会刷新号源列表，选中态随之失效，结果页只认提交时的快照
        result: { ...(res.data || {}), timeText: selectedSchedule.timeText, feeText: selectedSchedule.feeText }
      })
      this.loadDay(this.data.selectedDate)
    } catch (err) {
      this.setData({ submitting: false })
      wx.showToast({ title: err.message || '挂号失败', icon: 'none' })
    }
  },

  restart() {
    this.setData({
      step: 1,
      result: null,
      selectedDept: null,
      selectedDoctor: null,
      selectedSchedule: null,
      doctors: []
    })
    this.loadDay(this.data.selectedDate)
  },

  // 挂号费支付（统一支付单口子：桩模式直接已支付）
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
