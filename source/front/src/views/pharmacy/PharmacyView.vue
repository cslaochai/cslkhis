<template>
  <div>
    <div class="flex items-center justify-between mb-3">
      <el-button v-perm="'pharmacy:stock:add'" :icon="Plus" type="primary" @click="handleAdd">新增药品</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-1 gap-4 sm:grid-cols-4">
      <div v-for="item in [
        { label: '库存总量', value: totalStock, icon: Box, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: '库存总值', value: `¥${totalValue.toLocaleString()}`, icon: Money, color: 'text-emerald-600', bg: 'bg-emerald-50' },
        { label: '库存预警', value: lowStockCount, icon: Warning, color: 'text-amber-600', bg: 'bg-amber-50' },
        { label: '缺货', value: outOfStockCount, icon: Tickets, color: 'text-red-600', bg: 'bg-red-50' },
      ]" :key="item.label" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div :class="['rounded-lg p-2.5', item.bg]">
            <component :is="item.icon" :class="['h-5 w-5', item.color]"/>
          </div>
          <div>
            <p :class="['text-lg font-bold', item.color]">{{ item.value }}</p>
            <p class="text-xs text-slate-500">{{ item.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="searchTerm" :prefix-icon="Search" clearable placeholder="搜索药品名称、编码..."
                    style="width: 220px" @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="statusFilter" class="!w-36" placeholder="库存状态" @change="handleSearch">
            <el-option label="全部状态" value="all"/>
            <el-option :value="1" label="正常"/>
            <el-option :value="2" label="预警"/>
            <el-option :value="3" label="缺货"/>
          </el-select>
        </el-form-item>
        <el-form-item label="库存地点">
          <el-select v-model="roomFilter" class="!w-32" data-testid="stock-room-filter" placeholder="库存地点"
                     @change="handleSearch">
            <el-option label="两层都看" value="all"/>
            <el-option v-for="d in roomDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 药品表格 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="medicines" :max-height="tableMaxHeight" stripe>
        <el-table-column label="药品名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.drugName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="编码" prop="drugCode" width="100"/>
        <el-table-column label="规格" prop="specification" width="120"/>
        <el-table-column label="批号" prop="batchNo" width="120"/>
        <el-table-column align="center" label="地点" width="80">
          <template #default="{ row }">
            <el-tag :type="row.stockRoom === 1 ? 'primary' : 'success'" effect="plain" size="small">
              {{ row.stockRoomText || (Number(row.stockRoom) === 1 ? '药库' : '药房') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="right" label="进价" width="90">
          <template #default="{ row }">¥{{ row.costPrice }}</template>
        </el-table-column>
        <el-table-column align="right" label="售价" width="90">
          <template #default="{ row }">¥{{ row.retailPrice }}</template>
        </el-table-column>
        <el-table-column align="right" label="库存" width="100">
          <template #default="{ row }">
            <span
                :class="row.stockStatus === 2 ? 'text-amber-600 font-bold' : row.stockStatus === 3 ? 'text-red-600 font-bold' : 'text-slate-900'">
              {{ row.quantity }}
            </span>
            <span class="text-xs text-slate-400 ml-1">{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="效期" prop="expiryDate" width="110"/>
        <el-table-column align="right" label="锁定/可用" width="105">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.lockedQuantity }}</span>
            <span class="text-slate-400"> / </span>
            <span class="text-slate-900 font-medium">{{ row.availableQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="供货商" min-width="150" prop="supplier" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="row.supplierId ? '' : 'text-amber-600'">
              {{ row.supplier || '—' }}
              <span v-if="!row.supplierId" class="text-xs">（未挂档案，不能退供应商）</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span
                :class="['inline-block rounded px-2 py-0.5 text-xs font-medium border', statusColors[row.stockStatus] || '']">
              {{ statusMap[row.stockStatus] || '未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-perm="'pharmacy:stock:edit'" :icon="ArrowDown" link type="success" @click="handleInbound(row)">
              入库
            </el-button>
            <el-button v-perm="'pharmacy:stock:edit'" :icon="ArrowUp" link type="warning" @click="handleOutbound(row)">
              出库
            </el-button>
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

    <!-- 新增库存弹窗（从药品字典选药 + 批次入库） -->
    <PharmacyStockAdd v-model="showAddDialog" :room-dict="roomDict" @saved="loadData"/>

    <!-- 入库/出库弹窗 -->
    <PharmacyStockInOut v-model="showInOutDialog" :row="inOutRow" :type="inOutType" @saved="loadData"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {ArrowDown, ArrowUp, Box, Money, Plus, Refresh, Search, Tickets, Warning} from '@element-plus/icons-vue';
import {getStockList} from '@/api/pharmacy';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PharmacyStockAdd from './PharmacyStockAdd.vue';
import PharmacyStockInOut from './PharmacyStockInOut.vue';

const loading = ref(false);
const medicines = ref([]);
const searchTerm = ref('');
const statusFilter = ref('all');
/** 库位筛选（1-药库 2-药房，sql/154）；'all' = 两层都看，不传给后端 */
const roomFilter = ref('all');
const roomDict = ref([]);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(DICT_TYPE.STOCK_ROOM);
    roomDict.value = res?.data?.[DICT_TYPE.STOCK_ROOM] || [];
  } catch (e) {
    console.error('加载库位字典失败', e);
  }
};
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
// 新增库存弹窗（选药 + 批次入库的子组件）
const showAddDialog = ref(false);
// 入库/出库弹窗
const showInOutDialog = ref(false);
const inOutType = ref('in');
const inOutRow = ref(null);
const statusColors = {
  1: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  2: 'bg-amber-100 text-amber-700 border-amber-200',
  3: 'bg-red-100 text-red-600 border-red-200',
  4: 'bg-slate-100 text-slate-500 border-slate-200',
};
const statusMap = {
  1: '正常',
  2: '预警',
  3: '缺货',
  4: '过期',
};
const totalStock = computed(() => medicines.value.reduce((sum, m) => sum + (m.quantity || 0), 0));
const totalValue = computed(() => medicines.value.reduce((sum, m) => sum + (m.totalAmount || 0), 0));
const lowStockCount = computed(() => medicines.value.filter((m) => m.stockStatus === 2).length);
const outOfStockCount = computed(() => medicines.value.filter((m) => m.stockStatus === 3).length);
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    if (searchTerm.value)
      params.drugName = searchTerm.value;
    if (statusFilter.value !== 'all')
      params.stockStatus = statusFilter.value;
    if (roomFilter.value !== 'all')
      params.stockRoom = roomFilter.value;
    const res = await getStockList(params);
    medicines.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载药品库存失败:', error);
  } finally {
    loading.value = false;
  }
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchTerm.value = '';
  statusFilter.value = 'all';
  roomFilter.value = 'all';
  handleSearch();
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
// 新增库存（打开子组件弹窗，弹窗内自行加载下拉并 reset 表单）
const handleAdd = () => {
  showAddDialog.value = true;
};
// 入库/出库
const handleInbound = (row) => {
  inOutType.value = 'in';
  inOutRow.value = row;
  showInOutDialog.value = true;
};
const handleOutbound = (row) => {
  inOutType.value = 'out';
  inOutRow.value = row;
  showInOutDialog.value = true;
};
onMounted(() => {
  loadDicts();
  loadData();
});
</script>
