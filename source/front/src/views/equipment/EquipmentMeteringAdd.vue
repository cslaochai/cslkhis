<template>
  <el-dialog v-model="visible" :title="`计量登记 — ${form.equipmentName}`" width="520px">
    <el-form label-width="100px">
      <el-form-item label="计量类型" required>
        <el-radio-group v-model="form.meteringType">
          <el-radio v-for="d in meteringTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">
            {{ d.dictLabel }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="计量日期" required>
        <el-date-picker v-model="form.meteringDate" style="width: 200px" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="有效期至" required>
        <el-date-picker v-model="form.validUntil" style="width: 200px" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="计量结果" required>
        <el-radio-group v-model="form.meteringResult">
          <el-radio :value="1">合格</el-radio>
          <el-radio :value="2">不合格</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="证书编号">
        <el-input v-model="form.certNo" style="width: 220px"/>
      </el-form-item>
      <el-form-item label="检定机构">
        <el-input v-model="form.agency" style="width: 220px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:equipment:add'" type="primary" @click="submitMetering">登记</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {meteringCreate} from '@/api/equipment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 目标设备行（台账「计量登记」按钮传入）
  row: {type: Object, default: null},
  // 计量类型字典由父页一次加载后传入（详情弹框展示同一字典，避免两处各拉一次）
  meteringTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({
  equipmentId: null, equipmentName: '', meteringType: 1,
  meteringDate: '', validUntil: '', meteringResult: 1, certNo: '', agency: '',
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  Object.assign(form, {
    equipmentId: Number(row.id), equipmentName: row.equipmentName, meteringType: 1,
    meteringDate: '', validUntil: '', meteringResult: 1, certNo: '', agency: '',
  });
});

const submitMetering = async () => {
  if (!form.meteringDate || !form.validUntil) {
    ElMessage.warning('请选择计量日期与有效期至');
    return;
  }
  try {
    const res = await meteringCreate({
      equipmentId: form.equipmentId,
      meteringType: form.meteringType,
      meteringDate: form.meteringDate,
      validUntil: form.validUntil,
      meteringResult: form.meteringResult,
      certNo: form.certNo || undefined,
      agency: form.agency || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('计量登记成功');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  }
};
</script>
