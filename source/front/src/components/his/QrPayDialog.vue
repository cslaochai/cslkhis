<template>
  <el-dialog v-model="visible" destroy-on-close :title="dialogTitle" width="460px" @closed="onClosed">
    <div class="space-y-3">
      <div class="rounded-lg bg-slate-50 px-3 py-2 text-sm">
        <div class="flex justify-between">
          <span class="text-slate-500">账单号</span>
          <span class="font-mono">{{ billNo || '—' }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">患者</span>
          <span>{{ patientName || '—' }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">本单金额</span>
          <b class="text-red-600">{{ moneyText(amount) }}</b>
        </div>
      </div>

      <div v-loading="detailLoading" class="rounded-lg border border-slate-200">
        <div class="border-b border-slate-100 px-3 py-1.5 text-xs font-medium text-slate-500">费用明细</div>
        <el-table :data="feeItems" data-testid="qrpay-fee-table" max-height="176" size="small">
          <el-table-column label="项目" min-width="158" prop="itemName" show-overflow-tooltip/>
          <el-table-column align="right" label="单价" prop="price" width="86">
            <template #default="{ row }">{{ moneyText(row.price) }}</template>
          </el-table-column>
          <el-table-column align="right" label="数量" prop="quantity" width="56"/>
          <el-table-column align="right" label="金额" prop="amount" width="92">
            <template #default="{ row }">{{ moneyText(row.amount) }}</template>
          </el-table-column>
        </el-table>
        <div class="flex flex-wrap gap-x-4 gap-y-1 border-t border-slate-100 px-3 py-1.5 text-xs text-slate-500">
          <span>应收 <b class="font-mono text-slate-700">{{ moneyText(summary.totalAmount) }}</b></span>
          <span v-if="Number(summary.discountAmount) > 0">优惠 <b class="font-mono text-slate-700">-{{ moneyText(summary.discountAmount) }}</b></span>
          <span v-if="Number(summary.poolAmount) > 0">统筹报销 <b class="font-mono text-emerald-600">-{{ moneyText(summary.poolAmount) }}</b></span>
          <span v-if="Number(summary.accountAmount) > 0">个账 <b class="font-mono text-emerald-600">-{{ moneyText(summary.accountAmount) }}</b></span>
          <span>应缴 <b class="font-mono text-slate-700">{{ moneyText(summary.payableAmount) }}</b></span>
          <span>尚欠 <b class="font-mono text-red-600">{{ moneyText(summary.unpaidAmount) }}</b></span>
        </div>
      </div>

      <el-radio-group v-model="method" :disabled="creating || isPaid || cashSubmitting" @change="onMethodChange">
        <el-radio v-for="item in methodOptions" :key="item.code" :value="item.code">{{ item.label }}</el-radio>
      </el-radio-group>

      <!-- 现金：窗口当场收钱记流水，没有「等患者扫」这一段，所以码区/倒计时/轮询全不出现 -->
      <div v-if="isCash" class="min-h-[236px] rounded-lg border border-slate-200 p-3 text-sm">
        <div class="flex justify-between py-1">
          <span class="text-slate-500">收款方式</span>
          <span>现金</span>
        </div>
        <div class="flex justify-between py-1">
          <span class="text-slate-500">收款金额</span>
          <b class="text-red-600">{{ moneyText(cashAmount) }}</b>
        </div>
        <p v-if="error" class="mt-2 text-center text-sm text-red-600">{{ error }}</p>
      </div>
      <div v-else class="flex min-h-[236px] flex-col items-center justify-center rounded-lg border border-slate-200 p-3">
        <el-skeleton v-if="creating" :rows="3" animated class="w-full"/>
        <div v-else-if="qrImage && !isPaid" class="relative">
          <img :alt="`${channelLabel}收款码`" :class="{ 'opacity-40 blur-[3px]': qrExpired }" :src="qrImage"
               class="h-[220px] w-[220px]"/>
          <div v-if="qrExpired" class="absolute inset-0 flex flex-col items-center justify-center gap-2">
            <p class="text-sm font-medium text-slate-600">二维码已过期</p>
            <el-button :loading="creating" data-testid="qrpay-refresh" size="small" type="primary" @click="generate">
              刷新收款码
            </el-button>
          </div>
          <el-tooltip v-else content="刷新收款码" placement="top">
            <button :disabled="creating"
                    class="absolute bottom-0.5 right-0.5 flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-500 shadow-sm hover:text-[#1269B5] disabled:cursor-not-allowed disabled:opacity-40"
                    data-testid="qrpay-refresh-corner" type="button" @click="generate">
              <el-icon>
                <Refresh/>
              </el-icon>
            </button>
          </el-tooltip>
        </div>
        <p v-else-if="isPaid" class="text-center text-base font-medium text-emerald-600">
          支付成功<br>
          <span class="text-sm font-normal text-slate-500">{{ moneyText(amount) }} 已入账，可出票</span>
        </p>
        <p v-else class="px-2 text-center text-sm text-red-600">{{ error || '还没有生成收款码' }}</p>
      </div>

      <p v-if="!isCash" class="text-center text-xs text-slate-400">
        <template v-if="isPaid">收款流水已由后端按渠道查单确认，页面不代替它翻状态</template>
        <template v-else-if="qrImage && qrExpired">二维码已过期，点击「刷新收款码」重新出码</template>
        <template v-else-if="qrImage">
          请患者用{{ channelLabel }}扫此码 · 收款码 {{ countdownText }} 后过期 ·
          已自动确认支付结果{{ pollError ? `（最近一次查询失败：${pollError}）` : '' }}
        </template>
      </p>
    </div>

    <template #footer>
      <div class="flex justify-end space-x-2">
        <template v-if="isCash">
          <el-button :loading="cashSubmitting" data-testid="qrpay-cash-confirm" type="primary"
                     @click="confirmCash">确认收到现金
          </el-button>
          <el-button @click="visible = false">取消</el-button>
        </template>
        <template v-else>
          <el-button :disabled="!isPaid" :type="isPaid ? 'success' : 'info'" @click="visible = false">
            {{ isPaid ? '完成' : (qrExpired ? '已过期' : '等待患者支付') }}
          </el-button>
          <el-button v-if="!isPaid" @click="visible = false">取消</el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onUnmounted, ref, watch} from 'vue';
import {Refresh} from '@element-plus/icons-vue';
import {
  closeQrPay,
  createQrPay,
  getBillDetailById,
  payBill,
  queryQrPayResult,
} from '@/api/settlementBill';
import {moneyText} from '@/lib/patientField';

/**
 * 收款弹层：一笔账单收一次，方式在弹层里选。
 *
 * 界面不出现「确认已支付」那类按钮 —— 扫码那两档的收款事实来自渠道（异步通知 + 查单兜底），
 * 前端点一下就把账单置成已支付，等于让没收到钱的账先平了。
 * 现金是唯一「本地自己决定」的一档：收银员当场点钞收到，按下确认即记一笔现金流水，
 * 金额一律取账单尚欠（不给输入框），账实是否相符交给交班清点核对。
 *
 * 过期时间是服务端现算的（支付单创建时间 + 有效期），前端只负责按它做倒计时；
 * 倒计时走完后由服务端在轮询里把单关掉，刷新按钮出的是全新一张码。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  billId: {type: [String, Number], default: null},
  billNo: {type: String, default: ''},
  patientName: {type: String, default: ''},
  amount: {type: [String, Number], default: 0},
  /** 打开即给现金档（挂号窗口默认收现金）；收费台按「先扫码」的旧口径保持关闭 */
  allowCash: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'paid']);

