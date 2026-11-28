<template>
  <!-- 新建配送任务单（原 RobotDispatchView 内联弹框）：表单/校验/提交都在这里，主视图只控显隐 -->
  <el-dialog v-model="visible" title="新建配送任务单" width="560px" @closed="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="发起来源" prop="sourceType">
        <el-select v-model="form.sourceType" style="width: 100%">
          <el-option v-for="s in sourceOptions" :key="s.value" :label="s.label" :value="s.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发起站点" prop="sourceDeptName">
        <el-input v-model="form.sourceDeptName" placeholder="如 B1 静配中心 / 住院药房"/>
      </el-form-item>
      <el-form-item label="物品品类" prop="goodsType">
        <el-select v-model="form.goodsType" style="width: 100%">
          <el-option v-for="g in goodsTypeOptions" :key="g" :label="g" :value="g"/>
        </el-select>
      </el-form-item>
      <el-form-item label="物品明细" prop="goodsName">
        <el-input v-model="form.goodsName" placeholder="如 0.9%氯化钠注射液 250ml×24 瓶"/>
      </el-form-item>
      <el-form-item label="取货点" prop="fromStation">
        <el-input v-model="form.fromStation" placeholder="如 B1 中心药房"/>
      </el-form-item>
      <el-form-item label="目标楼层" prop="toFloor">
        <el-input-number v-model="form.toFloor" :min="1" :max="20" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="收货站点" prop="toStation">
        <el-input v-model="form.toStation" placeholder="如 12F 护士站"/>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="form.priority">
          <el-radio v-for="p in priorityOptions" :key="p.value" :value="p.value">{{ p.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="箱数" prop="boxCount">
        <el-input-number v-model="form.boxCount" :min="1" :max="4"/>
      </el-form-item>
      <el-form-item label="总重(kg)" prop="loadKg">
        <el-input-number v-model="form.loadKg" :min="0" :max="50" :step="0.5"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交并派车</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {robotDispatchCreate} from '@/api/robotDispatch';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  sourceOptions: {type: Array, default: () => []},
  goodsTypeOptions: {type: Array, default: () => []},
  priorityOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)});

const submitting = ref(false);
const formRef = ref(null);
const form = reactive({
  sourceType: 'MANUAL',
  sourceDeptName: '',
  goodsType: '药品',
  goodsName: '',
  fromStation: 'B1 中心药房',
  toFloor: 12,
  toStation: '护士站',
  priority: 1,
  boxCount: 1,
  loadKg: 3,
  remark: '',
});
const rules = {
  goodsName: [{required: true, message: '请填写物品明细', trigger: 'blur'}],
  fromStation: [{required: true, message: '请填写取货点', trigger: 'blur'}],
  toStation: [{required: true, message: '请填写收货站点', trigger: 'blur'}],
};

const resetForm = () => {
  Object.assign(form, {
    sourceType: 'MANUAL', sourceDeptName: '', goodsType: '药品', goodsName: '',
    fromStation: 'B1 中心药房', toFloor: 12, toStation: '护士站',
    priority: 1, boxCount: 1, loadKg: 3, remark: '',
  });
  formRef.value?.clearValidate();
};

const submit = async () => {
  await formRef.value.validate();
  submitting.value = true;
  try {
    // autoAssign=true：提交即由后端派单算法选车，不需要人工先派
    await robotDispatchCreate({...form, autoAssign: true});
    ElMessage.success('任务单已提交并派车');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('新建配送任务单失败', e);
    ElMessage.error(e?.response?.data?.message || '提交失败');
  } finally {
    submitting.value = false;
  }
};
</script>
