<template>
  <el-dialog v-model="visible" :title="`放射报告书写 - ${detail?.patientName || ''} ${detail?.itemName || ''}`" destroy-on-close
             width="1100px">
    <template v-if="detail">
      <div class="grid grid-cols-5 gap-4">
        <!-- 左：阅片 -->
        <div class="col-span-2 space-y-3">
          <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
            <div><span class="text-slate-400">患者：</span>{{ detail.patientName }}
              {{ patientGenderText(detail.gender) }} {{ detail.age }}岁
            </div>
            <div><span class="text-slate-400">记录号：</span>{{ detail.recordNo }}</div>
            <div><span class="text-slate-400">部位：</span>{{ detail.bodyPart || '—' }}</div>
            <div><span class="text-slate-400">申请：</span>{{ detail.applyDeptName || '—' }}
              {{ detail.applyDoctorName || '' }}
            </div>
            <div><span class="text-slate-400">临床诊断：</span>{{ detail.clinicalDiagnosis || '—' }}</div>
            <div><span class="text-slate-400">胶片：</span>{{ detail.filmCount ?? 0 }} 张</div>
          </div>
          <ExamImagePanel :apply-id="detail.applyId" :biz-type="1" readonly/>
        </div>
        <!-- 右：书写 -->
        <div class="col-span-3 space-y-3">
          <div class="flex items-center gap-2">
            <span class="text-sm text-slate-600">报告模板</span>
            <el-select v-model="form.templateId" class="!w-64" clearable placeholder="选择模板（可选）"
                       @change="applyTemplate">
              <el-option v-for="t in templates" :key="t.id" :label="t.templateName" :value="t.id"/>
            </el-select>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">检查方法</label>
            <el-input v-model="form.examMethod" :disabled="readonlyMode"
                      placeholder="如：胸部CT平扫，层厚5mm"/>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">影像所见
              <span class="text-red-500">*</span></label>
            <el-input v-model="form.reportContent" :autosize="{ minRows: 6, maxRows: 12 }" :disabled="readonlyMode"
                      placeholder="描述影像所见…" type="textarea"/>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">影像诊断/印象
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
      <el-button v-if="!readonlyMode" v-perm="'medtech:radioDiagnosis:write'" :loading="submitting"
                 @click="doSaveDraft">
        <el-icon class="mr-0.5">
          <Document/>
        </el-icon>
        保存草稿
      </el-button>
      <el-button v-if="!readonlyMode" v-perm="'medtech:radioDiagnosis:write'" :loading="submitting" type="primary"
                 @click="doSubmit">
        <el-icon class="mr-0.5">
          <Check/>
        </el-icon>
        提交审核
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 软签二次确认：提交报告要落报告医师签名，由签名人本人输入 PIN -->
  <SignPinDialog ref="pinDialogRef" v-model="pinDialogVisible"/>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {Check, Document} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getRadioReportDetailByRecordId,
  getRadioTemplateSelectList,
  saveRadioReportDraft,
  submitRadioReport
} from '@/api/medicaltech';
import {patientGenderText} from '@/lib/patientGender';
import ExamImagePanel from '@/components/his/ExamImagePanel.vue';
import SignPinDialog from '@/components/his/SignPinDialog.vue';

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

const submitting = ref(false);
// 软签二次确认：PIN 由签名人本人输入，服务端校验通过才解密私钥
const pinDialogRef = ref(null);
const pinDialogVisible = ref(false);
const detail = ref(null);
const templates = ref([]);
const form = ref({
  reportId: undefined,
  recordId: undefined,
  templateId: undefined,
  examMethod: '',
  reportContent: '',
  conclusion: '',
  suggestions: '',
  positiveFlag: 0,
  isCritical: 0,
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

watch(visible, (v) => {
  if (v) openWrite();
});

const openWrite = async () => {
  if (!props.row) return;
  try {
    const res = await getRadioReportDetailByRecordId(props.row.recordId);
    detail.value = res.data;
    form.value = {
      reportId: res.data?.reportId,
      recordId: res.data?.recordId,
      templateId: res.data?.templateId,
      examMethod: res.data?.examMethod || '',
      reportContent: res.data?.reportContent || '',
      conclusion: res.data?.conclusion || '',
      suggestions: res.data?.suggestions || '',
      positiveFlag: res.data?.positiveFlag ?? 0,
      isCritical: res.data?.isCritical ?? 0,
    };
    if (!templates.value.length) {
      const t = await getRadioTemplateSelectList(undefined);
      templates.value = t.data || [];
    }
  } catch (e) {
    
    visible.value = false;
  }
};

/** 套用模板：把三段模板文本填进表单（会覆盖已写内容，所以先问一句） */
const applyTemplate = async (id) => {
  const t = templates.value.find(x => x.id === id);
  if (!t)
    return;
  if (form.value.reportContent?.trim()) {
    try {
      await ElMessageBox.confirm('套用模板会覆盖当前已填写的检查所见 / 印象 / 建议，继续？', '套用模板', {
        confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning',
      });
    } catch {
      return;
    }
  }
  form.value.examMethod = t.examMethod || form.value.examMethod;
  form.value.reportContent = t.findingTpl || '';
  form.value.conclusion = t.impressionTpl || '';
  form.value.suggestions = t.suggestionTpl || '';
};
const payload = () => ({
  reportId: form.value.reportId,
  recordId: form.value.recordId,
  templateId: form.value.templateId,
  examMethod: form.value.examMethod,
  reportContent: form.value.reportContent,
  conclusion: form.value.conclusion,
  suggestions: form.value.suggestions,
  positiveFlag: form.value.positiveFlag,
  isCritical: form.value.isCritical,
});
const doSaveDraft = async () => {
  submitting.value = true;
  try {
    const res = await saveRadioReportDraft(payload());
    detail.value = res.data;
    form.value.reportId = res.data?.reportId;
    ElMessage.success('草稿已保存');
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitting.value = false;
  }
};
const doSubmit = async () => {
  if (!form.value.reportContent?.trim())
    return ElMessage.warning('请先填写影像所见');
  if (!form.value.conclusion?.trim())
    return ElMessage.warning('请先填写影像诊断/印象');
  try {
    await ElMessageBox.confirm('提交后报告进入待审核，并由你完成报告医师签名。确认提交？', '提交审核', {
      confirmButtonText: '提交', cancelButtonText: '再改改', type: 'warning',
    });
  } catch {
    return;
  }
  // 提交报告要落报告医师签名：先取 PIN，取消则不提交
  const pin = await pinDialogRef.value.askPin();
  if (!pin) {
    ElMessage.info('已取消签名，报告未提交');
    return;
  }
  submitting.value = true;
  try {
    // 草稿不签名，只有提交带 pin
    await submitRadioReport({...payload(), pin});
    ElMessage.success('已提交审核（报告医师签名已完成）');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitting.value = false;
  }
};
</script>
