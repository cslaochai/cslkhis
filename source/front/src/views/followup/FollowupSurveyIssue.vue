<template>
  <!-- 补发满意度问卷 -->
  <el-dialog v-model="visible" title="补发满意度问卷" width="480px">
    <el-form label-width="90px">
      <el-form-item label="随访任务">
        <el-input :model-value="issueForm.taskNo" disabled/>
      </el-form-item>
      <el-form-item label="回收渠道">
        <el-select v-model="issueForm.channel" :fit-input-width="false" style="width: 200px">
          <el-option :value="1" label="电话代填（唯一实测可回收）"/>
          <el-option :value="2" label="短信（无网关，只落状态）"/>
          <el-option :value="3" label="微信（无网关，只落状态）"/>
          <el-option :value="4" label="现场扫码"/>
        </el-select>
      </el-form-item>
      <el-form-item label="有效期">
        <el-input-number v-model="issueForm.expireDays" :min="1" :precision="0"/>
        <span class="hint">天后过期（默认 14）</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="issueLoading" type="primary" @click="submitIssue">发放</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 手工补发满意度问卷：正常路径是「完成随访」自动发，这里只服务两种缺口 ——
 * 完成那天院内有事没配问卷（后端跳过不报错），或电话重打了一次要再收一份。
 * 同一任务 + 同一模板幂等，重复点不会多出第二张发放单。
 *
 * 发放成功后不刷新任务列表（列表列不显示发放状态，原实现同样只关弹框），故本组件不声明 saved。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {issueDispatch} from '@/api/survey';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 列表行：只取 taskNo 与 id
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const issueLoading = ref(false);
const issueForm = reactive({taskNo: '', followupTaskId: '', channel: 1, expireDays: 14});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  Object.assign(issueForm, {taskNo: row.taskNo, followupTaskId: row.id, channel: 1, expireDays: 14});
});

const submitIssue = async () => {
  issueLoading.value = true;
  try {
    const res = await issueDispatch({
      followupTaskId: issueForm.followupTaskId,
      channel: issueForm.channel,
      expireDays: issueForm.expireDays,
    });
    if (res.code === 200) {
      ElMessage.success(`已发放：${res.data?.dispatchNo || ''}（到「满意度评价 → 发放回收」录入答卷）`);
      visible.value = false;
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    issueLoading.value = false;
  }
};
</script>

<style scoped>
.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
