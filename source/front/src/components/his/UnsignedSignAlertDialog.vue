<template>
  <el-dialog v-model="visible" width="600px" align-center append-to-body :show-close="false"
             :close-on-click-modal="false" :close-on-press-escape="false"
             class="unsigned-sign-alert" data-testid="unsigned-sign-alert-dialog">
    <div class="alert-card">
      <div class="alert-icon">!</div>
      <h2 class="alert-title">电子签名证书不可用提醒</h2>
      <div class="alert-body">
        <p>{{ reasonText }}</p>
        <p>本次结诊可以继续，本次<b>病历、处方及检查检验申请单</b>将标记为「待补签」，
          但<b>不会阻断患者就医</b>。</p>
        <ul>
          <li><b>检查、检验</b>与<b>药房发药</b>照常进行，患者缴费后可正常去做、正常取药；</li>
          <li>处方按《处方管理办法》的纸质路径处理：打印纸质处方由您手写签名或加盖签章，药师核对后发放；</li>
          <li><b>病案归档</b>会被拦截，直至补签完成（或病案管理岗核验纸质签名）后放行；</li>
          <li>系统已同步提醒信息科与医务科，请尽快办理或换发软签证书。</li>
        </ul>
      </div>
      <div class="alert-actions">
        <el-button size="large" data-testid="unsigned-sign-alert-cancel" @click="onCancel">取消，返回修改</el-button>
        <el-button type="warning" size="large" :disabled="countdown > 0"
                   data-testid="unsigned-sign-alert-confirm" @click="onConfirm">
          {{ countdown > 0 ? `确认已知晓（${countdown}s）` : '确认已知晓' }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {ref, onBeforeUnmount} from 'vue';

// 结诊前的无证书强提示：遮罩挂在当前页面之上，不铺满整屏；确认才放行无签结诊，取消则本次不结诊
const visible = ref(false);
const countdown = ref(0);
// 不可用原因（未办理 / CA 未签发 / 已过期）由结诊签名预检给出，组件不再自己猜
const reasonText = ref('经系统核验，您名下当前没有有效的电子签名证书。');
let timer = null;
let resolver = null;

/**
 * 打开强提示。
 * @returns {Promise<boolean>} true=确认继续无签结诊；false=取消，本次不结诊
 */
const open = (precheck) => {
  reasonText.value = (precheck && precheck.message)
      || '经系统核验，您名下当前没有有效的电子签名证书。';
  visible.value = true;
  // 确认按钮 3 秒倒计时，避免误点；取消不受限
  clearInterval(timer);
  countdown.value = 3;
  timer = setInterval(() => {
    if (countdown.value > 0) countdown.value--;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
  return new Promise((resolve) => {
    resolver = resolve;
  });
};

const settle = (confirmed) => {
  clearInterval(timer);
  visible.value = false;
  if (resolver) {
    resolver(confirmed);
    resolver = null;
  }
};

const onConfirm = () => {
  if (countdown.value > 0) return;
  settle(true);
};

const onCancel = () => settle(false);

onBeforeUnmount(() => clearInterval(timer));

defineExpose({open});
</script>

<style scoped>
.unsigned-sign-alert :deep(.el-dialog__header) {
  display: none;
}

.unsigned-sign-alert :deep(.el-dialog) {
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
}

.alert-card {
  padding: 28px 32px 24px;
  text-align: center;
}

.alert-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: #f59e0b;
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  line-height: 52px;
}

.alert-title {
  margin: 0 0 14px;
  font-size: 19px;
  color: #1f2937;
}

.alert-body {
  text-align: left;
  font-size: 14px;
  color: #475569;
  line-height: 1.9;
  margin-bottom: 22px;
}

.alert-body ul {
  margin: 8px 0 0;
  padding-left: 20px;
}

.alert-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.alert-actions :deep(.el-button) {
  width: 200px;
  margin-left: 0;
}
</style>
