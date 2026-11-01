<template>
  <!-- ---------------- 证书申请进度（流程图） ---------------- -->
  <el-dialog v-model="visible" :title="`证书申请进度 · ${certNo}`" width="780px" @closed="onClosed">
    <div v-loading="loading" class="text-sm">
      <p v-if="!certDetail" class="text-xs text-slate-400">加载中…</p>

      <!-- 流程图：生成密钥对 → CA 签发 → 回填生效 -->
      <div v-if="certDetail" class="apply-flow" data-testid="p5-sign-flow">
        <template v-for="(stage, idx) in stages" :key="idx">
          <div class="apply-flow__node" :class="`is-${stage.cls}`" :data-testid="`p5-sign-flow-stage-${idx}`">
            <div class="apply-flow__head">
              <span class="apply-flow__idx">{{ idx + 1 }}</span>
              <span class="apply-flow__title">{{ stage.title }}</span>
            </div>
            <el-tag :type="stage.tagType" effect="light" size="small">{{ stage.statusText }}</el-tag>
            <div class="apply-flow__time">{{ stage.time }}</div>
            <div v-if="stage.note" class="apply-flow__note">{{ stage.note }}</div>
          </div>
          <div v-if="idx < stages.length - 1" class="apply-flow__arrow" aria-hidden="true">
            <el-icon>
              <Right/>
            </el-icon>
          </div>
        </template>
      </div>

      <!-- 线下送 CA 指引（无在线申请单时展示：说明要传什么、怎么回填） -->
      <div v-if="certDetail && !latestApply"
           class="mt-4 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs leading-6">
        <p class="font-medium text-amber-800">该证书未发起 CA 在线申请，当前走线下送 CA 流程</p>
        <p class="mt-1 text-slate-700">HIS 无法自动查询 CA 结果，请按以下步骤让证书生效并展示 CA 处理记录：</p>
        <ol class="mt-1 list-decimal pl-5 text-slate-700">
          <li>复制下方「CA 申请材料」（主体 DN + 公钥 PEM）；</li>
          <li>到 CA 平台「证书申请」页粘贴提交，等待 CA 审核签发；</li>
          <li>CA 签发后在 CA 平台「详情」下载/复制证书 PEM（含 <span class="font-mono">BEGIN CERTIFICATE</span> 段）；</li>
          <li>回 HIS → 办理签名证书 → 改为线下回填 → 粘贴证书 PEM，证书即生效，CA 序列号/签发者也会在此展示。</li>
        </ol>
        <div class="mt-2">
          <el-button :disabled="!certDetail?.publicKey" link size="small" type="primary"
                     data-testid="p5-sign-flow-copy-apply" @click="copyApplyMaterial">复制 CA 申请材料（DN + 公钥）
          </el-button>
        </div>
        <div class="mt-2 grid grid-cols-1 gap-2">
          <div>
            <p class="text-slate-500">主体 DN</p>
            <el-input :model-value="certDetail?.caSubjectDn || ''" readonly size="small"/>
          </div>
          <div>
            <p class="text-slate-500">公钥 PEM</p>
            <el-input :model-value="certDetail?.publicKey || ''" :rows="4" readonly size="small" type="textarea"/>
          </div>
        </div>
      </div>

      <!-- CA 申请记录（与详情一致的逐次申请记录） -->
      <div class="mt-4 rounded-lg border border-slate-200 bg-white p-3">
        <div class="flex items-center justify-between">
          <p class="text-xs font-medium text-slate-900">CA 申请记录</p>
          <el-button v-if="latestApply" :loading="refreshing" link size="small" type="primary"
                     data-testid="p5-sign-flow-refresh" @click="refreshStatus">刷新状态
          </el-button>
        </div>
        <p v-if="!caApplies.length" class="mt-2 text-xs text-slate-500">
          未向 CA 发起过在线申请；该证书走线下送 CA 签发后回填。
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

      <p class="mt-3 text-xs text-slate-400">
        说明：证书提交 CA 申请后进入「待审核」，CA 签发后需回填到本系统才会「生效」。线上申请可在上方「CA 申请记录」处刷新进度；
        线下签发的证书在「办理签名证书」里粘贴证书 PEM 回填（见上方黄色指引）。
      </p>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Right} from '@element-plus/icons-vue';
import {getSignCertDetail, listSignCaApplies, refreshSignCaApply} from '@/api/signature';
import {caApplyTagType, num, text} from '@/lib/sign';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前查看的证书 id
  id: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const refreshing = ref(false);
const certDetail = ref(null);
const caApplies = ref([]);

const certNo = computed(() => certDetail.value?.certNo || '');

// 最近的 CA 申请（按 applySeq 倒序取第一条）
const latestApply = computed(() => {
  const list = caApplies.value || [];
  if (!list.length) return null;
  return list.slice().sort((a, b) => Number(b.applySeq) - Number(a.applySeq))[0];
});

