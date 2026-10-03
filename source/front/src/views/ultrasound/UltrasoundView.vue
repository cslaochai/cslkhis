<script setup lang="ts">
/**
 * 超声工作站（G17，菜单 409）
 *
 * 流程：登记 → 签到 → 执行 → 结构化测量值（整单覆盖保存，异常标志服务端判定）
 * → 报告（所见 + 提示）→ 审核（服务端硬校验审核人 ≠ 报告人）→ 发布。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import {
  getUltrasoundListPage, getUltrasoundStats, getUltrasoundDetail, ultrasoundUpsert,
  ultrasoundCheckIn, ultrasoundExecute, ultrasoundSaveMeasures, ultrasoundReport,
  ultrasoundAudit, ultrasoundPublish, ultrasoundCancel,
} from '@/api/medicaltech'
import { getDictDataMapList } from '@/api/system'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  recordNo: '', patientName: '', usType: null as number | null, status: null as number | null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const stats = reactive({ pending: 0, examining: 0, pendingAudit: 0, published: 0, todayCount: 0 })

const statusDict = ref<any[]>([])
const usTypeDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const usTypeText = (v: any) => dictLabelText(usTypeDict.value, v)
const statusTag = (v: any) => {
  const n = Number(v)
  if (n === 6) return 'success'
  if (n === 7) return 'info'
  if (n === 5) return 'warning'
  return 'primary'
}

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.ENDOUS_STATUS},${DICT_TYPE.ULTRASOUND_TYPE}`)
    statusDict.value = res?.data?.[DICT_TYPE.ENDOUS_STATUS] || []
    usTypeDict.value = res?.data?.[DICT_TYPE.ULTRASOUND_TYPE] || []
  } catch (e) { console.error('加载字典失败', e) }
}

const loadStats = async () => {
  try {
    const res: any = await getUltrasoundStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) { console.error(e) }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getUltrasoundListPage({
      recordNo: query.recordNo.trim() || undefined,
      patientName: query.patientName.trim() || undefined,
      usType: query.usType ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { loading.value = false }
}
const resetQuery = () => {
  query.recordNo = ''; query.patientName = ''; query.usType = null; query.status = null
  query.pageNum = 1; loadList()
}

// ---------------- 登记 ----------------
const editVisible = ref(false)
const editForm = reactive({
  id: null as any, patientId: null as any, patientNo: '', patientName: '', gender: null as number | null,
  age: null as number | null, visitDate: '', clinicalDiagnosis: '', usType: 1, bodyPart: '', examPurpose: '',
})
const editLoading = ref(false)
const openCreate = () => {
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null, visitDate: '',
    clinicalDiagnosis: '', usType: 1, bodyPart: '', examPurpose: '',
  })
  editVisible.value = true
}
const onPatientSelect = (p: any) => {
  editForm.patientId = p?.id
  editForm.patientName = p?.name || p?.patientName || ''
  editForm.patientNo = p?.patientNo || ''
  editForm.gender = p?.gender ?? null
  editForm.age = p?.age ?? null
}
const saveRecord = async () => {
  if (!editForm.patientId || !editForm.patientName.trim()) { ElMessage.warning('请选择患者'); return }
  editLoading.value = true
  try {
    const res: any = await ultrasoundUpsert({ ...editForm, visitDate: editForm.visitDate || undefined })
    if (res.code === 200) { ElMessage.success(res.message || '已保存'); editVisible.value = false; loadList(); loadStats() }
    else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { editLoading.value = false }
}

// ---------------- 动作 ----------------
const actLoading = ref(false)
const doAction = async (fn: () => Promise<any>, done: () => void) => {
  actLoading.value = true
  try {
    const res: any = await fn()
    if (res.code === 200) { ElMessage.success(res.message || '操作成功'); done() }
    else ElMessage.error(res.message || '操作失败')
  } catch (e) { console.error(e); ElMessage.error('操作失败') } finally { actLoading.value = false }
}
const checkIn = (row: any) => doAction(() => ultrasoundCheckIn(row.id), () => { loadList(); loadStats() })

const execVisible = ref(false)
const execRow = ref<any>(null)
const execForm = reactive({ sonographer: '', bodyPart: '' })
const openExecute = (row: any) => {
  execRow.value = row
  Object.assign(execForm, { sonographer: '', bodyPart: row.bodyPart || '' })
  execVisible.value = true
}
const saveExecute = () => doAction(
  () => ultrasoundExecute({
    recordId: execRow.value.id,
    sonographer: execForm.sonographer.trim() || undefined,
    bodyPart: execForm.bodyPart.trim() || undefined,
  }),
  () => { execVisible.value = false; loadList() })

// ---------------- 测量值 ----------------
const measureVisible = ref(false)
const measureRow = ref<any>(null)
const measures = ref<any[]>([])
const addMeasure = () => measures.value.push({ measureName: '', measureValue: '', unit: '', referenceRange: '', sortOrder: measures.value.length + 1 })
const removeMeasure = (i: number) => measures.value.splice(i, 1)
const openMeasures = async (row: any) => {
  try {
    const res: any = await getUltrasoundDetail(row.id)
    if (res.code !== 200) { ElMessage.error(res.message || '查询详情失败'); return }
    measureRow.value = res.data
    measures.value = (res.data.measures || []).map((m: any, i: number) => ({
      id: m.id, measureName: m.measureName, measureValue: m.measureValue,
      unit: m.unit, referenceRange: m.referenceRange, sortOrder: m.sortOrder || i + 1,
    }))
    if (!measures.value.length) addMeasure()
    measureVisible.value = true
  } catch (e) { console.error(e); ElMessage.error('查询详情失败') }
}
const saveMeasures = () => {
  if (measures.value.some((m) => !m.measureName.trim())) { ElMessage.warning('测量项名称不能为空'); return }
  doAction(
    () => ultrasoundSaveMeasures({
      recordId: measureRow.value.id,
      measures: measures.value.map((m, i) => ({
        id: m.id || undefined, measureName: m.measureName.trim(), measureValue: m.measureValue || undefined,
        unit: m.unit || undefined, referenceRange: m.referenceRange || undefined, sortOrder: i + 1,
      })),
    }),
    () => { measureVisible.value = false; loadList() })
}

// ---------------- 报告 ----------------
const reportVisible = ref(false)
const reportRow = ref<any>(null)
const reportForm = reactive({ findings: '', conclusion: '', suggestion: '' })
const openReport = (row: any) => {
  reportRow.value = row
  Object.assign(reportForm, { findings: row.findings || '', conclusion: row.conclusion || '', suggestion: row.suggestion || '' })
  reportVisible.value = true
}
const saveReport = () => doAction(
  () => ultrasoundReport({ recordId: reportRow.value.id, ...reportForm }),
  () => { reportVisible.value = false; loadList(); loadStats() })

const doAudit = (row: any) => doAction(() => ultrasoundAudit({ recordId: row.id }), () => { loadList(); loadStats() })
const doPublish = (row: any) => doAction(() => ultrasoundPublish(row.id), () => { loadList(); loadStats() })
const cancel = (row: any) => {
  ElMessageBox.prompt('取消原因（必填）', '取消检查', {
    confirmButtonText: '确定', cancelButtonText: '返回', inputPattern: /\S+/, inputErrorMessage: '取消原因不能为空',
  }).then(({ value }) => doAction(
    () => ultrasoundCancel({ recordId: row.id, cancelReason: value }), () => { loadList(); loadStats() })).catch(() => {})
}

// ---------------- 详情 ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getUltrasoundDetail(row.id)
    if (res.code === 200) { detail.value = res.data; detailVisible.value = true }
    else ElMessage.error(res.message || '查询详情失败')
  } catch (e) { console.error(e); ElMessage.error('查询详情失败') }
}

onMounted(() => { loadDicts(); loadStats(); loadList() })
</script>

<template>
  <div>
    <!-- 统计 -->
    <div class="mb-3 grid grid-cols-5 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待检</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">检查中</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.examining }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待审核</div>
        <div class="text-2xl font-semibold text-[#92400E] mt-1">{{ stats.pendingAudit }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已发布</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.published }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日检查</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.todayCount }}</div>
      </div>
    </div>

    <!-- 筛选（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="检查号">
            <el-input v-model="query.recordNo" placeholder="检查号" clearable style="width: 170px" @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="患者姓名">
            <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width: 150px" @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="超声类型">
            <el-select v-model="query.usType" placeholder="超声类型" clearable style="width: 130px" :fit-input-width="false">
              <el-option v-for="d in usTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" :fit-input-width="false">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" v-perm="'medtech:ultrasound:add'" @click="openCreate">登记检查</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="ultrasound-table">
        <el-table-column prop="recordNo" label="检查号" width="150" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column label="超声类型" width="110">
          <template #default="{ row }">{{ usTypeText(row.usType) }}</template>
        </el-table-column>
        <el-table-column prop="bodyPart" label="部位" min-width="120" show-overflow-tooltip />
        <el-table-column prop="clinicalDiagnosis" label="临床诊断" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusTag(row.status) as any">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sonographer" label="超声医师" width="100" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'medtech:ultrasound:edit'" link type="warning" @click="checkIn(row)">签到</el-button>
            <el-button v-if="[2, 3].includes(Number(row.status))" v-perm="'medtech:ultrasound:edit'" link type="primary" @click="openExecute(row)">执行</el-button>
            <el-button v-if="[3].includes(Number(row.status))" v-perm="'medtech:ultrasound:edit'" link type="primary" @click="openMeasures(row)">测量值</el-button>
            <el-button v-if="Number(row.status) === 3" v-perm="'medtech:ultrasound:edit'" link type="primary" @click="openReport(row)">出报告</el-button>
            <el-button v-if="Number(row.status) === 4" v-perm="'medtech:ultrasound:edit'" link type="warning" @click="doAudit(row)">审核</el-button>
            <el-button v-if="Number(row.status) === 5" v-perm="'medtech:ultrasound:edit'" link type="success" @click="doPublish(row)">发布</el-button>
            <el-button v-if="![6, 7].includes(Number(row.status))" v-perm="'medtech:ultrasound:delete'" link type="danger" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES" @current-change="loadList" @size-change="query.pageNum = 1; loadList()" />
      </div>
    </el-card>

    <!-- 登记弹窗 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '修改检查' : '登记超声检查'" width="640px">
      <el-form label-width="90px">
        <el-form-item label="患者" required>
          <patient-select v-model="editForm.patientId" @select="onPatientSelect" />
        </el-form-item>
        <el-form-item label="超声类型">
          <el-select v-model="editForm.usType" style="width: 100%">
            <el-option v-for="d in usTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查部位"><el-input v-model="editForm.bodyPart" /></el-form-item>
        <el-form-item label="检查目的"><el-input v-model="editForm.examPurpose" /></el-form-item>
        <el-form-item label="临床诊断"><el-input v-model="editForm.clinicalDiagnosis" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" v-perm="['medtech:ultrasound:add', 'medtech:ultrasound:edit']" :loading="editLoading" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>

    <!-- 执行弹窗 -->
    <el-dialog v-model="execVisible" :title="`执行检查：${execRow?.recordNo || ''}`" width="480px">
      <el-form label-width="100px">
        <el-form-item label="超声医师"><el-input v-model="execForm.sonographer" placeholder="留空默认当前登录人" /></el-form-item>
        <el-form-item label="检查部位"><el-input v-model="execForm.bodyPart" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="execVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'medtech:ultrasound:edit'" :loading="actLoading" @click="saveExecute">保存</el-button>
      </template>
    </el-dialog>

    <!-- 测量值弹窗 -->
    <el-dialog v-model="measureVisible" :title="`结构化测量值：${measureRow?.recordNo || ''}`" width="760px">
      <el-table :data="measures" size="small">
        <el-table-column label="测量项" min-width="150">
          <template #default="{ row }"><el-input v-model="row.measureName" placeholder="如 肝右叶斜径" /></template>
        </el-table-column>
        <el-table-column label="测量值" width="130">
          <template #default="{ row }"><el-input v-model="row.measureValue" /></template>
        </el-table-column>
        <el-table-column label="单位" width="90">
          <template #default="{ row }"><el-input v-model="row.unit" /></template>
        </el-table-column>
        <el-table-column label="参考范围" width="130">
          <template #default="{ row }"><el-input v-model="row.referenceRange" /></template>
        </el-table-column>
        <el-table-column label="" width="60" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" :icon="Delete" v-perm="'medtech:ultrasound:delete'" @click="removeMeasure($index)" />
          </template>
        </el-table-column>
      </el-table>
      <el-button class="mt-2" size="small" :icon="Plus" v-perm="'medtech:ultrasound:add'" @click="addMeasure">加测量项</el-button>
      <template #footer>
        <el-button @click="measureVisible = false">取消</el-button>
        <el-button type="primary" v-perm="['medtech:ultrasound:add', 'medtech:ultrasound:edit']" :loading="actLoading" @click="saveMeasures">保存（整单覆盖）</el-button>
      </template>
    </el-dialog>

    <!-- 报告弹窗 -->
    <el-dialog v-model="reportVisible" :title="`超声报告：${reportRow?.recordNo || ''}`" width="680px">
      <el-form label-width="90px">
        <el-form-item label="超声所见" required><el-input v-model="reportForm.findings" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="超声提示" required><el-input v-model="reportForm.conclusion" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="建议"><el-input v-model="reportForm.suggestion" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'medtech:ultrasound:edit'" :loading="actLoading" @click="saveReport">提交报告</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="超声检查详情" width="680px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="检查号">{{ detail.recordNo }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.usTypeText }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="超声所见" :span="2">{{ detail.findings || '-' }}</el-descriptions-item>
          <el-descriptions-item label="超声提示" :span="2">{{ detail.conclusion || '未出' }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="2">{{ detail.suggestion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报告医师">{{ detail.reportBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核医师">{{ detail.auditBy || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-3 font-medium text-sm text-gray-600">测量值</div>
        <el-table :data="detail.measures || []" size="small" class="mt-1">
          <el-table-column prop="measureName" label="测量项" min-width="140" />
          <el-table-column prop="measureValue" label="测量值" width="110" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="referenceRange" label="参考范围" width="120" />
          <el-table-column prop="abnormalFlagText" label="标志" width="90" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>
