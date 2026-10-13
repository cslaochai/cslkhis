<template>
  <!-- 登记弹窗 -->
  <el-dialog v-model="visible" :title="editForm.id ? '修改检查' : '登记内镜检查'" width="640px">
    <el-form label-width="100px">
      <el-form-item label="患者" required>
        <patient-select v-model="editForm.patientId" @select="onPatientSelect"/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="内镜类型">
          <el-select v-model="editForm.endoType" style="width: 100%">
            <el-option v-for="d in endoTypeDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="麻醉方式">
          <el-select v-model="editForm.anesthesiaMethod" style="width: 100%">
            <el-option v-for="d in anesthesiaDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="部位/范围">
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
      <el-button v-perm="['medtech:endoscopy:add', 'medtech:endoscopy:edit']" :loading="editLoading" type="primary"
                 @click="saveRecord">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 内镜检查登记：登记与修改走同一个 endoscopyUpsert，表单状态与校验都在这里
 * （后端是同一个主档入口，两侧字段必须同一份）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {endoscopyUpsert} from '@/api/medicaltech';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 字典由页面加载一次传下来，列表列与表单同一份口径
  endoTypeDict: {type: Array, default: () => []},
  anesthesiaDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const editLoading = ref(false);
const editForm = reactive({
  id: null, patientId: null, patientNo: '', patientName: '', gender: null,
  age: null, visitDate: '', clinicalDiagnosis: '', endoType: 1, anesthesiaMethod: 1,
  bodyPart: '', examPurpose: '',
});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null, visitDate: '',
    clinicalDiagnosis: '', endoType: 1, anesthesiaMethod: 1, bodyPart: '', examPurpose: '',
  });
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
    const res = await endoscopyUpsert({...editForm, visitDate: editForm.visitDate || undefined});
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
