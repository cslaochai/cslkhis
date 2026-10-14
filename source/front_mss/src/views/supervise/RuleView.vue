<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.keyword" placeholder="规则名称/编码" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.ruleType" placeholder="规则类型" clearable style="width:100%">
              <el-option v-for="(v, k) in RULE_TYPES" :key="k" :label="v" :value="k" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="状态" clearable style="width:100%">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
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
        <div class="font-semibold text-[15px] text-[#0B3B66]">监管规则 <el-tag size="small" type="success">{{ enabled }} 启用</el-tag></div>
        <el-button type="primary" size="small" @click="openEdit()">新增规则</el-button>
      </div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="ruleCode" label="规则编码" width="150" />
        <el-table-column prop="ruleName" label="规则名称" width="130" />
        <el-table-column label="规则类型" width="130">
          <template #default="{ row }">{{ ruleTypeText(row.ruleType) }}</template>
        </el-table-column>
        <el-table-column label="稽核对象" width="110" align="center">
          <template #default="{ row }">{{ row.target === 2 ? '机构报盘' : '经办结算单' }}</template>
        </el-table-column>
        <el-table-column label="违规级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="severityType(row.severity)">{{ severityText(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="规则说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
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

    <RuleEdit v-model="editVisible" :row="currentRow" @saved="load" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listRules, changeRuleStatus } from '@/api/supervise'
import { PAGE_SIZES } from '@/lib/pagination'
import RuleEdit from './RuleEdit.vue'
import { RULE_TYPES, ruleTypeText, severityText, severityType } from './ruleMeta'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', ruleType: '', status: '', page: 1, size: 10 })

const enabled = computed(() => list.value.filter((x) => x.status === 1).length)

const editVisible = ref(false)
const currentRow = ref(null)
function openEdit(row) {
  currentRow.value = row || null
  editVisible.value = true
}

async function load() {
  loading.value = true
  const res = await listRules(q)
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() { q.page = 1; load() }
function reset() { q.keyword = q.ruleType = ''; q.status = ''; q.page = 1; load() }

async function toggle(row) {
  const next = row.status === 1 ? 0 : 1
  await changeRuleStatus(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已停用')
  load()
}

onMounted(load)
</script>
