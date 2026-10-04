import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { prescriptionApi, aiApi } from '../../utils/api'

/** 取药指引：状态码口径见后端 PrescriptionStatusEnum，缴费码见 PrescriptionPayStatusEnum */
function tipOf(rx) {
  if (rx.prescriptionStatus === 2) return '药师审方中，请耐心等待'
  if (rx.prescriptionStatus === 3) {
    if (rx.paymentStatus === 0) return '处方已审核，请缴费后到药房窗口取药'
    if (rx.paymentStatus === 2) return '该处方已退费，不能取药'
    return '请到门诊药房窗口，报处方号取药'
  }
  if (rx.prescriptionStatus === 4) return `已于 ${rx.dispenseTime || ''} 发药取走`
  if (rx.prescriptionStatus === 5) return '该处方已作废'
  if (rx.prescriptionStatus === 6) return '该处方已退药'
  return ''
}

Page({
  data: {
    patientName: '',
    list: [],
    loading: false,
    // 用药说明按处方ID缓存；剂量/频次全部来自后端照抄的医嘱，前端不做任何加工
    guides: {},
    guideLoadingId: null
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
      const res = await prescriptionApi.myList(patientId)
      if (res.code === 200) {
        const list = (res.data || []).map(rx => ({
          ...rx,
          key: rx.id,
          tip: tipOf(rx),
          canPay: rx.prescriptionStatus === 3 && rx.paymentStatus === 0,
          drugs: (rx.details || []).map(d => ({
            name: d.drugName,
            spec: d.specification,
            qty: `${d.quantity != null ? d.quantity : ''}${d.unit || ''}`,
            usage: [d.singleDosage, d.frequency, d.route].filter(Boolean).join(' ') || d.usageDosage || ''
          })),
          amountText: rx.totalAmount != null ? Number(rx.totalAmount).toFixed(2) : ''
        }))
        this.setData({ list })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载处方失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onToggle(e) {
    const index = e.currentTarget.dataset.index
    const key = `list[${index}].expanded`
    this.setData({ [key]: !this.data.list[index].expanded })
  },

  // 用药说明：这盒药到底怎么吃。剂量/频次/疗程全部由后端照抄医嘱给出，
  // 前端只负责排版 —— 任何"帮他算一下"的加工都会让患者吃到与医嘱不一致的量。
  async loadGuide(e) {
    const id = e.currentTarget.dataset.id
    if (!id || this.data.guideLoadingId) return
    this.setData({ guideLoadingId: id })
    try {
      const res = await aiApi.medicationGuide({ prescriptionId: String(id) })
      if (res && res.code === 200 && res.data) {
        this.setData({ [`guides.${id}`]: res.data })
      } else {
        wx.showToast({ title: (res && res.message) || '暂无法生成用药说明', icon: 'none' })
      }
    } catch (err) {
      console.error('加载用药说明失败', err)
      wx.showToast({ title: '加载失败，请重试', icon: 'none' })
    } finally {
      this.setData({ guideLoadingId: null })
    }
  },

  closeGuide(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ [`guides.${id}`]: null })
  },

  goPayment() {
    wx.navigateTo({ url: '/pages/payment/payment' })
  },

  goPatients() {
    wx.navigateTo({ url: '/pages/patients/patients' })
  }
})
