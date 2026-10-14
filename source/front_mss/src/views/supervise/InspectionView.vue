<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item"><el-input v-model="q.keyword" placeholder="任务单号/名称/机构" clearable /></div>
          <div class="q-item">
            <el-select v-model="q.inspectType" placeholder="检查类型" clearable style="width:100%">
              <el-option label="专项检查" value="专项检查" />
              <el-option label="日常巡查" value="日常巡查" />
              <el-option label="举报核查" value="举报核查" />
            </el-select>
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="任务状态" clearable style="width:100%">
              <el-option label="草稿" value="草稿" />
              <el-option label="待开展" value="待开展" />
              <el-option label="检查中" value="检查中" />
              <el-option label="已完成" value="已完成" />
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
        <div class="font-semibold text-[15px] text-[#0B3B66]">飞检任务 <el-tag size="small" type="warning">检查中 {{ runningCount }}</el-tag></div>
        <el-button type="primary" size="small" @click="openEdit()">新增任务</el-button>
      </div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="inspectionNo" label="任务单号" width="170" />
        <el-table-column prop="title" label="任务名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="inspectType" label="检查类型" width="100" align="center" />
        <el-table-column prop="targetOrg" label="被查机构" min-width="140" show-overflow-tooltip />
        <el-table-column label="计划时间" width="180">
          <template #default="{ row }">{{ row.planStart }} ~ {{ row.planEnd }}</template>
        </el-table-column>
        <el-table-column prop="leader" label="带队人" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === '草稿'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === '草稿' || row.status === '待开展'" link type="success" @click="onStart(row)">开始检查</el-button>
            <el-button v-if="row.status === '检查中'" link type="warning" @click="openFinish(row)">办结</el-button>
            <el-button v-if="row.status === '检查中' || row.status === '已完成'" link type="danger" @click="onToRecover(row)">生成追回</el-button>
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

    <InspectionEdit v-model="editVisible" :row="currentRow" @saved="load" />

    <!-- 办结弹窗 -->
    <el-dialog v-model="finishVisible" title="办结任务" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="检查结果小结">
          <el-input v-model="finishForm.resultSummary" type="textarea" :rows="4" placeholder="填写检查结论、发现问题与处置意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onFinish">确认办结</el-button>
      </template>
    </el-dialog>

    <InspectionDetail v-model="detailVisible" :id="currentId" @saved="load" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listInspections, startInspection, finishInspection, inspectionToRecover } from '@/api/supervise'
import { PAGE_SIZES } from '@/lib/pagination'
import InspectionEdit from './InspectionEdit.vue'
import InspectionDetail from './InspectionDetail.vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const q = reactive({ keyword: '', inspectType: '', status: '', page: 1, size: 10 })

const runningCount = computed(() => list.value.filter((x) => x.status === '检查中').length)

const editVisible = ref(false)
const currentRow = ref(null)
function openEdit(row) {
  currentRow.value = row || null
  editVisible.value = true
}

const detailVisible = ref(false)
const currentId = ref(null)
function openDetail(row) {
  currentId.value = row.id
  detailVisible.value = true
}

const finishVisible = ref(false)
const finishForm = reactive({ id: null, resultSummary: '' })
function openFinish(row) {
  finishForm.id = row.id
  finishForm.resultSummary = ''
  finishVisible.value = true
}
async function onFinish() {
  saving.value = true
  try {
    await finishInspection(finishForm)
    ElMessage.success('任务已办结')
    finishVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onStart(row) {
  await ElMessageBox.confirm(`确定开始检查「${row.title}」？`, '提示', { type: 'warning' })
  await startInspection(row.id)
  ElMessage.success('已开始检查')
  load()
}

async function onToRecover(row) {
  await ElMessageBox.confirm('按处置方式为「追回」的问题明细合计金额生成追回单？', '提示', { type: 'warning' })
  const res = await inspectionToRecover(row.id)
  ElMessage.success(res.data?.ok ? '已生成追回单' : (res.data?.error || '操作失败'))
  load()
}

async function load() {
  loading.value = true
  const res = await listInspections(q)
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() { q.page = 1; load() }
function reset() { q.keyword = q.inspectType = q.status = ''; q.page = 1; load() }

function statusType(s) {
  return { 草稿: 'info', 待开展: '', 检查中: 'warning', 已完成: 'success' }[s] || 'info'
}

onMounted(load)
</script>
