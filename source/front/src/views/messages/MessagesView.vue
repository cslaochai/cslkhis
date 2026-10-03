<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {getMessageList, readMessage} from '@/api/system'
import {loadPermissions, hasAnyPermission} from '@/lib/permission'
import {
  messageActionOwner,
  messageActionPermissions,
  messageHandleStatusMeta,
  messageLabel,
  messagePayloadChips,
  messageTagClass,
  MESSAGE_TYPE_OPTIONS,
} from '@/lib/messageCatalog'
// 详情渲染 + 危急值闭环走全站唯一实现（Header 抽屉与消息页共用）
import MessageProcessDialog from '@/components/his/MessageProcessDialog.vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const messages = ref<any[]>([])

const searchForm = ref({
  keyword: '',
  bizType: '' as string,
  readStatus: '' as string,
  handleStatus: '' as string,
})

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const unreadCount = ref(0)

/**
 * 这条通知「当前角色能不能处理」。
 *
 * 为什么不按角色过滤列表（这是刻意的）：通知是投给**人**的待办，不是投给角色的菜单。
 * 医生下班前切回收费员身份去收个款，如果没处理完的危急值就消失了，他会漏病人。
 * 所以列表照常可见，只把「处理」按当前角色权限锁上，并标明它属于哪个岗位。
 *
 * 口径见 lib/messageCatalog.js（页面不写映射）。
 */
const canProcess = (row: any) => hasAnyPermission(messageActionPermissions(row?.bizType))
const actionOwner = (row: any) => messageActionOwner(row?.bizType)

// 阅读状态
const readStatusMap: Record<number, { label: string; color: string }> = {
  0: {label: '未读', color: 'bg-red-100 text-red-700'},
  1: {label: '已读', color: 'bg-slate-100 text-slate-600'},
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    }
    if (searchForm.value.keyword) params.keyword = searchForm.value.keyword
    if (searchForm.value.bizType) params.bizType = searchForm.value.bizType
    if (searchForm.value.readStatus) params.readStatus = searchForm.value.readStatus
    if (searchForm.value.handleStatus !== '') params.handleStatus = searchForm.value.handleStatus

    const res = await getMessageList(params)
    messages.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
    if (!searchForm.value.readStatus) {
      unreadCount.value = messages.value.filter(m => m.readStatus === 0).length
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    bizType: '',
    readStatus: '',
    handleStatus: '',
  }
  handleSearch()
}

