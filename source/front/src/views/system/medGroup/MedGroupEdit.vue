<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，id 为空走新增） -->
  <el-dialog v-model="visible" :title="formData.id ? '编辑医疗组' : '新增医疗组'" width="620px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="所属科室" prop="deptId">
        <el-select v-model="formData.deptId" class="!w-full" data-testid="medgroup-dept" filterable
                   placeholder="请选择科室">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
        <div class="mt-1 text-[12px] text-slate-500">
          医疗组挂在科室下，不挂病区：医生归属固定在科室与医疗组，查房时才到病区。
        </div>
      </el-form-item>
      <el-form-item label="医疗组名称" prop="groupName">
        <el-input v-model="formData.groupName" data-testid="medgroup-name" placeholder="如：呼吸内科一组"/>
      </el-form-item>
      <el-form-item label="医疗组编码" prop="groupCode">
        <el-input v-model="formData.groupCode" placeholder="留空由系统生成 MG001 形式的编码"/>
      </el-form-item>
      <el-form-item label="主诊医师" prop="leaderId">
        <el-select v-model="formData.leaderId" class="!w-full" clearable filterable
                   placeholder="医疗组长 / 主诊医师">
          <el-option v-for="e in leaderOptions" :key="e.id" :label="e.empName" :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="排序号" prop="sortOrder">
        <el-input-number v-model="formData.sortOrder" :min="0" class="!w-40" controls-position="right"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" :rows="3" placeholder="如：主攻肺部感染方向" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'org:medGroup:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getEmployeeList, getMedGroupDetail, saveMedGroup} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
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
  groupName: '',
  groupCode: '',
  deptId: null,
  leaderId: null,
  status: 1,
  sortOrder: 0,
  remark: '',
})

const leaderOptions = ref([])

const rules = {
  deptId: [{required: true, message: '请选择所属科室', trigger: 'change'}],
  groupName: [{required: true, message: '请输入医疗组名称', trigger: 'blur'}],
}

const deptOptions = computed(() => {
  const out = []
  const walk = (list) => {
    for (const d of list || []) {
      out.push({id: d.id, deptName: d.deptName})
      if (d.children && d.children.length) walk(d.children)
    }
  }
  walk(props.deptList)
  return out
})

const resetForm = () => {
  formData.id = null
  formData.groupName = ''
  formData.groupCode = ''
  formData.deptId = null
  formData.leaderId = null
  formData.status = 1
  formData.sortOrder = 0
  formData.remark = ''
}

// 主诊医师候选：组内医师按科室筛，未选科室时取全院医生
const loadLeaders = async (deptId) => {
  leaderOptions.value = []
  try {
    const res = await getEmployeeList(deptId ? {deptId} : {})
    const list = res.data || []
    leaderOptions.value = list.map(e => ({id: e.id, empName: e.empName || e.id}))
  } catch (error) {
    console.error('加载医师候选失败', error)
  }
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row) {
    await loadLeaders(null)
    return
  }
  try {
    const res = await getMedGroupDetail(props.row.id)
    if (res.code === 200 && res.data) {
      Object.assign(formData, res.data)
    }
    await loadLeaders(formData.deptId)
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
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
    const res = await saveMedGroup({...formData})
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
