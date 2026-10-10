<template>
  <div class="space-y-6">
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="t in ITEM_TYPES" :key="t.key" :label="t.label" :name="t.key"/>
        <el-tab-pane :name="HISTORY_TAB" label="调价历史"/>
      </el-tabs>

      <!-- 价表列表 -->
      <template v-if="activeTab !== HISTORY_TAB">
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
              {{ ITEM_TYPES.find(t => t.key === row.itemType)?.label || `未知(${row.itemType})` }}
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

    <el-dialog v-model="changeVisible" destroy-on-close title="调价" width="480px">
      <div class="space-y-4">
        <div class="rounded-lg border border-slate-200 p-3 text-sm">
          <div class="flex items-center gap-2">
            <el-icon class="text-slate-400">
              <Tickets/>
            </el-icon>
            <span class="font-medium text-slate-800">{{ changeForm.itemName || '—' }}</span>
            <span class="font-mono text-xs text-slate-400">{{ changeForm.itemCode }}</span>
          </div>
          <div class="mt-2 text-slate-500">
            类型：{{ ITEM_TYPES.find(t => t.key === changeForm.itemType)?.label || changeForm.itemType }}
            <span class="ml-3">当前价：<span class="font-semibold text-slate-700">¥{{
                formatMoney(changeForm.oldPrice)
              }}</span></span>
          </div>
        </div>
        <el-form label-width="84px">
          <el-form-item label="新价格" required>
            <el-input-number v-model="changeForm.newPrice" :min="0" :precision="2" :step="1" class="!w-full"/>
          </el-form-item>
          <el-form-item label="调价原因" required>
            <el-input v-model="changeForm.reason" :rows="3" placeholder="如：执行省级药品集中采购新价" type="textarea"/>
          </el-form-item>
        </el-form>
        <p class="rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
          调价会即时更新价表并写入调价历史，操作人取当前登录人，不可撤回。
        </p>
      </div>
      <template #footer>
        <el-button @click="changeVisible = false">取消</el-button>
        <el-button v-perm="'finance:price:edit'" :loading="changeSubmitting" type="primary" @click="submitChange">
          确认调价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Edit, Refresh, Search, Tickets} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {changePrice, getPriceHistoryList, getPriceList, getPriceSummary} from '@/api/price';
import {formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
// 5 类价表：对应后端的 DRUG / CONSUMABLE / INSPECTION / LABORATORY / TREATMENT
const ITEM_TYPES = [
  {key: 'DRUG', label: '药品', codeLabel: '药品编码', nameLabel: '药品名称'},
  {key: 'CONSUMABLE', label: '耗材', codeLabel: '耗材编码', nameLabel: '耗材名称'},
  {key: 'INSPECTION', label: '检查', codeLabel: '项目编码', nameLabel: '检查项目'},
  {key: 'LABORATORY', label: '检验', codeLabel: '项目编码', nameLabel: '检验项目'},
  {key: 'TREATMENT', label: '治疗', codeLabel: '项目编码', nameLabel: '治疗项目'},
];
const HISTORY_TAB = 'HISTORY';
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
const changeSubmitting = ref(false);
const changeForm = reactive({
  itemType: '',
  itemId: '',
  itemCode: '',
  itemName: '',
  oldPrice: 0,
  newPrice: 0,
  reason: '',
});

async function loadSummary() {
  try {
    const res = await getPriceSummary();
    totalPriceItems.value = Number(res.data ?? 0);
  } catch {
    totalPriceItems.value = null;
  }
}

async function loadList() {
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
    ElMessage.error(e?.message || '加载价格列表失败');
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
    ElMessage.error(e?.message || '加载调价历史失败');
  } finally {
    historyLoading.value = false;
  }
}

function handleTabChange() {
  pagination.pageNum = 1;
  historyPagination.pageNum = 1;
  loadList();
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
  changeForm.itemType = row.itemType || activeTab.value;
  changeForm.itemId = row.itemId;
  changeForm.itemCode = row.itemCode || '';
  changeForm.itemName = row.itemName || '';
  changeForm.oldPrice = Number(row.price ?? 0);
  changeForm.newPrice = Number(row.price ?? 0);
  changeForm.reason = '';
  changeVisible.value = true;
}

async function submitChange() {
  if (!changeForm.reason.trim()) {
    ElMessage.warning('调价原因必填（进调价留痕）');
    return;
  }
  if (changeForm.newPrice === null || Number(changeForm.newPrice) < 0) {
    ElMessage.warning('请填写正确的新价格');
    return;
  }
  changeSubmitting.value = true;
  try {
    await changePrice({
      itemType: changeForm.itemType,
      itemId: changeForm.itemId,
      newPrice: Number(changeForm.newPrice),
      reason: changeForm.reason.trim(),
    });
    ElMessage.success('调价成功，已写入调价历史');
    changeVisible.value = false;
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '调价失败');
  } finally {
    changeSubmitting.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadSummary(), loadList()]);
});
</script>
