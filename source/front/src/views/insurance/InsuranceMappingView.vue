<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search, Refresh, Connection, Upload, Setting, Check, Close} from '@element-plus/icons-vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  getYbCatalogList, upsertYbCatalog, importYbCatalog, changeYbCatalogStatus,
  getYbMappingList, getYbMappingStats, mapYbItem, unmapYbItem, autoMatchYb
} from '@/api/insuranceMapping'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

// ---- 口径（无字典枚举，页面内单点定义）----
const itemTypeMap: Record<number, string> = {1: '药品', 2: '诊疗项目', 3: '检验项目', 4: '耗材'}
const matchTypeMap: Record<number, string> = {1: '自动对照', 2: '人工对照', 3: '批量导入'}
const levelMap: Record<number, string> = {1: '甲类', 2: '乙类', 3: '丙类'}
const catalogTypeMap: Record<number, string> = {1: '西药/中成药', 2: '中药饮片', 3: '医疗服务项目', 4: '医用耗材'}
// 院内类型 → 默认候选目录类型（药品 1→西药/中成药；饮片在弹窗里手动切）
const defaultCatalogType: Record<number, number> = {1: 1, 2: 3, 3: 3, 4: 4}

// ---- 统计 ----
const stats = ref<any[]>([])
const statsRate = (t: number, m: number) => (t > 0 ? Math.round((m / t) * 100) : 0)

// ---- 主列表 ----
const loading = ref(false)
const rows = ref<any[]>([])
const query = ref({itemType: 1, keyword: '', mapStatus: undefined as number | undefined, itemStatus: undefined as number | undefined})
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadStats = async () => {
  const res: any = await getYbMappingStats()
  stats.value = res.data || []
}

