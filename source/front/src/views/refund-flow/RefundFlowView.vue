<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                :prefix-icon="Search"
                class="!w-72"
                clearable
                data-testid="refund-flow-keyword"
                placeholder="退费单号 / 原收费单号 / 患者姓名"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="流水来源">
            <el-select v-model="query.flowSource" :fit-input-width="false" class="!w-40" clearable
                       placeholder="流水来源">
              <el-option
                  v-for="o in flowSourceOptions"
                  :key="o.dictValue"
                  :label="o.dictLabel"
                  :value="Number(o.dictValue)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="退费方式">
            <el-select v-model="query.refundMethod" :fit-input-width="false" class="!w-40" clearable
                       placeholder="退费方式">
              <el-option
                  v-for="o in refundMethodOptions"
                  :key="o.dictValue"
                  :label="o.dictLabel"
                  :value="Number(o.dictValue)"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="refund-flow-search" type="primary" @click="handleSearch">查询
            </el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            本页合计
            <span class="font-semibold text-slate-700">{{
                sumAmount === null ? '—' : `¥${formatMoney(sumAmount)}`
              }}</span>
            · 共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 笔流水
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="refund-flow-table" stripe>
        <el-table-column class-name="font-mono" label="退费单号" min-width="180" prop="refundNo"/>
        <el-table-column label="患者" min-width="100">
          <template #default="{ row }">{{ row.patientName || '—' }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="原收费单号" min-width="180" prop="chargeNo">
          <template #default="{ row }">{{ row.chargeNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="退费类型" width="100">
          <template #default="{ row }">{{ dictLabelText(refundTypeOptions, row.refundType) }}</template>
        </el-table-column>
        <el-table-column align="right" label="实退金额" width="110">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流水来源" width="120">
          <template #default="{ row }">
            <el-tag
                :class="SOURCE_TAG_CLASS[row.flowSource] || 'bg-slate-100 text-slate-600'"
                class="border"
                data-testid="refund-flow-source-tag"
                effect="plain"
                size="small"
            >
              {{ dictLabelText(flowSourceOptions, row.flowSource) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退费方式" width="110">
          <template #default="{ row }">{{ dictLabelText(refundMethodOptions, row.refundMethod) }}</template>
        </el-table-column>
        <el-table-column label="原支付" width="100">
          <template #default="{ row }">{{ dictLabelText(payMethodOptions, row.payMethod) }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="渠道退费流水号" min-width="190">
          <template #default="{ row }">{{ row.channelRefundNo || '—' }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="来源申请号" min-width="180">
          <template #default="{ row }">{{ row.applyNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="退费人" prop="refundBy" width="100">
          <template #default="{ row }">{{ row.refundBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="退费时间" prop="refundTime" width="170">
          <template #default="{ row }">{{ row.refundTime || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="refund-flow-btn-detail" link size="small" type="primary"
                       @click="openDetail(row)">明细
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的退费流水
      </div>

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

    <!-- 明细：只读 -->
    <el-dialog v-model="detailVisible" data-testid="refund-flow-detail-dialog" destroy-on-close title="退费流水明细"
               width="760px">
      <div v-loading="detailLoading" class="space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">退费单号：</span><span
              class="font-mono text-slate-700">{{ detail?.refundNo || '—' }}</span></div>
          <div><span class="text-slate-400">原收费单号：</span><span
              class="font-mono text-slate-700">{{ detail?.chargeNo || '—' }}</span></div>
          <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{
              detail?.patientName || '—'
            }}</span></div>
          <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{
              detail?.patientNo || '—'
            }}</span></div>
          <div><span class="text-slate-400">实退金额：</span><span
              class="font-semibold text-slate-800">¥{{ formatMoney(detail?.totalAmount) }}</span></div>
          <div><span class="text-slate-400">退费类型：</span><span
              class="text-slate-700">{{ dictLabelText(refundTypeOptions, detail?.refundType) }}</span></div>
          <div><span class="text-slate-400">流水来源：</span><span
              class="text-slate-700">{{ dictLabelText(flowSourceOptions, detail?.flowSource) }}</span></div>
          <div><span class="text-slate-400">来源申请号：</span><span
              class="font-mono text-slate-700">{{ detail?.applyNo || '—' }}</span></div>
          <div><span class="text-slate-400">退费方式：</span><span
              class="text-slate-700">{{ dictLabelText(refundMethodOptions, detail?.refundMethod) }}</span></div>
          <div><span class="text-slate-400">原支付方式：</span><span
              class="text-slate-700">{{ dictLabelText(payMethodOptions, detail?.payMethod) }}</span></div>
          <div><span class="text-slate-400">渠道退费流水号：</span><span
              class="font-mono text-slate-700">{{ detail?.channelRefundNo || '—' }}</span></div>
          <div><span class="text-slate-400">医保报盘：</span><span
              class="text-slate-700">{{ detail?.insuranceCancelled === 1 ? '已随本次退费撤销（2305）' : '不涉及' }}</span>
          </div>
          <div><span class="text-slate-400">退费人：</span><span class="text-slate-700">{{
              detail?.refundBy || '—'
            }}</span></div>
          <div><span class="text-slate-400">退费时间：</span><span class="text-slate-700">{{
              detail?.refundTime || '—'
            }}</span></div>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <span class="text-slate-400">退费原因：</span><span class="text-slate-700">{{
            detail?.refundReason || '—'
          }}</span>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <div class="mb-2 text-slate-400">冲正的收费明细</div>
          <el-table :data="detail?.details || []" border data-testid="refund-flow-detail-table" size="small">
            <el-table-column label="项目名称" min-width="180" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="规格" min-width="110" prop="specification">
              <template #default="{ row }">{{ row.specification || '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="退费数量" prop="refundQuantity" width="100"/>
            <el-table-column label="单位" prop="unit" width="70">
              <template #default="{ row }">{{ row.unit || '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="单价" width="90">
              <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
            </el-table-column>
            <el-table-column align="right" label="退费金额" width="100">
              <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
            </el-table-column>
            <el-table-column class-name="font-mono text-xs" label="来源单号" min-width="150" prop="sourceNo">
              <template #default="{ row }">{{ row.sourceNo || '—' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Refresh, Search, View} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getRefundFlowDetail, getRefundFlowList} from '@/api/refundFlow';
