<script setup lang="ts">
/**
 * 病案借阅/复印工作台（G16 收口）
 *
 * 流通闭环：申请（1 待审核）→ 审核通过：借阅 → 2 已借出（有应还日期，超期每日提醒）
 * → 归还 3 已归还；复印 → 5 已复印；拒绝 → 4 已拒绝（必须写意见）。
 * 规则（服务端收口，前端只做显隐）：
 *  - 借阅只能借「已归档」病历原件；封存病历只开放复印；
 *  - 同一病历同类型存在在途单（待审核/未归还）不允许重复申请；
 *  - 删除仅限待审核单且申请人本人。
 * 类型/状态文案走字典（his_archive_borrow_*），tag 色与超期判定单点 lib/archiveBorrow.js。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Bell } from '@element-plus/icons-vue'
import {
  getBorrowList,
  getBorrowDetail,
  getBorrowStats,
  applyBorrow,
  auditBorrow,
  returnBorrow,
  deleteBorrow,
  notifyBorrowOverdue,
} from '@/api/archiveBorrow'
import { getArchiveList } from '@/api/archive'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { statusTagType, typeTagType, isOverdue } from '@/lib/archiveBorrow'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  borrowNo: '',
  borrowType: null as number | null,
  status: null as number | null,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const stats = reactive({ pending: 0, lentOut: 0, overdue: 0, returned: 0 })

const typeDict = ref<any[]>([])
const statusDict = ref<any[]>([])
const typeText = (v: any) => dictLabelText(typeDict.value, v)
const statusText = (v: any) => dictLabelText(statusDict.value, v)

// 当前登录人（删除按钮显隐；真边界在服务端）
const me = reactive({ employeeId: null as string | null })
try {
  const cached = localStorage.getItem('user-info') || localStorage.getItem('userInfo')
  if (cached) {
    const u = JSON.parse(cached)
    me.employeeId = String(u.employeeId ?? u.employee_id ?? '')
  }
} catch { /* 忽略缓存解析失败，按钮显隐退化为服务端校验 */ }

const canDelete = (row: any) => Number(row.status) === 1
  && !!me.employeeId && String(row.applicantId) === me.employeeId

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.ARCHIVE_BORROW_TYPE},${DICT_TYPE.ARCHIVE_BORROW_STATUS}`
    )
    typeDict.value = res?.data?.[DICT_TYPE.ARCHIVE_BORROW_TYPE] || []
    statusDict.value = res?.data?.[DICT_TYPE.ARCHIVE_BORROW_STATUS] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

const loadStats = async () => {
  try {
    const res: any = await getBorrowStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getBorrowList({
      borrowNo: query.borrowNo.trim() || undefined,
      borrowType: query.borrowType ?? undefined,
      status: query.status ?? undefined,
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
  query.borrowNo = ''
  query.borrowType = null
  query.status = null
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

const runOverdueNotify = async () => {
  try {
    const res: any = await notifyBorrowOverdue()
    if (res.code === 200) ElMessage.success(res.message || '已补跑超期提醒')
    else ElMessage.error(res.message || '补跑失败')
  } catch (e) {
    console.error(e)
    ElMessage.error('补跑失败')
  }
}

// ------------------------------------------------------------------
// 申请弹窗
// ------------------------------------------------------------------
const applyVisible = ref(false)
const saving = ref(false)
const form = reactive({
  archiveId: null as string | null,
  archiveLabel: '',
  archiveStatus: null as number | null,
  borrowType: 1 as number,
  purpose: '',
  expectReturnDate: '',
})

const archiveOptions = ref<any[]>([])
const archiveSearching = ref(false)
let archiveSearchTimer: ReturnType<typeof setTimeout> | null = null

/** 远程搜病案（已归档/已封存；待归档借不了也复不了印） */
const searchArchives = (kw: string) => {
  if (archiveSearchTimer) clearTimeout(archiveSearchTimer)
  archiveSearchTimer = setTimeout(async () => {
    archiveSearching.value = true
    try {
      const res: any = await getArchiveList({ archiveStatus: null, keyword: kw || undefined, pageNum: 1, pageSize: 20 })
      if (res.code === 200) {
        // 只放行可流通的：2 已归档 / 3 已封存
        archiveOptions.value = (res.data?.records || []).filter(
          (a: any) => Number(a.archiveStatus) === 2 || Number(a.archiveStatus) === 3
        )
      }
    } catch (e) {
      console.error('搜索病案失败', e)
    } finally {
      archiveSearching.value = false
    }
  }, 250)
}

const onArchiveChange = (id: string) => {
  const hit = archiveOptions.value.find(a => String(a.id) === String(id))
  form.archiveStatus = hit ? Number(hit.archiveStatus) : null
}

const openApply = () => {
  form.archiveId = null
  form.archiveLabel = ''
  form.archiveStatus = null
  form.borrowType = 1
  form.purpose = ''
  form.expectReturnDate = ''
  archiveOptions.value = []
  applyVisible.value = true
  searchArchives('')
}

const apply = async () => {
  if (!form.archiveId) {
    ElMessage.warning('请选择病案')
    return
  }
  if (!form.purpose.trim()) {
    ElMessage.warning('请填写用途')
    return
  }
  if (form.borrowType === 1 && !form.expectReturnDate) {
    ElMessage.warning('借阅必须选择应归还日期')
    return
  }
  saving.value = true
  try {
    const res: any = await applyBorrow({
      archiveId: form.archiveId,
      borrowType: form.borrowType,
      purpose: form.purpose.trim(),
      expectReturnDate: form.borrowType === 1 ? form.expectReturnDate : undefined,
    })
    if (res.code === 200) {
      ElMessage.success('申请成功，等待病案室审核')
      applyVisible.value = false
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '申请失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('申请失败')
  } finally {
    saving.value = false
  }
}

// ------------------------------------------------------------------
// 审核弹窗（通过 / 拒绝共用）
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
    ElMessage.warning('拒绝必须填写审核意见')
    return
  }
  auditLoading.value = true
  try {
    const res: any = await auditBorrow(auditRow.value.id, auditApprove.value, auditRemark.value.trim() || undefined)
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

const giveBack = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认收到归还：${row.borrowNo}（病历 ${row.recordNo}）？`, '归还确认', { type: 'info' })
  } catch {
    return
  }
  try {
    const res: any = await returnBorrow(row.id)
    if (res.code === 200) {
      ElMessage.success('归还登记成功')
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '归还失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('归还失败')
  }
}

