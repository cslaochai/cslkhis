<template>
  <el-dialog v-model="visible" destroy-on-close title="目标性监测登记" width="520px">
    <el-form label-width="90px">
      <el-form-item label="患者">
        <PatientSelect v-model="monitorForm.patientId" data-testid="monitor-patient"
                       @clear="monitorAnchor.inpOptions = []"
                       @select="onMonitorPatientSelect"/>
      </el-form-item>
      <el-form-item label="住院记录">
        <el-select v-model="monitorAnchor.inpId" data-testid="monitor-inp" filterable placeholder="选择住院记录（必选）"
                   style="width:100%">
          <el-option v-for="a in monitorAnchor.inpOptions" :key="a.admissionId"
                     :label="`${String(a.admissionId).slice(-6)} ${a.deptName || ''}`" :value="a.admissionId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="监测类型">
        <el-select v-model="monitorForm.monitorType" data-testid="monitor-type" filterable style="width:100%">
          <el-option v-for="d in monitorTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
        </el-select>
      </el-form-item>
      <el-form-item label="置入日期">
        <el-date-picker v-model="monitorForm.insertDate" data-testid="monitor-insert-date" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="monitorForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'emr:infectionMonitor:add'" data-testid="monitor-save" type="primary" @click="onSaveMonitor">
        保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 目标性监测登记弹窗（自 InfectionMonitorView 拆出，登记只有新增）
 *
 * 住院记录锚点在选患者后自取（导管相关监测必须关联住院次）；
 * 监测类型字典由页面统一加载后传入（筛选下拉同一份数据）。
 */
import {computed, reactive, watch} from 'vue'
import {ElMessage} from 'element-plus'
import PatientSelect from '@/components/his/PatientSelect.vue'
import request from '@/api/request'
import {monitorAdd} from '@/api/infectionMonitor'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 监测类型字典（页面已加载）
  monitorTypeDict: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const monitorAnchor = reactive({inpOptions: [], inpId: null})
const monitorForm = reactive({patientId: null, patientName: '', monitorType: null, insertDate: '', remark: ''})

// 每次打开重置（与拆分前 onMonitorCreate 一致）
watch(() => props.modelValue, (v) => {
  if (!v) return
  Object.assign(monitorForm, {patientId: null, patientName: '', monitorType: null, insertDate: '', remark: ''})
  monitorAnchor.inpOptions = [];
  monitorAnchor.inpId = null
})

const onMonitorPatientSelect = async (p) => {
  monitorForm.patientName = p?.patientName || ''
  monitorAnchor.inpOptions = [];
  monitorAnchor.inpId = null
  if (!p?.id) return
  try {
    const res = await request.get('/patient/inpatient/listPage', {params: {patientId: p.id, pageNum: 1, pageSize: 20}})
    if (res.code === 200) monitorAnchor.inpOptions = res.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

const onSaveMonitor = async () => {
  if (!monitorForm.patientId) return ElMessage.warning('请选择患者')
  if (!monitorAnchor.inpId) return ElMessage.warning('导管相关监测必须关联住院记录')
  if (!monitorForm.monitorType) return ElMessage.warning('请选择监测类型')
  if (!monitorForm.insertDate) return ElMessage.warning('请选择置入日期')
  try {
    const res = await monitorAdd({
      patientId: monitorForm.patientId, monitorType: monitorForm.monitorType,
      inpId: monitorAnchor.inpId, insertDate: monitorForm.insertDate, remark: monitorForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '监测登记已保存')
      visible.value = false
      emit('saved')
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) {
    console.error(e)
  }
}
</script>
