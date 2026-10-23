<template>
  <el-dialog
      v-model="visible"
      title="分诊腕带打印"
      width="420px"
      append-to-body
      :close-on-click-modal="false">
    <div class="wristband-sheet" ref="sheetRef">
      <div class="wb-band" :style="{ background: bandColor }">
        <span class="wb-level-text">{{ levelLabel }}</span>
        <span v-if="row.greenChannel" class="wb-green">{{ row.greenChannel }}</span>
      </div>
      <div class="wb-body">
        <div class="wb-name">{{ row.patientName || '—' }}</div>
        <div class="wb-sub">{{ genderText }} · {{ row.age != null ? row.age + '岁' : '—' }}</div>
        <div class="wb-row">
          <span class="wb-k">急诊号</span>
          <span class="wb-v wb-mono">{{ row.emergencyNo || '—' }}</span>
        </div>
        <div class="wb-row">
          <span class="wb-k">科室</span>
          <span class="wb-v">{{ row.deptName || '—' }}</span>
        </div>
        <div class="wb-row">
          <span class="wb-k">血型</span>
          <span class="wb-v">{{ patient.bloodType || '待查' }}</span>
        </div>
        <div class="wb-row wb-allergy">
          <span class="wb-k">过敏史</span>
          <span class="wb-v">{{ patient.allergyHistory || '无记录' }}</span>
        </div>
        <div class="wb-row">
          <span class="wb-k">登记时间</span>
          <span class="wb-v">{{ createTimeText }}</span>
        </div>
      </div>
      <div class="wb-foot">长沙麓康医院 · 急诊分诊</div>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :icon="Printer" @click="doPrint">打印腕带</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {Printer} from '@element-plus/icons-vue'
import {getPatientFullDetail} from '@/api/patient'
import {patientGenderText} from '@/lib/patientGender'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: () => ({})},
  triageLevelMap: {type: Object, default: () => ({})},
})
const emit = defineEmits(['update:modelValue'])
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const patient = ref({})
const sheetRef = ref(null)

// 真实 HIS 四级分诊色：I 红 / II 橙 / III 黄 / IV 绿
const TRIAGE_BAND = {1: '#dc2626', 2: '#ea580c', 3: '#ca8a04', 4: '#16a34a'}
const bandColor = computed(() => TRIAGE_BAND[props.row.triageLevel] || '#64748b')
const levelLabel = computed(
    () => props.triageLevelMap?.[props.row.triageLevel]?.label || ('分级 ' + props.row.triageLevel),
)
const genderText = computed(() => patientGenderText(props.row.gender))
const createTimeText = computed(() => {
  const t = props.row.createTime || props.row.admissionTime || ''
  return (String(t).replace('T', ' ').slice(0, 19)) || '—'
})

const loadPatient = async () => {
  patient.value = {}
  if (props.row && props.row.patientId) {
    try {
      // 腕带要印血型 / 过敏史：主档详情接口带回（零后端改动）。拉不到不弹错、不挡打印主线
      const res = await getPatientFullDetail(props.row.patientId)
      patient.value = res.data || {}
    } catch {
      patient.value = {}
    }
  }
}

watch(
    () => props.modelValue,
    (v) => {
      if (v) loadPatient()
    },
)

const doPrint = () => {
  window.print()
}
</script>

<style scoped>
.wristband-sheet {
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}
.wb-band {
  color: #fff;
  padding: 8px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
}
.wb-level-text {
  font-size: 16px;
}
.wb-green {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
}
.wb-body {
  padding: 12px;
}
.wb-name {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
}
.wb-sub {
  color: #475569;
  margin: 2px 0 8px;
}
.wb-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed #e2e8f0;
  font-size: 13px;
}
.wb-k {
  color: #64748b;
}
.wb-v {
  color: #0f172a;
  font-weight: 600;
}
.wb-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.wb-allergy .wb-v {
  color: #dc2626;
}
.wb-foot {
  text-align: center;
  color: #94a3b8;
  font-size: 12px;
  padding: 8px;
  background: #f8fafc;
}
</style>

<style>
/* 打印隔离：只显腕带，隐藏页面 / 弹框遮罩等其余一切 */
@media print {
  body * {
    visibility: hidden !important;
  }
  .wristband-sheet,
  .wristband-sheet * {
    visibility: visible !important;
  }
  .wristband-sheet {
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    max-width: 380px;
    margin: 0;
    border: none;
  }
}
</style>
