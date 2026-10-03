import { getPatientId } from '../../utils/auth'
import { chargeApi, payApi, aiApi } from '../../utils/api'
import { askSubscribe } from '../../utils/wechat'

Page({
  data: {
    list: [],
    totalAmount: '0.00',
    loading: false,
    payingId: null,
    // 费用解释：按账单存，key 是 billId。患者问「医保不是报 85% 吗为什么我掏这么多」，
    // 答案是账里有医保不承担的自费/丙类项目 —— 这是确定性计算，不是 AI 猜的
    feeExplains: {},
    feeExplainingId: null
  },

  onShow() {
    this.loadPending()
  },

  async loadPending() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await chargeApi.pendingPage({
        patientId,
        chargeStatus: 1,
        pageNum: 1,
        pageSize: 50
      })
      if (res.code === 200 && res.data) {
        const list = (res.data || []).map(c => ({
          id: c.id,
          billNo: c.billNo,
          deptName: (c.details && c.details[0] && c.details[0].deptName) || '',
          encounterNo: c.encounterNo,
          totalAmount: this.fmt(c.payableAmount),
          createTime: c.billTime ? String(c.billTime).replace('T', ' ') : '',
          // 医保拆分：患者问「自付为什么这么多」时界面自己能答，不必再问客服
          poolAmount: this.fmt(c.poolAmount),
          accountAmount: this.fmt(c.accountAmount),
          selfAmount: this.fmt(c.selfAmount),
          hasInsurance: this.num(c.poolAmount) > 0 || this.num(c.accountAmount) > 0 || this.num(c.selfAmount) > 0,
          details: (c.details || []).map(d => ({
            itemName: d.itemName,
            amount: this.fmt(d.amount),
            spec: d.specification || '',
            unit: d.unit || '',
            price: this.fmt(d.price),
            quantity: d.quantity == null ? '' : String(d.quantity),
            poolAmount: this.fmt(d.poolAmount),
            accountAmount: this.fmt(d.accountAmount),
            selfAmount: this.fmt(d.selfAmount),
            catalogTypeText: d.catalogTypeText || '',
            hasSplit: this.num(d.poolAmount) > 0 || this.num(d.accountAmount) > 0 || this.num(d.selfAmount) > 0
          })),
          expanded: false
        }))
        const total = list.reduce((s, c) => s + parseFloat(c.totalAmount), 0)
        this.setData({ list, totalAmount: total.toFixed(2) })
      }
    } catch (e) {
      console.error('加载待缴费失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  fmt(v) {
    const n = parseFloat(v)
    return isNaN(n) ? '0.00' : n.toFixed(2)
  },

  num(v) {
    const n = parseFloat(v)
    return isNaN(n) ? 0 : n
  },

  toggleDetail(e) {
    const id = e.currentTarget.dataset.id
    const list = this.data.list.map(c => c.id === id ? { ...c, expanded: !c.expanded } : c)
    this.setData({ list })
  },

  async loadFeeExplain(e) {
    const id = String(e.currentTarget.dataset.id)
    if (this.data.feeExplainingId) return
    this.setData({ feeExplainingId: id })
    try {
      const res = await aiApi.feeExplain({ billId: id })
      if (res.code === 200 && res.data) {
        this.setData({ [`feeExplains.${id}`]: res.data })
      } else {
        wx.showToast({ title: res.message || '暂无法解释', icon: 'none' })
      }
    } catch (err) {
      console.error('费用解释失败', err)
      wx.showToast({ title: '暂无法解释', icon: 'none' })
    } finally {
      this.setData({ feeExplainingId: null })
    }
  },

  closeFeeExplain(e) {
    const id = String(e.currentTarget.dataset.id)
    this.setData({ [`feeExplains.${id}`]: null })
  },

  async onPay(e) {
    const id = e.currentTarget.dataset.id
    const res = await wx.showModal({ title: '确认缴费', content: '确认支付该笔费用？' })
    if (!res.confirm) return
    this.setData({ payingId: id })
    try {
      // 统一支付单（微信支付口子）：桩模式直接返回已支付；真收银台模式返回 payParams
      const order = await payApi.createOrder({ bizType: 1, bizId: id })
      if (order.code !== 200 || !order.data) {
        wx.showToast({ title: order.message || '下单失败', icon: 'none' })
        return
      }
      const { payParams, payStatus } = order.data
      if (payParams) {
        // 真收银台模式：拉起微信支付，回调由后端异步推进
        await new Promise((resolve, reject) => {
          wx.requestPayment({
            ...payParams,
            success: resolve,
            fail: (err) => reject(new Error(err.errMsg || '支付取消'))
          })
        })
        wx.showToast({ title: '缴费成功', icon: 'success' })
      } else if (payStatus === 1) {
        // 桩模式：createOrder 已同步推进账单支付成功，无需再调旧的 /miniapp/charge/pay
        askSubscribe('pay_receipt')
        wx.showToast({ title: '缴费成功', icon: 'success' })
      }
      this.loadPending()
    } catch (err) {
      wx.showToast({ title: err.message || '缴费失败', icon: 'none' })
    } finally {
      this.setData({ payingId: null })
    }
  }
})
