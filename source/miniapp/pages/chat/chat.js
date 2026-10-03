import { isLoggedIn } from '../../utils/auth'
import { faqApi, serviceApi, hospitalApi } from '../../utils/api'

/**
 * 客服中心。
 *
 * 【为什么不是聊天机器人】
 * 这一页原来是自由对话壳，调的 /chat/createSession 和 /chat/agent-stream 后端根本没有，
 * 而且它把「要不要转人工」交给模型判断（emergencyLevel ≥ 3 就转）。这在医疗场景是错的：
 * 模型漏判不会报错，患者已经在家等了一夜。
 * 现在改成三件事：**能自己办的给入口、常见的给固定答案、剩下的给人**。
 * 固定答案由人工维护（sys_faq），模型只做后续的报告解读/费用解释，且模型挂了不影响这一页。
 */
Page({
  data: {
    keyword: '',
    searched: false,
    categories: [],
    activeCategory: 'HOT',
    faqs: [],
    hospitalPhone: '',

    showMessage: false,
    messageView: 'form',
    messageContent: '',
    contactPhone: '',
    myMessages: [],
    ticket: null,
    appendContent: '',
    actionReason: '',
    submitting: false,

    serviceCards: [
      { id: 1, name: '智能导诊', icon: '/images/icons/robot.svg', bg: '#e8f4fd', key: 'triage', page: '/pages/triage/triage' },
      { id: 2, name: '报告查询', icon: '/images/icons/filetext.svg', bg: '#e8f5e9', key: 'report', page: '/pages/report/report' },
      { id: 3, name: '门诊缴费', icon: '/images/icons/payment.svg', bg: '#fef3e2', key: 'payment', page: '/pages/payment/payment' },
      { id: 4, name: '处方查询', icon: '/images/icons/pill.svg', bg: '#fce4ec', key: 'prescription', page: '/pages/prescription/prescription' },
      { id: 5, name: '排队叫号', icon: '/images/icons/clock.svg', bg: '#e0f2f1', key: 'queue', page: '/pages/queue/queue' },
      { id: 6, name: '住院押金', icon: '/images/icons/bed.svg', bg: '#f3e5f5', key: 'deposit', page: '/pages/deposit/deposit' }
    ]
  },

  sessionId: '',

  onLoad() {
    if (!isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 会话标识：把同一次进客服页的动作串起来，才能回答「患者是在第几步放弃的」
    this.sessionId = 'cs_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
    this.trace('visit', '')
    this.loadCategories()
    this.loadHot()
    this.loadHospitalPhone()
  },

  goBack() {
    if (getCurrentPages().length > 1) {
      wx.navigateBack({ delta: 1 })
      return
    }
    this.goHome()
  },

  goHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  goMyMessages() {
    this.setData({ showMessage: true, messageView: 'list' })
    this.loadMyMessages()
  },

  async loadMyMessages() {
    try {
      const res = await serviceApi.myMessages({ pageNum: 1, pageSize: 20 })
      this.setData({ myMessages: res?.code === 200 ? (res.data?.records || []) : [] })
    } catch (e) {
      wx.showToast({ title: '工单记录加载失败', icon: 'none' })
    }
  },

  // ===== 工单详情（进展时间轴 + 补充 / 撤单 / 确认 / 重开）=====

  async openTicket(e) {
    const id = String(e.currentTarget.dataset.id || '')
    if (!id) return
    wx.showLoading({ title: '加载中' })
    try {
      const res = await serviceApi.ticketDetail(id)
      if (res?.code !== 200) {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
        return
      }
      const d = res.data || {}
      this.setData({
        messageView: 'detail',
        ticket: {
          ...d,
          canAppend: (d.actions || []).indexOf('append') >= 0,
          canCancel: (d.actions || []).indexOf('cancel') >= 0,
          canConfirm: (d.actions || []).indexOf('confirm') >= 0
        },
        appendContent: '',
        actionReason: ''
      })
    } catch (e2) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  backToTicketList() {
    this.setData({ messageView: 'list' })
    this.loadMyMessages()
  },

  onAppendInput(e) {
    this.setData({ appendContent: e.detail.value })
  },

  onReasonInput(e) {
    this.setData({ actionReason: e.detail.value })
  },

  async submitAppend() {
    const content = (this.data.appendContent || '').trim()
    if (!content) {
      wx.showToast({ title: '请填写补充内容', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const res = await serviceApi.ticketAppend({ id: String(this.data.ticket.id), content })
      if (res?.code === 200) {
        wx.showToast({ title: '已补充', icon: 'success' })
        this.setData({ appendContent: '' })
        this.openTicket({ currentTarget: { dataset: { id: this.data.ticket.id } } })
      } else {
        wx.showToast({ title: res.message || '提交失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async doTicketAction(e) {
    const action = e.currentTarget.dataset.action
    const needReason = action === 'cancel' || action === 'reopen'
    if (needReason) {
      const reason = (this.data.actionReason || '').trim()
      if (!reason) {
        wx.showToast({ title: '请填写原因', icon: 'none' })
        return
      }
    } else {
      const ok = await new Promise((resolve) => {
        wx.showModal({
          title: action === 'confirm' ? '确认已解决' : '提示',
          content: action === 'confirm' ? '确认后工单将关闭，如有新问题请重新提交' : '确定执行该操作？',
          success: (r) => resolve(r.confirm),
          fail: () => resolve(false)
        })
      })
      if (!ok) return
    }
    this.setData({ submitting: true })
    try {
      const res = await serviceApi.ticketAction({
        id: String(this.data.ticket.id),
        action,
        reason: this.data.actionReason || ''
      })
      if (res?.code === 200) {
        wx.showToast({ title: '已提交', icon: 'success' })
        this.setData({ actionReason: '' })
        this.openTicket({ currentTarget: { dataset: { id: this.data.ticket.id } } })
      } else {
        wx.showToast({ title: res.message || '操作失败', icon: 'none' })
      }
    } catch (e2) {
      wx.showToast({ title: '操作失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // ===== 数据 =====

  async loadCategories() {
    try {
      const res = await faqApi.categories()
      if (res?.code === 200 && res.data) {
        // 首位固定「热门」，对应后端的 hotList（热门标记优先、其次查看次数）
        this.setData({ categories: [{ categoryCode: 'HOT', categoryName: '热门', count: 0 }].concat(res.data) })
      }
    } catch (e) {
      wx.showToast({ title: '分类加载失败', icon: 'none' })
    }
  },

  async loadHot() {
    const res = await faqApi.hotList(8)
    if (res?.code === 200 && res.data) {
      this.setData({ faqs: res.data.map(this.decorate) })
    }
  },

  async loadByCategory(code) {
    const isHot = code === 'HOT'
    const res = isHot
      ? await faqApi.hotList(8)
      : await faqApi.listPage({ categoryCode: code, pageSize: 50 })
    if (res?.code === 200) {
      const list = isHot ? res.data : (res.data?.records || [])
      this.setData({ faqs: list.map(this.decorate) })
    }
  },

  async loadHospitalPhone() {
    try {
      const res = await hospitalApi.info()
      const phone = res?.data?.hospitalPhone || wx.getStorageSync('hospitalInfo')?.hospitalPhone || ''
      this.setData({ hospitalPhone: phone })
    } catch (e) {
      this.setData({ hospitalPhone: wx.getStorageSync('hospitalInfo')?.hospitalPhone || '' })
    }
  },

  decorate(item) {
    return { ...item, expanded: false }
  },

  // ===== 搜索 =====

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onClearSearch() {
    this.setData({ keyword: '', searched: false })
    this.loadByCategory(this.data.activeCategory)
  },

  async onSearch() {
    const keyword = (this.data.keyword || '').trim()
    if (!keyword) return
    wx.showLoading({ title: '搜索中' })
    try {
      const res = await faqApi.listPage({ keyword, pageSize: 50 })
      const list = res?.code === 200 ? (res.data?.records || []) : []
      this.setData({ searched: true, faqs: list.map(this.decorate) })
      // 命中数要回传：搜了但一条都没中，是最该补语料的信号
      this.trace('search', keyword, { hitCount: list.length })
    } catch (e) {
      wx.showToast({ title: '搜索失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  onCategoryTap(e) {
    const code = e.currentTarget.dataset.code
    if (code === this.data.activeCategory) return
    this.setData({ activeCategory: code, searched: false })
    this.loadByCategory(code)
  },

  // ===== 常见问题 =====

  async onFaqTap(e) {
    const id = String(e.currentTarget.dataset.id)
    const faqs = this.data.faqs.map((item) => (
      String(item.id) === id ? { ...item, expanded: !item.expanded } : item
    ))
    this.setData({ faqs })
    const target = faqs.find((item) => String(item.id) === id)
    if (target && target.expanded) {
      // 记查看次数用 getById；失败无所谓，答案已经在列表里了
      faqApi.getById(id).catch(() => {})
      this.trace('view', id, { faqId: id })
    }
  },

  async onFeedback(e) {
    const id = String(e.currentTarget.dataset.id)
    const helpful = Number(e.currentTarget.dataset.helpful)
    try {
      await faqApi.feedback({ faqId: id, helpful })
      this.trace(helpful === 1 ? 'helpful' : 'useless', id, { faqId: id })
      wx.showToast({ title: '感谢反馈', icon: 'none' })
    } catch (e) {
      wx.showToast({ title: '反馈失败', icon: 'none' })
    }
  },

  onCardTap(e) {
    const card = e.currentTarget.dataset.card
    this.trace('card', card.key)
    if (!card.page) {
      wx.showToast({ title: '功能即将上线', icon: 'none' })
      return
    }
    wx.navigateTo({ url: card.page })
  },

  // ===== 转人工 =====

  onCallHospital() {
    this.trace('transfer', 'phone')
    if (!this.data.hospitalPhone) {
      wx.showToast({ title: '暂未获取到医院电话', icon: 'none' })
      return
    }
    wx.makePhoneCall({
      phoneNumber: this.data.hospitalPhone,
      fail: () => {}
    })
  },

  onOpenMessage() {
    this.trace('transfer', 'message')
    this.setData({ showMessage: true, messageView: 'form' })
  },

  onCloseMessage() {
    this.setData({ showMessage: false, messageView: 'form' })
  },

  onMessageInput(e) {
    this.setData({ messageContent: e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ contactPhone: e.detail.value })
  },

  async onSubmitMessage() {
    const content = (this.data.messageContent || '').trim()
    if (!content) {
      wx.showToast({ title: '请填写留言内容', icon: 'none' })
      return
    }
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      const res = await serviceApi.messageUpsert({
        content,
        contactPhone: this.data.contactPhone || undefined,
        categoryCode: this.data.activeCategory === 'HOT' ? undefined : this.data.activeCategory
      })
      if (res?.code === 200) {
        this.trace('message', res.data || '')
        this.setData({ showMessage: false, messageView: 'form', messageContent: '' })
        wx.showToast({ title: '留言已提交', icon: 'success' })
      } else {
        wx.showToast({ title: res?.message || '提交失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // ===== 埋点 =====

  trace(eventType, eventKey, extra) {
    serviceApi.trace({
      sessionId: this.sessionId,
      eventType,
      eventKey: String(eventKey || ''),
      faqId: extra?.faqId ? String(extra.faqId) : undefined,
      hitCount: extra?.hitCount
    }).catch(() => {})
  }
})
