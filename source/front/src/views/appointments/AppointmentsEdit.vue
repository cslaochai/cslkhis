<template>
  <el-dialog v-model="visible" destroy-on-close title="修改挂号" width="600px">
    <el-form :model="editForm" label-position="top">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="选择科室" required>
          <el-select v-model="editForm.deptId" class="w-full" placeholder="选择科室" @change="handleEditDeptChange">
            <el-option v-for="d in departments" :key="d.id"
                       :label="d.parentDeptName ? `${d.parentDeptName} / ${d.deptName}` : d.deptName"
                       :value="d.id ?? d.deptName"/>
          </el-select>
        </el-form-item>
        <el-form-item label="选择医生">
          <el-select v-model="editForm.doctorId" class="w-full" placeholder="选择医生">
            <el-option v-for="e in employeeList" :key="e.id" :label="`${e.empName} - ${e.title || ''}`"
                       :value="e.id ?? e.empName"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="预约日期" required>
          <el-date-picker
              v-model="editForm.visitDate"
              :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
              class="w-full"
              placeholder="选择日期"
              type="date"
              value-format="YYYY-MM-DD"
              @change="handleEditDateChange"
          />
        </el-form-item>
        <el-form-item label="就诊类型">
          <el-select v-model="editForm.visitType" class="w-full">
            <el-option v-for="item in VISIT_TYPE" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="选择号源" required>
        <el-select v-model="editForm.scheduleId" :disabled="!editSchedules.length" class="w-full"
                   placeholder="请先选择科室和日期">
          <el-option
              v-for="s in editSchedules"
              :key="s.id"
              :label="`${s.doctorName} | ${s.startTime}-${s.endTime} | 余号:${s.availableSource} | 挂号费:¥${s.registFee}`"
              :value="s.id ?? s.doctorName"
          />
        </el-select>
        <p v-if="editForm.deptId && editForm.visitDate && !editSchedules.length" class="mt-1 text-xs text-slate-400">
          该日期无可挂号源
        </p>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'opd:appointments:edit'" :loading="submitLoading" type="primary" @click="handleEditSubmit">
        确认修改
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAvailableSchedule, updateRegistration} from '@/api/appoint';
import {getEmployeeList} from '@/api/system';
import {VISIT_TYPE} from '@/lib/dict';

// 编辑挂号弹窗（从 AppointmentsView 拆出）。改的是挂号单的号源指向（updateRegistration 换
// scheduleId / visitType），不碰号源余量 —— 占号/释放由后端在同一事务里处理。
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 要修改的挂号行 */
  row: {type: Object, default: null},
  /** 科室候选（页面级参照数据，主页面按 deptType:1 加载后传入） */
  departments: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const submitLoading = ref(false);
const editForm = ref({
  id: null,
  deptId: null,
  doctorId: null,
  scheduleId: null,
  visitDate: '',
  visitType: '',
});
const employeeList = ref([]);
const editSchedules = ref([]);
watch(visible, (open) => {
  if (!open)
    return;
  const row = props.row || {};
  editForm.value = {
    id: row.id,
    deptId: row.deptId,
    doctorId: row.doctorId,
    scheduleId: null,
    visitDate: row.visitDate || '',
    visitType: row.visitType || 1,
  };
  editSchedules.value = [];
  // 加载当前科室的医生列表
  if (row.deptId) {
    loadEmployeeList(row.deptId);
  } else {
    employeeList.value = [];
  }
});
const loadEmployeeList = async (deptId) => {
  try {
    const res = await getEmployeeList({empType: 1, deptId});
    employeeList.value = res.data?.records || res.data || [];
  } catch (e) {
    employeeList.value = [];
  }
};
const handleEditDeptChange = async (deptId) => {
  editForm.value.doctorId = null;
  editForm.value.scheduleId = null;
  editSchedules.value = [];
  try {
    const res = await getEmployeeList({empType: 1, deptId});
    employeeList.value = res.data?.records || res.data || [];
  } catch (e) {
    employeeList.value = [];
  }
};
const handleEditDateChange = async () => {
  editForm.value.scheduleId = null;
  editSchedules.value = [];
  if (!editForm.value.deptId || !editForm.value.visitDate)
    return;
  try {
    const res = await getAvailableSchedule(editForm.value.deptId, editForm.value.visitDate);
    editSchedules.value = res.data || [];
  } catch (e) {
    editSchedules.value = [];
  }
};
const handleEditSubmit = async () => {
  if (!editForm.value.deptId || !editForm.value.visitDate || !editForm.value.scheduleId) {
    ElMessage.warning('请完善修改信息');
    return;
  }
  submitLoading.value = true;
  try {
    await updateRegistration({
      id: editForm.value.id,
      scheduleId: editForm.value.scheduleId,
      visitType: editForm.value.visitType,
    });
    ElMessage.success('修改成功');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '修改失败');
  } finally {
    submitLoading.value = false;
  }
};
</script>
