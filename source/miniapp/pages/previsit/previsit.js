import { previsitApi } from '../../utils/api'

// options 里可能是字符串或 {code,label} 对象，统一成以中文 label 作为选中值，
// 这样提交与回显（answersJson 里的 value）天然同一个口径
function toOptionList(options) {
  return (options || []).map(o => {
    if (typeof o === 'string') return { value: o, label: o }
    const label = o.label || o.value || ''
    return { value: label, label }
  })
}

function normQuestion(q) {
  return {
    key: q.key,
    label: q.label,
    type: q.type === 'choice' ? 'choice' : 'text',
    // choice 题必答在提交时校验，text 题留空可跳过
    required: q.type === 'choice',
    optionList: toOptionList(q.options)
  }
}

Page({
  data: {
    registId: '',
    loading: true,
    submitting: false,
    submitted: false,
    mainSymptoms: [],
    commonQuestions: [],
    symptomQuestions: {},
    mainSymptomCode: '',
    answers: {},
    freeText: ''
  },

  onLoad(options) {
    const registId = options.registId || ''
    if (!registId) {
      wx.showToast({ title: '缺少挂号信息', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ registId })
    this.init()
  },

  async init() {
    this.setData({ loading: true })
    try {
      const [qRes, sRes] = await Promise.all([
        previsitApi.questionnaire(),
        previsitApi.getByRegist(this.data.registId)
      ])
      if (qRes.code === 200 && qRes.data) {
        const sq = {}
        const sqMap = qRes.data.symptomQuestions || {}
        Object.keys(sqMap).forEach(code => {
          sq[code] = (sqMap[code] || []).map(normQuestion)
        })
        this.setData({
          mainSymptoms: qRes.data.mainSymptoms || [],
          commonQuestions: (qRes.data.commonQuestions || []).map(normQuestion),
          symptomQuestions: sq
        })
      } else {
        wx.showToast({ title: qRes.message || '量表加载失败', icon: 'none' })
      }
      if (sRes && sRes.code === 200) this.applyEcho(sRes.data)
    } catch (e) {
      console.error('加载预问诊量表失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 回显已提交答卷：主症状按 label 匹配回 code，逐题按 key 回填
  applyEcho(d) {
    if (!d) return
    let parsed = []
    if (d.answersJson) {
      try {
        parsed = JSON.parse(d.answersJson) || []
      } catch (e) {
        parsed = []
      }
    }
    const answers = {}
    parsed.forEach(a => {
      if (a && a.key) answers[a.key] = a.value || ''
    })
    const hit = (this.data.mainSymptoms || []).find(m => m.label === d.mainSymptom)
    this.setData({
      submitted: true,
      answers,
      freeText: d.freeText || '',
      mainSymptomCode: hit ? hit.code : ''
    })
  },

  onPickMain(e) {
    this.setData({ mainSymptomCode: e.currentTarget.dataset.code })
  },

  onPickChoice(e) {
    const { key, value } = e.currentTarget.dataset
    this.setData({ ['answers.' + key]: value })
  },

  onTextInput(e) {
    this.setData({ ['answers.' + e.currentTarget.dataset.key]: e.detail.value })
  },

  onFreeInput(e) {
    this.setData({ freeText: e.detail.value })
  },

  mainSymptomLabel() {
    const hit = this.data.mainSymptoms.find(m => m.code === this.data.mainSymptomCode)
    return hit ? hit.label : ''
  },

  async onSubmit() {
    if (this.data.submitting) return
    if (!this.data.mainSymptomCode) {
      wx.showToast({ title: '请先选择主要症状', icon: 'none' })
      return
    }
    const unanswered = this.data.commonQuestions.find(q => q.required && !this.data.answers[q.key])
    if (unanswered) {
      wx.showToast({ title: `请回答：${unanswered.label}`, icon: 'none' })
      return
    }

    // 只收已作答的题：后端对每条 {key,label,value} 都要求非空，未答题塞占位值会被当成真实回答
    const answers = []
    const collect = questions => {
      ;(questions || []).forEach(q => {
        const v = String(this.data.answers[q.key] || '').trim()
        if (v) answers.push({ key: q.key, label: q.label, value: v })
      })
    }
    collect(this.data.commonQuestions)
    collect(this.data.symptomQuestions[this.data.mainSymptomCode])

    this.setData({ submitting: true })
    try {
      const res = await previsitApi.submit({
        registId: this.data.registId,
        mainSymptom: this.mainSymptomLabel(),
        answers,
        freeText: String(this.data.freeText || '').trim()
      })
      if (res.code === 200) {
        this.setData({ submitted: true })
        wx.showToast({ title: '提交成功，医生接诊时可见', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.message || '提交失败', icon: 'none' })
      }
    } catch (e) {
      console.error('提交预问诊失败', e)
      wx.showToast({ title: '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
