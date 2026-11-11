import { getPatientId } from '../../utils/auth'
import { billApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'

Page({
  data: {
    bills: [],
    loading: false,
    detail: null,
    detailLoading: false
  },

  onShow() {
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
      const res = await billApi.list(getPatientId() || patientId)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
        return
      }
      const bills = (res.data || []).map(b => ({
        ...b,
        totalText: Number(b.totalAmount || 0).toFixed(2),
        payableText: Number(b.payableAmount || 0).toFixed(2),
        paidText: Number(b.paidAmount || 0).toFixed(2),
        unpaidText: Number(b.unpaidAmount || 0).toFixed(2)
      }))
      this.setData({ bills })
    } finally {
      this.setData({ loading: false })
    }
  },

  async onOpen(e) {
    const billId = e.currentTarget.dataset.id
    this.setData({ detailLoading: true, detail: null })
    try {
      const res = await billApi.detail(getPatientId(), billId)
      if (res.code !== 200 || !res.data) {
        wx.showToast({ title: res.message || '详情加载失败', icon: 'none' })
        return
      }
      const d = res.data
      const bill = d.bill || {}
      this.setData({
        detail: {
          billNo: bill.billNo,
          billStatusText: d.billStatusText,
          billTypeText: d.billTypeText,
          billTime: bill.billTime ? String(bill.billTime).replace('T', ' ').slice(0, 16) : '',
          total: Number(bill.totalAmount || 0).toFixed(2),
          pool: Number(bill.poolAmount || 0).toFixed(2),
          account: Number(bill.accountAmount || 0).toFixed(2),
          self: Number(bill.selfAmount || 0).toFixed(2),
          payable: Number(bill.payableAmount || 0).toFixed(2),
          paid: Number(bill.paidAmount || 0).toFixed(2),
          unpaid: Number(bill.unpaidAmount || 0).toFixed(2),
          items: (d.items || []).map(it => ({
            ...it,
            amountText: Number(it.amount || 0).toFixed(2),
            selfText: Number(it.selfAmount || 0).toFixed(2)
          }))
        }
      })
    } finally {
      this.setData({ detailLoading: false })
    }
  },

  closeDetail() {
    this.setData({ detail: null })
  }
})
