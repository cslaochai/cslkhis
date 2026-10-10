<template>
  <div>
    <!-- 两卡式列表页（口径参照 views/system/user/UserView.vue）：查询卡与表格卡分隔 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              clearable
              data-testid="txn-keyword"
              placeholder="流水号 / 账单号 / 患者姓名 / 渠道流水号 / 备注"
              style="width: 260px"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="资金方向">
          <el-select v-model="query.direction" clearable data-testid="txn-direction" placeholder="全部"
                     style="width: 120px">
            <el-option v-for="o in dictOptions(DICT_TYPE.PAY_DIRECTION)" :key="o.dictValue" :label="o.dictLabel"
                       :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="query.payMethod" clearable data-testid="txn-pay-method" placeholder="全部"
                     style="width: 130px">
            <el-option v-for="o in dictOptions(DICT_TYPE.PAY_METHOD)" :key="o.dictValue" :label="o.dictLabel"
                       :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.txnStatus" clearable data-testid="txn-status" placeholder="全部"
                     style="width: 110px">
            <el-option v-for="o in dictOptions(DICT_TYPE.PAY_TXN_STATUS)" :key="o.dictValue" :label="o.dictLabel"
                       :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="query.sourceType" clearable data-testid="txn-source" placeholder="全部"
                     style="width: 130px">
            <el-option v-for="o in dictOptions(DICT_TYPE.TXN_SOURCE)" :key="o.dictValue" :label="o.dictLabel"
                       :value="Number(o.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item data-testid="txn-date-range" label="流水日期">
          <el-date-picker
              v-model="dateRange"
              :shortcuts="dateShortcuts"
              end-placeholder="结束日期"
              range-separator="至"
              start-placeholder="开始日期"
              style="width: 250px"
              type="daterange"
              value-format="YYYY-MM-DD"
              @change="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" data-testid="txn-search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡：数据列 min-width 摊满卡片宽度；body 置 0 内边距让表格全幅贴边 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="txn-table" stripe>
        <el-table-column class-name="font-mono" label="流水号" min-width="160" prop="txnNo"/>
        <el-table-column label="方向" min-width="80">
          <template #default="{ row }">
            <span :class="Number(row.direction) === 1 ? 'text-emerald-600' : 'text-rose-600'">{{
                directionText(row.direction)
              }}</span>
          </template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="账单号" min-width="150" prop="billNo"/>
        <el-table-column label="患者" min-width="90" prop="patientName"/>
        <el-table-column label="渠道" min-width="90">
          <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" min-width="95">
          <template #default="{ row }">
            <span :class="Number(row.amount) < 0 ? 'text-rose-600' : 'text-slate-700'">¥{{
                formatMoney(row.amount)
              }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="85">
          <template #default="{ row }">{{ statusText(row.txnStatus) }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="95">
          <template #default="{ row }">{{ sourceText(row.sourceType) }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="渠道流水号" min-width="150" prop="channelTxnNo"
                         show-overflow-tooltip/>
        <el-table-column label="收银员" min-width="90" prop="cashierName"/>
        <el-table-column class-name="font-mono" label="时间" min-width="150" prop="txnTime"/>
        <el-table-column label="备注" min-width="200" prop="remark" show-overflow-tooltip/>
      </el-table>

      <!-- 分页：在流内紧跟表格底 -->
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
  </div>
</template>

<script setup>
/**
 * L3 支付流水台账（biz_payment_txn）—— 只读。
 *
 * 收/退同表带符号：direction=1 收款、2 退款；金额带符号（退款为负）。
 * 本页没有任何编辑口子：能改流水就等于能对不上渠道。
 * 冲正一笔收款的唯一入口是账单退费（/charge/settlementBill/refund）。
 */
import {onMounted, reactive, ref} from 'vue';
import {Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getPaymentTxnListPage} from '@/api/paymentTxn';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];
const directionText = (v) => dictLabelText(dictOptions(DICT_TYPE.PAY_DIRECTION), v);
const payMethodText = (v) => dictLabelText(dictOptions(DICT_TYPE.PAY_METHOD), v);
const statusText = (v) => dictLabelText(dictOptions(DICT_TYPE.PAY_TXN_STATUS), v);
const sourceText = (v) => dictLabelText(dictOptions(DICT_TYPE.TXN_SOURCE), v);
const loading = ref(true);
const rows = ref([]);
const query = reactive({
  keyword: '',
  direction: null,
  payMethod: null,
  txnStatus: null,
  sourceType: null,
});
// 流水日期区间（后端按 txn_date 过滤，格式 yyyy-MM-dd）
const dateRange = ref(null);
const dateShortcuts = [
  {text: '今日', value: () => [new Date(), new Date()]},
  {
    text: '昨日', value: () => {
      const d = new Date();
      d.setDate(d.getDate() - 1);
      return [d, d];
    }
  },
  {
    text: '近7天', value: () => {
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 6);
      return [start, end];
    }
  },
];
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

async function loadList() {
  loading.value = true;
  try {
    const res = await getPaymentTxnListPage({
      keyword: query.keyword || undefined,
      direction: query.direction ?? undefined,
      payMethod: query.payMethod ?? undefined,
      txnStatus: query.txnStatus ?? undefined,
      sourceType: query.sourceType ?? undefined,
      beginDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    rows.value = res.data?.records || [];
    pagination.total = Number(res.data?.total || 0);
  } catch (e) {
    rows.value = [];
    ElMessage.error(e.message || '加载支付流水失败');
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
  query.direction = null;
  query.payMethod = null;
  query.txnStatus = null;
  query.sourceType = null;
  dateRange.value = null;
  handleSearch();
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.PAY_DIRECTION,
    DICT_TYPE.PAY_METHOD,
    DICT_TYPE.PAY_TXN_STATUS,
    DICT_TYPE.TXN_SOURCE,
  ].join(','));
  await loadList();
});
</script>
