<template>
  <el-dialog v-model="visible"
             :title="`录入质控结果：${row?.itemName || ''}（${levelText(row?.qcLevel)}）`" width="480px">
    <el-form label-width="100px">
      <el-form-item label="测定值" required>
        <el-input-number v-model="form.resultValue" :precision="4" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="text-xs text-gray-400 px-6">Westgard 判定由服务端按计划靶值/SD 计算，页面不做在控/失控判断。</div>
    <template #footer>
      <el-button v-perm="'medtech:lisQc:add'" :loading="saving" type="primary" @click="save">
        录入并判定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {qcInputResult} from '@/api/medicaltech';
import {dictLabelText} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  levelDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const levelText = (v) => dictLabelText(props.levelDict, v);
const saving = ref(false);
const form = reactive({resultValue: null, remark: ''});
watch(visible, (open) => {
  if (!open) return;
  Object.assign(form, {resultValue: null, remark: ''});
});
const save = async () => {
  if (form.resultValue == null) {
    ElMessage.warning('请填写测定值');
    return;
  }
  saving.value = true;
  try {
    const res = await qcInputResult({
      planId: props.row.id,
      resultValue: form.resultValue,
      remark: form.remark || undefined
    });
    if (res.code === 200) {
      const r = res.data;
      ElMessage.success(`已录入并判定：${r.statusText}${r.violatedRules ? '（' + r.violatedRules + '）' : ''}`);
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
