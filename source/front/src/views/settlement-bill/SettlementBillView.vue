<script setup lang="ts">
/**
 * L2 结算账单台账（biz_settlement_bill）
 *
 * 账单把 N 条记账行锁定成一张应收。本页是台账 + 详情 + 作废：
 * - 结算/收款的主战场在「收费结算窗口」（CashierView），这里不做第二条出账链；
 * - unpaidAmount 由后端按流水比出来，前端不再减一遍；
 * - 作废只对没有收款流水的账单可用（后端把关，这里只透传原因）。
 */
import {onMounted, reactive, ref} from 'vue'
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getBillDetailById, getBillListPage, voidBill} from '@/api/settlementBill'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText, formatMoney} from '@/lib/utils'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const dicts = ref<Record<string, any[]>>({})
const dictOptions = (type: string) => dicts.value[type] || []

const billStatusText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.BILL_STATUS), v)
const billTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.BILL_TYPE), v)
const sourceTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.TXN_SOURCE), v)
const payMethodText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.PAY_METHOD), v)

/** 金额列语义：应收 totalAmount → 患者该掏 payableAmount → 实收 paidAmount → 尚欠 unpaidAmount */
const statusTagClass = (v: any) => {
  const map: Record<string, string> = {'1': 'bg-amber-50 text-amber-700', '2': 'bg-orange-50 text-orange-700', '3': 'bg-emerald-50 text-emerald-700', '4': 'bg-slate-100 text-slate-500', '5': 'bg-rose-50 text-rose-700'}
  return map[String(v)] || 'bg-slate-100 text-slate-600'
}

const loading = ref(true)
const rows = ref<any[]>([])
const query = reactive({
  keyword: '',
  billStatus: null as number | null,
  billType: null as number | null,
})
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

