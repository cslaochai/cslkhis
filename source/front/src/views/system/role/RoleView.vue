<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="角色名称">
            <el-input
                v-model="searchForm.roleName"
                clearable
                placeholder="请输入角色名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'system:role:add'" :icon="Plus" type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="角色编码" prop="roleCode" width="120"/>
        <el-table-column label="角色名称" prop="roleName" width="150"/>
        <el-table-column label="角色类型" prop="roleType" width="120">
          <template #default="{ row }">
            <el-tag :type="row.roleType === 1 ? 'danger' : 'success'">
              {{ row.roleType === 1 ? '系统角色' : '自定义角色' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色描述" min-width="180" prop="remark"/>
        <el-table-column label="数据范围" prop="dataScope" width="150">
          <template #default="{ row }">
            <el-tag :type="row.dataScope === 1 ? 'warning' : 'info'">{{ dataScopeLabel(row.dataScope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180"/>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="{ row }">
            <el-button :icon="Setting" link type="primary" @click="handleConfigMenu(row)">菜单权限</el-button>
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.roleType !== 1" v-perm="'system:role:delete'" :icon="Delete" link type="danger"
                       @click="handleDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            :current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        width="500px"
        @close="dialogVisible = false"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'system:role:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 菜单权限配置抽屉 -->
    <el-drawer
        v-model="menuDrawerVisible"
        :title="`配置菜单权限 - ${menuRole?.roleName || ''}`"
        destroy-on-close
        size="440px"
    >
      <div v-loading="menuLoading">
        <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ label: 'menuName', children: 'children' }"
            default-expand-all
            node-key="id"
            show-checkbox
        />
        <div class="mt-4 flex justify-end gap-2 border-t border-slate-200 pt-4">
          <el-button @click="menuDrawerVisible = false">取消</el-button>
          <el-button :loading="menuSubmitting" type="primary" @click="handleSaveRoleMenu">
            保存
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="js" setup>
import {nextTick, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search, Setting} from '@element-plus/icons-vue'
import {
  createRole,
  deleteRole,
  getMenuTree,
  getRoleDetail,
  getRoleListPage,
  getRoleMenuIds,
  saveRoleMenu,
  updateRole,
} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const searchForm = ref({
  roleName: '',
})
const tableData = ref([])
/** 分页：走后端 /system/role/listPage，前端不再全量拉取后自己切 */
const pagination = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
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

// 表格展示口径与上面的提交口径一致：只有 1 是全院
const dataScopeLabel = (v) => (v === 1 ? '全部数据（全院）' : '按岗位科室')

const rules = {
  roleName: [
    {required: true, message: '请输入角色名称', trigger: 'blur'},
  ],
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleListPage({
      roleName: searchForm.value.roleName,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      pagination.total = Number(res.data?.total || 0)
      // 删掉本页最后一条后页码可能越界，回退到最后一页重查（避免停在空白页）
      const maxPage = Math.max(1, Math.ceil(pagination.total / pagination.pageSize))
      if (pagination.pageNum > maxPage) {
        pagination.pageNum = maxPage
        return loadData()
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value.roleName = ''
  pagination.pageNum = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
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

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑角色'
  try {
    const res = await getRoleDetail(row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      // 存量角色 data_scope 可能是 NULL：归一成 2（按岗位科室），下拉才有值、保存才不会被跳过
      if (formData.dataScope !== 1) formData.dataScope = 2
    } else {
      ElMessage.error(res.message || '获取角色信息失败')
      return
    }
  } catch (error) {
    ElMessage.error(error.message || '获取角色信息失败')
    return
  }
  dialogVisible.value = true
}

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
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  // 系统角色不允许删除
  if (row.roleType === 1) {
    ElMessage.warning('系统角色不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
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

// ===== 菜单权限配置 =====
const menuDrawerVisible = ref(false)
const menuTree = ref([])
const menuTreeRef = ref(null)
const menuLoading = ref(false)
const menuSubmitting = ref(false)
const menuRole = ref(null)

const ensureMenuTree = async () => {
  if (menuTree.value.length) return
  const res = await getMenuTree()
  if (res.code === 200) menuTree.value = res.data || []
}

const handleConfigMenu = async (row) => {
  menuRole.value = row
  menuDrawerVisible.value = true
  menuLoading.value = true
  try {
    // 菜单树的 id 由后端按字符串下发（MenuVO 用 ToStringSerializer），
    // 因此 tree 的 key 与回显值都必须用 String，否则 setCheckedKeys 匹配不上、权限树全空
    const [, idsRes] = await Promise.all([ensureMenuTree(), getRoleMenuIds(row.id)])
    await nextTick()
    const ids = idsRes.code === 200 ? (idsRes.data || []) : []
    // 回显只设置叶子节点。el-tree 的 setCheckedKeys(keys, leafOnly) 在父目录上不可靠：
    // 目录一旦被当作可勾选节点选中，父子联动会把它下面**未授权**的子菜单一并勾上，
    // 保存后权限被放大（实测医生角色 23 条被写成 32 条）。
    // 这里显式剔除「有子节点的目录」，父节点的全选/半选交给 el-tree 自己推导。
    const parentIds = new Set()
    const collectParents = (nodes) => {
      for (const node of nodes || []) {
        if (node.children && node.children.length) {
          parentIds.add(String(node.id))
          collectParents(node.children)
        }
      }
    }
    collectParents(menuTree.value)
    menuTreeRef.value?.setCheckedKeys(ids.map(String).filter(id => !parentIds.has(id)))
  } catch (error) {
    ElMessage.error(error.message || '加载菜单权限失败')
  } finally {
    menuLoading.value = false
  }
}

const handleSaveRoleMenu = async () => {
  if (!menuTreeRef.value || !menuRole.value) return
  // 半选状态的父目录也要提交，否则后端拿到的菜单可能缺目录节点
  const checked = menuTreeRef.value.getCheckedKeys()
  const halfChecked = menuTreeRef.value.getHalfCheckedKeys()
  menuSubmitting.value = true
  try {
    const res = await saveRoleMenu({
      roleId: menuRole.value.id,
      menuIds: [...checked, ...halfChecked],
    })
    if (res.code === 200) {
      ElMessage.success('菜单权限保存成功')
      menuDrawerVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    menuSubmitting.value = false
  }
}
</script>
