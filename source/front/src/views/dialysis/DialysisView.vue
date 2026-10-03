<script setup lang="ts">
/**
 * 血液净化（透析）中心工作台（sql/108，菜单 413）
 *
 * 四个页签（机位管理已拆为独立菜单 2931「血液净化机位管理」，sql/186）：
 *  1) 日看板：日期 × 三时段 × 全机位，空格子点一下即排班，已占格子看治疗单。
 *  2) 透析档案：一人一档（患者快照服务端重查），在透/暂停/退出，暂停与退出必须写原因。
 *  3) 透析单台账：上机（透前体重+通路评估）→ 下机（透后体重，超滤量与实际时长服务端回算）
 *     → 不良反应补登 → 改期/取消。
 *  4) 工作量统计：例次/人均/超滤均值/不良反应分布/机位负荷。
 * 规则（服务端收口，前端只做显隐）：本域不出收费单、不扣耗材；
 * 状态文案全部走字典 his_dialysis_*，tag 色与动作显隐单点 lib/dialysis.js。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, FirstAidKit, CircleClose, Edit, View, Tickets } from '@element-plus/icons-vue'
import {
  listDialysisArchivePage,
  getDialysisArchive,
  upsertDialysisArchive,
  changeDialysisArchiveStatus,
  listDialysisPrescriptions,
  upsertDialysisPrescription,
  stopDialysisPrescription,
  listDialysisMachineSelect,
  listDialysisSessionPage,
  getDialysisSession,
  getDialysisBoard,
  scheduleDialysisSession,
  rescheduleDialysisSession,
  startDialysisSession,
  finishDialysisSession,
  recordDialysisAdverse,
  cancelDialysisSession,
  getDialysisStats,
} from '@/api/dialysis'
import { getDictDataMapList } from '@/api/system'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, localDateStr } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  ARCHIVE_STATUS,
  SESSION_STATUS,
  archiveStatusTag,
  sessionStatusTag,
  machineStatusTag,
  prescriptionStatusTag,
  canChangeArchive,
  canWritePrescription,
  canStopPrescription,
  canStart,
  canFinish,
  canReschedule,
  canCancel,
  canRecordAdverse,
  canScheduleCell,
} from '@/lib/dialysis'

const activeTab = ref('board')

// ---------------- 字典 ----------------
const dicts = reactive<Record<string, any[]>>({
  archiveStatus: [], access: [], freq: [], dialyzer: [], anticoag: [],
  slot: [], sessionStatus: [], adverse: [], machineStatus: [],
})
const dictText = (key: string, value: any) => dictLabelText(dicts[key], value)

const loadDicts = async () => {
  const types = [
    DICT_TYPE.DIALYSIS_STATUS, DICT_TYPE.DIALYSIS_ACCESS, DICT_TYPE.DIALYSIS_FREQ,
    DICT_TYPE.DIALYSIS_DIALYZER, DICT_TYPE.DIALYSIS_ANTICOAG, DICT_TYPE.DIALYSIS_SLOT,
    DICT_TYPE.DIALYSIS_SESSION_STATUS, DICT_TYPE.DIALYSIS_ADVERSE, DICT_TYPE.DIALYSIS_MACHINE_STATUS,
  ]
  try {
    // selectGroup 单次最多 5 个类型（超了抛「数据字典每次最多只能查询5个」），分页取再合并
    const map: Record<string, any[]> = {}
    for (let i = 0; i < types.length; i += 5) {
      const res: any = await getDictDataMapList(types.slice(i, i + 5).join(','))
      Object.assign(map, res?.data || {})
    }
    dicts.archiveStatus = map[DICT_TYPE.DIALYSIS_STATUS] || []
    dicts.access = map[DICT_TYPE.DIALYSIS_ACCESS] || []
    dicts.freq = map[DICT_TYPE.DIALYSIS_FREQ] || []
    dicts.dialyzer = map[DICT_TYPE.DIALYSIS_DIALYZER] || []
    dicts.anticoag = map[DICT_TYPE.DIALYSIS_ANTICOAG] || []
    dicts.slot = map[DICT_TYPE.DIALYSIS_SLOT] || []
    dicts.sessionStatus = map[DICT_TYPE.DIALYSIS_SESSION_STATUS] || []
    dicts.adverse = map[DICT_TYPE.DIALYSIS_ADVERSE] || []
    dicts.machineStatus = map[DICT_TYPE.DIALYSIS_MACHINE_STATUS] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

// ---------------- 页签一：日看板 ----------------
const boardDate = ref(localDateStr())
const boardLoading = ref(false)
const board = reactive<{ slot1: any[]; slot2: any[]; slot3: any[]; sessionCount: number; doneCount: number }>({
  slot1: [], slot2: [], slot3: [], sessionCount: 0, doneCount: 0,
})

const loadBoard = async () => {
  boardLoading.value = true
  try {
    const res: any = await getDialysisBoard(boardDate.value)
    if (res.code === 200) {
      board.slot1 = res.data?.slot1 || []
      board.slot2 = res.data?.slot2 || []
      board.slot3 = res.data?.slot3 || []
      board.sessionCount = res.data?.sessionCount || 0
      board.doneCount = res.data?.doneCount || 0
    } else {
      ElMessage.error(res.message || '加载看板失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载看板失败')
  } finally {
    boardLoading.value = false
  }
}

const slotTables = computed(() => [
  { slot: 1, rows: board.slot1 },
  { slot: 2, rows: board.slot2 },
  { slot: 3, rows: board.slot3 },
])

const onCellClick = async (cell: any) => {
  if (cell.sessionId) {
    await openSessionView(cell.sessionId)
    return
  }
  if (!canScheduleCell(cell)) {
    ElMessage.warning('该机位当前为维修/停用，不能排班')
    return
  }
  await loadArchiveOptions()
  scheduleForm.dialysisDate = boardDate.value
  scheduleForm.timeSlot = cell.slot
  scheduleForm.machineId = cell.machineId
  scheduleForm.archiveId = null
  scheduleForm.remark = ''
  scheduleDialog.value = true
}

// ---------------- 排班弹框 ----------------
const scheduleDialog = ref(false)
const scheduling = ref(false)
const archiveOptions = ref<any[]>([])
const scheduleForm = reactive<{ dialysisDate: string; timeSlot: number | null; machineId: any; archiveId: any; remark: string }>({
  dialysisDate: '', timeSlot: null, machineId: null, archiveId: null, remark: '',
})

const loadArchiveOptions = async () => {
  if (archiveOptions.value.length) return
  try {
    const res: any = await listDialysisArchivePage({ status: ARCHIVE_STATUS.ON, pageNum: 1, pageSize: 100 })
    archiveOptions.value = res?.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

const machineOptions = ref<any[]>([])
const loadMachineOptions = async () => {
  try {
    const res: any = await listDialysisMachineSelect()
    machineOptions.value = res?.data || []
  } catch (e) {
    console.error(e)
  }
}

const doSchedule = async () => {
  if (!scheduleForm.archiveId || !scheduleForm.machineId || !scheduleForm.timeSlot || !scheduleForm.dialysisDate) {
    ElMessage.warning('请选择患者、日期、时段与机位')
    return
  }
  scheduling.value = true
  try {
    const res: any = await scheduleDialysisSession({
      archiveId: scheduleForm.archiveId,
      dialysisDate: scheduleForm.dialysisDate,
      timeSlot: scheduleForm.timeSlot,
      machineId: scheduleForm.machineId,
      remark: scheduleForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '已排班')
      scheduleDialog.value = false
      await loadBoard()
      await loadSession()
    } else {
      ElMessage.error(res.message || '排班失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '排班失败')
  } finally {
    scheduling.value = false
  }
}

// ---------------- 页签二：透析档案 ----------------
const arLoading = ref(false)
const arRows = ref<any[]>([])
const arTotal = ref(0)
const arQuery = reactive({ dialysisNo: '', patientName: '', accessType: null as number | null, status: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })

const loadArchive = async () => {
  arLoading.value = true
  try {
    const res: any = await listDialysisArchivePage({
      dialysisNo: arQuery.dialysisNo.trim() || undefined,
      patientName: arQuery.patientName.trim() || undefined,
      accessType: arQuery.accessType ?? undefined,
      status: arQuery.status ?? undefined,
      pageNum: arQuery.pageNum,
      pageSize: arQuery.pageSize,
    })
    if (res.code === 200) {
      arRows.value = res.data?.records || []
      arTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询档案失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询档案失败')
  } finally {
    arLoading.value = false
  }
}

const resetArchiveQuery = () => {
  arQuery.dialysisNo = ''
  arQuery.patientName = ''
  arQuery.accessType = null
  arQuery.status = null
  arQuery.pageNum = 1
  loadArchive()
}

const archiveDialog = ref(false)
const archiveSaving = ref(false)
const archiveForm = reactive<any>({
  id: null, patientId: null, patientName: '', dialysisNo: '',
  firstDialysisDate: localDateStr(), cause: '', accessType: null, accessSite: '',
  dialysisFreq: 3, remark: '',
})

const openArchiveEdit = async (row?: any) => {
  archiveForm.id = row?.id ?? null
  archiveForm.patientId = row?.patientId ?? null
  archiveForm.patientName = row?.patientName ?? ''
  archiveForm.dialysisNo = row?.dialysisNo ?? ''
  archiveForm.remark = row?.remark ?? ''
  if (row?.id) {
    // 编辑回显走 getById（明文电话），列表是脱敏出参
    try {
      const res: any = await getDialysisArchive(row.id)
      if (res.code === 200 && res.data) {
        Object.assign(archiveForm, {
          firstDialysisDate: res.data.firstDialysisDate,
          cause: res.data.cause || '',
          accessType: res.data.accessType,
          accessSite: res.data.accessSite || '',
          dialysisFreq: res.data.dialysisFreq,
          remark: res.data.remark || '',
        })
      }
    } catch (e) {
      console.error(e)
    }
  } else {
    Object.assign(archiveForm, {
      firstDialysisDate: localDateStr(), cause: '', accessType: null, accessSite: '', dialysisFreq: 3, remark: '',
    })
  }
  archiveDialog.value = true
}

const onPatientPicked = (p: any) => {
  archiveForm.patientId = p?.id ?? null
  archiveForm.patientName = p?.patientName || p?.name || ''
}

const saveArchive = async () => {
  if (!archiveForm.patientId || !archiveForm.firstDialysisDate || !archiveForm.accessType) {
    ElMessage.warning('请选择患者、首次透析日期与血管通路')
    return
  }
  archiveSaving.value = true
  try {
    const res: any = await upsertDialysisArchive({
      id: archiveForm.id ?? undefined,
      patientId: archiveForm.patientId,
      firstDialysisDate: archiveForm.firstDialysisDate,
      cause: archiveForm.cause || undefined,
      accessType: archiveForm.accessType,
      accessSite: archiveForm.accessSite || undefined,
      dialysisFreq: archiveForm.dialysisFreq ?? undefined,
      remark: archiveForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '保存成功')
      archiveDialog.value = false
      archiveOptions.value = []
      await loadArchive()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    archiveSaving.value = false
  }
}

// 档案状态变更（暂停/退出原因必填）
const statusDialog = ref(false)
const statusSaving = ref(false)
const statusForm = reactive<any>({ id: null, dialysisNo: '', patientName: '', currentStatus: null, status: null, reason: '' })

const openStatusChange = (row: any) => {
  Object.assign(statusForm, {
    id: row.id, dialysisNo: row.dialysisNo, patientName: row.patientName,
    currentStatus: row.status, status: null, reason: '',
  })
  statusDialog.value = true
}

const saveStatus = async () => {
  if (!statusForm.status) {
    ElMessage.warning('请选择目标状态')
    return
  }
  if (statusForm.status !== ARCHIVE_STATUS.ON && !statusForm.reason.trim()) {
    ElMessage.warning('暂停/退出必须填写原因')
    return
  }
  statusSaving.value = true
  try {
    const res: any = await changeDialysisArchiveStatus({
      id: statusForm.id, status: statusForm.status, reason: statusForm.reason || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '状态已更新')
      statusDialog.value = false
      await loadArchive()
    } else {
      ElMessage.error(res.message || '状态更新失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '状态更新失败')
  } finally {
    statusSaving.value = false
  }
}

// ---------------- 处方（档案行内弹框）----------------
const preDialog = ref(false)
const preArchive = ref<any>(null)
const preRows = ref<any[]>([])
const preLoading = ref(false)
const preForm = reactive<any>({
  id: null, dryWeight: null, durationMin: 240, bloodFlow: 220, dialyzer: 3,
  anticoagulant: 1, anticoagDose: '', targetUltraMl: null, startDate: localDateStr(), remark: '',
})

const loadPrescriptions = async () => {
  if (!preArchive.value?.id) return
  preLoading.value = true
  try {
    const res: any = await listDialysisPrescriptions(preArchive.value.id)
    preRows.value = res?.data || []
  } catch (e) {
    console.error(e)
  } finally {
    preLoading.value = false
  }
}

const openPrescription = async (row: any) => {
  preArchive.value = row
  resetPreForm()
  preDialog.value = true
  await loadPrescriptions()
}

const resetPreForm = () => {
  Object.assign(preForm, {
    id: null, dryWeight: null, durationMin: 240, bloodFlow: 220, dialyzer: 3,
    anticoagulant: 1, anticoagDose: '', targetUltraMl: null, startDate: localDateStr(), remark: '',
  })
}

const openPreEdit = (row?: any) => {
  if (!row) {
    resetPreForm()
    return
  }
  Object.assign(preForm, {
    id: row.id, dryWeight: row.dryWeight, durationMin: row.durationMin, bloodFlow: row.bloodFlow,
    dialyzer: row.dialyzer, anticoagulant: row.anticoagulant, anticoagDose: row.anticoagDose || '',
    targetUltraMl: row.targetUltraMl, startDate: row.startDate, remark: row.remark || '',
  })
}

const savePrescription = async () => {
  if (preForm.dryWeight == null) {
    ElMessage.warning('干体重不能为空')
    return
  }
  if (!preForm.startDate) {
    ElMessage.warning('处方生效日期不能为空')
    return
  }
  try {
    const res: any = await upsertDialysisPrescription({
      id: preForm.id ?? undefined,
      archiveId: preArchive.value.id,
      dryWeight: preForm.dryWeight,
      durationMin: preForm.durationMin ?? undefined,
      bloodFlow: preForm.bloodFlow ?? undefined,
      dialyzer: preForm.dialyzer ?? undefined,
      anticoagulant: preForm.anticoagulant ?? undefined,
      anticoagDose: preForm.anticoagDose || undefined,
      targetUltraMl: preForm.targetUltraMl ?? undefined,
      startDate: preForm.startDate,
      remark: preForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '处方已保存')
      resetPreForm()
      archiveOptions.value = []
      await loadPrescriptions()
      await loadArchive()
    } else {
      ElMessage.error(res.message || '处方保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '处方保存失败')
  }
}

const stopPrescription = async (row: any) => {
  try {
    const res: any = await stopDialysisPrescription({ id: row.id, reason: '临床调整，停用该处方' })
    if (res.code === 200) {
      ElMessage.success(res.message || '处方已停用')
      await loadPrescriptions()
      await loadArchive()
    } else {
      ElMessage.error(res.message || '处方停用失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '处方停用失败')
  }
}

// ---------------- 页签三：透析单台账 ----------------
const seLoading = ref(false)
const seRows = ref<any[]>([])
const seTotal = ref(0)
const seQuery = reactive({
  sessionNo: '', patientName: '', startDate: '' as string, endDate: '' as string,
  timeSlot: null as number | null, status: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})

const loadSession = async () => {
  seLoading.value = true
  try {
    const res: any = await listDialysisSessionPage({
      sessionNo: seQuery.sessionNo.trim() || undefined,
      patientName: seQuery.patientName.trim() || undefined,
      startDate: seQuery.startDate || undefined,
      endDate: seQuery.endDate || undefined,
      timeSlot: seQuery.timeSlot ?? undefined,
      status: seQuery.status ?? undefined,
      pageNum: seQuery.pageNum,
      pageSize: seQuery.pageSize,
    })
    if (res.code === 200) {
      seRows.value = res.data?.records || []
      seTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询透析单失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询透析单失败')
  } finally {
    seLoading.value = false
  }
}

const resetSessionQuery = () => {
  Object.assign(seQuery, {
    sessionNo: '', patientName: '', startDate: '', endDate: '',
    timeSlot: null, status: null, pageNum: 1,
  })
  loadSession()
}

const sessionViewDialog = ref(false)
const sessionView = ref<any>(null)
const openSessionView = async (id: number) => {
  try {
    const res: any = await getDialysisSession(id)
    if (res.code === 200) {
      sessionView.value = res.data
      sessionViewDialog.value = true
    } else {
      ElMessage.error(res.message || '查询透析单失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询透析单失败')
  }
}

// 上机 / 下机 / 不良反应 / 取消 / 改期
const actionDialog = ref('')
const actionSaving = ref(false)
const actionRow = ref<any>(null)
const actionForm = reactive<any>({
  beforeWeight: null, accessCheck: '', onTime: '', afterWeight: null, offTime: '',
  adverseType: null, adverseDesc: '', reason: '', dialysisDate: '', timeSlot: null, machineId: null,
})

const openAction = (kind: string, row: any) => {
  actionRow.value = row
  actionDialog.value = kind
  Object.assign(actionForm, {
    beforeWeight: null, accessCheck: '', onTime: '', afterWeight: null, offTime: '',
    adverseType: null, adverseDesc: '', reason: '',
    dialysisDate: row.dialysisDate, timeSlot: row.timeSlot, machineId: row.machineId,
  })
}

const doAction = async () => {
  const row = actionRow.value
  if (!row) return
  const kind = actionDialog.value
  let payload: any = { id: row.id }
  let call: any = null
  if (kind === 'start') {
    if (actionForm.beforeWeight == null || !actionForm.accessCheck.trim()) {
      ElMessage.warning('透前体重与通路评估都必填')
      return
    }
    payload = { ...payload, beforeWeight: actionForm.beforeWeight, accessCheck: actionForm.accessCheck, onTime: actionForm.onTime || undefined }
    call = startDialysisSession
  } else if (kind === 'finish') {
    if (actionForm.afterWeight == null) {
      ElMessage.warning('透后体重必填')
      return
    }
    payload = { ...payload, afterWeight: actionForm.afterWeight, offTime: actionForm.offTime || undefined }
    call = finishDialysisSession
  } else if (kind === 'adverse') {
    if (!actionForm.adverseType) {
      ElMessage.warning('请选择不良反应类型')
      return
    }
    payload = { ...payload, adverseType: actionForm.adverseType, adverseDesc: actionForm.adverseDesc || undefined }
    call = recordDialysisAdverse
  } else if (kind === 'cancel') {
    if (!actionForm.reason.trim()) {
      ElMessage.warning('取消原因必填')
      return
    }
    payload = { ...payload, reason: actionForm.reason }
    call = cancelDialysisSession
  } else if (kind === 'reschedule') {
    if (!actionForm.dialysisDate || !actionForm.machineId || !actionForm.timeSlot) {
      ElMessage.warning('日期、时段、机位都必选')
      return
    }
    payload = { ...payload, dialysisDate: actionForm.dialysisDate, timeSlot: actionForm.timeSlot, machineId: actionForm.machineId }
    call = rescheduleDialysisSession
  }
  actionSaving.value = true
  try {
    const res: any = await call(payload)
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功')
      actionDialog.value = ''
      await loadSession()
      await loadBoard()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    actionSaving.value = false
  }
}

const actionTitle = computed(() => ({
  start: '上机登记', finish: '下机登记', adverse: '登记不良反应', cancel: '取消治疗单', reschedule: '改期/改机位',
}[actionDialog.value] || ''))

// 机位管理页签已拆为独立菜单 2931（sql/186）；loadMachineOptions 保留供排班下拉使用。

// ---------------- 页签四：工作量统计 ----------------
const stLoading = ref(false)
const stRange = ref<[string, string] | null>([localDateStr(), localDateStr()])
const stats = ref<any>(null)

const loadStats = async () => {
  if (!stRange.value || !stRange.value[0] || !stRange.value[1]) {
    ElMessage.warning('请选择统计区间')
    return
  }
  stLoading.value = true
  try {
    const res: any = await getDialysisStats({ startDate: stRange.value[0], endDate: stRange.value[1] })
    if (res.code === 200) {
      stats.value = res.data
    } else {
      ElMessage.error(res.message || '统计失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '统计失败')
  } finally {
    stLoading.value = false
  }
}

const onTabChange = (name: string) => {
  if (name === 'board') loadBoard()
  if (name === 'archive') loadArchive()
  if (name === 'session') loadSession()
  if (name === 'stats') loadStats()
}

onMounted(() => {
  loadDicts()
  loadMachineOptions()
  loadBoard()
})
</script>

<template>
  <div class="p-5">
    <el-tabs v-model="activeTab" data-testid="hd-tabs" @tab-change="onTabChange">
      <!-- ============ 日看板 ============ -->
      <el-tab-pane label="机位日看板" name="board">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3" v-loading="boardLoading">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800">透析机位排班看板</h3>
            <el-date-picker v-model="boardDate" type="date" value-format="YYYY-MM-DD" placeholder="日期"
                            class="w-40" :clearable="false" data-testid="hd-board-date" @change="loadBoard" />
            <el-button :icon="Refresh" @click="loadBoard">刷新</el-button>
            <span class="text-sm text-slate-500">
              当日已排 {{ board.sessionCount }} 例，完成 {{ board.doneCount }} 例；点空格子排班，点已占格子看治疗单
            </span>
          </div>

          <div v-for="group in slotTables" :key="group.slot" class="space-y-2">
            <div class="text-sm font-medium text-slate-600">{{ dictText('slot', group.slot) }}</div>
            <el-table :data="group.rows" border size="small" :data-testid="`hd-board-slot${group.slot}`">
              <el-table-column prop="machineNo" label="机位" width="90" />
              <el-table-column prop="roomName" label="分区" width="130">
                <template #default="{ row }">{{ row.roomName || '—' }}</template>
              </el-table-column>
              <el-table-column label="机位状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="machineStatusTag(row.machineStatus)" size="small">{{ dictText('machineStatus', row.machineStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="患者" min-width="150">
                <template #default="{ row }">
                  <span v-if="row.sessionId">{{ row.patientName }}（{{ row.patientNo }}）</span>
                  <span v-else class="text-slate-400">空闲</span>
                </template>
              </el-table-column>
              <el-table-column label="治疗单" width="150">
                <template #default="{ row }">
                  <el-tag v-if="row.sessionId" :type="sessionStatusTag(row.sessionStatus)" size="small">
                    {{ row.sessionNo }} · {{ dictText('sessionStatus', row.sessionStatus) }}
                  </el-tag>
                  <span v-else class="text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column label="透前/透后 (kg)" width="140" align="right">
                <template #default="{ row }">{{ row.beforeWeight ?? '—' }} / {{ row.afterWeight ?? '—' }}</template>
              </el-table-column>
              <el-table-column label="超滤 (ml)" width="110" align="right">
                <template #default="{ row }">{{ row.ultraMl ?? '—' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="110" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small"
                             :data-testid="`hd-board-btn-${group.slot}-${row.machineNo}`"
                             @click="onCellClick({ ...row, slot: group.slot })">
                    {{ row.sessionId ? '查看' : '排班' }}
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-4 text-sm text-slate-400">还没有机位，请先到「血液净化机位管理」菜单维护</div>
              </template>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 透析档案 ============ -->
      <el-tab-pane label="透析档案" name="archive">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">透析患者档案（一人一档）</h3>
            <el-input v-model="arQuery.dialysisNo" placeholder="透析号" clearable class="w-40" data-testid="hd-ar-no" />
            <el-input v-model="arQuery.patientName" placeholder="患者姓名" clearable class="w-40" data-testid="hd-ar-name" />
            <el-select v-model="arQuery.accessType" placeholder="血管通路" clearable class="w-40" :fit-input-width="false">
              <el-option v-for="d in dicts.access" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="arQuery.status" placeholder="档案状态" clearable class="w-32" :fit-input-width="false">
              <el-option v-for="d in dicts.archiveStatus" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-button :icon="Search" @click="() => { arQuery.pageNum = 1; loadArchive() }">查询</el-button>
            <el-button :icon="Refresh" @click="resetArchiveQuery">重置</el-button>
            <el-button v-perm="'medtech:dialysis:add'" type="primary" :icon="Plus" data-testid="hd-ar-add-btn"
                       @click="openArchiveEdit()">新建档案</el-button>
          </div>

          <el-table v-loading="arLoading" :data="arRows" border data-testid="hd-archive-table">
            <el-table-column prop="dialysisNo" label="透析号" width="140" />
            <el-table-column prop="patientName" label="患者" width="100" />
            <el-table-column prop="patientNo" label="患者编号" width="130" />
            <el-table-column label="联系电话" width="120">
              <template #default="{ row }">{{ row.phoneMasked || '—' }}</template>
            </el-table-column>
            <el-table-column prop="firstDialysisDate" label="首透日期" width="110" />
            <el-table-column label="血管通路" width="120">
              <template #default="{ row }">{{ dictText('access', row.accessType) }}</template>
            </el-table-column>
            <el-table-column prop="accessSite" label="通路部位" min-width="140">
              <template #default="{ row }">{{ row.accessSite || '—' }}</template>
            </el-table-column>
            <el-table-column label="频次" width="110">
              <template #default="{ row }">{{ dictText('freq', row.dialysisFreq) }}</template>
            </el-table-column>
            <el-table-column label="有效处方" min-width="180">
              <template #default="{ row }">
                <span v-if="row.activePrescriptionId">
                  干体重 {{ row.dryWeight }}kg · {{ row.durationMin }}min · {{ dictText('dialyzer', row.dialyzer) }}
                </span>
                <el-tag v-else type="warning" size="small">未开处方</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="例次(完成/总)" width="120" align="right">
              <template #default="{ row }">{{ row.sessionDone || 0 }}/{{ row.sessionTotal || 0 }}</template>
            </el-table-column>
            <el-table-column prop="lastSessionDate" label="最近透析" width="110">
              <template #default="{ row }">{{ row.lastSessionDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="archiveStatusTag(row.status)" size="small">{{ dictText('archiveStatus', row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="'medtech:dialysis:add'" link type="primary" size="small"
                           data-testid="hd-ar-edit-btn" @click="openArchiveEdit(row)">编辑</el-button>
                <el-button link type="primary" size="small" data-testid="hd-ar-pre-btn"
                           @click="openPrescription(row)">处方</el-button>
                <el-button v-if="canChangeArchive(row.status)" v-perm="'medtech:dialysis:add'" link type="warning" size="small"
                           data-testid="hd-ar-status-btn" @click="openStatusChange(row)">状态</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400">还没有透析档案，点「新建档案」从患者主档选人生成</div>
            </template>
          </el-table>

          <el-pagination v-model:current-page="arQuery.pageNum" v-model:page-size="arQuery.pageSize"
                         :total="arTotal" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next"
                         @current-change="loadArchive" @size-change="() => { arQuery.pageNum = 1; loadArchive() }" />
        </div>
      </el-tab-pane>

      <!-- ============ 透析单台账 ============ -->
      <el-tab-pane label="透析单台账" name="session">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">透析单（排班→上机→下机）</h3>
            <el-input v-model="seQuery.sessionNo" placeholder="透析单号" clearable class="w-40" data-testid="hd-se-no" />
            <el-input v-model="seQuery.patientName" placeholder="患者姓名" clearable class="w-40" data-testid="hd-se-name" />
            <el-date-picker v-model="seQuery.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" class="w-36" />
            <el-date-picker v-model="seQuery.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" class="w-36" />
            <el-select v-model="seQuery.timeSlot" placeholder="时段" clearable class="w-28" :fit-input-width="false">
              <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="seQuery.status" placeholder="状态" clearable class="w-28" :fit-input-width="false">
              <el-option v-for="d in dicts.sessionStatus" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-button :icon="Search" @click="() => { seQuery.pageNum = 1; loadSession() }">查询</el-button>
            <el-button :icon="Refresh" @click="resetSessionQuery">重置</el-button>
          </div>

          <el-table v-loading="seLoading" :data="seRows" border data-testid="hd-session-table">
            <el-table-column prop="sessionNo" label="透析单号" width="150" />
            <el-table-column prop="dialysisDate" label="日期" width="110" />
            <el-table-column label="时段" width="80">
              <template #default="{ row }">{{ dictText('slot', row.timeSlot) }}</template>
            </el-table-column>
            <el-table-column prop="machineNo" label="机位" width="90" />
            <el-table-column prop="patientName" label="患者" width="100" />
            <el-table-column label="处方快照" width="160">
              <template #default="{ row }">{{ row.dryWeight }}kg / {{ row.durationMin }}min / {{ row.bloodFlow }}ml·min</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="sessionStatusTag(row.status)" size="small">{{ dictText('sessionStatus', row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="透前/透后" width="120" align="right">
              <template #default="{ row }">{{ row.beforeWeight ?? '—' }} / {{ row.afterWeight ?? '—' }}</template>
            </el-table-column>
            <el-table-column prop="ultraMl" label="超滤(ml)" width="100" align="right">
              <template #default="{ row }">{{ row.ultraMl ?? '—' }}</template>
            </el-table-column>
            <el-table-column prop="actualDurationMin" label="实际时长" width="90" align="right">
              <template #default="{ row }">{{ row.actualDurationMin ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="不良反应" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.adverseType" type="danger" size="small">{{ dictText('adverse', row.adverseType) }}</el-tag>
                <span v-else class="text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="290" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" data-testid="hd-se-view-btn" @click="openSessionView(row.id)">详情</el-button>
                <el-button v-if="canStart(row.status)" v-perm="'medtech:dialysis:edit'" link type="success" size="small"
                           data-testid="hd-se-start-btn" @click="openAction('start', row)">上机</el-button>
                <el-button v-if="canFinish(row.status)" v-perm="'medtech:dialysis:edit'" link type="success" size="small"
                           data-testid="hd-se-finish-btn" @click="openAction('finish', row)">下机</el-button>
                <el-button v-if="canRecordAdverse(row.status)" v-perm="'medtech:dialysis:edit'" link type="warning" size="small"
                           data-testid="hd-se-adverse-btn" @click="openAction('adverse', row)">反应</el-button>
                <el-button v-if="canReschedule(row.status)" v-perm="'medtech:dialysis:edit'" link type="primary" size="small"
                           data-testid="hd-se-resch-btn" @click="openAction('reschedule', row)">改期</el-button>
                <el-button v-if="canCancel(row.status)" v-perm="'medtech:dialysis:edit'" link type="danger" size="small"
                           data-testid="hd-se-cancel-btn" @click="openAction('cancel', row)">取消</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400">还没有透析单，请到「机位日看板」点空格子排班</div>
            </template>
          </el-table>

          <el-pagination v-model:current-page="seQuery.pageNum" v-model:page-size="seQuery.pageSize"
                         :total="seTotal" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next"
                         @current-change="loadSession" @size-change="() => { seQuery.pageNum = 1; loadSession() }" />
        </div>
      </el-tab-pane>

      <!-- 机位管理页签已拆为独立菜单 2931（sql/186） -->

      <!-- ============ 工作量统计 ============ -->
      <el-tab-pane label="工作量统计" name="stats">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3" v-loading="stLoading">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800">透析工作量与不良反应</h3>
            <el-date-picker v-model="stRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至"
                            start-placeholder="开始日期" end-placeholder="结束日期" class="w-64" data-testid="hd-stats-range" />
            <el-button :icon="Search" @click="loadStats">统计</el-button>
          </div>

          <div v-if="stats" class="grid grid-cols-2 md:grid-cols-4 gap-3" data-testid="hd-stats-cards">
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">在透患者</div>
              <div class="text-2xl font-semibold text-slate-800">{{ stats.inDialysisPatients ?? 0 }}</div>
            </div>
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">区间例次（完成/取消）</div>
              <div class="text-2xl font-semibold text-slate-800">
                {{ stats.sessionTotal ?? 0 }}
                <span class="text-sm text-slate-500">（{{ stats.doneCount ?? 0 }}/{{ stats.cancelledCount ?? 0 }}）</span>
              </div>
            </div>
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">人均例次</div>
              <div class="text-2xl font-semibold text-slate-800">{{ stats.sessionsPerPatient ?? '—' }}</div>
            </div>
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">平均超滤 (ml) / 平均时长 (min)</div>
              <div class="text-2xl font-semibold text-slate-800">
                {{ stats.avgUltraMl ?? '—' }} / {{ stats.avgActualDurationMin ?? '—' }}
              </div>
            </div>
          </div>

          <div v-if="stats" class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <div class="text-sm font-medium text-slate-600 mb-2">不良反应分布</div>
              <el-table :data="stats.adverseTypes || []" border size="small" max-height="240" data-testid="hd-stats-adverse">
                <el-table-column label="类型" min-width="160">
                  <template #default="{ row }">{{ dictText('adverse', row.type) }}</template>
                </el-table-column>
                <el-table-column prop="count" label="例次" width="100" align="right" />
                <template #empty><div class="py-4 text-sm text-slate-400">区间内没有不良反应登记</div></template>
              </el-table>
            </div>
            <div>
              <div class="text-sm font-medium text-slate-600 mb-2">机位负荷</div>
              <el-table :data="stats.machineLoads || []" border size="small" max-height="240" data-testid="hd-stats-machine">
                <el-table-column prop="machineNo" label="机位" min-width="140" />
                <el-table-column prop="count" label="例次" width="100" align="right" />
                <template #empty><div class="py-4 text-sm text-slate-400">区间内没有排班</div></template>
              </el-table>
            </div>
          </div>
          <div v-if="stats && (stats.onMachineCount || stats.scheduledCount)" class="text-sm text-slate-500">
            区间内透析中 {{ stats.onMachineCount }} 例、待上机 {{ stats.scheduledCount }} 例
          </div>
          <el-empty v-if="!stats" description="选择区间后点「统计」" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 排班弹框 ============ -->
    <el-dialog v-model="scheduleDialog" title="透析排班" width="560px" data-testid="hd-schedule-dialog">
      <el-form label-width="90px">
        <el-form-item label="患者">
          <el-select v-model="scheduleForm.archiveId" placeholder="选择在透患者" filterable class="w-full"
                     :fit-input-width="false" data-testid="hd-schedule-archive">
            <el-option v-for="a in archiveOptions" :key="a.id" :value="a.id"
                       :label="`${a.patientName}（${a.patientNo}）${a.dialysisNo}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="scheduleForm.dialysisDate" type="date" value-format="YYYY-MM-DD"
                          class="w-40" :clearable="false" data-testid="hd-schedule-date" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="scheduleForm.timeSlot" class="w-32" :fit-input-width="false" data-testid="hd-schedule-slot">
            <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="机位">
          <el-select v-model="scheduleForm.machineId" class="w-40" filterable :fit-input-width="false" data-testid="hd-schedule-machine">
            <el-option v-for="m in machineOptions" :key="m.id" :value="m.id" :label="`${m.machineNo} ${m.roomName || ''}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" data-testid="hd-schedule-remark" placeholder="可空" />
        </el-form-item>
        <div class="text-xs text-slate-500 pl-[90px]">
          排班会把该患者当前有效处方（干体重/时长/血流速/透析器/抗凝）整套快照进透析单；同一机位同一时段只能排一人。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialog = false">取消</el-button>
        <el-button type="primary" :loading="scheduling" data-testid="hd-schedule-ok" @click="doSchedule">确认排班</el-button>
      </template>
    </el-dialog>

    <!-- ============ 档案弹框 ============ -->
    <el-dialog v-model="archiveDialog" :title="archiveForm.id ? '编辑透析档案' : '新建透析档案'" width="620px"
               data-testid="hd-archive-dialog">
      <el-form label-width="100px">
        <el-form-item label="患者" required>
          <PatientSelect v-if="!archiveForm.id" v-model="archiveForm.patientId" @select="onPatientPicked"
                        data-testid="hd-archive-patient" />
          <span v-else>{{ archiveForm.patientName }}（{{ archiveForm.dialysisNo }}）</span>
        </el-form-item>
        <el-form-item label="首透日期" required>
          <el-date-picker v-model="archiveForm.firstDialysisDate" type="date" value-format="YYYY-MM-DD"
                          class="w-40" :clearable="false" data-testid="hd-archive-first-date" />
        </el-form-item>
        <el-form-item label="血管通路" required>
          <el-select v-model="archiveForm.accessType" placeholder="选择通路" class="w-44" :fit-input-width="false"
                     data-testid="hd-archive-access">
            <el-option v-for="d in dicts.access" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="通路部位">
          <el-input v-model="archiveForm.accessSite" placeholder="如 左前臂桡动脉-头静脉" data-testid="hd-archive-site" />
        </el-form-item>
        <el-form-item label="透析频次">
          <el-select v-model="archiveForm.dialysisFreq" class="w-40" :fit-input-width="false" data-testid="hd-archive-freq">
            <el-option v-for="d in dicts.freq" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="原发病">
          <el-input v-model="archiveForm.cause" type="textarea" :rows="2" placeholder="如 慢性肾小球肾炎致慢性肾脏病5期"
                    data-testid="hd-archive-cause" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="archiveForm.remark" data-testid="hd-archive-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveDialog = false">取消</el-button>
        <el-button type="primary" :loading="archiveSaving" data-testid="hd-archive-ok" @click="saveArchive">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 档案状态弹框 ============ -->
    <el-dialog v-model="statusDialog" title="档案状态变更" width="520px" data-testid="hd-status-dialog">
      <el-form label-width="90px">
        <el-form-item label="患者">{{ statusForm.patientName }}（{{ statusForm.dialysisNo }}）</el-form-item>
        <el-form-item label="当前状态">{{ dictText('archiveStatus', statusForm.currentStatus) }}</el-form-item>
        <el-form-item label="目标状态" required>
          <el-select v-model="statusForm.status" placeholder="选择状态" class="w-40" :fit-input-width="false"
                     data-testid="hd-status-target">
            <el-option v-for="d in dicts.archiveStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)" :disabled="Number(d.dictValue) === Number(statusForm.currentStatus)" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="statusForm.reason" type="textarea" :rows="2"
                    placeholder="暂停/退出必填（转腹透、肾移植、死亡、失访等）" data-testid="hd-status-reason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialog = false">取消</el-button>
        <el-button type="primary" :loading="statusSaving" data-testid="hd-status-ok" @click="saveStatus">确认变更</el-button>
      </template>
    </el-dialog>

    <!-- ============ 处方弹框 ============ -->
    <el-dialog v-model="preDialog" :title="`透析处方 · ${preArchive?.patientName || ''}`" width="900px"
               data-testid="hd-prescription-dialog">
      <div class="space-y-3">
        <el-table :data="preRows" border size="small" max-height="240" v-loading="preLoading"
                  data-testid="hd-prescription-table">
          <el-table-column prop="startDate" label="生效日" width="110" />
          <el-table-column prop="endDate" label="停用日" width="110">
            <template #default="{ row }">{{ row.endDate || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="prescriptionStatusTag(row.status)" size="small">
                {{ row.status === 1 ? '有效' : '已停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dryWeight" label="干体重" width="90" align="right" />
          <el-table-column prop="durationMin" label="时长(min)" width="90" align="right" />
          <el-table-column prop="bloodFlow" label="血流(ml/min)" width="110" align="right" />
          <el-table-column label="透析器" width="130">
            <template #default="{ row }">{{ dictText('dialyzer', row.dialyzer) }}</template>
          </el-table-column>
          <el-table-column label="抗凝" width="110">
            <template #default="{ row }">{{ dictText('anticoag', row.anticoagulant) }}</template>
          </el-table-column>
          <el-table-column prop="anticoagDose" label="剂量" min-width="140">
            <template #default="{ row }">{{ row.anticoagDose || '—' }}</template>
          </el-table-column>
          <el-table-column prop="doctorName" label="开立人" width="90" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" v-perm="'medtech:dialysis:add'" link type="primary" size="small"
                         data-testid="hd-pre-edit-btn" @click="openPreEdit(row)">编辑</el-button>
              <el-button v-if="canStopPrescription(row.status)" v-perm="'medtech:dialysis:add'" link type="danger" size="small"
                         data-testid="hd-pre-stop-btn" @click="stopPrescription(row)">停用</el-button>
            </template>
          </el-table-column>
          <template #empty><div class="py-4 text-sm text-slate-400">还没有处方，下面填一张（没有处方不能排班）</div></template>
        </el-table>

        <div v-if="canWritePrescription(preArchive?.status)" class="border border-slate-200 rounded p-3 space-y-2">
          <div class="text-sm font-medium text-slate-600">{{ preForm.id ? '修改处方' : '新开处方' }}</div>
          <div class="flex items-center gap-3 flex-wrap">
            <el-input-number v-model="preForm.dryWeight" :min="0" :max="999.99" :precision="2" :controls="false"
                             placeholder="干体重kg" class="w-28" data-testid="hd-pre-dry" />
            <el-input-number v-model="preForm.durationMin" :min="30" :max="720" :controls="false"
                             placeholder="时长min" class="w-24" data-testid="hd-pre-duration" />
            <el-input-number v-model="preForm.bloodFlow" :min="50" :max="400" :controls="false"
                             placeholder="血流ml/min" class="w-28" data-testid="hd-pre-flow" />
            <el-select v-model="preForm.dialyzer" placeholder="透析器" class="w-40" :fit-input-width="false" data-testid="hd-pre-dialyzer">
              <el-option v-for="d in dicts.dialyzer" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="preForm.anticoagulant" placeholder="抗凝" class="w-36" :fit-input-width="false" data-testid="hd-pre-anticoag">
              <el-option v-for="d in dicts.anticoag" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-input v-model="preForm.anticoagDose" placeholder="抗凝剂量描述" class="w-52" data-testid="hd-pre-dose" />
            <el-input-number v-model="preForm.targetUltraMl" :min="0" :max="99999" :precision="1" :controls="false"
                             placeholder="目标超滤ml" class="w-32" data-testid="hd-pre-ultra" />
            <el-date-picker v-model="preForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="生效日期"
                            class="w-36" :clearable="false" data-testid="hd-pre-start" />
            <el-input v-model="preForm.remark" placeholder="备注" class="w-40" data-testid="hd-pre-remark" />
            <el-button v-perm="'medtech:dialysis:add'" type="primary" :icon="preForm.id ? Edit : Plus"
                       data-testid="hd-pre-save-btn" @click="savePrescription">{{ preForm.id ? '保存修改' : '开处方' }}</el-button>
            <el-button v-if="preForm.id" :icon="Refresh" @click="resetPreForm">清空重填</el-button>
          </div>
          <div class="text-xs text-slate-500">新开处方会把该档案原来的有效处方自动停用；已停用的处方不可编辑。</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="preDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ============ 动作弹框（上机/下机/反应/取消/改期）============
         modelValue 声明为 Boolean，Vue 会把 '' 强转成 true，所以不能直接 v-model 绑字符串 kind，
         否则进页面就自带弹开一个空弹框（遮罩挡住整页）。 -->
    <el-dialog :model-value="!!actionDialog"
               @update:model-value="(v) => { if (!v) actionDialog = '' }"
               :title="actionTitle" width="560px" data-testid="hd-action-dialog">
      <div class="text-sm text-slate-500 mb-3" v-if="actionRow">
        {{ actionRow.patientName }} · {{ actionRow.dialysisDate }} {{ dictText('slot', actionRow.timeSlot) }} ·
        机位 {{ actionRow.machineNo }} · {{ actionRow.sessionNo }}
      </div>
      <el-form label-width="110px">
        <template v-if="actionDialog === 'start'">
          <el-form-item label="透前体重(kg)" required>
            <el-input-number v-model="actionForm.beforeWeight" :min="0" :max="999.99" :precision="2" :controls="false"
                             class="w-32" data-testid="hd-action-before-weight" />
          </el-form-item>
          <el-form-item label="通路评估" required>
            <el-input v-model="actionForm.accessCheck" type="textarea" :rows="2" placeholder="如 内瘘血流量良好、无渗血"
                      data-testid="hd-action-access" />
          </el-form-item>
          <el-form-item label="上机时间">
            <el-date-picker v-model="actionForm.onTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                            placeholder="默认当前时间" class="w-52" data-testid="hd-action-on-time" />
          </el-form-item>
        </template>
        <template v-else-if="actionDialog === 'finish'">
          <el-form-item label="透后体重(kg)" required>
            <el-input-number v-model="actionForm.afterWeight" :min="0" :max="999.99" :precision="2" :controls="false"
                             class="w-32" data-testid="hd-action-after-weight" />
          </el-form-item>
          <el-form-item label="下机时间">
            <el-date-picker v-model="actionForm.offTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                            placeholder="默认当前时间" class="w-52" data-testid="hd-action-off-time" />
          </el-form-item>
          <div class="text-xs text-slate-500 pl-[110px]">超滤量 =（透前-透后）×1000，实际时长按下机-上机回算，均由服务端计算。</div>
        </template>
        <template v-else-if="actionDialog === 'adverse'">
          <el-form-item label="不良反应" required>
            <el-select v-model="actionForm.adverseType" placeholder="选择类型" class="w-48" :fit-input-width="false"
                       data-testid="hd-action-adverse-type">
              <el-option v-for="d in dicts.adverse" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="处置描述">
            <el-input v-model="actionForm.adverseDesc" type="textarea" :rows="3" placeholder="如 透中血压下降至 90/55，降低血流速并补生理盐水 200ml"
                      data-testid="hd-action-adverse-desc" />
          </el-form-item>
        </template>
        <template v-else-if="actionDialog === 'cancel'">
          <el-form-item label="取消原因" required>
            <el-input v-model="actionForm.reason" type="textarea" :rows="2" placeholder="如 患者临时发热，本次取消"
                      data-testid="hd-action-reason" />
          </el-form-item>
        </template>
        <template v-else-if="actionDialog === 'reschedule'">
          <el-form-item label="日期">
            <el-date-picker v-model="actionForm.dialysisDate" type="date" value-format="YYYY-MM-DD" class="w-40"
                            :clearable="false" data-testid="hd-action-date" />
          </el-form-item>
          <el-form-item label="时段">
            <el-select v-model="actionForm.timeSlot" class="w-32" :fit-input-width="false" data-testid="hd-action-slot">
              <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="机位">
            <el-select v-model="actionForm.machineId" class="w-40" filterable :fit-input-width="false" data-testid="hd-action-machine">
              <el-option v-for="m in machineOptions" :key="m.id" :value="m.id" :label="`${m.machineNo} ${m.roomName || ''}`" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="actionDialog = ''">取消</el-button>
        <el-button type="primary" :loading="actionSaving" data-testid="hd-action-ok" @click="doAction">确认</el-button>
      </template>
    </el-dialog>

    <!-- 机位弹框已随 2931 拆分移除（sql/186） -->

    <!-- ============ 透析单详情（只读）============ -->
    <el-dialog v-model="sessionViewDialog" title="透析单详情" width="820px" data-testid="hd-session-dialog">
      <el-form v-if="sessionView" disabled label-width="110px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="透析单号">{{ sessionView.sessionNo }}</el-form-item>
          <el-form-item label="状态">
            <el-tag :type="sessionStatusTag(sessionView.status)" size="small">{{ dictText('sessionStatus', sessionView.status) }}</el-tag>
          </el-form-item>
          <el-form-item label="日期/时段">{{ sessionView.dialysisDate }} {{ dictText('slot', sessionView.timeSlot) }}</el-form-item>
          <el-form-item label="机位">{{ sessionView.machineNo }} {{ sessionView.roomName || '' }}</el-form-item>
          <el-form-item label="患者">{{ sessionView.patientName }}（{{ sessionView.patientNo }}）</el-form-item>
          <el-form-item label="处方快照">
            {{ sessionView.dryWeight }}kg / {{ sessionView.durationMin }}min / {{ sessionView.bloodFlow }}ml·min ·
            {{ dictText('dialyzer', sessionView.dialyzer) }} · {{ dictText('anticoag', sessionView.anticoagulant) }}
          </el-form-item>
          <el-form-item label="透前体重">{{ sessionView.beforeWeight ?? '—' }} kg</el-form-item>
          <el-form-item label="透后体重">{{ sessionView.afterWeight ?? '—' }} kg</el-form-item>
          <el-form-item label="上机">{{ sessionView.onTime || '—' }} {{ sessionView.onBy || '' }}</el-form-item>
          <el-form-item label="下机">{{ sessionView.offTime || '—' }} {{ sessionView.offBy || '' }}</el-form-item>
          <el-form-item label="实际时长">{{ sessionView.actualDurationMin ?? '—' }} min</el-form-item>
          <el-form-item label="超滤量">{{ sessionView.ultraMl ?? '—' }} ml</el-form-item>
        </div>
        <el-form-item label="通路评估">{{ sessionView.accessCheck || '—' }}</el-form-item>
        <el-form-item label="不良反应">
          <span v-if="sessionView.adverseType">
            {{ dictText('adverse', sessionView.adverseType) }}：{{ sessionView.adverseDesc || '未填处置' }}
          </span>
          <span v-else>无</span>
        </el-form-item>
        <el-form-item v-if="sessionView.cancelReason" label="取消原因">{{ sessionView.cancelReason }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sessionViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
:deep(.el-table .cell) {
  font-size: 13px;
  color: #1e293b;
}
</style>
