<template>
  <el-dialog v-model="visible" title="回复并结束问诊" width="620px">
    <el-form label-width="110px" size="small">
      <el-form-item label="回复内容" required>
        <el-input v-model="replyForm.reply" :rows="4" type="textarea"/>
      </el-form-item>
      <el-form-item label="处置建议">
        <el-input v-model="replyForm.advice" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="建议线下就诊">
        <el-radio-group v-model="replyForm.needVisit">
          <el-radio :value="0">否</el-radio>
          <el-radio :value="1">是</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="replyLoading" type="primary" @click="submitReply">提交并结束</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 回复并结束问诊（接诊中才可回复是后端铁律，前端只在能点时给按钮）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {replyOnlineConsult} from '@/api/teleconsult';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const replyForm = reactive({reply: '', advice: '', needVisit: 0});
const replyLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  replyForm.reply = '';
  replyForm.advice = '';
  replyForm.needVisit = 0;
});

const submitReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请填写回复内容');
    return;
  }
  replyLoading.value = true;
  try {
    const res = await replyOnlineConsult({
      id: props.row.id,
      reply: replyForm.reply.trim(),
      advice: replyForm.advice.trim() || undefined,
      needVisit: replyForm.needVisit ?? 0,
    });
    if (res.code === 200) {
      ElMessage.success('已回复并结束');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '回复失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('回复失败');
  } finally {
    replyLoading.value = false;
  }
};
</script>
