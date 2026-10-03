<script setup lang="ts">
/**
 * 医疗纠纷 / 投诉登记（L17）
 *
 * 闭环：登记（待受理）→ 受理（调查中，按需封存病历）→ 调查/处理跟踪（处理中）
 *      → 结案（终态，收口途径/责任/赔偿）/ 撤销（终态，原因必填）
 *
 * 五条口径：
 * 1. **按钮可用性一律读后端 can* 字段**（canEdit/canAccept/canFollow/canClose/canRevoke/canSeal/canDelete），
 *    不按 status 码值 switch —— 后端加一档状态，switch 会静默渲染成"看着正常"的错按钮。
 * 2. **受理联动封存病历**：needSeal=1 时后端自动封存该患者已归档病案，回写 sealStatus=1；
 *    暂无已归档病案 → sealStatus=2（待归档后封存）。这一列必须让用户看见，
 *    否则会出现"以为封了其实没封"——纠纷里这是致命的。
 * 3. 结案四项必填（处理途径 + 责任认定 + 赔偿金额 + 结论）：无赔偿填 0，不允许留空蒙混。
 * 4. 投诉人电话后端已脱敏，前端原样展示，不再裁一遍。
 * 5. 「受理天数」openDays 服务端算，前端不重算时间差。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  listDisputePage,
  getDisputeDetail,
  disputeUpsert,
  acceptDispute,
  followDispute,
  sealDisputeNow,
  closeDispute,
  revokeDispute,
  deleteDispute,
  getDisputeStat,
} from '@/api/dispute'
import { getDepartmentSelectList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import PatientSelect from '@/components/his/PatientSelect.vue'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  caseType: null as number | null,
  status: null as number | null,
  level: null as number | null,
  deptId: null as any,
  openOnly: false,
  dateRange: null as [string, string] | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const stats = reactive({
  total: 0, openCount: 0, closedCount: 0, revokedCount: 0,
  compensationTotal: '0', avgCloseDays: '0',
})

// 字典：8 张超过单次 5 个 type 的上限 → 分两批取（一次塞 8 个会整个返回空且零报错）
const caseTypeDict = ref<any[]>([])
const sourceDict = ref<any[]>([])
const statusDict = ref<any[]>([])
const levelDict = ref<any[]>([])
const dealTypeDict = ref<any[]>([])
const dutyDict = ref<any[]>([])
const relationDict = ref<any[]>([])
const sealDict = ref<any[]>([])

const caseTypeText = (v: any) => dictLabelText(caseTypeDict.value, v)
const sourceText = (v: any) => dictLabelText(sourceDict.value, v)
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const levelText = (v: any) => dictLabelText(levelDict.value, v)
const dealTypeText = (v: any) => dictLabelText(dealTypeDict.value, v)
const dutyText = (v: any) => dictLabelText(dutyDict.value, v)
const relationText = (v: any) => dictLabelText(relationDict.value, v)
const sealText = (v: any) => dictLabelText(sealDict.value, v)

const deptOptions = ref<any[]>([])

const statusTagType = (v: any) => {
  switch (Number(v)) {
    case 1: return 'warning'
    case 2: return 'primary'
    case 3: return 'danger'
    case 4: return 'success'
    case 5: return 'info'
    default: return 'info'
  }
}
const levelTagType = (v: any) => {
  switch (Number(v)) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}
const sealTagType = (v: any) => {
  switch (Number(v)) {
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const loadDicts = async () => {
  try {
    const a: any = await getDictDataMapList([
      DICT_TYPE.DISPUTE_CASE_TYPE, DICT_TYPE.DISPUTE_SOURCE, DICT_TYPE.DISPUTE_STATUS,
      DICT_TYPE.DISPUTE_LEVEL, DICT_TYPE.DISPUTE_DEAL_TYPE,
    ].join(','))
    const b: any = await getDictDataMapList([
      DICT_TYPE.DISPUTE_DUTY, DICT_TYPE.DISPUTE_RELATION, DICT_TYPE.DISPUTE_SEAL_STATUS,
    ].join(','))
    const da = a?.data || {}
    const db = b?.data || {}
    caseTypeDict.value = da[DICT_TYPE.DISPUTE_CASE_TYPE] || []
    sourceDict.value = da[DICT_TYPE.DISPUTE_SOURCE] || []
    statusDict.value = da[DICT_TYPE.DISPUTE_STATUS] || []
    levelDict.value = da[DICT_TYPE.DISPUTE_LEVEL] || []
    dealTypeDict.value = da[DICT_TYPE.DISPUTE_DEAL_TYPE] || []
    dutyDict.value = db[DICT_TYPE.DISPUTE_DUTY] || []
    relationDict.value = db[DICT_TYPE.DISPUTE_RELATION] || []
    sealDict.value = db[DICT_TYPE.DISPUTE_SEAL_STATUS] || []
  } catch (e) {
    console.error('加载纠纷字典失败', e)
  }
}

const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({})
    if (res.code === 200) deptOptions.value = res.data || []
  } catch (e) {
    console.error('加载科室失败', e)
  }
}

const loadStats = async () => {
  try {
    const res: any = await getDisputeStat()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listDisputePage({
      keyword: query.keyword.trim() || undefined,
      caseType: query.caseType ?? undefined,
      status: query.status ?? undefined,
      level: query.level ?? undefined,
      deptId: query.deptId || undefined,
      openOnly: query.openOnly || undefined,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1],
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.caseType = null
  query.status = null
  query.level = null
  query.deptId = null
  query.openOnly = false
  query.dateRange = null
  query.pageNum = 1
  loadList()
}

// ------------------------------------------------------------------
// 登记 / 修改
// ------------------------------------------------------------------
const formVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null as string | null,
  caseType: null as number | null,
  sourceType: null as number | null,
  level: 1 as number,
  patientId: null as string | null,
  patientName: '',
  deptId: null as any,
  involvedStaff: '',
  complainant: '',
  complainantRel: null as number | null,
  complainantTel: '',
  occurTime: '',
  occurPlace: '',
  content: '',
  demand: '',
  needSeal: 0 as number,
  remark: '',
})

const openCreate = () => {
  Object.assign(form, {
    id: null, caseType: null, sourceType: null, level: 1, patientId: null, patientName: '',
    deptId: null, involvedStaff: '', complainant: '', complainantRel: null, complainantTel: '',
    occurTime: '', occurPlace: '', content: '', demand: '', needSeal: 0, remark: '',
  })
  formVisible.value = true
}

const openEdit = async (row: any) => {
  const res: any = await getDisputeDetail(row.id)
  const d = res?.data || row
  Object.assign(form, {
    id: d.id,
    caseType: Number(d.caseType),
    sourceType: Number(d.sourceType),
    level: Number(d.level ?? 1),
    patientId: d.patientId ?? null,
    patientName: d.patientName || '',
    deptId: d.deptId ?? null,
    involvedStaff: d.involvedStaff || '',
    complainant: d.complainant || '',
    complainantRel: d.complainantRel ?? null,
    complainantTel: '',
    occurTime: (d.occurTime || '').slice(0, 16),
    occurPlace: d.occurPlace || '',
    content: d.content || '',
    demand: d.demand || '',
    needSeal: Number(d.needSeal ?? 0),
    remark: d.remark || '',
  })
  formVisible.value = true
}

const onPatientSelect = (p: any) => {
  form.patientId = p?.id ?? null
  form.patientName = p?.name || p?.patientName || ''
}

const save = async () => {
  if (!form.caseType || !form.sourceType) {
    ElMessage.warning('请选择类型与来源')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请填写投诉/纠纷内容')
    return
  }
  saving.value = true
  try {
    const res: any = await disputeUpsert({
      id: form.id || undefined,
      caseType: form.caseType,
      sourceType: form.sourceType,
      level: form.level,
      patientId: form.patientId || undefined,
      deptId: form.deptId || undefined,
      involvedStaff: form.involvedStaff.trim() || undefined,
      complainant: form.complainant.trim() || undefined,
      complainantRel: form.complainantRel ?? undefined,
      complainantTel: form.complainantTel.trim() || undefined,
      occurTime: form.occurTime ? (form.occurTime.length === 16 ? `${form.occurTime}:00` : form.occurTime) : undefined,
      occurPlace: form.occurPlace.trim() || undefined,
      content: form.content.trim(),
      demand: form.demand.trim() || undefined,
      needSeal: form.needSeal ?? 0,
      remark: form.remark.trim() || undefined,
    })
    if (res.code === 200) {
      const vo = res.data
      ElMessage.success(form.id ? '修改成功' : `登记成功，单号 ${vo?.caseNo || ''}`)
      formVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// ------------------------------------------------------------------
// 详情抽屉（主单 + 处理跟踪台账）
// ------------------------------------------------------------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getDisputeDetail(row.id)
    if (res.code === 200) {
      detail.value = res.data
      detailVisible.value = true
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载详情失败')
  }
}

// ------------------------------------------------------------------
// 动作弹窗（受理 / 处理跟踪 / 撤销 共用）
// ------------------------------------------------------------------
const actVisible = ref(false)
const actKey = ref<'accept' | 'follow' | 'revoke'>('accept')
const actRow = ref<any>(null)
const actAction = ref('')
const actContent = ref('')
const actToStatus = ref<number | null>(null)
const actLoading = ref(false)

const ACT_META: Record<string, { title: string, needAction: boolean, needContent: boolean, placeholder: string }> = {
  accept: { title: '受理', needAction: false, needContent: false, placeholder: '受理说明（可选）' },
  follow: { title: '登记处理跟踪', needAction: true, needContent: true, placeholder: '调查/协商/回复投诉人的具体情况' },
  revoke: { title: '撤销', needAction: false, needContent: true, placeholder: '撤销原因（必填，将留痕）' },
}

const openAct = (row: any, key: 'accept' | 'follow' | 'revoke') => {
  actRow.value = row
  actKey.value = key
  actAction.value = ''
  actContent.value = ''
  actToStatus.value = null
  actVisible.value = true
}

const submitAct = async () => {
  const meta = ACT_META[actKey.value]
  if (meta.needAction && !actAction.value.trim()) {
    ElMessage.warning('请填写处理动作')
    return
  }
  if (meta.needContent && !actContent.value.trim()) {
    ElMessage.warning('请填写说明内容')
    return
  }
  actLoading.value = true
  try {
    let res: any
    if (actKey.value === 'accept') {
      res = await acceptDispute({ id: actRow.value.id, content: actContent.value.trim() || undefined })
    } else if (actKey.value === 'follow') {
      res = await followDispute({
        id: actRow.value.id,
        action: actAction.value.trim(),
        content: actContent.value.trim(),
        toStatus: actToStatus.value ?? undefined,
      })
    } else {
      res = await revokeDispute({ id: actRow.value.id, content: actContent.value.trim() })
    }
    if (res.code === 200) {
      const vo = res.data
      let tip = `${meta.title}成功`
      if (actKey.value === 'accept' && vo?.sealStatus === 2) {
        tip += '（该患者暂无已归档病历，已标记待归档后封存，归档后请点「补封存」）'
      }
      if (actKey.value === 'accept' && vo?.sealStatus === 1) {
        tip += '（已联动封存病历）'
      }
      ElMessage.success(tip)
      actVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || `${meta.title}失败`)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(`${meta.title}失败`)
  } finally {
    actLoading.value = false
  }
}

// ------------------------------------------------------------------
// 结案
// ------------------------------------------------------------------
const closeVisible = ref(false)
const closeRow = ref<any>(null)
const closeForm = reactive({
  dealType: null as number | null,
  dutyType: null as number | null,
  compensation: 0 as number,
  conclusion: '',
})
const closeLoading = ref(false)

const openClose = (row: any) => {
  closeRow.value = row
  closeForm.dealType = null
  closeForm.dutyType = null
  closeForm.compensation = 0
  closeForm.conclusion = ''
  closeVisible.value = true
}

const submitClose = async () => {
  if (!closeForm.dealType || !closeForm.dutyType) {
    ElMessage.warning('请选择处理途径与责任认定')
    return
  }
  if (closeForm.compensation == null || Number(closeForm.compensation) < 0) {
    ElMessage.warning('赔偿金额不能为负（无赔偿请填 0）')
    return
  }
  if (!closeForm.conclusion.trim()) {
    ElMessage.warning('请填写调查结论/处理结果')
    return
  }
  closeLoading.value = true
  try {
    const res: any = await closeDispute({
      id: closeRow.value.id,
      dealType: closeForm.dealType,
      dutyType: closeForm.dutyType,
      compensation: Number(closeForm.compensation),
      conclusion: closeForm.conclusion.trim(),
    })
    if (res.code === 200) {
      ElMessage.success('已结案')
      closeVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '结案失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('结案失败')
  } finally {
    closeLoading.value = false
  }
}

// ------------------------------------------------------------------
// 补封存 / 删除
// ------------------------------------------------------------------
const doSeal = async (row: any) => {
  try {
    await ElMessageBox.confirm('将封存该患者已归档病历，确认继续？', '封存确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: any = await sealDisputeNow({ id: row.id })
    if (res.code === 200) {
      ElMessage.success('病历已封存')
      loadList()
    } else {
      ElMessage.error(res.message || '封存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('封存失败')
  }
}

const remove = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除单据 ${row.caseNo}？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: any = await deleteDispute(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts()])
  await loadList()
  await loadStats()
})
</script>

<template>
  <div>
    <el-card shadow="never" class="stat-card mb-3">
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-label">登记总数</span>
          <span class="stat-value">{{ stats.total }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">未结案</span>
          <span class="stat-value warn">{{ stats.openCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已结案</span>
          <span class="stat-value ok">{{ stats.closedCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已撤销</span>
          <span class="stat-value">{{ stats.revokedCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">赔偿合计（元）</span>
          <span class="stat-value money">{{ stats.compensationTotal }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">平均结案天数</span>
          <span class="stat-value">{{ stats.avgCloseDays }}</span>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="单号 / 患者 / 投诉人" clearable style="width: 200px"
              @keyup.enter="loadList" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.caseType" placeholder="类型" clearable style="width: 130px">
              <el-option v-for="d in caseTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="query.level" placeholder="等级" clearable style="width: 120px">
              <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="被投诉科室">
            <el-select v-model="query.deptId" placeholder="被投诉科室" clearable filterable style="width: 180px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="登记日期">
            <el-date-picker v-model="query.dateRange" type="daterange" value-format="YYYY-MM-DD"
              start-placeholder="登记起始" end-placeholder="登记截止" style="width: 240px" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.openOnly">仅未结案</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'qc:dispute:add'" type="primary" :icon="Plus" @click="openCreate">登记</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" @row-click="openDetail">
        <el-table-column prop="caseNo" label="单号" width="150" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="Number(row.caseType) === 1 ? 'info' : 'danger'">{{ caseTypeText(row.caseType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="levelTagType(row.level)">{{ levelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="deptName" label="被投诉科室" width="150" show-overflow-tooltip />
        <el-table-column prop="complainant" label="投诉人" width="100" />
        <el-table-column prop="complainantTel" label="联系电话" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="病历封存" width="140">
          <template #default="{ row }">
            <el-tag v-if="Number(row.needSeal) === 1" size="small" :type="sealTagType(row.sealStatus)">
              {{ sealText(row.sealStatus) }}
            </el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="处理途径" width="120">
          <template #default="{ row }">{{ row.dealType ? dealTypeText(row.dealType) : '—' }}</template>
        </el-table-column>
        <el-table-column label="赔偿(元)" width="100" align="right">
          <template #default="{ row }">{{ row.compensation ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="受理天数" width="90" align="right">
          <template #default="{ row }">{{ row.openDays ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="160" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.canAccept" v-perm="'qc:dispute:edit'" link type="primary" size="small"
              @click.stop="openAct(row, 'accept')">受理</el-button>
            <el-button v-if="row.canFollow" v-perm="'qc:dispute:edit'" link type="primary" size="small"
              @click.stop="openAct(row, 'follow')">处理跟踪</el-button>
            <el-button v-if="row.canClose" v-perm="'qc:dispute:edit'" link type="success" size="small"
              @click.stop="openClose(row)">结案</el-button>
            <el-button v-if="row.canSeal" v-perm="'qc:dispute:edit'" link type="warning" size="small"
              @click.stop="doSeal(row)">补封存</el-button>
            <el-button v-if="row.canRevoke" v-perm="'qc:dispute:edit'" link type="info" size="small"
              @click.stop="openAct(row, 'revoke')">撤销</el-button>
            <el-button v-if="row.canEdit" v-perm="'qc:dispute:add'" link type="primary" size="small"
              @click.stop="openEdit(row)">编辑</el-button>
            <el-button v-if="row.canDelete" v-perm="'qc:dispute:add'" link type="danger" size="small"
              @click.stop="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无纠纷/投诉登记" />
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 登记 / 修改 -->
    <el-dialog v-model="formVisible" :title="form.id ? '修改纠纷/投诉' : '登记纠纷/投诉'" width="760px"
      :close-on-click-modal="true">
      <el-form label-width="110px" size="small">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="类型" required>
              <el-select v-model="form.caseType" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in caseTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" required>
              <el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in sourceDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="等级">
              <el-select v-model="form.level" style="width: 100%">
                <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="被投诉科室">
              <el-select v-model="form.deptId" filterable clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="患者">
              <PatientSelect :model-value="form.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                @select="onPatientSelect" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="涉及人员">
              <el-input v-model="form.involvedStaff" placeholder="多人用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="投诉人">
              <el-input v-model="form.complainant" placeholder="姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="与患者关系">
              <el-select v-model="form.complainantRel" clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="d in relationDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="form.complainantTel" placeholder="出参脱敏展示" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="发生时间">
              <el-date-picker v-model="form.occurTime" type="datetime" value-format="YYYY-MM-DD HH:mm"
                placeholder="请选择" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发生地点">
              <el-input v-model="form.occurPlace" placeholder="如：内科三病区" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="投诉/纠纷的具体内容" />
        </el-form-item>
        <el-form-item label="诉求">
          <el-input v-model="form.demand" type="textarea" :rows="2" placeholder="投诉人诉求" />
        </el-form-item>
        <el-form-item label="需封存病历">
          <el-radio-group v-model="form.needSeal">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是（受理时联动封存已归档病案）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="纠纷/投诉详情" width="820px">
      <div v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="单号">{{ detail.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ caseTypeText(detail.caseType) }}</el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag size="small" :type="levelTagType(detail.level)">{{ levelText(detail.level) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName || '—' }}（{{ detail.patientNo || '—' }}）</el-descriptions-item>
          <el-descriptions-item label="被投诉科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="涉及人员">{{ detail.involvedStaff || '—' }}</el-descriptions-item>
          <el-descriptions-item label="投诉人">{{ detail.complainant || '—' }}（{{ relationText(detail.complainantRel) }}）</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.complainantTel || '—' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ sourceText(detail.sourceType) }}</el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ detail.occurTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发生地点">{{ detail.occurPlace || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="病历封存">
            <el-tag v-if="Number(detail.needSeal) === 1" size="small" :type="sealTagType(detail.sealStatus)">
              {{ sealText(detail.sealStatus) }}
            </el-tag>
            <span v-else>未申请</span>
          </el-descriptions-item>
          <el-descriptions-item label="受理天数">{{ detail.openDays ?? 0 }} 天</el-descriptions-item>
          <el-descriptions-item label="登记人">{{ detail.registerBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="受理人">{{ detail.acceptBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="结案人">{{ detail.closeBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理途径" :span="1">{{ detail.dealType ? dealTypeText(detail.dealType) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="责任认定">{{ detail.dutyType ? dutyText(detail.dutyType) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="赔偿(元)">{{ detail.compensation ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="3">{{ detail.content }}</el-descriptions-item>
          <el-descriptions-item label="诉求" :span="3">{{ detail.demand || '—' }}</el-descriptions-item>
          <el-descriptions-item label="结论" :span="3">{{ detail.conclusion || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.revokeReason" label="撤销原因" :span="3">{{ detail.revokeReason }}</el-descriptions-item>
        </el-descriptions>

        <div class="flow-title">处理跟踪台账</div>
        <el-timeline v-if="detail.flows && detail.flows.length">
          <el-timeline-item v-for="f in detail.flows" :key="f.id" :timestamp="f.operateTime" placement="top">
            <div class="flow-item">
              <strong>{{ f.action }}</strong>
              <span class="flow-status">{{ statusText(f.fromStatus) }} → {{ statusText(f.toStatus) }}</span>
              <span class="flow-op">{{ f.operator }}</span>
            </div>
            <div class="flow-content">{{ f.content || '—' }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无处理跟踪记录" :image-size="60" />
      </div>
    </el-dialog>

    <!-- 受理 / 处理跟踪 / 撤销 -->
    <el-dialog v-model="actVisible" :title="ACT_META[actKey].title" width="560px">
      <el-form label-width="100px" size="small">
        <el-form-item v-if="ACT_META[actKey].needAction" label="处理动作" required>
          <el-input v-model="actAction" placeholder="如：科室调查 / 医患协商 / 医调委调解" />
        </el-form-item>
        <el-form-item v-if="actKey === 'follow'" label="推进状态">
          <el-select v-model="actToStatus" clearable placeholder="不改变状态" style="width: 100%">
            <el-option label="调查中" :value="2" />
            <el-option label="处理中" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item :label="ACT_META[actKey].needContent ? '说明' : '说明（可选）'" :required="ACT_META[actKey].needContent">
          <el-input v-model="actContent" type="textarea" :rows="4" :placeholder="ACT_META[actKey].placeholder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actVisible = false">取消</el-button>
        <el-button type="primary" :loading="actLoading" @click="submitAct">确定</el-button>
      </template>
    </el-dialog>

    <!-- 结案 -->
    <el-dialog v-model="closeVisible" title="结案" width="600px">
      <el-form label-width="110px" size="small">
        <el-form-item label="处理途径" required>
          <el-select v-model="closeForm.dealType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in dealTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任认定" required>
          <el-select v-model="closeForm.dutyType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in dutyDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="赔偿金额(元)" required>
          <el-input-number v-model="closeForm.compensation" :min="0" :precision="2" :step="100" />
          <span class="hint">无赔偿请填 0</span>
        </el-form-item>
        <el-form-item label="结论" required>
          <el-input v-model="closeForm.conclusion" type="textarea" :rows="4" placeholder="调查结论 / 处理结果（将写入跟踪台账）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeVisible = false">取消</el-button>
        <el-button type="primary" :loading="closeLoading" @click="submitClose">结案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-row { display: flex; gap: 32px; flex-wrap: wrap; }
.stat-item { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 20px; font-weight: 600; color: #303133; }
.stat-value.warn { color: #e6a23c; }
.stat-value.ok { color: #67c23a; }
.stat-value.money { color: #f56c6c; }
.flow-title { margin: 16px 0 10px; font-weight: 600; color: #303133; }
.flow-item { display: flex; gap: 10px; align-items: baseline; }
.flow-status { font-size: 12px; color: #909399; }
.flow-op { font-size: 12px; color: #409eff; }
.flow-content { margin-top: 4px; color: #606266; white-space: pre-wrap; }
.hint { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
