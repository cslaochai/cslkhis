<template>
  <!-- 登记弹窗 -->
  <el-dialog v-model="visible" title="体检登记" width="480px">
    <el-form label-width="90px">
      <el-form-item label="体检人">
        <el-radio-group v-model="mode" size="small">
          <el-radio-button value="select">选择已有</el-radio-button>
          <el-radio-button value="new">新建体检人</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="mode === 'select'" label=" " required>
        <PatientSelect v-model="createForm.patientId"/>
      </el-form-item>
      <template v-else>
        <el-form-item label="姓名" required>
          <el-input v-model="newForm.patientName" placeholder="请输入姓名"/>
        </el-form-item>
        <el-form-item label="性别" required>
          <el-select v-model="newForm.gender" class="w-full" placeholder="请选择（不确定选未知）">
            <el-option v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :label="g.label" :value="g.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="身份证" required>
          <el-input v-model="newForm.idCard" placeholder="请输入身份证号" @blur="syncBirthDate"/>
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="newForm.phone" placeholder="请输入 11 位手机号"/>
        </el-form-item>
      </template>
      <el-form-item label="对象类型">
        <el-radio-group v-model="createForm.personType">
          <el-radio :value="1">个人</el-radio>
          <el-radio :value="2">团体</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="套餐" required>
        <el-select v-model="createForm.packageId" :fit-input-width="false" data-testid="checkup-package-select"
                   placeholder="选择套餐" style="width: 100%">
          <el-option v-for="p in packages" :key="p.id" :label="`${p.packageName}（¥${p.price}）`"
                     :value="String(p.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="体检日期" required>
        <el-date-picker v-model="createForm.checkupDate" style="width: 100%" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="createForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'checkup:record:add'" data-testid="checkup-create-submit" type="primary"
                 @click="submitCreate">登记
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 体检登记弹窗（仅新增：1 已登记为初始态，无修改语义，故为 xxxAdd 形态）
 * 套餐下拉只有本表单使用，弹框打开时懒加载。
 * 体检人两种来源：选择已有主档 / 现场新建体检人（先建档回填主档 ID，再建登记）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {checkupPackageListPage, checkupRecordCreate} from '@/api/checkup';
import {createPatient} from '@/api/patient';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {PATIENT_GENDER_OPTIONS, isPatientGenderCollected, isPhoneLegal, birthDateFromIdCard} from '@/lib/patientGender';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const mode = ref('select');
const createForm = reactive({patientId: null, personType: 1, packageId: null, checkupDate: '', remark: ''});
const newForm = reactive({patientName: '', gender: null, idCard: '', phone: '', birthDate: ''});
const packages = ref([]);

const loadPackages = async () => {
  try {
    const res = await checkupPackageListPage({status: 1, pageNum: 1, pageSize: 100});
    packages.value = res?.data?.records || [];
  } catch (e) {
    console.error('加载套餐失败', e);
  }
};

const resetForm = () => {
  mode.value = 'select';
  createForm.patientId = null;
  createForm.personType = 1;
  createForm.packageId = null;
  createForm.checkupDate = '';
  createForm.remark = '';
  Object.assign(newForm, {patientName: '', gender: null, idCard: '', phone: '', birthDate: ''});
};

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return;
    resetForm();
    loadPackages();
  }
);

const syncBirthDate = () => {
  if (!newForm.birthDate) newForm.birthDate = birthDateFromIdCard(newForm.idCard);
};

const submitCreate = async () => {
  let patientId = createForm.patientId;
  if (mode.value === 'new') {
    if (!newForm.patientName.trim()) return ElMessage.warning('请输入姓名');
    if (!isPatientGenderCollected(newForm.gender)) return ElMessage.warning('请选择性别（不确定选未知）');
    if (!newForm.idCard.trim()) return ElMessage.warning('请输入身份证号');
    if (!isPhoneLegal(newForm.phone)) return ElMessage.warning('请输入 11 位手机号');
    try {
      const res = await createPatient({
        patientName: newForm.patientName.trim(),
        gender: newForm.gender,
        idCard: newForm.idCard.trim(),
        phone: newForm.phone.trim(),
        birthDate: newForm.birthDate,
        medicalInsuranceType: '自费',
      });
      patientId = res?.data?.id;
      if (!patientId) return ElMessage.warning('建档失败，请重试');
    } catch (e) {
      console.error('现场建档失败', e);
      return;
    }
  }
  if (!patientId) return ElMessage.warning('请选择体检人');
  if (!createForm.packageId) return ElMessage.warning('请选择套餐');
  if (!createForm.checkupDate) return ElMessage.warning('请选择体检日期');
  try {
    await checkupRecordCreate({
      patientId, personType: createForm.personType,
      packageId: createForm.packageId, checkupDate: createForm.checkupDate, remark: createForm.remark,
    });
    ElMessage.success('登记成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('登记失败', e);
  }
};
</script>
