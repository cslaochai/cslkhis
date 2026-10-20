import request from './request'

// ============ DIP 分组模拟（菜单 908，镜像 DRG 模拟器）：每首页一条结果重跑覆盖 ============

// 单条模拟（首页主诊断为空时补 icdCode/icdName，首页手术操作为空时补 operCode/operName）
export function dipSimulate(data) {
  return request.post('/report/dip/simulate', data)
}

// 批量模拟（不传 summaryIds 默认最近 50 条）
export function dipSimulateBatch(data) {
  return request.post('/report/dip/simulateBatch', data || {})
}

// 模拟结果分页
export function dipResultListPage(data) {
  return request.post('/report/dip/result/listPage', data)
}

// 可模拟首页列表 + 入组/盈亏统计
export function dipSummaryList(limit, version) {
  return request.get('/report/dip/summary/list', { params: { limit, version } })
}

// 病种目录（biz_dip_disease）
export function dipDiseaseList(version) {
  return request.get('/report/dip/disease/list', { params: { version } })
}

// 当前 DIP 结算默认病种库版本（sys_config dip.default_version）
export function getDipDefaultVersion() {
  return request.get('/report/dip/version')
}

// 医保付费主参数（主付费方式 medpay.mode / DIP 病种库版本 dip.default_version / DIP 点值 dip.point_value）
export function dipMedpayConfig() {
  return request.get('/report/dip/config')
}

// 保存医保付费主参数
export function saveDipMedpayConfig(data) {
  return request.post('/report/dip/config', data)
}
