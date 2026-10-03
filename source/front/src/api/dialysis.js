import request from './request'

/**
 * 血液净化（透析）中心 —— 链路：透析档案 → 透析处方 → 机位排班 → 上机/下机 → 不良反应 → 工作量统计。
 * 只出治疗过程记录与台账，不生成收费单、不扣耗材（计费走治疗医嘱主链）。操作人服务端取当前登录人。
 */

/** 透析档案分页（电话出参已脱敏） */
export function listDialysisArchivePage(data) {
  return request({ url: '/medicaltech/dialysis/archive/listPage', method: 'post', data })
}

/** 档案详情（编辑回显，电话明文） */
export function getDialysisArchive(id) {
  return request({ url: '/medicaltech/dialysis/archive/getById', method: 'get', params: { id } })
}

/** 档案新增/修改（一人一档） */
export function upsertDialysisArchive(data) {
  return request({ url: '/medicaltech/dialysis/archive/upsert', method: 'post', data })
}

/** 档案状态变更（1在透/2暂停/3退出） */
export function changeDialysisArchiveStatus(data) {
  return request({ url: '/medicaltech/dialysis/archive/changeStatus', method: 'post', data })
}

/** 某档案的处方台账 */
export function listDialysisPrescriptions(archiveId) {
  return request({ url: '/medicaltech/dialysis/prescription/list', method: 'get', params: { archiveId } })
}

/** 处方新增/修改（新开自动停用旧的有效处方） */
export function upsertDialysisPrescription(data) {
  return request({ url: '/medicaltech/dialysis/prescription/upsert', method: 'post', data })
}

/** 处方停用 */
export function stopDialysisPrescription(data) {
  return request({ url: '/medicaltech/dialysis/prescription/stop', method: 'post', data })
}

/** 机位台账分页 */
export function listDialysisMachinePage(data) {
  return request({ url: '/medicaltech/dialysis/machine/listPage', method: 'post', data })
}

/** 可用机位下拉 */
export function listDialysisMachineSelect() {
  return request({ url: '/medicaltech/dialysis/machine/selectList', method: 'get' })
}

/** 机位新增/修改 */
export function upsertDialysisMachine(data) {
  return request({ url: '/medicaltech/dialysis/machine/upsert', method: 'post', data })
}

/** 透析单分页台账 */
export function listDialysisSessionPage(data) {
  return request({ url: '/medicaltech/dialysis/session/listPage', method: 'post', data })
}

/** 透析单详情 */
export function getDialysisSession(id) {
  return request({ url: '/medicaltech/dialysis/session/getById', method: 'get', params: { id } })
}

/** 日看板（三时段 × 全机位） */
export function getDialysisBoard(date) {
  return request({ url: '/medicaltech/dialysis/session/board', method: 'get', params: { date } })
}

/** 排班（占机位，处方快照） */
export function scheduleDialysisSession(data) {
  return request({ url: '/medicaltech/dialysis/session/schedule', method: 'post', data })
}

/** 改期/改机位（仅已排班） */
export function rescheduleDialysisSession(data) {
  return request({ url: '/medicaltech/dialysis/session/reschedule', method: 'post', data })
}

/** 上机 */
export function startDialysisSession(data) {
  return request({ url: '/medicaltech/dialysis/session/start', method: 'post', data })
}

/** 下机（超滤量与实际时长服务端回算） */
export function finishDialysisSession(data) {
  return request({ url: '/medicaltech/dialysis/session/finish', method: 'post', data })
}

/** 登记不良反应 */
export function recordDialysisAdverse(data) {
  return request({ url: '/medicaltech/dialysis/session/adverse', method: 'post', data })
}

/** 取消治疗单（原因必填） */
export function cancelDialysisSession(data) {
  return request({ url: '/medicaltech/dialysis/session/cancel', method: 'post', data })
}

/** 工作量统计 */
export function getDialysisStats(data) {
  return request({ url: '/medicaltech/dialysis/stats', method: 'post', data })
}
