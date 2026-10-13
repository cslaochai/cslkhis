<template>
  <el-dialog v-model="visible" destroy-on-close title="建批入库" width="560px">
    <el-form label-width="90px">
      <el-form-item label="选择耗材" required>
        <el-select v-model="form.consumableId" :fit-input-width="false" filterable placeholder="从耗材字典选择"
                   style="width: 100%">
          <el-option v-for="c in consumableOptions" :key="c.id" :label="`${c.consumableName}（${c.specification || '-'}）`"
                     :value="c.id"/>
        </el-select>
        <p v-if="selectedConsumable()" class="mt-1 text-xs text-slate-400">
          单位：{{ selectedConsumable()?.unit || '-' }}　字典零售价：¥{{ selectedConsumable()?.retailPrice ?? 0 }}
        </p>
      </el-form-item>
      <el-form-item label="批号" required>
        <el-input v-model="form.batchNo" placeholder="如 GZ20260401"/>
      </el-form-item>
      <el-form-item label="生产日期">
        <el-date-picker v-model="form.productionDate" placeholder="选择生产日期" style="width: 100%"
                        type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="有效期至" required>
        <el-date-picker v-model="form.expiryDate" placeholder="选择有效期" style="width: 100%" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="入库数量" required>
        <el-input-number v-model="form.quantity" :min="0.01" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="成本价" required>
        <el-input-number v-model="form.costPrice" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="存放位置">
        <el-input v-model="form.location" placeholder="如 耗材库房A-01"/>
      </el-form-item>
      <el-form-item label="供应商">
        <el-input v-model="form.supplier"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:supplies:add'" type="primary" @click="handleAddSubmit">确认入库</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createStock, getConsumableSelectList} from '@/api/supplies';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const consumableOptions = ref([]);
const blankForm = () => ({
  consumableId: null,
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  quantity: null,
  costPrice: null,
  location: '',
  supplier: ''
});
const form = ref(blankForm());

watch(visible, async (open) => {
  if (!open) return;
  form.value = blankForm();
  try {
    const res = await getConsumableSelectList();
    consumableOptions.value = res.data || [];
  } catch (e) {
    console.error('加载耗材字典失败:', e);
  }
});

const selectedConsumable = () => consumableOptions.value.find((c) => c.id === form.value.consumableId);
const handleAddSubmit = async () => {
  const f = form.value;
  if (!f.consumableId)
    return ElMessage.warning('请从耗材字典选择耗材');
  if (!f.batchNo.trim())
    return ElMessage.warning('批号不能为空');
  if (!f.expiryDate)
    return ElMessage.warning('有效期不能为空');
  if (!f.quantity || f.quantity <= 0)
    return ElMessage.warning('库存数量必须大于0');
  if (f.costPrice == null || f.costPrice < 0)
    return ElMessage.warning('成本价不能为负数');
  try {
    await createStock({
      consumableId: f.consumableId, batchNo: f.batchNo.trim(),
      productionDate: f.productionDate || null, expiryDate: f.expiryDate,
      quantity: f.quantity, costPrice: f.costPrice, location: f.location, supplier: f.supplier,
    });
    ElMessage.success('建批入库成功');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
</script>
