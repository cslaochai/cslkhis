import request from './request'

// 查询发票列表
export function getInvoiceList(params) {
  return request.post('/charge/invoice/listPage', params)
}

// 获取发票详情
export function getInvoiceDetail(id) {
  return request.get('/charge/invoice/getById', { params: { id } })
}

// 按账单出票（L4：一票对一账单，票面金额服务端从账单与支付流水现取，前端不传金额）
export function issueInvoice(data) {
  return request.post('/charge/invoice/issue', data)
}

// 打印发票
export function printInvoice(id) {
  return request.post('/charge/invoice/printInvoice', null, { params: { id } })
}

// 作废发票
export function voidInvoice(id, reason) {
  return request.post('/charge/invoice/voidInvoice', { id, reason })
}
