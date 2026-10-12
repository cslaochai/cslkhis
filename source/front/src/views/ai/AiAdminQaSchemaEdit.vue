<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑数据表' : '新增数据表'" width="600px">
    <el-alert
        :closable="false"
        class="qa-schema-hint"
        show-icon
        title="数据表即问数白名单：模型只允许查询此处启用的表。新增表后请到「管理列」补充列配置，列含义建议与表结构注释一致。"
        type="info"
    />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="数据表名" prop="tableName">
        <el-input
            v-model="form.tableName"
            :disabled="!!form.id"
            maxlength="64"
            placeholder="如 biz_settlement_bill"
        />
      </el-form-item>
      <el-form-item label="用途说明" prop="tableDesc">
        <el-input
            v-model="form.tableDesc"
            :rows="2"
            maxlength="200"
            placeholder="喂给模型的表用途说明，如：结算账单：应收/实收/优惠/医保分账"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="form.sortOrder" :max="999" :min="0"/>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0"/>
        <span class="qa-form-tip">停用后该表不再喂给模型，也不可被查询</span>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="500" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 运营问数数据表配置「新增/编辑」弹框（自 AiAdminView 拆出）
 *
 * 同时承载新增与修改：row 为空走新增、有值走修改；表名修改时锁定（后端唯一键 + 列配置归属）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertOperationQaSchema} from '@/api/ai';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑时传入的行（空 = 新增）
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formRef = ref(null);
const saving = ref(false);
const form = reactive({
  id: null,
  tableName: '',
  tableDesc: '',
  sortOrder: 0,
  isEnabled: 1,
  remark: '',
});

const rules = {
  tableName: [
    {required: true, message: '数据表名不能为空', trigger: 'blur'},
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]{0,63}$/,
      message: '表名应为字母开头的下划线小写风格，如 biz_settlement_bill',
      trigger: 'blur',
    },
  ],
  tableDesc: [{required: true, message: '用途说明不能为空', trigger: 'blur'}],
};

watch(() => props.modelValue, (v) => {
  if (!v) {
    return;
  }
  const row = props.row;
  form.id = row?.id ?? null;
  form.tableName = row?.tableName ?? '';
  form.tableDesc = row?.tableDesc ?? '';
  form.sortOrder = row?.sortOrder ?? 0;
  form.isEnabled = row?.isEnabled ?? 1;
  form.remark = row?.remark ?? '';
});

const save = async () => {
  try {
    await formRef.value.validate();
  } catch (e) {
    return;
  }
  saving.value = true;
  try {
    await upsertOperationQaSchema({...form});
    ElMessage.success(form.id ? '已保存' : '已新增');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存数据表配置失败', e);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.qa-schema-hint {
  margin-bottom: 12px;
}

.qa-form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
