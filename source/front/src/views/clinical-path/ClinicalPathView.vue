<template>
  <div class="p-5">
    <el-tabs v-model="activeTab" data-testid="cp-tabs" @tab-change="onTabChange">
      <!-- ============ 页签一：路径模板 ============ -->
      <el-tab-pane label="路径模板" name="templates">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">路径模板（草稿→使用中→已停用）</h3>
            <el-input v-model="tplQuery.keyword" class="w-52" clearable data-testid="cp-tpl-keyword"
                      placeholder="编码/名称/诊断"/>
            <el-select v-model="tplQuery.status" :fit-input-width="false" class="w-32" clearable placeholder="状态">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" @click="() => { tplQuery.pageNum = 1; loadTpl() }">查询</el-button>
            <el-button :icon="Refresh" @click="resetTpl">重置</el-button>
            <el-button v-perm="'qc:clinicalPath:add'" :icon="Plus" data-testid="cp-tpl-add-btn" type="primary"
                       @click="openTplEdit()">新增模板
            </el-button>
          </div>

          <el-table v-loading="tplLoading" :data="tplRows" border data-testid="cp-tpl-table">
            <el-table-column label="路径编码" prop="pathwayCode" width="130"/>
            <el-table-column label="路径名称" min-width="180" prop="pathwayName"/>
            <el-table-column label="适用科室" width="130">
              <template #default="{ row }">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="适用病种" min-width="160" prop="diagnosis">
              <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
            </el-table-column>
            <el-table-column label="版本" prop="version" width="70"/>
            <el-table-column align="right" label="总日数" prop="totalDays" width="80"/>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="pathwayStatusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发布人" prop="publishBy" width="90">
              <template #default="{ row }">{{ row.publishBy || '—' }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="220">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="openTplView(row)">查看</el-button>
                <el-button v-if="canEditPathway(row.status)" v-perm="'qc:clinicalPath:add'" data-testid="cp-tpl-edit-btn" link
                           size="small"
                           type="primary" @click="openTplEdit(row)">编辑
                </el-button>
                <el-button v-if="canPublish(row.status)" v-perm="'qc:clinicalPath:add'" data-testid="cp-tpl-publish-btn" link size="small"
                           type="success" @click="onPublish(row)">发布
                </el-button>
                <el-button v-if="canDeprecate(row.status)" v-perm="'qc:clinicalPath:add'" data-testid="cp-tpl-deprecate-btn" link
                           size="small"
                           type="danger" @click="onDeprecate(row)">停用
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400">还没有路径模板，点「新增模板」开始维护</div>
            </template>
          </el-table>

          <el-pagination v-model:current-page="tplQuery.pageNum" v-model:page-size="tplQuery.pageSize"
                         :total="tplTotal" layout="total, prev, pager, next" @current-change="loadTpl"/>
        </div>
      </el-tab-pane>

      <!-- ============ 页签二：在径管理 ============ -->
      <el-tab-pane label="在径管理" name="enroll">
        <div class="space-y-4">
          <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
            <h3 class="font-semibold text-slate-800">入径登记（在院且无在径记录的患者）</h3>
            <div class="flex items-center gap-3 flex-wrap">
              <el-input v-model="candKeyword" class="w-52" clearable data-testid="cp-cand-keyword"
                        placeholder="姓名/患者号/入院号"
                        @keyup.enter="loadCandidates"/>
              <el-button :icon="Search" :loading="candLoading" @click="loadCandidates">查询在院患者</el-button>
              <el-select v-model="enrollForm.admissionId" :fit-input-width="false" class="w-80" data-testid="cp-enroll-patient-select"
                         filterable
                         placeholder="选择患者">
                <el-option v-for="c in candidates" :key="c.admissionId" :label="`${c.patientName}（${c.patientNo}）${c.admissionNo} ${c.deptName || ''}`"
                           :value="c.admissionId"/>
              </el-select>
              <el-select v-model="enrollForm.pathwayId" :fit-input-width="false" class="w-72" data-testid="cp-enroll-pathway-select"
                         filterable
                         placeholder="选择路径模板">
                <el-option v-for="p in activePathways" :key="p.id" :label="`${p.pathwayName} ${p.version}（${p.pathwayCode}）`"
                           :value="p.id"/>
              </el-select>
              <el-date-picker v-model="enrollForm.enrollDate" :clearable="false" class="w-40"
                              placeholder="入径日期" type="date" value-format="YYYY-MM-DD"/>
              <el-button v-perm="'qc:clinicalPath:edit'" :disabled="!enrollForm.admissionId || !enrollForm.pathwayId" :loading="enrolling"
                         data-testid="cp-enroll-btn"
                         type="primary" @click="doEnroll">入径
              </el-button>
            </div>
            <el-table v-if="candidates.length" :data="candidates" border max-height="200" size="small">
              <el-table-column label="患者" prop="patientName" width="110"/>
              <el-table-column label="患者编号" prop="patientNo" width="140"/>
              <el-table-column label="入院记录号" prop="admissionNo" width="150"/>
              <el-table-column label="科室" prop="deptName" width="130"/>
              <el-table-column label="入院时间" prop="admitTime" width="160"/>
              <el-table-column label="入院诊断" min-width="180" prop="diagnosis"/>
            </el-table>
          </div>

          <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
            <div class="flex items-center gap-3 flex-wrap">
              <h3 class="font-semibold text-slate-800 mr-2">入径台账</h3>
              <el-select v-model="enQuery.pathwayId" :fit-input-width="false" class="w-60" clearable filterable
                         placeholder="模板">
                <el-option v-for="p in activePathways" :key="p.id" :label="`${p.pathwayName} ${p.version}`"
                           :value="p.id"/>
              </el-select>
              <el-input v-model="enQuery.patientName" class="w-40" clearable data-testid="cp-en-patient-search"
                        placeholder="患者姓名"/>
              <el-select v-model="enQuery.status" :fit-input-width="false" class="w-32" clearable placeholder="状态">
                <el-option v-for="d in enrollStatusDict" :key="d.dictValue" :label="d.dictLabel"
                           :value="Number(d.dictValue)"/>
              </el-select>
              <el-date-picker v-model="enQuery.enrollDate" class="w-40" placeholder="入径日期" type="date"
                              value-format="YYYY-MM-DD"/>
              <el-button :icon="Search" @click="() => { enQuery.pageNum = 1; loadEnroll() }">查询</el-button>
              <el-button :icon="Refresh" @click="resetEn">重置</el-button>
            </div>

            <el-table v-loading="enLoading" :data="enRows" border data-testid="cp-enroll-table">
              <el-table-column label="入径单号" prop="enrollNo" width="140"/>
              <el-table-column label="路径" min-width="190">
                <template #default="{ row }">{{ row.pathwayName }} <span
                    class="text-xs text-slate-400">{{ row.pathwayCode }} {{ row.version }}</span></template>
              </el-table-column>
              <el-table-column label="患者" min-width="130">
                <template #default="{ row }">{{ row.patientName }} <span class="text-xs text-slate-400">{{
                    row.patientNo
                  }}</span></template>
              </el-table-column>
              <el-table-column label="科室" prop="deptName" width="120">
                <template #default="{ row }">{{ row.deptName || '—' }}</template>
              </el-table-column>
              <el-table-column label="入径日期" prop="enrollDate" width="105"/>
              <el-table-column align="center" label="路径日" width="90">
                <template #default="{ row }">
                  <span class="font-semibold text-sky-700">{{ row.currentDay ?? '—' }}</span>
                  <span class="text-xs text-slate-400">/{{ row.totalDays }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="enrollStatusTag(row.status)" size="small">{{ enrollStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column align="right" label="变异" prop="varianceCount" width="70"/>
              <el-table-column fixed="right" label="操作" width="80">
                <template #default="{ row }">
                  <el-button link size="small" type="primary" @click="openEnrollDetail(row.id)">明细</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">当前筛选条件下没有入径记录</div>
              </template>
            </el-table>

            <el-pagination v-model:current-page="enQuery.pageNum" v-model:page-size="enQuery.pageSize"
                           :total="enTotal" layout="total, prev, pager, next" @current-change="loadEnroll"/>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 页签三：变异分析 ============ -->
      <el-tab-pane label="变异分析" name="analysis">
        <div v-loading="anaLoading" class="space-y-4">
          <div class="bg-white rounded-lg border border-slate-200 p-4 flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">变异分析</h3>
            <el-select v-model="anaPathwayId" :fit-input-width="false" class="w-72" clearable data-testid="cp-ana-pathway-select"
                       filterable
                       placeholder="全部模板">
              <el-option v-for="p in allPathways" :key="p.id" :label="`${p.pathwayName} ${p.version}（${p.pathwayCode}）`"
                         :value="p.id"/>
            </el-select>
            <el-button :icon="Search" @click="loadAnalysis">查询</el-button>
          </div>

          <div class="grid grid-cols-5 gap-3">
            <div v-for="c in anaCards" :key="c.label" class="bg-white rounded-lg border border-slate-200 px-4 py-3">
              <div class="text-xs text-slate-500">{{ c.label }}</div>
              <div :class="c.tone" class="text-2xl font-semibold mt-1">{{ c.value }}</div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
            <h3 class="font-semibold text-slate-800">模板维度</h3>
            <el-table :data="ana?.rows || []" border data-testid="cp-ana-rows" size="small">
              <el-table-column label="路径编码" prop="pathwayCode" width="130"/>
              <el-table-column label="路径名称" min-width="180" prop="pathwayName"/>
              <el-table-column label="版本" prop="version" width="70"/>
              <el-table-column align="right" label="入径数" prop="enrollCount" width="90"/>
              <el-table-column align="right" label="完成" prop="finishCount" width="80"/>
              <el-table-column align="right" label="退径" prop="abortCount" width="80"/>
              <el-table-column align="right" label="变异条数" prop="varianceCount" width="90"/>
              <el-table-column align="right" label="完成率" width="90">
                <template #default="{ row }">{{ row.finishRate != null ? row.finishRate + '%' : '—' }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
              <h3 class="font-semibold text-slate-800">变异类型分布</h3>
              <el-table :data="ana?.typeStats || []" border data-testid="cp-ana-types" max-height="320" size="small">
                <el-table-column label="类型" min-width="140">
                  <template #default="{ row }">{{ varianceTypeText(row.varianceType) }}</template>
                </el-table-column>
                <el-table-column align="right" label="条数" prop="cnt" width="90"/>
              </el-table>
            </div>
            <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
              <h3 class="font-semibold text-slate-800">变异原因 TOP10</h3>
              <el-table :data="ana?.topReasons || []" border data-testid="cp-ana-reasons" max-height="320" size="small">
                <el-table-column label="原因" min-width="220" prop="reason" show-overflow-tooltip/>
                <el-table-column align="right" label="条数" prop="cnt" width="90"/>
              </el-table>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 模板编辑弹窗（草稿）：表单状态、步骤清单与保存都在子组件里 -->
    <ClinicalPathTplEdit v-model="tplEditVisible" :item-type-dict="itemTypeDict" :row="tplRow" @saved="loadTpl"/>

    <!-- 模板详情（只读，组件按 id 自取） -->
    <ClinicalPathTplDetail v-model="tplViewVisible" :id="tplViewId" :item-type-dict="itemTypeDict"
                           :status-dict="statusDict"/>

    <!-- 入径详情工作区（含「登记变异」子弹框；完成/退径确认后组件内重取详情） -->
    <ClinicalPathEnrollDetail v-model="detailVisible" :enroll-status-dict="enrollStatusDict" :id="detailId"
                              :item-type-dict="itemTypeDict" :variance-type-dict="varianceTypeDict"
                              @saved="loadEnroll"
                              @changed="loadEnroll(); loadCandidates()"/>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  deprecatePathway,
  enrollUpsert,
  getActivePathwayList,
  getPathwayAnalysis,
  listAdmissionsForEnroll,
  listEnrollPage,
  listPathwayPage,
  publishPathway,
} from '@/api/pathway';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {
  canDeprecate,
  canEditPathway,
  canPublish,
  enrollStatusTag,
  pathwayStatusTag,
} from '@/lib/pathway';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import ClinicalPathEnrollDetail from './ClinicalPathEnrollDetail.vue';
import ClinicalPathTplDetail from './ClinicalPathTplDetail.vue';
import ClinicalPathTplEdit from './ClinicalPathTplEdit.vue';

const activeTab = ref('templates');
const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};
// ---------------- 字典 ----------------
const statusDict = ref([]);
const enrollStatusDict = ref([]);
const varianceTypeDict = ref([]);
const itemTypeDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const enrollStatusText = (v) => dictLabelText(enrollStatusDict.value, v);
const varianceTypeText = (v) => dictLabelText(varianceTypeDict.value, v);
// ---------------- 页签一：模板 ----------------
const tplLoading = ref(false);
const tplRows = ref([]);
const tplTotal = ref(0);
const tplQuery = reactive({
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.PATHWAY_STATUS},${DICT_TYPE.PATHWAY_ENROLL_STATUS},${DICT_TYPE.PATHWAY_VARIANCE_TYPE},${DICT_TYPE.PATHWAY_ITEM_TYPE}`);
    statusDict.value = res?.data?.[DICT_TYPE.PATHWAY_STATUS] || [];
    enrollStatusDict.value = res?.data?.[DICT_TYPE.PATHWAY_ENROLL_STATUS] || [];
    varianceTypeDict.value = res?.data?.[DICT_TYPE.PATHWAY_VARIANCE_TYPE] || [];
    itemTypeDict.value = res?.data?.[DICT_TYPE.PATHWAY_ITEM_TYPE] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadTpl = async () => {
  tplLoading.value = true;
  try {
    const res = await listPathwayPage({
      keyword: tplQuery.keyword.trim() || undefined,
      status: tplQuery.status ?? undefined,
      pageNum: tplQuery.pageNum,
      pageSize: tplQuery.pageSize,
    });
    if (res.code === 200) {
      tplRows.value = res.data?.records || [];
      tplTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询模板失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询模板失败');
  } finally {
    tplLoading.value = false;
  }
};
const resetTpl = () => {
  tplQuery.keyword = '';
  tplQuery.status = null;
  tplQuery.pageNum = 1;
  loadTpl();
};
// ---- 模板编辑弹窗（已拆到 ClinicalPathTplEdit，这里只管打开） ----
const tplEditVisible = ref(false);
const tplRow = ref(null);
const openTplEdit = (row) => {
  tplRow.value = row || null;
  tplEditVisible.value = true;
};
const onPublish = async (row) => {
  try {
    await ElMessageBox.confirm(`确认发布「${row.pathwayName} ${row.version}」？发布后锁定不可编辑，同编码只允许一张使用中。`, '发布模板', {
      confirmButtonText: '确认发布',
      cancelButtonText: '取消'
    });
    const res = await publishPathway({id: row.id});
    if (res.code === 200) {
      ElMessage.success('已发布为使用中');
      await loadTpl();
    } else {
      ElMessage.error(res.message || '发布失败');
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  }
};
const onDeprecate = async (row) => {
  try {
    await ElMessageBox.confirm(`确认停用「${row.pathwayName} ${row.version}」？停用后不再可入径，存量在径不受影响。`, '停用模板', {
      confirmButtonText: '确认停用',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const res = await deprecatePathway({id: row.id});
    if (res.code === 200) {
      ElMessage.success('已停用');
      await loadTpl();
    } else {
      ElMessage.error(res.message || '停用失败');
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  }
};
// ---- 模板详情（已拆到 ClinicalPathTplDetail，组件按 id 自取） ----
const tplViewVisible = ref(false);
const tplViewId = ref('');
const openTplView = (row) => {
  tplViewId.value = String(row.id);
  tplViewVisible.value = true;
};
// ---------------- 页签二：在径管理 ----------------
const enLoading = ref(false);
const enRows = ref([]);
const enTotal = ref(0);
const enQuery = reactive({
  pathwayId: null,
  patientName: '',
  status: null,
  enrollDate: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const activePathways = ref([]);
const loadActivePathways = async () => {
  try {
    const res = await getActivePathwayList({});
    if (res.code === 200)
      activePathways.value = res.data || [];
  } catch (e) {
    console.error('加载使用中模板失败', e);
  }
};
const loadEnroll = async () => {
  enLoading.value = true;
  try {
    const res = await listEnrollPage({
      pathwayId: enQuery.pathwayId ?? undefined,
      patientName: enQuery.patientName.trim() || undefined,
      status: enQuery.status ?? undefined,
      enrollDate: enQuery.enrollDate || undefined,
      pageNum: enQuery.pageNum,
      pageSize: enQuery.pageSize,
    });
    if (res.code === 200) {
      enRows.value = res.data?.records || [];
      enTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询入径台账失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询入径台账失败');
  } finally {
    enLoading.value = false;
  }
};
const resetEn = () => {
  enQuery.pathwayId = null;
  enQuery.patientName = '';
  enQuery.status = null;
  enQuery.enrollDate = '';
  enQuery.pageNum = 1;
  loadEnroll();
};
// ---- 入径区 ----
const candidates = ref([]);
const candLoading = ref(false);
const candKeyword = ref('');
const enrollForm = reactive({admissionId: null, pathwayId: null, enrollDate: today()});
const enrolling = ref(false);
const loadCandidates = async () => {
  candLoading.value = true;
  try {
    const res = await listAdmissionsForEnroll({keyword: candKeyword.value.trim() || undefined, limit: 50});
    if (res.code === 200)
      candidates.value = res.data || [];
    else
      ElMessage.error(res.message || '查询在院患者失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询在院患者失败');
  } finally {
    candLoading.value = false;
  }
};
const doEnroll = async () => {
  if (!enrollForm.admissionId) {
    ElMessage.warning('请先选择入径患者');
    return;
  }
  if (!enrollForm.pathwayId) {
    ElMessage.warning('请选择路径模板');
    return;
  }
  const cand = candidates.value.find((c) => String(c.admissionId) === String(enrollForm.admissionId));
  try {
    await ElMessageBox.confirm(`确认为「${cand?.patientName || ''}（${cand?.admissionNo || ''}）」入径？入径日期 ${enrollForm.enrollDate}，一次住院同时仅一条在径。`, '入径登记', {
      confirmButtonText: '确认入径',
      cancelButtonText: '取消'
    });
    enrolling.value = true;
    const res = await enrollUpsert({
      admissionId: enrollForm.admissionId,
      pathwayId: enrollForm.pathwayId,
      enrollDate: enrollForm.enrollDate,
    });
    if (res.code === 200) {
      ElMessage.success(`已入径（${res.data?.enrollNo}）`);
      enrollForm.admissionId = null;
      enrollForm.pathwayId = null;
      await Promise.all([loadCandidates(), loadEnroll()]);
      openEnrollDetail(res.data?.id);
    } else {
      ElMessage.error(res.message || '入径失败');
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      console.error(e);
      ElMessage.error('入径失败');
    }
  } finally {
    enrolling.value = false;
  }
};
// ---- 入径详情（已拆到 ClinicalPathEnrollDetail：组件按 id 自取，完成/退径/登记变异都在组件里） ----
const detailVisible = ref(false);
const detailId = ref('');
const openEnrollDetail = (id) => {
  if (!id)
    return;
  detailId.value = id;
  detailVisible.value = true;
};
// ---------------- 页签三：变异分析 ----------------
const anaLoading = ref(false);
const ana = ref(null);
const anaPathwayId = ref(null);
const allPathways = ref([]);
const loadAllPathways = async () => {
  try {
    const res = await listPathwayPage({pageNum: 1, pageSize: 100});
    if (res.code === 200)
      allPathways.value = res.data?.records || [];
  } catch (e) {
    console.error('加载模板列表失败', e);
  }
};
const loadAnalysis = async () => {
  anaLoading.value = true;
  try {
    const res = await getPathwayAnalysis({pathwayId: anaPathwayId.value ?? undefined});
    if (res.code === 200)
      ana.value = res.data;
    else
      ElMessage.error(res.message || '查询变异分析失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询变异分析失败');
  } finally {
    anaLoading.value = false;
  }
};
const anaCards = computed(() => [
  {label: '入径人次', value: ana.value?.enrollCount ?? 0, tone: 'text-[#1269B5]'},
  {label: '完成', value: ana.value?.finishCount ?? 0, tone: 'text-emerald-700'},
  {label: '退径', value: ana.value?.abortCount ?? 0, tone: 'text-slate-500'},
  {label: '变异条数', value: ana.value?.varianceCount ?? 0, tone: 'text-amber-600'},
  {label: '完成率(%)', value: ana.value?.finishRate ?? '—', tone: 'text-emerald-700'},
]);
const onTabChange = (name) => {
  if (name === 'enroll') {
    loadActivePathways();
    loadCandidates();
    loadEnroll();
  } else if (name === 'analysis') {
    loadAllPathways();
    loadAnalysis();
  }
};
onMounted(async () => {
  await loadDicts();
  await loadTpl();
});
</script>
