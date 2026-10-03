<script setup lang="ts">
/**
 * 护理质控（sql/168，菜单 330 护理管理 / 334 护理质控）
 *
 * 这是**护理部视角**的一页，不是护士站的一页：看的是「病区 × 月」的护理质量，
 * 而不是某一份病历写得怎么样（那是病案质控 biz_record_qc_*，另一张账）。
 *
 * 四条指标分两类，分母来源完全不同，所以页面不能把它们排成同一个「合格率」列：
 * 1. BASIC_NURSING / NURSING_DOC —— 检查表评分，分母=抽查例数，事实来自检查单；
 * 2. FALL_RATE / UPPR_RATE —— 千床日率，分母=实际占用床日数，分子来自不良事件。
 *    千床日类**故意不设目标值**（要按床位类型与收治结构分级定标），所以卡片显示「无目标」
 *    而不是「未达标」，页面不许自己拍一个常数当目标线。
 *
 * 五条口径，改页面前先读完：
 * 1. **所有数字都读后端字段**。合格率、得分、趋势点、床日数统统是后端算的，
 *    前端只连线画点；哪怕只是「顺手算个平均」也会和台账对不上。
 * 2. **全院合格率 = Σ分子 / Σ分母**，不是各病区的算术平均（后端已这么算，
 *    抽查 20 例与抽查 60 例的两个病区权重不同，平均会放大小病区的问题）。
 * 3. **主表的六个汇总数由明细求和**：保存检查单只填「抽查/合格」，前端传任何
 *    汇总字段服务端都不认；已确认的单要改必须先退回草稿。
 * 4. **重算只覆盖未上报的行**。已上报 = 已写进护理部月度通报，重算 SQL 侧有闸门跳过，
 *    要改数字先「退回未上报」。页面不给「强制覆盖」的口子。
 * 5. **数据范围由后端按当前岗位收口**：病区下拉只有本人可见科室的病区；
 *    「全院合并」= 可见范围内合并，不是越权开关。
 *
 * 日期入参：月份 `YYYY-MM`、日期 `YYYY-MM-DD`，不传 ISO T 分隔（AGENTS §3）。
 */
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, Plus, Edit, RefreshLeft } from '@element-plus/icons-vue'
import {
  getQcWardSelectList, getQcInspectorSelectList, getQcItemSelectList,
  listQcCheckPage, getQcCheckDetailById, upsertQcCheck, updateQcCheckStatus, deleteQcCheckById,
  getQcMonthMetrics, getQcTrend, getQcWardCompare,
  listQcLedgerPage, recalcQcLedger, reportQcLedger, deleteQcLedgerById,
} from '@/api/nursingQc'
import { loadDictDataList, DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { hasPerm } from '@/lib/perm'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const activeTab = ref('board')
const PERM_EDIT = 'nursing:qc:edit'
const PERM_CALC = 'nursing:qc:calc'
const PERM_DELETE = 'nursing:qc:delete'

const categoryDict = ref<any[]>([])
const statusDict = ref<any[]>([])
const indicatorDict = ref<any[]>([])
const reportDict = ref<any[]>([])
const categoryText = (v: any) => dictLabelText(categoryDict.value, v)
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const reportText = (v: any) => dictLabelText(reportDict.value, v)
const toOptions = (list: any[]) => list.map((d: any) => ({ label: d.dictLabel, value: Number(d.dictValue) }))
const categoryOptions = computed(() => toOptions(categoryDict.value))
const statusOptions = computed(() => toOptions(statusDict.value))
const reportOptions = computed(() => toOptions(reportDict.value))
const indicatorOptions = computed(() => indicatorDict.value.map((d: any) => ({ label: d.dictLabel, value: d.dictValue })))

/** 展示用：后端已给 indicatorName/reachedText，这里只兜「没数」的空态 */
const fmt = (v: any, digits = 2) => (v == null || v === '' ? '—' : Number(v).toFixed(digits))
const intFmt = (v: any) => (v == null || v === '' ? '—' : String(Number(v)))

// ---------------- 本地日期（toISOString 是 UTC，北京时间 0~8 点会拿到「昨天」） ----------------
const dstr = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
const today = dstr(new Date())
const thisMonth = today.slice(0, 7)
/** 往前推 n 个月的 yyyy-MM，趋势默认框半年 */
const monthBefore = (base: string, n: number) => {
  const d = new Date(`${base}-01T00:00:00`)
  d.setMonth(d.getMonth() - n)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

// ---------------- 病区（顶部筛选，看板/趋势/对比共用） ----------------
const wards = ref<any[]>([])
// null = 全院合并（当前岗位可见范围），这是护理部要的那张汇总，不是越权开关
const wardId = ref<string | null>(null)
const statMonth = ref(thisMonth)
const wardLabel = computed(() => {
  const w = wards.value.find((x: any) => String(x.wardId) === String(wardId.value))
  return w ? w.wardName : '全院合并'
})

const loadWards = async () => {
  try {
    const res: any = await getQcWardSelectList()
    wards.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载病区失败')
  }
}

// ---------------- 看板：KPI + 趋势 + 病区对比 ----------------
const kpis = ref<any[]>([])
const boardLoading = ref(false)
const boardCode = ref('BASIC_NURSING')
const trendPoints = ref<any[]>([])
const compareRows = ref<any[]>([])
const trendStart = ref(monthBefore(thisMonth, 5))

const loadBoard = async () => {
  boardLoading.value = true
  try {
    const [m, t, c] = await Promise.all([
      getQcMonthMetrics({ statMonth: statMonth.value, wardId: wardId.value || undefined }),
      getQcTrend({ indicatorCode: boardCode.value, wardId: wardId.value || undefined, startMonth: trendStart.value, endMonth: statMonth.value }),
      getQcWardCompare({ statMonth: statMonth.value, indicatorCode: boardCode.value }),
    ])
    if (m.code !== 200) { ElMessage.error(m.message || '加载月度指标失败'); return }
    kpis.value = m.data || []
    trendPoints.value = t.code === 200 ? (t.data || []) : []
    compareRows.value = c.code === 200 ? (c.data || []) : []
  } catch (e: any) {
    ElMessage.error(e.message || '加载看板失败')
  } finally {
    boardLoading.value = false
  }
}

/** 卡片副标题：两类指标的分母不是一个东西，必须写明 */
const kpiFoot = (k: any) => (Number(k.sourceType) === 1
  ? `合格 ${intFmt(k.numerator)} / 抽查 ${intFmt(k.denominator)} 例`
  : `事件 ${intFmt(k.numerator)} 例 / 占用 ${intFmt(k.denominator)} 床日`)
/** 上色只看后端 reachedFlag：1 达标 0 未达标 null 无目标或没数，前端不比数值 */
const kpiClass = (k: any) => (Number(k.rateValue ?? -1) < 0 ? 'is-flat'
  : Number(k.reachedFlag) === 1 ? 'is-ok' : Number(k.reachedFlag) === 0 ? 'is-bad' : 'is-flat')
const kpiTagType = (k: any) => (Number(k.reachedFlag) === 1 ? 'success' : Number(k.reachedFlag) === 0 ? 'danger' : 'info')

// ---------------- 纯 SVG 趋势（不引图表库：一个折线不值得再加一个依赖） ----------------
const CH = { w: 760, h: 240, l: 56, r: 20, t: 18, b: 32 }
const chart = computed(() => {
  const pts = trendPoints.value.map((p: any) => ({
    month: p.statMonth,
    v: p.rateValue == null ? null : Number(p.rateValue),
    num: p.numerator, den: p.denominator, wardCount: p.wardCount, raw: p,
  }))
  const vals = pts.filter(p => p.v != null).map(p => p.v as number)
  if (!vals.length) return null
  const target = Number(trendPoints.value[0]?.targetValue ?? 0) || null
  let lo = Math.min(...vals, ...(target != null ? [target] : []))
  let hi = Math.max(...vals, ...(target != null ? [target] : []))
  if (lo === hi) { lo -= 1; hi += 1 }
  const pad = (hi - lo) * 0.15
  lo = Math.max(0, lo - pad)
  hi = hi + pad
  const innerW = CH.w - CH.l - CH.r
  const innerH = CH.h - CH.t - CH.b
  const xAt = (i: number) => CH.l + (pts.length === 1 ? innerW / 2 : (i * innerW) / (pts.length - 1))
  const yAt = (v: number) => CH.t + (1 - (v - lo) / (hi - lo)) * innerH
  const dots = pts.filter(p => p.v != null).map(p => ({ ...p, x: xAt(pts.indexOf(p)), y: yAt(p.v as number) }))
  // 折线跳过无数据的月份：把它们画成 0 会让「本月没住院事实」冒充一个完美的低发生率
  const line = dots.map((d, i) => `${i === 0 ? 'M' : 'L'}${d.x.toFixed(1)} ${d.y.toFixed(1)}`).join(' ')
  const area = dots.length > 1
    ? `${line} L${dots[dots.length - 1].x.toFixed(1)} ${(CH.t + innerH).toFixed(1)} L${dots[0].x.toFixed(1)} ${(CH.t + innerH).toFixed(1)} Z`
    : ''
  const ticks = [0, 1, 2, 3].map((k) => {
    const v = lo + ((hi - lo) * k) / 3
    return { v, y: yAt(v) }
  })
  return {
    pts, dots, line, area, ticks, target,
    targetY: target == null ? null : yAt(target),
    baseY: CH.t + innerH,
    xs: pts.map((p, i) => ({ x: xAt(i), label: p.month.slice(5), empty: p.v == null })),
  }
})
const chartUnit = computed(() => trendPoints.value[0]?.unit || '')
const compareMax = computed(() => Math.max(0, ...compareRows.value.map((r: any) => Number(r.rateValue ?? 0))))

// ---------------- 检查表 ----------------
const checks = ref<any[]>([])
const checkTotal = ref(0)
const checkLoading = ref(false)
const checkQuery = reactive({
  keyword: '', wardId: null as string | null, category: null as number | null, status: null as number | null,
  startMonth: '' as string, endMonth: '' as string, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
const loadChecks = async () => {
  checkLoading.value = true
  try {
    const res: any = await listQcCheckPage({ ...checkQuery })
    if (res.code !== 200) { ElMessage.error(res.message || '加载检查单失败'); return }
    checks.value = res.data?.records || []
    checkTotal.value = Number(res.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e.message || '加载检查单失败')
  } finally {
    checkLoading.value = false
  }
}
const resetCheckQuery = () => {
  checkQuery.keyword = ''
  checkQuery.wardId = null
  checkQuery.category = null
  checkQuery.status = null
  checkQuery.startMonth = ''
  checkQuery.endMonth = ''
  checkQuery.pageNum = 1
  loadChecks()
}

/**
 * 页签是 lazy 挂载的，第一次切过去组件才建立，onMounted 里那次加载轮不到它们 ——
 * 所以「检查表 / 指标台账」的数据只能在 tab-change 时拉，否则切过去是一张「共 0 条」的空表
 * （看着像接口没数据，实际是页面没发请求）。切回看板也顺手刷新，三张账永远同一时点。
 */
const onTabChange = (name: any) => {
  if (name === 'check') loadChecks()
  else if (name === 'ledger') loadLedger()
  else loadBoard()
}

/** 详情弹框（只读）：明细 + 漏查项提示 */
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getQcCheckDetailById(row.id)
    if (res.code !== 200) { ElMessage.error(res.message || '加载详情失败'); return }
    detail.value = res.data
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '加载详情失败')
  }
}

/** 录入弹框：行来源是本类别的标准目录，用户只填「抽查/合格」两个数 */
const editVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null as string | null,
  wardId: null as string | null,
  checkMonth: thisMonth,
  checkDate: today,
  category: 1 as number | null,
  inspectorId: null as string | null,
  summary: '',
  status: 1,
})
const inspectors = ref<any[]>([])
const rows = ref<any[]>([])
const catalogLoading = ref(false)

