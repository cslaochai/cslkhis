<template>
  <!-- 详情 -->
  <el-dialog v-model="visible" data-testid="p4-op-detail-dialog" title="手术详情" width="820px">
    <div v-if="detail" class="space-y-4">
      <div class="flex items-center gap-3">
        <el-icon class="text-slate-400">
          <Scissor/>
        </el-icon>
        <span class="text-base font-semibold text-slate-900">{{ text(detail.applyNo) }}</span>
        <el-tag :type="statusTagType(detail.operationStatus)" size="small">
          {{ text(detail.operationStatusText) }}
        </el-tag>
        <span class="text-[12px] text-slate-400">{{ stageText(detail) }}</span>
      </div>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
        <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
        <el-descriptions-item label="申请科室">{{ text(detail.applyDeptName) }}</el-descriptions-item>
        <el-descriptions-item label="申请时床位">
          {{ text(detail.applyWardName) }} {{ text(detail.applyBedNo) }}床
        </el-descriptions-item>
        <el-descriptions-item label="申请医生">{{ text(detail.applyDoctorName) }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ fmt(detail.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="拟施手术">{{ text(detail.plannedOperationName) }}</el-descriptions-item>
        <el-descriptions-item label="手术编码">{{ text(detail.plannedOperationCode) }}</el-descriptions-item>
        <el-descriptions-item label="手术级别">{{ text(detail.operationLevelText) }}</el-descriptions-item>
        <el-descriptions-item label="切口等级">{{ text(detail.incisionLevelText) }}</el-descriptions-item>
        <el-descriptions-item label="麻醉方式">{{ text(detail.anesthesiaTypeText) }}</el-descriptions-item>
        <el-descriptions-item label="急诊 / 主次">
          {{ text(detail.isEmergencyText) }} / {{ text(detail.isMainText) }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="术前诊断">{{ text(detail.preopDiagnosis) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="手术指征">{{ text(detail.operationReason) }}</el-descriptions-item>
        <el-descriptions-item label="手术间">{{ text(detail.operationRoom) }}</el-descriptions-item>
        <el-descriptions-item label="计划时段">{{ text(detail.plannedTimeText) }}</el-descriptions-item>
        <el-descriptions-item label="主刀医师">{{ text(detail.surgeonName) }}</el-descriptions-item>
        <el-descriptions-item label="助手">{{ text(detail.assistantName) }}</el-descriptions-item>
        <el-descriptions-item label="麻醉医师">{{ text(detail.anesthetistName) }}</el-descriptions-item>
        <el-descriptions-item label="排台人 / 时间">
          {{ text(detail.scheduleDoctorName) }} / {{ fmt(detail.scheduleTime) }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="术前核对">
          <span :class="detail.preopCheckItems ? 'text-slate-700' : 'text-slate-400'">
            {{ detail.preopCheckItems ? detail.preopCheckItemsText : '尚未核对' }}
          </span>
          <div v-if="detail.preopCheckDoctorName" class="text-[11px] text-slate-400">
            由 {{ detail.preopCheckDoctorName }} 于 {{ fmt(detail.preopCheckTime) }} 核对
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.preopNote" :span="2" label="核对异常说明">
          {{ detail.preopNote }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.actualOperationName" :span="2" label="实际手术">
          {{ detail.actualOperationName }}
          <span class="ml-2 text-[11px] text-slate-400">
            {{ fmt(detail.operationStartTime) }} ~ {{ fmt(detail.operationEndTime) }}
            （{{ text(detail.durationText) }}）
          </span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.bloodLoss !== undefined && detail.bloodLoss !== null" label="术中出血量">
          {{ detail.bloodLoss }} ml
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.specimenSent" label="标本送检">{{
            detail.specimenSent
          }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.intraopFindings" :span="2" label="术中所见">
          {{ detail.intraopFindings }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.intraopProcedure" :span="2" label="手术经过">
          {{ detail.intraopProcedure }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.postopNote" :span="2" label="术后处理">
          {{ detail.postopNote }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="回写病历号">
          <span v-if="detail.recordNo" class="text-slate-700">{{ detail.recordNo }}</span>
          <span v-else class="text-slate-400">尚未回写（手术未完成）</span>
          <span v-if="detail.operationId" class="ml-2 text-[11px] text-slate-400">首页手术明细已写入</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelReason" :span="2" label="取消原因">
          {{ detail.cancelReason }}（{{ text(detail.cancelDoctorName) }} {{ fmt(detail.cancelTime) }}）
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.remark" :span="2" label="备注">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Scissor} from '@element-plus/icons-vue';
import {getOperationApplyDetail} from '@/api/inpatientOperation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 父页面把当前行一并传进来，打开瞬间不闪白
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
/** 每台手术的进行位置（列表里一眼看出卡在哪一步）——纯后端文案拼装，前端不加业务判断 */
const stageText = (row) => {
  if (row.operationStatus === 0)
    return '① 等待手术室排台';
  if (row.operationStatus === 1)
    return '② 已排台，等待术前核对';
  if (row.operationStatus === 2)
    return '③ 已核对，等待上台并登记完成';
  if (row.operationStatus === 3)
    return '④ 已完成并回写';
  return '已取消';
};
const statusTagType = (status) => {
  if (status === 0)
    return 'info';
  if (status === 1)
    return 'warning';
  if (status === 2)
    return 'primary';
  if (status === 3)
    return 'success';
  return 'info';
};

const detail = ref(null);

watch(visible, async (open) => {
  if (!open) return;
  detail.value = props.row || null;
  if (!props.id) return;
  try {
    const res = await getOperationApplyDetail(props.id);
    detail.value = (res.data || props.row);
  } catch (error) {
    ElMessage.error(error.message || '加载手术详情失败');
  }
});
</script>
