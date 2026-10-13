<template>
  <div class="kg-page">
    <el-tabs v-model="activeTab" class="config-tabs">
      <!-- 数据表配置 -->
      <el-tab-pane label="数据表配置" name="table">
        <el-card class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <el-form :model="schemaQuery" inline>
              <el-form-item label="表名">
                <el-input v-model="schemaQuery.tableName" clearable placeholder="请输入表名" style="width: 200px" @keyup.enter="searchSchemas"/>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="schemaQuery.isEnabled" clearable placeholder="全部" style="width: 130px">
                  <el-option :value="1" label="启用"/>
                  <el-option :value="0" label="停用"/>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button :icon="Search" type="primary" @click="searchSchemas">搜索</el-button>
                <el-button :icon="Refresh" @click="resetSchemaQuery">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="flex shrink-0 items-start gap-3">
              <el-button :icon="RefreshRight" type="warning" plain @click="rewriteSchema">重写刷新</el-button>
              <el-button :icon="Plus" type="primary" @click="openSchemaEdit()">新增数据表</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table v-loading="schemaLoading" :data="schemaRows" stripe>
            <el-table-column label="表名" prop="tableName" min-width="180"/>
            <el-table-column label="用途说明" prop="tableDesc" min-width="240" show-overflow-tooltip/>
            <el-table-column label="启用列" prop="enabledColumnCount" min-width="80" align="center"/>
            <el-table-column label="状态" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isEnabled ? 'success' : 'info'" size="small">{{ row.isEnabled ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="排序" prop="sortOrder" min-width="70" align="center"/>
            <el-table-column label="创建时间" prop="createTime" min-width="170"/>
            <el-table-column fixed="right" label="操作" width="220" align="center">
              <template #default="{ row }">
                <el-button :icon="Setting" link type="primary" @click.stop="openColumnManage(row)">管理列</el-button>
                <el-button :icon="Edit" link type="primary" @click.stop="openSchemaEdit(row)">编辑</el-button>
                <el-button :icon="Delete" link type="danger" @click.stop="removeSchema(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer flex items-center justify-end mt-3">
            <el-pagination v-model:current-page="schemaQuery.pageNum" v-model:page-size="schemaQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="schemaTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="onSchemaSizeChange" @current-change="loadSchemas"/>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 语义配置 -->
      <el-tab-pane label="语义配置" name="semantic">
        <el-card class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <el-form :model="semanticQuery" inline>
              <el-form-item label="语义词">
                <el-input v-model="semanticQuery.term" clearable placeholder="请输入语义词" style="width: 200px" @keyup.enter="searchSemantics"/>
              </el-form-item>
              <el-form-item>
                <el-button :icon="Search" type="primary" @click="searchSemantics">搜索</el-button>
                <el-button :icon="Refresh" @click="resetSemanticQuery">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="flex shrink-0 items-start gap-3">
              <el-button :icon="Plus" type="primary" @click="openSemanticEdit()">新增语义映射</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table v-loading="semanticLoading" :data="semanticRows" stripe @row-click="openSemanticDetail">
            <el-table-column label="语义词" prop="term" min-width="120"/>
            <el-table-column label="命中表" prop="tableName" min-width="180"/>
            <el-table-column label="目标列" min-width="110">
              <template #default="{ row }">{{ row.columnName || '（表级）' }}</template>
            </el-table-column>
            <el-table-column label="匹配方式" min-width="90" align="center">
              <template #default="{ row }">{{ row.matchMode === 2 ? '精确' : '包含' }}</template>
            </el-table-column>
            <el-table-column label="优先级" prop="priority" min-width="70" align="center"/>
            <el-table-column label="口径说明" prop="scopeDesc" min-width="200" show-overflow-tooltip/>
            <el-table-column label="状态" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isEnabled ? 'success' : 'warning'" size="small">{{ row.isEnabled ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button :icon="Edit" link type="primary" @click.stop="openSemanticEdit(row)">编辑</el-button>
                <el-button :icon="Delete" link type="danger" @click.stop="removeSemantic(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer flex items-center justify-end mt-3">
            <el-pagination v-model:current-page="semanticQuery.pageNum" v-model:page-size="semanticQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="semanticTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="onSemanticSizeChange" @current-change="loadSemantics"/>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 表间关系 -->
      <el-tab-pane label="表间关系" name="relation">
        <el-card class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <el-form :model="relationQuery" inline>
              <el-form-item label="源表">
                <el-input v-model="relationQuery.sourceTable" clearable placeholder="请输入源表" style="width: 160px" @keyup.enter="searchRelations"/>
              </el-form-item>
              <el-form-item label="目标表">
                <el-input v-model="relationQuery.targetTable" clearable placeholder="请输入目标表" style="width: 160px" @keyup.enter="searchRelations"/>
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="relationQuery.relationType" clearable placeholder="全部" style="width: 120px">
                  <el-option :value="2" label="维度关联"/>
                  <el-option :value="1" label="业务关联"/>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button :icon="Search" type="primary" @click="searchRelations">搜索</el-button>
                <el-button :icon="Refresh" @click="resetRelationQuery">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="flex shrink-0 items-start gap-3">
              <el-button :icon="Plus" type="primary" @click="openRelationEdit()">新增表间关系</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table v-loading="relationLoading" :data="relationRows" stripe>
            <el-table-column label="源表" prop="sourceTable" min-width="160"/>
            <el-table-column label="源列" prop="sourceColumn" min-width="100"/>
            <el-table-column align="center" min-width="40">
              <template #default><span class="text-gray-400">→</span></template>
            </el-table-column>
            <el-table-column label="目标表" prop="targetTable" min-width="160"/>
            <el-table-column label="目标列" prop="targetColumn" min-width="100"/>
            <el-table-column label="类型" min-width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.relationType === 2 ? 'primary' : 'info'" size="small">{{ row.relationType === 2 ? '维度关联' : '业务关联' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="关系说明" prop="description" min-width="200" show-overflow-tooltip/>
            <el-table-column label="排序" prop="sortOrder" min-width="70" align="center"/>
            <el-table-column label="状态" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isEnabled ? 'success' : 'warning'" size="small">{{ row.isEnabled ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button :icon="Edit" link type="primary" @click.stop="openRelationEdit(row)">编辑</el-button>
                <el-button :icon="Delete" link type="danger" @click.stop="removeRelation(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer flex items-center justify-end mt-3">
            <el-pagination v-model:current-page="relationQuery.pageNum" v-model:page-size="relationQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="relationTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="onRelationSizeChange" @current-change="loadRelations"/>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 关系图谱 -->
      <el-tab-pane label="关系图谱" name="graph">
        <el-card shadow="never" class="graph-card">
          <div ref="graphRef" class="graph-container" v-loading="graphLoading"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <AiAdminQaSchemaEdit v-model="schemaEditVisible" :row="schemaEditRow" @saved="onSchemaSaved"/>
    <AiAdminQaColumnManage v-model="columnManageVisible" :table-name="columnManageTable"/>
    <AiAdminQaSemanticEdit v-model="semanticEditVisible" :row="semanticEditRow" @saved="onSemanticSaved"/>
    <AiAdminQaRelationEdit v-model="relationEditVisible" :row="relationEditRow" @saved="onRelationSaved"/>

    <el-dialog v-model="semanticDetailVisible" title="语义映射详情" width="600px">
      <el-descriptions v-if="semanticDetailRow" :column="1" border size="small">
        <el-descriptions-item label="语义词">{{ semanticDetailRow.term }}</el-descriptions-item>
        <el-descriptions-item label="命中表">{{ semanticDetailRow.tableName }}</el-descriptions-item>
        <el-descriptions-item label="目标列">{{ semanticDetailRow.columnName || '（表级）' }}</el-descriptions-item>
        <el-descriptions-item label="匹配方式">{{ semanticDetailRow.matchMode === 2 ? '精确' : '包含' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ semanticDetailRow.priority }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ semanticDetailRow.isEnabled ? '启用' : '停用' }}</el-descriptions-item>
        <el-descriptions-item label="口径说明">{{ semanticDetailRow.scopeDesc || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, RefreshRight, Search, Setting } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  deleteOperationQaRelation,
  deleteOperationQaSchema,
  deleteOperationQaSemantic,
  getOperationQaRelationPage,
  getOperationQaSchemaPage,
  getOperationQaSemanticPage,
  rewriteOperationQaSchema,
} from '@/api/ai'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import AiAdminQaSchemaEdit from './AiAdminQaSchemaEdit.vue'
import AiAdminQaColumnManage from './AiAdminQaColumnManage.vue'
import AiAdminQaSemanticEdit from './AiAdminQaSemanticEdit.vue'
import AiAdminQaRelationEdit from './AiAdminQaRelationEdit.vue'

