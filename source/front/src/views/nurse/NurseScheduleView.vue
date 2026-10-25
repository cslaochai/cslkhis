<template>
  <div class="w-full">
    <!-- 顶部统计卡片：跟门诊排班一样，放在最上面 -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-4" data-testid="nurse-stats">
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default">
        <p class="text-xl font-semibold text-slate-800">
          {{ statServed }}<span class="text-sm font-normal text-slate-400"> / {{ statExpected }}</span>
        </p>
        <p class="text-xs text-slate-400">应排/实排 · 覆盖 {{ statCoveragePct }}%</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default">
        <p class="text-xl font-semibold text-blue-600">{{ statPlannedDays }}</p>
        <p class="text-xs text-slate-400">已排班天数</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default">
        <p class="text-xl font-semibold text-slate-800">{{ statTotalShifts }}</p>
        <p class="text-xs text-slate-400">总排班人次</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          @click="showWarnings = true" style="cursor: pointer">
        <p :class="weekAlerts.length > 0 ? 'text-xl font-semibold text-red-500' : 'text-xl font-semibold text-emerald-600'">{{ weekAlerts.length }}</p>
        <p class="text-xs text-slate-400">规则告警条数（点我查看）</p>
      </div>
    </div>

    <!-- 今日在岗横幅：跟门诊一样，显示当前病区的在岗护士 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm" data-testid="ns-on-duty-panel">
      <div class="mb-2 flex flex-wrap items-center gap-3">
        <span class="text-base font-medium text-slate-700">今日在岗</span>
        <span class="text-xs text-slate-400">
          {{ wardName || '未选病区' }} · 共 {{ onDutyNurseList.length }} 人在岗
        </span>
        <div class="ml-auto flex items-center gap-2">
          <el-button :icon="Refresh" size="small" @click="loadOnDutyNurses">刷新</el-button>
        </div>
      </div>
      <div v-if="onDutyNurseList.length" class="flex flex-wrap items-center gap-2">
        <el-tag v-for="n in onDutyNurseList" :key="n.scheduleId"
                effect="light" size="large" type="success">
          {{ n.employeeName || n.staffName }}
        </el-tag>
      </div>
      <div v-else class="py-2 text-sm text-slate-400">暂无在岗护士</div>
    </div>

    <el-card shadow="never" style="height: calc(100vh - 260px); min-height: 400px;">
      <el-tabs v-model="activeNurseTab" class="h-full flex flex-col">
        <!-- ===================== 周排班矩阵 ===================== -->
        <el-tab-pane label="周排班矩阵" name="matrix">
        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <!-- 搜索条件行：和表格在同一个卡片里 -->
          <div class="flex flex-wrap items-center gap-3 border-b border-slate-200 px-4 py-3">
            <span class="text-base font-medium text-slate-700">排班单元</span>
            <el-select v-model="unitKey" :fit-input-width="false" :clearable="false" class="!w-64" data-testid="ns-ward"
                       filterable placeholder="选择排班单元（病区 / 门诊科室）">
              <el-option v-for="u in units" :key="`${u.unitType}:${u.wardId}`"
                         :label="`${u.unitType === 2 ? '门诊' : '病区'} · ${u.wardName}（在册 ${u.nurseCount} 人）`"
                         :value="`${u.unitType}:${u.wardId}`"/>
            </el-select>
            <span class="text-base font-medium text-slate-700">护士</span>
            <el-select v-model="filterNurseId" :fit-input-width="false" class="!w-40" clearable
                       filterable placeholder="全部护士">
              <el-option v-for="n in (matrix?.nurses || [])" :key="n.employeeId"
                         :label="n.employeeName" :value="n.employeeId"/>
            </el-select>
            <span class="text-base font-medium text-slate-700">周</span>
            <div class="flex items-center gap-2">
              <el-button @click="shiftWeek(-1)"><span class="px-0.5 text-base leading-none">‹</span>上一周</el-button>
              <span class="min-w-[200px] text-center text-base font-medium text-slate-600">{{ weekStart }} ~ {{ weekEnd }}</span>
              <el-button @click="shiftWeek(1)">下一周<span class="px-0.5 text-base leading-none">›</span></el-button>
              <el-button link type="primary" @click="goThisWeek">回本周</el-button>
            </div>
            <span class="ml-auto flex items-center gap-2">
              <el-button :icon="Refresh" :loading="matrixLoading" type="primary" @click="reloadAll">查询</el-button>
              <el-button v-perm="PERM_ADD" :icon="MagicStick" type="primary" plain @click="handleGenerateFromTemplate">按模板生成下周</el-button>
              <el-button v-perm="PERM_ADD" :icon="CopyDocument" data-testid="ns-copy-week" type="success" @click="doCopyWeek">
                复制到下周
              </el-button>
              <el-button v-perm="PERM_ADD" :icon="Plus" data-testid="ns-batch-open" type="primary" plain @click="openBatch">
                批量铺班
              </el-button>
              <el-button v-perm="PERM_ADD" type="warning" :disabled="!pubSummary.draft"
                         data-testid="ns-submit-week" @click="doSubmitWeek">整周提交审核</el-button>
              <el-button v-perm="PERM_AUDIT" type="success" :disabled="!pubSummary.pending"
                         data-testid="ns-audit-week" @click="doAuditWeek">整周审核发布</el-button>
            </span>
          </div>

          <!-- 人力缺口提示：只显示哪天缺人，其他统计放排班总览 -->
          <div v-if="gaps.length" class="flex flex-wrap items-center gap-x-4 gap-y-1 border-b border-amber-100 bg-amber-50 px-4 py-2 text-sm"
               data-testid="ns-gap-tip">
            <span class="font-medium text-amber-700">人力缺口</span>
            <span v-for="d in (matrix?.days || [])" :key="d" class="flex items-center gap-1">
              <span class="text-slate-400">{{ d.slice(5) }}</span>
              <span v-if="gapOf(d) && Number(gapOf(d).gapCount) > 0" class="text-red-600 font-medium">
                缺 {{ gapOf(d).gapCount }} 人
              </span>
              <span v-else-if="gapOf(d) && Number(gapOf(d).gapCount) < 0" class="text-emerald-600">
                富余 {{ -Number(gapOf(d).gapCount) }}
              </span>
              <span v-else-if="gapOf(d)" class="text-emerald-600">已满</span>
            </span>
            <el-button v-perm="PERM_EDIT" :loading="gapLoading" size="small" text @click="doRecalcDemand">重算需求</el-button>
          </div>

          <div class="p-4">
          <el-table v-loading="matrixLoading" :data="matrixRows" :height="matrixTableHeight" border
                    data-testid="ns-matrix"
                    size="small" style="width: 100%">
            <el-table-column fixed label="护士" width="160">
              <template #default="{ row }">
                <div class="leading-tight">
                  <span class="font-bold text-base text-slate-900">{{ row.nurseName }}</span>
                  <span class="ml-1 text-xs text-slate-500">{{ row.nurseTitle }}</span>
                  <p class="text-xs text-slate-400">{{ row.empCode }}</p>
                </div>
              </template>
            </el-table-column>
            <el-table-column v-for="(d, i) in (matrix?.days || [])" :key="d" :label="dayHead(d)" align="center"
                             min-width="92">
              <template #header>
                <span :class="d === today ? 'font-semibold text-blue-700' : ''">{{ dayHead(d) }}</span>
              </template>
              <template #default="{ row }">
                <div :class="['cell-box', { 'is-today': d === today, 'is-readonly': !canAdd }]"
                     data-testid="ns-cell" @click="openCell(row, d)">
                  <template v-if="row.dayCells[i]">
                    <span v-if="row.dayCells[i].scheduleStatus === 1"
                          :style="{ background: shiftColor(row.dayCells[i].shiftId) }"
                          class="shift-chip">
                      {{ shortShift(row.dayCells[i]) }}
                    </span>
                    <span v-else class="status-chip">{{
                        (row.dayCells[i].scheduleStatusText || '—').slice(0, 2)
                      }}</span>
                    <p v-if="row.dayCells[i].scheduleStatus === 1" class="cell-time">{{ row.dayCells[i].startTime }}</p>
                    <!-- 发布态角标：一格就是一行排班，文案直接吃后端 publishStatusText（前端不抄第二份码表）。
                         草稿/待审核/调班中都不计在岗，所以这个标记是「这天到底算不算人」的唯一线索 -->
                    <span v-if="row.dayCells[i].publishStatusText"
                          :class="`publish-mark is-${publishStatusTagType(row.dayCells[i].publishStatus)}`"
                          :data-testid="`nurse-cell-publish-${row.dayCells[i].id}`"
                          :title="`发布态：${row.dayCells[i].publishStatusText}（点格子可提交/审核/调班）`">{{
                        row.dayCells[i].publishStatusText
                      }}</span>
                    <!-- 兼派公共工位角标：病区班次照排，仅标注当天另派到的公共位（独立派岗表） -->
                    <span v-if="stationOf(row.employeeId, d)" class="station-mark"
                          :data-testid="`nurse-cell-station-${row.employeeId}-${d}`"
                          :title="`兼派公共工位：${stationOf(row.employeeId, d).stationName}`">
                      {{ stationOf(row.employeeId, d).stationName }}
                    </span>
                  </template>
                  <!-- 空格子：有权限时用淡色 + 号暗示「点这里排班」，只读仍显示占位符 -->
                  <el-icon v-else-if="canAdd" class="cell-plus" :size="11" data-testid="ns-cell-add"><Plus/></el-icon>
                  <span v-else class="text-slate-300">—</span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空周引导：本周一行都没排时，告诉用户从哪下手（点格 / 复制上周），避免对着满屏「—」无从开始 -->
          <div v-if="canAdd && weekEmpty" class="mt-3 rounded-md border border-dashed border-blue-200 bg-blue-50 px-4 py-3 text-sm text-slate-600"
               data-testid="ns-empty-week">
            本周还没有排班：点击格子为护士逐个安排班次，或点上方「复制上周」把上周班次整套带过来再微调。
          </div>
        </div>
        </div>
        </el-tab-pane>

        <!-- ===================== 排班模板 ===================== -->
        <el-tab-pane label="排班模板" name="template">
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <el-empty description="排班模板：预设常用排班模式（APN / 8-4 / 小组制等），一键应用到本周"/>
          </div>
        </el-tab-pane>

        <!-- ===================== 派岗记录 ===================== -->
        <el-tab-pane label="派岗记录" name="assign">
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <el-form :model="assignSearch" inline class="mb-3">
              <el-form-item label="日期">
                <el-date-picker v-model="assignDateRange" type="daterange" range-separator="至"
                                start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD"
                                style="width: 260px"/>
              </el-form-item>
              <el-form-item label="人员">
                <el-input v-model="assignKeyword" clearable placeholder="姓名/工号" style="width: 130px"/>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadAssignRecords">查询</el-button>
              </el-form-item>
            </el-form>
            <el-table v-loading="assignLoading" :data="assignRecords" border stripe size="small">
              <el-table-column label="日期" prop="assignDate" width="110"/>
              <el-table-column label="工位" prop="stationName" min-width="140"/>
              <el-table-column label="类别" prop="stationKindText" width="100"/>
              <el-table-column label="人员" min-width="130">
                <template #default="{ row }">{{ row.employeeName }}（{{ row.empCode || '—' }}）</template>
              </el-table-column>
              <el-table-column label="班次" min-width="170">
                <template #default="{ row }">
                  <span v-if="row.shiftName">{{ row.shiftName }} {{ row.startTime }}~{{ row.endTime }}</span>
                  <span v-else class="text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column label="借调" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.isLoan ? 'warning' : 'info'" size="small">{{ row.isLoan ? '借调' : '本岗' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!assignLoading && assignRecords.length === 0" description="暂无派岗记录"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 规则告警详情弹框 -->
    <el-dialog v-model="showWarnings" title="本周规则告警详情" width="600px">
      <el-empty v-if="!weekAlerts.length" description="本周没有规则告警" />
      <ul v-else class="warn-list">
        <li v-for="(w, i) in matrix?.warnings || []" :key="i"
            :class="Number(w.level) === 1 ? 'is-alert' : 'is-hint'">
          <el-tag :type="Number(w.level) === 1 ? 'danger' : 'info'" effect="plain" size="small">
            {{ warnTypeText(w.type) }}
          </el-tag>
          <span class="text-slate-500">{{ w.scheduleDate || '整周' }}</span>
          <span>{{ w.message }}</span>
        </li>
      </ul>
    </el-dialog>

    <!-- 点格排班弹窗：表单与保存在 NurseScheduleCellEdit；发布态动作回传本页执行 -->
    <NurseScheduleCellEdit v-model="cellVisible" :row="cellRow" :shifts="shifts" :status-options="statusOptions"
                           :unit-type="unitType" :station-options="stationOptions"
                           :source-org-name="matrix?.deptName || ''"
                           @delete="removeCell" @saved="loadMatrix"
                           @transition="onCellTransition"/>

    <!-- 批量铺班弹框：共用「状态+班次」× 勾选日期 × 多选护士，只填空缺格、已排的不覆盖 -->
    <el-dialog v-model="batchVisible" destroy-on-close title="批量铺班" width="620px" data-testid="ns-batch-dialog">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="排班状态" required>
          <el-radio-group v-model="batchForm.scheduleStatus" data-testid="ns-batch-status" @change="onBatchStatusChange">
            <el-radio v-for="o in statusOptions" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :required="batchForm.scheduleStatus === 1" label="班次（仅「上班」需要）">
          <el-select v-model="batchForm.shiftId" :disabled="batchForm.scheduleStatus !== 1" :fit-input-width="false"
                     class="w-full" data-testid="ns-batch-shift" placeholder="选择护理班次">
            <el-option v-for="s in shifts" :key="s.shiftId"
                       :label="`${s.shiftName} ${s.startTime}~${s.endTime}`" :value="String(s.shiftId)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="铺到哪几天（勾选本周日期，只填空格、已排的跳过不覆盖）" required>
          <el-checkbox-group v-model="batchForm.dates" data-testid="ns-batch-dates" class="flex flex-wrap gap-x-4 gap-y-2">
            <el-checkbox v-for="(d, i) in (matrix?.days || [])" :key="d" :value="d">
              {{ dayHead(d) }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="铺给哪些护士（多选）" required>
          <div class="mb-1 flex gap-2 text-xs">
            <el-button link type="primary" size="small" @click="batchSelectAllNurses">全选</el-button>
            <el-button link type="info" size="small" @click="batchForm.employeeIds = []">清空</el-button>
          </div>
          <el-select v-model="batchForm.employeeIds" multiple filterable collapse-tags collapse-tags-tooltip
                     class="w-full" data-testid="ns-batch-nurses" placeholder="勾选要统一铺班的护士">
            <el-option v-for="n in (matrix?.nurses || [])" :key="n.employeeId"
                       :label="`${n.nurseName}（${n.empCode}）`" :value="String(n.employeeId)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注（统一说明，可空）">
          <el-input v-model="batchForm.remark" :rows="2" maxlength="500" type="textarea" show-word-limit/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="batchSaving" data-testid="ns-batch-submit" type="primary" @click="submitBatch">
          开始铺班
        </el-button>
        <el-button @click="batchVisible = false">取消</el-button>
      </template>
    </el-dialog>

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
import {ArrowLeft, CopyDocument, MagicStick, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  adjustStartNurseSchedule,
  auditPublishNurseSchedule,
  batchUpsertNurseSchedule,
  checkNurseSchedule,
  copyNurseScheduleWeek,
  deleteNurseScheduleCell,
  deleteNurseScheduleRule,
  getNurseMonthWorkload,
  getNurseScheduleRuleList,
  getNurseUnitSelectList,
  getNurseWeekMatrix,
  listNurseScheduleLogPage,
  listNurseSchedulePage,
  recallNurseSchedule,
  submitNurseSchedule,
  submitWeekNurseSchedule,
  auditWeekNurseSchedule,
} from '@/api/nurseSchedule';
import {getStaffDemandGapList, recalcStaffDemand} from '@/api/staffDemand';
import {getAttendanceAdvice, getAttendanceSummary} from '@/api/staffAttendance';
import {getOpdAssignPage, getOpdStationSelectList} from '@/api/opdStation';
import {getShiftSelectList} from '@/api/appoint';
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {hasPerm} from '@/lib/perm';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {publishStatusTagType} from '@/lib/publishStatus';
import {
  canAdjustPublish,
  canAuditPublish,
  canRecallPublish,
  canSubmitPublish,
  PERM_ADD,
  PERM_AUDIT,
  PERM_EDIT,
  PERM_DELETE,
} from '@/lib/nurseSchedule';
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
const activeNurseTab = ref('matrix');
const showWarnings = ref(false);
const filterNurseId = ref(null);

// ---------------- 今日在岗护士 ----------------
const onDutyNurseList = ref([]);
const wardName = ref('');
const loadOnDutyNurses = async () => {
  // 占位：后续接入真实在岗接口
  onDutyNurseList.value = [];
};
const weekOffset = ref(0);

// ---------------- 派岗记录 ----------------
const assignSearch = ref({});
const assignDateRange = ref(null);
const assignKeyword = ref('');
const assignLoading = ref(false);
const assignRecords = ref([]);
const loadAssignRecords = async () => {
  // 占位：后续接入真实派岗接口
  assignLoading.value = true;
  try {
    assignRecords.value = [];
  } finally {
    assignLoading.value = false;
  }
};
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
    await loadStationAssignments();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    matrixLoading.value = false;
  }
};
// ---------------- 兼派公共工位（独立派岗表，仅角标回显，不并入出勤底座） ----------------
const stationOptions = ref([]);
const stationMap = ref({});
const stationKey = (employeeId, date) => `${String(employeeId)}|${date}`;
const stationOf = (employeeId, date) => stationMap.value[stationKey(employeeId, date)] || null;
const loadStationAssignments = async () => {
  if (!stationOptions.value.length) {
    try {
      const sRes = await getOpdStationSelectList({});
      stationOptions.value = sRes.data || [];
    } catch {
      stationOptions.value = [];
    }
  }
  stationMap.value = {};
  try {
    const res = await getOpdAssignPage({
      startDate: weekStart.value, endDate: weekEnd.value, pageNum: 1, pageSize: 300,
    });
    (res.data?.records || []).forEach((r) => {
      if (Number(r.status) !== 1) return;
      stationMap.value[stationKey(r.employeeId, r.assignDate)] = r;
    });
  } catch {
    stationMap.value = {};
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
/** 班次字典映射：shiftId -> {color, shortName}，从 /shift/selectList 拉，替代硬编码循环色 */
const shiftMap = ref({});
const loadShiftMap = async () => {
  try {
    const res = await getShiftSelectList({useScope: 2, status: 1});
    const list = res?.data || [];
    const map = {};
    list.forEach((s) => { map[String(s.id)] = s; });
    shiftMap.value = map;
  } catch (e) { /* 拉不到就 fallback 到老循环色 */ }
};
/** 班次色卡：优先用班次字典声明的 color，没有再按数组顺序兜底（旧逻辑） */
const SHIFT_COLORS = ['#1269B5', '#0E9488', '#7C3AED', '#B45309', '#BE123C', '#475569', '#15803D', '#0369A1'];
const shiftColor = (shiftId) => {
  const hit = shiftMap.value[String(shiftId)];
  if (hit?.color) return hit.color;
  const idx = shifts.value.findIndex((s) => String(s.shiftId) === String(shiftId));
  return SHIFT_COLORS[(idx < 0 ? 0 : idx) % SHIFT_COLORS.length];
};
/** 班次缩写：优先用字典 shortName，没有再从名字里剥"护理"前缀 */
const shortShift = (cell) => {
  const hit = shiftMap.value[String(cell?.shiftId)];
  if (hit?.shortName) return hit.shortName;
  return String(cell?.shiftName || '').replace(/^护理/, '');
};
const cellOf = (employeeId, date) => (matrix.value?.cells || []).find((c) => String(c.employeeId) === String(employeeId) && c.scheduleDate === date);
/** 表格行：一行一个护士，带上该护士的 7 个格子（el-table 只认扁平行） */
const matrixRows = computed(() => (matrix.value?.nurses || []).map((n) => ({
  ...n,
  dayCells: (matrix.value?.days || []).map((d) => cellOf(n.employeeId, d) || null),
})));
/** 是否有排班写权限：决定格子显示 + 号、可否点开编辑，只读态不诱导点击 */
const canAdd = computed(() => hasPerm(PERM_ADD));
/** 本周是否一行排班都没有：用于空周引导条（已加载、有在册护士，但格子全空） */
const weekEmpty = computed(() =>
  !!matrix.value && (matrix.value.nurses || []).length > 0 &&
  matrixRows.value.every((r) => !r.dayCells.some(Boolean))
);
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

// ---------------- 顶部统计卡片 ----------------
/** 应排护士数 = 病区在册护士总数 */
const statExpected = computed(() => matrix.value?.nurses?.length || 0);
/** 实排护士数 = 本周至少有一个班次的护士数 */
const statServed = computed(() => {
  const nurses = matrix.value?.nurses || [];
  return nurses.filter(n => (n.days || []).some(d => d && d.status === 1)).length;
});
/** 覆盖率 */
const statCoveragePct = computed(() => {
  if (!statExpected.value) return 0;
  return Math.round(statServed.value / statExpected.value * 100);
});
/** 已排班天数（护士×日期去重） */
const statPlannedDays = computed(() => {
  let count = 0;
  const nurses = matrix.value?.nurses || [];
  nurses.forEach(n => (n.days || []).forEach(d => { if (d && d.status === 1) count++; }));
  return count;
});
/** 总排班人次 */
const statTotalShifts = computed(() => statPlannedDays.value);

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
  if (t === 'logs' && !logRows.value.length)
    loadLogs();
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
  const st = stationOf(nurse.employeeId, date);
  cellRow.value = {
    wardId: wardId.value,
    employeeId: String(nurse.employeeId),
    id: cell?.id ? String(cell.id) : null,
    nurseName: nurse.nurseName,
    scheduleDate: date,
    scheduleStatus: cell?.scheduleStatus ?? 2,
    // 兼派公共工位回显：已派岗则带上工位与派岗记录ID，清空时据此撤销
    stationId: st ? String(st.stationId) : null,
    assignId: st ? String(st.id) : null,
    // 班次停用后不在矩阵册子里了 → 回显不出来就当没选，提交才不会撞「班次不属于护理册」
    shiftId: cell?.shiftId && shifts.value.some((s) => String(s.shiftId) === String(cell.shiftId)) ? String(cell.shiftId) : null,
    remark: cell?.remark || '',
    // 发布态只读带上：弹框据此决定「提交审核/审核发布/进入调班/撤回」出不出现在footer
    publishStatus: cell?.publishStatus ?? null,
    publishStatusText: cell?.publishStatusText || '',
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
// ---------------- 发布态流转（G-15：草稿→待审核→已发布→调班中→已变更，任意态可撤回） ----------------
/**
 * 四个动作的单一执行口：台账表格与点格弹框都走这里，权限与状态的门槛在按钮的 v-if / v-perm 上，
 * 这里只管「调哪个接口 + 刷哪份数」。按钮出现与否的判定全在 lib/nurseSchedule，两处共用一份。
 */
const PUBLISH_ACTIONS = {
  submit: {api: submitNurseSchedule, success: '已提交审核'},
  audit: {api: auditPublishNurseSchedule, success: '已发布'},
  adjust: {api: adjustStartNurseSchedule, success: '已进入调班'},
  recall: {api: recallNurseSchedule, success: '已撤回'},
};
const runPublishAction = async (action, cell) => {
  const cfg = PUBLISH_ACTIONS[action];
  if (!cfg || !cell?.id)
    return;
  // 撤回只提示不拦人，但它是唯一会「悄悄把人从在岗里摘掉」的动作，必须确认
  if (action === 'recall') {
    try {
      await ElMessageBox.confirm(`撤回 ${cell.nurseName || ''} ${cell.scheduleDate || ''} 的排班会把它退回草稿：撤回后这条排班不再计入在岗，本周需求对照会立刻少一个人。确定撤回？`, '撤回排班', {type: 'warning'});
    } catch {
      return;
    }
  }
  try {
    const res = await cfg.api(cell.id);
    ElMessage.success(res.message || cfg.success);
  } catch {
    /* 错误已由全局拦截器统一提示，无需在此弹窗 */
  }
  cellVisible.value = false;
  await loadMatrix();
  // 台账已经拉过数就得跟着刷，否则停在旧态的按钮会让人对着同一行连点两次
  if (ledgerRows.value.length)
    await loadLedger();
};
/** 弹框只回传意图（action + 装配好的格子），请求与刷新统一在本页 */
const onCellTransition = ({action, row}) => runPublishAction(action, row);

// ---------------- 整周批量发布（草稿整周提交 / 待审核整周发布，单格微调仍走上面的逐条流转） ----------------
const EMPTY_SUMMARY = {draft: 0, pending: 0, published: 0, adjusting: 0, changed: 0, total: 0};
const pubSummary = computed(() => matrix.value?.publishSummary || EMPTY_SUMMARY);
const weekPayload = () => ({unitType: unitType.value, wardId: wardId.value, weekStart: weekStart.value});
const doSubmitWeek = async () => {
  try {
    await ElMessageBox.confirm(`把本周 ${pubSummary.value.draft} 条草稿一次性提交审核；提交后仍是待审核，审核发布后才计在岗。确定提交？`, '整周提交审核', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await submitWeekNurseSchedule(weekPayload());
    ElMessage.success(res.message || '已提交审核');
    await reloadAll();
  } catch {
    /* 错误已由全局拦截器统一提示 */
  }
};
const doAuditWeek = async () => {
  try {
    await ElMessageBox.confirm(`把本周 ${pubSummary.value.pending} 条待审核一次性审核发布；发布后立即计入在岗。确定发布？`, '整周审核发布', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await auditWeekNurseSchedule(weekPayload());
    ElMessage.success(res.message || '已发布');
    await reloadAll();
  } catch {
    /* 错误已由全局拦截器统一提示 */
  }
};

// ---------------- 复制上周 ----------------
const handleGenerateFromTemplate = async () => {
  // 占位：后续接入模板生成接口
  ElMessage.info('按模板生成下周排班功能开发中...');
};
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
// ---------------- 批量铺班（共用状态/班次 × 勾选日期 × 多选护士，只填空缺格） ----------------
const batchVisible = ref(false);
const batchSaving = ref(false);
const batchForm = reactive({scheduleStatus: 1, shiftId: null, dates: [], employeeIds: [], remark: ''});
const openBatch = () => {
  batchForm.scheduleStatus = 1;
  batchForm.shiftId = null;
  batchForm.dates = [...(matrix.value?.days || [])];
  batchForm.employeeIds = [];
  batchForm.remark = '';
  batchVisible.value = true;
};
const onBatchStatusChange = (v) => {
  if (Number(v) !== 1)
    batchForm.shiftId = null;
};
const batchSelectAllNurses = () => {
  batchForm.employeeIds = (matrix.value?.nurses || []).map((n) => String(n.employeeId));
};
const submitBatch = async () => {
  if (Number(batchForm.scheduleStatus) === 1 && !batchForm.shiftId) {
    ElMessage.warning('排「上班」必须选班次');
    return;
  }
  if (!batchForm.dates.length) {
    ElMessage.warning('请勾选要铺到的日期');
    return;
  }
  if (!batchForm.employeeIds.length) {
    ElMessage.warning('请勾选要铺班的护士');
    return;
  }
  batchSaving.value = true;
  try {
    const res = await batchUpsertNurseSchedule({
      unitType: unitType.value,
      wardId: wardId.value,
      scheduleStatus: batchForm.scheduleStatus,
      shiftId: Number(batchForm.scheduleStatus) === 1 ? batchForm.shiftId : null,
      scheduleDates: batchForm.dates,
      items: batchForm.employeeIds.map((id) => ({employeeId: Number(id)})),
      remark: batchForm.remark || undefined,
    });
    ElMessage.success(res.data?.message || res.message || '批量铺班完成');
    batchVisible.value = false;
    await loadMatrix();
  } catch {
    /* 错误已由全局拦截器统一提示 */
  } finally {
    batchSaving.value = false;
  }
};

// ---------------- 审批记录（提交/审核发布/撤回/调班/改格/删格/整周流转留痕，只写不删） ----------------
const logQuery = reactive({nurseName: '', dateRange: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const logRows = ref([]);
const logTotal = ref(0);
const logLoading = ref(false);
const loadLogs = async () => {
  logLoading.value = true;
  try {
    const res = await listNurseScheduleLogPage({
      deptId: matrix.value?.deptId || undefined,
      nurseName: logQuery.nurseName || undefined,
      startDate: logQuery.dateRange?.[0] || undefined,
      endDate: logQuery.dateRange?.[1] || undefined,
      pageNum: logQuery.pageNum,
      pageSize: logQuery.pageSize,
    });
    logRows.value = res.data?.records || [];
    logTotal.value = Number(res.data?.total || 0);
  } catch {
    /* 错误已由全局拦截器统一提示 */
  } finally {
    logLoading.value = false;
  }
};
const searchLogs = () => {
  logQuery.pageNum = 1;
  loadLogs();
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
  if (activeTab.value === 'logs')
    loadLogs();
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
  if (activeTab.value === 'logs')
    loadLogs();
  else
    logRows.value = [];
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
  loadShiftMap();
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
  min-height: 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
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

/* 只读态：不诱导点击，光标默认、hover 不变色 */
.cell-box.is-readonly {
  cursor: default;
}

.cell-box.is-readonly:hover {
  background: transparent;
}

/* 空格子的 + 号：淡色小巧，hover 格子时加深变蓝，明确「可点」。
   el-icon 容器固定尺寸，内部 svg 跟随，避免裸 SVG 被 flex 拉伸撑满格子 */
.cell-plus {
  width: 14px;
  height: 14px;
  color: #94a3b8;
  transition: color 0.15s;
}

.cell-plus svg {
  width: 14px;
  height: 11px;
}

.cell-box:hover .cell-plus {
  color: #3b82f6;
}

/* 图例小色块 */
.legend-swatch {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-swatch.legend-mark {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
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

/* 发布态角标：格子只有 104px 宽，用比班次更小的字号；颜色跟 el-tag 同一套码值口径（lib/publishStatus） */
.publish-mark {
  display: inline-block;
  padding: 0 6px;
  border-radius: 8px;
  font-size: 10px;
  line-height: 14px;
  white-space: nowrap;
  background: #f1f5f9;
  color: #64748b;
}

.publish-mark.is-warning {
  background: #fef3c7;
  color: #92400e;
}

.publish-mark.is-success {
  background: #d1fae5;
  color: #065f46;
}

.publish-mark.is-danger {
  background: #fee2e2;
  color: #991b1b;
}

.publish-mark.is-primary {
  background: #dbeafe;
  color: #1e40af;
}

/* 兼派公共工位角标：与发布态角标并列，青色底以示区别；长工位名省略，全称看悬浮 title */
.station-mark {
  display: inline-block;
  max-width: 100%;
  padding: 0 6px;
  border-radius: 8px;
  font-size: 10px;
  line-height: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: #cffafe;
  color: #155e75;
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
