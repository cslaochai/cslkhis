<template>
  <div>
    <!-- 版本切换：DIP 病种库按版本装载，2.0 官方数据未接入时模拟判为未入组 -->
    <div class="flex items-center gap-2 mb-3">
      <span class="text-sm text-gray-500">病种库版本</span>
      <el-select v-model="version" clearable placeholder="版本" style="width: 150px" @change="onVersionChange">
        <el-option label="2.0" value="2.0"/>
        <el-option label="3.0" value="3.0"/>
      </el-select>
      <el-tag v-if="!version || version === '2.0'" type="warning" size="small">2.0 官方数据尚未接入，模拟将判为未入组（QY）</el-tag>
    </div>

    <!-- 汇总卡 -->
    <div class="grid grid-cols-4 gap-3 mb-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="dip-stat-total">
        <p class="text-xs text-gray-500">已模拟病案</p>
        <p class="text-2xl font-semibold mt-1 text-blue-600">{{ stat?.total ?? '—' }}</p>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="dip-stat-grouped">
        <p class="text-xs text-gray-500">已入组 / 未入组</p>
        <p class="text-2xl font-semibold mt-1 text-green-600">
          {{ stat?.grouped ?? '—' }} <span class="text-sm text-gray-400">/</span>
          <span class="text-lg text-gray-500">{{ stat?.ungrouped ?? '—' }}</span></p>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="dip-stat-profit">
        <p class="text-xs text-gray-500">入组结余合计</p>
        <p class="text-2xl font-semibold mt-1 text-green-600">¥{{ Number(stat?.totalProfit || 0).toLocaleString() }}</p>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="dip-stat-overrun">
        <p class="text-xs text-gray-500">超支合计</p>
        <p class="text-2xl font-semibold mt-1 text-red-600">¥{{ Number(stat?.totalOverrun || 0).toLocaleString() }}</p>
      </div>
    </div>

    <!-- 可模拟首页 -->
    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm mb-3">
      <div class="flex items-center gap-2 mb-3">
        <p class="text-sm font-medium">病案首页（最近 50 条）</p>
        <el-button :icon="VideoPlay" :loading="batchLoading" class="ml-auto" data-testid="dip-batch-btn" plain
                   size="small" type="primary" @click="runBatch">批量模拟
        </el-button>
      </div>
      <el-table :data="summaries" border data-testid="dip-summary-table" max-height="320" stripe>
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
        <el-table-column label="已模拟入组" prop="simDipCode" width="110">
          <template #default="{ row }">
            <span v-if="row.simDipCode" :class="profitColor(row.simProfit)">{{ row.simDipCode }}</span>
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
          <el-input v-model="query.keyword" clearable placeholder="患者 / 病种" style="width: 180px"
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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="dip-result-table" stripe>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="主诊断" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.mainDiagCode }} {{ row.mainDiagName || '' }}</template>
        </el-table-column>
        <el-table-column align="center" label="手术" prop="operCode" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operCode || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="版本" prop="version" width="60"/>
        <el-table-column label="病种" prop="dipCode" width="100">
          <template #default="{ row }">
            <el-tag :type="simTag(row.simStatus)" size="small">{{ row.dipCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="类型" width="80">
          <template #default="{ row }">{{ row.diseaseType === 1 ? '核心' : row.diseaseType === 2 ? '综合' : '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="基准分值" width="85">
          <template #default="{ row }">{{ row.baseScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="系数" width="65">
          <template #default="{ row }">{{ row.coeff ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="分值" width="75">
          <template #default="{ row }">{{ row.score ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="结算金额" width="95">
          <template #default="{ row }">{{ row.settleAmount ?? '—' }}</template>
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

    <!-- 病种目录 -->
    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <p class="text-sm font-medium mb-3">DIP 病种目录（{{ version || '2.0' }} 病种库；基准分值由统筹区医保局下发，未下发处显示 —）</p>
      <el-table :data="diseases" border data-testid="dip-disease-table" max-height="280" stripe>
        <el-table-column label="病种编码" prop="diseaseCode" width="100"/>
        <el-table-column label="病种名称" min-width="220" prop="diseaseName" show-overflow-tooltip/>
        <el-table-column align="center" label="类型" width="80">
          <template #default="{ row }">{{ row.diseaseType === 1 ? '核心' : row.diseaseType === 2 ? '综合' : '—' }}</template>
        </el-table-column>
        <el-table-column label="主诊断" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.mainDiagCode }} {{ row.mainDiagName || '' }}</template>
        </el-table-column>
        <el-table-column label="手术操作" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operCode || '—' }} {{ row.operName || '' }}</template>
        </el-table-column>
        <el-table-column align="center" label="匹配方式" width="80">
          <template #default="{ row }">{{ row.diagMatch === 2 ? '前缀' : '整码' }}</template>
        </el-table-column>
        <el-table-column align="right" label="基准分值" prop="baseScore" width="90">
          <template #default="{ row }">{{ row.baseScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="150" prop="source" show-overflow-tooltip/>
      </el-table>
    </div>

    <!-- 模拟弹窗：拆到 DipSimEdit，页面只管开关与递参 -->
    <DipSimEdit v-model="simVisible" :row="simRow" :version="version" :sim-dict="simDict" @saved="loadSummary"/>
  </div>
