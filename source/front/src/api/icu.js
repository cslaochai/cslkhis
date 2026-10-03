import request from './request'

/**
 * ICU 专科监护 —— 入出科登记 + 床边监护记录单 + 床位看板 + 科室指标。
 * 床位复用 sys_bed（bed_type='ICU'）；GCS 总分与液体平衡由服务端回算，出科后记录封账。
 */

/** 入出科台账分页 */
export function listIcuStayPage(data) {
  return request({ url: '/patient/icu/stay/listPage', method: 'post', data })
}

/** 入科记录详情 */
export function getIcuStay(id) {
  return request({ url: '/patient/icu/stay/getById', method: 'get', params: { id } })
}

/** 可入科候选（在院且无在科记录） */
export function listIcuAdmissions(params) {
  return request({ url: '/patient/icu/stay/admissions', method: 'get', params })
}

/** 入科登记 / 在科期间修改 */
export function upsertIcuStay(data) {
  return request({ url: '/patient/icu/stay/upsert', method: 'post', data })
}

/** 出科登记（终态） */
export function outIcuStay(data) {
  return request({ url: '/patient/icu/stay/out', method: 'post', data })
}

/** ICU 床位看板 */
export function getIcuBedBoard(wardId) {
  return request({ url: '/patient/icu/bedBoard', method: 'get', params: { wardId } })
}

/** 监护记录分页 */
export function listIcuMonitorPage(data) {
  return request({ url: '/patient/icu/monitor/listPage', method: 'post', data })
}

/** 单患者监护趋势（按时间正序） */
export function getIcuMonitorTrend(stayId, hours) {
  return request({ url: '/patient/icu/monitor/trend', method: 'get', params: { stayId, hours } })
}

/** 登记/修改监护记录 */
export function upsertIcuMonitor(data) {
  return request({ url: '/patient/icu/monitor/upsert', method: 'post', data })
}

/** 科室指标 */
export function getIcuStats(params) {
  return request({ url: '/patient/icu/stats', method: 'get', params })
}
