<template>
  <el-dialog
      v-model="visible"
      title="群伤 / 批量登记"
      width="960px"
      append-to-body
      :close-on-click-modal="false"
      @open="onOpen">
    <div class="mb-3 flex flex-wrap items-center gap-3">
      <span class="text-sm text-slate-600">接诊科室</span>
      <el-select
          v-model="defaultDeptId"
          filterable
          placeholder="必选：群伤统一接诊科室"
          class="!w-56"
          @change="applyDefaultDept">
        <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
      </el-select>
      <span class="text-sm text-slate-600">默认分诊级别</span>
      <el-select v-model="defaultLevel" class="!w-40" @change="applyDefaultLevel">
        <el-option v-for="(v, k) in triageLevelMap" :key="k" :label="v.label" :value="Number(k)"/>
      </el-select>
      <el-button :icon="Plus" @click="addRow">增行</el-button>
      <span class="ml-auto text-sm" :class="resultTextClass">{{ resultText }}</span>
    </div>

    <el-table :data="rows" border size="small" max-height="420">
      <el-table-column type="index" label="#" width="46"/>
      <el-table-column label="姓名" width="120">
        <template #default="{row}">
          <el-input v-model="row.name" placeholder="必填" :class="row.touched && !row.name ? 'wb-err' : ''"/>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="96">
        <template #default="{row}">
          <el-select v-model="row.gender" class="w-full" placeholder="必选">
            <el-option v-for="o in genderOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="年龄" width="86">
        <template #default="{row}">
          <el-input v-model.number="row.age" type="number" placeholder="必填"
                    :class="row.touched && (row.age == null || row.age === '') ? 'wb-err' : ''"/>
        </template>
      </el-table-column>
      <el-table-column label="主诉" min-width="150">
        <template #default="{row}">
          <el-input v-model="row.chiefComplaint" placeholder="选填"/>
        </template>
      </el-table-column>
      <el-table-column label="分诊级别" width="116">
        <template #default="{row}">
          <el-select v-model="row.triageLevel" class="w-full" placeholder="默认">
            <el-option v-for="(v, k) in triageLevelMap" :key="k" :label="v.label" :value="Number(k)"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="绿色通道" width="124">
        <template #default="{row}">
          <el-select v-model="row.greenChannel" class="w-full">
            <el-option v-for="o in greenOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="登记结果" width="150">
        <template #default="{row}">
          <span v-if="row.status === 'success'" class="text-green-600 font-medium">✓ 已登记</span>
          <span v-else-if="row.status === 'fail'" class="text-red-600" :title="row.err">✗ {{ row.err }}</span>
          <span v-else class="text-slate-400">待登记</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="72">
        <template #default="{row, $index}">
          <el-button link type="danger" :icon="Delete" @click="removeRow($index)"/>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button
          type="primary"
          :icon="Check"
          :disabled="!canSubmit"
          :loading="submitting"
          @click="submitAll">
        登记全部（{{ pendingCount }}）
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref} from 'vue'
import {Check, Delete, Plus} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {registerEmergency} from '@/api/emergency'
import {getDepartmentSelectList} from '@/api/system'
import {PATIENT_GENDER_OPTIONS} from '@/lib/patientGender'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 分诊级别字典由主页面持有，子组件不许再拉一份
  triageLevelMap: {type: Object, default: () => ({})},
})
const emit = defineEmits(['update:modelValue', 'saved'])
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const genderOptions = PATIENT_GENDER_OPTIONS
const greenOptions = [
  {value: '胸痛中心', label: '胸痛中心'},
  {value: '卒中中心', label: '卒中中心'},
  {value: '创伤中心', label: '创伤中心'},
  {value: '无', label: '无'},
]

const departments = ref([])
const defaultDeptId = ref(null)
const defaultLevel = ref(3) // 群伤多为轻症，默认 III 级，行可覆盖
const submitting = ref(false)
const rows = ref([])

const blankRow = () => ({
  name: '', gender: null, age: null, chiefComplaint: '',
  triageLevel: null, greenChannel: '无',
  status: 'pending', err: '', touched: false,
})

const addRow = () => rows.value.push(blankRow())
const removeRow = (i) => {
  if (rows.value.length <= 1) return
  rows.value.splice(i, 1)
}
const applyDefaultDept = () => {
  /* 顶部科室仅作统一默认值提示，行级不强制覆盖（群伤同科室，登记时统一带 defaultDeptId） */
}
const applyDefaultLevel = () => {
  rows.value.forEach((r) => {
    if (r.triageLevel == null) r.triageLevel = defaultLevel.value
  })
}

const pendingCount = computed(() => rows.value.filter((r) => r.status !== 'success').length)
const canSubmit = computed(() => !!defaultDeptId.value && rows.value.length > 0)
const successCount = computed(() => rows.value.filter((r) => r.status === 'success').length)
const failCount = computed(() => rows.value.filter((r) => r.status === 'fail').length)
const resultText = computed(() => {
  if (submitting.value) return '登记中…'
  if (successCount.value === 0 && failCount.value === 0) return ''
  return `成功 ${successCount.value} / 共 ${rows.value.length}` + (failCount.value ? `，失败 ${failCount.value}（可重试）` : '')
})
const resultTextClass = computed(() => (failCount.value ? 'text-red-600' : 'text-green-600'))

const onOpen = async () => {
  rows.value = [blankRow(), blankRow(), blankRow(), blankRow(), blankRow()]
  defaultDeptId.value = null
  submitting.value = false
  if (departments.value.length === 0) {
    try {
      const res = await getDepartmentSelectList({deptType: 1})
      departments.value = res.data || []
    } catch (e) {
      console.error('加载科室失败:', e)
    }
  }
}

const submitAll = async () => {
  if (!defaultDeptId.value) {
    ElMessage.warning('请先选择接诊科室')
    return
  }
  // 前置校验：姓名 / 性别 / 年龄必须齐，缺的标红
  rows.value.forEach((r) => {
    r.touched = true
  })
  const invalid = rows.value.some(
      (r) => !r.name || r.gender == null || r.age == null || r.age === '',
  )
  if (invalid) {
    ElMessage.warning('存在必填项缺失（姓名 / 性别 / 年龄）')
    return
  }

  submitting.value = true
  let ok = 0
  let bad = 0
  for (const r of rows.value) {
    if (r.status === 'success') {
      ok++
      continue
    }
    const dto = {
      patientId: null, // 新患者模式：逐条建主档
      patientName: r.name,
      gender: Number(r.gender),
      age: Number(r.age),
      chiefComplaint: r.chiefComplaint || '',
      triageLevel: r.triageLevel != null ? Number(r.triageLevel) : defaultLevel.value,
      greenChannel: r.greenChannel || '无',
      deptId: defaultDeptId.value,
    }
    try {
      await registerEmergency(dto)
      r.status = 'success'
      r.err = ''
      ok++
    } catch (e) {
      r.status = 'fail'
      r.err = (e && e.message) || '登记失败'
      bad++
    }
  }
  submitting.value = false
  // 任一条成功即刷新看板（让患者进三区看板），失败行保留可重试
  if (ok > 0) emit('saved')
  if (bad === 0) {
    ElMessage.success(`批量登记完成，成功 ${ok} 人`)
  } else {
    ElMessage.warning(`成功 ${ok} 人，失败 ${bad} 人，可修正后再次点击登记全部`)
  }
}
</script>

<style scoped>
.wb-err :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dc2626 inset;
}
</style>
