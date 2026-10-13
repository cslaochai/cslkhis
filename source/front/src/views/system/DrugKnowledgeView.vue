<template>
  <div class="space-y-4">
    <div>
      <h1 class="text-xl font-bold text-slate-900">合理用药知识库</h1>
      <p class="mt-1 text-sm text-slate-500">
        按<b>成分关键字</b>维护（药品名称或通用名包含即命中，所以「华法林」能命中「华法林钠片」）。
        <span class="font-medium text-rose-600">禁忌</span>级命中会直接拒绝审方通过，
        <span class="font-medium text-amber-600">慎用</span>级与剂量超量只标注不拦。改完立即生效，无需重启。
      </p>
    </div>

    <el-tabs v-model="activeTab" class="bg-white rounded-lg border border-slate-200 px-4 pt-2 shadow-sm">
      <el-tab-pane label="药物相互作用" name="interaction">
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-input
              v-model="intQuery.component"
              clearable
              data-testid="kb-int-component"
              placeholder="成分关键字（任一成分）"
              style="width: 200px"
              @keyup.enter="handleIntSearch"
          />
          <el-input
              v-model="intQuery.keyword"
              clearable
              placeholder="后果 / 建议正文"
              style="width: 200px"
              @keyup.enter="handleIntSearch"
          />
          <el-select v-model="intQuery.severity" :fit-input-width="false" clearable placeholder="严重度"
                     style="width: 130px">
            <el-option v-for="d in severityDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="intQuery.status" :fit-input-width="false" clearable placeholder="状态"
                     style="width: 120px">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="停用"/>
          </el-select>
          <el-button :icon="Search" data-testid="kb-int-search" type="primary" @click="handleIntSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleIntReset">重置</el-button>
          <el-button v-perm="'system:drugKnowledge:add'" :icon="Plus" class="ml-auto" data-testid="kb-int-add"
                     type="primary" @click="openIntAdd">
            新增
          </el-button>
        </div>

        <el-table v-loading="intLoading" :data="intRows" border data-testid="kb-int-table" row-key="id">
          <el-table-column label="成分对" min-width="160">
            <template #default="{ row }">
              <p class="text-[15px] font-medium text-slate-800">{{ row.componentA }} × {{ row.componentB }}</p>
              <p class="text-[13px] text-slate-400">{{ row.pairKey }}</p>
            </template>
          </el-table-column>
          <el-table-column align="center" label="严重度" width="90">
            <template #default="{ row }">
              <el-tag :type="severityTagType(Number(row.severity))" size="small">{{
                  severityLabel(Number(row.severity))
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="相互作用后果（审方提示原文）" min-width="320">
            <template #default="{ row }">
              <p class="text-[15px] text-slate-800 leading-relaxed">{{ row.interactionDesc }}</p>
              <p v-if="row.suggestion" class="text-[14px] text-slate-500 leading-relaxed">处理：{{ row.suggestion }}</p>
            </template>
          </el-table-column>
          <el-table-column align="center" label="命中药品" width="110">
            <template #default="{ row }">
              <span :class="hitsClass(row.drugHitsA)">{{ row.drugHitsA }}</span>
              <span class="text-slate-300"> / </span>
              <span :class="hitsClass(row.drugHitsB)">{{ row.drugHitsB }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="Number(row.status) === 1" size="small" type="success">启用</el-tag>
              <el-tag v-else size="small" type="info">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更新" width="150">
            <template #default="{ row }">
              <p class="text-[14px] text-slate-600">{{ row.updateBy || row.createBy || '铺底' }}</p>
              <p class="text-[13px] text-slate-400">{{ row.updateTime || row.createTime || '' }}</p>
            </template>
          </el-table-column>
          <el-table-column align="center" fixed="right" label="操作" width="210">
            <template #default="{ row }">
              <el-button v-perm="'system:drugKnowledge:add'" :icon="Edit" link type="primary" @click="openIntEdit(row)">
                编辑
              </el-button>
              <el-button
                  v-perm="'system:drugKnowledge:add'"
                  :type="Number(row.status) === 1 ? 'warning' : 'success'"
                  link
                  @click="toggleInt(row, Number(row.status) === 1 ? 0 : 1)"
              >
                {{ Number(row.status) === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button v-perm="'system:drugKnowledge:delete'" :icon="Delete" link type="danger"
                         @click="removeInt(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="intPage.pageNum"
              v-model:page-size="intPage.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="intPage.total"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleIntSearch"
              @current-change="loadInteractions"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="剂量上限" name="dose">
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-input
              v-model="doseQuery.component"
              clearable
              data-testid="kb-dose-component"
              placeholder="成分关键字"
              style="width: 200px"
              @keyup.enter="handleDoseSearch"
          />
          <el-input
              v-model="doseQuery.keyword"
              clearable
              placeholder="口径说明正文"
              style="width: 200px"
              @keyup.enter="handleDoseSearch"
          />
          <el-select v-model="doseQuery.status" :fit-input-width="false" clearable placeholder="状态"
                     style="width: 120px">
            <el-option :value="1" label="启用"/>
            <el-option :value="0" label="停用"/>
          </el-select>
          <el-button :icon="Search" data-testid="kb-dose-search" type="primary" @click="handleDoseSearch">查询
          </el-button>
          <el-button :icon="Refresh" @click="handleDoseReset">重置</el-button>
          <el-button v-perm="'system:drugKnowledge:add'" :icon="Plus" class="ml-auto" data-testid="kb-dose-add"
                     type="primary" @click="openDoseAdd">
            新增
          </el-button>
        </div>

        <el-table v-loading="doseLoading" :data="doseRows" border data-testid="kb-dose-table" row-key="id">
          <el-table-column label="成分" min-width="140">
            <template #default="{ row }">
              <p class="text-[15px] font-medium text-slate-800">{{ row.component }}</p>
              <p :class="hitsClass(row.drugHits)" class="text-[13px]">命中 {{ row.drugHits }} 种在用药</p>
            </template>
          </el-table-column>
          <el-table-column align="center" label="单位" prop="doseUnit" width="80"/>
          <el-table-column align="right" label="单次最大量" width="120">
            <template #default="{ row }">
              <span v-if="row.maxSingleDose !== null" class="text-[15px] text-slate-800">{{
                  row.maxSingleDose
                }}{{ row.doseUnit }}</span>
              <span v-else class="text-slate-400">不判</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="每日最大量" width="120">
            <template #default="{ row }">
              <span v-if="row.maxDailyDose !== null" class="text-[15px] text-slate-800">{{
                  row.maxDailyDose
                }}{{ row.doseUnit }}</span>
              <span v-else class="text-slate-400">不判</span>
            </template>
          </el-table-column>
          <el-table-column label="口径说明（超量提示会带上）" min-width="300" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="text-[15px] text-slate-700">{{ row.note || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="Number(row.status) === 1" size="small" type="success">启用</el-tag>
              <el-tag v-else size="small" type="info">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" fixed="right" label="操作" width="210">
            <template #default="{ row }">
              <el-button v-perm="'system:drugKnowledge:add'" :icon="Edit" link type="primary"
                         @click="openDoseEdit(row)">编辑
              </el-button>
              <el-button
                  v-perm="'system:drugKnowledge:add'"
                  :type="Number(row.status) === 1 ? 'warning' : 'success'"
                  link
                  @click="toggleDose(row, Number(row.status) === 1 ? 0 : 1)"
              >
                {{ Number(row.status) === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button v-perm="'system:drugKnowledge:delete'" :icon="Delete" link type="danger"
                         @click="removeDose(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="dosePage.pageNum"
              v-model:page-size="dosePage.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="dosePage.total"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleDoseSearch"
              @current-change="loadDoseLimits"
          />
        </div>

        <p class="mt-3 text-[14px] leading-relaxed text-slate-500">
          单位只有 <span class="font-medium text-slate-700">g / mg / ug</span> 三种可比：
          注射剂的「支」、胰岛素笔的「IU」与包装量都不等于一次给药量，所以这类规格解不出来时
          <span class="font-medium text-slate-700">系统选择不判</span>，宁漏报也不给药师一条假超量。
          复方制剂、一个规格里写两个含量的、按周给药的药，同样不在本表口径内。
        </p>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/修改表单拆到同级子组件，页面只控显隐、传行与保存后刷新 -->
    <DrugInteractionEdit v-model="intDialog" :row="intRow" :severity-options="severityDict" @saved="loadInteractions"/>
    <DrugDoseLimitEdit v-model="doseDialog" :dose-unit-options="doseUnitDict" :row="doseRow" @saved="loadDoseLimits"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  doseLimitDeleteById,
  doseLimitListPage,
  doseLimitUpsert,
  interactionDeleteById,
  interactionListPage,
  interactionUpsert,
} from '@/api/drugKnowledge';
import {getDictDataMapList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import DrugDoseLimitEdit from './DrugDoseLimitEdit.vue';
import DrugInteractionEdit from './DrugInteractionEdit.vue';

/**
 * 系统管理 → 合理用药知识库（sql/130）
 *
 * 两个页签两套数据：
 *  · 相互作用 = sys_drug_interaction（成对成分 + 严重度 + 后果原文）；
 *  · 剂量上限 = sys_drug_dose_limit（一个成分一条极量）。
 *
 * 为什么值得把这个页面做厚一点：审方药师点「通过」被拒时，屏幕上那句理由<b>就是</b>这里的
 * interaction_desc 原文（后端逐字引用，不前端拼装）。所以这一页写的每一个字都是发给
 * 医生的正式结论，改这里等于改规则本身 —— 也因此「命中药品数」必须显示出来：
 * 0 只说明本院字典还没进这个药（知识储备），而不是写错了，但没这个数字就没法区分这两件事。
 */
const activeTab = ref('interaction');
/** 严重度字典 his_drug_interaction_severity（1-禁忌 2-慎用），取值以字典为准不写死 */
const severityDict = ref([]);
const doseUnitDict = ref([]);
const severityLabel = (v) => severityDict.value.find(d => String(d.dictValue) === String(v))?.dictLabel || (v === 1 ? '禁忌' : '慎用');
const severityTagType = (v) => (v === 1 ? 'danger' : 'warning');

async function loadDicts() {
  try {
    const res = await getDictDataMapList('his_drug_interaction_severity,his_dose_unit');
    severityDict.value = res.data?.his_drug_interaction_severity || [];
    doseUnitDict.value = res.data?.his_dose_unit || [];
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

/* ==================== 页签一：药物相互作用 ==================== */
const intLoading = ref(false);
const intRows = ref([]);
const intQuery = reactive({
  component: '',
  keyword: '',
  severity: null,
  status: null,
});
const intPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 表单弹框（新增/修改）状态整体搬进 DrugInteractionEdit.vue，这里只控显隐与传行
const intDialog = ref(false);
const intRow = ref(null);

async function loadInteractions() {
  intLoading.value = true;
  try {
    const res = await interactionListPage({
      component: intQuery.component || undefined,
      keyword: intQuery.keyword || undefined,
      severity: intQuery.severity,
      status: intQuery.status,
      pageNum: intPage.pageNum,
      pageSize: intPage.pageSize,
    });
    intRows.value = res.data?.records || [];
    intPage.total = Number(res.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    intLoading.value = false;
  }
}

function handleIntSearch() {
  intPage.pageNum = 1;
  loadInteractions();
}

function handleIntReset() {
  intQuery.component = '';
  intQuery.keyword = '';
  intQuery.severity = null;
  intQuery.status = null;
  handleIntSearch();
}

function openIntAdd() {
  intRow.value = null;
  intDialog.value = true;
}

function openIntEdit(row) {
  intRow.value = row;
  intDialog.value = true;
}

async function toggleInt(row, next) {
  try {
    // 启停与新增/修改在后端是同一个 xxxUpsert，所以整行原样回传、只改 status
    await interactionUpsert({
      id: row.id,
      componentA: row.componentA,
      componentB: row.componentB,
      severity: Number(row.severity),
      interactionDesc: row.interactionDesc,
      suggestion: row.suggestion,
      status: next,
      remark: row.remark,
    });
    ElMessage.success(next === 1 ? '已启用' : '已停用（保留条目，不再参与比对）');
    await loadInteractions();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

async function removeInt(row) {
  try {
    await ElMessageBox.confirm(`删除「${row.componentA} × ${row.componentB}」是物理删除（同成分对的唯一键会一并释放），`
        + '若只是暂时不想生效，请用「停用」。确定删除？', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await interactionDeleteById(row.id);
    ElMessage.success('删除成功');
    await loadInteractions();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

/* ==================== 页签二：剂量上限 ==================== */
const doseLoading = ref(false);
const doseRows = ref([]);
const doseQuery = reactive({component: '', keyword: '', status: null});
const dosePage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 表单弹框（新增/修改）状态整体搬进 DrugDoseLimitEdit.vue，这里只控显隐与传行
const doseDialog = ref(false);
const doseRow = ref(null);

async function loadDoseLimits() {
  doseLoading.value = true;
  try {
    const res = await doseLimitListPage({
      component: doseQuery.component || undefined,
      keyword: doseQuery.keyword || undefined,
      status: doseQuery.status,
      pageNum: dosePage.pageNum,
      pageSize: dosePage.pageSize,
    });
    doseRows.value = res.data?.records || [];
    dosePage.total = Number(res.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    doseLoading.value = false;
  }
}

function handleDoseSearch() {
  dosePage.pageNum = 1;
  loadDoseLimits();
}

function handleDoseReset() {
  doseQuery.component = '';
  doseQuery.keyword = '';
  doseQuery.status = null;
  handleDoseSearch();
}

function openDoseAdd() {
  doseRow.value = null;
  doseDialog.value = true;
}

function openDoseEdit(row) {
  doseRow.value = row;
  doseDialog.value = true;
}

async function toggleDose(row, next) {
  try {
    await doseLimitUpsert({
      id: row.id,
      component: row.component,
      doseUnit: row.doseUnit,
      maxSingleDose: row.maxSingleDose,
      maxDailyDose: row.maxDailyDose,
      note: row.note,
      status: next,
      remark: row.remark,
    });
    ElMessage.success(next === 1 ? '已启用' : '已停用');
    await loadDoseLimits();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

async function removeDose(row) {
  try {
    await ElMessageBox.confirm(`删除「${row.component}」的剂量上限是物理删除，确定继续？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await doseLimitDeleteById(row.id);
    ElMessage.success('删除成功');
    await loadDoseLimits();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
}

/** 命中药品数显示：0 单独标灰，提醒「这条知识现在打不到任何药」 */
const hitsClass = (n) => (Number(n) > 0 ? 'text-slate-700' : 'text-slate-400');
onMounted(() => {
  loadDicts();
  loadInteractions();
  loadDoseLimits();
});
</script>
