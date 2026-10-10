<template>
  <div data-testid="yb-mapping-page">
    <!-- 对照率统计 -->
    <el-row :gutter="12" class="mb-3">
      <el-col v-for="s in stats" :key="s.itemType" :span="6">
        <el-card :data-testid="`yb-stats-${s.itemType}`" class="!rounded-lg" shadow="never">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-slate-500">{{ itemTypeMap[s.itemType] }}对照率</div>
              <div class="text-2xl font-semibold mt-1">{{ statsRate(s.total, s.mapped) }}<span
                  class="text-sm font-normal">%</span></div>
              <div class="text-xs text-slate-400 mt-0.5">已对照 {{ s.mapped }} / {{ s.total }}</div>
            </div>
            <el-progress :percentage="statsRate(s.total, s.mapped)" :status="statsRate(s.total, s.mapped) >= 100 ? 'success' : undefined" :width="64"
                         type="circle"/>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="项目类型">
            <el-radio-group :model-value="query.itemType" data-testid="yb-type-switch" @update:model-value="switchType">
              <el-radio-button v-for="(label, t) in itemTypeMap" :key="t" :value="t">{{ label }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable data-testid="yb-keyword" placeholder="院内/医保编码、名称"
                      style="width: 220px" @keyup.enter="pagination.pageNum = 1; loadRows()"/>
          </el-form-item>
          <el-form-item label="对照状态">
            <el-select v-model="query.mapStatus" clearable data-testid="yb-map-status" placeholder="对照状态"
                       style="width: 120px">
              <el-option :value="0" label="未对照"/>
              <el-option :value="1" label="已对照"/>
            </el-select>
          </el-form-item>
          <el-form-item label="项目状态">
            <el-select v-model="query.itemStatus" clearable placeholder="项目状态" style="width: 120px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="yb-search-btn" type="primary"
                       @click="pagination.pageNum = 1; loadRows()">查询
            </el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'finance:insuranceMapping:edit'" :icon="Connection" :loading="autoMatching"
                     data-testid="yb-auto-match-btn" @click="doAutoMatch">自动对照
          </el-button>
          <el-button v-perm="'finance:insuranceMapping:add'" :icon="Upload" data-testid="yb-import-btn"
                     @click="openImportDialog">导入目录
          </el-button>
          <el-button v-perm="'finance:insuranceMapping:add'" :icon="Setting" data-testid="yb-catalog-manage-btn"
                     @click="openCatalogDialog">目录管理
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡：主表 院内项目 ↔ 医保编码 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="yb-mapping-table" stripe
                style="width: 100%">
        <el-table-column class-name="font-mono" label="院内编码" prop="itemCode" width="130"/>
        <el-table-column label="院内名称" min-width="170" prop="itemName" show-overflow-tooltip/>
        <el-table-column label="规格/标本" prop="spec" show-overflow-tooltip width="130"/>
        <el-table-column align="right" label="价格" width="90">
          <template #default="{row}">¥{{ (row.price ?? 0).toFixed?.(2) ?? row.price }}</template>
        </el-table-column>
        <el-table-column label="对照状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.mappingId" size="small" type="success">已对照</el-tag>
            <el-tag v-else size="small" type="info">未对照</el-tag>
          </template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="国家医保编码" prop="ybCode" show-overflow-tooltip width="230">
          <template #default="{row}">{{ row.ybCode || '—' }}</template>
        </el-table-column>
        <el-table-column label="目录名称" min-width="150" prop="ybName" show-overflow-tooltip>
          <template #default="{row}">{{ row.ybName || '—' }}</template>
        </el-table-column>
        <el-table-column label="甲乙类" width="80">
          <template #default="{row}">
            {{ row.insuranceLevel ? (levelMap[row.insuranceLevel] || `未知(${row.insuranceLevel})`) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="对照方式" width="95">
          <template #default="{row}">{{
              row.matchType ? (matchTypeMap[row.matchType] || `未知(${row.matchType})`) : '—'
            }}
          </template>
        </el-table-column>
        <el-table-column label="对照时间" prop="mappedTime" width="160">
          <template #default="{row}">{{ (row.mappedTime || '').slice(0, 16).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{row}">
            <el-button v-if="!row.mappingId" v-perm="'finance:insuranceMapping:edit'" :data-testid="`yb-map-btn-${row.itemCode}`" plain size="small"
                       type="primary" @click="openMapDialog(row)">对照
            </el-button>
            <template v-else>
              <el-button v-perm="'finance:insuranceMapping:edit'" :data-testid="`yb-map-btn-${row.itemCode}`" plain
                         size="small" @click="openMapDialog(row)">换对照
              </el-button>
              <el-button v-perm="'finance:insuranceMapping:edit'" :data-testid="`yb-unmap-btn-${row.itemCode}`" :icon="Close" plain size="small"
                         type="danger" @click="confirmUnmap(row)"/>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total"
                       data-testid="yb-pagination"
                       layout="total, sizes, prev, pager, next, jumper" @current-change="loadRows"
                       @size-change="pagination.pageNum = 1; loadRows()"/>
      </div>
    </el-card>

    <!-- 对照弹窗 -->
    <el-dialog v-model="mapDialog" :title="`医保目录对照 — ${mapRow?.itemName || ''}`" data-testid="yb-map-dialog"
               width="820px">
      <div class="flex flex-wrap items-center gap-2 mb-3">
        <span class="text-sm text-slate-500">目录类型：</span>
        <el-radio-group v-model="mapForm.catalogType" size="small" @change="loadCandidates">
          <el-radio-button :value="1">西药/中成药</el-radio-button>
          <el-radio-button :value="2">中药饮片</el-radio-button>
          <el-radio-button :value="3">医疗服务项目</el-radio-button>
          <el-radio-button :value="4">医用耗材</el-radio-button>
        </el-radio-group>
        <el-input v-model="mapForm.keyword" clearable data-testid="yb-candidate-keyword" placeholder="编码/名称搜索" size="small"
                  style="width: 220px" @keyup.enter="loadCandidates"/>
        <el-button :icon="Search" size="small" @click="loadCandidates">搜索</el-button>
      </div>
      <el-table v-loading="candidatesLoading" :data="candidates" data-testid="yb-candidate-table" height="360"
                highlight-current-row size="small"
                @current-change="(r: any) => selectedCatalog = r">
        <el-table-column width="40">
          <template #default="{row}">
            <el-icon v-if="selectedCatalog?.id === row.id" color="#1269B5">
              <Check/>
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column class-name="font-mono text-xs" label="医保编码" prop="ybCode" show-overflow-tooltip
                         width="230"/>
        <el-table-column label="目录名称" min-width="170" prop="ybName" show-overflow-tooltip/>
        <el-table-column label="规格" prop="spec" show-overflow-tooltip width="110"/>
        <el-table-column label="甲乙类" width="75">
          <template #default="{row}">
            {{ row.insuranceLevel ? (levelMap[row.insuranceLevel] || `未知(${row.insuranceLevel})`) : '—' }}
          </template>
        </el-table-column>
        <el-table-column align="right" label="支付比例" width="85">
          <template #default="{row}">{{ row.payRatio != null ? row.payRatio + '%' : '—' }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="mapDialog = false">取消</el-button>
        <el-button :disabled="!selectedCatalog" data-testid="yb-map-confirm" type="primary" @click="confirmMap">
          确认对照
        </el-button>
      </template>
    </el-dialog>

    <!-- 目录导入弹窗 -->
    <el-dialog v-model="importDialog" data-testid="yb-import-dialog" title="批量导入医保目录" width="680px">
      <el-alert :closable="false" class="mb-2" title="每行一条，逗号分隔：类型(1西药中成药/2中药饮片/3医疗服务项目/4医用耗材), 医保编码, 名称, 规格, 单位, 剂型, 甲乙类(1甲/2乙/3丙), 支付比例%"
                type="info"/>
      <el-input v-model="csvText" :rows="10" data-testid="yb-import-text"
                placeholder="1,XJ01CAA000001AA96150100124,阿莫西林胶囊,0.25g×24粒,盒,胶囊剂,1,100"
                type="textarea"/>
      <div class="text-xs text-slate-500 mt-2">已解析 {{ parsedCsv.length }} 行（按医保编码幂等：已存在则更新）</div>
      <template #footer>
        <el-button @click="importDialog = false">取消</el-button>
        <el-button data-testid="yb-import-confirm" type="primary" @click="doImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- 目录管理弹窗 -->
    <el-dialog v-model="catalogDialog" data-testid="yb-catalog-dialog" title="医保目录管理（只启停不删除，保证已对照追溯链）"
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
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Check, Close, Connection, Refresh, Search, Setting, Upload} from '@element-plus/icons-vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {
  autoMatchYb,
  changeYbCatalogStatus,
  getYbCatalogList,
  getYbMappingList,
  getYbMappingStats,
  importYbCatalog,
  mapYbItem,
  unmapYbItem,
  upsertYbCatalog
} from '@/api/insuranceMapping';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---- 口径（无字典枚举，页面内单点定义）----
const itemTypeMap = {1: '药品', 2: '诊疗项目', 3: '检验项目', 4: '耗材'};
const matchTypeMap = {1: '自动对照', 2: '人工对照', 3: '批量导入'};
const levelMap = {1: '甲类', 2: '乙类', 3: '丙类'};
const catalogTypeMap = {1: '西药/中成药', 2: '中药饮片', 3: '医疗服务项目', 4: '医用耗材'};
// 院内类型 → 默认候选目录类型（药品 1→西药/中成药；饮片在弹窗里手动切）
const defaultCatalogType = {1: 1, 2: 3, 3: 3, 4: 4};
// ---- 统计 ----
const stats = ref([]);
const statsRate = (t, m) => (t > 0 ? Math.round((m / t) * 100) : 0);
// ---- 主列表 ----
const loading = ref(false);
const rows = ref([]);
const query = ref({itemType: 1, keyword: '', mapStatus: undefined, itemStatus: undefined});
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadStats = async () => {
  const res = await getYbMappingStats();
  stats.value = res.data || [];
};
const loadRows = async () => {
  loading.value = true;
  try {
    const res = await getYbMappingList({
      ...query.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    });
    rows.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.value.keyword = '';
  query.value.mapStatus = undefined;
  query.value.itemStatus = undefined;
  pagination.value.pageNum = 1;
  loadRows();
};
const switchType = (t) => {
  query.value.itemType = t;
  pagination.value.pageNum = 1;
  loadRows();
};
// ---- 对照弹窗 ----
const mapDialog = ref(false);
const mapRow = ref(null);
const mapForm = ref({catalogType: 1, keyword: ''});
const candidates = ref([]);
const candidatesLoading = ref(false);
const selectedCatalog = ref(null);
const openMapDialog = (row) => {
  mapRow.value = row;
  mapForm.value = {catalogType: defaultCatalogType[row.itemType] || 1, keyword: row.itemName || ''};
  selectedCatalog.value = null;
  mapDialog.value = true;
  loadCandidates();
};
const loadCandidates = async () => {
  candidatesLoading.value = true;
  try {
    const res = await getYbCatalogList({
      catalogType: mapForm.value.catalogType,
      keyword: mapForm.value.keyword || undefined,
      status: 1,
      pageNum: 1,
      pageSize: 50,
    });
    candidates.value = res.data?.records || [];
  } finally {
    candidatesLoading.value = false;
  }
};
const confirmMap = async () => {
  if (!selectedCatalog.value) {
    ElMessage.warning('请先在候选列表中选择一条医保目录');
    return;
  }
  const r = await mapYbItem({
    itemType: mapRow.value.itemType,
    itemId: mapRow.value.itemId,
    catalogId: selectedCatalog.value.id
  });
  ElMessage.success(`已对照：${r.data.ybCode} ${r.data.ybName}`);
  mapDialog.value = false;
  await Promise.all([loadRows(), loadStats()]);
};
// ---- 解对照 ----
const confirmUnmap = (row) => {
  ElMessageBox.confirm(`解除「${row.itemName}」与医保编码 ${row.ybCode} 的对照？解除后该行回到未对照状态。`, '解对照确认', {type: 'warning'})
      .then(async () => {
        await unmapYbItem(row.itemType, row.itemId);
        ElMessage.success('已解对照');
        await Promise.all([loadRows(), loadStats()]);
      })
      .catch(() => {
      });
};
// ---- 自动对照 ----
const autoMatching = ref(false);
const doAutoMatch = async () => {
  ElMessageBox.confirm(query.value.itemType
      ? `对「${itemTypeMap[query.value.itemType]}」执行自动对照？规则：院内名称与启用目录名称精确相等且唯一命中才落对照，同名多条留人工处理。`
      : '对全部类型执行自动对照？规则：名称精确相等且唯一命中才落对照。', '自动对照', {type: 'info'})
      .then(async () => {
        autoMatching.value = true;
        try {
          const r = await autoMatchYb(query.value.itemType);
          const v = r.data;
          ElMessage.success(`自动对照完成：成功 ${v.matched} 条，同名歧义跳过 ${v.ambiguous} 条，无匹配 ${v.noMatch} 条`);
          await Promise.all([loadRows(), loadStats()]);
        } finally {
          autoMatching.value = false;
        }
      })
      .catch(() => {
      });
};
// ---- 目录导入（CSV 粘贴）----
const importDialog = ref(false);
const csvText = ref('');
const parsedCsv = computed(() => {
  return csvText.value.split('\n').map(l => l.trim()).filter(Boolean).map(l => {
    const p = l.split(',').map(s => s.trim());
    if (p.length < 3)
      return null;
    const t = Number(p[0]);
    if (![1, 2, 3, 4].includes(t))
      return null;
    return {
      catalogType: t, ybCode: p[1], ybName: p[2],
      spec: p[3] || undefined, unit: p[4] || undefined, dosageForm: p[5] || undefined,
      insuranceLevel: p[6] ? Number(p[6]) : undefined,
      payRatio: p[7] ? Number(p[7]) : undefined,
    };
  }).filter(Boolean);
});
const openImportDialog = () => {
  csvText.value = '';
  importDialog.value = true;
};
const doImport = async () => {
  if (!parsedCsv.value.length) {
    ElMessage.warning('没有可导入的行（格式：类型,编码,名称,规格,单位,剂型,甲乙类,支付比例）');
    return;
  }
  const r = await importYbCatalog(parsedCsv.value);
  const v = r.data;
  ElMessage.success(`导入完成：新增 ${v.inserted} 条，更新 ${v.updated} 条，跳过 ${v.skipped} 条`);
  importDialog.value = false;
  if (catalogDialog.value)
    await loadCatalogs();
};
// ---- 目录管理（列表 + 启停 + 补录）----
const catalogDialog = ref(false);
const catalogs = ref([]);
const catalogsLoading = ref(false);
const catalogQuery = ref({catalogType: undefined, keyword: ''});
const catalogForm = ref(null);
const openCatalogDialog = () => {
  catalogDialog.value = true;
  loadCatalogs();
};
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
onMounted(loadStats);
</script>
