<template>
  <div data-testid="duty-roster-view">
    <DutyOfficerBar class="mb-3"/>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="值班种类">
            <el-radio-group v-model="dutyKind" data-testid="duty-kind" @change="onDutyKindChange">
              <el-radio-button v-for="k in DUTY_KINDS" :key="k.value" :value="k.value">
                {{ k.label }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="值班日期">
            <el-date-picker
                v-model="searchForm.beginDate"
                clearable
                placeholder="开始日期"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="至">
            <el-date-picker
                v-model="searchForm.endDate"
                clearable
                placeholder="结束日期"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="班次">
            <el-select v-model="searchForm.shiftType" clearable placeholder="全部" style="width: 190px">
              <el-option v-for="s in SHIFTS" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.roleType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="r in ROLES" :key="r.value" :label="r.label" :value="r.value"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:duty:edit'" :icon="Plus" data-testid="duty-add" type="primary" @click="handleAdd">
            {{ dutyKind === 'clinical' ? '登记科室值班' : '登记排班' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="duty-table">
        <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
          <el-table-column label="值班日期" prop="dutyDate" width="120"/>
          <el-table-column label="班次" prop="shiftTypeText" width="90"/>
          <el-table-column label="角色" prop="roleTypeText" width="90">
            <template #default="{ row }">
              <el-tag :type="row.roleType === 1 ? 'primary' : 'info'" size="small">{{ row.roleTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="值班点位" min-width="170" prop="postName" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.postName || '—' }}</span>
              <el-tag v-if="row.dutyLevelText && row.dutyLevelText !== '不适用'" size="small" style="margin-left: 4px"
                      type="success">
                {{ row.dutyLevelText }}·{{ row.attendModeText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="排班值班人" prop="employeeName" width="130"/>
          <el-table-column label="实际值班人" prop="actualEmpName" width="150">
            <template #default="{ row }">
              <span>{{ row.actualEmpName }}</span>
              <el-tag v-if="row.substituted === 1" size="small" style="margin-left: 4px" type="warning">
                换班（原 {{ row.employeeName }}）
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="联系电话" prop="actualPhone" width="130"/>
          <el-table-column label="原属科室" min-width="140" prop="deptName"/>
          <el-table-column label="时段" prop="startTime" width="120">
            <template #default="{ row }">{{ row.startTime }}~{{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '有效' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="换班原因" min-width="160" prop="substituteReason" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="240">
            <template #default="{ row }">
              <el-button v-perm="'org:duty:edit'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button v-perm="'org:duty:substitute'" :icon="Switch" link type="warning" @click="openSubstitute(row)">
                换班
              </el-button>
              <el-button
                  v-if="row.substituted === 1"
                  v-perm="'org:duty:substitute'"
                  link
                  type="info"
                  @click="handleCancelSubstitute(row)"
              >
                撤回
              </el-button>
              <el-button v-perm="'org:duty:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

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

    <!-- 登记/修改 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
        <el-form-item label="值班日期" prop="dutyDate">
          <div data-testid="duty-date">
            <el-date-picker
                v-model="formData.dutyDate"
                placeholder="夜班请填开始日"
                style="width: 100%"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </div>
        </el-form-item>
        <el-form-item :label="dutyKind === 'clinical' ? '值班点位' : '点位（留空＝总值班位）'" prop="postId">
          <div data-testid="duty-post">
            <el-select
                v-model="formData.postId"
                clearable
                filterable
                placeholder="选择这个班落在哪个位上"
                style="width: 100%"
                @change="onPostChange"
            >
              <el-option
                  v-for="p in postOptions"
                  :key="p.id"
                  :label="p.dutyLevelText && p.dutyLevelText !== '不适用'
                  ? `${p.postName}（${p.dutyLevelText}·${p.attendModeText}）` : p.postName"
                  :value="p.id"
              />
            </el-select>
          </div>
          <div v-if="currentPost()" class="mt-1 text-xs text-gray-500">
            班次与角色由点位带出：{{ currentPost().shiftName }} {{ currentPost().startTime }}~{{ currentPost().endTime }}
          </div>
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-radio-group v-model="formData.shiftType" :disabled="!!formData.postId">
            <el-radio :value="1">白班 08:00~18:00</el-radio>
            <el-radio :value="2">夜班 18:00~次日 08:00</el-radio>
          </el-radio-group>
          <div v-if="formData.postId" class="text-xs text-gray-400">已选点位，班次按点位走</div>
        </el-form-item>
        <el-form-item label="班内角色" prop="roleType">
          <el-radio-group v-model="formData.roleType" :disabled="!!formData.postId">
            <el-radio :value="1">主班</el-radio>
            <el-radio :value="2">副班（备班）</el-radio>
          </el-radio-group>
          <div v-if="formData.postId" class="text-xs text-gray-400">已选点位，角色按点位走</div>
        </el-form-item>
        <el-form-item label="值班人" prop="employeeId">
          <div data-testid="duty-emp">
            <el-select
                v-model="formData.employeeId"
                :loading="empLoading"
                :remote-method="searchEmployee"
                filterable
                placeholder="输入姓名搜索（只显示在职工）"
                remote
                reserve-keyword
                style="width: 100%"
                @change="onEmpChange"
            >
              <el-option
                  v-for="e in empOptions"
                  :key="e.id"
                  :label="e.deptName ? `${e.empName}（${e.deptName}）` : e.empName"
                  :value="e.id"
              />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="值班电话">
          <el-input v-model="formData.phone" placeholder="留空取员工档案手机"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">有效</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" :rows="2" placeholder="如：本周总值，兼管急诊协调" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 临时换班 -->
    <el-dialog v-model="subDialogVisible" title="临时换班" width="520px">
      <el-form ref="subFormRef" :model="subForm" :rules="subRules" label-width="120px">
        <el-form-item label="换班后值班人" prop="substituteEmpId">
          <div data-testid="duty-sub-emp">
            <el-select
                v-model="subForm.substituteEmpId"
                :loading="empLoading"
                :remote-method="searchEmployee"
                filterable
                placeholder="输入姓名搜索"
                remote
                reserve-keyword
                style="width: 100%"
            >
              <el-option
                  v-for="e in empOptions"
                  :key="e.id"
                  :label="e.deptName ? `${e.empName}（${e.deptName}）` : e.empName"
                  :value="e.id"
              />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="换班后电话">
          <el-input v-model="subForm.phone" placeholder="留空取新员工档案手机"/>
        </el-form-item>
        <el-form-item label="换班原因" prop="substituteReason">
          <div data-testid="duty-sub-reason">
            <el-input v-model="subForm.substituteReason" :rows="2" placeholder="必填：如主班参加抢救无法脱身"
                      type="textarea"/>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subDialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubstitute">确定换班</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
/**
 * 全院总值班排班（菜单 806，sql/169）。
 *
 * 页面存在的意义：急诊升级、床位跨科调配、双向转诊三条链路的兜底收口人
 * 就是这张表里「今天」的那一行。排班空了 = 全院应急协调没人接 ——
 * 所以顶部那张卡必须一眼看到今天是谁，查不到人时显示红色告警而不是假装没有这一栏。
 */
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
// ⚠ @element-plus/icons-vue 没有导出 Swap（写了会导致整个页面模块加载失败、菜单点了白屏）
import {Delete, Edit, Plus, Refresh, Search, Switch} from '@element-plus/icons-vue'
import {
  deleteDutyRoster,
  dutyRosterUpsert,
  dutySubstitute,
  dutySubstituteCancel,
  getDutyRosterListPage,
} from '@/api/dutyRoster'
import {getDutyPostSelectList} from '@/api/dutyPost'
import {getEmployeeList} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import DutyOfficerBar from '@/components/his/DutyOfficerBar.vue'

// 值班日志（交班本）原为本页第二个 Tab，已于 2026-09-28 拆成独立菜单「总值班日志（交班本）」
// （菜单 2920 / 路由 /duty-log，见 sql/176）：排班是院办按周编，交班本当班的人每班都要记与签收，
// 岗位与频率都不同 —— 合成一页会让接班人先开排班页再切页签才能签收。

// 班次与班内角色是封闭枚举（就这两个值，不会新增第三档），与字典 his_duty_shift / his_duty_role
// 同口径、且与后端 DutyRosterService.SHIFT_* / ROLE_* 常量逐字对齐 —— 改任一侧必须同时改另一侧。
// 不接字典缓存：这两档决定了「当前总值班」的解析（夜班跨自然日归开始日），
// 下拉拿不到值会直接导致解析出不来人，写死比"字典没配就静默空"安全。
const SHIFTS = [
  {value: 1, label: '白班 08:00~18:00'},
  {value: 2, label: '夜班 18:00~次日 08:00'},
]
const ROLES = [
  {value: 1, label: '主班'},
  {value: 2, label: '副班（备班）'},
]

const shiftText = (v) => (v === 2 ? '夜班' : '白班')
const roleText = (v) => (v === 1 ? '主班' : '副班')

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const searchForm = ref({beginDate: null, endDate: null, shiftType: null, roleType: null})

// 值班种类：全院总值班（行政位）vs 科室医师值班（sql/202 铺的临床位，39 个科室 × 一线/二线/三线）。
// 两者同居 biz_duty_roster 一张表，靠 post_id 挂的点位分开 —— 页面必须让排班员先选是哪一类，
// 否则「今日全院谁负责」那张表会被 234 个科室值班行冲垮，谁也看不懂。
const DUTY_KINDS = [
  {value: 'admin', label: '全院总值班', scope: 1},
  {value: 'clinical', label: '科室医师值班', scope: 6},
]
const dutyKind = ref('admin')
const kindScope = () => (DUTY_KINDS.find((k) => k.value === dutyKind.value) || DUTY_KINDS[0]).scope

// 点位下拉：按值班种类过滤（总值班只有 4 个行政位，临床有 234 个科室位）
const postOptions = ref([])
const loadPostOptions = async () => {
  try {
    const res = await getDutyPostSelectList({dutyScope: kindScope()})
    postOptions.value = res?.data || []
  } catch {
    postOptions.value = []
  }
}
const onDutyKindChange = () => {
  pagination.value.pageNum = 1
  loadPostOptions()
  loadData()
}
// 选了点位：班次与班内角色都由点位带出（后端以点位为权威），页面上这两个单选就锁住，
// 免得排班员手选一个跟点位打架的值还以为生效了。
const onPostChange = (postId) => {
  const p = postOptions.value.find((x) => String(x.id) === String(postId))
  if (!p) return
  if (p.roleType) formData.roleType = p.roleType
  formData.shiftType = p.shiftName && p.shiftName.includes('夜') ? 2 : 1
  if (!formData.phone && p.phone) formData.phone = p.phone
}
const currentPost = () => postOptions.value.find((x) => String(x.id) === String(formData.postId))

const dialogVisible = ref(false)
const dialogTitle = ref('登记总值班')
const formRef = ref(null)
const formData = reactive({
  id: null,
  postId: null,
  dutyDate: null,
  shiftType: 1,
  roleType: 1,
  employeeId: null,
  phone: '',
  startTime: '',
  endTime: '',
  status: 1,
  remark: '',
})
const rules = {
  dutyDate: [{required: true, message: '请选择值班日期', trigger: 'change'}],
  shiftType: [{required: true, message: '请选择班次', trigger: 'change'}],
  roleType: [{required: true, message: '请选择班内角色', trigger: 'change'}],
  employeeId: [{required: true, message: '请选择值班人', trigger: 'change'}],
  // 科室医师值班必须挑位：不传 postId 后端就按老语义落行政总值班位，
  // 排班员以为排的是本科室一线，实际把全院总值班那行改了。
  postId: [{
    validator: (_r, v, cb) => {
      if (dutyKind.value === 'clinical' && !v) return cb(new Error('科室医师值班必须选择值班点位'))
      cb()
    },
    trigger: 'change',
  }],
}

// 换班
const subDialogVisible = ref(false)
const subFormRef = ref(null)
const subForm = reactive({id: null, substituteEmpId: null, phone: '', substituteReason: ''})
const subRules = {
  substituteEmpId: [{required: true, message: '请选择换班后值班人', trigger: 'change'}],
  substituteReason: [{required: true, message: '请填写换班原因', trigger: 'blur'}],
}

// 员工下拉（远程搜索：全院员工上千人，一次性拉全量会卡死下拉）
const empOptions = ref([])
const empLoading = ref(false)
const searchEmployee = async (keyword) => {
  empLoading.value = true
  try {
    const res = await getEmployeeList({empName: keyword || '', pageSize: 50})
    const list = res?.data || []
    empOptions.value = list.map((e) => ({
      id: e.id,
      empName: e.empName,
      deptName: e.deptName,
      phone: e.phone,
    }))
  } catch (e) {
    empOptions.value = []
  } finally {
    empLoading.value = false
  }
}
const onEmpChange = (id) => {
  const emp = empOptions.value.find((e) => e.id === id)
  // 联系电话留空时后端回落员工档案手机，这里只做提示用，不强制回填
  if (emp && !formData.phone) formData.phone = emp.phone || ''
}

onMounted(() => {
  searchEmployee('')
  loadPostOptions()
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDutyRosterListPage({
      ...searchForm.value,
      // 总值班：只认行政位（含无点位的老数据）；科室医师值班：只认临床位
      ...(dutyKind.value === 'admin' ? {adminOnly: true} : {dutyScope: 6}),
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载排班失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}
const handleReset = () => {
  searchForm.value = {beginDate: null, endDate: null, shiftType: null, roleType: null}
  handleSearch()
}

const resetForm = () => {
  formData.id = null
  formData.postId = null
  formData.dutyDate = null
  formData.shiftType = 1
  formData.roleType = 1
  formData.employeeId = null
  formData.phone = ''
  formData.startTime = ''
  formData.endTime = ''
  formData.status = 1
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = dutyKind.value === 'clinical' ? '登记科室医师值班' : '登记总值班'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = dutyKind.value === 'clinical' ? '修改科室医师值班' : '修改总值班'
  formData.id = row.id
  formData.postId = row.postId || null
  formData.dutyDate = row.dutyDate
  formData.shiftType = row.shiftType
  formData.roleType = row.roleType
  // 点位可能不在当前种类下拉里（从另一类切过来的老数据），补进去保证回显是点位名而不是ID
  if (row.postId && !postOptions.value.some((p) => String(p.id) === String(row.postId))) {
    postOptions.value = [
      {id: row.postId, postName: row.postName, roleType: row.roleType, shiftName: row.shiftTypeText},
      ...postOptions.value,
    ]
  }
  formData.employeeId = row.employeeId
  formData.phone = row.phone || ''
  formData.startTime = row.startTime || ''
  formData.endTime = row.endTime || ''
  formData.status = row.status ?? 1
  formData.remark = row.remark || ''
  // 值班人可能不在当前下拉候选里（搜索过别的字），补进去保证回显是姓名而不是ID
  if (row.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [
      {id: row.employeeId, empName: row.employeeName, deptName: row.deptName, phone: row.phone},
      ...empOptions.value,
    ]
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
    const res = await dutyRosterUpsert({...formData})
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '登记成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const openSubstitute = (row) => {
  subForm.id = row.id
  subForm.substituteEmpId = null
  subForm.phone = ''
  subForm.substituteReason = ''
  if (row.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [
      {id: row.employeeId, empName: row.employeeName, deptName: row.deptName, phone: row.phone},
      ...empOptions.value,
    ]
  }
  subDialogVisible.value = true
}

const handleSubstitute = async () => {
  if (!subFormRef.value) return
  try {
    await subFormRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await dutySubstitute({...subForm})
    if (res.code === 200) {
      ElMessage.success(res.message || '换班成功')
      subDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '换班失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '换班失败')
  } finally {
    submitLoading.value = false
  }
}

const handleCancelSubstitute = async (row) => {
  try {
    await ElMessageBox.confirm(
        `撤回后恢复由 ${row.employeeName} 值班（原排班人），确定吗？`, '撤回换班', {type: 'warning'})
    const res = await dutySubstituteCancel(row.id)
    if (res.code === 200) {
      ElMessage.success('已撤回换班')
      loadData()
    } else {
      ElMessage.error(res.message || '撤回失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e.message) ElMessage.error(e.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除 ${row.dutyDate} ${shiftText(row.shiftType)}${roleText(row.roleType)}（${row.employeeName}）吗？`,
        '删除排班', {type: 'warning'})
    const res = await deleteDutyRoster(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e.message) ElMessage.error(e.message)
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
