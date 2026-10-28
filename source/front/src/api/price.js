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

// 政策价列表（诊查费等政策类项目，政策×档位平铺）
export function getPolicyPriceList() {
  return request.get('/system/price/policyList')
}

// 政策价调价（写调价历史，itemType=POLICY）
export function changePolicyPrice(data) {
  return request.post('/system/price/policyChangePrice', data)
}

// 政策价档位启停（停用后运行时按「缺行」降级）
export function changePolicyPriceStatus(data) {
  return request.post('/system/price/policyChangeStatus', data)
}
