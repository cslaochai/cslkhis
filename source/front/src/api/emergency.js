import request from './request'

export function getEmergencyList(params) {
  return request.post('/emergency/list', params)
}

export function registerEmergency(data) {
  return request.post('/emergency/register', data)
}

/** extra：转留观时传 { observationWardId, observationBedId }，其余状态不传 */
export function updateEmergencyStatus(id, status, extra = {}) {
  return request.post('/emergency/updateStatus', { id, status, ...extra })
}

/** 急诊转住院：真实入院登记（入院途径=急诊），返回入院ID（字符串，防雪花精度丢失） */
export function admitEmergency(data) {
  return request.post('/emergency/admit', data)
}

/** 病区下拉（转住院选入院病区；留观在结果里筛「急诊」前缀病区） */
export function getEmergencyWardSelectList() {
  return request.get('/emergency/wardSelectList')
}

/** 床位下拉：wardId 必填，bedStatus=1 只要空闲床 */
export function getEmergencyBedSelectList(params) {
  return request.get('/emergency/bedSelectList', { params })
}

export function getEmergencyStats() {
  return request.get('/emergency/stats')
}

/** 此刻在岗的值班医生（登记表单；与后端登记自动派单同一条判定） */
export function getEmergencyDutySelectList(deptId) {
  return request.get('/emergency/dutySelectList', { params: { deptId } })
}

/** 待交班清单：本科室未闭环中「无人指派」+「挂我名下」的行 */
export function getEmergencyHandoverPendingList(deptId) {
  return request.get('/emergency/handoverPendingList', { params: { deptId } })
}

/** 接班人候选（在岗优先） */
export function getEmergencyHandoverTakeList(deptId) {
  return request.get('/emergency/handoverTakeList', { params: { deptId } })
}

/** 提交交班：items 必须逐条点名覆盖清单，漏一条后端整体拒绝 */
export function saveEmergencyHandover(data) {
  return request.post('/emergency/handoverSave', data)
}

export function getEmergencyHandoverListPage(params) {
  return request.post('/emergency/handoverListPage', params)
}

/** 交班单详情（抬头 + 逐条明细凭证） */
export function getEmergencyHandoverDetail(id) {
  return request.get('/emergency/handoverGetDetailById', { params: { id } })
}
