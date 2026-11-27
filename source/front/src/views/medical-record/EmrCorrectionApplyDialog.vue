<template>
  <el-dialog v-model="visible" title="申请病历更正" data-testid="correction-apply-dialog" width="640px">
    <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="已提交/归档病历原则上只读；更正需经科室初审、医务部门终审批准后方可回写，修改人与时间全程留痕。"
    />
    <el-form label-width="90px">
      <el-form-item label="病历">
        <div class="w-full space-y-1 text-xs text-slate-600">
          <div class="flex items-center gap-2">
            <span class="font-mono">{{ record?.recordNo }}</span>
            <span>{{ record?.patientName }}</span>
            <span class="text-slate-400">原经治：{{ record?.doctorName || '' }}</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="更正类型" required>
        <el-radio-group v-model="form.correctionType" data-testid="correction-type">
          <el-radio :value="1">经治医生更正</el-radio>
          <el-radio :value="2">上级代改</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="更正原因" required>
        <el-input
            v-model="form.reason"
            :rows="3"
            data-testid="correction-reason"
            maxlength="500"
            placeholder="说明需要更正的事项与事由"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="拟更正内容">
        <el-input
            v-model="form.plannedContent"
            :rows="3"
            data-testid="correction-planned"
            maxlength="2000"
            placeholder="拟修改的具体内容（可空，审批通过后在授权范围内回写）"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
            v-model="form.remark"
            :rows="2"
            data-testid="correction-remark"
            maxlength="500"
            placeholder="补充说明（可空）"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="save">提交申请</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {applyCorrection} from '@/api/emrCorrection';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 选中的病历行（已提交 2 / 已归档 3）
  record: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  correctionType: 1,
  reason: '',
  plannedContent: '',
  remark: '',
});

watch(() => props.modelValue, (v) => {
  if (!v) return;
  form.correctionType = 1;
  form.reason = '';
  form.plannedContent = '';
  form.remark = '';
});

const save = async () => {
  if (!props.record?.id) {
    ElMessage.warning('请先选择病历');
    return;
  }
  if (!form.reason?.trim()) {
    ElMessage.warning('请填写更正原因');
    return;
  }
  saving.value = true;
  try {
    await applyCorrection({
      recordId: props.record.id,
      correctionType: form.correctionType,
      reason: form.reason.trim(),
      plannedContent: form.plannedContent?.trim() || undefined,
      remark: form.remark?.trim() || undefined,
    });
    ElMessage.success('更正申请已提交');
    visible.value = false;
    emit('saved');
  } catch (error) {
    console.error('提交更正申请失败:', error);
  } finally {
    saving.value = false;
  }
};
</script>
