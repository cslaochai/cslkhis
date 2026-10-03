<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Search, Refresh, Check, Close, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecordListPage, reviewRecord, getRecordDetail } from '@/api/emr'
import { patientGenderText } from '@/lib/patientGender'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const records = ref<any[]>([])
const selectedRecord = ref<any>(null)
const detailLoading = ref(false)
const detailData = ref<any>(null)
const drawerVisible = ref(false)

/** 审核页只展示已提交的病历 */
const RECORD_STATUS_SUBMITTED = 2

const searchForm = ref({
  keyword: '',
  reviewStatus: null as number | null,
  visitDate: '' as string,
})

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const reviewMap: Record<number, { label: string; type: 'info' | 'warning' | 'success' | 'danger' }> = {
  0: { label: '未提交', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '已通过', type: 'success' },
  3: { label: '已驳回', type: 'danger' },
}

const reviewOptions = computed(() =>
  Object.entries(reviewMap).map(([code, item]) => ({ value: Number(code), label: item.label }))
)

/** 组装后端 MedicalRecordQueryDTO 入参 */
const buildQuery = () => {
  const query: any = {
    pageNum: pagination.value.pageNum,
    pageSize: pagination.value.pageSize,
    recordStatus: RECORD_STATUS_SUBMITTED,
  }
  if (searchForm.value.keyword) query.keyword = searchForm.value.keyword.trim()
  if (searchForm.value.reviewStatus != null) query.reviewStatus = searchForm.value.reviewStatus
  if (searchForm.value.visitDate) query.visitDate = searchForm.value.visitDate
  return query
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRecordListPage(buildQuery())
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载病历列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = { keyword: '', reviewStatus: null, visitDate: '' }
  handleSearch()
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

const viewDetail = async (row: any) => {
  selectedRecord.value = row
  detailData.value = null
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await getRecordDetail(row.id)
    detailData.value = res.data || {}
  } catch (error) {
    console.error('加载详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const handleReview = async (row: any, approved: boolean) => {
  const action = approved ? '通过' : '驳回'
  try {
    const { value: remark } = await ElMessageBox.prompt(`请输入审核${action}意见（可选）`, `审核${action}`, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: `审核${action}意见`,
      inputValue: approved ? '病历书写规范，审核通过' : '',
      type: approved ? 'success' : 'warning',
    })
    await reviewRecord(row.id, approved, remark || '', '审核人')
    ElMessage.success(`审核${action}成功`)
    loadData()
    if (selectedRecord.value?.id === row.id) {
      // 刷新当前详情，保持审核状态同步
      const res = await getRecordDetail(row.id)
      detailData.value = res.data || {}
      selectedRecord.value = { ...selectedRecord.value, reviewStatus: approved ? 2 : 3 }
    }
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '审核失败')
    }
  }
}

const getReviewStatus = (row: any) => reviewMap[row.reviewStatus] || { label: '未知', type: 'info' as const }

