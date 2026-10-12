<template>
  <!-- 处置 -->
  <el-dialog v-model="visible" :title="`处置确认 — ${disposeForm.wasteNo}`" width="440px">
    <el-form label-width="90px">
      <el-form-item label="处置公司" required>
        <el-input v-model="disposeForm.disposalCompany" style="width: 260px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'asset:waste:edit'" type="primary" @click="submitDispose">确认处置</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 处置确认：已交接(2) → 已处置(3) 闭环，不可逆（三态闭环见 WasteView 页头注释）
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {wasteDispose} from '@/api/waste';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 要确认处置的登记行
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const disposeForm = reactive({id: null, wasteNo: '', disposalCompany: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(disposeForm, {
    id: Number(props.row?.id), wasteNo: props.row?.wasteNo, disposalCompany: '',
  });
});

const submitDispose = async () => {
  if (!disposeForm.disposalCompany.trim()) {
    ElMessage.warning('请填写处置公司');
    return;
  }
  try {
    const res = await wasteDispose({id: disposeForm.id, disposalCompany: disposeForm.disposalCompany.trim()});
    if (res.code === 200) {
      ElMessage.success('处置确认成功');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
</script>