</template>

<script setup>
/**
 * DIP 分组模拟（菜单 908，挂报表统计；目录 1200）
 *
 * 院内简化模拟器：主诊断 + 手术操作组合匹配病种目录 biz_dip_disease（核心病种整码、综合病种前缀），
 * 辅助目录 biz_dip_aux 命中后按系数调整分值；分值 × 点值 = 结算金额，减去实际费用得盈亏。
 * 首页主诊断编码为空时在弹窗里补录 ICD（走 sys_icd10 检索接口），再跑模拟；每首页一条结果重跑覆盖。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search, VideoPlay} from '@element-plus/icons-vue';
import {dipDiseaseList, dipResultListPage, dipSimulateBatch, dipSummaryList, getDipDefaultVersion} from '@/api/dip';
import {getDictDataMapList} from '@/api/system';
import DipSimEdit from './DipSimEdit.vue';
import {DICT_TYPE} from '@/lib/dict-cache';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {profitColor, simTag} from '@/lib/drg';

const simDict = ref([]);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.DRG_SIM_STATUS}`);
    simDict.value = res?.data?.[DICT_TYPE.DRG_SIM_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 版本切换 ----------------
const version = ref('2.0');
const onVersionChange = () => {
  loadSummary();
  loadList();
  loadDiseases();
};
// ---------------- 汇总 + 可模拟首页 ----------------
const summaries = ref([]);
const stat = ref(null);
const loadSummary = async () => {
  try {
    const res = await dipSummaryList(50, version.value);
    summaries.value = res?.data?.rows || [];
    stat.value = res?.data?.stat || null;
  } catch (e) {
    console.error('加载首页列表失败', e);
  }
};
// ---------------- 模拟弹窗（补 ICD/手术编码）：表单与模拟逻辑拆到 DipSimEdit，页面只管开关与递参 ----------------
const simVisible = ref(false);
const simRow = ref(null);
const openSim = (row) => {
  simRow.value = row;
  simVisible.value = true;
};
// ---------------- 批量 ----------------
const batchLoading = ref(false);
const runBatch = async () => {
  batchLoading.value = true;
  try {
    await dipSimulateBatch({version: version.value});
    ElMessage.success('批量模拟完成（主诊断编码为空的行已跳过）');
    loadSummary();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
    const res = await dipResultListPage({
      keyword: query.keyword.trim() || undefined,
      simStatus: query.simStatus ?? undefined,
      version: version.value,
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
// ---------------- 病种目录 ----------------
const diseases = ref([]);
const loadDiseases = async () => {
  try {
    const res = await dipDiseaseList(version.value);
    diseases.value = res?.data || [];
  } catch (e) {
    console.error('加载病种目录失败', e);
  }
};
onMounted(async () => {
  // 初始版本跟随系统参数配置（参数配置 → 医保付费 DIP 病种库版本），下拉仍可按需临时切换查询版本
  try {
    const res = await getDipDefaultVersion();
    if (res.code === 200 && res.data?.dbVersion) {
      version.value = res.data.dbVersion;
    }
  } catch (e) {
    console.error('加载 DIP 默认版本失败', e);
  }
  loadDicts();
  loadSummary();
  loadList();
  loadDiseases();
});
</script>
