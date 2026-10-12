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
    <DutyRosterEdit v-model="dialogVisible" :duty-kind="dutyKind" :row="editRow" @saved="loadData"/>

    <!-- 临时换班 -->
    <DutyRosterSubstituteEdit v-model="subDialogVisible" :row="subRow" @saved="loadData"/>
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
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
// ⚠ @element-plus/icons-vue 没有导出 Swap（写了会导致整个页面模块加载失败、菜单点了白屏）
import {Delete, Edit, Plus, Refresh, Search, Switch} from '@element-plus/icons-vue'
import {
  deleteDutyRoster,
  dutySubstituteCancel,
  getDutyRosterListPage,
} from '@/api/dutyRoster'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {DUTY_KINDS} from '@/lib/dutyRoster'
import DutyOfficerBar from '@/components/his/DutyOfficerBar.vue'
import DutyRosterEdit from './DutyRosterEdit.vue'
import DutyRosterSubstituteEdit from './DutyRosterSubstituteEdit.vue'

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
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const searchForm = ref({beginDate: null, endDate: null, shiftType: null, roleType: null})

// 值班种类：全院总值班（行政位）vs 科室医师值班（sql/202 铺的临床位，39 个科室 × 一线/二线/三线）。
// 两者同居 biz_duty_roster 一张表，靠 post_id 挂的点位分开 —— 页面必须让排班员先选是哪一类，
// 否则「今日全院谁负责」那张表会被 234 个科室值班行冲垮，谁也看不懂。
const dutyKind = ref('admin')
const onDutyKindChange = () => {
  pagination.value.pageNum = 1
  loadData()
}

const dialogVisible = ref(false)
const editRow = ref(null)

// 换班
const subDialogVisible = ref(false)
const subRow = ref(null)

onMounted(() => {
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

const handleAdd = () => {
  editRow.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editRow.value = row
  dialogVisible.value = true
}

const openSubstitute = (row) => {
  subRow.value = row
  subDialogVisible.value = true
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
