<template>
  <el-dialog v-model="visible" fullscreen append-to-body :show-close="false"
             :close-on-click-modal="false" :close-on-press-escape="false"
             class="unsigned-sign-alert" data-testid="unsigned-sign-alert-dialog">
    <div class="alert-wrap">
      <div class="alert-card">
        <div class="alert-icon">!</div>
        <h2 class="alert-title">电子签名证书不可用提醒</h2>
        <div class="alert-body">
          <p>经系统核验，您名下当前<b>没有有效的电子签名证书</b>（未配置或已被吊销）。</p>
          <p>本次结诊可以继续，但本次<b>病历、处方及检查检验申请单将无法完成医师电子签名</b>，
            进入「待补签」状态。</p>
          <ul>
            <li>该病历的<b>病案归档</b>与相关处方的<b>药房发药</b>将被系统拦截，直至补签完成；</li>
            <li>系统已同步提醒医务科，请在 <b>2 个工作日内</b>联系医务科完成补签；</li>
            <li>如需立即合规归档，请在补签前按纸质病历流程手写签名后交病案管理岗人工核验。</li>
          </ul>
        </div>
        <el-button type="warning" size="large" class="alert-btn" :disabled="countdown > 0"
                   data-testid="unsigned-sign-alert-confirm" @click="onConfirm">
          {{ countdown > 0 ? `确认已知晓（${countdown}s）` : '确认已知晓' }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {ref, onBeforeUnmount} from 'vue';

// 不可绕过的强提示：无 cancel、无 ESC、无点遮罩关闭，医生确认后才放行结诊
const visible = ref(false);
const countdown = ref(0);
let timer = null;
let resolver = null;

const open = () => {
  visible.value = true;
  countdown.value = 3;
  timer = setInterval(() => {
    if (countdown.value > 0) countdown.value--;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
  return new Promise((resolve) => {
    resolver = resolve;
  });
};

const onConfirm = () => {
  if (countdown.value > 0) return;
  visible.value = false;
  if (resolver) {
    resolver();
    resolver = null;
  }
};

onBeforeUnmount(() => clearInterval(timer));

defineExpose({open});
</script>

<style scoped>
.unsigned-sign-alert :deep(.el-dialog) {
  background: #fff7ed;
}

.alert-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.alert-card {
  width: 560px;
  background: #fff;
  border-radius: 12px;
  padding: 32px 40px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.alert-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #f59e0b;
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  line-height: 56px;
}

.alert-title {
  margin: 0 0 16px;
  font-size: 20px;
  color: #1f2937;
}

.alert-body {
  text-align: left;
  font-size: 14px;
  color: #475569;
  line-height: 1.9;
  margin-bottom: 24px;
}

.alert-body ul {
  margin: 8px 0 0;
  padding-left: 20px;
}

.alert-btn {
  width: 220px;
}
</style>
