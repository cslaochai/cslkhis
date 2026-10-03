import { getPatientId, getCurrentPatient } from '../../utils/auth'
import { emrApi } from '../../utils/api'

// 患者可见的病历正文分节（院内质控字段如审核意见、签名状态不在此列）
const SECTIONS = [
  ['chiefComplaint', '主诉'],
  ['presentIllness', '现病史'],
  ['pastHistory', '既往史'],
  ['personalHistory', '个人史'],
  ['familyHistory', '家族史'],
  ['allergyHistory', '过敏史'],
  ['temperature', '体温(℃)'],
  ['pulse', '脉搏(次/分)'],
  ['respiration', '呼吸(次/分)'],
  ['systolicPressure', '收缩压(mmHg)'],
  ['diastolicPressure', '舒张压(mmHg)'],
  ['generalCondition', '一般情况'],
  ['skinMucosa', '皮肤黏膜'],
  ['headNeck', '头颈部'],
  ['chestLung', '胸肺'],
  ['heart', '心脏'],
  ['abdomen', '腹部'],
  ['spineLimbs', '脊柱四肢'],
  ['nervousSystem', '神经系统'],
  ['specialistExam', '专科检查'],
  ['auxiliaryExam', '辅助检查'],
  ['diagnosis', '诊断'],
  ['treatmentPlan', '处理意见']
]

function toSections(detail) {
  return SECTIONS
    .map(([field, label]) => ({ label, value: detail[field] }))
    .filter(s => s.value !== null && s.value !== undefined && s.value !== '')
}

Page({
  data: {
    patientName: '',
    list: [],
    loading: false
  },

  onShow() {
    const current = getCurrentPatient()
    this.setData({ patientName: current ? current.patientName : '' })
    this.loadRecords()
  },

  async loadRecords() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await emrApi.myRecords(patientId)
      if (res.code === 200) {
        const list = (res.data || []).map(r => ({ ...r, key: r.id, expanded: false, sections: null }))
        this.setData({ list })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载病历失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async onToggle(e) {
    const index = e.currentTarget.dataset.index
    const item = this.data.list[index]
    if (item.expanded) {
      this.setData({ [`list[${index}].expanded`]: false })
      return
    }
    if (item.sections) {
      this.setData({ [`list[${index}].expanded`]: true })
      return
    }
    wx.showLoading({ title: '加载中' })
    try {
      const res = await emrApi.myRecordDetail(getPatientId(), item.id)
      if (res.code === 200 && res.data) {
        this.setData({
          [`list[${index}].sections`]: toSections(res.data),
          [`list[${index}].expanded`]: true
        })
      } else {
        wx.showToast({ title: res.message || '加载详情失败', icon: 'none' })
      }
    } catch (err) {
      console.error('加载病历详情失败', err)
      wx.showToast({ title: '加载详情失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  goPatients() {
    wx.navigateTo({ url: '/pages/patients/patients' })
  },

  // 从这一次就诊直接发起复诊：revisit 页首屏会按 recordId 自动选中该病历
  goRevisit(e) {
    wx.setStorageSync('revisit_target', { recordId: e.currentTarget.dataset.id })
    wx.navigateTo({ url: '/pages/revisit/revisit' })
  }
})
