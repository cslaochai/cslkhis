<script setup lang="ts">
/**
 * 慢特病医保备案（菜单 1011，sql/163；sql/188 由「慢特病人员备案」更名 —— 加「医保」二字与
 * 门诊的「慢病建档与认定」（菜单 208，临床口径）区分：本页只管待遇资格，不管临床建档）
 *
 * 这张台账只回答一个问题：「这个人的门特资格是谁办的、还在不在有效期内」。
 * 经办人留空由服务端回填当前登录人（谁点保存就是谁办的）；改成外部机构经办人必须在备注写明原因。
 * 过期是展示态（服务端按当天现算，库里只有 1有效/2已注销/3已驳回），页面据此提示续备；
 * 注销/驳回是终态不可逆，要改内容只能另起新单——所以这两个动作只做状态流转，不提供编辑。
 */
import {ref, reactive, computed, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search, Refresh, Plus, View, Edit} from '@element-plus/icons-vue'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {getDepartmentSelectList} from '@/api/system'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {
  getChronicCatalogList, getChronicCatalogSelectList,
  getChronicRegList, getChronicRegDetail, getChronicRegSummary, upsertChronicReg, cancelChronicReg, rejectChronicReg
} from '@/api/insuranceChronic'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const route = useRoute()

// 库里只有三态，4-已过期 是服务端现算的展示态，只用于标签配色与筛选提示
const ST_VALID = 1, ST_CANCELLED = 2, ST_REJECTED = 3, ST_EXPIRED = 4

const CHRONIC_DICT_TYPES = [DICT_TYPE.CHRONIC_DISEASE_TYPE, DICT_TYPE.YB_CHRONIC_STATUS]
const dicts = ref<Record<string, any[]>>({})
const dictOptions = (type: string) => dicts.value[type] || []
const dictText = (type: string, value: any) => dictLabelText(dicts.value[type], value)

// ==================== 科室下拉（诊断科室要全院范围） ====================
const deptOptions = ref<any[]>([])
const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({scope: 'ALL'})
    deptOptions.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error?.message || '科室候选加载失败')
  }
}
const deptNameOf = (id: any) => deptOptions.value.find((d) => String(d.id) === String(id))?.deptName || ''

// ==================== 汇总（后端 SQL 聚合，禁止前端自算） ====================
const summary = ref<any>({})
const loadSummary = async () => {
  try {
    const res: any = await getChronicRegSummary()
    summary.value = res.data || {}
  } catch (error: any) {
    ElMessage.error(error?.message || '备案汇总加载失败')
  }
}

// ==================== 备案台账 ====================
const regLoading = ref(false)
const regRows = ref<any[]>([])
const regQuery = reactive({
  regStatus: undefined as number | undefined,
  diseaseType: undefined as number | undefined,
  catalogId: undefined as string | undefined,
  onlyExpired: false,
  keyword: ''
})
const regPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadRegs = async () => {
  regLoading.value = true
  try {
    const res: any = await getChronicRegList({
      ...regQuery, onlyExpired: regQuery.onlyExpired || undefined,
      pageNum: regPage.pageNum, pageSize: regPage.pageSize
    })
    regRows.value = res.data?.records || []
    regPage.total = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error?.message || '备案台账加载失败')
  } finally {
    regLoading.value = false
  }
}

const resetRegQuery = () => {
  regQuery.regStatus = undefined
  regQuery.diseaseType = undefined
  regQuery.catalogId = undefined
  regQuery.onlyExpired = false
  regQuery.keyword = ''
  regPage.pageNum = 1
  loadRegs()
}

const statusTagType = (s: number) =>
  s === ST_VALID ? 'success' : s === ST_EXPIRED ? 'danger' : s === ST_REJECTED ? 'warning' : 'info'
const statusText = (row: any) => {
  if (!row) return ''
  return row.displayStatus === ST_EXPIRED ? '已过期' : dictText(DICT_TYPE.YB_CHRONIC_STATUS, row.regStatus)
}
const validPeriod = (row: any) =>
  `${row.validStart} ~ ${row.longTerm ? '长期' : row.validEnd || ''}`

