<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                :prefix-icon="Search"
                class="!w-64"
                clearable
                data-testid="orderset-search"
                placeholder="组套名称 / 适用场景"
                size="small"
                @keyup.enter="onSearch"
            />
          </el-form-item>
          <el-form-item label="共享范围">
            <el-select v-model="query.scope" class="!w-32" clearable placeholder="共享范围" size="small">
              <el-option v-for="s in SCOPE_OPTIONS" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="医嘱类型">
            <el-select v-model="query.orderType" class="!w-32" clearable placeholder="医嘱类型" size="small">
              <el-option :value="1" label="长期"/>
              <el-option :value="2" label="临时"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="orderset-query" size="small" type="primary" @click="onSearch">查询
            </el-button>
            <el-button :icon="Refresh" size="small" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:orderSet:add'" :icon="Plus" data-testid="orderset-add" plain size="small"
                     type="primary" @click="openCreate">
            新增组套
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
          v-loading="loading"
          :data="rows"
          :max-height="tableMaxHeight"
          data-testid="orderset-table"
          row-key="id"
          stripe
          style="width: 100%"
      >
        <el-table-column label="组套名称" min-width="180" prop="templateName" show-overflow-tooltip/>
        <el-table-column align="center" label="共享范围" width="100">
          <template #default="{ row }">
            <el-tag :type="scopeTagType(row.scope)" size="small">{{ row.scopeText || scopeText(row.scope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归属" show-overflow-tooltip width="150">
          <template #default="{ row }">
            <span v-if="row.scope === 3">全院共用</span>
            <span v-else-if="row.scope === 2">{{ row.deptName || '本科室' }}</span>
            <span v-else>{{ row.doctorName || '本人' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="医嘱类型" width="90">
          <template #default="{ row }">{{ row.orderTypeText }}</template>
        </el-table-column>
        <el-table-column align="center" label="明细条数" prop="itemCount" width="90"/>
        <el-table-column label="适用场景" min-width="180" prop="remark" show-overflow-tooltip/>
        <el-table-column label="更新时间" prop="updateTime" width="160"/>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="orderset-view" link size="small" type="primary" @click="openView(row)">
              明细
            </el-button>
            <el-button
                v-if="row.editable"
                v-perm="'ipd:orderSet:add'"
                data-testid="orderset-edit"
                link
                size="small"
                type="primary"
                @click="openEdit(row)"
            >编辑
            </el-button>
            <el-button
                v-if="row.editable"
                v-perm="'ipd:orderSet:delete'"
                data-testid="orderset-delete"
                link
                size="small"
                type="danger"
                @click="remove(row)"
            >删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">还没有组套：点「新增组套」把一组常用医嘱存成模板</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            size="small"
            @current-change="loadList"
            @size-change="onSearch"
        />
      </div>
    </el-card>

    <!-- 明细预览（只读） -->
    <el-dialog v-model="viewDialog" :title="`组套明细：${viewRow?.templateName || ''}`" top="8vh" width="900px">
      <div v-loading="viewLoading">
        <div v-if="viewRow" class="mb-2 text-xs text-slate-500">
          共享范围 {{ viewRow.scopeText }} · {{ viewRow.orderTypeText }} · 共 {{ viewRow.itemCount }} 条
          <span v-if="viewRow.remark"> · {{ viewRow.remark }}</span>
        </div>
        <el-table v-if="viewRow" :data="viewRow.items || []" border data-testid="orderset-view-table" size="small">
          <el-table-column align="center" label="#" type="index" width="50"/>
          <el-table-column align="center" label="类别" width="80">
            <template #default="{ row }">{{ row.orderClassText || orderClassText(row.orderClass) }}</template>
          </el-table-column>
          <el-table-column label="项目名称" min-width="160" prop="itemName" show-overflow-tooltip/>
          <el-table-column label="规格" prop="spec" width="100"/>
          <el-table-column label="剂量" width="120">
            <template #default="{ row }">
              <span v-if="row.dosage != null">{{ row.dosage }} {{ row.dosageUnit || '' }}</span>
              <span v-else class="text-slate-400">—</span>
            </template>
          </el-table-column>
          <el-table-column label="途径" prop="route" width="90"/>
          <el-table-column label="频次" prop="frequency" width="80"/>
          <el-table-column align="center" label="数量" prop="quantity" width="70"/>
          <el-table-column label="单位" prop="unit" width="70"/>
          <el-table-column align="right" label="参考单价" prop="price" width="90"/>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="viewDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="dialog" :title="editingId ? '编辑组套' : '新增组套'" top="5vh" width="1100px">
      <el-form label-width="90px">
        <div class="grid grid-cols-1 gap-x-4 md:grid-cols-4">
          <el-form-item label="组套名称" required>
            <el-input v-model="form.templateName" data-testid="orderset-form-name" placeholder="如 术前常规组套"/>
          </el-form-item>
          <el-form-item label="共享范围" required>
            <el-select v-model="form.scope" :disabled="scopeDisabled" class="!w-full" data-testid="orderset-form-scope">
              <el-option v-for="s in SCOPE_OPTIONS" :key="s.value" :label="s.label" :value="s.value"/>
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
        <el-button @click="dialog = false">取消</el-button>
        <el-button :loading="saving" data-testid="orderset-submit" type="primary" @click="submit">保存组套</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {deleteOrderSetById, getOrderSetDetailById, getOrderSetListPage, upsertOrderSet,} from '@/api/inpatientOrder';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {
  blankOrderItem,
  findDictByCode,
  loadOrderItemDicts,
  optionsOfClass,
  ORDER_CLASS_OPTIONS,
  orderClassText,
  pickDictItem,
  resetRowOnClassChange,
} from '@/lib/orderItemDict';
import {dosageUnitOptions, frequencyOptions, loadOrderUsageOptions, routeOptions} from '@/lib/drugUsage';

const SCOPE_OPTIONS = [
  {value: 1, label: '个人'},
  {value: 2, label: '科室'},
  {value: 3, label: '全院'},
];
const scopeText = (v) => SCOPE_OPTIONS.find((o) => o.value === v)?.label || `未知(${v})`;
const scopeTagType = (v) => (v === 3 ? 'danger' : v === 2 ? 'warning' : 'info');
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  scope: null,
  orderType: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getOrderSetListPage({
      keyword: query.keyword || undefined,
      scope: query.scope ?? undefined,
      orderType: query.orderType ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    rows.value = res?.data?.records || [];
    total.value = Number(res?.data?.total || 0);
  } catch (e) {
    ElMessage.error(e?.message || '加载组套失败');
  } finally {
    loading.value = false;
  }
};
const onSearch = () => {
  query.pageNum = 1;
  loadList();
};
const onReset = () => {
  query.keyword = '';
  query.scope = null;
  query.orderType = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 明细预览 ----------------
const viewDialog = ref(false);
const viewRow = ref(null);
const viewLoading = ref(false);
const openView = async (row) => {
  viewRow.value = null;
  viewDialog.value = true;
  viewLoading.value = true;
  try {
    const res = await getOrderSetDetailById(row.id);
    viewRow.value = res?.data || null;
  } catch (e) {
    ElMessage.error(e?.message || '加载组套明细失败');
  } finally {
    viewLoading.value = false;
  }
};
// ---------------- 新增 / 编辑 ----------------
const dialog = ref(false);
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
const openCreate = () => {
  editingId.value = null;
  form.templateName = '';
  form.scope = 3;
  form.orderType = 2;
  form.remark = '';
  form.items = [blankOrderItem()];
  dialog.value = true;
  ensureDicts();
};
const openEdit = async (row) => {
  try {
    const res = await getOrderSetDetailById(row.id);
    const d = res?.data;
    if (!d) {
      ElMessage.error('组套不存在或已删除');
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
    dialog.value = true;
    ensureDicts();
  } catch (e) {
    ElMessage.error(e?.message || '加载组套明细失败');
  }
};
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
    dialog.value = false;
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`删除后新开医嘱时不再能套用「${row.templateName}」（${row.itemCount} 条明细），已按它开出的医嘱不受影响。`, '删除组套', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await deleteOrderSetById(row.id);
    ElMessage.success('已删除');
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '删除失败');
  }
};
onMounted(() => {
  loadList();
});
</script>
