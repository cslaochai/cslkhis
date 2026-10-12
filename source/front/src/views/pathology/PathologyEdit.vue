<template>
  <!-- 登记弹窗 -->
  <el-dialog v-model="visible" :title="editForm.id ? '修改病理单' : '登记病理单'" width="640px">
    <el-form label-width="90px">
      <el-form-item label="患者" required>
        <patient-select v-model="editForm.patientId" @select="onPatientSelect"/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="检查类型">
          <el-select v-model="editForm.examType" style="width: 100%">
            <el-option v-for="d in examTypeDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="是否冰冻">
          <el-radio-group v-model="editForm.isFrozen">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标本类型">
          <el-input v-model="editForm.specimenType" placeholder="活检/切除/穿刺等"/>
        </el-form-item>
        <el-form-item label="取材部位">
          <el-input v-model="editForm.specimenPart"/>
        </el-form-item>
      </div>
      <el-form-item label="临床诊断">
        <el-input v-model="editForm.clinicalDiagnosis" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="['medtech:pathology:add', 'medtech:pathology:edit']" :loading="editLoading" type="primary"
                 @click="saveOrder">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 病理单登记：登记与修改走同一个 pathologyUpsert（row 不带 id 走新增），
 * 表单状态与校验都在这里；检查类型字典由页面加载一次传下来，与列表列同一份口径。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {pathologyUpsert} from '@/api/medicaltech';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 字典由页面加载一次传下来，列表列与表单同一份口径
  examTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const editForm = reactive({
  id: null, patientId: null, patientNo: '', patientName: '', gender: null,
  age: null, visitDate: '', clinicalDiagnosis: '', examType: 1, specimenType: '', specimenPart: '', isFrozen: 0,
});
const editLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null,
    visitDate: '', clinicalDiagnosis: '', examType: 1, specimenType: '', specimenPart: '', isFrozen: 0,
  });
});

const onPatientSelect = (p) => {
  editForm.patientId = p?.id;
  editForm.patientName = p?.name || p?.patientName || '';
  editForm.patientNo = p?.patientNo || '';
  editForm.gender = p?.gender ?? null;
  editForm.age = p?.age ?? null;
};
const saveOrder = async () => {
  if (!editForm.patientId || !editForm.patientName.trim()) {
    ElMessage.warning('请选择患者');
    return;
  }
  editLoading.value = true;
  try {
    const res = await pathologyUpsert({
      id: editForm.id || undefined,
      patientId: editForm.patientId, patientNo: editForm.patientNo || undefined,
      patientName: editForm.patientName.trim(), gender: editForm.gender, age: editForm.age,
      visitDate: editForm.visitDate || undefined, clinicalDiagnosis: editForm.clinicalDiagnosis || undefined,
      examType: editForm.examType, specimenType: editForm.specimenType || undefined,
      specimenPart: editForm.specimenPart || undefined, isFrozen: editForm.isFrozen,
    });
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
