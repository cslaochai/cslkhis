<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">药品供应商退货</h2>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-400">只退挂了供应商档案的批次；药房的货先调拨回药库再退</span>
          <el-button v-perm="'pharmacy:supplierReturn:add'" :icon="Plus" data-testid="sreturn-new-btn" type="primary"
                     @click="openCreate">
            新建退货单
          </el-button>
        </div>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="退货单号">
          <el-input
              v-model="query.returnNo"
              clearable
              data-testid="sreturn-no-filter"
              placeholder="退货单号"
              style="width: 170px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              clearable
              placeholder="原因/原单据号关键字"
              style="width: 200px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="query.supplierId" :fit-input-width="false" clearable filterable placeholder="供应商"
                     style="width: 210px">
            <el-option v-for="s in suppliers" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态" style="width: 120px">
            <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
              v-model="query.dateRange"
              end-placeholder="结束"
              range-separator="至"
              start-placeholder="开始"
              style="width: 250px"
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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="sreturn-table" stripe>
        <el-table-column label="退货单号" prop="returnNo" width="150"/>
        <el-table-column label="供应商" min-width="160" prop="supplierName" show-overflow-tooltip/>
        <el-table-column label="退货原因" min-width="150" prop="returnReason" show-overflow-tooltip/>
        <el-table-column label="原单据号" width="140">
          <template #default="{ row }">{{ row.srcRefNo || '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="批次" width="70">
          <template #default="{ row }">{{ row.totalItems }}</template>
        </el-table-column>
        <el-table-column align="right" label="数量合计" width="90">
          <template #default="{ row }">{{ row.totalQuantity }}</template>
        </el-table-column>
        <el-table-column align="right" label="退款金额" width="105">
          <template #default="{ row }">
            <span class="text-rose-600">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="returnStatusTagType(row.status)">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="制单人" prop="createBy" width="90"/>
        <el-table-column label="制单时间" prop="createTime" width="150"/>
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="sreturn-detail-btn" link type="primary" @click="openDetail(row)">详情
            </el-button>
            <el-button v-if="canConfirmReturn(row)" v-perm="'pharmacy:supplierReturn:edit'" data-testid="sreturn-confirm-btn" link
                       type="warning" @click="doConfirm(row)">退货
            </el-button>
            <el-button v-if="canEditReturn(row)" v-perm="'pharmacy:supplierReturn:add'" link @click="openModify(row)">
              改明细
            </el-button>
            <el-button v-if="canCancelReturn(row)" v-perm="'pharmacy:supplierReturn:edit'" link type="info"
                       @click="doCancel(row)">作废
            </el-button>
            <el-button v-if="canDeleteReturn(row)" v-perm="'pharmacy:supplierReturn:delete'" link type="danger"
                       @click="doDelete(row)">删除
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

    <!-- 建单 / 改明细 -->
    <SupplierReturnEdit
        v-model="editVisible"
        :row="editRow"
        :initial-supplier-id="editInitialSupplierId"
        :suppliers="suppliers"
        :room-dict="roomDict"
        @saved="onEditSaved" />

    <!-- 详情 -->
    <SupplierReturnDetail
        v-model="drawerVisible"
        :detail="detail"
        :room-dict="roomDict"
        @confirm="doConfirm"
        @modify="openModify"
        @delete="doDelete" />
  </div>
</template>

