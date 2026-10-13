<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="620px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <el-form-item label="点位编码" prop="postCode">
        <div data-testid="dp-code">
          <el-input v-model="formData.postCode" placeholder="如 DUTY-NIGHT-MAIN（全局唯一）"/>
        </div>
      </el-form-item>
      <el-form-item label="点位名称" prop="postName">
        <div data-testid="dp-name">
          <el-input v-model="formData.postName" placeholder="如 夜间总值班（主班）"/>
        </div>
      </el-form-item>
      <el-form-item label="责任范围" prop="dutyScope">
        <el-select v-model="formData.dutyScope" style="width: 100%">
          <el-option v-for="o in DUTY_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="所属单元类型">
        <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
          <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="orgIdRequired" label="所属科室">
        <el-select v-model="formData.orgId" filterable placeholder="选择科室" style="width: 100%">
          <el-option v-for="o in deptOptions" :key="o.id" :label="o.name" :value="o.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="班次" prop="shiftId">
        <div data-testid="dp-shift">
          <el-select v-model="formData.shiftId" placeholder="这一位几点到几点由班次带出" style="width: 100%">
            <el-option v-for="s in shiftOptions" :key="s.id" :label="s.shiftName" :value="s.id"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="班内角色" prop="roleType">
        <el-radio-group v-model="formData.roleType">
          <el-radio v-for="o in DUTY_ROLE_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应到岗位类别">
        <el-select v-model="formData.requiredStaffType" clearable placeholder="不限" style="width: 100%">
          <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="值班电话">
        <el-input v-model="formData.phone" placeholder="半夜打得通的那一部"/>
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="formData.sortNo" :max="999" :min="0"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="如：急诊床位调配的最终兜底人" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" data-testid="dp-submit" type="primary" @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
// 值守点位新增/修改（row 为空走新增；原内联于 DutyPostView，按作业规范拆出，接口与字段未动）
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {dutyPostUpsert} from '@/api/dutyPost'
import {getDepartmentSelectList} from '@/api/system'
import {getShiftSelectList} from '@/api/appoint'
import {
  DUTY_ROLE_TYPE_OPTIONS,
  DUTY_SCOPE_OPTIONS,
  ORG_UNIT_TYPE_HOSPITAL,
  ORG_UNIT_TYPE_OPTIONS,
} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'

const SHIFT_USE_SCOPE_DUTY = 3

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 要修改的点位行；为空走新增
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const dialogTitle = ref('新增点位')
const formRef = ref(null)
const formData = reactive({
  id: null, postCode: '', postName: '', dutyScope: 1, orgType: ORG_UNIT_TYPE_HOSPITAL, orgId: null,
  roleType: 1, shiftId: null, requiredStaffType: null, phone: '', sortNo: 0, status: 1, remark: '',
})
const rules = {
  postCode: [{required: true, message: '请填写点位编码', trigger: 'blur'}],
  postName: [{required: true, message: '请填写点位名称', trigger: 'blur'}],
  dutyScope: [{required: true, message: '请选择责任范围', trigger: 'change'}],
  roleType: [{required: true, message: '请选择班内角色', trigger: 'change'}],
  shiftId: [{required: true, message: '请选择班次', trigger: 'change'}],
}

const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)

const deptOptions = ref([])
// 值班班次册（use_scope=3）：班次决定这一位几点到几点，点位不能再自己填一遍时间
const shiftOptions = ref([])

const resetForm = () => {
  formData.id = null
  formData.postCode = ''
  formData.postName = ''
  formData.dutyScope = 1
  formData.orgType = ORG_UNIT_TYPE_HOSPITAL
  formData.orgId = null
  formData.roleType = 1
  formData.shiftId = null
  formData.requiredStaffType = null
  formData.phone = ''
  formData.sortNo = 0
  formData.status = 1
  formData.remark = ''
}

const loadOptions = async () => {
  try {
    const [dRes, sRes] = await Promise.all([
      getDepartmentSelectList({}),
      getShiftSelectList({status: 1, useScope: SHIFT_USE_SCOPE_DUTY}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id ?? d.deptId, name: d.deptName ?? d.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName}))
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  const row = props.row
  if (row) {
    dialogTitle.value = '修改点位'
    Object.assign(formData, {
      id: row.id,
      postCode: row.postCode,
      postName: row.postName,
      dutyScope: row.dutyScope,
      orgType: row.orgType,
      orgId: row.orgId || null,
      roleType: row.roleType,
      shiftId: row.shiftId,
      requiredStaffType: row.requiredStaffType || null,
      phone: row.phone || '',
      sortNo: row.sortNo ?? 0,
      status: row.status ?? 1,
      remark: row.remark || '',
    })
  } else {
    dialogTitle.value = '新增点位'
  }
  if (!shiftOptions.value.length) await loadOptions()
  // 挂的班次已被停用时会不在下拉里，补进去保证回显不是空的
  if (row?.shiftId && !shiftOptions.value.some((s) => String(s.id) === String(row.shiftId))) {
    shiftOptions.value = [{id: row.shiftId, shiftName: row.shiftName || '已停用班次'}, ...shiftOptions.value]
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
    const payload = {...formData, orgId: orgIdRequired.value ? formData.orgId : null}
    const res = await dutyPostUpsert(payload)
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
