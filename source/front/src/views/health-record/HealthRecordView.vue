<script setup lang="js">
/**
 * 健康档案 —— 患者六组纵向数据的唯一维护出口。
 *
 * 这个文件原先是个 45 行的硬编码演示壳（「建档总数 286,420」这类假数字 + 4 行假表格），
 * 没有任何菜单指向它，后端那六组接口也没有一处前端调用方 —— 于是六张结构化表长期是空的：
 * 实测 92 份有效档案里，过敏史明细只有 1 行（文本字段却有 14 人有值）、
 * 既往病史 10 行全挂在**不存在的患者号**上、用药史连后端链路都没有。
 * 本次改造把这条闭环补上：真页面 + 六组真能增删改 + 与医生站共用同一份明细。
 *
 * 三条设计口径：
 *  1. **明细只有一份实现**：六组共用一张表 + 一个对话框，字段由 `GROUP_FIELDS` 配置驱动。
 *     不做六套几乎一样的模板 —— 那种写法必然出现「过敏史能改日期、既往病史忘了加」的漂移。
 *  2. **权威在明细**：主档 `biz_patient` 上的「过敏史 / 既往病史 / 联系人」三列是明细的**投影**，
 *     由服务端在每次写之后重算。页面把投影作为只读信息展示，并明确写出「改动明细会自动同步」，
 *     不让用户以为有两个地方可以改。
 *  3. **分叉要看得见**：历史数据里「文本有值、明细为空」的档案在页面上不可维护。
 *     这种情况给一条明确的提示 + 「转成明细条目」按钮（把文本填进新增对话框），
 *     而不是让它悄悄消失。
 */
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Delete, Edit } from '@element-plus/icons-vue'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {
  getPatientHealthProfile,
  saveAllergy, deleteAllergy,
  savePastDisease, deletePastDisease,
  saveSurgeryHistory, deleteSurgeryHistory,
  saveFamilyHistory, deleteFamilyHistory,
  saveMedicationHistory, deleteMedicationHistory,
  savePatientContact, deletePatientContact,
} from '@/api/patient'
import { DICT_TYPE, loadDictDataMap } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { patientGenderText, patientAgeText } from '@/lib/patientGender'
import {
  HEALTH_GROUPS,
  ALLERGY_TYPE_OPTIONS, ALLERGY_SEVERITY_OPTIONS,
  SURGERY_TYPE_OPTIONS, RECOVERY_STATUS_OPTIONS, DISEASE_STATUS_OPTIONS,
  DRUG_TYPE_OPTIONS, DRUG_ROUTE_OPTIONS, MEDICATION_STATUS_OPTIONS,
  ALIVE_OPTIONS, severityTone, medicationStatusTone,
} from '@/lib/healthProfile'

