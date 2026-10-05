<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScheduleList, createSchedule, updateSchedule, deleteSchedule, updateScheduleStatus,
         getScheduleTemplateListPage, saveScheduleTemplate, deleteScheduleTemplate, updateScheduleTemplateStatus,
         generateScheduleFromTemplate, previewScheduleTemplate, getStopImpact, batchCancelRegist,
         addScheduleSource, getScheduleSlots, saveScheduleSlots, getShiftSelectList, getShiftListPage, renameShift, updateShiftStatus, createShift,
         getOnDutyStaff } from '@/api/appoint'
import { getDepartmentSelectList, getEmployeeList, getClinicRoomListAll, getUserInfo } from '@/api/system'
import { loadDictDataList, DICT_TYPE } from '@/lib/dict-cache'
import { SCHEDULE_TYPE_TEXT, SCHEDULE_TYPE_OPTIONS, SHIFT_SCOPE_TEXT, SHIFT_SCOPE_OPTIONS, SHIFT_SCOPE_OUTPATIENT,
         STAFF_TYPE_TEXT, STAFF_TYPE_OPTIONS, STAFF_TYPE_DOCTOR, staffTypeHasSource, staffTypeText } from '@/lib/scheduleShift'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const departments = ref<any[]>([])
const employees = ref<any[]>([])
const clinicRooms = ref<any[]>([])
const titleMap = ref<Record<string, string>>({})

// ========== 排班模板（周模板驱动） ==========
const templates = ref<any[]>([])
const tplLoading = ref(false)
const tplDialogVisible = ref(false)
const tplDialogTitle = ref('新增模板')
const tplSubmitLoading = ref(false)
const generateLoading = ref(false)
const weekDayMap: Record<number, string> = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }

const tplFormData = ref({
  id: null as number | null,
  deptId: null as number | null,
  deptName: '',
  doctorId: null as number | null,
  doctorName: '',
  /** 岗位类别（sql/195）：模板生成排班时原样带过去；只有医生岗配号源/诊室/挂号费 */
  staffType: STAFF_TYPE_DOCTOR as number,
  weekDay: 1,
  weekParity: 0,
  validFrom: '',
  validUntil: '',
  shiftId: null as string | null,
  startTime: '',
  endTime: '',
  totalSource: 20,
  roomId: null as number | null,
  roomName: '',
  registFee: 0,
  diagnosisFee: 0,
  isExpert: 0,
  expertFee: 0,
  isAppointment: 1,
  appointmentSource: 0,
  status: 1,
  remark: '',
  // 段级号源配置：null=未启用细化（提交后端原样保留）；[]=清空待提交；[...]=整批替换
  slots: null as any[] | null,
})

const weekParityMap: Record<number, string> = { 0: '每周', 1: '单周', 2: '双周' }

// 模板页是「长期资产」：量会一直涨，全量拉下来既慢也没法翻查 → 走分页 + 关键词
const tplSearchForm = ref({
  keyword: '',
  deptId: null as number | null,
  staffType: null as number | null,
  weekDay: null as number | null,
  status: null as number | null,
})
const tplPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

const loadTemplates = async () => {
  tplLoading.value = true
  try {
    const res = await getScheduleTemplateListPage({
      pageNum: tplPagination.value.pageNum,
      pageSize: tplPagination.value.pageSize,
      keyword: tplSearchForm.value.keyword || undefined,
      deptId: tplSearchForm.value.deptId || undefined,
      staffType: tplSearchForm.value.staffType ?? undefined,
      weekDay: tplSearchForm.value.weekDay || undefined,
      status: tplSearchForm.value.status ?? undefined,
    })
    templates.value = res.data?.records || []
    tplPagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载排班模板失败:', error)
  } finally {
    tplLoading.value = false
  }
}

const handleTplSearch = () => {
  tplPagination.value.pageNum = 1
  loadTemplates()
}

const handleTplReset = () => {
  tplSearchForm.value = { keyword: '', deptId: null, staffType: null, weekDay: null, status: null }
  tplPagination.value.pageNum = 1
  loadTemplates()
}

const handleTplPageChange = (val: number) => {
  tplPagination.value.pageNum = val
  loadTemplates()
}

const handleTplSizeChange = (val: number) => {
  tplPagination.value.pageSize = val
  tplPagination.value.pageNum = 1
  loadTemplates()
}

const resetTplForm = () => {
  tplFormData.value = {
    id: null, deptId: null, deptName: '', doctorId: null, doctorName: '', staffType: STAFF_TYPE_DOCTOR,
    weekDay: 1, weekParity: 0, validFrom: '', validUntil: '',
    shiftId: null, startTime: '', endTime: '', totalSource: 20,
    roomId: null, roomName: '', registFee: 0, diagnosisFee: 0, isExpert: 0, expertFee: 0,
    isAppointment: 1, appointmentSource: 0, status: 1, remark: '', slots: null,
  }
}

const handleTplAdd = () => {
  resetTplForm()
  tplShiftDictId.value = null
  loadShiftDict(null)
  tplDialogTitle.value = '新增模板'
  tplDialogVisible.value = true
}

/** 模板换医生 = 换人重新定默认：专家开关/专家费按新医生档案重新带出（仍可手工改） */
const handleTplDoctorChange = (id: any) => {
  const dft = expertDefaultOf(employees.value.find((e: any) => String(e.id) === String(id)))
  if (dft) {
    tplFormData.value.isExpert = dft.isExpert
    tplFormData.value.expertFee = dft.expertFee
  }
}

/**
 * 模板换岗位类别：换的是「规则链」——医生岗配号源/诊室/挂号费，其余岗位是纯出勤。
 * 所以换岗位要把人一起清掉重新选：原来选的医生在「收费」岗位视图里根本不该出现。
 */
const handleTplStaffTypeChange = () => {
  tplFormData.value.doctorId = null
  tplFormData.value.doctorName = ''
  tplFormData.value.slots = null
  if (tplFormData.value.deptId) loadEmployees(Number(tplFormData.value.deptId), tplFormData.value.staffType)
}

// ========== 模板段级号源（按段细化号源，可选） ==========
// 与后端生成侧同口径：半小时切窗；均分「base 给所有段，余数补给前面的段」；
// 预约池先同口径均分，超出段号源的部分顺延到后面有空余的段。

const tplToMinute = (t: string) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5))
const tplToHHmm = (m: number) =>
    `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`

const generateTplSlots = () => {
  const f = tplFormData.value
  if (!f.startTime || !f.endTime) { ElMessage.warning('请先选择班次'); return }
  const s0 = tplToMinute(f.startTime)
  const e0 = tplToMinute(f.endTime)
  if (s0 % 30 !== 0 || e0 % 30 !== 0 || e0 <= s0) {
    ElMessage.warning('该班次时间窗不是半小时整点，无法按段细化')
    return
  }
  const segs: number[][] = []
  let cursor = s0
  while (cursor < e0) {
    const next = Math.min(cursor + 30, e0)
    segs.push([cursor, next])
    cursor = next
  }
  const n = segs.length
  const total = Number(f.totalSource) || 0
  const appt = f.isAppointment === 1 ? (Number(f.appointmentSource) || 0) : 0
  if (appt > total) { ElMessage.warning('预约号源数不能大于号源总数'); return }
  const spread = (amount: number) => {
    const base = Math.floor(amount / n)
    let rem = amount % n
    return segs.map(() => base + (rem-- > 0 ? 1 : 0))
  }
  const totals = spread(total)
  const appts = spread(appt)
  for (let i = 0; i < n; i++) {
    if (appts[i] > totals[i]) {
      let overflow = appts[i] - totals[i]
      appts[i] = totals[i]
      for (let j = n - 1; j >= 0 && overflow > 0; j--) {
        const move = Math.min(totals[j] - appts[j], overflow)
        appts[j] += move
        overflow -= move
      }
    }
  }
  f.slots = segs.map((seg, i) => ({
    startTime: tplToHHmm(seg[0]), endTime: tplToHHmm(seg[1]),
    totalSource: totals[i], appointmentSource: appts[i],
  }))
}

/** Σ段实时对账：后端硬校验「Σ段=主表」，前端提前显示差在哪、拦下必失败的提交 */
const tplSlotSum = computed(() => {
  const rows = tplFormData.value.slots
  if (!Array.isArray(rows) || rows.length === 0) return { total: 0, appt: 0 }
  return {
    total: rows.reduce((s: number, r: any) => s + (Number(r.totalSource) || 0), 0),
    appt: rows.reduce((s: number, r: any) => s + (Number(r.appointmentSource) || 0), 0),
  }
})
const tplSlotExpected = computed(() => ({
  total: Number(tplFormData.value.totalSource) || 0,
  appt: tplFormData.value.isAppointment === 1 ? (Number(tplFormData.value.appointmentSource) || 0) : 0,
}))
const tplSlotMismatch = computed(() =>
    tplSlotSum.value.total !== tplSlotExpected.value.total
    || tplSlotSum.value.appt !== tplSlotExpected.value.appt)

const handleTplEdit = (row: any) => {
  tplFormData.value = {
    id: row.id,
    deptId: row.deptId,
    deptName: row.deptName || '',
    doctorId: row.doctorId,
    doctorName: row.doctorName || '',
    // 历史模板（sql/195 之前）没有岗位类别 → 后端返回 1（DDL 默认值），这里同样兜底成医生
    staffType: row.staffType ?? STAFF_TYPE_DOCTOR,
    weekDay: row.weekDay,
    weekParity: row.weekParity || 0,
    validFrom: row.validFrom || '',
    validUntil: row.validUntil || '',
    shiftId: row.shiftId ? String(row.shiftId) : null,
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    totalSource: row.totalSource,
    roomId: row.roomId,
    roomName: row.roomName || '',
    registFee: row.registFee || 0,
    diagnosisFee: row.diagnosisFee || 0,
    isExpert: row.isExpert || 0,
    expertFee: row.expertFee || 0,
    isAppointment: row.isAppointment || 0,
    appointmentSource: row.appointmentSource || 0,
    status: row.status,
    remark: row.remark || '',
    // 已细化的模板把段配置带进表单（整批替换口径）；未细化保持 null，提交时不动后端现状
    slots: Array.isArray(row.slots) && row.slots.length > 0
        ? row.slots.map((s: any) => ({
            startTime: s.startTime, endTime: s.endTime,
            totalSource: s.totalSource, appointmentSource: s.appointmentSource,
          }))
        : (Array.isArray(row.slots) ? [] : null),
  }
  tplDialogTitle.value = '修改模板'
  if (row.deptId) {
    loadEmployees(Number(row.deptId), tplFormData.value.staffType)
    loadClinicRooms(Number(row.deptId))
  }
  // 标准班次按落库的 shift_id 回显；选项按科室过滤（该科室适用 + 全院通用）
  restoreShiftDict('template', row, row.deptId)
  tplDialogVisible.value = true
}

const handleTplDeptChange = (deptId: number) => {
  tplFormData.value.doctorId = null
  tplFormData.value.doctorName = ''
  tplFormData.value.roomId = null
  tplFormData.value.roomName = ''
  // 换科室：班次选项换成「新科室适用 + 通用」，原选中项可能不在新列表里
  tplShiftDictId.value = null
  loadShiftDict(deptId)
  loadEmployees(deptId, tplFormData.value.staffType)
  loadClinicRooms(deptId)
}

/**
 * 模板是「整个班次窗口」的定义（生成排班的母版）：时间段一律由班次带出，界面不给手改。
 * 需要非常规时段就到「班次字典」建一条，模板只负责选它。
 */
const handleTplSubmit = async () => {
  if (!tplFormData.value.deptId) { ElMessage.warning('请选择科室'); return }
  if (!tplFormData.value.doctorId) { ElMessage.warning('请选择排班人员'); return }
  if (!tplFormData.value.shiftId) { ElMessage.warning('请选择班次'); return }
  if (staffTypeHasSource(tplFormData.value.staffType)
      && tplFormData.value.isAppointment === 1 && tplFormData.value.appointmentSource > tplFormData.value.totalSource) {
    ElMessage.warning('预约号源数不能大于号源总数')
    return
  }
  // 段级号源提交前对账：Σ段=主表是后端硬校验，不一致时提交必失败，前端先拦一次省一次往返
  if (staffTypeHasSource(tplFormData.value.staffType)
      && Array.isArray(tplFormData.value.slots) && tplFormData.value.slots.length > 0 && tplSlotMismatch.value) {
    ElMessage.warning('各段号源/预约池合计与总数不一致，请调整后再保存')
    return
  }
  const dept = departments.value.find((d: any) => d.id === tplFormData.value.deptId)
  const room = clinicRooms.value.find((r: any) => r.id === tplFormData.value.roomId)
  const emp = employees.value.find((e: any) => e.id === tplFormData.value.doctorId)
  if (dept) tplFormData.value.deptName = dept.deptName
  if (room) tplFormData.value.roomName = room.name
  if (emp) tplFormData.value.doctorName = emp.empName

  tplSubmitLoading.value = true
  try {
    await saveScheduleTemplate(tplFormData.value)
    ElMessage.success(tplFormData.value.id ? '模板已更新' : '模板已创建')
    tplDialogVisible.value = false
    loadTemplates()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    tplSubmitLoading.value = false
  }
}

const handleTplDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`删除 ${row.doctorName} ${weekDayMap[row.weekDay]} ${shiftText(row)} 的模板？`, '提示', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
    })
    await deleteScheduleTemplate(row.id)
    ElMessage.success('模板已删除')
    loadTemplates()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const handleTplToggle = async (row: any) => {
  const stop = row.status === 1
  try {
    await updateScheduleTemplateStatus(row.id, stop ? 0 : 1)
    ElMessage.success(stop ? '模板已停用' : '模板已启用')
    loadTemplates()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// ========== 生成预览（dryRun） ==========
const previewVisible = ref(false)
const previewData = ref<any>(null)
const previewLoading = ref(false)

/**
 * 模板生成的目标周 = **下周**，与面板的「上一周/下一周」筛选**无关**。
 *
 * 周切换只是"看哪一周"的筛选条件，它一旦当写入目标参数用，就会出现
 * 「翻到上周想看历史 → 点生成 → 真往上周写班次」这种事故（实测过这个耦合）。
 * 后端 weekOffset 不传时默认也是 1（下周），这里显式传同一个口径，两边一致。
 * 本周剩下的日子要补班走「手工新增」或「复刻」，模板是长期资产、不承担临时补排。
 */
const GENERATE_TARGET_WEEK_OFFSET = 1

/** 下周区间（给按钮文案与预览确认用，本地拼串不用 toISOString） */
const nextWeekRangeLabel = computed(() => {
  const monday = mondayOfPanel(GENERATE_TARGET_WEEK_OFFSET)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  return `${fmtISO(monday).slice(5)} ~ ${fmtISO(sunday).slice(5)}`
})

/** 按模板生成下周排班：先预览确认（含医生名单人工核对），再执行 */
const handleGenerateFromTemplate = async () => {
  previewLoading.value = true
  previewVisible.value = true
  try {
    // 生成/预览跟着面板的岗位类别走：排班员是「先排下周医生出诊，再排下周窗口人力」，
    // 一次只处理一个岗位的模板（后端同样按 staffType 过滤模板集）
    const res: any = await previewScheduleTemplate(GENERATE_TARGET_WEEK_OFFSET, panelDeptId.value, panelStaffType.value)
    previewData.value = res.data || {}
  } catch (error: any) {
    ElMessage.error(error.message || '预览失败')
    previewVisible.value = false
  } finally {
    previewLoading.value = false
  }
}

const handleConfirmGenerate = async () => {
  generateLoading.value = true
  try {
    const res: any = await generateScheduleFromTemplate(GENERATE_TARGET_WEEK_OFFSET, panelDeptId.value, panelStaffType.value)
    ElMessage.success(res.message || '生成完成')
    previewVisible.value = false
    loadPanel()
  } catch (error: any) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generateLoading.value = false
  }
}

// 新增与修改是两个弹窗：新增的医生是「多选」（一批排完一个科室），
// 修改的医生是「单条只读」（换人等于改成别人的班次），两者本来就不是同一个控件。
const dialogVisible = ref(false)
const editVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)

/** 每位医生一份排班参数（新增批量时逐人覆盖；诊室一人一间，号源/费用按人不同） */
interface DoctorRow {
  roomId: number | null
  totalSource: number
  registFee: number
  diagnosisFee: number
  isExpert: number
  expertFee: number
  isAppointment: number
  appointmentSource: number
}

