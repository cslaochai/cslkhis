<script setup lang="js">
import {ref, onMounted, reactive} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search, Edit, Delete, Refresh, Check} from '@element-plus/icons-vue'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import EmployeePostTable from '@/components/his/EmployeePostTable.vue'
import {postsFromApi, postsToPayload, checkPosts} from '@/lib/employeePost'
import {
  getEmployeeListPage,
  getEmployeeDetail,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  getEmployeeQualificationList,
  upsertEmployeeQualification,
  deleteEmployeeQualification,
  getDepartmentTree,
  getRoleList
} from '@/api/system'
import {loadDictDataMap, DICT_TYPE} from '@/lib/dict-cache'
import {isIdCardFormatLegal, isIdCardBirthDateLegal} from '@/lib/patientGender'

const loading = ref(false)
const searchForm = ref({
  empName: '',
})
const tableData = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

// 部门列表
const deptList = ref([])

// 字典数据
const empTypeOptions = ref([])  // 员工类型
const titleOptions = ref([])  // 职称
const positionOptions = ref([])  // 职位
const certTypeOptions = ref([])  // 资格证书类型
const certOrgOptions = ref([])   // 证书发证机关
const educationOptions = ref([]) // 学历

// 资格证书（员工编辑弹框内的内嵌表格，随编辑弹框打开而加载）：行内编辑，不再走嵌套弹框
const certRows = ref([])
// 加载时的原始快照：提交时与行内容比对，只把真正改过的行发给后端
const certSnapshot = ref([])

// 角色列表（岗位表的角色下拉用）
const roleList = ref([])
// 岗位（角色 × 科室）表单行：一行 = 这个人在该科室以该角色执业
const postRows = ref([])

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  empCode: '',
  empName: '',
  empType: null,
  hireDate: '',
  position: '',
  title: '',
  education: '',
  idCard: '',
  phone: '',
  email: '',
  status: 1,
})

const rules = {
  empName: [
    {required: true, message: '请输入员工姓名', trigger: 'blur'},
  ],
  phone: [
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur'},
  ],
  idCard: [
    // 选填：填了就必须是 18 位合法号（只验格式+出生日期，不验校验位 —— 存量档案里编的号校验位多不成立，
    // 验校验位会让老员工连改个电话都存不了。口径与 lib/patientGender 一致）
    {
      validator: (_rule, value, callback) => {
        const v = (value || '').trim()
        if (!v) return callback()
        if (!isIdCardFormatLegal(v)) return callback(new Error('请输入 18 位身份证号（末位可为 X）'))
        if (!isIdCardBirthDateLegal(v)) return callback(new Error('身份证号中的出生日期不存在'))
        callback()
      },
      trigger: 'blur',
    },
  ],
}

onMounted(() => {
  loadData()
  loadDeptList()
  loadDictData()
  loadRoleList()
})

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

const loadDictData = async () => {
  try {
    // 职称/职位的候选值全部来自后端 sys_dict_data，页面只提交 dictValue（编码），
    // 中文文案一个字都不写死在前端 —— 字典改了下拉立刻跟着变，不用发版。
    // 存量中文已由 sql/174 全量迁成码值，所以「存进去的」和「选出来的」是同一套值。
    const map = await loadDictDataMap([
      'emp_type', DICT_TYPE.SYS_HOSPITAL_TITLE, DICT_TYPE.HOSPITAL_POSITION,
      'his_emp_cert_type', 'his_emp_cert_org', 'his_education',
    ].join(','))
    empTypeOptions.value = map['emp_type'] || []
    titleOptions.value = map[DICT_TYPE.SYS_HOSPITAL_TITLE] || []
    positionOptions.value = map[DICT_TYPE.HOSPITAL_POSITION] || []
    certTypeOptions.value = map['his_emp_cert_type'] || []
    certOrgOptions.value = map['his_emp_cert_org'] || []
    educationOptions.value = map['his_education'] || []
  } catch (error) {
    console.error('加载字典数据失败', error)
  }
}

