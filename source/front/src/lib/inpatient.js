// ------------------------------------------------------------------
// 住院域码值文案映射（InpatientView 与其拆出的子组件共用）
// 文案映射：未知码值必须显式说「未知」，绝不当成合法值渲染（后端 InpatientLabels 同规则）
// ------------------------------------------------------------------
export const admitWayMap = {1: '门诊', 2: '急诊', 3: '转院', 4: '其他'};
export const dischargeWayMap = {
  1: '医嘱离院', 2: '医嘱转院', 3: '医嘱转社区', 4: '非医嘱离院', 5: '死亡', 9: '其他',
};
export const summaryStatusMap = {
  1: {label: '草稿', type: 'info'},
  2: {label: '已提交', type: 'warning'},
  3: {label: '已归档', type: 'success'},
};
export const admitConditionMap = {1: '有', 2: '临床未确定', 3: '情况不明', 4: '无'};
export const orderStatusMap = {
  1: {label: '待收治', type: 'warning'},
  2: {label: '已收治', type: 'success'},
  3: {label: '已作废', type: 'info'},
  4: {label: '已过期', type: 'danger'},
};
export const operationLevelMap = {1: '一级', 2: '二级', 3: '三级', 4: '四级'};
export const incisionLevelMap = {0: '0类', 1: 'Ⅰ类', 2: 'Ⅱ类', 3: 'Ⅲ类'};
export const anesthesiaMap = {1: '全麻', 2: '椎管内', 3: '神经阻滞', 4: '局麻', 5: '其他'};
export const textOf = (map, code, empty = '—') => {
  if (code === null || code === undefined)
    return empty;
  return map[code] ?? `未知(${code})`;
};
export const money = (v) => (v === null || v === undefined ? '—' : `¥${Number(v).toFixed(2)}`);
