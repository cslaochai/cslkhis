<script setup lang="ts">
/**
 * 静配中心（PIVAS）工作台
 *
 * 链路：按病区/日期查询静脉用药医嘱 → 生成静配单（同入院同日复用主单、明细追加，幂等）
 * → 药师审方（通过/退回，退回原因必填、医嘱可重新入单）→ 打标签排队取号（标签打印预留）
 * → 调配 → 成品核对发放（终态）。
 * 规则（服务端收口，前端只做显隐）：
 *  - 候选口径 = 住院摆药 ∩ route 静脉关键词（静滴/静注/静推/静脉/泵入）；
 *  - 未来日期不可生成；仍有待审方明细时不可打标签；
 *  - 状态机 1待审方→2已审方→3已排队→4已调配→5已核对发放 顺序推进不可跳级，1 可退回 0；
 *  - 计费不在本链（仍走住院摆药），本页面零库存/费用动作。
 * 状态文案走字典（his_pivas_*），tag 色与动作显隐单点 lib/pivas.js。
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, CircleCheck, CircleClose, Tickets, Van, Box } from '@element-plus/icons-vue'
import {
  listPivasCandidates,
  generatePivas,
  listPivasPage,
  getPivasDetail,
  auditPivasItem,
  labelPivasBatch,
  compoundPivasItem,
  verifyPivasItem,
  getPivasStats,
} from '@/api/pivas'
import { getInpatientWardList } from '@/api/inpatient'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, formatMoney } from '@/lib/utils'
import { pivasItemStatusTag, pivasBatchStatusTag, canAudit, canCompound, canVerify, canLabel } from '@/lib/pivas'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  wardId: null as number | null,
  admixDate: '' as string,
  patientName: '',
  status: null as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const stats = reactive({
  pendingAudit: 0, audited: 0, queued: 0, compounded: 0, verified: 0, rejected: 0, batchCount: 0,
})

const wards = ref<any[]>([])
const batchStatusDict = ref<any[]>([])
const itemStatusDict = ref<any[]>([])
const batchStatusText = (v: any) => dictLabelText(batchStatusDict.value, v)
const itemStatusText = (v: any) => dictLabelText(itemStatusDict.value, v)

// ---------------- 生成区 ----------------
const gen = reactive({ wardId: null as number | null, admixDate: today() })
const candidates = ref<any[]>([])
const candidatesLoading = ref(false)
const generating = ref(false)
const unmatchedNote = ref('')

const loadCandidates = async () => {
  if (!gen.wardId) {
    ElMessage.warning('请先选择病区')
    return
  }
  candidatesLoading.value = true
  candidates.value = []
  unmatchedNote.value = ''
  try {
    const res: any = await listPivasCandidates({
      wardId: gen.wardId,
      admixDate: gen.admixDate || undefined,
    })
    if (res.code === 200) {
      candidates.value = res.data || []
      if (!candidates.value.length) ElMessage.info('该病区当天没有可入静配单的静脉用药医嘱')
    } else {
      ElMessage.error(res.message || '查询静脉用药医嘱失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询静脉用药医嘱失败')
  } finally {
    candidatesLoading.value = false
  }
}

const doGenerate = async () => {
  if (!gen.wardId) {
    ElMessage.warning('请先选择病区')
    return
  }
  generating.value = true
  try {
    const res: any = await generatePivas({
      wardId: gen.wardId,
      admixDate: gen.admixDate || undefined,
    })
    if (res.code === 200) {
      const items = res.data?.items || []
      ElMessage.success(`静配单 ${res.data?.pivasNo} 已生成（${items.length} 条明细待审方）${res.data?.remark ? '；' + res.data.remark : ''}`)
      unmatchedNote.value = res.data?.remark || ''
      candidates.value = []
      await Promise.all([loadList(), loadStats()])
      openDetail(res.data?.id)
    } else {
      ElMessage.error(res.message || '生成静配单失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('生成静配单失败')
  } finally {
    generating.value = false
  }
}

// ---------------- 列表 ----------------
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.PIVAS_BATCH_STATUS},${DICT_TYPE.PIVAS_ITEM_STATUS}`
    )
    batchStatusDict.value = res?.data?.[DICT_TYPE.PIVAS_BATCH_STATUS] || []
    itemStatusDict.value = res?.data?.[DICT_TYPE.PIVAS_ITEM_STATUS] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

const loadWards = async () => {
  try {
    const res: any = await getInpatientWardList()
    if (res.code === 200) wards.value = res.data || []
  } catch (e) {
    console.error('加载病区失败', e)
  }
}

const loadStats = async () => {
  try {
    const res: any = await getPivasStats({
      admixDate: query.admixDate || undefined,
      wardId: query.wardId ?? undefined,
    })
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listPivasPage({
      wardId: query.wardId ?? undefined,
      admixDate: query.admixDate || undefined,
      patientName: query.patientName.trim() || undefined,
      status: query.status ?? undefined,
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
  query.wardId = null
  query.admixDate = ''
  query.patientName = ''
  query.status = null
  query.pageNum = 1
  loadList()
  loadStats()
}

// ---------------- 明细弹窗与动作 ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const detailLoading = ref(false)
const actLoading = ref(false)

const statCards = computed(() => [
  { label: '待审方', value: stats.pendingAudit, tone: 'text-amber-600' },
  { label: '已审方待排队', value: stats.audited, tone: 'text-sky-700' },
  { label: '已排队待调配', value: stats.queued, tone: 'text-sky-700' },
  { label: '已调配待核对', value: stats.compounded, tone: 'text-amber-600' },
  { label: '已核对发放', value: stats.verified, tone: 'text-emerald-700' },
  { label: '已拒配', value: stats.rejected, tone: 'text-slate-500' },
  { label: '静配单数', value: stats.batchCount, tone: 'text-[#1269B5]' },
])

const openDetail = async (id: any) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res: any = await getPivasDetail(id)
    if (res.code === 200) detail.value = res.data
    else ElMessage.error(res.message || '查询详情失败')
  } catch (e) {
    console.error(e)
    ElMessage.error('查询详情失败')
  } finally {
    detailLoading.value = false
  }
}

const refreshDetail = async () => {
  if (detail.value?.id) await openDetail(detail.value.id)
  await Promise.all([loadList(), loadStats()])
}

const onAuditPass = async (item: any) => {
  actLoading.value = true
  try {
    const res: any = await auditPivasItem({ itemId: item.id, pass: true })
    if (res.code === 200) {
      ElMessage.success(`审方通过（${item.drugName} × ${item.quantity}）`)
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '审方失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('审方失败')
  } finally {
    actLoading.value = false
  }
}

const onAuditReject = async (item: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认退回「${item.drugName} × ${item.quantity}」？退回后该医嘱不进本次调配，可重新生成入单。`,
      '审方退回原因（必填）',
      { confirmButtonText: '确认退回', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '退回原因必填' },
    )
    actLoading.value = true
    const res: any = await auditPivasItem({ itemId: item.id, pass: false, reason: value.trim() })
    if (res.code === 200) {
      ElMessage.success('已退回（该医嘱可重新入单）')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '退回失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
  } finally {
    actLoading.value = false
  }
}

const onLabelBatch = async () => {
  if (!detail.value?.id) return
  try {
    await ElMessageBox.confirm(
      '确认对该单打标签排队？已审方明细将统一取排队号（标签打印为预留：只取号盖时间，不驱动打印机）。',
      '打标签排队',
      { confirmButtonText: '确认排队', cancelButtonText: '取消' },
    )
    actLoading.value = true
    const res: any = await labelPivasBatch({ batchId: detail.value.id })
    if (res.code === 200) {
      ElMessage.success('已排队取号，等待调配')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '打标签排队失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
  } finally {
    actLoading.value = false
  }
}

const onCompound = async (item: any) => {
  actLoading.value = true
  try {
    const res: any = await compoundPivasItem({ itemId: item.id })
    if (res.code === 200) {
      ElMessage.success(`调配完成（${item.drugName}）`)
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '调配失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('调配失败')
  } finally {
    actLoading.value = false
  }
}

const onVerify = async (item: any) => {
  actLoading.value = true
  try {
    const res: any = await verifyPivasItem({ itemId: item.id })
    if (res.code === 200) {
      ElMessage.success('成品核对通过，已配送病区')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '核对发放失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('核对发放失败')
  } finally {
    actLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadDicts(), loadWards()])
  await loadList()
  await loadStats()
})
</script>

<template>
  <div>
    <!-- 统计卡 -->
    <div class="mb-3 grid grid-cols-7 gap-3">
      <div v-for="c in statCards" :key="c.label" class="bg-white rounded-lg border border-slate-200 px-4 py-3">
        <div class="text-xs text-slate-500">{{ c.label }}</div>
        <div class="text-2xl font-semibold mt-1" :class="c.tone">{{ c.value }}</div>
      </div>
    </div>

    <!-- 生成静配单 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white p-4 space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="font-semibold text-slate-800">生成静配单（静脉用药医嘱）</h3>
        <span v-if="unmatchedNote" class="text-xs text-amber-600">{{ unmatchedNote }}</span>
      </div>
      <div class="flex items-center gap-3 flex-wrap">
        <el-select v-model="gen.wardId" placeholder="选择病区" class="w-64" :fit-input-width="false" data-testid="pivas-ward-select">
          <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
        </el-select>
        <el-date-picker v-model="gen.admixDate" type="date" value-format="YYYY-MM-DD"
                        placeholder="调配日期" class="w-44" :clearable="false" />
        <el-button :icon="Search" :loading="candidatesLoading" @click="loadCandidates">查询静脉用药医嘱</el-button>
        <el-button v-perm="'pharmacy:pivas:add'" type="primary" :icon="Plus" :loading="generating" :disabled="!candidates.length" data-testid="pivas-generate-btn"
                   @click="doGenerate">生成静配单（{{ candidates.length }} 条）</el-button>
      </div>
      <el-table v-if="candidates.length" :data="candidates" max-height="240" border>
        <el-table-column prop="patientName" label="患者" min-width="100" />
        <el-table-column prop="orderNo" label="医嘱号" min-width="150" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.orderType) === 1 ? 'warning' : 'info'" size="small">
              {{ Number(row.orderType) === 1 ? '长期' : '临时' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="drugName" label="药品" min-width="160" />
        <el-table-column prop="spec" label="规格" width="110" />
        <el-table-column prop="route" label="途径" width="90" />
        <el-table-column prop="frequency" label="频次" width="80" />
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(Number(row.quantity || 0) * Number(row.price || 0)) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 静配单列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <h3 class="mb-3 font-semibold text-slate-800">静配台账</h3>
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="病区">
          <el-select v-model="query.wardId" placeholder="病区" clearable class="w-44" :fit-input-width="false">
            <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调配日期">
          <el-date-picker v-model="query.admixDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="调配日期" class="w-44" />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="query.patientName" placeholder="患者姓名" clearable class="w-40" data-testid="pivas-patient-search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="状态" clearable class="w-32" :fit-input-width="false">
            <el-option v-for="d in batchStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" @click="() => { query.pageNum = 1; loadList(); loadStats() }">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="pivas-table">
        <el-table-column prop="pivasNo" label="静配单号" min-width="150" />
        <el-table-column prop="admixDate" label="调配日期" width="110" />
        <el-table-column prop="patientName" label="患者" min-width="110">
          <template #default="{ row }">
            <span>{{ row.patientName || '—' }}</span>
            <span class="text-xs text-slate-400 ml-1">{{ row.patientNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="wardName" label="病区" min-width="130">
          <template #default="{ row }">{{ row.wardName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="pivasBatchStatusTag(row.status)" size="small">{{ batchStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="明细数" width="80" align="right" />
        <el-table-column prop="generateBy" label="生成人" width="100">
          <template #default="{ row }">{{ row.generateBy || '—' }}</template>
        </el-table-column>
        <el-table-column prop="labelBy" label="排队操作" width="120">
          <template #default="{ row }">{{ row.labelBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row.id)">明细</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">当前筛选条件下没有静配单</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 明细弹窗（审方→排队→调配→核对 工作区） -->
    <el-dialog v-model="detailVisible" :title="`静配单 ${detail?.pivasNo || ''}`" width="1080px" data-testid="pivas-detail-dialog">
      <div v-if="detail" v-loading="detailLoading" class="space-y-3">
        <div class="flex items-center gap-6 text-sm text-slate-600 flex-wrap">
          <span>调配日期：<b class="text-slate-800">{{ detail.admixDate }}</b></span>
          <span>患者：<b class="text-slate-800">{{ detail.patientName || '—' }}</b>（{{ detail.patientNo || '—' }}）</span>
          <span>病区：<b class="text-slate-800">{{ detail.wardName || '—' }}</b></span>
          <span>
            状态：<el-tag :type="pivasBatchStatusTag(detail.status)" size="small">{{ batchStatusText(detail.status) }}</el-tag>
          </span>
          <span>生成人：{{ detail.generateBy || '—' }}</span>
          <el-button v-if="canLabel(detail.status)" v-perm="'pharmacy:pivas:edit'" type="primary" :icon="Tickets" size="small"
                     :loading="actLoading" data-testid="pivas-label-btn" @click="onLabelBatch">打标签排队</el-button>
        </div>
        <el-table :data="detail.items || []" border size="small" max-height="420" data-testid="pivas-item-table">
          <el-table-column label="排队号" width="72" align="center">
            <template #default="{ row }">
              <span v-if="row.queueNo" class="font-semibold text-sky-700">{{ row.queueNo }}</span>
              <span v-else class="text-slate-300">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="drugName" label="药品" min-width="140" />
          <el-table-column prop="spec" label="规格" width="100" />
          <el-table-column label="数量" width="90" align="right">
            <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
          </el-table-column>
          <el-table-column prop="route" label="途径" width="80" />
          <el-table-column prop="frequency" label="频次" width="70" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="pivasItemStatusTag(row.status)" size="small">{{ itemStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditorName" label="审方人" width="90">
            <template #default="{ row }">{{ row.auditorName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="compounderName" label="调配人" width="90">
            <template #default="{ row }">{{ row.compounderName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="verifierName" label="核对人" width="90">
            <template #default="{ row }">{{ row.verifierName || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="210" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canAudit(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="CircleCheck" type="success" size="small" plain
                         :loading="actLoading" data-testid="pivas-audit-pass-btn" @click="onAuditPass(row)">审方通过</el-button>
              <el-button v-if="canAudit(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="CircleClose" type="danger" size="small" plain
                         :loading="actLoading" data-testid="pivas-audit-reject-btn" @click="onAuditReject(row)">退回</el-button>
              <el-button v-if="canCompound(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="Van" type="primary" size="small" plain
                         :loading="actLoading" data-testid="pivas-compound-btn" @click="onCompound(row)">调配</el-button>
              <el-button v-if="canVerify(row.status)" v-perm="'pharmacy:pivas:edit'" :icon="Box" type="success" size="small" plain
                         :loading="actLoading" data-testid="pivas-verify-btn" @click="onVerify(row)">核对发放</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-4 text-sm text-slate-400">本单暂无明细</div>
          </template>
        </el-table>
        <div v-if="(detail.items || []).some((i: any) => i.rejectReason)" class="text-xs text-slate-500">
          <template v-for="i in detail.items.filter((x: any) => x.rejectReason)" :key="i.id">
            <div>审方退回留痕：{{ i.drugName }} × {{ i.quantity }} —— {{ i.rejectReason }}（{{ i.auditorName }}）</div>
          </template>
        </div>
        <div class="text-xs text-slate-400">
          说明：排队/打标签为流程节点留痕（标签打印预留，不驱动打印机）；费用结算仍走住院摆药链，本链不重复计费。
        </div>
      </div>
    </el-dialog>
  </div>
</template>
