<script setup lang="ts">
/**
 * 分诊工作站
 *
 * 数据源是 **biz_queue**（/queue/listPage），不是挂号列表 —— 上一版读 /appoint/listPage
 * 再把 registStatus（挂号状态）当队列状态渲染，两套码值混用导致整列串位：
 * 「已挂号」显示未知、「已签到」显示候诊中。
 *
 * 布局按「一个队列占主屏 + 右侧上下文」：左主表格放得下操作按钮，右栏未选中行时是
 * 当前叫号/待办提示，选中行时切换成分诊卡（体征 + 分级 + 诊室）。
 *
 * 闸门口径（2026-09-20 调整）：患者签到即按「4级·非急」入队，未核验的分级**不阻塞接诊** ——
 * 分诊台做的是把危重提上来。所以本页不再用「待分诊」这个说法（会让人以为接不了诊），
 * 统一叫「分级未核验」；唯一保留的硬约束是「队列里还有 1/2 级未接诊时，医生不能跳过他们」。
 */
import {ref, computed, onMounted, onUnmounted} from 'vue'
import {Refresh, Search, Check, Clock, UserFilled, WarningFilled, Bell} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  callPatient,
  recallPatient,
  overdueQueue,
  rejoinQueue,
  checkInByRegist,
  getQueueListPage,
  getQueueStats,
  getConsultingPatients,
  getRevisitTimeoutPatients,
  saveTriage,
  getTriageByQueueId,
  getUncheckedList,
} from '@/api/appoint'
import {getDepartmentSelectList, getEmployeeList, getUserInfo} from '@/api/system'
import {QUEUE_STATUS, TRIAGE_LEVEL, REGIST_TYPE, statusOf} from '@/lib/statusColor'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {shortQueueNo} from '@/lib/utils'

const loading = ref(false)
const rows = ref<any[]>([])
const depts = ref<any[]>([])
const doctors = ref<any[]>([])
// rooms（诊室下拉的数据源）随「分诊卡诊室只读」一起删掉了 —— 诊室来源是排班，这里不再需要

// 筛选条件（全部下推后端，前端不做任何 filter）
const visitDate = ref(localDate())
const deptId = ref<string>('')
const doctorId = ref<string>('')
const keyword = ref('')
const statusFilter = ref<number[]>([])
const unTriageOnly = ref(false)
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const stats = ref<any>({})
const consulting = ref<any[]>([])
const revisitTimeout = ref<any[]>([])
// 批次F：这两个列表原来没有加载标记，「暂无就诊中患者」在请求回来前就敢说 —— 护士会以为真没人。
// 初值给 true：onMounted 里先 await 科室/医生/诊室，这几个请求还没发出去，界面也不能先说"没有"。
const consultingLoading = ref(true)
const revisitLoading = ref(true)

