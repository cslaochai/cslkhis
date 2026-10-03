<script setup lang="ts">
/**
 * DRG-DIP 分组模拟（G23，菜单 907，挂报表统计；目录 1200 于 sql/188 由「报表与审计」更名）
 *
 * 院内简化模拟器：主诊断 ICD 类目 + 是否手术 → 组表 sys_drg_group（CHS-DRG 1.1 模拟种子）。
 * 首页主诊断编码为空时在弹窗里补录 ICD（走 sys_icd10 检索接口），再跑模拟；每首页一条结果重跑覆盖。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, VideoPlay } from '@element-plus/icons-vue'
import { drgSimulate, drgSimulateBatch, drgResultListPage, drgSummaryList, drgGroupList } from '@/api/drg'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const simDict = ref<any[]>([])
const simText = (v: any) => dictLabelText(simDict.value, v)
const simTag = (v: number) => ({ 1: 'success', 2: 'info' } as any)[v] || 'info'
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.DRG_SIM_STATUS}`)
    simDict.value = res?.data?.[DICT_TYPE.DRG_SIM_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 汇总 + 可模拟首页 ----------------
const summaries = ref<any[]>([])
const stat = ref<any>(null)
const loadSummary = async () => {
  try {
    const res: any = await drgSummaryList(50)
    summaries.value = res?.data?.rows || []
    stat.value = res?.data?.stat || null
  } catch (e) { console.error('加载首页列表失败', e) }
}

// ---------------- 模拟弹窗（补 ICD）----------------
const simVisible = ref(false)
const simForm = reactive({ summaryId: null as any, patientName: '', mainDiagCode: '', mainDiagName: '', icdCode: '', icdName: '' })
const openSim = (row: any) => {
  simForm.summaryId = row.summaryId
  simForm.patientName = row.patientName
  simForm.mainDiagCode = row.mainDiagCode || ''
  simForm.mainDiagName = row.mainDiagName || ''
  simForm.icdCode = ''; simForm.icdName = ''
  simVisible.value = true
}
const runSim = async () => {
  const code = (simForm.mainDiagCode || simForm.icdCode || '').trim()
  if (!code) return ElMessage.warning('请补录主诊断 ICD 编码后再模拟')
  try {
    const res: any = await drgSimulate({
      summaryId: simForm.summaryId,
      icdCode: code,
      icdName: (simForm.mainDiagName || simForm.icdName || '').trim() || undefined,
    })
    const d = res?.data
    simResult.value = d
    ElMessage.success(d?.simStatus === 1 ? `已入组 ${d.drgCode}` : '未入组（QY）')
    loadSummary()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '模拟失败') }
}
const simResult = ref<any>(null)

// ---------------- 批量 ----------------
const batchLoading = ref(false)
const runBatch = async () => {
  batchLoading.value = true
  try {
    await drgSimulateBatch({})
    ElMessage.success('批量模拟完成（主诊断编码为空的行已跳过）')
    loadSummary()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '批量模拟失败') } finally { batchLoading.value = false }
}

// ---------------- 结果列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', simStatus: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadList = async () => {
  loading.value = true
  try {
    const res: any = await drgResultListPage({
      keyword: query.keyword.trim() || undefined,
      simStatus: query.simStatus ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (e) { console.error('加载结果失败', e) } finally { loading.value = false }
}
const reset = () => { query.keyword = ''; query.simStatus = null; query.pageNum = 1; loadList() }

// ---------------- 组表 ----------------
const groups = ref<any[]>([])
const loadGroups = async () => {
  try {
    const res: any = await drgGroupList()
    groups.value = res?.data || []
  } catch (e) { console.error('加载组表失败', e) }
}
const profitColor = (v: any) => {
  const n = Number(v || 0)
  return n >= 0 ? 'text-green-600' : 'text-red-600'
}

onMounted(() => { loadDicts(); loadSummary(); loadList(); loadGroups() })
</script>

<template>
  <div>
    <!-- 汇总卡 -->
    <div class="grid grid-cols-4 gap-3 mb-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="drg-stat-total">
        <p class="text-xs text-gray-500">已模拟病案</p>
        <p class="text-2xl font-semibold mt-1 text-blue-600">{{ stat?.total ?? '—' }}</p>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="drg-stat-grouped">
        <p class="text-xs text-gray-500">已入组 / 未入组</p>
        <p class="text-2xl font-semibold mt-1 text-green-600">
          {{ stat?.grouped ?? '—' }} <span class="text-sm text-gray-400">/</span>
          <span class="text-lg text-gray-500">{{ stat?.ungrouped ?? '—' }}</span></p>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="drg-stat-profit">
        <p class="text-xs text-gray-500">入组结余合计</p>
        <p class="text-2xl font-semibold mt-1 text-green-600">¥{{ Number(stat?.totalProfit || 0).toLocaleString() }}</p>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="drg-stat-overrun">
        <p class="text-xs text-gray-500">超支合计</p>
        <p class="text-2xl font-semibold mt-1 text-red-600">¥{{ Number(stat?.totalOverrun || 0).toLocaleString() }}</p>
      </div>
    </div>

    <!-- 可模拟首页 -->
    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm mb-3">
      <div class="flex items-center gap-2 mb-3">
        <p class="text-sm font-medium">病案首页（最近 50 条）</p>
        <el-button type="primary" plain size="small" class="ml-auto" :icon="VideoPlay"
                   :loading="batchLoading" data-testid="drg-batch-btn" @click="runBatch">批量模拟</el-button>
      </div>
      <el-table :data="summaries" border stripe max-height="320" data-testid="drg-summary-table">
        <el-table-column prop="summaryId" label="首页ID" width="90" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="deptName" label="科室" width="110" show-overflow-tooltip />
        <el-table-column label="主诊断" min-width="180">
          <template #default="{ row }">
            <span v-if="row.mainDiagCode">{{ row.mainDiagCode }} {{ row.mainDiagName || '' }}</span>
            <el-tag v-else type="warning" size="small">编码缺失，需补录</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isSurgery" label="手术" width="70" align="center">
          <template #default="{ row }">{{ row.isSurgery === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="simDrgCode" label="已模拟入组" width="110">
          <template #default="{ row }">
            <span v-if="row.simDrgCode" :class="profitColor(row.simProfit)">{{ row.simDrgCode }}</span>
            <span v-else class="text-gray-400">未模拟</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openSim(row)">模拟</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 结果列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="患者 / DRG 组" clearable style="width: 180px"
                    @keyup.enter="query.pageNum = 1; loadList()" />
        </el-form-item>
        <el-form-item label="入组状态">
          <el-select v-model="query.simStatus" placeholder="入组状态" clearable style="width: 130px"
                     :fit-input-width="false">
            <el-option v-for="d in simDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="drg-result-table">
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column label="主诊断" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.mainDiagCode }} {{ row.mainDiagName || '' }}</template>
        </el-table-column>
        <el-table-column prop="isSurgery" label="手术" width="60" align="center">
          <template #default="{ row }">{{ row.isSurgery === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="inpatientDays" label="天数" width="60" align="center" />
        <el-table-column prop="drgCode" label="DRG 组" width="90">
          <template #default="{ row }">
            <el-tag :type="simTag(row.simStatus)" size="small">{{ row.drgCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="70" align="right">
          <template #default="{ row }">{{ row.weight ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="payStandard" label="支付标准" width="95" align="right">
          <template #default="{ row }">{{ row.payStandard ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实际费用" width="95" align="right" />
        <el-table-column label="盈亏" width="95" align="right">
          <template #default="{ row }">
            <span v-if="row.profitAmount != null" :class="profitColor(row.profitAmount)">
              {{ Number(row.profitAmount) >= 0 ? '+' : '' }}{{ Number(row.profitAmount).toLocaleString() }}</span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="ruleNote" label="命中规则" min-width="200" show-overflow-tooltip />
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 组表 -->
    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <p class="text-sm font-medium mb-3">组表 sys_drg_group（CHS-DRG 1.1 模拟种子，正式方案接入后重导）</p>
      <el-table :data="groups" border stripe max-height="280" data-testid="drg-group-table">
        <el-table-column prop="drgCode" label="组编码" width="90" />
        <el-table-column prop="drgName" label="组名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="mdcCode" label="MDC" width="70" align="center" />
        <el-table-column prop="adrgCode" label="ADRG" width="80" align="center" />
        <el-table-column prop="weight" label="权重" width="80" align="right" />
        <el-table-column prop="payStandard" label="支付标准(元)" width="110" align="right" />
        <el-table-column prop="source" label="来源" min-width="160" show-overflow-tooltip />
      </el-table>
    </div>

    <!-- 模拟弹窗 -->
    <el-dialog v-model="simVisible" :title="`DRG 模拟 - ${simForm.patientName}`" width="560px">
      <el-form label-width="110px">
        <el-form-item label="首页主诊断">
          <span>{{ simForm.mainDiagCode || '（缺失）' }} {{ simForm.mainDiagName }}</span>
        </el-form-item>
        <el-form-item v-if="!simForm.mainDiagCode" label="补录 ICD 编码" required>
          <el-input v-model="simForm.icdCode" placeholder="如 J18.9" data-testid="drg-icd-input" />
        </el-form-item>
        <el-form-item v-if="!simForm.mainDiagCode" label="补录诊断名称">
          <el-input v-model="simForm.icdName" placeholder="如 肺炎" />
        </el-form-item>
        <div v-if="simResult" class="mb-3 p-3 rounded bg-gray-50 text-sm" data-testid="drg-sim-result">
          <p>入组：<b>{{ simResult.drgCode }}</b> {{ simResult.drgName || '' }}
            <el-tag :type="simTag(simResult.simStatus)" size="small" class="ml-1">{{ simText(simResult.simStatus) }}</el-tag></p>
          <p class="mt-1" v-if="simResult.simStatus === 1">
            权重 {{ simResult.weight }}｜支付标准 ¥{{ simResult.payStandard }}｜实际费用 ¥{{ simResult.actualAmount }}
            ｜<span :class="profitColor(simResult.profitAmount)">
              {{ Number(simResult.profitAmount) >= 0 ? '结余 +' : '超支 ' }}{{ simResult.profitAmount }}</span></p>
          <p class="mt-1 text-gray-500">{{ simResult.ruleNote }}</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="simVisible = false">关闭</el-button>
        <el-button type="primary" data-testid="drg-sim-run" @click="runSim">运行模拟</el-button>
      </template>
    </el-dialog>
  </div>
</template>
