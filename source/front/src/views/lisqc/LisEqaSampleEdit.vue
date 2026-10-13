<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑盲样登记' : '登记盲样'" width="640px">
    <el-form label-width="110px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="盲样编号" required>
          <el-input v-model="form.sampleNo"/>
        </el-form-item>
        <el-form-item label="样品序号" required>
          <el-input-number v-model="form.sampleSeq" :min="1" :precision="0" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="项目编码" required>
          <el-input v-model="form.itemCode"/>
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="form.itemName"/>
        </el-form-item>
        <el-form-item label="检测仪器">
          <el-input v-model="form.instrumentName"/>
        </el-form-item>
        <el-form-item label="方法学">
          <el-input v-model="form.methodName"/>
        </el-form-item>
        <el-form-item label="接收日期">
          <el-date-picker v-model="form.receiveDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="接收人">
          <el-input v-model="form.receiveBy"/>
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button v-perm="['medtech:lisEqa:add','medtech:lisEqa:edit']" :loading="saving" type="primary"
                 @click="save">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {eqaSampleUpsert} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  planId: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null, planId: null, sampleNo: '', sampleSeq: 1, itemCode: '', itemName: '',
  instrumentName: '', methodName: '', receiveDate: '', receiveBy: '',
});
watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  Object.assign(form, {
    id: row?.id || null,
    planId: props.planId || null,
    sampleNo: row?.sampleNo || '', sampleSeq: row?.sampleSeq || 1,
    itemCode: row?.itemCode || '', itemName: row?.itemName || '',
    instrumentName: row?.instrumentName || '', methodName: row?.methodName || '',
    receiveDate: row?.receiveDate || '', receiveBy: row?.receiveBy || '',
  });
});
const save = async () => {
  if (!form.planId) {
    ElMessage.warning('请先选择质评批次');
    return;
  }
  if (!form.sampleNo.trim() || !form.itemCode.trim() || !form.itemName.trim()) {
    ElMessage.warning('盲样编号、项目编码与名称必填');
    return;
  }
  saving.value = true;
  try {
    const res = await eqaSampleUpsert({...form});
    if (res.code === 200) {
      ElMessage.success(res.message || '已登记');
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
