<template>
  <!-- 疗程重排（未打任何一次卡时） -->
  <el-dialog v-model="visible" :title="`重排疗程：${target?.applyNo || ''}`" width="480px">
    <div class="text-xs text-gray-500 mb-3">
      还没有打过任何一次卡，因此可以整体改次数/间隔/开始日；旧排期会被重建。已开始或已产生计费痕迹的疗程只能逐次改期。
    </div>
    <el-form label-width="88px">
      <el-form-item label="总次数">
        <el-input-number v-model="form.totalTimes" :max="60" :min="1" controls-position="right"/>
      </el-form-item>
      <el-form-item label="间隔天数">
        <el-input-number v-model="form.intervalDays" :max="30" :min="1" controls-position="right"/>
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker v-model="form.startDate" style="width: 100%" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" maxlength="200"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'opd:treatmentStation:edit'" :loading="saving" type="primary"
                 @click="doApplyReschedule">保存并重排
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {treatmentApplyUpsert} from '@/api/treatment';
import {today} from '@/lib/treatment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 被重排的那一行疗程（applyId/registId/treatmentItemId 从它取，入口只在「一次卡都没打过」时给出）
  target: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({totalTimes: 1, intervalDays: 1, startDate: '', remark: ''});

watch(visible, (open) => {
  if (open) Object.assign(form, {
    totalTimes: Number(props.target?.totalTimes || 1), intervalDays: Number(props.target?.intervalDays || 1),
    startDate: props.target?.startDate || today, remark: props.target?.remark || '',
  });
});

const doApplyReschedule = async () => {
  saving.value = true;
  try {
    const res = await treatmentApplyUpsert({
      applyId: props.target.applyId,
      registId: props.target.registId,
      treatmentItemId: props.target.treatmentItemId,
      totalTimes: Number(form.totalTimes),
      intervalDays: Number(form.intervalDays),
      startDate: form.startDate,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '疗程已重排');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    saving.value = false;
  }
};
</script>
