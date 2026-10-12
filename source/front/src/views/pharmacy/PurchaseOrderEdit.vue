<template>
  <!-- 新建/编辑弹窗 -->
  <el-dialog
      v-model="visible"
      :title="orderId ? '编辑采购订单' : '新建采购订单'"
      data-testid="order-dialog"
      top="5vh"
      width="980px"
  >
    <div class="flex gap-4 mb-3">
      <el-form-item class="flex-1 mb-0" label="供应商" label-width="80px" required>
        <el-select v-model="form.supplierId" :fit-input-width="false" data-testid="order-supplier" filterable
                   placeholder="选择供应商" style="width: 100%">
          <el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId"/>
        </el-select>
      </el-form-item>
      <el-form-item class="mb-0" label="下单时间" label-width="80px">
        <el-date-picker v-model="form.orderTime" style="width: 200px" type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
    </div>

    <el-table :data="items" border data-testid="order-item-table" size="small">
      <el-table-column label="药品" min-width="220">
        <template #default="{ row }">
          <el-select
              v-model="row.drugId"
              :fit-input-width="false"
              filterable
              placeholder="搜药名/编码/规格"
              style="width: 100%"
          >
            <el-option
                v-for="d in drugOptions"
                :key="d.id"
                :label="`${d.drugName}（${d.specification || '—'}）`"
                :value="d.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="批号" width="130">
        <template #default="{ row }">
          <el-input v-model="row.batchNo" placeholder="批号"/>
        </template>
      </el-table-column>
      <el-table-column label="生产日期" width="150">
        <template #default="{ row }">
          <el-date-picker v-model="row.productionDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </template>
      </el-table-column>
      <el-table-column label="有效期至" width="150">
        <template #default="{ row }">
          <el-date-picker v-model="row.expiryDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="110">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="0" :precision="0" controls-position="right"
                           style="width: 100%"/>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="130">
        <template #default="{ row }">
          <el-input-number v-model="row.unitPrice" :min="0" :precision="2" controls-position="right"
                           style="width: 100%"/>
        </template>
      </el-table-column>
      <el-table-column align="right" label="小计（预估）" width="110">
        <template #default="{ row }">
          {{ formatMoney((Number(row.quantity) || 0) * (Number(row.unitPrice) || 0)) }}
        </template>
      </el-table-column>
      <el-table-column label="" width="60">
        <template #default="{ $index }">
          <el-button v-perm="['pharmacy:purchase:add', 'pharmacy:purchase:edit']" link type="danger"
                     @click="items.splice($index, 1)">删
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex items-center justify-between mt-3">
      <el-button v-perm="['pharmacy:purchase:add', 'pharmacy:purchase:edit']" data-testid="order-item-add"
                 size="small" @click="items.push(emptyItem())">+ 加一行明细
      </el-button>
      <div class="text-sm text-slate-600">
        预估总额：<span class="font-semibold text-slate-800">¥{{ estimatedTotal }}</span>
        <span class="text-slate-400 ml-2">（以服务端重算为准）</span>
      </div>
    </div>

    <el-form-item class="mt-3 mb-0" label="备注" label-width="80px">
      <el-input v-model="form.remark" :rows="2" type="textarea"/>
    </el-form-item>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="['pharmacy:purchase:add', 'pharmacy:purchase:edit']" :loading="saving" data-testid="order-save-btn"
                 type="primary" @click="save">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 采购订单 新建/编辑弹窗 —— 从 views/pharmacy/PurchaseOrderView.vue 抽出
 *
 * 金额口径不变：明细 amount、单头总额全部服务端重算，前端传的金额后端不采信；
 * 这里的价格 × 数量只是给操作人看的预估。supplierOptions 由父页加载后作 prop 传入（列表筛选共用）。
 * 药品目录惰性加载：打开弹窗才拉（列表页不背这 545 条）。
 */
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getPurchaseOrderDetail, upsertPurchaseOrder } from '@/api/purchase';
import { getDrugSelectList } from '@/api/system';
import { formatMoney } from '@/lib/utils';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderId: { type: [Number, String], default: null },
  supplierOptions: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({supplierId: null, orderTime: '', remark: ''});
