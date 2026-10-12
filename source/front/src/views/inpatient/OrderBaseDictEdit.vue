<template>
  <el-dialog v-model="visible" :title="editing ? '编辑字典项' : `新增${tab.label}`" width="520px">
    <el-form label-width="90px">
      <el-form-item label="字典类型">
        <el-input :model-value="tab.label" disabled/>
      </el-form-item>
      <el-form-item label="值" required>
        <el-input
            v-model="form.dictValue"
            :disabled="!!editing"
            :placeholder="editing ? '值不可修改（存量医嘱在用它）' : '如 静滴 / qd / mg'"
            data-testid="orderdict-form-value"
        />
      </el-form-item>
      <el-form-item label="显示名" required>
        <el-input v-model="form.dictLabel" data-testid="orderdict-form-label" placeholder="下拉里显示的文字"/>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.dictSort" :min="1" class="!w-full" controls-position="right"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="500" placeholder="选填" show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>
    <el-alert
        v-if="editing"
        :closable="false"
        class="mt-2"
        show-icon
        title="值不允许修改：存量医嘱行里存的就是它，改了历史医嘱会显示成「未知」。要换值请停用这一条、再新增一条。"
        type="info"
    />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" data-testid="orderdict-submit" type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertOrderDict} from '@/api/inpatientOrder';
import {ORDER_DICT_TYPE} from '@/lib/drugUsage';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 当前编辑行：为空走新增
  row: {type: Object, default: null},
  // 当前页签（字典类型）：新增时取它当 dictType，编辑时只用于「字典类型」只读展示
  tab: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
// 有 row 即编辑态（值列禁改 + 提示不可改值）
const editing = computed(() => !!props.row);
const form = reactive({
  id: null,
  dictType: ORDER_DICT_TYPE.ROUTE,
  dictValue: '',
  dictLabel: '',
  dictSort: null,
  status: 1,
  remark: '',
});
const initCreate = () => {
  form.id = null;
  form.dictType = props.tab.type;
  form.dictValue = '';
  form.dictLabel = '';
  form.dictSort = null;
  form.status = 1;
  form.remark = '';
};
const initEdit = (row) => {
  form.id = row.id;
  form.dictType = row.dictType;
  form.dictValue = row.dictValue;
  form.dictLabel = row.dictLabel;
  form.dictSort = row.dictSort;
  form.status = row.status ?? 1;
  form.remark = row.remark || '';
};
watch(visible, (open) => {
  if (!open) return;
  if (props.row) {
    initEdit(props.row);
  } else {
    initCreate();
  }
});
const submit = async () => {
  if (!form.dictLabel) {
    ElMessage.warning('请填写显示名');
    return;
  }
  if (!editing.value && !form.dictValue) {
    ElMessage.warning('请填写字典值');
    return;
  }
  saving.value = true;
  try {
    await upsertOrderDict({
      id: form.id || undefined,
      dictType: form.dictType,
      dictValue: editing.value ? undefined : form.dictValue,
      dictLabel: form.dictLabel,
      dictSort: form.dictSort ?? undefined,
      status: form.status,
      remark: form.remark || undefined,
    });
    ElMessage.success('已保存');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
