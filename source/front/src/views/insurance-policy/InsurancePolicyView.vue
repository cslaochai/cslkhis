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

    <el-dialog v-model="formVisible" :title="form.id ? '编辑医保政策' : '新增医保政策'" destroy-on-close width="520px">
      <el-form label-width="100px">
        <el-form-item label="政策名称" required>
          <el-input v-model="form.policyName" placeholder="如：城镇职工医保-在职"/>
        </el-form-item>
        <el-form-item label="医保类型" required>
          <el-input v-model="form.insuranceType" placeholder="如：在职职工 / 退休职工 / 老年居民"/>
        </el-form-item>
        <el-form-item label="结算方式" required>
          <el-select v-model="form.settlementType" class="w-full" placeholder="请选择">
            <el-option
                v-for="o in settlementTypeOptions"
                :key="o.dictValue"
                :label="o.dictLabel"
                :value="Number(o.dictValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="统筹比例" required>
          <el-input-number v-model="form.coverageRatio" :max="100" :min="0" :precision="2" :step="1" class="!w-full"/>
          <span class="ml-2 text-xs text-slate-400">%，如 85 表示报销 85%</span>
        </el-form-item>
        <el-form-item label="乙类自付">
          <el-input-number v-model="form.selfPayRatio" :max="100" :min="0" :precision="2" :step="1" class="!w-full"/>
          <span class="ml-2 text-xs text-slate-400">%，乙类药品个人先自付比例</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button v-perm="'finance:insurancePolicy:add'" :loading="formSubmitting" type="primary" @click="submitForm">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deleteInsurancePolicy, getInsurancePolicyList, insurancePolicyUpsert,} from '@/api/insurancePolicy';
import {getDictDataMapList} from '@/api/system';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
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
// 注意：sys_insurance_policy 列注释只写了「2-城镇职工医保 3-城乡居民医保」，
// 但真实数据里存在 4-公费医疗（政策 id=6，统筹 100%）。以字典 + 数据为准，不按残缺注释渲染。
const settlementTypeOptions = ref([]);
const STATUS_OPTIONS = [
  {label: '启用', value: 1},
  {label: '停用', value: 0},
];
const formVisible = ref(false);
const formSubmitting = ref(false);
const form = reactive({
  id: null,
  policyName: '',
  insuranceType: '',
  settlementType: null,
  coverageRatio: 0,
  selfPayRatio: 0,
  status: 1,
  remark: '',
});

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
    ElMessage.error(e?.message || '加载医保政策失败');
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

function openCreate() {
  form.id = null;
  form.policyName = '';
  form.insuranceType = '';
  form.settlementType = null;
  form.coverageRatio = 0;
  form.selfPayRatio = 0;
  form.status = 1;
  form.remark = '';
  formVisible.value = true;
}

function openEdit(row) {
  form.id = row.id;
  form.policyName = row.policyName;
  form.insuranceType = row.insuranceType;
  form.settlementType = row.settlementType;
  form.coverageRatio = Number(row.coverageRatio ?? 0);
  form.selfPayRatio = Number(row.selfPayRatio ?? 0);
  form.status = row.status;
  form.remark = row.remark || '';
  formVisible.value = true;
}

async function submitForm() {
  if (!form.policyName.trim()) {
    ElMessage.warning('请填写政策名称');
    return;
  }
  if (!form.insuranceType.trim()) {
    ElMessage.warning('请填写医保类型');
    return;
  }
  if (form.settlementType === null) {
    ElMessage.warning('请选择结算方式');
    return;
  }
  const coverage = Number(form.coverageRatio);
  if (Number.isNaN(coverage) || coverage < 0 || coverage > 100) {
    ElMessage.warning('统筹比例需在 0 ~ 100 之间');
    return;
  }
  formSubmitting.value = true;
  try {
    await insurancePolicyUpsert({
      id: form.id,
      policyName: form.policyName.trim(),
      insuranceType: form.insuranceType.trim(),
      settlementType: form.settlementType,
      coverageRatio: coverage,
      selfPayRatio: Number(form.selfPayRatio),
      status: form.status,
      remark: form.remark,
    });
    ElMessage.success(form.id ? '修改成功' : '新增成功');
    formVisible.value = false;
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    formSubmitting.value = false;
  }
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
  } catch (e) {
    ElMessage.error(e?.message || '删除失败');
  }
}

onMounted(async () => {
  await loadDicts();
  await loadList();
});
</script>
