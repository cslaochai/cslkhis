<script setup lang="ts">
/**
 * 病案编码任务池（G16 收口）
 *
 * 任务流：同步任务池（幂等生成）→ 分配编码员 → 提交编码（主诊断 ICD 必填）
 * → 病案室审核：通过 → 3 已完成；退修 → 4 已退修（计数 + 站内信提醒，可再提交）。
 * 编码员姓名由服务端按 sys_employee 反查；未分配任务提交时自动认领当前编码员。
 * 状态文案走字典（his_archive_code_status），tag 色与动作口径单点 lib/codeTask.js。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, MagicStick } from '@element-plus/icons-vue'
import {
  getCodeTaskList,
  getCodeTaskDetail,
  getCodeTaskStats,
  syncCodeTasks,
  assignCodeTask,
  submitCodeTask,
  auditCodeTask,
} from '@/api/codeTask'
import { getEmployeeList } from '@/api/system'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { statusTagType, CODE_TASK_ACTIONS } from '@/lib/codeTask'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  taskNo: '',
  status: null as number | null,
  coderId: null as string | null,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const stats = reactive({ pending: 0, submitted: 0, done: 0, rework: 0 })
const statusDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)

const hasAction = (row: any, key: string) =>
  (CODE_TASK_ACTIONS[Number(row.status)] || []).includes(key)

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(DICT_TYPE.CODE_TASK_STATUS)
    statusDict.value = res?.data?.[DICT_TYPE.CODE_TASK_STATUS] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

const loadStats = async () => {
  try {
    const res: any = await getCodeTaskStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getCodeTaskList({
      taskNo: query.taskNo.trim() || undefined,
      status: query.status ?? undefined,
      coderId: query.coderId || undefined,
      keyword: query.keyword.trim() || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.taskNo = ''
  query.status = null
  query.coderId = null
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

const runSync = async () => {
  try {
    const res: any = await syncCodeTasks()
    if (res.code === 200) {
      ElMessage.success(res.message || '同步完成')
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '同步失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('同步失败')
  }
}

// ------------------------------------------------------------------
// 编码员下拉（在职员工）
// ------------------------------------------------------------------
const employeeOptions = ref<any[]>([])
const loadEmployees = async () => {
  try {
    const res: any = await getEmployeeList({})
    if (res.code === 200) employeeOptions.value = res.data || []
  } catch (e) {
    console.error('加载员工失败', e)
  }
}

// ------------------------------------------------------------------
// 分配弹窗
// ------------------------------------------------------------------
const assignVisible = ref(false)
const assignRow = ref<any>(null)
const assignCoderId = ref<string | null>(null)
const assignLoading = ref(false)

const openAssign = (row: any) => {
  assignRow.value = row
  assignCoderId.value = row.coderId ? String(row.coderId) : null
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignCoderId.value) {
    ElMessage.warning('请选择编码员')
    return
  }
  assignLoading.value = true
  try {
    const res: any = await assignCodeTask(assignRow.value.id, assignCoderId.value)
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '分配失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('分配失败')
  } finally {
    assignLoading.value = false
  }
}

// ------------------------------------------------------------------
// 提交编码弹窗
// ------------------------------------------------------------------
const submitVisible = ref(false)
const submitRow = ref<any>(null)
const submitForm = reactive({ mainIcdCode: '', mainIcdName: '', otherIcdText: '' })
const submitLoading = ref(false)

const openSubmit = (row: any) => {
  submitRow.value = row
  submitForm.mainIcdCode = ''
  submitForm.mainIcdName = ''
  submitForm.otherIcdText = ''
  submitVisible.value = true
}

const submitTask = async () => {
  if (!submitForm.mainIcdCode.trim() || !submitForm.mainIcdName.trim()) {
    ElMessage.warning('请填写主诊断 ICD 编码与名称')
    return
  }
  submitLoading.value = true
  try {
    const res: any = await submitCodeTask({
      id: submitRow.value.id,
      mainIcdCode: submitForm.mainIcdCode.trim(),
      mainIcdName: submitForm.mainIcdName.trim(),
      otherIcdText: submitForm.otherIcdText.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('编码已提交，等待审核')
      submitVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

// ------------------------------------------------------------------
// 审核弹窗（通过 / 退修共用）
// ------------------------------------------------------------------
const auditVisible = ref(false)
const auditRow = ref<any>(null)
const auditApprove = ref(true)
const auditRemark = ref('')
const auditLoading = ref(false)

const openAudit = (row: any) => {
  auditRow.value = row
  auditApprove.value = true
  auditRemark.value = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  if (!auditApprove.value && !auditRemark.value.trim()) {
    ElMessage.warning('退修必须填写审核意见')
    return
  }
  auditLoading.value = true
  try {
    const res: any = await auditCodeTask(auditRow.value.id, auditApprove.value, auditRemark.value.trim() || undefined)
    if (res.code === 200) {
      ElMessage.success('审核完成')
      auditVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('审核失败')
  } finally {
    auditLoading.value = false
  }
}

// ------------------------------------------------------------------
// 详情弹窗
// ------------------------------------------------------------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getCodeTaskDetail(row.id)
    if (res.code === 200) {
      detail.value = res.data
      detailVisible.value = true
    } else {
      ElMessage.error(res.message || '查询详情失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询详情失败')
  }
}

onMounted(() => {
  loadDicts()
  loadStats()
  loadEmployees()
  loadList()
})
</script>

<template>
  <div>
    <!-- 统计卡 -->
    <div class="grid grid-cols-4 gap-4 mb-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待编码</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已提交待审核</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.submitted }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已完成</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.done }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已退修（在途）</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.rework }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="任务号">
            <el-input
              v-model="query.taskNo"
              placeholder="任务号"
              clearable
              style="width: 180px"
              data-testid="ct-no-filter"
              @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input
              v-model="query.keyword"
              placeholder="任务号/病历号/患者姓名"
              clearable
              style="width: 200px"
              @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" :fit-input-width="false" data-testid="ct-status-filter">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" data-testid="ct-search-btn" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：同步动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="warning" :icon="MagicStick" data-testid="ct-sync-btn" v-perm="'emr:codeTask:add'" @click="runSync">同步任务池</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="ct-table">
        <el-table-column prop="taskNo" label="任务号" width="170" />
        <el-table-column prop="recordNo" label="病历号" width="170" show-overflow-tooltip />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="deptName" label="科室" width="120" show-overflow-tooltip />
        <el-table-column prop="diagnosis" label="诊断（参照）" min-width="150" show-overflow-tooltip />
        <el-table-column label="主诊断 ICD" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="row.mainIcdCode">{{ row.mainIcdCode }} {{ row.mainIcdName }}</template>
            <template v-else>—</template>
          </template>
        </el-table-column>
        <el-table-column prop="coderName" label="编码员" width="90">
          <template #default="{ row }">{{ row.coderName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
            <span v-if="Number(row.returnCount) > 0" class="ml-1 text-xs text-red-500">退{{ row.returnCount }}次</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="hasAction(row, 'assign')" v-perm="'emr:codeTask:edit'" link type="warning" @click="openAssign(row)">分配</el-button>
            <el-button v-if="hasAction(row, 'submit')" v-perm="'emr:codeTask:edit'" link type="success" @click="openSubmit(row)">提交编码</el-button>
            <el-button v-if="hasAction(row, 'audit')" v-perm="'emr:codeTask:edit'" link type="warning" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '任务池为空（点「同步任务池」为待编码病历建单）'" />
        </template>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignVisible" :title="`分配编码员 - ${assignRow?.taskNo || ''}`" width="480px" data-testid="ct-assign-dialog">
      <div v-if="assignRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">病案：</span>{{ assignRow.recordNo }} / {{ assignRow.patientName }}（{{ assignRow.deptName || '—' }}）</div>
      </div>
      <el-select v-model="assignCoderId" filterable placeholder="选择编码员（在职员工）" style="width: 100%" :fit-input-width="false" data-testid="ct-assign-coder">
        <el-option v-for="e in employeeOptions" :key="e.id" :label="`${e.empName || e.name}（${e.empCode || e.id}）`" :value="String(e.id)" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" data-testid="ct-assign-submit" v-perm="'emr:codeTask:edit'" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 提交编码弹窗 -->
    <el-dialog v-model="submitVisible" :title="`提交编码 - ${submitRow?.taskNo || ''}`" width="560px" data-testid="ct-submit-dialog">
      <div v-if="submitRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">病案：</span>{{ submitRow.recordNo }} / {{ submitRow.patientName }}（{{ submitRow.deptName || '—' }}）</div>
        <div v-if="submitRow.diagnosis"><span class="text-gray-400">病历诊断：</span>{{ submitRow.diagnosis }}</div>
      </div>
      <el-form label-width="100px">
        <el-form-item label="主诊断编码" required>
          <el-input v-model="submitForm.mainIcdCode" maxlength="20" placeholder="ICD-10 编码，如 I10.x00" data-testid="ct-submit-code" />
        </el-form-item>
        <el-form-item label="主诊断名称" required>
          <el-input v-model="submitForm.mainIcdName" maxlength="200" placeholder="如 原发性高血压" data-testid="ct-submit-name" />
        </el-form-item>
        <el-form-item label="其他诊断">
          <el-input v-model="submitForm.otherIcdText" type="textarea" :rows="3" maxlength="500" placeholder="其他诊断/手术 ICD，分号分隔（可空）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" data-testid="ct-submit-save" v-perm="'emr:codeTask:edit'" @click="submitTask">提交</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" :title="`审核编码 - ${auditRow?.taskNo || ''}`" width="520px" data-testid="ct-audit-dialog">
      <div v-if="auditRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">主诊断：</span>{{ auditRow.mainIcdCode }} {{ auditRow.mainIcdName }}</div>
        <div v-if="auditRow.otherIcdText"><span class="text-gray-400">其他诊断：</span>{{ auditRow.otherIcdText }}</div>
        <div><span class="text-gray-400">编码员：</span>{{ auditRow.coderName || '—' }}</div>
      </div>
      <el-radio-group v-model="auditApprove" class="mb-3" data-testid="ct-audit-approve">
        <el-radio :value="true">通过</el-radio>
        <el-radio :value="false">退修</el-radio>
      </el-radio-group>
      <el-input
        v-model="auditRemark"
        type="textarea"
        :rows="3"
        maxlength="500"
        :placeholder="auditApprove ? '审核意见（可空）' : '退修意见（必填）'"
        data-testid="ct-audit-remark"
      />
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" data-testid="ct-audit-submit" v-perm="'emr:codeTask:edit'" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`编码任务 ${detail?.taskNo || ''}`" width="640px" data-testid="ct-detail-dialog">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
            <span v-if="Number(detail.returnCount) > 0" class="ml-1 text-xs text-red-500">累计退修 {{ detail.returnCount }} 次</span>
          </el-descriptions-item>
          <el-descriptions-item label="编码员">{{ detail.coderName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="分配时间">{{ detail.assignTime ? (detail.assignTime || '').replace('T', ' ').slice(0, 19) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="病历诊断" :span="2">{{ detail.diagnosis || '—' }}</el-descriptions-item>
          <el-descriptions-item label="主诊断 ICD" :span="2">{{ detail.mainIcdCode ? `${detail.mainIcdCode} ${detail.mainIcdName}` : '—' }}</el-descriptions-item>
          <el-descriptions-item label="其他诊断" :span="2">{{ detail.otherIcdText || '—' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detail.submitTime ? (detail.submitTime || '').replace('T', ' ').slice(0, 19) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.auditTime ? (detail.auditTime || '').replace('T', ' ').slice(0, 19) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ detail.auditRemark || '—' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>
