<template>
  <div>
    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-blue-50">
          <DataLine class="h-5 w-5 text-blue-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">今日标本</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.todayCount || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-amber-50">
          <Warning class="h-5 w-5 text-amber-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">待采样</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.pendingSample || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-purple-50">
          <Tickets class="h-5 w-5 text-purple-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">已采样</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.sampled || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-emerald-50">
          <CircleCheck class="h-5 w-5 text-emerald-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">检验中</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.testing || 0 }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-red-50">
          <Warning class="h-5 w-5 text-red-600"/>
        </div>
        <div>
          <p class="text-xs text-slate-500">异常标本</p>
          <p class="text-lg font-bold text-slate-900">{{ stats.abnormal || 0 }}</p>
        </div>
      </div>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="searchTerm" :prefix-icon="Search" class="!max-w-xs" clearable
                      placeholder="搜索条码/患者/项目..." @clear="loadData" @keyup.enter="loadData"/>
          </el-form-item>
          <el-form-item label="状态">
            <div class="flex gap-1">
              <el-button :type="statusFilter === 'all' ? 'primary' : ''" size="small"
                         @click="statusFilter = 'all'; loadData()">全部
              </el-button>
              <el-button :type="statusFilter === '1' ? 'primary' : ''" size="small"
                         @click="statusFilter = '1'; loadData()">已登记
              </el-button>
              <el-button :type="statusFilter === '2' ? 'primary' : ''" size="small"
                         @click="statusFilter = '2'; loadData()">已采样
              </el-button>
              <el-button :type="statusFilter === '3' ? 'primary' : ''" size="small"
                         @click="statusFilter = '3'; loadData()">已接收
              </el-button>
              <el-button :type="statusFilter === '4' ? 'primary' : ''" size="small"
                         @click="statusFilter = '4'; loadData()">检验中
              </el-button>
              <el-button :type="statusFilter === '5' ? 'primary' : ''" size="small"
                         @click="statusFilter = '5'; loadData()">已出结果
              </el-button>
            </div>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-900">{{ pagination.total }}</span> 条
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <!-- 表格 -->
      <el-table v-loading="loading" :data="filtered" :max-height="tableMaxHeight" class="cursor-pointer" stripe
                @row-click="openDetail">
        <el-table-column label="标本条码" prop="specimenNo" width="150">
          <template #default="{row}">
            <span v-if="row.specimenNo" class="font-mono text-blue-600">{{ row.specimenNo }}</span>
            <span v-else class="text-slate-400">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" min-width="120">
          <template #default="{row}">
            <div>
              <span class="font-medium text-slate-900">{{ row.patientName }}</span>
              <span class="ml-1 text-slate-400">{{ patientGenderText(row.gender) }} {{ row.age }}岁</span>
            </div>
            <div class="text-slate-400">{{ row.patientNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="检验项目" min-width="140" prop="laboratoryItemName"/>
        <el-table-column label="标本类型" prop="specimenType" width="80"/>
        <el-table-column label="开单医生" prop="applyDoctorName" width="90"/>
        <el-table-column label="流转节点" min-width="180">
          <template #default="{row}">
            <span class="text-slate-500">{{ traceNodes(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTagType(recordStatusMap[row.recordStatus] || '')" effect="plain" size="small">
              {{ recordStatusMap[row.recordStatus] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="160">
          <template #default="{row}">
            <template v-if="row.recordStatus === 1">
              <el-button v-if="!row.specimenNo" v-perm="'medtech:specimen:edit'" link size="small" type="primary"
                         @click.stop="handleAssignBarcode(row)">
                分配条码
              </el-button>
              <el-button v-if="row.specimenNo" v-perm="'medtech:specimen:edit'" link size="small" type="success"
                         @click.stop="handleSample(row)">
                采集确认
              </el-button>
            </template>
            <template v-if="row.recordStatus === 2">
              <el-button v-perm="'medtech:specimen:edit'" link size="small" type="warning"
                         @click.stop="handleReject(row)">
                退回
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="pagination.total > pagination.pageSize" ref="footerRef"
           class="list-footer flex items-center justify-end">
        <el-pagination
            :current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <SpecimenDetail v-model="showDetailDialog" :specimen="selectedSpecimen"/>

    <!-- 条码分配弹窗 -->
    <el-dialog v-model="showBarcodeDialog" destroy-on-close title="分配标本条码" width="400px">
      <div class="space-y-4">
        <div v-if="barcodeTarget" class="rounded-lg bg-slate-50 p-3 text-sm">
          <p>患者：<span class="font-medium">{{ barcodeTarget.patientName }}</span></p>
          <p>项目：<span class="font-medium">{{ barcodeTarget.laboratoryItemName }}</span></p>
          <p>类型：<span class="font-medium">{{ barcodeTarget.specimenType }}</span></p>
        </div>
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">条码号</label>
          <el-input v-model="barcodeInput" clearable placeholder="请输入或扫描条码号"/>
        </div>
      </div>
      <template #footer>
        <el-button @click="showBarcodeDialog = false">取消</el-button>
        <el-button v-perm="'medtech:specimen:edit'" type="primary" @click="confirmAssignBarcode">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {CircleCheck, DataLine, Search, Tickets, Warning} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {assignBarcode, getSpecimenList, getSpecimenStats, rejectSpecimen, sampleSpecimen} from '@/api/medicaltech';
import {patientGenderText} from '@/lib/patientGender';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import SpecimenDetail from './SpecimenDetail.vue';

const loading = ref(false);
const specimens = ref([]);
const searchTerm = ref('');
const statusFilter = ref('all');
const selectedSpecimen = ref(null);
const showDetailDialog = ref(false);
const showBarcodeDialog = ref(false);
const barcodeInput = ref('');
const barcodeTarget = ref(null);
const stats = ref({todayCount: 0, pendingSample: 0, sampled: 0, testing: 0, abnormal: 0});
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const specimenStatusMap = {
  0: '待分配',
  1: '已分配',
  2: '已采集',
  3: '已接收',
  99: '异常退回',
};
const recordStatusMap = {
  1: '已登记',
  2: '已采样',
  3: '已接收',
  4: '检验中',
  5: '已出结果',
  6: '已审核',
  7: '已发布',
};
const statusTagType = (status) => {
  if (status.includes('异常') || status.includes('退回'))
    return 'danger';
  if (status.includes('已发布') || status.includes('已审核') || status.includes('已出'))
    return 'success';
  if (status.includes('检验中') || status.includes('采样中'))
    return 'warning';
  return 'info';
};
const filtered = computed(() => {
  let list = specimens.value;
  if (searchTerm.value) {
    const kw = searchTerm.value.toLowerCase();
    list = list.filter(s => s.patientName?.toLowerCase().includes(kw) ||
        s.specimenNo?.toLowerCase().includes(kw) ||
        s.laboratoryItemName?.toLowerCase().includes(kw) ||
        s.patientNo?.toLowerCase().includes(kw));
  }
  if (statusFilter.value !== 'all') {
    const statusNum = Number(statusFilter.value);
    list = list.filter(s => s.recordStatus === statusNum);
  }
  return list;
});
const loadData = async () => {
  loading.value = true;
  try {
    const [listRes, statsRes] = await Promise.all([
      getSpecimenList({
        pageNum: pagination.value.pageNum,
        pageSize: pagination.value.pageSize,
        keyword: searchTerm.value || undefined,
        recordStatus: statusFilter.value !== 'all' ? Number(statusFilter.value) : undefined,
      }),
      getSpecimenStats(),
    ]);
    specimens.value = (listRes.data?.records || []).map((item) => ({
      ...item,
      specimenNo: item.specimenNo || '',
      specimenType: item.specimenType || '血液',
      specimenStatus: item.specimenStatus ?? 0,
      sampleTime: item.sampleTime || '',
      sampleBy: item.sampleBy || '',
      receiveTime: item.receiveTime || '',
      receiveBy: item.receiveBy || '',
      executeTime: item.executeTime || '',
      executeBy: item.executeBy || '',
      auditTime: item.auditTime || '',
      auditBy: item.auditBy || '',
      suggestions: item.suggestions || '',
    }));
    pagination.value.total = listRes.data?.total || 0;
    stats.value = statsRes.data || {};
  } catch (e) {
    ElMessage.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
};
const handlePageChange = (page) => {
  pagination.value.pageNum = page;
  loadData();
};
const openDetail = (row) => {
  selectedSpecimen.value = row;
  showDetailDialog.value = true;
};
const handleAssignBarcode = (row) => {
  barcodeTarget.value = row;
  barcodeInput.value = row.specimenNo || `SP${Date.now()}`;
  showBarcodeDialog.value = true;
};
const confirmAssignBarcode = async () => {
  if (!barcodeInput.value.trim()) {
    ElMessage.warning('请输入条码号');
    return;
  }
  try {
    await assignBarcode(barcodeTarget.value.id, barcodeInput.value.trim());
    ElMessage.success('条码分配成功');
    showBarcodeDialog.value = false;
    loadData();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
};
const handleSample = async (row) => {
  try {
    await ElMessageBox.confirm(`确认患者「${row.patientName}」的${row.specimenType}标本已采集？`, '采集确认', {
      confirmButtonText: '确认采集',
      cancelButtonText: '取消',
      type: 'info'
    });
    await sampleSpecimen(row.id, '护士');
    ElMessage.success('采集确认成功');
    loadData();
  } catch (e) {
    if (e !== 'cancel' && e?.message)
      ElMessage.error(e.message);
  }
};
const handleReject = async (row) => {
  try {
    const {value: reason} = await ElMessageBox.prompt(`请填写退回原因（患者：${row.patientName}，项目：${row.laboratoryItemName}）`, '标本退回', {
      confirmButtonText: '确认退回',
      cancelButtonText: '取消',
      type: 'warning',
      inputPlaceholder: '退回原因',
      inputValidator: (v) => v?.trim() ? true : '请填写退回原因',
    });
    await rejectSpecimen(row.id, reason);
    ElMessage.success('退回成功');
    loadData();
  } catch (e) {
    if (e !== 'cancel' && e?.message)
      ElMessage.error(e.message);
  }
};
const traceNodes = (row) => {
  const nodes = [];
  if (row.specimenNo)
    nodes.push('已分配条码');
  if (row.sampleTime)
    nodes.push(`采样 ${row.sampleBy || ''}`);
  if (row.receiveTime)
    nodes.push(`接收 ${row.receiveBy || ''}`);
  if (row.executeTime)
    nodes.push(`检验 ${row.executeBy || ''}`);
  if (row.auditTime)
    nodes.push(`审核 ${row.auditBy || ''}`);
  return nodes.length > 0 ? nodes.join(' → ') : '医嘱已开';
};
onMounted(() => loadData());
</script>
