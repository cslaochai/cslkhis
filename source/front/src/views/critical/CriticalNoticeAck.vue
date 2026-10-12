<template>
  <!-- 签收弹框（家属手写签名板） -->
  <el-dialog v-model="visible" data-testid="cn-ack-dialog" title="家属签收（手写签名 + 法定关系）" width="560px">
    <el-form label-width="110px">
      <el-form-item label="通知单">{{ ack.noticeNo }} / {{ ack.patientName }}</el-form-item>
      <el-form-item label="签收人姓名" required>
        <el-input v-model="ack.signerName" data-testid="cn-ack-name" maxlength="50"/>
      </el-form-item>
      <el-form-item label="与患者关系" required>
        <el-select v-model="ack.signerRelation" :fit-input-width="false" data-testid="cn-ack-relation"
                   placeholder="法定必填" style="width:220px">
          <el-option v-for="d in dict.relation" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="证件号">
        <el-input v-model="ack.signerIdCard" data-testid="cn-ack-idcard" maxlength="20"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="ack.signerPhone" data-testid="cn-ack-phone" maxlength="20"/>
      </el-form-item>
      <el-form-item label="手写签名" required>
        <SignaturePad ref="signPad"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="cn-ack-cancel" @click="visible = false">取消</el-button>
      <el-button :loading="ackSaving" data-testid="cn-ack-submit" type="primary" @click="doAck">确认签收</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 家属签收：签收人信息与手写签名板都在本组件；
 * 签收成功 emit('saved') 让父页面重拉台账/统计/在院名单。
 */
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import SignaturePad from '@/components/his/SignaturePad.vue'
import {noticeAcknowledge} from '@/api/criticalNotice'
import {gate} from '@/lib/criticalNotice'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 台账行（取 id/noticeNo/patientName）
  row: {type: Object, default: null},
  // 字典（relation）：列表列也在用，父页面加载一次传入
  dict: {type: Object, required: true},
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const ackSaving = ref(false)
const signPad = ref(null)
const ack = reactive({
  id: null,
  noticeNo: '',
  patientName: '',
  signerName: '',
  signerRelation: null,
  signerIdCard: '',
  signerPhone: ''
})
watch(() => props.modelValue, (v) => {
  if (!v) return
  const r = props.row || {}
  Object.assign(ack, {
    id: r.id,
    noticeNo: r.noticeNo,
    patientName: r.patientName,
    signerName: '',
    signerRelation: null,
    signerIdCard: '',
    signerPhone: ''
  })
})
const doAck = async () => {
  if (!ack.signerName) return ElMessage.warning('签收人姓名不能为空')
  if (!ack.signerRelation) return ElMessage.warning('签收人与患者的关系是法定必填项')
  if (!signPad.value || signPad.value.isEmpty()) return ElMessage.warning('请家属在签名板上手写签名')
  ackSaving.value = true
  const res = await gate(noticeAcknowledge({...ack, signerSignature: signPad.value.confirm()}), '签收失败')
  ackSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '家属已签收')
  visible.value = false
  emit('saved')
}
</script>
