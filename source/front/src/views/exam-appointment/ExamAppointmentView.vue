<script setup lang="ts">
/**
 * 检查预约中心（G21，菜单 412）
 *
 * 两个 tab：
 *  A 预约工作台 —— 待预约申请（后端派生：已缴费/急诊提交 + 无在办预约）→ 选设备 → 点格子占号；
 *  B 预约台账  —— 全量预约单 + 状态分布 + 到检/完成/改约/取消。
 *
 * ⚠ 设备档位与号源（原第 3 个页签）不属于预约中心业务，2026-09 已拆为独立二级菜单
 *   「检查设备与号源」（400 医技医辅 / 2925，sql/181）。本页只在占号时自行补格子看板。
 *
 * ⚠ 页面不算库存、不判冲突：号源计数与四道冲突检测（设备/患者/时长/流程）全在服务端，
 *   冲突时后端返回的错误信息会点名是哪张单、哪位患者占了哪一格，直接透传给用户。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getExamDeviceSelectList,
  examSlotGenerate, getExamSlotBoard,
  getExamPendingListPage, getExamAppointListPage, getExamAppointStatusCount, getExamAppointStats,
  getExamAppointDetail, examAppointBook, examAppointReschedule, examAppointCancel,
  examAppointArrive, examAppointFinish, examAppointRecommend, examAppointAutoNoShow,
} from '@/api/examAppointment'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import {
  APPT_STATUS, DEVICE_STATUS, apptStatusTag, slotCellState, isMutable,
} from '@/lib/examAppointment'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const activeTab = ref('workbench')

const today = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})()

// ---------------- 字典 ----------------
const apptStatusDict = ref<any[]>([])

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.EXAM_APPT_STATUS}`)
    apptStatusDict.value = res?.data?.[DICT_TYPE.EXAM_APPT_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 统计 ----------------
const stats = reactive({
  todayBooked: 0, todayArrived: 0, todayFinished: 0, todayCancelled: 0, todayNoShow: 0,
  pendingApplies: 0, activeTotal: 0, deviceOpen: 0, devicePaused: 0,
})
const loadStats = async () => {
  try {
    const res: any = await getExamAppointStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) { console.error(e) }
}

const fmtTime = (t: any) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

// ================= A 预约工作台 =================
const pendLoading = ref(false)
const pendRows = ref<any[]>([])
const pendTotal = ref(0)
const pendQuery = reactive({ keyword: '', isEmergency: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })

const loadPending = async () => {
  pendLoading.value = true
  try {
    const res: any = await getExamPendingListPage({
      keyword: pendQuery.keyword.trim() || undefined,
      isEmergency: pendQuery.isEmergency ?? undefined,
      pageNum: pendQuery.pageNum, pageSize: pendQuery.pageSize,
    })
    if (res.code === 200) {
      pendRows.value = res.data?.records || []
      pendTotal.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { pendLoading.value = false }
}

// ---------------- 占号弹窗（预约 / 改约共用） ----------------
const apptDlg = reactive({
  visible: false,
  mode: 'book' as 'book' | 'reschedule',
  target: null as any,        // ApplyVO 或 ApptVO
  devices: [] as any[],
  form: { deviceId: null as any, examDate: '', startTime: '', remark: '', reason: '' },
  board: null as any,
  boardLoading: false,
  submitting: false,
  recLoading: false,
  rec: null as any,
})

const loadBookDevices = async (itemId: any) => {
  try {
    const res: any = await getExamDeviceSelectList({ itemId: itemId ?? undefined })
    if (res.code === 200) {
      apptDlg.devices = res.data || []
      if (!apptDlg.devices.length) ElMessage.warning('该项目还没有配置可开展设备，请到「设备与号源」页配置')
    } else ElMessage.error(res.message || '加载设备失败')
  } catch (e) { console.error(e); ElMessage.error('加载设备失败') }
}

const openBook = async (row: any) => {
  if (!Number(row.deviceCount || 0)) {
    ElMessage.warning(`项目「${row.itemName}」没有可承接的设备，请先到「设备与号源」配置`)
    return
  }
  apptDlg.mode = 'book'
  apptDlg.target = row
  Object.assign(apptDlg.form, { deviceId: null, examDate: today, startTime: '', remark: '', reason: '' })
  apptDlg.board = null; apptDlg.rec = null
  apptDlg.visible = true
  await loadBookDevices(row.itemId)
  if (apptDlg.devices.length === 1) { apptDlg.form.deviceId = apptDlg.devices[0].id; await loadBoard() }
}

const openReschedule = async (row: any) => {
  apptDlg.mode = 'reschedule'
  apptDlg.target = row
  Object.assign(apptDlg.form, { deviceId: row.deviceId, examDate: row.examDate, startTime: row.startTime, remark: '', reason: '' })
  apptDlg.board = null; apptDlg.rec = null
  apptDlg.visible = true
  await loadBookDevices(row.itemId)
  if (!apptDlg.devices.some((d) => String(d.id) === String(row.deviceId))) {
    apptDlg.devices = [{ id: row.deviceId, deviceCode: row.deviceCode, deviceName: row.deviceName, status: DEVICE_STATUS.OPEN }, ...apptDlg.devices]
  }
  await loadBoard()
}

const boardCells = computed<any[]>(() => apptDlg.board?.slots || [])

const loadBoard = async () => {
  if (!apptDlg.form.deviceId || !apptDlg.form.examDate) { apptDlg.board = null; return }
  apptDlg.boardLoading = true
  apptDlg.form.startTime = ''
  try {
    // 先补格子再看板：设备当天可能还没生成号源（ahead_days 内允许直接生成）
    await examSlotGenerate({ deviceId: apptDlg.form.deviceId, startDate: apptDlg.form.examDate, days: 1 })
    const res: any = await getExamSlotBoard({ deviceId: apptDlg.form.deviceId, slotDate: apptDlg.form.examDate })
    if (res.code === 200) apptDlg.board = res.data
    else ElMessage.error(res.message || '加载号源失败')
  } catch (e) { console.error(e); ElMessage.error('加载号源失败') } finally { apptDlg.boardLoading = false }
}

const pickCell = (cell: any) => {
  const st = slotCellState(cell)
  if (!st.clickable) return
  apptDlg.form.startTime = cell.startTime
}

const cellClass = (cell: any) => {
  const st = slotCellState(cell)
  if (st.key === 'free') return apptDlg.form.startTime === cell.startTime ? 'cell cell-free cell-on' : 'cell cell-free'
  if (st.key === 'full') return 'cell cell-full'
  if (st.key === 'locked') return 'cell cell-locked'
  return 'cell cell-past'
}

const runRecommend = async () => {
  if (!apptDlg.form.deviceId && !apptDlg.target?.itemId) { ElMessage.warning('请先选设备'); return }
  apptDlg.recLoading = true
  try {
    // 改约场景同样传 applyId：预约单快照里带着它，服务端按申请单项目找可承接设备
    const res: any = await examAppointRecommend({
      applyId: apptDlg.target?.applyId,
      deviceId: apptDlg.form.deviceId || undefined,
      examDate: apptDlg.form.examDate || undefined,
    })
    if (res.code === 200) {
      apptDlg.rec = res.data
      if (!res.data?.options?.length) ElMessage.info(res.data?.message || '近期没有可用时段')
    } else ElMessage.error(res.message || '推荐失败')
  } catch (e) { console.error(e); ElMessage.error('推荐失败') } finally { apptDlg.recLoading.value = false }
}

const useOption = async (opt: any) => {
  apptDlg.form.deviceId = opt.deviceId
  apptDlg.form.examDate = opt.examDate
  await loadBoard()
  apptDlg.form.startTime = opt.startTime
}

const submitAppt = async () => {
  const f = apptDlg.form
  if (!f.deviceId) { ElMessage.warning('请选择设备'); return }
  if (!f.examDate) { ElMessage.warning('请选择检查日期'); return }
  if (!f.startTime) { ElMessage.warning('请点击一个可用时段'); return }
  if (apptDlg.mode === 'reschedule' && !f.reason.trim()) { ElMessage.warning('改约原因必填'); return }
  apptDlg.submitting = true
  try {
    const isBook = apptDlg.mode === 'book'
    const payload = isBook
      ? { applyId: apptDlg.target.applyId, deviceId: f.deviceId, examDate: f.examDate, startTime: f.startTime, remark: f.remark.trim() || undefined }
      : { apptId: apptDlg.target.id, deviceId: f.deviceId, examDate: f.examDate, startTime: f.startTime, reason: f.reason.trim() }
    const res: any = isBook ? await examAppointBook(payload) : await examAppointReschedule(payload)
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功')
      apptDlg.visible = false
      loadPending(); loadAppts(); loadStats()
    } else ElMessage.error(res.message || '预约失败')
  } catch (e: any) {
    // 冲突由服务端判定，message 里点名占用者，必须原样给用户看
    ElMessage.error(e?.response?.data?.message || e?.message || '预约失败')
  } finally { apptDlg.submitting = false }
}

// ================= B 预约台账 =================
const apptLoading = ref(false)
const apptRows = ref<any[]>([])
const apptTotal = ref(0)
const apptCounts = ref<any[]>([])
const apptQuery = reactive({
  apptNo: '', keyword: '', status: null as number | null, deviceId: null as any,
  startDate: '', endDate: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})

const loadAppts = async () => {
  apptLoading.value = true
  try {
    const q = {
      apptNo: apptQuery.apptNo.trim() || undefined,
      keyword: apptQuery.keyword.trim() || undefined,
      status: apptQuery.status ?? undefined,
      deviceId: apptQuery.deviceId ?? undefined,
      startDate: apptQuery.startDate || undefined,
      endDate: apptQuery.endDate || undefined,
      pageNum: apptQuery.pageNum, pageSize: apptQuery.pageSize,
    }
    const [page, cnt]: any[] = await Promise.all([getExamAppointListPage(q), getExamAppointStatusCount(q)])
    if (page.code === 200) {
      apptRows.value = page.data?.records || []
      apptTotal.value = Number(page.data?.total || 0)
    } else ElMessage.error(page.message || '查询失败')
    if (cnt.code === 200) apptCounts.value = cnt.data || []
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { apptLoading.value = false }
}

const resetAppt = () => {
  Object.assign(apptQuery, { apptNo: '', keyword: '', status: null, deviceId: null, startDate: '', endDate: '', pageNum: 1 })
  loadAppts()
}

const filterByStatus = (s: any) => {
  apptQuery.status = s == null ? null : Number(s)
  apptQuery.pageNum = 1
  loadAppts()   // 纯筛选，不携带任何写动作参数
}

const doArrive = async (row: any) => {
  const res: any = await examAppointArrive(row.id).catch((e: any) => ({ message: e?.message }))
  if (res.code === 200) { ElMessage.success(res.message || '已到检') } else ElMessage.error(res.message || '操作失败')
  loadAppts(); loadStats()
}
const doFinish = async (row: any) => {
  const res: any = await examAppointFinish(row.id).catch((e: any) => ({ message: e?.message }))
  if (res.code === 200) { ElMessage.success(res.message || '已完成') } else ElMessage.error(res.message || '操作失败')
  loadAppts(); loadStats()
}
const doCancel = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt(`取消预约 ${row.apptNo}（${row.patientName} ${row.examDate} ${row.timeRange}）`, '取消预约', {
      inputPlaceholder: '取消原因（必填，会写入备注留痕）',
      inputValidator: (v: string) => (v && v.trim() ? true : '取消原因不能为空'),
    })
    const res: any = await examAppointCancel({ apptId: row.id, cancelReason: value.trim() })
    if (res.code === 200) { ElMessage.success(res.message || '已取消') } else ElMessage.error(res.message || '取消失败')
    loadAppts(); loadPending(); loadStats()
  } catch (e) { /* 用户取消弹窗 */ }
}
const runNoShow = async () => {
  const res: any = await examAppointAutoNoShow().catch((e: any) => ({ message: e?.message }))
  if (res.code === 200) ElMessage.success(res.message || '扫描完成')
  else ElMessage.error(res.message || '扫描失败')
  loadAppts(); loadStats()
}

