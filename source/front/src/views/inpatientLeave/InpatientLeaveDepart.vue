<template>
  <el-dialog v-model="visible" data-testid="lv-leave-dialog" title="登记离院（患方签署风险告知与责任承诺书）"
             width="620px">
    <el-alert :closable="false" class="mb-3" title="未批准不放人；患方三要素（姓名/关系/手写签名）缺一不可 —— 这是「离院期间出事责任界定」的院内凭证。"
              type="warning"/>
    <el-form label-width="120px">
      <el-form-item label="请假单">{{ lv.leaveNo }} / {{ lv.patientName }}</el-form-item>
      <el-form-item label="确认人姓名" required>
        <el-input v-model="lv.confirmName" data-testid="lv-leave-name" maxlength="50"/>
      </el-form-item>
      <el-form-item label="与患者关系" required>
        <el-select v-model="lv.confirmRelation" :fit-input-width="false" data-testid="lv-leave-relation"
                   placeholder="责任界定必填" style="width:220px">
          <el-option v-for="d in dictRelation" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话" required>
        <el-input v-model="lv.confirmPhone" data-testid="lv-leave-phone" maxlength="20"/>
      </el-form-item>
      <el-form-item label="实际离院时间">
        <el-date-picker v-model="lv.actualLeaveTime" data-testid="lv-leave-time" placeholder="默认当前时间"
                        style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="手写签名" required>
        <SignaturePad ref="signPad"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="lvSaving" data-testid="lv-leave-submit" type="primary" @click="doLeave">签署并登记离院
      </el-button>
      <el-button data-testid="lv-leave-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import SignaturePad from '@/components/his/SignaturePad.vue'
import {leaveConfirm} from '@/api/inpatientLeave'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  dictRelation: {type: Array, default: () => []},
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

const nowText = (plusHours = 0) => {
  const d = new Date(Date.now() + plusHours * 3600 * 1000)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
}

const lvSaving = ref(false)
const signPad = ref(null)
const lv = reactive({
  id: null,
  leaveNo: '',
  patientName: '',
  confirmName: '',
  confirmRelation: null,
  confirmPhone: '',
  actualLeaveTime: ''
})
watch(visible, (open) => {
  if (!open) return
  const row = props.row || {}
  Object.assign(lv, {
    id: row.id,
    leaveNo: row.leaveNo,
    patientName: row.patientName,
    confirmName: '',
    confirmRelation: null,
    confirmPhone: '',
    actualLeaveTime: nowText(0)
  })
})
const doLeave = async () => {
  if (!lv.confirmName) return ElMessage.warning('患方确认人姓名不能为空')
  if (!lv.confirmRelation) return ElMessage.warning('确认人与患者的关系是责任界定必填项')
  if (!lv.confirmPhone) return ElMessage.warning('确认人联系电话不能为空')
  if (!signPad.value || signPad.value.isEmpty()) return ElMessage.warning('请患方在签名板上手写签名（承诺书必须亲笔签署）')
  lvSaving.value = true
  const res = await gate(leaveConfirm({
    id: lv.id, confirmName: lv.confirmName, confirmRelation: lv.confirmRelation, confirmPhone: lv.confirmPhone,
    confirmSignature: signPad.value.confirm(), actualLeaveTime: lv.actualLeaveTime || undefined,
  }), '离院登记失败')
  lvSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '患方已签署承诺书并登记离院')
  visible.value = false
  emit('saved')
}
</script>
