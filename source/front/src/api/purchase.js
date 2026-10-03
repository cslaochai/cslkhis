import request from './request'

/**
 * 药品采购链 API
 * 供应商 → 采购订单（含明细/审批）→ 入库单（审核/入库/取消）
 *
 * 语义提醒：采购订单的「生成入库单」不动库存；动库存的是入库单的「入库」（/drugInbound/stockIn）。
 */

// ========== 供应商 ==========

/** 分页查询供应商 */
export function getSupplierList(params) {
  return request.post('/supplier/listPage', params)
}

/** 供应商下拉（只含启用中的） */
export function getSupplierSelectList() {
  return request.post('/supplier/selectList')
}

/** 供应商详情 */
export function getSupplierDetail(supplierId) {
  return request.get('/supplier/getById', { params: { supplierId } })
}

/** 新增/修改供应商 */
export function upsertSupplier(data) {
  return request.post('/supplier/upsert', data)
}

/** 删除供应商（被采购订单引用会被拒） */
export function deleteSupplier(supplierId) {
  return request.delete('/supplier/deleteById', { params: { supplierId } })
}

// ========== 采购订单 ==========

/** 分页查询采购订单 */
export function getPurchaseOrderList(params) {
  return request.post('/purchase/listPage', params)
}

/** 采购订单详情（含明细） */
export function getPurchaseOrderDetail(orderId) {
  return request.get('/purchase/getDetailById', { params: { orderId } })
}

/** 新增/修改采购订单（含明细；金额服务端重算） */
export function upsertPurchaseOrder(data) {
  return request.post('/purchase/upsert', data)
}

/** 审批（approvalStatus: 1-通过 2-驳回；驳回必须填原因） */
export function auditPurchaseOrder(data) {
  return request.post('/purchase/audit', data)
}

/** 由采购订单生成入库单（不动库存） */
export function generateInbound(orderId) {
  return request.post('/purchase/generateInbound', { orderId })
}

/** 删除采购订单（已生成未取消入库单/已入库会被拒） */
export function deletePurchaseOrder(orderId) {
  return request.delete('/purchase/deleteById', { params: { orderId } })
}

// ========== 入库单 ==========

/** 分页查询入库单 */
export function getInboundList(params) {
  return request.post('/drugInbound/listPage', params)
}

/** 入库单详情（含明细） */
export function getInboundDetail(inboundId) {
  return request.get('/drugInbound/getDetailById', { params: { inboundId } })
}

/** 审核入库单（待审核 → 已审核） */
export function auditInbound(inboundId) {
  return request.post('/drugInbound/audit', { inboundId })
}

/** 入库（已审核 → 已入库；按明细建/加药品批次并写库存流水） */
export function stockInInbound(inboundId) {
  return request.post('/drugInbound/stockIn', { inboundId })
}

/** 取消入库单（待审核/已审核 → 已取消；取消原因必填） */
export function cancelInbound(data) {
  return request.post('/drugInbound/cancel', data)
}

/** 删除入库单（已入库不可删） */
export function deleteInbound(inboundId) {
  return request.delete('/drugInbound/deleteById', { params: { inboundId } })
}
