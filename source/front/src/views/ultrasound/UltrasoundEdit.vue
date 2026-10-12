<template>
  <!-- 登记弹窗 -->
  <el-dialog v-model="visible" :title="editForm.id ? '修改检查' : '登记超声检查'" width="640px">
    <el-form label-width="90px">
      <el-form-item label="患者" required>
        <patient-select v-model="editForm.patientId" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="超声类型">
        <el-select v-model="editForm.usType" style="width: 100%">
          <el-option v-for="d in usTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="检查部位">
        <el-input v-model="editForm.bodyPart"/>
      </el-form-item>
      <el-form-item label="检查目的">
        <el-input v-model="editForm.examPurpose"/>
      </el-form-item>
      <el-form-item label="临床诊断">
        <el-input v-model="editForm.clinicalDiagnosis" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="['medtech:ultrasound:add', 'medtech:ultrasound:edit']" :loading="editLoading" type="primary"
                 @click="saveRecord">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 登记 / 修改超声检查（后端是同一个 recordUpsert：row 为空走登记，有值走修改）
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {ultrasoundUpsert} from '@/api/medicaltech';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 超声类型字典由页面加载一次传下来（查询卡与列表列用的是同一份，禁止再拉一遍）
  usTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const editLoading = ref(false);
const editForm = reactive({
  id: null, patientId: null, patientNo: '', patientName: '', gender: null,
  age: null, visitDate: '', clinicalDiagnosis: '', usType: 1, bodyPart: '', examPurpose: '',
});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null, visitDate: '',
    clinicalDiagnosis: '', usType: 1, bodyPart: '', examPurpose: '',
  });
  // row 有值走修改：只取表单认识的字段，避免把列表列混进 upsert 入参
  if (props.row) {
    const {
      id, patientId, patientNo, patientName, gender, age, visitDate,
      clinicalDiagnosis, usType, bodyPart, examPurpose,
    } = props.row;
    Object.assign(editForm, {
      id: id ?? null, patientId: patientId ?? null, patientNo: patientNo ?? '', patientName: patientName ?? '',
      gender: gender ?? null, age: age ?? null, visitDate: visitDate ?? '',
      clinicalDiagnosis: clinicalDiagnosis ?? '', usType: usType ?? 1,
      bodyPart: bodyPart ?? '', examPurpose: examPurpose ?? '',
    });
  }
});

const onPatientSelect = (p) => {
  editForm.patientId = p?.id;
  editForm.patientName = p?.name || p?.patientName || '';
  editForm.patientNo = p?.patientNo || '';
  editForm.gender = p?.gender ?? null;
  editForm.age = p?.age ?? null;
};
const saveRecord = async () => {
  if (!editForm.patientId || !editForm.patientName.trim()) {
    ElMessage.warning('请选择患者');
    return;
  }
  editLoading.value = true;
  try {
    const res = await ultrasoundUpsert({...editForm, visitDate: editForm.visitDate || undefined});
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    editLoading.value = false;
  }
};
</script>
