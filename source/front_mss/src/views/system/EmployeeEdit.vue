<template>
  <!-- 新增/编辑对话框（后端新增与修改共用同一套岗位+证书落库口径，row 为空走新增） -->
  <el-dialog
      v-model="visible"
      :title="dialogTitle"
      width="980px"
      @close="visible = false"
  >
    <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
    >
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="工号" prop="empCode">
          <el-input v-model="formData.empCode" disabled placeholder="工号由系统自动生成"/>
        </el-form-item>
        <el-form-item label="员工姓名" prop="empName">
          <el-input v-model="formData.empName" placeholder="请输入员工姓名"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="员工类型" prop="empType">
          <el-select v-model="formData.empType" class="w-full" clearable placeholder="请选择员工类型">
            <el-option
                v-for="item in empTypeOptions"
                :key="item.dictValue"
                :label="item.dictLabel"
                :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-select v-model="formData.position" class="w-full" clearable filterable placeholder="请选择职位">
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
          <el-select v-model="formData.title" class="w-full" clearable filterable placeholder="请选择职称">
            <el-option
                v-for="item in titleOptions"
                :key="item.dictValue"
                :label="item.dictLabel"
                :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学历" prop="education">
          <el-select v-model="formData.education" class="w-full" clearable placeholder="请选择学历">
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
              class="w-full"
              placeholder="请选择入职日期"
              type="date"
              value-format="YYYY-MM-DD"
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
      <!-- 资格证书：内嵌表格行内编辑。已保存的行点删除即单条删；新增与改过的行在点「确定」时一次批量提交（不逐条调接口） -->
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
          <el-table v-if="formData.id" :data="certRows" border max-height="260" size="small">
            <el-table-column width="130">
              <template #header><span class="text-red-500">*</span> 证书类型</template>
              <template #default="{ row }">
                <el-select v-model="row.certType" class="w-full" filterable placeholder="请选择证书类型">
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
                    class="!w-full"
                    placeholder="发证日期"
                    type="date"
                    value-format="YYYY-MM-DD"
                />
              </template>
            </el-table-column>
            <el-table-column width="150">
              <template #header>有效期至</template>
              <template #default="{ row }">
                <div class="flex flex-col gap-1">
                  <el-date-picker
                      v-model="row.validUntil"
                      class="!w-full"
                      placeholder="长期有效"
                      type="date"
                      value-format="YYYY-MM-DD"
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
                <el-select v-model="row.issueOrg" class="w-full" clearable filterable placeholder="请选择发证机关">
                  <el-option
                      v-for="item in certOrgOptions"
                      :key="item.dictValue"
                      :label="item.dictLabel"
                      :value="item.dictValue"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column align="center" label="操作" min-width="70">
              <template #default="{ row, $index }">
                <el-button v-if="row.__new" v-perm="'org:employee:delete'" link type="danger"
                           @click="certRows.splice($index, 1)">删除
                </el-button>
                <el-button v-else v-perm="'org:employee:delete'" link type="danger"
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
      <el-button v-perm="'org:employee:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import EmployeePostTable from '@/components/his/EmployeePostTable.vue'
import {checkPosts, postsFromApi, postsToPayload} from '@/lib/employeePost'
import {getDictLabelByValue} from '@/lib/employeeDict'
import {
  createEmployee,
  deleteEmployeeQualification,
  getDepartmentTree,
  getEmployeeDetail,
  getEmployeeQualificationList,
  getRoleList,
  updateEmployee,
  upsertEmployeeQualificationBatch
} from '@/api/system'
import {loadDictDataMap} from '@/lib/dict-cache'
import {isIdCardBirthDateLegal, isIdCardFormatLegal} from '@/lib/patientGender'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 员工类型/职位/职称/学历字典：列表列也要翻译这些码值，由页面加载一次下传，表单不再自己拉一遍
  empTypeOptions: {type: Array, default: () => []},
  positionOptions: {type: Array, default: () => []},
  titleOptions: {type: Array, default: () => []},
  educationOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const dialogTitle = computed(() => (props.row ? '编辑员工' : '新增员工'))

// 只有本表单用的参照数据（科室树、角色名册、证书两张字典）随表单在弹框打开时拉一次
const deptList = ref([])
const roleList = ref([])
const certTypeOptions = ref([])  // 资格证书类型
const certOrgOptions = ref([])   // 证书发证机关
let refLoaded = false

const loadRefs = async () => {
  if (refLoaded) return
  refLoaded = true
  try {
    const [deptRes, roleRes] = await Promise.all([getDepartmentTree(), getRoleList({})])
    if (deptRes.code === 200) deptList.value = deptRes.data || []
    if (roleRes.code === 200) roleList.value = roleRes.data || []
    const map = await loadDictDataMap('his_emp_cert_type,his_emp_cert_org')
    certTypeOptions.value = map['his_emp_cert_type'] || []
    certOrgOptions.value = map['his_emp_cert_org'] || []
  } catch (error) {
    refLoaded = false
    console.error('加载员工表单参照数据失败', error)
  }
}

// 资格证书（编辑弹框内的内嵌表格，随编辑弹框打开而加载）：行内编辑，不再走嵌套弹框
const certRows = ref([])
// 加载时的原始快照：提交时与行内容比对，只把真正改过的行发给后端
const certSnapshot = ref([])

// 岗位（角色 × 科室）表单行：一行 = 这个人在该科室以该角色执业
const postRows = ref([])

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

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  await loadRefs()
  if (!props.row) return
  try {
    const res = await getEmployeeDetail(props.row.id)
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
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
})

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
      visible.value = false
      emit('saved')
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}

// ===== 资格证书：独立内嵌资源，编辑弹框里即时增删改（不像岗位那样随主表单整体提交） =====
const loadCertRows = async (employeeId) => {
  try {
    const res = await getEmployeeQualificationList({employeeId})
    if (res.code === 200) {
      certRows.value = res.data || []
      certSnapshot.value = (res.data || []).map((row) => ({...row}))
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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

// 统一落库：草稿行新增、字段变过的行更新（与加载时快照比），没动的不发请求。
// dirty 一次交给后端批量接口：逐条调单本证书接口时，第 N 条失败前 N-1 条已各自提交，
// 「存了一半的证书」既回滚不掉也说不清该重试哪几条。
const flushCertRows = async () => {
  const dirty = certRows.value.filter((row) => {
    if (row.__new) return true
    const orig = certSnapshot.value.find((o) => String(o.id) === String(row.id))
    if (!orig) return true
    return ['certType', 'certNo', 'issueOrg', 'issueDate', 'validUntil', 'remark'].some((k) => (orig[k] || '') !== (row[k] || ''))
  })
  if (!dirty.length) return
  const res = await upsertEmployeeQualificationBatch({
    employeeId: formData.id,
    items: dirty.map((row) => ({
      id: row.id || undefined,
      employeeId: formData.id,
      certType: row.certType,
      certNo: row.certNo,
      issueOrg: row.issueOrg || null,
      issueDate: row.issueDate || null,
      validUntil: row.validUntil || null,
      remark: row.remark || null,
    })),
  })
  if (res.code !== 200) throw new Error(res.message || '证书保存失败')
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
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
