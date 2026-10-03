<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import {Search, Aim, Monitor, Check, Edit, View} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getInspectionRecordListPage, getInspectionDetail, checkIn, startInspection, executeInspection, auditInspection,
  finishShoot
} from '@/api/medicaltech'
import {patientGenderText} from '@/lib/patientGender'
import ExamImagePanel from '@/components/his/ExamImagePanel.vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface InspectionOrder {
  id: number
  recordNo: string
  itemName: string
  patientName: string
  patientNo?: string
  gender?: number
  age?: number
  doctorName: string
  deptName?: string
  orderTime: string
  statusCode: number
  status: string
  price: number
  bodyPart?: string
  purpose?: string
  clinicalDiagnosis?: string
  resultDescription?: string
  resultConclusion?: string
  suggestions?: string
  resultImage?: string
  /** 影像帧挂在申请单上（sql/137），列表 VO 直接带 applyId */
  applyId?: number | string
  applyNo?: string
  /** 项目类型（sql/138 分岗）：1-放射 → 技师只做「拍片完成」，报告由放射诊断工作站书写 */
  itemType?: number | null
}

const searchTerm = ref('')
const statusFilter = ref('all')
const selectedOrder = ref<InspectionOrder | null>(null)
const showDetailDialog = ref(false)
const showResultDialog = ref(false)
const showAuditDialog = ref(false)
const auditRemark = ref('')
const loading = ref(false)
const orders = ref<InspectionOrder[]>([])

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const inspectionResultForm = ref({
  resultDescription: '',
  resultConclusion: '',
  suggestions: '',
})

const statusMap: Record<number, string> = {
  1: '已登记',
  2: '已签到',
  3: '检查中',
  4: '已出结果',
  5: '已审核',
  6: '已发布',
}

const statusTagType = (status: string) => {
  if (status.includes('已审核') || status.includes('已发布')) return 'success'
  if (status.includes('检查中')) return 'warning'
  if (status.includes('已取消')) return 'danger'
  return 'info'
}

const filtered = computed(() =>
    orders.value.filter((o) => {
      const matchSearch = !searchTerm.value ||
          o.patientName?.includes(searchTerm.value) ||
          o.itemName?.includes(searchTerm.value) ||
          o.doctorName?.includes(searchTerm.value) ||
          o.recordNo?.includes(searchTerm.value)
      const matchStatus = statusFilter.value === 'all' || o.status === statusFilter.value
      return matchSearch && matchStatus
    })
)

