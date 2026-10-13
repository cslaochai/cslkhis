<template>
  <!-- 详情 -->
  <el-dialog v-model="visible" destroy-on-close title="会诊详情" width="680px">
    <el-descriptions v-if="detail" :column="2" border size="small">
      <el-descriptions-item label="会诊号">{{ text(detail.consultationNo) }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ text(detail.consultStatusText) }}</el-descriptions-item>
      <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
      <el-descriptions-item label="患者号">{{ text(detail.patientNo) }}</el-descriptions-item>
      <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
      <el-descriptions-item label="床号">{{ text(detail.bedNo) }}</el-descriptions-item>
      <el-descriptions-item label="申请科室">{{ text(detail.fromDeptName) }}</el-descriptions-item>
      <el-descriptions-item label="会诊科室">{{ text(detail.toDeptName) }}</el-descriptions-item>
      <el-descriptions-item label="会诊范围">{{ text(detail.consultTypeText) }}</el-descriptions-item>
      <el-descriptions-item label="紧急程度">{{ text(detail.isUrgentText) }}</el-descriptions-item>
      <el-descriptions-item label="申请医生">{{ text(detail.applyDoctorName) }}</el-descriptions-item>
      <el-descriptions-item label="申请时间">{{ fmt(detail.applyTime) }}</el-descriptions-item>
      <el-descriptions-item label="接诊医生">{{ text(detail.acceptDoctorName) }}</el-descriptions-item>
      <el-descriptions-item label="接诊时间">{{ fmt(detail.acceptTime) }}</el-descriptions-item>
      <el-descriptions-item label="完成时间">{{ fmt(detail.finishTime) }}</el-descriptions-item>
      <el-descriptions-item label="回写病历号">{{ text(detail.recordNo) }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="会诊理由">{{ text(detail.reason) }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="会诊结论">{{ text(detail.conclusion) }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="取消原因">{{ text(detail.cancelReason) }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="备注">{{ text(detail.remark) }}</el-descriptions-item>
    </el-descriptions>
    <div v-if="detail?.overdue" class="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">
      <el-icon class="mr-1">
        <Warning/>
      </el-icon>
      {{ detail.overdueText }}
    </div>
    <div class="mt-3 flex items-center gap-2 text-[11px] text-slate-400">
      <el-icon>
        <Clock/>
      </el-icon>
      等待/应答时长由后端按申请与接诊时间计算
      <el-icon class="ml-2">
        <Document/>
      </el-icon>
      完成会诊后病历号可见（{{ text(detail?.recordNo) }}）
      <el-icon class="ml-2">
        <User/>
      </el-icon>
      留痕为员工ID对应的姓名
      <el-icon class="ml-2">
        <CircleCheck/>
      </el-icon>
      状态机由后端强制
    </div>
    <template #footer>
      <el-button v-if="detail?.canFinish" v-perm="'ipd:consultation:edit'" type="primary"
                 @click="finishFromDetail">完成会诊
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 会诊详情弹框：按 id 自加载（列表行点开时由页面传入 consultationId）。
 * 「完成会诊」按钮沿用原链路：关闭本弹框后把已加载的详情抛给页面，由页面打开完成弹框。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {CircleCheck, Clock, Document, User, Warning} from '@element-plus/icons-vue';
import {getConsultationDetail} from '@/api/inpatientConsultation';
import {fmt, text} from '@/lib/consultation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
});

const emit = defineEmits(['update:modelValue', 'finish']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const loadDetail = async () => {
  try {
    const res = await getConsultationDetail(props.id);
    detail.value = (res.data || null);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
watch(visible, (open) => {
  if (open && props.id) loadDetail();
});

const finishFromDetail = () => {
  visible.value = false;
  detail.value && emit('finish', detail.value);
};
</script>
