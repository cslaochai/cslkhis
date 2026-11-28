import request from './request'

// ============ 设备后勤（G22）：设备档案建档/维保/计量 ============
// 台账落 sys_equipment：设备管理页可新增/编辑/停用，维保登记回写档案最近维保日期。
// 8-物流设备（机器人）的设备编码同时是物联网接入编号，保存时后端同步接入台账。

// 设备台账分页
export function equipmentListPage(data) {
  return request.post('/equipment/equipment/listPage', data)
}

// 设备详情（含最近维保/计量记录）
export function getEquipmentDetail(equipmentId) {
  return request.get('/equipment/equipment/getDetailById', { params: { equipmentId } })
}

// 维保登记
export function maintainCreate(data) {
  return request.post('/equipment/maintain/create', data)
}

// 维保记录删除（录错可删）
export function maintainDelete(id) {
  return request.delete('/equipment/maintain/deleteById', { params: { id } })
}

// 计量登记
export function meteringCreate(data) {
  return request.post('/equipment/metering/create', data)
}

// 计量记录删除（录错可删）
export function meteringDelete(id) {
  return request.delete('/equipment/metering/deleteById', { params: { id } })
}

// 维护设备是否进入共享池
export function updateEquipmentRentable(data) {
  return request.post('/equipment/equipment/updateRentable', data)
}

// 设备建档/修改：同一接口，id 为空走新增
export function equipmentUpsert(data) {
  return request.post('/equipment/equipment/equipmentUpsert', data)
}

// 设备状态变更（停用/启用/维修中/报废）
export function updateEquipmentStatus(data) {
  return request.post('/equipment/equipment/updateStatus', data)
}