// 详情抽屉
const detail = reactive({ visible: false, loading: false, data: null as any })
const openDetail = async (row: any) => {
  detail.visible = true; detail.loading = true; detail.data = null
  try {
    const res: any = await getExamAppointDetail(row.id)
    if (res.code === 200) detail.data = res.data
    else ElMessage.error(res.message || '加载详情失败')
  } catch (e) { console.error(e); ElMessage.error('加载详情失败') } finally { detail.loading = false }
}

// ================= 设备候选（预约台账的设备筛选下拉） =================
// 设备档位与号源的维护已拆到「检查设备与号源」菜单（2925，sql/181）；
// 本页只消费设备候选，故自行加载一次，不再借用对方的本地状态。
const deviceOptions = ref<any[]>([])
const loadDeviceOptions = async () => {
  try {
    const res: any = await getExamDeviceSelectList({})
    if (res.code === 200) deviceOptions.value = res.data || []
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadDicts(); loadStats(); loadPending(); loadAppts(); loadDeviceOptions()
})
</script>

<template>
  <div class="p-5 space-y-4">
    <!-- 统计 -->
    <div class="grid grid-cols-6 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待预约申请</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1" data-testid="stat-pending">{{ stats.pendingApplies }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日已预约</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1" data-testid="stat-booked">{{ stats.todayBooked }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日到检</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.todayArrived }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日完成</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.todayFinished }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日取消/爽约</div>
        <div class="text-2xl font-semibold text-gray-400 mt-1">{{ stats.todayCancelled + stats.todayNoShow }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">开放/暂停设备</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1" data-testid="stat-device">{{ stats.deviceOpen }}/{{ stats.devicePaused }}</div>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <el-tabs v-model="activeTab">
        <!-- ============ A 预约工作台 ============ -->
        <el-tab-pane label="预约工作台" name="workbench">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="pendQuery.keyword" placeholder="申请单号/患者/项目" clearable style="width: 220px"
              data-testid="pend-keyword" @keyup.enter="pendQuery.pageNum = 1; loadPending()" />
            <el-select v-model="pendQuery.isEmergency" placeholder="急诊" clearable style="width: 110px" :fit-input-width="false">
              <el-option label="急诊" :value="1" />
              <el-option label="普通" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="pendQuery.pageNum = 1; loadPending()">查询</el-button>
            <el-button :icon="Refresh" @click="loadPending">刷新</el-button>
            <span class="text-xs text-gray-400">列表只包含「已缴费（或急诊已提交）且没有 in-flight 预约」的申请单，口径由后端给出</span>
          </div>
          <el-table :data="pendRows" v-loading="pendLoading" size="small" data-testid="exam-pending-table">
            <el-table-column prop="applyNo" label="申请单号" width="180" />
            <el-table-column label="患者" min-width="120">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{ row.patientNo }}</span></template>
            </el-table-column>
            <el-table-column prop="itemName" label="检查项目" min-width="150" show-overflow-tooltip />
            <el-table-column prop="bodyPart" label="部位" width="110" show-overflow-tooltip />
            <el-table-column label="时长" width="70" align="right">
              <template #default="{ row }">{{ row.examMinutes }}′</template>
            </el-table-column>
            <el-table-column label="开单" min-width="130" show-overflow-tooltip>
              <template #default="{ row }">{{ row.applyDeptName }} {{ row.doctorName }}</template>
            </el-table-column>
            <el-table-column label="急诊" width="70">
              <template #default="{ row }">
                <el-tag v-if="Number(row.isEmergency) === 1" type="danger" size="small">急诊</el-tag>
                <span v-else class="text-gray-300">—</span>
              </template>
            </el-table-column>
            <el-table-column label="可承接设备" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="!Number(row.deviceCount)" type="warning" size="small">无</el-tag>
                <span v-else>{{ row.deviceCount }} 台</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :disabled="!Number(row.deviceCount)" v-perm="'medtech:examAppoint:add'"
                  data-testid="btn-book" @click="openBook(row)">占号预约</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination background layout="total, prev, pager, next" :total="pendTotal"
              v-model:current-page="pendQuery.pageNum" v-model:page-size="pendQuery.pageSize" @current-change="loadPending" />
          </div>
        </el-tab-pane>

        <!-- ============ B 预约台账 ============ -->
        <el-tab-pane label="预约台账" name="ledger">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-button v-for="c in apptCounts" :key="c.statusText" size="small" data-testid="appt-chip"
              :type="(c.status === null ? apptQuery.status === null : Number(apptQuery.status) === Number(c.status)) ? 'primary' : ''"
              @click="filterByStatus(c.status)">
              {{ c.statusText }} {{ c.count }}
            </el-button>
            <div class="flex-1" />
            <el-button size="small" v-perm="'medtech:examAppoint:edit'" @click="runNoShow">补跑爽约扫描</el-button>
          </div>
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="apptQuery.apptNo" placeholder="预约单号" clearable style="width: 180px" data-testid="appt-no" />
            <el-input v-model="apptQuery.keyword" placeholder="患者/项目/设备" clearable style="width: 190px" />
            <el-select v-model="apptQuery.deviceId" placeholder="设备" clearable filterable style="width: 190px" :fit-input-width="false">
              <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceName}（${d.deviceCode}）`" :value="d.id" />
            </el-select>
            <el-select v-model="apptQuery.status" placeholder="状态" clearable style="width: 120px" :fit-input-width="false">
              <el-option v-for="d in apptStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-date-picker v-model="apptQuery.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 140px" />
            <el-date-picker v-model="apptQuery.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 140px" />
            <el-button type="primary" :icon="Search" @click="apptQuery.pageNum = 1; loadAppts()">查询</el-button>
            <el-button :icon="Refresh" @click="resetAppt">重置</el-button>
          </div>
          <el-table :data="apptRows" v-loading="apptLoading" size="small" data-testid="exam-appt-table">
            <el-table-column prop="apptNo" label="预约单号" width="150" />
            <el-table-column label="患者" min-width="110">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{ row.patientNo }}</span></template>
            </el-table-column>
            <el-table-column prop="itemName" label="检查项目" min-width="140" show-overflow-tooltip />
            <el-table-column label="设备 / 机房" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.deviceName }}<span class="text-gray-400 text-xs"> {{ row.roomName || '' }}</span></template>
            </el-table-column>
            <el-table-column label="检查时间" width="160">
              <template #default="{ row }">
                <span data-testid="appt-when">{{ row.examDate }} {{ row.timeRange }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="apptStatusTag(row.status) as any" data-testid="appt-status">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="占号人/时间" width="150">
              <template #default="{ row }">{{ row.bookBy }}<span class="text-gray-400 text-xs"> {{ fmtTime(row.bookTime) }}</span></template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
                <el-button v-if="Number(row.status) === APPT_STATUS.BOOKED" v-perm="'medtech:examAppoint:edit'" link type="success" size="small" @click="doArrive(row)">到检</el-button>
                <el-button v-if="Number(row.status) === APPT_STATUS.ARRIVED" v-perm="'medtech:examAppoint:edit'" link type="success" size="small" @click="doFinish(row)">完成</el-button>
                <el-button v-if="isMutable(row.status)" v-perm="'medtech:examAppoint:edit'" link type="warning" size="small" @click="openReschedule(row)">改约</el-button>
                <el-button v-if="isMutable(row.status)" v-perm="'medtech:examAppoint:delete'" link type="danger" size="small" @click="doCancel(row)">取消</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination background layout="total, prev, pager, next" :total="apptTotal"
              v-model:current-page="apptQuery.pageNum" v-model:page-size="apptQuery.pageSize" @current-change="loadAppts" />
          </div>
        </el-tab-pane>

        <!-- ============ C 设备与号源 ============ -->
        <!-- 2026-09 菜单治理：设备档位与号源不属于预约中心业务（预约中心 = biz_exam_appoint 逐患者受理，
             本页签 = biz_exam_device 档位 + biz_exam_slot 号源的科室管理员低频配置），已拆为独立二级菜单
             「检查设备与号源」（400 医技医辅 / 2925，sql/181）。 -->
      </el-tabs>
    </div>

    <!-- ============ 占号 / 改约弹窗 ============ -->
    <el-dialog v-model="apptDlg.visible" :title="apptDlg.mode === 'book' ? '占号预约' : `改约：${apptDlg.target?.apptNo || ''}`"
      width="860px" data-testid="exam-book-dialog">
      <div class="mb-3 text-sm bg-gray-50 rounded p-3 leading-6">
        <span class="font-medium">{{ apptDlg.target?.patientName }}</span>
        <span class="text-gray-400"> / {{ apptDlg.target?.patientNo }}</span>
        　项目：<span class="font-medium">{{ apptDlg.target?.itemName }}</span>
        <span class="text-gray-400">{{ apptDlg.target?.bodyPart ? '（' + apptDlg.target.bodyPart + '）' : '' }}</span>
        　时长：{{ apptDlg.target?.examMinutes }}′
        <el-tag v-if="Number(apptDlg.target?.isEmergency) === 1" type="danger" size="small" class="ml-1">急诊</el-tag>
        <div class="text-xs text-gray-400 mt-1">
          冲突由服务端按「设备行 → 当日格子 → 患者当日预约」的固定加锁顺序判定，一次只暴露一个原因并点名占用者。
        </div>
      </div>
      <div class="flex flex-wrap items-center gap-2 mb-3">
        <el-select v-model="apptDlg.form.deviceId" placeholder="选择设备" filterable style="width: 260px" :fit-input-width="false"
          data-testid="book-device" @change="loadBoard">
          <el-option v-for="d in apptDlg.devices" :key="d.id" :label="`${d.deviceName}（${d.deviceCode}）`" :value="d.id"
            :disabled="Number(d.status) !== 1" />
        </el-select>
        <el-date-picker v-model="apptDlg.form.examDate" type="date" value-format="YYYY-MM-DD" placeholder="检查日期"
          style="width: 150px" data-testid="book-date" @change="loadBoard" />
        <el-button :loading="apptDlg.recLoading" @click="runRecommend">推荐可用时段</el-button>
        <span class="text-xs text-gray-400">开始时段：{{ apptDlg.form.startTime || '未选择' }}</span>
      </div>

      <div v-if="apptDlg.rec?.options?.length" class="mb-3 border border-[#0E9488]/30 rounded p-2">
        <div class="text-xs text-gray-500 mb-1">{{ apptDlg.rec.message || '最近可用时段：' }}</div>
        <div class="flex flex-wrap gap-2">
          <el-button v-for="(o, i) in apptDlg.rec.options" :key="i" size="small" @click="useOption(o)">
            {{ o.examDate }} {{ o.startTime }}-{{ o.endTime }} {{ o.deviceName }}
          </el-button>
        </div>
      </div>

      <div v-loading="apptDlg.boardLoading" class="grid grid-cols-8 gap-2 min-h-[120px]" data-testid="book-grid">
        <div v-for="c in boardCells" :key="c.slotId" :class="cellClass(c)" @click="pickCell(c)">
          <div class="text-xs font-medium">{{ c.startTime }}</div>
          <div class="text-xs">{{ slotCellState(c).label }}</div>
        </div>
      </div>
      <div v-if="!boardCells.length" class="text-xs text-gray-400">选择设备与日期后加载号源格子。</div>

      <el-form label-width="90px" class="mt-3">
        <el-form-item v-if="apptDlg.mode === 'reschedule'" label="改约原因" required>
          <el-input v-model="apptDlg.form.reason" type="textarea" :rows="2" placeholder="如：患者禁食准备未达标，改到次日上午" />
        </el-form-item>
        <el-form-item v-else label="备注">
          <el-input v-model="apptDlg.form.remark" type="textarea" :rows="2" placeholder="如：需家属陪同、增强前确认肾功能" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="apptDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="apptDlg.submitting" v-perm="['medtech:examAppoint:add', 'medtech:examAppoint:edit']" data-testid="book-submit" @click="submitAppt">
          {{ apptDlg.mode === 'book' ? '确认占号' : '确认改约' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 预约单详情 ============ -->
    <el-dialog v-model="detail.visible" title="预约单详情" width="680px">
      <div v-loading="detail.loading">
        <el-descriptions v-if="detail.data" :column="2" border size="small">
          <el-descriptions-item label="预约单号">{{ detail.data.apptNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="apptStatusTag(detail.data.status) as any">{{ detail.data.statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.data.patientName }} / {{ detail.data.patientNo }}</el-descriptions-item>
          <el-descriptions-item label="申请单号">{{ detail.data.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="检查项目">{{ detail.data.itemName }}</el-descriptions-item>
          <el-descriptions-item label="部位">{{ detail.data.bodyPart || '—' }}</el-descriptions-item>
          <el-descriptions-item label="设备">{{ detail.data.deviceName }}（{{ detail.data.deviceCode }}）</el-descriptions-item>
          <el-descriptions-item label="机房">{{ detail.data.roomName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="检查时间">{{ detail.data.examDate }} {{ detail.data.timeRange }}</el-descriptions-item>
          <el-descriptions-item label="时长">{{ detail.data.examMinutes }}′</el-descriptions-item>
          <el-descriptions-item label="开单科室">{{ detail.data.applyDeptName }} {{ detail.data.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="执行科室">{{ detail.data.examDeptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="占号">{{ detail.data.bookBy }} {{ fmtTime(detail.data.bookTime) }}</el-descriptions-item>
          <el-descriptions-item label="到检">{{ fmtTime(detail.data.arriveTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成">{{ fmtTime(detail.data.finishTime) }}</el-descriptions-item>
          <el-descriptions-item label="取消">{{ fmtTime(detail.data.cancelTime) }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ detail.data.cancelReason || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.data.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.cell {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 6px 8px;
  cursor: default;
  background: #fff;
  line-height: 1.4;
}
.cell-free { cursor: pointer; border-color: #bbe3de; background: #f2fbfa; }
.cell-free:hover { border-color: #0e9488; }
.cell-on { border-color: #1269b5; background: #e8f1fa; box-shadow: 0 0 0 2px rgba(18, 105, 181, .18); }
.cell-full { background: #fdf1f1; border-color: #f2c3c3; color: #b91c1c; }
.cell-locked { background: #f3f4f6; border-color: #d1d5db; color: #6b7280; }
.cell-past { background: #fafafa; border-color: #ececec; color: #9ca3af; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
