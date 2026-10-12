<template>
  <el-dialog
      v-model="visible"
      :title="form.id ? '修改退货明细' : '新建退货单'"
      data-testid="sreturn-form-dialog"
      width="1040px"
  >
    <el-form label-width="92px">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="供应商" required>
          <el-select
              v-model="form.supplierId"
              :fit-input-width="false"
              data-testid="sreturn-supplier-select"
              filterable
              placeholder="只列出启用中的供应商"
              @change="onSupplierChange"
          >
            <el-option v-for="s in suppliers" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="原入库单号">
          <el-input v-model="form.srcRefNo" clearable maxlength="50" placeholder="采购/入库单号，人工填的溯源线索"/>
        </el-form-item>
      </div>
      <el-form-item label="退货原因" required>
        <el-input
            v-model="form.returnReason"
            data-testid="sreturn-reason-input"
            maxlength="200"
            placeholder="如：效期不足 6 个月拒收 / 外包装破损 / 召回批次"
            show-word-limit
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" placeholder="承运、发票红冲等安排" type="textarea"/>
      </el-form-item>
    </el-form>

    <div class="mb-2 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-slate-700">可选批次（已挂供应商档案、可用量 &gt; 0）</h3>
      <div class="flex items-center gap-2">
        <el-select v-model="candidateRoom" :fit-input-width="false" clearable placeholder="库位" style="width: 110px"
                   @change="loadCandidates">
          <el-option v-for="d in roomDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
        <el-input
            v-model="candidateKeyword"
            clearable
            data-testid="sreturn-candidate-search"
            placeholder="药品名/编码/批号"
            style="width: 190px"
            @keyup.enter="loadCandidates"
        />
        <el-button :icon="Search" size="small" @click="loadCandidates">搜索</el-button>
      </div>
    </div>
    <el-table v-loading="candidateLoading" :data="visibleCandidates" border data-testid="sreturn-candidate-table" max-height="260"
              size="small">
      <el-table-column label="编码" prop="drugCode" width="95"/>
      <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
      <el-table-column label="规格" prop="specification" show-overflow-tooltip width="100"/>
      <el-table-column label="批号" prop="batchNo" width="95"/>
      <el-table-column label="效期至" prop="expiryDate" width="100"/>
      <el-table-column align="center" label="库位" width="70">
        <template #default="{ row }">{{ row.stockRoomText || roomText(row.stockRoom) }}</template>
      </el-table-column>
      <el-table-column align="right" label="库存/锁定/可用" width="120">
        <template #default="{ row }">
          {{ row.quantity }} / {{ row.lockedQuantity }} /
          <span class="text-emerald-700 font-medium">{{ row.availableQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column align="right" label="成本价" width="80">
        <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="70">
        <template #default="{ row }">
          <el-button data-testid="sreturn-pick-btn" link size="small" type="primary" @click="pickBatch(row)">加入
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="form.supplierId && !visibleCandidates.length && !candidateLoading" class="mt-2 text-sm text-amber-600">
      该供应商名下没有可退批次 —— 批次上的供应商必须是「供货商」而不是生产厂家，未挂档案的批次请先在库存里补 supplier。
    </div>

    <div class="mt-4 mb-2 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-slate-700">退货明细（共 {{ formItems.length }} 个批次）</h3>
      <span class="text-sm text-slate-500">向供应商主张退款（按成本价）：{{ formatMoney(formAmount) }}</span>
    </div>
    <el-table :data="formItems" border data-testid="sreturn-item-table" max-height="260" size="small">
      <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
      <el-table-column label="批号" prop="batchNo" width="95"/>
      <el-table-column label="效期至" prop="expiryDate" width="100"/>
      <el-table-column align="right" label="成本价" width="80">
        <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
      </el-table-column>
      <el-table-column align="right" label="可用量" width="80">
        <template #default="{ row }">{{ row.availableQuantity }}</template>
      </el-table-column>
      <el-table-column align="center" label="退货数量" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :max="row.availableQuantity" :min="0" size="small"/>
        </template>
      </el-table-column>
      <el-table-column label="行备注" min-width="130">
        <template #default="{ row }">
          <el-input v-model="row.remark" maxlength="200" placeholder="可空" size="small"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="70">
        <template #default="{ row }">
          <el-button :icon="Delete" link size="small" type="danger" @click="dropItem(row.stockId)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-alert :closable="false" class="!mt-3" show-icon type="info">
      <template #title>保存只抓快照、不动库存；点列表上的「退货」才真正扣减批次并留下 9-退货出库流水。</template>
    </el-alert>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="formSaving" data-testid="sreturn-form-save" type="primary" @click="saveForm">
        {{ form.id ? '保存明细' : '建单' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 供应商退货建单 / 改明细（合并：row 为空=新建，row.id 存在=改明细）
 * 逻辑自 SupplierReturnView.vue 原样搬出：候选批次筛选、换供应商确认、明细校验、接口、文案均未变。
 * suppliers / roomDict 由父页面按字典与下拉加载（筛选栏与详情抽屉同名），此处只作数据源。
 * 保存成功后把新建/更新后的单据抛回父页面（父页面据此刷列表并重开详情抽屉）。
 */
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete, Search } from '@element-plus/icons-vue';
import {
  getStockBatchCandidates,
  getSupplierReturnDetail,
  upsertSupplierReturn,
} from '@/api/pharmacy';
import { dictLabelText, formatMoney } from '@/lib/utils';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
  initialSupplierId: { type: [Number, String], default: null },
  suppliers: { type: Array, default: () => [] },
  roomDict: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) });
