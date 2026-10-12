/** 设备台账日期文本：列表列与记录详情弹框共用同一口径（空值出 —） */
export const fmtDate = (v) => (v ? String(v).slice(0, 10) : '—');
