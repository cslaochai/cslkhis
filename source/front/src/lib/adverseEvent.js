/**
 * 不良事件口径单点（G12）
 * —— 状态/等级/类型的文案走字典（his_adverse_event_*），这里只放「非字典语义」的
 *    展示映射：tag 色、状态动作提示。页面禁止另写状态映射副本。
 */

/** 状态 → el-tag type */
export const ADVERSE_EVENT_STATUS_TAG = {
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'info',
}

/** 等级 → el-tag type（I 级警讯红、II 级橙、III 级蓝、IV 级灰） */
export const ADVERSE_EVENT_LEVEL_TAG = {
  1: 'danger',
  2: 'warning',
  3: 'primary',
  4: 'info',
}

/** 各状态下的下一步动作（1=处理 2=整改 3=结案；4 无） */
export const ADVERSE_EVENT_NEXT_ACTION = {
  1: { key: 'handle', label: '处理', placeholder: '处理意见（现场核实、对患者的影响与处置）' },
  2: { key: 'rectify', label: '整改', placeholder: '整改措施（流程改进、培训、警示标识等）' },
  3: { key: 'close', label: '结案', placeholder: '验证结论（整改效果核实情况）' },
}

/** 状态文案兜底（字典加载失败时用，命中不了显示 未知(n) 由 dictLabelText 负责） */
export function statusTagType(status) {
  return ADVERSE_EVENT_STATUS_TAG[Number(status)] || 'info'
}

export function levelTagType(level) {
  return ADVERSE_EVENT_LEVEL_TAG[Number(level)] || 'info'
}
