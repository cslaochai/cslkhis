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
          <el-tag effect="plain" size="small" type="info">六组共 {{ totalCount }} 条明细</el-tag>
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
      <el-button class="mt-2" link type="primary" @click="loadProfile">重新加载</el-button>
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
            <div v-if="projectionOf(g.key)"
                 class="rounded border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-500">
              <span class="font-medium text-slate-600">患者主档摘要</span>
              <span class="ml-2 text-slate-700">{{ projectionOf(g.key) }}</span>
              <span class="ml-2 text-slate-400">（由下方明细自动同步，无需在此修改）</span>
            </div>

            <el-alert v-if="textOnlyOf(g.key)" :closable="false" show-icon type="warning">
              <template #title>
                这条{{ g.label }}目前只存在于患者主档的自由文本里，没有结构化明细
              </template>
              <div class="text-xs">
                文本内容：<span class="font-medium">{{ legacyTextOf(g.key) }}</span>
                <el-button v-perm="'patient:profile:add'" class="ml-2" link type="primary"
                           @click="convertLegacyText(g.key)">
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
            <el-button v-perm="'patient:profile:add'" :icon="Plus" size="small" type="primary" @click="openAdd(g.key)">
              新增{{ g.label }}
            </el-button>
          </div>

          <el-table :data="rowsOf(g.key)" :empty-text="'暂无' + g.label" size="small" style="width: 100%">
            <el-table-column
                v-for="f in fieldsOf(g.key)"
                :key="f.key"
                :label="f.label"
                :min-width="f.minWidth"
                :width="f.width"
                show-overflow-tooltip
            >
              <template #default="{ row }">
                <!-- 字典码值列：优先用出参里翻译好的文案，兜底查字典，命中不了渲染「未知(n)」 -->
                <template v-if="f.options === 'PATIENT_RELATION'">
                  {{ row[f.dictLabel] || dictLabelText(relationOptions, row[f.key]) }}
                </template>
                <template v-else-if="f.type === 'switch'">
                  <!-- 命中不了选项就渲染「未知(n)」，不静默显示成空或当成第一个选项 -->
                  <el-tag :type="row[f.key] === optValue(f.options[0]) ? 'success' : 'info'" effect="plain"
                          size="small">
                    {{ optLabel(f.options.find(o => optValue(o) === row[f.key])) ?? `未知(${row[f.key]})` }}
                  </el-tag>
                </template>
                <template v-else-if="f.tone">
                  <el-tag v-if="row[f.key]" :type="f.tone(row[f.key])" effect="plain" size="small">{{
                      row[f.key]
                    }}
                  </el-tag>
                  <span v-else class="text-slate-300">—</span>
                </template>
                <template v-else>
                  {{ row[f.key] === null || row[f.key] === undefined || row[f.key] === '' ? '—' : row[f.key] }}
                </template>
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" label="操作" width="130">
              <template #default="{ row }">
                <el-button v-perm="'patient:profile:edit'" :icon="Edit" link size="small" type="primary"
                           @click="openEdit(g.key, row)">编辑
                </el-button>
                <el-button v-perm="'patient:profile:delete'" :icon="Delete" link size="small" type="danger"
                           @click="removeRow(g.key, row)">删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ============ 新增/编辑对话框（六组共用，已拆至 HealthRecordEdit.vue） ============ -->
    <HealthRecordEdit v-model="dialogVisible" :group="dialogGroup" :patient-id="patientId" :preset="dialogPreset"
                      :relation-options="relationOptions" :row="dialogRow" @saved="loadProfile"/>
  </div>
</template>

<script lang="js" setup>
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
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh} from '@element-plus/icons-vue'
import PatientSelect from '@/components/his/PatientSelect.vue'
import {
  deleteAllergy,
  deleteFamilyHistory,
  deleteMedicationHistory,
  deletePastDisease,
  deletePatientContact,
  deleteSurgeryHistory,
  getPatientHealthProfile,
} from '@/api/patient'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {patientAgeText, patientGenderText} from '@/lib/patientGender'
import {
  ALIVE_OPTIONS,
  ALLERGY_SEVERITY_OPTIONS,
  ALLERGY_TYPE_OPTIONS,
  DISEASE_STATUS_OPTIONS,
  DRUG_ROUTE_OPTIONS,
  DRUG_TYPE_OPTIONS,
  GROUP_FIELDS,
  HEALTH_GROUPS,
  MEDICATION_STATUS_OPTIONS,
  medicationStatusTone,
  RECOVERY_STATUS_OPTIONS,
  severityTone,
  SURGERY_TYPE_OPTIONS,
} from '@/lib/healthProfile'
import HealthRecordEdit from './HealthRecordEdit.vue'

/** 组的 key（后端 PatientHealthProfileVO 的字段名） */
const PROFILE_FIELD = {
  allergy: 'allergies',
  pastDisease: 'pastDiseases',
  surgery: 'surgeryHistories',
  family: 'familyHistories',
  medication: 'medications',
  contact: 'contacts',
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

// 对话框开关与入参（表单、保存逻辑已拆至 HealthRecordEdit.vue）
const dialogVisible = ref(false)
const dialogGroup = ref('allergy')
const dialogRow = ref(null)
const dialogPreset = ref({})

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
  dialogVisible.value = false
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

/* ==================== 对话框（表单与保存已拆至 HealthRecordEdit.vue，这里只管开关与入参） ==================== */
const fieldsOf = (group) => GROUP_FIELDS[group] || []

/** 下拉/开关的选项值：字符串数组直接当 label=value，对象数组取 value */
const optValue = (o) => (o !== null && typeof o === 'object' ? o.value : o)
const optLabel = (o) => (o !== null && typeof o === 'object' ? o.label : o)

const openAdd = (group, preset = {}) => {
  dialogGroup.value = group
  dialogRow.value = null
  dialogPreset.value = preset
  dialogVisible.value = true
}

const openEdit = (group, row) => {
  dialogGroup.value = group
  dialogRow.value = {...row}
  dialogPreset.value = {}
  dialogVisible.value = true
}

/** 「文本转明细」：把主档那段自由文本填进新增对话框，让用户补齐结构化字段后保存 */
const convertLegacyText = (group) => {
  if (group === 'allergy') {
    openAdd(group, {allergenName: legacyTextOf(group)})
  } else if (group === 'pastDisease') {
    openAdd(group, {diseaseName: legacyTextOf(group)})
  } else if (group === 'contact') {
    const p = profile.value || {}
    openAdd(group, {contactName: p.contactNameText || '', phone: p.contactPhoneText || ''})
  }
}

const removeRow = async (group, row) => {
  const firstKey = GROUP_FIELDS[group]?.[0]?.key
  const name = row[firstKey] || row.contactName || '这条记录'
  try {
    await ElMessageBox.confirm(
        `确认删除「${name}」？该组主档上的摘要文本会随之重算。`,
        '删除确认', {type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'})
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
