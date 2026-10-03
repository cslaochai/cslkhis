import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { depositApi, payApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'
import { askSubscribe } from '../../utils/wechat'

Page({
  data: {
    today: '',
    patientName: '',
    admissions: [],
    current: null,
    balance: null,
    records: [],
    loading: false,
    paying: false
  },

  onShow() {
    const t = new Date()
    this.setData({
      today: `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`,
      patientName: getCurrentPatient() ? getCurrentPatient().patientName : ''
    })
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
      const res = await depositApi.myAdmissions(getPatientId() || patientId)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
        return
      }
      const admissions = (res.data || []).map(a => ({
        admissionId: String(a.admission_id),
        admissionNo: a.admission_no,
        deptName: a.dept_id ? `科室 ${a.dept_id}` : '',
        admitTime: a.admit_time ? String(a.admit_time).slice(0, 10) : '',
        dischargeTime: a.discharge_time ? String(a.discharge_time).slice(0, 10) : '',
        admitStatus: a.admit_status,
        statusText: Number(a.admit_status) === 1 ? '在院' : '已出院'
      }))
      this.setData({ admissions })
      if (admissions.length > 0) {
        await this.select(admissions[0])
      } else {
        this.setData({ current: null, balance: null, records: [] })
      }
    } finally {
      this.setData({ loading: false })
    }
  },

  async select(adm) {
    this.setData({ current: adm })
    const [bal, list] = await Promise.all([
      depositApi.balance(adm.admissionId),
      depositApi.listPage({ admissionId: adm.admissionId, pageNum: 1, pageSize: 50 })
    ])
    const balance = bal.code === 200 && bal.data ? bal.data : null
    const records = (list.code === 200 && list.data && list.data.records
      ? list.data.records : []).map(r => ({
      prepayNo: r.prepayNo,
      prepayType: r.prepayType,
      typeText: Number(r.prepayType) === 1 ? '充值' : '退款',
      amount: Number(r.amount || 0).toFixed(2),
      payMethodText: ['', '现金', '微信', '支付宝', '银行卡', '转账'][r.payMethod] || '',
      payTime: r.payTime ? String(r.payTime).replace('T', ' ').slice(0, 16) : ''
    }))
    this.setData({
      balance: balance ? {
        total: Number(balance.totalIn || 0).toFixed(2),
        refunded: Number(balance.totalOut || 0).toFixed(2),
        balance: Number(balance.balance || 0).toFixed(2)
      } : null,
      records
    })
  },

  onAdmissionTap(e) {
    const id = e.currentTarget.dataset.id
    const adm = this.data.admissions.find(a => a.admissionId === id)
    if (adm) this.select(adm)
  },

  async onRecharge(e) {
    const amount = Number(e.currentTarget.dataset.amount)
    const { current } = this.data
    if (!current) return
    const res = await wx.showModal({
      title: '押金充值',
      content: `确认为住院 ${current.admissionNo} 充值 ${amount.toFixed(2)} 元？`
    })
    if (!res.confirm) return
    this.setData({ paying: true })
    try {
      // 统一支付单（微信支付口子）：桩模式直接已支付并推进预交金入账
      const order = await payApi.createOrder({ bizType: 3, bizId: current.admissionId, amount })
      if (order.code !== 200 || !order.data) {
        wx.showToast({ title: order.message || '下单失败', icon: 'none' })
        return
      }
      const { payParams } = order.data
      if (payParams) {
        await new Promise((resolve, reject) => {
          wx.requestPayment({ ...payParams, success: resolve, fail: (err) => reject(new Error(err.errMsg || '支付取消')) })
        })
      }
      askSubscribe('pay_receipt')
      wx.showToast({ title: '充值成功', icon: 'success' })
      await this.select(current)
    } catch (err) {
      wx.showToast({ title: err.message || '充值失败', icon: 'none' })
    } finally {
      this.setData({ paying: false })
    }
  }
})
