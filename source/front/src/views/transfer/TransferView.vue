<template>
  <div>
    <!-- 标题 + 在院患者过滤 -->
    <div class="mb-3 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
            v-model="filters.admissionId"
            class="!w-72"
            clearable
            data-testid="p4-transfer-admission"
            filterable
            placeholder="按在院患者过滤"
            @change="handleSearch"
        >
          <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
          />
        </el-select>
        <el-button v-perm="'ipd:transfer:add'" :icon="Plus" data-testid="p4-transfer-apply" type="primary"
                   @click="openApply()">
          发起转科
        </el-button>
        <el-button :icon="Refresh" @click="handleSearch">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-3">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">待接收转科</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-transfer-pending">{{ pendingCount }}</p>
        <p class="text-[11px] text-slate-400">发起后需转入科室接收才生效</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">当前筛选结果</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-transfer-total">{{ total }}</p>
        <p class="text-[11px] text-slate-400">共 {{ total }} 条转科轨迹</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">本页医嘱未停清</p>
        <p
            :class="pageUnsettledCount > 0 ? 'text-red-600' : 'text-slate-900'"
            class="text-lg font-bold"
            data-testid="p4-transfer-unsettled"
        >
          {{ pageUnsettledCount }}
        </p>
        <p class="text-[11px] text-slate-400">待校对医嘱需原科室医生处理</p>
      </div>
    </div>

    <!-- 筛选 + 表格（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="filters" inline @submit.prevent>
        <el-form-item label="状态">
          <el-select
              v-model="filters.transferStatus"
              class="!w-32"
              clearable
              data-testid="p4-transfer-filter-status"
              placeholder="状态"
              @change="handleSearch"
          >
            <el-option :value="0" label="待接收"/>
            <el-option :value="1" label="已完成"/>
            <el-option :value="2" label="已取消"/>
          </el-select>
        </el-form-item>
        <el-form-item label="转入科室">
          <el-select
              v-model="filters.toDeptId"
              class="!w-44"
              clearable
              data-testid="p4-transfer-filter-dept"
              filterable
              placeholder="转入科室"
              @change="handleSearch"
          >
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="转科类型">
          <el-select
              v-model="filters.transferType"
              class="!w-32"
              clearable
              data-testid="p4-transfer-filter-type"
              placeholder="转科类型"
              @change="handleSearch"
          >
            <el-option :value="1" label="普通转科"/>
            <el-option :value="2" label="急诊转科"/>
            <el-option :value="3" label="转入ICU"/>
            <el-option :value="4" label="ICU转出"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
              v-model="filters.keyword"
              :prefix-icon="Search"
              class="!w-72"
              clearable
              placeholder="转科单号 / 入院号 / 患者姓名 / 转科原因"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="p4-transfer-table" stripe
                style="width: 100%">
        <el-table-column label="转科单号" prop="transferNo" width="150"/>
        <el-table-column label="患者" min-width="140">
          <template #default="{ row }">
            <div class="text-slate-900">{{ text(row.patientName) }}</div>
            <div class="text-slate-400">{{ text(row.admissionNo) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="转出 → 转入" min-width="220">
          <template #default="{ row }">
            <span class="text-slate-700">{{ text(row.fromDeptName) }}</span>
            <span class="mx-1 text-slate-400">→</span>
            <span class="font-medium text-slate-900">{{ text(row.toDeptName) }}</span>
            <div class="text-slate-400">
              {{ text(row.fromWardName) }} {{ text(row.fromBedNo) }}床 →
              {{ text(row.toWardName) }} {{ text(row.toBedNo) }}床
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="类型" prop="transferTypeText" width="90"/>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.transferStatus)" size="small">
              {{ text(row.transferStatusText) }}
            </el-tag>
            <div class="mt-1 text-slate-400">{{ text(row.waitText) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="发起 / 接收" min-width="170">
          <template #default="{ row }">
            <div class="text-slate-700">{{ text(row.applyDoctorName) }}</div>
            <div class="text-slate-400">{{ fmt(row.applyTime) }}</div>
            <div v-if="row.receiveDoctorName" class="mt-1 text-slate-700">{{ row.receiveDoctorName }}</div>
            <div v-if="row.receiveTime" class="text-slate-400">{{ fmt(row.receiveTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="医嘱处置" min-width="220">
          <template #default="{ row }">
            <div v-if="!row.orderRemark" class="text-slate-400">—（待接收，尚未处置）</div>
            <div
                v-else
                :class="(row.orderRemark || '').includes('仍有') ? 'text-red-600' : 'text-slate-600'"
                class="leading-5"
            >
              {{ row.orderRemark }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="病历号" width="140">
          <template #default="{ row }">
            <span v-if="row.recordNo" class="text-slate-700">{{ row.recordNo }}</span>
            <span v-else class="text-slate-400">未回写</span>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="190">
          <template #default="{ row }">
            <el-button
                v-if="row.canAccept"
                v-perm="'ipd:transfer:edit'"
                data-testid="p4-transfer-accept"
                link
                type="primary"
                @click="openAccept(row)"
            >
              接收
            </el-button>
            <el-button
                v-if="row.canCancel"
                v-perm="'ipd:transfer:delete'"
                data-testid="p4-transfer-cancel"
                link
                type="warning"
                @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button data-testid="p4-transfer-detail" link type="info" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400" data-testid="p4-transfer-empty">
            暂无转科记录（点击右上角「发起转科」开始）
          </div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 发起转科：表单与提交在 TransferApplyEdit（只新增，无修改语义） -->
    <TransferApplyEdit v-model="applyVisible" :admissions="admissions" :depts="depts"
                       :preset-admission-id="filters.admissionId" @saved="afterApplySaved"/>

    <!-- 接收转科：表单与提交在 TransferReceiveEdit -->
    <TransferReceiveEdit v-model="acceptVisible" :row="acceptRow" @saved="afterAcceptSaved"/>

    <!-- 转科详情：只读展示在 TransferDetail（自己按 id 取数） -->
    <TransferDetail v-model="detailVisible" :id="detailId" :row="detailRow"/>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  cancelTransfer,
  getTransferListPage,
  getTransferPendingCount,
} from '@/api/inpatientTransfer';
import {getInpatientListPage} from '@/api/inpatient';
import {getDepartmentSelectList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {admissionLabel, fmt, statusTagType, text} from '@/lib/transfer';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// 拆分出的弹框组件
import TransferApplyEdit from './TransferApplyEdit.vue';
import TransferDetail from './TransferDetail.vue';
import TransferReceiveEdit from './TransferReceiveEdit.vue';

// ---------------- 基础数据 ----------------
const admissions = ref([]);
const depts = ref([]);
const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200});
    admissions.value = (res.data?.records || []);
  } catch (error) {
    console.error('加载在院患者失败:', error);
  }
};
const loadDepts = async () => {
  try {
    // 转科申请：目标科室 = 转出科室之外的范围，但**不能越过自己的授权范围**。
    // 不传 scope → 默认按当前人过滤。
    const res = await getDepartmentSelectList({});
    depts.value = (res.data || []);
  } catch (error) {
    console.error('加载科室失败:', error);
  }
};
// ---------------- 列表 ----------------
const rows = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(DEFAULT_PAGE_SIZE);
const loading = ref(false);
const pendingCount = ref(0);
const filters = reactive({
  admissionId: '',
  toDeptId: '',
  transferStatus: '',
  transferType: '',
  keyword: '',
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getTransferListPage({
      admissionId: filters.admissionId || undefined,
      toDeptId: filters.toDeptId || undefined,
      transferStatus: filters.transferStatus === '' ? undefined : filters.transferStatus,
      transferType: filters.transferType === '' ? undefined : filters.transferType,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total || 0);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const loadPendingCount = async () => {
  try {
    const res = await getTransferPendingCount({});
    pendingCount.value = Number(res.data || 0);
  } catch (error) {
    console.error('加载待接收数失败:', error);
  }
};
/** 本页"医嘱没停干净"的行数：只认后端写出的「仍有 … 未停」，前端不自己拼这套规则 */
const pageUnsettledCount = computed(() => rows.value.filter((r) => (r.orderRemark || '').includes('仍有')).length);
const handleSearch = () => {
  pageNum.value = 1;
  loadList();
};
const resetFilters = () => {
  filters.admissionId = '';
  filters.toDeptId = '';
  filters.transferStatus = '';
  filters.transferType = '';
  filters.keyword = '';
  pageNum.value = 1;
  loadList();
};
// ---------------- 发起转科（表单与提交已拆至 TransferApplyEdit.vue，这里只管开关） ----------------
const applyVisible = ref(false);
const openApply = () => {
  applyVisible.value = true;
};
// ---------------- 接收 / 取消 / 详情 ----------------
const acceptVisible = ref(false);
const acceptRow = ref(null);
const openAccept = (row) => {
  acceptRow.value = row;
  acceptVisible.value = true;
};
const handleCancel = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`确认取消转科申请 ${row.transferNo || ''}（${row.patientName || ''}）？已接收的转科不能取消。`, '取消转科申请', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '取消原因（必填）',
      inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空'),
    });
    await cancelTransfer({transferId: row.id, cancelReason: value.trim()});
    ElMessage.success('转科申请已取消');
    await Promise.all([loadList(), loadPendingCount()]);
  } catch (error) {
    if (error === 'cancel' || error === 'close')
      return;
    
  }
};
// ---------------- 详情（展示在 TransferDetail.vue，子组件自己按 id 取数） ----------------
const detailVisible = ref(false);
const detailId = ref(null);
const detailRow = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailRow.value = row;
  detailVisible.value = true;
};
const afterApplySaved = async () => {
  await Promise.all([loadList(), loadPendingCount()]);
};
// 接收会换科室换床，在院患者名册（含床位）也变了，必须一起刷新
const afterAcceptSaved = async () => {
  await Promise.all([loadList(), loadPendingCount(), loadAdmissions()]);
};
onMounted(async () => {
  await Promise.all([loadAdmissions(), loadDepts()]);
  await Promise.all([loadList(), loadPendingCount()]);
});
</script>
