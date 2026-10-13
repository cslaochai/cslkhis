<template>
  <!-- 入库/出库弹窗 -->
  <el-dialog v-model="visible" :title="isIn ? '入库' : '出库'" destroy-on-close width="400px">
    <div class="space-y-4 py-2">
      <div class="rounded-lg bg-slate-50 p-3">
        <p class="text-sm text-slate-500">药品</p>
        <p class="font-medium text-slate-900">{{ row?.drugName }}</p>
      </div>
      <div class="rounded-lg bg-slate-50 p-3">
        <p class="text-sm text-slate-500">当前库存</p>
        <p class="font-medium text-slate-900">{{ currentQty }}</p>
      </div>
      <el-form-item :label="isIn ? '入库数量' : '出库数量'" class="!mb-0">
        <el-input-number v-model="quantity" :max="isIn ? 9999 : currentQty" :min="1" class="w-full"/>
      </el-form-item>
    </div>
    <template #footer>
      <el-button v-perm="'pharmacy:stock:edit'" :type="isIn ? 'success' : 'warning'" @click="handleSubmit">
        {{ isIn ? '确认入库' : '确认出库' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 入库/出库弹窗 —— 从 views/pharmacy/PharmacyView.vue 抽出
 *
 * 对既有批次做数量增减（同一批次 id）：入库上账、出库受当前库存上限约束。口径不变。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { inboundStock, outboundStock } from '@/api/pharmacy';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
  type: { type: String, default: 'in' },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const isIn = computed(() => props.type === 'in');
const currentQty = computed(() => Number(props.row?.quantity || 0));
const quantity = ref(1);

const handleSubmit = async () => {
  const stockId = props.row?.id;
  if (!stockId || quantity.value <= 0) {
    ElMessage.warning('请输入有效数量');
    return;
  }
  try {
    if (isIn.value) {
      await inboundStock(stockId, quantity.value);
      ElMessage.success('入库成功');
    } else {
      if (quantity.value > currentQty.value) {
        ElMessage.warning('出库数量不能大于当前库存');
        return;
      }
      await outboundStock(stockId, quantity.value);
      ElMessage.success('出库成功');
    }
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  }
};

watch(() => props.modelValue, (v) => {
  if (v) quantity.value = 1;
});
</script>
