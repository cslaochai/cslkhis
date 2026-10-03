/**
 * 班别（schedule_type）唯一口径
 * 码值口径（后端 ScheduleTypeEnum 一致）：
 *   1-上午 2-下午 3-全天 4-凌晨（00:00~08:00，急诊后夜班）5-夜班（16:00~23:00 这类不跨零点的夜间班）
 * 落库位置只有两处：biz_shift（班别是班次的属性）、biz_appoint_info（挂号时的历史快照）。
 * biz_schedule / biz_schedule_template 表上**没有**这一列，排班/模板 VO 的 scheduleType
 * 是后端按 shift_id 从班次字典批量补出来的派生只读值。
 * 页面禁写本映射的本地副本；纯 UI 配色（shiftColors）可留在页面。
 */
export const SCHEDULE_TYPE_TEXT = { 1: '上午', 2: '下午', 3: '全天', 4: '凌晨', 5: '夜班' }

/** 班别下拉（仅「班次字典」新增用：给一条新班次指定班别；默认时间只是填表初始值） */
export const SCHEDULE_TYPE_OPTIONS = [
  { label: '上午', value: 1, startTime: '08:00', endTime: '12:00' },
  { label: '下午', value: 2, startTime: '14:00', endTime: '17:30' },
  { label: '全天', value: 3, startTime: '08:00', endTime: '17:30' },
  { label: '凌晨', value: 4, startTime: '00:00', endTime: '08:00' },
  { label: '夜班', value: 5, startTime: '16:00', endTime: '23:00' },
]

/**
 * 码值 → 文案。未收录码值渲染「未知(n)」，绝不回落成某个合法值；
 * null/undefined/'' 返回空串（「未填」与「未知」是两回事）。
 */
export const scheduleTypeText = (code) => {
  if (code === null || code === undefined || code === '') return ''
  return SCHEDULE_TYPE_TEXT[code] ?? `未知(${code})`
}

/**
 * 班次适用域（biz_shift.use_scope）唯一口径
 *   1-门诊/急诊排班（biz_schedule / 排班模板）2-病区护理排班（biz_nurse_schedule）
 *   3-全院值守（biz_duty_roster）4-全院通用（sql/200 起：医技/窗口/行政的出勤班次）
 * 各册班次互不通用：门诊排班选到护理班次会被后端直接拒绝，
 * 所以下拉数据源必须带这个过滤，不能靠人眼从名称里挑。
 * 3/4 两档是 sql/200 扩的：原先「新增一个岗位类别就要改码值」的分册口径撑不住全院岗位出勤，
 * 4-全院通用才是给非医护岗位用的那本册。
 */
export const SHIFT_SCOPE_OUTPATIENT = 1
export const SHIFT_SCOPE_NURSING = 2
export const SHIFT_SCOPE_DUTY = 3
export const SHIFT_SCOPE_GENERAL = 4
export const SHIFT_SCOPE_TEXT = { 1: '门诊/急诊排班', 2: '病区护理排班', 3: '全院值守', 4: '全院通用' }
export const SHIFT_SCOPE_OPTIONS = [
  { label: '门诊/急诊排班', value: SHIFT_SCOPE_OUTPATIENT },
  { label: '病区护理排班', value: SHIFT_SCOPE_NURSING },
  { label: '全院值守', value: SHIFT_SCOPE_DUTY },
  { label: '全院通用', value: SHIFT_SCOPE_GENERAL },
]
export const shiftScopeText = (code) => {
  if (code === null || code === undefined || code === '') return ''
  return SHIFT_SCOPE_TEXT[code] ?? `未知(${code})`
}

/**
 * 岗位类别（sql/195，后端 StaffTypeEnum 同口径）唯一口径
 *
 * 排班对象从「医生」泛化到「全院岗位」：护士、技师、药师、收费员、导诊这些岗位每天谁在岗
 * 同样是排班员要排的东西。口径是**人事岗位类别**，不是权限角色：
 *   · 权威落点 sys_role.staff_type（角色 → 类别），一个类别下有多个角色；
 *   · 「人在哪个科室是什么岗位」由 sys_employee_post(人 × 科室 × 角色) 派生，
 *     员工档案上的 emp_type 与岗位表不一致，挑人一律不用它。
 *
 * 号源分水岭：只有 1-医生 的排班有号源/诊室/挂号费，其余岗位是纯出勤排班。
 * 判定一律走 staffTypeHasSource()，别在页面里写 === 1 这种裸比较。
 */
export const STAFF_TYPE_DOCTOR = 1
export const STAFF_TYPE_TEXT = { 1: '医生', 2: '护理', 3: '医技', 4: '药学', 5: '收费', 6: '行政其他' }
export const STAFF_TYPE_OPTIONS = [
  { label: '医生', value: 1 },
  { label: '护理', value: 2 },
  { label: '医技', value: 3 },
  { label: '药学', value: 4 },
  { label: '收费', value: 5 },
  { label: '行政其他', value: 6 },
]

/** 该岗位的排班是否承载号源（决定号源/诊室/挂号费/预约池/加号这些表单块显不显示） */
export const staffTypeHasSource = (code) => Number(code) === STAFF_TYPE_DOCTOR

export const staffTypeText = (code) => {
  if (code === null || code === undefined || code === '') return ''
  return STAFF_TYPE_TEXT[Number(code)] ?? `未知(${code})`
}
