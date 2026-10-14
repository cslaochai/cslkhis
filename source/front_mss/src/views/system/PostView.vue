<template>
  <div class="space-y-3">
    <!-- 岗位配置（按员工，岗位 = 角色 × 科室，抄 HIS EmployeePostTable 口径） -->
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
        <span class="text-[12px] text-gray-400">岗位 = 角色 × 科室；每人恰好一条主岗位（登录默认落点）</span>
      </div>

      <el-empty v-if="!selectedEmp" description="请选择员工后配置其岗位" :image-size="70" />
      <EmployeePostTable
        v-else
        v-model="postRows"
        :dept-list="deptTree"
        :role-list="roles"
      />
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
import { ElMessage } from 'element-plus'
import {
  employeeSelect, postPage, postsOfEmployee, postReplace,
  getRoleList, getDepartmentTree,
} from '@/api/system'
import { PAGE_SIZES } from '@/lib/pagination'
import EmployeePostTable from '@/components/his/EmployeePostTable.vue'
import { postsFromApi, postsToPayload } from '@/lib/employeePost'

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

// 岗位编辑（HIS 口径：角色按 roleCode 成对科室）
const postRows = ref([])
const roles = ref([])
const deptTree = ref([])
const saving = ref(false)

async function loadPosts() {
  if (!selectedEmp.value) return
  const res = await postsOfEmployee(selectedEmp.value)
  postRows.value = postsFromApi(res.data || [])
}

async function onSave() {
  const payload = postsToPayload(postRows.value)
  if (payload.length === 0) {
    ElMessage.warning('请至少配置一条「角色 + 科室」岗位')
    return
  }
  saving.value = true
  try {
    await postReplace({
      employeeId: selectedEmp.value,
      posts: payload,
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
  const [r, d] = await Promise.all([getRoleList({}), getDepartmentTree()])
  roles.value = r.data || []
  deptTree.value = d.data || []
  load()
})
</script>
