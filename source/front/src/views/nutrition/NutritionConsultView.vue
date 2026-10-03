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
    <el-dialog v-model="formVisible" :title="form.id ? '修改营养会诊申请' : '发起营养会诊'" width="560px"
               data-testid="consult-form-dialog">
      <el-form label-width="120px">
        <el-form-item label="住院患者" required>
          <div data-testid="sel-admission" style="width: 100%">
            <el-select v-model="form.admissionId" filterable :disabled="!!form.id" placeholder="选择在院患者"
                       style="width: 100%">
              <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)"
                         :value="a.admissionId"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="会诊科室" required>
          <div data-testid="sel-to-dept" style="width: 100%">
            <el-select v-model="form.toDeptId" filterable placeholder="选择营养科" style="width: 100%">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="会诊范围" required>
          <el-radio-group v-model="form.consultType" data-testid="sel-consult-type">
            <el-radio :value="2">科间会诊</el-radio>
            <el-radio :value="3">全院会诊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="紧急度">
          <el-radio-group v-model="form.isUrgent" data-testid="sel-urgent">
            <el-radio :value="0">普通（24 小时内应答）</el-radio>
            <el-radio :value="1">急会诊（10 分钟内应答）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会诊理由" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="500" data-testid="input-reason"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500"/>
        </el-form-item>
        <div class="form-tip">
          申请科室、申请医生、患者身份一律由服务端从入院记录与登录态推导，页面传了也不认。会诊类别固定为「营养会诊」。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-save-consult" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 出结论 -->
    <el-dialog v-model="finishVisible" title="填写营养会诊结论" width="560px" data-testid="finish-dialog">
      <el-form label-width="110px">
        <el-form-item label="患者">
          <el-input :value="`${finishRow.patientName || ''}（${finishRow.admissionNo || ''}）`" disabled/>
        </el-form-item>
        <el-form-item label="会诊时间">
          <div data-testid="sel-consult-time" style="width: 100%">
            <el-date-picker v-model="finishForm.consultTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                            placeholder="留空取当前时间" style="width: 100%"/>
          </div>
        </el-form-item>
        <el-form-item label="会诊结论" required>
          <el-input v-model="finishForm.conclusion" type="textarea" :rows="4" maxlength="2000"
                    placeholder="营养评定结论 + 膳食/管饲/肠外方案 + 目标热量蛋白 + 复评时间"
                    data-testid="input-conclusion"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="finishForm.remark" type="textarea" :rows="2" maxlength="500"/>
        </el-form-item>
        <div class="form-tip">结论会回写进住院病历，没有结论的「已完成」在病历上等于什么都没发生。</div>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-do-finish" @click="doFinish">完成并回写病历
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情（只读） -->
    <el-dialog v-model="detailVisible" title="营养会诊明细" width="660px" data-testid="consult-detail-dialog">
      <el-form :disabled="true" label-width="110px">
        <el-form-item label="会诊号 / 类别">
          <el-input :value="`${detail.consultationNo || '—'} · ${detail.consultCategoryText || '—'}`"/>
        </el-form-item>
        <el-form-item label="患者">
          <el-input
              :value="`${detail.patientName || ''} · ${detail.admissionNo || ''}（${detail.fromDeptName || ''}${detail.bedNo ? ' ' + detail.bedNo + '床' : ''}）`"/>
        </el-form-item>
        <el-form-item label="申请 / 接诊">
          <el-input
              :value="`${detail.applyDoctorName || '—'} ${detail.applyTime || ''} → ${detail.acceptDoctorName || '未接诊'} ${detail.acceptTime || ''}`"/>
        </el-form-item>
        <el-form-item label="范围 / 紧急度">
          <el-input
              :value="`${detail.consultTypeText || '—'} · ${detail.isUrgentText || '—'} · 应答耗时 ${responseText(detail)}`"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-input
              :value="`${detail.consultStatusText || '—'}${detail.overdueText ? ' · ' + detail.overdueText : ''}`"/>
        </el-form-item>
        <el-form-item label="会诊理由">
          <el-input :value="detail.reason || '—'" type="textarea" :rows="3"/>
        </el-form-item>
        <el-form-item label="会诊结论">
          <el-input :value="detail.conclusion || '—'" type="textarea" :rows="4"/>
        </el-form-item>
        <el-form-item label="回写病历">
          <el-input :value="detail.recordNo ? `病历号 ${detail.recordNo} · ${detail.finishTime || ''}` : '尚未回写'"/>
        </el-form-item>
        <el-form-item label="取消原因">
          <el-input :value="detail.cancelReason || '—'"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {getInpatientListPage} from '@/api/inpatient'
