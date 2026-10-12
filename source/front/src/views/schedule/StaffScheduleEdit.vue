<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <el-form-item label="排班日期" prop="scheduleDate">
        <div data-testid="ss-date">
          <el-date-picker
              v-model="formData.scheduleDate"
              placeholder="夜班请填开始日"
              style="width: 100%"
              type="date"
              value-format="YYYY-MM-DD"
          />
        </div>
      </el-form-item>
      <el-form-item label="排班单元类型" prop="orgType">
        <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
          <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="orgIdRequired" :label="formData.orgType === 2 ? '病区' : '科室'" prop="orgId">
        <el-select
            v-model="formData.orgId"
            :placeholder="formData.orgType === 2 ? '选择病区' : '选择科室'"
            filterable
            style="width: 100%"
        >
          <el-option v-for="o in orgIdOptions" :key="o.id" :label="o.name" :value="o.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="排班对象" prop="employeeId">
        <div data-testid="ss-employee">
          <el-select
              v-model="formData.employeeId"
              :loading="empLoading"
              :remote-method="searchEmployee"
              filterable
              placeholder="输入姓名搜索"
              remote
              reserve-keyword
              style="width: 100%"
          >
            <el-option
                v-for="e in empOptions"
                :key="e.id"
                :label="e.deptName ? `${e.empName}（${e.deptName}）` : e.empName"
                :value="e.id"
            />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="出勤状态" prop="dutyStatus">
        <el-select v-model="formData.dutyStatus" style="width: 100%">
          <el-option v-for="o in DUTY_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="needShift" label="班次" prop="shiftId">
        <el-select v-model="formData.shiftId" placeholder="选择班次（时间与工时由班次带出）" style="width: 100%">
          <el-option v-for="s in shiftOptions" :key="s.id" :label="s.shiftName" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item v-else label="班次">
        <el-alert :closable="false" title="休息/请假/培训/停班没有班次：这类行也要落库，否则看不出「今天没排他」是有意还是漏排"
                  type="info"/>
      </el-form-item>
      <el-form-item v-if="needShift" label="响应形态">
        <el-radio-group v-model="formData.attendMode">
          <el-radio v-for="o in ATTEND_MODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="needShift" label="是否出诊">
        <el-switch
            v-model="formData.clinicFlag"
            :active-value="1"
            :disabled="!clinicFlagAllowed"
            :inactive-value="0"
        />
        <span class="ml-2 text-xs text-gray-400">
          {{ clinicFlagAllowed ? '只有医生岗的出诊班会生成门诊号源（非医生岗后端会裁掉）' : '听班不放号：人在院外待命' }}
        </span>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="如：本周三下午参加院感培训" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitLoading" data-testid="ss-submit" type="primary" @click="handleSubmit">确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {staffScheduleUpsert} from '@/api/staffSchedule'
import {getDepartmentSelectList, getEmployeeList} from '@/api/system'
import {getShiftSelectList} from '@/api/appoint'
import {getNurseWardSelectList} from '@/api/nurseSchedule'
import {
  ATTEND_MODE_OPTIONS,
  DUTY_STATUS_OPTIONS,
  DUTY_STATUS_WORK,
  dutyStatusNeedShift,
  ORG_UNIT_TYPE_HOSPITAL,
  ORG_UNIT_TYPE_OPTIONS,
} from '@/lib/staffSchedule'

// 出勤排班走 4-全院通用班次册：按岗位类别选本册班次，别把门诊出诊册的班次混进来
const SHIFT_USE_SCOPE_GENERAL = 4

