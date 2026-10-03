/**
 * 财务班结 / 日结 相关枚举 —— 全前端唯一口径来源（页面禁写本地副本）
 *
 * 口径判定依据（2026-09-23）：
 *   1) 列注释：biz_cashier_settlement.settle_status tinyint「状态（1-已交班待日结 2-已日结 3-已审核）」
 *      biz_day_settlement.settle_status「状态（1-待审核 2-已审核）」
 *      biz_day_settlement.reconcile_status「对账结论（1-已平 2-有差异）」
 *      biz_cashier_settlement.shift_type「班次（1-白班 2-夜班 3-其他）」
 *      biz_charge_info.payment_method「支付方式（1-现金 2-微信 3-支付宝 4-医保 5-余额）」
 *   2) 字典表：sys_dict_data 里**没有**财务结账相关字典（这是流程内部状态机，
 *      不是需要运维维护的外部目录）→ 按项目约定在 lib 单点定义
 *   3) 真实数据：全库 biz_cashier_settlement / biz_day_settlement 一开始为空，
 *      码值由本次交付写入，故口径完全取自列注释
 *
 * ⚠ 不要拿 biz_charge_info.charge_status（1-待收费 2-已收费 3-已退费 4-部分退费 5-已取消）
 *   当交班单状态，两套码值语义不同。收费单是"单据状态"，交班单是"结账状态"。
 *
 * 未知码值渲染「未知(n)」，不回落成看似合法的值。
 */

// ---------------------------------------------------------------------------
// 班次（交班单选填，只作展示标签，不参与统计口径）
// ---------------------------------------------------------------------------
export const SHIFT_TYPE_OPTIONS = [
  { label: '白班', value: 1 },
  { label: '夜班', value: 2 },
  { label: '其他', value: 3 },
]

export function shiftTypeText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = SHIFT_TYPE_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

// ---------------------------------------------------------------------------
// 交班单状态：1-已交班待日结 2-已日结 3-已审核
// ---------------------------------------------------------------------------
export const CASHIER_SETTLE_STATUS_OPTIONS = [
  { label: '待日结', value: 1 },
  { label: '已日结', value: 2 },
  { label: '已审核', value: 3 },
]

export function cashierStatusText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = CASHIER_SETTLE_STATUS_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

export function cashierStatusTagClass(value) {
  switch (Number(value)) {
    case 1: return 'bg-amber-100 text-amber-700'
    case 2: return 'bg-blue-100 text-blue-700'
    case 3: return 'bg-emerald-100 text-emerald-700'
    default: return 'bg-rose-100 text-rose-700'
  }
}

// ---------------------------------------------------------------------------
// 日结单状态：1-待审核 2-已审核
// ---------------------------------------------------------------------------
export const DAY_SETTLE_STATUS_OPTIONS = [
  { label: '待审核', value: 1 },
  { label: '已审核', value: 2 },
]

export function dayStatusText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = DAY_SETTLE_STATUS_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

export function dayStatusTagClass(value) {
  switch (Number(value)) {
    case 1: return 'bg-amber-100 text-amber-700'
    case 2: return 'bg-emerald-100 text-emerald-700'
    default: return 'bg-rose-100 text-rose-700'
  }
}

// ---------------------------------------------------------------------------
// 对账结论：1-已平 2-有差异
// ---------------------------------------------------------------------------
export const RECONCILE_STATUS_OPTIONS = [
  { label: '已平', value: 1 },
  { label: '有差异', value: 2 },
]

export function reconcileText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = RECONCILE_STATUS_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

export function reconcileTagClass(value) {
  switch (Number(value)) {
    case 1: return 'bg-emerald-100 text-emerald-700'
    case 2: return 'bg-rose-100 text-rose-700'
    default: return 'bg-slate-200 text-slate-500'
  }
}

// ---------------------------------------------------------------------------
// 支付方式：1-现金 2-微信 3-支付宝 4-医保 5-余额
// ---------------------------------------------------------------------------
export const PAYMENT_METHOD_OPTIONS = [
  { label: '现金', value: 1 },
  { label: '微信', value: 2 },
  { label: '支付宝', value: 3 },
  { label: '医保', value: 4 },
  { label: '余额', value: 5 },
]

export function paymentMethodText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = PAYMENT_METHOD_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

// ---------------------------------------------------------------------------
// 一级/二级/三级对账的显示顺序（后端按语义返回值，前端只决定排版）
// ---------------------------------------------------------------------------
export const RECONCILE_LEVEL_ORDER = ['cashierShift', 'dayVsShift', 'deptAttribution']
