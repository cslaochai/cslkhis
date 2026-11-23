import request from './request'

/**
 * 门诊取药队列（活表 biz_dispense_queue）。
 *
 * 与 dispensing.js 的分工：那边是「发了什么药、扣了多少库存」，
 * 这边是「患者到没到、被哪个窗口叫走」。发药动作仍走 /charge/dispensing/*，
 * 整单发完后由后端回写队列状态，前端不用再调一次。
 */

// 签到取号（prescriptionId / prescriptionNo / patientId 三选一）
export function dispenseSignIn(data) {
  return request.post('/charge/dispenseQueue/signIn', data)
}

// 该患者名下待取药处方（含是否已签到、已发的号）
export function getWaitingRx(patientId) {
  return request.get('/charge/dispenseQueue/listWaitingByPatient', {params: {patientId}})
}

// 该处方当天的取药号（未签到返回 null）
export function getQueueByPrescription(prescriptionId) {
  return request.get('/charge/dispenseQueue/getByPrescription', {params: {prescriptionId}})
}

// 取药队列分页（不传 queueDate 按当天）
export function getQueueList(params) {
  return request.post('/charge/dispenseQueue/listPage', params)
}

// 叫下一位（号绑到该窗口）
export function callNext(windowId) {
  return request.post('/charge/dispenseQueue/callNext', {windowId})
}

// 重叫（该窗口当前正在叫的号再叫一次）
export function recall(windowId) {
  return request.post('/charge/dispenseQueue/recall', {windowId})
}

// 该窗口当前正在叫的号（叫号屏轮询）
export function getCurrentCall(windowId) {
  return request.get('/charge/dispenseQueue/currentCall', {params: {windowId}})
}

// 过号
export function skipQueue(queueId, reason) {
  return request.post('/charge/dispenseQueue/skip', {queueId, reason})
}

// 取消取药号
export function cancelQueue(queueId, reason) {
  return request.post('/charge/dispenseQueue/cancel', {queueId, reason})
}

// 过号后恢复排队（号不变，排到队尾）
export function reviveQueue(queueId) {
  return request.post('/charge/dispenseQueue/revive', {queueId})
}
