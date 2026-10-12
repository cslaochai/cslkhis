<template>
  <!-- 答卷只读详情（含逐题快照） -->
  <el-dialog v-model="visible" title="答卷详情" top="8vh" width="720px">
    <div v-loading="anDetailLoading">
      <el-form v-if="anDetail" disabled label-width="100px">
        <el-form-item label="答卷号">
          <el-input :model-value="anDetail.answerNo"/>
        </el-form-item>
        <el-form-item label="问卷">
          <el-input :model-value="anDetail.templateName"/>
        </el-form-item>
        <el-form-item label="患者">
          <el-input :model-value="`${anDetail.patientName || ''} ${anDetail.patientNo || ''}`"/>
        </el-form-item>
        <el-form-item label="科室">
          <el-input :model-value="anDetail.deptName || '—'"/>
        </el-form-item>
        <el-form-item label="得分">
          <el-input
              :model-value="`均分 ${anDetail.avgScore ?? '—'} ｜ 百分制 ${anDetail.score100 ?? '—'} ｜ NPS ${anDetail.nps ?? '—'}`"/>
        </el-form-item>
        <el-form-item label="填报">
          <el-input
              :model-value="`${fillSourceText(anDetail.fillSource)} ${anDetail.fillEmployeeName || ''} ${fmtTime(anDetail.fillTime)}`"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-input
              :model-value="answerStatusText(anDetail.answerStatus) + (anDetail.disputeCaseId ? '（已自动转投诉）' : '')"/>
        </el-form-item>
        <el-form-item v-if="anDetail.commentText" label="患者留言">
          <el-input :model-value="anDetail.commentText" :rows="3" type="textarea"/>
        </el-form-item>
      </el-form>
      <div class="block-title">逐题答案（题目快照，改模板不影响这张历史卷）</div>
      <el-table :data="anDetail?.items || []" border max-height="280" size="small" stripe>
        <el-table-column label="题号" prop="seqNo" width="60"/>
        <el-table-column label="维度" prop="dimension" width="110">
          <template #default="{ row }">{{ dimensionText(row.dimension) }}</template>
        </el-table-column>
        <el-table-column label="题干" min-width="200" prop="title" show-overflow-tooltip/>
        <el-table-column label="作答" width="150">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined">{{
                row.score
              }} 分{{ Number(row.questionType) === 1 ? `（${likertLabel(row.score)}）` : '' }}</span>
            <span v-else-if="row.optionLabel">{{ row.optionLabel }}</span>
            <span v-else-if="row.textValue" class="text-xs">{{ row.textValue }}</span>
            <span v-else class="text-gray-400">未作答</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 答卷详情（只读）：按 id 自取 getAnswerDetail —— 逐题答案是列表行里没有的快照，
 * 所以这张卷「当时问的是哪题、答了什么」只能从详情接口出。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAnswerDetail} from '@/api/survey';
import {dictLabelText} from '@/lib/utils';
import {likertLabel} from '@/lib/surveyScale';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 维度/填报方式/状态文案与列表同一份字典，由页面加载一次传下来
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmtTime = (v) => (v ? String(v).slice(0, 16).replace('T', ' ') : '—');
const dimensionText = (v) => dictLabelText(props.dicts.dimension, v);
const answerStatusText = (v) => dictLabelText(props.dicts.answerStatus, v);
const fillSourceText = (v) => dictLabelText(props.dicts.fillSource, v);

const anDetailLoading = ref(false);
const anDetail = ref(null);

const loadDetail = async () => {
  anDetailLoading.value = true;
  anDetail.value = null;
  try {
    const res = await getAnswerDetail(props.id);
    if (res.code === 200)
      anDetail.value = res.data;
    else
      ElMessage.error(res.message || '加载答卷失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载答卷失败');
  } finally {
    anDetailLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open || !props.id) return;
  await loadDetail();
});
</script>

<style scoped>
.block-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
</style>
