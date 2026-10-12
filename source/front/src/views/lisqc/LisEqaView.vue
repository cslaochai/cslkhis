<template>
  <div class="p-5 space-y-4">
    <!-- 概览 -->
    <div class="grid grid-cols-6 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">在评批次</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.activePlanCount }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待成绩回报</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.pendingReturnCount }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">{{ stats.thisYear }} 年 PT 均分</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">
          {{ stats.yearAvgScore != null ? stats.yearAvgScore + '%' : '-' }}
          <span class="text-xs text-gray-400 font-normal">/{{ stats.yearScoredCount }} 批</span>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待整改不合格</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.pendingRectifyCount }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">7 日内截止上报</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.dueSoonCount }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">{{ stats.thisYear }} 年不合格项</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.yearFailCount }}</div>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <el-tabs v-model="activeTab">
        <!-- 批次台账 -->
        <el-tab-pane label="质评批次" name="plan">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="planQuery.orgName" clearable data-testid="eqa-plan-org-search" placeholder="组织方"
                      style="width: 200px"
                      @keyup.enter="planQuery.pageNum = 1; loadPlans()"/>
            <el-select v-model="planQuery.status" :fit-input-width="false" clearable placeholder="批次状态"
                       style="width: 140px">
              <el-option v-for="d in planStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="planQuery.pageNum = 1; loadPlans()">查询</el-button>
            <div class="flex-1"/>
            <el-button v-perm="'medtech:lisEqa:add'" type="primary" @click="openPlan()">新建批次</el-button>
          </div>
          <el-table v-loading="planLoading" :data="planRows" data-testid="eqa-plan-table" size="small">
            <el-table-column label="批次号" prop="planNo" width="140"/>
            <el-table-column label="年度/批次" width="90">
              <template #default="{ row }">{{ row.planYear }} 年第 {{ row.batchNo }} 批</template>
            </el-table-column>
            <el-table-column label="组织方" min-width="150" prop="orgName" show-overflow-tooltip/>
            <el-table-column label="计划名称" min-width="150" prop="planName" show-overflow-tooltip/>
            <el-table-column label="项目/样品" width="100">
              <template #default="{ row }">{{ row.itemCount || 0 }} / {{ row.sampleCount || 0 }}</template>
            </el-table-column>
            <el-table-column label="上报截止" prop="reportDeadline" width="110"/>
            <el-table-column label="成绩回报日" prop="returnDate" width="110"/>
            <el-table-column align="right" label="PT 得分" width="100">
              <template #default="{ row }">
                <span :class="Number(row.passFlag) === 0 ? 'text-[#B91C1C] font-semibold' : ''">
                  {{ row.ptScore != null ? row.ptScore + '%' : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="结论" width="90">
              <template #default="{ row }">
                <el-tag :type="Number(row.passFlag) === 1 ? 'success' : (Number(row.passFlag) === 0 ? 'danger' : 'info')"
                        size="small">
                  {{ row.passFlagText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="statusText" width="90"/>
            <el-table-column fixed="right" label="操作" width="200">
              <template #default="{ row }">
                <el-button v-perm="'medtech:lisEqa:add'" link size="small" type="primary" @click="openPlan(row)">编辑
                </el-button>
                <el-button link size="small" type="primary" @click="gotoSample(row.id)">盲样</el-button>
                <el-button v-if="Number(row.status) === 4" v-perm="'medtech:lisEqa:edit'" link size="small"
                           type="success" @click="doArchive(row)">归档
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="planQuery.pageNum" v-model:page-size="planQuery.pageSize" :page-sizes="PAGE_SIZES"
                           :total="planTotal"
                           background layout="total, sizes, prev, pager, next"
                           @current-change="loadPlans" @size-change="loadPlans"/>
          </div>
        </el-tab-pane>

        <!-- 盲样台账 -->
        <el-tab-pane label="盲样台账" name="sample">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="curPlanId" :fit-input-width="false" clearable data-testid="eqa-sample-plan-select"
                       placeholder="选择质评批次"
                       style="width: 240px" @change="sampleQuery.pageNum = 1; loadSamples()">
              <el-option v-for="p in planOptions" :key="p.id" :label="p.planNo + ' · ' + p.orgName"
                         :value="String(p.id)"/>
            </el-select>
            <el-input v-model="sampleQuery.itemName" clearable placeholder="检验项目" style="width: 160px"
                      @keyup.enter="sampleQuery.pageNum = 1; loadSamples()"/>
            <el-select v-model="sampleQuery.status" :fit-input-width="false" clearable placeholder="流转状态"
                       style="width: 130px">
              <el-option v-for="d in sampleStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="sampleQuery.pageNum = 1; loadSamples()">查询</el-button>
            <el-button :icon="Refresh" @click="resetSamples">重置</el-button>
            <div class="flex-1"/>
            <el-button v-perm="'medtech:lisEqa:add'" type="primary" @click="openGenerate">批量生成盲样</el-button>
            <el-button v-perm="'medtech:lisEqa:add'" @click="openOne()">单条登记</el-button>
            <el-button v-perm="'medtech:lisEqa:edit'" type="warning" @click="doReport">批量上报</el-button>
          </div>
          <el-table v-loading="smpLoading" :data="smpRows" data-testid="eqa-sample-table" row-key="id"
                    size="small" @selection-change="onSmpSelection">
            <el-table-column :selectable="(r: any) => Number(r.status) < 2" type="selection" width="45"/>
            <el-table-column label="序号" prop="sampleSeq" width="60"/>
            <el-table-column label="盲样编号" prop="sampleNo" width="120"/>
            <el-table-column label="检验项目" min-width="130" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="仪器" min-width="110" show-overflow-tooltip>
              <template #default="{ row }">{{ row.instrumentName || '未指定' }}</template>
            </el-table-column>
            <el-table-column label="方法学" prop="methodName" show-overflow-tooltip width="110"/>
            <el-table-column align="right" label="本室测定值" width="110">
              <template #default="{ row }">{{ row.testValue != null ? row.testValue : '-' }}</template>
            </el-table-column>
            <el-table-column label="流转状态" prop="statusText" width="90"/>
            <el-table-column fixed="right" label="操作" width="150">
              <template #default="{ row }">
                <el-button v-perm="'medtech:lisEqa:edit'" link size="small" type="primary" @click="openTest(row)">
                  录结果
                </el-button>
                <el-button v-perm="'medtech:lisEqa:add'" link size="small" type="primary" @click="openOne(row)">编辑
                </el-button>
                <el-button v-if="Number(row.status) < 2" v-perm="'medtech:lisEqa:delete'" link size="small"
                           type="danger" @click="doDeleteSample(row)">删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="sampleQuery.pageNum" v-model:page-size="sampleQuery.pageSize" :page-sizes="PAGE_SIZES"
                           :total="smpTotal"
                           background layout="total, sizes, prev, pager, next"
                           @current-change="loadSamples" @size-change="loadSamples"/>
          </div>
        </el-tab-pane>

        <!-- 成绩回报 -->
        <el-tab-pane label="成绩回报与判定" name="return">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="curPlanId" :fit-input-width="false" clearable placeholder="选择质评批次"
                       style="width: 240px"
                       @change="retQuery.pageNum = 1; loadReturns()">
              <el-option v-for="p in planOptions" :key="p.id" :label="p.planNo + ' · ' + p.orgName"
                         :value="String(p.id)"/>
            </el-select>
            <el-select v-model="retMode" :fit-input-width="false" style="width: 150px"
                       @change="retQuery.pageNum = 1; loadReturns()">
              <el-option :value="2" label="已上报（待回报）"/>
              <el-option :value="3" label="已回报（看成绩）"/>
            </el-select>
            <el-input v-model="retQuery.itemName" clearable placeholder="检验项目" style="width: 150px"
                      @keyup.enter="retQuery.pageNum = 1; loadReturns()"/>
            <el-button :icon="Search" type="primary" @click="retQuery.pageNum = 1; loadReturns()">查询</el-button>
            <el-date-picker v-model="returnDate" placeholder="成绩回报日" style="width: 150px" type="date"
                            value-format="YYYY-MM-DD"/>
            <div class="flex-1"/>
            <el-button v-if="retMode === 2" v-perm="'medtech:lisEqa:edit'" :loading="scoreSaving" type="primary"
                       @click="submitScore">
              提交回报并判定
            </el-button>
          </div>
          <div class="text-xs text-gray-400 mb-2">
            判定口径由服务端按「有组SD用 SDI → 有 TEa 用允许总误差 → 只有上下限用可接受范围」择优执行，
            缺依据时落「未判定」，不会替你把不确定判成合格。
          </div>
          <el-table v-loading="retLoading" :data="retRows" data-testid="eqa-return-table" size="small">
            <el-table-column label="盲样" prop="sampleNo" width="120"/>
            <el-table-column label="项目" min-width="110" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="仪器" min-width="100" show-overflow-tooltip>
              <template #default="{ row }">{{ row.instrumentName || '未指定' }}</template>
            </el-table-column>
            <el-table-column align="right" label="本室值" width="90">
              <template #default="{ row }">{{ row.testValue != null ? row.testValue : '-' }}</template>
            </el-table-column>
            <el-table-column label="靶值" width="110">
              <template #default="{ row }">
                <span v-if="Number(row.status) === 3">{{ row.targetValue ?? '-' }}</span>
                <el-input-number v-else v-model="row.targetValue" :controls="false" :precision="4" size="small"
                                 style="width: 100px"/>
              </template>
            </el-table-column>
            <el-table-column label="组SD" width="110">
              <template #default="{ row }">
                <span v-if="Number(row.status) === 3">{{ row.groupSd ?? '-' }}</span>
                <el-input-number v-else v-model="row.groupSd" :controls="false" :precision="4" size="small"
                                 style="width: 100px"/>
              </template>
            </el-table-column>
            <el-table-column label="TEa%" width="100">
              <template #default="{ row }">
                <span v-if="Number(row.status) === 3">{{ row.tea ?? '-' }}</span>
                <el-input-number v-else v-model="row.tea" :controls="false" :precision="2" size="small"
                                 style="width: 90px"/>
              </template>
            </el-table-column>
            <el-table-column label="可接受范围" width="160">
              <template #default="{ row }">
                <span v-if="Number(row.status) === 3">{{ row.targetMin ?? '-' }} ~ {{ row.targetMax ?? '-' }}</span>
                <div v-else class="flex items-center gap-1">
                  <el-input-number v-model="row.targetMin" :controls="false" :precision="4" size="small"
                                   style="width: 74px"/>
                  <span class="text-gray-400">~</span>
                  <el-input-number v-model="row.targetMax" :controls="false" :precision="4" size="small"
                                   style="width: 74px"/>
                </div>
              </template>
            </el-table-column>
            <el-table-column align="right" label="SDI" width="80">
              <template #default="{ row }">{{ row.sdi != null ? row.sdi : '-' }}</template>
            </el-table-column>
            <el-table-column align="right" label="偏倚%" width="80">
              <template #default="{ row }">{{ row.biasRate != null ? row.biasRate : '-' }}</template>
            </el-table-column>
            <el-table-column label="判定" width="90">
              <template #default="{ row }">
                <el-tag :type="resultTag(row.resultStatus)" size="small">{{ row.resultStatusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="口径" prop="judgeModeText" show-overflow-tooltip width="120"/>
            <el-table-column label="整改" width="110">
              <template #default="{ row }">{{ row.handleStatusText }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="130">
              <template #default="{ row }">
                <el-button v-if="Number(row.resultStatus) === 3 && Number(row.handleStatus) === 1"
                           v-perm="'medtech:lisEqa:edit'" link size="small" type="warning" @click="openFix(row)">整改
                </el-button>
                <el-button v-if="Number(row.resultStatus) === 3 && Number(row.handleStatus) === 2 && !row.reviewBy"
                           v-perm="'medtech:lisEqa:edit'" link size="small" type="success" @click="doFixReview(row)">复核
                </el-button>
                <span v-if="row.reviewBy" class="text-xs text-gray-400">{{ row.reviewBy }} 已复核</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="retQuery.pageNum" v-model:page-size="retQuery.pageSize" :page-sizes="PAGE_SIZES"
                           :total="retTotal"
                           background layout="total, sizes, prev, pager, next"
                           @current-change="loadReturns" @size-change="loadReturns"/>
          </div>
        </el-tab-pane>

        <!-- 室间差 -->
        <el-tab-pane label="室间差比对" name="compare">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="curPlanId" :fit-input-width="false" clearable placeholder="选择质评批次"
                       style="width: 240px"
                       @change="cmpQuery.pageNum = 1; loadCompares()">
              <el-option v-for="p in planOptions" :key="p.id" :label="p.planNo + ' · ' + p.orgName"
                         :value="String(p.id)"/>
            </el-select>
            <el-select v-model="cmpQuery.status" :fit-input-width="false" clearable placeholder="比对结论"
                       style="width: 130px">
              <el-option v-for="d in compareStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="cmpQuery.pageNum = 1; loadCompares()">查询</el-button>
            <div class="flex-1"/>
            <span class="text-xs text-gray-400">当前页超差 {{ cmpFailedCount }} 条</span>
          </div>
          <el-table v-loading="cmpLoading" :data="cmpRows" data-testid="eqa-compare-table" size="small">
            <el-table-column label="盲样" prop="sampleNo" width="120"/>
            <el-table-column label="项目" min-width="110" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="A 仪器/方法" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.instrumentA || '未指定' }}<span v-if="row.methodA"
                                                                                  class="text-gray-400"> · {{
                  row.methodA
                }}</span></template>
            </el-table-column>
            <el-table-column align="right" label="A 值" prop="valueA" width="90"/>
            <el-table-column label="B 仪器/方法" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.instrumentB || '未指定' }}<span v-if="row.methodB"
                                                                                  class="text-gray-400"> · {{
                  row.methodB
                }}</span></template>
            </el-table-column>
            <el-table-column align="right" label="B 值" prop="valueB" width="90"/>
            <el-table-column align="right" label="互差" prop="diffValue" width="90"/>
            <el-table-column align="right" label="相对互差" width="100">
              <template #default="{ row }">{{ row.diffRate != null ? row.diffRate + '%' : '-' }}</template>
            </el-table-column>
            <el-table-column label="允许限" width="130">
              <template #default="{ row }">
                {{ row.allowRate }}%
                <span class="text-xs text-gray-400">（{{ row.allowSourceText }}）</span>
              </template>
            </el-table-column>
            <el-table-column label="结论" width="90">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 1 ? 'success' : 'danger'" size="small">{{
                    row.statusText
                  }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="cmpQuery.pageNum" v-model:page-size="cmpQuery.pageSize" :page-sizes="PAGE_SIZES"
                           :total="cmpTotal"
                           background layout="total, sizes, prev, pager, next"
                           @current-change="loadCompares" @size-change="loadCompares"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 批次弹窗 -->
    <LisEqaPlanEdit v-model="planVisible" :row="planEditRow" @saved="onPlanSaved"/>

    <!-- 批量生成盲样 -->
    <LisEqaSampleGenerate v-model="genVisible" :plan-id="curPlanId" @saved="onGenerateSaved"/>

    <!-- 单条登记 -->
    <LisEqaSampleEdit v-model="oneVisible" :row="oneEditRow" :plan-id="curPlanId" @saved="onOneSaved"/>

    <!-- 录检测结果 -->
    <LisEqaTestEdit v-model="testVisible" :row="testEditRow" @saved="onTestSaved"/>

    <!-- 不合格整改 -->
    <LisEqaRectifyEdit v-model="fixVisible" :row="fixEditRow" @saved="onFixSaved"/>
  </div>
</template>

<script setup>
/**
 * LIS 室间质评 EQA（菜单 416，菜单口径与 sql/172 一致）
 *
 * 四个 tab：批次台账 → 盲样台账（含批量生成与批量上报）→ 成绩回报与判定 → 室间差比对。
 * ⚠ 铁律：SDI / 偏倚 / PT 得分 / 仪器间互差全部服务端算，页面只展示 ——
 *   前端自己判"看着差不多就合格"，等于效果评议证据可以凭空制造。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  eqaPlanArchive,
  eqaRectifyReview,
  eqaReturnScore,
  eqaSampleDeleteById,
  eqaSampleReport,
  getEqaCompareListPage,
  getEqaPlanListPage,
  getEqaSampleListPage,
  getEqaStats,
} from '@/api/medicaltech';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import LisEqaPlanEdit from './LisEqaPlanEdit.vue';
import LisEqaSampleGenerate from './LisEqaSampleGenerate.vue';
import LisEqaSampleEdit from './LisEqaSampleEdit.vue';
import LisEqaTestEdit from './LisEqaTestEdit.vue';
import LisEqaRectifyEdit from './LisEqaRectifyEdit.vue';

const activeTab = ref('plan');
// ---------------- 字典 ----------------
const planStatusDict = ref([]);
const sampleStatusDict = ref([]);
const resultStatusDict = ref([]);
const judgeModeDict = ref([]);
const compareStatusDict = ref([]);
const planStatusText = (v) => dictLabelText(planStatusDict.value, v);
const sampleStatusText = (v) => dictLabelText(sampleStatusDict.value, v);
const resultStatusText = (v) => (Number(v) === 0 ? '未判定' : dictLabelText(resultStatusDict.value, v));
const compareStatusText = (v) => dictLabelText(compareStatusDict.value, v);
const resultTag = (v) => {
  const n = Number(v);
  if (n === 1)
    return 'success';
  if (n === 2)
    return 'warning';
  if (n === 3)
    return 'danger';
  return 'info';
};
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.LIS_EQA_PLAN_STATUS},${DICT_TYPE.LIS_EQA_SAMPLE_STATUS},${DICT_TYPE.LIS_EQA_RESULT_STATUS},${DICT_TYPE.LIS_EQA_JUDGE_MODE},${DICT_TYPE.LIS_EQA_COMPARE_STATUS}`);
    const d = res?.data || {};
    planStatusDict.value = d[DICT_TYPE.LIS_EQA_PLAN_STATUS] || [];
    sampleStatusDict.value = d[DICT_TYPE.LIS_EQA_SAMPLE_STATUS] || [];
    resultStatusDict.value = d[DICT_TYPE.LIS_EQA_RESULT_STATUS] || [];
    judgeModeDict.value = d[DICT_TYPE.LIS_EQA_JUDGE_MODE] || [];
    compareStatusDict.value = d[DICT_TYPE.LIS_EQA_COMPARE_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 统计 ----------------
const stats = reactive({
  activePlanCount: 0, pendingReturnCount: 0, pendingRectifyCount: 0,
  dueSoonCount: 0, yearFailCount: 0, yearAvgScore: null, yearScoredCount: 0, thisYear: 0,
});
const loadStats = async () => {
  try {
    const res = await getEqaStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
// ---------------- 批次下拉 / 台账 ----------------
const planOptions = ref([]);
const curPlanId = ref(null);
const loadPlanOptions = async () => {
  try {
    const res = await getEqaPlanListPage({pageNum: 1, pageSize: 200});
    if (res.code === 200)
      planOptions.value = res.data?.records || [];
  } catch (e) {
    console.error(e);
  }
};
const planLoading = ref(false);
const planRows = ref([]);
const planTotal = ref(0);
const planQuery = reactive({orgName: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadPlans = async () => {
  planLoading.value = true;
  try {
    const res = await getEqaPlanListPage({
      orgName: planQuery.orgName.trim() || undefined,
      status: planQuery.status ?? undefined,
      pageNum: planQuery.pageNum, pageSize: planQuery.pageSize,
    });
    if (res.code === 200) {
      planRows.value = res.data?.records || [];
      planTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    planLoading.value = false;
  }
};
// 批次新增 / 编辑
const planVisible = ref(false);
const planEditRow = ref(null);
const openPlan = (row) => {
  planEditRow.value = row || null;
  planVisible.value = true;
};
const onPlanSaved = () => {
  loadPlans();
  loadPlanOptions();
  loadStats();
};
const doArchive = (row) => {
  ElMessageBox.confirm(`归档后该批次的台账不再允许变更。确认归档批次 ${row.planNo}？`, '归档确认', {type: 'warning'})
      .then(async () => {
        try {
          const res = await eqaPlanArchive(row.id);
          if (res.code === 200) {
            ElMessage.success(res.message || '已归档');
            loadPlans();
            loadPlanOptions();
            loadStats();
          } else
            ElMessage.error(res.message || '归档失败');
        } catch (e) {
          console.error(e);
          ElMessage.error('归档失败');
        }
      })
      .catch(() => {
      });
};
// 切到盲样 tab 并锁定批次
const gotoSample = (planId) => {
  curPlanId.value = String(planId);
  activeTab.value = 'sample';
  sampleQuery.pageNum = 1;
  loadSamples();
};
// ---------------- 盲样台账 ----------------
const smpLoading = ref(false);
const smpRows = ref([]);
const smpTotal = ref(0);
const smpSelection = ref([]);
const sampleQuery = reactive({
  itemName: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadSamples = async () => {
  smpLoading.value = true;
  try {
    const res = await getEqaSampleListPage({
      planId: curPlanId.value || undefined,
      itemName: sampleQuery.itemName.trim() || undefined,
      status: sampleQuery.status ?? undefined,
      pageNum: sampleQuery.pageNum, pageSize: sampleQuery.pageSize,
    });
    if (res.code === 200) {
      smpRows.value = res.data?.records || [];
      smpTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    smpLoading.value = false;
  }
};
const resetSamples = () => {
  Object.assign(sampleQuery, {itemName: '', status: null, pageNum: 1});
  loadSamples();
};
const onSmpSelection = (rows) => {
  smpSelection.value = rows;
};
// 批量生成骨架
const genVisible = ref(false);
const openGenerate = () => {
  if (!curPlanId.value) {
    ElMessage.warning('请先选择质评批次');
    return;
  }
  genVisible.value = true;
};
const onGenerateSaved = () => {
  loadSamples();
  loadPlans();
  loadStats();
};
// 单条登记
const oneVisible = ref(false);
const oneEditRow = ref(null);
const openOne = (row) => {
  oneEditRow.value = row || null;
  oneVisible.value = true;
};
const onOneSaved = () => {
  loadSamples();
  loadPlans();
  loadStats();
};
// 录检测结果
const testVisible = ref(false);
const testEditRow = ref(null);
const openTest = (row) => {
  testEditRow.value = row;
  testVisible.value = true;
};
const onTestSaved = () => {
  loadSamples();
  loadPlans();
};
// 批量上报
const doReport = async () => {
  if (!curPlanId.value) {
    ElMessage.warning('请先选择质评批次');
    return;
  }
  if (smpSelection.value.length === 0) {
    ElMessage.warning('请先勾选要上报的盲样');
    return;
  }
  try {
    const res = await eqaSampleReport({
      planId: curPlanId.value, sampleIds: smpSelection.value.map((r) => r.id),
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已上报');
      loadSamples();
      loadPlans();
      loadPlanOptions();
      loadStats();
    } else
      ElMessage.error(res.message || '上报失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('上报失败');
  }
};
const doDeleteSample = (row) => {
  ElMessageBox.confirm(`确认删除盲样 ${row.sampleNo}（${row.itemName}）？已上报的不允许删除。`, '删除确认', {type: 'warning'})
      .then(async () => {
        try {
          const res = await eqaSampleDeleteById(row.id);
          if (res.code === 200) {
            ElMessage.success(res.message || '已删除');
            loadSamples();
            loadStats();
          } else
            ElMessage.error(res.message || '删除失败');
        } catch (e) {
          console.error(e);
          ElMessage.error('删除失败');
        }
      })
      .catch(() => {
      });
};
// ---------------- 成绩回报 / 判定 / 整改 ----------------
const retLoading = ref(false);
const retRows = ref([]);
const retTotal = ref(0);
const retMode = ref(2); // 2-已上报待回报（默认） 3-已回报
const retQuery = reactive({itemName: '', resultStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadReturns = async () => {
  retLoading.value = true;
  try {
    const res = await getEqaSampleListPage({
      planId: curPlanId.value || undefined,
      status: retMode.value,
      itemName: retQuery.itemName.trim() || undefined,
      resultStatus: retMode.value === 3 ? (retQuery.resultStatus ?? undefined) : undefined,
      pageNum: retQuery.pageNum, pageSize: retQuery.pageSize,
    });
    if (res.code === 200) {
      retRows.value = (res.data?.records || []).map((r) => ({...r}));
      retTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    retLoading.value = false;
  }
};
const returnDate = ref('');
const scoreSaving = ref(false);
const submitScore = async () => {
  const rows = retRows.value.filter((r) => r.targetValue != null || r.targetMin != null || r.targetMax != null);
  if (!rows.length) {
    ElMessage.warning('请至少给一行填回报靶值或可接受范围');
    return;
  }
  scoreSaving.value = true;
  try {
    const res = await eqaReturnScore({
      planId: curPlanId.value,
      returnDate: returnDate.value || undefined,
      rows: rows.map((r) => ({
        sampleId: r.id,
        targetValue: r.targetValue ?? null,
        groupSd: r.groupSd ?? null,
        tea: r.tea ?? null,
        targetMin: r.targetMin ?? null,
        targetMax: r.targetMax ?? null,
      })),
    });
    if (res.code === 200) {
      const d = res.data;
      ElMessage.success(`PT 得分 ${d?.ptScore ?? '—'}%，本次${d?.passFlagText || ''}；生成互差 ${d?.compareCount || 0} 条（超差 ${d?.compareFailedCount || 0} 条）`);
      retMode.value = 3;
      loadReturns();
      loadPlans();
      loadPlanOptions();
      loadStats();
      loadCompares();
    } else
      ElMessage.error(res.message || '回报失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('回报失败');
  } finally {
    scoreSaving.value = false;
  }
};
// 不合格整改
const fixVisible = ref(false);
const fixEditRow = ref(null);
const openFix = (row) => {
  fixEditRow.value = row;
  fixVisible.value = true;
};
const onFixSaved = () => {
  loadReturns();
  loadStats();
};
const doFixReview = (row) => {
  eqaRectifyReview(row.id).then((res) => {
    if (res.code === 200) {
      ElMessage.success(res.message || '复核通过');
      loadReturns();
      loadPlans();
      loadStats();
    } else
      ElMessage.error(res.message || '复核失败');
  }).catch((e) => {
    console.error(e);
    ElMessage.error('复核失败');
  });
};
// ---------------- 室间差 ----------------
const cmpLoading = ref(false);
const cmpRows = ref([]);
const cmpTotal = ref(0);
const cmpQuery = reactive({status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadCompares = async () => {
  cmpLoading.value = true;
  try {
    const res = await getEqaCompareListPage({
      planId: curPlanId.value || undefined,
      status: cmpQuery.status ?? undefined,
      pageNum: cmpQuery.pageNum, pageSize: cmpQuery.pageSize,
    });
    if (res.code === 200) {
      cmpRows.value = res.data?.records || [];
      cmpTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    cmpLoading.value = false;
  }
};
const cmpFailedCount = computed(() => cmpRows.value.filter((r) => Number(r.status) === 2).length);
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-');
onMounted(() => {
  loadDicts();
  loadStats();
  loadPlanOptions();
  loadPlans();
  loadSamples();
  loadReturns();
  loadCompares();
});
</script>
