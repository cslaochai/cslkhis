// 病案质控展示辅助：主页面与质控单明细抽屉共用
export const num = (v) => Number(v ?? 0);
export const scoreText = (v) => (v === null || v === undefined ? '—' : String(v));
// 严重度标签色：3-否决（红）2-重要（橙）1-提示（灰蓝）0-无问题（绿）
// 0 必须显式给一档，否则它会落到最后的分支上，被渲染成"提示"色
export const severityTag = (s) => (s === 3 ? 'danger' : s === 2 ? 'warning' : s === 1 ? 'info' : 'success');
export const dimColor = {
  1: '#1269B5',
  2: '#0E9488',
  3: '#D97706',
};
