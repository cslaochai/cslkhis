<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Search, Refresh, Plus, Check, Close, CircleClose, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTechAuthListPage, techAuthUpsert, approveTechAuth, revokeTechAuth, deleteTechAuth,
  getMyTechAuth, getTechAuthOverrideListPage, confirmTechAuthOverride,
} from '@/api/employeeTechAuth'
import { getEmployeeList } from '@/api/system'
import { loadDictDataMap } from '@/lib/dict-cache'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const activeTab = ref('ledger')
const loading = ref(false)

// 码值权威在 his-common/enums，字典在 sql/155
const categoryOptions = ref<any[]>([])
const authTypeOptions = ref<any[]>([])
const statusOptions = ref<any[]>([])
const overrideStatusOptions = ref<any[]>([])
const levelOptions = [
  { dictValue: '1', dictLabel: '一级手术' },
  { dictValue: '2', dictLabel: '二级手术' },
  { dictValue: '3', dictLabel: '三级手术' },
  { dictValue: '4', dictLabel: '四级手术' },
]
const statusColor: Record<number, string> = {
  1: 'bg-amber-100 text-amber-700',
  2: 'bg-emerald-100 text-emerald-700',
  3: 'bg-slate-200 text-slate-600',
  4: 'bg-red-100 text-red-600',
}
const levelColor: Record<number, string> = {
  1: 'bg-slate-100 text-slate-600',
  2: 'bg-sky-100 text-sky-700',
  3: 'bg-violet-100 text-violet-700',
  4: 'bg-rose-100 text-rose-700',
}
const pillClass = (map: Record<number, string>, v: number | null | undefined) =>
  (v != null && map[v as number]) || 'bg-slate-100 text-slate-500'

// ========== 医生（员工）下拉：授权只能授给院内员工 ==========
const employees = ref<any[]>([])
const employeeOptions = computed(() =>
  employees.value.filter((e: any) => e.status === 1).map((e: any) => ({
    id: String(e.id),
    label: `${e.empName}　${e.deptName || ''}　${e.title || ''}`,
  })),
)

// ========== 台账 ==========
const list = ref<any[]>([])
const filters = ref({
  employeeName: '',
  authCategory: null as number | null,
  techLevel: null as number | null,
  authStatus: null as number | null,
  onlyEffective: false,
})
const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

const loadList = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize }
    if (filters.value.employeeName.trim()) params.employeeName = filters.value.employeeName.trim()
    if (filters.value.authCategory != null) params.authCategory = filters.value.authCategory
    if (filters.value.techLevel != null) params.techLevel = filters.value.techLevel
    if (filters.value.authStatus != null) params.authStatus = filters.value.authStatus
    if (filters.value.onlyEffective) params.onlyEffective = 1
    const res = await getTechAuthListPage(params)
    list.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (e: any) {
    console.error('加载技术授权台账失败:', e)
    ElMessage.error(e?.message || '加载技术授权台账失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = { employeeName: '', authCategory: null, techLevel: null, authStatus: null, onlyEffective: false }
  pagination.value.pageNum = 1
  loadList()
}

// ========== 新增 / 编辑 ==========
const editVisible = ref(false)
const editLoading = ref(false)
const editTitle = ref('登记技术授权')
const emptyForm = () => ({
  id: null as string | null,
  employeeId: '' as string,
  authCategory: 1 as number,
  techLevel: 2 as number,
  authType: 1 as number,
  itemScope: '',
  authBasis: '',
  validFrom: '',
  validUntil: '',
  remark: '',
})
const form = ref(emptyForm())
const indefinite = ref(false)

const openCreate = () => {
  form.value = emptyForm()
  indefinite.value = false
  editTitle.value = '登记技术授权'
  editVisible.value = true
}

const openEdit = (row: any) => {
  form.value = {
    id: row.id,
    employeeId: String(row.employeeId),
    authCategory: row.authCategory,
    techLevel: row.techLevel,
    authType: row.authType || 1,
    itemScope: row.itemScope || '',
    authBasis: row.authBasis || '',
    validFrom: row.validFrom || '',
    validUntil: row.validUntil || '',
    remark: row.remark || '',
  }
  indefinite.value = !row.validUntil
  editTitle.value = `修改授权 - ${row.employeeName}`
  editVisible.value = true
}

const handleSave = async () => {
  if (!form.value.employeeId) return ElMessage.warning('请选择被授权员工')
  if (!form.value.validFrom) return ElMessage.warning('生效日期不能为空')
  editLoading.value = true
  try {
    await techAuthUpsert({
      ...form.value,
      validUntil: indefinite.value ? null : (form.value.validUntil || null),
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    editLoading.value = false
  }
}

// ========== 审批 / 驳回 / 收回 / 删除 ==========
const handleApprove = async (row: any, approved: boolean) => {
  let opinion = ''
  try {
    if (approved) {
      await ElMessageBox.confirm(
        `确认授予「${row.employeeName}」「${row.authCategoryText}」${row.techLevelText}（${row.validFrom} 起${row.validUntil ? ' 至 ' + row.validUntil : ' 长期有效'}）？通过后该医师即可在有效期内独立开展同级及以下操作。`,
        '审批确认', { confirmButtonText: '通过', cancelButtonText: '取消', type: 'warning' },
      )
    } else {
      const { value } = await ElMessageBox.prompt(`驳回「${row.employeeName}」的这条授权申请，理由会记入台账留痕。`, '驳回授权', {
        confirmButtonText: '确认驳回', cancelButtonText: '取消',
        inputPattern: /^.{2,}$/, inputErrorMessage: '驳回理由至少 2 个字', inputPlaceholder: '驳回理由（如：培训学时不足）',
      })
      opinion = value
    }
  } catch {
    return
  }
  try {
    const res = await approveTechAuth({ id: row.id, approved, approveOpinion: opinion })
    ElMessage.success(res.message || '已处理')
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '审批失败')
  }
}

const handleRevoke = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `收回后「${row.employeeName}」立即不能再以「${row.authCategoryText}」开单排台（历史单据不受影响）。`,
      '收回授权', {
        confirmButtonText: '确认收回', cancelButtonText: '取消',
        inputType: 'textarea', inputPattern: /^.{2,}$/, inputErrorMessage: '收回原因至少 2 个字',
        inputPlaceholder: '收回原因（如：年度再授权未通过 / 重大不良事件）',
      },
    )
    const res = await revokeTechAuth({ id: row.id, revokeReason: value })
    ElMessage.success(res.message || '已收回')
    loadList()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message || '收回失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `删除「${row.employeeName}」的这条${row.authCategoryText}申请？只有「待审批/已驳回」可删（本表不留软删）。`,
      '删除授权记录', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' },
    )
    await deleteTechAuth(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message || '删除失败')
  }
}

