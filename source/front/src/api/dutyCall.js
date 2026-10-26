import request from './request'

/**
 * 临床值班听班呼叫（sql/297，菜单 29616）。
 *
 * 呼叫链路：发起呼叫 → 到岗应答 → 超时升级。被叫人不由前端传，
 * 后端按「科室 + 层级 + 班次」从排班解析当班听班人。
 */

/** 可呼叫的听班目标下拉（这一班这一科室有几线听班、各是谁） */
export function getDutyCallTargets(params) {
  return request.get('/system/dutyCall/listTargets', { params })
}

/** 发起听班呼叫 */
export function dutyCallCreate(data) {
  return request.post('/system/dutyCall/call', data)
}

/** 到岗应答（只有被叫值班人本人可应答） */
export function dutyCallArrive(data) {
  return request.post('/system/dutyCall/arrive', data)
}

/** 取消呼叫（只有发起人可取消） */
export function dutyCallCancel(data) {
  return request.post('/system/dutyCall/cancel', data)
}

/** 呼叫单分页 */
export function getDutyCallListPage(params) {
  return request.post('/system/dutyCall/listPage', params)
}

/** 呼叫单详情（含呼叫流水） */
export function getDutyCallDetail(id) {
  return request.get('/system/dutyCall/detail', { params: { id } })
}

/** 手工补跑超时升级（与定时任务同一入口，幂等） */
export function dutyCallEscalateOverdue() {
  return request.post('/system/dutyCall/escalateOverdue')
}