// 终态不可逆：只有「有效」的单能改、能注销、能驳回
const canEditReg = (row: any) => row.regStatus === ST_VALID
const canCancelReg = (row: any) => row.regStatus === ST_VALID
const canRejectReg = (row: any) => row.regStatus === ST_VALID

// ==================== 病种目录候选（备案表单下拉 + 筛选） ====================
const catalogOptions = ref<any[]>([])
const loadCatalogOptions = async () => {
  try {
    const res: any = await getChronicCatalogSelectList()
    catalogOptions.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error?.message || '病种候选加载失败')
  }
}

// ==================== 备案 新建/编辑 ====================
const regFormVisible = ref(false)
const regSubmitting = ref(false)
const emptyReg = () => ({
  id: null as any, regNo: '', patientId: undefined as any, patientName: '',
  catalogId: undefined as any, certifyDeptId: undefined as any, certifyDeptName: '',
  certifyDoctorName: '', certifyDate: '', certifyBasis: '',
  registerDeptName: '', registerEmpName: '', registerDate: '',
  validStart: '', validEnd: '', longTerm: false, remark: ''
})
const regForm = ref<any>(emptyReg())

// 本地日期字符串：toISOString 走 UTC，早上 8 点前会把「今天」写成昨天
const fmtDate = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
const today = () => fmtDate(new Date())

const openRegCreate = async () => {
  regForm.value = emptyReg()
  await loadCatalogOptions()
  regForm.value.registerDate = today()
  regForm.value.certifyDate = today()
  regFormVisible.value = true
}

const openRegEdit = async (row: any) => {
  try {
    const res: any = await getChronicRegDetail(row.id)
    const d = res.data || {}
    await loadCatalogOptions()
    regForm.value = {
      ...emptyReg(), ...d,
      id: row.id,
      patientId: d.patientId ? String(d.patientId) : undefined,
      catalogId: d.catalogId ? String(d.catalogId) : undefined,
      certifyDeptId: d.certifyDeptId ? String(d.certifyDeptId) : undefined,
      // 长期单出参 validEnd 为 null，el-date-picker 要的是空串而不是 null
      validEnd: d.validEnd || '',
      longTerm: !d.validEnd
    }
    regFormVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.message || '备案详情加载失败')
  }
}

const onRegPatient = (p: any) => {
  if (p) regForm.value.patientName = p.patientName
}

const onRegDept = (id: any) => {
  regForm.value.certifyDeptName = id ? deptNameOf(id) : ''
}

/** 选病种时把目录的默认有效期带出来，长期病种就留空 */
const onRegCatalog = (id: any) => {
  const c = catalogOptions.value.find((x) => String(x.id) === String(id))
  if (!c) return
  if (c.defaultValidMonths) {
    regForm.value.longTerm = false
    if (regForm.value.validStart) fillValidEndByMonths(c.defaultValidMonths)
  } else {
    regForm.value.longTerm = true
    regForm.value.validEnd = ''
  }
}

/** 先生效日后选病种还是先选病种都要能带出默认终止日，所以两边都挂一次 */
const onRegValidStart = () => {
  const c = catalogOptions.value.find((x) => String(x.id) === String(regForm.value.catalogId))
  if (c?.defaultValidMonths && regForm.value.validStart && !regForm.value.longTerm && !regForm.value.validEnd) {
    fillValidEndByMonths(c.defaultValidMonths)
  }
}

const fillValidEndByMonths = (months: number) => {
  const start = new Date(`${regForm.value.validStart}T00:00:00`)
  start.setMonth(start.getMonth() + Number(months))
  regForm.value.validEnd = fmtDate(start)
}

const onRegTermChange = (longTerm: any) => {
  if (longTerm) regForm.value.validEnd = ''
}

