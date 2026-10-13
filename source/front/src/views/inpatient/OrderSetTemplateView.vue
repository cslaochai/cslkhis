<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                :prefix-icon="Search"
                class="!w-64"
                clearable
                data-testid="orderset-search"
                placeholder="组套名称 / 适用场景"
                size="small"
                @keyup.enter="onSearch"
            />
          </el-form-item>
          <el-form-item label="共享范围">
            <el-select v-model="query.scope" class="!w-32" clearable placeholder="共享范围" size="small">
              <el-option v-for="s in SCOPE_OPTIONS" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="医嘱类型">
            <el-select v-model="query.orderType" class="!w-32" clearable placeholder="医嘱类型" size="small">
              <el-option :value="1" label="长期"/>
              <el-option :value="2" label="临时"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="orderset-query" size="small" type="primary" @click="onSearch">查询
            </el-button>
            <el-button :icon="Refresh" size="small" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:orderSet:add'" :icon="Plus" data-testid="orderset-add" plain size="small"
                     type="primary" @click="openCreate">
            新增组套
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
          v-loading="loading"
          :data="rows"
          :max-height="tableMaxHeight"
          data-testid="orderset-table"
          row-key="id"
          stripe
          style="width: 100%"
      >
        <el-table-column label="组套名称" min-width="180" prop="templateName" show-overflow-tooltip/>
        <el-table-column align="center" label="共享范围" width="100">
          <template #default="{ row }">
            <el-tag :type="scopeTagType(row.scope)" size="small">{{ row.scopeText || scopeText(row.scope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归属" show-overflow-tooltip width="150">
          <template #default="{ row }">
            <span v-if="row.scope === 3">全院共用</span>
            <span v-else-if="row.scope === 2">{{ row.deptName || '本科室' }}</span>
            <span v-else>{{ row.doctorName || '本人' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="医嘱类型" width="90">
          <template #default="{ row }">{{ row.orderTypeText }}</template>
        </el-table-column>
        <el-table-column align="center" label="明细条数" prop="itemCount" width="90"/>
        <el-table-column label="适用场景" min-width="180" prop="remark" show-overflow-tooltip/>
        <el-table-column label="更新时间" prop="updateTime" width="160"/>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="orderset-view" link size="small" type="primary" @click="openView(row)">
              明细
            </el-button>
            <el-button
                v-if="row.editable"
                v-perm="'ipd:orderSet:add'"
                data-testid="orderset-edit"
                link
                size="small"
                type="primary"
                @click="openEdit(row)"
            >编辑
            </el-button>
            <el-button
                v-if="row.editable"
                v-perm="'ipd:orderSet:delete'"
                data-testid="orderset-delete"
                link
                size="small"
                type="danger"
                @click="remove(row)"
            >删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">还没有组套：点「新增组套」把一组常用医嘱存成模板</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            size="small"
            @current-change="loadList"
            @size-change="onSearch"
        />
      </div>
    </el-card>

    <!-- 明细预览（只读，子组件 OrderSetTemplateDetail，按 id 取数） -->
    <OrderSetTemplateDetail v-model="viewVisible" :id="viewId"/>

    <!-- 新增 / 编辑（子组件 OrderSetTemplateEdit，row 为空走新增） -->
    <OrderSetTemplateEdit v-model="editVisible" :row="editRow" :scope-options="SCOPE_OPTIONS" @saved="loadList"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {deleteOrderSetById, getOrderSetListPage,} from '@/api/inpatientOrder';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import OrderSetTemplateEdit from './OrderSetTemplateEdit.vue';
import OrderSetTemplateDetail from './OrderSetTemplateDetail.vue';

const SCOPE_OPTIONS = [
  {value: 1, label: '个人'},
  {value: 2, label: '科室'},
  {value: 3, label: '全院'},
];
const scopeText = (v) => SCOPE_OPTIONS.find((o) => o.value === v)?.label || `未知(${v})`;
const scopeTagType = (v) => (v === 3 ? 'danger' : v === 2 ? 'warning' : 'info');
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  scope: null,
  orderType: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getOrderSetListPage({
      keyword: query.keyword || undefined,
      scope: query.scope ?? undefined,
      orderType: query.orderType ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    rows.value = res?.data?.records || [];
    total.value = Number(res?.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const onSearch = () => {
  query.pageNum = 1;
  loadList();
};
const onReset = () => {
  query.keyword = '';
  query.scope = null;
  query.orderType = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 明细预览（子组件 OrderSetTemplateDetail，按 id 取数） ----------------
const viewVisible = ref(false);
const viewId = ref(null);
const openView = (row) => {
  viewId.value = row.id;
  viewVisible.value = true;
};
// ---------------- 新增 / 编辑（子组件 OrderSetTemplateEdit，row 为空走新增） ----------------
const editVisible = ref(false);
const editRow = ref(null);
const openCreate = () => {
  editRow.value = null;
  editVisible.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  editVisible.value = true;
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`删除后新开医嘱时不再能套用「${row.templateName}」（${row.itemCount} 条明细），已按它开出的医嘱不受影响。`, '删除组套', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await deleteOrderSetById(row.id);
    ElMessage.success('已删除');
    loadList();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
onMounted(() => {
  loadList();
});
</script>