const PAY_STATUS_PAID = 1;
const PAY_STATUS_PENDING = 0;
const PAY_STATUS_CLOSED = 2;
/** 渠道码值（PayChannelEnum）：1-微信 2-支付宝。支付宝已接通，微信缺商户证书，出码会明确报错 */
const CHANNEL_OPTIONS = [{code: 2, label: '支付宝'}, {code: 1, label: '微信'}];
/** 现金档的哨兵值（不是渠道码，也不是落库码值），只在弹层内部做分支 */
const CASH_METHOD = 'CASH';
/** 支付方式码值 1-现金（PaymentMethodEnum.CASH） */
const PAY_METHOD_CASH = 1;
/** 轮询间隔与后端「同单查单节流」同宽：比它更快只会撞上被节流的那一轮，白多一次请求 */
const POLL_MS = 5000;
const TICK_MS = 1000;

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const channel = ref(CHANNEL_OPTIONS[0].code);
/** 当前收款档：CASH 或渠道码值；method 与 channel 同步，渠道侧调用一律读 channel */
const method = ref(CHANNEL_OPTIONS[0].code);
const cashSubmitting = ref(false);
const payNo = ref('');
const qrImage = ref('');
const payStatus = ref(null);
const creating = ref(false);
const error = ref('');
const pollError = ref('');
const billDetail = ref(null);
const detailLoading = ref(false);
/** 过期时刻在本地时钟上的落点（由服务端口径的剩余时长锚定） */
const countdownDeadline = ref(0);
const nowTick = ref(Date.now());
let timer = null;
let tickTimer = null;

