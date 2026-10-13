<template>
  <el-dialog v-model="visible" :title="form.id ? '修改准入术式' : '新增准入术式'" width="620px">
    <el-form label-width="120px" size="small">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="术式编码" required>
            <el-input v-model="form.itemCode"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最长滞留(h)" required>
            <el-input-number v-model="form.maxStayHours" :max="168" :min="1"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="手术分级" required>
        <el-select v-model="form.operationLevel" :fit-input-width="false" style="width: 160px">
          <el-option v-for="(label, code) in operationLevelMap" :key="code" :label="label" :value="Number(code)"/>
        </el-select>
        <span class="ml-2 text-xs text-slate-400">决定预约时校验的术者授权级别（日间手术要求术者持有不低于此级）</span>
      </el-form-item>
      <el-form-item label="术式名称" required>
        <el-input v-model="form.itemName"/>
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="适用科室">
            <el-select v-model="form.deptId" clearable filterable placeholder="请选择" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="麻醉方式">
            <el-select v-model="form.anesthesiaType" clearable placeholder="请选择" style="width: 100%">
              <el-option v-for="d in anesDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="标准费用(元)">
            <el-input-number v-model="form.standardFee" :min="0" :precision="2"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
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
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {daySurgeryItemUpsert} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  deptOptions: {type: Array, default: () => []},
  anesDict: {type: Array, default: () => []},
  operationLevelMap: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null, itemCode: '', itemName: '', deptId: null,
  maxStayHours: 48, operationLevel: 2, anesthesiaType: null,
  standardFee: null, status: 1, remark: '',
});
watch(visible, (open) => {
  if (!open) return;
  if (props.row) {
    Object.assign(form, {
      id: props.row.id, itemCode: props.row.itemCode, itemName: props.row.itemName, deptId: props.row.deptId ?? null,
      maxStayHours: Number(props.row.maxStayHours ?? 48), operationLevel: Number(props.row.operationLevel ?? 2),
      anesthesiaType: props.row.anesthesiaType ?? null,
      standardFee: props.row.standardFee ?? null, status: Number(props.row.status ?? 1), remark: props.row.remark || '',
    });
    return;
  }
  Object.assign(form, {
    id: null, itemCode: '', itemName: '', deptId: null, maxStayHours: 48, operationLevel: 2,
    anesthesiaType: null, standardFee: null, status: 1, remark: '',
  });
});
const save = async () => {
  if (!form.itemCode.trim() || !form.itemName.trim()) {
    ElMessage.warning('请填写术式编码与名称');
    return;
  }
  saving.value = true;
  try {
    const res = await daySurgeryItemUpsert({
      id: form.id || undefined,
      itemCode: form.itemCode.trim(),
      itemName: form.itemName.trim(),
      deptId: form.deptId || undefined,
      maxStayHours: form.maxStayHours,
      operationLevel: form.operationLevel,
      anesthesiaType: form.anesthesiaType ?? undefined,
      standardFee: form.standardFee ?? undefined,
      status: form.status,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(form.id ? '修改成功' : '术式已录入');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    saving.value = false;
  }
};
</script>
