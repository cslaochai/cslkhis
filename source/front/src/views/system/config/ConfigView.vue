<template>
  <div class="p-6">
    <el-card v-loading="loading" shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ==================== 医院基础信息 ==================== -->
        <el-tab-pane label="医院基础信息" name="hospital">
          <el-form ref="hospitalFormRef" :model="hospitalConfig" :rules="hospitalRules" class="max-w-xl"
                   label-width="120px">
            <el-form-item label="医院名称" prop="hospitalName">
              <el-input v-model="hospitalConfig.hospitalName"/>
            </el-form-item>
            <el-form-item label="医院地址" prop="hospitalAddress">
              <el-input v-model="hospitalConfig.hospitalAddress"/>
            </el-form-item>
            <el-form-item label="联系电话" prop="hospitalPhone">
              <el-input v-model="hospitalConfig.hospitalPhone"/>
            </el-form-item>
            <el-form-item label="邮箱" prop="hospitalEmail">
              <el-input v-model="hospitalConfig.hospitalEmail"/>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveHospital">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== 急诊 ==================== -->
        <el-tab-pane label="急诊" name="emergency">
          <el-form ref="emergencyFormRef" :model="emergencyConfig" :rules="emergencyRules" class="max-w-xl"
                   label-width="160px">
            <el-form-item label="Ⅰ级濒危候诊时限（分钟）" prop="waitDeadlineLevel1">
              <el-input-number v-model="emergencyConfig.waitDeadlineLevel1" :min="0" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="Ⅱ级危重候诊时限（分钟）" prop="waitDeadlineLevel2">
              <el-input-number v-model="emergencyConfig.waitDeadlineLevel2" :min="0" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="Ⅲ级急症候诊时限（分钟）" prop="waitDeadlineLevel3">
              <el-input-number v-model="emergencyConfig.waitDeadlineLevel3" :min="0" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="Ⅳ级非急症候诊时限（分钟）" prop="waitDeadlineLevel4">
              <el-input-number v-model="emergencyConfig.waitDeadlineLevel4" :min="0" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="候诊催办兜底接收人" prop="waitFallbackReceiver">
              <el-input v-model="emergencyConfig.waitFallbackReceiver"
                        placeholder="用户名或员工ID，催办无人接收时兜底"/>
            </el-form-item>
            <el-form-item label="留观预警时长（小时）" prop="observationWarnHours">
              <el-input-number v-model="emergencyConfig.observationWarnHours" :min="0" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="留观最大时长（小时）" prop="observationMaxHours">
              <el-input-number v-model="emergencyConfig.observationMaxHours" :min="0" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveEmergency">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== 检验危急值 ==================== -->
        <el-tab-pane label="检验危急值" name="lab">
          <el-form ref="labFormRef" :model="labConfig" :rules="labRules" class="max-w-xl" label-width="160px">
            <el-form-item label="危急值处理时限（分钟）" prop="criticalValueDeadlineMinutes">
              <el-input-number v-model="labConfig.criticalValueDeadlineMinutes" :min="0" :max="10000"
                               controls-position="right"/>
            </el-form-item>
            <el-form-item label="危急值兜底接收人" prop="criticalValueFallbackReceiver">
              <el-input v-model="labConfig.criticalValueFallbackReceiver"
                        placeholder="用户名或员工ID，无人接收时兜底"/>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveLab">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== 住院 / 床位 ==================== -->
        <el-tab-pane label="住院 / 床位" name="inpatient">
          <el-form ref="inpatientFormRef" :model="inpatientConfig" :rules="inpatientRules" class="max-w-xl"
                   label-width="160px">
            <el-form-item label="住院请假时长上限（小时）" prop="leaveMaxHours">
              <el-input-number v-model="inpatientConfig.leaveMaxHours" :min="1" :max="100000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="待床等待预警时长（小时）" prop="bedWaitHours">
              <el-input-number v-model="inpatientConfig.bedWaitHours" :min="0" :max="100000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="转诊待处理预警时长（小时）" prop="referralPendingHours">
              <el-input-number v-model="inpatientConfig.referralPendingHours" :min="0" :max="100000"
                               controls-position="right"/>
            </el-form-item>
            <el-form-item label="床位最长可等待（天）" prop="bedWaitMaxDays">
              <el-input-number v-model="inpatientConfig.bedWaitMaxDays" :min="1" :max="10000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="住院证有效期（天）" prop="admissionOrderValidDays">
              <el-input-number v-model="inpatientConfig.admissionOrderValidDays" :min="1" :max="10000"
                               controls-position="right"/>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveInpatient">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== 电子签名 ==================== -->
        <el-tab-pane label="电子签名" name="sign">
          <el-form ref="signFormRef" :model="signConfig" :rules="signRules" class="max-w-xl" label-width="160px">
            <el-form-item label="签名时间来源" prop="timeSource">
              <el-select v-model="signConfig.timeSource" style="width: 260px">
                <el-option label="本机时钟" :value="1"/>
                <el-option label="院内授时" :value="2"/>
              </el-select>
            </el-form-item>
            <el-form-item label="证书有效期（天）" prop="certValidDays">
              <el-input-number v-model="signConfig.certValidDays" :min="1" :max="100000" controls-position="right"/>
            </el-form-item>
            <el-form-item label="缺证书自动签发" prop="certAutoIssue">
              <el-switch v-model="signConfig.certAutoIssue" :active-value="1" :inactive-value="0"/>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveSign">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== DRG 分组方案 ==================== -->
        <el-tab-pane label="DRG 分组方案" name="drg">
          <el-form ref="drgFormRef" :model="drgConfig" :rules="drgRules" class="max-w-xl" label-width="160px">
            <el-form-item label="结算默认版本" prop="version">
              <el-select v-model="drgConfig.version" style="width: 260px">
                <el-option label="chs-drg-2.0" value="chs-drg-2.0"/>
                <el-option label="3.0" value="3.0"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveDrg">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== 医保付费 ==================== -->
        <el-tab-pane label="医保付费" name="medpay">
          <el-form ref="medpayFormRef" :model="medpayConfig" :rules="medpayRules" class="max-w-xl" label-width="160px">
            <el-form-item label="住院结算主付费方式" prop="mode">
              <el-select v-model="medpayConfig.mode" style="width: 260px">
                <el-option label="DRG（按病组付费）" value="DRG"/>
                <el-option label="DIP（按病种分值付费）" value="DIP"/>
              </el-select>
              <div class="form-tip">主结算方式由统筹区确定（医保发〔2025〕18号按病种付费办法），DRG/DIP 二选一</div>
            </el-form-item>
            <el-form-item label="DIP 病种库默认版本" prop="dipVersion">
              <el-select v-model="medpayConfig.dipVersion" style="width: 260px">
                <el-option label="2.0" value="2.0"/>
                <el-option label="3.0" value="3.0"/>
              </el-select>
            </el-form-item>
            <el-form-item label="DIP 点值（元/分值）" prop="pointValue">
              <el-input v-model="medpayConfig.pointValue" placeholder="不填表示未配置，结算金额不计算" style="width: 260px"/>
            </el-form-item>
            <el-form-item>
              <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                         @click="handleSaveMedpay">保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Check} from '@element-plus/icons-vue'
