<template>
  <div class="antibiotic-catalog-page" data-testid="antibiotic-catalog-view">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="抗菌药物分级管理"
      description="三级目录（非限制使用级 / 限制使用级 / 特殊使用级）是开方闸的依据：医师授权级别必须 ≥ 药品分级才允许开方（医师那一侧在「抗菌药物处方权管理」菜单维护）。没有 DDD 值的抗菌药进不了使用强度统计 —— 纳入目录时 DDD 值与每单位含药量必须一起填（发药单位是盒/瓶/支，不是最小制剂单位）。" />

    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- ============ tab1 分级目录 ============ -->
      <el-tab-pane label="分级目录" name="catalog">
        <div class="toolbar">
          <div data-testid="catalog-keyword">
            <el-input
              v-model="catalogQuery.keyword"
              placeholder="药品名称 / 通用名 / 编码"
              clearable
              style="width: 220px"
              @keyup.enter="loadCatalog"
              @clear="loadCatalog" />
          </div>
          <div data-testid="catalog-level">
            <el-select v-model="catalogQuery.levelFilter" placeholder="分级" clearable style="width: 150px" @change="loadCatalog">
              <el-option v-for="(text, code) in ANTIBIOTIC_LEVEL" :key="code" :label="text" :value="Number(code)" />
            </el-select>
          </div>
          <el-button type="primary" data-testid="btn-catalog-query" @click="loadCatalog">查询</el-button>
          <el-button @click="resetCatalog">重置</el-button>
        </div>

        <el-table :data="catalogRows" border stripe v-loading="catalogLoading" data-testid="catalog-table">
          <el-table-column prop="drugName" label="药品名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" width="130" />
          <el-table-column prop="dosageForm" label="剂型" width="90" />
          <el-table-column prop="unit" label="发药单位" width="90" />
          <el-table-column label="抗菌药物分级" width="140">
            <template #default="{ row }">
              <el-tag v-if="row.inCatalog" :type="levelTagType(row.antibioticLevel)" data-testid="cell-level">
                {{ antibioticLevelText(row.antibioticLevel) }}
              </el-tag>
              <span v-else class="muted">未纳入</span>
            </template>
          </el-table-column>
          <el-table-column label="WHO DDD（g/日）" width="130" align="right">
            <template #default="{ row }">{{ row.dddValue ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="每单位含药量（g）" width="140" align="right">
            <template #default="{ row }">{{ row.dddUnitGram ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-perm="'pharmacy:antibiotic:catalogEdit'" link type="primary" @click="openLevelDialog(row)">
                维护分级
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- ⚠ el-pagination 必须给 page-size 配监听（v-model:page-size + @size-change）：
             EP 2.14 在 layout 含 sizes 且传了 :page-size 却没有 page-size 监听时，
             整个组件渲染成 null（控制台只给一句"废弃用法"警告，页面表现是"分页条不见了"）。
             所以不能用单向 :page-size，必须 v-model + size-change 都写。 -->
        <el-pagination
          class="pager"
          layout="total, sizes, prev, pager, next"
          :total="catalogTotal"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="catalogQuery.pageNum"
          v-model:page-size="catalogQuery.pageSize"
          @current-change="loadCatalog"
          @size-change="onCatalogSizeChange" />
      </el-tab-pane>

      <!-- ============ tab2 医嘱别名 ============ -->
      <el-tab-pane label="医嘱别名" name="alias">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="监测页「未匹配医嘱数」> 0 时来这里"
          description="住院医嘱写的是「注射用头孢曲松钠」，药品目录里叫「头孢曲松钠粉针」——名字对不上就进不了使用强度统计。别名是精确匹配键，不靠模糊匹配（模糊会把所有头孢算成一种药）。" />
        <div class="toolbar">
          <el-button
            v-perm="'pharmacy:antibiotic:catalogEdit'"
            type="primary"
            data-testid="btn-new-alias"
            @click="openAliasDialog()">新增别名</el-button>
        </div>
        <el-table :data="aliasRows" border stripe v-loading="aliasLoading" data-testid="alias-table">
          <el-table-column prop="aliasName" label="别名（医嘱写法）" min-width="220" show-overflow-tooltip />
          <el-table-column prop="drugName" label="指向药品" min-width="180" show-overflow-tooltip />
          <el-table-column label="该药分级" width="140">
            <template #default="{ row }">
              <el-tag :type="levelTagType(aliasLevel(row.drugId))">{{ antibioticLevelText(aliasLevel(row.drugId)) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button v-perm="'pharmacy:antibiotic:catalogEdit'" link type="danger" @click="doDeleteAlias(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 维护分级 ============ -->
    <el-dialog v-model="levelDialogVisible" title="维护抗菌药物分级" width="520px">
      <el-form :model="levelForm" label-width="140px">
        <el-form-item label="药品">
          <span>{{ levelForm.drugName }}<span class="muted">（{{ levelForm.specification || '—' }} / 发药单位：{{ levelForm.unit || '—' }}）</span></span>
        </el-form-item>
        <el-form-item label="抗菌药物分级" required>
          <div data-testid="level-select" style="width: 100%">
            <el-select v-model="levelForm.antibioticLevel" style="width: 100%">
              <el-option v-for="(text, code) in ANTIBIOTIC_LEVEL" :key="code" :label="text" :value="Number(code)" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item v-if="levelForm.antibioticLevel > 0" label="WHO DDD（g/日）" required>
          <el-input v-model="levelForm.dddValue" data-testid="level-ddd-value" placeholder="如 1.0" />
        </el-form-item>
        <el-form-item v-if="levelForm.antibioticLevel > 0" label="每单位含药量（g）" required>
          <el-input v-model="levelForm.dddUnitGram" data-testid="level-ddd-unit" placeholder="按包装折算，如 0.25g×24粒 = 6.0" />
        </el-form-item>
        <div v-if="levelForm.antibioticLevel > 0" class="form-tip">
          DDDs = 发药数量 × 每单位含药量 ÷ DDD 值；使用强度 AUD = DDDs × 100 ÷ 收治患者人天数
        </div>
      </el-form>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-save-level" @click="saveLevel">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 新增别名 ============ -->
    <el-dialog v-model="aliasDialogVisible" title="新增医嘱别名" width="520px">
      <el-form :model="aliasForm" label-width="130px">
        <el-form-item label="别名（医嘱写法）" required>
          <el-input v-model="aliasForm.aliasName" data-testid="alias-name" placeholder="如：注射用头孢曲松钠" />
        </el-form-item>
        <el-form-item label="指向药品" required>
          <div data-testid="alias-drug-select" style="width: 100%">
            <el-select v-model="aliasForm.drugId" filterable placeholder="选择抗菌药物目录里的药品" style="width: 100%">
              <el-option
                v-for="d in drugOptions"
                :key="d.id"
                :label="`${d.drugName}（${ANTIBIOTIC_LEVEL[d.antibioticLevel]}）`"
                :value="d.id" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="aliasForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="aliasDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-save-alias" @click="saveAlias">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ANTIBIOTIC_LEVEL,
  antibioticLevelText
} from '@/lib/antibiotic'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {
  deleteAntibioticAlias,
  getAntibioticDrugSelectList,
  listAntibioticAlias,
  listAntibioticCatalogPage,
  upsertAntibioticAlias,
  upsertAntibioticCatalogLevel
} from '@/api/antibiotic'

const activeTab = ref('catalog')
const saving = ref(false)

function levelTagType(level) {
  return level === 3 ? 'danger' : level === 2 ? 'warning' : level === 1 ? 'success' : 'info'
}

// ---------- 分级目录 ----------
const catalogQuery = reactive({ keyword: '', levelFilter: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const catalogRows = ref([])
const catalogTotal = ref(0)
const catalogLoading = ref(false)

async function loadCatalog() {
  catalogLoading.value = true
  try {
    const res = await listAntibioticCatalogPage({ ...catalogQuery })
    catalogRows.value = res?.data?.records || []
    catalogTotal.value = Number(res?.data?.total || 0)
  } finally {
    catalogLoading.value = false
  }
}

function onCatalogSizeChange() {
  catalogQuery.pageNum = 1
  loadCatalog()
}

function resetCatalog() {
  catalogQuery.keyword = ''
  catalogQuery.levelFilter = null
  catalogQuery.pageNum = 1
  loadCatalog()
}

const levelDialogVisible = ref(false)
const levelForm = reactive({ id: null, drugName: '', specification: '', unit: '', antibioticLevel: 0, dddValue: '', dddUnitGram: '' })

function openLevelDialog(row) {
  Object.assign(levelForm, {
    id: row.id,
    drugName: row.drugName,
    specification: row.specification,
    unit: row.unit,
    antibioticLevel: row.antibioticLevel || 0,
    dddValue: row.dddValue == null ? '' : String(row.dddValue),
    dddUnitGram: row.dddUnitGram == null ? '' : String(row.dddUnitGram)
  })
  levelDialogVisible.value = true
}

async function saveLevel() {
  saving.value = true
  try {
    await upsertAntibioticCatalogLevel({
      id: levelForm.id,
      antibioticLevel: levelForm.antibioticLevel,
      dddValue: levelForm.antibioticLevel > 0 ? Number(levelForm.dddValue) : null,
      dddUnitGram: levelForm.antibioticLevel > 0 ? Number(levelForm.dddUnitGram) : null
    })
    ElMessage.success('已保存')
    levelDialogVisible.value = false
    loadCatalog()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ---------- 别名 ----------
const aliasRows = ref([])
const aliasLoading = ref(false)
const drugOptions = ref([])
const aliasDialogVisible = ref(false)
const aliasForm = reactive({ drugId: null, aliasName: '', remark: '' })

async function loadAlias() {
  aliasLoading.value = true
  try {
    const res = await listAntibioticAlias()
    aliasRows.value = res?.data || []
  } finally {
    aliasLoading.value = false
  }
}

function aliasLevel(drugId) {
  const d = drugOptions.value.find(x => String(x.id) === String(drugId))
  return d ? d.antibioticLevel : null
}

function openAliasDialog() {
  aliasForm.drugId = null
  aliasForm.aliasName = ''
  aliasForm.remark = ''
  aliasDialogVisible.value = true
}

async function saveAlias() {
  saving.value = true
  try {
    await upsertAntibioticAlias({ ...aliasForm })
    ElMessage.success('已保存')
    aliasDialogVisible.value = false
    loadAlias()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function doDeleteAlias(row) {
  try {
    await ElMessageBox.confirm(`删除别名「${row.aliasName}」后，该写法的住院医嘱将不再计入使用强度。确认删除？`, '删除别名', { type: 'warning' })
  } catch { return }
  await deleteAntibioticAlias(row.id)
  ElMessage.success('已删除')
  loadAlias()
}

onMounted(async () => {
  loadCatalog()
  loadAlias()
  const res = await getAntibioticDrugSelectList()
  drugOptions.value = res?.data || []
})
</script>

<style scoped>
.antibiotic-catalog-page {
  padding: 12px;
}
.main-tabs {
  margin-top: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.pager {
  margin-top: 10px;
  justify-content: flex-end;
}
.muted {
  color: #909399;
}
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
  margin-bottom: 10px;
}
</style>
