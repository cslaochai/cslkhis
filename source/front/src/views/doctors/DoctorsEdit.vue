<template>
  <el-dialog
      v-model="visible"
      destroy-on-close
      title="编辑医生"
      width="500px"
  >
    <el-form v-loading="editLoading" :model="editForm" label-width="100px">
      <el-form-item label="医生姓名" required>
        <el-input v-model="editForm.empName" placeholder="请输入医生姓名"/>
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="editForm.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="editForm.phone" placeholder="请输入联系电话"/>
      </el-form-item>
      <el-form-item label="主科室">
        <span class="text-sm font-medium text-slate-700">{{ editForm.deptName || '-' }}</span>
        <p class="mt-1 text-xs text-slate-400">执业科室与角色请在员工档案的「岗位」中配置</p>
      </el-form-item>
      <el-form-item label="职称">
        <el-select v-model="editForm.title" class="w-full" clearable placeholder="请选择职称">
          <el-option
              v-for="item in titleOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="职位">
        <el-select v-model="editForm.position" class="w-full" clearable placeholder="请选择职位">
          <el-option
              v-for="item in positionOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="专家号">
        <el-switch v-model="editForm.isExpert" active-value="1" inactive-value="0"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getEmployeeDetail, updateEmployee} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 列表行：只提供 id，表单内容打开后按详情接口回填
  row: {type: Object, default: null},
  // 职称/职位字典由页面一次性加载（列表列也在用），这里只读取
  titleOptions: {type: Array, default: () => []},
  positionOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const editLoading = ref(false)
const editForm = ref({
  id: null,
  empName: '',
  gender: 1,
  phone: '',
  deptName: '',
  title: '',
  position: '',
  isExpert: '0',
})

// 打开时自己拉详情回填，父页面只管开关
watch(visible, async (open) => {
  if (!open || !props.row) return
  editLoading.value = true
  try {
    const res = await getEmployeeDetail(props.row.id)
    if (res.code === 200 && res.data) {
      const data = res.data
      editForm.value = {
        id: data.id,
        empName: data.empName || '',
        gender: data.gender || 1,
        phone: data.phone || '',
        deptName: data.deptName || '',
        title: data.title || '',
        position: data.position || '',
        isExpert: data.isExpert || '0',
      }
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    editLoading.value = false
  }
})

// 提交编辑
const handleEditSubmit = async () => {
  if (!editForm.value.empName) {
    ElMessage.warning('请输入医生姓名')
    return
  }
  try {
    const res = await updateEmployee(editForm.value)
    if (res.code === 200) {
      ElMessage.success('编辑成功')
      visible.value = false
      emit('saved')
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}
</script>
