<template>
  <div class="space-y-4">
    <el-card shadow="never">
      <div class="flex flex-wrap items-center gap-3">
        <!-- 收费台=门诊收费窗：住院不走这里（住院有专属的「住院结算」页，管预交金+出院汇总+多退少补）。
             真实 HIS 也是按岗位分窗口，绝不在收费台内切门诊/住院，否则会和住院结算页抢同一批费（同 encounter_id=admission_id）造成双重计费 -->
        <el-input
            v-model="keyword"
            clearable
            placeholder="患者姓名 / 患者号 / 就诊单号"
            style="width: 260px"
            @keyup.enter="handleSearch"
        />
        <el-button :icon="Search" data-testid="cash-search" type="primary" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table
          v-loading="boardLoading"
          :data="boardRows"
          data-testid="cash-board-table"
          stripe
          style="width: 100%"
      >
        <el-table-column label="患者" prop="patientName" width="110"/>
        <el-table-column label="患者号" prop="patientNo" show-overflow-tooltip width="150"/>
        <el-table-column label="就诊单号" min-width="180" prop="encounterNo" show-overflow-tooltip/>
        <el-table-column align="right" label="待出账应收" width="170">
          <template #default="{ row }">
            <span v-if="Number(row.pendingFeeCount) > 0" class="font-medium text-amber-600">
              ¥{{ money(row.pendingFeeAmount) }}
              <b class="text-xs font-normal text-slate-400">/ {{ row.pendingFeeCount }} 行</b>
            </span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="未收账单差额" width="170">
          <template #default="{ row }">
            <span v-if="Number(row.unpaidBillCount) > 0" class="font-medium text-red-600">
              ¥{{ money(row.unpaidBillAmount) }}
              <b class="text-xs font-normal text-slate-400">/ {{ row.unpaidBillCount }} 张</b>
            </span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最近发生" prop="lastTime" width="180"/>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button :icon="Coin" data-testid="cash-open" link type="primary" @click="openWorkbench(row)">
              结算收款
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="text-sm text-slate-400">当前没有待收费的就诊</span>
        </template>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSearch"
            @current-change="loadBoard"
        />
      </div>
    </el-card>

    <el-dialog v-model="workbench" data-testid="cash-workbench" title="就诊结算工作台" width="1180px">
      <div v-if="current" v-loading="workLoading" class="space-y-4">
        <div class="rounded-lg bg-slate-50 p-3">
          <div class="grid grid-cols-2 gap-2 text-sm sm:grid-cols-4">
            <div><span class="text-slate-500">患者：</span><b>{{ current.patientName }}</b></div>
            <div><span class="text-slate-500">患者号：</span>{{ current.patientNo || '—' }}</div>
            <div><span class="text-slate-500">就诊单号：</span>{{ current.encounterNo || '—' }}</div>
            <div>
              <span class="text-slate-500">就诊类型：</span>
              {{ Number(current.encounterType) === 2 ? '住院' : '门诊' }}
            </div>
          </div>
        </div>

        <!-- ① 费用明细（标准 HIS 收费台主区：待收费项目逐行列出，收不收都看得见）。盒式与②③统一 -->
        <div>
          <div class="mb-2 flex items-center justify-between">
            <h4 class="text-sm font-bold text-slate-700">① 费用明细（待收费项目）</h4>
            <span class="text-sm text-slate-500">
              应收合计 ¥{{ money(feeTotal) }} · 已勾选 <b class="text-slate-900">{{ selectedFees.length }}</b> 行
              ¥{{ money(selectedTotal) }}
            </span>
          </div>
          <div class="rounded-lg border border-slate-200 p-3">
            <el-table
                :data="fees"
                data-testid="cash-fee-table"
                max-height="240"
                size="small"
                @selection-change="onFeeSelectionChange"
            >
            <el-table-column type="selection" width="42"/>
            <el-table-column label="记账号" prop="feeNo" width="150"/>
            <el-table-column label="项目名称" min-width="180" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="类别" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ itemTypeText(row.itemType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="来源" width="100">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ sourceTypeText(row.sourceType) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="来源单号" prop="sourceNo" show-overflow-tooltip width="160"/>
            <el-table-column align="right" label="数量" width="70">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column align="right" label="单价" width="80">
              <template #default="{ row }">{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="100">
              <template #default="{ row }">
                <span :class="Number(row.amount) < 0 ? 'text-emerald-600' : 'font-medium'">{{
                    money(row.amount)
                  }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="feeStatusTag(row.feeStatus)" size="small">{{ feeStatusText(row.feeStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="记账时间" prop="bookTime" width="160"/>
            <template #empty>
              <span class="text-sm text-slate-400">
                该就诊没有待收费项目{{ bills.length ? '，费用已全部出账，请对下方收款单据收款' : '' }}
              </span>
            </template>
            </el-table>
          </div>
        </div>

        <!-- ② 收费 / 出账：标准 HIS 收费台的「收费」动作，选结算方式 → 出账试算 → 确认出账。
             标题与 ①③ 同级（不缩进边框盒子里）；试算明细列表常驻显示，没试算时给空态引导 -->
        <div>
          <h4 class="mb-2 text-sm font-bold text-slate-700">② 收费 / 出账</h4>
          <div class="rounded-lg border border-slate-200 p-3">
            <div class="flex flex-wrap items-center gap-4">
              <div class="flex items-center gap-2">
                <span class="text-sm text-slate-500">结算方式</span>
                <el-select v-model="settleForm.settlementMode" class="!w-28" data-testid="cash-settle-mode"
                           @change="invalidatePreview">
                  <el-option v-for="o in selectOptions(DICT_TYPE.SETTLEMENT_MODE)" :key="o.value" :label="o.label"
                             :value="o.value"/>
                </el-select>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm text-slate-500">账单类型</span>
                <el-select v-model="settleForm.billType" class="!w-40" clearable placeholder="自动"
                           @change="invalidatePreview">
                  <el-option v-for="o in selectOptions(DICT_TYPE.BILL_TYPE)" :key="o.value" :label="o.label"
                             :value="o.value"/>
                </el-select>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm text-slate-500">院内优惠</span>
                <el-input-number
                    v-model="settleForm.discountAmount"
                    :min="0"
                    :precision="2"
                    :step="10"
                    class="!w-36"
                    controls-position="right"
                    data-testid="cash-discount"
                    @change="invalidatePreview"
                />
              </div>
              <el-button :icon="Document" :loading="previewLoading" data-testid="cash-preview" @click="runPreview">
                出账试算
              </el-button>
              <el-button
                  :disabled="!preview"
                  :icon="Coin"
                  :loading="settling"
                  data-testid="cash-settle"
                  type="primary"
                  @click="runSettle"
              >
                确认出账
              </el-button>
            </div>

            <div class="mt-3 space-y-2" data-testid="cash-preview-result">
              <div v-if="preview" class="grid grid-cols-3 gap-2 text-sm sm:grid-cols-8">
                <div><p class="text-xs text-slate-400">记账行数</p><b>{{ preview.feeCount }}</b></div>
                <div><p class="text-xs text-slate-400">应收合计</p><b>¥{{ money(preview.totalAmount) }}</b></div>
                <div><p class="text-xs text-slate-400">优惠</p><b>¥{{ money(preview.discountAmount) }}</b></div>
                <div><p class="text-xs text-slate-400">统筹</p><b class="text-emerald-700">¥{{
                    money(preview.poolAmount)
                  }}</b></div>
                <div><p class="text-xs text-slate-400">个账</p><b
                    class="text-emerald-700">¥{{ money(preview.accountAmount) }}</b></div>
                <div><p class="text-xs text-slate-400">自付</p><b>¥{{ money(preview.selfAmount) }}</b></div>
                <div><p class="text-xs text-slate-400">患者应缴</p><b
                    class="text-red-600">¥{{ money(preview.payableAmount) }}</b></div>
                <div>
                  <p class="text-xs text-slate-400">{{ preview.settlementModeText }}</p>
                  <b class="text-sm">{{ preview.insuranceType || '—' }}
                    <span v-if="preview.coverageRatio != null" class="text-xs font-normal text-slate-500">
                      报销 {{ money(preview.coverageRatio) }}%
                    </span>
                  </b>
                </div>
              </div>
              <el-table :data="preview?.items || []" max-height="200" size="small">
                <el-table-column label="项目" min-width="180" prop="itemName" show-overflow-tooltip/>
                <el-table-column label="类别" width="90">
                  <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
                </el-table-column>
                <el-table-column align="center" label="目录" width="70">
                  <template #default="{ row }">
                    {{ ['自费', '甲类', '乙类', '丙类'][Number(row.catalogType)] || '—' }}
                  </template>
                </el-table-column>
                <el-table-column align="right" label="金额" width="100">
                  <template #default="{ row }">{{ money(row.amount) }}</template>
                </el-table-column>
                <el-table-column align="right" label="优惠" width="90">
                  <template #default="{ row }">{{ money(row.discountAmount) }}</template>
                </el-table-column>
                <el-table-column align="right" label="统筹" width="90">
                  <template #default="{ row }">{{ money(row.poolAmount) }}</template>
                </el-table-column>
                <el-table-column align="right" label="个账" width="90">
                  <template #default="{ row }">{{ money(row.accountAmount) }}</template>
                </el-table-column>
                <el-table-column align="right" label="自付" width="90">
                  <template #default="{ row }">
                    <span class="font-medium">{{ money(row.selfAmount) }}</span>
                  </template>
                </el-table-column>
                <template #empty>
                  <span class="text-sm text-slate-400">勾选上方费用后点「出账试算」，试算明细显示在这里</span>
                </template>
              </el-table>
            </div>
          </div>
        </div>

        <!-- ③ 收款单据（已出账账单）。盒式与①②统一 -->
        <div>
          <div class="mb-2 flex items-center justify-between">
            <h4 class="text-sm font-bold text-slate-700">③ 收款单据（已出账账单）</h4>
            <span class="text-sm text-slate-500">未收齐合计 <b class="text-red-600">¥{{
                money(unpaidBillAmount)
              }}</b></span>
          </div>
          <div class="rounded-lg border border-slate-200 p-3">
          <el-table :data="bills" data-testid="cash-bill-table" max-height="220" size="small">
            <el-table-column label="账单号" prop="billNo" width="160"/>
            <el-table-column label="结算类型" width="120">
              <template #default="{ row }">{{ billTypeText(row.billType) }}</template>
            </el-table-column>
            <el-table-column label="费用构成" min-width="220">
              <template #default="{ row }">
                <div v-if="row.feeComposition?.length" class="flex flex-wrap gap-1">
                  <el-tag
                      v-for="c in row.feeComposition"
                      :key="c.itemType"
                      size="small"
                      type="info"
                  >{{ itemTypeText(c.itemType) }} ¥{{ money(c.amount) }}</el-tag>
                </div>
                <span v-else class="text-slate-300">—</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="billStatusTag(row.billStatus)" size="small">{{ billStatusText(row.billStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="应收" width="100">
              <template #default="{ row }">{{ money(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="优惠" width="90">
              <template #default="{ row }">{{ money(row.discountAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="统筹" width="90">
              <template #default="{ row }">{{ money(row.poolAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="应缴" width="100">
              <template #default="{ row }"><b>{{ money(row.payableAmount) }}</b></template>
            </el-table-column>
            <el-table-column align="right" label="已收" width="100">
              <template #default="{ row }">{{ money(row.paidAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="尚需缴" width="100">
              <template #default="{ row }">
                <span :class="Number(row.unpaidAmount) > 0 ? 'font-medium text-red-600' : 'text-slate-400'">
                  {{ money(row.unpaidAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="出账人" prop="billByName" width="100"/>
            <el-table-column label="出账时间" prop="billTime" width="160"/>
            <el-table-column fixed="right" label="操作" width="90">
              <template #default="{ row }">
                <!-- 收费员动线只留一个口：未收齐点「收款」→ 复用预约收费弹层（默认现金，切支付宝/微信出码）；
                     已结清给「出票」（L4）。退费/作废走退费管理与账单台账页，不在收费台铺开 -->
                <el-button v-if="Number(row.unpaidAmount) > 0" data-testid="cash-btn-pay" link type="primary"
                           @click="openPay(row)">收款
                </el-button>
                <el-button v-else-if="Number(row.billStatus) === 3" data-testid="cash-btn-issue" link type="success"
                           @click="runIssue(row)">出票
                </el-button>
              </template>
            </el-table-column>
            <template #empty><span class="text-sm text-slate-400">该就诊还没有账单</span></template>
          </el-table>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="workbench = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ============ 收款 ============ -->
    <!-- 与预约收费同一弹层：默认现金档（窗口当场点钞记流水），切支付宝/微信即出对应渠道的码；
         金额取账单尚需缴，页面不改数。多渠道拆笔（余额+医保）由退费/台账侧的账单详情承接 -->
    <QrPayDialog v-model="qrPayDialog" allow-cash :amount="qrPayBill?.unpaidAmount" :bill-id="qrPayBill?.id"
                 :bill-no="qrPayBill?.billNo" :patient-name="qrPayBill?.patientName" @paid="onPaid"/>
  </div>
</template>

<script setup>
/**
 * 收费结算窗口（四层：L1 记账 → L2 结算 → L3 支付 → L4 票据）
 *
 * 页面只走一条链，每一步都只碰自己那一层：
 *   待收费就诊榜 →（选记账行）出账单 → 收款（默认现金，可切支付宝/微信出码）→ 出票
 *
 * 三条前端必须守住的口径：
 * 1. **应收金额不由本页传入**：settle 只提交「哪些记账行 + 优惠多少 + 结算方式」，
 *    合计、统筹、应缴一律后端现算；试算（settlePreview）与出账共用同一份算法，
 *    所以「试算说该收多少」和「账单开出多少」必然是同一个数。
 * 2. **payableAmount 才是患者该掏的钱**：已减优惠、统筹、个账。
 *    selfAmount 是「应收 − 统筹」，仍带着优惠与个账两段，两者并排显示必然不等。
 *    统筹不是支付方式，不进现金清点，所以收款明细里没有「统筹」这一档。
 * 3. **是否付清由流水比出来**：本页不翻状态，只在收款后重新拉账单；
 *    unpaidAmount 由后端算，前端不再减一遍。
 */
import {computed, onMounted, ref} from 'vue';
import {Coin, Document, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {
  getBillListPage,
  getPendingEncounterPage,
  listPendingFees,
  settleBill,
  settlePreview,
} from '@/api/settlementBill';
import {issueInvoice} from '@/api/invoice';
import QrPayDialog from '@/components/his/QrPayDialog.vue';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

/** 金额累加先转成分，避免浮点误差把「合计」和后端算出来的差出 0.01 */
const sumMoney = (rows, key = 'amount') => rows.reduce((acc, r) => acc + Math.round(Number(r?.[key] || 0) * 100), 0) / 100;
const money = (v) => formatMoney(v);
// ==================== 字典 ====================
const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];

/** 字典项给 el-select 用：dictValue 是字符串，页面里的码值一律按数字比较 */
function selectOptions(type) {
  return dictOptions(type).map((o) => ({value: Number(o.dictValue), label: o.dictLabel}));
}

const billStatusText = (v) => dictLabelText(dictOptions(DICT_TYPE.BILL_STATUS), v);
const billTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.BILL_TYPE), v);
const itemTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE), v);
const sourceTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.FEE_SOURCE_TYPE), v);
const feeStatusText = (v) => dictLabelText(dictOptions(DICT_TYPE.FEE_STATUS), v);
const feeStatusTag = (v) => ({1: '', 2: 'info', 3: 'success', 4: 'danger'}[Number(v)] ?? '');
// ==================== 待收费就诊榜 ====================
const boardLoading = ref(false);
const keyword = ref('');
const boardRows = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});

async function loadBoard() {
  boardLoading.value = true;
  try {
    const res = await getPendingEncounterPage({
      encounterType: 1,
      keyword: keyword.value || undefined,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    });
    boardRows.value = res.data?.records || [];
    pagination.value.total = Number(res.data?.total || 0);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    boardLoading.value = false;
  }
}

function handleSearch() {
  pagination.value.pageNum = 1;
  loadBoard();
}

function handleReset() {
  keyword.value = '';
  handleSearch();
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.BILL_STATUS,
    DICT_TYPE.BILL_TYPE,
    DICT_TYPE.CHARGE_ITEM_TYPE,
    DICT_TYPE.FEE_SOURCE_TYPE,
    DICT_TYPE.FEE_STATUS,
    DICT_TYPE.SETTLEMENT_MODE,
  ].join(','));
  await loadBoard();
});
const workbench = ref(false);
const workLoading = ref(false);
const current = ref(null);
const fees = ref([]);
const feeTotal = ref(0);
const unpaidBillAmount = ref(0);
const selectedFees = ref([]);
const bills = ref([]);
const settleForm = ref({settlementMode: 1, billType: null, discountAmount: 0});
const preview = ref(null);
const previewLoading = ref(false);
const settling = ref(false);
const selectedFeeIds = computed(() => selectedFees.value.map((f) => f.id));
const selectedTotal = computed(() => sumMoney(selectedFees.value));