// ========== 我的授权 ==========
const mineList = ref<any[]>([])
const mineLoading = ref(false)
const loadMine = async () => {
  mineLoading.value = true
  try {
    const res = await getMyTechAuth()
    mineList.value = res.data || []
  } catch (e: any) {
    console.error('加载我的技术授权失败:', e)
    ElMessage.error(e?.message || '加载我的技术授权失败')
  } finally {
    mineLoading.value = false
  }
}

// ========== 急诊越权登记 ==========
const ovList = ref<any[]>([])
const ovLoading = ref(false)
const ovFilters = ref({ employeeName: '', overrideStatus: null as number | null })
const ovPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

const loadOverride = async () => {
  ovLoading.value = true
  try {
    const params: any = { pageNum: ovPagination.value.pageNum, pageSize: ovPagination.value.pageSize }
    if (ovFilters.value.employeeName.trim()) params.employeeName = ovFilters.value.employeeName.trim()
    if (ovFilters.value.overrideStatus != null) params.overrideStatus = ovFilters.value.overrideStatus
    const res = await getTechAuthOverrideListPage(params)
    ovList.value = res.data?.records || []
    ovPagination.value.total = res.data?.total || 0
  } catch (e: any) {
    console.error('加载越权登记失败:', e)
    ElMessage.error(e?.message || '加载越权登记失败')
  } finally {
    ovLoading.value = false
  }
}

