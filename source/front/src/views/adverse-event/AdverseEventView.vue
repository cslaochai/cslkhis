<template>
  <div>
    <!-- 统计卡 -->
    <div class="grid grid-cols-4 gap-4 mb-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">本月上报</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.monthReported }} <span
            class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待处理</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }} <span
            class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">I 级警讯（本月）</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.sentinel }} <span
            class="text-sm font-normal text-gray-400">件</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已结案（本月上报）</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.closed }} <span
            class="text-sm font-normal text-gray-400">件</span></div>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="事件编号">
            <el-input
                v-model="query.eventNo"
                clearable
                data-testid="ae-no-filter"
                placeholder="事件编号"
                style="width: 170px"
                @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                clearable
                placeholder="摘要/经过/患者姓名"
                style="width: 200px"
                @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="事件类型">
            <el-select v-model="query.eventType" :fit-input-width="false" clearable placeholder="事件类型"
                       style="width: 130px">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="事件等级">
            <el-select v-model="query.eventLevel" :fit-input-width="false" clearable placeholder="事件等级"
                       style="width: 160px">
              <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable data-testid="ae-status-filter" placeholder="状态"
                       style="width: 140px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="发生科室">
            <el-select v-model="query.occurDeptId" :fit-input-width="false" clearable filterable placeholder="发生科室"
                       style="width: 160px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="上报日期">
            <el-date-picker
                v-model="query.dateRange"
                end-placeholder="上报日期止"
                range-separator="至"
                start-placeholder="上报日期起"
                style="width: 250px"
                type="daterange"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="ae-search-btn" type="primary" @click="query.pageNum = 1; loadList()">
              查询
            </el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'emr:adverseEvent:add'" :icon="WarningFilled" data-testid="ae-add-btn" type="warning"
                     @click="openCreate">上报事件
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="ae-table" stripe>
        <el-table-column label="事件编号" prop="eventNo" width="170"/>
        <el-table-column align="center" label="类型" width="110">
          <template #default="{ row }">{{ typeText(row.eventType) }}</template>
        </el-table-column>
        <el-table-column align="center" label="等级" width="160">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.eventLevel)">{{ levelText(row.eventLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发生科室" prop="occurDeptName" show-overflow-tooltip width="120"/>
        <el-table-column label="事件摘要" min-width="180" prop="title" show-overflow-tooltip/>
        <el-table-column label="患者" width="90">
          <template #default="{ row }">{{ row.patientName || '—' }}</template>
        </el-table-column>
        <el-table-column label="上报人" prop="reporterName" width="90"/>
        <el-table-column align="center" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上报时间" width="165">
          <template #default="{ row }">{{ (row.reportTime || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="215">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canAct(row, 'handle')" link type="warning" @click="openAction(row, 'handle')">处理
            </el-button>
            <el-button v-if="canAct(row, 'rectify')" link type="success" @click="openAction(row, 'rectify')">整改
            </el-button>
            <el-button v-if="canAct(row, 'close')" link type="primary" @click="openAction(row, 'close')">结案
            </el-button>
            <el-button v-if="canEdit(row)" v-perm="'emr:adverseEvent:edit'" link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button v-if="canDelete(row)" v-perm="'emr:adverseEvent:delete'" link type="danger" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '暂无不良事件记录'"/>
        </template>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 上报/修改弹窗（表单与提交已拆至 AdverseEventEdit.vue） -->
    <AdverseEventEdit v-model="dialogVisible" :acquired-dict="acquiredDict" :dept-options="deptOptions"
                      :level-dict="levelDict" :row="editRow" :type-dict="typeDict" @saved="onEditSaved"/>

    <!-- PDCA 流转弹窗 -->
    <el-dialog v-model="actionVisible" :title="`${actionLabel()} - ${actionRow?.eventNo || ''}`" data-testid="ae-action-dialog"
               width="520px">
      <div v-if="actionRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">摘要：</span>{{ actionRow.title }}</div>
      </div>
      <el-input
          v-model="actionRemark"
          :placeholder="actionPlaceholder()"
          :rows="4"
          data-testid="ae-action-remark"
          maxlength="500"
          type="textarea"
      />
      <template #footer>
        <el-button :loading="actionLoading" data-testid="ae-action-submit" type="primary" @click="submitAction">确定
        </el-button>
        <el-button @click="actionVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗（已拆至 AdverseEventDetail.vue，按 id 自取详情） -->
    <AdverseEventDetail v-model="detailVisible" :acquired-dict="acquiredDict" :id="detailId" :level-dict="levelDict"
                        :status-dict="statusDict" :type-dict="typeDict"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search, WarningFilled} from '@element-plus/icons-vue';
import {
  closeAdverseEvent,
  deleteAdverseEvent,
  getAdverseEventList,
  getAdverseEventStats,
  handleAdverseEvent,
  rectifyAdverseEvent,
} from '@/api/adverseEvent';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {ADVERSE_EVENT_NEXT_ACTION, levelTagType, statusTagType} from '@/lib/adverseEvent';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import AdverseEventEdit from './AdverseEventEdit.vue';
import AdverseEventDetail from './AdverseEventDetail.vue';