const loadInspectors = async (wid: string | null) => {
  inspectors.value = []
  if (!wid) return
  try {
    const res: any = await getQcInspectorSelectList({ wardId: wid })
    inspectors.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载检查人失败')
  }
}

/** 目录 → 表单行：已有明细按 itemId 回填，没录入的留空（空行不提交，等于本轮没查这项） */
const buildRows = async (category: number, items: any[] = []) => {
  catalogLoading.value = true
  try {
    const res: any = await getQcItemSelectList({ category })
    const byId = new Map((items || []).map((i: any) => [String(i.itemId), i]))
    rows.value = (res.data || []).map((c: any) => {
      const hit: any = byId.get(String(c.itemId))
      return {
        itemId: c.itemId,
        itemName: c.itemName,
        standard: c.standard,
        fullScore: c.fullScore,
        keyFlag: c.keyFlag,
        checkedNum: hit ? Number(hit.checkedNum) : null,
        qualifiedNum: hit ? Number(hit.qualifiedNum) : null,
        score: hit ? hit.score : null,
        qualifiedRate: hit ? hit.qualifiedRate : null,
        problem: hit?.problem || '',
        causeAnalysis: hit?.causeAnalysis || '',
        rectifyMeasure: hit?.rectifyMeasure || '',
      }
    })
  } catch (e: any) {
    ElMessage.error(e.message || '加载检查项目失败')
  } finally {
    catalogLoading.value = false
  }
}

