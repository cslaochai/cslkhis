import { getCurrentPatient, setCurrentPatient } from '../../utils/auth'
import { guardianApi } from '../../utils/api'
import { restoreCurrentPatient } from '../../utils/patientSync'

const GENDERS = [
  { value: 1, label: '男' },
  { value: 2, label: '女' }
]

const RESEND_SECONDS = 60
const PHONE_RE = /^1[3-9]\d{9}$/

// 与字典 sys_patient_relation 码值一致
const RELATIONS = [
  { value: 1, label: '本人' },
  { value: 2, label: '配偶' },
  { value: 3, label: '父亲' },
  { value: 4, label: '母亲' },
  { value: 5, label: '儿子' },
  { value: 6, label: '女儿' },
  { value: 7, label: '兄弟' },
  { value: 8, label: '姐妹' },
  { value: 9, label: '祖父' },
  { value: 10, label: '祖母' },
  { value: 11, label: '外祖父' },
  { value: 12, label: '外祖母' },
  { value: 13, label: '其他亲属' },
  { value: 14, label: '朋友' },
  { value: 15, label: '同事' },
  { value: 16, label: '单位' },
  { value: 99, label: '其他' }
]

Page({
  data: {
    patients: [],
    currentId: '',
    loading: false,
    genders: GENDERS,
    relations: RELATIONS,
    addVisible: false,
    bindVisible: false,
    addForm: { patientName: '', genderIndex: -1, idCard: '', phone: '', smsCode: '', relationIndex: -1 },
    bindForm: { patientName: '', idCard: '', smsCode: '', relationIndex: -1 },
    bindPhoneMask: '',
    addCountdown: 0,
    bindCountdown: 0,
    submitting: false
  },

  onShow() {
    this.loadPatients()
  },

  onHide() {
    this.clearCodeTimers()
  },

  onUnload() {
    this.clearCodeTimers()
  },

  clearCodeTimers() {
    if (this._addTimer) clearInterval(this._addTimer)
    if (this._bindTimer) clearInterval(this._bindTimer)
    this._addTimer = null
    this._bindTimer = null
  },

  async loadPatients() {
    this.setData({ loading: true })
    try {
      const res = await guardianApi.myPatients()
      if (res.code === 200) {
        let current = getCurrentPatient()
        const list = (res.data || []).map(p => ({
          ...p,
          genderText: p.gender === 1 ? '男' : p.gender === 2 ? '女' : '未知',
          // 后端已打码（前6后4），前端不再二次处理明文
          idCardMasked: p.idCard || '未登记',
          phoneText: p.phone || '未留电话'
        }))
        // 指针指向的档案已不在列表里（别处解绑、账号切换）时按后端默认重新回填
        if (current && !list.some(p => p.patientId === current.patientId)) {
          await restoreCurrentPatient()
          current = getCurrentPatient()
        }
        this.setData({
          patients: list,
          currentId: current ? current.patientId : ''
        })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载就诊人失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onSelect(e) {
    const p = e.currentTarget.dataset.patient
    // 当前选中项若还不是默认，顺手设为默认：小程序没有"每次选"的概念，选中即长期生效
    if (p.isDefault !== 1) guardianApi.setDefault({ patientId: p.patientId }).then(() => this.loadPatients())
    setCurrentPatient(p)
    this.setData({ currentId: p.patientId })
    wx.showToast({ title: `已切换为 ${p.patientName}`, icon: 'none' })
  },

  async onSetDefault(e) {
    const patientId = e.currentTarget.dataset.id
    const res = await guardianApi.setDefault({ patientId })
    if (res.code === 200) {
      wx.showToast({ title: '已设为默认', icon: 'none' })
      this.loadPatients()
    } else {
      wx.showToast({ title: res.message || '设置失败', icon: 'none' })
    }
  },

  async onUnbind(e) {
    const p = e.currentTarget.dataset.patient
    const { confirm } = await wx.showModal({
      title: '解绑就诊人',
      content: `确定解绑「${p.patientName}」吗？解绑后不能再为其挂号缴费。`
    })
    if (!confirm) return
    const res = await guardianApi.unbindPatient({ patientId: p.patientId })
    if (res.code === 200) {
      if (this.data.currentId === p.patientId) setCurrentPatient(null)
      wx.showToast({ title: '已解绑', icon: 'none' })
      this.loadPatients()
    } else {
      wx.showToast({ title: res.message || '解绑失败', icon: 'none' })
    }
  },

  openAdd() {
    this.setData({ addVisible: true, addForm: { patientName: '', genderIndex: -1, idCard: '', phone: '', smsCode: '', relationIndex: -1 } })
  },

  closeAdd() {
    this.setData({ addVisible: false })
  },

  onAddInput(e) {
    this.setData({ [`addForm.${e.currentTarget.dataset.field}`]: e.detail.value })
  },

  onAddGender(e) {
    this.setData({ 'addForm.genderIndex': Number(e.detail.value) })
  },

  onAddRelation(e) {
    this.setData({ 'addForm.relationIndex': Number(e.detail.value) })
  },

  async submitAdd() {
    const f = this.data.addForm
    if (!f.patientName.trim()) return wx.showToast({ title: '请输入姓名', icon: 'none' })
    if (f.genderIndex < 0) return wx.showToast({ title: '请选择性别', icon: 'none' })
    if (!/^\d{17}[\dXx]$/.test(f.idCard.trim())) return wx.showToast({ title: '身份证号格式不正确', icon: 'none' })
    if (!PHONE_RE.test(f.phone.trim())) return wx.showToast({ title: '手机号格式不正确', icon: 'none' })
    if (!f.smsCode.trim()) return wx.showToast({ title: '请输入短信验证码', icon: 'none' })
    if (f.relationIndex < 0) return wx.showToast({ title: '请选择关系', icon: 'none' })
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      const res = await guardianApi.addPatient({
        patientName: f.patientName.trim(),
        gender: GENDERS[f.genderIndex].value,
        idCard: f.idCard.trim().toUpperCase(),
        phone: f.phone.trim(),
        smsCode: f.smsCode.trim(),
        relation: RELATIONS[f.relationIndex].value
      })
      if (res.code === 200) {
        this.setData({ addVisible: false })
        setCurrentPatient(res.data)
        wx.showToast({ title: '已添加并选用', icon: 'success' })
        this.loadPatients()
      } else {
        wx.showToast({ title: res.message || '添加失败', icon: 'none' })
      }
    } finally {
      this.setData({ submitting: false })
    }
  },

  openBind() {
    this.setData({ bindVisible: true, bindForm: { patientName: '', idCard: '', smsCode: '', relationIndex: -1 }, bindPhoneMask: '' })
  },

  closeBind() {
    this.setData({ bindVisible: false })
  },

  onBindInput(e) {
    this.setData({ [`bindForm.${e.currentTarget.dataset.field}`]: e.detail.value })
  },

  onBindRelation(e) {
    this.setData({ 'bindForm.relationIndex': Number(e.detail.value) })
  },

  // 新增建档：验证码发往操作人填写的手机号
  async onAddSendCode() {
    if (this.data.addCountdown > 0) return
    const phone = this.data.addForm.phone.trim()
    if (!PHONE_RE.test(phone)) return wx.showToast({ title: '请先填写正确的手机号', icon: 'none' })
    try {
      const res = await guardianApi.sendAddCode({ phone })
      if (res.code === 200) {
        this.afterSendCode(res.data, 'add')
      } else {
        wx.showToast({ title: res.message || '发送失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '发送失败', icon: 'none' })
    }
  },

  // 绑定已建档：号码由后端按姓名+身份证定位档案后取建档预留手机号，前端传不了收码号码
  async onBindSendCode() {
    if (this.data.bindCountdown > 0) return
    const f = this.data.bindForm
    if (!f.patientName.trim()) return wx.showToast({ title: '请先输入姓名', icon: 'none' })
    if (!/^\d{17}[\dXx]$/.test(f.idCard.trim())) return wx.showToast({ title: '请先填写正确的身份证号', icon: 'none' })
    try {
      const res = await guardianApi.sendBindCode({ patientName: f.patientName.trim(), idCard: f.idCard.trim().toUpperCase() })
      if (res.code === 200) {
        if (res.data && res.data.phoneMask) this.setData({ bindPhoneMask: res.data.phoneMask })
        this.afterSendCode(res.data, 'bind')
      } else {
        wx.showToast({ title: res.message || '发送失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '发送失败', icon: 'none' })
    }
  },

  afterSendCode(data, formKey) {
    if (data && data.mockCode) this.setData({ [`${formKey}Form.smsCode`]: data.mockCode })
    wx.showToast({ title: data && data.mockCode ? '验证码已自动填入' : '验证码已发送', icon: 'none' })
    this.startCountdown(formKey)
  },

  startCountdown(formKey) {
    this.setData({ [`${formKey}Countdown`]: RESEND_SECONDS })
    const timer = setInterval(() => {
      const next = this.data[`${formKey}Countdown`] - 1
      this.setData({ [`${formKey}Countdown`]: next })
      if (next <= 0) clearInterval(timer)
    }, 1000)
    if (formKey === 'add') this._addTimer = timer
    else this._bindTimer = timer
  },

  async submitBind() {
    const f = this.data.bindForm
    if (!f.patientName.trim()) return wx.showToast({ title: '请输入姓名', icon: 'none' })
    if (!/^\d{17}[\dXx]$/.test(f.idCard.trim())) return wx.showToast({ title: '身份证号格式不正确', icon: 'none' })
    if (!f.smsCode.trim()) return wx.showToast({ title: '请输入短信验证码', icon: 'none' })
    if (f.relationIndex < 0) return wx.showToast({ title: '请选择关系', icon: 'none' })
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      const res = await guardianApi.bindPatient({
        patientName: f.patientName.trim(),
        idCard: f.idCard.trim().toUpperCase(),
        smsCode: f.smsCode.trim(),
        relation: RELATIONS[f.relationIndex].value
      })
      if (res.code === 200) {
        this.setData({ bindVisible: false })
        setCurrentPatient(res.data)
        wx.showToast({ title: '已绑定并选用', icon: 'success' })
        this.loadPatients()
      } else {
        wx.showToast({ title: res.message || '绑定失败', icon: 'none' })
      }
    } finally {
      this.setData({ submitting: false })
    }
  },

  noop() {}
})
