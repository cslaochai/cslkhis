<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="上岗日期" prop="assignDate">
        <el-date-picker v-model="formData.assignDate" type="date" value-format="YYYY-MM-DD"
                        placeholder="选择日期" style="width: 100%" @change="onScopeChange"/>
      </el-form-item>
      <el-form-item label="工位" prop="stationId">
        <el-select v-model="formData.stationId" filterable placeholder="选择排到哪个位" style="width: 100%"
                   @change="onScopeChange">
          <el-option v-for="s in stationOptions" :key="s.id" :label="s.stationName" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="上岗人员" prop="employeeId">
        <el-select v-model="formData.employeeId" filterable :loading="candidateLoading"
                   :disabled="!formData.stationId || !formData.assignDate"
                   :placeholder="formData.stationId && formData.assignDate ? '搜索姓名/工号选人' : '先选上岗日期与工位'"
                   style="width: 100%">
          <el-option v-for="e in candidateOptions" :key="e.employeeId"
                     :label="`${e.employeeName}（${e.empCode || '无工号'}${e.orgName ? '·' + e.orgName : ''}）`"
                     :value="e.employeeId"/>
          <template #empty>
            <div class="px-3 py-2 text-xs text-slate-500">
              当天没有可派的人：请先在护理排班把她这天的班排好并提交审核发布
            </div>
          </template>
        </el-select>
      </el-form-item>
      <el-form-item label="班次">
        <el-select v-model="formData.shiftId" clearable placeholder="选择班次（可空）" style="width: 100%">
          <el-option v-for="s in shiftOptions" :key="s.id"
                     :label="`${shiftOptionLabel(s)} ${s.startTime || ''}~${s.endTime || ''}`" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="是否借调">
        <el-radio-group v-model="formData.isLoan" @change="onLoanChange">
          <el-radio :value="0">本岗</el-radio>
          <el-radio :value="1">跨单元借调</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="formData.isLoan === 1">
        <el-form-item label="借出方类型">
          <el-select v-model="formData.sourceOrgType" style="width: 100%">
            <el-option :value="1" label="科室"/>
            <el-option :value="2" label="病区"/>
          </el-select>
        </el-form-item>
        <el-form-item label="借出方" prop="sourceOrgName">
          <el-input v-model="formData.sourceOrgName" placeholder="如：内科病区 / 门诊内科（登记来源即可）"/>
        </el-form-item>
      </template>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" type="textarea" placeholder="临时说明"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
// 排班员指定派岗（row 为空走新增）；候选人只列当天已发布上班且岗位类别相符的人
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getAssignCandidates, opdAssignUpsert} from '@/api/opdStation'
import {getShiftSelectList} from '@/api/appoint'
import {shiftOptionLabel, SHIFT_SCOPE_OUTPATIENT} from '@/lib/scheduleShift'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 父组件已拉好的启用工位下拉
  stationOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const dialogTitle = ref('派岗')
const formRef = ref(null)
const candidateOptions = ref([])
const candidateLoading = ref(false)
const shiftOptions = ref([])

const formData = reactive({
  id: null, assignDate: '', stationId: null, employeeId: null, shiftId: null,
  isLoan: 0, sourceOrgType: 1, sourceOrgName: '', remark: '',
})
const rules = {
  assignDate: [{required: true, message: '请选择上岗日期', trigger: 'change'}],
  stationId: [{required: true, message: '请选择工位', trigger: 'change'}],
  employeeId: [{required: true, message: '请选择上岗人员', trigger: 'change'}],
  sourceOrgName: [{required: true, message: '请填写借出方', trigger: 'blur'}],
}

const resetForm = () => {
  Object.assign(formData, {
    id: null, assignDate: '', stationId: null, employeeId: null, shiftId: null,
    isLoan: 0, sourceOrgType: 1, sourceOrgName: '', remark: '',
  })
}

const loadShiftOptions = async () => {
  if (shiftOptions.value.length) return
  try {
    const sRes = await getShiftSelectList({status: 1, useScope: SHIFT_SCOPE_OUTPATIENT})
    shiftOptions.value = sRes.data || []
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  }
}

// 候选口径与保存时的校验同源：不在名单里的人不该出现在下拉中，所以换日期/换工位都要重取
const loadCandidates = async () => {
  if (!formData.stationId || !formData.assignDate) {
    candidateOptions.value = []
    return
  }
  candidateLoading.value = true
  try {
    const res = await getAssignCandidates({
      stationId: formData.stationId,
      assignDate: formData.assignDate,
    })
    candidateOptions.value = res.data || []
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
    candidateOptions.value = []
  } finally {
    candidateLoading.value = false
  }
}

const onScopeChange = () => {
  formData.employeeId = null
  loadCandidates()
}

const onLoanChange = (v) => {
  if (v !== 1) {
    formData.sourceOrgType = 1
    formData.sourceOrgName = ''
  }
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  const row = props.row
  if (row) {
    dialogTitle.value = '修改派岗'
    Object.assign(formData, {
      id: row.id,
      assignDate: row.assignDate,
      stationId: row.stationId,
      employeeId: row.employeeId,
      shiftId: row.shiftId || null,
      isLoan: row.isLoan ?? 0,
      sourceOrgType: row.sourceOrgType ?? 1,
      sourceOrgName: row.sourceOrgName || '',
      remark: row.remark || '',
    })
  } else {
    dialogTitle.value = '派岗'
  }
  await loadShiftOptions()
  await loadCandidates()
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const payload = {...formData}
    if (payload.isLoan !== 1) {
      payload.sourceOrgType = null
      payload.sourceOrgName = null
    }
    const res = await opdAssignUpsert(payload)
    if (res.code === 200) {
      ElMessage.success('派岗成功')
      visible.value = false
      emit('saved')
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    submitLoading.value = false
  }
}
</script>