/**
 * 登记 / 修改排班：后端是同一个 staffScheduleUpsert，
 * row 为空走登记、有值走修改，标题与成功提示跟着变。
 * 科室/病区/班次/排班对象都是**表单专用候选**，在本组件里懒加载，页面不再持有。
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
const formData = reactive({
  id: null, scheduleDate: null, orgType: 1, orgId: null, employeeId: null, employeePostId: null,
  shiftId: null, dutyStatus: DUTY_STATUS_WORK, attendMode: 1, clinicFlag: 0, remark: '',
})
const rules = {
  scheduleDate: [{required: true, message: '请选择排班日期', trigger: 'change'}],
  orgType: [{required: true, message: '请选择排班单元类型', trigger: 'change'}],
  employeeId: [{required: true, message: '请选择排班对象', trigger: 'change'}],
  dutyStatus: [{required: true, message: '请选择出勤状态', trigger: 'change'}],
}

const dialogTitle = computed(() => (formData.id ? '修改排班' : '登记排班'))

// ---------------- 下拉 ----------------
const empOptions = ref([])
const empLoading = ref(false)
const deptOptions = ref([])
const wardOptions = ref([])
const shiftOptions = ref([])

const searchEmployee = async (keyword) => {
  empLoading.value = true
  try {
    const res = await getEmployeeList({empName: keyword || '', pageSize: 50})
    empOptions.value = (res?.data || []).map((e) => ({
      id: e.id, empName: e.empName, deptName: e.deptName,
    }))
  } catch {
    empOptions.value = []
  } finally {
    empLoading.value = false
  }
}

/** 单元候选随「排班单元类型」切换；全院级没有单元，后端要求 org_id=0 */
const orgIdOptions = computed(() => {
  if (formData.orgType === 2) return wardOptions.value
  return deptOptions.value
})
const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)

const needShift = computed(() => dutyStatusNeedShift(formData.dutyStatus))
/**
 * 听班一律不放号：AttendModeEnum.releasesSource —— 给一个在家待命的人放号，患者到了没人看。
 * 「是不是医生岗」这里不判：岗位类别由 员工→岗位→角色→sys_role.staff_type 三级派生，
 * 员工接口只给到 posts、没有 staff_type，前端判就是猜。这一层由服务端 applyStaffType 收口，
 * 传了不合规的出诊标志后端会裁成 0 并在返回文案里说明。
 */
const clinicFlagAllowed = computed(() => Number(formData.attendMode) !== 2)

let optionsLoaded = false
const ensureOptions = async () => {
  if (optionsLoaded) return
  try {
    const [dRes, wRes, sRes] = await Promise.all([
      getDepartmentSelectList({}),
      getNurseWardSelectList({}),
      getShiftSelectList({status: 1, useScope: SHIFT_USE_SCOPE_GENERAL}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id ?? d.deptId, name: d.deptName ?? d.name}))
    wardOptions.value = (wRes?.data || []).map((w) => ({id: w.id ?? w.wardId, name: w.wardName ?? w.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName}))
    optionsLoaded = true
  } catch (e) {
    ElMessage.error(e.message || '加载下拉数据失败')
  }
}

let empLoaded = false
const ensureEmpOptions = async () => {
  if (empLoaded) return
  await searchEmployee('')
  empLoaded = true
}

const resetForm = () => {
  formData.id = null
  formData.scheduleDate = null
  formData.orgType = 1
  formData.orgId = null
  formData.employeeId = null
  formData.employeePostId = null
  formData.shiftId = null
  formData.dutyStatus = DUTY_STATUS_WORK
  formData.attendMode = 1
  formData.clinicFlag = 0
  formData.remark = ''
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  await ensureEmpOptions()
  await ensureOptions()
  const row = props.row
  if (!row) return
  formData.id = row.id
  formData.scheduleDate = row.scheduleDate
  formData.orgType = row.orgType
  formData.orgId = row.orgId || null
  formData.employeeId = row.employeeId
  formData.employeePostId = row.employeePostId || null
  formData.shiftId = row.shiftId || null
  formData.dutyStatus = row.dutyStatus
  formData.attendMode = row.attendMode || 1
  formData.clinicFlag = row.clinicFlag ?? 0
  formData.remark = row.remark || ''
  if (row.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [{id: row.employeeId, empName: row.employeeName, deptName: row.deptName}, ...empOptions.value]
  }
  // 班次可能已被停用而从下拉里剔除，补进去保证回显是班次名而不是空
  if (row.shiftId && !shiftOptions.value.some((s) => String(s.id) === String(row.shiftId))) {
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
    const payload = {
      ...formData,
      orgId: orgIdRequired.value ? formData.orgId : null,
      shiftId: needShift.value ? formData.shiftId : null,
      clinicFlag: clinicFlagAllowed.value ? formData.clinicFlag : 0,
    }
    const res = await staffScheduleUpsert(payload)
    if (res.code === 200) {
      // 超出人力上限时后端不拦但会带文案回来，必须把这句话给到排班员
      ElMessage.success(res.message || (formData.id ? '修改成功' : '登记成功'))
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
