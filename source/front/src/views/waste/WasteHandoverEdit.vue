<template>
  <!-- 交接 -->
  <el-dialog v-model="visible" :title="`医废交接 — ${handoverForm.wasteNo}`" width="440px">
    <el-form label-width="90px">
      <el-form-item label="交接人" required>
        <el-input v-model="handoverForm.handoverName" placeholder="接收方签字人" style="width: 220px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:waste:edit'" type="primary" @click="submitHandover">确认交接</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 医废交接：已登记(1) → 已交接(2)，不可逆（三态闭环见 WasteView 页头注释）
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {wasteHandover} from '@/api/waste';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 要交接的登记行
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const handoverForm = reactive({id: null, wasteNo: '', handoverName: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(handoverForm, {
    id: Number(props.row?.id), wasteNo: props.row?.wasteNo, handoverName: '',
  });
});

const submitHandover = async () => {
  if (!handoverForm.handoverName.trim()) {
    ElMessage.warning('请填写交接人');
    return;
  }
  try {
    const res = await wasteHandover({id: handoverForm.id, handoverName: handoverForm.handoverName.trim()});
    if (res.code === 200) {
      ElMessage.success('交接成功');
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
