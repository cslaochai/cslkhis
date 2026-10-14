// 监管规则元数据（规则类型 / 级别文案，前后端口径一致）
export const RULE_TYPES = {
  duplicate_charge: '重复收费',
  over_limit_price: '超限价收费',
  split_hospitalization: '分解住院',
  bed_hanging: '挂床住院',
  abnormal_cost: '高额住院费用',
  payclass_c: '丙类项目入统筹',
}

export function ruleTypeText(type) {
  return RULE_TYPES[type] || type || '-'
}

export function severityText(s) {
  return { 1: '一般', 2: '较重', 3: '严重' }[s] || '-'
}

export function severityType(s) {
  return { 1: 'info', 2: 'warning', 3: 'danger' }[s] || 'info'
}
