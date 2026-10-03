<script setup lang="ts">
/**
 * 检查胶片量方与发放（菜单 415，sql/138）
 *
 * 岗位：拍片这一岗（检查技师）。诊断医师那边只在报告详情里看得到「本次已打 N 张」，改不了。
 * 三件事必须落在库里：打了什么规格几张（用量）、收没收钱（记账）、交没交到患者手上（发放）。
 * 金额一律服务端按 单价 × 张数 现算，前端不传钱。
 */
import {ref, computed, onMounted} from 'vue'
import {Search, Plus, Printer, Box, Coin, Delete, Setting} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getExamFilmListPage, getExamFilmStats, examFilmUpsert, examFilmCharge, examFilmMarkPrinted,
  examFilmDeliver, examFilmDelete, getFilmSpecSelectList, filmSpecUpsert, filmSpecDelete,
  getInspectionRecordListPage
} from '@/api/medicaltech'
import {hasPerm} from '@/lib/perm'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

interface FilmRow {
  id: string
  filmNo: string
  recordNo?: string
  patientName?: string
  patientNo?: string
  itemName?: string
  bodyPart?: string
  specName?: string
  unitPrice?: number
  quantity?: number
  amount?: number
  filmStatus?: number
  filmStatusText?: string
  chargeFlag?: number
  feeNo?: string
  printBy?: string
  printTime?: string
  deliverBy?: string
  deliverTime?: string
  createTime?: string
  remark?: string
}

const FILM_STATUS_OPTIONS = [
  {value: 1, label: '已登记'},
  {value: 2, label: '已打印'},
  {value: 3, label: '已发放'},
  {value: 4, label: '已作废'},
]

const loading = ref(false)
const rows = ref<FilmRow[]>([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const keyword = ref('')
const filmStatus = ref<number | undefined>(undefined)
const chargeFlag = ref<number | undefined>(undefined)
const dateRange = ref<string[]>([])
const stats = ref<any>({rowCount: 0, totalQuantity: 0, totalAmount: 0, chargedAmount: 0})

const canAdd = computed(() => hasPerm('medtech:examFilm:add'))
const canCharge = computed(() => hasPerm('medtech:examFilm:charge'))
const canDeliver = computed(() => hasPerm('medtech:examFilm:deliver'))
const canDelete = computed(() => hasPerm('medtech:examFilm:delete'))

const money = (v: any) => Number(v ?? 0).toFixed(2)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExamFilmListPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      filmStatus: filmStatus.value,
      chargeFlag: chargeFlag.value,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined,
    })
    rows.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getExamFilmStats(dateRange.value?.[0], dateRange.value?.[1])
    stats.value = res.data || {}
  } catch {
    /* 统计失败不拦页面 */
  }
}

const refresh = () => {
  pagination.value.pageNum = 1
  loadData()
  loadStats()
}

const handleSizeChange = (v: number) => {
  pagination.value.pageSize = v
  pagination.value.pageNum = 1
  loadData()
}
const handleCurrentChange = (v: number) => {
  pagination.value.pageNum = v
  loadData()
}

// ========== 登记用量 ==========

const showAdd = ref(false)
const saving = ref(false)
const specOptions = ref<any[]>([])
/** 选检查记录：按患者姓名查检查记录（现有接口只支持 patientName / 状态过滤） */
const recordKeyword = ref('')
const recordCandidates = ref<any[]>([])
const recordLoading = ref(false)
const selectedRecord = ref<any>(null)
const addForm = ref({specId: undefined as string | undefined, quantity: 1, remark: ''})

const addAmount = computed(() => {
  const spec = specOptions.value.find(s => s.id === addForm.value.specId)
  return spec ? Number(spec.unitPrice) * Number(addForm.value.quantity || 0) : 0
})

const searchRecords = async () => {
  if (!recordKeyword.value?.trim()) return ElMessage.warning('请先输入患者姓名')
  recordLoading.value = true
  try {
    const res = await getInspectionRecordListPage({pageNum: 1, pageSize: 20, patientName: recordKeyword.value.trim()})
    recordCandidates.value = res.data?.records || []
    if (!recordCandidates.value.length) ElMessage.info('没有查到该患者的检查记录')
  } catch (e: any) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    recordLoading.value = false
  }
}

const openAdd = async () => {
  if (!specOptions.value.length) {
    const r = await getFilmSpecSelectList()
    specOptions.value = r.data || []
  }
  selectedRecord.value = null
  recordCandidates.value = []
  recordKeyword.value = ''
  addForm.value = {specId: undefined, quantity: 1, remark: ''}
  showAdd.value = true
}

