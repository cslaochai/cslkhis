<template>
  <div class="space-y-4">
    <!-- KPI -->
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
      <StatCard label="今日结算笔数" :value="summary.kpi.todayCount" icon="Tickets" color="#1269B5" />
      <StatCard label="今日结算总额(元)" :value="fmt(summary.kpi.todayAmount)" icon="Money" color="#0E9488" />
      <StatCard label="统筹支付(元)" :value="fmt(summary.kpi.insurancePay)" icon="Wallet" color="#7C3AED" />
      <StatCard label="个人账户支付(元)" :value="fmt(summary.kpi.personalAccount)" icon="CreditCard" color="#D97706" />
      <StatCard label="个人现金支付(元)" :value="fmt(summary.kpi.cashPay)" icon="Coin" color="#DC2626" />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <!-- 待办 -->
      <div class="his-card p-4 lg:col-span-1">
        <div class="font-semibold text-[15px] mb-3 text-[#0B3B66]">待办事项</div>
        <div class="space-y-3">
          <div class="flex items-center justify-between p-3 rounded-lg bg-[#E8F1FA]">
            <div><div class="text-[13px] font-medium">待结算业务</div><div class="text-[11px] text-gray-500">门诊 + 住院</div></div>
            <el-button type="primary" link @click="go('/settlement/outpatient')">{{ summary.todo.pendingSettle }} 笔 ›</el-button>
          </div>
          <div class="flex items-center justify-between p-3 rounded-lg bg-[#FEF3C7]">
            <div><div class="text-[13px] font-medium">费用审核</div><div class="text-[11px] text-gray-500">含乙/丙类明细</div></div>
            <el-button type="warning" link @click="go('/audit/fee')">{{ summary.todo.audit }} 条 ›</el-button>
          </div>
          <div class="flex items-center justify-between p-3 rounded-lg bg-[#FCE7F3]">
            <div><div class="text-[13px] font-medium">待清算对账</div><div class="text-[11px] text-gray-500">与医保中心</div></div>
            <el-button type="danger" link @click="go('/reconciliation')">{{ summary.todo.reconcile }} 期 ›</el-button>
          </div>
          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50">
            <div><div class="text-[13px] font-medium">已退费</div><div class="text-[11px] text-gray-500">本期冲正</div></div>
            <span class="text-gray-500 text-[13px]">{{ summary.todo.refund }} 笔</span>
          </div>
          <div class="flex items-center justify-between p-3 rounded-lg bg-[#FEF9C3]">
            <div><div class="text-[13px] font-medium">待核实监管疑点</div><div class="text-[11px] text-gray-500">规则引擎稽核</div></div>
            <el-button type="warning" link @click="go('/supervise/suspicion')">{{ summary.todo.suspicion }} 条 ›</el-button>
          </div>
          <div class="flex items-center justify-between p-3 rounded-lg bg-[#FEE2E2]">
            <div><div class="text-[13px] font-medium">待追回违规资金</div><div class="text-[11px] text-gray-500">疑点/飞检确认</div></div>
            <el-button type="danger" link @click="go('/supervise/recover')">{{ summary.todo.recover }} 张 ›</el-button>
          </div>
        </div>
      </div>

      <!-- 最近结算 -->
      <div class="his-card p-4 lg:col-span-2">
        <div class="font-semibold text-[15px] mb-3 text-[#0B3B66]">最近结算单</div>
        <el-table :data="summary.recentBills" size="small" :max-height="300">
          <el-table-column prop="billNo" label="结算单号" width="150" />
          <el-table-column prop="patientName" label="患者" width="90" />
          <el-table-column prop="insuranceType" label="参保类型" width="100" />
          <el-table-column prop="settleType" label="类型" width="70" />
          <el-table-column label="总费用" align="right" width="100">
            <template #default="{ row }">{{ fmt(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column label="统筹支付" align="right" width="100">
            <template #default="{ row }"><span class="text-[#7C3AED]">{{ fmt(row.insurancePay) }}</span></template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import StatCard from '@/components/StatCard.vue'
import { dashboardSummary } from '@/api/insurance'

const router = useRouter()
const summary = reactive({ kpi: {}, todo: {}, recentBills: [] })

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 已结算: 'success', 待审核: 'warning', 已退费: 'info', 异常: 'danger' }[s] || ''
}
function go(p) {
  router.push(p)
}
onMounted(async () => {
  const res = await dashboardSummary()
  Object.assign(summary, res.data)
})
</script>
