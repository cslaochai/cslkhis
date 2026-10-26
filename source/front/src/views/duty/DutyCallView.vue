<template>
  <div data-testid="duty-call-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="值班日期">
            <el-date-picker
                v-model="searchForm.beginDate"
                clearable
                placeholder="开始日期"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="至">
            <el-date-picker
                v-model="searchForm.endDate"
                clearable
                placeholder="结束日期"
                type="date"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="层级">
            <el-select v-model="searchForm.targetLevel" clearable placeholder="全部" style="width: 130px">
              <el-option :label="'二线'" :value="2"/>
              <el-option :label="'三线'" :value="3"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 140px">
              <el-option :label="'待到岗'" :value="0"/>
              <el-option :label="'已到岗'" :value="1"/>
              <el-option :label="'已超时升级'" :value="2"/>
              <el-option :label="'已取消'" :value="3"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="mineOnly" data-testid="duty-call-mine">只看待我到岗</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:dutyCall:call'" :icon="Plus" data-testid="duty-call-add" type="primary" @click="handleAdd">
            发起呼叫
          </el-button>
          <el-button v-perm="'org:dutyCall:call'" :icon="Refresh" data-testid="duty-call-escalate" @click="handleEscalate">
            补跑超时升级
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="duty-call-table">
        <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
          <el-table-column label="呼叫单号" prop="callNo" min-width="160"/>
          <el-table-column label="值班" width="150">
            <template #default="{ row }">{{ row.dutyDate }} {{ row.shiftTypeText }}</template>
          </el-table-column>
          <el-table-column label="呼叫科室" prop="deptName" min-width="130" show-overflow-tooltip/>
          <el-table-column label="层级" width="80">
            <template #default="{ row }">
              <el-tag :type="row.targetLevel === 3 ? 'warning' : 'primary'" size="small">{{ row.targetLevelText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="被叫值班人" prop="targetEmpName" width="120"/>
          <el-table-column label="联系电话" prop="targetPhone" width="130"/>
          <el-table-column label="患者" min-width="150">
            <template #default="{ row }">
              <span>{{ row.patientName || '—' }}</span>
              <span v-if="row.patientLocation" class="ml-1 text-xs text-gray-500">{{ row.patientLocation }}</span>
            </template>
          </el-table-column>
          <el-table-column label="呼叫事由" prop="callReason" min-width="220" show-overflow-tooltip/>
          <el-table-column label="呼叫时间" prop="callTime" width="160"/>
          <el-table-column label="到岗时间" prop="arriveTime" width="160">
            <template #default="{ row }">{{ row.arriveTime || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small" :data-testid="`duty-call-status-${row.id}`">
                {{ row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="剩余时限" width="110">
            <template #default="{ row }">
              <span v-if="row.remainSeconds === null || row.remainSeconds === undefined" class="text-gray-400">—</span>
              <span v-else-if="row.remainSeconds < 0" class="text-red-600">已超时</span>
              <span v-else class="text-gray-700">{{ Math.ceil(row.remainSeconds / 60) }} 分钟</span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="190">
            <template #default="{ row }">
              <el-button
                  v-if="row.canArrive"
                  v-perm="'org:dutyCall:arrive'"
                  :icon="Check"
                  data-testid="duty-call-arrive-btn"
                  link
                  type="primary"
                  @click.stop="handleArrive(row)"
              >
                到岗应答
              </el-button>
              <el-button
                  v-if="row.canCancel"
                  v-perm="'org:dutyCall:cancel'"
                  :icon="Close"
                  data-testid="duty-call-cancel-btn"
                  link
                  type="info"
                  @click.stop="handleCancel(row)"
              >
                取消
              </el-button>
              <el-button :icon="View" link type="primary" @click.stop="handleDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="tableData.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
          暂无听班呼叫记录
        </div>
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
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

    <DutyCallCreate v-model="createVisible" @saved="handleSaved"/>
    <DutyCallDetail v-model="detailVisible" :id="currentId"/>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Check, Close, Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {dutyCallArrive, dutyCallCancel, dutyCallEscalateOverdue, getDutyCallListPage} from '@/api/dutyCall';
import DutyCallCreate from './DutyCallCreate.vue';
import DutyCallDetail from './DutyCallDetail.vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const loading = ref(false);
const tableData = ref([]);
const mineOnly = ref(false);
const createVisible = ref(false);
const detailVisible = ref(false);
const currentId = ref(null);

const searchForm = ref({
  beginDate: '',
  endDate: '',
  targetLevel: null,
  status: null,
});

const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

const statusTagType = (status) => {
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  if (status === 3) return 'info';
  return 'warning';
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getDutyCallListPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      beginDate: searchForm.value.beginDate || undefined,
      endDate: searchForm.value.endDate || undefined,
      targetLevel: searchForm.value.targetLevel ?? undefined,
      status: searchForm.value.status ?? undefined,
      mineOnly: mineOnly.value || undefined,
    });
    const data = res?.data || {};
    tableData.value = data.records || [];
    pagination.value.total = Number(data.total || 0);
  } catch (e) /* 错误已由全局拦截器统一提示 */{
    tableData.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};

const handleReset = () => {
  searchForm.value = {beginDate: '', endDate: '', targetLevel: null, status: null};
  mineOnly.value = false;
  pagination.value.pageNum = 1;
  loadData();
};

const handleSizeChange = () => {
  pagination.value.pageNum = 1;
  loadData();
};

const handleCurrentChange = () => loadData();

const handleAdd = () => {
  createVisible.value = true;
};

const handleDetail = (row) => {
  currentId.value = row.id;
  detailVisible.value = true;
};

const handleArrive = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确认已到岗？呼叫单 ${row.callNo} 将置为「已到岗」，站内信待办同时闭环。`,
        '到岗应答',
        {type: 'warning'},
    );
  } catch {
    return;
  }
  try {
    await dutyCallArrive({id: row.id});
    ElMessage.success('已登记到岗');
    loadData();
  } catch (e) /* 错误已由全局拦截器统一提示 */{
  }
};

const handleCancel = async (row) => {
  let reason = '';
  try {
    const {value} = await ElMessageBox.prompt('请填写取消事由（必填）', '取消呼叫', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputValidator: (v) => (v && v.trim() ? true : '取消事由不能为空'),
    });
    reason = value.trim();
  } catch {
    return;
  }
  try {
    await dutyCallCancel({id: row.id, cancelReason: reason});
    ElMessage.success('呼叫已取消');
    loadData();
  } catch (e) /* 错误已由全局拦截器统一提示 */{
  }
};

const handleEscalate = async () => {
  try {
    await ElMessageBox.confirm(
        '将扫描到岗时限内未应答的呼叫单并升级到上一层级。已升级过的不会重复升级。',
        '补跑超时升级',
        {type: 'warning'},
    );
  } catch {
    return;
  }
  try {
    const res = await dutyCallEscalateOverdue();
    ElMessage.success(res?.message || '补跑完成');
    loadData();
  } catch (e) /* 错误已由全局拦截器统一提示 */{
  }
};

const handleSaved = () => {
  pagination.value.pageNum = 1;
  loadData();
};

onMounted(loadData);
</script>
