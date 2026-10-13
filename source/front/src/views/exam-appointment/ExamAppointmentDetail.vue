<template>
  <el-dialog v-model="visible" title="预约单详情" width="680px">
    <div v-loading="detail.loading">
      <el-descriptions v-if="detail.data" :column="2" border size="small">
        <el-descriptions-item label="预约单号">{{ detail.data.apptNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="apptStatusTag(detail.data.status)" size="small">{{ detail.data.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.data.patientName }} / {{
            detail.data.patientNo
          }}
        </el-descriptions-item>
        <el-descriptions-item label="申请单号">{{ detail.data.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="检查项目">{{ detail.data.itemName }}</el-descriptions-item>
        <el-descriptions-item label="部位">{{ detail.data.bodyPart || '—' }}</el-descriptions-item>
        <el-descriptions-item label="设备">{{ detail.data.deviceName }}（{{
            detail.data.deviceCode
          }}）
        </el-descriptions-item>
        <el-descriptions-item label="机房">{{ detail.data.roomName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="检查时间">{{ detail.data.examDate }} {{
            detail.data.timeRange
          }}
        </el-descriptions-item>
        <el-descriptions-item label="时长">{{ detail.data.examMinutes }}′</el-descriptions-item>
        <el-descriptions-item label="开单科室">{{ detail.data.applyDeptName }} {{
            detail.data.doctorName
          }}
        </el-descriptions-item>
        <el-descriptions-item label="执行科室">{{ detail.data.examDeptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="占号">{{ detail.data.bookBy }} {{
            fmtTime(detail.data.bookTime)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="到检">{{ fmtTime(detail.data.arriveTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成">{{ fmtTime(detail.data.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消">{{ fmtTime(detail.data.cancelTime) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="取消原因">{{ detail.data.cancelReason || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="备注">{{ detail.data.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getExamAppointDetail} from '@/api/examAppointment';
import {apptStatusTag} from '@/lib/examAppointment';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 预约单 id（雪花 id，字符串/数字皆可；本组件按 id 自取详情）
  apptId: {type: [String, Number], default: ''},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-');

// 详情抽屉
const detail = reactive({loading: false, data: null});

watch(visible, (v) => {
  if (v) openDetail();
});

const openDetail = async () => {
  detail.loading = true;
  detail.data = null;
  try {
    const res = await getExamAppointDetail(props.apptId);
    if (res.code === 200)
      detail.data = res.data;
    
  } catch (e) {
    console.error(e);
    
  } finally {
    detail.loading = false;
  }
};
</script>
