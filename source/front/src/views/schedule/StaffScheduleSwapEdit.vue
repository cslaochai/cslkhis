<template>
  <el-dialog v-model="visible" title="换班 / 代班" width="560px">
    <el-alert
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
        title="选了「对方排班」就是两人对调（换班）；不选、直接选人就是这一班改由他承接（代班）。两种都会留痕。"
        type="info"
    />
    <el-form ref="swapFormRef" :model="swapForm" :rules="swapRules" label-width="120px">
      <el-form-item label="对方排班">
        <el-select
            v-model="swapForm.toScheduleId"
            clearable
            filterable
            placeholder="同日同单元的另一条排班（留空=代班）"
            style="width: 100%"
        >
          <el-option
              v-for="r in swapCandidates"
              :key="r.id"
              :label="`${r.employeeName} · ${r.shiftName} ${r.startTime}~${r.endTime}`"
              :value="r.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="!swapForm.toScheduleId" label="承接人" prop="substituteEmployeeId">
        <div data-testid="ss-substitute">
          <el-select
              v-model="swapForm.substituteEmployeeId"
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
      <el-form-item label="变更原因" prop="reason">
        <div data-testid="ss-swap-reason">
          <el-input v-model="swapForm.reason" :rows="2" placeholder="必填：如张三参加抢救无法脱身" type="textarea"/>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" data-testid="ss-swap-submit" type="primary" @click="handleSwap">确定变更
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {staffScheduleSwap} from '@/api/staffSchedule'
import {getEmployeeList} from '@/api/system'

/**
 * 换班 / 代班：从当前页列表里挑「对方排班」——选了就是互换，不选直接选人就是代班。
 * row 是被换的那条排班（父页面传当前行），列表由父页面 prop 下发，本组件不另拉列表接口。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  tableData: {type: Array, default: () => []},
  // 排班域（1-岗位出勤 2-急诊值班）：换班/代班只许动本域的行，服务端 assertRowScope 兜底
  scope: {type: Number, default: 1},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const swapFormRef = ref(null)
const swapForm = reactive({fromScheduleId: null, toScheduleId: null, substituteEmployeeId: null, reason: ''})
const swapRules = {reason: [{required: true, message: '请填写变更原因', trigger: 'blur'}]}

const swapCandidates = computed(() => props.tableData.filter((x) => Number(x.dutyStatus) === 1))

const empOptions = ref([])
const empLoading = ref(false)

const searchEmployee = async (keyword) => {
  empLoading.value = true
  try {
    // 承接人候选按域收口（同 StaffScheduleEdit）：岗位出勤域排除医生/护理/急诊科室员工
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

let empLoaded = false
const ensureEmpOptions = async () => {
  if (empLoaded) return
  await searchEmployee('')
  empLoaded = true
}

watch(visible, async (open) => {
  if (!open) return
  swapForm.fromScheduleId = props.row?.id ?? null
  swapForm.toScheduleId = null
  swapForm.substituteEmployeeId = null
  swapForm.reason = ''
  await ensureEmpOptions()
})

const handleSwap = async () => {
  if (!swapFormRef.value) return
  try {
    await swapFormRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const payload = {
      fromScheduleId: swapForm.fromScheduleId,
      scope: props.scope,
      reason: swapForm.reason,
    }
    // 传了 toScheduleId 就是互换，不传就是代班 —— 两种形态别混着传
    if (swapForm.toScheduleId) payload.toScheduleId = swapForm.toScheduleId
    else payload.substituteEmployeeId = swapForm.substituteEmployeeId
    const res = await staffScheduleSwap(payload)
    if (res.code === 200) {
      ElMessage.success(res.message || '变更成功')
      visible.value = false
      emit('saved')
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
