<template>
  <div>
    <el-card class="stat-card mb-3" shadow="never">
      <div class="stat-row">
        <div class="stat-item"><span class="stat-label">远程会诊总数</span><span class="stat-value">{{
            stats.teleTotal
          }}</span></div>
        <div class="stat-item"><span class="stat-label">待安排</span><span class="stat-value warn">{{
            stats.telePending
          }}</span></div>
        <div class="stat-item"><span class="stat-label">已安排</span><span class="stat-value">{{
            stats.teleArranged
          }}</span></div>
        <div class="stat-item"><span class="stat-label">已完成</span><span class="stat-value ok">{{
            stats.teleDone
          }}</span></div>
        <div class="stat-item"><span class="stat-label">已取消</span><span class="stat-value">{{
            stats.teleCanceled
          }}</span></div>
      </div>
    </el-card>

    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="teleQuery" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="teleQuery.keyword" clearable placeholder="单号 / 患者 / 专家" style="width: 200px"
                      @keyup.enter="loadTele"/>
          </el-form-item>
          <el-form-item label="会诊类型">
            <el-select v-model="teleQuery.consultType" clearable placeholder="会诊类型" style="width: 140px">
              <el-option v-for="d in teleTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="teleQuery.status" clearable placeholder="状态" style="width: 130px">
              <el-option v-for="d in teleStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="teleQuery.urgentOnly">仅急会诊</el-checkbox>
            <el-checkbox v-model="teleQuery.openOnly">仅未完成</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadTele">查询</el-button>
            <el-button :icon="Refresh" @click="resetTele">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:teleconsult:add'" :icon="Plus" type="primary" @click="openTeleCreate">申请会诊
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="teleLoading" :data="teleRows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="会诊单号" prop="consultNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ teleTypeText(row.consultType) }}</template>
        </el-table-column>
        <el-table-column align="center" label="急" width="60">
          <template #default="{ row }">
            <el-tag v-if="Number(row.isUrgent) === 1" size="small" type="danger">急</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="申请科室" prop="applyDeptName" show-overflow-tooltip width="140"/>
        <el-table-column label="专家医院" prop="expertHospital" show-overflow-tooltip width="150"/>
        <el-table-column label="专家" prop="expertName" width="100"/>
        <el-table-column label="会诊目的" min-width="180" prop="purpose" show-overflow-tooltip/>
        <el-table-column label="计划时间" prop="planTime" width="160"/>
        <el-table-column label="接入号" prop="meetNo" width="120"/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="teleStatusTag(row.status)" size="small">{{ teleStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openTeleDetail(row)">详情
            </el-button>
            <el-button v-if="row.canArrange" v-perm="'ipd:teleconsult:edit'" link size="small" type="primary"
                       @click.stop="openArrange(row)">安排
            </el-button>
            <el-button v-if="row.canComplete" v-perm="'ipd:teleconsult:edit'" link size="small" type="success"
                       @click.stop="openTeleAct(row, 'complete')">出意见完成
            </el-button>
            <el-button v-if="row.canCancel" v-perm="'ipd:teleconsult:edit'" link size="small" type="info"
                       @click.stop="openTeleAct(row, 'cancel')">取消
            </el-button>
            <el-button v-if="row.canEdit" v-perm="'ipd:teleconsult:add'" link size="small" type="primary"
                       @click.stop="openTeleEdit(row)">编辑
            </el-button>
            <el-button v-if="row.canDelete" v-perm="'ipd:teleconsult:add'" link size="small" type="danger"
                       @click.stop="removeTele(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无远程会诊申请"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="teleQuery.pageNum" v-model:page-size="teleQuery.pageSize"
                       :page-sizes="PAGE_SIZES" :total="teleTotal" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadTele" @current-change="loadTele"/>
      </div>
    </el-card>

    <!-- 远程会诊申请 / 修改 -->
    <TeleConsultEdit v-model="teleFormVisible" :row="teleFormRow" :tele-type-dict="teleTypeDict"
                     @saved="reloadAfterChange"/>

    <!-- 安排 -->
    <TeleConsultArrangeEdit v-model="arrangeVisible" :row="arrangeRow" @saved="reloadAfterChange"/>

    <!-- 完成 / 取消 -->
    <TeleConsultActEdit v-model="teleActVisible" :row="teleActRow" :act-key="teleActKey" @saved="reloadAfterChange"/>

    <!-- 远程会诊详情 -->
    <TeleConsultDetail v-model="teleDetailVisible" :id="teleDetailId" :tele-type-dict="teleTypeDict"
                       :tele-status-dict="teleStatusDict" :status-tag="teleStatusTag"/>

  </div>
</template>

<script setup>
/**
 * 远程会诊（菜单 315，sql/184 由「互联网医院 / 远程会诊」更名）
 *
 * 独立的一条链：申请（待安排）→ 安排（已安排，定时间与接入方式）→ 出意见完成（已完成）/ 取消
 *
 * 线上问诊已拆为独立菜单 2929「线上问诊」（sql/184）：两者是两个实体、两套接口
 * （teleConsult* ↔ onlineConsult*）、两条互不依赖的状态机，统计仍同源 getTeleConsultStat。
 *
 * 四条口径：
 * 1. **按钮可用性一律读后端 can* 字段**，不按 status 码值 switch。
 * 2. **已安排才可出意见**是后端铁律，前端只在能点时给按钮，不在本地拦截
 *    （本地拦截会掩盖后端规则的失效）。
 * 3. 费用 fee 只是**价目快照**，这里不计费 —— 计费走 charge 域，避免双计。
 * 4. 平台 platform / 接入号 meet_no 是**预留字段**：真实视频平台对接时只换成"平台开号回写"，
 *    不改表结构、不改页面。
 * 5. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {deleteTeleConsult, getTeleConsultStat, listTeleConsultPage} from '@/api/teleconsult';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import TeleConsultEdit from './TeleConsultEdit.vue';
import TeleConsultArrangeEdit from './TeleConsultArrangeEdit.vue';
import TeleConsultActEdit from './TeleConsultActEdit.vue';
import TeleConsultDetail from './TeleConsultDetail.vue';

const stats = reactive({
  teleTotal: 0, telePending: 0, teleArranged: 0, teleDone: 0, teleCanceled: 0,
});
const teleTypeDict = ref([]);
const teleStatusDict = ref([]);
const teleTypeText = (v) => dictLabelText(teleTypeDict.value, v);
const teleStatusText = (v) => dictLabelText(teleStatusDict.value, v);
const teleStatusTag = (v) => {
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
// ---------------- 远程会诊 ----------------
const teleLoading = ref(false);
const teleRows = ref([]);
const teleTotal = ref(0);
const teleQuery = reactive({
  keyword: '', consultType: null, status: null,
  urgentOnly: false, openOnly: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadTele = async () => {
  teleLoading.value = true;
  try {
    const res = await listTeleConsultPage({
      keyword: teleQuery.keyword.trim() || undefined,
      consultType: teleQuery.consultType ?? undefined,
      status: teleQuery.status ?? undefined,
      urgentOnly: teleQuery.urgentOnly || undefined,
      openOnly: teleQuery.openOnly || undefined,
      pageNum: teleQuery.pageNum, pageSize: teleQuery.pageSize,
    });
    if (res.code === 200) {
      teleRows.value = res.data?.records || [];
      teleTotal.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    teleLoading.value = false;
  }
};
const resetTele = () => {
  teleQuery.keyword = '';
  teleQuery.consultType = null;
  teleQuery.status = null;
  teleQuery.urgentOnly = false;
  teleQuery.openOnly = false;
  teleQuery.pageNum = 1;
  loadTele();
};
// 申请 / 修改（表单与提交在 TeleConsultEdit，row 为空走新增、有值走修改）
const teleFormVisible = ref(false);
const teleFormRow = ref(null);
const openTeleCreate = () => {
  teleFormRow.value = null;
  teleFormVisible.value = true;
};
const openTeleEdit = (row) => {
  teleFormRow.value = row;
  teleFormVisible.value = true;
};
// 安排（表单与提交在 TeleConsultArrangeEdit）
const arrangeVisible = ref(false);
const arrangeRow = ref(null);
const openArrange = (row) => {
  arrangeRow.value = row;
  arrangeVisible.value = true;
};
// 完成 / 取消（共用内容弹窗在 TeleConsultActEdit，teleActKey 切标题与字段）
const teleActVisible = ref(false);
const teleActKey = ref('complete');
const teleActRow = ref(null);
const openTeleAct = (row, key) => {
  teleActRow.value = row;
  teleActKey.value = key;
  teleActVisible.value = true;
};
const removeTele = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除会诊单 ${row.consultNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteTeleConsult(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      reloadAfterChange();
    } 
  } catch (e) {
    console.error(e);
    
  }
};
// 详情（按 id 取数在 TeleConsultDetail）
const teleDetailVisible = ref(false);
const teleDetailId = ref(null);
const openTeleDetail = (row) => {
  teleDetailId.value = row.id;
  teleDetailVisible.value = true;
};
// ---------------- 公共 ----------------
// 任一弹框保存成功后：刷新列表与统计（原各提交分支里的 loadTele() + loadStats() 收口成一处）
const reloadAfterChange = () => {
  loadTele();
  loadStats();
};
const loadStats = async () => {
  try {
    const res = await getTeleConsultStat();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList([
      DICT_TYPE.TELE_CONSULT_TYPE, DICT_TYPE.TELE_CONSULT_STATUS,
    ].join(','));
    const d = res?.data || {};
    teleTypeDict.value = d[DICT_TYPE.TELE_CONSULT_TYPE] || [];
    teleStatusDict.value = d[DICT_TYPE.TELE_CONSULT_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
onMounted(async () => {
  await loadDicts();
  await loadTele();
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
  align-items: flex-end;
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

.stat-sep {
  width: 1px;
  height: 34px;
  background: #e4e7ed;
}
</style>
