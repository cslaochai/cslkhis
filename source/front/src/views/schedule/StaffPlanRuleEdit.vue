<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="140px">
      <el-form-item label="排班单元类型">
        <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
          <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="orgIdRequired" label="所属科室">
        <el-select v-model="formData.orgId" filterable placeholder="选择科室" style="width: 100%">
          <el-option v-for="o in deptOptions" :key="o.id" :label="o.name" :value="o.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="班次">
        <el-select v-model="formData.shiftId" clearable placeholder="留空 = 该单元全部班次共用" style="width: 100%">
          <el-option v-for="s in shiftOptions" :key="s.id" :label="s.shiftName" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="岗位类别" prop="staffType">
        <div data-testid="pr-staff-type">
          <el-select v-model="formData.staffType" style="width: 100%">
            <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="最低在岗人数" prop="minStaff">
        <div data-testid="pr-min-staff">
          <el-input-number v-model="formData.minStaff" :max="999" :min="0"/>
        </div>
      </el-form-item>
      <el-form-item label="最高在岗人数">
        <el-input-number v-model="formData.maxStaff" :max="999" :min="0" placeholder="0 或留空 = 不限"/>
      </el-form-item>
      <el-form-item label="周工时上限">
        <el-input-number v-model="formData.maxWeekHours" :max="200" :min="0" :precision="1" :step="4"/>
      </el-form-item>
      <el-form-item label="连续夜班天数上限">
        <el-input-number v-model="formData.maxConsecutiveNightDays" :max="30" :min="0"/>
      </el-form-item>
      <el-form-item label="连续上班天数上限">
        <el-input-number v-model="formData.maxConsecutiveWorkDays" :max="30" :min="0"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" data-testid="pr-submit" type="primary" @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {staffPlanRuleUpsert} from '@/api/staffPlanRule'
import {getDepartmentSelectList} from '@/api/system'
import {getShiftSelectList} from '@/api/appoint'
import {ORG_UNIT_TYPE_HOSPITAL, ORG_UNIT_TYPE_OPTIONS} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'

const SHIFT_USE_SCOPE_GENERAL = 4

/**
 * 新增 / 修改人力标准：后端是同一个 staffPlanRuleUpsert，
 * row 为空走新增、有值走修改，标题与成功提示跟着变。
 * 科室与班次是**表单专用候选**，在本组件里懒加载，页面不再持有。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const formRef = ref(null)
const deptOptions = ref([])
const shiftOptions = ref([])
const formData = reactive({
  id: null, orgType: ORG_UNIT_TYPE_HOSPITAL, orgId: null, shiftId: null, staffType: 2,
  minStaff: 1, maxStaff: null, maxWeekHours: null, maxConsecutiveNightDays: null,
  maxConsecutiveWorkDays: null, status: 1, remark: '',
})
const rules = {
  staffType: [{required: true, message: '请选择岗位类别', trigger: 'change'}],
  minStaff: [{required: true, message: '请填写最低在岗人数', trigger: 'blur'}],
}

const dialogTitle = computed(() => (formData.id ? '修改人力标准' : '新增人力标准'))
const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)

const resetForm = () => {
  formData.id = null
  formData.orgType = ORG_UNIT_TYPE_HOSPITAL
  formData.orgId = null
  formData.shiftId = null
  formData.staffType = 2
  formData.minStaff = 1
  formData.maxStaff = null
  formData.maxWeekHours = null
  formData.maxConsecutiveNightDays = null
  formData.maxConsecutiveWorkDays = null
  formData.status = 1
  formData.remark = ''
}

let optionsLoaded = false
const ensureOptions = async () => {
  if (optionsLoaded) return
  try {
    const [dRes, sRes] = await Promise.all([
      getDepartmentSelectList({}),
      getShiftSelectList({status: 1, useScope: SHIFT_USE_SCOPE_GENERAL}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id ?? d.deptId, name: d.deptName ?? d.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName}))
    optionsLoaded = true
  } catch (e) {
    ElMessage.error(e.message || '加载下拉数据失败')
  }
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  await ensureOptions()
  const row = props.row
  if (!row) return
  Object.assign(formData, {
    id: row.id,
    orgType: row.orgType,
    orgId: row.orgId || null,
    // 0 = 该单元全部班次共用（合计行），下拉里没有这条，单独补
    shiftId: row.shiftId && Number(row.shiftId) !== 0 ? row.shiftId : null,
    staffType: row.staffType,
    minStaff: row.minStaff,
    maxStaff: row.maxStaff,
    maxWeekHours: row.maxWeekHours,
    maxConsecutiveNightDays: row.maxConsecutiveNightDays,
    maxConsecutiveWorkDays: row.maxConsecutiveWorkDays,
    status: row.status ?? 1,
    remark: row.remark || '',
  })
  if (row.shiftId && Number(row.shiftId) !== 0
      && !shiftOptions.value.some((s) => String(s.id) === String(row.shiftId))) {
    shiftOptions.value = [{id: row.shiftId, shiftName: row.shiftName || '已停用班次'}, ...shiftOptions.value]
  }
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
    const payload = {...formData, orgId: orgIdRequired.value ? formData.orgId : null}
    const res = await staffPlanRuleUpsert(payload)
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