const handleConfirmOverride = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认「${row.employeeName}」在 ${row.sourceTypeText} ${row.sourceNo || ''} 上的越权（要求${row.requiredLevelText}，现有${row.heldLevelText}）？`,
      '越权事后确认', {
        confirmButtonText: '确认追认', cancelButtonText: '取消', inputType: 'textarea',
        inputPlaceholder: '确认意见（如：抢救及时，同意追认；已安排补培训）',
      },
    )
    const res = await confirmTechAuthOverride({ id: row.id, confirmOpinion: value })
    ElMessage.success(res.message || '已确认')
    loadOverride()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message || '确认失败')
  }
}

const onTabChange = (name: string) => {
  if (name === 'mine' && mineList.value.length === 0) loadMine()
  if (name === 'override' && ovList.value.length === 0) loadOverride()
}

onMounted(async () => {
  loadList()
  try {
    const map = await loadDictDataMap(
      'his_tech_auth_category,his_tech_auth_type,his_tech_auth_status,his_tech_override_status',
    )
    categoryOptions.value = map.his_tech_auth_category || []
    authTypeOptions.value = map.his_tech_auth_type || []
    statusOptions.value = map.his_tech_auth_status || []
    overrideStatusOptions.value = map.his_tech_override_status || []
  } catch (e) {
    console.error('加载授权字典失败:', e)
  }
  try {
    const res = await getEmployeeList({ status: 1 })
    employees.value = res.data?.records || res.data || []
  } catch (e) {
    console.error('加载员工列表失败:', e)
  }
})
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <el-tabs v-model="activeTab" data-testid="ta-tabs" @tab-change="onTabChange">
        <!-- ============ 授权台账 ============ -->
        <el-tab-pane label="授权台账" name="ledger">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-input v-model="filters.employeeName" placeholder="医师姓名" :prefix-icon="Search" class="!w-40"
              clearable @keyup.enter="loadList" />
            <el-select v-model="filters.authCategory" placeholder="授权类别" clearable class="!w-36"
              :fit-input-width="false" @change="loadList">
              <el-option v-for="d in categoryOptions" :key="d.dictValue" :label="d.dictLabel"
                :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="filters.techLevel" placeholder="级别上限" clearable class="!w-32" @change="loadList">
              <el-option v-for="d in levelOptions" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="filters.authStatus" placeholder="状态" clearable class="!w-28" @change="loadList">
              <el-option v-for="d in statusOptions" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-checkbox v-model="filters.onlyEffective" @change="loadList">只看生效中</el-checkbox>
            <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
            <el-button v-perm="'org:techAuth:add'" type="success" :icon="Plus" data-testid="ta-create-btn" @click="openCreate">登记授权</el-button>
          </div>

          <el-table :data="list" v-loading="loading" data-testid="ta-ledger-table" style="width: 100%">
            <el-table-column prop="employeeName" label="医师" width="100" />
            <el-table-column prop="deptName" label="科室" width="120" show-overflow-tooltip />
            <el-table-column prop="title" label="职称" width="90" />
            <el-table-column label="授权类别" width="110" align="center">
              <template #default="{ row }">{{ row.authCategoryText }}</template>
            </el-table-column>
            <el-table-column label="级别上限" width="100" align="center">
              <template #default="{ row }">
                <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', pillClass(levelColor, row.techLevel)]">
                  {{ row.techLevelText }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="授权方式" width="100" align="center">
              <template #default="{ row }">{{ row.authTypeText }}</template>
            </el-table-column>
            <el-table-column label="限定术式" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <span :class="row.itemScope ? '' : 'text-slate-400'">{{ row.itemScope || '不限' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="有效期" width="200" align="center">
              <template #default="{ row }">
                {{ row.validFrom }} ~ {{ row.validUntil || '长期' }}
                <span v-if="row.effective" class="ml-1 text-xs text-emerald-600">生效中</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="86" align="center">
              <template #default="{ row }">
                <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', pillClass(statusColor, row.authStatus)]">
                  {{ row.authStatusText }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="approverName" label="审批人" width="90" />
            <el-table-column prop="authBasis" label="授权依据" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.canEdit" v-perm="'org:techAuth:add'" type="primary" link :icon="Edit"
                  @click="openEdit(row)">编辑</el-button>
                <el-button v-if="row.canApprove" v-perm="'org:techAuth:edit'" type="success" link :icon="Check"
                  @click="handleApprove(row, true)">通过</el-button>
                <el-button v-if="row.canApprove" v-perm="'org:techAuth:edit'" type="warning" link :icon="Close"
                  @click="handleApprove(row, false)">驳回</el-button>
                <el-button v-if="row.canRevoke" v-perm="'org:techAuth:edit'" type="danger" link :icon="CircleClose"
                  @click="handleRevoke(row)">收回</el-button>
                <el-button v-if="row.canEdit" v-perm="'org:techAuth:delete'" type="danger" link :icon="Delete"
                  @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="list.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
            暂无授权记录：右上「登记授权」按人按类别授到 1~4 级
          </div>
          <div class="mt-4 flex justify-end">
            <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
              :page-sizes="PAGE_SIZES" :total="pagination.total" layout="total, sizes, prev, pager, next"
              @size-change="loadList" @current-change="loadList" />
          </div>
        </el-tab-pane>

        <!-- ============ 我的授权 ============ -->
        <el-tab-pane label="我的授权" name="mine">
          <el-table :data="mineList" v-loading="mineLoading" data-testid="ta-mine-table" style="width: 100%">
            <el-table-column label="授权类别" width="140">
              <template #default="{ row }">{{ row.authCategoryText }}</template>
            </el-table-column>
            <el-table-column label="级别上限" width="120" align="center">
              <template #default="{ row }">
                <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', pillClass(levelColor, row.techLevel)]">
                  {{ row.techLevelText }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="授权方式" width="120" align="center">
              <template #default="{ row }">{{ row.authTypeText }}</template>
            </el-table-column>
            <el-table-column label="限定术式" min-width="160">
              <template #default="{ row }">
                <span :class="row.itemScope ? '' : 'text-slate-400'">{{ row.itemScope || '不限' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="有效期" width="220" align="center">
              <template #default="{ row }">{{ row.validFrom }} ~ {{ row.validUntil || '长期' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120" align="center">
              <template #default="{ row }">
                <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', pillClass(statusColor, row.authStatus)]">
                  {{ row.authStatusText }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="mineList.length === 0 && !mineLoading" class="py-12 text-center text-sm text-slate-400">
            你还没有任何技术授权记录，请联系医务科/系统管理员登记
          </div>
        </el-tab-pane>

        <!-- ============ 越权登记 ============ -->
        <el-tab-pane label="急诊越权登记" name="override">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-input v-model="ovFilters.employeeName" placeholder="医师姓名" :prefix-icon="Search" class="!w-40"
              clearable @keyup.enter="loadOverride" />
            <el-select v-model="ovFilters.overrideStatus" placeholder="确认状态" clearable class="!w-36"
              :fit-input-width="false" @change="loadOverride">
              <el-option v-for="d in overrideStatusOptions" :key="d.dictValue" :label="d.dictLabel"
                :value="Number(d.dictValue)" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadOverride">查询</el-button>
          </div>
          <el-table :data="ovList" v-loading="ovLoading" data-testid="ta-override-table" style="width: 100%">
            <el-table-column label="来源单据" width="200">
              <template #default="{ row }">{{ row.sourceTypeText }} {{ row.sourceNo || '' }}</template>
            </el-table-column>
            <el-table-column prop="employeeName" label="越权医师" width="110" />
            <el-table-column label="类别" width="110" align="center">
              <template #default="{ row }">{{ row.authCategoryText }}</template>
            </el-table-column>
            <el-table-column label="要求 / 现有" width="180" align="center">
              <template #default="{ row }">
                <span class="font-medium text-rose-600">{{ row.requiredLevelText }}</span>
                <span class="mx-1 text-slate-400">/</span>
                <span>{{ row.heldLevelText }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="越权原因" min-width="220" show-overflow-tooltip />
            <el-table-column prop="occurTime" label="发生时间" width="160" />
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium',
                  row.overrideStatus === 2 ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700']">
                  {{ row.overrideStatusText }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="supervisorName" label="确认人" width="100" />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.canConfirm" v-perm="'org:techAuth:override'" type="success" link :icon="Check"
                  @click="handleConfirmOverride(row)">确认</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="ovList.length === 0 && !ovLoading" class="py-12 text-center text-sm text-slate-400">
            暂无越权登记：说明目前没有「级别不够但急诊先做了」的记录
          </div>
          <div class="mt-4 flex justify-end">
            <el-pagination v-model:current-page="ovPagination.pageNum" v-model:page-size="ovPagination.pageSize"
              :page-sizes="PAGE_SIZES" :total="ovPagination.total" layout="total, sizes, prev, pager, next"
              @size-change="loadOverride" @current-change="loadOverride" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ============ 登记 / 修改弹框 ============ -->
    <el-dialog v-model="editVisible" :title="editTitle" width="680px" data-testid="ta-edit-dialog">
      <el-form :model="form" label-width="96px">
        <el-form-item label="被授权人" required>
          <el-select v-model="form.employeeId" placeholder="搜索并选择院内员工" filterable :fit-input-width="false"
            class="!w-full" :disabled="!!form.id">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="e.label" :value="e.id" />
          </el-select>
        </el-form-item>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="授权类别" required>
            <el-select v-model="form.authCategory" class="!w-full" :fit-input-width="false">
              <el-option v-for="d in categoryOptions" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="级别上限" required>
            <el-select v-model="form.techLevel" class="!w-full">
              <el-option v-for="d in levelOptions" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="授权方式">
            <el-select v-model="form.authType" class="!w-full" :fit-input-width="false">
              <el-option v-for="d in authTypeOptions" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="限定术式">
            <el-input v-model="form.itemScope" placeholder="术式编码，逗号分隔；留空=不限" />
          </el-form-item>
          <el-form-item label="生效日期" required>
            <el-date-picker v-model="form.validFrom" type="date" value-format="YYYY-MM-DD" placeholder="yyyy-MM-dd"
              class="!w-full" :clearable="false" />
          </el-form-item>
          <el-form-item label="有效期至">
            <el-date-picker v-model="form.validUntil" type="date" value-format="YYYY-MM-DD" placeholder="留空=长期"
              class="!w-full" :disabled="indefinite" />
          </el-form-item>
        </div>
        <el-checkbox v-model="indefinite" class="mb-3">长期有效（不过期）</el-checkbox>
        <el-form-item label="授权依据">
          <el-input v-model="form.authBasis" type="textarea" :rows="2"
            placeholder="技术准入评价结论 / 培训考核 / 累计操作例数等，审批时填即可" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
