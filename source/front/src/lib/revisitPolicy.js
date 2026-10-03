/**
 * 复诊收费策略的枚举口径（单点定义）
 *
 * 为什么放前端常量而不是字典：`same_doctor` / `same_dept` 是三态判定条件
 * （0 不限 / 1 要求相同 / 2 要求不同），语义由后端 RevisitFeePolicyServiceImpl 的
 * triStateHit 唯一解释，医院不会去改文案 —— 与「收费方式」那种要随政策调整的码值不同，
 * 后者已入库成字典 his_revisit_charge_mode（见 sql/121）。
 */

/** 策略匹配条件的三态 */
export const REVISIT_MATCH_ANY = 0
export const REVISIT_MATCH_SAME = 1
export const REVISIT_MATCH_DIFF = 2

/**
 * 「不限 / 要求同一 / 要求不同」的选项表。
 * label 带上前缀（同一/不同 而不是 是/否），因为三态里 0 是「不参与判定」，
 * 写成「否」会被读成「要求不同」。
 */
export const REVISIT_MATCH_OPTIONS = [
  { label: '不限', value: REVISIT_MATCH_ANY },
  { label: '要求同一', value: REVISIT_MATCH_SAME },
  { label: '要求不同', value: REVISIT_MATCH_DIFF },
]

export function revisitMatchLabel(value) {
  const hit = REVISIT_MATCH_OPTIONS.find((o) => o.value === Number(value))
  return hit ? hit.label : '—'
}

/** 复诊收费方式码值（字典 his_revisit_charge_mode 的兜底文案，字典没拉到才用） */
export const REVISIT_CHARGE_MODE = {
  FULL: 1,
  FREE_REGIST: 2,
  FREE_ALL: 3,
}

/** 复诊来源码值（后端 RevisitSourceEnum 同源；决定占不占号源，见 needsNoSchedule） */
export const REVISIT_SOURCE = {
  SAME_DAY_RETURN: 1,
  DOCTOR_ORDERED: 2,
  PATIENT_SELF: 3,
  FOLLOWUP_PLAN: 4,
}

/**
 * 只有「当日回诊」不占号源 —— 它是同一次就诊的延续（拿结果回来复看），
 * 其余三种都是新的一次就诊，必须选排班、扣号源。
 * 与后端 RevisitSourceEnum.needsNoSchedule 同一口径，改一边必须改另一边。
 */
export function revisitNeedsNoSchedule(source) {
  return Number(source) === REVISIT_SOURCE.SAME_DAY_RETURN
}
