<script setup lang="ts">
/**
 * L1 费用记账台账（biz_fee_record）
 *
 * 记账行是应收的唯一来源，本页三条铁律：
 * 1. 记账行不可修改，修正只能红冲（红冲行是负数行，指向被冲原行）；
 * 2. 手工补记账是错漏费用的唯一录入口，一切金额后端现算（单价×数量）；
 * 3. 状态由后端流转（待结算→已锁定→已结算 / 已红冲），本页只渲染不翻状态。
 */
import {computed, onMounted, reactive, ref} from 'vue'
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {bookFee, getFeeRecordDetail, getFeeRecordListPage, reverseFee} from '@/api/feeRecord'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText, formatMoney} from '@/lib/utils'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import PatientSelect from '@/components/his/PatientSelect.vue'

const dicts = ref<Record<string, any[]>>({})
const dictOptions = (type: string) => dicts.value[type] || []

const feeStatusText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.FEE_STATUS), v)
const itemTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE), v)
const sourceTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.FEE_SOURCE_TYPE), v)
const encounterTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.ENCOUNTER_TYPE), v)

/** 已红冲(4) 的行金额是负数（红冲行），正常行按金额排序看 */
const amountClass = (v: any) => (Number(v) < 0 ? 'text-rose-600' : 'text-slate-700')

const loading = ref(true)
const rows = ref<any[]>([])
const query = reactive({
  keyword: '',
  encounterType: null as number | null,
  feeStatus: null as number | null,
  itemType: null as number | null,
  sourceType: null as number | null,
})
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

