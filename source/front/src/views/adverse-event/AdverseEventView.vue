<script setup lang="ts">
/**
 * 不良事件上报（G12）
 *
 * PDCA 四态闭环：1 已上报待处理 → 2 处理中 → 3 已整改 → 4 已结案（不可逆）。
 * 每步流转留「操作人 + 意见 + 时间」痕迹；状态 ≥2 后上报内容与删除均被锁定。
 * 类型/等级/状态文案走字典（his_adverse_event_*），tag 色与动作口径单点 lib/adverseEvent.js。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, WarningFilled } from '@element-plus/icons-vue'
import {
  getAdverseEventList,
  getAdverseEventDetail,
  upsertAdverseEvent,
  handleAdverseEvent,
  rectifyAdverseEvent,
  closeAdverseEvent,
  deleteAdverseEvent,
  getAdverseEventStats,
} from '@/api/adverseEvent'
import { getDepartmentSelectList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { statusTagType, levelTagType, ADVERSE_EVENT_NEXT_ACTION } from '@/lib/adverseEvent'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  eventNo: '',
  eventType: null as number | null,
  eventLevel: null as number | null,
  status: null as number | null,
  occurDeptId: null as number | null,
  keyword: '',
  dateRange: null as [string, string] | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const stats = reactive({ monthReported: 0, pending: 0, sentinel: 0, closed: 0 })

const typeDict = ref<any[]>([])
const levelDict = ref<any[]>([])
const statusDict = ref<any[]>([])
const acquiredDict = ref<any[]>([])
const deptOptions = ref<any[]>([])
const typeText = (v: any) => dictLabelText(typeDict.value, v)
const levelText = (v: any) => dictLabelText(levelDict.value, v)
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const acquiredText = (v: any) => dictLabelText(acquiredDict.value, v)

/** 压力性损伤（字典 his_adverse_event_type=3）：唯一要区分院内获得/入院带入的类型 */
const EVENT_TYPE_PRESSURE_INJURY = 3

// 当前登录人（编辑/删除仅上报人本人；流转动作任何人可点，收口在服务端状态机）
const me = reactive({ employeeId: null as string | null })
try {
  const cached = localStorage.getItem('user-info') || localStorage.getItem('userInfo')
  if (cached) {
    const u = JSON.parse(cached)
    me.employeeId = String(u.employeeId ?? u.employee_id ?? '')
  }
} catch { /* 忽略缓存解析失败，按钮显隐退化为服务端校验 */ }

