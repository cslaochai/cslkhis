// 排班发布态（G-15）：门诊排班 / 护理排班 / 总值班三页共用的展示与可操作性判定。
// 文案唯一来源是后端 PublishStatusEnum（出参 publishStatusText），这里只放
// 「码值 → 标签颜色」和「当前态能走哪些流转」，不再抄一份中文，免得两处漂移。

export const PUBLISH_DRAFT = 0
export const PUBLISH_PENDING = 1
export const PUBLISH_PUBLISHED = 2
export const PUBLISH_ADJUSTING = 3
export const PUBLISH_CHANGED = 4

const TAG_TYPES = {
  [PUBLISH_DRAFT]: 'info',
  [PUBLISH_PENDING]: 'warning',
  [PUBLISH_PUBLISHED]: 'success',
  [PUBLISH_ADJUSTING]: 'danger',
  [PUBLISH_CHANGED]: 'primary',
}

/** 兜底文案：出参 publishStatusText 是权威来源，这里只在前端自造行（无后端文案）时兜底 */
const STATUS_TEXTS = {
  [PUBLISH_DRAFT]: '草稿',
  [PUBLISH_PENDING]: '待审核',
  [PUBLISH_PUBLISHED]: '已发布',
  [PUBLISH_ADJUSTING]: '调班中',
  [PUBLISH_CHANGED]: '已变更',
}

export const publishStatusTagType = (code) => TAG_TYPES[code] || 'info'

export const publishStatusText = (code) => {
  if (code === null || code === undefined || code === '') return ''
  return STATUS_TEXTS[Number(code)] ?? `未知(${code})`
}

// 生效态才对外出号 / 计在岗 / 进全院展示；草稿、待审核、调班中一律不生效。
// 与后端 PublishStatusEnum.isEffective 同一口径，改这里必须同步改那边。
export const isPublishEffective = (code) => code === PUBLISH_PUBLISHED || code === PUBLISH_CHANGED
