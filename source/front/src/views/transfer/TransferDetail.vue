<template>
  <!-- 转科详情 -->
  <el-dialog v-model="visible" data-testid="p4-transfer-detail-dialog" title="转科详情" width="720px">
    <div v-if="detail" class="space-y-4">
      <div class="flex items-center gap-3">
        <span class="text-base font-semibold text-slate-900">{{ text(detail.transferNo) }}</span>
        <el-tag :type="statusTagType(detail.transferStatus)" size="small">
          {{ text(detail.transferStatusText) }}
        </el-tag>
        <span v-if="detail.waitText" class="text-[12px] text-slate-400">{{ detail.waitText }}</span>
      </div>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
        <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
        <el-descriptions-item label="转出科室">{{ text(detail.fromDeptName) }}</el-descriptions-item>
        <el-descriptions-item label="转入科室">{{ text(detail.toDeptName) }}</el-descriptions-item>
        <el-descriptions-item label="转出床位">
          {{ text(detail.fromWardName) }} {{ text(detail.fromBedNo) }}床
        </el-descriptions-item>
        <el-descriptions-item label="转入床位">
          {{ text(detail.toWardName) }} {{ text(detail.toBedNo) }}床
        </el-descriptions-item>
        <el-descriptions-item label="转科类型">{{ text(detail.transferTypeText) }}</el-descriptions-item>
        <el-descriptions-item label="已住院天数">
          {{ detail.hospitalDays === undefined || detail.hospitalDays === null ? '—' : detail.hospitalDays + ' 天' }}
        </el-descriptions-item>
        <el-descriptions-item label="发起医生">{{ text(detail.applyDoctorName) }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ fmt(detail.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="接收医生">{{ text(detail.receiveDoctorName) }}</el-descriptions-item>
        <el-descriptions-item label="接收时间">{{ fmt(detail.receiveTime) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="转科原因">{{ text(detail.transferReason) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="医嘱处置">
          <span :class="(detail.orderRemark || '').includes('仍有') ? 'text-red-600' : 'text-slate-700'">
            {{ text(detail.orderRemark) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="回写病历号">
          <span v-if="detail.recordNo" class="text-slate-700">{{ detail.recordNo }}</span>
          <span v-else class="text-slate-400">尚未回写（转科未生效）</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelReason" :span="2" label="取消原因">
          {{ detail.cancelReason }}
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
/**
 * 转科详情（只读）：自己按 id 取数；页面把当前行一并传进来，用于打开瞬间不闪白。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTransferDetail} from '@/api/inpatientTransfer';
import {fmt, statusTagType, text} from '@/lib/transfer';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 转科记录 id（雪花 ID 字符串）
  id: {type: [String, Number], default: null},
  // 列表当前行快照：打开瞬间先显示它，取数回来后覆盖
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
watch(visible, async (open) => {
  if (!open)
    return;
  detail.value = props.row;
  try {
    const res = await getTransferDetail(props.id);
    detail.value = (res.data || props.row);
  } catch (error) {
    ElMessage.error(error.message || '加载转科详情失败');
    visible.value = false;
  }
});
</script>
