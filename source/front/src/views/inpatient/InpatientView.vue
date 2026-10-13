<template>
  <div class="space-y-6">
    <!-- 头部 -->
    <div class="flex items-start justify-between">
      <div class="flex gap-2">
        <el-button :icon="Refresh" @click="() => { loadList(); loadStats(); loadWards(); loadPending() }">刷新
        </el-button>
        <el-button v-perm="'ipd:inpatient:add'" :icon="Plus" type="primary" @click="openAdmit">入院登记</el-button>
      </div>
    </div>

    <!-- 统计卡片：数值全部来自后端（床位口径为 sys_bed 实时统计） -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-blue-50 p-2.5">
            <el-icon class="h-5 w-5 text-blue-600">
              <Suitcase/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-blue-600">{{ stats.inHospitalCount }}</p>
            <p class="text-xs text-slate-500">在院患者</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-purple-50 p-2.5">
            <el-icon class="h-5 w-5 text-purple-600">
              <CircleCheck/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-purple-600">{{ stats.todayAdmitted }}</p>
            <p class="text-xs text-slate-500">今日入院</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <el-icon class="h-5 w-5 text-amber-600">
              <Clock/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-amber-600">{{ stats.todayDischarged }}</p>
            <p class="text-xs text-slate-500">今日出院</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-emerald-50 p-2.5">
            <el-icon class="h-5 w-5 text-emerald-600">
              <User/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-emerald-600">{{ stats.bedUsageRate }}%</p>
            <p class="text-xs text-slate-500">
              床位使用率（{{ stats.occupiedBeds }}/{{ stats.totalBeds }}，空闲 {{ stats.freeBeds }}）
            </p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-amber-200 bg-amber-50/40 p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-100 p-2.5">
            <el-icon class="h-5 w-5 text-amber-700">
              <Tickets/>
            </el-icon>
          </div>
          <div>
            <p class="text-xl font-bold text-amber-700">{{ stats.pendingAdmissionOrderCount }}</p>
            <p class="text-xs text-slate-500">待收治住院证（门诊已开证、未排床）</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ================= 待收治（住院证）：门诊 → 住院这条桥的落点 ================= -->
    <div class="rounded-lg border border-amber-200 bg-white shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-amber-100 bg-amber-50/50 px-4 py-3">
        <div class="flex items-center gap-2">
          <el-icon class="text-amber-700">
            <Tickets/>
          </el-icon>
          <span class="font-semibold text-slate-800">待收治（住院证）</span>
          <el-tag v-if="pendingTotal > 0" effect="dark" size="small" type="warning">{{ pendingTotal }}</el-tag>
        </div>
        <div class="flex items-center gap-2">
          <el-select v-model="pendingOnly" class="!w-40" size="small" @change="searchPending">
            <el-option :value="1" label="只看可用（未过期）"/>
            <el-option :value="0" label="全部住院证"/>
          </el-select>
          <el-input
              v-model="pendingQuery.keyword"
              :prefix-icon="Search"
              class="!w-60"
              clearable
              placeholder="患者姓名 / 住院证号 / 挂号号"
              size="small"
              @clear="searchPending"
              @keyup.enter="searchPending"
          />
          <el-button :icon="Search" size="small" @click="searchPending">查询</el-button>
          <el-button :icon="Refresh" size="small" @click="loadPending">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="pendingLoading" :data="pendingRows" empty-text="暂无待收治住院证" style="width: 100%">
        <el-table-column label="住院证号" prop="orderNo" width="140"/>
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">
            <div class="font-medium text-slate-800">{{ row.patientName }}</div>
            <div class="text-xs text-slate-500">{{ row.genderText }} · {{ row.age ?? '—' }}岁 · {{
                row.phone || '无电话'
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="来源门诊" min-width="170">
          <template #default="{ row }">
            <div class="text-slate-700">{{ row.sourceDeptName || '—' }} · {{ row.sourceDoctorName || '—' }}</div>
            <div class="text-xs text-slate-500">挂号号 {{ row.registNo || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="拟收治科室" width="130">
          <template #default="{ row }">{{ row.applyDeptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="拟诊" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.diagnosisName || '—' }}</div>
            <div v-if="row.diagnosisCode" class="text-xs text-slate-400">{{ row.diagnosisCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="有效期至" width="150">
          <template #default="{ row }">
            <span :class="row.expired ? 'text-rose-500' : 'text-slate-600'">{{ row.validUntil || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="orderStatusMap[row.orderStatus]?.type || 'info'" size="small">
              {{ row.orderStatusText || orderStatusMap[row.orderStatus]?.label || `未知(${row.orderStatus})` }}
            </el-tag>
            <!-- 过期是算出来的展示态，必须和"待收治"同时可见 -->
            <el-tag v-if="row.expired" class="ml-1" effect="plain" size="small" type="danger">已过期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收治结果" min-width="170">
          <template #default="{ row }">
            <template v-if="row.admissionId">
              <div class="text-slate-700">{{ row.admissionNo || '—' }}</div>
              <div class="text-xs text-slate-500">
                收治科室 {{ row.admitDeptName || '—' }}
                <el-tag v-if="row.deptAdjusted" class="ml-1" effect="plain" size="small" type="warning">调科</el-tag>
              </div>
            </template>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button
                v-if="row.orderStatus === 1 && !row.expired"
                v-perm="'ipd:inpatient:add'"
                :icon="Check"
                size="small"
                type="primary"
                @click="openAdmitFromOrder(row)"
            >收治入院
            </el-button>
            <el-button
                v-if="row.orderStatus === 1"
                v-perm="'ipd:inpatient:edit'"
                :icon="CircleClose"
                link
                size="small"
                type="danger"
                @click="cancelOrder(row)"
            >作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end px-4 py-3">
        <el-pagination
            v-model:current-page="pendingQuery.pageNum"
            :page-size="pendingQuery.pageSize"
            :total="pendingTotal"
            layout="total, prev, pager, next"
            size="small"
            @current-change="loadPending"
        />
      </div>
    </div>

    <!-- 查询条件 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <el-input
            v-model="query.patientName"
            :prefix-icon="Search"
            class="!w-56"
            clearable
            placeholder="患者姓名 / 住院号"
            @clear="search"
            @keyup.enter="search"
        />
        <el-select v-model="query.wardId" class="!w-48" clearable placeholder="全部病区" @change="search">
          <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId">
            <span>{{ w.wardName }}</span>
            <span class="float-right text-xs text-slate-400">{{ w.occupiedBeds }}/{{ w.totalBeds }}</span>
          </el-option>
        </el-select>
        <el-radio-group v-model="query.admitStatus" @change="onStatusChange">
          <el-radio-button :value="1">在院</el-radio-button>
          <el-radio-button :value="0">已出院</el-radio-button>
          <!-- 'ALL' 而不是 null：EP 把 nil 的 value 当作「没传 value」，会退回废弃的 label 兜底并告警 -->
          <el-radio-button value="ALL">全部</el-radio-button>
        </el-radio-group>
        <el-button :icon="Search" type="primary" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-table v-loading="loading" :data="rows" style="width: 100%">
        <el-table-column label="住院号" prop="admissionNo" width="140"/>
        <el-table-column label="患者" min-width="140">
          <template #default="{ row }">
            <div class="font-medium text-slate-800">{{ row.patientName || '—' }}</div>
            <div class="text-xs text-slate-400">{{ genderText(row.gender) }} · {{ row.age ?? '—' }}岁</div>
          </template>
        </el-table-column>
        <el-table-column label="科室 / 病区" min-width="160">
          <template #default="{ row }">
            <div>{{ row.deptName || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.wardName || '—' }} · {{ row.bedNo || '未分床' }}</div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="入院途径" width="90">
          <template #default="{ row }">{{ textOf(admitWayMap, row.admitWay) }}</template>
        </el-table-column>
        <el-table-column label="门诊来源" show-overflow-tooltip width="160">
          <template #default="{ row }">
            <div class="text-slate-700">{{ row.registNo || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.admissionOrderNo || '直接入院' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="入院时间" width="150">
          <template #default="{ row }">{{ row.admitTime || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="住院天数" width="90">
          <template #default="{ row }">{{ row.inpatientDays ?? 0 }} 天</template>
        </el-table-column>
        <el-table-column label="主要诊断" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.mainDiagnosisName || row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column label="主管医生" width="100">
          <template #default="{ row }">{{ row.doctorName || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="病案首页" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.summaryStatus" :type="summaryStatusMap[row.summaryStatus]?.type || 'info'" effect="plain"
                    size="small">
              {{ summaryStatusMap[row.summaryStatus]?.label || `未知(${row.summaryStatus})` }}
            </el-tag>
            <span v-else class="text-xs text-slate-400">未生成</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="总费用" width="110">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button :icon="View" link size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-perm="'ipd:inpatient:edit'" :icon="Document" link size="small" type="primary"
                       @click="openSummary(row)">病案首页
            </el-button>
            <el-button
                v-perm="'ipd:inpatient:edit'"
                :disabled="row.admitStatus !== 1" :icon="Warning" link size="small"
                type="primary"
                @click="openTransfer(row)"
            >换床
            </el-button>
            <el-button
                v-perm="'ipd:inpatient:edit'"
                :disabled="row.admitStatus !== 1" :icon="Check" link size="small"
                type="danger"
                @click="confirmDischargeFromList(row)"
            >出院
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex items-center justify-between border-t border-slate-100 px-4 py-3">
        <span class="text-xs text-slate-400">共 {{ total }} 条</span>
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="() => { query.pageNum = 1; loadList() }"
        />
      </div>
    </div>

    <!-- ================= 详情 ================= -->
    <InpatientDetail v-model="detailVisible" :id="detailId"/>

    <!-- ================= 入院登记 ================= -->
    <InpatientAdmitEdit v-model="admitVisible" :admit-from-order="admitFromOrder"
                        :wards="wards" :employees="employees" @saved="onAdmitSaved"/>

    <!-- ================= 换床 ================= -->
    <InpatientBedTransferEdit v-model="transferVisible" :row="transferRow" @saved="onTransferSaved"/>

    <!-- ================= 出院办理 ================= -->
    <InpatientDischargeEdit v-model="dischargeVisible" :row="dischargeRow"
                            :employees="employees" @saved="onDischargeSaved"/>

    <!-- ================= 病案首页 ================= -->
    <InpatientSummaryEdit v-model="summaryVisible" :id="summaryId" @saved="onSummarySaved"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {patientGenderText} from '@/lib/patientGender';
import {
  Check,
  CircleCheck,
  CircleClose,
  Clock,
  Document,
  Plus,
  Refresh,
  Search,
  Suitcase,
  Tickets,
  User,
  View,
  Warning
} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getInpatientListPage,
  getInpatientStats,
  getInpatientWardList,
} from '@/api/inpatient';
import {cancelAdmissionOrder, getAdmissionOrderListPage,} from '@/api/admissionOrder';
import InpatientDetail from './InpatientDetail.vue';
import InpatientAdmitEdit from './InpatientAdmitEdit.vue';
import InpatientBedTransferEdit from './InpatientBedTransferEdit.vue';
import InpatientDischargeEdit from './InpatientDischargeEdit.vue';
import InpatientSummaryEdit from './InpatientSummaryEdit.vue';
import {getEmployeeList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {admitWayMap, money, orderStatusMap, summaryStatusMap, textOf} from '@/lib/inpatient';
// 性别文案统一走 lib/patientGender：0 是历史脏码值，渲染成「未知(0)」而不是"女"
const genderText = (g) => patientGenderText(g);
// ------------------------------------------------------------------
// 列表
// ------------------------------------------------------------------
const loading = ref(false);
const rows = ref([]);
const query = reactive({
  patientName: '',
  wardId: null,
  admitStatus: 1,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const total = ref(0);
const stats = reactive({
  inHospitalCount: 0, todayAdmitted: 0, todayDischarged: 0,
  totalBeds: 0, freeBeds: 0, occupiedBeds: 0, bedUsageRate: 0,
});
const wards = ref([]);
const employees = ref([]);
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getInpatientListPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      patientName: query.patientName || undefined,
      wardId: query.wardId || undefined,
      admitStatus: query.admitStatus === 'ALL' ? undefined : query.admitStatus,
    });
    rows.value = res.data?.records || [];
    total.value = Number(res.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const loadStats = async () => {
  try {
    const res = await getInpatientStats();
    Object.assign(stats, res.data || {});
  } catch { /* 统计失败不阻塞列表 */
  }
};
const loadWards = async () => {
  try {
    const res = await getInpatientWardList();
    wards.value = res.data || [];
  } catch { /* 忽略 */
  }
};
const loadEmployees = async () => {
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []).map((e) => ({id: String(e.id), empName: e.empName}));
  } catch { /* 忽略 */
  }
};
const search = () => {
  query.pageNum = 1;
  loadList();
};
const resetQuery = () => {
  query.patientName = '';
  query.wardId = null;
  query.admitStatus = 1;
  query.pageNum = 1;
  loadList();
};
const onStatusChange = () => {
  query.pageNum = 1;
  loadList();
};
// ------------------------------------------------------------------
// 详情
// ------------------------------------------------------------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.admissionId;
  detailVisible.value = true;
};
// ------------------------------------------------------------------
// 待收治（住院证）—— 门诊 → 住院这条桥的落点
//
// 这里是三甲真实链路上最容易断的一环：门诊医生开完住院证，患者拿着证到入院处，
// 入院处按证排床收治。这个页签就是"入院处的那张待办清单"。
//
// 两个口径不可混：
// 1. 「待收治」是库里的状态；「已过期」是后端按 valid_until 实时算的展示态。
//    一条证可以同时是"待收治 + 已过期"，此时它不能再被收治（后端会拒），
//    所以界面必须把两件事都摆出来，不能让入院处以为还能用。
// 2. 收治时入院处可以**调科**（真实医院允许），但调了就不能装作是按原计划收的，
//    列表里会用「调科」标记把差异显出来（后端 deptAdjusted）。
// ------------------------------------------------------------------
const pendingLoading = ref(false);
const pendingRows = ref([]);
const pendingTotal = ref(0);
const pendingOnly = ref(1);
const pendingQuery = reactive({keyword: '', pageNum: 1, pageSize: 5});
const loadPending = async () => {
  pendingLoading.value = true;
  try {
    const res = await getAdmissionOrderListPage({
      pageNum: pendingQuery.pageNum,
      pageSize: pendingQuery.pageSize,
      keyword: pendingQuery.keyword || undefined,
      onlyPending: pendingOnly.value,
    });
    pendingRows.value = res.data?.records || [];
    pendingTotal.value = Number(res.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    pendingLoading.value = false;
  }
};
const searchPending = () => {
  pendingQuery.pageNum = 1;
  loadPending();
};
const cancelOrder = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`作废住院证 ${row.orderNo}（${row.patientName}）。作废必须写原因，这条记录会留痕。`, '作废住院证', {
      inputPlaceholder: '如：患者放弃住院 / 电话联系不上 / 门诊诊断已修正',
      inputValidator: (v) => (!!v && v.trim().length > 0) || '作废原因不能为空'
    });
    await cancelAdmissionOrder({id: row.id, cancelReason: value});
    ElMessage.success('住院证已作废');
    await Promise.all([loadPending(), loadStats()]);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ------------------------------------------------------------------
// 入院登记
// ------------------------------------------------------------------
const admitVisible = ref(false);
/** 非空表示本次入院是「按住院证收治」，表单会锁掉患者与入院途径（由证决定） */
const admitFromOrder = ref(null);
const openAdmit = () => {
  admitFromOrder.value = null;
  admitVisible.value = true;
};
const openAdmitFromOrder = (row) => {
  admitFromOrder.value = row;
  admitVisible.value = true;
};
const onAdmitSaved = async () => {
  await Promise.all([loadList(), loadStats(), loadWards(), loadPending()]);
};
// ------------------------------------------------------------------
// 换床
// ------------------------------------------------------------------
const transferVisible = ref(false);
const transferRow = ref(null);
const openTransfer = (row) => {
  transferRow.value = row;
  transferVisible.value = true;
};
const onTransferSaved = async () => {
  await Promise.all([loadList(), loadWards()]);
};
// ------------------------------------------------------------------
// 出院办理
// ------------------------------------------------------------------
const dischargeVisible = ref(false);
const dischargeRow = ref(null);
const openDischarge = (row) => {
  dischargeRow.value = row;
  dischargeVisible.value = true;
};
const onDischargeSaved = async () => {
  await Promise.all([loadList(), loadStats(), loadWards()]);
};
// ------------------------------------------------------------------
// 病案首页
// ------------------------------------------------------------------
const summaryVisible = ref(false);
const summaryId = ref(null);
const openSummary = (row) => {
  summaryId.value = row.admissionId;
  summaryVisible.value = true;
};
const onSummarySaved = async () => {
  await loadList();
};
const confirmDischargeFromList = (row) => {
  ElMessageBox.confirm(`确认为「${row.patientName}」办理出院？出院会释放床位并把住院天数、离院方式回写到病案首页。`, '出院确认', {
    type: 'warning',
    confirmButtonText: '去填出院信息',
    cancelButtonText: '取消'
  }).then(() => openDischarge(row)).catch(() => {
  });
};
onMounted(async () => {
  await Promise.all([loadWards(), loadEmployees()]);
  await Promise.all([loadList(), loadStats(), loadPending()]);
});
</script>
