<template>
  <div>
    <!-- 统一待办：一张列表包住「待办」与「全部消息」两个视图，范围与来源都进查询条件。
         数据范围=待办时聚合「消息待办(handle_status=0) + 流程待办(PENDING)」；
         数据范围=全部消息时只列消息（含已读、通知型、已处理），交给后端分页。
         处置按来源走各自弹框：消息 → MessageProcessDialog（危急值/听班呼叫/检查报告/检验报告/会诊
         在组件内各自成页），流程 → FlowTaskHandleEdit（通过/驳回）。 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <!-- 顶部两个页签照厂商医生工作站的口径（东华 iMedical 是「待办工作 / 待阅工作 / 在途申请 / 我的消息」）：
           「待办 / 全部消息」是视图身份，不该混进查询条件下拉 -->
      <div class="mb-3 flex items-center justify-between gap-4">
        <div class="flex items-center gap-1.5">
          <button :class="tabClass('TODO')" type="button" @click="switchScope('TODO')">待办</button>
          <button :class="tabClass('READ')" type="button" @click="switchScope('READ')">待阅</button>
          <button :class="tabClass('MINE')" type="button" @click="switchScope('MINE')">我发起</button>
          <button :class="tabClass('ALL')" type="button" @click="switchScope('ALL')">全部消息</button>
        </div>
        <div class="flex shrink-0 items-center gap-3 text-xs text-slate-500">
          <template v-if="isTodo">
            <span>待办 <strong class="text-slate-800">{{ todoMerged.length }}</strong></span>
            <span :class="overdueTotal ? 'text-red-600' : ''">超时 <strong>{{ overdueTotal }}</strong></span>
            <span :class="urgentTotal ? 'text-amber-600' : ''">紧急 <strong>{{ urgentTotal }}</strong></span>
          </template>
          <span class="text-slate-400">共 {{ totalCount }} 条</span>
        </div>
      </div>
      <el-form :model="searchForm" class="flex flex-wrap items-center gap-2" inline>
        <el-form-item label="来源">
            <!-- 来源 = 消息的业务类型（急诊候诊超时 / 危急值 / 听班呼叫 …）+ 流程待办；
                 不同类型点「处理」弹的是不同页面，这里选哪一类就只看哪一类 -->
            <el-select v-model="searchForm.source" class="!w-44" clearable filterable placeholder="全部"
                       @change="handleConditionChange">
              <el-option v-if="isTodo" label="流程待办" value="FLOW"/>
              <el-option v-for="opt in MESSAGE_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" class="!w-52" clearable placeholder="标题/内容/流程/单据号"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item v-if="isArchive" label="阅读状态">
            <el-select v-model="searchForm.readStatus" class="!w-28" clearable placeholder="全部"
                       @change="handleConditionChange">
              <el-option label="未读" value="0"/>
              <el-option label="已读" value="1"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
    </el-card>

    <el-card v-loading="loading" class="table-card" shadow="never">
      <!-- 行左侧色条取 severity（urgent 红 / warning 橙）：厂商工作站的紧急置顶就是这么表达的。
           整行可点：点哪一行就弹哪一条的详情/处置弹框，不必再找「处理」按钮（操作列按钮保留给键盘/精确操作） -->
      <el-table :data="displayRows" :max-height="tableMaxHeight" :row-class-name="rowClassName" stripe
                style="width: 100%" @row-click="onRowClick">
        <el-table-column align="center" label="来源" width="130">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', row.sourceClass]">
              {{ row.sourceLabel }}
            </span>
          </template>
        </el-table-column>
        <!-- 事项只放标题 + 一行业务摘要（患者/单号/科室），正文在「处理/查看」的弹框里看 -->
        <el-table-column label="事项" min-width="190">
          <template #default="{ row }">
            <div class="flex items-start gap-2">
              <span v-if="row.unread" class="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full bg-red-500"></span>
              <div class="min-w-0">
                <div class="truncate text-slate-700" :title="row.title">{{ row.title }}</div>
                <div v-if="row.chipText" class="mt-0.5 truncate text-xs text-slate-400" :title="row.chipText">
                  {{ row.chipText }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发送人" show-overflow-tooltip width="110">
          <template #default="{ row }">{{ row.sender }}</template>
        </el-table-column>
        <el-table-column label="处理人" show-overflow-tooltip width="110">
          <template #default="{ row }">{{ row.belongTo }}</template>
        </el-table-column>
        <el-table-column v-if="isArchive" align="center" label="阅读状态" width="90">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', row.readClass]">
              {{ row.readLabel }}
            </span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="处理状态" width="100">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', row.stateClass]">
              {{ row.stateLabel }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="time" width="175"/>
        <!-- 不变「截止时间」空列，改成「时限」：待办看剩余/超时，已办回看原时间，没有时限写「无时限」 -->
        <el-table-column label="时限" width="150">
          <template #default="{ row }">
            <span v-if="row.deadlineText" :class="['text-xs font-medium', row.deadlineClass]">{{ row.deadlineText }}</span>
            <span v-else class="text-xs text-slate-400">无时限</span>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="110">
          <template #default="{ row }">
            <!-- 流程：通过 / 驳回（FlowTaskHandleEdit） -->
            <template v-if="row.source === 'FLOW'">
              <el-button link type="success" @click.stop="openFlow(row.raw, 'PASS')">通过</el-button>
              <el-button link type="danger" @click.stop="openFlow(row.raw, 'REJECT')">驳回</el-button>
            </template>
            <!-- 处理/查看统一可点：能不能办由后端按权限与业务规则兜底，
                 列表不再用「请切换岗位」把人挡在门外（处理人一列已写清该谁办）。
                 我发起档里的消息是别人待办的，只给回看，不代别人处置 -->
            <el-button v-else-if="row.raw.handleStatus === 0 && !isMine" :icon="View" link type="primary"
                       @click.stop="openMessage(row.raw)">处理</el-button>
            <el-button v-else link type="primary" @click.stop="openMessage(row.raw)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="displayRows.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        {{ isArchive ? '暂无消息' : '暂无待办' }}
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="totalCount"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <MessageProcessDialog v-model="processVisible" :message="processTarget" @processed="loadData"/>
    <FlowTaskHandleEdit v-model="handleVisible" :mode="handleMode" :task="handleTask" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {getMessageList, readMessage} from '@/api/system'
import {flowMyTasks} from '@/api/workflow'
import {hasAnyPermission, hasPermission, loadPermissions} from '@/lib/permission'
import {
  MESSAGE_TYPE_OPTIONS,
  messageDeadline,
  messageActionOwner,
  messageActionPermissions,
  messageHandleStatusMeta,
  messageLabel,
  messagePayloadChips,
  messageTagClass,
} from '@/lib/messageCatalog'
import MessageProcessDialog from '@/components/his/MessageProcessDialog.vue'
import FlowTaskHandleEdit from '@/views/workflow/FlowTaskHandleEdit.vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

// 待办视图两类来源各自一次取满（PageParam 上限 200），在内存里合并排序分页；
// 全部消息视图数据量无上限，走后端分页，前端只拿当前页。
const TODO_FETCH_SIZE = 200

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const route = useRoute()
const loading = ref(false)
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const searchForm = ref({
  // 数据范围：TODO 待办（默认）/ ALL 全部消息；/messages 老链接重定向过来带 scope=ALL
  scope: String(route.query.scope || '').toUpperCase() === 'ALL' ? 'ALL' : 'TODO',
  source: '',
  keyword: '',
  readStatus: '',
})
const isTodo = computed(() => searchForm.value.scope === 'TODO')
const isMine = computed(() => searchForm.value.scope === 'MINE')
// 非「待办」档（待阅 / 我发起 / 全部消息）都走后端分页
const isArchive = computed(() => !isTodo.value)

const rows = ref([])
const todoMerged = ref([])
const canHandleFlow = ref(false)
const canHandleMessage = ref(false)

/** 截止时间已过且仍待处理 → 标红（时限类消息才有截止时间，其余显示「—」） */
const isOverdue = (deadline, handleStatus) => {
  if (!deadline || handleStatus !== 0)
    return false
  const at = new Date(String(deadline).replace(' ', 'T'))
  return !isNaN(at.getTime()) && at.getTime() < Date.now()
}

/** 时限文案：待办看「剩 X / 超时 X」，已办只回看原截止时间，没有时限返回空串（列表显示「无时限」） */
const deadlineTextOf = (deadline, handleStatus) => {
  if (!deadline)
    return ''
  if (handleStatus !== 0)
    return deadline
  const at = new Date(String(deadline).replace(' ', 'T'))
  if (isNaN(at.getTime()))
    return deadline
  const diff = at.getTime() - Date.now()
  const mins = Math.floor(Math.abs(diff) / 60000)
  const span = mins >= 1440
    ? `${Math.floor(mins / 1440)}天${Math.floor((mins % 1440) / 60)}时`
    : mins >= 60 ? `${Math.floor(mins / 60)}时${mins % 60}分` : `${mins}分`
  return diff < 0 ? `超时 ${span}` : `剩 ${span}`
}

const deadlineClassOf = (deadline, handleStatus) => {
  if (!deadline)
    return ''
  if (handleStatus !== 0)
    return 'text-slate-400'
  return isOverdue(deadline, handleStatus) ? 'text-red-600' : 'text-amber-600'
}

/** payload 里已有的「已经超了多久」的客观事实：病案超期天数、急诊候诊超目标时长、转诊已等待时长。
 *  这些不是推算出来的截止时间，是业务侧算好的时长，可直接当「时限」列的红色/橙色提示。 */
const payloadOf = (m) => {
  try {
    return typeof m.payload === 'string' ? JSON.parse(m.payload) : (m.payload || null)
  } catch {
    return null
  }
}

const elapsedTextOf = (m) => {
  const p = payloadOf(m)
  if (!p)
    return null
  const days = Number(p.overdueDays ?? 0)
  if (days > 0)
    return {text: `超期 ${days}天`, cls: 'text-red-600', red: true}
  const wait = Number(p.waitMinutes ?? 0)
  const target = Number(p.targetSeeMinutes ?? 0)
  if (wait > 0 && target > 0 && wait > target)
    return {text: `超时 ${wait - target}分`, cls: 'text-red-600', red: true}
  const hours = Number(p.waitedHours ?? 0)
  if (hours > 0)
    return {text: `已等待 ${hours}时`, cls: 'text-amber-600', red: false}
  return null
}

/** 发送人：人工发出的写操作人姓名；定时任务自动发的（create_by 为空或 system:xxx）统一显示「系统」 */
const senderOf = (createBy) => {
  const name = String(createBy || '').trim()
  if (!name || name.startsWith('system:'))
    return '系统'
  return name
}

const normalizeMessage = (m) => {
  const meta = messageHandleStatusMeta(m.handleStatus)
  const deadline = messageDeadline(m)
  const deadlineText = deadlineTextOf(deadline, m.handleStatus)
  // 有明确截止时间就用倒计时；没有就用业务侧已算好的「超时/超期/已等待」事实
  const elapsed = m.handleStatus === 0 ? elapsedTextOf(m) : null
  return {
    key: 'M-' + m.messageId,
    source: 'MESSAGE',
    bizType: m.bizType,
    sourceLabel: messageLabel(m.bizType),
    sourceClass: messageTagClass(m.bizType),
    title: m.title || '(无标题)',
    // 列表只放标题：正文可能上千字，点「处理/查看」在弹框里看
    sender: senderOf(m.createBy),
    belongTo: m.receiverName || '',
    // 业务摘要（患者/单号/科室…取 payload 前两项），给标题补上下文，比一整段正文克制
    chipText: messagePayloadChips(m.payload).slice(0, 2).map(c => c.text).join(' · '),
    severity: m.severity || '',
    unread: m.readStatus === 0,
    readLabel: m.readStatus === 0 ? '未读' : '已读',
    readClass: m.readStatus === 0 ? 'bg-red-100 text-red-700' : 'bg-slate-100 text-slate-600',
    // 通知型（handle_status=NULL）没有处置回执，显示「—」而不是假装待处理
    stateLabel: meta?.label || '',
    stateClass: meta?.tagClass || 'bg-slate-100 text-slate-500',
    canProcess: m.handleStatus === 0 && canHandleMessage.value && hasAnyPermission(messageActionPermissions(m.bizType)),
    ownerRole: messageActionOwner(m.bizType),
    time: m.sendTime || '',
    deadline,
    overdue: isOverdue(deadline, m.handleStatus) || Boolean(elapsed?.red),
    deadlineText: deadlineText || elapsed?.text || '',
    deadlineClass: deadlineText ? deadlineClassOf(deadline, m.handleStatus) : (elapsed?.cls || ''),
    raw: m,
    sortTime: m.sendTime || '',
  }
}

const normalizeFlow = (t) => ({
  key: 'F-' + t.taskId,
  source: 'FLOW',
  bizType: '',
  sourceLabel: '流程待办',
  sourceClass: 'bg-sky-100 text-sky-700',
  title: t.flowName || '(未命名流程)',
  subtitle: '当前节点：' + (t.nodeName || ''),
  sender: senderOf(t.createBy || t.startUserName),
  belongTo: t.assigneeName || '',
  chipText: '当前节点：' + (t.nodeName || ''),
  severity: '',
  unread: false,
  readLabel: '',
  readClass: 'bg-slate-100 text-slate-500',
  stateLabel: t.statusLabel || '待审批',
  stateClass: 'bg-sky-100 text-sky-700',
  canProcess: false,
  ownerRole: '',
  time: t.createTime || '',
  deadline: t.dueTime || '',
  overdue: isOverdue(t.dueTime, 0),
  deadlineText: deadlineTextOf(t.dueTime, 0),
  deadlineClass: deadlineClassOf(t.dueTime, 0),
  raw: t,
  sortTime: t.createTime || '',
})

/** 行左侧色条取消息 severity（urgent 危急值 / warning 待办提醒）；可点行额外加手型光标 */
const rowClassName = ({row}) => {
  const classes = []
  if (row.severity === 'urgent')
    classes.push('row-urgent')
  if (row.severity === 'warning')
    classes.push('row-warning')
  if (row.source === 'MESSAGE' && !isMine.value)
    classes.push('row-clickable')
  return classes.join(' ')
}

/** 点行打开该条的详情/处置弹框；流程行与「我发起」档除外（前者要先选通过还是驳回，后者只读） */
const onRowClick = (row, column) => {
  if (column?.type === 'selection')
    return
  if (row.source === 'FLOW' || isMine.value)
    return
  openMessage(row.raw)
}

/** 顶部页签样式：选中态用主色，非选中态浅底 —— 与厂商工作站的「待办/消息」标签一致 */
const tabClass = (scope) => [
  'rounded-md px-4 py-1.5 text-sm transition',
  searchForm.value.scope === scope
    ? 'bg-[#1269B5] font-medium text-white shadow-sm'
    : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
]

/* ---------- 页签切换 ---------- */
const switchScope = (scope) => {
  if (searchForm.value.scope === scope)
    return
  searchForm.value.scope = scope
  handleScopeChange()
}

const overdueTotal = computed(() => todoMerged.value.filter(r => r.overdue).length)
const urgentTotal = computed(() => todoMerged.value.filter(r => r.severity === 'urgent').length)

const totalCount = computed(() => (isArchive.value ? pagination.value.total : todoMerged.value.length))
const displayRows = computed(() => {
  if (isArchive.value)
    return rows.value
  const start = (pagination.value.pageNum - 1) * pagination.value.pageSize
  return todoMerged.value.slice(start, start + pagination.value.pageSize)
})

/* ---------- 待办视图：消息待办 + 流程待办 内存合并 ---------- */
const loadTodo = async () => {
  const src = searchForm.value.source
  const messageParams = {handleStatus: 0, pageNum: 1, pageSize: TODO_FETCH_SIZE}
  // 来源选的是具体业务类型时，只拉这一类消息
  if (src && src !== 'FLOW')
    messageParams.bizType = src
  const messageRes = await getMessageList(messageParams)
  const messageRows = (messageRes.data?.records || []).map(normalizeMessage)

  let flowRows = []
  if (canHandleFlow.value && (!src || src === 'FLOW')) {
    const flowRes = await flowMyTasks({pageNum: 1, pageSize: TODO_FETCH_SIZE})
    flowRows = flowRes.code === 200 ? (flowRes.data?.records || []).map(normalizeFlow) : []
  }

  const kw = searchForm.value.keyword.trim().toLowerCase()
  // 排序照厂商工作站的轻重顺序：危急值(urgent) → 已超时 → 其余按时间倒序，
  // 而不是单纯按时间排让危急值淹没在一堆通知里
  const weight = r => (r.severity === 'urgent' ? 0 : r.overdue ? 1 : 2)
  todoMerged.value = [...messageRows, ...flowRows]
      .filter(r => !src || (src === 'FLOW' ? r.source === 'FLOW' : r.bizType === src))
      .filter(r => !kw || [r.title, r.subtitle, r.belongTo, r.raw.businessId, r.raw.flowName, r.raw.nodeName]
          .some(v => String(v || '').toLowerCase().includes(kw)))
      .sort((a, b) => weight(a) - weight(b) || String(b.sortTime).localeCompare(String(a.sortTime)))
  clampPage()
}

/* ---------- 全部消息视图：后端分页 ---------- */
const loadArchive = async () => {
  const src = searchForm.value.source
  const params = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize}
  // 待阅 = 通知型（handle_status 为空，看完即算）；我发起 = 按发送人取自己发出的
  if (searchForm.value.scope === 'READ')
    params.notifyOnly = true
  if (searchForm.value.scope === 'MINE')
    params.mine = true
  if (searchForm.value.keyword)
    params.keyword = searchForm.value.keyword
  if (src && src !== 'FLOW')
    params.bizType = src
  if (searchForm.value.readStatus !== '')
    params.readStatus = searchForm.value.readStatus
  const res = await getMessageList(params)
  rows.value = (res.data?.records || []).map(normalizeMessage)
  pagination.value.total = Number(res.data?.total || 0)
}

const loadData = async () => {
  loading.value = true
  try {
    if (isArchive.value)
      await loadArchive()
    else
      await loadTodo()
  } catch (e) {
    console.error('加载统一待办失败', e)
  } finally {
    loading.value = false
  }
}

/** 页码越界夹回（内存分页删行后常见） */
const clampPage = () => {
  const pages = Math.max(1, Math.ceil(todoMerged.value.length / pagination.value.pageSize))
  if (pagination.value.pageNum > pages)
    pagination.value.pageNum = pages
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}
const handleConditionChange = () => {
  pagination.value.pageNum = 1
  loadData()
}
const handleScopeChange = () => {
  // 流程待办只存在于「待办」范围，切到全部消息时清掉这个来源
  if (isArchive.value && searchForm.value.source === 'FLOW')
    searchForm.value.source = ''
  pagination.value.pageNum = 1
  loadData()
}
const handleReset = () => {
  searchForm.value = {scope: 'TODO', source: '', keyword: '', readStatus: ''}
  pagination.value.pageNum = 1
  loadData()
}
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  if (isArchive.value)
    loadData()
  else
    clampPage()
}
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  if (isArchive.value)
    loadData()
}

