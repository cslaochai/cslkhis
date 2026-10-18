<template>
  <el-dialog v-model="visible" destroy-on-close title="办理交班（班结）" width="560px">
    <div class="space-y-4" data-testid="fin-handover-dialog">
      <div class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
        交班单是定格凭证：金额按「上次交班时刻 → 现在」这个区间汇总，
        生成后不随后续补录变动。班次只是展示标签，不影响统计区间。
      </div>
      <el-form label-width="96px">
        <el-form-item label="班次" required>
          <el-select v-model="handoverForm.shiftType" class="w-full" data-testid="fin-handover-shift">
            <el-option v-for="o in SHIFT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="本班系统账">
          <div class="w-full rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs leading-6" data-testid="fin-handover-preview">
            <template v-if="preview.loading">正在试算本班系统账…</template>
            <template v-else-if="preview.error">
              <span class="text-amber-700">试算失败，不影响交班 —— 提交时仍会按系统现金校验差异。</span>
            </template>
            <template v-else>
              <div class="text-slate-500">统计区间：{{ preview.periodBegin || '当日 00:00' }} → {{ preview.periodEnd }}</div>
              <div>
                系统现金
                <span class="font-mono text-sm font-medium text-slate-800">¥{{ formatMoney(preview.cashAmount) }}</span>
                <span class="text-slate-400">（点钞要对账的数）</span>
              </div>
              <div class="text-slate-500">
                收费 {{ preview.chargeCount }} 笔 ¥{{ formatMoney(preview.chargeAmount) }}；
                退费 {{ preview.refundCount }} 笔 ¥{{ formatMoney(preview.refundAmount) }}
              </div>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="实交现金" required>
          <el-input-number
              v-model="handoverForm.handinCash"
              :min="0"
              :precision="2"
              :step="10"
              class="!w-full"
              data-testid="fin-handover-cash"
          />
          <div v-if="cashDiffCents !== null" class="text-xs leading-6" data-testid="fin-handover-diff">
            <span v-if="cashDiffCents === 0" class="text-emerald-700">账实相符</span>
            <span v-else class="text-amber-700">
              与系统现金差异 {{ cashDiffText }} 元 —— 请在下方填写差异说明，否则无法提交
            </span>
          </div>
        </el-form-item>
        <el-form-item label="差异说明">
          <el-input
              v-model="handoverForm.diffReason"
              :rows="2"
              data-testid="fin-handover-reason"
              placeholder="实交现金与系统现金不一致时必填（后端强校验）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="handoverForm.remark" :rows="2" placeholder="选填" type="textarea"/>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button
          v-perm="'finance:settlement:edit'"
          :loading="handoverSubmitting"
          data-testid="fin-handover-submit"
          type="primary"
          @click="submitHandover"
      >
        确认交班
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {handover, handoverPreview} from '@/api/financeSettlement';
import {SHIFT_TYPE_OPTIONS, defaultShiftType} from '@/lib/financeSettlement';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const handoverSubmitting = ref(false);
const handoverForm = reactive({
  shiftType: 1,
  handinCash: null,
  diffReason: '',
  remark: '',
});

// 本班系统账试算（handoverPreview，不落库）。cashAmount = 点钞要对账的数。
// error 时降级：只隐藏预览与实时比对，交班本身仍可走（后端提交时强校验差异）。
const preview = reactive({
  loading: false,
  error: false,
  periodBegin: '',
  periodEnd: '',
  chargeCount: 0,
  chargeAmount: 0,
  refundCount: 0,
  refundAmount: 0,
  cashAmount: 0,
});
const previewReady = computed(() => !preview.loading && !preview.error);

// 差异按「分」算再折回元：JS Number 直接相减会有 0.1+0.2 式浮点误差，
// 而这里差一分钱就要弹说明，容不得这种噪声。
const cashDiffCents = computed(() => {
  if (!previewReady.value) return null;
  const handin = handoverForm.handinCash;
  if (handin === null || handin === undefined) return null;
  return Math.round(Number(handin) * 100) - Math.round(Number(preview.cashAmount) * 100);
});
const cashDiffText = computed(() => {
  if (cashDiffCents.value === null) return '';
  const diff = cashDiffCents.value / 100;
  return (diff > 0 ? '+' : '') + formatMoney(diff);
});

watch(visible, (open) => {
  if (!open) return;
  handoverForm.shiftType = defaultShiftType();
  handoverForm.handinCash = null;
  handoverForm.diffReason = '';
  handoverForm.remark = '';
  loadPreview();
});

async function loadPreview() {
  preview.loading = true;
  preview.error = false;
  try {
    const res = await handoverPreview();
    const d = res.data || {};
    preview.periodBegin = d.periodBegin || '';
    preview.periodEnd = d.periodEnd || '';
    preview.chargeCount = d.chargeCount ?? 0;
    preview.chargeAmount = d.chargeAmount ?? 0;
    preview.refundCount = d.refundCount ?? 0;
    preview.refundAmount = d.refundAmount ?? 0;
    preview.cashAmount = d.cashAmount ?? 0;
  } catch (e) {
    // 预览挂了不堵交班：降级成「提交时才知道差异」，与改造前一致
    preview.error = true;
  } finally {
    preview.loading = false;
  }
}

async function submitHandover() {
  if (handoverForm.shiftType === null) {
    ElMessage.warning('请选择班次');
    return;
  }
  if (handoverForm.handinCash === null || Number(handoverForm.handinCash) < 0) {
    ElMessage.warning('请填写实际清点并上交的现金金额');
    return;
  }
  // 前端软拦：能提前看到的差异不留给后端拒。后端强校验仍在，这里只省一次往返。
  if (cashDiffCents.value !== null && cashDiffCents.value !== 0 && !handoverForm.diffReason.trim()) {
    ElMessage.warning(`实交现金与系统现金差异 ${cashDiffText.value} 元，请先填写差异说明`);
    return;
  }
  handoverSubmitting.value = true;
  try {
    const res = await handover({
      shiftType: handoverForm.shiftType,
      handinCash: handoverForm.handinCash,
      diffReason: handoverForm.diffReason.trim() || undefined,
      remark: handoverForm.remark.trim() || undefined,
      // cashierId 不传：服务端取登录态，前端传「我是谁」等于让交班单能挂到别人名下
    });
    const vo = res.data || {};
    const diff = Number(vo.cashDiff ?? 0);
    if (diff === 0) {
      ElMessage.success(`交班成功（${vo.settlementNo || ''}）现金账实相符`);
    } else {
      ElMessage.warning(`交班成功（${vo.settlementNo || ''}）现金差异 ${diff > 0 ? '+' : ''}${formatMoney(diff)} 元，已留痕`);
    }
    visible.value = false;
    emit('saved', vo);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    handoverSubmitting.value = false;
  }
}
</script>
