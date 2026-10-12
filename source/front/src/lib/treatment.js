/**
 * 门诊治疗站（G19，/treatment-station）页面与其弹框组件共用的口径小工具。
 * 实现逐字来自 TreatmentStationView 原定义，拆分弹框组件后收口到这里，禁止各处再抄第二份。
 */

export const EXEC = {PENDING: 0, DONE: 1, CANCELLED: 2};
export const CHARGE = {NONE: 0, DONE: 1, FAIL: 2, NA: 3};
export const execTag = (s) => (Number(s) === EXEC.DONE ? 'success' : Number(s) === EXEC.CANCELLED ? 'info' : 'warning');
export const chargeTag = (s) => (Number(s) === CHARGE.DONE ? 'success'
    : Number(s) === CHARGE.FAIL ? 'danger' : Number(s) === CHARGE.NA ? 'info' : '');
export const money = (v) => (v === null || v === undefined ? '—' : `¥${Number(v).toFixed(2)}`);
export const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—');
export const today = (() => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
})();
