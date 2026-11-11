import { getPatientId } from '../../utils/auth'
import { invoiceApi } from '../../utils/api'
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
      const res = await invoiceApi.list(getPatientId() || cached)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '查询票据失败', icon: 'none' })
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
    const invoiceId = e.currentTarget.dataset.id
    if (!invoiceId) {
      return
    }
    try {
      const res = await invoiceApi.detail(getPatientId(), invoiceId)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '票据加载失败', icon: 'none' })
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
  noop() { },

  // 电子票据是 PDF/OFD 外链，小程序内不能直接打开外部页面，先复制链接再用浏览器打开
  onOpenFile() {
    const url = this.data.detail && this.data.detail.electronicUrl
    if (!url) {
      wx.showToast({ title: '该票据暂无电子文件，请联系收费处', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: '链接已复制，请用浏览器打开', icon: 'none' })
    })
  }
})
