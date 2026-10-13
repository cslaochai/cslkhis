<template>
  <el-dialog v-model="visible" :title="applyForm.id ? '修改预约登记' : '日间手术预约登记'" width="640px">
    <el-form label-width="120px" size="small">
      <el-form-item label="准入术式" required>
        <el-select v-model="applyForm.itemId" filterable placeholder="仅启用中的术式" style="width: 100%">
          <el-option v-for="d in itemOptions" :key="d.id" :label="`${d.itemCode} ${d.itemName}`" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="患者" required>
        <PatientSelect :model-value="applyForm.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                       @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="手术科室">
        <el-select v-model="applyForm.deptId" clearable filterable placeholder="请选择" style="width: 100%">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="计划手术日期" required>
        <el-date-picker v-model="applyForm.planSurgeryDate" placeholder="请选择" style="width: 100%"
                        type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="applyForm.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="applySaving" type="primary" @click="saveApply">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {daySurgeryApplyUpsert, getDaySurgeryDetail} from '@/api/daySurgery';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  itemOptions: {type: Array, default: () => []},
  deptOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const applySaving = ref(false);
const applyForm = reactive({
  id: null,
  itemId: null,
  patientId: null,
  patientName: '',
  deptId: null,
  planSurgeryDate: '',
  remark: '',
});
const onPatientSelect = (p) => {
  applyForm.patientId = p?.id ?? null;
  applyForm.patientName = p?.name || p?.patientName || '';
};
watch(visible, async (open) => {
  if (!open) return;
  Object.assign(applyForm, {
    id: null, itemId: null, patientId: null, patientName: '', deptId: null,
    planSurgeryDate: '', remark: '',
  });
  if (!props.row?.id) return;
  const res = await getDaySurgeryDetail(props.row.id);
  const d = res?.data || props.row;
  Object.assign(applyForm, {
    id: d.id, itemId: d.itemId ?? null, patientId: d.patientId ?? null, patientName: d.patientName || '',
    deptId: d.deptId ?? null, planSurgeryDate: (d.planSurgeryDate || '').slice(0, 10), remark: d.remark || '',
  });
});
const saveApply = async () => {
  if (!applyForm.itemId) {
    ElMessage.warning('请选择准入术式');
    return;
  }
  if (!applyForm.patientId) {
    ElMessage.warning('请选择患者');
    return;
  }
  if (!applyForm.planSurgeryDate) {
    ElMessage.warning('请选择计划手术日期');
    return;
  }
  applySaving.value = true;
  try {
    const res = await daySurgeryApplyUpsert({
      id: applyForm.id || undefined,
      itemId: applyForm.itemId,
      patientId: applyForm.patientId,
      deptId: applyForm.deptId || undefined,
      planSurgeryDate: applyForm.planSurgeryDate,
      remark: applyForm.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(applyForm.id ? '修改成功' : `预约登记成功，单号 ${res.data?.applyNo || ''}`);
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    applySaving.value = false;
  }
};
</script>
