<template>
  <el-dialog v-model="visible" :title="title" data-testid="kb-dose-dialog" width="680px">
    <el-form :model="formData" label-width="120px">
      <el-form-item label="成分关键字" required>
        <el-input v-model="formData.component" data-testid="kb-form-doseComponent" placeholder="如「对乙酰氨基酚」"/>
      </el-form-item>
      <el-form-item label="剂量单位" required>
        <el-radio-group v-model="formData.doseUnit">
          <el-radio v-for="d in props.doseUnitOptions" :key="d.dictValue" :value="d.dictValue">
            {{ d.dictLabel }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="单次最大量">
        <el-input-number v-model="formData.maxSingleDose" :min="0" :precision="4" :step="1"
                         controls-position="right"/>
        <span class="ml-2 text-[14px] text-slate-400">留空=单次不判</span>
      </el-form-item>
      <el-form-item label="每日最大量">
        <el-input-number v-model="formData.maxDailyDose" :min="0" :precision="4" :step="1" controls-position="right"/>
        <span class="ml-2 text-[14px] text-slate-400">留空=日累计不判；频次写法不认识时这项也自动跳过</span>
      </el-form-item>
      <el-form-item label="口径说明">
        <el-input
            v-model="formData.note"
            :rows="2"
            data-testid="kb-form-note"
            placeholder="按什么人群/剂型定的极量。同一成分不同适应证极量差一个量级，必须写清"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'system:drugKnowledge:add'" :loading="submitting" data-testid="kb-dose-save" type="primary"
                 @click="handleSubmit">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
/**
 * 剂量上限：新增与修改共用一张表单（后端同一个 doseLimitUpsert，row 为空走新增）。
 * 两个极量至少填一项，否则这条规则什么也判不出来。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {doseLimitUpsert} from '@/api/drugKnowledge';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 剂量单位字典 his_dose_unit：只有本表单在用，随表单由页面一次取回下传
  doseUnitOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const title = computed(() => (props.row ? `修改剂量上限：${props.row.component}` : '新增剂量上限'));

const submitting = ref(false);
const emptyForm = () => ({
  id: null,
  component: '',
  doseUnit: 'mg',
  maxSingleDose: null,
  maxDailyDose: null,
  note: '',
  status: 1,
  remark: '',
});
const formData = reactive(emptyForm());

watch(visible, (open) => {
  if (!open) return;
  Object.assign(formData, emptyForm());
  const row = props.row;
  if (!row) return;
  Object.assign(formData, {
    id: row.id,
    component: row.component,
    doseUnit: row.doseUnit || 'mg',
    maxSingleDose: row.maxSingleDose === null || row.maxSingleDose === undefined ? null : Number(row.maxSingleDose),
    maxDailyDose: row.maxDailyDose === null || row.maxDailyDose === undefined ? null : Number(row.maxDailyDose),
    note: row.note || '',
    status: Number(row.status ?? 1),
    remark: row.remark || '',
  });
});

const handleSubmit = async () => {
  if (!formData.component.trim()) {
    return ElMessage.warning('成分关键字不能为空');
  }
  if (formData.maxSingleDose == null && formData.maxDailyDose == null) {
    return ElMessage.warning('单次最大量与每日最大量至少填一项');
  }
  submitting.value = true;
  try {
    await doseLimitUpsert({...formData});
    ElMessage.success('已保存，下一次审方立即生效');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
</script>
