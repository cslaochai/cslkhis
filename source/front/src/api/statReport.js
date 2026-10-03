import request from './request'

// ========== L9 病案统计上报（打印预留） ==========

// 台账分页（keyword/reportType/status/periodValue/pageNum/pageSize）
export function getStatReportList(params) {
  return request.post('/report/statReport/listPage', params)
}

// 生成上报报文（reportType/periodType/periodValue/deptId/remark）
export function generateStatReport(data) {
  return request.post('/report/statReport/generate', data)
}

// 报出（打印预留：冻结留痕，不对外发送）
export function submitStatReport(id) {
  return request.post('/report/statReport/submit', { id })
}

// 作废（reason 必填）
export function voidStatReport(id, reason) {
  return request.post('/report/statReport/void', { id, reason })
}

// 明细（含报文原文 payload）
export function getStatReportDetail(id) {
  return request.get('/report/statReport/getDetailById', { params: { id } })
}
