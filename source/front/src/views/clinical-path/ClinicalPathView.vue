<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Promotion, CircleClose, Delete } from '@element-plus/icons-vue'
import {
  listPathwayPage,
  getPathwayDetail,
  getActivePathwayList,
  pathwayUpsert,
  publishPathway,
  deprecatePathway,
  listEnrollPage,
  getEnrollDetail,
  listAdmissionsForEnroll,
  enrollUpsert,
  varianceUpsert,
  finishEnroll,
  abortEnroll,
  getPathwayAnalysis,
} from '@/api/pathway'
import { getDepartmentSelectList, getDictDataMapList } from '@/api/system'
import { getTreatmentItemSelectList } from '@/api/treatment'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import {
  pathwayStatusTag,
  enrollStatusTag,
  canEditPathway,
  canPublish,
  canDeprecate,
  canVariance,
  canFinish,
  canAbort,
  PATHWAY_STATUS,
  ENROLL_STATUS,
} from '@/lib/pathway'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const activeTab = ref('templates')

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ---------------- 字典与科室 ----------------
const statusDict = ref<any[]>([])
const enrollStatusDict = ref<any[]>([])
const varianceTypeDict = ref<any[]>([])
const itemTypeDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const enrollStatusText = (v: any) => dictLabelText(enrollStatusDict.value, v)
const varianceTypeText = (v: any) => dictLabelText(varianceTypeDict.value, v)
const itemTypeText = (v: any) => dictLabelText(itemTypeDict.value, v)

const depts = ref<any[]>([])

