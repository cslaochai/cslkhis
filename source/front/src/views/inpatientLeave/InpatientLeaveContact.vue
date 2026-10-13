<template>
  <el-dialog v-model="visible" data-testid="lv-contact-dialog" title="超期未归处置" width="520px">
    <el-alert :closable="false" :title="`已超期 ${ct.overdueHours} 小时未返回病区。联系不上必须升级上报（主管医师 → 护士长 → 医务科/总值班）。`" class="mb-3"
              type="error"/>
    <el-form label-width="110px">
      <el-form-item label="请假单">{{ ct.leaveNo }} / {{ ct.patientName }}</el-form-item>
      <el-form-item label="联系结果" required>
        <el-select v-model="ct.contactResult" :fit-input-width="false" data-testid="lv-contact-result" placeholder="请选择"
                   style="width:100%">
          <el-option v-for="d in dictContact" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="上报对象" required>
        <el-select v-model="ct.reportTo" :fit-input-width="false" data-testid="lv-contact-report" placeholder="请选择"
                   style="width:100%">
          <el-option v-for="d in dictReport" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="处置备注">
        <el-input v-model="ct.contactNote" :rows="3" data-testid="lv-contact-note" maxlength="500" placeholder="联系经过、约定内容等"
                  show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="ctSaving" data-testid="lv-contact-submit" type="primary" @click="doContact">记录处置
      </el-button>
      <el-button data-testid="lv-contact-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {leaveContact} from '@/api/inpatientLeave'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  dictContact: {type: Array, default: () => []},
  dictReport: {type: Array, default: () => []},
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

const ctSaving = ref(false)
const ct = reactive({
  id: null,
  leaveNo: '',
  patientName: '',
  overdueHours: 0,
  contactResult: null,
  contactNote: '',
  reportTo: null
})
watch(visible, (open) => {
  if (!open) return
  const row = props.row || {}
  Object.assign(ct, {
    id: row.id,
    leaveNo: row.leaveNo,
    patientName: row.patientName,
    overdueHours: row.overdueHours || 0,
    contactResult: null,
    contactNote: '',
    reportTo: null
  })
})
const doContact = async () => {
  if (!ct.contactResult) return ElMessage.warning('请选择联系结果')
  if (!ct.reportTo) return ElMessage.warning('请选择上报对象（联系不上必须升级上报）')
  ctSaving.value = true
  const res = await gate(leaveContact({
    id: ct.id,
    contactResult: ct.contactResult,
    contactNote: ct.contactNote || undefined,
    reportTo: ct.reportTo
  }), '超期处置失败')
  ctSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '超期处置已记录')
  visible.value = false
  emit('saved')
}
</script>
