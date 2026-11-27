<template>
  <el-dialog v-model="visible" append-to-body destroy-on-close title="标本标签打印" width="600px">
    <div v-loading="loading" class="space-y-3">
      <div v-if="label" class="lb-preview">
        <div class="specimen-label-sheet">
          <div class="lb-top">
            <span class="lb-name">{{ label.patientName || '' }}</span>
            <span class="lb-dept">{{ genderText }} {{ ageText }}<span v-if="label.applyDeptName"> · {{ label.applyDeptName }}</span></span>
          </div>
          <div class="lb-code" v-html="barcodeHtml"></div>
          <div class="lb-bottom">
            <span class="lb-no">{{ label.specimenNo || '未分配' }}</span>
            <span class="lb-item">{{ label.laboratoryItemName || '' }}</span>
          </div>
        </div>
      </div>

      <p class="text-xs text-slate-500">
        纸张 50×25mm（贴真空采血管）· 打印缩放必须 100%，边距选「无」——选了「适合页面」条码会被缩放，扫码枪读不出来
      </p>

      <el-alert v-if="label && label.printCount > 0" :closable="false" show-icon type="warning"
                :title="`该标签已打印 ${label.printCount} 次，本次为补打，必须填写原因`"/>

      <el-form v-if="label && label.printCount > 0" label-width="90px">
        <el-form-item label="补打原因" required>
          <el-input v-model="printReason" maxlength="200" placeholder="如：标签贴歪 / 污损 / 打印机卡纸"
                    show-word-limit/>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button :icon="Printer" :loading="printing" type="primary" @click="doPrint">打印</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, nextTick, ref, watch} from 'vue'
import {Printer} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {getSpecimenLabel, printSpecimenLabel} from '@/api/medicaltech'
import {barcodeSvg} from '@/lib/barcode'
import {patientAgeText, patientGenderText} from '@/lib/patientGender'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  recordId: {type: [String, Number], default: null},
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
// 条码宽 47mm / 高 9mm：50mm 纸去掉左右边距刚好贴满，模块宽约 0.28mm，扫码枪能读
const barcodeHtml = computed(() => (label.value?.specimenNo ? barcodeSvg(label.value.specimenNo, 47, 9) : ''))

const load = async () => {
  label.value = null
  printReason.value = ''
  if (!props.recordId) return
  loading.value = true
  try {
    const res = await getSpecimenLabel(props.recordId)
    label.value = res.data || null
  } catch (e) {
    /* 错误已由全局拦截器统一提示 */
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
  if (!props.recordId) return
  if (label.value?.printCount > 0 && !printReason.value.trim()) {
    ElMessage.warning('补打必须填写原因')
    return
  }
  printing.value = true
  try {
    const res = await printSpecimenLabel({
      recordId: props.recordId,
      printReason: printReason.value.trim() || null,
      printerName: localStorage.getItem('his_label_printer') || null,
    })
    label.value = res.data || label.value
    // 先落台账再出纸：台账记的是「这一次真的打了」，不是「点了按钮」
    await nextTick()
    applyPageSize()
    window.print()
  } catch (e) {
    /* 错误已由全局拦截器统一提示 */
    console.error(e)
  } finally {
    printing.value = false
  }
}

const applyPageSize = () => {
  let pageStyle = document.getElementById('specimen-label-page')
  if (!pageStyle) {
    pageStyle = document.createElement('style')
    pageStyle.id = 'specimen-label-page'
    document.head.appendChild(pageStyle)
  }
  pageStyle.textContent = '@page{size:50mm 25mm;margin:0}'
}

window.addEventListener('afterprint', () => {
  const pageStyle = document.getElementById('specimen-label-page')
  if (pageStyle) pageStyle.textContent = '@page{size:auto;margin:0}'
})
</script>

<style scoped>
/* 屏幕上放大看，打印时回 1:1（zoom 在打印媒体里复位） */
.lb-preview {
  padding: 16px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  display: flex;
  justify-content: center;
}

.specimen-label-sheet {
  width: 50mm;
  height: 25mm;
  padding: 1.4mm 1.6mm;
  background: #fff;
  color: #000;
  box-sizing: border-box;
  overflow: hidden;
  zoom: 2.2;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.lb-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 3.1mm;
  line-height: 1.15;
}

.lb-name {
  font-weight: 700;
  white-space: nowrap;
}

.lb-dept {
  font-size: 2.2mm;
  white-space: nowrap;
}

.lb-code {
  margin: 0.6mm 0;
}

.lb-bottom {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 2.5mm;
}

.lb-no {
  font-family: ui-monospace, Consolas, monospace;
  font-weight: 700;
}

.lb-item {
  white-space: nowrap;
}
</style>

<style>
/* 打印隔离：只显标签，隐藏页面与弹框遮罩的一切 */
@media print {
  body * {
    visibility: hidden !important;
  }

  .specimen-label-sheet,
  .specimen-label-sheet * {
    visibility: visible !important;
  }

  .specimen-label-sheet {
    position: fixed;
    left: 0;
    top: 0;
    margin: 0;
    zoom: 1 !important;
  }
}
</style>