async function openWorkbench(row) {
  current.value = row;
  workbench.value = true;
  settleForm.value = {settlementMode: 1, billType: null, discountAmount: 0};
  preview.value = null;
  selectedFees.value = [];
  await loadWorkbench();
}

async function loadWorkbench() {
  if (!current.value)
    return;
  workLoading.value = true;
  try {
    const [feeRes, billRes] = await Promise.all([
      listPendingFees(current.value.encounterType, current.value.encounterId),
      getBillListPage({
        encounterType: current.value.encounterType,
        encounterId: current.value.encounterId,
        pageNum: 1,
        pageSize: 50,
      }),
    ]);
    fees.value = feeRes.data?.fees || [];
    feeTotal.value = Number(feeRes.data?.totalAmount || 0);
    unpaidBillAmount.value = Number(feeRes.data?.unpaidBillAmount || 0);
    // 已作废的账单留在台账页看，收费台只关心还要处理的：待支付 / 部分支付 / 已支付（可退费出票）
    bills.value = (billRes.data?.records || []).filter((b) => Number(b.billStatus) !== 4);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    workLoading.value = false;
  }
}

/** 选中行、结算方式、优惠任一变化都要重算：试算的意义就是「现在出账会写成多少」 */
function invalidatePreview() {
  preview.value = null;
}