const doSave = async () => {
  if (!selectedRecord.value) return ElMessage.warning('请先选择一条检查记录')
  if (!addForm.value.specId) return ElMessage.warning('请选择胶片规格')
  if (!addForm.value.quantity || addForm.value.quantity < 1) return ElMessage.warning('张数至少 1')
  saving.value = true
  try {
    await examFilmUpsert({
      recordId: String(selectedRecord.value.id),
      specId: addForm.value.specId,
      quantity: addForm.value.quantity,
      remark: addForm.value.remark || undefined,
    })
    // addAmount 是 computed —— 模板里会自动解包，但 JS 里必须 .value，
    // 漏掉它不会报错，只会把「已登记 3 张，金额 ¥NaN」弹给用户（UI 脚本抓到的）。
    ElMessage.success(`已登记 ${addForm.value.quantity} 张，金额 ¥${money(addAmount.value)}`)
    showAdd.value = false
    refresh()
  } catch (e: any) {
    ElMessage.error(e?.message || '登记失败')
  } finally {
    saving.value = false
  }
}

// ========== 行操作 ==========

const doCharge = async (row: FilmRow) => {
  try {
    await ElMessageBox.confirm(
        `为胶片 ${row.filmNo} 生成记账（¥${money(row.amount)}）？记账后患者在收费台结算；已记账的重复点不会记两笔。`,
        '胶片记账', {confirmButtonText: '记账', cancelButtonText: '取消', type: 'warning'})
  } catch {
    return
  }
  try {
    await examFilmCharge(row.id)
    ElMessage.success('已生成记账')
    loadData()
    loadStats()
  } catch (e: any) {
    ElMessage.error(e?.message || '记账失败')
  }
}

