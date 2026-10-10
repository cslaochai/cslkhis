<template>
  <div data-testid="chronic-catalog-page">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="catQuery" inline @submit.prevent>
          <el-form-item label="类别">
            <el-select v-model="catQuery.diseaseType" clearable placeholder="全部类别" style="width: 130px">
              <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue"
                         :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="启停">
            <el-select v-model="catQuery.status" clearable placeholder="全部启停" style="width: 130px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="catQuery.keyword" clearable data-testid="catalog-keyword" placeholder="编码/名称/ICD"
                      style="width: 220px"
                      @keyup.enter="catPage.pageNum = 1; loadCatalogs()"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="catPage.pageNum = 1; loadCatalogs()">查询</el-button>
            <el-button :icon="Refresh" @click="resetCatQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'finance:insuranceChronic:add'" :icon="Plus" data-testid="catalog-create-btn"
                     type="primary" @click="openCatCreate">新增病种
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="catLoading" :data="catRows" :max-height="tableMaxHeight" data-testid="catalog-table" stripe
                style="width: 100%">
        <el-table-column class-name="font-mono" label="病种编码" prop="diseaseCode" width="120"/>
        <el-table-column label="病种名称" min-width="220" prop="diseaseName" show-overflow-tooltip/>
        <el-table-column label="类别" width="100">
          <template #default="{row}">{{ dictText(DICT_TYPE.CHRONIC_DISEASE_TYPE, row.diseaseType) }}</template>
        </el-table-column>
        <el-table-column class-name="font-mono" label="ICD-10" prop="icdCode" width="120">
          <template #default="{row}">{{ row.icdCode || '—' }}</template>
        </el-table-column>
        <el-table-column label="默认有效期" width="120">
          <template #default="{row}">{{ row.defaultValidMonths ? `${row.defaultValidMonths} 个月` : '长期' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :data-testid="`catalog-status-${row.diseaseCode}`" :type="row.status === 1 ? 'success' : 'info'"
                    size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180" prop="remark" show-overflow-tooltip>
          <template #default="{row}">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="{row}">
            <el-button :data-testid="`catalog-drill-${row.diseaseCode}`" plain size="small"
                       @click="drillRegsOfCatalog(row)">看备案
            </el-button>
            <el-button v-perm="'finance:insuranceChronic:add'" :data-testid="`catalog-edit-${row.diseaseCode}`" :icon="Edit" plain size="small"
                       type="primary" @click="openCatEdit(row)">编辑
            </el-button>
            <el-button v-perm="'finance:insuranceChronic:add'" :data-testid="`catalog-toggle-${row.diseaseCode}`" :type="row.status === 1 ? 'danger' : 'success'"
                       plain
                       size="small" @click="toggleCatStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="catPage.pageNum" v-model:page-size="catPage.pageSize"
                       :page-sizes="PAGE_SIZES" :total="catPage.total" data-testid="catalog-pagination"
                       layout="total, sizes, prev, pager, next, jumper" @current-change="loadCatalogs"
                       @size-change="catPage.pageNum = 1; loadCatalogs()"/>
      </div>
    </el-card>

    <!-- ==================== 病种目录 新建/编辑 ==================== -->
    <el-dialog v-model="catFormVisible" :title="catForm.id ? `修改病种 ${catForm.diseaseCode}` : '新增慢特病病种'"
               data-testid="catalog-form-dialog" width="640px">
      <el-form :model="catForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="病种编码" required>
              <el-input v-model="catForm.diseaseCode" data-testid="catalog-form-code" placeholder="如 MZ012"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别" required>
              <el-select v-model="catForm.diseaseType" data-testid="catalog-form-type" style="width: 100%">
                <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue"
                           :label="d.dictLabel" :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="病种名称" required>
              <el-input v-model="catForm.diseaseName" data-testid="catalog-form-name"
                        placeholder="与国家医保慢特病目录同名"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ICD-10 主码">
              <el-input v-model="catForm.icdCode" data-testid="catalog-form-icd" placeholder="如 I10"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认有效期">
              <el-input-number v-model="catForm.defaultValidMonths" :controls="false" :min="1" data-testid="catalog-form-months"
                               placeholder="留空＝长期" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态">
              <el-switch v-model="catForm.status" :active-value="1" :inactive-value="0"
                         data-testid="catalog-form-status"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="catForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catFormVisible = false">取消</el-button>
        <el-button :loading="catSubmitting" data-testid="catalog-form-save" type="primary" @click="saveCat">保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {useRouter} from 'vue-router';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {changeChronicCatalogStatus, getChronicCatalogList, upsertChronicCatalog} from '@/api/insuranceChronic';

