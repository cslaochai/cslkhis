<template>
  <!-- 发起营养会诊 -->
  <el-dialog v-model="visible" data-testid="consult-dialog" title="发起营养会诊" width="560px">
    <el-form label-width="120px">
      <el-form-item label="患者">
        <el-input :value="`${consultRow.patientName || ''}（${consultRow.admissionNo || ''}）`" disabled/>
      </el-form-item>
      <el-form-item label="会诊科室" required>
        <div data-testid="sel-consult-dept" style="width: 100%">
          <el-select v-model="consultForm.toDeptId" filterable placeholder="选择营养科" style="width: 100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="会诊范围" required>
        <el-radio-group v-model="consultForm.consultType">
          <el-radio :value="2">科间会诊</el-radio>
          <el-radio :value="3">全院会诊</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否急会诊">
        <el-radio-group v-model="consultForm.isUrgent">
          <el-radio :value="0">普通（24 小时内应答）</el-radio>
          <el-radio :value="1">急会诊（10 分钟内应答）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="会诊理由" required>
        <el-input v-model="consultForm.reason" :rows="3" data-testid="input-consult-reason" maxlength="500"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="savingConsult" data-testid="btn-do-consult" type="primary" @click="submitConsult">
        提交申请
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDepartmentSelectList } from '@/api/system'
import { applyNutritionConsult } from '@/api/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 达风险阈值的筛查行（阈值预检在页面完成，能打开即视为可会诊） */
  row: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// 科室下拉只在本弹框使用，弹框打开时现取
const depts = ref([])

async function loadDepts() {
  try {
    const res = await getDepartmentSelectList({})
    depts.value = res?.data || []
  } catch (e) {
    depts.value = []
  }
}

const savingConsult = ref(false)
const consultRow = ref({})
const consultForm = reactive({toDeptId: null, consultType: 2, isUrgent: 0, reason: ''})

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    loadDepts()
    const row = props.row || {}
    consultRow.value = row
    Object.assign(consultForm, {
      toDeptId: row.deptId || null,
      consultType: 2,
      isUrgent: 0,
      reason: `NRS2002 总分 ${row.totalScore} 分判为有营养风险（${row.screenTypeText || ''}），请营养科会诊制定膳食方案。`,
    })
  },
)

async function submitConsult() {
  if (!consultForm.toDeptId) {
    ElMessage.warning('请选择会诊科室');
    return
  }
  if (!consultForm.reason?.trim()) {
    ElMessage.warning('会诊理由不能为空');
    return
  }
  savingConsult.value = true
  try {
    const res = await applyNutritionConsult({
      admissionId: consultRow.value.admissionId,
      toDeptId: consultForm.toDeptId,
      consultType: consultForm.consultType,
      isUrgent: consultForm.isUrgent,
      reason: consultForm.reason,
    })
    ElMessage.success(`营养会诊已申请（${res?.data || ''}）`)
    visible.value = false
  } catch (e) {
    ElMessage.error(e?.message || '申请失败')
  } finally {
    savingConsult.value = false
  }
}
</script>
