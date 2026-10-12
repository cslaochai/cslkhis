/**
 * 床位服务中心（菜单 317）展示口径 —— 列表列与床位详情弹框共用，
 * 改色/改映射只在这里动一处。
 */

/** 床位状态 → tag 色：1 空闲 / 2 占用 / 3 锁定（预留） / 其余维修停用 */
export const bedStatusTag = (s) => (s === 1 ? 'success' : s === 2 ? 'info' : s === 3 ? 'warning' : 'danger');
