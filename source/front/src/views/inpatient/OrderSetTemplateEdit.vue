<template>
  <el-dialog v-model="visible" :title="editingId ? '编辑组套' : '新增组套'" top="5vh" width="1100px">
    <el-form label-width="90px">
      <div class="grid grid-cols-1 gap-x-4 md:grid-cols-4">
        <el-form-item label="组套名称" required>
          <el-input v-model="form.templateName" data-testid="orderset-form-name" placeholder="如 术前常规组套"/>
        </el-form-item>
        <el-form-item label="共享范围" required>
          <el-select v-model="form.scope" :disabled="scopeDisabled" class="!w-full" data-testid="orderset-form-scope">
            <el-option v-for="s in scopeOptions" :key="s.value" :label="s.label" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="医嘱类型">
          <el-select v-model="form.orderType" class="!w-full">
            <el-option :value="1" label="长期"/>
            <el-option :value="2" label="临时"/>
          </el-select>
        </el-form-item>
        <el-form-item label="适用场景">
          <el-input v-model="form.remark" placeholder="选填，列表里能看到"/>
        </el-form-item>
      </div>

      <div class="mb-2 flex items-center justify-between">
        <span class="text-sm font-medium text-slate-700">医嘱明细（{{ form.items.length }} 条）</span>
        <el-button :icon="Plus" data-testid="orderset-add-item" plain size="small" type="primary" @click="addItem">
          加一条
        </el-button>
      </div>

      <div v-for="(item, idx) in form.items" :key="idx" class="mb-3 rounded border border-slate-200 bg-slate-50 p-3">
        <div class="grid grid-cols-2 gap-x-3 gap-y-2 md:grid-cols-6">
          <el-form-item class="!mb-0" label="类别" label-width="56px">
            <el-select v-model="item.orderClass" class="!w-full" data-testid="orderset-item-class"
                       @change="onClassChange(idx)">
              <el-option v-for="c in ORDER_CLASS_OPTIONS" :key="c.value" :label="c.label" :value="c.value"/>
            </el-select>
          </el-form-item>
          <el-form-item class="!mb-0 md:col-span-2" label="项目" label-width="56px">
            <el-select
                v-if="optionsOfClass(item.orderClass).length"
                v-model="item.dictId"
                :fit-input-width="false"
                class="!w-full"
                clearable
                data-testid="orderset-item-name"
                filterable
                placeholder="搜索并选择项目"
                @change="(v: string) => pickDictItem(item, v, item.orderClass)"
            >
              <el-option
                  v-for="o in optionsOfClass(item.orderClass)"
                  :key="o.id"
                  :label="o.drugName || o.itemName"
                  :value="String(o.id)"
              >
                <span>{{ o.drugName || o.itemName }}</span>
                <span class="float-right text-xs text-slate-400">
                  {{ o.specification || o.spec || '' }}
                  <template v-if="o.retailPrice != null || o.price != null"> ¥{{
                      o.retailPrice ?? o.price
                    }}</template>
                </span>
              </el-option>
            </el-select>
            <el-input v-else v-model="item.itemName" data-testid="orderset-item-name"
                      placeholder="项目/药品名称（本类别无字典，手工录入）"/>
          </el-form-item>
          <el-form-item class="!mb-0" label="编码" label-width="56px">
            <el-input v-model="item.itemCode" placeholder="选项目自动带出"/>
          </el-form-item>
          <el-form-item class="!mb-0" label="规格" label-width="56px">
            <el-input v-model="item.spec" placeholder="如 1.0g"/>
          </el-form-item>
          <el-form-item class="!mb-0" label="单位" label-width="56px">
            <el-input v-model="item.unit" placeholder="支/次"/>
          </el-form-item>
          <el-form-item class="!mb-0" label="剂量" label-width="56px">
            <el-input v-model="item.dosage" placeholder="药品必填"/>
          </el-form-item>
          <el-form-item class="!mb-0" label="剂量单位" label-width="76px">
            <el-select v-model="item.dosageUnit" allow-create class="!w-full" clearable filterable placeholder="g/ml">
              <el-option v-for="u in dosageUnitOptions" :key="u" :label="u" :value="u"/>
            </el-select>
          </el-form-item>
          <el-form-item class="!mb-0" label="途径" label-width="56px">
            <el-select v-model="item.route" allow-create class="!w-full" clearable data-testid="orderset-item-route"
                       filterable placeholder="口服/静滴">
              <el-option v-for="r in routeOptions" :key="r" :label="r" :value="r"/>
            </el-select>
          </el-form-item>
          <el-form-item class="!mb-0" label="频次" label-width="56px">
            <el-select v-model="item.frequency" allow-create class="!w-full" clearable filterable
                       placeholder="qd/bid">
              <el-option v-for="f in frequencyOptions" :key="f.value" :label="f.label" :value="f.value"/>
            </el-select>
          </el-form-item>
          <el-form-item class="!mb-0" label="数量" label-width="56px">
            <el-input v-model="item.quantity"/>
          </el-form-item>
          <el-form-item class="!mb-0" label="单价" label-width="56px">
            <el-input v-model="item.price" placeholder="选项目自动带出"/>
          </el-form-item>
          <div class="flex items-center">
            <el-button :icon="Delete" link type="danger" @click="removeItem(idx)">删除本行</el-button>
          </div>
        </div>
      </div>

      <el-alert
          :closable="false"
          show-icon
          title="药品明细必须填全「单次剂量 / 剂量单位 / 给药途径」（与开立医嘱同一份硬规则，缺一项护士执行时只能靠猜）。价格只是参考价：套用时按字典现价重取。共享范围建好后不能改 —— 要换范围请新建一份。"
          type="info"
      />
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" data-testid="orderset-submit" type="primary" @click="submit">保存组套</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {getOrderSetDetailById, upsertOrderSet,} from '@/api/inpatientOrder';
import {
  blankOrderItem,
  findDictByCode,
  loadOrderItemDicts,
  optionsOfClass,
  ORDER_CLASS_OPTIONS,
  pickDictItem,
  resetRowOnClassChange,
} from '@/lib/orderItemDict';
import {dosageUnitOptions, frequencyOptions, loadOrderUsageOptions, routeOptions} from '@/lib/drugUsage';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 当前编辑行：为空走新增（编辑明细自己按 id 取数回填）
  row: {type: Object, default: null},
  // 共享范围选项（列表列也在用，页面加载一次传下来）
  scopeOptions: {type: Array, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const editingId = ref(null);
const form = reactive({
  templateName: '',
  scope: 3,
  orderType: 2,
  remark: '',
  items: [],
});
/** 编辑时 scope 不允许改（后端会拒绝），新增时自由选 */
const scopeDisabled = computed(() => !!editingId.value);
const addItem = () => {
  form.items.push(blankOrderItem());
};
const removeItem = (idx) => {
  form.items.splice(idx, 1);
};
const initCreate = () => {
  editingId.value = null;
  form.templateName = '';
  form.scope = 3;
  form.orderType = 2;
  form.remark = '';
  form.items = [blankOrderItem()];
  ensureDicts();
};
const initEdit = async (row) => {
  try {
    const res = await getOrderSetDetailById(row.id);
    const d = res?.data;
    if (!d) {
      ElMessage.error('组套不存在或已删除');
      visible.value = false;
      return;
    }
    editingId.value = d.id;
    form.templateName = d.templateName;
    form.scope = d.scope ?? 3;
    form.orderType = d.orderType ?? 2;
    form.remark = d.remark || '';
    form.items = (d.items || []).map((it) => ({
      orderClass: it.orderClass,
      dictId: '',
      itemCode: it.itemCode || '',
      itemName: it.itemName || '',
      spec: it.spec || '',
      unit: it.unit || '',
      dosage: it.dosage ?? '',
      dosageUnit: it.dosageUnit || '',
      route: it.route || '',
      frequency: it.frequency || '',
      quantity: it.quantity ?? 1,
      price: it.price ?? undefined,
    }));
    ensureDicts();
  } catch (e) {
    ElMessage.error(e?.message || '加载组套明细失败');
    visible.value = false;
  }
};
watch(visible, (open) => {
  if (!open) return;
  if (props.row) {
    initEdit(props.row);
  } else {
    initCreate();
  }
});
const ensureDicts = () => {
  loadOrderItemDicts().then(() => {
    // 字典到位后把下拉选中态按编码补回去，否则编辑回显时下拉显示空白
    form.items.forEach((item) => {
      const hit = findDictByCode(item.orderClass, item.itemCode);
      if (hit)
        item.dictId = String(hit.id);
    });
  });
  loadOrderUsageOptions();
};
const onClassChange = (idx) => {
  resetRowOnClassChange(form.items[idx]);
};
const submit = async () => {
  if (!form.templateName) {
    ElMessage.warning('请填写组套名称');
    return;
  }
  const items = form.items.filter((i) => i && i.itemName);
  if (!items.length) {
    ElMessage.warning('组套至少包含一条医嘱明细');
    return;
  }
  for (const it of items) {
    if (!it.orderClass) {
      ElMessage.warning('每条明细都要选类别');
      return;
    }
    // 与开立医嘱同一份硬规则：药品必须填全剂量/剂量单位/途径（后端也会拦，这里提前说人话）
    if (it.orderClass === 1 && (!it.dosage || !it.dosageUnit || !it.route)) {
      ElMessage.warning(`「${it.itemName}」是药品，单次剂量 / 剂量单位 / 给药途径必须填全`);
      return;
    }
  }
  saving.value = true;
  try {
    await upsertOrderSet({
      id: editingId.value || undefined,
      templateName: form.templateName,
      scope: form.scope,
      orderType: form.orderType,
      remark: form.remark || undefined,
      items: items.map((i) => ({
        orderClass: i.orderClass,
        itemCode: i.itemCode || undefined,
        itemName: i.itemName,
        spec: i.spec || undefined,
        unit: i.unit || undefined,
        dosage: i.dosage === '' || i.dosage == null ? undefined : Number(i.dosage),
        dosageUnit: i.dosageUnit || undefined,
        route: i.route || undefined,
        frequency: i.frequency || undefined,
        quantity: i.quantity == null ? undefined : Number(i.quantity),
        price: i.price == null ? undefined : Number(i.price),
      })),
    });
    ElMessage.success(editingId.value ? '组套已修改' : '组套已创建');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
