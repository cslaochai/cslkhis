<script setup lang="ts">
/**
 * 出院随访与满意度回收（G20 任务流转 + sql/164 回收闭环，菜单 311）
 *
 * 三种入口：手动新建（选患者）、按出院记录一键生成（幂等）、出院办理自动建单（后端 SPI）。
 * 状态机：1 待随访 → 2 随访中 → 3 已完成；未完成可取消（4）。
 *
 * 四条口径：
 * 1. **看板数字全部来自 `/charge/followup/stat`**，前端不数当前页 —— 翻页只能看见 20 条，
 *    而「今天欠了多少电话」是全量事实。逾期按 followup_time 现算，没有定时任务翻状态。
 * 2. **手机号列表只有 `phoneMasked`**：编辑必须先 `getById` 取明文再回填。
 *    直接把列表行塞进表单会怎样？`Object.assign` 后整对象回写，星号就把真号洗进库里了（不可逆）。
 * 3. 完成率分母**不含已取消**：取消是任务作废，不是随访失败，算进分母会把率无故压低。
 * 4. 科室范围由后端按登录岗位收口，前端不给「全部科室」开关。
 *
 * followupTime 传「yyyy-MM-dd HH:mm:ss」空格分隔，与后端 @JsonFormat 宽进口径一致。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getFollowupList, getFollowupDetail, getFollowupStat,
  startFollowup, completeFollowup, cancelFollowup,
  followupUpsert, followupCreateFromDischarge, followupCreateRevisitAppoint,
} from '@/api/followup'
import { issueDispatch } from '@/api/survey'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import PatientSelect from '@/components/his/PatientSelect.vue'
import RevisitAppointDialog from '@/components/his/RevisitAppointDialog.vue'
import { REVISIT_SOURCE } from '@/lib/revisitPolicy'

const activeTab = ref('list')

// ---------------- 字典 ----------------
const typeDict = ref<any[]>([])
const statusDict = ref<any[]>([])
const typeText = (v: any) => dictLabelText(typeDict.value, v)
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.FOLLOWUP_TYPE},${DICT_TYPE.FOLLOWUP_STATUS}`)
    typeDict.value = res?.data?.[DICT_TYPE.FOLLOWUP_TYPE] || []
    statusDict.value = res?.data?.[DICT_TYPE.FOLLOWUP_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
}
const statusTag = (v: number) => ({ 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' } as any)[v] || 'info'

// ---------------- 看板 ----------------
const stat = ref<any>(null)
const statLoading = ref(false)
const loadStat = async () => {
  statLoading.value = true
  try {
    const res: any = await getFollowupStat()
    if (res.code === 200) stat.value = res.data
    else ElMessage.error(res.message || '看板加载失败')
  } catch (e) { console.error(e); ElMessage.error('看板加载失败') } finally { statLoading.value = false }
}

// ---------------- 列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  patientName: '', followupType: null as number | null, followupStatus: null as number | null,
  overdueOnly: false,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getFollowupList({
      patientName: query.patientName.trim() || undefined,
      followupType: query.followupType ?? undefined,
      followupStatus: query.followupStatus ?? undefined,
      overdueOnly: query.overdueOnly || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { loading.value = false }
}
const reset = () => {
  Object.assign(query, { patientName: '', followupType: null, followupStatus: null, overdueOnly: false, pageNum: 1 })
  loadList()
}
/** 看板上点「逾期」直接筛出这批任务 */
const gotoOverdue = () => {
  Object.assign(query, { patientName: '', followupStatus: null, overdueOnly: true, pageNum: 1 })
  activeTab.value = 'list'
  loadList()
}
const fmtTime = (v: any) => (v ? String(v).slice(0, 16).replace('T', ' ') : '—')

// ---------------- 只读详情（行点击） ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  const res: any = await getFollowupDetail(row.id).catch((e: any) => { console.error(e); return null })
  if (res && res.code === 200) { detail.value = res.data; detailVisible.value = true }
  else if (res) ElMessage.error(res.message || '加载详情失败')
}