/* ==================== 六组字段配置（表格列 + 对话框字段共用一个来源） ==================== */
// type: text | select | date | number | switch
// options: 静态数组，或 'PATIENT_RELATION'（用 sys_patient_relation 字典）
const GROUP_FIELDS = {
  allergy: [
    { key: 'allergenName', label: '过敏原', type: 'text', required: true, width: 150 },
    // 类型必填：表列 allergy_type 是 NOT NULL 且取值只有药物/食物/其他（没有"未知"档），
    // 不标必填的话用户不选就会拿到后端 500，这里提前拦住并说清缺什么
    { key: 'allergyType', label: '类型', type: 'select', options: ALLERGY_TYPE_OPTIONS, required: true, width: 100 },
    { key: 'allergySeverity', label: '严重程度', type: 'select', options: ALLERGY_SEVERITY_OPTIONS, width: 120, tone: severityTone },
    { key: 'allergySymptoms', label: '反应表现', type: 'text', minWidth: 180, placeholder: '如：全身皮疹、呼吸困难' },
    { key: 'allergyDate', label: '首次发生', type: 'date', width: 130 },
    { key: 'occurrenceCount', label: '次数', type: 'number', width: 80 },
    { key: 'treatmentGiven', label: '处理措施', type: 'text', minWidth: 180 },
    { key: 'confirmedBy', label: '确认医生', type: 'text', width: 120 },
  ],
  pastDisease: [
    { key: 'diseaseName', label: '疾病名称', type: 'text', required: true, minWidth: 200 },
    { key: 'diseaseCode', label: 'ICD-10', type: 'text', width: 110, placeholder: '如 I10' },
    { key: 'diagnosisDate', label: '诊断日期', type: 'date', width: 130 },
    { key: 'diagnosisDept', label: '诊断科室', type: 'text', width: 140 },
    { key: 'currentStatus', label: '控制情况', type: 'select', options: DISEASE_STATUS_OPTIONS, width: 110 },
    { key: 'treatmentPlan', label: '治疗方案', type: 'text', minWidth: 200 },
    { key: 'relapseCount', label: '复发次数', type: 'number', width: 100 },
    { key: 'lastFollowupDate', label: '最近随访', type: 'date', width: 130 },
  ],
  surgery: [
    { key: 'surgeryName', label: '手术名称', type: 'text', required: true, minWidth: 200 },
    { key: 'surgeryDate', label: '手术日期', type: 'date', required: true, width: 130 },
    { key: 'surgeryType', label: '手术类型', type: 'select', options: SURGERY_TYPE_OPTIONS, width: 110 },
    { key: 'surgeon', label: '主刀医生', type: 'text', width: 120 },
    { key: 'anesthesiaType', label: '麻醉方式', type: 'text', width: 120 },
    { key: 'hospitalName', label: '手术医院', type: 'text', width: 160 },
    { key: 'postopDiagnosis', label: '术后诊断', type: 'text', minWidth: 170 },
    { key: 'recoveryStatus', label: '恢复情况', type: 'select', options: RECOVERY_STATUS_OPTIONS, width: 110 },
    { key: 'complications', label: '术后并发症', type: 'text', minWidth: 160 },
  ],
  family: [
    // relationship 在这一组存**称谓文案**（父亲/母亲/伯父），与联系人那一组的数字码值相反
    { key: 'relationship', label: '与患者关系', type: 'text', required: true, width: 120, placeholder: '如：父亲、母亲、伯父' },
    { key: 'name', label: '亲属姓名', type: 'text', width: 120 },
    { key: 'age', label: '年龄', type: 'number', width: 80 },
    { key: 'isAlive', label: '在世情况', type: 'switch', options: ALIVE_OPTIONS, width: 110 },
    { key: 'causeOfDeath', label: '死亡原因', type: 'text', width: 150, placeholder: '已故必填' },
    { key: 'healthStatus', label: '健康状况', type: 'text', minWidth: 180 },
    { key: 'hereditaryDisease', label: '遗传性疾病', type: 'text', width: 160 },
    { key: 'infectiousDisease', label: '传染病史', type: 'text', width: 140 },
  ],
  medication: [
    { key: 'drugName', label: '药物名称', type: 'text', required: true, minWidth: 180 },
    { key: 'drugType', label: '药物类型', type: 'select', options: DRUG_TYPE_OPTIONS, width: 110 },
    { key: 'dosage', label: '剂量', type: 'text', width: 100, placeholder: '如 0.5g' },
    { key: 'frequency', label: '频次', type: 'text', width: 90, placeholder: '如 bid' },
    { key: 'route', label: '给药途径', type: 'select', options: DRUG_ROUTE_OPTIONS, width: 110 },
    { key: 'startDate', label: '开始用药', type: 'date', required: true, width: 130 },
    { key: 'endDate', label: '停药日期', type: 'date', width: 130 },
    { key: 'status', label: '用药状态', type: 'select', options: MEDICATION_STATUS_OPTIONS, width: 110, tone: medicationStatusTone },
    { key: 'indications', label: '用药指征', type: 'text', minWidth: 160 },
    { key: 'prescriber', label: '处方医生', type: 'text', width: 120 },
    { key: 'reasonStop', label: '停药原因', type: 'text', width: 150 },
  ],
  contact: [
    { key: 'contactName', label: '联系人', type: 'text', required: true, width: 120 },
    // relationship 在这一组是 sys_patient_relation 的**码值**（tinyint），不是文案
    // 表列 relationship 是 NOT NULL 的字典码值：标必填，别让用户提交后才被后端退回
    { key: 'relationship', label: '与患者关系', type: 'select', options: 'PATIENT_RELATION', dictLabel: 'relationshipText', required: true, width: 130 },
    { key: 'phone', label: '联系电话', type: 'text', width: 140, placeholder: '11 位手机号' },
    { key: 'isPrimary', label: '主要联系人', type: 'switch', options: [{ label: '是', value: 1 }, { label: '否', value: 0 }], width: 120 },
    { key: 'address', label: '联系地址', type: 'text', minWidth: 200 },
  ],
}

