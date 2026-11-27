<template>
  <!-- 套餐编辑（明细整单替换；项目只能从项目目录选，名称/价格/参考范围由目录带出） -->
  <el-dialog v-model="visible"
             :title="form.id ? `编辑套餐 — ${form.packageName}` : '新增体检套餐'" width="960px">
    <el-form label-width="100px">
      <div class="flex flex-wrap gap-x-6">
        <el-form-item label="套餐名称" required>
          <el-input v-model="form.packageName" style="width: 220px"/>
        </el-form-item>
        <el-form-item label="适用性别">
          <el-select v-model="form.genderLimit" style="width: 110px">
            <el-option :value="0" label="不限"/>
            <el-option :value="1" label="男"/>
            <el-option :value="2" label="女"/>
          </el-select>
        </el-form-item>
        <el-form-item label="套餐价(元)">
          <el-input-number v-model="form.price" :controls="false" :min="0" :precision="2"
                           style="width: 130px" @change="priceAuto = false"/>
        </el-form-item>
        <el-form-item label="最低折扣率">
          <el-input-number v-model="form.floorRatePercent" :controls="false" :min="1" :max="100"
                           :precision="0" style="width: 80px"/>
          <span class="text-xs text-gray-500 ml-1">%（价格底线 = 原价 × 本值）</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="说明">
        <el-input v-model="form.description" style="width: 620px"/>
      </el-form-item>
      <el-form-item label="项目明细" required>
        <div class="w-full">
          <el-table :data="form.items" border max-height="360" size="small">
            <el-table-column label="项目" min-width="320">
              <template #default="{ row }">
                <el-select v-model="row.key" clearable filterable placeholder="选择项目（检验 / 检查 / 一般）"
                           style="width: 100%" @change="(key) => onPick(row, key)">
                  <el-option-group v-for="g in optionGroups" :key="g.source" :label="g.label">
                    <el-option v-for="o in g.items" :key="o.key" :label="o.itemName" :value="o.key">
                      <span>{{ o.itemName }}</span>
                      <span class="text-xs text-gray-400 ml-3">{{ o.itemCode }} ￥{{ fmtMoney(o.price) }}</span>
                    </el-option>
                  </el-option-group>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="80">
              <template #default="{ row }">{{ sourceLabel[row.itemSource] }}</template>
            </el-table-column>
            <el-table-column label="参考范围" min-width="180">
              <template #default="{ row }">
                <span class="text-xs">{{ row.refStandard }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" label="单项金额(元)" width="110">
              <template #default="{ row }">{{ fmtMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column align="center" label="" width="60">
              <template #default="{ $index }">
                <el-button :disabled="form.items.length <= 1" :icon="Delete" link size="small" type="danger"
                           @click="removeItem($index)"/>
              </template>
            </el-table-column>
          </el-table>
          <div class="flex items-center gap-x-4 gap-y-1 flex-wrap mt-1 text-xs">
            <el-button :icon="Plus" link type="primary" @click="addItem">添加项目</el-button>
            <span class="text-gray-500">项目原价合计 ￥{{ fmtMoney(itemsTotal) }}</span>
            <template v-if="itemsTotal > 0">
              <span class="text-gray-500">套餐折扣率 {{ discountPercent }}%</span>
              <span class="text-gray-500">价格底线 ￥{{ fmtMoney(floorPrice) }}（最低折扣率 {{ form.floorRatePercent }}%）</span>
              <span v-if="belowFloor" class="text-red-500">
                套餐价低于价格底线 ￥{{ fmtMoney(floorPrice) }}，保存会被拒绝；要促销请先下调最低折扣率
              </span>
            </template>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="save">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 体检套餐新增/修改（后端同一个 checkupPackageSave，row 为空走新增、有值走修改）
 * 套餐保存是明细整单替换（Save DTO items 整删重插），编辑前必须回填全量明细，
 * 故打开时按 id 走 getCheckupPackage 取详情而不是直接用列表行。
 *
 * 明细项目只能从项目目录选（检验 sys_laboratory_item / 检查 sys_inspection_item /
 * 一般检查 sys_treatment_item），选中即回填项目编码、分类、参考范围与目录价；
 * 名称与金额不在这里手填，价格以价表为准，套餐价默认等于明细合计（可手工改打包价）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {checkupPackageSave, getCheckupPackage, getCheckupItemOptions} from '@/api/checkup';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 为空走新增，有值走修改
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const sourceLabel = {1: '检验', 2: '检查', 3: '一般'};
const groupLabel = {1: '检验项目', 2: '检查项目', 3: '一般检查项目'};

const saving = ref(false);
// 套餐价是否跟随明细合计：手工改过价格就置 false
const priceAuto = ref(true);
// 项目目录全量（一次拉取，按来源分三组渲染下拉）
const catalog = ref([]);
const catalogMap = computed(() => {
  const map = new Map();
  catalog.value.forEach((o) => map.set(o.key, o));
  return map;
});
const optionGroups = computed(() => [1, 2, 3].map((source) => ({
  source,
  label: groupLabel[source],
  items: catalog.value.filter((o) => o.itemSource === source),
})));

const form = reactive({
  id: null, packageName: '', genderLimit: 0, price: null, floorRatePercent: 100,
  description: '', status: 1, remark: '',
  items: [],
});

const fmtMoney = (v) => (v == null || v === '' ? '' : Number(v).toFixed(2));

const itemsTotal = computed(() => form.items.reduce((sum, i) => sum + Number(i.amount || 0), 0));

// 价格底线 = 原价合计 × 最低折扣率；套餐价低于它就是越权定价，后端也会拦
const floorPrice = computed(() => itemsTotal.value * Number(form.floorRatePercent || 100) / 100);

// 当前实收价（自动跟随时等于原价）
const currentPrice = computed(() => {
  const p = priceAuto.value ? itemsTotal.value : Number(form.price || 0);
  return Number(p.toFixed(2));
});

const discountPercent = computed(() => {
  if (!itemsTotal.value) return '0.0';
  return (currentPrice.value / itemsTotal.value * 100).toFixed(1);
});

const belowFloor = computed(() => itemsTotal.value > 0 && currentPrice.value + 0.001 < floorPrice.value);

const blankItem = () => ({
  key: '', itemSource: null, itemId: null, itemCode: '', itemName: '', itemType: null,
  refStandard: '', amount: null,
});

// 明细变动且仍是「自动」时，套餐价跟着合计走
watch(itemsTotal, (total) => {
  if (priceAuto.value) form.price = Number(total.toFixed(2));
});

const loadCatalog = async () => {
  if (catalog.value.length) return;
  try {
    const res = await getCheckupItemOptions();
    if (res.code !== 200) {
      ElMessage.error(res.message || '项目目录加载失败');
      return;
    }
    catalog.value = (res.data || []).map((o) => ({...o, key: `${o.itemSource}:${o.id}`}));
  } catch (e) {
    console.error(e);
  }
};

const onPick = (row, key) => {
  const picked = catalogMap.value.get(key);
  if (!picked) {
    // 清空选择：整行退回未选状态
    Object.assign(row, blankItem());
    return;
  }
  row.itemSource = picked.itemSource;
  row.itemId = picked.id;
  row.itemCode = picked.itemCode;
  row.itemName = picked.itemName;
  row.itemType = picked.itemSource;
  row.refStandard = picked.refStandard || '';
  row.amount = picked.price;
};

watch(
  () => props.modelValue,
  async (v) => {
    if (!v) return;
    loadCatalog();
    if (!props.row) {
      Object.assign(form, {
        id: null, packageName: '', genderLimit: 0, price: null, floorRatePercent: 100,
        description: '', status: 1, remark: '',
        items: [blankItem()],
      });
      priceAuto.value = true;
      return;
    }
    try {
      const res = await getCheckupPackage(props.row.id);
      if (res.code !== 200) {
        ElMessage.error(res.message || '查询失败');
        visible.value = false;
        return;
      }
      const d = res.data;
      const items = (d.items || []).map((i) => ({
        key: i.itemId ? `${i.itemSource}:${i.itemId}` : '',
        itemSource: Number(i.itemSource ?? i.itemType ?? 3),
        itemId: i.itemId ?? null,
        itemCode: i.itemCode || '',
        itemName: i.itemName || '',
        itemType: Number(i.itemType ?? i.itemSource ?? 3),
        refStandard: i.refStandard || '',
        amount: i.amount ?? null,
      }));
      Object.assign(form, {
        id: d.id, packageName: d.packageName, genderLimit: Number(d.genderLimit ?? 0),
        price: d.price ?? null,
        floorRatePercent: d.floorRate == null ? 100 : Math.round(Number(d.floorRate) * 100),
        description: d.description || '', status: Number(d.status ?? 1),
        remark: d.remark || '',
        items: items.length ? items : [blankItem()],
      });
      // 已存档套餐价与合计一致时继续跟随，否则视为已手工定价
      const total = items.reduce((sum, i) => sum + Number(i.amount || 0), 0);
      priceAuto.value = d.price == null || Math.abs(Number(d.price) - total) < 0.01;
    } catch (e) {
      console.error(e);
      visible.value = false;
    }
  }
);

const addItem = () => form.items.push(blankItem());
const removeItem = (idx) => form.items.splice(idx, 1);

const save = async () => {
  if (!form.packageName.trim()) {
    ElMessage.warning('请填写套餐名称');
    return;
  }
  const picked = form.items.filter((i) => i.itemId);
  if (!picked.length) {
    ElMessage.warning('项目明细至少一条');
    return;
  }
  if (picked.length !== form.items.filter((i) => i.key || i.itemName).length) {
    ElMessage.warning('还有明细行没有选择项目');
    return;
  }
  // 定价前置校验（后端同口径再校验一次，前端只做即时提示）
  const total = Number(itemsTotal.value.toFixed(2));
  const price = Number((priceAuto.value ? total : form.price ?? total).toFixed(2));
  if (total > 0 && price > total + 0.001) {
    ElMessage.warning(`套餐价不能高于项目原价合计 ￥${fmtMoney(total)}`);
    return;
  }
  if (total > 0 && price + 0.001 < floorPrice.value) {
    ElMessage.warning(`套餐价低于价格底线 ￥${fmtMoney(floorPrice.value)}（最低折扣率 ${form.floorRatePercent}%），请调价或先下调最低折扣率`);
    return;
  }
  saving.value = true;
  try {
    const res = await checkupPackageSave({
      id: form.id ?? undefined,
      packageName: form.packageName.trim(),
      genderLimit: form.genderLimit,
      price,
      floorRate: Number(form.floorRatePercent || 100) / 100,
      description: form.description.trim() || undefined,
      status: form.status,
      remark: form.remark.trim() || undefined,
      items: picked.map((i) => ({
        itemSource: i.itemSource,
        itemId: i.itemId,
        itemName: i.itemName,
        itemCode: i.itemCode,
        itemType: i.itemType,
        refStandard: i.refStandard || undefined,
        amount: i.amount ?? undefined,
      })),
    });
    if (res.code === 200) {
      ElMessage.success(form.id ? '套餐已保存' : '套餐已创建');
      visible.value = false;
      emit('saved');
    }
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
</script>
