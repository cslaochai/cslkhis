<template>
  <el-dialog
      v-model="visible"
      :title="(isEdit ? '编辑' : '新增') + (HEALTH_GROUPS.find(g => g.key === group) || {}).label"
      destroy-on-close
      width="720px"
  >
    <el-form :model="form" class="pr-2" label-width="110px">
      <el-form-item
          v-for="f in fieldsOf(group)"
          :key="f.key"
          :label="f.label"
          :required="!!f.required"
      >
        <el-select
            v-if="f.type === 'select'"
            v-model="form[f.key]"
            :placeholder="'请选择' + f.label"
            class="w-full"
            clearable
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
            v-model="form[f.key]"
            :placeholder="'请选择' + f.label"
            class="w-full"
            type="date"
            value-format="YYYY-MM-DD"
        />
        <el-input-number
            v-else-if="f.type === 'number'"
            v-model="form[f.key]"
            :min="0"
            class="w-full"
        />
        <el-switch
            v-else-if="f.type === 'switch'"
            v-model="form[f.key]"
            :active-text="optLabel(f.options[0])"
            :active-value="optValue(f.options[0])"
            :inactive-text="optLabel(f.options[1])"
            :inactive-value="optValue(f.options[1])"
        />
        <el-input
            v-else
            v-model="form[f.key]"
            :placeholder="f.placeholder || ('请输入' + f.label)"
            clearable
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['patient:profile:add', 'patient:profile:edit']" :loading="saving" type="primary"
                 @click="submitDialog">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 健康档案「新增/编辑」对话框（自 HealthRecordView 拆出，六组共用一个组件）
 *
 * 字段由 lib/healthProfile 的 GROUP_FIELDS 配置驱动（与页面表格列同一来源，禁写第二套副本）；
 * row 为空=新增（preset 支持「文本转明细」预填），row 有值=编辑；
 * relationOptions（sys_patient_relation 字典）由页面加载后传入（联系人列渲染同一份）。
 * 保存成功后 emit('saved')，由页面重拉整份档案（主档投影文本由服务端重算）。
 */
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {
  saveAllergy,
  saveFamilyHistory,
  saveMedicationHistory,
  savePastDisease,
  savePatientContact,
  saveSurgeryHistory,
} from '@/api/patient'
import {GROUP_FIELDS, HEALTH_GROUPS} from '@/lib/healthProfile'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 组 key（allergy / pastDisease / surgery / family / medication / contact）
  group: {type: String, default: 'allergy'},
  // 编辑行快照（null=新增）
  row: {type: Object, default: null},
  // 所属患者（雪花 ID，务必传字符串，别转 Number —— 会丢精度）
  patientId: {type: String, default: ''},
  // 新增时的预填（「文本转明细」把主档自由文本填进对应字段）
  preset: {type: Object, default: () => ({})},
  // 与患者关系字典选项（页面已加载）
  relationOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const SAVE_API = {
  allergy: saveAllergy, pastDisease: savePastDisease, surgery: saveSurgeryHistory,
  family: saveFamilyHistory, medication: saveMedicationHistory, contact: savePatientContact,
}

const form = ref({})
const saving = ref(false)
const isEdit = computed(() => form.value && form.value.id != null)

const fieldsOf = (group) => GROUP_FIELDS[group] || []

const optionsOf = (f) => {
  if (f.options === 'PATIENT_RELATION') return props.relationOptions
  return f.options || []
}

/** 下拉/开关的选项值：字符串数组直接当 label=value，对象数组取 value */
const optValue = (o) => (o !== null && typeof o === 'object' ? o.value : o)
const optLabel = (o) => (o !== null && typeof o === 'object' ? o.label : o)

// 每次打开按 row/preset 初始化表单（与拆分前 openAdd / openEdit 口径一致）
watch(() => props.modelValue, (v) => {
  if (!v) return
  form.value = props.row
      ? {...props.row, patientId: props.patientId}
      : {patientId: props.patientId, ...props.preset}
})

const submitDialog = async () => {
  const group = props.group
  // 必填校验放在提交前（后端也有校验，但等一个网络往返再报错体验差）
  for (const f of fieldsOf(group)) {
    if (f.required && (form.value[f.key] === undefined || form.value[f.key] === null || form.value[f.key] === '')) {
      ElMessage.warning(`请填写「${f.label}」`)
      return
    }
  }
  saving.value = true
  try {
    const res = await SAVE_API[group]({...form.value})
    if (res?.code !== 200) {
      ElMessage.error(res?.message || '保存失败')
      return
    }
    ElMessage.success(isEdit.value ? '已保存' : '已新增')
    visible.value = false
    emit('saved')
  } catch (e) {
    // request.js 已统一弹错，这里只需阻止把「已保存」也弹出来
    console.error('保存健康档案失败', e)
  } finally {
    saving.value = false
  }
}
</script>
