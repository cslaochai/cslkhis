import request from './request'

/**
 * 住院摆药（G13）—— 链路：候选预览/生成 → 配药(扣库存+计费) → 病区核对 → 退药(回库+负冲账)
 */

/** 可摆药医嘱候选（生成前预览） */
export function listCandidates(params) {
  return request({ url: '/pharmacy/wardDispense/candidates', method: 'get', params })
}

/** 生成摆药单（同入院同日复用主单、明细追加，幂等） */
export function generateWardDispense(data) {
  return request({ url: '/pharmacy/wardDispense/generate', method: 'post', data })
}

/** 摆药单分页 */
export function listWardDispensePage(data) {
  return request({ url: '/pharmacy/wardDispense/listPage', method: 'post', data })
}

/** 摆药单详情（主单+明细） */
export function getWardDispenseDetail(id) {
  return request({ url: '/pharmacy/wardDispense/getDetailById', method: 'get', params: { id } })
}

/** 配药（FEFO 扣库存+计费，1→2） */
export function dispenseItem(data) {
  return request({ url: '/pharmacy/wardDispense/dispenseItem', method: 'post', data })
}

/** 病区核对（2→3） */
export function checkItem(data) {
  return request({ url: '/pharmacy/wardDispense/checkItem', method: 'post', data })
}

/** 退药（回库+负冲账，2/3→4，终态不可逆） */
export function returnItem(data) {
  return request({ url: '/pharmacy/wardDispense/returnItem', method: 'post', data })
}

/** 统计（按日期+可选病区） */
export function getWardDispenseStats(params) {
  return request({ url: '/pharmacy/wardDispense/stats', method: 'get', params })
}
