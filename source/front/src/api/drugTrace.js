import request from './request'

// ========== 药品追溯码采集与核对（sql/156，后端 /drugTrace）==========
// 医保局口径：入库扫码采集、发药扫码核销，两个动作都要上传。
// 闸门全在服务端（未采集不许核销 / 一码只采一次 / 串码拒绝），前端只负责把结论说清楚。

// 扫码解析（不落库）：返回码制解析段 + 药品命中 + 批次候选 + 采集/核销闸门结论
export function scanDrugTrace(data) {
  return request.post('/drugTrace/scan', data)
}

// 入库采集 / 存量补采
export function collectDrugTrace(data) {
  return request.post('/drugTrace/collect', data)
}

// 发药核销（扫追溯码绑定已发药记录）
export function verifyDispenseDrugTrace(data) {
  return request.post('/drugTrace/verifyDispense', data)
}

// 作废（1-退药 2-报损 3-召回）
export function voidDrugTrace(data) {
  return request.post('/drugTrace/void', data)
}

// 批量上传医保局（ids 为空 = 上传全部待上传/失败）
export function uploadDrugTrace(data) {
  return request.post('/drugTrace/upload', data)
}

// 台账分页
export function listDrugTracePage(params) {
  return request.post('/drugTrace/listPage', params)
}

// 台账详情
export function getDrugTraceDetail(id) {
  return request.get('/drugTrace/getDetailById', { params: { id } })
}

// 对账统计（码状态分布 / 上传分布 / 近30天未核销发药行数）
export function getDrugTraceReconcile() {
  return request.get('/drugTrace/reconcileStats')
}

// 删除误采记录（仅在库且未上传可删）
export function deleteDrugTrace(id) {
  return request.delete('/drugTrace/deleteById', { params: { id } })
}