const formData = ref({
  id: null as number | null,
  deptId: null as number | null,
  deptName: '',
  roomId: null as number | null,
  roomName: '',
  doctorId: null as number | null,
  doctorName: '',
  /**
   * 岗位类别（sql/195）：排班对象不再只有医生。
   * 只有 1-医生 才配号源/诊室/挂号费/预约池；其余岗位是纯出勤排班
   * （提交时这些字段由后端一律清零，界面同样不显示，免得填了被静默丢弃还以为存上了）。
   */
  staffType: STAFF_TYPE_DOCTOR as number,
  /** 批量排班：新增时可多选医生（同科室多位医生一次排完）；编辑时长度恒为 1 */
  doctorIds: [] as number[],
  /**
   * 医生 → 该医生的排班参数（诊室/号源/费用/预约）。
   * 这些是「医生 × 班次」级的值：整批共用一个值，挂号台按条读出来就是同质数据
   * （五位医生号源都是 20、挂号费都是 0，看着像数据坏了）。所以每人一行可覆盖，
   * 表单顶上的同名字段在新增弹窗里只当「默认值」，改默认值不回刷已生成的行（要点「应用到全部」）。
   */
  doctorRows: {} as Record<string, DoctorRow>,
  scheduleDate: '',
  /** 批量日期范围（仅新增弹窗）：新增只认这个区间，排一天就起止填同一天 */
  scheduleDateRange: [] as string[],
  /** 班次ID（biz_shift.id）：排班唯一的时间段/班别来源，「班次」下拉选中才有值 */
  shiftId: null as string | null,
  startTime: '',
  endTime: '',
  totalSource: 20,
  // 已挂数：只给修改弹窗当号源下限用（总数不能小于已挂），提交时后端不认它
  usedSource: 0,
  registFee: 0,
  diagnosisFee: 0,
  isExpert: 0,
  expertFee: 0,
  isAppointment: 0,
  appointmentSource: 0,
  status: 1,
})

const shiftOptions = SCHEDULE_TYPE_OPTIONS

// ========== 班次字典（biz_shift）：排班/模板的唯一时间段与班别来源 ==========
// 字典 id 是字符串（后端 BIGINT ToStringSerializer），v-model 用 string
const shiftDictOptions = ref<any[]>([])
const shiftDictId = ref<string | null>(null)
const tplShiftDictId = ref<string | null>(null)

/** 拉班次：选了科室只显示「该科室适用 + 全院通用」的启用班次；本页面只用门诊/急诊那半册 */
const loadShiftDict = async (deptId?: number | string | null) => {
  try {
    const params: any = { status: 1, useScope: SHIFT_SCOPE_OUTPATIENT }
    if (deptId) params.deptId = deptId
    const res = await getShiftSelectList(params)
    shiftDictOptions.value = res.data || []
    return shiftDictOptions.value
  } catch (error) {
    console.error('加载班次失败:', error)
    return []
  }
}

/**
 * 班次回显：编辑时按落库的 shift_id 还原下拉。
 * 班次已停用 / 不适用于本科室 → 不在选项里，就不回显（宁空不猜：反查时间会撞「上午门诊 vs 专家门诊（上午）」）。
 */
const restoreShiftDict = (target: 'schedule' | 'template', row: any, deptId: any) => {
  const shiftId = row?.shiftId ? String(row.shiftId) : null
  loadShiftDict(deptId).then((opts: any[]) => {
    const hit = shiftId && opts.some((s: any) => String(s.id) === shiftId)
    const form = target === 'schedule' ? formData.value : tplFormData.value
    if (target === 'schedule') {
      shiftDictId.value = hit ? shiftId : null
    } else {
      tplShiftDictId.value = hit ? shiftId : null
    }
    // 回显不出来（班次停用/删除/跨科室）就连带清掉表单里的 shiftId：
    // 留着一个界面上看不见的班次ID，提交必然被后端拦
    form.shiftId = hit ? shiftId : null
  })
}

const shiftDictLabel = (s: any) => `${s.shiftName} ${s.startTime}~${s.endTime}`

/**
 * 选中班次：时间段由班次带出，表单里不再给改。
 * 排班/模板两个表单共用，target 区分落哪个表单。
 */
const applyShiftDict = (target: 'schedule' | 'template', shiftId: any) => {
  const form = target === 'schedule' ? formData.value : tplFormData.value
  const dictId = target === 'schedule' ? shiftDictId : tplShiftDictId
  const hit = shiftDictOptions.value.find((s: any) => String(s.id) === String(shiftId))
  if (!hit) {
    // 选项被清空只可能是班次停用/删除后从列表里消失了：把选中值一起清掉，别留一个提交必失败的假选中
    dictId.value = null
    form.shiftId = null
    form.startTime = ''
    form.endTime = ''
    return
  }
  form.shiftId = String(hit.id)
  const winChanged = form.startTime !== hit.startTime || form.endTime !== hit.endTime
  form.startTime = hit.startTime
  form.endTime = hit.endTime
  // 模板换班次=换时间窗：已细化的段配置不再铺满新窗，留着保存必失败 → 就地清空（生成时回退半小时均分）
  if (target === 'template' && winChanged && Array.isArray(tplFormData.value.slots) && tplFormData.value.slots.length > 0) {
    tplFormData.value.slots = []
    ElMessage.info('班次已变更，原按段细化号源配置已清空')
  }
}

// ========== 班次字典维护（查询/分页/新增/编辑） ==========
// 已有班次的编辑只开放改名（时间/科室/类型是铺底口径，界面不可改）；
// 新增是完整建条（名称/起止时间/科室/类型），复用后端 shiftUpsert 的校验。
const shifts = ref<any[]>([])
const shiftLoading = ref(false)
const shiftKeyword = ref('')
// 适用域筛选：不选就是两册混列（护理班次在这里能看见、但不进门诊排班下拉）
const shiftUseScope = ref<number | null>(null)
const shiftPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

const loadShifts = async () => {
  shiftLoading.value = true
  try {
    const res = await getShiftListPage({
      pageNum: shiftPagination.value.pageNum,
      pageSize: shiftPagination.value.pageSize,
      keyword: shiftKeyword.value || undefined,
      useScope: shiftUseScope.value ?? undefined,
    })
    shifts.value = res.data?.records || []
    shiftPagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载班次字典失败:', error)
  } finally {
    shiftLoading.value = false
  }
}

const handleShiftSearch = () => {
  shiftPagination.value.pageNum = 1
  loadShifts()
}

const handleShiftPageChange = (val: number) => {
  shiftPagination.value.pageNum = val
  loadShifts()
}

const handleShiftSizeChange = (val: number) => {
  shiftPagination.value.pageSize = val
  shiftPagination.value.pageNum = 1
  loadShifts()
}

const shiftDeptName = (deptId: any) => {
  if (!deptId) return '全院通用'
  const hit = departments.value.find((d: any) => String(d.id) === String(deptId))
  return hit?.deptName || `科室${deptId}`
}

// ---------- 编辑弹窗：展示完整详情，名称 + 启用状态可改 ----------
const shiftEditVisible = ref(false)
const shiftEditRow = ref<any>(null)
const shiftEditName = ref('')
const shiftEditStatus = ref(1)
const shiftEditUseScope = ref(1)
const shiftEditSaving = ref(false)

const handleShiftEdit = (row: any) => {
  shiftEditRow.value = row
  shiftEditName.value = row.shiftName
  shiftEditStatus.value = row.status ?? 1
  shiftEditUseScope.value = Number(row.useScope ?? 1)
  shiftEditVisible.value = true
}

const handleShiftEditSubmit = async () => {
  const name = shiftEditName.value.trim()
  if (!name) {
    ElMessage.warning('班次名称不能为空')
    return
  }
  shiftEditSaving.value = true
  try {
    const row = shiftEditRow.value
    const nameChanged = name !== row.shiftName
    const statusChanged = shiftEditStatus.value !== row.status
    const scopeChanged = Number(shiftEditUseScope.value) !== Number(row.useScope ?? 1)
    if (scopeChanged) {
      // 改适用域必须整条 upsert（后端只有 rename/updateStatus 两个单列接口，都不碰 use_scope）
      await createShift({
        id: row.id,
        shiftName: name,
        startTime: row.startTime,
        endTime: row.endTime,
        deptId: row.deptId,
        scheduleType: row.scheduleType,
        useScope: shiftEditUseScope.value,
        status: shiftEditStatus.value,
      })
    } else {
      if (nameChanged) await renameShift(row.id, name)
      if (statusChanged) await updateShiftStatus(row.id, shiftEditStatus.value)
    }
    if (!nameChanged && !statusChanged && !scopeChanged) {
      ElMessage.info('名称、状态与适用域均未修改')
    } else {
      ElMessage.success('保存成功')
    }
    shiftEditVisible.value = false
    loadShifts()
    // 排班/模板弹窗「标准班次」选项的 label 带名字，同步刷新
    loadShiftDict(panelDeptId.value)
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    shiftEditSaving.value = false
  }
}

// ---------- 新增弹窗：完整建条（名称/起止时间/科室/类型） ----------
const shiftAddVisible = ref(false)
const shiftAddSaving = ref(false)
const shiftAddForm = ref({
  shiftName: '',
  startTime: '08:00',
  endTime: '12:00',
  deptId: '' as number | '',
  scheduleType: null as number | null,
  useScope: SHIFT_SCOPE_OUTPATIENT,
  status: 1,
})

const shiftAddDuration = computed(() => {
  const toMin = (t: string) => (t ? Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5)) : NaN)
  const diff = toMin(shiftAddForm.value.endTime) - toMin(shiftAddForm.value.startTime)
  return Number.isFinite(diff) && diff > 0 ? diff : 0
})

const handleShiftAdd = () => {
  shiftAddForm.value = { shiftName: '', startTime: '08:00', endTime: '12:00', deptId: null, scheduleType: null, useScope: SHIFT_SCOPE_OUTPATIENT, status: 1 }
  shiftAddVisible.value = true
}

const handleShiftAddSubmit = async () => {
  const f = shiftAddForm.value
  if (!f.shiftName.trim()) { ElMessage.warning('请填写班次名称'); return }
  if (!f.startTime || !f.endTime) { ElMessage.warning('请选择起止时间'); return }
  if (f.endTime <= f.startTime) { ElMessage.warning('结束时间必须晚于开始时间（不支持跨零点）'); return }
  shiftAddSaving.value = true
  try {
    await createShift({
      shiftName: f.shiftName.trim(),
      startTime: f.startTime,
      endTime: f.endTime,
      deptId: f.deptId,
      scheduleType: f.scheduleType,
      useScope: f.useScope,
      status: f.status,
    })
    ElMessage.success('班次已创建')
    shiftAddVisible.value = false
    handleShiftSearch()
    loadShiftDict(panelDeptId.value)
  } catch (error: any) {
    ElMessage.error(error.message || '新增失败')
  } finally {
    shiftAddSaving.value = false
  }
}

// 本地日期格式化。别用 toISOString()：那是 UTC，北京时间每天 0~8 点会拿到「昨天」
const fmtISO = (d: Date) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

const dateRangeShortcuts = [
  {
    text: '今日',
    value: () => {
      const today = new Date()
      const dateStr = fmtISO(today)
      return [dateStr, dateStr]
    }
  },
  {
    text: '本周',
    value: () => {
      const today = new Date()
      const day = today.getDay()
      const diff = day === 0 ? 6 : day - 1
      const start = new Date(today)
      start.setDate(today.getDate() - diff)
      const end = new Date(start)
      end.setDate(start.getDate() + 6)
      return [fmtISO(start), fmtISO(end)]
    }
  },
  {
    text: '下周',
    value: () => {
      const today = new Date()
      const day = today.getDay()
      const diff = day === 0 ? 6 : day - 1
      const start = new Date(today)
      start.setDate(today.getDate() - diff + 7)
      const end = new Date(start)
      end.setDate(start.getDate() + 6)
      return [fmtISO(start), fmtISO(end)]
    }
  },
  {
    text: '本月',
    value: () => {
      const today = new Date()
      const first = new Date(today.getFullYear(), today.getMonth(), 1)
      const last = new Date(today.getFullYear(), today.getMonth() + 1, 0)
      return [fmtISO(first), fmtISO(last)]
    }
  }
]

const statusMap: Record<number, string> = { 0: '停用', 1: '正常' }
const statusColors: Record<number, string> = { 0: 'danger', 1: 'success' }

