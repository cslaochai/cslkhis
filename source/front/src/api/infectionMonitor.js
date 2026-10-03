import request from './request'

/**
 * 院感监测（L10，菜单 614 / 路径 /infection-monitor）
 *
 * 口径（视图层不再各写一套）：
 * 1. 病例：1 待核实 → 2 已确认 / 3 已排除；漏报补报 = leakFlag=1 的报卡，不设独立状态。
 * 2. 监测：感染确认（infectionFlag）与在管状态（status）独立，拔管前可确认感染；
 *    导管日/感染率全由后端算好，前端只渲染。
 * 3. 手卫生：只增不改；依从率先聚合再算比率，后端出数。
 * 4. ID 全是雪花 ID 字符串，不要 Number()。
 */

// ---------------- 病例报告卡 ----------------
export const getCaseStats = () => request.get('/emr/infection/case/stats')
export const getCaseListPage = (params) => request.post('/emr/infection/case/listPage', params)
export const getCaseDetail = (id) => request.get('/emr/infection/case/getDetailById', { params: { id } })
export const caseUpsert = (data) => request.post('/emr/infection/case/upsert', data)
export const caseAudit = (data) => request.post('/emr/infection/case/audit', data)

// ---------------- 目标性监测 ----------------
export const getMonitorListPage = (params) => request.post('/emr/infection/monitor/listPage', params)
export const getMonitorDetail = (id) => request.get('/emr/infection/monitor/getDetailById', { params: { id } })
export const monitorAdd = (data) => request.post('/emr/infection/monitor/add', data)
export const monitorPunchDaily = (data) => request.post('/emr/infection/monitor/punchDaily', data)
export const monitorRemove = (data) => request.post('/emr/infection/monitor/remove', data)
export const monitorConfirmInfection = (data) => request.post('/emr/infection/monitor/confirmInfection', data)
export const getMonitorStats = () => request.get('/emr/infection/monitor/stats')
export const getMonitorDailyList = (monitorId) => request.get('/emr/infection/monitor/dailyList', { params: { monitorId } })

// ---------------- 手卫生依从性 ----------------
export const getHandObsListPage = (params) => request.post('/emr/infection/handObs/listPage', params)
export const handObsAdd = (data) => request.post('/emr/infection/handObs/add', data)
export const getHandObsStats = (params) => request.post('/emr/infection/handObs/stats', params || {})
