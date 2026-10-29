<template>
  <el-dialog v-model="visible" data-testid="lv-approve-dialog" title="审批请假单" width="560px">
    <el-form label-width="110px">
      <el-form-item label="请假单">{{ ap.leaveNo }} / {{ ap.patientName }}</el-form-item>
      <el-form-item label="审批结论" required>
        <el-radio-group v-model="ap.allow" data-testid="lv-approve-allow">
          <el-radio :value="true">批准（医师电子签名锁定）</el-radio>
          <el-radio :value="false">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="ap.allow" label="医师意见" required>
        <el-input v-model="ap.doctorAdvice" :rows="4" data-testid="lv-approve-advice" maxlength="1000" placeholder="病情评估：是否允许外出、外出期间注意事项（随签名一并锁定）"
                  show-word-limit
                  type="textarea"/>
      </el-form-item>
      <el-form-item v-else label="拒绝理由" required>
        <el-input v-model="ap.rejectReason" :rows="3" data-testid="lv-approve-reject" maxlength="500" placeholder="写清病情为什么不允许外出"
                  show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="apSaving" data-testid="lv-approve-submit" type="primary" @click="doApprove">
        {{ ap.allow ? '确认批准' : '确认拒绝' }}
      </el-button>
      <el-button data-testid="lv-approve-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- 软签二次确认：批准要落医师电子签名，由审批医师本人输入 PIN -->
  <SignPinDialog ref="pinDialogRef" v-model="pinDialogVisible"/>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {leaveApprove} from '@/api/inpatientLeave'
import SignPinDialog from '@/components/his/SignPinDialog.vue'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const gate = async (p, msg) => {
  try {
    const res = await p
    if (res.code === 200) return res
    ElMessage.error(res.message || msg)
    return null
  } catch (e) {
    
    return null
  }
}

const apSaving = ref(false)
// 软签二次确认：PIN 由审批医师本人输入，服务端校验通过才解密私钥
const pinDialogRef = ref(null)
const pinDialogVisible = ref(false)
const ap = reactive({id: null, leaveNo: '', patientName: '', allow: true, doctorAdvice: '', rejectReason: ''})
watch(visible, (open) => {
  if (!open) return
  const row = props.row || {}
  Object.assign(ap, {
    id: row.id,
    leaveNo: row.leaveNo,
    patientName: row.patientName,
    allow: true,
    doctorAdvice: '',
    rejectReason: ''
  })
})
const doApprove = async () => {
  if (ap.allow && !ap.doctorAdvice) return ElMessage.warning('批准必须填写医师意见（病情评估：是否允许外出、外出期间注意事项）')
  if (!ap.allow && !ap.rejectReason) return ElMessage.warning('拒绝必须填写理由（写清病情为什么不允许外出）')
  // 批准即电子签名：先取 PIN，取消则不提交；拒绝不产生签名，不需要 PIN
  let pin
  if (ap.allow) {
    pin = await pinDialogRef.value.askPin()
    if (!pin) {
      ElMessage.info('已取消签名，审批未提交')
      return
    }
  }
  apSaving.value = true
  const res = await gate(leaveApprove({
    id: ap.id, allow: ap.allow,
    doctorAdvice: ap.allow ? ap.doctorAdvice : (ap.doctorAdvice || undefined),
    rejectReason: ap.allow ? undefined : ap.rejectReason,
    pin,
  }), '审批失败')
  apSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '已审批')
  visible.value = false
  emit('saved')
}
</script>