const submitReg = async () => {
  const f = regForm.value
  if (!f.patientId || !f.catalogId || !f.certifyDate || !f.certifyBasis || !f.registerDate || !f.validStart) {
    ElMessage.warning('患者、病种、诊断日期、诊断依据、备案日期、待遇生效日都要填')
    return
  }
  // 后端只在「新建且姓名不是当前登录人」时强制备注，这里提前拦一遍省一次 400
  if (!f.id && f.registerEmpName && !f.remark) {
    ElMessage.warning('改成本院之外的经办人时，备注要写明原因（如「长沙市医保中心窗口张XX代办」）')
    return
  }
  regSubmitting.value = true
  try {
    await upsertChronicReg({
      id: f.id || undefined, patientId: f.patientId, catalogId: f.catalogId,
      certifyDeptId: f.certifyDeptId || undefined, certifyDeptName: f.certifyDeptName || undefined,
      certifyDoctorName: f.certifyDoctorName || undefined, certifyDate: f.certifyDate, certifyBasis: f.certifyBasis,
      registerDeptName: f.registerDeptName || undefined, registerEmpName: f.registerEmpName || undefined,
      registerDate: f.registerDate, validStart: f.validStart, validEnd: f.longTerm ? undefined : (f.validEnd || undefined),
      remark: f.remark || undefined
    })
    ElMessage.success(f.id ? '备案已更新' : '备案已登记')
    regFormVisible.value = false
    await Promise.all([loadRegs(), loadSummary()])
  } catch (error: any) {
    // 「同一患者同一病种已有有效备案」这类判定只在后端有据（要查历史单号与待遇期），
    // 拒绝理由必须原样弹出来，否则用户只看到弹窗不动
    ElMessage.error(error?.message || '备案保存失败')
  } finally {
    regSubmitting.value = false
  }
}

// ==================== 备案详情（只读） ====================
const regDetailVisible = ref(false)
const regDetail = ref<any>(null)

const openRegDetail = async (row: any) => {
  try {
    const res: any = await getChronicRegDetail(row.id)
    regDetail.value = res.data || null
    regDetailVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.message || '备案详情加载失败')
  }
}

// ==================== 注销 / 驳回 ====================
const terminalReg = (row: any, kind: 'cancel' | 'reject') => {
  const label = kind === 'cancel' ? '注销' : '驳回'
  const hint = kind === 'cancel'
    ? `注销备案「${row.regNo}」？注销后该患者此病种不能再走门特，终态不可逆，需重新备案。`
    : `驳回备案「${row.regNo}」？驳回是认定这次备案不成立，终态不可逆，补材料后另起新单。`
  ElMessageBox.prompt(hint, `${label}确认`, {
    inputPlaceholder: `${label}原因（必填）`,
    inputValidator: (v: string) => (v && v.trim() ? true : `${label}原因不能为空`),
    type: 'warning'
  }).then(async ({value}) => {
    const payload = {id: row.id, reason: value.trim()}
    try {
      await (kind === 'cancel' ? cancelChronicReg(payload) : rejectChronicReg(payload))
      ElMessage.success(`已${label}`)
      await Promise.all([loadRegs(), loadSummary()])
    } catch (error: any) {
      ElMessage.error(error?.message || `${label}失败`)
    }
  }).catch(() => {})
}

// ==================== 病种目录 ====================
// 台账筛选里按病种过滤要带停用病种的历史单，候选这里单独捞全量（一次性，不是分页查询）
const catFilterOptions = ref<any[]>([])
const loadCatFilterOptions = async () => {
  try {
    const res: any = await getChronicCatalogList({pageNum: 1, pageSize: 200})
    catFilterOptions.value = res.data?.records || []
  } catch (error: any) {
    ElMessage.error(error?.message || '病种筛选候选加载失败')
  }
}

/** 待续备名单：点汇总卡片直接把台账筛成「有效但已过期」 */
const filterExpiredOnly = () => {
  regQuery.regStatus = undefined
  regQuery.onlyExpired = true
  regPage.pageNum = 1
  loadRegs()
}

const expiringSoonCount = computed(() =>
  regRows.value.filter((r) => r.regStatus === ST_VALID && r.remainDays != null && r.remainDays >= 0 && r.remainDays <= 30).length
)

onMounted(async () => {
  dicts.value = await loadDictDataMap(CHRONIC_DICT_TYPES.join(','))
  // 「门诊慢特病病种目录」页的「看备案」跳过来时带 catalogId（sql/179 拆页后由路由参数承接，
  // 替代原先的页签内切 tab）
  const q = route.query.catalogId
  if (q != null && q !== '') regQuery.catalogId = String(q)
  await Promise.all([loadDepts(), loadRegs(), loadSummary(), loadCatalogOptions(), loadCatFilterOptions()])
})
</script>

