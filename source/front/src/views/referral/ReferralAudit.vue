<template>
  <el-dialog v-model="visible" title="确认转诊" width="480px">
    <el-form label-width="90px">
      <el-form-item label="转入科室">
        <el-select v-model="form.toDeptId" :fit-input-width="false" clearable filterable style="width: 240px">
          <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="确认意见">
        <el-input v-model="form.auditRemark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitAudit">确认</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 确认转诊弹框（自 ReferralView 拆出）
 *
 * 打开时从 row 快照取 referralId / toDeptId（与拆分前 openAudit 口径一致）；
 * 状态机 0 待确认 → 1 已确认，确认动作与意见提交在这里，成功后 emit('saved') 让列表刷新。
 */
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {referralAudit} from '@/api/referral';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 待确认行快照（referralId / toDeptId 从这取）
  row: {type: Object, default: null},
  // 科室下拉数据（页面已加载）
  depts: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({referralId: null, toDeptId: null, auditRemark: ''});

watch(() => props.modelValue, (v) => {
  if (!v) return;
  const r = props.row || {};
  Object.assign(form, {referralId: r.referralId ?? null, toDeptId: r.toDeptId ?? null, auditRemark: ''});
});

const submitAudit = async () => {
  try {
    const res = await referralAudit(form.referralId, form.toDeptId ?? undefined, form.auditRemark || undefined);
    if (res.code === 200) {
      ElMessage.success('转诊已确认');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
</script>
