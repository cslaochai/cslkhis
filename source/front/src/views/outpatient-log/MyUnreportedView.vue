<template>
  <div v-loading="loading">
    <!-- 统计条：与列表同一套 WHERE（后端已强制本人+应报未报），口径可核对 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <div class="grid grid-cols-2 gap-3">
        <div class="text-center">
          <p class="text-xl font-bold leading-tight text-red-600">{{ statsLoaded ? stats.totalCount : '—' }}</p>
          <p class="mt-0.5 text-xs text-slate-500">应报未报（当前筛选）</p>
        </div>
        <div class="text-center">
          <p class="text-xl font-bold leading-tight text-orange-500">{{ statsLoaded ? stats.feverCount : '—' }}</p>
          <p class="mt-0.5 text-xs text-slate-500">其中发热(≥37.3℃)</p>
        </div>
      </div>
    </div>

    <!-- 查询卡：只有日期与关键词，医生与科室是服务端强制口径，不提供筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :inline="true" label-width="70px">
        <el-form-item label="就诊日期">
          <el-date-picker
              v-model="searchForm.dateRange" :shortcuts="dateShortcuts" class="!w-64"
              end-placeholder="结束日期" range-separator="至"
              start-placeholder="开始日期" type="daterange" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" :prefix-icon="Search"
                    class="!w-52" clearable placeholder="姓名 / 病历号 / 诊断" @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item class="ml-auto">
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡（只读自查清单） -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe style="width: 100%">
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
        <el-table-column align="center" fixed="left" label="就诊日期" width="150">
          <template #default="{ row }"><span class="text-slate-600">{{ row.visitDate || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="200"/>
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
        <el-table-column align="center" label="病历状态" width="92">
          <template #default="{ row }">
            <el-tag :type="recordStatusTagType(row.recordStatus)" effect="plain" size="small">
              {{ recordStatusText(row.recordStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="病历号" width="150">
          <template #default="{ row }"><span class="font-mono text-slate-500">{{ row.recordNo }}</span></template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="View" size="small" @click="openDetail(row)">病历</el-button>
            <el-button :icon="Position" size="small" type="primary" @click="goReport(row)">去报卡
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="text-slate-400">筛选范围内没有属于你的应报未报记录</span>
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
 * 我的未报（菜单 2950 / sql/238）—— 医生自查「应报未报」入口。
 *
 * 与「门诊日志」（法规台账，菜单 207）的分工：207 是全院口径的行政/感控核查页（已收口给公卫/质控），
 * 本页面是医生本人的自查清单：后端强制 doctorId=登录员工、deptId 清空、可报+未报固定，
 * 前端不提供医生/科室筛选——客户端伪造的筛选一律被服务端覆盖。
 * 只读页：报卡写动作一律走 /infectious-report，「去报卡」只是跳转，本页面零写逻辑。
 */
import {onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {Position, Refresh, Search, View} from '@element-plus/icons-vue';
import {getMyPendingPage, getMyPendingStats, getRecordDetail} from '@/api/emr';
import {patientGenderText} from '@/lib/patientGender';
import {recordStatusTagType, recordStatusText} from '@/lib/recordStatus';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const router = useRouter();
// ========== 列表 ==========
const loading = ref(false);
const rows = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const emptyStats = () => ({totalCount: 0, feverCount: 0, reportableCount: 0, pendingCount: 0, reportedCount: 0});
const stats = ref(emptyStats());
const statsLoaded = ref(false);
// ========== 筛选（默认本月，与门诊日志同惯例；跨科室回溯合法，历史漏报可往前翻） ==========
const fmtDate = (d) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
};
const thisMonth = () => {
  const now = new Date();
  return [fmtDate(new Date(now.getFullYear(), now.getMonth(), 1)), fmtDate(new Date(now.getFullYear(), now.getMonth() + 1, 0))];
};
const searchForm = ref({dateRange: thisMonth(), keyword: ''});
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
  {
    text: '近一年', value: () => {
      const now = new Date();
      const first = new Date(now.getFullYear(), now.getMonth() - 11, now.getDate());
      return [fmtDate(first), fmtDate(now)];
    }
  },
];
// ========== 查询：条件全部下推后端，前端不做任何 filter ==========
const buildParams = () => {
  const p = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize};
  const f = searchForm.value;
  if (f.dateRange?.[0] && f.dateRange?.[1]) {
    p.startDate = f.dateRange[0];
    p.endDate = f.dateRange[1];
  }
  if (f.keyword.trim())
    p.keyword = f.keyword.trim();
  return p;
};
const loadData = async () => {
  loading.value = true;
  try {
    const params = buildParams();
    const [listRes, statsRes] = await Promise.all([getMyPendingPage(params), getMyPendingStats(params)]);
    rows.value = listRes.data?.records || [];
    pagination.value.total = listRes.data?.total || 0;
    stats.value = statsRes.data || emptyStats();
    statsLoaded.value = true;
  } catch (err) {
    console.error('加载我的未报失败:', err);
  } finally {
    loading.value = false;
  }
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {dateRange: thisMonth(), keyword: ''};
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
// ========== 病历详情（只读弹框，与门诊日志同接口） ==========
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
  loadData();
});
</script>
