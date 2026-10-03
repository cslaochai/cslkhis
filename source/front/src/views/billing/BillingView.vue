<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search, Money, CreditCard, Lock, Document } from '@element-plus/icons-vue'
import { getBillListPage, getBillDetailById } from '@/api/settlementBill'

interface BillingRecord {
  id: string
  billNo: string
  patientName: string
  patientNo: string
  totalAmount: number
  paidAmount: number
  billStatus: number
  billTime: string
  details?: any[]
}

const searchTerm = ref('')
const statusFilter = ref<number | string>('all')
const selectedRecord = ref<BillingRecord | null>(null)
const loading = ref(false)
const billingRecords = ref<BillingRecord[]>([])

// 账单状态字典 his_bill_status：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费
const statusMap: Record<number, string> = {
  1: '待支付',
  2: '部分支付',
  3: '已支付',
  4: '已作废',
  5: '已退费',
}

const filtered = computed(() =>
  billingRecords.value.filter((r) => {
    const matchSearch = r.patientName?.includes(searchTerm.value) || r.billNo?.includes(searchTerm.value)
    const matchStatus = statusFilter.value === 'all' || r.billStatus === statusFilter.value
    return matchSearch && matchStatus
  })
)

const totalRevenue = computed(() => billingRecords.value.reduce((sum, r) => sum + (r.totalAmount || 0), 0))
// 已收款 = 已支付账单的实收合计（权威口径在 biz_payment_txn，这里用账单镜像 paidAmount）
const totalPaid = computed(() => billingRecords.value.filter(r => r.billStatus === 3).reduce((sum, r) => sum + (r.paidAmount || 0), 0))

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBillListPage({ pageNum: 1, pageSize: 100 })
    const records = res.data?.records || []
    billingRecords.value = records.map((item: any) => ({
      id: item.id,
      billNo: item.billNo,
      patientName: item.patientName,
      patientNo: item.patientNo,
      totalAmount: item.totalAmount,
      paidAmount: item.paidAmount,
      billStatus: item.billStatus,
      billTime: item.billTime,
    }))
  } catch (error) {
    console.error('加载收费记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (record: BillingRecord) => {
  try {
    const res = await getBillDetailById(record.id)
    selectedRecord.value = res.data || record
  } catch (error) {
    selectedRecord.value = record
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-slate-900">收费管理</h1>
      <p class="mt-1 text-sm text-slate-500">管理费用结算、医保报销和收费记录</p>
    </div>

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-4">
      <div v-for="item in [
        { label: '总费用', value: `¥${totalRevenue.toLocaleString()}`, icon: Money, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: '已收款', value: `¥${totalPaid.toLocaleString()}`, icon: CreditCard, color: 'text-emerald-600', bg: 'bg-emerald-50' },
        { label: '收费笔数', value: billingRecords.length, icon: Document, color: 'text-purple-600', bg: 'bg-purple-50' },
        { label: '已支付', value: billingRecords.filter((r) => r.billStatus === 3).length, icon: Lock, color: 'text-amber-600', bg: 'bg-amber-50' },
      ]" :key="item.label" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div :class="['rounded-lg p-2.5', item.bg]">
            <component :is="item.icon" :class="['h-5 w-5', item.color]" />
          </div>
          <div>
            <p :class="['text-lg font-bold', item.color]">{{ item.value }}</p>
            <p class="text-xs text-slate-500">{{ item.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex items-center gap-4">
        <el-input v-model="searchTerm" placeholder="搜索患者姓名、单据号..." :prefix-icon="Search" class="flex-1" />
        <el-select v-model="statusFilter" class="!w-36" placeholder="结算状态">
          <el-option label="全部状态" value="all" />
          <el-option label="待结算" :value="1" />
          <el-option label="已结算" :value="2" />
          <el-option label="已退费" :value="3" />
        </el-select>
      </div>
    </div>

    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-table :data="filtered" style="width: 100%" @row-click="handleViewDetail">
        <el-table-column prop="billNo" label="账单号" class-name="font-mono text-sm" width="180" />
        <el-table-column prop="patientName" label="患者" />
        <el-table-column label="总费用" align="right" width="100">
          <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="实收" align="right" width="100">
          <template #default="{ row }">¥{{ row.paidAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :class="row.billStatus === 3 ? 'bg-emerald-100 text-emerald-700' : row.billStatus === 1 || row.billStatus === 2 ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-600'" effect="plain" size="small" class="border">{{ statusMap[row.billStatus] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billTime" label="时间" width="200" />
      </el-table>
    </div>

    <el-dialog v-model="selectedRecord" :title="`费用明细 - ${selectedRecord?.patientName}`" width="600px" destroy-on-close>
      <template v-if="selectedRecord">
        <div class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4 text-sm">
            <div><span class="text-slate-400">账单号：</span><span class="font-mono text-slate-700">{{ selectedRecord.billNo }}</span></div>
            <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{ selectedRecord.patientName }}</span></div>
            <div><span class="text-slate-400">总费用：</span><span class="text-slate-700">¥{{ selectedRecord.totalAmount?.toFixed(2) }}</span></div>
            <div><span class="text-slate-400">实收：</span><span class="text-slate-700">¥{{ selectedRecord.paidAmount?.toFixed(2) }}</span></div>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