<script setup>
/**
 * 药品供应商退货（sql/154 ③级：把库存批次退给供应商）
 *
 * 状态机：1待退货 →（确认退货）2已退货；只有「待退货」可改明细/作废/删除。
 * **只有「确认退货」这一步动库存**：逐批次扣减并落 9-退货出库流水（来源 supplierReturn+退货单号），
 * 金额按批次成本价 × 数量服务端算 —— 这是向供应商主张退款的依据，让前端传金额等于让它编。
 *
 * ⚠ 批次必须挂了 supplier_id 才能退：历史数据里 `supplier` 那一列多是**生产厂家名**而不是供货商，
 * 拿它当退货对象会把钱要错人，所以候选列表直接只出有结构化供应商的批次（药库侧）。
 * 药房的批次应先开「药房退回药库」调拨单搬回药库，再从这里退供应商 —— 两级不能跳。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {
  cancelSupplierReturn,
  confirmSupplierReturn,
  deleteSupplierReturnById,
  getSupplierReturnDetail,
  listSupplierReturnPage,
} from '@/api/pharmacy';
import {getSupplierSelectList} from '@/api/purchase';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {
  canCancelReturn,
  canConfirmReturn,
  canDeleteReturn,
  canEditReturn,
  returnStatusTagType,
} from '@/lib/supplierReturn';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import SupplierReturnEdit from './SupplierReturnEdit.vue';
import SupplierReturnDetail from './SupplierReturnDetail.vue';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  returnNo: '',
  supplierId: null,
  status: null,
  keyword: '',
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const statusDict = ref([]);
const roomDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const suppliers = ref([]);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.SUPPLIER_RETURN_STATUS},${DICT_TYPE.STOCK_ROOM}`);
    statusDict.value = res?.data?.[DICT_TYPE.SUPPLIER_RETURN_STATUS] || [];
    roomDict.value = res?.data?.[DICT_TYPE.STOCK_ROOM] || [];
  } catch (e) {
    console.error('加载退货字典失败', e);
  }
};
const loadSuppliers = async () => {
  try {
    const res = await getSupplierSelectList();
    suppliers.value = res?.data || [];
  } catch (e) {
    console.error('加载供应商下拉失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listSupplierReturnPage({
      returnNo: query.returnNo?.trim() || undefined,
      supplierId: query.supplierId || undefined,
      status: query.status ?? undefined,
      keyword: query.keyword?.trim() || undefined,
      dateStart: query.dateRange?.[0] || undefined,
      dateEnd: query.dateRange?.[1] || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    } else {
      ElMessage.error(res.message || '加载退货单失败');
    }
  } catch (e) {
    
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.returnNo = '';
  query.supplierId = null;
  query.status = null;
  query.keyword = '';
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// ==================== 建单 / 改明细 ====================
const editVisible = ref(false);
const editRow = ref(null);
const editInitialSupplierId = ref(null);
const openCreate = () => {
  editRow.value = null;
  editInitialSupplierId.value = query.supplierId || null;
  editVisible.value = true;
};
const onEditSaved = (data) => {
  loadList();
  openDetail(data);
};
// ==================== 详情 ====================
const drawerVisible = ref(false);
const detail = ref(null);
const openDetail = async (rowOrId) => {
  const id = typeof rowOrId === 'object' ? rowOrId.id : rowOrId;
  try {
    const res = await getSupplierReturnDetail(id);
    if (res.code === 200) {
      detail.value = res.data;
      drawerVisible.value = true;
    } 
  } catch (e) {
    console.error(e);
  }
};
// 列表行不带明细（主单分页只返回汇总）—— 详情拉取与候选加载都在建单子组件里做
const openModify = (doc) => {
  editRow.value = doc;
  editInitialSupplierId.value = null;
  editVisible.value = true;
};
// ==================== 状态机动作 ====================
const doConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确认退货？将按 ${row.totalItems ?? 0} 个批次扣减库存并写 9-退货出库流水（金额 ¥${row.totalAmount ?? 0}），` +
        '货一旦出库就离院，退货单不可再作废或删除。', '确认退货', {type: 'warning', confirmButtonText: '确认退货'});
  } catch {
    return;
  }
  await runAction(() => confirmSupplierReturn({id: row.id}), '已退货出库');
};
const doCancel = async (row) => {
  let reason = '';
  try {
    const r = await ElMessageBox.prompt('作废后这张单不再动库存，可以删除。请填写作废原因。', `作废退货单 ${row.returnNo}`, {
      confirmButtonText: '确认作废',
      inputPlaceholder: '作废原因（必填）',
      inputValidator: (v) => (v && v.trim() ? true : '作废必须填写原因'),
    });
    reason = r.value.trim();
  } catch {
    return;
  }
  await runAction(() => cancelSupplierReturn({id: row.id, reason}), '退货单已作废');
};
const doDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除退货单 ${row.returnNo}？只有「待退货 / 已作废」可删，动过库存的单据一律留档。`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteSupplierReturnById(String(row.id));
    if (res.code === 200) {
      ElMessage.success('已删除');
      drawerVisible.value = false;
      loadList();
    } 
  } catch (e) {
    console.error(e);
  }
};
const runAction = async (call, okText) => {
  try {
    const res = await call();
    if (res.code === 200) {
      ElMessage.success(res.message || okText);
      loadList();
      if (drawerVisible.value)
        await openDetail(res.data);
    } 
  } catch (e) {
    console.error(e);
  }
};
onMounted(() => {
  loadDicts();
  loadSuppliers();
  loadList();
});
</script>