const markAsRead = async (row: any) => {
  if (row.readStatus === 0) {
    try {
      await readMessage(row.messageId)
      row.readStatus = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

/* ---------- 处理：统一走共用详情弹窗 ---------- */

const processVisible = ref(false)
const processTarget = ref<any>(null)

const handleProcess = async (row: any) => {
  // 兜底再判一次：按钮已禁用，但键盘/程序化触发不该绕过（真正的边界在服务端）
  if (!canProcess(row)) {
    ElMessage.warning(`该通知属于${actionOwner(row)}岗位，当前角色无法处理，请切换岗位后再处理`)
    return
  }
  await markAsRead(row)
  processTarget.value = row
  processVisible.value = true
}

// 危急值在弹窗里完成了接收/处置 → 列表时间轴可能已变化，静默刷新当前页
const handleProcessed = () => {
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

onMounted(async () => {
  // 先判定当前角色权限再取数：否则首屏会先渲染成"可处理"，权限到位后再翻转成禁用，
  // 用户会看到按钮闪一下（也可能因此误以为处理过）
  await loadPermissions()
  loadData()
})
</script>

<template>
  <div>
    <!-- 搜索条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form inline class="flex flex-wrap items-center gap-2">
        <el-form-item label="消息内容">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable class="!w-48"
                    @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="业务类型">
          <!-- 选项由 lib/messageCatalog 目录驱动（含危急值等全部码值，页面不再手写） -->
          <el-select v-model="searchForm.bizType" placeholder="全部" clearable filterable class="!w-36">
            <el-option v-for="opt in MESSAGE_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="阅读状态">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="!block">
            <el-select v-model="searchForm.readStatus" placeholder="全部" clearable class="!w-28">
              <el-option label="未读" value="0"/>
              <el-option label="已读" value="1"/>
            </el-select>
          </el-badge>
        </el-form-item>
        <el-form-item label="处理状态">
          <!-- 待办型消息的处置回执（sql/70 handle_status）；通知型为空，选「待处理/已处理」即过滤待办 -->
          <el-select v-model="searchForm.handleStatus" placeholder="全部" clearable class="!w-28">
            <el-option label="待处理" value="0"/>
            <el-option label="已处理" value="1"/>
          </el-select>
        </el-form-item>
        <el-form-item class="ml-auto">
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="messages" v-loading="loading" stripe :max-height="tableMaxHeight" style="width: 100%">
        <el-table-column label="业务类型" width="110" align="center">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', messageTagClass(row.bizType)]">
              {{ messageLabel(row.bizType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="标题" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span v-if="row.readStatus === 0"
                    class="inline-block h-2.5 w-2.5 flex-shrink-0 rounded-full bg-red-500 shadow-[0_0_6px_rgba(239,68,68,0.6)]"></span>
              <span :class="[row.readStatus === 0 ? 'font-semibold text-red-700' : 'text-slate-600']">
                  <div class="whitespace-normal break-words leading-relaxed"> {{ row.title }}</div>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="whitespace-normal break-words leading-relaxed">{{ row.content }}</div>
            <div v-if="messagePayloadChips(row.payload).length" class="mt-1 flex flex-wrap gap-1">
              <span v-for="chip in messagePayloadChips(row.payload)" :key="chip.label"
                    class="rounded bg-slate-100 px-1.5 py-0.5 text-xs text-slate-600">
                {{ chip.label }}：{{ chip.text }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="接收人" width="100"/>
        <el-table-column prop="channel" label="来源" width="100"/>
        <el-table-column prop="sendTime" label="发送时间" width="200"/>
        <el-table-column label="阅读状态" width="90" align="center">
          <template #default="{ row }">
            <span
                :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', readStatusMap[row.readStatus]?.color || 'bg-slate-100 text-slate-600']">
              {{ readStatusMap[row.readStatus]?.label || '未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="readTime" label="阅读时间" width="200"/>
        <el-table-column label="处理状态" width="90" align="center">
          <template #default="{ row }">
            <!-- 通知型（handleStatus=NULL）不渲染；看过≠办完，待办型必须有处置回执 -->
            <span v-if="messageHandleStatusMeta(row.handleStatus)"
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', messageHandleStatusMeta(row.handleStatus).tagClass]">
              {{ messageHandleStatusMeta(row.handleStatus).label }}
            </span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" align="center" fixed="right">
          <template #default="{ row }">
            <!-- 当前角色处理不了这类通知：按钮禁用 + 标明它属于哪个岗位。
                 不做"列表里直接隐藏"——通知是投给人的待办，切角色时让它消失会漏病人。 -->
            <template v-if="canProcess(row)">
              <el-button v-perm="'portal:messages:edit'" type="primary" link :icon="View" @click="handleProcess(row)">处理</el-button>
            </template>
            <template v-else>
              <el-tooltip
                  :content="`该通知属于${actionOwner(row)}岗位，当前角色无法处理，请切换岗位后再处理`"
                  placement="top"
              >
                <span class="inline-flex items-center gap-1.5">
                  <el-button type="info" link :icon="View" disabled>处理</el-button>
                  <span class="rounded bg-amber-50 px-1.5 py-0.5 text-xs text-amber-700">{{ actionOwner(row) }}岗位</span>
                </span>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="messages.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无消息
      </div>
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

    <!-- 详情/处理弹窗：全站唯一实现（含危急值确认接收 → 处置措施闭环） -->
    <MessageProcessDialog
        v-model="processVisible"
        :message="processTarget"
        @processed="handleProcessed"
    />
  </div>
</template>