import {getDictDataMapList} from '@/api/system';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

/** 流水来源：本表只有这一列会为空/为 0，其余来源都对应一次真实的冲正动作 */
const SOURCE_TAG_CLASS = {
  0: 'bg-slate-100 text-slate-500',
  1: 'bg-blue-100 text-blue-700',
  2: 'bg-emerald-100 text-emerald-700',
  3: 'bg-amber-100 text-amber-700',
};
const loading = ref(true);
const rows = ref([]);
const query = reactive({
  keyword: '',
  flowSource: null,
  refundMethod: null,
});
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const flowSourceOptions = ref([]);
const refundMethodOptions = ref([]);
const payMethodOptions = ref([]);
const refundTypeOptions = ref([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref(null);
/** 本页合计：退出去的总钱数。按当前筛选条件现算，不是前端自造的数字 */
const sumAmount = ref(null);

async function loadDicts() {
  try {
    const res = await getDictDataMapList('his_refund_flow_source,his_refund_method,his_pay_method,his_refund_apply_type,his_refund_status');
    if (res.code === 200 && res.data) {
      flowSourceOptions.value = res.data['his_refund_flow_source'] || [];
      refundMethodOptions.value = res.data['his_refund_method'] || [];
      payMethodOptions.value = res.data['his_pay_method'] || [];
      refundTypeOptions.value = res.data['his_refund_apply_type'] || [];
    }
  } catch (e) {
    console.error('加载退费流水字典失败', e);
  }
}

async function loadList() {
  loading.value = true;
  try {
    const res = await getRefundFlowList({
      keyword: query.keyword || undefined,
      flowSource: query.flowSource === null ? undefined : query.flowSource,
      refundMethod: query.refundMethod === null ? undefined : query.refundMethod,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    const data = res.data || {};
    rows.value = data.records || [];
    pagination.total = Number(data.total || 0);
    sumAmount.value = rows.value.reduce((s, r) => s + Number(r.totalAmount || 0), 0);
  } catch (e) {
    rows.value = [];
    pagination.total = 0;
    sumAmount.value = null;
    ElMessage.error(e?.message || '加载退费流水失败');
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
  query.flowSource = null;
  query.refundMethod = null;
  handleSearch();
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

async function openDetail(row) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = row;
  try {
    const res = await getRefundFlowDetail(row.id);
    if (res.data)
      detail.value = res.data;
  } catch (e) {
    ElMessage.warning(e?.message || '获取退费流水明细失败，已展示列表数据');
  } finally {
    detailLoading.value = false;
  }
}

onMounted(async () => {
  await loadDicts();
  await loadList();
});
</script>
