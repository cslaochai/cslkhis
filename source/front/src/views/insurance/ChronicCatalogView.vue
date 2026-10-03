<script setup lang="ts">
/**
 * 门诊慢特病病种目录（菜单 2923 / 路由 /chronic-catalog，sql/179）
 *
 * 为什么从「慢特病医保备案」（菜单 1011，sql/188 由「慢特病人员备案」更名）里拆出来单独挂菜单：
 * 备案台账的一行是**一个患者的一个病种**（每天经办，一行一张新单）；
 * 病种目录的一行是**一个病种**（随国家/省医保慢特病目录调整，一年动不了几回），
 * 而且目录条目决定备案的默认待遇期（defaultValidMonths，选病种自动带出终止日）
 * 与"这个病种还开不开放新备案"（启停）—— 属标准的「待遇目录配置」，不是备案台账的附属页签。
 * 内容与拆之前的「门诊慢特病病种目录」页签同源，接口与权限码（finance:insuranceChronic:add）均未变；
 * 原页签上的「看备案」改为带 catalogId 跳回备案台账（/insurance-chronic?catalogId=xxx）。
 */
import {ref, reactive, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search, Refresh, Plus, Edit} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {getChronicCatalogList, upsertChronicCatalog, changeChronicCatalogStatus} from '@/api/insuranceChronic'

const router = useRouter()

const dicts = ref<Record<string, any[]>>({})
const dictOptions = (type: string) => dicts.value[type] || []
const dictText = (type: string, value: any) => dictLabelText(dicts.value[type], value)

// ==================== 病种目录 ====================
const catLoading = ref(false)
const catRows = ref<any[]>([])
const catQuery = reactive({diseaseType: undefined as number | undefined, status: undefined as number | undefined, keyword: ''})
const catPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadCatalogs = async () => {
  catLoading.value = true
  try {
    const res: any = await getChronicCatalogList({...catQuery, pageNum: catPage.pageNum, pageSize: catPage.pageSize})
    catRows.value = res.data?.records || []
    catPage.total = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error?.message || '病种目录加载失败')
  } finally {
    catLoading.value = false
  }
}

const resetCatQuery = () => {
  catQuery.diseaseType = undefined
  catQuery.status = undefined
  catQuery.keyword = ''
  catPage.pageNum = 1
  loadCatalogs()
}

const catFormVisible = ref(false)
const catSubmitting = ref(false)
const emptyCat = () => ({
  id: null as any, diseaseCode: '', diseaseName: '', diseaseType: 1,
  icdCode: '', defaultValidMonths: undefined as any, status: 1, remark: ''
})
const catForm = ref<any>(emptyCat())

const openCatCreate = () => {
  catForm.value = emptyCat()
  catFormVisible.value = true
}

const openCatEdit = (row: any) => {
  catForm.value = {...emptyCat(), ...row, id: row.id, defaultValidMonths: row.defaultValidMonths ?? undefined}
  catFormVisible.value = true
}

const saveCat = async () => {
  const f = catForm.value
  if (!f.diseaseCode || !f.diseaseName || !f.diseaseType) {
    ElMessage.warning('病种编码、名称、类别都要填')
    return
  }
  catSubmitting.value = true
  try {
    await upsertChronicCatalog({
      id: f.id || undefined, diseaseCode: f.diseaseCode.trim(), diseaseName: f.diseaseName.trim(),
      diseaseType: f.diseaseType, icdCode: f.icdCode || undefined,
      defaultValidMonths: f.defaultValidMonths ?? undefined, status: f.status, remark: f.remark || undefined
    })
    ElMessage.success(f.id ? '病种已更新' : '病种已新增')
    catFormVisible.value = false
    loadCatalogs()
  } catch (error: any) {
    ElMessage.error(error?.message || '病种保存失败')
  } finally {
    catSubmitting.value = false
  }
}

const toggleCatStatus = (row: any) => {
  const next = row.status === 1 ? 0 : 1
  const hint = next === 0
    ? `停用病种「${row.diseaseName}」？停用后不能再新备案，历史备案与已完成结算不受影响。`
    : `重新启用病种「${row.diseaseName}」？`
  ElMessageBox.confirm(hint, next === 0 ? '停用确认' : '启用确认', {type: 'warning'})
    .then(async () => {
      try {
        await changeChronicCatalogStatus(row.id, next)
        ElMessage.success(next === 0 ? '已停用' : '已启用')
        loadCatalogs()
      } catch (error: any) {
        ElMessage.error(error?.message || '病种状态变更失败')
      }
    })
    .catch(() => {})
}

