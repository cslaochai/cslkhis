<template>
  <div class="flex h-[calc(100vh-120px)] gap-4">
    <!-- 左侧：菜单树 -->
    <div class="w-96 flex-shrink-0 rounded-lg border border-gray-200 bg-white p-4">
      <div class="mb-3 flex items-center justify-between">
        <span class="font-medium">菜单列表</span>
        <div class="tree-toolbar flex items-center">
          <el-button :icon="Expand" bg size="small" text @click="handleExpandAll">展开</el-button>
          <el-button :icon="Fold" bg size="small" text @click="handleCollapseAll">收缩</el-button>
          <el-button :icon="Refresh" bg size="small" text @click="handleRefresh">刷新</el-button>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="mb-3">
        <el-input
            v-model="searchKeyword"
            clearable
            placeholder="搜索菜单名称"
            size="small"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
      </div>

      <div v-loading="loading" class="h-[calc(100%-100px)] overflow-auto">
        <el-tree
            ref="treeRef"
            :data="menuTree"
            :default-expanded-keys="expandedKeys"
            :indent="10"
            :props="{ label: 'menuName', children: 'children' }"
            highlight-current
            node-key="id"
            @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="flex items-center justify-between w-full pr-2">
              <span :class="{ 'text-slate-400': data.menuType === 3 }" class="text-sm">
                <template v-if="highlightKeyword && data.menuName.includes(highlightKeyword)">
                  {{ data.menuName.substring(0, data.menuName.indexOf(highlightKeyword)) }}<span
                    class="text-red-500 font-medium">{{
                    highlightKeyword
                  }}</span>{{
                    data.menuName.substring(data.menuName.indexOf(highlightKeyword) + highlightKeyword.length)
                  }}
                </template>
                <template v-else>{{ data.menuName }}</template>
              </span>
              <!-- G5：权限码树内直显，绑定关系不再是黑盒 -->
              <span
                  v-if="data.permission"
                  :title="data.permission"
                  class="ml-auto mr-1 max-w-[160px] truncate rounded bg-slate-100 px-1 text-[10px] text-slate-500"
                  data-testid="g5-menu-perm-badge"
              >{{ data.permission }}</span>
              <div class="flex gap-1">
                <el-button
                    v-if="data.menuType !== 3"
                    v-perm="'system:menu:add'"
                    :icon="Plus"
                    link
                    size="small"
                    type="primary"
                    @click.stop="handleAdd(data.id)"
                />
                <el-button
                    v-if="!hasChildren(data)"
                    v-perm="'system:menu:delete'"
                    :icon="Delete"
                    link
                    size="small"
                    type="danger"
                    @click.stop="handleDelete(data)"
                />
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 右侧：菜单详情 -->
    <div class="flex-1 flex flex-col rounded-lg border border-gray-200 bg-white">
      <div class="flex items-center border-b border-gray-100 px-4 py-3">
        <span class="font-medium">{{ formData.id ? '编辑' : '新增' }}</span>
      </div>

      <div v-loading="formLoading" class="flex-1 overflow-auto p-4">
        <el-form :model="formData" class="max-w-xl" label-width="100px">
          <el-form-item label="菜单类型">
            <el-radio-group v-model="formData.menuType">
              <el-radio
                  v-for="item in menuTypeOptions"
                  :key="item.value"
                  :value="item.value"
              >
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="菜单名称">
            <el-input v-model="formData.menuName" placeholder="请输入菜单名称"/>
          </el-form-item>
          <el-form-item v-if="formData.menuType !== 3" label="图标">
            <el-input v-model="formData.icon" placeholder="请输入图标名称"/>
          </el-form-item>
          <el-form-item v-if="formData.menuType !== 3" label="路由地址">
            <el-input v-model="formData.path" placeholder="请输入路由地址"/>
          </el-form-item>
          <el-form-item v-if="formData.menuType === 2" label="组件路径">
            <el-input v-model="formData.component" placeholder="请输入组件路径"/>
          </el-form-item>
          <!-- 页面与按钮都可维护权限标识；目录不挂权限码（角色配页面即配权限） -->
          <el-form-item v-if="formData.menuType !== 1" label="权限标识">
            <el-input v-model="formData.permission" data-testid="g5-menu-permission"
                      placeholder="如: opd:appointments:list（模块:页面:动作）"/>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="formData.sortOrder" :max="999" :min="0"/>
          </el-form-item>
          <el-form-item v-if="formData.menuType !== 3" label="是否可见">
            <el-radio-group v-model="formData.isVisible">
              <el-radio :value="1">显示</el-radio>
              <el-radio :value="0">隐藏</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item class="!mb-0">
            <div class="flex justify-end w-full">
              <el-button v-perm="'system:menu:add'" :icon="Check" :loading="formLoading" type="primary"
                         @click="handleSave">
                保存
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Check, Delete, Expand, Fold, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {createMenu, deleteMenu, getMenuDetail, getMenuTree, updateMenu} from '@/api/system'

