<template>
  <el-dialog v-model="visible" :title="form.id ? '修改请假单（待审批）' : '填写请假单（申请）'" data-testid="lv-form-dialog"
             width="720px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="在院患者" prop="admissionId">
        <el-select v-model="form.admissionId" :disabled="!!form.id" :fit-input-width="false" class="!w-full"
                   clearable
                   data-testid="lv-form-admission" filterable placeholder="搜索姓名/住院号选择在院患者"
                   @change="onAdmissionPick">
          <el-option v-for="p in inpatients" :key="p.admissionId"
                     :label="`${p.bedNo || '—'}床 ${p.patientName}（${p.deptName || '—'}）`" :value="p.admissionId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="患者快照">
        <span class="text-sm text-slate-600" data-testid="lv-form-snapshot">
          {{ form.patientName || '—' }} / {{ form.admissionNo || '—' }} / {{
            form.wardName || '—'
          }} {{ form.bedNo ? form.bedNo + '床' : '' }}
        </span>
      </el-form-item>
      <el-form-item label="请假类别" prop="leaveType">
        <el-radio-group v-model="form.leaveType" data-testid="lv-form-type">
          <el-radio v-for="d in dictType" :key="d.dictValue" :value="Number(d.dictValue)">{{
              d.dictLabel
            }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="请假事由" prop="reason">
        <el-input v-model="form.reason" :rows="2" data-testid="lv-form-reason" maxlength="500" show-word-limit
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="去向" prop="destination">
        <el-input v-model="form.destination" data-testid="lv-form-destination" maxlength="200"
                  placeholder="写清去哪、能联系到人的地方（责任界定的关键）"/>
      </el-form-item>
      <el-form-item label="随行/联系人" required>
        <div class="flex w-full gap-2">
          <el-input v-model="form.companionName" data-testid="lv-form-companion" maxlength="50" placeholder="姓名"
                    style="flex:1"/>
          <el-select v-model="form.companionRelation" :fit-input-width="false" data-testid="lv-form-relation"
                     placeholder="与患者关系" style="width:150px">
            <el-option v-for="d in dictRelation" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-input v-model="form.companionPhone" :placeholder="form.id && form.companionPhoneMasked ? `已留存 ${form.companionPhoneMasked}，留空表示不修改` : '联系电话'"
                    data-testid="lv-form-phone"
                    maxlength="20" style="width:160px"/>
        </div>
      </el-form-item>
      <el-form-item label="预计离院时间" prop="expectedLeaveTime">
        <el-date-picker v-model="form.expectedLeaveTime" data-testid="lv-form-leave-time" placeholder="预计离开病区"
                        style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="预计返回时间" prop="expectedReturnTime">
        <el-date-picker v-model="form.expectedReturnTime" data-testid="lv-form-return-time" placeholder="超过即超期未归"
                        style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        <span class="ml-2 text-xs text-slate-400">单次请假上限见系统参数（默认 72 小时），超上限后端直接拒绝</span>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" data-testid="lv-form-remark" maxlength="500"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="lv-form-cancel" @click="visible = false">取消</el-button>
      <el-button :loading="fSaving" data-testid="lv-form-save" type="primary" @click="saveForm">保存申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getLeaveBase, getLeaveById, leaveUpsert} from '@/api/inpatientLeave'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // row 为空 = 新建（banner 选人在途时传 {admissionId} + base），有值 = 修改待审批草稿
  row: {type: Object, default: null},
  base: {type: Object, default: null},
  inpatients: {type: Array, default: () => []},
  dictType: {type: Array, default: () => []},
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

