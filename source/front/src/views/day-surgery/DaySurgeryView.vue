<template>
  <div>
    <el-card class="stat-card mb-3" shadow="never">
      <div class="stat-row">
        <div class="stat-item"><span class="stat-label">登记总数</span><span class="stat-value">{{ stats.total }}</span>
        </div>
        <div class="stat-item"><span class="stat-label">术后观察中</span><span
            class="stat-value warn">{{ stats.observingCount }}</span></div>
        <div class="stat-item"><span class="stat-label">滞留超期</span><span
            class="stat-value danger">{{ stats.overdueCount }}</span></div>
        <div class="stat-item"><span class="stat-label">已出院</span><span
            class="stat-value ok">{{ stats.dischargedCount }}</span></div>
        <div class="stat-item"><span class="stat-label">已转住院</span><span
            class="stat-value">{{ stats.transferredCount }}</span></div>
        <div class="stat-item"><span class="stat-label">应随访未随访</span><span
            class="stat-value danger">{{ stats.followOverdueCount }}</span></div>
        <div class="stat-item"><span class="stat-label">非计划再入院</span><span
            class="stat-value">{{ stats.readmitCount }}</span></div>
        <div class="stat-item"><span class="stat-label">按时离院率(%)</span><span
            class="stat-value">{{ stats.onTimeLeaveRate }}</span></div>
      </div>
    </el-card>

    <!-- 登记台账（「准入目录」页签已拆为独立菜单 2928「日间手术准入目录」，sql/183） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="单号 / 患者 / 术式" style="width: 200px"
                      @keyup.enter="loadList"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="准入术式">
            <el-select v-model="query.itemId" clearable filterable placeholder="准入术式" style="width: 200px">
              <el-option v-for="d in itemOptions" :key="d.id" :label="`${d.itemCode} ${d.itemName}`" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="手术科室">
            <el-select v-model="query.deptId" clearable filterable placeholder="手术科室" style="width: 170px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="计划日期">
            <el-date-picker v-model="query.dateRange" end-placeholder="计划截止" start-placeholder="计划起始"
                            style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.openOnly">仅在院</el-checkbox>
            <el-checkbox v-model="query.overdueOnly">仅超期</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:daySurgery:add'" :icon="Plus" type="primary" @click="openApplyCreate">预约登记
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="登记单号" prop="applyNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="术式" min-width="180" prop="itemName" show-overflow-tooltip/>
        <el-table-column align="right" label="最长滞留(h)" width="100">
          <template #default="{ row }">{{ row.maxStayHours ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="计划手术日" prop="planSurgeryDate" width="120"/>
        <el-table-column label="术前评估" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.evalResult === 1" size="small" type="success">通过</el-tag>
            <el-tag v-else-if="row.evalResult === 2" size="small" type="danger">不通过</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="滞留超期" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.overdue" size="small" type="danger">超期</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="随访" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.followOverdue" size="small" type="danger">已过时限未访</el-tag>
            <span v-else-if="row.followCount">{{ row.followCount }} 次</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="手术间" prop="operatingRoom" width="100"/>
        <el-table-column label="主刀" prop="surgeon" width="100"/>
        <el-table-column fixed="right" label="操作" width="420">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openDetail(row)">详情
            </el-button>
            <el-button v-if="row.canEvaluate" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="openEval(row)">术前评估
            </el-button>
            <el-button v-if="row.canArrange" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="openArrange(row)">安排
            </el-button>
            <el-button v-if="row.canFinish" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="doFinish(row)">完成手术
            </el-button>
            <el-button v-if="row.canDischarge" v-perm="'ipd:daySurgery:edit'" link size="small" type="success"
                       @click.stop="openDischarge(row)">离院
            </el-button>
            <el-button v-if="row.canTransfer" v-perm="'ipd:daySurgery:edit'" link size="small" type="warning"
                       @click.stop="openTransfer(row)">转住院
            </el-button>
            <el-button v-if="row.canFollow" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="openFollow(row)">随访
            </el-button>
            <el-button v-if="row.canCancel" v-perm="'ipd:daySurgery:edit'" link size="small" type="info"
                       @click.stop="openCancel(row)">取消
            </el-button>
            <el-button v-if="row.canEdit" v-perm="'ipd:daySurgery:add'" link size="small" type="primary"
                       @click.stop="openApplyEdit(row)">编辑
            </el-button>
            <el-button v-if="row.canDelete" v-perm="'ipd:daySurgery:add'" link size="small" type="danger"
                       @click.stop="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无日间手术登记"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 预约登记 -->
    <DaySurgeryApplyEdit v-model="applyVisible" :row="applyRow" :item-options="itemOptions"
                         :dept-options="deptOptions" @saved="handleSaved"/>

    <!-- 术前评估 -->
    <DaySurgeryAssess v-model="evalVisible" :row="evalRow" :eval-dict="evalDict" @saved="handleSaved"/>

    <!-- 安排 -->
    <DaySurgeryArrange v-model="arrangeVisible" :row="arrangeRow" :anes-dict="anesDict" @saved="handleSaved"/>

    <!-- 离院 -->
    <DaySurgeryDischarge v-model="disVisible" :row="disRow" @saved="handleSaved"/>

    <!-- 转住院 -->
    <DaySurgeryTransfer v-model="trVisible" :row="trRow" @saved="handleSaved"/>

    <!-- 随访 -->
    <DaySurgeryFollow v-model="foVisible" :row="foRow" :follow-type-dict="followTypeDict"
                      :follow-result-dict="followResultDict" @saved="handleSaved"/>

    <!-- 取消 -->
    <DaySurgeryCancel v-model="cancelVisible" :row="cancelRow" @saved="handleSaved"/>

    <!-- 详情 -->
    <DaySurgeryDetail v-model="detailVisible" :id="detailId" :status-dict="statusDict" :anes-dict="anesDict"
                      :leave-dict="leaveDict" :follow-type-dict="followTypeDict"
                      :follow-result-dict="followResultDict" :status-tag="statusTag"/>
  </div>
