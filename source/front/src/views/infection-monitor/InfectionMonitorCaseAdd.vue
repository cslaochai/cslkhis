<template>
  <el-dialog v-model="visible" :title="title" destroy-on-close width="560px">
    <el-form label-width="90px">
      <el-form-item v-if="Number(caseForm.leakFlag) === 1" label="漏报口径">
        <el-tag type="danger">漏报调查发现后补报（统计单列）</el-tag>
      </el-form-item>
      <el-form-item label="患者">
        <PatientSelect v-model="caseForm.patientId" data-testid="case-patient"
                       @clear="anchor.registOptions = []; anchor.inpOptions = []"
                       @select="onCasePatientSelect"/>
      </el-form-item>
      <el-form-item label="就诊类型">
        <el-radio-group v-model="caseForm.visitType">
          <el-radio :value="2">住院</el-radio>
          <el-radio :value="1">门诊</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="caseForm.visitType === 1" label="门诊就诊">
        <el-select v-model="anchor.registId" data-testid="case-regist" filterable placeholder="选择门诊就诊"
                   style="width:100%">
          <el-option v-for="r in anchor.registOptions" :key="r.id"
                     :label="`${String(r.id).slice(-6)} ${r.deptName || ''} ${r.visitDate || ''}`" :value="r.id"/>
        </el-select>
      </el-form-item>
      <el-form-item v-else label="住院记录">
        <el-select v-model="anchor.inpId" data-testid="case-inp" filterable placeholder="选择住院记录"
                   style="width:100%">
          <el-option v-for="a in anchor.inpOptions" :key="a.admissionId"
                     :label="`${String(a.admissionId).slice(-6)} ${a.deptName || ''}`" :value="a.admissionId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="感染来源">
        <el-select v-model="caseForm.caseSource" style="width:100%">
          <el-option v-for="d in caseSourceDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
        </el-select>
      </el-form-item>
      <el-form-item label="感染部位">
        <el-select v-model="caseForm.infectionSite" data-testid="case-site" filterable placeholder="选择部位"
                   style="width:100%">
          <el-option v-for="d in siteDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
        </el-select>
      </el-form-item>
      <el-form-item label="感染诊断">
        <el-input v-model="caseForm.infectionDiag" data-testid="case-diag"/>
      </el-form-item>
      <el-form-item label="病原菌">
        <el-input v-model="caseForm.pathogen" placeholder="可选"/>
      </el-form-item>
      <el-form-item label="标本来源">
        <el-input v-model="caseForm.specimen" placeholder="可选"/>
      </el-form-item>
      <el-form-item label="感染日期">
        <el-date-picker v-model="caseForm.infectDate" data-testid="case-infect-date" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="caseForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'emr:infectionMonitor:add'" data-testid="case-save" type="primary" @click="onSaveCase">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 院感报卡 / 漏报补报建卡弹窗（自 InfectionMonitorView 拆出，建卡只有新增、无修改）
 *
 * leak=真 走「漏报补报建卡」（leakFlag=1，统计单列）；就诊锚点（门诊挂号单/住院记录）在选患者后自取。
 * 感染来源与部位字典由页面统一加载后传入（筛选/列表列同一份数据）。
 */
import {computed, reactive, watch} from 'vue'
import {ElMessage} from 'element-plus'
import PatientSelect from '@/components/his/PatientSelect.vue'
import request from '@/api/request'
import {caseUpsert} from '@/api/infectionMonitor'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 漏报补报口径（true=漏报补报建卡）
  leak: {type: Boolean, default: false},
  // 感染来源字典（页面已加载）
  caseSourceDict: {type: Array, default: () => []},
  // 感染部位字典（页面已加载）
  siteDict: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})
const title = computed(() => props.leak ? '漏报补报建卡' : '院感报卡')

// 就诊锚点选项（门诊挂号单 / 住院记录）
const anchor = reactive({registOptions: [], inpOptions: [], registId: null, inpId: null})
const loadRegists = async (patientId) => {
  anchor.registOptions = [];
  anchor.inpOptions = []
  anchor.registId = null;
  anchor.inpId = null
  if (!patientId) return
  try {
    const [r1, r2] = await Promise.all([
      request.get('/appoint/listPage', {params: {patientId, pageNum: 1, pageSize: 20}}),
      request.get('/patient/inpatient/listPage', {params: {patientId, pageNum: 1, pageSize: 20}}),
    ])
    if (r1.code === 200) anchor.registOptions = r1.data?.records || []
    if (r2.code === 200) anchor.inpOptions = r2.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

const caseForm = reactive({
  id: null,
  patientId: null,
  patientName: '',
  visitType: 2,
  caseSource: 2,
  infectionSite: null,
  infectionDiag: '',
  pathogen: '',
  specimen: '',
  infectDate: '',
  leakFlag: 0,
  remark: ''
})

// 每次打开按 leak 口径重置（与拆分前 onCaseCreate 一致）
watch(() => props.modelValue, (v) => {
  if (!v) return
  Object.assign(caseForm, {
    id: null,
    patientId: null,
    patientName: '',
    visitType: 2,
    caseSource: 2,
    infectionSite: null,
    infectionDiag: '',
    pathogen: '',
    specimen: '',
    infectDate: '',
    leakFlag: props.leak ? 1 : 0,
    remark: ''
  })
  anchor.registOptions = [];
  anchor.inpOptions = [];
  anchor.registId = null;
  anchor.inpId = null
})

const onCasePatientSelect = async (p) => {
  caseForm.patientName = p?.patientName || '';
  await loadRegists(p?.id)
}

const onSaveCase = async () => {
  if (!caseForm.patientId) return ElMessage.warning('请选择患者')
  if (caseForm.visitType === 1 && !anchor.registId) return ElMessage.warning('门诊病例必须选门诊就诊')
  if (caseForm.visitType === 2 && !anchor.inpId) return ElMessage.warning('住院病例必须选住院记录')
  if (!caseForm.infectionSite) return ElMessage.warning('请选择感染部位')
  if (!caseForm.infectionDiag?.trim()) return ElMessage.warning('请填写感染诊断')
  if (!caseForm.infectDate) return ElMessage.warning('请选择感染日期')
  try {
    const res = await caseUpsert({
      id: caseForm.id, patientId: caseForm.patientId, visitType: caseForm.visitType,
      registId: caseForm.visitType === 1 ? anchor.registId : null,
      inpId: caseForm.visitType === 2 ? anchor.inpId : null,
      caseSource: caseForm.caseSource, infectionSite: caseForm.infectionSite,
      infectionDiag: caseForm.infectionDiag, pathogen: caseForm.pathogen || undefined,
      specimen: caseForm.specimen || undefined, infectDate: caseForm.infectDate,
      leakFlag: caseForm.leakFlag || undefined, remark: caseForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '报卡已保存')
      visible.value = false
      emit('saved')
    } 
  } catch (e) {
    console.error(e)
  }
}
</script>
