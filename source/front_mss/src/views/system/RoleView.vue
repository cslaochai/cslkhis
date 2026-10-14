<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item">
            <el-input v-model="q.keyword" placeholder="角色编码/名称" clearable @keyup.enter="search" />
          </div>
        </div>
        <div class="query-grid-ops">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="primary" plain @click="openEdit(null)">新增角色</el-button>
        </div>
      </div>
    </div>

    <div class="his-card p-3">
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.roleType === 1 ? 'primary' : 'info'">
              {{ row.roleType === 1 ? '系统角色' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据范围" width="120" align="center">
          <template #default="{ row }">
            {{ { 1: '全部数据', 2: '自定义数据', 3: '本部门数据', 5: '仅本人' }[row.dataScope] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-3">
        <el-pagination
          :page-sizes="PAGE_SIZES" :page-size="q.size" :current-page="q.page" :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="(p) => { q.page = p; load() }"
          @size-change="(s) => { q.size = s; q.page = 1; load() }"
        />
      </div>
    </div>

    <!-- 新增/编辑角色（含菜单授权） -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑角色' : '新增角色'" width="640px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色编码" prop="roleCode">
              <el-input v-model="form.roleCode" placeholder="如 MSS_OPERATOR" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据范围">
              <el-select v-model="form.dataScope" style="width:100%">
                <el-option label="全部数据" :value="1" />
                <el-option label="自定义数据" :value="2" />
                <el-option label="本部门数据" :value="3" />
                <el-option label="仅本人数据" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
          <el-col :span="24">
            <el-form-item label="菜单权限">
              <div class="border rounded-md p-2 w-full" style="max-height: 300px; overflow:auto">
                <el-tree
                  ref="menuTreeRef"
                  :data="menuTreeData"
                  node-key="id"
                  show-checkbox
                  default-expand-all
                  :props="{ label: 'menuName', children: 'children' }"
                />
              </div>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rolePage, roleDetail, roleUpsert, roleDelete, menuTree } from '@/api/system'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', page: 1, size: 10 })

async function load() {
  loading.value = true
  const res = await rolePage({ pageNum: q.page, pageSize: q.size, keyword: q.keyword || undefined })
  loading.value = false
  list.value = res.data.records || []
  total.value = res.data.total || 0
}
function search() { q.page = 1; load() }
function reset() { q.keyword = ''; q.page = 1; load() }

// 菜单树（授权勾选用）
const menuTreeData = ref([])
async function loadMenuTree() {
  const res = await menuTree()
  menuTreeData.value = res.data || []
}

// 新增/编辑
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const menuTreeRef = ref()
const form = reactive({
  id: null, roleCode: '', roleName: '', roleType: 2, dataScope: 3, sortOrder: 0, status: 1, remark: '',
})
const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

function openEdit(row) {
  Object.assign(form, {
    id: row?.id || null, roleCode: row?.roleCode || '', roleName: row?.roleName || '',
    roleType: row?.roleType ?? 2, dataScope: row?.dataScope ?? 3,
    sortOrder: row?.sortOrder ?? 0, status: row?.status ?? 1, remark: row?.remark || '',
  })
  editVisible.value = true
  // 回显授权：编辑时先拉详情拿 menuIds，再勾选
  loadMenuTree().then(() => {
    if (form.id) {
      roleDetail(form.id).then((res) => {
        const ids = (res.data?.menuIds || []).map(String)
        menuTreeRef.value?.setCheckedKeys(ids)
      })
    } else {
      menuTreeRef.value?.setCheckedKeys([])
    }
  })
}

async function onSave() {
  await formRef.value.validate()
  const checked = (menuTreeRef.value?.getCheckedKeys() || []).map(Number)
  const half = (menuTreeRef.value?.getHalfCheckedKeys() || []).map(Number)
  saving.value = true
  try {
    await roleUpsert({
      id: form.id, roleCode: form.roleCode, roleName: form.roleName,
      roleType: form.roleType, dataScope: form.dataScope, sortOrder: form.sortOrder,
      status: form.status, remark: form.remark, menuIds: [...checked, ...half],
    })
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
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  await roleDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  load()
  loadMenuTree()
})
</script>
