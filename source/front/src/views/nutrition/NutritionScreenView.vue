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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="screen-table" stripe
                @row-click="openDetail">
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
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
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
    <el-dialog v-model="formVisible" :title="form.id ? '修改筛查记录' : '登记营养风险筛查'" data-testid="screen-dialog"
               width="620px">
      <el-form label-width="130px">
        <el-form-item label="住院患者" required>
          <div data-testid="sel-admission" style="width: 100%">
            <el-select v-model="form.admissionId" :disabled="!!form.id" filterable placeholder="选择在院患者"
                       style="width: 100%">
              <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)"
                         :value="a.admissionId"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="量表" required>
          <div data-testid="sel-screen-type">
            <el-select v-model="form.screenType" style="width: 240px" @change="onTypeChange">
              <el-option v-for="(t, code) in SCREEN_TYPE_TEXT" :key="code" :label="t" :value="Number(code)"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="筛查时机" required>
          <div data-testid="sel-screen-source">
            <el-select v-model="form.screenSource" style="width: 200px">
              <el-option v-for="(t, code) in SCREEN_SOURCE_TEXT" :key="code" :label="t" :value="Number(code)"/>
            </el-select>
          </div>
        </el-form-item>

        <template v-if="form.screenType === 1">
          <el-form-item label="营养状态受损">
            <div data-testid="sel-impair">
              <el-select v-model="form.impairScore" style="width: 120px">
                <el-option v-for="o in SCORE_OPTIONS_0_3" :key="o.value" :label="o.label" :value="o.value"/>
              </el-select>
            </div>
            <span class="muted hint">0-无 1-肿瘤患者 2-重度消耗（卧床、胸腹部术后） 3-重症监护</span>
          </el-form-item>
          <el-form-item label="疾病严重程度">
            <div data-testid="sel-severity">
              <el-select v-model="form.severityScore" style="width: 120px">
                <el-option v-for="o in SCORE_OPTIONS_0_3" :key="o.value" :label="o.label" :value="o.value"/>
              </el-select>
            </div>
            <span class="muted hint">1-发热等 2-腹部手术、慢病急性发作 3-ICU、重度颅脑损伤</span>
          </el-form-item>
          <el-form-item label="年龄项（≥70 岁 +1）">
            <span data-testid="age-score">{{ ageScoreHint }}</span>
          </el-form-item>
          <el-form-item label="预计总分 / 判定">
            <span data-testid="local-total">{{ localTotal }} 分</span>
            <el-tag :type="nrsLocalRisk(localTotal) ? 'danger' : 'success'" class="hint" disable-transitions>
              {{ nrsLocalRisk(localTotal) ? '达到营养风险阈值（≥3）' : '未达阈值，需 7 天后复筛' }}
            </el-tag>
            <div class="muted hint">仅作表单提示，保存后以服务端返回的总分与判定为准。</div>
          </el-form-item>
        </template>
        <el-form-item v-else label="评定总分" required>
          <el-input-number v-model="form.totalScore" :max="form.screenType === 3 ? 17 : 35" :min="0"
                           data-testid="input-total"/>
          <span class="muted hint">{{
              form.screenType === 3 ? 'MNA 满分 17，<11 判为存在营养不良' : 'PG-SGA 由营养师按 0~35 分级'
            }}</span>
        </el-form-item>

        <el-form-item label="身高 / 体重">
          <el-input-number v-model="form.heightCm" :max="250" :min="0" :precision="1" controls-position="right"
                           data-testid="input-height" style="width: 140px"/>
          <span class="muted hint">cm</span>
          <el-input-number v-model="form.weightKg" :max="400" :min="0" :precision="1" controls-position="right"
                           data-testid="input-weight" style="width: 140px"/>
          <span class="muted hint">kg（BMI 由服务端算）</span>
        </el-form-item>
        <el-form-item label="近 3 月体重下降">
          <el-input-number v-model="form.weightLossPercent" :max="100" :min="0" :precision="1" controls-position="right"
                           data-testid="input-loss" style="width: 140px"/>
          <span class="muted hint">%</span>
        </el-form-item>
        <el-form-item label="下次筛查日期">
          <div data-testid="sel-next-screen">
            <el-date-picker v-model="form.nextScreenDate" placeholder="留空：阴性自动取筛查日 +7 天" style="width: 260px"
                            type="date" value-format="YYYY-MM-DD"/>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" maxlength="500" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button :loading="saving" data-testid="btn-save-screen" type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 只读详情：行点击打开，编辑走列表按钮 -->
    <el-dialog v-model="detailVisible" data-testid="detail-dialog" title="筛查记录详情" width="560px">
      <el-form disabled label-width="120px">
        <el-form-item label="筛查编号">
          <el-input :value="detail.screenNo"/>
        </el-form-item>
        <el-form-item label="患者">
          <el-input :value="`${detail.patientName || ''}（${detail.patientNo || ''}）`"/>
        </el-form-item>
        <el-form-item label="量表">
          <el-input :value="detail.screenTypeText"/>
        </el-form-item>
        <el-form-item label="总分 / 判定">
          <el-input :value="`${detail.totalScore} 分 · ${detail.riskFlagText}`"/>
        </el-form-item>
        <el-form-item label="BMI">
          <el-input :value="detail.bmi ?? '—'"/>
        </el-form-item>
        <el-form-item label="下次筛查">
          <el-input :value="detail.nextScreenDate || '—'"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input :rows="2" :value="detail.remark || '—'" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="btn-plan-for-risk" type="primary" @click="goDietPlan">查看膳食方案</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发起营养会诊 -->
    <el-dialog v-model="consultVisible" data-testid="consult-dialog" title="发起营养会诊" width="560px">
      <el-form label-width="120px">
        <el-form-item label="患者">
          <el-input :value="`${consultRow.patientName || ''}（${consultRow.admissionNo || ''}）`" disabled/>
        </el-form-item>
        <el-form-item label="会诊科室" required>
          <div data-testid="sel-consult-dept" style="width: 100%">
            <el-select v-model="consultForm.toDeptId" filterable placeholder="选择营养科" style="width: 100%">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="会诊范围" required>
          <el-radio-group v-model="consultForm.consultType">
            <el-radio :value="2">科间会诊</el-radio>
            <el-radio :value="3">全院会诊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否急会诊">
          <el-radio-group v-model="consultForm.isUrgent">
            <el-radio :value="0">普通（24 小时内应答）</el-radio>
            <el-radio :value="1">急会诊（10 分钟内应答）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会诊理由" required>
          <el-input v-model="consultForm.reason" :rows="3" data-testid="input-consult-reason" maxlength="500"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consultVisible = false">取消</el-button>
        <el-button :loading="savingConsult" data-testid="btn-do-consult" type="primary" @click="submitConsult">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {getInpatientListPage} from '@/api/inpatient'