// ---------------- 新建 / 编辑 ----------------
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: null as string | number | null, patientId: null as string | number | null, patientName: '',
  followupType: 1, followupTime: '', followupContent: '', phone: '', remark: '',
})
const openCreate = () => {
  Object.assign(editForm, {
    id: null, patientId: null, patientName: '', followupType: 1,
    followupTime: '', followupContent: '', phone: '', remark: '',
  })
  editVisible.value = true
}
/**
 * 编辑必须走 getById 取明文：列表行里的 phone 是 null、只有 phoneMasked。
 * 用 `editForm.phone = row.phone` 会把电话栏清空，保存后随访员打电话没号可拨。
 */
const openEdit = async (row: any) => {
  try {
    const res: any = await getFollowupDetail(row.id)
    if (res.code !== 200) { ElMessage.error(res.message || '加载任务失败'); return }
    const t = res.data || {}
    Object.assign(editForm, {
      id: t.id, patientId: t.patientId, patientName: t.patientName, followupType: t.followupType,
      followupTime: fmtTime(t.followupTime),
      followupContent: t.followupContent || '', phone: t.phone || '', remark: t.remark || '',
    })
    editVisible.value = true
  } catch (e) { console.error(e); ElMessage.error('加载任务失败') }
}
const onPatientSelect = (p: any) => {
  editForm.patientName = p?.patientName || p?.name || ''
  if (p?.phone && !editForm.phone) editForm.phone = p.phone
}
const submitEdit = async () => {
  if (!editForm.patientId || !editForm.followupTime || !editForm.followupContent.trim()) {
    ElMessage.warning('患者、计划时间、随访内容为必填')
    return
  }
  editLoading.value = true
  try {
    const res: any = await followupUpsert({
      id: editForm.id ?? undefined,
      patientId: editForm.patientId,
      followupType: editForm.followupType,
      followupTime: editForm.followupTime,
      followupContent: editForm.followupContent.trim(),
      phone: editForm.phone || undefined,
      remark: editForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('随访任务已保存')
      editVisible.value = false
      loadList(); loadStat()
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { editLoading.value = false }
}

// ---------------- 执行 ----------------
const completeVisible = ref(false)
const completeId = ref<string | number | null>(null)
const completeResult = ref('')
const openComplete = (row: any) => {
  completeId.value = row.id
  completeResult.value = ''
  completeVisible.value = true
}
const submitComplete = async () => {
  if (!completeResult.value.trim()) { ElMessage.warning('请填写随访结果'); return }
  try {
    const res: any = await completeFollowup(completeId.value, { result: completeResult.value.trim() })
    if (res.code === 200) {
      ElMessage.success('随访已完成，满意度问卷已按启用模板自动发放')
      completeVisible.value = false
      loadList(); loadStat()
    } else ElMessage.error(res.message || '操作失败')
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}
const start = async (row: any) => {
  try {
    const res: any = await startFollowup(row.id, {})
    if (res.code === 200) { ElMessage.success('已开始随访'); loadList(); loadStat() } else ElMessage.error(res.message || '操作失败')
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}
const cancel = async (row: any) => {
  try {
    const res: any = await cancelFollowup(row.id, { reason: '页面手动取消' })
    if (res.code === 200) { ElMessage.success('已取消'); loadList(); loadStat() } else ElMessage.error(res.message || '操作失败')
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

/**
 * 手工补发满意度问卷：正常路径是「完成随访」自动发，这里只服务两种缺口 ——
 * 完成那天院内有事没配问卷（后端跳过不报错），或电话重打了一次要再收一份。
 * 同一任务 + 同一模板幂等，重复点不会多出第二张发放单。
 */
const issueVisible = ref(false)
const issueLoading = ref(false)
const issueForm = reactive({ taskNo: '', followupTaskId: '' as string | number, channel: 1, expireDays: 14 })
const openIssue = (row: any) => {
  Object.assign(issueForm, { taskNo: row.taskNo, followupTaskId: row.id, channel: 1, expireDays: 14 })
  issueVisible.value = true
}
const submitIssue = async () => {
  issueLoading.value = true
  try {
    const res: any = await issueDispatch({
      followupTaskId: issueForm.followupTaskId,
      channel: issueForm.channel,
      expireDays: issueForm.expireDays,
    })
    if (res.code === 200) {
      ElMessage.success(`已发放：${res.data?.dispatchNo || ''}（到「满意度评价 → 发放回收」录入答卷）`)
      issueVisible.value = false
    } else ElMessage.error(res.message || '发放失败')
  } catch (e) { console.error(e); ElMessage.error('发放失败') } finally { issueLoading.value = false }
}

// ---------------- 按出院记录生成 ----------------
const genVisible = ref(false)
const genLoading = ref(false)
const genForm = reactive({ dischargeId: '', followupType: 1, daysOffset: 7, followupContent: '' })
const openGenerate = () => {
  Object.assign(genForm, { dischargeId: '', followupType: 1, daysOffset: 7, followupContent: '' })
  genVisible.value = true
}
const submitGenerate = async () => {
  if (!genForm.dischargeId) { ElMessage.warning('请填写出院记录ID'); return }
  genLoading.value = true
  try {
    const res: any = await followupCreateFromDischarge({
      dischargeId: genForm.dischargeId,
      followupType: genForm.followupType,
      daysOffset: genForm.daysOffset,
      followupContent: genForm.followupContent || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('随访计划已生成（同一出院记录重复生成会返回已生成任务）')
      genVisible.value = false
      loadList(); loadStat()
    } else ElMessage.error(res.message || '生成失败')
  } catch (e) { console.error(e); ElMessage.error('生成失败') } finally { genLoading.value = false }
}

// ---------------- 生成复诊号（复诊来源 4，sql/121） ----------------
const revisitVisible = ref(false)
const revisitBusy = ref(false)
const revisitTask = ref<any>(null)

/**
 * 随访打电话时约定「下周回来复查」，这一步就是把约定落成一张真实的号：
 * 占号源、按复诊收费策略收钱，并把复诊号回写到任务上（一条任务同一时间只挂一个有效复诊号，
 * 旧号退号/过号/爽约后允许重约 —— 这条判断在后端，前端只负责把按钮写成「重约复诊号」）。
 */
const openRevisit = (row: any) => {
  revisitTask.value = row
  revisitVisible.value = true
}

const submitRevisit = async (payload: {
  revisitRecordId: string | number
  scheduleId: string | number
  slotId?: string | number
  settlementType: number
}) => {
  const task = revisitTask.value
  if (!task) return
  revisitBusy.value = true
  try {
    const res: any = await followupCreateRevisitAppoint({ taskId: task.id, ...payload })
    if (res.code === 200) {
      ElMessage.success('复诊号已生成，患者按预约时段来院即可')
      revisitVisible.value = false
      loadList(); loadStat()
    } else ElMessage.error(res.message || '生成复诊号失败')
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e?.message || '生成复诊号失败')
  } finally {
    revisitBusy.value = false
  }
}

onMounted(() => { loadDicts(); loadList(); loadStat() })
</script>

<template>
  <div class="followup-page">
    <!-- 看板：今日该打多少电话、欠了多少 -->
    <el-card shadow="never" class="stat-card" v-loading="statLoading">
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-label">待随访</span>
          <span class="stat-value warn">{{ stat?.pendingCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">随访中</span>
          <span class="stat-value">{{ stat?.doingCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已完成</span>
          <span class="stat-value ok">{{ stat?.doneCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日应随访</span>
          <span class="stat-value">{{ stat?.todayDueCount ?? 0 }}</span>
        </div>
        <div class="stat-item clickable" title="点击筛出逾期未随访的任务" @click="gotoOverdue">
          <span class="stat-label">逾期未访</span>
          <span class="stat-value danger">{{ stat?.overdueCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日完成</span>
          <span class="stat-value ok">{{ stat?.doneTodayCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">完成率</span>
          <span class="stat-value">{{ stat?.completeRate ?? 0 }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">随访带来的复诊号</span>
          <span class="stat-value">{{ stat?.revisitCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已取消</span>
          <span class="stat-value">{{ stat?.cancelledCount ?? 0 }}</span>
        </div>
      </div>
      <div class="stat-foot">
        <span class="text-xs text-gray-400">统计时点 {{ fmtTime(stat?.statTime) }}（数字为全量口径，不随下方翻页变化）</span>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="随访任务" name="list">
          <div class="filter-bar">
            <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width: 160px"
                      @keyup.enter="query.pageNum = 1; loadList()" />
            <el-select v-model="query.followupType" placeholder="随访类型" clearable style="width: 130px"
                       :fit-input-width="false">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="query.followupStatus" placeholder="状态" clearable style="width: 120px"
                       :fit-input-width="false">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-checkbox v-model="query.overdueOnly" @change="query.pageNum = 1; loadList()">仅逾期未访</el-checkbox>
            <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
            <el-button v-perm="'inpatient:followup:add'" type="primary" plain @click="openGenerate">按出院记录生成</el-button>
            <el-button v-perm="'inpatient:followup:add'" type="primary" @click="openCreate">新建随访</el-button>
          </div>
          <el-table :data="rows" v-loading="loading" border stripe size="small"
                    data-testid="followup-table" @row-click="openDetail">
            <el-table-column prop="taskNo" label="任务编号" width="200" />
            <el-table-column prop="patientName" label="患者" width="120">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs ml-1">{{ row.patientNo }}</span></template>
            </el-table-column>
            <el-table-column prop="phoneMasked" label="联系电话" width="120">
              <template #default="{ row }">{{ row.phoneMasked || '—' }}</template>
            </el-table-column>
            <el-table-column prop="deptName" label="出院科室" width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="followupType" label="类型" width="100">
              <template #default="{ row }">{{ typeText(row.followupType) }}</template>
            </el-table-column>
            <el-table-column prop="followupTime" label="计划时间" width="150">
              <template #default="{ row }">
                <span :class="{ 'overdue-text': row.overdue }">{{ fmtTime(row.followupTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="diagnosis" label="诊断" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
            </el-table-column>
            <el-table-column prop="followupStatus" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.followupStatus)">{{ statusText(row.followupStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executorName" label="执行人" width="90">
              <template #default="{ row }">{{ row.executorName || '—' }}</template>
            </el-table-column>
            <el-table-column label="复诊号" width="150">
              <template #default="{ row }">
                <span v-if="row.revisitAppointId" class="font-mono text-xs text-slate-600">{{ row.revisitAppointId }}</span>
                <span v-else class="text-gray-400 text-xs">未预约</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <!-- 行点击会开详情弹框，操作列每个按钮必须 .stop，否则点「编辑」同时弹两个框 -->
                <el-button v-if="row.followupStatus !== 4" v-perm="'inpatient:followup:edit'"
                           link type="warning" size="small" @click.stop="openRevisit(row)">
                  {{ row.revisitAppointId ? '重约复诊号' : '生成复诊号' }}
                </el-button>
                <template v-if="row.followupStatus === 1">
                  <el-button v-perm="'inpatient:followup:edit'" link type="primary" size="small"
                             @click.stop="openEdit(row)">编辑</el-button>
                  <el-button link type="success" size="small" @click.stop="start(row)">开始</el-button>
                  <el-button v-perm="'inpatient:followup:delete'" link type="danger" size="small"
                             @click.stop="cancel(row)">取消</el-button>
                </template>
                <el-button v-else-if="row.followupStatus === 2" link type="success" size="small"
                           @click.stop="openComplete(row)">完成随访</el-button>
                <!-- 补发问卷：完成随访时没有启用模板会被后端跳过，这里给一条人工兜底 -->
                <el-button v-else-if="row.followupStatus === 3" v-perm="'qc:survey:add'"
                           link type="primary" size="small" @click.stop="openIssue(row)">补发问卷</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                           :page-sizes="PAGE_SIZES" :total="total"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="query.pageNum = 1; loadList()" @current-change="loadList" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="科室待办与类型分布" name="byDept">
          <div class="two-col">
            <div>
              <div class="block-title">科室待办 TOP10（未完成 / 已完成）</div>
              <el-table :data="stat?.byDeptPending || []" border stripe size="small" v-loading="statLoading">
                <el-table-column prop="deptName" label="科室" min-width="160">
                  <template #default="{ row }">{{ row.deptName || '未归科（出院记录无科室）' }}</template>
                </el-table-column>
                <el-table-column prop="pendingCount" label="未完成" width="100" align="right">
                  <template #default="{ row }"><span class="stat-value warn">{{ row.pendingCount }}</span></template>
                </el-table-column>
                <el-table-column prop="doneCount" label="已完成" width="100" align="right">
                  <template #default="{ row }">{{ row.doneCount }}</template>
                </el-table-column>
              </el-table>
              <div class="text-xs text-gray-400 mt-2">
                待办多可能是出院量大本就该多，所以同一行给出已完成 —— 一眼看出是「活多」还是「没干」。
              </div>
            </div>
            <div>
              <div class="block-title">按随访方式分布</div>
              <el-table :data="stat?.byType || []" border stripe size="small" v-loading="statLoading">
                <el-table-column prop="name" label="随访方式" min-width="140" />
                <el-table-column prop="count" label="任务数" width="100" align="right" />
              </el-table>
              <div class="text-xs text-gray-400 mt-2">
                术后随访（出院时有手术记录）与复诊提醒由出院办理自动建单，慢病/用药指导多为手工新建。
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 详情（行点击，只读） -->
    <el-dialog v-model="detailVisible" title="随访任务详情" width="640px">
      <el-form v-if="detail" label-width="90px" disabled>
        <el-form-item label="任务编号"><el-input :model-value="detail.taskNo" /></el-form-item>
        <el-form-item label="患者">
          <el-input :model-value="`${detail.patientName || ''} ${detail.patientNo || ''}`" />
        </el-form-item>
        <el-form-item label="联系电话"><el-input :model-value="detail.phoneMasked || '—'" /></el-form-item>
        <el-form-item label="出院科室"><el-input :model-value="detail.deptName || '—'" /></el-form-item>
        <el-form-item label="随访类型">
          <el-input :model-value="typeText(detail.followupType)" />
        </el-form-item>
        <el-form-item label="计划时间"><el-input :model-value="fmtTime(detail.followupTime)" /></el-form-item>
        <el-form-item label="状态"><el-input :model-value="statusText(detail.followupStatus)" /></el-form-item>
        <el-form-item label="诊断"><el-input :model-value="detail.diagnosis || '—'" /></el-form-item>
        <el-form-item label="随访内容"><el-input :model-value="detail.followupContent" type="textarea" :rows="3" /></el-form-item>
        <el-form-item v-if="detail.executeResult" label="随访结果">
          <el-input :model-value="detail.executeResult" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="detail.executorName" label="执行人">
          <el-input :model-value="`${detail.executorName} ${fmtTime(detail.executeTime)}`" />
        </el-form-item>
        <el-form-item v-if="detail.revisitAppointId" label="复诊号">
          <el-input :model-value="String(detail.revisitAppointId)" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <!-- 新建/编辑 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑随访任务' : '新建随访任务'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="患者" required>
          <PatientSelect v-model="editForm.patientId as any" :disabled="!!editForm.id" @select="onPatientSelect" />
        </el-form-item>
        <el-form-item label="随访类型" required>
          <el-select v-model="editForm.followupType" style="width: 220px" :fit-input-width="false">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划时间" required>
          <el-date-picker v-model="editForm.followupTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                          placeholder="计划随访时间" />
        </el-form-item>
        <el-form-item label="随访内容" required>
          <el-input v-model="editForm.followupContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" placeholder="不填自动取患者档案电话" style="width: 220px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button v-perm="['inpatient:followup:add','inpatient:followup:edit']" type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 按出院记录生成 -->
    <el-dialog v-model="genVisible" title="按出院记录生成随访计划" width="520px">
      <el-form label-width="90px">
        <el-form-item label="出院记录ID" required>
          <el-input v-model="genForm.dischargeId" placeholder="出院记录ID（biz_discharge）" />
        </el-form-item>
        <el-form-item label="随访类型">
          <el-select v-model="genForm.followupType" style="width: 220px" :fit-input-width="false">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访天数">
          <el-input-number v-model="genForm.daysOffset" :min="1" :precision="0" />
          <span class="hint">计划时间 = 出院时间 + N 天（默认 7）</span>
        </el-form-item>
        <el-form-item label="内容覆盖">
          <el-input v-model="genForm.followupContent" type="textarea" :rows="2" placeholder="留空则按出院诊断自动生成" />
        </el-form-item>
      </el-form>
      <div class="text-xs text-gray-400">
        日常不需要手工点这个：出院办理会自动建一条（有手术记录 → 术后随访，否则复诊提醒；死亡病例不建）。
        这里服务的是自动建单失败后的补录，以及慢病/用药指导这类额外计划。
      </div>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button v-perm="'inpatient:followup:add'" type="primary" :loading="genLoading" @click="submitGenerate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 完成随访 -->
    <el-dialog v-model="completeVisible" title="完成随访" width="480px">
      <el-input v-model="completeResult" type="textarea" :rows="3" placeholder="随访结果（接通情况/患者反馈/处理）" />
      <div class="text-xs text-gray-400 mt-2">
        提交后系统会按当前启用的「出院随访」问卷自动发放一张满意度评价单，随访员在同一次通话里回收。
        没有启用中的问卷时随访照常完成，可在列表「补发问卷」人工兜底。
      </div>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">提交</el-button>
      </template>
    </el-dialog>

    <!-- 补发满意度问卷 -->
    <el-dialog v-model="issueVisible" title="补发满意度问卷" width="480px">
      <el-form label-width="90px">
        <el-form-item label="随访任务"><el-input :model-value="issueForm.taskNo" disabled /></el-form-item>
        <el-form-item label="回收渠道">
          <el-select v-model="issueForm.channel" style="width: 200px" :fit-input-width="false">
            <el-option :value="1" label="电话代填（唯一实测可回收）" />
            <el-option :value="2" label="短信（无网关，只落状态）" />
            <el-option :value="3" label="微信（无网关，只落状态）" />
            <el-option :value="4" label="现场扫码" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期">
          <el-input-number v-model="issueForm.expireDays" :min="1" :precision="0" />
          <span class="hint">天后过期（默认 14）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="issueLoading" @click="submitIssue">发放</el-button>
      </template>
    </el-dialog>

    <!-- 生成复诊号（来源 4）：与医生站「预约复诊」共用同一个弹框（选原病历 + 号源 + 当场看金额） -->
    <RevisitAppointDialog
        v-model="revisitVisible"
        :patient-id="revisitTask?.patientId ?? null"
        :patient-name="revisitTask?.patientName"
        :revisit-source="REVISIT_SOURCE.FOLLOWUP_PLAN"
        :default-record-id="revisitTask?.revisitRecordId ?? null"
        :submitting="revisitBusy"
        @submitted="submitRevisit"
    />
  </div>
</template>

<style scoped>
.followup-page { display: flex; flex-direction: column; gap: 12px; }
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-row { display: flex; gap: 28px; flex-wrap: wrap; }
.stat-item { display: flex; flex-direction: column; gap: 4px; }
.stat-item.clickable { cursor: pointer; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 20px; font-weight: 600; color: #303133; }
.stat-value.warn { color: #e6a23c; }
.stat-value.ok { color: #67c23a; }
.stat-value.danger { color: #f56c6c; }
.stat-foot { margin-top: 10px; }
.filter-bar { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; align-items: center; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
.overdue-text { color: #f56c6c; font-weight: 600; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.block-title { font-weight: 600; color: #303133; margin-bottom: 8px; }
.hint { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
