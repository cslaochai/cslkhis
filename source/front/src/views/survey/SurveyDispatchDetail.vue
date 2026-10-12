<template>
  <!-- 发放单只读详情 -->
  <el-dialog v-model="visible" title="发放单详情" width="600px">
    <el-form v-if="detail" disabled label-width="100px">
      <el-form-item label="发放单号">
        <el-input :model-value="detail.dispatchNo"/>
      </el-form-item>
      <el-form-item label="问卷">
        <el-input :model-value="detail.templateName"/>
      </el-form-item>
      <el-form-item label="场景">
        <el-input :model-value="sceneText(detail.scene)"/>
      </el-form-item>
      <el-form-item label="患者">
        <el-input :model-value="`${detail.patientName || ''} ${detail.patientNo || ''}`"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input :model-value="detail.phoneMasked || '—'"/>
      </el-form-item>
      <el-form-item label="科室">
        <el-input :model-value="detail.deptName || '—'"/>
      </el-form-item>
      <el-form-item label="发放来源">
        <el-input :model-value="sourceText(detail.sourceType)"/>
      </el-form-item>
      <el-form-item label="回收渠道">
        <el-input :model-value="channelText(detail.channel)"/>
      </el-form-item>
      <el-form-item label="回收状态">
        <el-input :model-value="dispatchStatusText(detail.dispatchStatus) + (detail.overdue ? '（已逾期）' : '')"/>
      </el-form-item>
      <el-form-item label="推送时间">
        <el-input :model-value="fmtTime(detail.pushTime)"/>
      </el-form-item>
      <el-form-item label="截止时间">
        <el-input :model-value="fmtTime(detail.expireTime)"/>
      </el-form-item>
      <el-form-item label="答卷">
        <el-input :model-value="detail.answerId ? String(detail.answerId) : '未回收'"/>
      </el-form-item>
      <el-form-item v-if="detail.remark" label="备注">
        <el-input :model-value="detail.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 发放单详情（只读）：直接吃列表行 —— 行里已含全部展示字段，
 * 不回后端取明文手机号（`dispatch/getById` 要 qc:survey:edit，且会把手机号带进弹框）。
 */
import {computed} from 'vue';
import {dictLabelText} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 字典由页面加载一次后传下来，列表列与详情同一份口径
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmtTime = (v) => (v ? String(v).slice(0, 16).replace('T', ' ') : '—');
const detail = computed(() => props.row);
const sceneText = (v) => dictLabelText(props.dicts.scene, v);
const sourceText = (v) => dictLabelText(props.dicts.source, v);
const channelText = (v) => dictLabelText(props.dicts.channel, v);
const dispatchStatusText = (v) => dictLabelText(props.dicts.dispatchStatus, v);
</script>
