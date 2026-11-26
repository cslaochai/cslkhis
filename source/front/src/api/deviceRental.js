import request from './request'

// ============ 设备共享调配（设备科共享池） ============
// 临床申请 → 审批 → 设备科发放（IoT 泵同时绑定患者）→ 归还 → 设备科验收。

export function deviceRentalCatalog(keyword) {
  return request.get('/iot/deviceRental/catalog', { params: { keyword } })
}

export function deviceRentalListPage(data) {
  return request.post('/iot/deviceRental/listPage', data)
}

export function deviceRentalDetail(id) {
  return request.get('/iot/deviceRental/getDetailById', { params: { id } })
}

export function deviceRentalCreate(data) {
  return request.post('/iot/deviceRental/create', data)
}

export function deviceRentalApprove(data) {
  return request.post('/iot/deviceRental/approve', data)
}

export function deviceRentalIssue(data) {
  return request.post('/iot/deviceRental/issue', data)
}

export function deviceRentalReturn(data) {
  return request.post('/iot/deviceRental/return', data)
}

export function deviceRentalAccept(data) {
  return request.post('/iot/deviceRental/accept', data)
}
