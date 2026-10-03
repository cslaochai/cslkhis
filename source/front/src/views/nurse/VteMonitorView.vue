<template>
  <div data-testid="vte-monitor-view">
    <!-- 实时试算 -->
    <el-card shadow="never" class="stat-card">
      <div class="stat-head">
        <span class="stat-title">实时试算（不落库）</span>
        <div data-testid="preview-month">
          <el-date-picker
              v-model="previewMonth"
              type="month"
              placeholder="选择月份"
              value-format="YYYY-MM"
              :clearable="false"
              style="width: 140px"
              @change="loadPreview"/>
        </div>
        <el-button data-testid="btn-preview" @click="loadPreview">试算</el-button>
      </div>
      <div class="stat-items" v-if="preview.statMonth">
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-assess-rate">{{ preview.assessRate ?? '-' }}%</div>
          <div class="stat-label">VTE 风险评估率（目标 ≥{{ preview.assessRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.assessedCount }} / {{ preview.dischargeCount }} 名出院患者</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-high-risk-rate">{{ preview.highRiskRate ?? '-' }}%</div>
          <div class="stat-label">中高危占比（占已评估）</div>
          <div class="stat-sub">{{ preview.highRiskCount }} / {{ preview.assessedCount }} 人</div>
        </div>
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'stat-value-danger': Number(preview.preventRate) < Number(preview.preventRateTarget) }"
               data-testid="pv-prevent-rate">
            {{ preview.preventRate ?? '-' }}%
          </div>
          <div class="stat-label">预防措施落实率（目标 ≥{{ preview.preventRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.preventDoneCount }} / {{ preview.highRiskCount }} 名中高危已落实</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-vte-rate">{{ preview.vteIncidenceRate ?? '-' }}%</div>
          <div class="stat-label">院内 VTE 发生率</div>
          <div class="stat-sub">{{ preview.vteEventCount }} / {{ preview.dischargeCount }} · 出血 {{
              preview.bleedCount
            }} 例
          </div>
        </div>
      </div>
      <div v-else class="muted">选择月份后点「试算」</div>
    </el-card>

    <!-- 快照列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="statsQuery" inline @submit.prevent>
          <el-form-item label="统计月份">
            <div data-testid="stats-month">
              <el-date-picker v-model="statsQuery.statMonth" type="month" placeholder="统计月份" value-format="YYYY-MM"
                              clearable style="width: 140px"/>
            </div>
          </el-form-item>
          <el-form-item label="统计范围">
            <div data-testid="stats-scope">
              <el-select v-model="statsQuery.scopeType" placeholder="统计范围" clearable style="width: 130px">
                <el-option label="全院" :value="1"/>
                <el-option label="科室" :value="2"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-stats-query" @click="loadStats">查询</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：生成/导出统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'nursing:vte:statGenerate'" type="primary" data-testid="btn-generate"
                     @click="openGenerateDialog">生成月度快照
          </el-button>
          <el-button v-perm="'nursing:vte:statExport'" data-testid="btn-export" @click="doExport">导出 CSV</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="statsRows" stripe :max-height="tableMaxHeight" v-loading="statsLoading"
                data-testid="stats-table">
        <el-table-column prop="statMonth" label="统计月份" width="100"/>
        <el-table-column label="范围" width="80">
          <template #default="{ row }">{{ row.scopeTypeText }}</template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip/>
        <el-table-column label="评估率" width="140" align="right">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.assessRate) < Number(row.assessRateTarget) }">{{
                row.assessRate
              }}%</span>
            <span class="muted">（{{ row.assessedCount }}/{{ row.dischargeCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="中高危" width="130" align="right">
          <template #default="{ row }">
            {{ row.highRiskRate }}%
            <span class="muted">（{{ row.highRiskCount }} 人）</span>
          </template>
        </el-table-column>
        <el-table-column label="措施落实率" width="150" align="right">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.preventRate) < Number(row.preventRateTarget) }"
                  data-testid="cell-prevent-rate">{{ row.preventRate }}%</span>
            <span class="muted">（{{ row.preventDoneCount }}/{{ row.highRiskCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="院内 VTE 发生率" width="150" align="right">
          <template #default="{ row }">
            <span data-testid="cell-vte-rate">{{ row.vteIncidenceRate }}%</span>
            <span class="muted">（{{ row.vteEventCount }}/{{ row.dischargeCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="bleedCount" label="出血例数" width="90" align="right"/>
        <el-table-column prop="generateBy" label="生成人" width="100"/>
        <el-table-column prop="generateTime" label="生成时间" width="170"/>
      </el-table>
      <!-- ⚠ layout 含 sizes 时 page-size 必须 v-model + @size-change，否则 EP 2.14 把整条分页渲染成 null -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            layout="total, sizes, prev, pager, next"
            :total="statsTotal"
            :page-sizes="PAGE_SIZES"
            v-model:current-page="statsQuery.pageNum"
            v-model:page-size="statsQuery.pageSize"
            @current-change="loadStats"
            @size-change="onStatsSizeChange"/>
      </div>
    </el-card>

    <!-- VTE 事件登记 -->
    <el-card shadow="never" class="event-card">
      <div class="stat-head">
        <span class="stat-title">VTE 事件登记（发生率的分子）</span>
        <div class="toolbar-right">
          <el-button v-perm="'nursing:vte:eventEdit'" type="primary" data-testid="btn-add-event" @click="openEventForm">
            登记事件
          </el-button>
        </div>
      </div>
      <el-table :data="eventRows" border stripe v-loading="eventLoading" data-testid="event-table">
        <el-table-column prop="eventNo" label="事件编号" width="150"/>
        <el-table-column prop="patientName" label="患者" width="110"/>
        <el-table-column label="事件类型" width="150">
          <template #default="{ row }">{{ row.eventTypeText }}</template>
        </el-table-column>
        <el-table-column label="发生时机" width="120">
          <template #default="{ row }">
            <el-tag :type="row.counted ? 'danger' : 'info'" disable-transitions>{{ row.onsetTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diagnoseDate" label="确诊日期" width="110"/>
        <el-table-column label="诊断依据" width="130">
          <template #default="{ row }">{{ row.diagnosisBasisText || '-' }}</template>
        </el-table-column>
        <el-table-column prop="thrombusSite" label="部位" min-width="140" show-overflow-tooltip/>
        <el-table-column label="转归" width="80">
          <template #default="{ row }">{{ row.outcomeText || '-' }}</template>
        </el-table-column>
        <el-table-column label="计入发生率" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.counted ? 'danger' : 'info'" disable-transitions>{{
                row.counted ? '计入' : '不计入'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="登记人" width="100"/>
      </el-table>
      <el-pagination
          class="pager"
          layout="total, sizes, prev, pager, next"
          :total="eventTotal"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="eventQuery.pageNum"
          v-model:page-size="eventQuery.pageSize"
          @current-change="loadEvents"
          @size-change="onEventSizeChange"/>
    </el-card>

    <!-- 生成快照 -->
    <el-dialog v-model="genDialogVisible" title="生成月度防控指标快照" width="460px">
      <el-form label-width="110px">
        <el-form-item label="统计月份" required>
          <div data-testid="gen-month" style="width: 100%">
            <el-date-picker v-model="genMonth" type="month" placeholder="选择月份" value-format="YYYY-MM"
                            style="width: 100%"/>
          </div>
        </el-form-item>
        <el-form-item label="统计范围" required>
          <el-radio-group v-model="genScope">
            <el-radio :value="1">全院一条</el-radio>
            <el-radio :value="2">按科室生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="form-tip">同月同范围重复生成会覆盖上一次结果 —— 指标是对外报数，只留最新一次复算。</div>
      </el-form>
      <template #footer>
        <el-button @click="genDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-do-generate" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 事件登记 -->
    <el-dialog v-model="eventVisible" title="登记 VTE 事件" width="520px" data-testid="event-dialog">
      <el-form label-width="110px">
        <el-form-item label="住院患者" required>
          <div data-testid="sel-admission" style="width: 100%">
            <el-select v-model="eventForm.admissionId" filterable placeholder="选择住院患者" style="width: 100%">
              <el-option
                  v-for="p in inpatients"
                  :key="p.admissionId"
                  :label="`${p.patientName || '未命名'} · ${p.admissionNo || ''}`"
                  :value="p.admissionId"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="事件类型" required>
          <div data-testid="sel-event-type" style="width: 100%">
            <el-select v-model="eventForm.eventType" placeholder="选择事件类型" style="width: 100%">
              <el-option label="深静脉血栓（DVT）" :value="1"/>
              <el-option label="肺栓塞（PE）" :value="2"/>
              <el-option label="预防相关出血" :value="3"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="发生时机" required>
          <div data-testid="sel-onset-type" style="width: 100%">
            <el-radio-group v-model="eventForm.onsetType">
              <el-radio :value="1">院内发生（计入发生率）</el-radio>
              <el-radio :value="2">入院时已存在（不计入）</el-radio>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item label="确诊日期" required>
          <div data-testid="sel-diagnose-date" style="width: 100%">
            <el-date-picker v-model="eventForm.diagnoseDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="确诊日期" style="width: 100%"/>
          </div>
        </el-form-item>
        <el-form-item label="诊断依据">
          <el-select v-model="eventForm.diagnosisBasis" clearable placeholder="选择诊断依据" style="width: 100%">
            <el-option label="超声" :value="1"/>
            <el-option label="CT 肺动脉造影" :value="2"/>
            <el-option label="静脉造影" :value="3"/>
            <el-option label="临床诊断" :value="4"/>
            <el-option label="其他" :value="5"/>
          </el-select>
        </el-form-item>
        <el-form-item label="血栓部位">
          <el-input v-model="eventForm.thrombusSite" maxlength="100" placeholder="如：左下肢股静脉"/>
        </el-form-item>
        <el-form-item label="转归">
          <el-select v-model="eventForm.outcome" clearable placeholder="选择转归" style="width: 100%">
            <el-option label="好转" :value="1"/>
            <el-option label="未愈" :value="2"/>
            <el-option label="死亡" :value="3"/>
            <el-option label="未知" :value="4"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="eventForm.remark" type="textarea" :rows="2" maxlength="500"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eventVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingEvent" data-testid="btn-save-event" @click="submitEvent">保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {getInpatientListPage} from '@/api/inpatient'
import {
  generateVteStats,
  exportVteStatsCsv,
  listVteEventPage,
  listVteStatsPage,
  previewVteStats,
  upsertVteEvent
} from '@/api/vte'

const today = () => {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  return `${d.getFullYear()}-${m}`
}

const previewMonth = ref(today())
const preview = ref({})

async function loadPreview() {
  try {
    const res = await previewVteStats(previewMonth.value)
    preview.value = res?.data || {}
  } catch (e) {
    ElMessage.error(e?.message || '试算失败')
  }
}

const statsQuery = reactive({statMonth: null, scopeType: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const statsRows = ref([])
const statsTotal = ref(0)
const statsLoading = ref(false)

async function loadStats() {
  statsLoading.value = true
  try {
    const res = await listVteStatsPage({...statsQuery})
    statsRows.value = res?.data?.records || []
    statsTotal.value = Number(res?.data?.total || 0)
  } finally {
    statsLoading.value = false
  }
}

function onStatsSizeChange() {
  statsQuery.pageNum = 1
  loadStats()
}

const genDialogVisible = ref(false)
const genMonth = ref(today())
const genScope = ref(1)
const saving = ref(false)

function openGenerateDialog() {
  genDialogVisible.value = true
}

async function doGenerate() {
  if (!genMonth.value) {
    ElMessage.warning('请选择统计月份');
    return
  }
  saving.value = true
  try {
    const res = await generateVteStats({statMonth: genMonth.value, scopeType: genScope.value})
    ElMessage.success(`已生成 ${(res?.data || []).length} 条快照`)
    genDialogVisible.value = false
    statsQuery.statMonth = genMonth.value
    loadStats()
    loadPreview()
  } catch (e) {
    ElMessage.error(e?.message || '生成失败')
  } finally {
    saving.value = false
  }
}

async function doExport() {
  try {
    const res = await exportVteStatsCsv({statMonth: statsQuery.statMonth, scopeType: statsQuery.scopeType})
    const text = res?.data || ''
    if (!text) {
      ElMessage.warning('没有可导出的数据');
      return
    }
    const blob = new Blob([text], {type: 'text/csv;charset=utf-8'})
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `VTE防控指标_${statsQuery.statMonth || '全部'}.csv`
    a.click()
    URL.revokeObjectURL(a.href)
    ElMessage.success('已导出')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

// ========== VTE 事件 ==========
const eventQuery = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})
const eventRows = ref([])
const eventTotal = ref(0)
const eventLoading = ref(false)
const inpatients = ref([])

async function loadEvents() {
  eventLoading.value = true
  try {
    const res = await listVteEventPage({...eventQuery})
    eventRows.value = res?.data?.records || []
    eventTotal.value = Number(res?.data?.total || 0)
  } finally {
    eventLoading.value = false
  }
}

function onEventSizeChange() {
  eventQuery.pageNum = 1
  loadEvents()
}

const eventVisible = ref(false)
const savingEvent = ref(false)
const eventForm = reactive({
  admissionId: null,
  eventType: 1,
  onsetType: 1,
  diagnoseDate: null,
  diagnosisBasis: null,
  thrombusSite: '',
  outcome: null,
  remark: '',
})

async function openEventForm() {
  eventForm.admissionId = null
  eventForm.eventType = 1
  eventForm.onsetType = 1
  eventForm.diagnoseDate = new Date().toISOString().slice(0, 10)
  eventForm.diagnosisBasis = null
  eventForm.thrombusSite = ''
  eventForm.outcome = null
  eventForm.remark = ''
  if (!inpatients.value.length) {
    try {
      const res = await getInpatientListPage({admitStatus: null, pageNum: 1, pageSize: 100})
      inpatients.value = res?.data?.records || res?.data?.list || []
    } catch (e) {
      inpatients.value = []
    }
  }
  eventVisible.value = true
}

async function submitEvent() {
  if (!eventForm.admissionId) {
    ElMessage.warning('请选择住院患者');
    return
  }
  if (!eventForm.diagnoseDate) {
    ElMessage.warning('请选择确诊日期');
    return
  }
  savingEvent.value = true
  try {
    await upsertVteEvent({...eventForm})
    ElMessage.success('事件已登记')
    eventVisible.value = false
    loadEvents()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    savingEvent.value = false
  }
}

onMounted(() => {
  loadStats()
  loadEvents()
  loadPreview()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 12px;
}

.event-card {
  margin-top: 12px;
}

.stat-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.stat-title {
  font-weight: 600;
}

.stat-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 190px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
}

.stat-value-danger {
  color: #f56c6c;
}

.stat-label {
  color: #606266;
  font-size: 12px;
  margin-top: 4px;
}

.stat-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.toolbar-right {
  margin-left: auto;
}

.pager {
  margin-top: 10px;
  justify-content: flex-end;
}

.muted {
  color: #909399;
  font-size: 12px;
}

.under-target {
  color: #e6a23c;
  font-weight: 600;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
