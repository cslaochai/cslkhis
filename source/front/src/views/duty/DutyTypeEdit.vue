<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="560px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <el-form-item label="类型编码" prop="typeCode">
        <el-input v-model="formData.typeCode" maxlength="32" placeholder="大写字母/下划线，如 MEDICAL"/>
      </el-form-item>
      <el-form-item label="类型名称" prop="typeName">
        <el-input v-model="formData.typeName" maxlength="64" placeholder="如 医疗总值班"/>
      </el-form-item>
      <el-form-item label="归口科室" prop="manageDeptId">
        <DeptTreeSelect v-model="formData.manageDeptId" mode="LEAF" width="100%" placeholder="这条线由哪个部门排班"/>

        <div class="mt-1 text-xs text-gray-500">只有该科室岗位的人能排这条线；其他部门切到本岗位才能排</div>
      </el-form-item>
      <el-form-item label="应到岗位类别">
        <el-select v-model="formData.requiredStaffType" clearable placeholder="不限" style="width: 100%">
          <el-option v-for="s in STAFF_TYPES" :key="s.value" :label="s.label" :value="s.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="资质要求（职务）">
        <el-select v-model="positionCodes" multiple collapse-tags collapse-tags-tooltip filterable
                   placeholder="不限（留空＝任何人都能排）" style="width: 100%">
          <el-option v-for="p in positionOptions" :key="p.value" :label="p.label" :value="p.value"/>
        </el-select>
        <div class="mt-1 text-xs text-gray-500">
          制度口径：医疗总值要求科室正/副主任，护理总值要求护士长 —— 排人时后端按员工档案职务校验
        </div>
      </el-form-item>
      <el-form-item label="院领导带班层">
        <el-radio-group v-model="formData.leaderLayer">
          <el-radio :value="1">需要（这条线要排院领导二线）</el-radio>
          <el-radio :value="0">不需要</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="formData.sortNo" :max="999" :min="1" controls-position="right"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" maxlength="200" placeholder="如：临床、医技科室正/副主任担任"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
// 值班类型登记/修改（row 为空走新增）
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {dutyTypeUpsert} from '@/api/dutyType'
import {getDictDataList} from '@/api/system'
import DeptTreeSelect from '@/components/his/DeptTreeSelect.vue'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// 岗位类别与后端 StaffTypeEnum 逐字对齐（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
const STAFF_TYPES = [
  {value: 1, label: '医生'},
  {value: 2, label: '护理'},
  {value: 3, label: '医技'},
  {value: 4, label: '药学'},
  {value: 5, label: '收费'},
  {value: 6, label: '行政其他'},
]

const submitLoading = ref(false)
const dialogTitle = ref('新增值班类型')
const formRef = ref(null)
const positionOptions = ref([])
// 资质职务是逗号分隔的码值串，表单里用数组承接（多选）
const positionCodes = ref([])

const formData = reactive({
  id: null,
  typeCode: '',
  typeName: '',
  manageDeptId: null,
  requiredStaffType: null,
  leaderLayer: 0,
  sortNo: 1,
  status: 1,
  remark: '',
})

const rules = {
  typeCode: [{required: true, message: '请填写类型编码', trigger: 'blur'}],
  typeName: [{required: true, message: '请填写类型名称', trigger: 'blur'}],
  manageDeptId: [{required: true, message: '请选择归口科室', trigger: 'change'}],
}

onMounted(async () => {
  try {
    const dictRes = await getDictDataList(['hospital_position'])
    positionOptions.value = (dictRes?.data || []).map((d) => ({
      value: String(d.dictValue),
      label: d.dictLabel,
    }))
  } catch {
    positionOptions.value = []
  }
})

watch(visible, (open) => {
  if (!open) return
  const row = props.row
  dialogTitle.value = row ? '修改值班类型' : '新增值班类型'
  formData.id = row?.id || null
  formData.typeCode = row?.typeCode || ''
  formData.typeName = row?.typeName || ''
  formData.manageDeptId = row?.manageDeptId || null
  formData.requiredStaffType = row?.requiredStaffType ?? null
  formData.leaderLayer = row?.leaderLayer ?? 0
  formData.sortNo = row?.sortNo ?? 1
  formData.status = row?.status ?? 1
  formData.remark = row?.remark || ''
  positionCodes.value = row?.requiredPositions
      ? String(row.requiredPositions).split(',').map((x) => x.trim()).filter(Boolean)
      : []
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
    const res = await dutyTypeUpsert({
      ...formData,
      requiredPositions: positionCodes.value.length ? positionCodes.value.join(',') : null,
    })
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    }
  } catch (e) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    submitLoading.value = false
  }
}
</script>