const activeTab = ref('table')
const graphRef = ref(null)
const graphLoading = ref(false)
let chartInstance = null

// ============ 数据表配置 ============
const schemaQuery = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, tableName: '', isEnabled: undefined })
const schemaLoading = ref(false)
const schemaRows = ref([])
const schemaTotal = ref(0)
const loadSchemas = async () => {
  schemaLoading.value = true
  try {
    const res = await getOperationQaSchemaPage({
      pageNum: schemaQuery.pageNum, pageSize: schemaQuery.pageSize,
      tableName: schemaQuery.tableName || undefined, isEnabled: schemaQuery.isEnabled,
    })
    schemaRows.value = res?.data?.records || []
    schemaTotal.value = res?.data?.total || 0
  } catch (e) { console.error('加载数据表配置失败', e) } finally { schemaLoading.value = false }
}
const searchSchemas = () => { schemaQuery.pageNum = 1; loadSchemas() }
const resetSchemaQuery = () => { schemaQuery.tableName = ''; schemaQuery.isEnabled = undefined; searchSchemas() }
const onSchemaSizeChange = () => { schemaQuery.pageNum = 1; loadSchemas() }
const schemaEditVisible = ref(false)
const schemaEditRow = ref(null)
const openSchemaEdit = (row) => { schemaEditRow.value = row || null; schemaEditVisible.value = true }
const onSchemaSaved = () => { schemaQuery.pageNum = 1; loadSchemas() }
const columnManageVisible = ref(false)
const columnManageTable = ref('')
const openColumnManage = (row) => { columnManageTable.value = row.tableName; columnManageVisible.value = true }
const removeSchema = async (row) => {
  await ElMessageBox.confirm(`确认删除数据表「${row.tableName}」？其列配置将一并删除。`, '删除确认', { type: 'warning' })
  try { await deleteOperationQaSchema(row.id); ElMessage.success('已删除'); loadSchemas() } catch (e) { console.error('删除数据表失败', e) }
}
const rewriteSchema = async () => {
  await ElMessageBox.confirm('重写刷新将扫描当前库全部表结构并覆盖现有表/列配置。确定执行？', '重写刷新确认', { type: 'warning' })
  try { const res = await rewriteOperationQaSchema(); ElMessage.success(res?.data || '重写刷新完成'); loadSchemas() } catch (e) { console.error('重写刷新失败', e) }
}

