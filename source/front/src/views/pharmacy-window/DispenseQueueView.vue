<template>
  <div>
    <!-- 窗口工作台：选窗口 + 当前叫号 + 叫号动作 -->
    <el-card class="mb-3" shadow="never">
      <div class="flex flex-wrap items-center gap-4">
        <el-select v-model="windowId" class="!w-56" placeholder="选择本窗口" @change="handleWindowChange">
          <el-option v-for="w in windows" :key="w.id" :label="w.name" :value="w.id"/>
        </el-select>
        <div class="flex-1 rounded-lg bg-slate-50 px-6 py-3">
          <template v-if="current">
            <span class="text-3xl font-bold text-blue-600">{{ current.queueNo }}</span>
            <span class="ml-4 text-base text-slate-700">{{ current.patientName }}</span>
            <span class="ml-2 text-xs text-slate-500">{{ current.prescriptionNo }}</span>
            <span class="ml-4 text-xs text-slate-400">已叫 {{ current.callCount }} 次</span>
          </template>
          <span v-else class="text-sm text-slate-400">本窗口暂无叫号</span>
        </div>
        <el-button v-perm="'pharmacy:dispensing:edit'" size="large" type="primary" @click="handleCallNext">
          叫下一位
        </el-button>
        <el-button v-perm="'pharmacy:dispensing:edit'" :disabled="!current" @click="handleRecall">重叫</el-button>
      </div>
    </el-card>

    <!-- 签到取号 -->
    <el-card class="mb-3 query-card" ref="queryCardRef" shadow="never">
      <el-form :model="{signInPrescriptionNo}" inline @submit.prevent>
        <el-form-item label="处方号">
          <el-input v-model="signInPrescriptionNo" :prefix-icon="Search" class="!w-64" clearable
                    placeholder="扫处方单或手工输入" @keyup.enter="handleSignIn"/>
        </el-form-item>
        <el-form-item label="签到方式">
          <el-select v-model="signInType" class="!w-32">
            <el-option :value="1" label="身份证"/>
            <el-option :value="2" label="就诊卡"/>
            <el-option :value="3" label="扫码"/>
            <el-option :value="4" label="手工"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button v-perm="'pharmacy:dispensing:edit'" :icon="Search" type="primary" @click="handleSignIn">
            签到取号
          </el-button>
          <el-button @click="refreshAll">刷新</el-button>
        </el-form-item>
      </el-form>
      <p class="text-xs text-slate-400">
        患者缴费后才会有待发药明细；同一处方当天重复签到返回原号，不会重复发号。
      </p>
    </el-card>

    <!-- 队列 -->
    <el-card class="table-card" shadow="never">
      <div class="mb-2 flex items-center gap-2">
        <el-select v-model="statusFilter" class="!w-32" clearable placeholder="全部状态" @change="handleSearch">
          <el-option :value="1" label="待取药"/>
          <el-option :value="2" label="已发药"/>
          <el-option :value="3" label="已过号"/>
          <el-option :value="4" label="已取消"/>
        </el-select>
        <span class="text-xs text-slate-400">{{ queueDate }} 共 {{ pagination.total }} 个号</span>
      </div>
      <el-table v-loading="loading" :data="queueList" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column label="取药号" width="100">
          <template #default="{ row }">
            <span class="font-mono text-base font-bold">{{ row.queueNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" prop="patientName" width="110"/>
        <el-table-column class-name="font-mono" label="处方号" prop="prescriptionNo" width="180"/>
        <el-table-column align="center" label="发药进度" width="110">
          <template #default="{ row }">{{ row.dispensedItemCount ?? 0 }} / {{ row.prescriptionItemCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.queueStatus)" size="small">{{ statusText(row.queueStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="窗口" prop="windowName" width="150"/>
        <el-table-column label="签到时间" prop="signInTime" width="160"/>
        <el-table-column align="center" label="叫号次数" prop="callCount" width="90"/>
        <el-table-column label="发药时间" prop="dispenseTime" width="160"/>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <template v-if="row.queueStatus === 1">
              <el-button v-perm="'pharmacy:dispensing:edit'" link type="primary" @click="handleSkip(row)">过号</el-button>
              <el-button v-perm="'pharmacy:dispensing:edit'" link type="danger" @click="handleCancel(row)">取消</el-button>
            </template>
            <el-button v-else-if="row.queueStatus === 3" v-perm="'pharmacy:dispensing:edit'" link type="primary"
                       @click="handleRevive(row)">恢复排队
            </el-button>
            <span v-else class="text-slate-400">—</span>
            <el-button link type="info" @click="goDispense">去发药</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="queueList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        今天还没有患者取号
      </div>
      <div class="list-footer flex items-center justify-end" ref="footerRef">
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
  </div>
</template>

<script setup>
/**
 * 发药窗口取药队列
 *
 * 回答「患者到没到、队伍有多长、下一号叫谁」——这三件事缴费数据里都没有。
 * 药师的操作顺序：患者来了 → 扫处方号签到（发号）→ 点「叫下一位」（号绑到本窗口）
 * → 到发药页发药（整单发完由后端回写队列为已发药，这里不用再点）。
 *
 * ⚠ 窗口一旦选定，本页所有叫号动作都只作用于这个窗口：
 * 号被绑给别人窗口的患者，在这里叫不到，也不会出现在本窗口的叫号屏上。
 */
import {onMounted, onUnmounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Search} from '@element-plus/icons-vue';
import {getOpdStationSelectList} from '@/api/opdStation';
import {
  callNext,
  cancelQueue,
  dispenseSignIn,
  getCurrentCall,
  getQueueList,
  recall,
  reviveQueue,
  skipQueue,
} from '@/api/dispenseQueue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const router = useRouter();
const loading = ref(false);
const queueList = ref([]);
const windows = ref([]);
const windowId = ref(null);
const current = ref(null);
const signInPrescriptionNo = ref('');
const signInType = ref(3);
const statusFilter = ref(1);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
/** 叫号屏是给人看的，时间到秒即可，不显示毫秒 */
const queueDate = ref(todayStr());
let timer = null;

function todayStr() {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

const STATUS_TEXT = {1: '待取药', 2: '已发药', 3: '已过号', 4: '已取消'};
const STATUS_TYPE = {1: 'warning', 2: 'success', 3: 'info', 4: 'info'};
const statusText = (s) => STATUS_TEXT[s] || '未知';
const statusType = (s) => STATUS_TYPE[s] || 'info';

/**
 * 只列发药窗口（station_kind=6）。
 * 参数先按后端过滤，再按前端兜底过滤：接口忽略参数时不会把输液室/分诊台混进窗口下拉，
 * 但字段名不认识时（kind 字段缺失）放行，不把整张表过滤成空。
 */
const loadWindows = async () => {
  try {
    const res = await getOpdStationSelectList({stationKind: 6});
    const list = res.data || [];
    windows.value = list
        .filter((s) => {
          const kind = s.stationKind ?? s.station_kind;
          return kind === undefined || kind === null || kind === 6;
        })
        .map((s) => ({id: s.id, name: s.stationName || s.station_name || s.name}));
    if (!windowId.value && windows.value.length) windowId.value = windows.value[0].id;
  } catch (e) {
    console.error('加载发药窗口失败', e);
  }
};

const loadQueue = async () => {
  loading.value = true;
  try {
    queueDate.value = todayStr();
    const params = {queueDate: queueDate.value, pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize};
    if (statusFilter.value != null) params.queueStatus = statusFilter.value;
    const res = await getQueueList(params);
    queueList.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载取药队列失败', e);
  } finally {
    loading.value = false;
  }
};

const loadCurrent = async () => {
  if (!windowId.value) {
    current.value = null;
    return;
  }
  try {
    const res = await getCurrentCall(windowId.value);
    current.value = res.data || null;
  } catch (e) {
    // 轮询失败不打扰药师，下一轮再试
    current.value = null;
  }
};

const refreshAll = () => {
  loadQueue();
  loadCurrent();
};

const handleWindowChange = () => {
  loadCurrent();
};

const handleCallNext = async () => {
  if (!windowId.value) {
    ElMessage.warning('请先选择本窗口');
    return;
  }
  try {
    const res = await callNext(windowId.value);
    current.value = res.data || null;
    if (current.value) ElMessage.success(`请 ${current.value.queueNo} 号 ${current.value.patientName} 到本窗口`);
    refreshAll();
  } catch (e) {
    // 错误已由全局拦截器提示（如「当前没有等待取药的号」）
  }
};

const handleRecall = async () => {
  try {
    const res = await recall(windowId.value);
    current.value = res.data || current.value;
    ElMessage.success('已重叫');
  } catch (e) {
    // 已由全局拦截器提示
  }
};

const handleSignIn = async () => {
  const no = signInPrescriptionNo.value.trim();
  if (!no) {
    ElMessage.warning('请输入处方号');
    return;
  }
  try {
    const res = await dispenseSignIn({prescriptionNo: no, signInType: signInType.value});
    const list = res.data || [];
    const first = list[0];
    ElMessage.success(first ? `已取号 ${first.queueNo}（${first.patientName}）` : '签到成功');
    signInPrescriptionNo.value = '';
    refreshAll();
  } catch (e) {
    // 已由全局拦截器提示
  }
};

const handleSkip = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('请输入过号原因', `过号 - ${row.queueNo}`, {
      confirmButtonText: '确认过号',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：叫号三次未到',
      inputValidator: (v) => (v && v.trim() ? true : '过号原因不能为空'),
    });
    await skipQueue(row.id, value.trim());
    ElMessage.success('已过号，患者回来可在队列里恢复排队');
    refreshAll();
  } catch (e) {
    // 取消或错误已由全局拦截器提示
  }
};

const handleCancel = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('请输入取消原因', `取消 - ${row.queueNo}`, {
      confirmButtonText: '确认取消',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：患者放弃取药',
      inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空'),
    });
    await cancelQueue(row.id, value.trim());
    ElMessage.success('已取消该取药号');
    refreshAll();
  } catch (e) {
    // 取消或错误已由全局拦截器提示
  }
};

const handleRevive = async (row) => {
  try {
    await reviveQueue(row.id);
    ElMessage.success(`号 ${row.queueNo} 已恢复排队（号不变，排到队尾）`);
    refreshAll();
  } catch (e) {
    // 已由全局拦截器提示
  }
};

const goDispense = () => {
  router.push('/pharmacy-window');
};

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadQueue();
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.pageNum = 1;
  loadQueue();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadQueue();
};

onMounted(async () => {
  await loadWindows();
  refreshAll();
  // 10 秒轮询当前叫号：药师在别的窗口叫走号时，本窗口的显示要跟着变
  timer = setInterval(loadCurrent, 10000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>
