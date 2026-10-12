/** CSSD 包状态 → el-tag 类型（追溯列表列与追溯链详情共用同一口径） */
export const cssdStatusTag = (v) => ({
  1: 'warning',
  2: 'warning',
  3: 'primary',
  4: 'primary',
  5: 'success',
  6: 'info'
}[v] || 'info');
/** CSSD 流转时间文本（空值出 —） */
export const fmtTime = (v) => (v ? String(v).slice(0, 19).replace('T', ' ') : '—');
