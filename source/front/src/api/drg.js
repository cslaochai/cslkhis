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
export function drgSummaryList(limit) {
  return request.get('/report/drg/summary/list', { params: { limit } })
}

// 组表（sys_drg_group）
export function drgGroupList() {
  return request.get('/report/drg/group/list')
}
