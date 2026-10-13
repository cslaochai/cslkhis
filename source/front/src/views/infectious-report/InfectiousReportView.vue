<template>
  <div data-testid="infectious-report-page">
    <!-- 统计卡 -->
    <div class="mb-3 grid grid-cols-7 gap-3">
      <el-card v-for="c in [
        { k: 'pendingAudit', label: '待审核', cls: 'text-amber-500' },
        { k: 'audited', label: '已审核', cls: 'text-blue-500' },
        { k: 'directReported', label: '已直报', cls: 'text-green-600' },
        { k: 'returned', label: '已退报', cls: 'text-red-500' },
        { k: 'overduePending', label: '超时未报', cls: 'text-red-600' },
        { k: 'todayNew', label: '今日新增', cls: '' },
        { k: 'classAPending', label: '甲类在办', cls: 'text-red-600' },
      ]" :key="c.k" class="!rounded-lg" shadow="never">
        <div class="text-center">
          <div :class="['text-2xl', c.cls]" :data-testid="'stat-' + c.k">{{ stats[c.k] ?? 0 }}</div>
          <div class="text-xs text-gray-400 mt-1">{{ c.label }}</div>
        </div>
      </el-card>
    </div>

    <!-- 筛选（两卡式：查询卡 + 表格卡，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="状态">
            <el-select v-model="query.reportStatus" clearable data-testid="filter-status" placeholder="状态"
                       style="width:120px" @change="query.pageNum = 1; loadRows()">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="query.infectiousClass" clearable data-testid="filter-class" placeholder="类别"
                       style="width:110px" @change="query.pageNum = 1; loadRows()">
              <el-option v-for="d in classDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable data-testid="filter-keyword" placeholder="单号/患者/病种"
                      style="width:200px" @keyup.enter="query.pageNum = 1; loadRows()"/>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="overdueFlag" data-testid="filter-overdue" @change="onOverdueChange">只看超时未报
            </el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="btn-search" type="primary" @click="query.pageNum = 1; loadRows()">
              查询
            </el-button>
            <el-button :icon="Refresh" data-testid="btn-reset" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button :loading="notifying" data-testid="btn-notify" type="warning" @click="onNotify">
            <el-icon class="mr-1">
              <Warning/>
            </el-icon>
            超时限催报
          </el-button>
          <el-button v-perm="'emr:infectiousReport:add'" data-testid="btn-create" type="primary" @click="onOpenCreate">
            填卡上报
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="report-table" stripe>
        <el-table-column label="报卡编号" min-width="170" prop="reportNo"/>
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">{{ row.patientName }} / {{ row.patientNo }}</template>
        </el-table-column>
        <el-table-column label="病种" min-width="180">
          <template #default="{ row }">
            <el-tag :type="classTag(row.infectiousClass)" class="mr-1" size="small">{{
                row.infectiousClassText
              }}
            </el-tag>
            {{ row.diseaseName }}
          </template>
        </el-table-column>
        <el-table-column label="科室" min-width="110" prop="visitDeptName"/>
        <el-table-column label="报卡时限" min-width="150">
          <template #default="{ row }">
            <div :class="row.overdue ? 'text-red-500' : ''">
              {{ fmtTime(row.reportDeadline) }}
              <span v-if="row.overdue" class="ml-1">已超时</span>
              <span v-else-if="row.reportStatus === 1" class="ml-1 text-xs text-gray-400">余{{
                  row.remainHours
                }}h</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.reportStatus)">{{ row.reportStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="次数" prop="reportCount" width="60"/>
        <el-table-column label="填卡医生" min-width="90" prop="reportByName"/>
        <el-table-column label="填卡时间" min-width="150">
          <template #default="{ row }">{{ fmtTime(row.reportTime) }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="230">
          <template #default="{ row }">
            <el-button v-if="Number(row.reportStatus) === 1" :data-testid="'audit-' + row.reportNo" size="small"
                       type="primary" @click="onAudit(row)">审核
            </el-button>
            <el-button v-if="[1, 2].includes(Number(row.reportStatus))" :data-testid="'return-' + row.reportNo" plain size="small"
                       type="danger" @click="onReturn(row)">退报
            </el-button>
            <el-button v-if="Number(row.reportStatus) === 2" :data-testid="'direct-' + row.reportNo" size="small"
                       type="success" @click="onDirect(row)">直报
            </el-button>
            <el-button v-if="[1, 4].includes(Number(row.reportStatus))" v-perm="'emr:infectiousReport:edit'"
                       :data-testid="'edit-' + row.reportNo" size="small" @click="onEdit(row)">修改
            </el-button>
            <el-button :data-testid="'detail-' + row.reportNo" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       data-testid="report-pagination" layout="total, prev, pager, next" @current-change="loadRows"/>
      </div>
    </el-card>

    <!-- 填卡弹窗 -->
    <el-dialog v-model="openVisible" :title="openTitle" width="560px">
      <div class="space-y-3">
        <div>
          <div class="text-xs text-gray-400 mb-1">患者</div>
          <PatientSelect v-model="openForm.patientId" data-testid="open-patient" @clear="openForm.registOptions = []; openForm.registId = null"
                         @select="onPatientSelect"/>
        </div>
        <div>
          <div class="text-xs text-gray-400 mb-1">门诊就诊（挂报卡的就诊锚点，住院报卡可留空改填住院ID）</div>
          <el-select v-model="openForm.registId" clearable data-testid="open-regist" filterable placeholder="选择本次就诊"
                     style="width:100%">
            <el-option v-for="r in openForm.registOptions" :key="r.id" :label="`${String(r.visitDate || r.appointDate || '').slice(0, 10)} ${r.deptName || ''} ${r.patientName || ''}`"
                       :value="r.id"/>
          </el-select>
        </div>
        <div>
          <div class="text-xs text-gray-400 mb-1">传染病病种（法定目录）</div>
          <el-select v-model="openForm.diseaseId" :remote-method="loadDiseases" data-testid="open-disease" filterable
                     placeholder="搜索病种名称" remote style="width:100%">
            <el-option v-for="d in diseaseOptions" :key="d.id" :label="`${d.diseaseName}（${d.diseaseCode}，${d.infectiousClassText}，报卡时限${d.deadlineHours}h）`"
                       :value="d.id"/>
          </el-select>
        </div>
        <div>
          <div class="text-xs text-gray-400 mb-1">临床摘要（症状/检验依据，可选）</div>
          <el-input v-model="openForm.clinicalDesc" :rows="3" data-testid="open-clinical" maxlength="500" show-word-limit
                    type="textarea"/>
        </div>
        <div v-if="openTitle === '修改重报'">
          <div class="text-xs text-red-400 mb-1">退报重报：必须说明本次修改内容（将留痕）</div>
          <el-input v-model="openForm.resubmitRemark" :rows="2" data-testid="open-resubmit" maxlength="255"
                    type="textarea"/>
        </div>
      </div>
      <template #footer>
        <el-button v-perm="['emr:infectiousReport:add','emr:infectiousReport:edit']" data-testid="open-save"
                   type="primary" @click="onSave">保存
        </el-button>
        <el-button data-testid="open-cancel" @click="openVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉（已拆至 InfectiousReportDetail.vue，组件按 id 自取详情） -->
    <InfectiousReportDetail v-model="detailVisible" :id="detailId"/>
  </div>
