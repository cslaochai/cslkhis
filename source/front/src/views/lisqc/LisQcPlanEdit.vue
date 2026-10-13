<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑质控计划' : '新建质控计划'" width="640px">
    <el-form label-width="110px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="项目编码">
          <el-input v-model="form.itemCode"/>
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="form.itemName"/>
        </el-form-item>
        <el-form-item label="仪器编号">
          <el-input v-model="form.instrumentNo"/>
        </el-form-item>
        <el-form-item label="仪器名称">
          <el-input v-model="form.instrumentName"/>
        </el-form-item>
        <el-form-item label="质控水平">
          <el-select v-model="form.qcLevel" style="width: 100%">
            <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="质控品效期">
          <el-date-picker v-model="form.expireDate" style="width: 100%" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="质控品名称">
          <el-input v-model="form.controlName"/>
        </el-form-item>
        <el-form-item label="批号">
          <el-input v-model="form.controlLotNo"/>
        </el-form-item>
        <el-form-item label="靶值（均值）" required>
          <el-input-number v-model="form.meanValue" :precision="4" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="标准差 SD" required>
          <el-input-number v-model="form.sdValue" :min="0.0001" :precision="4" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="允许 CV 上限%">
          <el-input-number v-model="form.cvLimit" :precision="2" style="width: 100%"/>
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button v-perm="['medtech:lisQc:add','medtech:lisQc:edit']" :loading="saving" type="primary"
                 @click="save">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {qcPlanUpsert} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  levelDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null, itemCode: '', itemName: '', instrumentNo: '', instrumentName: '', qcLevel: 2,
  controlName: '', controlLotNo: '', meanValue: null, sdValue: null,
  cvLimit: null, expireDate: '',
});
watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  Object.assign(form, {
    id: row?.id || null, itemCode: row?.itemCode || '', itemName: row?.itemName || '',
    instrumentNo: row?.instrumentNo || '', instrumentName: row?.instrumentName || '',
    qcLevel: row?.qcLevel || 2, controlName: row?.controlName || '', controlLotNo: row?.controlLotNo || '',
    meanValue: row?.meanValue ?? null, sdValue: row?.sdValue ?? null, cvLimit: row?.cvLimit ?? null,
    expireDate: row?.expireDate || '',
  });
});
const save = async () => {
  if (!form.itemName.trim()) {
    ElMessage.warning('检验项目名称不能为空');
    return;
  }
  if (form.meanValue == null || form.sdValue == null) {
    ElMessage.warning('靶值与 SD 必填');
    return;
  }
  saving.value = true;
  try {
    const res = await qcPlanUpsert({...form, expireDate: form.expireDate || undefined});
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
    saving.value = false;
  }
};
</script>
