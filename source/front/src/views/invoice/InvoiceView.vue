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
                placeholder="发票号 / 收费单号 / 患者姓名"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="发票状态">
            <el-select v-model="query.invoiceStatus" class="!w-36" clearable placeholder="发票状态">
              <el-option v-for="o in INVOICE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 张发票
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column class-name="font-mono" label="发票号" min-width="200" prop="invoiceNo"/>
        <el-table-column label="患者" min-width="110">
          <template #default="{ row }">
            <span class="text-slate-800">{{ row.patientName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="收费单号" min-width="180" prop="chargeNo">
          <template #default="{ row }">{{ row.chargeNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="发票类型" width="110">
          <template #default="{ row }">{{ invoiceTypeText(row.invoiceType) }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :class="invoiceStatusTagClass(row.invoiceStatus)" class="border" effect="plain" size="small">
              {{ invoiceStatusText(row.invoiceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开票时间" prop="invoiceTime" width="170">
          <template #default="{ row }">{{ row.invoiceTime || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button :icon="View" link size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button :icon="Printer" link size="small" type="primary" @click="handlePrint(row)">打印</el-button>
            <el-button
                v-if="row.invoiceStatus !== 3"
                v-perm="'finance:invoice:delete'"
                :icon="CircleClose"
                link
                size="small"
                type="danger"
                @click="handleVoid(row)"
            >
              作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的发票
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

    <el-dialog v-model="detailVisible" destroy-on-close title="发票详情" width="640px">
      <div v-loading="detailLoading" class="space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">发票号：</span><span
              class="font-mono text-slate-700">{{ detail?.invoiceNo || '—' }}</span></div>
          <div><span class="text-slate-400">发票类型：</span><span
              class="text-slate-700">{{ invoiceTypeText(detail?.invoiceType) }}</span></div>
          <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{
              detail?.patientName || '—'
            }}</span></div>
          <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{
              detail?.patientNo || '—'
            }}</span></div>
          <div><span class="text-slate-400">收费单号：</span><span
              class="font-mono text-slate-700">{{ detail?.chargeNo || '—' }}</span></div>
          <div><span class="text-slate-400">金额：</span><span
              class="font-semibold text-slate-800">¥{{ formatMoney(detail?.totalAmount) }}</span></div>
          <div><span class="text-slate-400">状态：</span><span
              class="text-slate-700">{{ invoiceStatusText(detail?.invoiceStatus) }}</span></div>
          <div><span class="text-slate-400">开票时间：</span><span class="text-slate-700">{{
              detail?.invoiceTime || '—'
            }}</span></div>
          <div><span class="text-slate-400">打印时间：</span><span class="text-slate-700">{{
              detail?.printTime || '—'
            }}</span></div>
          <div><span class="text-slate-400">作废时间：</span><span class="text-slate-700">{{
              detail?.voidTime || '—'
            }}</span></div>
        </div>
        <div v-if="detail?.voidReason" class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-rose-700">
          作废原因：{{ detail.voidReason }}
        </div>
        <div v-if="detail?.electronicUrl" class="rounded-lg border border-slate-200 p-3">
          电子发票地址：<a :href="detail.electronicUrl" class="text-[#1269B5] hover:underline"
                          target="_blank">{{ detail.electronicUrl }}</a>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {CircleClose, Printer, Refresh, Search, View} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getInvoiceDetail, getInvoiceList, printInvoice, voidInvoice} from '@/api/invoice';
import {INVOICE_STATUS_OPTIONS, invoiceStatusTagClass, invoiceStatusText, invoiceTypeText,} from '@/lib/invoiceStatus';
import {formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// 「加载中 ≠ 没有」：初值 true，未加载完不显示「暂无数据」
const loading = ref(true);
const rows = ref([]);
const query = reactive({
  keyword: '',
  invoiceStatus: null,
});
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref(null);

async function loadList() {
  loading.value = true;
  try {
    const res = await getInvoiceList({
      keyword: query.keyword || undefined,
      invoiceStatus: query.invoiceStatus === null ? undefined : query.invoiceStatus,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    const data = res.data || {};
    rows.value = data.records || [];
    pagination.total = Number(data.total || 0);
  } catch (e) {
    rows.value = [];
    pagination.total = 0;
    ElMessage.error(e?.message || '加载发票列表失败');
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
  query.invoiceStatus = null;
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
    const res = await getInvoiceDetail(row.id);
    if (res.data)
      detail.value = res.data;
  } catch (e) {
    // 详情失败保留列表行，但要明确告知，不能静默
    ElMessage.warning(e?.message || '获取发票详情失败，已展示列表数据');
  } finally {
    detailLoading.value = false;
  }
}

async function handlePrint(row) {
  try {
    await ElMessageBox.confirm(`确认打印发票 ${row.invoiceNo}？`, '打印发票', {
      type: 'info',
      confirmButtonText: '确认打印',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  try {
    await printInvoice(row.id);
    ElMessage.success('打印已记录');
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '打印失败');
  }
}

async function handleVoid(row) {
  let reason = '';
  try {
    const r = await ElMessageBox.prompt(`作废发票 ${row.invoiceNo}，请填写作废原因`, '作废发票', {
      inputPlaceholder: '作废原因（必填）',
      inputValidator: (v) => (v && v.trim() ? true : '作废原因不能为空'),
      type: 'warning',
      confirmButtonText: '确认作废',
      cancelButtonText: '取消',
    });
    reason = r.value;
  } catch {
    return;
  }
  try {
    await voidInvoice(row.id, reason);
    ElMessage.success('发票已作废');
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '作废失败');
  }
}

onMounted(loadList);
</script>
