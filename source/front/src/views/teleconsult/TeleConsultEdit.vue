<template>
  <el-dialog v-model="visible" :title="isEdit ? '修改会诊申请' : '申请远程会诊'" width="720px">
    <el-form label-width="110px" size="small">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="患者" required>
            <PatientSelect :model-value="teleForm.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                           @select="onTelePatientSelect"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="申请科室">
            <el-select v-model="teleForm.applyDeptId" clearable filterable placeholder="请选择" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="会诊类型" required>
            <el-select v-model="teleForm.consultType" style="width: 100%">
              <el-option v-for="d in teleTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否急会诊">
            <el-radio-group v-model="teleForm.isUrgent">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="专家医院">
            <el-input v-model="teleForm.expertHospital" placeholder="院内会诊填本院"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="专家科室">
            <el-input v-model="teleForm.expertDept"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="专家姓名">
            <el-input v-model="teleForm.expertName"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="专家职称">
            <el-input v-model="teleForm.expertTitle" placeholder="如：主任医师"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="会诊目的" required>
        <el-input v-model="teleForm.purpose" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="病情摘要">
        <el-input v-model="teleForm.diagnosis" :rows="2" placeholder="申请方诊断 / 病情摘要" type="textarea"/>
      </el-form-item>
      <el-form-item label="会诊费用">
        <el-input-number v-model="teleForm.fee" :min="0" :precision="2"/>
        <span class="hint">价目快照，此处不计费</span>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="teleForm.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="teleSaving" type="primary" @click="saveTele">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 远程会诊申请 / 修改（后端同一个 teleConsultUpsert：row 为空走新增、有值走修改）
 *
 * 口径：所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTeleConsultDetail, teleConsultUpsert} from '@/api/teleconsult';
import {getDepartmentSelectList} from '@/api/system';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // row 为空 = 申请，有值 = 修改
  row: {type: Object, default: null},
  // 会诊类型字典由页面加载一次传下来（查询条件与列表列同一份）
  teleTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const isEdit = computed(() => !!props.row?.id);
const teleSaving = ref(false);
const deptOptions = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
const teleForm = reactive({
  id: null,
  patientId: null,
  patientName: '',
  applyDeptId: null,
  consultType: 1,
  isUrgent: 0,
  expertHospital: '',
  expertDept: '',
  expertName: '',
  expertTitle: '',
  purpose: '',
  diagnosis: '',
  fee: null,
  remark: '',
});

watch(visible, async (open) => {
  if (!open) return;
  if (!deptOptions.value.length) await loadDepts();
  if (props.row?.id) {
    const res = await getTeleConsultDetail(props.row.id);
    const d = res?.data || props.row;
    Object.assign(teleForm, {
      id: d.id, patientId: d.patientId ?? null, patientName: d.patientName || '',
      applyDeptId: d.applyDeptId ?? null, consultType: Number(d.consultType ?? 1),
      isUrgent: Number(d.isUrgent ?? 0), expertHospital: d.expertHospital || '',
      expertDept: d.expertDept || '', expertName: d.expertName || '', expertTitle: d.expertTitle || '',
      purpose: d.purpose || '', diagnosis: d.diagnosis || '', fee: d.fee ?? null, remark: d.remark || '',
    });
    return;
  }
  Object.assign(teleForm, {
    id: null, patientId: null, patientName: '', applyDeptId: null, consultType: 1, isUrgent: 0,
    expertHospital: '', expertDept: '', expertName: '', expertTitle: '', purpose: '',
    diagnosis: '', fee: null, remark: '',
  });
});

const onTelePatientSelect = (p) => {
  teleForm.patientId = p?.id ?? null;
  teleForm.patientName = p?.name || p?.patientName || '';
};
const saveTele = async () => {
  if (!teleForm.patientId) {
    ElMessage.warning('请选择患者');
    return;
  }
  if (!teleForm.purpose.trim()) {
    ElMessage.warning('请填写会诊目的');
    return;
  }
  teleSaving.value = true;
  try {
    const res = await teleConsultUpsert({
      id: teleForm.id || undefined,
      patientId: teleForm.patientId,
      applyDeptId: teleForm.applyDeptId || undefined,
      consultType: teleForm.consultType,
      isUrgent: teleForm.isUrgent,
      expertHospital: teleForm.expertHospital.trim() || undefined,
      expertDept: teleForm.expertDept.trim() || undefined,
      expertName: teleForm.expertName.trim() || undefined,
      expertTitle: teleForm.expertTitle.trim() || undefined,
      purpose: teleForm.purpose.trim(),
      diagnosis: teleForm.diagnosis.trim() || undefined,
      fee: teleForm.fee ?? undefined,
      remark: teleForm.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(teleForm.id ? '修改成功' : `会诊申请已提交，单号 ${res.data?.consultNo || ''}`);
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    teleSaving.value = false;
  }
};
</script>

<style scoped>
.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
