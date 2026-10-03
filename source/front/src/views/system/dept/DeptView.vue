<script setup lang="js">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh, Check, Search, ArrowDown } from '@element-plus/icons-vue'
import { getDepartmentTree, getDepartmentDetail, createDepartment, updateDepartment, deleteDepartment, getUserList } from '@/api/system'

const loading = ref(false)
const deptTree = ref([])
const expandedKeys = ref([])
const treeRef = ref(null)

// 搜索
const searchKeyword = ref('')
const highlightKeyword = ref('')

// 选中的部门
const selectedDept = ref(null)

// 用户列表（用于负责人选择）
const userList = ref([])
const userLoading = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  parentId: 0,
  deptName: '',
  deptCode: '',
  leaderId: null,
  leader: '',
  phone: '',
  email: '',
  sortOrder: 0,
  status: 1,
})

const formLoading = ref(false)

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentTree()
    if (res.code === 200) {
      deptTree.value = res.data || []
      // 默认展开第一级
      if (deptTree.value.length > 0) {
        expandedKeys.value = deptTree.value.map(item => item.id)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadUserList = async (keyword) => {
  userLoading.value = true
  try {
    const res = await getUserList({ userName: keyword, pageNum: 1, pageSize: 20 })
    if (res.code === 200) {
      userList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败', error)
  } finally {
    userLoading.value = false
  }
}

const handleUserSearch = (keyword) => {
  loadUserList(keyword)
}

const handleUserChange = (userId) => {
  const user = userList.value.find(u => u.id === userId)
  if (user) {
    formData.leader = user.realName || user.userName
  } else {
    formData.leader = ''
  }
}

// 搜索部门
const handleSearch = () => {
  highlightKeyword.value = searchKeyword.value

  // 展开匹配的节点
  if (searchKeyword.value) {
    const matchedKeys = []
    const findMatched = (list) => {
      list.forEach(item => {
        if (item.deptName.includes(searchKeyword.value) ||
            (item.deptCode && item.deptCode.includes(searchKeyword.value))) {
          matchedKeys.push(item.id)
        }
        if (item.children && item.children.length > 0) {
          findMatched(item.children)
        }
      })
    }
    findMatched(deptTree.value)
    expandedKeys.value = matchedKeys
  }
}

// 点击树节点
const handleNodeClick = async (data) => {
  selectedDept.value = data
  formLoading.value = true
  try {
    const res = await getDepartmentDetail(data.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      // 如果有负责人，加载用户列表以显示名称
      if (formData.leaderId) {
        await loadUserList('')
        const user = userList.value.find(u => u.id === formData.leaderId)
        if (user) {
          formData.leader = user.realName || user.userName
        }
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '获取部门信息失败')
  } finally {
    formLoading.value = false
  }
}

// 新增部门
const handleAdd = (parentId = 0) => {
  selectedDept.value = null
  formData.id = null
  formData.parentId = parentId
  formData.deptName = ''
  formData.deptCode = ''
  formData.leaderId = null
  formData.leader = ''
  formData.phone = ''
  formData.email = ''
  formData.sortOrder = 0
  formData.status = 1
}

// 保存
const handleSave = async () => {
  if (!formData.deptName) {
    ElMessage.warning('请输入部门名称')
    return
  }

  formLoading.value = true
  try {
    let res
    if (formData.id) {
      res = await updateDepartment(formData)
    } else {
      res = await createDepartment(formData)
    }

    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    formLoading.value = false
  }
}

// 删除部门
const handleDelete = async (data) => {
  // 有子节点不能删除
  if (data.children && data.children.length > 0) {
    ElMessage.warning('该部门下有子部门，不能删除')
    return
  }

  // 顶级部门不能删除
  if (data.deptCode === '1001') {
    ElMessage.warning('顶级部门不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除部门 "${data.deptName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteDepartment(data.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedDept.value?.id === data.id) {
        selectedDept.value = null
        formData.id = null
      }
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 操作按钮
const handleCommand = (command) => {
  switch (command) {
    case 'expand':
      handleExpandAll()
      break
    case 'collapse':
      handleCollapseAll()
      break
    case 'refresh':
      handleRefresh()
      break
  }
}

// 展开所有节点
const handleExpandAll = () => {
  const allKeys = getAllKeys(deptTree.value)
  expandedKeys.value = allKeys
  // 手动展开所有节点
  allKeys.forEach(key => {
    const node = treeRef.value?.getNode(key)
    if (node) {
      node.expanded = true
    }
  })
}

// 收缩所有节点
const handleCollapseAll = () => {
  expandedKeys.value = []
  // 手动收缩所有节点
  const allKeys = getAllKeys(deptTree.value)
  allKeys.forEach(key => {
    const node = treeRef.value?.getNode(key)
    if (node) {
      node.expanded = false
    }
  })
}

// 刷新树
const handleRefresh = () => {
  selectedDept.value = null
  formData.id = null
  searchKeyword.value = ''
  highlightKeyword.value = ''
  loadData()
}

// 获取所有节点的key
const getAllKeys = (data) => {
  const keys = []
  const traverse = (list) => {
    list.forEach(item => {
      keys.push(item.id)
      if (item.children && item.children.length > 0) {
        traverse(item.children)
      }
    })
  }
  traverse(data)
  return keys
}

// 判断是否有子节点
const hasChildren = (data) => {
  return data.children && data.children.length > 0
}
</script>

<template>
  <div class="flex h-[calc(100vh-120px)] gap-4">
    <!-- 左侧：部门树 -->
    <div class="w-96 flex-shrink-0 rounded-lg border border-gray-200 bg-white p-4">
      <div class="mb-3 flex items-center justify-between">
        <span class="font-medium">部门列表</span>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button size="small">
            操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="expand">展开全部</el-dropdown-item>
              <el-dropdown-item command="collapse">收缩全部</el-dropdown-item>
              <el-dropdown-item divided command="refresh">刷新</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 搜索框 -->
      <div class="mb-3">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索部门名称/编号"
          clearable
          size="small"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div v-loading="loading" class="h-[calc(100%-100px)] overflow-auto">
        <el-tree
          ref="treeRef"
          :data="deptTree"
          :props="{ label: 'deptName', children: 'children' }"
          node-key="id"
          :default-expanded-keys="expandedKeys"
          highlight-current
          :indent="10"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="flex items-center justify-between w-full pr-2">
              <span class="text-sm">
                <template v-if="highlightKeyword && data.deptName.includes(highlightKeyword)">
                  {{ data.deptName.substring(0, data.deptName.indexOf(highlightKeyword)) }}<span class="text-red-500 font-medium">{{ highlightKeyword }}</span>{{ data.deptName.substring(data.deptName.indexOf(highlightKeyword) + highlightKeyword.length) }}
                </template>
                <template v-else>{{ data.deptName }}</template>
              </span>
              <div class="flex gap-1">
                <el-button
                  type="primary"
                  link
                  :icon="Plus"
                  size="small"
                  @click.stop="handleAdd(data.id)"
                />
                <el-button
                  v-if="!hasChildren(data) && data.deptCode !== '1001'"
                  type="danger"
                  link
                  :icon="Delete"
                  size="small"
                  @click.stop="handleDelete(data)"
                />
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 右侧：部门详情 -->
    <div class="flex-1 flex flex-col rounded-lg border border-gray-200 bg-white">
      <div class="flex items-center border-b border-gray-100 px-4 py-3">
        <span class="font-medium">{{ formData.id ? '编辑' : '新增' }}</span>
      </div>

      <div v-loading="formLoading" class="flex-1 overflow-auto p-4">
        <el-form :model="formData" label-width="100px" class="max-w-xl">
          <el-form-item label="上级部门">
            <el-tree-select
              v-model="formData.parentId"
              :data="deptTree"
              :props="{ label: 'deptName', value: 'id', children: 'children' }"
              placeholder="请选择上级部门"
              check-strictly
              :disabled="formData.deptCode === '1001'"
            />
          </el-form-item>
          <el-form-item label="部门名称">
            <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
          </el-form-item>
          <el-form-item label="部门编号">
            <el-input v-model="formData.deptCode" placeholder="系统自动生成" disabled />
          </el-form-item>
          <el-form-item label="负责人">
            <el-select
              v-model="formData.leaderId"
              placeholder="请输入用户姓名搜索"
              filterable
              remote
              :remote-method="handleUserSearch"
              :loading="userLoading"
              clearable
              @change="handleUserChange"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.realName || user.userName"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="formData.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item class="!mb-0">
            <div class="flex justify-end w-full">
              <el-button type="primary" :icon="Check" :loading="formLoading" @click="handleSave">
                保存
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-tree) {
  --el-tree-node-content-height: 28px;
}

:deep(.el-tree-node__content) {
  border-radius: 4px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
}

:deep(.el-tree-node) {
  position: relative;
  padding-left: 16px;
}

:deep(.el-tree-node::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 6px;
  width: 1px;
  height: 100%;
  background-color: #e4e7ed;
}

:deep(.el-tree-node::after) {
  content: '';
  position: absolute;
  top: 14px;
  left: 6px;
  width: 10px;
  height: 1px;
  background-color: #e4e7ed;
}

:deep(.el-tree-node:last-child::before) {
  height: 14px;
}

:deep(.el-tree-node__expand-icon) {
  margin-left: -2px;
  font-size: 14px;
}
</style>
