/**
 * 药品采购链枚举与判定 —— 前端唯一口径来源
 *
 * 文案**一律走字典**，不写死在这里：
 *   his_purchase_approval_status → biz_purchase_order.approval_status（0待审批/1已通过/2已驳回）
 *   his_inbound_status           → biz_drug_inbound.inbound_status（1待审核/2已审核/3已入库/4已取消）
 *   his_supplier_rating          → sys_supplier.rating（1差/2一般/3良好/4优秀）
 *   his_drug_inbound_type        → biz_drug_inbound.inbound_type（1采购入库/2退货入库/3盘盈入库/4其他入库）
 * 取文案统一走 `dictLabelText(getDictDataMapList(...)[type], value)`。
 *
 * 本文件只负责**语义判定**（哪些状态可编辑/可审批/可生成入库单/可入库/可取消）与标签色，
 * 以及**无字典的枚举**（入库单明细状态 detail_status）的单点定义。
 *
 * ⚠ 订单的「收货状态」**不是订单表的列**，由入库单派生（inboundNo / inboundDone）：
 *   采购只决定买什么，是否到货由入库单记录 —— 订单表存一份状态就有两个事实源，迟早不一致。
 *
 * ⚠ 未知码值一律渲染「未知(n)」，**不回落成合法值** ——
 *   回落会把"状态没维护"洗成"待审批/未入库"，让人以为流程还在正常推进。
 */

/** 采购订单审批状态（字典 his_purchase_approval_status） */
export const APPROVAL_STATUS = {
  PENDING: 0,   // 待审批
  APPROVED: 1,  // 已通过
  REJECTED: 2,  // 已驳回
}

/** 入库单状态（字典 his_inbound_status） */
export const INBOUND_STATUS = {
  PENDING_AUDIT: 1, // 待审核
  AUDITED: 2,       // 已审核
  STOCKED: 3,       // 已入库
  CANCELLED: 4,     // 已取消
}

/** 入库类型（字典 his_drug_inbound_type） */
export const INBOUND_TYPE = {
  PURCHASE: 1, // 采购入库
  RETURN: 2,   // 退货入库
  OVERAGE: 3,  // 盘盈入库
  OTHER: 4,    // 其他入库
}

/** 入库单明细状态（**无字典**，口径按 biz_drug_inbound_detail.detail_status 列注释单点定义） */
export const DETAIL_STATUS = {
  NORMAL: 1,    // 正常
  STOCKED: 2,   // 已入库
  CANCELLED: 3, // 已取消
}

// ==================== 采购订单判定 ====================

/** 可编辑：待审批 / 已驳回（已通过要先驳回才能改；已入库不可改） */
export function canEditOrder(order) {
  if (!order) return false
  const s = Number(order.approvalStatus)
  return s === APPROVAL_STATUS.PENDING || s === APPROVAL_STATUS.REJECTED
}

/** 可审批：只有待审批 */
export function canAuditOrder(order) {
  return !!order && Number(order.approvalStatus) === APPROVAL_STATUS.PENDING
}

/** 可生成入库单：审批通过 且 尚未入库 */
export function canGenerateInbound(order) {
  return !!order
    && Number(order.approvalStatus) === APPROVAL_STATUS.APPROVED
    && !order.inboundDone
}

/** 可删除：未入库（生成中的入库单由后端再校验） */
export function canDeleteOrder(order) {
  return !!order && !order.inboundDone
}

/** 订单收货进度文案（派生自入库单，不是订单表字段） */
export function orderInboundText(order) {
  if (!order) return '—'
  if (order.inboundDone) return '已入库'
  if (order.inboundNo) return '入库中'
  return '未入库'
}

/** 订单收货进度标签色 */
export function orderInboundTagType(order) {
  if (!order) return 'info'
  if (order.inboundDone) return 'success'
  if (order.inboundNo) return 'warning'
  return 'info'
}

/** 审批状态 → 标签色（文案仍从字典取） */
export function approvalTagType(status) {
  const n = Number(status)
  if (n === APPROVAL_STATUS.APPROVED) return 'success'
  if (n === APPROVAL_STATUS.REJECTED) return 'danger'
  if (n === APPROVAL_STATUS.PENDING) return 'warning'
  return 'info'
}

// ==================== 入库单判定 ====================

/** 可审核：待审核 */
export function canAuditInbound(inbound) {
  return !!inbound && Number(inbound.inboundStatus) === INBOUND_STATUS.PENDING_AUDIT
}

/** 可入库：已审核（待审核必须先审；已入库不可重复入） */
export function canStockIn(inbound) {
  return !!inbound && Number(inbound.inboundStatus) === INBOUND_STATUS.AUDITED
}

/** 可取消：待审核 / 已审核（已入库不可取消 —— 批次与流水已生成，冲销要走退货入库） */
export function canCancelInbound(inbound) {
  if (!inbound) return false
  const s = Number(inbound.inboundStatus)
  return s === INBOUND_STATUS.PENDING_AUDIT || s === INBOUND_STATUS.AUDITED
}

/** 可删除：未入库 */
export function canDeleteInbound(inbound) {
  return !!inbound && Number(inbound.inboundStatus) !== INBOUND_STATUS.STOCKED
}

/** 入库单状态 → 标签色（文案仍从字典取） */
export function inboundStatusTagType(status) {
  const n = Number(status)
  if (n === INBOUND_STATUS.STOCKED) return 'success'
  if (n === INBOUND_STATUS.AUDITED) return 'primary'
  if (n === INBOUND_STATUS.CANCELLED) return 'info'
  if (n === INBOUND_STATUS.PENDING_AUDIT) return 'warning'
  return 'info'
}

/** 入库单明细状态 → 文案（无字典，单点定义） */
export function detailStatusText(status) {
  if (status === null || status === undefined || status === '') return '—'
  const n = Number(status)
  if (n === DETAIL_STATUS.NORMAL) return '正常'
  if (n === DETAIL_STATUS.STOCKED) return '已入库'
  if (n === DETAIL_STATUS.CANCELLED) return '已取消'
  return `未知(${status})`
}

/** 入库单明细状态 → 标签色 */
export function detailStatusTagType(status) {
  const n = Number(status)
  if (n === DETAIL_STATUS.STOCKED) return 'success'
  if (n === DETAIL_STATUS.CANCELLED) return 'info'
  return 'primary'
}
