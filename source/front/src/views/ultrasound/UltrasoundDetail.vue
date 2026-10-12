<template>
  <!-- 详情 -->
  <el-dialog v-model="visible" title="超声检查详情" width="680px">
    <template v-if="detail">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="检查号">{{ detail.recordNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.usTypeText }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="超声所见">{{ detail.findings || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="超声提示">{{ detail.conclusion || '未出' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="建议">{{ detail.suggestion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告医师">{{ detail.reportBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核医师">{{ detail.auditBy || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt-3 font-medium text-sm text-gray-600">测量值</div>
      <el-table :data="detail.measures || []" class="mt-1" size="small">
        <el-table-column label="测量项" min-width="140" prop="measureName"/>
        <el-table-column label="测量值" prop="measureValue" width="110"/>
        <el-table-column label="单位" prop="unit" width="80"/>
        <el-table-column label="参考范围" prop="referenceRange" width="120"/>
        <el-table-column label="标志" prop="abnormalFlagText" width="90"/>
      </el-table>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 超声检查详情（只读；组件自己按 id 取数，父页面只传 id）
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getUltrasoundDetail} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  try {
    const res = await getUltrasoundDetail(props.id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '查询详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
});
</script>