// ============ 语义配置 ============
const semanticQuery = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, term: '' })
const semanticLoading = ref(false)
const semanticRows = ref([])
const semanticTotal = ref(0)
const loadSemantics = async () => {
  semanticLoading.value = true
  try {
    const res = await getOperationQaSemanticPage({
      pageNum: semanticQuery.pageNum, pageSize: semanticQuery.pageSize,
      term: semanticQuery.term || undefined,
    })
    semanticRows.value = res?.data?.records || []
    semanticTotal.value = res?.data?.total || 0
  } catch (e) { console.error('加载语义配置失败', e) } finally { semanticLoading.value = false }
}
const searchSemantics = () => { semanticQuery.pageNum = 1; loadSemantics() }
const resetSemanticQuery = () => { semanticQuery.term = ''; searchSemantics() }
const onSemanticSizeChange = () => { semanticQuery.pageNum = 1; loadSemantics() }
const semanticEditVisible = ref(false)
const semanticEditRow = ref(null)
const openSemanticEdit = (row) => { semanticEditRow.value = row || null; semanticEditVisible.value = true }
const semanticDetailVisible = ref(false)
const semanticDetailRow = ref(null)
const openSemanticDetail = (row) => { semanticDetailRow.value = row; semanticDetailVisible.value = true }
const onSemanticSaved = () => { semanticQuery.pageNum = 1; loadSemantics() }
const removeSemantic = async (row) => {
  await ElMessageBox.confirm(`确认删除语义映射「${row.term}」？`, '删除确认', { type: 'warning' })
  try { await deleteOperationQaSemantic(row.id); ElMessage.success('已删除'); loadSemantics() } catch (e) { console.error('删除语义映射失败', e) }
}