const items = ref([]);
// 药品下拉：一次性拉全量（院内药品目录量级可控），用 EP 默认 label 过滤
const drugOptions = ref([]);
const loadDrugs = async () => {
  try {
    const res = await getDrugSelectList({});
    if (res.code === 200)
      drugOptions.value = res.data || [];
  } catch (e) {
    console.error('加载药品目录失败', e);
  }
};
const emptyItem = () => ({
  drugId: null,
  drugLabel: '',
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  quantity: null,
  unitPrice: null,
  remark: '',
});
const estimatedTotal = computed(() => (items.value || [])
    .reduce((s, i) => s + (Number(i.quantity) || 0) * (Number(i.unitPrice) || 0), 0)
    .toFixed(2));

const initCreate = () => {
  form.supplierId = null;
  form.orderTime = '';
  form.remark = '';
  items.value = [emptyItem()];
};
const initEdit = async (orderId) => {
  try {
    const res = await getPurchaseOrderDetail(orderId);
    if (res.code !== 200 || !res.data) {
      ElMessage.error(res.message || '加载订单详情失败');
      return;
    }
    const d = res.data;
    form.supplierId = d.supplierId;
    form.orderTime = d.orderTime || '';
    form.remark = d.remark || '';
    items.value = (d.items || []).map((x) => ({
      drugId: x.drugId,
      drugLabel: `${x.drugName}（${x.specification || '—'}）`,
      batchNo: x.batchNo || '',
      productionDate: x.productionDate || '',
      expiryDate: x.expiryDate || '',
      quantity: Number(x.quantity),
      unitPrice: Number(x.unitPrice),
      remark: x.remark || '',
    }));
    if (!items.value.length)
      items.value = [emptyItem()];
  } catch (e) {
    ElMessage.error('加载订单详情失败');
    console.error(e);
  }
};
const validateForm = () => {
  if (!form.supplierId)
    return '请选择供应商';
  const valid = items.value.filter((i) => i.drugId && Number(i.quantity) > 0 && i.unitPrice !== null && Number(i.unitPrice) >= 0);
  if (!valid.length)
    return '至少要有一条完整明细（药品 + 数量 + 单价）';
  if (valid.length !== items.value.length)
    return '存在未填完整的明细行，请删除或补全';
  const seen = new Set();
  for (const i of valid) {
    const key = `${i.drugId}|${(i.batchNo || '').trim()}`;
    if (seen.has(key))
      return '同一药品同一批号在一行内不能重复';
    seen.add(key);
    if (i.productionDate && i.expiryDate && i.expiryDate < i.productionDate) {
      return '有效期不能早于生产日期';
    }
  }
  return null;
};
const save = async () => {
  const err = validateForm();
  if (err) {
    ElMessage.warning(err);
    return;
  }
  saving.value = true;
  try {
    const res = await upsertPurchaseOrder({
      orderId: props.orderId || undefined,
      supplierId: form.supplierId,
      orderTime: form.orderTime || undefined,
      remark: form.remark.trim() || undefined,
      items: items.value.map((i) => ({
        drugId: i.drugId,
        batchNo: (i.batchNo || '').trim(),
        productionDate: i.productionDate || undefined,
        expiryDate: i.expiryDate || undefined,
        quantity: Number(i.quantity),
        unitPrice: Number(i.unitPrice),
        remark: i.remark?.trim() || undefined,
      })),
    });
    if (res.code === 200) {
      ElMessage.success(props.orderId ? '订单已更新（审批状态回到待审批）' : '订单已创建');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};

watch(() => props.modelValue, async (v) => {
  if (!v) return;
  loadDrugs(); // 药品目录惰性加载：打开弹窗才拉（列表页不背这 545 条）
  if (props.orderId)
    await initEdit(props.orderId);
  else
    initCreate();
});
</script>
