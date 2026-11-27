<template>
  <!-- 登记 -->
  <el-dialog v-model="visible" title="医废登记" width="520px">
    <el-form label-width="100px">
      <el-form-item label="医废类别" required>
        <el-select v-model="createForm.wasteType" :fit-input-width="false" style="width: 220px">
          <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="重量(kg)">
        <el-input-number v-model="createForm.weightKg" :min="0" :precision="2" style="width: 180px"/>
      </el-form-item>
      <el-form-item label="产生科室" required>
        <DeptTreeSelect v-model="createForm.deptId" mode="LEAF" width="220px" placeholder="请选择科室"
                        @change="onCreateDeptChange"/>

      </el-form-item>
      <el-form-item label="收集时间" required>
        <el-date-picker v-model="createForm.collectTime" format="YYYY-MM-DD HH:mm:ss" style="width: 220px"
                        type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="收集人">
        <el-select v-model="createForm.collectorName" :fit-input-width="false" allow-create clearable
                   default-first-option filterable placeholder="不填默认当前登录人" style="width: 220px">
          <el-option v-for="e in employeeOptions" :key="e.id" :label="e.empName || e.name"
                     :value="e.empName || e.name"/>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:waste:add'" type="primary" @click="submitCreate">登记</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 医废登记（G22）：只新增，无修改语义 —— 交接/处置是状态推进，另走 WasteHandoverEdit / WasteDisposeEdit
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {wasteCreate} from '@/api/waste';
import {getEmployeeList} from '@/api/system';
import {loadDeptTreeOptions, flattenDeptTree} from '@/lib/deptOptions';
import DeptTreeSelect from '@/components/his/DeptTreeSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 医废类别字典：列表列也要用，由父页面加载一次传下来
  typeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const deptOptions = ref([]);
const employeeOptions = ref([]);
const loadDeptsAndEmployees = async () => {
  try {
    const [tree, e] = await Promise.all([loadDeptTreeOptions({mode: 'LEAF'}), getEmployeeList({})]);
    deptOptions.value = flattenDeptTree(tree);
    employeeOptions.value = e?.data || [];
  } catch (e) {
    console.error('加载科室/员工失败', e);
  }
};

const createForm = reactive({
  wasteType: 1, weightKg: '', deptId: null, deptName: '',
  collectTime: '', collectorName: '',
});
const nowLocal = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const onCreateDeptChange = (id) => {
  const d = deptOptions.value.find((x) => String(x.id) === String(id));
  createForm.deptName = d?.deptName || '';
};

watch(visible, (open) => {
  if (!open) return;
  Object.assign(createForm, {
    wasteType: 1, weightKg: '', deptId: null, deptName: '', collectTime: nowLocal(), collectorName: '',
  });
  if (!deptOptions.value.length && !employeeOptions.value.length) loadDeptsAndEmployees();
});

const submitCreate = async () => {
  if (!createForm.deptId) {
    ElMessage.warning('请选择产生科室');
    return;
  }
  if (!createForm.collectTime) {
    ElMessage.warning('请选择收集时间');
    return;
  }
  try {
    const res = await wasteCreate({
      wasteType: createForm.wasteType,
      weightKg: createForm.weightKg || undefined,
      deptId: createForm.deptId ?? undefined,
      deptName: createForm.deptName.trim(),
      collectTime: createForm.collectTime,
      collectorName: createForm.collectorName || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(`登记成功，单号 ${res.data?.wasteNo}`);
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  }
};
</script>
