<template>
  <!-- 入径详情弹窗（工作区） -->
  <el-dialog v-model="visible" :title="`入径 ${detail?.enrollNo || ''}`" data-testid="cp-enroll-detail-dialog"
             width="1080px">
    <div v-if="detail" v-loading="detailLoading" class="space-y-3">
      <div class="flex items-center gap-5 text-sm text-slate-600 flex-wrap">
        <span>路径：<b class="text-slate-800">{{ detail.pathwayName }} {{ detail.version }}</b>（共 {{
            detail.totalDays
          }} 天）</span>
        <span>患者：<b class="text-slate-800">{{ detail.patientName }}</b>（{{ detail.patientNo }}）</span>
        <span>科室：{{ detail.deptName || '—' }}</span>
        <span>入径日期：<b class="text-slate-800">{{ detail.enrollDate }}</b></span>
        <span>当前路径日：<b class="text-lg font-semibold text-sky-700">{{ detail.currentDay }}</b></span>
        <span>状态：<el-tag :type="enrollStatusTag(detail.status)" size="small">{{
            enrollStatusText(detail.status)
          }}</el-tag></span>
        <span class="ml-auto flex gap-2">
          <el-button v-if="canVariance(detail.status)" v-perm="'qc:clinicalPath:edit'" :icon="Plus" data-testid="cp-variance-btn"
                     plain size="small"
                     type="warning" @click="openVariance">登记变异</el-button>
          <el-button v-if="canFinish(detail.status)" v-perm="'qc:clinicalPath:edit'" :loading="actLoading" data-testid="cp-finish-btn" plain
                     size="small"
                     type="success" @click="onFinish">完成</el-button>
          <el-button v-if="canAbort(detail.status)" v-perm="'qc:clinicalPath:edit'" :icon="CircleClose" :loading="actLoading" data-testid="cp-abort-btn"
                     plain
                     size="small" type="danger" @click="onAbort">退径</el-button>
        </span>
      </div>
      <div v-if="detail.diagnosis" class="text-xs text-slate-500">入院诊断：{{ detail.diagnosis }}（入径依据）</div>

      <el-table :data="detail.steps || []" :row-class-name="({ row }) => (detail.status === ENROLL_STATUS.ENROLLED && row.dayNo === detail.currentDay ? 'bg-amber-50' : '')" border data-testid="cp-detail-steps" max-height="320"
                size="small">
        <el-table-column align="center" label="路径日" prop="dayNo" width="80"/>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
        </el-table-column>
        <el-table-column label="项目" min-width="160" prop="itemName"/>
        <el-table-column label="路径要求" min-width="240" prop="content" show-overflow-tooltip/>
      </el-table>

      <div>
        <h4 class="font-semibold text-slate-800 mb-2">变异台账（{{ (detail.variances || []).length }} 条）</h4>
        <el-table :data="detail.variances || []" border data-testid="cp-variance-table" max-height="240" size="small">
          <el-table-column align="center" label="路径日" prop="dayNo" width="80"/>
          <el-table-column label="类型" width="130">
            <template #default="{ row }">
              <el-tag size="small" type="warning">{{ varianceTypeText(row.varianceType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="变异原因" min-width="200" prop="varianceReason" show-overflow-tooltip/>
          <el-table-column label="处理措施" min-width="160" prop="handling" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handling || '—' }}</template>
          </el-table-column>
          <el-table-column label="发生日期" prop="occurredDate" width="105"/>
          <el-table-column label="登记人" prop="recorderName" width="90"/>
        </el-table>
        <div v-if="!(detail.variances || []).length" class="py-2 text-sm text-slate-400">暂无变异记录</div>
      </div>
      <div v-if="detail.abortReason" class="text-xs text-red-600">退径留痕：{{ detail.abortReason }}（{{
          detail.finishBy
        }} {{ detail.finishDate }}）
      </div>
      <div class="text-xs text-slate-400">
        说明：路径步骤为文书记录，不生成医嘱、不计费；变异不挡完成，偏离大走退径。
      </div>
    </div>
  </el-dialog>

  <!-- 登记变异（与详情弹框并列的子弹框，保存后重取详情刷新变异台账） -->
  <ClinicalPathVarianceEdit v-model="varVisible" :default-day-no="detail?.currentDay" :enroll-id="detail?.id"
                            :max-days="detail?.totalDays" :variance-type-dict="varianceTypeDict"
                            @saved="onVarianceSaved"/>
</template>

<script setup>
/**
 * 入径详情工作区：步骤执行到哪一天、变异台账、完成/退径都在这里，
 * 所以组件按 id 自取 getEnrollDetail，动作成功后重取本详情并通知页面刷新台账。
 * 完成/退径仍用 ElMessageBox 确认（口径不变：确认弹框不拆组件）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {CircleClose, Plus} from '@element-plus/icons-vue';
import {abortEnroll, finishEnroll, getEnrollDetail} from '@/api/pathway';
import {dictLabelText} from '@/lib/utils';
import {
  canAbort,
  canFinish,
  canVariance,
  ENROLL_STATUS,
  enrollStatusTag,
} from '@/lib/pathway';
import ClinicalPathVarianceEdit from './ClinicalPathVarianceEdit.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 状态/项目类型/变异类型文案与列表同一份字典，由页面加载一次传下来
  enrollStatusDict: {type: Array, default: () => []},
  itemTypeDict: {type: Array, default: () => []},
  varianceTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved', 'changed']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const enrollStatusText = (v) => dictLabelText(props.enrollStatusDict, v);
const itemTypeText = (v) => dictLabelText(props.itemTypeDict, v);
const varianceTypeText = (v) => dictLabelText(props.varianceTypeDict, v);

const detailLoading = ref(false);
const actLoading = ref(false);
const detail = ref(null);

const loadDetail = async () => {
  detailLoading.value = true;
  try {
    const res = await getEnrollDetail(props.id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '查询入径详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询入径详情失败');
  } finally {
    detailLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  await loadDetail();
});

// ---- 登记变异 ----
const varVisible = ref(false);
const openVariance = () => {
  if (!detail.value?.id)
    return;
  varVisible.value = true;
};
const onVarianceSaved = async () => {
  await loadDetail();
  emit('saved');
};

const onFinish = async () => {
  if (!detail.value?.id)
    return;
  try {
    await ElMessageBox.confirm('确认该患者完成临床路径？有变异记录不挡完成（完成=按计划走完或临床认可结局）。', '路径完成', {
      confirmButtonText: '确认完成',
      cancelButtonText: '取消'
    });
    actLoading.value = true;
    const res = await finishEnroll({id: detail.value.id});
    if (res.code === 200) {
      ElMessage.success('已完成');
      await loadDetail();
      emit('changed');
    } else {
      ElMessage.error(res.message || '完成失败');
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  } finally {
    actLoading.value = false;
  }
};
const onAbort = async () => {
  if (!detail.value?.id)
    return;
  try {
    const {value} = await ElMessageBox.prompt('确认退出临床路径？退径后转非路径管理，该住院记录可重新入径。', '退径原因（必填）', {
      confirmButtonText: '确认退径',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '退径原因必填'
    });
    actLoading.value = true;
    const res = await abortEnroll({id: detail.value.id, reason: value.trim()});
    if (res.code === 200) {
      ElMessage.success('已退径');
      await loadDetail();
      emit('changed');
    } else {
      ElMessage.error(res.message || '退径失败');
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  } finally {
    actLoading.value = false;
  }
};
</script>
