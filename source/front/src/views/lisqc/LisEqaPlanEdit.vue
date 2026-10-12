<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑质评批次' : '新建质评批次'" width="640px">
    <el-form label-width="120px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="质评年度" required>
          <el-input-number v-model="form.planYear" :precision="0" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="批次序号" required>
          <el-input-number v-model="form.batchNo" :min="1" :precision="0" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="组织方" required>
          <el-input v-model="form.orgName" data-testid="eqa-plan-org" placeholder="如：湖南省临床检验中心"/>
        </el-form-item>
        <el-form-item label="计划名称">
          <el-input v-model="form.planName"/>
        </el-form-item>
        <el-form-item label="盲样接收日">
          <el-date-picker v-model="form.receiveDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="上报截止日">
          <el-date-picker v-model="form.reportDeadline" style="width: 100%" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="接收人">
          <el-input v-model="form.receiveBy"/>
        </el-form-item>
      </div>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="['medtech:lisEqa:add','medtech:lisEqa:edit']" :loading="saving" type="primary"
                 @click="save">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {eqaPlanUpsert} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null, planYear: new Date().getFullYear(), batchNo: 1, orgName: '', planName: '',
  receiveDate: '', receiveBy: '', reportDeadline: '', remark: '',
});
watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  Object.assign(form, {
    id: row?.id || null,
    planYear: row?.planYear || new Date().getFullYear(),
    batchNo: row?.batchNo || 1,
    orgName: row?.orgName || '', planName: row?.planName || '',
    receiveDate: row?.receiveDate || '', receiveBy: row?.receiveBy || '',
    reportDeadline: row?.reportDeadline || '', remark: row?.remark || '',
  });
});
const save = async () => {
  if (!form.orgName.trim()) {
    ElMessage.warning('组织方不能为空');
    return;
  }
  saving.value = true;
  try {
    const res = await eqaPlanUpsert({
      ...form,
      orgName: form.orgName.trim(),
      receiveDate: form.receiveDate || undefined,
      reportDeadline: form.reportDeadline || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