const isPaid = computed(() => Number(payStatus.value) === PAY_STATUS_PAID);
const isClosed = computed(() => Number(payStatus.value) === PAY_STATUS_CLOSED);
const isCash = computed(() => method.value === CASH_METHOD);
const methodOptions = computed(() => (props.allowCash
    ? [{code: CASH_METHOD, label: '现金'}, ...CHANNEL_OPTIONS]
    : CHANNEL_OPTIONS));
const dialogTitle = computed(() => (props.allowCash ? '收费' : '扫码收款'));
/** 现金只收账单尚欠：应收/已收都由服务端现算，页面不自减（尚欠没回来时退回父页面传入的本单金额） */
const cashAmount = computed(() => Number(summary.value.unpaidAmount ?? props.amount) || 0);
const channelLabel = computed(() => CHANNEL_OPTIONS.find((i) => i.code === channel.value)?.label || '');
const feeItems = computed(() => billDetail.value?.items || []);
const summary = computed(() => billDetail.value || {});
const remainMs = computed(() => Math.max(0, countdownDeadline.value - nowTick.value));
const qrExpired = computed(() => !isPaid.value && !!qrImage.value
    && (isClosed.value || (countdownDeadline.value > 0 && remainMs.value <= 0)));
const countdownText = computed(() => {
  const sec = Math.ceil(remainMs.value / 1000);
  return `${String(Math.floor(sec / 60)).padStart(2, '0')}:${String(sec % 60).padStart(2, '0')}`;
});

watch(visible, (v) => {
  if (!v) {
    return;
  }
  method.value = props.allowCash ? CASH_METHOD : CHANNEL_OPTIONS[0].code;
  loadBillDetail();
  if (!isCash.value) {
    generate();
  }
});

onUnmounted(() => {
  stopPoll();
  stopTick();
});

async function loadBillDetail() {
  billDetail.value = null;
  if (!props.billId) {
    return;
  }
  detailLoading.value = true;
  try {
    const res = await getBillDetailById(props.billId);
    billDetail.value = res.data || null;
  } catch {
    // 明细只给收银员核对用，取不到不阻断收款：金额与收款事实都由服务端按账单现算
  } finally {
    detailLoading.value = false;
  }
}

/**
 * 倒计时锚定：服务端给「过期时刻 + 当前时刻」，两端相减得服务端口径的剩余时长，再落到本地时钟上。
 * 直接减本地时钟会把客户端时钟漂移算进去，偏快的机器刚出的码就显示「已过期」。
 */
function anchorCountdown(expireTime, serverTime) {
  const expire = parseServerTime(expireTime);
  if (!expire) {
    return;
  }
  const server = parseServerTime(serverTime);
  const remain = expire - (server || Date.now());
  countdownDeadline.value = Date.now() + Math.max(0, remain);
}

/** 后端时间是「yyyy-MM-dd HH:mm:ss」本地时间；替成 T 分隔走标准解析，别依赖浏览器的宽松解析 */
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

