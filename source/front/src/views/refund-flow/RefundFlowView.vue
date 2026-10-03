<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRefundFlowList, getRefundFlowDetail } from '@/api/refundFlow'
import { getDictDataMapList } from '@/api/system'
import { formatMoney, dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface FlowRow {
  id: string
  refundNo?: string
  chargeId?: string
  chargeNo?: string
  patientId?: string
  patientNo?: string
  patientName?: string
  refundType?: number
  totalAmount?: number | string
  refundReason?: string
  refundStatus?: number
  refundMethod?: number
  payMethod?: number
  channelRefundNo?: string
  flowSource?: number
  applyId?: string
  applyNo?: string
  insuranceCancelled?: number
  refundBy?: string
  refundTime?: string
  details?: any[]
}

/** 流水来源：本表只有这一列会为空/为 0，其余来源都对应一次真实的冲正动作 */
const SOURCE_TAG_CLASS: Record<number, string> = {
  0: 'bg-slate-100 text-slate-500',
  1: 'bg-blue-100 text-blue-700',
  2: 'bg-emerald-100 text-emerald-700',
  3: 'bg-amber-100 text-amber-700',
}

const loading = ref(true)
const rows = ref<FlowRow[]>([])
const query = reactive({
  keyword: '',
  flowSource: null as number | null,
  refundMethod: null as number | null,
})
const pagination = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const flowSourceOptions = ref<any[]>([])
const refundMethodOptions = ref<any[]>([])
const payMethodOptions = ref<any[]>([])
const refundTypeOptions = ref<any[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<FlowRow | null>(null)

/** 本页合计：退出去的总钱数。按当前筛选条件现算，不是前端自造的数字 */
const sumAmount = ref<number | null>(null)

async function loadDicts() {
  try {
    const res = await getDictDataMapList(
        'his_refund_flow_source,his_refund_method,his_pay_method,his_refund_apply_type,his_refund_status')
    if (res.code === 200 && res.data) {
      flowSourceOptions.value = res.data['his_refund_flow_source'] || []
      refundMethodOptions.value = res.data['his_refund_method'] || []
      payMethodOptions.value = res.data['his_pay_method'] || []
      refundTypeOptions.value = res.data['his_refund_apply_type'] || []
    }
  } catch (e) {
    console.error('加载退费流水字典失败', e)
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getRefundFlowList({
      keyword: query.keyword || undefined,
      flowSource: query.flowSource === null ? undefined : query.flowSource,
      refundMethod: query.refundMethod === null ? undefined : query.refundMethod,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    const data = res.data || {}
    rows.value = data.records || []
    pagination.total = Number(data.total || 0)
    sumAmount.value = rows.value.reduce((s, r) => s + Number(r.totalAmount || 0), 0)
  } catch (e: any) {
    rows.value = []
    pagination.total = 0
    sumAmount.value = null
    ElMessage.error(e?.message || '加载退费流水失败')
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
  query.flowSource = null
  query.refundMethod = null
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

async function openDetail(row: FlowRow) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = row
  try {
    const res = await getRefundFlowDetail(row.id)
    if (res.data) detail.value = res.data
  } catch (e: any) {
    ElMessage.warning(e?.message || '获取退费流水明细失败，已展示列表数据')
  } finally {
    detailLoading.value = false
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
        <h1 class="text-2xl font-bold text-slate-900">退费流水</h1>
        <p class="mt-1 text-sm text-slate-500">
          钱真正退出去的台账：退费申请执行、收费处直退、退号联动三类都在这，只读不改
        </p>
      </div>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                placeholder="退费单号 / 原收费单号 / 患者姓名"
                :prefix-icon="Search"
                clearable
                class="!w-72"
                data-testid="refund-flow-keyword"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="流水来源">
            <el-select v-model="query.flowSource" placeholder="流水来源" clearable :fit-input-width="false" class="!w-40">
              <el-option
                  v-for="o in flowSourceOptions"
                  :key="o.dictValue"
                  :label="o.dictLabel"
                  :value="Number(o.dictValue)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="退费方式">
            <el-select v-model="query.refundMethod" placeholder="退费方式" clearable :fit-input-width="false" class="!w-40">
              <el-option
                  v-for="o in refundMethodOptions"
                  :key="o.dictValue"
                  :label="o.dictLabel"
                  :value="Number(o.dictValue)"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" data-testid="refund-flow-search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            本页合计
            <span class="font-semibold text-slate-700">{{ sumAmount === null ? '—' : `¥${formatMoney(sumAmount)}` }}</span>
            · 共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 笔流水
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="refund-flow-table">
        <el-table-column prop="refundNo" label="退费单号" min-width="180" class-name="font-mono" />
        <el-table-column label="患者" min-width="100">
          <template #default="{ row }">{{ row.patientName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="chargeNo" label="原收费单号" min-width="180" class-name="font-mono">
          <template #default="{ row }">{{ row.chargeNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="退费类型" width="100">
          <template #default="{ row }">{{ dictLabelText(refundTypeOptions, row.refundType) }}</template>
        </el-table-column>
        <el-table-column label="实退金额" align="right" width="110">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流水来源" width="120">
          <template #default="{ row }">
            <el-tag
                :class="SOURCE_TAG_CLASS[row.flowSource] || 'bg-slate-100 text-slate-600'"
                effect="plain"
                size="small"
                class="border"
                data-testid="refund-flow-source-tag"
            >
              {{ dictLabelText(flowSourceOptions, row.flowSource) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退费方式" width="110">
          <template #default="{ row }">{{ dictLabelText(refundMethodOptions, row.refundMethod) }}</template>
        </el-table-column>
        <el-table-column label="原支付" width="100">
          <template #default="{ row }">{{ dictLabelText(payMethodOptions, row.payMethod) }}</template>
        </el-table-column>
        <el-table-column label="渠道退费流水号" min-width="190" class-name="font-mono">
          <template #default="{ row }">{{ row.channelRefundNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="来源申请号" min-width="180" class-name="font-mono">
          <template #default="{ row }">{{ row.applyNo || '—' }}</template>
        </el-table-column>
        <el-table-column prop="refundBy" label="退费人" width="100">
          <template #default="{ row }">{{ row.refundBy || '—' }}</template>
        </el-table-column>
        <el-table-column prop="refundTime" label="退费时间" width="170">
          <template #default="{ row }">{{ row.refundTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="View" data-testid="refund-flow-btn-detail" @click="openDetail(row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的退费流水
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

    <!-- 明细：只读 -->
    <el-dialog v-model="detailVisible" title="退费流水明细" width="760px" destroy-on-close data-testid="refund-flow-detail-dialog">
      <div v-loading="detailLoading" class="space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">退费单号：</span><span class="font-mono text-slate-700">{{ detail?.refundNo || '—' }}</span></div>
          <div><span class="text-slate-400">原收费单号：</span><span class="font-mono text-slate-700">{{ detail?.chargeNo || '—' }}</span></div>
          <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{ detail?.patientName || '—' }}</span></div>
          <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{ detail?.patientNo || '—' }}</span></div>
          <div><span class="text-slate-400">实退金额：</span><span class="font-semibold text-slate-800">¥{{ formatMoney(detail?.totalAmount) }}</span></div>
          <div><span class="text-slate-400">退费类型：</span><span class="text-slate-700">{{ dictLabelText(refundTypeOptions, detail?.refundType) }}</span></div>
          <div><span class="text-slate-400">流水来源：</span><span class="text-slate-700">{{ dictLabelText(flowSourceOptions, detail?.flowSource) }}</span></div>
          <div><span class="text-slate-400">来源申请号：</span><span class="font-mono text-slate-700">{{ detail?.applyNo || '—' }}</span></div>
          <div><span class="text-slate-400">退费方式：</span><span class="text-slate-700">{{ dictLabelText(refundMethodOptions, detail?.refundMethod) }}</span></div>
          <div><span class="text-slate-400">原支付方式：</span><span class="text-slate-700">{{ dictLabelText(payMethodOptions, detail?.payMethod) }}</span></div>
          <div><span class="text-slate-400">渠道退费流水号：</span><span class="font-mono text-slate-700">{{ detail?.channelRefundNo || '—' }}</span></div>
          <div><span class="text-slate-400">医保报盘：</span><span class="text-slate-700">{{ detail?.insuranceCancelled === 1 ? '已随本次退费撤销（2305）' : '不涉及' }}</span></div>
          <div><span class="text-slate-400">退费人：</span><span class="text-slate-700">{{ detail?.refundBy || '—' }}</span></div>
          <div><span class="text-slate-400">退费时间：</span><span class="text-slate-700">{{ detail?.refundTime || '—' }}</span></div>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <span class="text-slate-400">退费原因：</span><span class="text-slate-700">{{ detail?.refundReason || '—' }}</span>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <div class="mb-2 text-slate-400">冲正的收费明细</div>
          <el-table :data="detail?.details || []" size="small" border data-testid="refund-flow-detail-table">
            <el-table-column prop="itemName" label="项目名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="specification" label="规格" min-width="110">
              <template #default="{ row }">{{ row.specification || '—' }}</template>
            </el-table-column>
            <el-table-column prop="refundQuantity" label="退费数量" align="right" width="100" />
            <el-table-column prop="unit" label="单位" width="70">
              <template #default="{ row }">{{ row.unit || '—' }}</template>
            </el-table-column>
            <el-table-column label="单价" align="right" width="90">
              <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
            </el-table-column>
            <el-table-column label="退费金额" align="right" width="100">
              <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
            </el-table-column>
            <el-table-column prop="sourceNo" label="来源单号" min-width="150" class-name="font-mono text-xs">
              <template #default="{ row }">{{ row.sourceNo || '—' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
