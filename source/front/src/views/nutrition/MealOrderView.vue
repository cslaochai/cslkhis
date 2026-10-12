<template>
  <div data-testid="meal-order-view">
    <el-card shadow="never" class="stat-card">
      <div class="stat-items">
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-meal-count">{{ overview.todayMealCount ?? '-' }}</div>
          <div class="stat-label">今日订餐总条数</div>
          <div class="stat-sub">含已取消，签收率另算</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-danger': (overview.todayMealPendingCount ?? 0) > 0 }"
               data-testid="ov-meal-pending">
            {{ overview.todayMealPendingCount ?? '-' }}
          </div>
          <div class="stat-label">今日未签收到位</div>
          <div class="stat-sub">已签收 {{ overview.todayMealSignedCount ?? 0 }} 条</div>
        </div>
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'stat-value-warn': Number(overview.todayMealSignRate) < STATS_TARGETS.mealSignRate }"
               data-testid="ov-meal-rate">
            {{ overview.todayMealSignRate ?? '0.00' }}%
          </div>
          <div class="stat-label">今日签收率（目标 ≥{{ STATS_TARGETS.mealSignRate }}%）</div>
          <div class="stat-sub">按「已签收 ÷ 应送」现算，不落库</div>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="就餐日期">
            <div data-testid="filter-meal-date">
              <el-date-picker v-model="query.mealDate" type="date" value-format="YYYY-MM-DD" placeholder="就餐日期"
                              :clearable="false" style="width: 150px"/>
            </div>
          </el-form-item>
          <el-form-item label="病区">
            <div data-testid="filter-ward">
              <el-select v-model="query.wardId" placeholder="病区" clearable style="width: 150px">
                <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="餐次">
            <div data-testid="filter-meal-type">
              <el-select v-model="query.mealType" placeholder="餐次" clearable style="width: 110px">
                <el-option v-for="(t, code) in MEAL_TYPE_TEXT" :key="code" :label="t" :value="Number(code)"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="配餐状态">
            <div data-testid="filter-status">
              <el-select v-model="query.deliverStatus" placeholder="配餐状态" clearable style="width: 130px">
                <el-option v-for="(t, code) in MEAL_STATUS_TEXT" :key="code" :label="t" :value="Number(code)"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="姓名 / 患者编号 / 住院号 / 订餐单号" clearable
                      style="width: 240px" data-testid="filter-keyword"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-query" @click="onQuery">查询</el-button>
            <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：批量推进/退订与生成餐单统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:meal:status'" :disabled="!selection.length" data-testid="btn-batch-advance"
                     @click="openAdvance(selection, null)">
            批量推进（{{ selection.length }}）
          </el-button>
          <el-button v-perm="'ipd:meal:status'" :disabled="!selection.length" type="danger" plain
                     data-testid="btn-batch-cancel" @click="openCancelBatch">
            批量退订（{{ selection.length }}）
          </el-button>
          <el-button v-perm="'ipd:meal:generate'" type="primary" data-testid="btn-generate" @click="genVisible = true">
            生成餐单
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe :max-height="tableMaxHeight" v-loading="loading" data-testid="meal-table"
                @selection-change="selection = $event" @row-click="onRowClick">
        <el-table-column type="selection" width="42"/>
        <el-table-column label="患者" min-width="130">
          <template #default="{ row }">
            <div>{{ row.patientName || '-' }}</div>
            <div class="muted">{{ row.admissionNo || row.patientNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="病区 / 床" min-width="130">
          <template #default="{ row }">
            <div>{{ row.wardName || '-' }}</div>
            <div class="muted">{{ row.bedNo ? row.bedNo + '床' : '' }}{{
                row.deptName ? ' · ' + row.deptName : ''
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="饮食" min-width="150">
          <template #default="{ row }">
            <div>{{ row.dietName || '-' }}</div>
            <div class="muted">{{ row.dietCode || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="日期 / 餐次" width="130">
          <template #default="{ row }">
            <div>{{ row.mealDate || '-' }}</div>
            <div data-testid="cell-meal-type">{{ row.mealTypeText || '-' }} × {{ row.quantity ?? 1 }}</div>
          </template>
        </el-table-column>
        <el-table-column label="配餐内容" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.dishContent || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="MEAL_STATUS_TAG[row.deliverStatus]" disable-transitions data-testid="cell-meal-status">
              {{ row.deliverStatusText }}
            </el-tag>
            <div class="muted">{{ row.nextStatusText ? '下一步 ' + row.nextStatusText : '终态' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="配送 / 签收" width="165">
          <template #default="{ row }">
            <div class="muted">{{ row.deliverTime || '—' }}</div>
            <div class="muted">{{ row.signTime || '—' }}{{ row.signBy ? ' · ' + row.signBy : '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <div>{{ row.sourceText || '-' }}</div>
            <div class="muted">{{ row.mealNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="Number(row.canAdvance) === 1"
                v-perm="'ipd:meal:status'"
                link
                type="primary"
                data-testid="btn-advance"
                @click.stop="openAdvance([row], null)">{{ row.nextStatusText || '推进' }}
            </el-button>
            <el-button
                v-if="Number(row.canCancel) === 1"
                v-perm="'ipd:meal:status'"
                link
                type="danger"
                data-testid="btn-cancel-meal"
                @click.stop="openAdvance([row], 4)">退订
            </el-button>
            <el-button
                v-if="row.deliverStatus < 2"
                v-perm="'ipd:meal:status'"
                link
                type="info"
                data-testid="btn-delete-meal"
                @click.stop="doDelete(row)">删除
            </el-button>
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
            @size-change="onSizeChange"/>
      </div>
    </el-card>

    <!-- 生成餐单 -->
    <MealOrderGenerate v-model="genVisible" :wards="wards" :ward-id="query.wardId" @saved="onGenerated" />

    <!-- 状态推进 / 退订 -->
    <MealOrderAdvance v-model="advanceVisible" :rows="advanceRows" :target="advanceTarget" @saved="onAdvanceSaved" />

    <!-- 详情（只读） -->
    <MealOrderDetail v-model="detailVisible" :row="detail" />
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  deleteMealOrder,
  getDietWardSelectList,
  listMealOrderPage,
} from '@/api/diet'
import {getNutritionOverview} from '@/api/nutrition'
import {MEAL_STATUS_TAG, MEAL_STATUS_TEXT, MEAL_TYPE_TEXT, STATS_TARGETS} from '@/lib/nutrition'
import MealOrderGenerate from './MealOrderGenerate.vue'
import MealOrderAdvance from './MealOrderAdvance.vue'
import MealOrderDetail from './MealOrderDetail.vue'

const route = useRoute()

const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const overview = ref({})
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const selection = ref([])

const query = reactive({
  mealDate: todayStr(),
  wardId: null,
  mealType: null,
  deliverStatus: null,
  dietPlanId: null,
  keyword: '',
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
    const res = await listMealOrderPage({...query})
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function onQuery() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  query.mealDate = todayStr()
  query.wardId = null
  query.mealType = null
  query.deliverStatus = null
  query.dietPlanId = null
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

function onSizeChange() {
  query.pageNum = 1
  loadList()
}

const wards = ref([])

async function loadWards() {
  try {
    const res = await getDietWardSelectList()
    wards.value = res?.data || []
  } catch (e) {
    wards.value = []
  }
}

// ---------------- 生成餐单（子组件 MealOrderGenerate） ----------------
const genVisible = ref(false)

// ---------------- 状态机推进（子组件 MealOrderAdvance） ----------------
const advanceVisible = ref(false)
const advanceRows = ref([])
const advanceTarget = ref(null)

/** 单条推进取后端算好的下一步；批量要求勾选的餐同一状态，否则服务端按状态机整批拒 */
function openAdvance(list, forcedTarget) {
  if (!list.length) {
    ElMessage.warning('请先勾选订餐明细');
    return
  }
  const statuses = [...new Set(list.map((r) => r.deliverStatus))]
  if (statuses.length > 1 && list.length > 1) {
    ElMessage.warning('批量推进只能勾选同一状态的餐（现在是 ' + statuses.map((s) => MEAL_STATUS_TEXT[s]).join('、') + '）')
    return
  }
  if (forcedTarget === 4) {
    if (Number(list[0].canCancel) !== 1) {
      ElMessage.warning('已签收或已取消的餐不能再退订');
      return
    }
    advanceTarget.value = 4
  } else {
    if (Number(list[0].canAdvance) !== 1) {
      ElMessage.warning('这批餐已经是终态，不能再推进');
      return
    }
    advanceTarget.value = statuses[0] + 1
  }
  advanceRows.value = list
  advanceVisible.value = true
}

function openCancelBatch() {
  openAdvance(selection.value, 4)
}

/** 生成成功：把列表日期切到刚生成的那天，看到的就是要送的餐 */
function onGenerated(mealDate) {
  query.mealDate = mealDate
  loadList()
  loadOverview()
}

function onAdvanceSaved() {
  selection.value = []
  loadList()
  loadOverview()
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除订餐 ${row.mealNo || ''}（${row.patientName || ''} ${row.mealTypeText || ''}）？`, '删除确认', {
      type: 'warning',
    })
  } catch (e) {
    return
  }
  try {
    await deleteMealOrder(row.id)
    ElMessage.success('已删除')
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// ---------------- 详情 ----------------
const detailVisible = ref(false)
const detail = ref({})

/** 勾选列的点击也会冒泡成 row-click */
function onRowClick(row, column) {
  if (column && column.type === 'selection') return
  detail.value = row
  detailVisible.value = true
}

onMounted(() => {
  if (route.query.dietPlanId) {
    query.dietPlanId = String(route.query.dietPlanId)
    query.mealDate = null
  }
  loadOverview()
  loadList()
  loadWards()
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
</style>
