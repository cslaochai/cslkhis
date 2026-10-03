<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import {Search, Document, Tickets, Warning, CircleCheck, DataLine} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getSpecimenList, getSpecimenStats, assignBarcode, sampleSpecimen, rejectSpecimen} from '@/api/medicaltech'
import {patientGenderText} from '@/lib/patientGender'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface SpecimenItem {
  id: number
  recordNo: string
  specimenNo: string
  specimenType: string
  patientName: string
  patientNo: string
  gender: number
  age: number
  laboratoryItemName: string
  applyDoctorName: string
  applyDeptName: string
  recordStatus: number
  specimenStatus: number
  sampleTime: string
  sampleBy: string
  receiveTime: string
  receiveBy: string
  executeTime: string
  executeBy: string
  auditTime: string
  auditBy: string
  createTime: string
  suggestions: string
}

const loading = ref(false)
const specimens = ref<SpecimenItem[]>([])
const searchTerm = ref('')
const statusFilter = ref('all')
const selectedSpecimen = ref<SpecimenItem | null>(null)
const showDetailDialog = ref(false)
const showBarcodeDialog = ref(false)
const barcodeInput = ref('')
const barcodeTarget = ref<SpecimenItem | null>(null)
const stats = ref({todayCount: 0, pendingSample: 0, sampled: 0, testing: 0, abnormal: 0})

const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const specimenStatusMap: Record<number, string> = {
  0: '待分配',
  1: '已分配',
  2: '已采集',
  3: '已接收',
  99: '异常退回',
}

const recordStatusMap: Record<number, string> = {
  1: '已登记',
  2: '已采样',
  3: '已接收',
  4: '检验中',
  5: '已出结果',
  6: '已审核',
  7: '已发布',
}

const statusTagType = (status: string) => {
  if (status.includes('异常') || status.includes('退回')) return 'danger'
  if (status.includes('已发布') || status.includes('已审核') || status.includes('已出')) return 'success'
  if (status.includes('检验中') || status.includes('采样中')) return 'warning'
  return 'info'
}

