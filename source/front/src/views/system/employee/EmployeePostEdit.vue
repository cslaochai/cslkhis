<template>
  <!-- 岗位调配：只维护该员工的岗位（角色 × 科室），档案其他字段不动 -->
  <el-dialog
      v-model="visible"
      title="岗位调配"
      width="860px"
      :destroy-on-close="true"
      @close="visible = false"
  >
    <div class="mb-2 text-xs text-gray-400">
      一行岗位 = 以某角色在某科室执业；主岗位一人一条，登录默认落点、号源与名册都读它。
    </div>
    <EmployeePostTable v-model="postRows" :dept-list="deptList" :role-list="roleList"/>
    <template #footer>
      <el-button v-perm="'org:employee:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import EmployeePostTable from '@/components/his/EmployeePostTable.vue'
import {checkPosts, postsFromApi, postsToPayload} from '@/lib/employeePost'
import {getEmployeeDetail, getRoleList, saveEmployeePosts} from '@/api/system'
import {loadDeptTreeOptions} from '@/lib/deptOptions'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 操作列传入当前行；需带 id
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const deptList = ref([])
const roleList = ref([])
const postRows = ref([])
const submitLoading = ref(false)

const loadRefs = async () => {
  try {
    const [tree, roleRes] = await Promise.all([loadDeptTreeOptions({scope: 'ALL', mode: 'ORG'}), getRoleList({})])
    deptList.value = tree
    if (roleRes.code === 200) roleList.value = roleRes.data || []
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
}

watch(visible, async (open) => {
  if (!open) return
  postRows.value = []
  await loadRefs()
  if (!props.row?.id) return
  try {
    const res = await getEmployeeDetail(props.row.id)
    if (res.code === 200 && res.data) {
      postRows.value = postsFromApi(res.data.posts)
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
})

// 校验报错写成名字（科室是树，需递归；deptId 两侧类型不一律字符串比较）
const findDeptName = (list, id) => {
  for (const dept of list || []) {
    if (String(dept.id) === String(id)) return dept.deptName
    if (dept.children && dept.children.length > 0) {
      const hit = findDeptName(dept.children, id)
      if (hit) return hit
    }
  }
  return null
}
const deptNameOf = (deptId) => findDeptName(deptList.value, deptId) || `科室(${deptId})`
const roleNameOf = (roleCode) => {
  const hit = roleList.value.find(r => r.roleCode === roleCode)
  return hit ? hit.roleName : roleCode
}

const handleSubmit = async () => {
  const postError = checkPosts(postRows.value, {roleNameOf, deptNameOf})
  if (postError) {
    ElMessage.warning(postError)
    return
  }
  submitLoading.value = true
  try {
    const res = await saveEmployeePosts({id: props.row.id, posts: postsToPayload(postRows.value)})
    if (res.code === 200) {
      ElMessage.success('岗位保存成功')
      visible.value = false
      emit('saved')
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    submitLoading.value = false
  }
}
</script>
