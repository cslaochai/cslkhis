<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.billNo" placeholder="结算单号" clearable /></div>
          <div class="q-item"><el-input v-model="q.patientName" placeholder="患者姓名" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.insuranceType" placeholder="参保类型" clearable style="width:100%">
              <el-option v-for="t in insTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.settleType" placeholder="结算类型" clearable style="width:100%">
              <el-option label="门诊" value="门诊" />
              <el-option label="住院" value="住院" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="状态" clearable style="width:100%">
              <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
            </el-select>
          </div>
        </div>
        <div class="query-grid-ops">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="his-card p-3">
      <el-table :data="list" size="small" v-loading="loading" @row-click="openDetail">
        <el-table-column prop="billNo" label="结算单号" width="160" />
        <el-table-column prop="patientName" label="患者" width="90" />
        <el-table-column prop="insuranceType" label="参保类型" width="100" />
        <el-table-column prop="settleType" label="类型" width="70" />
        <el-table-column prop="dept" label="科室" width="110" />
        <el-table-column label="总费用" align="right" width="100">
          <template #default="{ row }">{{ fmt(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="统筹支付" align="right" width="100">
          <template #default="{ row }"><span class="text-[#7C3AED]">{{ fmt(row.insurancePay) }}</span></template>
        </el-table-column>
        <el-table-column label="现金支付" align="right" width="100">
          <template #default="{ row }"><span class="text-[#DC2626]">{{ fmt(row.cashPay) }}</span></template>
        </el-table-column>
        <el-table-column prop="settleTime" label="结算时间" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDetail(row)">明细</el-button>
            <el-button link type="danger" :disabled="row.status === '已退费' || row.status === '异常'" @click.stop="reverse(row)">退费</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-3">
        <el-pagination
          :page-sizes="PAGE_SIZES"
          :page-size="q.size"
          :current-page="q.page"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="(p) => { q.page = p; load() }"
          @size-change="(s) => { q.size = s; q.page = 1; load() }"
        />
      </div>
    </div>

    <!-- 明细弹框：只读 -->
    <el-dialog v-model="detailVisible" :title="`结算单明细 - ${detail.billNo}`" width="760px">
      <template v-if="detail.id">
        <el-descriptions :column="3" size="small" border>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="参保类型">{{ detail.insuranceType }}</el-descriptions-item>
          <el-descriptions-item label="结算类型">{{ detail.settleType }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ detail.dept }}</el-descriptions-item>
          <el-descriptions-item label="医师">{{ detail.doctor }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="statusType(detail.status)">{{ detail.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items" size="small" border class="mt-3">
          <el-table-column prop="name" label="项目" min-width="140" />
          <el-table-column prop="payClass" label="类别" width="70" align="center">
            <template #default="{ row }"><el-tag size="small" :type="payClassType(row.payClass)">{{ row.payClass }}类</el-tag></template>
          </el-table-column>
          <el-table-column prop="qty" label="数量" width="60" align="right" />
          <el-table-column label="金额" align="right" width="100">
            <template #default="{ row }">{{ fmt(row.qty * row.price) }}</template>
          </el-table-column>
        </el-table>
        <div class="grid grid-cols-4 gap-2 mt-3 text-center">
          <div class="p-2 bg-gray-50 rounded"><div class="text-[11px] text-gray-500">总额</div><div class="amount">{{ fmt(detail.totalAmount) }}</div></div>
          <div class="p-2 bg-[#F3E8FF] rounded"><div class="text-[11px] text-gray-500">统筹</div><div class="amount !text-[#7C3AED]">{{ fmt(detail.insurancePay) }}</div></div>
          <div class="p-2 bg-[#FEF3C7] rounded"><div class="text-[11px] text-gray-500">账户</div><div class="amount !text-[#D97706]">{{ fmt(detail.personalAccount) }}</div></div>
          <div class="p-2 bg-[#FEE2E2] rounded"><div class="text-[11px] text-gray-500">现金</div><div class="amount !text-[#DC2626]">{{ fmt(detail.cashPay) }}</div></div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBills, getBillDetail, reverseBill } from '@/api/insurance'
import { INSURANCE_TYPES } from '@/lib/dict'
import { PAGE_SIZES } from '@/lib/pagination'

const insTypes = INSURANCE_TYPES
const statuses = ['已结算', '待审核', '已退费', '异常']
const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ billNo: '', patientName: '', insuranceType: '', settleType: '', status: '', page: 1, size: 10 })

const detailVisible = ref(false)
const detail = reactive({})

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 已结算: 'success', 待审核: 'warning', 已退费: 'info', 异常: 'danger' }[s] || ''
}
function payClassType(c) {
  return { 甲: 'success', 乙: 'warning', 丙: 'danger' }[c] || 'info'
}
async function load() {
  loading.value = true
  const res = await listBills({ ...q })
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() {
  q.page = 1
  load()
}
function reset() {
  q.billNo = q.patientName = q.insuranceType = q.settleType = q.status = ''
  q.page = 1
  load()
}
async function openDetail(row) {
  const res = await getBillDetail(row.id)
  Object.assign(detail, res.data)
  detailVisible.value = true
}
async function reverse(row) {
  await ElMessageBox.confirm(`确认对结算单 ${row.billNo} 执行退费冲正？`, '退费冲正', { type: 'warning' })
  const res = await reverseBill(row.id)
  if (res.data.ok) {
    ElMessage.success('已退费')
    load()
  }
}
onMounted(load)
</script>
