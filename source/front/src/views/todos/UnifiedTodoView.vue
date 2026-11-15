<template>
  <div>
    <div class="mb-3 flex items-center justify-between">
      <div class="text-sm text-slate-500">
        一张列表看全部待办：消息待办（sys_message 待处理）+ 流程待办（biz_flow_task 待审批）。
      </div>
      <span class="text-xs text-slate-400">共 {{ mergedAll.length }} 条待办</span>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" class="flex flex-wrap items-center gap-2" inline>
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" class="!w-52" clearable placeholder="标题/内容/流程/单据号"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="searchForm.source" class="!w-32" clearable placeholder="全部">
              <el-option label="消息待办" value="MESSAGE"/>
              <el-option label="流程待办" value="FLOW"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card v-loading="loading" class="table-card" shadow="never">
      <el-table :data="pageRows" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column align="center" label="来源" width="110">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', row.source === 'FLOW'
              ? 'bg-sky-100 text-sky-700' : 'bg-amber-100 text-amber-700']">
              {{ row.sourceLabel }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="待办事项" min-width="220">
          <template #default="{ row }">
            <div class="whitespace-normal break-words font-medium text-slate-700">{{ row.title }}</div>
            <div v-if="row.subtitle" class="mt-0.5 whitespace-normal break-words text-xs text-slate-500">
              {{ row.subtitle }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="归属" min-width="150">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.belongTo }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', row.stateClass]">
              {{ row.stateLabel }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="time" width="170"/>
        <el-table-column align="center" fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <template v-if="row.source === 'FLOW'">
              <el-button link type="success" @click.stop="openFlow(row.raw, 'PASS')">通过</el-button>
              <el-button link type="danger" @click.stop="openFlow(row.raw, 'REJECT')">驳回</el-button>
            </template>
            <template v-else>
              <el-tooltip v-if="!row.canHandle" :content="`该通知属于${row.ownerRole}岗位，请切换岗位后处理`"
                          placement="top">
                <span class="inline-flex items-center gap-1.5">
                  <el-button :icon="View" disabled link type="info">处理</el-button>
                  <span class="rounded bg-amber-50 px-1.5 py-0.5 text-xs text-amber-700">{{ row.ownerRole }}岗位</span>
                </span>
              </el-tooltip>
              <el-button v-else :icon="View" link type="primary" @click.stop="openMessage(row.raw)">处理</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="pageRows.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无待办
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="mergedAll.length"
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
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {getMessageList, readMessage} from '@/api/system'
import {flowMyTasks} from '@/api/workflow'
import {hasAnyPermission, hasPermission, loadPermissions} from '@/lib/permission'
import {
  messageActionOwner,
  messageActionPermissions,
  messageHandleStatusMeta,
  messageLabel,
} from '@/lib/messageCatalog'
import MessageProcessDialog from '@/components/his/MessageProcessDialog.vue'
import FlowTaskHandleEdit from '@/views/workflow/FlowTaskHandleEdit.vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

// 一类待办一次最多捞 200 条（PageParam 的上限），两个来源各自取满后在内存里合并排序。
// 待办是「当下要办的事」，量级是几十条；超过 200 条说明该加工商/加催办，而不是加一个假分页。
const SOURCE_FETCH_SIZE = 200

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loading = ref(false)
const searchForm = ref({keyword: '', source: ''})
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})

const messageRows = ref([])
const flowRows = ref([])
const canHandleFlow = ref(false)
const canHandleMessage = ref(false)

const normalizeMessage = (m) => ({
  key: 'M-' + m.messageId,
  source: 'MESSAGE',
  sourceLabel: '消息待办',
  title: m.title || '(无标题)',
  subtitle: m.content || '',
  belongTo: messageLabel(m.bizType),
  stateLabel: messageHandleStatusMeta(m.handleStatus)?.label || '待处理',
  stateClass: messageHandleStatusMeta(m.handleStatus)?.tagClass || 'bg-amber-100 text-amber-700',
  time: m.sendTime || '',
  canHandle: canHandleMessage.value && hasAnyPermission(messageActionPermissions(m.bizType)),
  ownerRole: messageActionOwner(m.bizType),
  raw: m,
  sortTime: m.sendTime || '',
})

const normalizeFlow = (t) => ({
  key: 'F-' + t.taskId,
  source: 'FLOW',
  sourceLabel: '流程待办',
  title: t.flowName || '(未命名流程)',
  subtitle: '当前节点：' + (t.nodeName || '—'),
  belongTo: t.businessId || '—',
  stateLabel: t.statusLabel || '待审批',
  stateClass: 'bg-sky-100 text-sky-700',
  time: t.createTime || '',
  canHandle: true,
  ownerRole: '',
  raw: t,
  sortTime: t.createTime || '',
})

const mergedAll = computed(() => {
  const kw = searchForm.value.keyword.trim().toLowerCase()
  const src = searchForm.value.source
  return [...messageRows.value, ...flowRows.value]
      .filter(r => !src || r.source === src)
      .filter(r => !kw || [r.title, r.subtitle, r.belongTo, r.raw.businessId, r.raw.flowName, r.raw.nodeName]
          .some(v => String(v || '').toLowerCase().includes(kw)))
      .sort((a, b) => String(b.sortTime).localeCompare(String(a.sortTime)))
})

const pageRows = computed(() => {
  const start = (pagination.value.pageNum - 1) * pagination.value.pageSize
  return mergedAll.value.slice(start, start + pagination.value.pageSize)
})

const loadData = async () => {
  loading.value = true
  try {
    const messageRes = await getMessageList({handleStatus: 0, pageNum: 1, pageSize: SOURCE_FETCH_SIZE})
    messageRows.value = (messageRes.data?.records || []).map(normalizeMessage)

    if (canHandleFlow.value) {
      const flowRes = await flowMyTasks({pageNum: 1, pageSize: SOURCE_FETCH_SIZE})
      flowRows.value = flowRes.code === 200 ? (flowRes.data?.records || []).map(normalizeFlow) : []
    } else {
      flowRows.value = []
    }
    pagination.value.pageNum = 1
  } catch (e) {
    console.error('加载统一待办失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
}
const handleReset = () => {
  searchForm.value = {keyword: '', source: ''}
  pagination.value.pageNum = 1
}
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
}
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
}

/* ---------- 消息待办处置 ---------- */
const processVisible = ref(false)
const processTarget = ref(null)
const openMessage = async (m) => {
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
