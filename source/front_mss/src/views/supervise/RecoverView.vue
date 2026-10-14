<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.keyword" placeholder="追回单号/来源单号/机构" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.sourceType" placeholder="来源类型" clearable style="width:100%">
              <el-option label="疑点稽核" :value="1" />
              <el-option label="飞检" :value="2" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="状态" clearable style="width:100%">
              <el-option label="待追回" value="待追回" />
              <el-option label="已追回" value="已追回" />
              <el-option label="已核销" value="已核销" />
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
        <div class="font-semibold text-[15px] text-[#0B3B66]">违规追回台账 <el-tag size="small" type="danger">本页待追回 {{ pendingCount }}</el-tag></div>
        <div class="text-[12px] text-gray-500">本页合计 {{ fmt(pageSum) }} 元</div>
      </div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="recoverNo" label="追回单号" width="170" />
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">{{ row.sourceType === 2 ? '飞检' : '疑点稽核' }}</template>
        </el-table-column>
        <el-table-column prop="sourceNo" label="来源单号" width="170" show-overflow-tooltip />
        <el-table-column prop="targetOrgName" label="定点机构" min-width="150" show-overflow-tooltip />
        <el-table-column prop="month" label="所属账期" width="90" align="center" />
        <el-table-column label="追回金额" align="right" width="120">
          <template #default="{ row }">{{ fmt(row.recoverAmount) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="追回原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === '待追回'" link type="success" @click="onRecover(row)">确认追回</el-button>
            <el-button v-if="row.status === '待追回'" link type="info" @click="onWriteOff(row)">核销</el-button>
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

    <RecoverDetail v-model="detailVisible" :id="currentId" @saved="load" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRecovers, doRecover, writeOffRecover } from '@/api/supervise'
import { PAGE_SIZES } from '@/lib/pagination'
import RecoverDetail from './RecoverDetail.vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', sourceType: '', status: '', page: 1, size: 10 })

const pendingCount = computed(() => list.value.filter((x) => x.status === '待追回').length)
const pageSum = computed(() => list.value.reduce((acc, x) => acc + (Number(x.recoverAmount) || 0), 0))

const detailVisible = ref(false)
const currentId = ref(null)
function openDetail(row) {
  currentId.value = row.id
  detailVisible.value = true
}

async function onRecover(row) {
  await ElMessageBox.confirm(`确认已从「${row.targetOrgName}」追回 ${fmt(row.recoverAmount)} 元？`, '确认追回', { type: 'warning' })
  await doRecover({ id: row.id })
  ElMessage.success('已确认追回')
  load()
}

async function onWriteOff(row) {
  await ElMessageBox.confirm('确认核销该追回单？（追回单关闭，金额不再追收）', '核销', { type: 'warning' })
  await writeOffRecover({ id: row.id })
  ElMessage.info('已核销')
  load()
}

async function load() {
  loading.value = true
  const res = await listRecovers(q)
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() { q.page = 1; load() }
function reset() { q.keyword = q.sourceType = q.status = ''; q.page = 1; load() }

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 待追回: 'danger', 已追回: 'success', 已核销: 'info' }[s] || 'info'
}

onMounted(load)
</script>
