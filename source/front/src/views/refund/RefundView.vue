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

    <!-- 详情 / 发起申请：拆到同目录兄弟组件，页面只管开关与递参 -->
    <RefundDetail v-model="detailVisible" :id="detailId" :row="detailRow"
                  :apply-status-options="applyStatusOptions" :refund-type-options="refundTypeOptions"
                  :refund-method-options="refundMethodOptions" :pay-method-options="payMethodOptions"/>
    <RefundAdd v-model="applyVisible" :refund-type-options="refundTypeOptions" @saved="onApplySaved"/>

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
  getRefundApplyList,
} from '@/api/refund';
import {getDictDataMapList} from '@/api/system';
import RefundDetail from './RefundDetail.vue';
import RefundAdd from './RefundAdd.vue';
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
const detailId = ref('');
const detailRow = ref(null);
// 审核
const auditVisible = ref(false);
const auditSubmitting = ref(false);
const auditForm = reactive({id: '', applyNo: '', approved: true, remark: ''});
// 发起申请：表单与提交逻辑拆到 RefundAdd，页面只管开关与保存后刷新
const applyVisible = ref(false);

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

function openDetail(row) {
  detailId.value = row.id;
  detailRow.value = row;
  detailVisible.value = true;
}

/* ---------------- 发起退费申请：表单拆到 RefundAdd ---------------- */
function openApply() {
  applyVisible.value = true;
}

function onApplySaved() {
  pagination.pageNum = 1;
  loadList();
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