const isMine = (row: any) => !!me.employeeId && String(row.reporterId) === String(me.employeeId)
const canEdit = (row: any) => Number(row.status) === 1 && isMine(row)
const canDelete = canEdit
const canAct = (row: any, key: string) => ADVERSE_EVENT_NEXT_ACTION[Number(row.status)]?.key === key

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.ADVERSE_EVENT_TYPE},${DICT_TYPE.ADVERSE_EVENT_LEVEL},${DICT_TYPE.ADVERSE_EVENT_STATUS},${DICT_TYPE.ADVERSE_ACQUIRED}`
    )
    typeDict.value = res?.data?.[DICT_TYPE.ADVERSE_EVENT_TYPE] || []
    levelDict.value = res?.data?.[DICT_TYPE.ADVERSE_EVENT_LEVEL] || []
    statusDict.value = res?.data?.[DICT_TYPE.ADVERSE_EVENT_STATUS] || []
    acquiredDict.value = res?.data?.[DICT_TYPE.ADVERSE_ACQUIRED] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({})
    if (res.code === 200) deptOptions.value = res.data || []
  } catch (e) {
    console.error('加载科室失败', e)
  }
}

const loadStats = async () => {
  try {
    const res: any = await getAdverseEventStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getAdverseEventList({
      eventNo: query.eventNo.trim() || undefined,
      eventType: query.eventType ?? undefined,
      eventLevel: query.eventLevel ?? undefined,
      status: query.status ?? undefined,
      occurDeptId: query.occurDeptId ?? undefined,
      keyword: query.keyword.trim() || undefined,
      dateStart: query.dateRange?.[0],
      dateEnd: query.dateRange?.[1],
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
  query.eventNo = ''
  query.eventType = null
  query.eventLevel = null
  query.status = null
  query.occurDeptId = null
  query.keyword = ''
  query.dateRange = null
  query.pageNum = 1
  loadList()
}

// ------------------------------------------------------------------
// 上报 / 修改弹窗
// ------------------------------------------------------------------
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null as string | null,
  eventType: null as number | null,
  eventLevel: null as number | null,
  occurDeptId: null as number | null,
  acquiredFlag: null as number | null,
  occurTime: '',
  patientId: null as string | null,
  patientName: '',
  title: '',
  description: '',
  immediateAction: '',
})

const openCreate = () => {
  form.id = null
  form.eventType = null
  form.eventLevel = null
  form.occurDeptId = null
  form.acquiredFlag = null
  form.occurTime = ''
  form.patientId = null
  form.patientName = ''
  form.title = ''
  form.description = ''
  form.immediateAction = ''
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  form.id = row.id
  form.eventType = Number(row.eventType)
  form.eventLevel = Number(row.eventLevel)
  form.occurDeptId = row.occurDeptId
  form.acquiredFlag = row.acquiredFlag == null ? null : Number(row.acquiredFlag)
  form.occurTime = (row.occurTime || '').slice(0, 16)
  form.patientId = row.patientId
  form.patientName = row.patientName || ''
  form.title = row.title || ''
  form.description = row.description || ''
  form.immediateAction = row.immediateAction || ''
  dialogVisible.value = true
}

const onPatientSelect = (p: any) => {
  form.patientId = p?.id ?? null
  form.patientName = p?.name || p?.patientName || ''
}

const save = async () => {
  if (!form.eventType || !form.eventLevel || !form.occurDeptId) {
    ElMessage.warning('请选择事件类型、等级与发生科室')
    return
  }
  if (!form.occurTime) {
    ElMessage.warning('请选择发生时间')
    return
  }
  if (!form.title.trim() || !form.description.trim()) {
    ElMessage.warning('请填写事件摘要与详细经过')
    return
  }
  // 压疮必须显式选来源：入院带入的皮肤问题不算本院的发生率（护理质控千床日率口径），
  // 让服务端兜默认值等于把「没区分」洗成「院内获得」，评审一查就对不上
  if (form.eventType === EVENT_TYPE_PRESSURE_INJURY && !form.acquiredFlag) {
    ElMessage.warning('压力性损伤请选择来源：院内获得还是入院带入')
    return
  }
  saving.value = true
  try {
    const res: any = await upsertAdverseEvent({
      id: form.id || undefined,
      eventType: form.eventType,
      eventLevel: form.eventLevel,
      occurDeptId: form.occurDeptId,
      acquiredFlag: form.eventType === EVENT_TYPE_PRESSURE_INJURY ? form.acquiredFlag : undefined,
      occurTime: form.occurTime.length === 16 ? `${form.occurTime}:00` : form.occurTime,
      patientId: form.patientId || undefined,
      title: form.title.trim(),
      description: form.description.trim(),
      immediateAction: form.immediateAction.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(form.id ? '修改成功' : '上报成功')
      dialogVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const remove = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除事件 ${row.eventNo}？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: any = await deleteAdverseEvent(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败')
  }
}

// ------------------------------------------------------------------
// PDCA 流转弹窗（处理/整改/结案 共用）
// ------------------------------------------------------------------
const actionVisible = ref(false)
const actionKey = ref<'handle' | 'rectify' | 'close'>('handle')
const actionRow = ref<any>(null)
const actionRemark = ref('')
const actionLoading = ref(false)

const openAction = (row: any, key: 'handle' | 'rectify' | 'close') => {
  actionRow.value = row
  actionKey.value = key
  actionRemark.value = ''
  actionVisible.value = true
}

const actionLabel = () => ADVERSE_EVENT_NEXT_ACTION[actionKey.value]?.label || ''
const actionPlaceholder = () => ADVERSE_EVENT_NEXT_ACTION[actionKey.value]?.placeholder || ''

const submitAction = async () => {
  if (!actionRemark.value.trim()) {
    ElMessage.warning('请填写意见内容')
    return
  }
  actionLoading.value = true
  try {
    const api = actionKey.value === 'handle' ? handleAdverseEvent
      : actionKey.value === 'rectify' ? rectifyAdverseEvent : closeAdverseEvent
    const res: any = await api(actionRow.value.id, actionRemark.value.trim())
    if (res.code === 200) {
      ElMessage.success(`${actionLabel()}成功`)
      actionVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || `${actionLabel()}失败`)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(`${actionLabel()}失败`)
  } finally {
    actionLoading.value = false
  }
}

// ------------------------------------------------------------------
// 详情弹窗
// ------------------------------------------------------------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getAdverseEventDetail(row.id)
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
  loadDepts()
  loadStats()
  loadList()
})
</script>

<template>
  <div>
    <!-- 统计卡 -->
    <div class="grid grid-cols-4 gap-4 mb-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">本月上报</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.monthReported }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待处理</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">I 级警讯（本月）</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.sentinel }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已结案（本月上报）</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.closed }} <span class="text-sm font-normal text-gray-400">件</span></div>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="事件编号">
            <el-input
              v-model="query.eventNo"
              placeholder="事件编号"
              clearable
              style="width: 170px"
              data-testid="ae-no-filter"
              @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input
              v-model="query.keyword"
              placeholder="摘要/经过/患者姓名"
              clearable
              style="width: 200px"
              @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="事件类型">
            <el-select v-model="query.eventType" placeholder="事件类型" clearable style="width: 130px" :fit-input-width="false">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="事件等级">
            <el-select v-model="query.eventLevel" placeholder="事件等级" clearable style="width: 160px" :fit-input-width="false">
              <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" :fit-input-width="false" data-testid="ae-status-filter">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="发生科室">
            <el-select v-model="query.occurDeptId" placeholder="发生科室" clearable filterable style="width: 160px" :fit-input-width="false">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="上报日期">
            <el-date-picker
              v-model="query.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="上报日期起"
              end-placeholder="上报日期止"
              value-format="YYYY-MM-DD"
              style="width: 250px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" data-testid="ae-search-btn" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'emr:adverseEvent:add'" type="warning" :icon="WarningFilled" data-testid="ae-add-btn" @click="openCreate">上报事件</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="ae-table">
        <el-table-column prop="eventNo" label="事件编号" width="170" />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">{{ typeText(row.eventType) }}</template>
        </el-table-column>
        <el-table-column label="等级" width="160" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.eventLevel)">{{ levelText(row.eventLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="occurDeptName" label="发生科室" width="120" show-overflow-tooltip />
        <el-table-column prop="title" label="事件摘要" min-width="180" show-overflow-tooltip />
        <el-table-column label="患者" width="90">
          <template #default="{ row }">{{ row.patientName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="reporterName" label="上报人" width="90" />
        <el-table-column label="状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上报时间" width="165">
          <template #default="{ row }">{{ (row.reportTime || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="215" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canAct(row, 'handle')" link type="warning" @click="openAction(row, 'handle')">处理</el-button>
            <el-button v-if="canAct(row, 'rectify')" link type="success" @click="openAction(row, 'rectify')">整改</el-button>
            <el-button v-if="canAct(row, 'close')" link type="primary" @click="openAction(row, 'close')">结案</el-button>
            <el-button v-if="canEdit(row)" v-perm="'emr:adverseEvent:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canDelete(row)" v-perm="'emr:adverseEvent:delete'" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '暂无不良事件记录'" />
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

    <!-- 上报/修改弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '修改不良事件' : '上报不良事件'"
      width="640px"
      data-testid="ae-dialog"
    >
      <el-form label-width="90px">
        <el-form-item label="事件类型" required>
          <el-select v-model="form.eventType" placeholder="请选择" style="width: 100%" :fit-input-width="false" data-testid="ae-form-type">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件等级" required>
          <el-select v-model="form.eventLevel" placeholder="请选择" style="width: 100%" :fit-input-width="false" data-testid="ae-form-level">
            <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.eventType === EVENT_TYPE_PRESSURE_INJURY" label="压疮来源" required>
          <el-select v-model="form.acquiredFlag" placeholder="院内获得 / 入院带入" style="width: 100%" :fit-input-width="false" data-testid="ae-form-acquired">
            <el-option v-for="d in acquiredDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生科室" required>
          <el-select v-model="form.occurDeptId" placeholder="请选择" filterable style="width: 100%" :fit-input-width="false" data-testid="ae-form-dept">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生时间" required>
          <el-date-picker
            v-model="form.occurTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            placeholder="选择发生时间"
            style="width: 100%"
            :fit-input-width="false"
          />
        </el-form-item>
        <el-form-item label="关联患者">
          <PatientSelect
            v-model="form.patientId"
            placeholder="搜索患者（可空：非患者事件）"
            width="100%"
            @select="onPatientSelect"
            @clear="form.patientId = null; form.patientName = ''"
          />
        </el-form-item>
        <el-form-item label="事件摘要" required>
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话概括事件" data-testid="ae-form-title" />
        </el-form-item>
        <el-form-item label="事件经过" required>
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细经过（时间、地点、人员、过程、后果）" data-testid="ae-form-desc" />
        </el-form-item>
        <el-form-item label="即时处置">
          <el-input v-model="form.immediateAction" type="textarea" :rows="2" placeholder="已采取的即时处置措施（可空）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="['emr:adverseEvent:add','emr:adverseEvent:edit']" type="primary" :loading="saving" data-testid="ae-save-btn" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- PDCA 流转弹窗 -->
    <el-dialog v-model="actionVisible" :title="`${actionLabel()} - ${actionRow?.eventNo || ''}`" width="520px" data-testid="ae-action-dialog">
      <div v-if="actionRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">摘要：</span>{{ actionRow.title }}</div>
      </div>
      <el-input
        v-model="actionRemark"
        type="textarea"
        :rows="4"
        maxlength="500"
        :placeholder="actionPlaceholder()"
        data-testid="ae-action-remark"
      />
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" data-testid="ae-action-submit" @click="submitAction">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`不良事件 ${detail?.eventNo || ''}`" width="680px" data-testid="ae-detail-dialog">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="事件类型">{{ typeText(detail.eventType) }}</el-descriptions-item>
          <el-descriptions-item label="事件等级">
            <el-tag :type="levelTagType(detail.eventLevel)">{{ levelText(detail.eventLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生科室">{{ detail.occurDeptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发生病区">{{ detail.occurWardName || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="Number(detail.eventType) === EVENT_TYPE_PRESSURE_INJURY" label="压疮来源">
            {{ acquiredText(detail.acquiredFlag) || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ (detail.occurTime || '').replace('T', ' ') }}</el-descriptions-item>
          <el-descriptions-item label="关联患者">{{ detail.patientName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="上报人">{{ detail.reporterName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ (detail.reportTime || '').replace('T', ' ') }}</el-descriptions-item>
          <el-descriptions-item label="事件摘要" :span="2">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="详细经过" :span="2">{{ detail.description }}</el-descriptions-item>
          <el-descriptions-item label="即时处置" :span="2">{{ detail.immediateAction || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理（D）" :span="2">
            <template v-if="detail.handleTime">
              {{ detail.handlerName }} · {{ (detail.handleTime || '').replace('T', ' ') }}<br />{{ detail.handleRemark }}
            </template>
            <template v-else>—</template>
          </el-descriptions-item>
          <el-descriptions-item label="整改（C）" :span="2">
            <template v-if="detail.rectifyTime">
              {{ detail.rectifyByName }} · {{ (detail.rectifyTime || '').replace('T', ' ') }}<br />{{ detail.rectifyMeasures }}
            </template>
            <template v-else>—</template>
          </el-descriptions-item>
          <el-descriptions-item label="结案（A）" :span="2">
            <template v-if="detail.closeTime">
              {{ detail.closeByName }} · {{ (detail.closeTime || '').replace('T', ' ') }}<br />{{ detail.verifyRemark }}
            </template>
            <template v-else>—</template>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>
