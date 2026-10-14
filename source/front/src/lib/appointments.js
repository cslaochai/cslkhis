import {SCHEDULE_TYPE_OPTIONS} from './scheduleShift';

// 挂号页共享口径：看板班次文案、时间片段文案、结算方式档位、「全部科室」哨兵。
// 从 AppointmentsView.vue 拆弹框时抽出来，主页面与各弹框子组件共用同一份定义。

const boardShifts = SCHEDULE_TYPE_OPTIONS.map(o => ({type: o.value, label: o.label}));
/** 班次文案（凌晨/上午/下午/夜班…），命中不到返回空串 */
export const deskShiftLabel = (type) => boardShifts.find(s => s.type === type)?.label || '';

/** 时间片段选项文案（biz_schedule_slot） */
export const slotLabel = (s) => `${s.startTime} ~ ${s.endTime}（余 ${s.availableSource}/${s.totalSource}）`;

// 本单结算方式（与收费账单 settlement_mode、字典 his_settlement_mode 同口径，统一为两档）：
// 1-全自费 2-医保联网。具体险种（职工/居民/公费）看患者费别 medicalInsuranceType，不在本单结算方式里再分。
export const settlementTypes = [
  {value: 1, label: '全自费', color: 'text-slate-600'},
  {value: 2, label: '医保联网', color: 'text-emerald-600'},
];
/**
 * 本单结算方式文案。命中不到就出「未知(n)」——**不要回落成「全自费」**：
 * 结算方式决定这单走不走医保联网，把它显示成自费会让人以为这单没走医保。
 */
export const settlementLabel = (code) => {
  if (code === null || code === undefined || code === '')
    return '未知';
  return settlementTypes.find(t => t.value === Number(code))?.label || `未知(${code})`;
};

export const ALL_DEPT = '__ALL__';
export const isAllDept = (v) => v === ALL_DEPT || v == null || v === '';

/**
 * 账单上已经有收到的钱（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）。
 * 已收费的挂号改约要过「同医生 + 同费用」闸，看板卡片与改约弹窗共用这一份判定。
 */
export const isRegistPaid = (r) => [2, 3].includes(Number(r?.billStatus));

/** 号源原价 = 挂号费 + 诊查费，单位「分」（与后端出账口径一致；用分比避免浮点误差） */
export const scheduleFeeCents = (s) => Math.round((Number(s?.registFee) || 0) * 100)
    + Math.round((Number(s?.diagnosisFee) || 0) * 100);

/**
 * 已收费号改约的候选判定（与后端 assertPaidRescheduleSamePrice 同一口径）：
 * 只留「同医生 + 同费用」的号源，其余不进候选列表。
 *
 * 后端只放这一档：价差没人补/退，收入归属一经出账不改写。
 * `origFeeCents` 传 null 表示原号源费用无从得知（历史号源已被删）→ 不比费用，由后端兜底判。
 */
export const isPaidRescheduleCandidate = (row, origFeeCents, schedule) => {
  if (!isRegistPaid(row))
    return true;
  if (row?.doctorId && String(schedule?.doctorId) !== String(row.doctorId))
    return false;
  if (origFeeCents !== null && scheduleFeeCents(schedule) !== origFeeCents)
    return false;
  return true;
};
