<template>
  <div data-testid="nutrition-consult-view">
    <el-card shadow="never" class="stat-card">
      <div class="stat-items">
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-danger': (overview.consultUnfinishedCount ?? 0) > 0 }"
               data-testid="ov-consult-unfinished">
            {{ overview.consultUnfinishedCount ?? '-' }}
          </div>
          <div class="stat-label">未完成营养会诊</div>
          <div class="stat-sub">待应答 + 已应答，全部要有人收尾</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-warn': (overview.consultOverdueCount ?? 0) > 0 }"
               data-testid="ov-consult-overdue">
            {{ overview.consultOverdueCount ?? '-' }}
          </div>
          <div class="stat-label">超时未应答</div>
          <div class="stat-sub">急会诊超 10 分钟 / 普通超 24 小时</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-risk">{{ overview.inHospitalRiskCount ?? '-' }}</div>
          <div class="stat-label">在院有营养风险人数</div>
          <div class="stat-sub">筛出来没会诊的，就是这里的活</div>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="会诊科室">
            <div data-testid="filter-to-dept">
              <el-select v-model="query.toDeptId" placeholder="会诊科室" clearable filterable style="width: 170px">
                <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="会诊状态">
            <div data-testid="filter-status">
              <el-select v-model="query.consultStatus" placeholder="会诊状态" clearable style="width: 130px">
                <el-option label="待应答" :value="0"/>
                <el-option label="已应答" :value="3"/>
                <el-option label="已完成" :value="1"/>
                <el-option label="已取消" :value="2"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="紧急度">
            <div data-testid="filter-urgent">
              <el-select v-model="query.isUrgent" placeholder="紧急度" clearable style="width: 120px">
                <el-option label="普通会诊" :value="0"/>
                <el-option label="急会诊" :value="1"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="只看未完成">
            <div data-testid="filter-unfinished">
              <el-select v-model="query.unfinishedOnly" placeholder="只看未完成" clearable style="width: 130px">
                <el-option label="是" :value="1"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="会诊号 / 患者姓名 / 患者号 / 理由" clearable
                      style="width: 240px" data-testid="filter-keyword"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-query" @click="onQuery">查询</el-button>
            <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：发起会诊统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:nutrition:consultApply'" type="primary" data-testid="btn-apply" @click="openForm()">
            发起营养会诊
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe :max-height="tableMaxHeight" v-loading="loading" data-testid="consult-table"
                @row-click="openDetail">
        <el-table-column label="患者" min-width="130">
          <template #default="{ row }">
            <div>{{ row.patientName || '-' }}</div>
            <div class="muted">{{ row.admissionNo || row.patientNo || '' }}{{
                row.bedNo ? ' · ' + row.bedNo + '床' : ''
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请 → 会诊科室" min-width="170">
          <template #default="{ row }">
            <div>{{ row.fromDeptName || '-' }} → {{ row.toDeptName || '-' }}</div>
            <div class="muted">{{ row.consultTypeText || '' }} · {{ row.consultCategoryText || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="紧急度" width="120">
          <template #default="{ row }">
            <el-tag :type="Number(row.isUrgent) === 1 ? 'danger' : 'info'" disable-transitions>
              {{ row.isUrgentText || (Number(row.isUrgent) === 1 ? '急会诊' : '普通') }}
            </el-tag>
            <div v-if="row.overdueText" class="overdue" data-testid="cell-overdue">{{ row.overdueText }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="会诊理由" min-width="200" show-overflow-tooltip/>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG[row.consultStatus]" disable-transitions data-testid="cell-consult-status">
              {{ row.consultStatusText }}
            </el-tag>
            <div class="muted">{{ row.consultationNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申请 / 应答" width="165">
          <template #default="{ row }">
            <div class="muted">{{ row.applyTime || '-' }}</div>
            <div class="muted">{{ row.acceptDoctorName ? row.acceptDoctorName + ' 接诊' : '尚未接诊' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="应答耗时" width="110" align="right">
          <template #default="{ row }">
            <span data-testid="cell-response">{{ responseText(row) }}</span>
            <div v-if="row.onTime === false" class="muted">未按时</div>
            <div v-else-if="row.onTime === true" class="muted">按时</div>
          </template>
        </el-table-column>
        <el-table-column label="结论 / 病历" min-width="160">
          <template #default="{ row }">
            <div class="muted">{{ row.conclusion || '—' }}</div>
            <div class="muted">{{ row.recordNo ? '病历 ' + row.recordNo : '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="row.canAccept"
                v-perm="'ipd:nutrition:consultEdit'"
                link
                type="primary"
                data-testid="btn-accept"
                @click.stop="doAccept(row)">接诊
            </el-button>
            <el-button
                v-if="row.canFinish"
                v-perm="'ipd:nutrition:consultEdit'"
                link
                type="success"
                data-testid="btn-finish"
                @click.stop="openFinish(row)">出结论
            </el-button>
            <el-button
                v-if="row.canCancel"
                v-perm="'ipd:nutrition:consultEdit'"
                link
                type="danger"
                data-testid="btn-cancel"
                @click.stop="doCancel(row)">取消
            </el-button>
            <el-button
                v-if="row.canEdit"
                v-perm="'ipd:nutrition:consultApply'"
                link
                type="info"
                data-testid="btn-edit-consult"
                @click.stop="openForm(row)">修改
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

    <!-- 发起 / 修改申请 -->
    <NutritionConsultEdit v-model="formVisible" :row="formRow" :depts="depts" @saved="onSaved" />

    <!-- 出结论 -->
    <NutritionConsultFinish v-model="finishVisible" :row="finishRow" @saved="onSaved" />

    <!-- 详情（只读） -->
    <NutritionConsultDetail v-model="detailVisible" :row="detailRow" />
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {getDepartmentSelectList} from '@/api/system'
import {
  acceptNutritionConsult,
  cancelNutritionConsult,
  getNutritionOverview,
  listNutritionConsultPage,
} from '@/api/nutrition'
import {responseText} from '@/lib/nutrition'
import NutritionConsultEdit from './NutritionConsultEdit.vue'
import NutritionConsultFinish from './NutritionConsultFinish.vue'
import NutritionConsultDetail from './NutritionConsultDetail.vue'

const STATUS_TAG = {0: 'warning', 3: 'primary', 1: 'success', 2: 'info'}

const overview = ref({})
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)

const query = reactive({
  toDeptId: null,
  consultStatus: null,
  consultType: null,
  isUrgent: null,
  unfinishedOnly: null,
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
    const res = await listNutritionConsultPage({...query})
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
    toDeptId: null,
    consultStatus: null,
    consultType: null,
    isUrgent: null,
    unfinishedOnly: null,
    keyword: '',
    pageNum: 1,
  })
  loadList()
}

function onSizeChange() {
  query.pageNum = 1
  loadList()
}

// ---------------- 基础数据 ----------------
// 会诊科室下拉：查询卡与发起/修改弹框共用，留在页面按 prop 下发
const depts = ref([])

async function loadBaseData() {
  try {
    const res = await getDepartmentSelectList({})
    depts.value = res?.data || []
  } catch (e) {
    depts.value = []
  }
}

// ---------------- 发起 / 修改（子组件 NutritionConsultEdit） ----------------
const formVisible = ref(false)
const formRow = ref(null)

function openForm(row) {
  formRow.value = row || null
  formVisible.value = true
}

// ---------------- 应答 ----------------
async function doAccept(row) {
  saving.value = true
  try {
    await acceptNutritionConsult({consultationId: row.consultationId})
    ElMessage.success('已接诊（接诊人 = 当前登录用户）')
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '接诊失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 出结论（子组件 NutritionConsultFinish） ----------------
const finishVisible = ref(false)
const finishRow = ref({})

function openFinish(row) {
  finishRow.value = row
  finishVisible.value = true
}

async function doCancel(row) {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt('取消原因（必填）', `取消会诊 ${row.consultationNo || ''}`, {
      inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空'),
    })
    reason = String(r?.value || '').trim()
  } catch (e) {
    return
  }
  try {
    await cancelNutritionConsult({consultationId: row.consultationId, cancelReason: reason})
    ElMessage.success('已取消')
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '取消失败')
  }
}

// ---------------- 详情（子组件 NutritionConsultDetail） ----------------
const detailVisible = ref(false)
const detailRow = ref({})

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

// ---------------- 子组件保存后的刷新 ----------------
function onSaved() {
  loadList()
  loadOverview()
}

onMounted(() => {
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

.overdue {
  color: #f56c6c;
  font-size: 12px;
}
</style>
