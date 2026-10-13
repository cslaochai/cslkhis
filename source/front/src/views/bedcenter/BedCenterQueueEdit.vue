<template>
  <!-- ==================== 登记 / 改需求 ==================== -->
  <el-dialog v-model="visible" :title="form.id ? '修改排队需求' : '登记床位排队'" width="620px">
    <el-form label-width="110px" size="small">
      <el-form-item v-if="!form.admissionOrderId" label="患者" required>
        <PatientSelect v-model="form.patientId" :disabled="!!form.id" data-testid="bc-form-patient"
                       @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item v-else label="患者">
        <span class="text-sm text-slate-700">
          {{ form.patientName }}（来源住院证 {{ form.admissionOrderId }}）
        </span>
      </el-form-item>
      <el-form-item label="拟收治科室">
        <el-select v-model="form.applyDeptId" :fit-input-width="false" class="!w-full" clearable data-testid="bc-form-dept"
                   filterable placeholder="选择科室">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="期望病区">
        <el-select v-model="form.expectWardId" :fit-input-width="false" class="!w-full" clearable
                   data-testid="bc-form-ward" placeholder="不指定=服从调配">
          <el-option v-for="w in wardOptions" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="床型 / 优先级">
        <div class="flex gap-2">
          <el-select v-model="form.bedType" class="!w-32" data-testid="bc-form-bedtype">
            <el-option label="普通" value="normal"/>
            <el-option label="重症 ICU" value="ICU"/>
            <el-option label="特需 VIP" value="VIP"/>
          </el-select>
          <el-select v-model="form.priority" class="!w-32" data-testid="bc-form-priority">
            <el-option :value="1" label="普通"/>
            <el-option :value="2" label="急"/>
            <el-option :value="3" label="危重"/>
          </el-select>
          <el-select v-model="form.genderLimit" class="!w-32" data-testid="bc-form-gender">
            <el-option :value="0" label="不限性别"/>
            <el-option :value="1" label="限男床"/>
            <el-option :value="2" label="限女床"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="隔离需求">
        <el-checkbox v-model="isolation" data-testid="bc-form-isolation">需要隔离床（不得混住）</el-checkbox>
      </el-form-item>
      <el-form-item label="预计入院日期">
        <el-date-picker v-model="form.expectAdmitDate" data-testid="bc-form-expect" placeholder="选择预计入院日期"
                        type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item v-if="form.id" label="联系电话">
        <el-input v-model="form.phone" class="!w-40" data-testid="bc-form-phone"/>
      </el-form-item>
      <el-form-item label="拟诊名称">
        <el-input v-model="form.diagnosisName" data-testid="bc-form-diag" placeholder="拟诊（快照到排队单上）"/>
      </el-form-item>
    </el-form>
    <p class="mb-2 text-xs text-slate-400">
      保存后进入队列并按「优先级 → 登记时间」排序；门急诊所开的住院证会由系统自动入队，不需要在这里重复登记。
    </p>
    <template #footer>
      <el-button :loading="submitting" data-testid="bc-form-submit" type="primary" @click="submitForm">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 床位排队登记 / 修改需求（新增+编辑合并：row 为空即登记，带 id 即改需求）
 * 表单态与提交都在本组件；父页面只传 v-model + row + 科室/病区候选。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {upsertBedWait} from '@/api/bedCenter';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑目标行（null = 登记新排队）
  row: {type: Object, default: null},
  // 科室/病区候选：列表筛选也在用，由父页面加载一次传入
  deptOptions: {type: Array, default: () => []},
  wardOptions: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const isolation = ref(false);
const form = reactive({
  id: null, patientId: null, patientName: '', patientNo: '', gender: null, age: null, phone: '',
  admissionOrderId: null, applyDeptId: null, applyDeptName: '', expectWardId: null, bedType: 'normal',
  priority: 1, genderLimit: 0, isolationFlag: 0, expectAdmitDate: null, diagnosisName: '', remark: '',
});
const resetForm = () => {
  Object.assign(form, {
    id: null, patientId: null, patientName: '', patientNo: '', gender: null, age: null, phone: '',
    admissionOrderId: null, applyDeptId: null, applyDeptName: '', expectWardId: null, bedType: 'normal',
    priority: 1, genderLimit: 0, isolationFlag: 0, expectAdmitDate: null, diagnosisName: '', remark: '',
  });
  isolation.value = false;
};
watch(() => props.modelValue, (v) => {
  if (!v)
    return;
  resetForm();
  const row = props.row;
  if (row) {
    Object.assign(form, {
      id: row.id, patientId: row.patientId, patientName: row.patientName, gender: row.gender, age: row.age,
      phone: row.phone, admissionOrderId: row.admissionOrderId, applyDeptId: row.applyDeptId,
      expectWardId: row.expectWardId, bedType: row.bedType, priority: row.priority,
      genderLimit: row.genderLimit ?? 0, diagnosisName: row.diagnosisName,
    });
    isolation.value = Number(row.isolationFlag) === 1;
  }
});
const onPatientSelect = (p) => {
  if (!p)
    return;
  form.patientName = p.patientName || '';
  form.patientNo = p.patientNo || '';
  form.gender = p.gender ?? null;
  form.age = p.age ?? null;
  form.phone = p.phone || '';
};
const submitForm = async () => {
  if (!form.id && !form.patientId)
    return ElMessage.warning('请选择患者');
  if (!form.id && !form.patientName)
    return ElMessage.warning('未取到患者姓名，请重新选择患者');
  submitting.value = true;
  try {
    const payload = {
      ...form,
      patientId: form.patientId ? String(form.patientId) : null,
      isolationFlag: isolation.value ? 1 : 0,
    };
    const res = await upsertBedWait(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '排队登记成功');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
</script>
