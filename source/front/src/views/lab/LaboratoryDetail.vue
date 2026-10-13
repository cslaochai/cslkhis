<template>
  <el-dialog v-model="visible" :title="`检验详情 - ${selectedOrder?.itemName}`" destroy-on-close width="920px">
    <template v-if="selectedOrder">
      <div class="space-y-4 py-2">
        <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
          <div><p class="text-xs text-slate-400">患者</p>
            <p class="text-sm font-medium">{{ selectedOrder.patientName }}</p></div>
          <div><p class="text-xs text-slate-400">记录号</p>
            <p class="text-sm font-medium">{{ selectedOrder.recordNo }}</p></div>
          <div><p class="text-xs text-slate-400">申请医生</p>
            <p class="text-sm font-medium">{{ selectedOrder.doctorName }}</p></div>
          <div><p class="text-xs text-slate-400">申请科室</p>
            <p class="text-sm font-medium">{{ selectedOrder.deptName }}</p></div>
          <div><p class="text-xs text-slate-400">检验项目</p>
            <p class="text-sm font-medium">{{ selectedOrder.itemName }}</p></div>
          <div><p class="text-xs text-slate-400">标本类型</p>
            <p class="text-sm font-medium">{{ selectedOrder.specimenType }}</p></div>
          <div><p class="text-xs text-slate-400">费用</p>
            <p class="text-sm font-medium">¥{{ selectedOrder.price }}</p></div>
          <div><p class="text-xs text-slate-400">临床诊断</p>
            <p class="text-sm font-medium">{{ selectedOrder.clinicalDiagnosis }}</p></div>
        </div>

        <!-- 检验结果 -->
        <div v-if="selectedOrder.results?.length" class="rounded-lg border border-slate-200 p-4">
          <p class="mb-3 text-sm font-medium text-slate-700">检验结果</p>
          <el-table :data="selectedOrder.results" border size="small">
            <el-table-column label="项目" min-width="120" prop="laboratoryItemName"/>
            <el-table-column label="结果" prop="resultValue" width="100"/>
            <el-table-column label="单位" prop="resultUnit" width="80"/>
            <el-table-column label="参考范围" prop="referenceRange" width="100"/>
            <el-table-column align="center" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="abnormalTagType(row)" effect="plain" size="small">
                  {{ abnormalText(row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="判定说明" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ row.judgeNote || '—' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- AI 检验解读 -->
        <div class="rounded-lg border border-slate-200 p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-700">检验结果智能解读</p>
              <p class="mt-0.5 text-xs text-slate-400">
                异常项、危急值、趋势由系统按规则算出，模型只负责解释它们组合起来意味着什么
              </p>
            </div>
            <el-button
                :disabled="!selectedOrder.results?.length"
                :loading="interpretLoading"
                type="primary"
                @click="runInterpret(false)"
            >
              {{ interpret ? '重新解读' : 'AI 解读' }}
            </el-button>
          </div>

          <div v-if="interpret" class="mt-3 space-y-3">
            <!-- 结论来源必须让医生知道：degraded=组合异常但模型失败；source=model=模型连贯解读；
                 其余=规则解读（异常项未达组合阈值，设计内路径，不是降级） -->
            <el-alert
                v-if="interpret.degraded"
                :closable="false"
                show-icon
                title="组合异常需模型解读，但本次模型调用失败，结论来自确定性规则"
                type="warning"
            >
              <template #default>
                <span class="text-xs">{{ interpret.degradeReason }}</span>
              </template>
            </el-alert>
            <el-alert
                v-else-if="interpret.source === 'model'"
                :closable="false"
                show-icon
                title="多项异常组合，已由大模型连贯解读"
                type="success"
            />
            <el-alert
                v-else
                :closable="false"
                show-icon
                title="规则解读：异常项未达组合解读阈值，结论由确定性规则生成"
                type="info"
            />

            <!-- 计数 -->
            <div class="grid grid-cols-4 gap-2 text-center">
              <div class="rounded bg-slate-50 py-2">
                <p class="text-lg font-bold text-slate-800">{{ interpret.itemCount ?? 0 }}</p>
                <p class="text-xs text-slate-500">结果项</p>
              </div>
              <div class="rounded bg-red-50 py-2">
                <p class="text-lg font-bold text-red-600">{{ interpret.abnormalCount ?? 0 }}</p>
                <p class="text-xs text-slate-500">异常</p>
              </div>
              <div class="rounded bg-red-50 py-2">
                <p class="text-lg font-bold text-red-600">{{ interpret.criticalCount ?? 0 }}</p>
                <p class="text-xs text-slate-500">危急值</p>
              </div>
              <div :class="interpret.unjudgedCount ? 'bg-amber-50' : 'bg-slate-50'" class="rounded py-2">
                <p :class="interpret.unjudgedCount ? 'text-amber-600' : 'text-slate-800'" class="text-lg font-bold">
                  {{ interpret.unjudgedCount ?? 0 }}
                </p>
                <p class="text-xs text-slate-500">未判定</p>
              </div>
            </div>

            <!-- 趋势 -->
            <div v-if="interpret.trends?.length" class="rounded border border-slate-200 p-3">
              <p class="mb-2 text-xs font-medium text-slate-600">历史趋势（系统比对得出）</p>
              <div v-for="t in interpret.trends" :key="t.itemName" class="flex items-center gap-2 py-0.5 text-xs">
                <span class="font-medium text-slate-700">{{ t.itemName }}</span>
                <span :class="t.direction === '升高' ? 'text-red-600' : 'text-blue-600'">
                  {{ t.changeText }}
                </span>
                <span v-if="t.unit" class="text-slate-400">{{ t.unit }}</span>
                <span v-if="t.magnitudeText" class="text-slate-400">· {{ t.magnitudeText }}</span>
              </div>
            </div>

            <!-- 逐项标志 -->
            <el-table :data="interpret.items || []" border max-height="240" size="small">
              <el-table-column label="项目" min-width="110" prop="itemName"/>
              <el-table-column label="结果" width="110">
                <template #default="{ row }">
                  {{ row.resultValue }} <span class="text-xs text-slate-400">{{ row.resultUnit }}</span>
                </template>
              </el-table-column>
              <el-table-column label="参考区间" prop="referenceRange" width="110"/>
              <el-table-column align="center" label="标志" width="80">
                <template #default="{ row }">
                  <el-tag :type="abnormalTagType(row)" effect="plain" size="small">
                    {{ abnormalText(row) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column align="center" label="危急" width="70">
                <template #default="{ row }">
                  <el-tag v-if="row.critical" size="small" type="danger">危急</el-tag>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column label="判定留痕" min-width="170" show-overflow-tooltip>
                <template #default="{ row }">
                  <span class="text-xs text-slate-500">{{ row.judgeNote || '—' }}</span>
                </template>
              </el-table-column>
            </el-table>

            <!-- 结论与建议 -->
            <div class="rounded border border-slate-200 p-3">
              <p class="mb-1 text-xs font-medium text-slate-600">结论草稿</p>
              <p class="whitespace-pre-wrap text-sm text-slate-800">{{ interpret.conclusion }}</p>

              <template v-if="interpret.trendSummary">
                <p class="mt-3 mb-1 text-xs font-medium text-slate-600">趋势解读</p>
                <p class="text-sm text-slate-700">{{ interpret.trendSummary }}</p>
              </template>

              <template v-if="interpret.suggestions?.length">
                <p class="mt-3 mb-1 text-xs font-medium text-slate-600">建议</p>
                <ul class="list-disc pl-5 text-sm text-slate-700">
                  <li v-for="(s, i) in interpret.suggestions" :key="i">{{ s }}</li>
                </ul>
              </template>

              <template v-if="interpret.attentionPoints?.length">
                <p class="mt-3 mb-1 text-xs font-medium text-slate-600">需要重点关注</p>
                <ul class="list-disc pl-5 text-sm text-red-600">
                  <li v-for="(s, i) in interpret.attentionPoints" :key="i">{{ s }}</li>
                </ul>
              </template>

              <p v-if="interpret.saveTip" class="mt-2 text-xs text-slate-400">{{ interpret.saveTip }}</p>
              <p v-if="interpret.latencyMs != null" class="mt-1 text-xs text-slate-300">
                耗时 {{ interpret.latencyMs }} ms
              </p>
            </div>

            <div class="flex items-center justify-between">
              <el-checkbox v-model="overwriteOnWriteBack">
                同时写回检验记录的「结论/建议」（会覆盖检验科已填内容）
              </el-checkbox>
              <el-button
                  v-perm="'medtech:laboratoryWorkstation:edit'"
                  :loading="interpretLoading"
                  plain
                  size="small"
                  type="primary"
                  @click="runInterpret(true)"
              >
                写回结论
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getLaboratoryDetail} from '@/api/medicaltech';
import {executeLabInterpret} from '@/api/ai';
import {abnormalTagType, abnormalText} from '@/lib/lab';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 列表行数据（父级传入避免闪烁；检验结果明细由本组件打开时自取）
  order: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const selectedOrder = ref(null);

// ---------------- AI 检验解读 ----------------
const interpret = ref(null);
const interpretLoading = ref(false);
const overwriteOnWriteBack = ref(false);

watch(visible, (v) => {
  if (!v) return;
  selectedOrder.value = props.order ? {...props.order, results: []} : null;
  // 换记录时清掉上一次的解读结果，避免张冠李戴
  interpret.value = null;
  interpretLoading.value = false;
  // 加载检验结果
  loadResults();
});

const loadResults = async () => {
  if (!props.order) return;
  try {
    const res = await getLaboratoryDetail(props.order.id);
    if (res.data?.results) {
      selectedOrder.value.results = res.data.results;
    }
  } catch (e) {
    console.error('加载详情失败', e);
  }
};

/**
 * 调用解读接口。
 *
 * overwrite 为 false 时只是拿一份结论草稿，不碰检验记录；
 * 只有用户明确勾选并点「写回结论」才会覆盖 diagnosis / suggestions。
 */
const runInterpret = async (overwrite) => {
  if (!selectedOrder.value)
    return;
  if (overwrite && !overwriteOnWriteBack.value) {
    ElMessage.warning('请先勾选「同时写回检验记录的结论/建议」');
    return;
  }
  interpretLoading.value = true;
  try {
    const res = await executeLabInterpret({
      recordId: selectedOrder.value.id,
      overwriteConclusion: overwrite,
      includeTrend: true,
    });
    interpret.value = res.data;
    if (overwrite) {
      ElMessage.success('结论已写回检验记录');
      emit('saved');
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    interpretLoading.value = false;
  }
};
</script>
