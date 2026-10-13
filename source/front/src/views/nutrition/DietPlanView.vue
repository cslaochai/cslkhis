<template>
  <div data-testid="diet-plan-view">
    <el-card shadow="never" class="stat-card">
      <div class="stat-items">
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-danger': (overview.pendingConfirmPlanCount ?? 0) > 0 }" data-testid="ov-pending">
            {{ overview.pendingConfirmPlanCount ?? '-' }}
          </div>
          <div class="stat-label">待接收膳食方案</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-meal-today">{{ overview.todayMealCount ?? '-' }}</div>
          <div class="stat-label">今日订餐条数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-warn': (overview.todayMealPendingCount ?? 0) > 0 }" data-testid="ov-meal-pending">
            {{ overview.todayMealPendingCount ?? '-' }}
          </div>
          <div class="stat-label">今日尚未签收的餐</div>
          <div class="stat-sub">已签收 {{ overview.todayMealSignedCount ?? 0 }} 条 · 签收率 {{ overview.todayMealSignRate ?? '0.00' }}%</div>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="接收状态">
            <div data-testid="filter-confirm-status">
              <el-select v-model="query.confirmStatus" placeholder="接收状态" clearable style="width: 130px">
                <el-option v-for="(t, code) in CONFIRM_STATUS_TEXT" :key="code" :label="t" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="方案状态">
            <div data-testid="filter-plan-status">
              <el-select v-model="query.planStatus" placeholder="方案状态" clearable style="width: 120px">
                <el-option v-for="(t, code) in PLAN_STATUS_TEXT" :key="code" :label="t" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="来源">
            <div data-testid="filter-source">
              <el-select v-model="query.source" placeholder="来源" clearable style="width: 150px">
                <el-option v-for="(t, code) in PLAN_SOURCE_TEXT" :key="code" :label="t" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="饮食类别">
            <div data-testid="filter-category">
              <el-select v-model="query.dietCategory" placeholder="饮食类别" clearable style="width: 130px">
                <el-option v-for="(t, code) in DIET_CATEGORY_TEXT" :key="code" :label="t" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="给食途径">
            <div data-testid="filter-route">
              <el-select v-model="query.route" placeholder="给食途径" clearable style="width: 140px">
                <el-option v-for="(t, code) in DIET_ROUTE_TEXT" :key="code" :label="t" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="病区">
            <div data-testid="filter-ward">
              <el-select v-model="query.wardId" placeholder="病区" clearable style="width: 150px">
                <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="开始日期起"
              end-placeholder="开始日期止"
              style="width: 240px"
              data-testid="filter-daterange" />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="姓名 / 患者编号 / 住院号 / 方案号 / 医嘱号" clearable style="width: 260px" data-testid="filter-keyword" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-query" @click="onQuery">查询</el-button>
            <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：批量接收/退回与登记统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:diet:confirm'" type="success" :disabled="!selection.length" data-testid="btn-batch-accept" @click="doConfirm()">
            批量接收（{{ selection.length }}）
          </el-button>
          <el-button v-perm="'ipd:diet:confirm'" :disabled="!selection.length" data-testid="btn-batch-reject" @click="rejectVisible = true">
            批量退回（{{ selection.length }}）
          </el-button>
          <el-button v-perm="'ipd:diet:edit'" type="primary" data-testid="btn-add-plan" @click="openForm()">手工登记方案</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe :max-height="tableMaxHeight" v-loading="loading" data-testid="plan-table" @selection-change="selection = $event">
      <el-table-column type="selection" width="42" />
      <el-table-column label="患者" min-width="140">
        <template #default="{ row }">
          <div>{{ row.patientName || '-' }}</div>
          <div class="muted">{{ row.patientNo || '' }}{{ row.age != null ? ' · ' + row.age + '岁' : '' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="admissionNo" label="住院号" width="140" show-overflow-tooltip />
      <el-table-column label="科室 / 病区" min-width="140">
        <template #default="{ row }">
          <div>{{ row.deptName || '-' }}</div>
          <div class="muted">{{ row.wardName || '' }}{{ row.bedNo ? ' · ' + row.bedNo + '床' : '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="饮食类型" min-width="170">
        <template #default="{ row }">
          <span :class="{ 'to-determine': row.dietCode === TO_DETERMINE_CODE }" data-testid="cell-diet-name">{{ row.dietName || '-' }}</span>
          <div class="muted">{{ row.dietCategoryText || '' }} · {{ row.dietCode || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="途径 / 餐次" min-width="170">
        <template #default="{ row }">
          <div>
            {{ row.routeText || '-' }}
            <el-tag v-if="Number(row.needsMeal) === 1" type="success" size="small" disable-transitions>食堂订餐</el-tag>
            <el-tag v-else type="info" size="small" disable-transitions>不订餐</el-tag>
          </div>
          <div class="muted">{{ row.mealTypesText || '-' }}{{ row.feedWay ? ' · ' + row.feedWay : '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="目标量" width="150">
        <template #default="{ row }">
          <div class="muted">热量 {{ row.calorieTarget ?? '-' }} kcal</div>
          <div class="muted">蛋白 {{ row.proteinTarget ?? '-' }} g · 液 {{ row.fluidTarget ?? '-' }} ml</div>
        </template>
      </el-table-column>
      <el-table-column label="来源" width="120">
        <template #default="{ row }">
          <span>{{ row.sourceText || '-' }}</span>
          <div class="muted">{{ row.orderNo || row.dietNo || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="起止" width="165">
        <template #default="{ row }">
          <div>{{ row.startTime || '-' }}</div>
          <div class="muted">{{ row.stopTime ? '止 ' + row.stopTime : '长期' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="方案状态" width="95">
        <template #default="{ row }">
          <el-tag :type="PLAN_STATUS_TAG[row.planStatus]" disable-transitions>{{ row.planStatusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接收状态" width="130">
        <template #default="{ row }">
          <el-tag :type="CONFIRM_STATUS_TAG[row.confirmStatus]" disable-transitions data-testid="cell-confirm-status">
            {{ row.confirmStatusText }}
          </el-tag>
          <div class="muted">{{ row.confirmStatus === 1 ? (row.confirmerName || '') : (row.rejectReason || '') }}</div>
        </template>
      </el-table-column>
      <el-table-column label="今日订餐" width="85" align="right">
        <template #default="{ row }">
          <span data-testid="cell-today-meal">{{ row.todayMealCount ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="295" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click.stop="openDetail(row)">详情</el-button>
          <el-button
            v-if="row.confirmStatus === 0"
            v-perm="'ipd:diet:confirm'"
            link
            type="success"
            data-testid="btn-accept"
            @click.stop="confirmOne(row)">接收</el-button>
          <el-button v-perm="'ipd:diet:edit'" link type="primary" data-testid="btn-edit-plan" @click.stop="openForm(row)">修改</el-button>
          <el-button
            v-if="row.planStatus === 1"
            v-perm="'ipd:diet:edit'"
            link
            type="warning"
            data-testid="btn-stop-plan"
            @click.stop="openStop(row)">停餐</el-button>
          <el-button
            v-if="Number(row.needsMeal) === 1 && row.planStatus === 1"
            v-perm="'ipd:meal:generate'"
            link
            type="primary"
            data-testid="btn-generate-today"
            @click.stop="generateTodayMeal(row)">补今日餐</el-button>
          <el-button v-if="Number(row.needsMeal) === 1" link type="info" data-testid="btn-meal-of-plan" @click.stop="goMeal(row)">订餐</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- ⚠ layout 含 sizes 时 page-size 必须 v-model + @size-change，否则 EP 2.14 把整条分页渲染成 null -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          @current-change="loadList"
          @size-change="onSizeChange" />
      </div>
    </el-card>

    <!-- 手工登记 / 修改方案 -->
    <DietPlanEdit v-model="formVisible" :row="formRow" @saved="onSaved" />

    <!-- 批量退回 -->
    <DietPlanReject v-model="rejectVisible" :ids="selection.map((r) => r.id)" @saved="onRejectSaved" />

    <!-- 停餐 -->
    <DietPlanStop v-model="stopVisible" :row="stopRow" @saved="onSaved" />

    <!-- 详情（只读） -->
    <DietPlanDetail v-model="detailVisible" :row="detail" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  confirmDietPlan,
  generateMealOrder,
  getDietWardSelectList,
  listDietPlanPage,
} from '@/api/diet'
import { getNutritionOverview } from '@/api/nutrition'
import {
  CONFIRM_STATUS_TAG,
  CONFIRM_STATUS_TEXT,
  DIET_CATEGORY_TEXT,
  DIET_ROUTE_TEXT,
  PLAN_SOURCE_TEXT,
  PLAN_STATUS_TAG,
  PLAN_STATUS_TEXT,
} from '@/lib/nutrition'
import DietPlanEdit from './DietPlanEdit.vue'
import DietPlanReject from './DietPlanReject.vue'
import DietPlanStop from './DietPlanStop.vue'
import DietPlanDetail from './DietPlanDetail.vue'

/** 医嘱派生时文本解析不出饮食类型会落这个占位档，必须先改成真实饮食才允许接收 */
const TO_DETERMINE_CODE = 'TO_DETERMINE'

const route = useRoute()
const router = useRouter()

const overview = ref({})
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])
const selection = ref([])

const query = reactive({
  admissionId: null,
  wardId: null,
  source: null,
  dietCategory: null,
  route: null,
  planStatus: null,
  confirmStatus: null,
  keyword: '',
  beginDate: null,
  endDate: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

async function loadOverview() {
  try {
    const res = await getNutritionOverview()
    overview.value = res?.data || {}
  } catch (e) {
    overview.value = {}
  }
}

async function loadList() {
  loading.value = true
  try {
    query.beginDate = dateRange.value?.[0] || null
    query.endDate = dateRange.value?.[1] || null
    const res = await listDietPlanPage({ ...query })
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false
  }
}

function onQuery() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  Object.assign(query, {
    admissionId: null,
    wardId: null,
    source: null,
    dietCategory: null,
    route: null,
    planStatus: null,
    confirmStatus: null,
    keyword: '',
    beginDate: null,
    endDate: null,
    pageNum: 1,
  })
  dateRange.value = []
  loadList()
}

function onSizeChange() {
  query.pageNum = 1
  loadList()
}

// ---------------- 下拉数据 ----------------
const wards = ref([])

async function loadBaseData() {
  try {
    const res = await getDietWardSelectList()
    wards.value = res?.data || []
  } catch (e) {
    wards.value = []
  }
}

// ---------------- 接收 ----------------
async function doConfirm() {
  if (!selection.value.length) { ElMessage.warning('请先勾选膳食方案'); return }
  saving.value = true
  try {
    const res = await confirmDietPlan({
      ids: selection.value.map((r) => r.id),
      accept: true,
      rejectReason: null,
    })
    ElMessage.success(`已接收 ${res?.data ?? 0} 条膳食方案`)
    selection.value = []
    loadList()
    loadOverview()
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false
  }
}

function confirmOne(row) {
  selection.value = [row]
  doConfirm()
}

const saving = ref(false)

// ---------------- 登记 / 修改（子组件 DietPlanEdit） ----------------
const formVisible = ref(false)
const formRow = ref(null)

function openForm(row) {
  formRow.value = row || null
  formVisible.value = true
}

// ---------------- 批量退回（子组件 DietPlanReject） ----------------
const rejectVisible = ref(false)

// ---------------- 停餐（子组件 DietPlanStop） ----------------
const stopVisible = ref(false)
const stopRow = ref(null)

function openStop(row) {
  stopRow.value = row
  stopVisible.value = true
}

// ---------------- 详情 / 跳转订餐 ----------------
const detailVisible = ref(false)
const detail = ref({})

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

function goMeal(row) {
  detailVisible.value = false
  router.push({ path: '/meal-order', query: { dietPlanId: row.id } })
}

// ---------------- 子组件保存后的刷新 ----------------
function onSaved() {
  loadList()
  loadOverview()
}

function onRejectSaved() {
  selection.value = []
  onSaved()
}

/** 接收完不等于有饭吃：批量生成是每天凌晨按病区跑的，白天新接的方案要单独补当天的餐 */
async function generateTodayMeal(row) {
  saving.value = true
  try {
    const res = await generateMealOrder({
      mealDate: todayStr(),
      admissionIds: [row.admissionId],
    })
    ElMessage.success(res?.data?.message || '已补生成')
    loadList()
    loadOverview()
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false
  }
}

const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** 从筛查页带 admissionId 跳过来时，直接定位到这个人 */
onMounted(() => {
  if (route.query.admissionId) {
    query.admissionId = String(route.query.admissionId)
  }
  loadOverview()
  loadList()
  loadBaseData()
})
</script>

<style scoped>
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
.muted {
  color: #909399;
  font-size: 12px;
}
.to-determine {
  color: #f56c6c;
  font-weight: 600;
}
.target-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
