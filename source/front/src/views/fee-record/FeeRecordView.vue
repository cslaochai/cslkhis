<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                clearable
                data-testid="fee-keyword"
                placeholder="患者姓名 / 单号 / 项目"
                style="width: 220px"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="就诊类型">
            <el-select v-model="query.encounterType" clearable data-testid="fee-encounter-type" placeholder="就诊类型"
                       style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.ENCOUNTER_TYPE)" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="记账状态">
            <el-select v-model="query.feeStatus" clearable data-testid="fee-status" placeholder="记账状态"
                       style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.FEE_STATUS)" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="项目类型">
            <el-select v-model="query.itemType" clearable data-testid="fee-item-type" placeholder="项目类型"
                       style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE)" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="费用来源">
            <el-select v-model="query.sourceType" clearable data-testid="fee-source-type" placeholder="费用来源"
                       style="width: 130px">
              <el-option v-for="o in dictOptions(DICT_TYPE.FEE_SOURCE_TYPE)" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="fee-search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button :icon="Plus" data-testid="fee-book-open" type="primary" @click="openBook">手工补记账</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="fee-table" stripe>
        <el-table-column class-name="font-mono" label="记账流水号" min-width="160" prop="feeNo"/>
        <el-table-column label="患者" min-width="90" prop="patientName"/>
        <el-table-column label="就诊" min-width="70">
          <template #default="{ row }">{{ encounterTypeText(row.encounterType) }}</template>
        </el-table-column>
        <el-table-column label="项目" min-width="150" prop="itemName" show-overflow-tooltip/>
        <el-table-column align="right" label="单价" min-width="80" prop="price">
          <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column align="right" label="数量" min-width="70" prop="quantity"/>
        <el-table-column align="right" label="金额" min-width="90">
          <template #default="{ row }">
            <span :class="amountClass(row.amount)">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">{{ feeStatusText(row.feeStatus) }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="100">
          <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="记账时间" min-width="150" prop="bookTime"/>
        <el-table-column fixed="right" label="操作" width="130">
          <template #default="{ row }">
            <el-button :icon="View" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
                v-if="Number(row.feeStatus) === 1 || Number(row.feeStatus) === 3"
                data-testid="fee-reverse"
                link
                type="danger"
                @click="openReverse(row)"
            >红冲
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
            @change="loadList"
        />
      </div>
    </el-card>

    <!-- 详情：本行 + 红冲链 -->
    <FeeRecordDetail v-model="detailVisible" :id="detailRow?.id" :row="detailRow" :fee-status-text="feeStatusText"/>

    <!-- 手工补记账 -->
    <FeeRecordBookAdd v-model="bookVisible" :dict-options="dictOptions" @saved="loadList"/>

    <!-- 红冲 -->
    <FeeRecordReverseAdd v-model="reverseVisible" :row="reverseRow" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * L1 费用记账台账（biz_fee_record）
 *
 * 记账行是应收的唯一来源，本页三条铁律：
 * 1. 记账行不可修改，修正只能红冲（红冲行是负数行，指向被冲原行）；
 * 2. 手工补记账是错漏费用的唯一录入口，一切金额后端现算（单价×数量）；
 * 3. 状态由后端流转（待结算→已锁定→已结算 / 已红冲），本页只渲染不翻状态。
 */
import {onMounted, reactive, ref} from 'vue';
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getFeeRecordListPage} from '@/api/feeRecord';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import FeeRecordDetail from './FeeRecordDetail.vue';
import FeeRecordBookAdd from './FeeRecordBookAdd.vue';
import FeeRecordReverseAdd from './FeeRecordReverseAdd.vue';

const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];
const feeStatusText = (v) => dictLabelText(dictOptions(DICT_TYPE.FEE_STATUS), v);
const itemTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE), v);
const sourceTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.FEE_SOURCE_TYPE), v);
const encounterTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.ENCOUNTER_TYPE), v);
/** 已红冲(4) 的行金额是负数（红冲行），正常行按金额排序看 */
const amountClass = (v) => (Number(v) < 0 ? 'text-rose-600' : 'text-slate-700');
const loading = ref(true);
const rows = ref([]);
const query = reactive({
  keyword: '',
  encounterType: null,
  feeStatus: null,
  itemType: null,
  sourceType: null,
});
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

async function loadList() {
  loading.value = true;
  try {
    const res = await getFeeRecordListPage({
      keyword: query.keyword || undefined,
      encounterType: query.encounterType ?? undefined,
      feeStatus: query.feeStatus ?? undefined,
      itemType: query.itemType ?? undefined,
      sourceType: query.sourceType ?? undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    rows.value = res.data?.records || [];
    pagination.total = Number(res.data?.total || 0);
  } catch (e) {
    rows.value = [];
    ElMessage.error(e.message || '加载记账行失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.pageNum = 1;
  loadList();
}

function handleReset() {
  query.keyword = '';
  query.encounterType = null;
  query.feeStatus = null;
  query.itemType = null;
  query.sourceType = null;
  handleSearch();
}

// ==================== 详情（含红冲链） ====================
const detailVisible = ref(false);
const detailRow = ref(null);

function openDetail(row) {
  detailRow.value = row;
  detailVisible.value = true;
}

// ==================== 手工补记账 ====================
const bookVisible = ref(false);

function openBook() {
  bookVisible.value = true;
}

// ==================== 红冲 ====================
const reverseVisible = ref(false);
const reverseRow = ref(null);

function openReverse(row) {
  reverseRow.value = row;
  reverseVisible.value = true;
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.FEE_STATUS,
    DICT_TYPE.CHARGE_ITEM_TYPE,
    DICT_TYPE.FEE_SOURCE_TYPE,
    DICT_TYPE.ENCOUNTER_TYPE,
  ].join(','));
  await loadList();
});
</script>
