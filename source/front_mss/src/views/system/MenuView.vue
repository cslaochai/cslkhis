<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="flex items-center justify-between mb-3">
        <div class="font-semibold text-[15px] text-[#0B3B66]">菜单管理（目录/菜单/按钮）</div>
        <el-button type="primary" @click="openEdit(null)">新增菜单</el-button>
      </div>

      <el-table
        :data="tree" row-key="id" border size="small" v-loading="loading"
        :tree-props="{ children: 'children' }" default-expand-all
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="{ 1: 'warning', 2: 'primary', 3: 'info' }[row.menuType]">
              {{ { 1: '目录', 2: '菜单', 3: '按钮' }[row.menuType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" />
        <el-table-column prop="component" label="组件" width="220" show-overflow-tooltip />
        <el-table-column prop="menuKey" label="菜单标识" width="140" />
        <el-table-column prop="permission" label="权限码" width="160" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="可见" width="70" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isVisible === 1 ? 'success' : 'info'">
              {{ row.isVisible === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="openEdit(null, row)">新增子级</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑菜单 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions" node-key="id"
            :props="{ label: 'menuName', children: 'children' }"
            check-strictly clearable placeholder="不选为顶级菜单" style="width:100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单标识" prop="menuKey">
          <el-input v-model="form.menuKey" placeholder="唯一标识，如 mss_settle_monthly" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="路由路径">
              <el-input v-model="form.path" placeholder="如 /settlement/monthly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件">
              <el-input v-model="form.component" placeholder="如 settlement/MonthlySettleView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标">
              <el-input v-model="form.icon" placeholder="Element 图标名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限码">
              <el-input v-model="form.permission" placeholder="如 mss:monthly:declare" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可见">
              <el-radio-group v-model="form.isVisible">
                <el-radio :value="1">显示</el-radio>
                <el-radio :value="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { menuTree, menuUpsert, menuDelete } from '@/api/system'

const tree = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  const res = await menuTree()
  loading.value = false
  tree.value = res.data || []
}

const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null, parentId: null, menuName: '', menuType: 2, menuKey: '',
  path: '', component: '', icon: '', permission: '', sortOrder: 0,
  isVisible: 1, isFrame: 0, isCache: 1, status: 1, remark: '',
})
const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuKey: [{ required: true, message: '请输入菜单标识', trigger: 'blur' }],
}

// 上级菜单选项：过滤掉按钮类型与自身
const parentOptions = computed(() => filterMenu(tree.value))

function filterMenu(menus) {
  return (menus || [])
    .filter((m) => m.menuType !== 3)
    .map((m) => ({
      ...m,
      children: form.id ? (m.children || []).filter((c) => c.id !== form.id) : m.children || [],
    }))
}

function openEdit(row, parentRow) {
  Object.assign(form, {
    id: row?.id || null,
    parentId: parentRow ? parentRow.id : row?.parentId || null,
    menuName: row?.menuName || '',
    menuType: row?.menuType ?? 2,
    menuKey: row?.menuKey || '',
    path: row?.path || '',
    component: row?.component || '',
    icon: row?.icon || '',
    permission: row?.permission || '',
    sortOrder: row?.sortOrder ?? 0,
    isVisible: row?.isVisible ?? 1,
    isFrame: row?.isFrame ?? 0,
    isCache: row?.isCache ?? 1,
    status: row?.status ?? 1,
    remark: row?.remark || '',
  })
  editVisible.value = true
}

async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await menuUpsert({ ...form, parentId: form.parentId || undefined })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e) {
    // 后端 message 已全局提示
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  const label = row.menuType === 1 ? `目录「${row.menuName}」（其下子级将一并删除）` : `菜单「${row.menuName}」`
  await ElMessageBox.confirm(`确定删除${label}吗？`, '提示', { type: 'warning' })
  await menuDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
