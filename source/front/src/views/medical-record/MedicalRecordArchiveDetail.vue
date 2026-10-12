<template>
  <!-- 详情（只读；组件自己按 id 取数，状态文案走页面传入的列表字典） -->
  <el-dialog v-model="visible" destroy-on-close title="归档详情" width="720px">
    <el-descriptions v-if="detail" :column="2" border>
      <el-descriptions-item label="归档单号">{{ detail.archiveNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="患者">{{ detail.patientName || '—' }}（{{
          detail.patientNo || '—'
        }}）
      </el-descriptions-item>
      <el-descriptions-item label="就诊日期">{{ detail.visitDate || '—' }}</el-descriptions-item>
      <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="医生">{{ detail.doctorName || '—' }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="诊断">{{ detail.diagnosis || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTagType(detail.archiveStatus)" effect="plain" size="small">
          {{ statusText(detail.archiveStatus) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="归档时间">{{ detail.archiveTime || '—' }}</el-descriptions-item>
      <el-descriptions-item label="封存时间">{{ detail.sealTime || '—' }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getArchiveDetail} from '@/api/archive';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 归档状态字典由主页面加载一次（列表列共用同一份），子组件不许再拉一遍
  statusOptions: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

/** 状态文案：命中字典用字典，命中不了暴露「未知(n)」 */
const statusText = (v) => {
  if (v === null || v === undefined)
    return '—';
  const hit = props.statusOptions.find(o => o.value === Number(v));
  return hit ? hit.label : `未知(${v})`;
};
const statusTagType = (v) => {
  if (v === 1)
    return 'warning';
  if (v === 2)
    return 'success';
  if (v === 3)
    return 'info';
  return 'info';
};

const detail = ref(null);
const loadDetail = async () => {
  try {
    const res = await getArchiveDetail(props.id);
    if (res.code === 200 && res.data) {
      detail.value = res.data;
    } else {
      ElMessage.error(res.message || '获取归档详情失败');
    }
  } catch (e) {
    ElMessage.error('获取归档详情失败');
    console.error(e);
  }
};
watch(visible, (open) => {
  if (!open)
    return;
  detail.value = null;
  loadDetail();
});
</script>
