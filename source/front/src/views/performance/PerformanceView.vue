<template>
  <div class="p-5 space-y-4">
    <el-tabs>
      <el-tab-pane label="绩效核算结果">
        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="perfQuery.deptId" :fit-input-width="false" clearable filterable placeholder="科室"
                       style="width: 180px">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
            <el-date-picker v-model="perfQuery.costMonth" placeholder="核算月份" style="width: 130px"
                            type="month" value-format="YYYY-MM"/>
            <el-button :icon="Search" type="primary" @click="perfQuery.pageNum = 1; loadPerfList()">查询</el-button>
            <el-button :icon="Refresh"
                       @click="perfQuery.deptId = null; perfQuery.costMonth = ''; perfQuery.pageNum = 1; loadPerfList()">
              重置
            </el-button>
            <div class="ml-auto flex items-center gap-2">
              <el-select v-model="calcForm.deptId" :fit-input-width="false" data-testid="perf-calc-dept" filterable
                         placeholder="选择科室" style="width: 160px">
                <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
              </el-select>
              <el-date-picker v-model="calcForm.costMonth" placeholder="核算月份" style="width: 130px"
                              type="month" value-format="YYYY-MM"/>
              <el-button data-testid="perf-calc-btn" plain type="primary" @click="openCalc">执行核算</el-button>
            </div>
          </div>
          <el-table v-loading="perfLoading" :data="perfRows" border data-testid="perf-table" stripe>
            <el-table-column label="月份" prop="costMonth" width="85"/>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="130"/>
            <el-table-column align="right" label="收入" prop="revenue" width="110">
              <template #default="{ row }">{{ money(row.revenue) }}</template>
            </el-table-column>
            <el-table-column align="right" label="药占比" prop="drugRatio" width="80">
              <template #default="{ row }">
                {{ row.drugRatio != null ? (Number(row.drugRatio) * 100).toFixed(1) + '%' : '—' }}
              </template>
            </el-table-column>
            <el-table-column align="right" label="成本" prop="totalCost" width="110">
              <template #default="{ row }">{{ money(row.totalCost) }}</template>
            </el-table-column>
            <el-table-column align="right" label="结余" width="110">
              <template #default="{ row }">
                <span :class="surplusColor(row.surplus)">{{ money(row.surplus) }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" label="系数" prop="bonusRate" width="70">
              <template #default="{ row }">{{ Number(row.bonusRate || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column align="right" label="绩效金额" prop="perfAmount" width="120">
              <template #default="{ row }">
                <b>{{ money(row.perfAmount) }}</b>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="perfStatus" width="85">
              <template #default="{ row }">
                <el-tag :type="row.perfStatus === 3 ? 'success' : row.perfStatus === 2 ? 'primary' : 'info'"
                        size="small">
                  {{ ({1: '草稿', 2: '已核算', 3: '已发布'})[row.perfStatus] || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="核算时间" prop="updateTime" width="160">
              <template #default="{ row }">{{ (row.updateTime || '').replace('T', ' ').slice(0, 19) || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="perfQuery.pageNum" :page-size="perfQuery.pageSize"
                           :total="perfTotal" layout="total, prev, pager, next" @current-change="loadPerfList"/>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="科室月度成本">
        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="costQuery.deptId" :fit-input-width="false" clearable filterable placeholder="科室"
                       style="width: 180px">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
            <el-date-picker v-model="costQuery.costMonth" placeholder="核算月份" style="width: 130px"
                            type="month" value-format="YYYY-MM"/>
            <el-button :icon="Search" type="primary" @click="costQuery.pageNum = 1; loadCostList()">查询</el-button>
            <el-button v-perm="'report:perf:add'" class="ml-auto" data-testid="perf-cost-btn" plain type="primary"
                       @click="openCost">成本录入
            </el-button>
          </div>
          <el-table v-loading="costLoading" :data="costRows" border data-testid="perf-cost-table" stripe>
            <el-table-column label="月份" prop="costMonth" width="85"/>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="140"/>
            <el-table-column align="right" label="人力" prop="laborCost" width="100"/>
            <el-table-column align="right" label="药品" prop="drugCost" width="100"/>
            <el-table-column align="right" label="耗材" prop="materialCost" width="100"/>
            <el-table-column align="right" label="折旧" prop="depreciation" width="100"/>
            <el-table-column align="right" label="其他" prop="otherCost" width="100"/>
            <el-table-column align="right" label="合计" prop="totalCost" width="110">
              <template #default="{ row }"><b>{{ money(row.totalCost) }}</b></template>
            </el-table-column>
            <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip>
              <template #default="{ row }">{{ row.remark || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="costQuery.pageNum" :page-size="costQuery.pageSize"
                           :total="costTotal" layout="total, prev, pager, next" @current-change="loadCostList"/>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 成本录入 / 执行核算：拆到同目录兄弟组件，页面只管开关与递参 -->
    <PerformanceCostAdd v-model="costVisible" :depts="depts" @saved="loadCostList"/>
    <PerformanceCalcEdit v-model="calcVisible" :dept-id="calcForm.deptId" :cost-month="calcForm.costMonth"
                         @saved="loadPerfList"/>
  </div>
</template>

<script setup>
/**
 * 绩效与成本核算（G23，菜单 908，挂财务结算；壳页重写为真实接口驱动）
 *
 * 口径：收入=收费明细月度净额（收入-退款抵扣，按明细 dept_id 归属）；药占比=药费/收入；
 * 结余=收入-成本；绩效=max(0,结余)×提成系数（默认 0.06）。
 * 成本同科室同月唯一（重复录入拒绝）；核算结果重算覆盖。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {perfCostListPage, perfResultListPage} from '@/api/perf';
import {getDepartmentSelectList} from '@/api/system';
import PerformanceCalcEdit from './PerformanceCalcEdit.vue';
import PerformanceCostAdd from './PerformanceCostAdd.vue';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {money, surplusColor} from '@/lib/performance';
// ---------------- 科室 ----------------
const depts = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    depts.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
// ---------------- 成本录入（表单拆到 PerformanceCostAdd，页面只管开关） ----------------
const costVisible = ref(false);
const openCost = () => {
  costVisible.value = true;
};
// ---------------- 成本列表 ----------------
const costLoading = ref(false);
const costRows = ref([]);
const costTotal = ref(0);
const costQuery = reactive({deptId: null, costMonth: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadCostList = async () => {
  costLoading.value = true;
  try {
    const res = await perfCostListPage({
      deptId: costQuery.deptId ?? undefined,
      costMonth: costQuery.costMonth || undefined,
      pageNum: costQuery.pageNum, pageSize: costQuery.pageSize,
    });
    costRows.value = res?.data?.records || [];
    costTotal.value = Number(res?.data?.total || 0);
  } catch (e) {
    console.error('加载成本失败', e);
  } finally {
    costLoading.value = false;
  }
};
// ---------------- 执行核算（收入信息获取与核算提交拆到 PerformanceCalcEdit；科室/月份是本 tab 工具栏状态） ----------------
const calcVisible = ref(false);
const calcForm = reactive({deptId: null, costMonth: ''});
const openCalc = () => {
  if (!calcForm.deptId)
    return ElMessage.warning('请选择科室');
  if (!calcForm.costMonth)
    return ElMessage.warning('请选择核算月份');
  calcVisible.value = true;
};
// ---------------- 绩效结果 ----------------
const perfLoading = ref(false);
const perfRows = ref([]);
const perfTotal = ref(0);
const perfQuery = reactive({deptId: null, costMonth: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadPerfList = async () => {
  perfLoading.value = true;
  try {
    const res = await perfResultListPage({
      deptId: perfQuery.deptId ?? undefined,
      costMonth: perfQuery.costMonth || undefined,
      pageNum: perfQuery.pageNum, pageSize: perfQuery.pageSize,
    });
    perfRows.value = res?.data?.records || [];
    perfTotal.value = Number(res?.data?.total || 0);
  } catch (e) {
    console.error('加载绩效结果失败', e);
  } finally {
    perfLoading.value = false;
  }
};
onMounted(() => {
  loadDepts();
  loadCostList();
  loadPerfList();
});
</script>
