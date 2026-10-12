<template>
  <el-dialog v-model="visible" title="病种维护" width="460px">
    <el-form label-width="110px">
      <el-form-item label="病种编码">
        <el-input v-model="form.diseaseCode" data-testid="sd-code"/>
      </el-form-item>
      <el-form-item label="病种名称">
        <el-input v-model="form.diseaseName" data-testid="sd-name"/>
      </el-form-item>
      <el-form-item label="纳入 ICD 前缀">
        <el-input v-model="form.icd10Prefix" data-testid="sd-prefix" placeholder="如 I21,I22"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="sd-disease-cancel" @click="visible = false">取消</el-button>
      <el-button v-perm="'qc:singleDisease:add'" data-testid="sd-disease-save" type="primary" @click="save">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 病种维护（新增 + 编辑同一 upsert，row 为空即新增）
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertDisease} from '@/api/singleDisease';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = ref({});
watch(visible, (open) => {
  if (!open) return;
  form.value = props.row ? {...props.row} : {id: null, diseaseCode: '', diseaseName: '', icd10Prefix: '', remark: ''};
});

const save = async () => {
  const res = await upsertDisease(form.value);
  if (res?.code === 200) {
    ElMessage.success('病种已保存');
    visible.value = false;
    emit('saved');
  }
};
</script>
