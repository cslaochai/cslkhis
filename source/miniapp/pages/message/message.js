import { messageApi } from '../../utils/api'

const SCENE_TEXT = {
  report_ready: '报告出具',
  queue_called: '排队叫号',
  pay_receipt: '缴费回执'
}

Page({
  data: {
    records: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false
  },

  onShow() {
    this.setData({ records: [], pageNum: 1, finished: false })
    this.loadPage()
  },

  onReachBottom() {
    if (!this.data.finished && !this.data.loading) this.loadPage()
  },

  async loadPage() {
    this.setData({ loading: true })
    try {
      const { pageNum, pageSize } = this.data
      const res = await messageApi.listPage({ pageNum, pageSize })
      if (res.code === 200 && res.data) {
        const items = (res.data.records || []).map(m => ({
          ...m,
          sceneText: SCENE_TEXT[m.bizType] || '通知',
          unread: Number(m.readStatus) === 0
        }))
        const records = this.data.records.concat(items)
        this.setData({
          records,
          total: res.data.total,
          pageNum: pageNum + 1,
          finished: records.length >= res.data.total
        })
        // 列表拉到即视为已读（简化口径：整页标已读）
        const unreadIds = items.filter(i => i.unread).map(i => i.messageId)
        if (unreadIds.length) {
          messageApi.markRead(unreadIds).catch(() => { /* 已读失败不影响浏览 */ })
        }
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载消息失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
