import { getPatientId } from '../../utils/auth'
import { checkupApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'

Page({
  data: {
    list: [],
    loading: false,
    detail: null
  },

  onShow() {
    this.load()
  },

  async load() {
    const cached = getPatientId()
    if (!cached) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    await restoreCurrentPatient()
    this.setData({ loading: true })
    try {
      const res = await checkupApi.list(getPatientId() || cached)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '查询体检记录失败', icon: 'none' })
        return
      }
      const page = res.data || {}
      this.setData({ list: page.records || [] })
    } catch (e) {
      console.error(e)
    } finally {
      this.setData({ loading: false })
    }
  },

  async onTapItem(e) {
    const recordId = e.currentTarget.dataset.id
    if (!recordId) {
      return
    }
    try {
      const res = await checkupApi.detail(getPatientId(), recordId)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '报告加载失败', icon: 'none' })
        return
      }
      this.setData({ detail: res.data })
    } catch (e) {
      console.error(e)
    }
  },

  onCloseDetail() {
    this.setData({ detail: null })
  },

  // 浮层内容区 catchtap，阻止冒泡到遮罩关闭
  noop() { }
})