const doPrint = async (row: FilmRow) => {
  try {
    await examFilmMarkPrinted(row.id)
    ElMessage.success('已标记打印')
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const doDeliver = async (row: FilmRow) => {
  try {
    await ElMessageBox.confirm(`确认胶片 ${row.filmNo} 已交给患者/病区？`, '胶片发放', {
      confirmButtonText: '确认发放', cancelButtonText: '取消', type: 'warning'})
  } catch {
    return
  }
  try {
    await examFilmDeliver(row.id)
    ElMessage.success('已标记发放')
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const doDelete = async (row: FilmRow) => {
  if (row.chargeFlag === 1) {
    return ElMessage.warning('已记账的胶片不能作废（记账流水 ' + (row.feeNo || '') + '）：请先到收费台红冲那笔费用')
  }
  try {
    const {value} = await ElMessageBox.prompt('作废原因（会写进审计日志）', '作废胶片', {
      confirmButtonText: '作废', cancelButtonText: '取消', inputPlaceholder: '如：规格录错，重新登记',
    })
    await examFilmDelete(row.id, value)
    ElMessage.success('已作废（操作已留痕）')
    loadData()
    loadStats()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '作废失败')
  }
}

// ========== 规格价目 ==========

const showSpec = ref(false)
const specRows = ref<any[]>([])
const showSpecEdit = ref(false)
const specForm = ref<any>({id: undefined, specCode: '', specName: '', unitPrice: 0, unit: '张', sortOrder: 0, status: 1})

const openSpec = async () => {
  const r = await getFilmSpecSelectList()
  specRows.value = r.data || []
  showSpec.value = true
}

const openSpecEdit = (row?: any) => {
  specForm.value = row
      ? {...row}
      : {id: undefined, specCode: '', specName: '', unitPrice: 0, unit: '张', sortOrder: 0, status: 1}
  showSpecEdit.value = true
}

const doSaveSpec = async () => {
  if (!specForm.value.specCode?.trim() || !specForm.value.specName?.trim()) {
    return ElMessage.warning('规格编码与名称必填')
  }
  try {
    await filmSpecUpsert(specForm.value)
    ElMessage.success('规格已保存')
    showSpecEdit.value = false
    const r = await getFilmSpecSelectList()
    specRows.value = r.data || []
    specOptions.value = r.data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
}

const doDeleteSpec = async (row: any) => {
  try {
    await ElMessageBox.confirm(`删除规格「${row.specName}」？已被引用过的规格删不掉，只能停用。`, '删除规格', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'})
  } catch {
    return
  }
  try {
    await filmSpecDelete(row.id)
    ElMessage.success('已删除')
    const r = await getFilmSpecSelectList()
    specRows.value = r.data || []
    specOptions.value = r.data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

const statusTagType = (st?: number) => {
  if (st === 3) return 'success'
  if (st === 2) return 'warning'
  if (st === 4) return 'danger'
  return 'info'
}

onMounted(() => {
  // 默认看今天：胶片是当天结账的东西，翻历史的前提是先知道今天打了多少
  const d = new Date()
  const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = [today, today]
  refresh()
})
</script>

<template>
  <div v-loading="loading">
    <!-- 统计：张数 / 金额 / 已记账金额 -->
    <div class="mb-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-slate-700">{{ stats.totalQuantity ?? 0 }}</p>
        <p class="mt-1 text-xs text-slate-500">胶片张数</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-slate-700">{{ stats.rowCount ?? 0 }}</p>
        <p class="mt-1 text-xs text-slate-500">登记笔数</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-blue-600">¥{{ money(stats.totalAmount) }}</p>
        <p class="mt-1 text-xs text-slate-500">金额合计</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-emerald-600">¥{{ money(stats.chargedAmount) }}</p>
        <p class="mt-1 text-xs text-slate-500">其中已记账</p>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="keyword" placeholder="患者 / 胶片单号 / 记录号" :prefix-icon="Search" class="!w-64"
                      clearable @keyup.enter="refresh"/>
          </el-form-item>
          <el-form-item label="胶片状态">
            <el-select v-model="filmStatus" placeholder="胶片状态" class="!w-32" clearable>
              <el-option v-for="o in FILM_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="记账状态">
            <el-select v-model="chargeFlag" placeholder="记账状态" class="!w-32" clearable>
              <el-option label="未记账" :value="0"/>
              <el-option label="已记账" :value="1"/>
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                            start-placeholder="开始日期" end-placeholder="截止日期" class="!w-64" @change="refresh"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="refresh">查询</el-button>
            <el-button @click="keyword = ''; filmStatus = undefined; chargeFlag = undefined; dateRange = []; refresh()">
              重置
            </el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'medtech:examFilm:add'" type="primary" @click="openAdd">
            <el-icon class="mr-0.5">
              <Plus/>
            </el-icon>
            登记胶片
          </el-button>
          <el-button v-perm="'medtech:examFilm:add'" @click="openSpec">
            <el-icon class="mr-0.5">
              <Setting/>
            </el-icon>
            规格价目
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" style="width: 100%" stripe :max-height="tableMaxHeight">
        <el-table-column prop="filmNo" label="胶片单号" width="150"/>
        <el-table-column label="患者" width="140">
          <template #default="{ row }">
            <span>{{ row.patientName }}</span>
            <div class="text-xs text-slate-400">{{ row.patientNo }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="检查项目" min-width="120"/>
        <el-table-column prop="bodyPart" label="部位" width="100"/>
        <el-table-column prop="specName" label="胶片规格" width="160"/>
        <el-table-column label="单价" width="80" align="right">
          <template #default="{ row }">¥{{ money(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="张数" width="70" align="center"/>
        <el-table-column label="金额" width="90" align="right">
          <template #default="{ row }"><span class="font-medium">¥{{ money(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="statusTagType(row.filmStatus)">{{ row.filmStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="记账" width="120">
          <template #default="{ row }">
            <span v-if="row.chargeFlag === 1" class="text-xs text-emerald-600">已记账</span>
            <span v-else class="text-xs text-amber-600">未记账</span>
            <div v-if="row.feeNo" class="text-xs text-slate-400">{{ row.feeNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="打印/发放" width="140">
          <template #default="{ row }">
            <div class="text-xs leading-5">
              <div>{{ row.printBy ? '打印 ' + row.printBy : '—' }}</div>
              <div>{{ row.deliverBy ? '发放 ' + row.deliverBy : '—' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.chargeFlag !== 1 && row.filmStatus !== 4" v-perm="'medtech:examFilm:charge'"
                       type="warning" link size="small" @click="doCharge(row)">
              <el-icon class="mr-0.5">
                <Coin/>
              </el-icon>
              记账
            </el-button>
            <el-button v-if="row.filmStatus === 1" v-perm="'medtech:examFilm:deliver'" type="info" link size="small"
                       @click="doPrint(row)">
              <el-icon class="mr-0.5">
                <Printer/>
              </el-icon>
              已打印
            </el-button>
            <el-button v-if="row.filmStatus === 2" v-perm="'medtech:examFilm:deliver'" type="success" link size="small"
                       @click="doDeliver(row)">
              <el-icon class="mr-0.5">
                <Box/>
              </el-icon>
              发放
            </el-button>
            <el-button v-if="row.filmStatus !== 4 && row.filmStatus !== 3" v-perm="'medtech:examFilm:delete'"
                       type="danger" link size="small" @click="doDelete(row)">
              <el-icon class="mr-0.5">
                <Delete/>
              </el-icon>
              作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="handleSizeChange" @current-change="handleCurrentChange"/>
      </div>
    </el-card>

    <!-- 登记胶片 -->
    <el-dialog v-model="showAdd" title="登记胶片用量" width="800px" destroy-on-close>
      <div class="space-y-4">
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="mb-2 text-sm font-medium text-slate-700">① 选择检查记录</p>
          <div class="flex items-center gap-2">
            <el-input v-model="recordKeyword" placeholder="输入患者姓名查询检查记录" class="!w-64"
                      @keyup.enter="searchRecords"/>
            <el-button :loading="recordLoading" @click="searchRecords">查询</el-button>
          </div>
          <el-table v-if="recordCandidates.length" :data="recordCandidates" size="small" class="mt-3" height="200"
                    highlight-current-row @current-change="(r) => selectedRecord = r">
            <el-table-column prop="recordNo" label="记录号" width="170"/>
            <el-table-column prop="patientName" label="患者" width="90"/>
            <el-table-column prop="inspectionItemName" label="项目" min-width="120"/>
            <el-table-column prop="bodyPart" label="部位" width="110"/>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.recordStatusText || row.recordStatus }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <p v-if="selectedRecord" class="mt-2 text-xs text-emerald-600">
            已选：{{ selectedRecord.recordNo }} · {{ selectedRecord.patientName }} ·
            {{ selectedRecord.inspectionItemName }}
          </p>
        </div>

        <div class="rounded-lg border border-slate-200 p-3">
          <p class="mb-2 text-sm font-medium text-slate-700">② 用量与规格</p>
          <div class="flex flex-wrap items-end gap-4">
            <div>
              <label class="mb-1 block text-xs text-slate-500">胶片规格</label>
              <el-select v-model="addForm.specId" placeholder="选择规格" class="!w-56">
                <el-option v-for="s in specOptions" :key="s.id" :label="`${s.specName}（¥${money(s.unitPrice)}/张）`"
                           :value="s.id"/>
              </el-select>
            </div>
            <div>
              <label class="mb-1 block text-xs text-slate-500">张数</label>
              <el-input-number v-model="addForm.quantity" :min="1" :max="200" class="!w-32"/>
            </div>
            <div>
              <label class="mb-1 block text-xs text-slate-500">金额（服务端按单价×张数现算）</label>
              <p class="text-lg font-bold text-slate-700">¥{{ money(addAmount) }}</p>
            </div>
          </div>
          <el-input v-model="addForm.remark" class="mt-3" placeholder="备注（可选）"/>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 规格价目 -->
    <el-dialog v-model="showSpec" title="胶片规格价目" width="700px" destroy-on-close>
      <el-table :data="specRows" size="small">
        <el-table-column prop="specCode" label="编码" width="120"/>
        <el-table-column prop="specName" label="规格名称" min-width="180"/>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">¥{{ money(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="60"/>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'medtech:examFilm:add'" type="primary" link size="small"
                       @click="openSpecEdit(row)">改价/编辑
            </el-button>
            <el-button v-perm="'medtech:examFilm:add'" type="danger" link size="small"
                       @click="doDeleteSpec(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button v-perm="'medtech:examFilm:add'" @click="openSpecEdit()">
          <el-icon class="mr-0.5">
            <Plus/>
          </el-icon>
          新增规格
        </el-button>
        <el-button type="primary" @click="showSpec = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 规格编辑 -->
    <el-dialog v-model="showSpecEdit" :title="specForm.id ? '修改规格' : '新增规格'" width="460px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="规格编码">
          <el-input v-model="specForm.specCode" :disabled="!!specForm.id" placeholder="如 F-14X17"/>
        </el-form-item>
        <el-form-item label="规格名称">
          <el-input v-model="specForm.specName" placeholder="如 14×17英寸激光胶片"/>
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="specForm.unitPrice" :min="0" :precision="2" :step="1"/>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="specForm.unit" class="!w-24"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="specForm.status" class="!w-32">
            <el-option label="启用" :value="1"/>
            <el-option label="停用" :value="0"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSpecEdit = false">取消</el-button>
        <el-button type="primary" @click="doSaveSpec">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