</template>

<script setup>
/**
 * 传染病报告卡（G11，菜单 613 / 路由 /infectious-report）
 *
 * 三个区块：统计卡（含催报）→ 报卡列表（筛选/审核/退报/直报）→ 填卡与详情。
 * 口径：时限与逾期全在后端算（overdue/remainHours），前端只渲染；
 * 已直报的卡是法定留痕凭证（终态禁改），退报重报走同一编辑入口由后端计数。
 */
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Refresh, Search, Warning} from '@element-plus/icons-vue'
import PatientSelect from '@/components/his/PatientSelect.vue'
import request from '@/api/request'
import {getDictDataMapList} from '@/api/system'
import {DICT_TYPE} from '@/lib/dict-cache'
import {
  getDiseaseSelectList,
  getReportDetail,
  getReportListPage,
  getReportStats,
  reportAudit,
  reportDirectReport,
  reportNotifyOverdue,
  reportReturnCard,
  reportUpsert,
} from '@/api/infectiousReport'
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import InfectiousReportDetail from './InfectiousReportDetail.vue'

const ST = {PENDING: 1, AUDITED: 2, DIRECT: 3, RETURNED: 4}
const statusTag = (s) => (Number(s) === ST.PENDING ? 'warning' : Number(s) === ST.AUDITED ? 'primary'
    : Number(s) === ST.DIRECT ? 'success' : Number(s) === ST.RETURNED ? 'danger' : 'info')
