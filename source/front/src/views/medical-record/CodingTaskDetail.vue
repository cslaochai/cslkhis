<template>
  <!-- 详情弹窗（只读；组件自己按 id 取数，状态文案走页面传入的列表字典） -->
  <el-dialog v-model="visible" :title="`编码任务 ${detail?.taskNo || ''}`" data-testid="ct-detail-dialog"
             width="640px">
    <template v-if="detail">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          <span v-if="Number(detail.returnCount) > 0" class="ml-1 text-xs text-red-500">累计退修 {{
              detail.returnCount
            }} 次</span>
        </el-descriptions-item>
        <el-descriptions-item label="编码员">{{ detail.coderName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="分配时间">
          {{ detail.assignTime ? (detail.assignTime || '').replace('T', ' ').slice(0, 19) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="病历诊断">{{ detail.diagnosis || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="主诊断 ICD">
          {{ detail.mainIcdCode ? `${detail.mainIcdCode} ${detail.mainIcdName}` : '—' }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="其他诊断">{{ detail.otherIcdText || '—' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ detail.submitTime ? (detail.submitTime || '').replace('T', ' ').slice(0, 19) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间">
          {{ detail.auditTime ? (detail.auditTime || '').replace('T', ' ').slice(0, 19) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="审核意见">{{ detail.auditRemark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getCodeTaskDetail} from '@/api/codeTask';
import {dictLabelText} from '@/lib/utils';
import {statusTagType} from '@/lib/codeTask';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 状态字典由主页面加载一次（列表列共用同一份），子组件不许再拉一遍
  statusDict: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const statusText = (v) => dictLabelText(props.statusDict, v);

const detail = ref(null);
const loadDetail = async () => {
  try {
    const res = await getCodeTaskDetail(props.id);
    if (res.code === 200) {
      detail.value = res.data;
    } else {
      ElMessage.error(res.message || '查询详情失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
};
watch(visible, (open) => {
  if (!open)
    return;
  detail.value = null;
  loadDetail();
});
</script>
