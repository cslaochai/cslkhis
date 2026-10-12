<template>
  <div class="p-5">
    <el-tabs v-model="activeTab" data-testid="icu-tabs" @tab-change="onTabChange">
      <!-- ============ 页签一：床位看板 ============ -->
      <el-tab-pane label="ICU 床位看板" name="board">
        <div class="flex items-center gap-3 mb-3">
          <span class="text-sm text-slate-500">
            开放 {{ beds.length }} 床 · 在科 {{ occupiedBeds.length }} · 空床 {{
              freeBeds.length
            }} · 停用 {{ disabledBeds.length }}
          </span>
          <el-button :icon="Refresh" data-testid="icu-board-refresh" @click="loadBoard">刷新</el-button>
          <el-button v-perm="'ipd:icu:add'" :icon="Plus" data-testid="icu-board-admit-btn" type="primary"
                     @click="openAdmit()">入科登记
          </el-button>
        </div>

        <div v-loading="bedLoading" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3"
             data-testid="icu-board">
          <div v-for="bed in beds" :key="bed.bedId"
               :class="bed.stayId ? 'bg-white border-slate-300' : (Number(bed.bedStatus) === 0 ? 'bg-slate-100 border-slate-200' : 'bg-emerald-50/60 border-emerald-200')"
               :data-testid="`icu-bed-${bed.bedNo}`"
               class="rounded-lg border p-3">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span class="text-base font-semibold text-slate-800">{{ bed.bedNo }}</span>
                <el-tag v-if="bed.careLevel" :type="careLevelTag(bed.careLevel)" size="small">
                  {{ dictText('careLevel', bed.careLevel) }}
                </el-tag>
                <el-tag v-else-if="Number(bed.bedStatus) === 0" size="small" type="info">停用</el-tag>
                <el-tag v-else size="small" type="success">空床</el-tag>
              </div>
              <span class="text-xs text-slate-500">{{ bed.stayNo }}</span>
            </div>

            <template v-if="bed.stayId">
              <div class="text-sm text-slate-700 mb-1">
                {{ bed.patientName }}
                <span class="text-xs text-slate-500 ml-1">{{ bed.patientNo }}</span>
              </div>
              <div class="text-xs text-slate-500 mb-2">入科 {{ bed.inTime }} · GCS {{ bed.inGcs ?? '—' }}</div>
              <div class="grid grid-cols-3 gap-1 text-xs text-slate-600 mb-2">
                <span>T {{ bed.lastTemperature ?? '—' }}</span>
                <span>P {{ bed.lastPulse ?? '—' }}</span>
                <span>SpO2 {{ bed.lastSpo2 ?? '—' }}</span>
                <span>BP {{ bed.lastSbp ?? '—' }}/{{ bed.lastDbp ?? '—' }}</span>
                <span>GCS {{ bed.lastGcsTotal ?? '—' }}</span>
                <span>记录 {{ bed.monitorCount ?? 0 }} 条</span>
              </div>
              <div :class="monitorLagDanger(bed) ? 'text-red-600' : 'text-slate-500'" class="text-xs mb-2">
                记录 {{ bed.monitorCount ?? 0 }} 条 · 上次 {{ monitorLagText(bed) }}
              </div>
              <div class="flex items-center gap-1 flex-wrap">
                <el-button v-perm="'ipd:icu:edit'" data-testid="icu-bed-monitor-btn" link size="small" type="primary"
                           @click="openMonitorCreate(bed.stayId)">登记监护
                </el-button>
                <el-button data-testid="icu-bed-trend-btn" link size="small" type="primary"
                           @click="openTrend(bed)">趋势
                </el-button>
                <el-button data-testid="icu-bed-detail-btn" link size="small" type="primary"
                           @click="openStayView(bed.stayId)">详情
                </el-button>
                <el-button v-perm="'ipd:icu:add'" data-testid="icu-bed-out-btn" link size="small" type="warning"
                           @click="openOut({ id: bed.stayId, stayNo: bed.stayNo, patientName: bed.patientName })">出科
                </el-button>
              </div>
            </template>
            <template v-else>
              <div class="text-xs text-slate-400 mb-2">
                {{ Number(bed.bedStatus) === 0 ? '该床已停用，不可入科' : '空闲可用' }}
              </div>
              <el-button v-if="Number(bed.bedStatus) !== 0" v-perm="'ipd:icu:add'" data-testid="icu-bed-free-admit-btn" link size="small"
                         type="primary" @click="openAdmit(bed)">入科
              </el-button>
            </template>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 页签二：入出科台账 ============ -->
      <el-tab-pane label="入出科台账" name="stays">
        <div class="flex flex-wrap items-center gap-2 mb-3">
          <el-input v-model="stayQuery.stayNo" class="w-40" clearable data-testid="icu-st-no" placeholder="入科单号"/>
          <el-input v-model="stayQuery.patientName" class="w-36" clearable data-testid="icu-st-name"
                    placeholder="患者姓名"/>
          <el-date-picker v-model="stayQuery.startDate" class="w-36" placeholder="入科开始" type="date"
                          value-format="YYYY-MM-DD"/>
          <el-date-picker v-model="stayQuery.endDate" class="w-36" placeholder="入科结束" type="date"
                          value-format="YYYY-MM-DD"/>
          <el-select v-model="stayQuery.careLevel" :fit-input-width="false" class="w-36" clearable
                     placeholder="监护等级">
            <el-option v-for="d in dicts.careLevel" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="stayQuery.status" :fit-input-width="false" class="w-28" clearable placeholder="状态">
            <el-option v-for="d in dicts.stayStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-button :icon="Search" data-testid="icu-st-search" type="primary"
                     @click="stayQuery.pageNum = 1; loadStay()">查询
          </el-button>
          <el-button :icon="Refresh" @click="resetStayQuery">重置</el-button>
          <el-button v-perm="'ipd:icu:add'" :icon="Plus" data-testid="icu-st-add-btn" type="primary"
                     @click="openAdmit()">入科登记
          </el-button>
        </div>

        <el-table v-loading="stayLoading" :data="stayRows" border data-testid="icu-stay-table">
          <el-table-column label="入科单号" prop="stayNo" width="170"/>
          <el-table-column label="患者" width="150">
            <template #default="{ row }">
              <div>{{ row.patientName }}</div>
              <div class="text-xs text-slate-500">{{ row.patientNo }}</div>
            </template>
          </el-table-column>
          <el-table-column label="来源科室" prop="fromDeptName" show-overflow-tooltip width="120"/>
          <el-table-column label="ICU 床位" width="110">
            <template #default="{ row }">{{ row.wardName || '—' }} {{ row.bedNo }}</template>
          </el-table-column>
          <el-table-column label="监护等级" width="100">
            <template #default="{ row }">
              <el-tag :type="careLevelTag(row.careLevel)" size="small">{{
                  dictText('careLevel', row.careLevel)
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="入科时间" prop="inTime" width="160"/>
          <el-table-column label="入科GCS" prop="inGcs" width="90"/>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="stayStatusTag(row.status)" size="small">{{ dictText('stayStatus', row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="出科时间" prop="outTime" width="160">
            <template #default="{ row }">{{ row.outTime || '—' }}</template>
          </el-table-column>
          <el-table-column label="去向" width="110">
            <template #default="{ row }">{{ row.outDest ? dictText('outDest', row.outDest) : '—' }}</template>
          </el-table-column>
          <el-table-column label="转归说明" min-width="160" prop="outReason" show-overflow-tooltip/>
          <el-table-column label="记录数" width="90">
            <template #default="{ row }">{{ row.monitorCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="滞留" width="90">
            <template #default="{ row }">{{ row.stayHours ?? 0 }}h</template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="230">
            <template #default="{ row }">
              <el-button data-testid="icu-st-view-btn" link size="small" type="primary"
                         @click.stop="openStayView(row.id)">详情
              </el-button>
              <el-button v-if="canEditStay(row.status)" v-perm="'ipd:icu:add'" data-testid="icu-st-edit-btn" link size="small"
                         type="primary"
                         @click.stop="openAdmit(null, row)">修改
              </el-button>
              <el-button v-if="canOutStay(row.status)" v-perm="'ipd:icu:add'" data-testid="icu-st-out-btn" link size="small"
                         type="warning"
                         @click.stop="openOut(row)">出科
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="flex justify-end mt-3">
          <el-pagination v-model:current-page="stayQuery.pageNum" v-model:page-size="stayQuery.pageSize"
                         :page-sizes="PAGE_SIZES" :total="stayTotal"
                         layout="total, sizes, prev, pager, next" @size-change="stayQuery.pageNum = 1; loadStay()"
                         @current-change="loadStay"/>
        </div>
      </el-tab-pane>

      <!-- ============ 页签三：监护记录单 ============ -->
      <el-tab-pane label="监护记录单" name="monitors">
        <div class="flex flex-wrap items-center gap-2 mb-3">
          <el-select v-model="monQuery.stayId" :fit-input-width="false" class="w-64" clearable data-testid="icu-mo-stay"
                     filterable
                     placeholder="选择患者">
            <el-option v-for="s in inStays" :key="s.id" :label="`${s.patientName}（${s.bedNo}）${s.stayNo}`"
                       :value="s.id"/>
          </el-select>
          <el-date-picker v-model="monQuery.startDate" class="w-36" placeholder="开始日期" type="date"
                          value-format="YYYY-MM-DD"/>
          <el-date-picker v-model="monQuery.endDate" class="w-36" placeholder="结束日期" type="date"
                          value-format="YYYY-MM-DD"/>
          <el-button :icon="Search" data-testid="icu-mo-search" type="primary"
                     @click="monQuery.pageNum = 1; loadMonitor()">查询
          </el-button>
          <el-button :icon="Refresh" @click="resetMonitorQuery">重置</el-button>
          <el-button v-perm="'ipd:icu:edit'" :icon="Plus" data-testid="icu-mo-add-btn" type="primary"
                     @click="openMonitorCreate()">登记监护记录
          </el-button>
        </div>

        <el-table v-loading="monLoading" :data="monRows" border data-testid="icu-monitor-table" max-height="620">
          <el-table-column label="记录时刻" prop="recordTime" width="160"/>
          <el-table-column label="患者/床位" width="140">
            <template #default="{ row }">{{ row.patientName }} · {{ row.bedNo }}</template>
          </el-table-column>
          <el-table-column label="T" prop="temperature" width="70"/>
          <el-table-column label="P" prop="pulse" width="60"/>
          <el-table-column label="R" prop="respiratory" width="60"/>
          <el-table-column label="BP" width="95">
            <template #default="{ row }">{{ bpText(row) }}</template>
          </el-table-column>
          <el-table-column label="SpO2" prop="spo2" width="70"/>
          <el-table-column label="GCS" width="120">
            <template #default="{ row }">
              <span v-if="row.gcsTotal != null">{{ row.gcsTotal }}（{{ row.gcsEye }}/{{ row.gcsVerbal }}/{{
                  row.gcsMotor
                }}）</span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="瞳孔" prop="pupil" show-overflow-tooltip width="140"/>
          <el-table-column label="呼吸支持" width="150">
            <template #default="{ row }">
              <span v-if="row.ventMode">{{ dictText('ventMode', row.ventMode) }}</span>
              <span v-else class="text-slate-400">—</span>
              <span v-if="row.fio2 != null" class="text-xs text-slate-500 ml-1">FiO2 {{ row.fio2 }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="CVP" prop="cvp" width="70"/>
          <el-table-column label="入/出/平衡" width="150">
            <template #default="{ row }">
              {{ row.intakeMl ?? '—' }} / {{ row.outputMl ?? '—' }} /
              <span :class="Number(row.fluidBalance) > 0 ? 'text-red-600' : 'text-slate-700'">{{
                  row.fluidBalance ?? '—'
                }}</span>
            </template>
          </el-table-column>
          <el-table-column label="尿量" prop="urineMl" width="80"/>
          <el-table-column label="带管" width="150">
            <template #default="{ row }">{{ tubeText(row) }}</template>
          </el-table-column>
          <el-table-column label="病情观察" min-width="180" prop="conditionDesc" show-overflow-tooltip/>
          <el-table-column label="处置" min-width="160" prop="handling" show-overflow-tooltip/>
          <el-table-column label="记录人" prop="recorderName" width="100"/>
          <el-table-column fixed="right" label="操作" width="90">
            <template #default="{ row }">
              <el-button v-if="canWriteMonitor(row.stayStatus)" v-perm="'ipd:icu:edit'" data-testid="icu-mo-edit-btn" link
                         size="small" type="primary" @click.stop="openMonitorEdit(row)">修改
              </el-button>
              <span v-else class="text-xs text-slate-400">已封账</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="flex justify-end mt-3">
          <el-pagination v-model:current-page="monQuery.pageNum" v-model:page-size="monQuery.pageSize"
                         :page-sizes="PAGE_SIZES" :total="monTotal"
                         layout="total, sizes, prev, pager, next" @size-change="monQuery.pageNum = 1; loadMonitor()"
                         @current-change="loadMonitor"/>
        </div>
      </el-tab-pane>

      <!-- ============ 页签四：科室指标 ============ -->
      <el-tab-pane label="科室指标" name="stats">
        <div class="flex flex-wrap items-center gap-2 mb-3">
          <el-date-picker v-model="stRange" class="w-64" data-testid="icu-stats-range" end-placeholder="结束日期"
                          range-separator="至" start-placeholder="开始日期" type="daterange"
                          value-format="YYYY-MM-DD"/>
          <el-input v-model="stLagHours" class="w-40" data-testid="icu-stats-lag" placeholder="漏记阈值(小时)"/>
          <el-button :icon="Search" data-testid="icu-stats-search" type="primary" @click="loadStats">查询</el-button>
          <span class="text-xs text-slate-500">统计窗口最长 30 天，超出按结束日往前推</span>
        </div>

        <div v-if="stats" v-loading="statsLoading" class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4"
             data-testid="icu-stats-cards">
          <div v-for="c in statCards" :key="c.key" class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-xs text-slate-500 mb-1">{{ c.label }}</div>
            <div :class="c.danger ? 'text-red-600' : 'text-slate-800'" :data-testid="`icu-stat-${c.key}`"
                 class="text-xl font-semibold">{{ c.value }}
            </div>
          </div>
        </div>

        <div v-if="stats" class="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-sm font-medium text-slate-700 mb-2">监护等级分布（在科）</div>
            <el-table :data="stats.careLevels || []" border data-testid="icu-stats-carelevel" max-height="240"
                      size="small">
              <el-table-column label="等级" min-width="120">
                <template #default="{ row }">{{ dictText('careLevel', row.type) }}</template>
              </el-table-column>
              <el-table-column label="人数" prop="count" width="90"/>
            </el-table>
          </div>
          <div class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-sm font-medium text-slate-700 mb-2">呼吸支持方式分布（在科最近一条）</div>
            <el-table :data="stats.ventModes || []" border data-testid="icu-stats-vent" max-height="240" size="small">
              <el-table-column label="方式" min-width="140">
                <template #default="{ row }">{{ dictText('ventMode', row.type) }}</template>
              </el-table-column>
              <el-table-column label="人数" prop="count" width="90"/>
            </el-table>
          </div>
          <div class="rounded-lg border border-slate-200 bg-white p-3">
            <div class="text-sm font-medium text-slate-700 mb-2">现带管人数（在科）</div>
            <div class="grid grid-cols-2 gap-2" data-testid="icu-stats-tubes">
              <div v-for="t in tubeCards" :key="t.label" class="rounded border border-slate-200 p-2">
                <div class="text-xs text-slate-500">{{ t.label }}</div>
                <div class="text-lg font-semibold text-slate-800">{{ t.value }}</div>
              </div>
            </div>
            <div class="text-xs text-slate-500 mt-2">区间：{{ stats.startDate }} ~ {{ stats.endDate }}</div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 入科弹框 ============ -->
    <IcuAdmitEdit v-model="admitDialog" :bed="admitBed" :dicts="dicts" :free-beds="freeBeds" :row="admitRow"
                  @saved="refreshStayRelated"/>

    <!-- ============ 出科弹框 ============ -->
    <IcuOutEdit v-model="outDialog" :dicts="dicts" :row="outRow" @saved="refreshStayRelated"/>

    <!-- ============ 入科详情（只读） ============ -->
    <IcuStayDetail v-model="stayDialog" :dicts="dicts" :id="stayViewId"/>

    <!-- ============ 监护记录弹框 ============ -->
    <IcuMonitorEdit v-model="monDialog" :dicts="dicts" :in-stays="inStays" :row="monRow" :stay-id="monStayTarget"
                    @saved="refreshMonitorRelated"/>

    <!-- ============ 监护趋势弹框（只读） ============ -->
    <el-dialog v-model="trendDialog" :title="trendTitle" data-testid="icu-trend-dialog" width="1000px">
      <div class="flex items-center gap-2 mb-2">
        <el-radio-group v-model="trendHours" data-testid="icu-trend-hours" @change="refreshTrend">
          <el-radio-button :value="6">6 小时</el-radio-button>
          <el-radio-button :value="24">24 小时</el-radio-button>
          <el-radio-button :value="72">72 小时</el-radio-button>
          <el-radio-button :value="0">全部</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="trendRows" border data-testid="icu-trend-table" max-height="420">
        <el-table-column label="时刻" prop="recordTime" width="160"/>
        <el-table-column label="T" prop="temperature" width="70"/>
        <el-table-column label="P" prop="pulse" width="60"/>
        <el-table-column label="R" prop="respiratory" width="60"/>
        <el-table-column label="BP" width="95">
          <template #default="{ row }">{{ bpText(row) }}</template>
        </el-table-column>
        <el-table-column label="SpO2" prop="spo2" width="70"/>
        <el-table-column label="GCS" prop="gcsTotal" width="70"/>
        <el-table-column label="呼吸支持" width="150">
          <template #default="{ row }">
            {{ row.ventMode ? dictText('ventMode', row.ventMode) : '—' }}
            <span v-if="row.fio2 != null" class="text-xs text-slate-500">{{ row.fio2 }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="平衡" prop="fluidBalance" width="90"/>
        <el-table-column label="处置" min-width="180" prop="handling" show-overflow-tooltip/>
        <el-table-column label="记录人" prop="recorderName" width="100"/>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="trendDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  getIcuBedBoard,
  getIcuMonitorTrend,
  getIcuStats,
  listIcuMonitorPage,
  listIcuStayPage,
} from '@/api/icu';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {
  bpText,
  canEditStay,
  canOutStay,
  canWriteMonitor,
  careLevelTag,
  STAY_STATUS,
  stayStatusTag,
} from '@/lib/icu';
import IcuAdmitEdit from './IcuAdmitEdit.vue';
import IcuOutEdit from './IcuOutEdit.vue';
import IcuStayDetail from './IcuStayDetail.vue';
import IcuMonitorEdit from './IcuMonitorEdit.vue';

const activeTab = ref('board');
// ---------------- 字典 ----------------
const dicts = reactive({stayStatus: [], careLevel: [], outDest: [], ventMode: []});
const dictText = (key, value) => dictLabelText(dicts[key], value);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList([
      DICT_TYPE.ICU_STAY_STATUS, DICT_TYPE.ICU_CARE_LEVEL, DICT_TYPE.ICU_OUT_DEST, DICT_TYPE.ICU_VENT_MODE,
    ].join(','));
    const map = res?.data || {};
    dicts.stayStatus = map[DICT_TYPE.ICU_STAY_STATUS] || [];
    dicts.careLevel = map[DICT_TYPE.ICU_CARE_LEVEL] || [];
    dicts.outDest = map[DICT_TYPE.ICU_OUT_DEST] || [];
    dicts.ventMode = map[DICT_TYPE.ICU_VENT_MODE] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 页签一：床位看板 ----------------
const bedLoading = ref(false);
const beds = ref([]);
const loadBoard = async () => {
  bedLoading.value = true;
  try {
    const res = await getIcuBedBoard();
    if (res.code === 200) {
      beds.value = res.data || [];
    } else {
      ElMessage.error(res.message || '加载床位看板失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载床位看板失败');
  } finally {
    bedLoading.value = false;
  }
};
const occupiedBeds = computed(() => beds.value.filter((b) => b.stayId));
const freeBeds = computed(() => beds.value.filter((b) => !b.stayId && Number(b.bedStatus) !== 0));
const disabledBeds = computed(() => beds.value.filter((b) => !b.stayId && Number(b.bedStatus) === 0));
/** 距上次监护记录多久（漏记提醒，看板与台账共用） */
const monitorLagText = (row) => {
  if (!row.lastMonitorTime)
    return '尚无记录';
  const diffH = Math.max(0, Math.floor((Date.now() - new Date(String(row.lastMonitorTime).replace(' ', 'T')).getTime()) / 3600000));
  if (diffH < 1)
    return '1 小时内';
  if (diffH < 24)
    return `${diffH} 小时前`;
  return `${Math.floor(diffH / 24)} 天前`;
};
const monitorLagDanger = (row) => {
  if (!row.lastMonitorTime)
    return true;
  const diffH = (Date.now() - new Date(String(row.lastMonitorTime).replace(' ', 'T')).getTime()) / 3600000;
  return diffH >= 8;
};
// ---------------- 页签二：入出科台账 ----------------
const stayLoading = ref(false);
const stayRows = ref([]);
const stayTotal = ref(0);
const stayQuery = reactive({
  stayNo: '',
  patientName: '',
  startDate: '',
  endDate: '',
  careLevel: null,
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const loadStay = async () => {
  stayLoading.value = true;
  try {
    const res = await listIcuStayPage({
      stayNo: stayQuery.stayNo.trim() || undefined,
      patientName: stayQuery.patientName.trim() || undefined,
      startDate: stayQuery.startDate || undefined,
      endDate: stayQuery.endDate || undefined,
      careLevel: stayQuery.careLevel ?? undefined,
      status: stayQuery.status ?? undefined,
      pageNum: stayQuery.pageNum,
      pageSize: stayQuery.pageSize,
    });
    if (res.code === 200) {
      stayRows.value = res.data?.records || [];
      stayTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询入出科台账失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询入出科台账失败');
  } finally {
    stayLoading.value = false;
  }
};
const resetStayQuery = () => {
  stayQuery.stayNo = '';
  stayQuery.patientName = '';
  stayQuery.startDate = '';
  stayQuery.endDate = '';
  stayQuery.careLevel = null;
  stayQuery.status = null;
  stayQuery.pageNum = 1;
  loadStay();
};
/** 在科记录（监护单与看板下拉用，一次性拉满一页） */
const inStays = ref([]);
const loadInStays = async () => {
  try {
    const res = await listIcuStayPage({status: STAY_STATUS.IN, pageNum: 1, pageSize: 100});
    inStays.value = res?.data?.records || [];
  } catch (e) {
    console.error(e);
  }
};
// ---------------- 入科 / 修改弹框 ----------------
const admitDialog = ref(false);
const admitRow = ref(null);
const admitBed = ref(null);
const openAdmit = (bed, row) => {
  admitBed.value = bed ?? null;
  admitRow.value = row ?? null;
  admitDialog.value = true;
};
/** 入科/出科保存后刷新看板、台账与在科名单（沿用原页面的刷新集合） */
const refreshStayRelated = async () => {
  await Promise.all([loadBoard(), loadStay(), loadInStays()]);
};
// ---------------- 出科弹框 ----------------
const outDialog = ref(false);
const outRow = ref(null);
const openOut = (row) => {
  outRow.value = row;
  outDialog.value = true;
};
// ---------------- 入科详情（只读） ----------------
const stayDialog = ref(false);
const stayViewId = ref(null);
const openStayView = (id) => {
  stayViewId.value = id;
  stayDialog.value = true;
};
// ---------------- 页签三：监护记录单 ----------------
const monLoading = ref(false);
const monRows = ref([]);
const monTotal = ref(0);
const monQuery = reactive({stayId: null, startDate: '', endDate: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadMonitor = async () => {
  monLoading.value = true;
  try {
    const res = await listIcuMonitorPage({
      stayId: monQuery.stayId || undefined,
      startDate: monQuery.startDate || undefined,
      endDate: monQuery.endDate || undefined,
      pageNum: monQuery.pageNum,
      pageSize: monQuery.pageSize,
    });
    if (res.code === 200) {
      monRows.value = res.data?.records || [];
      monTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询监护记录失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询监护记录失败');
  } finally {
    monLoading.value = false;
  }
};
const resetMonitorQuery = () => {
  monQuery.stayId = null;
  monQuery.startDate = '';
  monQuery.endDate = '';
  monQuery.pageNum = 1;
  loadMonitor();
};
const monDialog = ref(false);
const monRow = ref(null);
const monStayTarget = ref(null);
const openMonitorCreate = async (stayId) => {
  await loadInStays();
  monRow.value = null;
  monStayTarget.value = stayId ?? monQuery.stayId ?? inStays.value[0]?.id ?? null;
  monDialog.value = true;
};
const openMonitorEdit = (row) => {
  monRow.value = row;
  monDialog.value = true;
};
/** 监护记录保存后刷新记录单、看板与台账（沿用原页面的刷新集合） */
const refreshMonitorRelated = async () => {
  await Promise.all([loadMonitor(), loadBoard(), loadStay()]);
};
const tubeText = (row) => {
  const items = [
    [row.hasAirway, '气道'],
    [row.hasCvc, 'CVC'],
    [row.hasArterial, '动脉'],
    [row.hasCatheter, '尿管'],
    [row.hasDrain, '引流'],
  ].filter((t) => Number(t[0]) === 1).map((t) => t[1]);
  return items.length ? items.join('/') : '—';
};
// ---------------- 趋势弹框 ----------------
const trendDialog = ref(false);
const trendRows = ref([]);
const trendTitle = ref('');
const trendHours = ref(24);
const trendStayId = ref(null);
const openTrend = async (stay) => {
  trendTitle.value = `${stay.patientName || ''} ${stay.bedNo || ''} · 监护趋势`;
  trendStayId.value = stay.id ?? stay.stayId ?? null;
  trendHours.value = 24;
  await refreshTrend();
  trendDialog.value = true;
};
const refreshTrend = async () => {
  if (!trendStayId.value)
    return ElMessage.warning('请先选择入科记录');
  try {
    const res = await getIcuMonitorTrend(trendStayId.value, trendHours.value || undefined);
    if (res.code === 200) {
      trendRows.value = res.data || [];
    } else {
      ElMessage.error(res.message || '查询监护趋势失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询监护趋势失败');
  }
};
// ---------------- 页签四：科室指标 ----------------
const statsLoading = ref(false);
const stats = ref(null);
const stRange = ref([]);
const stLagHours = ref(8);
const loadStats = async () => {
  statsLoading.value = true;
  try {
    const res = await getIcuStats({
      startDate: stRange.value?.[0] || undefined,
      endDate: stRange.value?.[1] || undefined,
      lagHours: stLagHours.value || undefined,
    });
    if (res.code === 200) {
      stats.value = res.data || null;
    } else {
      ElMessage.error(res.message || '查询科室指标失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询科室指标失败');
  } finally {
    statsLoading.value = false;
  }
};
const statCards = computed(() => {
  const s = stats.value || {};
  return [
    {key: 'inBed', label: '当前在科 / 开放床位', value: `${s.inCount ?? 0} / ${s.bedTotal ?? 0}`},
    {key: 'useRate', label: '床位使用率', value: s.bedUseRate == null ? '—' : `${s.bedUseRate}%`},
    {key: 'inOut', label: '区间入科 / 出科', value: `${s.inCountRange ?? 0} / ${s.outCountRange ?? 0}`},
    {key: 'monitorTotal', label: '区间监护记录', value: s.monitorTotalRange ?? 0},
    {key: 'perStay', label: '人均记录条数', value: s.monitorsPerStay ?? '—'},
    {key: 'avgStay', label: '平均滞留小时', value: s.avgStayHours ?? '—'},
    {key: 'death', label: '死亡人数', value: s.deathCount ?? 0},
    {
      key: 'lag',
      label: `漏记预警（≥${stLagHours.value}h）`,
      value: s.monitorLagCount ?? 0,
      danger: Number(s.monitorLagCount || 0) > 0
    },
  ];
});
const tubeCards = computed(() => {
  const s = stats.value || {};
  return [
    {label: '人工气道', value: s.airwayCount ?? 0},
    {label: '中心静脉', value: s.cvcCount ?? 0},
    {label: '动脉置管', value: s.arterialCount ?? 0},
    {label: '导尿管', value: s.catheterCount ?? 0},
    {label: '引流管', value: s.drainCount ?? 0},
  ];
});
const onTabChange = (name) => {
  if (name === 'board')
    loadBoard();
  if (name === 'stays')
    loadStay();
  if (name === 'monitors') {
    loadInStays();
    loadMonitor();
  }
  if (name === 'stats')
    loadStats();
};
onMounted(async () => {
  await loadDicts();
  await loadBoard();
});
</script>
