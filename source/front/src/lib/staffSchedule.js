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
import {PUBLISH_DRAFT, PUBLISH_PENDING, isPublishEffective} from './publishStatus'

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
 * 排班域（后端 StaffScheduleScopeEnum，biz_schedule 按域拆页后的视图口径）。
 *
 * 一张表四个域：门诊号源（门诊排班页）/ 护理（护理排班页）/ 急诊值班（急诊排班页）/
 * 岗位出勤（员工排班页）。前三个域各有专用页面，员工排班页(STAFF)对它们只排除不承载；
 * 急诊排班页(ER)只看急诊科室 + 医生。scope 是服务端过滤与越域拦截的唯一依据，
 * 页面不传 = 不过域（看到别个域的行比报错更危险）。
 */
export const SCHEDULE_SCOPE_STAFF = 1
export const SCHEDULE_SCOPE_ER = 2
/** 门诊出诊域（出勤侧）：门诊排班页的休息/请假/培训/停班行，医生为主角 */
export const SCHEDULE_SCOPE_OPD = 3

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
  7: '提交审核', 8: '审核发布', 9: '驳回', 10: '整周提交', 11: '整周审核发布',
  12: '撤回', 13: '进入调班',
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

// ========== 发布态流转（排班保存只到草稿，审核发布后才计在岗） ==========
// 判定口径与后端 PublishStatusEnum 同源，页面与其拆出的弹框组件共用这一份。

/** 审核发布权限码：独立于排班权限，排班人不能自己把自己排的班发出去 */
export const PERM_SCHEDULE_AUDIT = 'staff:schedule:audit'

/** 只有草稿能提交审核 */
export const canSubmitPublish = (row) => !!row?.id && Number(row.publishStatus) === PUBLISH_DRAFT
/** 只有待审核能审核发布 */
export const canAuditPublish = (row) => !!row?.id && Number(row.publishStatus) === PUBLISH_PENDING
/** 已发布/已变更才能进入调班 */
export const canAdjustPublish = (row) => !!row?.id && isPublishEffective(Number(row.publishStatus))
/** 撤回：任意态都能回草稿（与后端 recall 的状态机同口径） */
export const canRecallPublish = (row) => !!row?.id