const loading = ref(true);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  eventNo: '',
  eventType: null,
  eventLevel: null,
  status: null,
  occurDeptId: null,
  keyword: '',
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({monthReported: 0, pending: 0, sentinel: 0, closed: 0});
const typeDict = ref([]);
const levelDict = ref([]);
const statusDict = ref([]);
const acquiredDict = ref([]);
const deptOptions = ref([]);
const typeText = (v) => dictLabelText(typeDict.value, v);
const levelText = (v) => dictLabelText(levelDict.value, v);
const statusText = (v) => dictLabelText(statusDict.value, v);
// 当前登录人（编辑/删除仅上报人本人；流转动作任何人可点，收口在服务端状态机）
const me = reactive({employeeId: null});
try {
  const cached = localStorage.getItem('user-info') || localStorage.getItem('userInfo');
  if (cached) {
    const u = JSON.parse(cached);
    me.employeeId = String(u.employeeId ?? u.employee_id ?? '');
  }
} catch { /* 忽略缓存解析失败，按钮显隐退化为服务端校验 */
}
const isMine = (row) => !!me.employeeId && String(row.reporterId) === String(me.employeeId);
const canEdit = (row) => Number(row.status) === 1 && isMine(row);
const canDelete = canEdit;
const canAct = (row, key) => ADVERSE_EVENT_NEXT_ACTION[Number(row.status)]?.key === key;
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.ADVERSE_EVENT_TYPE},${DICT_TYPE.ADVERSE_EVENT_LEVEL},${DICT_TYPE.ADVERSE_EVENT_STATUS},${DICT_TYPE.ADVERSE_ACQUIRED}`);
    typeDict.value = res?.data?.[DICT_TYPE.ADVERSE_EVENT_TYPE] || [];
    levelDict.value = res?.data?.[DICT_TYPE.ADVERSE_EVENT_LEVEL] || [];
    statusDict.value = res?.data?.[DICT_TYPE.ADVERSE_EVENT_STATUS] || [];
    acquiredDict.value = res?.data?.[DICT_TYPE.ADVERSE_ACQUIRED] || [];
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
const loadStats = async () => {
  try {
    const res = await getAdverseEventStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getAdverseEventList({
      eventNo: query.eventNo.trim() || undefined,
      eventType: query.eventType ?? undefined,
      eventLevel: query.eventLevel ?? undefined,
      status: query.status ?? undefined,
      occurDeptId: query.occurDeptId ?? undefined,
      keyword: query.keyword.trim() || undefined,
      dateStart: query.dateRange?.[0],
      dateEnd: query.dateRange?.[1],
      pageNum: query.pageNum,
      pageSize: query.pageSize,
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
  query.eventNo = '';
  query.eventType = null;
  query.eventLevel = null;
  query.status = null;
  query.occurDeptId = null;
  query.keyword = '';
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// ------------------------------------------------------------------
// 上报 / 修改弹窗（表单与提交已拆至 AdverseEventEdit.vue，这里只管开关与行快照）
// ------------------------------------------------------------------
const dialogVisible = ref(false);
const editRow = ref(null);
const onEditSaved = () => {
  loadList();
  loadStats();
};
const openCreate = () => {
  editRow.value = null;
  dialogVisible.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  dialogVisible.value = true;
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除事件 ${row.eventNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteAdverseEvent(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
// ------------------------------------------------------------------
// PDCA 流转弹窗（处理/整改/结案 共用）
// ------------------------------------------------------------------
const actionVisible = ref(false);
const actionKey = ref('handle');
const actionRow = ref(null);
const actionRemark = ref('');
const actionLoading = ref(false);
const openAction = (row, key) => {
  actionRow.value = row;
  actionKey.value = key;
  actionRemark.value = '';
  actionVisible.value = true;
};
const actionLabel = () => ADVERSE_EVENT_NEXT_ACTION[actionKey.value]?.label || '';
const actionPlaceholder = () => ADVERSE_EVENT_NEXT_ACTION[actionKey.value]?.placeholder || '';
const submitAction = async () => {
  if (!actionRemark.value.trim()) {
    ElMessage.warning('请填写意见内容');
    return;
  }
  actionLoading.value = true;
  try {
    const api = actionKey.value === 'handle' ? handleAdverseEvent
        : actionKey.value === 'rectify' ? rectifyAdverseEvent : closeAdverseEvent;
    const res = await api(actionRow.value.id, actionRemark.value.trim());
    if (res.code === 200) {
      ElMessage.success(`${actionLabel()}成功`);
      actionVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || `${actionLabel()}失败`);
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(`${actionLabel()}失败`);
  } finally {
    actionLoading.value = false;
  }
};
// ------------------------------------------------------------------
// 详情弹窗（已拆至 AdverseEventDetail.vue，组件按 id 自取详情）
// ------------------------------------------------------------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
onMounted(() => {
  loadDicts();
  loadDepts();
  loadStats();
  loadList();
});
</script>
