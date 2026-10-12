<template>
  <!-- ---------------- 签名详情抽屉 ---------------- -->
  <el-drawer v-model="visible" size="60%" title="签名详情与验签">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <div class="flex flex-wrap items-center gap-4 text-sm">
          <span>签名编号：<b>{{ detail.signNo }}</b></span>
          <span>对象：<b>{{ detail.bizTypeText }}</b> · {{ detail.bizNo || '—' }}</span>
          <span>患者：<b>{{ detail.patientName || '—' }}</b></span>
          <span>场景：<b>{{ detail.signSceneText }}</b>（第 {{ detail.chainNo }} 次）</span>
        </div>

        <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div class="rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs leading-6">
            <p class="font-medium text-slate-900">签名人（不可否认性依据）</p>
            <p>姓名：{{ text(detail.signerName) }}<span v-if="detail.signerTitle"> · {{ detail.signerTitle }}</span>
            </p>
            <p>科室：{{ text(detail.signerDeptName) }}</p>
            <p>证书：{{ text(detail.certNo) }}</p>
            <p>算法：{{ text(detail.signAlgo) }} / {{ text(detail.digestAlgo) }}</p>
          </div>
          <div class="rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs leading-6">
            <p class="font-medium text-slate-900">签名时间与来源</p>
            <p data-testid="p5-sign-detail-signedtime">{{ text(detail.signedTime) }}</p>
            <p>
              来源：
              <el-tag :type="timeSourceTag(detail.timeSource)" data-testid="p5-sign-detail-timesource" size="small">
                {{ detail.timeSourceText }}
              </el-tag>
            </p>
            <p class="text-amber-700">{{ text(detail.timeSourceNote) }}</p>
            <p v-if="detail.tsaSerial" data-testid="g6-detail-tsaserial">
              时间戳：{{ detail.tsaSerial }}（授时 {{ text(detail.tsaTime) }}）
            </p>
            <p v-if="detail.prevSignId">上一次签名 ID：{{ detail.prevSignId }}（本次内容已包含其摘要）</p>
          </div>
        </div>

        <div class="mt-3 rounded-lg border border-slate-200 bg-white p-3 text-xs leading-6">
          <p class="font-medium text-slate-900">内容摘要（SHA-256）</p>
          <p class="break-all font-mono text-slate-700" data-testid="p5-sign-detail-digest">
            {{ text(detail.contentDigest) }}</p>
          <p class="mt-1 text-slate-500">留存快照：{{ detail.hasSnapshot ? '是（可独立复算，不依赖业务表）' : '否' }}</p>
        </div>

        <!-- 验签：两个断言分开展示，绝不合成一个"通过/不通过" -->
        <div class="mt-4 rounded-lg border border-slate-200 bg-white p-4">
          <div class="flex flex-wrap items-center gap-3">
            <el-button :loading="verifying" data-testid="p5-sign-detail-verify" size="small" type="primary"
                       @click="doVerify">执行验签
            </el-button>
            <span class="text-xs text-slate-500">验签结论会回写库中，列表里的「验签」列随之更新。</span>
          </div>

          <div v-if="verifyResult" class="mt-3 space-y-2" data-testid="p5-sign-verify-result">
            <div class="flex flex-wrap items-center gap-2 text-xs">
              <el-tag :type="conclusionTag(verifyResult.conclusionLevel)" data-testid="p5-sign-verify-conclusion"
                      size="small">
                {{ verifyResult.conclusion }}
              </el-tag>
              <span class="text-slate-500">核查时刻 {{ text(verifyResult.checkedAt) }}</span>
            </div>
            <div class="grid grid-cols-1 gap-2 sm:grid-cols-2">
              <div
                  :class="verifyResult.signatureValid ? 'border-emerald-200 bg-emerald-50' : 'border-red-200 bg-red-50'"
                  class="rounded border px-3 py-2 text-xs">
                <p :class="verifyResult.signatureValid ? 'text-emerald-700' : 'text-red-700'"
                   :data-testid="'p5-sign-verify-sigvalid'"
                   class="font-medium">
                  ① 签名值校验：{{ verifyResult.signatureValid ? '通过' : '不通过' }}
                </p>
                <p class="mt-1 text-slate-600">用证书公钥验签名值。不通过 = 这份<b>证据本身</b>被换过，性质最严重。</p>
              </div>
              <div
                  :class="verifyResult.contentMatched ? 'border-emerald-200 bg-emerald-50' : 'border-amber-200 bg-amber-50'"
                  class="rounded border px-3 py-2 text-xs">
                <p :class="verifyResult.contentMatched ? 'text-emerald-700' : 'text-amber-700'"
                   :data-testid="'p5-sign-verify-contentmatched'"
                   class="font-medium">
                  ② 内容比对：{{ verifyResult.contentMatched ? '一致' : '已变更' }}
                </p>
                <p class="mt-1 text-slate-600">重算当前内容摘要并与签名时摘要比对。不一致 =
                  签名之后这份<b>病历被改过</b>。</p>
              </div>
            </div>
            <!-- ③ 可信时间戳：只有 time_source=3 的行有断言；失败不推翻①②，但"时间可信"不成立 -->
            <div
                v-if="verifyResult.tsaValid !== null && verifyResult.tsaValid !== undefined"
                :class="verifyResult.tsaValid ? 'border-emerald-200 bg-emerald-50' : 'border-amber-200 bg-amber-50'"
                class="rounded border px-3 py-2 text-xs"
            >
              <p :class="verifyResult.tsaValid ? 'text-emerald-700' : 'text-amber-700'" class="font-medium"
                 data-testid="g6-verify-tsavalid">
                ③ 可信时间戳：{{ verifyResult.tsaValid ? '通过' : '不通过' }}
              </p>
              <p class="mt-1 text-slate-600">序列号 {{ text(verifyResult.tsaSerial) }} · 授时时刻
                {{ text(verifyResult.tsaTime) }}</p>
              <p v-if="verifyResult.tsaNote" class="mt-1 text-slate-600">{{ verifyResult.tsaNote }}</p>
            </div>
            <div class="rounded border border-slate-200 bg-slate-50 px-3 py-2 text-xs leading-6">
              <p>签名时摘要：<span class="break-all font-mono">{{ text(verifyResult.digestAtSign) }}</span></p>
              <p>当前重算：<span class="break-all font-mono"
                                data-testid="p5-sign-verify-digestnow">{{ text(verifyResult.digestNow) }}</span></p>
            </div>
          </div>
        </div>

        <!-- 被签内容快照 -->
        <div class="mt-4 rounded-lg border border-slate-200 bg-white p-3">
          <p class="text-xs font-medium text-slate-900">被签内容快照（签名当时的原始内容）</p>
          <pre
              class="mt-2 max-h-72 overflow-auto whitespace-pre-wrap rounded bg-slate-50 p-3 text-xs leading-6 text-slate-700"
              data-testid="p5-sign-detail-snapshot">{{
              detail.hasSnapshot ? detail.contentSnapshot : '本条签名未留存内容快照'
            }}</pre>
        </div>

        <!-- 作废 -->
        <div v-if="detail.canInvalidate" class="mt-4 rounded-lg border border-red-100 bg-red-50 p-4">
          <p class="text-xs font-medium text-red-800">作废这条签名</p>
          <p class="mt-1 text-xs leading-5 text-red-700">
            作废不会删除任何历史记录：签名值、被签内容快照、签名时间全部保留，只追加作废人 / 时间 / 理由。
            作废后内容锁定解除，该文书可再次编辑并重新签名。理由必填 —— 事后要能回答「是谁撤了这份签名、为什么」。
          </p>
          <el-input v-model="invalidReason" class="mt-2" data-testid="p5-sign-invalid-reason"
                    placeholder="作废理由（必填）"/>
          <el-button :loading="invalidating" class="mt-3" data-testid="p5-sign-invalid-submit" size="small"
                     type="danger" @click="doInvalidate">确认作废
          </el-button>
        </div>
        <div v-else-if="detail.actionHint"
             class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs text-slate-500">
          {{ detail.actionHint }}
        </div>
        <div v-if="detail.invalidTime"
             class="mt-3 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs leading-6 text-slate-600">
          <p>作废人：{{ text(detail.invalidByName) }} · {{ text(detail.invalidTime) }}</p>
          <p>作废理由：{{ text(detail.invalidReason) }}</p>
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getSignatureDetail, invalidateSignature, verifySignature} from '@/api/signature';
import {text, timeSourceTag} from '@/lib/sign';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前查看的签名记录 id，抽屉打开时按 id 自取详情
  id: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailLoading = ref(false);
