import request from './request'

// ============ 医疗废物登记（G22）：登记→交接→处置，已交接后禁删 ============

// 医废登记
export function wasteCreate(data) {
  return request.post('/waste/waste/create', data)
}

// 交接（1→2）
export function wasteHandover(data) {
  return request.post('/waste/waste/handover', data)
}

// 处置确认（2→3）
export function wasteDispose(data) {
  return request.post('/waste/waste/dispose', data)
}

// 删除（仅已登记状态可删）
export function wasteDelete(id) {
  return request.delete('/waste/waste/deleteById', { params: { id } })
}

// 分页查询
export function wasteListPage(data) {
  return request.post('/waste/waste/listPage', data)
}
