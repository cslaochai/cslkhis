<template>
  <el-dialog v-model="visible" title="发起听班呼叫" width="600px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="呼叫科室" prop="deptId">
        <el-select
            v-model="formData.deptId"
            data-testid="duty-call-dept"
            filterable
            placeholder="选择要呼叫的科室"
            style="width: 100%"
            @change="onDeptChange"
        >
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="值班日期" prop="dutyDate">
        <el-date-picker
            v-model="formData.dutyDate"
            data-testid="duty-call-date"
            style="width: 100%"
            type="date"
            value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="班次" prop="shiftType">
        <el-radio-group v-model="formData.shiftType" data-testid="duty-call-shift">
          <el-radio :value="1">白班</el-radio>
          <el-radio :value="2">夜班</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="被叫层级" prop="targetLevel">
        <el-radio-group v-model="formData.targetLevel" data-testid="duty-call-level">
          <el-radio :value="2">二线</el-radio>
          <el-radio :value="3">三线</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="当班听班人">
        <div class="w-full">
          <el-tag
              v-if="currentTarget"
              data-testid="duty-call-target"
              :type="currentTarget.callable ? 'success' : 'danger'"
          >
            {{ currentTarget.callable
              ? `${currentTarget.targetLevelText} · ${currentTarget.employeeName}${currentTarget.phone ? '（' + currentTarget.phone + '）' : ''}`
              : `${currentTarget.targetLevelText} 未排班，暂不可呼叫` }}
          </el-tag>
          <span v-else class="text-xs text-gray-400">该科室没有配置临床二/三线听班点位</span>
        </div>
      </el-form-item>
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="formData.patientName" clearable maxlength="50" placeholder="选填"/>
      </el-form-item>
      <el-form-item label="患者位置" prop="patientLocation">
        <el-input v-model="formData.patientLocation" clearable maxlength="100" placeholder="选填，如 呼吸内科 3 床"/>
      </el-form-item>
      <el-form-item label="呼叫事由" prop="callReason">
        <div data-testid="duty-call-reason" style="width: 100%">
          <el-input
              v-model="formData.callReason"
              :rows="3"
              maxlength="500"
              placeholder="如：患者突发胸闷血氧下降，一线处理后仍不缓解，需二线到场指导"
              show-word-limit
              type="textarea"
          />
        </div>
      </el-form-item>      <el-form-item label="到岗时限" prop="timeoutMinutes">
        <el-input-number v-model="formData.timeoutMinutes" :min="1" :max="120" style="width: 160px"/>
        <span class="ml-2 text-xs text-gray-500">分钟内未到岗即升级到上一层级</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" data-testid="duty-call-submit" type="primary" @click="handleSubmit">
        发出呼叫
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {dutyCallCreate, getDutyCallTargets} from '@/api/dutyCall'
import {getDepartmentSelectList} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 默认科室（从页面上下文带进来） */
  defaultDeptId: {type: [String, Number], default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const formRef = ref()
const submitting = ref(false)
const deptOptions = ref([])
const targets = ref([])

const formData = ref({
  deptId: null,
  dutyDate: '',
  shiftType: 1,
  targetLevel: 2,
  patientName: '',
  patientLocation: '',
  callReason: '',
  timeoutMinutes: 10,
})

const rules = {
  deptId: [{required: true, message: '请选择呼叫科室', trigger: 'change'}],
  dutyDate: [{required: true, message: '请选择值班日期', trigger: 'change'}],
  shiftType: [{required: true, message: '请选择班次', trigger: 'change'}],
  targetLevel: [{required: true, message: '请选择被叫层级', trigger: 'change'}],
  callReason: [{required: true, message: '请填写呼叫事由', trigger: 'blur'}],
  timeoutMinutes: [{required: true, message: '请填写到岗时限', trigger: 'blur'}],
}

/** 当前被叫层级对应的当班听班人（页面据此提示「这一班有没有人」） */
const currentTarget = computed(() =>
    targets.value.find((t) => t.targetLevel === formData.value.targetLevel) || null)

const todayStr = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

/** 当前时刻所属班次：08:00-18:00 白班，其余夜班（与后端同口径） */
const currentShift = () => {
  const h = new Date().getHours()
  return (h < 8 || h >= 18) ? 2 : 1
}

const loadTargets = async () => {
  if (!formData.value.deptId) {
    targets.value = []
    return
  }
  try {
    const res = await getDutyCallTargets({
      deptId: formData.value.deptId,
      dutyDate: formData.value.dutyDate,
      shiftType: formData.value.shiftType,
    })
    targets.value = res?.data || []
  } catch (e) /* 错误已由全局拦截器统一提示 */{
    targets.value = []
  }
}

const onDeptChange = () => loadTargets()

const handleSubmit = async () => {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  submitting.value = true
  try {
    const res = await dutyCallCreate({
      deptId: formData.value.deptId,
      dutyDate: formData.value.dutyDate,
      shiftType: formData.value.shiftType,
      targetLevel: formData.value.targetLevel,
      patientName: formData.value.patientName,
      patientLocation: formData.value.patientLocation,
      callReason: formData.value.callReason,
      timeoutMinutes: formData.value.timeoutMinutes,
    })
    ElMessage.success(res?.message || '呼叫已发出')
    emit('saved')
    visible.value = false
  } catch (e) /* 错误已由全局拦截器统一提示 */{
  } finally {
    submitting.value = false
  }
}

watch(visible, async (open) => {
  if (!open) return
  formData.value = {
    deptId: props.defaultDeptId || null,
    dutyDate: todayStr(),
    shiftType: currentShift(),
    targetLevel: 2,
    patientName: '',
    patientLocation: '',
    callReason: '',
    timeoutMinutes: 10,
  }
  if (!deptOptions.value.length) {
    try {
      const res = await getDepartmentSelectList({})
      deptOptions.value = (res?.data || []).map((d) => ({id: d.id ?? d.deptId, name: d.deptName ?? d.name}))
    } catch (e) /* 错误已由全局拦截器统一提示 */{
    }
  }
  await loadTargets()
})
</script>