const loadRows = async () => {
  loading.value = true
  try {
    const res: any = await getYbMappingList({...query.value, pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize})
    rows.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.value.keyword = ''
  query.value.mapStatus = undefined
  query.value.itemStatus = undefined
  pagination.value.pageNum = 1
  loadRows()
}

const switchType = (t: number) => {
  query.value.itemType = t
  pagination.value.pageNum = 1
  loadRows()
}

// ---- 对照弹窗 ----
const mapDialog = ref(false)
const mapRow = ref<any>(null)
const mapForm = ref({catalogType: 1, keyword: ''})
const candidates = ref<any[]>([])
const candidatesLoading = ref(false)
const selectedCatalog = ref<any>(null)

const openMapDialog = (row: any) => {
  mapRow.value = row
  mapForm.value = {catalogType: defaultCatalogType[row.itemType] || 1, keyword: row.itemName || ''}
  selectedCatalog.value = null
  mapDialog.value = true
  loadCandidates()
}

const loadCandidates = async () => {
  candidatesLoading.value = true
  try {
    const res: any = await getYbCatalogList({
      catalogType: mapForm.value.catalogType,
      keyword: mapForm.value.keyword || undefined,
      status: 1,
      pageNum: 1,
      pageSize: 50,
    })
    candidates.value = res.data?.records || []
  } finally {
    candidatesLoading.value = false
  }
}

const confirmMap = async () => {
  if (!selectedCatalog.value) {
    ElMessage.warning('请先在候选列表中选择一条医保目录')
    return
  }
  const r: any = await mapYbItem({itemType: mapRow.value.itemType, itemId: mapRow.value.itemId, catalogId: selectedCatalog.value.id})
  ElMessage.success(`已对照：${r.data.ybCode} ${r.data.ybName}`)
  mapDialog.value = false
  await Promise.all([loadRows(), loadStats()])
}

// ---- 解对照 ----
const confirmUnmap = (row: any) => {
  ElMessageBox.confirm(`解除「${row.itemName}」与医保编码 ${row.ybCode} 的对照？解除后该行回到未对照状态。`, '解对照确认', {type: 'warning'})
    .then(async () => {
      await unmapYbItem(row.itemType, row.itemId)
      ElMessage.success('已解对照')
      await Promise.all([loadRows(), loadStats()])
    })
    .catch(() => {})
}

// ---- 自动对照 ----
const autoMatching = ref(false)
const doAutoMatch = async () => {
  ElMessageBox.confirm(
    query.value.itemType
      ? `对「${itemTypeMap[query.value.itemType]}」执行自动对照？规则：院内名称与启用目录名称精确相等且唯一命中才落对照，同名多条留人工处理。`
      : '对全部类型执行自动对照？规则：名称精确相等且唯一命中才落对照。',
    '自动对照', {type: 'info'})
    .then(async () => {
      autoMatching.value = true
      try {
        const r: any = await autoMatchYb(query.value.itemType)
        const v = r.data
        ElMessage.success(`自动对照完成：成功 ${v.matched} 条，同名歧义跳过 ${v.ambiguous} 条，无匹配 ${v.noMatch} 条`)
        await Promise.all([loadRows(), loadStats()])
      } finally {
        autoMatching.value = false
      }
    })
    .catch(() => {})
}

// ---- 目录导入（CSV 粘贴）----
const importDialog = ref(false)
const csvText = ref('')
const parsedCsv = computed(() => {
  return csvText.value.split('\n').map(l => l.trim()).filter(Boolean).map(l => {
    const p = l.split(',').map(s => s.trim())
    if (p.length < 3) return null
    const t = Number(p[0])
    if (![1, 2, 3, 4].includes(t)) return null
    return {
      catalogType: t, ybCode: p[1], ybName: p[2],
      spec: p[3] || undefined, unit: p[4] || undefined, dosageForm: p[5] || undefined,
      insuranceLevel: p[6] ? Number(p[6]) : undefined,
      payRatio: p[7] ? Number(p[7]) : undefined,
    }
  }).filter(Boolean)
})

const openImportDialog = () => {
  csvText.value = ''
  importDialog.value = true
}

const doImport = async () => {
  if (!parsedCsv.value.length) {
    ElMessage.warning('没有可导入的行（格式：类型,编码,名称,规格,单位,剂型,甲乙类,支付比例）')
    return
  }
  const r: any = await importYbCatalog(parsedCsv.value)
  const v = r.data
  ElMessage.success(`导入完成：新增 ${v.inserted} 条，更新 ${v.updated} 条，跳过 ${v.skipped} 条`)
  importDialog.value = false
  if (catalogDialog.value) await loadCatalogs()
}

// ---- 目录管理（列表 + 启停 + 补录）----
const catalogDialog = ref(false)
const catalogs = ref<any[]>([])
const catalogsLoading = ref(false)
const catalogQuery = ref({catalogType: undefined as number | undefined, keyword: ''})
const catalogForm = ref<any>(null)

const openCatalogDialog = () => {
  catalogDialog.value = true
  loadCatalogs()
}

const loadCatalogs = async () => {
  catalogsLoading.value = true
  try {
    const res: any = await getYbCatalogList({...catalogQuery.value, pageNum: 1, pageSize: 100})
    catalogs.value = res.data?.records || []
  } finally {
    catalogsLoading.value = false
  }
}

const toggleCatalog = async (row: any) => {
  const target = row.status === 1 ? 0 : 1
  await changeYbCatalogStatus(row.id, target)
  ElMessage.success(target === 1 ? '已启用' : '已停用（停用后不可新对照）')
  await loadCatalogs()
}

const saveCatalog = async () => {
  const f = catalogForm.value
  if (!f || !f.catalogType || !f.ybCode || !f.ybName) {
    ElMessage.warning('目录类型、编码、名称为必填')
    return
  }
  await upsertYbCatalog({...f, id: f.id || undefined, status: f.status ?? 1})
  ElMessage.success(f.id ? '目录已更新' : '目录已补录')
  catalogForm.value = null
  await loadCatalogs()
}

onMounted(loadStats)
</script>

<template>
  <div data-testid="yb-mapping-page">
    <!-- 对照率统计 -->
    <el-row :gutter="12" class="mb-3">
      <el-col v-for="s in stats" :key="s.itemType" :span="6">
        <el-card shadow="never" class="!rounded-lg" :data-testid="`yb-stats-${s.itemType}`">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-slate-500">{{ itemTypeMap[s.itemType] }}对照率</div>
              <div class="text-2xl font-semibold mt-1">{{ statsRate(s.total, s.mapped) }}<span class="text-sm font-normal">%</span></div>
              <div class="text-xs text-slate-400 mt-0.5">已对照 {{ s.mapped }} / {{ s.total }}</div>
            </div>
            <el-progress type="circle" :percentage="statsRate(s.total, s.mapped)" :width="64"
                         :status="statsRate(s.total, s.mapped) >= 100 ? 'success' : undefined" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="项目类型">
            <el-radio-group :model-value="query.itemType" @update:model-value="switchType" data-testid="yb-type-switch">
              <el-radio-button v-for="(label, t) in itemTypeMap" :key="t" :value="t">{{ label }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="院内/医保编码、名称" clearable style="width: 220px"
                      data-testid="yb-keyword" @keyup.enter="pagination.pageNum = 1; loadRows()" />
          </el-form-item>
          <el-form-item label="对照状态">
            <el-select v-model="query.mapStatus" placeholder="对照状态" clearable style="width: 120px" data-testid="yb-map-status">
              <el-option label="未对照" :value="0" />
              <el-option label="已对照" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="项目状态">
            <el-select v-model="query.itemStatus" placeholder="项目状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" data-testid="yb-search-btn" @click="pagination.pageNum = 1; loadRows()">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button :icon="Connection" :loading="autoMatching" v-perm="'finance:insuranceMapping:edit'" data-testid="yb-auto-match-btn" @click="doAutoMatch">自动对照</el-button>
          <el-button :icon="Upload" v-perm="'finance:insuranceMapping:add'" data-testid="yb-import-btn" @click="openImportDialog">导入目录</el-button>
          <el-button :icon="Setting" v-perm="'finance:insuranceMapping:add'" data-testid="yb-catalog-manage-btn" @click="openCatalogDialog">目录管理</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡：主表 院内项目 ↔ 医保编码 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" style="width: 100%" data-testid="yb-mapping-table" stripe :max-height="tableMaxHeight">
        <el-table-column prop="itemCode" label="院内编码" width="130" class-name="font-mono" />
        <el-table-column prop="itemName" label="院内名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格/标本" width="130" show-overflow-tooltip />
        <el-table-column label="价格" width="90" align="right">
          <template #default="{row}">¥{{ (row.price ?? 0).toFixed?.(2) ?? row.price }}</template>
        </el-table-column>
        <el-table-column label="对照状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.mappingId" type="success" size="small">已对照</el-tag>
            <el-tag v-else type="info" size="small">未对照</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ybCode" label="国家医保编码" width="230" class-name="font-mono" show-overflow-tooltip>
          <template #default="{row}">{{ row.ybCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="ybName" label="目录名称" min-width="150" show-overflow-tooltip>
          <template #default="{row}">{{ row.ybName || '—' }}</template>
        </el-table-column>
        <el-table-column label="甲乙类" width="80">
          <template #default="{row}">{{ row.insuranceLevel ? (levelMap[row.insuranceLevel] || `未知(${row.insuranceLevel})`) : '—' }}</template>
        </el-table-column>
        <el-table-column label="对照方式" width="95">
          <template #default="{row}">{{ row.matchType ? (matchTypeMap[row.matchType] || `未知(${row.matchType})`) : '—' }}</template>
        </el-table-column>
        <el-table-column prop="mappedTime" label="对照时间" width="160">
          <template #default="{row}">{{ (row.mappedTime || '').slice(0, 16).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button v-if="!row.mappingId" size="small" type="primary" plain v-perm="'finance:insuranceMapping:edit'"
                       :data-testid="`yb-map-btn-${row.itemCode}`" @click="openMapDialog(row)">对照</el-button>
            <template v-else>
              <el-button size="small" plain v-perm="'finance:insuranceMapping:edit'"
                         :data-testid="`yb-map-btn-${row.itemCode}`" @click="openMapDialog(row)">换对照</el-button>
              <el-button size="small" type="danger" plain :icon="Close" v-perm="'finance:insuranceMapping:edit'"
                         :data-testid="`yb-unmap-btn-${row.itemCode}`" @click="confirmUnmap(row)" />
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :total="pagination.total" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next, jumper"
                       data-testid="yb-pagination" @current-change="loadRows" @size-change="pagination.pageNum = 1; loadRows()" />
      </div>
    </el-card>

    <!-- 对照弹窗 -->
    <el-dialog v-model="mapDialog" :title="`医保目录对照 — ${mapRow?.itemName || ''}`" width="820px" data-testid="yb-map-dialog">
      <div class="flex flex-wrap items-center gap-2 mb-3">
        <span class="text-sm text-slate-500">目录类型：</span>
        <el-radio-group v-model="mapForm.catalogType" size="small" @change="loadCandidates">
          <el-radio-button :value="1">西药/中成药</el-radio-button>
          <el-radio-button :value="2">中药饮片</el-radio-button>
          <el-radio-button :value="3">医疗服务项目</el-radio-button>
          <el-radio-button :value="4">医用耗材</el-radio-button>
        </el-radio-group>
        <el-input v-model="mapForm.keyword" placeholder="编码/名称搜索" clearable style="width: 220px" size="small"
                  data-testid="yb-candidate-keyword" @keyup.enter="loadCandidates" />
        <el-button size="small" :icon="Search" @click="loadCandidates">搜索</el-button>
      </div>
      <el-table :data="candidates" v-loading="candidatesLoading" height="360" size="small"
                data-testid="yb-candidate-table" highlight-current-row @current-change="(r: any) => selectedCatalog = r">
        <el-table-column width="40">
          <template #default="{row}">
            <el-icon v-if="selectedCatalog?.id === row.id" color="#1269B5"><Check /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="ybCode" label="医保编码" width="230" class-name="font-mono text-xs" show-overflow-tooltip />
        <el-table-column prop="ybName" label="目录名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="110" show-overflow-tooltip />
        <el-table-column label="甲乙类" width="75">
          <template #default="{row}">{{ row.insuranceLevel ? (levelMap[row.insuranceLevel] || `未知(${row.insuranceLevel})`) : '—' }}</template>
        </el-table-column>
        <el-table-column label="支付比例" width="85" align="right">
          <template #default="{row}">{{ row.payRatio != null ? row.payRatio + '%' : '—' }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="mapDialog = false">取消</el-button>
        <el-button type="primary" data-testid="yb-map-confirm" :disabled="!selectedCatalog" @click="confirmMap">确认对照</el-button>
      </template>
    </el-dialog>

    <!-- 目录导入弹窗 -->
    <el-dialog v-model="importDialog" title="批量导入医保目录" width="680px" data-testid="yb-import-dialog">
      <el-alert type="info" :closable="false" class="mb-2"
                title="每行一条，逗号分隔：类型(1西药中成药/2中药饮片/3医疗服务项目/4医用耗材), 医保编码, 名称, 规格, 单位, 剂型, 甲乙类(1甲/2乙/3丙), 支付比例%" />
      <el-input v-model="csvText" type="textarea" :rows="10" placeholder="1,XJ01CAA000001AA96150100124,阿莫西林胶囊,0.25g×24粒,盒,胶囊剂,1,100"
                data-testid="yb-import-text" />
      <div class="text-xs text-slate-500 mt-2">已解析 {{ parsedCsv.length }} 行（按医保编码幂等：已存在则更新）</div>
      <template #footer>
        <el-button @click="importDialog = false">取消</el-button>
        <el-button type="primary" data-testid="yb-import-confirm" @click="doImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- 目录管理弹窗 -->
    <el-dialog v-model="catalogDialog" title="医保目录管理（只启停不删除，保证已对照追溯链）" width="960px" data-testid="yb-catalog-dialog">
      <div class="flex flex-wrap items-center gap-2 mb-3">
        <el-select v-model="catalogQuery.catalogType" placeholder="全部类型" clearable style="width: 160px" @change="loadCatalogs">
          <el-option v-for="(label, t) in catalogTypeMap" :key="t" :label="label" :value="t" />
        </el-select>
        <el-input v-model="catalogQuery.keyword" placeholder="编码/名称" clearable style="width: 200px" @keyup.enter="loadCatalogs" />
        <el-button size="default" :icon="Search" @click="loadCatalogs">查询</el-button>
        <div class="flex-1"></div>
        <el-button type="primary" plain @click="catalogForm = {catalogType: 1, ybCode: '', ybName: '', spec: '', unit: '', dosageForm: '', insuranceLevel: undefined, payRatio: undefined, status: 1}">人工补录</el-button>
      </div>

      <el-form v-if="catalogForm" :model="catalogForm" inline class="mb-3 p-3 rounded bg-slate-50" data-testid="yb-catalog-form">
        <el-form-item label="类型">
          <el-select v-model="catalogForm.catalogType" style="width: 140px">
            <el-option v-for="(label, t) in catalogTypeMap" :key="t" :label="label" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="医保编码"><el-input v-model="catalogForm.ybCode" style="width: 220px" data-testid="yb-catalog-code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="catalogForm.ybName" style="width: 180px" data-testid="yb-catalog-name" /></el-form-item>
        <el-form-item label="甲乙类">
          <el-select v-model="catalogForm.insuranceLevel" clearable style="width: 90px">
            <el-option label="甲类" :value="1" /><el-option label="乙类" :value="2" /><el-option label="丙类" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付比例%"><el-input-number v-model="catalogForm.payRatio" :min="0" :max="100" :precision="2" style="width: 110px" /></el-form-item>
        <el-form-item>
          <el-button type="primary" data-testid="yb-catalog-save" @click="saveCatalog">保存</el-button>
          <el-button @click="catalogForm = null">取消</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="catalogs" v-loading="catalogsLoading" height="380" size="small" data-testid="yb-catalog-table">
        <el-table-column prop="ybCode" label="医保编码" width="230" class-name="font-mono text-xs" show-overflow-tooltip />
        <el-table-column prop="ybName" label="目录名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{row}">{{ catalogTypeMap[row.catalogType] || `未知(${row.catalogType})` }}</template>
        </el-table-column>
        <el-table-column prop="spec" label="规格" width="110" show-overflow-tooltip />
        <el-table-column label="甲乙类" width="75">
          <template #default="{row}">{{ row.insuranceLevel ? (levelMap[row.insuranceLevel] || `未知(${row.insuranceLevel})`) : '—' }}</template>
        </el-table-column>
        <el-table-column label="支付比例" width="85" align="right">
          <template #default="{row}">{{ row.payRatio != null ? row.payRatio + '%' : '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{row}">
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain v-perm="'finance:insuranceMapping:edit'"
                       :data-testid="`yb-catalog-toggle-${row.ybCode}`" @click="toggleCatalog(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>
