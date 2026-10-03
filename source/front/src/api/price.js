import request from './request'

// 分页查询价格（按项目类型：DRUG/CONSUMABLE/INSPECTION/LABORATORY/TREATMENT）
export function getPriceList(params) {
  return request.post('/system/price/listPage', params)
}

// 调价（更新价格并记录调价历史）
export function changePrice(data) {
  return request.post('/system/price/changePrice', data)
}

// 分页查询调价历史
export function getPriceHistoryList(params) {
  return request.post('/system/price/historyListPage', params)
}

// 5 类价表总条数
export function getPriceSummary() {
  return request.get('/system/price/summary')
}
