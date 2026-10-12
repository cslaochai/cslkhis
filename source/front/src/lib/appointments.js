import {SCHEDULE_TYPE_OPTIONS} from './scheduleShift';

// 挂号页共享口径：看板班次文案、时间片段文案、结算方式档位、「全部科室」哨兵。
// 从 AppointmentsView.vue 拆弹框时抽出来，主页面与各弹框子组件共用同一份定义。

const boardShifts = SCHEDULE_TYPE_OPTIONS.map(o => ({type: o.value, label: o.label}));
/** 班次文案（凌晨/上午/下午/夜班…），命中不到返回空串 */
export const deskShiftLabel = (type) => boardShifts.find(s => s.type === type)?.label || '';

/** 时间片段选项文案（biz_schedule_slot） */
export const slotLabel = (s) => `${s.startTime} ~ ${s.endTime}（余 ${s.availableSource}/${s.totalSource}）`;

// 结算方式选项（口径 = 字典 his_settlement_type_regist，与 biz_appoint_info.settlement_type 列注释一致）
export const settlementTypes = [
  {value: 1, label: '自费', color: 'text-slate-600'},
  {value: 2, label: '城镇职工医保', color: 'text-blue-600'},
  {value: 3, label: '城乡居民医保', color: 'text-emerald-600'},
  {value: 4, label: '公费', color: 'text-purple-600'},
  {value: 5, label: '商业保险', color: 'text-amber-600'},
];
/**
 * 结算方式文案。命中不到就出「未知(n)」——**不要回落成「自费」**：
 * 结算方式决定这单走不走医保结算，把它显示成自费会让人以为患者没医保。
 */
export const settlementLabel = (code) => {
  if (code === null || code === undefined || code === '')
    return '未知';
  return settlementTypes.find(t => t.value === Number(code))?.label || `未知(${code})`;
};

export const ALL_DEPT = '__ALL__';
export const isAllDept = (v) => v === ALL_DEPT || v == null || v === '';
