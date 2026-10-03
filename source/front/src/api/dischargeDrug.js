import request from './request'

// ============ 出院带药（G20）：挂入院次，出院前即可开单；药房发药单向 ============

// 开带药单 / 修改（仅待发药可改）
export function dischargeDrugUpsert(data) {
  return request.post('/patient/dischargeDrug/upsert', data)
}

// 分页查询带药单
export function dischargeDrugListPage(data) {
  return request.post('/patient/dischargeDrug/listPage', data)
}

// 按入院次列全部带药单
export function dischargeDrugListByAdmission(admissionId) {
  return request.get('/patient/dischargeDrug/selectList', { params: { admissionId } })
}

// 带药单详情
export function getDischargeDrugDetail(id) {
  return request.get('/patient/dischargeDrug/getDetailById', { params: { id } })
}

// 批量发药（药房岗，单向）
export function dischargeDrugDispense(ids, remark) {
  return request.post('/patient/dischargeDrug/dispense', { ids, remark })
}

// 删除带药单（仅待发药）
export function dischargeDrugDelete(id) {
  return request.post('/patient/dischargeDrug/deleteById', { id })
}