import {getDepartmentSelectList} from '@/api/system'
import {
  acceptNutritionConsult,
  applyNutritionConsult,
  cancelNutritionConsult,
  finishNutritionConsult,
  getNutritionConsultDetail,
  getNutritionOverview,
  listNutritionConsultPage,
} from '@/api/nutrition'

const STATUS_TAG = {0: 'warning', 3: 'primary', 1: 'success', 2: 'info'}

const overview = ref({})
const rows = ref([])
const total = ref(0)
const loading = ref(false)

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

function responseText(row) {
  if (row.responseMinutes == null) return '—'
  const m = Number(row.responseMinutes)
  return m < 60 ? `${m} 分钟` : `${(m / 60).toFixed(1)} 小时`
}

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

// ---------------- 发起 / 修改 ----------------
const formVisible = ref(false)
const saving = ref(false)
const emptyForm = () => ({
  id: null,
  admissionId: null,
  toDeptId: null,
  consultType: 2,
  isUrgent: 0,
  reason: '',
  remark: ''
})
const form = reactive(emptyForm())

function openForm(row) {
  Object.assign(form, emptyForm())
  if (row) {
    Object.assign(form, {
      id: row.consultationId,
      admissionId: row.admissionId,
      toDeptId: row.toDeptId,
      consultType: row.consultType,
      isUrgent: row.isUrgent,
      reason: row.reason || '',
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
  if (!form.toDeptId) {
    ElMessage.warning('请选择会诊科室');
    return
  }
  if (!form.reason.trim()) {
    ElMessage.warning('会诊理由不能为空');
    return
  }
  saving.value = true
  try {
    await applyNutritionConsult({...form})
    ElMessage.success(form.id ? '会诊申请已修改' : '营养会诊已申请')
    formVisible.value = false
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 应答 / 完成 / 取消 ----------------
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

const finishVisible = ref(false)
const finishRow = ref({})
const finishForm = reactive({consultTime: null, conclusion: '', remark: ''})

function openFinish(row) {
  finishRow.value = row
  finishForm.consultTime = null
  finishForm.conclusion = ''
  finishForm.remark = ''
  finishVisible.value = true
}

async function doFinish() {
  if (!finishForm.conclusion.trim()) {
    ElMessage.warning('会诊结论不能为空');
    return
  }
  saving.value = true
  try {
    const res = await finishNutritionConsult({
      consultationId: finishRow.value.consultationId,
      consultTime: finishForm.consultTime,
      conclusion: finishForm.conclusion.trim(),
      remark: finishForm.remark,
    })
    ElMessage.success(res?.data ? `已完成，病历号 ${res.data}` : '已完成并回写病历')
    finishVisible.value = false
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '完成失败')
  } finally {
    saving.value = false
  }
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

// ---------------- 详情 ----------------
const detailVisible = ref(false)
const detail = ref({})

async function openDetail(row) {
  detail.value = row
  detailVisible.value = true
  try {
    const res = await getNutritionConsultDetail(row.consultationId)
    if (res?.data) detail.value = res.data
  } catch (e) {
    // 列表行已有主字段，详情接口失败时保留行数据，不打断查看
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

.overdue {
  color: #f56c6c;
  font-size: 12px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