const dayMap: Record<number, string> = { 0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六' }

const shiftColors: Record<number, string> = {
  1: 'bg-blue-100 text-blue-700',
  2: 'bg-emerald-100 text-emerald-700',
  3: 'bg-purple-100 text-purple-700',
  4: 'bg-indigo-100 text-indigo-700',
  5: 'bg-slate-200 text-slate-700',
}

// 班次文案唯一口径在 lib/scheduleShift.js（1上午/2下午/3全天/4凌晨/5夜班）
const shiftLabel: Record<number, string> = SCHEDULE_TYPE_TEXT

/**
 * 面板/提示上的班次名：**字典名优先**（「专家门诊」「急诊前夜班」这种才是排班员说的班次），
 * 退回班别（后端按 shift_id 补的派生值），都没了才是「未分类」——不猜。
 */
const shiftText = (row: any) => row?.shiftName || shiftLabel[row?.scheduleType] || '未分类'
const shiftColor = (row: any) => shiftColors[row?.scheduleType] || 'bg-slate-50 text-slate-600'

/** 格子 tooltip：紧凑密度下时段/占用/诊室/预约余量的唯一出口（不丢信息） */
const cellTooltip = (s: any) => {
  const parts = [
    `${shiftText(s)} ${s.startTime || ''}-${s.endTime || ''}`.trim(),
    `已挂 ${usedOf(s)}/${s.totalSource}`,
  ]
  if (s.roomName) parts.push(s.roomName)
  if (!s.roomName && s.status !== 0) parts.push('未排诊室')
  if (s.isExpert === 1) parts.push('专家')
  if ((s.appointmentSource || 0) > 0) {
    parts.push(`约余${Math.max((s.appointmentSource || 0) - (s.usedAppointmentSource || 0), 0)}`)
  }
  if (s.status === 0) parts.push('已停诊')
  return parts.join(' · ')
}

// ========== 周视图排班面板 ==========
const activeView = ref('panel')
const panelDeptId = ref<number | null>(null)
const panelDoctorId = ref<number | null>(null)
/**
 * 面板的岗位类别（sql/195）：默认落在「医生」——排班面板的主活是排出诊班，
 * 但它同时也是窗口/护理人力的排班入口，切到别的岗位就换一套名册与班次。
 * 置空 = 全部岗位混列（只在排查数据时用，日常不用：号源列在出勤岗上一串 0 没有意义）。
 */
const panelStaffType = ref<number>(STAFF_TYPE_DOCTOR)
/**
 * 选中的"科室+医生"组合键（`deptId_doctorId`）。
 * 不能只存 doctorId：同一个 doctor_id 会横跨多个科室（实测 2098255864065458177 同时排在
 * 全科医学科与呼吸内科），只按 id 过滤会把几个科室的班次一起拉出来。
 */
const panelDoctorKey = ref<string | null>(null)
const panelWeekOffset = ref(0) // 0=本周，1=下周，-1=上周
const panelLoading = ref(false)
const panelSchedules = ref<any[]>([])
const panelDays = ref<string[]>([])
const replicateLoading = ref(false)

// ========== 今日在岗（排班的下游出口，sql/196）==========
/**
 * 排班表只是「计划」，业务流程真正要问的是「此刻这个科室谁在班」——这里就是那个答案。
 * 全岗位混排（医生/护理/医技/药学/收费/行政），跟 panels 的单一岗位视角互补：
 * 面板回答「这周怎么排」，这里回答「今天此刻谁顶着」。
 * 科室跟随排班面板的科室（不另起一套筛选状态），双份状态迟早打架。
 */
const onDutyList = ref<any[]>([])
const onDutyLoading = ref(false)
const onDutyMoment = ref('')

const loadOnDuty = async () => {
  if (!panelDeptId.value) {
    onDutyList.value = []
    return
  }
  onDutyLoading.value = true
  try {
    // onDutyOnly=true：只要此刻真正在班的（跨零点夜班由后端 ShiftCoverUtil 判定）
    const res = await getOnDutyStaff({ deptId: panelDeptId.value, onDutyOnly: true })
    onDutyList.value = res.data || []
    const d = new Date()
    onDutyMoment.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (error) {
    console.error('加载今日在岗失败:', error)
    onDutyList.value = []
  } finally {
    onDutyLoading.value = false
  }
}

/** 在岗名单按岗位分组：医生一组、护理一组……顺序跟 STAFF_TYPE_OPTIONS 一致 */
const onDutyGroups = computed(() => {  const map = new Map<number, any[]>()
  for (const s of onDutyList.value) {
    const key = Number(s.staffType)
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(s)
  }
  return [...map.entries()]
    .sort((a, b) => a[0] - b[0])
    .map(([staffType, list]) => ({ staffType, staffTypeName: staffTypeText(staffType), list }))
})

// 切科室 = 换一批人：在岗名单必须跟着变，否则会停留在上一个科室的人身上
watch(() => panelDeptId.value, () => { loadOnDuty() })

/** 当前科室名。id 在后端 ToStringSerializer 出来是字符串，两边都用 String 比，别用 === */
const onDutyDeptName = computed(() => {
  const hit = departments.value.find(d => String(d.id) === String(panelDeptId.value))
  return hit ? hit.deptName : ''
})

/**
 * 人员名册：**在岗人员全量**（`/system/employee/selectList`，按岗位类别 staffType + status=1）。
 *
 * 面板行 = 名册 ∪ 排班里真实出现的医生。为什么必须带名册：
 * 没排班的医生在排班数据里根本不存在，只按排班去重出不来行 —— 排班员看到的就是
 * "本周谁排了"，而看不到"谁还没排"，而后者才是排班这份工作的主语（排班 = 给没排的人排）。
 * 所以本周无班次的医生也要占一行，七格全空，点格子「＋」直接补班。
 *
 * 反向不能省：排班里的医生可能是外院/多点执业的（sys_employee 里查不到），
 * 只按名册出行会**漏掉真实排班**，所以两边取并集。
 */
const panelRoster = ref<any[]>([])

/** 号源占用工具：used = total - available（格子里展示「已挂/总」） */
const usedOf = (s: any) => (s.totalSource || 0) - (s.availableSource || 0)

/**
 * 统计专用的全量周排班（不带医生筛选）。
 * 为什么单独存一份：统计卡片的口径必须稳定在「科室 × 本周」——如果跟着医生筛选走，
 * 选中某位医生后 panelSchedules 只剩他一个人的班次，「应排/实排覆盖率」就没有意义了。
 * 未选医生时 panelSchedules 直接复用这一份（只发一次请求）；选了医生才多发一次。
 */
const panelAllSchedules = ref<any[]>([])

/** 本周排班总览：六张置顶卡片（随上周/下周切换联动，口径 = 当前科室筛选 × 面板所在周） */
const panelStats = computed(() => {
  const list = panelAllSchedules.value
  const total = list.reduce((a: number, s: any) => a + (s.totalSource || 0), 0)
  const shifts = list.length
  const stopped = list.filter((s: any) => s.status === 0).length
  // 应排 = 当前科室筛选下的在岗医生名册；实排 = 本周真有班次的医生（按 科室+医生 去重，
  // 与面板行同粒度）。外院/多点执业医生可能让实排 > 应排，覆盖率封顶 100%。
  const expected = panelRoster.value.length
  const servedKeys = new Set(list.map((s: any) => `${s.deptId}_${s.doctorId}`))
  const served = servedKeys.size
  // 已排班天数：医生 × 日期 去重（同一天上午+下午两个班次算 1 天）
  const dayKeys = new Set(list.map((s: any) => `${s.deptId}_${s.doctorId}_${s.scheduleDate}`))
  // 平均单班次排号量 = 总号源 / 班次数
  return {
    expected,
    served,
    coveragePct: expected ? Math.min(100, Math.round((served / expected) * 100)) : (served ? 100 : 0),
    plannedDays: dayKeys.size,
    total,
    shifts,
    stopped,
    avgPerShift: shifts ? Math.round(total / shifts) : 0,
  }
})

const panelWeekLabel = computed(() => {
  if (!panelDays.value.length) return ''
  return `${panelDays.value[0]} ~ ${panelDays.value[6]}`
})

/** 今天（YYYY-MM-DD）：面板上「已过日期不再排班」的判定基准，ISO 串可直接字典序比较 */
const todayStr = computed(() => fmtISO(new Date()))

/** 医生维度周汇总：每位医生本周班次数与号源占用（医生视图的轻量形态） */
const doctorSummary = computed(() => {
  const map = new Map<string, { name: string; shifts: number; total: number; used: number; stopped: number }>()
  panelSchedules.value.forEach((s: any) => {
    const key = `${s.deptId}_${s.doctorId}`
    if (!map.has(key)) {
      map.set(key, { name: `${s.deptName || ''} ${s.doctorName || ''}`.trim(), shifts: 0, total: 0, used: 0, stopped: 0 })
    }
    const row = map.get(key)!
    row.shifts++
    row.total += s.totalSource || 0
    row.used += usedOf(s)
    if (s.status === 0) row.stopped++
  })
  return Array.from(map.values()).sort((a, b) => b.used - a.used)
})

/** 面板行：名册 ∪ 排班（去重键 = 科室+医生+姓名，与下拉选项同一个键，过滤才能对得上） */
const doctorRows = computed(() => {
  const map = new Map<string, any>()
  const put = (r: any) => {
    const key = doctorOptionKey(r)
    if (!map.has(key)) {
      map.set(key, {deptId: r.deptId, deptName: r.deptName, doctorId: r.doctorId, doctorName: r.doctorName, title: r.title || ''})
    }
  }
  panelRoster.value.forEach(put)
  panelSchedules.value.forEach(put)
  let rows = Array.from(map.values())
  if (panelDoctorKey.value != null) {
    rows = rows.filter(r => doctorOptionKey(r) === panelDoctorKey.value)
  }
  return rows.sort((a, b) =>
    (a.deptName || '').localeCompare(b.deptName || '') || (a.doctorName || '').localeCompare(b.doctorName || ''))
})

/**
 * 医生下拉选项 = **在岗名册 ∪ 当前排班里真实出现的医生**（与面板行同源，键也同名）。
 *
 * 为什么不能只取一边：
 *   1) 只用排班数据 → **没排班的医生在下拉里不存在**，而排班员最常做的事就是"筛出某人给他排"，
 *      等于最常用的路径被堵死（本次改造的直接原因）；
 *   2) 只用员工表 → 排班里的「医生」可能是**外院/多点执业**的（实测 `biz_schedule.doctor_id`
 *      有 id 在 `sys_employee` 里查不到），会**漏掉真实排班**；同一医生还可能被排在
 *      **非主科室**（实测任永星主科室=全科医学科，也在呼吸内科出诊），按员工主科室过滤同样会漏。
 *
 * ⚠️ 去重键必须包含 `deptId + doctorId + doctorName` 三者，**少任何一个都会静默吞选项**：
 *   实测同一个 `doctor_id=2098255864065458177` 在**同一科室**下挂了两个不同名字
 *   （`VSCH-任永星P8` 与 `E2E-TAB-挂号医生`，都在呼吸内科，属夹具残留）。
 *   - 只按 doctorId 去重 → 跨科室的三个名字塌成 1 项；
 *   - 只按 deptId+doctorId 去重 → 同科室的两个名字仍然塌成 1 项（**丢掉的那项无法被选中**，
 *     且用户在下拉里根本看不到它，属于"列表少了东西但不报错"）。
 *   键 = 面板行的展示粒度，才能保证「下拉里能看到的，就是表格里能筛出来的」。
 */
const doctorOptionKey = (s: any) => `${s.deptId}_${s.doctorId}_${s.doctorName}`

const panelDoctorOptions = computed(() => {
  const map = new Map<string, {key: string; doctorId: any; doctorName: string; deptId: any; deptName: string}>()
  const put = (s: any) => {
    const key = doctorOptionKey(s)
    if (!map.has(key)) {
      map.set(key, {key, doctorId: s.doctorId, doctorName: s.doctorName, deptId: s.deptId, deptName: s.deptName})
    }
  }
  panelRoster.value.forEach((e: any) => put(e))
  panelSchedules.value.forEach((s: any) => {
    if (panelDeptId.value != null && String(s.deptId) !== String(panelDeptId.value)) return
    put(s)
  })
  return Array.from(map.values()).sort((a, b) =>
    (a.deptName || '').localeCompare(b.deptName || '') || (a.doctorName || '').localeCompare(b.doctorName || ''))
})

const mondayOfPanel = (offset: number) => {
  const now = new Date()
  const day = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - day + 1 + offset * 7)
  return monday
}

/**
 * 拉在岗医生名册。只跟科室有关（与翻周无关），所以按科室缓存，翻周不重复请求。
 * 离职/停用（status=0）的不进名册 —— 排班面板是给"还能出诊的人"排的。
 * 但**已排了班的人不受影响**：那部分由 panelSchedules 并集兜住，数据不会凭空消失。
 */
let rosterDeptKey: string | null = null
const loadPanelRoster = async () => {
  // 缓存键要带上岗位类别：换岗位就是换一拨人，沿用旧名册会让「护理」视图里站着一堆医生
  const key = `${panelStaffType.value}_${panelDeptId.value ?? '__ALL__'}`
  if (rosterDeptKey === key) return
  try {
    const params: any = {staffType: panelStaffType.value}
    if (panelDeptId.value) params.deptId = panelDeptId.value
    const res = await getEmployeeList(params)
    const list: any[] = res.data?.records || res.data || []
    panelRoster.value = list
      .filter((e: any) => e.status === 1)
      .map((e: any) => ({
        deptId: e.deptId,
        deptName: e.deptName || departments.value.find((d: any) => String(d.id) === String(e.deptId))?.deptName || '',
        doctorId: e.id,
        doctorName: e.empName,
        title: e.title || '',
      }))
    rosterDeptKey = key
  } catch (error) {
    console.error('加载医生名册失败:', error)
  }
}

const loadPanel = async () => {
  panelLoading.value = true
  try {
    await loadPanelRoster()
    const monday = mondayOfPanel(panelWeekOffset.value)
    const days: string[] = []
    for (let i = 0; i < 7; i++) {
      const d = new Date(monday)
      d.setDate(monday.getDate() + i)
      days.push(fmtISO(d))
    }
    panelDays.value = days
    const params: any = {startDate: days[0], endDate: days[6], staffType: panelStaffType.value}
    if (panelDeptId.value) params.deptId = panelDeptId.value
    // 统计与医生筛选解耦：先拉不带医生条件的全量（统计卡用）；选了医生再拉一份精简集（表格用），
    // 未选医生时两份同源，只发一次请求。
    const res = await getScheduleList(params)
    panelAllSchedules.value = res.data?.records || res.data || []
    if (panelDoctorId.value != null) {
      const filtered = await getScheduleList({...params, staffType: panelStaffType.value, doctorId: panelDoctorId.value})
      panelSchedules.value = filtered.data?.records || filtered.data || []
    } else {
      panelSchedules.value = panelAllSchedules.value
    }
  } catch (error) {
    console.error('加载排班面板失败:', error)
  } finally {
    panelLoading.value = false
  }
}

/**
 * 面板默认科室 = 「我的科室」。
 *
 * 排班管理是按科室运维的（科主任/门诊部看自己科），所以默认落在当前用户主科室；
 * 管理员/门诊部要看全院就点下拉右侧的清除，回到「全部科室」。
 *
 * 四个边界（都实测过，别想当然）：
 *   1) `/auth/info` 的 deptId 必须**先确认科室下拉已就绪**再落值，否则 el-select 显示成裸数字
 *      （`applyPanelDefaultDept` 用 `departments` 找 option，找不到就不设 —— 这是对的）；
 *   2) **落值后要自己再拉一次面板**。onMounted 里 `loadPanel()` 是无参首屏拉取，默认科室是
 *      异步拿到的，晚于它 —— 不补拉就会出现「下拉显示产前诊断中心、表格却是全院数据」
 *      这种 UI 与数据各说各话的状态（实测踩到）。
 *   3) 取不到就保持「全部科室」，默认值不该成为故障点；
 *   4) **只在首次生效**（`deptDefaultApplied` 守卫），否则用户手动清空科室后
 *      任何一次刷新都会把它弹回来 —— 那是"默认值"变成了"强制值"。
 */
let deptDefaultApplied = false

const applyPanelDefaultDept = async (deptId: any) => {
  if (deptDefaultApplied || panelDeptId.value != null) return
  if (deptId == null || deptId === '') return
  const hit = departments.value.find((d: any) => String(d.id) === String(deptId))
  if (hit) {
    panelDeptId.value = hit.id
    // 默认科室落地后重拉（首屏那次不带 deptId，不重拉会 UI 与数据不一致）
    await loadPanel()
    // 我的科室一个医生都没有 → 空面板比"全院"更难用，退回全部科室。
    // 科室下拉会显示成"全部科室"（清除态），UI 状态本身就是说明，不再弹文字提示。
    if (!doctorRows.value.length) {
      panelDeptId.value = null
      await loadPanel()
    }
  }
  deptDefaultApplied = true
}

const loadPanelDefaultDept = async () => {
  try {
    const res = await getUserInfo()
    await applyPanelDefaultDept(res.data?.deptId)
  } catch (error) {
    console.error('加载当前用户科室失败，排班默认「全部科室」:', error)
  }
}

const handlePanelDeptChange = () => {
  // 换科室必须清医生：旧医生未必属于新科室，留着会筛出空表且用户不知道为什么
  panelDoctorKey.value = null
  panelDoctorId.value = null
  loadPanel()
}

const handlePanelDoctorChange = () => {
  // 下拉绑的是组合键；同步出 doctorId 供后端下推（后端只按 doctorId 过滤，
  // 所以同 id 跨科室时后端会多给几条，由前端 panelDoctorKey 精确到科室。
  // 这是刻意的：不为一个"同 id 多科室"的脏数据形态给查询接口加参）
  const hit = panelDoctorOptions.value.find(o => o.key === panelDoctorKey.value)
  panelDoctorId.value = hit ? hit.doctorId : null
  loadPanel()
}

/** 换岗位类别 = 换一拨人与一套排班：人员筛选要一起清掉（旧医生不在新岗位的名册里） */
const handlePanelStaffTypeChange = () => {
  panelDoctorKey.value = null
  panelDoctorId.value = null
  loadPanel()
}

const handlePanelWeekShift = (delta: number) => {
  panelWeekOffset.value += delta
  loadPanel()
}

const handlePanelBackToThisWeek = () => {
  panelWeekOffset.value = 0
  loadPanel()
}

/**
 * 某行某天的班次。
 * 必须**同时比科室**：同一 doctor_id 会横跨多个科室（任永星同时排在全科医学科与呼吸内科），
 * 只按医生过滤会把别的科室的班次画到这一行 —— 名册里每人一行后这个错位会直接可见。
 */
const cellsFor = (row: any, date: string) =>
  panelSchedules.value.filter(s =>
    String(s.doctorId) === String(row.doctorId)
    && (row.deptId == null || s.deptId == null || String(s.deptId) === String(row.deptId))
    && s.scheduleDate === date)

/** 该行本周是否有任何班次（名册进来的医生可能一整周都没排） */
const hasShift = (row: any) =>
  panelSchedules.value.some(s =>
    String(s.doctorId) === String(row.doctorId)
    && (row.deptId == null || s.deptId == null || String(s.deptId) === String(row.deptId)))

/** 点空格子：预填科室/医生/日期，直接进新增弹窗 */
const handlePanelCellAdd = (row: any, date: string) => {
  resetForm()
  isEdit.value = false
  shiftDictId.value = null
  formData.value.deptId = row.deptId
  formData.value.deptName = row.deptName
  formData.value.doctorId = row.doctorId
  formData.value.doctorName = row.doctorName
  // 面板空格子进来：医生预填一位（要批量加人再自己在弹窗里多选）
  formData.value.doctorIds = [row.doctorId]
  formData.value.scheduleDate = ''
  formData.value.scheduleDateRange = [date, date]
  if (row.deptId) {
    loadEmployees(row.deptId, formData.value.staffType)
    loadClinicRooms(row.deptId)
    loadShiftDict(row.deptId)
  } else {
    loadShiftDict(null)
  }
  dialogVisible.value = true
}

/** 点已有格子：进编辑弹窗（可改号源/停诊/删除） */
const handlePanelCellEdit = (row: any) => {
  handleEdit(row)
}

/** 日期 + N 天（本地拼串，不用 toISOString） */
const plusDays = (dateStr: string, days: number) => {
  const d = new Date(dateStr + 'T00:00:00')
  d.setDate(d.getDate() + days)
  return fmtISO(d)
}

/**
 * 复刻的目标周 = 当前显示周 + 7 天。
 * 文案必须算出来而不是写死「下周」：面板翻到下周时，"下一周"其实是下下周 ——
 * 写死会让用户以为点错了（按钮说的是本周，干的是下下周）。
 */
const replicateTargetLabel = computed(() => {
  if (panelDays.value.length !== 7) return '下周'
  return `${plusDays(panelDays.value[0], 7).slice(5)}~${plusDays(panelDays.value[6], 7).slice(5)}`
})

/** 把当前显示的这一周整体滚动复制到下一周（停用条目与目标日期已过的条目不复制） */
const handleReplicateNextWeek = async () => {
  const today = fmtISO(new Date())
  const source = panelSchedules.value.filter(s => s.status === 1)
  if (!source.length) {
    ElMessage.warning('当前周没有可复刻的排班')
    return
  }
  const pastCount = source.filter((s: any) => plusDays(s.scheduleDate, 7) < today).length
  try {
    await ElMessageBox.confirm(
        `将把 ${panelDays.value[0]} ~ ${panelDays.value[6]} 的 ${source.length} 条正常排班复制到 ${replicateTargetLabel.value}`
        + (pastCount ? `（其中 ${pastCount} 条的目标日期已过，不会复制）` : '')
        + '，是否继续？',
        '复刻排班到下一周',
        {confirmButtonText: '复刻', cancelButtonText: '取消', type: 'info'}
    )
  } catch {
    return
  }
  replicateLoading.value = true
  let ok = 0
  let skip = 0
  let skipPast = 0
  try {
    for (const s of source) {
      const targetDate = plusDays(s.scheduleDate, 7)
      // 目标日期已过 → 不复制（服务端同样拒绝，这里提前拦并把结论说清楚）
      if (targetDate < today) {
        skipPast++
        continue
      }
      try {
        await createSchedule({
          deptId: s.deptId,
          deptName: s.deptName,
          roomId: s.roomId != null ? Number(s.roomId) : null,
          roomName: s.roomName,
          doctorId: s.doctorId,
          doctorName: s.doctorName,
          // 岗位类别必须原样带过去：漏了会被后端默认成医生，出勤岗一复刻就变成「能挂号的号源池」
          staffType: s.staffType ?? STAFF_TYPE_DOCTOR,
          scheduleDate: targetDate,
          shiftId: s.shiftId ?? null,
          startTime: s.startTime,
          endTime: s.endTime,
          totalSource: s.totalSource,
          registFee: s.registFee || 0,
          diagnosisFee: s.diagnosisFee || 0,
          isExpert: s.isExpert || 0,
          expertFee: s.expertFee || 0,
          isAppointment: s.isAppointment || 0,
          appointmentSource: s.appointmentSource || 0,
          status: 1,
        })
        ok++
      } catch {
        // 同一时段已排班（时间冲突）等，跳过并计数
        skip++
      }
    }
    ElMessage.success(`复刻到 ${replicateTargetLabel.value} 完成：成功 ${ok} 条`
        + (skip ? `，跳过 ${skip} 条（时间冲突、已存在或源排班没挂班次）` : '')
        + (skipPast ? `，${skipPast} 条目标日期已过未复制` : ''))
    loadPanel()
  } finally {
    replicateLoading.value = false
  }
}

const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤：排班员只能给自己的授权科室排班。
    // 这一处同时喂面板筛选（1091）与排班/模板表单（1317/1521/1612），
    // 四处共用一份列表 —— 只要能选的科室就是能排的科室，不会出现"能选不能排"。
    const res = await getDepartmentSelectList({ deptType: 1 })
    departments.value = res.data || []
  } catch (error) {
    console.error('加载科室列表失败:', error)
  }
}

