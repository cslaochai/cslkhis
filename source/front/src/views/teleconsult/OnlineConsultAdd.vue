<template>
  <el-dialog v-model="visible" title="发起线上问诊" width="620px">
    <el-form label-width="100px" size="small">
      <el-form-item label="患者" required>
        <PatientSelect :model-value="form.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                       @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="接诊科室">
        <el-select v-model="form.deptId" clearable filterable placeholder="请选择" style="width: 100%">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="问诊方式" required>
        <el-radio-group v-model="form.consultType">
          <el-radio v-for="d in onlineTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">{{
              d.dictLabel
            }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="主诉/问题" required>
        <el-input v-model="form.chiefComplaint" :rows="4" placeholder="患者描述的主要问题" type="textarea"/>
      </el-form-item>
      <el-form-item label="问诊费用">
        <el-input-number v-model="form.fee" :min="0" :precision="2"/>
        <span class="hint">价目快照，此处不计费</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" type="primary" @click="save">发起</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 发起线上问诊（只有新增语义：待接诊 → 接诊 → 回复并结束/退诊，没有修改入口，故为 Add）
 *
 * 费用 fee 只是价目快照，这里不计费 —— 计费走 charge 域，避免双计。
 * 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {applyOnlineConsult} from '@/api/teleconsult';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 接诊科室名册由页面加载一次传下来（查询条件同一份）
  deptOptions: {type: Array, default: () => []},
  // 问诊方式字典由页面加载一次传下来（查询条件与列表列同一份）
  onlineTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  patientId: null,
  patientName: '',
  deptId: null,
  consultType: 1,
  chiefComplaint: '',
  fee: null,
});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(form, {
    patientId: null, patientName: '', deptId: null, consultType: 1, chiefComplaint: '', fee: null,
  });
});

const onPatientSelect = (p) => {
  form.patientId = p?.id ?? null;
  form.patientName = p?.name || p?.patientName || '';
};
const save = async () => {
  if (!form.patientId) {
    ElMessage.warning('请选择患者');
    return;
  }
  if (!form.chiefComplaint.trim()) {
    ElMessage.warning('请填写主诉/问题描述');
    return;
  }
  saving.value = true;
  try {
    const res = await applyOnlineConsult({
      patientId: form.patientId,
      deptId: form.deptId || undefined,
      consultType: form.consultType,
      chiefComplaint: form.chiefComplaint.trim(),
      fee: form.fee ?? undefined,
    });
    if (res.code === 200) {
      ElMessage.success(`问诊已发起，单号 ${res.data?.consultNo || ''}`);
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '发起失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('发起失败');
  } finally {
    saving.value = false;
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
