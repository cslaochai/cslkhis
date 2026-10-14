<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item">
            <el-input v-model="q.keyword" placeholder="用户名/姓名" clearable @keyup.enter="search" />
          </div>
          <div class="q-item">
            <el-select v-model="q.status" placeholder="状态" clearable style="width:100%">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </div>
        </div>
        <div class="query-grid-ops">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="primary" plain @click="openEdit(null)">新增用户</el-button>
        </div>
      </div>
    </div>

    <div class="his-card p-3">
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="empName" label="员工" width="110" />
        <el-table-column prop="deptName" label="科室" width="120" />
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="r in row.roleNames || []" :key="r" size="small" class="mr-1">{{ r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : row.status === 2 ? '锁定' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">{{ row.lastLoginTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="loginCount" label="登录次数" width="90" align="right" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openReset(row)">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
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

    <!-- 新增/编辑用户 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑用户' : '新增用户'" width="480px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="默认 123456" />
        </el-form-item>
        <el-form-item label="关联员工">
          <el-select v-model="form.empId" filterable clearable placeholder="选择员工（取岗位配置）" style="width:100%">
            <el-option v-for="e in employees" :key="e.id" :label="`${e.empName}（${e.empCode}）`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="400px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="用户">{{ resetTarget?.realName }}（{{ resetTarget?.userName }}）</el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userPage, userUpsert, userResetPassword, userChangeStatus, userDelete, employeeSelect } from '@/api/system'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', status: '', page: 1, size: 10 })
const employees = ref([])

async function load() {
  loading.value = true
  const res = await userPage({
    pageNum: q.page, pageSize: q.size,
    keyword: q.keyword || undefined, status: q.status === '' ? undefined : q.status,
  })
  loading.value = false
  list.value = res.data.records || []
  total.value = res.data.total || 0
}
function search() { q.page = 1; load() }
function reset() { q.keyword = ''; q.status = ''; q.page = 1; load() }

async function loadEmployees() {
  const res = await employeeSelect('')
  employees.value = res.data || []
}

// 新增/编辑
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, userName: '', realName: '', password: '', empId: null, status: 1, remark: '' })
const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}
function openEdit(row) {
  if (row) {
    Object.assign(form, {
      id: row.id, userName: row.userName, realName: row.realName, password: '',
      empId: row.empId, status: row.status ?? 1, remark: row.remark || '',
    })
  } else {
    Object.assign(form, { id: null, userName: '', realName: '', password: '', empId: null, status: 1, remark: '' })
  }
  editVisible.value = true
}
async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await userUpsert({
      id: form.id, userName: form.userName, realName: form.realName,
      password: form.password || undefined, empId: form.empId,
      status: form.status, remark: form.remark,
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

// 重置密码
const resetVisible = ref(false)
const resetTarget = ref(null)
const newPassword = ref('')
function openReset(row) {
  resetTarget.value = row
  newPassword.value = ''
  resetVisible.value = true
}
async function onReset() {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  saving.value = true
  try {
    await userResetPassword({ id: resetTarget.value.id, newPassword: newPassword.value })
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } catch (e) {
  } finally {
    saving.value = false
  }
}

// 启停
async function toggleStatus(row) {
  const target = row.status === 1 ? 0 : 1
  await userChangeStatus({ id: row.id, status: target })
  ElMessage.success(target === 1 ? '已启用' : '已停用')
  load()
}

// 删除
async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.userName}」吗？`, '提示', { type: 'warning' })
  await userDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  load()
  loadEmployees()
})
</script>
