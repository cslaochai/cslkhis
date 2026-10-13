<template>
  <div>
    <!-- 器械包模板页签已拆为独立菜单 2932（sql/187）；本页只剩追溯链，拆掉 el-tabs 外壳 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="条码/包名/科室" style="width: 200px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'asset:cssd:edit'" data-testid="cssd-receive-btn" plain type="primary"
                     @click="openReceive">器械回收登记
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="cssd-table" stripe>
        <el-table-column label="器械包条码" prop="packNo" width="180"/>
        <el-table-column label="器械包名称" min-width="130" prop="packName" show-overflow-tooltip/>
        <el-table-column label="申领科室" prop="deptName" width="110">
          <template #default="{ row }">{{ row.deptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="灭菌方式" width="100">
          <template #default="{ row }">{{ methodText(row.sterilizeMethod) }}</template>
        </el-table-column>
        <el-table-column label="灭菌锅次" prop="sterilizerNo" width="100">
          <template #default="{ row }">{{ row.sterilizerNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" width="100">
          <template #default="{ row }">{{ row.batchNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="最近流转" width="160">
          <template #default="{ row }">{{ fmtTime(row.lastNodeTime) }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="90">
          <template #default="{ row }">
            <el-tag :type="cssdStatusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status < 6" v-perm="'asset:cssd:edit'" link size="small" type="primary"
                       @click="openAdvance(row)">流转
            </el-button>
            <el-button link size="small" @click="openDetail(row)">追溯链</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 回收登记 -->
    <CssdReceiveAdd v-model="receiveVisible" :method-dict="methodDict" @saved="loadList"/>

    <!-- 流转 -->
    <CssdAdvanceEdit v-model="advanceVisible" :row="advanceRow" @saved="loadList"/>

    <!-- 模板编辑弹窗已随 2932 拆分移除（sql/187） -->

    <!-- 追溯链 -->
    <CssdTraceDetail v-model="detailVisible" :id="detailId" :method-dict="methodDict" :status-dict="statusDict"/>
  </div>
</template>

<script setup>
/**
 * 消毒供应 CSSD（G22，菜单 903）
 *
 * 追溯链：回收登记（建包）→ 清洗 → 打包 → 灭菌（必填锅次/批次）→ 储存（不合格自动退回清洗）→ 发放。
 * 追溯节点只增不改；详情弹窗展示全链路时间线。
 * 器械包模板目录已拆为独立菜单 2932「器械包模板」（sql/187）；
 * 回收登记的包名下拉照旧走 cssdTemplateSelectList（仅启用模板），选包带出默认灭菌方式与组成清单。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {cssdListPage} from '@/api/cssd';
import CssdReceiveAdd from './CssdReceiveAdd.vue';
import CssdAdvanceEdit from './CssdAdvanceEdit.vue';
import CssdTraceDetail from './CssdTraceDetail.vue';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {cssdStatusTag, fmtTime} from '@/lib/cssd';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 字典 ----------------
const statusDict = ref([]);
const methodDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const methodText = (v) => dictLabelText(methodDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.CSSD_PACK_STATUS},${DICT_TYPE.CSSD_STERIL_METHOD}`);
    statusDict.value = res?.data?.[DICT_TYPE.CSSD_PACK_STATUS] || [];
    methodDict.value = res?.data?.[DICT_TYPE.CSSD_STERIL_METHOD] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 追溯列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await cssdListPage({
      keyword: query.keyword.trim() || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {keyword: '', status: null, pageNum: 1});
  loadList();
};
// ---------------- 回收登记 ----------------
const receiveVisible = ref(false);
const openReceive = () => {
  receiveVisible.value = true;
};
// ---------------- 流转 ----------------
const advanceVisible = ref(false);
const advanceRow = ref(null);
const openAdvance = (row) => {
  advanceRow.value = row;
  advanceVisible.value = true;
};
// ---------------- 详情（追溯链） ----------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
// 器械包模板维护页签已拆为独立菜单 2932（sql/187）；回收登记选包带出默认灭菌方式与组成清单见 CssdReceiveAdd。
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
