import { reportApi, aiApi } from '../../utils/api'
import { getToken } from '../../utils/auth'
import { BASE_URL } from '../../utils/request'

// 影像帧是静态资源（后端 /uploads/**），与接口同域并走同一个 /api 前缀，必须拼绝对地址
const API_BASE = BASE_URL

// 异常与否一律以后端 abnormalFlagText 判断，不拿 abnormalFlag 写三目：
// 「未判定」在库里 abnormal_flag 也是 0，照 flag 判会把「不知道」显示成「正常」
const ABNORMAL_TEXT = ['偏高', '偏低', '异常']
const ARROW_OF = { 偏高: '↑', 偏低: '↓', 异常: '!' }

Page({
  data: {
    id: '',
    type: '',
    report: null,
    images: [],
    labItems: [],
    abnormalCount: 0,
    loading: true,
    // 大白话解读结果。检验（type 2）走词典逐项；检查（type 1）走影像白话串话
    // （G-17：词典只讲「这项检查查什么」，描述/结论的白话由模型转述、后端硬闸把关，
    //  模型不可用时白话缺位但事实与原文引导照常 —— 前端照常渲染，不弹失败）
    explain: null,
    explaining: false,
    imagingExplain: null,
    imagingExplaining: false
  },

  onLoad(options) {
    this.setData({ id: options.id, type: options.type || '2' })
    this.loadDetail()
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const res = await reportApi.getById(this.data.id)
      if (res.code === 200 && res.data) {
        const r = res.data
        // 影像随报告一起出参（sql/137 简化 PACS）：后端已把「报告→记录→申请单」三跳算完，
        // 这里只是把相对路径拼成小程序能加载的绝对地址
        const images = (r.images || []).map(img => ({
          ...img,
          absUrl: `${API_BASE}/${String(img.fileUrl || '').replace(/^\/+/, '')}`
        }))
        // 检验结果明细：逐项的结果值/参考区间/异常判定本来就在库里，只是此前没带出来
        const labItems = (r.labItems || []).map(it => ({
          itemName: it.laboratoryItemName || '-',
          resultValue: it.resultValue || '',
          resultUnit: it.resultUnit || '',
          referenceRange: it.referenceRange || '',
          abnormalFlagText: it.abnormalFlagText || '',
          abnormalDesc: it.abnormalDesc || '',
          abnormal: ABNORMAL_TEXT.indexOf(it.abnormalFlagText) >= 0,
          arrow: ARROW_OF[it.abnormalFlagText] || ''
        }))
        this.setData({
          images,
          labItems,
          abnormalCount: labItems.filter(i => i.abnormal).length,
          report: {
            reportNo: r.reportNo,
            reportType: r.reportType,
            typeText: r.reportType === 1 ? '检查报告' : '检验报告',
            itemName: r.itemName,
            examDeptName: r.examDeptName,
            applyDoctorName: r.applyDoctorName,
            clinicalDiagnosis: r.clinicalDiagnosis,
            reportContent: r.reportContent,
            conclusion: r.conclusion,
            suggestions: r.suggestions,
            reportStatus: r.reportStatus,
            statusText: ['', '待审核', '初审通过', '已审核', '已发布', '已作废'][r.reportStatus] || '未知',
            visitDate: r.visitDate ? String(r.visitDate) : '',
            patientName: r.patientName,
            gender: r.gender,
            age: r.age,
            publishTime: r.publishTime ? String(r.publishTime).replace('T', ' ') : '',
            isUrgent: r.isUrgent
          }
        })
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    } catch (e) {
      console.error('加载报告详情失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 大白话解读：事实（哪些项异常、是否危急值）由后端规则算，白话来自医院维护的词典，
  // 模型只润色一句话。所以模型不可用（degraded=true）时照样有完整解读，
  // 这里刻意不弹失败提示 —— 弹「AI 服务不可用」只会让本来就焦虑的患者更慌。
  async loadExplain() {
    if (this.data.explaining) return
    this.setData({ explaining: true })
    try {
      const res = await aiApi.reportExplain({ reportId: String(this.data.id) })
      if (res.code === 200 && res.data) {
        this.setData({ explain: res.data })
      } else {
        wx.showToast({ title: res.message || '暂无法解读', icon: 'none' })
      }
    } catch (e) {
      console.error('报告解读失败', e)
      wx.showToast({ title: '暂无法解读', icon: 'none' })
    } finally {
      this.setData({ explaining: false })
    }
  },

  closeExplain() {
    this.setData({ explain: null })
  },

  // 影像白话解读（检查报告）：事实（阴阳性/危急）由后端代码给，检查介绍来自院内词典，
  // 描述/结论的白话是模型转述且逐段过了患者文案硬闸。所以模型不可用（degraded=true）
  // 时照样渲染卡片 —— 白话段落缺位、检查介绍与原文引导照常，刻意不弹失败提示
  async loadImagingExplain() {
    if (this.data.imagingExplaining) return
    this.setData({ imagingExplaining: true })
    try {
      const res = await aiApi.imagingExplain({ reportId: String(this.data.id) })
      if (res.code === 200 && res.data) {
        this.setData({ imagingExplain: res.data })
      } else {
        wx.showToast({ title: res.message || '暂无法解读', icon: 'none' })
      }
    } catch (e) {
      console.error('影像报告解读失败', e)
      wx.showToast({ title: '暂无法解读', icon: 'none' })
    } finally {
      this.setData({ imagingExplaining: false })
    }
  },

  closeImagingExplain() {
    this.setData({ imagingExplain: null })
  },

  // 影像全屏预览（wx.previewImage 自带双指缩放，患者端不需要窗宽窗位）
  previewImage(e) {
    const urls = this.data.images.map(i => i.absUrl)
    const current = e.currentTarget.dataset.url
    if (!urls.length) return
    wx.previewImage({ urls, current })
  },

  copyContent() {    const r = this.data.report
    if (!r) return
    const text = `结论：${r.conclusion || ''}\n建议：${r.suggestions || ''}\n描述：${r.reportContent || ''}`
    wx.setClipboardData({ data: text })
  },

  // 报告原文 PDF（打印桩：服务端生成占位文档，真对接换 PDF 服务）
  openPdf() {
    const token = getToken()
    wx.downloadFile({
      url: `http://localhost:8080/api/miniapp/report/pdf?reportId=${this.data.id}`,
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(res) {
        if (res.statusCode !== 200) {
          wx.showToast({ title: res.statusCode === 400 ? '报告尚未发布' : '获取原文失败', icon: 'none' })
          return
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          fileType: 'pdf',
          fail: () => wx.showToast({ title: '打开失败', icon: 'none' })
        })
      },
      fail: () => wx.showToast({ title: '下载失败', icon: 'none' })
    })
  }
})
