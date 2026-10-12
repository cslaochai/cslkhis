<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑语义映射' : '新增语义映射'" width="640px">
    <el-alert
        :closable="false"
        class="qa-semantic-hint"
        show-icon
        title="语义映射是「业务口径的权威来源」：问题文本命中语义词时，该口径会作为优先规则注入模型提示词，避免模型每次现场猜口径。"
        type="info"
    />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="语义词" prop="term">
        <el-input
            v-model="form.term"
            maxlength="64"
            placeholder="如：收入 / 门诊人次 / 发药"
        />
      </el-form-item>
      <el-form-item label="命中表" prop="tableName">
        <el-select
            v-model="form.tableName"
            :fit-input-width="false"
            clearable
            filterable
            placeholder="选择数据表"
            style="width: 100%"
        >
          <el-option
              v-for="t in schemaOptions"
              :key="t.tableName"
              :label="`${t.tableName}（${t.tableDesc}）`"
              :value="t.tableName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="目标列">
        <el-input v-model="form.columnName" maxlength="64" placeholder="可空=表级口径；如 paid_amount"/>
      </el-form-item>
      <el-form-item label="口径说明" prop="scopeDesc">
        <el-input
            v-model="form.scopeDesc"
            :rows="3"
            maxlength="500"
            placeholder="喂给模型作为权威口径的完整说明，如：账单收入以已支付账单（bill_status=3）的 paid_amount 为口径…"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="匹配方式">
        <el-radio-group v-model="form.matchMode">
          <el-radio :value="1">包含（问题文本含该词即命中）</el-radio>
          <el-radio :value="2">精确（归一化后全等才命中）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="优先级">
        <el-input-number v-model="form.priority" :max="999" :min="0"/>
        <span class="qa-form-tip">同词多口径时数值大者先注入</span>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0"/>
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
 * 运营问数语义映射配置「新增/编辑」弹框（自 AiAdminView 拆出）
 *
 * 同时承载新增与修改：row 为空走新增、有值走修改。
 */
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getOperationQaSchemaSelectList, upsertOperationQaSemantic} from '@/api/ai';

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
const schemaOptions = ref([]);
const form = reactive({
  id: null,
  term: '',
  tableName: '',
  columnName: '',
  scopeDesc: '',
  matchMode: 1,
  priority: 0,
  isEnabled: 1,
});

const rules = {
  term: [{required: true, message: '语义词不能为空', trigger: 'blur'}],
  tableName: [{required: true, message: '请选择命中表', trigger: 'change'}],
  scopeDesc: [{required: true, message: '口径说明不能为空', trigger: 'blur'}],
};

// 打开时拉取启用表下拉（页面加载一次，弹框内自取候选）
watch(() => props.modelValue, async (v) => {
  if (!v) {
    return;
  }
  const row = props.row;
  form.id = row?.id ?? null;
  form.term = row?.term ?? '';
  form.tableName = row?.tableName ?? '';
  form.columnName = row?.columnName ?? '';
  form.scopeDesc = row?.scopeDesc ?? '';
  form.matchMode = row?.matchMode ?? 1;
  form.priority = row?.priority ?? 0;
  form.isEnabled = row?.isEnabled ?? 1;
  if (!schemaOptions.value.length) {
    try {
      const res = await getOperationQaSchemaSelectList();
      schemaOptions.value = res?.data ?? [];
    } catch (e) {
      console.error('加载数据表下拉失败', e);
    }
  }
});

const save = async () => {
  try {
    await formRef.value.validate();
  } catch (e) {
    return;
  }
  saving.value = true;
  try {
    await upsertOperationQaSemantic({...form});
    ElMessage.success(form.id ? '已保存' : '已新增');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存语义映射失败', e);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.qa-semantic-hint {
  margin-bottom: 12px;
}

.qa-form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