const roomText = (v) => dictLabelText(props.roomDict, v);

const formSaving = ref(false);
const form = reactive({
  id: null,
  supplierId: null,
  returnReason: '',
  srcRefNo: '',
  remark: '',
});
/** 已选明细：一条 = 一个库存批次（stockId 保持字符串，雪花 ID 转 Number 会丢精度） */
const formItems = ref([]);
const candidateLoading = ref(false);
const candidates = ref([]);
const candidateKeyword = ref('');
const candidateRoom = ref(1);
const formAmount = computed(() => formItems.value.reduce((s, it) => s + Number(it.costPrice || 0) * Number(it.quantity || 0), 0));
/**
 * 候选按「本单供应商」再筛一次：后端只保证批次挂了某个供应商，
 * 不知道你要退给谁 —— 混进别家的批次会在保存时被当场拒掉，提前筛掉少一次返工。
 */
const visibleCandidates = computed(() => candidates.value.filter((b) => !form.supplierId || b.supplierId === form.supplierId));
/** 记住上一次确认过的供应商：取消换供应商时要有东西可退回去 */
const lastSupplierId = ref(null);

const loadCandidates = async () => {
  candidateLoading.value = true;
  try {
    const res = await getStockBatchCandidates({
      stockRoom: candidateRoom.value ?? undefined,
      drugName: candidateKeyword.value?.trim() || undefined,
      onlyWithSupplier: true,
    });
    if (res.code === 200) {
      candidates.value = res.data || [];
    } else {
      ElMessage.error(res.message || '加载批次候选失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    candidateLoading.value = false;
  }
};

/** 换供应商 = 能退的批次换了一批，已选行留着只会撞「批次不属于本单供应商」 */
const onSupplierChange = async () => {
  if (formItems.value.length) {
    try {
      await ElMessageBox.confirm(`换供应商后已选的 ${formItems.value.length} 个批次要重新选（批次必须属于本单供应商才能退）。确认换？`, '换供应商确认', {type: 'warning'});
    } catch {
      form.supplierId = lastSupplierId.value;
      return;
    }
  }
  lastSupplierId.value = form.supplierId;
  formItems.value = [];
};
const pickBatch = (batch) => {
  if (formItems.value.some((it) => it.stockId === batch.id)) {
    ElMessage.warning('这个批次已经在明细里了（同一批次一单只能出现一次）');
    return;
  }
  formItems.value.push({
    stockId: batch.id,
    supplierId: batch.supplierId,
    drugCode: batch.drugCode,
    drugName: batch.drugName,
    specification: batch.specification,
    unit: batch.unit,
    batchNo: batch.batchNo,
    expiryDate: batch.expiryDate,
    costPrice: Number(batch.costPrice || 0),
    availableQuantity: Number(batch.availableQuantity || 0),
    quantity: Number(batch.availableQuantity || 0),
    remark: '',
  });
};
const dropItem = (stockId) => {
  formItems.value = formItems.value.filter((it) => it.stockId !== stockId);
};

watch(visible, async (open) => {
  if (!open) return;
  const row = props.row;
  if (row && row.id) {
    // 改明细：列表行不带明细（主单分页只返回汇总），先把详情捞回来
    let full = row;
    if (!full.items) {
      try {
        const res = await getSupplierReturnDetail(full.id);
        if (res.code !== 200 || !res.data) {
          ElMessage.error(res.message || '加载明细失败，请重试');
          visible.value = false;
          return;
        }
        full = res.data;
      } catch (e) {
        console.error(e);
        visible.value = false;
        return;
      }
    }
    form.supplierId = full.supplierId;
    lastSupplierId.value = full.supplierId;
    candidateKeyword.value = '';
    await loadCandidates();
    const availMap = (id) => {
      const hit = candidates.value.find((b) => b.id === id);
      return hit ? Number(hit.availableQuantity || 0) : 0;
    };
    form.id = full.id;
    form.returnReason = full.returnReason || '';
    form.srcRefNo = full.srcRefNo || '';
    form.remark = full.remark || '';
    formItems.value = (full.items || []).map((it) => ({
      stockId: it.stockId,
      supplierId: it.supplierId,
      drugCode: it.drugCode,
      drugName: it.drugName,
      specification: it.specification,
      unit: it.unit,
      batchNo: it.batchNo,
      expiryDate: it.expiryDate,
      costPrice: Number(it.costPrice || 0),
      // 明细上的 currentQuantity 是「数量」不含锁定量，上限一律以候选的可用量为准
      availableQuantity: availMap(it.stockId) || Number(it.currentQuantity || 0),
      quantity: Number(it.quantity || 0),
      remark: it.remark || '',
    }));
  } else {
    // 新建：供应商默认沿用列表筛选条件
    form.id = null;
    form.supplierId = props.initialSupplierId || null;
    lastSupplierId.value = form.supplierId;
    form.returnReason = '';
    form.srcRefNo = '';
    form.remark = '';
    formItems.value = [];
    candidateKeyword.value = '';
    loadCandidates();
  }
});

const saveForm = async () => {
  if (!form.supplierId) {
    ElMessage.warning('请选择供应商');
    return;
  }
  if (!form.returnReason.trim()) {
    ElMessage.warning('请填写退货原因');
    return;
  }
  if (!formItems.value.length) {
    ElMessage.warning('请至少从候选里点一个批次');
    return;
  }
  const bad = formItems.value.find((it) => !(Number(it.quantity) > 0));
  if (bad) {
    ElMessage.warning(`批次「${bad.drugName} / ${bad.batchNo}」的退货数量必须大于 0`);
    return;
  }
  const over = formItems.value.find((it) => Number(it.quantity) > it.availableQuantity);
  if (over) {
    ElMessage.warning(`批次「${over.drugName} / ${over.batchNo}」可用量只有 ${over.availableQuantity}，不够退 ${over.quantity}`);
    return;
  }
  formSaving.value = true;
  try {
    const res = await upsertSupplierReturn({
      id: form.id || undefined,
      supplierId: form.supplierId,
      returnReason: form.returnReason.trim(),
      srcRefNo: form.srcRefNo?.trim() || undefined,
      remark: form.remark?.trim() || undefined,
      items: formItems.value.map((it) => ({
        stockId: it.stockId,
        quantity: Number(it.quantity),
        remark: it.remark?.trim() || undefined,
      })),
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存');
      visible.value = false;
      emit('saved', res.data);
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    formSaving.value = false;
  }
};
</script>