/* ---------- 消息处置（按来源由 MessageProcessDialog 分支渲染） ---------- */
const processVisible = ref(false)
const processTarget = ref(null)
const openMessage = async (m) => {
  // 打开即置读：看过 ≠ 办完，待办型仍需「处理」里走各自闭环
  if (m.readStatus === 0) {
    try {
      await readMessage(m.messageId)
      m.readStatus = 1
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }
  processTarget.value = m
  processVisible.value = true
}

/* ---------- 流程待办处置 ---------- */
const handleVisible = ref(false)
const handleMode = ref('PASS')
const handleTask = ref(null)
const openFlow = (task, mode) => {
  handleTask.value = task
  handleMode.value = mode
  handleVisible.value = true
}

onMounted(async () => {
  await loadPermissions()
  canHandleFlow.value = hasPermission('workflow:task:handle')
  canHandleMessage.value = hasPermission('portal:messages:edit')
  loadData()
})
</script>

<style scoped>
/* 行左侧色条取 severity：危急值(urgent) 红、待办提醒(warning) 橙。
   el-table 的 row-class-name 加在 tr 上，scoped 里必须 :deep 才能命中子组件节点。 */
:deep(.el-table .row-urgent td:first-child) {
  box-shadow: inset 3px 0 0 #ef4444;
}

:deep(.el-table .row-warning td:first-child) {
  box-shadow: inset 3px 0 0 #f59e0b;
}

/* 可点行给手型，免得用户还得去猜哪列是操作入口 */
:deep(.el-table .row-clickable) {
  cursor: pointer;
}
</style>
