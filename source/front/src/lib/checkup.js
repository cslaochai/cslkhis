// 体检域共用：码值 → el-tag 类型映射（列表列与拆出的弹框组件同一口径）
export const checkupStatusTag = (v) => ({1: 'info', 2: 'warning', 3: 'primary', 4: 'success'}[v] || 'info');
export const checkupFlagTag = (v) => ({0: 'success', 1: 'danger', 2: 'warning'}[v] || 'info');
