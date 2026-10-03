import request from './request'

// ========== 库存管理 ==========

// 查询库存列表
export function getStockList(params) {
  return request.post('/pharmacy/stockListPage', params)
}

// 获取库存详情
export function getStockDetail(stockId) {
  return request.get('/pharmacy/getStockById', { params: { stockId } })
}

// 新增库存
export function createStock(data) {
  return request.post('/pharmacy/stockUpsert', data)
}

// 入库
export function inboundStock(stockId, quantity) {
  return request.post('/pharmacy/stockInbound', { stockId, quantity })
}

// 出库
export function outboundStock(stockId, quantity) {
  return request.post('/pharmacy/stockOutbound', { stockId, quantity })
}

// 查询库存预警列表
export function getStockWarningList(stockStatus) {
  return request.post('/pharmacy/stockWarningList', { stockStatus })
}

// 库存出入库流水分页（drugName 模糊 + changeType 过滤）
export function getStockLogList(params) {
  return request.post('/pharmacy/stockLogListPage', params)
}

// 批次候选（调拨/退货建单时点批次）：只出可用量>0 的批次，按效期升序
// stockRoom 1-药库 2-药房（留空=两层都看）；onlyWithSupplier 供退货页过滤掉没挂供应商档案的批次
export function getStockBatchCandidates(params) {
  return request.post('/pharmacy/stockBatchCandidates', params)
}

// ========== 药房盘点（sql/127：账面快照 → 实盘录入 → 差异 → 复核过账）==========

export function listStocktakePage(params) {
  return request.post('/pharmacy/stocktake/listPage', params)
}

export function getStocktakeDetail(id) {
  return request.get('/pharmacy/stocktake/getDetailById', { params: { id } })
}

// id 为空 = 新建并按范围抓账面快照；id 非空 = 改主题/范围（改范围会重抓快照、清空已录实盘数）
export function upsertStocktake(data) {
  return request.post('/pharmacy/stocktake/upsert', data)
}

export function saveStocktakeCount(data) {
  return request.post('/pharmacy/stocktake/saveCount', data)
}

export function submitStocktake(id) {
  return request.post('/pharmacy/stocktake/submit', { id })
}

// pass=true 差异过账（改批次余额 + 落库存流水）；pass=false 退回盘点中重录
export function auditStocktake(data) {
  return request.post('/pharmacy/stocktake/audit', data)
}

export function deleteStocktake(id) {
  return request.delete('/pharmacy/stocktake/deleteById', { params: { id } })
}

// ========== 药品调拨（sql/154 ②级：药库 ↔ 药房，两层库位之间搬批次）==========
//
// 状态机：1待发出 →（确认发出）2待接收 →（确认接收）3已完成；1 可作废(4)、可改明细、可删。
// 一张单落两行库存流水（7-调拨出库为负 / 8-调拨入库为正），两行合计 0 就是账实闭环的证据。
// ⚠ 批号/成本价/效期由服务端从批次快照，前端只传 stockId + 数量。

export function listDrugTransferPage(params) {
  return request.post('/pharmacy/drugTransfer/listPage', params)
}

export function getDrugTransferDetail(id) {
  return request.get('/pharmacy/drugTransfer/getDetailById', { params: { id } })
}

export function upsertDrugTransfer(data) {
  return request.post('/pharmacy/drugTransfer/upsert', data)
}

export function confirmDrugTransferOut(data) {
  return request.post('/pharmacy/drugTransfer/confirmOut', data)
}

export function confirmDrugTransferIn(data) {
  return request.post('/pharmacy/drugTransfer/confirmIn', data)
}

// 仅「待发出」可作废；已在途的只能再开一张反向调拨搬回去
export function cancelDrugTransfer(data) {
  return request.post('/pharmacy/drugTransfer/cancel', data)
}

export function deleteDrugTransferById(id) {
  return request.delete('/pharmacy/drugTransfer/deleteById', { params: { id } })
}

// ========== 药品供应商退货（sql/154 ③级：把批次退给供应商，向供应商主张退款的依据）==========
//
// 状态机：1待退货 →（确认退货）2已退货；1 可作废(3)、可改明细、可删。
// 批次必须已挂 supplier_id（历史批次的 supplier 文本多是生产厂家名，不能当退货对象）。

export function listSupplierReturnPage(params) {
  return request.post('/pharmacy/supplierReturn/listPage', params)
}

export function getSupplierReturnDetail(id) {
  return request.get('/pharmacy/supplierReturn/getDetailById', { params: { id } })
}

export function upsertSupplierReturn(data) {
  return request.post('/pharmacy/supplierReturn/upsert', data)
}

export function confirmSupplierReturn(data) {
  return request.post('/pharmacy/supplierReturn/confirmReturn', data)
}

export function cancelSupplierReturn(data) {
  return request.post('/pharmacy/supplierReturn/cancel', data)
}

export function deleteSupplierReturnById(id) {
  return request.delete('/pharmacy/supplierReturn/deleteById', { params: { id } })
}

// ========== 处方点评（sql/160：事后专项点评 + 超常处方公示 + 医师约谈，菜单 510/511）==========
//
// 口径：不合理处方 = 不规范(2) + 用药不适宜(3) + 超常(4)；公示只增不可撤；
// 约谈医师确认签字后禁改禁删。码值与文案见 lib/rxReview.js。

export function getRxReviewStats(month) {
  return request.get('/rxReview/stats', { params: { month } })
}

export function listRxBatchPage(params) {
  return request.post('/rxReview/batchListPage', params)
}

export function upsertRxBatch(data) {
  return request.post('/rxReview/batchUpsert', data)
}

export function completeRxBatch(id) {
  return request.post('/rxReview/completeBatch', null, { params: { id } })
}

export function deleteRxBatchById(id) {
  return request.delete('/rxReview/batchDeleteById', { params: { id } })
}

export function listRxItemPage(params) {
  return request.post('/rxReview/itemListPage', params)
}

export function upsertRxItem(data) {
  return request.post('/rxReview/itemUpsert', data)
}

export function addRxItemByNo(data) {
  return request.post('/rxReview/itemAddByNo', data)
}

export function publicityRxItems(itemIds) {
  return request.post('/rxReview/publicity', { itemIds })
}

export function listRxPublicityPage(params) {
  return request.post('/rxReview/publicityListPage', params)
}

export function getRxPublicityStats(params) {
  return request.get('/rxReview/publicityStats', { params })
}

export function listRxTalkPage(params) {
  return request.post('/rxReview/talkListPage', params)
}

export function upsertRxTalk(data) {
  return request.post('/rxReview/talkUpsert', data)
}

export function confirmRxTalk(id, confirmBy) {
  return request.post('/rxReview/talkConfirm', null, { params: { id, confirmBy } })
}

export function deleteRxTalkById(id) {
  return request.delete('/rxReview/talkDeleteById', { params: { id } })
}

export function exportRxItemCsv(params) {
  return request.post('/rxReview/itemExportCsv', params)
}
