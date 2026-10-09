<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Van, CircleCheck, RefreshLeft } from '@element-plus/icons-vue'
import {
  listCandidates,
  generateWardDispense,
  listWardDispensePage,
  getWardDispenseDetail,
  dispenseItem,
  checkItem,
  returnItem,
  getWardDispenseStats,
} from '@/api/wardDispense'
import { getInpatientWardList } from '@/api/inpatient'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, formatMoney } from '@/lib/utils'
import { itemStatusTag, orderStatusTag, canDispense, canCheck, canReturn } from '@/lib/wardDispense'
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
  dispenseDate: '' as string,
  patientName: '',
  status: null as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const stats = reactive({ pending: 0, dispensed: 0, checked: 0, returned: 0, dispenseCount: 0 })

const wards = ref<any[]>([])
const orderStatusDict = ref<any[]>([])
const itemStatusDict = ref<any[]>([])
const orderStatusText = (v: any) => dictLabelText(orderStatusDict.value, v)
const itemStatusText = (v: any) => dictLabelText(itemStatusDict.value, v)

// ---------------- 生成区 ----------------
const gen = reactive({ wardId: null as number | null, dispenseDate: today() })
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
    const res: any = await listCandidates({
      wardId: gen.wardId,
      dispenseDate: gen.dispenseDate || undefined,
    })
    if (res.code === 200) {
      candidates.value = res.data || []
      if (!candidates.value.length) ElMessage.info('该病区当天没有可摆药的医嘱')
    } else {
      ElMessage.error(res.message || '查询可摆医嘱失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('查询可摆医嘱失败')
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
    const res: any = await generateWardDispense({
      wardId: gen.wardId,
      dispenseDate: gen.dispenseDate || undefined,
    })
    if (res.code === 200) {
      const items = res.data?.items || []
      const pendingCount = items.filter((i: any) => Number(i.status) === 1).length
      ElMessage.success(`摆药单 ${res.data?.dispenseNo} 已就绪（${items.length} 条明细，其中待配药 ${pendingCount} 条）${res.data?.remark ? '；' + res.data.remark : ''}`)
      unmatchedNote.value = res.data?.remark || ''
      candidates.value = []
      await Promise.all([loadList(), loadStats()])
      openDetail(res.data?.id)
    } else {
      ElMessage.error(res.message || '生成摆药单失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('生成摆药单失败')
  } finally {
    generating.value = false
  }
}

// ---------------- 列表 ----------------
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.WARD_DISPENSE_STATUS},${DICT_TYPE.WARD_DISPENSE_ITEM_STATUS}`
    )
    orderStatusDict.value = res?.data?.[DICT_TYPE.WARD_DISPENSE_STATUS] || []
    itemStatusDict.value = res?.data?.[DICT_TYPE.WARD_DISPENSE_ITEM_STATUS] || []
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
    const res: any = await getWardDispenseStats({
      dispenseDate: query.dispenseDate || undefined,
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
    const res: any = await listWardDispensePage({
      wardId: query.wardId ?? undefined,
      dispenseDate: query.dispenseDate || undefined,
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
  query.dispenseDate = ''
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
  { label: '待配药', value: stats.pending, tone: 'text-amber-600' },
  { label: '已配药待核对', value: stats.dispensed, tone: 'text-sky-700' },
  { label: '已核对', value: stats.checked, tone: 'text-emerald-700' },
  { label: '已退药', value: stats.returned, tone: 'text-slate-500' },
  { label: '摆药单数', value: stats.dispenseCount, tone: 'text-[#1269B5]' },
])

const openDetail = async (id: any) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res: any = await getWardDispenseDetail(id)
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

const onDispense = async (item: any) => {
  actLoading.value = true
  try {
    const res: any = await dispenseItem({ itemId: item.id })
    if (res.code === 200) {
      ElMessage.success(`明细已配药（${item.drugName} × ${item.quantity}），库存已扣减并计费`)
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '配药失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('配药失败')
  } finally {
    actLoading.value = false
  }
}

const onCheck = async (item: any) => {
  actLoading.value = true
  try {
    const res: any = await checkItem({ itemId: item.id })
    if (res.code === 200) {
      ElMessage.success('核对通过')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '核对失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('核对失败')
  } finally {
    actLoading.value = false
  }
}

const onReturn = async (item: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认为「${item.drugName} × ${item.quantity}」办理退药？回库后金额将负冲账，操作不可逆。`,
      '退药原因（必填）',
      { confirmButtonText: '确认退药', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '退药原因必填' },
    )
    actLoading.value = true
    const res: any = await returnItem({ itemId: item.id, reason: value.trim() })
    if (res.code === 200) {
      ElMessage.success('退药完成（已回库并负冲账）')
      await refreshDetail()
    } else {
      ElMessage.error(res.message || '退药失败')
    }
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') console.error(e)
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
    <div class="mb-3 grid grid-cols-5 gap-3">
      <div v-for="c in statCards" :key="c.label" class="bg-white rounded-lg border border-slate-200 px-4 py-3">
        <div class="text-xs text-slate-500">{{ c.label }}</div>
        <div class="text-2xl font-semibold mt-1" :class="c.tone">{{ c.value }}</div>
      </div>
    </div>

    <!-- 生成摆药单 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white p-4 space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="font-semibold text-slate-800">生成摆药单</h3>
        <span v-if="unmatchedNote" class="text-xs text-amber-600">{{ unmatchedNote }}</span>
      </div>
      <div class="flex items-center gap-3 flex-wrap">
        <el-select v-model="gen.wardId" placeholder="选择病区" class="w-64" :fit-input-width="false" data-testid="wd-ward-select">
          <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
        </el-select>
        <el-date-picker v-model="gen.dispenseDate" type="date" value-format="YYYY-MM-DD"
                        placeholder="摆药日期" class="w-44" :clearable="false" />
        <el-button :icon="Search" :loading="candidatesLoading" @click="loadCandidates">查询可摆医嘱</el-button>
        <el-button v-perm="'pharmacy:wardDispense:add'" type="primary" :icon="Plus" :loading="generating" :disabled="!candidates.length" data-testid="wd-generate-btn"
                   @click="doGenerate">生成摆药单（{{ candidates.length }} 条）</el-button>
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
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(Number(row.quantity || 0) * Number(row.price || 0)) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 摆药单列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <h3 class="mb-3 font-semibold text-slate-800">摆药单列表</h3>
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="病区">
          <el-select v-model="query.wardId" placeholder="病区" clearable class="w-44" :fit-input-width="false">
            <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
          </el-select>
        </el-form-item>
        <el-form-item label="摆药日期">
          <el-date-picker v-model="query.dispenseDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="摆药日期" class="w-44" />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="query.patientName" placeholder="患者姓名" clearable class="w-40" data-testid="wd-patient-search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="状态" clearable class="w-32" :fit-input-width="false">
            <el-option v-for="d in orderStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" @click="() => { query.pageNum = 1; loadList(); loadStats() }">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="wd-table">
        <el-table-column prop="dispenseNo" label="摆药单号" min-width="150" />
        <el-table-column prop="dispenseDate" label="摆药日期" width="110" />
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
            <el-tag :type="orderStatusTag(row.status)" size="small">{{ orderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generateBy" label="生成人" width="100">
          <template #default="{ row }">{{ row.generateBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row.id)">明细</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">当前筛选条件下没有摆药单</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 明细弹窗 -->
    <el-dialog v-model="detailVisible" :title="`摆药单 ${detail?.dispenseNo || ''}`" width="920px" data-testid="wd-detail-dialog">
      <div v-if="detail" v-loading="detailLoading" class="space-y-3">
        <div class="flex gap-6 text-sm text-slate-600 flex-wrap">
          <span>摆药日期：<b class="text-slate-800">{{ detail.dispenseDate }}</b></span>
          <span>患者：<b class="text-slate-800">{{ detail.patientName || '—' }}</b>（{{ detail.patientNo || '—' }}）</span>
          <span>病区：<b class="text-slate-800">{{ detail.wardName || '—' }}</b></span>
          <span>
            状态：<el-tag :type="orderStatusTag(detail.status)" size="small">{{ orderStatusText(detail.status) }}</el-tag>
          </span>
          <span>生成人：{{ detail.generateBy || '—' }}</span>
        </div>
        <el-table :data="detail.items || []" border size="small" data-testid="wd-item-table">
          <el-table-column prop="drugName" label="药品" min-width="150" />
          <el-table-column prop="spec" label="规格" width="100" />
          <el-table-column label="数量" width="90" align="right">
            <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
          </el-table-column>
          <el-table-column label="金额" width="90" align="right">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="itemStatusTag(row.status)" size="small">{{ itemStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="库存(配前→配后)" width="140" align="center">
            <template #default="{ row }">
              <span v-if="row.stockBefore != null" class="text-xs">{{ row.stockBefore }} → {{ row.stockAfter }}</span>
              <span v-else class="text-slate-300">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="dispenserName" label="配药人" width="90">
            <template #default="{ row }">{{ row.dispenserName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="checkerName" label="核对人" width="90">
            <template #default="{ row }">{{ row.checkerName || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canDispense(row.status)" v-perm="'pharmacy:wardDispense:edit'" :icon="Van" type="primary" size="small" plain
                         :loading="actLoading" data-testid="wd-dispense-btn" @click="onDispense(row)">配药</el-button>
              <el-button v-if="canCheck(row.status)" v-perm="'pharmacy:wardDispense:edit'" :icon="CircleCheck" type="success" size="small" plain
                         :loading="actLoading" data-testid="wd-check-btn" @click="onCheck(row)">核对</el-button>
              <el-button v-if="canReturn(row.status)" v-perm="'pharmacy:wardDispense:edit'" :icon="RefreshLeft" type="danger" size="small" plain
                         :loading="actLoading" data-testid="wd-return-btn" @click="onReturn(row)">退药</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-4 text-sm text-slate-400">本单暂无明细</div>
          </template>
        </el-table>
        <div v-if="(detail.items || []).some((i: any) => i.returnReason)" class="text-xs text-slate-500">
          <template v-for="i in detail.items.filter((x: any) => x.returnReason)" :key="i.id">
            <div>退药留痕：{{ i.drugName }} × {{ i.quantity }} —— {{ i.returnReason }}（{{ i.returnBy }}）</div>
          </template>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