// 获取字典标签
const getDictLabelByValue = (options, value) => {
  if (!value) return '-'
  const item = options.find(item => item.dictValue === value)
  return item ? item.dictLabel : value
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEmployeeListPage({
      ...searchForm.value,
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
  searchForm.value.empName = ''
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.empCode = ''
  formData.empName = ''
  formData.empType = null
  formData.hireDate = ''
  formData.position = ''
  formData.title = ''
  formData.education = ''
  formData.idCard = ''
  formData.phone = ''
  formData.email = ''
  formData.status = 1
  postRows.value = []
  certRows.value = []
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增员工'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑员工'
  try {
    const res = await getEmployeeDetail(row.id)
    if (res.code === 200 && res.data) {
      const data = res.data
      // 只回填表单字段：deptIds / deptName / roleCodes 是后端从岗位派生的只读值，
      // 跟着提交回去就等于前端反过来定义授权（改造前正是这样配的，两边会打架）
      formData.id = data.id
      formData.empCode = data.empCode || ''
      formData.empName = data.empName || ''
      formData.empType = data.empType ?? null
      formData.hireDate = data.hireDate || ''
      formData.position = data.position || ''
      formData.title = data.title || ''
      formData.education = data.education || ''
      // 详情接口不脱敏（列表接口才脱敏），所以这里是明文，原样回写不会把 **** 存进库
      formData.idCard = data.idCard || ''
      formData.phone = data.phone || ''
      formData.email = data.email || ''
      formData.status = data.status ?? 1
      postRows.value = postsFromApi(data.posts)
      loadCertRows(data.id)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取员工信息失败')
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

  // 岗位必须成对（角色 + 科室）且不能重复；后端同样会拒，前端先拦一次少一趟往返
  const postError = checkPosts(postRows.value, {roleNameOf, deptNameOf})
  if (postError) {
    ElMessage.warning(postError)
    return
  }

  // 证书必填只在这里拦（行内没有保存按钮，校验时机=点确定）
  if (formData.id) {
    const certError = checkCerts()
    if (certError) {
      ElMessage.warning(certError)
      return
    }
  }

  submitLoading.value = true
  try {
    // 组装提交数据（角色与科室不再分开传，只有岗位这一份口径）
    const submitData = {
      ...formData,
      posts: postsToPayload(postRows.value),
    }

    let res
    if (formData.id) {
      res = await updateEmployee(submitData)
      if (res.code === 200) {
        // 员工改成功后证书统一落库：草稿行新增、改过的更新，没动的不发
        await flushCertRows()
      }
    } else {
      res = await createEmployee(submitData)
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
  try {
    await ElMessageBox.confirm(`确定要删除员工 "${row.empName}" 吗？`, '删除确认', {
      type: 'warning',
    })
    const res = await deleteEmployee(row.id)
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

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  loadData()
}

// ===== 资格证书：独立内嵌资源，编辑弹框里即时增删改（不像岗位那样随主表单整体提交） =====
const loadCertRows = async (employeeId) => {
  try {
    const res = await getEmployeeQualificationList({employeeId})
    if (res.code === 200) {
      certRows.value = res.data || []
      certSnapshot.value = (res.data || []).map((row) => ({...row}))
    }
  } catch (error) {
    ElMessage.error(error.message || '加载资格证书失败')
  }
}

// 到期状态按 validUntil 现算（库里不存冗余状态列）：NULL=长期有效，过期红标，90 天内黄标
const certExpiry = (row) => {
  if (!row.validUntil) return {label: '长期有效', type: 'info'}
  const today = new Date().toLocaleDateString('sv-SE') // 固定输出 YYYY-MM-DD
  if (String(row.validUntil) < today) return {label: '已到期', type: 'danger'}
  const days = Math.ceil((new Date(row.validUntil) - new Date(today)) / 86400000)
  if (days <= 90) return {label: `${days}天后到期`, type: 'warning'}
  return null
}

// 行内新增：往表头插一条草稿行（__new），随主表单「确定」统一提交，所以操作列只有删除
const addCertRow = () => {
  certRows.value.unshift({
    id: null, certType: '', certNo: '', issueOrg: '', issueDate: '', validUntil: '', remark: '', __new: true,
  })
}

// 编辑弹框只对已保存员工开放（新增流程无 employeeId，证书表隐藏），
// 提交前逐条拦必填：证书类型/证书编号
const checkCerts = () => {
  for (let i = 0; i < certRows.value.length; i++) {
    const row = certRows.value[i]
    if (!row.certType) return `第 ${i + 1} 条证书：请选择证书类型`
    if (!String(row.certNo || '').trim()) return `第 ${i + 1} 条证书：请输入证书编号`
  }
  return null
}

// 统一落库：草稿行新增、字段变过的行更新（与加载时快照比），没动的不发请求
const flushCertRows = async () => {
  const dirty = certRows.value.filter((row) => {
    if (row.__new) return true
    const orig = certSnapshot.value.find((o) => String(o.id) === String(row.id))
    if (!orig) return true
    return ['certType', 'certNo', 'issueOrg', 'issueDate', 'validUntil', 'remark'].some((k) => (orig[k] || '') !== (row[k] || ''))
  })
  for (const row of dirty) {
    const res = await upsertEmployeeQualification({
      id: row.id || undefined,
      employeeId: formData.id,
      certType: row.certType,
      certNo: row.certNo,
      issueOrg: row.issueOrg || null,
      issueDate: row.issueDate || null,
      validUntil: row.validUntil || null,
      remark: row.remark || null,
    })
    if (res.code !== 200) throw new Error(res.message || '证书保存失败')
  }
}

const handleDeleteCert = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除「${getDictLabelByValue(certTypeOptions.value, row.certType)} ${row.certNo}」吗？`, '删除确认',
        {type: 'warning'},
    )
    const res = await deleteEmployeeQualification(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadCertRows(formData.id)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 岗位校验的报错要写成名字（科室是树，需递归；deptId 两侧类型不一律字符串比较）
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
</script>

<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="员工姓名">
            <el-input
                v-model="searchForm.empName"
                placeholder="请输入员工姓名"
                clearable
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:employee:add'" type="primary" :icon="Plus" @click="handleAdd">新增员工</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="empCode" label="工号" width="100"/>
        <el-table-column prop="empName" label="员工姓名" width="120"/>
        <!-- 列表接口已脱敏（前 4 后 4，中间打星），页面直接渲染，不要再自己打码一次 -->
        <el-table-column prop="idCard" label="身份证号" width="180">
          <template #default="{ row }">
            <span :class="row.idCard ? '' : 'text-slate-300'">{{ row.idCard || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="主科室" min-width="120"/>
        <el-table-column prop="empType" label="员工类型" width="100">
          <template #default="{ row }">
            {{ getDictLabelByValue(empTypeOptions, row.empType) }}
          </template>
        </el-table-column>
        <el-table-column prop="deptNames" label="执业科室" min-width="500">
          <template #default="{ row }">
            {{ (row.deptNames && row.deptNames.length > 0) ? row.deptNames.join('、') : row.deptName }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职位" width="120">
          <template #default="{ row }">
            {{ getDictLabelByValue(positionOptions, row.position) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="120">
          <template #default="{ row }">
            {{ getDictLabelByValue(titleOptions, row.title) }}
          </template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="80">
          <template #default="{ row }">
            {{ getDictLabelByValue(educationOptions, row.education) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140"/>
        <el-table-column prop="email" label="邮箱" min-width="100"/>
        <el-table-column prop="hireDate" label="入职日期" width="120"/>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'org:employee:delete'" type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="980px"
        @close="dialogVisible = false"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
      >
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="工号" prop="empCode">
            <el-input v-model="formData.empCode" placeholder="工号由系统自动生成" disabled/>
          </el-form-item>
          <el-form-item label="员工姓名" prop="empName">
            <el-input v-model="formData.empName" placeholder="请输入员工姓名"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="员工类型" prop="empType">
            <el-select v-model="formData.empType" placeholder="请选择员工类型" clearable class="w-full">
              <el-option
                  v-for="item in empTypeOptions"
                  :key="item.dictValue"
                  :label="item.dictLabel"
                  :value="item.dictValue"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="职位" prop="position">
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
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="职称" prop="title">
            <el-select v-model="formData.title" placeholder="请选择职称" clearable filterable class="w-full">
              <el-option
                  v-for="item in titleOptions"
                  :key="item.dictValue"
                  :label="item.dictLabel"
                  :value="item.dictValue"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学历" prop="education">
            <el-select v-model="formData.education" placeholder="请选择学历" clearable class="w-full">
              <el-option
                  v-for="item in educationOptions"
                  :key="item.dictValue"
                  :label="item.dictLabel"
                  :value="item.dictValue"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="formData.idCard" maxlength="18" placeholder="选填，18 位"/>
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入联系电话"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱"/>
          </el-form-item>
          <el-form-item label="入职日期" prop="entryDate">
            <el-date-picker
                v-model="formData.hireDate"
                type="date"
                placeholder="请选择入职日期"
                value-format="YYYY-MM-DD"
                class="w-full"
            />
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">在职</el-radio>
              <el-radio :value="0">离职</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div class="grid">
          <el-form-item label-width="0">
            <div class="w-full">
              <div class="mb-2 flex items-center gap-1.5">
                <span class="h-3.5 w-1 rounded bg-blue-500"></span>
                <span class="text-sm font-semibold text-slate-700">岗位</span>
              </div>
              <EmployeePostTable v-model="postRows" :dept-list="deptList" :role-list="roleList"/>
            </div>
          </el-form-item>
        </div>
        <!-- 资格证书：独立内嵌资源，即时增删改，不随本表单提交 -->
        <el-form-item label-width="0">
          <div class="w-full">
            <div class="mb-2 flex items-center gap-1.5">
              <span class="h-3.5 w-1 rounded bg-blue-500"></span>
              <span class="text-sm font-semibold text-slate-700">资格证书</span>
              <span v-if="!formData.id"
                    class="ml-2 text-xs text-gray-400">新增员工请先保存，保存后即可登记资格证书</span>
            </div>
            <div v-if="formData.id" class="mb-2 flex items-center gap-3">
              <el-button v-perm="'org:employee:add'" :icon="Plus" plain size="small" @click="addCertRow()">
                新增证书
              </el-button>
              <span v-if="certRows.length > 0" class="text-sm text-slate-500">共 {{ certRows.length }} 本证书</span>
            </div>
            <el-table v-if="formData.id" :data="certRows" size="small" border max-height="260">
              <el-table-column width="130">
                <template #header><span class="text-red-500">*</span> 证书类型</template>
                <template #default="{ row }">
                  <el-select v-model="row.certType" placeholder="请选择证书类型" filterable class="w-full">
                    <el-option
                        v-for="item in certTypeOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column width="220">
                <template #header><span class="text-red-500">*</span> 证书编号</template>
                <template #default="{ row }">
                  <el-input v-model="row.certNo" maxlength="64" placeholder="请输入证书编号"/>
                </template>
              </el-table-column>
              <el-table-column width="150">
                <template #header>发证日期</template>
                <template #default="{ row }">
                  <el-date-picker
                      v-model="row.issueDate"
                      type="date"
                      placeholder="发证日期"
                      value-format="YYYY-MM-DD"
                      class="!w-full"
                  />
                </template>
              </el-table-column>
              <el-table-column width="150">
                <template #header>有效期至</template>
                <template #default="{ row }">
                  <div class="flex flex-col gap-1">
                    <el-date-picker
                        v-model="row.validUntil"
                        type="date"
                        placeholder="长期有效"
                        value-format="YYYY-MM-DD"
                        class="!w-full"
                    />
                    <el-tag v-if="!row.__new && certExpiry(row)" :type="certExpiry(row).type" size="small">
                      {{ certExpiry(row).label }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column width="150">
                <template #header>发证机关</template>
                <template #default="{ row }">
                  <el-select v-model="row.issueOrg" placeholder="请选择发证机关" filterable clearable class="w-full">
                    <el-option
                        v-for="item in certOrgOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="70" align="center">
                <template #default="{ row, $index }">
                  <el-button v-if="row.__new" v-perm="'org:employee:delete'" type="danger" link
                             @click="certRows.splice($index, 1)">删除
                  </el-button>
                  <el-button v-else v-perm="'org:employee:delete'" type="danger" link
                             @click="handleDeleteCert(row)">删除
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <span class="text-sm text-slate-400">还没有登记证书，请点上方「新增证书」</span>
              </template>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'org:employee:add'" type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
