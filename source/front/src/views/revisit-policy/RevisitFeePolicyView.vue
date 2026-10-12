<template>
  <div>
    <div class="mb-3 flex items-start justify-between">
      <el-button v-perm="'opd:revisitPolicy:add'" :icon="Plus" type="primary" @click="openCreate">新增策略</el-button>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="策略名称">
            <el-input
                v-model="query.policyName"
                :prefix-icon="Search"
                class="!w-56"
                clearable
                placeholder="策略名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="复诊来源">
            <el-select v-model="query.revisitSource" class="!w-44" clearable placeholder="复诊来源">
              <el-option v-for="o in listSourceOptions" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="收费方式">
            <el-select v-model="query.chargeMode" class="!w-40" clearable placeholder="收费方式">
              <el-option v-for="o in chargeModeOptions" :key="o.dictValue" :label="o.dictLabel"
                         :value="Number(o.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" class="!w-28" clearable placeholder="状态">
              <el-option v-for="o in STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 条策略
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="策略名称" min-width="170" prop="policyName"/>
        <el-table-column label="适用来源" min-width="150">
          <template #default="{ row }">
            {{ row.revisitSource === 0 ? SOURCE_ANY.dictLabel : dictLabelText(sourceOptions, row.revisitSource) }}
          </template>
        </el-table-column>
        <el-table-column label="与原就诊医生" width="120">
          <template #default="{ row }">{{ revisitMatchLabel(row.sameDoctor) }}</template>
        </el-table-column>
        <el-table-column label="与原就诊科室" width="120">
          <template #default="{ row }">{{ revisitMatchLabel(row.sameDept) }}</template>
        </el-table-column>
        <el-table-column align="right" label="间隔天数" width="110">
          <template #default="{ row }">
            {{ row.withinDays === null || row.withinDays === undefined ? '不限' : `≤ ${row.withinDays} 天` }}
          </template>
        </el-table-column>
        <el-table-column label="收费方式" min-width="130">
          <template #default="{ row }">
            <span :class="row.chargeMode === 1 ? 'text-slate-700' : 'font-semibold text-[#0E9488]'">
              {{ dictLabelText(chargeModeOptions, row.chargeMode) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="优先级" prop="priority" width="90"/>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag
                :class="row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-500'"
                class="border"
                effect="plain"
                size="small"
            >
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180" prop="remark" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-perm="'opd:revisitPolicy:add'" :icon="Edit" link size="small" type="primary"
                       @click="openEdit(row)">编辑
            </el-button>
            <el-button v-perm="'opd:revisitPolicy:delete'" :icon="Delete" link size="small" type="danger"
                       @click="handleDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的复诊收费策略
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

    <!-- 新增/编辑：表单与提交逻辑拆到 RevisitFeePolicyEdit，页面只管开关与递参 -->
    <RevisitFeePolicyEdit v-model="formVisible" :row="editRow" :source-options="listSourceOptions"
                          :charge-mode-options="chargeModeOptions" :status-options="STATUS_OPTIONS" @saved="loadList"/>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deleteRevisitFeePolicy, getRevisitFeePolicyList,} from '@/api/revisitPolicy';
import {getDictDataMapList} from '@/api/system';
import RevisitFeePolicyEdit from './RevisitFeePolicyEdit.vue';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {revisitMatchLabel} from '@/lib/revisitPolicy';

const loading = ref(true);
const rows = ref([]);
const query = reactive({
  policyName: '',
  revisitSource: null,
  chargeMode: null,
  status: null,
});
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
// 来源与收费方式都是库里字典（sql/121）：文案漂移会让前台与收费处对不上，故不写死
const sourceOptions = ref([]);
const chargeModeOptions = ref([]);
// 策略表允许 revisit_source=0=「任意复诊来源」，那是配置的表达能力而不是业务码值，
// 库里字典没有它，由本页面补一个选项（判定语义在后端 RevisitFeePolicyServiceImpl）
const SOURCE_ANY = {dictValue: '0', dictLabel: '不限（任意复诊来源）'};
const listSourceOptions = computed(() => [SOURCE_ANY, ...sourceOptions.value]);
const STATUS_OPTIONS = [
  {label: '启用', value: 1},
  {label: '停用', value: 0},
];
// 新增/编辑：表单与提交逻辑已拆至 RevisitFeePolicyEdit.vue，页面只管开关与递参
const formVisible = ref(false);
const editRow = ref(null);

async function loadDicts() {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.REVISIT_SOURCE},${DICT_TYPE.REVISIT_CHARGE_MODE}`);
    if (res.code === 200 && res.data) {
      sourceOptions.value = res.data[DICT_TYPE.REVISIT_SOURCE] || [];
      chargeModeOptions.value = res.data[DICT_TYPE.REVISIT_CHARGE_MODE] || [];
    }
  } catch (e) {
    console.error('加载复诊字典失败', e);
  }
}

async function loadList() {
  loading.value = true;
  try {
    const res = await getRevisitFeePolicyList({
      policyName: query.policyName || undefined,
      revisitSource: query.revisitSource === null ? undefined : query.revisitSource,
      chargeMode: query.chargeMode === null ? undefined : query.chargeMode,
      status: query.status === null ? undefined : query.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    const data = res.data || {};
    rows.value = data.records || [];
    pagination.total = Number(data.total || 0);
  } catch (e) {
    rows.value = [];
    pagination.total = 0;
    ElMessage.error(e?.message || '加载复诊收费策略失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.pageNum = 1;
  loadList();
}

function handleReset() {
  query.policyName = '';
  query.revisitSource = null;
  query.chargeMode = null;
  query.status = null;
  handleSearch();
}

function handleSizeChange(size) {
  pagination.pageSize = size;
  pagination.pageNum = 1;
  loadList();
}

function handleCurrentChange(page) {
  pagination.pageNum = page;
  loadList();
}

function openCreate() {
  editRow.value = null;
  formVisible.value = true;
}

function openEdit(row) {
  editRow.value = row;
  formVisible.value = true;
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除策略「${row.policyName}」？删除后原本命中它的复诊号按「全额收费」处理（无策略即不免钱）。`, '删除复诊收费策略', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await deleteRevisitFeePolicy(row.id);
    ElMessage.success('删除成功');
    if (rows.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1;
    }
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '删除失败');
  }
}

onMounted(async () => {
  await loadDicts();
  await loadList();
});
</script>