const loading = ref(false)
const menuTree = ref([])
const expandedKeys = ref([])
const treeRef = ref(null)

// 搜索
const searchKeyword = ref('')
const highlightKeyword = ref('')

// 选中的菜单
const selectedMenu = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  menuType: 2,
  icon: '',
  path: '',
  component: '',
  permission: '',
  sortOrder: 0,
  isVisible: 1,
  status: 1,
})

const formLoading = ref(false)

// 菜单类型选项
const menuTypeOptions = [
  {value: 1, label: '目录'},
  {value: 2, label: '菜单'},
  {value: 3, label: '按钮'},
]

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    if (res.code === 200) {
      menuTree.value = res.data || []
      // 默认展开第一级
      if (menuTree.value.length > 0) {
        expandedKeys.value = menuTree.value.map(item => item.id)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索菜单
const handleSearch = () => {
  highlightKeyword.value = searchKeyword.value

  // 展开匹配的节点
  if (searchKeyword.value) {
    const matchedKeys = []
    const findMatched = (list) => {
      list.forEach(item => {
        if (item.menuName.includes(searchKeyword.value)) {
          matchedKeys.push(item.id)
        }
        if (item.children && item.children.length > 0) {
          findMatched(item.children)
        }
      })
    }
    findMatched(menuTree.value)
    expandedKeys.value = matchedKeys
  }
}

// 点击树节点
const handleNodeClick = async (data) => {
  selectedMenu.value = data
  formLoading.value = true
  try {
    const res = await getMenuDetail(data.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取菜单信息失败')
  } finally {
    formLoading.value = false
  }
}

// 新增菜单（在节点后面）
const handleAdd = (parentId = 0) => {
  selectedMenu.value = null
  formData.id = null
  formData.parentId = parentId
  formData.menuName = ''
  formData.menuType = 2
  formData.icon = ''
  formData.path = ''
  formData.component = ''
  formData.permission = ''
  formData.sortOrder = 0
  formData.isVisible = 1
  formData.status = 1
}

// 保存
const handleSave = async () => {
  if (!formData.menuName) {
    ElMessage.warning('请输入菜单名称')
    return
  }

  formLoading.value = true
  try {
    let res
    if (formData.id) {
      res = await updateMenu(formData)
    } else {
      res = await createMenu(formData)
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

// 删除菜单
const handleDelete = async (data) => {
  // 有子节点不能删除
  if (data.children && data.children.length > 0) {
    ElMessage.warning('该菜单下有子节点，不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除菜单 "${data.menuName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteMenu(data.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedMenu.value?.id === data.id) {
        selectedMenu.value = null
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

// 展开所有节点
const handleExpandAll = () => {
  const allKeys = getAllKeys(menuTree.value)
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
  const allKeys = getAllKeys(menuTree.value)
  allKeys.forEach(key => {
    const node = treeRef.value?.getNode(key)
    if (node) {
      node.expanded = false
    }
  })
}

// 刷新树
const handleRefresh = () => {
  selectedMenu.value = null
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

<style scoped>
/* 工具栏三按钮：EP 默认给相邻 el-button 加 12px 左边距，贴右紧凑要归零并收窄内边距 */
.tree-toolbar :deep(.el-button + .el-button) {
  margin-left: 2px;
}

.tree-toolbar :deep(.el-button) {
  padding: 5px 8px;
}

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

/* 搜索高亮 */
:deep(.el-tree-node__label) {
  padding: 2px 4px;
  border-radius: 2px;
}

.highlight-node > .el-tree-node__content {
  background-color: #fff7e6 !important;
}
</style>
