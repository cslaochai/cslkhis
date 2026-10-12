<template>
  <!-- 详情（行点击，只读） -->
  <el-dialog v-model="visible" title="随访任务详情" width="640px">
    <el-form v-if="detail" disabled label-width="90px">
      <el-form-item label="任务编号">
        <el-input :model-value="detail.taskNo"/>
      </el-form-item>
      <el-form-item label="患者">
        <el-input :model-value="`${detail.patientName || ''} ${detail.patientNo || ''}`"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input :model-value="detail.phoneMasked || '—'"/>
      </el-form-item>
      <el-form-item label="出院科室">
        <el-input :model-value="detail.deptName || '—'"/>
      </el-form-item>
      <el-form-item label="随访类型">
        <el-input :model-value="typeText(detail.followupType)"/>
      </el-form-item>
      <el-form-item label="计划时间">
        <el-input :model-value="fmtTime(detail.followupTime)"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-input :model-value="statusText(detail.followupStatus)"/>
      </el-form-item>
      <el-form-item label="诊断">
        <el-input :model-value="detail.diagnosis || '—'"/>
      </el-form-item>
      <el-form-item label="随访内容">
        <el-input :model-value="detail.followupContent" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item v-if="detail.executeResult" label="随访结果">
        <el-input :model-value="detail.executeResult" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item v-if="detail.executorName" label="执行人">
        <el-input :model-value="`${detail.executorName} ${fmtTime(detail.executeTime)}`"/>
      </el-form-item>
      <el-form-item v-if="detail.revisitAppointId" label="复诊号">
        <el-input :model-value="String(detail.revisitAppointId)"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 随访任务详情（只读）。自己按 id 取数，父页面只传 id 与显隐。
 *
 * 随访结果的填写不在这里：本弹框全表 disabled，填结果走列表的「完成随访」动作。
 * row 只用于「打开瞬间不闪白」，真实数据一律以 getById 的返回为准。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getFollowupDetail} from '@/api/followup';
import {dictLabelText} from '@/lib/utils';
import {fmtTime} from '@/lib/followup';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 列表行数据：仅为打开瞬间不闪白，缺 followupContent / executeResult / executeTime 三列
  row: {type: Object, default: null},
  // 字典由页面加载一次传下来（列表列同一份），禁止两个子组件各拉一遍
  typeDict: {type: Array, default: () => []},
  statusDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const typeText = (v) => dictLabelText(props.typeDict, v);
const statusText = (v) => dictLabelText(props.statusDict, v);

watch(visible, async (open) => {
  if (!open) return;
  detail.value = props.row || null;
  if (!props.id) return;
  const res = await getFollowupDetail(props.id).catch((e) => {
    console.error(e);
    return null;
  });
  if (res && res.code === 200)
    detail.value = res.data;
  else if (res) ElMessage.error(res.message || '加载详情失败');
});
</script>
