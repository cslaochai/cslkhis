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
          <el-button v-perm="'ipd:diet:confirm'" type="success" :disabled="!selection.length" data-testid="btn-batch-accept" @click="doConfirm(true)">
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
      <el-table :data="rows" stripe :max-height="tableMaxHeight" v-loading="loading" data-testid="plan-table" @selection-change="selection = $event" @row-click="onRowClick">
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
      <el-table-column label="操作" width="255" fixed="right">
        <template #default="{ row }">
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
    <el-dialog v-model="formVisible" :title="form.id ? '修改膳食方案' : '手工登记膳食方案'" width="620px" data-testid="plan-dialog">
      <el-form label-width="120px">
        <el-form-item label="住院患者" required>
          <div data-testid="sel-admission" style="width: 100%">
            <el-select v-model="form.admissionId" filterable :disabled="!!form.id" placeholder="选择在院患者" style="width: 100%">
              <el-option
                v-for="a in admissions"
                :key="a.admissionId"
                :label="admissionLabel(a)"
                :value="a.admissionId" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="饮食类型" required>
          <div data-testid="sel-diet-code" style="width: 100%">
            <el-select v-model="form.dietCode" filterable placeholder="从饮食目录中选择" style="width: 100%" @change="onDietChange">
              <el-option
                v-for="d in dietOptions"
                :key="d.code"
                :label="`${d.name}（${d.categoryText} · ${d.routeText}）`"
                :value="d.code" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="目录口径">
          <div class="muted" data-testid="diet-hint">
            {{ currentDiet ? `${currentDiet.routeText} · ${currentDiet.needsMeal ? '进食堂订餐' : '不进食堂订餐'} · 默认 ${currentDiet.mealTypesText || '不订餐'}` : '选择饮食类型后由服务端带出类别、途径与默认餐次' }}
          </div>
        </el-form-item>
        <el-form-item label="饮食名称">
          <el-input v-model="form.dietName" maxlength="100" placeholder="留空取目录名，可写个体化名称" data-testid="input-diet-name" />
        </el-form-item>
        <el-form-item v-if="currentDiet && Number(currentDiet.route) !== 1" label="管饲/输注方式">
          <el-input v-model="form.feedWay" maxlength="100" placeholder="如：鼻胃管 间歇滴注 40ml/h" data-testid="input-feed-way" />
        </el-form-item>
        <el-form-item label="供应餐次">
          <el-checkbox-group v-model="mealTypeList" data-testid="chk-meal-types">
            <el-checkbox v-for="(t, code) in MEAL_TYPE_TEXT" :key="code" :value="Number(code)">{{ t }}</el-checkbox>
          </el-checkbox-group>
          <div class="muted">为空取目录默认餐次；勾了但途径不是口服，食堂侧不会生成餐单。</div>
        </el-form-item>
        <el-form-item label="目标量">
          <div class="target-row">
            <el-input-number v-model="form.calorieTarget" :min="0" :max="6000" :step="50" controls-position="right" data-testid="input-calorie" />
            <span class="muted">kcal</span>
            <el-input-number v-model="form.proteinTarget" :min="0" :max="300" :step="5" controls-position="right" data-testid="input-protein" />
            <span class="muted">g</span>
            <el-input-number v-model="form.fluidTarget" :min="0" :max="5000" :step="100" controls-position="right" data-testid="input-fluid" />
            <span class="muted">ml</span>
          </div>
          <div class="muted">同一种饮食按体重分级是临床常态，这里可以覆盖目录默认值。</div>
        </el-form-item>
        <el-form-item label="开始时间">
          <div data-testid="sel-start-time" style="width: 100%">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空取当前时间" style="width: 100%" />
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" data-testid="input-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-save-plan" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量退回 -->
    <el-dialog v-model="rejectVisible" title="批量退回膳食方案" width="480px" data-testid="reject-dialog">
      <el-form label-width="90px">
        <el-form-item label="退回原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" maxlength="500" placeholder="如：饮食类型与病情不符，需重评 NRS2002 后改嘱" data-testid="input-reject-reason" />
        </el-form-item>
        <div class="form-tip">退回去的是「让开嘱医师改医嘱」，方案会停在「已退回」，食堂不会为它配餐；改完由医师重新校对派生。</div>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="saving" data-testid="btn-do-reject" @click="doConfirm(false)">确认退回 {{ selection.length }} 条</el-button>
      </template>
    </el-dialog>

    <!-- 停餐 -->
    <el-dialog v-model="stopVisible" title="停止膳食方案" width="480px" data-testid="stop-dialog">
      <el-form label-width="90px">
        <el-form-item label="方案">
          <el-input :value="`${stopRow.dietName || ''}（${stopRow.dietNo || ''}）`" disabled />
        </el-form-item>
        <el-form-item label="停止时间">
          <div data-testid="sel-stop-time" style="width: 100%">
            <el-date-picker v-model="stopForm.stopTime" type="datetime" value-format="YYYY-MM-dd HH:mm:ss" placeholder="留空取当前时间" style="width: 100%" />
          </div>
        </el-form-item>
        <el-form-item label="停止原因" required>
          <el-input v-model="stopForm.reason" type="textarea" :rows="3" maxlength="500" placeholder="如：患者拒食 / 明日造影需禁食 / 转出病区" data-testid="input-stop-reason" />
        </el-form-item>
        <div class="form-tip">医嘱没停但临床上不吃了才用这里。停方案会把该时间之后还没配送的餐一并作废，已经送出去的不追。</div>
      </el-form>
      <template #footer>
        <el-button @click="stopVisible = false">取消</el-button>
        <el-button type="warning" :loading="saving" data-testid="btn-do-stop" @click="doStop">确认停餐</el-button>
      </template>
    </el-dialog>

    <!-- 详情（只读） -->
    <el-dialog v-model="detailVisible" title="膳食方案明细" width="640px" data-testid="plan-detail-dialog">
      <el-form :disabled="true" label-width="110px">
        <el-form-item label="患者">
          <el-input :value="`${detail.patientName || ''} · ${detail.admissionNo || ''}（${detail.wardName || detail.deptName || ''}${detail.bedNo ? ' ' + detail.bedNo + '床' : ''}）`" />
        </el-form-item>
        <el-form-item label="方案号 / 医嘱号">
          <el-input :value="`${detail.dietNo || '—'} / ${detail.orderNo || '—'}`" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input :value="`${detail.sourceText || '—'} · ${detail.dietCode || ''} ${detail.dietName || ''}`" />
        </el-form-item>
        <el-form-item label="类别 / 途径">
          <el-input :value="`${detail.dietCategoryText || '—'} · ${detail.routeText || '—'} · ${Number(detail.needsMeal) === 1 ? '食堂订餐' : '不订餐'}`" />
        </el-form-item>
        <el-form-item label="供应餐次">
          <el-input :value="detail.mealTypesText || '—'" />
        </el-form-item>
        <el-form-item label="管饲/输注">
          <el-input :value="detail.feedWay || '—'" />
        </el-form-item>
        <el-form-item label="目标量">
          <el-input :value="`热量 ${detail.calorieTarget ?? '—'} kcal · 蛋白 ${detail.proteinTarget ?? '—'} g · 液体 ${detail.fluidTarget ?? '—'} ml`" />
        </el-form-item>
        <el-form-item label="起止时间">
          <el-input :value="`${detail.startTime || '—'} → ${detail.stopTime || '长期'}`" />
        </el-form-item>
        <el-form-item label="接收">
          <el-input :value="`${detail.confirmStatusText || '—'}${detail.confirmerName ? ' · ' + detail.confirmerName : ''}${detail.confirmTime ? ' · ' + detail.confirmTime : ''}`" />
        </el-form-item>
        <el-form-item label="退回原因">
          <el-input :value="detail.rejectReason || '—'" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input :value="detail.remark || '—'" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" data-testid="btn-detail-meal" @click="goMeal(detail)">查看订餐</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import { getInpatientListPage } from '@/api/inpatient'
