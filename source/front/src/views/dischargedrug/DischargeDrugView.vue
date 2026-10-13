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

    <!-- 开单/编辑（子组件自持表单与提交，见 DischargeDrugEdit.vue） -->
    <DischargeDrugEdit v-model="editVisible" :admission-id="query.admissionId" :row="editRow" @saved="loadList"/>
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
} from '@/api/dischargeDrug';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import DischargeDrugEdit from './DischargeDrugEdit.vue';
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
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {admissionId: '', drugName: '', dispenseStatus: null, pageNum: 1});
  loadList();
};
const statusTag = (v) => (v === 2 ? 'success' : 'warning');
// ---------------- 开单 / 编辑（表单与提交已拆至 DischargeDrugEdit.vue，这里只管开关与行快照） ----------------
const editVisible = ref(false);
const editRow = ref(null);
const openCreate = () => {
  editRow.value = null;
  editVisible.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  editVisible.value = true;
};
// ---------------- 发药 / 删除 ----------------
const dispense = async (row) => {
  try {
    const res = await dischargeDrugDispense([row.id]);
    if (res.code === 200) {
      ElMessage.success('发药完成');
      loadList();
    } 
  } catch (e) {
    console.error(e);
    
  }
};
const remove = async (row) => {
  try {
    const res = await dischargeDrugDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadList();
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
