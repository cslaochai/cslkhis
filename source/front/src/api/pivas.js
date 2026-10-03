import request from './request'

/**
 * 静配中心 PIVAS —— 链路：候选预览/生成 → 审方(通过/退回) → 打标签排队取号 → 调配 → 核对发放
 * 计费不在本链（仍走住院摆药），这里只做调配过程留痕。
 */

/** 可静配医嘱候选（生成前预览） */
export function listPivasCandidates(params) {
  return request({ url: '/pharmacy/pivas/candidates', method: 'get', params })
}

/** 生成静配单（同入院同日复用主单、明细追加，幂等；新明细待审方） */
export function generatePivas(data) {
  return request({ url: '/pharmacy/pivas/generate', method: 'post', data })
}

/** 静配单分页 */
export function listPivasPage(data) {
  return request({ url: '/pharmacy/pivas/listPage', method: 'post', data })
}

/** 静配单详情（主单+明细） */
export function getPivasDetail(id) {
  return request({ url: '/pharmacy/pivas/getDetailById', method: 'get', params: { id } })
}

/** 审方（pass=true 通过 1→2 / false 退回 1→0，退回原因必填） */
export function auditPivasItem(data) {
  return request({ url: '/pharmacy/pivas/auditItem', method: 'post', data })
}

/** 打标签排队（整单已审方明细 2→3 取排队号） */
export function labelPivasBatch(data) {
  return request({ url: '/pharmacy/pivas/labelBatch', method: 'post', data })
}

/** 调配（3→4） */
export function compoundPivasItem(data) {
  return request({ url: '/pharmacy/pivas/compoundItem', method: 'post', data })
}

/** 核对发放（4→5，终态） */
export function verifyPivasItem(data) {
  return request({ url: '/pharmacy/pivas/verifyItem', method: 'post', data })
}

/** 统计（按调配日期+可选病区） */
export function getPivasStats(params) {
  return request({ url: '/pharmacy/pivas/stats', method: 'get', params })
}
