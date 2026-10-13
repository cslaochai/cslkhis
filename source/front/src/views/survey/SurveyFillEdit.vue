<template>
  <!-- 答卷录入（电话代填） -->
  <el-dialog v-model="visible" title="满意度答卷录入" top="6vh" width="780px">
    <div v-loading="fillLoading">
      <div v-if="fillDispatch" class="fill-head">
        <div class="fill-patient">
          {{ fillDispatch.patientName }}
          <span class="text-gray-400 text-xs">{{ fillDispatch.patientNo }}</span>
          <el-tag class="ml-2" size="small" type="info">{{ fillDispatch.deptName || '未归科' }}</el-tag>
        </div>
        <div class="fill-phone">联系电话：{{ fillDispatch.phone || fillDispatch.phoneMasked }}</div>
        <div class="text-xs text-gray-400">
          问卷：{{ fillDispatch.templateName }} ｜ 截止 {{ fmtTime(fillDispatch.expireTime) }}
        </div>
      </div>

      <el-form v-if="fillPaper.length" class="fill-items" label-width="46px">
        <div v-for="it in fillPaper" :key="it.id" class="fill-item">
          <div class="fill-title">
            <span class="seq">{{ it.seqNo }}.</span>
            <span>{{ it.title }}</span>
            <el-tag v-if="Number(it.required) === 1" class="ml-1" size="small" type="danger">必答</el-tag>
            <span class="dim">{{ dimensionText(it.dimension) }} · {{ questionTypeText(it.questionType) }}</span>
          </div>
          <!-- 1-量表：5 级李克特，文案来自 lib/surveyScale（不是字典，见其注释） -->
          <el-radio-group v-if="Number(it.questionType) === 1" v-model="fillAnswers[it.id].score">
            <el-radio v-for="s in [1,2,3,4,5]" :key="s" :value="s">{{ s }} {{ LIKERT_LABELS[s] }}</el-radio>
          </el-radio-group>
          <!-- 4-NPS：0-10 单列，不参与百分制加权 -->
          <div v-else-if="Number(it.questionType) === 4" class="nps-row">
            <el-select v-model="fillAnswers[it.id].score" :fit-input-width="false" placeholder="推荐度"
                       style="width: 120px">
              <el-option v-for="n in npsOptions" :key="n" :label="String(n)" :value="n"/>
            </el-select>
            <span class="text-xs text-gray-400 ml-2">
              {{
                fillAnswers[it.id].score === null || fillAnswers[it.id].score === '' ? '0=完全不推荐，10=强烈推荐' : npsBand(fillAnswers[it.id].score)
              }}
            </span>
          </div>
          <!-- 5-开放文本 -->
          <el-input v-else-if="Number(it.questionType) === 5" v-model="fillAnswers[it.id].textValue"
                    :rows="2" placeholder="患者原话（不要替患者总结）" type="textarea"/>
          <!-- 2/3-单选多选：表结构里没有选项集，按患者所选文本记录 -->
          <el-input v-else v-model="fillAnswers[it.id].optionLabel"
                    placeholder="选项内容（本卷未定义选项集，按患者所选文本记录）"/>
        </div>

        <el-form-item class="mt-3" label="方式">
          <el-select v-model="fillForm.fillSource" :fit-input-width="false" style="width: 160px">
            <el-option :value="2" label="随访员代填"/>
            <el-option :value="1" label="患者自填"/>
            <el-option :value="3" label="现场扫码"/>
          </el-select>
          <el-checkbox v-model="fillForm.anonymousFlag" :false-value="0" :true-value="1" class="ml-3">匿名
          </el-checkbox>
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="fillForm.commentText" :rows="2" placeholder="患者补充意见（原文）" type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="fillForm.remark" :rows="2" placeholder="录入备注（接通情况等）" type="textarea"/>
        </el-form-item>
      </el-form>
      <el-empty v-else-if="!fillLoading" description="该问卷没有题目，请先在「问卷模板」里配置"/>
    </div>
    <template #footer>
      <span class="foot-hint text-xs text-gray-400">
        {{ unansweredRequired.length ? `还有 ${unansweredRequired.length} 道必答题未作答` : '必答题已全部作答' }}
      </span>
      <el-button :disabled="!fillPaper.length" :loading="fillBusy" type="primary" @click="submitFill">提交回收
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 满意度答卷录入（电话代填）：发放单 + 当前问卷题目都在这里自取，
 * 提交 submitAnswer 与「还有 N 道必答题」的校验也都在这里 —— 页面只管打开与刷新。
 *
 * 明文手机号只用于顶部给随访员照着拨号，不写回任何表单再提交（见页面五条口径第 4 条）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDispatchDetail, getTemplateDetail, submitAnswer} from '@/api/survey';
