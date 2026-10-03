<script setup lang="js">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, ChatDotRound, Select } from '@element-plus/icons-vue'
import { getTicketList, getTicketStats, getTicketDetail, ticketHandle } from '@/api/serviceTicket'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const searchForm = ref({
  keyword: '',
  status: null,
  onlyMine: false,
})
const tableData = ref([])
const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })
const stats = ref({ waitAccept: 0, handling: 0, finished: 0, closed: 0, overdueWaitAccept: 0 })

const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const STATUS_OPTIONS = [
  { value: 0, label: '待受理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '已办结' },
  { value: 3, label: '已关闭' },
]

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const detail = ref(null)
const handleForm = reactive({ id: '', action: '', content: '', visibleToPatient: 1 })
const submitting = ref(false)

const statusTagType = (s) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' })[s] || 'info'
const statusText = (s) => (STATUS_OPTIONS.find((o) => o.value === s) || { label: '未知' }).label

onMounted(() => {
  loadStats()
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTicketList({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getTicketStats()
    if (res.code === 200) stats.value = res.data
  } catch {
    /* 统计失败不影响列表 */
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value.keyword = ''
  searchForm.value.status = null
  searchForm.value.onlyMine = false
  pagination.value.pageNum = 1
  loadData()
}

const openDetail = async (row) => {
  drawerVisible.value = true
  drawerLoading.value = true
  handleForm.id = String(row.id)
  handleForm.action = ''
  handleForm.content = ''
  handleForm.visibleToPatient = 1
  try {
    const res = await getTicketDetail(row.id)
    if (res.code === 200) detail.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    drawerLoading.value = false
  }
}

// 快捷受理：列表里直接点，不用进抽屉
const quickAccept = async (row) => {
  try {
    await ticketHandle({ id: String(row.id), action: 'accept' })
    ElMessage.success('已受理')
    loadData()
    loadStats()
  } catch (error) {
    ElMessage.error(error.message || '受理失败')
  }
}

const submitHandle = async () => {
  if (!handleForm.action) {
    ElMessage.warning('请选择要执行的动作')
    return
  }
  if (handleForm.action !== 'accept' && !handleForm.content.trim()) {
    ElMessage.warning('请填写内容')
    return
  }
  const needConfirm = handleForm.action === 'close'
  if (needConfirm) {
    try {
      await ElMessageBox.confirm('关闭后患者不能再补充留言，确定关闭这张工单吗？', '关闭确认', { type: 'warning' })
    } catch {
      return
    }
  }
  submitting.value = true
  try {
    await ticketHandle({
      id: handleForm.id,
      action: handleForm.action,
      content: handleForm.content.trim(),
      visibleToPatient: handleForm.visibleToPatient,
    })
    ElMessage.success('已提交')
    handleForm.content = ''
    // 刷新详情与列表
    const res = await getTicketDetail(handleForm.id)
    if (res.code === 200) detail.value = res.data
    loadData()
    loadStats()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  loadData()
}
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  loadData()
}
</script>

<template>
  <div>
    <!-- 工作台统计：客服进来第一件事是看有没有新单没人接 -->
    <el-card class="mb-3" shadow="never">
      <div class="flex items-center gap-8">
        <div>
          <div class="text-xs text-gray-500">待受理</div>
          <div class="text-2xl font-bold" :class="stats.waitAccept > 0 ? 'text-orange-500' : ''">
            {{ stats.waitAccept }}
          </div>
        </div>
        <div>
          <div class="text-xs text-gray-500">处理中</div>
          <div class="text-2xl">{{ stats.handling }}</div>
        </div>
        <div>
          <div class="text-xs text-gray-500">已办结</div>
          <div class="text-2xl">{{ stats.finished }}</div>
        </div>
        <div>
          <div class="text-xs text-gray-500">已关闭</div>
          <div class="text-2xl">{{ stats.closed }}</div>
        </div>
        <div v-if="stats.overdueWaitAccept > 0" class="ml-auto">
          <el-tag type="danger" size="small">
            有 {{ stats.overdueWaitAccept }} 张单超过 24 小时没人受理
          </el-tag>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="工单">
          <el-input
            v-model="searchForm.keyword"
            placeholder="工单号 / 内容 / 姓名 / 电话"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="o in STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="searchForm.onlyMine" label="只看我受理的" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="messageNo" label="工单号" width="150" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="acceptByName" label="受理人" width="110">
          <template #default="{ row }">
            <span v-if="row.acceptByName">{{ row.acceptByName }}</span>
            <span v-else class="text-orange-500 text-xs">无人受理</span>
          </template>
        </el-table-column>
        <el-table-column prop="replyCount" label="回复" width="70" />
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              v-perm="'service:ticket:handle'"
              type="primary"
              link
              :icon="Select"
              @click="quickAccept(row)"
            >受理</el-button>
            <el-button type="primary" link :icon="View" @click="openDetail(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-drawer v-model="drawerVisible" title="工单处理" size="620px">
      <div v-loading="drawerLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small" class="mb-3">
            <el-descriptions-item label="工单号">{{ detail.messageNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(detail.status)" size="small">{{ detail.statusText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="就诊人">{{ detail.patientName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="受理人">
              {{ detail.acceptByName || '无人受理' }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="问题" :span="2">{{ detail.content }}</el-descriptions-item>
          </el-descriptions>

          <div class="text-sm font-bold mb-2">流转记录</div>
          <el-timeline class="mb-3">
            <el-timeline-item
              v-for="(log, i) in detail.logs || []"
              :key="i"
              :type="log.visibleToPatient === 1 ? 'primary' : 'info'"
              :timestamp="log.createTime"
              placement="top"
            >
              <div>
                <b>{{ log.actionText }}</b>
                <span class="text-gray-500 text-xs ml-2">{{ log.operatorName || '' }}</span>
                <el-tag v-if="log.visibleToPatient === 0" size="small" type="info" class="ml-2">内部备注</el-tag>
              </div>
              <div class="text-gray-600 text-xs mt-1">{{ log.content }}</div>
            </el-timeline-item>
            <el-timeline-item v-if="!detail.logs || detail.logs.length === 0" timestamp="">
              暂无流转记录（sql/221 之前提交的工单没有留痕）
            </el-timeline-item>
          </el-timeline>

          <el-divider content-position="left">处理</el-divider>
          <el-radio-group v-model="handleForm.action" class="mb-2">
            <el-radio-button v-if="detail.status === 0" value="accept" :disabled="false">受理</el-radio-button>
            <el-radio-button v-if="detail.status !== 3" value="reply">回复患者</el-radio-button>
            <el-radio-button v-if="detail.status === 1" value="finish">办结</el-radio-button>
            <el-radio-button v-if="detail.status !== 3" value="close">关闭</el-radio-button>
            <el-radio-button value="note">内部备注</el-radio-button>
          </el-radio-group>

          <div v-if="handleForm.action === 'reply' || handleForm.action === 'note'" class="mb-2">
            <el-checkbox
              v-model="handleForm.visibleToPatient"
              :true-value="1"
              :false-value="0"
            >患者可见</el-checkbox>
            <span class="text-xs text-gray-400 ml-2">
              勾选后这条会出现在患者端时间轴；内部备注请取消勾选
            </span>
          </div>

          <el-input
            v-if="handleForm.action && handleForm.action !== 'accept'"
            v-model="handleForm.content"
            type="textarea"
            :rows="4"
            :placeholder="
              handleForm.action === 'finish' ? '写清楚办了什么、结果是什么（患者会看到）' :
              handleForm.action === 'close' ? '关闭原因（患者会看到）' :
              handleForm.action === 'note' ? '内部备注（默认患者不可见）' : '回复内容'"
          />
          <div v-else class="text-xs text-gray-400 mb-2">受理不需要填内容，受理人取当前登录人</div>

          <div class="mt-3">
            <el-button
              v-perm="'service:ticket:handle'"
              type="primary"
              :icon="handleForm.action === 'finish' ? Select : ChatDotRound"
              :loading="submitting"
              @click="submitHandle"
            >提交</el-button>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>
