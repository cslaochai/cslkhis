<template>
  <div class="w-full">
    <!-- 工具条：病区 + 周 + 动作。数据范围由后端按岗位收口，这里不给「全部病区」开关 -->
    <div
        class="mb-3 flex flex-wrap items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm filter-row">
      <el-select v-model="unitKey" :fit-input-width="false" class="!w-64" data-testid="ns-ward"
                 filterable placeholder="选择排班单元（病区 / 门诊科室）">
        <el-option v-for="u in units" :key="`${u.unitType}:${u.wardId}`"
                   :label="`${u.unitType === 2 ? '门诊' : '病区'} · ${u.wardName}（在册 ${u.nurseCount} 人）`"
                   :value="`${u.unitType}:${u.wardId}`"/>
      </el-select>
      <!-- 不用 WW 令牌（没装 dayjs isoWeek 插件会整块渲染报错）；日期选择器也别挂 data-testid，EP 只透传 class/style，
           验收脚本一律用 .el-date-editor--week / --daterange 这类结构选择器 -->
      <el-date-picker v-model="weekPick" :clearable="false" class="!w-40" format="YYYY-MM-DD" placeholder="选择周"
                      type="week" value-format="YYYY-MM-DD"/>
      <el-button :icon="ArrowLeft" data-testid="ns-prev-week" @click="shiftWeek(-1)">上一周</el-button>
      <el-button data-testid="ns-this-week" @click="goThisWeek">本周</el-button>
      <el-button data-testid="ns-next-week" @click="shiftWeek(1)">下一周</el-button>
      <el-button :icon="Refresh" :loading="matrixLoading" type="primary" @click="reloadAll">刷新</el-button>
      <el-button v-perm="PERM_ADD" :icon="CopyDocument" data-testid="ns-copy-week" type="success" @click="doCopyWeek">
        复制上周
      </el-button>
      <span class="ml-auto text-sm text-slate-500" data-testid="ns-week-range">
        {{ weekStart }} ~ {{ weekEnd }}
        <template v-if="matrix"> · {{ matrix.deptName }}</template>
      </span>
    </div>

    <el-tabs v-model="activeTab" class="ns-tabs">
      <!-- ============ 周排班矩阵 ============ -->
      <el-tab-pane label="周排班矩阵" name="matrix">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-2 flex flex-wrap items-center gap-3 text-sm">
            <span class="font-medium text-slate-700">在岗人力对照</span>
            <el-tag v-perm="PERM_ADD" effect="plain" size="small" type="info">有排班权限</el-tag>
            <el-tag v-if="!hasPerm(PERM_ADD)" effect="plain" size="small" type="warning">只读（无排班权限）</el-tag>
          </div>

          <!-- 本周护理需求：排班的分母。只显示后端算好的三个数，前端不算需求也不判阈值 -->
          <div
              v-loading="gapLoading"
              class="mb-3 flex flex-wrap items-center gap-x-4 gap-y-1 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-sm"
              data-testid="ns-demand-gap">
            <span class="font-medium text-slate-700">本周护理需求</span>
            <span v-for="d in (matrix?.days || [])" :key="d" class="flex items-center gap-1">
              <span class="text-slate-400">{{ d.slice(5) }}</span>
              <span :class="gapClass(d)">
                {{ gapOf(d) ? `需 ${gapOf(d).requiredCount} / 在岗 ${gapOf(d).scheduledCount}` : '—' }}
              </span>
              <span v-if="gapOf(d) && Number(gapOf(d).gapCount) > 0" class="text-red-600">缺 {{
                  gapOf(d).gapCount
                }}</span>
              <span v-else-if="gapOf(d) && Number(gapOf(d).gapCount) < 0"
                    class="text-emerald-600">富余 {{ -Number(gapOf(d).gapCount) }}</span>
            </span>
            <el-button v-perm="PERM_EDIT" :loading="gapLoading" size="small" @click="doRecalcDemand">重算需求
            </el-button>
            <span v-if="!gaps.length" class="w-full text-xs text-amber-600">
              本周（{{ weekStart }} ~ {{ weekEnd }}）没有需求数据：需求按当前在院患者与出诊计划派生，默认覆盖今天起 14 天。点「重算需求」可就本周补算。
            </span>
            <span v-else-if="gaps[0].calcBasis" :title="gaps[0].calcBasis" class="w-full text-xs text-slate-400">
              测算依据：{{ gaps[0].calcBasis }}
            </span>
          </div>

          <div
              v-loading="attendLoading"
              class="mb-3 flex flex-wrap items-center gap-x-4 gap-y-1 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-sm"
              data-testid="ns-attend-summary">
            <span class="font-medium text-slate-700">本周出勤执行</span>
            <span v-for="d in (matrix?.days || [])" :key="d"
                  class="flex cursor-pointer items-center gap-1 hover:underline" @click="openDayDetail(d)">
              <span class="text-slate-400">{{ d.slice(5) }}</span>
              <span v-if="isFuture(d)" class="text-slate-300">未到</span>
              <span v-else-if="attendOf(d)"
                    :class="Number(attendOf(d).absentHead) > 0 ? 'text-red-600'
                          : Number(attendOf(d).unrecordedHead) > 0 ? 'text-amber-600' : 'text-emerald-600'">
                实到 {{ attendOf(d).presentHead }}/{{ attendOf(d).planHead }}
              </span>
              <span v-else class="text-slate-400">—</span>
              <span v-if="!isFuture(d) && attendOf(d) && Number(attendOf(d).unrecordedHead) > 0" class="text-amber-600">
                未回填 {{ attendOf(d).unrecordedHead }}
              </span>
              <span v-if="attendOf(d) && Number(attendOf(d).absentHead) > 0" class="text-red-600">
                缺勤 {{ attendOf(d).absentHead }}
              </span>
            </span>
            <span v-if="!attends.length" class="w-full text-xs text-slate-400">
              本周还没有出勤登记 —— 「没有记录」不等于缺勤，点某一天可以逐个补登工时或确认缺勤。
            </span>
            <span v-else-if="advice" :title="advice.adviceText" class="w-full text-xs text-slate-400">
              执行回看：{{ advice.adviceText }}
            </span>
          </div>

          <el-table v-loading="matrixLoading" :data="matrixRows" :height="matrixTableHeight" border
                    data-testid="ns-matrix"
                    size="small" style="width: 100%">
            <el-table-column fixed label="护士" width="150">
              <template #default="{ row }">
                <div class="leading-tight">
                  <span class="font-medium text-slate-800">{{ row.nurseName }}</span>
                  <span class="ml-1 text-xs text-slate-400">{{ row.nurseTitle }}</span>
                  <p class="text-xs text-slate-400">{{ row.empCode }}</p>
                </div>
              </template>
            </el-table-column>
            <el-table-column v-for="(d, i) in (matrix?.days || [])" :key="d" :label="dayHead(d)" align="center"
                             min-width="104">
              <template #header>
                <span :class="d === today ? 'font-semibold text-blue-700' : ''">{{ dayHead(d) }}</span>
              </template>
              <template #default="{ row }">
                <div :class="{ 'is-today': d === today }" class="cell-box" data-testid="ns-cell"
                     @click="openCell(row, d)">
                  <template v-if="row.dayCells[i]">
                    <span v-if="row.dayCells[i].scheduleStatus === 1"
                          :style="{ background: shiftColor(row.dayCells[i].shiftId) }"
                          class="shift-chip">
                      {{ shortShift(row.dayCells[i].shiftName) }}
                    </span>
                    <span v-else class="status-chip">{{
                        (row.dayCells[i].scheduleStatusText || '—').slice(0, 2)
                      }}</span>
                    <p v-if="row.dayCells[i].scheduleStatus === 1" class="cell-time">{{ row.dayCells[i].startTime }}</p>
                  </template>
                  <span v-else class="text-slate-300">—</span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 底部人力对照：每班每天在岗人数 / 应配下限，缺口标红（数字全部来自后端 staffing） -->
          <div v-if="staffingRows.length" class="mt-3 overflow-x-auto">
            <table class="staffing-table" data-testid="ns-staffing">
              <thead>
              <tr>
                <th class="left">班次 ＼ 日期</th>
                <th v-for="d in (matrix?.days || [])" :key="d">{{ dayHead(d) }}</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="s in staffingRows" :key="s.shiftId" :class="{ 'is-total': s.shiftId === '0' }">
                <td class="left">{{ s.shiftName }}</td>
                <td v-for="(x, i) in s.days" :key="i"
                    :class="x && x.minStaff != null && x.staffCount < x.minStaff ? 'is-gap' : ''">
                  <template v-if="x">{{ x.staffCount }}<span
                      class="text-slate-400"> / {{ x.minStaff != null ? x.minStaff : '—' }}</span></template>
                  <span v-else class="text-slate-300">—</span>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- 本周告警：只列，不拦保存 -->
          <div class="mt-3" data-testid="ns-warnings">
            <div class="mb-1 flex items-center gap-2 text-sm">
              <span class="font-medium text-slate-700">本周规则提示</span>
              <el-tag effect="dark" size="small" type="danger">{{ weekAlerts.length }} 条告警</el-tag>
              <el-tag effect="plain" size="small" type="info">{{ weekHints.length }} 条提示</el-tag>
            </div>
            <div v-if="!weekAlerts.length && !weekHints.length" class="text-sm text-emerald-600">本周没有规则冲突</div>
            <ul class="warn-list">
              <li v-for="(w, i) in matrix?.warnings || []" :key="i"
                  :class="Number(w.level) === 1 ? 'is-alert' : 'is-hint'" data-testid="ns-warn-item">
                <el-tag :type="Number(w.level) === 1 ? 'danger' : 'info'" effect="plain" size="small">
                  {{ warnTypeText(w.type) }}
                </el-tag>
                <span class="text-slate-500">{{ w.scheduleDate || '整周' }}</span>
                <span>{{ w.message }}</span>
              </li>
            </ul>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 规则校验 ============ -->
      <el-tab-pane label="规则校验" lazy name="check">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-date-picker v-model="checkQuery.startDate" class="!w-40" placeholder="开始日期" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-date-picker v-model="checkQuery.endDate" class="!w-40" placeholder="结束日期" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-button :icon="Search" :loading="checkLoading" data-testid="ns-check-btn" type="primary"
                       @click="runCheck">开始校验
            </el-button>
            <template v-if="checkResult">
              <el-tag effect="dark" size="small" type="danger">告警 {{ checkResult.alertCount }}</el-tag>
              <el-tag effect="plain" size="small" type="info">提示 {{ checkResult.hintCount }}</el-tag>
              <span class="text-sm text-slate-500">{{ checkResult.wardName }} · {{
                  checkResult.startDate
                }} ~ {{ checkResult.endDate }}</span>
            </template>
          </div>
          <el-table v-loading="checkLoading" :data="checkResult?.warnings || []" :height="matrixTableHeight" border
                    data-testid="ns-check-table">
            <el-table-column align="center" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="Number(row.level) === 1 ? 'danger' : 'info'" size="small">
                  {{ Number(row.level) === 1 ? '告警' : '提示' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" label="类型" width="130">
              <template #default="{ row }">{{ warnTypeText(row.type) }}</template>
            </el-table-column>
            <el-table-column align="center" label="日期" prop="scheduleDate" width="120"/>
            <el-table-column label="班次" prop="shiftName" width="120"/>
            <el-table-column label="护士" prop="nurseName" width="110"/>
            <el-table-column align="center" label="实际/标准" width="110">
              <template #default="{ row }">{{ row.actual ?? '—' }} / {{ row.required ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="说明" min-width="300" prop="message" show-overflow-tooltip/>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- ============ 月度工时 ============ -->
      <el-tab-pane label="月度工时" lazy name="workload">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-date-picker v-model="workloadMonth" :clearable="false" class="!w-40" placeholder="统计月份"
                            type="month"
                            value-format="YYYY-MM" @change="loadWorkload"/>
            <el-button :icon="Refresh" :loading="workloadLoading" type="primary" @click="loadWorkload">统计</el-button>
            <template v-if="workload">
              <span class="text-sm text-slate-600" data-testid="ns-wl-total">
                在册 {{ workload.nurseCount }} 人 · 上班 {{
                  workload.totalWorkDays
                }} 班 · 夜班 {{ workload.totalNightDays }} 班 ·
                合计 <b class="text-blue-700">{{ workload.totalWorkHours }}</b> 小时
                <span v-if="workload.maxWeekHours" class="text-slate-400">（单周上限 {{ workload.maxWeekHours }}h）</span>
              </span>
            </template>
          </div>
          <el-table v-loading="workloadLoading" :data="workload?.rows || []" :height="matrixTableHeight" border
                    data-testid="ns-wl-table">
            <el-table-column fixed label="工号" prop="empCode" width="100"/>
            <el-table-column fixed label="护士" prop="nurseName" width="110"/>
            <el-table-column label="职称" prop="nurseTitle" width="90"/>
            <el-table-column align="center" label="上班" prop="workDays" width="70"/>
            <el-table-column align="center" label="休息" prop="restDays" width="70"/>
            <el-table-column align="center" label="请假" prop="leaveDays" width="70"/>
            <el-table-column align="center" label="培训" prop="trainingDays" width="70"/>
            <el-table-column align="center" label="停排" prop="suspendedDays" width="70"/>
            <el-table-column align="center" label="夜班" width="80">
              <template #default="{ row }">
                <span :class="row.nightDays > 8 ? 'font-semibold text-red-600' : ''">{{ row.nightDays }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" label="工时(h)" width="100">
              <template #default="{ row }">{{ row.workHours }}</template>
            </el-table-column>
            <el-table-column align="center" label="缺排班(天)" width="110">
              <template #default="{ row }">
                <el-tag v-if="row.missingDays > 0" size="small" type="warning">{{ row.missingDays }}</el-tag>
                <span v-else class="text-slate-400">0</span>
              </template>
            </el-table-column>
          </el-table>
          <p class="mt-2 text-xs text-slate-400">
            夜班口径=班次开始时刻早于 08:00 或晚于 16:00（与连续夜班校验同一把尺）；缺排班天数=当月天数−已写排班行的天数，用来发现「整周没排」。
          </p>
        </div>
      </el-tab-pane>

      <!-- ============ 排班台账 ============ -->
      <el-tab-pane label="排班台账" lazy name="ledger">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-input v-model="ledger.keyword" class="!w-44" clearable data-testid="ns-ledger-kw"
                      placeholder="护士姓名/工号"
                      @clear="searchLedger" @keyup.enter="searchLedger"/>
            <el-select v-model="ledger.status" :fit-input-width="false" class="!w-36" clearable
                       data-testid="ns-ledger-status"
                       placeholder="排班状态" @change="searchLedger">
              <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-date-picker v-model="ledger.dateRange" class="!w-64" end-placeholder="结束日期"
                            start-placeholder="开始日期"
                            type="daterange" value-format="YYYY-MM-DD" @change="searchLedger"/>
            <el-button :icon="Search" type="primary" @click="searchLedger">查询</el-button>
          </div>
          <el-table v-loading="ledgerLoading" :data="ledgerRows" :max-height="matrixTableHeight" border
                    data-testid="ns-ledger-table">
            <el-table-column label="病区" prop="wardName" show-overflow-tooltip width="130"/>
            <el-table-column align="center" label="日期" prop="scheduleDate" width="110"/>
            <el-table-column label="工号" prop="empCode" width="90"/>
            <el-table-column label="护士" prop="nurseName" width="100"/>
            <el-table-column label="职称" prop="nurseTitle" width="90"/>
            <el-table-column label="班次" width="120">
              <template #default="{ row }">{{
                  row.scheduleStatus === 1 ? row.shiftName : statusText(row.scheduleStatus)
                }}
              </template>
            </el-table-column>
            <el-table-column align="center" label="起止" width="120">
              <template #default="{ row }">
                <span class="font-mono text-xs text-slate-600">{{
                    row.startTime ? `${row.startTime}~${row.endTime}` : '—'
                  }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" label="工时" width="80">
              <template #default="{ row }">{{ row.workMinutes ? (row.workMinutes / 60).toFixed(1) : '0' }}</template>
            </el-table-column>
            <el-table-column align="center" label="来源" width="90">
              <template #default="{ row }">{{ Number(row.scheduleSource) === 2 ? '复制上周' : '人工' }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip/>
            <el-table-column align="center" fixed="right" label="操作" width="80">
              <template #default="{ row }">
                <el-button v-perm="PERM_DELETE" data-testid="ns-ledger-del" link size="small" type="danger"
                           @click.stop="removeCell(row)">删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination :current-page="ledger.pageNum" :page-size="ledger.pageSize" :page-sizes="PAGE_SIZES"
                         :total="ledgerTotal" background class="mt-3 justify-end"
                         layout="total, prev, pager, next, sizes, jumper"
                         @current-change="(v: number) => { ledger.pageNum = v; loadLedger() }"
                         @size-change="(v: number) => { ledger.pageSize = v; ledger.pageNum = 1; loadLedger() }"/>
        </div>
      </el-tab-pane>

      <!-- ============ 人力配置标准 ============ -->
      <el-tab-pane label="人力配置标准" lazy name="rules">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <el-button v-perm="PERM_EDIT" :icon="Plus" data-testid="ns-rule-add" type="primary" @click="openRule()">
              新增标准
            </el-button>
            <el-button :icon="Refresh" @click="loadRules">刷新</el-button>
            <span
                class="text-sm text-slate-500">「病区合计」是病区级行：周工时上限与连班上限只在这一行有效；班次行只管每班最低/最高在岗。</span>
          </div>
          <el-table v-loading="ruleLoading" :data="rules" :height="matrixTableHeight" border
                    data-testid="ns-rule-table">
            <el-table-column label="适用班次" prop="shiftName" width="150"/>
            <el-table-column align="center" label="最低在岗" width="100">
              <template #default="{ row }">{{ row.minStaff ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="center" label="最高在岗" width="100">
              <template #default="{ row }">{{ row.maxStaff ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="center" label="周工时上限(h)" width="130">
              <template #default="{ row }">{{ row.maxWeekHours ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="center" label="连续夜班≤" width="120">
              <template #default="{ row }">{{ row.maxConsecutiveNightDays ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="center" label="连续上班≤" width="120">
              <template #default="{ row }">{{ row.maxConsecutiveWorkDays ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="center" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">
                  {{ Number(row.status) === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="180" prop="remark" show-overflow-tooltip/>
            <el-table-column align="center" fixed="right" label="操作" width="120">
              <template #default="{ row }">
                <el-button v-perm="PERM_EDIT" data-testid="ns-rule-edit" link size="small" type="primary"
                           @click="openRule(row)">编辑
                </el-button>
                <el-button v-perm="PERM_EDIT" link size="small" type="danger" @click="removeRule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 点格排班弹窗：表单与保存在 NurseScheduleCellEdit -->
    <NurseScheduleCellEdit v-model="cellVisible" :row="cellRow" :shifts="shifts" :status-options="statusOptions"
                           :unit-type="unitType" @delete="removeCell" @saved="loadMatrix"/>

    <!-- 人力标准弹窗：表单与保存在 NurseScheduleRuleEdit（新增+编辑合并） -->
    <NurseScheduleRuleEdit v-model="ruleVisible" :row="ruleRow" :shifts="shifts" :ward-id="wardId"
                           @saved="onRuleSaved"/>

    <!-- 某一天的出勤对照：数据拉取与补登/缺勤/撤销动作在 NurseScheduleAttendDetail -->
    <NurseScheduleAttendDetail v-model="dayDetailVisible" :date="dayDetailDate" :org-id="wardId"
                               :org-type="demandOrgType" @changed="loadAttend"/>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {ArrowLeft, CopyDocument, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  checkNurseSchedule,
  copyNurseScheduleWeek,
  deleteNurseScheduleCell,
  deleteNurseScheduleRule,
  getNurseMonthWorkload,
  getNurseScheduleRuleList,
  getNurseUnitSelectList,
  getNurseWeekMatrix,
  listNurseSchedulePage,
} from '@/api/nurseSchedule';
import {getStaffDemandGapList, recalcStaffDemand} from '@/api/staffDemand';
import {getAttendanceAdvice, getAttendanceSummary} from '@/api/staffAttendance';
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {hasPerm} from '@/lib/perm';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {PERM_ADD, PERM_EDIT, PERM_DELETE} from '@/lib/nurseSchedule';
// 拆分出的弹框组件
import NurseScheduleCellEdit from './NurseScheduleCellEdit.vue';
import NurseScheduleRuleEdit from './NurseScheduleRuleEdit.vue';
import NurseScheduleAttendDetail from './NurseScheduleAttendDetail.vue';

const activeTab = ref('matrix');
/** 排班状态字典（1上班/2休息/3请假/4培训/5停排），矩阵格与筛选下拉共用 */
const statusDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const statusOptions = computed(() => statusDict.value.map((d) => ({
  label: d.dictLabel,
  value: Number(d.dictValue)
})));
/** 格子里班次名的短写：铺底名称都是「护理白班」这类，矩阵列宽放不下 */
const shortShift = (name) => String(name || '').replace(/^护理/, '');
const WEEK_DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
// ---------------- 本地日期（别用 toISOString：那是 UTC，北京时间 0~8 点会拿到「昨天」） ----------------
const dstr = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const addDays = (s, n) => {
  const d = new Date(`${s}T00:00:00`);
  d.setDate(d.getDate() + n);
  return dstr(d);
};
/** 周一为一周起点（与后端 mondayOf 同口径，两边算出的 weekStart 必须同一天） */
const mondayOf = (s) => {
  const d = new Date(`${s}T00:00:00`);
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7));
  return dstr(d);
};
// ---------------- 病区 + 周 ----------------
const units = ref([]);
const wardId = ref(null);
// 排班单元类型（1-病区 2-门诊科室，sql/209）：病区 id 与科室 id 不在同一个 id 空间，
// 下拉的 value 必须把类型一起带上，否则「门诊科室的 id」会被当成「病区的 id」。
const unitType = ref(1);
const unitKey = computed({
  get: () => (wardId.value ? `${unitType.value}:${wardId.value}` : ''),
  set: (v) => {
    const [t, id] = String(v || '').split(':');
    unitType.value = Number(t) || 1;
    wardId.value = id || null;
  },
});
const weekStart = ref(mondayOf(dstr(new Date())));
// 周选择器回传的是「该周内任意一天」（随区域设置而变），一律再锚回周一，
// 保证与后端 mondayOf 算出的 weekStart 是同一天
const weekPick = ref(weekStart.value);
const weekEnd = computed(() => addDays(weekStart.value, 6));
const today = dstr(new Date);
const loadUnits = async () => {
  try {
    const res = await getNurseUnitSelectList();
    units.value = (res.data || []).map((u) => ({...u, unitType: Number(u.unitType) || 1}));
    // 全院单元几十个，默认落在第一个「有在册护士」的单元：一个护士都没有的单元点开只能看见一张空表，
    // 护士长进来就是要排班，不该先做一次无效选择
    if (!wardId.value && units.value.length) {
      const first = units.value.find((u) => Number(u.nurseCount) > 0) || units.value[0];
      unitType.value = first.unitType;
      wardId.value = first.wardId;
    }
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ---------------- 周矩阵 ----------------
const matrix = ref(null);
const matrixLoading = ref(false);
const loadMatrix = async () => {
  if (!wardId.value)
    return;
  matrixLoading.value = true;
  try {
    const res = await getNurseWeekMatrix({
      unitType: unitType.value,
      wardId: wardId.value,
      weekStart: weekStart.value
    });
    
    matrix.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    matrixLoading.value = false;
  }
};
// ---------------- 本周护理需求缺口（sql/212，排班的分母） ----------------
const demandOrgType = computed(() => (unitType.value === 1 ? 2 : 1));
const gaps = ref([]);
const gapLoading = ref(false);
const loadGaps = async () => {
  if (!wardId.value)
    return;
  gapLoading.value = true;
  try {
    const res = await getStaffDemandGapList({
      startDate: weekStart.value, endDate: weekEnd.value,
      orgType: demandOrgType.value, orgId: wardId.value, staffType: 2,
    });
    gaps.value = res.data || [];
  } catch {
    // 缺口是辅助信息，拉不到不能挡住排班：静默置空，矩阵照常渲染
    gaps.value = [];
  } finally {
    gapLoading.value = false;
  }
};
const gapOf = (d) => gaps.value.find((g) => g.demandDate === d) || null;
/** 缺口标红、富余标绿：只显示数字，判据一律来自后端（前端不算需求） */
const gapClass = (d) => {
  const g = gapOf(d);
  if (!g)
    return 'text-slate-400';
  return Number(g.gapCount) > 0 ? 'text-red-600 font-medium' : 'text-emerald-600';
};
const doRecalcDemand = async () => {
  try {
    await ElMessageBox.confirm(`按当前在院患者与出诊计划重算 ${weekStart.value} ~ ${weekEnd.value} 的护理需求。护士长手工调过的不会被覆盖。`, '重算人力需求', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await recalcStaffDemand({startDate: weekStart.value, endDate: weekEnd.value});
    ElMessage.success(res.message || '需求已重算');
    await loadGaps();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ---------------- 本周出勤执行（sql/214，闭环第3步：计划 vs 实际） ----------------
const attends = ref([]);
const attendLoading = ref(false);
const advice = ref(null);
const loadAttend = async () => {
  if (!wardId.value)
    return;
  attendLoading.value = true;
  try {
    const res = await getAttendanceSummary({
      startDate: weekStart.value, endDate: weekEnd.value,
      orgType: demandOrgType.value, orgId: wardId.value, staffType: 2,
    });
    attends.value = res.data || [];
    const ra = await getAttendanceAdvice({orgType: demandOrgType.value, orgId: wardId.value, staffType: 2});
    advice.value = (ra.data || [])[0] || null;
  } catch {
    // 出勤是辅助信息，拉不到不能挡住排班
    attends.value = [];
    advice.value = null;
  } finally {
    attendLoading.value = false;
  }
};
const attendOf = (d) => attends.value.find((a) => a.workDate === d) || null;
const pad2 = (n) => String(n).padStart(2, '0');
const now0 = new Date();
const todayStr = `${now0.getFullYear()}-${pad2(now0.getMonth() + 1)}-${pad2(now0.getDate())}`;
const isFuture = (d) => d > todayStr;
// 某一天的出勤对照：抽屉在 NurseScheduleAttendDetail，补登/缺勤/撤销都在那里做
const dayDetailVisible = ref(false);
const dayDetailDate = ref('');
const openDayDetail = (d) => {
  dayDetailDate.value = d;
  dayDetailVisible.value = true;
};
const shifts = computed(() => matrix.value?.shifts || []);
/** 班次色卡：按矩阵返回的班次顺序取，保证同一班次在两周内颜色一致 */
const SHIFT_COLORS = ['#1269B5', '#0E9488', '#7C3AED', '#B45309', '#BE123C', '#475569', '#15803D', '#0369A1'];
const shiftColor = (shiftId) => {
  const idx = shifts.value.findIndex((s) => String(s.shiftId) === String(shiftId));
  return SHIFT_COLORS[(idx < 0 ? 0 : idx) % SHIFT_COLORS.length];
};
const cellOf = (employeeId, date) => (matrix.value?.cells || []).find((c) => String(c.employeeId) === String(employeeId) && c.scheduleDate === date);
/** 表格行：一行一个护士，带上该护士的 7 个格子（el-table 只认扁平行） */
const matrixRows = computed(() => (matrix.value?.nurses || []).map((n) => ({
  ...n,
  dayCells: (matrix.value?.days || []).map((d) => cellOf(n.employeeId, d) || null),
})));
/** 底部人力对照：班次（含「病区合计」shiftId=0）× 7 天 */
const staffingRows = computed(() => {
  const st = matrix.value?.staffing || [];
  const keys = [];
  st.forEach((s) => {
    if (!keys.some(k => k.shiftId === String(s.shiftId)))
      keys.push({
        shiftId: String(s.shiftId),
        shiftName: s.shiftName
      });
  });
  return keys.map(k => ({
    shiftId: k.shiftId,
    shiftName: k.shiftName,
    days: (matrix.value?.days || []).map(d => st.find((s) => String(s.shiftId) === k.shiftId && s.scheduleDate === d) || null),
  }));
});
const dayHead = (d) => {
  const i = (matrix.value?.days || []).indexOf(d);
  return `${WEEK_DAYS[i < 0 ? 0 : i]} ${d.slice(5)}`;
};
const weekAlerts = computed(() => (matrix.value?.warnings || []).filter((w) => Number(w.level) === 1));
const weekHints = computed(() => (matrix.value?.warnings || []).filter((w) => Number(w.level) !== 1));
const setWeek = (s) => {
  const m = mondayOf(s);
  if (!m || Number.isNaN(new Date(`${m}T00:00:00`).getTime()))
    return;
  weekStart.value = m;
  weekPick.value = m;
};
const shiftWeek = (n) => setWeek(addDays(weekStart.value, n * 7));
const goThisWeek = () => setWeek(dstr(new Date()));
// ---------------- 表格高度：撑满视口，内部滚动 ----------------
const matrixTableHeight = ref(420);
const calcHeight = () => {
  // 卡片顶偏移实测 ~150（Header 64 + main 上边距 + 工具条 44），底部留 16
  matrixTableHeight.value = Math.max(240, window.innerHeight - (activeTab.value === 'matrix' ? 330 : 300));
};
watch(activeTab, (t) => {
  calcHeight();
  // 页签是 lazy 挂载的：切过去才拉数，进页面只拉矩阵那一份
  if (t === 'ledger' && !ledgerRows.value.length)
    loadLedger();
  if (t === 'workload' && !workload.value)
    loadWorkload();
  if (t === 'rules' && !rules.value.length)
    loadRules();
  if (t === 'check' && !checkResult.value)
    runCheck();
});
// ---------------- 点格排班（表单与保存在 NurseScheduleCellEdit） ----------------
const cellVisible = ref(false);
const cellRow = ref(null);
const openCell = (nurse, date) => {
  if (!hasPerm(PERM_ADD)) {
    ElMessage.info('当前岗位没有排班权限（nursing:schedule:add），矩阵为只读');
    return;
  }
  const cell = cellOf(nurse.employeeId, date);
  cellRow.value = {
    wardId: wardId.value,
    employeeId: String(nurse.employeeId),
    id: cell?.id ? String(cell.id) : null,
    nurseName: nurse.nurseName,
    scheduleDate: date,
    scheduleStatus: cell?.scheduleStatus ?? 2,
    // 班次停用后不在矩阵册子里了 → 回显不出来就当没选，提交才不会撞「班次不属于护理册」
    shiftId: cell?.shiftId && shifts.value.some((s) => String(s.shiftId) === String(cell.shiftId)) ? String(cell.shiftId) : null,
    remark: cell?.remark || '',
  };
  cellVisible.value = true;
};
const removeCell = async (cell) => {
  if (!cell?.id)
    return;
  try {
    await ElMessageBox.confirm(`确定删除 ${cell.nurseName || ''} ${cell.scheduleDate} 的排班行？删除后可重新排班。`, '删除排班', {type: 'warning'});
  } catch {
    return;
  }
  const res = await deleteNurseScheduleCell(cell.id);
  
  ElMessage.success(res.message || '已删除');
  cellVisible.value = false;
  await loadMatrix();
};
// ---------------- 复制上周 ----------------
const doCopyWeek = async () => {
  const source = addDays(weekStart.value, -7);
  try {
    await ElMessageBox.confirm(`把 ${source} 那一周的班次复制到 ${weekStart.value} 这一周。只填本周还空着的格子，已排的一律不动。`, '复制上周排班', {type: 'warning'});
  } catch {
    return;
  }
  const res = await copyNurseScheduleWeek({
    unitType: unitType.value,
    wardId: wardId.value,
    sourceWeekStart: source,
    targetWeekStart: weekStart.value
  });
  
  ElMessage.success(res.message || `已复制 ${res.data?.copiedCount ?? 0} 格`);
  await loadMatrix();
};
// ---------------- 规则校验（区间） ----------------
const checkQuery = reactive({startDate: weekStart.value, endDate: weekEnd.value});
const checkResult = ref(null);
const checkLoading = ref(false);
const runCheck = async () => {
  checkLoading.value = true;
  try {
    const res = await checkNurseSchedule({
      unitType: unitType.value,
      wardId: wardId.value,
      startDate: checkQuery.startDate,
      endDate: checkQuery.endDate
    });
    
    checkResult.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    checkLoading.value = false;
  }
};
const WARN_TYPE_TEXT = {
  STAFF_GAP: '人力缺口', STAFF_OVER: '人力超配', WEEK_HOURS: '周工时超限',
  NIGHT_STREAK: '连续夜班超限', WORK_STREAK: '连续上班超限', SHIFT_ON_NON_WORK: '非上班状态挂了班次',
  NOT_SCHEDULED: '漏排',
};
const warnTypeText = (t) => WARN_TYPE_TEXT[t] || t;
// ---------------- 月度工时 ----------------
const workloadMonth = ref(today.slice(0, 7));
const workload = ref(null);
const workloadLoading = ref(false);
const loadWorkload = async () => {
  workloadLoading.value = true;
  try {
    const res = await getNurseMonthWorkload({
      unitType: unitType.value,
      wardId: wardId.value,
      month: workloadMonth.value
    });
    
    workload.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    workloadLoading.value = false;
  }
};
// ---------------- 排班台账 ----------------
const ledger = reactive({
  keyword: '', status: null, dateRange: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const ledgerRows = ref([]);
const ledgerTotal = ref(0);
const ledgerLoading = ref(false);
const loadLedger = async () => {
  ledgerLoading.value = true;
  try {
    const res = await listNurseSchedulePage({
      keyword: ledger.keyword || undefined,
      wardId: wardId.value || undefined,
      scheduleStatus: ledger.status ?? undefined,
      startDate: ledger.dateRange?.[0] || undefined,
      endDate: ledger.dateRange?.[1] || undefined,
      pageNum: ledger.pageNum, pageSize: ledger.pageSize,
    });
    
    ledgerRows.value = res.data?.records || [];
    ledgerTotal.value = Number(res.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    ledgerLoading.value = false;
  }
};
const searchLedger = () => {
  ledger.pageNum = 1;
  loadLedger();
};
// ---------------- 人力配置标准 ----------------
const rules = ref([]);
const ruleLoading = ref(false);
const loadRules = async () => {
  if (!wardId.value)
    return;
  ruleLoading.value = true;
  try {
    const res = await getNurseScheduleRuleList(unitType.value, wardId.value);
    
    rules.value = res.data || [];
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    ruleLoading.value = false;
  }
};
// 标准表单在 NurseScheduleRuleEdit（新增+编辑合并），这里只管开合与保存后的重拉
const ruleVisible = ref(false);
const ruleRow = ref(null);
const openRule = (row) => {
  ruleRow.value = row || null;
  ruleVisible.value = true;
};
const onRuleSaved = async () => {
  await loadRules();
  // 标准变了，本周在岗对照与告警要重算
  if (activeTab.value === 'matrix')
    await loadMatrix();
};
const removeRule = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.shiftName}」这条配置标准？删除后该项校验立即停用。`, '删除标准', {type: 'warning'});
  } catch {
    return;
  }
  const res = await deleteNurseScheduleRule(row.id);
  
  ElMessage.success(res.message || '已删除');
  await loadRules();
  if (activeTab.value === 'matrix')
    await loadMatrix();
};
const reloadAll = () => {
  loadMatrix();
  if (activeTab.value === 'ledger')
    loadLedger();
  if (activeTab.value === 'rules')
    loadRules();
  if (activeTab.value === 'workload')
    loadWorkload();
  if (activeTab.value === 'check')
    runCheck();
};
watch(wardId, async () => {
  checkQuery.startDate = weekStart.value;
  checkQuery.endDate = weekEnd.value;
  await loadMatrix();
  loadGaps();
  loadAttend();
  // 病区就是这一页的数据范围：换病区后各页签先前拉到的数全部作废。
  // 当前页签立刻重拉，其余清空等它下次挂载时按 lazy 再拉，避免停在旧病区的花名册和工时上。
  if (activeTab.value === 'rules')
    loadRules();
  else
    rules.value = [];
  if (activeTab.value === 'workload')
    loadWorkload();
  else
    workload.value = null;
  if (activeTab.value === 'ledger')
    loadLedger();
  else
    ledgerRows.value = [];
  if (activeTab.value === 'check')
    runCheck();
  else
    checkResult.value = null;
});
watch(weekStart, () => {
  checkQuery.startDate = weekStart.value;
  checkQuery.endDate = weekEnd.value;
  loadMatrix();
  loadGaps();
  loadAttend();
  // 校验页签的区间跟着周走，翻周后旧结果同样作废（台账/工时各有自己的区间，不受影响）
  if (activeTab.value === 'check')
    runCheck();
  else
    checkResult.value = null;
});
watch(weekPick, (v) => {
  if (v && v !== weekStart.value)
    setWeek(v);
});
onMounted(async () => {
  statusDict.value = await loadDictDataList(DICT_TYPE.NURSE_SCHEDULE_STATUS);
  calcHeight();
  window.addEventListener('resize', calcHeight);
  await loadUnits();
  if (wardId.value) {
    await loadMatrix();
    loadGaps();
    loadAttend();
    loadRules();
    loadWorkload();
  }
});
onUnmounted(() => window.removeEventListener('resize', calcHeight));
</script>

<style scoped>
:deep(.el-table) {
  --el-table-border-color: #cbd5e1;
}

:deep(.ns-tabs .el-tabs__item) {
  font-size: 16px;
}

:deep(.el-table th.el-table__cell) {
  font-size: 15px;
}

:deep(.filter-row .el-button),
:deep(.filter-row .el-input__inner),
:deep(.filter-row .el-select__placeholder),
:deep(.filter-row .el-select__selected-item) {
  font-size: 15px;
}

/* 矩阵格子：整格可点，班次用色块 + 上班时间；状态（休/假/培/停）用灰块 */
.cell-box {
  min-height: 46px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;
}

.cell-box:hover {
  background: #eff6ff;
}

.cell-box.is-today {
  background: #f0f9ff;
}

.shift-chip {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 18px;
  color: #fff;
}

.status-chip {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 18px;
  color: #475569;
  background: #e2e8f0;
}

.cell-time {
  font-size: 11px;
  color: #94a3b8;
}

/* 人力对照表：负数缺口标红，合计行加粗（数字来自后端，不前端自算） */
.staffing-table {
  border-collapse: collapse;
  width: 100%;
  font-size: 13px;
}

.staffing-table th,
.staffing-table td {
  border: 1px solid #cbd5e1;
  padding: 4px 8px;
  text-align: center;
  white-space: nowrap;
}

.staffing-table th {
  background: #f8fafc;
  font-weight: 500;
  color: #475569;
}

.staffing-table th.left,
.staffing-table td.left {
  text-align: left;
  min-width: 130px;
}

.staffing-table tr.is-total td {
  font-weight: 600;
  background: #f1f5f9;
}

.staffing-table td.is-gap {
  color: #b91c1c;
  background: #fef2f2;
  font-weight: 600;
}

.warn-list {
  margin: 0;
  padding: 0;
  list-style: none;
  max-height: 190px;
  overflow-y: auto;
}

.warn-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 0;
  font-size: 13px;
}

.warn-list li.is-alert span:last-child {
  color: #b91c1c;
}
</style>
