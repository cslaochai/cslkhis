<template>
  <el-dialog v-model="visible" append-to-body destroy-on-close title="住院腕带打印" width="760px">
    <div v-loading="loading" class="space-y-3">
      <div v-if="label" class="wb-preview">
        <div class="wristband-sheet">
          <div class="wb-left">
            <div class="wb-line1">
              <span class="wb-name">{{ label.patientName || '' }}</span>
              <span class="wb-dem">{{ genderText }} · {{ ageText }}</span>
            </div>
            <div class="wb-line2">
              <span class="wb-k">住院号</span>
              <span class="wb-v wb-mono">{{ label.admissionNo || '' }}</span>
              <span class="wb-k">床号</span>
              <span class="wb-v">{{ bedText }}</span>
            </div>
            <div class="wb-line2">
              <span class="wb-k">科室</span>
              <span class="wb-v">{{ wardText }}</span>
            </div>
            <div class="wb-line2">
              <span class="wb-k">血型</span>
              <span class="wb-v">{{ label.bloodType || '待查' }}</span>
              <span class="wb-k">过敏</span>
              <span class="wb-v wb-allergy">{{ label.allergyHistory || '无记录' }}</span>
            </div>
          </div>
          <div class="wb-right">
            <div v-if="barcodeHtml" v-html="barcodeHtml"></div>
            <div class="wb-no">{{ label.admissionNo || '' }}</div>
          </div>
        </div>
      </div>

      <p class="text-xs text-slate-500">
        纸张 250×30mm（住院患者腕带，条码码值取住院号）· 打印缩放必须 100%，边距选「无」——选了「适合页面」条码会被缩放，扫码枪读不出来
      </p>

      <el-alert v-if="label && label.printCount > 0" :closable="false" show-icon type="warning"
                :title="`该腕带已打印 ${label.printCount} 次，本次为补打，必须填写原因`"/>

      <el-form v-if="label && label.printCount > 0" label-width="90px">
        <el-form-item label="补打原因" required>
          <el-input v-model="printReason" maxlength="200" placeholder="如：腕带污损 / 字迹掉色 / 扣带损坏"
                    show-word-limit/>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button :icon="Printer" :loading="printing" type="primary" :disabled="!label"
                 @click="doPrint">打印腕带
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, nextTick, ref, watch} from 'vue'
import {Printer} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
// 腕带归住院侧：入科后佩戴的身份标识，与急诊分诊的临时腕带是两件事
import {getWristband, printWristband} from '@/api/inpatient'
import {barcodeSvg} from '@/lib/barcode'
import {patientAgeText, patientGenderText} from '@/lib/patientGender'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  admissionId: {type: [String, Number], default: null},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const label = ref(null)
const loading = ref(false)
const printing = ref(false)
const printReason = ref('')

const genderText = computed(() => patientGenderText(label.value?.gender))
const ageText = computed(() => patientAgeText(label.value?.age))
const bedText = computed(() => label.value?.bedNo || '未分床')
const wardText = computed(() => [label.value?.deptName, label.value?.wardName].filter(Boolean).join(' · ') || '—')
// 条码宽 62mm / 高 12mm：横向腕带右侧留白刚好放下，模块宽约 0.29mm，扫码枪能读
const barcodeHtml = computed(() => (label.value?.admissionNo ? barcodeSvg(label.value.admissionNo, 62, 12) : ''))

const load = async () => {
  label.value = null
  printReason.value = ''
  if (!props.admissionId) return
  loading.value = true
  try {
    const res = await getWristband(props.admissionId)
    label.value = res.data || null
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(
    () => props.modelValue,
    (v) => {
      if (v) load()
    },
)

const doPrint = async () => {
  if (!props.admissionId) return
  if (label.value?.printCount > 0 && !printReason.value.trim()) {
    ElMessage.warning('补打必须填写原因')
    return
  }
  printing.value = true
  try {
    const res = await printWristband({
      admissionId: props.admissionId,
      printReason: printReason.value.trim() || null,
      printerName: localStorage.getItem('his_label_printer') || null,
    })
    label.value = res.data || label.value
    // 先落台账再出纸：台账记的是「这一次真的打了」，不是「点了按钮」
    await nextTick()
    applyPageSize()
    window.print()
  } catch (e) {
    console.error(e)
  } finally {
    printing.value = false
  }
}

const applyPageSize = () => {
  let pageStyle = document.getElementById('inpatient-wristband-page')
  if (!pageStyle) {
    pageStyle = document.createElement('style')
    pageStyle.id = 'inpatient-wristband-page'
    document.head.appendChild(pageStyle)
  }
  pageStyle.textContent = '@page{size:250mm 30mm;margin:0}'
}

window.addEventListener('afterprint', () => {
  const pageStyle = document.getElementById('inpatient-wristband-page')
  if (pageStyle) pageStyle.textContent = '@page{size:auto;margin:0}'
})
</script>

<style scoped>
.wb-preview {
  padding: 16px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  overflow-x: auto;
}

.wristband-sheet {
  width: 250mm;
  height: 30mm;
  padding: 2mm 3mm;
  background: #fff;
  color: #000;
  box-sizing: border-box;
  overflow: hidden;
  /* 250mm 实物约 945px，弹框装不下：屏幕预览缩到 0.62，打印时复位成 1:1（见下方打印媒体段） */
  zoom: 0.62;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  display: flex;
  align-items: center;
  gap: 4mm;
  border: 0.3mm dashed #94a3b8;
}

.wb-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.2mm;
}

.wb-line1 {
  display: flex;
  align-items: baseline;
  gap: 3mm;
}

.wb-name {
  font-size: 7mm;
  font-weight: 800;
  line-height: 1.05;
  white-space: nowrap;
}

.wb-dem {
  font-size: 3mm;
}

.wb-line2 {
  display: flex;
  align-items: baseline;
  gap: 1.6mm;
  font-size: 2.9mm;
  white-space: nowrap;
  overflow: hidden;
}

.wb-k {
  color: #475569;
}

.wb-v {
  font-weight: 700;
}

.wb-mono {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 4mm;
  letter-spacing: 0.3mm;
}

.wb-allergy {
  color: #b91c1c;
}

.wb-right {
  width: 64mm;
  text-align: center;
  flex: none;
}

.wb-no {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 2.8mm;
  letter-spacing: 0.4mm;
  font-weight: 700;
}
</style>

<style>
/* 打印隔离：只显腕带，隐藏页面与弹框遮罩的一切 */
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
    margin: 0;
    border: none;
    zoom: 1 !important;
  }
}
</style>
