<template>
  <!-- 总检弹窗 -->
  <el-dialog v-model="visible" :title="`总检出报告 - ${concludeForm.recordNo}`" width="480px">
    <el-form label-width="90px">
      <el-form-item label="总检结论" required>
        <el-input v-model="concludeForm.conclusion" :rows="4" data-testid="checkup-conclusion-input"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="总检医师">
        <el-input v-model="concludeForm.doctorName"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'checkup:manage:edit'" data-testid="checkup-conclude-submit" type="primary"
                 @click="submitConclude">出报告
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 总检出报告（3 已完成 → 4 已出报告）
 * 开弹窗前按 id 复查状态：存在未录入结果项（recordStatus !== 3）时警告并自行关闭，
 * 与原先「父页面先取数校验再打开」同口径。
 */
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {checkupConclude, getCheckupRecord} from '@/api/checkup';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前登记行（列表操作列或结果明细弹窗抛出），只取 id/recordNo 定位记录
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const concludeForm = reactive({recordId: null, recordNo: '', conclusion: '', doctorName: ''});

watch(
  () => props.modelValue,
  async (v) => {
    if (!v) return;
    concludeForm.recordId = props.row?.id ?? null;
    concludeForm.recordNo = props.row?.recordNo ?? '';
    concludeForm.conclusion = '';
    concludeForm.doctorName = '';
    try {
      const res = await getCheckupRecord(props.row?.id);
      const d = res?.data;
      if (d?.recordStatus !== 3) {
        ElMessage.warning('存在未录入的结果项，不能总检出报告');
        visible.value = false;
        return;
      }
      concludeForm.recordId = d.id;
      concludeForm.recordNo = d.recordNo;
    } catch (e) {
      
      visible.value = false;
    }
  }
);

const submitConclude = async () => {
  if (!concludeForm.conclusion.trim())
    return ElMessage.warning('总检结论必填');
  try {
    await checkupConclude({
      recordId: concludeForm.recordId, conclusion: concludeForm.conclusion, doctorName: concludeForm.doctorName,
    });
    ElMessage.success('体检报告已出');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
</script>
