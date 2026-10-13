<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑列' : '新增列'" width="600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="数据表">
        <el-input :model-value="tableName" disabled/>
      </el-form-item>
      <el-form-item label="列名" prop="columnName">
        <el-input
            v-model="form.columnName"
            :disabled="!!form.id"
            maxlength="64"
            placeholder="如 paid_amount"
        />
      </el-form-item>
      <el-form-item label="列含义" prop="columnDesc">
        <el-input
            v-model="form.columnDesc"
            :rows="2"
            maxlength="200"
            placeholder="喂给模型的列含义说明，建议与表结构注释一致，如：已收合计"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="用途标记">
        <el-checkbox v-model="isFilterable" :true-value="1" :false-value="0">可用于过滤条件（WHERE）</el-checkbox>
        <div class="qa-column-tip">
          <el-checkbox v-model="isGroupable" :true-value="1" :false-value="0">可用于分组统计（GROUP BY）</el-checkbox>
        </div>
      </el-form-item>
      <el-form-item label="是否喂给模型">
        <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0"/>
        <span class="qa-form-tip">停用后该列不出现在模型提示词中；患者个人信息列应停用</span>
      </el-form-item>
      <el-form-item label="敏感类型">
        <el-select v-model="form.sensitiveType" placeholder="普通" style="width: 240px">
          <el-option :value="0" label="0-普通（不处理）"/>
          <el-option :value="1" label="1-脱敏（出参打码）"/>
          <el-option :value="2" label="2-禁止出库（SQL 层拦截）"/>
        </el-select>
        <span class="qa-form-tip">敏感字段集中在这里维护，问数链路自动生效</span>
      </el-form-item>
      <el-form-item v-if="form.sensitiveType === 1" label="脱敏方式">
        <el-select v-model="form.maskType" placeholder="选择脱敏方式" style="width: 240px">
          <el-option value="phone" label="phone-手机号"/>
          <el-option value="idcard" label="idcard-身份证"/>
          <el-option value="email" label="email-邮箱"/>
          <el-option value="cardno" label="cardno-卡号"/>
          <el-option value="middle" label="middle-姓名/中间打码"/>
        </el-select>
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="form.sortOrder" :max="999" :min="0"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="save">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 运营问数数据列配置「新增/编辑」弹框（自 AiAdminView 拆出，由列管理弹框挂载）
 *
 * 同时承载新增与修改：row 为空走新增、有值走修改；列名修改时锁定。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertOperationQaColumn} from '@/api/ai';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 所属表名（必填）
  tableName: {type: String, required: true},
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
  columnName: '',
  columnDesc: '',
  isFilterable: 0,
  isGroupable: 1,
  isEnabled: 1,
  sensitiveType: 0,
  maskType: '',
  sortOrder: 0,
});
// 用途标记用两个独立布尔，提交时合并进 form
const isFilterable = ref(0);
const isGroupable = ref(1);

const rules = {
  columnName: [
    {required: true, message: '列名不能为空', trigger: 'blur'},
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]{0,63}$/,
      message: '列名应为字母开头的下划线小写风格，如 paid_amount',
      trigger: 'blur',
    },
  ],
  columnDesc: [{required: true, message: '列含义不能为空', trigger: 'blur'}],
};

watch(() => props.modelValue, (v) => {
  if (!v) {
    return;
  }
  const row = props.row;
  form.id = row?.id ?? null;
  form.columnName = row?.columnName ?? '';
  form.columnDesc = row?.columnDesc ?? '';
  isFilterable.value = row?.isFilterable ?? 0;
  isGroupable.value = row?.isGroupable ?? 1;
  form.isEnabled = row?.isEnabled ?? 1;
  form.sensitiveType = row?.sensitiveType ?? 0;
  form.maskType = row?.maskType ?? '';
  form.sortOrder = row?.sortOrder ?? 0;
});

const save = async () => {
  try {
    await formRef.value.validate();
  } catch (e) {
    return;
  }
  saving.value = true;
  try {
    await upsertOperationQaColumn({
      id: form.id,
      tableName: props.tableName,
      columnName: form.columnName,
      columnDesc: form.columnDesc,
      isFilterable: isFilterable.value,
      isGroupable: isGroupable.value,
      isEnabled: form.isEnabled,
      sensitiveType: form.sensitiveType,
      maskType: form.sensitiveType === 1 ? form.maskType : null,
      sortOrder: form.sortOrder,
    });
    ElMessage.success(form.id ? '已保存' : '已新增');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存列配置失败', e);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.qa-column-tip {
  margin-top: 6px;
}

.qa-form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
