/**
 * 护理质控（sql/168）展示工具 —— 看板/台账/详情弹框共用一份格式化口径。
 */

/** 展示用：后端已给 indicatorName/reachedText，这里只兜「没数」的空态 */
export const fmt = (v, digits = 2) => (v == null || v === '' ? '—' : Number(v).toFixed(digits));
export const intFmt = (v) => (v == null || v === '' ? '—' : String(Number(v)));
