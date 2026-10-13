<template>
  <el-dialog v-model="visible" title="线上问诊详情" width="700px">
    <el-descriptions v-if="detail" :column="2" border size="small">
      <el-descriptions-item label="问诊单号">{{ detail.consultNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTag(detail.status)" size="small">{{ onlineStatusText(detail.status) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="患者">{{ detail.patientName }}（{{
          detail.patientNo || '—'
        }}）
      </el-descriptions-item>
      <el-descriptions-item label="问诊方式">{{ onlineTypeText(detail.consultType) }}</el-descriptions-item>
      <el-descriptions-item label="接诊科室">{{ detail.deptName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="接诊医生">{{ detail.doctorName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="发起时间">{{ detail.applyTime || '—' }}</el-descriptions-item>
      <el-descriptions-item label="完成时间">{{ detail.finishTime || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="主诉/问题">{{ detail.chiefComplaint }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="医生回复">{{ detail.reply || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="处置建议">{{ detail.advice || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="建议线下就诊">
        <el-tag v-if="Number(detail.needVisit) === 1" size="small" type="warning">是</el-tag>
        <span v-else>否</span>
      </el-descriptions-item>
      <el-descriptions-item v-if="detail.rejectReason" :span="2" label="退诊原因">{{
          detail.rejectReason
        }}
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
/**
 * 线上问诊详情：自己按 id 取数（visible 变 true 时 watch 取数），所有 ID 是字符串（雪花ID）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getOnlineConsultDetail} from '@/api/teleconsult';
import {dictLabelText} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 问诊方式/状态字典由页面加载一次传下来（查询条件与列表列同一份）
  onlineTypeDict: {type: Array, default: () => []},
  onlineStatusDict: {type: Array, default: () => []},
  statusTag: {type: Function, required: true},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const onlineTypeText = (v) => dictLabelText(props.onlineTypeDict, v);
const onlineStatusText = (v) => dictLabelText(props.onlineStatusDict, v);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  const res = await getOnlineConsultDetail(props.id);
  if (res.code === 200) {
    detail.value = res.data;
  } 
});
</script>
