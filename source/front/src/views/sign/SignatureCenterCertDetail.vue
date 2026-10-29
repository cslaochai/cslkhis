<template>
  <!-- ---------------- 证书详情 ---------------- -->
  <el-drawer v-model="visible" size="50%" title="签名证书详情">
    <template v-if="certDetail">
      <div class="text-sm">
        <p>证书编号：<b>{{ certDetail.certNo }}</b></p>
        <p class="mt-1">持有人：<b>{{ certDetail.empName }}</b>（{{ text(certDetail.deptName) }}）</p>
      </div>
      <div class="mt-3 rounded-lg border border-slate-200 bg-white p-3 text-xs leading-6">
        <p>算法组合：{{ certDetail.keyAlgo }} / {{ certDetail.digestAlgo }} / {{ certDetail.signAlgo }}</p>
        <p>CA 签发：{{ text(certDetail.caIssuer) || '未回填（待线下送 CA 签发）' }}<span
            v-if="certDetail.caSerial" class="text-slate-500"> · 序列号 {{ certDetail.caSerial }}</span></p>
        <p>状态：{{ certDetail.certStatusText }}<span v-if="certDetail.expired"
                                                     class="text-red-600"> · 已过有效期</span></p>
        <p>有效期：{{ text(certDetail.validFrom) }} ~ {{ text(certDetail.validTo) }}</p>
        <p>累计签名：{{ num(certDetail.signCount) }} 次 · 最近使用 {{ text(certDetail.lastUsedTime) }}</p>
        <p>公钥指纹：<span class="font-mono" data-testid="p5-sign-cert-detail-fp">{{
            text(certDetail.keyFingerprint)
          }}</span></p>
        <template v-if="certDetail.revokeTime">
          <p class="text-red-700">吊销：{{ text(certDetail.revokeTime) }} · {{ text(certDetail.revokeByName) }}</p>
          <p class="text-red-700">理由：{{ text(certDetail.revokeReason) }}</p>
        </template>
      </div>
      <div class="mt-3 rounded-lg border border-slate-200 bg-white p-3">
        <p class="text-xs font-medium text-slate-900">公钥（PEM）</p>
        <pre
            class="mt-2 max-h-64 overflow-auto whitespace-pre-wrap rounded bg-slate-50 p-3 font-mono text-xs leading-5 text-slate-700"
            data-testid="p5-sign-cert-publickey">{{
            text(certDetail.publicKey)
          }}</pre>
        <p class="mt-2 text-xs text-slate-500">私钥以主口令加密后托管，任何接口都不会回显私钥。</p>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getSignCertDetail} from '@/api/signature';
import {num, text} from '@/lib/sign';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前查看的证书 id，抽屉打开时按 id 自取详情
  id: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const certDetail = ref(null);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  certDetail.value = null;
  try {
    const res = await getSignCertDetail(props.id);
    certDetail.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
});
</script>
