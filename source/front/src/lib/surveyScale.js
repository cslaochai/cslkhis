/**
 * 满意度量表口径（sql/164）—— 打分语义的单点定义，刻意**不**走字典。
 *
 * 字典是运营可改的，而「1 分 = 非常不满意」是统计口径本身：
 * 一旦有人在字典里把 5 改成「不满意」，历史答卷在新文案下就解释不通了，
 * 百分制 `(avg-1)/4*100` 也会跟着翻面。所以文案写死在这里，后端算分同样不读字典。
 */

/** 5 级李克特量表：1-5 分 */
export const LIKERT_LABELS = {
  1: '非常不满意',
  2: '不满意',
  3: '一般',
  4: '满意',
  5: '非常满意',
}

/** 量表满分（百分制换算的分母：(avg - 1) / (LIKERT_MAX - 1) × 100） */
export const LIKERT_MAX = 5

/** NPS 推荐度量表：0-10 分，独立于百分制，不参与加权 */
export const NPS_MIN = 0
export const NPS_MAX = 10

/** NPS 三档：推荐者 9-10 / 中立者 7-8 / 贬损者 0-6 */
export const NPS_PROMOTER_MIN = 9
export const NPS_DETRACTOR_MAX = 6

/**
 * 低分转投诉阈值（与后端 SurveyServiceImpl 保持一致，改一处必须改两处）：
 * 百分制总分 < 60，或任一维度均分 <= 2。
 */
export const LOW_SCORE_TOTAL = 60
export const LOW_SCORE_DIMENSION = 2

export const likertLabel = (score) => LIKERT_LABELS[Number(score)] || `${score} 分`

/** NPS 档位文案（0-10 单题，用于答卷逐题渲染） */
export const npsBand = (score) => {
  const v = Number(score)
  if (Number.isNaN(v)) return ''
  if (v >= NPS_PROMOTER_MIN) return '推荐者'
  if (v > NPS_DETRACTOR_MAX) return '中立者'
  return '贬损者'
}
