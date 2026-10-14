<template>
  <div class="space-y-3">
    <div class="his-card p-4">
      <div class="font-semibold text-[15px] mb-1 text-[#0B3B66]">医保清算对账</div>
      <div class="text-[12px] text-gray-500">按月与医保中心清算资金核对：结算申报金额 vs 基金实际到账，差额需排查退费/冲正/拨付差异。</div>
    </div>

    <div class="his-card p-3">
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="period" label="清算账期" width="110" />
        <el-table-column label="结算笔数" align="right" width="100">
          <template #default="{ row }">{{ row.billCount.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="申报结算额" align="right" width="130">
          <template #default="{ row }">{{ fmt(row.settleAmount) }}</template>
        </el-table-column>
        <el-table-column label="统筹申报支付" align="right" width="130">
          <template #default="{ row }"><span class="text-[#7C3AED]">{{ fmt(row.insurancePay) }}</span></template>
        </el-table-column>
        <el-table-column label="基金到账" align="right" width="130">
          <template #default="{ row }"><span class="text-[#0E9488]">{{ fmt(row.receivedFromFund) }}</span></template>
        </el-table-column>
        <el-table-column label="差额" align="right" width="120">
          <template #default="{ row }">
            <span :class="Math.abs(row.diff) < 0.01 ? 'text-gray-400' : 'text-[#DC2626] font-semibold'">{{ fmt(row.diff) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== '待对账'" @click="reconcile(row)">执行对账</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-2">
        <el-pagination :page-sizes="PAGE_SIZES" :page-size="q.size" :current-page="q.page" :total="total"
          layout="total, sizes, prev, pager, next" @current-change="(p)=>{q.page=p;load()}" @size-change="(s)=>{q.size=s;q.page=1;load()}" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReconciliation, doReconcile } from '@/api/insurance'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ page: 1, size: 10 })

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 已对账: 'success', 待对账: 'warning', 存在差异: 'danger' }[s] || 'info'
}
async function load() {
  loading.value = true
  const res = await listReconciliation({ ...q })
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
async function reconcile(row) {
  await ElMessageBox.confirm(`对 ${row.period} 账期执行与医保中心对账？`, '清算对账', { type: 'info' })
  await doReconcile(row.period)
  ElMessage.success(`${row.period} 对账完成，差额已平账`)
  load()
}
onMounted(load)
</script>
