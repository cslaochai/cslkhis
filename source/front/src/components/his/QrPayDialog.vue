<template>
  <el-dialog v-model="visible" destroy-on-close width="460px" @closed="onClosed">
    <template #header>
      <div class="flex items-center justify-between pr-5">
        <span>{{ dialogTitle }}</span>
        <!-- 静音开关只给扫码档：现金是收银员自己按下的，不存在「没注意到」 -->
        <el-tooltip v-if="!isCash" :content="muted ? '已静音：收款成功不播报' : '收款成功会语音播报金额'"
                    placement="bottom">
          <button :data-muted="String(muted)"
                  class="flex h-7 w-7 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 hover:text-[#1269B5]"
                  data-testid="qrpay-mute-toggle" type="button" @click="toggleMute">
            <el-icon>
              <MuteNotification v-if="muted"/>
              <Bell v-else/>
            </el-icon>
          </button>
        </el-tooltip>
      </div>
    </template>
    <div class="space-y-3">
      <div class="rounded-lg bg-slate-50 px-3 py-2 text-sm">
        <div class="flex justify-between">
          <span class="text-slate-500">账单号</span>
          <span class="font-mono">{{ displayBillNo }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">患者</span>
          <span>{{ displayPatientName }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-slate-500">本单金额</span>
          <b class="text-red-600">{{ moneyText(displayAmount) }}</b>
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
          <span v-if="Number(summary.discountAmount) > 0">优惠 <b
              class="font-mono text-slate-700">-{{ moneyText(summary.discountAmount) }}</b></span>
          <span v-if="Number(summary.poolAmount) > 0">统筹报销 <b
              class="font-mono text-emerald-600">-{{ moneyText(summary.poolAmount) }}</b></span>
          <span v-if="Number(summary.accountAmount) > 0">个账 <b
              class="font-mono text-emerald-600">-{{ moneyText(summary.accountAmount) }}</b></span>
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

      <!-- 医保个账：刷社保卡→2201预结算→核对→2301扣个账→剩余cashPay转现金/扫码 -->
      <div v-else-if="isInsurance" class="min-h-[236px] rounded-lg border border-slate-200 p-3 text-sm">
        <template v-if="!insuranceSplit">
          <div class="py-2 text-center text-slate-500">
            <p>请患者出示医保电子凭证 / 社保卡</p>
            <p class="mt-1 text-xs">系统将调医保平台预结算，计算统筹与个账分摊</p>
          </div>
          <el-button :loading="insuranceLoading" data-testid="insurance-pre-settle" type="primary"
                     class="mt-2 w-full" @click="doPreSettle">
            调医保预结算（2201）
          </el-button>
        </template>
        <template v-else-if="!insuranceConfirmed">
          <div class="space-y-1.5 py-1">
            <div class="flex justify-between">
              <span class="text-slate-500">医保统筹报销</span>
              <b class="text-emerald-600">-{{ moneyText(insuranceSplit.insurancePay) }}</b>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">医保个账扣款</span>
              <b class="text-emerald-600">-{{ moneyText(insuranceSplit.personalAccount) }}</b>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">个人自付（现金/扫码）</span>
              <b class="text-red-600">{{ moneyText(insuranceSplit.cashPay) }}</b>
            </div>
            <div v-if="insuranceSplit.warnMsg" class="rounded bg-amber-50 px-2 py-1 text-xs text-amber-700">
              {{ insuranceSplit.warnMsg }}
            </div>
          </div>
          <el-button :loading="insuranceLoading" data-testid="insurance-formal-settle" type="success"
                     class="mt-2 w-full" @click="doFormalSettle">
            确认扣个账（2301）
          </el-button>
        </template>
        <template v-else>
          <div class="py-2 text-center">
            <el-icon class="text-emerald-500" size="40"><CircleCheckFilled/></el-icon>
            <p class="mt-1 font-medium text-emerald-600">医保个账已扣 {{ moneyText(insuranceSplit.personalAccount) }}</p>
            <p class="mt-1 text-xs text-slate-500">医保单号：{{ insuranceSplit.receiptNo }}</p>
            <p class="mt-2 text-sm text-slate-600">
              剩余自付 <b class="text-red-600">{{ moneyText(insuranceSplit.cashPay) }}</b> 元，请选择现金或扫码收款
            </p>
          </div>
        </template>
        <p v-if="error" class="mt-2 text-center text-sm text-red-600">{{ error }}</p>
      </div>

      <div v-else
           class="flex min-h-[236px] flex-col items-center justify-center rounded-lg border border-slate-200 p-3">
        <el-skeleton v-if="creating" :rows="3" animated class="w-full"/>
        <div v-else-if="qrImage && !isPaid" class="relative">
          <img :class="{ 'opacity-40 blur-[3px]': qrExpired }" :src="qrImage" alt="聚合收款码"
               class="h-[220px] w-[220px]"/>
          <div v-if="qrExpired" class="absolute inset-0 flex flex-col items-center justify-center gap-2">
            <p class="text-sm font-medium text-slate-600">二维码已过期</p>
            <el-button :loading="creating" data-testid="qrpay-refresh" size="small" type="primary"
                       @click="generate(true)">
              刷新收款码
            </el-button>
          </div>
          <el-tooltip v-else content="刷新收款码" placement="top">
            <button :disabled="creating"
                    class="absolute bottom-0.5 right-0.5 flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-500 shadow-sm hover:text-[#1269B5] disabled:cursor-not-allowed disabled:opacity-40"
                    data-testid="qrpay-refresh-corner" type="button" @click="generate(true)">
              <el-icon>
                <Refresh/>
              </el-icon>
            </button>
          </el-tooltip>
        </div>
        <!-- 成功态做成整块绿色 + 大对勾：余光能扫到。只靠「换一行小字」在收费台等于没有反馈 -->
        <div v-else-if="isPaid" class="flex flex-col items-center justify-center gap-1"
             data-testid="qrpay-paid-banner">
          <el-icon class="text-emerald-500" size="52">
            <CircleCheckFilled/>
          </el-icon>
          <p class="text-lg font-semibold text-emerald-600">支付成功</p>
          <p class="text-sm text-slate-500">{{ moneyText(amount) }} 已入账，可出票</p>
        </div>
        <p v-else class="px-2 text-center text-sm text-red-600">{{ error || '还没有生成收款码' }}</p>
      </div>

      <p v-if="!isCash" class="text-center text-xs text-slate-400">
        <template v-if="isPaid">钱已到账并已入账 —— 以本系统显示为准，不要只看患者手机</template>
        <template v-else-if="qrImage && qrExpired">二维码已过期，点击「刷新收款码」重新出码</template>
        <template v-else-if="qrImage">
          请患者用手机（微信/支付宝/云闪付）扫此码 · 收款码 {{ countdownText }} 后过期 ·
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
        <!-- 扫码档不摆「等待/已过期」这类禁用按钮当状态牌：按钮长那样就是让人点，
             点了没反应只会让人以为卡死。状态一律用文字说，按钮只留真能做的动作：
             未付=放弃这次收款（关闭即作废这张码），已付=完成 -->
        <template v-else>
          <span v-if="!isPaid" class="mr-auto self-center text-xs text-slate-400">
            {{ qrExpired ? '收款码已过期，可刷新重出' : '等待患者扫码支付…' }}
          </span>
          <el-button :type="isPaid ? 'success' : 'default'"
                     :data-testid="isPaid ? 'qrpay-done' : 'qrpay-cancel'" @click="visible = false">
            {{ isPaid ? '完成' : '取消收款' }}
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onUnmounted, ref, watch} from 'vue';
import {Bell, CircleCheckFilled, MuteNotification, Refresh} from '@element-plus/icons-vue';
import {closeQrPayByBill, createQrPay, formalSettleInsurance, getBillDetailById, payBill, preSettleInsurance, queryQrPayResult,} from '@/api/settlementBill';
import {moneyText} from '@/lib/patientField';

/**
 * 收款弹层：一笔账单收一次，方式在弹层里选 —— 只有「现金」与「扫码收款」两档。
 *
 * 扫码收款出的是<b>聚合支付码</b>：一张码微信、支付宝、云闪付都能扫，渠道在患者支付那一刻
 * 由医院聚合网关识别运行环境后确定。因此这里不再有支付宝/微信切换，
 * 「从支付宝切到微信要重出一张码、还可能把患者手上那张作废」这个问题在模型上就不存在了。
 *
 * 界面不出现「确认已支付」那类按钮 —— 扫码档的收款事实来自渠道（异步通知 + 查单兜底），
 * 前端点一下就把账单置成已支付，等于让没收到钱的账先平了。
 *
 * 收款成功怎么让收费员知道：渠道异步通知（秒级）+ 弹层开着时每 5 秒查单兜底（≤5s），
 * 落到界面上是整块绿色成功态，并<b>语音播报金额</b> —— 收费员在找零、核对下一位、看患者手机时，
 * 视觉反馈根本进不了注意力，声音是唯一能穿透的通道（可在标题栏关掉，偏好按终端记住）。
 * 现金是唯一「本地自己决定」的一档：收银员当场点钞收到，按下确认即记一笔现金流水，
 * 金额一律取账单尚欠（不给输入框），账实是否相符交给交班清点核对。
 *
 * 现金与扫码可来回切：切到现金或关掉弹层时，本账单的待支付码一并作废
 * （现金已进抽屉，任何一张活码都是重复收款入口）；切回扫码则重新出一张聚合码。
 * 码还活着时（点刷新、重开弹层）不会重出 —— 本地沿用原图，后端也先查缓存再决定要不要生成。
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
  /** 打开即给现金档（挂号窗口默认收现金）；收费台按「先扫码」的口径保持关闭 */
  allowCash: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'paid']);