// ============ 表间关系 ============
const relationQuery = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, sourceTable: '', targetTable: '', relationType: null })
const relationLoading = ref(false)
const relationRows = ref([])
const relationTotal = ref(0)
const loadRelations = async () => {
  relationLoading.value = true
  try {
    const res = await getOperationQaRelationPage({
      pageNum: relationQuery.pageNum, pageSize: relationQuery.pageSize,
      sourceTable: relationQuery.sourceTable || undefined,
      targetTable: relationQuery.targetTable || undefined,
      relationType: relationQuery.relationType ?? undefined,
    })
    relationRows.value = res?.data?.records || []
    relationTotal.value = res?.data?.total || 0
  } catch (e) { console.error('加载表间关系失败', e) } finally { relationLoading.value = false }
}
const searchRelations = () => { relationQuery.pageNum = 1; loadRelations() }
const resetRelationQuery = () => { relationQuery.sourceTable = ''; relationQuery.targetTable = ''; relationQuery.relationType = null; searchRelations() }
const onRelationSizeChange = () => { relationQuery.pageNum = 1; loadRelations() }
const relationEditVisible = ref(false)
const relationEditRow = ref(null)
const openRelationEdit = (row) => { relationEditRow.value = row || null; relationEditVisible.value = true }
const onRelationSaved = () => { relationQuery.pageNum = 1; loadRelations(); renderGraph() }
const removeRelation = async (row) => {
  await ElMessageBox.confirm(`确认删除表间关系？`, '删除确认', { type: 'warning' })
  try { await deleteOperationQaRelation(row.id); ElMessage.success('已删除'); loadRelations(); renderGraph() } catch (e) { console.error('删除表间关系失败', e) }
}

// ============ 关系图谱 ============
const renderGraph = async () => {
  if (!graphRef.value) return
  if (!chartInstance) chartInstance = echarts.init(graphRef.value)
  graphLoading.value = true
  try {
    const [schemaRes, relationRes] = await Promise.all([
      getOperationQaSchemaPage({ pageNum: 1, pageSize: 200, isEnabled: 1 }),
      getOperationQaRelationPage({ pageNum: 1, pageSize: 200 }),
    ])
    const tableList = schemaRes?.data?.records || []
    const relList = relationRes?.data?.records || []
    const tableMap = {}
    tableList.forEach(t => { tableMap[t.tableName] = t.tableDesc || t.tableName })
    const dimTables = ['sys_department', 'sys_ward']

    const nodes = tableList.map(t => ({
      id: t.tableName,
      name: t.tableDesc ? `${t.tableDesc}\n${t.tableName}` : t.tableName,
      symbolSize: dimTables.includes(t.tableName) ? 55 : 40,
      category: dimTables.includes(t.tableName) ? 0 : 1,
      itemStyle: { color: dimTables.includes(t.tableName) ? '#0E9488' : '#1269B5' },
      label: { show: true, fontSize: 11 },
    }))
    const links = relList
      .filter(r => tableMap[r.sourceTable] && tableMap[r.targetTable])
      .map(r => ({
        source: r.sourceTable,
        target: r.targetTable,
        lineStyle: {
          color: r.relationType === 2 ? '#0E9488' : '#1269B5',
          width: r.relationType === 2 ? 1.5 : 2,
          curveness: 0.1,
        },
      }))

    chartInstance.setOption({
      tooltip: { formatter: (p) => p.dataType === 'edge' ? `${p.data.source} → ${p.data.target}` : p.data.name },
      legend: [{ data: ['维度表', '业务表'], bottom: 10 }],
      series: [{
        type: 'graph', layout: 'force', roam: true, draggable: true,
        categories: [{ name: '维度表' }, { name: '业务表' }],
        data: nodes, links,
        force: { repulsion: 600, edgeLength: [100, 200], gravity: 0.1 },
        emphasis: { focus: 'adjacency', lineStyle: { width: 4 } },
        label: { show: true, position: 'bottom' },
      }],
    }, true)
    chartInstance.resize()
  } catch (e) { console.error('渲染图谱失败', e) } finally { graphLoading.value = false }
}

watch(activeTab, (val) => { if (val === 'graph') nextTick(() => { renderGraph(); chartInstance?.resize() }) })
onMounted(() => {
  loadSchemas(); loadSemantics(); loadRelations()
  window.addEventListener('resize', () => chartInstance?.resize())
})
</script>

<style scoped>
.kg-page { padding: 16px; }
.graph-card :deep(.el-card__body) { padding: 0; }
.graph-container { width: 100%; height: calc(100vh - 200px); min-height: 500px; background: #fff; }
</style>