async function loadList() {
  loading.value = true
  try {
    const res = await getFeeRecordListPage({
      keyword: query.keyword || undefined,
      encounterType: query.encounterType ?? undefined,
      feeStatus: query.feeStatus ?? undefined,
      itemType: query.itemType ?? undefined,
      sourceType: query.sourceType ?? undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    rows.value = res.data?.records || []
    pagination.total = Number(res.data?.total || 0)
  } catch (e: any) {
    rows.value = []
    ElMessage.error(e.message || '加载记账行失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadList()
}

function handleReset() {
  query.keyword = ''
  query.encounterType = null
  query.feeStatus = null
  query.itemType = null
  query.sourceType = null
  handleSearch()
}

// ==================== 详情（含红冲链） ====================

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>(null)

async function openDetail(row: any) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getFeeRecordDetail(row.id)
    detail.value = res.data || null
  } catch (e: any) {
    detail.value = null
    ElMessage.error(e.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ==================== 手工补记账 ====================

const bookVisible = ref(false)
const bookSaving = ref(false)
const bookForm = reactive<any>({
  patientId: null,
  patientNo: '',
  patientName: '',
  encounterType: 1,
  encounterId: null,
  encounterNo: '',
  itemType: null,
  itemName: '',
  price: null,
  quantity: 1,
  unit: '',
  sourceType: 11,
  remark: '',
})

const bookAmount = computed(() => {
  const p = Number(bookForm.price || 0)
  const q = Number(bookForm.quantity || 0)
  return Math.round(p * q * 100) / 100
})

function openBook() {
  bookForm.patientId = null
  bookForm.patientNo = ''
  bookForm.patientName = ''
  bookForm.encounterType = 1
  bookForm.encounterId = null
  bookForm.encounterNo = ''
  bookForm.itemType = null
  bookForm.itemName = ''
  bookForm.price = null
  bookForm.quantity = 1
  bookForm.unit = ''
  bookForm.remark = ''
  bookVisible.value = true
}

function onPatientSelect(p: any) {
  bookForm.patientId = p?.id ?? null
  bookForm.patientNo = p?.patientNo || ''
  bookForm.patientName = p?.name || p?.patientName || ''
}

async function submitBook() {
  if (!bookForm.patientId) {
    ElMessage.warning('请选择患者')
    return
  }
  if (!bookForm.encounterId) {
    ElMessage.warning('请填写就诊标识（挂号/住院记录ID）')
    return
  }
  if (!bookForm.itemType) {
    ElMessage.warning('请选择项目类型')
    return
  }
  if (!bookForm.itemName) {
    ElMessage.warning('请填写项目名称')
    return
  }
  if (!(Number(bookForm.price) > 0) || !(Number(bookForm.quantity) > 0)) {
    ElMessage.warning('单价与数量必须大于 0')
    return
  }
  bookSaving.value = true
  try {
    const res = await bookFee({...bookForm})
    ElMessage.success(`已记账 ${res.data?.feeNo || ''}，金额 ¥${formatMoney(res.data?.amount)}`)
    bookVisible.value = false
    await loadList()
  } catch (e: any) {
    ElMessage.error(e.message || '补记账失败')
  } finally {
    bookSaving.value = false
  }
}

// ==================== 红冲 ====================

const reverseVisible = ref(false)
const reverseSaving = ref(false)
const reverseRow = ref<any>(null)
const reverseForm = reactive({quantity: null as number | null, reason: ''})

/** 部分冲减只在数量>可冲数量内有意义；留空 = 整行冲 */
const reverseIsPartial = computed(() => reverseForm.quantity != null && Number(reverseForm.quantity) > 0)

function openReverse(row: any) {
  reverseRow.value = row
  reverseForm.quantity = null
  reverseForm.reason = ''
  reverseVisible.value = true
}

async function submitReverse() {
  if (!reverseForm.reason) {
    ElMessage.warning('请填写红冲原因')
    return
  }
  const row = reverseRow.value
  const partial = reverseIsPartial.value
  const confirmed = await ElMessageBox.confirm(
      partial
          ? `将对「${row.itemName}」按数量 ${reverseForm.quantity}/${row.quantity} 部分冲减，生成负数红冲行，确定吗？`
          : `将整行冲销「${row.itemName}」¥${formatMoney(row.amount)}，生成负数红冲行，确定吗？`,
      '红冲确认',
      {type: 'warning'},
  ).catch(() => null)
  if (!confirmed) return
  reverseSaving.value = true
  try {
    const res = await reverseFee({
      feeId: row.id,
      quantity: partial ? Number(reverseForm.quantity) : undefined,
      reason: reverseForm.reason,
    })
    ElMessage.success(`已红冲，生成红冲行 ${res.data?.feeNo || ''}`)
    reverseVisible.value = false
    await loadList()
  } catch (e: any) {
    ElMessage.error(e.message || '红冲失败')
  } finally {
    reverseSaving.value = false
  }
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.FEE_STATUS,
    DICT_TYPE.CHARGE_ITEM_TYPE,
    DICT_TYPE.FEE_SOURCE_TYPE,
    DICT_TYPE.ENCOUNTER_TYPE,
  ].join(','))
  await loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                data-testid="fee-keyword"
                placeholder="患者姓名 / 单号 / 项目"
                clearable
                style="width: 220px"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="就诊类型">
            <el-select v-model="query.encounterType" data-testid="fee-encounter-type" placeholder="就诊类型" clearable style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.ENCOUNTER_TYPE)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="记账状态">
            <el-select v-model="query.feeStatus" data-testid="fee-status" placeholder="记账状态" clearable style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.FEE_STATUS)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="项目类型">
            <el-select v-model="query.itemType" data-testid="fee-item-type" placeholder="项目类型" clearable style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="费用来源">
            <el-select v-model="query.sourceType" data-testid="fee-source-type" placeholder="费用来源" clearable style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.FEE_SOURCE_TYPE)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" data-testid="fee-search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" :icon="Plus" data-testid="fee-book-open" @click="openBook">手工补记账</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="fee-table">
      <el-table-column prop="feeNo" label="记账流水号" min-width="160" class-name="font-mono"/>
      <el-table-column prop="patientName" label="患者" min-width="90"/>
      <el-table-column label="就诊" min-width="70">
        <template #default="{ row }">{{ encounterTypeText(row.encounterType) }}</template>
      </el-table-column>
      <el-table-column prop="itemName" label="项目" min-width="150" show-overflow-tooltip/>
      <el-table-column prop="price" label="单价" min-width="80" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" min-width="70" align="right"/>
      <el-table-column label="金额" min-width="90" align="right">
        <template #default="{ row }">
          <span :class="amountClass(row.amount)">¥{{ formatMoney(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="90">
        <template #default="{ row }">{{ feeStatusText(row.feeStatus) }}</template>
      </el-table-column>
      <el-table-column label="来源" min-width="100">
        <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
      </el-table-column>
      <el-table-column prop="bookTime" label="记账时间" min-width="150" class-name="font-mono"/>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="openDetail(row)">详情</el-button>
          <el-button
              v-if="Number(row.feeStatus) === 1 || Number(row.feeStatus) === 3"
              link
              type="danger"
              data-testid="fee-reverse"
              @click="openReverse(row)"
          >红冲</el-button>
        </template>
      </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="PAGE_SIZES"
            layout="total, sizes, prev, pager, next, jumper"
            @change="loadList"
        />
      </div>
    </el-card>

    <!-- 详情：本行 + 红冲链 -->
    <el-dialog v-model="detailVisible" title="记账行详情" width="760px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="记账流水号">{{ detail.feeNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ feeStatusText(detail.feeStatus) }}</el-descriptions-item>
            <el-descriptions-item label="患者">{{ detail.patientName }}（{{ detail.patientNo || '—' }}）</el-descriptions-item>
            <el-descriptions-item label="项目">{{ detail.itemName }}</el-descriptions-item>
            <el-descriptions-item label="单价×数量">¥{{ formatMoney(detail.price) }} × {{ detail.quantity }}</el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ formatMoney(detail.amount) }}</el-descriptions-item>
            <el-descriptions-item label="已红冲金额">¥{{ formatMoney(detail.reversedAmount) }}</el-descriptions-item>
            <el-descriptions-item label="剩余金额">¥{{ formatMoney(detail.remainingAmount) }}</el-descriptions-item>
            <el-descriptions-item label="记账时间">{{ detail.bookTime }}</el-descriptions-item>
            <el-descriptions-item label="记账人">{{ detail.bookByName || '—' }}</el-descriptions-item>
          </el-descriptions>
          <template v-if="detail.reverseRows?.length">
            <div class="mt-3 mb-1 text-sm font-medium">红冲链（负数为红冲行）</div>
            <el-table :data="detail.reverseRows" border size="small">
              <el-table-column prop="feeNo" label="红冲行号" min-width="150" class-name="font-mono text-xs"/>
              <el-table-column prop="quantity" label="数量" min-width="70" align="right"/>
              <el-table-column prop="amount" label="金额" min-width="90" align="right">
                <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="bookTime" label="红冲时间" min-width="150" class-name="font-mono text-xs"/>
            </el-table>
          </template>
        </template>
      </div>
    </el-dialog>

    <!-- 手工补记账 -->
    <el-dialog v-model="bookVisible" title="手工补记账（错漏费用的唯一录入口）" width="560px">
      <el-form label-width="110px">
        <el-form-item label="患者" required>
          <PatientSelect v-model="bookForm.patientId" data-testid="fee-book-patient" @select="onPatientSelect"/>
        </el-form-item>
        <el-form-item label="就诊类型" required>
          <el-radio-group v-model="bookForm.encounterType">
            <el-radio :value="1">门诊</el-radio>
            <el-radio :value="2">住院</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="就诊标识ID" required>
          <el-input v-model="bookForm.encounterId" data-testid="fee-book-encounter" placeholder="挂号ID / 住院记录ID"/>
        </el-form-item>
        <el-form-item label="项目类型" required>
          <el-select v-model="bookForm.itemType" data-testid="fee-book-item-type" placeholder="选择类型" style="width: 100%">
            <el-option v-for="o in dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="bookForm.itemName" data-testid="fee-book-item-name"/>
        </el-form-item>
        <el-form-item label="单价 / 数量" required>
          <div class="flex items-center gap-2">
            <el-input-number v-model="bookForm.price" :min="0" :precision="4" data-testid="fee-book-price"/>
            <el-input-number v-model="bookForm.quantity" :min="0.01" :precision="2" data-testid="fee-book-quantity"/>
            <span class="text-sm text-slate-500">合计 ¥{{ formatMoney(bookAmount) }}</span>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bookForm.remark" type="textarea" :rows="2" maxlength="200"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookSaving" data-testid="fee-book-submit" @click="submitBook">记账</el-button>
      </template>
    </el-dialog>

    <!-- 红冲 -->
    <el-dialog v-model="reverseVisible" title="红冲记账行" width="460px">
      <el-form label-width="100px">
        <el-form-item label="红冲行">
          <span>{{ reverseRow?.itemName }}（¥{{ formatMoney(reverseRow?.amount) }}）</span>
        </el-form-item>
        <el-form-item label="冲减数量">
          <el-input-number v-model="reverseForm.quantity" :min="0" :precision="2" data-testid="fee-reverse-quantity"/>
          <div class="w-full text-xs text-slate-500 mt-1">留空 = 整行冲；填数量 = 部分冲减（生成负数红冲行）</div>
        </el-form-item>
        <el-form-item label="红冲原因" required>
          <el-input v-model="reverseForm.reason" type="textarea" :rows="2" data-testid="fee-reverse-reason" maxlength="200"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reverseVisible = false">取消</el-button>
        <el-button type="danger" :loading="reverseSaving" data-testid="fee-reverse-submit" @click="submitReverse">确认红冲</el-button>
      </template>
    </el-dialog>
  </div>
</template>
