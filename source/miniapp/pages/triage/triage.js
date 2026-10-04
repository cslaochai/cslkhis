import { triageApi, aiApi } from '../../utils/api'

Page({
  data: {
    description: '',
    symptoms: [],
    results: [],
    searched: false,
    loading: false,
    // 口语归一：整理出的症状词与补充追问（模型不可用时都为空，页面不显示这两块）
    terms: [],
    followUps: [],
    normalized: false
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
      // 先把口语整理成症状词再查规则表。归一失败/模型不可用都用原话继续查，
      // 患者完全无感 —— 关键词匹配本来就是按原始文本走的，归一只是提高命中率。
      let searchText = description
      let terms = []
      let followUps = []
      try {
        const n = await aiApi.triageNormalize({ description })
        if (n && n.code === 200 && n.data) {
          if (n.data.searchText) searchText = n.data.searchText
          terms = n.data.terms || []
          followUps = n.data.followUps || []
        }
      } catch (e) {
        console.error('导诊归一失败，改用原话查询', e)
      }

      const res = await triageApi.recommend({ description: searchText })
      if (res && res.code === 200) {
        // urgent 由后端硬规则给出（表里的急症关键词），前端只负责置顶加红
        const results = (res.data || []).map(d => ({ ...d, urgent: d.urgent === 1 }))
        this.setData({
          results,
          searched: true,
          terms,
          followUps,
          normalized: searchText !== description
        })
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

  // 点「医生可能还会问」里的某条 → 让患者补一句 → 带着补充再查一次。
  // 导诊真正的痛点不是算法不够聪明，是患者第一句永远说不全。
  async onFollowUp(e) {
    const question = e.currentTarget.dataset.q
    if (!question) return
    const modal = await wx.showModal({
      title: question,
      editable: true,
      placeholderText: '补充一下，再帮你查一次',
      confirmText: '再查一次',
      cancelText: '不用了'
    })
    if (!modal.confirm || !modal.content || !modal.content.trim()) return
    this.setData({ description: this.data.description.trim() + '，' + modal.content.trim() })
    this.recommend()
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
