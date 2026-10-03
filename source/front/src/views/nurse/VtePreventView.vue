<template>
  <div class="vte-prevent-page" data-testid="vte-prevent-view">
    <!-- sql/191：332「VTE 风险防控」与 333「院内 VTE 监测」合并为「VTE 防控与监测」（同 VteController，是防控闭环的两段），333 置 is_visible=0 退出侧栏但保留 nursing:vte:monitor 权限码。 -->
    <el-tabs v-model="mergedTab" class="merged-tabs">
      <el-tab-pane label="中高危名单与预防措施" name="prevent">
        <!-- 看板 -->
        <el-card shadow="never" class="stat-card">
          <div class="stat-items">
            <div class="stat-item">
              <div class="stat-value" data-testid="ov-high-risk">{{ overview.inHospitalHighRiskCount ?? '-' }}</div>
              <div class="stat-label">在院中高危人数</div>
              <div class="stat-sub">在院 {{ overview.inHospitalCount ?? 0 }} 人中已评
                {{ overview.inHospitalAssessedCount ?? 0 }} 人
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-value stat-value-danger" data-testid="ov-pending">{{
                  overview.highRiskPendingCount ?? '-'
                }}
              </div>
              <div class="stat-label">中高危未落实人数（今天要干的事）</div>
              <div class="stat-sub">在院中高危落实率 {{ overview.highRiskPreventRate ?? '0.00' }}%</div>
            </div>
            <div class="stat-item">
              <div class="stat-value" data-testid="ov-month-event">{{ overview.monthVteEventCount ?? '-' }}</div>
              <div class="stat-label">本月院内新发 VTE</div>
              <div class="stat-sub">预防相关出血 {{ overview.monthBleedCount ?? 0 }} 例</div>
            </div>
            <div class="stat-item">
              <div class="stat-value" :class="{ 'stat-value-warn': (overview.missedAssessCount ?? 0) > 0 }"
                   data-testid="ov-missed">
                {{ overview.missedAssessCount ?? '-' }}
              </div>
              <div class="stat-label">在院未评 Caprini（漏评提醒）</div>
              <div class="stat-sub">未评者不进中高危名单</div>
            </div>
          </div>
        </el-card>

        <!-- 筛选 -->
        <div class="toolbar">
          <div data-testid="filter-admit-status">
            <el-select v-model="query.admitStatus" placeholder="在院状态" style="width: 120px">
              <el-option label="在院" :value="1"/>
              <el-option label="已出院" :value="0"/>
              <el-option label="全部" :value="null"/>
            </el-select>
          </div>
          <div data-testid="filter-ward">
            <el-select v-model="query.wardId" placeholder="病区" clearable style="width: 160px">
              <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
            </el-select>
          </div>
          <div data-testid="filter-risk">
            <el-select v-model="query.riskLevel" placeholder="风险等级" clearable style="width: 130px">
              <el-option label="低风险" :value="1"/>
              <el-option label="中风险" :value="2"/>
              <el-option label="高风险" :value="3"/>
              <el-option label="极高风险" :value="4"/>
            </el-select>
          </div>
          <div data-testid="filter-prevent-status">
            <el-select v-model="query.preventStatus" placeholder="落实状态" clearable style="width: 130px">
              <el-option label="未落实" :value="0"/>
              <el-option label="部分落实" :value="1"/>
              <el-option label="已落实" :value="2"/>
            </el-select>
          </div>
          <el-input v-model="query.keyword" placeholder="姓名 / 患者编号 / 住院号" clearable style="width: 210px"
                    data-testid="filter-keyword"/>
          <el-button type="primary" data-testid="btn-query" @click="onQuery">查询</el-button>
          <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
        </div>

        <el-table :data="rows" border stripe v-loading="loading" data-testid="risk-table">
          <el-table-column label="患者" min-width="150">
            <template #default="{ row }">
              <div>{{ row.patientName || '-' }}</div>
              <div class="muted">{{ row.patientNo || '' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="admissionNo" label="住院号" width="150" show-overflow-tooltip/>
          <el-table-column label="科室 / 病区" min-width="150">
            <template #default="{ row }">
              <div>{{ row.deptName || '-' }}</div>
              <div class="muted">{{ row.wardName || '' }}{{ row.bedNo ? ' · ' + row.bedNo + '床' : '' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="Caprini" width="110" align="right">
            <template #default="{ row }">
              <span data-testid="cell-score">{{ row.capriniScore ?? '-' }}</span>
              <span class="muted"> 分</span>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="riskLevelTag(row.riskLevel)" disable-transitions>{{
                  riskLevelText(row.riskLevel)
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="推荐措施" min-width="170">
            <template #default="{ row }">
              <span class="muted">{{ row.recommendText || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="落实状态" width="110">
            <template #default="{ row }">
              <el-tag :type="PREVENT_STATUS_TAG[row.preventStatus]" disable-transitions
                      data-testid="cell-prevent-status">
                {{ PREVENT_STATUS_TEXT[row.preventStatus] || '-' }}
              </el-tag>
              <div class="muted">{{ row.doneCount }} / {{ row.recommendCount }} 条</div>
            </template>
          </el-table-column>
          <el-table-column label="最近落实" width="165">
            <template #default="{ row }">{{ row.latestExecuteTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" data-testid="btn-measures" @click="openMeasures(row)">措施</el-button>
              <el-button
                  v-perm="'nursing:vte:preventEdit'"
                  link
                  type="primary"
                  data-testid="btn-add-measure"
                  @click="openForm(row)">登记
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <span class="muted">没有符合条件的患者（名单默认只看中高危）</span>
          </template>
        </el-table>
        <el-pagination
            class="pager"
            layout="total, sizes, prev, pager, next"
            :total="total"
            :page-sizes="PAGE_SIZES"
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            @current-change="loadRows"
            @size-change="onSizeChange"/>

        <!-- 措施详情 -->
        <el-dialog v-model="detailVisible" :title="`预防措施 · ${current.patientName || ''}`" width="640px">
          <el-table :data="detailRows" border size="small" data-testid="detail-table">
            <el-table-column label="措施" width="110">
              <template #default="{ row }">{{ MEASURE_CODE_TEXT[row.measureCode] || row.measureCode }}</template>
            </el-table-column>
            <el-table-column prop="measureName" label="内容" min-width="200" show-overflow-tooltip/>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="EXECUTE_STATUS_TAG[row.executeStatus]" disable-transitions>
                  {{ EXECUTE_STATUS_TEXT[row.executeStatus] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executorName" label="执行人" width="90"/>
            <el-table-column prop="executeTime" label="落实时间" width="165"/>
          </el-table>
          <div class="muted tip-block">未登记的措施不出现在这里；禁忌未用 / 患者拒绝的原因见下方「原因」列。</div>
          <el-table v-if="detailRows.some(r => r.reason)" :data="detailRows.filter(r => r.reason)" border size="small"
                    class="reason-table">
            <el-table-column label="措施" width="110">
              <template #default="{ row }">{{ MEASURE_CODE_TEXT[row.measureCode] || row.measureCode }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="300"/>
          </el-table>
        </el-dialog>

        <!-- 登记 / 修改措施 -->
        <el-dialog v-model="formVisible" :title="formTitle" width="520px" data-testid="measure-dialog">
          <el-form label-width="110px">
            <el-form-item label="患者">
              <div>{{ current.patientName || '-' }}（{{ current.admissionNo || '-' }}）</div>
            </el-form-item>
            <el-form-item label="风险等级">
              <el-tag :type="riskLevelTag(current.riskLevel)" disable-transitions>{{
                  riskLevelText(current.riskLevel)
                }}
              </el-tag>
              <span class="muted">Caprini {{ current.capriniScore ?? '-' }} 分</span>
            </el-form-item>
            <el-form-item label="措施" required>
              <div data-testid="sel-measure-code" style="width: 100%">
                <el-select v-model="form.measureCode" placeholder="选择措施" style="width: 100%">
                  <el-option
                      v-for="m in measureOptions"
                      :key="m.measureCode"
                      :label="m.measureName + (m.recommend ? '（推荐）' : '')"
                      :value="m.measureCode"/>
                </el-select>
              </div>
            </el-form-item>
            <el-form-item label="落实状态" required>
              <div data-testid="sel-exec-status" style="width: 100%">
                <el-radio-group v-model="form.executeStatus">
                  <el-radio :value="1">已落实</el-radio>
                  <el-radio :value="0">待落实</el-radio>
                  <el-radio :value="2">禁忌未用</el-radio>
                  <el-radio :value="3">患者拒绝</el-radio>
                </el-radio-group>
              </div>
            </el-form-item>
            <el-form-item v-if="form.executeStatus === 2 || form.executeStatus === 3" label="原因" required>
              <el-input v-model="form.reason" type="textarea" :rows="2" maxlength="500"
                        placeholder="禁忌/拒绝的原因（必填）" data-testid="ipt-reason"/>
            </el-form-item>
            <el-form-item label="措施说明">
              <el-input v-model="form.measureName" maxlength="200" placeholder="如：梯度压力袜 + 间歇充气加压装置"/>
            </el-form-item>
            <el-form-item label="计划日期">
              <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" placeholder="默认今天"
                              style="width: 100%"/>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="formVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" data-testid="btn-save-measure" @click="submitForm">保存
            </el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
      <el-tab-pane label="院内 VTE 监测" name="monitor" lazy>
        <VteMonitorView/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import VteMonitorView from './VteMonitorView.vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {getInpatientWardList} from '@/api/inpatient'
import {
  deleteVtePrevent,
  getVteMeasureOptions,
  getVteOverview,
  listVtePreventByAdmission,
  listVteRiskPage,
  upsertVtePrevent
} from '@/api/vte'
import {
  EXECUTE_STATUS_TAG,
  EXECUTE_STATUS_TEXT,
  MEASURE_CODE_TEXT,
  PREVENT_STATUS_TAG,
  PREVENT_STATUS_TEXT,
  riskLevelTag,
  riskLevelText
} from '@/lib/vte'

const overview = ref({})
const wards = ref([])
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)

const query = reactive({
  admitStatus: 1,
  wardId: null,
  riskLevel: null,
  preventStatus: null,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

async function loadOverview() {
  try {
    const res = await getVteOverview()
    overview.value = res?.data || {}
  } catch (e) {
    ElMessage.error(e?.message || '看板加载失败')
  }
}

async function loadRows() {
  loading.value = true
  try {
    const res = await listVteRiskPage({...query, keyword: query.keyword || null})
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function onQuery() {
  query.pageNum = 1
  loadRows()
}

function onReset() {
  query.admitStatus = 1
  query.wardId = null
  query.riskLevel = null
  query.preventStatus = null
  query.keyword = ''
  query.pageNum = 1
  loadRows()
}

function onSizeChange() {
  query.pageNum = 1
  loadRows()
}

// ========== 措施详情 ==========
const detailVisible = ref(false)
const detailRows = ref([])
const current = ref({})

async function openMeasures(row) {
  current.value = row
  try {
    const res = await listVtePreventByAdmission(row.admissionId)
    detailRows.value = res?.data || []
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '措施记录加载失败')
  }
}

// ========== 登记 / 修改 ==========
const formVisible = ref(false)
const formTitle = ref('')
const measureOptions = ref([])
const form = reactive({id: null, measureCode: null, executeStatus: 1, reason: '', measureName: '', planDate: null})

async function openForm(row) {
  current.value = row
  form.id = null
  form.measureCode = null
  form.executeStatus = 1
  form.reason = ''
  form.measureName = ''
  form.planDate = null
  formTitle.value = `登记预防措施 · ${row.patientName || ''}`
  try {
    const res = await getVteMeasureOptions(row.riskLevel)
    measureOptions.value = res?.data || []
  } catch (e) {
    measureOptions.value = []
  }
  formVisible.value = true
}

async function submitForm() {
  if (!form.measureCode) {
    ElMessage.warning('请选择措施');
    return
  }
  if ((form.executeStatus === 2 || form.executeStatus === 3) && !form.reason.trim()) {
    ElMessage.warning('禁忌未用 / 患者拒绝必须填写原因')
    return
  }
  saving.value = true
  try {
    await upsertVtePrevent({
      admissionId: current.value.admissionId,
      measureCode: form.measureCode,
      measureName: form.measureName || null,
      planDate: form.planDate || null,
      executeStatus: form.executeStatus,
      reason: form.reason || null,
    })
    ElMessage.success('措施已保存')
    formVisible.value = false
    loadRows()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function doDelete(id) {
  try {
    await deleteVtePrevent(id)
    ElMessage.success('已删除')
    loadRows()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// 合并页签的当前页。被并页面自带 onMounted 请求，故其页签用 lazy —— 进页不预拉两套数据。
const mergedTab = ref('prevent')

onMounted(async () => {
  loadOverview()
  loadRows()
  try {
    const res = await getInpatientWardList()
    wards.value = res?.data || []
  } catch (e) {
    wards.value = []
  }
})

// 暴露给抽屉内删除按钮（模板里未直接用则无害）
defineExpose({doDelete})
</script>

<style scoped>
.vte-prevent-page {
  padding: 12px;
}

.stat-card {
  margin-bottom: 12px;
}

.stat-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 180px;
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

.stat-value-warn {
  color: #e6a23c;
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 10px;
  justify-content: flex-end;
}

.muted {
  color: #909399;
  font-size: 12px;
}

.tip-block {
  margin-top: 8px;
  line-height: 1.6;
}

.reason-table {
  margin-top: 8px;
}

/* 被并页面自带页级留白，嵌进页签后统一由宿主提供，避免双层 padding */
.merged-tabs :deep(.el-tab-pane > .vte-monitor-page) {
  padding: 0;
}

</style>
