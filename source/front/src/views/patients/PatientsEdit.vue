<template>
  <el-dialog v-model="visible" destroy-on-close :title="isAdd ? '新增患者' : '编辑患者'" width="800px">
    <el-form :model="form" label-position="top">
      <h4 class="mb-2 text-sm font-medium text-slate-700">基本信息</h4>
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="姓名" required>
          <el-input v-model="form.patientName" placeholder="请输入患者姓名"/>
        </el-form-item>
        <el-form-item label="性别" required>
          <el-select v-model="form.gender" class="w-full" placeholder="请选择（不确定选未知）">
            <el-option v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :label="g.label" :value="g.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthDate" class="w-full" placeholder="选择日期" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="身份证号" required>
          <el-input v-model="form.idCard" placeholder="请输入身份证号" @blur="syncBirthDateFromIdCard"/>
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.phone" placeholder="请输入 11 位手机号"/>
        </el-form-item>
        <el-form-item label="患者类型">
          <el-select v-model="form.patientType" class="w-full">
            <el-option v-for="o in PATIENT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
      </div>

      <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">联系人信息</h4>
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="联系人姓名">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名"/>
        </el-form-item>
        <el-form-item label="联系人电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系人电话"/>
        </el-form-item>
        <el-form-item label="与患者关系">
          <!-- 绑 dictLabel 而不是 dictValue：这一列存量存的是中文关系名，写码值进去就是第二种事实 -->
          <el-select v-model="form.contactRelation" class="w-full" default-first-option
                     filterable placeholder="请选择关系">
            <el-option v-for="item in patientRelationList" :key="item.dictValue" :label="item.dictLabel"
                       :value="item.dictLabel"/>
          </el-select>
        </el-form-item>
      </div>

      <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">其他信息</h4>
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="血型">
          <el-select v-model="form.bloodType" class="w-full" clearable placeholder="选择血型">
            <el-option label="A型" value="A"/>
            <el-option label="B型" value="B"/>
            <el-option label="AB型" value="AB"/>
            <el-option label="O型" value="O"/>
          </el-select>
        </el-form-item>
        <el-form-item label="民族">
          <!-- allow-create 保留：字典里没有的民族要能录进去，所以这一列同时吃 dictValue 和自由文本 -->
          <el-select v-model="form.nation" allow-create class="w-full" default-first-option filterable
                     placeholder="请选择民族">
            <el-option v-for="item in nationalityList" :key="item.dictValue" :label="item.dictLabel"
                       :value="item.dictValue"/>
          </el-select>
        </el-form-item>
        <el-form-item label="职业">
          <el-input v-model="form.occupation" placeholder="请输入职业"/>
        </el-form-item>
        <el-form-item label="婚姻状况">
          <el-select v-model="form.maritalStatus" class="w-full" clearable placeholder="请选择">
            <el-option :value="0" label="未婚"/>
            <el-option :value="1" label="已婚"/>
            <el-option :value="2" label="离异"/>
            <el-option :value="3" label="丧偶"/>
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="家庭住址">
        <el-input v-model="form.address" placeholder="请输入家庭住址"/>
      </el-form-item>

      <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">医保信息</h4>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="医保类型">
          <el-select v-model="form.medicalInsuranceType" class="w-full" clearable filterable
                     placeholder="请选择医保类型">
            <el-option v-for="item in medicalInsuranceTypes" :key="item.dictValue" :label="item.dictLabel"
                       :value="item.dictValue"/>
          </el-select>
        </el-form-item>
        <el-form-item label="医保卡号">
          <el-input v-model="form.medicalInsuranceNo" placeholder="请输入医保卡号"/>
        </el-form-item>
      </div>

      <el-form-item label="过敏史">
        <el-input v-model="form.allergyHistory" :rows="3"
                  placeholder="该患者已有结构化过敏记录时，此处显示的是明细摘要、保存后会被明细覆盖；按条目维护请到「患者中心 → 健康档案」"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="isAdd ? 'patient:add' : 'patient:edit'" type="primary" @click="handleSubmit">
        {{ isAdd ? '确认添加' : '确认修改' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createPatient, getPatientDetail, updatePatient} from '@/api/patient';
import {getDictDataList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {
  birthDateFromIdCard,
  isIdCardBirthDateLegal,
  isIdCardChecksumLegal,
  isIdCardFormatLegal,
  isPatientGenderCollected,
  isPhoneLegal,
  PATIENT_GENDER_OPTIONS
} from '@/lib/patientGender';
import {PATIENT_TYPE_OPTIONS} from '@/lib/patientType';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 为空走新增，有值走修改（后端新增/修改是同一个 xxxUpsert 口径，前端也只留一套字段）
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const isAdd = computed(() => !props.row?.id);

function emptyForm() {
  return {
    patientName: '',
    // 不预设性别：默认成"男"等于静默编造性别，改为必须显式选择（含「未知」）
    gender: null,
    birthDate: '',
    idCard: '',
    phone: '',
    patientType: 1,
    contactName: '',
    contactPhone: '',
    contactRelation: '',
    bloodType: '',
    nation: '',
    occupation: '',
    maritalStatus: null,
    address: '',
    medicalInsuranceType: '',
    medicalInsuranceNo: '',
    allergyHistory: '',
  };
}

const form = ref(emptyForm());

watch(() => props.modelValue, (v) => {
  if (!v) return;
  if (!props.row?.id) {
    form.value = emptyForm();
    return;
  }
  form.value = {...emptyForm(), ...props.row};
  loadFull();
});

/**
 * 列表出参的身份证/电话/医保卡号是脱敏后置空的（展示接口一律遮码），
 * 直接拿行数据回填会让用户以为患者没填，且整对象回写时把星号洗进库（不可逆数据损坏）。
 * 所以编辑必须单独取一次编辑回显接口，那里保留明文。
 */
const loadFull = async () => {
  try {
    const res = await getPatientDetail(props.row.id);
    if (res.data) {
      form.value = {...emptyForm(), ...props.row, ...res.data};
    }
  } catch (error) {
    console.error('加载患者全量信息失败（编辑表单按列表行数据展示）:', error);
  }
};

/**
 * 身份证填完 → 把出生日期带出来。录入时少填一格，也避免 birth_date 空着：
 * age 是后端拿 birth_date 算的，空着列表就显示「—」；EMPI 的「同名+同性别+同出生日期」
 * 那一档匹配也依赖它。已经手动填过出生日期就不覆盖。
 */
const syncBirthDateFromIdCard = () => {
  if (form.value.birthDate) return;
  const d = birthDateFromIdCard(form.value.idCard);
  if (d) form.value.birthDate = d;
};

const validate = () => {
  const f = form.value;
  if (!f.patientName) return '请输入患者姓名';
  if (!isPatientGenderCollected(f.gender)) return '请选择性别（确实没问到请选「未知」；历史脏值 0 请顺手修正）';
  if (!f.idCard) return '请输入身份证号';
  if (!isIdCardFormatLegal(f.idCard)) return '身份证号格式不正确：应为 18 位（末位可为 X）';
  if (isAdd.value) {
    // 出生日期单独判：校验位对了不代表日期存在（19990230 这种），提示要指到点子上
    if (!isIdCardBirthDateLegal(f.idCard)) return '身份证号中的出生日期不存在，请核对';
    if (!isIdCardChecksumLegal(f.idCard)) return '身份证号校验位不正确，请核对';
  }
  // 联系电话必填：预约提醒、报告推送、随访都要能打到人
  if (!f.phone) return '请输入联系电话';
  if (!isPhoneLegal(f.phone)) return '手机号格式不正确：应为 11 位手机号';
  return '';
};

const submitting = ref(false);
const handleSubmit = async () => {
  const err = validate();
  if (err) {
    ElMessage.warning(err);
    return;
  }
  if (submitting.value) return;
  submitting.value = true;
  try {
    if (isAdd.value) {
      await createPatient(form.value);
      ElMessage.success('新增成功');
    } else {
      await updatePatient(form.value);
      ElMessage.success('修改成功');
    }
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || (isAdd.value ? '新增失败' : '修改失败'));
  } finally {
    submitting.value = false;
  }
};

const medicalInsuranceTypes = ref([]);
const nationalityList = ref([]);
const patientRelationList = ref([]);

onMounted(async () => {
  const [insurance, nationality, relation] = await Promise.all([
    getDictDataList(DICT_TYPE.MEDICAL_INSURANCE_TYPE).catch(() => null),
    getDictDataList(DICT_TYPE.SYS_NATIONALITY).catch(() => null),
    getDictDataList(DICT_TYPE.SYS_PATIENT_RELATION).catch(() => null),
  ]);
  medicalInsuranceTypes.value = insurance?.data || [];
  nationalityList.value = nationality?.data || [];
  patientRelationList.value = relation?.data || [];
});
</script>
