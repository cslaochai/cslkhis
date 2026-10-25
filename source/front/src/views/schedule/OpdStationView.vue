<template>
  <div data-testid="opd-station-view">
    <el-card class="query-card mb-3" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ===================== 工位字典 ===================== -->
        <el-tab-pane label="公共工位字典" name="station">
          <el-form :model="stationSearch" inline>
            <el-form-item label="工位类别">
              <el-select v-model="stationSearch.stationKind" clearable placeholder="全部" style="width: 140px">
                <el-option v-for="o in STATION_KIND_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="stationSearch.status" clearable placeholder="全部" style="width: 100px">
                <el-option :value="1" label="启用"/>
                <el-option :value="0" label="停用"/>
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="stationSearch.keyword" clearable placeholder="编码/名称" style="width: 150px"/>
            </el-form-item>
            <el-form-item>
              <el-button :icon="Search" type="primary" @click="searchStation">查询</el-button>
              <el-button :icon="Refresh" @click="resetStation">重置</el-button>
            </el-form-item>
            <el-form-item class="ml-auto">
              <el-button v-perm="'opd:station:add'" :icon="Plus" type="primary" @click="handleStationAdd">新增工位</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ===================== 派岗记录 ===================== -->
        <el-tab-pane label="派岗记录" name="assign">
          <el-form :model="assignSearch" inline>
            <el-form-item label="日期">
              <el-date-picker v-model="assignDateRange" type="daterange" range-separator="至"
                              start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD"
                              style="width: 260px"/>
            </el-form-item>
            <el-form-item label="工位">
              <el-select v-model="assignSearch.stationId" clearable filterable placeholder="全部" style="width: 160px">
                <el-option v-for="s in stationOptions" :key="s.id" :label="s.stationName" :value="s.id"/>
              </el-select>
            </el-form-item>
            <el-form-item label="借调">
              <el-select v-model="assignSearch.isLoan" clearable placeholder="全部" style="width: 100px">
                <el-option :value="0" label="本岗"/>
                <el-option :value="1" label="借调"/>
              </el-select>
            </el-form-item>
            <el-form-item label="人员">
              <el-input v-model="assignSearch.keyword" clearable placeholder="姓名/工号" style="width: 130px"/>
            </el-form-item>
            <el-form-item>
              <el-button :icon="Search" type="primary" @click="searchAssign">查询</el-button>
              <el-button :icon="Refresh" @click="resetAssign">重置</el-button>
            </el-form-item>
            <el-form-item class="ml-auto">
              <el-button v-perm="'opd:station:add'" :icon="Plus" type="primary" @click="handleAssignAdd">派岗</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ===================== 工位字典表 ===================== -->
    <el-card v-show="activeTab === 'station'" class="table-card" shadow="never">
      <el-table v-loading="stationLoading" :data="stationData" max-height="560" stripe>
        <el-table-column label="工位编码" prop="stationCode" width="150"/>
        <el-table-column label="工位名称" prop="stationName" min-width="150"/>
        <el-table-column label="类别" prop="stationKindText" width="110"/>
        <el-table-column label="应到岗位" prop="requiredStaffTypeName" width="100"/>
        <el-table-column label="在岗人数" prop="capacity" width="90"/>
        <el-table-column label="电话" prop="phone" width="120"/>
        <el-table-column label="排序" prop="sortNo" width="70"/>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="130" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'opd:station:add'" :icon="Edit" link type="primary" @click="handleStationEdit(row)">修改</el-button>
            <el-button v-perm="'opd:station:delete'" :icon="Delete" link type="danger" @click="handleStationDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="stationPage.pageNum" v-model:page-size="stationPage.pageSize"
                       :page-sizes="PAGE_SIZES" :total="stationPage.total"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadStation" @current-change="loadStation"/>
      </div>
    </el-card>

    <!-- ===================== 派岗记录表 ===================== -->
    <el-card v-show="activeTab === 'assign'" class="table-card" shadow="never">
      <el-table v-loading="assignLoading" :data="assignData" max-height="560" stripe>
        <el-table-column label="日期" prop="assignDate" width="110"/>
        <el-table-column label="工位" prop="stationName" min-width="140"/>
        <el-table-column label="类别" prop="stationKindText" width="100"/>
        <el-table-column label="人员" min-width="130">
          <template #default="{ row }">{{ row.employeeName }}<span class="text-muted">（{{ row.empCode || '—' }}）</span></template>
        </el-table-column>
        <el-table-column label="岗位" prop="staffTypeName" width="90"/>
        <el-table-column label="班次" min-width="170">
          <template #default="{ row }">
            <span v-if="row.shiftName">{{ row.shiftName }} {{ row.startTime }}~{{ row.endTime }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isLoan === 1" type="warning" size="small">借调</el-tag>
            <el-tag v-else type="info" size="small">本岗</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="借出方" prop="sourceOrgName" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.sourceOrgName || '—' }}</template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="100">
          <template #default="{ row }">
            <el-button v-perm="'opd:station:delete'" :icon="Delete" link type="danger" @click="handleAssignDelete(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="assignPage.pageNum" v-model:page-size="assignPage.pageSize"
                       :page-sizes="PAGE_SIZES" :total="assignPage.total"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadAssign" @current-change="loadAssign"/>
      </div>
    </el-card>

    <OpdStationEdit v-model="stationDialog" :row="stationEditRow" @saved="loadStation"/>
    <OpdAssignEdit v-model="assignDialog" :row="assignEditRow" :station-options="stationOptions" @saved="loadAssign"/>
  </div>