const openCreate = async () => {
  form.id = null
  form.wardId = wardId.value || (wards.value[0] ? String(wards.value[0].wardId) : null)
  form.checkMonth = statMonth.value
  form.checkDate = today
  form.category = 1
  form.inspectorId = null
  form.summary = ''
  form.status = 1
  editVisible.value = true
  await loadInspectors(form.wardId)
  await buildRows(1)
}

const openEdit = async (row: any) => {
  try {
    const res: any = await getQcCheckDetailById(row.id)
    if (res.code !== 200) { ElMessage.error(res.message || '加载检查单失败'); return }
    const c = res.data.check
    form.id = c.id
    form.wardId = String(c.wardId)
    form.checkMonth = c.checkMonth
    form.checkDate = c.checkDate
    form.category = Number(c.category)
    form.inspectorId = c.inspectorId ? String(c.inspectorId) : null
    form.summary = c.summary || ''
    form.status = Number(c.status)
    editVisible.value = true
    await loadInspectors(form.wardId)
    await buildRows(form.category, res.data.items)
  } catch (e: any) {
    ElMessage.error(e.message || '加载检查单失败')
  }
}

const onFormWard = async (v: string | null) => {
  form.inspectorId = null
  await loadInspectors(v)
}
const onFormCategory = async (v: number) => {
  await buildRows(v)
}

const filledRows = computed(() => rows.value.filter((r: any) => r.checkedNum != null && r.qualifiedNum != null))

