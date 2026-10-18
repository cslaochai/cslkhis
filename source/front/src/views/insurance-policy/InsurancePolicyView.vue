<template>
  <div>
    <div class="mb-3 flex items-start justify-between">
      <el-button v-perm="'finance:insurancePolicy:add'" :icon="Plus" type="primary" @click="openCreate">新增政策
      </el-button>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="政策名称">
          <el-input
              v-model="query.policyName"
              :prefix-icon="Search"
              class="!w-56"
              clearable
              placeholder="政策名称"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="医保类型">
          <el-input
              v-model="query.insuranceType"
              class="!w-52"
              clearable
              placeholder="医保类型（如：在职职工）"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="结算方式">
          <el-select v-model="query.settlementType" class="!w-40" clearable placeholder="结算方式">
            <el-option
                v-for="o in settlementTypeOptions"
                :key="o.dictValue"
                :label="o.dictLabel"
                :value="Number(o.dictValue)"
            />
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
        <el-form-item>
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 条政策
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column label="政策名称" min-width="180" prop="policyName"/>
        <el-table-column label="医保类型" min-width="110" prop="insuranceType"/>
        <el-table-column label="结算方式" min-width="130">
          <template #default="{ row }">{{ dictLabelText(settlementTypeOptions, row.settlementType) }}</template>
        </el-table-column>
        <el-table-column align="right" label="统筹比例" width="110">
          <template #default="{ row }">
            <span class="font-semibold text-[#1269B5]">{{ Number(row.coverageRatio ?? 0).toFixed(2) }}%</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="乙类自付" width="110">
          <template #default="{ row }">{{ Number(row.selfPayRatio ?? 0).toFixed(2) }}%</template>
        </el-table-column>
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
        <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button :icon="Edit" link size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="'finance:insurancePolicy:delete'" :icon="Delete" link size="small" type="danger"
                       @click="handleDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的医保政策
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

    <!-- 新增/编辑医保政策（拆到 InsurancePolicyEdit） -->
    <InsurancePolicyEdit v-model="formVisible" :row="editRow" :settlement-type-options="settlementTypeOptions"
                         @saved="loadList"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deleteInsurancePolicy, getInsurancePolicyList,} from '@/api/insurancePolicy';
import {getDictDataMapList} from '@/api/system';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import InsurancePolicyEdit from './InsurancePolicyEdit.vue';
// 「加载中 ≠ 没有」：初值 true
const loading = ref(true);
const rows = ref([]);
const query = reactive({
  policyName: '',
  insuranceType: '',
  settlementType: null,
  status: null,
});
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
// 结算方式口径：字典 his_medical_insurance_type_code（2-城镇职工医保 3-城乡居民医保 4-公费医疗）
// 注意：biz_yb_policy 列注释只写了「2-城镇职工医保 3-城乡居民医保」，
// 但真实数据里存在 4-公费医疗（政策 id=6，统筹 100%）。以字典 + 数据为准，不按残缺注释渲染。
const settlementTypeOptions = ref([]);
const STATUS_OPTIONS = [
  {label: '启用', value: 1},
  {label: '停用', value: 0},
];
const formVisible = ref(false);
// 传给编辑弹窗的行：null＝新增，有值＝编辑；表单复位与提交逻辑在 InsurancePolicyEdit 内
const editRow = ref(null);

async function loadDicts() {
  try {
    const res = await getDictDataMapList('his_medical_insurance_type_code');
    if (res.code === 200 && res.data) {
      settlementTypeOptions.value = (res.data['his_medical_insurance_type_code'] || [])
          .filter((o) => String(o.dictValue) !== '0');
    }
  } catch (e) {
    console.error('加载医保类型字典失败', e);
  }
}

async function loadList() {
  loading.value = true;
  try {
    const res = await getInsurancePolicyList({
      policyName: query.policyName || undefined,
      insuranceType: query.insuranceType || undefined,
      settlementType: query.settlementType === null ? undefined : query.settlementType,
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
  query.insuranceType = '';
  query.settlementType = null;
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

// 新增/编辑弹窗（InsurancePolicyEdit）：row 为空＝新增，有值＝编辑，复位与提交都在弹窗内
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
    await ElMessageBox.confirm(`确认删除政策「${row.policyName}」？删除后该医保类型将按自费处理（不报销）。`, '删除医保政策', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await deleteInsurancePolicy(row.id);
    ElMessage.success('删除成功');
    // 删掉的是本页最后一条时，回退一页，避免停在空页
    if (rows.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1;
    }
    loadList();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

onMounted(async () => {
  await loadDicts();
  await loadList();
});
</script>
