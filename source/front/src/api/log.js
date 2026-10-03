import request from './request'

// ========== 日志审计（sql/158，后端 /system/log）==========
// 三本账：操作日志 sys_oper_log / 登录日志 sys_login_log / 审计日志 sys_audit_log。
// 等保三级 8.1.4 要的"审计覆盖到每个用户 + 记录重要安全事件"就落在这三个接口上。
// 只读 + 导出，后端不提供任何删除接口（审计记录不允许应用侧删改）。

// 操作日志分页（只记写动作：读接口在后端拦截器的死表里挡掉了）
export function listOperLogPage(params) {
  return request.post('/system/log/operLogListPage', params)
}

// 操作日志详情（含请求参数与返回内容，凭据类字段已打码）
export function getOperLogDetail(id) {
  return request.get('/system/log/operLogDetail', { params: { id } })
}

// 登录日志分页（成功失败同表，靠 loginStatus 区分）
export function listLoginLogPage(params) {
  return request.post('/system/log/loginLogListPage', params)
}

// 审计日志分页（业务模块显式写的那本账：谁对哪个对象做了什么）
export function listAuditLogPage(params) {
  return request.post('/system/log/auditLogListPage', params)
}

export function getAuditLogDetail(id) {
  return request.get('/system/log/auditLogDetail', { params: { id } })
}

// 字段级修改日志分页（sql/159 第四本账：哪个字段从什么值改成了什么值）
// 入参复用同一套：targetType=对象类型 targetId=对象ID fieldName=字段名
export function listFieldChangePage(params) {
  return request.post('/system/log/fieldChangeListPage', params)
}

// 同批次明细：一次保存改了 5 个字段会落 5 行，按 batchNo 拉回来看完整一刀
export function getFieldChangeBatch(batchNo) {
  return request.get('/system/log/fieldChangeBatch', { params: { batchNo } })
}

// 四本账统计（含近24小时口令爆破嫌疑账号）
export function getLogStat() {
  return request.get('/system/log/stat')
}

// 导出 CSV（logType 1-操作 2-登录 3-审计 4-字段变更，最多 5000 行），返回文件文本
export function exportLogCsv(data) {
  return request.post('/system/log/exportCsv', data)
}
