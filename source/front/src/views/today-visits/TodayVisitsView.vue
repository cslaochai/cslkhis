<template>
  <div v-loading="loading">
    <!-- 统计条（跟随筛选，口径与列表同一套 WHERE） -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <div class="grid grid-cols-4 gap-3 lg:grid-cols-11">
        <div v-for="c in statCards()" :key="c.label" class="text-center">
          <p :class="['text-xl font-bold leading-tight', c.color]">{{ statsLoaded ? c.value : '—' }}</p>
          <p class="mt-0.5 text-xs text-slate-500">{{ c.label }}</p>
        </div>
      </div>
      <div
          class="mt-2 flex flex-wrap items-center gap-x-6 gap-y-1 border-t border-slate-100 pt-2 text-xs text-slate-500">
        <span>平均候诊 <b class="text-slate-700">{{ statsLoaded ? stats.avgWaitMinutes : '—' }}</b> 分</span>
        <span>平均就诊 <b class="text-slate-700">{{ statsLoaded ? stats.avgVisitMinutes : '—' }}</b> 分</span>
        <span class="text-slate-400">（时长按「到达→开始 / 开始→结束」现算，跨天异常值已剔除）</span>
        <el-button :icon="Refresh" class="ml-auto" size="small" @click="settleDialog = true">日终结转</el-button>
      </div>
    </div>

    <TodayVisitsSettleEdit v-model="settleDialog" @saved="loadData"/>

    <!-- 筛选区：条件全部下推后端（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="query-grid-wrap">
        <el-form class="query-grid" label-width="70px">
          <el-form-item class="is-daterange" label="就诊日期">
            <el-date-picker
                v-model="searchForm.dateRange" :shortcuts="dateShortcuts" end-placeholder="结束"
                range-separator="至" start-placeholder="开始"
                type="daterange" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="科室">
            <el-select
                v-model="searchForm.deptIds" :fit-input-width="false" collapse-tags collapse-tags-tooltip
                filterable multiple placeholder="全部科室">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="医生">
            <el-select v-model="searchForm.doctorId" :fit-input-width="false" clearable filterable
                       placeholder="全部医生">
              <el-option v-for="e in doctorOptions" :key="e.id" :label="e.empName" :value="String(e.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" :prefix-icon="Search"
                      clearable placeholder="姓名 / 就诊号 / 门诊号 / 排队号" @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="就诊状态">
            <el-select v-model="searchForm.statusCodes" collapse-tags multiple placeholder="全部状态">
              <el-option v-for="s in OPD_LOG_STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="号别">
            <el-select v-model="searchForm.registType" clearable placeholder="全部">
              <el-option v-for="s in registTypeOptions" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="结算方式">
            <el-select v-model="searchForm.settlementType" clearable placeholder="全部">
              <el-option v-for="s in settlementOptions" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="就诊类型">
            <el-select v-model="searchForm.revisitType" clearable placeholder="全部">
              <el-option v-for="s in revisitOptions" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
        </el-form>
        <div class="query-grid-ops">
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column fixed="left" label="就诊号" prop="registNo" width="160">
          <template #default="{ row }">
            <span class="font-mono font-bold text-blue-700">{{ row.registNo }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="left" label="患者" width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-1.5">
              <span class="font-medium text-slate-800">{{ row.patientName }}</span>
              <span class="text-slate-400">{{ patientGenderText(row.gender) }}{{ row.age ? '·' + row.age : '' }}</span>
            </div>
            <div class="font-mono text-slate-400">{{ row.patientNo }}</div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="排队号" width="100">
          <template #default="{ row }">
            <span v-if="row.queueNo" class="rounded bg-blue-50 px-1.5 py-0.5 font-mono text-blue-700">{{
                shortQueueNo(row.queueNo)
              }}</span>
            <span v-else class="text-slate-300">未签到</span>
          </template>
        </el-table-column>
        <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="120"/>
        <el-table-column label="接诊医生" prop="doctorName" show-overflow-tooltip width="130"/>
        <el-table-column align="center" label="号别" width="86">
          <template #default="{ row }">
            <el-tag v-if="row.registType != null" :type="statusOf(REGIST_TYPE, row.registType).tagType"
                    effect="plain" size="small">{{ statusOf(REGIST_TYPE, row.registType).label }}
            </el-tag>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="就诊类型" width="86">
          <template #default="{ row }">
            <span
                :class="['rounded px-1.5 py-0.5', row.revisitType != null ? 'bg-slate-100 text-slate-600' : 'text-slate-400']">
              {{ row.revisitType != null ? statusOf(REVISIT_TYPE, row.revisitType).label : '未标注' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="来源" width="76">
          <template #default="{ row }">
            <span class="text-slate-500">{{
                row.registSource != null ? statusOf(REGIST_SOURCE, row.registSource).label : '-'
              }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="就诊日期" width="104">
          <template #default="{ row }"><span class="text-slate-600">{{ row.visitDate || '-' }}</span></template>
        </el-table-column>

        <el-table-column label="签到到达" width="132">
          <template #default="{ row }"><span class="text-slate-600">{{ fmt(row.arriveTime) }}</span></template>
        </el-table-column>
        <el-table-column label="开始就诊" width="132">
          <template #default="{ row }"><span class="text-slate-600">{{ fmt(row.startTime) }}</span></template>
        </el-table-column>
        <el-table-column label="结束" width="132">
          <template #default="{ row }"><span class="text-slate-600">{{ fmt(row.endTime) }}</span></template>
        </el-table-column>
        <el-table-column align="center" label="候诊" width="70">
          <template #default="{ row }"><span class="text-slate-600">{{ duration(row.waitMinutes) }}</span></template>
        </el-table-column>
        <el-table-column align="center" label="就诊时长" width="76">
          <template #default="{ row }"><span class="text-slate-600">{{ duration(row.visitMinutes) }}</span></template>
        </el-table-column>
        <el-table-column align="center" label="结算" width="90">
          <template #default="{ row }">
            <span class="text-slate-600">{{
                row.settlementType != null ? statusOf(SETTLEMENT_TYPE, row.settlementType).label : '-'
              }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="状态" width="96">
          <template #default="{ row }">
            <el-tag :type="logStatusStyle(row).tagType" effect="plain" size="small">
              {{ row.logStatusLabel || logStatusStyle(row).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button :icon="View" link size="small" type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-8 text-sm text-slate-400">该筛选条件下没有就诊记录</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"/>
      </div>
    </el-card>

    <TodayVisitsDetail v-model="drawer" :row="drawerRow"/>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Refresh, Search, View} from '@element-plus/icons-vue';
import {getOpdLogListPage, getOpdLogStats} from '@/api/appoint';
import {getDepartmentSelectList, getEmployeeList} from '@/api/system';
import {
  OPD_LOG_STATUS_OPTIONS,
  REGIST_SOURCE,
  REGIST_TYPE,
  REVISIT_TYPE,
  SETTLEMENT_TYPE,
  statusOf,
} from '@/lib/statusColor';
import {patientGenderText} from '@/lib/patientGender';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {shortQueueNo} from '@/lib/utils';
import {duration, fmt, logStatusStyle} from '@/lib/todayVisits';
import TodayVisitsDetail from './TodayVisitsDetail.vue';
import TodayVisitsSettleEdit from './TodayVisitsSettleEdit.vue';
// ========== 列表 ==========
const loading = ref(false);
const rows = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
/** 统计条默认值：字段与 OpdLogStatsVO 一一对应，别少写（少写的字段会显示成 0 骗人） */
const emptyStats = () => ({
  total: 0, unpaid: 0, waitCheckIn: 0, waiting: 0, consulting: 0,
  completed: 0, cancelled: 0, overdue: 0, unvisited: 0, noShow: 0, unrecognized: 0,
  avgWaitMinutes: 0, avgVisitMinutes: 0,
});
const stats = ref(emptyStats());
/** 批次F：第一次拿到统计之前不能显示 0 —— "0 例就诊"和"还没查出来"是两句不同的话 */
const statsLoaded = ref(false);
// ========== 筛选（默认本月 —— 就诊总览是回溯分析页，不是「看今天」） ==========
const fmtDate = (d) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
};
const thisMonth = () => {
  const now = new Date();
  return [fmtDate(new Date(now.getFullYear(), now.getMonth(), 1)), fmtDate(new Date(now.getFullYear(), now.getMonth() + 1, 0))];
};
const searchForm = ref({
  dateRange: thisMonth(),
  deptIds: [],
  doctorId: null,
  statusCodes: [],
  registType: null,
  settlementType: null,
  revisitType: null,
  keyword: '',
});
const dateShortcuts = [
  {
    text: '今日', value: () => {
      const t = new Date();
      return [fmtDate(t), fmtDate(t)];
    }
  },
  {
    text: '本周', value: () => {
      const now = new Date();
      const day = now.getDay() || 7;
      const mon = new Date(now);
      mon.setDate(now.getDate() - day + 1);
      const sun = new Date(mon);
      sun.setDate(mon.getDate() + 6);
      return [fmtDate(mon), fmtDate(sun)];
    }
  },
  {text: '本月', value: () => thisMonth()},
  {
    text: '近三月', value: () => {
      const now = new Date();
      const first = new Date(now.getFullYear(), now.getMonth() - 2, 1);
      return [fmtDate(first), fmtDate(now)];
    }
  },
];
// ========== 下拉数据源（科室 / 员工数组，全部走真实接口） ==========
const deptOptions = ref([]);
const doctorOptions = ref([]);
const loadOptions = async () => {
  try {
    // 不传 scope → 默认按当前人过滤（今日就诊列表本就该只看自己能管的科室）
    const [d, e] = await Promise.all([getDepartmentSelectList({}), getEmployeeList({})]);
    deptOptions.value = d.data || [];
    doctorOptions.value = e.data || [];
  } catch (err) {
    console.error('加载科室/医生下拉失败:', err);
  }
};
const optionOf = (table) => Object.entries(table).map(([v, s]) => ({value: Number(v), label: s.label}));
const registTypeOptions = optionOf(REGIST_TYPE);
const settlementOptions = optionOf(SETTLEMENT_TYPE);
const revisitOptions = optionOf(REVISIT_TYPE);
// ========== 查询参数：筛选一律下推，前端不做任何 slice/filter ==========
const buildParams = () => {
  const p = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize};
  const f = searchForm.value;
  if (f.dateRange?.[0] && f.dateRange?.[1]) {
    p.startDate = f.dateRange[0];
    p.endDate = f.dateRange[1];
  }
  if (f.deptIds.length)
    p.deptIds = f.deptIds;
  if (f.doctorId)
    p.doctorId = f.doctorId;
  if (f.statusCodes.length)
    p.statusCodes = f.statusCodes;
  if (f.registType != null)
    p.registType = f.registType;
  if (f.settlementType != null)
    p.settlementType = f.settlementType;
  if (f.revisitType != null)
    p.revisitType = f.revisitType;
  if (f.keyword.trim())
    p.keyword = f.keyword.trim();
  return p;
};
const loadData = async () => {
  loading.value = true;
  try {
    const params = buildParams();
    const [listRes, statsRes] = await Promise.all([getOpdLogListPage(params), getOpdLogStats(params)]);
    rows.value = listRes.data?.records || [];
    pagination.value.total = listRes.data?.total || 0;
    stats.value = statsRes.data || emptyStats();
    statsLoaded.value = true;
  } catch (err) {
    console.error('加载就诊总览失败:', err);
  } finally {
    loading.value = false;
  }
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {
    dateRange: thisMonth(), deptIds: [], doctorId: null, statusCodes: [],
    registType: null, settlementType: null, revisitType: null, keyword: '',
  };
  pagination.value.pageNum = 1;
  loadData();
};
const handleSizeChange = (v) => {
  pagination.value.pageSize = v;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (v) => {
  pagination.value.pageNum = v;
  loadData();
};
// ========== 视图辅助 ==========
/** 统计条定义（unrecognized 只在 >0 时出现，避免常规视图多一个恒为 0 的格子） */
const statCards = () => {
  const list = [
    {label: '总就诊', value: stats.value.total, color: 'text-slate-800'},
    {label: '未缴费', value: stats.value.unpaid, color: 'text-amber-600'},
    {label: '待签到', value: stats.value.waitCheckIn, color: 'text-slate-600'},
    {label: '候诊中', value: stats.value.waiting, color: 'text-blue-600'},
    {label: '就诊中', value: stats.value.consulting, color: 'text-purple-600'},
    {label: '已就诊', value: stats.value.completed, color: 'text-emerald-600'},
    {label: '已退号', value: stats.value.cancelled, color: 'text-red-500'},
    {label: '已过号', value: stats.value.overdue, color: 'text-orange-500'},
    // 未就诊 / 爽约 是日终结转收的两个终态。放在统计条里是**故意**的：
    // 这两个数字长期为 0 才说明「每天的号都有人收尾」，一旦涨起来就是有人没签字/没结诊。
    {label: '未就诊', value: stats.value.unvisited, color: 'text-slate-500'},
    {label: '爽约', value: stats.value.noShow, color: 'text-rose-600'},
  ];
  if (stats.value.unrecognized > 0) {
    list.push({label: `未识别`, value: stats.value.unrecognized, color: 'text-slate-400'});
  }
  return list;
};
const settleDialog = ref(false);
// ========== 就诊详情抽屉（六段，数据全部来自真实接口） ==========
const drawer = ref(false);
const drawerRow = ref(null);
const openDetail = (row) => {
  drawerRow.value = row;
  drawer.value = true;
};
onMounted(() => {
  loadOptions();
  loadData();
});
</script>