/** 组的 key（后端 PatientHealthProfileVO 的字段名） */
const PROFILE_FIELD = {
  allergy: 'allergies',
  pastDisease: 'pastDiseases',
  surgery: 'surgeryHistories',
  family: 'familyHistories',
  medication: 'medications',
  contact: 'contacts',
}

const SAVE_API = {
  allergy: saveAllergy, pastDisease: savePastDisease, surgery: saveSurgeryHistory,
  family: saveFamilyHistory, medication: saveMedicationHistory, contact: savePatientContact,
}
const DELETE_API = {
  allergy: deleteAllergy, pastDisease: deletePastDisease, surgery: deleteSurgeryHistory,
  family: deleteFamilyHistory, medication: deleteMedicationHistory, contact: deletePatientContact,
}

/* ==================== 状态 ==================== */
const route = useRoute()
// 从患者详情弹框跳过来时会带 patientId（雪花 ID 是字符串，别转 Number —— 会丢精度）
const patientId = ref(route.query.patientId ? String(route.query.patientId) : '')
const profile = ref(null)
const loading = ref(false)
const failed = ref(false)
const activeGroup = ref('allergy')

const relationOptions = ref([])

const dialog = reactive({ visible: false, group: 'allergy', form: {}, saving: false })
const isEdit = computed(() => dialog.form && dialog.form.id != null)

const loadProfile = async () => {
  if (!patientId.value) {
    profile.value = null
    return
  }
  loading.value = true
  failed.value = false
  try {
    const res = await getPatientHealthProfile(patientId.value)
    if (res?.code === 200 && res.data) {
      profile.value = res.data
    } else {
      failed.value = true
      ElMessage.error(res?.message || '加载健康档案失败')
    }
  } catch (e) {
    console.error('加载健康档案失败', e)
    failed.value = true
  } finally {
    loading.value = false
  }
}

const loadDicts = async () => {
  const map = await loadDictDataMap(DICT_TYPE.SYS_PATIENT_RELATION)
  // 字典出参兼容 dictValue/value 两种形状（后端 SysDictData 用 dictValue）
  relationOptions.value = (map[DICT_TYPE.SYS_PATIENT_RELATION] || []).map(d => ({
    label: d.dictLabel ?? d.label,
    value: Number(d.dictValue ?? d.value),
  }))
}

onMounted(() => {
  loadDicts()
  if (patientId.value) {
    loadProfile()
  }
})

// 带着 patientId 直达时（从患者详情弹框跳过来），之后在同一路由内换了 query 也要跟上
watch(() => route.query.patientId, (v) => {
  const next = v ? String(v) : ''
  if (next !== patientId.value) {
    patientId.value = next
  }
})

watch(patientId, () => {
  dialog.visible = false
  activeGroup.value = 'allergy'
  loadProfile()
})

/* ==================== 派生 ==================== */
const rowsOf = (group) => profile.value?.[PROFILE_FIELD[group]] || []
const countOf = (group) => rowsOf(group).length

