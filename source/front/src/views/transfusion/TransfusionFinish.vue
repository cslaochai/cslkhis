<template>
  <el-dialog
      v-model="visible"
      data-testid="p4-tf-finish-dialog"
      title="登记输血完成（回写输血记录病历 + 病案首页是否输血）"
      width="700px"
  >
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">{{ text(row.patientName) }}</div>
        <div class="text-slate-700">
          {{ text(row.bloodTypeText) }} · {{ text(row.bloodComponentText) }} ·
          {{ text(row.bagProgressText) }}
        </div>
        <div class="text-[12px] text-slate-500">
          双人核对：{{ text(row.checkNurseName) }} / {{ text(row.checkNurse2Name) }} ·
          开始 {{ fmt(row.infusionStartTime) }}
        </div>
      </div>
      <el-form label-width="120px">
        <el-form-item label="输注结束时间" required>
          <el-date-picker
              v-model="finishForm.infusionEndTime"
              class="!w-full"
              data-testid="p4-tf-finish-time"
              placeholder="选择结束时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="实际输注量" required>
          <el-input
              v-model.number="finishForm.actualAmount"
              class="!w-40"
              data-testid="p4-tf-finish-amount"
              placeholder="必须大于 0"
          />
          <span class="ml-2 text-[12px] text-slate-500">{{ text(row.amountUnit) }}</span>
        </el-form-item>
        <el-form-item label="输注观察" required>
          <el-input
              v-model="finishForm.observation"
              :rows="3"
              data-testid="p4-tf-finish-observation"
              placeholder="输注过程与输注后观察：生命体征、有无寒战发热皮疹等（开始后 15 分钟是反应高发期）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="疗效评估">
          <el-input
              v-model="finishForm.efficacyEval"
              :rows="2"
              placeholder="如：输注后心悸气促改善，复查 Hb 升至 85 g/L"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="输血后复查">
          <div class="flex items-center gap-3">
            <el-input v-model.number="finishForm.postHb" class="!w-40" placeholder="Hb g/L"/>
            <el-input v-model.number="finishForm.postHct" class="!w-40" placeholder="HCT %"/>
            <el-input v-model.number="finishForm.postPlt" class="!w-48" placeholder="PLT ×10⁹/L"/>
          </div>
        </el-form-item>
      </el-form>
      <el-alert :closable="false" show-icon type="info">
        完成后系统会回写一份<b>输血记录</b>病历（record_type=11），并把病案首页「是否输血」置为是。
        如输注后出现不良反应，请用列表里的「上报反应」补登记（不改回本单状态）。
      </el-alert>
    </div>
    <template #footer>
      <el-button
          v-perm="'medtech:transfusion:edit'"
          :loading="finishSubmitting"
          data-testid="p4-tf-finish-submit"
          type="primary"
          @click="submitFinish"
      >
        确认完成并回写
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 五、完成（回写病历） ----------------
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {finishTransfusion} from '@/api/inpatientTransfusion';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const finishSubmitting = ref(false);
const finishForm = reactive({
  infusionEndTime: '',
  actualAmount: undefined,
  observation: '',
  efficacyEval: '',
  postHb: undefined,
  postHct: undefined,
  postPlt: undefined,
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  finishForm.infusionEndTime = '';
  finishForm.actualAmount = row.plannedAmount;
  finishForm.observation = row.observation || '';
  finishForm.efficacyEval = '';
  finishForm.postHb = undefined;
  finishForm.postHct = undefined;
  finishForm.postPlt = undefined;
});

const submitFinish = async () => {
  if (!props.row)
    return;
  if (!finishForm.infusionEndTime) {
    ElMessage.warning('请选择输注结束时间');
    return;
  }
  if (!finishForm.actualAmount || finishForm.actualAmount <= 0) {
    ElMessage.warning('实际输注量必须大于 0');
    return;
  }
  if (!finishForm.observation.trim()) {
    ElMessage.warning('请填写输注过程观察（开始后 15 分钟是反应高发期）');
    return;
  }
  finishSubmitting.value = true;
  try {
    await finishTransfusion({
      applyId: props.row.id,
      infusionEndTime: finishForm.infusionEndTime,
      actualAmount: finishForm.actualAmount,
      observation: finishForm.observation.trim(),
      efficacyEval: finishForm.efficacyEval.trim() || undefined,
      postHb: finishForm.postHb,
      postHct: finishForm.postHct,
      postPlt: finishForm.postPlt,
    });
    ElMessage.success('输血已完成（已回写输血记录病历与病案首页是否输血标志）');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    finishSubmitting.value = false;
  }
};
</script>
