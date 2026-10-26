<template>
  <el-dialog v-model="visible" title="批量排班（人员多选 × 日期区间）" width="640px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <el-form-item label="排班单元类型" prop="orgType">
        <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
          <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="orgIdRequired" :label="formData.orgType === 2 ? '病区' : '科室'" prop="orgId">
        <el-select
            v-model="formData.orgId"
            data-testid="batch-org"
            :placeholder="formData.orgType === 2 ? '选择病区' : '选择科室'"
            filterable
            style="width: 100%"
        >
          <el-option v-for="o in orgIdOptions" :key="o.id" :label="o.name" :value="o.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="排班对象" prop="employeeIds">
        <div class="w-full" data-testid="batch-employees">
          <el-select
              v-model="formData.employeeIds"
              :loading="empLoading"
              multiple
              filterable
              placeholder="可多选：勾选本次要排的人"
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
      <el-form-item label="排班日期" prop="dateRange">
        <div class="w-full" data-testid="batch-date">
          <el-date-picker
              v-model="formData.dateRange"
              :disabled-date="(time) => time.getTime() < Date.now() - 86400000"
              class="!w-full"
              end-placeholder="结束日期"
              range-separator="至"
              start-placeholder="开始日期"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
        </div>
        <div class="w-full text-xs text-gray-400">生成 {{ plannedCount }} 条（{{ empCount }} 人 × {{ dayCount }} 天，当天已有排班的自动跳过）</div>
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
        <el-alert :closable="false" title="休息/请假/培训/停班没有班次：批量记一笔「这天没排他」，落库才能看出是有意还是漏排" type="info"/>
      </el-form-item>
      <el-form-item v-if="needShift" label="响应形态">
        <el-radio-group v-model="formData.attendMode">
          <el-radio v-for="o in ATTEND_MODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
        <div class="w-full text-xs text-gray-400">坐班=正常到岗；听班=二线待命，不进在岗名单、不接派单；留院值班=夜间/节假日住宿舍值守</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="选填，会写到本次批量生成的每一行" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" data-testid="batch-submit" type="primary" @click="handleSubmit">
        {{ submitLoading ? `保存中 ${savedCount}/${plannedCount}` : '确定' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getStaffScheduleListPage, getStaffScheduleUnitSelectList, staffScheduleUpsert} from '@/api/staffSchedule'
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

/**
 * 批量排班：人员多选 × 日期区间，一次生成 人×日 条考勤排班。
 * 与单条 StaffScheduleEdit 同一个后端 staffScheduleUpsert（逐条调），所以
 * 域校验/重复拦截/整天互斥/人力提示全部复用，不在前端另立口径。
 * 发布态不传：新增由后端落草稿，走既有「提交审核 → 审核发布」闭环。
 * 已有排班（任意出勤状态）的人×日先查出来跳过，不让整批卡在重复拦截上。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 排班域（2-急诊值班 1-岗位出勤）：单元/人员候选与保存入参按域收口
  scope: {type: Number, default: 2},
  // 班次册（biz_shift.use_scope）：同 StaffScheduleEdit.shiftUseScope
  shiftUseScope: {type: Number, default: 4},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const savedCount = ref(0)
const formRef = ref(null)
const formData = reactive({
  orgType: 1, orgId: null,
  employeeIds: [],
  dateRange: null,
  dutyStatus: DUTY_STATUS_WORK, attendMode: 1, shiftId: null,
  remark: '',
})
const rules = {
  orgType: [{required: true, message: '请选择排班单元类型', trigger: 'change'}],
  orgId: [{required: true, message: '请选择科室', trigger: 'change'}],
  employeeIds: [{required: true, type: 'array', min: 1, message: '至少勾选一名排班对象', trigger: 'change'}],
  dateRange: [{required: true, message: '请选择排班日期区间', trigger: 'change'}],
  dutyStatus: [{required: true, message: '请选择出勤状态', trigger: 'change'}],
}

// ---------------- 下拉 ----------------
const empOptions = ref([])
const empLoading = ref(false)
const deptOptions = ref([])
const wardOptions = ref([])
const shiftOptions = ref([])

const orgIdOptions = computed(() => (formData.orgType === 2 ? wardOptions.value : deptOptions.value))
const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)
const needShift = computed(() => dutyStatusNeedShift(formData.dutyStatus))

// 日期必须本地格式化：toISOString 会按 UTC 偏移（UTC+8 下 10-26 零点变 10-25），
// 批量日期整体漂一天 = 排到昨天被后端拦 / 预检查错天
const fmtLocal = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
const dayList = computed(() => {
  const r = formData.dateRange || []
  if (r.length !== 2) return []
  const days = []
  const cur = new Date(r[0] + 'T00:00:00')
  const end = new Date(r[1] + 'T00:00:00')
  while (cur <= end) {
    days.push(fmtLocal(cur))
    cur.setDate(cur.getDate() + 1)
  }
  return days
})
const dayCount = computed(() => dayList.value.length)
const empCount = computed(() => (formData.employeeIds || []).length)
const plannedCount = computed(() => dayCount.value * empCount.value)

const loadOptions = async () => {
  empLoading.value = true
  try {
    const [dRes, wRes, sRes, eRes] = await Promise.all([
      getStaffScheduleUnitSelectList({orgType: ORG_UNIT_TYPE_DEPT, scope: props.scope}),
      getNurseWardSelectList({}),
      getShiftSelectList({status: 1, useScope: props.shiftUseScope}),
      // 候选与保存侧 assertScope 同一份域口径（急诊域只出急诊科室医生）
      getEmployeeList({pageSize: 200, scheduleScope: props.scope}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id, name: d.name}))
    wardOptions.value = (wRes?.data || []).map((w) => ({id: w.id ?? w.wardId, name: w.wardName ?? w.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName, useScope: s.useScope}))
    empOptions.value = (eRes?.data || []).map((e) => ({id: e.id, empName: e.empName, deptName: e.deptName}))
  } catch /* 全局拦截器已提示 */ {
  } finally {
    empLoading.value = false
  }
}

