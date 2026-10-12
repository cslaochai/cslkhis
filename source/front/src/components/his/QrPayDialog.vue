<template>
  <el-dialog v-model="visible" destroy-on-close title="扫码收款" width="440px" @closed="onClosed">
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

      <el-radio-group v-model="channel" :disabled="creating || isPaid" @change="generate">
        <el-radio v-for="item in CHANNEL_OPTIONS" :key="item.code" :value="item.code">{{ item.label }}</el-radio>
      </el-radio-group>

      <div class="flex min-h-[236px] flex-col items-center justify-center rounded-lg border border-slate-200 p-3">
        <el-skeleton v-if="creating" :rows="3" animated class="w-full"/>
        <img v-else-if="qrImage" :src="qrImage" :alt="`${channelLabel}收款码`" class="h-[220px] w-[220px]"/>
        <p v-else-if="isPaid" class="text-center text-base font-medium text-emerald-600">
          支付成功<br>
          <span class="text-sm font-normal text-slate-500">{{ moneyText(amount) }} 已入账，可出票</span>
        </p>
        <p v-else class="px-2 text-center text-sm text-red-600">{{ error || '还没有生成收款码' }}</p>
      </div>

      <p class="text-center text-xs text-slate-400">
        <template v-if="isPaid">收款流水已由后端按渠道查单确认，页面不代替它翻状态</template>
        <template v-else-if="qrImage">
          请患者用{{ channelLabel }}扫此码 · 已自动确认支付结果{{ pollError ? `（最近一次查询失败：${pollError}）` : '' }}
        </template>
        <template v-else>一张码只属于一个渠道，换渠道会作废前一张码</template>
      </p>
    </div>

    <template #footer>
      <div class="flex justify-between">
        <el-button @click="visible = false">{{ isPaid ? '关闭' : '放弃收款' }}</el-button>
        <div class="space-x-2">
          <el-button v-if="!isPaid" :loading="creating" plain @click="generate">重新出码</el-button>
          <el-button :type="isPaid ? 'success' : 'info'" :disabled="!isPaid" @click="visible = false">
            {{ isPaid ? '完成' : '等待患者支付' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onUnmounted, ref, watch} from 'vue';
import {closeQrPay, createQrPay, queryQrPayResult} from '@/api/settlementBill';
import {moneyText} from '@/lib/patientField';

/**
 * 主扫收款弹层（收费台与挂号共用）：医院出码、患者扫码，页面只轮询结果。
 *
 * 界面不出现「确认已支付」那类按钮 —— 收款事实来自渠道（异步通知 + 查单兜底），
 * 前端点一下就把账单置成已支付，等于让没收到钱的账先平了。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  billId: {type: [String, Number], default: null},
  billNo: {type: String, default: ''},
  patientName: {type: String, default: ''},
  amount: {type: [String, Number], default: 0},
});
const emit = defineEmits(['update:modelValue', 'paid']);

const PAY_STATUS_PAID = 1;
const PAY_STATUS_PENDING = 0;
/** 渠道码值（PayChannelEnum）：1-微信 2-支付宝。支付宝已接通，微信缺商户证书，出码会明确报错 */
const CHANNEL_OPTIONS = [{code: 2, label: '支付宝'}, {code: 1, label: '微信'}];
const POLL_MS = 2500;

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const channel = ref(CHANNEL_OPTIONS[0].code);
const payNo = ref('');
const qrImage = ref('');
const payStatus = ref(null);
const creating = ref(false);
const error = ref('');
const pollError = ref('');
let timer = null;

const isPaid = computed(() => Number(payStatus.value) === PAY_STATUS_PAID);
const channelLabel = computed(() => CHANNEL_OPTIONS.find((i) => i.code === channel.value)?.label || '');

watch(visible, (v) => {
  if (v) {
    generate();
  }
});

onUnmounted(stopPoll);

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
  try {
    const res = await createQrPay({billId: props.billId, channel: channel.value});
    const data = res.data || {};
    payNo.value = data.payNo || '';
    qrImage.value = data.qrImage || '';
    payStatus.value = data.payStatus ?? null;
    if (!qrImage.value) {
      error.value = '渠道没有返回收款码，请重新出码';
      return;
    }
    startPoll();
  } catch (e) {
    error.value = e.message || '出码失败';
  } finally {
    creating.value = false;
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
    pollError.value = '';
    if (Number(data.payStatus) === PAY_STATUS_PAID) {
      stopPoll();
      emit('paid', data);
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
  // 未付款的码必须关掉：留着它就是一张「本地已作废、渠道还能扫出来付款」的活码
  if (payNo.value && Number(payStatus.value) === PAY_STATUS_PENDING) {
    closeQrPay({payNo: payNo.value}).catch(() => {});
  }
  payNo.value = '';
  qrImage.value = '';
  payStatus.value = null;
  error.value = '';
  pollError.value = '';
}
</script>
