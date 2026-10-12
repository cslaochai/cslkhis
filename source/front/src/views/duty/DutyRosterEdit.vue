<template>
  <!-- 登记/修改 -->
  <el-dialog v-model="visible" :title="dialogTitle" width="560px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="值班日期" prop="dutyDate">
        <div data-testid="duty-date">
          <el-date-picker
              v-model="formData.dutyDate"
              placeholder="夜班请填开始日"
              style="width: 100%"
              type="date"
              value-format="YYYY-MM-DD"
          />
        </div>
      </el-form-item>
      <el-form-item :label="dutyKind === 'clinical' ? '值班点位' : '点位（留空＝总值班位）'" prop="postId">
        <div data-testid="duty-post">
          <el-select
              v-model="formData.postId"
              clearable
              filterable
              placeholder="选择这个班落在哪个位上"
              style="width: 100%"
              @change="onPostChange"
          >
            <el-option
                v-for="p in postOptions"
                :key="p.id"
                :label="p.dutyLevelText && p.dutyLevelText !== '不适用'
                ? `${p.postName}（${p.dutyLevelText}·${p.attendModeText}）` : p.postName"
                :value="p.id"
            />
          </el-select>
        </div>
        <div v-if="currentPost()" class="mt-1 text-xs text-gray-500">
          班次与角色由点位带出：{{ currentPost().shiftName }} {{ currentPost().startTime }}~{{ currentPost().endTime }}
        </div>
      </el-form-item>
      <el-form-item label="班次" prop="shiftType">
        <el-radio-group v-model="formData.shiftType" :disabled="!!formData.postId">
          <el-radio :value="1">白班 08:00~18:00</el-radio>
          <el-radio :value="2">夜班 18:00~次日 08:00</el-radio>
        </el-radio-group>
        <div v-if="formData.postId" class="text-xs text-gray-400">已选点位，班次按点位走</div>
      </el-form-item>
      <el-form-item label="班内角色" prop="roleType">
        <el-radio-group v-model="formData.roleType" :disabled="!!formData.postId">
          <el-radio :value="1">主班</el-radio>
          <el-radio :value="2">副班（备班）</el-radio>
        </el-radio-group>
        <div v-if="formData.postId" class="text-xs text-gray-400">已选点位，角色按点位走</div>
      </el-form-item>
      <el-form-item label="值班人" prop="employeeId">
        <div data-testid="duty-emp">
          <el-select
              v-model="formData.employeeId"
              :loading="empLoading"
              :remote-method="searchEmployee"
              filterable
              placeholder="输入姓名搜索（只显示在职工）"
              remote
              reserve-keyword
              style="width: 100%"
              @change="onEmpChange"
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
      <el-form-item label="值班电话">
        <el-input v-model="formData.phone" placeholder="留空取员工档案手机"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">有效</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="如：本周总值，兼管急诊协调" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
// 总值班登记/修改（row 为空走登记；原内联于 DutyRosterView，按作业规范拆出，接口与字段未动）
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {dutyRosterUpsert} from '@/api/dutyRoster'
import {getDutyPostSelectList} from '@/api/dutyPost'
import {dutyKindScope, useEmployeeSearch} from '@/lib/dutyRoster'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 要修改的排班行；为空走登记
  row: {type: Object, default: null},
  // 值班种类（admin 全院总值班 / clinical 科室医师值班）：决定标题、点位下拉口径与校验
  dutyKind: {type: String, default: 'admin'},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const dialogTitle = ref('登记总值班')
