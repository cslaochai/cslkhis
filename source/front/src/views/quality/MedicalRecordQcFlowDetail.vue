<template>
  <el-drawer v-model="visible" data-testid="g18-drawer" size="560px" title="三级质控流转详情">
    <div v-loading="flowDetailLoading">
      <template v-if="flowDetail">
        <div class="grid grid-cols-2 gap-2 text-sm">
          <div><span class="text-slate-400">流转单号：</span>{{ flowDetail.flowNo }}</div>
          <div><span class="text-slate-400">状态：</span>
            <el-tag :type="flowStatusTag(flowDetail.flowStatus)" data-testid="g18-drawer-status" size="small">
              {{ flowStatusText(flowDetail.flowStatus) }}
            </el-tag>
          </div>
          <div><span class="text-slate-400">患者：</span>{{ flowDetail.patientName }}</div>
          <div><span class="text-slate-400">科室：</span>{{ flowDetail.deptName || '—' }}</div>
          <div><span class="text-slate-400">当前级：</span>{{ flowDetail.currentLevelText }}</div>
          <div><span class="text-slate-400">终审定级：</span>{{ flowGradeText(flowDetail.grade) }}<span
              v-if="flowDetail.finalScore !== null && flowDetail.finalScore !== undefined">（{{
              flowDetail.finalScore
            }} 分）</span></div>
        </div>
        <div v-if="flowDetail.flowStatus === 5"
             class="mt-3 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-700"
             data-testid="g18-rework-box">
          <p class="font-medium">整改中（{{ flowDetail.returnLevelText }}退回）</p>
          <p class="mt-1">缺陷：{{ flowDetail.returnReason }}</p>
          <p class="mt-1">要求：{{ flowDetail.returnRequirement }}<span
              v-if="flowDetail.returnDeadline">（期限 {{ flowDetail.returnDeadline }}）</span></p>
        </div>

        <!-- 分级操作：按钮显隐按 flowStatus / currentLevel，状态判断在后端 -->
        <div class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3">
          <el-input v-model="flowOpinion" data-testid="g18-opinion" placeholder="审核 / 整改意见（可空）"/>
          <template v-if="flowDetail.flowStatus === 1 || flowDetail.flowStatus === 2">
            <div class="mt-2 flex gap-2">
              <el-button v-perm="'qc:recordQc:edit'" :loading="flowBusy" data-testid="g18-approve" size="small"
                         type="primary" @click="doApprove">
                通过，送{{ flowDetail.currentLevel === 1 ? '病案室' : '医务处' }}
              </el-button>
            </div>
            <el-input v-model="flowDefect" :rows="2" class="mt-2" data-testid="g18-defect" placeholder="缺陷明细（退回必填）"
                      type="textarea"/>
            <el-input v-model="flowRequirement" class="mt-2" data-testid="g18-requirement"
                      placeholder="整改要求（退回必填）"/>
            <div class="mt-2 flex items-center gap-2">
              <el-date-picker v-model="flowDeadline" placeholder="整改期限（可空）" style="width: 180px"
                              type="date" value-format="YYYY-MM-DD"/>
              <el-button v-perm="'qc:recordQc:edit'" :loading="flowBusy" data-testid="g18-return" size="small"
                         type="warning" @click="doReturn">退回整改
              </el-button>
            </div>
          </template>
          <template v-else-if="flowDetail.flowStatus === 5">
            <el-button v-perm="'qc:recordQc:edit'" :loading="flowBusy" class="mt-2" data-testid="g18-resubmit" size="small"
                       type="primary" @click="doResubmit">整改完成，提交{{ flowDetail.currentLevelText }}
            </el-button>
          </template>
          <template v-else-if="flowDetail.flowStatus === 3">
            <div class="mt-2 flex items-center gap-2">
              <el-select v-model="flowGrade" data-testid="g18-grade" placeholder="定级（必填）" style="width: 130px">
                <el-option :value="1" label="甲级"/>
                <el-option :value="2" label="乙级"/>
                <el-option :value="3" label="丙级"/>
              </el-select>
              <el-input-number v-model="flowScore" :max="100" :min="0" placeholder="评分" style="width: 120px"/>
            </div>
            <el-button v-perm="'qc:recordQc:edit'" :loading="flowBusy" class="mt-2" data-testid="g18-final" size="small"
                       type="primary" @click="doFinal">终审通过（终态）
            </el-button>
          </template>
          <div v-else-if="flowDetail.flowStatus === 4" class="mt-2 text-xs text-slate-500">
            已终审通过（{{ flowGradeText(flowDetail.grade) }}）——终态，不可再操作。
          </div>
        </div>

        <!-- 时间线 -->
        <p class="mt-4 mb-2 text-sm font-medium text-slate-700">流转时间线</p>
        <el-timeline>
          <el-timeline-item v-for="a in flowActions" :key="a.id" :timestamp="a.actionTime" placement="top">
            <p class="text-sm"><span class="font-medium text-slate-700">{{ a.actionText }}</span><span
                class="ml-1 text-xs text-slate-400">{{ a.levelText }} · {{ a.operatorName }}</span></p>
            <p v-if="a.opinion" class="text-xs text-slate-500">{{ a.opinion }}</p>
            <p v-if="a.defectDetail" class="text-xs text-amber-600">缺陷：{{ a.defectDetail }}</p>
            <p v-if="a.requirement" class="text-xs text-amber-600">要求：{{ a.requirement }}</p>
          </el-timeline-item>
        </el-timeline>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
