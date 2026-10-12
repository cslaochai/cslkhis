// 会诊页面共用展示工具：列表与弹框（申请/详情）同口径格式化，避免两处各写一遍。

/** 时间展示：ISO 串换空格分隔，空值回退为破折号 */
export const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');

/** 文本展示：空值统一回退为破折号 */
export const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

/** 住院患者选项标签：床号 姓名（病区优先，无病区退科室） */
export const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`;
