<template>
  <!-- 表单弹框（草稿）：新增=填写通知单，编辑=改草稿，按 form.id 区分 -->
  <el-dialog v-model="visible" :title="form.id ? '修改通知单（草稿）' : '填写通知单（草稿）'" data-testid="cn-form-dialog"
             width="720px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="在院患者" prop="admissionId">
        <el-select v-model="form.admissionId" :disabled="!!form.id" :fit-input-width="false" class="!w-full"
                   clearable
                   data-testid="cn-form-admission" filterable placeholder="搜索姓名/住院号选择在院患者"
                   @change="onAdmissionPick">
          <el-option v-for="p in inpatients" :key="p.admissionId"
                     :label="`${p.bedNo || '—'}床 ${p.patientName}（${p.deptName || '—'}）`" :value="p.admissionId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="患者快照">
        <span class="text-sm text-slate-600" data-testid="cn-form-snapshot">
          {{ form.patientName || '—' }} / {{ form.admissionNo || '—' }} / {{
            form.wardName || '—'
          }} {{ form.bedNo ? form.bedNo + '床' : '' }}
        </span>
      </el-form-item>
      <el-form-item label="通知类别" prop="noticeType">
        <el-radio-group v-model="form.noticeType" data-testid="cn-form-type">
          <el-radio v-for="d in dict.type" :key="d.dictValue" :value="Number(d.dictValue)">{{
              d.dictLabel
            }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="患者神志" prop="consciousnessStatus">
        <el-select v-model="form.consciousnessStatus" data-testid="cn-form-cons" style="width:180px">
          <el-option v-for="d in dict.consciousness" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="目前诊断" prop="clinicalDiagnosis">
        <el-input v-model="form.clinicalDiagnosis" data-testid="cn-form-diagnosis" maxlength="500"/>
      </el-form-item>
      <el-form-item label="病情及危险因素" prop="conditionDesc">
        <el-input v-model="form.conditionDesc" :rows="3" data-testid="cn-form-condition" maxlength="1000" show-word-limit
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="预警事项" prop="warningMatters">
        <el-input v-model="form.warningMatters" :rows="3" data-testid="cn-form-warning" maxlength="1000" show-word-limit
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="医方措施">
        <el-input v-model="form.doctorMeasures" :rows="2" data-testid="cn-form-measures" maxlength="1000" show-word-limit
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="告知时间" prop="notifyTime">
        <el-date-picker v-model="form.notifyTime" data-testid="cn-form-notify-time" placeholder="精确到分"
                        style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="见证医师">
        <el-select v-model="form.witnessDoctorId" :fit-input-width="false" class="!w-full"
                   clearable data-testid="cn-form-witness"
                   filterable placeholder="抢救场景第二医师（可空，不得与告知医师同人）">
          <el-option v-for="d in doctors" :key="d.employeeId" :label="`${d.empName}（${d.deptName || '—'}）`"
                     :value="d.employeeId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" data-testid="cn-form-remark" maxlength="500"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="cn-form-cancel" @click="visible = false">取消</el-button>
      <el-button :loading="fSaving" data-testid="cn-form-save" type="primary" @click="saveForm">保存草稿</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 通知单草稿 填写/修改（新增+编辑合并：form.id 有无决定标题与提交语义）。
 * 表单态、校验、保存都在本组件；在院患者候选由父页面（横幅共用）传入，
 * 见证医师下拉只这里用，组件内自 load；编辑草稿时正文大字段走详情接口补全。
 */
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getNoticeBase, getNoticeById, getNoticeDoctorOptions, noticeUpsert} from '@/api/criticalNotice'
import {gate} from '@/lib/criticalNotice'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 台账行（编辑草稿）或 {admissionId}（横幅直接开单）；null = 空白新建
  row: {type: Object, default: null},
  // getNoticeBase 带出的患者快照（横幅开单时传入）
  base: {type: Object, default: null},
  // 字典（type/consciousness）：筛选与横幅也在用，父页面加载一次传入
  dict: {type: Object, required: true},
  // 在院患者候选：与横幅下拉同一份，父页面加载
  inpatients: {type: Array, default: () => []},
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const fSaving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, admissionId: null, patientName: '', wardName: '', bedNo: '', admissionNo: '',
  noticeType: 1, consciousnessStatus: 1, clinicalDiagnosis: '', conditionDesc: '',
  warningMatters: '', doctorMeasures: '', notifyTime: '', witnessDoctorId: null, remark: '',
})
const rules = {
  admissionId: [{required: true, message: '必须挂在一次住院上', trigger: 'change'}],
  noticeType: [{required: true, message: '请选择通知类别', trigger: 'change'}],
  consciousnessStatus: [{required: true, message: '请选择患者神志', trigger: 'change'}],
  clinicalDiagnosis: [{required: true, message: '目前诊断不能为空', trigger: 'blur'}],
  conditionDesc: [{required: true, message: '病情及危险因素不能为空', trigger: 'blur'}],
  warningMatters: [{required: true, message: '预警事项不能为空', trigger: 'blur'}],
  notifyTime: [{required: true, message: '告知时间不能为空', trigger: 'change'}],
}
const nowText = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
}
// ---------------- 医师下拉（只这里用，组件内自 load） ----------------
const doctors = ref([])
const loadDoctors = async () => {
  if (doctors.value.length) return
  const res = await gate(getNoticeDoctorOptions(), '加载医师候选失败')
  if (res) doctors.value = res.data || []
}
/** 编辑草稿：正文大字段不在台账行里，走详情接口补全（后端未脱敏正文，回显零损失） */
const fillEditableContent = async (id) => {
  const res = await gate(getNoticeById(id), '加载通知单失败')
  if (!res) return
  const d = res.data || {}
  Object.assign(form, {
    conditionDesc: d.conditionDesc || '', warningMatters: d.warningMatters || '',
    doctorMeasures: d.doctorMeasures || '', remark: d.remark || '',
    witnessDoctorId: d.witnessDoctorId || form.witnessDoctorId,
    patientName: d.patientName, wardName: d.wardName, bedNo: d.bedNo, admissionNo: d.admissionNo,
  })
}
watch(() => props.modelValue, async (v) => {
  if (!v) return
  const r = props.row || {}
  const b = props.base || {}
  Object.assign(form, {
    id: r?.id || null,
    admissionId: r?.admissionId || b?.admissionId || null,
    patientName: b?.patientName || r?.patientName || '',
    wardName: b?.wardName || '', bedNo: b?.bedNo || '', admissionNo: b?.admissionNo || r?.admissionNo || '',
    noticeType: r?.noticeType || 1, consciousnessStatus: r?.consciousnessStatus || 1,
    clinicalDiagnosis: r?.clinicalDiagnosis || b?.diagnosis || '',
    conditionDesc: '', warningMatters: '', doctorMeasures: '',
    notifyTime: r?.notifyTime ? String(r.notifyTime).slice(0, 19) : nowText(),
    witnessDoctorId: r?.witnessDoctorId || null, remark: r?.remark || '',
  })
  loadDoctors()
  if (r?.id) await fillEditableContent(r.id)
})
const onAdmissionPick = async (admissionId) => {
  if (!admissionId || form.id) return
  const res = await gate(getNoticeBase(admissionId), '带出患者信息失败')
  if (!res) return
  const b = res.data || {}
  Object.assign(form, {
    patientName: b.patientName, wardName: b.wardName, bedNo: b.bedNo,
    admissionNo: b.admissionNo, clinicalDiagnosis: form.clinicalDiagnosis || b.diagnosis || '',
  })
}
const saveForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  fSaving.value = true
  const res = await gate(noticeUpsert({
    id: form.id, admissionId: form.admissionId, noticeType: form.noticeType,
    consciousnessStatus: form.consciousnessStatus, clinicalDiagnosis: form.clinicalDiagnosis,
    conditionDesc: form.conditionDesc, warningMatters: form.warningMatters,
    doctorMeasures: form.doctorMeasures, notifyTime: form.notifyTime,
    witnessDoctorId: form.witnessDoctorId, remark: form.remark,
  }), '保存失败')
  fSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '通知单已保存')
  visible.value = false
  emit('saved')
}
</script>
