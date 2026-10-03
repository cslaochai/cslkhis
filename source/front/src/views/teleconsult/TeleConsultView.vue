<script setup lang="ts">
/**
 * 远程会诊（菜单 315，sql/184 由「互联网医院 / 远程会诊」更名）
 *
 * 独立的一条链：申请（待安排）→ 安排（已安排，定时间与接入方式）→ 出意见完成（已完成）/ 取消
 *
 * 线上问诊已拆为独立菜单 2929「线上问诊」（sql/184）：两者是两个实体、两套接口
 * （teleConsult* ↔ onlineConsult*）、两条互不依赖的状态机，统计仍同源 getTeleConsultStat。
 *
 * 四条口径：
 * 1. **按钮可用性一律读后端 can* 字段**，不按 status 码值 switch。
 * 2. **已安排才可出意见**是后端铁律，前端只在能点时给按钮，不在本地拦截
 *    （本地拦截会掩盖后端规则的失效）。
 * 3. 费用 fee 只是**价目快照**，这里不计费 —— 计费走 charge 域，避免双计。
 * 4. 平台 platform / 接入号 meet_no 是**预留字段**：真实视频平台对接时只换成"平台开号回写"，
 *    不改表结构、不改页面。
 * 5. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Connection } from '@element-plus/icons-vue'
import {
  listTeleConsultPage, getTeleConsultDetail, teleConsultUpsert, arrangeTeleConsult,
  completeTeleConsult, cancelTeleConsult, deleteTeleConsult, getTeleConsultStat,
} from '@/api/teleconsult'
import { getDepartmentSelectList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import PatientSelect from '@/components/his/PatientSelect.vue'

const stats = reactive({
  teleTotal: 0, telePending: 0, teleArranged: 0, teleDone: 0, teleCanceled: 0,
})

const teleTypeDict = ref<any[]>([])
const teleStatusDict = ref<any[]>([])
const deptOptions = ref<any[]>([])

const teleTypeText = (v: any) => dictLabelText(teleTypeDict.value, v)
const teleStatusText = (v: any) => dictLabelText(teleStatusDict.value, v)

const teleStatusTag = (v: any) => {
  switch (Number(v)) {
    case 1: return 'warning'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'info'
    default: return 'info'
  }
}

// ---------------- 远程会诊 ----------------
const teleLoading = ref(false)
const teleRows = ref<any[]>([])
const teleTotal = ref(0)
const teleQuery = reactive({
  keyword: '', consultType: null as number | null, status: null as number | null,
  urgentOnly: false, openOnly: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadTele = async () => {
  teleLoading.value = true
  try {
    const res: any = await listTeleConsultPage({
      keyword: teleQuery.keyword.trim() || undefined,
      consultType: teleQuery.consultType ?? undefined,
      status: teleQuery.status ?? undefined,
      urgentOnly: teleQuery.urgentOnly || undefined,
      openOnly: teleQuery.openOnly || undefined,
      pageNum: teleQuery.pageNum, pageSize: teleQuery.pageSize,
    })
    if (res.code === 200) {
      teleRows.value = res.data?.records || []
      teleTotal.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询失败')
  } finally {
    teleLoading.value = false
  }
}

const resetTele = () => {
  teleQuery.keyword = ''
  teleQuery.consultType = null
  teleQuery.status = null
  teleQuery.urgentOnly = false
  teleQuery.openOnly = false
  teleQuery.pageNum = 1
  loadTele()
}

const teleFormVisible = ref(false)
const teleSaving = ref(false)
const teleForm = reactive({
  id: null as string | null,
  patientId: null as string | null,
  patientName: '',
  applyDeptId: null as any,
  consultType: 1 as number,
  isUrgent: 0 as number,
  expertHospital: '',
  expertDept: '',
  expertName: '',
  expertTitle: '',
  purpose: '',
  diagnosis: '',
  fee: null as number | null,
  remark: '',
})

const openTeleCreate = () => {
  Object.assign(teleForm, {
    id: null, patientId: null, patientName: '', applyDeptId: null, consultType: 1, isUrgent: 0,
    expertHospital: '', expertDept: '', expertName: '', expertTitle: '', purpose: '',
    diagnosis: '', fee: null, remark: '',
  })
  teleFormVisible.value = true
}

const openTeleEdit = async (row: any) => {
  const res: any = await getTeleConsultDetail(row.id)
  const d = res?.data || row
  Object.assign(teleForm, {
    id: d.id, patientId: d.patientId ?? null, patientName: d.patientName || '',
    applyDeptId: d.applyDeptId ?? null, consultType: Number(d.consultType ?? 1),
    isUrgent: Number(d.isUrgent ?? 0), expertHospital: d.expertHospital || '',
    expertDept: d.expertDept || '', expertName: d.expertName || '', expertTitle: d.expertTitle || '',
    purpose: d.purpose || '', diagnosis: d.diagnosis || '', fee: d.fee ?? null, remark: d.remark || '',
  })
  teleFormVisible.value = true
}

const onTelePatientSelect = (p: any) => {
  teleForm.patientId = p?.id ?? null
  teleForm.patientName = p?.name || p?.patientName || ''
}

const saveTele = async () => {
  if (!teleForm.patientId) { ElMessage.warning('请选择患者'); return }
  if (!teleForm.purpose.trim()) { ElMessage.warning('请填写会诊目的'); return }
  teleSaving.value = true
  try {
    const res: any = await teleConsultUpsert({
      id: teleForm.id || undefined,
      patientId: teleForm.patientId,
      applyDeptId: teleForm.applyDeptId || undefined,
      consultType: teleForm.consultType,
      isUrgent: teleForm.isUrgent,
      expertHospital: teleForm.expertHospital.trim() || undefined,
      expertDept: teleForm.expertDept.trim() || undefined,
      expertName: teleForm.expertName.trim() || undefined,
      expertTitle: teleForm.expertTitle.trim() || undefined,
      purpose: teleForm.purpose.trim(),
      diagnosis: teleForm.diagnosis.trim() || undefined,
      fee: teleForm.fee ?? undefined,
      remark: teleForm.remark.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(teleForm.id ? '修改成功' : `会诊申请已提交，单号 ${res.data?.consultNo || ''}`)
      teleFormVisible.value = false
      loadTele(); loadStats()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('保存失败')
  } finally {
    teleSaving.value = false
  }
}

// 安排
const arrangeVisible = ref(false)
const arrangeRow = ref<any>(null)
const arrangeForm = reactive({
  planTime: '', durationMin: null as number | null, platform: '', meetNo: '',
  expertHospital: '', expertDept: '', expertName: '', expertTitle: '',
})
const arrangeLoading = ref(false)

const openArrange = (row: any) => {
  arrangeRow.value = row
  arrangeForm.planTime = ''
  arrangeForm.durationMin = 30
  arrangeForm.platform = row.platform || ''
  arrangeForm.meetNo = row.meetNo || ''
  arrangeForm.expertHospital = row.expertHospital || ''
  arrangeForm.expertDept = row.expertDept || ''
  arrangeForm.expertName = row.expertName || ''
  arrangeForm.expertTitle = row.expertTitle || ''
  arrangeVisible.value = true
}

const submitArrange = async () => {
  if (!arrangeForm.planTime) { ElMessage.warning('请选择计划会诊时间'); return }
  arrangeLoading.value = true
  try {
    const res: any = await arrangeTeleConsult({
      id: arrangeRow.value.id,
      planTime: arrangeForm.planTime.length === 16 ? `${arrangeForm.planTime}:00` : arrangeForm.planTime,
      durationMin: arrangeForm.durationMin ?? undefined,
      platform: arrangeForm.platform.trim() || undefined,
      meetNo: arrangeForm.meetNo.trim() || undefined,
      expertHospital: arrangeForm.expertHospital.trim() || undefined,
      expertDept: arrangeForm.expertDept.trim() || undefined,
      expertName: arrangeForm.expertName.trim() || undefined,
      expertTitle: arrangeForm.expertTitle.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('已安排')
      arrangeVisible.value = false
      loadTele(); loadStats()
    } else {
      ElMessage.error(res.message || '安排失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('安排失败')
  } finally {
    arrangeLoading.value = false
  }
}

// 完成 / 取消（共用内容弹窗）
const teleActVisible = ref(false)
const teleActKey = ref<'complete' | 'cancel'>('complete')
const teleActRow = ref<any>(null)
const teleActContent = ref('')
const teleActLoading = ref(false)

const openTeleAct = (row: any, key: 'complete' | 'cancel') => {
  teleActRow.value = row
  teleActKey.value = key
  teleActContent.value = ''
  teleActVisible.value = true
}

const submitTeleAct = async () => {
  if (!teleActContent.value.trim()) {
    ElMessage.warning(teleActKey.value === 'complete' ? '请填写会诊意见' : '请填写取消原因')
    return
  }
  teleActLoading.value = true
  try {
    const res: any = teleActKey.value === 'complete'
      ? await completeTeleConsult({ id: teleActRow.value.id, content: teleActContent.value.trim() })
      : await cancelTeleConsult({ id: teleActRow.value.id, content: teleActContent.value.trim() })
    if (res.code === 200) {
      ElMessage.success(teleActKey.value === 'complete' ? '会诊已完成' : '已取消')
      teleActVisible.value = false
      loadTele(); loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('操作失败')
  } finally {
    teleActLoading.value = false
  }
}

const removeTele = async (row: any) => {
  try { await ElMessageBox.confirm(`确认删除会诊单 ${row.consultNo}？`, '删除确认', { type: 'warning' }) } catch { return }
  try {
    const res: any = await deleteTeleConsult(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadTele(); loadStats() }
    else ElMessage.error(res.message || '删除失败')
  } catch (e) { console.error(e); ElMessage.error('删除失败') }
}

const teleDetailVisible = ref(false)
const teleDetail = ref<any>(null)
const openTeleDetail = async (row: any) => {
  const res: any = await getTeleConsultDetail(row.id)
  if (res.code === 200) { teleDetail.value = res.data; teleDetailVisible.value = true }
  else ElMessage.error(res.message || '加载详情失败')
}

// ---------------- 公共 ----------------
const loadStats = async () => {
  try {
    const res: any = await getTeleConsultStat()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) { console.error('加载统计失败', e) }
}

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList([
      DICT_TYPE.TELE_CONSULT_TYPE, DICT_TYPE.TELE_CONSULT_STATUS,
    ].join(','))
    const d = res?.data || {}
    teleTypeDict.value = d[DICT_TYPE.TELE_CONSULT_TYPE] || []
    teleStatusDict.value = d[DICT_TYPE.TELE_CONSULT_STATUS] || []
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
  await loadTele()
  await loadStats()
})
</script>

<template>
  <div>
    <el-card shadow="never" class="stat-card mb-3">
      <div class="stat-row">
        <div class="stat-item"><span class="stat-label">远程会诊总数</span><span class="stat-value">{{ stats.teleTotal }}</span></div>
        <div class="stat-item"><span class="stat-label">待安排</span><span class="stat-value warn">{{ stats.telePending }}</span></div>
        <div class="stat-item"><span class="stat-label">已安排</span><span class="stat-value">{{ stats.teleArranged }}</span></div>
        <div class="stat-item"><span class="stat-label">已完成</span><span class="stat-value ok">{{ stats.teleDone }}</span></div>
        <div class="stat-item"><span class="stat-label">已取消</span><span class="stat-value">{{ stats.teleCanceled }}</span></div>
      </div>
    </el-card>

    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="teleQuery" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="teleQuery.keyword" placeholder="单号 / 患者 / 专家" clearable style="width: 200px"
              @keyup.enter="loadTele" />
          </el-form-item>
          <el-form-item label="会诊类型">
            <el-select v-model="teleQuery.consultType" placeholder="会诊类型" clearable style="width: 140px">
              <el-option v-for="d in teleTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="teleQuery.status" placeholder="状态" clearable style="width: 130px">
              <el-option v-for="d in teleStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="teleQuery.urgentOnly">仅急会诊</el-checkbox>
            <el-checkbox v-model="teleQuery.openOnly">仅未完成</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadTele">查询</el-button>
            <el-button :icon="Refresh" @click="resetTele">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:teleconsult:add'" type="primary" :icon="Plus" @click="openTeleCreate">申请会诊</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="teleLoading" :data="teleRows" stripe :max-height="tableMaxHeight" @row-click="openTeleDetail">
            <el-table-column prop="consultNo" label="会诊单号" width="150" />
            <el-table-column prop="patientName" label="患者" width="100" />
            <el-table-column label="类型" width="110">
              <template #default="{ row }">{{ teleTypeText(row.consultType) }}</template>
            </el-table-column>
            <el-table-column label="急" width="60" align="center">
              <template #default="{ row }">
                <el-tag v-if="Number(row.isUrgent) === 1" size="small" type="danger">急</el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column prop="applyDeptName" label="申请科室" width="140" show-overflow-tooltip />
            <el-table-column prop="expertHospital" label="专家医院" width="150" show-overflow-tooltip />
            <el-table-column prop="expertName" label="专家" width="100" />
            <el-table-column prop="purpose" label="会诊目的" min-width="180" show-overflow-tooltip />
            <el-table-column prop="planTime" label="计划时间" width="160" />
            <el-table-column prop="meetNo" label="接入号" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="teleStatusTag(row.status)">{{ teleStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.canArrange" v-perm="'ipd:teleconsult:edit'" link type="primary" size="small"
                  @click.stop="openArrange(row)">安排</el-button>
                <el-button v-if="row.canComplete" v-perm="'ipd:teleconsult:edit'" link type="success" size="small"
                  @click.stop="openTeleAct(row, 'complete')">出意见完成</el-button>
                <el-button v-if="row.canCancel" v-perm="'ipd:teleconsult:edit'" link type="info" size="small"
                  @click.stop="openTeleAct(row, 'cancel')">取消</el-button>
                <el-button v-if="row.canEdit" v-perm="'ipd:teleconsult:add'" link type="primary" size="small"
                  @click.stop="openTeleEdit(row)">编辑</el-button>
                <el-button v-if="row.canDelete" v-perm="'ipd:teleconsult:add'" link type="danger" size="small"
                  @click.stop="removeTele(row)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="暂无远程会诊申请" /></template>
          </el-table>

          <div ref="footerRef" class="list-footer flex items-center justify-end">
            <el-pagination v-model:current-page="teleQuery.pageNum" v-model:page-size="teleQuery.pageSize"
              :page-sizes="PAGE_SIZES" :total="teleTotal" layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadTele" @current-change="loadTele" />
          </div>
    </el-card>

    <!-- 远程会诊申请 / 修改 -->
    <el-dialog v-model="teleFormVisible" :title="teleForm.id ? '修改会诊申请' : '申请远程会诊'" width="720px">
      <el-form label-width="110px" size="small">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="患者" required>
              <PatientSelect :model-value="teleForm.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                @select="onTelePatientSelect" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请科室">
              <el-select v-model="teleForm.applyDeptId" filterable clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="会诊类型" required>
              <el-select v-model="teleForm.consultType" style="width: 100%">
                <el-option v-for="d in teleTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否急会诊">
              <el-radio-group v-model="teleForm.isUrgent">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="专家医院">
              <el-input v-model="teleForm.expertHospital" placeholder="院内会诊填本院" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专家科室">
              <el-input v-model="teleForm.expertDept" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="专家姓名">
              <el-input v-model="teleForm.expertName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专家职称">
              <el-input v-model="teleForm.expertTitle" placeholder="如：主任医师" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="会诊目的" required>
          <el-input v-model="teleForm.purpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="病情摘要">
          <el-input v-model="teleForm.diagnosis" type="textarea" :rows="2" placeholder="申请方诊断 / 病情摘要" />
        </el-form-item>
        <el-form-item label="会诊费用">
          <el-input-number v-model="teleForm.fee" :min="0" :precision="2" />
          <span class="hint">价目快照，此处不计费</span>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="teleForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teleFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="teleSaving" @click="saveTele">保存</el-button>
      </template>
    </el-dialog>

    <!-- 安排 -->
    <el-dialog v-model="arrangeVisible" title="安排会诊" width="600px">
      <el-form label-width="120px" size="small">
        <el-form-item label="计划会诊时间" required>
          <el-date-picker v-model="arrangeForm.planTime" type="datetime" value-format="YYYY-MM-DD HH:mm"
            placeholder="请选择" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划时长(分钟)">
          <el-input-number v-model="arrangeForm.durationMin" :min="5" :max="480" />
        </el-form-item>
        <el-form-item label="对接平台">
          <el-input v-model="arrangeForm.platform" placeholder="预留字段：真实视频平台对接后回写" />
        </el-form-item>
        <el-form-item label="接入号/会议室">
          <el-input v-model="arrangeForm.meetNo" placeholder="预留字段：平台开号后回写" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="专家医院"><el-input v-model="arrangeForm.expertHospital" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专家科室"><el-input v-model="arrangeForm.expertDept" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="专家姓名"><el-input v-model="arrangeForm.expertName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专家职称"><el-input v-model="arrangeForm.expertTitle" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="arrangeVisible = false">取消</el-button>
        <el-button type="primary" :icon="Connection" :loading="arrangeLoading" @click="submitArrange">安排</el-button>
      </template>
    </el-dialog>

    <!-- 完成 / 取消 -->
    <el-dialog v-model="teleActVisible" :title="teleActKey === 'complete' ? '填写会诊意见' : '取消会诊'" width="600px">
      <el-form label-width="100px" size="small">
        <el-form-item :label="teleActKey === 'complete' ? '会诊意见' : '取消原因'" required>
          <el-input v-model="teleActContent" type="textarea" :rows="5"
            :placeholder="teleActKey === 'complete' ? '会诊意见（完成后为终态，不可再改）' : '取消原因（必填，将留痕）'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teleActVisible = false">取消</el-button>
        <el-button type="primary" :loading="teleActLoading" @click="submitTeleAct">确定</el-button>
      </template>
    </el-dialog>

    <!-- 远程会诊详情 -->
    <el-dialog v-model="teleDetailVisible" title="远程会诊详情" width="760px">
      <el-descriptions v-if="teleDetail" :column="2" border size="small">
        <el-descriptions-item label="会诊单号">{{ teleDetail.consultNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="teleStatusTag(teleDetail.status)">{{ teleStatusText(teleDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ teleDetail.patientName }}（{{ teleDetail.patientNo || '—' }}）</el-descriptions-item>
        <el-descriptions-item label="会诊类型">{{ teleTypeText(teleDetail.consultType) }}</el-descriptions-item>
        <el-descriptions-item label="申请科室">{{ teleDetail.applyDeptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="申请医生">{{ teleDetail.applyDoctor || '—' }}</el-descriptions-item>
        <el-descriptions-item label="专家医院">{{ teleDetail.expertHospital || '—' }}</el-descriptions-item>
        <el-descriptions-item label="专家">{{ teleDetail.expertName || '—' }} {{ teleDetail.expertTitle || '' }}</el-descriptions-item>
        <el-descriptions-item label="计划时间">{{ teleDetail.planTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="接入号">{{ teleDetail.meetNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="费用(元)">{{ teleDetail.fee ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ teleDetail.completeTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="会诊目的" :span="2">{{ teleDetail.purpose }}</el-descriptions-item>
        <el-descriptions-item label="病情摘要" :span="2">{{ teleDetail.diagnosis || '—' }}</el-descriptions-item>
        <el-descriptions-item label="会诊意见" :span="2">{{ teleDetail.opinion || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="teleDetail.cancelReason" label="取消原因" :span="2">{{ teleDetail.cancelReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>
</template>

<style scoped>
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-row { display: flex; gap: 28px; flex-wrap: wrap; align-items: flex-end; }
.stat-item { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 20px; font-weight: 600; color: #303133; }
.stat-value.warn { color: #e6a23c; }
.stat-value.ok { color: #67c23a; }
.stat-sep { width: 1px; height: 34px; background: #e4e7ed; }
.hint { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
