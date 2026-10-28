<template>
  <div class="space-y-6">
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="t in ITEM_TYPES" :key="t.key" :label="t.label" :name="t.key"/>
        <el-tab-pane :name="POLICY_TAB" label="政策价"/>
        <el-tab-pane :name="HISTORY_TAB" label="调价历史"/>
      </el-tabs>

      <!-- 政策价（诊查费等政策类项目：价格权威在价表，调价即时生效） -->
      <template v-if="activeTab === POLICY_TAB">
        <el-alert
            class="mt-2"
            :closable="false"
            show-icon
            title="政策价是诊查费等政策类项目的价格权威：急诊接诊记账、门诊复诊拆行实时查这里；调价即时生效，价表缺行/停用时急诊不记账、门诊回退兜底价（见后端日志）。"
            type="info"
        />
        <el-table v-loading="policyLoading" :data="policyRows" class="mt-3" style="width: 100%">
          <el-table-column label="政策项目" min-width="180">
            <template #default="{ row }">{{ row.policyName }}</template>
          </el-table-column>
          <el-table-column class-name="font-mono text-xs" label="院内项目" min-width="150">
            <template #default="{ row }">{{ row.itemCode }}（{{ row.itemName }}）</template>
          </el-table-column>
          <el-table-column label="档位" width="110">
            <template #default="{ row }">{{ row.tierLabel }}</template>
          </el-table-column>
          <el-table-column class-name="font-mono text-xs" label="国家编码" min-width="160">
            <template #default="{ row }">{{ row.ybCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="单位" width="70">
            <template #default="{ row }">{{ row.unit || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="全额" width="110">
            <template #default="{ row }">
              <span class="font-semibold text-[#1269B5]">¥{{ formatMoney(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="加收额" width="110">
            <template #default="{ row }">
              <span v-if="row.surcharge !== null && row.surcharge !== undefined">¥{{ formatMoney(row.surcharge) }}</span>
              <span v-else class="text-slate-300">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag
                  :class="row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-500'"
                  class="border"
                  effect="plain"
                  size="small"
              >
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button v-perm="'finance:price:edit'" :icon="Edit" link size="small" type="primary"
                         @click="openPolicyChange(row)">调价
              </el-button>
              <el-button v-perm="'finance:price:edit'" link size="small"
                         :type="row.status === 1 ? 'danger' : 'success'"
                         @click="togglePolicyStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!policyLoading && policyRows.length === 0" class="py-12 text-center text-sm text-slate-400">
          暂无政策价配置（请执行增量 SQL 304 种子数据）
        </div>
      </template>

      <!-- 价表列表 -->
      <template v-else-if="activeTab !== HISTORY_TAB">
        <div class="mt-2 flex flex-wrap items-center gap-3">
          <el-input
              v-model="keyword"
              :placeholder="`${typeMeta().codeLabel} / ${typeMeta().nameLabel}`"
              :prefix-icon="Search"
              class="!w-72"
              clearable
              @keyup.enter="handleSearch"
          />
          <el-select v-model="statusFilter" class="!w-28" clearable placeholder="状态">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="停用"/>
          </el-select>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <span class="ml-auto text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 条
          </span>
        </div>

        <el-table v-loading="loading" :data="rows" class="mt-3" style="width: 100%">
          <el-table-column :label="typeMeta().codeLabel" class-name="font-mono text-xs" min-width="150">
            <template #default="{ row }">{{ row.itemCode || '—' }}</template>
          </el-table-column>
          <el-table-column :label="typeMeta().nameLabel" min-width="200">
            <template #default="{ row }">{{ row.itemName || '—' }}</template>
          </el-table-column>
          <el-table-column label="规格" min-width="120">
            <template #default="{ row }">{{ row.specification || '—' }}</template>
          </el-table-column>
          <el-table-column label="单位" width="80">
            <template #default="{ row }">{{ row.unit || '—' }}</template>
          </el-table-column>
          <el-table-column label="生产厂家" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.manufacturer || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="价格" width="120">
            <template #default="{ row }">
              <span class="font-semibold text-[#1269B5]">¥{{ formatMoney(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag
                  :class="row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-500'"
                  class="border"
                  effect="plain"
                  size="small"
              >
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="100">
            <template #default="{ row }">
              <el-button v-perm="'finance:price:edit'" :icon="Edit" link size="small" type="primary"
                         @click="openChange(row)">调价
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
          没有符合条件的项目
        </div>
        <div class="mt-4 flex justify-end">
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
      </template>

      <!-- 调价历史 -->
      <template v-else>
        <div class="mt-2 flex flex-wrap items-center gap-3">
          <el-input
              v-model="historyKeyword"
              :prefix-icon="Search"
              class="!w-72"
              clearable
              placeholder="项目编码 / 项目名称"
              @keyup.enter="handleHistorySearch"
          />
          <el-button :icon="Search" type="primary" @click="handleHistorySearch">查询</el-button>
          <span class="ml-auto text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ historyLoading ? '—' : historyPagination.total }}</span> 条
          </span>
        </div>
        <el-table v-loading="historyLoading" :data="historyRows" class="mt-3" style="width: 100%">
          <el-table-column label="调价时间" prop="changeTime" width="170">
            <template #default="{ row }">{{ row.changeTime || '—' }}</template>
          </el-table-column>
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              {{ row.itemType === 'POLICY' ? '政策价' : ITEM_TYPES.find(t => t.key === row.itemType)?.label || `未知(${row.itemType})` }}
            </template>
          </el-table-column>
          <el-table-column class-name="font-mono text-xs" label="项目编码" min-width="150" prop="itemCode">
            <template #default="{ row }">{{ row.itemCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="项目名称" min-width="180" prop="itemName">
            <template #default="{ row }">{{ row.itemName || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="原价" width="110">
            <template #default="{ row }">¥{{ formatMoney(row.oldPrice) }}</template>
          </el-table-column>
          <el-table-column align="right" label="新价" width="110">
            <template #default="{ row }">
              <span class="font-semibold text-[#1269B5]">¥{{ formatMoney(row.newPrice) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调价原因" min-width="180" prop="changeReason" show-overflow-tooltip>
            <template #default="{ row }">{{ row.changeReason || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作人" prop="operatorName" width="110">
            <template #default="{ row }">{{ row.operatorName || '—' }}</template>
          </el-table-column>
        </el-table>
        <div v-if="!historyLoading && historyRows.length === 0" class="py-12 text-center text-sm text-slate-400">
          暂无调价记录
        </div>
        <div class="mt-4 flex justify-end">
          <el-pagination
              v-model:current-page="historyPagination.pageNum"
              v-model:page-size="historyPagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="historyPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleHistorySizeChange"
              @current-change="handleHistoryCurrentChange"
          />
        </div>
      </template>
    </div>

    <PriceEdit v-model="changeVisible" :row="changeRow" :item-types="ITEM_TYPES" :default-item-type="activeTab"
               @saved="loadList"/>
    <PolicyPriceEdit v-model="policyChangeVisible" :row="policyChangeRow" @saved="loadPolicy"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Edit, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  changePolicyPriceStatus,
  getPriceHistoryList,
  getPriceList,
  getPriceSummary,
  getPolicyPriceList,
} from '@/api/price';
import {formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import PriceEdit from './PriceEdit.vue';
import PolicyPriceEdit from './PolicyPriceEdit.vue';
// 5 类价表：对应后端的 DRUG / CONSUMABLE / INSPECTION / LABORATORY / TREATMENT
const ITEM_TYPES = [
  {key: 'DRUG', label: '药品', codeLabel: '药品编码', nameLabel: '药品名称'},
  {key: 'CONSUMABLE', label: '耗材', codeLabel: '耗材编码', nameLabel: '耗材名称'},
  {key: 'INSPECTION', label: '检查', codeLabel: '项目编码', nameLabel: '检查项目'},
  {key: 'LABORATORY', label: '检验', codeLabel: '项目编码', nameLabel: '检验项目'},
  {key: 'TREATMENT', label: '治疗', codeLabel: '项目编码', nameLabel: '治疗项目'},
];
const HISTORY_TAB = 'HISTORY';
const POLICY_TAB = 'POLICY';
const activeTab = ref('DRUG');
const typeMeta = () => ITEM_TYPES.find(t => t.key === activeTab.value) || ITEM_TYPES[0];
// 「加载中 ≠ 没有」：初值 true
const loading = ref(true);
const rows = ref([]);
const keyword = ref('');
const statusFilter = ref(null);
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const totalPriceItems = ref(null);
const historyLoading = ref(false);
const historyRows = ref([]);
const historyKeyword = ref('');
const historyPagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const changeVisible = ref(false);
const changeRow = ref(null);
// 政策价（POLICY 页签）
const policyLoading = ref(false);
const policyRows = ref([]);
const policyChangeVisible = ref(false);
const policyChangeRow = ref(null);

async function loadSummary() {
  try {
    const res = await getPriceSummary();
    totalPriceItems.value = Number(res.data ?? 0);
  } catch {
    totalPriceItems.value = null;
  }
}

async function loadList() {
  if (activeTab.value === POLICY_TAB) {
    await loadPolicy();
    return;
  }
  if (activeTab.value === HISTORY_TAB) {
    await loadHistory();
    return;
  }
  loading.value = true;
  try {
    const res = await getPriceList({
      itemType: activeTab.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value === null ? undefined : statusFilter.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    const data = res.data || {};
    rows.value = data.records || [];
    pagination.total = Number(data.total || 0);
  } catch (e) {
    rows.value = [];
    pagination.total = 0;
    
  } finally {
    loading.value = false;
  }
}

async function loadHistory() {
  historyLoading.value = true;
  try {
    const res = await getPriceHistoryList({
      itemType: activeTab.value === HISTORY_TAB ? undefined : activeTab.value,
      keyword: historyKeyword.value || undefined,
      pageNum: historyPagination.pageNum,
      pageSize: historyPagination.pageSize,
    });
    const data = res.data || {};
    historyRows.value = data.records || [];
    historyPagination.total = Number(data.total || 0);
  } catch (e) {
    historyRows.value = [];
    historyPagination.total = 0;
    
  } finally {
    historyLoading.value = false;
  }
}

function handleTabChange() {
  pagination.pageNum = 1;
  historyPagination.pageNum = 1;
  loadList();
}

/** 政策价列表：不分页（政策×档位就十几行），每次进页签实时拉 */
async function loadPolicy() {
  policyLoading.value = true;
  try {
    const res = await getPolicyPriceList();
    policyRows.value = res.data || [];
  } catch (e) {
    policyRows.value = [];
  } finally {
    policyLoading.value = false;
  }
}

function openPolicyChange(row) {
  policyChangeRow.value = row;
  policyChangeVisible.value = true;
}

async function togglePolicyStatus(row) {
  const disabling = row.status === 1;
  const tip = disabling
      ? `停用「${row.policyName}·${row.tierLabel}档」后：急诊接诊不再记该档诊查费（只留日志不拦人）、门诊该档回退兜底价。确定停用？`
      : `启用「${row.policyName}·${row.tierLabel}档」？启用后按价表金额计费。`;
  try {
    await ElMessageBox.confirm(tip, disabling ? '停用档位' : '启用档位', {
      confirmButtonText: disabling ? '确定停用' : '确定启用',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }
  try {
    await changePolicyPriceStatus({priceId: row.priceId, status: disabling ? 0 : 1});
    ElMessage.success(disabling ? '已停用' : '已启用');
    await loadPolicy();
  } catch (e) /* 错误已由全局拦截器统一提示 */ {

  }
}

function handleSearch() {
  pagination.pageNum = 1;
  loadList();
}

function handleReset() {
  keyword.value = '';
  statusFilter.value = null;
  handleSearch();
}

function handleHistorySearch() {
  historyPagination.pageNum = 1;
  loadHistory();
}

function handleSizeChange(size) {
  pagination.pageSize = size;
  pagination.pageNum = 1;
  loadList();
}

function handleCurrentChange(page) {
  pagination.pageNum = page;
  loadList();
}

function handleHistorySizeChange(size) {
  historyPagination.pageSize = size;
  historyPagination.pageNum = 1;
  loadHistory();
}

function handleHistoryCurrentChange(page) {
  historyPagination.pageNum = page;
  loadHistory();
}

function openChange(row) {
  changeRow.value = row;
  changeVisible.value = true;
}

onMounted(async () => {
  await Promise.all([loadSummary(), loadList()]);
});
</script>
