<template>
  <el-dialog v-model="visible" title="转诊登记" width="580px">
    <el-form label-width="90px">
      <el-form-item label="患者" required>
        <PatientSelect v-model="form.patientId"/>
      </el-form-item>
      <el-form-item label="方向" required>
        <el-radio-group v-model="form.direction">
          <el-radio v-for="d in dirDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="入院ID">
        <el-input v-model="form.admissionId" placeholder="住院患者转诊时填（可空=门诊）"/>
      </el-form-item>
      <el-form-item label="转出科室" required>
        <el-select v-model="form.fromDeptId" :fit-input-width="false" filterable style="width: 240px">
          <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="转入医院">
        <el-input v-model="form.toHospital" placeholder="院际转诊必填（上转上级/下转基层）"/>
      </el-form-item>
      <el-form-item label="转入科室">
        <el-select v-model="form.toDeptId" :fit-input-width="false" clearable filterable style="width: 240px">
          <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="转诊原因" required>
        <el-input v-model="form.reason" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="诊断摘要">
        <el-input v-model="form.diagnosis"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.contactPhone" style="width: 220px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'inpatient:referral:add'" :loading="saving" type="primary" @click="submitCreate">
        登记
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 转诊登记弹框（自 ReferralView 拆出）
 *
 * 上转=转往上级医院、下转=转回基层/社区（院际转诊 toHospital 必填；本院内转科室走「转科」，别混）。
 * 方向字典与科室列表由页面统一加载后传入（列表列渲染同一份数据，见拆分规范第 6 条）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {referralCreate} from '@/api/referral';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 转诊方向字典（页面已加载）
  dirDict: {type: Array, default: () => []},
  // 科室下拉数据（页面已加载）
  depts: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  patientId: null, direction: 1, admissionId: '',
  fromDeptId: null, toDeptId: null,
  toHospital: '', reason: '', diagnosis: '', contactPhone: '',
});

// 每次打开重置为默认（方向默认上转=1，与拆分前口径一致）
watch(() => props.modelValue, (v) => {
  if (!v) return;
  Object.assign(form, {
    patientId: null, direction: 1, admissionId: '', fromDeptId: null, toDeptId: null,
    toHospital: '', reason: '', diagnosis: '', contactPhone: '',
  });
});

const submitCreate = async () => {
  if (!form.patientId || !form.fromDeptId || !form.reason.trim()) {
    ElMessage.warning('患者、转出科室、转诊原因为必填');
    return;
  }
  if (!form.toHospital.trim() && !form.toDeptId) {
    ElMessage.warning('院际转诊必须填转入医院，院内转诊必须选转入科室');
    return;
  }
  saving.value = true;
  try {
    const res = await referralCreate({
      patientId: form.patientId,
      direction: form.direction,
      admissionId: form.admissionId || undefined,
      fromDeptId: form.fromDeptId,
      toDeptId: form.toDeptId ?? undefined,
      toHospital: form.toHospital.trim() || undefined,
      reason: form.reason.trim(),
      diagnosis: form.diagnosis || undefined,
      contactPhone: form.contactPhone || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('转诊单已登记');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  } finally {
    saving.value = false;
  }
};
</script>
