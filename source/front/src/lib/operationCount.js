// 手术器械清点页面与建单/阶段清点弹框组件共用的展示辅助（各处不再各写一份副本）
export const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
// 手术申请单下拉的统一文案：申请号 · 患者 · 术式（急诊单独标注）
export const applyLabel = (a) => `${a.applyNo || '—'} · ${a.patientName || '—'} · ${a.plannedOperationName || '—'}${a.isEmergency === 1 ? '（急诊）' : ''}`;
