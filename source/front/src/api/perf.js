import request from './request'

// ============ 绩效与成本核算（G23）：收入=收费明细月度净额；绩效=max(0,结余)×系数 ============

// 科室月度成本录入（同科室同月唯一，重复拒绝）
export function perfCostSave(data) {
  return request.post('/report/perf/cost/save', data)
}

// 成本分页
export function perfCostListPage(data) {
  return request.post('/report/perf/cost/listPage', data)
}

// 核算预览（收入聚合 + 成本快照）
export function perfRevenueInfo(deptId, costMonth) {
  return request.get('/report/perf/revenueInfo', { params: { deptId, costMonth } })
}

// 执行核算（重算覆盖）
export function perfCalc(data) {
  return request.post('/report/perf/calc', data)
}

// 绩效结果分页
export function perfResultListPage(data) {
  return request.post('/report/perf/result/listPage', data)
}
