<template>
  <div class="grid grid-cols-1 lg:grid-cols-5 gap-4 h-full">
    <!-- 待结算列表 -->
    <div class="his-card p-3 lg:col-span-2 flex flex-col">
      <div class="font-semibold text-[15px] mb-2 text-[#0B3B66]">门诊待结算业务 <el-tag size="small" type="info">{{ list.length }}</el-tag></div>
      <el-table :data="list" highlight-current-row size="small" height="calc(100vh - 220px)" @current-change="onSelect">
        <el-table-column prop="visitNo" label="就诊号" width="110" />
        <el-table-column prop="patientName" label="患者" width="80" />
        <el-table-column prop="insuranceType" label="参保" width="90" />
        <el-table-column prop="dept" label="科室" />
      </el-table>
    </div>

    <!-- 结算明细 -->
    <div class="his-card p-4 lg:col-span-3" v-loading="loading">
      <template v-if="current">
        <div class="flex items-center justify-between mb-3">
          <div>
            <span class="text-[16px] font-semibold">{{ current.patientName }}</span>
            <span class="ml-3 text-gray-500 text-[13px]">{{ current.visitNo }} · {{ current.dept }} · {{ current.doctor }}</span>
          </div>
          <el-tag>{{ current.insuranceType }}</el-tag>
        </div>

        <el-table :data="current.items" size="small" border :max-height="260">
          <el-table-column prop="name" label="项目" min-width="140" />
          <el-table-column prop="category" label="类别" width="90" />
          <el-table-column prop="spec" label="规格" width="90" />
          <el-table-column prop="ybCode" label="医保编码" width="120" />
          <el-table-column prop="payClass" label="支付类别" width="80" align="center">
            <template #default="{ row }"><el-tag size="small" :type="payClassType(row.payClass)">{{ row.payClass }}类</el-tag></template>
          </el-table-column>
          <el-table-column prop="qty" label="数量" width="60" align="right" />
          <el-table-column label="单价" align="right" width="80">
            <template #default="{ row }">{{ fmt(row.price) }}</template>
          </el-table-column>
          <el-table-column label="金额" align="right" width="90">
            <template #default="{ row }"><span class="amount">{{ fmt(row.qty * row.price) }}</span></template>
          </el-table-column>
        </el-table>

        <!-- 医保分解 -->
        <div class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-3">
          <div class="p-3 rounded-lg bg-gray-50 text-center">
            <div class="text-[12px] text-gray-500">费用总额</div>
            <div class="amount-strong">{{ fmt(dec.total) }}</div>
          </div>
          <div class="p-3 rounded-lg bg-[#F3E8FF] text-center">
            <div class="text-[12px] text-gray-500">统筹支付</div>
            <div class="amount-strong !text-[#7C3AED]">{{ fmt(dec.insurancePay) }}</div>
          </div>
          <div class="p-3 rounded-lg bg-[#FEF3C7] text-center">
            <div class="text-[12px] text-gray-500">个人账户</div>
            <div class="amount-strong !text-[#D97706]">{{ fmt(dec.personalAccount) }}</div>
          </div>
          <div class="p-3 rounded-lg bg-[#FEE2E2] text-center">
            <div class="text-[12px] text-gray-500">现金支付</div>
            <div class="amount-strong !text-[#DC2626]">{{ fmt(dec.cashPay) }}</div>
          </div>
        </div>
        <div class="text-[11px] text-gray-400 mt-2">
          起付线 {{ fmt(dec.deductible) }} 元 · 统筹计入基数 {{ fmt(dec.basePool) }} 元
          （甲类全额、乙类扣除 {{ (dec.basePool ? '' : '') }}自付比例后计入）
        </div>

        <div class="mt-4 flex justify-end gap-3">
          <el-button @click="onSelect(null)">取消</el-button>
          <el-button type="primary" :disabled="!current" @click="doSettle">确认结算并生成结算单</el-button>
        </div>
      </template>
      <el-empty v-else description="请从左侧选择一笔待结算门诊业务" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listPendingOutpatient, settleOutpatient, previewOutpatient } from '@/api/insurance'

const list = ref([])
const current = ref(null)
const loading = ref(false)

function zeroDec() {
  return { total: 0, insurancePay: 0, personalAccount: 0, cashPay: 0, selfPay: 0, deductible: 0, basePool: 0 }
}
const dec = ref(zeroDec())

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function payClassType(c) {
  return { 甲: 'success', 乙: 'warning', 丙: 'danger' }[c] || 'info'
}
async function onSelect(row) {
  current.value = row || null
  dec.value = zeroDec()
  if (!row) return
  const res = await previewOutpatient(row.visitNo)
  if (res.data) dec.value = res.data
}
async function doSettle() {
  loading.value = true
  const res = await settleOutpatient(current.value.visitNo)
  loading.value = false
  if (res.data.error) {
    ElMessage.error(res.data.error)
    return
  }
  ElMessage.success(`结算成功，结算单号 ${res.data.bill.billNo}`)
  await onSelect(null)
  await load()
}
async function load() {
  const res = await listPendingOutpatient({ page: 1, size: 100 })
  list.value = res.data.list
}
onMounted(load)
</script>
