<template>
  <el-dialog v-model="visible" title="红冲记账行" width="460px">
    <el-form label-width="100px">
      <el-form-item label="红冲行">
        <span>{{ row?.itemName }}（¥{{ formatMoney(row?.amount) }}）</span>
      </el-form-item>
      <el-form-item label="冲减数量">
        <el-input-number v-model="form.quantity" :min="0" :precision="2" data-testid="fee-reverse-quantity"/>
        <div class="w-full text-xs text-slate-500 mt-1">留空 = 整行冲；填数量 = 部分冲减（生成负数红冲行）</div>
      </el-form-item>
      <el-form-item label="红冲原因" required>
        <el-input v-model="form.reason" :rows="2" data-testid="fee-reverse-reason" maxlength="200" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" data-testid="fee-reverse-submit" type="danger" @click="submit">确认红冲</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * L1 红冲记账行 —— 从 views/fee-record/FeeRecordView.vue 拆出
 *
 * 记账行不可修改，修正只能红冲：本弹框生成的是一条指向原行的负数行，原行金额永不改。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {reverseFee} from '@/api/feeRecord';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 被红冲的记账行 */
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({quantity: null, reason: ''});
/** 部分冲减只在数量>可冲数量内有意义；留空 = 整行冲 */
const isPartial = computed(() => form.quantity != null && Number(form.quantity) > 0);

async function submit() {
  if (!form.reason) {
    ElMessage.warning('请填写红冲原因');
    return;
  }
  const row = props.row;
  const partial = isPartial.value;
  const confirmed = await ElMessageBox.confirm(partial
      ? `将对「${row.itemName}」按数量 ${form.quantity}/${row.quantity} 部分冲减，生成负数红冲行，确定吗？`
      : `将整行冲销「${row.itemName}」¥${formatMoney(row.amount)}，生成负数红冲行，确定吗？`, '红冲确认', {type: 'warning'}).catch(() => null);
  if (!confirmed)
    return;
  saving.value = true;
  try {
    const res = await reverseFee({
      feeId: row.id,
      quantity: partial ? Number(form.quantity) : undefined,
      reason: form.reason,
    });
    ElMessage.success(`已红冲，生成红冲行 ${res.data?.feeNo || ''}`);
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '红冲失败');
  } finally {
    saving.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    form.quantity = null;
    form.reason = '';
  }
});
</script>
