<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="包编码/名称" style="width: 200px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'asset:cssd:edit'" data-testid="cssd-tpl-create-btn" plain type="primary"
                     @click="openCreate">新增模板
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="cssd-tpl-table" stripe>
        <el-table-column label="包编码" prop="templateCode" width="120"/>
        <el-table-column label="器械包名称" min-width="150" prop="packName" show-overflow-tooltip/>
        <el-table-column label="默认灭菌方式" width="120">
          <template #default="{ row }">{{ row.sterilizeMethodText || methodText(row.sterilizeMethod) }}</template>
        </el-table-column>
        <el-table-column align="center" label="明细项数" prop="itemCount" width="90"/>
        <el-table-column align="center" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button v-perm="'asset:cssd:edit'" link size="small" type="primary" @click="openEdit(row)">编辑
            </el-button>
            <el-button v-perm="'asset:cssd:edit'" link size="small" type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <CssdTemplateEdit v-model="editVisible" :method-dict="methodDict" :row="editRow" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 器械包模板（菜单 2932，sql/187）
 *
 * 原为 903「消毒供应 CSSD」的「器械包模板」页签，2026-09-29 拆出独立挂菜单。
 * 模板是**建档**（包编码/包名/默认灭菌方式/组成明细，一次性维护）；
 * 回收-清洗-打包-灭菌-发放是**每日流转动作**，岗位与频率不同，故拆开。
 * 回收登记的包名下拉照旧走 cssdTemplateSelectList（仅启用模板），数据流不断。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {cssdTemplateDelete, cssdTemplateListPage} from '@/api/cssd';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import CssdTemplateEdit from './CssdTemplateEdit.vue';
// ---------------- 字典 ----------------
const methodDict = ref([]);
const methodText = (v) => dictLabelText(methodDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(DICT_TYPE.CSSD_STERIL_METHOD);
    methodDict.value = res?.data?.[DICT_TYPE.CSSD_STERIL_METHOD] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', status: null, pageNum: 1, pageSize: 10});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await cssdTemplateListPage({
      keyword: query.keyword.trim() || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {keyword: '', status: null, pageNum: 1});
  loadList();
};
// ---------------- 新增/编辑 ----------------
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
const del = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除模板【${row.packName}】？历史追溯存的是包名快照，不受影响。`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await cssdTemplateDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadList();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
