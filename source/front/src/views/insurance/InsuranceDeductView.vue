<template>
  <div class="p-4 space-y-4" data-testid="yb-deduct-page">
    <!-- 台账汇总：数字全部来自后端 SQL 聚合 -->
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-pending" shadow="never">
          <div class="text-xs text-slate-500">待确认</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.pendingConfirmCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-appealing" shadow="never">
          <div class="text-xs text-slate-500">申诉中</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.appealingCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-waitpay" shadow="never">
          <div class="text-xs text-slate-500">维持扣款待缴</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.waitPayCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-paid" shadow="never">
          <div class="text-xs text-slate-500">已缴回</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.paidCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-overdue" shadow="never">
          <div class="text-xs text-slate-500">超期未结</div>
          <div :class="(summary.overdueCount ?? 0) > 0 ? 'text-red-600' : ''" class="text-2xl font-semibold mt-1">
            {{ summary.overdueCount ?? 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-amount" shadow="never">
          <div class="text-xs text-slate-500">未结案金额</div>
          <div class="text-2xl font-semibold mt-1 text-[#1269B5]">{{ money(summary.openAmountSum) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="!rounded-lg" shadow="never">
      <el-tabs v-model="activeTab" data-testid="deduct-tabs">
        <!-- ==================== 扣款通知 ==================== -->
        <el-tab-pane label="扣款通知台账" name="notice">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="noticeQuery.deductStatus" clearable data-testid="deduct-filter-status" placeholder="全部状态"
                       style="width: 150px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_STATUS)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.sourceType" clearable placeholder="全部来源" style="width: 160px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_SOURCE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.violationType" clearable placeholder="全部违规类型" style="width: 170px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_VIOLATION_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.inspectionId" clearable data-testid="deduct-filter-inspection" placeholder="不限飞检批次"
                       style="width: 190px">
              <el-option v-for="i in inspectionOptions" :key="i.id" :label="`${i.inspectNo} ${i.fundOrg}`"
                         :value="String(i.id)"/>
            </el-select>
            <el-checkbox v-model="noticeQuery.onlyOverdue" data-testid="deduct-only-overdue">只看超期</el-checkbox>
            <el-input v-model="noticeQuery.keyword" clearable data-testid="deduct-keyword" placeholder="单号/患者/科室/违规描述"
                      style="width: 220px"
                      @keyup.enter="noticePage.pageNum = 1; loadNotices()"/>
            <el-button :icon="Search" data-testid="deduct-search-btn" type="primary"
                       @click="noticePage.pageNum = 1; loadNotices()">查询
            </el-button>
            <el-button :icon="Refresh" @click="resetNoticeQuery">重置</el-button>
            <div class="flex-1"></div>
            <el-button v-perm="'finance:insuranceDeduct:add'" :icon="Plus" data-testid="deduct-create-btn"
                       type="primary" @click="openNoticeCreate">录入扣款通知
            </el-button>
          </div>

          <el-table v-loading="noticeLoading" :data="noticeRows" data-testid="deduct-table" size="default"
                    style="width: 100%" @row-click="openNoticeDetail">
            <el-table-column class-name="font-mono text-xs" label="扣款单号" prop="deductNo" width="150"/>
            <el-table-column label="来源" show-overflow-tooltip width="140">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_DEDUCT_SOURCE, row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="患者" prop="patientName" width="90">
              <template #default="{row}">{{ row.patientName || '—' }}</template>
            </el-table-column>
            <el-table-column label="被审科室" prop="deptName" show-overflow-tooltip width="110">
              <template #default="{row}">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="违规类型" show-overflow-tooltip width="140">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_VIOLATION_TYPE, row.violationType) }}</template>
            </el-table-column>
            <el-table-column label="违规事实" min-width="220" prop="violationDesc" show-overflow-tooltip/>
            <el-table-column align="right" label="扣款金额" width="110">
              <template #default="{row}">{{ money(row.deductAmount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{row}">
                <el-tag :data-testid="`deduct-status-${row.deductNo}`" :type="statusTagType(row.deductStatus)"
                        size="small">
                  {{ dictText(DICT_TYPE.YB_DEDUCT_STATUS, row.deductStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处理期限" width="150">
              <template #default="{row}">
                <div class="text-xs">{{ row.handleDeadline }}</div>
                <el-tag v-if="row.overdue" data-testid="deduct-overdue-tag" size="small" type="danger">超期
                  {{ Math.abs(row.deadlineDays) }} 天
                </el-tag>
                <span v-else-if="row.deadlineDays != null" class="text-xs text-slate-400">剩 {{
                    row.deadlineDays
                  }} 天</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="250">
              <template #default="{row}">
                <el-button v-if="canAppeal(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-appeal-${row.deductNo}`" plain
                           size="small"
                           type="primary" @click.stop="openAppeal(row)">申诉
                </el-button>
                <el-button v-if="canAppealResult(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-result-${row.deductNo}`" plain
                           size="small"
                           type="primary" @click.stop="openAppealResult(row)">录结果
                </el-button>
                <el-button v-if="canConfirm(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-confirm-${row.deductNo}`" plain
                           size="small"
                           type="warning" @click.stop="openConfirm(row)">确认追责
                </el-button>
                <el-button v-if="canPayback(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-payback-${row.deductNo}`" :icon="Money" plain
                           size="small"
                           type="success" @click.stop="openPayback(row)">缴回
                </el-button>
                <el-button v-if="canEditNotice(row)" v-perm="'finance:insuranceDeduct:add'" :data-testid="`deduct-edit-${row.deductNo}`" :icon="Edit"
                           plain
                           size="small" @click.stop="openNoticeEdit(row)">编辑
                </el-button>
                <el-button v-if="canCancelNotice(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-cancel-${row.deductNo}`" :icon="Delete" plain
                           size="small"
                           type="danger" @click.stop="cancelDeductRow(row)">作废
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-end mt-3">
            <el-pagination v-model:current-page="noticePage.pageNum" v-model:page-size="noticePage.pageSize"
                           :page-sizes="PAGE_SIZES" :total="noticePage.total"
                           data-testid="deduct-pagination"
                           layout="total, sizes, prev, pager, next, jumper" @current-change="loadNotices"
                           @size-change="noticePage.pageNum = 1; loadNotices()"/>
          </div>
        </el-tab-pane>

        <!-- ==================== 飞检批次 ==================== -->
        <el-tab-pane label="飞检/专项审核批次" name="inspection">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="inspectQuery.inspectType" clearable placeholder="全部检查类型" style="width: 170px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="inspectQuery.status" clearable placeholder="全部状态" style="width: 140px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_STATUS)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-input v-model="inspectQuery.keyword" clearable data-testid="inspect-keyword" placeholder="批次号/医保局/检查组"
                      style="width: 220px"
                      @keyup.enter="inspectPage.pageNum = 1; loadInspections()"/>
            <el-button :icon="Search" data-testid="inspect-search-btn" type="primary"
                       @click="inspectPage.pageNum = 1; loadInspections()">查询
            </el-button>
            <el-button :icon="Refresh" @click="resetInspectQuery">重置</el-button>
            <div class="flex-1"></div>
            <el-button v-perm="'finance:insuranceDeduct:add'" :icon="Plus" data-testid="inspect-create-btn"
                       type="primary" @click="openInspectCreate">新建批次
            </el-button>
          </div>

          <el-table v-loading="inspectLoading" :data="inspectRows" data-testid="inspect-table" size="default"
                    style="width: 100%">
            <el-table-column class-name="font-mono text-xs" label="批次号" prop="inspectNo" width="150"/>
            <el-table-column label="检查类型" width="130">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_INSPECT_TYPE, row.inspectType) }}</template>
            </el-table-column>
            <el-table-column label="统筹区/医保局" min-width="150" prop="fundOrg" show-overflow-tooltip/>
            <el-table-column label="审核期间" width="200">
              <template #default="{row}">{{ row.inspectStartDate }} ~ {{ row.inspectEndDate }}</template>
            </el-table-column>
            <el-table-column label="进驻/通知日期" prop="inspectDate" width="120"/>
            <el-table-column label="检查组" min-width="150" prop="inspectTeam" show-overflow-tooltip>
              <template #default="{row}">{{ row.inspectTeam || '—' }}</template>
            </el-table-column>
            <el-table-column label="院内接待" prop="ourReceiver" width="100">
              <template #default="{row}">{{ row.ourReceiver || '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="名下扣款" width="150">
              <template #default="{row}">
                <span class="text-xs">{{ row.deductCount }} 张 / </span>
                <span class="font-medium">{{ money(row.deductAmountSum) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="inspectTagType(row.status)" size="small">
                  {{ dictText(DICT_TYPE.YB_INSPECT_STATUS, row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="结论" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.conclusion || '—' }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="280">
              <template #default="{row}">
                <el-button :data-testid="`inspect-drill-${row.inspectNo}`" :icon="View" plain size="small"
                           @click="drillNotices(row)">看扣款单
                </el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" v-perm="'finance:insuranceDeduct:add'" :data-testid="`inspect-edit-${row.inspectNo}`" plain
                           size="small"
                           type="primary" @click="openInspectEdit(row)">编辑
                </el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`inspect-conclude-${row.inspectNo}`" plain
                           size="small"
                           type="success" @click="openConclude(row)">结项
                </el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`inspect-cancel-${row.inspectNo}`" :icon="Delete" plain
                           size="small"
                           type="danger" @click="cancelInspectRow(row)">作废
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-end mt-3">
            <el-pagination v-model:current-page="inspectPage.pageNum" v-model:page-size="inspectPage.pageSize"
                           :page-sizes="PAGE_SIZES" :total="inspectPage.total"
                           data-testid="inspect-pagination"
                           layout="total, sizes, prev, pager, next, jumper" @current-change="loadInspections"
                           @size-change="inspectPage.pageNum = 1; loadInspections()"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ==================== 通知单 新建/编辑 ==================== -->
    <InsuranceDeductNoticeEdit v-model="noticeEditVisible" :dicts="dicts" :dept-options="deptOptions"
                               :inspection-options="inspectionOptions" :row="noticeEditRow"
                               @saved="reloadNoticeData"/>

    <!-- ==================== 通知单详情（只读 + 留痕） ==================== -->
    <InsuranceDeductNoticeDetail v-model="noticeDetailVisible" :dicts="dicts" :id="noticeDetailId"
                                 :money="money" :status-tag-type="statusTagType"/>

    <!-- ==================== 发起申诉 ==================== -->
    <InsuranceDeductAppealEdit v-model="appealVisible" :row="appealRow" @saved="reloadNoticeData"/>

    <!-- ==================== 录入申诉结果 ==================== -->
    <InsuranceDeductAppealResultEdit v-model="resultVisible" :row="resultRow" @saved="reloadNoticeData"/>

    <!-- ==================== 确认扣款并追责 ==================== -->
    <InsuranceDeductConfirmEdit v-model="confirmVisible" :dicts="dicts" :dept-options="deptOptions"
                                :money="money" :row="confirmRow" @saved="reloadNoticeData"/>

    <!-- ==================== 录入缴回 ==================== -->
    <InsuranceDeductPaybackEdit v-model="paybackVisible" :money="money" :row="paybackRow" @saved="reloadNoticeData"/>

    <!-- ==================== 批次 新建/编辑 ==================== -->
    <InsuranceDeductInspectEdit v-model="inspectEditVisible" :dicts="dicts" :row="inspectEditRow"
                                @saved="reloadInspectData"/>

    <!-- ==================== 批次结项 ==================== -->
    <InsuranceDeductInspectConclude v-model="concludeVisible" :row="concludeRow" @saved="reloadInspectData"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Money, Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {getDepartmentSelectList} from '@/api/system';
import InsuranceDeductAppealEdit from './InsuranceDeductAppealEdit.vue';
import InsuranceDeductAppealResultEdit from './InsuranceDeductAppealResultEdit.vue';
import InsuranceDeductConfirmEdit from './InsuranceDeductConfirmEdit.vue';
import InsuranceDeductInspectConclude from './InsuranceDeductInspectConclude.vue';
import InsuranceDeductInspectEdit from './InsuranceDeductInspectEdit.vue';
import InsuranceDeductNoticeDetail from './InsuranceDeductNoticeDetail.vue';
import InsuranceDeductNoticeEdit from './InsuranceDeductNoticeEdit.vue';
import InsuranceDeductPaybackEdit from './InsuranceDeductPaybackEdit.vue';
import {
  cancelDeduct,
  cancelYbInspection,
  getDeductList,
  getDeductSummary,
  getYbInspectionList,
  getYbInspectionSelectList
} from '@/api/insuranceDeduct';
// 状态码只用来判定按钮显隐与标签配色，文字一律从字典翻译（字典权威在 sql/163）
const ST_PENDING = 1, ST_APPEALING = 2, ST_WAIT_PAY = 4, ST_PAID = 5;
const INSPECT_RUNNING = 1;
const DEDUCT_DICT_TYPES = [
  DICT_TYPE.YB_INSPECT_TYPE, DICT_TYPE.YB_INSPECT_STATUS, DICT_TYPE.YB_DEDUCT_SOURCE,
  DICT_TYPE.YB_VIOLATION_TYPE, DICT_TYPE.YB_DEDUCT_STATUS, DICT_TYPE.YB_APPEAL_RESULT,
  DICT_TYPE.YB_LOSS_BEAR, DICT_TYPE.YB_DEDUCT_ACTION
];
const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];
const dictText = (type, value) => dictLabelText(dicts.value[type], value);
const activeTab = ref('notice');
// ==================== 科室下拉（跨科室选责任科室，要全院范围） ====================
const deptOptions = ref([]);
// ==================== 汇总（后端 SQL 聚合，禁止前端自算） ====================
const summary = ref({});
const loadSummary = async () => {
  try {
    const res = await getDeductSummary();
    summary.value = res.data || {};
  } catch (error) {
    ElMessage.error(error?.message || '扣款汇总加载失败');
  }
};
// ==================== 扣款通知列表 ====================
const noticeLoading = ref(false);
const noticeRows = ref([]);
const noticeQuery = reactive({
  deductStatus: undefined, sourceType: undefined,
  violationType: undefined, inspectionId: undefined,
  onlyOverdue: false, keyword: ''
});
const noticePage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadNotices = async () => {
  noticeLoading.value = true;
  try {
    const res = await getDeductList({
      ...noticeQuery, onlyOverdue: noticeQuery.onlyOverdue || undefined,
      pageNum: noticePage.pageNum, pageSize: noticePage.pageSize
    });
    noticeRows.value = res.data?.records || [];
    noticePage.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error?.message || '扣款通知列表加载失败');
  } finally {
    noticeLoading.value = false;
  }
};
const resetNoticeQuery = () => {
  noticeQuery.deductStatus = undefined;
  noticeQuery.sourceType = undefined;
  noticeQuery.violationType = undefined;
  noticeQuery.inspectionId = undefined;
  noticeQuery.onlyOverdue = false;
  noticeQuery.keyword = '';
  noticePage.pageNum = 1;
  loadNotices();
};
const money = (v) => `¥${Number(v ?? 0).toFixed(2)}`;
const statusTagType = (s) => s === ST_PENDING ? 'warning' : s === ST_APPEALING ? 'primary' : s === 3 ? 'success'
    : s === ST_WAIT_PAY ? 'danger' : s === ST_PAID ? 'success' : 'info';
// 动作显隐与后端状态机逐条对齐，后端仍是唯一裁判
const canEditNotice = (row) => row.deductStatus === ST_PENDING;
const canAppeal = (row) => row.deductStatus === ST_PENDING;
const canAppealResult = (row) => row.deductStatus === ST_APPEALING;
const canConfirm = (row) => row.deductStatus === ST_PENDING || row.deductStatus === ST_WAIT_PAY;
// 状态 4 有两个来路：申诉驳回（还没定责）和已确认追责。后端要求先定责才能缴回，
// 所以只有带 confirmBy 的待缴单才露「缴回」，否则点进去必然被拒。
const canPayback = (row) => row.deductStatus === ST_WAIT_PAY && !!row.confirmBy;
const canCancelNotice = (row) => row.deductStatus === ST_PENDING;
// ==================== 飞检批次候选 ====================
const inspectionOptions = ref([]);
const loadInspectionOptions = async () => {
  try {
    const res = await getYbInspectionSelectList();
    inspectionOptions.value = res.data || [];
  } catch (error) {
    ElMessage.error(error?.message || '飞检批次候选加载失败');
  }
};
// ==================== 通知单 新建/编辑 ====================
const noticeEditVisible = ref(false);
const noticeEditRow = ref(null);
const openNoticeCreate = () => {
  noticeEditRow.value = null;
  noticeEditVisible.value = true;
};
const openNoticeEdit = (row) => {
  noticeEditRow.value = row;
  noticeEditVisible.value = true;
};
// ==================== 详情（只读 + 留痕时间线） ====================
const noticeDetailVisible = ref(false);
const noticeDetailId = ref('');
const openNoticeDetail = (row) => {
  noticeDetailId.value = row.id;
  noticeDetailVisible.value = true;
};
// ==================== 申诉 ====================
const appealVisible = ref(false);
const appealRow = ref(null);
const openAppeal = (row) => {
  appealRow.value = row;
  appealVisible.value = true;
};
// ==================== 申诉结果 ====================
const resultVisible = ref(false);
const resultRow = ref(null);
const openAppealResult = (row) => {
  resultRow.value = row;
  resultVisible.value = true;
};
// ==================== 确认扣款并追责 ====================
const confirmVisible = ref(false);
const confirmRow = ref(null);
const openConfirm = (row) => {
  confirmRow.value = row;
  confirmVisible.value = true;
};
// ==================== 缴回 ====================
const paybackVisible = ref(false);
const paybackRow = ref(null);
const openPayback = (row) => {
  paybackRow.value = row;
  paybackVisible.value = true;
};
const reloadNoticeData = () => Promise.all([loadNotices(), loadSummary()]);
const cancelDeductRow = (row) => {
  ElMessageBox.prompt(`作废扣款单「${row.deductNo}」？仅误录/重复录入才作废，作废原因会写进留痕。`, '作废确认', {
    inputPlaceholder: '作废原因（必填）',
    inputValidator: (v) => (v && v.trim() ? true : '作废原因不能为空'),
    type: 'warning'
  }).then(async ({value}) => {
    try {
      await cancelDeduct({id: row.id, reason: value.trim()});
      ElMessage.success('已作废');
      await Promise.all([loadNotices(), loadSummary()]);
    } catch (error) {
      ElMessage.error(error?.message || '作废失败');
    }
  }).catch(() => {
  });
};
// ==================== 飞检批次 ====================
const inspectLoading = ref(false);
const inspectRows = ref([]);
const inspectQuery = reactive({inspectType: undefined, status: undefined, keyword: ''});
const inspectPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadInspections = async () => {
  inspectLoading.value = true;
  try {
    const res = await getYbInspectionList({
      ...inspectQuery,
      pageNum: inspectPage.pageNum,
      pageSize: inspectPage.pageSize
    });
    inspectRows.value = res.data?.records || [];
    inspectPage.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error?.message || '飞检批次列表加载失败');
  } finally {
    inspectLoading.value = false;
  }
};
const resetInspectQuery = () => {
  inspectQuery.inspectType = undefined;
  inspectQuery.status = undefined;
  inspectQuery.keyword = '';
  inspectPage.pageNum = 1;
  loadInspections();
};
const inspectEditVisible = ref(false);
const inspectEditRow = ref(null);
const openInspectCreate = () => {
  inspectEditRow.value = null;
  inspectEditVisible.value = true;
};
const openInspectEdit = (row) => {
  inspectEditRow.value = row;
  inspectEditVisible.value = true;
};
const concludeVisible = ref(false);
const concludeRow = ref(null);
const openConclude = (row) => {
  concludeRow.value = row;
  concludeVisible.value = true;
};
const reloadInspectData = () => Promise.all([loadInspections(), loadInspectionOptions()]);
const cancelInspectRow = (row) => {
  ElMessageBox.prompt(`作废批次「${row.inspectNo}」？名下有扣款通知时不能作废。`, '作废确认', {
    inputPlaceholder: '作废原因（必填）',
    inputValidator: (v) => (v && v.trim() ? true : '作废原因不能为空'),
    type: 'warning'
  }).then(async ({value}) => {
    try {
      await cancelYbInspection({id: row.id, reason: value.trim()});
      ElMessage.success('已作废');
      await Promise.all([loadInspections(), loadInspectionOptions()]);
    } catch (error) {
      ElMessage.error(error?.message || '批次作废失败');
    }
  }).catch(() => {
  });
};
// 从批次下钻到它名下的扣款单
const drillNotices = (row) => {
  noticeQuery.inspectionId = String(row.id);
  noticeQuery.onlyOverdue = false;
  noticePage.pageNum = 1;
  activeTab.value = 'notice';
  loadNotices();
};
const inspectTagType = (s) => (s === 1 ? 'primary' : s === 2 ? 'success' : 'info');
onMounted(async () => {
  dicts.value = await loadDictDataMap(DEDUCT_DICT_TYPES.join(','));
  await Promise.all([loadDepts(), loadNotices(), loadSummary(), loadInspections(), loadInspectionOptions()]);
});
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({scope: 'ALL'});
    deptOptions.value = res.data || [];
  } catch (error) {
    ElMessage.error(error?.message || '科室候选加载失败');
  }
};
</script>
