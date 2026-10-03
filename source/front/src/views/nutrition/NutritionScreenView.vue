<template>
  <div data-testid="nutrition-screen-view">
    <el-alert
      class="mb-3"
      type="info"
      :closable="false"
      show-icon
      title="营养风险筛查（NRS2002 / PG-SGA / MNA）"
      description="NRS2002 总分 = 营养状态受损（0~3）+ 疾病严重程度（0~3）+ 年龄≥70 岁加 1 分，<b>总分≥3 判为有营养风险</b>。总分、判定、BMI、复筛日期全部由服务端算（分数是能凑的，判定不能交给浏览器）。判阴性的必须留复筛日期，默认筛查日 +7 天 —— 「每周复筛」不落成一行日期就没人执行。" />

    <el-card shadow="never" class="stat-card">
      <div class="stat-items">
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-in-hospital">{{ overview.inHospitalCount ?? '-' }}</div>
          <div class="stat-label">在院患者数</div>
          <div class="stat-sub">其中已筛 {{ overview.inHospitalScreenedCount ?? 0 }} 人</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-danger': (overview.missedScreenCount ?? 0) > 0 }" data-testid="ov-missed">
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
          <div class="stat-value" :class="{ 'stat-value-warn': (overview.reScreenDueCount ?? 0) > 0 }" data-testid="ov-due">
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
                <el-option label="在院" :value="1" />
                <el-option label="已出院" :value="0" />
                <el-option label="全部" :value="null" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="量表">
            <div data-testid="filter-screen-type">
              <el-select v-model="query.screenType" placeholder="量表" clearable style="width: 190px">
                <el-option v-for="(t, code) in SCREEN_TYPE_TEXT" :key="code" :label="t" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="判定">
            <div data-testid="filter-risk">
              <el-select v-model="query.riskFlag" placeholder="判定" clearable style="width: 130px">
                <el-option label="无营养风险" :value="0" />
                <el-option label="有营养风险" :value="1" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="只看到期复筛">
            <div data-testid="filter-due">
              <el-select v-model="query.dueOnly" placeholder="只看到期复筛" clearable style="width: 140px">
                <el-option label="是" :value="1" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="筛查开始日"
              end-placeholder="筛查结束日"
              value-format="YYYY-MM-DD"
              style="width: 240px"
              data-testid="filter-date" />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="姓名 / 患者编号 / 住院号" clearable style="width: 210px" data-testid="filter-keyword" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-query" @click="onQuery">查询</el-button>
            <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：登记筛查统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:nutrition:screenEdit'" type="primary" data-testid="btn-add-screen" @click="openForm()">登记筛查</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe :max-height="tableMaxHeight" v-loading="loading" data-testid="screen-table" @row-click="openDetail">
      <el-table-column label="患者" min-width="150">
        <template #default="{ row }">
          <div>{{ row.patientName || '-' }}</div>
          <div class="muted">{{ row.patientNo || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="admissionNo" label="住院号" width="150" show-overflow-tooltip />
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
          <span class="muted">受损 {{ row.impairScore ?? '-' }} · 严重 {{ row.severityScore ?? '-' }} · 年龄 {{ row.ageScore ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总分" width="80" align="right">
        <template #default="{ row }">
          <span data-testid="cell-total">{{ row.totalScore }}</span>
        </template>
      </el-table-column>
      <el-table-column label="判定" width="120">
        <template #default="{ row }">
          <el-tag :type="RISK_FLAG_TAG[row.riskFlag]" disable-transitions data-testid="cell-risk">{{ row.riskFlagText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="BMI" width="80" align="right">
        <template #default="{ row }">{{ row.bmi ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="筛查时机" width="130">
        <template #default="{ row }">{{ row.screenSourceText }}</template>
      </el-table-column>
      <el-table-column prop="screenTime" label="筛查时间" width="165" />
      <el-table-column label="下次筛查" width="130">
        <template #default="{ row }">
          <span :class="{ 'stat-value-warn': row.reScreenDue === 1 }" data-testid="cell-next-screen">
            {{ row.nextScreenDate || '—' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="screenerName" label="筛查人" width="100" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button
            v-perm="'ipd:nutrition:screenEdit'"
            link
            type="primary"
            data-testid="btn-edit-screen"
            @click.stop="openForm(row)">修改</el-button>
          <el-button
            v-perm="'ipd:nutrition:consultApply'"
            link
            type="primary"
            data-testid="btn-apply-consult"
            @click.stop="openConsult(row)">发起营养会诊</el-button>
          <el-button
            v-perm="'ipd:nutrition:screenDelete'"
            link
            type="danger"
            data-testid="btn-delete-screen"
            @click.stop="doDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- 登记 / 修改筛查 -->
    <el-dialog v-model="formVisible" :title="form.id ? '修改筛查记录' : '登记营养风险筛查'" width="620px" data-testid="screen-dialog">
      <el-form label-width="130px">
        <el-form-item label="住院患者" required>
          <div data-testid="sel-admission" style="width: 100%">
            <el-select v-model="form.admissionId" filterable :disabled="!!form.id" placeholder="选择在院患者" style="width: 100%">
              <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)" :value="a.admissionId" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="量表" required>
          <div data-testid="sel-screen-type">
            <el-select v-model="form.screenType" style="width: 240px" @change="onTypeChange">
              <el-option v-for="(t, code) in SCREEN_TYPE_TEXT" :key="code" :label="t" :value="Number(code)" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="筛查时机" required>
          <div data-testid="sel-screen-source">
            <el-select v-model="form.screenSource" style="width: 200px">
              <el-option v-for="(t, code) in SCREEN_SOURCE_TEXT" :key="code" :label="t" :value="Number(code)" />
            </el-select>
          </div>
        </el-form-item>

        <template v-if="form.screenType === 1">
          <el-form-item label="营养状态受损">
            <div data-testid="sel-impair">
              <el-select v-model="form.impairScore" style="width: 120px">
                <el-option v-for="o in SCORE_OPTIONS_0_3" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </div>
            <span class="muted hint">0-无 1-肿瘤患者 2-重度消耗（卧床、胸腹部术后） 3-重症监护</span>
          </el-form-item>
          <el-form-item label="疾病严重程度">
            <div data-testid="sel-severity">
              <el-select v-model="form.severityScore" style="width: 120px">
                <el-option v-for="o in SCORE_OPTIONS_0_3" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </div>
            <span class="muted hint">1-发热等 2-腹部手术、慢病急性发作 3-ICU、重度颅脑损伤</span>
          </el-form-item>
          <el-form-item label="年龄项（≥70 岁 +1）">
            <span data-testid="age-score">{{ ageScoreHint }}</span>
          </el-form-item>
          <el-form-item label="预计总分 / 判定">
            <span data-testid="local-total">{{ localTotal }} 分</span>
            <el-tag :type="nrsLocalRisk(localTotal) ? 'danger' : 'success'" disable-transitions class="hint">
              {{ nrsLocalRisk(localTotal) ? '达到营养风险阈值（≥3）' : '未达阈值，需 7 天后复筛' }}
            </el-tag>
            <div class="muted hint">仅作表单提示，保存后以服务端返回的总分与判定为准。</div>
          </el-form-item>
        </template>
        <el-form-item v-else label="评定总分" required>
          <el-input-number v-model="form.totalScore" :min="0" :max="form.screenType === 3 ? 17 : 35" data-testid="input-total" />
          <span class="muted hint">{{ form.screenType === 3 ? 'MNA 满分 17，<11 判为存在营养不良' : 'PG-SGA 由营养师按 0~35 分级' }}</span>
        </el-form-item>

        <el-form-item label="身高 / 体重">
          <el-input-number v-model="form.heightCm" :min="0" :max="250" :precision="1" controls-position="right" style="width: 140px" data-testid="input-height" />
          <span class="muted hint">cm</span>
          <el-input-number v-model="form.weightKg" :min="0" :max="400" :precision="1" controls-position="right" style="width: 140px" data-testid="input-weight" />
          <span class="muted hint">kg（BMI 由服务端算）</span>
        </el-form-item>
        <el-form-item label="近 3 月体重下降">
          <el-input-number v-model="form.weightLossPercent" :min="0" :max="100" :precision="1" controls-position="right" style="width: 140px" data-testid="input-loss" />
          <span class="muted hint">%</span>
        </el-form-item>
        <el-form-item label="下次筛查日期">
          <div data-testid="sel-next-screen">
            <el-date-picker v-model="form.nextScreenDate" type="date" value-format="YYYY-MM-DD" placeholder="留空：阴性自动取筛查日 +7 天" style="width: 260px" />
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-save-screen" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 只读详情：行点击打开，编辑走列表按钮 -->
    <el-dialog v-model="detailVisible" title="筛查记录详情" width="560px" data-testid="detail-dialog">
      <el-form label-width="120px" disabled>
        <el-form-item label="筛查编号"><el-input :value="detail.screenNo" /></el-form-item>
        <el-form-item label="患者"><el-input :value="`${detail.patientName || ''}（${detail.patientNo || ''}）`" /></el-form-item>
        <el-form-item label="量表"><el-input :value="detail.screenTypeText" /></el-form-item>
        <el-form-item label="总分 / 判定"><el-input :value="`${detail.totalScore} 分 · ${detail.riskFlagText}`" /></el-form-item>
        <el-form-item label="BMI"><el-input :value="detail.bmi ?? '—'" /></el-form-item>
        <el-form-item label="下次筛查"><el-input :value="detail.nextScreenDate || '—'" /></el-form-item>
        <el-form-item label="备注"><el-input :value="detail.remark || '—'" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" data-testid="btn-plan-for-risk" @click="goDietPlan">查看膳食方案</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发起营养会诊 -->
    <el-dialog v-model="consultVisible" title="发起营养会诊" width="560px" data-testid="consult-dialog">
      <el-form label-width="120px">
        <el-form-item label="患者">
          <el-input :value="`${consultRow.patientName || ''}（${consultRow.admissionNo || ''}）`" disabled />
        </el-form-item>
        <el-form-item label="会诊科室" required>
          <div data-testid="sel-consult-dept" style="width: 100%">
            <el-select v-model="consultForm.toDeptId" filterable placeholder="选择营养科" style="width: 100%">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id" />
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
          <el-input v-model="consultForm.reason" type="textarea" :rows="3" maxlength="500" data-testid="input-consult-reason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consultVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingConsult" data-testid="btn-do-consult" @click="submitConsult">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import { getInpatientListPage } from '@/api/inpatient'
import { getDepartmentSelectList } from '@/api/system'
import {
  applyNutritionConsult,
  deleteNutritionScreen,
  getNutritionOverview,
  listNutritionScreenPage,
  upsertNutritionScreen,
} from '@/api/nutrition'
import {
  NRS_RISK_CUTOFF,
  RISK_FLAG_TAG,
  SCORE_OPTIONS_0_3,
  SCREEN_SOURCE_TEXT,
  SCREEN_TYPE_TEXT,
  nrsLocalRisk,
  nrsLocalTotal,
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
    const res = await listNutritionScreenPage({ ...query })
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
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
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
  if (!form.admissionId) { ElMessage.warning('请选择住院患者'); return }
  if (form.screenType !== 1 && form.totalScore == null) { ElMessage.warning('请填写评定总分'); return }
  saving.value = true
  try {
    const res = await upsertNutritionScreen({ ...form })
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
  router.push({ path: '/diet-plan', query: { admissionId: detail.value.admissionId } })
}

// ---------------- 发起营养会诊 ----------------
const consultVisible = ref(false)
const savingConsult = ref(false)
const consultRow = ref({})
const consultForm = reactive({ toDeptId: null, consultType: 2, isUrgent: 0, reason: '' })

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
  if (!consultForm.toDeptId) { ElMessage.warning('请选择会诊科室'); return }
  if (!consultForm.reason?.trim()) { ElMessage.warning('会诊理由不能为空'); return }
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