function localDate(): string {
  // 不能用 toISOString().slice(0,10)：UTC+8 上午 8 点前会取到昨天
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const showUnknown = (code: any) => code === null || code === undefined

const genderText = (g: any) => (g === 1 ? '男' : g === 2 ? '女' : '未知')

/**
 * 时间解析（全站唯一入口）。
 *
 * ⚠ 不要写 `new Date(str.replace(/-/g,'/'))` —— 那是给老 Safari 兼容 `yyyy-MM-dd` 的老写法，
 * 一旦后端给的是 ISO（`2026-09-20T16:34:25`），替换后就变成 `2026/09/20T16:34:25`，
 * 分隔符 `/` 与 `T` 混用 → Invalid Date → 页面上出现「NaN时NaN分」。
 * 正确做法是只在「空格分隔」时换成 `T`（ISO 允许），两种格式都能解析。
 */
const parseTime = (v: any): Date | null => {
  if (!v) return null
  const d = new Date(String(v).trim().replace(' ', 'T'))
  return isNaN(d.getTime()) ? null : d
}

/** 等候时长按时间戳现算，不读 wait_duration（那是结诊时才回填的字段） */
const waitText = (row: any) => {
  const start = parseTime(row.arriveTime)
  if (!start) return '-'
  const end = parseTime(row.endTime) || new Date()
  const minutes = Math.max(0, Math.floor((end.getTime() - start.getTime()) / 60000))
  if (minutes < 60) return `${minutes}分`
  return `${Math.floor(minutes / 60)}时${minutes % 60}分`
}

// ========== 列表 ==========

const buildParams = () => {
  const params: any = {
    pageNum: pagination.value.pageNum,
    pageSize: pagination.value.pageSize,
    visitDate: visitDate.value || undefined,
    deptId: deptId.value || undefined,
    doctorId: doctorId.value || undefined,
    keyword: keyword.value.trim() || undefined,
  }
  if (statusFilter.value.length > 0) params.queueStatuses = statusFilter.value.join(',')
  if (unTriageOnly.value) params.unTriageOnly = true
  return params
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getQueueListPage(buildParams())
    rows.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error: any) {
    console.error('加载队列失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getQueueStats({deptId: deptId.value || undefined, visitDate: visitDate.value || undefined})
    stats.value = res.data || {}
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const loadConsulting = async () => {
  consultingLoading.value = true
  try {
    const res = await getConsultingPatients()
    consulting.value = res.data || []
  } catch (error) {
    console.error('加载就诊中患者失败:', error)
    consulting.value = []
  } finally {
    consultingLoading.value = false
  }
}

const loadRevisitTimeout = async () => {
  revisitLoading.value = true
  try {
    const res = await getRevisitTimeoutPatients()
    revisitTimeout.value = res.data || []
  } catch (error) {
    console.error('加载复诊超时失败:', error)
    revisitTimeout.value = []
  } finally {
    revisitLoading.value = false
  }
}

const reload = async () => {
  await Promise.all([loadList(), loadStats(), loadConsulting(), loadRevisitTimeout()])
}

const onDeptChange = async () => {
  pagination.value.pageNum = 1
  doctorId.value = ''
  selected.value = null
  await loadDoctors()
  await reload()
}

const loadDoctors = async () => {
  try {
    const params: any = {empType: 1, pageSize: 200}
    if (deptId.value) params.deptId = deptId.value
    const res = await getEmployeeList(params)
    doctors.value = res.data?.records || []
  } catch (error) {
    console.error('加载医生失败:', error)
  }
}

const loadDepts = async () => {
  try {
    // 不传 scope → 默认按当前人过滤（分诊护士只看到自己科室的诊区）
    const res = await getDepartmentSelectList({})
    depts.value = res.data || []
    if (deptId.value) return

    // 默认诊区 = 登录人所属科室。**别指望 localStorage.deptId** ——
    // 登录页只写了 token/userId/realName/currentRole，deptId 要等切换科室才写，
    // 所以直接问后端要（/auth/info 的 UserLoginVO.deptId）。
    let own = localStorage.getItem('deptId')
    if (!own) {
      try {
        const info = await getUserInfo()
        own = info.data?.deptId ? String(info.data.deptId) : null
      } catch (error) {
        own = null
      }
    }
    // 拿不到登录人科室就留空，由护士自己选。原先「挑第一个科室」的做法会让页面
    // 显示成「当前诊区暂无排队记录」—— 看着像今天没人看病，比留空更有害。
    const hit = own ? depts.value.find((d: any) => String(d.id) === String(own)) : null
    deptId.value = hit ? String(hit.id) : ''
  } catch (error) {
    console.error('加载科室失败:', error)
  }
}

const handleReset = async () => {
  pagination.value.pageNum = 1
  doctorId.value = ''
  keyword.value = ''
  statusFilter.value = []
  unTriageOnly.value = false
  visitDate.value = localDate()
  await reload()
}

const toggleStatus = (code: number) => {
  const idx = statusFilter.value.indexOf(code)
  if (idx >= 0) statusFilter.value.splice(idx, 1)
  else statusFilter.value.push(code)
  unTriageOnly.value = false
  pagination.value.pageNum = 1
  loadList()
}

const filterUnTriage = () => {
  statusFilter.value = []
  unTriageOnly.value = true
  pagination.value.pageNum = 1
  loadList()
}

// ========== 行操作 ==========

const handleCall = async (row: any) => {
  try {
    await callPatient(row.id)
    ElMessage.success(`已呼叫 ${row.patientName}`)
    await reload()
  } catch (error: any) {
    ElMessage.error(error.message || '呼叫失败')
  }
}

const handleRecall = async (row: any) => {
  try {
    await recallPatient(row.id)
    ElMessage.success(`已重呼 ${row.patientName}`)
    await reload()
  } catch (error: any) {
    ElMessage.error(error.message || '重呼失败')
  }
}

const handleOverdue = async (row: any) => {
  try {
    const {value} = await ElMessageBox.prompt(
        `将 ${row.patientName}（${row.queueNo}）标记为过号，请填写原因`,
        '过号确认',
        {confirmButtonText: '确定过号', cancelButtonText: '取消', inputPlaceholder: '如：呼叫三次未到'}
    )
    if (!value || !value.trim()) {
      ElMessage.warning('过号必须填写原因')
      return
    }
    await overdueQueue(row.id, value.trim())
    ElMessage.success('已标记过号')
    await reload()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '操作失败')
  }
}

/**
 * 重新入队（候诊中 = 复诊优先；已过号 = 过号回队）。
 *
 * 过号不是终态：患者叫号时不在、过一会儿又回来了，必须有回队的路 ——
 * 原先只有候诊中能插队，过号(6)的行在页面上连按钮都没有，患者只能干等或重挂号。
 */
const handleRejoin = async (row: any) => {
  const overdue = row.queueStatus === 6
  try {
    await ElMessageBox.confirm(
        overdue
            ? `将 ${row.patientName}（${row.queueNo}）重新排回候诊队列？会排到当前队首`
            : `将 ${row.patientName}（${row.queueNo}）置为优先就诊？`,
        overdue ? '过号回队' : '复诊优先',
        {confirmButtonText: overdue ? '确认回队' : '确认优先', cancelButtonText: '取消', type: 'warning'},
    )
    const res = await rejoinQueue(row.id)
    // 序号必须原样告诉护士 —— 只说「已优先」没法向患者交代。
    // 文案由前端按场景拼（后端那句中性的「已入队」在这里读起来会像没生效）
    const action = overdue ? '已重新入队' : '已置为优先'
    ElMessage.success(res.data != null ? `${action}，当前序号 ${res.data}` : (res.message || action))
    await reload()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '操作失败')
  }
}

// ========== 分诊卡 ==========

const selected = ref<any>(null)
const triageHistory = ref<any[]>([])
const saving = ref(false)
// 注意这里**没有 roomId**：诊室不由分诊台决定（原来有，会在保存时覆盖挂号时定下的诊室）。
// 诊室唯一来源是排班，卡片上只读展示 selected.roomName。
const form = ref<any>({
  temperature: null, pulse: null, respiration: null,
  systolicBp: null, diastolicBp: null, spo2: null,
  height: null, weight: null, painScore: null,
  chiefComplaint: '', triageLevel: 4, remark: '',
})

/** 排队号前缀就是诊室编号（generateQueueNo 用排班诊室的 code 做前缀），用来对号码认诊室 */
const queueNoPrefix = (queueNo: any) => (queueNo ? String(queueNo).split('-')[0] : '-')

const onRowChange = async (row: any) => {
  if (!row) return
  await openTriage(row)
}

const openTriage = async (row: any) => {
  selected.value = row
  form.value = {
    temperature: null, pulse: null, respiration: null,
    systolicBp: null, diastolicBp: null, spo2: null,
    height: null, weight: null, painScore: null,
    chiefComplaint: '', triageLevel: 4, remark: '',
  }
  triageHistory.value = []
  try {
    const res = await getTriageByQueueId(row.id)
    const latest = res.data?.latest
    triageHistory.value = res.data?.history || []
    if (latest) {
      // 已分诊过：把上次的值填回来，护士只改变化项
      form.value = {
        temperature: latest.temperature ?? null,
        pulse: latest.pulse ?? null,
        respiration: latest.respiration ?? null,
        systolicBp: latest.systolicBp ?? null,
        diastolicBp: latest.diastolicBp ?? null,
        spo2: latest.spo2 ?? null,
        height: latest.height ?? null,
        weight: latest.weight ?? null,
        painScore: latest.painScore ?? null,
        chiefComplaint: latest.chiefComplaint || '',
        triageLevel: latest.triageLevel ?? 4,
        remark: '',
      }
    }
  } catch (error) {
    console.error('加载分诊记录失败:', error)
  }
}

const clearForm = () => {
  form.value = {
    temperature: null, pulse: null, respiration: null,
    systolicBp: null, diastolicBp: null, spo2: null,
    height: null, weight: null, painScore: null,
    chiefComplaint: '', triageLevel: 4, remark: '',
  }
}

/** 仅用于即时展示；落库由后端算（两边算法必须一致，前端只是不让护士瞪着空格等） */
const bmiPreview = computed(() => {
  const h = Number(form.value.height)
  const w = Number(form.value.weight)
  if (!h || !w || h <= 0) return null
  const m = h / 100
  return (w / (m * m)).toFixed(1)
})

const handleSaveTriage = async () => {
  if (!selected.value) {
    ElMessage.warning('请先在左侧队列中选择患者')
    return
  }
  if (!form.value.triageLevel) {
    ElMessage.warning('分诊等级不能为空')
    return
  }
  saving.value = true
  try {
    // 不带诊室：诊室由排班决定，后端也不收这个字段（传了也没用，接口已删）
    const payload: any = {
      queueId: selected.value.id,
      triageLevel: form.value.triageLevel,
      chiefComplaint: form.value.chiefComplaint || undefined,
      remark: form.value.remark || undefined,
    }
    for (const k of ['temperature', 'pulse', 'respiration', 'systolicBp', 'diastolicBp', 'spo2', 'height', 'weight', 'painScore']) {
      if (form.value[k] !== null && form.value[k] !== '' && form.value[k] !== undefined) {
        payload[k] = form.value[k]
      }
    }
    const res = await saveTriage(payload)
    ElMessage.success(res.message || '分诊已保存')
    await openTriage(selected.value)
    await Promise.all([loadList(), loadStats()])
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ========== 待签到（已缴费未签到） ==========

const checkInDrawer = ref(false)
const uncheckedList = ref<any[]>([])
const uncheckedLoading = ref(false)
const checkedRows = ref<any[]>([])

const openCheckInDrawer = async () => {
  checkInDrawer.value = true
  uncheckedLoading.value = true
  checkedRows.value = []
  try {
    const res = await getUncheckedList({
      deptId: deptId.value || undefined,
      visitDate: visitDate.value || undefined,
    })
    uncheckedList.value = res.data || []
  } catch (error) {
    console.error('加载待签到列表失败:', error)
  } finally {
    uncheckedLoading.value = false
  }
}

const doCheckIn = async (row: any) => {
  try {
    await checkInByRegist(row.registId)
    ElMessage.success(`${row.patientName} 已签到`)
    await openCheckInDrawer()
    await reload()
  } catch (error: any) {
    ElMessage.error(error.message || '签到失败')
  }
}

const doBatchCheckIn = async () => {
  if (checkedRows.value.length === 0) {
    ElMessage.warning('请先选择要签到的患者')
    return
  }
  let okCount = 0
  const failed: string[] = []
  for (const row of checkedRows.value) {
    try {
      await checkInByRegist(row.registId)
      okCount++
    } catch (error: any) {
      failed.push(`${row.patientName}：${error.message || '签到失败'}`)
    }
  }
  if (okCount > 0) ElMessage.success(`已签到 ${okCount} 人`)
  // 失败原因逐条说出来 —— 只说「部分失败」护士不知道该找谁
  if (failed.length > 0) ElMessage.warning(failed.join('；'))
  await openCheckInDrawer()
  await reload()
}

// ========== 定时刷新 ==========

let timer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  await loadDepts()
  await loadDoctors()
  await reload()
  // 5 秒刷统计与就诊中（护士站要挂在墙上），列表不自动刷，避免正在点按钮时行被刷新掉
  timer = setInterval(() => {
    loadStats()
    loadConsulting()
    loadRevisitTimeout()
  }, 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="flex h-[var(--his-page-h)] flex-col gap-3">
    <!-- ============ 工具栏 ============ -->
    <div class="flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 shadow-sm">
      <el-date-picker
          v-model="visitDate"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          style="width: 148px"
          @change="pagination.pageNum = 1; reload()"
      />
      <el-select v-model="deptId" placeholder="诊区" filterable style="width: 150px" @change="onDeptChange">
        <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
      </el-select>
      <el-select v-model="doctorId" placeholder="医生" clearable filterable style="width: 140px"
                 @change="pagination.pageNum = 1; loadList()">
        <el-option v-for="e in doctors" :key="e.id" :label="e.empName" :value="String(e.id)"/>
      </el-select>
      <el-input v-model="keyword" placeholder="姓名/患者号/就诊号/排队号" clearable style="width: 220px"
                @keyup.enter="pagination.pageNum = 1; loadList()">
        <template #prefix>
          <el-icon>
            <Search/>
          </el-icon>
        </template>
      </el-input>
      <el-button type="primary" :icon="Search" @click="pagination.pageNum = 1; loadList()">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>

      <div class="ml-auto flex flex-wrap items-center gap-1.5">
        <!-- 危重待接诊摆在最前：这是队列里唯一还有硬约束的场景，护士扫一眼就得看见 -->
        <el-tag v-if="(stats.criticalWaiting ?? 0) > 0" type="danger" effect="dark" size="small">
          危重待接诊 {{ stats.criticalWaiting }}
        </el-tag>
        <el-tag v-else type="success" effect="plain" size="small">无危重候诊</el-tag>
        <el-button size="small" :type="statusFilter.includes(2) ? 'primary' : 'default'" @click="toggleStatus(2)">
          候诊 {{ stats.waiting ?? 0 }}
        </el-button>
        <el-button size="small" :type="statusFilter.includes(3) ? 'primary' : 'default'" @click="toggleStatus(3)">
          就诊中 {{ stats.consulting ?? 0 }}
        </el-button>
        <el-button size="small" :type="statusFilter.includes(6) ? 'primary' : 'default'" @click="toggleStatus(6)">
          过号 {{ stats.overdue ?? 0 }}
        </el-button>
        <el-button size="small" type="warning" plain @click="filterUnTriage">
          未核验分级 {{ stats.unTriage ?? 0 }}
        </el-button>
        <el-button size="small" type="danger" plain>超时 {{ stats.timeout ?? 0 }}</el-button>
        <el-button size="small" :icon="Bell" @click="openCheckInDrawer">
          待签到 {{ stats.unchecked ?? 0 }}
        </el-button>
      </div>
    </div>

    <div class="flex min-h-0 flex-1 gap-3">
      <!-- ============ 主表格 ============ -->
      <div class="flex min-w-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
        <el-table
            :data="rows"
            v-loading="loading"
            height="100%"
            highlight-current-row
            row-key="id"
            @current-change="onRowChange"
        >
          <el-table-column type="index" label="序" width="52" align="center"/>
          <el-table-column label="排队号" width="92">
            <template #default="{ row }">
              <span class="rounded bg-blue-50 px-2 py-0.5 text-xs font-bold text-blue-700">{{ shortQueueNo(row.queueNo) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="患者" min-width="150">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
              <div class="text-xs text-slate-400">
                {{ genderText(row.gender) }}/{{ row.age ?? '-' }} · {{ row.patientNo }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="deptName" label="科室" width="100" show-overflow-tooltip/>
          <el-table-column prop="doctorName" label="医生" width="88" show-overflow-tooltip/>
          <el-table-column label="号别" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="statusOf(REGIST_TYPE, row.registType).tagType" effect="plain" size="small">
                {{ statusOf(REGIST_TYPE, row.registType).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="等级" width="112" align="center">
            <template #default="{ row }">
              <!-- 未核验（triage_status=0）**也有**等级：签到入队时后端就写了 4 级。
                   所以这里不能再显示「待分诊」—— 那会让护士以为这个人接不了诊。 -->
              <el-tag v-if="row.triageStatus === 1" :type="statusOf(TRIAGE_LEVEL, row.triageLevel).tagType"
                      effect="plain" size="small">
                {{ statusOf(TRIAGE_LEVEL, row.triageLevel).label }}
              </el-tag>
              <el-tooltip v-else placement="top"
                          :content="'分诊护士尚未核验，现按默认等级排队，不影响接诊'">
                <el-tag type="info" effect="plain" size="small">
                  {{ row.triageLevelText || '未分级' }} · 未核验
                </el-tag>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="诊室" width="86" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="row.roomName ? 'text-slate-700' : 'text-slate-300'">{{ row.roomName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="等候" width="86" align="right">
            <template #default="{ row }">
              <span :class="row.queueStatus === 2 && waitText(row).includes('时') ? 'text-xs font-medium text-red-500' : 'text-xs text-slate-500'">
                {{ waitText(row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="86" align="center">
            <template #default="{ row }">
              <el-tag :type="statusOf(QUEUE_STATUS, row.queueStatus).tagType" effect="plain" size="small">
                {{ statusOf(QUEUE_STATUS, row.queueStatus).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="196" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.queueStatus === 2" type="primary" link @click.stop="handleCall(row)">呼叫</el-button>
              <el-button v-if="row.queueStatus === 3" type="success" link @click.stop="handleRecall(row)">重呼</el-button>
              <el-button v-if="row.queueStatus === 2 || row.queueStatus === 3" type="warning" link
                         @click.stop="handleOverdue(row)">过号</el-button>
              <el-button v-if="row.queueStatus === 2" type="danger" link @click.stop="handleRejoin(row)">复诊优先</el-button>
              <!-- 过号回队：过号患者人又来了的唯一出口（原先这里只显示「已结束」） -->
              <el-button v-if="row.queueStatus === 6" type="danger" link @click.stop="handleRejoin(row)">重新入队</el-button>
              <span v-if="row.queueStatus !== 2 && row.queueStatus !== 3 && row.queueStatus !== 6"
                    class="text-xs text-slate-300">已结束</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-8 text-sm text-slate-400">
              {{ !deptId ? '请先在上方选择诊区' : (unTriageOnly ? '所有分级都已核验' : '当前诊区暂无排队记录') }}
            </div>
          </template>
        </el-table>
        <div class="flex items-center justify-between border-t border-slate-100 px-4 py-2">
          <span class="text-xs text-slate-400">
            共 {{ pagination.total }} 条 · 点行右侧填分诊卡
          </span>
          <el-pagination
              :current-page="pagination.pageNum"
              :page-size="pagination.pageSize"
              :total="pagination.total"
              layout="sizes, prev, pager, next"
              :page-sizes="PAGE_SIZES"
              background
              @current-change="(p: number) => { pagination.pageNum = p; loadList() }"
              @size-change="(s: number) => { pagination.pageSize = s; pagination.pageNum = 1; loadList() }"
          />
        </div>
      </div>

      <!-- ============ 右栏：分诊卡 / 上下文 ============ -->
      <div class="flex w-[var(--his-order-col-w)] flex-shrink-0 flex-col gap-3 overflow-y-auto">
        <!-- 选中行 → 分诊卡 -->
        <div v-if="selected" class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
            <UserFilled class="h-4 w-4 text-blue-600"/>
            <span class="text-sm font-medium text-slate-700">分诊 · {{ selected.patientName }}</span>
            <el-button class="ml-auto" size="small" link @click="selected = null">收起</el-button>
          </div>
          <div class="space-y-2.5 p-3">
            <div class="flex items-center gap-2 text-xs text-slate-500">
              <span class="rounded bg-blue-50 px-1.5 py-0.5 font-bold text-blue-700">{{ shortQueueNo(selected.queueNo) }}</span>
              <span>{{ genderText(selected.gender) }}/{{ selected.age ?? '-' }}</span>
              <span>{{ selected.doctorName || '未指定医生' }}</span>
              <span v-if="triageHistory.length > 0" class="ml-auto text-slate-400">
                已分诊 {{ triageHistory.length }} 次
              </span>
            </div>

            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="mb-1 block text-xs text-slate-500">体温(℃)</label>
                <el-input-number v-model="form.temperature" :min="30" :max="45" :step="0.1" :precision="1"
                                 controls-position="right" size="small" class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">脉搏(次/分)</label>
                <el-input-number v-model="form.pulse" :min="0" :max="300" controls-position="right" size="small"
                                 class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">呼吸(次/分)</label>
                <el-input-number v-model="form.respiration" :min="0" :max="100" controls-position="right"
                                 size="small" class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">血氧 SpO₂(%)</label>
                <el-input-number v-model="form.spo2" :min="0" :max="100" controls-position="right" size="small"
                                 class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">收缩压(mmHg)</label>
                <el-input-number v-model="form.systolicBp" :min="0" :max="400" controls-position="right" size="small"
                                 class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">舒张压(mmHg)</label>
                <el-input-number v-model="form.diastolicBp" :min="0" :max="300" controls-position="right" size="small"
                                 class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">身高(cm)</label>
                <el-input-number v-model="form.height" :min="0" :max="300" :step="0.1" controls-position="right"
                                 size="small" class="w-full"/>
              </div>
              <div>
                <label class="mb-1 block text-xs text-slate-500">体重(kg)</label>
                <el-input-number v-model="form.weight" :min="0" :max="500" :step="0.1" controls-position="right"
                                 size="small" class="w-full"/>
              </div>
            </div>

            <div class="flex items-center justify-between rounded bg-slate-50 px-2 py-1.5 text-xs">
              <span class="text-slate-500">BMI</span>
              <span class="font-semibold text-slate-700">{{ bmiPreview ?? '填身高体重后自动计算' }}</span>
            </div>

            <div>
              <label class="mb-1 block text-xs text-slate-500">疼痛评分（0~10）</label>
              <el-input-number v-model="form.painScore" :min="0" :max="10" controls-position="right" size="small"
                               class="w-full"/>
            </div>

            <div>
              <label class="mb-1 block text-xs text-slate-500">
                分诊等级
                <span class="ml-1 text-slate-400">保存即完成核验；改为 1/2 级后立即参与叫号插队</span>
              </label>
              <el-radio-group v-model="form.triageLevel" size="small">
                <el-radio-button v-for="lv in [1, 2, 3, 4]" :key="lv" :value="lv">
                  {{ statusOf(TRIAGE_LEVEL, lv).label }}
                </el-radio-button>
              </el-radio-group>
            </div>

            <!-- 诊室只读：来源是**排班**（医生-诊室绑定在排班上，排队号前缀也取自排班诊室）。
                 原先这里是个可自由选择的下拉框 —— 护士选一下就能把挂号时定下的诊室改走，
                 患者按号（诊室编号前缀）走却被告知去另一个房间，直接引发客诉。
                 要换诊室去「排班管理」改该医生本班次：一处生效，该班次的人一起搬。 -->
            <div>
              <div class="mb-1 flex items-center justify-between text-xs text-slate-500">
                <span>诊室</span>
                <span class="text-slate-400">由排班决定 · 此处不可改</span>
              </div>
              <div class="flex items-center justify-between rounded bg-slate-50 px-2 py-1.5 text-xs">
                <span :class="selected.roomName ? 'font-semibold text-slate-700' : 'font-semibold text-amber-600'">
                  {{ selected.roomName || '排班未分配诊室' }}
                </span>
                <span class="text-slate-400">号前缀 {{ queueNoPrefix(selected.queueNo) }}</span>
              </div>
              <div v-if="!selected.roomName" class="mt-1 text-xs text-amber-600">
                请到「排班管理」给 {{ selected.doctorName || '该医生' }} 本班次设置诊室 —— 患者按号找不到房间
              </div>
            </div>

            <div>
              <label class="mb-1 block text-xs text-slate-500">主诉</label>
              <el-input v-model="form.chiefComplaint" type="textarea" :rows="2" maxlength="500" show-word-limit
                        placeholder="患者自述的主要症状"/>
            </div>

            <div class="flex gap-2">
              <el-button v-perm="['opd:triage:add', 'opd:triage:edit']" type="primary" class="flex-1" :loading="saving" @click="handleSaveTriage">保存分诊</el-button>
              <el-button @click="clearForm">清空</el-button>
            </div>

            <div v-if="triageHistory.length > 1" class="rounded bg-slate-50 p-2">
              <div class="mb-1 text-xs font-medium text-slate-500">分诊留痕</div>
              <div v-for="(h, i) in triageHistory" :key="h.id" class="text-xs text-slate-500">
                {{ i === 0 ? '当前' : '历史' }}：{{ statusOf(TRIAGE_LEVEL, h.triageLevel).label }}
                · {{ h.triageTime }} · {{ h.triageNurseName || '未记录护士' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 未选中 → 上下文提示 -->
        <template v-else>
          <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
              <Clock class="h-4 w-4 text-emerald-600"/>
              <span class="text-sm font-medium text-slate-700">当前就诊中</span>
            </div>
            <div class="divide-y divide-slate-100">
              <template v-for="room in consulting" :key="'c-' + room.doctorId">
                <div v-for="p in (room.consultingPatients || [])" :key="p.id" class="flex items-center gap-2 px-3 py-2">
                  <span
                      class="inline-flex h-6 w-6 items-center justify-center rounded-full bg-purple-100 text-xs font-bold text-purple-700">
                    {{ p.patientName?.charAt(0) }}
                  </span>
                  <div class="min-w-0">
                    <div class="truncate text-sm font-medium text-slate-800">{{ p.patientName }}</div>
                    <div class="truncate text-xs text-slate-400">
                      {{ shortQueueNo(p.queueNo) }} · {{ room.roomName || '未分配诊室' }} · {{ room.doctorName }}
                    </div>
                  </div>
                </div>
              </template>
              <div v-if="consultingLoading && consulting.every((r: any) => !r.consultingPatients || r.consultingPatients.length === 0)"
                   class="py-5 text-center text-xs text-slate-400">
                就诊中患者加载中…
              </div>
              <div v-else-if="consulting.every((r: any) => !r.consultingPatients || r.consultingPatients.length === 0)"
                   class="py-5 text-center text-xs text-slate-400">
                暂无就诊中患者
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-amber-200 bg-amber-50 p-3 shadow-sm">
            <div class="flex items-center gap-2">
              <WarningFilled class="h-4 w-4 text-amber-600"/>
              <span class="text-sm font-medium text-amber-700">待办</span>
            </div>
            <div class="mt-2 space-y-1.5 text-xs text-amber-700">
              <div class="flex items-center justify-between">
                <span>危重待接诊（1/2 级，须优先处置）</span>
                <span class="font-semibold"
                      :class="(stats.criticalWaiting ?? 0) > 0 ? 'text-red-600' : ''">
                  {{ stats.criticalWaiting ?? 0 }} 人
                </span>
              </div>
              <div class="flex items-center justify-between">
                <span>分级未核验（已按默认等级入队，不影响接诊）</span>
                <el-button link size="small" type="warning" @click="filterUnTriage">
                  {{ stats.unTriage ?? 0 }} 人 · 查看
                </el-button>
              </div>
              <div class="flex items-center justify-between">
                <span>候诊超时（&gt;30 分钟）</span>
                <span class="font-semibold">{{ stats.timeout ?? 0 }} 人</span>
              </div>
              <div class="flex items-center justify-between">
                <span>已缴费未签到</span>
                <el-button link size="small" type="warning" @click="openCheckInDrawer">
                  {{ stats.unchecked ?? 0 }} 人 · 签到
                </el-button>
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
              <WarningFilled class="h-4 w-4 text-red-500"/>
              <span class="text-sm font-medium text-slate-700">复诊等候超时</span>
              <span v-if="revisitTimeout.length > 0"
                    class="ml-auto rounded-full bg-red-500 px-1.5 py-0.5 text-xs font-bold text-white">
                {{ revisitTimeout.length }}
              </span>
            </div>
            <div class="divide-y divide-slate-100">
              <div v-for="q in revisitTimeout" :key="'rt-' + q.id" class="flex items-center justify-between px-3 py-2">
                <div class="min-w-0">
                  <div class="truncate text-sm font-medium text-slate-800">
                    {{ shortQueueNo(q.queueNo) }} · {{ q.patientName }}
                  </div>
                  <div class="text-xs text-slate-400">等候 {{ waitText(q) }} · {{ q.deptName }}</div>
                </div>
                <el-button type="danger" link size="small" @click="handleRejoin(q)">复诊优先</el-button>
              </div>
              <div v-if="revisitTimeout.length === 0 && revisitLoading" class="py-5 text-center text-xs text-slate-400">
                超时复诊加载中…
              </div>
              <div v-else-if="revisitTimeout.length === 0" class="py-5 text-center text-xs text-slate-400">
                暂无超时复诊患者
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-blue-100 bg-blue-50 p-3 shadow-sm">
            <div class="mb-1 text-xs font-medium text-blue-700">操作说明</div>
            <ul class="space-y-1 text-xs text-blue-600">
              <li>· 点击左侧某一行，右栏会出现该患者的分诊卡</li>
              <li>· 患者签到即按「4级·非急」入队，<b>未核验也能被叫号接诊</b>；分诊台做的是把危重提上来</li>
              <li>· 保存分诊即完成核验，等级按「等级 → 排队号」参与叫号，1 级危重可插队</li>
              <li>· 队列里还有 1/2 级未接诊时，医生无法跳过他们去叫普通患者</li>
              <li>· 诊室<b>只读</b>：由排班决定（排队号前缀就是诊室编号）。要换诊室去「排班管理」改该医生本班次，该班次患者一起换</li>
              <li>· 叫了没到的患者点「过号」；过号后本人回来点「重新入队」，会排回队首</li>
              <li>· 签到只受理<b>就诊日 = 今天</b>的号；隔日号请先办理改约</li>
              <li>· 缴费请引导患者到收费处，护士站不受理收费</li>
            </ul>
          </div>
        </template>
      </div>
    </div>

    <!-- ============ 待签到抽屉 ============ -->
    <el-drawer v-model="checkInDrawer" title="待签到（已缴费未签到）" size="640px">
      <div class="mb-3 flex items-center gap-2">
        <el-button type="primary" size="small" :icon="Check"
                   :disabled="checkedRows.length === 0" @click="doBatchCheckIn">
          批量签到（已选 {{ checkedRows.length }}）
        </el-button>
        <span class="text-xs text-slate-400">与工具栏「待签到」统计同一口径：本科室 + 就诊日期 + 收费单已支付</span>
      </div>
      <el-table :data="uncheckedList" v-loading="uncheckedLoading" @selection-change="(v: any[]) => checkedRows = v">
        <el-table-column type="selection" width="48"/>
        <el-table-column label="就诊号" width="150" prop="registNo"/>
        <el-table-column label="患者" min-width="110">
          <template #default="{ row }">
            <div class="text-sm text-slate-800">{{ row.patientName }}</div>
            <div class="text-xs text-slate-400">{{ genderText(row.gender) }}/{{ row.age ?? '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" width="90"/>
        <el-table-column label="号别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusOf(REGIST_TYPE, row.registType).tagType" effect="plain" size="small">
              {{ statusOf(REGIST_TYPE, row.registType).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="doCheckIn(row)">签到</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">没有已缴费待签到的患者</div>
        </template>
      </el-table>
    </el-drawer>
  </div>
</template>
