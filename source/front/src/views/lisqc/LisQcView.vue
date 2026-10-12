<template>
  <div class="p-5 space-y-4">
    <!-- sql/191：410「室内质控」与 416「室间质评」合并为同一个「检验质控」入口（真实 LIS 把 IQC/EQA 当一个质控子系统），416 置 is_visible=0 退出侧栏但保留 medtech:lisEqa:list 权限码。 -->
    <el-tabs v-model="mergedTab" class="merged-tabs">
      <el-tab-pane label="室内质控" name="iqc">
        <!-- 统计 -->
        <div class="grid grid-cols-6 gap-4">
          <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
            <div class="text-sm text-gray-500">启用计划</div>
            <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.planCount }}</div>
          </div>
          <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
            <div class="text-sm text-gray-500">今日质控点</div>
            <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.todayCount }}</div>
          </div>
          <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
            <div class="text-sm text-gray-500">今日在控率</div>
            <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.inControlRate }}%</div>
          </div>
          <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
            <div class="text-sm text-gray-500">今日警告</div>
            <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.warning }}</div>
          </div>
          <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
            <div class="text-sm text-gray-500">今日失控</div>
            <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.outOfControl }}</div>
          </div>
          <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
            <div class="text-sm text-gray-500">待处理失控</div>
            <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.pendingHandle }}</div>
          </div>
        </div>

        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <el-tabs v-model="activeTab">
            <!-- 质控计划 -->
            <el-tab-pane label="质控计划" name="plan">
              <div class="flex flex-wrap items-center gap-2 mb-3">
                <el-input v-model="planQuery.itemName" clearable placeholder="检验项目名称" style="width: 200px"
                          @keyup.enter="planQuery.pageNum = 1; loadPlans()"/>
                <el-button :icon="Search" type="primary" @click="planQuery.pageNum = 1; loadPlans()">查询</el-button>
                <div class="flex-1"/>
                <el-button v-perm="'medtech:lisQc:add'" type="primary" @click="openPlan()">新建计划</el-button>
              </div>
              <el-table v-loading="planLoading" :data="planRows" data-testid="qc-plan-table" size="small">
                <el-table-column label="计划编号" prop="planNo" width="150"/>
                <el-table-column label="检验项目" min-width="130" prop="itemName" show-overflow-tooltip/>
                <el-table-column label="仪器" min-width="110" prop="instrumentName" show-overflow-tooltip/>
                <el-table-column label="水平" width="80">
                  <template #default="{ row }">{{ levelText(row.qcLevel) }}</template>
                </el-table-column>
                <el-table-column label="质控品" min-width="110" prop="controlName" show-overflow-tooltip/>
                <el-table-column label="批号" prop="controlLotNo" width="100"/>
                <el-table-column align="right" label="靶值" prop="meanValue" width="90"/>
                <el-table-column align="right" label="SD" prop="sdValue" width="90"/>
                <el-table-column align="right" label="实际 CV" width="90">
                  <template #default="{ row }">{{ row.cvActual != null ? row.cvActual + '%' : '-' }}</template>
                </el-table-column>
                <el-table-column label="效期" prop="expireDate" width="110"/>
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">{{
                        row.statusText
                      }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column fixed="right" label="操作" width="190">
                  <template #default="{ row }">
                    <el-button v-perm="'medtech:lisQc:add'" link size="small" type="primary" @click="openInput(row)">
                      录结果
                    </el-button>
                    <el-button v-perm="'medtech:lisQc:edit'" link size="small" type="primary" @click="openPlan(row)">
                      编辑
                    </el-button>
                    <el-button v-perm="'medtech:lisQc:edit'" :type="Number(row.status) === 1 ? 'danger' : 'success'"
                               link size="small"
                               @click="togglePlan(row)">
                      {{ Number(row.status) === 1 ? '停用' : '启用' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="mt-3 flex justify-end">
                <el-pagination v-model:current-page="planQuery.pageNum" v-model:page-size="planQuery.pageSize" :total="planTotal"
                               background layout="total, prev, pager, next"
                               @current-change="loadPlans"/>
              </div>
            </el-tab-pane>

            <!-- 质控记录 -->
            <el-tab-pane label="质控记录" name="record">
              <div class="flex flex-wrap items-center gap-2 mb-3">
                <el-input v-model="recQuery.itemName" clearable placeholder="检验项目名称" style="width: 180px"
                          @keyup.enter="recQuery.pageNum = 1; loadRecords()"/>
                <el-select v-model="recQuery.status" :fit-input-width="false" clearable placeholder="判定"
                           style="width: 110px">
                  <el-option v-for="d in qcStatusDict" :key="d.dictValue" :label="d.dictLabel"
                             :value="Number(d.dictValue)"/>
                  <el-option :value="0" label="未判定"/>
                </el-select>
                <el-select v-model="recQuery.handleStatus" :fit-input-width="false" clearable placeholder="处理状态"
                           style="width: 120px">
                  <el-option v-for="d in handleDict" :key="d.dictValue" :label="d.dictLabel"
                             :value="Number(d.dictValue)"/>
                </el-select>
                <el-date-picker v-model="recQuery.startDate" placeholder="开始日期" style="width: 140px"
                                type="date" value-format="YYYY-MM-DD"/>
                <el-date-picker v-model="recQuery.endDate" placeholder="结束日期" style="width: 140px" type="date"
                                value-format="YYYY-MM-DD"/>
                <el-button :icon="Search" type="primary" @click="recQuery.pageNum = 1; loadRecords()">查询</el-button>
                <el-button :icon="Refresh" @click="resetRec">重置</el-button>
              </div>
              <el-table v-loading="recLoading" :data="recRows" data-testid="qc-record-table" size="small">
                <el-table-column label="检验项目" min-width="120" prop="itemName" show-overflow-tooltip/>
                <el-table-column label="仪器" min-width="100" prop="instrumentName" show-overflow-tooltip/>
                <el-table-column label="水平" width="70">
                  <template #default="{ row }">{{ levelText(row.qcLevel) }}</template>
                </el-table-column>
                <el-table-column label="质控时间" width="140">
                  <template #default="{ row }">{{ fmtTime(row.qcTime) }}</template>
                </el-table-column>
                <el-table-column align="right" label="测定值" prop="resultValue" width="90"/>
                <el-table-column align="right" label="Z 值" prop="zScore" width="90"/>
                <el-table-column label="判定" width="90">
                  <template #default="{ row }">
                    <el-tag :type="qcStatusTag(row.status)" size="small">{{ row.statusText }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="命中规则" prop="violatedRules" width="110"/>
                <el-table-column label="处理" width="90">
                  <template #default="{ row }">{{ handleText(row.handleStatus) }}</template>
                </el-table-column>
                <el-table-column fixed="right" label="操作" width="130">
                  <template #default="{ row }">
                    <el-button v-if="Number(row.status) === 3 && Number(row.handleStatus) === 1"
                               v-perm="'medtech:lisQc:edit'" link size="small" type="warning" @click="openHandle(row)">
                      处理
                    </el-button>
                    <el-button v-if="Number(row.status) === 3 && Number(row.handleStatus) === 2 && !row.reviewBy"
                               v-perm="'medtech:lisQc:edit'" link size="small" type="success" @click="doReview(row)">复核
                    </el-button>
                    <span v-if="row.reviewBy" class="text-xs text-gray-400">{{ row.reviewBy }} 已复核</span>
                  </template>
                </el-table-column>
              </el-table>
              <div class="mt-3 flex justify-end">
                <el-pagination v-model:current-page="recQuery.pageNum" v-model:page-size="recQuery.pageSize" :total="recTotal"
                               background layout="total, prev, pager, next"
                               @current-change="loadRecords"/>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 计划弹窗 -->
        <LisQcPlanEdit v-model="planVisible" :level-dict="levelDict" :row="planEditRow" @saved="onPlanSaved"/>

        <!-- 录入结果弹窗 -->
        <LisQcResultAdd v-model="inputVisible" :level-dict="levelDict" :row="inputRow" @saved="onInputSaved"/>

        <!-- 失控处理弹窗 -->
        <LisQcHandleEdit v-model="handleVisible" :row="handleRow" @saved="onHandleSaved"/>
      </el-tab-pane>
      <el-tab-pane label="室间质评" lazy name="eqa">
        <LisEqaView/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
/**
 * LIS 室内质控（G17，菜单 410）
 *
 * 三个 tab：质控计划（靶值/SD 维护）、质控记录（录入 + Westgard 判定结果展示）、
 * 失控处理（处理 → 复核闭环，复核人 ≠ 处理人，服务端硬校验）。
 * ⚠ 判定只在服务端：页面只展示 zScore / status / violatedRules，不自己算在控失控。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import LisEqaView from './LisEqaView.vue';
import {
  getQcPlanListPage,
  getQcRecordListPage,
  getQcStats,
  qcPlanToggle,
  qcReview,
} from '@/api/medicaltech';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import LisQcPlanEdit from './LisQcPlanEdit.vue';
import LisQcResultAdd from './LisQcResultAdd.vue';
import LisQcHandleEdit from './LisQcHandleEdit.vue';

const activeTab = ref('plan');
// ---------------- 字典 ----------------
const levelDict = ref([]);
const qcStatusDict = ref([]);
const handleDict = ref([]);
const levelText = (v) => dictLabelText(levelDict.value, v);
const qcStatusText = (v) => dictLabelText(qcStatusDict.value, v);
const handleText = (v) => dictLabelText(handleDict.value, v);
const qcStatusTag = (v) => {
  const n = Number(v);
  if (n === 1)
    return 'success';
  if (n === 2)
    return 'warning';
  if (n === 3)
    return 'danger';
  return 'info'; // 0 未判定
};
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.LIS_QC_LEVEL},${DICT_TYPE.LIS_QC_STATUS},${DICT_TYPE.LIS_QC_HANDLE_STATUS}`);
    levelDict.value = res?.data?.[DICT_TYPE.LIS_QC_LEVEL] || [];
    qcStatusDict.value = res?.data?.[DICT_TYPE.LIS_QC_STATUS] || [];
    handleDict.value = res?.data?.[DICT_TYPE.LIS_QC_HANDLE_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 统计 ----------------
const stats = reactive({
  planCount: 0,
  todayCount: 0,
  inControl: 0,
  warning: 0,
  outOfControl: 0,
  pendingHandle: 0,
  inControlRate: 0
});
const loadStats = async () => {
  try {
    const res = await getQcStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
// ---------------- 计划 ----------------
const planLoading = ref(false);
const planRows = ref([]);
const planTotal = ref(0);
const planQuery = reactive({itemName: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadPlans = async () => {
  planLoading.value = true;
  try {
    const res = await getQcPlanListPage({
      itemName: planQuery.itemName.trim() || undefined,
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
const planVisible = ref(false);
const planEditRow = ref(null);
const openPlan = (row) => {
  planEditRow.value = row || null;
  planVisible.value = true;
};
const onPlanSaved = () => {
  loadPlans();
  loadStats();
};
const togglePlan = (row) => {
  const to = Number(row.status) === 1 ? 0 : 1;
  qcPlanToggle(row.id, to).then((res) => {
    if (res.code === 200) {
      ElMessage.success(res.message || '已变更');
      loadPlans();
      loadStats();
    } else
      ElMessage.error(res.message || '操作失败');
  }).catch((e) => {
    console.error(e);
    ElMessage.error('操作失败');
  });
};
// 录入质控结果
const inputVisible = ref(false);
const inputRow = ref(null);
const openInput = (row) => {
  inputRow.value = row;
  inputVisible.value = true;
};
const onInputSaved = () => {
  activeTab.value = 'record';
  loadRecords();
  loadStats();
};
// ---------------- 记录 ----------------
const recLoading = ref(false);
const recRows = ref([]);
const recTotal = ref(0);
const recQuery = reactive({
  itemName: '', status: null, handleStatus: null,
  startDate: '', endDate: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadRecords = async () => {
  recLoading.value = true;
  try {
    const res = await getQcRecordListPage({
      itemName: recQuery.itemName.trim() || undefined,
      status: recQuery.status ?? undefined,
      handleStatus: recQuery.handleStatus ?? undefined,
      startDate: recQuery.startDate || undefined,
      endDate: recQuery.endDate || undefined,
      pageNum: recQuery.pageNum, pageSize: recQuery.pageSize,
    });
    if (res.code === 200) {
      recRows.value = res.data?.records || [];
      recTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    recLoading.value = false;
  }
};
const resetRec = () => {
  Object.assign(recQuery, {itemName: '', status: null, handleStatus: null, startDate: '', endDate: '', pageNum: 1});
  loadRecords();
};
// 失控处理
const handleVisible = ref(false);
const handleRow = ref(null);
const openHandle = (row) => {
  handleRow.value = row;
  handleVisible.value = true;
};
const onHandleSaved = () => {
  loadRecords();
  loadStats();
};
const doReview = (row) => {
  qcReview(row.id).then((res) => {
    if (res.code === 200) {
      ElMessage.success('复核通过，失控闭环完成');
      loadRecords();
      loadStats();
    } else
      ElMessage.error(res.message || '复核失败');
  }).catch((e) => {
    console.error(e);
    ElMessage.error('复核失败');
  });
};
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-');
// 合并页签的当前页。被并页面自带 onMounted 请求，故其页签用 lazy —— 进页不预拉两套数据。
const mergedTab = ref('iqc');
onMounted(() => {
  loadDicts();
  loadStats();
  loadPlans();
  loadRecords();
});
</script>

<style scoped>
/* 被并页面自带页级留白，嵌进页签后统一由宿主提供，避免双层 padding */
.merged-tabs :deep(.el-tab-pane > .p-5) {
  padding: 0;
}
</style>
