<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">采购订单</h2>
        <el-button v-perm="'pharmacy:purchase:add'" :icon="Plus" data-testid="order-add-btn" type="primary"
                   @click="openCreate">新建采购订单
        </el-button>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="订单号">
          <el-input
              v-model="query.orderNo"
              clearable
              data-testid="order-no-filter"
              placeholder="订单号"
              style="width: 180px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select
              v-model="query.supplierId"
              :fit-input-width="false"
              clearable
              filterable
              placeholder="供应商"
              style="width: 200px"
          >
            <el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="query.approvalStatus" :fit-input-width="false" clearable placeholder="审批状态"
                     style="width: 130px">
            <el-option :value="0" label="待审批"/>
            <el-option :value="1" label="已通过"/>
            <el-option :value="2" label="已驳回"/>
          </el-select>
        </el-form-item>
        <el-form-item label="收货状态">
          <el-select v-model="query.inboundDone" :fit-input-width="false" clearable placeholder="收货状态"
                     style="width: 130px">
            <el-option :value="false" label="未入库"/>
            <el-option :value="true" label="已入库"/>
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
              v-model="query.dateRange"
              end-placeholder="下单结束"
              range-separator="至"
              start-placeholder="下单开始"
              style="width: 260px"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="order-table" stripe>
        <el-table-column label="订单号" prop="orderNo" width="150"/>
        <el-table-column label="供应商" min-width="160" prop="supplierName" show-overflow-tooltip/>
        <el-table-column label="下单时间" prop="orderTime" width="160"/>
        <el-table-column align="right" label="金额" width="110">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column align="center" label="明细" width="90">
          <template #default="{ row }">{{ row.itemCount }} 项 / {{ row.totalQuantity }}</template>
        </el-table-column>
        <el-table-column align="center" label="审批" width="90">
          <template #default="{ row }">
            <el-tag :type="approvalTagType(row.approvalStatus)">{{ approvalText(row.approvalStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="收货" width="90">
          <template #default="{ row }">
            <el-tag :type="orderInboundTagType(row)">{{ orderInboundText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" prop="createBy" width="90"/>
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row }">
            <el-button :icon="View" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canEditOrder(row)" v-perm="'pharmacy:purchase:edit'" :icon="Edit" link type="primary"
                       @click="openEdit(row)">编辑
            </el-button>
            <el-button v-if="canAuditOrder(row)" v-perm="'pharmacy:purchase:edit'" link type="success"
                       @click="audit(row, true)">通过
            </el-button>
            <el-button v-if="canAuditOrder(row)" v-perm="'pharmacy:purchase:edit'" link type="danger"
                       @click="audit(row, false)">驳回
            </el-button>
            <el-button v-if="canGenerateInbound(row)" v-perm="'pharmacy:purchase:edit'" link type="warning"
                       @click="genInbound(row)">生成入库单
            </el-button>
            <el-button v-if="canDeleteOrder(row)" v-perm="'pharmacy:purchase:delete'" :icon="Delete" link type="danger"
                       @click="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, prev, pager, next, sizes"
            @current-change="loadList"
            @size-change="query.pageNum = 1; loadList()"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <PurchaseOrderEdit v-model="dialogVisible" :order-id="editingId" :supplier-options="supplierOptions"
                       @saved="loadList"/>

    <!-- 详情抽屉 -->
    <PurchaseOrderDetail v-model="drawerVisible" :order-id="detailId" :approval-dict="approvalDict"/>
  </div>
</template>

<script setup>
/**
 * 采购订单（G9）
 *
 * 金额口径：明细 amount = 数量 × 单价、单头总额 = Σ明细 —— 全部服务端重算，
 * 前端传的金额后端不采信；前端展示价 × 数量只是给操作人看的预估。
 *
 * 收货状态**不是订单表的列**：inboundDone / inboundNo 由入库单派生
 * （未入库 → 有入库单未完成「入库中」→ 已入库）。语义判定单点 lib/purchase.js。
 *
 * 审批文案走字典 his_purchase_approval_status；命中不了渲染「未知(n)」。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {
  auditPurchaseOrder,
  deletePurchaseOrder,
  generateInbound,
  getPurchaseOrderList,
} from '@/api/purchase';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {
  approvalTagType,
  canAuditOrder,
  canDeleteOrder,
  canEditOrder,
  canGenerateInbound,
  orderInboundTagType,
  orderInboundText,
} from '@/lib/purchase';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PurchaseOrderEdit from './PurchaseOrderEdit.vue';
import PurchaseOrderDetail from './PurchaseOrderDetail.vue';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  orderNo: '',
  supplierId: null,
  approvalStatus: null,
  inboundDone: null,
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const approvalDict = ref([]);
const approvalText = (v) => dictLabelText(approvalDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(DICT_TYPE.PURCHASE_APPROVAL_STATUS);
    approvalDict.value = res?.data?.[DICT_TYPE.PURCHASE_APPROVAL_STATUS] || [];
  } catch (e) {
    console.error('加载审批状态字典失败', e);
  }
};
// 供应商下拉（只含启用）
const supplierOptions = ref([]);
const loadSuppliers = async () => {
  try {
    const {getSupplierSelectList} = await import('@/api/purchase');
    const res = await getSupplierSelectList();
    if (res.code === 200)
      supplierOptions.value = res.data || [];
  } catch (e) {
    console.error('加载供应商下拉失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getPurchaseOrderList({
      orderNo: query.orderNo?.trim() || undefined,
      supplierId: query.supplierId || undefined,
      approvalStatus: query.approvalStatus ?? undefined,
      inboundDone: query.inboundDone ?? undefined,
      dateStart: query.dateRange?.[0] || undefined,
      dateEnd: query.dateRange?.[1] || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    } else {
      ElMessage.error(res.message || '加载采购订单失败');
    }
  } catch (e) {
    
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.orderNo = '';
  query.supplierId = null;
  query.approvalStatus = null;
  query.inboundDone = null;
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// ==================== 新建/编辑（子组件 PurchaseOrderEdit） ====================
const dialogVisible = ref(false);
const editingId = ref(null);
const openCreate = () => {
  editingId.value = null;
  dialogVisible.value = true;
};
const openEdit = (row) => {
  editingId.value = row.orderId;
  dialogVisible.value = true;
};
// ==================== 审批 / 生成入库单 / 删除 ====================
const audit = async (row, pass) => {
  let remark = '';
  if (!pass) {
    try {
      const r = await ElMessageBox.prompt('驳回必须填写原因', `驳回订单 ${row.orderNo}`, {
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputPlaceholder: '驳回原因（必填）',
        inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
      });
      remark = r.value.trim();
    } catch {
      return;
    }
  } else {
    try {
      await ElMessageBox.confirm(`确认通过订单 ${row.orderNo} 的审批？`, '审批确认', {type: 'warning'});
    } catch {
      return;
    }
  }
  try {
    const res = await auditPurchaseOrder({
      orderId: row.orderId,
      approvalStatus: pass ? 1 : 2,
      remark: remark || undefined
    });
    if (res.code === 200) {
      ElMessage.success(pass ? '已通过' : '已驳回');
      loadList();
    } 
  } catch (e) {
    console.error(e);
  }
};
const genInbound = async (row) => {
  try {
    await ElMessageBox.confirm(`将按订单 ${row.orderNo} 的明细生成入库单（生成后到「入库单」页审核并入库）。确认？`, '生成入库单', {
      type: 'info',
      confirmButtonText: '生成',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    const res = await generateInbound(row.orderId);
    if (res.code === 200) {
      ElMessage.success(`入库单 ${res.data?.inboundNo || ''} 已生成`);
      loadList();
    } 
  } catch (e) {
    console.error(e);
  }
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除订单 ${row.orderNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deletePurchaseOrder(row.orderId);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadList();
    } 
  } catch (e) {
    console.error(e);
  }
};
// ==================== 详情抽屉（子组件 PurchaseOrderDetail） ====================
const drawerVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.orderId;
  drawerVisible.value = true;
};
onMounted(() => {
  loadDicts();
  loadSuppliers();
  loadList();
});
</script>