const formRef = ref(null)
const formData = reactive({
  id: null,
  postId: null,
  dutyDate: null,
  shiftType: 1,
  roleType: 1,
  employeeId: null,
  phone: '',
  startTime: '',
  endTime: '',
  status: 1,
  remark: '',
})
const rules = {
  dutyDate: [{required: true, message: '请选择值班日期', trigger: 'change'}],
  shiftType: [{required: true, message: '请选择班次', trigger: 'change'}],
  roleType: [{required: true, message: '请选择班内角色', trigger: 'change'}],
  employeeId: [{required: true, message: '请选择值班人', trigger: 'change'}],
  // 科室医师值班必须挑位：不传 postId 后端就按老语义落行政总值班位，
  // 排班员以为排的是本科室一线，实际把全院总值班那行改了。
  postId: [{
    validator: (_r, v, cb) => {
      if (props.dutyKind === 'clinical' && !v) return cb(new Error('科室医师值班必须选择值班点位'))
      cb()
    },
    trigger: 'change',
  }],
}

// 点位下拉：按值班种类过滤（总值班只有 4 个行政位，临床有 234 个科室位）
const postOptions = ref([])
// 已按哪种值班种类拉过点位下拉；种类一换下次打开必须重拉
const loadedScope = ref(null)

const loadPostOptions = async () => {
  try {
    const res = await getDutyPostSelectList({dutyScope: dutyKindScope(props.dutyKind)})
    postOptions.value = res?.data || []
  } catch {
    postOptions.value = []
  }
}

const {empOptions, empLoading, searchEmployee} = useEmployeeSearch()

// 选了点位：班次与班内角色都由点位带出（后端以点位为权威），页面上这两个单选就锁住，
// 免得排班员手选一个跟点位打架的值还以为生效了。
const onPostChange = (postId) => {
  const p = postOptions.value.find((x) => String(x.id) === String(postId))
  if (!p) return
  if (p.roleType) formData.roleType = p.roleType
  formData.shiftType = p.shiftName && p.shiftName.includes('夜') ? 2 : 1
  if (!formData.phone && p.phone) formData.phone = p.phone
}
const currentPost = () => postOptions.value.find((x) => String(x.id) === String(formData.postId))

const onEmpChange = (id) => {
  const emp = empOptions.value.find((e) => e.id === id)
  // 联系电话留空时后端回落员工档案手机，这里只做提示用，不强制回填
  if (emp && !formData.phone) formData.phone = emp.phone || ''
}

const resetForm = () => {
  formData.id = null
  formData.postId = null
  formData.dutyDate = null
  formData.shiftType = 1
  formData.roleType = 1
  formData.employeeId = null
  formData.phone = ''
  formData.startTime = ''
  formData.endTime = ''
  formData.status = 1
  formData.remark = ''
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  const row = props.row
  dialogTitle.value = props.dutyKind === 'clinical'
      ? (row ? '修改科室医师值班' : '登记科室医师值班')
      : (row ? '修改总值班' : '登记总值班')
  if (row) {
    formData.id = row.id
    formData.postId = row.postId || null
    formData.dutyDate = row.dutyDate
    formData.shiftType = row.shiftType
    formData.roleType = row.roleType
    formData.employeeId = row.employeeId
    formData.phone = row.phone || ''
    formData.startTime = row.startTime || ''
    formData.endTime = row.endTime || ''
    formData.status = row.status ?? 1
    formData.remark = row.remark || ''
  }
  const scope = dutyKindScope(props.dutyKind)
  if (loadedScope.value !== scope) {
    await loadPostOptions()
    loadedScope.value = scope
  }
  if (!empOptions.value.length) await searchEmployee('')
  // 点位可能不在当前种类下拉里（从另一类切过来的老数据），补进去保证回显是点位名而不是ID
  if (row?.postId && !postOptions.value.some((p) => String(p.id) === String(row.postId))) {
    postOptions.value = [
      {id: row.postId, postName: row.postName, roleType: row.roleType, shiftName: row.shiftTypeText},
      ...postOptions.value,
    ]
  }
  // 值班人可能不在当前下拉候选里（搜索过别的字），补进去保证回显是姓名而不是ID
  if (row?.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [
      {id: row.employeeId, empName: row.employeeName, deptName: row.deptName, phone: row.phone},
      ...empOptions.value,
    ]
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await dutyRosterUpsert({...formData})
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '登记成功')
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
