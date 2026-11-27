<template>
  <el-dialog v-model="visible" title="科室月度成本录入" width="520px">
    <el-form label-width="100px">
      <el-form-item label="科室" required>
        <DeptTreeSelect v-model="costForm.deptId" mode="LEAF" data-testid="perf-cost-dept"
                        class="!w-full" placeholder="选择科室"/>
      </el-form-item>
      <el-form-item label="核算月份" required>
        <el-date-picker v-model="costForm.costMonth" style="width: 100%" type="month" value-format="YYYY-MM"/>
      </el-form-item>
      <el-form-item label="人力成本">
        <el-input-number v-model="costForm.laborCost" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="药品成本">
        <el-input-number v-model="costForm.drugCost" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="耗材成本">
        <el-input-number v-model="costForm.materialCost" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="设备折旧">
        <el-input-number v-model="costForm.depreciation" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="其他成本">
        <el-input-number v-model="costForm.otherCost" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="costForm.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'report:perf:add'" data-testid="perf-cost-submit" type="primary" @click="submitCost">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {perfCostSave} from '@/api/perf';
import {loadDeptTreeOptions, flattenDeptTree} from '@/lib/deptOptions';
import DeptTreeSelect from '@/components/his/DeptTreeSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// 科室名册仅用于提交时回填科室名快照，下拉树由 DeptTreeSelect 自取
const depts = ref([]);
const loadDepts = async () => {
  if (depts.value.length)
    return;
  try {
    const tree = await loadDeptTreeOptions({mode: 'LEAF'});
    depts.value = flattenDeptTree(tree);
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
const deptName = (id) => depts.value.find(d => String(d.id) === String(id))?.deptName || '';

const costForm = reactive({
  deptId: null, costMonth: '', laborCost: 0, drugCost: 0,
  materialCost: 0, depreciation: 0, otherCost: 0, remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  loadDepts();
  costForm.deptId = null;
  const d = new Date();
  costForm.costMonth = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  costForm.laborCost = 0;
  costForm.drugCost = 0;
  costForm.materialCost = 0;
  costForm.depreciation = 0;
  costForm.otherCost = 0;
  costForm.remark = '';
});

const submitCost = async () => {
  if (!costForm.deptId)
    return ElMessage.warning('请选择科室');
  if (!costForm.costMonth)
    return ElMessage.warning('请选择核算月份');
  try {
    await perfCostSave({
      deptId: costForm.deptId, deptName: deptName(costForm.deptId), costMonth: costForm.costMonth,
      laborCost: costForm.laborCost, drugCost: costForm.drugCost, materialCost: costForm.materialCost,
      depreciation: costForm.depreciation, otherCost: costForm.otherCost, remark: costForm.remark,
    });
    ElMessage.success('成本已录入');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
</script>
