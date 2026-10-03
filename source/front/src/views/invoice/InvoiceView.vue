<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Printer, CircleClose, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInvoiceList, getInvoiceDetail, printInvoice, voidInvoice } from '@/api/invoice'
import {
  INVOICE_STATUS_OPTIONS,
  invoiceStatusText,
  invoiceStatusTagClass,
  invoiceTypeText,
} from '@/lib/invoiceStatus'
import { formatMoney } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface InvoiceRow {
  id: string
  invoiceNo: string
  invoiceType: number
  chargeId?: string
  chargeNo?: string
  patientId?: string
  patientNo?: string
  patientName?: string
  totalAmount?: number | string
  invoiceStatus: number
  invoiceTime?: string
  printTime?: string
  voidTime?: string
  voidReason?: string
  electronicUrl?: string
  remark?: string
}

// 「加载中 ≠ 没有」：初值 true，未加载完不显示「暂无数据」
const loading = ref(true)
const rows = ref<InvoiceRow[]>([])
const query = reactive({
  keyword: '',
  invoiceStatus: null as number | null,
})
const pagination = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<InvoiceRow | null>(null)

async function loadList() {
  loading.value = true
  try {
    const res = await getInvoiceList({
      keyword: query.keyword || undefined,
      invoiceStatus: query.invoiceStatus === null ? undefined : query.invoiceStatus,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    const data = res.data || {}
    rows.value = data.records || []
    pagination.total = Number(data.total || 0)
  } catch (e: any) {
    rows.value = []
    pagination.total = 0
    ElMessage.error(e?.message || '加载发票列表失败')
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
  query.invoiceStatus = null
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

async function openDetail(row: InvoiceRow) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = row
  try {
    const res = await getInvoiceDetail(row.id)
    if (res.data) detail.value = res.data
  } catch (e: any) {
    // 详情失败保留列表行，但要明确告知，不能静默
    ElMessage.warning(e?.message || '获取发票详情失败，已展示列表数据')
  } finally {
    detailLoading.value = false
  }
}

async function handlePrint(row: InvoiceRow) {
  try {
    await ElMessageBox.confirm(`确认打印发票 ${row.invoiceNo}？`, '打印发票', {
      type: 'info',
      confirmButtonText: '确认打印',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await printInvoice(row.id)
    ElMessage.success('打印已记录')
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '打印失败')
  }
}

async function handleVoid(row: InvoiceRow) {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt(`作废发票 ${row.invoiceNo}，请填写作废原因`, '作废发票', {
      inputPlaceholder: '作废原因（必填）',
      inputValidator: (v: string) => (v && v.trim() ? true : '作废原因不能为空'),
      type: 'warning',
      confirmButtonText: '确认作废',
      cancelButtonText: '取消',
    })
    reason = (r as any).value
  } catch {
    return
  }
  try {
    await voidInvoice(row.id, reason)
    ElMessage.success('发票已作废')
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '作废失败')
  }
}

onMounted(loadList)
</script>

<template>
  <div>
    <div class="mb-3">
      <h1 class="text-2xl font-bold text-slate-900">发票管理</h1>
      <p class="mt-1 text-sm text-slate-500">查询收费票据、打印与作废，作废需填原因并留痕</p>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                placeholder="发票号 / 收费单号 / 患者姓名"
                :prefix-icon="Search"
                clearable
                class="!w-72"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="发票状态">
            <el-select v-model="query.invoiceStatus" placeholder="发票状态" clearable class="!w-36">
              <el-option v-for="o in INVOICE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 张发票
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" style="width: 100%" stripe :max-height="tableMaxHeight">
        <el-table-column prop="invoiceNo" label="发票号" min-width="200" class-name="font-mono" />
        <el-table-column label="患者" min-width="110">
          <template #default="{ row }">
            <span class="text-slate-800">{{ row.patientName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chargeNo" label="收费单号" min-width="180" class-name="font-mono">
          <template #default="{ row }">{{ row.chargeNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="发票类型" width="110">
          <template #default="{ row }">{{ invoiceTypeText(row.invoiceType) }}</template>
        </el-table-column>
        <el-table-column label="金额" align="right" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :class="invoiceStatusTagClass(row.invoiceStatus)" effect="plain" size="small" class="border">
              {{ invoiceStatusText(row.invoiceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceTime" label="开票时间" width="170">
          <template #default="{ row }">{{ row.invoiceTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="View" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" :icon="Printer" @click="handlePrint(row)">打印</el-button>
            <el-button
                v-if="row.invoiceStatus !== 3"
                v-perm="'finance:invoice:delete'"
                link
                type="danger"
                size="small"
                :icon="CircleClose"
                @click="handleVoid(row)"
            >
              作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的发票
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

    <el-dialog v-model="detailVisible" title="发票详情" width="640px" destroy-on-close>
      <div v-loading="detailLoading" class="space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">发票号：</span><span class="font-mono text-slate-700">{{ detail?.invoiceNo || '—' }}</span></div>
          <div><span class="text-slate-400">发票类型：</span><span class="text-slate-700">{{ invoiceTypeText(detail?.invoiceType) }}</span></div>
          <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{ detail?.patientName || '—' }}</span></div>
          <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{ detail?.patientNo || '—' }}</span></div>
          <div><span class="text-slate-400">收费单号：</span><span class="font-mono text-slate-700">{{ detail?.chargeNo || '—' }}</span></div>
          <div><span class="text-slate-400">金额：</span><span class="font-semibold text-slate-800">¥{{ formatMoney(detail?.totalAmount) }}</span></div>
          <div><span class="text-slate-400">状态：</span><span class="text-slate-700">{{ invoiceStatusText(detail?.invoiceStatus) }}</span></div>
          <div><span class="text-slate-400">开票时间：</span><span class="text-slate-700">{{ detail?.invoiceTime || '—' }}</span></div>
          <div><span class="text-slate-400">打印时间：</span><span class="text-slate-700">{{ detail?.printTime || '—' }}</span></div>
          <div><span class="text-slate-400">作废时间：</span><span class="text-slate-700">{{ detail?.voidTime || '—' }}</span></div>
        </div>
        <div v-if="detail?.voidReason" class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-rose-700">
          作废原因：{{ detail.voidReason }}
        </div>
        <div v-if="detail?.electronicUrl" class="rounded-lg border border-slate-200 p-3">
          电子发票地址：<a :href="detail.electronicUrl" target="_blank" class="text-[#1269B5] hover:underline">{{ detail.electronicUrl }}</a>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
