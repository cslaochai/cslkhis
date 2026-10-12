<template>
  <!-- 单条改期 -->
  <el-dialog v-model="visible"
             :title="`改期：第 ${target?.execSeq || ''} 次（${target?.itemName || ''}）`"
             width="480px">
    <el-form label-width="88px">
      <el-form-item label="改到">
        <span data-testid="resch-date" style="display: contents">
          <el-date-picker v-model="form.planDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </span>
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="form.reason" :rows="2" data-testid="resch-reason" maxlength="200"
                  placeholder="如：患者外出，顺延一天" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'opd:treatmentStation:edit'" :loading="saving" data-testid="resch-submit"
                 type="primary" @click="doReschedule">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {treatmentExecReschedule} from '@/api/treatment';
import {today} from '@/lib/treatment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 被改期的那一行待执行流水（只有待执行次才给「改期」入口，判断留在父页面与后端）
  target: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({planDate: '', reason: ''});

watch(visible, (open) => {
  if (open) Object.assign(form, {planDate: props.target?.planDate || today, reason: ''});
});

const doReschedule = async () => {
  if (!form.planDate) {
    ElMessage.warning('请选择改到哪天');
    return;
  }
  if (!form.reason.trim()) {
    ElMessage.warning('改期原因必填');
    return;
  }
  saving.value = true;
  try {
    const res = await treatmentExecReschedule({
      recordId: props.target.recordId, planDate: form.planDate, reason: form.reason.trim(),
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已改期');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '改期失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('改期失败');
  } finally {
    saving.value = false;
  }
};
</script>
