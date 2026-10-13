<template>
  <el-dialog v-model="visible" :title="form.id ? '修改规格' : '新增规格'" destroy-on-close width="460px">
    <el-form label-width="90px">
      <el-form-item label="规格编码">
        <el-input v-model="form.specCode" :disabled="!!form.id" placeholder="如 F-14X17"/>
      </el-form-item>
      <el-form-item label="规格名称">
        <el-input v-model="form.specName" placeholder="如 14×17英寸激光胶片"/>
      </el-form-item>
      <el-form-item label="单价">
        <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :step="1"/>
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="form.unit" class="!w-24"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" class="!w-32">
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="停用"/>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="doSaveSpec">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {filmSpecUpsert} from '@/api/medicaltech';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑时传当前规格行；新增时传空
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = ref({id: undefined, specCode: '', specName: '', unitPrice: 0, unit: '张', sortOrder: 0, status: 1});

watch(visible, (v) => {
  if (!v) return;
  form.value = props.row
      ? {...props.row}
      : {id: undefined, specCode: '', specName: '', unitPrice: 0, unit: '张', sortOrder: 0, status: 1};
});

const doSaveSpec = async () => {
  if (!form.value.specCode?.trim() || !form.value.specName?.trim()) {
    return ElMessage.warning('规格编码与名称必填');
  }
  try {
    await filmSpecUpsert(form.value);
    ElMessage.success('规格已保存');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  }
};
</script>