// 三级质控流转详情抽屉：按 id 自取数（时间线 + 分级操作），操作成功后 emit('saved') 由页面关抽屉、刷新并重开
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {
  approveQcFlow,
  finalQcFlow,
  getQcFlowActions,
  getQcFlowDetail,
  resubmitQcFlow,
  returnQcFlow,
} from '@/api/qualityControl';
import {flowGradeText, flowStatusTag, flowStatusText} from '@/lib/recordQcFlow';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [Number, String], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const flowDetailLoading = ref(false);
const flowDetail = ref(null);
const flowActions = ref([]);
const flowBusy = ref(false);
const flowOpinion = ref('');
const flowDefect = ref('');
const flowRequirement = ref('');
const flowDeadline = ref('');
const flowGrade = ref(undefined);
const flowScore = ref(undefined);

const load = async () => {
  flowDetailLoading.value = true;
  flowOpinion.value = '';
  flowDefect.value = '';
  flowRequirement.value = '';
  flowDeadline.value = '';
  flowGrade.value = undefined;
  flowScore.value = undefined;
  try {
    const [d, a] = await Promise.all([getQcFlowDetail(props.id), getQcFlowActions(props.id)]);
    flowDetail.value = d.data;
    flowActions.value = a.data || [];
  } catch (e) {
    ElMessage.error(e?.message || '加载流转详情失败');
  } finally {
    flowDetailLoading.value = false;
  }
};

watch(visible, (open) => {
  if (open && props.id)
    load();
});

const doApprove = async () => {
  flowBusy.value = true;
  try {
    await approveQcFlow({flowId: flowDetail.value.id, opinion: flowOpinion.value || undefined});
    ElMessage.success('审核通过，已送下一级');
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '审核失败');
  } finally {
    flowBusy.value = false;
  }
};
const doReturn = async () => {
  if (!flowDefect.value.trim() || !flowRequirement.value.trim()) {
    ElMessage.warning('缺陷明细与整改要求必填——退回必须写清缺陷');
    return;
  }
  flowBusy.value = true;
  try {
    await returnQcFlow({
      flowId: flowDetail.value.id,
      defectDetail: flowDefect.value,
      requirement: flowRequirement.value,
      returnDeadline: flowDeadline.value || undefined,
      opinion: flowOpinion.value || undefined,
    });
    ElMessage.success('已退回整改');
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '退回失败');
  } finally {
    flowBusy.value = false;
  }
};
const doResubmit = async () => {
  flowBusy.value = true;
  try {
    await resubmitQcFlow({flowId: flowDetail.value.id, opinion: flowOpinion.value || undefined});
    ElMessage.success('整改已提交');
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '提交失败');
  } finally {
    flowBusy.value = false;
  }
};
const doFinal = async () => {
  if (!flowGrade.value) {
    ElMessage.warning('终审必须定级（甲/乙/丙）');
    return;
  }
  flowBusy.value = true;
  try {
    await finalQcFlow({
      flowId: flowDetail.value.id,
      grade: flowGrade.value,
      finalScore: flowScore.value ?? undefined,
      finalOpinion: flowOpinion.value || undefined,
    });
    ElMessage.success('终审通过');
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '终审失败');
  } finally {
    flowBusy.value = false;
  }
};
</script>
