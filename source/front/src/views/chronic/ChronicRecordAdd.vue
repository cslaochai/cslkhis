<template>
  <el-dialog v-model="visible" destroy-on-close title="慢病建档（建档即认定）" width="560px">
    <el-form label-width="110px">
      <el-form-item label="患者" required>
        <PatientSelect v-model="form.patientId" data-testid="chronic-form-patient" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="慢病病种" required>
        <el-select v-model="form.diseaseCode" data-testid="chronic-form-disease" filterable style="width:100%"
                   @change="onDiseaseChange">
          <el-option v-for="d in diseaseDict" :key="d.dictValue" :label="`${d.dictLabel}（${d.dictValue}）`"
                     :value="d.dictValue"/>
        </el-select>
      </el-form-item>
      <el-form-item label="慢病名称" required>
        <el-input v-model="form.diseaseName" data-testid="chronic-form-disease-name"
                  placeholder="选中病种自动回填，可手动补并发症描述"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" data-testid="chronic-form-remark" placeholder="诊断依据 / 认定材料说明"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" data-testid="chronic-form-save" type="primary" @click="onSubmit">保存并认定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 慢病建档弹框（自 ChronicRecordView 拆出，建档只有新增、无修改）
 *
 * 口径（服务端为准，前端不自判）：
 *  - 建档即认定：保存就是 confirmStatus=1，认定医生=当前登录医生、科室=当前科室。
 *  - 同患者同一慢病编码只允许一条有效档案 —— 重复建档会被服务端拒绝，前端不拦、不静默。
 *  - 慢病字典 his_chronic_disease 的 dict_value 就是 ICD-10 编码，选中即回填 code+name；
 *    该字典只在建档表单里用，按拆分规范由本组件自行加载。
 */
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {upsertChronicRecord} from '@/api/chronic'
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const diseaseDict = ref([])
onMounted(async () => {
  diseaseDict.value = await loadDictDataList(DICT_TYPE.CHRONIC_DISEASE)
})

const submitting = ref(false)
const form = reactive({patientId: null, patientName: '', patientNo: '', diseaseCode: '', diseaseName: '', remark: ''})

// 每次打开清空表单（与拆分前 onCreate 口径一致）
watch(() => props.modelValue, (v) => {
  if (!v) return
  Object.assign(form, {patientId: null, patientName: '', patientNo: '', diseaseCode: '', diseaseName: '', remark: ''})
})

const onPatientSelect = (p) => {
  form.patientName = p?.patientName || ''
  form.patientNo = p?.patientNo || p?.patientNoStr || ''
}
const onDiseaseChange = (code) => {
  const d = diseaseDict.value.find((x) => String(x.dictValue) === String(code))
  form.diseaseName = d?.dictLabel || ''
}
const onSubmit = async () => {
  if (!form.patientId) return ElMessage.warning('请选择患者')
  if (!form.diseaseCode) return ElMessage.warning('请选择慢病病种')
  if (!form.diseaseName?.trim()) return ElMessage.warning('请填写慢病名称')
  submitting.value = true
  try {
    const res = await upsertChronicRecord({
      patientId: form.patientId,
      patientNo: form.patientNo,
      patientName: form.patientName,
      diseaseCode: form.diseaseCode,
      diseaseName: form.diseaseName,
      remark: form.remark?.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('建档已认定')
      visible.value = false
      emit('saved')
    } else ElMessage.error(res.message || '建档失败')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>
