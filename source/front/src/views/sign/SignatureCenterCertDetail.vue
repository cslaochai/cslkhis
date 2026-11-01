<template>
  <!-- ---------------- 证书详情 ---------------- -->
  <el-drawer v-model="visible" size="50%" title="签名证书详情">
    <template v-if="certDetail">
      <div class="text-sm">
        <p>证书编号：<b>{{ certDetail.certNo }}</b></p>
        <p class="mt-1">持有人：<b>{{ certDetail.empName }}</b>（{{ text(certDetail.deptName) }}）</p>
        <p>员工编号：<span class="font-mono" data-testid="p5-sign-cert-detail-empcode">{{
            text(certDetail.empCode)
          }}</span></p>
      </div>
      <div class="mt-3 rounded-lg border border-slate-200 bg-white p-3 text-xs leading-6">
        <p>算法组合：{{ certDetail.keyAlgo }} / {{ certDetail.digestAlgo }} / {{ certDetail.signAlgo }}</p>
        <p>申请主体 DN：<span class="font-mono">{{ text(certDetail.caSubjectDn) }}</span></p>
        <p>CA 签发：<span v-if="certDetail.caSerial" class="font-mono">序列号 {{ certDetail.caSerial }}</span>
          <span v-else>{{ certDetail.caApplyNo ? '待 CA 签发' : '未回填（待线下送 CA 签发）' }}</span></p>
        <p v-if="certDetail.caApplyNo">CA 申请单：<span class="font-mono">{{ certDetail.caApplyNo }}</span> ·
          {{ text(certDetail.caApplyStatusText) }}</p>
        <p v-if="certDetail.caLastMessage" class="text-slate-600">CA 说明：{{ certDetail.caLastMessage }}</p>
        <p>状态：{{ certDetail.certStatusText }}<span v-if="certDetail.expired"
                                                     class="text-red-600"> · 已过有效期</span></p>
        <p v-if="certDetail.validityEstimated">有效期：<span class="text-slate-500">待 CA 签发，以 CA 实际签发证书为准</span>
          <span class="text-slate-400">（申请预期 {{ text(certDetail.validFrom) }} ~ {{ text(certDetail.validTo) }}）</span></p>
        <p v-else>有效期：{{ text(certDetail.validFrom) }} ~ {{ text(certDetail.validTo) }}
          <span class="text-slate-400">（取自 CA 证书文件的 notBefore / notAfter，即 CA 实际签发的有效期）</span></p>
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
        <p class="text-xs font-medium text-slate-900">CA 申请记录</p>
        <p v-if="!caApplies.length" class="mt-2 text-xs text-slate-500">
          未向 CA 发起过在线申请；这张证书走线下送 CA 签发。
        </p>
        <div v-for="item in caApplies" :key="item.applySeq"
             class="mt-2 border-t border-slate-100 pt-2 text-xs leading-6 first:mt-1 first:border-t-0 first:pt-1">
          <p class="flex flex-wrap items-center gap-2">
            <span class="font-medium">第 {{ num(item.applySeq) }} 次申请</span>
            <el-tag v-if="item.applyStatus" :type="caApplyTagType(item.applyStatus)" size="small">{{
                text(item.applyStatusText)
              }}</el-tag>
            <span v-if="item.applyNo" class="font-mono text-slate-500">{{ item.applyNo }}</span>
          </p>
          <p class="text-slate-600">提交 {{ text(item.submittedAt) }}
            <span v-if="item.reviewedAt"> · 审核 {{ text(item.reviewedAt) }}</span>
            <span v-if="item.issuedAt"> · 签发 {{ text(item.issuedAt) }}</span>
            <span v-if="item.boundAt"> · 回填生效 {{ text(item.boundAt) }}</span>
          </p>
          <p v-if="item.caMessage" class="text-slate-600">CA 说明：{{ item.caMessage }}</p>
          <p v-if="item.caIssuer" class="text-slate-500">签发 CA：<span class="font-mono">{{
              text(item.caIssuer)
            }}</span></p>
          <p class="text-slate-500">操作人：{{ text(item.operatorName) }}</p>
        </div>
      </div>
      <div class="mt-3 rounded-lg border border-slate-200 bg-white p-3">
        <div class="flex items-center justify-between">
          <p class="text-xs font-medium text-slate-900">公钥（PEM）</p>
          <el-button v-if="certDetail.publicKey" data-testid="p5-sign-cert-copy-apply" link size="small"
                     type="primary" @click="copyApplyMaterial">复制 CA 申请材料（DN + 公钥）
          </el-button>
        </div>
        <pre
            class="mt-2 max-h-64 overflow-auto whitespace-pre-wrap rounded bg-slate-50 p-3 font-mono text-xs leading-5 text-slate-700"
            data-testid="p5-sign-cert-publickey">{{
            text(certDetail.publicKey)
          }}</pre>
        <p class="mt-2 text-xs text-slate-500">私钥以主口令加密后托管，任何接口都不会回显私钥。复制的内容可直接在 CA
          平台「证书申请」里提交。</p>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getSignCertDetail, listSignCaApplies} from '@/api/signature';
import {caApplyTagType, num, text} from '@/lib/sign';

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
const caApplies = ref([]);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  certDetail.value = null;
  caApplies.value = [];
  try {
    const res = await getSignCertDetail(props.id);
    certDetail.value = res.data;
    const applies = await listSignCaApplies(props.id);
    caApplies.value = applies.data || [];
  } catch (e) {
    console.error('加载签名证书详情失败', e);
  }
});

// 一键复制 CA 申请材料：拿去 CA 平台「证书申请」粘贴提交（Subject DN + 公钥 PEM，私钥不出 HIS）
const copyApplyMaterial = async () => {
  const material = `Subject DN: ${certDetail.value.caSubjectDn || ''}\nPublic Key PEM:\n${certDetail.value.publicKey || ''}`;
  try {
    await navigator.clipboard.writeText(material);
    ElMessage.success('已复制申请材料，可到 CA 平台证书申请页粘贴提交');
  } catch (e) {
    ElMessage.warning('复制失败，请手动选中上方文本复制');
  }
};
</script>
