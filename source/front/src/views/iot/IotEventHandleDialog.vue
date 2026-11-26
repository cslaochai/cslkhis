<template>
  <el-dialog v-model="visible" :title="title" destroy-on-close width="520px">
    <div v-if="event" class="text-sm text-gray-600 mb-3">
      <div class="mb-1"><span class="text-gray-400">设备编号：</span>{{ event.deviceNo }}</div>
      <div class="mb-1"><span class="text-gray-400">告警事件：</span>{{ event.eventCodeText || event.eventCode }}</div>
      <div v-if="event.detail" class="text-gray-500">{{ event.detail }}</div>
    </div>
    <el-input
      v-model="remark"
      :rows="3"
      maxlength="500"
      type="textarea"
      :placeholder="actionType === 1 ? '处理说明（可选）：如已现场确认、恢复正常' : '忽略原因（可选）：如误报、重复上报'"
    />
    <template #footer>
      <el-button :loading="submitting" type="primary" @click="submit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 设备告警处理弹框（确认处理 / 忽略共用）。
 * 提交收口在服务端：并发重复处理由后端条件更新兜底，本页不判状态。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {iotEventHandle} from '@/api/iot';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  event: {type: Object, default: null},
  // 1-已处理 2-已忽略
  actionType: {type: Number, default: 1},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const remark = ref('');
const submitting = ref(false);

const title = computed(() => props.actionType === 2 ? '忽略告警' : '确认处理告警');

watch(visible, (v) => {
  if (v) remark.value = '';
});

const submit = async () => {
  if (!props.event?.id) return;
  submitting.value = true;
  try {
    const res = await iotEventHandle({
      id: props.event.id,
      handledFlag: props.actionType,
      remark: remark.value.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(props.actionType === 2 ? '已忽略' : '已处理');
      visible.value = false;
      emit('saved');
    }
  } catch (e) {
    console.error(e);
  } finally {
    submitting.value = false;
  }
};
</script>
