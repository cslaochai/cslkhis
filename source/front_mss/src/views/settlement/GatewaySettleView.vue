<template>
  <div class="space-y-3">
    <!-- 顶部说明 -->
    <el-alert type="info" :closable="false" show-icon
      title="定点机构（HIS）通过 2304 结算信息上传接口实时报盘，平台侧独立核定统筹并登记台账，作为月度清算的「对方账」依据。" />

    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.settlementNo" placeholder="结算清单号" clearable /></div>
          <div class="q-item"><el-input v-model="q.patientName" placeholder="患者姓名" clearable /></div>
          <div class="q-item"><el-input v-model="q.billNo" placeholder="HIS账单号" clearable /></div>
          <div class="q-item"><el-input v-model="q.tradeNo" placeholder="交易流水号" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="状态" clearable style="width:100%">
              <el-option label="已接收" :value="1" />
              <el-option label="已撤销冲正" :value="2" />
            </el-select>
          </div>
          <div class="q-item">
            <el-date-picker v-model="q.month" type="month" value-format="YYYY-MM"
              placeholder="报盘月份" clearable style="width:100%" />
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
        <el-table-column prop="settlementNo" label="结算清单号" width="160" />
        <el-table-column prop="tradeNo" label="交易流水" width="150" />
        <el-table-column prop="patientName" label="患者" width="80" />
        <el-table-column prop="insuranceType" label="参保类型" width="110" />
        <el-table-column prop="encounterType" label="类型" width="60" align="center" />
        <el-table-column label="总费用" align="right" width="90">
          <template #default="{ row }">{{ fmt(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="机构申报统筹" align="right" width="110">
          <template #default="{ row }"><span class="text-[#7C3AED]">{{ fmt(row.insurancePay) }}</span></template>
        </el-table-column>
        <el-table-column label="平台核定统筹" align="right" width="110">
          <template #default="{ row }">
            <span :class="diff(row) ? 'text-[#DC2626] font-semibold' : 'text-[#059669]'">{{ fmt(row.poolConfirm) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="个人自付" align="right" width="90">
          <template #default="{ row }"><span class="text-[#DC2626]">{{ fmt(row.selfPay) }}</span></template>
        </el-table-column>
        <el-table-column prop="receiptNo" label="医保回执号" width="150" />
        <el-table-column prop="billDate" label="报盘日期" width="110" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已接收' : '已撤销' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDetail(row)">报文明细</el-button>
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

    <!-- 报文明细弹框：只读 -->
    <el-dialog v-model="detailVisible" :title="`2304 报盘接收明细 - ${detail.settlementNo || ''}`" width="820px" top="6vh">
      <template v-if="detail.id">
        <el-descriptions :column="3" size="small" border>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="参保类型">{{ detail.insuranceType }}</el-descriptions-item>
          <el-descriptions-item label="就诊类型">{{ detail.encounterType }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detail.idCard }}</el-descriptions-item>
          <el-descriptions-item label="医保卡号" :span="2">{{ detail.insuranceNo }}</el-descriptions-item>
          <el-descriptions-item label="结算清单号">{{ detail.settlementNo }}</el-descriptions-item>
          <el-descriptions-item label="HIS账单号">{{ detail.billNo }}</el-descriptions-item>
          <el-descriptions-item label="交易流水号">{{ detail.tradeNo }}</el-descriptions-item>
          <el-descriptions-item label="定点机构编码">{{ detail.fixMedinsCode }}</el-descriptions-item>
          <el-descriptions-item label="医保回执号">{{ detail.receiptNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="detail.status === 1 ? 'success' : 'info'">
              {{ detail.status === 1 ? '已接收' : '已撤销冲正' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报盘时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="撤销时间" :span="2">{{ detail.cancelTime || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="grid grid-cols-4 gap-2 mt-3 text-center">
          <div class="p-2 bg-gray-50 rounded"><div class="text-[11px] text-gray-500">总费用</div><div class="amount">{{ fmt(detail.totalAmount) }}</div></div>
          <div class="p-2 bg-[#F3E8FF] rounded"><div class="text-[11px] text-gray-500">机构申报统筹</div><div class="amount !text-[#7C3AED]">{{ fmt(detail.insurancePay) }}</div></div>
          <div class="p-2 bg-[#ECFDF5] rounded"><div class="text-[11px] text-gray-500">平台核定统筹</div>
            <div class="amount" :class="diff(detail) ? '!text-[#DC2626]' : '!text-[#059669]'">{{ fmt(detail.poolConfirm) }}</div>
          </div>
          <div class="p-2 bg-[#FEE2E2] rounded"><div class="text-[11px] text-gray-500">个人自付</div><div class="amount !text-[#DC2626]">{{ fmt(detail.selfPay) }}</div></div>
        </div>

        <el-alert v-if="detail.warnMsg" type="warning" :closable="false" class="mt-3"
          :title="`平台接收告警：${detail.warnMsg}`" />

        <div class="mt-3">
          <div class="text-[13px] font-semibold text-slate-700 mb-1">2304 原始报文（HIS 报盘原文）</div>
          <el-input type="textarea" :model-value="pretty(detail.rawPayload)" :rows="14" readonly
            class="!font-mono text-[12px]" />
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listGatewaySettlements, getGatewaySettlement } from '@/api/insurance'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({
  settlementNo: '', patientName: '', billNo: '', tradeNo: '',
  status: '', month: '', page: 1, size: 10,
})

const detailVisible = ref(false)
const detail = reactive({})

function fmt(n) {
  return Number(n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
// 机构申报与平台核定是否存在差额（保留两位比对，规避浮点尾差）
function diff(row) {
  if (!row) return false
  return Math.round((Number(row.poolConfirm ?? 0) - Number(row.insurancePay ?? 0)) * 100) !== 0
}
function pretty(raw) {
  if (!raw) return ''
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
}

async function load() {
  loading.value = true
  try {
    const res = await listGatewaySettlements({ ...q })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}
function search() {
  q.page = 1
  load()
}
function reset() {
  q.settlementNo = q.patientName = q.billNo = q.tradeNo = q.status = q.month = ''
  q.page = 1
  load()
}
async function openDetail(row) {
  if (!row || !row.id) return
  try {
    const res = await getGatewaySettlement(row.id)
    Object.keys(detail).forEach((k) => delete detail[k])
    Object.assign(detail, res.data || {})
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('报文明细加载失败')
  }
}

onMounted(load)
</script>
