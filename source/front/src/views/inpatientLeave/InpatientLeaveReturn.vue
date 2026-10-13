<template>
  <el-dialog v-model="visible" data-testid="lv-back-dialog" title="返回销假" width="480px">
    <el-form label-width="110px">
      <el-form-item label="请假单">{{ bk.leaveNo }} / {{ bk.patientName }}</el-form-item>
      <el-form-item label="返回情况">
        <el-input v-model="bk.returnNote" :rows="3" data-testid="lv-back-note" maxlength="500" placeholder="可空：返回时患者状态等"
                  show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="bkSaving" data-testid="lv-back-submit" type="primary" @click="doBack">确认销假</el-button>
      <el-button data-testid="lv-back-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {leaveBack} from '@/api/inpatientLeave'

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
    ElMessage.error(String((e && e.message) || msg))
    return null
  }
}

const bkSaving = ref(false)
const bk = reactive({id: null, leaveNo: '', patientName: '', returnNote: ''})
watch(visible, (open) => {
  if (!open) return
  const row = props.row || {}
  Object.assign(bk, {id: row.id, leaveNo: row.leaveNo, patientName: row.patientName, returnNote: ''})
})
const doBack = async () => {
  bkSaving.value = true
  const res = await gate(leaveBack({id: bk.id, returnNote: bk.returnNote || undefined}), '销假失败')
  bkSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '已销假')
  visible.value = false
  emit('saved')
}
</script>
