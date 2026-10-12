<template>
  <el-dialog v-model="visible" destroy-on-close title="办理交班（班结）" width="560px">
    <div class="space-y-4" data-testid="fin-handover-dialog">
      <div class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
        交班单是定格凭证：金额按「上次交班时刻 → 现在」这个区间汇总，
        生成后不随后续补录变动。班次只是展示标签，不影响统计区间。
      </div>
      <el-form label-width="96px">
        <el-form-item label="班次" required>
          <el-select v-model="handoverForm.shiftType" class="w-full" data-testid="fin-handover-shift">
            <el-option v-for="o in SHIFT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="实交现金" required>
          <el-input-number
              v-model="handoverForm.handinCash"
              :min="0"
              :precision="2"
              :step="10"
              class="!w-full"
              data-testid="fin-handover-cash"
          />
        </el-form-item>
        <el-form-item label="差异说明">
          <el-input
              v-model="handoverForm.diffReason"
              :rows="2"
              data-testid="fin-handover-reason"
              placeholder="实交现金与系统现金不一致时必填（后端强校验）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="handoverForm.remark" :rows="2" placeholder="选填" type="textarea"/>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
          v-perm="'finance:settlement:edit'"
          :loading="handoverSubmitting"
          data-testid="fin-handover-submit"
          type="primary"
          @click="submitHandover"
      >
        确认交班
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {handover} from '@/api/financeSettlement';
import {SHIFT_TYPE_OPTIONS} from '@/lib/financeSettlement';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const handoverSubmitting = ref(false);
const handoverForm = reactive({
  shiftType: 1,
  handinCash: null,
  diffReason: '',
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  handoverForm.shiftType = 1;
  handoverForm.handinCash = null;
  handoverForm.diffReason = '';
  handoverForm.remark = '';
});

async function submitHandover() {
  if (handoverForm.shiftType === null) {
    ElMessage.warning('请选择班次');
    return;
  }
  if (handoverForm.handinCash === null || Number(handoverForm.handinCash) < 0) {
    ElMessage.warning('请填写实际清点并上交的现金金额');
    return;
  }
  handoverSubmitting.value = true;
  try {
    const res = await handover({
      shiftType: handoverForm.shiftType,
      handinCash: handoverForm.handinCash,
      diffReason: handoverForm.diffReason.trim() || undefined,
      remark: handoverForm.remark.trim() || undefined,
      // cashierId 不传：服务端取登录态，前端传「我是谁」等于让交班单能挂到别人名下
    });
    const vo = res.data || {};
    const diff = Number(vo.cashDiff ?? 0);
    if (diff === 0) {
      ElMessage.success(`交班成功（${vo.settlementNo || ''}）现金账实相符`);
    } else {
      ElMessage.warning(`交班成功（${vo.settlementNo || ''}）现金差异 ${diff > 0 ? '+' : ''}${formatMoney(diff)} 元，已留痕`);
    }
    visible.value = false;
    emit('saved', vo);
  } catch (e) {
    ElMessage.error(e?.message || '交班失败');
  } finally {
    handoverSubmitting.value = false;
  }
}
</script>