function onFeeSelectionChange(rows) {
  selectedFees.value = rows;
  invalidatePreview();
}

function buildSettleDTO() {
  return {
    encounterType: current.value.encounterType,
    encounterId: current.value.encounterId,
    feeIds: selectedFeeIds.value,
    billType: settleForm.value.billType || undefined,
    settlementMode: settleForm.value.settlementMode,
    discountAmount: Number(settleForm.value.discountAmount || 0),
  };
}

async function runPreview() {
  if (!selectedFees.value.length) {
    ElMessage.warning('请先勾选要结算的记账行');
    return;
  }
  previewLoading.value = true;
  try {
    const res = await settlePreview(buildSettleDTO());
    preview.value = res.data || null;
  } catch (error) {
    preview.value = null;
    
  } finally {
    previewLoading.value = false;
  }
}

async function runSettle() {
  if (!preview.value) {
    ElMessage.warning('请先试算，确认金额后再出账');
    return;
  }
  settling.value = true;
  try {
    const res = await settleBill(buildSettleDTO());
    const bill = res.data;
    ElMessage.success(`已出账单 ${bill?.billNo}，应缴 ¥${money(bill?.payableAmount)}`);
    selectedFees.value = [];
    preview.value = null;
    await loadWorkbench();
    await loadBoard();
    if (Number(bill?.payableAmount || 0) > 0) {
      await openPay(bill);
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    settling.value = false;
  }
}

// ==================== 收款（L3：与预约收费同一弹层，默认现金档） ====================
const qrPayDialog = ref(false);
const qrPayBill = ref(null);

function openPay(bill) {
  // 出账后直接拉起时，settle 响应可能不带患者名，兜底取当前就诊的，避免弹层闪「—」
  qrPayBill.value = {...bill, patientName: bill.patientName || current.value?.patientName};
  qrPayDialog.value = true;
}

/** 收款事实来自现金清点或渠道查单，本页不翻状态：入账成功只重新拉账单与就诊榜 */
async function onPaid() {
  ElMessage.success('收款已入账');
  await loadWorkbench();
  await loadBoard();
}

// ==================== 出票（L4：账单结清后打发票） ====================
async function runIssue(bill) {
  try {
    const res = await issueInvoice({billId: bill.id, invoiceType: 1});
    ElMessage.success(`已出票 ${res.data?.invoiceNo || ''}`);
    await loadWorkbench();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
  }
}

const billStatusTag = (status) => Number(status) === 3 ? 'success' : Number(status) === 2 ? 'warning' : Number(status) === 4 || Number(status) === 5 ? 'info' : '';
</script>