/**
 * 拉「可排班人员」候选（sql/195 起不限医生：护士/技师/药师/收费员都能排）。
 *
 * <b>按岗位类别取人，不按 emp_type</b>：emp_type 是员工档案上的冗余分类，与岗位表
 * sys_employee_post 不一致（档案说护士、岗位表说收费员），传它会在名册里出现
 * 「选得到、排上去才发现岗位不对」的人。后端按 staffType 走「岗位表 → 角色 → 类别」，
 * 传了 staffType 时 deptId 也按岗位所在科室过滤（而不是员工档案的主科室快照）。
 */
const loadEmployees = async (deptId?: number, staffType?: number | null) => {
  try {
    const params: any = { staffType: staffType ?? STAFF_TYPE_DOCTOR }
    if (deptId) params.deptId = deptId
    const res = await getEmployeeList(params)
    employees.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('加载排班人员列表失败:', error)
  }
}

const loadClinicRooms = async (deptId?: number) => {
  try {
    const params: any = { status: 1 }
    if (deptId) params.deptId = deptId
    const res = await getClinicRoomListAll(params)
    clinicRooms.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('加载诊室列表失败:', error)
  }
}

const doctorRowDefaults = (): DoctorRow => ({
  roomId: null,
  totalSource: formData.value.totalSource,
  registFee: formData.value.registFee,
  diagnosisFee: formData.value.diagnosisFee,
  isExpert: formData.value.isExpert,
  expertFee: formData.value.expertFee,
  isAppointment: formData.value.isAppointment,
  appointmentSource: formData.value.appointmentSource,
})

/**
 * 批量排班（多医生）：每位医生一行排班参数，其中诊室必须一人一间。
 * 为什么不共用一间：排队号前缀按诊室编号生成、大屏按诊室导诊，两位医生同一时段挤一间 = 患者跑错房间。
 * 规则：已有的行原样保留（人手工改过的不被冲掉）；新进来的医生按当前「默认值」补一行，
 *       诊室按科室诊室列表顺序取空闲，不够就留空，提交前由校验拦（启用态必须有诊室）。
 */
const ensureDoctorRows = () => {
  const ids = formData.value.doctorIds || []
  const rooms = clinicRooms.value || []
  const prev = formData.value.doctorRows || {}
  const used = new Set<any>(ids.map((id: any) => prev[String(id)]?.roomId).filter(Boolean))
  const map: Record<string, DoctorRow> = {}
  for (const id of ids) {
    const key = String(id)
    const kept = prev[key]
    if (kept) {
      const roomStillValid = kept.roomId != null && rooms.some((r: any) => r.id === kept.roomId)
      map[key] = roomStillValid ? kept : { ...kept, roomId: null }
      if (roomStillValid) used.add(kept.roomId)
      continue
    }
    const row = doctorRowDefaults()
    // 新医生行的专家开关/专家费按该医生档案带出，而不是沿用顶部默认值
    const dft = expertDefaultOf(employees.value.find((e: any) => String(e.id) === String(id)))
    if (dft) {
      row.isExpert = dft.isExpert
      row.expertFee = dft.expertFee
    }
    const free = rooms.find((r: any) => !used.has(r.id))
    if (free) {
      row.roomId = free.id
      used.add(free.id)
    }
    map[key] = row
  }
  formData.value.doctorRows = map
}

/** 把顶部的「默认值」刷进每一行（诊室不动：一间房已经分好了，重刷会把人挪房） */
const applyDefaultsToAllRows = () => {
  const map = formData.value.doctorRows || {}
  for (const key of Object.keys(map)) {
    const { roomId, ...rest } = doctorRowDefaults()
    map[key] = { ...map[key], ...rest }
  }
  formData.value.doctorRows = { ...map }
  ElMessage.success(`已把默认值应用到 ${Object.keys(map).length} 位人员`)
}

/** 选医生（或科室诊室列表到位）都要重算分配 */
watch(() => formData.value.doctorIds, ensureDoctorRows, { deep: true })
watch(clinicRooms, ensureDoctorRows)

const doctorNameOf = (id: any) => (employees.value.find((e: any) => e.id === id)?.empName) || ''

/** 修改弹窗换了医生：doctorIds/doctorName 要跟着走（提交时按 doctorIds[0] 出医生） */
const handleEditDoctorChange = (id: any) => {
  formData.value.doctorIds = [id]
  formData.value.doctorName = doctorNameOf(id)
}
const roomNameOf = (id: any) => (clinicRooms.value.find((r: any) => r.id === id)?.name) || ''

/** 新增批量实际会生成的条数：医生数 × 天数（天数只来自日期区间，排一天就是起止同一天） */
const batchPlanCount = computed(() => {
  const doctors = (formData.value.doctorIds || []).length || 0
  const days = formData.value.scheduleDateRange?.length === 2 ? batchDays(formData.value.scheduleDateRange).length : 0
  return doctors * days
})

const batchPlanDesc = computed(() => {
  const days = formData.value.scheduleDateRange?.length === 2 ? batchDays(formData.value.scheduleDateRange).length : 0
  const doctors = (formData.value.doctorIds || []).length || 0
  return days && doctors
    ? `${doctors} 位${staffTypeText(formData.value.staffType)}人员 × ${days} 天 = ${batchPlanCount.value} 条` : ''
})

/** 日期范围 → 逐天（本地时区拼串，别用 toISOString：UTC 在北京时间 0~8 点会取到昨天） */
const batchDays = (range: string[]) => {
  const days: string[] = []
  if (!range || range.length !== 2 || !range[0] || !range[1]) return days
  const start = new Date(range[0])
  const end = new Date(range[1])
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) days.push(fmtISO(d))
  return days
}

const handleDeptChange = (deptId: number) => {
  formData.value.doctorId = null
  formData.value.doctorName = ''
  formData.value.doctorIds = []
  formData.value.doctorRows = {}
  formData.value.roomId = null
  formData.value.roomName = ''
  // 换科室：班次选项换成「新科室适用 + 通用」，原选中项可能不在新列表里 → 一律清掉重选
  shiftDictId.value = null
  formData.value.shiftId = null
  formData.value.startTime = ''
  formData.value.endTime = ''
  loadShiftDict(deptId)
  loadEmployees(deptId, formData.value.staffType)
  loadClinicRooms(deptId)
}

/**
 * 换岗位类别：换的是「规则链」，所以人必须重选（原来选的医生在收费岗视图里根本不该出现），
 * 诊室/号源/费用这些只对医生有意义的值也一起回到默认。
 */
const handleStaffTypeChange = () => {
  formData.value.doctorId = null
  formData.value.doctorName = ''
  formData.value.doctorIds = []
  formData.value.doctorRows = {}
  formData.value.roomId = null
  formData.value.roomName = ''
  if (formData.value.deptId) loadEmployees(Number(formData.value.deptId), formData.value.staffType)
}

/** 挂号按时段：半小时一档的时刻（00:00 ~ 23:30 共 48 档），班次字典建条时选起止用 */
const timeOptions: string[] = []
for (let h = 0; h < 24; h++) {
  const hh = String(h).padStart(2, '0')
  timeOptions.push(`${hh}:00`, `${hh}:30`)
}

/** 表单里的时间段：只读展示，值由班次带出（手填时段等于绕开班次字典，统计口径就散了） */
const formTimeRange = computed(() =>
    formData.value.startTime && formData.value.endTime
        ? `${formData.value.startTime}~${formData.value.endTime}` : '')
const tplTimeRange = computed(() =>
    tplFormData.value.startTime && tplFormData.value.endTime
        ? `${tplFormData.value.startTime}~${tplFormData.value.endTime}` : '')

const resetForm = () => {
  formData.value = {
    id: null,
    deptId: null,
    deptName: '',
    roomId: null,
    roomName: '',
    doctorId: null,
    doctorName: '',
    // 默认落在面板当前看的岗位：排班员切到「护理」再点新增，想排的就是护理班
    staffType: panelStaffType.value,
    doctorIds: [],
    doctorRows: {},
    scheduleDate: '',
    scheduleDateRange: [],
    shiftId: null,
    startTime: '',
    endTime: '',
    totalSource: 20,
    // 已挂数：只给修改弹窗当号源下限用（总数不能小于已挂），提交时后端不认它
    usedSource: 0,
    registFee: 0,
    diagnosisFee: 0,
    isExpert: 0,
    expertFee: 0,
    isAppointment: 0,
    appointmentSource: 0,
    status: 1,
  }
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  slotDetailRows.value = []
  shiftDictId.value = null
  // 人员候选按当前岗位类别拉：切到「护理」再点新增，下拉里就该是护士，不是上一次留下的医生
  loadEmployees(undefined, formData.value.staffType)
  loadShiftDict(null)
  dialogVisible.value = true
}

// ========== 时间片段明细（编辑弹窗：号源与占用的事实都在段上，可逐段调号源/预约池/停用） ==========
const slotDetailRows = ref<any[]>([])
const slotDetailLoading = ref(false)
const slotSaving = ref(false)
// 载入时的段快照：与当前值逐段比对出「有没有改过」，没改就禁用保存
const slotSnapshot = ref('')

const slotEditableFields = (rows: any[]) =>
  JSON.stringify(rows.map((s: any) => [s.id, s.totalSource, s.appointmentSource || 0, s.status]))

const slotEditEnabled = computed(() =>
  isEdit.value && formData.value.id && staffTypeHasSource(formData.value.staffType)
  && formData.value.status === 1
  // 过期与停诊后端拒收（历史班次以门诊日志为准），前端直接只读
  && !!formData.value.scheduleDate && formData.value.scheduleDate >= fmtISO(new Date())
)
const slotDirty = computed(() => !!slotDetailRows.value.length && slotEditableFields(slotDetailRows.value) !== slotSnapshot.value)

const loadSlotDetail = async (scheduleId: any) => {
  slotDetailRows.value = []
  slotSnapshot.value = ''
  if (!scheduleId) return
  slotDetailLoading.value = true
  try {
    const res = await getScheduleSlots(scheduleId)
    slotDetailRows.value = res.data || []
    slotSnapshot.value = slotEditableFields(slotDetailRows.value)
  } catch (error) {
    console.error('加载时间片段失败:', error)
  } finally {
    slotDetailLoading.value = false
  }
}

// 段级号源编辑：整批校验整批提交，Σ段由后端写回主表；used 系是事实，前端同样不许改
const handleSaveSlots = async () => {
  for (const s of slotDetailRows.value) {
    if (s.totalSource < (s.usedSource || 0)) {
      ElMessage.warning(`${s.startTime} 段号源不能小于已挂号数（${s.usedSource || 0}）`)
      return
    }
    if ((s.appointmentSource || 0) > s.totalSource) {
      ElMessage.warning(`${s.startTime} 段预约号源不能大于号源总数`)
      return
    }
    if ((s.appointmentSource || 0) < (s.usedAppointmentSource || 0)) {
      ElMessage.warning(`${s.startTime} 段预约号源不能小于已约数（${s.usedAppointmentSource || 0}）`)
      return
    }
  }
  slotSaving.value = true
  try {
    await saveScheduleSlots(formData.value.id, slotDetailRows.value.map((s: any) => ({
      id: s.id, totalSource: s.totalSource, appointmentSource: s.appointmentSource || 0, status: s.status,
    })))
    ElMessage.success('号源调整已保存')
    // Σ段变了：重拉段明细 + 面板（主表的号源总数/剩余是汇总冗余），表单里的汇总值同步对齐
    await loadSlotDetail(formData.value.id)
    formData.value.totalSource = slotDetailRows.value.reduce((a: number, s: any) => a + (s.totalSource || 0), 0)
    formData.value.appointmentSource = slotDetailRows.value.reduce((a: number, s: any) => a + (s.appointmentSource || 0), 0)
    await loadPanel()
  } catch (error: any) {
    ElMessage.error(error.message || '号源调整失败')
  } finally {
    slotSaving.value = false
  }
}

const handleEdit = (row: any) => {
  isEdit.value = true
  formData.value = {
    id: row.id,
    deptId: row.deptId,
    deptName: row.deptName,
    roomId: row.roomId,
    roomName: row.roomName,
    doctorId: row.doctorId,
    doctorName: row.doctorName,
    // 历史排班（sql/195 之前）没有岗位类别 → 后端返回 1（DDL 默认值），这里同样兜底
    staffType: row.staffType ?? STAFF_TYPE_DOCTOR,
    // 修改是单条：人员是单选下拉（排错人要在这一条上改回来），日期只有一天
    doctorIds: [row.doctorId],
    doctorRows: {},
    scheduleDate: row.scheduleDate,
    scheduleDateRange: [],
    shiftId: row.shiftId ? String(row.shiftId) : null,
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    totalSource: row.totalSource,
    usedSource: row.usedSource || 0,
    registFee: row.registFee || 0,
    diagnosisFee: row.diagnosisFee || 0,
    isExpert: row.isExpert || 0,
    expertFee: row.expertFee || 0,
    isAppointment: row.isAppointment || 0,
    appointmentSource: row.appointmentSource || 0,
    status: row.status,
  }
  if (row.deptId) {
    loadEmployees(row.deptId, row.staffType ?? STAFF_TYPE_DOCTOR)
    loadClinicRooms(row.deptId)
  }
  // 标准班次按落库的 shift_id 回显；选项按科室过滤（该科室适用 + 全院通用）
  restoreShiftDict('schedule', row, row.deptId)
  loadSlotDetail(row.id)
  editVisible.value = true
}