const PAY_STATUS_PAID = 1;
const PAY_STATUS_CLOSED = 2;
/** 扫码收款档（聚合支付码，一码多平台）的哨兵值，只在弹层内部做分支，不是渠道码也不是落库码值 */
const SCAN_METHOD = 'SCAN';
/** 现金档的哨兵值（不是落库码值） */
const CASH_METHOD = 'CASH';
/** 医保个账档的哨兵值（收款窗口刷社保卡→2201预结算→2301扣个账） */
const INSURANCE_METHOD = 'INSURANCE';
/** 支付方式码值 1-现金（PaymentMethodEnum.CASH） */
const PAY_METHOD_CASH = 1;
/** 轮询间隔与后端「同单查单节流」同宽：比它更快只会撞上被节流的那一轮，白多一次请求 */
const POLL_MS = 5000;
const TICK_MS = 1000;
/** 静音偏好按终端记（localStorage）：夜班/安静窗口不需要声音，不该每次开弹层都去点一次 */
const MUTE_KEY = 'his:qrpay:mute';

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

/** 当前收款档：SCAN（聚合码）或 CASH */
const method = ref(SCAN_METHOD);
const cashSubmitting = ref(false);
const payNo = ref('');
const qrImage = ref('');
const payStatus = ref(null);
const creating = ref(false);
/** 本次收款意图的请求幂等号（防重提交）：同一笔收款双击/网络重试复用同一值，
 *  后端按 (账单, requestId) 判重拦重放；重开弹窗才是新的一笔，onClosed 里换新值 */
