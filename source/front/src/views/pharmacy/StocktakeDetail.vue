<template>
  <!-- 详情 + 实盘录入 -->
  <el-drawer v-model="visible" data-testid="stocktake-detail-drawer" size="1000px" title="盘点单详情">
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
                     @click="emit('submit', detail)">提交
          </el-button>
          <el-button v-if="canAudit(detail)" v-perm="'pharmacy:stocktake:edit'" size="small" type="success"
                     @click="emit('audit', { row: detail, pass: true })">复核过账
          </el-button>
          <el-button v-if="canAudit(detail)" v-perm="'pharmacy:stocktake:edit'" size="small"
                     @click="emit('audit', { row: detail, pass: false })">退回
          </el-button>
          <el-button v-if="editable" v-perm="'pharmacy:stocktake:add'" size="small" @click="emit('modify', detail)">
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
</template>

<script setup>
/**
 * 盘点单详情 + 实盘录入抽屉 —— 从 views/pharmacy/StocktakeView.vue 抽出
 *
 * 状态机：1盘点中 →（提交）2待复核 →（复核通过）3已过账；账实一致直接 4已关单。
 * 只有「复核通过」这一步动库存（服务端按明细差异加减差量并落流水）。这里只做：
 *   - 逐批录实盘数（脏行才提交，未出现的明细服务端保持「未录入」）；
 *   - 提交 / 复核过账 / 退回 / 改范围 均交回父页跑对应的状态机动作（列表行也在用同一份）。
 * 明细的药品名/规格/批号/成本价是建单那一刻的快照，后端算好返回，前端不自己翻译。
 * 说明：本抽屉按需求保留内部分页（明细行数多时限高 + 分页），门禁会 WARN 子组件含 el-pagination，属预期。
 */
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getStocktakeDetail, saveStocktakeCount } from '@/api/pharmacy';
import { formatMoney } from '@/lib/utils';
import { canAudit, canCount, postedText, stocktakeStatusTagType } from '@/lib/stocktake';
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
});
const emit = defineEmits(['update:modelValue', 'saved', 'submit', 'audit', 'modify']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const saving = ref(false);
// itemId -> { countedQuantity, remark }：只把动过的行提交，未出现的明细在服务端保持「未录入」
const editMap = reactive({});
const itemPage = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE });
const items = computed(() => detail.value?.items || []);
const pagedItems = computed(() => {
  const start = (itemPage.pageNum - 1) * itemPage.pageSize;
  return items.value.slice(start, start + itemPage.pageSize);
});
const uncountedCount = computed(() => items.value.filter((it) => it.countedQuantity === null || it.countedQuantity === undefined).length);
const editable = computed(() => detail.value && canCount(detail.value));
const getEdit = (id) => {
  if (!editMap[id])
    editMap[id] = { countedQuantity: null, remark: '' };
  return editMap[id];
};
const dirtyCount = computed(() => Object.values(editMap).filter((v) => v.countedQuantity !== null && v.countedQuantity !== undefined).length);
const diffText = (v) => {
  if (v === null || v === undefined)
    return '—';
  const n = Number(v);
  return (n > 0 ? '+' : '') + n;
};
const fillBookAsCounted = () => {
  items.value.forEach((it) => {
    if (it.countedQuantity === null || it.countedQuantity === undefined) {
      getEdit(it.id).countedQuantity = Number(it.bookQuantity);
    }
  });
  ElMessage.success('已把未录入行的账面数填为实盘数，请逐批核对后修改差异行');
};
const load = async (id) => {
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
          editMap[it.id] = { countedQuantity: Number(it.countedQuantity), remark: it.remark || '' };
        }
      });
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
    const res = await saveStocktakeCount({ id: detail.value.id, items: list });
    if (res.code === 200) {
      ElMessage.success('实盘数已保存');
      await load(res.data);
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};

watch(() => props.modelValue, (v) => {
  if (v && props.id) load(props.id);
});
watch(() => props.id, (id) => {
  if (props.modelValue && id) load(id);
});

defineExpose({ reload: load });
</script>
