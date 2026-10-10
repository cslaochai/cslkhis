<template>
  <el-dialog
      v-model="visible"
      :title="`预约复诊号 · ${sourceLabel}`"
      destroy-on-close
      width="620px"
  >
    <div class="mb-4 rounded-lg border border-blue-200 bg-blue-50 px-3 py-2 text-xs text-blue-700">
      复诊号是新的一次就诊：占所选号源的号源，收不收费、免哪几项由「复诊收费策略」按所选原病历判定。
      <span v-if="patientName">患者：{{ patientName }}</span>
    </div>

    <el-form :model="form" label-position="top">
      <el-form-item label="原病历（按哪一次就诊复诊）" required>
        <el-select
            v-model="form.revisitRecordId"
            :disabled="!patientId"
            :loading="recordLoading"
            :placeholder="patientId ? '选择原病历' : '请先选择患者'"
            class="w-full"
        >
          <el-option v-for="r in recordOptions" :key="r.id" :label="recordLabel(r)" :value="r.id"/>
        </el-select>
        <p v-if="patientId && !recordLoading && !recordOptions.length"
           class="w-full mt-1 text-xs text-amber-600">
          该患者还没有可关联的就诊病历，无法预约复诊
        </p>
      </el-form-item>

      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="复诊科室" required>
          <el-select v-model="form.deptId" class="w-full" filterable placeholder="选择科室" @change="loadSchedules">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="复诊日期" required>
          <el-date-picker
              v-model="form.visitDate"
              :disabled-date="(t: Date) => t.getTime() < Date.now() - 86400000"
              class="w-full"
              placeholder="选择日期"
              type="date"
              value-format="YYYY-MM-DD"
              @change="loadSchedules"
          />
        </el-form-item>
      </div>

      <el-form-item label="号源" required>
        <el-select
            v-model="form.scheduleId"
            :disabled="!scheduleOptions.length"
            :loading="scheduleLoading"
            :placeholder="schedulePlaceholder"
            class="w-full"
        >
          <el-option
              v-for="s in scheduleOptions"
              :key="s.id"
              :label="`${s.doctorName} | ${s.roomName || '诊室待定'} | ${s.startTime}-${s.endTime} | 余号:${s.availableSource} | 挂号费:¥${s.registFee}`"
              :value="s.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="时间段（半小时一档）">
        <el-select v-model="form.slotId" :disabled="!slotRows.length" :loading="slotLoading" class="w-full"
                   clearable placeholder="请选择时间段">
          <el-option v-for="s in slotRows" :key="s.id" :disabled="s.status !== 1 || s.availableSource <= 0" :label="slotLabel(s)"
                     :value="s.id"/>
        </el-select>
        <p v-if="form.scheduleId && !slotRows.length && !slotLoading"
           class="w-full mt-1 text-xs text-slate-400">该号源暂无时间段明细，将按整班次号源预约</p>
      </el-form-item>

      <div v-if="preview || previewLoading" class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
        <div class="flex items-center justify-between text-sm">
          <span class="text-slate-500">复诊应收</span>
          <span :class="Number(preview?.totalFee) > 0 ? 'text-red-500' : 'text-emerald-600'"
                class="font-mono text-lg font-bold">
            {{ previewLoading ? '计算中…' : `¥${Number(preview?.totalFee || 0).toFixed(2)}` }}
          </span>
        </div>
        <div v-if="preview && !previewLoading" class="mt-1 text-xs text-slate-500">
          挂号费 ¥{{ Number(preview.registFee || 0).toFixed(2) }} ·
          诊查费 ¥{{ Number(preview.diagnosisFee || 0).toFixed(2) }} ·
          {{ preview.policyName ? `命中策略「${preview.policyName}」` : '未命中策略，按全额收费' }}
        </div>
        <p v-if="preview?.reason && !previewLoading" class="mt-1 text-xs text-slate-400">{{ preview.reason }}</p>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" type="primary" @click="handleSubmit">确定预约</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAvailableSchedule, getRevisitRecordSelectList, getScheduleSlots, previewRevisitFee,} from '@/api/appoint';
import {getDepartmentSelectList} from '@/api/system';
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache';
import {revisitNeedsNoSchedule} from '@/lib/revisitPolicy';