const requestId = ref('');
/** 切到现金时关码失败 —— 此时可能还有能扫的码，收现金就是重复收款，必须拦住 */
const codesAlive = ref(false);
const error = ref('');
const pollError = ref('');
const muted = ref(localStorage.getItem(MUTE_KEY) === '1');
const billDetail = ref(null);
/** 出码返回的身份事实（服务端按 billId 查到的账单号/患者/金额），是头部显示的核对基准 */
const billing = ref(null);
const detailLoading = ref(false);
/** 医保个账档：2201 预结算返回的 splits（统筹/个账/自付） */
const insuranceSplit = ref(null);
/** 医保个账档：2201/2301 请求中 */
const insuranceLoading = ref(false);
/** 医保个账档：2301 已成功扣个账 */
const insuranceConfirmed = ref(false);
/** 过期时刻在本地时钟上的落点（由服务端口径的剩余时长锚定） */
const countdownDeadline = ref(0);
const nowTick = ref(Date.now());
let timer = null;
let tickTimer = null;

const isPaid = computed(() => Number(payStatus.value) === PAY_STATUS_PAID);
const isClosed = computed(() => Number(payStatus.value) === PAY_STATUS_CLOSED);
const isCash = computed(() => method.value === CASH_METHOD);
const isInsurance = computed(() => method.value === INSURANCE_METHOD);
/** 这张账单是不是医保结算方式（settlementMode=2）：只有医保账单才显示「医保个账」档 */
const isInsuranceBill = computed(() => Number(summary.value.settlementMode) === 2);
const methodOptions = computed(() => {
  const base = props.allowCash
    ? [{code: CASH_METHOD, label: '现金'}, {code: SCAN_METHOD, label: '扫码收款'}]
    : [{code: SCAN_METHOD, label: '扫码收款'}, {code: CASH_METHOD, label: '现金'}];
  if (isInsuranceBill.value && !insuranceConfirmed.value) {
    base.splice(0, 0, {code: INSURANCE_METHOD, label: '医保个账'});
  }
  return base;
});
const dialogTitle = computed(() => (props.allowCash ? '收费' : '扫码收款'));
/** 现金只收账单尚欠：应收/已收都由服务端现算，页面不自减（尚欠没回来时退回父页面传入的本单金额） */
const cashAmount = computed(() => Number(summary.value.unpaidAmount ?? props.amount) || 0);
const feeItems = computed(() => billDetail.value?.items || []);
const summary = computed(() => billDetail.value || {});
const displayBillNo = computed(() => billing.value?.billNo || props.billNo || '—');
const displayPatientName = computed(() => billing.value?.patientName || props.patientName || '—');
const displayAmount = computed(() => billing.value?.amount ?? props.amount ?? 0);
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
  // 医保账单默认停在「医保个账」档：收费员开弹层就是为了刷社保卡，让他自己切一档是多余动作
  method.value = isInsuranceBill.value ? INSURANCE_METHOD : (props.allowCash ? CASH_METHOD : SCAN_METHOD);
  codesAlive.value = false;
  loadBillDetail();
  if (!isCash.value && !isInsurance.value) {
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

/**
 * 出聚合码。
 *
 * @param force true=明确要换一张新码（点刷新、过期重出）；false=「切到扫码档，有码就显示」。
 *              后者是默认：码还在有效期内重新出码会把患者手上那张作废，也白挨一次后端出码节流
 */
async function generate(force = false) {
  if (!props.billId) {
    error.value = '缺少账单，无法出码';
    return;
  }
  if (!force && qrImage.value && !isPaid.value && !qrExpired.value) {
    // 本地已有一张活着的聚合码：直接沿用，连后端都不用问（后端也是先查缓存的）
    error.value = '';
    pollError.value = '';
    startTick();
    startPoll();
    return;
  }
  stopPoll();
  creating.value = true;
  error.value = '';
  pollError.value = '';
  qrImage.value = '';
  payStatus.value = null;
  payNo.value = '';
  countdownDeadline.value = 0;
  try {
    // 聚合码不传渠道：一张码多平台可扫，渠道在支付时由网关确定
    const res = await createQrPay({billId: props.billId});
    const data = res.data || {};
    if (!data.qrImage) {
      error.value = '没有返回收款码，请刷新重试';
      return;
    }
    payNo.value = data.payNo || '';
    qrImage.value = data.qrImage;
    payStatus.value = data.payStatus ?? null;
    // 头部改按出码返回值渲染：账单号/患者/金额是服务端事实，不再是列表行快照
    billing.value = {
      billNo: data.billNo,
      patientName: data.patientName,
      amount: data.amount,
    };
    anchorCountdown(data.expireTime, data.serverTime);
    startTick();
    startPoll();
  } catch (e) {
    error.value = e.message || '出码失败';
  } finally {
    creating.value = false;
  }
}

/**
 * 档位切换：扫码 ⇄ 现金。
 *
 * 现金 → 扫码：重新出一张聚合码（上次那张在切现金时已作废）。
 * 扫码 → 现金：本账单的待支付码一律关掉 —— 收了现金还留着能扫的码，患者扫完就是同一笔账收两次。
 */
async function onMethodChange(val) {
  if (val === CASH_METHOD) {
    await closeAllQrCodes();
    return;
  }
  await generate();
}

/**
 * 关闭本账单的待支付码并清空本地码状态（切现金、关弹层共用）。
 *
 * 关失败必须如实提示：码可能还活着，此时此刻收现金就是重复收款。
 */
async function closeAllQrCodes() {
  const hadCode = !!payNo.value || !!qrImage.value;
  stopPoll();
  stopTick();
  resetQrState();
  if (!hadCode || !props.billId) {
    codesAlive.value = false;
    return;
  }
  try {
    await closeQrPayByBill({billId: props.billId});
    codesAlive.value = false;
  } catch (e) {
    // 关不掉就必须当真：那张码可能还活着，此刻收现金就是同一笔账收两次
    codesAlive.value = true;
    error.value = e.message || '收款码未能关闭，请勿收现金，先联系信息科';
  }
}

/** 清空当前显示的码（不碰后端） */
function resetQrState() {
  payNo.value = '';
  qrImage.value = '';
  payStatus.value = null;
  countdownDeadline.value = 0;
  // 出码事实一并清掉：切现金/关弹层后头部回落 props，避免拿已作废的码的事实继续显示
  billing.value = null;
}

/** 现金入账：记一笔现金流水，账单是否付清由后端按流水现算 */
async function confirmCash() {
  if (!props.billId) {
    error.value = '缺少账单，无法收款';
    return;
  }
  if (codesAlive.value) {
    // 上一张码没关掉：先让它彻底失效再收现金，否则患者扫旧码就是同一笔账付两次
    error.value = '收款码未能关闭，请勿收现金：请先关掉弹窗重新打开，或等收款码过期后再收';
    return;
  }
  if (cashAmount.value <= 0) {
    error.value = '该账单没有应缴金额';
    return;
  }
  // 本次收款意图只生成一次幂等号：双击确认/网络重试带上同一个值，
  // 后端判重把重放拦下，不会收出第二笔钱
  requestId.value = requestId.value || genRequestId();
  cashSubmitting.value = true;
  error.value = '';
  try {
    await payBill({
      billId: props.billId,
      // 收银员在本窗口当面收款（收银台 / 挂号窗口现金档），来源固定记「收费台收款」
      sourceType: 1,
      requestId: requestId.value,
      items: [{payMethod: PAY_METHOD_CASH, amount: cashAmount.value}],
    });
    emit('paid', {payMethod: PAY_METHOD_CASH, amount: cashAmount.value});
    visible.value = false;
  } catch (e) {
    error.value = e.message || '现金收款失败';
  } finally {
    cashSubmitting.value = false;
  }
}

/** 生成请求幂等号：优先 crypto.randomUUID（Edge/Chrome 均支持），低版本回退时间戳+随机串 */
function genRequestId() {
  if (window.crypto && typeof window.crypto.randomUUID === 'function') {
    return window.crypto.randomUUID();
  }
  return Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
}

/** 2201 预结算：调医保平台算 splits，不落库 */
async function doPreSettle() {
  if (!props.billId) {
    error.value = '缺少账单';
    return;
  }
  insuranceLoading.value = true;
  error.value = '';
  try {
    const res = await preSettleInsurance(props.billId);
    insuranceSplit.value = res.data || null;
  } catch (e) {
    error.value = e.message || '医保预结算失败';
  } finally {
    insuranceLoading.value = false;
  }
}

/** 2301 正式结算：医保平台扣个账、出 YBZ 单号，HIS 同事务落个账流水+回写 splits */
async function doFormalSettle() {
  if (!props.billId || !insuranceSplit.value) {
    error.value = '请先预结算';
    return;
  }
  insuranceLoading.value = true;
  error.value = '';
  try {
    const res = await formalSettleInsurance(props.billId);
    const data = res.data || {};
    insuranceSplit.value = data;
    insuranceConfirmed.value = true;
    // 个账扣完，重取账单明细（pool/account/self 已回写，cashPay 就是剩下的钱）
    await loadBillDetail();
    // 自动切到现金档收剩余（收费员当面收现金最直接；要扫码收费员自己切）
    method.value = props.allowCash ? CASH_METHOD : SCAN_METHOD;
    if (method.value === SCAN_METHOD) {
      await generate();
    }
  } catch (e) {
    error.value = e.message || '医保正式结算失败';
  } finally {
    insuranceLoading.value = false;
  }
}

async function pollOnce() {
  if (isPaid.value || !payNo.value) {
    return;
  }
  try {
    const res = await queryQrPayResult({payNo: payNo.value});
    const data = res.data || {};
    payStatus.value = data.payStatus ?? payStatus.value;
    pollError.value = '';
    if (Number(data.payStatus) === PAY_STATUS_PAID) {
      finishPaid(data);
      return;
    }
    anchorCountdown(data.expireTime, data.serverTime);
    nowTick.value = Date.now();
    // 服务端已把这张过期单关掉：停下轮询，别每 5 秒再问一次
    if (Number(data.payStatus) === PAY_STATUS_CLOSED) {
      stopPoll();
    }
  } catch (e) {
    // 查询失败不打断轮询：公网穿透抖动时渠道那边钱可能已经到账
    pollError.value = e.message || '查询失败';
  }
}

/**
 * 收款成功语音播报（只报金额，不报单号）。
 *
 * <p>为什么必须有声音：收费员不会一直盯着这个 460px 的弹层 —— 他在找零、核对下一位患者、
 * 抬头看患者手机。屏幕上换一行字根本进不了注意力，而「患者说付了、屏幕还写着等待」是最容易
 * 吵起来的场景。声音是唯一能穿透注意力的通道，这也是收钱吧 / 支付宝收款码的标配行为。
 * 只报金额：屏幕上已有完整信息，收费员要核对的只是「这笔收了多少」。
 *
 * <p>没有 TTS 引擎（老机器无语音包、浏览器不支持）就静默跳过：声音是<b>加强</b>，
 * 视觉反馈仍然成立，不能因为播不出来就报错、更不能因此不推进收款。
 */
function announcePaid() {
  if (muted.value || !window.speechSynthesis) {
    return;
  }
  try {
    const amt = Number(props.amount || 0).toFixed(2);
    const utter = new SpeechSynthesisUtterance(`收款成功，${amt}元`);
    utter.lang = 'zh-CN';
    utter.rate = 1.05;
    window.speechSynthesis.speak(utter);
  } catch {
    // 静默：播报失败不影响收款结果，账单该入账的已经入账
  }
}

/** 切换静音只是改偏好，不试听 —— 试听一声「收款成功」会让收费员以为真收到钱了 */
function toggleMute() {
  muted.value = !muted.value;
  localStorage.setItem(MUTE_KEY, muted.value ? '1' : '0');
}

function finishPaid(data) {
  stopPoll();
  stopTick();
  payStatus.value = PAY_STATUS_PAID;
  emit('paid', data);
  announcePaid();
  // 支付成功后重取明细：费用条上的「尚欠」要跟着收到钱归零，不能停在收款前的快照
  loadBillDetail();
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
  // 未付款的码必须关掉：留着它就是一张「本地已作废、还能扫出来付款」的活码
  const hadCode = !!payNo.value || !!qrImage.value;
  const billId = props.billId;
  if (!isPaid.value && hadCode && billId) {
    closeQrPayByBill({billId}).catch(() => {
    });
  }
  method.value = props.allowCash ? CASH_METHOD : SCAN_METHOD;
  cashSubmitting.value = false;
  codesAlive.value = false;
  requestId.value = '';
  insuranceSplit.value = null;
  insuranceConfirmed.value = false;
  insuranceLoading.value = false;
  resetQrState();
  error.value = '';
  pollError.value = '';
  billDetail.value = null;
}
</script>
