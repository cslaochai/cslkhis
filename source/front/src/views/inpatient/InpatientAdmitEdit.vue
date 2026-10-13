<template>
  <el-dialog
      v-model="visible"
      :title="admitFromOrder ? '按住院证收治入院' : '入院登记（分床并占用床位）'"
      destroy-on-close
      width="720px"
  >
    <div v-if="admitFromOrder" class="mb-4 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-slate-600">
      <div class="font-semibold text-slate-800">住院证 {{ admitFromOrder.orderNo }}</div>
      <div class="mt-1">
        来源门诊：{{ admitFromOrder.sourceDeptName || '—' }} · {{ admitFromOrder.sourceDoctorName || '—' }}
        （挂号号 {{ admitFromOrder.registNo || '—' }}）
      </div>
      <div>拟收治：{{ admitFromOrder.applyDeptName || '—' }} · 拟诊：{{ admitFromOrder.diagnosisName || '—' }}</div>
      <div class="mt-1 text-slate-500">
        入院途径由证决定（门诊转住院），患者与拟诊已锁定；入院处只需选病区、床位、入院医生。
        如实际收治科室与拟收治不同，系统会记为「调科」。
      </div>
    </div>
    <el-form :model="admitForm" label-width="110px">
      <el-form-item label="患者" required>
        <PatientSelect
            v-model="admitForm.patientId"
            :disabled="!!admitFromOrder"
            placeholder="输入姓名 / 患者号 / 手机号 / 身份证号搜索"
            @select="handlePatientSelectForAdmit"
        />
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="病区" required>
          <el-select v-model="admitForm.wardId" class="!w-full" placeholder="选择病区" @change="onAdmitWardChange">
            <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId">
              <span>{{ w.wardName }}</span>
              <span :class="w.freeBeds > 0 ? 'text-emerald-500' : 'text-rose-500'" class="float-right text-xs">
                空闲 {{ w.freeBeds }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="床位" required>
          <el-select v-model="admitForm.bedId" :disabled="!admitForm.wardId" class="!w-full" placeholder="先选病区">
            <el-option v-for="b in admitBeds" :key="b.bedId" :label="b.bedNo" :value="b.bedId"/>
          </el-select>
          <p v-if="admitForm.wardId && !admitBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
        </el-form-item>
        <el-form-item label="入院途径" required>
          <el-select v-model="admitForm.admitWay" :disabled="!!admitFromOrder" class="!w-full">
            <el-option v-for="(label, code) in admitWayMap" :key="code" :label="label" :value="Number(code)"/>
          </el-select>
          <p v-if="admitFromOrder" class="mt-1 text-xs text-amber-600">有住院证 =
            门诊转住院，途径固定为「门诊」，不允许手填</p>
        </el-form-item>
        <el-form-item label="入院医生" required>
          <el-select v-model="admitForm.admitDoctorId" class="!w-full" placeholder="选择医生">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="入院时间">
          <el-date-picker
              v-model="admitForm.admitTime"
              class="!w-full"
              placeholder="不填取当前时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="入院诊断编码">
          <el-input v-model="admitForm.admitDiagnosisCode" placeholder="ICD-10，如 J18.9"/>
        </el-form-item>
        <el-form-item label="入院诊断名称">
          <el-input v-model="admitForm.admitDiagnosisName" placeholder="如 肺炎"/>
        </el-form-item>
      </div>
      <el-form-item label="入院诊断(文本)">
        <el-input v-model="admitForm.diagnosis" :rows="2" placeholder="入院时的初步诊断描述" type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="admitForm.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'ipd:inpatient:add'" :loading="admitLoading" type="primary" @click="submitAdmit">
        {{ admitFromOrder ? '确认收治' : '确认登记' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {admitInpatient, getInpatientBedList} from '@/api/inpatient';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {admitWayMap} from '@/lib/inpatient';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 非空表示本次入院是「按住院证收治」，表单会锁掉患者与入院途径（由证决定） */
  admitFromOrder: {type: Object, default: null},
  // 病区列表由主页面加载一次（查询条件也要用），以 prop 传下来
  wards: {type: Array, default: () => []},
  // 医生名册由主页面加载一次（出院办理弹框共用），以 prop 传下来
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const admitLoading = ref(false);
const admitBeds = ref([]);
const admitForm = reactive({
  patientId: null,
  admissionOrderId: null,
  wardId: null,
  bedId: null,
  admitDoctorId: null,
  admitTime: '',
  admitWay: 1,
  diagnosis: '',
  admitDiagnosisCode: '',
  admitDiagnosisName: '',
  remark: '',
});

watch(visible, async (open) => {
  if (!open)
    return;
  if (props.admitFromOrder) {
    // 按住院证收治：患者、入院途径、拟诊都由证决定，入院处只需要选病区/床位/入院医生。
    // 病区默认按证的「拟收治科室」自动带出——只有一个匹配病区时才自动选，
    // 多个病区时留空让入院处自己选，避免替人做决定后收错病区。
    const row = props.admitFromOrder;
    Object.assign(admitForm, {
      patientId: row.patientId,
      admissionOrderId: row.id,
      wardId: null, bedId: null, admitDoctorId: null, admitTime: '',
      admitWay: 1,
      diagnosis: row.diagnosisName || '',
      admitDiagnosisCode: row.diagnosisCode || '',
      admitDiagnosisName: row.diagnosisName || '',
      remark: `由住院证 ${row.orderNo} 收治`,
    });
    admitBeds.value = [];
    const matched = props.wards.filter((w) => String(w.deptId) === String(row.applyDeptId));
    if (matched.length === 1) {
      admitForm.wardId = matched[0].wardId;
      await onAdmitWardChange(matched[0].wardId);
    }
  } else {
    Object.assign(admitForm, {
      patientId: null, admissionOrderId: null, wardId: null, bedId: null, admitDoctorId: null, admitTime: '',
      admitWay: 1, diagnosis: '', admitDiagnosisCode: '', admitDiagnosisName: '', remark: '',
    });
    admitBeds.value = [];
  }
});

// 患者远程搜索已抽成 PatientSelect 组件
const handlePatientSelectForAdmit = (patient) => {
  if (!patient)
    return;
  // PatientSelect @select 回调已给出完整患者对象，直接用
};
const onAdmitWardChange = async (wardId) => {
  admitForm.bedId = null;
  admitBeds.value = [];
  if (!wardId)
    return;
  try {
    // 只列空闲床位：占用/维修/锁定的床选了也会被后端拒，不如一开始就不给选
    const res = await getInpatientBedList({wardId, bedStatus: 1});
    admitBeds.value = res.data || [];
  } catch {
    admitBeds.value = [];
  }
};
const submitAdmit = async () => {
  if (!admitForm.patientId)
    return ElMessage.warning('请选择患者');
  if (!admitForm.wardId)
    return ElMessage.warning('请选择病区');
  if (!admitForm.bedId)
    return ElMessage.warning('请选择床位');
  if (!admitForm.admitDoctorId)
    return ElMessage.warning('请选择入院医生');
  admitLoading.value = true;
  try {
    const ordered = !!admitForm.admissionOrderId;
    const res = await admitInpatient({...admitForm});
    ElMessage.success(ordered
        ? `按住院证收治成功（入院ID ${res.data}）；住院证已置为「已收治」并回填入院ID`
        : `入院登记成功，住院号已生成（入院ID ${res.data}）`);
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    admitLoading.value = false;
  }
};
</script>