const filtered = computed(() => {
  let list = specimens.value
  if (searchTerm.value) {
    const kw = searchTerm.value.toLowerCase()
    list = list.filter(s =>
        s.patientName?.toLowerCase().includes(kw) ||
        s.specimenNo?.toLowerCase().includes(kw) ||
        s.laboratoryItemName?.toLowerCase().includes(kw) ||
        s.patientNo?.toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value !== 'all') {
    const statusNum = Number(statusFilter.value)
    list = list.filter(s => s.recordStatus === statusNum)
  }
  return list
})

const loadData = async () => {
  loading.value = true
  try {
    const [listRes, statsRes] = await Promise.all([
      getSpecimenList({
        pageNum: pagination.value.pageNum,
        pageSize: pagination.value.pageSize,
        keyword: searchTerm.value || undefined,
        recordStatus: statusFilter.value !== 'all' ? Number(statusFilter.value) : undefined,
      }),
      getSpecimenStats(),
    ])
    specimens.value = (listRes.data?.records || []).map((item: any) => ({
      ...item,
      specimenNo: item.specimenNo || '',
      specimenType: item.specimenType || '血液',
      specimenStatus: item.specimenStatus ?? 0,
      sampleTime: item.sampleTime || '',
      sampleBy: item.sampleBy || '',
      receiveTime: item.receiveTime || '',
      receiveBy: item.receiveBy || '',
      executeTime: item.executeTime || '',
      executeBy: item.executeBy || '',
      auditTime: item.auditTime || '',
      auditBy: item.auditBy || '',
      suggestions: item.suggestions || '',
    }))
    pagination.value.total = listRes.data?.total || 0
    stats.value = statsRes.data || {}
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.value.pageNum = page
  loadData()
}

const openDetail = (row: SpecimenItem) => {
  selectedSpecimen.value = row
  showDetailDialog.value = true
}

const handleAssignBarcode = (row: SpecimenItem) => {
  barcodeTarget.value = row
  barcodeInput.value = row.specimenNo || `SP${Date.now()}`
  showBarcodeDialog.value = true
}

const confirmAssignBarcode = async () => {
  if (!barcodeInput.value.trim()) {
    ElMessage.warning('请输入条码号')
    return
  }
  try {
    await assignBarcode(barcodeTarget.value!.id, barcodeInput.value.trim())
    ElMessage.success('条码分配成功')
    showBarcodeDialog.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleSample = async (row: SpecimenItem) => {
  try {
    await ElMessageBox.confirm(
        `确认患者「${row.patientName}」的${row.specimenType}标本已采集？`,
        '采集确认',
        {confirmButtonText: '确认采集', cancelButtonText: '取消', type: 'info'}
    )
    await sampleSpecimen(row.id, '护士')
    ElMessage.success('采集确认成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

const handleReject = async (row: SpecimenItem) => {
  try {
    const {value: reason} = await ElMessageBox.prompt(
        `请填写退回原因（患者：${row.patientName}，项目：${row.laboratoryItemName}）`,
        '标本退回',
        {
          confirmButtonText: '确认退回',
          cancelButtonText: '取消',
          type: 'warning',
          inputPlaceholder: '退回原因',
          inputValidator: (v: string) => v?.trim() ? true : '请填写退回原因',
        }
    )
    await rejectSpecimen(row.id, reason)
    ElMessage.success('退回成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

const traceNodes = (row: SpecimenItem) => {
  const nodes: string[] = []
  if (row.specimenNo) nodes.push('已分配条码')
  if (row.sampleTime) nodes.push(`采样 ${row.sampleBy || ''}`)
  if (row.receiveTime) nodes.push(`接收 ${row.receiveBy || ''}`)
  if (row.executeTime) nodes.push(`检验 ${row.executeBy || ''}`)
  if (row.auditTime) nodes.push(`审核 ${row.auditBy || ''}`)
  return nodes.length > 0 ? nodes.join(' → ') : '医嘱已开'
}

onMounted(() => loadData())
</script>

<template>
  <div>
    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-blue-50">
          <DataLine class="h-5 w-5 text-blue-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">今日标本</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.todayCount || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-amber-50">
          <Warning class="h-5 w-5 text-amber-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">待采样</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.pendingSample || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-purple-50">
          <Tickets class="h-5 w-5 text-purple-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">已采样</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.sampled || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-emerald-50">
          <CircleCheck class="h-5 w-5 text-emerald-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">检验中</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.testing || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-red-50">
          <Warning class="h-5 w-5 text-red-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">异常标本</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.abnormal || 0 }}</p>
        </div>
      </div>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="searchTerm" placeholder="搜索条码/患者/项目..." :prefix-icon="Search" class="!max-w-xs"
                      clearable @clear="loadData" @keyup.enter="loadData"/>
          </el-form-item>
          <el-form-item label="状态">
            <div class="flex gap-1">
              <el-button :type="statusFilter === 'all' ? 'primary' : ''" size="small"
                         @click="statusFilter = 'all'; loadData()">全部
              </el-button>
              <el-button :type="statusFilter === '1' ? 'primary' : ''" size="small"
                         @click="statusFilter = '1'; loadData()">已登记
              </el-button>
              <el-button :type="statusFilter === '2' ? 'primary' : ''" size="small"
                         @click="statusFilter = '2'; loadData()">已采样
              </el-button>
              <el-button :type="statusFilter === '3' ? 'primary' : ''" size="small"
                         @click="statusFilter = '3'; loadData()">已接收
              </el-button>
              <el-button :type="statusFilter === '4' ? 'primary' : ''" size="small"
                         @click="statusFilter = '4'; loadData()">检验中
              </el-button>
              <el-button :type="statusFilter === '5' ? 'primary' : ''" size="small"
                         @click="statusFilter = '5'; loadData()">已出结果
              </el-button>
            </div>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-900">{{ pagination.total }}</span> 条
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <!-- 表格 -->
      <el-table :data="filtered" v-loading="loading" stripe :max-height="tableMaxHeight" @row-click="openDetail"
                class="cursor-pointer">
        <el-table-column prop="specimenNo" label="标本条码" width="150">
          <template #default="{row}">
            <span v-if="row.specimenNo" class="font-mono text-blue-600">{{ row.specimenNo }}</span>
            <span v-else class="text-slate-400">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" min-width="120">
          <template #default="{row}">
            <div>
              <span class="font-medium text-slate-900">{{ row.patientName }}</span>
              <span class="ml-1 text-slate-400">{{ patientGenderText(row.gender) }} {{ row.age }}岁</span>
            </div>
            <div class="text-slate-400">{{ row.patientNo }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="laboratoryItemName" label="检验项目" min-width="140"/>
        <el-table-column prop="specimenType" label="标本类型" width="80"/>
        <el-table-column prop="applyDoctorName" label="开单医生" width="90"/>
        <el-table-column label="流转节点" min-width="180">
          <template #default="{row}">
            <span class="text-slate-500">{{ traceNodes(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="statusTagType(recordStatusMap[row.recordStatus] || '')" size="small" effect="plain">
              {{ recordStatusMap[row.recordStatus] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{row}">
            <template v-if="row.recordStatus === 1">
              <el-button v-if="!row.specimenNo" v-perm="'medtech:specimen:edit'" type="primary" link size="small" @click.stop="handleAssignBarcode(row)">
                分配条码
              </el-button>
              <el-button v-if="row.specimenNo" v-perm="'medtech:specimen:edit'" type="success" link size="small" @click.stop="handleSample(row)">
                采集确认
              </el-button>
            </template>
            <template v-if="row.recordStatus === 2">
              <el-button v-perm="'medtech:specimen:edit'" type="warning" link size="small" @click.stop="handleReject(row)">
                退回
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="pagination.total > pagination.pageSize" ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            :current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="标本详情" width="600px" destroy-on-close>
      <template v-if="selectedSpecimen">
        <div class="space-y-4">
          <!-- 基本信息 -->
          <div class="rounded-lg bg-slate-50 p-4">
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div><span class="text-slate-500">标本条码：</span>
                <span class="font-mono font-medium">{{ selectedSpecimen.specimenNo || '未分配' }}</span>
              </div>
              <div><span class="text-slate-500">标本类型：</span><span class="font-medium">{{
                  selectedSpecimen.specimenType
                }}</span></div>
              <div><span class="text-slate-500">患者：</span><span class="font-medium">{{
                  selectedSpecimen.patientName
                }} {{ patientGenderText(selectedSpecimen.gender) }} {{ selectedSpecimen.age }}岁</span></div>
              <div><span class="text-slate-500">患者号：</span><span class="font-medium">{{
                  selectedSpecimen.patientNo
                }}</span></div>
              <div><span class="text-slate-500">检验项目：</span><span class="font-medium">{{
                  selectedSpecimen.laboratoryItemName
                }}</span></div>
              <div><span class="text-slate-500">开单医生：</span><span class="font-medium">{{
                  selectedSpecimen.applyDoctorName
                }}</span></div>
              <div><span class="text-slate-500">申请科室：</span><span class="font-medium">{{
                  selectedSpecimen.applyDeptName || '-'
                }}</span></div>
              <div><span class="text-slate-500">记录号：</span>
                <span class="font-mono text-xs">{{ selectedSpecimen.recordNo }}</span>
              </div>
            </div>
          </div>

          <!-- 流转时间线 -->
          <div>
            <h4 class="mb-3 text-sm font-semibold text-slate-700">流转记录</h4>
            <div class="space-y-3 pl-2">
              <div v-if="selectedSpecimen.createTime" class="flex gap-3">
                <div class="flex flex-col items-center">
                  <div class="h-2.5 w-2.5 rounded-full bg-blue-500"></div>
                  <div class="w-px flex-1 bg-slate-200"></div>
                </div>
                <div class="pb-3">
                  <p class="text-sm font-medium text-slate-700">医嘱开出</p>
                  <p class="text-xs text-slate-400">{{ selectedSpecimen.createTime?.replace('T', ' ') }}</p>
                </div>
              </div>
              <div v-if="selectedSpecimen.sampleTime" class="flex gap-3">
                <div class="flex flex-col items-center">
                  <div class="h-2.5 w-2.5 rounded-full bg-amber-500"></div>
                  <div class="w-px flex-1 bg-slate-200"></div>
                </div>
                <div class="pb-3">
                  <p class="text-sm font-medium text-slate-700">标本采集</p>
                  <p class="text-xs text-slate-400">{{ selectedSpecimen.sampleTime?.replace('T', ' ') }} · {{
                      selectedSpecimen.sampleBy
                    }}</p>
                </div>
              </div>
              <div v-if="selectedSpecimen.receiveTime" class="flex gap-3">
                <div class="flex flex-col items-center">
                  <div class="h-2.5 w-2.5 rounded-full bg-purple-500"></div>
                  <div class="w-px flex-1 bg-slate-200"></div>
                </div>
                <div class="pb-3">
                  <p class="text-sm font-medium text-slate-700">标本接收</p>
                  <p class="text-xs text-slate-400">{{ selectedSpecimen.receiveTime?.replace('T', ' ') }} · {{
                      selectedSpecimen.receiveBy
                    }}</p>
                </div>
              </div>
              <div v-if="selectedSpecimen.executeTime" class="flex gap-3">
                <div class="flex flex-col items-center">
                  <div class="h-2.5 w-2.5 rounded-full bg-emerald-500"></div>
                  <div class="w-px flex-1 bg-slate-200"></div>
                </div>
                <div class="pb-3">
                  <p class="text-sm font-medium text-slate-700">检验执行</p>
                  <p class="text-xs text-slate-400">{{ selectedSpecimen.executeTime?.replace('T', ' ') }} · {{
                      selectedSpecimen.executeBy
                    }}</p>
                </div>
              </div>
              <div v-if="selectedSpecimen.auditTime" class="flex gap-3">
                <div class="flex flex-col items-center">
                  <div class="h-2.5 w-2.5 rounded-full bg-emerald-600"></div>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-700">报告审核</p>
                  <p class="text-xs text-slate-400">{{ selectedSpecimen.auditTime?.replace('T', ' ') }} · {{
                      selectedSpecimen.auditBy
                    }}</p>
                </div>
              </div>
              <div v-if="selectedSpecimen.specimenStatus === 99" class="flex gap-3">
                <div class="flex flex-col items-center">
                  <div class="h-2.5 w-2.5 rounded-full bg-red-500"></div>
                </div>
                <div>
                  <p class="text-sm font-medium text-red-600">异常退回</p>
                  <p v-if="selectedSpecimen.suggestions" class="text-xs text-slate-400">原因：{{
                      selectedSpecimen.suggestions
                    }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 条码分配弹窗 -->
    <el-dialog v-model="showBarcodeDialog" title="分配标本条码" width="400px" destroy-on-close>
      <div class="space-y-4">
        <div v-if="barcodeTarget" class="rounded-lg bg-slate-50 p-3 text-sm">
          <p>患者：<span class="font-medium">{{ barcodeTarget.patientName }}</span></p>
          <p>项目：<span class="font-medium">{{ barcodeTarget.laboratoryItemName }}</span></p>
          <p>类型：<span class="font-medium">{{ barcodeTarget.specimenType }}</span></p>
        </div>
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">条码号</label>
          <el-input v-model="barcodeInput" placeholder="请输入或扫描条码号" clearable/>
        </div>
      </div>
      <template #footer>
        <el-button @click="showBarcodeDialog = false">取消</el-button>
        <el-button v-perm="'medtech:specimen:edit'" type="primary" @click="confirmAssignBarcode">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>
