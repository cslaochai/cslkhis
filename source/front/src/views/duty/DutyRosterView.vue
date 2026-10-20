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

    <el-tabs v-model="activeTab" class="mb-3">
      <el-tab-pane label="排班列表" name="list">
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
          <el-table-column label="发布态" prop="publishStatus" width="100">
            <template #default="{ row }">
              <el-tag
                  :type="publishStatusTagType(row.publishStatus)"
                  :data-testid="`duty-publish-tag-${row.id}`"
                  size="small"
              >
                {{ row.publishStatusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="换班原因" min-width="160" prop="substituteReason" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="320">
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
              <el-button
                  v-if="row.publishStatus === PUBLISH_DRAFT"
                  v-perm="'org:duty:edit'"
                  :data-testid="`duty-publish-submit-${row.id}`"
                  link
                  type="primary"
                  @click="handleSubmitPublish(row)"
              >
                提交审核
              </el-button>
              <el-button
                  v-if="row.publishStatus === PUBLISH_PENDING"
                  v-perm="'org:duty:audit'"
                  :data-testid="`duty-publish-audit-${row.id}`"
                  link
                  type="success"
                  @click="handleAuditPublish(row)"
              >
                审核发布
              </el-button>
              <el-button
                  v-if="isPublishEffective(row.publishStatus)"
                  v-perm="'org:duty:edit'"
                  :data-testid="`duty-publish-adjust-${row.id}`"
                  link
                  type="warning"
                  @click="handleAdjustStart(row)"
              >
                进入调班
              </el-button>
              <el-button
                  v-perm="'org:duty:edit'"
                  :data-testid="`duty-publish-recall-${row.id}`"
                  link
                  type="info"
                  @click="handleRecallPublish(row)"
              >
                撤回发布态
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
      </el-tab-pane>

      <!-- ============ 统计校验：月度值班工时 + 区间规则校验 ============ -->
      <el-tab-pane label="统计校验" name="stats">
        <div class="space-y-3">
          <el-card class="table-card" shadow="never">
            <template #header>
              <span class="font-medium">月度值班工时</span>
              <span class="ml-3 text-xs text-slate-400">按实际值班人归并（换班后算顶替人）；白班/夜班时长取班次字典</span>
            </template>
            <div class="mb-2 flex flex-wrap items-center gap-2">
              <el-date-picker v-model="workloadForm.month" type="month" value-format="YYYY-MM"
                              placeholder="选择月份" style="width: 140px"/>
              <el-button :icon="Search" type="primary" :loading="workloadLoading" @click="loadWorkload">统计</el-button>
              <span v-if="workloadResult.rows.length" class="text-sm text-slate-500">
                {{ workloadResult.dutyCount ?? 0 }} 人参与排班 · 总 {{ workloadResult.totalDuties ?? 0 }} 班次 ·
                总时长 {{ workloadResult.totalHours ?? 0 }} 小时
              </span>
            </div>
            <el-table v-loading="workloadLoading" :data="workloadResult.rows" :max-height="tableMaxHeight" stripe>
              <el-table-column label="工号" prop="empCode" width="100"/>
              <el-table-column label="姓名" prop="employeeName" width="100"/>
              <el-table-column label="原属科室" prop="deptName" min-width="140"/>
              <el-table-column label="白班" prop="dayCount" width="70" align="center"/>
              <el-table-column label="夜班" prop="nightCount" width="70" align="center"/>
              <el-table-column label="主班" prop="primaryCount" width="70" align="center"/>
              <el-table-column label="副班" prop="secondaryCount" width="70" align="center"/>
              <el-table-column label="总时长(分)" prop="totalMinutes" width="100" align="right"/>
              <el-table-column label="总时长(时)" prop="totalHours" width="100" align="right"/>
            </el-table>
            <el-empty v-if="!workloadLoading && workloadResult.rows.length === 0" :image-size="60"
                      description="该月还没有已发布的值班排班"/>
          </el-card>

          <el-card class="table-card" shadow="never">
            <template #header>
              <span class="font-medium">区间规则校验</span>
              <span class="ml-3 text-xs text-slate-400">连续值班 / 值班频次 / 主班漏排，只列明细不阻断</span>
            </template>
            <div class="mb-2 flex flex-wrap items-center gap-2">
              <el-date-picker v-model="checkForm.startDate" type="date" value-format="YYYY-MM-DD"
                              placeholder="开始日期" style="width: 140px"/>
              <el-date-picker v-model="checkForm.endDate" type="date" value-format="YYYY-MM-DD"
                              placeholder="结束日期" style="width: 140px"/>
              <el-button :icon="Search" type="primary" :loading="checkLoading" @click="runCheck">开始校验</el-button>
              <span class="text-sm text-slate-500">
                告警 {{ checkResult.alertCount ?? 0 }} 条 · 提示 {{ checkResult.hintCount ?? 0 }} 条
              </span>
            </div>
            <el-table v-loading="checkLoading" :data="checkResult.warnings" :max-height="tableMaxHeight" stripe>
              <el-table-column label="级别" width="80">
                <template #default="{ row }">
                  <el-tag :type="Number(row.level) === 1 ? 'danger' : 'warning'" size="small">
                    {{ Number(row.level) === 1 ? '告警' : '提示' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="规则" prop="typeText" width="110"/>
              <el-table-column label="日期" prop="dutyDate" width="105"/>
              <el-table-column label="班次" prop="shiftTypeText" width="80"/>
              <el-table-column label="点位" prop="postName" min-width="150" show-overflow-tooltip/>
              <el-table-column label="值班人" prop="employeeName" width="110"/>
              <el-table-column label="实际/上限" width="100">
                <template #default="{ row }">
                  <span v-if="row.actual != null">{{ row.actual }}<template v-if="row.required != null">/{{ row.required }}</template></span>
                  <span v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column label="说明" prop="message" min-width="260" show-overflow-tooltip/>
            </el-table>
            <el-empty v-if="!checkLoading && checkResult.warnings.length === 0" :image-size="60"
                      description="区间内没有违反值班规则的排班"/>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

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
  checkDutyRoster,
  deleteDutyRoster,
  dutyRosterAdjustStart,
  dutyRosterAuditPublish,
  dutyRosterRecall,
  dutyRosterSubmit,
  dutySubstituteCancel,
  getDutyRosterListPage,
  getDutyWorkload,
} from '@/api/dutyRoster'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {DUTY_KINDS} from '@/lib/dutyRoster'
import {PUBLISH_DRAFT, PUBLISH_PENDING, isPublishEffective, publishStatusTagType} from '@/lib/publishStatus'
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

// 发布态流转（G-15）：草稿 → 待审核 → 已发布，已发布/已变更可进入调班。
// 只有「已发布 / 已变更」才进今日值班表、才让「此刻谁负责」解析到人，
// 所以撤回必须二次确认 —— 点错了等于半夜把全院的兜底收口人撤没了。
const handleSubmitPublish = async (row) => {
  try {
    const res = await dutyRosterSubmit(row.id)
    if (res.code === 200) {
      ElMessage.success('已提交审核')
      loadData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
}

const handleAuditPublish = async (row) => {
  try {
    const res = await dutyRosterAuditPublish(row.id)
    if (res.code === 200) {
      ElMessage.success('已发布')
      loadData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
}

const handleAdjustStart = async (row) => {
  try {
    const res = await dutyRosterAdjustStart(row.id)
    if (res.code === 200) {
      ElMessage.success('已进入调班')
      loadData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
}

const handleRecallPublish = async (row) => {
  try {
    await ElMessageBox.confirm(
        `撤回后本行退回草稿，今日值班表与「此刻谁负责」将不再指向 ${row.actualEmpName || row.employeeName}，确定吗？`,
        '撤回发布态', {type: 'warning'})
    const res = await dutyRosterRecall(row.id)
    if (res.code === 200) {
      ElMessage.success('已撤回至草稿')
      loadData()
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

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
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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

// ---------------- 统计校验页签 ----------------
const activeTab = ref('list')
const pad = (n) => String(n).padStart(2, '0')

const workloadForm = ref({month: null})
const workloadResult = ref({dutyCount: 0, totalDuties: 0, totalHours: 0, rows: []})
const workloadLoading = ref(false)

const loadWorkload = async () => {
  if (!workloadForm.value.month) {
    ElMessage.warning('请选择统计月份')
    return
  }
  workloadLoading.value = true
  try {
    const res = await getDutyWorkload({month: workloadForm.value.month})
    workloadResult.value = res.code === 200
        ? (res.data || {dutyCount: 0, totalDuties: 0, totalHours: 0, rows: []})
        : {dutyCount: 0, totalDuties: 0, totalHours: 0, rows: []}
  } catch {
    workloadResult.value = {dutyCount: 0, totalDuties: 0, totalHours: 0, rows: []}
  } finally {
    workloadLoading.value = false
  }
}

const checkForm = ref({startDate: null, endDate: null})
const checkResult = ref({alertCount: 0, hintCount: 0, warnings: []})
const checkLoading = ref(false)

const runCheck = async () => {
  if (!checkForm.value.startDate || !checkForm.value.endDate) {
    ElMessage.warning('请选择校验日期区间')
    return
  }
  checkLoading.value = true
  try {
    const res = await checkDutyRoster(checkForm.value)
    checkResult.value = res.code === 200 ? (res.data || {alertCount: 0, hintCount: 0, warnings: []}) : {alertCount: 0, hintCount: 0, warnings: []}
  } catch {
    checkResult.value = {alertCount: 0, hintCount: 0, warnings: []}
  } finally {
    checkLoading.value = false
  }
}

// 初始默认值：工时统计取本月，校验区间取本月 1 号 ~ 今天
{
  const now = new Date()
  const ymd = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  const ym = `${now.getFullYear()}-${pad(now.getMonth() + 1)}`
  workloadForm.value.month = ym
  checkForm.value.endDate = ymd
  checkForm.value.startDate = `${ym}-01`
}
</script>
