<template>
  <div class="p-5 space-y-4">
    <!-- 统计 -->
    <div class="grid grid-cols-6 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待预约申请</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1" data-testid="stat-pending">{{
            stats.pendingApplies
          }}
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日已预约</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1" data-testid="stat-booked">{{ stats.todayBooked }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日到检</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.todayArrived }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日完成</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.todayFinished }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日取消/爽约</div>
        <div class="text-2xl font-semibold text-gray-400 mt-1">{{ stats.todayCancelled + stats.todayNoShow }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">开放/暂停设备</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1" data-testid="stat-device">{{
            stats.deviceOpen
          }}/{{ stats.devicePaused }}
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="预约工作台" name="workbench">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="pendQuery.keyword" clearable data-testid="pend-keyword" placeholder="申请单号/患者/项目"
                      style="width: 220px" @keyup.enter="pendQuery.pageNum = 1; loadPending()"/>
            <el-select v-model="pendQuery.isEmergency" :fit-input-width="false" clearable placeholder="急诊"
                       style="width: 110px">
              <el-option :value="1" label="急诊"/>
              <el-option :value="0" label="普通"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="pendQuery.pageNum = 1; loadPending()">查询</el-button>
            <el-button :icon="Refresh" @click="loadPending">刷新</el-button>
            <span
                class="text-xs text-gray-400">列表只包含「已缴费（或急诊已提交）且没有 in-flight 预约」的申请单，口径由后端给出</span>
          </div>
          <el-table v-loading="pendLoading" :data="pendRows" data-testid="exam-pending-table" size="small">
            <el-table-column label="申请单号" prop="applyNo" width="180"/>
            <el-table-column label="患者" min-width="120">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{
                  row.patientNo
                }}</span></template>
            </el-table-column>
            <el-table-column label="检查项目" min-width="150" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="部位" prop="bodyPart" show-overflow-tooltip width="110"/>
            <el-table-column align="right" label="时长" width="70">
              <template #default="{ row }">{{ row.examMinutes }}′</template>
            </el-table-column>
            <el-table-column label="开单" min-width="130" show-overflow-tooltip>
              <template #default="{ row }">{{ row.applyDeptName }} {{ row.doctorName }}</template>
            </el-table-column>
            <el-table-column label="急诊" width="70">
              <template #default="{ row }">
                <el-tag v-if="Number(row.isEmergency) === 1" size="small" type="danger">急诊</el-tag>
                <span v-else class="text-gray-300">—</span>
              </template>
            </el-table-column>
            <el-table-column align="center" label="可承接设备" width="100">
              <template #default="{ row }">
                <el-tag v-if="!Number(row.deviceCount)" size="small" type="warning">无</el-tag>
                <span v-else>{{ row.deviceCount }} 台</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="100">
              <template #default="{ row }">
                <el-button v-perm="'medtech:examAppoint:add'" :disabled="!Number(row.deviceCount)" data-testid="btn-book" link
                           size="small"
                           type="primary" @click="openBook(row)">占号预约
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="pendQuery.pageNum" v-model:page-size="pendQuery.pageSize" :total="pendTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadPending"/>
          </div>
        </el-tab-pane>

        <!-- ============ B 预约台账 ============ -->
        <el-tab-pane label="预约台账" name="ledger">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-button v-for="c in apptCounts" :key="c.statusText" :type="(c.status === null ? apptQuery.status === null : Number(apptQuery.status) === Number(c.status)) ? 'primary' : ''" data-testid="appt-chip"
                       size="small"
                       @click="filterByStatus(c.status)">
              {{ c.statusText }} {{ c.count }}
            </el-button>
            <div class="flex-1"/>
            <el-button v-perm="'medtech:examAppoint:edit'" size="small" @click="runNoShow">补跑爽约扫描</el-button>
          </div>
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="apptQuery.apptNo" clearable data-testid="appt-no" placeholder="预约单号"
                      style="width: 180px"/>
            <el-input v-model="apptQuery.keyword" clearable placeholder="患者/项目/设备" style="width: 190px"/>
            <el-select v-model="apptQuery.deviceId" :fit-input-width="false" clearable filterable placeholder="设备"
                       style="width: 190px">
              <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.deviceName}（${d.deviceCode}）`"
                         :value="d.id"/>
            </el-select>
            <el-select v-model="apptQuery.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in apptStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-date-picker v-model="apptQuery.startDate" placeholder="开始日期" style="width: 140px" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-date-picker v-model="apptQuery.endDate" placeholder="结束日期" style="width: 140px" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-button :icon="Search" type="primary" @click="apptQuery.pageNum = 1; loadAppts()">查询</el-button>
            <el-button :icon="Refresh" @click="resetAppt">重置</el-button>
          </div>
          <el-table v-loading="apptLoading" :data="apptRows" data-testid="exam-appt-table" size="small">
            <el-table-column label="预约单号" prop="apptNo" width="150"/>
            <el-table-column label="患者" min-width="110">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{
                  row.patientNo
                }}</span></template>
            </el-table-column>
            <el-table-column label="检查项目" min-width="140" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="设备 / 机房" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.deviceName }}<span
                  class="text-gray-400 text-xs"> {{ row.roomName || '' }}</span></template>
            </el-table-column>
            <el-table-column label="检查时间" width="160">
              <template #default="{ row }">
                <span data-testid="appt-when">{{ row.examDate }} {{ row.timeRange }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="apptStatusTag(row.status)" data-testid="appt-status" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="占号人/时间" width="150">
              <template #default="{ row }">{{ row.bookBy }}<span class="text-gray-400 text-xs"> {{
                  fmtTime(row.bookTime)
                }}</span></template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="230">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="openDetail(row)">详情</el-button>
                <el-button v-if="Number(row.status) === APPT_STATUS.BOOKED" v-perm="'medtech:examAppoint:edit'" link
                           size="small" type="success" @click="doArrive(row)">到检
                </el-button>
                <el-button v-if="Number(row.status) === APPT_STATUS.ARRIVED" v-perm="'medtech:examAppoint:edit'" link
                           size="small" type="success" @click="doFinish(row)">完成
                </el-button>
                <el-button v-if="isMutable(row.status)" v-perm="'medtech:examAppoint:edit'" link size="small"
                           type="warning" @click="openReschedule(row)">改约
                </el-button>
                <el-button v-if="isMutable(row.status)" v-perm="'medtech:examAppoint:delete'" link size="small"
                           type="danger" @click="doCancel(row)">取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="apptQuery.pageNum" v-model:page-size="apptQuery.pageSize" :total="apptTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadAppts"/>
          </div>
        </el-tab-pane>

        <!-- ============ C 设备与号源 ============ -->
        <!-- 2026-09 菜单治理：设备档位与号源不属于预约中心业务（预约中心 = biz_exam_appoint 逐患者受理，
             本页签 = biz_exam_device 档位 + biz_exam_slot 号源的科室管理员低频配置），已拆为独立二级菜单
             「检查设备与号源」（400 医技医辅 / 2925，sql/181）。 -->
      </el-tabs>
    </div>

    <!-- ============ 占号 / 改约弹窗 ============ -->
    <ExamAppointmentEdit v-model="bookDlg.visible" :mode="bookDlg.mode" :target="bookDlg.target"
                         @saved="handleApptSaved"/>

    <!-- ============ 预约单详情 ============ -->
    <ExamAppointmentDetail v-model="detail.visible" :appt-id="detail.id"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  examAppointArrive,
  examAppointAutoNoShow,
  examAppointCancel,
  examAppointFinish,
  getExamAppointListPage,
  getExamAppointStats,
  getExamAppointStatusCount,
  getExamDeviceSelectList,
  getExamPendingListPage,
} from '@/api/examAppointment';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {APPT_STATUS, apptStatusTag, isMutable,} from '@/lib/examAppointment';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import ExamAppointmentEdit from './ExamAppointmentEdit.vue';
import ExamAppointmentDetail from './ExamAppointmentDetail.vue';

const activeTab = ref('workbench');
// ---------------- 字典 ----------------
const apptStatusDict = ref([]);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.EXAM_APPT_STATUS}`);
    apptStatusDict.value = res?.data?.[DICT_TYPE.EXAM_APPT_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 统计 ----------------
const stats = reactive({
  todayBooked: 0, todayArrived: 0, todayFinished: 0, todayCancelled: 0, todayNoShow: 0,
  pendingApplies: 0, activeTotal: 0, deviceOpen: 0, devicePaused: 0,
});
const loadStats = async () => {
  try {
    const res = await getExamAppointStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-');
const pendLoading = ref(false);
const pendRows = ref([]);
const pendTotal = ref(0);
const pendQuery = reactive({keyword: '', isEmergency: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadPending = async () => {
  pendLoading.value = true;
  try {
    const res = await getExamPendingListPage({
      keyword: pendQuery.keyword.trim() || undefined,
      isEmergency: pendQuery.isEmergency ?? undefined,
      pageNum: pendQuery.pageNum, pageSize: pendQuery.pageSize,
    });
    if (res.code === 200) {
      pendRows.value = res.data?.records || [];
      pendTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    pendLoading.value = false;
  }
};
// ---------------- 占号弹窗（预约 / 改约共用，见 ExamAppointmentEdit） ----------------
const bookDlg = reactive({visible: false, mode: 'book', target: null});
const openBook = (row) => {
  if (!Number(row.deviceCount || 0)) {
    ElMessage.warning(`项目「${row.itemName}」没有可承接的设备，请先到「设备与号源」配置`);
    return;
  }
  bookDlg.mode = 'book';
  bookDlg.target = row;
  bookDlg.visible = true;
};
const openReschedule = (row) => {
  bookDlg.mode = 'reschedule';
  bookDlg.target = row;
  bookDlg.visible = true;
};
const handleApptSaved = () => {
  loadPending();
  loadAppts();
  loadStats();
};
// ================= B 预约台账 =================
const apptLoading = ref(false);
const apptRows = ref([]);
const apptTotal = ref(0);
const apptCounts = ref([]);
const apptQuery = reactive({
  apptNo: '', keyword: '', status: null, deviceId: null,
  startDate: '', endDate: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadAppts = async () => {
  apptLoading.value = true;
  try {
    const q = {
      apptNo: apptQuery.apptNo.trim() || undefined,
      keyword: apptQuery.keyword.trim() || undefined,
      status: apptQuery.status ?? undefined,
      deviceId: apptQuery.deviceId ?? undefined,
      startDate: apptQuery.startDate || undefined,
      endDate: apptQuery.endDate || undefined,
      pageNum: apptQuery.pageNum, pageSize: apptQuery.pageSize,
    };
    const [page, cnt] = await Promise.all([getExamAppointListPage(q), getExamAppointStatusCount(q)]);
    if (page.code === 200) {
      apptRows.value = page.data?.records || [];
      apptTotal.value = Number(page.data?.total || 0);
    } else
      ElMessage.error(page.message || '查询失败');
    if (cnt.code === 200)
      apptCounts.value = cnt.data || [];
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    apptLoading.value = false;
  }
};
const resetAppt = () => {
  Object.assign(apptQuery, {
    apptNo: '',
    keyword: '',
    status: null,
    deviceId: null,
    startDate: '',
    endDate: '',
    pageNum: 1
  });
  loadAppts();
};
const filterByStatus = (s) => {
  apptQuery.status = s == null ? null : Number(s);
  apptQuery.pageNum = 1;
  loadAppts(); // 纯筛选，不携带任何写动作参数
};
const doArrive = async (row) => {
  const res = await examAppointArrive(row.id).catch((e) => ({message: e?.message}));
  if (res.code === 200) {
    ElMessage.success(res.message || '已到检');
  } else
    ElMessage.error(res.message || '操作失败');
  loadAppts();
  loadStats();
};
const doFinish = async (row) => {
  const res = await examAppointFinish(row.id).catch((e) => ({message: e?.message}));
  if (res.code === 200) {
    ElMessage.success(res.message || '已完成');
  } else
    ElMessage.error(res.message || '操作失败');
  loadAppts();
  loadStats();
};
const doCancel = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`取消预约 ${row.apptNo}（${row.patientName} ${row.examDate} ${row.timeRange}）`, '取消预约', {
      inputPlaceholder: '取消原因（必填，会写入备注留痕）',
      inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空'),
    });
    const res = await examAppointCancel({apptId: row.id, cancelReason: value.trim()});
    if (res.code === 200) {
      ElMessage.success(res.message || '已取消');
    } else
      ElMessage.error(res.message || '取消失败');
    loadAppts();
    loadPending();
    loadStats();
  } catch (e) { /* 用户取消弹窗 */
  }
};
const runNoShow = async () => {
  const res = await examAppointAutoNoShow().catch((e) => ({message: e?.message}));
  if (res.code === 200)
    ElMessage.success(res.message || '扫描完成');
  else
    ElMessage.error(res.message || '扫描失败');
  loadAppts();
  loadStats();
};
// 详情抽屉（详情加载见 ExamAppointmentDetail）
const detail = reactive({visible: false, id: ''});
const openDetail = (row) => {
  detail.id = row.id;
  detail.visible = true;
};
// ================= 设备候选（预约台账的设备筛选下拉） =================
// 设备档位与号源的维护已拆到「检查设备与号源」菜单（2925，sql/181）；
// 本页只消费设备候选，故自行加载一次，不再借用对方的本地状态。
const deviceOptions = ref([]);
const loadDeviceOptions = async () => {
  try {
    const res = await getExamDeviceSelectList({});
    if (res.code === 200)
      deviceOptions.value = res.data || [];
  } catch (e) {
    console.error(e);
  }
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadPending();
  loadAppts();
  loadDeviceOptions();
});
</script>

<style scoped>
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
