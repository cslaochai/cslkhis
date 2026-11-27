<template>
  <div class="p-4 space-y-4">
    <div class="bg-white rounded-lg shadow-sm px-5 py-4 flex flex-wrap items-center gap-5">
      <div class="min-w-[280px]">
        <div class="flex items-center gap-2">
          <h2 class="text-[18px] font-semibold text-gray-800">设备共享调配</h2>
          <el-tag size="small" effect="plain">设备科共享池</el-tag>
        </div>
        <div class="text-[12px] text-gray-500 mt-1">
          临床科室申请 → 护士长/设备科审批 → 设备科发放并绑定 → 使用后归还 → 设备科验收
        </div>
      </div>

      <div class="flex items-center gap-3">
        <span class="text-[13px] text-gray-500">状态</span>
        <el-select v-model="query.status" clearable :fit-input-width="false" style="width: 130px"
                   placeholder="全部" @change="query.pageNum = 1; loadOrders()">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"/>
        </el-select>
        <el-input v-model="query.keyword" clearable placeholder="单号/患者/床位/申请人/科室"
                  style="width: 220px" @keyup.enter="query.pageNum = 1; loadOrders()"/>
        <el-button type="primary" @click="query.pageNum = 1; loadOrders()">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <div class="ml-auto flex items-center gap-5 text-center">
        <div>
          <div class="text-[22px] font-bold leading-6 text-orange-600">{{ stats.pending }}</div>
          <div class="text-[12px] text-gray-500">待审批</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6 text-[#1269B5]">{{ stats.toIssue }}</div>
          <div class="text-[12px] text-gray-500">待发放</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6 text-[#0E9488]">{{ stats.inUse }}</div>
          <div class="text-[12px] text-gray-500">使用中</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6 text-orange-600">{{ stats.pendingAccept }}</div>
          <div class="text-[12px] text-gray-500">待验收</div>
        </div>
        <div>
          <div class="text-[22px] font-bold leading-6" :class="stats.available ? 'text-gray-800' : 'text-red-600'">
            {{ stats.available }}
          </div>
          <div class="text-[12px] text-gray-500">共享池可借</div>
        </div>
      </div>

      <div class="flex items-center gap-2 text-[12px] text-gray-400">
        <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
        自动刷新 15s · {{ lastRefresh || '' }}
        <el-button size="small" text @click="refreshAll">刷新</el-button>
      </div>
    </div>

    <div v-if="loadError" class="bg-white rounded-lg shadow-sm px-4 py-3">
      <el-alert :title="loadError" type="error" show-icon :closable="false"/>
    </div>

    <div class="bg-white rounded-lg shadow-sm">
      <el-tabs v-model="tab" class="px-4 pt-2">
        <el-tab-pane label="申请单" name="orders">
          <div class="flex items-center gap-3 pb-3">
            <el-button v-perm="'asset:deviceRental:apply'" type="primary" @click="openApply()">发起申请</el-button>
            <span class="text-[12px] text-gray-400">申请只登记设备类别/用途；具体泵号/资产编号由设备科发放时分配</span>
          </div>

          <el-table v-loading="loading" :data="orders" max-height="560" row-key="id">
            <el-table-column type="expand">
              <template #default="{row}">
                <div class="px-8 py-2 bg-[#F7FAFD] text-[13px] space-y-2">
                  <div v-if="row.items?.length">
                    <span class="text-gray-500">设备明细：</span>
                    <span v-for="(it, i) in row.items" :key="it.id" class="mr-4">
                      {{ it.equipmentName || it.requestedName }}
                      <span v-if="it.deviceNo" class="text-[#1269B5]">（{{ it.deviceNo }}）</span>
                      <span class="text-gray-400">· {{ it.statusText }}</span>
                      <span v-if="it.bindId" class="text-gray-400"> · 已绑定患者</span>
                      <span v-if="i < row.items.length - 1">；</span>
                    </span>
                  </div>
                  <div v-if="row.approveBy" class="text-gray-500">
                    审批：{{ row.approveBy }} {{ shortTime(row.approveTime) }}
                    <span v-if="row.approveRemark"> · {{ row.approveRemark }}</span>
                  </div>
                  <div v-if="row.issueBy" class="text-gray-500">
                    发放：{{ row.issueBy }} {{ shortTime(row.issueTime) }}
                  </div>
                  <div v-if="row.returnBy" class="text-gray-500">
                    归还申请：{{ row.returnBy }} {{ shortTime(row.returnTime) }} · {{ row.returnReason }}
                  </div>
                  <div v-if="row.acceptBy" class="text-gray-500">
                    验收：{{ row.acceptBy }} {{ shortTime(row.acceptTime) }}
                    <span v-if="row.acceptRemark"> · {{ row.acceptRemark }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="申请单号" width="156">
              <template #default="{row}"><span class="font-mono text-[13px]">{{ row.orderNo }}</span></template>
            </el-table-column>
            <el-table-column label="申请科室 / 人" min-width="150">
              <template #default="{row}">
                <div>{{ row.applicantDeptName || '' }}</div>
                <div class="text-[12px] text-gray-400">{{ row.applicantName || '' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="患者 / 床位" min-width="150">
              <template #default="{row}">
                <template v-if="row.admissionId">
                  <div>{{ row.patientName || '' }}</div>
                  <div class="text-[12px] text-gray-400">{{ row.bedNo || '' }} 床 · {{ row.wardName || '' }}</div>
                </template>
                <span v-else class="text-gray-400">科室公用</span>
              </template>
            </el-table-column>
            <el-table-column label="设备" min-width="220">
              <template #default="{row}">
                <span class="text-[13px]">{{ itemSummary(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="紧急" width="76">
              <template #default="{row}">
                <el-tag v-if="row.urgency >= 2" size="small" :type="row.urgency === 3 ? 'danger' : 'warning'">
                  {{ row.urgencyText }}
                </el-tag>
                <span v-else class="text-[12px] text-gray-400">普通</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="92">
              <template #default="{row}">
                <el-tag :type="statusTagType(row.status)" effect="plain">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="申请时间" width="128">
              <template #default="{row}"><span class="text-[12px]">{{ shortTime(row.createTime) }}</span></template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{row}">
                <el-button v-if="row.status === 1" v-perm="'asset:deviceRental:approve'" link type="primary"
                           size="small" @click="approve(row, true)">通过</el-button>
                <el-button v-if="row.status === 1" v-perm="'asset:deviceRental:approve'" link type="danger"
                           size="small" @click="approve(row, false)">驳回</el-button>
                <el-button v-if="row.status === 2" v-perm="'asset:deviceRental:issue'" link type="primary"
                           size="small" @click="issue(row)">发放</el-button>
                <el-button v-if="row.status === 3" v-perm="'asset:deviceRental:return'" link type="warning"
                           size="small" @click="markReturn(row)">归还</el-button>
                <el-button v-if="row.status === 4" v-perm="'asset:deviceRental:accept'" link type="success"
                           size="small" @click="accept(row)">验收</el-button>
                <el-button link size="small" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="list-footer flex items-center justify-end">
            <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                           layout="total, prev, pager, next" @current-change="loadOrders"/>
          </div>
        </el-tab-pane>

        <el-tab-pane label="共享池" name="catalog">
          <div class="flex items-center gap-3 pb-3">
            <el-input v-model="catalogKeyword" clearable placeholder="设备名称/编码/型号/科室"
                      style="width: 240px" @keyup.enter="loadCatalog"/>
            <el-button @click="loadCatalog">查询</el-button>
            <span class="text-[12px] text-gray-400">只列设备科标记为「共享池」的在用设备；冷链、气体、床垫、资产标签不在此列</span>
          </div>
          <el-table v-loading="catalogLoading" :data="catalog" max-height="560" row-key="id">
            <el-table-column label="设备" min-width="190">
              <template #default="{row}">
                <div class="font-medium text-gray-800">{{ row.equipmentName }}</div>
                <div class="text-[12px] text-gray-400">{{ row.equipmentCode }} · {{ row.brand || '' }} {{ row.model || '' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="归属科室" prop="deptName" width="130"/>
            <el-table-column label="可借 / 总数" width="110" align="center">
              <template #default="{row}">
                <span :class="row.availableCount ? 'text-[#0E9488] font-semibold' : 'text-red-600 font-semibold'">
                  {{ row.availableCount }} / {{ row.totalCount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="上次保养" width="112">
              <template #default="{row}">{{ row.lastMaintainDate || '' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="84">
              <template #default="{row}"><el-tag size="small" type="success" effect="plain">在用</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="94">
              <template #default="{row}">
                <el-button v-perm="'asset:deviceRental:apply'" link type="primary" size="small"
                           :disabled="!row.availableCount" @click="openApply(row.id)">申请</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="px-2 py-3 text-[12px] text-gray-400">
            共享池目录由设备科在「设备管理 · 共享池」维护；当前可借 = 总数 − 已绑定/已借出。
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <DeviceRentalApply v-model="applyVisible" :preset-equipment-id="presetEquipmentId" @saved="refreshAll"/>

    <el-dialog v-model="detailVisible" title="设备借用申请详情" width="760px" append-to-body>
      <template v-if="detailRow">
        <div class="text-[13px] space-y-1 mb-3">
          <div><span class="text-gray-400">单号：</span>{{ detailRow.orderNo }}</div>
          <div><span class="text-gray-400">申请人：</span>{{ detailRow.applicantDeptName }} · {{ detailRow.applicantName }}</div>
          <div><span class="text-gray-400">患者：</span>{{ detailRow.admissionId ? `${detailRow.patientName} · ${detailRow.bedNo}床` : '科室公用' }}</div>
          <div><span class="text-gray-400">原因：</span>{{ detailRow.purpose || '' }}</div>
          <div><span class="text-gray-400">送达：</span>{{ detailRow.deliveryPlace || '' }}</div>
          <div><span class="text-gray-400">状态：</span>{{ detailRow.statusText }}</div>
        </div>
        <el-table :data="detailRow.items || []" size="small">
          <el-table-column label="设备" prop="equipmentName" min-width="150"/>
          <el-table-column label="发放编号" prop="deviceNo" width="120">
            <template #default="{row}">{{ row.deviceNo || '待分配' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="statusText" width="90"/>
          <el-table-column label="备注" prop="remark" min-width="150"/>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 设备共享调配（物资设备 · 设备共享调配，菜单 380）。
 *
 * 业务口径对齐设备科共享调配中心：
 *  - 共享池 = sys_equipment 中 rentable=1 的在用设备；不是所有 IoT 基础设施。
 *  - 临床按设备类型/数量发起申请，设备科审批后发放具体设备；
 *  - IoT 泵发放时同时绑定患者（biz_iot_device_bind），普通设备只登记资产编号；
 *  - 归还由临床发起，设备科验收后恢复可借库存。
 */
import {onMounted, onUnmounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  deviceRentalApprove,
  deviceRentalCatalog,
  deviceRentalAccept,
  deviceRentalIssue,
  deviceRentalListPage,
  deviceRentalReturn,
} from '@/api/deviceRental';
import DeviceRentalApply from './DeviceRentalApply.vue';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';

const tab = ref('orders');
const loading = ref(false);
const catalogLoading = ref(false);
const loadError = ref('');
const orders = ref([]);
const catalog = ref([]);
const total = ref(0);
const lastRefresh = ref('');
const catalogKeyword = ref('');
const query = reactive({status: null, keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const stats = reactive({pending: 0, toIssue: 0, inUse: 0, pendingAccept: 0, available: 0});

const applyVisible = ref(false);
const presetEquipmentId = ref(null);
const detailVisible = ref(false);
const detailRow = ref(null);

const statusOptions = [
  {value: 1, label: '待审批'},
  {value: 2, label: '待发放'},
  {value: 3, label: '使用中'},
  {value: 4, label: '待验收'},
  {value: 5, label: '已完成'},
  {value: 6, label: '已取消'},
  {value: 7, label: '已驳回'},
];

const statusTagType = (s) => (
    s === 1 ? 'warning' : s === 2 ? 'info' : s === 3 ? 'success' : s === 4 ? 'warning'
        : s === 5 ? 'success' : s === 7 ? 'danger' : 'info'
);
const shortTime = (t) => (t ? String(t).replace('T', ' ').slice(5, 16) : '');
const itemSummary = (row) => (row.items || []).map(i => i.equipmentName || i.deviceNo || '设备').join('、') || '';

const refreshAll = async () => {
  await Promise.all([loadOrders(), loadStats(), loadCatalog()]);
};

const loadOrders = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const res = await deviceRentalListPage({
      status: query.status,
      keyword: query.keyword.trim(),
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    orders.value = res.data?.records || [];
    total.value = res.data?.total || 0;
    lastRefresh.value = new Date().toLocaleTimeString('zh-CN', {hour12: false});
  } catch (e) {
    console.error('加载设备借用申请失败', e);
    loadError.value = e?.response?.data?.message || e?.message || '申请单加载失败';
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    const res = await deviceRentalListPage({pageNum: 1, pageSize: 200});
    const rows = res.data?.records || [];
    stats.pending = rows.filter(r => r.status === 1).length;
    stats.toIssue = rows.filter(r => r.status === 2).length;
    stats.inUse = rows.filter(r => r.status === 3).length;
    stats.pendingAccept = rows.filter(r => r.status === 4).length;
  } catch (e) {
    console.error('加载设备借用统计失败', e);
  }
};

const loadCatalog = async () => {
  catalogLoading.value = true;
  try {
    const res = await deviceRentalCatalog(catalogKeyword.value.trim());
    catalog.value = res.data || [];
    stats.available = catalog.value.reduce((n, e) => n + (e.availableCount || 0), 0);
  } catch (e) {
    console.error('加载共享池失败', e);
    catalog.value = [];
    ElMessage.error(e?.response?.data?.message || '共享池加载失败');
  } finally {
    catalogLoading.value = false;
  }
};

const resetQuery = () => {
  query.status = null;
  query.keyword = '';
  query.pageNum = 1;
  loadOrders();
};

const openApply = (equipmentId = null) => {
  presetEquipmentId.value = equipmentId;
  applyVisible.value = true;
};

const openDetail = (row) => {
  detailRow.value = row;
  detailVisible.value = true;
};

const approve = async (row, approved) => {
  let remark = '';
  try {
    const r = await ElMessageBox.prompt(
        approved ? `通过 ${row.orderNo} 的借用申请？` : `驳回 ${row.orderNo} 的借用申请？`,
        approved ? '审批通过' : '审批驳回',
        {inputValue: approved ? '同意借用，按共享池发放' : '', confirmButtonText: approved ? '通过' : '驳回',
          cancelButtonText: '取消', inputPlaceholder: '审批意见（可空）'},
    );
    remark = r.value || '';
  } catch (e) {
    return;
  }
  try {
    await deviceRentalApprove({id: row.id, approved, remark});
    ElMessage.success(approved ? '已通过，进入待发放' : '已驳回');
    await refreshAll();
  } catch (e) {
    console.error('审批设备借用失败', e);
    ElMessage.error(e?.response?.data?.message || '审批失败');
  }
};

const issue = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确认发放 ${row.orderNo}？系统会从共享池分配具体设备，有物联网编号的设备同时绑定到患者。`,
        '设备发放',
        {confirmButtonText: '确认发放', cancelButtonText: '取消', type: 'warning'},
    );
  } catch (e) {
    return;
  }
  try {
    await deviceRentalIssue({id: row.id});
    ElMessage.success('已发放；IoT 设备已绑定患者');
    await refreshAll();
  } catch (e) {
    console.error('发放设备失败', e);
    ElMessage.error(e?.response?.data?.message || '发放失败');
  }
};

const markReturn = async (row) => {
  let reason;
  try {
    const r = await ElMessageBox.prompt(
        `申请归还 ${row.orderNo}，设备科验收后恢复库存。`,
        '设备归还',
        {inputValue: '治疗结束，设备归还', confirmButtonText: '提交归还', cancelButtonText: '取消',
          inputPlaceholder: '归还原因/设备状态'},
    );
    reason = r.value;
  } catch (e) {
    return;
  }
  try {
    await deviceRentalReturn({id: row.id, reason: reason || '治疗结束，设备归还'});
    ElMessage.success('已提交归还，等待设备科验收');
    await refreshAll();
  } catch (e) {
    console.error('提交设备归还失败', e);
    ElMessage.error(e?.response?.data?.message || '归还失败');
  }
};

const accept = async (row) => {
  try {
    await ElMessageBox.confirm(`确认验收 ${row.orderNo}？验收后设备恢复可借库存。`, '设备验收',
        {confirmButtonText: '确认验收', cancelButtonText: '取消', type: 'success'});
  } catch (e) {
    return;
  }
  try {
    await deviceRentalAccept({id: row.id});
    ElMessage.success('已验收，设备回到共享池');
    await refreshAll();
  } catch (e) {
    console.error('验收设备失败', e);
    ElMessage.error(e?.response?.data?.message || '验收失败');
  }
};

let timer = null;
onMounted(async () => {
  await refreshAll();
  timer = setInterval(() => {
    if (!document.hidden) refreshAll();
  }, 15_000);
});
onUnmounted(() => timer && clearInterval(timer));
</script>
