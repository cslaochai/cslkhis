import { getPatientId } from '../../utils/auth'
import { reportApi } from '../../utils/api'
import { askSubscribe } from '../../utils/wechat'

Page({
  data: {
    activeTab: 'lab', // lab-检验(2) imaging-检查(1)
    labReports: [],
    imagingReports: [],
    loading: false
  },

  onShow() {
    askSubscribe('report_ready')
    this.loadReports()
  },

  async loadReports() {
    const patientId = getPatientId()
    if (!patientId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    const mapReport = (r) => ({
      id: r.id,
      reportNo: r.reportNo,
      reportType: r.reportType,
      itemName: r.itemName,
      examDeptName: r.examDeptName,
      applyDoctorName: r.applyDoctorName,
      visitDate: r.visitDate ? String(r.visitDate) : '',
      reportStatus: r.reportStatus,
      statusText: ['', '待审核', '初审通过', '已审核', '已发布', '已作废'][r.reportStatus] || '未知',
      published: r.reportStatus === 3
    })

    try {
      const labRes = await reportApi.list({ patientId, reportType: 2, pageNum: 1, pageSize: 50 })
      const imgRes = await reportApi.list({ patientId, reportType: 1, pageNum: 1, pageSize: 50 })
      if (labRes.code === 200 && labRes.data) {
        this.setData({ labReports: (labRes.data.records || []).map(mapReport) })
      }
      if (imgRes.code === 200 && imgRes.data) {
        this.setData({ imagingReports: (imgRes.data.records || []).map(mapReport) })
      }
    } catch (e) {
      console.error('加载报告失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onTabChange(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab })
  },

  onReportTap(e) {
    const report = e.currentTarget.dataset.report
    wx.navigateTo({ url: `/pages/reportDetail/reportDetail?id=${report.id}&type=${report.reportType}` })
  },

  onPullDownRefresh() {
    this.loadReports()
    wx.stopPullDownRefresh()
  }
})
