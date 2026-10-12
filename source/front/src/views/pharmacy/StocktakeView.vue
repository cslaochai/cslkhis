<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">药房盘点</h2>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-400">建单抓账面快照 → 录实盘 → 复核通过才调整库存</span>
          <el-button v-perm="'pharmacy:stocktake:add'" :icon="Plus" data-testid="stocktake-new-btn" type="primary"
                     @click="openCreate">
            新建盘点单
          </el-button>
        </div>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="盘点单号">
          <el-input
              v-model="query.stocktakeNo"
              clearable
              data-testid="stocktake-no-filter"
              placeholder="盘点单号"
              style="width: 170px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="盘点主题">
          <el-input
              v-model="query.stocktakeTitle"
              clearable
              placeholder="盘点主题"
              style="width: 190px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态" style="width: 130px">
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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="stocktake-table" stripe>
        <el-table-column label="盘点单号" prop="stocktakeNo" width="150"/>
        <el-table-column label="主题" min-width="170" prop="stocktakeTitle" show-overflow-tooltip/>
        <el-table-column label="范围" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.scopeDesc || '—' }}</template>
        </el-table-column>
        <el-table-column label="账面快照时点" prop="snapshotTime" width="160"/>
        <el-table-column align="right" label="批次(已录/总)" width="115">
          <template #default="{ row }">{{ row.countedItems }} / {{ row.totalItems }}</template>
        </el-table-column>
        <el-table-column align="center" label="差异(盈/亏)" width="110">
          <template #default="{ row }">
            <span class="text-rose-600">{{ row.profitItems }}</span> /
            <span class="text-emerald-700">{{ row.lossItems }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="净差数量" width="90">
          <template #default="{ row }">{{ diffText(row.diffQuantity) }}</template>
        </el-table-column>
        <el-table-column align="right" label="净差金额" width="100">
          <template #default="{ row }">{{ formatMoney(row.diffAmount) }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="stocktakeStatusTagType(row.status)">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="270">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="stocktake-detail-btn" link type="primary" @click="openDetail(row)">
              详情
            </el-button>
            <el-button v-if="canCount(row)" v-perm="'pharmacy:stocktake:edit'" data-testid="stocktake-submit-btn" link
                       type="warning" @click="doSubmit(row)">提交
            </el-button>
            <el-button v-if="canAudit(row)" v-perm="'pharmacy:stocktake:edit'" data-testid="stocktake-audit-pass-btn" link
                       type="success" @click="doAudit(row, true)">过账
            </el-button>
            <el-button v-if="canAudit(row)" v-perm="'pharmacy:stocktake:edit'" link type="info"
                       @click="doAudit(row, false)">退回
            </el-button>
            <el-button v-if="canCount(row)" v-perm="'pharmacy:stocktake:delete'" link type="danger"
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

    <!-- 建单 / 改范围 -->
    <StocktakeEdit v-model="formVisible" :id="editId" :row="editRow" :drug-type-dict="drugTypeDict"
                   @saved="onEditSaved"/>

    <!-- 详情 + 实盘录入 -->
    <StocktakeDetail ref="stocktakeDetailRef" v-model="drawerVisible" :id="detailId"
                     @saved="loadList"
                     @submit="doSubmit"
                     @audit="({ row, pass }) => doAudit(row, pass)"
                     @modify="openModify"/>
  </div>
</template>

<script setup>
/**
 * 药房盘点（T4 / sql/127）
 *
 * 状态机：1盘点中 →（提交）2待复核 →（复核通过）3已过账；账实一致直接 4已关单。
 * **只有「复核通过」这一步动库存**：按明细差异给批次加/减差量并落库存流水（来源 stocktake+盘点单号）。
 * 明细的药品名/规格/批号/成本价是建单那一刻的**快照**，事后字典改名不改历史记载；
 * 状态与差异方向文案由后端算好返回（statusText / diffTypeText），前端不自己翻译。
 *
 * 盘点期间**不冻结库存**（学习阶段与真实系统的差异）：所以差异按快照算、过账按差量叠加，
 * 「复核期间又发了一笔药」既不会被算成盘亏，也不会被快照覆盖掉。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {
  auditStocktake,
  deleteStocktake,
  listStocktakePage,
  submitStocktake,
} from '@/api/pharmacy';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {canAudit, canCount, stocktakeStatusTagType} from '@/lib/stocktake';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import StocktakeEdit from './StocktakeEdit.vue';
import StocktakeDetail from './StocktakeDetail.vue';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  stocktakeNo: '',
  stocktakeTitle: '',
  status: null,
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const statusDict = ref([]);
const drugTypeDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const drugTypeText = (v) => v === null || v === undefined || v === '' ? '全部' : dictLabelText(drugTypeDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.STOCKTAKE_STATUS},${DICT_TYPE.DRUG_TYPE}`);
    statusDict.value = res?.data?.[DICT_TYPE.STOCKTAKE_STATUS] || [];
    drugTypeDict.value = res?.data?.[DICT_TYPE.DRUG_TYPE] || [];
  } catch (e) {
    console.error('加载盘点字典失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listStocktakePage({
      stocktakeNo: query.stocktakeNo?.trim() || undefined,
      stocktakeTitle: query.stocktakeTitle?.trim() || undefined,
      status: query.status ?? undefined,
      dateStart: query.dateRange?.[0] || undefined,
      dateEnd: query.dateRange?.[1] || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    } else {
      ElMessage.error(res.message || '加载盘点单失败');
    }
  } catch (e) {
    ElMessage.error('加载盘点单失败');
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.stocktakeNo = '';
  query.stocktakeTitle = '';
  query.status = null;
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// ==================== 建单 / 改范围（子组件 StocktakeEdit） ====================
const formVisible = ref(false);
const editId = ref(null);
const editRow = ref(null);
const openCreate = () => {
  editId.value = null;
  editRow.value = null;
  formVisible.value = true;
};
const openModify = (row) => {
  editId.value = row.id;
  editRow.value = row;
  formVisible.value = true;
};
const onEditSaved = ({id, isNew}) => {
  loadList();
  if (isNew)
    openDetail(id);
};
// ==================== 详情 + 实盘录入（子组件 StocktakeDetail） ====================
const drawerVisible = ref(false);
const detailId = ref(null);
const stocktakeDetailRef = ref(null);
const openDetail = (rowOrId) => {
  detailId.value = typeof rowOrId === 'object' ? rowOrId.id : rowOrId;
  drawerVisible.value = true;
};
const reloadDrawer = (id) => {
  if (drawerVisible.value)
    stocktakeDetailRef.value?.reload(id);
};
// ==================== 状态机动作（列表行与详情抽屉共用） ====================
const doSubmit = async (row) => {
  try {
    await ElMessageBox.confirm(`提交后不能再改实盘数。当前差异 ${row.diffItems ?? 0} 条（盈 ${row.profitItems ?? 0} / 亏 ${row.lossItems ?? 0}）；` +
        '账实完全一致将直接关单，有差异需第二人复核过账。确认提交盘点单？', '提交确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await submitStocktake(String(row.id));
    if (res.code === 200) {
      ElMessage.success(res.message || '已提交');
      loadList();
      reloadDrawer(res.data);
    } else {
      ElMessage.error(res.message || '提交失败');
    }
  } catch (e) {
    console.error(e);
  }
};
const doAudit = async (row, pass) => {
  let remark = '';
  try {
    const r = await ElMessageBox.prompt(pass
        ? `确认过账？将按 ${row.diffItems ?? 0} 条差异调整批次库存并写库存流水（来源＝本盘点单），过账后单据不可改。`
        : '退回后单据回到「盘点中」，可重录实盘数。请填写退回原因。', pass ? '复核通过' : '复核退回', {
      confirmButtonText: pass ? '确认过账' : '确认退回',
      inputPlaceholder: pass ? '复核意见（可空）' : '退回原因（必填）',
      inputValidator: (v) => (pass || (v && v.trim()) ? true : '退回必须填写原因'),
    });
    remark = (r.value || '').trim();
  } catch {
    return;
  }
  try {
    const res = await auditStocktake({id: String(row.id), pass, remark: remark || undefined});
    if (res.code === 200) {
      ElMessage.success(res.message || '已复核');
      loadList();
      reloadDrawer(res.data);
    } else {
      ElMessage.error(res.message || '复核失败');
    }
  } catch (e) {
    console.error(e);
  }
};
const doDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除盘点单 ${row.stocktakeNo}？只有「盘点中」的单据可删。`, '删除确认', {
      type: 'warning',
    });
  } catch {
    return;
  }
  try {
    const res = await deleteStocktake(String(row.id));
    if (res.code === 200) {
      ElMessage.success('已删除');
      drawerVisible.value = false;
      loadList();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (e) {
    console.error(e);
  }
};
const diffText = (v) => {
  if (v === null || v === undefined)
    return '—';
  const n = Number(v);
  return (n > 0 ? '+' : '') + n;
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
