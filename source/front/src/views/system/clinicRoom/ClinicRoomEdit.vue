<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，row 为空走新增） -->
  <el-dialog v-model="visible" :title="formData.id ? '编辑诊室' : '新增诊室'" width="600px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="诊室编号" prop="code">
        <el-input v-model="formData.code" placeholder="请输入诊室编号"/>
      </el-form-item>
      <el-form-item label="诊室名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入诊室名称"/>
      </el-form-item>
      <el-form-item label="地理位置" prop="location">
        <el-input v-model="formData.location" placeholder="如：门诊楼A座3层"/>
      </el-form-item>
      <el-form-item label="所属科室" prop="deptId">
        <el-tree-select
            v-model="formData.deptId"
            :data="deptList"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择科室"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" :rows="3" placeholder="如：靠窗、配备B超机等" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'org:clinicRoom:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {createClinicRoom, getClinicRoomDetail, updateClinicRoom} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 科室树：查询卡与表格列也要用，由页面加载一次下传
  deptList: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  code: '',
  location: '',
  deptId: null,
  status: 1,
  remark: '',
})

const rules = {
  name: [
    {required: true, message: '请输入诊室名称', trigger: 'blur'},
  ],
  code: [
    {required: true, message: '请输入诊室编号', trigger: 'blur'},
  ],
  location: [
    {required: true, message: '请输入地理位置', trigger: 'blur'},
  ],
  deptId: [
    {required: true, message: '请选择所属科室', trigger: 'change'},
  ],
}

const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.code = ''
  formData.location = ''
  formData.deptId = null
  formData.status = 1
  formData.remark = ''
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row) return
  try {
    const res = await getClinicRoomDetail(props.row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取诊室信息失败')
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
    let res
    if (formData.id) {
      res = await updateClinicRoom(formData)
    } else {
      res = await createClinicRoom(formData)
    }

    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
