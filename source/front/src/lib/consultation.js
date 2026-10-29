// 会诊页面共用展示工具：列表与弹框（申请/详情）同口径格式化，避免两处各写一遍。

/** 时间展示：ISO 串换空格分隔，空值回退为破折号 */
export const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');

/** 文本展示：空值统一回退为破折号 */
export const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

/** 住院患者选项标签：床号 姓名（病区优先，无病区退科室） */
export const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`;

/**
 * 会诊类别下拉候选：码值与文案逐字对齐后端 ConsultCategoryEnum。
 * 类别决定这单落在哪个会诊工作台，只能由入口钉死，不能由表单自选（申请弹框里不出现这个字段）。
 */
export const CONSULT_CATEGORY_OPTIONS = [
  {value: 1, label: '普通科间'},
  {value: 2, label: '营养'},
  {value: 3, label: '药学'},
  {value: 4, label: '其他专科'},
];
