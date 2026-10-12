<template>
  <!-- 转住院：一次动作完成入院登记（途径=急诊）+ 急诊终态，不再"只翻状态不落入院" -->
  <el-dialog v-model="visible" destroy-on-close title="急诊转住院（入院登记）" width="560px">
    <el-form :model="admitForm" label-position="top">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="患者">
          <el-input :model-value="admitForm.patientName" disabled/>
        </el-form-item>
        <el-form-item label="入院途径">
          <el-input disabled model-value="急诊"/>
        </el-form-item>
        <el-form-item label="入院病区" required>
          <el-select v-model="admitForm.wardId" class="!w-full" filterable placeholder="选择病区"
                     @change="onAdmitWardChange">
            <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)">
              <span>{{ w.wardName }}</span>
              <span :class="w.freeBeds > 0 ? 'text-emerald-500' : 'text-rose-500'" class="float-right text-xs">
                空闲 {{ w.freeBeds }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="入院床位" required>
          <el-select v-model="admitForm.bedId" :disabled="!admitForm.wardId" class="!w-full" placeholder="先选病区">
            <el-option v-for="b in admitBeds" :key="b.bedId" :label="b.bedNo" :value="String(b.bedId)"/>
          </el-select>
          <p v-if="admitForm.wardId && !admitBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
        </el-form-item>
        <el-form-item label="入院医生" required>
          <el-select v-model="admitForm.admitDoctorId" class="!w-full" filterable placeholder="选择医生">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="入院诊断">
        <el-input v-model="admitForm.diagnosis" placeholder="默认取急诊初步诊断，可修改"/>
      </el-form-item>
      <p class="text-xs text-slate-400">
        确认后同步办理：生成入院记录（占住院床位）、释放留观床（若在观）、急诊号挂号单与叫号队列置为已就诊。
      </p>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="admitSubmitting" type="success" @click="submitAdmit">确认入院登记</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {admitEmergency, getEmergencyBedSelectList} from '@/api/emergency';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 病区名册由主页面加载一次（转入留观弹框共用同一份），子组件不许再拉一遍
  wards: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const admitSubmitting = ref(false);
const admitBeds = ref([]);
const employees = ref([]);
const admitForm = ref({
  id: '',
  patientName: '',
  wardId: null,
  bedId: null,
  admitDoctorId: null,
  diagnosis: '',
});

const loadEmployeesForAdmit = async () => {
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []).map((e) => ({id: String(e.id), empName: e.empName}));
  } catch {
    employees.value = [];
  }
};

watch(visible, async (open) => {
  if (!open)
    return;
  admitForm.value = {
    id: props.row?.id, patientName: props.row?.patientName, wardId: null, bedId: null,
    admitDoctorId: props.row?.doctorId || null, diagnosis: props.row?.diagnosis || '',
  };
  admitBeds.value = [];
  if (employees.value.length === 0)
    await loadEmployeesForAdmit();
});

const onAdmitWardChange = async (wardId) => {
  admitForm.value.bedId = null;
  admitBeds.value = [];
  if (!wardId)
    return;
  try {
    const res = await getEmergencyBedSelectList({wardId, bedStatus: 1});
    admitBeds.value = res.data || [];
  } catch {
    admitBeds.value = [];
  }
};

const submitAdmit = async () => {
  if (!admitForm.value.wardId)
    return ElMessage.warning('请选择入院病区');
  if (!admitForm.value.bedId)
    return ElMessage.warning('请选择入院床位');
  if (!admitForm.value.admitDoctorId)
    return ElMessage.warning('请选择入院医生');
  admitSubmitting.value = true;
  try {
    const res = await admitEmergency({...admitForm.value});
    ElMessage.success(`转住院成功，入院登记已办理（入院ID ${res.data}）`);
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '转住院失败');
  } finally {
    admitSubmitting.value = false;
  }
};
</script>
