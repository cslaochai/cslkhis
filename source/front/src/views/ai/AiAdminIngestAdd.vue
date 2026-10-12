<template>
  <el-dialog v-model="visible" title="录入知识文档" width="640px">
    <el-form label-width="80px">
      <el-form-item label="标题" required>
        <el-input v-model="ingestForm.title" maxlength="100" placeholder="文档标题"/>
      </el-form-item>
      <el-form-item label="分类">
        <el-select
            v-model="ingestForm.category"
            :fit-input-width="false"
            allow-create
            clearable
            default-first-option
            filterable
            placeholder="选择或输入分类"
            style="width: 100%"
        >
          <el-option label="就诊须知" value="就诊须知"/>
          <el-option label="科室介绍" value="科室介绍"/>
          <el-option label="检查注意事项" value="检查注意事项"/>
          <el-option label="药品说明书" value="药品说明书"/>
        </el-select>
      </el-form-item>
      <el-form-item label="来源">
        <el-radio-group v-model="ingestForm.sourceType">
          <el-radio :value="2">手工录入</el-radio>
          <el-radio :value="3">文件导入</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input
            v-model="ingestForm.content"
            :rows="12"
            maxlength="20000"
            placeholder="文档全文；保存后自动切块并建立向量索引"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="ingestSubmitting" type="primary" @click="submitIngest">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 知识文档「录入」弹框（自 AiAdminView 拆出）
 *
 * 只有新增语义（后端 ingest 接口即建库+切块+建索引，页面无修改入口），故用 Add 而不是 Edit；
 * 表单 state / 校验 / 提交都留在这里，保存成功 emit('saved') 由页面刷新列表。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {ingestKnowledgeDoc} from '@/api/ai';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const ingestForm = reactive({
  title: '',
  category: '',
  content: '',
  sourceType: 2,
});
const ingestSubmitting = ref(false);

// 每次打开重置表单（与拆分前 openIngest 的重置口径一致）
watch(() => props.modelValue, (v) => {
  if (!v) return;
  ingestForm.title = '';
  ingestForm.category = '';
  ingestForm.content = '';
  ingestForm.sourceType = 2;
});

const submitIngest = async () => {
  if (!ingestForm.title.trim() || !ingestForm.content.trim()) {
    ElMessage.warning('标题与内容不能为空');
    return;
  }
  ingestSubmitting.value = true;
  try {
    await ingestKnowledgeDoc({...ingestForm});
    ElMessage.success('已录入并建立索引');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('录入知识文档失败', e);
  } finally {
    ingestSubmitting.value = false;
  }
};
</script>
