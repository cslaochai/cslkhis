<template>
  <!-- 执行弹窗 -->
  <el-dialog v-model="visible" :title="`执行检查：${row?.recordNo || ''}`" width="480px">
    <el-form label-width="100px">
      <el-form-item label="超声医师">
        <el-input v-model="execForm.sonographer" placeholder="留空默认当前登录人"/>
      </el-form-item>
      <el-form-item label="检查部位">
        <el-input v-model="execForm.bodyPart"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'medtech:ultrasound:edit'" :loading="actLoading" type="primary" @click="saveExecute">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 执行检查（登记后 2-已签到 / 3-检查中 的记录，指定超声医师与检查部位）
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {ultrasoundExecute} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const actLoading = ref(false);
const execForm = reactive({sonographer: '', bodyPart: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(execForm, {sonographer: '', bodyPart: props.row?.bodyPart || ''});
});

const saveExecute = async () => {
  actLoading.value = true;
  try {
    const res = await ultrasoundExecute({
      recordId: props.row.id,
      sonographer: execForm.sonographer.trim() || undefined,
      bodyPart: execForm.bodyPart.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  } finally {
    actLoading.value = false;
  }
};
</script>
