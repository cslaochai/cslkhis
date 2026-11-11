import { getPatientId } from '../../utils/auth'
import { surveyApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'

Page({
  data: {
    tasks: [],
    loading: false,
    paper: null,
    answers: [],
    anonymous: false,
    commentText: '',
    submitting: false
  },

  onShow() {
    this.load()
  },

  async load() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    await restoreCurrentPatient()
    this.setData({ loading: true })
    try {
      const res = await surveyApi.pending(getPatientId() || patientId)
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
        return
      }
      this.setData({ tasks: res.data || [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  async onOpen(e) {
    const task = e.currentTarget.dataset.task
    const res = await surveyApi.paper(getPatientId(), task.dispatchId)
    if (res.code !== 200 || !res.data) {
      wx.showToast({ title: res.message || '问卷加载失败', icon: 'none' })
      return
    }
    const paper = res.data
    const answers = (paper.items || []).map(q => ({
      itemId: q.itemId,
      seqNo: q.seqNo,
      title: q.title,
      questionType: q.questionType,
      required: q.required,
      maxScore: q.maxScore || (q.questionType === 4 ? 10 : 5),
      dimension: q.dimension,
      // 量表/NPS 未选时保持 null：0 分是 NPS 的合法值，不能用它表示"未作答"
      score: null,
      optionLabel: '',
      textValue: '',
      options: this.buildOptions(q)
    }))
    this.setData({ paper, answers, anonymous: false, commentText: paper.commentText || '' })
  },

  buildOptions(q) {
    if (q.questionType === 1) {
      const max = q.maxScore || 5
      const arr = []
      for (let i = 1; i <= max; i++) arr.push(i)
      return arr
    }
    if (q.questionType === 4) {
      const max = q.maxScore || 10
      const arr = []
      for (let i = 0; i <= max; i++) arr.push(i)
      return arr
    }
    return []
  },

  // 内层分值列表与外层题目是嵌套 wx:for，用题目ID定位而不是下标，避免索引被内层遮蔽
  indexOfItem(itemId) {
    return this.data.answers.findIndex(a => String(a.itemId) === String(itemId))
  },

  onScore(e) {
    const index = this.indexOfItem(e.currentTarget.dataset.item)
    if (index < 0) return
    this.setData({ [`answers[${index}].score`]: Number(e.currentTarget.dataset.score) })
  },

  onText(e) {
    const index = this.indexOfItem(e.currentTarget.dataset.item)
    if (index < 0) return
    this.setData({ [`answers[${index}].textValue`]: e.detail.value })
  },

  onOption(e) {
    const index = this.indexOfItem(e.currentTarget.dataset.item)
    if (index < 0) return
    this.setData({ [`answers[${index}].optionLabel`]: e.detail.value })
  },

  onAnonymous(e) {
    this.setData({ anonymous: e.detail.value })
  },

  onComment(e) {
    this.setData({ commentText: e.detail.value })
  },

  closePaper() {
    this.setData({ paper: null, answers: [] })
  },

  async onSubmit() {
    const { paper, answers, anonymous, commentText, submitting } = this.data
    if (!paper || submitting) return
    for (const a of answers) {
      if (a.required !== 1) continue
      if ((a.questionType === 1 || a.questionType === 4) && a.score === null) {
        wx.showToast({ title: `请完成第 ${a.seqNo || ''} 题：${a.title}`, icon: 'none' })
        return
      }
      if ((a.questionType === 2 || a.questionType === 3) && !String(a.optionLabel || '').trim()) {
        wx.showToast({ title: `请完成第 ${a.seqNo || ''} 题：${a.title}`, icon: 'none' })
        return
      }
      if (a.questionType === 5 && !String(a.textValue || '').trim()) {
        wx.showToast({ title: `请完成第 ${a.seqNo || ''} 题：${a.title}`, icon: 'none' })
        return
      }
    }
    this.setData({ submitting: true })
    try {
      const res = await surveyApi.submit({
        patientId: getPatientId(),
        dispatchId: paper.dispatchId,
        anonymousFlag: anonymous ? 1 : 0,
        commentText: commentText,
        answers: answers.map(a => ({
          itemId: a.itemId,
          score: a.questionType === 1 || a.questionType === 4 ? a.score : null,
          optionLabel: a.questionType === 2 || a.questionType === 3 ? a.optionLabel : null,
          textValue: a.questionType === 5 ? a.textValue : null
        }))
      })
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '提交失败', icon: 'none' })
        return
      }
      wx.showToast({ title: '感谢您的评价', icon: 'success' })
      this.closePaper()
      await this.load()
    } catch (err) {
      wx.showToast({ title: err.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
