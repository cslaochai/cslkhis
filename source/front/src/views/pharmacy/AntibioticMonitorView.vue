<template>
  <div data-testid="antibiotic-monitor-view">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="抗菌药物使用监测（评审与专项整治硬指标）"
      description="使用强度 AUD = 抗菌药物累计 DDD 数 × 100 ÷ 同期收治患者人天数（只算住院，分子分母同源）。微生物送检率按「同一次住院期间是否有微生物标本送检」判定，未做「送检早于首剂」的严格时序比对 —— 这是简化口径，不要拿它报严格口径的表。" />

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
            @change="loadPreview" />
        </div>
        <el-button data-testid="btn-preview" @click="loadPreview">试算</el-button>
      </div>
      <div class="stat-items" v-if="preview.statMonth">
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-op-rate">{{ preview.opUsageRate ?? '-' }}%</div>
          <div class="stat-label">门诊抗菌药物使用率（目标 ≤{{ preview.opUsageRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.opAbxRxCount }} / {{ preview.opRxCount }} 张处方</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-ip-rate">{{ preview.ipUsageRate ?? '-' }}%</div>
          <div class="stat-label">住院抗菌药物使用率（目标 ≤{{ preview.ipUsageRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.ipAbxPatientCount }} / {{ preview.ipDischargeCount }} 名出院患者</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-aud">{{ preview.aud ?? '-' }}</div>
          <div class="stat-label">使用强度 AUD（目标 ≤{{ preview.audTarget }}）</div>
          <div class="stat-sub">DDDs {{ preview.ddds }} / 人天数 {{ preview.patientDays }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-micro-rate">{{ preview.microSubmitRate ?? '-' }}%</div>
          <div class="stat-label">微生物标本送检率（目标 ≥{{ preview.microSubmitRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.microSubmitCount }} / {{ preview.abxTreatCount }} 名用药患者</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-danger': preview.unmatchedOrderCount > 0 }" data-testid="pv-unmatched">
            {{ preview.unmatchedOrderCount ?? '-' }}
          </div>
          <div class="stat-label">未匹配医嘱数（>0 请去维护别名）</div>
          <div class="stat-sub">这部分消耗没计入使用强度</div>
        </div>
      </div>
      <div v-else class="muted">选择月份后点「试算」</div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="统计月份">
            <div data-testid="stats-month">
              <el-date-picker
                v-model="query.statMonth"
                type="month"
                placeholder="统计月份"
                value-format="YYYY-MM"
                clearable
                style="width: 140px" />
            </div>
          </el-form-item>
          <el-form-item label="统计范围">
            <div data-testid="stats-scope">
              <el-select v-model="query.scopeType" placeholder="统计范围" clearable style="width: 130px">
                <el-option label="全院" :value="1" />
                <el-option label="科室" :value="2" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-stats-query" @click="loadStats">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button
            v-perm="'pharmacy:antibiotic:statGenerate'"
            type="primary"
            data-testid="btn-generate"
            @click="openGenerateDialog">生成月度快照</el-button>
          <el-button
            v-perm="'pharmacy:antibiotic:statExport'"
            data-testid="btn-export"
            @click="doExport">导出 CSV</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="statsRows" stripe v-loading="statsLoading" :max-height="tableMaxHeight" data-testid="stats-table">
      <el-table-column prop="statMonth" label="统计月份" width="100" />
      <el-table-column label="范围" width="80">
        <template #default="{ row }">{{ row.scopeTypeText }}</template>
      </el-table-column>
      <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
      <el-table-column label="门诊使用率" width="120" align="right">
        <template #default="{ row }">
          <span :class="{ 'over-target': Number(row.opUsageRate) > Number(row.opUsageRateTarget) }">
            {{ row.opUsageRate }}%
          </span>
          <span class="muted">（{{ row.opAbxRxCount }}/{{ row.opRxCount }}）</span>
        </template>
      </el-table-column>
      <el-table-column label="住院使用率" width="120" align="right">
        <template #default="{ row }">
          <span :class="{ 'over-target': Number(row.ipUsageRate) > Number(row.ipUsageRateTarget) }">
            {{ row.ipUsageRate }}%
          </span>
          <span class="muted">（{{ row.ipAbxPatientCount }}/{{ row.ipDischargeCount }}）</span>
        </template>
      </el-table-column>
      <el-table-column label="使用强度 AUD" width="150" align="right">
        <template #default="{ row }">
          <span :class="{ 'over-target': Number(row.aud) > Number(row.audTarget) }" data-testid="cell-aud">{{ row.aud }}</span>
          <span class="muted">（DDDs {{ row.ddds }} / {{ row.patientDays }} 人天）</span>
        </template>
      </el-table-column>
      <el-table-column label="微生物送检率" width="140" align="right">
        <template #default="{ row }">
          <span :class="{ 'under-target': Number(row.microSubmitRate) < Number(row.microSubmitRateTarget) }">
            {{ row.microSubmitRate }}%
          </span>
          <span class="muted">（{{ row.microSubmitCount }}/{{ row.abxTreatCount }}）</span>
        </template>
      </el-table-column>
      <el-table-column prop="unmatchedOrderCount" label="未匹配医嘱" width="100" align="right" />
      <el-table-column prop="generateBy" label="生成人" width="100" />
      <el-table-column prop="generateTime" label="生成时间" width="170" />
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <!-- ⚠ 见分级目录页同款注释：layout 含 sizes 时 page-size 必须 v-model + @size-change，
             否则 EP 2.14 把整个分页组件渲染成 null（只有一句控制台警告，页面是"分页条消失"） -->
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :total="statsTotal"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          @current-change="loadStats"
          @size-change="onStatsSizeChange" />
      </div>
    </el-card>

    <!-- 生成快照 -->
    <el-dialog v-model="genDialogVisible" title="生成月度监测快照" width="460px">
      <el-form label-width="110px">
        <el-form-item label="统计月份" required>
          <div data-testid="gen-month" style="width: 100%">
            <el-date-picker
              v-model="genMonth"
              type="month"
              placeholder="选择月份"
              value-format="YYYY-MM"
              style="width: 100%" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import {
  exportAntibioticStatsCsv,
  generateAntibioticStats,
  listAntibioticStatsPage,
  previewAntibioticStats
} from '@/api/antibiotic'

const saving = ref(false)
const previewMonth = ref(new Date().toISOString().slice(0, 7))
const preview = ref({})

async function loadPreview() {
  try {
    const res = await previewAntibioticStats(previewMonth.value)
    preview.value = res?.data || {}
  } catch (e) {
    ElMessage.error(e?.message || '试算失败')
  }
}

const query = reactive({ statMonth: null, scopeType: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const statsRows = ref([])
const statsTotal = ref(0)
const statsLoading = ref(false)

async function loadStats() {
  statsLoading.value = true
  try {
    const res = await listAntibioticStatsPage({ ...query })
    statsRows.value = res?.data?.records || []
    statsTotal.value = Number(res?.data?.total || 0)
  } finally {
    statsLoading.value = false
  }
}

function onStatsSizeChange() {
  query.pageNum = 1
  loadStats()
}

const genDialogVisible = ref(false)
const genMonth = ref(new Date().toISOString().slice(0, 7))
const genScope = ref(1)

function openGenerateDialog() {
  genDialogVisible.value = true
}

async function doGenerate() {
  if (!genMonth.value) { ElMessage.warning('请选择统计月份'); return }
  saving.value = true
  try {
    const res = await generateAntibioticStats({ statMonth: genMonth.value, scopeType: genScope.value })
    const n = (res?.data || []).length
    ElMessage.success(`已生成 ${n} 条快照`)
    genDialogVisible.value = false
    query.statMonth = genMonth.value
    loadStats()
  } catch (e) {
    ElMessage.error(e?.message || '生成失败')
  } finally {
    saving.value = false
  }
}

async function doExport() {
  try {
    const res = await exportAntibioticStatsCsv({ statMonth: query.statMonth, scopeType: query.scopeType })
    const text = res?.data || ''
    if (!text) { ElMessage.warning('没有可导出的数据'); return }
    const blob = new Blob([text], { type: 'text/csv;charset=utf-8' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `抗菌药物使用监测_${query.statMonth || '全部'}.csv`
    a.click()
    URL.revokeObjectURL(a.href)
    ElMessage.success('已导出')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

onMounted(() => {
  loadStats()
  loadPreview()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 12px;
}
.stat-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.stat-title {
  font-weight: 600;
  margin-right: auto;
}
.stat-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.stat-item {
  flex: 1;
  min-width: 170px;
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
.muted {
  color: #909399;
  font-size: 12px;
}
.over-target {
  color: #f56c6c;
  font-weight: 600;
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