import {dictLabelText} from '@/lib/utils';
import {LIKERT_LABELS, NPS_MAX, NPS_MIN, npsBand} from '@/lib/surveyScale';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 维度/题型文案与列表同一份字典，由页面加载一次传下来
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmtTime = (v) => (v ? String(v).slice(0, 16).replace('T', ' ') : '—');
const dimensionText = (v) => dictLabelText(props.dicts.dimension, v);
const questionTypeText = (v) => dictLabelText(props.dicts.questionType, v);

const fillLoading = ref(false);
const fillBusy = ref(false);
const fillDispatch = ref(null);
const fillPaper = ref([]);
const fillForm = reactive({fillSource: 2, anonymousFlag: 0, commentText: '', remark: ''});
/** itemId -> { score, optionLabel, textValue } */
const fillAnswers = reactive({});
const npsOptions = computed(() => {
  const out = [];
  for (let i = NPS_MIN; i <= NPS_MAX; i++)
    out.push(i);
  return out;
});

/**
 * 打开录入：并发取「发放单（含明文电话，供照着拨号）」与「当前问卷题目」。
 * 题目取的是模板**当前**版本 —— 已发放未回收的卷子按新题作答，
 * 历史答卷有逐题快照保护，不会被改题篡改。
 */
const loadPaper = async () => {
  fillLoading.value = true;
  fillDispatch.value = null;
  fillPaper.value = [];
  Object.keys(fillAnswers).forEach(k => delete fillAnswers[k]);
  Object.assign(fillForm, {fillSource: 2, anonymousFlag: 0, commentText: '', remark: ''});
  try {
    const [d, t] = await Promise.all([getDispatchDetail(props.row.id), getTemplateDetail(props.row.templateId)]);
    if (d.code !== 200) {
      ElMessage.error(d.message || '加载发放单失败');
      return;
    }
    if (t.code !== 200) {
      ElMessage.error(t.message || '加载问卷失败');
      return;
    }
    fillDispatch.value = d.data;
    fillPaper.value = (t.data?.items || []).slice().sort((x, y) => Number(x.seqNo) - Number(y.seqNo));
    fillPaper.value.forEach(it => {
      fillAnswers[it.id] = {score: null, optionLabel: '', textValue: ''};
    });
  } catch (e) {
    console.error(e);
    ElMessage.error('加载失败');
  } finally {
    fillLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open || !props.row?.id) return;
  await loadPaper();
});

const unansweredRequired = computed(() => fillPaper.value.filter(it => {
  if (Number(it.required) !== 1)
    return false;
  const a = fillAnswers[it.id] || {};
  if (Number(it.questionType) === 5)
    return !String(a.textValue || '').trim();
  if (Number(it.questionType) === 2 || Number(it.questionType) === 3)
    return !String(a.optionLabel || '').trim();
  return a.score === null || a.score === undefined || a.score === '';
}));
const submitFill = async () => {
  if (unansweredRequired.value.length) {
    ElMessage.warning(`还有 ${unansweredRequired.value.length} 道必答题未作答（第 ${unansweredRequired.value.map(i => i.seqNo).join('、')} 题）`);
    return;
  }
  const items = fillPaper.value.map(it => {
    const a = fillAnswers[it.id] || {};
    return {
      itemId: it.id,
      score: a.score === null || a.score === '' || a.score === undefined ? undefined : Number(a.score),
      optionLabel: a.optionLabel ? String(a.optionLabel).trim() : undefined,
      textValue: a.textValue ? String(a.textValue).trim() : undefined,
    };
  }).filter(i => i.score !== undefined || i.optionLabel || i.textValue);
  fillBusy.value = true;
  try {
    const res = await submitAnswer({
      dispatchId: fillDispatch.value.id,
      fillSource: fillForm.fillSource,
      anonymousFlag: fillForm.anonymousFlag,
      commentText: fillForm.commentText || undefined,
      remark: fillForm.remark || undefined,
      items,
    });
    if (res.code === 200) {
      // 是否转投诉由后端判，这里只把它已经做的事说清楚，不重复判阈值
      ElMessage.success(res.data?.disputeCaseId
          ? `已回收：总分 ${res.data?.score100} 分，低分已自动登记投诉单，请医务/质控岗跟进受理`
          : `已回收：总分 ${res.data?.score100} 分（均分 ${res.data?.avgScore}）`);
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '提交失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('提交失败');
  } finally {
    fillBusy.value = false;
  }
};
</script>

<style scoped>
.fill-head {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #f8fafc;
}

.fill-patient {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.fill-phone {
  font-size: 13px;
  color: #1269b5;
  margin-top: 2px;
}

.fill-items {
  margin-top: 4px;
}

.fill-item {
  padding: 8px 0;
  border-bottom: 1px dashed #eef2f7;
}

.fill-title {
  margin-bottom: 6px;
  color: #303133;
}

.fill-title .seq {
  font-weight: 600;
  margin-right: 4px;
}

.fill-title .dim {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.nps-row {
  display: flex;
  align-items: center;
}

.foot-hint {
  float: left;
  line-height: 32px;
}
</style>
