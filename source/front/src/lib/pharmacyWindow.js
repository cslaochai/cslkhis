/**
 * 药房发药窗口（biz_dispensing_record）共用口径
 *
 * 从 views/pharmacy-window/PharmacyWindowView.vue 抽出：发药状态既是列表列的渲染口径，
 * 也是发药明细详情弹框的渲染口径，两处必须同一个来源，否则同一行会在列表和详情里显示成两个状态。
 */

// 三态口径：1-待发药 2-已发药 3-已退药（表注释为准；未知值渲染 未知(n) 不回落）
export const DISPENSING_STATUS_MAP = {
  1: {label: '待发药', color: 'bg-amber-100 text-amber-700'},
  2: {label: '已发药', color: 'bg-blue-100 text-blue-700'},
  3: {label: '已退药', color: 'bg-red-100 text-red-600'},
};

export const dispensingStatusLabel = (s) => {
  if (s == null)
    return '未知';
  return DISPENSING_STATUS_MAP[s]?.label || `未知(${s})`;
};

export const dispensingStatusColor = (s) => DISPENSING_STATUS_MAP[s]?.color || 'bg-slate-100 text-slate-500';
