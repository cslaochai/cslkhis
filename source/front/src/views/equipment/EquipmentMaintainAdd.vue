<template>
  <el-dialog v-model="visible" :title="`维保登记 — ${form.equipmentName}`" width="540px">
    <el-form label-width="100px">
      <el-form-item label="维保类型" required>
        <el-radio-group v-model="form.maintainType">
          <el-radio v-for="d in maintainTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">
            {{ d.dictLabel }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="维保日期" required>
        <el-date-picker v-model="form.maintainDate" style="width: 200px" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="下次维保日期">
        <el-date-picker v-model="form.nextMaintainDate" style="width: 200px" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="费用(元)">
        <el-input-number v-model="form.cost" :min="0" :precision="2" style="width: 200px"/>
      </el-form-item>
      <el-form-item label="故障描述">
        <el-input v-model="form.faultDesc" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="处理结果">
        <el-input v-model="form.handleResult" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="维保结果" required>
        <el-radio-group v-model="form.maintainResult">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="2">异常</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="维保人">
        <el-input v-model="form.handlerName" placeholder="不填默认当前登录人" style="width: 200px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:equipment:add'" type="primary" @click="submitMaintain">登记</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {maintainCreate} from '@/api/equipment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 目标设备行（台账「维保登记」按钮传入）
  row: {type: Object, default: null},
  // 维保类型字典由父页一次加载后传入（详情弹框展示同一字典，避免两处各拉一次）
  maintainTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({
  equipmentId: null, equipmentName: '', maintainType: 1,
  maintainDate: '', nextMaintainDate: '', cost: '', faultDesc: '', handleResult: '',
  maintainResult: 1, handlerName: '',
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  Object.assign(form, {
    equipmentId: Number(row.id), equipmentName: row.equipmentName, maintainType: 1,
    maintainDate: '', nextMaintainDate: '', cost: '', faultDesc: '', handleResult: '',
    maintainResult: 1, handlerName: '',
  });
});

const submitMaintain = async () => {
  if (!form.maintainDate) {
    ElMessage.warning('请选择维保日期');
    return;
  }
  try {
    const res = await maintainCreate({
      equipmentId: form.equipmentId,
      maintainType: form.maintainType,
      maintainDate: form.maintainDate,
      nextMaintainDate: form.nextMaintainDate || undefined,
      cost: form.cost || undefined,
      faultDesc: form.faultDesc || undefined,
      handleResult: form.handleResult || undefined,
      maintainResult: form.maintainResult,
      handlerName: form.handlerName || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('维保登记成功');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  }
};
</script>
