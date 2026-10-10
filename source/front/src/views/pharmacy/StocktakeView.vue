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
    <el-dialog v-model="formVisible" :title="form.id ? '修改盘点单' : '新建盘点单'" data-testid="stocktake-form-dialog"
               width="620px">
      <el-form label-width="92px">
        <el-form-item label="盘点主题" required>
          <el-input v-model="form.stocktakeTitle" data-testid="stocktake-title-input" maxlength="100" placeholder="如：药房 9 月西药专项盘点"
                    show-word-limit/>
        </el-form-item>
        <el-form-item label="药品类型">
          <el-select v-model="form.scopeDrugType" :fit-input-width="false" clearable data-testid="stocktake-drugtype-select"
                     placeholder="全部类型">
            <el-option v-for="d in drugTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="form.scopeKeyword" clearable data-testid="stocktake-keyword-input" maxlength="50"
                    placeholder="药品名称/编码/批号，留空不限"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" placeholder="盘点依据、参与人等" type="textarea"/>
        </el-form-item>
        <el-alert :closable="false" class="!mt-1" show-icon type="info">
          <template #title>范围留空 = 全盘所有库存批次。建单即抓「药品×批号」的账面数快照，之后库存再变也不改快照。
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button :loading="formSaving" data-testid="stocktake-form-save" type="primary" @click="saveForm">
          {{ form.id ? '保存' : '建单并抓快照' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 + 实盘录入 -->
    <el-drawer v-model="drawerVisible" data-testid="stocktake-detail-drawer" size="1000px" title="盘点单详情">
      <template v-if="detail">
        <div class="mb-3 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <el-tag :type="stocktakeStatusTagType(detail.status)">{{ detail.statusText }}</el-tag>
            <span class="text-sm text-slate-500">{{ detail.scopeDesc }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-button v-if="editable" v-perm="'pharmacy:stocktake:add'" size="small" @click="fillBookAsCounted">
              未录入行填账面数
            </el-button>
            <el-button v-if="editable" v-perm="'pharmacy:stocktake:add'" :loading="saving" data-testid="stocktake-savecount-btn" size="small"
                       type="primary" @click="doSaveCount">
              保存实盘数{{ dirtyCount ? `（已改 ${dirtyCount} 行）` : '' }}
            </el-button>
            <el-button v-if="editable" v-perm="'pharmacy:stocktake:edit'" size="small" type="warning"
                       @click="doSubmit(detail)">提交
            </el-button>
            <el-button v-if="canAudit(detail)" v-perm="'pharmacy:stocktake:edit'" size="small" type="success"
                       @click="doAudit(detail, true)">复核过账
            </el-button>
            <el-button v-if="canAudit(detail)" v-perm="'pharmacy:stocktake:edit'" size="small"
                       @click="doAudit(detail, false)">退回
            </el-button>
            <el-button v-if="editable" v-perm="'pharmacy:stocktake:add'" size="small" @click="openModify(detail)">
              改范围
            </el-button>
          </div>
        </div>

        <el-descriptions :column="3" border class="mb-4" size="small">
          <el-descriptions-item label="盘点单号">{{ detail.stocktakeNo }}</el-descriptions-item>
          <el-descriptions-item label="主题">{{ detail.stocktakeTitle }}</el-descriptions-item>
          <el-descriptions-item label="账面快照时点">{{ detail.snapshotTime }}</el-descriptions-item>
          <el-descriptions-item label="批次数">{{ detail.totalItems }}</el-descriptions-item>
          <el-descriptions-item label="已录实盘">{{ detail.countedItems }}</el-descriptions-item>
          <el-descriptions-item label="未录入">
            <span :class="uncountedCount ? 'text-amber-600 font-semibold' : ''">{{ uncountedCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="差异批次数">{{ detail.diffItems }}（盈 {{ detail.profitItems }} / 亏
            {{ detail.lossItems }}）
          </el-descriptions-item>
          <el-descriptions-item label="净差数量">{{ diffText(detail.diffQuantity) }}</el-descriptions-item>
          <el-descriptions-item label="净差金额">{{ formatMoney(detail.diffAmount) }}</el-descriptions-item>
          <el-descriptions-item label="制单人">{{ detail.createBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ detail.submitBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复核人">{{ detail.auditBy || '—' }}</el-descriptions-item>
          <el-descriptions-item :span="3" label="复核意见 / 退回原因">{{
              detail.auditRemark || '—'
            }}
          </el-descriptions-item>
          <el-descriptions-item :span="3" label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="pagedItems" border data-testid="stocktake-item-table" size="small">
          <el-table-column label="编码" prop="drugCode" width="95"/>
          <el-table-column label="药品" min-width="130" prop="drugName" show-overflow-tooltip/>
          <el-table-column label="规格" prop="specification" show-overflow-tooltip width="100"/>
          <el-table-column label="批号" prop="batchNo" width="95"/>
          <el-table-column label="效期至" prop="expiryDate" width="100"/>
          <el-table-column align="right" label="成本价" width="80">
            <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
          </el-table-column>
          <el-table-column align="right" label="账面数" width="80">
            <template #default="{ row }">{{ row.bookQuantity }}</template>
          </el-table-column>
          <el-table-column align="right" label="当前余额" width="85">
            <template #default="{ row }">
              <span :class="Number(row.currentQuantity) !== Number(row.bookQuantity) ? 'text-amber-600' : ''">
                {{ row.currentQuantity ?? '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="实盘数" width="130">
            <template #default="{ row }">
              <el-input-number
                  v-if="editable"
                  v-model="getEdit(row.id).countedQuantity"
                  :controls="false"
                  :data-testid="`stocktake-count-${row.drugCode}`"
                  :min="0"
                  :precision="2"
                  class="!w-24"
              />
              <span v-else>{{ row.countedQuantity ?? '未录入' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="差异" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.diffTypeText && row.diffTypeText !== '未录入'" :type="Number(row.diffQuantity) > 0 ? 'danger' : 'success'"
                      size="small">
                {{ row.diffTypeText }} {{ diffText(row.diffQuantity) }}
              </el-tag>
              <span v-else class="text-slate-400">{{ row.diffTypeText || '未录入' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="差异金额" width="90">
            <template #default="{ row }">{{ row.diffAmount === null ? '—' : formatMoney(row.diffAmount) }}</template>
          </el-table-column>
          <el-table-column align="center" label="过账" width="80">
            <template #default="{ row }">{{ postedText(row.posted) }}</template>
          </el-table-column>
          <el-table-column label="差异说明" min-width="150">
            <template #default="{ row }">
              <el-input v-if="editable" v-model="getEdit(row.id).remark" maxlength="500" placeholder="盈亏原因"/>
              <span v-else>{{ row.remark || '—' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
            v-model:current-page="itemPage.pageNum"
            v-model:page-size="itemPage.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="items.length"
            class="mt-3 justify-end"
            layout="total, prev, pager, next, sizes"
        />

        <template v-if="detail.logs && detail.logs.length">
          <h3 class="mt-5 mb-2 text-sm font-semibold text-slate-700">本次过账的库存流水（账实差异的可追溯证据）</h3>
          <el-table :data="detail.logs" border data-testid="stocktake-log-table" size="small">
            <el-table-column label="药品" min-width="130" prop="drugName" show-overflow-tooltip/>
            <el-table-column label="批号" prop="batchNo" width="100"/>
            <el-table-column align="center" label="类型" width="80">
              <template #default="{ row }">{{ Number(row.changeType) === 5 ? '盘盈' : '盘亏' }}</template>
            </el-table-column>
            <el-table-column align="right" label="变动数量" width="90">
              <template #default="{ row }">{{ diffText(row.changeQuantity) }}</template>
            </el-table-column>
            <el-table-column align="right" label="变动前" prop="quantityBefore" width="80"/>
            <el-table-column align="right" label="变动后" prop="quantityAfter" width="80"/>
            <el-table-column label="来源单号" prop="sourceNo" width="150"/>
            <el-table-column label="操作人" prop="operatorName" width="90"/>
            <el-table-column label="时间" prop="createTime" width="150"/>
            <el-table-column label="原因" min-width="200" prop="remark" show-overflow-tooltip/>
          </el-table>
        </template>
      </template>
    </el-drawer>
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
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {
  auditStocktake,
  deleteStocktake,
  getStocktakeDetail,
  listStocktakePage,
  saveStocktakeCount,
  submitStocktake,
  upsertStocktake,
} from '@/api/pharmacy';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {canAudit, canCount, postedText, stocktakeStatusTagType} from '@/lib/stocktake';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

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
// ==================== 建单 / 改范围 ====================
const formVisible = ref(false);
const formSaving = ref(false);
const form = reactive({
  id: null,
  stocktakeTitle: '',
  scopeDrugType: null,
  scopeKeyword: '',
  remark: '',
});
const openCreate = () => {
  form.id = null;
  form.stocktakeTitle = `药房库存盘点 ${new Date().toISOString().slice(0, 10)}`;
  form.scopeDrugType = null;
  form.scopeKeyword = '';
  form.remark = '';
  formVisible.value = true;
};
const saveForm = async () => {
  if (!form.stocktakeTitle.trim()) {
    ElMessage.warning('请填写盘点主题');
    return;
  }
  // 改范围会重抓快照并清空已录入的实盘数 —— 破坏性，必须二次确认
  if (form.id && !!(form.scopeDrugType || form.scopeKeyword?.trim())) {
    try {
      await ElMessageBox.confirm('修改盘点范围会重新抓取账面快照，并清空本单已录入的实盘数。确认继续？', '重抓快照确认', {
        type: 'warning',
        confirmButtonText: '确认重抓'
      });
    } catch {
      return;
    }
  }
  formSaving.value = true;
  try {
    const res = await upsertStocktake({
      id: form.id || undefined,
      stocktakeTitle: form.stocktakeTitle.trim(),
      scopeDrugType: form.scopeDrugType ?? undefined,
      scopeKeyword: form.scopeKeyword?.trim() || undefined,
      remark: form.remark?.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存');
      formVisible.value = false;
      loadList();
      if (!form.id)
        openDetail(res.data);
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    formSaving.value = false;
  }
};
// ==================== 详情 + 实盘录入 ====================
const drawerVisible = ref(false);
const detail = ref(null);
const saving = ref(false);
// itemId -> { countedQuantity, remark }：只把动过的行提交，未出现的明细在服务端保持「未录入」
const editMap = reactive({});
const itemPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const items = computed(() => detail.value?.items || []);
const pagedItems = computed(() => {
  const start = (itemPage.pageNum - 1) * itemPage.pageSize;
  return items.value.slice(start, start + itemPage.pageSize);
});
const uncountedCount = computed(() => items.value.filter((it) => it.countedQuantity === null || it.countedQuantity === undefined).length);
const editable = computed(() => detail.value && canCount(detail.value));
const getEdit = (id) => {
  if (!editMap[id])
    editMap[id] = {countedQuantity: null, remark: ''};
  return editMap[id];
};
const dirtyCount = computed(() => Object.values(editMap).filter((v) => v.countedQuantity !== null && v.countedQuantity !== undefined).length);
const fillBookAsCounted = () => {
  items.value.forEach((it) => {
    if (it.countedQuantity === null || it.countedQuantity === undefined) {
      getEdit(it.id).countedQuantity = Number(it.bookQuantity);
    }
  });
  ElMessage.success('已把未录入行的账面数填为实盘数，请逐批核对后修改差异行');
};
const openDetail = async (rowOrId) => {
  const id = typeof rowOrId === 'object' ? rowOrId.id : rowOrId;
  try {
    const res = await getStocktakeDetail(id);
    if (res.code === 200) {
      detail.value = res.data;
      Object.keys(editMap).forEach((k) => delete editMap[k]);
      itemPage.pageNum = 1;
      // 上一行不带分号，紧跟以 ( 开头的语句会被解析成 1(...)（ASI 陷阱），故先取名
      const snapshotItems = res.data?.items || [];
      snapshotItems.forEach((it) => {
        if (it.countedQuantity !== null && it.countedQuantity !== undefined) {
          editMap[it.id] = {countedQuantity: Number(it.countedQuantity), remark: it.remark || ''};
        }
      });
      drawerVisible.value = true;
    } else {
      ElMessage.error(res.message || '加载详情失败');
    }
  } catch (e) {
    console.error(e);
  }
};
const doSaveCount = async () => {
  const list = Object.entries(editMap)
      .filter(([, v]) => v.countedQuantity !== null && v.countedQuantity !== undefined)
      .map(([itemId, v]) => ({
        itemId,
        countedQuantity: v.countedQuantity,
        remark: v.remark?.trim() || undefined,
      }));
  if (!list.length) {
    ElMessage.warning('请先录入实盘数');
    return;
  }
  saving.value = true;
  try {
    const res = await saveStocktakeCount({id: detail.value.id, items: list});
    if (res.code === 200) {
      ElMessage.success('实盘数已保存');
      await openDetail(res.data);
      loadList();
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
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
      if (drawerVisible.value)
        await openDetail(res.data);
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
      if (drawerVisible.value)
        await openDetail(res.data);
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
const openModify = (row) => {
  form.id = row.id;
  form.stocktakeTitle = row.stocktakeTitle;
  form.scopeDrugType = row.scopeDrugType ?? null;
  form.scopeKeyword = row.scopeKeyword || '';
  form.remark = row.remark || '';
  formVisible.value = true;
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
