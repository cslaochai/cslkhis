<template>
  <div data-testid="duty-log-panel">
    <!-- 待我签收：接班人一进页面就要看到昨夜压在他头上的事，不能靠自己翻列表撞运气 -->
    <el-alert
        v-if="pendingList.length > 0"
        :closable="false"
        data-testid="duty-log-pending"
        show-icon
        style="margin-bottom: 12px"
        type="warning"
    >
      <template #title>
        <span>你有 {{ pendingList.length }} 条交班遗留事项待签收：</span>
        <span v-for="p in pendingList" :key="p.id" style="margin-left: 8px">
          {{ p.dutyDate }} {{ p.shiftTypeText }} · {{ p.title }}
        </span>
      </template>
    </el-alert>

    <el-card class="mb-4" shadow="never">
      <el-form :model="searchForm" inline>
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
        <el-form-item label="类型">
          <el-select v-model="searchForm.logType" clearable placeholder="全部" style="width: 130px">
            <el-option v-for="t in LOG_TYPES" :key="t.value" :label="t.label" :value="t.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="s in STATUSES" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button
              v-perm="'org:duty:log:edit'"
              :icon="Plus"
              data-testid="duty-log-add"
              type="primary"
              @click="handleAdd"
          >
            登记日志
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span class="font-medium">值班日志（值班事件 / 遗留事项 / 巡查记录）</span>
      </template>

      <div data-testid="duty-log-table">
        <el-table v-loading="loading" :data="tableData" stripe>
          <el-table-column label="值班日期" prop="dutyDate" width="120"/>
          <el-table-column label="班次" prop="shiftTypeText" width="80"/>
          <el-table-column label="值班人" prop="employeeName" width="110"/>
          <el-table-column label="类型" prop="logTypeText" width="100">
            <template #default="{ row }">
              <el-tag :type="row.logType === 2 ? 'warning' : row.logType === 3 ? 'info' : 'primary'" size="small">
                {{ row.logTypeText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题" min-width="240" prop="title" show-overflow-tooltip/>
          <el-table-column label="发生时间" prop="happenTime" width="160"/>
          <el-table-column label="状态" prop="statusText" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="接班人" prop="handoverEmpName" width="110"/>
          <el-table-column label="签收时间" prop="ackTime" width="160"/>
          <el-table-column fixed="right" label="操作" width="260">
            <template #default="{ row }">
              <el-button
                  v-if="row.status === 0 || row.status === 1"
                  v-perm="'org:duty:log:handover'"
                  data-testid="duty-log-handover-btn"
                  link
                  type="warning"
                  @click="openHandover(row)"
              >
                交班
              </el-button>
              <el-button
                  v-if="row.canAck === 1"
                  v-perm="'org:duty:log:handover'"
                  data-testid="duty-log-ack-btn"
                  link
                  type="success"
                  @click="handleAck(row)"
              >
                签收
              </el-button>
              <el-button
                  v-if="row.status === 0 || row.status === 1"
                  v-perm="'org:duty:log:edit'"
                  :icon="Edit"
                  link
                  type="primary"
                  @click="handleEdit(row)"
              >
                修改
              </el-button>
              <el-button
                  v-if="row.status !== 3"
                  v-perm="'org:duty:log:delete'"
                  :icon="Delete"
                  link
                  type="danger"
                  @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="mt-4 flex justify-end">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-alert
          :closable="false"
          show-icon
          style="margin-bottom: 12px"
          title="值班人留空 = 记在当前总值班头上；代记/补记请选择值班人，记录人会单独留痕"
          type="info"
      />
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
        <el-form-item label="值班日期" prop="dutyDate">
          <div data-testid="duty-log-date">
            <el-date-picker
                v-model="formData.dutyDate"
                placeholder="夜班请填开始日"
                style="width: 100%"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </div>
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-radio-group v-model="formData.shiftType">
            <el-radio :value="1">白班</el-radio>
            <el-radio :value="2">夜班</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="记录类型" prop="logType">
          <el-radio-group v-model="formData.logType">
            <el-radio :value="1">值班事件</el-radio>
            <el-radio :value="2">遗留事项</el-radio>
            <el-radio :value="3">巡查记录</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="值班人">
          <el-select
              v-model="formData.employeeId"
              :loading="empLoading"
              :remote-method="searchEmployee"
              clearable
              filterable
              placeholder="留空 = 当前总值班"
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
        </el-form-item>
        <el-form-item label="发生时间">
          <div data-testid="duty-log-happen">
            <el-date-picker
                v-model="formData.happenTime"
                placeholder="留空 = 当前时间"
                style="width: 100%"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <div data-testid="duty-log-title">
            <el-input v-model="formData.title" placeholder="一句话说清是什么事"/>
          </div>
        </el-form-item>
        <el-form-item label="事件经过">
          <el-input v-model="formData.content" :rows="3" placeholder="时间、地点、涉及科室与人员" type="textarea"/>
        </el-form-item>
        <el-form-item label="处理情况">
          <el-input
              v-model="formData.handleResult"
              :rows="2"
              placeholder="选「已处理」时必填；遗留事项可交班后再由接班人补"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">待处理</el-radio>
            <el-radio :value="1">已处理</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 交班 -->
    <el-dialog v-model="handoverVisible" title="交班（交给下一班总值班）" width="560px">
      <el-alert
          :closable="false"
          show-icon
          style="margin-bottom: 12px"
          title="接班人留空 = 下一班的总值班（白班→同日夜班，夜班→次日白班）；交接后由接班人签收才算闭环"
          type="warning"
      />
      <el-form ref="handoverRef" :model="handoverForm" label-width="110px">
        <el-form-item label="接班人">
          <div data-testid="duty-log-handover-emp">
            <el-select
                v-model="handoverForm.handoverEmpId"
                :loading="empLoading"
                :remote-method="searchEmployee"
                clearable
                filterable
                placeholder="留空 = 下一班总值班"
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
        <el-form-item label="留给接班人">
          <el-input v-model="handoverForm.handleResult" :rows="3" placeholder="已做了什么、还差什么、联系谁"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handoverVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleHandover">确定交班</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {
  deleteDutyLog,
  dutyLogAck,
  dutyLogHandover,
  dutyLogUpsert,
  getDutyLogListPage,
  getDutyLogPendingMine,
} from '@/api/dutyRoster'
import {getEmployeeList} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'

const LOG_TYPES = [
  {value: 1, label: '值班事件'},
  {value: 2, label: '遗留事项'},
  {value: 3, label: '巡查记录'},
]
const STATUSES = [
  {value: 0, label: '待处理'},
  {value: 1, label: '已处理'},
  {value: 2, label: '已交班'},
  {value: 3, label: '已签收'},
]
const SHIFTS = [
  {value: 1, label: '白班 08:00~18:00'},
  {value: 2, label: '夜班 18:00~次日 08:00'},
]

const statusTagType = (s) => (s === 3 ? 'success' : s === 2 ? 'warning' : s === 1 ? 'info' : 'danger')

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const pendingList = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const searchForm = ref({beginDate: null, endDate: null, shiftType: null, logType: null, status: null})

const dialogVisible = ref(false)
const dialogTitle = ref('登记值班日志')
const formRef = ref(null)
const formData = reactive({
  id: null,
  dutyDate: null,
  shiftType: 1,
  employeeId: null,
  logType: 1,
  happenTime: null,
  title: '',
  content: '',
  handleResult: '',
  status: 0,
  remark: '',
})
const rules = {
  dutyDate: [{required: true, message: '请选择值班日期', trigger: 'change'}],
  shiftType: [{required: true, message: '请选择班次', trigger: 'change'}],
  logType: [{required: true, message: '请选择记录类型', trigger: 'change'}],
  title: [{required: true, message: '请填写标题', trigger: 'blur'}],
}

// 交班
const handoverVisible = ref(false)
const handoverRef = ref(null)
const handoverForm = reactive({id: null, handoverEmpId: null, handleResult: ''})

// 员工下拉（远程搜索，同排班页口径：全院员工上千人，一次拉全量会卡死下拉）
const empOptions = ref([])
const empLoading = ref(false)
const searchEmployee = async (keyword) => {
  empLoading.value = true
  try {
    const res = await getEmployeeList({empName: keyword || '', pageSize: 50})
    empOptions.value = (res?.data || []).map((e) => ({
      id: e.id, empName: e.empName, deptName: e.deptName, phone: e.phone,
    }))
  } catch {
    empOptions.value = []
  } finally {
    empLoading.value = false
  }
}

const loadPending = async () => {
  try {
    const res = await getDutyLogPendingMine()
    if (res.code === 200) pendingList.value = res.data || []
  } catch {
    pendingList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDutyLogListPage({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载值班日志失败')
  } finally {
    loading.value = false
  }
}

const refreshAll = async () => {
  await Promise.all([loadData(), loadPending()])
}

onMounted(() => {
  searchEmployee('')
  refreshAll()
})

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}
const handleReset = () => {
  searchForm.value = {beginDate: null, endDate: null, shiftType: null, logType: null, status: null}
  handleSearch()
}

const resetForm = () => {
  formData.id = null
  formData.dutyDate = null
  formData.shiftType = 1
  formData.employeeId = null
  formData.logType = 1
  formData.happenTime = null
  formData.title = ''
  formData.content = ''
  formData.handleResult = ''
  formData.status = 0
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '登记值班日志'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改值班日志'
  formData.id = row.id
  formData.dutyDate = row.dutyDate
  formData.shiftType = row.shiftType ?? 1
  formData.employeeId = row.employeeId
  formData.logType = row.logType ?? 1
  formData.happenTime = row.happenTime || null
  formData.title = row.title || ''
  formData.content = row.content || ''
  formData.handleResult = row.handleResult || ''
  formData.status = row.status === 1 ? 1 : 0
  formData.remark = row.remark || ''
  // 值班人可能不在当前候选里（搜索过别的字），补进去保证回显是姓名而不是ID
  if (row.employeeId && !empOptions.value.some((e) => e.id === row.employeeId)) {
    empOptions.value = [{id: row.employeeId, empName: row.employeeName, deptName: '', phone: ''}, ...empOptions.value]
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
    const res = await dutyLogUpsert({...formData})
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '登记成功')
      dialogVisible.value = false
      refreshAll()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const openHandover = (row) => {
  handoverForm.id = row.id
  handoverForm.handoverEmpId = null
  handoverForm.handleResult = row.handleResult || ''
  handoverVisible.value = true
}

const handleHandover = async () => {
  submitLoading.value = true
  try {
    const res = await dutyLogHandover({...handoverForm})
    if (res.code === 200) {
      ElMessage.success(res.message || '已交班')
      handoverVisible.value = false
      refreshAll()
    } else {
      ElMessage.error(res.message || '交班失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '交班失败')
  } finally {
    submitLoading.value = false
  }
}

const handleAck = async (row) => {
  try {
    await ElMessageBox.confirm(`确认接收「${row.title}」并跟进吗？`, '交班签收', {type: 'warning'})
    const res = await dutyLogAck(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '已签收')
      refreshAll()
    } else {
      ElMessage.error(res.message || '签收失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e.message) ElMessage.error(e.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除值班日志', {type: 'warning'})
    const res = await deleteDutyLog(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      refreshAll()
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
