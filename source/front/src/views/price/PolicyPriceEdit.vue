<template>
  <el-dialog v-model="visible" destroy-on-close title="政策价调价" width="480px">
    <div class="space-y-4">
      <div class="rounded-lg border border-slate-200 p-3 text-sm">
        <div class="flex items-center gap-2">
          <el-icon class="text-slate-400">
            <Tickets/>
          </el-icon>
          <span class="font-medium text-slate-800">{{ form.policyName }} · {{ form.tierLabel }}档</span>
        </div>
        <div class="mt-2 text-slate-500">
          院内项目：{{ form.itemCode }}（{{ form.itemName }}）
          <span v-if="form.ybCode" class="ml-3 font-mono text-xs">国码 {{ form.ybCode }}</span>
        </div>
        <div class="mt-1 text-slate-500">
          当前全额：<span class="font-semibold text-slate-700">¥{{ formatMoney(form.oldAmount) }}</span>
          <template v-if="form.hasSurcharge">
            <span class="ml-3">当前加收：<span class="font-semibold text-slate-700">¥{{ formatMoney(form.oldSurcharge) }}</span></span>
          </template>
        </div>
      </div>
      <el-form label-width="84px">
        <el-form-item label="新全额" required>
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1" class="!w-full"/>
        </el-form-item>
        <el-form-item v-if="form.hasSurcharge" label="新加收额">
          <el-input-number v-model="form.surcharge" :min="0" :precision="2" :step="1" class="!w-full"/>
          <p class="w-full text-xs text-slate-400">该档按「主项目 + 加收」结构出两行费用，改全额时建议一起核对加收额</p>
        </el-form-item>
        <el-form-item label="调价原因" required>
          <el-input v-model="form.reason" :rows="3" placeholder="如：执行湘医保发〔2025〕58号新价" type="textarea"/>
        </el-form-item>
      </el-form>
      <p class="rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
        调价即时生效（运行时实时查价表），并写入调价历史（类型「政策价」），操作人取当前登录人，不可撤回。
      </p>
    </div>
    <template #footer>
      <el-button v-perm="'finance:price:edit'" :loading="submitting" type="primary" @click="submit">确认调价</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 政策价调价（改全额/加收额 + 调价留痕）—— 价格管理页「政策价」页签用。
 *
 * 与五类价表的 PriceEdit 分开：政策价一行 = 政策×档位，调价字段是「全额 + 可选加收额」，
 * 且加收额只在「主项目 + 加收」结构的档位（门诊副主任及以上）出现。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Tickets} from '@element-plus/icons-vue';
import {changePolicyPrice} from '@/api/price';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 政策价行（PolicyPriceAdminVO） */
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const form = reactive({
  priceId: null,
  policyName: '',
  tierLabel: '',
  itemCode: '',
  itemName: '',
  ybCode: '',
  oldAmount: 0,
  oldSurcharge: 0,
  hasSurcharge: false,
  amount: 0,
  surcharge: null,
  reason: '',
});

function fillFromRow(row) {
  form.priceId = row.priceId;
  form.policyName = row.policyName || '';
  form.tierLabel = row.tierLabel || '';
  form.itemCode = row.itemCode || '';
  form.itemName = row.itemName || '';
  form.ybCode = row.ybCode || '';
  form.oldAmount = Number(row.amount ?? 0);
  form.oldSurcharge = Number(row.surcharge ?? 0);
  form.hasSurcharge = row.surcharge !== null && row.surcharge !== undefined;
  form.amount = Number(row.amount ?? 0);
  form.surcharge = form.hasSurcharge ? Number(row.surcharge ?? 0) : null;
  form.reason = '';
}

async function submit() {
  if (!form.reason.trim()) {
    ElMessage.warning('调价原因必填（进调价留痕）');
    return;
  }
  if (form.amount === null || Number(form.amount) < 0) {
    ElMessage.warning('请填写正确的新全额');
    return;
  }
  submitting.value = true;
  try {
    await changePolicyPrice({
      priceId: form.priceId,
      amount: Number(form.amount),
      surcharge: form.hasSurcharge && form.surcharge !== null ? Number(form.surcharge) : null,
      reason: form.reason.trim(),
    });
    ElMessage.success('调价成功，已即时生效并写入调价历史');
    visible.value = false;
    emit('saved');
  } catch (e) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    submitting.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v && props.row) fillFromRow(props.row);
});
</script>
