import { triageApi } from '../../utils/api'

Page({
  data: {
    description: '',
    symptoms: [],
    results: [],
    searched: false,
    loading: false
  },

  onLoad() {
    this.loadSymptoms()
  },

  async loadSymptoms() {
    try {
      const res = await triageApi.hotSymptoms()
      if (res && res.code === 200 && res.data) {
        this.setData({ symptoms: res.data })
      }
    } catch (e) {
      console.error('加载常见症状失败', e)
    }
  },

  onInput(e) {
    this.setData({ description: e.detail.value })
  },

  onSymptom(e) {
    this.setData({ description: e.currentTarget.dataset.name })
    this.recommend()
  },

  async recommend() {
    const description = this.data.description.trim()
    if (!description) {
      wx.showToast({ title: '请描述您的不适', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await triageApi.recommend({ description })
      if (res && res.code === 200) {
        // urgent 由后端硬规则给出（表里的急症关键词），前端只负责置顶加红
        const results = (res.data || []).map(d => ({ ...d, urgent: d.urgent === 1 }))
        this.setData({ results, searched: true })
      } else {
        wx.showToast({ title: (res && res.message) || '查询失败', icon: 'none' })
      }
    } catch (e) {
      console.error('导诊失败', e)
      wx.showToast({ title: '网络异常，请重试', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 带着科室跳挂号页：appointment 是 tabBar 页不能带 query，走 appoint_target 缓存
  goAppoint(e) {
    const deptId = e.currentTarget.dataset.deptId
    if (!deptId) return
    wx.setStorageSync('appoint_target', { deptId })
    wx.switchTab({ url: '/pages/appointment/appointment' })
  },

  // 没匹配到时直接进挂号页自己挑科室，不卡住患者
  goAppointFallback() {
    wx.removeStorageSync('appoint_target')
    wx.switchTab({ url: '/pages/appointment/appointment' })
  }
})
