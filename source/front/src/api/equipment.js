import request from './request'

// ============ 设备后勤（G22）：设备档案维保计量 ============
// 台账只读（sys_equipment 49 号铺底）；维保登记回写档案最近维保日期。

// 设备台账分页
export function equipmentListPage(data) {
  return request.post('/equipment/equipment/listPage', data)
}

// 设备详情（含最近维保/计量记录）
export function getEquipmentDetail(equipmentId) {
  return request.get('/equipment/equipment/getDetailById', { params: { equipmentId } })
}

// 维保记录分页
export function maintainListPage(data) {
  return request.post('/equipment/maintain/listPage', data)
}

// 维保登记
export function maintainCreate(data) {
  return request.post('/equipment/maintain/create', data)
}

// 维保记录删除（录错可删）
export function maintainDelete(id) {
  return request.delete('/equipment/maintain/deleteById', { params: { id } })
}

// 计量记录分页
export function meteringListPage(data) {
  return request.post('/equipment/metering/listPage', data)
}

// 计量登记
export function meteringCreate(data) {
  return request.post('/equipment/metering/create', data)
}

// 计量记录删除（录错可删）
export function meteringDelete(id) {
  return request.delete('/equipment/metering/deleteById', { params: { id } })
}
