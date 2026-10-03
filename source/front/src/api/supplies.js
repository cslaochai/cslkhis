import request from './request'

// ========== 耗材字典 ==========

// 字典分页（keyword/category/status）
export function getConsumableList(params) {
  return request.post('/supplies/consumableListPage', params)
}

// 耗材详情
export function getConsumableById(id) {
  return request.get('/supplies/getConsumableById', { params: { id } })
}

// 字典新增/修改（id 空=新增）
export function consumableUpsert(data) {
  return request.post('/supplies/consumableUpsert', data)
}

// 启用耗材下拉
export function getConsumableSelectList() {
  return request.get('/supplies/consumable/selectList')
}

// ========== 批次库存 ==========

// 库存分页（keyword/category/stockStatus）
export function getStockList(params) {
  return request.post('/supplies/stockListPage', params)
}

// 库存详情
export function getStockById(stockId) {
  return request.get('/supplies/getStockById', { params: { stockId } })
}

// 建批入库
export function createStock(data) {
  return request.post('/supplies/stockUpsert', data)
}

// 补货入库
export function inboundStock(stockId, quantity) {
  return request.post('/supplies/stockInbound', { stockId, quantity })
}

// 其他出库
export function outboundStock(stockId, quantity) {
  return request.post('/supplies/stockOutbound', { stockId, quantity })
}

// ========== 科室领用 ==========

// 科室领用（扣库存 FEFO）
export function consume(data) {
  return request.post('/supplies/consume', data)
}

// 领用台账分页（keyword/deptId）
export function getConsumeList(params) {
  return request.post('/supplies/consumeListPage', params)
}

// 科室下拉已收敛到统一入口：@/api/system 的 getDepartmentSelectList
// （原 /supplies/deptSelectList 返回下划线 dept_name，与全仓 deptName 不一致，
//   且不经数据权限 —— 2026-09-21 移除，勿再新增模块私有的科室下拉接口）

// ========== 流水 ==========

// 出入库流水分页（keyword/changeType）
export function getStockLogList(params) {
  return request.post('/supplies/stockLogListPage', params)
}

// ========== L11 高值耗材 UDI 扫码溯源 ==========

// 扫码解析：回显 UDI-DI/序列号/批号/效期 + 命中字典 + 有货批次列表
export function udiScan(udiCode) {
  return request.post('/supplies/udiScan', { udiCode })
}

// 高值耗材使用登记（扣批次 1 件 + 计费尝试）
export function traceUse(data) {
  return request.post('/supplies/traceUse', data)
}

// 溯源台账分页（keyword/consumableId/patientId/chargeStatus/status）
export function getTraceList(params) {
  return request.post('/supplies/traceListPage', params)
}

// 溯源详情（字典→批次→患者→计费 全链）
export function getTraceDetailById(traceId) {
  return request.get('/supplies/getTraceDetailById', { params: { traceId } })
}

// 溯源记录作废（退货回库；已计费拒绝）
export function traceVoid(data) {
  return request.post('/supplies/traceVoid', data)
}

// 计费失败补记（按台账快照重走计费）
export function traceRecharge(traceId) {
  return request.post('/supplies/traceRecharge', { traceId })
}
