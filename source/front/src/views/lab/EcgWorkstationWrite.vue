<template>
  <el-dialog v-model="visible" :title="`心电报告书写 - ${detail?.patientName || ''} ${detail?.itemName || ''}`" destroy-on-close top="4vh"
             width="1200px">
    <template v-if="detail">
      <div class="grid grid-cols-5 gap-4">
        <!-- 左：波形 + 测量 + Holter -->
        <div class="col-span-2 space-y-3">
          <EcgWavePanel :title="detail.ecgTypeText || '12 导联心电图'" :wave-data="detail.waveData"/>

          <div class="rounded-lg border border-slate-200 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">测量参数</p>
            <div class="grid grid-cols-3 gap-x-2 gap-y-2">
              <div>
                <label class="text-xs text-slate-500">心率 bpm</label>
                <el-input-number v-model="measureForm.hr" :disabled="readonlyMode" :max="300" :min="20" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">PR ms</label>
                <el-input-number v-model="measureForm.prMs" :disabled="readonlyMode" :max="500" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">QRS ms</label>
                <el-input-number v-model="measureForm.qrsMs" :disabled="readonlyMode" :max="300" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">QT ms</label>
                <el-input-number v-model="measureForm.qtMs" :disabled="readonlyMode" :max="700" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">QTc ms</label>
                <el-input-number v-model="measureForm.qtcMs" :disabled="readonlyMode" :max="700" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">节律</label>
                <el-input v-model="measureForm.rhythmText" :disabled="readonlyMode" maxlength="100"
                          placeholder="如 窦性心律" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">P 电轴°</label>
                <el-input-number v-model="measureForm.pAxis" :disabled="readonlyMode" :max="180" :min="-180" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">QRS 电轴°</label>
                <el-input-number v-model="measureForm.qrsAxis" :disabled="readonlyMode" :max="180" :min="-180" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">T 电轴°</label>
                <el-input-number v-model="measureForm.tAxis" :disabled="readonlyMode" :max="180" :min="-180" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
            </div>
            <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" :loading="submitting" class="mt-2"
                       size="small" @click="doSaveMeasure">保存测量参数
            </el-button>
          </div>

          <!-- Holter 分析（仅动态心电出现） -->
          <div v-if="isHolter" class="rounded-lg border border-slate-200 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">
              Holter 动态心电分析
              <el-tag v-if="detail.holterId" class="ml-1" size="small" type="success">已分析</el-tag>
              <el-tag v-else class="ml-1" size="small" type="danger">未分析（提交报告前必填）</el-tag>
            </p>
            <div class="grid grid-cols-2 gap-x-2 gap-y-2">
              <div class="col-span-2 grid grid-cols-2 gap-2">
                <div>
                  <label class="text-xs text-slate-500">佩戴开始</label>
                  <el-date-picker v-model="holterForm.wearStartTime" :disabled="readonlyMode" class="!w-full"
                                  size="small" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
                </div>
                <div>
                  <label class="text-xs text-slate-500">佩戴结束</label>
                  <el-date-picker v-model="holterForm.wearEndTime" :disabled="readonlyMode" class="!w-full"
                                  size="small" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
                </div>
              </div>
              <div>
                <label class="text-xs text-slate-500">总心搏</label>
                <el-input-number v-model="holterForm.totalBeats" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">平均心率 bpm</label>
                <el-input-number v-model="holterForm.avgHr" :disabled="readonlyMode" :max="250" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">最快心率</label>
                <el-input-number v-model="holterForm.maxHr" :disabled="readonlyMode" :max="250" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">最慢心率</label>
                <el-input-number v-model="holterForm.minHr" :disabled="readonlyMode" :max="250" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">房颤</label>
                <el-select v-model="holterForm.afibFlag" :disabled="readonlyMode" class="!w-full" size="small">
                  <el-option :value="0" label="未检出"/>
                  <el-option :value="1" label="检出"/>
                </el-select>
              </div>
              <div>
                <label class="text-xs text-slate-500">房颤心搏</label>
                <el-input-number v-model="holterForm.afibBeats" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">室上早</label>
                <el-input-number v-model="holterForm.svcCount" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">室早</label>
                <el-input-number v-model="holterForm.pvcCount" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">室速阵次</label>
                <el-input-number v-model="holterForm.vtCount" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">停搏次数</label>
                <el-input-number v-model="holterForm.pauseCount" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">最长停搏 ms</label>
                <el-input-number v-model="holterForm.longestPauseMs" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
              <div>
                <label class="text-xs text-slate-500">ST 段改变阵次</label>
                <el-input-number v-model="holterForm.stEpisodeCount" :disabled="readonlyMode" :min="0" class="!w-full"
                                 controls-position="right" size="small"/>
              </div>
            </div>
            <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" :loading="submitting" class="mt-2" plain
                       size="small" type="primary" @click="doSaveHolter">保存 Holter 分析
            </el-button>
          </div>
        </div>

        <!-- 右：报告书写 -->
        <div class="col-span-3 space-y-3">
          <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
            <span class="text-slate-400">患者：</span>{{ detail.patientName }}
            {{ patientGenderText(detail.gender) }} {{ detail.age }}岁
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">记录号：</span>{{ detail.recordNo }}
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">申请：</span>{{ detail.applyDeptName || '—' }}
            {{ detail.applyDoctorName || '' }}
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">临床诊断：</span>{{ detail.clinicalDiagnosis || '—' }}
          </div>

          <div class="flex items-center gap-2">
            <span class="text-sm text-slate-600">报告模板</span>
            <el-select v-model="form.templateId" :disabled="readonlyMode" class="!w-64" clearable
                       placeholder="选择模板（可选）" @change="applyTemplate">
              <el-option v-for="t in templates" :key="t.id" :label="t.templateName" :value="t.id"/>
            </el-select>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">心电图所见
              <span class="text-red-500">*</span></label>
            <el-input v-model="form.reportContent" :autosize="{ minRows: 6, maxRows: 12 }" :disabled="readonlyMode"
                      placeholder="描述各导联节律、波形、ST-T 改变…" type="textarea"/>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">心电图诊断
              <span class="text-red-500">*</span></label>
            <el-input v-model="form.conclusion" :autosize="{ minRows: 3, maxRows: 6 }" :disabled="readonlyMode"
                      placeholder="给出诊断意见…" type="textarea"/>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">建议</label>
            <el-input v-model="form.suggestions" :autosize="{ minRows: 2, maxRows: 4 }" :disabled="readonlyMode"
                      type="textarea"/>
          </div>
          <div class="flex items-center gap-6">
            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-600">阴阳性</span>
              <el-select v-model="form.positiveFlag" :disabled="readonlyMode" class="!w-32">
                <el-option v-for="o in POSITIVE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
              </el-select>
            </div>
            <el-checkbox v-model="form.isCritical" :disabled="readonlyMode" :false-label="0"
                         :true-label="1">危急
            </el-checkbox>
          </div>
          <div v-if="detail.rejectReason" class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs">
            <span class="font-medium text-amber-700">上次退回原因：</span>{{ detail.rejectReason }}
            <span class="ml-2 text-slate-400">（第 {{ detail.reportVersion }} 版）</span>
          </div>
        </div>
      </div>
    </template>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" :loading="submitting" @click="doSaveDraft">
        <el-icon class="mr-0.5">
          <Document/>
        </el-icon>
        保存草稿
      </el-button>
      <el-button v-if="!readonlyMode" v-perm="'medtech:ecg:write'" :loading="submitting" type="primary"
                 @click="doSubmit">
        <el-icon class="mr-0.5">
          <Check/>
        </el-icon>
        提交审核
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {Check, Document} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  ecgSaveDraft,
  ecgSaveHolter,
  ecgSaveMeasure,
  ecgSubmit,
  getEcgDetailByRecordId,
  getEcgTemplateSelectList
} from '@/api/medicaltech';
import {patientGenderText} from '@/lib/patientGender';
import EcgWavePanel from '@/components/his/EcgWavePanel.vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 列表行数据（按 row.recordId 自取报告详情）
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// ========== 报告书写（医师岗） ==========
const submitting = ref(false);
const detail = ref(null);
const templates = ref([]);
const form = ref({
  reportId: undefined,
  recordId: undefined,
  templateId: undefined,
  reportContent: '',
  conclusion: '',
  suggestions: '',
  positiveFlag: 0,
  isCritical: 0,
});
// 测量参数
const measureForm = ref({
  hr: undefined,
  prMs: undefined,
  qrsMs: undefined,
  qtMs: undefined,
  qtcMs: undefined,
  pAxis: undefined,
  qrsAxis: undefined,
  tAxis: undefined,
  rhythmText: '',
});
// Holter 分析（仅 ecgType=2 出现）
const holterForm = ref({
  wearStartTime: '',
  wearEndTime: '',
  totalBeats: undefined,
  avgHr: undefined,
  maxHr: undefined,
  maxHrTime: '',
  minHr: undefined,
  minHrTime: '',
  afibFlag: 0,
  afibBeats: undefined,
  svcCount: undefined,
  pvcCount: undefined,
  vtCount: undefined,
  pauseCount: undefined,
  longestPauseMs: undefined,
  stEpisodeCount: undefined,
});
const POSITIVE_OPTIONS = [
  {value: 0, label: '未判定'},
  {value: 1, label: '阴性'},
  {value: 2, label: '阳性'},
  {value: 3, label: '未见异常'},
];
const readonlyMode = computed(() => {
  const st = detail.value?.reportStatus;
  return st === 4 || st === 5;
});
const isHolter = computed(() => detail.value?.ecgType === 2);

