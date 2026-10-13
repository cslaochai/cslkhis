<template>
  <div data-testid="nutrition-screen-view">
    <el-card class="stat-card" shadow="never">
      <div class="stat-items">
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-in-hospital">{{ overview.inHospitalCount ?? '-' }}</div>
          <div class="stat-label">在院患者数</div>
          <div class="stat-sub">其中已筛 {{ overview.inHospitalScreenedCount ?? 0 }} 人</div>
        </div>
        <div class="stat-item">
          <div :class="{ 'stat-value-danger': (overview.missedScreenCount ?? 0) > 0 }" class="stat-value"
               data-testid="ov-missed">
            {{ overview.missedScreenCount ?? '-' }}
          </div>
          <div class="stat-label">在院未筛人数（漏筛提醒）</div>
          <div class="stat-sub">筛查率考核的就是这一档</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-risk">{{ overview.inHospitalRiskCount ?? '-' }}</div>
          <div class="stat-label">在院有营养风险人数</div>
          <div class="stat-sub">最新一次 NRS2002 总分≥3</div>
        </div>
        <div class="stat-item">
          <div :class="{ 'stat-value-warn': (overview.reScreenDueCount ?? 0) > 0 }" class="stat-value"
               data-testid="ov-due">
            {{ overview.reScreenDueCount ?? '-' }}
          </div>
          <div class="stat-label">到期未复筛人数</div>
          <div class="stat-sub">下次筛查日期 ≤ 今天</div>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="在院状态">
            <div data-testid="filter-admit-status">
              <el-select v-model="query.admitStatus" placeholder="在院状态" style="width: 120px">
                <el-option :value="1" label="在院"/>
                <el-option :value="0" label="已出院"/>
                <!-- 'ALL' 而不是 null：el-option 的 value 是必填且类型不含 null，绑 null 会报 Invalid prop -->
                <el-option label="全部" value="ALL"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="量表">
            <div data-testid="filter-screen-type">
              <el-select v-model="query.screenType" clearable placeholder="量表" style="width: 190px">
                <el-option v-for="(t, code) in SCREEN_TYPE_TEXT" :key="code" :label="t" :value="Number(code)"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="判定">
            <div data-testid="filter-risk">
              <el-select v-model="query.riskFlag" clearable placeholder="判定" style="width: 130px">
                <el-option :value="0" label="无营养风险"/>
                <el-option :value="1" label="有营养风险"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="只看到期复筛">
            <div data-testid="filter-due">
              <el-select v-model="query.dueOnly" clearable placeholder="只看到期复筛" style="width: 140px">
                <el-option :value="1" label="是"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
                v-model="dateRange"
                data-testid="filter-date"
                end-placeholder="筛查结束日"
                range-separator="至"
                start-placeholder="筛查开始日"
                style="width: 240px"
                type="daterange"
                value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable data-testid="filter-keyword" placeholder="姓名 / 患者编号 / 住院号"
                      style="width: 210px"/>
          </el-form-item>
          <el-form-item>
            <el-button data-testid="btn-query" type="primary" @click="onQuery">查询</el-button>
            <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：登记筛查统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:nutrition:screenEdit'" data-testid="btn-add-screen" type="primary"
                     @click="openForm()">登记筛查
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="screen-table" stripe>
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">
            <div>{{ row.patientName || '-' }}</div>
            <div class="muted">{{ row.patientNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="住院号" prop="admissionNo" show-overflow-tooltip width="150"/>
        <el-table-column label="科室 / 病区" min-width="160">
          <template #default="{ row }">
            <div>{{ row.deptName || '-' }}</div>
            <div class="muted">{{ row.wardName || '' }}{{ row.bedNo ? ' · ' + row.bedNo + '床' : '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="量表" width="180">
          <template #default="{ row }">{{ row.screenTypeText }}</template>
        </el-table-column>
        <el-table-column label="分项" width="150">
          <template #default="{ row }">
            <span class="muted">受损 {{ row.impairScore ?? '-' }} · 严重 {{
                row.severityScore ?? '-'
              }} · 年龄 {{ row.ageScore ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="总分" width="80">
          <template #default="{ row }">
            <span data-testid="cell-total">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="判定" width="120">
          <template #default="{ row }">
            <el-tag :type="RISK_FLAG_TAG[row.riskFlag]" data-testid="cell-risk" disable-transitions>{{
                row.riskFlagText
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="right" label="BMI" width="80">
          <template #default="{ row }">{{ row.bmi ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="筛查时机" width="130">
          <template #default="{ row }">{{ row.screenSourceText }}</template>
        </el-table-column>
        <el-table-column label="筛查时间" prop="screenTime" width="165"/>
        <el-table-column label="下次筛查" width="130">
          <template #default="{ row }">
          <span :class="{ 'stat-value-warn': row.reScreenDue === 1 }" data-testid="cell-next-screen">
            {{ row.nextScreenDate || '—' }}
          </span>
          </template>
        </el-table-column>
        <el-table-column label="筛查人" prop="screenerName" width="100"/>
        <el-table-column fixed="right" label="操作" width="210">
          <template #default="{ row }">
            <el-button
                link
                type="primary"
                @click.stop="openDetail(row)">详情
            </el-button>
            <el-button
                v-perm="'ipd:nutrition:screenEdit'"
                data-testid="btn-edit-screen"
                link
                type="primary"
                @click.stop="openForm(row)">修改
            </el-button>
            <el-button
                v-perm="'ipd:nutrition:consultApply'"
                data-testid="btn-apply-consult"
                link
                type="primary"
                @click.stop="openConsult(row)">发起营养会诊
            </el-button>
            <el-button
                v-perm="'ipd:nutrition:screenDelete'"
                data-testid="btn-delete-screen"
                link
                type="danger"
                @click.stop="doDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="onSizeChange"/>
      </div>
    </el-card>

    <!-- 登记 / 修改筛查 -->
    <NutritionScreenEdit v-model="formVisible" :row="formRow" @saved="onSaved" />

    <!-- 只读详情：行点击打开，编辑走列表按钮 -->
    <NutritionScreenDetail v-model="detailVisible" :row="detailRow" />

    <!-- 发起营养会诊 -->
    <NutritionScreenConsult v-model="consultVisible" :row="consultRow" />
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  deleteNutritionScreen,
  getNutritionOverview,
  listNutritionScreenPage,
} from '@/api/nutrition'
import {
  NRS_RISK_CUTOFF,
  RISK_FLAG_TAG,
  SCREEN_TYPE_TEXT,
} from '@/lib/nutrition'
import NutritionScreenEdit from './NutritionScreenEdit.vue'
import NutritionScreenDetail from './NutritionScreenDetail.vue'
import NutritionScreenConsult from './NutritionScreenConsult.vue'

const overview = ref({})
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])

const query = reactive({
  admitStatus: 1,
  screenType: null,
  riskFlag: null,
  dueOnly: null,
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
    const res = await listNutritionScreenPage({
      ...query,
      admitStatus: query.admitStatus === 'ALL' ? null : query.admitStatus,
    })
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
  query.admitStatus = 1
  query.screenType = null
  query.riskFlag = null
  query.dueOnly = null
  query.keyword = ''
  dateRange.value = []
  query.pageNum = 1
  loadList()
}

function onSizeChange() {
  query.pageNum = 1
  loadList()
}

// ---------------- 登记 / 修改（子组件 NutritionScreenEdit） ----------------
const formVisible = ref(false)
const formRow = ref(null)

function openForm(row) {
  formRow.value = row || null
  formVisible.value = true
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除 ${row.patientName || ''} 的筛查记录 ${row.screenNo || ''}？`, '删除确认', {
      type: 'warning',
    })
  } catch (e) {
    return
  }
  try {
    await deleteNutritionScreen(row.id)
    ElMessage.success('已删除')
    loadList()
    loadOverview()
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

// ---------------- 详情（子组件 NutritionScreenDetail） ----------------
const detailVisible = ref(false)
const detailRow = ref({})

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

// ---------------- 发起营养会诊（子组件 NutritionScreenConsult） ----------------
const consultVisible = ref(false)
const consultRow = ref({})

/** 未达阈值的筛出不给从这里发起，这是入口把关，不是表单逻辑 */
function openConsult(row) {
  if (Number(row.riskFlag) !== 1 && Number(row.totalScore) < NRS_RISK_CUTOFF) {
    ElMessage.warning('该次筛查未达营养风险阈值，一般不需要会诊；确需会诊请到「营养会诊」页发起')
    return
  }
  consultRow.value = row
  consultVisible.value = true
}

// ---------------- 子组件保存后的刷新 ----------------
function onSaved() {
  loadList()
  loadOverview()
}

onMounted(() => {
  loadOverview()
  loadList()
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
