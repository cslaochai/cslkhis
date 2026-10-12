<template>
  <el-dialog v-model="visible" destroy-on-close title="科室领用" width="520px">
    <el-form label-width="90px">
      <el-form-item label="选择耗材" required>
        <el-select v-model="form.consumableId" :fit-input-width="false" filterable placeholder="从耗材字典选择"
                   style="width: 100%">
          <el-option v-for="c in consumableOptions" :key="c.id" :label="`${c.consumableName}（${c.specification || '-'}）`"
                     :value="c.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="领用数量" required>
        <el-input-number v-model="form.quantity" :min="0.01" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="领用科室" required>
        <el-select v-model="form.deptId" :fit-input-width="false" filterable placeholder="选择科室"
                   style="width: 100%">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="用途">
        <el-input v-model="form.purpose" :rows="2" placeholder="如 病房日常换药使用" type="textarea"/>
      </el-form-item>
      <p class="pl-2 text-xs text-slate-400">领用将按先过期先出（FEFO）扣减批次库存，并落出入库流水。</p>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'asset:supplies:add'" type="primary" @click="handleConsumeSubmit">确认领用</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {consume, getConsumableSelectList} from '@/api/supplies';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 科室下拉由父页统一加载后传入（溯源登记也在用同一份，避免两处各拉一次）
  deptOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const blankForm = () => ({consumableId: null, quantity: null, deptId: null, purpose: ''});
const consumableOptions = ref([]);
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

const handleConsumeSubmit = async () => {
  const f = form.value;
  if (!f.consumableId)
    return ElMessage.warning('请选择耗材');
  if (!f.quantity || f.quantity <= 0)
    return ElMessage.warning('领用数量必须大于0');
  if (!f.deptId)
    return ElMessage.warning('请选择领用科室');
  try {
    await consume({consumableId: f.consumableId, quantity: f.quantity, deptId: f.deptId, purpose: f.purpose.trim()});
    ElMessage.success('领用成功，库存已按先过期先出扣减');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '领用失败');
  }
};
</script>