const classTag = (c) => (Number(c) === 1 ? 'danger' : Number(c) === 2 ? 'warning' : 'info')
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')

// ---------------- 字典（筛选用；列表文案取后端 *Text） ----------------
const classDict = ref([])
const statusDict = ref([])
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.INFECTIOUS_CLASS},${DICT_TYPE.INFECTIOUS_REPORT_STATUS}`)
    classDict.value = res?.data?.[DICT_TYPE.INFECTIOUS_CLASS] || []
    statusDict.value = res?.data?.[DICT_TYPE.INFECTIOUS_REPORT_STATUS] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

// ---------------- 统计 ----------------
const stats = ref({})
const loadStats = async () => {
  try {
    const res = await getReportStats()
    if (res.code === 200) stats.value = res.data || {}
    
  } catch (e) {
    console.error(e);
    
  }
}
const notifying = ref(false)
const onNotify = async () => {
  notifying.value = true
  try {
    const res = await reportNotifyOverdue()
    if (res.code === 200) ElMessage.success(`超时限催报完成，发送 ${res.data || 0} 条站内信`)
    
    await loadStats()
  } finally {
    notifying.value = false
  }
}

// ---------------- 列表 ----------------
const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  reportStatus: null,
  infectiousClass: null,
  keyword: '',
  overdue: 0
})
const rows = ref([])
const total = ref(0)
const loading = ref(false)
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const loadRows = async () => {
  loading.value = true
  try {
    const res = await getReportListPage({...query})
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = res.data?.total || 0
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false
  }
}
const resetQuery = () => {
  query.reportStatus = null;
  query.infectiousClass = null;
  query.keyword = '';
  query.overdue = 0;
  query.pageNum = 1;
  loadRows()
}
const overdueFlag = ref(false)
const onOverdueChange = (v) => {
  query.overdue = v ? 1 : 0;
  query.pageNum = 1;
  loadRows()
}

// ---------------- 填卡 / 编辑 ----------------
const openVisible = ref(false)
const openTitle = ref('填卡')
const openForm = reactive({
  id: null,
  patientId: null,
  patientName: '',
  registId: null,
  registOptions: [],
  diseaseId: null,
  clinicalDesc: '',
  resubmitRemark: ''
})
const diseaseOptions = ref([])
const loadDiseases = async (keyword) => {
  try {
    const res = await getDiseaseSelectList(keyword || '')
    if (res.code === 200) diseaseOptions.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}
const loadRegists = async (patientId) => {
  openForm.registOptions = []
  openForm.registId = null
  if (!patientId) return
  try {
    const res = await request.get('/appoint/listPage', {params: {patientId, pageNum: 1, pageSize: 20}})
    if (res.code === 200) openForm.registOptions = res.data?.records || []
  } catch (e) {
    console.error(e)
  }
}
const onPatientSelect = async (p) => {
  openForm.patientName = p?.patientName || '';
  await loadRegists(p?.id)
}
const onOpenCreate = async () => {
  openTitle.value = '填卡'
  Object.assign(openForm, {
    id: null,
    patientId: null,
    patientName: '',
    registId: null,
    registOptions: [],
    diseaseId: null,
    clinicalDesc: '',
    resubmitRemark: ''
  })
  await loadDiseases('')
  openVisible.value = true
}
const onEdit = async (row) => {
  openTitle.value = Number(row.reportStatus) === ST.RETURNED ? '修改重报' : '修改报卡'
  const res = await getReportDetail(row.id)
  const c = res.data?.card || row
  Object.assign(openForm, {
    id: c.id, patientId: c.patientId, patientName: c.patientName,
    registId: c.registId, registOptions: [], diseaseId: c.diseaseId,
    clinicalDesc: c.clinicalDesc || '', resubmitRemark: '',
  })
  await loadDiseases('')
  await loadRegists(c.patientId)
  openVisible.value = true
}
const onSave = async () => {
  if (!openForm.patientId) return ElMessage.warning('请选择患者')
  if (!openForm.registId && !openForm.inpId) return ElMessage.warning('门诊就诊与住院记录至少选一项')
  if (!openForm.diseaseId) return ElMessage.warning('请选择传染病病种')
  try {
    const res = await reportUpsert({
      id: openForm.id, patientId: openForm.patientId, registId: openForm.registId,
      diseaseId: openForm.diseaseId, clinicalDesc: openForm.clinicalDesc,
      resubmitRemark: openForm.resubmitRemark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '报卡已保存')
      openVisible.value = false
      await Promise.all([loadRows(), loadStats()])
    } 
  } catch (e) {
    console.error(e)
  }
}

// ---------------- 审核 / 退报 / 直报 ----------------
const onAudit = async (row) => {
  const {value} = await ElMessageBox.prompt(`审核报卡 ${row.reportNo}（${row.diseaseName}，${row.infectiousClassText}）`, '审核意见', {
    inputPlaceholder: '审核意见（可选）', inputValue: row.auditOpinion || '',
    confirmButtonText: '通过', cancelButtonText: '取消',
  }).catch(() => ({value: undefined}))
  if (value === undefined) return
  const res = await reportAudit({id: row.id, opinion: value})
  if (res.code === 200) {
    ElMessage.success('审核通过，可直报');
    await Promise.all([loadRows(), loadStats()])
  } 
}
const onReturn = async (row) => {
  const {value} = await ElMessageBox.prompt(`退回报卡 ${row.reportNo}，修改后可重报（重报次数会留痕）`, '退报原因（必填）', {
    inputPlaceholder: '退报原因', confirmButtonText: '退报', cancelButtonText: '取消',
  }).catch(() => ({value: undefined}))
  if (value === undefined) return
  if (!value || !value.trim()) return ElMessage.warning('退报原因必填')
  const res = await reportReturnCard({id: row.id, reason: value})
  if (res.code === 200) {
    ElMessage.success('已退报');
    await Promise.all([loadRows(), loadStats()])
  } 
}
const onDirect = async (row) => {
  await ElMessageBox.confirm(
      `直报将组装标准报文上报（当前为报文留痕，真实疾控通道未接入）。确认直报 ${row.reportNo}？`,
      '直报确认', {confirmButtonText: '直报', cancelButtonText: '取消'}).catch(() => null)
      .then(async (v) => {
        if (v === null) return
        const res = await reportDirectReport(row.id)
        if (res.code === 200) {
          ElMessage.success('直报完成（报文已留痕）');
          await loadRows();
          await loadStats();
          await showDetail({id: row.id})
        } 
      })
}

// ---------------- 详情（抽屉已拆至 InfectiousReportDetail.vue，组件按 id 自取详情） ----------------
const detailVisible = ref(false)
const detailId = ref(null)
const showDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true
}

onMounted(async () => {
  await Promise.all([loadDicts(), loadStats(), loadRows()])
})
</script>
