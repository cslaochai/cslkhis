// 转科页面共用展示工具：列表与弹框（发起/接收/详情）同口径格式化，避免两处各写一遍。

/** 时间展示：ISO 串换空格分隔，空值回退为破折号 */
export const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');

/** 文本展示：空值统一回退为破折号 */
export const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

/** 在院患者选项标签：床号 姓名（科室优先，无科室退病区） */
export const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.deptName || a.wardName || '—'}）`;

/** 转科状态 → el-tag 类型（列表状态列与详情弹框共用同一口径） */
export const statusTagType = (status) => {
  if (status === 0)
    return 'warning';
  if (status === 1)
    return 'success';
  return 'info';
};
