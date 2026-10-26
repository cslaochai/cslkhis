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
          <el-option v-for="s in shiftOptions" :key="s.id" :label="shiftOptionLabel(s)" :value="s.id"/>
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
        <div class="w-full text-xs text-gray-400">坐班=正常到岗；听班=二线待命，不进在岗名单、不接派单；留院值班=夜间/节假日住宿舍值守</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="如：本周三下午参加院感培训" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" data-testid="ss-submit" type="primary" @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {staffScheduleUpsert, getStaffScheduleUnitSelectList} from '@/api/staffSchedule'
import {getEmployeeList} from '@/api/system'
import {getShiftSelectList} from '@/api/appoint'
import {getNurseWardSelectList} from '@/api/nurseSchedule'
import {shiftOptionLabel} from '@/lib/scheduleShift'
import {
  ATTEND_MODE_OPTIONS,
  DUTY_STATUS_OPTIONS,
  DUTY_STATUS_WORK,
  dutyStatusNeedShift,
  ORG_UNIT_TYPE_DEPT,
  ORG_UNIT_TYPE_HOSPITAL,
  ORG_UNIT_TYPE_OPTIONS,
} from '@/lib/staffSchedule'

// 发布态：本弹框不传、不展示——新增一律后端落草稿（resolvePublishStatus 对没传的新行强制 0），
// 修改已发布行由后端按原态自动迁移（生效态→已变更），杜绝从这里直接选「已发布」绕过审核闸。
// 出诊号源派生行（clinic_flag=1）由门诊排班页产生，本弹框不提供该开关（手工置 1 会被审批中心滤掉）。

/**
 * 登记 / 修改排班：后端是同一个 staffScheduleUpsert，
 * row 为空走登记、有值走修改，标题与成功提示跟着变。
 * 科室/病区/班次/排班对象都是**表单专用候选**，在本组件里懒加载，页面不再持有。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 排班域（1-岗位出勤 2-急诊值班 3-门诊出诊）：科室候选、排班对象候选与保存入参都按域收口
  scope: {type: Number, default: 1},
  // 班次册（biz_shift.use_scope）：岗位出勤用 4-全院通用；门诊出诊域传 1-门诊/急诊册，
  // 否则休息行改回上班时会挂错班次（门诊出诊行的班次必须与号源行同册）
  shiftUseScope: {type: Number, default: 4},
})
const emit = defineEmits(['update:modelValue', 'saved'])

// 候选按 scope 缓存：换域要重拉，防止组件复用时把上一个域的候选带过来
watch(() => props.scope, () => {
  optionsLoaded = false
  empLoaded = false
  empOptions.value = []
})

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null, scheduleDate: null, orgType: 1, orgId: null, employeeId: null, employeePostId: null,
  shiftId: null, dutyStatus: DUTY_STATUS_WORK, attendMode: 1, clinicFlag: 0,
  remark: '',
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
    // scheduleScope 与保存侧 assertScope 同一份域口径：岗位出勤域排除医生/护理/急诊科室员工，
    // 急诊值班域只出急诊科室医生。候选藏人 + 服务端拦截双闸，直调绕得过前者绕不过后者。
    const res = await getEmployeeList({empName: keyword || '', pageSize: 50, scheduleScope: props.scope})
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

/**
 * 听班一律不放号：AttendModeEnum.releasesSource —— 给一个在家待命的人放号，患者到了没人看。
 * 是否出诊(clinic_flag) 本弹框不再提供：号源派生行只由门诊排班页产生，
 * 这里只在编辑既有行时原样带回、新增恒 0（手工置 1 会被排班审批中心滤掉）。
 */
const needShift = computed(() => dutyStatusNeedShift(formData.dutyStatus))

let optionsLoaded = false
const ensureOptions = async () => {
  if (optionsLoaded) return
  try {
    // 科室候选用排班单元候选接口（带 scope 域过滤），不再用全量科室下拉 ——
    // 员工排班页要排除急诊科室，急诊排班页只出急诊叶子科室
    const [dRes, wRes, sRes] = await Promise.all([
      getStaffScheduleUnitSelectList({orgType: ORG_UNIT_TYPE_DEPT, scope: props.scope}),
      getNurseWardSelectList({}),
      getShiftSelectList({status: 1, useScope: props.shiftUseScope}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id, name: d.name}))
    wardOptions.value = (wRes?.data || []).map((w) => ({id: w.id ?? w.wardId, name: w.wardName ?? w.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName, useScope: s.useScope}))
    optionsLoaded = true
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
      scope: props.scope,
      orgId: orgIdRequired.value ? formData.orgId : null,
      shiftId: needShift.value ? formData.shiftId : null,
      // clinicFlag：新增恒 0（号源派生行只由门诊排班页产生）；编辑既有行时原样带回
      clinicFlag: formData.id ? (formData.clinicFlag ?? 0) : 0,
      // publishStatus 不传：新增后端落草稿；修改由后端按原态自动迁移（生效态→已变更）
      publishStatus: null,
    }
    const res = await staffScheduleUpsert(payload)
    if (res.code === 200) {
      // 超出人力上限时后端不拦但会带文案回来，必须把这句话给到排班员
      ElMessage.success(res.message || (formData.id ? '修改成功' : '登记成功'))
      visible.value = false
      emit('saved')
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
