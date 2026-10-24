<template>
  <div class="flex h-full flex-col gap-3">
    <!-- ============== 顶栏：病区 + 搜索 ============== -->
    <div class="flex shrink-0 items-center gap-3">
      <span class="text-sm font-medium text-slate-700">床旁结算</span>
      <span class="text-xs text-slate-400">在院患者 · 按床号排序 · 数据范围按当前岗位科室收口</span>
      <div class="ml-auto flex items-center gap-2">
        <el-input v-model="keyword" clearable placeholder="搜患者姓名/患者号" style="width: 200px"
                  @keyup.enter="loadBoard" @clear="loadBoard"/>
        <el-button :icon="Search" @click="loadBoard">查询</el-button>
        <el-button :icon="Refresh" data-testid="bedside-refresh" @click="loadBoard">刷新</el-button>
      </div>
    </div>

    <div class="flex min-h-0 flex-1 gap-3">
      <!-- ============== 左：病区患者榜 ============== -->
      <div class="flex w-[460px] shrink-0 flex-col rounded-lg border border-slate-200">
        <div class="border-b border-slate-100 px-3 py-2 text-xs font-medium text-slate-500">
          本病区在院患者（{{ boardTotal }}）
        </div>
        <el-table v-loading="boardLoading" :data="boardRows" class="bedside-board" data-testid="bedside-board"
                  highlight-current-row row-key="admissionId" size="small" style="flex: 1"
                  @current-change="onRowChange">
          <el-table-column label="床号" prop="bedNo" width="64"/>
          <el-table-column label="患者" min-width="110">
            <template #default="{ row }">
              <span>{{ row.patientName }}</span>
              <span class="ml-1 text-xs text-slate-400">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '' }}{{ row.age ? ` ${row.age}岁` : '' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="预交金余额" prop="prepayBalance" width="104">
            <template #default="{ row }">{{ money(row.prepayBalance) }}</template>
          </el-table-column>
          <el-table-column align="right" label="累计费用" prop="chargedAmount" width="96">
            <template #default="{ row }">{{ money(row.chargedAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="欠费" width="88">
            <template #default="{ row }">
              <b :class="Number(row.arrearsAmount) > 0 ? 'text-red-600' : 'text-slate-400'">{{
                  money(row.arrearsAmount)
                }}</b>
            </template>
          </el-table-column>
          <el-table-column label="结算" width="76">
            <template #default="{ row }">
              <el-tag v-if="row.settled && Number(row.billUnpaid) > 0" size="small" type="warning">未付清</el-tag>
              <el-tag v-else-if="row.settled" size="small" type="success">已付清</el-tag>
              <el-tag v-else size="small" type="info">未结算</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="flex shrink-0 justify-end border-t border-slate-100 px-2 py-1">
          <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="boardTotal"
                         background layout="prev, pager, next" small
                         @current-change="loadBoard"/>
        </div>
      </div>

      <!-- ============== 右：工作区 ============== -->
      <div class="min-w-0 flex-1 overflow-y-auto rounded-lg border border-slate-200">
        <!-- 空态 -->
        <div v-if="!current" class="flex h-full items-center justify-center text-sm text-slate-400"
             data-testid="bedside-empty">
          从左侧选择一位患者，办理床旁出院结算
        </div>

        <template v-else>
          <!-- 患者头 + 钱况卡 -->
          <div class="border-b border-slate-100 px-4 py-3">
            <div class="flex items-center gap-2">
              <span class="text-base font-semibold text-slate-800">{{ current.patientName }}</span>
              <el-tag size="small">{{ current.bedNo || '无床' }}床</el-tag>
              <span class="text-xs text-slate-400">{{ current.admissionNo }} · {{ current.deptName }}</span>
            </div>
            <div class="mt-1 text-xs text-slate-500">诊断：{{ current.diagnosis || '—' }} · 入院：{{
                (current.admitTime || '').replace('T', ' ').slice(0, 16)
              }}
            </div>
            <div class="mt-3 grid grid-cols-4 gap-2">
              <div class="rounded bg-slate-50 px-3 py-2" data-testid="bedside-card-prepay">
                <div class="text-xs text-slate-500">预交金余额</div>
                <div class="font-mono text-lg font-semibold text-slate-700">{{ money(current.prepayBalance) }}</div>
              </div>
              <div class="rounded bg-slate-50 px-3 py-2" data-testid="bedside-card-charged">
                <div class="text-xs text-slate-500">累计费用</div>
                <div class="font-mono text-lg font-semibold text-slate-700">{{ money(current.chargedAmount) }}</div>
              </div>
              <div class="rounded bg-slate-50 px-3 py-2" data-testid="bedside-card-arrears">
                <div class="text-xs text-slate-500">欠费</div>
                <div class="font-mono text-lg font-semibold"
                     :class="Number(current.arrearsAmount) > 0 ? 'text-red-600' : 'text-slate-700'">{{
                    money(current.arrearsAmount)
                  }}
                </div>
              </div>
              <div class="rounded bg-slate-50 px-3 py-2" data-testid="bedside-card-unpaid">
                <div class="text-xs text-slate-500">账单未付清</div>
                <div class="font-mono text-lg font-semibold"
                     :class="Number(current.billUnpaid) > 0 ? 'text-red-600' : 'text-slate-700'">{{
                    money(current.billUnpaid)
                  }}
                </div>
              </div>
            </div>
          </div>

          <!-- 操作条 -->
          <div class="flex flex-wrap items-center gap-2 px-4 py-3">
            <template v-if="!current.settled">
              <!-- 结算方式只能由收窗口的人选：后端拿参保号也判不出「这次要不要走医保」（与住院结算页同契约） -->
              <el-select v-model="settleMode" class="!w-28" data-testid="bedside-settle-mode">
                <el-option :value="1" label="自费"/>
                <el-option :value="2" label="医保"/>
              </el-select>
              <el-button :icon="DataAnalysis" :loading="previewLoading" data-testid="bedside-preview-btn"
                         type="primary" @click="runPreview">结算试算
              </el-button>
              <el-button :disabled="!preview" :icon="Money" :loading="settleLoading"
                         data-testid="bedside-settle-btn" @click="runSettle">办理出院结算
              </el-button>
            </template>
            <template v-else>
              <el-button v-if="Number(current.billUnpaid) > 0" :icon="Iphone" :loading="qrCreating"
                         data-testid="bedside-qr-btn" type="primary" @click="showQr">亮聚合码收款
              </el-button>
              <el-button :icon="Document" data-testid="bedside-detail-btn" @click="loadSettlementDetail">
                查看结算单
              </el-button>
            </template>
            <span class="text-xs text-slate-400">一次住院一张有效结算单；欠费也留单，付清前不放行出院</span>
          </div>

          <!-- 试算面板 -->
          <div v-if="preview" class="space-y-3 px-4 pb-4">
            <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700"
                 data-testid="bedside-conclusion">
              {{ preview.conclusionText }}
            </div>
            <el-descriptions :column="4" border size="small">
              <el-descriptions-item label="费用行数">{{ preview.feeCount }}</el-descriptions-item>
              <el-descriptions-item label="总费用">{{ money(preview.totalAmount) }}</el-descriptions-item>
              <el-descriptions-item label="优惠">{{ money(preview.discountAmount) }}</el-descriptions-item>
              <el-descriptions-item label="统筹">{{ money(preview.poolAmount) }}</el-descriptions-item>
              <el-descriptions-item label="个账">{{ money(preview.accountAmount) }}</el-descriptions-item>
              <el-descriptions-item label="自付">{{ money(preview.selfAmount) }}</el-descriptions-item>
              <el-descriptions-item label="应缴">{{ money(preview.payableAmount) }}</el-descriptions-item>
              <el-descriptions-item label="账户余额">{{ money(preview.prepayBalance) }}</el-descriptions-item>
              <el-descriptions-item label="本次抵扣">{{ money(preview.balanceUsed) }}</el-descriptions-item>
              <el-descriptions-item label="应退">{{ money(preview.refundAmount) }}</el-descriptions-item>
              <el-descriptions-item label="欠费">
                <b :class="Number(preview.arrearsAmount) > 0 ? 'text-red-600' : ''">{{
                    money(preview.arrearsAmount)
                  }}</b>
              </el-descriptions-item>
              <el-descriptions-item label="结算方式">{{ preview.settleModeText }}</el-descriptions-item>
            </el-descriptions>
            <el-table :data="preview.feeRows || []" data-testid="bedside-fee-rows" max-height="220" size="small">
              <el-table-column label="记账单号" prop="feeNo" width="170"/>
              <el-table-column label="类别" prop="itemTypeName" width="90"/>
              <el-table-column label="项目" min-width="140" prop="itemName" show-overflow-tooltip/>
              <el-table-column label="规格" min-width="100" prop="specification" show-overflow-tooltip/>
              <el-table-column align="right" label="单价" prop="price" width="84">
                <template #default="{ row }">{{ money(row.price) }}</template>
              </el-table-column>
              <el-table-column align="right" label="数量" prop="quantity" width="64"/>
              <el-table-column align="right" label="金额" prop="amount" width="92">
                <template #default="{ row }">{{ money(row.amount) }}</template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 结算单面板 -->
          <div v-if="settlement" class="mx-4 mb-4 rounded border border-green-200 bg-green-50 p-4 text-sm"
               data-testid="bedside-settlement">
            <div class="mb-2 font-medium text-green-800">
              结算单 {{ settlement.settlementNo }}（{{ settlement.settleStatusText }}）
            </div>
            <div class="grid grid-cols-2 gap-y-1 text-slate-700">
              <div>应缴：<b>{{ money(settlement.payableAmount) }}</b></div>
              <div>已收：<b>{{ money(settlement.paidAmount) }}</b>（账户抵扣 {{ money(settlement.balanceUsed) }}）</div>
              <div>应退：<b>{{ money(settlement.refundAmount) }}</b></div>
              <div>欠费：<b :class="Number(settlement.arrearsAmount) > 0 ? 'text-red-600' : ''">{{
                  money(settlement.arrearsAmount)
                }}</b></div>
            </div>
          </div>

          <!-- 收款码面板（扫码档：床旁不代收现金，患者自己扫） -->
          <div v-if="qrVisible" class="mx-4 mb-4 rounded border border-slate-200 p-4" data-testid="bedside-qr-panel">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-700">聚合收款码</span>
              <span class="text-xs text-slate-400">微信 / 支付宝 / 云闪付均可扫</span>
            </div>
            <div class="mt-3 flex flex-col items-center">
              <div v-if="qrPaid" class="flex flex-col items-center gap-1 py-6" data-testid="bedside-qr-paid">
                <el-icon class="text-emerald-500" size="52">
                  <CircleCheckFilled/>
                </el-icon>
                <p class="text-lg font-semibold text-emerald-600">支付成功</p>
                <p class="text-sm text-slate-500">{{ money(qrAmount) }} 已入账</p>
              </div>
              <template v-else>
                <img v-if="qrImage" :class="{ 'opacity-40': qrExpired }" :src="qrImage" alt="聚合收款码"
                     class="h-[220px] w-[220px]" data-testid="bedside-qr-image"/>
                <p v-if="qrExpired" class="mt-2 text-sm text-red-600">二维码已过期，请关闭后重新亮码</p>
                <p v-else class="mt-2 text-xs text-slate-400">
                  请患者用手机扫码支付 · 收款码 {{ countdownText }} 后过期 · 已自动确认支付结果{{
                    pollError ? `（查询失败：${pollError}）` : ''
                  }}
                </p>
              </template>
            </div>
            <div class="mt-3 flex justify-end gap-2">
              <el-button v-if="!qrPaid" data-testid="bedside-qr-close" @click="cancelQr">取消收款（关码）</el-button>
              <el-button v-else data-testid="bedside-qr-done" type="success" @click="finishPaid">完成</el-button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 床旁结算工作区（护士站第 5 个工作区）。
 *
 * 病区护士拿平板/推车在床旁给分管患者办出院结算：试算 → 出账 → 亮聚合码 → 患者自己扫
 * （微信/支付宝/云闪付）→ 轮询到账。患者不用跑收费窗口，护士也不用离开病区。
 *
 * 三条口径（与收费台一致，前端不自算）：
 * 1. 金额全部服务端现算：试算/出账/轮询返回什么显示什么，页面不做加减；
 * 2. 收款事实来自渠道查单（轮询兜底入账），页面没有「确认已支付」按钮 ——
 *    前端点一下就把账单置平，等于让没收到钱的账先平了；
 * 3. 数据范围按当前岗位科室服务端收口，本页不传任何科室参数（URS-COM-09）。
 *
 * 为什么内嵌收款面板而不是复用收费台的 QrPayDialog：床旁是扫码单档
 * （不代收现金、不需要语音播报、费用明细试算时已展示），且打的是
 * /charge/bedside/* 带病区门禁的接口，与收费台 /charge/qrPay/* 不是一套权限。
 */
import {computed, onUnmounted, ref} from 'vue';
import {CircleCheckFilled, DataAnalysis, Document, Iphone, Money, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  bedsideCloseQr,
  bedsideCreateQr,
  bedsideQueryQr,
  bedsideSettle,
  bedsideSettlePreview,
  bedsideSettlementDetail,
  getBedsideWardBoard,
} from '@/api/bedside';
import {moneyText as money} from '@/lib/patientField';

const PAY_STATUS_PAID = 1;
const PAY_STATUS_CLOSED = 2;
/** 轮询间隔与后端「同单查单节流」同宽 */
const POLL_MS = 5000;
const TICK_MS = 1000;

const keyword = ref('');
const pageNum = ref(1);
const pageSize = ref(20);
const settleMode = ref(1); // 结算方式：床旁就是收窗口，显式选，不靠服务端猜
const boardRows = ref([]);
const boardTotal = ref(0);
const boardLoading = ref(false);
const current = ref(null);

const preview = ref(null);
const previewLoading = ref(false);
const settleLoading = ref(false);
const settlement = ref(null);

const qrVisible = ref(false);
const qrCreating = ref(false);
const payNo = ref('');
const qrImage = ref('');
const qrAmount = ref(0);
const qrPaid = ref(false);
const qrClosed = ref(false);
const countdownDeadline = ref(0);
const nowTick = ref(Date.now());
const pollError = ref('');

let pollTimer = null;
let tickTimer = null;

const qrExpired = computed(() => !qrPaid.value && !!qrImage.value
    && (qrClosed.value || (countdownDeadline.value > 0 && countdownDeadline.value - nowTick.value <= 0)));
const countdownText = computed(() => {
  const sec = Math.max(0, Math.ceil((countdownDeadline.value - nowTick.value) / 1000));
  return `${String(Math.floor(sec / 60)).padStart(2, '0')}:${String(sec % 60).padStart(2, '0')}`;
});

onUnmounted(() => {
  stopPoll();
  stopTick();
});

async function loadBoard() {
  boardLoading.value = true;
  try {
    const res = await getBedsideWardBoard({pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined});
    boardRows.value = res.data?.records || [];
    boardTotal.value = Number(res.data?.total || 0);
    syncCurrentFromBoard();
  } catch (e) {
    ElMessage.error(e.message || '病区患者加载失败');
  } finally {
    boardLoading.value = false;
  }
}

/**
 * 把左侧刷新后的榜行同步回右侧患者头：出账、到账后钱况都变了，
 * 不同步的话头部还显示旧的「欠费 500」——护士会以为钱没到。
 * 同步的是服务端现算的整行，不是前端自己减出来的数（口径不变：前端不自算）。
 */
function syncCurrentFromBoard() {
  const admissionId = current.value?.admissionId;
  if (!admissionId) {
    return;
  }
  const fresh = boardRows.value.find((r) => String(r.admissionId) === String(admissionId));
  if (fresh) {
    current.value = {...fresh};
  }
}

/** 换患者：清空上一患者的所有面板状态，按新患者的出账状态回显 */
function onRowChange(row) {
  // 同一患者的「重发」不是用户操作：loadBoard 刷新后 el-table 凭 row-key 保住选中行会再发一次
  // current-change（行对象已换新）。若跟真换患者一样 resetQr + stopPoll，到账那一刻面板会被擦掉。
  if (row && current.value && String(row.admissionId) === String(current.value.admissionId)) {
    return;
  }
  stopPoll();
  stopTick();
  current.value = row || null;
  preview.value = null;
  settlement.value = null;
  resetQr();
  if (row?.settled) {
    loadSettlementDetail();
  }
}

async function loadSettlementDetail() {
  if (!current.value) {
    return;
  }
  try {
    const res = await bedsideSettlementDetail({admissionId: current.value.admissionId});
    settlement.value = res.data || null;
  } catch (e) {
    ElMessage.error(e.message || '结算单加载失败');
  }
}

async function runPreview() {
  if (!current.value) {
    return;
  }
  previewLoading.value = true;
  try {
    // settleMode 由操作条显式选择随试算一起提交：服务端不按参保号静默推断
    const res = await bedsideSettlePreview({admissionId: current.value.admissionId, settleMode: settleMode.value});
    preview.value = res.data || null;
  } catch (e) {
    ElMessage.error(e.message || '试算失败');
  } finally {
    previewLoading.value = false;
  }
}

async function runSettle() {
  if (!current.value || !preview.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(
        `确认办理出院结算？应缴 ${money(preview.value.payableAmount)} 元，一次住院一张有效结算单。`,
        '办理出院结算', {type: 'warning', confirmButtonText: '确认结算', cancelButtonText: '取消'},
    );
  } catch {
    return; // 用户取消
  }
  settleLoading.value = true;
  try {
    const res = await bedsideSettle({
      admissionId: current.value.admissionId,
      // 结算方式取试算返回的实际生效口径：选了医保但平台查无参保/停保会降级自费，
      // 回传试算结果而不是用户原始选择，避免二次判定漂移
      settleMode: preview.value.settleMode,
    });
    settlement.value = res.data?.settlement || null;
    const billId = res.data?.billId;
    if (billId) {
      current.value = {...current.value, settled: true, billId, billNo: res.data?.billNo, billUnpaid: res.data?.unpaidAmount};
    }
    preview.value = null;
    ElMessage.success('结算单已生成');
    loadBoard();
    // 出账即欠费为 0（余额全抵/退差）时没必要亮码
    if (Number(res.data?.unpaidAmount) > 0 && billId) {
      showQr(billId, res.data?.unpaidAmount);
    }
  } catch (e) {
    ElMessage.error(e.message || '结算失败');
  } finally {
    settleLoading.value = false;
  }
}

// ---------- 收款码 ----------

async function showQr(billId, amount) {
  const targetBillId = billId || current.value?.billId;
  if (!targetBillId) {
    ElMessage.error('缺少账单，无法出码');
    return;
  }
  qrCreating.value = true;
  resetQr();
  qrVisible.value = true;
  try {
    const res = await bedsideCreateQr({billId: targetBillId});
    const data = res.data || {};
    if (!data.qrImage) {
      ElMessage.error('没有返回收款码，请重试');
      return;
    }
    payNo.value = data.payNo || '';
    qrImage.value = data.qrImage;
    qrAmount.value = data.amount ?? amount ?? 0;
    qrPaid.value = Number(data.payStatus) === PAY_STATUS_PAID;
    anchorCountdown(data.expireTime, data.serverTime);
    if (!qrPaid.value) {
      startPoll();
      startTick();
    }
  } catch (e) {
    ElMessage.error(e.message || '出码失败');
  } finally {
    qrCreating.value = false;
  }
}

/** 倒计时锚定：服务端给「过期时刻 + 当前时刻」，相减得服务端口径剩余时长，落到本地时钟 */
function anchorCountdown(expireTime, serverTime) {
  const expire = parseServerTime(expireTime);
  if (!expire) {
    return;
  }
  const server = parseServerTime(serverTime);
  countdownDeadline.value = Date.now() + Math.max(0, expire - (server || Date.now()));
}

function parseServerTime(text) {
  if (!text) {
    return 0;
  }
  const ms = Date.parse(String(text).replace(' ', 'T'));
  return Number.isNaN(ms) ? 0 : ms;
}

function startTick() {
  if (!tickTimer) {
    tickTimer = setInterval(() => {
      nowTick.value = Date.now();
    }, TICK_MS);
  }
}

function stopTick() {
  if (tickTimer) {
    clearInterval(tickTimer);
    tickTimer = null;
  }
}

async function pollOnce() {
  if (qrPaid.value || !payNo.value) {
    return;
  }
  try {
    const res = await bedsideQueryQr({payNo: payNo.value});
    const data = res.data || {};
    pollError.value = '';
    anchorCountdown(data.expireTime, data.serverTime);
    nowTick.value = Date.now();
    if (Number(data.payStatus) === PAY_STATUS_PAID) {
      qrPaid.value = true;
      stopPoll();
      stopTick();
      loadBoard();
      // 到账后结算单面板也要跟着翻（否则绿色面板还显示着到账前的「欠费 500」）
      loadSettlementDetail();
      return;
    }
    if (Number(data.payStatus) === PAY_STATUS_CLOSED) {
      qrClosed.value = true;
      stopPoll();
    }
  } catch (e) {
    // 查询失败不打断轮询：渠道那边钱可能已经到账
    pollError.value = e.message || '查询失败';
  }
}

function startPoll() {
  stopPoll();
  pollTimer = setInterval(pollOnce, POLL_MS);
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

/** 取消收款：关掉该账单下全部待支付码（患者不付了/改去收费台） */
async function cancelQr() {
  stopPoll();
  stopTick();
  const billId = current.value?.billId;
  if (billId) {
    try {
      await bedsideCloseQr({billId});
    } catch (e) {
      // 关不掉也先收起面板，但如实告知：码可能还活着
      ElMessage.error(e.message || '收款码未能关闭，请 reopen 重试或联系信息科');
      return;
    }
  }
  qrVisible.value = false;
  resetQr();
}

function finishPaid() {
  qrVisible.value = false;
  resetQr();
  if (current.value?.admissionId) {
    loadSettlementDetail();
  }
}

function resetQr() {
  payNo.value = '';
  qrImage.value = '';
  qrAmount.value = 0;
  qrPaid.value = false;
  qrClosed.value = false;
  countdownDeadline.value = 0;
  pollError.value = '';
}

loadBoard();
</script>

<style scoped>
/* 表格撑满左列高度、内部滚动（工作站类页面口径） */
.bedside-board :deep(.el-table__inner-wrapper) {
  height: 100%;
}
</style>