watch(visible, (v) => {
  if (v) openWrite();
});

const openWrite = async () => {
  try {
    const res = await getEcgDetailByRecordId(props.row?.recordId);
    detail.value = res.data;
    form.value = {
      reportId: res.data?.reportId,
      recordId: res.data?.recordId,
      templateId: res.data?.templateId,
      reportContent: res.data?.reportContent || '',
      conclusion: res.data?.conclusion || '',
      suggestions: res.data?.suggestions || '',
      positiveFlag: res.data?.positiveFlag ?? 0,
      isCritical: res.data?.isCritical ?? 0,
    };
    measureForm.value = {
      hr: res.data?.hr, prMs: res.data?.prMs, qrsMs: res.data?.qrsMs,
      qtMs: res.data?.qtMs, qtcMs: res.data?.qtcMs,
      pAxis: res.data?.pAxis, qrsAxis: res.data?.qrsAxis, tAxis: res.data?.tAxis,
      rhythmText: res.data?.rhythmText || '',
    };
    holterForm.value = {
      wearStartTime: res.data?.wearStartTime || '', wearEndTime: res.data?.wearEndTime || '',
      totalBeats: res.data?.totalBeats, avgHr: res.data?.avgHr,
      maxHr: res.data?.maxHr, maxHrTime: res.data?.maxHrTime || '',
      minHr: res.data?.minHr, minHrTime: res.data?.minHrTime || '',
      afibFlag: res.data?.afibFlag ?? 0, afibBeats: res.data?.afibBeats,
      svcCount: res.data?.svcCount, pvcCount: res.data?.pvcCount,
      vtCount: res.data?.vtCount, pauseCount: res.data?.pauseCount,
      longestPauseMs: res.data?.longestPauseMs, stEpisodeCount: res.data?.stEpisodeCount,
    };
    if (!templates.value.length) {
      const t = await getEcgTemplateSelectList(res.data?.ecgType);
      templates.value = t.data || [];
    }
  } catch (e) {
    ElMessage.error(e?.message || '打开失败');
    visible.value = false;
  }
};
/** 套用模板：三段模板文本填进报告表单（会覆盖已写内容，所以先问一句） */
const applyTemplate = async (id) => {
  const t = templates.value.find(x => x.id === id);
  if (!t)
    return;
  if (form.value.reportContent?.trim()) {
    try {
      await ElMessageBox.confirm('套用模板会覆盖当前已填写的心电图所见 / 诊断 / 建议，继续？', '套用模板', {
        confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning',
      });
    } catch {
      return;
    }
  }
  form.value.reportContent = t.findingTpl || '';
  form.value.conclusion = t.conclusionTpl || '';
  form.value.suggestions = t.suggestionTpl || '';
};
const doSaveMeasure = async () => {
  if (!measureForm.value.hr)
    return ElMessage.warning('请先填写心率');
  submitting.value = true;
  try {
    const res = await ecgSaveMeasure({recordId: detail.value?.recordId, ...measureForm.value});
    detail.value = res.data;
    ElMessage.success('测量参数已保存');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
const doSaveHolter = async () => {
  if (!holterForm.value.wearStartTime || !holterForm.value.wearEndTime) {
    return ElMessage.warning('请先填写佩戴起止时间');
  }
  submitting.value = true;
  try {
    const res = await ecgSaveHolter({recordId: detail.value?.recordId, ...holterForm.value});
    detail.value = res.data;
    ElMessage.success('Holter 分析已保存');
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
const payload = () => ({
  reportId: form.value.reportId,
  recordId: form.value.recordId,
  templateId: form.value.templateId,
  reportContent: form.value.reportContent,
  conclusion: form.value.conclusion,
  suggestions: form.value.suggestions,
  positiveFlag: form.value.positiveFlag,
  isCritical: form.value.isCritical,
});
const doSaveDraft = async () => {
  submitting.value = true;
  try {
    const res = await ecgSaveDraft(payload());
    detail.value = res.data;
    form.value.reportId = res.data?.reportId;
    ElMessage.success('草稿已保存');
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
const doSubmit = async () => {
  if (!detail.value?.waveId) {
    return ElMessage.warning('该检查还没有波形数据：请先完成波形采集再提交');
  }
  if (isHolter.value && !detail.value?.holterId) {
    return ElMessage.warning('Holter 检查必须先保存动态心电分析结果再提交');
  }
  if (!form.value.reportContent?.trim())
    return ElMessage.warning('请先填写心电图所见');
  if (!form.value.conclusion?.trim())
    return ElMessage.warning('请先填写心电图诊断');
  try {
    await ElMessageBox.confirm('提交后报告进入待审核，并由你完成报告医师签名。确认提交？', '提交审核', {
      confirmButtonText: '提交', cancelButtonText: '再改改', type: 'warning',
    });
  } catch {
    return;
  }
  submitting.value = true;
  try {
    await ecgSubmit(payload());
    ElMessage.success('已提交审核（报告医师签名已完成）');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '提交失败');
  } finally {
    submitting.value = false;
  }
};
</script>
