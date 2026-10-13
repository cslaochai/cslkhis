<template>
  <el-dialog v-model="visible" title="远程会诊详情" width="760px">
    <el-descriptions v-if="detail" :column="2" border size="small">
      <el-descriptions-item label="会诊单号">{{ detail.consultNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTag(detail.status)" size="small">{{ teleStatusText(detail.status) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="患者">{{ detail.patientName }}（{{
          detail.patientNo || '—'
        }}）
      </el-descriptions-item>
      <el-descriptions-item label="会诊类型">{{ teleTypeText(detail.consultType) }}</el-descriptions-item>
      <el-descriptions-item label="申请科室">{{ detail.applyDeptName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="申请医生">{{ detail.applyDoctor || '—' }}</el-descriptions-item>
      <el-descriptions-item label="专家医院">{{ detail.expertHospital || '—' }}</el-descriptions-item>
      <el-descriptions-item label="专家">{{ detail.expertName || '—' }} {{
          detail.expertTitle || ''
        }}
      </el-descriptions-item>
      <el-descriptions-item label="计划时间">{{ detail.planTime || '—' }}</el-descriptions-item>
      <el-descriptions-item label="接入号">{{ detail.meetNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="费用(元)">{{ detail.fee ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="完成时间">{{ detail.completeTime || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="会诊目的">{{ detail.purpose }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="病情摘要">{{ detail.diagnosis || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="会诊意见">{{ detail.opinion || '—' }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.cancelReason" :span="2" label="取消原因">{{
          detail.cancelReason
        }}
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
/**
 * 远程会诊详情：自己按 id 取数（visible 变 true 时 watch 取数），所有 ID 是字符串（雪花ID）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTeleConsultDetail} from '@/api/teleconsult';
import {dictLabelText} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 会诊类型/状态字典由页面加载一次传下来（查询条件与列表列同一份）
  teleTypeDict: {type: Array, default: () => []},
  teleStatusDict: {type: Array, default: () => []},
  statusTag: {type: Function, required: true},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const teleTypeText = (v) => dictLabelText(props.teleTypeDict, v);
const teleStatusText = (v) => dictLabelText(props.teleStatusDict, v);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  const res = await getTeleConsultDetail(props.id);
  if (res.code === 200) {
    detail.value = res.data;
  } 
});
</script>
