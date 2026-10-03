/**
 * 病案编码任务口径单点（G16 收口）
 * —— 状态文案走字典（his_archive_code_status），这里只放「非字典语义」的
 *    展示映射：tag 色、各状态动作。页面禁止另写映射副本。
 */

/** 状态 → el-tag type（1待编码 2已提交 3已完成 4已退修） */
export const CODE_TASK_STATUS_TAG = {
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger',
}

/** 各状态下可用动作（key → 弹窗语义） */
export const CODE_TASK_ACTIONS = {
  1: ['assign', 'submit'],
  4: ['submit'],
  2: ['audit'],
}

export function statusTagType(status) {
  return CODE_TASK_STATUS_TAG[Number(status)] || 'info'
}
