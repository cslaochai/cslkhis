<template>
  <el-dialog v-model="visible" title="登记随访（出院后 24h 内必访）" width="600px">
    <el-form label-width="110px" size="small">
      <el-form-item label="随访方式" required>
        <el-radio-group v-model="foForm.followType">
          <el-radio v-for="d in followTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">{{
              d.dictLabel
            }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="随访结果" required>
        <el-select v-model="foForm.result" style="width: 100%">
          <el-option v-for="d in followResultDict" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="随访内容">
        <el-input v-model="foForm.content" :rows="3" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="foLoading" type="primary" @click="submitFollow">登记</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {followDaySurgery} from '@/api/daySurgery';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  followTypeDict: {type: Array, default: () => []},
  followResultDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const foForm = reactive({followType: 1, result: 1, content: ''});
const foLoading = ref(false);
watch(visible, (open) => {
  if (!open) return;
  foForm.followType = 1;
  foForm.result = 1;
  foForm.content = '';
});
const submitFollow = async () => {
  foLoading.value = true;
  try {
    const res = await followDaySurgery({
      id: props.row.id, followType: foForm.followType, result: foForm.result,
      content: foForm.content.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('随访已登记');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '随访登记失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('随访登记失败');
  } finally {
    foLoading.value = false;
  }
};
</script>
