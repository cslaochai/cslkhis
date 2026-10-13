<template>
  <el-dialog v-model="visible" title="手工纳入病例" width="460px">
    <el-form label-width="110px">
      <el-form-item label="病种">
        <el-select v-model="form.diseaseId" data-testid="sd-enroll-disease" style="width:100%">
          <el-option v-for="d in diseases" :key="d.id" :label="d.diseaseName" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="住院ID">
        <el-input v-model.number="form.admissionId" data-testid="sd-enroll-admission"
                  placeholder="biz_admission.admission_id"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'qc:singleDisease:edit'" data-testid="sd-enroll-save" type="primary" @click="save">
        纳入
      </el-button>
      <el-button data-testid="sd-enroll-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 手工纳入病例（只有纳入语义，无修改）
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {enrollCase} from '@/api/singleDisease';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  diseases: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = ref({});
watch(visible, (open) => {
  if (!open) return;
  form.value = {diseaseId: null, admissionId: null};
});

const save = async () => {
  const f = form.value;
  if (!f.diseaseId || !f.admissionId) {
    ElMessage.warning('病种与住院ID必填');
    return;
  }
  const res = await enrollCase(f);
  if (res?.code === 200) {
    ElMessage.success(`已纳入（病例 ${res.data?.caseNo}）`);
    visible.value = false;
    emit('saved');
  }
};
</script>
