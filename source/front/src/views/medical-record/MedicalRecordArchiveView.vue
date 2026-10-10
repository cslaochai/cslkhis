<template>
  <div>
    <!-- 页头 -->
    <div class="flex items-center justify-between mb-3">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">病案归档工作台</h2>
        <p class="mt-1 text-sm text-slate-500">病历归档（1→2）与封存（2→3）单向流转；封存为法律动作，不可解封</p>
      </div>
      <el-button :icon="Bell" @click="doNotifyOverdue">补跑超期提醒</el-button>
    </div>

    <!-- 三态计数：走后端数字，不用本页 list 数 -->
    <div class="grid grid-cols-2 gap-3 md:grid-cols-4 mb-3">
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="arc-count-pending">
        <div class="text-xs text-slate-500">待归档</div>
        <div class="mt-1 text-xl font-semibold text-amber-600">{{ countsLoaded ? counts.pending : '—' }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="arc-count-archived">
        <div class="text-xs text-slate-500">已归档</div>
        <div class="mt-1 text-xl font-semibold text-emerald-600">{{ countsLoaded ? counts.archived : '—' }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="arc-count-sealed">
        <div class="text-xs text-slate-500">已封存</div>
        <div class="mt-1 text-xl font-semibold text-slate-600">{{ countsLoaded ? counts.sealed : '—' }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="arc-count-total">
        <div class="text-xs text-slate-500">合计</div>
        <div class="mt-1 text-xl font-semibold text-[#1269B5]">{{ countsLoaded ? counts.total : total }}</div>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="患者姓名">
          <el-input
              v-model="query.patientName"
              :prefix-icon="Search"
              class="!w-56"
              clearable
              data-testid="arc-search"
              placeholder="患者姓名"
              @clear="onSearch"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="归档状态">
          <el-select v-model="query.archiveStatus" class="!w-40" clearable data-testid="arc-status-filter"
                     placeholder="归档状态" @change="onSearch">
            <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="arc-table" stripe>
        <el-table-column class-name="font-mono" label="归档单号" min-width="150" prop="archiveNo"/>
        <el-table-column label="患者" min-width="110">
          <template #default="{ row }">
            <div class="text-sm text-slate-800">{{ row.patientName || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.patientNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="就诊" min-width="150">
          <template #default="{ row }">
            <div class="text-xs text-slate-600">{{ row.visitDate || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.deptName || '' }}{{
                row.doctorName ? ' · ' + row.doctorName : ''
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="诊断" min-width="140" prop="diagnosis" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.archiveStatus)" effect="plain" size="small">
              {{ statusText(row.archiveStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归档/封存时间" min-width="160">
          <template #default="{ row }">
            <div v-if="row.archiveTime" class="text-xs text-slate-600">归档 {{ row.archiveTime }}</div>
            <div v-if="row.sealTime" class="text-xs text-slate-500">封存 {{ row.sealTime }}</div>
            <span v-if="!row.archiveTime && !row.sealTime" class="text-xs text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button :icon="Document" link size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
                v-if="canArchive(row)"
                v-perm="'emr:archive:edit'"
                :data-testid="`arc-archive-${row.id}`" :icon="Box" :loading="actionLoading" link
                size="small"
                type="warning"
                @click="doArchive(row)"
            >归档
            </el-button>
            <el-button
                v-if="canSeal(row)"
                v-perm="'emr:archive:edit'"
                :data-testid="`arc-seal-${row.id}`" :icon="Lock" :loading="actionLoading" link
                size="small"
                type="danger"
                @click="doSeal(row)"
            >封存
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-8 text-center text-xs text-slate-400">
            {{ query.patientName || query.archiveStatus !== null ? '没有符合条件的归档记录' : '暂无归档记录' }}
          </div>
        </template>
      </el-table>

      <!-- 分页：在流内紧跟表格底 -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="onSearch"
        />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" destroy-on-close title="归档详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="归档单号">{{ detail.archiveNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '—' }}（{{
            detail.patientNo || '—'
          }}）
        </el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ detail.visitDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ detail.doctorName || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="诊断">{{ detail.diagnosis || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.archiveStatus)" effect="plain" size="small">
            {{ statusText(detail.archiveStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="归档时间">{{ detail.archiveTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="封存时间">{{ detail.sealTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Bell, Box, Document, Lock, Refresh, Search} from '@element-plus/icons-vue';
import {
  archiveRecord,
  getArchiveDetail,
  getArchiveList,
  getArchiveStatusCount,
  notifyArchiveOverdue,
  sealRecord
} from '@/api/archive';
import {getDictDataMapList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  patientName: '',
  archiveStatus: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
/** 字典：归档状态（1-待归档 2-已归档 3-已封存） */
const statusOptions = ref([]);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList('his_archive_status');
    if (res.code === 200 && res.data) {
      statusOptions.value = (res.data['his_archive_status'] || []).map((d) => ({
        label: d.dictLabel,
        value: Number(d.dictValue),
      }));
    }
  } catch (e) {
    // 拿不到字典 → 码值渲染「未知(n)」，不回落成合法值；只记录不阻塞
    console.error('加载归档状态字典失败', e);
  }
};
/** 状态文案：命中字典用字典，命中不了暴露「未知(n)」 */
const statusText = (v) => {
  if (v === null || v === undefined)
    return '—';
  const hit = statusOptions.value.find(o => o.value === Number(v));
  return hit ? hit.label : `未知(${v})`;
};
const statusTagType = (v) => {
  if (v === 1)
    return 'warning';
  if (v === 2)
    return 'success';
  if (v === 3)
    return 'info';
  return 'info';
};
/** 待归档/已归档/已封存 计数：必须走后端聚合，不能拿本页 list 去数（翻页就变） */
const counts = ref({pending: 0, archived: 0, sealed: 0, total: 0});
const countsLoaded = ref(false);
const loadCounts = async () => {
  try {
    const res = await getArchiveStatusCount();
    if (res.code === 200 && res.data) {
      counts.value = {
        pending: Number(res.data.pending ?? 0),
        archived: Number(res.data.archived ?? 0),
        sealed: Number(res.data.sealed ?? 0),
        total: Number(res.data.total ?? 0),
      };
      countsLoaded.value = true;
    }
  } catch (e) {
    // 计数拿不到就显示「—」，绝不回落成 0 —— 0 是"确实没有"，加载失败不是
    console.error('加载归档计数失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getArchiveList({
      patientName: query.patientName || undefined,
      archiveStatus: query.archiveStatus ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    }
  } catch (e) {
    ElMessage.error('加载归档记录失败');
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const reloadAll = async () => {
  await Promise.all([loadList(), loadCounts()]);
};
const onSearch = () => {
  query.pageNum = 1;
  loadList();
};
const onReset = () => {
  query.patientName = '';
  query.archiveStatus = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 详情 ----------------
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getArchiveDetail(row.id);
    if (res.code === 200 && res.data) {
      detail.value = res.data;
      detailVisible.value = true;
    } else {
      ElMessage.error(res.message || '获取归档详情失败');
    }
  } catch (e) {
    ElMessage.error('获取归档详情失败');
    console.error(e);
  }
};
// ---------------- 归档 / 封存 ----------------
const actionLoading = ref(false);
const doArchive = async (row) => {
  await ElMessageBox.confirm(`确认将「${row.patientName || '—'}」的病历（${row.recordNo || row.archiveNo || '—'}）归档？归档后病历不可再修改。`, '归档确认', {
    type: 'warning',
    confirmButtonText: '确认归档',
    cancelButtonText: '取消'
  }).catch(() => 'cancel').then(async (r) => {
    if (r === 'cancel')
      return;
    actionLoading.value = true;
    try {
      const res = await archiveRecord(row.id);
      if (res.code === 200) {
        ElMessage.success('归档成功');
        await reloadAll();
      } else
        ElMessage.error(res.message || '归档失败');
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '归档失败');
    } finally {
      actionLoading.value = false;
    }
  });
};
const doSeal = async (row) => {
  let reason = '';
  try {
    const {value} = await ElMessageBox.prompt(`封存「${row.patientName || '—'}」的病历（${row.recordNo || row.archiveNo || '—'}）。封存是法律动作，封存后不可解封 —— 请填写封存事由。`, '病历封存', {
      type: 'warning',
      confirmButtonText: '确认封存',
      cancelButtonText: '取消',
      inputPlaceholder: '如：医疗纠纷诉讼，应法院要求封存',
      inputValidator: (v) => (v && v.trim().length >= 2) || '封存事由必填（至少 2 个字）',
    });
    reason = value;
  } catch {
    return;
  }
  actionLoading.value = true;
  try {
    const res = await sealRecord(row.id);
    if (res.code === 200) {
      ElMessage.success('封存成功');
      await reloadAll();
    } else
      ElMessage.error(res.message || '封存失败');
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '封存失败');
  } finally {
    actionLoading.value = false;
  }
};
/** 补跑超期提醒：把"门诊病历超 3 天未归档"的提醒重新生成（日常由定时任务 08:00 跑） */
const doNotifyOverdue = async () => {
  try {
    const res = await notifyArchiveOverdue();
    if (res.code === 200)
      ElMessage.success(`已补跑超期提醒，生成 ${res.data ?? 0} 条`);
    else
      ElMessage.error(res.message || '补跑失败');
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '补跑失败');
  }
};
const canArchive = (row) => Number(row.archiveStatus) === 1;
const canSeal = (row) => Number(row.archiveStatus) === 2;
onMounted(async () => {
  await loadDicts();
  await reloadAll();
});
</script>
