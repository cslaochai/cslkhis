<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRevisitFeePolicyList,
  revisitFeePolicyUpsert,
  deleteRevisitFeePolicy,
} from '@/api/revisitPolicy'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import { REVISIT_MATCH_OPTIONS, REVISIT_MATCH_ANY, revisitMatchLabel } from '@/lib/revisitPolicy'

interface PolicyRow {
  id: string
  policyName: string
  revisitSource: number
  sameDoctor: number
  sameDept: number
  withinDays: number | null
  chargeMode: number
  priority: number
  status: number
  remark?: string
}

const loading = ref(true)
const rows = ref<PolicyRow[]>([])
const query = reactive({
  policyName: '',
  revisitSource: null as number | null,
  chargeMode: null as number | null,
  status: null as number | null,
})
const pagination = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

// 来源与收费方式都是库里字典（sql/121）：文案漂移会让前台与收费处对不上，故不写死
const sourceOptions = ref<any[]>([])
const chargeModeOptions = ref<any[]>([])
// 策略表允许 revisit_source=0=「任意复诊来源」，那是配置的表达能力而不是业务码值，
// 库里字典没有它，由本页面补一个选项（判定语义在后端 RevisitFeePolicyServiceImpl）
const SOURCE_ANY = { dictValue: '0', dictLabel: '不限（任意复诊来源）' }
const listSourceOptions = computed(() => [SOURCE_ANY, ...sourceOptions.value])

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

const formVisible = ref(false)
const formSubmitting = ref(false)
const form = reactive({
  id: null as string | null,
  policyName: '',
  revisitSource: null as number | null,
  sameDoctor: REVISIT_MATCH_ANY,
  sameDept: REVISIT_MATCH_ANY,
  withinDays: null as number | null,
  chargeMode: null as number | null,
  priority: 100,
  status: 1,
  remark: '',
})