const router = useRouter();
const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];
const dictText = (type, value) => dictLabelText(dicts.value[type], value);
// ==================== 病种目录 ====================
const catLoading = ref(false);
const catRows = ref([]);
const catQuery = reactive({diseaseType: undefined, status: undefined, keyword: ''});
const catPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadCatalogs = async () => {
  catLoading.value = true;
  try {
    const res = await getChronicCatalogList({...catQuery, pageNum: catPage.pageNum, pageSize: catPage.pageSize});
    catRows.value = res.data?.records || [];
    catPage.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error?.message || '病种目录加载失败');
  } finally {
    catLoading.value = false;
  }
};
const resetCatQuery = () => {
  catQuery.diseaseType = undefined;
  catQuery.status = undefined;
  catQuery.keyword = '';
  catPage.pageNum = 1;
  loadCatalogs();
};
const catFormVisible = ref(false);
const catSubmitting = ref(false);
const emptyCat = () => ({
  id: null, diseaseCode: '', diseaseName: '', diseaseType: 1,
  icdCode: '', defaultValidMonths: undefined, status: 1, remark: ''
});
const catForm = ref(emptyCat());
const openCatCreate = () => {
  catForm.value = emptyCat();
  catFormVisible.value = true;
};
const openCatEdit = (row) => {
  catForm.value = {...emptyCat(), ...row, id: row.id, defaultValidMonths: row.defaultValidMonths ?? undefined};
  catFormVisible.value = true;
};
const saveCat = async () => {
  const f = catForm.value;
  if (!f.diseaseCode || !f.diseaseName || !f.diseaseType) {
    ElMessage.warning('病种编码、名称、类别都要填');
    return;
  }
  catSubmitting.value = true;
  try {
    await upsertChronicCatalog({
      id: f.id || undefined, diseaseCode: f.diseaseCode.trim(), diseaseName: f.diseaseName.trim(),
      diseaseType: f.diseaseType, icdCode: f.icdCode || undefined,
      defaultValidMonths: f.defaultValidMonths ?? undefined, status: f.status, remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '病种已更新' : '病种已新增');
    catFormVisible.value = false;
    loadCatalogs();
  } catch (error) {
    ElMessage.error(error?.message || '病种保存失败');
  } finally {
    catSubmitting.value = false;
  }
};
const toggleCatStatus = (row) => {
  const next = row.status === 1 ? 0 : 1;
  const hint = next === 0
      ? `停用病种「${row.diseaseName}」？停用后不能再新备案，历史备案与已完成结算不受影响。`
      : `重新启用病种「${row.diseaseName}」？`;
  ElMessageBox.confirm(hint, next === 0 ? '停用确认' : '启用确认', {type: 'warning'})
      .then(async () => {
        try {
          await changeChronicCatalogStatus(row.id, next);
          ElMessage.success(next === 0 ? '已停用' : '已启用');
          loadCatalogs();
        } catch (error) {
          ElMessage.error(error?.message || '病种状态变更失败');
        }
      })
      .catch(() => {
      });
};
/** 看该病种的备案台账：跳回备案页并带上病种筛选（原为页签内切 tab） */
const drillRegsOfCatalog = (row) => {
  router.push({path: '/insurance-chronic', query: {catalogId: String(row.id)}});
};
onMounted(async () => {
  dicts.value = await loadDictDataMap(DICT_TYPE.CHRONIC_DISEASE_TYPE);
  loadCatalogs();
});
</script>
