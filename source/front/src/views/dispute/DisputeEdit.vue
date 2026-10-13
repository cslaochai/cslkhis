<template>
  <!-- 登记 / 修改 -->
  <el-dialog v-model="visible" :close-on-click-modal="true" :title="form.id ? '修改纠纷/投诉' : '登记纠纷/投诉'"
             width="760px">
    <el-form label-width="110px" size="small">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="类型" required>
            <el-select v-model="form.caseType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dicts.caseType" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="来源" required>
            <el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dicts.source" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="等级">
            <el-select v-model="form.level" style="width: 100%">
              <el-option v-for="d in dicts.level" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="被投诉科室">
            <el-select v-model="form.deptId" clearable filterable placeholder="请选择" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="患者">
            <PatientSelect :model-value="form.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                           @select="onPatientSelect"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="涉及人员">
            <el-input v-model="form.involvedStaff" placeholder="多人用逗号分隔"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="投诉人">
            <el-input v-model="form.complainant" placeholder="姓名"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="与患者关系">
            <el-select v-model="form.complainantRel" clearable placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dicts.relation" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="联系电话">
            <el-input v-model="form.complainantTel" placeholder="出参脱敏展示"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="发生时间">
            <el-date-picker v-model="form.occurTime" placeholder="请选择" style="width: 100%"
                            type="datetime" value-format="YYYY-MM-DD HH:mm"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发生地点">
            <el-input v-model="form.occurPlace" placeholder="如：内科三病区"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="内容" required>
        <el-input v-model="form.content" :rows="3" placeholder="投诉/纠纷的具体内容" type="textarea"/>
      </el-form-item>
      <el-form-item label="诉求">
        <el-input v-model="form.demand" :rows="2" placeholder="投诉人诉求" type="textarea"/>
      </el-form-item>
      <el-form-item label="需封存病历">
        <el-radio-group v-model="form.needSeal">
          <el-radio :value="0">否</el-radio>
          <el-radio :value="1">是（受理时联动封存已归档病案）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="save">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 登记与修改共用一张表单（后端同一个 disputeUpsert，row 为空走登记）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {disputeUpsert, getDisputeDetail} from '@/api/dispute';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示登记
  row: {type: Object, default: null},
  dicts: {type: Object, default: () => ({caseType: [], source: [], level: [], relation: []})},
  deptOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null,
  caseType: null,
  sourceType: null,
  level: 1,
  patientId: null,
  patientName: '',
  deptId: null,
  involvedStaff: '',
  complainant: '',
  complainantRel: null,
  complainantTel: '',
  occurTime: '',
  occurPlace: '',
  content: '',
  demand: '',
  needSeal: 0,
  remark: '',
});
const resetForm = () => {
  Object.assign(form, {
    id: null, caseType: null, sourceType: null, level: 1, patientId: null, patientName: '',
    deptId: null, involvedStaff: '', complainant: '', complainantRel: null, complainantTel: '',
    occurTime: '', occurPlace: '', content: '', demand: '', needSeal: 0, remark: '',
  });
};

watch(visible, async (open) => {
  if (!open) return;
  resetForm();
  if (!props.row) return;
  try {
    const res = await getDisputeDetail(props.row.id);
    const d = res?.data || props.row;
    Object.assign(form, {
      id: d.id,
      caseType: Number(d.caseType),
      sourceType: Number(d.sourceType),
      level: Number(d.level ?? 1),
      patientId: d.patientId ?? null,
      patientName: d.patientName || '',
      deptId: d.deptId ?? null,
      involvedStaff: d.involvedStaff || '',
      complainant: d.complainant || '',
      complainantRel: d.complainantRel ?? null,
      complainantTel: '',
      occurTime: (d.occurTime || '').slice(0, 16),
      occurPlace: d.occurPlace || '',
      content: d.content || '',
      demand: d.demand || '',
      needSeal: Number(d.needSeal ?? 0),
      remark: d.remark || '',
    });
  } catch (e) {
    console.error(e);
    ElMessage.error('加载纠纷单据失败');
    visible.value = false;
  }
});

const onPatientSelect = (p) => {
  form.patientId = p?.id ?? null;
  form.patientName = p?.name || p?.patientName || '';
};

const save = async () => {
  if (!form.caseType || !form.sourceType) {
    ElMessage.warning('请选择类型与来源');
    return;
  }
  if (!form.content.trim()) {
    ElMessage.warning('请填写投诉/纠纷内容');
    return;
  }
  saving.value = true;
  try {
    const res = await disputeUpsert({
      id: form.id || undefined,
      caseType: form.caseType,
      sourceType: form.sourceType,
      level: form.level,
      patientId: form.patientId || undefined,
      deptId: form.deptId || undefined,
      involvedStaff: form.involvedStaff.trim() || undefined,
      complainant: form.complainant.trim() || undefined,
      complainantRel: form.complainantRel ?? undefined,
      complainantTel: form.complainantTel.trim() || undefined,
      occurTime: form.occurTime ? (form.occurTime.length === 16 ? `${form.occurTime}:00` : form.occurTime) : undefined,
      occurPlace: form.occurPlace.trim() || undefined,
      content: form.content.trim(),
      demand: form.demand.trim() || undefined,
      needSeal: form.needSeal ?? 0,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      const vo = res.data;
      ElMessage.success(form.id ? '修改成功' : `登记成功，单号 ${vo?.caseNo || ''}`);
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