async function loadDicts() {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.REVISIT_SOURCE},${DICT_TYPE.REVISIT_CHARGE_MODE}`)
    if (res.code === 200 && res.data) {
      sourceOptions.value = res.data[DICT_TYPE.REVISIT_SOURCE] || []
      chargeModeOptions.value = res.data[DICT_TYPE.REVISIT_CHARGE_MODE] || []
    }
  } catch (e) {
    console.error('加载复诊字典失败', e)
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getRevisitFeePolicyList({
      policyName: query.policyName || undefined,
      revisitSource: query.revisitSource === null ? undefined : query.revisitSource,
      chargeMode: query.chargeMode === null ? undefined : query.chargeMode,
      status: query.status === null ? undefined : query.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    const data = res.data || {}
    rows.value = data.records || []
    pagination.total = Number(data.total || 0)
  } catch (e: any) {
    rows.value = []
    pagination.total = 0
    ElMessage.error(e?.message || '加载复诊收费策略失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadList()
}

function handleReset() {
  query.policyName = ''
  query.revisitSource = null
  query.chargeMode = null
  query.status = null
  handleSearch()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadList()
}

function handleCurrentChange(page: number) {
  pagination.pageNum = page
  loadList()
}

function openCreate() {
  form.id = null
  form.policyName = ''
  form.revisitSource = null
  form.sameDoctor = REVISIT_MATCH_ANY
  form.sameDept = REVISIT_MATCH_ANY
  form.withinDays = null
  form.chargeMode = null
  form.priority = 100
  form.status = 1
  form.remark = ''
  formVisible.value = true
}

function openEdit(row: PolicyRow) {
  form.id = row.id
  form.policyName = row.policyName
  form.revisitSource = row.revisitSource
  form.sameDoctor = Number(row.sameDoctor ?? 0)
  form.sameDept = Number(row.sameDept ?? 0)
  form.withinDays = row.withinDays === null || row.withinDays === undefined ? null : Number(row.withinDays)
  form.chargeMode = row.chargeMode
  form.priority = Number(row.priority ?? 100)
  form.status = row.status
  form.remark = row.remark || ''
  formVisible.value = true
}

async function submitForm() {
  if (!form.policyName.trim()) {
    ElMessage.warning('请填写策略名称')
    return
  }
  if (form.revisitSource === null) {
    ElMessage.warning('请选择适用的复诊来源')
    return
  }
  if (form.chargeMode === null) {
    ElMessage.warning('请选择收费方式')
    return
  }
  formSubmitting.value = true
  try {
    await revisitFeePolicyUpsert({
      id: form.id,
      policyName: form.policyName.trim(),
      revisitSource: form.revisitSource,
      sameDoctor: form.sameDoctor,
      sameDept: form.sameDept,
      // 留空 = 不限间隔；不能兜 0 —— 0 是「只允许同一天」，两者差得很远
      withinDays: form.withinDays,
      chargeMode: form.chargeMode,
      priority: form.priority,
      status: form.status,
      remark: form.remark,
    })
    ElMessage.success(form.id ? '修改成功' : '新增成功')
    formVisible.value = false
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    formSubmitting.value = false
  }
}

async function handleDelete(row: PolicyRow) {
  try {
    await ElMessageBox.confirm(
        `确认删除策略「${row.policyName}」？删除后原本命中它的复诊号按「全额收费」处理（无策略即不免钱）。`,
        '删除复诊收费策略',
        { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    await deleteRevisitFeePolicy(row.id)
    ElMessage.success('删除成功')
    if (rows.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1
    }
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(async () => {
  await loadDicts()
  await loadList()
})
</script>

<template>
  <div>
    <div class="mb-3 flex items-start justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">复诊收费策略</h1>
        <p class="mt-1 text-sm text-slate-500">
          决定每种复诊号收不收挂号费/诊查费；一次只命中一条（优先级数值小者优先），匹配不到任何策略则全额收费
        </p>
      </div>
      <el-button v-perm="'opd:revisitPolicy:add'" type="primary" :icon="Plus" @click="openCreate">新增策略</el-button>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="策略名称">
            <el-input
                v-model="query.policyName"
                placeholder="策略名称"
                :prefix-icon="Search"
                clearable
                class="!w-56"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="复诊来源">
            <el-select v-model="query.revisitSource" placeholder="复诊来源" clearable class="!w-44">
              <el-option v-for="o in listSourceOptions" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="收费方式">
            <el-select v-model="query.chargeMode" placeholder="收费方式" clearable class="!w-40">
              <el-option v-for="o in chargeModeOptions" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable class="!w-28">
              <el-option v-for="o in STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 条策略
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight">
        <el-table-column prop="policyName" label="策略名称" min-width="170" />
        <el-table-column label="适用来源" min-width="150">
          <template #default="{ row }">
            {{ row.revisitSource === 0 ? SOURCE_ANY.dictLabel : dictLabelText(sourceOptions, row.revisitSource) }}
          </template>
        </el-table-column>
        <el-table-column label="与原就诊医生" width="120">
          <template #default="{ row }">{{ revisitMatchLabel(row.sameDoctor) }}</template>
        </el-table-column>
        <el-table-column label="与原就诊科室" width="120">
          <template #default="{ row }">{{ revisitMatchLabel(row.sameDept) }}</template>
        </el-table-column>
        <el-table-column label="间隔天数" width="110" align="right">
          <template #default="{ row }">
            {{ row.withinDays === null || row.withinDays === undefined ? '不限' : `≤ ${row.withinDays} 天` }}
          </template>
        </el-table-column>
        <el-table-column label="收费方式" min-width="130">
          <template #default="{ row }">
            <span :class="row.chargeMode === 1 ? 'text-slate-700' : 'font-semibold text-[#0E9488]'">
              {{ dictLabelText(chargeModeOptions, row.chargeMode) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="right" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag
                :class="row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-500'"
                effect="plain"
                size="small"
                class="border"
            >
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="Edit" v-perm="'opd:revisitPolicy:add'" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" :icon="Delete" v-perm="'opd:revisitPolicy:delete'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的复诊收费策略
      </div>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑复诊收费策略' : '新增复诊收费策略'" width="560px" destroy-on-close>
      <el-form label-width="120px">
        <el-form-item label="策略名称" required>
          <el-input v-model="form.policyName" placeholder="如：同医生 7 日内复诊免挂号费" />
        </el-form-item>
        <el-form-item label="适用来源" required>
          <el-select v-model="form.revisitSource" placeholder="请选择" class="w-full">
            <el-option v-for="o in listSourceOptions" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="与原就诊医生">
          <el-radio-group v-model="form.sameDoctor">
            <el-radio v-for="o in REVISIT_MATCH_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="与原就诊科室">
          <el-radio-group v-model="form.sameDept">
            <el-radio v-for="o in REVISIT_MATCH_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="间隔天数">
          <el-input-number v-model="form.withinDays" :min="0" :max="3650" class="!w-40" />
          <span class="ml-2 text-xs text-slate-400">留空=不限；与原病历就诊日相差超过该天数则本条不命中</span>
        </el-form-item>
        <el-form-item label="收费方式" required>
          <el-select v-model="form.chargeMode" placeholder="请选择" class="w-full">
            <el-option v-for="o in chargeModeOptions" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="1" :max="9999" class="!w-40" />
          <span class="ml-2 text-xs text-slate-400">数值小者优先命中；铺底策略占用 10 的间隔便于插队</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="o in STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="免钱的依据，会写进收费单备注供医保/审计追溯" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button v-perm="'opd:revisitPolicy:add'" type="primary" :loading="formSubmitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
