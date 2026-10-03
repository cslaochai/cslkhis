<script setup lang="ts">
/**
 * ICU 专科监护工作台（sql/108，菜单 314）
 *
 * 四个页签：
 *  1) 床位看板：ICU 床（复用 sys_bed 的 bed_type='ICU'）逐床一张卡，空格子直接入科，
 *     在科格子显示最近一次监护读数与「上次记录多久前」。
 *  2) 入出科台账：在院患者 → 选 ICU 床入科（患者/科室/床位快照服务端重查）→ 出科（终态，
 *     转院/死亡/自动离院必填转归说明）。
 *  3) 监护记录单：一条 = 一个时刻的床边记录（体征 + GCS 三项 + 呼吸机 + 出入量 + 五类导管），
 *     GCS 总分与液体平衡服务端回算；出科后封账禁写。
 *  4) 科室指标：床位使用率、人均记录、死亡与平均滞留、监护等级/呼吸支持分布、
 *     现带管人数、漏记预警（窗口最长 30 天，超出由服务端收口）。
 * 规则（服务端收口，前端只做显隐）：本域不生成医嘱、不出收费单；普通病区三测仍走护理记录不双轨；
 * 状态文案走字典 his_icu_*，tag 色与显隐单点 lib/icu.js。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  listIcuStayPage,
  getIcuStay,
  listIcuAdmissions,
  upsertIcuStay,
  outIcuStay,
  getIcuBedBoard,
  listIcuMonitorPage,
  getIcuMonitorTrend,
  upsertIcuMonitor,
  getIcuStats,
} from '@/api/icu'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, localDateStr } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  STAY_STATUS,
  stayStatusTag,
  careLevelTag,
  canWriteMonitor,
  canEditStay,
  canOutStay,
  outNeedsReason,
  gcsTotalOf,
  gcsIncomplete,
  balanceOf,
  bpText,
} from '@/lib/icu'

const activeTab = ref('board')

const nowText = () => {
  const d = new Date()
  const p = (v: number) => String(v).padStart(2, '0')
  return `${localDateStr(d)} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** 空串统一转 null：未测项不落 0，避免「体温 36」与「没测」混成一列 */
const blankToNull = (obj: Record<string, any>) => {
  const out: Record<string, any> = {}
  Object.keys(obj).forEach((k) => {
    const v = obj[k]
    out[k] = v === '' || v === undefined ? null : v
  })
  return out
}

// ---------------- 字典 ----------------
const dicts = reactive<Record<string, any[]>>({ stayStatus: [], careLevel: [], outDest: [], ventMode: [] })
const dictText = (key: string, value: any) => dictLabelText(dicts[key], value)

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList([
      DICT_TYPE.ICU_STAY_STATUS, DICT_TYPE.ICU_CARE_LEVEL, DICT_TYPE.ICU_OUT_DEST, DICT_TYPE.ICU_VENT_MODE,
    ].join(','))
    const map = res?.data || {}
    dicts.stayStatus = map[DICT_TYPE.ICU_STAY_STATUS] || []
    dicts.careLevel = map[DICT_TYPE.ICU_CARE_LEVEL] || []
    dicts.outDest = map[DICT_TYPE.ICU_OUT_DEST] || []
    dicts.ventMode = map[DICT_TYPE.ICU_VENT_MODE] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

// ---------------- 页签一：床位看板 ----------------
const bedLoading = ref(false)
const beds = ref<any[]>([])

