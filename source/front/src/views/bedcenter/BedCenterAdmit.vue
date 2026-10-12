<template>
  <!-- ==================== 办理入院 ==================== -->
  <el-dialog v-model="visible" title="按已安排床位办理入院" width="560px">
    <el-form label-width="110px" size="small">
      <el-form-item label="床位">
        <span class="text-sm text-slate-700" data-testid="bc-admit-bed">
          {{ row?.assignedDeptName }} · {{ row?.assignedWardName }} · {{ row?.assignedBedNo }}床
          <el-tag v-if="row?.crossDept" class="ml-1" size="small" type="warning">跨科调配</el-tag>
        </span>
      </el-form-item>
      <el-form-item label="入院医生" required>
        <el-select v-model="admitForm.admitDoctorId" :fit-input-width="false" class="!w-full" data-testid="bc-admit-doctor"
                   filterable placeholder="选择医生">
          <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="!row?.admissionOrderId" label="入院途径" required>
        <el-select v-model="admitForm.admitWay" class="!w-full" data-testid="bc-admit-way" placeholder="病案首页必填">
          <el-option :value="2" label="急诊"/>
          <el-option :value="3" label="转院"/>
          <el-option :value="4" label="其他"/>
        </el-select>
      </el-form-item>
      <el-form-item v-else label="入院途径">
        <span class="text-sm text-slate-500">门诊（按来源住院证自动确定）</span>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="admitForm.remark" data-testid="bc-admit-remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" data-testid="bc-admit-submit" type="primary" @click="submitAdmit">确认入院
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 按已安排床位办理入院（表单态 + 提交在本组件；医生候选只这里用，组件内自 load）
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEmployeeList} from '@/api/system';
import {admitBedWait} from '@/api/bedCenter';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 排队记录（含 assigned* 床位快照与 admissionOrderId 来源）
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const employees = ref([]);
const admitForm = reactive({admitDoctorId: null, admitWay: null, remark: ''});
const loadEmployees = async () => {
  if (employees.value.length)
    return;
  try {
    const res = await getEmployeeList({empType: 1});
    if (res.code === 200)
      employees.value = res.data?.records || res.data || [];
  } catch {
    employees.value = [];
  }
};
watch(() => props.modelValue, async (v) => {
  if (!v)
    return;
  admitForm.admitDoctorId = null;
  admitForm.admitWay = props.row?.admissionOrderId ? 1 : null;
  admitForm.remark = '';
  await loadEmployees();
});
const submitAdmit = async () => {
  if (!admitForm.admitDoctorId)
    return ElMessage.warning('请选择入院医生');
  if (!props.row?.admissionOrderId && !admitForm.admitWay)
    return ElMessage.warning('请选择入院途径');
  submitting.value = true;
  try {
    const res = await admitBedWait({
      waitId: String(props.row.id),
      admitDoctorId: admitForm.admitDoctorId,
      admitWay: admitForm.admitWay,
      remark: admitForm.remark || null,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '入院登记成功');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '入院失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '入院失败');
  } finally {
    submitting.value = false;
  }
};
</script>
