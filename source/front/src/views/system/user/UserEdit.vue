<template>
  <!-- 新增/编辑/详情同一张 form，三段上下排布（用户信息 → 员工信息 → 岗位），段内字段两列 -->
  <el-dialog
      v-model="visible"
      :title="dialogTitle"
      top="5vh"
      width="980px"
  >
    <div class="max-h-[calc(85vh-120px)] overflow-y-auto pr-1">
      <el-form
          ref="formRef"
          :disabled="viewOnly"
          :model="formData"
          :rules="formRules"
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
            <el-input v-model="formData.empNo" disabled placeholder="工号由系统自动生成"/>
          </el-form-item>
          <el-form-item label="用户名" prop="userName">
            <el-input
                v-model="formData.userName"
                :disabled="!!formData.id"
                placeholder="请输入用户名"
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
                  class="w-full"
                  placeholder="请选择出生日期"
                  type="date"
                  value-format="YYYY-MM-DD"
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
              <el-select v-model="formData.title" class="w-full" clearable filterable placeholder="请选择职称">
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
              <el-select v-model="formData.education" class="w-full" clearable placeholder="请选择学历">
                <el-option
                    v-for="item in educationOptions"
                    :key="item.dictValue"
                    :label="item.dictLabel"
                    :value="item.dictValue"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="职位">
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
          <el-form-item label="专业特长">
            <el-input v-model="formData.specialty" :rows="2" placeholder="请输入专业特长" type="textarea"/>
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
            :disabled="viewOnly"
            :role-list="roleList"
        />
      </template>
    </div>
    <template #footer>
      <el-button v-if="!viewOnly" v-perm="['system:user:add','system:user:edit']" :loading="submitLoading"
                 type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">{{ viewOnly ? '关闭' : '取消' }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {
  createUser,
  getDepartmentTree,
  getDictDataMapList,
  getRoleList,
  getUserDetail,
  updateUser
} from '@/api/system'
import {USER_STATUS} from '@/lib/dict'
import EmployeePostTable from '@/components/his/EmployeePostTable.vue'
import {checkPosts, postsFromApi, postsToPayload} from '@/lib/employeePost'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // row 为空 = 新增；有值 = 编辑/详情（详情由 viewOnly 决定，两者共用这一张表单）
  row: {type: Object, default: null},
  viewOnly: {type: Boolean, default: false},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const dialogTitle = computed(() => {
  if (props.viewOnly) return '用户详情'
  return formData.id ? '编辑用户' : '新增用户'
})

const submitLoading = ref(false)
const formRef = ref(null)

// 科室树/角色/职称字典只服务这张表单（列表列不读它们），随表单在本组件加载一次
const deptList = ref([])
const roleList = ref([])
const titleOptions = ref([])  // 职称
const positionOptions = ref([])  // 职位
const educationOptions = ref([]) // 学历
let refLoaded = false

const loadRefs = async () => {
  if (refLoaded) return
  refLoaded = true
  try {
    const [deptRes, roleRes, dictRes] = await Promise.all([
      getDepartmentTree(),
      getRoleList({}),
      getDictDataMapList('sys_hospital_title,hospital_position,his_education'),
    ])
    if (deptRes.code === 200) deptList.value = deptRes.data || []
    if (roleRes.code === 200) roleList.value = roleRes.data || []
    if (dictRes.code === 200 && dictRes.data) {
      titleOptions.value = dictRes.data['sys_hospital_title'] || []
      positionOptions.value = dictRes.data['hospital_position'] || []
      educationOptions.value = dictRes.data['his_education'] || []
    }
  } catch (error) {
    refLoaded = false
    console.error('加载用户表单参照数据失败', error)
  }
}

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

// 岗位校验报错要写成名字，所以科室/角色反查留在表单里（科室是树，需递归）
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

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  await loadRefs()
  if (!props.row) return
  try {
    const res = await getUserDetail(props.row.id)
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
      visible.value = false
      emit('saved')
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false
  }
}
</script>
