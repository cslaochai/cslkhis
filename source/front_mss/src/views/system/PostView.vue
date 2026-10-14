<template>
  <div class="space-y-3">
    <!-- 岗位配置（按员工） -->
    <div class="his-card p-3">
      <div class="flex items-center gap-3 mb-3">
        <span class="font-semibold text-[15px] text-[#0B3B66]">岗位配置</span>
        <el-select
          v-model="selectedEmp" filterable remote clearable placeholder="搜索并选择员工"
          :remote-method="searchEmp" :loading="empLoading" style="width: 280px"
        >
          <el-option v-for="e in employees" :key="e.id" :label="`${e.empName}（${e.empCode}）`" :value="e.id" />
        </el-select>
        <el-button type="primary" :disabled="!selectedEmp" @click="loadPosts">加载岗位</el-button>
        <el-button type="success" :disabled="!selectedEmp" :loading="saving" @click="onSave">保存岗位</el-button>
        <span class="text-[12px] text-gray-400">岗位 = 角色 × 科室；每人恰好一条主岗位</span>
      </div>

      <el-empty v-if="!selectedEmp" description="请选择员工后配置其岗位" :image-size="70" />
      <el-table v-else :data="posts" size="small" border>
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-select v-model="row.roleId" placeholder="选择角色" style="width:100%">
              <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="科室" min-width="160">
          <template #default="{ row }">
            <el-select v-model="row.deptId" placeholder="选择科室" style="width:100%">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="主岗位" width="90" align="center">
          <template #default="{ row, $index }">
            <el-checkbox
              :model-value="row.isPrimary === 1"
              @change="(v) => setPrimary($index, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="生效日期" width="150">
          <template #default="{ row }">
            <el-date-picker v-model="row.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="失效日期" width="150">
          <template #default="{ row }">
            <el-date-picker v-model="row.expireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" @click="posts.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-2" v-if="selectedEmp">
        <el-button plain type="primary" size="small" @click="addRow">+ 添加岗位</el-button>
      </div>
    </div>

    <!-- 全部岗位（只读分页） -->
    <div class="his-card p-3">
      <div class="flex items-center justify-between mb-2">
        <div class="font-semibold text-[15px] text-[#0B3B66]">全部岗位</div>
        <div class="q-item" style="width:240px">
          <el-input v-model="q.keyword" placeholder="员工姓名/工号" clearable @keyup.enter="search" />
        </div>
      </div>
      <el-table :data="allPosts" size="small" v-loading="loading">
        <el-table-column prop="empCode" label="工号" width="100" />
        <el-table-column prop="empName" label="员工" width="110" />
        <el-table-column prop="roleName" label="角色" width="150" />
        <el-table-column prop="deptName" label="科室" width="150" />
        <el-table-column label="主岗位" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isPrimary === 1" size="small" type="success">主</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="expireDate" label="失效日期" width="120" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  employeeSelect, postPage, postsOfEmployee, postReplace, roleSelectList, deptSelectList,
} from '@/api/system'
import { PAGE_SIZES } from '@/lib/pagination'

// 员工选择（远程搜索）
const employees = ref([])
const empLoading = ref(false)
const selectedEmp = ref(null)
async function searchEmp(kw) {
  empLoading.value = true
  const res = await employeeSelect(kw || '')
  empLoading.value = false
  employees.value = res.data || []
}

// 岗位编辑
const posts = ref([])
const roles = ref([])
const depts = ref([])
const saving = ref(false)

async function loadPosts() {
  if (!selectedEmp.value) return
  const res = await postsOfEmployee(selectedEmp.value)
  posts.value = (res.data || []).map((p) => ({
    roleId: Number(p.roleId), deptId: Number(p.deptId), isPrimary: p.isPrimary ?? 0,
    effectiveDate: p.effectiveDate || '', expireDate: p.expireDate || '',
  }))
}

function addRow() {
  posts.value.push({ roleId: null, deptId: null, isPrimary: 0, effectiveDate: '', expireDate: '' })
}

function setPrimary(index, v) {
  posts.value.forEach((p, i) => { p.isPrimary = i === index ? (v ? 1 : 0) : 0 })
}

async function onSave() {
  const valid = posts.value.filter((p) => p.roleId && p.deptId)
  if (valid.length === 0) {
    ElMessage.warning('请至少配置一条「角色 + 科室」岗位')
    return
  }
  if (!valid.some((p) => p.isPrimary === 1)) {
    await ElMessageBox.confirm('未勾选主岗位，保存后将自动把第一条设为主岗位，继续吗？', '提示', { type: 'warning' })
  }
  saving.value = true
  try {
    await postReplace({
      employeeId: selectedEmp.value,
      posts: valid.map((p) => ({
        roleId: p.roleId, deptId: p.deptId, isPrimary: p.isPrimary,
        effectiveDate: p.effectiveDate || undefined, expireDate: p.expireDate || undefined,
      })),
    })
    ElMessage.success('岗位保存成功')
    loadPosts()
    load()
  } catch (e) {
    // 后端 message 已全局提示
  } finally {
    saving.value = false
  }
}

// 全部岗位（只读）
const allPosts = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', page: 1, size: 10 })

async function load() {
  loading.value = true
  const res = await postPage({ pageNum: q.page, pageSize: q.size, keyword: q.keyword || undefined })
  loading.value = false
  allPosts.value = res.data.records || []
  total.value = res.data.total || 0
}
function search() { q.page = 1; load() }

onMounted(async () => {
  searchEmp('')
  const [r, d] = await Promise.all([roleSelectList(), deptSelectList()])
  roles.value = r.data || []
  depts.value = d.data || []
  load()
})
</script>
