<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <div>
          <div class="head-title">超常处方公示</div>
          <div class="head-sub">
            依据《医院处方点评管理规范（试行）》，对点评发现的不规范处方、用药不适宜处方与超常处方进行公示；
            医师超常处方 3 次以上且无正当理由的，予以警告并限制处方权。公示内容只增不可撤。
          </div>
        </div>
        <el-form inline @submit.prevent class="shrink-0">
          <el-form-item label="公示日期">
            <div data-testid="publicity-date-range">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="公示起"
                end-placeholder="公示止"
                @change="reloadAll" />
            </div>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 医师排名：超常 ≥3 标红（应约谈/限制处方权） -->
    <el-card shadow="never" class="table-card mb-3">
      <template #header><span class="section-title">医师不合理处方排名（已公示口径）</span></template>
      <el-table :data="doctorStats" stripe v-loading="statsLoading" empty-text="暂无公示数据">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="doctorName" label="医师" width="110" />
        <el-table-column prop="deptName" label="科室" min-width="140" show-overflow-tooltip />
        <el-table-column prop="reviewCount" label="被公示不合理处方" width="150" align="center" />
        <el-table-column label="超常处方" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'abnormal-danger': row.needTalk }">{{ row.abnormalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="处理提示" min-width="220">
          <template #default="{ row }">
            <el-tag v-if="row.needTalk" type="danger">超常 ≥3 次，应警告并限制处方权（约谈）</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastPublicityTime" label="最近公示时间" width="170" />
      </el-table>
    </el-card>

    <!-- 已公示明细 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="section-head">
          <span class="section-title">已公示处方明细</span>
          <el-input
            v-model="query.doctorName"
            placeholder="医师姓名"
            clearable
            style="width: 160px"
            @keyup.enter="loadPublicity"
            @clear="loadPublicity" />
          <el-input
            v-model="query.prescriptionNo"
            placeholder="处方号"
            clearable
            style="width: 180px"
            @keyup.enter="loadPublicity"
            @clear="loadPublicity" />
          <el-button type="primary" @click="loadPublicity">查询</el-button>
        </div>
      </template>
      <el-table :data="publicityRows" stripe :max-height="tableMaxHeight" v-loading="listLoading">
        <el-table-column prop="prescriptionNo" label="处方号" width="180" />
        <el-table-column prop="patientName" label="患者" width="90" />
        <el-table-column prop="doctorName" label="医师" width="100" />
        <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
        <el-table-column prop="visitDate" label="就诊日期" width="110" />
        <el-table-column label="点评结论" width="130">
          <template #default="{ row }">
            <el-tag :type="RESULT_TAG_TYPE[row.reviewResult]">{{ resultText(row.reviewResult) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="问题项" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ problemTypesText(row.problemTypes) }}</template>
        </el-table-column>
        <el-table-column prop="reviewOpinion" label="点评意见" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publicityBy" label="公示人" width="100" />
        <el-table-column prop="publicityTime" label="公示时间" width="170" />
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :total="publicityTotal"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          @current-change="loadPublicity"
          @size-change="query.pageNum = 1; loadPublicity()" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RESULT_TAG_TYPE, problemTypesText, resultText } from '@/lib/rxReview'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import { getRxPublicityStats, listRxPublicityPage } from '@/api/pharmacy'

const dateRange = ref([])
const query = reactive({ prescriptionNo: '', doctorName: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const publicityRows = ref([])
const publicityTotal = ref(0)
const listLoading = ref(false)
const doctorStats = ref([])
const statsLoading = ref(false)

async function loadPublicity() {
  listLoading.value = true
  try {
    const res = await listRxPublicityPage({ ...query })
    publicityRows.value = res?.data?.records || []
    publicityTotal.value = Number(res?.data?.total || 0)
  } finally {
    listLoading.value = false
  }
}

async function loadStats() {
  statsLoading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.dateStart = dateRange.value[0]
      params.dateEnd = dateRange.value[1]
    }
    const res = await getRxPublicityStats(params)
    doctorStats.value = res?.data || []
  } finally {
    statsLoading.value = false
  }
}

function reloadAll() {
  loadStats()
  loadPublicity()
}

onMounted(reloadAll)
</script>

<style scoped>
.head-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 6px;
}

.head-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  max-width: 760px;
  line-height: 1.6;
}

.section-title {
  font-weight: 600;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-head .section-title {
  margin-right: auto;
}

.abnormal-danger {
  color: var(--el-color-danger);
  font-weight: 700;
}

.muted {
  color: var(--el-text-color-placeholder);
}
</style>
