<template>
  <div>
    <div class="mb-3 flex items-start justify-between">
      <el-button v-perm="'finance:refund:add'" :icon="Plus" type="primary" @click="openApply">发起退费申请</el-button>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                :prefix-icon="Search"
                class="!w-72"
                clearable
                placeholder="申请号 / 原收费单号 / 患者姓名"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select v-model="query.applyStatus" class="!w-40" clearable placeholder="申请状态">
              <el-option
                  v-for="o in applyStatusOptions"
                  :key="o.dictValue"
                  :label="o.dictLabel"
                  :value="Number(o.dictValue)"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="text-sm text-slate-500">
            共 <span class="font-semibold text-slate-700">{{ loading ? '—' : pagination.total }}</span> 条申请
          </span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="refund-apply-table" stripe>
        <el-table-column class-name="font-mono" label="退费申请号" min-width="200" prop="refundApplyNo"/>
        <el-table-column label="患者" min-width="110">
          <template #default="{ row }">{{ row.patientName || '—' }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="原账单号" min-width="160" prop="billNo">
          <template #default="{ row }">{{ row.billNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="退费类型" width="110">
          <template #default="{ row }">{{ dictLabelText(refundTypeOptions, row.refundType) }}</template>
        </el-table-column>
        <el-table-column align="right" label="退费金额" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
                :class="STATUS_TAG_CLASS[row.applyStatus] || 'bg-slate-100 text-slate-600'"
                class="border"
                data-testid="refund-status-tag"
                effect="plain"
                size="small"
            >
              {{ dictLabelText(applyStatusOptions, row.applyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请人" prop="applyBy" width="100">
          <template #default="{ row }">{{ row.applyBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" prop="applyTime" width="170">
          <template #default="{ row }">{{ row.applyTime || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="refund-btn-detail" link size="small" type="primary"
                       @click="openDetail(row)">详情
            </el-button>
            <template v-if="row.applyStatus === 1">
              <el-button v-perm="'finance:refund:edit'" :icon="Select" data-testid="refund-btn-pass" link size="small"
                         type="success" @click="openAudit(row, true)">通过
              </el-button>
              <el-button v-perm="'finance:refund:edit'" :icon="Close" data-testid="refund-btn-reject" link size="small"
                         type="danger" @click="openAudit(row, false)">驳回
              </el-button>
            </template>
            <el-button
                v-else-if="row.applyStatus === 2"
                v-perm="'finance:refund:edit'"
                :icon="CircleCheck"
                data-testid="refund-btn-execute"
                link
                size="small"
                type="warning"
                @click="handleExecute(row)"
            >
              执行退费
            </el-button>
            <!-- 作废：把这张收费单从判重的锁里放出来。审核通过后才发现问题（金额退不动、
                 患者改了主意）以前只能来库里改状态，现在收费处自己就能退回重发。 -->
            <el-button
                v-if="DISCARDABLE.includes(row.applyStatus)"
                v-perm="'finance:refund:edit'"
                :icon="CircleClose"
                data-testid="refund-btn-discard"
                link
                size="small"
                type="info"
                @click="openDiscard(row)"
            >
              作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="py-12 text-center text-sm text-slate-400">
        没有符合条件的退费申请
      </div>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" data-testid="refund-detail-dialog" destroy-on-close title="退费申请详情"
               width="640px">
      <div v-loading="detailLoading" class="space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-3 rounded-lg border border-slate-200 p-4">
          <div><span class="text-slate-400">申请号：</span><span
              class="font-mono text-slate-700">{{ detail?.refundApplyNo || '—' }}</span></div>
          <div><span class="text-slate-400">状态：</span><span
              class="text-slate-700">{{ dictLabelText(applyStatusOptions, detail?.applyStatus) }}</span></div>
          <div><span class="text-slate-400">患者：</span><span class="text-slate-700">{{
              detail?.patientName || '—'
            }}</span></div>
          <div><span class="text-slate-400">患者号：</span><span class="text-slate-700">{{
              detail?.patientNo || '—'
            }}</span></div>
          <div><span class="text-slate-400">原账单号：</span><span
              class="font-mono text-slate-700">{{ detail?.billNo || '—' }}</span></div>
          <div><span class="text-slate-400">退费类型：</span><span
              class="text-slate-700">{{ dictLabelText(refundTypeOptions, detail?.refundType) }}</span></div>
          <div><span class="text-slate-400">退费金额：</span><span
              class="font-semibold text-slate-800">¥{{ formatMoney(detail?.refundAmount) }}</span></div>
          <div><span class="text-slate-400">申请人：</span><span class="text-slate-700">{{
              detail?.applyBy || '—'
            }}</span></div>
          <div><span class="text-slate-400">申请时间：</span><span class="text-slate-700">{{
              detail?.applyTime || '—'
            }}</span></div>
          <div><span class="text-slate-400">审核人：</span><span class="text-slate-700">{{
              detail?.auditorName || '—'
            }}</span></div>
          <div><span class="text-slate-400">审核时间：</span><span class="text-slate-700">{{
              detail?.auditTime || '—'
            }}</span></div>
          <div><span class="text-slate-400">退费人：</span><span class="text-slate-700">{{
              detail?.refundBy || '—'
            }}</span></div>
          <div><span class="text-slate-400">退费时间：</span><span class="text-slate-700">{{
              detail?.refundTime || '—'
            }}</span></div>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <span class="text-slate-400">退费原因：</span><span class="text-slate-700">{{
            detail?.refundReason || '—'
          }}</span>
        </div>
        <div v-if="detail?.auditRemark" class="rounded-lg border border-slate-200 p-3">
          <span class="text-slate-400">审核意见：</span><span class="text-slate-700">{{ detail.auditRemark }}</span>
        </div>
        <div v-if="detail?.applyStatus === 5" class="rounded-lg border border-slate-200 p-3">
          <div class="text-slate-400">作废留痕</div>
          <div class="mt-1"><span class="text-slate-400">作废人：</span><span
              class="text-slate-700">{{ detail?.cancelBy || '—' }}</span></div>
          <div><span class="text-slate-400">作废时间：</span><span class="text-slate-700">{{
              detail?.cancelTime || '—'
            }}</span></div>
          <div><span class="text-slate-400">作废原因：</span><span class="text-slate-700">{{
              detail?.cancelReason || '—'
            }}</span></div>
        </div>
        <!-- 执行过才非空：钱到底走哪个渠道退出去的，日结对账就查这几列 -->
        <div v-if="detail?.flowRefundNo" class="rounded-lg border border-slate-200 p-3" data-testid="refund-flow-block">
          <div class="text-slate-400">退费流水</div>
          <div class="mt-1 grid grid-cols-2 gap-3">
            <div><span class="text-slate-400">退费单号：</span><span
                class="font-mono text-slate-700">{{ detail.flowRefundNo }}</span></div>
            <div><span class="text-slate-400">退费方式：</span><span
                class="text-slate-700">{{ dictLabelText(refundMethodOptions, detail.flowRefundMethod) }}</span></div>
            <div><span class="text-slate-400">原支付方式：</span><span
                class="text-slate-700">{{ dictLabelText(payMethodOptions, detail.flowPayMethod) }}</span></div>
            <div><span class="text-slate-400">渠道退费流水号：</span><span
                class="font-mono text-slate-700">{{ detail.flowChannelRefundNo || '—' }}</span></div>
          </div>
          <div v-if="detail.flowInsuranceCancelled === 1" class="mt-1 text-amber-700">
            医保结算清单已随本次退费撤销报盘（2305）
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 发起申请 -->
    <el-dialog v-model="applyVisible" destroy-on-close title="发起退费申请" width="560px">
      <el-form class="space-y-2" label-width="96px">
        <el-form-item label="患者" required>
          <PatientSelect v-model="applyForm.patientId" @select="onPatientSelect"/>
        </el-form-item>
        <el-form-item label="结算账单" required>
          <el-select
              v-model="applyForm.billId"
              :disabled="!applyForm.patientId"
              :fit-input-width="false"
              :loading="billLoading"
              class="w-full"
              data-testid="refund-bill-select"
              filterable
              placeholder="请先选择患者"
              @change="onBillChange"
          >
            <el-option
                v-for="b in billOptions"
                :key="b.id"
                :label="`${b.billNo}（应收 ¥${formatMoney(b.payableAmount)}，已收 ¥${formatMoney(b.paidAmount)}）`"
                :value="b.id"
            />
          </el-select>
          <div class="w-full mt-1 text-xs text-slate-400">
            只列收过钱的账单（已支付 / 已退费）；金额必须落在账单行边界上，后端会列出可选金额。
          </div>
        </el-form-item>
        <el-form-item label="退费类型" required>
          <el-select v-model="applyForm.refundType" class="w-full" placeholder="请选择">
            <el-option
                v-for="o in refundTypeOptions"
                :key="o.dictValue"
                :label="o.dictLabel"
                :value="Number(o.dictValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="退费金额" required>
          <el-input-number v-model="applyForm.refundAmount" :min="0" :precision="2" :step="1" class="!w-full"/>
        </el-form-item>
        <el-form-item label="退费原因" required>
          <el-input v-model="applyForm.refundReason" :rows="3" placeholder="请填写退费原因" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button v-perm="'finance:refund:add'" :loading="applySubmitting" type="primary" @click="submitApply">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核 -->
    <el-dialog v-model="auditVisible" :title="auditForm.approved ? '审核通过' : '审核驳回'" destroy-on-close
               width="460px">
      <div class="space-y-4 text-sm">
        <div class="rounded-lg border border-slate-200 p-3">
          申请号：<span class="font-mono text-slate-700">{{ auditForm.applyNo }}</span>
        </div>
        <el-input
            v-model="auditForm.remark"
            :placeholder="auditForm.approved ? '审核意见（选填）' : '驳回原因（必填）'"
            :rows="3"
            type="textarea"
        />
      </div>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button
            v-perm="'finance:refund:edit'"
            :loading="auditSubmitting"
            :type="auditForm.approved ? 'primary' : 'danger'"
            @click="submitAudit"
        >
          确认{{ auditForm.approved ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
    <!-- 作废 -->
    <el-dialog v-model="discardVisible" data-testid="refund-discard-dialog" destroy-on-close title="作废退费申请"
               width="460px">
      <div class="space-y-4 text-sm">
        <div class="rounded-lg border border-slate-200 p-3">
          申请号：<span class="font-mono text-slate-700">{{ discardForm.applyNo }}</span>
        </div>
        <p class="text-slate-500">
          作废只撤回这张申请单，<b>不动收费单、不动钱</b>；作废后同一张收费单就能重新发起退费申请了。
        </p>
        <el-input
            v-model="discardForm.reason"
            :rows="3"
            data-testid="refund-discard-reason"
            placeholder="作废原因（必填）"
            type="textarea"
        />
      </div>
      <template #footer>
        <el-button @click="discardVisible = false">取消</el-button>
        <el-button
            v-perm="'finance:refund:edit'"
            :loading="discardSubmitting"
            data-testid="refund-discard-confirm"
            type="danger"
            @click="submitDiscard"
        >
          确认作废
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {CircleCheck, CircleClose, Close, Plus, Refresh, Search, Select, View} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  auditRefundApply,
  discardRefundApply,
  executeRefund,
  getRefundApplyDetail,
  getRefundApplyList,
  submitRefundApply,
} from '@/api/refund';
import {getBillListPage} from '@/api/settlementBill';
import {getDictDataMapList} from '@/api/system';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const STATUS_TAG_CLASS = {
  1: 'bg-amber-100 text-amber-700',
  2: 'bg-blue-100 text-blue-700',
  3: 'bg-rose-100 text-rose-700',
  4: 'bg-emerald-100 text-emerald-700',
  5: 'bg-slate-100 text-slate-500',
};
/** 1-待审核 / 2-审核通过：还能作废的两态（与后端 RefundApplyStatusEnum.isInflight 同口径） */
const DISCARDABLE = [1, 2];
// 「加载中 ≠ 没有」：初值 true，未加载完不显示「暂无数据」
const loading = ref(true);
const rows = ref([]);
const query = reactive({
  keyword: '',
  applyStatus: null,
});
const pagination = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const refundTypeOptions = ref([]);
const applyStatusOptions = ref([]);
const refundMethodOptions = ref([]);
const payMethodOptions = ref([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref(null);
// 审核
const auditVisible = ref(false);
const auditSubmitting = ref(false);
const auditForm = reactive({id: '', applyNo: '', approved: true, remark: ''});
// 发起申请
const applyVisible = ref(false);
const applySubmitting = ref(false);
const billLoading = ref(false);
const billOptions = ref([]);
const selectedPatient = ref(null);
const applyForm = reactive({
  patientId: null,
  billId: null,
  billNo: '',
  refundType: null,
  refundAmount: null,
  refundReason: '',
});

async function loadDicts() {
  try {
    const res = await getDictDataMapList('his_refund_apply_type,his_refund_apply_status,his_refund_method,his_pay_method');
    if (res.code === 200 && res.data) {
      refundTypeOptions.value = res.data['his_refund_apply_type'] || [];
      applyStatusOptions.value = res.data['his_refund_apply_status'] || [];
      refundMethodOptions.value = res.data['his_refund_method'] || [];
      payMethodOptions.value = res.data['his_pay_method'] || [];
    }
  } catch (e) {
    // 字典拿不到 → 码值会渲染「未知(n)」，不回落成合法值；这里只记录，不阻塞页面
    console.error('加载退费字典失败', e);
  }
}

async function loadList() {
  loading.value = true;
  try {
    const res = await getRefundApplyList({
      keyword: query.keyword || undefined,
      applyStatus: query.applyStatus === null ? undefined : query.applyStatus,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    });
    const data = res.data || {};
    rows.value = data.records || [];
    pagination.total = Number(data.total || 0);
  } catch (e) {
    rows.value = [];
    pagination.total = 0;
    ElMessage.error(e?.message || '加载退费申请失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.pageNum = 1;
  loadList();
}

function handleReset() {
  query.keyword = '';
  query.applyStatus = null;
  handleSearch();
}

function handleSizeChange(size) {
  pagination.pageSize = size;
  pagination.pageNum = 1;
  loadList();
}

function handleCurrentChange(page) {
  pagination.pageNum = page;
  loadList();
}

async function openDetail(row) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = row;
  try {
    const res = await getRefundApplyDetail(row.id);
    if (res.data)
      detail.value = res.data;
  } catch (e) {
    ElMessage.warning(e?.message || '获取退费详情失败，已展示列表数据');
  } finally {
    detailLoading.value = false;
  }
}

/* ---------------- 发起退费申请 ---------------- */
function openApply() {
  applyForm.patientId = null;
  applyForm.billId = null;
  applyForm.billNo = '';
  applyForm.refundType = null;
  applyForm.refundAmount = null;
  applyForm.refundReason = '';
  billOptions.value = [];
  selectedPatient.value = null;
  applyVisible.value = true;
}

async function onPatientSelect(patient) {
  selectedPatient.value = patient || null;
  applyForm.patientId = patient?.id ?? null;
  applyForm.billId = null;
  applyForm.billNo = '';
  applyForm.refundAmount = null;
  billOptions.value = [];
  if (!applyForm.patientId)
    return;
  billLoading.value = true;
  try {
    // 四层口径：退费锚点是结算账单（L2）。收过钱的账单才有钱可退：
    // 3-已支付 全额可退，5-已退费 还能续退；1/2 没收齐、4 已作废（没收过钱）不可退。
    const res = await getBillListPage({
      patientId: applyForm.patientId,
      billStatusList: [3, 5],
      pageNum: 1,
      pageSize: 200,
    });
    billOptions.value = res.data?.records || [];
  } catch (e) {
    ElMessage.error(e?.message || '加载该患者的结算账单失败');
  } finally {
    billLoading.value = false;
  }
}

function onBillChange(billId) {
  const hit = billOptions.value.find(b => String(b.id) === String(billId));
  applyForm.billNo = hit?.billNo || '';
  // 默认带出已收合计，允许改小；金额必须落在账单行边界上（后端按整行退校验，报错会列出可选金额）
  const paid = hit?.paidAmount;
  applyForm.refundAmount = paid === null || paid === undefined ? null : Number(paid);
}

async function submitApply() {
  if (!applyForm.patientId) {
    ElMessage.warning('请先选择患者');
    return;
  }
  if (!applyForm.billId) {
    ElMessage.warning('请选择原结算账单');
    return;
  }
  if (applyForm.refundType === null) {
    ElMessage.warning('请选择退费类型');
    return;
  }
  if (applyForm.refundAmount === null || Number(applyForm.refundAmount) < 0) {
    ElMessage.warning('请填写正确的退费金额');
    return;
  }
  if (!applyForm.refundReason.trim()) {
    ElMessage.warning('请填写退费原因');
    return;
  }
  applySubmitting.value = true;
  try {
    await submitRefundApply({
      billId: applyForm.billId,
      billNo: applyForm.billNo,
      patientId: applyForm.patientId,
      patientNo: selectedPatient.value?.patientNo,
      patientName: selectedPatient.value?.name,
      refundType: applyForm.refundType,
      refundReason: applyForm.refundReason.trim(),
      refundAmount: applyForm.refundAmount,
      // applyBy 不从前端编造，后端按当前登录人回填
    });
    ElMessage.success('退费申请已提交，等待审核');
    applyVisible.value = false;
    pagination.pageNum = 1;
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '提交退费申请失败');
  } finally {
    applySubmitting.value = false;
  }
}

/* ---------------- 审核 / 执行 ---------------- */
function openAudit(row, approved) {
  auditForm.id = row.id;
  auditForm.applyNo = row.refundApplyNo;
  auditForm.approved = approved;
  auditForm.remark = '';
  auditVisible.value = true;
}

async function submitAudit() {
  if (!auditForm.approved && !auditForm.remark.trim()) {
    ElMessage.warning('驳回必须填写审核意见');
    return;
  }
  auditSubmitting.value = true;
  try {
    await auditRefundApply({
      id: auditForm.id,
      approved: auditForm.approved,
      remark: auditForm.remark.trim(),
      // auditorId / auditorName 由后端按当前登录人回填
    });
    ElMessage.success(auditForm.approved ? '已审核通过' : '已驳回');
    auditVisible.value = false;
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '审核失败');
  } finally {
    auditSubmitting.value = false;
  }
}

/* ---------------- 作废 ---------------- */
const discardVisible = ref(false);
const discardSubmitting = ref(false);
const discardForm = reactive({id: '', applyNo: '', reason: ''});

function openDiscard(row) {
  discardForm.id = row.id;
  discardForm.applyNo = row.refundApplyNo;
  discardForm.reason = '';
  discardVisible.value = true;
}

async function submitDiscard() {
  if (!discardForm.reason.trim()) {
    ElMessage.warning('请填写作废原因');
    return;
  }
  discardSubmitting.value = true;
  try {
    await discardRefundApply({id: discardForm.id, reason: discardForm.reason.trim()});
    ElMessage.success('已作废，这张收费单可以重新发起退费申请了');
    discardVisible.value = false;
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '作废失败');
  } finally {
    discardSubmitting.value = false;
  }
}

async function handleExecute(row) {
  try {
    await ElMessageBox.confirm(`确认对申请 ${row.refundApplyNo} 执行退费 ¥${formatMoney(row.refundAmount)}？执行后不可撤销。`, '执行退费', {
      type: 'warning',
      confirmButtonText: '确认执行',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await executeRefund(row.id);
    ElMessage.success('退费已执行');
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '执行退费失败');
  }
}

onMounted(async () => {
  await loadDicts();
  await loadList();
});
</script>