/** 该组是否「只有文本、没有明细」——历史遗留的自由文本，页面上不可维护 */
const textOnlyOf = (group) => {
  if (!profile.value) return false
  if (group === 'allergy') return !!profile.value.allergyTextOnly
  if (group === 'pastDisease') return !!profile.value.pastDiseaseTextOnly
  if (group === 'contact') return !!profile.value.contactTextOnly
  return false
}

/** 该组在主档上的投影文本快照（只读展示，由服务端维护） */
const projectionOf = (group) => {
  const p = profile.value
  if (!p) return ''
  if (group === 'allergy') return p.allergyHistoryText || ''
  if (group === 'pastDisease') return p.medicalHistoryText || ''
  if (group === 'contact') {
    return [p.contactNameText, p.contactPhoneText, p.contactRelationText].filter(Boolean).join(' / ')
  }
  return ''
}

/** 分叉时用来预填新增对话框的原始文本 */
const legacyTextOf = (group) => {
  const p = profile.value
  if (!p) return ''
  if (group === 'allergy') return p.allergyHistoryText || ''
  if (group === 'pastDisease') return p.medicalHistoryText || ''
  if (group === 'contact') return [p.contactNameText, p.contactPhoneText].filter(Boolean).join(' / ')
  return ''
}

const totalCount = computed(() =>
    profile.value ? HEALTH_GROUPS.reduce((s, g) => s + countOf(g.key), 0) : 0)

/* ==================== 对话框 ==================== */
const fieldsOf = (group) => GROUP_FIELDS[group] || []

const optionsOf = (f) => {
  if (f.options === 'PATIENT_RELATION') return relationOptions.value
  return f.options || []
}

/** 下拉/开关的选项值：字符串数组直接当 label=value，对象数组取 value */
const optValue = (o) => (o !== null && typeof o === 'object' ? o.value : o)
const optLabel = (o) => (o !== null && typeof o === 'object' ? o.label : o)

const openAdd = (group, preset = {}) => {
  dialog.group = group
  dialog.form = { patientId: patientId.value, ...preset }
  dialog.visible = true
}

const openEdit = (group, row) => {
  dialog.group = group
  dialog.form = { ...row, patientId: patientId.value }
  dialog.visible = true
}

/** 「文本转明细」：把主档那段自由文本填进新增对话框，让用户补齐结构化字段后保存 */
const convertLegacyText = (group) => {
  if (group === 'allergy') {
    openAdd(group, { allergenName: legacyTextOf(group) })
  } else if (group === 'pastDisease') {
    openAdd(group, { diseaseName: legacyTextOf(group) })
  } else if (group === 'contact') {
    const p = profile.value || {}
    openAdd(group, { contactName: p.contactNameText || '', phone: p.contactPhoneText || '' })
  }
}

const submitDialog = async () => {
  const group = dialog.group
  // 必填校验放在提交前（后端也有校验，但等一个网络往返再报错体验差）
  for (const f of fieldsOf(group)) {
    if (f.required && (dialog.form[f.key] === undefined || dialog.form[f.key] === null || dialog.form[f.key] === '')) {
      ElMessage.warning(`请填写「${f.label}」`)
      return
    }
  }
  dialog.saving = true
  try {
    const res = await SAVE_API[group]({ ...dialog.form })
    if (res?.code !== 200) {
      ElMessage.error(res?.message || '保存失败')
      return
    }
    ElMessage.success(isEdit.value ? '已保存' : '已新增')
    dialog.visible = false
    await loadProfile()
  } catch (e) {
    // request.js 已统一弹错，这里只需阻止把「已保存」也弹出来
    console.error('保存健康档案失败', e)
  } finally {
    dialog.saving = false
  }
}

