<template>
  <el-dialog v-model="visible" destroy-on-close title="调价" width="480px">
    <div class="space-y-4">
      <div class="rounded-lg border border-slate-200 p-3 text-sm">
        <div class="flex items-center gap-2">
          <el-icon class="text-slate-400">
            <Tickets/>
          </el-icon>
          <span class="font-medium text-slate-800">{{ form.itemName || '—' }}</span>
          <span class="font-mono text-xs text-slate-400">{{ form.itemCode }}</span>
        </div>
        <div class="mt-2 text-slate-500">
          类型：{{ itemTypes.find(t => t.key === form.itemType)?.label || form.itemType }}
          <span class="ml-3">当前价：<span class="font-semibold text-slate-700">¥{{
              formatMoney(form.oldPrice)
            }}</span></span>
        </div>
      </div>
      <el-form label-width="84px">
        <el-form-item label="新价格" required>
          <el-input-number v-model="form.newPrice" :min="0" :precision="2" :step="1" class="!w-full"/>
        </el-form-item>
        <el-form-item label="调价原因" required>
          <el-input v-model="form.reason" :rows="3" placeholder="如：执行省级药品集中采购新价" type="textarea"/>
        </el-form-item>
      </el-form>
      <p class="rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
        调价会即时更新价表并写入调价历史，操作人取当前登录人，不可撤回。
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
 * 价表调价（改价 + 调价留痕）—— 从 views/price/PriceView.vue 拆出
 *
 * 原行只从列表带入（itemType/itemId/原价），新价与原价的差额不在本页解释，
 * 调价原因必填、由后端写进调价历史；操作人取当前登录人。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Tickets} from '@element-plus/icons-vue';
import {changePrice} from '@/api/price';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 价表行 */
  row: {type: Object, default: null},
  /** 5 类价表（页签与调价历史同源，由页面传下） */
  itemTypes: {type: Array, default: () => []},
  /** 当前页签类型（行上没有 itemType 时的兜底，与页面 activeTab 同源） */
  defaultItemType: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const form = reactive({
  itemType: '',
  itemId: '',
  itemCode: '',
  itemName: '',
  oldPrice: 0,
  newPrice: 0,
  reason: '',
});

function fillFromRow(row) {
  form.itemType = row.itemType || props.defaultItemType;
  form.itemId = row.itemId;
  form.itemCode = row.itemCode || '';
  form.itemName = row.itemName || '';
  form.oldPrice = Number(row.price ?? 0);
  form.newPrice = Number(row.price ?? 0);
  form.reason = '';
}

async function submit() {
  if (!form.reason.trim()) {
    ElMessage.warning('调价原因必填（进调价留痕）');
    return;
  }
  if (form.newPrice === null || Number(form.newPrice) < 0) {
    ElMessage.warning('请填写正确的新价格');
    return;
  }
  submitting.value = true;
  try {
    await changePrice({
      itemType: form.itemType,
      itemId: form.itemId,
      newPrice: Number(form.newPrice),
      reason: form.reason.trim(),
    });
    ElMessage.success('调价成功，已写入调价历史');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '调价失败');
  } finally {
    submitting.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v && props.row) fillFromRow(props.row);
});
</script>