const resetForm = () => {
  formData.orgType = 1
  formData.orgId = null
  formData.employeeIds = []
  formData.dateRange = null
  formData.dutyStatus = DUTY_STATUS_WORK
  formData.attendMode = 1
  formData.shiftId = null
  formData.remark = ''
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  savedCount.value = 0
  await loadOptions()
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (needShift.value && !formData.shiftId) {
    ElMessage.warning('请选择班次：上班的时间段由班次带出')
    return
  }
  submitLoading.value = true
  savedCount.value = 0
  try {
    // 预取区间内该单元的既有排班：人×日已存在（含休息行）就跳过，
    // 避免整批卡在「不要重复排同一个班」上
    const exist = await getStaffScheduleListPage({
      startDate: dayList.value[0],
      endDate: dayList.value[dayList.value.length - 1],
      orgType: formData.orgType,
      orgId: formData.orgId,
      scope: props.scope,
      pageSize: 500,
    })
    const occupied = new Set((exist?.data?.records || exist?.data?.list || [])
        .map((r) => `${r.employeeId}|${r.scheduleDate}`))
    let skipped = 0
    let lastError = ''
    outer:
    for (const empId of formData.employeeIds) {
      for (const day of dayList.value) {
        if (occupied.has(`${empId}|${day}`)) {
          skipped += 1
          continue
        }
        try {
          const res = await staffScheduleUpsert({
            scope: props.scope,
            orgType: formData.orgType,
            orgId: orgIdRequired.value ? formData.orgId : null,
            employeeId: empId,
            scheduleDate: day,
            dutyStatus: formData.dutyStatus,
            attendMode: needShift.value ? formData.attendMode : null,
            shiftId: needShift.value ? formData.shiftId : null,
            // 发布态不传：新增一律后端落草稿，走提交审核 → 审核发布闭环
            remark: formData.remark || null,
          })
          if (res.code !== 200) {
            lastError = res.message || '未知错误'
            break outer
          }
          savedCount.value += 1
        } catch (e) {
          lastError = e?.message || '保存失败'
          break outer
        }
      }
    }
    const planText = `${empCount.value} 人 × ${dayCount.value} 天`
    if (savedCount.value > 0) {
      ElMessage.success(`批量排班完成：成功 ${savedCount.value} 条` +
          (skipped ? `，跳过 ${skipped} 条（当天已有排班）` : '') +
          (lastError ? `；中断于：${lastError}` : ''))
      visible.value = false
      emit('saved')
    } else {
      ElMessage.warning(`一条都没存成：` + (skipped >= plannedCount.value
          ? `${planText} 全部当天已有排班` : (lastError || '未知原因')))
    }
  } finally {
    submitLoading.value = false
  }
}
</script>
