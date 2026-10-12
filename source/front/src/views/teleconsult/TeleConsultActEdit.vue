<template>
  <el-dialog v-model="visible" :title="actKey === 'complete' ? '填写会诊意见' : '取消会诊'" width="600px">
    <el-form label-width="100px" size="small">
      <el-form-item :label="actKey === 'complete' ? '会诊意见' : '取消原因'" required>
        <el-input v-model="teleActContent" :placeholder="actKey === 'complete' ? '会诊意见（完成后为终态，不可再改）' : '取消原因（必填，将留痕）'" :rows="5"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="teleActLoading" type="primary" @click="submitTeleAct">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 完成 / 取消（共用内容弹窗）：actKey 一个弹框两种动作，切标题、字段文案与提交接口，
 * 两种动作共用同一份内容 state，禁止拆成两个组件丢掉联动。
 *
 * 已安排才可出意见是后端铁律，前端只在能点时给按钮，不在本地拦截。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {cancelTeleConsult, completeTeleConsult} from '@/api/teleconsult';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 'complete'-出意见完成 / 'cancel'-取消
  actKey: {type: String, default: 'complete'},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const teleActContent = ref('');
const teleActLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  teleActContent.value = '';
});

const submitTeleAct = async () => {
  if (!teleActContent.value.trim()) {
    ElMessage.warning(props.actKey === 'complete' ? '请填写会诊意见' : '请填写取消原因');
    return;
  }
  teleActLoading.value = true;
  try {
    const res = props.actKey === 'complete'
        ? await completeTeleConsult({id: props.row.id, content: teleActContent.value.trim()})
        : await cancelTeleConsult({id: props.row.id, content: teleActContent.value.trim()});
    if (res.code === 200) {
      ElMessage.success(props.actKey === 'complete' ? '会诊已完成' : '已取消');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  } finally {
    teleActLoading.value = false;
  }
};
</script>
