<template>
  <div class="rounded border border-slate-200 bg-white p-3" data-testid="emg-score-panel">
    <div class="mb-2 flex items-center gap-2">
      <span class="text-sm font-bold text-slate-700">专科评分</span>
      <span class="text-[11px] text-slate-400">GCS / RTS 创伤 / REMS——体征自动带入，可手工修正</span>
      <span class="flex-1"></span>
      <el-button size="small" type="primary" plain :disabled="!canWrite"
                 data-testid="emg-score-write" @click="writeToRecord">写入病历（体格检查）
      </el-button>
    </div>

    <!-- 2026-10-28 改版：面板移入医生站 420px 右辅助列常驻，三块评分纵向堆叠（3 列会把 REMS 内层输入框挤成 0 宽） -->
    <div class="grid grid-cols-1 gap-3">
      <!-- ===== GCS ===== -->
      <div class="rounded border border-slate-100 p-2">
        <div class="mb-1.5 flex items-baseline gap-2">
          <span class="text-xs font-bold text-slate-600">GCS 格拉斯哥昏迷</span>
          <span class="text-lg font-bold" :class="gcsColor" data-testid="emg-score-gcs-total">{{ gcsTotal ?? '—' }}</span>
          <span class="text-[11px] text-slate-400">{{ gcsSeverity }}</span>
        </div>
        <div class="space-y-1.5">
          <div class="flex items-center gap-1.5 text-xs">
            <span class="w-8 text-slate-500">睁眼</span>
            <el-select v-model="gcsEye" filterable size="small" class="flex-1" data-testid="emg-score-gcs-eye">
              <el-option v-for="o in EYE_OPTS" :key="o.v" :value="o.v" :label="o.v + ' · ' + o.t"/>
            </el-select>
          </div>
          <div class="flex items-center gap-1.5 text-xs">
            <span class="w-8 text-slate-500">语言</span>
            <el-select v-model="gcsVerbal" filterable size="small" class="flex-1" data-testid="emg-score-gcs-verbal">
              <el-option v-for="o in VERBAL_OPTS" :key="o.v" :value="o.v" :label="o.v + ' · ' + o.t"/>
            </el-select>
          </div>
          <div class="flex items-center gap-1.5 text-xs">
            <span class="w-8 text-slate-500">运动</span>
            <el-select v-model="gcsMotor" filterable size="small" class="flex-1" data-testid="emg-score-gcs-motor">
              <el-option v-for="o in MOTOR_OPTS" :key="o.v" :value="o.v" :label="o.v + ' · ' + o.t"/>
            </el-select>
          </div>
        </div>
      </div>

      <!-- ===== RTS ===== -->
      <div class="rounded border border-slate-100 p-2">
        <div class="mb-1.5 flex items-baseline gap-2">
          <span class="text-xs font-bold text-slate-600">RTS 修订创伤评分</span>
          <span class="text-lg font-bold" :class="rtsColor" data-testid="emg-score-rts-total">{{ rtsTotal ?? '—' }}</span>
          <span class="text-[11px] text-slate-400">{{ rtsSeverity }}</span>
        </div>
        <div class="space-y-1.5 text-xs text-slate-500">
          <div class="flex items-center gap-1.5">
            <span class="w-14">GCS</span>
            <span class="flex-1 rounded bg-slate-50 px-2 py-1">{{ gcsTotal != null ? gcsTotal + ' 分 → ' + rtsGcsPts + ' 分' : '先选上方 GCS 三分项' }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="w-14">收缩压</span>
            <el-input v-model.number="rtsSbp" size="small" placeholder="mmHg" data-testid="emg-score-rts-sbp"/>
            <span class="w-10 rounded bg-slate-50 px-1 py-1 text-center">{{ rtsSbpPts ?? '—' }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="w-14">呼吸频率</span>
            <el-input v-model.number="rtsRr" size="small" placeholder="次/分" data-testid="emg-score-rts-rr"/>
            <span class="w-10 rounded bg-slate-50 px-1 py-1 text-center">{{ rtsRrPts ?? '—' }}</span>
          </div>
        </div>
      </div>

      <!-- ===== REMS ===== -->
      <div class="rounded border border-slate-100 p-2">
        <div class="mb-1.5 flex items-baseline gap-2">
          <span class="text-xs font-bold text-slate-600">REMS 急诊死亡风险</span>
          <span class="text-lg font-bold" :class="remsColor" data-testid="emg-score-rems-total">{{ remsTotal ?? '—' }}</span>
          <span class="text-[11px] text-slate-400">/26 · 分高预后差</span>
        </div>
        <div class="grid grid-cols-2 gap-1.5 text-xs text-slate-500">
          <div class="flex items-center gap-1">
            <span class="w-9">年龄</span>
            <span class="flex-1 rounded bg-slate-50 px-2 py-1" data-testid="emg-score-rems-age">{{ patient?.age ?? '—' }}（{{ remsAgePts ?? '—' }}）</span>
          </div>
          <div class="flex items-center gap-1">
            <span class="w-9">MAP</span>
            <span class="flex-1 rounded bg-slate-50 px-2 py-1">{{ remsMap ?? '—' }}（{{ remsMapPts ?? '—' }}）</span>
          </div>
          <div class="flex items-center gap-1">
            <span class="w-9">心率</span>
            <el-input v-model.number="remsHr" size="small" placeholder="bpm"/>
            <span class="w-8 text-center">{{ remsHrPts ?? '—' }}</span>
          </div>
          <div class="flex items-center gap-1">
            <span class="w-9">呼吸</span>
            <span class="flex-1 rounded bg-slate-50 px-2 py-1">{{ rtsRr ?? '—' }}（{{ remsRrPts ?? '—' }}）</span>
          </div>
          <div class="flex items-center gap-1">
            <span class="w-9">SpO₂%</span>
            <el-input v-model.number="remsSpo2" size="small" placeholder="%" data-testid="emg-score-rems-spo2"/>
            <span class="w-8 text-center">{{ remsSpo2Pts ?? '—' }}</span>
          </div>
          <div class="flex items-center gap-1">
            <span class="w-9">GCS</span>
            <span class="flex-1 rounded bg-slate-50 px-2 py-1">{{ gcsTotal ?? '—' }}（{{ remsGcsPts ?? '—' }}）</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue';

const props = defineProps({
  patient: {type: Object, required: true},
  editable: {type: Boolean, default: true},
  /** 预填体征来源：父组件传入 {sbp, dbp, rr, hr, spo2}（病历表单/分诊体征 JSON 已解析的值） */
  vitals: {type: Object, default: () => ({})},
  /** 是否可写回病历（未接诊/已转归置灰） */
  canWrite: {type: Boolean, default: true},
});
const emit = defineEmits(['write']);

// ---------- GCS 分项 ----------
const EYE_OPTS = [
  {v: 4, t: '自动睁眼'}, {v: 3, t: '呼唤睁眼'}, {v: 2, t: '刺痛睁眼'}, {v: 1, t: '无反应'},
];
const VERBAL_OPTS = [
  {v: 5, t: '回答正确'}, {v: 4, t: '回答错误'}, {v: 3, t: '语无伦次'}, {v: 2, t: '只能发声'}, {v: 1, t: '无反应'},
];
const MOTOR_OPTS = [
  {v: 6, t: '遵嘱动作'}, {v: 5, t: '刺痛定位'}, {v: 4, t: '刺痛躲避'}, {v: 3, t: '刺痛屈曲'},
  {v: 2, t: '刺痛过伸'}, {v: 1, t: '无反应'},
];
const gcsEye = ref(null);
const gcsVerbal = ref(null);
const gcsMotor = ref(null);
const gcsTotal = computed(() => {
  const vals = [gcsEye.value, gcsVerbal.value, gcsMotor.value];
  return vals.every((v) => v != null) ? vals.reduce((a, b) => a + b, 0) : null;
});
const gcsSeverity = computed(() => {
  if (gcsTotal.value == null) return '3~15 分';
  if (gcsTotal.value >= 13) return '轻度';
  if (gcsTotal.value >= 9) return '中度';
  return '重度（≤8 分）';
});
const gcsColor = computed(() => {
  if (gcsTotal.value == null) return 'text-slate-300';
  if (gcsTotal.value <= 8) return 'text-red-600';
  if (gcsTotal.value <= 12) return 'text-amber-600';
  return 'text-emerald-600';
});

// ---------- RTS ----------
const rtsSbp = ref(null);
const rtsRr = ref(null);
const rtsGcsPts = computed(() => {
  if (gcsTotal.value == null) return null;
  if (gcsTotal.value >= 13) return 4;
  if (gcsTotal.value >= 9) return 3;
  if (gcsTotal.value >= 6) return 2;
  if (gcsTotal.value >= 4) return 1;
  return 0;
});
const rtsSbpPts = computed(() => {
  const v = rtsSbp.value;
  if (v == null || v === '') return null;
  if (v >= 90) return 4;
  if (v >= 76) return 3;
  if (v >= 50) return 2;
  if (v >= 1) return 1;
  return 0;
});
const rtsRrPts = computed(() => {
  const v = rtsRr.value;
  if (v == null || v === '') return null;
  if (v >= 10 && v <= 29) return 4;
  if (v >= 30) return 3;
  if (v >= 6) return 2;
  if (v >= 1) return 1;
  return 0;
});
const rtsTotal = computed(() => {
  const pts = [rtsGcsPts.value, rtsSbpPts.value, rtsRrPts.value];
  return pts.every((p) => p != null) ? pts.reduce((a, b) => a + b, 0) : null;
});
const rtsSeverity = computed(() => {
  if (rtsTotal.value == null) return '0~12 分';
  if (rtsTotal.value >= 12) return '正常';
  if (rtsTotal.value >= 11) return '中度创伤';
  return '重度创伤（≤10）';
});
const rtsColor = computed(() => {
  if (rtsTotal.value == null) return 'text-slate-300';
  if (rtsTotal.value <= 10) return 'text-red-600';
  if (rtsTotal.value <= 11) return 'text-amber-600';
  return 'text-emerald-600';
});

// ---------- REMS ----------
const remsHr = ref(null);
const remsSpo2 = ref(null);
const remsAgePts = computed(() => {
  const v = props.patient?.age;
  if (v == null) return null;
  if (v < 45) return 0;
  if (v < 55) return 2;
  if (v < 65) return 3;
  if (v < 75) return 5;
  return 6;
});
const remsMap = computed(() => {
  const s = Number(rtsSbp.value);
  const d = Number(props.vitals?.dbp);
  if (!s || !d) return null;
  return Math.round((s + 2 * d) / 3);
});
const remsMapPts = computed(() => {
  const v = remsMap.value;
  if (v == null) return null;
  if (v >= 70 && v <= 109) return 0;
  if ((v >= 50 && v <= 69) || (v >= 110 && v <= 129)) return 2;
  if (v >= 130 && v <= 159) return 3;
  return 4; // <=49 或 >=160
});
const remsHrPts = computed(() => {
  const v = remsHr.value;
  if (v == null || v === '') return null;
  if (v >= 70 && v <= 109) return 0;
  if ((v >= 55 && v <= 69) || (v >= 110 && v <= 139)) return 2;
  if ((v >= 40 && v <= 54) || (v >= 140 && v <= 179)) return 3;
  return 4; // <=39 或 >=180
});
const remsRrPts = computed(() => {
  const v = rtsRr.value;
  if (v == null || v === '') return null;
  if (v >= 12 && v <= 24) return 0;
  if ((v >= 10 && v <= 11) || (v >= 25 && v <= 34)) return 1;
  if (v >= 6 && v <= 9) return 2;
  if (v >= 35 && v <= 49) return 3;
  return 4; // <=5 或 >=50
});
const remsSpo2Pts = computed(() => {
  const v = remsSpo2.value;
  if (v == null || v === '') return null;
  if (v > 89) return 0;
  if (v >= 86) return 1;
  if (v >= 75) return 3;
  return 4;
});
const remsGcsPts = computed(() => {
  if (gcsTotal.value == null) return null;
  if (gcsTotal.value >= 14) return 0;
  if (gcsTotal.value >= 11) return 1;
  if (gcsTotal.value >= 8) return 2;
  if (gcsTotal.value >= 5) return 3;
  return 4;
});
const remsTotal = computed(() => {
  const pts = [remsAgePts.value, remsMapPts.value, remsHrPts.value, remsRrPts.value, remsSpo2Pts.value, remsGcsPts.value];
  return pts.every((p) => p != null) ? pts.reduce((a, b) => a + b, 0) : null;
});
const remsColor = computed(() => {
  if (remsTotal.value == null) return 'text-slate-300';
  if (remsTotal.value >= 14) return 'text-red-600';
  if (remsTotal.value >= 8) return 'text-amber-600';
  return 'text-emerald-600';
});

// ---------- 预填：患者切换时清空；体征源（病历表单/分诊体征）后到时回填空槽位 ----------
watch(
    () => props.patient?.id,
    () => {
      const v = props.vitals || {};
      rtsSbp.value = v.sbp ?? null;
      rtsRr.value = v.rr ?? null;
      remsHr.value = v.hr ?? null;
      remsSpo2.value = v.spo2 ?? null;
      gcsEye.value = null;
      gcsVerbal.value = null;
      gcsMotor.value = null;
    },
    {immediate: true},
);
// 体征源是异步/迟填的（医生先选患者后填体征）——只回填仍为空的项，不覆盖医生手改值
watch(
    () => props.vitals,
    (v) => {
      if (!v) return;
      if (v.sbp != null && (rtsSbp.value == null || rtsSbp.value === '')) rtsSbp.value = v.sbp;
      if (v.rr != null && (rtsRr.value == null || rtsRr.value === '')) rtsRr.value = v.rr;
      if (v.hr != null && (remsHr.value == null || remsHr.value === '')) remsHr.value = v.hr;
      if (v.spo2 != null && (remsSpo2.value == null || remsSpo2.value === '')) remsSpo2.value = v.spo2;
    },
    {deep: true},
);

// ---------- 写入病历 ----------
const writeToRecord = () => {
  const parts = [];
  if (gcsTotal.value != null) {
    parts.push(`GCS ${gcsTotal.value}分（睁眼${gcsEye.value}/语言${gcsVerbal.value}/运动${gcsMotor.value}，${gcsSeverity.value.replace(/（.*/, '')}）`);
  }
  if (rtsTotal.value != null) {
    parts.push(`RTS ${rtsTotal.value}分（GCS ${rtsGcsPts.value}+SBP ${rtsSbpPts.value}+RR ${rtsRrPts.value}，${rtsSeverity.value}）`);
  }
  if (remsTotal.value != null) {
    parts.push(`REMS ${remsTotal.value}/26（年龄${remsAgePts.value}+MAP ${remsMapPts.value}+HR ${remsHrPts.value}+RR ${remsRrPts.value}+SpO₂ ${remsSpo2Pts.value}+GCS ${remsGcsPts.value}）`);
  }
  if (!parts.length) {
    return;
  }
  emit('write', parts.join('；'));
};
</script>
