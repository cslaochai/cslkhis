<template>
  <!-- ============ 监护记录弹框 ============ -->
  <el-dialog v-model="visible" :title="monForm.id ? '修改监护记录' : '登记监护记录'" data-testid="icu-monitor-dialog"
             width="920px">
    <el-form :model="monForm" label-width="92px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="入科记录" required>
          <el-select v-model="monForm.stayId" :disabled="!!monForm.id" :fit-input-width="false" class="w-full"
                     data-testid="icu-mon-stay"
                     filterable placeholder="选择在科患者"
                     @change="(v: any) => { monForm.patientName = inStays.find((s: any) => String(s.id) === String(v))?.patientName || '' }">
            <el-option v-for="s in inStays" :key="s.id" :label="`${s.patientName}（${s.bedNo}）${s.stayNo}`"
                       :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="记录时刻" required>
          <el-date-picker v-model="monForm.recordTime" :clearable="false" class="w-full"
                          data-testid="icu-mon-time"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
      </div>

      <div class="text-sm font-medium text-slate-600 mb-1 mt-2">生命体征</div>
      <div class="grid grid-cols-3 md:grid-cols-6 gap-x-3">
        <el-form-item label="体温">
          <el-input v-model="monForm.temperature" data-testid="icu-mon-temp" placeholder="℃"/>
        </el-form-item>
        <el-form-item label="脉搏">
          <el-input v-model="monForm.pulse" data-testid="icu-mon-pulse" placeholder="次/分"/>
        </el-form-item>
        <el-form-item label="呼吸">
          <el-input v-model="monForm.respiratory" data-testid="icu-mon-rr" placeholder="次/分"/>
        </el-form-item>
        <el-form-item label="收缩压">
          <el-input v-model="monForm.sbp" data-testid="icu-mon-sbp" placeholder="mmHg"/>
        </el-form-item>
        <el-form-item label="舒张压">
          <el-input v-model="monForm.dbp" data-testid="icu-mon-dbp" placeholder="mmHg"/>
        </el-form-item>
        <el-form-item label="SpO2">
          <el-input v-model="monForm.spo2" data-testid="icu-mon-spo2" placeholder="%，0~100"/>
        </el-form-item>
      </div>

      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="GCS">
          <div class="flex items-center gap-2">
            <el-select v-model="monForm.gcsEye" :fit-input-width="false" class="w-24" data-testid="icu-mon-gcs-eye"
                       placeholder="睁眼">
              <el-option v-for="v in [4,3,2,1]" :key="v" :label="String(v)" :value="v"/>
            </el-select>
            <el-select v-model="monForm.gcsVerbal" :fit-input-width="false" class="w-24" data-testid="icu-mon-gcs-verbal"
                       placeholder="语言">
              <el-option v-for="v in [5,4,3,2,1]" :key="v" :label="String(v)" :value="v"/>
            </el-select>
            <el-select v-model="monForm.gcsMotor" :fit-input-width="false" class="w-24" data-testid="icu-mon-gcs-motor"
                       placeholder="运动">
              <el-option v-for="v in [6,5,4,3,2,1]" :key="v" :label="String(v)" :value="v"/>
            </el-select>
            <span class="text-sm text-slate-600" data-testid="icu-mon-gcs-total">总分 {{ monGcsTotal ?? '—' }}</span>
          </div>
        </el-form-item>
        <el-form-item label="瞳孔">
          <el-input v-model="monForm.pupil" data-testid="icu-mon-pupil" placeholder="如 双侧3mm对光灵敏"/>
        </el-form-item>
      </div>

      <div class="text-sm font-medium text-slate-600 mb-1 mt-2">呼吸支持与内环境</div>
      <div class="grid grid-cols-3 md:grid-cols-6 gap-x-3">
        <el-form-item label="支持方式">
          <el-select v-model="monForm.ventMode" :fit-input-width="false" class="w-full" clearable data-testid="icu-mon-vent"
                     placeholder="可空">
            <el-option v-for="d in dicts.ventMode" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="FiO2">
          <el-input v-model="monForm.fio2" data-testid="icu-mon-fio2" placeholder="%，21~100"/>
        </el-form-item>
        <el-form-item label="PEEP">
          <el-input v-model="monForm.peep" data-testid="icu-mon-peep" placeholder="cmH2O"/>
        </el-form-item>
        <el-form-item label="CVP">
          <el-input v-model="monForm.cvp" data-testid="icu-mon-cvp" placeholder="cmH2O"/>
        </el-form-item>
      </div>

      <div class="text-sm font-medium text-slate-600 mb-1 mt-2">出入量（液体平衡服务端回算）</div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-x-3">
        <el-form-item label="入量">
          <el-input v-model="monForm.intakeMl" data-testid="icu-mon-intake" placeholder="ml"/>
        </el-form-item>
        <el-form-item label="出量">
          <el-input v-model="monForm.outputMl" data-testid="icu-mon-output" placeholder="ml"/>
        </el-form-item>
        <el-form-item label="尿量">
          <el-input v-model="monForm.urineMl" data-testid="icu-mon-urine" placeholder="ml"/>
        </el-form-item>
        <el-form-item label="平衡">
          <span :class="Number(monBalance) > 0 ? 'text-red-600' : 'text-slate-700'" class="text-sm"
                data-testid="icu-mon-balance">
            {{ monBalance ?? '—' }} ml
          </span>
        </el-form-item>
      </div>

      <div class="text-sm font-medium text-slate-600 mb-1 mt-2">导管（0-无 1-有）</div>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-x-3">
        <el-form-item label="人工气道">
          <el-switch v-model="monForm.hasAirway" :active-value="1" :inactive-value="0" data-testid="icu-mon-airway"/>
        </el-form-item>
        <el-form-item label="中心静脉">
          <el-switch v-model="monForm.hasCvc" :active-value="1" :inactive-value="0" data-testid="icu-mon-cvc"/>
        </el-form-item>
        <el-form-item label="动脉置管">
          <el-switch v-model="monForm.hasArterial" :active-value="1" :inactive-value="0"
                     data-testid="icu-mon-arterial"/>
        </el-form-item>
        <el-form-item label="导尿管">
          <el-switch v-model="monForm.hasCatheter" :active-value="1" :inactive-value="0"
                     data-testid="icu-mon-catheter"/>
        </el-form-item>
        <el-form-item label="引流管">
          <el-switch v-model="monForm.hasDrain" :active-value="1" :inactive-value="0" data-testid="icu-mon-drain"/>
        </el-form-item>
      </div>

      <el-form-item label="病情观察">
        <el-input v-model="monForm.conditionDesc" :rows="2" data-testid="icu-mon-condition" type="textarea"/>
      </el-form-item>
      <el-form-item label="处置">
        <el-input v-model="monForm.handling" :rows="2" data-testid="icu-mon-handling" type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="monForm.remark" data-testid="icu-mon-remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="monSaving" data-testid="icu-mon-ok" type="primary" @click="saveMonitor">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertIcuMonitor} from '@/api/icu';
