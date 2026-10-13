<template>
  <el-dialog v-model="visible" title="安排手术" width="600px">
    <el-form label-width="110px" size="small">
      <el-form-item label="手术时间" required>
        <el-date-picker v-model="arrangeForm.surgeryTime" placeholder="请选择" style="width: 100%"
                        type="datetime" value-format="YYYY-MM-DD HH:mm"/>
      </el-form-item>
      <el-form-item label="手术间" required>
        <el-input v-model="arrangeForm.operatingRoom" placeholder="如：日间手术室 1 间"/>
      </el-form-item>
      <el-form-item label="台次">
        <el-input-number v-model="arrangeForm.seqNo" :max="99" :min="1"/>
      </el-form-item>
      <el-form-item label="麻醉方式" required>
        <el-select v-model="arrangeForm.anesthesiaType" placeholder="请选择" style="width: 100%">
          <el-option v-for="d in anesDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="主刀医生" required>
        <el-input v-model="arrangeForm.surgeon"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :icon="Scissor" :loading="arrangeLoading" type="primary" @click="submitArrange">安排</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Scissor} from '@element-plus/icons-vue';
import {arrangeDaySurgery} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  anesDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const arrangeForm = reactive({
  surgeryTime: '', operatingRoom: '', seqNo: null,
  anesthesiaType: null, surgeon: '',
});
const arrangeLoading = ref(false);
watch(visible, (open) => {
  if (!open) return;
  arrangeForm.surgeryTime = '';
  arrangeForm.operatingRoom = '';
  arrangeForm.seqNo = null;
  arrangeForm.anesthesiaType = null;
  arrangeForm.surgeon = props.row?.doctorName || '';
});
const submitArrange = async () => {
  if (!arrangeForm.surgeryTime) {
    ElMessage.warning('请选择手术时间');
    return;
  }
  if (!arrangeForm.operatingRoom.trim()) {
    ElMessage.warning('请填写手术间');
    return;
  }
  if (!arrangeForm.anesthesiaType) {
    ElMessage.warning('请选择麻醉方式');
    return;
  }
  if (!arrangeForm.surgeon.trim()) {
    ElMessage.warning('请填写主刀医生');
    return;
  }
  arrangeLoading.value = true;
  try {
    const res = await arrangeDaySurgery({
      id: props.row.id,
      surgeryTime: arrangeForm.surgeryTime.length === 16 ? `${arrangeForm.surgeryTime}:00` : arrangeForm.surgeryTime,
      operatingRoom: arrangeForm.operatingRoom.trim(),
      seqNo: arrangeForm.seqNo ?? undefined,
      anesthesiaType: arrangeForm.anesthesiaType,
      surgeon: arrangeForm.surgeon.trim(),
    });
    if (res.code === 200) {
      ElMessage.success('已安排');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    arrangeLoading.value = false;
  }
};
</script>
