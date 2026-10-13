<template>
  <!-- 电话外呼（G-15）：mock 通道=登记待呼，护士按明文电话拨打后回填结果 -->
  <el-dialog v-model="visible" title="电话外呼" width="560px">
    <div v-if="callTask" class="space-y-3">
      <div class="rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-xs text-sky-800">
        已登记待外呼（第 {{ callTask.callAttempts || 1 }} 次，人工通道）。请拨打以下电话，通话结束后回填结果。
      </div>
      <el-form label-width="90px">
        <el-form-item label="患者">
          <el-input :model-value="`${callTask.patientName || ''}（${callTask.patientNo || '—'}）`" disabled/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input :model-value="callTask.phone || '—'" disabled/>
        </el-form-item>
        <el-form-item label="话术稿">
          <el-input v-model="callScript" :rows="5" placeholder="按随访内容通话，可修改" type="textarea"/>
        </el-form-item>
        <el-form-item label="外呼备注">
          <el-input v-model="callRemark" :rows="2" placeholder="未接通原因、患者反馈摘要等（选填）"
                    type="textarea"/>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button :disabled="!callTask?.patientId" @click="composeCallScript">AI 拟话术</el-button>
      <el-button :loading="callResultLoading" type="warning" @click="submitCallResult(false)">未接通</el-button>
      <el-button :loading="callResultLoading" type="primary" @click="submitCallResult(true)">已接通</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 电话外呼弹框（G-15，mock 通道=人工登记待呼）。
 *
 * 两种进法由 resume 区分：
 * - resume=false 首次外呼：登记待外呼，返回明文电话供护士拨打；
 * - resume=true 已登记待外呼的行：不再重复登记，直接取详情（明文电话）回弹窗回填结果。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {followupCallRegister, followupCallResult, getFollowupDetail} from '@/api/followup';
import {composeFollowup} from '@/api/ai';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  taskId: {type: [String, Number], default: null},
  resume: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const callTask = ref(null);
const callScript = ref('');
const callRemark = ref('');
const callResultLoading = ref(false);

watch(visible, async (open) => {
  if (!open || !props.taskId) return;
  if (props.resume) {
    try {
      const res = await getFollowupDetail(props.taskId);
      if (res.code === 200 && res.data) {
        callTask.value = res.data;
        callScript.value = res.data.followupContent || '';
        callRemark.value = '';
      } else
        ElMessage.error(res.message || '加载任务失败');
    } catch (e) {
      console.error(e);
      ElMessage.error('加载任务失败');
    }
    return;
  }
  try {
    const res = await followupCallRegister(props.taskId);
    if (res.code === 200 && res.data) {
      callTask.value = res.data;
      callScript.value = res.data.followupContent || '';
      callRemark.value = '';
    } else
      ElMessage.error(res.message || '外呼登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('外呼登记失败');
  }
});

// AI 拟话术：与编辑弹窗同一能力（followup_compose），产物仍是护士终审的初稿
const composeCallScript = async () => {
  const t = callTask.value;
  if (!t?.patientId) {
    ElMessage.warning('缺少患者信息，无法拟话术');
    return;
  }
  try {
    const res = await composeFollowup({
      patientId: t.patientId,
      followupType: t.followupType,
      diagnosis: t.diagnosis || undefined,
    });
    if (res.code === 200 && res.data?.content) {
      callScript.value = res.data.content;
      if (res.data.degraded)
        ElMessage.warning(`AI 暂不可用（${res.data.degradeReason || '原因未知'}），已按随访类型模板生成`);
      else
        ElMessage.success('AI 话术已填入，请核对后拨打');
    } else
      ElMessage.error(res.message || 'AI 未生成话术');
  } catch (e) {
    console.error(e);
    ElMessage.error('AI 拟话术失败');
  }
};
const submitCallResult = async (connected) => {
  callResultLoading.value = true;
  try {
    const res = await followupCallResult({
      id: callTask.value.id,
      connected,
      remark: callRemark.value.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(connected ? '已接通，任务已转随访中' : '已登记未接通，可稍后再次外呼');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  } finally {
    callResultLoading.value = false;
  }
};
</script>