import {getDepartmentSelectList} from '@/api/system'
import {
  applyNutritionConsult,
  deleteNutritionScreen,
  getNutritionOverview,
  listNutritionScreenPage,
  upsertNutritionScreen,
} from '@/api/nutrition'
import {
  NRS_RISK_CUTOFF,
  nrsLocalRisk,
  nrsLocalTotal,
  RISK_FLAG_TAG,
  SCORE_OPTIONS_0_3,
  SCREEN_SOURCE_TEXT,
  SCREEN_TYPE_TEXT,
} from '@/lib/nutrition'

const router = useRouter()

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

// ---------------- 患者与科室下拉（在院住院，一次抓全量供选择） ----------------
const admissions = ref([])
const depts = ref([])

const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

async function loadBaseData() {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200})
    admissions.value = res?.data?.records || []
  } catch (e) {
    admissions.value = []
  }
  try {
    const res = await getDepartmentSelectList({})
    depts.value = res?.data || []
  } catch (e) {
    depts.value = []
  }
}

// ---------------- 表单 ----------------
const formVisible = ref(false)
const saving = ref(false)
const emptyForm = () => ({
  id: null,
  admissionId: null,
  screenType: 1,
  screenSource: 1,
  impairScore: 0,
  severityScore: 0,
  totalScore: null,
  heightCm: null,
  weightKg: null,
  weightLossPercent: null,
  nextScreenDate: null,
  remark: '',
})
const form = reactive(emptyForm())