const removeRow = async (group, row) => {
  const firstKey = GROUP_FIELDS[group]?.[0]?.key
  const name = row[firstKey] || row.contactName || '这条记录'
  try {
    await ElMessageBox.confirm(
        `确认删除「${name}」？该组主档上的摘要文本会随之重算。`,
        '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  } catch {
    return
  }
  const res = await DELETE_API[group](row.id)
  if (res?.code !== 200) {
    ElMessage.error(res?.message || '删除失败')
    return
  }
  ElMessage.success('已删除')
  await loadProfile()
}

const reload = () => {
  loadDicts()
  loadProfile()
}
</script>

<template>
  <div class="space-y-3">
    <!-- ============ 选患者 ============ -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <span class="text-sm font-medium text-slate-600">选择患者</span>
        <PatientSelect v-model="patientId" width="380px"/>
        <el-button :icon="Refresh" @click="reload">刷新</el-button>
        <div v-if="profile" class="ml-auto flex items-center gap-3 text-sm text-slate-600">
          <span class="font-medium text-slate-800">{{ profile.patientName }}</span>
          <span>{{ patientGenderText(profile.gender) }} / {{ patientAgeText(profile.age) }}</span>
          <span class="font-mono text-xs text-slate-400">{{ profile.patientNo }}</span>
          <el-tag size="small" effect="plain" type="info">六组共 {{ totalCount }} 条明细</el-tag>
        </div>
      </div>
    </div>

    <!-- ============ 未选患者 ============ -->
    <div v-if="!patientId" class="rounded-lg border border-slate-200 bg-white py-20 text-center shadow-sm">
      <p class="text-sm text-slate-400">请先在上方选择患者，再维护其健康档案</p>
      <p class="mt-1 text-xs text-slate-400">
        过敏史 / 既往病史 / 手术外伤史 / 家族史 / 用药史 / 联系人，六组纵向数据都在这里维护
      </p>
    </div>

    <!-- ============ 加载失败 ============ -->
    <div v-else-if="failed && !loading" class="rounded-lg border border-slate-200 bg-white py-20 text-center shadow-sm">
      <p class="text-sm text-slate-400">健康档案加载失败</p>
      <el-button link type="primary" class="mt-2" @click="loadProfile">重新加载</el-button>
    </div>

    <!-- ============ 六组 ============ -->
    <div v-else v-loading="loading" class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-tabs v-model="activeGroup" class="px-4 pt-2">
        <el-tab-pane v-for="g in HEALTH_GROUPS" :key="g.key" :name="g.key">
          <template #label>
            <span>{{ g.label }}</span>
            <span class="ml-1 text-xs text-slate-400">({{ countOf(g.key) }})</span>
          </template>

          <!-- 主档投影（只读）+ 历史文本分叉提示 -->
          <div class="mb-3 space-y-2">
            <div v-if="projectionOf(g.key)" class="rounded border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-500">
              <span class="font-medium text-slate-600">患者主档摘要</span>
              <span class="ml-2 text-slate-700">{{ projectionOf(g.key) }}</span>
              <span class="ml-2 text-slate-400">（由下方明细自动同步，无需在此修改）</span>
            </div>

            <el-alert v-if="textOnlyOf(g.key)" type="warning" :closable="false" show-icon>
              <template #title>
                这条{{ g.label }}目前只存在于患者主档的自由文本里，没有结构化明细
              </template>
              <div class="text-xs">
                文本内容：<span class="font-medium">{{ legacyTextOf(g.key) }}</span>
                <el-button v-perm="'patient:profile:add'" link type="primary" class="ml-2" @click="convertLegacyText(g.key)">
                  转成明细条目
                </el-button>
                <span class="ml-2 text-slate-400">（只存在于文本的记录改不了严重程度、日期等字段）</span>
              </div>
            </el-alert>
          </div>

          <!-- 明细表：六组共用同一套渲染，字段由 GROUP_FIELDS 驱动 -->
          <div class="mb-2 flex items-center justify-between">
            <span class="text-xs text-slate-400">
              {{ rowsOf(g.key).length ? `共 ${rowsOf(g.key).length} 条` : '暂无明细' }}
            </span>
            <el-button v-perm="'patient:profile:add'" type="primary" size="small" :icon="Plus" @click="openAdd(g.key)">新增{{ g.label }}</el-button>
          </div>

          <el-table :data="rowsOf(g.key)" size="small" style="width: 100%" :empty-text="'暂无' + g.label">
            <el-table-column
                v-for="f in fieldsOf(g.key)"
                :key="f.key"
                :label="f.label"
                :width="f.width"
                :min-width="f.minWidth"
                show-overflow-tooltip
            >
              <template #default="{ row }">
                <!-- 字典码值列：优先用出参里翻译好的文案，兜底查字典，命中不了渲染「未知(n)」 -->
                <template v-if="f.options === 'PATIENT_RELATION'">
                  {{ row[f.dictLabel] || dictLabelText(relationOptions, row[f.key]) }}
                </template>
                <template v-else-if="f.type === 'switch'">
                  <!-- 命中不了选项就渲染「未知(n)」，不静默显示成空或当成第一个选项 -->
                  <el-tag size="small" effect="plain" :type="row[f.key] === optValue(f.options[0]) ? 'success' : 'info'">
                    {{ optLabel(f.options.find(o => optValue(o) === row[f.key])) ?? `未知(${row[f.key]})` }}
                  </el-tag>
                </template>
                <template v-else-if="f.tone">
                  <el-tag v-if="row[f.key]" size="small" effect="plain" :type="f.tone(row[f.key])">{{ row[f.key] }}</el-tag>
                  <span v-else class="text-slate-300">—</span>
                </template>
                <template v-else>
                  {{ row[f.key] === null || row[f.key] === undefined || row[f.key] === '' ? '—' : row[f.key] }}
                </template>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right" align="center">
              <template #default="{ row }">
                <el-button v-perm="'patient:profile:edit'" link type="primary" size="small" :icon="Edit" @click="openEdit(g.key, row)">编辑</el-button>
                <el-button v-perm="'patient:profile:delete'" link type="danger" size="small" :icon="Delete" @click="removeRow(g.key, row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ============ 新增/编辑对话框（六组共用） ============ -->
    <el-dialog
        v-model="dialog.visible"
        :title="(isEdit ? '编辑' : '新增') + (HEALTH_GROUPS.find(g => g.key === dialog.group) || {}).label"
        width="720px"
        destroy-on-close
    >
      <el-form :model="dialog.form" label-width="110px" class="pr-2">
        <el-form-item
            v-for="f in fieldsOf(dialog.group)"
            :key="f.key"
            :label="f.label"
            :required="!!f.required"
        >
          <el-select
              v-if="f.type === 'select'"
              v-model="dialog.form[f.key]"
              :placeholder="'请选择' + f.label"
              clearable
              class="w-full"
          >
            <el-option
                v-for="o in optionsOf(f)"
                :key="optValue(o)"
                :label="optLabel(o)"
                :value="optValue(o)"
            />
          </el-select>
          <el-date-picker
              v-else-if="f.type === 'date'"
              v-model="dialog.form[f.key]"
              type="date"
              value-format="YYYY-MM-DD"
              :placeholder="'请选择' + f.label"
              class="w-full"
          />
          <el-input-number
              v-else-if="f.type === 'number'"
              v-model="dialog.form[f.key]"
              :min="0"
              class="w-full"
          />
          <el-switch
              v-else-if="f.type === 'switch'"
              v-model="dialog.form[f.key]"
              :active-value="optValue(f.options[0])"
              :inactive-value="optValue(f.options[1])"
              :active-text="optLabel(f.options[0])"
              :inactive-text="optLabel(f.options[1])"
          />
          <el-input
              v-else
              v-model="dialog.form[f.key]"
              :placeholder="f.placeholder || ('请输入' + f.label)"
              clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button v-perm="['patient:profile:add', 'patient:profile:edit']" type="primary" :loading="dialog.saving" @click="submitDialog">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