import {balanceOf, gcsIncomplete, gcsTotalOf, nowText} from '@/lib/icu';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 修改时传入的监护记录行；为空走新增（登记监护）
  row: {type: Object, default: null},
  // 新增时页面预选的入科记录 id（看板/查询条件/在科第一条的兜底链由页面算好传入）
  stayId: {type: [String, Number], default: null},
  // 在科记录候选，页面已加载，两个下拉共用一份
  inStays: {type: Array, default: () => []},
  // 列表也在用的字典由页面加载一次传下来
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

/** 空串统一转 null：未测项不落 0，避免「体温 36」与「没测」混成一列 */
const blankToNull = (obj) => {
  const out = {};
  Object.keys(obj).forEach((k) => {
    const v = obj[k];
    out[k] = v === '' || v === undefined ? null : v;
  });
  return out;
};

const monSaving = ref(false);
const monForm = reactive({
  id: null,
  stayId: null,
  patientName: '',
  recordTime: '',
  temperature: '',
  pulse: '',
  respiratory: '',
  sbp: '',
  dbp: '',
  spo2: '',
  gcsEye: null,
  gcsVerbal: null,
  gcsMotor: null,
  pupil: '',
  cvp: '',
  ventMode: null,
  fio2: '',
  peep: '',
  intakeMl: '',
  outputMl: '',
  urineMl: '',
  hasAirway: 0,
  hasCvc: 0,
  hasArterial: 0,
  hasCatheter: 0,
  hasDrain: 0,
  conditionDesc: '',
  handling: '',
  remark: '',
});
const monGcsTotal = computed(() => gcsTotalOf(monForm));
const monBalance = computed(() => balanceOf(monForm.intakeMl, monForm.outputMl));
const emptyMonitor = () => {
  Object.assign(monForm, {
    id: null, stayId: null, patientName: '', recordTime: nowText(),
    temperature: '', pulse: '', respiratory: '', sbp: '', dbp: '', spo2: '',
    gcsEye: null, gcsVerbal: null, gcsMotor: null, pupil: '', cvp: '',
    ventMode: null, fio2: '', peep: '', intakeMl: '', outputMl: '', urineMl: '',
    hasAirway: 0, hasCvc: 0, hasArterial: 0, hasCatheter: 0, hasDrain: 0,
    conditionDesc: '', handling: '', remark: '',
  });
};
watch(() => props.modelValue, (v) => {
  if (!v) return;
  const row = props.row;
  if (row) {
    emptyMonitor();
    Object.assign(monForm, {
      id: row.id,
      stayId: row.stayId,
      patientName: row.patientName,
      recordTime: row.recordTime,
      temperature: row.temperature ?? '',
      pulse: row.pulse ?? '',
      respiratory: row.respiratory ?? '',
      sbp: row.sbp ?? '',
      dbp: row.dbp ?? '',
      spo2: row.spo2 ?? '',
      gcsEye: row.gcsEye ?? null,
      gcsVerbal: row.gcsVerbal ?? null,
      gcsMotor: row.gcsMotor ?? null,
      pupil: row.pupil ?? '',
      cvp: row.cvp ?? '',
      ventMode: row.ventMode ?? null,
      fio2: row.fio2 ?? '',
      peep: row.peep ?? '',
      intakeMl: row.intakeMl ?? '',
      outputMl: row.outputMl ?? '',
      urineMl: row.urineMl ?? '',
      hasAirway: Number(row.hasAirway || 0),
      hasCvc: Number(row.hasCvc || 0),
      hasArterial: Number(row.hasArterial || 0),
      hasCatheter: Number(row.hasCatheter || 0),
      hasDrain: Number(row.hasDrain || 0),
      conditionDesc: row.conditionDesc ?? '',
      handling: row.handling ?? '',
      remark: row.remark ?? '',
    });
    return;
  }
  emptyMonitor();
  const target = props.stayId;
  if (target) {
    monForm.stayId = target;
    monForm.patientName = props.inStays.find((s) => String(s.id) === String(target))?.patientName || '';
  }
});
const saveMonitor = async () => {
  if (!monForm.stayId)
    return ElMessage.warning('请选择入科记录');
  if (!monForm.recordTime)
    return ElMessage.warning('请选择记录时刻');
  if (gcsIncomplete(monForm))
    return ElMessage.warning('GCS 睁眼/语言/运动需同时填写，或全部留空');
  monSaving.value = true;
  try {
    const payload = blankToNull({
      id: monForm.id,
      stayId: monForm.stayId,
      recordTime: monForm.recordTime,
      temperature: monForm.temperature,
      pulse: monForm.pulse,
      respiratory: monForm.respiratory,
      sbp: monForm.sbp,
      dbp: monForm.dbp,
      spo2: monForm.spo2,
      gcsEye: monForm.gcsEye,
      gcsVerbal: monForm.gcsVerbal,
      gcsMotor: monForm.gcsMotor,
      pupil: monForm.pupil,
      cvp: monForm.cvp,
      ventMode: monForm.ventMode,
      fio2: monForm.fio2,
      peep: monForm.peep,
      intakeMl: monForm.intakeMl,
      outputMl: monForm.outputMl,
      urineMl: monForm.urineMl,
      hasAirway: monForm.hasAirway,
      hasCvc: monForm.hasCvc,
      hasArterial: monForm.hasArterial,
      hasCatheter: monForm.hasCatheter,
      hasDrain: monForm.hasDrain,
      conditionDesc: monForm.conditionDesc,
      handling: monForm.handling,
      remark: monForm.remark,
    });
    if (payload.id == null)
      delete payload.id;
    const res = await upsertIcuMonitor(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '监护记录已保存');
      visible.value = false;
      emit('saved');
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    monSaving.value = false;
  }
};
</script>
