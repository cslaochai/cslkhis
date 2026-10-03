<script setup lang="ts">
/**
 * 病理工作站（G17，菜单 407）
 *
 * 流程：登记 → 标本接收 → 蜡块取材/包埋/切片（明细单点推进）→ 初诊
 * → 审核（服务端硬校验：审核人 ≠ 初诊人）→ 发布。
 * 状态/类型文案全部走字典（his_pathology_status / his_pathology_exam_type），
 * 本页不写任何映射 —— 口径单点在 sys_dict_data。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getPathologyListPage, getPathologyStats, getPathologyDetail, pathologyUpsert,
  pathologyReceive, pathologyBlockUpsert, pathologyBlockAction,
  pathologyReport, pathologyAudit, pathologyPublish, pathologyCancel,
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
  orderNo: '', patientName: '', examType: null as number | null, status: null as number | null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const stats = reactive({ pendingReceive: 0, processing: 0, pendingAudit: 0, published: 0, frozenToday: 0 })

const statusDict = ref<any[]>([])
const examTypeDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const examTypeText = (v: any) => dictLabelText(examTypeDict.value, v)

const statusTag = (v: any) => {
  const n = Number(v)
  if (n === 7) return 'success'
  if (n === 8) return 'info'
  if (n === 5 || n === 6) return 'warning'
  return 'primary'
}

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.PATHOLOGY_STATUS},${DICT_TYPE.PATHOLOGY_EXAM_TYPE}`)
    statusDict.value = res?.data?.[DICT_TYPE.PATHOLOGY_STATUS] || []
    examTypeDict.value = res?.data?.[DICT_TYPE.PATHOLOGY_EXAM_TYPE] || []
  } catch (e) { console.error('加载字典失败', e) }
}

const loadStats = async () => {
  try {
    const res: any = await getPathologyStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) { console.error(e) }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getPathologyListPage({
      orderNo: query.orderNo.trim() || undefined,
      patientName: query.patientName.trim() || undefined,
      examType: query.examType ?? undefined,
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
  query.orderNo = ''; query.patientName = ''; query.examType = null; query.status = null
  query.pageNum = 1; loadList()
}

// ---------------- 登记 / 修改 ----------------
const editVisible = ref(false)
const editForm = reactive({
  id: null as any, patientId: null as any, patientNo: '', patientName: '', gender: null as number | null,
  age: null as number | null, visitDate: '', clinicalDiagnosis: '', examType: 1, specimenType: '', specimenPart: '', isFrozen: 0,
})
const editLoading = ref(false)

const openCreate = () => {
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null,
    visitDate: '', clinicalDiagnosis: '', examType: 1, specimenType: '', specimenPart: '', isFrozen: 0,
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
const saveOrder = async () => {
  if (!editForm.patientId || !editForm.patientName.trim()) { ElMessage.warning('请选择患者'); return }
  editLoading.value = true
  try {
    const res: any = await pathologyUpsert({
      id: editForm.id || undefined,
      patientId: editForm.patientId, patientNo: editForm.patientNo || undefined,
      patientName: editForm.patientName.trim(), gender: editForm.gender, age: editForm.age,
      visitDate: editForm.visitDate || undefined, clinicalDiagnosis: editForm.clinicalDiagnosis || undefined,
      examType: editForm.examType, specimenType: editForm.specimenType || undefined,
      specimenPart: editForm.specimenPart || undefined, isFrozen: editForm.isFrozen,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存')
      editVisible.value = false; loadList(); loadStats()
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { editLoading.value = false }
}

// ---------------- 通用动作（接收 / 流程推进 / 初诊 / 审核 / 发布 / 取消） ----------------
const actLoading = ref(false)
const doAction = async (fn: () => Promise<any>, done: () => void) => {
  actLoading.value = true
  try {
    const res: any = await fn()
    if (res.code === 200) { ElMessage.success(res.message || '操作成功'); done() }
    else ElMessage.error(res.message || '操作失败')
  } catch (e) { console.error(e); ElMessage.error('操作失败') } finally { actLoading.value = false }
}

const receive = (row: any) => doAction(
  () => pathologyReceive({ orderId: row.id }), () => { loadList(); loadStats() })

const addBlock = (row: any) => {
  ElMessageBox.prompt('登记蜡块号（如 A1，多个用英文逗号分隔）', '登记蜡块', {
    confirmButtonText: '登记', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '蜡块号不能为空',
  }).then(({ value }) => {
    const nos = String(value).split(',').map((s: string) => s.trim()).filter(Boolean)
    doAction(
      () => Promise.all(nos.map((n, i) => pathologyBlockUpsert({ orderId: row.id, blockNo: n, partDesc: row.specimenPart, blockCount: 1 })))
        .then((rs: any[]) => ({ code: rs.every((r) => r.code === 200) ? 200 : 500, message: '蜡块已登记' })),
      () => { loadList(); loadDetail(row.id) })
  }).catch(() => {})
}

const blockAction = (row: any, block: any, action: number) => doAction(
  () => pathologyBlockAction({ blockId: block.id, action }),
  () => { loadDetail(row.id); loadList() })

const reportVisible = ref(false)
const reportRow = ref<any>(null)
const reportForm = reactive({ grossFindings: '', microscopyFindings: '', ihcResult: '', diagnosis: '', suggestion: '', frozenResult: '' })
const openReport = async (row: any) => {
  await loadDetail(row.id)
  reportRow.value = detail.value
  Object.assign(reportForm, {
    grossFindings: detail.value?.grossFindings || '', microscopyFindings: detail.value?.microscopyFindings || '',
    ihcResult: detail.value?.ihcResult || '', diagnosis: detail.value?.diagnosis || '',
    suggestion: detail.value?.suggestion || '', frozenResult: detail.value?.frozenResult || '',
  })
  reportVisible.value = true
}
const saveReport = () => doAction(
  () => pathologyReport({ orderId: reportRow.value.id, ...reportForm }),
  () => { reportVisible.value = false; loadList(); loadStats() })

const doAudit = (row: any) => doAction(
  () => pathologyAudit({ orderId: row.id }), () => { loadList(); loadStats() })
const doPublish = (row: any) => doAction(
  () => pathologyPublish(row.id), () => { loadList(); loadStats() })

const cancel = (row: any) => {
  ElMessageBox.prompt('取消原因（必填）', '取消病理单', {
    confirmButtonText: '确定', cancelButtonText: '返回', inputPattern: /\S+/, inputErrorMessage: '取消原因不能为空',
  }).then(({ value }) => doAction(
    () => pathologyCancel({ orderId: row.id, cancelReason: value }), () => { loadList(); loadStats() })).catch(() => {})
}

// ---------------- 详情（含蜡块） ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const loadDetail = async (orderId: any) => {
  try {
    const res: any = await getPathologyDetail(orderId)
    if (res.code === 200) { detail.value = res.data; return res.data }
    ElMessage.error(res.message || '查询详情失败')
  } catch (e) { console.error(e); ElMessage.error('查询详情失败') }
  return null
}
const openDetail = async (row: any) => { await loadDetail(row.id); detailVisible.value = true }

onMounted(() => { loadDicts(); loadStats(); loadList() })
</script>

<template>
  <div>
    <!-- 统计 -->
    <div class="mb-3 grid grid-cols-5 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待接收标本</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pendingReceive }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">处理中（制片阶段）</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.processing }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待审核（已初诊）</div>
        <div class="text-2xl font-semibold text-[#92400E] mt-1">{{ stats.pendingAudit }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已发布</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.published }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日冰冻</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.frozenToday }}</div>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="病理号">
            <el-input v-model="query.orderNo" placeholder="病理号" clearable style="width: 170px" @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="患者姓名">
            <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width: 150px" @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="检查类型">
            <el-select v-model="query.examType" placeholder="检查类型" clearable style="width: 130px" :fit-input-width="false">
              <el-option v-for="d in examTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
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
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" v-perm="'medtech:pathology:add'" @click="openCreate">登记病理单</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="pathology-table">
        <el-table-column prop="orderNo" label="病理号" width="160" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column label="检查类型" width="110">
          <template #default="{ row }">{{ examTypeText(row.examType) }}<el-tag v-if="Number(row.isFrozen) === 1" size="small" type="danger" class="ml-1">冰冻</el-tag></template>
        </el-table-column>
        <el-table-column prop="specimenPart" label="取材部位" min-width="130" show-overflow-tooltip />
        <el-table-column prop="clinicalDiagnosis" label="临床诊断" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusTag(row.status) as any">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="blockCount" label="蜡块数" width="80" align="center" />
        <el-table-column prop="reportBy" label="初诊" width="90" />
        <el-table-column prop="auditBy" label="审核" width="90" />
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'medtech:pathology:edit'" link type="warning" @click="receive(row)">接收标本</el-button>
            <el-button v-if="[2, 3].includes(Number(row.status))" v-perm="'medtech:pathology:add'" link type="primary" @click="addBlock(row)">加蜡块</el-button>
            <el-button v-if="[4, 3].includes(Number(row.status)) || (Number(row.isFrozen) === 1 && [2, 3].includes(Number(row.status)))" v-perm="'medtech:pathology:edit'" link type="primary" @click="openReport(row)">初诊</el-button>
            <el-button v-if="Number(row.status) === 5" v-perm="'medtech:pathology:edit'" link type="warning" @click="doAudit(row)">审核</el-button>
            <el-button v-if="Number(row.status) === 6" v-perm="'medtech:pathology:edit'" link type="success" @click="doPublish(row)">发布</el-button>
            <el-button v-if="Number(row.status) !== 7 && Number(row.status) !== 8" v-perm="'medtech:pathology:delete'" link type="danger" @click="cancel(row)">取消</el-button>
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
    <el-dialog v-model="editVisible" :title="editForm.id ? '修改病理单' : '登记病理单'" width="640px">
      <el-form label-width="90px">
        <el-form-item label="患者" required>
          <patient-select v-model="editForm.patientId" @select="onPatientSelect" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="检查类型">
            <el-select v-model="editForm.examType" style="width: 100%">
              <el-option v-for="d in examTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否冰冻">
            <el-radio-group v-model="editForm.isFrozen">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标本类型"><el-input v-model="editForm.specimenType" placeholder="活检/切除/穿刺等" /></el-form-item>
          <el-form-item label="取材部位"><el-input v-model="editForm.specimenPart" /></el-form-item>
        </div>
        <el-form-item label="临床诊断"><el-input v-model="editForm.clinicalDiagnosis" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" v-perm="['medtech:pathology:add', 'medtech:pathology:edit']" :loading="editLoading" @click="saveOrder">保存</el-button>
      </template>
    </el-dialog>

    <!-- 初诊弹窗 -->
    <el-dialog v-model="reportVisible" :title="`病理初诊：${reportRow?.orderNo || ''}`" width="680px">
      <el-form label-width="100px">
        <el-form-item label="肉眼所见"><el-input v-model="reportForm.grossFindings" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="镜下所见"><el-input v-model="reportForm.microscopyFindings" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="免疫组化/特染"><el-input v-model="reportForm.ihcResult" type="textarea" :rows="2" /></el-form-item>
        <el-form-item v-if="reportRow && Number(reportRow.isFrozen) === 1" label="冰冻结果" required>
          <el-input v-model="reportForm.frozenResult" type="textarea" :rows="2" placeholder="术中冰冻快速诊断结果（冰冻单必填）" />
        </el-form-item>
        <el-form-item label="病理诊断" required><el-input v-model="reportForm.diagnosis" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="建议"><el-input v-model="reportForm.suggestion" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'medtech:pathology:edit'" :loading="actLoading" @click="saveReport">提交初诊</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="病理单详情" width="760px">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="病理号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="检查类型">{{ detail.examTypeText }}</el-descriptions-item>
          <el-descriptions-item label="标本">{{ detail.specimenType || '-' }} / {{ detail.specimenPart || '-' }}</el-descriptions-item>
          <el-descriptions-item label="冰冻结果">{{ detail.frozenResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="诊断" :span="3">{{ detail.diagnosis || '未出' }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ detail.suggestion || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-3 font-medium text-sm text-gray-600">蜡块明细</div>
        <el-table :data="detail.blocks || []" size="small" class="mt-1">
          <el-table-column prop="blockNo" label="蜡块号" width="90" />
          <el-table-column prop="partDesc" label="部位" min-width="120" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row: b }">
              <el-tag size="small">{{ b.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sliceNo" label="切片号" width="90" />
          <el-table-column label="流转" width="220">
            <template #default="{ row: b }">
              <el-button v-if="Number(b.status) === 1" v-perm="'medtech:pathology:edit'" link type="primary" size="small" @click="blockAction(detail, b, 1)">取材</el-button>
              <el-button v-if="Number(b.status) === 2" v-perm="'medtech:pathology:edit'" link type="primary" size="small" @click="blockAction(detail, b, 2)">包埋</el-button>
              <el-button v-if="Number(b.status) === 3" v-perm="'medtech:pathology:edit'" link type="primary" size="small" @click="blockAction(detail, b, 3)">切片</el-button>
              <span v-if="Number(b.status) === 4" class="text-gray-400 text-xs">已完成</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>
