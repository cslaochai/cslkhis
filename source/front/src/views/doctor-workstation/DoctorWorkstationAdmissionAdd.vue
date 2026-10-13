<template>
  <el-dialog v-model="visible" destroy-on-close title="开住院证（入院通知单）" width="640px">
    <div v-if="patient"
         class="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-xs text-slate-600">
      <div class="font-semibold text-slate-800">患者：{{ patient.patientName }}（{{
          patient.patientNo || '—'
        }}）
      </div>
      <div class="mt-1">
        本次挂号：{{ patient.registNo || '—' }} · 开证科室：{{ patient.deptName || '—' }}
        · 开证医生：{{ patient.doctorName || '—' }}
      </div>
      <div class="mt-1 text-slate-500">
        开证后患者持证到「入出院管理 → 待收治（住院证）」排床收治；入院记录会记下这张证的号与本次挂号号。
      </div>
    </div>
    <el-form :model="orderForm" label-width="110px">
      <el-form-item label="拟收治科室" required>
        <el-select v-model="orderForm.applyDeptId" class="!w-full" filterable placeholder="选择收治科室">
          <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
        </el-select>
        <p class="mt-1 text-xs text-slate-400">住哪个科是临床决策；入院处收治时可以调科，调科会被系统标记出来</p>
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="拟诊编码">
          <el-input v-model="orderForm.diagnosisCode" placeholder="ICD-10，如 J18.9"/>
        </el-form-item>
        <el-form-item label="拟诊名称" required>
          <el-input v-model="orderForm.diagnosisName" placeholder="如 社区获得性肺炎"/>
        </el-form-item>
      </div>
      <el-form-item label="预计入院时间">
        <el-date-picker
            v-model="orderForm.expectAdmitTime"
            class="!w-full"
            placeholder="不填表示由患者自行择期"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="收治说明">
        <el-input v-model="orderForm.diagnosisNote" :rows="3" placeholder="病情摘要、收治理由、需注意的事项"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="orderForm.remark"/>
      </el-form-item>
    </el-form>
    <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
      同一挂号只能有一张有效住院证：重复开证会被拒绝，需要变更请让入院处先作废原证。
      住院证默认 7 天有效（系统参数 <span class="font-mono">admission_order.valid_days</span>），过期后不能再收治。
    </div>
    <template #footer>
      <el-button v-perm="'opd:doctorWorkstation:add'" :loading="orderSubmitting" type="primary" @click="submitOrder">
        开具住院证
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createAdmissionOrder} from '@/api/admissionOrder';
import {getDepartmentSelectList} from '@/api/system';

// ------------------------------------------------------------------
// 开住院证（门诊 → 住院的入口），从 DoctorWorkstationView 拆出：接诊患者、患者详情、
// 病历诊断都由父页面以 props 传来，这里不重拉已有数据。
//
// 门诊医生判断患者需要住院时，在这里开一张住院证。开完患者拿着证到入院处排床收治，
// 入院记录会记下：来源挂号号、来源住院证号、开证科室与医生、拟诊 —— 这就是"打通"。
//
// 不能替医生猜的事情一律不猜：
//  · 拟收治科室必须医生选（患者住哪个科是临床决策，不是系统能推的）；
//  · 拟诊默认带过来自「诊断」输入框的值，医生可以改，改了就以改的为准。
// ------------------------------------------------------------------
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 当前接诊患者（队列行） */
  patient: {type: Object, default: null},
  /** 患者详情：补齐证件与医保字段；拟诊也优先取这里的诊断 */
  patientDetail: {type: Object, default: null},
  /** 病历里已录入的诊断名称/编码（拟诊预填的兜底） */
  diagnosisCode: {type: String, default: ''},
  diagnosisName: {type: String, default: ''},
});
// 原实现开证成功后只关窗、不刷页面数据（开证不动医生站任何列表），契约保持不变
const emit = defineEmits(['update:modelValue']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const orderSubmitting = ref(false);
const departments = ref([]);
const orderForm = reactive({
  // 科室 ID 也用字符串承载：后端是 Long，Jackson 能精确解析数字字符串，
  // 而前端一旦 Number() 就会在不经意间改掉大 ID 的末几位。
  applyDeptId: '',
  diagnosisCode: '',
  diagnosisName: '',
  diagnosisNote: '',
  expectAdmitTime: '',
  remark: '',
});
// 拟诊预填：优先取病历里已录入的诊断名称/编码，没有就留空让医生填（不猜）
const resetOrderForm = () => {
  const diagCode = (props.patientDetail?.diagnosisCode || props.diagnosisCode || '').split(',')[0]?.trim() || '';
  const diagName = (props.patientDetail?.diagnosisName || props.diagnosisName || '').split(',')[0]?.trim() || '';
  Object.assign(orderForm, {
    applyDeptId: '',
    diagnosisCode: diagCode,
    diagnosisName: diagName,
    diagnosisNote: '',
    expectAdmitTime: '',
    remark: '',
  });
};
watch(() => props.modelValue, (v) => {
  if (v)
    resetOrderForm();
});
const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤（医生站只看自己被授权的科室）
    const res = await getDepartmentSelectList({deptType: 1});
    departments.value = res.data || [];
  } catch (error) {
    console.error('加载科室失败:', error);
  }
};
onMounted(() => {
  loadDepartments();
});
const submitOrder = async () => {
  if (!orderForm.applyDeptId)
    return ElMessage.warning('请选择拟收治科室');
  if (!orderForm.diagnosisName)
    return ElMessage.warning('请填写拟诊（住院证的诊断是入院处排床的依据，不能空着）');
  if (!props.patient)
    return;
  orderSubmitting.value = true;
  try {
    const p = props.patient;
    const dept = departments.value.find((d) => String(d.id) === String(orderForm.applyDeptId));
    // 注意：所有 ID 一律**原样以字符串**传给后端，绝不做 Number() 转换。
    // 雪花ID 是 19 位（约 2.1e18），远超 JS 的安全整数 2^53（9.007e15），
    // Number() 会静默改掉末几位 —— 实测挂号 ID ...834 被转成 ...800，
    // 于是住院证上记的是一个**不存在的挂号号**，而且一眼看不出来。
    const res = await createAdmissionOrder({
      registId: p.registId || null,
      registNo: p.registNo || null,
      patientId: p.patientId,
      patientNo: p.patientNo || null,
      patientName: p.patientName,
      gender: p.gender ?? null,
      age: p.age ?? null,
      phone: props.patientDetail?.phone || null,
      idCard: props.patientDetail?.idCard || null,
      sourceDeptId: p.deptId || null,
      sourceDeptName: p.deptName || null,
      sourceDoctorId: p.doctorId || null,
      sourceDoctorName: p.doctorName || null,
      applyDeptId: orderForm.applyDeptId,
      applyDeptName: dept?.deptName || null,
      diagnosisCode: orderForm.diagnosisCode || null,
      diagnosisName: orderForm.diagnosisName,
      diagnosisNote: orderForm.diagnosisNote || null,
      insuranceType: props.patientDetail?.medicalInsuranceType || null,
      medicalInsuranceNo: props.patientDetail?.medicalInsuranceNo || null,
      expectAdmitTime: orderForm.expectAdmitTime || null,
      remark: orderForm.remark || null,
    });
    ElMessage.success(`住院证已开具（${res.data}），患者持证到入院处排床即可`);
    visible.value = false;
  } catch (e) {
    ElMessage.error(e?.message || '开住院证失败');
  } finally {
    orderSubmitting.value = false;
  }
};
</script>
