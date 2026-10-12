<template>
  <!-- 问卷模板编辑（新建 / 编辑同一张卷，整卷覆盖） -->
  <el-dialog v-model="visible" :title="tpForm.id ? '编辑问卷' : '新建问卷'" top="5vh" width="920px">
    <div v-loading="tpEditLoading">
      <el-form label-width="90px">
        <el-form-item label="问卷名称" required>
          <el-input v-model="tpForm.templateName" placeholder="如：住院出院随访满意度调查表" style="width: 360px"/>
        </el-form-item>
        <el-form-item label="适用场景" required>
          <el-select v-model="tpForm.scene" :fit-input-width="false" style="width: 200px">
            <el-option v-for="d in dicts.scene" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
          <span class="hint">同一场景只会启用一张卷</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="tpForm.status" :fit-input-width="false" style="width: 140px">
            <el-option v-for="d in dicts.tplStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="tpForm.description" :rows="2" placeholder="适用范围与填卷说明" type="textarea"/>
        </el-form-item>
      </el-form>

      <div class="block-title">题目清单（整卷覆盖：保存即以此为准）</div>
      <el-table :data="tpItems" border max-height="360" size="small">
        <el-table-column label="题号" prop="seqNo" width="60"/>
        <el-table-column label="维度" width="140">
          <template #default="{ row }">
            <el-select v-model="row.dimension" :fit-input-width="false" style="width: 120px">
              <el-option v-for="d in dicts.dimension" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="题型" width="120">
          <template #default="{ row }">
            <el-select v-model="row.questionType" :fit-input-width="false" style="width: 100px"
                       @change="onQuestionTypeChange(row)">
              <el-option
                  v-for="d in dicts.questionType.filter(x => EDITABLE_QUESTION_TYPES.includes(Number(x.dictValue)))"
                  :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="题干" min-width="240">
          <template #default="{ row }">
            <el-input v-model="row.title" placeholder="题干"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="必答" width="70">
          <template #default="{ row }">
            <el-checkbox v-model="row.required" :false-value="0" :true-value="1"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="权重" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.weight" :disabled="Number(row.questionType) === 5" :max="10" :min="0" :precision="2" :step="0.1"
                             controls-position="right" size="small" style="width: 86px"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作" width="70">
          <template #default="{ $index }">
            <el-button :icon="Delete" link size="small" type="danger" @click="removeTemplateItem($index)"/>
          </template>
        </el-table-column>
      </el-table>
      <el-button :icon="Plus" class="mt-2" @click="addTemplateItem">添加题目</el-button>
      <div class="text-xs text-gray-400 mt-2">
        百分制只算量表题（按权重加权，均分 →（x−1）/4×100）；NPS 单列算净推荐值；开放文本权重固定 0，不参与打分。
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="tpEditBusy" type="primary" @click="submitTemplate">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 问卷模板编辑：新建与编辑共用一份表单（后端 templateUpsert 是同一个整卷覆盖入口，
 * row 为空 = 新建，带 id = 编辑），题目清单也在组件里自取 getTemplateDetail。
 * 启停（只改 status）仍走页面行操作 —— 它要先读回原题目再原样提交，不是编辑表单。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {getTemplateDetail, templateUpsert} from '@/api/survey';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 场景/状态/维度/题型字典与列表同一份，由页面加载一次传下来
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const tpEditLoading = ref(false);
const tpEditBusy = ref(false);
const tpForm = reactive({
  id: null, templateName: '', scene: 1, status: 1, description: '', remark: '',
});
const tpItems = ref([]);
/**
 * 题型只放能算分的三种：量表 / NPS / 开放文本。
 * 单选与多选要能落地，得先给题目加「选项集」这一列（当前表结构没有），
 * 与其让录入员对着无选项的单选题自由发挥，不如不在编辑器里开这个口子。
 */
const EDITABLE_QUESTION_TYPES = [1, 4, 5];

const addTemplateItem = () => {
  tpItems.value.push({
    seqNo: tpItems.value.length + 1, dimension: 7, questionType: 1, title: '',
    required: 1, weight: 1, maxScore: 5,
  });
};
const removeTemplateItem = (index) => {
  tpItems.value.splice(index, 1);
  tpItems.value.forEach((it, i) => {
    it.seqNo = i + 1;
  });
};
const onQuestionTypeChange = (it) => {
  // 满分跟着题型走：量表 5、NPS 10、开放文本不计分（权重 0）
  if (Number(it.questionType) === 4) {
    it.maxScore = 10;
    it.weight = 1;
  } else if (Number(it.questionType) === 5) {
    it.maxScore = 5;
    it.weight = 0;
    it.required = 0;
  } else {
    it.maxScore = 5;
    it.weight = Number(it.weight) || 1;
  }
};

const loadTemplate = async () => {
  tpEditLoading.value = true;
  try {
    const res = await getTemplateDetail(props.row.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载问卷失败');
      return;
    }
    const t = res.data;
    Object.assign(tpForm, {
      id: t.id, templateName: t.templateName, scene: t.scene, status: t.status,
      description: t.description || '', remark: t.remark || '',
    });
    tpItems.value = (t.items || []).map((i) => ({
      seqNo: i.seqNo,
      dimension: i.dimension,
      questionType: i.questionType,
      title: i.title,
      required: Number(i.required),
      weight: Number(i.weight ?? 1),
      maxScore: Number(i.maxScore ?? (i.questionType === 4 ? 10 : 5)),
    }));
  } catch (e) {
    console.error(e);
    ElMessage.error('加载问卷失败');
  } finally {
    tpEditLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open) return;
  Object.assign(tpForm, {id: null, templateName: '', scene: 1, status: 1, description: '', remark: ''});
  tpItems.value = [];
  if (!props.row?.id) {
    addTemplateItem();
    return;
  }
  await loadTemplate();
});

const submitTemplate = async () => {
  if (!tpForm.templateName.trim()) {
    ElMessage.warning('问卷名称必填');
    return;
  }
  if (!tpItems.value.length) {
    ElMessage.warning('至少一道题');
    return;
  }
  if (tpItems.value.some(i => !String(i.title).trim())) {
    ElMessage.warning('存在题干为空的题目');
    return;
  }
  tpEditBusy.value = true;
  try {
    const res = await templateUpsert({
      id: tpForm.id ?? undefined,
      templateName: tpForm.templateName.trim(),
      scene: tpForm.scene,
      status: tpForm.status,
      description: tpForm.description || undefined,
      remark: tpForm.remark || undefined,
      items: tpItems.value.map(i => ({
        seqNo: i.seqNo, dimension: i.dimension, questionType: i.questionType,
        title: String(i.title).trim(), required: i.required,
        weight: i.questionType === 5 ? 0 : Number(i.weight) || 0, maxScore: i.maxScore,
      })),
    });
    if (res.code === 200) {
      ElMessage.success('问卷已保存（整卷覆盖：题目以本次提交为准）');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    tpEditBusy.value = false;
  }
};
</script>

<style scoped>
.block-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
