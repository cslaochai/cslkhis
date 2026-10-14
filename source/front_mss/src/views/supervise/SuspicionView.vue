<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.keyword" placeholder="疑点单号/患者/单据号" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.ruleType" placeholder="规则类型" clearable style="width:100%">
              <el-option v-for="(v, k) in RULE_TYPES" :key="k" :label="v" :value="k" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="处置状态" clearable style="width:100%">
              <el-option label="待核实" value="待核实" />
              <el-option label="已确认" value="已确认" />
              <el-option label="已排除" value="已排除" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.bizSource" placeholder="疑点来源" clearable style="width:100%">
              <el-option label="经办结算单" :value="1" />
              <el-option label="机构报盘" :value="2" />
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
        <div class="font-semibold text-[15px] text-[#0B3B66]">疑点稽核 <el-tag size="small" type="warning">本页待核实 {{ pending }}</el-tag></div>
        <el-button type="primary" size="small" :loading="running" @click="onRunAudit">
          <el-icon class="mr-1"><VideoPlay /></el-icon>一键稽核
        </el-button>
      </div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="suspicionNo" label="疑点单号" width="170" />
        <el-table-column prop="ruleName" label="命中规则" width="120" />
        <el-table-column prop="patientName" label="患者" width="90" />
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">{{ row.bizSource === 2 ? '机构报盘' : '经办结算单' }}</template>
        </el-table-column>
        <el-table-column prop="bizNo" label="来源单据号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="encounterType" label="就诊类型" width="80" align="center" />
        <el-table-column label="涉及金额" align="right" width="110">
          <template #default="{ row }">{{ fmt(row.involvedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="疑点说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

    <SuspicionDetail v-model="detailVisible" :id="currentId" @saved="load" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listSuspicions, runAudit } from '@/api/supervise'
import { PAGE_SIZES } from '@/lib/pagination'
import SuspicionDetail from './SuspicionDetail.vue'
import { RULE_TYPES } from './ruleMeta'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const running = ref(false)
const q = reactive({ keyword: '', ruleType: '', status: '', bizSource: '', page: 1, size: 10 })

const pending = computed(() => list.value.filter((x) => x.status === '待核实').length)

const detailVisible = ref(false)
const currentId = ref(null)
function openDetail(row) {
  currentId.value = row.id
  detailVisible.value = true
}

async function onRunAudit() {
  running.value = true
  try {
    const res = await runAudit()
    ElMessage.success(res.data?.matched ? `稽核完成，新增疑点 ${res.data.matched} 条` : '稽核完成，无新增疑点')
    load()
  } finally {
    running.value = false
  }
}

async function load() {
  loading.value = true
  const params = { ...q }
  if (!params.bizSource) params.bizSource = ''
  const res = await listSuspicions(params)
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() { q.page = 1; load() }
function reset() { q.keyword = q.ruleType = q.status = ''; q.bizSource = ''; q.page = 1; load() }

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 待核实: 'warning', 已确认: 'danger', 已排除: 'info' }[s] || 'info'
}

onMounted(load)
</script>
