<template>
  <div class="p-4">
    <el-card shadow="never">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-2">
          <span class="text-base font-medium">医嘱基础字典</span>
        </div>
      </template>

      <el-tabs v-model="activeType" data-testid="orderdict-tabs">
        <el-tab-pane v-for="t in TABS" :key="t.type" :label="t.label" :name="t.type"/>
      </el-tabs>

      <div class="mb-3 flex flex-wrap items-center gap-3">
        <el-input
            v-model="query.keyword"
            :prefix-icon="Search"
            class="!w-64"
            clearable
            data-testid="orderdict-search"
            placeholder="值 / 显示名"
            size="small"
            @keyup.enter="onSearch"
        />
        <el-select v-model="query.status" class="!w-32" clearable placeholder="状态" size="small">
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="停用"/>
        </el-select>
        <el-button :icon="Search" data-testid="orderdict-query" size="small" type="primary" @click="onSearch">查询
        </el-button>
        <el-button :icon="Refresh" size="small" @click="onReset">重置</el-button>
        <el-button v-perm="'ipd:orderDict:add'" :icon="Plus" data-testid="orderdict-add" plain size="small"
                   type="primary" @click="openCreate">
          新增{{ currentTab().label }}
        </el-button>
        <span class="text-xs text-slate-400">{{ currentTab().hint }}</span>
      </div>

      <el-table
          v-loading="loading"
          :data="rows"
          border
          data-testid="orderdict-table"
          row-key="id"
          size="small"
          style="width: 100%"
      >
        <el-table-column label="值" prop="dictValue" width="140"/>
        <el-table-column label="显示名" min-width="160" prop="dictLabel"/>
        <el-table-column align="center" label="排序" prop="dictSort" width="80"/>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="医嘱使用量" prop="usageCount" width="110"/>
        <el-table-column align="center" label="来源" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.builtIn" effect="plain" size="small" type="primary">内置</el-tag>
            <span v-else class="text-xs text-slate-500">自定义</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-perm="'ipd:orderDict:add'" data-testid="orderdict-edit" link size="small" type="primary"
                       @click="openEdit(row)">编辑
            </el-button>
            <el-button v-perm="'ipd:orderDict:delete'" data-testid="orderdict-delete" link size="small" type="danger"
                       @click="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">该类型下还没有字典项</div>
        </template>
      </el-table>

      <div class="mt-3 flex justify-end">
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

    <el-dialog v-model="dialog" :title="editing ? '编辑字典项' : `新增${currentTab().label}`" width="520px">
      <el-form label-width="90px">
        <el-form-item label="字典类型">
          <el-input :model-value="currentTab().label" disabled/>
        </el-form-item>
        <el-form-item label="值" required>
          <el-input
              v-model="form.dictValue"
              :disabled="!!editing"
              :placeholder="editing ? '值不可修改（存量医嘱在用它）' : '如 静滴 / qd / mg'"
              data-testid="orderdict-form-value"
          />
        </el-form-item>
        <el-form-item label="显示名" required>
          <el-input v-model="form.dictLabel" data-testid="orderdict-form-label" placeholder="下拉里显示的文字"/>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.dictSort" :min="1" class="!w-full" controls-position="right"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" maxlength="500" placeholder="选填" show-word-limit type="textarea"/>
        </el-form-item>
      </el-form>
      <el-alert
          v-if="editing"
          :closable="false"
          class="mt-2"
          show-icon
          title="值不允许修改：存量医嘱行里存的就是它，改了历史医嘱会显示成「未知」。要换值请停用这一条、再新增一条。"
          type="info"
      />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button :loading="saving" data-testid="orderdict-submit" type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {deleteOrderDictById, getOrderDictListPage, upsertOrderDict,} from '@/api/inpatientOrder';
import {loadOrderUsageOptions, ORDER_DICT_TYPE} from '@/lib/drugUsage';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

const TABS = [
  {type: ORDER_DICT_TYPE.ROUTE, label: '给药途径', hint: '值存中文（历史医嘱 route 列就是中文）'},
  {type: ORDER_DICT_TYPE.FREQ, label: '用药频次', hint: '值存英文缩写（qd / bid / q12h…）'},
  {type: ORDER_DICT_TYPE.DOSE_UNIT, label: '剂量单位', hint: '值存单位字面量（g / mg / ml / 片…）'},
];
const activeType = ref(ORDER_DICT_TYPE.ROUTE);
const currentTab = () => TABS.find((t) => t.type === activeType.value) || TABS[0];
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getOrderDictListPage({
      dictType: activeType.value,
      keyword: query.keyword || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    rows.value = res?.data?.records || [];
    total.value = Number(res?.data?.total || 0);
  } catch (e) {
    ElMessage.error(e?.message || '加载字典失败');
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
  query.status = null;
  query.pageNum = 1;
  loadList();
};
watch(activeType, () => {
  query.pageNum = 1;
  loadList();
});
// ---------------- 新增 / 编辑 ----------------
const dialog = ref(false);
const saving = ref(false);
const editing = ref(null);
const form = reactive({
  id: null,
  dictType: ORDER_DICT_TYPE.ROUTE,
  dictValue: '',
  dictLabel: '',
  dictSort: null,
  status: 1,
  remark: '',
});
const openCreate = () => {
  editing.value = null;
  form.id = null;
  form.dictType = activeType.value;
  form.dictValue = '';
  form.dictLabel = '';
  form.dictSort = null;
  form.status = 1;
  form.remark = '';
  dialog.value = true;
};
const openEdit = (row) => {
  editing.value = row;
  form.id = row.id;
  form.dictType = row.dictType;
  form.dictValue = row.dictValue;
  form.dictLabel = row.dictLabel;
  form.dictSort = row.dictSort;
  form.status = row.status ?? 1;
  form.remark = row.remark || '';
  dialog.value = true;
};
const submit = async () => {
  if (!form.dictLabel) {
    ElMessage.warning('请填写显示名');
    return;
  }
  if (!editing.value && !form.dictValue) {
    ElMessage.warning('请填写字典值');
    return;
  }
  saving.value = true;
  try {
    await upsertOrderDict({
      id: form.id || undefined,
      dictType: form.dictType,
      dictValue: editing.value ? undefined : form.dictValue,
      dictLabel: form.dictLabel,
      dictSort: form.dictSort ?? undefined,
      status: form.status,
      remark: form.remark || undefined,
    });
    ElMessage.success('已保存');
    dialog.value = false;
    // 字典页改完要顺手把前端缓存的选项刷一遍，否则当前会话的下拉还是旧值
    await loadOrderUsageOptions();
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`删除后新开医嘱的下拉里不再有「${row.dictLabel}」${row.usageCount ? `（当前有 ${row.usageCount} 条医嘱在用，历史医嘱不受影响）` : ''}。`, '删除字典项', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await deleteOrderDictById(row.id, row.dictType);
    ElMessage.success('已删除');
    await loadOrderUsageOptions();
    loadList();
  } catch (e) {
    ElMessage.error(e?.message || '删除失败');
  }
};
onMounted(() => {
  loadList();
  loadOrderUsageOptions();
});
</script>
