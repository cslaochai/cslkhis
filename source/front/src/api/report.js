import request from './request'

// 报表统计总览（门诊/住院/收入/药事，按日期区间聚合，一次返回）
export function getStatsOverview(params) {
  return request.post('/report/statsOverview', params)
}
