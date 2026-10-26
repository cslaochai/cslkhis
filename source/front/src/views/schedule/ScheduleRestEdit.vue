<template>
  <el-dialog v-model="visible" :title="row?.id ? '修改休息标记' : '标记休息'" width="440px">
    <!-- 日期/科室/医生由点击的格子决定，只读展示——排班员不该在这里重选一遍 -->
    <div class="mb-3 rounded bg-slate-50 px-3 py-2 text-sm leading-6 text-slate-700">
      <span class="font-medium">{{ row?.employeeName }}</span>（{{ row?.deptName }}） ·
      {{ row?.scheduleDate }} <span class="text-slate-400">{{ dutyTextHint }}</span>
    </div>
    <el-alert
        v-if="clinicBound"
        :closable="false"
        class="mb-3"
        title="这是出诊班：标记后其号源将随停诊，已有挂号会提示退号"
        type="warning"
    />
    <el-form label-width="80px">
      <el-form-item label="出勤状态">
        <el-radio-group v-model="dutyStatus" data-testid="rest-status">
          <el-radio-button v-for="o in REST_STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input
            v-model="remark"
            :rows="2"
            data-testid="rest-remark"
            placeholder="选填，如：参加院感培训"
            type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button
          v-if="row?.id"
          v-perm="'staff:schedule:delete'"
          data-testid="rest-remove"
          type="danger"
          plain
          @click="handleRemove"
      >取消标记</el-button>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitLoading" data-testid="rest-submit" type="primary" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {staffScheduleUpsert, deleteStaffSchedule} from '@/api/staffSchedule'
import {DUTY_STATUS_TEXT} from '@/lib/staffSchedule'

/**
 * 门诊排班页「标休/转休」轻量弹框（标准 HIS 口径：点哪个格子，日期·科室·医生就确定了，
 * 弹层只问「休什么」）。与 StaffScheduleEdit（岗位/急诊域通用编辑）不同：
 * 这里不选单元类型/排班对象/班次/发布态——全部由格子预填，scope 固定门诊出诊域。
 */
// 休息类状态只有 2~5：上班走正常排班流程（选班次出号源），不在这个弹框里提供
const REST_STATUS_OPTIONS = [
  {label: '休息', value: 2},
  {label: '请假', value: 3},
  {label: '培训', value: 4},
  {label: '停班', value: 5},
]

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 格子预填：scheduleDate/orgType/orgId/orgName/employeeId/employeeName/deptName/id/staffScheduleId/dutyStatus/publishStatus/remark/clinicBound
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const dutyStatus = ref(2)
const remark = ref('')
const submitLoading = ref(false)

const dutyTextHint = computed(() =>
    props.row?.id ? `当前：${DUTY_STATUS_TEXT[props.row.dutyStatus] || '休息'}` : '')
const clinicBound = computed(() => Boolean(props.row?.clinicBound))

watch(visible, (open) => {
  if (!open) return
  dutyStatus.value = props.row?.dutyStatus ?? 2
  remark.value = props.row?.remark || ''
})

const handleSubmit = async () => {
  const row = props.row
  if (!row) return
  submitLoading.value = true
  try {
    const res = await staffScheduleUpsert({
      id: row.id ?? undefined,
      scheduleDate: row.scheduleDate,
      orgType: row.orgType ?? 1,
      orgId: row.orgId,
      employeeId: row.employeeId,
      dutyStatus: dutyStatus.value,
      publishStatus: row.publishStatus ?? 2,
      remark: remark.value,
      scope: 3,
    })
    if (res.code === 200) {
      // 转休会带回「出诊已随停班停诊」等联动文案，必须原样给到排班员
      ElMessage.success(res.message || (row.id ? '修改成功' : '已标记'))
      visible.value = false
      emit('saved')
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    submitLoading.value = false
  }
}

/** 取消标记：删掉这条休息行（物理删，唯一键不含 del_flag），格子回到「未排」 */
const handleRemove = async () => {
  const id = props.row?.id
  if (!id) return
  try {
    await ElMessageBox.confirm('取消后该格回到「未排」状态，确定？', '取消标记', {type: 'warning'})
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await deleteStaffSchedule(id)
    if (res.code === 200) {
      ElMessage.success('已取消标记')
      visible.value = false
      emit('saved')
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    submitLoading.value = false
  }
}
</script>
