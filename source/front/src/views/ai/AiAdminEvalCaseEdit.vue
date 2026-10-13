<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑评测用例' : '新增评测用例'" width="640px" append-to-body>
    <el-form :model="form" label-width="100px">
      <el-form-item label="评测问题" required>
        <el-input v-model="form.question" maxlength="500" placeholder="如：抽血前需要空腹吗" type="textarea" :rows="3"/>
      </el-form-item>
      <el-form-item label="期望命中文档" required>
        <el-select
            v-model="form.expectedDocIds"
            :fit-input-width="false"
            clearable
            filterable
            multiple
            placeholder="选择检索期望命中的知识文档（可多选）"
            style="width: 100%"
        >
          <el-option v-for="d in docOptions" :key="d.id" :label="d.title" :value="d.id"/>
        </el-select>
        <div class="field-tip">命中判据：该文档（至少一个切块）出现在检索 top-k 结果中即算命中</div>
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="form.remark" maxlength="200" placeholder="选填，如：验证门诊须知类问题"/>
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
 * 评测用例「新增/编辑」弹框（自 AiAdminView 拆出）。
 * 期望命中文档从知识文档列表选择（一次拉 200 条），保存时 id/标题都回传（分号分隔）供展示。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getKnowledgeDocPage, upsertKnowledgeEvalCase} from '@/api/ai';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑时传当前行（null/undefined 走新增）
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({
  id: null,
  question: '',
  expectedDocIds: [],
  remark: '',
});
const saving = ref(false);

// 期望命中文档候选：知识文档列表（分页参数取全量一页，评测选择场景够用）
const docOptions = ref([]);
const loadDocOptions = async () => {
  try {
    const res = await getKnowledgeDocPage({pageNum: 1, pageSize: 200});
    docOptions.value = res?.data?.records || [];
  } catch (e) {
    console.error('加载知识文档列表失败', e);
  }
};

watch(() => props.modelValue, (v) => {
  if (!v) return;
  const row = props.row;
  form.id = row?.id ?? null;
  form.question = row?.question || '';
  form.expectedDocIds = row?.expectedDocIds ? row.expectedDocIds.split(';').filter(Boolean) : [];
  form.remark = row?.remark || '';
  loadDocOptions();
});

// 期望命中文档标题：由选中文档反查，与 id 一一对应拼分号
const buildPayload = () => {
  const idList = form.expectedDocIds.filter(Boolean);
  const idSet = new Set(idList);
  const titles = docOptions.value.filter((d) => idSet.has(d.id)).map((d) => d.title);
  return {
    id: form.id || undefined,
    question: form.question.trim(),
    expectedDocIds: idList.join(';'),
    expectedDocTitles: titles.join(';'),
    remark: form.remark?.trim() || undefined,
  };
};

const save = async () => {
  if (!form.question.trim()) {
    ElMessage.warning('评测问题不能为空');
    return;
  }
  if (!form.expectedDocIds.length) {
    ElMessage.warning('请选择期望命中的知识文档');
    return;
  }
  saving.value = true;
  try {
    await upsertKnowledgeEvalCase(buildPayload());
    ElMessage.success(form.id ? '已保存' : '已新增');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存评测用例失败', e);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