const loadBoard = async () => {
  bedLoading.value = true
  try {
    const res: any = await getIcuBedBoard()
    if (res.code === 200) {
      beds.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载床位看板失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载床位看板失败')
  } finally {
    bedLoading.value = false
  }
}

const occupiedBeds = computed(() => beds.value.filter((b: any) => b.stayId))
const freeBeds = computed(() => beds.value.filter((b: any) => !b.stayId && Number(b.bedStatus) !== 0))
const disabledBeds = computed(() => beds.value.filter((b: any) => !b.stayId && Number(b.bedStatus) === 0))

/** 距上次监护记录多久（漏记提醒，看板与台账共用） */
const monitorLagText = (row: any) => {
  if (!row.lastMonitorTime) return '尚无记录'
  const diffH = Math.max(0, Math.floor((Date.now() - new Date(String(row.lastMonitorTime).replace(' ', 'T')).getTime()) / 3600000))
  if (diffH < 1) return '1 小时内'
  if (diffH < 24) return `${diffH} 小时前`
  return `${Math.floor(diffH / 24)} 天前`
}
const monitorLagDanger = (row: any) => {
  if (!row.lastMonitorTime) return true
  const diffH = (Date.now() - new Date(String(row.lastMonitorTime).replace(' ', 'T')).getTime()) / 3600000
  return diffH >= 8
}

// ---------------- 页签二：入出科台账 ----------------
const stayLoading = ref(false)
const stayRows = ref<any[]>([])
const stayTotal = ref(0)
const stayQuery = reactive({
  stayNo: '',
  patientName: '',
  startDate: '',
  endDate: '',
  careLevel: null as number | null,
  status: null as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

const loadStay = async () => {
  stayLoading.value = true
  try {
    const res: any = await listIcuStayPage({
      stayNo: stayQuery.stayNo.trim() || undefined,
      patientName: stayQuery.patientName.trim() || undefined,
      startDate: stayQuery.startDate || undefined,
      endDate: stayQuery.endDate || undefined,
      careLevel: stayQuery.careLevel ?? undefined,
      status: stayQuery.status ?? undefined,
      pageNum: stayQuery.pageNum,
      pageSize: stayQuery.pageSize,
    })
    if (res.code === 200) {
      stayRows.value = res.data?.records || []
      stayTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询入出科台账失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询入出科台账失败')
  } finally {
    stayLoading.value = false
  }
}

const resetStayQuery = () => {
  stayQuery.stayNo = ''
  stayQuery.patientName = ''
  stayQuery.startDate = ''
  stayQuery.endDate = ''
  stayQuery.careLevel = null
  stayQuery.status = null
  stayQuery.pageNum = 1
  loadStay()
}

/** 在科记录（监护单与看板下拉用，一次性拉满一页） */
const inStays = ref<any[]>([])
const loadInStays = async () => {
  try {
    const res: any = await listIcuStayPage({ status: STAY_STATUS.IN, pageNum: 1, pageSize: 100 })
    inStays.value = res?.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

// ---------------- 入科 / 修改弹框 ----------------
const admitDialog = ref(false)
const admitSaving = ref(false)
const admitForm = reactive<any>({
  id: null,
  admissionId: null,
  bedId: null,
  bedNo: '',
  careLevel: 1,
  inTime: '',
  inGcs: null,
  inDiag: '',
  remark: '',
})
const admitTitle = computed(() => (admitForm.id ? '修改入科信息' : 'ICU 入科登记'))
const admissionOptions = ref<any[]>([])
const admissionLoading = ref(false)

const searchAdmission = async (keyword: string) => {
  admissionLoading.value = true
  try {
    const res: any = await listIcuAdmissions({ keyword: keyword?.trim() || undefined, limit: 50 })
    admissionOptions.value = res?.data || []
  } catch (e) {
    console.error(e)
  } finally {
    admissionLoading.value = false
  }
}

const openAdmit = async (bed?: any, row?: any) => {
  admitForm.id = row?.id ?? null
  admitForm.inTime = row?.inTime || nowText()
  admitForm.careLevel = row?.careLevel ?? 1
  admitForm.inGcs = row?.inGcs ?? null
  admitForm.inDiag = row?.inDiag ?? ''
  admitForm.remark = row?.remark ?? ''
  admitForm.bedId = row?.bedId ?? bed?.bedId ?? null
  admitForm.bedNo = row?.bedNo ?? bed?.bedNo ?? ''
  admitForm.admissionId = row?.admissionId ?? null
  // 修改时患者不可换：候选下拉里放上这条记录的患者，否则 el-select 只显示 ID
  admissionOptions.value = row
    ? [{ admissionId: row.admissionId, patientName: row.patientName, patientNo: row.patientNo, deptName: row.fromDeptName, bedNo: row.bedNo, diagnosis: row.inDiag }]
    : []
  if (!row) await searchAdmission('')
  admitDialog.value = true
}

const saveAdmit = async () => {
  if (!admitForm.admissionId) return ElMessage.warning('请选择住院患者')
  if (!admitForm.bedId) return ElMessage.warning('请选择 ICU 床位')
  if (!admitForm.inTime) return ElMessage.warning('请选择入科时间')
  admitSaving.value = true
  try {
    const res: any = await upsertIcuStay({
      id: admitForm.id || undefined,
      admissionId: admitForm.admissionId,
      bedId: admitForm.bedId,
      careLevel: admitForm.careLevel,
      inTime: admitForm.inTime,
      inGcs: admitForm.inGcs === '' ? null : admitForm.inGcs,
      inDiag: admitForm.inDiag || undefined,
      remark: admitForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '入科已登记')
      admitDialog.value = false
      await Promise.all([loadBoard(), loadStay(), loadInStays()])
    } else {
      ElMessage.error(res.message || '入科登记失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '入科登记失败')
  } finally {
    admitSaving.value = false
  }
}

// ---------------- 出科弹框 ----------------
const outDialog = ref(false)
const outSaving = ref(false)
const outForm = reactive<any>({ id: null, stayNo: '', patientName: '', outTime: '', outDest: null, outReason: '', outGcs: null })

const openOut = (row: any) => {
  outForm.id = row.id
  outForm.stayNo = row.stayNo
  outForm.patientName = row.patientName
  outForm.outTime = nowText()
  outForm.outDest = null
  outForm.outReason = ''
  outForm.outGcs = null
  outDialog.value = true
}

const saveOut = async () => {
  if (!outForm.outTime) return ElMessage.warning('请选择出科时间')
  if (!outForm.outDest) return ElMessage.warning('请选择转出去向')
  if (outNeedsReason(outForm.outDest) && !outForm.outReason.trim()) {
    return ElMessage.warning('转院/死亡/自动离院必须填写转归说明')
  }
  outSaving.value = true
  try {
    const res: any = await outIcuStay({
      id: outForm.id,
      outTime: outForm.outTime,
      outDest: outForm.outDest,
      outReason: outForm.outReason || undefined,
      outGcs: outForm.outGcs === '' || outForm.outGcs == null ? null : outForm.outGcs,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '已出科')
      outDialog.value = false
      await Promise.all([loadBoard(), loadStay(), loadInStays()])
    } else {
      ElMessage.error(res.message || '出科登记失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '出科登记失败')
  } finally {
    outSaving.value = false
  }
}

// ---------------- 入科详情（只读） ----------------
const stayDialog = ref(false)
const stayDetail = ref<any>(null)
const stayMonitors = ref<any[]>([])

const openStayView = async (id: any) => {
  try {
    const res: any = await getIcuStay(id)
    if (res.code === 200 && res.data) {
      stayDetail.value = res.data
      stayDialog.value = true
      const trend: any = await getIcuMonitorTrend(id, undefined)
      stayMonitors.value = trend?.code === 200 ? (trend.data || []) : []
    } else {
      ElMessage.error(res.message || '查询入科记录失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询入科记录失败')
  }
}

// ---------------- 页签三：监护记录单 ----------------
const monLoading = ref(false)
const monRows = ref<any[]>([])
const monTotal = ref(0)
const monQuery = reactive({ stayId: null as any, startDate: '', endDate: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })

const loadMonitor = async () => {
  monLoading.value = true
  try {
    const res: any = await listIcuMonitorPage({
      stayId: monQuery.stayId || undefined,
      startDate: monQuery.startDate || undefined,
      endDate: monQuery.endDate || undefined,
      pageNum: monQuery.pageNum,
      pageSize: monQuery.pageSize,
    })
    if (res.code === 200) {
      monRows.value = res.data?.records || []
      monTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询监护记录失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询监护记录失败')
  } finally {
    monLoading.value = false
  }
}

const resetMonitorQuery = () => {
  monQuery.stayId = null
  monQuery.startDate = ''
  monQuery.endDate = ''
  monQuery.pageNum = 1
  loadMonitor()
}

const monDialog = ref(false)
const monSaving = ref(false)
const monForm = reactive<any>({
  id: null,
  stayId: null,
  patientName: '',
  recordTime: '',
  temperature: '',
  pulse: '',
  respiratory: '',
  sbp: '',
  dbp: '',
  spo2: '',
  gcsEye: null,
  gcsVerbal: null,
  gcsMotor: null,
  pupil: '',
  cvp: '',
  ventMode: null,
  fio2: '',
  peep: '',
  intakeMl: '',
  outputMl: '',
  urineMl: '',
  hasAirway: 0,
  hasCvc: 0,
  hasArterial: 0,
  hasCatheter: 0,
  hasDrain: 0,
  conditionDesc: '',
  handling: '',
  remark: '',
})
const monGcsTotal = computed(() => gcsTotalOf(monForm))
const monBalance = computed(() => balanceOf(monForm.intakeMl, monForm.outputMl))

const emptyMonitor = () => {
  Object.assign(monForm, {
    id: null, stayId: null, patientName: '', recordTime: nowText(),
    temperature: '', pulse: '', respiratory: '', sbp: '', dbp: '', spo2: '',
    gcsEye: null, gcsVerbal: null, gcsMotor: null, pupil: '', cvp: '',
    ventMode: null, fio2: '', peep: '', intakeMl: '', outputMl: '', urineMl: '',
    hasAirway: 0, hasCvc: 0, hasArterial: 0, hasCatheter: 0, hasDrain: 0,
    conditionDesc: '', handling: '', remark: '',
  })
}

const openMonitorCreate = async (stayId?: any) => {
  await loadInStays()
  emptyMonitor()
  const target = stayId ?? monQuery.stayId ?? inStays.value[0]?.id ?? null
  if (target) {
    monForm.stayId = target
    monForm.patientName = inStays.value.find((s: any) => String(s.id) === String(target))?.patientName || ''
  }
  monDialog.value = true
}

const openMonitorEdit = (row: any) => {
  emptyMonitor()
  Object.assign(monForm, {
    id: row.id,
    stayId: row.stayId,
    patientName: row.patientName,
    recordTime: row.recordTime,
    temperature: row.temperature ?? '',
    pulse: row.pulse ?? '',
    respiratory: row.respiratory ?? '',
    sbp: row.sbp ?? '',
    dbp: row.dbp ?? '',
    spo2: row.spo2 ?? '',
    gcsEye: row.gcsEye ?? null,
    gcsVerbal: row.gcsVerbal ?? null,
    gcsMotor: row.gcsMotor ?? null,
    pupil: row.pupil ?? '',
    cvp: row.cvp ?? '',
    ventMode: row.ventMode ?? null,
    fio2: row.fio2 ?? '',
    peep: row.peep ?? '',
    intakeMl: row.intakeMl ?? '',
    outputMl: row.outputMl ?? '',
    urineMl: row.urineMl ?? '',
    hasAirway: Number(row.hasAirway || 0),
    hasCvc: Number(row.hasCvc || 0),
    hasArterial: Number(row.hasArterial || 0),
    hasCatheter: Number(row.hasCatheter || 0),
    hasDrain: Number(row.hasDrain || 0),
    conditionDesc: row.conditionDesc ?? '',
    handling: row.handling ?? '',
    remark: row.remark ?? '',
  })
  monDialog.value = true
}

const saveMonitor = async () => {
  if (!monForm.stayId) return ElMessage.warning('请选择入科记录')
  if (!monForm.recordTime) return ElMessage.warning('请选择记录时刻')
  if (gcsIncomplete(monForm)) return ElMessage.warning('GCS 睁眼/语言/运动需同时填写，或全部留空')
  monSaving.value = true
  try {
    const payload = blankToNull({
      id: monForm.id,
      stayId: monForm.stayId,
      recordTime: monForm.recordTime,
      temperature: monForm.temperature,
      pulse: monForm.pulse,
      respiratory: monForm.respiratory,
      sbp: monForm.sbp,
      dbp: monForm.dbp,
      spo2: monForm.spo2,
      gcsEye: monForm.gcsEye,
      gcsVerbal: monForm.gcsVerbal,
      gcsMotor: monForm.gcsMotor,
      pupil: monForm.pupil,
      cvp: monForm.cvp,
      ventMode: monForm.ventMode,
      fio2: monForm.fio2,
      peep: monForm.peep,
      intakeMl: monForm.intakeMl,
      outputMl: monForm.outputMl,
      urineMl: monForm.urineMl,
      hasAirway: monForm.hasAirway,
      hasCvc: monForm.hasCvc,
      hasArterial: monForm.hasArterial,
      hasCatheter: monForm.hasCatheter,
      hasDrain: monForm.hasDrain,
      conditionDesc: monForm.conditionDesc,
      handling: monForm.handling,
      remark: monForm.remark,
    })
    if (payload.id == null) delete payload.id
    const res: any = await upsertIcuMonitor(payload)
    if (res.code === 200) {
      ElMessage.success(res.message || '监护记录已保存')
      monDialog.value = false
      await Promise.all([loadMonitor(), loadBoard(), loadStay()])
    } else {
      ElMessage.error(res.message || '保存监护记录失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存监护记录失败')
  } finally {
    monSaving.value = false
  }
}

const tubeText = (row: any) => {
  const items = [
    [row.hasAirway, '气道'],
    [row.hasCvc, 'CVC'],
    [row.hasArterial, '动脉'],
    [row.hasCatheter, '尿管'],
    [row.hasDrain, '引流'],
  ].filter((t: any[]) => Number(t[0]) === 1).map((t: any[]) => t[1])
  return items.length ? items.join('/') : '—'
}

// ---------------- 趋势弹框 ----------------
const trendDialog = ref(false)
const trendRows = ref<any[]>([])
const trendTitle = ref('')
const trendHours = ref(24)
const trendStayId = ref<any>(null)

const openTrend = async (stay: any) => {
  trendTitle.value = `${stay.patientName || ''} ${stay.bedNo || ''} · 监护趋势`
  trendStayId.value = stay.id ?? stay.stayId ?? null
  trendHours.value = 24
  await refreshTrend()
  trendDialog.value = true
}

const refreshTrend = async () => {
  if (!trendStayId.value) return ElMessage.warning('请先选择入科记录')
  try {
    const res: any = await getIcuMonitorTrend(trendStayId.value, trendHours.value || undefined)
    if (res.code === 200) {
      trendRows.value = res.data || []
    } else {
      ElMessage.error(res.message || '查询监护趋势失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询监护趋势失败')
  }
}

// ---------------- 页签四：科室指标 ----------------
const statsLoading = ref(false)
const stats = ref<any>(null)
const stRange = ref<string[]>([])
const stLagHours = ref(8)

const loadStats = async () => {
  statsLoading.value = true
  try {
    const res: any = await getIcuStats({
      startDate: stRange.value?.[0] || undefined,
      endDate: stRange.value?.[1] || undefined,
      lagHours: stLagHours.value || undefined,
    })
    if (res.code === 200) {
      stats.value = res.data || null
    } else {
      ElMessage.error(res.message || '查询科室指标失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询科室指标失败')
  } finally {
    statsLoading.value = false
  }
}

const statCards = computed(() => {
  const s = stats.value || {}
  return [
    { key: 'inBed', label: '当前在科 / 开放床位', value: `${s.inCount ?? 0} / ${s.bedTotal ?? 0}` },
    { key: 'useRate', label: '床位使用率', value: s.bedUseRate == null ? '—' : `${s.bedUseRate}%` },
    { key: 'inOut', label: '区间入科 / 出科', value: `${s.inCountRange ?? 0} / ${s.outCountRange ?? 0}` },
    { key: 'monitorTotal', label: '区间监护记录', value: s.monitorTotalRange ?? 0 },
    { key: 'perStay', label: '人均记录条数', value: s.monitorsPerStay ?? '—' },
    { key: 'avgStay', label: '平均滞留小时', value: s.avgStayHours ?? '—' },
    { key: 'death', label: '死亡人数', value: s.deathCount ?? 0 },
    { key: 'lag', label: `漏记预警（≥${stLagHours.value}h）`, value: s.monitorLagCount ?? 0, danger: Number(s.monitorLagCount || 0) > 0 },
  ]
})

const tubeCards = computed(() => {
  const s = stats.value || {}
  return [
    { label: '人工气道', value: s.airwayCount ?? 0 },
    { label: '中心静脉', value: s.cvcCount ?? 0 },
    { label: '动脉置管', value: s.arterialCount ?? 0 },
    { label: '导尿管', value: s.catheterCount ?? 0 },
    { label: '引流管', value: s.drainCount ?? 0 },
  ]
})

const onTabChange = (name: any) => {
  if (name === 'board') loadBoard()
  if (name === 'stays') loadStay()
  if (name === 'monitors') { loadInStays(); loadMonitor() }
  if (name === 'stats') loadStats()
}

onMounted(async () => {
  await loadDicts()
  await loadBoard()
})
</script>

<template>
  <div class="p-5">
    <el-tabs v-model="activeTab" data-testid="icu-tabs" @tab-change="onTabChange">
      <!-- ============ 页签一：床位看板 ============ -->
      <el-tab-pane label="ICU 床位看板" name="board">
        <div class="flex items-center gap-3 mb-3">
          <span class="text-sm text-slate-500">
            开放 {{ beds.length }} 床 · 在科 {{ occupiedBeds.length }} · 空床 {{ freeBeds.length }} · 停用 {{ disabledBeds.length }}
          </span>
          <el-button :icon="Refresh" data-testid="icu-board-refresh" @click="loadBoard">刷新</el-button>
          <el-button v-perm="'ipd:icu:add'" type="primary" :icon="Plus" data-testid="icu-board-admit-btn"
                     @click="openAdmit()">入科登记</el-button>
        </div>

        <div v-loading="bedLoading" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3" data-testid="icu-board">
          <div v-for="bed in beds" :key="bed.bedId"
               class="rounded-lg border p-3"
               :class="bed.stayId ? 'bg-white border-slate-300' : (Number(bed.bedStatus) === 0 ? 'bg-slate-100 border-slate-200' : 'bg-emerald-50/60 border-emerald-200')"
               :data-testid="`icu-bed-${bed.bedNo}`">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span class="text-base font-semibold text-slate-800">{{ bed.bedNo }}</span>
                <el-tag v-if="bed.careLevel" size="small" :type="careLevelTag(bed.careLevel)">
                  {{ dictText('careLevel', bed.careLevel) }}
                </el-tag>
                <el-tag v-else-if="Number(bed.bedStatus) === 0" size="small" type="info">停用</el-tag>
                <el-tag v-else size="small" type="success">空床</el-tag>
              </div>
              <span class="text-xs text-slate-500">{{ bed.stayNo }}</span>
            </div>

            <template v-if="bed.stayId">
              <div class="text-sm text-slate-700 mb-1">
                {{ bed.patientName }}
                <span class="text-xs text-slate-500 ml-1">{{ bed.patientNo }}</span>
              </div>
              <div class="text-xs text-slate-500 mb-2">入科 {{ bed.inTime }} · GCS {{ bed.inGcs ?? '—' }}</div>
              <div class="grid grid-cols-3 gap-1 text-xs text-slate-600 mb-2">
                <span>T {{ bed.lastTemperature ?? '—' }}</span>
                <span>P {{ bed.lastPulse ?? '—' }}</span>
                <span>SpO2 {{ bed.lastSpo2 ?? '—' }}</span>
                <span>BP {{ bed.lastSbp ?? '—' }}/{{ bed.lastDbp ?? '—' }}</span>
                <span>GCS {{ bed.lastGcsTotal ?? '—' }}</span>
                <span>记录 {{ bed.monitorCount ?? 0 }} 条</span>
              </div>
              <div class="text-xs mb-2" :class="monitorLagDanger(bed) ? 'text-red-600' : 'text-slate-500'">
                记录 {{ bed.monitorCount ?? 0 }} 条 · 上次 {{ monitorLagText(bed) }}
              </div>
              <div class="flex items-center gap-1 flex-wrap">
                <el-button link type="primary" size="small" data-testid="icu-bed-monitor-btn" v-perm="'ipd:icu:edit'"
                           @click="openMonitorCreate(bed.stayId)">登记监护</el-button>
                <el-button link type="primary" size="small" data-testid="icu-bed-trend-btn"
                           @click="openTrend(bed)">趋势</el-button>
                <el-button link type="primary" size="small" data-testid="icu-bed-detail-btn"
                           @click="openStayView(bed.stayId)">详情</el-button>
                <el-button link type="warning" size="small" data-testid="icu-bed-out-btn" v-perm="'ipd:icu:add'"
                           @click="openOut({ id: bed.stayId, stayNo: bed.stayNo, patientName: bed.patientName })">出科</el-button>
              </div>
            </template>
            <template v-else>
              <div class="text-xs text-slate-400 mb-2">
                {{ Number(bed.bedStatus) === 0 ? '该床已停用，不可入科' : '空闲可用' }}
              </div>
              <el-button v-if="Number(bed.bedStatus) !== 0" link type="primary" size="small" v-perm="'ipd:icu:add'"
                         data-testid="icu-bed-free-admit-btn" @click="openAdmit(bed)">入科</el-button>
            </template>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 页签二：入出科台账 ============ -->
      <el-tab-pane label="入出科台账" name="stays">
        <div class="flex flex-wrap items-center gap-2 mb-3">
          <el-input v-model="stayQuery.stayNo" placeholder="入科单号" clearable class="w-40" data-testid="icu-st-no" />
          <el-input v-model="stayQuery.patientName" placeholder="患者姓名" clearable class="w-36" data-testid="icu-st-name" />
          <el-date-picker v-model="stayQuery.startDate" type="date" value-format="YYYY-MM-DD" placeholder="入科开始" class="w-36" />
          <el-date-picker v-model="stayQuery.endDate" type="date" value-format="YYYY-MM-DD" placeholder="入科结束" class="w-36" />
          <el-select v-model="stayQuery.careLevel" placeholder="监护等级" clearable class="w-36" :fit-input-width="false">
            <el-option v-for="d in dicts.careLevel" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
          <el-select v-model="stayQuery.status" placeholder="状态" clearable class="w-28" :fit-input-width="false">
            <el-option v-for="d in dicts.stayStatus" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
          <el-button type="primary" :icon="Search" data-testid="icu-st-search" @click="stayQuery.pageNum = 1; loadStay()">查询</el-button>
          <el-button :icon="Refresh" @click="resetStayQuery">重置</el-button>
          <el-button v-perm="'ipd:icu:add'" type="primary" :icon="Plus" data-testid="icu-st-add-btn" @click="openAdmit()">入科登记</el-button>
        </div>

        <el-table v-loading="stayLoading" :data="stayRows" border data-testid="icu-stay-table">
          <el-table-column prop="stayNo" label="入科单号" width="170" />
          <el-table-column label="患者" width="150">
            <template #default="{ row }">
              <div>{{ row.patientName }}</div>
              <div class="text-xs text-slate-500">{{ row.patientNo }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="fromDeptName" label="来源科室" width="120" show-overflow-tooltip />
          <el-table-column label="ICU 床位" width="110">
            <template #default="{ row }">{{ row.wardName || '—' }} {{ row.bedNo }}</template>
          </el-table-column>
          <el-table-column label="监护等级" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="careLevelTag(row.careLevel)">{{ dictText('careLevel', row.careLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="inTime" label="入科时间" width="160" />
          <el-table-column prop="inGcs" label="入科GCS" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="stayStatusTag(row.status)">{{ dictText('stayStatus', row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="outTime" label="出科时间" width="160">
            <template #default="{ row }">{{ row.outTime || '—' }}</template>
          </el-table-column>
          <el-table-column label="去向" width="110">
            <template #default="{ row }">{{ row.outDest ? dictText('outDest', row.outDest) : '—' }}</template>
          </el-table-column>
          <el-table-column prop="outReason" label="转归说明" min-width="160" show-overflow-tooltip />
          <el-table-column label="记录数" width="90">
            <template #default="{ row }">{{ row.monitorCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="滞留" width="90">
            <template #default="{ row }">{{ row.stayHours ?? 0 }}h</template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" data-testid="icu-st-view-btn" @click.stop="openStayView(row.id)">详情</el-button>
              <el-button link type="primary" size="small" data-testid="icu-st-edit-btn" v-if="canEditStay(row.status)" v-perm="'ipd:icu:add'"
                         @click.stop="openAdmit(null, row)">修改</el-button>
              <el-button link type="warning" size="small" data-testid="icu-st-out-btn" v-if="canOutStay(row.status)" v-perm="'ipd:icu:add'"
                         @click.stop="openOut(row)">出科</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="flex justify-end mt-3">
          <el-pagination v-model:current-page="stayQuery.pageNum" v-model:page-size="stayQuery.pageSize"
                         :page-sizes="PAGE_SIZES" :total="stayTotal"
                         layout="total, sizes, prev, pager, next" @size-change="stayQuery.pageNum = 1; loadStay()"
                         @current-change="loadStay" />
        </div>
      </el-tab-pane>

      <!-- ============ 页签三：监护记录单 ============ -->
      <el-tab-pane label="监护记录单" name="monitors">
        <div class="flex flex-wrap items-center gap-2 mb-3">
          <el-select v-model="monQuery.stayId" placeholder="选择患者" clearable filterable class="w-64" :fit-input-width="false"
                     data-testid="icu-mo-stay">
            <el-option v-for="s in inStays" :key="s.id" :label="`${s.patientName}（${s.bedNo}）${s.stayNo}`" :value="s.id" />
          </el-select>
          <el-date-picker v-model="monQuery.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" class="w-36" />
          <el-date-picker v-model="monQuery.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" class="w-36" />
          <el-button type="primary" :icon="Search" data-testid="icu-mo-search" @click="monQuery.pageNum = 1; loadMonitor()">查询</el-button>
          <el-button :icon="Refresh" @click="resetMonitorQuery">重置</el-button>
          <el-button v-perm="'ipd:icu:edit'" type="primary" :icon="Plus" data-testid="icu-mo-add-btn"
                     @click="openMonitorCreate()">登记监护记录</el-button>
        </div>

        <el-table v-loading="monLoading" :data="monRows" border max-height="620" data-testid="icu-monitor-table">
          <el-table-column prop="recordTime" label="记录时刻" width="160" />
          <el-table-column label="患者/床位" width="140">
            <template #default="{ row }">{{ row.patientName }} · {{ row.bedNo }}</template>
          </el-table-column>
          <el-table-column prop="temperature" label="T" width="70" />
          <el-table-column prop="pulse" label="P" width="60" />
          <el-table-column prop="respiratory" label="R" width="60" />
          <el-table-column label="BP" width="95">
            <template #default="{ row }">{{ bpText(row) }}</template>
          </el-table-column>
          <el-table-column prop="spo2" label="SpO2" width="70" />
          <el-table-column label="GCS" width="120">
            <template #default="{ row }">
              <span v-if="row.gcsTotal != null">{{ row.gcsTotal }}（{{ row.gcsEye }}/{{ row.gcsVerbal }}/{{ row.gcsMotor }}）</span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column prop="pupil" label="瞳孔" width="140" show-overflow-tooltip />
          <el-table-column label="呼吸支持" width="150">
            <template #default="{ row }">
              <span v-if="row.ventMode">{{ dictText('ventMode', row.ventMode) }}</span>
              <span v-else class="text-slate-400">—</span>
              <span v-if="row.fio2 != null" class="text-xs text-slate-500 ml-1">FiO2 {{ row.fio2 }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="cvp" label="CVP" width="70" />
          <el-table-column label="入/出/平衡" width="150">
            <template #default="{ row }">
              {{ row.intakeMl ?? '—' }} / {{ row.outputMl ?? '—' }} /
              <span :class="Number(row.fluidBalance) > 0 ? 'text-red-600' : 'text-slate-700'">{{ row.fluidBalance ?? '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="urineMl" label="尿量" width="80" />
          <el-table-column label="带管" width="150">
            <template #default="{ row }">{{ tubeText(row) }}</template>
          </el-table-column>
          <el-table-column prop="conditionDesc" label="病情观察" min-width="180" show-overflow-tooltip />
          <el-table-column prop="handling" label="处置" min-width="160" show-overflow-tooltip />
          <el-table-column prop="recorderName" label="记录人" width="100" />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canWriteMonitor(row.stayStatus)" link type="primary" size="small"
                         data-testid="icu-mo-edit-btn" v-perm="'ipd:icu:edit'" @click.stop="openMonitorEdit(row)">修改</el-button>
              <span v-else class="text-xs text-slate-400">已封账</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="flex justify-end mt-3">
          <el-pagination v-model:current-page="monQuery.pageNum" v-model:page-size="monQuery.pageSize"
                         :page-sizes="PAGE_SIZES" :total="monTotal"
                         layout="total, sizes, prev, pager, next" @size-change="monQuery.pageNum = 1; loadMonitor()"
                         @current-change="loadMonitor" />
        </div>
      </el-tab-pane>

      <!-- ============ 页签四：科室指标 ============ -->
      <el-tab-pane label="科室指标" name="stats">
        <div class="flex flex-wrap items-center gap-2 mb-3">
          <el-date-picker v-model="stRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至"
                          start-placeholder="开始日期" end-placeholder="结束日期" class="w-64" data-testid="icu-stats-range" />
          <el-input v-model="stLagHours" placeholder="漏记阈值(小时)" class="w-40" data-testid="icu-stats-lag" />
          <el-button type="primary" :icon="Search" data-testid="icu-stats-search" @click="loadStats">查询</el-button>
          <span class="text-xs text-slate-500">统计窗口最长 30 天，超出按结束日往前推</span>
        </div>

        <div v-loading="statsLoading" v-if="stats" class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4" data-testid="icu-stats-cards">
          <div v-for="c in statCards" :key="c.key" class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-xs text-slate-500 mb-1">{{ c.label }}</div>
            <div class="text-xl font-semibold" :class="c.danger ? 'text-red-600' : 'text-slate-800'"
                 :data-testid="`icu-stat-${c.key}`">{{ c.value }}</div>
          </div>
        </div>

        <div v-if="stats" class="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-sm font-medium text-slate-700 mb-2">监护等级分布（在科）</div>
            <el-table :data="stats.careLevels || []" border size="small" max-height="240" data-testid="icu-stats-carelevel">
              <el-table-column label="等级" min-width="120">
                <template #default="{ row }">{{ dictText('careLevel', row.type) }}</template>
              </el-table-column>
              <el-table-column prop="count" label="人数" width="90" />
            </el-table>
          </div>
          <div class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-sm font-medium text-slate-700 mb-2">呼吸支持方式分布（在科最近一条）</div>
            <el-table :data="stats.ventModes || []" border size="small" max-height="240" data-testid="icu-stats-vent">
              <el-table-column label="方式" min-width="140">
                <template #default="{ row }">{{ dictText('ventMode', row.type) }}</template>
              </el-table-column>
              <el-table-column prop="count" label="人数" width="90" />
            </el-table>
          </div>
          <div class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-sm font-medium text-slate-700 mb-2">现带管人数（在科）</div>
            <div class="grid grid-cols-2 gap-2" data-testid="icu-stats-tubes">
              <div v-for="t in tubeCards" :key="t.label" class="rounded border border-slate-200 p-2">
                <div class="text-xs text-slate-500">{{ t.label }}</div>
                <div class="text-lg font-semibold text-slate-800">{{ t.value }}</div>
              </div>
            </div>
            <div class="text-xs text-slate-500 mt-2">区间：{{ stats.startDate }} ~ {{ stats.endDate }}</div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 入科弹框 ============ -->
    <el-dialog v-model="admitDialog" :title="admitTitle" width="620px" data-testid="icu-admit-dialog">
      <el-form :model="admitForm" label-width="100px">
        <el-form-item label="住院患者" required>
          <el-select v-model="admitForm.admissionId" placeholder="输入姓名/住院号搜索在院患者" filterable remote
                     :remote-method="searchAdmission" :loading="admissionLoading" class="w-full" :fit-input-width="false"
                     :disabled="!!admitForm.id" data-testid="icu-admit-patient">
            <el-option v-for="a in admissionOptions" :key="a.admissionId"
                       :label="`${a.patientName}（${a.patientNo}）${a.deptName || ''} ${a.bedNo || ''}`"
                       :value="a.admissionId" />
          </el-select>
        </el-form-item>
        <el-form-item label="ICU 床位" required>
          <el-select v-model="admitForm.bedId" placeholder="选择空闲 ICU 床" class="w-52" :fit-input-width="false"
                     :disabled="!!admitForm.id" data-testid="icu-admit-bed">
            <el-option v-for="b in freeBeds" :key="b.bedId" :label="b.bedNo" :value="b.bedId" />
            <el-option v-if="admitForm.id && admitForm.bedId" :key="`cur-${admitForm.bedId}`"
                       :label="`${admitForm.bedNo}（在科中不可换床，需换床请先出科）`" :value="admitForm.bedId" />
          </el-select>
          <span class="text-xs text-slate-500 ml-2">一次住院同时仅一条在科记录，一床同时只允许一名患者</span>
        </el-form-item>
        <el-form-item label="监护等级" required>
          <el-select v-model="admitForm.careLevel" class="w-40" :fit-input-width="false" data-testid="icu-admit-care">
            <el-option v-for="d in dicts.careLevel" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="入科时间" required>
          <el-date-picker v-model="admitForm.inTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                          placeholder="不得晚于当前、不早于入院" class="w-52" :clearable="false" data-testid="icu-admit-time" />
        </el-form-item>
        <el-form-item label="入科 GCS">
          <el-input v-model="admitForm.inGcs" placeholder="3~15，可空" class="w-32" data-testid="icu-admit-gcs" />
        </el-form-item>
        <el-form-item label="入科诊断">
          <el-input v-model="admitForm.inDiag" placeholder="如 重症肺炎、感染性休克" data-testid="icu-admit-diag" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="admitForm.remark" data-testid="icu-admit-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="admitDialog = false">取消</el-button>
        <el-button type="primary" :loading="admitSaving" data-testid="icu-admit-ok" @click="saveAdmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 出科弹框 ============ -->
    <el-dialog v-model="outDialog" :title="`出科登记 · ${outForm.patientName}`" width="560px" data-testid="icu-out-dialog">
      <el-form :model="outForm" label-width="100px">
        <el-form-item label="入科单号">
          <span class="text-sm text-slate-600">{{ outForm.stayNo }}</span>
        </el-form-item>
        <el-form-item label="出科时间" required>
          <el-date-picker v-model="outForm.outTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-52"
                          :clearable="false" data-testid="icu-out-time" />
        </el-form-item>
        <el-form-item label="转出去向" required>
          <el-select v-model="outForm.outDest" placeholder="选择去向" class="w-44" :fit-input-width="false"
                     data-testid="icu-out-dest">
            <el-option v-for="d in dicts.outDest" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="转归说明" :required="outNeedsReason(outForm.outDest)">
          <el-input v-model="outForm.outReason" type="textarea" :rows="2"
                    :placeholder="outNeedsReason(outForm.outDest) ? '转院/死亡/自动离院必填' : '可空'"
                    data-testid="icu-out-reason" />
        </el-form-item>
        <el-form-item label="出科 GCS">
          <el-input v-model="outForm.outGcs" placeholder="3~15，可空" class="w-32" data-testid="icu-out-gcs" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outDialog = false">取消</el-button>
        <el-button type="primary" :loading="outSaving" data-testid="icu-out-ok" @click="saveOut">确认出科</el-button>
      </template>
    </el-dialog>

    <!-- ============ 入科详情（只读） ============ -->
    <el-dialog v-model="stayDialog" title="入科记录详情" width="720px" data-testid="icu-stay-dialog">
      <el-form v-if="stayDetail" :model="stayDetail" label-width="100px" disabled>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="入科单号"><el-input :model-value="stayDetail.stayNo" /></el-form-item>
          <el-form-item label="状态">
            <el-tag size="small" :type="stayStatusTag(stayDetail.status)">{{ dictText('stayStatus', stayDetail.status) }}</el-tag>
          </el-form-item>
          <el-form-item label="患者"><el-input :model-value="`${stayDetail.patientName}（${stayDetail.patientNo}）`" /></el-form-item>
          <el-form-item label="来源科室"><el-input :model-value="stayDetail.fromDeptName || '—'" /></el-form-item>
          <el-form-item label="ICU 床位"><el-input :model-value="`${stayDetail.wardName || ''} ${stayDetail.bedNo}`" /></el-form-item>
          <el-form-item label="监护等级"><el-input :model-value="dictText('careLevel', stayDetail.careLevel)" /></el-form-item>
          <el-form-item label="入科时间"><el-input :model-value="stayDetail.inTime" /></el-form-item>
          <el-form-item label="入科登记人"><el-input :model-value="stayDetail.inBy || '—'" /></el-form-item>
          <el-form-item label="入科 GCS"><el-input :model-value="String(stayDetail.inGcs ?? '—')" /></el-form-item>
          <el-form-item label="滞留时长"><el-input :model-value="`${stayDetail.stayHours ?? 0} 小时`" /></el-form-item>
          <el-form-item label="入科诊断" class="col-span-2"><el-input :model-value="stayDetail.inDiag || '—'" /></el-form-item>
          <el-form-item label="出科时间"><el-input :model-value="stayDetail.outTime || '—'" /></el-form-item>
          <el-form-item label="转出去向"><el-input :model-value="stayDetail.outDest ? dictText('outDest', stayDetail.outDest) : '—'" /></el-form-item>
          <el-form-item label="出科 GCS"><el-input :model-value="String(stayDetail.outGcs ?? '—')" /></el-form-item>
          <el-form-item label="出科登记人"><el-input :model-value="stayDetail.outBy || '—'" /></el-form-item>
          <el-form-item label="转归说明" class="col-span-2"><el-input :model-value="stayDetail.outReason || '—'" /></el-form-item>
          <el-form-item label="监护记录"><el-input :model-value="`${stayDetail.monitorCount ?? 0} 条 · 最近 ${stayDetail.lastMonitorTime || '无'}`" /></el-form-item>
          <el-form-item label="备注"><el-input :model-value="stayDetail.remark || '—'" /></el-form-item>
        </div>
      </el-form>
      <div v-if="stayDetail" class="mb-1 text-sm font-medium text-slate-700" data-testid="icu-stay-monitors">
        监护记录（{{ stayMonitors.length }} 条）
      </div>
      <el-table v-if="stayDetail" :data="stayMonitors" border size="small" max-height="240">
        <el-table-column prop="recordTime" label="时刻" width="160" />
        <el-table-column prop="temperature" label="T" width="70" />
        <el-table-column prop="pulse" label="P" width="60" />
        <el-table-column label="BP" width="95">
          <template #default="{ row }">{{ bpText(row) }}</template>
        </el-table-column>
        <el-table-column prop="spo2" label="SpO2" width="70" />
        <el-table-column prop="gcsTotal" label="GCS" width="70" />
        <el-table-column prop="fluidBalance" label="平衡" width="90" />
        <el-table-column prop="handling" label="处置" min-width="160" show-overflow-tooltip />
        <el-table-column prop="recorderName" label="记录人" width="100" />
      </el-table>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button type="primary" @click="stayDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ============ 监护记录弹框 ============ -->
    <el-dialog v-model="monDialog" :title="monForm.id ? '修改监护记录' : '登记监护记录'" width="920px"
               data-testid="icu-monitor-dialog">
      <el-form :model="monForm" label-width="92px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="入科记录" required>
            <el-select v-model="monForm.stayId" placeholder="选择在科患者" filterable class="w-full" :fit-input-width="false"
                       :disabled="!!monForm.id" data-testid="icu-mon-stay"
                       @change="(v: any) => { monForm.patientName = inStays.find((s: any) => String(s.id) === String(v))?.patientName || '' }">
              <el-option v-for="s in inStays" :key="s.id" :label="`${s.patientName}（${s.bedNo}）${s.stayNo}`" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录时刻" required>
            <el-date-picker v-model="monForm.recordTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full"
                            :clearable="false" data-testid="icu-mon-time" />
          </el-form-item>
        </div>

        <div class="text-sm font-medium text-slate-600 mb-1 mt-2">生命体征</div>
        <div class="grid grid-cols-3 md:grid-cols-6 gap-x-3">
          <el-form-item label="体温"><el-input v-model="monForm.temperature" placeholder="℃" data-testid="icu-mon-temp" /></el-form-item>
          <el-form-item label="脉搏"><el-input v-model="monForm.pulse" placeholder="次/分" data-testid="icu-mon-pulse" /></el-form-item>
          <el-form-item label="呼吸"><el-input v-model="monForm.respiratory" placeholder="次/分" data-testid="icu-mon-rr" /></el-form-item>
          <el-form-item label="收缩压"><el-input v-model="monForm.sbp" placeholder="mmHg" data-testid="icu-mon-sbp" /></el-form-item>
          <el-form-item label="舒张压"><el-input v-model="monForm.dbp" placeholder="mmHg" data-testid="icu-mon-dbp" /></el-form-item>
          <el-form-item label="SpO2"><el-input v-model="monForm.spo2" placeholder="%，0~100" data-testid="icu-mon-spo2" /></el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="GCS">
            <div class="flex items-center gap-2">
              <el-select v-model="monForm.gcsEye" placeholder="睁眼" class="w-24" :fit-input-width="false" data-testid="icu-mon-gcs-eye">
                <el-option v-for="v in [4,3,2,1]" :key="v" :label="String(v)" :value="v" />
              </el-select>
              <el-select v-model="monForm.gcsVerbal" placeholder="语言" class="w-24" :fit-input-width="false" data-testid="icu-mon-gcs-verbal">
                <el-option v-for="v in [5,4,3,2,1]" :key="v" :label="String(v)" :value="v" />
              </el-select>
              <el-select v-model="monForm.gcsMotor" placeholder="运动" class="w-24" :fit-input-width="false" data-testid="icu-mon-gcs-motor">
                <el-option v-for="v in [6,5,4,3,2,1]" :key="v" :label="String(v)" :value="v" />
              </el-select>
              <span class="text-sm text-slate-600" data-testid="icu-mon-gcs-total">总分 {{ monGcsTotal ?? '—' }}</span>
            </div>
          </el-form-item>
          <el-form-item label="瞳孔">
            <el-input v-model="monForm.pupil" placeholder="如 双侧3mm对光灵敏" data-testid="icu-mon-pupil" />
          </el-form-item>
        </div>

        <div class="text-sm font-medium text-slate-600 mb-1 mt-2">呼吸支持与内环境</div>
        <div class="grid grid-cols-3 md:grid-cols-6 gap-x-3">
          <el-form-item label="支持方式">
            <el-select v-model="monForm.ventMode" placeholder="可空" clearable class="w-full" :fit-input-width="false"
                       data-testid="icu-mon-vent">
              <el-option v-for="d in dicts.ventMode" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="FiO2"><el-input v-model="monForm.fio2" placeholder="%，21~100" data-testid="icu-mon-fio2" /></el-form-item>
          <el-form-item label="PEEP"><el-input v-model="monForm.peep" placeholder="cmH2O" data-testid="icu-mon-peep" /></el-form-item>
          <el-form-item label="CVP"><el-input v-model="monForm.cvp" placeholder="cmH2O" data-testid="icu-mon-cvp" /></el-form-item>
        </div>

        <div class="text-sm font-medium text-slate-600 mb-1 mt-2">出入量（液体平衡服务端回算）</div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-x-3">
          <el-form-item label="入量"><el-input v-model="monForm.intakeMl" placeholder="ml" data-testid="icu-mon-intake" /></el-form-item>
          <el-form-item label="出量"><el-input v-model="monForm.outputMl" placeholder="ml" data-testid="icu-mon-output" /></el-form-item>
          <el-form-item label="尿量"><el-input v-model="monForm.urineMl" placeholder="ml" data-testid="icu-mon-urine" /></el-form-item>
          <el-form-item label="平衡">
            <span class="text-sm" :class="Number(monBalance) > 0 ? 'text-red-600' : 'text-slate-700'" data-testid="icu-mon-balance">
              {{ monBalance ?? '—' }} ml
            </span>
          </el-form-item>
        </div>

        <div class="text-sm font-medium text-slate-600 mb-1 mt-2">导管（0-无 1-有）</div>
        <div class="grid grid-cols-2 md:grid-cols-5 gap-x-3">
          <el-form-item label="人工气道"><el-switch v-model="monForm.hasAirway" :active-value="1" :inactive-value="0" data-testid="icu-mon-airway" /></el-form-item>
          <el-form-item label="中心静脉"><el-switch v-model="monForm.hasCvc" :active-value="1" :inactive-value="0" data-testid="icu-mon-cvc" /></el-form-item>
          <el-form-item label="动脉置管"><el-switch v-model="monForm.hasArterial" :active-value="1" :inactive-value="0" data-testid="icu-mon-arterial" /></el-form-item>
          <el-form-item label="导尿管"><el-switch v-model="monForm.hasCatheter" :active-value="1" :inactive-value="0" data-testid="icu-mon-catheter" /></el-form-item>
          <el-form-item label="引流管"><el-switch v-model="monForm.hasDrain" :active-value="1" :inactive-value="0" data-testid="icu-mon-drain" /></el-form-item>
        </div>

        <el-form-item label="病情观察">
          <el-input v-model="monForm.conditionDesc" type="textarea" :rows="2" data-testid="icu-mon-condition" />
        </el-form-item>
        <el-form-item label="处置">
          <el-input v-model="monForm.handling" type="textarea" :rows="2" data-testid="icu-mon-handling" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="monForm.remark" data-testid="icu-mon-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="monDialog = false">取消</el-button>
        <el-button type="primary" :loading="monSaving" data-testid="icu-mon-ok" @click="saveMonitor">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 监护趋势弹框（只读） ============ -->
    <el-dialog v-model="trendDialog" :title="trendTitle" width="1000px" data-testid="icu-trend-dialog">
      <div class="flex items-center gap-2 mb-2">
        <el-radio-group v-model="trendHours" data-testid="icu-trend-hours" @change="refreshTrend">
          <el-radio-button :value="6">6 小时</el-radio-button>
          <el-radio-button :value="24">24 小时</el-radio-button>
          <el-radio-button :value="72">72 小时</el-radio-button>
          <el-radio-button :value="0">全部</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="trendRows" border max-height="420" data-testid="icu-trend-table">
        <el-table-column prop="recordTime" label="时刻" width="160" />
        <el-table-column prop="temperature" label="T" width="70" />
        <el-table-column prop="pulse" label="P" width="60" />
        <el-table-column prop="respiratory" label="R" width="60" />
        <el-table-column label="BP" width="95">
          <template #default="{ row }">{{ bpText(row) }}</template>
        </el-table-column>
        <el-table-column prop="spo2" label="SpO2" width="70" />
        <el-table-column prop="gcsTotal" label="GCS" width="70" />
        <el-table-column label="呼吸支持" width="150">
          <template #default="{ row }">
            {{ row.ventMode ? dictText('ventMode', row.ventMode) : '—' }}
            <span v-if="row.fio2 != null" class="text-xs text-slate-500">{{ row.fio2 }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="fluidBalance" label="平衡" width="90" />
        <el-table-column prop="handling" label="处置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="recorderName" label="记录人" width="100" />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="trendDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
