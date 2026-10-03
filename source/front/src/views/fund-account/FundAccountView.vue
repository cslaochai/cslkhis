<script setup lang="ts">
/**
 * L3 资金账户台账（biz_fund_account / biz_fund_account_txn）—— 只读。
 *
 * 门诊余额与住院预交金统一账本，差别只在 owner_type：
 * 1-患者（门诊余额）2-住院就诊次（预交金）。
 * 余额永远 SUM(流水)，biz_fund_account.balance 只是行锁保护下的缓存 ——
 * 页面不提供改账口子：能直接改余额就等于账本作废。
 */
import {onMounted, reactive, ref} from 'vue'
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {ACCOUNT_STATUS, getFundAccountListPage, listFundTxnsByAccount} from '@/api/fundAccount'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText, formatMoney} from '@/lib/utils'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const dicts = ref<Record<string, any[]>>({})
const dictOptions = (type: string) => dicts.value[type] || []

const ownerTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.ACCOUNT_OWNER_TYPE), v)
const txnTypeText = (v: any) => dictLabelText(dictOptions(DICT_TYPE.ACCOUNT_TXN_TYPE), v)
const accountStatusText = (v: any) => (ACCOUNT_STATUS as any)[String(v)] || `未知(${v})`

const loading = ref(true)
const rows = ref<any[]>([])
const query = reactive({
  keyword: '',
  ownerType: null as number | null,
})
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

async function loadList() {
  loading.value = true
  try {
    const res = await getFundAccountListPage({
      keyword: query.keyword || undefined,
      ownerType: query.ownerType ?? undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    rows.value = res.data?.records || []
    pagination.total = Number(res.data?.total || 0)
  } catch (e: any) {
    rows.value = []
    ElMessage.error(e.message || '加载资金账户失败')
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
  query.ownerType = null
  handleSearch()
}

// ==================== 账户流水详情 ====================

const txnVisible = ref(false)
const txnLoading = ref(false)
const txnAccount = ref<any>(null)
const txnRows = ref<any[]>([])

async function openTxns(row: any) {
  txnAccount.value = row
  txnVisible.value = true
  txnLoading.value = true
  try {
    const res = await listFundTxnsByAccount(row.id)
    txnRows.value = res.data || []
  } catch (e: any) {
    txnRows.value = []
    ElMessage.error(e.message || '加载账户流水失败')
  } finally {
    txnLoading.value = false
  }
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.ACCOUNT_OWNER_TYPE,
    DICT_TYPE.ACCOUNT_TXN_TYPE,
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
              data-testid="fund-keyword"
              placeholder="患者姓名 / 患者号"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="账户主体">
          <el-select v-model="query.ownerType" data-testid="fund-owner-type" placeholder="账户主体" clearable style="width: 170px">
            <el-option v-for="o in dictOptions(DICT_TYPE.ACCOUNT_OWNER_TYPE)" :key="o.dictValue" :label="o.dictLabel" :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" data-testid="fund-search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="fund-table">
        <el-table-column prop="patientName" label="患者" min-width="90"/>
        <el-table-column prop="patientNo" label="患者号" min-width="110" class-name="font-mono"/>
        <el-table-column label="主体" min-width="150">
          <template #default="{ row }">{{ ownerTypeText(row.ownerType) }}</template>
        </el-table-column>
        <el-table-column label="余额" min-width="100" align="right">
          <template #default="{ row }">
            <span :class="Number(row.balance) < 0 ? 'text-rose-600' : 'text-slate-700'">¥{{ formatMoney(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="累计充值" min-width="100" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.totalRecharge) }}</template>
        </el-table-column>
        <el-table-column label="累计消耗" min-width="100" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.totalConsume) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">{{ accountStatusText(row.accountStatus) }}</template>
        </el-table-column>
        <el-table-column prop="lastTxnTime" label="最近流水" min-width="150" class-name="font-mono"/>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" data-testid="fund-txn" @click="openTxns(row)">流水</el-button>
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

    <el-dialog v-model="txnVisible" :title="`账户流水 — ${txnAccount?.patientName || ''}（余额 ¥${formatMoney(txnAccount?.balance)}）`" width="820px">
      <el-table v-loading="txnLoading" :data="txnRows" border size="small" data-testid="fund-txn-table" max-height="480">
        <el-table-column prop="txnNo" label="流水号" min-width="160" class-name="font-mono text-xs"/>
        <el-table-column label="类型" min-width="130">
          <template #default="{ row }">{{ txnTypeText(row.txnType) }}</template>
        </el-table-column>
        <el-table-column label="变动" min-width="95" align="right">
          <template #default="{ row }">
            <span :class="Number(row.amount) < 0 ? 'text-rose-600' : 'text-emerald-600'">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" min-width="100" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.balanceAfter) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="摘要" min-width="180" show-overflow-tooltip/>
        <el-table-column prop="operatorName" label="操作人" min-width="90"/>
        <el-table-column prop="txnTime" label="时间" min-width="150" class-name="font-mono text-xs"/>
      </el-table>
    </el-dialog>
  </div>
</template>
