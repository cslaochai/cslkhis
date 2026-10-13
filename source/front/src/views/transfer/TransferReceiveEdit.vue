<template>
  <!-- 接收转科 -->
  <el-dialog v-model="visible" data-testid="p4-transfer-accept-dialog" title="接收转科" width="560px">
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">
          {{ text(row.patientName) }}（{{ text(row.admissionNo) }}）
        </div>
        <div class="mt-1 text-slate-600">
          <span class="text-slate-500">{{ text(row.fromDeptName) }}</span>
          <span class="mx-1">→</span>
          <span class="font-medium">{{ text(row.toDeptName) }}</span>
        </div>
        <div class="text-[12px] text-slate-500">
          转入 {{ text(row.toWardName) }} {{ text(row.toBedNo) }}床 ·
          {{ text(row.transferTypeText) }}
        </div>
        <div class="mt-1 text-[12px] text-slate-500">转科原因：{{ text(row.transferReason) }}</div>
      </div>
      <el-alert :closable="false" show-icon type="warning">
        接收即生效：停原科室长期医嘱 → 换科室换床 → 回写转科记录病历。
        若该床位在此期间已被占用，后端会拒绝（不会把两个患者塞进同一张床）。
      </el-alert>
      <el-form label-width="80px">
        <el-form-item label="接收备注">
          <el-input
              v-model="acceptRemark"
              :rows="2"
              data-testid="p4-accept-remark"
              placeholder="可空"
              type="textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:transfer:edit'" :loading="acceptSubmitting" data-testid="p4-accept-submit"
                 type="primary" @click="submitAccept">
        确认接收
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 接收转科：接收即生效（停原科室长期医嘱 + 换科室换床 + 回写转科记录病历），
 * 行快照由列表页传进来，提交后回列表刷新（含在院患者名册，床位占用已变）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {acceptTransfer} from '@/api/inpatientTransfer';
import {text} from '@/lib/transfer';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 待接收的转科行（含 canAccept 与转入目标快照）
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const acceptSubmitting = ref(false);
const acceptRemark = ref('');
watch(visible, (open) => {
  if (!open)
    return;
  acceptRemark.value = '';
});
const submitAccept = async () => {
  if (!props.row)
    return;
  acceptSubmitting.value = true;
  try {
    await acceptTransfer({
      transferId: props.row.id,
      remark: acceptRemark.value.trim() || undefined,
    });
    ElMessage.success('已接收，转科生效（原科室长期医嘱已按规则处置，并回写转科记录病历）');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '接收失败');
  } finally {
    acceptSubmitting.value = false;
  }
};
</script>
