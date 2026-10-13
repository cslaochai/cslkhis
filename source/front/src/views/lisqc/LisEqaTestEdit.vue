<template>
  <el-dialog v-model="visible" :title="`录入本室检测结果：${row?.sampleNo || ''}（${row?.itemName || ''}）`"
             width="480px">
    <el-form label-width="100px">
      <el-form-item label="本室测定值" required>
        <el-input-number v-model="form.testValue" :precision="4" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="text-xs text-gray-400 px-6">盲样须按常规标本流程上机检测，靶值由组织方回报，录入时不可知。</div>
    <template #footer>
      <el-button v-perm="'medtech:lisEqa:edit'" :loading="saving" type="primary" @click="save">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {eqaSampleTest} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({testValue: null, remark: ''});
watch(visible, (open) => {
  if (!open) return;
  Object.assign(form, {testValue: props.row?.testValue ?? null, remark: props.row?.remark || ''});
});
const save = async () => {
  if (form.testValue == null) {
    ElMessage.warning('请填写本室测定值');
    return;
  }
  saving.value = true;
  try {
    const res = await eqaSampleTest({
      sampleId: props.row.id, testValue: form.testValue, remark: form.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已录入');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '录入失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('录入失败');
  } finally {
    saving.value = false;
  }
};
</script>