const detail = ref(null);
const verifyResult = ref(null);
const verifying = ref(false);
const invalidReason = ref('');
const invalidating = ref(false);

// 结论级别：1-通过 2-警告 3-失败
const conclusionTag = (l) => (l === 1 ? 'success' : l === 2 ? 'warning' : 'danger');

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  verifyResult.value = null;
  invalidReason.value = '';
  detailLoading.value = true;
  try {
    const res = await getSignatureDetail(props.id);
    detail.value = res.data;
  } catch (e) {
    ElMessage.error(e?.message || '加载签名详情失败');
  } finally {
    detailLoading.value = false;
  }
});

const doVerify = async () => {
  if (!detail.value)
    return;
  verifying.value = true;
  try {
    const res = await verifySignature(detail.value.id);
    verifyResult.value = res.data;
    // 验签会回写核查结果，列表里的"验签"列必须跟着变，否则页面自相矛盾
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '验签失败');
  } finally {
    verifying.value = false;
  }
};

const doInvalidate = async () => {
  if (!detail.value)
    return;
  if (!invalidReason.value.trim()) {
    ElMessage.warning('作废必须写理由 —— 事后要能回答「是谁撤了这份签名、为什么」');
    return;
  }
  invalidating.value = true;
  try {
    await invalidateSignature(detail.value.id, invalidReason.value.trim());
    ElMessage.success('已作废该签名（历史行保留，内容锁定已解除）');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '作废失败');
  } finally {
    invalidating.value = false;
  }
};
</script>
