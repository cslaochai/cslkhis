<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="编码 / 名称" style="width: 200px"
                      @keyup.enter="loadList"/>
          </el-form-item>
          <el-form-item label="适用科室">
            <el-select v-model="query.deptId" clearable filterable placeholder="适用科室" style="width: 180px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.enabledOnly">仅启用</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:daySurgery:add'" :icon="Plus" type="primary" @click="openCreate">新增术式</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="术式编码" prop="itemCode" width="120"/>
        <el-table-column label="术式名称" min-width="200" prop="itemName" show-overflow-tooltip/>
        <el-table-column label="适用科室" prop="deptName" show-overflow-tooltip width="160"/>
        <el-table-column align="right" label="最长滞留(h)" width="110">
          <template #default="{ row }">{{ row.maxStayHours }}</template>
        </el-table-column>
        <el-table-column align="center" label="手术分级" width="90">
          <template #default="{ row }">{{ operationLevelMap[Number(row.operationLevel)] || '—' }}</template>
        </el-table-column>
        <el-table-column label="麻醉方式" width="120">
          <template #default="{ row }">{{ anesText(row.anesthesiaType) }}</template>
        </el-table-column>
        <el-table-column align="right" label="标准费用(元)" width="120">
          <template #default="{ row }">{{ row.standardFee ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">
              {{ Number(row.status) === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="160">
          <template #default="{ row }">
            <el-button v-perm="'ipd:daySurgery:add'" link size="small" type="primary"
                       @click.stop="openEdit(row)">编辑
            </el-button>
            <el-button v-perm="'ipd:daySurgery:add'" :type="Number(row.status) === 1 ? 'warning' : 'success'" link
                       size="small" @click.stop="toggle(row)">
              {{ Number(row.status) === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无准入术式"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 准入术式 -->
    <DaySurgeryItemEdit v-model="editVisible" :row="editingRow" :dept-options="deptOptions" :anes-dict="anesDict"
                        :operation-level-map="operationLevelMap" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 日间手术准入目录（菜单 2928，sql/183；原为 316 日间手术的第二个页签）
 *
 * 目录是日间手术的**准入闸门主数据**：
 * 1. 预约登记只能选启用中的目录术式（316 的预约下拉走 getDaySurgeryItemList，只含启用，
 *    后端也校验）——目录停用后不能再新预约，存量登记单不受影响。
 * 2. 手术分级决定术者授权校验：日间手术要求术者持有不低于目录登记的分级（sql/155）。
 * 3. 最长滞留(h)是超期判定的基准，超期由服务端算（overdue），前端不重算。
 *
 * 拆分口径：目录（主数据，随术式准入政策调整，低频）与登记台账（单据，逐患者经办，高频）
 * 主键维度不同、写接口不同（daySurgeryItemUpsert/updateDaySurgeryItemStatus ↔ daySurgery* 单据流转），
 * 拆开后各自独立菜单，数据流：本页改目录 → 316 预约下拉自动跟着变（同一份目录表）。
 *
 * 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {listDaySurgeryItemPage, updateDaySurgeryItemStatus} from '@/api/daySurgery';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import DaySurgeryItemEdit from './DaySurgeryItemEdit.vue';
// 手术分级（字典 his_operation_level）：日间手术按目录上登记的分级要求术者授权（sql/155）
const operationLevelMap = {1: '一级', 2: '二级', 3: '三级', 4: '四级'};
const anesDict = ref([]);
const anesText = (v) => dictLabelText(anesDict.value, v);
const deptOptions = ref([]);
// ---------------- 目录列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', deptId: null, enabledOnly: true, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listDaySurgeryItemPage({
      keyword: query.keyword.trim() || undefined,
      deptId: query.deptId || undefined,
      enabledOnly: query.enabledOnly || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.keyword = '';
  query.deptId = null;
  query.enabledOnly = true;
  query.pageNum = 1;
  loadList();
};
// ---------------- 新增 / 编辑 ----------------
const editVisible = ref(false);
const editingRow = ref(null);
const openCreate = () => {
  editingRow.value = null;
  editVisible.value = true;
};
const openEdit = (row) => {
  editingRow.value = row;
  editVisible.value = true;
};
const toggle = async (row) => {
  const next = Number(row.status) === 1 ? 0 : 1;
  try {
    await ElMessageBox.confirm(next === 0 ? `停用后该术式不可新预约（存量登记单不受影响），确认停用「${row.itemName}」？` : `确认启用「${row.itemName}」？`, '启停确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await updateDaySurgeryItemStatus(row.id, next);
    if (res.code === 200) {
      ElMessage.success(next === 1 ? '已启用' : '已停用');
      loadList();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
onMounted(async () => {
  try {
    const [deptRes, dictRes] = await Promise.all([
      getDepartmentSelectList({}),
      getDictDataMapList(DICT_TYPE.DAY_SURGERY_ANESTHESIA),
    ]);
    if (deptRes.code === 200)
      deptOptions.value = deptRes.data || [];
    anesDict.value = dictRes?.data?.[DICT_TYPE.DAY_SURGERY_ANESTHESIA] || [];
  } catch (e) {
    console.error('加载基础数据失败', e);
  }
  await loadList();
});
</script>