const handleSubmit = async () => {
  if (!formData.value.deptId) {
    ElMessage.warning('请选择科室')
    return
  }
  const doctorIds = (formData.value.doctorIds || []).filter(Boolean)
  if (!doctorIds.length) {
    ElMessage.warning(`请选择${staffTypeText(formData.value.staffType)}人员（同科室多位可多选，一次排完）`)
    return
  }
  // 日期口径：编辑 = 单日（改日期=把这条搬到别天）；新增 = 只认日期区间，排一天就起止填同一天。
  // 以前两个控件并列、区间静默覆盖单日，等于两个入口抢一件事——现在各自只剩一个入口。
  let days: string[] = []
  if (isEdit.value) {
    if (!formData.value.scheduleDate) { ElMessage.warning('请选择排班日期'); return }
    days = [formData.value.scheduleDate]
  } else {
    if (formData.value.scheduleDateRange?.length !== 2) {
      ElMessage.warning('请选择排班日期（只排一天就起止填同一天）')
      return
    }
    days = batchDays(formData.value.scheduleDateRange)
    // 逐条发请求，区间手滑选大了就是几百次串行提交，先在入口卡住
    if (days.length > 31) {
      ElMessage.warning(`日期区间最多 31 天（当前 ${days.length} 天），跨月请分批排`)
      return
    }
  }
  if (!days.length) {
    ElMessage.warning('请选择排班日期')
    return
  }
  // 过去的日期一律不可写（新增/修改同口径，日期选择器已禁选，这里兜住手输与批量范围；服务端同样拦）
  const today = todayStr.value
  const pastDays = days.filter(d => d < today)
  if (pastDays.length) {
    ElMessage.warning(`排班日期不能早于今天：${pastDays.join('、')} 已过去，历史排班只读（以门诊日志为准）`)
    return
  }
  // 班次是时间段的唯一入口：没选班次就没有时间窗（服务端 resolveForScheduling 同样拦）
  if (!formData.value.shiftId) {
    ElMessage.warning('请选择班次：时间段由班次带出，需要的时段在「班次字典」里先建一条')
    return
  }
  const dept = departments.value.find((d: any) => d.id === formData.value.deptId)
  if (dept) formData.value.deptName = dept.deptName

  // 医生 × 排班参数：编辑是单条（诊室取表单里的），新增是「每位医生一行、各填各的」
  // （见 doctorRows：号源/费用/预约按人不同，整批共用一个值会让挂号台读出来全是同质数据）
  const pairs = isEdit.value
      ? doctorIds.map((id: any) => ({
          doctorId: id,
          doctorName: formData.value.doctorName || doctorNameOf(id),
          roomId: formData.value.roomId,
          roomName: formData.value.roomName || roomNameOf(formData.value.roomId),
        }))
      : doctorIds.map((id: any) => {
          const row = formData.value.doctorRows[String(id)] || doctorRowDefaults()
          const rid = row.roomId ?? null
          return {
            doctorId: id,
            doctorName: doctorNameOf(id),
            roomId: rid,
            roomName: roomNameOf(rid),
            totalSource: row.totalSource,
            registFee: row.registFee,
            diagnosisFee: row.diagnosisFee,
            isExpert: row.isExpert,
            expertFee: row.isExpert === 1 ? row.expertFee : 0,
            isAppointment: row.isAppointment,
            appointmentSource: row.isAppointment === 1 ? Math.min(row.appointmentSource, row.totalSource) : 0,
          }
        })

  // 启用态必须排诊室（服务端同样校验，这里只是把提示提前到提交前）。
  // 停诊态允许先建后补 —— 停诊的班次不放号，没有诊室不构成患者找不到房间的问题。
  // 只有医生岗要诊室：护士/收费/技师这些出勤岗排的是「人在不在」，不是「在哪个房间接诊」。
  if (formData.value.status !== 0 && staffTypeHasSource(formData.value.staffType)) {
    const noRoom = pairs.filter((p: any) => !p.roomId && !p.roomName)
    if (noRoom.length) {
      ElMessage.warning(`启用状态的医生排班必须选诊室：${noRoom.map((p: any) => p.doctorName).join('、')} 还没分到诊室（分诊台按诊室编号发号，缺诊室患者找不到房间）`)
      return
    }
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      // 编辑模式只修改单条记录
      const p = pairs[0]
      await updateSchedule({ ...formData.value, ...p, scheduleDate: days[0] })
      ElMessage.success('修改成功')
    } else {
      // 新增模式：医生 × 日期 逐条建；批量里某条撞车（该医生当天已有排班）不影响其余，但结论要如实报
      let count = 0
      let failed = 0
      let lastErr = ''
      for (const p of pairs) {
        for (const day of days) {
          try {
            await createSchedule({ ...formData.value, ...p, scheduleDate: day })
            count++
          } catch (e: any) {
            failed++
            lastErr = e?.message || ''
          }
        }
      }
      if (failed > 0) {
        ElMessage.warning(`批量排班完成：成功 ${count} 条 / 共 ${count + failed} 条${lastErr ? '（末条失败：' + lastErr + '）' : ''}`)
      } else {
        ElMessage.success(`成功创建 ${count} 条排班（${pairs.length} 位${staffTypeText(formData.value.staffType)}人员 × ${days.length} 天）`)
      }
    }
    dialogVisible.value = false
    editVisible.value = false
    loadPanel()
  } catch (error) {
    ElMessage.error(error.message || (isEdit.value ? '修改失败' : '新增失败'))
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    loadPanel()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// ========== 停诊闭环（影响名单 + 批量退号） ==========
const stopImpactVisible = ref(false)
const stopImpactLoading = ref(false)
const stopImpactList = ref<any[]>([])
const stopImpactSchedule = ref<any>(null)
const batchCancelLoading = ref(false)
const registSourceMap: Record<number, string> = { 1: '窗口', 2: '自助机', 3: '网上', 4: '预约' }
const registStatusMap: Record<number, string> = { 1: '待支付', 2: '已签到', 3: '就诊中', 4: '已就诊' }

/** 停诊确认弹名单：展示该班次在挂患者，可一键批量退号（按渠道还池），再切停诊 */
const handleToggleStatus = async (row: any) => {
  if (row.status !== 1) {
    // 启用：直接切
    try {
      await updateScheduleStatus(row.id, 1)
      ElMessage.success('已启用')
      loadPanel()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
    return
  }
  // 出勤岗（护士/技师/收费…）没有在挂患者，「停诊=通知患者退号」这套流程对它没意义：
  // 直接停用，不弹那份永远是空的影响名单。
  if (!staffTypeHasSource(row.staffType)) {
    try {
      await updateScheduleStatus(row.id, 0)
      ElMessage.success('已停用该出勤班次')
      loadPanel()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
    return
  }
  // 停诊：先拉影响名单
  stopImpactSchedule.value = row
  stopImpactLoading.value = true
  stopImpactVisible.value = true
  try {
    const res: any = await getStopImpact(row.id)
    stopImpactList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取影响名单失败')
    stopImpactVisible.value = false
  } finally {
    stopImpactLoading.value = false
  }
}

const handleBatchCancel = async () => {
  const ids = stopImpactList.value.map((r: any) => r.registId)
  if (!ids.length) return
  batchCancelLoading.value = true
  try {
    const res: any = await batchCancelRegist(ids, `班次停诊（${stopImpactSchedule.value?.doctorName || ''} ${stopImpactSchedule.value?.scheduleDate || ''}）批量退号`)
    ElMessage.success(res.message || '批量退号完成')
    // 退完直接停诊
    await updateScheduleStatus(stopImpactSchedule.value.id, 0)
    stopImpactVisible.value = false
    loadPanel()
  } catch (error: any) {
    ElMessage.error(error.message || '批量退号失败')
  } finally {
    batchCancelLoading.value = false
  }
}

const handleStopOnly = async () => {
  try {
    await updateScheduleStatus(stopImpactSchedule.value.id, 0)
    ElMessage.success('已停诊，请按名单通知患者改约或退号')
    stopImpactVisible.value = false
    loadPanel()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// ========== 加号 ==========
const handleAddSource = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
        `为 ${row.doctorName} ${row.scheduleDate} ${shiftText(row)} 加号（当前总号源 ${row.totalSource}，剩余 ${row.availableSource}），原因将留痕`,
        '专家加号', {
          confirmButtonText: '加号', cancelButtonText: '取消',
          inputPattern: /^[1-9]\d*$/, inputErrorMessage: '请输入正整数',
          inputPlaceholder: '加号数量（1-50）',
        })
    const num = Math.min(50, Math.max(1, parseInt(value)))
    const reason = await ElMessageBox.prompt('加号原因（必填，写入排班留痕）', '加号原因', {
      confirmButtonText: '确定', cancelButtonText: '取消',
      inputPattern: /^.+$/, inputErrorMessage: '原因不能为空',
    })
    await addScheduleSource(row.id, num, reason.value)
    ElMessage.success(`已加号 ${num} 个`)
    loadPanel()
  } catch (error: any) {
    if (error !== 'cancel' && error?.message) ElMessage.error(error.message)
  }
}

const loadTitleDict = async () => {
  try {
    const list = await loadDictDataList(DICT_TYPE.SYS_HOSPITAL_TITLE)
    const map: Record<string, string> = {}
    list.forEach((item: any) => {
      map[item.dictValue] = item.dictLabel
    })
    titleMap.value = map
  } catch {
    // 字典加载失败不影响页面使用
  }
}

const getTitleLabel = (value: string) => {
  return titleMap.value[value] || value || ''
}

// sys_employee.title 存的是中文标签而非字典 value，故按标签的层级关键词着色：
// 正高(主任xx)红 / 副高(副主任xx)橙 / 中级(主治·主管)蓝 / 初级(医士·医师等)灰
const getTitleTagClass = (value: string) => {
  const label = getTitleLabel(value)
  if (!label) return ''
  if (label.includes('副主任')) return 'bg-orange-50 text-orange-600 ring-orange-200'
  if (label.startsWith('主任')) return 'bg-red-50 text-red-600 ring-red-200'
  if (label.includes('主治') || label.includes('主管')) return 'bg-blue-50 text-blue-600 ring-blue-200'
  return 'bg-slate-50 text-slate-500 ring-slate-200'
}

/** 专家号软联动：员工档案 isExpert 优先，职称≥副主任医师兜底；专家费带出档案 expertPrice。只给默认值，开关仍可手工改 */
const expertDefaultOf = (emp: any) => {
  if (!emp) return null
  const label = getTitleLabel(emp.title || '')
  const on = emp.isExpert === 1 || label.includes('副主任') || label.startsWith('主任') ? 1 : 0
  return { isExpert: on, expertFee: on === 1 ? Number(emp.expertPrice) || 0 : 0 }
}

/**
 * 模板表格高度：铺满剩余窗口。卡片顶偏移实测 ~220（Header 64 + main 上边距 + 周统计卡行 + tabs 头），
 * 底部留 16 → 卡片高 = 100vh - 236；卡片内再扣 内边距 32 + 搜索栏 40 + 分页 44 → 表高 = vh - 352。
 * el-table 只认确定高度才能内部滚动，所以给数值而不是百分比。
 */
const tplTableHeight = ref(420)
const calcTplTableHeight = () => {
  tplTableHeight.value = Math.max(240, window.innerHeight - 352)
}

/**
 * 排班面板高度：医生一多就是几十行，不给高度整页就跟着滚，
 * 筛选行和「本周汇总」被顶出屏幕，排完一班还要滚回去找科室下拉 —— 表自己滚，页面不动。
 *
 * 两步实测而不是写死常量：
 *   1) 卡片高 = 窗口底 - 卡片顶（顶部还有周统计卡行 +「今日在岗」卡，后者随在岗人数换行，
 *      高度不固定，写死常量在人多时会漏出一截）；
 *   2) 卡片内 flex 分完之后，把表格区**实测到的剩余高度**交给 el-table 当 max-height
 *      —— 筛选行固定高，下方「本周汇总」有多少占多少，剩下的全给表格，尽量铺满。
 * 用 max-height 而不是 height：只有两三个医生时表就矮着，不必顶着一屏空白。
 */
const panelCard = ref<HTMLElement | null>(null)
const panelTableWrap = ref<HTMLElement | null>(null)
const panelCardHeight = ref(420)
const panelTableMaxHeight = ref(420)
const calcPanelTableHeight = () => {
  const card = panelCard.value
  const wrap = panelTableWrap.value
  if (!card || !wrap) return
  panelCardHeight.value = Math.max(320, window.innerHeight - card.getBoundingClientRect().top - 12)
  // 卡片高度改完要等一帧，flex 分完才知道表格区剩多少（此时量才准）
  requestAnimationFrame(() => {
    panelTableMaxHeight.value = Math.max(240, wrap.clientHeight)
  })
}

// 模板页签 lazy：切过去才挂载，首次进入要补一次加载；resize 时重算表格高度
watch(activeView, (val) => {
  if (val === 'template') {
    calcTplTableHeight()
    loadTemplates()
  }
  nextTick(calcPanelTableHeight)
})

// 面板上方（今日在岗，随人数换行）与下方（本周汇总，随人数换行）都会变高变矮 → 变了就重算
watch(() => [doctorRows.value.length, doctorSummary.value.length, onDutyGroups.value.length], () => {
  nextTick(calcPanelTableHeight)
})

const onWindowResize = () => {
  calcTplTableHeight()
  calcPanelTableHeight()
}

onMounted(() => {
  calcTplTableHeight()
  window.addEventListener('resize', onWindowResize)
  // 必须先拿到科室下拉 → 再把「我的科室」落到真实 option 上 → 再拉面板。
  // 顺序错了会出现「下拉显示 A、表格是全院」的自相矛盾状态（实测踩过）。
  loadDepartments().then(loadPanelDefaultDept).then(() => {
  // 科室列表为空（或取用户科室失败）时兜底拉一次全院，保证面板不是空白
  if (!panelSchedules.value.length && !panelLoading.value) loadPanel()
  })
  loadTitleDict()
  loadTemplates()
  loadShifts()
  nextTick(calcPanelTableHeight)
})
onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
})
</script>

