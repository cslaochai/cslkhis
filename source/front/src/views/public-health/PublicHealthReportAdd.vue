<template>
  <el-dialog v-model="visible" destroy-on-close title="新建公卫上报" width="620px">
    <el-form label-width="110px">
      <el-form-item label="患者" required>
        <PatientSelect v-model="form.patientId" data-testid="ph-form-patient" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="上报类型" required>
        <el-select v-model="form.reportType" data-testid="ph-form-type" style="width:100%">
          <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="诊断" required>
        <el-input v-model="form.diagnosis" data-testid="ph-form-diagnosis"
                  placeholder="如 手足口病 / 新型冠状病毒感染"/>
      </el-form-item>
      <el-form-item label="ICD-10">
        <el-input v-model="form.diagnosisCode" data-testid="ph-form-dxcode" placeholder="选填，如 B08.4"/>
      </el-form-item>
      <el-form-item label="上报内容" required>
        <el-input v-model="form.reportContent" :rows="3" data-testid="ph-form-content"
                  placeholder="流行病学史 / 临床表现 / 实验室依据 / 已采取措施"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" data-testid="ph-form-save" type="primary" @click="onSubmit">提交上报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 新建公卫上报弹框（自 PublicHealthReportView 拆出）
 *
 * 服务端兜底的三件事，前端不要自己拼：
 *  - 上报单号 PH+时间戳+4位；
 *  - 上报人取当前登录人（前端留空即可）；
 *  - 审核判在服务端，这里只负责提交进入「待审核」。
 */
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {submitPublicHealth} from '@/api/publicHealth'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 上报类型字典（页面已加载，与筛选下拉同一份数据）
  typeDict: {type: Array, default: () => []},
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitting = ref(false)
const form = reactive({
  patientId: null,
  patientNo: '',
  patientName: '',
  reportType: null,
  diagnosis: '',
  diagnosisCode: '',
  reportContent: ''
})

// 每次打开清空表单（与拆分前 onCreate 的口径一致）
watch(() => props.modelValue, (v) => {
  if (!v) return
  Object.assign(form, {
    patientId: null,
    patientNo: '',
    patientName: '',
    reportType: null,
    diagnosis: '',
    diagnosisCode: '',
    reportContent: ''
  })
})

const onPatientSelect = (p) => {
  form.patientName = p?.patientName || ''
  form.patientNo = p?.patientNo || p?.patientNoStr || ''
}

const onSubmit = async () => {
  if (!form.patientId) return ElMessage.warning('请选择患者')
  if (!form.reportType) return ElMessage.warning('请选择上报类型')
  if (!form.diagnosis?.trim()) return ElMessage.warning('请填写诊断')
  if (!form.reportContent?.trim()) return ElMessage.warning('请填写上报内容')
  submitting.value = true
  try {
    const res = await submitPublicHealth({
      patientId: form.patientId,
      patientNo: form.patientNo,
      patientName: form.patientName,
      reportType: Number(form.reportType),
      diagnosis: form.diagnosis.trim(),
      diagnosisCode: form.diagnosisCode?.trim() || undefined,
      reportContent: form.reportContent.trim(),
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '上报已提交，进入待审核')
      visible.value = false
      emit('saved')
    } else ElMessage.error(res.message || '提交失败')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>
