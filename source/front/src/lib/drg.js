// DRG 模拟展示工具：入组状态标签色与盈亏配色，页面与模拟弹窗共用一份口径。

/** 模拟状态 → el-tag 类型（1-已入组 success，2-未入组 info，其余兜底 info） */
export const simTag = (v) => ({1: 'success', 2: 'info'}[v] || 'info');

/** 盈亏金额 → 文字颜色类（结余绿 / 超支红） */
export const profitColor = (v) => {
  const n = Number(v || 0);
  return n >= 0 ? 'text-green-600' : 'text-red-600';
};