// 三阶段流程图（生成密钥对 → CA 签发 → 回填生效）
const stages = computed(() => {
  const d = certDetail.value || {};
  const latest = latestApply.value;
  const active = d.certStatus === 1;

  const node1 = {
    title: '生成密钥对',
    cls: 'success',
    tagType: 'success',
    statusText: '已完成',
    time: '',
    note: '密钥对已在本地生成，私钥加密托管',
  };

  let node2;
  if (active) {
    node2 = {title: 'CA 签发', cls: 'success', tagType: 'success', statusText: '已签发', time: text(latest?.issuedAt), note: ''};
  } else if (latest) {
    const s = latest.applyStatus;
    if (s === 'PENDING') {
      node2 = {title: 'CA 签发', cls: 'warning', tagType: 'warning', statusText: '待审核', time: text(latest.submittedAt), note: latest.applyMessage || latest.caMessage || '已提交 CA，等待审核签发'};
    } else if (s === 'APPROVED') {
      node2 = {title: 'CA 签发', cls: 'success', tagType: 'success', statusText: '已批准待签发', time: text(latest.reviewedAt), note: latest.caMessage || ''};
    } else if (s === 'ISSUED') {
      node2 = {title: 'CA 签发', cls: 'success', tagType: 'success', statusText: '已签发·待回填', time: text(latest.issuedAt), note: 'CA 已签发，回填证书后即生效'};
    } else if (s === 'REJECTED') {
      node2 = {title: 'CA 签发', cls: 'danger', tagType: 'danger', statusText: '已驳回', time: text(latest.reviewedAt), note: latest.caMessage || '被 CA 驳回，可在办理证书里重新提交'};
    } else if (s === 'FAILED') {
      node2 = {title: 'CA 签发', cls: 'danger', tagType: 'danger', statusText: '提交失败', time: text(latest.submittedAt), note: latest.caMessage || '向 CA 提交失败，可重新提交'};
    } else if (s === 'CANCELLED') {
      node2 = {title: 'CA 签发', cls: 'info', tagType: 'info', statusText: '已撤销', time: text(latest.reviewedAt), note: ''};
    } else {
      node2 = {title: 'CA 签发', cls: 'info', tagType: 'info', statusText: text(latest.applyStatusText), time: text(latest.submittedAt), note: ''};
    }
  } else {
    node2 = {title: 'CA 签发', cls: 'info', tagType: 'info', statusText: '线下送签·待办理', time: '', note: '未发起在线申请，需线下送 CA 后回填'};
  }

  let node3;
  if (active) {
    node3 = {title: '回填生效', cls: 'success', tagType: 'success', statusText: '已生效', time: text(latest?.boundAt) || text(d.validFrom), note: ''};
  } else if (latest && (latest.applyStatus === 'ISSUED' || latest.applyStatus === 'APPROVED')) {
    node3 = {title: '回填生效', cls: 'warning', tagType: 'warning', statusText: '待回填', time: '', note: 'CA 已签发，回填证书后即生效'};
  } else {
    node3 = {title: '回填生效', cls: 'info', tagType: 'info', statusText: '待办理', time: '', note: '证书生效前不可用于电子签名'};
  }

  return [node1, node2, node3];
});

const loadData = async () => {
  if (!props.id) return;
  loading.value = true;
  try {
    const res = await getSignCertDetail(props.id);
    certDetail.value = res.data;
    const applies = await listSignCaApplies(props.id);
    caApplies.value = applies.data || [];
  } catch (e) {
    console.error('加载证书申请进度失败', e);
  } finally {
    loading.value = false;
  }
};

// 流程图内的「刷新状态」：重新拉取 CA 申请状态，已签发会自动回填生效
const refreshStatus = async () => {
  refreshing.value = true;
  try {
    const res = await refreshSignCaApply(props.id);
    const data = res.data || {};
    if (data.certStatus === 1) {
      ElMessage.success(`CA 已签发，证书已生效 ${data.certNo || ''}`);
    } else {
      ElMessage.success(`CA 状态：${data.applyStatusText || data.applyStatus || '未知'}`);
      if (data.applyMessage) ElMessage.info(data.applyMessage);
    }
    await loadData();
    emit('saved');
  } catch (e) {
    console.error('刷新 CA 申请状态失败', e);
  } finally {
    refreshing.value = false;
  }
};

watch(visible, async (open) => {
  if (open) await loadData();
});

// 一键复制 CA 申请材料（Subject DN + 公钥 PEM）：拿去 CA 平台「证书申请」粘贴提交
const copyApplyMaterial = async () => {
  const d = certDetail.value || {};
  const material = `Subject DN: ${d.caSubjectDn || ''}\nPublic Key PEM:\n${d.publicKey || ''}`;
  try {
    await navigator.clipboard.writeText(material);
    ElMessage.success('已复制申请材料，可到 CA 平台证书申请页粘贴提交');
  } catch (e) {
    ElMessage.warning('复制失败，请手动选中上方文本复制');
  }
};

const onClosed = () => {
  certDetail.value = null;
  caApplies.value = [];
};
</script>

<style scoped>
.apply-flow {
  display: flex;
  align-items: stretch;
  flex-wrap: wrap;
  gap: 0;
}

.apply-flow__node {
  flex: 1 1 200px;
  min-width: 180px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #64748b;
  border-radius: 8px;
  padding: 12px 14px;
  margin: 4px 0;
}

.apply-flow__node.is-success {
  border-left-color: #16a34a;
}

.apply-flow__node.is-warning {
  border-left-color: #d97706;
}

.apply-flow__node.is-danger {
  border-left-color: #dc2626;
}

.apply-flow__node.is-info {
  border-left-color: #64748b;
}

.apply-flow__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.apply-flow__idx {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 9999px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
}

.apply-flow__title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.apply-flow__time {
  margin-top: 8px;
  font-size: 12px;
  color: #475569;
}

.apply-flow__note {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.apply-flow__arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  color: #94a3b8;
  font-size: 20px;
}
</style>