const fSaving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, admissionId: null, patientName: '', wardName: '', bedNo: '', admissionNo: '',
  leaveType: 1, reason: '', destination: '', companionName: '', companionRelation: null, companionPhone: '',
  expectedLeaveTime: '', expectedReturnTime: '', remark: '',
  // 编辑草稿时电话拿不到明文（列表不出联系方式、详情只给脱敏值），这一格默认留空＝沿用原值
  companionPhoneMasked: '',
})
// 随行人电话的必填是**动态**的：列表 VO 不出联系方式、详情只给脱敏值，编辑草稿时这一格
// 必然是空的（后端也同样放宽了 Service 强校验）。若这里写死 required:true，用户点「编辑」
// 什么都不改直接保存就会被自己的规则弹回「电话不能为空」——现象像修改功能坏了。
// 有留存值（脱敏串）时允许留空＝沿用原值；新建（没有留存值）才必填。
const rules = computed(() => ({
  admissionId: [{required: true, message: '必须挂在一次住院上', trigger: 'change'}],
  leaveType: [{required: true, message: '请选择请假类别', trigger: 'change'}],
  reason: [{required: true, message: '请假事由不能为空', trigger: 'blur'}],
  destination: [{required: true, message: '去向不能为空（写清去哪，责任界定的关键）', trigger: 'blur'}],
  companionName: [{required: true, message: '随行/联系人不能为空', trigger: 'blur'}],
  companionPhone: [{required: !form.companionPhoneMasked, message: '随行人联系电话不能为空', trigger: 'blur'}],
  expectedLeaveTime: [{required: true, message: '预计离院时间不能为空', trigger: 'change'}],
  expectedReturnTime: [{required: true, message: '预计返回时间不能为空', trigger: 'change'}],
}))
const nowText = (plusHours = 0) => {
  const d = new Date(Date.now() + plusHours * 3600 * 1000)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
}
watch(visible, (open) => {
  if (!open) return
  const row = props.row
  const base = props.base
  Object.assign(form, {
    id: row?.id || null,
    admissionId: row?.admissionId || base?.admissionId || null,
    patientName: base?.patientName || row?.patientName || '',
    wardName: base?.wardName || '',
    bedNo: base?.bedNo || '',
    admissionNo: base?.admissionNo || row?.admissionNo || '',
    leaveType: row?.leaveType || 1,
    reason: row?.reason || '',
    destination: row?.destination || '',
    companionName: row?.companionName || '',
    companionRelation: row?.companionRelation ?? null,
    companionPhone: row?.companionPhone || '',
    expectedLeaveTime: row?.expectedLeaveTime ? String(row.expectedLeaveTime).slice(0, 19) : nowText(1),
    expectedReturnTime: row?.expectedReturnTime ? String(row.expectedReturnTime).slice(0, 19) : nowText(5),
    remark: row?.remark || '',
    companionPhoneMasked: '',
  })
  if (row?.id) fillEditableContent(row.id)
})
/** 编辑草稿：补全详情字段（台账行有全部申请字段，详情只是兜底） */
const fillEditableContent = async (id) => {
  const res = await gate(getLeaveById(id), '加载请假单失败')
  if (!res) return
  const d = res.data || {}
  Object.assign(form, {
    reason: d.reason || form.reason,
    destination: d.destination || form.destination,
    companionName: d.companionName || form.companionName,
    companionRelation: d.companionRelation ?? form.companionRelation,
    remark: d.remark || form.remark,
    patientName: d.patientName,
    wardName: d.wardName,
    bedNo: d.bedNo,
    admissionNo: d.admissionNo,
    companionPhoneMasked: d.companionPhoneMasked || '',
  })
}
const onAdmissionPick = async (admissionId) => {
  if (!admissionId || form.id) return
  const res = await gate(getLeaveBase(admissionId), '带出患者信息失败')
  if (!res) return
  const b = res.data || {}
  Object.assign(form, {
    patientName: b.patientName, wardName: b.wardName, bedNo: b.bedNo, admissionNo: b.admissionNo,
    reason: form.reason || (b.diagnosis ? '' : ''),
  })
}
const saveForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.expectedReturnTime <= form.expectedLeaveTime) {
    return ElMessage.warning('预计返回时间必须晚于预计离院时间')
  }
  // 电话：编辑态留空＝沿用原值（列表不出明文、详情只给脱敏值，前端无法回显）；新建必须填
  if (!form.companionPhone && !form.companionPhoneMasked) {
    return ElMessage.warning('随行人联系电话不能为空（超期要打电话找人）')
  }
  fSaving.value = true
  const res = await gate(leaveUpsert({
    id: form.id, admissionId: form.admissionId, leaveType: form.leaveType,
    reason: form.reason, destination: form.destination,
    companionName: form.companionName, companionRelation: form.companionRelation,
    // 留空＝沿用原值（后端口径：列表/详情都不出明文电话，编辑时无法回显）
    companionPhone: form.companionPhone || undefined,
    expectedLeaveTime: form.expectedLeaveTime, expectedReturnTime: form.expectedReturnTime, remark: form.remark,
  }), '保存失败')
  fSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '请假单已保存')
  visible.value = false
  emit('saved')
}
</script>
