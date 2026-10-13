<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">药品调拨（药库 ↔ 药房）</h2>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-400">发出扣一个库位、接收加另一个库位，两行流水合计 0 才算搬完</span>
          <el-button v-perm="'pharmacy:drugTransfer:add'" :icon="Plus" data-testid="transfer-new-btn" type="primary"
                     @click="openCreate">
            新建调拨单
          </el-button>
        </div>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="调拨单号">
          <el-input
              v-model="query.transferNo"
              clearable
              data-testid="transfer-no-filter"
              placeholder="调拨单号"
              style="width: 170px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              clearable
              placeholder="事由/单号关键字"
              style="width: 190px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="query.transferType" :fit-input-width="false" clearable placeholder="方向"
                     style="width: 150px">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="transfer-table" stripe>
        <el-table-column label="调拨单号" prop="transferNo" width="150"/>
        <el-table-column align="center" label="方向" width="140">
          <template #default="{ row }">
            <span class="text-slate-700">{{ row.transferTypeText }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="库位" width="120">
          <template #default="{ row }">{{ row.fromRoomText }} → {{ row.toRoomText }}</template>
        </el-table-column>
        <el-table-column label="事由" min-width="150" prop="reason" show-overflow-tooltip/>
        <el-table-column align="right" label="批次" width="70">
          <template #default="{ row }">{{ row.totalItems }}</template>
        </el-table-column>
        <el-table-column align="right" label="申请/发出/接收" width="130">
          <template #default="{ row }">
            <span>{{ row.totalQuantity }}</span>
            <span class="text-slate-400"> / {{ row.outQuantity }} / {{ row.inQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="100">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="transferStatusTagType(row.status)">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="制单人" prop="createBy" width="90"/>
        <el-table-column label="制单时间" prop="createTime" width="150"/>
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="transfer-detail-btn" link type="primary" @click="openDetail(row)">
              详情
            </el-button>
            <el-button v-if="canTransferOut(row)" v-perm="'pharmacy:drugTransfer:edit'" data-testid="transfer-out-btn" link
                       type="warning" @click="doOut(row)">发出
            </el-button>
            <el-button v-if="canTransferIn(row)" v-perm="'pharmacy:drugTransfer:edit'" data-testid="transfer-in-btn" link
                       type="success" @click="doIn(row)">接收
            </el-button>
            <el-button v-if="canEditTransfer(row)" v-perm="'pharmacy:drugTransfer:add'" link @click="openModify(row)">
              改明细
            </el-button>
            <el-button v-if="canCancelTransfer(row)" v-perm="'pharmacy:drugTransfer:edit'" link type="info"
                       @click="doCancel(row)">作废
            </el-button>
            <el-button v-if="canDeleteTransfer(row)" v-perm="'pharmacy:drugTransfer:delete'" link type="danger"
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
    <DrugTransferEdit v-model="formVisible" :id="editId" :row="editRow" :type-dict="typeDict"
                      @saved="onEditSaved"/>

    <!-- 详情 -->
    <DrugTransferDetail ref="detailRef" v-model="drawerVisible" :id="detailId"
                        @out="doOut"
                        @in="doIn"
                        @modify="openModify"
                        @delete="doDelete"/>
  </div>
</template>

<script setup>
/**
 * 药品调拨（sql/154 ②级：药库 ↔ 药房）
 *
 * 状态机：1待发出 →（确认发出）2待接收 →（确认接收）3已完成；只有「待发出」可改明细/作废/删除。
 * **发出与接收各动一次库存**：确认发出扣发出库位并落 7-调拨出库（负），确认接收加接收库位并落
 * 8-调拨入库（正），两行流水合计 0 就是「搬出去又搬进来」的账实闭环证据（详情抽屉直接算给你看）。
 * 在途（待接收）不许作废 —— 只有一行 7 没有 8，作废等于把库存挂在空中，只能再开一张反向调拨搬回去。
 *
 * 明细的药品名/规格/批号/成本价是建单那一刻从批次上抓的**快照**，事后字典改名不改历史记载；
 * 数量前端只传「哪个批次 + 调多少」，成本价一律服务端算（让前端传金额等于让它编）。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {
  cancelDrugTransfer,
  confirmDrugTransferIn,
  confirmDrugTransferOut,
  deleteDrugTransferById,
  listDrugTransferPage,
} from '@/api/pharmacy';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {
  canCancelTransfer,
  canDeleteTransfer,
  canEditTransfer,
  canTransferIn,
  canTransferOut,
  transferStatusTagType,
} from '@/lib/drugTransfer';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import DrugTransferEdit from './DrugTransferEdit.vue';
import DrugTransferDetail from './DrugTransferDetail.vue';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  transferNo: '',
  transferType: null,
  status: null,
  keyword: '',
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const typeDict = ref([]);
const statusDict = ref([]);
const typeText = (v) => dictLabelText(typeDict.value, v);
const statusText = (v) => dictLabelText(statusDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.DRUG_TRANSFER_TYPE},${DICT_TYPE.DRUG_TRANSFER_STATUS}`);
    typeDict.value = res?.data?.[DICT_TYPE.DRUG_TRANSFER_TYPE] || [];
    statusDict.value = res?.data?.[DICT_TYPE.DRUG_TRANSFER_STATUS] || [];
  } catch (e) {
    console.error('加载调拨字典失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listDrugTransferPage({
      transferNo: query.transferNo?.trim() || undefined,
      transferType: query.transferType ?? undefined,
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
      ElMessage.error(res.message || '加载调拨单失败');
    }
  } catch (e) {
    
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.transferNo = '';
  query.transferType = null;
  query.status = null;
  query.keyword = '';
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// ==================== 建单 / 改明细（子组件 DrugTransferEdit） ====================
const formVisible = ref(false);
const editId = ref(null);
const editRow = ref(null);
const openCreate = () => {
  editId.value = null;
  editRow.value = null;
  formVisible.value = true;
};
// 列表行不带明细（主单分页只返回汇总），把行交给子组件，由子组件按需捞详情再改
const openModify = (doc) => {
  editId.value = doc.id;
  editRow.value = doc;
  formVisible.value = true;
};
const onEditSaved = (resData) => {
  loadList();
  openDetail(resData);
};
// ==================== 详情（子组件 DrugTransferDetail） ====================
const drawerVisible = ref(false);
const detailId = ref(null);
const detailRef = ref(null);
const openDetail = (rowOrId) => {
  detailId.value = typeof rowOrId === 'object' ? rowOrId.id : rowOrId;
  drawerVisible.value = true;
};
// 状态机动作成功后：刷新列表，并让打开着的详情抽屉重取本单（明细/流水/状态都变了）
const reloadDrawer = (rowOrId) => {
  if (drawerVisible.value)
    detailRef.value?.reload(typeof rowOrId === 'object' ? rowOrId.id : rowOrId);
};
// ==================== 状态机动作 ====================
const doOut = async (row) => {
  try {
    await ElMessageBox.confirm(`确认发出？将按 ${row.totalItems ?? 0} 个批次从「${row.fromRoomText}」扣减库存并写 7-调拨出库流水，` +
        `发出后单据进入「待接收」，不能再改明细。`, '确认发出', {type: 'warning', confirmButtonText: '确认发出'});
  } catch {
    return;
  }
  await runAction(() => confirmDrugTransferOut({id: row.id}), '已发出，等待对方库位接收');
};
const doIn = async (row) => {
  try {
    await ElMessageBox.confirm(`确认接收？将按明细把 ${row.outQuantity ?? 0} 个单位落到「${row.toRoomText}」并写 8-调拨入库流水；` +
        '同批号在该库位已有批次就叠加，没有则按快照新建批次。接收后本单完成、不可再改。', '确认接收', {
      type: 'warning',
      confirmButtonText: '确认接收'
    });
  } catch {
    return;
  }
  await runAction(() => confirmDrugTransferIn({id: row.id}), '已接收，本单调拨完成');
};
const doCancel = async (row) => {
  let reason = '';
  try {
    const r = await ElMessageBox.prompt('作废后这张单不再动库存，可以删除。请填写作废原因。', `作废调拨单 ${row.transferNo}`, {
      confirmButtonText: '确认作废',
      inputPlaceholder: '作废原因（必填）',
      inputValidator: (v) => (v && v.trim() ? true : '作废必须填写原因'),
    });
    reason = r.value.trim();
  } catch {
    return;
  }
  await runAction(() => cancelDrugTransfer({id: row.id, reason}), '调拨单已作废');
};
const doDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除调拨单 ${row.transferNo}？只有「待发出 / 已作废」可删，动过库存的单据一律留档。`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteDrugTransferById(String(row.id));
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
      reloadDrawer(res.data);
    } 
  } catch (e) {
    console.error(e);
  }
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
