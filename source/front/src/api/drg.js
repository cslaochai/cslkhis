import request from './request'

// ============ DRG-DIP 分组模拟（G23）：院内简化模拟器，每首页一条结果重跑覆盖 ============

// 单条模拟（首页主诊断为空时补 icdCode/icdName）
export function drgSimulate(data) {
  return request.post('/report/drg/simulate', data)
}

// 批量模拟（不传 summaryIds 默认最近 50 条）
export function drgSimulateBatch(data) {
  return request.post('/report/drg/simulateBatch', data || {})
}

// 模拟结果分页
export function drgResultListPage(data) {
  return request.post('/report/drg/result/listPage', data)
}

// 可模拟首页列表 + 入组/盈亏统计
export function drgSummaryList(limit, version) {
  return request.get('/report/drg/summary/list', { params: { limit, version } })
}

// 组表（biz_drg_group）
export function drgGroupList(version) {
  return request.get('/report/drg/group/list', { params: { version } })
}

// 方案健康检查（完整度 / 规则来源 / 复核状态 / 缺失集合）
export function drgSchemeHealth(version) {
  return request.get('/report/drg/scheme/health', { params: { version } })
}

// MDC 主诊断 ICD 前缀分布（实时聚合，仅用于目录页展示提示，不得用于业务）
export function drgMdcDiagPrefixes(version, mdcCode) {
  return request.get('/report/drg/mdc/diag-prefixes', { params: { version, mdcCode } })
}
