import request from './request'

// ============ BI 驾驶舱（G23）：一个总览接口全量返回 ============

// 驾驶舱总览（今日挂号/在院/出院/收入/药占比/床位/7日趋势/科室TOP5）
export function getBiOverview() {
  return request.get('/report/bi/overview')
}
