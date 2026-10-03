<template>
  <div data-testid="nutrition-stats-view">
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
      <div v-if="preview.statMonth" class="stat-items">
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'under-target': Number(preview.screenRate) < Number(preview.screenRateTarget) }"
               data-testid="pv-screen-rate">
            {{ preview.screenRate ?? '-' }}%
          </div>
          <div class="stat-label">营养风险筛查率（目标 ≥{{ preview.screenRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.screenedCount }} / {{ preview.dischargeCount }} 名出院患者</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="pv-risk-rate">{{ preview.riskRate ?? '-' }}%</div>
          <div class="stat-label">营养风险检出率（占已筛查）</div>
          <div class="stat-sub">{{ preview.riskCount }} / {{ preview.screenedCount }} 人</div>
        </div>
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'under-target': Number(preview.dietConfirmRate) < Number(preview.dietConfirmRateTarget) }"
               data-testid="pv-diet-rate">
            {{ preview.dietConfirmRate ?? '-' }}%
          </div>
          <div class="stat-label">膳食医嘱执行率（目标 ≥{{ preview.dietConfirmRateTarget }}%）</div>
          <div class="stat-sub">已接收 {{ preview.dietConfirmCount }} / 方案 {{ preview.dietPlanCount }} 条</div>
        </div>
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'under-target': Number(preview.consultOnTimeRate) < Number(preview.consultOnTimeRateTarget) }"
               data-testid="pv-consult-rate">
            {{ preview.consultOnTimeRate ?? '-' }}%
          </div>
          <div class="stat-label">营养会诊按时应答率（目标 ≥{{ preview.consultOnTimeRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.consultOnTimeCount }} / {{ preview.consultCount }} 例</div>
        </div>
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'under-target': Number(preview.mealSignRate) < Number(preview.mealSignRateTarget) }"
               data-testid="pv-meal-rate">
            {{ preview.mealSignRate ?? '-' }}%
          </div>
          <div class="stat-label">订餐签收率（目标 ≥{{ preview.mealSignRateTarget }}%）</div>
          <div class="stat-sub">{{ preview.mealSignedCount }} / {{ preview.mealOrderCount }} 条 · 退订
            {{ preview.mealCancelCount }} 条
          </div>
        </div>
      </div>
      <div v-else class="muted">选择月份后点「试算」</div>
    </el-card>

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
            <el-button type="primary" data-testid="btn-stats-query" @click="onStatsQuery">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:nutrition:statGenerate'" type="primary" data-testid="btn-generate"
                     @click="openGenerateDialog">生成月度快照
          </el-button>
          <el-button v-perm="'ipd:nutrition:statExport'" data-testid="btn-export" @click="doExport">导出 CSV</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="statsRows" stripe v-loading="statsLoading" :max-height="tableMaxHeight"
                data-testid="stats-table">
        <el-table-column prop="statMonth" label="统计月份" width="95"/>
        <el-table-column label="范围" width="75">
          <template #default="{ row }">{{ row.scopeTypeText }}</template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="120" show-overflow-tooltip/>
        <el-table-column label="出院人数" width="90" align="right">
          <template #default="{ row }">{{ row.dischargeCount }}</template>
        </el-table-column>
        <el-table-column label="筛查率" width="135" align="right">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.screenRate) < Number(row.screenRateTarget) }">{{
                row.screenRate
              }}%</span>
            <span class="muted">（{{ row.screenedCount }}/{{ row.dischargeCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="检出风险" width="120" align="right">
          <template #default="{ row }">
            {{ row.riskRate }}%
            <span class="muted">（{{ row.riskCount }} 人）</span>
          </template>
        </el-table-column>
        <el-table-column label="膳食医嘱执行率" width="155" align="right">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.dietConfirmRate) < Number(row.dietConfirmRateTarget) }"
                  data-testid="cell-diet-rate">{{ row.dietConfirmRate }}%</span>
            <span class="muted">（{{ row.dietConfirmCount }}/{{ row.dietPlanCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="会诊按时率" width="135" align="right">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.consultOnTimeRate) < Number(row.consultOnTimeRateTarget) }">{{
                row.consultOnTimeRate
              }}%</span>
            <span class="muted">（{{ row.consultOnTimeCount }}/{{ row.consultCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="订餐签收率" width="150" align="right">
          <template #default="{ row }">
            <span :class="{ 'under-target': Number(row.mealSignRate) < Number(row.mealSignRateTarget) }"
                  data-testid="cell-meal-rate">{{ row.mealSignRate }}%</span>
            <span class="muted">（{{ row.mealSignedCount }}/{{ row.mealOrderCount }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="mealCancelCount" label="退订" width="70" align="right"/>
        <el-table-column prop="generateBy" label="生成人" width="95"/>
        <el-table-column prop="generateTime" label="生成时间" width="160"/>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <!-- ⚠ layout 含 sizes 时 page-size 必须 v-model + @size-change，否则 EP 2.14 把整条分页渲染成 null -->
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

    <!-- 生成快照 -->
    <el-dialog v-model="genDialogVisible" title="生成营养膳食月度快照" width="470px" data-testid="gen-stats-dialog">
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
        <div class="form-tip">
          按科室生成时，出院人数与膳食方案数都取该科室的口径（病区归属按入院科室）。同月同范围重复生成覆盖上一次结果。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="genDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-do-generate" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  exportNutritionStatsCsv,
  generateNutritionStats,
  listNutritionStatsPage,
  previewNutritionStats,
} from '@/api/nutrition'

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const previewMonth = ref(today())
const preview = ref({})

async function loadPreview() {
  try {
    const res = await previewNutritionStats(previewMonth.value)
    preview.value = res?.data || {}
  } catch (e) {
    ElMessage.error(e?.message || '试算失败')
  }
}

const statsQuery = reactive({statMonth: null, scopeType: null, deptId: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const statsRows = ref([])
const statsTotal = ref(0)
const statsLoading = ref(false)

async function loadStats() {
  statsLoading.value = true
  try {
    const res = await listNutritionStatsPage({...statsQuery})
    statsRows.value = res?.data?.records || []
    statsTotal.value = Number(res?.data?.total || 0)
  } finally {
    statsLoading.value = false
  }
}

function onStatsQuery() {
  statsQuery.pageNum = 1
  loadStats()
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
    const res = await generateNutritionStats({statMonth: genMonth.value, scopeType: genScope.value})
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
    const res = await exportNutritionStatsCsv({
      statMonth: statsQuery.statMonth,
      scopeType: statsQuery.scopeType,
      deptId: statsQuery.deptId,
    })
    const text = res?.data || ''
    if (!text) {
      ElMessage.warning('没有可导出的数据');
      return
    }
    const blob = new Blob([text], {type: 'text/csv;charset=utf-8'})
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `营养膳食指标_${statsQuery.statMonth || '全部'}.csv`
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
}

.stat-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 185px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

.stat-value {
  font-size: 21px;
  font-weight: 600;
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