<template>
  <div data-testid="yb-chronic-page">
    <!-- 台账汇总：数字全部来自后端 SQL 聚合 -->
    <el-row :gutter="12" class="mb-3">
      <el-col :span="6">
        <el-card shadow="never" class="!rounded-lg" data-testid="chronic-sum-valid">
          <div class="text-xs text-slate-500">在有效期的备案</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.validCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="!rounded-lg cursor-pointer" data-testid="chronic-sum-expired" @click="filterExpiredOnly">
          <div class="text-xs text-slate-500">已过期待续备</div>
          <div class="text-2xl font-semibold mt-1" :class="(summary.expiredCount ?? 0) > 0 ? 'text-red-600' : ''">
            {{ summary.expiredCount ?? 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="!rounded-lg">
          <div class="text-xs text-slate-500">已注销</div>
          <div class="text-2xl font-semibold mt-1 text-slate-500">{{ summary.cancelledCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="!rounded-lg">
          <div class="text-xs text-slate-500">已驳回</div>
          <div class="text-2xl font-semibold mt-1 text-amber-600">{{ summary.rejectedCount ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <!-- ==================== 备案台账筛选 ==================== -->
      <div class="flex items-start justify-between gap-4">
        <el-form :model="regQuery" inline @submit.prevent>
          <el-form-item label="状态">
            <el-select v-model="regQuery.regStatus" placeholder="全部状态" clearable style="width: 140px" data-testid="chronic-filter-status">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_CHRONIC_STATUS)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="regQuery.diseaseType" placeholder="全部类别" clearable style="width: 130px">
              <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="病种">
            <el-select v-model="regQuery.catalogId" placeholder="全部病种" clearable filterable style="width: 210px" data-testid="chronic-filter-catalog">
              <el-option v-for="c in catFilterOptions" :key="c.id" :label="`${c.diseaseCode} ${c.diseaseName}`" :value="String(c.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="regQuery.onlyExpired" data-testid="chronic-only-expired">只看已过期待续备</el-checkbox>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="regQuery.keyword" placeholder="单号/患者/病种/经办人" clearable style="width: 220px" data-testid="chronic-keyword"
                      @keyup.enter="regPage.pageNum = 1; loadRegs()"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" data-testid="chronic-search-btn" @click="regPage.pageNum = 1; loadRegs()">查询</el-button>
            <el-button :icon="Refresh" @click="resetRegQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span v-if="expiringSoonCount > 0" class="text-xs text-amber-600 mr-2" data-testid="chronic-expiring-hint">
            本页 {{ expiringSoonCount }} 条 30 天内到期
          </span>
          <el-button type="primary" :icon="Plus" v-perm="'finance:insuranceChronic:add'" data-testid="chronic-create-btn" @click="openRegCreate">登记备案</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <!-- ==================== 备案台账 ==================== -->
      <el-table v-loading="regLoading" :data="regRows" style="width: 100%" data-testid="chronic-table"
                stripe :max-height="tableMaxHeight" @row-click="openRegDetail">
            <el-table-column prop="regNo" label="备案单号" width="150" class-name="font-mono"/>
            <el-table-column label="患者" width="150" show-overflow-tooltip>
              <template #default="{row}">
                <div>{{ row.patientName }}</div>
                <div class="text-xs text-slate-400">{{ row.patientNo }}</div>
              </template>
            </el-table-column>
            <el-table-column label="病种" min-width="200" show-overflow-tooltip>
              <template #default="{row}">
                <el-tag size="small" :type="row.diseaseType === 2 ? 'danger' : 'info'" class="mr-1">
                  {{ dictText(DICT_TYPE.CHRONIC_DISEASE_TYPE, row.diseaseType) }}
                </el-tag>
                <span>{{ row.diseaseName }}</span>
                <div class="text-xs text-slate-400 font-mono">{{ row.diseaseCode }}</div>
              </template>
            </el-table-column>
            <el-table-column label="诊断" width="160" show-overflow-tooltip>
              <template #default="{row}">
                <div class="text-xs">{{ row.certifyDeptName || '—' }} / {{ row.certifyDoctorName || '—' }}</div>
                <div class="text-xs text-slate-400">{{ row.certifyDate }}</div>
              </template>
            </el-table-column>
            <el-table-column label="备案经办人" width="130" show-overflow-tooltip>
              <template #default="{row}">
                <div data-testid="chronic-emp-name">{{ row.registerEmpName }}</div>
                <div class="text-xs text-slate-400">{{ row.registerDeptName || '—' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="待遇有效期" width="180">
              <template #default="{row}">
                <div class="text-xs">{{ validPeriod(row) }}</div>
                <el-tag v-if="row.displayStatus === ST_EXPIRED" type="danger" size="small" data-testid="chronic-expired-tag">
                  已过期 {{ Math.abs(row.remainDays) }} 天
                </el-tag>
                <el-tag v-else-if="row.longTerm" type="success" size="small" class="!ml-0">长期</el-tag>
                <span v-else-if="row.remainDays != null" class="text-xs"
                      :class="row.remainDays <= 30 ? 'text-amber-600' : 'text-slate-400'">剩 {{ row.remainDays }} 天</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="statusTagType(row.displayStatus)" size="small" :data-testid="`chronic-status-${row.regNo}`">
                  {{ statusText(row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{row}">
                <el-button size="small" plain :icon="View" :data-testid="`chronic-view-${row.regNo}`" @click.stop="openRegDetail(row)">详情</el-button>
                <el-button v-if="canEditReg(row)" size="small" type="primary" plain :icon="Edit" v-perm="'finance:insuranceChronic:add'"
                           :data-testid="`chronic-edit-${row.regNo}`" @click.stop="openRegEdit(row)">编辑</el-button>
                <el-button v-if="canRejectReg(row)" size="small" type="warning" plain v-perm="'finance:insuranceChronic:cancel'"
                           :data-testid="`chronic-reject-${row.regNo}`" @click.stop="terminalReg(row, 'reject')">驳回</el-button>
                <el-button v-if="canCancelReg(row)" size="small" type="danger" plain v-perm="'finance:insuranceChronic:cancel'"
                           :data-testid="`chronic-cancel-${row.regNo}`" @click.stop="terminalReg(row, 'cancel')">注销</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div ref="footerRef" class="list-footer flex items-center justify-end">
            <el-pagination v-model:current-page="regPage.pageNum" v-model:page-size="regPage.pageSize"
                           :total="regPage.total" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next, jumper"
                           data-testid="chronic-pagination" @current-change="loadRegs" @size-change="regPage.pageNum = 1; loadRegs()"/>
          </div>
    </el-card>

    <!-- ==================== 备案 新建/编辑 ==================== -->
    <el-dialog v-model="regFormVisible" :title="regForm.id ? `修改备案 ${regForm.regNo}` : '登记慢特病备案'" width="820px" data-testid="chronic-form-dialog">
      <el-alert v-if="!regForm.id" type="info" :closable="false" class="mb-3"
                title="经办人留空＝记为当前登录人（谁点保存就是谁办的备案）；填别人姓名时备注必须写明原因"/>
      <el-form :model="regForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="患者" required>
              <PatientSelect v-model="regForm.patientId" @select="onRegPatient" data-testid="chronic-form-patient"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="病种" required>
              <el-select v-model="regForm.catalogId" filterable style="width: 100%" @change="onRegCatalog" data-testid="chronic-form-catalog">
                <el-option v-for="c in catalogOptions" :key="c.id"
                           :label="`${c.diseaseCode} ${c.diseaseName}`" :value="String(c.id)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="诊断科室">
              <el-select v-model="regForm.certifyDeptId" filterable clearable style="width: 100%" @change="onRegDept" data-testid="chronic-form-dept">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="诊断医师">
              <el-input v-model="regForm.certifyDoctorName" placeholder="开诊断的那位医师"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="诊断日期" required data-testid="chronic-form-certify-date">
              <el-date-picker v-model="regForm.certifyDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备案日期" required data-testid="chronic-form-register-date">
              <el-date-picker v-model="regForm.registerDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="待遇生效日" required data-testid="chronic-form-valid-start">
              <el-date-picker v-model="regForm.validStart" type="date" value-format="YYYY-MM-DD" style="width: 100%"
                              @change="onRegValidStart"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="待遇终止日" data-testid="chronic-form-valid-end">
              <div class="flex items-center gap-2 w-full">
                <el-date-picker v-model="regForm.validEnd" type="date" value-format="YYYY-MM-DD" style="flex: 1"
                                :disabled="regForm.longTerm" placeholder="留空＝长期"/>
                <el-checkbox v-model="regForm.longTerm" @change="onRegTermChange" data-testid="chronic-form-long-term">长期</el-checkbox>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备案机构">
              <el-input v-model="regForm.registerDeptName" placeholder="如 长沙市医疗保障局 / 本院医保科" data-testid="chronic-form-register-dept"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备案经办人">
              <el-input v-model="regForm.registerEmpName" placeholder="留空＝当前登录人" data-testid="chronic-form-register-emp"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="诊断依据" required>
          <el-input v-model="regForm.certifyBasis" type="textarea" :rows="3"
                    placeholder="病历摘要 / 检验检查结果 / 出院小结——医保稽核时要拿这句对材料" data-testid="chronic-form-basis"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="regForm.remark" type="textarea" :rows="2"
                    placeholder="外部机构代办时在这里写明，如「长沙市医保中心窗口张XX代办」" data-testid="chronic-form-remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="regFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="regSubmitting" data-testid="chronic-form-save" @click="submitReg">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 备案详情（只读） ==================== -->
    <el-dialog v-model="regDetailVisible" :title="`慢特病备案 ${regDetail?.regNo || ''}`" width="820px" data-testid="chronic-detail-dialog">
      <el-form :model="regDetail" label-width="110px" disabled>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="状态">
            <el-tag :type="statusTagType(regDetail?.displayStatus)" size="small">{{ statusText(regDetail) }}</el-tag>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="患者">{{ regDetail?.patientName }}（{{ regDetail?.patientNo }}）</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="医保卡号">{{ regDetail?.medicalInsuranceNoMasked || '—' }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="病种">{{ regDetail?.diseaseCode }} {{ regDetail?.diseaseName }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类别">{{ dictText(DICT_TYPE.CHRONIC_DISEASE_TYPE, regDetail?.diseaseType) }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="诊断科室">{{ regDetail?.certifyDeptName || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="诊断医师">{{ regDetail?.certifyDoctorName || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="诊断日期">{{ regDetail?.certifyDate }}</el-form-item></el-col>
          <el-col :span="24"><el-form-item label="诊断依据">{{ regDetail?.certifyBasis }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="备案机构">{{ regDetail?.registerDeptName || '—' }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="备案经办人">{{ regDetail?.registerEmpName }}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="备案日期">{{ regDetail?.registerDate }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="待遇有效期">{{ regDetail ? validPeriod(regDetail) : '' }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="剩余天数">
            {{ regDetail?.longTerm ? '长期有效' : (regDetail?.remainDays != null ? `${regDetail.remainDays} 天` : '—') }}
          </el-form-item></el-col>
          <el-col v-if="regDetail?.cancelReason" :span="24"><el-form-item label="注销原因">{{ regDetail.cancelReason }}</el-form-item></el-col>
          <el-col v-if="regDetail?.cancelBy" :span="24"><el-form-item label="注销经办">{{ regDetail.cancelBy }} / {{ regDetail.cancelTime }}</el-form-item></el-col>
          <el-col v-if="regDetail?.rejectReason" :span="24"><el-form-item label="驳回原因">{{ regDetail.rejectReason }}</el-form-item></el-col>
          <el-col v-if="regDetail?.rejectBy" :span="24"><el-form-item label="驳回经办">{{ regDetail.rejectBy }} / {{ regDetail.rejectTime }}</el-form-item></el-col>
          <el-col v-if="regDetail?.remark" :span="24"><el-form-item label="备注">{{ regDetail.remark }}</el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="regDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>