</template>

<script setup>
/**
 * 日间手术（菜单 316）
 *
 * 闭环：预约登记（待评估）→ 术前评估（评估通过）→ 手术安排（已安排）
 *      → 完成手术（术后观察）→ 出院 / 转住院（终态）→ 24h 随访
 *
 * 准入目录已拆为独立菜单 2928「日间手术准入目录」（sql/183）：
 * 本页预约下拉走 getDaySurgeryItemList（仅启用术式）——目录停用即不能再新预约，数据流不断。
 *
 * 五条口径：
 * 1. **按钮可用性一律读后端 can* 字段**，不按 status 码值 switch。
 * 2. **准入是闸门**：预约只能选启用中的目录术式（下拉只给启用的，后端也校验）。
 * 3. **评估未通过不得安排、未安排不得登记完成** —— 评审必查的两道硬闸门，收在服务端。
 * 4. **超期 / 随访时限是后端算的**（overdue / followDue / followOverdue），
 *    前端不重算时间差 —— 两套算法必然对不上。
 * 5. 转住院必须填住院号：那是医保与病案口径的分界点，没有它无法证明"住院从哪天算起"。
 * 6. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  deleteDaySurgery,
  finishDaySurgery,
  getDaySurgeryItemList,
  getDaySurgeryStat,
  listDaySurgeryPage,
} from '@/api/daySurgery';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import DaySurgeryApplyEdit from './DaySurgeryApplyEdit.vue';
import DaySurgeryAssess from './DaySurgeryAssess.vue';
import DaySurgeryArrange from './DaySurgeryArrange.vue';
import DaySurgeryDischarge from './DaySurgeryDischarge.vue';
import DaySurgeryTransfer from './DaySurgeryTransfer.vue';
import DaySurgeryFollow from './DaySurgeryFollow.vue';
import DaySurgeryCancel from './DaySurgeryCancel.vue';
import DaySurgeryDetail from './DaySurgeryDetail.vue';

const stats = reactive({
  total: 0, observingCount: 0, dischargedCount: 0, canceledCount: 0, transferredCount: 0,
  overdueCount: 0, followOverdueCount: 0, readmitCount: 0, onTimeLeaveRate: '0',
});
const statusDict = ref([]);
const anesDict = ref([]);
const leaveDict = ref([]);
const followResultDict = ref([]);
const followTypeDict = ref([]);
const evalDict = ref([]);
const deptOptions = ref([]);
const itemOptions = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const statusTag = (v) => {
  switch (Number(v)) {
    case 1:
      return 'warning';
    case 2:
      return 'primary';
    case 3:
      return 'primary';
    case 4:
      return 'danger';
    case 5:
      return 'success';
    case 6:
      return 'info';
    case 7:
      return 'warning';
    default:
      return 'info';
  }
};
// ---------------- 登记单 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '', status: null, itemId: null, deptId: null,
  openOnly: false, overdueOnly: false, dateRange: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listDaySurgeryPage({
      keyword: query.keyword.trim() || undefined,
      status: query.status ?? undefined,
      itemId: query.itemId || undefined,
      deptId: query.deptId || undefined,
      openOnly: query.openOnly || undefined,
      overdueOnly: query.overdueOnly || undefined,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1],
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
  query.status = null;
  query.itemId = null;
  query.deptId = null;
  query.openOnly = false;
  query.overdueOnly = false;
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// 预约
const applyVisible = ref(false);
const applyRow = ref(null);
const openApplyCreate = () => {
  applyRow.value = null;
  applyVisible.value = true;
};
const openApplyEdit = (row) => {
  applyRow.value = row;
  applyVisible.value = true;
};
// 术前评估
const evalVisible = ref(false);
const evalRow = ref(null);
const openEval = (row) => {
  evalRow.value = row;
  evalVisible.value = true;
};
// 安排
const arrangeVisible = ref(false);
const arrangeRow = ref(null);
const openArrange = (row) => {
  arrangeRow.value = row;
  arrangeVisible.value = true;
};
// 完成手术
const doFinish = async (row) => {
  try {
    const res = await finishDaySurgery({id: row.id});
    if (res.code === 200) {
      ElMessage.success('手术已完成，进入术后观察');
      loadList();
      loadStats();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
// 出院
const disVisible = ref(false);
const disRow = ref(null);
const openDischarge = (row) => {
  disRow.value = row;
  disVisible.value = true;
};
// 转住院
const trVisible = ref(false);
const trRow = ref(null);
const openTransfer = (row) => {
  trRow.value = row;
  trVisible.value = true;
};
// 随访
const foVisible = ref(false);
const foRow = ref(null);
const openFollow = (row) => {
  foRow.value = row;
  foVisible.value = true;
};
// 取消
const cancelVisible = ref(false);
const cancelRow = ref(null);
const openCancel = (row) => {
  cancelRow.value = row;
  cancelVisible.value = true;
};
const handleSaved = () => {
  loadList();
  loadStats();
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除登记单 ${row.applyNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteDaySurgery(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadList();
      loadStats();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
// 详情
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
// ---------------- 公共 ----------------
const loadStats = async () => {
  try {
    const res = await getDaySurgeryStat();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadItemOptions = async () => {
  try {
    const res = await getDaySurgeryItemList();
    if (res.code === 200)
      itemOptions.value = res.data || [];
  } catch (e) {
    console.error('加载术式下拉失败', e);
  }
};
const loadDicts = async () => {
  try {
    // 6 张字典超过单次 5 个 type 上限 → 分两批
    const a = await getDictDataMapList([
      DICT_TYPE.DAY_SURGERY_STATUS, DICT_TYPE.DAY_SURGERY_ANESTHESIA,
      DICT_TYPE.DAY_SURGERY_LEAVE_TYPE, DICT_TYPE.DAY_SURGERY_EVAL_RESULT,
      DICT_TYPE.DAY_SURGERY_FOLLOW_RESULT,
    ].join(','));
    const b = await getDictDataMapList(DICT_TYPE.DAY_SURGERY_FOLLOW_TYPE);
    const da = a?.data || {};
    const db = b?.data || {};
    statusDict.value = da[DICT_TYPE.DAY_SURGERY_STATUS] || [];
    anesDict.value = da[DICT_TYPE.DAY_SURGERY_ANESTHESIA] || [];
    leaveDict.value = da[DICT_TYPE.DAY_SURGERY_LEAVE_TYPE] || [];
    evalDict.value = da[DICT_TYPE.DAY_SURGERY_EVAL_RESULT] || [];
    followResultDict.value = da[DICT_TYPE.DAY_SURGERY_FOLLOW_RESULT] || [];
    followTypeDict.value = db[DICT_TYPE.DAY_SURGERY_FOLLOW_TYPE] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts(), loadItemOptions()]);
  await loadList();
  await loadStats();
});
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.stat-row {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-value.warn {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-value.ok {
  color: #67c23a;
}

.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
