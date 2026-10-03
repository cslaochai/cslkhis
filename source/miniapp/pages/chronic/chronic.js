import { chronicApi } from '../../utils/api'

Page({
  data: {
    records: [],
    longRxEligible: false,
    loading: true
  },

  onShow() {
    this.loadRecords()
  },

  async loadRecords() {
    this.setData({ loading: true })
    try {
      const res = await chronicApi.myRecords()
      if (res.code === 200 && res.data) {
        const records = (res.data.records || []).map(r => ({
          ...r,
          statusText: ['', '待认定', '已认定', '已取消'][r.confirmStatus] || '未知',
          active: r.confirmStatus === 1,
          confirmTime: r.confirmTime ? String(r.confirmTime).replace('T', ' ') : ''
        }))
        this.setData({ records, longRxEligible: !!res.data.longRxEligible })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载慢病档案失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