<template>
  <div class="w-full">
    <!-- 周统计总览：页面顶部白底卡片行，与下方 tab 分离；口径 = 当前科室筛选 × 面板所在周，随上周/下周切换联动 -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5" data-testid="panel-stats">
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-coverage"
           :title="`实排 ${panelStats.served} / 应排 ${panelStats.expected}：本周有班次的医生占在岗名册的比例，实排少于应排说明有医生漏排`">
        <p class="text-xl font-semibold text-slate-800">
          {{ panelStats.served }}<span class="text-sm font-normal text-slate-400"> / {{ panelStats.expected }}</span>
        </p>
        <p class="text-xs text-slate-400">应排/实排 · 覆盖 {{ panelStats.coveragePct }}%</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-planned-days" title="本周所有医生累计的排班天数总和（医生×日期去重，同一天多个班次算 1 天）">
        <p class="text-xl font-semibold text-blue-600">{{ panelStats.plannedDays }}</p>
        <p class="text-xs text-slate-400">已排班天数</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-total-source">
        <p class="text-xl font-semibold text-slate-800">{{ panelStats.total }}</p>
        <p class="text-xs text-slate-400">总排班号源量</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-stopped">
        <p class="text-xl font-semibold" :class="panelStats.stopped > 0 ? 'text-red-500' : 'text-slate-800'">
          {{ panelStats.stopped }}
        </p>
        <p class="text-xs text-slate-400">停诊班次数</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
           data-testid="stat-avg-source" title="总号源 ÷ 班次数：横向对比不同医生、不同科室的排班负荷">
        <p class="text-xl font-semibold text-emerald-600">{{ panelStats.avgPerShift }}</p>
        <p class="text-xs text-slate-400">平均单班次排号量</p>
      </div>
    </div>

    <!-- 今日在岗（sql/196）：排班的下游出口 —— 此刻这个科室谁在班。
         全岗位混排，回答的是「今天谁顶着」，跟下方面板回答的「这周怎么排」互补。 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm" data-testid="on-duty-panel">
      <div class="mb-2 flex flex-wrap items-center gap-3">
        <span class="text-base font-medium text-slate-700">今日在岗</span>
        <el-tag size="small" type="info" effect="plain">{{ onDutyMoment || '—' }}</el-tag>
        <span class="text-xs text-slate-400" data-testid="on-duty-count">
          {{ onDutyDeptName || '未选科室' }} ·
          共 {{ onDutyList.length }} 人在岗
        </span>
        <div class="ml-auto flex items-center gap-2">
          <span v-if="onDutyLoading" class="text-xs text-slate-400">加载中…</span>
          <el-button size="small" :icon="Refresh" data-testid="on-duty-refresh" @click="loadOnDuty">刷新</el-button>
        </div>
      </div>

      <div v-if="!panelDeptId" class="py-2 text-sm text-slate-400" data-testid="on-duty-empty">
        请先在下方选择科室
      </div>
      <div v-else-if="onDutyList.length === 0" class="py-2 text-sm text-slate-400" data-testid="on-duty-empty">
        此刻该科室没有在岗排班（可能尚未排班，或正处在两个班次之间的交接空档）
      </div>
      <div v-else class="flex flex-col gap-2" data-testid="on-duty-groups">
        <div v-for="g in onDutyGroups" :key="g.staffType"
             class="flex flex-wrap items-center gap-2"
             :data-testid="`on-duty-group-${g.staffType}`">
          <span class="w-14 shrink-0 text-xs text-slate-500">{{ g.staffTypeName }}</span>
          <el-tag v-for="s in g.list" :key="s.scheduleId"
                  effect="light" type="success" size="large"
                  class="on-duty-tag"
                  :title="`${s.startTime || ''}-${s.endTime || ''}${s.roomName ? ' · ' + s.roomName : ''}`"
                  :data-testid="`on-duty-item`">
            {{ s.staffName }}
            <span class="ml-1 text-xs text-slate-500">
              {{ s.shiftName || `${s.startTime}-${s.endTime}` }}{{ s.roomName ? ' · ' + s.roomName : '' }}
            </span>
          </el-tag>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeView">
      <!-- 周视图排班面板 -->
      <el-tab-pane label="排班面板" name="panel">
        <div ref="panelCard" class="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
             :style="{ height: panelCardHeight + 'px' }">
          <div class="filter-row flex flex-wrap items-center gap-3">
            <span class="text-base font-medium text-slate-700">科室</span>
            <el-select v-model="panelDeptId" placeholder="全部科室" clearable filterable class="!w-48"
                       popper-class="schedule-filter-popper" :fit-input-width="false"
                       data-testid="panel-dept"
                       @change="handlePanelDeptChange">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
            <span class="text-base font-medium text-slate-700">岗位</span>
            <el-select v-model="panelStaffType" class="!w-32" data-testid="panel-staff-type"
                       popper-class="schedule-filter-popper" :fit-input-width="false"
                       @change="handlePanelStaffTypeChange">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <span class="text-base font-medium text-slate-700">人员</span>
            <el-select v-model="panelDoctorKey" placeholder="全部人员" clearable filterable class="!w-52"
                       popper-class="schedule-filter-popper" :fit-input-width="false"
                       data-testid="panel-doctor"
                       @change="handlePanelDoctorChange">
              <el-option v-for="d in panelDoctorOptions" :key="d.key"
                         :label="panelDeptId ? d.doctorName : `${d.doctorName}（${d.deptName}）`"
                         :value="d.key"/>
            </el-select>
            <div class="flex items-center gap-2">
              <el-button @click="handlePanelWeekShift(-1)"><span class="px-0.5 text-base leading-none">‹</span>上一周</el-button>
              <span class="min-w-[190px] text-center text-base font-medium text-slate-600">{{ panelWeekLabel }}</span>
              <el-button @click="handlePanelWeekShift(1)">下一周<span class="px-0.5 text-base leading-none">›</span></el-button>
              <el-button v-if="panelWeekOffset !== 0" link type="primary" @click="handlePanelBackToThisWeek">
                回本周
              </el-button>
            </div>
            <span class="ml-auto flex items-center gap-2">
              <el-button :loading="generateLoading" v-perm="'org:schedule:add'" data-testid="generate-by-template"
                         :title="`目标周固定为下周（${nextWeekRangeLabel}），与上面「上一周/下一周」的浏览筛选无关；已过去的日期会自动跳过`"
                         @click="handleGenerateFromTemplate">按模板生成下周排班</el-button>
              <el-button type="success" :loading="replicateLoading" v-perm="'org:schedule:add'" @click="handleReplicateNextWeek">
                复刻到 {{ replicateTargetLabel }}
              </el-button>
              <el-button type="primary" :icon="Plus" v-perm="'org:schedule:add'" @click="handleAdd">新增排班</el-button>
            </span>
          </div>
          <div ref="panelTableWrap" v-loading="panelLoading" class="mt-3 min-h-0 flex-1 overflow-x-auto">
            <el-table :data="doctorRows" border style="width: 100%" min-width="980"
                      :max-height="panelTableMaxHeight">
              <el-table-column width="170" fixed="left">
                <template #header><span class="text-base font-semibold text-slate-800">{{ staffTypeText(panelStaffType) }}人员</span></template>
                <template #default="{ row }">
                  <div :data-testid="`panel-row-${row.doctorName}`">
                    <p class="text-base font-semibold text-slate-800">
                      {{ row.doctorName }}<span v-if="row.title"
                        class="ml-1 inline-block rounded px-1 py-px align-middle text-[10px] font-normal leading-4 ring-1 ring-inset"
                        :class="getTitleTagClass(row.title)">{{ getTitleLabel(row.title) }}</span>
                    </p>
                    <p class="text-sm text-slate-500">{{ row.deptName }}</p>
                    <p v-if="!hasShift(row)" class="text-sm text-slate-400">本周未排班</p>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-for="d in panelDays" :key="d" align="center" min-width="140">
                <template #header>
                  <p :class="['text-base font-semibold', d === fmtISO(new Date()) ? 'text-blue-700' : 'text-slate-800']">
                    {{ d }} {{ dayMap[new Date(d + 'T00:00:00').getDay()] }}
                  </p>
                </template>
                <template #default="{ row }">
                  <div class="group/cell relative min-h-[60px] py-1">
                    <!-- 「加排班」悬停才浮现（opacity 切换 + 绝对定位，格子尺寸零变化）；已过日期整格只读，按钮直接不渲染 -->
                    <button v-if="d >= todayStr" v-perm="'org:schedule:add'"
                            class="absolute right-1 top-0.5 z-20 rounded border border-blue-200 bg-blue-50 px-1.5 py-0.5 text-[10px] font-medium leading-4 text-blue-600 opacity-0 transition-opacity duration-150 group-hover/cell:opacity-100 hover:bg-blue-100"
                            :data-testid="`panel-add-${row.doctorId}-${d}`"
                            title="为该医生该日补班"
                            @click="handlePanelCellAdd(row, d)">加排班</button>
                    <div :class="['flex flex-col gap-1', d >= todayStr ? 'mt-6' : '']">
                    <div v-for="s in cellsFor(row, d)" :key="s.id"
                         :class="['group relative rounded border px-2.5 py-1.5 text-left leading-5',
                                  s.status === 0
                                    ? 'border-slate-200 bg-slate-100 text-slate-400 line-through'
                                    : 'border-slate-200 bg-white ' + shiftColor(s)]"
                         :title="cellTooltip(s)"
                         :data-testid="`panel-cell-${row.doctorId}-${d}`">
                      <!-- 第一行：班次名居左，label「已挂」+ 已挂/总号源 紧靠右（值加粗） -->
                      <div class="flex items-baseline justify-between">
                        <span class="text-sm" :class="s.status === 0 ? '' : 'font-medium text-slate-700'">{{ shiftText(s) }}</span>
                        <!-- 只有医生岗有号源：出勤岗这一格显示「在岗」，不显示永远为 0 的 已挂/总 -->
                        <span v-if="s.status !== 0 && staffTypeHasSource(s.staffType)" class="text-xs text-slate-600">已挂 <span class="text-base font-bold text-slate-900">{{ usedOf(s) }}/{{ s.totalSource }}</span></span>
                        <span v-else-if="s.status !== 0" class="text-xs font-medium text-emerald-600">在岗</span>
                        <span v-else class="text-sm">停用</span>
                      </div>
                      <div class="mt-0.5 text-xs leading-4 text-slate-600">
                        <p class="truncate">
                          <span v-if="s.status === 0">已停用 · 原 {{ s.totalSource }} 个号源</span>
                          <template v-else>
                            <span>{{ s.startTime || '' }}-{{ s.endTime || '' }}</span><template v-if="staffTypeHasSource(s.staffType)"> · <span :class="s.roomName ? '' : 'font-medium text-amber-600'">{{ s.roomName || '未排诊室' }}</span><span v-if="s.isExpert === 1" class="ml-2 inline-block rounded bg-red-50 px-1 py-px text-[10px] font-medium leading-4 text-red-500 ring-1 ring-inset ring-red-200">专家</span></template>
                          </template>
                        </p>
                      </div>
                      <!-- 操作行：修改/停/加 独占一行。高度常驻预留（opacity 切换），悬停不会撑动卡片。日期已过 → 整条不渲染（服务端同口径拒绝） -->
                      <div v-if="s.status === 1 && d >= todayStr"
                           class="mt-1 flex h-5 items-center justify-end gap-1.5 opacity-0 transition-opacity duration-150 group-hover:opacity-100">
                        <button class="text-xs leading-4 text-blue-600" v-perm="'org:schedule:edit'" title="修改排班"
                                :data-testid="`panel-edit-${row.doctorId}-${d}`"
                                @click.stop="handlePanelCellEdit(s)">修改</button>
                        <span class="text-slate-200">|</span>
                        <button class="text-xs leading-4 text-red-500" v-perm="'org:schedule:delete'"
                                :title="staffTypeHasSource(s.staffType) ? '快捷停诊' : '停用该班次'"
                                @click.stop="handleToggleStatus(s)">停</button>
                        <!-- 加号只给医生出诊班：出勤岗没有号源池（后端同样拒绝），按钮直接不渲染 -->
                        <template v-if="staffTypeHasSource(s.staffType)">
                        <span class="text-slate-200">|</span>
                        <button class="text-xs leading-4 text-blue-600" v-perm="'org:schedule:edit'" title="专家加号"
                                @click.stop="handleAddSource(s)">加</button>
                        </template>
                      </div>
                    </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="doctorRows.length === 0 && !panelLoading" class="py-12 text-center text-sm text-slate-400"
                 data-testid="panel-empty">
              <!-- 到了这里说明「一个医生都没有」（名册空 + 排班空），不是「没人排班」——
                   后者现在照样出一行，不该再走空态 -->
              <template v-if="panelDeptId != null">
                本科室没有在岗{{ staffTypeText(panelStaffType) }}人员 ——
                <el-button link type="primary" @click="panelDeptId = null; loadPanel()">查看全部科室</el-button>
              </template>
              <template v-else>
                没有可排班的在岗{{ staffTypeText(panelStaffType) }}人员（先在员工管理里给员工配好科室与岗位）
              </template>
            </div>
          </div>

          <!-- 医生维度周汇总：本周谁的号最紧张一目了然。
               它自己封顶 + 内部滚（全院视图几百个医生时会铺成十几行），
               不然汇总越长表格越矮 —— 排班面板的主角是表格，不是这块附注。 -->
          <div v-if="doctorSummary.length" class="mt-3 max-h-14 shrink-0 overflow-y-auto border-t border-slate-100 pt-2">
            <p class="mb-2 text-xs font-medium text-slate-500">{{ staffTypeText(panelStaffType) }}本周汇总（按已挂号排序）</p>
            <div class="flex flex-wrap gap-2">
              <div v-for="row in doctorSummary" :key="row.name"
                   class="flex items-center gap-2 rounded-md border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs">
                <span class="font-medium text-slate-700">{{ row.name }}</span>
                <span class="text-slate-400">{{ row.shifts }} 班</span>
                <span class="text-slate-500">已挂 <span class="font-semibold text-blue-600">{{ row.used }}</span>/{{ row.total }}</span>
                <span v-if="row.stopped > 0" class="font-medium text-red-500">停诊 {{ row.stopped }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="排班模板" name="template" lazy>
        <div class="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
             style="height: calc(100vh - 236px); min-height: 420px;">
          <!-- 搜索区：模板是长期资产，量会一直涨 → 条件必须下推后端，前端不切片 -->
          <div class="filter-row mb-2 flex flex-wrap items-center gap-3">
            <el-input v-model="tplSearchForm.keyword" placeholder="搜索医生 / 科室 / 诊室" :prefix-icon="Search"
                      clearable class="!w-56" data-testid="tpl-keyword"
                      @keyup.enter="handleTplSearch" @clear="handleTplSearch" />
            <el-select v-model="tplSearchForm.deptId" placeholder="全部科室" clearable filterable class="!w-44"
                       data-testid="tpl-dept" @change="handleTplSearch">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
            <el-select v-model="tplSearchForm.staffType" placeholder="全部岗位" clearable class="!w-32"
                       data-testid="tpl-staff-type" @change="handleTplSearch">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="tplSearchForm.weekDay" placeholder="全部星期" clearable class="!w-36"
                       data-testid="tpl-weekday" @change="handleTplSearch">
              <el-option v-for="(label, day) in weekDayMap" :key="day" :label="label" :value="Number(day)" />
            </el-select>
            <el-select v-model="tplSearchForm.status" placeholder="全部状态" clearable class="!w-36"
                       data-testid="tpl-status" @change="handleTplSearch">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" data-testid="tpl-search-btn" @click="handleTplSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleTplReset">重置</el-button>
            <div class="ml-auto flex items-center gap-3">
              <el-button :icon="Refresh" @click="loadTemplates">刷新</el-button>
              <el-button type="primary" :icon="Plus" v-perm="'org:schedule:add'" @click="handleTplAdd">新增模板</el-button>
            </div>
          </div>

          <!-- 表格铺满剩余高度，行多时表格内部滚动，分页常驻底部 -->
          <div class="min-h-0 flex-1">
            <el-table :data="templates" v-loading="tplLoading" :height="tplTableHeight" stripe border
                      style="width: 100%" data-testid="tpl-table">
              <el-table-column prop="deptName" label="科室" min-width="110" />
              <el-table-column label="岗位" width="80" align="center">
                <template #default="{ row }">
                  <el-tag size="small" :type="staffTypeHasSource(row.staffType) ? 'primary' : 'info'">
                    {{ staffTypeText(row.staffType) || '—' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="doctorName" label="人员" min-width="100" />
              <el-table-column label="星期几" min-width="90" align="center">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.weekDay === 6 || row.weekDay === 7 ? 'warning' : 'info'">
                    {{ weekDayMap[row.weekDay] || '-' }}
                  </el-tag>
                  <p v-if="(row.weekParity || 0) > 0" class="mt-0.5 text-[10px] text-slate-400">{{ weekParityMap[row.weekParity] }}</p>
                </template>
              </el-table-column>
              <el-table-column label="班次" min-width="90">
                <template #default="{ row }">
                  <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', shiftColor(row)]">
                    {{ shiftText(row) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="时段" min-width="120">
                <template #default="{ row }">
                  <span class="font-mono text-sm text-slate-600">{{ row.startTime }}-{{ row.endTime }}</span>
                </template>
              </el-table-column>
              <el-table-column label="生效期" min-width="180">
                <template #default="{ row }">
                  <span v-if="row.validFrom || row.validUntil" class="text-xs text-slate-500">
                    {{ row.validFrom || '不限' }} ~ {{ row.validUntil || '不限' }}
                  </span>
                  <span v-else class="text-xs text-slate-400">长期有效</span>
                </template>
              </el-table-column>
              <el-table-column label="号源" min-width="80" align="center">
                <template #default="{ row }">
                  <!-- 出勤岗没有号源：显示「—」而不是 0（0 会被读成「号源配错了」） -->
                  <span v-if="staffTypeHasSource(row.staffType)" class="font-medium">{{ row.totalSource }}</span>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column label="预约池" min-width="90" align="center">
                <template #default="{ row }">
                  <span v-if="!staffTypeHasSource(row.staffType)" class="text-xs text-slate-300">—</span>
                  <span v-else-if="(row.appointmentSource || 0) > 0" class="text-xs text-blue-600">划 {{ row.appointmentSource }}</span>
                  <span v-else class="text-xs text-slate-400">不划</span>
                </template>
              </el-table-column>
              <el-table-column prop="roomName" label="诊室" min-width="110" />
              <el-table-column label="挂号费" min-width="90" align="right">
                <template #default="{ row }">
                  <span v-if="staffTypeHasSource(row.staffType)">¥{{ row.registFee || 0 }}</span>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column label="专家" min-width="70" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.isExpert === 1" type="danger" size="small">专家</el-tag>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusColors[row.status]" size="small">{{ statusMap[row.status] || '-' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" class-name="tpl-op-col" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.status === 1" v-perm="'org:schedule:edit'" type="warning" link size="small" @click="handleTplToggle(row)">停用</el-button>
                  <el-button v-else v-perm="'org:schedule:edit'" type="success" link size="small" @click="handleTplToggle(row)">启用</el-button>
                  <el-button type="primary" link :icon="Edit" v-perm="'org:schedule:edit'" @click="handleTplEdit(row)">编辑</el-button>
                  <el-button type="danger" link :icon="Delete" v-perm="'org:schedule:delete'" @click="handleTplDelete(row)">删除</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-10 text-sm text-slate-400">
                  暂无模板。点击「新增模板」配置人员（医生/护理/医技/收费…）的固定班次，之后每周在「排班面板」一键生成
                </div>
              </template>
            </el-table>
          </div>

          <div class="mt-3 flex justify-end">
            <el-pagination
              v-model:current-page="tplPagination.pageNum"
              v-model:page-size="tplPagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="tplPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleTplSizeChange"
              @current-change="handleTplPageChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 班次字典维护：查询/分页/新增；已有班次编辑仅改名（时间/科室/类型由系统铺底，界面不可改） -->
      <el-tab-pane label="班次字典" name="shifts">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-input v-model="shiftKeyword" placeholder="按班次名称搜索" clearable class="!w-56"
                      data-testid="shift-keyword" @keyup.enter="handleShiftSearch" @clear="handleShiftSearch" />
            <el-select v-model="shiftUseScope" placeholder="适用域" clearable class="!w-44" data-testid="shift-scope-filter"
                       :fit-input-width="false" @change="handleShiftSearch">
              <el-option v-for="o in SHIFT_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-button type="primary" :icon="Search" data-testid="shift-search-btn" @click="handleShiftSearch">查询</el-button>
            <el-button type="success" :icon="Plus" data-testid="shift-add-btn" v-perm="'org:schedule:add'" @click="handleShiftAdd">新增字典</el-button>
          </div>
          <el-table :data="shifts" v-loading="shiftLoading" border style="width: 100%" data-testid="shift-dict-table">
            <el-table-column prop="shiftName" label="班次名称" min-width="150" />
            <el-table-column label="适用域" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="Number(row.useScope) === 2 ? 'success' : 'info'" size="small"
                        data-testid="shift-scope-tag">{{ SHIFT_SCOPE_TEXT[Number(row.useScope ?? 1)] || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="起止时间" width="130" align="center">
              <template #default="{ row }">
                <span class="font-mono text-xs text-slate-600">{{ row.startTime }} ~ {{ row.endTime }}</span>
              </template>
            </el-table-column>
            <el-table-column label="时长" width="80" align="center">
              <template #default="{ row }">{{ row.durationMinutes }}分</template>
            </el-table-column>
            <el-table-column label="适用科室" min-width="120">
              <template #default="{ row }">{{ shiftDeptName(row.deptId) }}</template>
            </el-table-column>
            <el-table-column label="班次类型" width="90" align="center">
              <template #default="{ row }">{{ row.scheduleType ? (shiftLabel[row.scheduleType] || '—') : '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" data-testid="shift-edit-btn" v-perm="'org:schedule:edit'" @click="handleShiftEdit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination class="mt-3 justify-end" background layout="total, prev, pager, next, sizes"
                         :total="shiftPagination.total" :current-page="shiftPagination.pageNum"
                         :page-size="shiftPagination.pageSize" :page-sizes="PAGE_SIZES"
                         @current-change="handleShiftPageChange" @size-change="handleShiftSizeChange" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 班次编辑弹窗：展示完整详情，仅名称可改（时间/科室/类型/状态只读） -->
    <el-dialog v-model="shiftEditVisible" title="编辑班次" width="480px" destroy-on-close>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="班次名称" required>
          <el-input v-model="shiftEditName" maxlength="50" show-word-limit data-testid="shift-edit-name"
                    placeholder="班次名称（同科室范围内不可重复）" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="起止时间">
            <el-input :model-value="shiftEditRow ? `${shiftEditRow.startTime} ~ ${shiftEditRow.endTime}` : ''" disabled />
          </el-form-item>
          <el-form-item label="时长">
            <el-input :model-value="shiftEditRow ? `${shiftEditRow.durationMinutes} 分钟` : ''" disabled />
          </el-form-item>
          <el-form-item label="适用科室">
            <el-input :model-value="shiftEditRow ? shiftDeptName(shiftEditRow.deptId) : ''" disabled />
          </el-form-item>
          <el-form-item label="班次类型">
            <el-input :model-value="shiftEditRow?.scheduleType ? (shiftLabel[shiftEditRow.scheduleType] || '—') : '—'" disabled />
          </el-form-item>
        </div>
        <el-form-item label="适用域">
          <el-radio-group v-model="shiftEditUseScope" data-testid="shift-edit-scope">
            <el-radio :value="1">门诊/急诊排班</el-radio>
            <el-radio :value="2">病区护理排班</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="shiftEditStatus" data-testid="shift-edit-status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <p class="text-xs text-slate-400">可修改名称、适用域与启用状态；起止时间/适用科室/班次类型等口径调整需走 SQL 变更。
          改成「病区护理排班」后，这条班次就只会出现在护理排班页，门诊排班下拉里再也选不到它。</p>
      </el-form>
      <template #footer>
        <el-button @click="shiftEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="shiftEditSaving" v-perm="'org:schedule:edit'" @click="handleShiftEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增班次弹窗：完整建条（防重/时间校验在后端 shiftUpsert） -->
    <el-dialog v-model="shiftAddVisible" title="新增字典" width="480px" destroy-on-close>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="班次名称" required>
          <el-input v-model="shiftAddForm.shiftName" maxlength="50" show-word-limit
                    placeholder="如：康复科上午班" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="开始时间" required>
            <el-select v-model="shiftAddForm.startTime" filterable class="w-full" data-testid="shift-add-start">
              <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="结束时间" required>
            <el-select v-model="shiftAddForm.endTime" filterable class="w-full" data-testid="shift-add-end">
              <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
        </div>
        <p class="-mt-2 mb-3 text-xs text-slate-400">时长自动计算：{{ shiftAddDuration ? `${shiftAddDuration} 分钟` : '结束时间需晚于开始时间' }}</p>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="适用科室">
            <el-select v-model="shiftAddForm.deptId" placeholder="请选择适用科室" filterable class="w-full">
              <el-option label="全部科室" value="" />
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="班次类型">
            <el-select v-model="shiftAddForm.scheduleType" placeholder="班次类型" clearable class="w-full">
              <el-option v-for="s in shiftOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="适用域" required>
            <el-select v-model="shiftAddForm.useScope" class="w-full" data-testid="shift-add-scope" :fit-input-width="false">
              <el-option v-for="o in SHIFT_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="状态">
          <el-radio-group v-model="shiftAddForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shiftAddVisible = false">取消</el-button>
        <el-button type="primary" :loading="shiftAddSaving" v-perm="'org:schedule:add'" @click="handleShiftAddSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增排班（批量：医生多选 × 日期区间）与修改排班（单条）是两个弹窗。
         以前一个弹窗靠 v-if="isEdit" 分叉，两套排版挤在一起，改一边就碰坏另一边。 -->
    <el-dialog v-model="dialogVisible" title="新增排班" width="980px" destroy-on-close>
      <el-form label-position="top" :model="formData">
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="科室" required>
            <el-select v-model="formData.deptId" placeholder="输入科室名称搜索" filterable class="w-full" @change="handleDeptChange">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </el-form-item>
          <!-- 岗位类别：排班对象不再只有医生（sql/195）。
               只有「医生」才配号源/诊室/挂号费，换岗位要把人一起重选（不同岗位是不同的人）。 -->
          <el-form-item label="岗位类别" required>
            <el-select v-model="formData.staffType" class="w-full" data-testid="form-staff-type"
                       @change="handleStaffTypeChange">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="`${staffTypeText(formData.staffType)}人员`" required>
            <el-select v-model="formData.doctorIds" multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="1"
                       :placeholder="`选择${staffTypeText(formData.staffType)}人员（可多选，一次排完）`" class="w-full"
                       :disabled="!formData.deptId" data-testid="form-doctor" @change="ensureDoctorRows">
              <el-option v-for="e in employees" :key="e.id" :label="`${e.empName} - ${getTitleLabel(e.title)}`" :value="e.id" />
            </el-select>
          </el-form-item>
        </div>
        <p v-if="!staffTypeHasSource(formData.staffType)" class="-mt-2 mb-3 text-xs text-slate-500">
          {{ staffTypeText(formData.staffType) }}岗位是<b>出勤排班</b>：只排「谁哪天哪个班次在岗」，不放号、不需要诊室、不计挂号费。
        </p>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="排班日期" required data-testid="form-date-range">
            <el-date-picker
                v-model="formData.scheduleDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                class="!w-full"
                :shortcuts="dateRangeShortcuts"
                :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
            />
          </el-form-item>
          <el-form-item label="班次" required>
            <el-select v-model="shiftDictId" placeholder="选择班次（时间段由班次带出）" filterable
                       class="w-full" data-testid="form-shift-dict" @change="(v: any) => applyShiftDict('schedule', v)">
              <el-option v-for="s in shiftDictOptions" :key="s.id" :label="shiftDictLabel(s)" :value="s.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="mb-3 -mt-1 text-xs text-slate-500">
          {{ batchPlanDesc }} · 时间段 {{ formTimeRange || '待选班次' }}（不可手填：统计按班次口径出，需要的时段先到「班次字典」建一条）
        </div>

        <!-- 默认值：只用来给「新加进来的人」补一行，以及一键覆盖已有行。
             号源/费用/预约是「人员 × 班次」级的值，不该整批共用一个数 —— 见下方逐人表格。
             只对医生岗有意义：出勤岗没有号源与费用，整块不显示（免得填了被后端清零还以为存上了）。 -->
        <div v-if="staffTypeHasSource(formData.staffType)" class="mb-3 rounded-lg border border-slate-200 bg-slate-50 p-3">
          <div class="mb-2 flex items-center justify-between">
            <span class="text-sm font-medium text-slate-700">默认值（新加入的人员按此补一行）</span>
            <el-button size="small" :disabled="!(formData.doctorIds || []).length" data-testid="apply-defaults"
                       @click="applyDefaultsToAllRows">应用到全部人员</el-button>
          </div>
          <div class="grid grid-cols-6 gap-3">
            <el-form-item label="号源数量">
              <el-input-number v-model="formData.totalSource" :min="1" :max="999" controls-position="right" class="!w-full" />
            </el-form-item>
            <el-form-item label="挂号费(元)">
              <el-input-number v-model="formData.registFee" :min="0" :precision="2" controls-position="right" class="!w-full" />
            </el-form-item>
            <el-form-item label="诊疗费(元)">
              <el-input-number v-model="formData.diagnosisFee" :min="0" :precision="2" controls-position="right" class="!w-full" />
            </el-form-item>
            <el-form-item label="专家号">
              <el-switch v-model="formData.isExpert" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item v-if="formData.isExpert === 1" label="专家费(元)">
              <el-input-number v-model="formData.expertFee" :min="0" :precision="2" controls-position="right" class="!w-full" />
            </el-form-item>
            <el-form-item label="开放预约">
              <el-switch v-model="formData.isAppointment" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item v-if="formData.isAppointment === 1" label="预约号源">
              <el-input-number v-model="formData.appointmentSource" :min="0" :max="formData.totalSource"
                               controls-position="right" class="!w-full" />
            </el-form-item>
          </div>
          <p class="text-xs text-slate-400">预约号源 = 划给网上预约的专用号，现场窗口不可占用；0 = 不划池</p>
        </div>

        <!-- 医生 × 排班参数：一人一行。诊室必须一人一间（排队号前缀按诊室生成、大屏按诊室导诊，
             两位医生同一时段挤一间 = 患者跑错房间）；号源与费用按人可不同。 -->
        <div v-if="(formData.doctorIds || []).length" class="mb-3 rounded-lg border border-slate-200 p-3"
             data-testid="doctor-room-map">
          <div class="mb-1 text-sm font-medium text-slate-700">
            {{ staffTypeHasSource(formData.staffType)
              ? '人员 × 排班参数（一人一行，诊室不够就留空，提交前会拦）'
              : `本次将为以下 ${(formData.doctorIds || []).length} 位${staffTypeText(formData.staffType)}人员建立出勤排班` }}
          </div>
          <el-table :data="(formData.doctorIds || []).map((id: any) => ({ id, room: formData.doctorRows[String(id)] || doctorRowDefaults() }))" size="small"
                    max-height="240">
            <el-table-column label="人员" width="110">
              <template #default="{ row }">
                <span class="truncate text-sm text-slate-700">{{ doctorNameOf(row.id) }}</span>
              </template>
            </el-table-column>
            <!-- 号源/诊室/费用只对医生岗有意义：出勤岗整块不显示 -->
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="诊室" width="140">
              <template #default="{ row }">
                <el-select v-model="row.room.roomId" placeholder="选择诊室" clearable size="small" class="!w-full">
                  <el-option v-for="r in clinicRooms" :key="r.id" :label="r.name" :value="r.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="号源" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.room.totalSource" :min="1" :max="999" :controls="false" size="small" class="!w-full" />
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="挂号费" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.room.registFee" :min="0" :precision="2" :controls="false" size="small" class="!w-full" />
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="诊疗费" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.room.diagnosisFee" :min="0" :precision="2" :controls="false" size="small" class="!w-full" />
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="专家号" width="80" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.room.isExpert" :active-value="1" :inactive-value="0" size="small" />
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="专家费" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.room.expertFee" :min="0" :precision="2" :controls="false" size="small"
                                 :disabled="row.room.isExpert !== 1" class="!w-full" />
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="开放预约" width="90" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.room.isAppointment" :active-value="1" :inactive-value="0" size="small" />
              </template>
            </el-table-column>
            <el-table-column v-if="staffTypeHasSource(formData.staffType)" label="预约号源" min-width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.room.appointmentSource" :min="0" :max="row.room.totalSource" :controls="false"
                                 size="small" :disabled="row.room.isAppointment !== 1" class="!w-full" />
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" data-testid="add-submit" v-perm="'org:schedule:add'" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改排班：单条。医生不可换人（换人=别人的班次），日期只有一天 -->
    <el-dialog v-model="editVisible" title="修改排班" width="780px" destroy-on-close>
      <el-form label-position="top" :model="formData">
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="科室">
            <el-input :model-value="formData.deptName" disabled />
          </el-form-item>
          <el-form-item label="岗位类别" required>
            <el-select v-model="formData.staffType" class="w-full" data-testid="edit-staff-type"
                       @change="handleStaffTypeChange">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="`${staffTypeText(formData.staffType)}人员`" required>
            <!-- 修改弹窗的人员是「单选下拉」：排错了人要能在这条上改回来（新增是多选，两个弹窗各自排版）。
                 科室保持只读：换科室等于换一整批人，那是"删了重排"，不是在单条上改。 -->
            <el-select v-model="formData.doctorId" :placeholder="`选择${staffTypeText(formData.staffType)}人员`" filterable class="w-full"
                       :disabled="!formData.deptId" data-testid="edit-doctor" @change="handleEditDoctorChange">
              <el-option v-for="e in employees" :key="e.id" :label="`${e.empName} - ${getTitleLabel(e.title)}`" :value="e.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="排班日期" required data-testid="form-schedule-date">
            <el-date-picker
                v-model="formData.scheduleDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                class="!w-full"
                :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
            />
          </el-form-item>
          <el-form-item v-if="staffTypeHasSource(formData.staffType)" label="诊室" required>
            <el-select v-model="formData.roomId" placeholder="选择诊室" class="w-full" :disabled="!formData.deptId">
              <el-option v-for="r in clinicRooms" :key="r.id" :label="r.name" :value="r.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="班次" required>
            <el-select v-model="shiftDictId" placeholder="选择班次（时间段由班次带出）" filterable
                       class="w-full" data-testid="edit-shift-dict" @change="(v: any) => applyShiftDict('schedule', v)">
              <el-option v-for="s in shiftDictOptions" :key="s.id" :label="shiftDictLabel(s)" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间段">
            <el-input :model-value="formTimeRange" disabled data-testid="edit-time-range" placeholder="选班次后自动带出" />
          </el-form-item>
        </div>
        <!-- 号源/费用/预约只对医生岗有意义（出勤岗这些值后端一律清零，界面不给填） -->
        <template v-if="staffTypeHasSource(formData.staffType)">
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="号源数量" required>
            <el-input-number v-model="formData.totalSource" :min="formData.usedSource || 1" :max="999" class="w-full" />
            <p v-if="formData.usedSource" class="mt-1 text-xs text-slate-400">已挂 {{ formData.usedSource }} 个，总数不能小于它</p>
          </el-form-item>
          <el-form-item label="挂号费(元)">
            <el-input-number v-model="formData.registFee" :min="0" :precision="2" class="w-full" />
          </el-form-item>
          <el-form-item label="诊疗费(元)">
            <el-input-number v-model="formData.diagnosisFee" :min="0" :precision="2" class="w-full" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="专家号">
            <el-switch v-model="formData.isExpert" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="formData.isExpert === 1" label="专家费(元)">
            <el-input-number v-model="formData.expertFee" :min="0" :precision="2" class="w-full" />
          </el-form-item>
          <el-form-item label="开放预约">
            <el-switch v-model="formData.isAppointment" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        </template>
        <div v-if="staffTypeHasSource(formData.staffType) && formData.isAppointment === 1" class="grid grid-cols-3 gap-4">
          <el-form-item label="预约号源数">
            <div class="w-full">
              <el-input-number v-model="formData.appointmentSource" :min="0" :max="formData.totalSource" class="w-full" />
              <p class="mt-1 text-xs text-slate-400">划给网上预约的专用号，现场窗口不可占用；0 = 不划池</p>
            </div>
          </el-form-item>
        </div>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <!-- 时间片段明细：号源与占用的事实落在段上，主表只是 Σ段 汇总；可编辑时逐段调号源/预约池/停用 -->
      <div class="mt-2 rounded-lg border border-slate-200 bg-slate-50 p-3" data-testid="slot-detail">
        <div class="mb-2 flex items-center justify-between">
          <span class="text-sm font-medium text-slate-700">时间片段（半小时一档）</span>
          <div class="flex items-center gap-2">
            <span v-if="slotDetailRows.length" class="text-xs text-slate-400">
              共 {{ slotDetailRows.length }} 段 · 余 {{ slotDetailRows.reduce((a: number, s: any) => a + (s.availableSource || 0), 0) }} / {{ slotDetailRows.reduce((a: number, s: any) => a + (s.totalSource || 0), 0) }}
            </span>
            <el-button v-if="slotEditEnabled" size="small" type="primary" plain
                       :disabled="!slotDirty" :loading="slotSaving"
                       data-testid="slot-save" @click="handleSaveSlots">保存号源调整</el-button>
          </div>
        </div>
        <p v-if="slotEditEnabled && slotDetailRows.length" class="mb-1 text-xs text-slate-400">
          逐段调整号源/预约池/停用，整批保存生效；已挂数与已约数是事实，不能改小
        </p>
        <p v-if="slotDetailLoading" class="py-2 text-center text-xs text-slate-400">加载中…</p>
        <p v-else-if="!slotDetailRows.length" class="py-2 text-center text-xs text-slate-400">
          {{ staffTypeHasSource(formData.staffType) ? '该排班暂无片段明细（保存后自动按半小时切分生成）' : '出勤排班不放号，不切分时间段' }}
        </p>
        <div v-else class="max-h-56 overflow-y-auto">
          <table class="w-full text-xs">
            <thead>
              <tr class="text-left text-slate-500">
                <th class="py-1.5 pr-2 font-medium">时段</th>
                <th class="py-1.5 pr-2 font-medium">总号</th>
                <th class="py-1.5 pr-2 font-medium">已挂</th>
                <th class="py-1.5 pr-2 font-medium">剩余</th>
                <th class="py-1.5 pr-2 font-medium">预约池</th>
                <th class="py-1.5 pr-2 font-medium">约余</th>
                <th class="py-1.5 font-medium">{{ slotEditEnabled ? '停用' : '状态' }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in slotDetailRows" :key="s.id" class="border-t border-slate-200 text-slate-700">
                <td class="py-1.5 pr-2 font-mono">{{ s.startTime }} ~ {{ s.endTime }}</td>
                <td class="py-1.5 pr-2">
                  <el-input-number v-if="slotEditEnabled" v-model="s.totalSource" size="small"
                                   :min="s.usedSource || 0" :max="999" controls-position="right" class="!w-20" />
                  <template v-else>{{ s.totalSource }}</template>
                </td>
                <td class="py-1.5 pr-2">{{ s.usedSource }}</td>
                <td class="py-1.5 pr-2" :class="s.availableSource <= 0 ? 'text-red-500 font-medium' : 'text-emerald-600'">{{ s.availableSource }}</td>
                <td class="py-1.5 pr-2">
                  <el-input-number v-if="slotEditEnabled" v-model="s.appointmentSource" size="small"
                                   :min="s.usedAppointmentSource || 0" :max="s.totalSource" controls-position="right" class="!w-20" />
                  <template v-else>{{ (s.appointmentSource || 0) > 0 ? s.appointmentSource : '—' }}</template>
                </td>
                <td class="py-1.5 pr-2">{{ (s.appointmentSource || 0) > 0 ? Math.max((s.appointmentSource || 0) - (s.usedAppointmentSource || 0), 0) : '—' }}</td>
                <td class="py-1.5">
                  <el-switch v-if="slotEditEnabled" v-model="s.status" :active-value="1" :inactive-value="0"
                             inline-prompt active-text="用" inactive-text="停" />
                  <span v-else :class="s.status === 1 ? 'text-emerald-600' : 'text-red-500'">{{ s.status === 1 ? '正常' : '停用' }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" data-testid="edit-submit" v-perm="'org:schedule:edit'" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑排班模板弹窗 -->
    <el-dialog v-model="tplDialogVisible" :title="tplDialogTitle" width="680px" destroy-on-close>
      <el-form label-position="top" :model="tplFormData">
        <div class="grid grid-cols-4 gap-4">
          <el-form-item label="科室" required>
            <el-select v-model="tplFormData.deptId" placeholder="输入科室名称搜索" filterable class="w-full" @change="handleTplDeptChange">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="岗位类别" required>
            <el-select v-model="tplFormData.staffType" class="w-full" data-testid="tpl-form-staff-type"
                       @change="handleTplStaffTypeChange">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="`${staffTypeText(tplFormData.staffType)}人员`" required>
            <el-select v-model="tplFormData.doctorId" :placeholder="`选择${staffTypeText(tplFormData.staffType)}人员`"
                       class="w-full" :disabled="!tplFormData.deptId"
                       @change="handleTplDoctorChange">
              <el-option v-for="e in employees" :key="e.id" :label="`${e.empName} - ${getTitleLabel(e.title)}`" :value="e.id" />
            </el-select>
          </el-form-item>
          <!-- 诊室只对医生岗有意义：出勤模板排的是「人在不在」，不是「在哪个房间接诊」 -->
          <el-form-item v-if="staffTypeHasSource(tplFormData.staffType)" label="诊室">
            <el-select v-model="tplFormData.roomId" placeholder="选择诊室" class="w-full" :disabled="!tplFormData.deptId">
              <el-option v-for="r in clinicRooms" :key="r.id" :label="r.name" :value="r.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="星期几" required>
            <el-select v-model="tplFormData.weekDay" class="w-full">
              <el-option v-for="(label, day) in weekDayMap" :key="day" :label="label" :value="Number(day)" />
            </el-select>
          </el-form-item>
          <el-form-item label="单双周">
            <el-select v-model="tplFormData.weekParity" class="w-full">
              <el-option label="每周" :value="0" />
              <el-option label="单周" :value="1" />
              <el-option label="双周" :value="2" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="班次" required>
            <el-select v-model="tplShiftDictId" placeholder="选择班次（时间段由班次带出）" filterable
                       class="w-full" data-testid="tpl-shift-dict" @change="(v: any) => applyShiftDict('template', v)">
              <el-option v-for="s in shiftDictOptions" :key="s.id" :label="shiftDictLabel(s)" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间段">
            <el-input :model-value="tplTimeRange" disabled placeholder="选班次后自动带出" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="生效起始">
            <el-date-picker v-model="tplFormData.validFrom" type="date" value-format="YYYY-MM-DD" placeholder="长期有效" class="!w-full" />
          </el-form-item>
          <el-form-item label="生效截止">
            <el-date-picker v-model="tplFormData.validUntil" type="date" value-format="YYYY-MM-DD" placeholder="长期有效" class="!w-full" />
          </el-form-item>
        </div>
        <!-- 号源/费用/预约只对医生岗有意义：出勤模板（护士/技师/收费…）只描述「谁每周哪天哪个班在岗」 -->
        <template v-if="staffTypeHasSource(tplFormData.staffType)">
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="号源数量" required>
            <el-input-number v-model="tplFormData.totalSource" :min="1" :max="999" class="w-full" />
          </el-form-item>
          <el-form-item label="挂号费(元)">
            <el-input-number v-model="tplFormData.registFee" :min="0" :precision="2" class="w-full" />
          </el-form-item>
          <el-form-item label="诊疗费(元)">
            <el-input-number v-model="tplFormData.diagnosisFee" :min="0" :precision="2" class="w-full" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="专家号">
            <el-switch v-model="tplFormData.isExpert" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="专家费(元)" v-if="tplFormData.isExpert === 1">
            <el-input-number v-model="tplFormData.expertFee" :min="0" :precision="2" class="w-full" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="开放预约">
            <el-switch v-model="tplFormData.isAppointment" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="预约号源数" v-if="tplFormData.isAppointment === 1">
            <el-input-number v-model="tplFormData.appointmentSource" :min="0" :max="tplFormData.totalSource" class="w-full" />
          </el-form-item>
        </div>
        <!-- 段级号源细化（可选）：改的是「生成的排班各半小时段各放几个号」，不改模板自身时间窗 -->
        <div class="rounded border border-slate-200 p-3 mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-slate-700">按段细化号源（可选）</span>
            <div class="flex gap-2">
              <el-button size="small" data-testid="tpl-slot-generate" :disabled="!tplFormData.startTime"
                         @click="generateTplSlots">按半小时均分生成段</el-button>
              <el-button v-if="Array.isArray(tplFormData.slots)" size="small" data-testid="tpl-slot-clear"
                         @click="tplFormData.slots = []">清空细分</el-button>
            </div>
          </div>
          <p class="text-xs text-slate-500 mb-2">
            未细化时生成的排班按半小时均分号源；细化后按每段配置放号。
            段必须铺满班次时间窗{{ tplTimeRange ? `（${tplTimeRange}）` : '' }}，各段合计须等于号源总数与预约号源数；清空并保存后回退均分。
          </p>
          <template v-if="Array.isArray(tplFormData.slots) && tplFormData.slots.length > 0">
            <el-table :data="tplFormData.slots" size="small" max-height="260">
              <el-table-column type="index" label="#" width="48" />
              <el-table-column label="时间段" width="130">
                <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
              </el-table-column>
              <el-table-column label="段号源" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.totalSource" :min="0" :max="999" size="small"
                                   class="!w-full" data-testid="tpl-slot-total" />
                </template>
              </el-table-column>
              <el-table-column label="段预约池" min-width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.appointmentSource" :min="0" :max="999" size="small"
                                   class="!w-full" :disabled="tplFormData.isAppointment !== 1" />
                </template>
              </el-table-column>
            </el-table>
            <p class="mt-2 text-xs" :class="tplSlotMismatch ? 'text-red-500' : 'text-emerald-600'"
               data-testid="tpl-slot-sum">
              合计：号源 {{ tplSlotSum.total }} / 期望 {{ tplSlotExpected.total }}，
              预约池 {{ tplSlotSum.appt }} / 期望 {{ tplSlotExpected.appt }}
              <span v-if="tplSlotMismatch">（不一致，无法保存）</span>
            </p>
          </template>
        </div>
        </template>
        <p v-else class="mb-3 text-xs text-slate-500">
          「{{ staffTypeText(tplFormData.staffType) }}」是<b>出勤模板</b>：生成出来的是出勤排班，
          不放号、不需要诊室、不计挂号费——这几项由后端一律置空。
        </p>
        <el-form-item label="状态">
          <el-radio-group v-model="tplFormData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tplDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tplSubmitLoading" v-perm="['org:schedule:add','org:schedule:edit']" @click="handleTplSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 停诊影响名单 + 批量退号 -->
    <el-dialog v-model="stopImpactVisible" title="停诊确认——该班次在挂患者" width="640px" destroy-on-close>
      <div v-loading="stopImpactLoading">
        <p class="mb-3 text-sm text-slate-600">
          <span class="font-medium">{{ stopImpactSchedule?.doctorName }}</span>
          {{ stopImpactSchedule?.scheduleDate }} {{ shiftText(stopImpactSchedule) }}
          · 共 <span class="font-semibold text-red-500">{{ stopImpactList.length }}</span> 人在挂
        </p>
        <el-table v-if="stopImpactList.length" :data="stopImpactList" border max-height="320" size="small">
          <el-table-column label="患者" min-width="110">
            <template #default="{ row }">{{ row.patientName }}</template>
          </el-table-column>
          <el-table-column label="联系电话" min-width="120">
            <template #default="{ row }">{{ row.phone || '-' }}</template>
          </el-table-column>
          <el-table-column label="渠道" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.registSource === 4 ? 'warning' : 'info'" size="small">
                {{ registSourceMap[row.registSource] || '窗口' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">{{ registStatusMap[row.registStatus] || row.registStatus }}</template>
          </el-table-column>
          <el-table-column label="时段" width="70" align="center">
            <template #default="{ row }">{{ row.slotTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <p v-else class="py-6 text-center text-sm text-slate-400">该班次暂无在挂患者，可直接停诊</p>
        <p v-if="stopImpactList.length" class="mt-3 text-xs text-slate-400">
          批量退号按原挂号渠道回补号源（预约单还预约池、窗口单还现场池）；已缴费的记录需先到收费处退费，失败记录会在结论中提示。
        </p>
      </div>
      <template #footer>
        <el-button @click="stopImpactVisible = false">取消</el-button>
        <el-button v-if="stopImpactList.length" v-perm="'org:schedule:delete'" type="warning" :loading="batchCancelLoading" @click="handleBatchCancel">
          停诊并批量退号（{{ stopImpactList.length }}）
        </el-button>
        <el-button type="danger" v-perm="'org:schedule:delete'" @click="handleStopOnly">仅停诊（名单留存自行通知）</el-button>
      </template>
    </el-dialog>

    <!-- 按模板生成——预览确认 -->
    <el-dialog v-model="previewVisible" title="按模板生成下周排班——预览" width="680px" destroy-on-close>
      <div v-loading="previewLoading">
        <template v-if="previewData">
          <p class="mb-3 text-sm text-slate-600">
            目标周：<span class="font-medium">{{ previewData.weekStart }} ~ {{ previewData.weekEnd }}</span>
            · 将新增 <span class="font-semibold text-blue-600">{{ (previewData.willCreate || []).length }}</span> 条
          </p>
          <div v-if="(previewData.willCreate || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">将生成</p>
            <p v-for="(d, i) in previewData.willCreate" :key="'c' + i" class="text-xs text-slate-600">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipExist || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：已存在（{{ previewData.skipExist.length }}）</p>
            <p v-for="(d, i) in previewData.skipExist" :key="'se' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipOverlap || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：时间冲突（{{ previewData.skipOverlap.length }}）</p>
            <p v-for="(d, i) in previewData.skipOverlap" :key="'so' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipParity || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：单双周不匹配（{{ previewData.skipParity.length }}）</p>
            <p v-for="(d, i) in previewData.skipParity" :key="'sp' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipPast || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：日期已过（{{ previewData.skipPast.length }}）</p>
            <p v-for="(d, i) in previewData.skipPast" :key="'sp2' + i" class="text-xs text-slate-400">· {{ d }}</p>
            <p class="mt-1 text-xs text-amber-600">这些日期已就诊完毕，不能再生成班次（号源池不该出现能挂历史号的口子）</p>
          </div>
          <div v-if="(previewData.skipExpired || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：不在生效日期（{{ previewData.skipExpired.length }}）</p>
            <p v-for="(d, i) in previewData.skipExpired" :key="'sx' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipInvalid || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：模板没选班次或班次已删除（{{ previewData.skipInvalid.length }}）</p>
            <p v-for="(d, i) in previewData.skipInvalid" :key="'si' + i" class="text-xs text-slate-400">· {{ d }}</p>
            <p class="mt-1 text-xs text-amber-600">时间段由班次带出，这类模板要编辑一次选上班次才能生成</p>
          </div>
          <div v-if="(previewData.doctorNames || []).length" class="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-700">
            涉及医生：{{ (previewData.doctorNames || []).join('、') }}——请确认目标周无停诊/请假人员（系统暂无可靠的医生停用数据，出诊人请人工核对）
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" :loading="generateLoading" v-perm="'org:schedule:add'"
                   :disabled="!previewData || !(previewData.willCreate || []).length"
                   @click="handleConfirmGenerate">确认生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 表格线条加深一档：默认 --el-table-border-color #ebeef5 在投影/低对比屏上几乎看不见。
   只作用于本页三个表（排班面板/排班模板/班次字典），slate-300 = #cbd5e1 */
:deep(.el-table) {
  --el-table-border-color: #cbd5e1;
}

/* 页签/表头/筛选控件整体放大一档（用户反馈原 14px 太小）。
   按钮只放大筛选行（.filter-row），表格里 link 操作按钮维持小字号，避免操作列换行 */
:deep(.el-tabs__item) {
  font-size: 16px;
}
:deep(.el-table th.el-table__cell) {
  font-size: 15px;
}
:deep(.filter-row .el-button),
:deep(.filter-row .el-input__inner),
:deep(.filter-row .el-select__placeholder),
:deep(.filter-row .el-select__selected-item) {
  font-size: 15px;
}

/* 模板表操作列：收紧单元格内边距和按钮间距，三个按钮保持单行 */
:deep(.tpl-op-col .cell) {
  padding-left: 4px;
  padding-right: 4px;
}
:deep(.tpl-op-col .el-button + .el-button) {
  margin-left: 6px;
}
</style>