/** 看该病种的备案台账：跳回备案页并带上病种筛选（原为页签内切 tab） */
const drillRegsOfCatalog = (row: any) => {
  router.push({path: '/insurance-chronic', query: {catalogId: String(row.id)}})
}

onMounted(async () => {
  dicts.value = await loadDictDataMap(DICT_TYPE.CHRONIC_DISEASE_TYPE)
  loadCatalogs()
})
</script>

<template>
  <div data-testid="chronic-catalog-page">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="catQuery" inline @submit.prevent>
          <el-form-item label="类别">
            <el-select v-model="catQuery.diseaseType" placeholder="全部类别" clearable style="width: 130px">
              <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="启停">
            <el-select v-model="catQuery.status" placeholder="全部启停" clearable style="width: 130px">
              <el-option label="启用" :value="1"/><el-option label="停用" :value="0"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="catQuery.keyword" placeholder="编码/名称/ICD" clearable style="width: 220px" data-testid="catalog-keyword"
                      @keyup.enter="catPage.pageNum = 1; loadCatalogs()"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="catPage.pageNum = 1; loadCatalogs()">查询</el-button>
            <el-button :icon="Refresh" @click="resetCatQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" :icon="Plus" v-perm="'finance:insuranceChronic:add'" data-testid="catalog-create-btn" @click="openCatCreate">新增病种</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="catLoading" :data="catRows" style="width: 100%" data-testid="catalog-table" stripe :max-height="tableMaxHeight">
        <el-table-column prop="diseaseCode" label="病种编码" width="120" class-name="font-mono"/>
        <el-table-column prop="diseaseName" label="病种名称" min-width="220" show-overflow-tooltip/>
        <el-table-column label="类别" width="100">
          <template #default="{row}">{{ dictText(DICT_TYPE.CHRONIC_DISEASE_TYPE, row.diseaseType) }}</template>
        </el-table-column>
        <el-table-column prop="icdCode" label="ICD-10" width="120" class-name="font-mono">
          <template #default="{row}">{{ row.icdCode || '—' }}</template>
        </el-table-column>
        <el-table-column label="默认有效期" width="120">
          <template #default="{row}">{{ row.defaultValidMonths ? `${row.defaultValidMonths} 个月` : '长期' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" :data-testid="`catalog-status-${row.diseaseCode}`">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip>
          <template #default="{row}">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button size="small" plain :data-testid="`catalog-drill-${row.diseaseCode}`" @click="drillRegsOfCatalog(row)">看备案</el-button>
            <el-button size="small" type="primary" plain :icon="Edit" v-perm="'finance:insuranceChronic:add'"
                       :data-testid="`catalog-edit-${row.diseaseCode}`" @click="openCatEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" plain v-perm="'finance:insuranceChronic:add'"
                       :data-testid="`catalog-toggle-${row.diseaseCode}`" @click="toggleCatStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="catPage.pageNum" v-model:page-size="catPage.pageSize"
                       :total="catPage.total" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next, jumper"
                       data-testid="catalog-pagination" @current-change="loadCatalogs" @size-change="catPage.pageNum = 1; loadCatalogs()"/>
      </div>
    </el-card>

    <!-- ==================== 病种目录 新建/编辑 ==================== -->
    <el-dialog v-model="catFormVisible" :title="catForm.id ? `修改病种 ${catForm.diseaseCode}` : '新增慢特病病种'" width="640px" data-testid="catalog-form-dialog">
      <el-form :model="catForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="病种编码" required>
              <el-input v-model="catForm.diseaseCode" placeholder="如 MZ012" data-testid="catalog-form-code"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别" required>
              <el-select v-model="catForm.diseaseType" style="width: 100%" data-testid="catalog-form-type">
                <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="病种名称" required>
              <el-input v-model="catForm.diseaseName" placeholder="与国家医保慢特病目录同名" data-testid="catalog-form-name"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ICD-10 主码">
              <el-input v-model="catForm.icdCode" placeholder="如 I10" data-testid="catalog-form-icd"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认有效期">
              <el-input-number v-model="catForm.defaultValidMonths" :min="1" :controls="false" style="width: 100%"
                               placeholder="留空＝长期" data-testid="catalog-form-months"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态">
              <el-switch v-model="catForm.status" :active-value="1" :inactive-value="0" data-testid="catalog-form-status"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="catForm.remark" type="textarea" :rows="2"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="catSubmitting" data-testid="catalog-form-save" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
