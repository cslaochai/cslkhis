<template>
  <el-drawer v-model="visible" size="62%" title="质控单明细">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <div class="flex flex-wrap items-center gap-4 text-sm">
          <span>质控单：<b>{{ detail.qcNo }}</b></span>
          <span>病历：<b>{{ detail.recordNo || '—' }}</b>（{{
              detail.recordSourceText
            }}{{ detail.recordTypeText ? ' · ' + detail.recordTypeText : '' }}）</span>
          <span>患者：<b>{{ detail.patientName || '—' }}</b></span>
          <span data-testid="p5-qc-detail-score">得分：<b>{{ scoreText(detail.score) }}</b></span>
          <span>等级：<b>{{ detail.gradeText || '—' }}</b></span>
          <span :class="detail.qcResult === 1 ? 'text-emerald-600' : 'text-red-600'">
            结论：<b>{{ detail.qcResultText }}</b>
          </span>
          <span data-testid="p5-qc-detail-issuecount">问题：<b>{{ num(detail.errorCount) }}</b> 条</span>
        </div>
        <p class="mt-2 text-xs text-slate-500">{{ detail.qcContent }} · 触发 {{ detail.triggerSourceText || '—' }} ·
          质控人 {{ detail.qcBy || '—' }} ·
          {{ detail.qcTime || '—' }}</p>

        <!-- 明细表为空但 error_count>0：这类单子的问题只存在 error_detail 文本里
             （旧版质控，以及 qc_type=4 的 AI 内涵质控 —— AI 的发现是自由文本，没有结构化明细）。
             不兜底的话，抽屉会显示「问题：6 条」配一张空表，看起来像数据丢了。 -->
        <div
            v-if="(detail.issues || []).length === 0 && num(detail.errorCount) > 0"
            class="mt-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-xs leading-6 text-amber-900"
            data-testid="p5-qc-detail-textdetail"
        >
          <p class="font-medium">这张质控单的 {{ num(detail.errorCount) }} 条问题没有结构化明细（{{
              detail.qcTypeText
            }}），以下为质控原文：</p>
          <p class="mt-1 whitespace-pre-wrap">{{ detail.errorDetail }}</p>
        </div>

        <el-table :data="detail.issues || []" border class="mt-4" data-testid="p5-qc-detail-issues" size="small">
          <el-table-column label="维度" width="80">
            <template #default="{ row }">
              <span :style="{ color: dimColor[row.dimension] }" class="text-xs">{{ row.dimensionText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="严重度" width="80">
            <template #default="{ row }">
              <el-tag :type="severityTag(row.severity)" size="small">{{ row.severityText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="规则" width="150">
            <template #default="{ row }">
              <div class="text-xs">{{ row.ruleName }}</div>
              <div class="text-xs text-slate-400">{{ row.ruleCode }}</div>
            </template>
          </el-table-column>
          <el-table-column label="字段" width="90">
            <template #default="{ row }">
              <span class="text-xs">{{ row.fieldName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="问题" min-width="240">
            <template #default="{ row }">
              <div class="text-xs leading-5">{{ row.errorDetail }}</div>
            </template>
          </el-table-column>
          <el-table-column label="整改建议" min-width="200">
            <template #default="{ row }">
              <div class="text-xs leading-5 text-slate-500">{{ row.suggestion }}</div>
            </template>
          </el-table-column>
          <el-table-column label="原文证据" min-width="140">
            <template #default="{ row }">
              <span class="text-xs text-slate-500">{{ row.evidence }}</span>
            </template>
          </el-table-column>
          <el-table-column label="扣分" width="70">
            <template #default="{ row }">
              <span class="text-xs">-{{ row.deduct }}</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-4 text-sm text-slate-400">这张质控单没有问题明细</div>
          </template>
        </el-table>

        <div v-if="detail.qcStatus === 1" class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
          <p class="text-xs font-medium text-slate-700">处理这张质控单</p>
          <el-input v-model="handleRemark" class="mt-2" data-testid="p5-qc-handle-remark"
                    placeholder="处理备注（可空）"/>
          <div class="mt-3 flex gap-2">
            <el-button v-perm="'qc:recordQc:edit'" :loading="handling" data-testid="p5-qc-handle-done" size="small"
                       type="primary" @click="submitHandle(false)">整改完成
            </el-button>
            <el-button v-perm="'qc:recordQc:edit'" :loading="handling" data-testid="p5-qc-handle-ignore" size="small"
                       @click="submitHandle(true)">忽略
            </el-button>
          </div>
        </div>
        <div v-else class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs text-slate-500">
          当前状态：{{ detail.qcStatusText }}<span v-if="detail.remark"> · 备注：{{ detail.remark }}</span>
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
// 质控单明细抽屉：按 id 自取数，处理（整改完成/忽略）在本组件收口
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getQualityControlDetail, handleQualityControl} from '@/api/qualityControl';
import {dimColor, num, scoreText, severityTag} from '@/lib/quality';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [Number, String], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailLoading = ref(false);
const detail = ref(null);
const handleRemark = ref('');
const handling = ref(false);

const load = async () => {
  detailLoading.value = true;
  detail.value = null;
  handleRemark.value = '';
  try {
    const res = await getQualityControlDetail(props.id);
    detail.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    detailLoading.value = false;
  }
};

watch(visible, (open) => {
  if (open && props.id)
    load();
});

const submitHandle = async (ignore) => {
  if (!detail.value)
    return;
  handling.value = true;
  try {
    await handleQualityControl(detail.value.id, {ignore, remark: handleRemark.value || undefined});
    ElMessage.success(ignore ? '已忽略该质控单' : '已标记为处理完成');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    handling.value = false;
  }
};
</script>
