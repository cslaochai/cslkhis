<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">供应商管理</h2>
        <el-button v-perm="'pharmacy:supplier:add'" :icon="Plus" data-testid="supplier-add-btn" type="primary"
                   @click="openCreate">
          新增供应商
        </el-button>
      </div>

      <!-- 筛选：条件全部下推后端，前端不对当前页切片 -->
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              clearable
              data-testid="supplier-keyword"
              placeholder="编码 / 名称 / 联系人"
              style="width: 220px"
              @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="评级">
          <el-select
              v-model="query.rating"
              :fit-input-width="false"
              clearable
              data-testid="supplier-rating-filter"
              placeholder="评级"
              style="width: 120px"
          >
            <el-option v-for="o in ratingOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="query.status"
              :fit-input-width="false"
              clearable
              placeholder="状态"
              style="width: 120px"
          >
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="停用"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="supplier-table" stripe>
        <el-table-column label="编码" prop="supplierCode" width="130"/>
        <el-table-column label="名称" min-width="180" prop="supplierName" show-overflow-tooltip/>
        <el-table-column label="联系人" prop="contactPerson" width="100"/>
        <el-table-column label="联系电话" prop="phone" width="130"/>
        <el-table-column label="证照有效期" width="120">
          <template #default="{ row }">{{ row.licenseExpiry || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="评级" width="90">
          <template #default="{ row }">
            <el-tag :type="Number(row.rating) === 1 ? 'danger' : Number(row.rating) === 4 ? 'success' : 'info'">
              {{ ratingText(row.rating) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">{{ enableText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="120" prop="remark" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-perm="'pharmacy:supplier:add'" :icon="Edit" link type="primary" @click="openEdit(row)">编辑
            </el-button>
            <el-button v-perm="'pharmacy:supplier:delete'" :icon="Delete" link type="danger" @click="remove(row)">删除
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="form.supplierId ? '编辑供应商' : '新增供应商'"
        data-testid="supplier-dialog"
        width="560px"
    >
      <el-form label-width="100px">
        <el-form-item label="编码">
          <el-input v-model="form.supplierCode" placeholder="留空由后端生成"/>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.supplierName" placeholder="供应商名称"/>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson"/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone"/>
        </el-form-item>
        <el-form-item label="经营许可证">
          <el-input v-model="form.businessLicense"/>
        </el-form-item>
        <el-form-item label="证照有效期">
          <el-date-picker v-model="form.licenseExpiry" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="评级">
          <el-select v-model="form.rating" :fit-input-width="false" style="width: 100%">
            <el-option v-for="o in ratingOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'pharmacy:supplier:add'" :loading="saving" data-testid="supplier-save-btn" type="primary"
                   @click="save">保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 供应商管理（G9）
 *
 * 供应商是采购链的**主数据**：采购订单引用它，删不掉被引用的供应商（后端拦截）。
 * 评级文案走字典 his_supplier_rating（1差/2一般/3良好/4优秀）；
 * 启停用走 his_enable_status —— 两个都命中不了渲染「未知(n)」，不回落成看似合法的值。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {deleteSupplier, getSupplierList, upsertSupplier} from '@/api/purchase';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
// 字典：供应商评级 / 启停用
const ratingDict = ref([]);
const enableDict = ref([]);
const ratingOptions = computed(() => ratingDict.value.map((d) => ({label: d.dictLabel, value: Number(d.dictValue)})));
const ratingText = (v) => dictLabelText(ratingDict.value, v);
const enableText = (v) => dictLabelText(enableDict.value, v);
const loadDicts = async () => {
  try {
    // ⚠ 必须取 res.data[...]：getDictDataMapList 外层还包了一层 {data:{dictType:[...]}}
    const res = await getDictDataMapList(`${DICT_TYPE.SUPPLIER_RATING},${DICT_TYPE.ENABLE_STATUS}`);
    ratingDict.value = res?.data?.[DICT_TYPE.SUPPLIER_RATING] || [];
    enableDict.value = res?.data?.[DICT_TYPE.ENABLE_STATUS] || [];
  } catch (e) {
    console.error('加载供应商字典失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getSupplierList({
      keyword: query.keyword?.trim() || undefined,
      rating: query.rating ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    } else {
      ElMessage.error(res.message || '加载供应商失败');
    }
  } catch (e) {
    ElMessage.error('加载供应商失败');
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.keyword = '';
  query.status = null;
  query.pageNum = 1;
  loadList();
};
// ==================== 编辑弹窗 ====================
const dialogVisible = ref(false);
const saving = ref(false);
const form = reactive({
  supplierId: null,
  supplierCode: '',
  supplierName: '',
  contactPerson: '',
  phone: '',
  address: '',
  licenseNo: '',
  licenseExpiry: '',
  rating: 3,
  status: 1,
  remark: '',
});
const openCreate = () => {
  form.supplierId = null;
  form.supplierCode = '';
  form.supplierName = '';
  form.contactPerson = '';
  form.phone = '';
  form.address = '';
  form.licenseNo = '';
  form.licenseExpiry = '';
  form.rating = 3;
  form.status = 1;
  form.remark = '';
  dialogVisible.value = true;
};
const openEdit = (row) => {
  form.supplierId = row.supplierId;
  form.supplierCode = row.supplierCode || '';
  form.supplierName = row.supplierName || '';
  form.contactPerson = row.contactPerson || '';
  form.phone = row.phone || '';
  form.address = row.address || '';
  form.licenseNo = row.licenseNo || '';
  form.licenseExpiry = row.licenseExpiry || '';
  form.rating = Number(row.rating ?? 3);
  form.status = Number(row.status ?? 1);
  form.remark = row.remark || '';
  dialogVisible.value = true;
};
const save = async () => {
  if (!form.supplierName.trim()) {
    ElMessage.warning('请填写供应商名称');
    return;
  }
  saving.value = true;
  try {
    const res = await upsertSupplier({
      supplierId: form.supplierId || undefined,
      supplierCode: form.supplierCode.trim() || undefined,
      supplierName: form.supplierName.trim(),
      contactPerson: form.contactPerson.trim() || undefined,
      phone: form.phone.trim() || undefined,
      address: form.address.trim() || undefined,
      licenseNo: form.licenseNo.trim() || undefined,
      licenseExpiry: form.licenseExpiry || undefined,
      rating: form.rating,
      status: form.status,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(form.supplierId ? '供应商已更新' : '供应商已创建');
      dialogVisible.value = false;
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
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除供应商「${row.supplierName}」？被采购订单引用的供应商无法删除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    const res = await deleteSupplier(row.supplierId);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadList();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (e) {
    console.error(e);
  }
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