const remove = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除申请单 ${row.borrowNo}？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: any = await deleteBorrow(row.id)
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
// 详情弹窗
// ------------------------------------------------------------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getBorrowDetail(row.id)
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
  loadList()
})
</script>

<template>
  <div>
    <!-- 统计卡 -->
    <div class="mb-3 grid grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待审核</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }} <span class="text-sm font-normal text-gray-400">单</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已借出未还</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.lentOut }} <span class="text-sm font-normal text-gray-400">单</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">超期未还</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.overdue }} <span class="text-sm font-normal text-gray-400">单</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已归还（累计）</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.returned }} <span class="text-sm font-normal text-gray-400">单</span></div>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="单号">
            <el-input
              v-model="query.borrowNo"
              placeholder="单号"
              clearable
              style="width: 180px"
              data-testid="ab-no-filter"
              @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input
              v-model="query.keyword"
              placeholder="病历号/患者姓名/用途"
              clearable
              style="width: 200px"
              @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.borrowType" placeholder="类型" clearable style="width: 120px" :fit-input-width="false">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" :fit-input-width="false" data-testid="ab-status-filter">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" data-testid="ab-search-btn" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button :icon="Bell" data-testid="ab-notify-btn" @click="runOverdueNotify">补跑超期提醒</el-button>
          <el-button type="primary" :icon="Plus" data-testid="ab-apply-btn" v-perm="'emr:archiveBorrow:add'" @click="openApply">借阅/复印申请</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" data-testid="ab-table" stripe :max-height="tableMaxHeight">
        <el-table-column prop="borrowNo" label="单号" width="170" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.borrowType)">{{ typeText(row.borrowType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordNo" label="病历号" width="170" show-overflow-tooltip />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="deptName" label="科室" width="120" show-overflow-tooltip />
        <el-table-column prop="purpose" label="用途" min-width="160" show-overflow-tooltip />
        <el-table-column label="应还日期" width="110" align="center">
          <template #default="{ row }">{{ row.expectReturnDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
            <el-tag v-if="isOverdue(row)" type="danger" size="small" class="ml-1">超期</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="90" />
        <el-table-column label="申请时间" width="165">
          <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'emr:archiveBorrow:edit'" link type="warning" @click="openAudit(row)">审核</el-button>
            <el-button v-if="Number(row.status) === 2" v-perm="'emr:archiveBorrow:edit'" link type="success" @click="giveBack(row)">归还</el-button>
            <el-button v-if="canDelete(row)" v-perm="'emr:archiveBorrow:delete'" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '暂无借阅/复印记录'" />
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

    <!-- 申请弹窗 -->
    <el-dialog v-model="applyVisible" title="借阅/复印申请" width="560px" data-testid="ab-apply-dialog">
      <el-form label-width="100px">
        <el-form-item label="选择病案" required>
          <el-select
            v-model="form.archiveId"
            filterable
            remote
            :remote-method="searchArchives"
            :loading="archiveSearching"
            placeholder="输入病历号/患者姓名搜索（已归档/已封存）"
            style="width: 100%"
            :fit-input-width="false"
            data-testid="ab-form-archive"
            @change="onArchiveChange"
          >
            <el-option
              v-for="a in archiveOptions"
              :key="a.id"
              :label="`${a.recordNo} / ${a.patientName}（${a.deptName || '—'}）`"
              :value="String(a.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.borrowType" data-testid="ab-form-type">
            <el-radio :value="1">借阅（原件，需应还日期）</el-radio>
            <el-radio :value="2">复印</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.borrowType === 1" label="应归还日期" required>
          <el-date-picker
            v-model="form.expectReturnDate"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
            placeholder="选择应归还日期"
            style="width: 100%"
            :fit-input-width="false"
          />
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input v-model="form.purpose" type="textarea" :rows="3" maxlength="500" placeholder="病历讨论 / 医保核查 / 司法取证 / 科研等" data-testid="ab-form-purpose" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="ab-apply-save" v-perm="'emr:archiveBorrow:add'" @click="apply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" :title="`审核 - ${auditRow?.borrowNo || ''}`" width="520px" data-testid="ab-audit-dialog">
      <div v-if="auditRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">病案：</span>{{ auditRow.recordNo }} / {{ auditRow.patientName }}（{{ auditRow.deptName || '—' }}）</div>
        <div><span class="text-gray-400">用途：</span>{{ auditRow.purpose }}</div>
        <div v-if="auditRow.expectReturnDate"><span class="text-gray-400">应还日期：</span>{{ auditRow.expectReturnDate }}</div>
      </div>
      <el-radio-group v-model="auditApprove" class="mb-3" data-testid="ab-audit-approve">
        <el-radio :value="true">通过</el-radio>
        <el-radio :value="false">拒绝</el-radio>
      </el-radio-group>
      <el-input
        v-model="auditRemark"
        type="textarea"
        :rows="3"
        maxlength="500"
        :placeholder="auditApprove ? '审核意见（可空）' : '拒绝理由（必填）'"
        data-testid="ab-audit-remark"
      />
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" data-testid="ab-audit-submit" v-perm="'emr:archiveBorrow:edit'" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`借阅/复印单 ${detail?.borrowNo || ''}`" width="640px" data-testid="ab-detail-dialog">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="类型">
            <el-tag :type="typeTagType(detail.borrowType)">{{ typeText(detail.borrowType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
            <el-tag v-if="isOverdue(detail)" type="danger" size="small" class="ml-1">超期</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="应还日期">{{ detail.expectReturnDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ (detail.createTime || '').replace('T', ' ').slice(0, 19) }}</el-descriptions-item>
          <el-descriptions-item label="用途" :span="2">{{ detail.purpose }}</el-descriptions-item>
          <el-descriptions-item label="审核" :span="2">
            <template v-if="detail.auditTime">
              {{ detail.auditByName }} · {{ (detail.auditTime || '').replace('T', ' ').slice(0, 19) }}<br />{{ detail.auditRemark || '（无意见）' }}
            </template>
            <template v-else>—</template>
          </el-descriptions-item>
          <el-descriptions-item label="借出时间">{{ detail.lendTime ? (detail.lendTime || '').replace('T', ' ').slice(0, 19) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="归还时间">{{ detail.returnTime ? (detail.returnTime || '').replace('T', ' ').slice(0, 19) : '—' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>
