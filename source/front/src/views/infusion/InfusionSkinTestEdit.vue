<template>
  <!-- 打皮试：对待皮试的输液单登记皮试药物 -->
  <el-dialog v-model="visible" title="打皮试" width="420px">
    <el-form label-width="90px">
      <el-form-item label="皮试药物">
        <el-input v-model="form.drugName" data-testid="ir-skin-drug"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="ir-skin-cancel" @click="visible = false">取消</el-button>
      <el-button v-perm="'medtech:infusion:edit'" data-testid="ir-skin-save" type="primary" @click="saveSkinTest">
        确认打皮试
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createSkinTest} from '@/api/infusionRoom';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 要打皮试的输液单行
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
  form.value = {infusionId: props.row?.id, drugName: '', remark: ''};
});

const saveSkinTest = async () => {
  const res = await createSkinTest(form.value);
  if (res?.code === 200) {
    ElMessage.success('皮试已打，观察 15 分钟后判读');
    visible.value = false;
    emit('saved');
  }
};
</script>
