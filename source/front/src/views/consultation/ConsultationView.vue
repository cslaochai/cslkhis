<template>
  <div>
    <!-- 页头 -->
    <div class="mb-3 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
            v-model="admissionId"
            class="!w-72"
            clearable
            data-testid="p4-consult-admission"
            filterable
            placeholder="全部在院患者"
            @change="handleAdmissionChange"
        >
          <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)"
                     :value="String(a.admissionId)"/>
        </el-select>
        <el-button :icon="Refresh" data-testid="p4-consult-refresh" @click="reloadAll">刷新</el-button>
        <el-button v-perm="'ipd:consultation:add'" :icon="Plus" data-testid="p4-consult-apply" type="primary"
                   @click="openApply">申请会诊
        </el-button>
      </div>
    </div>

    <!-- 统计卡片：数字全部来自后端 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">未完成会诊</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-unfinished">{{ unfinishedCount }}</p>
        <p class="text-[11px] text-slate-400">待应答 + 已应答</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">急会诊待应答</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-urgent">{{ urgentPendingCount }}</p>
        <p class="text-[11px] text-slate-400">时限 10 分钟，超时在列表标红</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已完成</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-finished">{{ finishedCount }}</p>
        <p class="text-[11px] text-slate-400">完成即回写病历</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">当前筛选结果</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-consult-total">{{ total }}</p>
        <p class="text-[11px] text-slate-400">{{ currentAdmission?.patientName || '全部患者' }}</p>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="filters" inline @submit.prevent>
        <el-form-item label="会诊科室">
          <el-select v-model="filters.toDeptId" class="!w-44" clearable data-testid="p4-filter-dept"
                     placeholder="会诊科室" @change="handleSearch">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.consultStatus" class="!w-32" clearable data-testid="p4-filter-status"
                     placeholder="状态" @change="handleSearch">
            <el-option :value="0" label="待应答"/>
            <el-option :value="3" label="已应答"/>
            <el-option :value="1" label="已完成"/>
            <el-option :value="2" label="已取消"/>
          </el-select>
        </el-form-item>
        <el-form-item label="范围">
          <el-select v-model="filters.consultType" class="!w-32" clearable data-testid="p4-filter-type"
                     placeholder="范围" @change="handleSearch">
            <el-option :value="1" label="科内会诊"/>
            <el-option :value="2" label="科间会诊"/>
            <el-option :value="3" label="全院会诊"/>
          </el-select>
        </el-form-item>
        <el-form-item label="紧急">
          <el-select v-model="filters.isUrgent" class="!w-32" clearable data-testid="p4-filter-urgent"
                     placeholder="紧急" @change="handleSearch">
            <el-option :value="0" label="普通会诊"/>
            <el-option :value="1" label="急会诊"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
              v-model="filters.keyword"
              :prefix-icon="Search"
              class="!w-72"
              clearable
              data-testid="p4-filter-keyword"
              placeholder="会诊号 / 患者姓名 / 患者号 / 理由"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="filters.unfinishedOnly" data-testid="p4-filter-unfinished" @change="handleSearch">
            只看未完成
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="p4-consult-table" stripe>
        <el-table-column label="会诊号" prop="consultationNo" width="150"/>
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">
            <div class="text-slate-900">{{ text(row.patientName) }}</div>
            <div class="text-slate-400">{{ text(row.patientNo) }} · 床号 {{ text(row.bedNo) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申请 → 会诊科室" min-width="180">
          <template #default="{ row }">
            <span class="text-slate-700">{{ text(row.fromDeptName) }}</span>
            <span class="mx-1 text-slate-400">→</span>
            <span class="font-medium text-slate-900">{{ text(row.toDeptName) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="范围" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ text(row.consultTypeText) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="紧急" width="110">
          <template #default="{ row }">
            <el-tooltip v-if="row.overdue" :content="row.overdueText || ''" placement="top">
              <el-tag data-testid="p4-row-overdue" size="small" type="danger">急会诊超时</el-tag>
            </el-tooltip>
            <el-tag v-else :type="row.isUrgent === 1 ? 'warning' : 'info'" effect="plain" size="small">
              {{ text(row.isUrgentText) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会诊理由" min-width="200" prop="reason" show-overflow-tooltip/>
        <el-table-column label="申请医生" prop="applyDoctorName" width="100"/>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ fmt(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="等待 / 应答" width="130">
          <template #default="{ row }">{{ waitText(row) }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.consultStatus)" data-testid="p4-row-status" size="small">
              {{ text(row.consultStatusText) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="病历号" width="150">
          <template #default="{ row }">{{ text(row.recordNo) }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-button v-if="row.canAccept" v-perm="'ipd:consultation:edit'" link size="small" type="primary"
                       @click="handleAccept(row)">接诊
            </el-button>
            <el-button v-if="row.canFinish" v-perm="'ipd:consultation:edit'" link size="small" type="primary"
                       @click="openFinish(row)">完成
            </el-button>
            <el-button v-if="row.canCancel" v-perm="'ipd:consultation:delete'" link size="small" type="danger"
                       @click="openCancel(row)">取消
            </el-button>
            <el-button link size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-8 text-slate-400">暂无会诊记录</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            :current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 申请会诊 -->
    <ConsultationApply v-model="applyVisible" :depts="depts" :admission-id="admissionId" :admission="currentAdmission"
                       @saved="reloadAll"/>

    <!-- 完成会诊 -->
    <ConsultationFinish v-model="finishVisible" :row="finishRow" @saved="reloadAll"/>

    <!-- 取消会诊 -->
    <ConsultationCancel v-model="cancelVisible" :row="cancelRow" @saved="reloadAll"/>

    <!-- 详情 -->
    <ConsultationDetail v-model="detailVisible" :id="detailId" @finish="openFinish"/>
  </div>
</template>

<script setup>
/**
 * 住院会诊（P4.1：申请 → 应答 → 会诊记录 → 完成 → 回写病历）
 *
 * 这个页面替代了原来的硬编码演示壳（写死的 4 行假数据）—— 按项目规范，所有页面数据必须来自后端接口。
 *
 * 五条口径：
 * 1. **按钮可用性由后端给**（canAccept / canFinish / canCancel / canEdit），不按 consultStatus 码值 switch。
 * 2. **未应答不可完成、已应答不可取消**是后端铁律，前端只按 canFinish / canCancel 渲染，
 *    不做"看起来能点"的按钮，也不在本地拦截（本地拦截会掩盖后端规则的失效）。
 * 3. **完成会诊会自动回写住院病历**：完成后提示里带出返回的病历ID，列表的「病历号」列即证据链。
 * 4. **急会诊超时是后端算的**（overdue / overdueText），前端不重复算时间差。
 * 5. 所有 ID 都是字符串（雪花ID），不要 Number()。
 */
import {computed, onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {acceptConsultation, getConsultationListPage, getConsultationUnfinishedCount} from '@/api/inpatientConsultation';
import {getInpatientListPage} from '@/api/inpatient';
import {getDepartmentSelectList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {admissionLabel, fmt, text} from '@/lib/consultation';
import ConsultationApply from './ConsultationApply.vue';
import ConsultationFinish from './ConsultationFinish.vue';
import ConsultationCancel from './ConsultationCancel.vue';
import ConsultationDetail from './ConsultationDetail.vue';
// ---------------- 基础数据 ----------------
const admissions = ref([]);
const depts = ref([]);
const admissionId = ref('');
const currentAdmission = computed(() => admissions.value.find((a) => String(a.admissionId) === String(admissionId.value)));
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
    // 不传 scope → 默认按当前人过滤（医生只看到自己被授权的科室）
    const res = await getDepartmentSelectList({});
    depts.value = (res.data || []);
  } catch (error) {
    console.error('加载科室失败:', error);
  }
};
// ---------------- 列表 ----------------
const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const filters = ref({
  toDeptId: '',
  consultStatus: undefined,
  consultType: undefined,
  isUrgent: undefined,
  keyword: '',
  unfinishedOnly: false,
});
const queryParams = (extra = {}) => ({
  pageNum: pagination.value.pageNum,
  pageSize: pagination.value.pageSize,
  ...(admissionId.value ? {admissionId: admissionId.value} : {}),
  ...(filters.value.toDeptId ? {toDeptId: filters.value.toDeptId} : {}),
  ...(filters.value.consultStatus !== undefined ? {consultStatus: filters.value.consultStatus} : {}),
  ...(filters.value.consultType !== undefined ? {consultType: filters.value.consultType} : {}),
  ...(filters.value.isUrgent !== undefined ? {isUrgent: filters.value.isUrgent} : {}),
  ...(filters.value.keyword ? {keyword: filters.value.keyword} : {}),
  ...(filters.value.unfinishedOnly ? {unfinishedOnly: 1} : {}),
  ...extra,
});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getConsultationListPage(queryParams());
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total || 0);
  } catch (error) {
    ElMessage.error(error.message || '加载会诊列表失败');
  } finally {
    loading.value = false;
  }
};
// ---------------- 统计（全部来自后端，前端不自己数当前页） ----------------
const unfinishedCount = ref(0);
const urgentPendingCount = ref(0);
const finishedCount = ref(0);
const loadCounts = async () => {
  try {
    const [unfinished, urgent, finished] = await Promise.all([
      getConsultationUnfinishedCount({
        ...(admissionId.value ? {admissionId: admissionId.value} : {}),
        ...(filters.value.toDeptId ? {toDeptId: filters.value.toDeptId} : {}),
      }),
      getConsultationListPage(queryParams({consultStatus: 0, isUrgent: 1, unfinishedOnly: 0, pageNum: 1, pageSize: 1})),
      getConsultationListPage(queryParams({consultStatus: 1, unfinishedOnly: 0, pageNum: 1, pageSize: 1})),
    ]);
    unfinishedCount.value = Number(unfinished.data || 0);
    urgentPendingCount.value = Number(urgent.data?.total || 0);
    finishedCount.value = Number(finished.data?.total || 0);
  } catch (error) {
    console.error('加载会诊统计失败:', error);
  }
};
const reloadAll = async () => {
  await Promise.all([loadList(), loadCounts()]);
};
const handleAdmissionChange = async () => {
  pagination.value.pageNum = 1;
  await reloadAll();
};
const handleSearch = async () => {
  pagination.value.pageNum = 1;
  await reloadAll();
};
const resetFilters = async () => {
  filters.value = {
    toDeptId: '',
    consultStatus: undefined,
    consultType: undefined,
    isUrgent: undefined,
    keyword: '',
    unfinishedOnly: false,
  };
  pagination.value.pageNum = 1;
  await reloadAll();
};
// ---------------- 申请会诊（表单/校验/提交在子组件，页面只管开关） ----------------
const applyVisible = ref(false);
const openApply = () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择住院患者（会诊必须挂在一次住院上）');
    return;
  }
  applyVisible.value = true;
};
// ---------------- 接诊 ----------------
const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm(`确认由你接诊会诊 ${row.consultationNo}（${row.toDeptName || '—'}）？接诊人将记为当前登录用户。`, '会诊接诊', {
      type: 'warning',
      confirmButtonText: '确认接诊',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await acceptConsultation({consultationId: row.consultationId});
    ElMessage.success('已接诊');
    await reloadAll();
  } catch (error) {
    ElMessage.error(error.message || '接诊失败');
  }
};
// ---------------- 完成（回写病历） ----------------
const finishVisible = ref(false);
const finishRow = ref(null);
const openFinish = (row) => {
  finishRow.value = row;
  finishVisible.value = true;
};
// ---------------- 取消 ----------------
const cancelVisible = ref(false);
const cancelRow = ref(null);
const openCancel = (row) => {
  cancelRow.value = row;
  cancelVisible.value = true;
};
// ---------------- 详情（按 id 子组件自加载） ----------------
const detailVisible = ref(false);
const detailId = ref('');
const openDetail = (row) => {
  detailId.value = row.consultationId;
  detailVisible.value = true;
};
// ---------------- 展示辅助 ----------------
const statusTagType = (s) => {
  if (s === 0)
    return 'warning';
  if (s === 3)
    return 'primary';
  if (s === 1)
    return 'success';
  if (s === 2)
    return 'info';
  return 'info';
};
const waitText = (row) => {
  if (row.acceptTime)
    return `已应答 ${row.responseMinutes ?? '—'} 分钟`;
  return `等待 ${row.responseMinutes ?? '—'} 分钟`;
};
const handleSizeChange = async (size) => {
  pagination.value.pageSize = size;
  pagination.value.pageNum = 1;
  await loadList();
};
const handlePageChange = async (page) => {
  pagination.value.pageNum = page;
  await loadList();
};
onMounted(async () => {
  await Promise.all([loadAdmissions(), loadDepts()]);
  await reloadAll();
});
</script>
