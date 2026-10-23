<template>
  <el-dialog v-model="visible" destroy-on-close title="分诊复评" width="720px">
    <div class="mb-3 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs text-slate-600">
      <p><b>{{ row?.patientName }}</b>（{{ row?.emergencyNo }}）当前分级
        <span :class="['rounded border px-1.5 py-0.5 font-medium', triageLevelMap[row?.triageLevel]?.color || '']">
          {{ triageLevelMap[row?.triageLevel]?.label || '未定级' }}
        </span>
        已候诊 {{ row?.waitMinutes ?? 0 }} 分钟，应接诊时限 {{ row?.targetSeeMinutes ?? '-' }} 分钟。
      </p>
      <p class="mt-1 text-slate-400">
        分诊规范：候诊超过响应时限应再次评估分级，级别<b>只升不降</b>。复评后时限按新级别重算，流水留痕。
      </p>
    </div>

    <el-form :model="reevalForm" label-position="left">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="复评后分级" required>
          <el-select v-model="reevalForm.triageLevel" class="w-full">
            <!-- 只列不轻于当前级别的选项：降级选项不出现在界面上（后端还有第二道） -->
            <el-option v-for="(v, k) in levelOptions" :key="k" :label="v.label" :value="Number(k)"/>
          </el-select>
          <div class="mt-1 text-xs text-slate-400">只能选与当前同级或更危重的级别</div>
        </el-form-item>
        <el-form-item label="意识状态" required>
          <el-select v-model="reevalForm.consciousness" class="w-full" placeholder="复评必须重采">
            <el-option v-for="c in CONSCIOUSNESS_OPTIONS" :key="c.value" :label="c.label" :value="c.value"/>
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="生命体征">
        <div class="grid grid-cols-3 gap-2">
          <el-input-number v-model="vitalForm.temperature" :max="45" :min="30" :precision="1" :step="0.1"
                           class="!w-full" controls-position="right" placeholder="体温℃" size="small"/>
          <el-input-number v-model="vitalForm.pulse" :max="300" :min="0" class="!w-full" controls-position="right"
                           placeholder="脉搏" size="small"/>
          <el-input-number v-model="vitalForm.respiratory" :max="100" :min="0" class="!w-full"
                           controls-position="right" placeholder="呼吸" size="small"/>
          <el-input-number v-model="vitalForm.systolic" :max="300" :min="0" class="!w-full" controls-position="right"
                           placeholder="收缩压" size="small"/>
          <el-input-number v-model="vitalForm.diastolic" :max="200" :min="0" class="!w-full"
                           controls-position="right" placeholder="舒张压" size="small"/>
          <el-input-number v-model="vitalForm.spo2" :max="100" :min="0" class="!w-full" controls-position="right"
                           placeholder="血氧%" size="small"/>
        </div>
        <div class="mt-1 text-xs text-slate-400">预填登记时的体征；未复测可保持不变，MEWS 按当前值 + 新意识重算</div>
      </el-form-item>

      <div class="mb-4 rounded-lg border border-slate-200 bg-slate-50/60 p-3" data-testid="emg-reeval-mews">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-700">MEWS 评分（复算）</p>
          <span v-if="mews" :class="['rounded border px-2 py-0.5 text-xs font-bold', mewsLevelClass(mews.levelText)]">
            {{ mews.score }} 分 · {{ mews.levelText }}
          </span>
          <span v-else class="text-xs text-slate-400">选择意识后自动算分</span>
        </div>
        <div v-if="mews" class="mt-2 flex flex-wrap gap-x-3 gap-y-1 text-xs">
          <span v-for="it in mews.items" :key="it.name"
                :class="it.score >= 2 ? 'font-bold text-red-600' : it.score === 1 ? 'text-amber-600' : 'text-slate-500'">
            {{ it.name }} {{ it.valueText }}（{{ it.score }}分）
          </span>
        </div>
      </div>

      <el-form-item label="复评原因" required>
        <el-input v-model="reevalForm.reason" :rows="2" maxlength="500" placeholder="如：候诊 45 分钟无人接诊，复测血压 85/50，上调为 II 级"
                  show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>

    <div v-if="reevalHistory.length" class="rounded-lg border border-slate-200 p-3">
      <p class="mb-1 text-xs font-medium text-slate-600">复评记录（为什么改过级，在这里查）</p>
      <div v-for="h in reevalHistory" :key="h.id" class="border-l-2 border-slate-200 py-1 pl-2 text-xs text-slate-500">
        <span class="text-slate-400">{{ h.createTime }}</span>
        {{ h.triageLevelBeforeText }} → <b class="text-slate-700">{{ h.triageLevelAfterText }}</b>
        （MEWS {{ h.mewsScore }}）· {{ h.reason }} · {{ h.createBy }}
      </div>
    </div>

    <template #footer>
      <el-button v-perm="'opd:emergency:reeval'" data-testid="emg-reeval-submit" type="danger"
                 @click="handleSubmit">提交复评
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEmergencyMewsScore, getEmergencyReevalList, reevaluateEmergency} from '@/api/emergency';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 分诊级别字典由主页面持有（查询条与表格「分诊」列也在用），子组件不许再拉一份
  triageLevelMap: {type: Object, default: () => ({})},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const CONSCIOUSNESS_OPTIONS = [
  {value: 1, label: '清楚'},
  {value: 2, label: '对声音有反应'},
  {value: 3, label: '对疼痛有反应'},
  {value: 4, label: '无反应'},
];

