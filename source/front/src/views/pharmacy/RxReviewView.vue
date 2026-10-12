<template>
  <div class="rx-review-page">
    <!-- sql/191：515「处方点评」与 516「处方公示」合并（公示是点评的输出环节，515 名下本就有 pharmacy:rxReview:publicity 按钮码），516 置 is_visible=0 退出侧栏但保留 pharmacy:rxPublicity:list 权限码。 -->
    <el-tabs v-model="mergedTab" class="merged-tabs">
      <el-tab-pane label="处方点评" name="review">
        <!-- 评审口径统计卡：点评率 / 不合理处方率 / 超常处方 -->
        <el-card shadow="never" class="stat-card">
          <div class="stat-head">
            <span class="stat-title">处方点评统计</span>
            <div data-testid="stats-month">
              <el-date-picker
                v-model="statsMonth"
                type="month"
                placeholder="选择月份"
                value-format="YYYY-MM"
                :clearable="false"
                style="width: 140px"
                @change="loadStats" />
            </div>
          </div>
          <div class="stat-items">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalPrescriptions ?? '-' }}</div>
              <div class="stat-label">期内处方总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.reviewedCount ?? '-' }}</div>
              <div class="stat-label">已点评处方</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.reviewRate ?? '-' }}%</div>
              <div class="stat-label">处方点评率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.unreasonableCount ?? '-' }}</div>
              <div class="stat-label">不合理处方（{{ stats.unreasonableRate ?? '-' }}%）</div>
            </div>
            <div class="stat-item">
              <div class="stat-value stat-value-danger">{{ stats.abnormalCount ?? '-' }}</div>
              <div class="stat-label">超常处方</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.publicityCount ?? '-' }}</div>
              <div class="stat-label">已公示</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.pendingCount ?? '-' }}</div>
              <div class="stat-label">待点评</div>
            </div>
          </div>
        </el-card>

        <el-tabs v-model="activeTab" class="main-tabs">
          <!-- ============ tab1 点评批次 ============ -->
          <el-tab-pane label="点评批次" name="batch">
            <div class="toolbar">
              <el-input
                v-model="batchQuery.keyword"
                placeholder="批次号 / 批次名称"
                clearable
                style="width: 200px"
                @keyup.enter="loadBatches"
                @clear="loadBatches" />
              <el-select v-model="batchQuery.status" placeholder="状态" clearable style="width: 120px" @change="loadBatches">
                <el-option v-for="(text, code) in BATCH_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-button type="primary" @click="loadBatches">查询</el-button>
              <div class="toolbar-right">
                <el-button v-perm="'pharmacy:rxReview:add'" type="primary" data-testid="btn-new-batch" @click="openBatchDialog()">
                  新建点评批次
                </el-button>
              </div>
            </div>
            <el-table :data="batchRows" border stripe v-loading="batchLoading">
              <el-table-column prop="batchNo" label="批次号" width="180" />
              <el-table-column prop="batchName" label="批次名称" min-width="200" show-overflow-tooltip />
              <el-table-column label="类型" width="150">
                <template #default="{ row }">
                  {{ REVIEW_TYPE[row.reviewType] || '未知(' + row.reviewType + ')' }}
                  <span v-if="row.reviewType === 2">（{{ row.specialty }}）</span>
                </template>
              </el-table-column>
              <el-table-column label="处方日期范围" width="200">
                <template #default="{ row }">{{ row.dateStart }} ~ {{ row.dateEnd }}</template>
              </el-table-column>
              <el-table-column prop="sampleCount" label="抽样数" width="80" align="center" />
              <el-table-column prop="reviewedCount" label="已点评" width="80" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 2 ? 'info' : 'primary'">{{ BATCH_STATUS[row.status] || '未知(' + row.status + ')' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reviewerName" label="点评人" width="100" />
              <el-table-column prop="createTime" label="创建时间" width="170" />
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="enterBatch(row)">进入点评</el-button>
                  <el-button
                    v-if="row.status === 1"
                    v-perm="'pharmacy:rxReview:add'"
                    link
                    type="warning"
                    @click="doCompleteBatch(row)">完成批次</el-button>
                  <el-button v-perm="'pharmacy:rxReview:delete'" link type="danger" @click="doDeleteBatch(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pager"
              layout="total, sizes, prev, pager, next"
              :total="batchTotal"
              :page-sizes="PAGE_SIZES"
              v-model:current-page="batchQuery.pageNum"
              v-model:page-size="batchQuery.pageSize"
              @current-change="loadBatches"
              @size-change="batchQuery.pageNum = 1; loadBatches()" />
          </el-tab-pane>

          <!-- ============ tab2 点评明细 ============ -->
          <el-tab-pane label="点评明细" name="item">
            <div class="toolbar">
              <el-select
                v-model="itemQuery.batchId"
                placeholder="选择点评批次"
                style="width: 260px"
                @change="loadItems">
                <el-option
                  v-for="b in batchOptions"
                  :key="b.id"
                  :label="b.batchNo + ' ' + b.batchName"
                  :value="b.id" />
              </el-select>
              <el-input
                v-model="itemQuery.prescriptionNo"
                placeholder="处方号"
                clearable
                style="width: 160px"
                @keyup.enter="loadItems"
                @clear="loadItems" />
              <el-input
                v-model="itemQuery.doctorName"
                placeholder="医生姓名"
                clearable
                style="width: 130px"
                @keyup.enter="loadItems"
                @clear="loadItems" />
              <el-select v-model="itemQuery.reviewStatus" placeholder="点评状态" clearable style="width: 120px" @change="loadItems">
                <el-option v-for="(text, code) in REVIEW_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-select v-model="itemQuery.reviewResult" placeholder="结论" clearable style="width: 150px" @change="loadItems">
                <el-option v-for="(text, code) in REVIEW_RESULT" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-button type="primary" @click="loadItems">查询</el-button>
              <div class="toolbar-right">
                <el-button
                  v-perm="'pharmacy:rxReview:add'"
                  data-testid="btn-add-by-no"
                  @click="addPromptVisible = true">按处方号补录</el-button>
                <el-button
                  v-perm="'pharmacy:rxReview:publicity'"
                  type="danger"
                  :disabled="!selectedItems.length"
                  data-testid="btn-publicity"
                  @click="doPublicitySelected">公示所选（{{ selectedItems.length }}）</el-button>
                <el-button v-perm="'pharmacy:rxReview:export'" @click="doExport">导出台账</el-button>
              </div>
            </div>
            <el-table :data="itemRows" border stripe v-loading="itemLoading" @selection-change="onItemSelectionChange">
              <el-table-column type="selection" width="42" :selectable="itemSelectable" />
              <el-table-column prop="prescriptionNo" label="处方号" width="170" />
              <el-table-column prop="patientName" label="患者" width="90" />
              <el-table-column prop="doctorName" label="医生" width="100" />
              <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
              <el-table-column prop="visitDate" label="就诊日期" width="110" />
              <el-table-column prop="totalAmount" label="金额" width="90" align="right" />
              <el-table-column label="点评结论" width="130">
                <template #default="{ row }">
                  <el-tag v-if="row.reviewResult" :type="RESULT_TAG_TYPE[row.reviewResult]">
                    {{ resultText(row.reviewResult) }}
                  </el-tag>
                  <span v-else class="muted">待点评</span>
                </template>
              </el-table-column>
              <el-table-column label="问题项" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">{{ problemTypesText(row.problemTypes) }}</template>
              </el-table-column>
              <el-table-column label="公示" width="90" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.publicityStatus === 1" type="danger">已公示</el-tag>
                  <span v-else class="muted">—</span>
                </template>
              </el-table-column>
              <el-table-column prop="reviewerName" label="点评人" width="100" />
              <el-table-column label="操作" width="130" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-perm="'pharmacy:rxReview:add'"
                    link
                    type="primary"
                    :disabled="row.publicityStatus === 1"
                    @click="openReviewDialog(row)">点评</el-button>
                  <el-button
                    v-if="canPublicity(row)"
                    v-perm="'pharmacy:rxReview:publicity'"
                    link
                    type="danger"
                    @click="doPublicity(row)">公示</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pager"
              layout="total, sizes, prev, pager, next"
              :total="itemTotal"
              :page-sizes="PAGE_SIZES"
              v-model:current-page="itemQuery.pageNum"
              v-model:page-size="itemQuery.pageSize"
              @current-change="loadItems"
              @size-change="itemQuery.pageNum = 1; loadItems()" />
          </el-tab-pane>

          <!-- ============ tab3 医师约谈 ============ -->
          <el-tab-pane label="医师约谈" name="talk">
            <div class="toolbar">
              <el-input
                v-model="talkQuery.keyword"
                placeholder="医师姓名 / 约谈编号"
                clearable
                style="width: 200px"
                @keyup.enter="loadTalks"
                @clear="loadTalks" />
              <el-select v-model="talkQuery.talkType" placeholder="约谈类型" clearable style="width: 140px" @change="loadTalks">
                <el-option v-for="(text, code) in TALK_TYPE" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-select v-model="talkQuery.rectifyStatus" placeholder="整改状态" clearable style="width: 120px" @change="loadTalks">
                <el-option v-for="(text, code) in RECTIFY_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-button type="primary" @click="loadTalks">查询</el-button>
              <div class="toolbar-right">
                <el-button v-perm="'pharmacy:rxReview:talk'" type="primary" data-testid="btn-new-talk" @click="openTalkDialog()">
                  登记约谈
                </el-button>
              </div>
            </div>
            <el-table :data="talkRows" border stripe v-loading="talkLoading">
              <el-table-column prop="talkNo" label="约谈编号" width="170" />
              <el-table-column prop="doctorName" label="被约谈医师" width="110" />
              <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
              <el-table-column label="约谈类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.talkType >= 3 ? 'danger' : 'warning'">{{ TALK_TYPE[row.talkType] || '未知(' + row.talkType + ')' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="talkTime" label="约谈时间" width="170" />
              <el-table-column prop="talkerName" label="约谈人" width="100" />
              <el-table-column prop="talkerOrg" label="约谈部门" width="110" />
              <el-table-column prop="relatedCount" label="关联不合理处方" width="130" align="center" />
              <el-table-column label="整改" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.rectifyStatus === 2 ? 'success' : 'info'">{{ RECTIFY_STATUS[row.rectifyStatus] || '未知(' + row.rectifyStatus + ')' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="医师确认" width="150">
                <template #default="{ row }">
                  <template v-if="row.doctorConfirm === 1">
                    <el-tag type="success">{{ row.doctorConfirmBy }}</el-tag>
                  </template>
                  <span v-else class="muted">未确认</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.doctorConfirm !== 1"
                    v-perm="'pharmacy:rxReview:talk'"
                    link
                    type="primary"
                    @click="openTalkDialog(row)">编辑</el-button>
                  <el-button
                    v-if="row.doctorConfirm !== 1"
                    v-perm="'pharmacy:rxReview:talk'"
                    link
                    type="success"
                    @click="doConfirmTalk(row)">医师签字确认</el-button>
                  <el-button
                    v-if="row.doctorConfirm !== 1"
                    v-perm="'pharmacy:rxReview:delete'"
                    link
                    type="danger"
                    @click="doDeleteTalk(row)">删除</el-button>
                  <el-button link type="info" @click="showTalkDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pager"
              layout="total, sizes, prev, pager, next"
              :total="talkTotal"
              :page-sizes="PAGE_SIZES"
              v-model:current-page="talkQuery.pageNum"
              v-model:page-size="talkQuery.pageSize"
              @current-change="loadTalks"
              @size-change="talkQuery.pageNum = 1; loadTalks()" />
          </el-tab-pane>
        </el-tabs>

        <!-- ============ 新建/编辑批次 ============ -->
        <RxReviewBatchEdit v-model="batchDialogVisible" :row="batchEditRow" @saved="onBatchSaved" />

        <!-- ============ 提交点评 ============ -->
        <RxReviewItemEdit v-model="reviewDialogVisible" :row="reviewRow" @saved="onReviewSaved" />

        <!-- ============ 按处方号补录 ============ -->
        <RxReviewItemAdd v-model="addPromptVisible" :batch-id="itemQuery.batchId" @saved="onAddByNoSaved" />

        <!-- ============ 登记约谈 ============ -->
        <RxReviewTalkEdit v-model="talkDialogVisible" :row="talkEditRow" @saved="onTalkSaved" />

        <!-- ============ 约谈详情（只读） ============ -->
        <RxReviewTalkDetail v-model="talkDetailVisible" :row="talkDetailRow" />
      </el-tab-pane>
      <el-tab-pane label="超常处方公示" name="publicity" lazy>
        <RxPublicityView />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RxPublicityView from './RxPublicityView.vue'
import RxReviewBatchEdit from './RxReviewBatchEdit.vue'
import RxReviewItemEdit from './RxReviewItemEdit.vue'
import RxReviewItemAdd from './RxReviewItemAdd.vue'
import RxReviewTalkEdit from './RxReviewTalkEdit.vue'
import RxReviewTalkDetail from './RxReviewTalkDetail.vue'
import {
  BATCH_STATUS,
  PUBLICITY_STATUS,
  RECTIFY_STATUS,
  RESULT_TAG_TYPE,
  REVIEW_RESULT,
  REVIEW_STATUS,
  REVIEW_TYPE,
  TALK_TYPE,
  problemTypesText,
  resultText,
  UNREASONABLE_RESULTS
} from '@/lib/rxReview'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {
  completeRxBatch,
  confirmRxTalk,
  deleteRxBatchById,
  deleteRxTalkById,
  exportRxItemCsv,
  getRxReviewStats,
  listRxBatchPage,
  listRxItemPage,
  listRxTalkPage,
  publicityRxItems
} from '@/api/pharmacy'

const activeTab = ref('batch')

// ---------- 统计 ----------
const statsMonth = ref(new Date().toISOString().slice(0, 7))
const stats = ref({})
async function loadStats() {
  const res = await getRxReviewStats(statsMonth.value || undefined)
  stats.value = res?.data || {}
}

// ---------- 批次 ----------
const batchQuery = reactive({ keyword: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const batchRows = ref([])
const batchTotal = ref(0)
const batchLoading = ref(false)
const batchOptions = ref([])

async function loadBatches() {
  batchLoading.value = true
  try {
    const res = await listRxBatchPage({ ...batchQuery })
    batchRows.value = res?.data?.records || []
    batchTotal.value = Number(res?.data?.total || 0)
  } finally {
    batchLoading.value = false
  }
}

// 批次下拉候选（一次性抓全量，供明细 tab 选择；不是分页查询）
async function loadBatchOptions() {
  const res = await listRxBatchPage({ pageNum: 1, pageSize: 200 })
  batchOptions.value = res?.data?.records || []
}

function enterBatch(row) {
  itemQuery.batchId = row.id
  itemQuery.prescriptionNo = ''
  itemQuery.doctorName = ''
  itemQuery.reviewStatus = null
  itemQuery.reviewResult = null
  activeTab.value = 'item'
  loadItems()
}

const batchDialogVisible = ref(false)
const batchEditRow = ref(null)

function openBatchDialog(row) {
  batchEditRow.value = row || null
  batchDialogVisible.value = true
}

async function onBatchSaved() {
  await loadBatches()
  await loadBatchOptions()
}

async function doCompleteBatch(row) {
  await ElMessageBox.confirm(`确认完成批次「${row.batchName}」？完成后不可再点评/补录。`, '完成批次')
  await completeRxBatch(row.id)
  ElMessage.success('批次已完成归档')
  await loadBatches()
  await loadBatchOptions()
}

async function doDeleteBatch(row) {
  await ElMessageBox.confirm(
    `确认删除批次「${row.batchName}」？未点评明细将一并物理删除；已有点评结论的批次不可删。`,
    '删除批次',
    { type: 'warning' }
  )
  await deleteRxBatchById(row.id)
  ElMessage.success('批次已删除')
  await loadBatches()
  await loadBatchOptions()
}

// ---------- 明细 ----------
const itemQuery = reactive({
  batchId: null,
  prescriptionNo: '',
  doctorName: '',
  reviewStatus: null,
  reviewResult: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
})
const itemRows = ref([])
const itemTotal = ref(0)
const itemLoading = ref(false)
const selectedItems = ref([])

function itemSelectable(row) {
  return canPublicity(row)
}

function canPublicity(row) {
  return row.reviewStatus === 1
    && UNREASONABLE_RESULTS.includes(row.reviewResult)
    && row.publicityStatus !== 1
}

async function loadItems() {
  itemLoading.value = true
  try {
    const res = await listRxItemPage({ ...itemQuery })
    itemRows.value = res?.data?.records || []
    itemTotal.value = Number(res?.data?.total || 0)
  } finally {
    itemLoading.value = false
  }
}

function onItemSelectionChange(rows) {
  selectedItems.value = rows
}

async function doPublicity(row) {
  await ElMessageBox.confirm(
    `确认公示处方 ${row.prescriptionNo}（${resultText(row.reviewResult)}）？公示只增不可撤，公示后点评结论不可修改。`,
    '公示处方',
    { type: 'warning' }
  )
  await publicityRxItems([row.id])
  ElMessage.success('已公示')
  await loadItems()
  await loadStats()
}

async function doPublicitySelected() {
  const ids = selectedItems.value.map(r => r.id)
  await ElMessageBox.confirm(`确认公示所选 ${ids.length} 张不合理处方？公示只增不可撤。`, '批量公示', { type: 'warning' })
  const res = await publicityRxItems(ids)
  ElMessage.success(`已公示 ${res?.data ?? ids.length} 张`)
  await loadItems()
  await loadStats()
}

const addPromptVisible = ref(false)

async function onAddByNoSaved() {
  await loadItems()
  await loadStats()
}

// ---------- 点评 ----------
const reviewDialogVisible = ref(false)
const reviewRow = ref(null)

function openReviewDialog(row) {
  reviewRow.value = row
  reviewDialogVisible.value = true
}

async function onReviewSaved() {
  await loadItems()
  await loadStats()
  await loadBatches()
}

// ---------- 约谈 ----------
const talkQuery = reactive({ keyword: '', talkType: null, rectifyStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const talkRows = ref([])
const talkTotal = ref(0)
const talkLoading = ref(false)
const talkDialogVisible = ref(false)
const talkDetailVisible = ref(false)
const talkDetailRow = ref(null)
const talkEditRow = ref(null)

async function loadTalks() {
  talkLoading.value = true
  try {
    const res = await listRxTalkPage({ ...talkQuery })
    talkRows.value = res?.data?.records || []
    talkTotal.value = Number(res?.data?.total || 0)
  } finally {
    talkLoading.value = false
  }
}

function openTalkDialog(row) {
  talkEditRow.value = row || null
  talkDialogVisible.value = true
}

async function onTalkSaved() {
  await loadTalks()
}

async function doConfirmTalk(row) {
  const { value } = await ElMessageBox.prompt(
    `医师「${row.doctorName}」签字确认约谈记录 ${row.talkNo}。请输入签字医师姓名：`,
    '医师签字确认',
    { inputPattern: /\S+/, inputErrorMessage: '请输入签字医师姓名' }
  )
  await confirmRxTalk(row.id, value.trim())
  ElMessage.success('已记录医师签字确认')
  await loadTalks()
}

function showTalkDetail(row) {
  talkDetailRow.value = row
  talkDetailVisible.value = true
}

async function doDeleteTalk(row) {
  await ElMessageBox.confirm(`确认删除约谈记录 ${row.talkNo}？医师已签字确认的记录不可删除。`, '删除约谈', { type: 'warning' })
  await deleteRxTalkById(row.id)
  ElMessage.success('约谈记录已删除')
  await loadTalks()
}

// ---------- 导出 ----------
async function doExport() {
  const res = await exportRxItemCsv({ ...itemQuery })
  const blob = new Blob([res?.data || ''], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `处方点评台账_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出完成')
}

// 合并页签的当前页。被并页面自带 onMounted 请求，故其页签用 lazy —— 进页不预拉两套数据。
const mergedTab = ref('review')

onMounted(async () => {
  await Promise.all([loadStats(), loadBatches(), loadBatchOptions(), loadItems(), loadTalks()])
})
</script>

<style scoped>
.rx-review-page {
  padding: 16px;
}

.stat-card {
  margin-bottom: 12px;
}

.stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-title {
  font-weight: 600;
  font-size: 15px;
}

.stat-items {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 130px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
}

.stat-value-danger {
  color: var(--el-color-danger);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.main-tabs {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 4px 16px 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin: 8px 0 12px;
}

.toolbar-right {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.muted {
  color: var(--el-text-color-placeholder);
}

/* 被并页面自带页级留白，嵌进页签后统一由宿主提供，避免双层 padding */
.merged-tabs :deep(.el-tab-pane > .rx-publicity-page) {
  padding: 0;
}

</style>
