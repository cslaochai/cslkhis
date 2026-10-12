<template>
  <!-- 新增库存弹窗（从药品字典选药 + 批次入库） -->
  <el-dialog v-model="visible" destroy-on-close title="新增库存（选择药品批次入库）" width="640px">
    <el-form :model="form" label-position="top">
      <el-form-item label="药品（来自药品字典）" required>
        <el-select
            v-model="form.drugId"
            class="w-full"
            filterable
            placeholder="搜索或选择药品"
            @change="handleDrugChange"
        >
          <el-option
              v-for="d in drugOptions"
              :key="d.id"
              :label="`${d.drugName}（${d.specification || '—'}）`"
              :value="d.id"
          >
            <span>{{ d.drugName }}</span>
            <span class="text-xs text-slate-400 ml-2">{{ d.drugCode }} / {{
                d.specification || '—'
              }} / ¥{{ d.retailPrice }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <div v-if="selectedDrug" class="grid grid-cols-3 gap-4 mb-4">
        <div class="rounded bg-slate-50 p-2.5">
          <p class="text-xs text-slate-500">规格</p>
          <p class="text-sm font-medium text-slate-900">{{ selectedDrug.specification || '—' }}</p>
        </div>
        <div class="rounded bg-slate-50 p-2.5">
          <p class="text-xs text-slate-500">单位</p>
          <p class="text-sm font-medium text-slate-900">{{ selectedDrug.unit || '—' }}</p>
        </div>
        <div class="rounded bg-slate-50 p-2.5">
          <p class="text-xs text-slate-500">字典零售价</p>
          <p class="text-sm font-medium text-slate-900">¥{{ selectedDrug.retailPrice ?? '—' }}</p>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="批号" required>
          <el-input v-model="form.batchNo" placeholder="生产批号"/>
        </el-form-item>
        <el-form-item label="有效期" required>
          <el-date-picker v-model="form.expiryDate" class="w-full" placeholder="有效期至" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="生产日期">
          <el-date-picker v-model="form.productionDate" class="w-full" placeholder="生产日期"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="入库数量" required>
          <el-input-number v-model="form.quantity" :min="1" class="w-full"/>
        </el-form-item>
        <el-form-item label="成本价(元)" required>
          <el-input-number v-model="form.costPrice" :min="0" :precision="2" class="w-full"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-3 gap-4">
        <el-form-item label="存放位置">
          <el-input v-model="form.location" placeholder="货位，如 A-01-01"/>
        </el-form-item>
        <el-form-item label="库存地点" required>
          <el-select v-model="form.stockRoom" :fit-input-width="false" data-testid="stock-room-select">
            <el-option v-for="d in roomDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="供货商">
          <el-select
              v-model="form.supplierId"
              :fit-input-width="false"
              clearable
              data-testid="stock-supplier-select"
              filterable
              placeholder="挂上档案才能被供应商退货选中"
              @change="onSupplierPick"
          >
            <el-option v-for="s in suppliers" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId"/>
          </el-select>
        </el-form-item>
      </div>
      <el-alert :closable="false" class="!mt-1" show-icon type="info">
        <template #title>
          「药房」的批次才能被发药/锁库；落在「药库」的批次要先在「药品调拨」下拨到药房。
          供货商留空 = 这一批退不了供应商（历史数据里那列文本多是生产厂家名，不能当退货对象）。
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'pharmacy:stock:add'" type="primary" @click="handleSubmit">确认新增</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 新增库存弹窗 —— 从 views/pharmacy/PharmacyView.vue 抽出
 *
 * 以 sys_drug 为唯一药品来源做批次入库；库位（roomDict）由父页加载后作 prop 传入（列表筛选与这里共用一份）。
 * 口径不变：stockRoom 默认落药房(2)、要进药库选 1 再走调拨下拨；供货商留空即不可退供应商。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { createStock } from '@/api/pharmacy';
import { getSupplierSelectList } from '@/api/purchase';
import { getDrugSelectList } from '@/api/system';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  roomDict: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const drugOptions = ref([]);
const suppliers = ref([]);
const selectedDrug = ref(null);
const form = ref({
  drugId: null,
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  quantity: 0,
  costPrice: 0,
  location: '',
  /** 库位：手工建批默认落在药房（与后端默认一致）；要进药库就选 1，之后走调拨下拨 */
  stockRoom: 2,
  supplierId: null,
  supplier: '',
});
/** 选供货商：同时把名字快照进 supplier 文本列（历史页面读的是这一列，不能只存 ID） */
const onSupplierPick = (supplierId) => {
  const hit = suppliers.value.find((s) => s.supplierId === supplierId);
  form.value.supplier = hit ? hit.supplierName : '';
};
// 加载药品字典下拉（库存的新增以 sys_drug 为唯一药品来源）
const loadDrugOptions = async () => {
  try {
    const res = await getDrugSelectList();
    drugOptions.value = res.data || [];
  } catch (error) {
    console.error('加载药品字典失败:', error);
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
const handleDrugChange = (drugId) => {
  selectedDrug.value = drugOptions.value.find((d) => d.id === drugId) || null;
};
const reset = () => {
  form.value = {
    drugId: null, batchNo: '', productionDate: '', expiryDate: '',
    quantity: 0, costPrice: 0, location: '', stockRoom: 2, supplierId: null, supplier: '',
  };
  selectedDrug.value = null;
};
const handleSubmit = async () => {
  if (!form.value.drugId) {
    ElMessage.warning('请从药品字典选择药品');
    return;
  }
  if (!form.value.batchNo || !form.value.expiryDate) {
    ElMessage.warning('请填写批号和有效期');
    return;
  }
  if (form.value.quantity <= 0) {
    ElMessage.warning('库存数量必须大于0');
    return;
  }
  try {
    await createStock(form.value);
    ElMessage.success('新增成功');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '新增失败');
  }
};

watch(() => props.modelValue, (v) => {
  if (!v) return;
  reset();
  if (!drugOptions.value.length)
    loadDrugOptions();
  if (!suppliers.value.length)
    loadSuppliers();
});
</script>