async function generate() {
  stopPoll();
  if (!props.billId) {
    error.value = '缺少账单，无法出码';
    return;
  }
  creating.value = true;
  error.value = '';
  pollError.value = '';
  qrImage.value = '';
  payStatus.value = null;
  payNo.value = '';
  countdownDeadline.value = 0;
  try {
    const res = await createQrPay({billId: props.billId, channel: channel.value});
    const data = res.data || {};
    payNo.value = data.payNo || '';
    qrImage.value = data.qrImage || '';
    payStatus.value = data.payStatus ?? null;
    anchorCountdown(data.expireTime, data.serverTime);
    if (!qrImage.value) {
      error.value = '渠道没有返回收款码，请刷新重试';
      return;
    }
    startTick();
    startPoll();
  } catch (e) {
    error.value = e.message || '出码失败';
  } finally {
    creating.value = false;
  }
}

/**
 * 档位切换：现金与码不能并存 —— 收了现金还留着一张能扫的码，患者扫完就是同一笔账收两次，
 * 所以切走时必须关掉待支付的支付单（与关掉弹框同一口径）。
 */
async function onMethodChange(val) {
  if (val === CASH_METHOD) {
    stopPoll();
    stopTick();
    if (payNo.value && Number(payStatus.value) === PAY_STATUS_PENDING) {
      closeQrPay({payNo: payNo.value}).catch(() => {});
    }
    payNo.value = '';
    qrImage.value = '';
    payStatus.value = null;
    error.value = '';
    return;
  }
  channel.value = val;
  error.value = '';
  await generate();
}

/** 现金入账：记一笔现金流水，账单是否付清由后端按流水现算 */
async function confirmCash() {
  if (!props.billId) {
    error.value = '缺少账单，无法收款';
    return;
  }
  if (cashAmount.value <= 0) {
    error.value = '该账单没有应缴金额';
    return;
  }
  cashSubmitting.value = true;
  error.value = '';
  try {
    await payBill({billId: props.billId, items: [{payMethod: PAY_METHOD_CASH, amount: cashAmount.value}]});
    emit('paid', {payMethod: PAY_METHOD_CASH, amount: cashAmount.value});
    visible.value = false;
  } catch (e) {
    error.value = e.message || '现金收款失败';
  } finally {
    cashSubmitting.value = false;
  }
}

async function pollOnce() {
  if (!payNo.value || isPaid.value) {
    return;
  }
  try {
    const res = await queryQrPayResult({payNo: payNo.value});
    const data = res.data || {};
    payStatus.value = data.payStatus ?? payStatus.value;
    anchorCountdown(data.expireTime, data.serverTime);
    nowTick.value = Date.now();
    pollError.value = '';
    if (Number(data.payStatus) === PAY_STATUS_PAID) {
      stopPoll();
      emit('paid', data);
      // 支付成功后重取明细：费用条上的「尚欠」要跟着收到钱归零，不能停在收款前的快照
      loadBillDetail();
      return;
    }
    // 服务端已把过期未付的单关掉：停轮询，界面切「已过期 + 刷新」。
    // 钱若在临界点付进来，上面的已支付分支会先拦到，不会被这里吞掉
    if (Number(data.payStatus) === PAY_STATUS_CLOSED) {
      stopPoll();
    }
  } catch (e) {
    // 查询失败不打断轮询：公网穿透抖动时渠道那边钱可能已经到账
    pollError.value = e.message || '查询失败';
  }
}

function startPoll() {
  stopPoll();
  timer = setInterval(pollOnce, POLL_MS);
}

function stopPoll() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

function onClosed() {
  stopPoll();
  stopTick();
  // 未付款的码必须关掉：留着它就是一张「本地已作废、渠道还能扫出来付款」的活码
  if (payNo.value && Number(payStatus.value) === PAY_STATUS_PENDING) {
    closeQrPay({payNo: payNo.value}).catch(() => {});
  }
  method.value = props.allowCash ? CASH_METHOD : CHANNEL_OPTIONS[0].code;
  cashSubmitting.value = false;
  payNo.value = '';
  qrImage.value = '';
  payStatus.value = null;
  error.value = '';
  pollError.value = '';
  billDetail.value = null;
  countdownDeadline.value = 0;
}
</script>