async function loadList() {
  loading.value = true
  try {
    const res = await getBillListPage({
      keyword: query.keyword || undefined,
      billStatus: query.billStatus ?? undefined,
      billType: query.billType ?? undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    rows.value = res.data?.records || []
    pagination.total = Number(res.data?.total || 0)
  } catch (e: any) {
    rows.value = []
    ElMessage.error(e.message || '加载账单失败')
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
  query.billStatus = null
  query.billType = null
  handleSearch()
}

// ==================== 详情（行快照 + 收/退流水一次给全） ====================

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>(null)

async function openDetail(row: any) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getBillDetailById(row.id)
    detail.value = res.data || null
  } catch (e: any) {
    detail.value = null
    ElMessage.error(e.message || '加载账单详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ==================== 作废（取消结算） ====================

const voidSaving = ref(false)

async function runVoid(row: any) {
  const res = await ElMessageBox.prompt(
      `将作废账单 ${row.billNo} 并解锁名下记账行（回到待结算）。已有收款流水的账单无法作废，请先走退费。`,
      '取消结算',
      {inputPlaceholder: '作废原因（必填）', inputPattern: /\S/, inputErrorMessage: '作废原因必填', type: 'warning'},
  ).catch(() => null)
  if (!res) return
  voidSaving.value = true
  try {
    await voidBill({billId: row.id, reason: res.value})
    ElMessage.success('已作废，记账行已解锁')
    await loadList()
  } catch (e: any) {
    ElMessage.error(e.message || '作废失败')
  } finally {
    voidSaving.value = false
  }
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.BILL_STATUS,
    DICT_TYPE.BILL_TYPE,
    DICT_TYPE.PAY_METHOD,
    DICT_TYPE.TXN_SOURCE,
    DICT_TYPE.PAY_DIRECTION,
    DICT_TYPE.PAY_TXN_STATUS,
  ].join(','))
  await loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              data-testid="bill-keyword"
              placeholder="账单号 / 患者姓名"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="账单状态">
          <el-select v-model="query.billStatus" data-testid="bill-status" placeholder="账单状态" clearable style="width: 140px">
            <el-option v-for="o in dictOptions(DICT_TYPE.BILL_STATUS)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="query.billType" data-testid="bill-type" placeholder="账单类型" clearable style="width: 140px">
            <el-option v-for="o in dictOptions(DICT_TYPE.BILL_TYPE)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" data-testid="bill-search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="bill-table">
        <el-table-column prop="billNo" label="账单号" min-width="150" class-name="font-mono"/>
        <el-table-column prop="patientName" label="患者" min-width="90"/>
        <el-table-column label="类型" min-width="90">
          <template #default="{ row }">{{ billTypeText(row.billType) }}</template>
        </el-table-column>
        <el-table-column label="应收" min-width="90" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="应缴" min-width="90" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.payableAmount) }}</template>
        </el-table-column>
        <el-table-column label="已收" min-width="90" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="尚欠" min-width="90" align="right">
          <template #default="{ row }">
            <span :class="Number(row.unpaidAmount) > 0 ? 'text-rose-600' : 'text-slate-500'">¥{{ formatMoney(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="95">
          <template #default="{ row }">
            <span class="px-2 py-0.5 rounded" :class="statusTagClass(row.billStatus)">{{ billStatusText(row.billStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="出账时间" min-width="150" class-name="font-mono"/>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" data-testid="bill-detail" @click="openDetail(row)">详情</el-button>
            <el-button
                v-if="Number(row.billStatus) === 1 || Number(row.billStatus) === 2"
                link
                type="danger"
                data-testid="bill-void"
                :disabled="Number(row.paidAmount) > 0"
                @click="runVoid(row)"
            >作废</el-button>
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

    <el-dialog v-model="detailVisible" title="账单详情（行快照 + 收/退流水）" width="860px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="账单号">{{ detail.billNo }}</el-descriptions-item>
            <el-descriptions-item label="患者">{{ detail.patientName }}（{{ detail.patientNo || '—' }}）</el-descriptions-item>
            <el-descriptions-item label="状态">{{ billStatusText(detail.billStatus) }}</el-descriptions-item>
            <el-descriptions-item label="应收">¥{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="优惠">¥{{ formatMoney(detail.discountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="统筹">¥{{ formatMoney(detail.poolAmount) }}</el-descriptions-item>
            <el-descriptions-item label="应缴">¥{{ formatMoney(detail.payableAmount) }}</el-descriptions-item>
            <el-descriptions-item label="已收">¥{{ formatMoney(detail.paidAmount) }}</el-descriptions-item>
            <el-descriptions-item label="尚欠">¥{{ formatMoney(detail.unpaidAmount) }}</el-descriptions-item>
          </el-descriptions>

          <div class="mt-3 mb-1 text-sm font-medium">账单行快照</div>
          <el-table :data="detail.items || []" border size="small">
            <el-table-column prop="itemName" label="项目" min-width="150" show-overflow-tooltip/>
            <el-table-column prop="price" label="单价" min-width="80" align="right">
              <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" min-width="70" align="right"/>
            <el-table-column prop="amount" label="金额" min-width="90" align="right">
              <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
            </el-table-column>
          </el-table>

          <div class="mt-3 mb-1 text-sm font-medium">收/退流水</div>
          <el-table :data="detail.txns || []" border size="small" data-testid="bill-txn-table">
            <el-table-column prop="txnNo" label="流水号" min-width="150" class-name="font-mono text-xs"/>
            <el-table-column label="方向" min-width="70">
              <template #default="{ row }">{{ dictLabelText(dictOptions(DICT_TYPE.PAY_DIRECTION), row.direction) }}</template>
            </el-table-column>
            <el-table-column label="渠道" min-width="90">
              <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" min-width="90" align="right">
              <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">{{ dictLabelText(dictOptions(DICT_TYPE.PAY_TXN_STATUS), row.txnStatus) }}</template>
            </el-table-column>
            <el-table-column label="来源" min-width="90">
              <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column prop="txnTime" label="时间" min-width="150" class-name="font-mono text-xs"/>
          </el-table>
        </template>
      </div>
    </el-dialog>
  </div>
</template>
