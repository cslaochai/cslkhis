<script setup lang="js">
import {ref, onMounted, reactive, computed} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search, Edit, Delete, Refresh} from '@element-plus/icons-vue'
import {
  getUserList,
  getUserDetail,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  getDepartmentTree,
  getRoleList,
  getDictDataMapList
} from '@/api/system'
import {USER_STATUS, getDictLabel} from '@/lib/dict'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import EmployeePostTable from '@/components/his/EmployeePostTable.vue'
import {postsFromApi, postsToPayload, checkPosts} from '@/lib/employeePost'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)
const searchForm = reactive({
  userName: '',
  status: '',
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 部门列表
const deptList = ref([])

// 角色列表
const roleList = ref([])

// 字典数据
const titleOptions = ref([])  // 职称
const positionOptions = ref([])  // 职位
const educationOptions = ref([]) // 学历

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
// 行点击打开的详情：患者/其他账号不可编辑，弹窗只读
const viewOnly = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 账号 + 员工档案共用一个表单对象（一个 el-form 搞定）：
// 员工字段只在院内用户时展示并参与校验，提交时仍是平铺进同一个 payload
const formData = reactive({
  id: null,
  userName: '',
  realName: '',
  userType: 1,  // 1-院内用户 2-院外用户 3-患者 4-其他
  empId: null,   // 员工ID（院内用户关联）
  empNo: '',     // 工号（后端从关联员工带出，只读展示）
  status: 1,
  // 员工信息（院内用户时填写）
  gender: 1,
  phone: '',
  email: '',
  title: '',
  specialty: '',
  education: '',
  birthDate: '',
  idCard: '',
  position: '',
})

// 岗位（角色 × 科室）：一行 = 这个人在该科室以该角色执业。
// 取代原来的「执业科室多选 + 主科室 + 所属角色多选」三处独立配置
const postRows = ref([])

// 是否是院内用户
const isInternal = computed(() => formData.userType === 1)

// 列表页布局（两卡式）：查询卡与表格卡分隔；面板高随内容自适应，不锁死。
// 表格高度只设最大值：不足时表格随内容收缩，超过最大值表格内部滚动（高度计算在 useTableMaxHeight）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// 患者(3)/其他(4) 账号由自助注册等流程维护，用户管理页不开编辑/改状态/重置密码/删除（后端同步拦截）
const canManage = (row) => row.userType !== 3 && row.userType !== 4

// admin 账号只允许查看详情，编辑入口隐藏（删除本来就有同款拦截）
const canEdit = (row) => canManage(row) && row.userName !== 'admin'

const rules = {
  userName: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur'},
  ],
  realName: [
    {required: true, message: '请输入姓名', trigger: 'blur'},
  ],
}

// 员工字段的必填只对内联的「院内用户」成立：非院内用户页面上根本不渲染这些 prop，
// 规则也一并收进 computed，避免两个 el-form 各挂一套 rules 的历史结构复活
const empRules = {
  gender: [{required: true, message: '请选择性别', trigger: 'change'}],
  phone: [
    {required: true, message: '请输入联系电话', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur'},
  ],
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'},
  ],
  title: [{required: true, message: '请输入职称', trigger: 'blur'}],
  education: [{required: true, message: '请选择学历', trigger: 'change'}],
  birthDate: [{required: true, message: '请选择出生日期', trigger: 'change'}],
  idCard: [
    {required: true, message: '请输入身份证号', trigger: 'blur'},
    {pattern: /^\d{17}[\dX]$/, message: '请输入正确的身份证号', trigger: 'blur'},
  ],
}

const formRules = computed(() => (isInternal.value ? {...rules, ...empRules} : rules))

onMounted(() => {
  loadData()
  loadDeptList()
  loadRoleList()
  loadDictData()
})

const loadDeptList = async () => {
  try {
    const res = await getDepartmentTree()
    if (res.code === 200) {
      deptList.value = res.data || []
    }
  } catch (error) {
    console.error('加载部门列表失败', error)
  }
}

const loadRoleList = async () => {
  try {
    const res = await getRoleList({})
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

const loadDictData = async () => {
  try {
    const res = await getDictDataMapList('sys_hospital_title,hospital_position,his_education')
    if (res.code === 200 && res.data) {
      titleOptions.value = res.data['sys_hospital_title'] || []
      positionOptions.value = res.data['hospital_position'] || []
      educationOptions.value = res.data['his_education'] || []
    }
  } catch (error) {
    console.error('加载字典数据失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      ...searchForm,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.status = ''
  handleSearch()
}

const resetForm = () => {
  formData.id = null
  formData.userName = ''
  formData.realName = ''
  formData.userType = 1
  formData.empId = null
  formData.empNo = ''
  formData.status = 1

  // 重置员工信息（字段与账号同在 formData）
  formData.gender = 1
  formData.phone = ''
  formData.email = ''
  formData.title = ''
  formData.specialty = ''
  formData.education = ''
  formData.birthDate = ''
  formData.idCard = ''
  formData.position = ''

  postRows.value = []
}

// 岗位校验报错要写成名字，所以科室/角色反查留在页面里（科室是树，需递归）
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

const handleAdd = () => {
  resetForm()
  viewOnly.value = false
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

// 点行 = 只看详情；改资料必须走「编辑」按钮
const handleRowClick = (row) => openUserDialog(row, true)

const handleEdit = (row) => openUserDialog(row, false)

const openUserDialog = async (row, asView) => {
  resetForm()
  viewOnly.value = asView || !canManage(row)
  dialogTitle.value = viewOnly.value ? '用户详情' : '编辑用户'
  try {
    const res = await getUserDetail(row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      // 加载员工信息（字段与账号同在 formData）
      if (res.data.empId) {
        formData.gender = res.data.gender ?? 1
        formData.phone = res.data.phone || ''
        formData.email = res.data.email || ''
        formData.title = res.data.title || ''
        formData.education = res.data.education || ''
        formData.specialty = res.data.specialty || ''
        formData.birthDate = res.data.birthDate || ''
        formData.idCard = res.data.idCard || ''
        formData.position = res.data.position || ''
      }
      // 岗位（角色 × 科室）成对回显；roleCodes/deptIds 只是后端从岗位派生的只读值，不再回填
      postRows.value = postsFromApi(res.data.posts)
    } else {
      ElMessage.error(res.message || '获取用户信息失败')
      return
    }
  } catch (error) {
    ElMessage.error(error.message || '获取用户信息失败')
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

  // 院内用户：岗位必须成对（角色 + 科室），且不能重复 —— 后端同样会拒，先在前端拦下来少一次往返
  if (isInternal.value) {
    const postError = checkPosts(postRows.value, {roleNameOf, deptNameOf})
    if (postError) {
      ElMessage.warning(postError)
      return
    }
  }

  submitLoading.value = true
  try {
    // 组装提交数据（员工字段已并入 formData，payload 形状不变）
    const submitData = {...formData}
    if (!isInternal.value) {
      // 非院内用户不渲染员工字段，也不该把它们带着默认值送进后端
      delete submitData.gender
      delete submitData.phone
      delete submitData.email
      delete submitData.title
      delete submitData.specialty
      delete submitData.education
      delete submitData.birthDate
      delete submitData.idCard
      delete submitData.position
    } else {
      submitData.posts = postsToPayload(postRows.value)
    }

    let res
    if (formData.id) {
      res = await updateUser(submitData)
    } else {
      res = await createUser(submitData)
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
  if (row.userName === 'admin') {
    ElMessage.warning('管理员不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.userName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteUser(row.id)
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

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${row.userName}" 的密码吗？`, '重置密码', {
      type: 'warning',
    })
    const res = await resetPassword(row.id)
    if (res.code === 200) {
      ElMessage.success('密码已重置')
    }
  } catch {
    // 用户取消
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  loadData()
}
</script>

<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔开；页面自身不再加内边距（直接用外层布局的 24px），
         面板高随内容自适应；表格限最大高、超高内部滚动，分页在流内紧跟表格底 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input
                v-model="searchForm.userName"
                placeholder="请输入用户名"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px">
              <el-option label="启用" :value="1"/>
              <el-option label="禁用" :value="0"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'system:user:add'" type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡：数据列 min-width 摊满卡片宽度；body 置 0 内边距让表格全幅贴边 -->
    <el-card class="table-card" shadow="never">
      <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          :max-height="tableMaxHeight"
          :row-style="{ cursor: 'pointer' }"
          @row-click="handleRowClick"
      >
        <el-table-column prop="userName" label="用户名" min-width="120"/>
        <el-table-column prop="realName" label="姓名" min-width="100"/>
        <el-table-column prop="userType" label="用户类型" min-width="110">
          <template #default="{ row }">
            <el-tag
                :type="row.userType === 1 ? 'primary' : row.userType === 2 ? 'success' : row.userType === 3 ? 'warning' : 'info'">
              {{
                row.userType === 1 ? '院内用户' : row.userType === 2 ? '院外用户' : row.userType === 3 ? '患者' : '其他'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="180">
          <template #default="{ row }">
            {{ row.lastLoginTime }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginIp" label="最后登录IP" min-width="130">
          <template #default="{ row }">
            {{ row.lastLoginIp }}
          </template>
        </el-table-column>
        <el-table-column prop="loginCount" label="登录次数" min-width="90" align="center">
          <template #default="{ row }">
            {{ row.loginCount }}
          </template>
        </el-table-column>
        <el-table-column prop="passwordUpdateTime" label="密码更新时间" min-width="180">
          <template #default="{ row }">
            {{ row.passwordUpdateTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status == 1 ? 'success' : 'danger'" size="small">
              {{ getDictLabel(USER_STATUS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180"/>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEdit(row)" v-perm="'system:user:edit'" type="primary" link :icon="Edit"
                       @click.stop="handleEdit(row)">编辑
            </el-button>
            <el-button v-if="canManage(row)" v-perm="'system:user:edit'" type="warning" link
                       @click.stop="handleResetPassword(row)">重置密码
            </el-button>
            <el-button v-if="row.userName !== 'admin' && canManage(row)" v-perm="'system:user:delete'" type="danger"
                       link :icon="Delete" @click.stop="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：在流内紧跟表格底，表格多高它就贴在哪，不钉面板底 -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框：单 form，三段上下排布（用户信息 → 员工信息 → 岗位），段内字段两列 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="980px"
        top="5vh"
        @close="dialogVisible = false"
    >
      <div class="max-h-[calc(85vh-120px)] overflow-y-auto pr-1">
        <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            :disabled="viewOnly"
            label-width="90px"
        >
          <h4 class="mb-3 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
            <span class="h-3.5 w-1 rounded bg-blue-500"></span>用户信息
          </h4>
          <div class="grid grid-cols-2 gap-x-4">
            <el-form-item label="用户类型" prop="userType">
              <el-radio-group v-model="formData.userType" :disabled="!!formData.id">
                <el-radio :value="1">院内用户</el-radio>
                <el-radio :value="2">院外用户</el-radio>
                <el-radio :value="3">患者</el-radio>
                <el-radio :value="4">其他</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <div class="grid grid-cols-3 gap-x-4">
            <el-form-item label="工号" prop="empNo">
              <el-input v-model="formData.empNo" placeholder="工号由系统自动生成" disabled/>
            </el-form-item>
            <el-form-item label="用户名" prop="userName">
              <el-input
                  v-model="formData.userName"
                  placeholder="请输入用户名"
                  :disabled="!!formData.id"
              />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名"/>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status" :disabled="formData.userName === 'admin'">
                <el-radio
                    v-for="item in USER_STATUS"
                    :key="item.value"
                    :value="item.value"
                >
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <template v-if="isInternal">
            <h4 class="mb-3 mt-5 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
              <span class="h-3.5 w-1 rounded bg-blue-500"></span>员工信息
            </h4>
            <div class="grid grid-cols-3 gap-x-4">
              <el-form-item label="性别" prop="gender" required>
                <el-radio-group v-model="formData.gender">
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="出生日期" prop="birthDate" required>
                <el-date-picker
                    v-model="formData.birthDate"
                    type="date"
                    placeholder="请选择出生日期"
                    value-format="YYYY-MM-DD"
                    class="w-full"
                />
              </el-form-item>
              <el-form-item label="身份证号" prop="idCard" required>
                <el-input v-model="formData.idCard" placeholder="请输入身份证号"/>
              </el-form-item>
            </div>
            <div class="grid grid-cols-3 gap-x-4">
              <el-form-item label="联系电话" prop="phone" required>
                <el-input v-model="formData.phone" placeholder="请输入手机号"/>
              </el-form-item>
              <el-form-item label="邮箱" prop="email" required>
                <el-input v-model="formData.email" placeholder="请输入邮箱"/>
              </el-form-item>
              <el-form-item label="职称" prop="title" required>
                <el-select v-model="formData.title" placeholder="请选择职称" clearable filterable class="w-full">
                  <el-option
                      v-for="item in titleOptions"
                      :key="item.dictValue"
                      :label="item.dictLabel"
                      :value="item.dictValue"
                  />
                </el-select>
              </el-form-item>
            </div>
            <div class="grid grid-cols-3 gap-x-4">
              <el-form-item label="学历" prop="education" required>
                <el-select v-model="formData.education" placeholder="请选择学历" clearable class="w-full">
                  <el-option
                      v-for="item in educationOptions"
                      :key="item.dictValue"
                      :label="item.dictLabel"
                      :value="item.dictValue"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="职位">
                <el-select v-model="formData.position" placeholder="请选择职位" clearable filterable class="w-full">
                  <el-option
                      v-for="item in positionOptions"
                      :key="item.dictValue"
                      :label="item.dictLabel"
                      :value="item.dictValue"
                  />
                </el-select>
              </el-form-item>
            </div>
            <el-form-item label="专业特长">
              <el-input type="textarea" v-model="formData.specialty" :rows="2" placeholder="请输入专业特长"/>
            </el-form-item>
          </template>
        </el-form>

        <template v-if="isInternal">
          <h4 class="mb-3 mt-5 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
            <span class="h-3.5 w-1 rounded bg-blue-500"></span>岗位
          </h4>
          <EmployeePostTable
              v-model="postRows"
              :dept-list="deptList"
              :role-list="roleList"
              :disabled="viewOnly"
          />
        </template>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ viewOnly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!viewOnly" v-perm="['system:user:add','system:user:edit']" type="primary"
                   :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
