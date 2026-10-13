<template>
  <el-dialog v-model="visible" title="质控判级" width="460px">
    <el-form label-width="110px">
      <el-form-item label="疗效判定">
        <el-select v-model="form.curativeEffect" data-testid="sd-qc-curative" style="width:100%">
          <el-option v-for="(t, k) in curative" :key="k" :label="t" :value="Number(k)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="质控意见">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'qc:singleDisease:edit'" data-testid="sd-qc-save" type="primary" @click="save">提交质控
      </el-button>
      <el-button data-testid="sd-qc-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 质控判级（提交入参含 row，逐字保留原 payload）
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {qcCase} from '@/api/singleDisease';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  curative: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = ref({});
watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  form.value = {id: row.id, curativeEffect: row.deathFlag === 1 ? 4 : null, remark: '', row};
});

const save = async () => {
  if (!form.value.curativeEffect) {
    ElMessage.warning('疗效判定必选');
    return;
  }
  const res = await qcCase(form.value);
  if (res?.code === 200) {
    const issues = res.data?.qcIssues;
    if (res.data?.qcStatus === 1)
      ElMessage.success('质控通过');
    else
      ElMessage.warning('质控异常：' + (issues || ''));
    visible.value = false;
    emit('saved');
  }
};
</script>
