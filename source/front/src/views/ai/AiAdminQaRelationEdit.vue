<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑表间关系' : '新增表间关系'" width="680px">
    <el-alert
        :closable="false"
        class="qa-relation-hint"
        show-icon
        title="表间关系用于说明两张表按哪个字段关联，问数需要跨表取数时直接按这里登记的连接条件拼表。"
        type="info"
    />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="源表.源列" prop="sourceTable">
        <div class="relation-pair">
          <el-select
              v-model="form.sourceTable"
              :fit-input-width="false"
              clearable
              filterable
              placeholder="源表"
              style="width: 46%"
              @change="onSourceTableChange"
          >
            <el-option
                v-for="t in schemaOptions"
                :key="t.tableName"
                :label="`${t.tableName}（${t.tableDesc}）`"
                :value="t.tableName"
            />
          </el-select>
          <span class="relation-dot">.</span>
          <el-select
              v-model="form.sourceColumn"
              :disabled="!form.sourceTable"
              :fit-input-width="false"
              filterable
              placeholder="源列"
              style="width: 46%"
          >
            <el-option
                v-for="c in sourceColumnOptions"
                :key="c.columnName"
                :label="`${c.columnName}（${c.columnDesc || ''}）`"
                :value="c.columnName"
            />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="目标表.目标列" prop="targetTable">
        <div class="relation-pair">
          <el-select
              v-model="form.targetTable"
              :fit-input-width="false"
              clearable
              filterable
              placeholder="目标表"
              style="width: 46%"
              @change="onTargetTableChange"
          >
            <el-option
                v-for="t in schemaOptions"
                :key="t.tableName"
                :label="`${t.tableName}（${t.tableDesc}）`"
                :value="t.tableName"
            />
          </el-select>
          <span class="relation-dot">.</span>
          <el-select
              v-model="form.targetColumn"
              :disabled="!form.targetTable"
              :fit-input-width="false"
              filterable
              placeholder="目标列"
              style="width: 46%"
          >
            <el-option
                v-for="c in targetColumnOptions"
                :key="c.columnName"
                :label="`${c.columnName}（${c.columnDesc || ''}）`"
                :value="c.columnName"
            />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="关系类型">
        <el-radio-group v-model="form.relationType">
          <el-radio :value="2">维度关联（取维度名称，如科室）</el-radio>
          <el-radio :value="1">业务关联（同域单据链）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="关系说明" prop="description">
        <el-input
            v-model="form.description"
            :rows="3"
            maxlength="200"
            placeholder="说明此关系的使用场景，如：按科室统计入院人数时，按此关联取科室名称"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="form.sortOrder" :max="999" :min="0"/>
        <span class="qa-form-tip">小者优先</span>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0"/>
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
 * 运营问数表间关系配置「新增/编辑」弹框（自 AiAdminView 拆出）
 *
 * 同时承载新增与修改：row 为空走新增、有值走修改。
 * 源/目标表选择后联动拉取该表的启用字段，源列/目标列从字段下拉中选，不再手输。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getOperationQaColumnSelectList, getOperationQaSchemaSelectList, upsertOperationQaRelation} from '@/api/ai';

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
const sourceColumnOptions = ref([]);
const targetColumnOptions = ref([]);
const form = reactive({
  id: null,
  sourceTable: '',
  sourceColumn: '',
  targetTable: '',
  targetColumn: '',
  relationType: 2,
  description: '',
  sortOrder: 0,
  isEnabled: 1,
});

const rules = {
  sourceTable: [{required: true, message: '请选择源表', trigger: 'change'}],
  sourceColumn: [{required: true, message: '请选择源列', trigger: 'change'}],
  targetTable: [{required: true, message: '请选择目标表', trigger: 'change'}],
  targetColumn: [{required: true, message: '请选择目标列', trigger: 'change'}],
  description: [{required: true, message: '关系说明不能为空', trigger: 'blur'}],
};

async function loadColumnOptions(table, into) {
  if (!table) {
    into.value = [];
    return;
  }
  try {
    const res = await getOperationQaColumnSelectList(table);
    into.value = res?.data ?? [];
  } catch (e) {
    console.error('加载字段下拉失败', e);
    into.value = [];
  }
}

function onSourceTableChange() {
  form.sourceColumn = '';
  loadColumnOptions(form.sourceTable, sourceColumnOptions);
}

function onTargetTableChange() {
  form.targetColumn = '';
  loadColumnOptions(form.targetTable, targetColumnOptions);
}

// 打开时拉取启用表下拉（页面加载一次，弹框内自取候选）
watch(() => props.modelValue, async (v) => {
  if (!v) {
    return;
  }
  const row = props.row;
  form.id = row?.id ?? null;
  form.sourceTable = row?.sourceTable ?? '';
  form.sourceColumn = row?.sourceColumn ?? '';
  form.targetTable = row?.targetTable ?? '';
  form.targetColumn = row?.targetColumn ?? '';
  form.relationType = row?.relationType ?? 2;
  form.description = row?.description ?? '';
  form.sortOrder = row?.sortOrder ?? 0;
  form.isEnabled = row?.isEnabled ?? 1;
  if (!schemaOptions.value.length) {
    try {
      const res = await getOperationQaSchemaSelectList();
      schemaOptions.value = res?.data ?? [];
    } catch (e) {
      console.error('加载数据表下拉失败', e);
    }
  }
  // 编辑回显：按已选表把列下拉补出来（回显列即便不在启用列里，也作为当前值显示，不拦截保存）
  if (form.sourceTable) {
    await loadColumnOptions(form.sourceTable, sourceColumnOptions);
    if (form.sourceColumn && !sourceColumnOptions.value.some(c => c.columnName === form.sourceColumn)) {
      sourceColumnOptions.value = [{columnName: form.sourceColumn, columnDesc: ''}, ...sourceColumnOptions.value];
    }
  }
  if (form.targetTable) {
    await loadColumnOptions(form.targetTable, targetColumnOptions);
    if (form.targetColumn && !targetColumnOptions.value.some(c => c.columnName === form.targetColumn)) {
      targetColumnOptions.value = [{columnName: form.targetColumn, columnDesc: ''}, ...targetColumnOptions.value];
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
    await upsertOperationQaRelation({...form});
    ElMessage.success(form.id ? '已保存' : '已新增');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存表间关系失败', e);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.qa-relation-hint {
  margin-bottom: 12px;
}

.relation-pair {
  display: flex;
  align-items: center;
  width: 100%;
}

.relation-dot {
  margin: 0 8px;
  font-weight: 700;
  color: #909399;
}

.qa-form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
