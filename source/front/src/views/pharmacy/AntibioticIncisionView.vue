<template>
  <div class="antibiotic-incision-page" data-testid="antibiotic-incision-view">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="I 类切口手术预防用药专项点评"
      description="点评对象 = 已完成且切口等级为 I 类的手术。规范：I 类切口原则上不预防用药，确需时选一/二代头孢，术前 0.5~1 小时给药（剖宫产为结扎脐带后），总疗程 ≤24 小时，不联合用药；用特殊使用级须经抗菌药物管理工作组会诊同意。" />

    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- ============ tab1 待点评 ============ -->
      <el-tab-pane :label="`待点评（${candidates.length}）`" name="candidate">
        <el-table :data="candidates" border stripe v-loading="candLoading" data-testid="candidate-table">
          <el-table-column prop="patientName" label="患者" width="100" />
          <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
          <el-table-column prop="operationName" label="手术名称" min-width="200" show-overflow-tooltip />
          <el-table-column label="手术时间" width="170">
            <template #default="{ row }">{{ row.operationTime || '—' }}</template>
          </el-table-column>
          <el-table-column prop="surgeonName" label="主刀" width="100" />
          <el-table-column label="围手术期抗菌药证据" min-width="260">
            <template #default="{ row }">
              <template v-if="(row.drugCandidates || []).length">
                <div v-for="d in row.drugCandidates" :key="d.orderId" class="drug-line">
                  {{ d.drugName }}（{{ antibioticLevelText(d.antibioticLevel) }}）
                  {{ d.startTime }}
                  <el-tag size="small" :type="d.minutesFromIncision < 0 ? 'success' : 'danger'">
                    {{ timingDeltaText(d.minutesFromIncision) }}
                  </el-tag>
                </div>
              </template>
              <span v-else class="muted">围手术期 24 小时内无抗菌药物医嘱</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-perm="'pharmacy:antibiotic:incisionReview'"
                link
                type="primary"
                data-testid="btn-review"
                @click="openReviewDialog(row)">点评</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ============ tab2 已点评 ============ -->
      <el-tab-pane label="点评记录" name="reviewed">
        <div class="toolbar">
          <div data-testid="review-keyword">
            <el-input
              v-model="reviewQuery.keyword"
              placeholder="患者姓名 / 手术名称"
              clearable
              style="width: 200px"
              @keyup.enter="loadReviews"
              @clear="loadReviews" />
          </div>
          <div data-testid="review-result">
            <el-select v-model="reviewQuery.reviewResult" placeholder="点评结论" clearable style="width: 130px" @change="loadReviews">
              <el-option label="合理" :value="1" />
              <el-option label="不合理" :value="2" />
            </el-select>
          </div>
          <el-button type="primary" data-testid="btn-review-query" @click="loadReviews">查询</el-button>
          <el-button @click="resetReviews">重置</el-button>
        </div>
        <el-table :data="reviewRows" border stripe v-loading="reviewLoading" data-testid="review-table">
          <el-table-column prop="reviewNo" label="点评编号" width="150" />
          <el-table-column prop="patientName" label="患者" width="100" />
          <el-table-column prop="operationName" label="手术名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="surgeonName" label="主刀" width="90" />
          <el-table-column label="预防用药" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.drugName">{{ row.drugName }}（{{ antibioticLevelText(row.antibioticLevel) }}）</span>
              <span v-else class="muted">未用</span>
            </template>
          </el-table-column>
          <el-table-column label="给药时机" width="130">
            <template #default="{ row }">{{ timingText(row.timingType) }}</template>
          </el-table-column>
          <el-table-column label="疗程（h）" width="90" align="right">
            <template #default="{ row }">
              <span :class="{ 'over-target': (row.courseHours || 0) > 24 }">{{ row.courseHours ?? '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结论" width="100">
            <template #default="{ row }">
              <el-tag :type="row.reviewResult === 1 ? 'success' : 'danger'" data-testid="cell-review-result">
                {{ row.reviewResultText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="问题" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ incisionProblemText(row.problemTypes) }}</template>
          </el-table-column>
          <el-table-column prop="reviewerName" label="点评人" width="100" />
          <el-table-column prop="reviewTime" label="点评时间" width="170" />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button
                v-perm="'pharmacy:antibiotic:incisionReview'"
                link
                type="primary"
                @click="openReviewDialog(null, row)">重评</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- ⚠ 见分级目录页同款注释：layout 含 sizes 时 page-size 必须 v-model + @size-change，
             否则 EP 2.14 把分页组件渲染成 null（分页条直接消失） -->
        <el-pagination
          class="pager"
          layout="total, sizes, prev, pager, next"
          :total="reviewTotal"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="reviewQuery.pageNum"
          v-model:page-size="reviewQuery.pageSize"
          @current-change="loadReviews"
          @size-change="onReviewSizeChange" />
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 点评 ============ -->
    <AntibioticIncisionEdit v-model="reviewDialogVisible" :candidate="reviewCandidate" :row="reviewRow" @saved="onReviewSaved" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  antibioticLevelText,
  incisionProblemText,
  timingText
} from '@/lib/antibiotic'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {
  getIncisionCandidates,
  listIncisionReviewPage
} from '@/api/antibiotic'
import AntibioticIncisionEdit from './AntibioticIncisionEdit.vue'

const activeTab = ref('candidate')

// ---------- 待点评 ----------
const candidates = ref([])
const candLoading = ref(false)

async function loadCandidates() {
  candLoading.value = true
  try {
    const res = await getIncisionCandidates()
    candidates.value = res?.data || []
  } finally {
    candLoading.value = false
  }
}

function timingDeltaText(minutes) {
  if (minutes === null || minutes === undefined) return '—'
  const abs = Math.abs(minutes)
  const h = Math.floor(abs / 60)
  const m = abs % 60
  const txt = (h ? `${h}小时` : '') + (m ? `${m}分` : '') || '0分'
  return minutes < 0 ? `术前 ${txt}` : `术后 ${txt}`
}

// ---------- 已点评 ----------
const reviewQuery = reactive({ keyword: '', reviewResult: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const reviewRows = ref([])
const reviewTotal = ref(0)
const reviewLoading = ref(false)

async function loadReviews() {
  reviewLoading.value = true
  try {
    const res = await listIncisionReviewPage({ ...reviewQuery })
    reviewRows.value = res?.data?.records || []
    reviewTotal.value = Number(res?.data?.total || 0)
  } finally {
    reviewLoading.value = false
  }
}

function onReviewSizeChange() {
  reviewQuery.pageNum = 1
  loadReviews()
}

function resetReviews() {
  reviewQuery.keyword = ''
  reviewQuery.reviewResult = null
  reviewQuery.pageNum = 1
  loadReviews()
}

// ---------- 点评弹框 ----------
const reviewDialogVisible = ref(false)
const reviewCandidate = ref(null)
const reviewRow = ref(null)

function openReviewDialog(candidate, row) {
  reviewCandidate.value = candidate || null
  reviewRow.value = row || null
  reviewDialogVisible.value = true
}

function onReviewSaved() {
  loadCandidates()
  loadReviews()
}

onMounted(() => {
  loadCandidates()
  loadReviews()
})
</script>

<style scoped>
.antibiotic-incision-page {
  padding: 12px;
}
.main-tabs {
  margin-top: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.pager {
  margin-top: 10px;
  justify-content: flex-end;
}
.muted {
  color: #909399;
  font-size: 12px;
}
.drug-line {
  line-height: 1.8;
}
.over-target {
  color: #f56c6c;
  font-weight: 600;
}
</style>
