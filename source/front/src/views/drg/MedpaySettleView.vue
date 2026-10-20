<template>
  <div>
    <!-- 当前医保付费参数 + 批量计算 -->
    <div class="flex flex-wrap items-center gap-3 mb-3">
      <el-tag :type="modeInfo.mode === 'DIP' ? 'warning' : 'primary'" size="large">
        {{ modeInfo.mode === 'DIP' ? 'DIP 按病种分值付费' : 'DRG 按病组付费' }}
      </el-tag>
      <span class="text-sm text-gray-500">DRG 版本 {{ modeInfo.drgVersion || '—' }}
        <template v-if="modeInfo.drgRate"> · 费率 {{ modeInfo.drgRate }} 元/权重</template>
        <template v-else> · 费率未下发</template>
      </span>
      <span class="text-sm text-gray-500">DIP 版本 {{ modeInfo.dipVersion || '—' }}
        <template v-if="modeInfo.pointValue"> · 点值 {{ modeInfo.pointValue }} 元/分值</template>
        <template v-else> · 点值未下发</template>
      </span>
      <el-button :icon="VideoPlay" :loading="batchLoading" class="ml-auto" plain size="small" type="primary"
                 @click="runBatch">批量结算（最近 50 张出院结算账单）
      </el-button>
    </div>

    <!-- 结算结果查询 -->
    <el-card class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="付费方式">
          <el-select v-model="query.payMode" clearable placeholder="全部" style="width: 110px">
            <el-option label="DRG" value="DRG"/>
            <el-option label="DIP" value="DIP"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结算状态">
          <el-select v-model="query.calcStatus" clearable placeholder="全部" style="width: 130px">
            <el-option label="已入组计算" :value="1"/>
            <el-option label="未入组" :value="2"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="患者 / 组码 / 组名" style="width: 190px"
                    @keyup.enter="query.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 结算结果表 -->
    <el-card class="table-card mb-3" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="账单号" prop="billNo" width="170" show-overflow-tooltip/>
        <el-table-column align="center" label="方式" width="60">
          <template #default="{ row }">
            <el-tag :type="row.payMode === 'DIP' ? 'warning' : 'primary'" size="small">{{ row.payMode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="版本" prop="version" width="90" show-overflow-tooltip/>
        <el-table-column label="组码 / 病种" width="110">
          <template #default="{ row }">
            <span v-if="row.groupCode">{{ row.groupCode }}</span>
            <el-tag v-else size="small" type="danger">QY 未入组</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="组名 / 病种名" min-width="170" prop="groupName" show-overflow-tooltip/>
        <el-table-column align="center" label="类型" width="70">
          <template #default="{ row }">{{ row.groupType === 1 ? '核心' : row.groupType === 2 ? '综合' : '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="权重 / 基准" width="85">
          <template #default="{ row }">{{ row.weight ?? row.baseScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="系数 / 分值" width="85">
          <template #default="{ row }">{{ row.coeff != null ? row.coeff + ' / ' + (row.score ?? '—') : (row.rate ?? '—') }}</template>
        </el-table-column>
        <el-table-column align="right" label="应付（元）" width="95">
          <template #default="{ row }">{{ row.insuranceAmount ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="实际费用" width="95">
          <template #default="{ row }">{{ row.actualAmount ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="盈亏" width="95">
          <template #default="{ row }">
            <span v-if="row.profitAmount != null" :class="profitColor(row.profitAmount)">
              {{ Number(row.profitAmount) >= 0 ? '+' : '' }}{{ Number(row.profitAmount).toLocaleString() }}</span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="命中规则" min-width="180" prop="ruleNote" show-overflow-tooltip/>
        <el-table-column align="center" label="计算时间" prop="settleTime" width="130"/>
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="row.calcStatus === 1" link size="small" type="primary" @click="openAppeal(row)">特例单议
            </el-button>
            <span v-else class="text-gray-300">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 特例单议 -->
    <el-card class="table-card" shadow="never">
      <div class="flex items-center mb-2">
        <p class="text-sm font-medium">特例单议（DRG ≤5% / DIP 2027 起 ≤1%，医保发〔2025〕18号配套口径）</p>
      </div>
      <el-form :model="appealQuery" inline class="mb-2" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="appealQuery.status" clearable placeholder="全部" style="width: 130px">
            <el-option label="草稿" :value="1"/>
            <el-option label="已提交" :value="2"/>
            <el-option label="审核通过" :value="3"/>
            <el-option label="审核驳回" :value="4"/>
            <el-option label="已撤回" :value="5"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="appealQuery.pageNum = 1; loadAppeals()">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="appeals" border stripe>
        <el-table-column label="单号" prop="appealNo" width="175"/>
        <el-table-column label="患者" prop="patientName" width="95"/>
        <el-table-column align="center" label="方式" prop="payMode" width="65"/>
        <el-table-column label="组码 / 病种" width="110" prop="groupCode" show-overflow-tooltip/>
        <el-table-column align="center" label="申请类型" width="100">
          <template #default="{ row }">{{ reasonLabel(row.reasonType) }}</template>
        </el-table-column>
        <el-table-column label="申请理由" min-width="170" prop="reasonDesc" show-overflow-tooltip/>
        <el-table-column align="right" label="盈亏快照" width="95">
          <template #default="{ row }">
            <span v-if="row.profitAmount != null" :class="profitColor(row.profitAmount)">
              {{ Number(row.profitAmount) >= 0 ? '+' : '' }}{{ Number(row.profitAmount).toLocaleString() }}</span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核意见" min-width="120" prop="reviewRemark" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link size="small" type="primary" @click="submitAppeal(row)">提交</el-button>
            <el-button v-if="row.status === 1" link size="small" type="warning" @click="withdrawAppeal(row)">撤回</el-button>
            <template v-if="row.status === 2">
              <el-button link size="small" type="success" @click="reviewAppeal(row, true)">通过</el-button>
              <el-button link size="small" type="danger" @click="reviewAppeal(row, false)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="appealQuery.pageNum" :page-size="appealQuery.pageSize" :total="appealTotal"
                       layout="total, prev, pager, next" @current-change="loadAppeals"/>
      </div>
    </el-card>

    <!-- 特例单议创建弹窗 -->
    <el-dialog v-model="appealDialog" :title="'特例单议申请 · ' + (appealTarget?.patientName || '')" width="480px">
      <el-form label-width="90px">
        <el-form-item label="组码 / 病种">
          <el-input :model-value="appealTarget?.groupCode + ' ' + (appealTarget?.groupName || '')" disabled/>
        </el-form-item>
        <el-form-item label="盈亏快照">
          <span :class="profitColor(appealTarget?.profitAmount)">
            {{ appealTarget?.profitAmount != null ? Number(appealTarget.profitAmount).toLocaleString() : '—' }}</span>
        </el-form-item>
        <el-form-item label="申请类型">
          <el-select v-model="appealForm.reasonType" placeholder="选择申请类型" style="width: 100%">
            <el-option label="1-费用极高" :value="1"/>
            <el-option label="2-重大新技术" :value="2"/>
            <el-option label="3-危急重症" :value="3"/>
            <el-option label="4-罕见病" :value="4"/>
            <el-option label="5-其他" :value="5"/>
          </el-select>
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input v-model="appealForm.reasonDesc" :rows="3" maxlength="500" show-word-limit type="textarea"
                    placeholder="说明费用构成、技术/病情特殊性等"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialog = false">取消</el-button>
        <el-button :loading="appealSaving" type="primary" @click="saveAppeal">创建并提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Refresh, Search, VideoPlay} from '@element-plus/icons-vue'
import {
  medpayAppealCreate,
  medpayAppealListPage,
  medpayAppealReview,
  medpayAppealSubmit,
  medpayAppealWithdraw,
  medpayMode,
  medpayResultListPage,
  medpaySettleBatch,
} from '../../api/medpay'

const modeInfo = reactive({mode: 'DRG', drgVersion: '', dipVersion: '', drgRate: '', pointValue: ''})

const loading = ref(false)
const batchLoading = ref(false)
const rows = ref([])
const total = ref(0)
const tableMaxHeight = computed(() => Math.max(360, window.innerHeight - 420))

const query = reactive({payMode: '', calcStatus: undefined, keyword: '', pageNum: 1, pageSize: 20})
const loadList = async () => {
  loading.value = true
  try {
    const res = await medpayResultListPage(query)
    if (res.code === 200) {
      rows.value = res.data?.rows || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

const runBatch = async () => {
  batchLoading.value = true
  try {
    const res = await medpaySettleBatch({})
    if (res.code === 200) {
      ElMessage.success(`批量结算完成：共 ${res.data.total} 张，成功 ${res.data.grouped} 张` +
        (res.data.skipped ? `，跳过 ${res.data.skipped} 张` : ''))
      if (res.data.skippedNote) {
        console.warn('批量结算跳过明细：', res.data.skippedNote)
      }
      await loadList()
      await loadAppeals()
    }
  } finally {
    batchLoading.value = false
  }
}

const reset = () => {
  query.payMode = ''
  query.calcStatus = undefined
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

// ---------------- 特例单议 ----------------
const appealQuery = reactive({status: undefined, pageNum: 1, pageSize: 20})
const appeals = ref([])
const appealTotal = ref(0)
const appealDialog = ref(false)
const appealSaving = ref(false)
const appealTarget = ref(null)
const appealForm = reactive({reasonType: undefined, reasonDesc: ''})

const loadAppeals = async () => {
  const res = await medpayAppealListPage(appealQuery)
  if (res.code === 200) {
    appeals.value = res.data?.rows || []
    appealTotal.value = res.data?.total || 0
  }
}

const openAppeal = (row) => {
  appealTarget.value = row
  appealForm.reasonType = undefined
  appealForm.reasonDesc = ''
  appealDialog.value = true
}

const saveAppeal = async () => {
  if (!appealForm.reasonType) {
    ElMessage.warning('请选择申请类型')
    return
  }
  appealSaving.value = true
  try {
    const res = await medpayAppealCreate({
      settleId: appealTarget.value.id,
      reasonType: appealForm.reasonType,
      reasonDesc: appealForm.reasonDesc,
    })
    if (res.code === 200) {
      ElMessage.success('特例单议草稿已创建')
      appealDialog.value = false
      const id = res.data?.id
      if (id) {
        await submitAppeal({id})
      }
      await loadAppeals()
    }
  } finally {
    appealSaving.value = false
  }
}

const submitAppeal = async (row) => {
  const res = await medpayAppealSubmit(row.id)
  if (res.code === 200) {
    ElMessage.success('特例单议已提交')
    await loadAppeals()
  }
}

const withdrawAppeal = async (row) => {
  const res = await medpayAppealWithdraw(row.id)
  if (res.code === 200) {
    ElMessage.success('特例单议已撤回')
    await loadAppeals()
  }
}

const reviewAppeal = async (row, approved) => {
  try {
    const {value} = await ElMessageBox.prompt(
      approved ? '确认通过该特例单议？' : '确认驳回该特例单议？',
      approved ? '审核通过' : '审核驳回', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPlaceholder: '审核意见（选填）',
      })
    const res = await medpayAppealReview({id: row.id, approved, reviewRemark: value || ''})
    if (res.code === 200) {
      ElMessage.success('审核完成')
      await loadAppeals()
    }
  } catch (e) {
    // 取消弹窗不处理
  }
}

// ---------------- 展示口径 ----------------
const reasonLabel = (t) => ({1: '费用极高', 2: '重大新技术', 3: '危急重症', 4: '罕见病', 5: '其他'}[t] || t)
const statusLabel = (s) => ({1: '草稿', 2: '已提交', 3: '审核通过', 4: '审核驳回', 5: '已撤回'}[s] || s)
const statusTag = (s) => ({1: 'info', 2: 'warning', 3: 'success', 4: 'danger', 5: 'info'}[s] || 'info')
const profitColor = (p) => {
  if (p == null) return 'text-gray-400'
  const n = Number(p)
  if (n >= 0) return 'text-green-600 font-medium'
  return 'text-red-600 font-medium'
}

onMounted(async () => {
  const res = await medpayMode()
  if (res.code === 200 && res.data) {
    Object.assign(modeInfo, res.data)
  }
  await Promise.all([loadList(), loadAppeals()])
})
</script>