/** 年龄项只作展示：服务端按患者档案真实年龄算，前端传了也不认 */
const ageScoreHint = computed(() => {
  const a = admissions.value.find((x) => String(x.admissionId) === String(form.admissionId))
  return a?.age != null ? `${a.age} 岁 · 年龄项由服务端按档案年龄判定（≥70 岁 +1 分）` : '由服务端按患者档案年龄判定（≥70 岁 +1 分）'
})
const localTotal = computed(() => nrsLocalTotal(form.impairScore, form.severityScore, 0))

function onTypeChange() {
  if (form.screenType !== 1) {
    form.impairScore = null
    form.severityScore = null
  } else {
    form.impairScore = 0
    form.severityScore = 0
    form.totalScore = null
  }
}

function openForm(row) {
  Object.assign(form, emptyForm())
  if (row) {
    Object.assign(form, {
      id: row.id,
      admissionId: row.admissionId,
      screenType: row.screenType,
      screenSource: row.screenSource,
      impairScore: row.impairScore,
      severityScore: row.severityScore,
      totalScore: row.screenType === 1 ? null : row.totalScore,
      heightCm: row.heightCm,
      weightKg: row.weightKg,
      weightLossPercent: row.weightLossPercent,
      nextScreenDate: row.nextScreenDate,
      remark: row.remark || '',
    })
  }
  formVisible.value = true
}

async function submitForm() {
  if (!form.admissionId) {
    ElMessage.warning('请选择住院患者');
    return
  }
  if (form.screenType !== 1 && form.totalScore == null) {
    ElMessage.warning('请填写评定总分');
    return
  }
  saving.value = true
  try {
    const res = await upsertNutritionScreen({...form})
    const saved = res?.data || {}
    ElMessage.success(`已保存：总分 ${saved.totalScore ?? '-'} 分 · ${saved.riskFlagText || ''}`
        + (saved.nextScreenDate ? ` · 下次筛查 ${saved.nextScreenDate}` : ''))
    formVisible.value = false
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
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
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// ---------------- 详情 ----------------
const detailVisible = ref(false)
const detail = ref({})

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

function goDietPlan() {
  detailVisible.value = false
  router.push({path: '/diet-plan', query: {admissionId: detail.value.admissionId}})
}

// ---------------- 发起营养会诊 ----------------
const consultVisible = ref(false)
const savingConsult = ref(false)
const consultRow = ref({})
const consultForm = reactive({toDeptId: null, consultType: 2, isUrgent: 0, reason: ''})

function openConsult(row) {
  if (Number(row.riskFlag) !== 1 && Number(row.totalScore) < NRS_RISK_CUTOFF) {
    ElMessage.warning('该次筛查未达营养风险阈值，一般不需要会诊；确需会诊请到「营养会诊」页发起')
    return
  }
  consultRow.value = row
  Object.assign(consultForm, {
    toDeptId: row.deptId || null,
    consultType: 2,
    isUrgent: 0,
    reason: `NRS2002 总分 ${row.totalScore} 分判为有营养风险（${row.screenTypeText || ''}），请营养科会诊制定膳食方案。`,
  })
  consultVisible.value = true
}

async function submitConsult() {
  if (!consultForm.toDeptId) {
    ElMessage.warning('请选择会诊科室');
    return
  }
  if (!consultForm.reason?.trim()) {
    ElMessage.warning('会诊理由不能为空');
    return
  }
  savingConsult.value = true
  try {
    const res = await applyNutritionConsult({
      admissionId: consultRow.value.admissionId,
      toDeptId: consultForm.toDeptId,
      consultType: consultForm.consultType,
      isUrgent: consultForm.isUrgent,
      reason: consultForm.reason,
    })
    ElMessage.success(`营养会诊已申请（${res?.data || ''}）`)
    consultVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || '申请失败')
  } finally {
    savingConsult.value = false
  }
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

.hint {
  margin-left: 8px;
}
</style>
