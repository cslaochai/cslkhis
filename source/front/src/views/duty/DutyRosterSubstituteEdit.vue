<template>
  <el-dialog v-model="visible" title="临时换班" width="520px">
    <el-form ref="subFormRef" :model="subForm" :rules="subRules" label-width="120px">
      <el-form-item label="换班后值班人" prop="substituteEmpId">
        <div data-testid="duty-sub-emp">
          <el-select
              v-model="subForm.substituteEmpId"
              :loading="empLoading"
              :remote-method="searchEmployee"
              filterable
              placeholder="输入姓名搜索"
              remote
              reserve-keyword
              style="width: 100%"
          >
            <el-option
                v-for="e in empOptions"
                :key="e.id"
                :label="e.deptName ? `${e.empName}（${e.deptName}）` : e.empName"
                :value="e.id"
            />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="换班后电话">
        <el-input v-model="subForm.phone" placeholder="留空取新员工档案手机"/>
      </el-form-item>
      <el-form-item label="换班原因" prop="substituteReason">
        <div data-testid="duty-sub-reason">
          <el-input v-model="subForm.substituteReason" :rows="2" placeholder="必填：如主班参加抢救无法脱身"
                    type="textarea"/>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="handleSubstitute">确定换班</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {dutySubstitute} from '@/api/dutyRoster'
import {useEmployeeSearch} from '@/lib/dutyRoster'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// 换班
const subFormRef = ref(null)
const submitLoading = ref(false)
const subForm = reactive({id: null, substituteEmpId: null, phone: '', substituteReason: ''})
const subRules = {
  substituteEmpId: [{required: true, message: '请选择换班后值班人', trigger: 'change'}],
  substituteReason: [{required: true, message: '请填写换班原因', trigger: 'blur'}],
}

// 员工下拉（远程搜索：全院员工上千人，一次性拉全量会卡死下拉）
const {empOptions, empLoading, searchEmployee} = useEmployeeSearch()

watch(visible, async (open) => {
  if (!open) return
  const row = props.row
  subForm.id = row ? row.id : null
  subForm.substituteEmpId = null
  subForm.phone = ''
  subForm.substituteReason = ''
  subFormRef.value?.clearValidate()
  if (!empOptions.value.length) await searchEmployee('')
  if (row?.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [
      {id: row.employeeId, empName: row.employeeName, deptName: row.deptName, phone: row.phone},
      ...empOptions.value,
    ]
  }
})

const handleSubstitute = async () => {
  if (!subFormRef.value) return
  try {
    await subFormRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await dutySubstitute({...subForm})
    if (res.code === 200) {
      ElMessage.success(res.message || '换班成功')
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.message || '换班失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '换班失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
