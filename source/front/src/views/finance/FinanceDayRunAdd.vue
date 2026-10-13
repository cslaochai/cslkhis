<template>
  <el-dialog v-model="visible" destroy-on-close title="执行日结（含三级对账预览）" width="760px">
    <div class="space-y-4" data-testid="fin-run-dialog">
      <el-form label-width="96px">
        <el-form-item label="日结日期" required>
          <el-date-picker
              v-model="runForm.settleDate"
              class="!w-48"
              data-testid="fin-run-date"
              placeholder="选择日期"
              type="date"
              value-format="YYYY-MM-DD"
              @change="loadRunPreview"
          />
          <span class="ml-3 text-xs text-slate-400">已审核的日结单不可重算</span>
        </el-form-item>
      </el-form>

      <div v-loading="runPreviewLoading" class="space-y-3">
        <template v-if="runReconcile">
          <div
              :class="runPassed ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
                : 'border-rose-200 bg-rose-50 text-rose-800'"
              class="rounded-lg border p-3 text-sm"
              data-testid="fin-run-summary"
          >
            <span class="font-semibold">{{ runPassed ? '三级对账已平' : '三级对账未平' }}</span>
            <span class="ml-2">{{ runReconcile.summary }}</span>
          </div>

          <table class="w-full border-collapse text-sm" data-testid="fin-run-recon">
            <thead>
            <tr class="bg-slate-50 text-left text-slate-500">
              <th class="border border-slate-200 px-3 py-2">级次</th>
              <th class="border border-slate-200 px-3 py-2">左口径</th>
              <th class="border border-slate-200 px-3 py-2 text-right">左金额</th>
              <th class="border border-slate-200 px-3 py-2">右口径</th>
              <th class="border border-slate-200 px-3 py-2 text-right">右金额</th>
              <th class="border border-slate-200 px-3 py-2 text-right">差额</th>
              <th class="border border-slate-200 px-3 py-2">结论</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="it in runReconcile.items || []"
                :key="it.level"
                :data-testid="`fin-run-recon-${it.level}`"
            >
              <td class="border border-slate-200 px-3 py-2">{{ it.levelName }}</td>
              <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.leftLabel }}</td>
              <td class="border border-slate-200 px-3 py-2 text-right font-mono">
                ¥{{ formatMoney(it.leftAmount) }}
              </td>
              <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.rightLabel }}</td>
              <td class="border border-slate-200 px-3 py-2 text-right font-mono">
                ¥{{ formatMoney(it.rightAmount) }}
              </td>
              <td
                  :class="it.passed ? 'text-emerald-600' : 'text-rose-600'"
                  class="border border-slate-200 px-3 py-2 text-right font-mono"
              >
                {{ formatMoney(it.diffAmount) }}
              </td>
              <td class="border border-slate-200 px-3 py-2">
                <el-tag :class="reconcileTagClass(it.passed ? 1 : 2)" class="border" effect="plain" size="small">
                  {{ it.passed ? '平' : '差异' }}
                </el-tag>
              </td>
            </tr>
            </tbody>
          </table>

          <div
              v-for="it in runReconcile.items || []"
              v-show="!it.passed"
              :key="`msg-${it.level}`"
              class="rounded border border-rose-200 bg-rose-50 p-2 text-xs text-rose-700"
          >
            {{ it.levelName }}：{{ it.message }}
          </div>
        </template>
        <div v-else-if="!runPreviewLoading" class="py-8 text-center text-sm text-slate-400">
          选择日期后自动试算当日对账结果
        </div>
      </div>

      <div
          v-if="runReconcile && !runPassed"
          class="space-y-3 rounded-lg border border-amber-200 bg-amber-50 p-3"
      >
        <el-checkbox v-model="runForm.allowDiff" data-testid="fin-run-allowdiff">
          差异确认无误，放行出单（放行后差异写进日结单留痕）
        </el-checkbox>
        <el-input
            v-model="runForm.remark"
            :rows="2"
            data-testid="fin-run-remark"
            placeholder="放行理由（必填）：如跨日退费、渠道延迟到账"
            type="textarea"
        />
      </div>
    </div>
    <template #footer>
      <el-button
          v-perm="'finance:settlement:edit'"
          :disabled="!!runReconcile && !runPassed && !runForm.allowDiff"
          :loading="runSubmitting"
          data-testid="fin-run-submit"
          type="primary"
          @click="submitRun"
      >
        确认日结
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDayDetailByDate, runDaySettlement} from '@/api/financeSettlement';
import {reconcileTagClass} from '@/lib/financeSettlement';
import {formatMoney, localDateStr} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const runSubmitting = ref(false);
const runPreviewLoading = ref(false);
const runDetail = ref(null);
const runForm = reactive({
  settleDate: localDateStr(),
  allowDiff: false,
  remark: '',
});
const runReconcile = computed(() => runDetail.value?.reconcile || null);
const runPassed = computed(() => runReconcile.value?.passed === true);

async function loadRunPreview() {
  if (!runForm.settleDate)
    return;
  runPreviewLoading.value = true;
  runDetail.value = null;
  try {
    const res = await getDayDetailByDate(runForm.settleDate);
    runDetail.value = res.data || null;
    // 有差异时默认不勾放行 —— 逼人先看清楚差在哪，而不是顺手点过去
    if (res.data?.reconcile?.passed !== true) {
      runForm.allowDiff = false;
    }
  } catch (e) {
    runDetail.value = null;
    ElMessage.error(e?.message || '加载对账预览失败');
  } finally {
    runPreviewLoading.value = false;
  }
}

watch(visible, (open) => {
  if (!open) return;
  runForm.settleDate = localDateStr();
  runForm.allowDiff = false;
  runForm.remark = '';
  loadRunPreview();
});

async function submitRun() {
  if (!runForm.settleDate) {
    ElMessage.warning('请选择日结日期');
    return;
  }
  if (!runPassed.value && !runForm.allowDiff) {
    ElMessage.warning('对账未平。确认差异无误请勾选「放行差异」并填写理由');
    return;
  }
  if (!runPassed.value && !runForm.remark.trim()) {
    ElMessage.warning('放行差异必须填写理由');
    return;
  }
  runSubmitting.value = true;
  try {
    const res = await runDaySettlement({...runForm});
    const vo = res.data || {};
    if (vo.reconcileStatus === 2) {
      ElMessage.warning(`日结完成（${vo.settlementNo}）对账有差异，已记录差异说明`);
    } else {
      ElMessage.success(`日结完成（${vo.settlementNo}）三级对账全部通过`);
    }
    visible.value = false;
    emit('saved', vo);
  } catch (e) {
    ElMessage.error(e?.message || '日结失败');
  } finally {
    runSubmitting.value = false;
  }
}
</script>
