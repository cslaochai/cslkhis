/**
 * 全院岗位排班（sql/200 核心表）码值唯一口径。
 *
 * 与后端 his-common 枚举逐字对齐，页面禁写本地副本：
 *   OrgUnitTypeEnum  / StaffDutyStatusEnum / AttendModeEnum
 *   StaffScheduleSourceEnum / ScheduleChangeTypeEnum / DutyScopeEnum
 *
 * 为什么这里要知道「上课」而不是只看字典：
 *   1) 出勤状态与响应形态决定了「此刻谁在岗」的解析口径（夜里还班的人是昨天的班），
 *      下拉拿不到值会静默解析出空名单 —— 比报错更危险；
 *   2) 列表渲染一律直接用后端 VO 的 xxxText 字段，这里只服务**下拉与表单选项**，
 *      两端文案必须一致，所以单一口径放在这里而不是各页自己写。
 *
 * 未知码值渲染「未知(n)」，绝不回落成某个合法值（未知[x] 是 bug 的线索，假数据不是）。
 */

/** 排班单元类型（biz_staff_schedule.org_type） */
export const ORG_UNIT_TYPE_TEXT = { 1: '科室', 2: '病区', 3: '全院' }
export const ORG_UNIT_TYPE_OPTIONS = [
  { label: '科室', value: 1 },
  { label: '病区', value: 2 },
  { label: '全院', value: 3 },
]
export const ORG_UNIT_TYPE_DEPT = 1
export const ORG_UNIT_TYPE_WARD = 2
export const ORG_UNIT_TYPE_HOSPITAL = 3

/** 全院级排班没有具体单元，org_id / dept_id 都落 0 */
export const isHospitalLevel = (orgType) => Number(orgType) === ORG_UNIT_TYPE_HOSPITAL

/**
 * 出勤状态（biz_staff_schedule.duty_status）
 *
 * 它回答的是「这个人今天来不来」，与 biz_schedule.status（号源态：停诊/正常/已满）**不是一件事**：
 * 停诊只是这个班不放号，人可能还在科里；把停诊当成停班，在岗名单会少一个本来能接到电话的人。
 */
export const DUTY_STATUS_WORK = 1
export const DUTY_STATUS_TEXT = { 1: '上班', 2: '休息', 3: '请假', 4: '培训', 5: '停班' }
export const DUTY_STATUS_OPTIONS = [
  { label: '上班', value: 1 },
  { label: '休息', value: 2 },
  { label: '请假', value: 3 },
  { label: '培训', value: 4 },
  { label: '停班', value: 5 },
]
/** 只有上班的班次才需要选班次（休息/请假/培训/停班没有班次，shift_id 落 0） */
export const dutyStatusNeedShift = (code) => Number(code) === DUTY_STATUS_WORK

/**
 * 响应形态（biz_staff_schedule.attend_mode）
 *
 * 回答「叫得动人叫不动」：坐班的人在单元里；听班的人在家待命、来电话才到岗；留院值班住在医院。
 * 三档在派单与急诊升级里的处置不同，所以独立成一维。**听班不放号** ——
 * 给一个在家待命的医生放号，患者到了没人看，是投诉而不是数据问题。
 */
export const ATTEND_MODE_ON_SITE = 1
export const ATTEND_MODE_TEXT = { 1: '坐班', 2: '听班', 3: '留院值班' }
export const ATTEND_MODE_OPTIONS = [
  { label: '坐班', value: 1 },
  { label: '听班', value: 2 },
  { label: '留院值班', value: 3 },
]

/** 排班生成来源（biz_staff_schedule.schedule_source） */
export const SCHEDULE_SOURCE_TEXT = { 1: '手工', 2: '模板', 3: '复制周期', 4: '换班' }

/** 排班变更类型（biz_schedule_change_log.action_type） */
export const SCHEDULE_CHANGE_TYPE_TEXT = {
  1: '换班', 2: '代班', 3: '停班', 4: '加号', 5: '减号', 6: '出诊变更',
}

/** 值守责任范围（biz_duty_post.duty_scope） */
export const DUTY_SCOPE_TEXT = { 1: '全院行政', 2: '急诊', 3: '感染', 4: '总务', 5: '信息' }
export const DUTY_SCOPE_OPTIONS = [
  { label: '全院行政', value: 1 },
  { label: '急诊', value: 2 },
  { label: '感染', value: 3 },
  { label: '总务', value: 4 },
  { label: '信息', value: 5 },
]

/** 班内角色（biz_duty_post.role_type / biz_duty_roster.role_type，主班找不到就叫副班） */
export const DUTY_ROLE_TYPE_TEXT = { 1: '主班', 2: '副班' }
export const DUTY_ROLE_TYPE_OPTIONS = [
  { label: '主班', value: 1 },
  { label: '副班', value: 2 },
]

/** 码值 → 文案：null/'' 返回空串（「未填」与「未知」是两回事） */
const textOf = (map, code) => {
  if (code === null || code === undefined || code === '') return ''
  return map[Number(code)] ?? `未知(${code})`
}

export const orgUnitTypeText = (code) => textOf(ORG_UNIT_TYPE_TEXT, code)
export const dutyStatusText = (code) => textOf(DUTY_STATUS_TEXT, code)
export const attendModeText = (code) => textOf(ATTEND_MODE_TEXT, code)
export const scheduleSourceText = (code) => textOf(SCHEDULE_SOURCE_TEXT, code)
export const scheduleChangeTypeText = (code) => textOf(SCHEDULE_CHANGE_TYPE_TEXT, code)
export const dutyScopeText = (code) => textOf(DUTY_SCOPE_TEXT, code)
export const dutyRoleTypeText = (code) => textOf(DUTY_ROLE_TYPE_TEXT, code)