const props = defineProps({
  modelValue: {type: Boolean, required: true},
  patientId: {type: [String, Number, null], required: true},
  patientName: {type: String, required: false},
  revisitSource: {type: Number, required: true},
  settlementType: {type: Number, required: false},
  medicalInsuranceType: {type: String, required: false},
  medicalInsuranceNo: {type: String, required: false},
  defaultRecordId: {type: [String, Number, null], required: false},
  defaultDeptId: {type: [String, Number, null], required: false},
  submitting: {type: Boolean, required: false}
});
const emit = defineEmits();
const recordOptions = ref([]);
const recordLoading = ref(false);
const deptOptions = ref([]);
const scheduleOptions = ref([]);
const scheduleLoading = ref(false);
const slotRows = ref([]);
const slotLoading = ref(false);
const preview = ref(null);
const previewLoading = ref(false);
/** 复诊来源文案（表头那句话要能说出这是哪一种复诊） */
const sourceDict = ref([]);
const form = ref({
  revisitRecordId: null,
  deptId: null,
  visitDate: '',
  scheduleId: null,
  slotId: null,
});
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const sourceLabel = computed(() => {
  const hit = sourceDict.value.find((o) => Number(o.dictValue) === Number(props.revisitSource));
  return hit?.dictLabel || `复诊来源${props.revisitSource}`;
});
const recordLabel = (r) => `${r.visitDate || '日期未知'} ${r.deptName || '-'} / ${r.doctorName || '-'}${r.diagnosisName ? ` | ${r.diagnosisName}` : ''}`;
const slotLabel = (s) => `${s.startTime} ~ ${s.endTime}（余 ${s.availableSource}/${s.totalSource}）`;
/** 当日回诊走的是另一条路（不占号源），本组件只服务要占号源的那两种 */
const needsSchedule = computed(() => !revisitNeedsNoSchedule(props.revisitSource));
const schedulePlaceholder = computed(() => {
  if (!form.value.deptId || !form.value.visitDate)
    return '请先选择科室和日期';
  if (scheduleOptions.value.length)
    return '请选择号源';
  return '该日期无可预约号源';
});
const reset = () => {
  form.value = {
    revisitRecordId: props.defaultRecordId ?? null,
    deptId: props.defaultDeptId ?? null,
    visitDate: '',
    scheduleId: null,
    slotId: null,
  };
  scheduleOptions.value = [];
  slotRows.value = [];
  preview.value = null;
};
const loadRecords = async () => {
  if (!props.patientId)
    return;
  recordLoading.value = true;
  try {
    const res = await getRevisitRecordSelectList(props.patientId);
    recordOptions.value = res.data || [];
    // 预选的原病历必须真的在这份列表里（后端会校验归属），不在就不猜、留空让人自己挑
    if (form.value.revisitRecordId
        && !recordOptions.value.some((r) => String(r.id) === String(form.value.revisitRecordId))) {
      form.value.revisitRecordId = null;
    }
  } catch (e) {
    recordOptions.value = [];
    ElMessage.error(e?.message || '加载原病历失败');
  } finally {
    recordLoading.value = false;
  }
};
const loadSchedules = async () => {
  scheduleOptions.value = [];
  form.value.scheduleId = null;
  slotRows.value = [];
  form.value.slotId = null;
  if (!form.value.deptId || !form.value.visitDate)
    return;
  scheduleLoading.value = true;
  try {
    const res = await getAvailableSchedule(form.value.deptId, form.value.visitDate);
    scheduleOptions.value = (res.data || []).filter((s) => Number(s.availableSource) > 0);
  } catch (e) {
    ElMessage.error(e?.message || '加载号源失败');
  } finally {
    scheduleLoading.value = false;
  }
};
const loadSlots = async () => {
  slotRows.value = [];
  form.value.slotId = null;
  if (!form.value.scheduleId)
    return;
  slotLoading.value = true;
  try {
    const res = await getScheduleSlots(form.value.scheduleId);
    slotRows.value = res.data || [];
  } catch (e) {
    ElMessage.error(e?.message || '加载时间段失败');
  } finally {
    slotLoading.value = false;
  }
};
/**
 * 费用预估：与后端实收同一个 decide，所以「预约时显示 ¥12、挂号时收 ¥0」不可能出现。
 * previewToken 掐掉过期响应：连改两次号源会并发两个请求，后到的旧响应不能盖掉新结果。
 */
let previewToken = 0;
const runPreview = async () => {
  const token = ++previewToken;
  const ready = props.patientId && form.value.revisitRecordId && form.value.scheduleId;
  if (!ready) {
    preview.value = null;
    previewLoading.value = false;
    return;
  }
  previewLoading.value = true;
  try {
    const res = await previewRevisitFee({
      patientId: props.patientId,
      revisitSource: props.revisitSource,
      revisitRecordId: form.value.revisitRecordId,
      scheduleId: form.value.scheduleId,
    });
    if (token !== previewToken)
      return;
    preview.value = res.data || null;
  } catch (e) {
    if (token !== previewToken)
      return;
    preview.value = null;
    ElMessage.error(e?.message || '复诊费用预估失败');
  } finally {
    if (token === previewToken)
      previewLoading.value = false;
  }
};
/**
 * 字典与科室在挂载时拉一次即可（弹框随父页面活，销毁重建的成本比多发的请求低）。
 * 原病历必须每次打开重查：上一位患者的病历列表留在这里，人选到的就是别人的就诊。
 */
onMounted(async () => {
  sourceDict.value = await loadDictDataList(DICT_TYPE.REVISIT_SOURCE);
  try {
    // deptType=1 门诊科室：复诊号挂的是门诊排班，住院科室没有号源可选
    // 不传 scope → 后端按当前人收口：医生只看到自己被授权的科室
    const res = await getDepartmentSelectList({deptType: 1});
    deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室列表失败:', e);
  }
});
watch(() => props.modelValue, async (open) => {
  if (!open)
    return;
  reset();
  await loadRecords();
});
watch(() => form.value.scheduleId, () => {
  loadSlots();
  runPreview();
});
watch(() => form.value.revisitRecordId, runPreview);
const handleSubmit = () => {
  if (!form.value.revisitRecordId) {
    ElMessage.warning('请选择原病历（复诊要关联的那一次就诊）');
    return;
  }
  if (needsSchedule.value) {
    if (!form.value.deptId) {
      ElMessage.warning('请选择科室');
      return;
    }
    if (!form.value.visitDate) {
      ElMessage.warning('请选择复诊日期');
      return;
    }
    if (!form.value.scheduleId) {
      ElMessage.warning('请选择号源');
      return;
    }
    if (slotRows.value.length && !form.value.slotId) {
      ElMessage.warning('请选择时间段');
      return;
    }
  }
  emit('submitted', {
    revisitRecordId: form.value.revisitRecordId,
    scheduleId: form.value.scheduleId,
    slotId: form.value.slotId || undefined,
    settlementType: Number(props.settlementType) || 1,
    medicalInsuranceType: props.medicalInsuranceType || '',
    medicalInsuranceNo: props.medicalInsuranceNo || '',
  });
};
/** 父页面提交失败时把按钮解锁（成功则由父页面关掉弹框） */
</script>
