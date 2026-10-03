/**
 * 发票状态 / 发票类型 文案 —— 全前端唯一口径来源
 *
 * 口径判定依据（实测 2026-09-20）：
 *   1) 列注释：biz_invoice.invoice_status tinyint「发票状态（1-已开具 2-已打印 3-已作废）」
 *      biz_invoice.invoice_type tinyint「发票类型（1-普通发票 2-电子发票 3-数电发票）」
 *   2) 字典表：sys_dict_data 里**没有**发票相关字典（只有 his_charge_status / his_refund_* 等）
 *      → 所以列注释是唯一可用口径，不能凭空编，也不能去借 his_charge_status
 *   3) 真实数据：87 行全部 invoice_status=1 / invoice_type=1 —— 后两档暂无用例，
 *      但列注释已定义，按注释渲染
 *
 * ⚠ 不要拿 biz_charge_info.charge_status（1-待结算 2-已结算 3-已退费 5-已取消）当发票状态，
 *   两套码值语义不同。发票是"票据"，收费单是"业务单据"。
 *
 * 未知码值渲染「未知(n)」，不回落成看似合法的值。
 */

/** 发票状态下拉选项（筛选 + 渲染共用） */
export const INVOICE_STATUS_OPTIONS = [
  { label: '已开具', value: 1 },
  { label: '已打印', value: 2 },
  { label: '已作废', value: 3 },
]

/** 发票类型下拉选项 */
export const INVOICE_TYPE_OPTIONS = [
  { label: '普通发票', value: 1 },
  { label: '电子发票', value: 2 },
  { label: '数电发票', value: 3 },
]

/**
 * 发票状态码值 → 文案
 * @param {number|string|null|undefined} value
 * @returns {string} 已开具 / 已打印 / 已作废 / 未知(n) / —
 */
export function invoiceStatusText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = INVOICE_STATUS_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

/**
 * 发票类型码值 → 文案
 * @param {number|string|null|undefined} value
 * @returns {string} 普通发票 / 电子发票 / 数电发票 / 未知(n) / —
 */
export function invoiceTypeText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = INVOICE_TYPE_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

/** 状态标签配色（已开具=蓝 已打印=绿 已作废=灰） */
export function invoiceStatusTagClass(value) {
  switch (Number(value)) {
    case 1:
      return 'bg-blue-100 text-blue-700'
    case 2:
      return 'bg-emerald-100 text-emerald-700'
    case 3:
      return 'bg-slate-200 text-slate-500'
    default:
      return 'bg-rose-100 text-rose-700'
  }
}
