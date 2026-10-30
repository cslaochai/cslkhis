<template>
  <!-- ---------------- 员工签名证书（只读弹框，从员工管理操作列进入） ---------------- -->
  <el-dialog v-model="visible" width="640px" :title="`签名证书（${employee?.empName || '—'}）`">
    <div v-loading="loading">
      <template v-if="cert">
        <div class="rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs leading-6">
          <p>证书编号：<b>{{ cert.certNo }}</b>
            <el-tag :type="cert.certStatus === 1 ? 'success' : (cert.certStatus === 0 ? 'warning' : 'info')"
                    class="ml-2" size="small">{{ cert.certStatusText }}
            </el-tag>
            <span v-if="cert.expired" class="ml-1 text-red-600">· 已过有效期</span>
          </p>
          <p>算法组合：{{ cert.keyAlgo }} / {{ cert.digestAlgo }} / {{ cert.signAlgo }}</p>
          <p>有效期：{{ text(cert.validFrom) }} ~ {{ text(cert.validTo) }}</p>
          <p>CA 签发：{{ text(cert.caIssuer) }}<span v-if="cert.caSerial" class="text-slate-500"> · 序列号 {{
              cert.caSerial
            }}</span></p>
          <p>公钥指纹：<span class="break-all font-mono">{{ text(cert.keyFingerprintGroups || cert.keyFingerprint) }}</span></p>
          <p>累计签名：{{ num(cert.signCount) }} 次 · 最近使用 {{ text(cert.lastUsedTime) }}</p>
          <p v-if="cert.remark">备注：{{ text(cert.remark) }}</p>
        </div>

        <div class="mt-3">
          <p class="text-xs font-medium text-slate-900">公钥（PEM）</p>
          <pre
              class="mt-2 max-h-56 overflow-auto whitespace-pre-wrap rounded bg-slate-50 p-3 font-mono text-xs leading-5 text-slate-700">{{
              text(cert.publicKey)
            }}</pre>
        </div>
      </template>
      <div v-else-if="!loading" class="py-6 text-center text-sm text-slate-400" data-testid="emp-sign-cert-empty">
        该员工暂无有效签名证书
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {getSignCertByEmp} from '@/api/signature';
import {num, text} from '@/lib/sign';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 员工行（用到 id / empName），弹框打开时按员工取其签名证书
  employee: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const cert = ref(null);

watch(visible, async (open) => {
  if (!open || !props.employee?.id) return;
  cert.value = null;
  loading.value = true;
  try {
    const res = await getSignCertByEmp(props.employee.id);
    cert.value = res.data || null;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    loading.value = false;
  }
});
</script>
