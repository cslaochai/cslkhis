import { request, sseRequest } from '../../utils/request'
import { isLoggedIn } from '../../utils/auth'

Page({
  data: {
    messages: [],
    inputValue: '',
    sending: false,
    scrollToId: '',
    sessionId: null,

    // 问题定位点
    userQuestions: [],
    currentDotIndex: -1,

    quickQuestions: [
      '骨科挂号怎么挂？',
      '河南医保在上海能报销吗？',
      '帮我看看体检报告'
    ]
  },

  sseTask: null,

  onLoad() {
    if (!isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.createSession()
  },

  onUnload() {
    this.abortSSE()
  },

  // ===== 会话 =====

  goBack() {
    // 直接由 reLaunch/switchTab 进来时页面栈只有本页，navigateBack 会失败，需兜底回首页
    if (getCurrentPages().length > 1) {
      wx.navigateBack({ delta: 1 })
      return
    }
    this.goHome()
  },

  goHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  async createSession() {
    try {
      const res = await request({ url: '/chat/createSession?title=小程序对话', method: 'POST' })
      if (res?.code === 200 && res.data) {
        this.setData({ sessionId: res.data.id })
      }
    } catch (e) {
      this.setData({ sessionId: 'mini_' + Date.now() })
    }
  },

  // ===== 消息发送 =====

  onInput(e) {
    this.setData({ inputValue: e.detail.value })
  },

  onQuickQuestion(e) {
    const q = e.currentTarget.dataset.q
    this.setData({ inputValue: q })
    this.sendMessage()
  },

  sendMessage() {
    const text = this.data.inputValue.trim()
    if (!text || this.data.sending) return

    const userMsg = {
      id: Date.now(),
      role: 'user',
      content: text,
      time: this.formatTime(new Date())
    }

    const aiMsg = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      emergencyLevel: 0
    }

    const messages = [...this.data.messages, userMsg, aiMsg]
    this.setData({
      messages,
      inputValue: '',
      sending: true,
      scrollToId: 'msg-' + aiMsg.id
    })

    this.updateUserQuestions()

    let contentTimer = null
    let maxTimer = null

    const resetSending = () => {
      if (contentTimer) clearTimeout(contentTimer)
      if (maxTimer) clearTimeout(maxTimer)
      if (this.data.sending) this.setData({ sending: false })
    }

    // 最长30秒，无论如何都恢复按钮
    maxTimer = setTimeout(resetSending, 30000)

    this.sseTask = sseRequest({
      url: '/chat/agent-stream',
      data: { sessionId: this.data.sessionId, message: text },
      onMessage: (data) => {
        this.handleSSEMessage(data, aiMsg.id)
        if (data.type === 'content') {
          if (contentTimer) clearTimeout(contentTimer)
          contentTimer = setTimeout(resetSending, 5000)
        }
        if (data.type === 'done' || data.type === 'error') {
          resetSending()
        }
      },
      onDone: resetSending,
      onError: (err) => {
        resetSending()
        this.updateMessage(aiMsg.id, { content: '请求失败: ' + (err.message || '网络错误') })
      }
    })
  },

  handleSSEMessage(data, aiMsgId) {
    if (data.type === 'content') {
      const msg = this.findMessage(aiMsgId)
      if (msg) this.updateMessage(aiMsgId, { content: msg.content + data.content })
    } else if (data.type === 'need_transfer') {
      this.updateMessage(aiMsgId, {
        content: '',
        needTransfer: true,
        transferTip: data.message,
        emergencyLevel: data.emergencyLevel || 3
      })
    } else if (data.type === 'error') {
      this.updateMessage(aiMsgId, { content: data.message })
      if (data.code === 401) {
        wx.showToast({ title: '登录已过期', icon: 'none' })
        setTimeout(() => wx.reLaunch({ url: '/pages/login/login' }), 1500)
      }
    } else if (data.type === 'done') {
      this.updateMessage(aiMsgId, {
        messageId: data.messageId,
        responseTime: data.responseTime,
        emergencyLevel: data.emergencyLevel || 0,
        needTransfer: data.needTransfer || false,
        transferTip: data.transferTip || '',
        time: this.formatTime(new Date())
      })
      this.setData({ sending: false })
    }
  },

  handleTransfer(e) {
    const msg = e.currentTarget.dataset.msg
    this.updateMessage(msg.id, { transferred: true })
    const sysMsg = {
      id: Date.now(),
      role: 'assistant',
      content: '已为您转接人工客服，请稍候。',
      emergencyLevel: 3,
      time: this.formatTime(new Date())
    }
    this.setData({ messages: [...this.data.messages, sysMsg] })
  },

  // ===== 问题定位点 =====

  updateUserQuestions() {
    const userQuestions = this.data.messages
      .filter(m => m.role === 'user')
      .map(m => ({ id: m.id, text: m.content.substring(0, 15) }))
    this.setData({ userQuestions })
  },

  scrollToQuestion(e) {
    const id = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    this.setData({
      scrollToId: 'msg-' + id,
      currentDotIndex: index
    })
  },

  // ===== 工具方法 =====

  updateMessage(msgId, updates) {
    const messages = this.data.messages.map(m => m.id === msgId ? { ...m, ...updates } : m)
    this.setData({ messages, scrollToId: 'msg-' + msgId })
  },

  findMessage(msgId) {
    return this.data.messages.find(m => m.id === msgId)
  },

  abortSSE() {
    if (this.sseTask) {
      this.sseTask.abort()
      this.sseTask = null
    }
  },

  formatTime(date) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
  }
})