const statusCounts = computed(() => ({
  pending: orders.value.filter((o) => o.statusCode === 1 || o.statusCode === 2).length,
  processing: orders.value.filter((o) => o.statusCode === 3).length,
  completed: orders.value.filter((o) => o.statusCode === 4 || o.statusCode === 5).length,
  published: orders.value.filter((o) => o.statusCode === 6).length,
}))

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInspectionRecordListPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      patientName: searchTerm.value || undefined,
      recordStatus: statusFilter.value !== 'all' ? getStatusKey(statusFilter.value) : undefined,
    })
    const list = (res.data?.records || []).map((item: any) => ({
      id: item.id,
      recordNo: item.recordNo,
      itemName: item.inspectionItemName,
      patientName: item.patientName,
      patientNo: item.patientNo,
      gender: item.gender,
      age: item.age,
      doctorName: item.applyDoctorName,
      deptName: item.applyDeptName,
      orderTime: item.createTime?.split('T')[0],
      statusCode: item.recordStatus || 1,
      // 未知码值渲染成「未知(n)」，不回落成某个合法状态 —— 回落会把陌生状态伪装成正常
      status: statusMap[item.recordStatus] ?? (item.recordStatus == null ? '未知' : `未知(${item.recordStatus})`),
      price: item.price || 0,
      reportSignId: item.reportSignId,
      reportSignedTime: item.reportSignedTime,
      auditSignId: item.auditSignId,
      auditSignedTime: item.auditSignedTime,
      bodyPart: item.bodyPart,
      purpose: item.inspectionPurpose,
      clinicalDiagnosis: item.clinicalDiagnosis,
      resultDescription: item.resultDescription,
      resultConclusion: item.resultConclusion,
      suggestions: item.suggestions,
      resultImage: item.resultImage,
      applyId: item.applyId,
      applyNo: item.applyNo,
      itemType: item.itemType ?? null,
    }))
    orders.value = list
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载检查记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getStatusKey = (statusName: string) => {
  for (const [key, value] of Object.entries(statusMap)) {
    if (value === statusName) return parseInt(key)
  }
  return null
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchTerm.value = ''
  statusFilter.value = 'all'
  pagination.value.pageNum = 1
  loadData()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

const handleViewDetail = async (row: InspectionOrder) => {
  selectedOrder.value = row
  showDetailDialog.value = true
}

const handleCheckIn = async (row: InspectionOrder) => {
  try {
    await ElMessageBox.confirm(`确认患者 ${row.patientName} 已到达？`, '签到确认', {
      confirmButtonText: '确认签到',
      cancelButtonText: '取消',
      type: 'info',
    })
    await checkIn(row.id)
    ElMessage.success('签到成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '签到失败')
    }
  }
}

const handleStartExam = async (row: InspectionOrder) => {
  try {
    await ElMessageBox.confirm(`确认开始为 ${row.patientName} 进行检查？`, '开始检查', {
      confirmButtonText: '确认开始',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await startInspection(row.id)
    ElMessage.success('已开始检查')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

/**
 * 拍片完成（sql/138，放射项目专用）。
 *
 * 放射类的报告由放射诊断医师在「放射诊断工作站」书写，技师在这里只把记录推进到
 * 「已出结果」，不建报告也不签名 —— 后端 executeInspection 对放射项目是硬拒绝的，
 * 所以这里必须走另一个按钮，不然技师点「录入」只会撞一句报错。
 */
const handleFinishShoot = async (row: InspectionOrder) => {
  try {
    await ElMessageBox.confirm(
        `确认 ${row.patientName} 的「${row.itemName}」已拍片完成？完成后转到放射诊断工作站待书写报告。`,
        '拍片完成', {confirmButtonText: '确认完成', cancelButtonText: '取消', type: 'info'})
  } catch (e) {
    return
  }
  try {
    await finishShoot(row.id)
    ElMessage.success('拍片完成，已转到放射诊断工作站')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleResultEntry = async (row: InspectionOrder) => {
  selectedOrder.value = row
  inspectionResultForm.value = {
    resultDescription: row.resultDescription || '',
    resultConclusion: row.resultConclusion || '',
    suggestions: row.suggestions || '',
  }
  showResultDialog.value = true
}

const handleSubmitResult = async () => {
  if (!selectedOrder.value) return

  try {
    await executeInspection(selectedOrder.value.id, {
      resultDescription: inspectionResultForm.value.resultDescription,
      resultConclusion: inspectionResultForm.value.resultConclusion,
      suggestions: inspectionResultForm.value.suggestions,
    })
    ElMessage.success('检查结果提交成功')
    showResultDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const handleAudit = async (row: InspectionOrder) => {
  selectedOrder.value = {...row}
  auditRemark.value = ''
  try {
    const res = await getInspectionDetail(row.id)
    if (res.data?.record) {
      Object.assign(selectedOrder.value, res.data.record)
    }
  } catch (e) {
    console.error('加载详情失败', e)
  }
  showAuditDialog.value = true
}

const handleSubmitAudit = async () => {
  if (!selectedOrder.value) return

  try {
    await auditInspection(selectedOrder.value.id, '当前用户')
    ElMessage.success('审核成功')
    showAuditDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading">
    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
      <div v-for="item in [
        { label: '待处理', value: statusCounts.pending, color: 'text-slate-600', bg: 'bg-slate-50' },
        { label: '检查中', value: statusCounts.processing, color: 'text-amber-600', bg: 'bg-amber-50' },
        { label: '待审核', value: statusCounts.completed, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: '已发布', value: statusCounts.published, color: 'text-emerald-600', bg: 'bg-emerald-50' },
      ]" :key="item.label" class="rounded-lg border border-slate-200 bg-white p-4 text-center shadow-sm">
        <p :class="['text-2xl font-bold', item.color]">{{ item.value }}</p>
        <p class="mt-1 text-xs text-slate-500">{{ item.label }}</p>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="searchTerm" placeholder="搜索患者/项目/医生..." :prefix-icon="Search" class="!w-64"
                    @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" class="!w-32" placeholder="状态">
            <el-option label="全部状态" value="all"/>
            <el-option v-for="s in ['已登记','已签到','检查中','已出结果','已审核']" :key="s" :label="s" :value="s"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="filtered" style="width: 100%" stripe :max-height="tableMaxHeight">
        <el-table-column prop="recordNo" label="记录号" width="200"/>
        <el-table-column prop="patientName" label="患者" width="150"/>
        <el-table-column label="性别/年龄" width="200" align="center">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ patientGenderText(row.gender) }} {{
                row.age
              }}岁</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="检查项目" min-width="120"/>
        <el-table-column label="部位" width="200">
          <template #default="{ row }">
            <span class="text-xs">{{ row.bodyPart }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="申请科室" width="150"/>
        <el-table-column prop="doctorName" label="申请医生" width="150"/>
        <el-table-column prop="orderTime" label="申请时间" width="200"/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small" :type="statusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签名" width="120" align="center">
          <template #default="{ row }">
            <div class="flex flex-col items-center gap-0.5 text-xs leading-5">
              <span :class="row.reportSignId ? 'text-emerald-600' : 'text-slate-400'">
                报告{{ row.reportSignId ? '已签' : '未签' }}
              </span>
              <span :class="row.auditSignId ? 'text-emerald-600' : 'text-amber-600'">
                审核{{ row.auditSignId ? '已签' : '未签' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon class="mr-0.5">
                <View/>
              </el-icon>
              详情
            </el-button>
            <el-button
                v-if="row.statusCode === 1"
                type="info" link size="small"
                @click="handleCheckIn(row)">
              签到
            </el-button>
            <el-button
                v-if="row.statusCode === 2"
                type="warning" link size="small"
                @click="handleStartExam(row)">
              开始检查
            </el-button>
            <!-- sql/138 分岗：放射项目（item_type=1）技师只做「拍片完成」，诊断结论归放射诊断工作站 -->
            <el-button
                v-if="row.statusCode === 3 && row.itemType === 1"
                type="success" link size="small"
                @click="handleFinishShoot(row)">
              <el-icon class="mr-0.5">
                <Aim/>
              </el-icon>
              拍片完成
            </el-button>
            <el-button
                v-if="row.statusCode === 3 && row.itemType !== 1"
                type="warning" link size="small"
                @click="handleResultEntry(row)">
              <el-icon class="mr-0.5">
                <Edit/>
              </el-icon>
              录入
            </el-button>
            <el-button
                v-if="row.statusCode === 4"
                type="success" link size="small"
                @click="handleAudit(row)">
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" :title="`检查详情 - ${selectedOrder?.itemName}`" width="700px"
               destroy-on-close>
      <template v-if="selectedOrder">
        <div class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
            <div><p class="text-xs text-slate-400">患者</p>
              <p class="text-sm font-medium">{{ selectedOrder.patientName }}</p></div>
            <div><p class="text-xs text-slate-400">记录号</p>
              <p class="text-sm font-medium">{{ selectedOrder.recordNo }}</p></div>
            <div><p class="text-xs text-slate-400">申请医生</p>
              <p class="text-sm font-medium">{{ selectedOrder.doctorName }}</p></div>
            <div><p class="text-xs text-slate-400">申请科室</p>
              <p class="text-sm font-medium">{{ selectedOrder.deptName }}</p></div>
            <div><p class="text-xs text-slate-400">检查项目</p>
              <p class="text-sm font-medium">{{ selectedOrder.itemName }}</p></div>
            <div><p class="text-xs text-slate-400">检查部位</p>
              <p class="text-sm font-medium">{{ selectedOrder.bodyPart }}</p></div>
            <div><p class="text-xs text-slate-400">费用</p>
              <p class="text-sm font-medium">¥{{ selectedOrder.price }}</p></div>
            <div><p class="text-xs text-slate-400">临床诊断</p>
              <p class="text-sm font-medium">{{ selectedOrder.clinicalDiagnosis }}</p></div>
          </div>

          <!-- 检查所见 -->
          <div v-if="selectedOrder.resultDescription" class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 text-sm font-medium text-slate-700">检查所见</p>
            <p class="text-sm text-slate-600 whitespace-pre-wrap">{{ selectedOrder.resultDescription }}</p>
          </div>

          <!-- 影像诊断 -->
          <div v-if="selectedOrder.resultConclusion" class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 text-sm font-medium text-slate-700">影像诊断/印象</p>
            <p class="text-sm text-slate-600 whitespace-pre-wrap">{{ selectedOrder.resultConclusion }}</p>
          </div>

          <!-- 建议 -->
          <div v-if="selectedOrder.suggestions" class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 text-sm font-medium text-slate-700">建议</p>
            <p class="text-sm text-slate-600 whitespace-pre-wrap">{{ selectedOrder.suggestions }}</p>
          </div>

          <!-- 影像展示（sql/137：真影像帧，挂在申请单上；详情弹框只读） -->
          <ExamImagePanel :biz-type="1" :apply-id="selectedOrder.applyId" readonly/>
        </div>
      </template>
    </el-dialog>

    <!-- 结果录入对话框 -->
    <el-dialog v-model="showResultDialog" title="检查结果录入" width="900px" destroy-on-close>
      <template v-if="selectedOrder">
        <div class="space-y-4">
          <div class="rounded-lg bg-slate-50 p-3 text-sm">
            <span class="text-slate-500">患者：</span>
            <span class="font-medium">{{ selectedOrder.patientName }}</span>
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-500">项目：</span>
            <span class="font-medium">{{ selectedOrder.itemName }}</span>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <!-- 左侧：影像录入（结果录入时顺手传片/模拟导入，写权限由按钮码控制） -->
            <div class="rounded-lg border border-slate-200 p-3">
              <ExamImagePanel :biz-type="1" :apply-id="selectedOrder.applyId"/>
            </div>
            <!-- 右侧：录入表单 -->
            <div class="space-y-3">
              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">检查所见 <span
                    class="text-red-500">*</span></label>
                <el-input v-model="inspectionResultForm.resultDescription" type="textarea"
                          :autosize="{ minRows: 4, maxRows: 8 }"
                          placeholder="请根据影像描述检查所见，如：胸廓对称，气管居中，双肺纹理清晰..."/>
              </div>
              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">影像诊断/印象 <span
                    class="text-red-500">*</span></label>
                <el-input v-model="inspectionResultForm.resultConclusion" type="textarea"
                          :autosize="{ minRows: 2, maxRows: 4 }"
                          placeholder="请根据检查所见给出诊断意见"/>
              </div>
              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">建议</label>
                <el-input v-model="inspectionResultForm.suggestions" type="textarea"
                          :autosize="{ minRows: 2, maxRows: 3 }"
                          placeholder="请填写建议，如：建议结合临床，必要时进一步检查"/>
              </div>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showResultDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitResult">提交结果</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="showAuditDialog" title="审核检查报告" width="800px" destroy-on-close>
      <template v-if="selectedOrder">
        <div class="space-y-4">
          <!-- 患者信息 -->
          <div class="rounded-lg bg-slate-50 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">患者信息</h4>
            <div class="grid grid-cols-3 gap-3 text-sm">
              <div><span class="text-slate-500">患者：</span><span class="font-medium">{{
                  selectedOrder.patientName
                }}</span></div>
              <div><span class="text-slate-500">记录号：</span><span class="font-medium">{{
                  selectedOrder.recordNo
                }}</span></div>
              <div><span class="text-slate-500">申请科室：</span><span
                  class="font-medium">{{ selectedOrder.deptName }}</span></div>
              <div><span class="text-slate-500">检查项目：</span><span class="font-medium">{{
                  selectedOrder.itemName
                }}</span></div>
              <div><span class="text-slate-500">申请医生：</span><span class="font-medium">{{
                  selectedOrder.doctorName
                }}</span></div>
              <div><span class="text-slate-500">检查部位：</span><span
                  class="font-medium">{{ selectedOrder.bodyPart }}</span></div>
            </div>
          </div>

          <!-- 检查所见 -->
          <div v-if="selectedOrder.resultDescription" class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">检查所见</h4>
            <p class="text-sm text-slate-700 whitespace-pre-wrap">{{ selectedOrder.resultDescription }}</p>
          </div>

          <!-- 影像诊断 -->
          <div v-if="selectedOrder.resultConclusion" class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">影像诊断/印象</h4>
            <p class="text-sm text-slate-700 whitespace-pre-wrap">{{ selectedOrder.resultConclusion }}</p>
          </div>

          <!-- 建议 -->
          <div v-if="selectedOrder.suggestions" class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">建议</h4>
            <p class="text-sm text-slate-700 whitespace-pre-wrap">{{ selectedOrder.suggestions }}</p>
          </div>

          <!-- 影像展示（审核岗只读看图，不能改历史影像） -->
          <ExamImagePanel :biz-type="1" :apply-id="selectedOrder.applyId" readonly/>

          <!-- 审核意见 -->
          <div class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-2 text-sm font-bold text-slate-700">审核意见</h4>
            <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（可选）..."/>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAudit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          确认审核通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
