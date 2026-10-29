<template>
  <el-dialog v-model="visible" destroy-on-close title="出院办理" width="640px">
    <div class="mb-3 rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
      {{ dischargeForm.patientName }} · {{ dischargeForm.admissionNo }} —— 提交后：床位释放、入院状态置已出院、
      病案首页回写住院天数与离院方式，并自动判定 31 日内再入院。
    </div>
    <!-- 出院前置条件是「已结算」：后端硬拦，前端提前说清楚并给出口，
         否则操作者只会拿到一句「请先结算」，不知道结算在哪个页面 -->
    <div v-if="notSettled"
         class="mb-3 flex items-start gap-3 rounded-lg border border-rose-200 bg-rose-50 p-3 text-xs text-rose-700"
         data-testid="discharge-need-settle">
      <div class="flex-1">
        <p class="font-medium">该住院尚未办理出院结算，现在提交出院会被拒绝。</p>
        <p class="mt-0.5">{{ settleHint || '请先到「住院结算」结清费用（欠费可办理欠费结算）再办出院。' }}</p>
        <p v-if="!canSettle" class="mt-0.5">
          当前岗位没有「住院结算」菜单，请通知出入院处或收费窗口办理结算后再办出院。
        </p>
      </div>
      <!-- 跳转按钮只在有结算菜单时出现：病区护士有 ipd:inpatient:edit（后端允许结算），
           但角色菜单里没有住院结算页，路由守卫会把人弹回工作台，等于给一个死链接 -->
      <el-button v-if="canSettle" size="small" type="primary" @click="goSettle">去住院结算</el-button>
    </div>
    <el-form :model="dischargeForm" label-width="110px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="离院方式" required>
          <el-select v-model="dischargeForm.dischargeWay" class="!w-full" @change="onDischargeWayChange">
            <el-option v-for="(label, code) in dischargeWayMap" :key="code" :label="label" :value="Number(code)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="是否死亡">
          <el-radio-group v-model="dischargeForm.deathFlag" @change="onDeathFlagChange">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出院时间">
          <el-date-picker
              v-model="dischargeForm.dischargeTime"
              class="!w-full"
              placeholder="不填取当前时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="出院医生">
          <el-select v-model="dischargeForm.dischargeDoctorId" class="!w-full" clearable placeholder="选择医生">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="出院诊断编码">
          <el-input v-model="dischargeForm.dischargeDiagnosisCode" placeholder="ICD-10"/>
        </el-form-item>
        <el-form-item label="出院诊断">
          <el-input v-model="dischargeForm.dischargeDiagnosis" placeholder="如 肺炎（治愈）"/>
        </el-form-item>
      </div>
      <el-form-item label="出院小结">
        <el-input v-model="dischargeForm.dischargeSummary" :rows="3" placeholder="住院经过、治疗结果、出院带药与随访要求"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
      死亡病例必须同时满足「是否死亡=是」与「离院方式=死亡」，两者不一致会被拒绝 —— 这两个字段是医保审核死亡病例的第一着眼点。
    </div>
    <template #footer>
      <el-button v-perm="'ipd:inpatient:edit'" :disabled="notSettled" :loading="dischargeLoading" type="primary"
                 @click="submitDischarge">
        确认出院
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {useRouter} from 'vue-router';
import {dischargeInpatient} from '@/api/inpatient';
import {getAccountSummary} from '@/api/inpatientSettlement';
import {dischargeWayMap} from '@/lib/inpatient';
import {hasPerm} from '@/lib/perm';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前住院列表行：取 admissionId / patientName / admissionNo / 诊断兜底
  row: {type: Object, default: null},
  // 医生名册由主页面加载一次（入院登记弹框共用），以 prop 传下来
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const router = useRouter();
/** 是否配了「住院结算」菜单：没有就只给文字指引，不给跳不过去的按钮 */
const canSettle = computed(() => hasPerm('ipd:settlement:list'));
const dischargeLoading = ref(false);
/** 结算状态：null=没查到（不提示、不拦），false=未结算（禁止提交并给跳转） */
const settled = ref(null);
const settleHint = ref('');
const notSettled = computed(() => settled.value === false);
const loadSettleState = async (admissionId) => {
  settled.value = null;
  settleHint.value = '';
  if (!admissionId)
    return;
  try {
    const res = await getAccountSummary(admissionId);
    settled.value = res.data?.settled === true;
    settleHint.value = res.data?.hintText || '';
  } catch (e)  /* 状态查询失败不阻断出院：真正的闸门在后端 */{
    console.error('查询结算状态失败', e);
  }
};
const goSettle = () => {
  visible.value = false;
  router.push('/inpatient-settlement');
};
const dischargeForm = reactive({
  admissionId: '',
  dischargeTime: '',
  dischargeDoctorId: null,
  dischargeWay: 1,
  deathFlag: 0,
  dischargeDiagnosis: '',
  dischargeDiagnosisCode: '',
  dischargeSummary: '',
  patientName: '',
  admissionNo: '',
});

watch(visible, (open) => {
  if (!open)
    return;
  const row = props.row || {};
  Object.assign(dischargeForm, {
    admissionId: row.admissionId,
    dischargeTime: '',
    dischargeDoctorId: null,
    dischargeWay: 1,
    deathFlag: 0,
    dischargeDiagnosis: row.mainDiagnosisName || row.diagnosis || '',
    dischargeDiagnosisCode: '',
    dischargeSummary: '',
    patientName: row.patientName || '',
    admissionNo: row.admissionNo,
  });
  loadSettleState(row.admissionId);
});
// 死亡标志与离院方式必须一致：这是病案首页的硬约束，后端会拒，前端先联动起来
const onDischargeWayChange = (v) => {
  if (v === 5)
    dischargeForm.deathFlag = 1;
  else
    dischargeForm.deathFlag = 0;
};
const onDeathFlagChange = (v) => {
  if (v === 1)
    dischargeForm.dischargeWay = 5;
  else if (dischargeForm.dischargeWay === 5)
    dischargeForm.dischargeWay = 1;
};
const submitDischarge = async () => {
  dischargeLoading.value = true;
  try {
    await dischargeInpatient({
      ...dischargeForm,
      dischargeTime: dischargeForm.dischargeTime || undefined,
    });
    ElMessage.success('出院办理成功：床位已释放、病案首页已回写住院天数与离院方式');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    dischargeLoading.value = false;
  }
};
</script>