// ---------------- 页签一：模板 ----------------
const tplLoading = ref(false)
const tplRows = ref<any[]>([])
const tplTotal = ref(0)
const tplQuery = reactive({
  keyword: '',
  status: null as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.PATHWAY_STATUS},${DICT_TYPE.PATHWAY_ENROLL_STATUS},${DICT_TYPE.PATHWAY_VARIANCE_TYPE},${DICT_TYPE.PATHWAY_ITEM_TYPE}`
    )
    statusDict.value = res?.data?.[DICT_TYPE.PATHWAY_STATUS] || []
    enrollStatusDict.value = res?.data?.[DICT_TYPE.PATHWAY_ENROLL_STATUS] || []
    varianceTypeDict.value = res?.data?.[DICT_TYPE.PATHWAY_VARIANCE_TYPE] || []
    itemTypeDict.value = res?.data?.[DICT_TYPE.PATHWAY_ITEM_TYPE] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({ pageSize: 200 })
    depts.value = res?.data?.records || res?.data || []
  } catch (e) {
    console.error('加载科室失败', e)
  }
}

const loadTpl = async () => {
  tplLoading.value = true
  try {
    const res: any = await listPathwayPage({
      keyword: tplQuery.keyword.trim() || undefined,
      status: tplQuery.status ?? undefined,
      pageNum: tplQuery.pageNum,
      pageSize: tplQuery.pageSize,
    })
    if (res.code === 200) {
      tplRows.value = res.data?.records || []
      tplTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询模板失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询模板失败')
  } finally {
    tplLoading.value = false
  }
}

const resetTpl = () => {
  tplQuery.keyword = ''
  tplQuery.status = null
  tplQuery.pageNum = 1
  loadTpl()
}

// ---- 模板编辑弹窗（草稿） ----
const tplEditVisible = ref(false)
const tplSaving = ref(false)
const tplForm = reactive({
  id: null as string | null,
  pathwayCode: '',
  pathwayName: '',
  deptId: null as number | null,
  diagnosis: '',
  version: 'V1',
  remark: '',
  steps: [] as any[],
})

const openTplEdit = async (row?: any) => {
  tplForm.id = null
  tplForm.pathwayCode = ''
  tplForm.pathwayName = ''
  tplForm.deptId = null
  tplForm.diagnosis = ''
  tplForm.version = 'V1'
  tplForm.remark = ''
  tplForm.steps = [{ dayNo: 1, itemType: 1, itemName: '', content: '', sortNo: 1 }]
  if (row?.id) {
    const res: any = await getPathwayDetail(row.id)
    if (res.code === 200 && res.data) {
      const d = res.data
      tplForm.id = d.id
      tplForm.pathwayCode = d.pathwayCode
      tplForm.pathwayName = d.pathwayName
      tplForm.deptId = d.deptId ? Number(d.deptId) : null
      tplForm.diagnosis = d.diagnosis || ''
      tplForm.version = d.version || 'V1'
      tplForm.remark = d.remark || ''
      tplForm.steps = (d.steps || []).map((s: any) => ({ ...s }))
    } else {
      ElMessage.error(res.message || '加载模板失败')
      return
    }
  }
  tplEditVisible.value = true
}

const addStep = () => {
  const last = tplForm.steps[tplForm.steps.length - 1]
  tplForm.steps.push({
    dayNo: last?.dayNo || 1,
    itemType: last?.itemType || 1,
    itemName: '',
    content: '',
    sortNo: tplForm.steps.length + 1,
  })
}

const delStep = (idx: number) => {
  tplForm.steps.splice(idx, 1)
}

// 项目名称软带出：治疗项目字典远程搜索，允许自由文本（filterable + allow-create）
// 选中字典项时同步 row.itemCode（医生站偏离比对只认带编码的步骤）；自由文本编码置空
const itemNameOptions = ref<any[]>([])
const itemNameLoading = ref(false)
const searchItemNames = async (kw: string) => {
  itemNameLoading.value = true
  try {
    const res: any = await getTreatmentItemSelectList({ keyword: kw || undefined, limit: 20 })
    itemNameOptions.value = (res?.data || []).map((x: any) => ({ itemName: x.itemName, itemCode: x.itemCode }))
  } catch (e) {
    console.error(e)
    itemNameOptions.value = []
  } finally {
    itemNameLoading.value = false
  }
}
const onItemNameChange = (row: any, val: string) => {
  row.itemCode = itemNameOptions.value.find(o => o.itemName === val)?.itemCode ?? null
}

const saveTpl = async () => {
  if (!tplForm.pathwayCode.trim() || !tplForm.pathwayName.trim()) {
    ElMessage.warning('路径编码与名称必填')
    return
  }
  for (const s of tplForm.steps) {
    if (!s.dayNo || !s.itemType || !String(s.itemName || '').trim()) {
      ElMessage.warning('步骤的路径日/项目类型/项目名称必填')
      return
    }
  }
  tplSaving.value = true
  try {
    const res: any = await pathwayUpsert({
      id: tplForm.id || undefined,
      pathwayCode: tplForm.pathwayCode.trim(),
      pathwayName: tplForm.pathwayName.trim(),
      deptId: tplForm.deptId ?? undefined,
      diagnosis: tplForm.diagnosis.trim() || undefined,
      version: tplForm.version.trim() || undefined,
      remark: tplForm.remark.trim() || undefined,
      steps: tplForm.steps.map((s, i) => ({
        dayNo: Number(s.dayNo),
        itemType: Number(s.itemType),
        itemName: String(s.itemName).trim(),
        itemCode: String(s.itemCode || '').trim() || undefined,
        content: String(s.content || '').trim() || undefined,
        sortNo: i + 1,
      })),
    })
    if (res.code === 200) {
      ElMessage.success('模板已保存（草稿）')
      tplEditVisible.value = false
      await loadTpl()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    tplSaving.value = false
  }
}

const onPublish = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认发布「${row.pathwayName} ${row.version}」？发布后锁定不可编辑，同编码只允许一张使用中。`,
      '发布模板', { confirmButtonText: '确认发布', cancelButtonText: '取消' },
    )
    const res: any = await publishPathway({ id: row.id })
    if (res.code === 200) {
      ElMessage.success('已发布为使用中')
      await loadTpl()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
  }
}