import {
  getDrgDefaultVersion,
  getEmergencyConfig,
  getHospitalConfig,
  getInpatientConfig,
  getLabCriticalConfig,
  getSignConfig,
  saveDrgDefaultVersion,
  saveEmergencyConfig,
  saveHospitalConfig,
  saveInpatientConfig,
  saveLabCriticalConfig,
  saveSignConfig,
} from '../../../api/system'
import {dipMedpayConfig, saveDipMedpayConfig} from '../../../api/dip'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('hospital')

// ---------------- 医院基础信息 ----------------
const hospitalFormRef = ref()
const hospitalConfig = ref({hospitalName: '', hospitalAddress: '', hospitalPhone: '', hospitalEmail: ''})
const hospitalRules = {
  hospitalName: [{required: true, message: '请输入医院名称', trigger: 'blur'}],
  hospitalAddress: [{required: true, message: '请输入医院地址', trigger: 'blur'}],
  hospitalPhone: [{required: true, message: '请输入联系电话', trigger: 'blur'}],
}
const handleSaveHospital = async () => {
  await hospitalFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveHospitalConfig(hospitalConfig.value)
    if (res.code === 200) {
      ElMessage.success('医院基础信息保存成功')
    }
  } finally {
    saving.value = false
  }
}

// ---------------- 急诊 ----------------
const emergencyFormRef = ref()
const emergencyConfig = ref({
  waitDeadlineLevel1: undefined,
  waitDeadlineLevel2: undefined,
  waitDeadlineLevel3: undefined,
  waitDeadlineLevel4: undefined,
  waitFallbackReceiver: '',
  observationWarnHours: undefined,
  observationMaxHours: undefined,
})
const emergencyRules = {
  waitDeadlineLevel1: [{required: true, message: '请输入Ⅰ级濒危候诊时限', trigger: 'blur'}],
  waitDeadlineLevel2: [{required: true, message: '请输入Ⅱ级危重候诊时限', trigger: 'blur'}],
  waitDeadlineLevel3: [{required: true, message: '请输入Ⅲ级急症候诊时限', trigger: 'blur'}],
  waitDeadlineLevel4: [{required: true, message: '请输入Ⅳ级非急症候诊时限', trigger: 'blur'}],
  observationWarnHours: [{required: true, message: '请输入留观预警时长', trigger: 'blur'}],
  observationMaxHours: [{required: true, message: '请输入留观最大时长', trigger: 'blur'}],
}
const handleSaveEmergency = async () => {
  await emergencyFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveEmergencyConfig(emergencyConfig.value)
    if (res.code === 200) {
      ElMessage.success('急诊参数保存成功')
    }
  } finally {
    saving.value = false
  }
}