const getWaitDuration = (row: any) => {
  if (!row.submitTime) return '-'
  const start = new Date(String(row.submitTime).replace(' ', 'T'))
  const minutes = Math.floor((Date.now() - start.getTime()) / 60000)
  if (Number.isNaN(minutes) || minutes < 0) return '-'
  if (minutes < 60) return `${minutes}分钟`
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}小时${minutes % 60}分钟`
  return `${Math.floor(minutes / 1440)}天`
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading">
    <!-- 查询条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="searchForm" inline @submit.prevent>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.reviewStatus" placeholder="审核状态" clearable class="!w-36" @change="handleSearch">
            <el-option v-for="item in reviewOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="患者姓名/病历号/挂号单号"
            :prefix-icon="Search"
            clearable
            class="!w-64"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="就诊日期">
          <el-date-picker
            v-model="searchForm.visitDate"
            type="date"
            placeholder="就诊日期"
            value-format="YYYY-MM-DD"
            clearable
            class="!w-40"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分页列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="records" stripe :max-height="tableMaxHeight" style="width: 100%" empty-text="暂无待审核病历">
        <el-table-column prop="recordNo" label="病历号" width="180" />
        <el-table-column prop="patientName" label="患者" width="90" />
        <el-table-column label="性别/年龄" width="110" align="center">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ patientGenderText(row.gender) }} {{ row.age ?? '-' }}岁</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="120" />
        <el-table-column prop="doctorName" label="医生" width="90" />
        <el-table-column prop="visitDate" label="就诊日期" width="120" />
        <el-table-column label="诊断" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.diagnosisName || row.diagnosis || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.submitTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已等待" width="110">
          <template #default="{ row }">
            <span class="text-xs text-slate-400">{{ getWaitDuration(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" size="small" :type="getReviewStatus(row).type">
              {{ getReviewStatus(row).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon class="mr-0.5"><View /></el-icon>详情
            </el-button>
            <template v-if="row.reviewStatus === 1">
              <el-button v-perm="'emr:medicalReview:edit'" type="success" link size="small" @click="handleReview(row, true)">
                <el-icon class="mr-0.5"><Check /></el-icon>通过
              </el-button>
              <el-button v-perm="'emr:medicalReview:edit'" type="danger" link size="small" @click="handleReview(row, false)">
                <el-icon class="mr-0.5"><Close /></el-icon>驳回
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：在流内紧跟表格底 -->
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

    <!-- 病历详情抽屉 -->
    <el-drawer v-model="drawerVisible" direction="rtl" size="60%">
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">{{ selectedRecord?.patientName }}</span>
          <span class="text-sm text-slate-400">{{ selectedRecord?.recordNo }}</span>
          <el-tag effect="plain" size="small" :type="getReviewStatus(selectedRecord || {}).type">
            {{ getReviewStatus(selectedRecord || {}).label }}
          </el-tag>
        </div>
      </template>

      <div v-loading="detailLoading" class="space-y-4">
        <!-- 患者信息 -->
        <div class="flex items-center justify-between rounded-lg border border-slate-200 bg-slate-50 p-4">
          <div class="flex flex-wrap items-center gap-3 text-sm">
            <span class="text-slate-400">患者号：<span class="font-mono text-slate-700">{{ selectedRecord?.patientNo || '-' }}</span></span>
            <span class="text-slate-400">科室：<span class="text-slate-700">{{ selectedRecord?.deptName || '-' }}</span></span>
            <span class="text-slate-400">医生：<span class="text-slate-700">{{ selectedRecord?.doctorName || '-' }}</span></span>
            <span class="text-slate-400">就诊日期：<span class="text-slate-700">{{ selectedRecord?.visitDate || '-' }}</span></span>
          </div>
          <div v-if="selectedRecord?.reviewStatus === 1" class="flex shrink-0 gap-2">
            <el-button v-perm="'emr:medicalReview:edit'" type="success" :icon="Check" @click="handleReview(selectedRecord, true)">通过</el-button>
            <el-button v-perm="'emr:medicalReview:edit'" type="danger" :icon="Close" @click="handleReview(selectedRecord, false)">驳回</el-button>
          </div>
        </div>

        <!-- 病历内容 -->
        <div v-if="detailData?.record" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div class="rounded-lg border border-slate-200 p-4">
              <h4 class="mb-2 text-sm font-medium text-slate-700">主诉</h4>
              <p class="text-sm text-slate-600">{{ detailData.record.chiefComplaint || '-' }}</p>
            </div>
            <div class="rounded-lg border border-slate-200 p-4">
              <h4 class="mb-2 text-sm font-medium text-slate-700">现病史</h4>
              <p class="text-sm text-slate-600">{{ detailData.record.presentIllness || '-' }}</p>
            </div>
          </div>
          <div class="rounded-lg border-2 border-red-200 bg-red-50 p-4">
            <h4 class="mb-2 text-sm font-bold text-red-700">过敏史</h4>
            <p class="text-sm text-red-600">{{ detailData.record.allergyHistory || '无' }}</p>
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div class="rounded-lg border border-slate-200 p-4">
              <h4 class="mb-2 text-sm font-medium text-slate-700">生命体征</h4>
              <div class="space-y-1 text-sm text-slate-600">
                <div>体温：{{ detailData.record.temperature || '-' }}℃</div>
                <div>脉搏：{{ detailData.record.pulse || '-' }}次/分</div>
                <div>血压：{{ detailData.record.systolicPressure || '-' }}/{{ detailData.record.diastolicPressure || '-' }}mmHg</div>
              </div>
            </div>
            <div class="rounded-lg border border-slate-200 p-4">
              <h4 class="mb-2 text-sm font-medium text-slate-700">诊断</h4>
              <p class="text-sm font-medium text-slate-900">{{ detailData.record.diagnosisName || detailData.record.diagnosis || '-' }}</p>
              <p v-if="detailData.record.diagnosisCode" class="mt-1 font-mono text-xs text-slate-400">编码：{{ detailData.record.diagnosisCode }}</p>
            </div>
            <div class="rounded-lg border border-slate-200 p-4">
              <h4 class="mb-2 text-sm font-medium text-slate-700">治疗方案</h4>
              <p class="text-sm text-slate-600">{{ detailData.record.treatmentPlan || '-' }}</p>
            </div>
          </div>

          <!-- 处方 -->
          <div v-if="detailData.prescriptions?.length">
            <h4 class="mb-2 text-sm font-medium text-slate-700">处方</h4>
            <div v-for="p in detailData.prescriptions" :key="p.id" class="mb-2 rounded-lg border border-slate-200 p-3">
              <div class="flex items-center justify-between">
                <span class="font-mono text-sm font-medium text-slate-900">{{ p.prescriptionNo }}</span>
                <span class="text-xs text-slate-400">{{ p.createTime ? String(p.createTime).substring(0, 10) : '-' }}</span>
              </div>
              <div class="mt-1 text-xs text-slate-500">{{ p.diagnosis || '-' }} | {{ p.drugCount || 0 }}种药 | ¥{{ p.totalAmount || 0 }}</div>
            </div>
          </div>

          <!-- 检查/检验
               顶层键必须是 inspectionApplies / laboratoryApplies（见 EmrRecordDetailVO）；
               原先写的 inspections / laboratories 后端不返回 → 本段恒不渲染且不报错（2026-09-23 修）。 -->
          <div v-if="detailData.inspectionApplies?.length || detailData.laboratoryApplies?.length" class="grid grid-cols-2 gap-4">
            <div v-if="detailData.inspectionApplies?.length">
              <h4 class="mb-2 text-sm font-medium text-slate-700">检查申请</h4>
              <div v-for="ins in detailData.inspectionApplies" :key="ins.id" class="mb-1 rounded border border-slate-200 p-2 text-xs">
                <span class="font-medium text-slate-900">{{ ins.inspectionItemName }}</span>
                <span class="ml-2 text-slate-400">{{ ins.bodyPart || '-' }}</span>
              </div>
            </div>
            <div v-if="detailData.laboratoryApplies?.length">
              <h4 class="mb-2 text-sm font-medium text-slate-700">检验申请</h4>
              <div v-for="lab in detailData.laboratoryApplies" :key="lab.id" class="mb-1 rounded border border-slate-200 p-2 text-xs">
                <span class="font-medium text-slate-900">{{ lab.laboratoryItemName }}</span>
                <span class="ml-2 text-slate-400">{{ lab.specimenType || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 审核信息 -->
          <div
            v-if="detailData.record.reviewStatus === 2 || detailData.record.reviewStatus === 3"
            class="rounded-lg border border-slate-200 p-4 text-sm"
            :class="detailData.record.reviewStatus === 2 ? 'bg-emerald-50' : 'bg-red-50'"
          >
            <h4 class="mb-1 text-sm font-medium text-slate-700">审核意见</h4>
            <p class="text-slate-600">{{ detailData.record.reviewRemark || '-' }}</p>
            <p class="mt-1 text-xs text-slate-400">
              {{ detailData.record.reviewBy || '-' }} · {{ detailData.record.reviewTime || '-' }}
            </p>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
