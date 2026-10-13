<template>
  <!-- 详情 -->
  <el-dialog v-model="visible" title="内镜检查详情" width="680px">
    <div v-loading="detailLoading">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="检查号">{{ detail.recordNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.endoTypeText }}</el-descriptions-item>
        <el-descriptions-item label="麻醉">{{ detail.anesthesiaMethodText }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
        <el-descriptions-item label="Hp">{{ detail.hpResultText }}</el-descriptions-item>
        <el-descriptions-item label="Boston 评分">{{ detail.bowelPrepScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="活检">
          {{ Number(detail.biopsyFlag) === 1 ? `${detail.biopsyPart}（${detail.biopsyCount} 块）` : '无' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.pathologyOrderNo" :span="2" label="关联病理号">{{
            detail.pathologyOrderNo
          }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="内镜所见">{{ detail.findings || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="内镜诊断">{{ detail.diagnosis || '未出' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="建议">{{ detail.suggestion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告医师">{{ detail.reportBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核医师">{{ detail.auditBy || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 内镜检查详情：整链路结果（含执行录的所见、病理联动单号）都在详情接口里，
 * 列表行没有这么多字段，所以组件按 id 自取。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEndoscopyDetail} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailLoading = ref(false);
const detail = ref(null);

const loadDetail = async () => {
  detailLoading.value = true;
  try {
    const res = await getEndoscopyDetail(props.id);
    if (res.code === 200)
      detail.value = res.data;
    
  } catch (e) {
    console.error(e);
    
  } finally {
    detailLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  await loadDetail();
});
</script>
