<template>
  <div v-loading="loading">
    <!-- 统计条：与列表同一套 WHERE，口径可核对 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <div class="grid grid-cols-5 gap-3">
        <div v-for="c in statCards()" :key="c.label" :class="c.onClick ? 'cursor-pointer' : ''"
             class="text-center" @click="c.onClick && c.onClick()">
          <p :class="['text-xl font-bold leading-tight', c.color]">{{ statsLoaded ? c.value : '—' }}</p>
          <p class="mt-0.5 text-xs text-slate-500">{{ c.label }}</p>
        </div>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :inline="true" label-width="70px">
        <el-form-item label="就诊日期">
          <el-date-picker
              v-model="searchForm.dateRange" :shortcuts="dateShortcuts" class="!w-64"
              end-placeholder="结束日期" range-separator="至"
              start-placeholder="开始日期" type="daterange" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="searchForm.deptId" :fit-input-width="false" class="!w-44" clearable
                     filterable placeholder="全部科室">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="searchForm.doctorId" :fit-input-width="false" class="!w-40" clearable
                     filterable placeholder="全部医生">
            <el-option v-for="e in doctorOptions" :key="e.id" :label="e.empName" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" :prefix-icon="Search"
                    class="!w-52" clearable placeholder="姓名 / 病历号 / 诊断" @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label-width="0">
          <el-checkbox v-model="searchForm.feverOnly" class="!mr-4">只看发热</el-checkbox>
          <el-checkbox v-model="searchForm.reportableOnly" class="!mr-2">只看法定可报</el-checkbox>
        </el-form-item>
        <el-form-item v-if="searchForm.reportableOnly" label="上报情况" label-width="70px">
          <el-select v-model="searchForm.reportedFilter" class="!w-28" clearable placeholder="全部">
            <el-option :value="1" label="未上报"/>
            <el-option :value="2" label="已上报"/>
          </el-select>
        </el-form-item>
        <el-form-item class="ml-auto">
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡（只读台账） -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column align="center" fixed="left" label="就诊日期" width="150">
          <template #default="{ row }"><span class="text-slate-600">{{ row.visitDate || '-' }}</span></template>
        </el-table-column>
        <el-table-column fixed="left" label="患者" width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-1.5">
              <span class="font-medium text-slate-800">{{ row.patientName }}</span>
              <span class="text-slate-400">{{ patientGenderText(row.gender) }}{{ row.age ? '·' + row.age : '' }}</span>
            </div>
            <div class="font-mono text-slate-400">{{ row.patientNo }}</div>
            <div v-if="row.phoneMasked" class="font-mono text-slate-400">{{ row.phoneMasked }}</div>
          </template>
        </el-table-column>
        <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="200"/>
        <el-table-column label="接诊医生" prop="doctorName" show-overflow-tooltip width="150"/>
        <el-table-column label="诊断" min-width="200">
          <template #default="{ row }">
            <div class="text-slate-800">{{ row.diagnosisName || '-' }}</div>
            <div v-if="row.diagnosisCode" class="font-mono text-slate-400">ICD {{ row.diagnosisCode }}</div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="体温" width="82">
          <template #default="{ row }">
            <span :class="[row.fever ? 'font-bold text-red-600' : 'text-slate-600']">
              {{ row.temperature ? row.temperature + '℃' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="法定病种" show-overflow-tooltip width="150">
          <template #default="{ row }">
            <el-tag v-if="row.reportable" effect="plain" size="small" type="danger">
              {{ row.matchedDiseaseName || row.diagnosisCode }}
            </el-tag>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="上报情况" width="150">
          <template #default="{ row }">
            <template v-if="row.reportable">
              <el-tag v-if="row.reported" size="small" type="success">已上报</el-tag>
              <el-tag v-else size="small" type="danger">应报未报</el-tag>
              <div v-if="row.reported" class="font-mono text-slate-400">{{ row.reportNo }}</div>
              <div v-if="row.reportDiseaseName" class="text-slate-400">卡诊断：{{ row.reportDiseaseName }}</div>
            </template>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="病历状态" width="92">
          <template #default="{ row }">
            <el-tag :type="recordStatusTagType(row.recordStatus)" effect="plain" size="small">
              {{ recordStatusText(row.recordStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="签名" width="96">
          <template #default="{ row }">
            <el-tag v-if="row.signStatus === 1" effect="plain" size="small" type="success">已电子签名</el-tag>
            <el-tag v-else-if="row.signStatus === 2" effect="plain" size="small" type="warning">签名已作废</el-tag>
            <span v-else class="text-slate-300">未签名</span>
          </template>
        </el-table-column>
        <el-table-column label="病历号" width="150">
          <template #default="{ row }"><span class="font-mono text-slate-500">{{ row.recordNo }}</span></template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="View" size="small" @click="openDetail(row)">病历</el-button>
            <el-button v-if="row.reportable && !row.reported" :icon="Position" size="small" type="primary"
                       @click="goReport(row)">去报卡
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="text-slate-400">当前条件下没有已提交/已归档的门诊病历</span>
        </template>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES" :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange" @current-change="handleCurrentChange"/>
      </div>
    </el-card>

    <!-- 病历详情（只读） -->
    <el-dialog v-model="detailDialog" title="门诊病历（只读）" width="720px">
      <div v-loading="detailLoading" class="space-y-3 text-sm">
        <div v-if="detailRow"
             class="flex flex-wrap gap-x-6 gap-y-1 rounded bg-slate-50 px-3 py-2 text-xs text-slate-500">
          <span>{{ detailRow.patientName }} · {{
              patientGenderText(detailRow.gender)
            }}{{ detailRow.age ? ' · ' + detailRow.age + '岁' : '' }}</span>
          <span>就诊日 {{ detailRow.visitDate }}</span>
          <span>{{ detailRow.deptName }} / {{ detailRow.doctorName }}</span>
          <span>病历状态 <b>{{ recordStatusText(detailRow.recordStatus) }}</b></span>
        </div>
        <template v-if="detailRecord">
          <div><p class="text-slate-400">主诉</p>
            <p class="whitespace-pre-wrap text-slate-700">{{ detailRecord.chiefComplaint || '-' }}</p></div>
          <div><p class="text-slate-400">现病史</p>
            <p class="whitespace-pre-wrap text-slate-700">{{ detailRecord.presentIllness || '-' }}</p></div>
          <div><p class="text-slate-400">既往史</p>
            <p class="whitespace-pre-wrap text-slate-700">{{ detailRecord.pastHistory || '-' }}</p></div>
          <div><p class="text-slate-400">体格检查</p>
            <p class="whitespace-pre-wrap text-slate-700">{{ detailRecord.physicalExamination || '-' }}</p></div>
          <div><p class="text-slate-400">诊断</p>
            <p class="whitespace-pre-wrap text-slate-700">
              {{ detailRecord.diagnosisName || detailRecord.diagnosis || '-' }}
              <span v-if="detailRecord.diagnosisCode"
                    class="font-mono text-xs text-slate-400">（ICD {{ detailRecord.diagnosisCode }}）</span>
            </p>
          </div>
          <div><p class="text-slate-400">处理意见</p>
            <p class="whitespace-pre-wrap text-slate-700">{{
                detailRecord.treatmentPlan || detailRecord.advice || '-'
              }}</p></div>
        </template>
        <el-empty v-else-if="!detailLoading" :image-size="60" description="未取到病历内容"/>
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 门诊日志（法规台账，sql/131 新建菜单 207）
 *
 * 与「就诊总览」（/today-visits，原误占本名的挂号运营页）的本质差异：
 *  本页面基表是**门诊病历**（record_status 2已提交/3已归档），一行 = 一次有诊断事实的接诊，
 *  服务《门诊日志管理规定》的行政/感控核查 —— 核心用途是传染病「应报未报」筛查。
 *
 * 只读页：报卡的增删改审一律走 /infectious-report（后端 /emr/infectious），
 * 这里「去报卡」只是跳转，不做任何写动作（筛选态不得驱动写入）。
 * 「可报/已报」不落状态列，查询时现算：诊断 ICD 前缀命中法定传染病字典=可报；
 * 该挂号存在 biz_infectious_report 行=已报（取最新一张卡展示）。
 */
import {onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {Position, Refresh, Search, View} from '@element-plus/icons-vue';
import {getOutpatientLogPage, getOutpatientLogStats, getRecordDetail} from '@/api/emr';
import {getDepartmentSelectList, getEmployeeList} from '@/api/system';
import {patientGenderText} from '@/lib/patientGender';
import {recordStatusTagType, recordStatusText} from '@/lib/recordStatus';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const router = useRouter();
// ========== 列表 ==========
const loading = ref(false);
const rows = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const emptyStats = () => ({totalCount: 0, feverCount: 0, reportableCount: 0, pendingCount: 0, reportedCount: 0});
const stats = ref(emptyStats());
const statsLoaded = ref(false);
// ========== 筛选（默认本月，与就诊总览同惯例） ==========
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
  deptId: null,
  doctorId: null,
  keyword: '',
  feverOnly: false,
  reportableOnly: false,
  reportedFilter: null,
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
const deptOptions = ref([]);
const doctorOptions = ref([]);
const loadOptions = async () => {
  try {
    const [d, e] = await Promise.all([getDepartmentSelectList({}), getEmployeeList({})]);
    deptOptions.value = d.data || [];
    doctorOptions.value = e.data || [];
  } catch (err) {
    console.error('加载科室/医生下拉失败:', err);
  }
};
// ========== 查询：条件全部下推后端，前端不做任何 filter ==========
const buildParams = () => {
  const p = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize};
  const f = searchForm.value;
  if (f.dateRange?.[0] && f.dateRange?.[1]) {
    p.startDate = f.dateRange[0];
    p.endDate = f.dateRange[1];
  }
  if (f.deptId)
    p.deptId = f.deptId;
  if (f.doctorId)
    p.doctorId = f.doctorId;
  if (f.keyword.trim())
    p.keyword = f.keyword.trim();
  if (f.feverOnly)
    p.feverOnly = true;
  if (f.reportableOnly)
    p.reportableOnly = true;
  // reportedFilter 只在勾选「法定可报」时有意义（未报/已报是针对可报集合的三分法）
  if (f.reportableOnly && f.reportedFilter != null)
    p.reportedFilter = f.reportedFilter;
  return p;
};
const loadData = async () => {
  loading.value = true;
  try {
    const params = buildParams();
    const [listRes, statsRes] = await Promise.all([getOutpatientLogPage(params), getOutpatientLogStats(params)]);
    rows.value = listRes.data?.records || [];
    pagination.value.total = listRes.data?.total || 0;
    stats.value = statsRes.data || emptyStats();
    statsLoaded.value = true;
  } catch (err) {
    console.error('加载门诊日志失败:', err);
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
    dateRange: thisMonth(), deptId: null, doctorId: null, keyword: '',
    feverOnly: false, reportableOnly: false, reportedFilter: null
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
/** 统计卡联动筛选：点「应报未报」直接查该子集（只读页，点卡=查询，不是写动作） */
const filterPending = () => {
  searchForm.value.reportableOnly = true;
  searchForm.value.reportedFilter = 1;
  handleSearch();
};
const statCards = () => [
  {label: '日志总量', value: stats.value.totalCount, color: 'text-slate-800'},
  {label: '发热(≥37.3℃)', value: stats.value.feverCount, color: 'text-orange-500'},
  {label: '法定可报', value: stats.value.reportableCount, color: 'text-blue-600'},
  {label: '应报未报', value: stats.value.pendingCount, color: 'text-red-600', onClick: filterPending},
  {label: '已上报', value: stats.value.reportedCount, color: 'text-emerald-600'},
];
// ========== 病历详情（只读弹框） ==========
const detailDialog = ref(false);
const detailRow = ref(null);
const detailLoading = ref(false);
const detailRecord = ref(null);
const openDetail = async (row) => {
  detailRow.value = row;
  detailRecord.value = null;
  detailDialog.value = true;
  detailLoading.value = true;
  try {
    const res = await getRecordDetail(String(row.recordId));
    detailRecord.value = res.data?.record || res.data || null;
  } catch (err) {
    console.error('加载病历详情失败:', err);
  } finally {
    detailLoading.value = false;
  }
};
/** 去报卡：只跳转到传染病报告卡页，本页面零写逻辑 */
const goReport = (row) => {
  router.push({path: '/infectious-report', query: {registId: String(row.registId), patientName: row.patientName}});
};
onMounted(() => {
  loadOptions();
  loadData();
});
</script>
