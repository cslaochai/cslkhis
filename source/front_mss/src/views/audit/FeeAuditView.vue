<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.keyword" placeholder="结算单号/患者/项目" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.payClass" placeholder="支付类别" clearable style="width:100%">
              <el-option label="乙类" value="乙" />
              <el-option label="丙类" value="丙" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="审核状态" clearable style="width:100%">
              <el-option label="待审核" value="待审核" />
              <el-option label="已通过" value="已通过" />
              <el-option label="已驳回" value="已驳回" />
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
      <div class="flex items-center justify-between mb-2">
        <div class="font-semibold text-[15px] text-[#0B3B66]">费用合规性审核 <el-tag size="small" type="warning">{{ pending }}</el-tag></div>
        <el-radio-group v-model="batch" size="small">
          <el-radio-button label="待我审核" value="todo" />
          <el-radio-button label="全部" value="all" />
        </el-radio-group>
      </div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="billNo" label="结算单号" width="160" />
        <el-table-column prop="patientName" label="患者" width="90" />
        <el-table-column prop="itemName" label="费用项目" min-width="140" />
        <el-table-column prop="category" label="类别" width="90" />
        <el-table-column prop="payClass" label="支付类别" width="80" align="center">
          <template #default="{ row }"><el-tag size="small" :type="payClassType(row.payClass)">{{ row.payClass }}类</el-tag></template>
        </el-table-column>
        <el-table-column label="金额" align="right" width="100">
          <template #default="{ row }">{{ fmt(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="审核要点" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" :disabled="row.status !== '待审核'" @click="pass(row)">通过</el-button>
            <el-button link type="danger" :disabled="row.status !== '待审核'" @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-3">
        <el-pagination
          :page-sizes="PAGE_SIZES" :page-size="q.size" :current-page="q.page" :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="(p) => { q.page = p; load() }"
          @size-change="(s) => { q.size = s; q.page = 1; load() }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { feeAuditList, auditPass, auditReject } from '@/api/insurance'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const batch = ref('todo')
const q = reactive({ keyword: '', payClass: '', status: '待审核', page: 1, size: 10 })

const pending = computed(() => list.value.filter((x) => x.status === '待审核').length)

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 待审核: 'warning', 已通过: 'success', 已驳回: 'danger' }[s] || 'info'
}
function payClassType(c) {
  return { 甲: 'success', 乙: 'warning', 丙: 'danger' }[c] || 'info'
}
async function load() {
  loading.value = true
  const params = { ...q }
  if (batch.value === 'todo') params.status = '待审核'
  const res = await feeAuditList(params)
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() { q.page = 1; load() }
function reset() { q.keyword = q.payClass = ''; q.status = '待审核'; q.page = 1; load() }
async function pass(row) {
  await auditPass(row.id)
  ElMessage.success('已通过')
  load()
}
async function reject(row) {
  await auditReject(row.id)
  ElMessage.warning('已驳回')
  load()
}
onMounted(load)
</script>