</template>

<script lang="js" setup>
/**
 * 门诊工位派岗（sql/277，菜单 2960，权限 opd:station:*）。
 *
 * 补「A 护士今天被排到总导诊/分诊台/输液室/注射室/采血室哪个位」这一小层：
 * 上半是公共工位字典（位先定义存在），下半是排班员按日指定派岗（把人写进位里，isLoan=1 登记借调）。
 * 独立于 biz_schedule 出勤底座，不进工时/缺勤闭环。
 */
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {
  deleteOpdAssign, deleteOpdStation, getOpdAssignPage, getOpdStationPage, getOpdStationSelectList,
} from '@/api/opdStation'
import {STATION_KIND_OPTIONS} from '@/lib/opdStation'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import OpdStationEdit from './OpdStationEdit.vue'
import OpdAssignEdit from './OpdAssignEdit.vue'

const activeTab = ref('station')

// ---------- 工位字典 ----------
const stationLoading = ref(false)
const stationData = ref([])
const stationPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const stationSearch = ref({stationKind: null, status: null, keyword: ''})

const stationDialog = ref(false)
const stationEditRow = ref(null)

const loadStation = async () => {
  stationLoading.value = true
  try {
    const res = await getOpdStationPage({
      ...stationSearch.value,
      pageNum: stationPage.value.pageNum,
      pageSize: stationPage.value.pageSize,
    })
    if (res.code === 200) {
      stationData.value = res.data.records || []
      stationPage.value.total = res.data.total || 0
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    stationLoading.value = false
  }
}
const searchStation = () => { stationPage.value.pageNum = 1; loadStation() }
const resetStation = () => {
  stationSearch.value = {stationKind: null, status: null, keyword: ''}
  searchStation()
}
const handleStationAdd = () => { stationEditRow.value = null; stationDialog.value = true }
const handleStationEdit = (row) => { stationEditRow.value = row; stationDialog.value = true }
const handleStationDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除工位「${row.stationName}」吗？（已有在岗派岗的删不掉）`, '删除工位', {type: 'warning'})
    const res = await deleteOpdStation(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadStation() }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  }
}

// ---------- 派岗记录 ----------
const assignLoading = ref(false)
const assignData = ref([])
const assignPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const assignSearch = ref({stationId: null, isLoan: null, keyword: ''})
const assignDateRange = ref([])
const stationOptions = ref([])

const assignDialog = ref(false)
const assignEditRow = ref(null)

const loadAssign = async () => {
  assignLoading.value = true
  try {
    const [startDate, endDate] = assignDateRange.value || []
    const res = await getOpdAssignPage({
      ...assignSearch.value,
      startDate: startDate || null,
      endDate: endDate || null,
      pageNum: assignPage.value.pageNum,
      pageSize: assignPage.value.pageSize,
    })
    if (res.code === 200) {
      assignData.value = res.data.records || []
      assignPage.value.total = res.data.total || 0
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    assignLoading.value = false
  }
}
const searchAssign = () => { assignPage.value.pageNum = 1; loadAssign() }
const resetAssign = () => {
  assignSearch.value = {stationId: null, isLoan: null, keyword: ''}
  assignDateRange.value = []
  searchAssign()
}
const handleAssignAdd = () => { assignEditRow.value = null; assignDialog.value = true }
const handleAssignDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`撤销 ${row.assignDate}「${row.stationName}」的派岗（${row.employeeName}）？`, '撤销派岗', {type: 'warning'})
    const res = await deleteOpdAssign(row.id)
    if (res.code === 200) { ElMessage.success('已撤销'); loadAssign() }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  }
}

const loadStationOptions = async () => {
  try {
    const res = await getOpdStationSelectList()
    stationOptions.value = res.data || []
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  }
}

const handleTabChange = (name) => {
  if (name === 'assign') { loadAssign(); loadStationOptions() }
  else { loadStation() }
}

onMounted(() => {
  loadStation()
  loadStationOptions()
})
</script>
