import request from './request'

/**
 * 门诊输液室（M10）—— 座位 → 入座 → 皮试（≥15 分钟观察窗）→ 开始（滴速）→ 巡视 → 结束（不良反应）。
 * 业务来源是 G19 治疗记录，本域只管护理过程，不重复计费、不扣库存。
 */

/** 今日看板（座位图 + 各状态计数） */
export function getInfusionBoard() {
  return request({ url: '/medicaltech/infusionRoom/board', method: 'get' })
}

/** 座位图（含占用输液单摘要） */
export function listInfusionSeats() {
  return request({ url: '/medicaltech/infusionRoom/seats', method: 'get' })
}

/** 座位新增/修改（占用中不可改） */
export function upsertInfusionSeat(data) {
  return request({ url: '/medicaltech/infusionRoom/seatUpsert', method: 'post', data })
}

/** 入座（建输液单 + 占座；needSkinTest=1 先皮试） */
export function admitInfusion(data) {
  return request({ url: '/medicaltech/infusionRoom/admit', method: 'post', data })
}

/** 打皮试 */
export function createSkinTest(data) {
  return request({ url: '/medicaltech/infusionRoom/skinTest', method: 'post', data })
}

/** 皮试判读（阳性自动取消输液单并释放座位） */
export function judgeSkinTest(data) {
  return request({ url: '/medicaltech/infusionRoom/skinTestResult', method: 'post', data })
}

/** 开始输注（待输注 + 皮试阴性；记录起始滴速） */
export function startInfusion(data) {
  return request({ url: '/medicaltech/infusionRoom/start', method: 'post', data })
}

/** 巡视（输液中；滴速/余量/备注） */
export function roundInfusion(data) {
  return request({ url: '/medicaltech/infusionRoom/round', method: 'post', data })
}

/** 结束输注（adverseFlag=1 时描述必填；释放座位） */
export function finishInfusion(data) {
  return request({ url: '/medicaltech/infusionRoom/finish', method: 'post', data })
}

/** 取消（非终态；必填原因；释放座位） */
export function cancelInfusion(data) {
  return request({ url: '/medicaltech/infusionRoom/cancel', method: 'post', data })
}

/** 今日输液单分页 */
export function listInfusionPage(data) {
  return request({ url: '/medicaltech/infusionRoom/listPage', method: 'post', data })
}

/** 某输液单的巡视记录（时间升序） */
export function listInfusionRounds(infusionId) {
  return request({ url: '/medicaltech/infusionRoom/rounds', method: 'get', params: { infusionId } })
}