// ---------------- 检验危急值 ----------------
const labFormRef = ref()
const labConfig = ref({criticalValueDeadlineMinutes: undefined, criticalValueFallbackReceiver: ''})
const labRules = {
  criticalValueDeadlineMinutes: [{required: true, message: '请输入危急值处理时限', trigger: 'blur'}],
}
const handleSaveLab = async () => {
  await labFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveLabCriticalConfig(labConfig.value)
    if (res.code === 200) {
      ElMessage.success('检验危急值参数保存成功')
    }
  } finally {
    saving.value = false
  }
}

// ---------------- 住院 / 床位 ----------------
const inpatientFormRef = ref()
const inpatientConfig = ref({
  leaveMaxHours: undefined,
  bedWaitHours: undefined,
  referralPendingHours: undefined,
  bedWaitMaxDays: undefined,
  admissionOrderValidDays: undefined,
})
const inpatientRules = {
  leaveMaxHours: [{required: true, message: '请输入住院请假时长上限', trigger: 'blur'}],
  bedWaitHours: [{required: true, message: '请输入待床等待预警时长', trigger: 'blur'}],
  referralPendingHours: [{required: true, message: '请输入转诊待处理预警时长', trigger: 'blur'}],
  bedWaitMaxDays: [{required: true, message: '请输入床位最长可等待天数', trigger: 'blur'}],
  admissionOrderValidDays: [{required: true, message: '请输入住院证有效期', trigger: 'blur'}],
}
const handleSaveInpatient = async () => {
  await inpatientFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveInpatientConfig(inpatientConfig.value)
    if (res.code === 200) {
      ElMessage.success('住院 / 床位参数保存成功')
    }
  } finally {
    saving.value = false
  }
}

// ---------------- 电子签名 ----------------
const signFormRef = ref()
const signConfig = ref({timeSource: 1, certValidDays: undefined, certAutoIssue: 1})
const signRules = {
  timeSource: [{required: true, message: '请选择签名时间来源', trigger: 'change'}],
  certValidDays: [{required: true, message: '请输入证书有效期', trigger: 'blur'}],
}
const handleSaveSign = async () => {
  await signFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveSignConfig(signConfig.value)
    if (res.code === 200) {
      ElMessage.success('电子签名参数保存成功')
    }
  } finally {
    saving.value = false
  }
}

// ---------------- DRG 分组方案 ----------------
const drgFormRef = ref()
const drgConfig = ref({version: '3.0'})
const drgRules = {
  version: [{required: true, message: '请选择结算默认版本', trigger: 'change'}],
}
const handleSaveDrg = async () => {
  await drgFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveDrgDefaultVersion(drgConfig.value.version)
    if (res.code === 200) {
      ElMessage.success('DRG 结算默认版本保存成功')
      loadDrgConfig()
    }
  } finally {
    saving.value = false
  }
}

// ---------------- 医保付费 ----------------
const medpayFormRef = ref()
const medpayConfig = ref({mode: 'DRG', dipVersion: '2.0', pointValue: ''})
const medpayRules = {
  mode: [{required: true, message: '请选择住院结算主付费方式', trigger: 'change'}],
  dipVersion: [{required: true, message: '请选择 DIP 病种库默认版本', trigger: 'change'}],
}
const handleSaveMedpay = async () => {
  await medpayFormRef.value.validate()
  saving.value = true
  try {
    const res = await saveDipMedpayConfig(medpayConfig.value)
    if (res.code === 200) {
      ElMessage.success('医保付费参数保存成功')
      loadMedpayConfig()
    }
  } finally {
    saving.value = false
  }
}

// ---------------- 加载 ----------------
const loadMedpayConfig = async () => {
  const res = await dipMedpayConfig()
  if (res.code === 200 && res.data) {
    medpayConfig.value = {...medpayConfig.value, ...res.data}
  }
}

const loadDrgConfig = async () => {
  const res = await getDrgDefaultVersion()
  if (res.code === 200 && res.data) {
    drgConfig.value.version = res.data.dbVersion || '3.0'
  }
}

const loadConfig = async () => {
  loading.value = true
  try {
    const [hospital, emergency, lab, inpatient, sign] = await Promise.all([
      getHospitalConfig(), getEmergencyConfig(), getLabCriticalConfig(), getInpatientConfig(), getSignConfig(),
    ])
    if (hospital.code === 200 && hospital.data) {
      hospitalConfig.value = {...hospitalConfig.value, ...hospital.data}
    }
    if (emergency.code === 200 && emergency.data) {
      emergencyConfig.value = {...emergencyConfig.value, ...emergency.data}
    }
    if (lab.code === 200 && lab.data) {
      labConfig.value = {...labConfig.value, ...lab.data}
    }
    if (inpatient.code === 200 && inpatient.data) {
      inpatientConfig.value = {...inpatientConfig.value, ...inpatient.data}
    }
    if (sign.code === 200 && sign.data) {
      signConfig.value = {...signConfig.value, ...sign.data}
    }
    await Promise.all([loadDrgConfig(), loadMedpayConfig()])
  } catch (e) {
    console.error('加载参数配置失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadConfig)
</script>