const save = async () => {
  if (!form.wardId || !form.category) { ElMessage.warning('请选择病区与检查类别'); return }
  if (!form.checkMonth) { ElMessage.warning('请选择检查月份'); return }
  if (!form.checkDate) { ElMessage.warning('请选择现场检查日期'); return }
  if (!filledRows.value.length) { ElMessage.warning('请至少录入一项抽查结果'); return }
  saving.value = true
  try {
    const res: any = await upsertQcCheck({
      // 雪花 id 全程保字符串：转 Number 会丢精度（wardId 变成错误的另一个数 → 后端按错 id 查病区 →
      // 「病区不存在或已停用」）。DTO 侧是 Long，Jackson 收 JSON 字符串照样能解析。
      wardId: form.wardId,
      checkMonth: form.checkMonth,
      checkDate: form.checkDate,
      category: form.category,
      inspectorId: form.inspectorId || undefined,
      summary: form.summary.trim() || undefined,
      items: filledRows.value.map((r: any) => ({
        itemId: String(r.itemId),
        checkedNum: Number(r.checkedNum),
        qualifiedNum: Number(r.qualifiedNum),
        problem: r.problem || undefined,
        causeAnalysis: r.causeAnalysis || undefined,
        rectifyMeasure: r.rectifyMeasure || undefined,
      })),
    })
    if (res.code === 200) {
      // 汇总数字用服务端回写的，不拿前端输入再算一遍
      ElMessage.success(res.message || '保存成功')
      editVisible.value = false
      await loadChecks()
      if (activeTab.value === 'board') loadBoard()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const changeStatus = async (row: any, status: number) => {
  const label = status === 2 ? '确认' : '退回草稿'
  try {
    await ElMessageBox.confirm(`确认${label}检查单 ${row.checkNo}？${status === 2 ? '确认后明细冻结，要改得先退回。' : ''}`, `${label}确认`, { type: 'warning' })
  } catch { return }
  try {
    // id 保字符串：转 Number 丢雪花精度，按错 id 更新等于没更新（与 save 同一条铁律）
    const res: any = await updateQcCheckStatus({ id: row.id, status })
    if (res.code === 200) { ElMessage.success(res.message || `${label}成功`); await loadChecks() } else { ElMessage.error(res.message || `${label}失败`) }
  } catch (e: any) {
    ElMessage.error(e.message || `${label}失败`)
  }
}

const removeCheck = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除检查单 ${row.checkNo}？明细一并物理删除。`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    const res: any = await deleteQcCheckById(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); await loadChecks(); if (activeTab.value === 'board') loadBoard() } else { ElMessage.error(res.message || '删除失败') }
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ---------------- 指标台账 ----------------
const ledger = ref<any[]>([])
const ledgerTotal = ref(0)
const ledgerLoading = ref(false)
const ledgerQuery = reactive({
  keyword: '', wardId: null as string | null, indicatorCode: 'BASIC_NURSING' as string,
  reportStatus: null as number | null, statMonth: thisMonth, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
const loadLedger = async () => {
  ledgerLoading.value = true
  try {
    const res: any = await listQcLedgerPage({ ...ledgerQuery })
    if (res.code !== 200) { ElMessage.error(res.message || '加载台账失败'); return }
    ledger.value = res.data?.records || []
    ledgerTotal.value = Number(res.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e.message || '加载台账失败')
  } finally {
    ledgerLoading.value = false
  }
}
const calcBusy = ref(false)
const doRecalc = async () => {
  calcBusy.value = true
  try {
    const res: any = await recalcQcLedger({ statMonth: ledgerQuery.statMonth, wardId: ledgerQuery.wardId || undefined })
    if (res.code === 200) { ElMessage.success(res.message || '重算完成'); await loadLedger(); if (activeTab.value === 'board') loadBoard() } else { ElMessage.error(res.message || '重算失败') }
  } catch (e: any) {
    ElMessage.error(e.message || '重算失败')
  } finally {
    calcBusy.value = false
  }
}
const doReport = async (reportStatus: number) => {
  const label = reportStatus === 2 ? '上报锁定' : '退回未上报'
  const tip = reportStatus === 2
    ? `确认上报 ${ledgerQuery.statMonth} 的台账？上报后这些行不再被重算覆盖。`
    : `确认退回 ${ledgerQuery.statMonth} 的台账？退回后重算才会覆盖这些行。`
  try {
    await ElMessageBox.confirm(tip, `${label}确认`, { type: 'warning' })
  } catch { return }
  calcBusy.value = true
  try {
    const res: any = await reportQcLedger({ statMonth: ledgerQuery.statMonth, wardId: ledgerQuery.wardId || undefined, reportStatus })
    if (res.code === 200) { ElMessage.success(res.message || `${label}成功`); await loadLedger() } else { ElMessage.error(res.message || `${label}失败`) }
  } catch (e: any) {
    ElMessage.error(e.message || `${label}失败`)
  } finally {
    calcBusy.value = false
  }
}
const removeLedger = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除台账行（${row.wardName} / ${row.statMonth} / ${row.indicatorName}）？台账是重算出来的结果账，删掉再重算即可。`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    const res: any = await deleteQcLedgerById(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); await loadLedger(); if (activeTab.value === 'board') loadBoard() } else { ElMessage.error(res.message || '删除失败') }
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ---------------- 表格高度：撑满视口，表体内部滚动，分页钉在卡片底 ----------------
const tableHeight = ref(420)
const calcHeight = () => { tableHeight.value = Math.max(240, window.innerHeight - (activeTab.value === 'board' ? 300 : 300)) }

onMounted(async () => {
  const [c, s, i, r] = await Promise.all([
    loadDictDataList(DICT_TYPE.NURSING_QC_CATEGORY),
    loadDictDataList(DICT_TYPE.NURSING_QC_STATUS),
    loadDictDataList(DICT_TYPE.NURSING_INDICATOR),
    loadDictDataList(DICT_TYPE.NURSING_QC_REPORT),
  ])
  categoryDict.value = c
  statusDict.value = s
  indicatorDict.value = i
  reportDict.value = r
  calcHeight()
  window.addEventListener('resize', calcHeight)
  await loadWards()
  await loadBoard()
})
onUnmounted(() => window.removeEventListener('resize', calcHeight))
</script>

<template>
  <div class="w-full">
    <!-- 工具条：病区（含「全院合并」= 可见范围汇总）+ 统计月份 -->
    <div class="mb-3 flex flex-wrap items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm filter-row">
      <el-select v-model="wardId" placeholder="全院合并（可见范围）" filterable clearable class="!w-60"
                 data-testid="qc-ward" :fit-input-width="false">
        <el-option v-for="w in wards" :key="w.wardId" :label="`${w.wardName}（${w.totalBeds ?? '—'} 床）`" :value="String(w.wardId)" />
      </el-select>
      <el-date-picker v-model="statMonth" type="month" placeholder="统计月份" value-format="YYYY-MM"
                      class="!w-40" :clearable="false" data-testid="qc-month" />
      <el-button type="primary" :icon="Refresh" :loading="boardLoading" data-testid="qc-reload" @click="loadBoard">刷新看板</el-button>
      <span class="ml-auto text-sm text-slate-500" data-testid="qc-scope">{{ statMonth }} · {{ wardLabel }}</span>
    </div>

    <el-tabs v-model="activeTab" class="qc-tabs" @tab-change="onTabChange">
      <!-- ============ 质量看板 ============ -->
      <el-tab-pane label="质量看板" name="board">
        <div v-loading="boardLoading">
          <!-- 四条指标卡片：数字、达标、分子分母全部来自后端 -->
          <div class="mb-3 grid grid-cols-2 gap-3 xl:grid-cols-4" data-testid="qc-kpis">
            <div v-for="k in kpis" :key="k.indicatorCode" class="kpi-card rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
                 :class="kpiClass(k)" :data-testid="`qc-kpi-${k.indicatorCode}`">
              <div class="flex items-start justify-between gap-2">
                <span class="text-sm text-slate-600">{{ k.indicatorName }}</span>
                <el-tag :type="kpiTagType(k)" size="small" effect="plain">{{ k.reachedText }}</el-tag>
              </div>
              <p class="mt-2 flex items-baseline gap-1">
                <span class="text-3xl font-semibold" :class="kpiClass(k) === 'is-bad' ? 'text-rose-700' : kpiClass(k) === 'is-ok' ? 'text-emerald-700' : 'text-slate-400'">
                  {{ fmt(k.rateValue) }}
                </span>
                <span class="text-sm text-slate-500">{{ k.unit }}</span>
              </p>
              <p class="mt-1 text-xs text-slate-500" :data-testid="`qc-kpi-foot-${k.indicatorCode}`">{{ kpiFoot(k) }}</p>
              <p class="mt-1 text-xs text-slate-400">
                目标 {{ k.targetValue == null ? '—' : `${fmt(k.targetValue)}${k.unit}` }}
                · {{ k.wardCount }} 个病区
                <template v-if="Number(k.notReachedCount) > 0">
                  · <span class="text-rose-600">{{ k.notReachedCount }} 个未达标</span>
                </template>
              </p>
              <p class="mt-1 text-xs text-slate-400">
                已上报 {{ k.reportedCount ?? 0 }} 行 / 未上报 {{ k.unreportedCount ?? 0 }} 行
                <span v-if="!Number(k.wardCount)" class="text-amber-600">（本月未重算）</span>
              </p>
            </div>
          </div>

          <div class="grid grid-cols-1 gap-3 2xl:grid-cols-2">
            <!-- 趋势 -->
            <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div class="mb-2 flex flex-wrap items-center gap-3">
                <span class="font-medium text-slate-700">指标趋势</span>
                <el-select v-model="boardCode" class="!w-52" data-testid="qc-chart-code" :fit-input-width="false"
                           @change="loadBoard">
                  <el-option v-for="o in indicatorOptions" :key="o.value" :label="o.label" :value="o.value" />
                </el-select>
                <el-date-picker v-model="trendStart" type="month" placeholder="起始月" value-format="YYYY-MM"
                                class="!w-36" :clearable="false" @change="loadBoard" />
                <span class="text-xs text-slate-400">
                  {{ Number(kpis.find((k: any) => k.indicatorCode === boardCode)?.higherIsBetter) ? '越高越好' : '越低越好' }}
                </span>
              </div>
              <svg v-if="chart" :viewBox="`0 0 ${CH.w} ${CH.h}`" class="w-full" data-testid="qc-trend-svg">
                <line v-for="t in chart.ticks" :key="t.y" :x1="CH.l" :x2="CH.w - CH.r" :y1="t.y" :y2="t.y"
                      stroke="#e2e8f0" stroke-width="1" />
                <text v-for="t in chart.ticks" :key="`lb-${t.y}`" :x="CH.l - 8" :y="t.y + 4" text-anchor="end"
                      font-size="11" fill="#94a3b8">{{ t.v.toFixed(1) }}</text>
                <line v-if="chart.targetY != null" :x1="CH.l" :x2="CH.w - CH.r" :y1="chart.targetY" :y2="chart.targetY"
                      stroke="#0E9488" stroke-width="1.5" stroke-dasharray="5 4" data-testid="qc-target-line" />
                <text v-if="chart.targetY != null" :x="CH.w - CH.r" :y="chart.targetY - 5" text-anchor="end"
                      font-size="11" fill="#0E9488">目标 {{ fmt(chart.target) }}{{ chartUnit }}</text>
                <path v-if="chart.area" :d="chart.area" fill="#1269B5" fill-opacity="0.08" />
                <path :d="chart.line" fill="none" stroke="#1269B5" stroke-width="2" data-testid="qc-trend-line" />
                <circle v-for="d in chart.dots" :key="d.month" :cx="d.x" :cy="d.y" r="4" fill="#fff"
                        stroke="#1269B5" stroke-width="2" data-testid="qc-trend-dot">
                  <title>{{ d.month }}：{{ d.v }}{{ chartUnit }}（{{ d.num }} / {{ d.den }}，{{ d.wardCount }} 个病区）</title>
                </circle>
                <text v-for="d in chart.dots" :key="`v-${d.month}`" :x="d.x" :y="d.y - 9" text-anchor="middle"
                      font-size="11" fill="#1269B5">{{ d.v }}</text>
                <line :x1="CH.l" :x2="CH.w - CH.r" :y1="chart.baseY" :y2="chart.baseY" stroke="#cbd5e1" />
                <text v-for="x in chart.xs" :key="x.label" :x="x.x" :y="CH.h - 10" text-anchor="middle"
                      font-size="11" :fill="x.empty ? '#cbd5e1' : '#64748b'">{{ x.label }}</text>
              </svg>
              <div v-else class="py-16 text-center text-sm text-slate-400" data-testid="qc-trend-empty">
                该指标在所选区间内没有已重算的月份
              </div>
            </div>

            <!-- 病区对比 -->
            <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div class="mb-2 flex flex-wrap items-center gap-3">
                <span class="font-medium text-slate-700">病区对比</span>
                <span class="text-xs text-slate-400">{{ statMonth }} · {{ (compareRows[0] || {}).indicatorName }}</span>
                <span class="text-xs text-slate-400">后端按指标值倒序，发生率类榜首即最需要关注的病区</span>
              </div>
              <div v-if="compareRows.length" class="space-y-2" data-testid="qc-compare">
                <div v-for="r in compareRows" :key="r.wardId" class="compare-row" data-testid="qc-compare-row">
                  <span class="w-32 shrink-0 truncate text-sm text-slate-700" :title="r.wardName">{{ r.wardName }}</span>
                  <span class="bar-track">
                    <span class="bar-fill" :class="Number(r.reachedFlag) === 0 ? 'is-bad' : 'is-ok'"
                          :style="{ width: compareMax ? `${Math.max(2, Number(r.rateValue ?? 0) / compareMax * 100)}%` : '2%' }" />
                  </span>
                  <span class="w-24 shrink-0 text-right text-sm font-medium"
                        :class="Number(r.reachedFlag) === 0 ? 'text-rose-700' : 'text-slate-700'">
                    {{ fmt(r.rateValue) }}{{ r.unit }}
                  </span>
                  <span class="w-20 shrink-0 text-right text-xs text-slate-400">{{ intFmt(r.numerator) }}/{{ intFmt(r.denominator) }}</span>
                  <el-tag :type="Number(r.reachedFlag) === 1 ? 'success' : Number(r.reachedFlag) === 0 ? 'danger' : 'info'"
                          size="small" effect="plain" class="shrink-0">{{ r.reachedText }}</el-tag>
                </div>
              </div>
              <div v-else class="py-16 text-center text-sm text-slate-400" data-testid="qc-compare-empty">
                {{ statMonth }} 还没有该指标的台账，请到「指标台账」页签重算
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 检查表 ============ -->
      <el-tab-pane label="检查表（评分）" name="check" lazy>
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-input v-model="checkQuery.keyword" placeholder="单号/病区/检查人" clearable class="!w-52" data-testid="qc-ck-keyword" />
            <el-select v-model="checkQuery.wardId" placeholder="全部病区" filterable clearable class="!w-52" :fit-input-width="false">
              <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)" />
            </el-select>
            <el-select v-model="checkQuery.category" placeholder="检查类别" clearable class="!w-40" :fit-input-width="false">
              <el-option v-for="o in categoryOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="checkQuery.status" placeholder="状态" clearable class="!w-32" :fit-input-width="false">
              <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-date-picker v-model="checkQuery.startMonth" type="month" placeholder="起始月" value-format="YYYY-MM" class="!w-36" />
            <el-date-picker v-model="checkQuery.endMonth" type="month" placeholder="结束月" value-format="YYYY-MM" class="!w-36" />
            <el-button type="primary" :icon="Search" :loading="checkLoading" data-testid="qc-ck-search"
                       @click="() => { checkQuery.pageNum = 1; loadChecks() }">查询</el-button>
            <el-button @click="resetCheckQuery">重置</el-button>
            <el-button v-perm="PERM_EDIT" type="success" :icon="Plus" data-testid="qc-ck-add" @click="openCreate">新增检查单</el-button>
          </div>

          <el-table :data="checks" v-loading="checkLoading" border size="small" :height="tableHeight"
                    data-testid="qc-ck-table" @row-click="openDetail">
            <el-table-column prop="checkNo" label="单号" width="150" />
            <el-table-column prop="wardName" label="病区" width="130" show-overflow-tooltip />
            <el-table-column prop="checkMonth" label="月份" width="90" align="center" />
            <el-table-column label="类别" width="100" align="center">
              <template #default="{ row }">{{ row.categoryName || categoryText(row.category) }}</template>
            </el-table-column>
            <el-table-column prop="checkDate" label="检查日期" width="110" align="center" />
            <el-table-column prop="inspectorName" label="检查人" width="100" />
            <el-table-column label="抽查/合格" width="110" align="center">
              <template #default="{ row }">{{ row.qualifiedCount }} / {{ row.sampleCount }}</template>
            </el-table-column>
            <el-table-column label="合格率" width="90" align="right">
              <template #default="{ row }">{{ fmt(row.qualifiedRate) }}%</template>
            </el-table-column>
            <el-table-column label="得分" width="120" align="right">
              <template #default="{ row }">
                {{ fmt(row.totalScore, 1) }} / {{ fmt(row.fullScore, 1) }}
                <span class="text-xs text-slate-400">（{{ fmt(row.scoreRate) }}%）</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 2 ? 'success' : 'info'" size="small" effect="plain">
                  {{ row.statusText || statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="summary" label="小结" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button v-perm="PERM_EDIT" link type="primary" size="small" data-testid="qc-ck-edit"
                           :disabled="Number(row.status) === 2" @click.stop="openEdit(row)">编辑</el-button>
                <el-button v-if="Number(row.status) !== 2" v-perm="PERM_EDIT" link type="success" size="small"
                           data-testid="qc-ck-confirm" @click.stop="changeStatus(row, 2)">确认</el-button>
                <el-button v-else v-perm="PERM_EDIT" link type="warning" size="small" data-testid="qc-ck-back"
                           @click.stop="changeStatus(row, 1)">退回</el-button>
                <el-button v-perm="PERM_DELETE" link type="danger" size="small" data-testid="qc-ck-del"
                           @click.stop="removeCheck(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination class="mt-3 justify-end" background layout="total, prev, pager, next, sizes, jumper"
                         :total="checkTotal" :current-page="checkQuery.pageNum" :page-size="checkQuery.pageSize" :page-sizes="PAGE_SIZES"
                         @current-change="(v: number) => { checkQuery.pageNum = v; loadChecks() }"
                         @size-change="(v: number) => { checkQuery.pageSize = v; checkQuery.pageNum = 1; loadChecks() }" />
        </div>
      </el-tab-pane>

      <!-- ============ 指标台账 ============ -->
      <el-tab-pane label="指标台账" name="ledger" lazy>
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-date-picker v-model="ledgerQuery.statMonth" type="month" placeholder="统计月份" value-format="YYYY-MM"
                            class="!w-40" :clearable="false" data-testid="qc-lg-month" @change="() => { ledgerQuery.pageNum = 1; loadLedger() }" />
            <el-select v-model="ledgerQuery.indicatorCode" placeholder="全部指标" clearable class="!w-52" :fit-input-width="false"
                       data-testid="qc-lg-code" @change="() => { ledgerQuery.pageNum = 1; loadLedger() }">
              <el-option v-for="o in indicatorOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="ledgerQuery.wardId" placeholder="全部病区" filterable clearable class="!w-48"
                       data-testid="qc-lg-ward" :fit-input-width="false"
                       @change="() => { ledgerQuery.pageNum = 1; loadLedger() }">
              <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)" />
            </el-select>
            <el-select v-model="ledgerQuery.reportStatus" placeholder="上报状态" clearable class="!w-32" :fit-input-width="false"
                       @change="() => { ledgerQuery.pageNum = 1; loadLedger() }">
              <el-option v-for="o in reportOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-input v-model="ledgerQuery.keyword" placeholder="病区/指标名" clearable class="!w-44" />
            <el-button type="primary" :icon="Search" :loading="ledgerLoading" @click="() => { ledgerQuery.pageNum = 1; loadLedger() }">查询</el-button>
            <el-button v-perm="PERM_CALC" :icon="Refresh" :loading="calcBusy" data-testid="qc-lg-recalc" @click="doRecalc">重算该月</el-button>
            <el-button v-perm="PERM_CALC" type="success" :loading="calcBusy" data-testid="qc-lg-report" @click="doReport(2)">上报锁定</el-button>
            <el-button v-perm="PERM_CALC" type="warning" :loading="calcBusy" data-testid="qc-lg-unreport" @click="doReport(1)">退回未上报</el-button>
            <span class="ml-auto text-xs text-slate-400">已上报的行重算不会覆盖；要改先退回</span>
          </div>

          <el-table :data="ledger" v-loading="ledgerLoading" border size="small" :height="tableHeight" data-testid="qc-lg-table">
            <el-table-column prop="statMonth" label="月份" width="90" align="center" />
            <el-table-column prop="wardName" label="病区" width="130" show-overflow-tooltip />
            <el-table-column prop="indicatorName" label="指标" width="180" show-overflow-tooltip />
            <el-table-column label="分子/分母" width="120" align="center">
              <template #default="{ row }">{{ intFmt(row.numerator) }} / {{ intFmt(row.denominator) }}</template>
            </el-table-column>
            <el-table-column label="指标值" width="110" align="right">
              <template #default="{ row }">
                <span :class="Number(row.reachedFlag) === 0 ? 'text-rose-700 font-medium' : ''">{{ fmt(row.rateValue) }}{{ row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column label="目标" width="90" align="right">
              <template #default="{ row }">{{ row.targetValue == null ? '—' : `${fmt(row.targetValue)}${row.unit}` }}</template>
            </el-table-column>
            <el-table-column label="达标" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="Number(row.reachedFlag) === 1 ? 'success' : Number(row.reachedFlag) === 0 ? 'danger' : 'info'"
                        size="small" effect="plain">{{ row.reachedText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="来源" width="90" align="center">
              <template #default="{ row }">{{ Number(row.sourceType) === 1 ? '检查表' : '事件+床日' }}</template>
            </el-table-column>
            <el-table-column label="上报" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="Number(row.reportStatus) === 2 ? 'success' : 'info'" size="small" effect="plain">
                  {{ row.reportStatusText || reportText(row.reportStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="calcTime" label="重算时间" width="150" align="center" />
            <el-table-column prop="remark" label="口径备注" min-width="240" show-overflow-tooltip />
            <el-table-column label="操作" width="80" fixed="right" align="center">
              <template #default="{ row }">
                <el-button v-perm="PERM_DELETE" link type="danger" size="small" data-testid="qc-lg-del"
                           @click.stop="removeLedger(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination class="mt-3 justify-end" background layout="total, prev, pager, next, sizes, jumper"
                         :total="ledgerTotal" :current-page="ledgerQuery.pageNum" :page-size="ledgerQuery.pageSize" :page-sizes="PAGE_SIZES"
                         @current-change="(v: number) => { ledgerQuery.pageNum = v; loadLedger() }"
                         @size-change="(v: number) => { ledgerQuery.pageSize = v; ledgerQuery.pageNum = 1; loadLedger() }" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 检查单详情（只读：行点击进来，改单走列表上的编辑按钮） ============ -->
    <el-dialog v-model="detailVisible" title="检查单详情" width="1080px" data-testid="qc-detail-dialog">
      <el-descriptions v-if="detail" :column="4" border size="small">
        <el-descriptions-item label="单号">{{ detail.check.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="病区">{{ detail.check.wardName }}</el-descriptions-item>
        <el-descriptions-item label="月份 / 日期">{{ detail.check.checkMonth }} · {{ detail.check.checkDate }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ detail.check.categoryName || categoryText(detail.check.category) }}</el-descriptions-item>
        <el-descriptions-item label="检查人">{{ detail.check.inspectorName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="抽查/合格">{{ detail.check.qualifiedCount }} / {{ detail.check.sampleCount }}</el-descriptions-item>
        <el-descriptions-item label="合格率">{{ fmt(detail.check.qualifiedRate) }}%</el-descriptions-item>
        <el-descriptions-item label="得分">{{ fmt(detail.check.totalScore, 1) }} / {{ fmt(detail.check.fullScore, 1) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.check.statusText || statusText(detail.check.status) }}</el-descriptions-item>
        <el-descriptions-item label="录入时间">{{ detail.check.createTime }}</el-descriptions-item>
        <el-descriptions-item label="小结" :span="2">{{ detail.check.summary || '—' }}</el-descriptions-item>
      </el-descriptions>
      <p v-if="detail && Number(detail.missingItemCount) > 0" class="mt-2 text-sm text-amber-700" data-testid="qc-missing">
        本类别共 {{ detail.catalog?.length }} 项，本轮还有 {{ detail.missingItemCount }} 项未录入，合格率只按已查项统计
      </p>
      <el-table v-if="detail" :data="detail.items" border size="small" max-height="360" class="mt-2" data-testid="qc-detail-items">
        <el-table-column prop="itemName" label="检查项目" width="200" show-overflow-tooltip />
        <el-table-column label="抽查" width="70" align="center" prop="checkedNum" />
        <el-table-column label="合格" width="70" align="center" prop="qualifiedNum" />
        <el-table-column label="单项合格率" width="110" align="right">
          <template #default="{ row }">{{ fmt(row.qualifiedRate) }}%</template>
        </el-table-column>
        <el-table-column label="得分" width="100" align="right">
          <template #default="{ row }">{{ fmt(row.score, 1) }} / {{ fmt(row.fullScore, 1) }}</template>
        </el-table-column>
        <el-table-column prop="problem" label="存在问题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="causeAnalysis" label="原因分析" min-width="160" show-overflow-tooltip />
        <el-table-column prop="rectifyMeasure" label="整改措施" min-width="160" show-overflow-tooltip />
      </el-table>
      <template #footer><el-button type="primary" @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <!-- ============ 检查单录入 ============ -->
    <el-dialog v-model="editVisible" :title="form.id ? '修改检查单' : '新增检查单'" width="1180px" data-testid="qc-edit-dialog">
      <div class="mb-3 grid grid-cols-2 gap-3 xl:grid-cols-3">
        <el-select v-model="form.wardId" placeholder="选择病区" filterable class="w-full" data-testid="qc-f-ward"
                   :fit-input-width="false" :disabled="!!form.id" @change="onFormWard">
          <el-option v-for="w in wards" :key="w.wardId" :label="`${w.wardName}（${w.totalBeds ?? '—'} 床）`" :value="String(w.wardId)" />
        </el-select>
        <el-date-picker v-model="form.checkMonth" type="month" placeholder="检查月份" value-format="YYYY-MM"
                        class="!w-full" :clearable="false" :disabled="!!form.id" data-testid="qc-f-month" />
        <el-date-picker v-model="form.checkDate" type="date" placeholder="现场检查日期" value-format="YYYY-MM-DD"
                        class="!w-full" :clearable="false" data-testid="qc-f-date" />
        <el-select v-model="form.category" placeholder="检查类别" class="w-full" data-testid="qc-f-category"
                   :fit-input-width="false" :disabled="!!form.id" @change="onFormCategory">
          <el-option v-for="o in categoryOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-select v-model="form.inspectorId" placeholder="检查人（空=记当前登录人）" filterable clearable class="w-full"
                   data-testid="qc-f-inspector" :fit-input-width="false">
          <el-option v-for="p in inspectors" :key="p.employeeId" :label="`${p.empName}${p.title ? `（${p.title}）` : ''}`" :value="String(p.employeeId)" />
        </el-select>
        <el-input v-model="form.summary" placeholder="本轮小结（空则由服务端自动生成）" maxlength="500" show-word-limit class="xl:col-span-2" />
      </div>

      <p class="mb-2 text-xs text-slate-500">
        只填「抽查例数 / 合格例数」，得分、合格率、主表六个汇总数全部由服务端按明细求和与「应得分 × 合格 ÷ 抽查」算出；
        没填的两列留空的检查项视为本轮未查，不会提交。
      </p>
      <el-table :data="rows" v-loading="catalogLoading" border size="small" max-height="360" data-testid="qc-form-items">
        <el-table-column label="检查项目" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="Number(row.keyFlag) === 1 ? 'font-medium text-slate-800' : ''">{{ row.itemName }}</span>
            <el-tag v-if="Number(row.keyFlag) === 1" type="danger" size="small" effect="plain" class="ml-1">关键</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="standard" label="检查标准" min-width="180" show-overflow-tooltip />
        <el-table-column label="应得分" width="80" align="center">
          <template #default="{ row }">{{ fmt(row.fullScore, 1) }}</template>
        </el-table-column>
        <el-table-column label="抽查例数" width="120" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.checkedNum" :min="0" :precision="0" controls-position="right"
                             size="small" class="!w-24" data-testid="qc-f-checked" placeholder="—" />
          </template>
        </el-table-column>
        <el-table-column label="合格例数" width="120" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.qualifiedNum" :min="0" :precision="0" controls-position="right"
                             size="small" class="!w-24" data-testid="qc-f-qualified" placeholder="—" />
          </template>
        </el-table-column>
        <el-table-column label="存在问题" min-width="150">
          <template #default="{ row }"><el-input v-model="row.problem" type="textarea" :rows="1" size="small" autosize /></template>
        </el-table-column>
        <el-table-column label="原因分析" min-width="150">
          <template #default="{ row }"><el-input v-model="row.causeAnalysis" type="textarea" :rows="1" size="small" autosize /></template>
        </el-table-column>
        <el-table-column label="整改措施" min-width="150">
          <template #default="{ row }"><el-input v-model="row.rectifyMeasure" type="textarea" :rows="1" size="small" autosize /></template>
        </el-table-column>
      </el-table>
      <p class="mt-2 text-xs text-slate-400">
        已录入 {{ filledRows.length }} / {{ rows.length }} 项{{ form.id ? '' : '（同一病区同一月同一类别只有一张单，再保存会整单覆盖明细）' }}
      </p>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="qc-f-save" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
:deep(.el-table) {
  --el-table-border-color: #cbd5e1;
}
:deep(.qc-tabs .el-tabs__item) {
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

/* KPI 卡片：达标绿、未达标红、无目标/没数灰，判定只看后端 reachedFlag */
.kpi-card {
  border-left: 4px solid #e2e8f0;
}
.kpi-card.is-ok {
  border-left-color: #0e9488;
}
.kpi-card.is-bad {
  border-left-color: #be123c;
  background: #fff7f7;
}
.kpi-card.is-flat {
  border-left-color: #cbd5e1;
}

.compare-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.bar-track {
  flex: 1;
  height: 14px;
  background: #f1f5f9;
  border-radius: 7px;
  overflow: hidden;
}
.bar-fill {
  display: block;
  height: 100%;
  border-radius: 7px;
}
.bar-fill.is-ok {
  background: #1269b5;
}
.bar-fill.is-bad {
  background: #be123c;
}
</style>
