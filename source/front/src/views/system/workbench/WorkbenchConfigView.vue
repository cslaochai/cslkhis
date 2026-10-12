<template>
  <div>
    <!-- 工作台卡片管理（原「工作台配置」去掉角色工作台页签后的卡片注册表）：查询卡 + 表格卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="widgetQuery" inline>
          <el-form-item label="卡片编码/名称">
            <el-input
                v-model="widgetQuery.keyword"
                clearable
                placeholder="卡片编码 / 名称"
                @keyup.enter="handleWidgetSearch"
            />
          </el-form-item>
          <el-form-item label="分区">
            <el-select v-model="widgetQuery.area" :fit-input-width="false" clearable placeholder="分区"
                       style="width: 150px">
              <el-option v-for="a in AREA_OPTIONS" :key="a.value" :label="a.label" :value="a.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="widgetQuery.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 130px">
              <el-option :value="1" label="已上线"/>
              <el-option :value="0" label="未上线"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleWidgetSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleWidgetReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增卡片靠右 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button :icon="Plus" type="primary" @click="openAdd">新增卡片</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="widgetLoading" :data="widgetRows" :max-height="tableMaxHeight" border row-key="id">
        <el-table-column label="编码" min-width="160" prop="widgetCode"/>
        <el-table-column label="名称" min-width="140" prop="widgetName"/>
        <el-table-column label="分区" width="110">
          <template #default="{ row }">{{ areaLabel(row.area) }}</template>
        </el-table-column>
        <el-table-column label="取数来源" min-width="180" prop="apiKey" show-overflow-tooltip/>
        <el-table-column label="权限码" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.permission">{{ row.permission }}</span>
            <span v-else class="text-slate-400">登录即可见</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="宽度" width="80">
          <template #default="{ row }">{{ row.span }}</template>
        </el-table-column>
        <el-table-column align="center" label="顺序" prop="sortOrder" width="80"/>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" size="small" type="success">已上线</el-tag>
            <el-tag v-else size="small" type="info">未上线</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button :icon="Edit" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button :icon="Delete" link type="danger" @click="removeWidget(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="widgetPage.pageNum"
            v-model:page-size="widgetPage.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="widgetPage.total"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleWidgetSearch"
            @current-change="loadWidgets"
        />
      </div>
    </el-card>

    <WorkbenchConfigEdit v-model="dialogVisible" :area-options="AREA_OPTIONS" :row="editRow" @saved="loadWidgets"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {deleteWidgetById, widgetListPage} from '@/api/workbench';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import WorkbenchConfigEdit from '@/views/system/workbench/WorkbenchConfigEdit.vue';

/** 卡片分区，与 sys_workbench_widget.area 一字不差 */
const AREA_OPTIONS = [
  {value: 'todo', label: '待办'},
  {value: 'notice', label: '通知'},
  {value: 'entry', label: '快捷入口'},
  {value: 'kpi', label: '数字概览'},
  {value: 'domain', label: '业务领域'},
];
const areaLabel = (a) => AREA_OPTIONS.find(x => x.value === a)?.label || a;

const widgetLoading = ref(false);
const widgetRows = ref([]);
const widgetQuery = reactive({keyword: '', area: '', status: null});
const widgetPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const dialogVisible = ref(false);
// 新增/修改共用 WorkbenchConfigEdit，row 为空走新增
const editRow = ref(null);

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/role/RoleView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

async function loadWidgets() {
  widgetLoading.value = true;
  try {
    const res = await widgetListPage({
      keyword: widgetQuery.keyword || undefined,
      area: widgetQuery.area || undefined,
      status: widgetQuery.status,
      pageNum: widgetPage.pageNum,
      pageSize: widgetPage.pageSize,
    });
    widgetRows.value = res.data?.records || [];
    widgetPage.total = Number(res.data?.total || 0);
  } catch (e) {
    ElMessage.error(e?.message || '卡片列表加载失败');
  } finally {
    widgetLoading.value = false;
  }
}

function handleWidgetSearch() {
  widgetPage.pageNum = 1;
  loadWidgets();
}

function handleWidgetReset() {
  widgetQuery.keyword = '';
  widgetQuery.area = '';
  widgetQuery.status = null;
  handleWidgetSearch();
}

function openAdd() {
  editRow.value = null;
  dialogVisible.value = true;
}

function openEdit(row) {
  editRow.value = row;
  dialogVisible.value = true;
}

async function removeWidget(row) {
  try {
    await ElMessageBox.confirm(`删除卡片「${row.widgetName}」会同时删除全部角色对它的配置，确定继续？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await deleteWidgetById(row.id);
    ElMessage.success('删除成功');
    loadWidgets();
  } catch (e) {
    ElMessage.error(e?.message || '删除失败');
  }
}

onMounted(() => {
  loadWidgets();
});
</script>
