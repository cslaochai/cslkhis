<script setup lang="ts">
/**
 * 线上问诊（菜单 2929，sql/184；原为 315 互联网医院的第二个页签）
 *
 * 独立的一条链：发起（待接诊）→ 接诊（接诊中）→ 回复并结束（已完成）/ 退诊（已退诊）。
 *
 * 拆分口径：线上问诊（互联网医院面向患者的轻问诊单）与远程会诊（院际会诊单）是**两个实体、
 * 两套完全独立的接口**（onlineConsult* ↔ teleConsult*）、两条互不依赖的状态机，
 * 运营岗位也不同（线上问诊客服/互联网诊室 ↔ 远程会诊医务协调）。
 * 拆开后 315 更名「远程会诊」只留会诊链，本页独立挂菜单。
 *
 * 口径：
 * 1. **按钮可用性一律读后端 can* 字段**，不按 status 码值 switch；
 *    「待接诊才可接诊、接诊中才可回复」是后端铁律，前端只在能点时给按钮。
 * 2. 费用 fee 只是**价目快照**，这里不计费 —— 计费走 charge 域，避免双计。
 * 3. 统计数字仍取 getTeleConsultStat 的 online* 字段（同源接口，拆页不拆数据）。
 * 4. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  listOnlineConsultPage, getOnlineConsultDetail, applyOnlineConsult, acceptOnlineConsult,
  replyOnlineConsult, rejectOnlineConsult, deleteOnlineConsult, getTeleConsultStat,
} from '@/api/teleconsult'
import { getDepartmentSelectList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import PatientSelect from '@/components/his/PatientSelect.vue'

const stats = reactive({
  onlineTotal: 0, onlineWaiting: 0, onlineAccepted: 0, onlineDone: 0, onlineRejected: 0,
})

const onlineTypeDict = ref<any[]>([])
const onlineStatusDict = ref<any[]>([])
const deptOptions = ref<any[]>([])

const onlineTypeText = (v: any) => dictLabelText(onlineTypeDict.value, v)
const onlineStatusText = (v: any) => dictLabelText(onlineStatusDict.value, v)

const onlineStatusTag = (v: any) => {
  switch (Number(v)) {
    case 1: return 'warning'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'info'
    default: return 'info'
  }
}

// ---------------- 问诊列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  keyword: '', consultType: null as number | null, status: null as number | null,
  deptId: null as any, waitingOnly: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listOnlineConsultPage({
      keyword: query.keyword.trim() || undefined,
      consultType: query.consultType ?? undefined,
      status: query.status ?? undefined,
      deptId: query.deptId || undefined,
      waitingOnly: query.waitingOnly || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.consultType = null
  query.status = null
  query.deptId = null
  query.waitingOnly = false
  query.pageNum = 1
  loadList()
}

// ---------------- 发起问诊 ----------------
const formVisible = ref(false)
const saving = ref(false)
const form = reactive({
  patientId: null as string | null,
  patientName: '',
  deptId: null as any,
  consultType: 1 as number,
  chiefComplaint: '',
  fee: null as number | null,
})

const openCreate = () => {
  Object.assign(form, {
    patientId: null, patientName: '', deptId: null, consultType: 1, chiefComplaint: '', fee: null,
  })
  formVisible.value = true
}

const onPatientSelect = (p: any) => {
  form.patientId = p?.id ?? null
  form.patientName = p?.name || p?.patientName || ''
}

const save = async () => {
  if (!form.patientId) { ElMessage.warning('请选择患者'); return }
  if (!form.chiefComplaint.trim()) { ElMessage.warning('请填写主诉/问题描述'); return }
  saving.value = true
  try {
    const res: any = await applyOnlineConsult({
      patientId: form.patientId,
      deptId: form.deptId || undefined,
      consultType: form.consultType,
      chiefComplaint: form.chiefComplaint.trim(),
      fee: form.fee ?? undefined,
    })
    if (res.code === 200) {
      ElMessage.success(`问诊已发起，单号 ${res.data?.consultNo || ''}`)
      formVisible.value = false
      loadList(); loadStats()
    } else {
      ElMessage.error(res.message || '发起失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('发起失败')
  } finally {
    saving.value = false
  }
}

const doAccept = async (row: any) => {
  try {
    const res: any = await acceptOnlineConsult(row.id)
    if (res.code === 200) { ElMessage.success('已接诊'); loadList(); loadStats() }
    else ElMessage.error(res.message || '接诊失败')
  } catch (e) { console.error(e); ElMessage.error('接诊失败') }
}

// ---------------- 回复并结束 ----------------
const replyVisible = ref(false)
const replyRow = ref<any>(null)
const replyForm = reactive({ reply: '', advice: '', needVisit: 0 as number })
const replyLoading = ref(false)

const openReply = (row: any) => {
  replyRow.value = row
  replyForm.reply = ''
  replyForm.advice = ''
  replyForm.needVisit = 0
  replyVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.reply.trim()) { ElMessage.warning('请填写回复内容'); return }
  replyLoading.value = true
  try {
    const res: any = await replyOnlineConsult({
      id: replyRow.value.id,
      reply: replyForm.reply.trim(),
      advice: replyForm.advice.trim() || undefined,
      needVisit: replyForm.needVisit ?? 0,
    })
    if (res.code === 200) {
      ElMessage.success('已回复并结束')
      replyVisible.value = false
      loadList(); loadStats()
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

// ---------------- 退诊 ----------------
const rejectVisible = ref(false)
const rejectRow = ref<any>(null)
const rejectReason = ref('')
const rejectLoading = ref(false)

const openReject = (row: any) => {
  rejectRow.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请填写退诊原因'); return }
  rejectLoading.value = true
  try {
    const res: any = await rejectOnlineConsult({ id: rejectRow.value.id, content: rejectReason.value.trim() })
    if (res.code === 200) {
      ElMessage.success('已退诊')
      rejectVisible.value = false
      loadList(); loadStats()
    } else {
      ElMessage.error(res.message || '退诊失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('退诊失败')
  } finally {
    rejectLoading.value = false
  }
}

const remove = async (row: any) => {
  try { await ElMessageBox.confirm(`确认删除问诊单 ${row.consultNo}？`, '删除确认', { type: 'warning' }) } catch { return }
  try {
    const res: any = await deleteOnlineConsult(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadList(); loadStats() }
    else ElMessage.error(res.message || '删除失败')
  } catch (e) { console.error(e); ElMessage.error('删除失败') }
}

// ---------------- 详情 ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  const res: any = await getOnlineConsultDetail(row.id)
  if (res.code === 200) { detail.value = res.data; detailVisible.value = true }
  else ElMessage.error(res.message || '加载详情失败')
}

// ---------------- 公共 ----------------
const loadStats = async () => {
  try {
    const res: any = await getTeleConsultStat()
    if (res.code === 200 && res.data) {
      stats.onlineTotal = res.data.onlineTotal ?? 0
      stats.onlineWaiting = res.data.onlineWaiting ?? 0
      stats.onlineAccepted = res.data.onlineAccepted ?? 0
      stats.onlineDone = res.data.onlineDone ?? 0
      stats.onlineRejected = res.data.onlineRejected ?? 0
    }
  } catch (e) { console.error('加载统计失败', e) }
}

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList([
      DICT_TYPE.ONLINE_CONSULT_TYPE, DICT_TYPE.ONLINE_CONSULT_STATUS,
    ].join(','))
    const d = res?.data || {}
    onlineTypeDict.value = d[DICT_TYPE.ONLINE_CONSULT_TYPE] || []
    onlineStatusDict.value = d[DICT_TYPE.ONLINE_CONSULT_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
}

const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({})
    if (res.code === 200) deptOptions.value = res.data || []
  } catch (e) { console.error('加载科室失败', e) }
}

onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts()])
  await loadList()
  await loadStats()
})
</script>

<template>
  <div>
    <el-card shadow="never" class="stat-card mb-3">
      <div class="stat-row">
        <div class="stat-item"><span class="stat-label">线上问诊总数</span><span class="stat-value">{{ stats.onlineTotal }}</span></div>
        <div class="stat-item"><span class="stat-label">待接诊</span><span class="stat-value warn">{{ stats.onlineWaiting }}</span></div>
        <div class="stat-item"><span class="stat-label">接诊中</span><span class="stat-value">{{ stats.onlineAccepted }}</span></div>
        <div class="stat-item"><span class="stat-label">已完成</span><span class="stat-value ok">{{ stats.onlineDone }}</span></div>
        <div class="stat-item"><span class="stat-label">已退诊</span><span class="stat-value">{{ stats.onlineRejected }}</span></div>
      </div>
    </el-card>

    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="单号 / 患者" clearable style="width: 200px"
              @keyup.enter="loadList" />
          </el-form-item>
          <el-form-item label="问诊方式">
            <el-select v-model="query.consultType" placeholder="问诊方式" clearable style="width: 140px">
              <el-option v-for="d in onlineTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
              <el-option v-for="d in onlineStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="接诊科室">
            <el-select v-model="query.deptId" placeholder="接诊科室" clearable filterable style="width: 170px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.waitingOnly">仅待接诊</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:teleconsult:add'" type="primary" :icon="Plus" @click="openCreate">发起问诊</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" @row-click="openDetail">
        <el-table-column prop="consultNo" label="问诊单号" width="150" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column label="方式" width="100">
          <template #default="{ row }">{{ onlineTypeText(row.consultType) }}</template>
        </el-table-column>
        <el-table-column prop="deptName" label="接诊科室" width="140" show-overflow-tooltip />
        <el-table-column prop="doctorName" label="接诊医生" width="100" />
        <el-table-column prop="chiefComplaint" label="主诉/问题描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="发起时间" width="160" />
        <el-table-column label="建议线下" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="Number(row.needVisit) === 1" size="small" type="warning">是</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="onlineStatusTag(row.status)">{{ onlineStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.canAccept" v-perm="'ipd:teleconsult:edit'" link type="primary" size="small"
              @click.stop="doAccept(row)">接诊</el-button>
            <el-button v-if="row.canReply" v-perm="'ipd:teleconsult:edit'" link type="success" size="small"
              @click.stop="openReply(row)">回复并结束</el-button>
            <el-button v-if="row.canReject" v-perm="'ipd:teleconsult:edit'" link type="info" size="small"
              @click.stop="openReject(row)">退诊</el-button>
            <el-button v-if="row.canDelete" v-perm="'ipd:teleconsult:add'" link type="danger" size="small"
              @click.stop="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无线上问诊" /></template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 发起问诊 -->
    <el-dialog v-model="formVisible" title="发起线上问诊" width="620px">
      <el-form label-width="100px" size="small">
        <el-form-item label="患者" required>
          <PatientSelect :model-value="form.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
            @select="onPatientSelect" />
        </el-form-item>
        <el-form-item label="接诊科室">
          <el-select v-model="form.deptId" filterable clearable placeholder="请选择" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问诊方式" required>
          <el-radio-group v-model="form.consultType">
            <el-radio v-for="d in onlineTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="主诉/问题" required>
          <el-input v-model="form.chiefComplaint" type="textarea" :rows="4" placeholder="患者描述的主要问题" />
        </el-form-item>
        <el-form-item label="问诊费用">
          <el-input-number v-model="form.fee" :min="0" :precision="2" />
          <span class="hint">价目快照，此处不计费</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">发起</el-button>
      </template>
    </el-dialog>

    <!-- 回复 -->
    <el-dialog v-model="replyVisible" title="回复并结束问诊" width="620px">
      <el-form label-width="110px" size="small">
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.reply" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="处置建议">
          <el-input v-model="replyForm.advice" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="建议线下就诊">
          <el-radio-group v-model="replyForm.needVisit">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">提交并结束</el-button>
      </template>
    </el-dialog>

    <!-- 退诊 -->
    <el-dialog v-model="rejectVisible" title="退诊" width="560px">
      <el-form label-width="100px" size="small">
        <el-form-item label="退诊原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="原因必填（如：非本科室诊疗范围）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" :loading="rejectLoading" @click="submitReject">确定退诊</el-button>
      </template>
    </el-dialog>

    <!-- 问诊详情 -->
    <el-dialog v-model="detailVisible" title="线上问诊详情" width="700px">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="问诊单号">{{ detail.consultNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="onlineStatusTag(detail.status)">{{ onlineStatusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }}（{{ detail.patientNo || '—' }}）</el-descriptions-item>
        <el-descriptions-item label="问诊方式">{{ onlineTypeText(detail.consultType) }}</el-descriptions-item>
        <el-descriptions-item label="接诊科室">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="接诊医生">{{ detail.doctorName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ detail.applyTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detail.finishTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="主诉/问题" :span="2">{{ detail.chiefComplaint }}</el-descriptions-item>
        <el-descriptions-item label="医生回复" :span="2">{{ detail.reply || '—' }}</el-descriptions-item>
        <el-descriptions-item label="处置建议" :span="2">{{ detail.advice || '—' }}</el-descriptions-item>
        <el-descriptions-item label="建议线下就诊" :span="2">
          <el-tag v-if="Number(detail.needVisit) === 1" size="small" type="warning">是</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.rejectReason" label="退诊原因" :span="2">{{ detail.rejectReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-row { display: flex; gap: 28px; flex-wrap: wrap; }
.stat-item { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 20px; font-weight: 600; color: #303133; }
.stat-value.warn { color: #e6a23c; }
.stat-value.ok { color: #67c23a; }
.hint { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
