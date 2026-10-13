<template>
  <!-- 新增与修改共用一张表单（后端同一个 upsert，row 为空走新增） -->
  <el-dialog
      v-model="visible"
      :title="formData.id ? '编辑角色' : '新增角色'"
      width="500px"
  >
    <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
    >
      <el-form-item label="角色编码" prop="roleCode">
        <el-input
            v-model="formData.roleCode"
            disabled
            placeholder="新增时系统自动生成"
        />
      </el-form-item>
      <el-form-item label="角色名称" prop="roleName">
        <el-input
            v-model="formData.roleName"
            placeholder="请输入角色名称"
        />
      </el-form-item>
      <el-form-item label="角色类型" prop="roleType">
        <el-input :value="formData.roleType === 1 ? '系统角色' : '自定义角色'" disabled/>
      </el-form-item>
      <el-form-item label="角色描述" prop="remark">
        <el-input
            v-model="formData.remark"
            placeholder="请输入角色描述"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="数据范围" prop="dataScope">
        <el-select v-model="formData.dataScope" class="w-full">
          <el-option :value="1" label="全部数据（该角色看全院，不按科室收口）"/>
          <el-option :value="2" label="按岗位科室（只能看该角色下被分配的科室）"/>
        </el-select>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="formData.sortOrder" :max="999" :min="0"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'system:role:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {createRole, getRoleDetail, updateRole} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
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
  roleCode: '',
  roleName: '',
  remark: '',
  roleType: 0,
  sortOrder: 0,
  status: 1,
  // 数据范围：1-全部数据（全院不收口）；其余值一律「按岗位科室」收口。
  // 后端判定点只认 ==1（DeptScopeProviderImpl.isUnrestrictedRole），库里 NULL 与非 1 行为相同，
  // 前端统一归一成 2 提交，避免 updateById 跳过 null 字段导致改不回「按岗位」。
  dataScope: 2,
})

const rules = {
  roleName: [
    {required: true, message: '请输入角色名称', trigger: 'blur'},
  ],
}

const resetForm = () => {
  formData.id = null
  formData.roleCode = ''
  formData.roleName = ''
  formData.remark = ''
  formData.roleType = 0  // 新增时固定为自定义角色
  formData.sortOrder = 0
  formData.status = 1
  formData.dataScope = 2  // 新增默认按岗位科室收口，放开全院要显式选
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row) return
  try {
    const res = await getRoleDetail(props.row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      // 存量角色 data_scope 可能是 NULL：归一成 2（按岗位科室），下拉才有值、保存才不会被跳过
      if (formData.dataScope !== 1) formData.dataScope = 2
    } else {
      ElMessage.error(res.message || '获取角色信息失败')
      visible.value = false
    }
  } catch (error) {
    
    visible.value = false
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
      res = await updateRole(formData)
    } else {
      res = await createRole(formData)
    }

    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
