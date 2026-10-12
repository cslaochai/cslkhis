<template>
  <div data-testid="consumable-dict-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="dictKeyword" :prefix-icon="Search" class="!w-60" clearable placeholder="耗材名称/编码"
                      @keyup.enter="loadDict"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadDict">查询</el-button>
            <el-button :icon="Refresh" @click="dictKeyword = ''; loadDict()">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'asset:supplies:add'" :icon="Plus" plain type="primary" @click="openDictDialog()">
            新增耗材
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="dictList" :max-height="tableMaxHeight" stripe>
        <el-table-column class-name="font-mono" label="编码" prop="consumableCode" width="110"/>
        <el-table-column label="耗材名称" min-width="150" prop="consumableName" show-overflow-tooltip/>
        <el-table-column align="center" label="类别" width="100">
          <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
        </el-table-column>
        <el-table-column align="center" label="高值" width="70">
          <template #default="{ row }">
            <span v-if="row.isHighValue === 1"
                  class="inline-block rounded bg-violet-100 px-2 py-0.5 font-medium text-violet-700">高值</span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="规格" min-width="140" prop="specification" show-overflow-tooltip/>
        <el-table-column align="center" label="单位" prop="unit" width="70"/>
        <el-table-column label="生产厂家" min-width="130" prop="manufacturer" show-overflow-tooltip/>
        <el-table-column align="right" label="零售价" width="90">
          <template #default="{ row }">¥{{ row.retailPrice ?? 0 }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="80">
          <template #default="{ row }">
            <span
                :class="['inline-block rounded px-2 py-0.5 font-medium', row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500']">
              {{ row.status === 1 ? '启用' : row.status === 0 ? '停用' : `未知(${row.status})` }}
            </span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button v-perm="'asset:supplies:edit'" link type="primary" @click="openDictDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="dictList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无耗材字典</div>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="dictPagination.pageNum" v-model:page-size="dictPagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="dictPagination.total" layout="total, sizes, prev, pager, next"
                       @size-change="loadDict" @current-change="loadDict"/>
      </div>
    </el-card>

    <!-- 字典编辑弹窗（新增/修改合一组件） -->
    <ConsumableDictEdit v-model="dictDialogVisible" :row="dictEditRow" @saved="loadDict"/>
  </div>
</template>

<script setup>
/**
 * 耗材字典（菜单 2921 / 路由 /consumable-dict，sql/177）
 *
 * 为什么从「物资耗材」里拆出来单独挂菜单：
 * 字典是**建档**动作 —— 新耗材进院时录一次编码/类别/规格/零售价/是否高值，
 * 一年到头动不了几回；而批次库存、出入库、领用是每天发生的库房动作。
 * 合在一页时，库管每次点开「物资耗材」看到的第一个页签是库存，要建档得先切页签，
 * 而*建档该由谁做*（耗材管理员/设备科）与*发料该由谁做*（库管）也常不是同一个人。
 * 内容与拆之前的「耗材字典」页签同源，接口与权限码均未变。
 */
import {onMounted, ref} from 'vue';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {getConsumableList} from '@/api/supplies';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {categoryLabel} from '@/lib/supplies';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import ConsumableDictEdit from './ConsumableDictEdit.vue';

const loading = ref(false);
const dictList = ref([]);
const dictKeyword = ref('');
const dictPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const dictDialogVisible = ref(false);
const dictEditRow = ref(null);
const loadDict = async () => {
  loading.value = true;
  try {
    const params = {pageNum: dictPagination.value.pageNum, pageSize: dictPagination.value.pageSize};
    if (dictKeyword.value.trim())
      params.keyword = dictKeyword.value.trim();
    const res = await getConsumableList(params);
    dictList.value = res.data?.records || [];
    dictPagination.value.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载耗材字典失败:', e);
  } finally {
    loading.value = false;
  }
};
const openDictDialog = (row) => {
  dictEditRow.value = row || null;
  dictDialogVisible.value = true;
};
onMounted(() => {
  loadDict();
});
</script>
