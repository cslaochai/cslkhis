<template>
  <div class="p-4 space-y-4">
    <div class="bg-white rounded-lg shadow-sm px-5 py-4 flex flex-wrap items-center gap-5">
      <div class="min-w-[280px]">
        <div class="flex items-center gap-2">
          <h2 class="text-[18px] font-semibold text-gray-800">机器人调度</h2>
          <el-tag size="small" effect="plain">全院共享车队</el-tag>
        </div>
        <div class="text-[12px] text-gray-500 mt-1">
          站点/科室发起配送单 → 设备科选空闲机器人派车 → 装车配送 → 到站签收；急送插队，常规排队
        </div>
      </div>

      <div class="flex items-center gap-3">
        <el-select v-model="query.status" clearable :fit-input-width="false" style="width: 130px"
                   placeholder="状态" @change="query.pageNum = 1; loadOrders()">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"/>
        </el-select>
        <el-select v-model="query.priority" clearable :fit-input-width="false" style="width: 130px"
                   placeholder="优先级" @change="query.pageNum = 1; loadOrders()">
          <el-option v-for="p in priorityOptions" :key="p.value" :label="p.label" :value="p.value"/>
        </el-select>
        <el-input v-model="query.keyword" clearable placeholder="单号/物品/站点/发起人"
                  style="width: 220px" @keyup.enter="query.pageNum = 1; loadOrders()"/>
        <el-button type="primary" @click="query.pageNum = 1; loadOrders()">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <div class="ml-auto flex items-center gap-2 text-[12px] text-gray-400">
        <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
        自动刷新 10s · {{ lastRefresh || '' }}
        <el-button size="small" text @click="refreshAll">刷新</el-button>
      </div>
    </div>

    <div v-if="loadError" class="bg-white rounded-lg shadow-sm px-4 py-3">
      <el-alert :title="loadError" type="error" show-icon :closable="false"/>
    </div>

    <!-- 车队状态 -->
    <div class="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3">
      <div v-for="r in fleet" :key="r.robotNo"
           class="bg-white rounded-lg shadow-sm px-4 py-3 space-y-2">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-[14px] font-semibold text-gray-800">{{ r.deviceName || r.robotNo }}</div>
            <div class="text-[12px] text-gray-400 font-mono">{{ r.robotNo }}</div>
          </div>
          <el-tag :type="stateTagType(r.state)" size="small">{{ r.stateLabel || '离线' }}</el-tag>
        </div>
        <div class="flex items-center gap-2 text-[12px] text-gray-500">
          <span>电量</span>
          <el-progress :percentage="Math.max(0, Math.min(100, Math.round(r.battery ?? 0)))"
                       :stroke-width="8" :color="batteryColor(r.battery)" class="flex-1"/>
          <span class="font-mono w-9 text-right">{{ r.battery ?? '—' }}%</span>
        </div>
        <div class="text-[12px] text-gray-500 truncate">
          {{ r.currentOrderNo ? `执行 ${r.currentOrderNo}` : (r.locationText || r.location || '待命') }}
        </div>
        <div class="text-[12px] text-gray-400 truncate">
          <template v-if="r.currentGoods">{{ r.currentGoods }} → {{ r.currentToStation }}</template>
          <template v-else>载重 {{ r.loadKg ?? 0 }}kg · {{ r.boxCount ?? 0 }} 箱</template>
        </div>
      </div>
    </div>

    <!-- 任务单 -->
    <div class="bg-white rounded-lg shadow-sm">
      <div class="flex items-center gap-3 px-4 pt-3 pb-2">
        <el-button v-perm="'iot:robotDispatch:apply'" type="primary" @click="openCreate()">新建派单</el-button>
        <span class="text-[12px] text-gray-400">待调度任务由设备科派车；车辆忙碌时任务留在队列，空闲后自动可选</span>
      </div>

      <el-table v-loading="loading" :data="orders" max-height="520" row-key="id">
        <el-table-column label="单号" width="160">
          <template #default="{row}"><span class="font-mono text-[13px]">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column label="来源 / 发起人" min-width="150">
          <template #default="{row}">
            <div>{{ row.sourceTypeLabel }}<span v-if="row.sourceDeptName"> · {{ row.sourceDeptName }}</span></div>
            <div class="text-[12px] text-gray-400">{{ row.requesterName || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="物品" min-width="200">
          <template #default="{row}">
            <div>{{ row.goodsName }}</div>
            <div class="text-[12px] text-gray-400">{{ row.goodsType }} · {{ row.boxCount }} 箱 {{ row.loadKg }}kg</div>
          </template>
        </el-table-column>
        <el-table-column label="路线" min-width="190">
          <template #default="{row}">
            <div>{{ row.fromStation }}</div>
            <div class="text-[12px] text-gray-400">→ {{ row.toFloor }}F {{ row.toStation }}</div>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="90">
          <template #default="{row}">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ row.priorityLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="机器人" min-width="130">
          <template #default="{row}">
            <div>{{ row.robotName || '—' }}</div>
            <div v-if="row.robotNo" class="text-[12px] text-gray-400 font-mono">{{ row.robotNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="签收人" width="120">
          <template #default="{row}">{{ row.signer || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{row}">
            <el-button size="small" text type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-perm="'iot:robotDispatch:assign'" v-if="row.status === 1" size="small" text type="primary"
                       @click="openAssign(row)">派车</el-button>
            <el-button v-perm="'iot:robotDispatch:cancel'" v-if="[1, 2].includes(row.status)"
                       size="small" text type="danger" @click="cancelOrder(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex items-center justify-end px-4 py-3">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                       @size-change="loadOrders" @current-change="loadOrders"/>
      </div>
    </div>

    <!-- 新建派单 -->
    <el-dialog v-model="createVisible" title="新建配送任务单" width="560px" @closed="resetForm">
      <el-form ref="createFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="发起来源" prop="sourceType">
          <el-select v-model="form.sourceType" style="width: 100%">
            <el-option v-for="s in sourceOptions" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="发起站点" prop="sourceDeptName">
          <el-input v-model="form.sourceDeptName" placeholder="如 B1 静配中心 / 住院药房"/>
        </el-form-item>
        <el-form-item label="物品品类" prop="goodsType">
          <el-select v-model="form.goodsType" style="width: 100%">
            <el-option v-for="g in goodsTypeOptions" :key="g" :label="g" :value="g"/>
          </el-select>
        </el-form-item>
        <el-form-item label="物品明细" prop="goodsName">
          <el-input v-model="form.goodsName" placeholder="如 0.9%氯化钠注射液 250ml×24 瓶"/>
        </el-form-item>
        <el-form-item label="取货点" prop="fromStation">
          <el-input v-model="form.fromStation" placeholder="如 B1 中心药房"/>
        </el-form-item>
        <el-form-item label="目标楼层" prop="toFloor">
          <el-input-number v-model="form.toFloor" :min="1" :max="20" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="收货站点" prop="toStation">
          <el-input v-model="form.toStation" placeholder="如 12F 护士站"/>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio v-for="p in priorityOptions" :key="p.value" :value="p.value">{{ p.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="箱数" prop="boxCount">
          <el-input-number v-model="form.boxCount" :min="1" :max="4"/>
        </el-form-item>
        <el-form-item label="总重(kg)" prop="loadKg">
          <el-input-number v-model="form.loadKg" :min="0" :max="50" :step="0.5"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交并派车</el-button>
      </template>
    </el-dialog>

    <!-- 派车 -->
    <el-dialog v-model="assignVisible" title="调度派车" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="单号">{{ assignRow?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="物品">{{ assignRow?.goodsName }}（{{ assignRow?.boxCount }} 箱 {{ assignRow?.loadKg }}kg）</el-descriptions-item>
        <el-descriptions-item label="路线">{{ assignRow?.fromStation }} → {{ assignRow?.toFloor }}F {{ assignRow?.toStation }}</el-descriptions-item>
      </el-descriptions>
      <el-form class="mt-4" label-width="90px">
        <el-form-item label="机器人">
          <el-select v-model="assignRobotNo" clearable placeholder="自动选择空闲车辆" style="width: 100%">
            <el-option label="自动选车（空闲 + 电量最高）" value=""/>
            <el-option v-for="r in freeFleet" :key="r.robotNo"
                       :label="`${r.deviceName || r.robotNo}（电量 ${r.battery ?? '—'}%）`" :value="r.robotNo"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmAssign">确认派车</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="任务单详情" width="600px">
      <el-descriptions v-if="detailRow" :column="2" border>
        <el-descriptions-item label="单号">{{ detailRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailRow.status)" size="small">{{ detailRow.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ detailRow.sourceTypeLabel }} · {{ detailRow.sourceDeptName }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detailRow.requesterName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="品类">{{ detailRow.goodsType }}</el-descriptions-item>
        <el-descriptions-item label="物品">{{ detailRow.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="取货点">{{ detailRow.fromStation }}</el-descriptions-item>
        <el-descriptions-item label="收货站点">{{ detailRow.toFloor }}F {{ detailRow.toStation }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ detailRow.priorityLabel }}</el-descriptions-item>
        <el-descriptions-item label="箱数/重量">{{ detailRow.boxCount }} 箱 / {{ detailRow.loadKg }}kg</el-descriptions-item>
        <el-descriptions-item label="机器人">{{ detailRow.robotName || '—' }}（{{ detailRow.robotNo || '未分配' }}）</el-descriptions-item>
        <el-descriptions-item label="签收人">{{ detailRow.signer || '—' }}</el-descriptions-item>
        <el-descriptions-item label="派单人">{{ detailRow.dispatchBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="派单时间">{{ shortTime(detailRow.dispatchTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ shortTime(detailRow.completeTime) }}</el-descriptions-item>
        <el-descriptions-item label="全程用时">{{ durationText(detailRow.totalSec) }}</el-descriptions-item>
        <el-descriptions-item label="里程">{{ detailRow.odometerM != null ? `${detailRow.odometerM} 米` : '—' }}</el-descriptions-item>
        <el-descriptions-item label="乘梯用时" v-if="detailRow.elevatorSec != null">
          排队 {{ detailRow.queueSec ?? 0 }} 秒 · 运行 {{ detailRow.rideSec ?? 0 }} 秒
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2" v-if="detailRow.cancelReason">{{ detailRow.cancelReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  robotDispatchAssign,
  robotDispatchCancel,
  robotDispatchCreate,
  robotDispatchFleet,
  robotDispatchListPage,
} from '@/api/robotDispatch';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';

const loading = ref(false);
const submitting = ref(false);
const loadError = ref('');
const fleet = ref([]);
const orders = ref([]);
const total = ref(0);
const lastRefresh = ref('');
const query = reactive({status: null, priority: null, keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});

const createVisible = ref(false);
const assignVisible = ref(false);
const detailVisible = ref(false);
const assignRow = ref(null);
const assignRobotNo = ref('');
const detailRow = ref(null);
const createFormRef = ref(null);

const statusOptions = [
  {value: 1, label: '待调度'},
  {value: 2, label: '已派单'},
  {value: 3, label: '配送中'},
  {value: 4, label: '已签收'},
  {value: 5, label: '已取消'},
  {value: 6, label: '异常'},
];
const priorityOptions = [
  {value: 1, label: '常规'},
  {value: 2, label: '批量补货'},
  {value: 3, label: '急送'},
];
const sourceOptions = [
  {value: 'PIVAS', label: '静配中心'},
  {value: 'PHARMACY', label: '住院药房'},
  {value: 'LAB', label: '检验科'},
  {value: 'WAREHOUSE', label: '中心库房'},
  {value: 'CSSD', label: '消毒供应中心'},
  {value: 'LAUNDRY', label: '洗涤中心'},
  {value: 'MANUAL', label: '手工派单'},
];
const goodsTypeOptions = ['药品', '标本', '耗材', '器械包', '被服'];

const form = reactive({
  sourceType: 'MANUAL',
  sourceDeptName: '',
  goodsType: '药品',
  goodsName: '',
  fromStation: 'B1 中心药房',
  toFloor: 12,
  toStation: '护士站',
  priority: 1,
  boxCount: 1,
  loadKg: 3,
  remark: '',
});

const rules = {
  goodsName: [{required: true, message: '请填写物品明细', trigger: 'blur'}],
  fromStation: [{required: true, message: '请填写取货点', trigger: 'blur'}],
  toStation: [{required: true, message: '请填写收货站点', trigger: 'blur'}],
};

const statusTagType = (s) => (s === 1 ? 'warning' : s === 2 ? 'info' : s === 3 ? 'primary'
    : s === 4 ? 'success' : s === 6 ? 'danger' : 'info');
const priorityTagType = (p) => (p === 3 ? 'danger' : p === 2 ? 'warning' : 'info');
const stateTagType = (s) => (s === 'IDLE' ? 'success' : ['DELIVERING', 'LOADING', 'SIGNING', 'WAITING_ELEVATOR', 'RETURNING'].includes(s)
    ? 'primary' : s === 'CHARGING' ? 'warning' : s === 'ALARM' ? 'danger' : 'info');
const batteryColor = (b) => (b == null ? '#C0C4CC' : b < 20 ? '#F56C6C' : b < 50 ? '#E6A23C' : '#0E9488');
const shortTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—');
const durationText = (sec) => {
  if (sec == null) return '—';
  const m = Math.floor(sec / 60);
  return m > 0 ? `${m} 分 ${sec % 60} 秒` : `${sec} 秒`;
};

const freeFleet = ref([]);

const loadFleet = async () => {
  try {
    const res = await robotDispatchFleet();
    fleet.value = res.data || [];
    freeFleet.value = fleet.value.filter(r => !r.currentOrderNo && r.state !== 'OFFLINE');
  } catch (e) {
    console.error('加载车队状态失败', e);
  }
};

const loadOrders = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const res = await robotDispatchListPage({
      status: query.status,
      priority: query.priority,
      keyword: query.keyword.trim(),
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    orders.value = res.data?.records || [];
    total.value = res.data?.total || 0;
    lastRefresh.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
  } catch (e) {
    console.error('加载配送任务单失败', e);
    loadError.value = e?.response?.data?.message || e?.message || '任务单加载失败';
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const refreshAll = async () => Promise.all([loadFleet(), loadOrders()]);

const resetQuery = () => {
  query.status = null;
  query.priority = null;
  query.keyword = '';
  query.pageNum = 1;
  loadOrders();
};

const resetForm = () => {
  form.sourceType = 'MANUAL';
  form.sourceDeptName = '';
  form.goodsType = '药品';
  form.goodsName = '';
  form.fromStation = 'B1 中心药房';
  form.toFloor = 12;
  form.toStation = '护士站';
  form.priority = 1;
  form.boxCount = 1;
  form.loadKg = 3;
  form.remark = '';
  createFormRef.value?.clearValidate();
};

const openCreate = () => {
  resetForm();
  createVisible.value = true;
};

const submitCreate = async () => {
  await createFormRef.value.validate();
  submitting.value = true;
  try {
    await robotDispatchCreate({...form, autoAssign: true});
    ElMessage.success('任务单已提交并派车');
    createVisible.value = false;
    await refreshAll();
  } catch (e) {
    console.error('新建配送任务单失败', e);
    ElMessage.error(e?.response?.data?.message || '提交失败');
  } finally {
    submitting.value = false;
  }
};

const openAssign = (row) => {
  assignRow.value = row;
  assignRobotNo.value = '';
  assignVisible.value = true;
};

const confirmAssign = async () => {
  submitting.value = true;
  try {
    await robotDispatchAssign({id: assignRow.value.id, robotNo: assignRobotNo.value || null});
    ElMessage.success('已派车，指令下发至机器人');
    assignVisible.value = false;
    await refreshAll();
  } catch (e) {
    console.error('派车失败', e);
    ElMessage.error(e?.response?.data?.message || '派车失败');
  } finally {
    submitting.value = false;
  }
};

const cancelOrder = async (row) => {
  let reason = '';
  try {
    const r = await ElMessageBox.prompt(`取消任务单 ${row.orderNo}？`, '取消派单', {
      confirmButtonText: '确认取消', cancelButtonText: '返回', inputPlaceholder: '取消原因（可空）',
    });
    reason = r.value || '';
  } catch (e) {
    return;
  }
  try {
    await robotDispatchCancel({id: row.id, cancelReason: reason});
    ElMessage.success('任务单已取消');
    await refreshAll();
  } catch (e) {
    console.error('取消任务单失败', e);
    ElMessage.error(e?.response?.data?.message || '取消失败');
  }
};

const openDetail = (row) => {
  detailRow.value = row;
  detailVisible.value = true;
};

let timer = null;
onMounted(() => {
  refreshAll();
  timer = setInterval(refreshAll, 10_000);
});
onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>
