<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="入院ID">
            <el-input v-model="query.admissionId" clearable placeholder="入院ID" style="width: 180px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="药品名称">
            <el-input v-model="query.drugName" clearable placeholder="药品名称" style="width: 180px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="发药状态">
            <el-select v-model="query.dispenseStatus" :fit-input-width="false" clearable placeholder="发药状态"
                       style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'inpatient:dischargeDrug:add'" plain type="primary" @click="openCreate">开带药单
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="discharge-drug-table" stripe>
        <el-table-column label="带药单号" prop="orderNo" width="200"/>
        <el-table-column label="患者" prop="patientName" width="100">
          <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 ml-1">{{ row.patientNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="入院ID" prop="admissionId" width="170"/>
        <el-table-column label="药品" min-width="160" prop="drugName">
          <template #default="{ row }">{{ row.drugName }}<span v-if="row.spec" class="text-gray-400 ml-1">{{
              row.spec
            }}</span></template>
        </el-table-column>
        <el-table-column align="right" label="数量" prop="quantity" width="80">
          <template #default="{ row }">{{ row.quantity }}<span v-if="row.unit" class="text-gray-400 ml-1">{{
              row.unit
            }}</span></template>
        </el-table-column>
        <el-table-column label="用法" min-width="140" prop="usageText" show-overflow-tooltip>
          <template #default="{ row }">{{ row.usageText || row.dosage || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="dispenseStatusText" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.dispenseStatus)">{{ statusText(row.dispenseStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发药人" prop="dispenseName" width="100">
          <template #default="{ row }">{{ row.dispenseName || '—' }}</template>
        </el-table-column>
        <el-table-column label="开单人" prop="createBy" width="100"/>
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <template v-if="row.dispenseStatus === 1">
              <el-button v-perm="'inpatient:dischargeDrug:edit'" link size="small" type="primary"
                         @click="openEdit(row)">编辑
              </el-button>
              <el-button link size="small" type="success" @click="dispense(row)">发药</el-button>
              <el-button v-perm="'inpatient:dischargeDrug:delete'" link size="small" type="danger" @click="remove(row)">
                删除
              </el-button>
            </template>
            <span v-else class="text-gray-400">已发药留痕</span>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 开单/编辑 -->
    <el-dialog v-model="editVisible" :close-on-click-modal="false" :title="editForm.id ? '编辑带药单' : '开带药单'"
               width="560px">
      <el-form label-width="90px">
        <el-form-item label="入院ID" required>
          <el-input v-model="editForm.admissionId" :disabled="!!editForm.id" placeholder="入院记录ID"/>
        </el-form-item>
        <el-form-item label="药品名称" required>
          <el-input v-model="editForm.drugName" placeholder="药品名称"/>
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="editForm.spec" placeholder="如 0.25g*24片"/>
        </el-form-item>
        <el-form-item label="用法用量">
          <div class="flex gap-2 w-full">
            <el-input v-model="editForm.dosage" placeholder="每次剂量"/>
            <el-input v-model="editForm.usageText" placeholder="如 每日三次 饭后"/>
          </div>
        </el-form-item>
        <el-form-item label="带药数量" required>
          <div class="flex gap-2 w-full">
            <el-input-number v-model="editForm.quantity" :min="0.01" :precision="2" class="!w-40"/>
            <el-input v-model="editForm.unit" class="!w-32" placeholder="单位（盒/瓶）"/>
          </div>
        </el-form-item>
        <el-form-item label="用药天数">
          <el-input-number v-model="editForm.days" :min="1" :precision="0"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button v-perm="['inpatient:dischargeDrug:add','inpatient:dischargeDrug:edit']" :loading="editLoading"
                   type="primary" @click="submitEdit">保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 出院带药（G20，菜单 310）
 *
 * 流程：开带药单（挂入院次，出院前即可开）→ 药房批量发药（单向，已发药留痕不可改删）。
 * 单条一行药品项；只有「待发药」可编辑/删除。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  dischargeDrugDelete,
  dischargeDrugDispense,
  dischargeDrugListPage,
  dischargeDrugUpsert,
} from '@/api/dischargeDrug';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 字典 ----------------
const statusDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(DICT_TYPE.DISCHARGE_DRUG_STATUS);
    statusDict.value = res?.data?.[DICT_TYPE.DISCHARGE_DRUG_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  admissionId: '',
  drugName: '', dispenseStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await dischargeDrugListPage({
      admissionId: query.admissionId === '' ? undefined : query.admissionId,
      drugName: query.drugName.trim() || undefined,
      dispenseStatus: query.dispenseStatus ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {admissionId: '', drugName: '', dispenseStatus: null, pageNum: 1});
  loadList();
};
const statusTag = (v) => (v === 2 ? 'success' : 'warning');
// ---------------- 开单 / 编辑 ----------------
const editVisible = ref(false);
const editLoading = ref(false);
const editForm = reactive({
  id: null, admissionId: '', drugId: null, drugName: '',
  spec: '', dosage: '', unit: '', quantity: null, usageText: '', days: null, remark: '',
});
const openCreate = () => {
  Object.assign(editForm, {
    id: null, admissionId: query.admissionId === '' ? '' : query.admissionId, drugId: null, drugName: '',
    spec: '', dosage: '', unit: '', quantity: null, usageText: '', days: null, remark: '',
  });
  editVisible.value = true;
};
const openEdit = (row) => {
  Object.assign(editForm, {
    id: row.id, admissionId: row.admissionId, drugId: row.drugId, drugName: row.drugName,
    spec: row.spec, dosage: row.dosage, unit: row.unit, quantity: row.quantity,
    usageText: row.usageText, days: row.days, remark: row.remark,
  });
  editVisible.value = true;
};
const submitEdit = async () => {
  if (!editForm.admissionId || !editForm.drugName.trim() || !editForm.quantity) {
    ElMessage.warning('入院ID、药品名称、带药数量为必填');
    return;
  }
  editLoading.value = true;
  try {
    const res = await dischargeDrugUpsert({
      id: editForm.id ?? undefined,
      admissionId: editForm.admissionId,
      drugId: editForm.drugId ?? undefined,
      drugName: editForm.drugName.trim(),
      spec: editForm.spec || undefined,
      dosage: editForm.dosage || undefined,
      unit: editForm.unit || undefined,
      quantity: editForm.quantity,
      usageText: editForm.usageText || undefined,
      days: editForm.days ?? undefined,
      remark: editForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('带药单已保存');
      editVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    editLoading.value = false;
  }
};
// ---------------- 发药 / 删除 ----------------
const dispense = async (row) => {
  try {
    const res = await dischargeDrugDispense([row.id]);
    if (res.code === 200) {
      ElMessage.success('发药完成');
      loadList();
    } else
      ElMessage.error(res.message || '发药失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('发药失败');
  }
};
const remove = async (row) => {
  try {
    const res = await dischargeDrugDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadList();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
