<template>
  <el-dialog
      v-model="visible"
      :title="form.id ? '修改调拨明细' : '新建调拨单'"
      data-testid="transfer-form-dialog"
      width="1040px"
  >
    <el-form label-width="80px">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="调拨方向" required>
          <el-select v-model="form.transferType" :fit-input-width="false" data-testid="transfer-type-select"
                     @change="onTypeChange">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="库位走向">
          <span class="text-sm text-slate-600">{{ fromRoomLabel }} → {{ toRoomLabel }}（按方向定死，不能自选）</span>
        </el-form-item>
      </div>
      <el-form-item label="调拨事由" required>
        <el-input
            v-model="form.reason"
            data-testid="transfer-reason-input"
            maxlength="200"
            placeholder="如：药房本周拆零用量增大，从药库下拨 20 批"
            show-word-limit
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" placeholder="参与人、运输条件等" type="textarea"/>
      </el-form-item>
    </el-form>

    <div class="mb-2 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-slate-700">
        可选批次（{{ fromRoomLabel }}可用量 &gt; 0，按效期先到排）
      </h3>
      <div class="flex items-center gap-2">
        <el-input
            v-model="candidateKeyword"
            clearable
            data-testid="transfer-candidate-search"
            placeholder="药品名/编码/批号"
            style="width: 200px"
            @keyup.enter="loadCandidates"
        />
        <el-button :icon="Search" size="small" @click="loadCandidates">搜索</el-button>
      </div>
    </div>
    <el-table v-loading="candidateLoading" :data="candidates" border data-testid="transfer-candidate-table" max-height="260"
              size="small">
      <el-table-column label="编码" prop="drugCode" width="95"/>
      <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
      <el-table-column label="规格" prop="specification" show-overflow-tooltip width="100"/>
      <el-table-column label="批号" prop="batchNo" width="95"/>
      <el-table-column label="效期至" prop="expiryDate" width="100"/>
      <el-table-column align="right" label="库存/锁定/可用" width="125">
        <template #default="{ row }">
          {{ row.quantity }} / {{ row.lockedQuantity }} /
          <span class="text-emerald-700 font-medium">{{ row.availableQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column align="right" label="成本价" width="80">
        <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="80">
        <template #default="{ row }">
          <el-button data-testid="transfer-pick-btn" link size="small" type="primary" @click="pickBatch(row)">加入
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-4 mb-2 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-slate-700">调拨明细（共 {{ formItems.length }} 个批次）</h3>
      <span class="text-sm text-slate-500">金额按批次成本价现算：{{ formatMoney(formAmount) }}</span>
    </div>
    <el-table :data="formItems" border data-testid="transfer-item-table" max-height="260" size="small">
      <el-table-column label="药品" min-width="120" prop="drugName" show-overflow-tooltip/>
      <el-table-column label="批号" prop="batchNo" width="95"/>
      <el-table-column label="效期至" prop="expiryDate" width="100"/>
      <el-table-column align="right" label="成本价" width="80">
        <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
      </el-table-column>
      <el-table-column align="right" label="可用量" width="80">
        <template #default="{ row }">{{ row.availableQuantity }}</template>
      </el-table-column>
      <el-table-column align="center" label="调拨数量" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.applyQuantity" :max="row.availableQuantity" :min="0" size="small"/>
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
      <template #title>保存只抓快照、不动库存；点列表上的「发出」才开始扣减，「接收」才算搬完。</template>
    </el-alert>

    <template #footer>
      <el-button :loading="formSaving" data-testid="transfer-form-save" type="primary" @click="saveForm">
        {{ form.id ? '保存明细' : '建单' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 药品库房调拨 —— 建单 / 改明细（拆自 DrugTransferView）
 *
 * 方向决定发出库位（fromRoomOf），换方向必须清空已选批次；候选批次、校验、保存都在本组件内完成。
 * 保存成功 emit('saved', 单据对象) 由父页面刷新列表并打开详情。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Search} from '@element-plus/icons-vue';
import {getDrugTransferDetail, getStockBatchCandidates, upsertDrugTransfer} from '@/api/pharmacy';
import {formatMoney} from '@/lib/utils';
import {fromRoomOf, STOCK_ROOM} from '@/lib/drugTransfer';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 改明细传入的调拨单（null 表示新建） */
  row: {type: Object, default: null},
  /** 调拨方向字典（父页面加载一次共用） */
  typeDict: {type: Array, default: () => []}
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
});

const formSaving = ref(false);
const form = reactive({
  id: null,
  transferType: 1,
  reason: '',
  remark: '',
});
/** 已选明细：一条 = 一个发出方批次（stockId 保持字符串，雪花 ID 转 Number 会丢精度） */
const formItems = ref([]);
const candidateLoading = ref(false);
const candidates = ref([]);
const candidateKeyword = ref('');
/** 记住上一次确认过的方向：取消换方向时要有东西可退回去 */
const lastType = ref(1);
const fromRoom = computed(() => fromRoomOf(form.transferType));
const fromRoomLabel = computed(() => fromRoom.value === STOCK_ROOM.PHARMACY ? '药房' : '药库');
const toRoomLabel = computed(() => fromRoom.value === STOCK_ROOM.PHARMACY ? '药库' : '药房');
const formAmount = computed(() => formItems.value.reduce((s, it) => s + Number(it.costPrice || 0) * Number(it.applyQuantity || 0), 0));

const loadCandidates = async () => {
  candidateLoading.value = true;
  try {
    const res = await getStockBatchCandidates({
      stockRoom: fromRoom.value,
      drugName: candidateKeyword.value?.trim() || undefined,
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

/** 批次当前可用量以候选为准：明细上的 fromQuantity 是「数量」不含锁定量，拿它当上限会把患者的药调走 */
const availableOf = (stockId, fallback) => {
  const hit = candidates.value.find((b) => b.id === stockId);
  return hit ? Number(hit.availableQuantity || 0) : Number(fallback || 0);
};

/** 换方向 = 发出库位变了，已选批次全部失效；留着只会让后端按库位校验当场拒掉 */
const onTypeChange = async () => {
  if (formItems.value.length) {
    try {
      await ElMessageBox.confirm(`换方向后发出库位变成「${fromRoomLabel.value}」，已选的 ${formItems.value.length} 个批次要重新选。确认换？`, '换方向确认', {type: 'warning'});
    } catch {
      // 取消则把选择退回原方向：不能留下「方向已换、明细还是老库位」的半态
      form.transferType = lastType.value;
      return;
    }
  }
  lastType.value = form.transferType;
  formItems.value = [];
  loadCandidates();
};

const pickBatch = (batch) => {
  if (formItems.value.some((it) => it.stockId === batch.id)) {
    ElMessage.warning('这个批次已经在明细里了（同一批次一单只能出现一次）');
    return;
  }
  formItems.value.push({
    stockId: batch.id,
    drugCode: batch.drugCode,
    drugName: batch.drugName,
    specification: batch.specification,
    unit: batch.unit,
    batchNo: batch.batchNo,
    expiryDate: batch.expiryDate,
    costPrice: Number(batch.costPrice || 0),
    availableQuantity: Number(batch.availableQuantity || 0),
    applyQuantity: Number(batch.availableQuantity || 0),
    remark: '',
  });
};

const dropItem = (stockId) => {
  formItems.value = formItems.value.filter((it) => it.stockId !== stockId);
};

// 打开：新建给空单，改明细先把主单详情（含 items）捞回来再回填
watch(visible, async (v) => {
  if (!v) return;
  candidateKeyword.value = '';
  if (!props.row) {
    form.id = null;
    form.transferType = 1;
    lastType.value = 1;
    form.reason = '';
    form.remark = '';
    formItems.value = [];
    loadCandidates();
    return;
  }
  // 列表行不带明细（主单分页只返回汇总），改明细前先把详情捞回来
  let full = props.row;
  if (!full.items) {
    try {
      const res = await getDrugTransferDetail(full.id);
      if (res.code !== 200 || !res.data) {
        ElMessage.error(res.message || '加载明细失败，请重试');
        emit('update:modelValue', false);
        return;
      }
      full = res.data;
    } catch (e) {
      console.error(e);
      ElMessage.error('加载明细失败，请重试');
      emit('update:modelValue', false);
      return;
    }
  }
  await loadCandidates();
  form.id = full.id;
  form.transferType = Number(full.transferType);
  lastType.value = form.transferType;
  form.reason = full.reason || '';
  form.remark = full.remark || '';
  formItems.value = (full.items || []).map((it) => ({
    stockId: it.stockId,
    drugCode: it.drugCode,
    drugName: it.drugName,
    specification: it.specification,
    unit: it.unit,
    batchNo: it.batchNo,
    expiryDate: it.expiryDate,
    costPrice: Number(it.costPrice || 0),
    availableQuantity: availableOf(it.stockId, it.fromQuantity),
    applyQuantity: Number(it.applyQuantity || 0),
    remark: it.remark || '',
  }));
  candidateKeyword.value = '';
});

const saveForm = async () => {
  if (!form.reason.trim()) {
    ElMessage.warning('请填写调拨事由');
    return;
  }
  if (!formItems.value.length) {
    ElMessage.warning('请至少从候选里点一个批次');
    return;
  }
  const bad = formItems.value.find((it) => !(Number(it.applyQuantity) > 0));
  if (bad) {
    ElMessage.warning(`批次「${bad.drugName} / ${bad.batchNo}」的调拨数量必须大于 0`);
    return;
  }
  const over = formItems.value.find((it) => Number(it.applyQuantity) > it.availableQuantity);
  if (over) {
    ElMessage.warning(`批次「${over.drugName} / ${over.batchNo}」可用量只有 ${over.availableQuantity}，不够调 ${over.applyQuantity}`);
    return;
  }
  formSaving.value = true;
  try {
    const res = await upsertDrugTransfer({
      id: form.id || undefined,
      transferType: form.transferType,
      reason: form.reason.trim(),
      remark: form.remark?.trim() || undefined,
      items: formItems.value.map((it) => ({
        stockId: it.stockId,
        applyQuantity: Number(it.applyQuantity),
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
