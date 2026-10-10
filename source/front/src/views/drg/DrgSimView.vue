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
        <el-button :icon="VideoPlay" :loading="batchLoading" class="ml-auto" data-testid="drg-batch-btn" plain
                   size="small" type="primary" @click="runBatch">批量模拟
        </el-button>
      </div>
      <el-table :data="summaries" border data-testid="drg-summary-table" max-height="320" stripe>
        <el-table-column label="首页ID" prop="summaryId" width="90"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="110"/>
        <el-table-column label="主诊断" min-width="180">
          <template #default="{ row }">
            <span v-if="row.mainDiagCode">{{ row.mainDiagCode }} {{ row.mainDiagName || '' }}</span>
            <el-tag v-else size="small" type="warning">编码缺失，需补录</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="手术" prop="isSurgery" width="70">
          <template #default="{ row }">{{ row.isSurgery === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="已模拟入组" prop="simDrgCode" width="110">
          <template #default="{ row }">
            <span v-if="row.simDrgCode" :class="profitColor(row.simProfit)">{{ row.simDrgCode }}</span>
            <span v-else class="text-gray-400">未模拟</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="openSim(row)">模拟</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 结果列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="患者 / DRG 组" style="width: 180px"
                    @keyup.enter="query.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item label="入组状态">
          <el-select v-model="query.simStatus" :fit-input-width="false" clearable placeholder="入组状态"
                     style="width: 130px">
            <el-option v-for="d in simDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="drg-result-table" stripe>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="主诊断" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.mainDiagCode }} {{ row.mainDiagName || '' }}</template>
        </el-table-column>
        <el-table-column align="center" label="手术" prop="isSurgery" width="60">
          <template #default="{ row }">{{ row.isSurgery === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column align="center" label="天数" prop="inpatientDays" width="60"/>
        <el-table-column label="DRG 组" prop="drgCode" width="90">
          <template #default="{ row }">
            <el-tag :type="simTag(row.simStatus)" size="small">{{ row.drgCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="right" label="权重" prop="weight" width="70">
          <template #default="{ row }">{{ row.weight ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="支付标准" prop="payStandard" width="95">
          <template #default="{ row }">{{ row.payStandard ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="实际费用" prop="actualAmount" width="95"/>
        <el-table-column align="right" label="盈亏" width="95">
          <template #default="{ row }">
            <span v-if="row.profitAmount != null" :class="profitColor(row.profitAmount)">
              {{ Number(row.profitAmount) >= 0 ? '+' : '' }}{{ Number(row.profitAmount).toLocaleString() }}</span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="命中规则" min-width="200" prop="ruleNote" show-overflow-tooltip/>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 组表 -->
    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <p class="text-sm font-medium mb-3">DRG 细分组目录（CHS-DRG 3.0 官方方案；权重与支付标准由统筹区医保局下发，未下发处显示 —）</p>
      <el-table :data="groups" border data-testid="drg-group-table" max-height="280" stripe>
        <el-table-column label="组编码" prop="drgCode" width="90"/>
        <el-table-column label="组名称" min-width="240" prop="drgName" show-overflow-tooltip/>
        <el-table-column align="center" label="MDC" prop="mdcCode" width="70"/>
        <el-table-column align="center" label="ADRG" prop="adrgCode" width="80"/>
        <el-table-column align="right" label="权重" prop="weight" width="80">
          <template #default="{ row }">{{ row.weight ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="支付标准(元)" prop="payStandard" width="110">
          <template #default="{ row }">{{ row.payStandard ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="160" prop="source" show-overflow-tooltip/>
      </el-table>
    </div>

    <!-- 模拟弹窗 -->
    <el-dialog v-model="simVisible" :title="`DRG 模拟 - ${simForm.patientName}`" width="560px">
      <el-form label-width="110px">
        <el-form-item label="首页主诊断">
          <span>{{ simForm.mainDiagCode || '（缺失）' }} {{ simForm.mainDiagName }}</span>
        </el-form-item>
        <el-form-item v-if="!simForm.mainDiagCode" label="补录 ICD 编码" required>
          <el-input v-model="simForm.icdCode" data-testid="drg-icd-input" placeholder="如 J18.9"/>
        </el-form-item>
        <el-form-item v-if="!simForm.mainDiagCode" label="补录诊断名称">
          <el-input v-model="simForm.icdName" placeholder="如 肺炎"/>
        </el-form-item>
        <div v-if="simResult" class="mb-3 p-3 rounded bg-gray-50 text-sm" data-testid="drg-sim-result">
          <p>入组：<b>{{ simResult.drgCode }}</b> {{ simResult.drgName || '' }}
            <el-tag :type="simTag(simResult.simStatus)" class="ml-1" size="small">{{
                simText(simResult.simStatus)
              }}
            </el-tag>
          </p>
          <p v-if="simResult.simStatus === 1" class="mt-1">
            权重 {{ simResult.weight ?? '—' }}｜
            支付标准 {{ simResult.payStandard == null ? '未下发' : '¥' + simResult.payStandard }}｜实际费用
            ¥{{ simResult.actualAmount }}
            ｜<span v-if="simResult.profitAmount != null" :class="profitColor(simResult.profitAmount)">
              {{ Number(simResult.profitAmount) >= 0 ? '结余 +' : '超支 ' }}{{ simResult.profitAmount }}</span>
            <span v-else class="text-gray-400">盈亏待统筹区下发支付标准</span></p>
          <p class="mt-1 text-gray-500">{{ simResult.ruleNote }}</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="simVisible = false">关闭</el-button>
        <el-button data-testid="drg-sim-run" type="primary" @click="runSim">运行模拟</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * DRG-DIP 分组模拟（G23，菜单 907，挂报表统计；目录 1200 于 sql/188 由「报表与审计」更名）
 *
 * 院内简化模拟器：主诊断 ICD 类目 + 是否手术 → 组表 sys_drg_group（CHS-DRG 1.1 模拟种子）。
 * 首页主诊断编码为空时在弹窗里补录 ICD（走 sys_icd10 检索接口），再跑模拟；每首页一条结果重跑覆盖。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search, VideoPlay} from '@element-plus/icons-vue';
import {drgGroupList, drgResultListPage, drgSimulate, drgSimulateBatch, drgSummaryList} from '@/api/drg';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const simDict = ref([]);
const simText = (v) => dictLabelText(simDict.value, v);
const simTag = (v) => ({1: 'success', 2: 'info'}[v] || 'info');
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.DRG_SIM_STATUS}`);
    simDict.value = res?.data?.[DICT_TYPE.DRG_SIM_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 汇总 + 可模拟首页 ----------------
const summaries = ref([]);
const stat = ref(null);
const loadSummary = async () => {
  try {
    const res = await drgSummaryList(50);
    summaries.value = res?.data?.rows || [];
    stat.value = res?.data?.stat || null;
  } catch (e) {
    console.error('加载首页列表失败', e);
  }
};
// ---------------- 模拟弹窗（补 ICD）----------------
const simVisible = ref(false);
const simForm = reactive({
  summaryId: null,
  patientName: '',
  mainDiagCode: '',
  mainDiagName: '',
  icdCode: '',
  icdName: ''
});
const openSim = (row) => {
  simForm.summaryId = row.summaryId;
  simForm.patientName = row.patientName;
  simForm.mainDiagCode = row.mainDiagCode || '';
  simForm.mainDiagName = row.mainDiagName || '';
  simForm.icdCode = '';
  simForm.icdName = '';
  simVisible.value = true;
};
const runSim = async () => {
  const code = (simForm.mainDiagCode || simForm.icdCode || '').trim();
  if (!code)
    return ElMessage.warning('请补录主诊断 ICD 编码后再模拟');
  try {
    const res = await drgSimulate({
      summaryId: simForm.summaryId,
      icdCode: code,
      icdName: (simForm.mainDiagName || simForm.icdName || '').trim() || undefined,
    });
    const d = res?.data;
    simResult.value = d;
    ElMessage.success(d?.simStatus === 1 ? `已入组 ${d.drgCode}` : '未入组（QY）');
    loadSummary();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '模拟失败');
  }
};
const simResult = ref(null);
// ---------------- 批量 ----------------
const batchLoading = ref(false);
const runBatch = async () => {
  batchLoading.value = true;
  try {
    await drgSimulateBatch({});
    ElMessage.success('批量模拟完成（主诊断编码为空的行已跳过）');
    loadSummary();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '批量模拟失败');
  } finally {
    batchLoading.value = false;
  }
};
// ---------------- 结果列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', simStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await drgResultListPage({
      keyword: query.keyword.trim() || undefined,
      simStatus: query.simStatus ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    rows.value = res?.data?.records || [];
    total.value = Number(res?.data?.total || 0);
  } catch (e) {
    console.error('加载结果失败', e);
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  query.keyword = '';
  query.simStatus = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 组表 ----------------
const groups = ref([]);
const loadGroups = async () => {
  try {
    const res = await drgGroupList();
    groups.value = res?.data || [];
  } catch (e) {
    console.error('加载组表失败', e);
  }
};
const profitColor = (v) => {
  const n = Number(v || 0);
  return n >= 0 ? 'text-green-600' : 'text-red-600';
};
onMounted(() => {
  loadDicts();
  loadSummary();
  loadList();
  loadGroups();
});
</script>
