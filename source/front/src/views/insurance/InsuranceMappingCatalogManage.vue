<template>
  <!-- 目录管理弹窗（从 InsuranceMappingView 拆出：列表 + 启停 + 人工补录） -->
  <el-dialog v-model="visible" data-testid="yb-catalog-dialog" title="医保目录管理（只启停不删除，保证已对照追溯链）"
             width="960px">
    <div class="flex flex-wrap items-center gap-2 mb-3">
      <el-select v-model="catalogQuery.catalogType" clearable placeholder="全部类型" style="width: 160px"
                 @change="loadCatalogs">
        <el-option v-for="(label, t) in catalogTypeMap" :key="t" :label="label" :value="t"/>
      </el-select>
      <el-input v-model="catalogQuery.keyword" clearable placeholder="编码/名称" style="width: 200px"
                @keyup.enter="loadCatalogs"/>
      <el-button :icon="Search" size="default" @click="loadCatalogs">查询</el-button>
      <div class="flex-1"></div>
      <el-button plain type="primary"
                 @click="catalogForm = {catalogType: 1, ybCode: '', ybName: '', spec: '', unit: '', dosageForm: '', insuranceLevel: undefined, payRatio: undefined, status: 1}">
        人工补录
      </el-button>
    </div>

    <el-form v-if="catalogForm" :model="catalogForm" class="mb-3 p-3 rounded bg-slate-50" data-testid="yb-catalog-form"
             inline>
      <el-form-item label="类型">
        <el-select v-model="catalogForm.catalogType" style="width: 140px">
          <el-option v-for="(label, t) in catalogTypeMap" :key="t" :label="label" :value="t"/>
        </el-select>
      </el-form-item>
      <el-form-item label="医保编码">
        <el-input v-model="catalogForm.ybCode" data-testid="yb-catalog-code" style="width: 220px"/>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="catalogForm.ybName" data-testid="yb-catalog-name" style="width: 180px"/>
      </el-form-item>
      <el-form-item label="甲乙类">
        <el-select v-model="catalogForm.insuranceLevel" clearable style="width: 90px">
          <el-option :value="1" label="甲类"/>
          <el-option :value="2" label="乙类"/>
          <el-option :value="3" label="丙类"/>
        </el-select>
      </el-form-item>
      <el-form-item label="支付比例%">
        <el-input-number v-model="catalogForm.payRatio" :max="100" :min="0" :precision="2" style="width: 110px"/>
      </el-form-item>
      <el-form-item>
        <el-button data-testid="yb-catalog-save" type="primary" @click="saveCatalog">保存</el-button>
        <el-button @click="catalogForm = null">取消</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="catalogsLoading" :data="catalogs" data-testid="yb-catalog-table" height="380" size="small">
      <el-table-column class-name="font-mono text-xs" label="医保编码" prop="ybCode" show-overflow-tooltip
                       width="230"/>
      <el-table-column label="目录名称" min-width="160" prop="ybName" show-overflow-tooltip/>
      <el-table-column label="类型" width="110">
        <template #default="{row}">{{ catalogTypeMap[row.catalogType] || `未知(${row.catalogType})` }}</template>
      </el-table-column>
      <el-table-column label="规格" prop="spec" show-overflow-tooltip width="110"/>
      <el-table-column label="甲乙类" width="75">
        <template #default="{row}">
          {{ row.insuranceLevel ? (levelMap[row.insuranceLevel] || `未知(${row.insuranceLevel})`) : '—' }}
        </template>
      </el-table-column>
      <el-table-column align="right" label="支付比例" width="85">
        <template #default="{row}">{{ row.payRatio != null ? row.payRatio + '%' : '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{
              row.status === 1 ? '启用' : '停用'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{row}">
          <el-button v-perm="'finance:insuranceMapping:edit'" :data-testid="`yb-catalog-toggle-${row.ybCode}`" :type="row.status === 1 ? 'warning' : 'success'"
                     plain
                     size="small" @click="toggleCatalog(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
/**
 * 医保目录管理弹窗（从 InsuranceMappingView 拆出）：弹窗内自取目录列表（原页内 loadCatalogs 随迁）。
 * refreshToken：CSV 导入目录后由父页 +1，弹窗开着就重拉一次（替代原先跨弹窗直接调 loadCatalogs）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Search} from '@element-plus/icons-vue';
import {changeYbCatalogStatus, getYbCatalogList, upsertYbCatalog} from '@/api/insuranceMapping';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 目录类型枚举：主表列与弹窗两处都要用，口径仍由页面单点定义后传入 */
  levelMap: {type: Object, required: true},
  /** 父页导入目录后自增，弹窗打开时据此刷新列表 */
  refreshToken: {type: Number, default: 0},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const catalogTypeMap = {1: '西药/中成药', 2: '中药饮片', 3: '医疗服务项目', 4: '医用耗材'};

const catalogs = ref([]);
const catalogsLoading = ref(false);
const catalogQuery = ref({catalogType: undefined, keyword: ''});
const catalogForm = ref(null);
const loadCatalogs = async () => {
  catalogsLoading.value = true;
  try {
    const res = await getYbCatalogList({...catalogQuery.value, pageNum: 1, pageSize: 100});
    catalogs.value = res.data?.records || [];
  } finally {
    catalogsLoading.value = false;
  }
};
const toggleCatalog = async (row) => {
  const target = row.status === 1 ? 0 : 1;
  await changeYbCatalogStatus(row.id, target);
  ElMessage.success(target === 1 ? '已启用' : '已停用（停用后不可新对照）');
  await loadCatalogs();
};
const saveCatalog = async () => {
  const f = catalogForm.value;
  if (!f || !f.catalogType || !f.ybCode || !f.ybName) {
    ElMessage.warning('目录类型、编码、名称为必填');
    return;
  }
  await upsertYbCatalog({...f, id: f.id || undefined, status: f.status ?? 1});
  ElMessage.success(f.id ? '目录已更新' : '目录已补录');
  catalogForm.value = null;
  await loadCatalogs();
};

watch(visible, (open) => {
  if (open)
    loadCatalogs();
});
watch(() => props.refreshToken, () => {
  if (visible.value)
    loadCatalogs();
});
</script>