const reevalForm = ref({triageLevel: null, consciousness: null, reason: ''});
const vitalForm = ref({
  temperature: null, pulse: null, respiratory: null, systolic: null, diastolic: null, spo2: null,
});
const mews = ref(null);
const reevalHistory = ref([]);

/** 只升不降的下拉：只列数值 ≤ 当前级别的选项（数字越小越危重） */
const levelOptions = computed(() => {
  const current = props.row?.triageLevel;
  const out = {};
  for (const [k, v] of Object.entries(props.triageLevelMap || {})) {
    if (current == null || Number(k) <= current)
      out[k] = v;
  }
  return out;
});

const mewsLevelClass = (level) => {
  if (level === '极高危')
    return 'bg-red-100 text-red-700 border-red-300';
  if (level === '高危')
    return 'bg-amber-100 text-amber-700 border-amber-300';
  return 'bg-emerald-100 text-emerald-700 border-emerald-300';
};

const parseVitals = (raw) => {
  if (!raw)
    return null;
  try {
    const obj = JSON.parse(raw);
    return {
      temperature: obj.temperature ?? null,
      pulse: obj.pulse ?? null,
      respiratory: obj.respiratory ?? null,
      systolic: obj.systolic ?? null,
      diastolic: obj.diastolic ?? null,
      spo2: obj.spo2 ?? null,
    };
  } catch {
    return null;
  }
};
const buildVitalSigns = () => {
  const src = vitalForm.value;
  const out = {};
  const keys = ['temperature', 'pulse', 'respiratory', 'systolic', 'diastolic', 'spo2'];
  for (const k of keys) {
    const v = src[k];
    if (v !== null && v !== undefined && !Number.isNaN(Number(v)))
      out[k] = Number(v);
  }
  return Object.keys(out).length > 0 ? JSON.stringify(out) : undefined;
};

let mewsTimer = null;
watch([vitalForm, () => reevalForm.value.consciousness], () => {
  if (mewsTimer)
    clearTimeout(mewsTimer);
  mewsTimer = setTimeout(async () => {
    try {
      const res = await getEmergencyMewsScore({
        vitalSigns: buildVitalSigns(),
        consciousness: reevalForm.value.consciousness || undefined,
      });
      mews.value = res.data || null;
    } catch {
      mews.value = null;
    }
  }, 400);
}, {deep: true});

watch(visible, async (open) => {
  if (!open)
    return;
  const row = props.row;
  reevalForm.value = {
    // 默认维持当前级别：复评弹框不该诱导改级，改不改是护士看完体征后的决定
    triageLevel: row?.triageLevel ?? null,
    consciousness: row?.consciousness ?? null,
    reason: '',
  };
  const v = parseVitals(row?.vitalSigns);
  vitalForm.value = v || {temperature: null, pulse: null, respiratory: null, systolic: null, diastolic: null, spo2: null};
  mews.value = null;
  try {
    const res = await getEmergencyReevalList(row?.id);
    reevalHistory.value = res.data || [];
  } catch {
    reevalHistory.value = [];
  }
});

const handleSubmit = async () => {
  if (!reevalForm.value.triageLevel) {
    ElMessage.warning('请选择复评后分级');
    return;
  }
  if (!reevalForm.value.consciousness) {
    ElMessage.warning('请选择意识状态（复评必须重采）');
    return;
  }
  if (!reevalForm.value.reason?.trim()) {
    ElMessage.warning('请填写复评原因（改级必须留下依据）');
    return;
  }
  try {
    await reevaluateEmergency({
      id: props.row.id,
      triageLevel: reevalForm.value.triageLevel,
      consciousness: reevalForm.value.consciousness,
      vitalSigns: buildVitalSigns(),
      reason: reevalForm.value.reason.trim(),
    });
    ElMessage.success('复评成功' + (reevalForm.value.triageLevel < props.row.triageLevel ? '，已通知负责医生' : ''));
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
</script>