import {
  confirmDietPlan,
  generateMealOrder,
  getDietTypeOptions,
  getDietWardSelectList,
  listDietPlanPage,
  stopDietPlan,
  upsertDietPlan,
} from '@/api/diet'
import { getNutritionOverview } from '@/api/nutrition'
import {
  CONFIRM_STATUS_TAG,
  CONFIRM_STATUS_TEXT,
  DIET_CATEGORY_TEXT,
  DIET_ROUTE_TEXT,
  MEAL_TYPE_TEXT,
  PLAN_SOURCE_TEXT,
  PLAN_STATUS_TAG,
  PLAN_STATUS_TEXT,
} from '@/lib/nutrition'

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
const admissions = ref([])
const dietOptions = ref([])

const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

async function loadBaseData() {
  try {
    const res = await getDietWardSelectList()
    wards.value = res?.data || []
  } catch (e) {
    wards.value = []
  }
  try {
    const res = await getDietTypeOptions()
    dietOptions.value = res?.data || []
  } catch (e) {
    dietOptions.value = []
  }
  try {
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
    admissions.value = res?.data?.records || []
  } catch (e) {
    admissions.value = []
  }
}

// ---------------- 接收 / 退回 ----------------
async function doConfirm(accept) {
  if (!selection.value.length) { ElMessage.warning('请先勾选膳食方案'); return }
  if (!accept && !rejectReason.value.trim()) { ElMessage.warning('退回必须填写原因'); return }
  saving.value = true
  try {
    const res = await confirmDietPlan({
      ids: selection.value.map((r) => r.id),
      accept,
      rejectReason: accept ? null : rejectReason.value.trim(),
    })
    ElMessage.success(accept ? `已接收 ${res?.data ?? 0} 条膳食方案` : `已退回 ${res?.data ?? 0} 条`)
    rejectVisible.value = false
    rejectReason.value = ''
    selection.value = []
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

function confirmOne(row) {
  selection.value = [row]
  doConfirm(true)
}

const rejectVisible = ref(false)
const rejectReason = ref('')

// ---------------- 登记 / 修改 ----------------
const formVisible = ref(false)
const saving = ref(false)
const emptyForm = () => ({
  id: null,
  admissionId: null,
  dietCode: null,
  dietName: '',
  feedWay: '',
  calorieTarget: null,
  proteinTarget: null,
  fluidTarget: null,
  startTime: null,
  remark: '',
})
const form = reactive(emptyForm())
const mealTypeList = ref([])

const currentDiet = computed(() => dietOptions.value.find((d) => d.code === form.dietCode) || null)

/** 选完饮食类型把目录建议值填进表单（落库仍以服务端按 code 重算为准） */
function onDietChange() {
  const d = currentDiet.value
  if (!d) return
  if (!form.dietName) form.dietName = d.name
  form.calorieTarget = d.calorieTarget ?? null
  form.proteinTarget = d.proteinTarget ?? null
  mealTypeList.value = (d.mealTypes || '').split(',').filter(Boolean).map(Number)
}

function openForm(row) {
  Object.assign(form, emptyForm())
  mealTypeList.value = []
  if (row) {
    Object.assign(form, {
      id: row.id,
      admissionId: row.admissionId,
      dietCode: row.dietCode,
      dietName: row.dietName || '',
      feedWay: row.feedWay || '',
      calorieTarget: row.calorieTarget,
      proteinTarget: row.proteinTarget,
      fluidTarget: row.fluidTarget,
      remark: row.remark || '',
    })
    mealTypeList.value = (row.mealTypes || '').split(',').filter(Boolean).map(Number)
  }
  formVisible.value = true
}

async function submitForm() {
  if (!form.admissionId) { ElMessage.warning('请选择住院患者'); return }
  if (!form.dietCode) { ElMessage.warning('请选择饮食类型'); return }
  saving.value = true
  try {
    await upsertDietPlan({
      ...form,
      mealTypes: mealTypeList.value.length ? mealTypeList.value.join(',') : null,
    })
    ElMessage.success('膳食方案已保存')
    formVisible.value = false
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 停餐 ----------------
const stopVisible = ref(false)
const stopRow = ref({})
const stopForm = reactive({ stopTime: null, reason: '' })

function openStop(row) {
  stopRow.value = row
  stopForm.stopTime = null
  stopForm.reason = ''
  stopVisible.value = true
}

async function doStop() {
  if (!stopForm.reason.trim()) { ElMessage.warning('停止原因不能为空（停餐是一个临床决定）'); return }
  saving.value = true
  try {
    await stopDietPlan({ id: stopRow.value.id, stopTime: stopForm.stopTime, reason: stopForm.reason.trim() })
    ElMessage.success('已停餐')
    stopVisible.value = false
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '停餐失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 详情 / 跳转订餐 ----------------
const detailVisible = ref(false)
const detail = ref({})

/** 勾选列的点击也会冒泡成 row-click，勾一下弹一个详情是灾难 */
function onRowClick(row, column) {
  if (column && column.type === 'selection') return
  openDetail(row)
}

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

function goMeal(row) {
  detailVisible.value = false
  router.push({ path: '/meal-order', query: { dietPlanId: row.id } })
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
  } catch (e) {
    ElMessage.error(e?.message || '补生成失败')
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