const onDeprecate = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认停用「${row.pathwayName} ${row.version}」？停用后不再可入径，存量在径不受影响。`,
      '停用模板', { confirmButtonText: '确认停用', cancelButtonText: '取消', type: 'warning' },
    )
    const res: any = await deprecatePathway({ id: row.id })
    if (res.code === 200) {
      ElMessage.success('已停用')
      await loadTpl()
    } else {
      ElMessage.error(res.message || '停用失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
  }
}

// ---- 模板详情（只读） ----
const tplViewVisible = ref(false)
const tplView = ref<any>(null)
const openTplView = async (row: any) => {
  tplViewVisible.value = true
  try {
    const res: any = await getPathwayDetail(row.id)
    if (res.code === 200) tplView.value = res.data
    else ElMessage.error(res.message || '加载模板失败')
  } catch (e) {
    console.error(e)
    ElMessage.error('加载模板失败')
  }
}

// ---------------- 页签二：在径管理 ----------------
const enLoading = ref(false)
const enRows = ref<any[]>([])
const enTotal = ref(0)
const enQuery = reactive({
  pathwayId: null as string | null,
  patientName: '',
  status: null as number | null,
  enrollDate: '' as string,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

const activePathways = ref<any[]>([])
const loadActivePathways = async () => {
  try {
    const res: any = await getActivePathwayList({})
    if (res.code === 200) activePathways.value = res.data || []
  } catch (e) {
    console.error('加载使用中模板失败', e)
  }
}

const loadEnroll = async () => {
  enLoading.value = true
  try {
    const res: any = await listEnrollPage({
      pathwayId: enQuery.pathwayId ?? undefined,
      patientName: enQuery.patientName.trim() || undefined,
      status: enQuery.status ?? undefined,
      enrollDate: enQuery.enrollDate || undefined,
      pageNum: enQuery.pageNum,
      pageSize: enQuery.pageSize,
    })
    if (res.code === 200) {
      enRows.value = res.data?.records || []
      enTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询入径台账失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询入径台账失败')
  } finally {
    enLoading.value = false
  }
}

const resetEn = () => {
  enQuery.pathwayId = null
  enQuery.patientName = ''
  enQuery.status = null
  enQuery.enrollDate = ''
  enQuery.pageNum = 1
  loadEnroll()
}

// ---- 入径区 ----
const candidates = ref<any[]>([])
const candLoading = ref(false)
const candKeyword = ref('')
const enrollForm = reactive({ admissionId: null as string | null, pathwayId: null as string | null, enrollDate: today() })
const enrolling = ref(false)

const loadCandidates = async () => {
  candLoading.value = true
  try {
    const res: any = await listAdmissionsForEnroll({ keyword: candKeyword.value.trim() || undefined, limit: 50 })
    if (res.code === 200) candidates.value = res.data || []
    else ElMessage.error(res.message || '查询在院患者失败')
  } catch (e) {
    console.error(e)
    ElMessage.error('查询在院患者失败')
  } finally {
    candLoading.value = false
  }
}

const doEnroll = async () => {
  if (!enrollForm.admissionId) {
    ElMessage.warning('请先选择入径患者')
    return
  }
  if (!enrollForm.pathwayId) {
    ElMessage.warning('请选择路径模板')
    return
  }
  const cand = candidates.value.find((c: any) => String(c.admissionId) === String(enrollForm.admissionId))
  try {
    await ElMessageBox.confirm(
      `确认为「${cand?.patientName || ''}（${cand?.admissionNo || ''}）」入径？入径日期 ${enrollForm.enrollDate}，一次住院同时仅一条在径。`,
      '入径登记', { confirmButtonText: '确认入径', cancelButtonText: '取消' },
    )
    enrolling.value = true
    const res: any = await enrollUpsert({
      admissionId: enrollForm.admissionId,
      pathwayId: enrollForm.pathwayId,
      enrollDate: enrollForm.enrollDate,
    })
    if (res.code === 200) {
      ElMessage.success(`已入径（${res.data?.enrollNo}）`)
      enrollForm.admissionId = null
      enrollForm.pathwayId = null
      await Promise.all([loadCandidates(), loadEnroll()])
      openEnrollDetail(res.data?.id)
    } else {
      ElMessage.error(res.message || '入径失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      console.error(e)
      ElMessage.error('入径失败')
    }
  } finally {
    enrolling.value = false
  }
}

// ---- 入径详情弹窗 ----
const detailVisible = ref(false)
const detail = ref<any>(null)
const detailLoading = ref(false)
const actLoading = ref(false)

const openEnrollDetail = async (id: any) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res: any = await getEnrollDetail(id)
    if (res.code === 200) detail.value = res.data
    else ElMessage.error(res.message || '查询入径详情失败')
  } catch (e) {
    console.error(e)
    ElMessage.error('查询入径详情失败')
  } finally {
    detailLoading.value = false
  }
}

const refreshDetail = async () => {
  if (detail.value?.id) await openEnrollDetail(detail.value.id)
  await Promise.all([loadEnroll(), loadCandidates()])
}

// ---- 变异登记弹窗 ----
const varVisible = ref(false)
const varSaving = ref(false)
const varForm = reactive({
  enrollId: null as string | null,
  dayNo: 1 as number,
  varianceType: 1 as number | null,
  varianceReason: '',
  handling: '',
  occurredDate: today(),
})

const openVariance = () => {
  if (!detail.value?.id) return
  varForm.enrollId = detail.value.id
  varForm.dayNo = Math.min(detail.value.currentDay || 1, detail.value.totalDays || 1)
  varForm.varianceType = 1
  varForm.varianceReason = ''
  varForm.handling = ''
  varForm.occurredDate = today()
  varVisible.value = true
}

const saveVariance = async () => {
  if (!varForm.varianceReason.trim()) {
    ElMessage.warning('变异原因必填')
    return
  }
  varSaving.value = true
  try {
    const res: any = await varianceUpsert({
      enrollId: varForm.enrollId,
      dayNo: Number(varForm.dayNo),
      varianceType: Number(varForm.varianceType),
      varianceReason: varForm.varianceReason.trim(),
      handling: varForm.handling.trim() || undefined,
      occurredDate: varForm.occurredDate,
    })
    if (res.code === 200) {
      ElMessage.success('变异已登记')
      varVisible.value = false
      await openEnrollDetail(varForm.enrollId)
      await loadEnroll()
    } else {
      ElMessage.error(res.message || '登记变异失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('登记变异失败')
  } finally {
    varSaving.value = false
  }
}

const onFinish = async () => {
  if (!detail.value?.id) return
  try {
    await ElMessageBox.confirm(
      '确认该患者完成临床路径？有变异记录不挡完成（完成=按计划走完或临床认可结局）。',
      '路径完成', { confirmButtonText: '确认完成', cancelButtonText: '取消' },
    )
    actLoading.value = true
    const res: any = await finishEnroll({ id: detail.value.id })
    if (res.code === 200) {
      ElMessage.success('已完成')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '完成失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
  } finally {
    actLoading.value = false
  }
}

const onAbort = async () => {
  if (!detail.value?.id) return
  try {
    const { value } = await ElMessageBox.prompt(
      '确认退出临床路径？退径后转非路径管理，该住院记录可重新入径。',
      '退径原因（必填）',
      { confirmButtonText: '确认退径', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '退径原因必填' },
    )
    actLoading.value = true
    const res: any = await abortEnroll({ id: detail.value.id, reason: value.trim() })
    if (res.code === 200) {
      ElMessage.success('已退径')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '退径失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
  } finally {
    actLoading.value = false
  }
}

// ---------------- 页签三：变异分析 ----------------
const anaLoading = ref(false)
const ana = ref<any>(null)
const anaPathwayId = ref<string | null>(null)
const allPathways = ref<any[]>([])

const loadAllPathways = async () => {
  try {
    const res: any = await listPathwayPage({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) allPathways.value = res.data?.records || []
  } catch (e) {
    console.error('加载模板列表失败', e)
  }
}

const loadAnalysis = async () => {
  anaLoading.value = true
  try {
    const res: any = await getPathwayAnalysis({ pathwayId: anaPathwayId.value ?? undefined })
    if (res.code === 200) ana.value = res.data
    else ElMessage.error(res.message || '查询变异分析失败')
  } catch (e) {
    console.error(e)
    ElMessage.error('查询变异分析失败')
  } finally {
    anaLoading.value = false
  }
}

const anaCards = computed(() => [
  { label: '入径人次', value: ana.value?.enrollCount ?? 0, tone: 'text-[#1269B5]' },
  { label: '完成', value: ana.value?.finishCount ?? 0, tone: 'text-emerald-700' },
  { label: '退径', value: ana.value?.abortCount ?? 0, tone: 'text-slate-500' },
  { label: '变异条数', value: ana.value?.varianceCount ?? 0, tone: 'text-amber-600' },
  { label: '完成率(%)', value: ana.value?.finishRate ?? '—', tone: 'text-emerald-700' },
])

const onTabChange = (name: any) => {
  if (name === 'enroll') {
    loadActivePathways()
    loadCandidates()
    loadEnroll()
  } else if (name === 'analysis') {
    loadAllPathways()
    loadAnalysis()
  }
}

onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts()])
  await loadTpl()
})
</script>

<template>
  <div class="p-5">
    <el-tabs v-model="activeTab" data-testid="cp-tabs" @tab-change="onTabChange">
      <!-- ============ 页签一：路径模板 ============ -->
      <el-tab-pane label="路径模板" name="templates">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">路径模板（草稿→使用中→已停用）</h3>
            <el-input v-model="tplQuery.keyword" placeholder="编码/名称/诊断" clearable class="w-52" data-testid="cp-tpl-keyword" />
            <el-select v-model="tplQuery.status" placeholder="状态" clearable class="w-32" :fit-input-width="false">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-button :icon="Search" @click="() => { tplQuery.pageNum = 1; loadTpl() }">查询</el-button>
            <el-button :icon="Refresh" @click="resetTpl">重置</el-button>
            <el-button v-perm="'qc:clinicalPath:add'" type="primary" :icon="Plus" data-testid="cp-tpl-add-btn"
                       @click="openTplEdit()">新增模板</el-button>
          </div>

          <el-table v-loading="tplLoading" :data="tplRows" border data-testid="cp-tpl-table">
            <el-table-column prop="pathwayCode" label="路径编码" width="130" />
            <el-table-column prop="pathwayName" label="路径名称" min-width="180" />
            <el-table-column label="适用科室" width="130">
              <template #default="{ row }">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="diagnosis" label="适用病种" min-width="160">
              <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
            </el-table-column>
            <el-table-column prop="version" label="版本" width="70" />
            <el-table-column prop="totalDays" label="总日数" width="80" align="right" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="pathwayStatusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishBy" label="发布人" width="90">
              <template #default="{ row }">{{ row.publishBy || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openTplView(row)">查看</el-button>
                <el-button v-if="canEditPathway(row.status)" v-perm="'qc:clinicalPath:add'" link type="primary" size="small"
                           data-testid="cp-tpl-edit-btn" @click="openTplEdit(row)">编辑</el-button>
                <el-button v-if="canPublish(row.status)" v-perm="'qc:clinicalPath:add'" link type="success" size="small"
                           data-testid="cp-tpl-publish-btn" @click="onPublish(row)">发布</el-button>
                <el-button v-if="canDeprecate(row.status)" v-perm="'qc:clinicalPath:add'" link type="danger" size="small"
                           data-testid="cp-tpl-deprecate-btn" @click="onDeprecate(row)">停用</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400">还没有路径模板，点「新增模板」开始维护</div>
            </template>
          </el-table>

          <el-pagination v-model:current-page="tplQuery.pageNum" v-model:page-size="tplQuery.pageSize"
                         :total="tplTotal" layout="total, prev, pager, next" @current-change="loadTpl" />
        </div>
      </el-tab-pane>

      <!-- ============ 页签二：在径管理 ============ -->
      <el-tab-pane label="在径管理" name="enroll">
        <div class="space-y-4">
          <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
            <h3 class="font-semibold text-slate-800">入径登记（在院且无在径记录的患者）</h3>
            <div class="flex items-center gap-3 flex-wrap">
              <el-input v-model="candKeyword" placeholder="姓名/患者号/入院号" clearable class="w-52" data-testid="cp-cand-keyword"
                        @keyup.enter="loadCandidates" />
              <el-button :icon="Search" :loading="candLoading" @click="loadCandidates">查询在院患者</el-button>
              <el-select v-model="enrollForm.admissionId" placeholder="选择患者" class="w-80" filterable :fit-input-width="false"
                         data-testid="cp-enroll-patient-select">
                <el-option v-for="c in candidates" :key="c.admissionId" :value="c.admissionId"
                           :label="`${c.patientName}（${c.patientNo}）${c.admissionNo} ${c.deptName || ''}`" />
              </el-select>
              <el-select v-model="enrollForm.pathwayId" placeholder="选择路径模板" class="w-72" filterable :fit-input-width="false"
                         data-testid="cp-enroll-pathway-select">
                <el-option v-for="p in activePathways" :key="p.id" :value="p.id"
                           :label="`${p.pathwayName} ${p.version}（${p.pathwayCode}）`" />
              </el-select>
              <el-date-picker v-model="enrollForm.enrollDate" type="date" value-format="YYYY-MM-DD"
                              placeholder="入径日期" class="w-40" :clearable="false" />
              <el-button v-perm="'qc:clinicalPath:edit'" type="primary" :loading="enrolling" :disabled="!enrollForm.admissionId || !enrollForm.pathwayId"
                         data-testid="cp-enroll-btn" @click="doEnroll">入径</el-button>
            </div>
            <el-table v-if="candidates.length" :data="candidates" size="small" max-height="200" border>
              <el-table-column prop="patientName" label="患者" width="110" />
              <el-table-column prop="patientNo" label="患者编号" width="140" />
              <el-table-column prop="admissionNo" label="入院记录号" width="150" />
              <el-table-column prop="deptName" label="科室" width="130" />
              <el-table-column prop="admitTime" label="入院时间" width="160" />
              <el-table-column prop="diagnosis" label="入院诊断" min-width="180" />
            </el-table>
          </div>

          <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
            <div class="flex items-center gap-3 flex-wrap">
              <h3 class="font-semibold text-slate-800 mr-2">入径台账</h3>
              <el-select v-model="enQuery.pathwayId" placeholder="模板" clearable filterable class="w-60" :fit-input-width="false">
                <el-option v-for="p in activePathways" :key="p.id" :value="p.id" :label="`${p.pathwayName} ${p.version}`" />
              </el-select>
              <el-input v-model="enQuery.patientName" placeholder="患者姓名" clearable class="w-40" data-testid="cp-en-patient-search" />
              <el-select v-model="enQuery.status" placeholder="状态" clearable class="w-32" :fit-input-width="false">
                <el-option v-for="d in enrollStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
              <el-date-picker v-model="enQuery.enrollDate" type="date" value-format="YYYY-MM-DD" placeholder="入径日期" class="w-40" />
              <el-button :icon="Search" @click="() => { enQuery.pageNum = 1; loadEnroll() }">查询</el-button>
              <el-button :icon="Refresh" @click="resetEn">重置</el-button>
            </div>

            <el-table v-loading="enLoading" :data="enRows" border data-testid="cp-enroll-table">
              <el-table-column prop="enrollNo" label="入径单号" width="140" />
              <el-table-column label="路径" min-width="190">
                <template #default="{ row }">{{ row.pathwayName }} <span class="text-xs text-slate-400">{{ row.pathwayCode }} {{ row.version }}</span></template>
              </el-table-column>
              <el-table-column label="患者" min-width="130">
                <template #default="{ row }">{{ row.patientName }} <span class="text-xs text-slate-400">{{ row.patientNo }}</span></template>
              </el-table-column>
              <el-table-column prop="deptName" label="科室" width="120">
                <template #default="{ row }">{{ row.deptName || '—' }}</template>
              </el-table-column>
              <el-table-column prop="enrollDate" label="入径日期" width="105" />
              <el-table-column label="路径日" width="90" align="center">
                <template #default="{ row }">
                  <span class="font-semibold text-sky-700">{{ row.currentDay ?? '—' }}</span>
                  <span class="text-xs text-slate-400">/{{ row.totalDays }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="enrollStatusTag(row.status)" size="small">{{ enrollStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="varianceCount" label="变异" width="70" align="right" />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="openEnrollDetail(row.id)">明细</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">当前筛选条件下没有入径记录</div>
              </template>
            </el-table>

            <el-pagination v-model:current-page="enQuery.pageNum" v-model:page-size="enQuery.pageSize"
                           :total="enTotal" layout="total, prev, pager, next" @current-change="loadEnroll" />
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 页签三：变异分析 ============ -->
      <el-tab-pane label="变异分析" name="analysis">
        <div v-loading="anaLoading" class="space-y-4">
          <div class="bg-white rounded-lg border border-slate-200 p-4 flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">变异分析</h3>
            <el-select v-model="anaPathwayId" placeholder="全部模板" clearable filterable class="w-72" :fit-input-width="false"
                       data-testid="cp-ana-pathway-select">
              <el-option v-for="p in allPathways" :key="p.id" :value="p.id" :label="`${p.pathwayName} ${p.version}（${p.pathwayCode}）`" />
            </el-select>
            <el-button :icon="Search" @click="loadAnalysis">查询</el-button>
          </div>

          <div class="grid grid-cols-5 gap-3">
            <div v-for="c in anaCards" :key="c.label" class="bg-white rounded-lg border border-slate-200 px-4 py-3">
              <div class="text-xs text-slate-500">{{ c.label }}</div>
              <div class="text-2xl font-semibold mt-1" :class="c.tone">{{ c.value }}</div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
            <h3 class="font-semibold text-slate-800">模板维度</h3>
            <el-table :data="ana?.rows || []" border size="small" data-testid="cp-ana-rows">
              <el-table-column prop="pathwayCode" label="路径编码" width="130" />
              <el-table-column prop="pathwayName" label="路径名称" min-width="180" />
              <el-table-column prop="version" label="版本" width="70" />
              <el-table-column prop="enrollCount" label="入径数" width="90" align="right" />
              <el-table-column prop="finishCount" label="完成" width="80" align="right" />
              <el-table-column prop="abortCount" label="退径" width="80" align="right" />
              <el-table-column prop="varianceCount" label="变异条数" width="90" align="right" />
              <el-table-column label="完成率" width="90" align="right">
                <template #default="{ row }">{{ row.finishRate != null ? row.finishRate + '%' : '—' }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
              <h3 class="font-semibold text-slate-800">变异类型分布</h3>
              <el-table :data="ana?.typeStats || []" border size="small" max-height="320" data-testid="cp-ana-types">
                <el-table-column label="类型" min-width="140">
                  <template #default="{ row }">{{ varianceTypeText(row.varianceType) }}</template>
                </el-table-column>
                <el-table-column prop="cnt" label="条数" width="90" align="right" />
              </el-table>
            </div>
            <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
              <h3 class="font-semibold text-slate-800">变异原因 TOP10</h3>
              <el-table :data="ana?.topReasons || []" border size="small" max-height="320" data-testid="cp-ana-reasons">
                <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
                <el-table-column prop="cnt" label="条数" width="90" align="right" />
              </el-table>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 模板编辑弹窗（草稿） -->
    <el-dialog v-model="tplEditVisible" :title="tplForm.id ? '编辑路径模板（草稿）' : '新增路径模板'"
               width="1000px" data-testid="cp-tpl-edit-dialog">
      <div class="space-y-4">
        <el-form label-width="90px" class="grid grid-cols-2 gap-x-4">
          <el-form-item label="路径编码" required>
            <el-input v-model="tplForm.pathwayCode" placeholder="如 LP-GG-001" :disabled="!!tplForm.id" data-testid="cp-tpl-code" />
          </el-form-item>
          <el-form-item label="路径名称" required>
            <el-input v-model="tplForm.pathwayName" placeholder="如 人工全髋关节置换术路径" data-testid="cp-tpl-name" />
          </el-form-item>
          <el-form-item label="适用科室">
            <el-select v-model="tplForm.deptId" placeholder="科室（可不选）" clearable filterable class="w-full" :fit-input-width="false">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="版本号">
            <el-input v-model="tplForm.version" placeholder="V1" />
          </el-form-item>
          <el-form-item label="适用病种" class="col-span-2">
            <el-input v-model="tplForm.diagnosis" placeholder="诊断口径描述，入径时医生判断" />
          </el-form-item>
        </el-form>

        <div class="flex items-center justify-between">
          <h4 class="font-semibold text-slate-800">路径步骤（路径日 × 项目，文书不生成医嘱）</h4>
          <el-button :icon="Plus" size="small" data-testid="cp-step-add-btn" @click="addStep">加步骤</el-button>
        </div>
        <el-table :data="tplForm.steps" border size="small" max-height="360" data-testid="cp-step-table">
          <el-table-column label="路径日" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.dayNo" :min="1" :max="60" size="small" controls-position="right" class="w-24" />
            </template>
          </el-table-column>
          <el-table-column label="项目类型" width="150">
            <template #default="{ row }">
              <el-select v-model="row.itemType" size="small" class="w-full" :fit-input-width="false">
                <el-option v-for="d in itemTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="项目名称" width="240">
            <template #default="{ row }">
              <el-select v-model="row.itemName" size="small" class="w-full" filterable remote allow-create default-first-option
                         :remote-method="searchItemNames" :loading="itemNameLoading" placeholder="搜索字典或直接输入"
                         @change="(v: string) => onItemNameChange(row, v)"
                         :fit-input-width="false" data-testid="cp-step-name-select">
                <el-option v-for="o in itemNameOptions" :key="o.itemName" :label="o.itemName" :value="o.itemName" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="路径要求" min-width="220">
            <template #default="{ row }">
              <el-input v-model="row.content" size="small" placeholder="该路径日对本项目的要求" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" :icon="Delete" @click="delStep($index)" />
            </template>
          </el-table-column>
          <template #empty><div class="py-3 text-sm text-slate-400">点「加步骤」维护路径日项目</div></template>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="tplEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="tplSaving" data-testid="cp-tpl-save-btn" @click="saveTpl">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 模板详情（只读） -->
    <el-dialog v-model="tplViewVisible" :title="`模板 ${tplView?.pathwayName || ''} ${tplView?.version || ''}`" width="900px">
      <div v-if="tplView" class="space-y-3">
        <div class="flex items-center gap-6 text-sm text-slate-600 flex-wrap">
          <span>编码：<b class="text-slate-800">{{ tplView.pathwayCode }}</b></span>
          <span>科室：<b class="text-slate-800">{{ tplView.deptName || '—' }}</b></span>
          <span>总日数：<b class="text-slate-800">{{ tplView.totalDays }}</b></span>
          <span>状态：<el-tag :type="pathwayStatusTag(tplView.status)" size="small">{{ statusText(tplView.status) }}</el-tag></span>
          <span>适用病种：{{ tplView.diagnosis || '—' }}</span>
        </div>
        <el-table :data="tplView.steps || []" border size="small" max-height="420">
          <el-table-column prop="dayNo" label="路径日" width="80" align="center" />
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
          </el-table-column>
          <el-table-column prop="itemName" label="项目" min-width="170" />
          <el-table-column prop="content" label="路径要求" min-width="260" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>

    <!-- 入径详情弹窗（工作区） -->
    <el-dialog v-model="detailVisible" :title="`入径 ${detail?.enrollNo || ''}`" width="1080px" data-testid="cp-enroll-detail-dialog">
      <div v-if="detail" v-loading="detailLoading" class="space-y-3">
        <div class="flex items-center gap-5 text-sm text-slate-600 flex-wrap">
          <span>路径：<b class="text-slate-800">{{ detail.pathwayName }} {{ detail.version }}</b>（共 {{ detail.totalDays }} 天）</span>
          <span>患者：<b class="text-slate-800">{{ detail.patientName }}</b>（{{ detail.patientNo }}）</span>
          <span>科室：{{ detail.deptName || '—' }}</span>
          <span>入径日期：<b class="text-slate-800">{{ detail.enrollDate }}</b></span>
          <span>当前路径日：<b class="text-lg font-semibold text-sky-700">{{ detail.currentDay }}</b></span>
          <span>状态：<el-tag :type="enrollStatusTag(detail.status)" size="small">{{ enrollStatusText(detail.status) }}</el-tag></span>
          <span class="ml-auto flex gap-2">
            <el-button v-if="canVariance(detail.status)" v-perm="'qc:clinicalPath:edit'" size="small" :icon="Plus" type="warning" plain
                       data-testid="cp-variance-btn" @click="openVariance">登记变异</el-button>
            <el-button v-if="canFinish(detail.status)" v-perm="'qc:clinicalPath:edit'" size="small" type="success" plain :loading="actLoading"
                       data-testid="cp-finish-btn" @click="onFinish">完成</el-button>
            <el-button v-if="canAbort(detail.status)" v-perm="'qc:clinicalPath:edit'" size="small" type="danger" plain :loading="actLoading"
                       :icon="CircleClose" data-testid="cp-abort-btn" @click="onAbort">退径</el-button>
          </span>
        </div>
        <div v-if="detail.diagnosis" class="text-xs text-slate-500">入院诊断：{{ detail.diagnosis }}（入径依据）</div>

        <el-table :data="detail.steps || []" border size="small" max-height="320" data-testid="cp-detail-steps"
                  :row-class-name="({ row }: any) => (detail.status === ENROLL_STATUS.ENROLLED && row.dayNo === detail.currentDay ? 'bg-amber-50' : '')">
          <el-table-column prop="dayNo" label="路径日" width="80" align="center" />
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
          </el-table-column>
          <el-table-column prop="itemName" label="项目" min-width="160" />
          <el-table-column prop="content" label="路径要求" min-width="240" show-overflow-tooltip />
        </el-table>

        <div>
          <h4 class="font-semibold text-slate-800 mb-2">变异台账（{{ (detail.variances || []).length }} 条）</h4>
          <el-table :data="detail.variances || []" border size="small" max-height="240" data-testid="cp-variance-table">
            <el-table-column prop="dayNo" label="路径日" width="80" align="center" />
            <el-table-column label="类型" width="130">
              <template #default="{ row }">
                <el-tag type="warning" size="small">{{ varianceTypeText(row.varianceType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="varianceReason" label="变异原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="handling" label="处理措施" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.handling || '—' }}</template>
            </el-table-column>
            <el-table-column prop="occurredDate" label="发生日期" width="105" />
            <el-table-column prop="recorderName" label="登记人" width="90" />
          </el-table>
          <div v-if="!(detail.variances || []).length" class="py-2 text-sm text-slate-400">暂无变异记录</div>
        </div>
        <div v-if="detail.abortReason" class="text-xs text-red-600">退径留痕：{{ detail.abortReason }}（{{ detail.finishBy }} {{ detail.finishDate }}）</div>
        <div class="text-xs text-slate-400">
          说明：路径步骤为文书记录，不生成医嘱、不计费；变异不挡完成，偏离大走退径。
        </div>
      </div>
    </el-dialog>

    <!-- 变异登记弹窗 -->
    <el-dialog v-model="varVisible" title="登记变异" width="620px" data-testid="cp-variance-dialog">
      <el-form label-width="90px">
        <el-form-item label="路径日" required>
          <el-input-number v-model="varForm.dayNo" :min="1" :max="detail?.totalDays || 60" data-testid="cp-var-day" />
        </el-form-item>
        <el-form-item label="变异类型" required>
          <el-select v-model="varForm.varianceType" class="w-56" :fit-input-width="false" data-testid="cp-var-type">
            <el-option v-for="d in varianceTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生日期" required>
          <el-date-picker v-model="varForm.occurredDate" type="date" value-format="YYYY-MM-DD" :clearable="false" />
        </el-form-item>
        <el-form-item label="变异原因" required>
          <el-input v-model="varForm.varianceReason" type="textarea" :rows="2" maxlength="255" show-word-limit
                    placeholder="实际诊疗如何偏离路径要求" data-testid="cp-var-reason" />
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input v-model="varForm.handling" type="textarea" :rows="2" maxlength="255" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="varVisible = false">取消</el-button>
        <el-button type="primary" :loading="varSaving" data-testid="cp-var-save-btn" @click="saveVariance">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.w-24 { width: 96px; }
</style>
