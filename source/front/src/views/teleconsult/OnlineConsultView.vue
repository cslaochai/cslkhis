<template>
  <div>
    <el-card class="stat-card mb-3" shadow="never">
      <div class="stat-row">
        <div class="stat-item"><span class="stat-label">线上问诊总数</span><span class="stat-value">{{
            stats.onlineTotal
          }}</span></div>
        <div class="stat-item"><span class="stat-label">待接诊</span><span
            class="stat-value warn">{{ stats.onlineWaiting }}</span></div>
        <div class="stat-item"><span class="stat-label">接诊中</span><span class="stat-value">{{
            stats.onlineAccepted
          }}</span></div>
        <div class="stat-item"><span class="stat-label">已完成</span><span class="stat-value ok">{{
            stats.onlineDone
          }}</span></div>
        <div class="stat-item"><span class="stat-label">已退诊</span><span class="stat-value">{{
            stats.onlineRejected
          }}</span></div>
      </div>
    </el-card>

    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="单号 / 患者" style="width: 200px"
                      @keyup.enter="loadList"/>
          </el-form-item>
          <el-form-item label="问诊方式">
            <el-select v-model="query.consultType" clearable placeholder="问诊方式" style="width: 140px">
              <el-option v-for="d in onlineTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 130px">
              <el-option v-for="d in onlineStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="接诊科室">
            <el-select v-model="query.deptId" clearable filterable placeholder="接诊科室" style="width: 170px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.waitingOnly">仅待接诊</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:teleconsult:add'" :icon="Plus" type="primary" @click="openCreate">发起问诊</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="问诊单号" prop="consultNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="方式" width="100">
          <template #default="{ row }">{{ onlineTypeText(row.consultType) }}</template>
        </el-table-column>
        <el-table-column label="接诊科室" prop="deptName" show-overflow-tooltip width="140"/>
        <el-table-column label="接诊医生" prop="doctorName" width="100"/>
        <el-table-column label="主诉/问题描述" min-width="220" prop="chiefComplaint" show-overflow-tooltip/>
        <el-table-column label="发起时间" prop="applyTime" width="160"/>
        <el-table-column align="center" label="建议线下" width="90">
          <template #default="{ row }">
            <el-tag v-if="Number(row.needVisit) === 1" size="small" type="warning">是</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="onlineStatusTag(row.status)" size="small">{{ onlineStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openDetail(row)">详情
            </el-button>
            <el-button v-if="row.canAccept" v-perm="'ipd:teleconsult:edit'" link size="small" type="primary"
                       @click.stop="doAccept(row)">接诊
            </el-button>
            <el-button v-if="row.canReply" v-perm="'ipd:teleconsult:edit'" link size="small" type="success"
                       @click.stop="openReply(row)">回复并结束
            </el-button>
            <el-button v-if="row.canReject" v-perm="'ipd:teleconsult:edit'" link size="small" type="info"
                       @click.stop="openReject(row)">退诊
            </el-button>
            <el-button v-if="row.canDelete" v-perm="'ipd:teleconsult:add'" link size="small" type="danger"
                       @click.stop="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无线上问诊"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 发起问诊 -->
    <OnlineConsultAdd v-model="formVisible" :dept-options="deptOptions" :online-type-dict="onlineTypeDict"
                      @saved="reloadAfterChange"/>

    <!-- 回复 -->
    <OnlineConsultReplyEdit v-model="replyVisible" :row="replyRow" @saved="reloadAfterChange"/>

    <!-- 退诊 -->
    <OnlineConsultRejectEdit v-model="rejectVisible" :row="rejectRow" @saved="reloadAfterChange"/>

    <!-- 问诊详情 -->
    <OnlineConsultDetail v-model="detailVisible" :id="detailId" :online-type-dict="onlineTypeDict"
                         :online-status-dict="onlineStatusDict" :status-tag="onlineStatusTag"/>
  </div>
</template>

<script setup>
/**
 * 线上问诊（菜单 2929，sql/184；原为 315 互联网医院的第二个页签）
 *
 * 独立的一条链：发起（待接诊）→ 接诊（接诊中）→ 回复并结束（已完成）/ 退诊（已退诊）。
 *
 * 拆分口径：线上问诊（互联网医院面向患者的轻问诊单）与远程会诊（院际会诊单）是**两个实体、
 * 两套完全独立的接口**（onlineConsult* ↔ teleConsult*）、两条互不依赖的状态机，
 * 运营岗位也不同（线上问诊客服/互联网诊室 ↔ 远程会诊医务协调）。
 * 拆开后 315 更名「远程会诊」只留会诊链，本页独立挂菜单。
 *
 * 口径：
 * 1. **按钮可用性一律读后端 can* 字段**，不按 status 码值 switch；
 *    「待接诊才可接诊、接诊中才可回复」是后端铁律，前端只在能点时给按钮。
 * 2. 费用 fee 只是**价目快照**，这里不计费 —— 计费走 charge 域，避免双计。
 * 3. 统计数字仍取 getTeleConsultStat 的 online* 字段（同源接口，拆页不拆数据）。
 * 4. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  acceptOnlineConsult,
  deleteOnlineConsult,
  getTeleConsultStat,
  listOnlineConsultPage,
} from '@/api/teleconsult';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import OnlineConsultAdd from './OnlineConsultAdd.vue';
import OnlineConsultReplyEdit from './OnlineConsultReplyEdit.vue';
import OnlineConsultRejectEdit from './OnlineConsultRejectEdit.vue';
import OnlineConsultDetail from './OnlineConsultDetail.vue';

const stats = reactive({
  onlineTotal: 0, onlineWaiting: 0, onlineAccepted: 0, onlineDone: 0, onlineRejected: 0,
});
const onlineTypeDict = ref([]);
const onlineStatusDict = ref([]);
const deptOptions = ref([]);
const onlineTypeText = (v) => dictLabelText(onlineTypeDict.value, v);
const onlineStatusText = (v) => dictLabelText(onlineStatusDict.value, v);
const onlineStatusTag = (v) => {
  switch (Number(v)) {
    case 1:
      return 'warning';
    case 2:
      return 'primary';
    case 3:
      return 'success';
    case 4:
      return 'info';
    default:
      return 'info';
  }
};
// ---------------- 问诊列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '', consultType: null, status: null,
  deptId: null, waitingOnly: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listOnlineConsultPage({
      keyword: query.keyword.trim() || undefined,
      consultType: query.consultType ?? undefined,
      status: query.status ?? undefined,
      deptId: query.deptId || undefined,
      waitingOnly: query.waitingOnly || undefined,
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
const resetQuery = () => {
  query.keyword = '';
  query.consultType = null;
  query.status = null;
  query.deptId = null;
  query.waitingOnly = false;
  query.pageNum = 1;
  loadList();
};
// ---------------- 发起问诊（表单与提交在 OnlineConsultAdd） ----------------
const formVisible = ref(false);
const openCreate = () => {
  formVisible.value = true;
};
const doAccept = async (row) => {
  try {
    const res = await acceptOnlineConsult(row.id);
    if (res.code === 200) {
      ElMessage.success('已接诊');
      loadList();
      loadStats();
    } 
  } catch (e) {
    console.error(e);
    
  }
};
// ---------------- 回复并结束（表单与提交在 OnlineConsultReplyEdit） ----------------
const replyVisible = ref(false);
const replyRow = ref(null);
const openReply = (row) => {
  replyRow.value = row;
  replyVisible.value = true;
};
// ---------------- 退诊（原因与提交在 OnlineConsultRejectEdit） ----------------
const rejectVisible = ref(false);
const rejectRow = ref(null);
const openReject = (row) => {
  rejectRow.value = row;
  rejectVisible.value = true;
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除问诊单 ${row.consultNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteOnlineConsult(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadList();
      loadStats();
    } 
  } catch (e) {
    console.error(e);
    
  }
};
// ---------------- 详情（按 id 取数在 OnlineConsultDetail） ----------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
// ---------------- 公共 ----------------
// 任一弹框保存成功后：刷新列表与统计（原各提交分支里的 loadList() + loadStats() 收口成一处）
const reloadAfterChange = () => {
  loadList();
  loadStats();
};
const loadStats = async () => {
  try {
    const res = await getTeleConsultStat();
    if (res.code === 200 && res.data) {
      stats.onlineTotal = res.data.onlineTotal ?? 0;
      stats.onlineWaiting = res.data.onlineWaiting ?? 0;
      stats.onlineAccepted = res.data.onlineAccepted ?? 0;
      stats.onlineDone = res.data.onlineDone ?? 0;
      stats.onlineRejected = res.data.onlineRejected ?? 0;
    }
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList([
      DICT_TYPE.ONLINE_CONSULT_TYPE, DICT_TYPE.ONLINE_CONSULT_STATUS,
    ].join(','));
    const d = res?.data || {};
    onlineTypeDict.value = d[DICT_TYPE.ONLINE_CONSULT_TYPE] || [];
    onlineStatusDict.value = d[DICT_TYPE.ONLINE_CONSULT_STATUS] || [];
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
  await Promise.all([loadDicts(), loadDepts()]);
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

.stat-value.ok {
  color: #67c23a;
}
</style>
